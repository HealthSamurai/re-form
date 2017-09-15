
(ns re-form.inputs.select-input-impl
  (:require [reagent.core :as r]
            [garden.units :as u]))

(def select-input-style
  [:.re-select
   {:position "relative"
    :background-color "white"
    :margin-left "5px"
    :min-width "10em"
    :padding "2px 5px"
    :border "1px solid #ddd"}
   [:.clear {:padding {:left (u/px 10)
                       :right (u/px 10)
                       :top (u/px 5)
                       :bottom (u/px 5)}
             :cursor "pointer"
             :position "absolute"
             :opactity 0.7
             :top (u/px 0)
             :right (u/px 0)
             :color :red}
    [:&:hover {:opacity 1 :background-color "#f1f1f1"}]]
   [:.options
    {:position "absolute"
     :background-color "white"
     :min-width "10em"
     :z-index 1000
     :left 0
     :top (u/px 40)
     :width "auto"
     :display "inline-block"
     :box-shadow "1px 1px 2px #ccc"
     :border "1px solid #ddd"}
    [:.re-search {:display "inline-block"
                  :width "90%"
                  :margin "5px"}]
    [:.option {:cursor "pointer"
                  :padding (u/px 10)}
     [:&:hover {:background-color "#f1f1f1"}]]]])

(defn select-input [_]
  (let [state (r/atom {:active false})]
    (fn [{:keys [value on-change value-fn label-fn items] :as props}]
      (let [label-fn (or label-fn pr-str)
            value-fn (or value-fn identity)]
        [:div.re-select
         {:on-click #(swap! state update :active not)}
         (if value
           [:span.value
            [:span.value value (or (label-fn value) (value-fn value) (str value))]]
           [:span.choose-value
            (or (:placeholder props) "Select...")])
         (when (:active @state)
           [:div.options
            (for [i items] ^{:key (pr-str i)}
              [:div.option
               {:on-click (fn [_] (on-change (value-fn i)))}
               (label-fn i)])])]))))
