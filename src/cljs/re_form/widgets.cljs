(ns re-form.widgets
  (:require [re-form.widgets.file-upload-impl :as fui]
            [re-form.widgets.textarea-impl :as textarea-impl]
            [re-form.widgets.csv-input-impl :as csv]))

;; Widgets

(def file-upload fui/file-upload)
(def textarea textarea-impl/textarea)
(def csv-input csv/csv-input)

;; Styles

(def textarea-style textarea-impl/textarea-style)

(defn text-input [props]
  (let [my-onchange (fn [event on-change] (on-change (.. event -target -value)))]
    (fn [{:keys [value on-change] :as props}]
      [:input (merge {:type "text"} props {:on-change #(my-onchange % on-change)
                                           :value value})])))
