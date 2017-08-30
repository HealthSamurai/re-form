(ns re-form.checkbox)

;; [:.re-checkbox
;;  [:.re-box {:width (u/px 20)
;;             :display "inline-block"
;;             :height (u/px 20)
;;             :border "1px solid #ddd"}]
;;  [:&.re-checked
;;   [:.re-box {:background-color "#666"}]]]

;; (defn check-box [{pth :path lbl :label}]
;;   (let [sub (rf/subscribe [:reform/data pth])
;;         on-change (fn [ev] (rf/dispatch [:re-form/change pth (not @sub)]))
;;         on-key-press  (fn [ev] (when (= 32 (.. ev -which)) (on-change ev)))]
;;     (fn [props]
;;       [:a.re-checkbox
;;        {:href "javascript:void()"
;;         :class (when @sub "re-checked")
;;         :on-key-press on-key-press
;;         :on-click on-change}
;;        [:span.re-box] (when lbl [:span.re-label lbl])])))
