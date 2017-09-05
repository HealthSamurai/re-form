(ns re-form.calendar)

(defn- get-days-of-month
  ([date]
  (get-days-of-month date false))

  ([date mark-current]
  (let [curr-day (.getDate date)
        year (.getFullYear date)
        month (.getMonth date)
        first-day-of-month (js/Date. year month 1)
        day-of-week (.getDay first-day-of-month)
        first-day 1
        last-day (.getDate (js/Date. year (inc month) 0))]

    (->> (range first-day (inc last-day))
         (map #(if (and mark-current
                        (= % curr-day))
                 (hash-map :value % :current true)
                 (hash-map :value %)))
         (concat (repeat day-of-week nil))
         (partition 7 7 nil)
         (map #(drop-while nil? %))))))

(defn- get-next-month [date]
  (js/Date. (.getFullYear date) (inc (.getMonth date)) 1))

(defn- get-prev-month [date]
  (js/Date. (.getFullYear date) (dec (.getMonth date)) 1))

(defn get-month-matrix
  "Takes js Date object
  Returns list of lists of hash-maps"
  [date]
  (let [prev-month-week (->> (-> (get-prev-month date) get-days-of-month last)
                             (map #(assoc % :off true)))

        next-month-week (->> (-> (get-next-month date) get-days-of-month first)
                             (map #(assoc % :off true)))

        curr-month (get-days-of-month date true)

        first-week (concat prev-month-week (first curr-month))
        last-week (concat (last curr-month) next-month-week)]
    (concat [first-week]
            (drop-last (rest curr-month))
            [last-week])))

(defn *calendar-header []
  [:thead
   [:tr
    [:th "<"]
    [:th {:col-span 5} [:div.date "TODO"]]
    [:th ">"]]
   [:tr
    [:th "Su"]
    [:th "Mo"]
    [:th "Tu"]
    [:th "We"]
    [:th "Th"]
    [:th "Fr"]
    [:th "Su"]]])

(defn *calendar [data]
  [:div.re-calendar
   [:table
    [*calendar-header]
    [:tbody
     (map-indexed (fn [idx week]
                    [:tr {:key idx}
                     (for [day week]
                       [:td {:class (cond (:current day) "current"
                                          (:off day) "off")
                             :key (:value day)}
                        (:value day)])])
                  data)]]])

(def re-calendar-style
  [:.re-calendar
   [:.calendar-title {:padding-top "20px"
                      :display :flex
                      :justify-content :space-around
                      :flex-direction :row
                      :color :gray
                      :font-size "12px"}]
   [:table
    {:width "auto"}
    [:th {:font-size "16px" :text-align "center" :color "#888"}]
    [:td {:text-align "center"}]]
   [:.date {:font-size "16px" :text-align "center"}]])

(defn re-calendar [opts]
  (let [data (get-month-matrix (js/Date.))]
    (fn [props]
      [*calendar data])))


;; (defn *get-weeks [start-with]
;;   (let [start-from (or (:offset start-with) 2)
;;         num-days 31
;;         all-days (+ start-from num-days)
;;         num-weeks (+ (quot all-days  7)
;;                      (if (> (rem all-days 7) 0) 1 0))]
;;     (for [w (range num-weeks)]
;;       (for [n (range 7)]
;;         (- (+ (* w 7) n) start-from)))))

;; (rf/reg-event-db
;;  ::cal-next-month
;;  (fn [db [_ dir]]
;;    (update-in db [::month :offset] (fn [x] (+ x dir)))))

;; (rf/reg-sub-raw ::month
;;                 (fn [db] (reaction (::month @db))))

;; (rf/reg-event-db
;;  ::set-day
;;  (fn [db [_ day]]
;;    (assoc-in db [::month :day] day)))

;; (defn calendar []
;;   (let [cal (rf/subscribe [::month])]
;;     (fn []
;;       [:div
;;        [styles/style
;;         [:.calendar {:width "80%" :margin "20px auto"}
;;          [:.ctrl {:display "block"
;;                   :padding "3px 4px"
;;                   :text-align "center"
;;                   :border-radius "50%"
;;                   :cursor "pointer"}
;;           [:&:hover {:background-color "#525862"}]]
;;          [:.month {:text-align "center"}]
;;          [:th {:text-align "center"}]
;;          [:td {:padding "4px 6px 4px 7px"
;;                :opacity 0.6
;;                :cursor "pointer"
;;                :border-radius "50%"
;;                :text-align "center"}
;;           [:&:hover {:background-color "#525862"
;;                      :opacity 1}]]
;;          [:td.active {:background-color "#28C194"
;;                       :opacity 1
;;                       :transition "background-color 0.3s ease-in-out"
;;                       :font-weight "bold"}]]]
;;        [:table.calendar
;;         [:thead
;;          [:tr
;;           [:th
;;            [:a.ctrl {:on-click #(rf/dispatch [::cal-next-month -1])}
;;             (wgt/icon :chevron-left)]]
;;           [:th.month {:col-span "5"} "December 2016"]
;;           [:th
;;            [:a.ctrl {:on-click #(rf/dispatch [::cal-next-month 1])}
;;             (wgt/icon :chevron-right)]]]
;;          [:tr [:th "S"] [:th "M"] [:th "T"] [:th "W"] [:th "T"] [:th "F"] [:th "S"]]]
;;         [:tbody
;;          (doall (map-indexed
;;            (fn [i ds]
;;              [:tr {:key i}
;;               (doall (map (fn [d]
;;                       [:td {:key d
;;                             :class (when (= (:day @cal) d) "active")
;;                             :on-click #(rf/dispatch [::set-day d])}
;;                        (when (< 0 d) (str d))]) ds))
;;               ]) (*get-weeks @cal)))]]])))
