(ns re-form.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [garden.core :as garden]
            [clojure.string :as str]
            [re-form.select :as select]
            [re-form.switchbox :as switchbox]
            [re-form.validators :as validators]
            ))


(defn insert-by-path [m [k & ks :as path] value]
  (if ks
    (if (int? k)
      (assoc (or m []) k (insert-by-path (get m k) ks value))
      (assoc (or m {}) k (insert-by-path (get m k) ks value)))
    (if (int? k)
      (assoc (or m []) k value)
      (assoc (or m {}) k value))))

(rf/reg-event-db
 :re-form/change
 (fn [db [_ path value]]
   (insert-by-path db path value)))

(rf/reg-sub-raw
 :re-form/data
 (fn [db [_ path]]
   (let [cur (reagent/cursor db path)]
     (reaction @cur))))

(rf/reg-sub-raw
 :re-form/error
 (fn [db [_ path f]]
   (let [cur (reagent/cursor db path)]
     (reaction (f @cur)))))

(rf/reg-sub-raw
 :reform/data
 (fn [db [_ path]]
   (let [cur (reagent/cursor db path)]
     (reaction @cur))))

(rf/reg-event-db
 :re-form/manifest
 (fn [db [_ manifest]]
   (assoc-in db [:forms (:name manifest)] manifest)))

(rf/reg-event-db
 :re-form/on-change
 (fn [db [_ pth v]]
   (let [manifest (get-in db pth)
         db (if-let [valid (get validators/validators (:validator manifest))]
              (re-form.core/insert-by-path db (conj pth :error) (valid v))
              db)]
     (re-form.core/insert-by-path db (conj pth :value) v))))

(rf/reg-sub
 :re-form/value
 (fn [db [_ pth]]
   (let [form (get-in db pth)]
     (clojure.walk/prewalk
      (fn [x]
        (cond
          (and (map? x) (:items x)) (:items x)
          (and (map? x) (:fields x)) (:fields x)
          (and (map? x) (some? (:value x))) (:value x)
          :else x))
      form))))


(defn input [{pth :path :as opts}]
  (let [sub (rf/subscribe [:re-form/data pth])
        on-change (fn [ev] (rf/dispatch [:re-form/change pth (.. ev -target -value)]))]
    (fn [props]
      [:input.form-control (merge (dissoc opts :path)
                                  {:type "text" :value @sub  :on-change on-change})])))

(defn errors [{pth :path f :validator} cmp]
  (let [err (rf/subscribe [:re-form/error pth f])]
    (fn [props cmp]
      [:div {:class (when @err "has-danger")}
       cmp [:br] "Errors:" (pr-str @err)])))

(defn re-input [{pth :path}]
  (let [input-path pth
        sub (rf/subscribe [:re-form/data input-path])
        on-change (fn [ev] (rf/dispatch [:re-form/on-change input-path (.. ev -target -value)]))]
    (fn [props]
      [:input.form-control {:type "text" :value (:value @sub)  :on-change on-change}])))

(rf/reg-event-db
 :re-form/add-item
 (fn [db [_ path metadata]]
   (update-in db path conj metadata)))

(rf/reg-event-db
 :re-form/remove-item
 (fn [db [_ path i]]
   (update-in db path (fn [v] (into (subvec v 0 i) (subvec v (inc i)))))))


(defn re-collection [{pth :path} input]
  (let [sub (rf/subscribe [:re-form/data pth])
        add-item (fn [] (rf/dispatch [:re-form/add-item (conj pth :items) (:item @sub)]))]
    (fn [props]
      [:div 
       (for [i (range (count (:items @sub)))]
         [:div {:key i} [input {:path (into pth [:items i])}]
          [:button {:on-click #(rf/dispatch [:re-form/remove-item (conj pth :items) i])} "x"]])
       [:br]
       [:button {:on-click add-item} "+"]])))

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
