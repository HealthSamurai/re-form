(ns ui.inputs-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-form.core :as form]
            [re-form.collection :as fc]
            [re-form.submit :as s]
            [reagent.core :as reagent]
            [cljs.core.async :refer [<! >! timeout]]
            [re-form.inputs :as w]
            [ui.routes :as ui-routes]
            [re-form.validators :as valid]))

(def example-async-validator
  (valid/debounced-async-validator
   500
   (fn [val path]
     (go
       (let [_ (<! (timeout 1000))]
         [[(str "Async validation result: " val)] path])))))

(defn inputs-page []
  (let [form {:form-name :inputs-form
              :validate-fn (fn [v]
                             (if (and (:name v) (not= (:name v) "nicola"))
                               {[:name] ["should be nicola"]}
                               {[:name] nil}))

              :value {:name "nicola"
                      :email "niquola@mail.com"
                      :organization {:name "github" :url "github.com"}
                      :groups [{:name "admin"} {:name "physician"}]
                      :telecom [{:system "phone" :value "+7 999 666 55 44"}
                                {:system "email" :value "abcab@aaa.com"}]

                      :cities ["Omsk" "SPb"]}}
        state (reagent/atom {:first-input-mode :first
                             :password-mounted true})]

    (fn []
      [form/form form
       [:div
        [:h1 "re-form demo page (most complete one)"]

        [:hr]
        [:div.row
         [:div.col
          [:div.re-form-row
           [:label "Name or Family name: "]
           [form/field (if (= (:first-input-mode @state) :first)
                         {:input w/text-input
                          :path [:name]}

                         {:input w/text-input
                          :path [:family-name]
                          :validators [(valid/min-count 5 count :message "Too short for a family name")]})]

           " "
           [:button {:on-click (fn [] (swap! state (fn [s]
                                                     (if (= (:first-input-mode s) :first)
                                                       (assoc s :first-input-mode :second)
                                                       (assoc s :first-input-mode :first)))))}
            "Change path for that input"]]

          [:div.re-form-row
           [:label "Email: "]
           [form/field {:path [:email]
                        :validators [(valid/email :message "email please")]
                        :input w/text-input}]

           #_[:code [:pre
                   "[form/field {:path [:email]
             :validators [(valid/email :message \"email please\")]
             :input w/text-input}]"]]]

          [:div.re-form-row
           [:label "Password: "]
           (when (:password-mounted @state)
             [form/field {:path [:password]
                          :validators [(valid/min-count 8 count :message "Too short for a password")
                                       example-async-validator]
                          :input w/text-input
                          :type "password"}])
           " "
           [:button {:on-click (fn []
                                 (swap! state (fn [s]
                                                (if (:password-mounted s)
                                                  (assoc s :password-mounted false)
                                                  (assoc s :password-mounted true)))))}
            "Remove/add password input"]]

          [:div.re-form-row
           [:label "Organization.name: "]
           [form/field {:path [:organization :name]
                        :input w/text-input
                        :validators [(valid/regex #".+ GmbH")]}]]

          [:div.re-form-row
           [:label "Organization.url: "]
           [form/field {:path [:organization :url] :input w/text-input}]]

          [:div.re-form-row
           [:label "group.0.name: "]
           [form/field {:path [:groups 0 :name]
                        :input w/text-input}]]

          [:div.re-form-row
           [:label "group.1.name: "]
           [form/field {:path [:groups 1 :name]
                        :input w/text-input}]]

          [:div.re-form-row
           [:label "Telecom: "]
           [fc/collection {:path [:telecom] :new-item-value {:system "phone"}}
            [form/field {:path [:value] :input w/text-input :validators [example-async-validator]}]]]

          [:div.re-form-row
           [:label "Cities: "]
           [fc/collection {:path [:cities] :new-item-value ""}
            [form/field {:path [] :input w/text-input :validators [(valid/not-blank :message "Me not to be so blank!")]}]]]

          [:hr]
          [:div.re-form-row
           [s/submit-button {:submit-fn #(js/alert (pr-str %))} "Submit!"]]]

         [:div.col
          [form/form-data {:form-name :inputs-form}]]]]])))

(ui-routes/reg-page
 :inputs {:title "Inputs"
          :w 2
          :cmp inputs-page})
