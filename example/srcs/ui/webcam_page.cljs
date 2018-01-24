(ns ui.webcam-page
  (:require [ui.routes :as ui-routes]
            [re-frame.core :as rf]
            [re-form.inputs :as inputs]))

(rf/reg-sub
 ::thumbs
 (fn [db]
   (::thumbs db)))

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

(defn webcam-page []
  (rf/dispatch [::init])
  (fn []
    [:div
     [inputs/thumbs (rf/subscribe [::thumbs])]
     [inputs/webcam {:width 640
                     :divisor 5
                     :new-thumb #(rf/dispatch [::new-thumb %])
                     :new-pic #(rf/dispatch [::new-pic %])
                     :height 480}]]))

(ui-routes/reg-page
 :webcam {:title "Webcam capture"
          :w 27
          :cmp webcam-page})
