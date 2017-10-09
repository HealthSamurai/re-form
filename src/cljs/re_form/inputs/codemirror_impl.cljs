(ns re-form.inputs.codemirror-impl
  (:require [reagent.core :as r]))

(def codemirror-style
  [:.text :.CodeMirror
   {
    :width "100%"
    :height "auto"
    :font-family  "sans-serif"
    :letter-spacing ".01rem;"
    :font-weight 400
    :font-style "normal;"
    :color "rgba(0,0,0,.8)"
    :font-size "18px;"
    :border "2px solid #eee"
    :line-height 1.58}
   ])

(defn- *codemirror-input [{:keys [value on-change complete-fn]}]
  (let [cm-atom (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [editor-opts
              (cond-> {:viewportMargin (.-Infinity js/window)
                       :lineWrapping true}
                (some? complete-fn) (assoc :hintOptions {:hint complete-fn}))
              editor
              (.fromTextArea
               js/CodeMirror (-> (r/dom-node this)
                                 (.getElementsByClassName "cm-textarea")
                                 array-seq first)
               (clj->js editor-opts))]
          (.on editor "change" (fn [cm _] (on-change (.getValue cm))))
          (when (some? complete-fn)
            (.on editor "keyup"
                 (fn [cm _] (when-not (.. cm -state -completionActive)
                              (.. js/CodeMirror -commands
                                  (autocomplete cm nil (clj->js {:completeSingle false})))))))
          (reset! cm-atom editor)))

      :component-will-receive-props
      (fn [this next-props]
        (when-let [cm @cm-atom]
          (let [nvalue (-> next-props second :value)]
            (when (not= (.getValue cm) nvalue)
              (.setValue cm (or nvalue ""))))))

      :reagent-render
      (fn [{:keys [value on-change complete-fn]}]
        [:div.cm-wrapper
         [:textarea.cm-textarea]])})))

(defn codemirror-input [& args]
  (if (exists? js/CodeMirror)
    (apply *codemirror-input args)
    (throw
     (js/Error. "CodeMirror must be included in the document in order to use this input!"))))
