(ns re-form.inputs.common
  (:require [garden.units :as u]))

(defn errors-div [errors]
  [:div.errors (map-indexed (fn [idx e] [:div.error {:key idx} e])
                            (or errors []))])

(defn label-wrapper-style
  [{:keys [m h h2 h3 selection-bg-color hover-bg-color border gray-color]}]
  [:*
   [:.re-label {:margin-bottom (u/px 2)}]
   [:.re-small-label
    {:font-size (u/px m)
     :line-height (u/px* m 1.5)
     :margin-top 0
     :margin-bottom (u/px 2)
     :color gray-color}]])

(defn label-wrapper
  [{:keys [label upper-description bottom-description]} input]
  [:div
   [:label.re-label label]
   [:label.re-small-label upper-description]
   input])
