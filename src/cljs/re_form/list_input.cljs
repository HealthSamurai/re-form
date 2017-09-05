(ns re-form.list-input
  (:require
   [reagent.core :as reagent]
   [garden.units :as u]
   [re-frame.core :as rf]
   [clojure.string :as str]))

(def re-list-style
  [:.re-list {}])

(defn parse-value [x]
  (->> (str/split x #",")
       (mapv str/trim)
       (filterv #(not (str/blank? %)))))

;; (parse-value "a,b")


(defn *re-list [{on-change :on-change v :value}]  
  (let [state (reagent/atom {:value nil :old-value nil})
        on-change (fn [ev]
                    (swap! state assoc :value (.. ev -target -value))
                    (let [new-v (parse-value (.. ev -target -value))]
                      (when (and on-change (not (= (:old-value @state) new-v)))
                        (swap! state assoc :old-value new-v)
                        (on-change {:value new-v}))))]

    (reagent/create-class
     {:component-will-receive-props
      (fn [this new-argv]
        (let [n-v (:value (second new-argv))]
          (when (not (= n-v (:old-value @state)))
            (swap! state assoc
                   :old-value n-v
                   :value (str/join ", " n-v)))))

      :display-name  "re-list"

      :reagent-render
      (fn [props]
        [:input {:type "text" :value (:value @state) :on-change on-change}])})))

(defn re-list [opts]  
  (let [state (reagent/atom {:value nil})
        on-change (fn [ev] (rf/dispatch [:re-form/update opts (:value ev)]))
        v (rf/subscribe [:re-form/value opts])]
    (fn [props]
      [*re-list {:value @v :on-change on-change}])))

