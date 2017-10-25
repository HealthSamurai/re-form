(ns ui.switchbox-page
  (:require [re-form.core :as form]
            [re-form.inputs :as w]

            [ui.routes :as ui-routes]))

(defn switchbox-page []
  (let [form {:form-name :switches-form
              :value {:admin true
                      :superuser false}}]
    (fn []
      [form/form form
       [:div.row
        [:div.col
         [:h1 "Switch widget"]
         [:label "Is admin?"]
         [form/field {:path [:admin]
                      :label "admin"
                      :input w/switchbox-input}]
         [:label "Is superuser?"]
         [form/field {:path [:superuser]
                      :label "superuser"
                      :input w/switchbox-input}]]
        [:div.col
         [form/form-data {:form-name :switches-form}]]]])))

(ui-routes/reg-page
 :switchbox {:title "Switch"
             :w 6
             :cmp  switchbox-page})
