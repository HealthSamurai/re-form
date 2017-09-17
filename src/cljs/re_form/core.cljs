(ns re-form.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [garden.core :as garden]
            [clojure.string :as str]
            [cljs.pprint]
            [re-form.shared :as shared]
            [re-form.validators :as validators]
            [re-form.inputs :as inputs]
            [re-form.context :as ctx]))

(rf/reg-sub-raw
 :re-form/input-value
 (fn [db [_ form-name path]]
   (let [cur (reagent/cursor db (into [:re-form form-name :value] path))]
     (reaction @cur))))

(rf/reg-sub-raw
 :re-form/form-value
 (fn [db [_ form-name]]
   (reaction @(reagent/cursor db [:re-form form-name :value]))))

(rf/reg-sub-raw
 :re-form/form-errors
 (fn [db [_ form-name]]
   (reaction @(reagent/cursor db [:re-form form-name :errors]))))

(rf/reg-sub-raw
 :re-form/state
 (fn [db [_ opts]]
   (let [path (shared/state-path opts)
         cur (reagent/cursor db path)]
     (reaction @cur))))

(rf/reg-event-db
 :re-form/init
 (fn [db [_ manifest]]
   (assoc-in db [:re-form (:name manifest)] manifest)))

(rf/reg-event-db
 :re-form/deinit
 (fn [db [_ manifest]]
   (update db :re-form  dissoc (:name manifest))))

(rf/reg-sub-raw
 :re-form/errors-for
 (fn [db [_ form-name path]]
   (let [cur (reagent/cursor db [:re-form form-name :errors path])]
     (reaction @cur))))

(rf/reg-event-db
 :re-form/input-changed
 (fn [db [_ form-name input-path v]] (shared/on-change db form-name input-path v)))

(rf/reg-event-db
 :re-form/validation-errors
 (fn [db [_ form-name errors]]
   (shared/put-validation-errors db form-name errors)))


(defn errors-for [{form-name :form path :path :as props}]
  (let [errors (rf/subscribe [:re-form/errors-for form-name path])]
    (fn [props]
      [:div.errors
       (doall (map-indexed
               (fn [idx e] [:span.error {:key idx} e])
               (or @errors [])))])))

(defn init [{:keys [name validate-fn value] :as form}]
  (rf/dispatch [:re-form/init (dissoc form :validate-fn)])

  (when validate-fn
    (add-watch (rf/subscribe [:re-form/form-value name]) :re-form-validator
               (fn [_ _ _ v]
                 (rf/dispatch [:re-form/validation-errors name (or (validate-fn v) {})])))))

(defn deinit [form-name]
  (rf/dispatch [:re-form/deinit form-name]))

(defn form [props & body]
  (reagent/create-class
   {:component-will-mount
    (fn [] (init props))
    :component-will-receive-props
    (fn [this props]
      (let [cur-value (nth (.. this -props -argv ) 1)
            new-value (nth props 1)]
        (.log js/console "NEW" new-value)
        (.log js/console "CUR" new-value)
        (when-not (= new-value cur-value)
          (.log js/console "UPDATE")
          (init new-value))))
    :component-will-unmount
    (fn [] (deinit props))
    :display-name (str (:name props))
    :reagent-render
    (fn [{:keys [name value] :as props} & body]
      [ctx/set-context {:form-name name :base-path []}
       (into [:div.re-form] body)] )}))

(defn binded-input [{form-name :form path :path :as props}]
  (let [value (rf/subscribe [:re-form/input-value form-name path])
        my-on-change (fn binded-input-onchange [v on-change]
                       (rf/dispatch [:re-form/input-changed form-name path v])
                       (and on-change (on-change v)))]
    (fn [{on-change :on-change :as props}]
      [(:input props)
       (merge (dissoc props :form :path :input)
              {:value @value :on-change #(my-on-change % on-change)})])))

(defn- validate-and-update-errors [form-name path validators val]
  (when-not (empty? validators)
    (let [errors (reduce (fn [acc validator]
                           (when-let [res (validator val)]
                             (if (or (vector? res) (list? res))
                               (into acc res)
                               (conj acc res))))
                         []
                         validators)]
      (rf/dispatch [:re-form/validation-errors form-name {path errors}]))))

(defn validated-input [{form-name :form :keys [validators path on-change value] :as props}]
  (let [form-value (rf/subscribe [:re-form/form-value form-name])
        my-on-change (fn validated-input-onchange [v on-change]
                       (validate-and-update-errors form-name path validators v)
                       (and on-change (on-change v)))]

    ;; initial validation
    (my-on-change value on-change)

    (fn [{:keys [on-change] :as props}]
      [:div.input
       [binded-input (assoc (dissoc props :validators) :on-change #(my-on-change % on-change))]
       [errors-for {:form form-name :path path}]])))

(defn input [props]
  [ctx/get-context props validated-input])

(defn form-data [{form-name :form}]
  (let [data (rf/subscribe [:re-form/form-value form-name])]
    (fn [props]
      [:pre (with-out-str (cljs.pprint/pprint @data))])))

(def form-style
  [:*
   inputs/radio-input-style
   inputs/file-upload-style
   inputs/button-select-style
   inputs/textarea-style
   inputs/calendar-style
   inputs/switchbox-style
   inputs/select-input-style
   inputs/checkbox-group-style])
