(ns re-form.inputs.select-xhr-impl
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<! chan timeout]]
            [garden.units :as u]))

(def select-xhr-style
  [:.re-select-xhr
   {:position "relative"
    :background-color "white"
    :padding "2px 5px"
    :border "1px solid #ddd"}

   [:input
    {:border "none"
     :padding "0"
     :width "100%"
     :outline "none"}]

   [:.suggestions
    {:position "absolute"
     :background-color "white"
     :z-index 1000
     :left 0
     :top (u/px 38)
     :width "auto"
     :display "block"
     :box-shadow "1px 1px 2px #ccc"
     :max-height (u/px 300)
     :overflow-y "auto"
     :border "1px solid #ddd"}
    [:.option {:cursor "pointer" :padding (u/px 10)}
     [:&.active {:background-color "#f1f1f1"}]
     [:&:hover {:background-color "#f1f1f1"}]]]])

(defn- debounced-callback [state suggest-fn in ms]
  (go-loop [last-val nil]
    (let [val (if (nil? last-val) (<! in) last-val)
          timer (timeout ms)
          [new-val ch] (alts! [in timer])]
      (condp = ch
        timer (do (swap! state assoc :suggestions (<! (suggest-fn val)))
                  (recur nil))
        in (when new-val (recur new-val))))))

(defn- lookup-suggestions [ch query]
  (when-not (empty? query)
    (go (>! ch query))))

(defn select-xhr-input [{:keys [suggest-fn]}]
  (let [state (r/atom {:current-text nil
                       :focused false
                       :suggestions []})

        on-change-ch (chan)
        handle-input-change
        (fn [event]
          (let [text (.-value (.-target event))]
            (swap! state assoc :current-text text)
            (lookup-suggestions on-change-ch text)))]

    (debounced-callback state suggest-fn on-change-ch 100)

    (r/create-class
     {:reagent-render
      (fn [{:keys [value on-change value-fn label-fn match-fn suggest-fn placeholder]}]
        (let [label-fn (or label-fn pr-str)
              value-fn (or value-fn identity)
              match-fn (or match-fn
                           (fn [v] (->> (:suggestions @state)
                                        (filter #(= (value-fn %) v))
                                        first label-fn)))]
          [:div.re-select-xhr
           [:input {:type "text"
                    :on-change handle-input-change
                    :on-focus #(swap! state assoc :focused true)
                    :on-blur #(js/setTimeout (fn [] (swap! state assoc :focused false :current-text nil)) 100)
                    :value (or (:current-text @state)
                               (label-fn value))}]

           (when (and (:focused @state) (not (empty? (:suggestions @state))))
             [:div.suggestions
              (for [i (:suggestions @state)] ^{:key (pr-str (value-fn i))}
                [:div.option {:on-click (fn []
                                         (when on-change
                                           (on-change (value-fn i)))
                                         (swap! state assoc :current-text nil))}
                 (label-fn i)])])]))})))
