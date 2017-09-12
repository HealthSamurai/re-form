(ns re-form.select
  (:require
   [reagent.core :as reagent]
   [garden.core :as garden]
   [garden.color :as c]
   [garden.units :as u]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [re-form.shared :as shared]))



(rf/reg-event-db
 :re-form.select/activate
 (fn [db [_ pth v]]
   (assoc-in db (conj pth :active) v)))

(rf/reg-event-db
 :re-form.select/set-value
 (fn [db [_ pth v]]
   (assoc-in db (conj pth :value) v)))

(def re-select-style
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

(defn re-select [{pth :path options-path :options-path
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

(def re-radio-group-style
  [:div.re-radio-group
   {:display "inline-block"}
   [:div.option {:cursor "pointer"
                 :opacity 0.7
                 :border-radius (u/px 15)
                 :padding {:top (u/px 5)
                           :left (u/px 10)
                           :right (u/px 10)
                           :bottom (u/px 5)}}
    [:.radio {:border "1px solid #ddd"
              :border-radius "50%"
              :background-color "white"
              :display "inline-block"
              :vertical-align "middle"
              :margin-right (u/px 5)
              :width (u/px 20)
              :height (u/px 20)}]
    [:&.active {}
     [:.radio {:background-color "#007bff"}]]
    [:&:hover {:opacity 1 :background-color "#f1f1f1"}]]])

(defn re-radio-group [{:keys [pth options-path label-fn value-fn] :as opts}]
  (let [label-fn (or label-fn pr-str)
        v (rf/subscribe [:re-form/value opts])
        items (rf/subscribe [:re-form/data options-path])
        set-value (fn [v] (rf/dispatch [:re-form/update opts v]))]
    (fn [props]
      [:div.re-radio-group
       (doall
        (for [i @items]
          [:div.option
           {:key (pr-str i)
            :class (when (= (value-fn i) @v) "active")
            :on-click #(set-value (value-fn i))}
           [:span.radio]
           [:span.value (label-fn i)]]))
       #_[:div.option.clear {:on-click #(set-value nil)} "Clear x"]])))


(def re-radio-buttons-style
  [:div.re-radio-buttons
   {:display "inline-block"
    :box-shadow "0px 0px 3px #ddd"
    :border-radius "15px";
    :margin-left "10px"}
   [:div.option {:cursor "pointer"
                 :transition "background-color 200ms, color 100ms" 
                 :background-color "white"
                 :opacity 0.7
                 :border-right "1px solid #ddd"
                 :padding {:top (u/px 5)
                           :left (u/px 10)
                           :right (u/px 10)
                           :bottom (u/px 5)}
                 :display "inline-block"}

    [:&:hover {:background-color "#f1f1f1"
               :opacity 1}]
    [:&:first-child {:border-radius "15px 0px 0px 15px"
                     :padding-left (u/px 15)}]
    [:&:last-child {:border-radius "0px 15px 15px 0px"
                    :padding-right (u/px 15)
                    :border-right "none"}]
    [:&.active {:background-color "#007bff"
                :opacity 1
                :color "white"}]]])

(defn re-radio-buttons [{options-path :options-path
                         value-fn :value-fn
                         lbl-fn :label-fn
                         :as opts}]
  (let [label-fn (or lbl-fn pr-str)
        value-fn (or value-fn identity)
        sub (rf/subscribe [:re-form/value opts])
        items (rf/subscribe [:re-form/data options-path])
        set-value (fn [v] (rf/dispatch [:re-form/update opts (value-fn v)]))]
    (fn [props]
      [:div.re-radio-buttons
       (doall
        (for [i @items]
          [:div.option
           {:key (pr-str i)
            :class (when (= (value-fn i) @sub) "active")
            :on-click #(set-value i)}
           [:span.value (label-fn i)]]))])))
 
