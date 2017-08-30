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


(defn data []
  (let [d (rf/subscribe [:reform/data [:forms :user]])]
    (fn [] [:pre (pr-str @d)])))


(defn subform [{pth :path}]
  (let [sub (rf/subscribe [:re-form/data pth])]
    (fn [props]
      [:div [:h3 "Address"]
       [:pre (pr-str pth)]
       [form/re-input {:path (into pth [:fields :city])}]
       [form/re-input {:path (into pth [:fields :line])}]])))


;; user space

(def form-address {:name :address
                   :fields {:city {:validator :not-blank? :type :string}}})

(def form-manifest {:name :user
                    :fields {:name {:validator :not-blank? :type :string}
                             :email {:validator :email? :type :password}
                             :roles {:type :collection
                                     :item form-address
                                     :items []}}})

(defn index []
  (rf/dispatch [:re-form/manifest form-manifest])
  (let [v (rf/subscribe [:re-form/value [:forms :user]])]
    (fn []
      [:div
       [:h1 "Form builder"]
       [:hr]
       [:pre (pr-str @v)]

       [form/re-input {:path [:forms :user :fields :name]}]
       [form/re-input {:path [:forms :user :fields :email]}]

       [:h3 "Collection"]

       [form/re-collection {:path [:forms :user :fields :roles]} subform]

       ])))

(defn select-page []
  (rf/dispatch [:re-form/manifest {:name :myform
                                   :fields {:owner {:type :object
                                                    :options [{:name "Nikolai"}
                                                              {:name "Mike"}
                                                              {:name "Max"}
                                                              {:name "Marat"}
                                                              {:name "Tim"}
                                                              {:name "Slava"}]}}}])
  (let [v (rf/subscribe [:re-form/value [:forms :myform]])]
    (fn []
      [:div
       [:h1 "Select widget"]
       (style [:pre.value {:background-color "#f1f1f1"
                        :padding (u/px 20)}])
       [:hr]
       [:pre.value [:code (pr-str @v)]]
       [:lable "Owner: "]
       [form/re-select {:path [:forms :myform :fields :owner]
                        :options-path [:forms :myform :fields :owner :options]}]

       [:br]
       [:br]
       [:lable "Owner: "]
       [form/re-radio-buttons {:path [:forms :myform :fields :owner]
                               :options-path [:forms :myform :fields :owner :options]}]

       [:br]
       [:br]
       [:lable "Owner: "]
       [form/re-radio-group {:path [:forms :myform :fields :owner]
                        :options-path [:forms :myform :fields :owner :options]}]

       ])))

(defn inputs-page []
  (rf/dispatch [:re-form/manifest {:name :myform
                                   :fields {:name {:type :string}
                                            :email {:type :string}
                                            :password {:type :password}}}])
  (let [v (rf/subscribe [:re-form/value [:forms :myform]])]
    (fn []
      [:div

       [:h1 "Select widget"]
       [:hr]
       [:pre [:code (pr-str @v)]]
       [:lable "Name: "]
       [form/re-input {:path [:forms :myform :fields :name]}]

       [:lable "Email: "]
       [form/re-input {:path [:forms :myform :fields :email]}]

       [:lable "Password: "]
       [form/re-input {:path [:forms :myform :fields :password]}]

       ])))

(defn multiselect-page []
  [:h1 "Index"])

(def pages
  {:index {:title "Form builder"
           :w 1
           :cmp index}
   :inputs {:title "Inputs"
            :w 2
            :cmp inputs-page}
   :select {:title "Select"
            :w 3
            :cmp select-page}

   :multiselect {:title "MultiSelect"
                 :w 4
                 :cmp multiselect-page}
   :datetime {:title "Date/Time"
                 :w 5
                 :cmp multiselect-page}
   :upload {:title "Upload"
              :w 5
              :cmp multiselect-page}})

(def routes (reduce (fn [acc [k v]] (assoc acc (name k) {:. (assoc (dissoc v :cmp) :id k)})) {:. :index} pages))

(defn href
  [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))

(defn current-page []
  (let [current-route (rf/subscribe [:route-map/current-route])]
    (fn []
      (let [{page :match params :params} @current-route]
        (.log js/console page (:id page))
        (if page
          (if-let [cmp (:cmp (get pages (:id page)))]
            [:div [cmp params]]
            [:div.not-found (str "Page not found [" (str page) "]" )])
          [:div.not-found (str "Route not found ")])))))

(defn navigation []
  (let [current-route (rf/subscribe [:route-map/current-route])]
    (fn []
      [:div.navigation
       (style [:.navigation {:padding {:top (u/px 20)
                                       :right (u/px 20)}
                             :background-color "#f1f1f1"}
               [:a.navitem {:display "block"
                            :color "#888"
                            :border-left "6px solid #ddd"
                            :font-family "lato"
                            :padding {:top (u/px 10)
                                      :bottom (u/px 10)
                                      :left (u/px 20)}}
                [:&.active {:color "#007bff"
                            :background-color "#eee"
                            :border-color "#007bff"}]]])
       (doall
        (for [[i p] (sort-by (fn [[_ x]] (:w x)) pages)]
          [:a.navitem {:key i
                       :class (when (= i (get-in @current-route [:match :id]))
                                "active")
                       :href (href (name i))} (:title p)]))])))

(defn root-component []
  [:div
   (style
    (let [nav-width 300]
      [:body
       [:.topnav {:background-color "#3F51B5"
                  :color "white"}
        [:.brand {:display "inline-block"
                  :font-size (u/px 30)
                  :font-weight "bold"
                  :margin {:left (u/px 20)}
                  :font-family "lato"
                  :padding (u/px 10)}]]
       [:.navigation {:width (u/px nav-width)
                      :position "absolute"
                      :top (u/px 67)
                      :bottom 0
                      :left 0}]
       [:.pane {:margin {:left (u/px (+ nav-width 20))
                         :top (u/px 20)
                         :right (u/px 40)}
                :padding (u/px 40)}]
       form/form-style]))
   [:div.topnav [:a.brand "re-form"]]
   [navigation]
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
