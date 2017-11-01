(ns re-form.inputs.radio-input-impl
  (:require [reagent.core :as r]
            [garden.units :as u]
            [re-form.inputs.common :as cmn]))

(defn radio-input-styles
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:div.re-radio-group
   {:display :inline-block
    :outline :none}
   [:div.option {:cursor :pointer
                 :border-radius (u/px 2)
                 :line-height (u/px h3)
                 :padding {:right (u/px h)}}
    [:.radio {:border border
              :border-radius (u/percent 50)
              :background-color :white
              :display :inline-flex
              :vertical-align :middle
              :align-items :center
              :justify-content :center
              :margin-right (u/px 12)
              :margin-top (u/px -3)
              :width (u/px h2)
              :height (u/px h2)}
     [:.inner-radio
      {:width (u/px-div h2 1.5)
       :height (u/px-div h2 1.5)
       :border-radius (u/percent 50)
       :display :none
       :background-color selection-bg-color}]]
    [:&.active {}
     [:.radio [:.inner-radio {:display :inline-block}]]]
    [:&:hover {:opacity 1 :background-color hover-bg-color}]]])

(defn radio-input [{:keys [value-fn label-fn on-change] :as props}]
  (let [label-fn (or label-fn pr-str)
        value-fn (or value-fn identity)
        arrow-handler (fn [e]
                        (when-let [active (cmn/f-child (.-target e) "active")]
                          (case (.-keyCode e)
                            38 (when-let [prev-sibl (.-previousSibling active)]
                                 (.click prev-sibl))
                            40 (when-let [next-sibl (.-nextSibling active)]
                                 (.click next-sibl))
                            nil)))]

    (r/create-class
     {
      :component-did-mount
      (fn [this] (.addEventListener (r/dom-node this) "keydown" arrow-handler))

      :component-will-unmount
      (fn [this] (.removeEventListener (r/dom-node this) "keydown" arrow-handler))

      :reagent-render
      (fn [{:keys [value on-change items]}]
        [:div.re-radio-group {:tab-index 0}
         (for [i items] ^{:key (pr-str i)}
           [:div.option
            {:class (when (= (value-fn i) value) "active")
             :on-click #(on-change (value-fn i))}
            [:span.radio [:div.inner-radio]]
            [:span.value (label-fn i)]])])})))
