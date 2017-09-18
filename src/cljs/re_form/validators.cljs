(ns re-form.validators
  (:require [clojure.string :as str]
            [bouncer.validators :as v]))

(defn not-blank [& {:keys [message] :or {message "Should not be blank"}}]
  (fn [v]
    (when (str/blank? v) message)))

(defn email [& {:keys [message] :or {message "Not a valid email"}}]
  (fn [v]
    (when-not (v/email v) message)))

(defn regex [rx & {:keys [message] :or {message (str "Should match regex: " rx)}}]
  (fn [v]
    (when-not (and v (re-matches rx v)) message)))

(defn min-count [n count-f & {:keys [message] :or {message (str "Must be >= " n)}}]
  (fn [v]
    (when (< (count-f v) n) message)))
