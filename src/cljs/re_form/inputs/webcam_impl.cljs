(ns re-form.inputs.webcam-impl
  (:require [reagent.core :as r]))


;; A widget to take webcam photos
;; `new-thumb` of `webcam` component is a function to process base64 thumbnails
;; (`divisor` times smaller by each axis)
;; `new-pic` of `webcam` component is a function to process base64 images (fullsize)
;;
;; In `ui.webcam-page` example handlers are re-frame db events to store base64 in db
;;
;; Using subscription `thumbs` component can show the images gallery.

(defn webcam-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:.re-webcam {}])

(defn thumbs [thumbs]
  [:div
   (for [t @thumbs]
     [:img {:src t
            :key t}])])

(defn- apply-display [nodes display]
  (doall (for [node nodes] (set! (.. node -style -display) display))))

(defn- hide [& nodes] (apply-display nodes "none"))
(defn- show [& nodes] (apply-display nodes "block"))

(defn webcam [{:keys [width height divisor new-thumb new-pic] :or {divisor 10}}]
  (let [state (r/atom {})]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [video (:video @state)
              canvas (:canvas @state)
              snap (:snap @state)
              context (.getContext canvas "2d")
              accept (:accept @state)
              discard (:discard @state)]
          (if (and (.-mediaDevices js/navigator)
                   (.. js/navigator -mediaDevices -getUserMedia))
            (.. js/navigator
                -mediaDevices
                (getUserMedia (clj->js {:video true}))
                (then (fn [stream]
                        (set! (.-src video) (.. js/window
                                                -URL
                                                (createObjectURL stream)))
                        (.play video)))))
          (.addEventListener snap "click" (fn []
                                              (.drawImage context video 0 0 width height)
                                              (show canvas accept discard)
                                              (hide video snap)))
          (.addEventListener discard "click"
                             (fn []
                               (hide canvas accept discard)
                               (show video snap)))
          (.addEventListener accept "click"
                             (fn []
                               (let [small-canvas (.createElement js/document "canvas")
                                     ctx (.getContext small-canvas "2d")
                                     w (quot width divisor)
                                     h (quot height divisor)]
                                 (set! (.-width small-canvas) w)
                                 (set! (.-height small-canvas) h)
                                 (.drawImage ctx canvas 0 0 w h)
                                 (new-thumb (.toDataURL small-canvas "image/png"))
                                 (new-pic (.toDataURL canvas "image/png"))
                                 (hide canvas accept discard)
                                 (show video snap))))))

      :reagent-render
      (fn [{:keys [upload-fn uploading]}]
        [:div
         [:button {:ref #(swap! state assoc :snap %)
                   :style {:display :block}} "Take a shot"]
         [:button {:ref #(swap! state assoc :accept %)
                   :style {:display :none}} "Ok"]
         [:button {:ref #(swap! state assoc :discard %)
                   :style {:display :none}} "Discard"]
         [:video {:ref #(swap! state assoc :video %)
                  :width width
                  :height height
                  :autoPlay true}]
         [:canvas {:ref #(swap! state assoc :canvas %)
                   :style {:display :none}
                   :width width
                   :height height}]])})))
