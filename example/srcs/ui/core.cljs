(ns ui.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [cljsjs.react]
   [reagent.core :as reagent]
   [garden.core :as garden]
   [garden.color :as c]
   [garden.units :as u]
   [re-frame.core :as rf]
   [route-map.core :as route-map]
   [ui.routing]
   [re-form.core :as form]
   [clojure.string :as str]))

(defn style [gcss]
  [:style (garden/css gcss)])

(defn input [{pth :path :as opts}]
  (let [sub (rf/subscribe [:re-form/data pth])
        on-change (fn [ev] (rf/dispatch [:re-form/change pth (.. ev -target -value)]))]
    (fn [props]
      [:input.form-control (merge (dissoc opts :path)
                                  {:type "text" :value @sub  :on-change on-change})])))


(defn check-box [{pth :path lbl :label}]
  (let [sub (rf/subscribe [:reform/data pth])
        on-change (fn [ev] (rf/dispatch [:re-form/change pth (not @sub)]))
        on-key-press  (fn [ev] (when (= 32 (.. ev -which)) (on-change ev)))]
    (fn [props]
      [:a.re-checkbox
       {:href "javascript:void()"
        :class (when @sub "re-checked")
        :on-key-press on-key-press
        :on-click on-change}
       [:span.re-box] (when lbl [:span.re-label lbl])])))

(defn switch-box [{pth :path lbl :label}]
  (let [sub (rf/subscribe [:reform/data pth])
        on-change (fn [ev] (rf/dispatch [:re-form/change pth (not @sub)]))
        on-key-press  (fn [ev] (when (= 32 (.. ev -which)) (on-change ev)))]
    (fn [props]
      [:a.re-switch
       {:href "javascript:void()"
        :class (when @sub "re-checked")
        :on-key-press on-key-press
        :on-click on-change}
       [:span.re-switch-span [:span.re-box]] (when lbl [:span.re-label lbl])])))


(defn errors [{pth :path f :validator} cmp]
  (let [err (rf/subscribe [:re-form/error pth f])]
    (fn [props cmp]
      [:div {:class (when @err "has-danger")}
       cmp [:br] "Errors:" (pr-str @err)])))

(rf/reg-sub-raw
 :re-form/error
 (fn [db [_ path f]]
   (let [cur (reagent/cursor db path)]
     (reaction (f @cur)))))

(rf/reg-sub-raw
 :reform/data
 (fn [db [_ path]]
   (let [cur (reagent/cursor db path)]
     (reaction @cur))))

(defn data []
  (let [d (rf/subscribe [:reform/data [:forms :user]])]
    (fn [] [:pre (pr-str @d)])))

(def form-style
  [:body
   [:.re-checkbox
    [:.re-box {:width (u/px 20)
               :display "inline-block"
               :height (u/px 20)
               :border "1px solid #ddd"}]
    [:&.re-checked
     [:.re-box {:background-color "#666"}]]]
   [:.re-switch
    [:.re-switch-span {:border "1px solid #ddd"
                       :width (u/px 40)
                       :position "relative"
                       :display "inline-block"
                       :height (u/px 15)}
     [:.re-box {:width (u/px 20)
                :height (u/px 20)
                :display "inline-block"
                :position "absolute"
                :left 0
                :top 0
                :border-radius "50%"
                :border "1px solid #ddd"}]]
    [:&.re-checked
     [:.re-box {:left (u/px 10)}]]]
   ])

(defn select [{[items path] :keys}]
  [:div.re-select
   (for [i items]
     [:div.re-option {:key i} i])])


;; {:validators [:at-least-one]
;;  :fields {:name {:path [b-p :name] :validators [:required [:longer-then 5]] :type :text}
;;           :groups {:collection true :type :object :fields {}}}}

;; [:change-form [:groups 0 :name] "New value"]

;; ;; =>


(rf/reg-event-db
 :re-form/manifest
 (fn [db [_ manifest]]
   (assoc-in db [:forms (:name manifest)] manifest)))

(defn not-blank? [v]
  (when (str/blank? v) "Should not be blank"))

(defn email? [v]
  (when (not (= "a@b.com" v)) "Should be a@b.com"))



(rf/reg-event-db
 :re-form/on-change
 (fn [db [_ pth v]]
   (let [manifest (get-in db pth)
         db (if-let [valid (get validators (:validator manifest))]
              (re-form.core/insert-by-path db (conj pth :error) (valid v))
              db)]
     (re-form.core/insert-by-path db (conj pth :value) v))))

(defn re-input [{pth :path}]
  (let [input-path pth
        sub (rf/subscribe [:re-form/data input-path])
        on-change (fn [ev] (rf/dispatch [:re-form/on-change input-path (.. ev -target -value)]))]
    (fn [props]
      [:div
       [:input.form-control {:type "text" :value (:value @sub)  :on-change on-change}]
       [:pre (:error @sub)]])))

(defn subform [{pth :path}]
  (let [sub (rf/subscribe [:re-form/data pth])]
    (fn [props]
      [:div [:h3 "Address"]
       [:pre (pr-str pth)]
       [re-input {:path (into pth [:fields :city])}]
       [re-input {:path (into pth [:fields :line])}]])))

(rf/reg-sub
 :re-form/value
 (fn [db [_ pth]]
   (let [form (get-in db pth)]
     (clojure.walk/prewalk
      (fn [x]
        (cond
          (and (map? x) (:fields x)) (:fields x)
          (and (map? x) (:value x)) (:value x)
          (and (map? x) (:items x)) (:items x)
          (map? x) nil
          :else x))
      form))))

(rf/reg-event-db
 :re-form/add-item
 (fn [db [_ path metadata]]
   (.log js/console "item" path metadata)
   (update-in db path conj metadata)))

(rf/reg-event-db
 :re-form/remove-item
 (fn [db [_ path i]]
   (.log js/console "item" path i)
   (update-in db path (fn [v]
                        (.log js/console (subvec v 0 i))
                        (into (subvec v 0 i)
                              (subvec v (inc i)))))))


(defn re-collection [{pth :path} input]
  (let [sub (rf/subscribe [:re-form/data pth])
        add-item (fn [] (rf/dispatch [:re-form/add-item (conj pth :items) (:item @sub)]))]
    (fn [props]
      [:div 
       (for [i (range (count (:items @sub)))]
         [:div {:key i} [input {:path (into pth [:items i])}]
          [:button {:on-click #(rf/dispatch [:re-form/remove-item (conj pth :items) i])} "x"]])
       [:br]
       [:button {:on-click add-item} "+"]])))

;; user space

(def form-address {:name :address
                   :fields {:city {:validator :not-blank? :type :string}}})

(def form-manifest {:name :user
                    :fields {:name {:validator :not-blank? :type :string}
                             :email {:validator :email? :type :password}
                             :roles {:type :collection
                                     :item form-address
                                     :items []}}})

(def validators {:not-blank? not-blank?
                 :email? email?})
(defn index []
  (rf/dispatch [:re-form/manifest form-manifest])
  (let [v (rf/subscribe [:re-form/value [:forms :user]])]
    (fn []
      [:div
       [:h1 "Form builder"]
       [:hr]
       (style form-style)

       [:pre (pr-str @v)]

       [re-input {:path [:forms :user :fields :name]}]
       [re-input {:path [:forms :user :fields :email]}]

       [:h3 "Collection"]

       [re-collection
        {:path [:forms :user :fields :roles]}
        subform]

       ])))


(defn select-page []
  [:div
   [:h1 "Select widget"]

   ])

(defn multiselect-page []
  [:h1 "Index"])

(def routes {:. :index
             "select" {:. :select}
             "multiselect" {:. :multiselect}})
(def pages
  {:index index
   :select select-page
   :multiselect multiselect-page})

(defn href
  [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))

(defn current-page []
  (let [{page :match params :params} @(rf/subscribe [:route-map/current-route])]
    (if page
      (if-let [cmp (get pages page)]
        [:div [cmp params]]
        [:div.not-found (str "Page not found [" (str page) "]" )])
      [:div.not-found (str "Route not found ")])))

(defn root-component []
  [:div.container-fluid
   (style [:body
           [:.navigation {:width (u/px 200) :float "left"}
            [:a.navitem {:display "block" :padding (u/px 10)}]]
           [:.pane {:margin-left (u/px 210) :margin {:top (u/px 20)}}]])
   [:div.navigation
    [:a.navitem {:href (href)} "re-form"]
    [:a.navitem {:href (href "select")} "Select"]
    [:a.navitem {:href (href "multiselect")} "MultiSelect"]]
   [:div.pane [current-page]]])

(rf/reg-event-fx
 ::initialize
 (fn [cofx]
   {:dispatch-n [[:route-map/init routes]]}))

(defn dispatch-routes [_]
  (let [fragment (.. js/window -location -hash)]
    (rf/dispatch [:fragment-changed fragment])))


(defn mount-root []
  (reagent/render [root-component] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch [::initialize])
  (mount-root))
