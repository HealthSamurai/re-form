(ns re-form.inputs.button-select-input-impl
  (:require [reagent.core :as r]
            [garden.units :as u]
            [re-form.inputs.common :as cmn]))

(defn button-select-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:div.re-radio-buttons
   {:display "inline-block"
    :outline :none
    :border border
    :border-radius (u/px 2)}
   [:div.option {:cursor "pointer"
                 :transition "background-color 200ms, color 100ms"
                 :background-color "white"
                 :border-right border
                 :line-height (u/px- h3 2)
                 :padding-left (u/px h)
                 :padding-right (u/px h)
                 :display "inline-block"}

    [:&:hover {:background-color hover-bg-color}]
    [:&:first-child {:border-radius "2px 0px 0px 2px"}]
    [:&:last-child {:border-radius "0px 2px 2px 0px"
                    :border-right "none"}]
    [:&.active {:background-color selection-bg-color
                :color "white"}]]])

(defn button-select-input [_]
  (let [arrow-handler (fn [e]
                        (when-let [active (cmn/f-child (.-target e) "active")]
                          (case (.-keyCode e)
                            37 (when-let [prev-sibl (.-previousSibling active)]
                                 (.click prev-sibl))
                            39 (when-let [next-sibl (.-nextSibling active)]
                                 (.click next-sibl))
                            nil)))]
    (r/create-class
     {
      :component-did-mount
      (fn [this] (.addEventListener (r/dom-node this) "keydown" arrow-handler))

      :component-will-unmount
      (fn [this] (.removeEventListener (r/dom-node this) "keydown" arrow-handler))

      :reagent-render
      (fn [{:keys [value on-change label-fn value-fn items]}]
        (let [value-fn (or value-fn identity)]
          [:div.re-radio-buttons {:tab-index 0}
           (doall
            (for [i items]
              [:div.option
               {:key (pr-str i)
                :class (when (= (value-fn i) value) "active")
                :on-click (fn [_] (on-change (value-fn i)))}
               [:span.value (label-fn i)]]))]))})))
