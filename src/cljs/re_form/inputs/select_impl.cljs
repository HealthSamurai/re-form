(ns re-form.inputs.select-impl
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-form.inputs.common :as cmn]
            [goog.functions :refer [debounce]]
            [garden.units :as u]))

(defn select-style
  [{:keys [radius w h h2 h3 selection-bg-color hover-bg-color border error-border]}]
  [:.re-select-container
   {:position :relative}
   [:.re-re-select
    {:display "inline-block"
     :background-color :white
     :border-radius (u/px 2)
     :padding [[(u/px-div h 2) (u/px 12)]]
     :line-height (u/px h2)
     :border border}
    [:.flex {:display :inline-flex
             :width (u/percent 100)}
     [:i.cross {:margin-left :auto
                :color "#e1e1e1"
                :font-size (u/px h)}]]
    [:&.error
     {:border error-border}]
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
     [:&:hover {:opacity 1 :background-color "#f1f1f1"}]]]
   [:.re-search-search
    {:border border
     :width "100%"
     :position :absolute
     :z-index 1000
     :left (u/px 0)
     :top (u/px+ 10 (+ 16 (* h 1.5)))
     :padding [[0 (u/px w)]]
     :border-radius (u/px radius)
     :line-height (u/px* h3)}
    [:&.re-invisible
     {:display :none :position :absolute}]]
   [:.options
    {:position "absolute"
     :background-color "white"
     :z-index 1000
     :left (u/px 0)
     :top (u/px+ 10 (* 2 (+ 16 (* h 1.5))))
     :width "100%"
     :display "inline-block"
     :box-shadow "1px 1px 2px #ccc"
     :max-height (u/px 500)
     :overflow-y :auto
     :border "1px solid #ddd"}
    [:&.no-input
     {:top (u/px+ 10 (+ 16 (* h 1.5)))}]
    [:.no-results
     {:text-align :center
      :padding (u/px 10)
      :color :gray}]
    [:.option {:cursor "pointer"
               :display "block"
               :padding-left (u/px h)
               :padding-right (u/px h)
               :line-height (u/px h3)}
     [:&.active {:background-color hover-bg-color}]
     [:&:hover {:background-color hover-bg-color}]]]])

(defn options-list [options label-fn on-change]
  (let [opts (if (instance? reagent.ratom/Reaction options) @options options)]
    [:div
     (if (empty? opts)
       [:div.no-results "No results"]
       (for [i opts]
         [:div.option
          {:key (pr-str i)
           :on-click (fn [_] (on-change i))}
          (label-fn i)]))]))

(defn select [{:keys [on-search debounce-interval on-blur on-change label-fn]}]
  (let [state (r/atom {:active false})
        doc-click-listener (fn [e]
                             (when (or (and (not (cmn/has-ancestor
                                                  (.-target e)
                                                  (:root-node @state)))
                                            (:active @state))
                                       (=
                                        (.-target e)
                                        (cmn/f-child (:root-node @state)
                                                     "cross")))
                               (on-blur e)
                               (swap! state assoc :active false)))
        search-fn (if debounce-interval
                    (debounce on-search debounce-interval)
                    on-search)
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
                                     #_(.blur (:node @state)))
                              (swap! state dissoc :selected)))
                          (when-let [first-opt (cmn/f-child (:root-node @state) "option")]
                            (.. first-opt -classList (add "active"))
                            (swap! state assoc :selected first-opt
                                   :suggestions-container
                                   (cmn/f-child (:root-node @state) "options")))))
        reset-input (fn [e]
                      (on-change nil)
                      (when search-fn
                        (set! (.-value (:input-node @state)) "")))
        label-fn (or label-fn identity)]

    (r/create-class
     {
      :component-did-mount
      (fn [this]
        (let [root (r/dom-node this)]
          (swap! state assoc :root-node root)
          (when-let [input (cmn/f-child root "re-search-search")]
            (swap! state assoc :input-node input)
            (.addEventListener input "keydown" arrow-handler)))
        (.addEventListener js/document "click" doc-click-listener))

      :component-will-unmount
      (fn [this]
        (when-let [input (:input-node @state)]
          (.removeEventListener input "keydown" arrow-handler))
        (.removeEventListener js/document "click" doc-click-listener))

      :reagent-render
      (fn [{:keys [value on-change value-fn options errors] :as props}]
        [:div.re-select-container
         [:div.re-re-select
          {:class (when-not (empty? errors) :error)}
          [:div.flex
           {:on-click (fn [_] (do
                                (when search-fn
                                  (js/setTimeout #(.focus (:input-node @state)) 10)
                                  (search-fn (.-value (:input-node @state))))
                                (swap! state assoc :active true
                                       :selected nil)))}
           [:span.triangle "▾"]
           (if value
             [:span.value (label-fn value)]
             [:span.choose-value
              (or (:placeholder props) "Select...")])
           (when value
             [:i.material-icons.cross {:on-click reset-input} "close"])]]
         (when search-fn
           [:input.re-search-search
            {:tab-index 0
             :class (when-not (:active @state) :re-invisible)
             :on-change #(search-fn (.. % -target -value))}])
         (when (:active @state)
           [:div.options {:class (when-not search-fn :no-input)}
            [options-list options label-fn (comp #(swap! state assoc :active false)
                                                 on-change)]])])})))
