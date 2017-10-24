(ns re-form.inputs.select-input-impl
  (:require [reagent.core :as r]
            [garden.units :as u]))

(defn select-input-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.re-select
   {:position :relative
    :background-color :white
    :width (u/px 600)
    :border-radius (u/px 2)
    :padding [[(u/px 8) (u/px 12)]]
    :line-height (u/px* h 1.5)
    :border "1px solid #ddd"}
   [:span.triangle {:color "gray"
                    :margin-left (u/px-div h 2)}]
   [:&:hover {:cursor "pointer"
              :border "1px solid #ccc"}]
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
     :top (u/px+ (* 1.5 h) 16 10)
     :width "auto"
     :display "inline-block"
     :box-shadow "1px 1px 2px #ccc"
     :border "1px solid #ddd"}
    [:.re-search {:display "inline-block"
                  :width "90%"
                  :margin "5px"}]
    [:.option {:cursor "pointer"
               :padding-left (u/px h)
               :padding-right (u/px h)
               :line-height (u/px- h3 2)}
     [:&.active {:background-color "#f1f1f1"}]
     [:&:hover {:background-color "#f1f1f1"}]]]])

(defn select-input [_]
  (let [state (r/atom {:active false})]
    (fn [{:keys [value on-change value-fn label-fn match-fn items] :as props}]
      (let [label-fn (or label-fn pr-str)
            value-fn (or value-fn identity)
            match-fn (or match-fn
                         (fn [v] (->> items
                                      (filter #(= (value-fn %) v))
                                      first label-fn)))]
        [:div.re-select
         {:on-click #(swap! state update :active not)}
         (if value
           [:span.value
            [:span.value (match-fn value) #_(or (label-fn value) (value-fn value) (str value))]]
           [:span.choose-value
            (or (:placeholder props) "Select...")])
         [:span.triangle "▾"]
         (when (:active @state)
           [:div.options
            (for [i items] ^{:key (pr-str i)}
              [:div.option
               {:on-click (fn [_] (on-change (value-fn i)))
                :class (when (= value (value-fn i)) "active")}
               (label-fn i)
               ])])]))))
