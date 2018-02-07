(ns re-form.inputs.webcam-impl
  (:require [reagent.core :as r]
            [garden.units :as u]
            [re-frame.core :as rf]))

(defonce widget-state (atom {}))

(defn webcam [{:keys [photo-name] :as opts}]
  (r/create-class
   {:component-did-mount
    (fn [_]
      (swap! widget-state assoc-in [photo-name :opts] opts)
      (let [video (get-in @widget-state [photo-name :video])]
        (when (and video
                   (.-mediaDevices js/navigator)
                   (.. js/navigator -mediaDevices -getUserMedia))
          (.. js/navigator
              -mediaDevices
              (getUserMedia (clj->js {:video true}))
              (then (fn [stream]
                      (swap! widget-state assoc-in [photo-name :video-tracks] (js->clj (.getVideoTracks stream)))
                      (set! (.-src video) (.. js/window
                                              -URL
                                              (createObjectURL stream)))
                      (.play video)))))))

    :component-will-unmount
    (fn [_]
      ;; FIXME here we deinit all widgets at once
      ;; assumption is they are all the part of one page
      (let [tracks (mapcat :video-tracks (vals @widget-state))]
        (doseq [t tracks]
          (when t (.stop t)))
        (reset! widget-state {})))

    :reagent-render
    (fn [{:keys [photo-name width height]}]
      [:div.re-webcam {:style {:padding "10px"}}
       [:video {:ref #(swap! widget-state assoc-in [photo-name :video] %)
                :width width
                :height height
                :autoPlay true}]
       [:canvas {:ref #(swap! widget-state assoc-in [photo-name :canvas] %)
                 :style {:display :none}
                 :width width
                 :height height}]])}))

(rf/reg-fx
 :webcam/photo
 (fn [photo-name]
   (let [{:keys [video canvas opts]} (get @widget-state photo-name)
         context (.getContext canvas "2d")]
     (.drawImage context video 0 0 (:width opts) (:height opts))
     (set! (.. video -style -display) "none")
     (set! (.. canvas -style -display) "block")
     (.toBlob canvas #(do (swap! widget-state assoc-in [photo-name :blob] %)
                          ;; this is done to add value into re-form
                          ((:on-change opts) true)
                          (when-let [on-image-ev (:on-image opts)]
                            (rf/dispatch (into on-image-ev [photo-name]))))))))

(rf/reg-event-fx
 :webcam/photo
 (fn [{db :db} [_ photo-name]]
   {:webcam/photo photo-name}))

(rf/reg-fx
 :webcam/clear
 (fn [photo-name]
   (let [{:keys [video canvas opts]} (get @widget-state photo-name)]
     ((:on-change opts) nil)
     (swap! widget-state update photo-name #(dissoc % :blob))
     (set! (.. video -style -display) "block")
     (set! (.. canvas -style -display) "none"))))

(rf/reg-event-fx
 :webcam/clear
 (fn [{db :db} [_ photo-name]]
   {:webcam/clear photo-name}))

(rf/reg-cofx
 :webcam/photos
 (fn [cofx]
   (let [idx (->> @widget-state
                  vals
                  (map (fn [v] [(get-in v [:opts :photo-name]) (:blob v)]))
                  (into {}))]
     (assoc cofx :photos idx))))
