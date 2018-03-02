(ns re-form.inputs.select-input-impl
  (:require [reagent.core :as r]
            [re-form.inputs.common :as cmn]
            [garden.units :as u]))

(defn select-input-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.re-select
   {:position :relative
    :display "inline-block"
    :background-color :white
    :border-radius (u/px 2)
    :padding [[(u/px-div (- h3 h2) 2) (u/px 12)]]
    :line-height (u/px* h 1.5)
    :border "1px solid #ddd"}
   [:span.triangle {:color "gray"
                    :margin-right (u/px-div h 2)}]
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
     :z-index 1000
     :left (u/px -1)
     :top (u/px h3)
     :width "auto"
     :display "inline-block"
     :box-shadow "1px 1px 2px #ccc"
     :border "1px solid #ddd"}
    [:.re-search {:display "inline-block"
                  :width "90%"
                  :margin "5px"}]
    [:.option {:cursor "pointer"
               :display "block"
               :padding-left (u/px h)
               :padding-right (u/px h)
               :line-height (u/px h3)}
     [:&.active {:background-color hover-bg-color}]
     [:&:hover {:background-color hover-bg-color}]]]])

(defn select-input [{:keys [on-blur]}]
  (let [state (r/atom {:active false})
        my-on-blur (or on-blur identity)
        doc-click-listener
        (fn [e]
          (when (and (not (cmn/has-ancestor (.-target e) (:root-node @state)))
                     (:active @state))
            (my-on-blur e)
            (swap! state assoc :active false)))]
    (r/create-class
     {
      :component-did-mount
      (fn [this]
        (swap! state assoc :root-node (r/dom-node this))
        (.addEventListener js/document "click" doc-click-listener))

      :component-will-unmount
      (fn [this]
        (.removeEventListener js/document "click" doc-click-listener))

      :reagent-render
      (fn [{:keys [class value on-change value-fn label-fn match-fn items on-blur] :as props}]
        (let [label-fn (or label-fn pr-str)
              value-fn (or value-fn identity)
              match-fn (or match-fn
                           (fn [v] (->> items
                                        (filter #(= (value-fn %) v))
                                        first label-fn)))]
          [:div.re-select
           {:on-click #(swap! state update :active not)
            :class class}
           [:span.triangle "▾"]
           (if value
             [:span.value
              [:span.value (match-fn value) #_(or (label-fn value) (value-fn value) (str value))]]
             [:span.choose-value
              (or (:placeholder props) "Select...")])

           (when (:active @state)
             [:div.options
              (for [i items] ^{:key (pr-str i)}
                [:div.option
                 {:on-click (fn [_] (on-change (value-fn i)))
                  :class (when (= value (value-fn i)) "active")}
                 (label-fn i)
                 ])])]))})))
