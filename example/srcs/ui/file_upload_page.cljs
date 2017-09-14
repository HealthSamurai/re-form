(ns ui.file-upload-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [reagent.core :as reagent]
   [re-form.core :as form]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [re-form.inputs :refer [file-upload-input]]))

(defn file-upload-page []
  (let [state (reagent/atom {})
        upload-fn (fn [files]
                    (go
                      (:body (<! (http/post "http://tealnet.health-samurai.io/$upload"
                                            {:multipart-params
                                             (apply hash-map (mapcat (fn [[idx f]] [(keyword (str "file" idx)) f])
                                                                     (map-indexed (fn [idx f] [idx f]) files)))})))))]

    (fn []
      [:article
       [:h1 "File Upload"]

       [file-upload-input {:on-change (fn [x] (swap! state assoc :v x))
                           :upload-fn upload-fn
                           :multiple (:multiple @state)
                           :value (:v @state)}]

       [:br]
       [:label
        [:input {:type "checkbox"
                 :value (:multiple @state)
                 :on-change (fn [e] (swap! state update-in [:multiple] not))}]

        "Allow to choose multiple files"]
       [:br]
       [:br]
       [:br]
       [:button {:on-click #(swap! state assoc :v {:foobar 42} )} "Set File Upload Value"]])))
