(ns ui.webcam-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ui.routes :as ui-routes]
            [re-frame.core :as rf]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [re-form.inputs :as inputs]
            [garden.core :as garden]))

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

(rf/reg-event-db
 ::image-taken
 (fn [db [_ name id]]
   db))

(def webcam-style
  [:style
   (garden/css
    [:*
     [:.buttons {:display :flex
                 :justify-content :left}]])])

(rf/reg-event-fx
 ::submit
 [(rf/inject-cofx :webcam/images)]
 (fn [{:keys [db images]} [_ image-name]]
   {}))

(defn webcam-page []
  (rf/dispatch [::init])
  (fn []
    [:div
     webcam-style
     [inputs/webcam {:width 320
                     :height 240
                     :on-image [::image-taken]}]
     [:div.buttons
      [:button.btn.btn-primary {:on-click #(rf/dispatch [:webcam/shot {:name :my-photo}])}
       "Photo"]
      [:button.btn.btn-primary {:on-click #(rf/dispatch [:webcam/clear-history :my-photo])}
       "Take new"]
      [:button.btn.btn-primary {:on-click #(rf/dispatch [::submit :my-photo])}
       "Submit"]]]))

(ui-routes/reg-page
 :webcam {:title "Webcam capture"
          :w 27
          :cmp webcam-page})
