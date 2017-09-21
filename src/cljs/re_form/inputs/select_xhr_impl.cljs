(ns re-form.inputs.select-xhr-impl
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<! chan timeout]]
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
     :max-height (u/px 500)
     :overflow-y "auto"
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

(defn debounced-callback [state suggest-fn in ms]
  (go-loop [last-val nil]
    (let [val (if (nil? last-val) (<! in) last-val)
          timer (timeout ms)
          [new-val ch] (alts! [in timer])]
      (condp = ch
        timer (do (swap! state assoc :suggestions (<! (suggest-fn val)))
                  (recur nil))
        in (when new-val (recur new-val))))))

(defn- on-change-update [ch node]
  (let [query (.-textContent node)]
    (when-not (empty? query)
      (go (>! ch query)))))

(defn select-xhr-input [{:keys [suggest-fn]}]
  (let [state (r/atom {:active false :suggestions []})
        on-change-ch (chan)]
    (debounced-callback state suggest-fn on-change-ch 100)
    (r/create-class
     {
      :component-did-mount
      (fn [this]
        (let [node (-> (r/dom-node this)
                       (.getElementsByClassName "choose-value")
                       array-seq
                       first)]
          (swap! state assoc :node node)
          (.addEventListener node "input" #(on-change-update on-change-ch node))))

      :reagent-render
      (fn [{:keys [value on-change value-fn label-fn match-fn suggest-fn placeholder]}]
        (let [label-fn (or label-fn pr-str)
              value-fn (or value-fn identity)
              match-fn (or match-fn
                           (fn [v] (->> (:suggestions @state)
                                        (filter #(= (value-fn %) v))
                                        first label-fn)))]
          [:div.re-select-xhr
           {:on-click (fn [_] (swap! state update :active not)
                        (set! (.-textContent (:node @state)) "")
                        (when (:active @state) (.focus (:node @state))))}
           [:span.value {:style {:display (if (:active @state) "none" "inline")}} (match-fn value)]
           [:span.choose-value {:contentEditable true
                                :style {:display "inline-block"}}]
           (when (:active @state)
             [:div.options
              (for [i (:suggestions @state)] ^{:key (label-fn i)}
                [:div.option
                 {:on-click (fn [_] (on-change (value-fn i)))
                  :class (when (= value (value-fn i)) "active")}
                 (label-fn i)])])]))})))
