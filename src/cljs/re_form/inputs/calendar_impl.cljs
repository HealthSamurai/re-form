(ns re-form.inputs.calendar-impl
  (:require [reagent.core :as r]))

(def months
  {0  {:name "January" :days 31}
   1  {:name "February" :days 28 :leap 29}
   2  {:name "March" :days 31}
   3  {:name "April" :days 30}
   4  {:name "May" :days 31}
   5  {:name "June" :days 30}
   6  {:name "July" :days 31}
   7  {:name "August" :days 31}
   8  {:name "September" :days 30}
   9  {:name "October" :days 31}
   10 {:name "November" :days 30}
   11 {:name "December" :days 31}})

(def weeks
  {1 {:name "Monday"}
   2 {:name "Tuesday"}
   3 {:name "Wednesday"}
   4 {:name "Thursday"}
   5 {:name "Friday"}
   6 {:name "Saturday"}
   7 {:name "Sunday"}})

(defn get-month [y m]
  (let [start (js/Date. y m)
        today (js/Date.)
        today-day (.getDate today)
        today-year (.getFullYear today)
        today-month (.getMonth today)
        year (.getFullYear start)
        month (.getMonth start)
        day (.getDay start)
        cal-start (js/Date. (.setDate start (- 1 day)))
        prev-date (.getDate cal-start)
        prev-year (.getFullYear cal-start)
        prev-month (.getMonth cal-start)

        last-date (js/Date. year (inc month) 0)
        last-day (.getDay last-date)
        num-days (.getDate last-date)

        next-date (js/Date. year (inc month) 1)
        next-month (.getMonth next-date)
        next-year (.getFullYear next-date)]
    (for [w (range 6)]
      (for [d (range 7)]
        (let [offset (+ d (* w 7))]
          (cond
            (< offset day) {:m prev-month :y prev-year :d (+ prev-date offset)}
            (>= offset (+ day num-days)) {:m next-month :y next-year :d (inc (- offset day num-days))}
            :else (let [d (inc (- offset day))]
                    {:m month
                     :y year
                     :d d
                     #_(:today (and (= d today) (= year today-year) (= month today-month))
                                :current true)})))))))

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
        last-week (if (< (count (last curr-month)) 7)
                    (concat (last curr-month) next-month-week)
                    (last curr-month))]
    (concat [first-week]
            (drop-last (rest curr-month))
            [last-week])))


(defn re-calendar-style [_]
  [:.re-calendar {:display "inline-block"}
   [:.calendar-title {:padding-top "20px"
                      :display :flex
                      :color :gray
                      :font-size "12px"}]
   [:table
    {:width "auto"}
    [:th {:font-size "16px" :text-align "center" :color "#888" :font-weight "normal"}]
    [:td {:text-align "center" :color "#aaa"
          :font-size "0.8em"
          :width "30px" :height "30px" :cursor "pointer"}
     [:&:hover {:background-color "#f1f1f1"}]
     [:&.active {:background-color "#007bff" :color "white!important"}]
     [:&.current {:color "#555"}]
     [:&.today {:font-weight "bold" :color "#333"}]]]
   [:.date {:font-size "16px" :text-align "center"}]])

(defn *calendar-header [state]
  (let [prev-month (fn [_]
                     (let [cal (:cal @state)
                           p (js/Date. (:y cal) (dec (:m cal)) 1)]
                       (swap! state #(-> % (assoc :cal {:y (.getFullYear p)
                                                        :m (.getMonth p)})
                                         (assoc :detached true)))))
        next-month (fn [_]
                     (let [cal (:cal @state)
                           p (js/Date. (:y cal) (inc (:m cal)) 1)]
                       (swap! state #(-> % (assoc :cal {:y (.getFullYear p)
                                                        :m (.getMonth p)})
                                         (assoc :detached true)))))
        switch-mode (fn [m] (fn [_] (swap! state assoc :mode m)))]
    (fn []
      [:thead
       [:tr
        [:th {:on-click prev-month} "<"]
        [:th {:col-span 5} [:div.date {:on-click switch-mode}
                            [:a {:on-click (switch-mode :month)}
                             (:name (get months (get-in @state [:cal :m])))]
                            " "
                            [:a {:on-click (switch-mode :year)}
                             (get-in @state [:cal :y])]]]
        [:th {:on-click next-month} ">"]]
       [:tr
        [:th "Su"]
        [:th "Mo"]
        [:th "Tu"]
        [:th "We"]
        [:th "Th"]
        [:th "Fr"]
        [:th "Sa"]]])))

(defn calendar-days [state on-change v]
  (let [cal (:cal @state)
        data (get-month (:y cal) (:m cal))]
    [:table
     [*calendar-header state]
     [:tbody
      (for [week data]
        [:tr {:key (let [f (first week)] (str (:m f) "-" (:d f)))}
         (for [day week]
           [:td {:key (str (:m day) (:d day))
                 :on-click (fn [_] (swap! state assoc :detached false)
                             (on-change day))
                 :class (str
                         (when (= day v)
                           "active")
                         " "
                         (when (:today day) "today"))}
            (pr-str (:d day))])])]]))

(defn calendar-month [state]
  (let [switch-mode (fn [m] (swap! state assoc :mode :year))
        choose-month (fn [m]
                       (swap! state (fn [s]
                                      (-> s 
                                          (assoc-in [:cal :m] m)
                                          (assoc :mode :days)
                                          (assoc :detached true)))))]
    (fn []
      [:table
       [:thead
        [:tr [:th {:col-span 4} [:div.date {:on-click switch-mode} (get-in @state [:cal :y]) ]]]]
       [:tbody
        (doall (for [r (range 4)]
                 [:tr {:key r}
                  (for [idx (range 1 4)]
                    (let [m (dec (+ idx (* 3 r)))]
                      [:td {:key idx :on-click #(choose-month m)}
                       (subs (:name (get months m))
                             0 3)]))]))]])))

(defn calendar-year [state]
  (let [switch-mode (fn [_] (swap! state assoc :mode :month))
        choose-year (fn [y]
                       (swap! state (fn [x]
                                      (-> x 
                                          (assoc-in [:cal :y] y)
                                          (assoc :mode :days)
                                          (assoc :detached true)))))]
    (fn []
      (let [y (get-in @state [:cal :y])]
        [:table
         [:tbody
          (doall (for [r (range 5)]
                   [:tr {:key r}
                    (for [idx (range 5)]
                      (let [x (+ (- y 11) idx (* 5 r))]
                        [:td {:key x :on-click #(choose-year x)}
                         x]))]))]]))))

(defn re-calendar [{:keys [value on-change errors]}]
  (let [state (r/atom {:mode :days})]
    (fn [{:keys [value on-change errors]}]
      (when-not (:detached @state)
        (swap! state assoc :cal value))
      [:div.re-calendar
       (case (:mode @state)
         :days
         [calendar-days state on-change value]

         :month
         [calendar-month state]

         :year
         [calendar-year state])])))
