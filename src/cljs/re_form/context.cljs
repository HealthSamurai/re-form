(ns re-form.context
  (:require [reagent.core :as r]))

(def context-type (clj->js {"re-form-context" (-> js/React
                                                  (aget "PropTypes")
                                                  (aget "object"))}))

(defn set-context [context-data child]
  (r/create-class
   {:child-context-types context-type

    :get-child-context
    (fn []
      (let [obj (js/Object.)]
        (aset obj "re-form-context" context-data)
        obj))

    :reagent-render
    (fn [context-data child] child)}))

(defn get-context [props child & child-body]
  (let [ctx (r/atom nil)]
    (r/create-class
     {:context-types context-type

      :component-will-mount
      (fn [this]
        (reset! ctx (aget (.-context this) "re-form-context")))

      :reagent-render
      (fn [props child & child-body]
        (into [child (merge props
                            {:form-name (or (:form-name props)
                                            (keyword (:form-name @ctx)))
                             :path (into (or (:base-path @ctx) [])
                                         (or (:path props) []))})] child-body))})))
