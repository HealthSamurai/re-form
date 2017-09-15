(ns re-form.inputs.checkbox-group-impl
  (:require [clojure.set :as s]
            [reagent.core :as r]
            [garden.units :as u]))

(def checkbox-group-style
  [:.re-checkbox-group
   {:display "inline-block"}
   [:.re-checkbox {:cursor "pointer"
                 :opacity 0.7
                 :border-radius (u/px 15)
                 :padding {:top (u/px 5)
                           :left (u/px 10)
                           :right (u/px 10)
                           :bottom (u/px 5)}}
    [:.radio {:border "1px solid #ddd"
              :background-color "white"
              :display "inline-block"
              :vertical-align "middle"
              :margin-right (u/px 5)
              :width (u/px 20)
              :height (u/px 20)}]
    [:&.active {}
     [:.radio {:background-color "#007bff"}]]
    [:&:hover {:opacity 1 :background-color "#f1f1f1"}]]])

(defn checkbox-group-input [_]
  (fn [{:keys [value on-change value-fn label-fn items] :as props}]
    (let [label-fn (or label-fn pr-str)
          value-fn (or value-fn identity)
          setgen #(-> % vector set)
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
           [:span.radio]
           [:span.value (label-fn i)]]))])))
