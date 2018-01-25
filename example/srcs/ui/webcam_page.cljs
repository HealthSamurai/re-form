(ns ui.webcam-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ui.routes :as ui-routes]
            [re-frame.core :as rf]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [re-form.inputs :as inputs]))

(rf/reg-sub
 ::thumbs
 (fn [db]
   (::thumbs db)))

(rf/reg-sub
 ::pics
 (fn [db]
   (::pics db)))

(rf/reg-event-db
 ::init
 (fn [db _]
   (assoc db
          ::thumbs []
          ::pics [])))

(rf/reg-event-db
 ::new-thumb
 (fn [db [_ data]]
   (update db ::thumbs conj data)))

(rf/reg-event-db
 ::new-pic
 (fn [db [_ data]]
   (update db ::pics conj data)))

(defn remove-nth [v i]
  (into (subvec v 0 i) (subvec v (inc i))))

(rf/reg-event-db
 ::remove
 (fn [db [_ i]]
   (-> db
       (update ::pics remove-nth i)
       (update ::thumbs remove-nth i))))

(defn webcam-page []
  (rf/dispatch [::init])
  (fn []
    [:div
     [:button.btn.btn-primary
      {:on-click
       (fn [_]
         (go (doall (for [f @(rf/subscribe [::pics])]
                      (http/post
                       "http://localhost:8080/$upload"
                       {:multipart-params
                        {:file f}})))))}
      "Upload"]
     [inputs/thumbs {:thumbs (rf/subscribe [::thumbs])
                     :remove-fn #(rf/dispatch [::remove %])}]
     [inputs/webcam {:width 640
                     :divisor 5
                     :new-thumb #(rf/dispatch [::new-thumb %])
                     :new-pic #(rf/dispatch [::new-pic %])
                     :height 480}]]))

(ui-routes/reg-page
 :webcam {:title "Webcam capture"
          :w 27
          :cmp webcam-page})
