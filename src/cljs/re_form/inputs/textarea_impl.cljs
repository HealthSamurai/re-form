(ns re-form.widgets.textarea-impl
  (:require [reagent.core :as r]))

(def textarea-style
  [:.re-textarea {:resize "none"}])

(defn- local-onchange [visual-state event]
  (let [node (.-target event)
        rows (Math.ceil (/ (do (set! (.-rows node) 1)
                               (.-scrollHeight node))
                           (:base-line-height @visual-state)))]
    (set! (.-rows node) rows)))

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
        [:textarea.re-textarea {:value @value
                                :on-change (juxt on-change (partial local-onchange
                                                                    visual-state))}])})))
