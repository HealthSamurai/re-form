(ns re-form.inputs.radio-input-impl
  (:require [reagent.core :as r]
            [garden.units :as u]))

(defn radio-input-styles
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:div.re-radio-group
   {:display "inline-block"}
   [:div.option {:cursor "pointer"
                 :border-radius (u/px 2)
                 :line-height (u/px h3)
                 :padding {:right (u/px h)}}
    [:.radio {:border border
              :border-radius "50%"
              :background-color "white"
              :display "inline-block"
              :vertical-align "middle"
              :margin-right (u/px-div h 2)
              :margin-top (u/px -3)
              :width (u/px h2)
              :height (u/px h2)}]
    [:&.active {}
     [:.radio {:background-color selection-bg-color}]]
    [:&:hover {:opacity 1 :background-color hover-bg-color}]]])

(defn radio-input [{:keys [value on-change value-fn label-fn items] :as props}]
  (let [label-fn (or label-fn pr-str)
        value-fn (or value-fn identity)]
    [:div.re-radio-group
     (doall
      (for [i items]
        [:div.option
         {:key (pr-str i)
          :class (when (= (value-fn i) value) "active")
          :on-click #(on-change (value-fn i))}
         [:span.radio]
         [:span.value (label-fn i)]]))]))
