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
 :re-form/input-errors
 (fn [db [_ form-name path]]
   (let [cur (reagent/cursor db [:re-form form-name :errors path])]
     (reaction @cur))))

(rf/reg-sub-raw
 :re-form/form-value
 (fn [db [_ form-name]]
   (reaction @(reagent/cursor db [:re-form form-name :value]))))

(rf/reg-sub-raw
 :re-form/form-errors
 (fn [db [_ form-name]]
   (reaction @(reagent/cursor db [:re-form form-name :errors]))))

(rf/reg-event-db
 :re-form/init
 (fn [db [_ manifest]]
   (assoc-in db [:re-form (:name manifest)] manifest)))

(rf/reg-event-db
 :re-form/deinit
 (fn [db [_ manifest]]
   (update db :re-form  dissoc (:name manifest))))

(rf/reg-event-db
 :re-form/input-changed
 (fn [db [_ form-name input-path v]] (shared/on-change db form-name input-path v)))

(rf/reg-event-db
 :re-form/validation-errors
 (fn [db [_ form-name errors]]
   (shared/put-validation-errors db form-name errors)))


(defn errors-for [{form-name :form path :path :as props}]
  (let [errors (rf/subscribe [:re-form/input-errors form-name path])]
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
    :component-will-unmount
    (fn [] (deinit props))
    :display-name (str (:name props))
    :reagent-render
    (fn [{:keys [name value] :as props} & body]
      [ctx/set-context {:form-name name :base-path []}
       (into [:div.re-form] body)] )}))

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

(defn binded-field [props]
  (let [my-on-change (fn binded-input-onchange [form-name path v on-change]
                       (rf/dispatch [:re-form/input-changed form-name path v])
                       (and on-change (on-change v)))

        get-props #(second (.-argv (.-props %)))

        state (reagent/atom {})
        run-validators
        (fn []
          (let [{form-name :form path :path validators :validators value :value} @state]
            (validate-and-update-errors form-name path validators @value)))

        unbind-input
        (fn [form-name path]
          (when (:value @state)
            (remove-watch (:value @state) :binded-field))
          (rf/dispatch [:re-form/validation-errors form-name {path []}])
          (rf/dispatch [:re-form/input-changed form-name path nil]))

        update-binding
        (fn [{new-form :form new-path :path validators :validators}]
          (swap! state (fn [{val :value err :errors path :path form :form :as curr-state}]
                         (if (not (and (= new-path path) (= new-form form)))
                           (do
                             (unbind-input form path)
                             (let [new-val-subscr (rf/subscribe [:re-form/input-value new-form new-path])]
                               ;; initial validation (do it before we're attaching a watcher)
                               (validate-and-update-errors new-form new-path validators @new-val-subscr)

                               ;; future validations
                               (add-watch new-val-subscr :binded-field run-validators)

                               {:form new-form
                                :path new-path
                                :value new-val-subscr
                                :validators validators
                                :errors (rf/subscribe [:re-form/input-errors new-form new-path])}))
                           (assoc curr-state :validators validators)))))]

    (reagent/create-class
     {:display-name :binded-field
      :component-will-mount
      (fn [this]
        (update-binding (get-props this)))

      :component-will-unmount
      (fn [this]
        (let [{form-name :form path :path} (get-props this)]
          (unbind-input form-name path)))

      :component-will-receive-props
      (fn [this new-props]
        (update-binding (second new-props)))

      :reagent-render
      (fn [{on-change :on-change form-name :form path :path :as props}]
        [:div.re-form-field
         [(:input props)
          (merge (dissoc props :form :path :input :validators)
                 {:value @(:value @state)
                  :on-change #(my-on-change form-name path % on-change)})]

         [errors-for {:form form-name :path path}]])})))

(defn validated-input [{form-name :form :keys [validators path on-change value] :as props}]
  (let [my-on-change (fn validated-input-onchange [v on-change]
                       (validate-and-update-errors form-name path validators v)
                       (and on-change (on-change v)))]

    ;; initial validation
    (my-on-change value on-change)

    (fn [{:keys [on-change] :as props}]
      [:div.re-form-field
       [binded-field (assoc (dissoc props :validators) :on-change #(my-on-change % on-change))]
       [errors-for {:form form-name :path path}]])))

(defn field [props]
  [ctx/get-context props binded-field])

(defn input [& args]
  (.error js/console "Warning! `re-form.input` is deprecated. Please use `re-form.field` instead. Caused by " (into ["re-form.input"] args))
  (fn [& args]
    (into [field] args)))

(defn form-data [{form-name :form}]
  (let [data (rf/subscribe [:re-form/form-value form-name])
        errors (rf/subscribe [:re-form/form-errors form-name])]
    (fn [props]
      [:div
       [:pre (with-out-str (cljs.pprint/pprint @data))]
       (when @errors
         [:pre (with-out-str (cljs.pprint/pprint @errors))])])))

(def form-style
  [:*
   inputs/radio-input-style
   inputs/file-upload-style
   inputs/button-select-style
   inputs/textarea-style
   inputs/calendar-style
   inputs/switchbox-style
   inputs/select-input-style
   inputs/checkbox-group-style
   inputs/select-xhr-style])
