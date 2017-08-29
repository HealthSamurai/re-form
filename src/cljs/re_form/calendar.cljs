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
         (map #(drop-while nil? %))
         ))))

(defn- get-next-month [date]
  (js/Date. (.getFullYear date)
            (inc (.getMonth date))
            1))

(defn- get-prev-month [date]
  (js/Date. (.getFullYear date)
            (dec (.getMonth date))
            1))

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
