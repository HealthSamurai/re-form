(ns re-form.inputs.select-impl
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-form.inputs.tags-input-impl :as ti]
            [re-form.inputs.common :as cmn]
            [clojure.string :as str]
            [goog.functions :refer [debounce]]
            [garden.units :as u]
            [clojure.set :as s]))

(defn select-style
  [{:keys [radius w h h2 h3 selection-bg-color hover-bg-color border error-border]}]
  [:.re-select-container
   {:position :relative}
   [:.re-re-select
    {:display "inline-block"
     :background-color :white
     :border-radius (u/px 2)
     :line-height (u/px h2)
     :border border
     :color "black"
     :text-decoration "none"}
    [:.tag
     {:border border
      :color :inherit
      :text-decoration :inherit
      :position :relative
      :display :inline-flex
      :align-items :center
      :padding {:left (u/px-div w 2)
                :right (u/px 2)}
      :margin-right (u/px w)}
     [:.cross
      {:cursor :pointer
       :font-size (u/px h)}]]
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
     :position :relative
     :z-index 1000
     :left (u/px 0)
     :bottom (u/px- 1) #_(u/px+ 10 (+ 16 (* h 1.5)))
     :padding [[0 (u/px w)]]
     :border-radius (u/px radius)
     :line-height (u/px* h3)}
    [:&.re-invisible
     {:display :none :position :absolute}]]
   [:.options
    {:position :relative
     :background-color "white"
     :z-index 1000
     :left (u/px 0)
     :bottom (u/px- 1) #_(u/px+ 10 (* 2 (+ 16 (* h 1.5))))
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

(defn- tag [label on-delete]
  [:span.tag
   [:span label]
   [:i.tag-cross.material-icons
    {:on-click on-delete} "close"]])

(defn select [{:keys [on-search debounce-interval on-blur on-change
                      label-fn search-by-enter multiselect options] :as props}]
  (let [state (r/atom {:active false})
        nodes (r/atom {})
        inner-value (r/atom nil)
        doc-click-listener (fn [e]
                             (let [*state @state
                                   *nodes @nodes]
                               (when (or (and (not (cmn/has-ancestor
                                                    (.-target e)
                                                    (:root-node *nodes)))
                                              (:active *state))
                                         ((set (cmn/f-childn (:root-node *nodes)
                                                             "cross"
                                                             "tag-cross"))
                                          (.-target e)))
                                 (on-blur e)
                                 (swap! state assoc :active false))))
        setgen #(-> % vector set)
        search-fn (cond
                    search-by-enter (fn [_] (swap! state dissoc :selected))
                    debounce-interval (debounce on-search debounce-interval)
                    :else on-search)
        arrow-handler (fn [e]
                        (if-let [s (:selected @state)]
                          (let [upd (fn [x direction]
                                      (.. s -classList (remove "active"))
                                      (.. x -classList (add "active"))
                                      (swap! state assoc :selected x)
                                      (let [sugs (:suggestions-container @state)]
                                        (cmn/scroll sugs x direction)))]
                            (case (.-keyCode e)
                              38 (do (when-let [prev-sibl (.-previousSibling s)]
                                       (upd prev-sibl :up))
                                     (.preventDefault e))
                              40 (when-let [next-sibl (.-nextSibling s)]
                                   (upd next-sibl :down))
                              13 (do (.click (:selected @state))
                                     (swap! state dissoc :selected))
                              (swap! state dissoc :selected)))
                          (cond
                            (and search-by-enter on-search (= 13 (.-keyCode e)))
                            (on-search (.. e -target -value))
                            :else (when-let [first-opt (cmn/f-child (:root-node @nodes) "option")]
                                    (.. first-opt -classList (add "active"))
                                    (swap! state assoc :selected first-opt
                                           :suggestions-container
                                           (cmn/f-child (:root-node @nodes) "options"))))))
        reset-input (fn [e]
                      (on-change nil)
                      (when search-fn
                        (set! (.-value (:input @nodes)) "")))
        open-popup (fn [_]
                     (when search-fn
                       (js/setTimeout #(.focus (:input @nodes)) 0)
                       (search-fn (.-value (:input @nodes))))
                     (swap! state assoc :active true :selected nil))

        on-select (fn [value v]
                    (swap! state assoc :active false)
                    (if-let [i (:input @nodes)]
                      (set! (.-value i) ""))
                    (if multiselect
                      (on-change (conj (set value) v))
                      (on-change v))

                    (when-let [focus-node (:focus-node @nodes)]
                      (js/setTimeout #(when focus-node (.focus focus-node)) 0)))


        my-label-fn (or label-fn identity)

        outer-key-handler
        (fn [ev]
          (when-not (#{46 8 9 16} (.-keyCode ev))
            (open-popup ev)
            (.preventDefault ev)))]

    (r/create-class
     {:component-did-mount
      (fn [this]
        (swap! nodes assoc :root-node (r/dom-node this))
        (.addEventListener (:input @nodes) "keydown" arrow-handler)
        (.addEventListener (:focus-node @nodes) "keydown" outer-key-handler)
        (.addEventListener js/document "click" doc-click-listener)
        (when-let [focus-node (and (:auto-focus props) (:focus-node @nodes))]
          (js/setTimeout #(.focus focus-node) 0)))

      :component-will-unmount
      (fn [this]
        (.removeEventListener (:input @nodes) "keydown" arrow-handler)
        (.removeEventListener (:focus-node @nodes) "keydown" outer-key-handler)
        (.removeEventListener js/document "click" doc-click-listener))

      :reagent-render
      (fn [{:keys [value on-change value-fn options errors] :as props}]
        (reset! inner-value (set value))
        [:div.re-select-container
         [:div.re-re-select
          {:class (when-not (empty? errors) :error)
           :tab-index 0 #_(:tab-index props)
           :ref (fn [focus-node] (swap! nodes assoc :focus-node focus-node))
           :on-click (fn [ev] (open-popup ev))}
          [:div.flex
           [:span.triangle "▾"]
           (if (and value (not (empty? value)))
             [:span.value
              (if multiselect
                (for [v value] ^{:key v}
                  [ti/tag (my-label-fn v) (r/cursor nodes [:input])
                   #(on-change (s/difference @inner-value (setgen v)))])
                (my-label-fn value))]
             [:span.choose-value
              (or (:placeholder props) "Select...")])
           (when (and value (not multiselect))
             [:i.material-icons.cross {:on-click reset-input} "close"])]]
         (when search-fn
           [:input.re-search-search
            {:tab-index 0
             :ref (fn [this] (swap! nodes assoc :input this))
             :class (when-not (:active @state) :re-invisible)
             :on-change (fn [ev] (search-fn (.. ev -target -value)))}])
         (when (:active @state)
           [:div.options {:class (when-not search-fn :no-input)}
            (when search-by-enter [:div.no-results "Press enter for lookup"])
            (if (= :loading options)
              [:div.no-results "Loading..."]
              [options-list options my-label-fn
               (fn [ev]
                 (on-select value ev))])])])})))
