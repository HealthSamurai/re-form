(ns re-form.inputs.text-input-impl
  (:require [reagent.core :as r]
            [re-form.inputs.common :refer [errors-div]]))

(defn text-input [props]
  (let [my-onchange (fn [event on-change] (on-change (.. event -target -value)))]
    (fn [{:keys [value on-change errors] :as props}]
      [:div
       [:input (merge (dissoc props :errors)
                      {:type "text"
                       :on-change #(my-onchange % on-change)
                       :value value})]
       [errors-div errors]])))
