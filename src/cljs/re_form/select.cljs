(ns re-form.select
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [cljsjs.react]
   [reagent.core :as reagent]
   [garden.core :as garden]
   [garden.color :as c]
   [garden.units :as u]
   [re-frame.core :as rf]
   [clojure.string :as str]))



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
    :padding "5px 10px" 
    :border "1px solid #ddd"}
   [:.clear {:padding (u/px 5)
             :cursor "pointer"
             :color :red}]
   [:div.options
    {:position "absolute"
     :background-color "white"
     :left 0
     :top (u/px 40)
     :width "auto"
     :display "inline-block"
     :box-shadow "1px 1px 2px #ddd"
     :border "1px solid #ddd"}
    [:.re-search {:display "inline-block"
                  :margin "5px"}]
    [:div.option {:cursor "pointer"
                  :padding (u/px 10)}
     [:&:hover {:background-color "#f1f1f1"}]]]])

(defn re-select [{pth :path options-path :options-path}]
  (let [sub (rf/subscribe [:re-form/data pth])
        items (rf/subscribe [:re-form/data options-path])
        activate (fn [ev] (rf/dispatch [:re-form.select/activate pth (not (:active @sub))]))
        set-value (fn [v]
                    (rf/dispatch [:re-form/on-change pth v])
                    (activate false))]
    (fn [props]
      [:div.re-select
       (if-let [v (:value @sub)]
         [:span.value
          [:span.value {:on-click activate} (pr-str v) ]
          [:span.clear {:on-click #(set-value nil)} "x"]]
         [:span.choose-value {:on-click activate} (or (:placeholder props) "Select...")])
       (when (:active @sub)
         [:div.options
          [:input.re-search {:type "text"}]
          (for [i @items]
            [:div.option
             {:key (pr-str i)
              :on-click #(set-value i)}
             (pr-str i)])])])))

(def re-radio-group-style
  [:div.re-radio-group
   [:div.option {:cursor "pointer"
                 :display "block"
                 :padding (u/px 10)}
    [:.radio {:border "1px solid #ddd"
              :border-radius "50%"
              :display "inline-block"
              :vertical-align "middle"
              :margin-right (u/px 5)
              :width (u/px 20)
              :height (u/px 20)}]
    [:&.active {}
     [:.radio {:background-color "#888"}]]
    [:&:hover {}]]])

(defn re-radio-group [{pth :path options-path :options-path}]
  (let [sub (rf/subscribe [:re-form/data pth])
        items (rf/subscribe [:re-form/data options-path])
        set-value (fn [v] (rf/dispatch [:re-form/on-change pth v]))]
    (fn [props]
      [:div.re-radio-group
       (doall
        (for [i @items]
          [:div.option
           {:key (pr-str i)
            :class (when (= i (:value @sub)) "active")
            :on-click #(set-value i)}
           [:span.radio]
           [:span.value (pr-str i)]]))
       [:div.option.clear {:on-click #(set-value nil)} "Clear x"]])))

 

(def re-radio-buttons-style
  [:div.re-radio-buttons
   {:display "inline-block"}
   [:div.option {:cursor "pointer"
                 :border "1px solid #ddd"
                 :margin-right "-1px"
                 :display "inline-block"
                 :padding (u/px 10)}
    [:&.active {:background-color "#007bff"
                :color "white"}]]])

(defn re-radio-buttons [{pth :path options-path :options-path}]
  (let [sub (rf/subscribe [:re-form/data pth])
        items (rf/subscribe [:re-form/data options-path])
        set-value (fn [v] (rf/dispatch [:re-form/on-change pth v]))]
    (fn [props]
      [:div.re-radio-buttons
       (doall
        (for [i @items]
          [:div.option
           {:key (pr-str i)
            :class (when (= i (:value @sub)) "active")
            :on-click #(set-value i)}
           [:span.value (pr-str i)]]))])))

 
