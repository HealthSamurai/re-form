(ns re-form.submit
  (:require [re-frame.core :as rf]
            [re-form.core :as fc]
            [re-form.context :as ctx]
            [clojure.string :as str]))

(defn- process-class [class]
  (if (keyword? class)
    (str/join " " (str/split (name class) \.))
    class))

(defn- submit-button* [{:keys [form-name prevent-default] :as props} & children]
  (let [form-errors (rf/subscribe [:re-form/form-errors form-name])
        form-value (rf/subscribe [:re-form/form-value form-name])
        onclick (fn [ev submit-fn]
                  (when prevent-default
                    (.preventDefault ev))
                  (rf/dispatch [:re-form/start-submitting form-name])
                  (when (empty? @form-errors)
                    (and submit-fn (submit-fn @form-value))))]

    (fn [{:keys [submit-fn class]}]
      (into [:button.submit {:type "submit"
                             :class (process-class class)
                             :on-click #(onclick % submit-fn)}]
            children))))

(defn submit-button [props & children]
  (into [ctx/get-context props submit-button*] children))
