(ns ui.styles-page
  (:require [re-form.core :as form]
            [garden.core :as garden]
            [ui.routes :as ui-routes]
            [re-form.inputs.completions :as completions]
            [re-form.inputs :as w]
            [garden.color :as c]
            [garden.units :as u]))

(def colors {1 1
             2 0.5
             3 0.2
             4 0.1
             5 0.05})

(def h 8)
(def w 6)
(def clr [52 59 81])

(defn block-style [h w clr]
  {:line-height (u/px* h 4)

   :padding {:top (u/px- (/ h 2) 1)
             :bottom (u/px- (/ h 2) 1)}

   :border {:width (u/px 1)
            :style "solid"
            :color (c/rgba (conj clr 0.2))}})

(defn inner-block-style [h w clr]
  {:line-height (u/px* h 3)
   :padding {:top (u/px- (/ h 2) 1)
             :bottom (u/px- (/ h 2) 1)}
   :background {:color (c/rgba (conj clr 0.1))}
   :border {:width (u/px 1)
            :style "solid"
            :color (c/rgba (conj clr 0.2))}})

(defn icon-style [h w clr]
  {:width (u/px* h 3)
   :height (u/px* h 3)
   :display "inline-block"
   :text-align "center"
   :color (c/rgba (conj clr 0.5))})

(defn styles-page []
  [:div
   [:h1 "System"]
   [:h3 "Primitives:"]

   (for [i (range 1 6)]
     (let [clr (str "rgba(52, 59, 81, " (get colors i)")")]
       [:div {:key (str "div1" i)}
        [:div {:key "a"}
         [:span [:b "Height: "] (str (* 8 i) "px")]
         " "
         [:span [:b "Color: "] clr]]
        [:div {:key (str "div1" i)
               :style {:background-color clr 
                       :margin-bottom "8px" 
                       :height (str (* 8 i))}}]]))

   [:style
    (garden/css
     [:*
      [:.box (block-style h w clr)]
      [:.space {:margin-right (u/px w)}]
      [:.space2 {:margin-right (u/px* w 2)}]
      [:.icon (icon-style h w clr)]
      [:.inner-box (inner-block-style h w clr)]
      ])]

   [:div.box
    [:span.space]
    [:span.icon "▾"]
    [:span.space]
    "I'm a box"
    [:span.space]
    [:span.inner-box
     [:span.space2]
     "I'm inner"
     [:span.space]
     [:span.icon "✕"]
     [:span.space]]

    [:span.space2]

    [:span.inner-box
     [:span.space2]
     "I'm inner"
     [:span.space]
     [:span.icon "✕"]
     [:span.space]]

    ]

   ;; [:h1 "Styles"]
   #_(for [[k v] form/default-base-consts]
     [:div [:b (name k)]  " " (pr-str v)])])

(ui-routes/reg-page
 :textarea {:title "Styles"
            :w 1.1 
            :cmp styles-page})
