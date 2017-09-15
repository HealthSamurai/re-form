(ns re-form.collection
  (:require [reagent.core :as c]
            [re-frame.core :as rf]
            [re-form.context :as ctx]))

(defn collection* [{:keys [form path] :as props} & body]
  (let [array (rf/subscribe [:re-form/input-value form path])]
    (fn [props & body]
      [:div.collection

       (doall
        (for [[idx item] (map-indexed (fn [idx itm] [idx itm]) @array)]
          [:div.collection-item {:key (pr-str item)}
           [ctx/set-context {:form-name form :base-path (conj path idx)}
            (into [:div.item] body)]]))])))

(defn collection [props & body]
  (into [ctx/get-context props collection*] body))
