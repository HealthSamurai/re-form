(ns re-form.inputs.text-input-impl
  (:require [reagent.core :as r]
            [re-form.inputs.common :refer [errors-div]]
            [garden.units :as u]))

(defn text-input-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.re-input
   {:border border
    :padding [[(u/px 8) (u/px 12)]]
    :border-radius (u/px 2)
    :line-height (u/px* h 1.5)}])

(defn text-input [props]
  (let [my-onchange (fn [event on-change] (on-change (.. event -target -value)))]
    (fn [{:keys [value on-change errors] :as props}]
      [:div
       [:input.re-input (merge (dissoc props :errors)
                               {:type (or (:type props) "text")
                                :on-change #(my-onchange % on-change)
                                :value value})]
       [errors-div errors]])))
