(ns re-form.inputs.switchbox-impl
  (:require
   [reagent.core :as reagent]
   [garden.units :as u]
   [re-frame.core :as rf]))

(def re-switch-box-style
  [:.re-switch
   {:font-decoration "none"}
   [:.re-switch-line {:width (u/px 40)
                      :height (u/px 20)
                      :display "inline-block"
                      :background-color "gray"
                      :border-radius "10px"
                      :margin-left (u/px 10)
                      :margin-right (u/px 20)
                      :position "relative"}
    [:.re-box {:width (u/px 30)
               :transition "left 300ms ease-out"
               :height (u/px 30)
               :background-color "white"
               :border "1px solid #ddd"
               :box-shadow "1px 1px 2px #ddd"
               :border-radius "60%"
               :position "absolute"
               :top (u/px -5)
               :left (u/px -10)}]]
   [:&.re-checked [:.re-box {:left (u/px 10)}]]])

(defn switch-box [_]
  (fn [{:keys [value on-change label]}]
    (let [local-onchange (partial on-change (not value))]
      [:a.re-switch
       {:href "javascript:void(0)"
        :class (when value "re-checked")
        :on-key-press (fn [event] (when (= 32 (.-which event)) (local-onchange)))
        :on-click local-onchange}
       [:span.re-switch-line [:span.re-box]]
       (when label [:span.re-label label])])))
