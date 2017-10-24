(ns re-form.inputs.text-input-impl
  (:require [reagent.core :as r]
            [re-form.inputs.common :refer [errors-div]]
            [garden.units :as u]))

(defn text-input-style
  [{:keys [h h2 h3 w selection-bg-color hover-bg-color border radius]}]
  [:.re-input-wrap
   {:display "inline-block"}
   [:.re-input
    {:border border
     :padding [[0 (u/px w)]]
     :border-radius (u/px radius)
     :line-height (u/px* h3)}]])

(defn text-input [props]
  (let [my-onchange (fn [event on-change] (on-change (.. event -target -value)))]
    (fn [{:keys [value on-change errors] :as props}]
      [:div.re-input-wrap
       [:input.re-input (merge (dissoc props :errors)
                               {:type (or (:type props) "text")
                                :on-change #(my-onchange % on-change)
                                :value value})]
       [errors-div errors]])))
