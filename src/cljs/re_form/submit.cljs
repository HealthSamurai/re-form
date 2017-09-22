(ns re-form.submit
  (:require [re-frame.core :as rf]
            [re-form.core :as fc]
            [re-form.context :as ctx]))

(defn- submit-button* [{:keys [form-name] :as props} & children]
  (let [form-errors (rf/subscribe [:re-form/form-errors form-name])
        form-value (rf/subscribe [:re-form/form-value form-name])
        onclick (fn [submit-fn]
                  (rf/dispatch [:re-form/start-submitting form-name])
                  (when (empty? @form-errors)
                    (and submit-fn (submit-fn @form-value))))]

    (fn [{submit-fn :submit-fn}]
      (into [:button.submit {:type "submit"
                             :on-click #(onclick submit-fn)
                             :disabled disabled?}]
            children))))

(defn submit-button [props & children]
  (into [ctx/get-context props submit-button*] children))
