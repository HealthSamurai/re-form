(ns re-form.shared
  (:require [clojure.string :as str]))

(defn init [db manifest]
  (assoc-in db (:path manifest) manifest))

(defn not-blank-validator [opts field _]
  (when (str/blank? field)
    "Could not be blank!"))

(defn min-length-validator [opts field _]
  ;; (println "min-length" opts field)
  (when (and field (>= opts (count field)))
    (str "Lenght should be more then " opts)))

(defn email-validator [opts field _]
  (when-not (and (string? field) (re-matches #".+@.+\..+" field))
    (str "Should be email")))


(def validators
  (atom {:not-blank not-blank-validator
         :email email-validator
         :min-length min-length-validator}))

(defn get-manifest [db {frm :form pth :path :as opts}]
  ;; (println "mani" opts)
  (let [*meta (:meta (get-in db (:path frm)))
        ppath (conj pth (:name opts))]
    (loop [obj *meta
           [p & pp] ppath]
      ;; (println "step:" p obj)
      (cond
        (nil? p) obj
        (keyword p) (recur (get-in obj [:properties p]) pp)
        (number? p) (recur (get-in obj [:items]) pp)
        :else nil))))

(defn insert-by-path [m [k & ks :as path] value]
  (if ks
    (if (int? k)
      (assoc (or m []) k (insert-by-path (get m k) ks value))
      (assoc (or m {}) k (insert-by-path (get m k) ks value)))
    (if (int? k)
      (assoc (or m []) k value)
      (assoc (or m {}) k value))))

(defn input-path [{{fpth :path :as frm} :form  pth :path  nm :name :as opts}]
  (let [pth (if pth (into (conj fpth :value) pth) (conj fpth :value))]
    (conj pth nm)))

(defn state-path [{frm :form :as opts}]
  (into []
        (concat (:path frm)
                [:state]
                (conj (or (:path opts) []) (:name opts)))))

(defn errors-path [{frm :form :as opts}]
  (concat (:path frm)
          [:state]
          (conj (or (:path opts) []) (:name opts))
          [:errors]))

(defn dirty-path [{frm :form :as opts}]
  (concat (:path frm)
          [:state]
          (conj (or (:path opts) []) (:name opts))
          [:dirty]))

(defn touched-path [{frm :form :as opts}]
  (concat (:path frm)
          [:state]
          (conj (or (:path opts) []) (:name opts))
          [:touched]))

(defn validate [manifest v]
  (when-let [vlds (:validators manifest)]
    (reduce-kv
     (fn [acc k opts]
       (if-let [f (get @validators k)]
         (if-let [err (f opts v nil)]
           (assoc acc k err)
           acc)
         (do (println "Validator " k  " not found") acc)))
     {}
     vlds)))

(defn get-errors [db opts]
  (get-in db (errors-path opts)))

(defn on-change [db opts v]
  (let [manifest (get-manifest db opts)]
    (-> db
        (insert-by-path (input-path opts) v)
        (insert-by-path (conj (:path (:form opts)) :touched)  true)
        (insert-by-path (conj (:path (:form opts)) :dirty)  true)
        (insert-by-path (dirty-path opts) true)
        (insert-by-path (touched-path opts) true)
        (insert-by-path (errors-path opts) (validate manifest v)))))
