(ns ui.complex-path-page
  (:require [re-form.core :as form]
            [re-form.collection :as fc]
            [re-frame.core :as rf]
            [re-form.submit :as s]
            [reagent.core :as reagent]
            [cljs.core.async :refer [<! >! timeout]]
            [re-form.inputs :as w]
            [ui.routes :as ui-routes]
            [re-form.validators :as valid]))

(def form-name :complex-path)
(def form-init
  {:form-name form-name
   :value {:resourceType "Patient"
           :id "patient-id"
           :contact [{:telecom [{:system "phone"
                                 :use "home"
                                 :rank 1
                                 :value "8800100200"}
                                {:system "sms"
                                 :use "work"
                                 :value "88888900000"}
                                {:system "fax"
                                 :value "+78219090"}]}]}})

(defn page [& [form-value]]
  (let [form (merge-with merge form-init form-value)
        ]
    [:div

     [:h1 "Complex paths"]

     [form/form form
      [:div.re-form-row
       [:label "Home phone"]
       [form/field
        {:input w/text-input
         :path [:contact 0 :telecom {:get [:and [:= :system "phone"] [:= :use "home"]]
                                     :set {:system "phone" :use "home" :value nil}}      :value ]}]]

      [:label "Work email"]
      [form/field {:input w/text-input
                   :validators [(valid/email :message "email please") (valid/not-blank :message "not blank")]
                   :path [:contact 0 :telecom {:get [:and [:= :system "email"] [:= :use "work"]]
                                               :set {:system "email" :use "work" :value nil}}  :value ]}]

      [:label "Fax"]
      [form/field {:input w/text-input
                   :path [:contact 0 :telecom {:get [:= "fax" :system]
                                               :set {:system "fax" :value nil}}  :value ]}]

      [:div.re-form-row
       [:div.col [form/form-data form]]]]]

    ))


(ui-routes/reg-page
 :complex {:title "Complex paths"
          :w 40
          :cmp page})
