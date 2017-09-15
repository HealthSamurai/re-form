(ns re-form.collection
  (:require [reagent.core :as c]
            [re-form.context :as ctx]))

(defn collection* [{:keys [form path] :as props} & body]
  (let [idx 0]
    [:div.collection
     [ctx/set-context {:form-name form :base-path (conj path idx)}
      (into [:div.item] body)]]))

(defn collection [props & body]
  (into [ctx/get-context props collection*] body))
