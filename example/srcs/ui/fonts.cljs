(ns ui.fonts
  (:require
   [cljsjs.react]
   [reagent.core :as reagent]
   [garden.core :as garden]
   [garden.color :as c]
   [garden.units :as u]
   [re-frame.core :as rf]
   [clojure.string :as str]))

(defn style [gcss]
  [:style (garden/css gcss)])

(defn fonts []
  (let [fonts (str/split "Cabin|Exo+2|Lato|Mukta|Nunito|Quicksand|Roboto|Roboto+Condensed|Ubuntu" #"\|")
        font-idx (reagent/atom 0)
        next-font (fn [_]
                    (.log js/console @font-idx)
                    (if (>= @font-idx (dec (count fonts)))
                      (reset! font-idx 0)
                      (swap! font-idx inc)))]
    (fn []
      (let [font (nth fonts @font-idx)]
        [:div.pane
         (style [:.pane {:width (u/px 900)
                         :margin "0 auto"
                         :transition "all 2s ease-out"
                         :padding {:top (u/px 10)
                                   :left (u/px 10)
                                   :right (u/px 10)
                                   :bottom (u/px 10)}}
                 [:.click-me {:padding (u/px 10)
                              :border "1px solid #ddd" 
                              :cursor "pointer"}]
                 [:.letter
                  {:font-size (u/px 200)
                   :display "inline-block"
                   :vertical-align "top"
                   :width (u/px 200)
                   :line-height (u/px 200)
                   :text-align "center"
                   :border "1px solid #ddd"
                   :position "relative"}]])

         (style [:.font {:font-family font}])
         [:h1 "re-form"]

         [:div.click-me {:on-click next-font} "change"]
         [:br]
         [:br]
         [:div.font {:style {:font-size "20px"}} font]
         [:br]

         [:div.letter.font "A"]
         [:div.letter.font "B"]
         [:div.letter.font "G"]
         [:div.letter.font "C"]
         [:div.letter.font "D"]
         [:div.letter.font "E"]
         [:div.letter.font "F"]
         [:div.letter.font "R"]

         ])))
  )
