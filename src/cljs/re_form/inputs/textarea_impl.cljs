
(ns re-form.inputs.textarea-impl
  (:require [reagent.core :as r]))

(defn textarea-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.re-textarea {:resize ""
                  :border border}])

(defn- style-onchange [visual-state node]
  (let [rows (Math.ceil (/ (do (set! (.-rows node) 1)
                               (.-scrollHeight node))
                           (:base-line-height @visual-state)))]
    (set! (.-rows node) rows)))

(defn- event->value [orig-fn]
  (fn [event]
    (orig-fn (.. event -target -value))))

(defn textarea [{:keys [value on-change lines-after html-params]}]
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

      :component-did-update
      (fn [this _ _]
        (style-onchange visual-state (r/dom-node this)))

      :reagent-render
      (fn [{:keys [value on-change html-params]}]
        [:textarea.re-textarea
         (merge html-params
                {:value value
                 :on-change (juxt (event->value on-change)
                                  #(style-onchange visual-state (.-target %)))})])})))
