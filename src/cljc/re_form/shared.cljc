(ns re-form.shared
  (:require [clojure.string :as str]))

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
      (assoc (or m (vec (replicate k nil))) k v)
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
      (assoc-by-path (into [:re-form form-name :value] input-path) v)
      (assoc-by-path [:re-form form-name :flags input-path :dirty] true)))

(defn on-input-removed [db form-name input-path]
  (-> (put-validation-errors db form-name {input-path []})
      (dissoc-by-path (into [:re-form form-name :value] input-path))))
