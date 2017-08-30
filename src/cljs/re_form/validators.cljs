(ns re-form.validators
  (:require [clojure.string :as str]))


(defn not-blank? [v]
  (when (str/blank? v) "Should not be blank"))

(defn email? [v]
  (when (not (= "a@b.com" v)) "Should be a@b.com"))


(def validators {:not-blank? not-blank?
                 :email? email?})
