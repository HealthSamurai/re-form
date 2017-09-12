(ns ui.file-upload-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [reagent.core :as reagent]
   [re-form.core :as form]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [re-form.widgets :refer [file-upload]]))

(defn file-upload-page []
  (let [state (reagent/atom {})
        upload-fn (fn [files]
                    (.log js/console "!!!!!!" files)
                    (go
                      (.log js/console "222222222" files)
                      (:body (<! (http/post "http://tealnet.health-samurai.io/$upload"
                                            {:multipart-params {:file0 (aget files 0)}})))))]

    (fn []
      [:article
       [:h1 "File Upload"]

       [file-upload {:on-change (fn [x] (swap! state assoc :v x))
                     :upload-fn upload-fn
                     :value (:v @state)}]

       [:br]
       [:br]
       [:br]
       [:br]
       [:button {:on-click #(swap! state assoc :v {:foobar 42} )} "Set File Upload Value"]])))
