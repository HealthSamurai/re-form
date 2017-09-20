(ns re-form.collection
  (:require [reagent.core :as c]
            [re-frame.core :as rf]
            [re-form.context :as ctx]))

(defn- drop-idx [v idx]
  (vec (concat (subvec v 0 idx) (subvec v (inc idx) (count v)))))

(defn- collection* [{:keys [form-name path new-item-value] :as props} & body]
  (let [array (rf/subscribe [:re-form/input-value form-name path])
        on-new-item (fn [] (rf/dispatch [:re-form/input-changed form-name path
                                         (conj @array (or new-item-value {}))]))

        remove-item (fn [idx] (rf/dispatch [:re-form/input-changed form-name path
                                            (drop-idx @array idx)]))]

    (fn [props & body]
      [:div.collection
       [:button {:on-click on-new-item} "new item"]

       (doall
        (for [[idx item] (map-indexed (fn [idx itm] [idx itm]) @array)]
          [:div.collection-item {:key idx}
           (into [ctx/set-context {:form-name form-name :base-path (conj path idx)}] body)
           [:button {:on-click #(remove-item idx)} "x"]]))])))

(defn collection [props & body]
  (into [ctx/get-context props collection*] body))
