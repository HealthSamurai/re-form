(ns re-form.inputs.checkbox-group-impl
  (:require [clojure.set :as s]
            [reagent.core :as r]
            [garden.units :as u]))

(defn checkbox-group-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.re-checkbox-group
   {:display :inline-block}
   [:.re-checkbox {:cursor :pointer
                   :line-height (u/px h3)}
    [:.radio {:border border
              :background-color :white
              :border-radius (u/px 2)
              :display :inline-flex
              :align-items :center
              :justify-content :center
              :vertical-align :middle
              :margin-right (u/px 12)
              :margin-top (u/px -3)
              :width (u/px h2)
              :height (u/px h2)}
     [:.inner-radio
      {:width (u/px-div h2 1.5)
       :height (u/px-div h2 1.5)
       :display :none
       :background-color selection-bg-color}]]
    [:&.active
     [:.radio [:.inner-radio {:display :inline-block}]]]
    [:&:hover]]])

(defn checkbox-group-input [_]
  (fn [{:keys [value on-change value-fn label-fn items] :as props}]
    (let [label-fn (or label-fn pr-str)
          value-fn (or value-fn identity)
          setgen #(-> % value-fn vector set)
          cont #(s/subset? % value)
          change-fn (fn [set-item]
                      (if (cont set-item)
                        (s/difference value set-item)
                        (s/union value set-item)))]
      [:div.re-checkbox-group
       (doall
        (for [i items] ^{:key (pr-str i)}
          [:div.re-checkbox
           {:class (when (cont (setgen i)) "active")
            :on-click #(on-change (-> i setgen change-fn))}
           [:span.radio [:div.inner-radio]]
           [:span.value (label-fn i)]]))])))
