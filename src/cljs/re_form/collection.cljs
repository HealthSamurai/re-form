(ns re-form.collection
  (:require [reagent.core :as c]
            [re-frame.core :as rf]
            [re-form.context :as ctx]))

(defn- drop-idx [v idx]
  (vec (concat (subvec v 0 idx) (subvec v (inc idx) (count v)))))

(rf/reg-event-fx
 :re-form/new-coll-item
 (fn [_ [_ form-name path new-item]]
   (let [array (rf/subscribe [:re-form/input-value form-name path])]
     {:dispatch [:re-form/input-changed form-name (conj path (count @array)) new-item]})))

(defn- add-button* [{:keys [form-name path new-item-value] :as props} & body]
  [:div.add-button {:on-click (fn [] (rf/dispatch [:re-form/new-coll-item form-name path (or new-item-value {})]))}
   body])

(defn add-button [props & body]
  (into [ctx/get-context props add-button*] body))

(defn- del-button* [{:keys [form-name path] :as props} & body]
  [:div.del-button {:on-click (fn [idx] (rf/dispatch [:re-form/input-removed form-name path]))}
   body])

(defn del-button [props & body]
  (into [ctx/get-context props del-button*] body))

(defn- collection* [{:keys [form-name path new-item-value] :as props} & body]
  (let [array (rf/subscribe [:re-form/input-value form-name path])]
    (fn [props & body]
      [:div.collection
       (doall
        (for [[idx item] (map-indexed (fn [idx itm] [idx itm]) @array)]
          [:div.collection-item {:key idx}
           (into [ctx/set-context {:form-name form-name :base-path (conj path idx)}] body)]))])))

(defn collection [props & body]
  (into [ctx/get-context props collection*] body))
