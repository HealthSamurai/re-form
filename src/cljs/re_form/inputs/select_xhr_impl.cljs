(ns re-form.inputs.select-xhr-impl
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<! chan timeout]]
            [garden.units :as u]
            [re-form.inputs.common :as cmn]))

;; TODO it should be one style for selects
(defn select-xhr-style
  [{:keys [h h2 h3 radius selection-bg-color hover-bg-color border gray-color]}]
  [:.re-select-xhr
   {:position :relative
    :padding 0
    :width "auto"
    :display "inline-block"}

   [:input.query
    {:line-height (u/px h2)
     :position :relative
     :box-shadow "none"
     :padding [[(u/px-div (- h3 h2) 2) (u/px 12)]]
     :border border
     :border-radius (u/px radius)
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
     :top (u/px- h3 1)
     :width "auto"
     :display "block"
     :box-shadow "1px 1px 2px #ccc"
     :max-height (u/px 300)
     :overflow-y "auto"
     :border border}
    [:.option {:cursor "pointer" :padding {:left (u/px h) :right (u/px h)}
               :line-height (u/px h3)}

     [:&.active {:background-color hover-bg-color}]
     [:&:hover {:background-color hover-bg-color}]]]])

(defn- debounced-callback [state suggest-fn in ms]
  (go-loop [last-val nil]
    (let [val (if (nil? last-val) (<! in) last-val)
          timer (timeout ms)
          [new-val ch] (alts! [in timer])]
      (condp = ch
        timer (do (swap! state assoc
                         :suggestions (<! (suggest-fn val))
                         :loading false
                         :selected nil)
                  (recur nil))
        in (when new-val (recur new-val))))))

(defn- lookup-suggestions [ch query]
  (when-not (empty? query)
    (go (>! ch query))))

(defn select-xhr-input [{:keys [suggest-fn on-change]}]
  (let [state (r/atom {:current-text nil
                       :focused false
                       :suggestions []})
        close #(swap! state assoc
                      :focused false
                      :current-text nil
                      :selected nil)
        on-change-ch (chan)
        handle-input-change
        (fn [text]
          (swap! state assoc
                 :current-text text
                 :selected nil)
          (if (empty? text)
            (do
              (swap! state assoc :current-text nil :suggestions [])
              (on-change ""))
            (do
              (swap! state assoc :loading true)
              (lookup-suggestions on-change-ch text))))
        arrow-handler (fn [e]
                        (if-let [s (:selected @state)]
                          (let [upd (fn [x direction]
                                      (.. s -classList (remove "active"))
                                      (.. x -classList (add "active"))
                                      (swap! state assoc :selected x)
                                      (let [sugs (:suggestions-container @state)]
                                        (cmn/scroll sugs x direction)))]
                            (case (.-keyCode e)
                              38 (when-let [prev-sibl (.-previousSibling s)]
                                   (upd prev-sibl :up))
                              40 (when-let [next-sibl (.-nextSibling s)]
                                   (upd next-sibl :down))
                              13 (do (.click (:selected @state))
                                     (swap! state dissoc :selected)
                                     (.blur (:node @state)))
                              (swap! state dissoc :selected)))
                          (when-let [first-opt (cmn/f-child (:root-node @state) "option")]
                            (.. first-opt -classList (add "active"))
                            (swap! state assoc :selected first-opt
                                   :suggestions-container
                                   (cmn/f-child (:root-node @state) "suggestions")))))]

    (debounced-callback state suggest-fn on-change-ch 100)
    (.addEventListener (.-body js/document) "click" #(when (:current-text @state) (close)))
    (r/create-class
     {
      :component-did-mount
      (fn [this]
        (let [root-node (r/dom-node this)
              input (cmn/f-child root-node "query")]
          (swap! state assoc :node input)
          (swap! state assoc :root-node root-node)
          (.addEventListener input "keydown" arrow-handler)))

      :component-will-unmount
      (fn [this]
        (let [input (cmn/f-child (r/dom-node this) "query")]
          (.removeEventListener input "keydown" arrow-handler)))

      :reagent-render
      (fn [{:keys [value on-change value-fn label-fn match-fn suggest-fn placeholder icon]}]
        (let [label-fn (or label-fn pr-str)
              value-fn (or value-fn identity)
              match-fn (or match-fn
                           (fn [v] (->> (:suggestions @state)
                                        (filter #(= (value-fn %) v))
                                        first label-fn)))]
          [:div.re-select-xhr
           [:input.query
            {:type "text"
             :placeholder placeholder
             :on-change #(handle-input-change (.. % -target -value))
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
