(ns ui.checkbox-page
  (:require [re-form.core :as form]
            [re-form.inputs :as w]
            [ui.routes :as ui-routes]))

(defn checkbox-page []
  (let [items [{:name "Nikolai"}
               {:name "Mike"}
               {:name "Max"}
               {:name "Marat"}
               {:name "Tim"}
               {:name "Slava"}]
        form {:form-name :checkbox-form
              :value {:multies #{{:name "Mike"}
                                 {:name "Marat"}}}}]
    (fn []
      [form/form form
       [:div.row
        [:div.col
         [:h1 "Checkbox group"]

         [:label "Select multiple: "]
         [form/field {:items items
                      :label-fn :name
                      :path [:multies]
                      :input w/checkbox-group-input}]
         [:div.col [form/form-data {:form-name :checkbox-form}]]]]])))

(ui-routes/reg-page
 :checkbox {:title "Checkbox"
            :w 4
            :cmp  checkbox-page})
