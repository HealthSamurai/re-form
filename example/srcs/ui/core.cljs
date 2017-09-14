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
   [clojure.string :as str]
   [ui.file-upload-page :as fup]
   [re-form.inputs :as w]))

(defn style [gcss]
  [:style (garden/css gcss)])

(defn index []
  (let [form {:name :example-form
              :options  {:gender ["Male" "Female"]}
              :meta {:properties {:name {:validators {:not-blank true}}
                                  :gender {}
                                  :owner  {:validators {:not-blank true}}}}
              :value {:name "Mike"}}]
    (rf/dispatch [:re-form/init form])
    (fn []
      [:div
       [:h1 "Form builder"]

       [:label "Name"]
       [form/input {:form :example-form :path [:name] :input w/text-input}]
       [form/errors-for {:form :example-form :path [:name]}]


       [:label "Gender"]
       #_[form/re-radio-buttons {:form form :name :gender
                                 :label-fn identity
                                 :options-path [:forms :myform :options :gender]}]
       [form/input {:form :example-form :path [:gender] :input w/text-input}]

       #_[:h3 "Collection"]

       #_[form/re-collection
          {:form form :name :address}
          address-form]])))

(defn select-page []
  (let [items [{:name "Nikolai"}
               {:name "Mike"}
               {:name "Max"}
               {:name "Marat"}
               {:name "Tim"}
               {:name "Slava"}]
        form {:name :selects-form
              :meta {:properties {:owner  {:validators {:not-blank true}}}}
              :value {:owner {:name "Mike"}}}]
    (rf/dispatch [:re-form/init form])

    (fn []
      [:div.row
       [:div.col
        [:h1 "Select widget"]

        [:label "Owner: "]
        [form/input {:form :selects-form
                     :items items
                     :label-fn :name
                     :path [:owner]
                     :input w/radio-input}]

        ;; [:label "Owner: "]
        ;; [form/re-radio-buttons {:form form
        ;;                         :name :owner
        ;;                         :options-path (conj form-path :options)
        ;;                         :label-fn :name}]

        ;; [:br]
        ;; [:br]
        ;; [:label "Owner: "]
        ;; [form/re-radio-group {:form form
        ;;                       :name :owner
        ;;                       :options-path (conj form-path :options)
        ;;                       :label-fn :name}]]
        [:div.col [form/form-data {:form :selects-form}]]]])))

(defn switchbox-page []
  (let [form {:path [:forms :myform]
              :properties {:admin {:type :boolean}}}]
    (rf/dispatch [:re-form/init form])
    (let [v (rf/subscribe [:re-form/value [:forms :myform]])]
      (fn []
        [:div.row
         #_[:div.col
            [:h1 "Switch widget"]
            [form/re-switch-box {:form form :name :admin :label "admin?"}]]
         #_[:div.col
            [form-data form]]]))))

#_(defn list-page []
    (let [form {:path [:forms :myform]
                :properties {:roles {:items {:type :string}}}
                :value {:roles ["a", "b"]}}]
      (rf/dispatch [:re-form/init form])
      (let [v (rf/subscribe [:re-form/value [:forms :myform]])]
        (fn []
          [:div "list"]
          [:div.row
           [:div.col
            [:h1 "re-list widget"]
            [:div.form-row
             [:label "Roles"]
             [form/re-list {:form form :name :roles}]]

            [:div.form-row
             [:label "Roles"]
             [form/re-list {:form form :name :roles}]]]

           [:div.col
            [form-data form]]]))))

(defn datetime-page []
  (let [name :birthdate
        form {:path [:forms :myform]
              :properties {:birthdate {:type :date}}
              :value {name "05-03-1980"}}
        opts {:form form :name name}
        value (rf/subscribe [:re-form/value opts])
        on-change (fn [day] (rf/dispatch [:re-form/update opts day]))]
    (rf/dispatch [:re-form/init form])
    (fn []
      [:div.row
       #_[:div.col
          [:h1 "Calendar"]
          [:div.form-row
           [:label "Birth Date"]
           [form/re-calendar {:form form :name :birthdate}]]

            [:div.form-row
             [:label "Birth Date 2"]
             [form/re-calendar {:form form :name :birthdate}]]]

         #_[:div.col
          [form-data form]]])))


(defn inputs-page []
  (let [form {:name :inputs-form
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
          [form/input {:form :inputs-form :path [:name] :input w/text-input}]
          [form/errors-for {:form :inputs-form :path [:name]}]]

         [:div.form-row
          [:label "Email: "]
          [form/input {:form :inputs-form :path [:email] :input w/text-input}]
          [form/errors-for {:form :inputs-form :path [:email]}]]

         [:label "Password: "]
         [form/input {:form :inputs-form :path [:password] :input w/text-input :type "password"}]

         [:div.form-row
          [:label "Organization.name: "]
          [form/input {:form :inputs-form :path [:organization :name] :input w/text-input}]
          [form/errors-for {:form :inputs-form :path [:organization :name]}]]

         [:div.form-row
          [:label "Organization.url: "]
          [form/input {:form :inputs-form :path [:organization :url] :input w/text-input}]
          [form/errors-for {:form :inputs-form :path [:organization :url]}]]

         [:div.form-row
          [:label "group.0.name: "]
          [form/input {:form :inputs-form :path [:groups 0 :name] :input w/text-input}]
          [form/errors-for {:form :inputs-form :path [:groups 0 :name]}]]

         [:div.form-row
          [:label "group.1.name: "]
          [form/input {:form :inputs-form :path [:groups 1 :name] :input w/text-input}]
          [form/errors-for {:form :inputs-form :path [:groups 1 :name]}]]
         ]
        [:div.col
         [form/form-data {:form :inputs-form}]]]])))

(defn multiselect-page []
  [:h1 "Index"])

(defn textarea-page []
  (let [name :simpletext
        form {:path [:forms :myform]
              :lines-after 2
              :value {name "Fill me"}}
        opts {:form form :name name}
        value (rf/subscribe [:re-form/value opts])
        update-fn #(rf/dispatch [:re-form/update opts (.. % -target -value)])]
    (rf/dispatch [:re-form/init form])
    (fn []
      [:div [:h1 "Text field widget"]
       #_[:div.row
          [:div.col
           [widgets/textarea {:value value
                              :on-change update-fn
                              :lines-after (:lines-after form)}]]
          [:div.col
           [form-data form]]]])))

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
              :cmp datetime-page}
   :switchbox {:title "Switch"
               :w 6
               :cmp switchbox-page}

   ;; :list {:title "List"
   ;;        :w 2
   ;;        :cmp list-page}

   :file_upload {:title "File Upload"
                 :w 2
                 :cmp fup/file-upload-page}
   :textarea {:title "Text Area"
              :w 7
              :cmp textarea-page}})

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
