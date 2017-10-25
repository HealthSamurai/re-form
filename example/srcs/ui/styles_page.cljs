(ns ui.styles-page
  (:require [re-form.core :as form]
            [ui.routes :as ui-routes]
            [re-form.inputs.completions :as completions]
            [re-form.inputs :as w]))

(defn styles-page []
  [:div
   [:h1 "Styles"]
   (for [[k v] form/default-base-consts]
     [:div [:b (name k)]  " " (pr-str v)])
   
   ])

(ui-routes/reg-page
 :textarea {:title "Styles"
            :w 1.1 
            :cmp styles-page})
