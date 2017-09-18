(ns re-form.validators
  (:require [clojure.string :as str]
            [bouncer.validators :as v]))

(defn not-blank [& {:keys [message] :or {message "Should not be blank"}}]
  (fn [v]
    (when (str/blank? v) message)))

(defn email [& {:keys [message] :or {message "Not a valid email"}}]
  (fn [v]
    (when-not (v/email v) message)))
