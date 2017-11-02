(ns re-form.inputs.select-impl
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-form.inputs.common :as cmn]
            [goog.functions :refer [debounce]]
            [garden.units :as u]))

(defn select-style
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

(defn options-list [options label-fn on-change]
  [:div
   (for [i options]
     [:div.option
      {:key (pr-str i)
       :on-click (fn [_] (on-change i))}
      (label-fn i)])])

(defn select [{:keys [on-search debounce-interval]}]
  (let [state (r/atom {:active false})
        doc-click-listener (fn [e]
                             (when (and (not (cmn/has-ancestor (.-target e) (:node @state)))
                                        (:active @state))
                               (swap! state assoc :active false)))
        search-fn (if debounce-interval
                    (debounce on-search debounce-interval)
                    on-search)]
    (r/create-class
     {
      :component-did-mount
      (fn [this]
        (swap! state assoc :node (r/dom-node this))
        (.addEventListener js/document "click" doc-click-listener))

      :component-will-unmount
      #(.removeEventListener js/document "click" doc-click-listener)

      :reagent-render
      (fn [{:keys [value on-change label-fn value-fn options] :as props}]
        [:div.re-re-select
         [:div
          {:on-click #(swap! state assoc :active true)}
          [:span.triangle "▾"]
          (if value
            [:span.value
             [:span.value (label-fn value)]]
            [:span.choose-value
             (or (:placeholder props) "Select...")])]
         (when (:active @state)
           [:div.options
            [:input.re-search-search
             {:auto-focus true
              :on-change #(search-fn (.. % -target -value))}]
            [options-list options label-fn (comp #(swap! state assoc :active false)
                                                 on-change)]])])})))
