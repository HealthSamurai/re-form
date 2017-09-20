(ns re-form.inputs.select-xhr-impl
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<!]]
            [garden.units :as u]))

(def select-xhr-style
  [:.re-select-xhr
   {:position "relative"
    :background-color "white"
    :margin-left "5px"
    :min-width "10em"
    :padding "2px 5px"
    :border "1px solid #ddd"}
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
     [:&.active {:background-color "#f1f1f1"}]
     [:&:hover {:background-color "#f1f1f1"}]]]])

(defn- suggest-set-callback [state suggs]
  (swap! state assoc :suggestions suggs))

(defn- on-change-update [state suggest-fn node]
  (let [query (.-textContent node)]
    (when-not (empty? query)
      (go (suggest-set-callback state (<! (suggest-fn query)))))))

;; TODO:
;; text input to query
;; on component-did-mount do r/dom-node and .addEventListener 'input' to track
;; contenteditable changes
;; debounce
(defn select-xhr-input [{:keys [suggest-fn]}]
  (let [state (r/atom {:active false :suggestions []})]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [root-node (r/dom-node this)
              node (first (array-seq (.getElementsByClassName root-node "choose-value")))]
          (.addEventListener node "input" #(on-change-update state suggest-fn node))))

      :reagent-render
      (fn [{:keys [value on-change value-fn label-fn match-fn suggest-fn placeholder]}]
        (let [label-fn (or label-fn pr-str)
              value-fn (or value-fn identity)
              match-fn (or match-fn
                           (fn [v] (->> (:suggestions @state)
                                        (filter #(= (value-fn %) v))
                                        first label-fn)))]
          [:div.re-select-xhr
           {:on-click #(swap! state update :active not)}
           (if value
             [:span.value
              [:span.value (match-fn value) ]]
             [:span.choose-value
              {:contentEditable true}])
           (when (:active @state)
             [:div.options
              (for [i (:suggestions @state)] ^{:key (label-fn i)}
                [:div.option
                 {:on-click (fn [_] (on-change (value-fn i)))
                  :class (when (= value (value-fn i)) "active")}
                 (label-fn i)])])]))}))
  )
