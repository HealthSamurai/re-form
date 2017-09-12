(ns re-form.widgets.file-upload-impl
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [cljs.core.async :refer [<!]]))

(defn file-upload [{:keys [value]}]
  (let [state (r/atom {:value value :uploading? false})
        my-onchange (fn [e upload-fn callback]
                      (let [files (array-seq (.-files (.-target e)))]
                        (swap! state merge {:uploading? true :value nil :files files})
                        (let [result-ch (upload-fn files)]
                          (go (callback (<! result-ch))
                              (swap! state merge {:uploading? false :files nil})))))

        open-file-dialog (fn []
                           (when-let [file-input (:input-ref @state)]
                             (.click file-input)))]

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
        (let [{:keys [uploading? value files]} @state]
          [:div.file-upload {:class (and uploading? "uploading")}
           [:input {:style {:display "none"}
                    :ref #(swap! state assoc :input-ref %)
                    :type "file"
                    :multiple multiple
                    :on-change #(my-onchange % upload-fn on-change)}]

           (if uploading?
             (str "Uploading " (str/join ", " (map #(.-name %) files)) "...")
             (if value
               [:pre (.stringify js/JSON (clj->js (:value @state)))]
               [:a {:href "javascript:void(0);" :on-click open-file-dialog} "Select file to upload..."]))]))})))
