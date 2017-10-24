(ns re-form.inputs.select-xhr-impl
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<! chan timeout]]
            [garden.units :as u]))

(defn select-xhr-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border gray-color]}]
  [:.re-select-xhr
   {:position :relative
    :border-radius (u/px 2)
    :width (u/px 600)
    :background-color :white
    :vertical-align :middle
    :padding [[(u/px 8) (u/px 12)]]
    :border border}

   [:input.query
    {:border "none!important"
     :font-size (u/px h)
     :line-height (u/px (* h 1.5))
     :position :relative
     :box-shadow "none"
     :padding "0"
     :outline "none"}]

   [:div.controls
    {:position :absolute
     :top "2px"
     :color gray-color 
     :right (u/px 12)}]

   [:.suggestions
    {:position "absolute"
     :background-color "white"
     :z-index 1000
     :left 0
     :top (u/px+ (* 1.5 h) 16 10)
     :width "auto"
     :display "block"
     :box-shadow "1px 1px 2px #ccc"
     :max-height (u/px 300)
     :overflow-y "auto"
     :border "1px solid #ddd"}
    [:.option {:cursor "pointer" :padding {:left (u/px h) :right (u/px h)}
               :line-height (u/px h3)}

     [:&.active {:background-color "#f1f1f1"}]
     [:&:hover {:background-color "#f1f1f1"}]]]])

(defn- debounced-callback [state suggest-fn in ms]
  (go-loop [last-val nil]
    (let [val (if (nil? last-val) (<! in) last-val)
          timer (timeout ms)
          [new-val ch] (alts! [in timer])]
      (condp = ch
        timer (do (swap! state assoc
                         :suggestions (<! (suggest-fn val))
                         :loading false)
                  (recur nil))
        in (when new-val (recur new-val))))))

(defn- lookup-suggestions [ch query]
  (when-not (empty? query)
    (go (>! ch query))))

(defn select-xhr-input [{:keys [suggest-fn on-change]}]
  (let [state (r/atom {:current-text nil
                       :focused false
                       :suggestions []})
        close #(swap! state assoc :focused false :current-text nil)
        on-change-ch (chan)
        handle-input-change
        (fn [event]
          (let [text (.-value (.-target event))]
            (swap! state assoc :current-text text)
            (if (empty? text)
              (do
                (swap! state assoc :current-text nil :suggestions [])
                (on-change ""))
              (do
                (swap! state assoc :loading true)
                (lookup-suggestions on-change-ch text)))))]

    (debounced-callback state suggest-fn on-change-ch 100)
    (.addEventListener (aget js/document "body" ) "click" #(when (:current-text @state) (close)))
    (r/create-class
     {:reagent-render
      (fn [{:keys [value on-change value-fn label-fn match-fn suggest-fn placeholder icon]}]
        (let [label-fn (or label-fn pr-str)
              value-fn (or value-fn identity)
              match-fn (or match-fn
                           (fn [v] (->> (:suggestions @state)
                                        (filter #(= (value-fn %) v))
                                        first label-fn)))]
          [:div.re-select-xhr
           #_[:i.search-icon.material-icons "search"]
           [:input.query
            {:type "text"
             :placeholder placeholder
             :on-change handle-input-change
             :on-focus (fn [e] (swap! state assoc
                                      :current-text nil :focused true :suggestions []))
             :value (or (:current-text @state)
                        (match-fn value))}]
           (when (:loading @state)
             [:div.controls "loading..."])
           (when (and (:focused @state) (not-empty (:suggestions @state)))
             [:div.suggestions
              (for [i (:suggestions @state)] ^{:key (pr-str (value-fn i))}
                [:div.option {:on-click #(do (when on-change (on-change (value-fn i)))
                                             (close))}
                 (label-fn i)])])]))})))
