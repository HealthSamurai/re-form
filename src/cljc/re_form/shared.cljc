(ns re-form.shared
  (:require [clojure.string :as str]
            [clojure.set :as set]))


(defn operand [ex]
  (fn [e]
    (if (coll? ex)
      (get-in e ex)
      (if (keyword? ex)
        (get e ex)
        ex))))

(def cmp {:= = :< < :> > :<= <= :>= >=})


(defn pred [[op l r]]
  #((get cmp op)
    ((operand l) %) ((operand r) %)))

(defn comp-expr [expr]
  (fn [coll]
    (filter (pred expr) coll)))

(defn and-expr [[op & exprs]]
  (fn [coll]
    (reduce #(%2 %1)
            coll
            (map comp-expr exprs) )))

(defn or-expr [[op & exprs]]
  (fn [coll]
    (reduce #(concat %1 (%2 coll))
            []
            (map comp-expr exprs) )))

(defn not-expr [[op l]]
  (fn [coll]
    (filter #(not ((operand l) %)) coll)))

(defn query [m q]
  (case (first q)
    :and ((and-expr q) m)
    :or  ((or-expr q) m)
    :not ((not-expr q) m)
    ((comp-expr q) m)))

(defn getin [m path]
  (reduce
   (fn [acc p]
     (if (map? p)
       (let [res (query acc (:get p) )]
         (if (empty? res)
           (:set p)
           (first res)))
       (get acc p)))
   m path))

(defn indexof [x coll]
  (first (keep-indexed #(when (= %2 x) %1) coll)))

(defn find-idx [m expr]
  (indexof (first (query m expr)) m))

(defn setin [m [k & ks :as path] value]
  (let [v (if ks
            (if (map? k)
              (setin (first (query m (:get k))) ks value)
              (setin (get m k) ks value))
            value)]
    (cond
      (integer? k)
      (assoc (or m (vec (repeat k nil))) k v)

      (map? k)
      (let [v (merge (:set k) v)
            idx (find-idx m (:get k))]
        (if idx
          (assoc m idx  v)
          (conj (or m []) v)))

      :else
      (assoc m k v)
      )))


(defn- filter-vals [pred m]
  (into {} (filter (fn [[k v]] (pred v))
                   m)))

(defn- dissoc-index [v idx]
  (if (> (count v) idx)
    (vec (concat (subvec v 0 idx) (subvec v (inc idx) (count v))))
    v))

(defn- assoc-by-path [m [k & ks :as path] value]
  (let [v (if ks
            (assoc-by-path (get m k) ks value)
            value)]
    (if (integer? k)
      (assoc (or m (vec (repeat k nil))) k v)
      (assoc (or m {}) k v))))

(defn- dissoc-by-path [m path]
  (let [path-butlast (butlast path)
        path-last (last path)
        not-found (gensym "not-found")]
    (if (= (get-in m path-butlast not-found) not-found)
      m
      (if (integer? path-last)
        (update-in m path-butlast dissoc-index path-last)
        (update-in m path-butlast dissoc path-last)))))

(defn put-validation-errors [db form-name errors]
  (update-in db [:re-form form-name :errors] (fn [current-errs]
                                               (filter-vals (complement empty?)
                                                            (merge current-errs errors)))))

(defn add-validation-errors [db form-name path errors]
  (update-in db [:re-form form-name :errors]
             (fn [errs]
               (assoc errs path (if (get errs path)
                                  (into (get errs path) errors)
                                  errors)))))

(defn on-input-changed [db form-name input-path v]
  (-> db
      (setin (into [:re-form form-name :value] input-path) v)
      (setin [:re-form form-name :flags input-path :dirty] true)))

(defn on-input-removed [db form-name input-path]
  (-> (put-validation-errors db form-name {input-path []})
      (dissoc-by-path (into [:re-form form-name :value] input-path))))
