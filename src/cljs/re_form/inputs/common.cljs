(ns re-form.inputs.common
  (:require [garden.units :as u]))

(defn errors-div [errors]
  [:div.errors (map-indexed (fn [idx e] [:div.error {:key idx} e])
                            (or errors []))])

(defn label-wrapper-style
  [{:keys [m h h2 h3 selection-bg-color hover-bg-color border gray-color]}]
  [:* [:.re-label {:margin-bottom (u/px 2)}]
   [:.re-small-label {:font-size (u/px m)
                      :line-height (u/px* m 1.5)
                      :margin-top 0
                      :margin-bottom (u/px 2)
                      :color gray-color}]])

(defn f-child [parent-node classname]
  "Shortcut to get first child of node matching the classname"
  (aget (.getElementsByClassName parent-node classname) 0))

(defn has-ancestor [x node]
  "Check if node `x` has ancestor `node`"
  (when x
    (if (= (.-nodeName x) "body")
      false
      (or (.isEqualNode x node)
          (recur (.-parentElement x) node)))))

(defn scroll
  "Proper scrolling for custom dropdowns.

     container -- usually a div with a scrollbar
     elem -- currently 'active' child element that has to be visible
     direction -- :up if selection goes up(selected previous), :down otherwise

   Ex.
     (scroll suggestions option :down) -- call it when
       select option in suggesions div with down arrow"

  [container elem direction]

  (let [elem-top (.-offsetTop elem)
        elem-bot (+ elem-top (.-clientHeight elem))
        view-top (.-scrollTop container)
        view-bot (+ view-top (.-clientHeight container))]
    (case direction
      :down (when (> elem-bot view-bot)
              (set! (.-scrollTop container)
                    (- elem-bot (.-clientHeight container))))
      :up (when (< elem-top view-top)
            (set! (.-scrollTop container)
                  elem-top))
      nil)))
