(ns ui.date-page
  (:require [re-form.core :as form]
            [ui.routes :as ui-routes]
            [re-form.inputs :as w]))

(defn datetime-page []
  (let [form {:form-name :calendars-form
              :value {:birthdate-one {:d 5 :m 3 :y 1980}
                      :birthdate-two "1995-04-17"
                      :empty nil}}]
    (fn []
      [form/form form
       [:div.row
        [:div.col
         [:h1 "Calendar"]
         [:div.re-form-row

          [:label "Birth Date"]
          [form/field {:form-name :calendars-form
                       :path [:birthdate-one]
                       :input w/calendar-input}]]
         [:div.re-form-row
          [:label "Birth Date 2(iso)"]
          [form/field {:form-name :calendars-form
                       :path [:birthdate-two]
                       :type "date"
                       :input w/date-input}]]
         [:div.re-form-row
          [:label "Birth Date 2(russian)"]
          [form/field {:form-name :calendars-form
                       :path [:birthdate-two]
                       :type "date"
                       :format "dd.mm.yyyy"
                       :input w/date-input}]]

         [:div.re-form-row
          [:label "Birth Date 2(eu)"]
          [form/field {:form-name :calendars-form
                       :path [:birthdate-two]
                       :type "date"
                       :format "dd/mm/yyyy"
                       :input w/date-input}]]


         [:div.re-form-row
          [:label "Birth Date 2(us)"]
          [form/field {:form-name :calendars-form
                       :path [:birthdate-two]
                       :type "date"
                       :format "us"
                       :input w/date-input}]]]

        [:div.re-form-row
           [:label "Birth Date empy"]
           [form/field {:form-name :calendars-form
                        :path [:empty]
                        :type "date"
                        :format "us"
                        :input w/date-input}]]


        [:div.col
         [form/form-data {:form-name :calendars-form}]]]])))

(ui-routes/reg-page
 :datetime {:title "Date/Time"
            :w 5
            :cmp  datetime-page})
