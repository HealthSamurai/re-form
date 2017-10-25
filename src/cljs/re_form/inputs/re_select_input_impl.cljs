(ns re-form.inputs.re-select-input-impl
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [garden.units :as u]))

(defn re-select-input-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.re-re-select
   {:position :relative
    :display "inline-block"
    :min-width "30em"
    :background-color :white
    :border-radius (u/px 2)
    :padding [[(u/px-div h 2) (u/px 12)]]
    :line-height (u/px h2)
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
     :top (u/px* 5 (/ h 2))
     :width "100%"
     :display "inline-block"
     :box-shadow "1px 1px 2px #ccc"
     :border "1px solid #ddd"}
    [:.re-search-search {:width "100%"}]
    [:.option {:cursor "pointer"
               :display "block"
               :padding-left (u/px h)
               :padding-right (u/px h)
               :line-height (u/px h3)}
     [:&.active {:background-color hover-bg-color}]
     [:&:hover {:background-color hover-bg-color}]]]])

(defn options [options-sub label-fn on-change]
  [:div
   (for [i @options-sub]
     [:div.option 
      {:key (pr-str i)
       :on-click (fn [_] (on-change i))}
      (label-fn i)])])

(defn re-select-input [opts]
  (let [state (r/atom {:active false})
        on-search (fn [ev]
                    (when-let [se (:search-event opts)]
                      (rf/dispatch [(:event se) (.. ev -target -value) se])))
        activate (fn [_]
                   (when-let [se (:search-event opts)]
                     (rf/dispatch [(:event se) nil se]))
                   (swap! state update :active not))]
    (fn [{:keys [value on-change label-fn value-fn options-sub] :as props}]
      [:div.re-re-select
       {:on-click activate}
       [:span.triangle "▾"]
       (if value
         [:span.value
          [:span.value (label-fn value)]]
         [:span.choose-value
          (or (:placeholder props) "Select...")])
       (when (:active @state)
         [:div.options
          [:input.re-search-search {:auto-focus true :on-change on-search}]
          [options options-sub label-fn on-change]])])))
