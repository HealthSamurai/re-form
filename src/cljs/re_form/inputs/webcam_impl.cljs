(ns re-form.inputs.webcam-impl
  (:require [reagent.core :as r]
            [garden.units :as u]
            [re-frame.core :as rf]))

(defonce widget-state (atom {}))

(defn webcam [{:keys [width height divisor on-image] :as opts}]
  (r/create-class
   {:component-did-mount
    (fn [this]
      (swap! widget-state assoc :opts opts)
      (let [video (:video @widget-state)]
        (when (and video
                   (.-mediaDevices js/navigator)
                   (.. js/navigator -mediaDevices -getUserMedia))
          (.. js/navigator
              -mediaDevices
              (getUserMedia (clj->js {:video true}))
              (then (fn [stream]
                      (set! (.-src video) (.. js/window
                                              -URL
                                              (createObjectURL stream)))
                      (.play video)))))))
    :component-will-unmount #(reset! widget-state {})
    :reagent-render
    (fn [{:keys [upload-fn uploading]}]
      [:div.re-webcam {:style {:padding "10px"}}
       [:video {:ref #(swap! widget-state assoc :video %)
                :width width
                :height height
                :autoPlay true}]
       [:canvas {:ref #(swap! widget-state assoc :canvas %)
                 :style {:display :none}
                 :width width
                 :height height}]])}))

(rf/reg-fx
 :webcam/shot
 (fn [image-name]
   (let [{:keys [video canvas opts]} @widget-state
         context (.getContext canvas "2d")]
     (.drawImage context video 0 0 (:width opts) (:height opts))
     (set! (.. video -style -display) "none")
     (set! (.. canvas -style -display) "block")
     (swap! widget-state assoc-in [:images image-name :data-uri] (.toDataURL canvas "image/png"))
     (.toBlob canvas #(do (swap! widget-state assoc-in [:images image-name :blob] %)
                          (when-let [on-image-ev (:on-image opts)]
                            (rf/dispatch (into on-image-ev [image-name]))))))))

(rf/reg-event-fx
 :webcam/shot
 (fn [{db :db} [_ {:keys [name] :as meta}]]
   {:db (assoc-in db [:re-form/webcam :images name] meta)
    :webcam/shot name}))

(rf/reg-fx
 :webcam/clear-history
 (fn [image-name]
   (let [{:keys [video canvas]} @widget-state]
     (swap! widget-state update :images #(dissoc % image-name))
     (set! (.. video -style -display) "block")
     (set! (.. canvas -style -display) "none"))))

(rf/reg-event-fx
 :webcam/clear-history
 (fn [{db :db} [_ image-name]]
   {:db (update-in db [:re-form/webcam :images] dissoc image-name)
    :webcam/clear-history image-name}))

(rf/reg-cofx
 :webcam/images
 (fn [cofx] (assoc cofx :images (:images @widget-state))))
