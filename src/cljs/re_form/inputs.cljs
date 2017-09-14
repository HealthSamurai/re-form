(ns re-form.inputs
  (:require [re-form.inputs.file-upload-impl :as fupl-impl]
            [re-form.inputs.textarea-impl :as ta-impl]
            [re-form.inputs.text-input-impl :as tei-impl]
            [re-form.inputs.radio-input-impl :as rad-impl]
            [re-form.inputs.csv-input-impl :as csv-impl]
            [re-form.inputs.calendar-impl :as cal-impl]
            [re-form.inputs.switchbox-impl :as sw-impl]
            [re-form.inputs.button-select-input-impl :as bs-impl]))

;; Widgets

(def file-upload-input fupl-impl/file-upload)
(def textarea-input ta-impl/textarea)
(def csv-input csv-impl/csv-input)
(def text-input tei-impl/text-input)
(def radio-input rad-impl/radio-input)
(def calendar-input cal-impl/re-calendar)
(def switchbox-input sw-impl/switch-box)
(def button-select-input bs-impl/button-select-input)

;; Styles

(def textarea-style ta-impl/textarea-style)
(def radio-input-style rad-impl/radio-input-styles)
(def calendar-style cal-impl/re-calendar-style)
(def switchbox-style sw-impl/re-switch-box-style)
(def button-select-style bs-impl/button-select-style)
