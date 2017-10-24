(ns re-form.inputs.text-input-impl
  (:require [reagent.core :as r]
            [re-form.inputs.common :refer [errors-div]]
            [garden.units :as u]))

(def h 16)
(def h2 24)
(def h3 38)
(def selection-bg-color "#007bff")
(def hover-bg-color "#f1f1f1")
(def border "1px solid #ddd")

(def text-input-style
  [:.re-input
   {:border border
    :padding {:left (u/px h) :right (u/px h)}
    :line-height (u/px h3)}])

(defn text-input [props]
  (let [my-onchange (fn [event on-change] (on-change (.. event -target -value)))]
    (fn [{:keys [value on-change errors] :as props}]
      [:div
       [:input.re-input (merge (dissoc props :errors)
                               {:type (or (:type props) "text")
                                :on-change #(my-onchange % on-change)
                                :value value})]
       [errors-div errors]])))
