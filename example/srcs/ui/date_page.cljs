(ns ui.date-page
  (:require [re-form.core :as form]
            [ui.routes :as ui-routes]
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

          [:label "Birth Date (dropdown)"]
          [form/field {:path [:birthdate]
                       :format "dd.mm.yyyy"
                       :with-dropdown true
                       :input w/date-input}]]
         [:div.re-form-row

          [:label "Birth Date (chevrons)"]
          [form/field {:path [:birthdate]
                       :format "dd.mm.yyyy"
                       :with-chevrons true
                       :input w/date-input}]]
         [:div.re-form-row
          [:label "Birth Date 2(iso)"]
          [form/field {:path [:birthdate]
                       :input w/date-input}]]
         [:div.re-form-row
          [:label "Birth Date 2(russian)"]
          [form/field {:path [:birthdate]
                       :format "dd.mm.yyyy"
                       :input w/date-input}]]

         [:div.re-form-row
          [:label "Birth Date 2(eu)"]
          [form/field {:path [:birthdate]
                       :format "dd/mm/yyyy"
                       :input w/date-input}]]


         [:div.re-form-row
          [:label "Birth Date 2(us)"]
          [form/field {:path [:birthdate]
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
