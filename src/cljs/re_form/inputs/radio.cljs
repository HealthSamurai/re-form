(ns re-form.inputs.radio
  (:require [reagent.core :as r]))

(defn radio [{:keys [value] :as props}]
  (let [state (r/atom {:value value})]
    (r/create-class
     {:component-will-receive-props
      (fn [this props]
        (let [my-value (:value @state)
              new-value (:value (nth props 1))]
          (when (not= my-value new-value)
            (swap! state merge {:value new-value}))))

      :reagent-render
      (fn [{:keys [on-change value-fn label-fn items] :as props}]
        (let [label-fn (or label-fn pr-str)
              value-fn (or value-fn identity)
              {:keys [value]} @state
              on-cnange (or on-change js/console.log)]
          [:div.re-radio-group
           (doall
            (for [i items]
              [:div.option
               {:key (pr-str i)
                :class (when (= (value-fn i) value) "active")
                :on-click (fn []
                            (let [v (value-fn i)]
                              (swap! state assoc  :value v)
                              (on-change v)))}
               [:span.radio]
               [:span.value (label-fn i)]]))]))})))
