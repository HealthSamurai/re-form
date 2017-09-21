(ns re-form.inputs.common)

(defn errors-div [errors]
  [:div.errors (map-indexed (fn [idx e] [:div.error {:key idx} e])
                            (or errors []))])
