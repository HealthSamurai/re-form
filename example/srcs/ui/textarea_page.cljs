(ns ui.textarea-page
  (:require [re-form.core :as form]
            [ui.routes :as ui-routes]
            [re-form.inputs.completions :as completions]
            [re-form.inputs :as w]))

(defn textarea-page []
  (let [form {:form-name :textarea-form
              :value {:area-one "Fill me"
                      :area-two "Superior codemirror! Type #comp to get completions!\n"}}]
    (fn []
      [form/form form
       [:div [:h1 "Text field widget"]
        [:div.row
         [:div.col
          [form/field {:path [:area-one]
                       :input w/textarea-input}]
          [form/field {:path [:area-two]
                       :complete-fn (partial completions/complete-startswith
                                             #"#"
                                             {"comp" ["complement" "complete" "computer"]})
                       :input w/codemirror-input}]]
         [:div.col
          [form/form-data {:form-name :textarea-form}]]]]])))

(ui-routes/reg-page
 :textarea {:title "Text Area"
            :w 7
            :cmp textarea-page})
