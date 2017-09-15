(ns re-form.shared
  (:require [clojure.string :as str]))

(defn insert-by-path [m [k & ks :as path] value]
  (let [v (if ks
            (insert-by-path (get m k) ks value)
            value)]
    (if (integer? k)
      (assoc (or m (vec (replicate k nil))) k v)
      (assoc (or m {}) k v))))

(defn state-path [{frm :form :as opts}]
  (into []
        (concat (:path frm)
                [:state]
                (conj (or (:path opts) []) (:name opts)))))

(defn errors-path [{frm :form :as opts}]
  (concat (:path frm)
          [:state]
          (conj (or (:path opts) []) (:name opts))
          [:errors]))

(defn dirty-path [{frm :form :as opts}]
  (concat (:path frm)
          [:state]
          (conj (or (:path opts) []) (:name opts))
          [:dirty]))

(defn touched-path [{frm :form :as opts}]
  (concat (:path frm)
          [:state]
          (conj (or (:path opts) []) (:name opts))
          [:touched]))

(defn get-errors [db opts]
  (get-in db (errors-path opts)))

(defn put-validation-errors [db form-name errors]
  (update-in db [:re-form form-name :errors] merge errors))

(defn on-change [db form-name input-path v]
  (-> db
      (insert-by-path (into [:re-form form-name :value] input-path) v)
      (insert-by-path [:re-form form-name :dirty] true)
      (insert-by-path (into [:re-form form-name :state] (conj input-path :dirty)) true)))
