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
   (let [cur (reagent/cursor db (into [:re-form form-name :state] path))]
     (reaction @cur))))

(rf/reg-event-db
 :re-form/input-changed
 (fn [db [_ form-name input-path v]] (shared/on-change db form-name input-path v)))


(defn errors-for [{{form-name :name} :form path :path :as props}]
  (let [errors (rf/subscribe [:re-form/errors-for form-name path])]
    (fn [props]
      [:div.errors
       (doall (for [[k v] (or @errors [])] [:span.error {:key k} v]))])))

;; what's this?
;; (rf/reg-event-db
;;  :re-form/state
;;  (fn [db [_ opts v]]
;;    (let [spath (shared/state-path opts)]
;;      (update-in db spath (fn [o] (merge (or o {}) v))))))

(defn input [{form-name :form path :path :as props}]
  (let [value (rf/subscribe [:re-form/input-value form-name path])
        on-change #(rf/dispatch [:re-form/input-changed form-name path %])]
    (fn [props]
      [(:input props)
       (merge (dissoc props :form :path :input)
              {:value @value :on-change on-change})])))

(defn errors [{pth :path f :validator} cmp]
  (let [err (rf/subscribe [:re-form/error pth f])]
    (fn [props cmp]
      [:div {:class (when @err "has-danger")}
       cmp [:br] "Errors:" (pr-str @err)])))

(defn form-data [{form-name :form}]
  (let [data (rf/subscribe [:re-form/form-value form-name])]
    (fn [props]
      [:pre [:Code (with-out-str (cljs.pprint/pprint @data))]])))

(def form-style
  [:*
   inputs/radio-input-style
   inputs/textarea-style
   inputs/calendar-style
   inputs/switchbox-style])
