(ns re-form.switchbox
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

(defn switch-box [{ lbl :label :as opts}]
  (let [v (rf/subscribe [:re-form/value opts])
        on-change (fn [ev] (rf/dispatch [:re-form/update opts (not @v)]))
        on-key-press  (fn [ev] (when (= 32 (.. ev -which)) (on-change ev)))]
    (fn [props]
      [:a.re-switch
       {:href "javascript:void(0)"
        :class (when @v "re-checked")
        :on-key-press on-key-press
        :on-click on-change}
       [:pre (pr-str @v)]
       [:span.re-switch-line [:span.re-box]]
       (when lbl [:span.re-label lbl])])))
