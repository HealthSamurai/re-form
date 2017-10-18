(ns re-form.inputs.date-input-impl
  (:require [reagent.core :as r]
            [re-form.inputs.common :refer [errors-div]]))

(def regexps
  {"us" #"^(\d\d)/(\d\d)/(\d\d\d\d)$"
   "iso" #"^(\d\d\d\d)-(\d\d)-(\d\d)$"})

(defn parse [fmt x]
  (when x
    (if (= "us" fmt)
      (when-let [[_ m d y] (re-matches (get regexps "us") x)]
        (str d "-" m "-" y))
      (when (re-matches (get regexps "iso") x)
        x))))

(defn unparse [fmt x]
  (when x
    (if (= "us" fmt)
      (when-let [[_ y m d] (re-matches (get regexps "iso") x)]
        (str m "/" d "/" y))
      x)))

(defn date-input [opts]
  (let [fmt (or (:format opts) "iso")
        state (r/atom {:lastValue (:value opts) :value (unparse fmt (:value opts))})
        my-onchange (fn [event on-change]
                      (let [v (.. event -target -value)]
                        (swap! state assoc :value v)
                        (when-let [vv (parse fmt v)]
                          (println "on-change" vv)
                          (on-change vv))))]
    (r/create-class
     {:component-did-mount
      (fn [this] (.log js/console "Mount"))

      :component-will-receive-props
      (fn [_ nextprops]
        (when-let [{v :value} (second nextprops)]
          (when-not (= v (:lastValue @state))
            (swap! state assoc :value (unparse fmt v) :lastValue v))))

      :reagent-render
      (fn [{:keys [value on-change errors] :as props}]
        [:div
         [:input (merge (dissoc props :errors)
                        {:type "text" 
                         :on-change #(my-onchange % on-change)
                         :value (:value @state)})]
         [errors-div errors]])})))
