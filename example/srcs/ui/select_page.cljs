(ns ui.select-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
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

(defn select-page []
  (let [items [{:name "Nikolai"}
               {:name "Mike"}
               {:name "Max"}
               {:name "Marat"}
               {:name "Tim"}
               {:name "Slava"}]

        form {:form-name :selects-form
              :value {:owner {:name "Mike"}
                      :other-owner {:name "Marat"}
                      :last-owner {:name "Max"}}}]
    (fn []
      [form/form form
       [:h3 "Select widgets"]
       [:hr]
       [:div.row
        [:div.col
         [:div.re-form-row
          [:label "inputs/select-input"]
          [form/field {:items items
                       :label-fn :name
                       :path [:last-owner]
                       :input w/select-input}]]

         [:div.re-form-row
          [:label "inputs/select-xhr-input"]
          [form/field {:value-fn identity
                       :label-fn :display
                       :placeholder "Xhr select example"
                       :suggest-fn suggest
                       :path [:xhr]
                       :input w/select-xhr-input}]]

         [:div.re-form-row
          [:label "inputs/button-select-input"]
          [form/field {:items items
                       :label-fn :name
                       :path [:other-owner]
                       :input w/button-select-input}]]

         [:div.re-form-row
          [:div.col [form/form-data {:form-name :selects-form}]]]]
        [:div.col
         [:div.re-form-row
          [:label "Owner: "]
          [form/field {:items items
                       :label-fn :name
                       :path [:owner]
                       :input w/radio-input}]]]]])))

(ui-routes/reg-page
 :select {:title "Select"
          :w 3
          :cmp select-page})
