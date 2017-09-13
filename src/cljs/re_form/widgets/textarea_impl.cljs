(ns re-form.widgets.textarea-impl
  (:require [reagent.core :as r]))

(def textarea-style
  [:.re-textarea {:resize "none"}])

(defn- count-newlines [a]
  (r/track (fn [] (count (re-seq #"\n" (or @a ""))))))

(defn textarea [{:keys [value on-change lines-after]}]
  (let [lines (count-newlines value)]
    (fn [{:keys [value on-change]}]
      (let [compute-lines (+ @lines lines-after)]
        [:textarea.re-textarea {:value @value
                                :on-change on-change
                                :rows compute-lines}]))))
