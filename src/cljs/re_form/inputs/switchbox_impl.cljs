(ns re-form.inputs.switchbox-impl
  (:require
   [reagent.core :as reagent]
   [garden.units :as u]
   [re-frame.core :as rf]))
 
(def h 16)
(def h2 24)
(def h3 38)
(def selection-bg-color "#007bff")
(def hover-bg-color "#f1f1f1")
(def border "1px solid #ddd")

(def re-switch-box-style
  [:.re-switch
   {:font-decoration "none"}
   [:.re-label {:line-height (u/px h3)
                :display "inline-block"}]
   [:.re-switch-line {:width (u/px h3)
                      :top (u/px 5)
                      :height (u/px h2)
                      :display "inline-block"
                      :background-color "gray"
                      :border-radius (u/px 12)
                      :margin-right (u/px-div h 2)
                      :position "relative"}
    [:.re-box {:width (u/px h2)
               :height (u/px h2)
               :transition "left 300ms ease-out"
               :background-color "white"
               :border border
               :border-radius (u/px 12)
               :position "absolute"
               :top 0
               :left 0}]]
   [:&.re-checked
    [:.re-box {:left (u/px 14)}]
    [:.re-switch-line {:background-color "#007bff"}]]])

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
