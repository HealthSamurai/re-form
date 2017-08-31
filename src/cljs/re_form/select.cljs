(ns re-form.select
  (:require
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
    :background-color "white"
    :margin-left "10px"
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
   (.log js/console "re-search/search" txt manifest)
   (let [opts (get-in db (:options-path manifest))
         lbl-fn (:label-fn manifest)
         res (->> (or opts [])
                  (filter (fn [x] (str/includes? (str/lower-case ((or lbl-fn pr-str) x))
                                                 (str/lower-case txt))))
                  (take 50))]
     (assoc-in db (conj (:path manifest) :options) res))))

(defn re-select [{pth :path options-path :options-path
                  value-fn :value-fn
                  lbl-fn :label-fn :as props}]
  (let [label-fn (or lbl-fn pr-str)
        value-fn (or value-fn identity)
        sub (rf/subscribe [:re-form/data pth])
        items (rf/subscribe [:re-form/data (conj pth :options)])
        activate (fn [ev] (rf/dispatch [:re-form.select/activate pth (not (:active @sub))]))
        on-search (fn [ev]
                    (let [txt (.. ev -target -value)]
                      (rf/dispatch [:re-select/search props txt])))
        set-value (fn [v]
                    (rf/dispatch [:re-form/on-change pth (value-fn v)])
                    (activate false))]
    (fn [props]
      [:div.re-select
       (if-let [v (:value @sub)]
         [:span.value
          [:span.value {:on-click activate } (label-fn v) ]
          [:span.clear {:on-click #(set-value nil)} "x"]]
         [:span.choose-value {:on-click activate} (or (:placeholder props) "Select...")])
       (when (:active @sub)
         [:div.options
          [:input.re-search {:type "text"
                             :on-change  on-search}]
          (for [i @items]
            [:div.option
             {:key (pr-str i)
              :on-click #(set-value i)}
             (label-fn i)])])])))

(def re-radio-group-style
  [:div.re-radio-group
   {:display "inline-block"}
   [:div.option {:cursor "pointer"
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
    [:&:hover {}]]])

(defn re-radio-group [{pth :path options-path :options-path lbl-fn :label-fn}]
  (let [label-fn (or lbl-fn pr-str)
        sub (rf/subscribe [:re-form/data pth])
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
                 :border-right "1px solid #ddd"
                 :padding {:top (u/px 5)
                           :left (u/px 10)
                           :right (u/px 10)
                           :bottom (u/px 5)}
                 :display "inline-block"}

    [:&:first-child {:border-radius "15px 0px 0px 15px"}]
    [:&:last-child {:border-radius "0px 15px 15px 0px"
                    :border-right "none"}]
    [:&.active {:background-color "#007bff"
                :color "white"}]]])

(defn re-radio-buttons [{pth :path
                         options-path :options-path
                         value-fn :value-fn
                         lbl-fn :label-fn}]
  (let [label-fn (or lbl-fn pr-str)
        value-fn (or value-fn identity)
        sub (rf/subscribe [:re-form/data pth])
        items (rf/subscribe [:re-form/data options-path])
        set-value (fn [v] (rf/dispatch [:re-form/on-change pth (value-fn v)]))]
    (fn [props]
      [:div.re-radio-buttons
       (doall
        (for [i @items]
          [:div.option
           {:key (pr-str i)
            :class (when (= (value-fn i) (:value @sub)) "active")
            :on-click #(set-value i)}
           [:span.value (label-fn i)]]))])))
 
