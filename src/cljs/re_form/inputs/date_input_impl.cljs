(ns re-form.inputs.date-input-impl
  (:require [reagent.core :as r]
            [re-form.inputs.common :refer [errors-div]]))

(def regexps
  {"us" #"^(\d\d)/(\d\d)/(\d\d\d\d)$"
   "iso" #"^(\d\d\d\d)-(\d\d)-(\d\d)$"})

(def formats
  {"us" "mm/dd/yyyy"
   "iso" "yyyy-mm-dd"})

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
        my-on-focus (fn []
                      (when (:show-error @state)
                        (swap! state #(-> %
                                          (dissoc :value)
                                          (assoc :show-error false)))))
        my-on-blur (fn [event on-blur]
                     (swap! state assoc :show-error true)
                     (on-blur))
        my-onchange (fn [event on-change]
                      (let [v (.. event -target -value)]
                        (swap! state assoc :value v)
                        (if-let [vv (parse fmt v)]
                          (on-change vv)
                          (do
                            (swap! state assoc :errors
                                   (str "The format is: " (formats fmt)))))))]
    (r/create-class
     {#_(:component-did-mount
         (fn [this] (.log js/console "Mount")))

      :component-will-receive-props
      (fn [_ nextprops]
        (when-let [{v :value} (second nextprops)]
          (when-not (= v (:lastValue @state))
            (swap! state assoc :value (unparse fmt v) :lastValue v))))

      :reagent-render
      (fn [{:keys [value on-change errors on-blur err-classes] :as props}]
        [:div
         [:input (merge (dissoc props :errors)
                        {:type "text" 
                         :placeholder (formats fmt)
                         :on-blur #(my-on-blur % on-blur)
                         :on-focus #(my-on-focus)
                         :on-change #(my-onchange % on-change)
                         :value (:value @state)})]
         [:div {:class (apply str (interpose "." err-classes))}
          (apply str
                 (interpose "\n" (conj errors
                                       (when (:show-error @state)
                                         (:errors @state)))))]])})))
