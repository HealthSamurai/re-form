(ns ui.date-page
  (:require [re-form.core :as form]
            [ui.routes :as ui-routes]
            [re-form.inputs :as w]))

(defn datetime-page []
  (let [form {:form-name :calendars-form
              :value {:birthdate-one {:d 5 :m 3 :y 1980}
                      :birthdate-two "1995-04-17"
                      :empty nil
                      :time "16:20"}}]
    (fn []
      [form/form form
       [:h1 "Calendar"]
       [:div.row
        [:div.col
         [:div.re-form-row

          [:label "Birth Date"]
          [form/field {:path [:birthdate-one]
                       :input w/calendar-input}]]
         [:div.re-form-row
          [:label "Birth Date 2(iso)"]
          [form/field {:path [:birthdate-two]
                       :input w/date-input}]]
         [:div.re-form-row
          [:label "Birth Date 2(russian)"]
          [form/field {:path [:birthdate-two]
                       :format "dd.mm.yyyy"
                       :input w/date-input}]]

         [:div.re-form-row
          [:label "Birth Date 2(eu)"]
          [form/field {:path [:birthdate-two]
                       :format "dd/mm/yyyy"
                       :input w/date-input}]]


         [:div.re-form-row
          [:label "Birth Date 2(us)"]
          [form/field {:path [:birthdate-two]
                       :format "us"
                       :input w/date-input}]]]
        [:div.col
         [:div.re-form-row
          [:label "Birth Date empy"]
          [form/field {:path [:empty]
                       :format "us"
                       :input w/date-input}]]

         [:div.re-form-row
          [:label "Time empy"]
          [form/field {:path [:empty]
                       :input w/time-input}]]
         [:div.re-form-row
          [:label "Time 24"]
          [form/field {:path [:time]
                       :input w/time-input}]]

         [:div.re-form-row
          [:label "Time 12"]
          [form/field {:path [:time]
                       :format "12h"
                       :input w/time-input}]]]


        [:div.col
         [form/form-data {:form-name :calendars-form}]]]])))

(ui-routes/reg-page
 :datetime {:title "Date/Time"
            :w 5
            :cmp  datetime-page})
