(ns re-form.core
  (:require-macros [reagent.ratom :refer [reaction run!]]
                   [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [garden.core :as garden]
            [clojure.string :as str]
            [cljs.core.async.impl.channels :as channels-impl]
            [cljs.core.async :refer [<! close!]]
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
 :re-form/input-flags
 (fn [db [_ form-name path]]
   (let [cur (reagent/cursor db [:re-form form-name :flags path])]
     (reaction @cur))))

(rf/reg-sub-raw
 :re-form/input-errors
 (fn [db [_ form-name paths]]
   (let [cur (reagent/cursor db [:re-form form-name :errors])
         path-set (set paths)]
     (reaction (vec (mapcat second (filterv (fn [[k _]] (path-set k)) @cur)))))))

(rf/reg-sub-raw
 :re-form/form-value
 (fn [db [_ form-name]]
   ;; Related to this issues:
   ;; https://github.com/reagent-project/reagent/issues/244
   ;; https://github.com/reagent-project/reagent/issues/116
   (run! @(reagent/cursor db [:re-form form-name :value]))))

(rf/reg-sub-raw
 :re-form/is-form-submitting
 (fn [db [_ form-name]]
   (reaction @(reagent/cursor db [:re-form form-name :submitting]))))

(rf/reg-sub-raw
 :re-form/form-errors
 (fn [db [_ form-name]]
   (reaction @(reagent/cursor db [:re-form form-name :errors]))))

(rf/reg-event-db
 :re-form/init
 (fn [db [_ manifest]]
   (assoc-in db [:re-form (:form-name manifest)] manifest)))

(rf/reg-event-db
 :re-form/deinit
 (fn [db [_ form-name]]
   (update db :re-form  dissoc form-name)))

(rf/reg-event-db
 :re-form/input-changed
 (fn [db [_ form-name input-path v]]
   (shared/on-input-changed db form-name input-path v)))

(rf/reg-event-db
 :re-form/set-input-flags
 (fn [db [_ form-name input-path flags]]
   (update-in db [:re-form form-name :flags input-path] merge flags)))

(rf/reg-event-db
 :re-form/input-removed
 (fn [db [_ form-name input-path v]]
   (shared/on-input-removed db form-name input-path)))

(rf/reg-event-db
 :re-form/validation-errors
 (fn [db [_ form-name errors]]
   (shared/put-validation-errors db form-name errors)))

(rf/reg-event-db
 :re-form/add-validation-errors
 (fn [db [_ form-name path errors]]
   (shared/add-validation-errors db form-name path errors)))

(rf/reg-event-db
 :re-form/start-submitting
 (fn [db [_ form-name]]
   (assoc-in db [:re-form form-name :submitting] true)))

(defn process-class [class]
  (if (keyword? class)
    (str/join " " (str/split (name class) \.))
    class))

(defn init [{:keys [form-name validate-fn value] :as form}]
  (when-not form-name
    (throw (js/Error. (str "No form name is provided for `re-form.form` component, props are: " (pr-str form)))))

  (rf/dispatch [:re-form/init (dissoc form :validate-fn)])

  (when validate-fn
    (add-watch (rf/subscribe [:re-form/form-value form-name]) :re-form-validator
               (fn [_ _ _ v]
                 (rf/dispatch [:re-form/validation-errors form-name (or (validate-fn v) {})])))))

(defn deinit [form-name]
  (rf/dispatch [:re-form/deinit form-name]))

(defn form [props & body]
  (reagent/create-class
   {:component-will-mount
    (fn [] (init props))

    :component-will-unmount
    (fn [] (deinit (:form-name props)))

    :display-name (str (:form-name props))

    :component-will-receive-props
    (fn [_ coll]
      (let [new-props (second coll)]
        (deinit (:form-name new-props))
        (init new-props)))

    :reagent-render
    (fn [{:keys [form-name value class] :as props} & body]
      [ctx/set-context {:form-name form-name :base-path []}
       (into [:div.re-form
              (merge {:class (when class (process-class class))}
                     (select-keys props [:on-key-down :auto-focus :tab-index]))]
             body)])}))

(defn- validate-and-update-errors [form-name path validators val]
  (when-not (empty? validators)
    (let [errors
          (reduce (fn [acc validator]
                    (if-let [res (validator val path)]
                      (if (or (vector? res) (list? res))
                        (into acc res)

                        (if (= (type res) channels-impl/ManyToManyChannel)
                          (do
                            (go
                              (let [[e e-path] (<! res)]
                                (rf/dispatch [:re-form/add-validation-errors form-name e-path e])))
                            acc)

                          (conj acc res)))
                      acc))
                  []
                  validators)]
      (rf/dispatch [:re-form/validation-errors form-name {path errors}]))))

(defn binded-field [props]
  (let [my-on-change
        (fn binded-input-onchange [form-name path v on-change]
          (rf/dispatch [:re-form/input-changed form-name path v])
          (and on-change (on-change v)))

        my-on-blur
        (fn binded-input-onblur [form-name path on-blur]
          (rf/dispatch [:re-form/set-input-flags form-name path {:touched true}])
          (and on-blur (on-blur)))

        get-props #(second (aget (aget % "props") "argv") )

        state (reagent/atom {})
        run-validators
        (fn []
          (let [{form-name :form-name path :path validators :validators value :value} @state]
            (validate-and-update-errors form-name path validators @value)))

        unbind-input
        (fn [form-name path]
          (when (:value @state)
            (remove-watch (:value @state) :binded-field))
          (rf/dispatch [:re-form/input-removed form-name path]))

        update-binding
        (fn [{new-form :form-name new-path :path validators :validators}]
          (swap! state (fn [{val :value err :errors path :path form :form-name :as curr-state}]
                         (if (not (and (= new-path path) (= new-form form)))
                           (do
                             (when (and form path)
                               (unbind-input form path))
                             (let [new-val-subscr (rf/subscribe [:re-form/input-value new-form new-path])]
                               ;; initial validation (do it before we're attaching a watcher)
                               (validate-and-update-errors new-form new-path validators @new-val-subscr)

                               ;; future validations
                               (add-watch new-val-subscr :binded-field run-validators)

                               {:form-name new-form
                                :path new-path
                                :value new-val-subscr
                                :validators validators}))
                           (assoc curr-state :validators validators)))))]

    (reagent/create-class
     {:display-name :binded-field
      :component-will-mount
      (fn [this]
        (update-binding (get-props this)))

      :component-will-unmount
      (fn [this]
        (let [{form-name :form-name path :path} (get-props this)]
          (unbind-input form-name path)))

      :component-will-receive-props
      (fn [this new-props]
        (update-binding (second new-props)))

      :reagent-render
      (fn [{:keys [on-change form-name path on-blur error-paths] :as props}]
        (let [flags @(rf/subscribe [:re-form/input-flags form-name path])
              is-form-submitting @(rf/subscribe [:re-form/is-form-submitting form-name])
              errors @(rf/subscribe [:re-form/input-errors form-name (into [path] error-paths)])]
          [:div.re-form-field {:class (->> flags
                                           (filter second)
                                           (map #(name (first %)))
                                           (str/join " "))}
           [(:input props)
            (merge (dissoc props :form-name :path :input :validators)
                   {:value @(:value @state)
                    :on-change #(my-on-change form-name path % on-change)
                    :on-blur #(my-on-blur form-name path on-blur)
                    :errors (if (or (:touched flags) is-form-submitting)
                              errors [])})]]))})))

(defn field [props]
  [ctx/get-context props binded-field])

(defn- error* [{:keys [form-name path error-paths] :as props}]
  (let [flags @(rf/subscribe [:re-form/input-flags form-name path])
        is-form-submitting @(rf/subscribe [:re-form/is-form-submitting form-name])
        errors @(rf/subscribe [:re-form/input-errors form-name (into [path] error-paths)])]
    [(:view props)
     (merge (dissoc props :form-name :path :input :validators)
            {:errors (if (or (:touched flags) is-form-submitting)
                       errors [])})]))

(defn error [props]
  [ctx/get-context props error*])

(defn input [& args]
  (.error js/console "Warning! `re-form.input` is deprecated. Please use `re-form.field` instead. Caused by " (into ["re-form.input"] args))
  (fn [& args]
    (into [field] args)))

(defn form-data [{form-name :form-name}]
  (let [data (rf/subscribe [:re-form/form-value form-name])
        errors (rf/subscribe [:re-form/form-errors form-name])]
    (fn [props]
      [:div
       [:pre (with-out-str (cljs.pprint/pprint @data))]
       (when @errors
         [:pre (with-out-str (cljs.pprint/pprint @errors))])])))

(def default-base-consts
  {:h 16
   :h2 24
   :h3 38
   :selection-bg-color "#007bff"
   :hover-bg-color "#f1f1f1"
   :border "1px solid #ddd"})

(def input-style-fns
  [inputs/radio-input-style
   inputs/file-upload-style
   inputs/button-select-style
   inputs/textarea-style
   inputs/calendar-style
   inputs/switchbox-style
   inputs/text-input-style
   inputs/select-input-style
   inputs/checkbox-group-style
   inputs/select-xhr-style
   inputs/codemirror-style])

(defn form-style-fn [s]
  (into [:*] (map #(% s) input-style-fns)))

(def form-style (form-style-fn default-base-consts))
