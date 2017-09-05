(ns re-form.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [garden.core :as garden]
            [clojure.string :as str]
            [re-form.select :as select]
            [re-form.switchbox :as switchbox]
            [re-form.shared :as shared]
            [re-form.validators :as validators]))


(rf/reg-sub-raw
 :re-form/data
 (fn [db [_ path]]
   (let [cur (reagent/cursor db path)]
     (reaction @cur))))

(rf/reg-sub-raw
 :re-form/value
 (fn [db [_ opts]]
   (let [path (shared/input-path opts)
         cur (reagent/cursor db path)]
     (reaction @cur))))

(rf/reg-sub-raw
 :re-form/state
 (fn [db [_ opts]]
   (let [path (shared/state-path opts)
         cur (reagent/cursor db path)]
     (reaction @cur))))


(rf/reg-event-db
 :re-form/init
 (fn [db [_ manifest]]
   (assoc-in db (:path manifest) manifest)))


(rf/reg-sub-raw
 :re-form/errors-for
 (fn [db [_ opts]]
   (let [cur (reagent/cursor db (shared/errors-path opts))]
     (reaction @cur))))

(defn errors-for [opts]
  (let [errs (rf/subscribe [:re-form/errors-for opts])]
    (fn [props]
      (when @errs [:div.errors
                   (doall (for [[k v] @errs] [:span.error {:key k} v]))]))))

(rf/reg-event-db
 :re-form/update
 (fn [db [_ opts v]] (shared/on-change db opts v)))

(rf/reg-event-db
 :re-form/state
 (fn [db [_ opts v]]
   (let [spath (shared/state-path opts)]
     (update-in db spath (fn [o] (merge (or o {}) v))))))


(defn input [{{pth :path :as frm} :form  nm :name :as opts}]
  (let [v (rf/subscribe [:re-form/data (shared/input-path opts)])
        on-change (fn [ev] (rf/dispatch [:re-form/update opts (.. ev -target -value)]))]
    (fn [props]
      [:input
       (merge (dissoc opts :form :path)
              {:type (or (:type opts) "text") :value @v  :on-change on-change})])))

(defn errors [{pth :path f :validator} cmp]
  (let [err (rf/subscribe [:re-form/error pth f])]
    (fn [props cmp]
      [:div {:class (when @err "has-danger")}
       cmp [:br] "Errors:" (pr-str @err)])))

(defn re-input [{pth :path}]
  [:b "remove me"])

(defn re-collection [{pth :path} input]
  (.log js/console "obsolete"))

(def re-select select/re-select)
(def re-radio-group select/re-radio-group)
(def re-radio-buttons select/re-radio-buttons)
(def re-switch-box switchbox/switch-box)

(def form-style
  [:*
   select/re-select-style
   switchbox/re-switch-box-style
   select/re-radio-group-style
   select/re-radio-buttons-style])
