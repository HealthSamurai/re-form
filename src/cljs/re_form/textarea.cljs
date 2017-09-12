(ns re-form.textarea
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(def re-textarea-style
  [:.re-textarea {:resize "none"}])

(defn- count-newlines [a]
  (reagent/track (fn [] (count (re-seq #"\n" (or @a ""))))))

(defn- *re-textarea [ra-opts]
  [:textarea.re-textarea ra-opts])

(defn re-textarea [opts]
  (let [on-change (fn [event] (rf/dispatch [:re-form/update opts (.. event -target -value)]))
        v (rf/subscribe [:re-form/value opts])
        c (count-newlines v)]
    (fn [props]
      (let [compute-lines (+ @c (get-in opts [:form :lines-after]))]
        [*re-textarea {:value @v
                       :on-change on-change
                       :rows compute-lines}]))))
