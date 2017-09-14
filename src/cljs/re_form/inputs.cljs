(ns re-form.inputs
  (:require [re-form.inputs.file-upload-impl :as fui]
            [re-form.inputs.textarea-impl :as textarea-impl]
            [re-form.inputs.text-input-impl :as tii]
            [re-form.inputs.radio-input-impl :as radio]
            [re-form.inputs.csv-input-impl :as csv]))

;; Widgets

(def file-upload-input fui/file-upload)
(def textarea-input textarea-impl/textarea)
(def csv-input csv/csv-input)
(def text-input tii/text-input)
(def radio-input radio/radio-input)

;; Styles

(def textarea-style textarea-impl/textarea-style)
(def radio-input-style radio/radio-input-styles)
