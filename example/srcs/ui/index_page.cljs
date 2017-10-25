(ns ui.index-page
  (:require
   [re-form.core :as form]
   [ui.routes :as ui-routes]
   [re-form.inputs :as w]))

(defn index []
  (let [form {:form-name :example-form
              :value {:name "Mike"}}]
    (fn []
      [form/form form
       [:h1 "Form builder"]

       [:label "Name"]
       [form/field {:form-name :example-form :path [:name] :input w/text-input}]


       [:label "Gender"]
       [form/field {:form-name :example-form :path [:gender] :input w/text-input}]
       ])))


(ui-routes/reg-page
 :index {:title "All components"
         :w 1
         :cmp index})
