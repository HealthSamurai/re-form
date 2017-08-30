(ns re-form.switchbox)

;; (defn switch-box [{pth :path lbl :label}]
;;   (let [sub (rf/subscribe [:reform/data pth])
;;         on-change (fn [ev] (rf/dispatch [:re-form/change pth (not @sub)]))
;;         on-key-press  (fn [ev] (when (= 32 (.. ev -which)) (on-change ev)))]
;;     (fn [props]
;;       [:a.re-switch
;;        {:href "javascript:void()"
;;         :class (when @sub "re-checked")
;;         :on-key-press on-key-press
;;         :on-click on-change}
;;        [:span.re-switch-span [:span.re-box]] (when lbl [:span.re-label lbl])])))
