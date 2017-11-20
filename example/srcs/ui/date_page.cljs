(ns ui.date-page
  (:require [re-form.core :as form]
            [ui.routes :as ui-routes]
            [re-form.validators :as v]
            [re-form.inputs :as w]))

(defn datetime-page []
  (let [form {:form-name :calendars-form
              :value {:birthdate "1995-04-17"
                      :empty nil
                      :time "16:20"}}]
    (fn []
      [form/form form
       [:h1 "Calendar"]
       [:div.row
        [:div.col
         [:div.re-form-row
          [:label "Birth Date (all in one)"]
          [form/field {:path [:birthdate]
                       :format "dd.mm.yyyy"
                       :with-chevrons true
                       :with-dropdown true
                       :input w/date-input}]]
         [:div.re-form-row

          [:label "Birth Date (dropdown)"]
          [form/field {:path [:birthdate]
                       :format "dd.mm.yyyy"
                       :with-dropdown true
                       :input w/date-input}]]
         [:div.re-form-row
          [:label "Birth Date (iso)"]
          [form/field {:path [:birthdate]
                       :input w/date-input}]]
         [:div.re-form-row
          [:label "Birth Date (russian)"]
          [form/field {:path [:birthdate]
                       :format "dd.mm.yyyy"
                       :input w/date-input}]]

         [:div.re-form-row
          [:label "Birth Date (eu)"]
          [form/field {:path [:birthdate]
                       :format "dd/mm/yyyy"
                       :input w/date-input}]]


         [:div.re-form-row
          [:label "Birth Date Required (us, required)"]
          [form/field {:path [:birthdate-two]
                       :format "us"
                       :validators [(v/not-blank)]
                       :input w/date-input}]]]
        [:div.col
         [:div.re-form-row
          [:label "Birth Date (chevrons)"]
          [form/field {:path [:birthdate]
                       :format "dd.mm.yyyy"
                       :with-chevrons true
                       :input w/date-input}]]
         [:div.re-form-row
          [:label "Birth Date empy"]
          [form/field {:path [:empty-date]
                       :format "us"
                       :input w/date-input}]]

         [:div.re-form-row
          [:label "Time empy"]
          [form/field {:path [:empty-time]
                       :input w/time-input}]]
         [:div.re-form-row
          [:label "Time 24"]
          [form/field {:path [:time]
                       :input w/time-input}]]

         [:div.re-form-row
          [:label "Time 12"]
          [form/field {:path [:time]
                       :format "12h"
                       :input w/time-input}]]
         [:div.re-form-row
          [:label "Datetime input (UTC)"]
          [form/field {:path [:date-time]
                       :format-date "dd.mm.yyyy"
                       :format-time "24h"
                       :input w/date-time-input}]]
         [:div.re-form-row
          [:label "Datetime input (Moscow local)"]
          [form/field {:path [:date-time]
                       :format-date "dd.mm.yyyy"
                       :timezone "Europe/Moscow"
                       :format-time "24h"
                       :input w/date-time-input}]]]


        [:div.col
         [form/form-data {:form-name :calendars-form}]]]])))

(ui-routes/reg-page
 :datetime {:title "Date/Time"
            :w 5
            :cmp  datetime-page})
