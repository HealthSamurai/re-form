(ns re-form.inputs.checkbox-group-impl
  (:require [clojure.set :as s]
            [reagent.core :as r]
            [garden.units :as u]))


(def h 16)
(def h2 24)
(def h3 38)
(def selection-bg-color "#007bff")
(def hover-bg-color "#f1f1f1")
(def border "1px solid #ddd")

(def checkbox-group-style
  [:.re-checkbox-group
   {:display "inline-block"}
   [:.re-checkbox {:cursor "pointer"
                   :line-height (u/px h3)}
    [:.radio {:border "1px solid #ddd"
              :background-color "white"
              :display "inline-block"
              :vertical-align "middle"
              :margin-right (u/px-div h 2)
              :margin-top (u/px -3)
              :padding-top (u/px 6)
              :width (u/px h2)
              :height (u/px h2)}]
    [:&.active {}
     [:.radio {:background-color selection-bg-color}]]
    [:&:hover]]])

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
