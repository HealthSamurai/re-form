(ns re-form.widgets
  (:require [re-form.widgets.file-upload-impl :as fui]
            [re-form.widgets.textarea-impl :as textarea-impl]))

;; Widgets

(def file-upload fui/file-upload)
(def textarea textarea-impl/textarea)


;; Styles

(def textarea-style textarea-impl/textarea-style)
