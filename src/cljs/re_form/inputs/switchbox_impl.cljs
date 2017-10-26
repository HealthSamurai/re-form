(ns re-form.inputs.switchbox-impl
  (:require
   [reagent.core :as reagent]
   [garden.units :as u]
   [re-frame.core :as rf]))
 
(defn re-switch-box-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border gray-color]}]
  [:.re-switch
   {:font-decoration :none}
   [:.re-label {:line-height (u/px h3)
                :display :inline-block}]
   [:.re-switch-line {:width (u/px* h3 2)
                      :top (u/px 5)
                      :height (u/px h2)
                      :display :inline-block
                      :background-color gray-color
                      :border-radius (u/px h2)
                      :margin-right (u/px 12)
                      :position :relative}
    [:.re-box {:width (u/px h3)
               :height (u/px h3)
               :transition "left 300ms ease-out"
               :background-color :white
               :border border
               :border-radius (u/percent 50)
               :position :absolute
               :top (u/px- (/ h2 2) (/ h3 2))
               :left 0}]]
   [:&.re-checked
    [:.re-box {:left (u/px h3)}]
    [:.re-switch-line {:background-color "#007bff"}]]])

(defn switch-box [_]
  (fn [{:keys [value on-change label]}]
    (let [local-onchange (partial on-change (not value))]
      [:a.re-switch
       {:href "javascript:void(0)"
        :class (when value :re-checked)
        :on-key-press (fn [event] (when (= 32 (.-which event)) (local-onchange)))
        :on-click local-onchange}
       [:span.re-switch-line [:span.re-box]]
       (when label [:span.re-label label])])))
