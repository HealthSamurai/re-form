(ns re-form.inputs.date-input-impl
  (:require [reagent.core :as r]
            [goog.string :as gstring]
            [goog.string.format]
            [clojure.string :as str]))

(def formats
  {
   "dd.mm.yyyy" {:regex #"^(\d\d)\.(\d\d)\.(\d\d\d\d)$"
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

(defn zip-date [names groups]
  (apply hash-map (interleave names groups)))

(defn parse [fmt x]
  (when x
    (when-let [f (formats fmt)]
      (when-let [[_ & groups] (re-matches (:regex f) x)]
        (let [date-hm (zip-date (:groups f) groups)]
          (str (:y date-hm) "-" (:m date-hm) "-" (:d date-hm)))))))

(defn unparse [fmt x]
  (when x
    (when-let [f (formats "iso")]
      (when-let [[_ & groups] (re-matches (:regex f) x)]
        (let [date-hm (zip-date (:groups f) groups)]
          (when-let [fmt-to (get formats fmt)]
            (->> (mapv #(get date-hm %) (:groups fmt-to))
                 (str/join (:delimiter fmt-to)))))))))


(defn date-input [opts]
  (let [fmt (or (:format opts) "iso")
        fmt-obj (get formats fmt)
        state (r/atom {:lastValue (:value opts) :value (unparse fmt (:value opts))})
        my-on-blur (fn [event on-blur]
                     (when (:errors @state)
                       (swap! state assoc :value (unparse fmt (:lastValue @state)))
                       (swap! state dissoc :errors))
                     (on-blur))
        my-onchange (fn [event on-change]
                      (let [v (.. event -target -value)]
                        (swap! state assoc :value v)
                        (if-let [vv (parse fmt v)]
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
            (swap! state assoc :value (unparse fmt v) :lastValue v))))

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
            (str/join "\n" (conj errors local-errors)))]])})))

(defn parse-time [fmt x]
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
            (str/join "\n" (conj errors local-errors)))]])})))

(defn time-parse-time [fmt x]
  (if (= fmt "12h")
    (when-let [[_ hp mp noonp] (re-matches #"(\d?\d):(\d\d) (\wM)" (str/upper-case x))]
      (let [h (+ (js/parseInt hp) (if (= noonp "PM") 12 0))
            m (js/parseInt mp)]
        (when (and (< h 24) (< m 59) (some #(= % noonp) ["AM" "PM"]))
          (str (gstring/format "%02d" h) ":" (gstring/format "%02d" m)))))
    (when-let [[_ h m] (re-matches #"(\d\d):(\d\d)" x)]
      (when (and (< h 24) (< m 59))
        x))))

(defn time-unparse-time [fmt x]
  (if (= fmt "12h")
    (when-let [[_ hp m] (re-matches #"(\d\d):(\d\d)" x)]
      (let [[h noon] (if (> hp 12) [(- hp 12) "PM"] [hp "AM"])]
        (str (gstring/format "%02d" h) ":" (gstring/format "%02d" m) " " noon)))
    x))

(defn time-input [opts]
  (let [fmt (or (:format opts) "24h")
        placeholder {"24h" "hh:mm" "12h" "hh:mm AM|PM"}
        state (r/atom {:lastValue (:value opts) :value (time-unparse-time fmt (:value opts))})
        my-on-blur (fn [event on-blur]
                     (when (:errors @state)
                       (swap! state assoc :value (time-unparse-time fmt (:lastValue @state)))
                       (swap! state dissoc :errors))
                     (on-blur))
        my-onchange (fn [event on-change]
                      (let [v (.. event -target -value)]
                        (swap! state assoc :value v)
                        (if-let [vv (time-parse-time fmt v)]
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
            (swap! state assoc :value (time-unparse-time fmt v) :lastValue v))))

      :reagent-render
      (fn [{:keys [value on-change errors on-blur err-classes] :as props}]
        [:div
         [:input.re-input (merge (dissoc props :errors)
                        {:type "text"
                         :placeholder (get placeholder fmt)
                         :on-blur #(my-on-blur % on-blur)
                         :on-change #(my-onchange % on-change)
                         :value (:value @state)})]
         [:div {:class (apply str (interpose "." err-classes))}
          (when-let [local-errors (:errors @state)]
            (str/join "\n" (conj errors local-errors)))]])})))
