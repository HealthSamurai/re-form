(ns re-form.inputs.tags-input-impl
  (:require [garden.units :as u]
            [re-form.inputs.common :as cmn]
            [reagent.core :as r]
            [clojure.set :as s]))

(defn tags-style
  [{:keys [radius w h h2 h3 selection-bg-color hover-bg-color border error-border]}]
  [:div.re-tags-container
   {:display "inline-block"
     :min-width "30em"
     :background-color :white
     :padding [[(u/px 3) (u/px 12)]]
     :border-radius (u/px 2)
     :line-height (u/px h2)
    :border border}
   [:&.empty-tags
    {:padding [[(u/px 4) (u/px 12)]]}]
   [:input {:border :none
            :width (u/px 100)
            :outline :none}]
   [:.tag
    {:border border
     :position :relative
     :display :inline-block
     :padding {:left (u/px-div w 2)
               :right (u/px w)}
     :margin-right (u/px w)}
    [:.cross
     {:position :absolute
      :cursor :pointer
      :font-size (u/px w)
      :top 0
      :right 0}]]])

(defn tag [label on-delete]
  [:span.tag label
   [:i.cross.material-icons
    {:on-click on-delete} "close"]])

(defn tags [{:keys [value on-change space-delimiter]}]
  (let [setgen #(-> % vector set)
        newtag (r/atom "")
        nodes (r/atom nil)
        inner-value (r/atom nil)
        add-tag (fn [] (when-not (empty? @newtag)
                         (on-change (s/union @inner-value (setgen @newtag)))
                         (reset! newtag "")))
        doc-click-listener
        (fn [e] (when (not (cmn/has-ancestor (.-target e) (:root-node @nodes)))
                  (add-tag)))
        keydown-listener
        (fn [e] (when (.includes
                       (apply array (cond-> [13 9 188] space-delimiter (conj 32)))
                       (.-keyCode e))
                  (.preventDefault e)
                  (add-tag)))

        field-click-listener
        (fn [e]
          (when (.isEqualNode (.-target e) (:root-node @nodes))
            (.focus (:input @nodes))))]

    (r/create-class
     {:component-did-mount
      (fn [this]
        (.addEventListener (:input @nodes) "keydown" keydown-listener)
        (.addEventListener js/document "click" doc-click-listener)
        (.addEventListener (:root-node @nodes) "click" field-click-listener))

      :component-will-unmount
      (fn [this]
        (.removeEventListener (:input @nodes) "keydown" keydown-listener)
        (.removeEventListener js/document "click" doc-click-listener)
        (.removeEventListener (:root-node @nodes) "click" field-click-listener))

      :reagent-render
      (fn [{:keys [value on-change]}]
        (reset! inner-value (set value))
        [:div.re-tags-container
         {:class (when (empty? value) :empty-tags)
          :ref (fn [this] (swap! nodes assoc :root-node this))}
         (for [v value] ^{:key v}
           [tag v #(on-change (s/difference (set value) (setgen v)))])
         [:input.new-tag {:type :text
                          :ref (fn [this] (swap! nodes assoc :input this))
                          :value @newtag
                          :on-change #(reset! newtag (.. % -target -value))}]])})))
