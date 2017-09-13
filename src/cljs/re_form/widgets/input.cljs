(ns re-form.widgets.input
  (:require [reagent.core :as r]))

(defn input [{:keys [value] :as props}]
  (let [state (r/atom {:value value})]
    (r/create-class
     {:component-will-receive-props
      (fn [this props]
        (let [my-value (:value @state)
              new-value (:value (nth props 1))]
          (when (not= my-value new-value)
            (swap! state merge {:value new-value}))))

      :reagent-render
      (fn [{:keys [on-change] :as props}]
        (let [{:keys [value]} @state
              on-cnange (or on-change js/console.log)]
          [:input (merge props {:on-change #(on-change (.. % -target -value))})]))})))
