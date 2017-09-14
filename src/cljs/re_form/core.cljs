(ns re-form.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [garden.core :as garden]
            [clojure.string :as str]
            [cljs.pprint]
            [re-form.shared :as shared]
            [re-form.validators :as validators]
            [re-form.inputs :as inputs]))

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
 :re-form/state
 (fn [db [_ opts]]
   (let [path (shared/state-path opts)
         cur (reagent/cursor db path)]
     (reaction @cur))))

(rf/reg-event-db
 :re-form/init
 (fn [db [_ manifest]]
   (assoc-in db [:re-form (:name manifest)] manifest)))


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

;; what's this?
;; (rf/reg-event-db
;;  :re-form/state
;;  (fn [db [_ opts v]]
;;    (let [spath (shared/state-path opts)]
;;      (update-in db spath (fn [o] (merge (or o {}) v))))))\

(defn init [{:keys [name validate-fn] :as form}]
  (rf/dispatch [:re-form/init (dissoc form :validate-fn)])
  (add-watch (rf/subscribe [:re-form/form-value name]) :re-form-validator
             (fn [_ _ _ v]
               (rf/dispatch [:re-form/validation-errors name (or (and validate-fn (validate-fn v)) {})]))))

(defn binded-input [{form-name :form path :path :as props}]
  (let [value (rf/subscribe [:re-form/input-value form-name path])
        my-on-change (fn [v on-change]
                       (rf/dispatch [:re-form/input-changed form-name path v])
                       (and on-change (on-change v)))]
    (fn [{on-change :on-change :as props}]
      [(:input props)
       (merge (dissoc props :form :path :input)
              {:value @value :on-change #(my-on-change % on-change)})])))

(defn input-with-errors [{form-name :form :keys [validators path] :as props} child-component]
  (let [form-value (rf/subscribe [:re-form/form-value form-name])]
    (fn [props]
      [:div.input
       [child-component (dissoc props :validators)]
       [errors-for {:form form-name :path path}]])))

(defn input [props]
  [input-with-errors props binded-input])

(defn form-data [{form-name :form}]
  (let [data (rf/subscribe [:re-form/form-value form-name])]
    (fn [props]
      [:pre [:Code (with-out-str (cljs.pprint/pprint @data))]])))

(def form-style
  [:*
   inputs/radio-input-style
   inputs/button-select-style
   inputs/textarea-style
   inputs/calendar-style
   inputs/switchbox-style])
