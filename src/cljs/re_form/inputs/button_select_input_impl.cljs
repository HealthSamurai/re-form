(ns re-form.inputs.button-select-input-impl
  (:require [reagent.core :as r]))

(def button-select-style
  [:div.re-select
   {:position "relative"
    :display "inline-block"
    :background-color "white"
    :margin-left "10px"
    :min-width "10em"
    :padding {:left (u/px 10)
              :right (u/px 40)
              :top (u/px 5)
              :bottom (u/px 5)}
    :border "1px solid #ddd"}
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
    [:&:hover {:opacity 1 :background-color "#f1f1f1"}]]
   [:div.options
    {:position "absolute"
     :background-color "white"
     :min-width "10em"
     :z-index 1000
     :left 0
     :top (u/px 40)
     :width "auto"
     :display "inline-block"
     :box-shadow "1px 1px 2px #ccc"
     :border "1px solid #ddd"}
    [:.re-search {:display "inline-block"
                  :width "90%"
                  :margin "5px"}]
    [:div.option {:cursor "pointer"
                  :padding (u/px 10)}
     [:&:hover {:background-color "#f1f1f1"}]]]])

(rf/reg-event-db
 :re-select/search
 (fn [db [_ manifest txt]]
   (let [opts (get-in db (:options-path manifest))
         lbl-fn (:label-fn manifest)
         res (->> (or opts [])
                  (filter (fn [x] (str/includes? (str/lower-case ((or lbl-fn pr-str) x))
                                                 (str/lower-case txt))))
                  (take 50))]
     (assoc-in db (conj (:path manifest) :options) res))))

(defn button-select-input [{pth :path options-path :options-path
                  value-fn :value-fn
                  lbl-fn :label-fn :as opts}]
  (let [label-fn (or lbl-fn pr-str)
        value-fn (or value-fn identity)
        v (rf/subscribe [:re-form/value opts])
        state (rf/subscribe [:re-form/state opts])
        items (rf/subscribe [:re-form/data (conj pth :options)])
        activate (fn [ev] (rf/dispatch [:re-form/state opts {:active (not (:active @state))}]))
        on-search (fn [ev]
                    (let [txt (.. ev -target -value)]
                      (rf/dispatch [:re-select/search opts txt])))
        set-value (fn [v]
                    (rf/dispatch [:re-form/update opts (value-fn v)])
                    (activate false))]
    (fn [props]
      [:div.re-select {:on-click activate }
       (if-let [v @v]
         [:span.value
          [:span.value  (or (label-fn v) (value-fn v) (str v))]
          [:span.clear {:on-click #(set-value nil)} "x"]]
         [:span.choose-value
          (or (:placeholder opts) "Select...")])
       (when (:active @state)
         [:div.options
          [:input.re-search {:type "text" :on-change  on-search}]
          (for [i @items]
            [:div.option
             {:key (pr-str i)
              :on-click #(set-value i)}
             (label-fn i)])])])))

