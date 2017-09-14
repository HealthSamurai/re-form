(ns re-form.inputs.radio-input-impl
  (:require [reagent.core :as r]
            [garden.units :as u]))

(def radio-input-styles
  [:div.re-radio-group
   {:display "inline-block"}
   [:div.option {:cursor "pointer"
                 :opacity 0.7
                 :border-radius (u/px 15)
                 :padding {:top (u/px 5)
                           :left (u/px 10)
                           :right (u/px 10)
                           :bottom (u/px 5)}}
    [:.radio {:border "1px solid #ddd"
              :border-radius "50%"
              :background-color "white"
              :display "inline-block"
              :vertical-align "middle"
              :margin-right (u/px 5)
              :width (u/px 20)
              :height (u/px 20)}]
    [:&.active {}
     [:.radio {:background-color "#007bff"}]]
    [:&:hover {:opacity 1 :background-color "#f1f1f1"}]]])

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
