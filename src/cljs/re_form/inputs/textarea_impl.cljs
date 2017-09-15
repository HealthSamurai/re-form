(ns re-form.inputs.textarea-impl
  (:require [reagent.core :as r]))

(def textarea-style
  [:.re-textarea {:resize ""}])

(defn- style-onchange [visual-state event]
  (let [node (.-target event)
        rows (Math.ceil (/ (do (set! (.-rows node) 1)
                               (.-scrollHeight node))
                           (:base-line-height @visual-state)))]
    (set! (.-rows node) rows)))

(defn- event->value [orig-fn]
  (fn [event]
    (orig-fn (.. event -target -value))))

(defn textarea [{:keys [value on-change lines-after]}]
  (let [visual-state (r/atom nil)]
    (r/create-class
     {
      :component-did-mount
      (fn [this]
        (let [node (r/dom-node this)
              style (.getComputedStyle js/window node)]
          (swap! visual-state assoc :base-line-height (-> (.-lineHeight style)
                                                          (clojure.string/replace #"\D" "")
                                                          js/parseInt))))

      :reagent-render
      (fn [{:keys [value on-change]}]
        [:textarea.re-textarea {:value value
                                :on-change (juxt (event->value on-change)
                                                 (partial style-onchange
                                                          visual-state))}])})))
