(ns re-form.inputs.text-input-impl
  (:require [reagent.core :as r]))

(defn text-input [props]
  (let [my-onchange (fn [event on-change] (on-change (.. event -target -value)))]
    (fn [{:keys [value on-change] :as props}]
      [:input (merge {:type "text"}
                     (dissoc props :form-name)
                     {:on-change #(my-onchange % on-change)
                      :value value})])))
