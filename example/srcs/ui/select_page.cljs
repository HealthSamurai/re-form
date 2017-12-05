(ns ui.select-page
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [re-frame.core :as rf])
  (:require
   [clojure.string :as str]
   [re-form.validators :as v]
   [re-form.submit :as s]
   [re-frame.core :as rf]
   [goog.string :as gstring]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<! >! timeout]]
   [goog.string.format]
   [re-form.core :as form]
   [ui.routes :as ui-routes]
   [re-form.inputs :as w]))

(defn suggest [value]
  (go
    (map (fn [e] (select-keys (:resource e) [:display :system :code]))
         (:entry (:body
                  (<! (http/get
                       (gstring/format "https://ml.aidbox.io/$terminology/CodeSystem/$lookup?display=%s&system=http%3A%2F%2Fhl7.org%2Ffhir%2Fsid%2Ficd-10" value))))))))

(def people-path [:options :people])
(def people [{:name "Nikolai"}
             {:name "Mike"}
             {:name "Max"}
             {:name "Marat"}
             {:name "Tim"}
             {:name "Slava"}])

(rf/reg-event-db
 ::init
 (fn [db _]
   (assoc-in db people-path people)))

(rf/reg-sub
 ::people
 (fn [db]
   (->> (get-in db people-path))))

(rf/reg-event-db
 ::search-people
 (fn [db [_ q]]
   (.log js/console "search..." q) db
   (->>
    (if (and q (not (str/blank? q)))
      (->> people
           (filterv (fn [x] (str/includes? (str/lower-case (:name x))
                                           (str/lower-case q)))))
      people)
    (assoc-in db people-path))))


(rf/reg-sub
 ::async-items
 (fn [db]
   (->> (get-in db [:options :async]))))

(rf/reg-event-fx
 ::search-async
 (fn [fx [_ q]]
   {:json/fetch {:uri "https://ml.aidbox.io/$terminology/CodeSystem/$lookup"
                 :params {:display q}
                 :success {:event ::search-async-done}}}))

(rf/reg-event-db
 ::search-async-done
 (fn [db [_ {data :data}]]
   (.log js/console data)
   (assoc-in db [:options :async]
             (mapv :resource (:entry data)))))

(defn select-page []
  (rf/dispatch [::init])
  (let [items people
        people-sub (rf/subscribe [::people])
        async-items (rf/subscribe [::async-items])
        form {:form-name :selects-form
              :value {:owner {:name "Mike"}
                      :tags #{"tags" "are" "unordered"}
                      :other-owner {:name "Marat"}
                      :last-owner {:name "Max"}}}]
    (fn []
      [form/form form
       [:h3 "Select widgets"]
       [:hr]
       [:div.row
        [:div.col
         [:div.re-form-row
          [:label "Ultimate (maybe) alpha select"]
          [form/field {:label-fn :display
                       :path [:new-icd10]
                       :on-search #(rf/dispatch [::search-async %])
                       :options async-items
                       :validators [(v/not-blank)]
                       :debounce-interval 300
                       :input w/select}]]
         [:div.re-form-row
          [:label "Ultimate alpha select (static)"]
          [form/field {:label-fn identity
                       :path [:new-superuser]
                       :options ["One" "Two" "Three"]
                       :input w/select}]]
         [:div.re-form-row
          [:label "inputs/select-input"]
          [form/field {:label-fn :name
                       :path [:last-owner]
                       :items people 
                       :input w/select-input}]]

         [:div.re-form-row
          [:label "inputs/re-select-input"]
          [form/field {:label-fn :name
                       :path [:supperuser]
                       :search-event {:event ::search-people}
                       :options-sub people-sub
                       :input w/re-select-input}]]

         [:div.re-form-row
          [:label "inputs/re-select-input async"]
          [form/field {:label-fn :display #_(fn [x] [:span [:b (:dispaly x)] " " (:definition x)])
                       :path [:icd10]
                       :search-event {:event ::search-async}
                       :options-sub async-items
                       :input w/re-select-input}]]

         [:div.re-form-row
          [:label "inputs/select-xhr-input"]
          [form/field {:value-fn identity
                       :label-fn :display
                       :placeholder "Xhr select example"
                       :suggest-fn suggest
                       :path [:xhr]
                       :input w/select-xhr-input}]]

         [:div.re-form-row
          [:label "inputs/button-select-input (required)"]
          [form/field {:items items
                       :validators [(v/not-blank)]
                       :label-fn :name
                       :path [:empty-but-required]
                       :input w/button-select-input}]]
         [:div.re-form-row
          [:label "inputs/button-select-input"]
          [form/field {:items items
                       :label-fn :name
                       :path [:other-owner]
                       :input w/button-select-input}]]

         [:div.re-form-row
          [s/submit-button {:submit-fn #(js/alert (pr-str %))} "Submit!"]]

         [:div.re-form-row
          [:div.col [form/form-data {:form-name :selects-form}]]]]
        [:div.col
         [:div.re-form-row
          [:label "Owner: "]
          [form/field {:items items
                       :label-fn :name
                       :path [:owner]
                       :input w/radio-input}]]

         [:div.re-form-row
          [:label "Tags input (space separates):"]
          [form/field {:path [:tags]
                       :space-delimiter true
                       :input w/tags}]]
         [:div.re-form-row
          [:label "Tags input:"]
          [form/field {:path [:tags]
                       :input w/tags}]]]]])))

(ui-routes/reg-page
 :select {:title "Select"
          :w 3
          :cmp select-page})
