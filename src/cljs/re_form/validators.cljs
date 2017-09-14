(ns re-form.validators
  (:require [clojure.string :as str]))

(defn not-blank [v]
  (when (str/blank? v) "Should not be blank"))

(defn email [v]
  (if (not (= "a@b.com" v)) ["Should be a@b.com"]))
