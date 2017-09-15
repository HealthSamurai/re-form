(ns re-form.submit
  (:require [re-frame.core :as rf]
            [re-form.core :as fc]
            [re-form.context :as ctx]))

(defn- submit-button* [{:keys [form] :as props} & children]
  (let [form-errors (rf/subscribe [:re-form/form-errors form])
        form-value (rf/subscribe [:re-form/form-value form])]
    (fn [{submit-fn :submit-fn}]
      (let [disabled? (not (empty? @form-errors))]
        (into [:button.submit {:type "submit"
                               :on-click (fn [] (and submit-fn (submit-fn @form-value)))
                               :disabled disabled?}]
              children)))))

(defn submit-button [props & children]
  (into [ctx/get-context props submit-button*] children))
