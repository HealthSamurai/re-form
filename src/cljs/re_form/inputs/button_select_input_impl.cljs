(ns re-form.inputs.button-select-input-impl
  (:require [reagent.core :as r]
            [garden.units :as u]))

(def button-select-style
  [:div.re-radio-buttons
   {:display "inline-block"
    :box-shadow "0px 0px 3px #ddd"
    :border-radius "15px";
    :margin-left "10px"}
   [:div.option {:cursor "pointer"
                 :transition "background-color 200ms, color 100ms"
                 :background-color "white"
                 :opacity 0.7
                 :border-right "1px solid #ddd"
                 :padding {:top (u/px 5)
                           :left (u/px 10)
                           :right (u/px 10)
                           :bottom (u/px 5)}
                 :display "inline-block"}

    [:&:hover {:background-color "#f1f1f1"
               :opacity 1}]
    [:&:first-child {:border-radius "15px 0px 0px 15px"
                     :padding-left (u/px 15)}]
    [:&:last-child {:border-radius "0px 15px 15px 0px"
                    :padding-right (u/px 15)
                    :border-right "none"}]
    [:&.active {:background-color "#007bff"
                :opacity 1
                :color "white"}]]])

(defn button-select-input [_]
  (fn [{:keys [value on-change label-fn items]}]
    [:div.re-radio-buttons
     (doall
      (for [i items]
        [:div.option
         {:key (pr-str i)
          :class (when (= i value) "active")
          :on-click (partial on-change i)}
         [:span.value (label-fn i)]]))]))
