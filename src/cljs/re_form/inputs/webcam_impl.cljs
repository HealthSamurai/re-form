(ns re-form.inputs.webcam-impl
  (:require [reagent.core :as r]
            [garden.units :as u]))

;; A widget to take webcam photos
;; `new-thumb` of `webcam` component is a function to process base64 thumbnails
;; (`divisor` times smaller by each axis)
;; `new-pic` of `webcam` component is a function to process blob images (fullsize)
;;
;; In `ui.webcam-page` example handlers are re-frame db events
;; to store base64 and blobs in db
;;
;; Using subscription `thumbs` component can show the images gallery (base64 thumbnails).

(defn webcam-style
  [{:keys [h h2 h3 selection-bg-color hover-bg-color border]}]
  [:*
   [:.re-webcam
    {:width (u/px 640)}
    [:.controls
     {:display :flex
      :justify-content :center
      :margin (u/px 10)}
     [:button {:margin (u/px 10)}]]]
   [:.thumbs
    {:display :flex
     :flex-wrap :wrap}
    [:.thumbnail
     {:display :inline-block
      :margin (u/px 10)
      :position :relative}
     [:.cross {:display :none}]
     [:&:hover
      [:.cross
       {:position :absolute
        :top 0
        :display :inline-block
        :cursor :pointer
        :color hover-bg-color
        :right (u/px 5)}]]]]])

(defn- thumbnail [data remove-fn]
  [:div.thumbnail
   [:img {:src data}]
   [:span.cross
    {:on-click remove-fn}
    "❌"]])

(defn thumbs [{:keys [thumbs remove-fn]}]
  [:div.thumbs
   (for [[i t] (map-indexed vector @thumbs)] ^{:key i}
     [thumbnail t (partial remove-fn i)])])

(defn- apply-display [nodes display]
  (doall (for [node nodes] (set! (.. node -style -display) display))))

(defn- hide [& nodes] (apply-display nodes "none"))
(defn- show [& nodes] (apply-display nodes "block"))

#_(defn- to-blob [data-uri]
    (let [splitted (.split data-uri ",")
          byte-string-conv (if (>= (-> (.split data-uri ",")
                                       (aget 0)
                                       (.indexOf "base64")) 0)
                             js/atob js/unescape)
          byte-string (byte-string-conv (aget splitted 1))
          mime-string (-> splitted (aget 0) (.split ":") (aget 1) (.split ";") (aget 0))
          ia (js/Uint8Array. (.-length byte-string))]
      (for [i (range (.-length byte-string))]
        (aset ia i (.charCodeAt byte-string i)))
      (js/Blob. #js [ia] (clj->js {:type mime-string}))))

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
          (.addEventListener snap "click"
                             (fn []
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
                                 (.toBlob canvas new-pic)
                                 (hide canvas accept discard)
                                 (show video snap))))))

      :reagent-render
      (fn [{:keys [upload-fn uploading]}]
        [:div.re-webcam
         [:div.controls
          [:button.btn.btn-primary {:ref #(swap! state assoc :snap %)
                                    :style {:display :block}} "Take a shot"]
          [:button.btn.btn-success {:ref #(swap! state assoc :accept %)
                                    :style {:display :none}} "Ok"]
          [:button.btn {:ref #(swap! state assoc :discard %)
                        :style {:display :none}} "Discard"]]
         [:div.visuals
          [:video {:ref #(swap! state assoc :video %)
                   :width width
                   :height height
                   :autoPlay true}]
          [:canvas {:ref #(swap! state assoc :canvas %)
                    :style {:display :none}
                    :width width
                    :height height}]]])})))
