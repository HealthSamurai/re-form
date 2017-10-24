(ns re-form.inputs.file-upload-impl
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [cljs.core.async :refer [<!]]))


(defn file-upload-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.file-upload {:padding "2px 5px"}
   [:.remove {:font-weight "500" :margin-right "5px"}
    [:&:hover {:cursor "pointer" :font-weight "600"}]]])

(defn file-upload [{:keys [value value-fn label-fn]}]
  (let [state (r/atom {:value value :uploading? false})
        label-fn (or label-fn identity)
        value-fn (or value-fn identity)
        my-onchange (fn [e upload-fn callback]
                      (let [files (array-seq (.-files (.-target e)))]
                        (swap! state merge {:uploading? true :value nil :files files})
                        (let [result-ch (upload-fn files)]
                          (go (callback (value-fn (<! result-ch)))
                              (swap! state merge {:uploading? false :files nil})))))

        open-file-dialog (fn [input] (.click input))]

    (r/create-class
     {:component-will-receive-props
      (fn [this props]
        (let [my-value (:value @state)
              new-value (:value (nth props 1))]
          (when (not= my-value new-value)
            (swap! state merge {:value new-value
                                :uploading? false}))))

      :reagent-render
      (fn [{:keys [on-change upload-fn multiple] :as props}]
        (let [{:keys [uploading? value files]} @state
              inp (r/atom {})]
          [:div.file-upload {:class (and uploading? "uploading")}
           [:input {:style {:display "none"}
                    :ref #(reset! inp  %)
                    :type "file"
                    :multiple multiple
                    :on-change #(my-onchange % upload-fn on-change)}]

           (if uploading?
             (str "Uploading " (str/join ", " (map #(.-name %) files)) "...")
             (if-not (empty? value)
               [:div
                [:span.remove {:title "Delete" :on-click #(on-change {})} "X"]
                [:span.value (label-fn  (:value @state))]]
               [:a {:href "javascript:void(0);" :on-click #(open-file-dialog @inp)}
                (or (:placeholder props) "Select file to upload...")]))]))})))
