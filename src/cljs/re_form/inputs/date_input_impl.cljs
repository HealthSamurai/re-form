(ns re-form.inputs.date-input-impl
  (:require [reagent.core :as r]
            [goog.string :as gstring]
            [goog.string.format]
            [garden.units :as u]
            [re-form.inputs.calendar-impl :refer [re-calendar]]
            [re-form.inputs.common :as cmn]
            [clojure.string :as str]
            [cljsjs.moment-timezone]))

(defn date-input-style
  [{:keys [w h h2 h3 selection-bg-color gray-color hover-bg-color border error-border]}]
  [:*
   [:.date-chevrons
    {:display :inline-block
     :user-select :none
     :font-size (u/px h2)
     :cursor :pointer
     :line-height (u/px* h2 1.5)}]
   [:.dropdown-input
    [:.calendar-dropdown
     {:position :absolute
      :z-index 1
      :display :flex
      :justify-content :center
      :width (u/px 238)
      :border border
      :background-color :white}
     [:&.chevron-offset
      {:margin-left (u/px h2)}]
     [:.clickable {:cursor :pointer}]]]
   [:.date-input
    {:position :relative
     :border border
     :display :inline-flex
     :align-items :center
     :border-radius (u/px 2)}
    [:&.error
     {:border error-border}]
    [:i.material-icons {:display :inline-block
                        :font-size (u/px h2)
                        :vertical-align :middle
                        :color gray-color
                        :padding [[0 (u/px w)]]}]
    [:input.re-input {:display :inline-block
                      :border :none
                      :padding-left 0
                      :width (u/px- 244 h2 (* 3 w))}
     [:&:focus
      {:outline :none}]]]])

(def simple-regex #"^(\d\d)(\d\d)(\d\d\d\d)$")
(def formats
  {
   "dd.mm.yyyy" {:regex #"^(\d\d)\.(\d\d)\.(\d\d\d\d)$"
                 :simple-regex #"^(\d\d)(\d\d)$"
                 :placeholder "dd.mm.yyyy"
                 :delimiter "."
                 :groups [:d :m :y]}
   "dd-mm-yyyy" {:regex #"^(\d\d)-(\d\d)-(\d\d\d\d)$"
                 :placeholder "dd-mm-yyyy"
                 :delimiter "-"
                 :groups [:d :m :y]}
   "dd/mm/yyyy" {:regex #"^(\d\d)/(\d\d)/(\d\d\d\d)$"
                 :delimiter "/"
                 :placeholder "dd/mm/yyyy"
                 :groups [:d :m :y]}
   "mm/dd/yyyy" {:regex #"^(\d\d)/(\d\d)/(\d\d\d\d)$"
                 :delimiter "/"
                 :placeholder "mm/dd/yyyy"
                 :groups [:m :d :y]}
   "iso"        {:regex #"^(\d\d\d\d)-(\d\d)-(\d\d)$"
                 :delimiter "-"
                 :placeholder "yyyy-mm-dd"
                 :groups [:y :m :d]}
   "us"         {:regex #"^(\d\d)/(\d\d)/(\d\d\d\d)$"
                 :delimiter "/"
                 :placeholder "mm/dd/yyyy"
                 :groups [:m :d :y]}
   "yyyy-mm-dd" {:regex #"^(\d\d\d\d)-(\d\d)-(\d\d)$"
                 :delimiter "-"
                 :placeholder "yyyy-mm-dd"
                 :groups [:y :m :d]}})

(defn to-utc [timezone local-datetime]
  (.. js/moment (tz local-datetime timezone) utc format))

(defn to-local [timezone utc-datetime]
  (let [converted (.. js/moment (tz utc-datetime "utc") (tz timezone) format)]
    (subs converted 0 19)))

(defn zip-date [names groups]
  (apply hash-map (interleave names groups)))

(defn date-parse [fmt x]
  (when x
    (if (empty? x)
      x
      (when-let [f (formats fmt)]
        (when-let [[_ & groups] (or (re-matches (:regex f) x)
                                    (re-matches simple-regex x))]
          (let [date-hm (zip-date (:groups f) groups)]
            (str (:y date-hm) "-" (:m date-hm) "-" (:d date-hm))))))))

(defn date-unparse [fmt x]
  (when x
    (when-let [f (formats "iso")]
      (when-let [[_ & groups] (re-matches (:regex f) x)]
        (let [date-hm (zip-date (:groups f) groups)]
          (when-let [fmt-to (get formats fmt)]
            (->> (mapv #(get date-hm %) (:groups fmt-to))
                 (str/join (:delimiter fmt-to)))))))))

(defn f-iso [f x]
  (let [date-obj (js/Date. x)]
    (.setUTCDate date-obj (f (.getUTCDate date-obj)))
    (gstring/format "%04d-%02d-%02d"
                    (.getUTCFullYear date-obj)
                    (inc (.getUTCMonth date-obj))
                    (.getUTCDate date-obj))))

(def inc-iso (partial f-iso inc))
(def dec-iso (partial f-iso dec))

(defn date-input [opts]
  (let [fmt (or (:format opts) "iso")
        fmt-obj (get formats fmt)
        state (r/atom {:lastValue (:value opts) :value (date-unparse fmt (:value opts))})
        input-state (r/atom "blur")
        doc-click-listener (fn [e]
                             (when (and (not (cmn/has-ancestor (.-target e) (:node @state)))
                                        (:dropdown-visible @state))
                               (swap! state assoc :dropdown-visible false)))
        my-on-blur (fn [event on-blur]
                     (reset! input-state "blur")
                     (when (:errors @state)
                       (swap! state assoc :value (date-unparse fmt (:lastValue @state)))
                       (swap! state dissoc :errors))
                     (on-blur))
        will-recv-props (fn [v]
                          (when-not (= v (:lastValue @state))
                            (swap! state assoc :value (date-unparse fmt v) :lastValue v))
                          v)
        my-onchange (fn [event on-change]
                      (let [v (.. event -target -value)]
                        (swap! state assoc :value v)
                        (if-let [vv (date-parse fmt v)]
                          (do
                            (on-change vv)
                            (swap! state dissoc :errors))
                          (swap! state assoc :errors
                                 (str "The format is: " (:placeholder fmt-obj))))))]
    (r/create-class
     {
      :component-did-mount
      (fn [this]
        (swap! state assoc :node (r/dom-node this))
        (.addEventListener js/document "click" doc-click-listener))

      :component-will-unmount
      #(.removeEventListener js/document "click" doc-click-listener)

      :component-will-receive-props
      (fn [_ nextprops]
        (when-let [{v :value} (second nextprops)]
          (will-recv-props v)))

      :reagent-render
      (fn [{:keys [with-chevrons with-dropdown value on-change errors on-blur err-classes label placeholder] :as props}]
        [:div.dropdown-input
         (when with-chevrons
           [:div.date-chevrons
            {:on-click #(on-change (dec-iso value))}
            [:i.material-icons "chevron_left"]])
         [:div.date-input {:on-blur #(my-on-blur % (or on-blur identity))
                           :class (if label
                                    (str "input-field")
                                    (when-not (empty? errors) :error))}
          [:i.material-icons
           {:on-click
            #(let [parent-node (.. % -target -parentNode)
                   input (cmn/f-child parent-node "re-input")]
               (.focus input))} "today"]
          [:input.re-input (merge (dissoc props :errors :with-dropdown :format :with-chevrons :err-classes)
                                  (when label
                                    {:class (str "validate " (when-not (empty? errors) "invalid"))})
                                  {:type "text"
                                   :placeholder (if placeholder
                                                  (when (or (not (empty? errors))
                                                            (not (str/blank? value))
                                                            (= "focus" @input-state))
                                                    placeholder)
                                                  (:placeholder fmt-obj))
                                   :on-focus #(do
                                                (reset! input-state "focus")
                                                (swap! state assoc :dropdown-visible true))
                                   :on-change #(my-onchange % on-change)
                                   :value (:value @state)})]
          (when label
            [:label {:data-error (cond
                                   (seq errors)     (str/join " " errors)
                                   (:errors @state) (:errors @state))
                     :class (str (when (or (not (empty? errors))
                                           (not (str/blank? value))
                                           (= "focus" @input-state)) "active") " "
                                 (when (= "focus" @input-state) "focus"))} label])]
         (when with-chevrons
           [:div.date-chevrons
            {:on-click #(on-change (inc-iso value))}
            [:i.material-icons.date-chevrons "chevron_right"]])
         (when (and with-dropdown (:dropdown-visible @state))
           [:div.calendar-dropdown
            {:tab-index 0
             :class (when with-chevrons :chevron-offset)}
            [re-calendar
             {:value (date-parse fmt (:value @state))
              :on-change (comp #(swap! state assoc :dropdown-visible false)
                               on-change will-recv-props)}]])
         (when-not label
           [:div {:class (apply str (interpose "." err-classes))}
            (when-let [local-errors (:errors @state)]
              (str/join "\n" (conj errors local-errors)))])])})))

(defn iso-dt [date time] (gstring/format "%sT%s:00%s" date time ""))

#_(defn local-tz-offset []
    (let [local-tz (- (.getTimezoneOffset (js/Date.)))
          hours (Math/floor (/ local-tz 60))
          minutes (mod local-tz 60)]
      (gstring/format "%s%02d:%02d" (if (neg? hours) "-" "+")
                      (Math/abs hours) minutes)))

#_(defn iso-dt [date time & {:keys [with-local-tz]}]
    (gstring/format "%sT%s:00%s" date time (if with-local-tz
                                             (local-tz-offset) "")))


#_((defn parse-time [fmt x]
     )

   (defn unparse-time [fmt x]
     )

   (defn date-time-input [opts]
     (let [fmt (or (:format opts) "iso")
           fmt-obj (get formats fmt)
           state (r/atom {:lastValue (:value opts) :value (unparse-time fmt (:value opts))})
           my-on-blur (fn [event on-blur]
                        (when (:errors @state)
                          (swap! state assoc :value (unparse-time fmt (:lastValue @state)))
                          (swap! state dissoc :errors))
                        (on-blur))
           my-onchange (fn [event on-change]
                         (let [v (.. event -target -value)]
                           (swap! state assoc :value v)
                           (if-let [vv (parse-time fmt v)]
                             (do
                               (on-change vv)
                               (swap! state dissoc :errors))
                             (do
                               (swap! state assoc :errors
                                      (str "The format is: " (:placeholder fmt-obj)))))))]
       (r/create-class
        {:component-will-receive-props
         (fn [_ nextprops]
           (when-let [{v :value} (second nextprops)]
             (when-not (= v (:lastValue @state))
               (swap! state assoc :value (unparse-time fmt v) :lastValue v))))

         :reagent-render
         (fn [{:keys [value on-change errors on-blur err-classes] :as props}]
           [:div
            [:input.re-input (merge (dissoc props :errors)
                                    {:type "text"
                                     :placeholder (:placeholder fmt-obj)
                                     :on-blur #(my-on-blur % on-blur)
                                     :on-change #(my-onchange % on-change)
                                     :value (:value @state)})]
            [:div {:class (apply str (interpose "." err-classes))}
             (when-let [local-errors (:errors @state)]
               (str/join "\n" (conj errors local-errors)))]])}))))

(defn time-parse [fmt x]
  (when x
    (if (= fmt "12h")
      (when-let [[_ hp mp noonp] (re-matches #"(\d?\d):(\d\d) (\wM)" (str/upper-case x))]
        (let [h (as-> hp $
                  (js/parseInt $)
                  (if (= $ 12) 0 $)
                  (+ $ (if (= noonp "PM") 12 0)))
              m (js/parseInt mp)]
          (when (and (< h 24) (< m 60) (some #(= % noonp) ["AM" "PM"]))
            (str (gstring/format "%02d" h) ":" (gstring/format "%02d" m)))))
      (when-let [[_ h m] (re-matches #"(\d\d):(\d\d)" x)]
        (when (and (< h 24) (< m 60))
          x)))))

(defn time-unparse [fmt inp-x]
  (when-let [x (str/join (take 5 inp-x))]
    (if (= fmt "12h")
      (when-let [[_ hp m] (re-matches #"(\d\d):(\d\d)" x)]
        (let [hp (js/parseInt hp)
              m (js/parseInt m)
              [h noon] (if (>= hp 12) [(- hp 12) "PM"] [hp "AM"])
              h (if (= 0 h) 12 h)]
          (str (gstring/format "%02d" h) ":" (gstring/format "%02d" m) " " noon)))
      x)))

(defn time-input [opts]
  (let [fmt (or (:format opts) "24h")
        placeholder {"24h" "hh:mm" "12h" "hh:mm AM|PM"}
        state (r/atom {:lastValue (:value opts) :value (time-unparse fmt (:value opts))})
        my-on-blur (fn [event on-blur]
                     (when (:errors @state)
                       (swap! state assoc :value (time-unparse fmt (:lastValue @state)))
                       (swap! state dissoc :errors))
                     (on-blur))
        my-onchange (fn [event on-change]
                      (let [v (.. event -target -value)]
                        (swap! state assoc :value v)
                        (if-let [vv (time-parse fmt v)]
                          (do
                            (on-change vv)
                            (swap! state dissoc :errors))
                          (do
                            (swap! state assoc :errors
                                   (str "The format is: " (get placeholder fmt)))))))]
    (r/create-class
     {:component-will-receive-props
      (fn [_ nextprops]
        (when-let [{v :value} (second nextprops)]
          (when-not (= v (:lastValue @state))
            (swap! state assoc :value (time-unparse fmt v) :lastValue v))))

      :reagent-render
      (fn [{:keys [value on-change errors on-blur err-classes] :as props}]
        [:div
         [:div.date-input
          [:i.material-icons
           {:on-click
            #(let [parent-node (.. % -target -parentNode)
                   input (cmn/f-child parent-node "re-input")]
               (.focus input))} "schedule"]
          [:input.re-input (merge (dissoc props :errors)
                                  {:type "text"
                                   :placeholder (get placeholder fmt)
                                   :on-blur #(my-on-blur % on-blur)
                                   :on-change #(my-onchange % on-change)
                                   :value (:value @state)})]]
         [:div {:class (apply str (interpose "." err-classes))}
          (when-let [local-errors (:errors @state)]
            (str/join "\n" (conj errors local-errors)))]])})))

(defn date-time-unparse [date-fmt time-fmt x]
  (let [[date time] (str/split x #"T")
        unparsed-date (date-unparse date-fmt date)
        unparsed-time (time-unparse time-fmt time)]
    (when (and unparsed-date unparsed-time)
      (str unparsed-date " " unparsed-time))))

(defn date-time-parse [date-fmt time-fmt x]
  (let [[date time] (str/split x #" " 2)
        parsed-date (date-parse date-fmt date)
        parsed-time (time-parse time-fmt time)]
    (when (and parsed-date parsed-time)
      (iso-dt parsed-date parsed-time))))

(defn date-time-input [opts]
  (let [fmt-time (or (:format-time opts) "24h")
        fmt-date (or (:format-date opts) "iso")
        fmt-date-obj (get formats fmt-date)
        timezone (:timezone opts)
        to-utc-fn (partial to-utc (or timezone "utc"))
        to-local-fn (if timezone (partial to-local timezone) identity)
        plc-time {"24h" "hh:mm" "12h" "hh:mm AM|PM"}
        placeholder (str (:placeholder fmt-date-obj) " " (get plc-time fmt-time))
        state (r/atom {:lastValue (:value opts) :value
                       (date-time-unparse fmt-date fmt-time
                                          (to-local-fn (:value opts)))})
        my-on-blur (fn [event on-blur]
                     (when (:errors @state)
                       (swap! state assoc :value (date-time-unparse
                                                  fmt-date fmt-time (:lastValue @state)))
                       (swap! state dissoc :errors))
                     (on-blur))
        my-onchange (fn [event on-change]
                      (let [v (.. event -target -value)]
                        (swap! state assoc :value v)
                        (if-let [vv (date-time-parse fmt-date fmt-time v)]
                          (do
                            (on-change (to-utc-fn vv))
                            (swap! state dissoc :errors))
                          (do
                            (swap! state assoc :errors
                                   (str "The format is: " placeholder))))))]
    (r/create-class
     {:component-will-receive-props
      (fn [_ nextprops]
        (when-let [{utc-v :value} (second nextprops)]
          (let [v (to-local-fn utc-v)]
            (when-not (= v (:lastValue @state))
              (swap! state assoc
                     :value (date-time-unparse fmt-date fmt-time v)
                     :lastValue v)))))

      :reagent-render
      (fn [{:keys [on-change errors on-blur err-classes] :as props}]
        [:div
         [:div.date-input
          [:i.material-icons
           {:on-click
            #(let [parent-node (.. % -target -parentNode)
                   input (cmn/f-child parent-node "re-input")]
               (.focus input))} "schedule"]
          [:input.re-input (merge (dissoc props :errors :format-date :format-time :timezone :err-classes)
                                  {:type "text"
                                   :placeholder placeholder
                                   :on-blur #(my-on-blur % on-blur)
                                   :on-change #(my-onchange % on-change)
                                   :value (:value @state)})]]
         [:div {:class (apply str (interpose "." err-classes))}
          (when-let [local-errors (:errors @state)]
            (str/join "\n" (conj errors local-errors)))]])})))
