(ns ui.index-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [goog.string :as gstring]
   [cljs-http.client :as http]
   [re-form.inputs.completions :as completions]
   [cljs.core.async :refer [<! >! timeout]]
   [goog.string.format]
   [re-form.core :as form]
   [ui.routes :as ui-routes]
   [re-form.inputs :as w]))

(defn suggest [value]
  (go
    (map (fn [e] (select-keys (:resource e) [:display :system :code]))
         (:entry (:body
                  (<! (http/get
                       (gstring/format "https://ml.aidbox.io/$terminology/CodeSystem/$lookup?display=%s&system=http%3A%2F%2Fhl7.org%2Ffhir%2Fsid%2Ficd-10" value))))))))

(defn index []
  (let [select-items
        [{:name "Nikolai"}
         {:name "Mike"}
         {:name "Max"}
         {:name "Marat"}
         {:name "Tim"}
         {:name "Evgeny"}
         {:name "Slava"}]
        form {:form-name :example-form
              :value {:name "Mike"}}]
    (fn []
      [form/form form
       [:h1 "Form builder"]
       [:div.row
        [:div.col
         [:div.re-form-comp
          [:label.re-form-label "Text input"]
          [:label.re-form-label.sub "Fields can also have a description"]
          [form/field {:form-name :example-form :path [:name] :input w/text-input}]]
         [:div.re-form-comp
          [:label.re-form-label "Another text input"]
          [form/field {:form-name :example-form :path [:name] :input w/text-input}]]
         [:div.re-form-comp
          [:label.re-form-label "Select input"]
          [form/field {:items select-items
                       :label-fn :name
                       :path [:last-owner]
                       :input w/select-input}]]
         [:div.re-form-comp
          [:label.re-form-label "Textarea"]
          [form/field {:path [:area-one]
                       :input w/textarea-input}]]]
        [:div.col
         [:div.re-form-comp
          [:label.re-form-label "XHR select input"]
          [:label.re-form-label.sub "Loads data from external resource"]
          [form/field {:value-fn identity
                       :label-fn :display
                       :placeholder "Xhr select example"
                       :suggest-fn suggest
                       :path [:xhr]
                       :input w/select-xhr-input}]]
         [:div.re-form-comp
          [:label.re-form-label "CM textarea"]
          [form/field {:path [:area-two]
                       :complete-fn (partial completions/complete-startswith
                                             #"#"
                                             {"comp" ["complement" "complete" "computer"]})
                       :input w/codemirror-input}]]
         [:div.re-form-comp
          [:label.re-form-label "Radio input"]
          [form/field {:items select-items
                       :label-fn :name
                       :path [:last-owner]
                       :input w/radio-input}]]]
        [:div.col
         [:div.re-form-comp
          [:label.re-form-label "Button select input"]
          [:label.re-form-label.sub "Use it when there are less than 5 elements"]
          [form/field {:items (take 5 select-items)
                       :label-fn :name
                       :path [:last-owner]
                       :input w/button-select-input}]]
         [:div.re-form-comp
          [:label.re-form-label "Switchbox"]
          [form/field {:path [:admin]
                       :label "admin"
                       :input w/switchbox-input}]]
         [:div.re-form-comp
          [:label.re-form-label "Checkbox"]
          [form/field {:items select-items
                       :label-fn :name
                       :path [:multi]
                       :input w/checkbox-group-input}]]]]])))

(ui-routes/reg-page
 :index {:title "All components"
         :w 1
         :cmp index})
