(ns re-form.shared
  (:require [clojure.string :as str]))

(defn filter-vals [pred m]
  (into {} (filter (fn [[k v]] (pred v))
                   m)))

(defn insert-by-path [m [k & ks :as path] value]
  (let [v (if ks
            (insert-by-path (get m k) ks value)
            value)]
    (if (integer? k)
      (assoc (or m (vec (replicate k nil))) k v)
      (assoc (or m {}) k v))))

(defn put-validation-errors [db form-name errors]
  (update-in db [:re-form form-name :errors] (fn [current-errs]
                                               (filter-vals (complement empty?)
                                                            (merge current-errs errors)))))

(defn on-change [db form-name input-path v]
  (-> db
      (insert-by-path (into [:re-form form-name :value] input-path) v)
      (insert-by-path [:re-form form-name :dirty] true)
      (insert-by-path (into [:re-form form-name :state] (conj input-path :dirty)) true)))
