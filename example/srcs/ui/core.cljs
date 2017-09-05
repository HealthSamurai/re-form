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



(defn form-data [form]
  (let [ data (rf/subscribe [:re-form/data (:path form)])]
    (fn [props]
      [:pre [:code (with-out-str (cljs.pprint/pprint @data))]])))


(defn address-form [{form :form pth :path}]

  )


(defn index []
  (let [form-path [:forms :myform]
        form {:path form-path
              :options  {:gender ["Male" "Female"]}
              :meta {:properties {:name {:validators {:not-blank true}}
                                  :gender {}
                                  :owner  {:validators {:not-blank true}}}}
              :value {:owner {:name "Mike"}}}]
    (rf/dispatch [:re-form/init form])
    (fn []
      [:div
       [:h1 "Form builder"]

       [:label "Name"]
       [form/input {:form form :name :name}]


       [:label "Gender"]
       [form/re-radio-buttons {:form form :name :gender
                               :label-fn identity
                               :options-path [:forms :myform :options :gender]}]

       [:h3 "Collection"]

       #_[form/re-collection
        {:form form :name :address}
        address-form]])))

(defn select-page []
  (let [form-path [:forms :myform]
        form {:path form-path
              :options [{:name "Nikolai"}
                        {:name "Mike"}
                        {:name "Max"}
                        {:name "Marat"}
                        {:name "Tim"}
                        {:name "Slava"}]
              :meta {:properties {:owner  {:validators {:not-blank true}}}}
              :value {:owner {:name "Mike"}}}]
    (rf/dispatch [:re-form/init form])
    (fn []
      [:div.row

       [:div.col
        [:h1 "Select widget"]
        
        [:label "Owner: "]
        [form/re-select {:form form
                         :options-path (conj form-path :options)
                         :placeholder "Select user"
                         :name :owner
                         :label-fn :name}]

        [:br]
        [:br]
        [:label "Owner: "]
        [form/re-radio-buttons {:form form
                                :name :owner
                                :options-path (conj form-path :options)
                                :label-fn :name}]

        [:br]
        [:br]
        [:label "Owner: "]
        [form/re-radio-group {:form form
                              :name :owner
                              :options-path (conj form-path :options)
                              :label-fn :name}]]
       [:div.col [form-data form]]])))

(defn switchbox-page []
  (let [form {:path [:forms :myform]
              :properties {:admin {:type :boolean}}}]
    (rf/dispatch [:re-form/init form])
    (let [v (rf/subscribe [:re-form/value [:forms :myform]])]
      (fn []
        [:div.row
         [:div.col
          [:h1 "Switch widget"]
          [form/re-switch-box {:form form :name :admin :label "admin?"}]]
         [:div.col
          [form-data form]]]))))



(defn inputs-page []
  (let [form-path [:forms :myform]
        form {:path form-path
              :meta {:properties {:name  {:validators {:not-blank true}}
                                  :email {:validators {:email true}}
                                  :organization {:properties {:name {:validators {:not-blank true}}
                                                              :url {:validators {:uri true}}}}
                                  :groups {:items {:properties {:name {:validators {:not-blank true}}}}}}}
              :value {:name "nicola"
                      :email "niquola@mail.com"
                      :organization {:name "github" :url "github.com"}
                      :groups [{:name "admin"} {:name "physician"}]}}]
    (rf/dispatch [:re-form/init form])
    (fn []
      [:div
       [:h1 "Select widget"]
       
       [:hr]
       [:div.row
        [:div.col
         [:div.form-row
          [:label "Name: "]
          [form/input {:form form :name :name}]
          [form/errors-for {:form form :name :name}]]

         [:div.form-row
          [:label "Email: "]
          [form/input {:form form :name :email}]
          [form/errors-for {:form form :name :email}]]

         [:label "Password: "]
         [form/input {:form form :name :password :type "password"}]

         [:div.form-row
          [:label "Organization.name: "]
          [form/input {:form form :path [:organization] :name :name}]
          [form/errors-for {:form form :path [:organization] :name :name}]]

         [:div.form-row
          [:label "Organization.url: "]
          [form/input {:form form :path [:organization] :name :url}]
          [form/errors-for {:form form :path [:organization] :name :url}]]


         [:div.form-row
          [:label "group.0.name: "]
          [form/input {:form form :path [:groups 0] :name :name}]
          [form/errors-for {:form form :path [:groups 0] :name :name}]]

         [:div.form-row
          [:label "group.1.name: "]
          [form/input {:form form :path [:groups 1] :name :name}]
          [form/errors-for {:form form :path [:groups 1] :name :name}]]
         ]
        [:div.col
         [form-data form]]]
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
   :switchbox {:title "Switch"
               :w 6
               :cmp switchbox-page}
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
       [:h1 {:margin-bottom (u/px 30)}]
       form/form-style
       [:.form-row {:padding "5px 0px"}]
       [:pre {:background-color "#f1f1f1" :padding "20px" :border "1px solid #ddd"} ]
       [:label {:width "10em"
                :vertical-align "top"
                :color "#888"
                :display "inline-block" :text-align "right" :padding-right "10px"}]
       [:.errors {:color "red" :margin-left "10em"}]]))

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
