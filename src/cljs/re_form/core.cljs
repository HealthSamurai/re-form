(ns re-form.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [garden.core :as garden]
            [clojure.string :as str]))

(defn errors-hint [errors path]
  (when-let [e false #_(get-in @errors path)]
    [:div.form-control-feedback (str/join ";" e)]))

(defn insert-by-path [m [k & ks :as path] value]
  (if ks
    (if (int? k)
      (assoc (or m []) k (insert-by-path (get m k) ks value))
      (assoc (or m {}) k (insert-by-path (get m k) ks value)))
    (if (int? k)
      (assoc (or m []) k value)
      (assoc (or m {}) k value))))

(rf/reg-event-db
 :re-form/change
 (fn [db [_ path value]]
   (insert-by-path db path value)))

(rf/reg-event-db
 :re-form/toggle-arr
 (fn [db [_ path value]]
   (let [arr (or (get-in db path) [])
         value (if (contains? (set arr) value)
                 (remove #(= value %) arr)
                 (conj arr value)) ]
     (insert-by-path db path value))))

(rf/reg-sub-raw
 :re-form/data
 (fn [db [_ path]] (reaction (get-in @db path))))

(defn input [{errors :errors desc :description
              multiple :multiple
              b-pth :base-path
              pth :path :as props}]
  (let [sub (rf/subscribe [:re-form/data (into b-pth pth)])
        on-change (fn [ev] (rf/dispatch [:re-form/change (into b-pth pth) (.. ev -target -value)]))]
    (fn [props]
      [:div
       [:input.form-control (merge (select-keys props [:placeholkder :class])
                                   {:type (or (:type props) "text") :value @sub  :on-change on-change})]
       [errors-hint errors pth]
       [:small.form-text.text-muted desc]]
      )))

(defn lookup [{errors :errors desc :description opts-subs :options
               label-fn :label-fn value-fn :value-fn
               b-pth :base-path pth :path :as props}]
  (let [sub (rf/subscribe [:re-form/data (into b-pth pth)])
        opts (rf/subscribe opts-subs)
        search-str (reagent/atom "")
        selected (reaction (first (filter #(= @sub (value-fn %)) @opts)))
        on-change #(rf/dispatch [:re-form/change (into b-pth pth) (value-fn %)])
        search-set (reaction (filterv #(str/includes?
                                        (str/lower-case (or (:name %) ""))
                                        (str/lower-case (or @search-str ""))) @opts))
        on-search #(reset! search-str (.. % -target -value))]
    (fn [_]
      [:div
       [:div.lookup
        [:div.lookup-value (label-fn @selected)]
        [:div.lookup-search-group
         [:input.form-control.lookup-search.mt-2.p-1
          {:type "text" :placeholder "Type to search" :on-change on-search}]
         [:div.opts
          (doall
           (for [o @search-set] ^{:key ((:value-fn props) o)}
             [:div.lookup-option.p-1 {:on-click #(on-change o)} (label-fn o)]))]
         [:hr]]]

       [errors-hint errors pth]
       [:small.form-text.text-muted desc]])
    ))


(defn select [{errors :errors opts :options desc :description
               desc-fn :desc-fn
               b-pth :base-path pth :path :as props}]
  (let [sub (rf/subscribe [:re-form/data (into b-pth pth)])]
    (fn []
      (let [on-change (fn [ev]
                        (let [selected (.. ev -target -value)
                              val (-> (filter #(= selected (:l %)) opts) first :v)]
                          (rf/dispatch [:re-form/change (into b-pth pth) val])))]
        (fn [props]
          [:div
           [:select.form-control {:on-change on-change}
            (doall
             (for [o opts] ^{:key (:v o)}
               [:option (:l o)]))]
           [errors-hint errors pth]
           [:small.form-text.text-muted (or desc (and desc-fn (desc-fn @sub)))]])))))


(defn textarea [{b-pth :base-path pth :path :as props}]
  (let [sub (rf/subscribe [:re-form/data (into b-pth pth)])]
    (fn []
      (let [on-change (fn [ev]
                        (rf/dispatch [:re-form/change (into b-pth pth) (.. ev -target -value)]))]
        (fn [props]
          [:textarea (merge (select-keys props [:placeholder :class])
                            {:type (or (:type props) "text") :value @sub  :on-change on-change})])))))

(defn static-text [{b-pth :base-path pth :path value-fn :value-fn :as props}]
  (let [form (rf/subscribe [:re-form/data b-pth])
        sub (rf/subscribe [:re-form/data (into b-pth pth)])]
    (fn [_]
      (let [value (if (and value-fn @form) (value-fn @form) @sub)]
        (rf/dispatch [:re-form/change (into b-pth pth) value])
        [:div.form-control-static value]))))

(defn datetime [{b-pth :base-path pth :path :as props}]
  (let [sub (rf/subscribe [:re-form/data (into b-pth pth)])]
    (fn []
      (let [on-change (fn [ev]
                        (rf/dispatch [:re-form/change (into b-pth pth) (.. ev -target -value)]))]
        (fn [props]
          [:input.form-control {:type "date" :value @sub  :on-change on-change}])))))


(defn has-errors? [errors path]
  #_(not (empty? (get-in @errors path))))


(defn style [gcss]
  [:style (garden/css gcss)])

(defn checkbox [{b-pth :base-path pth :path :as props}]
  (let [sub (rf/subscribe [:re-form/data (into b-pth pth)])
        label-fn (:label-fn props)
        value-fn (:value-fn props)
        on-change (fn [value]
                    (rf/dispatch [:re-form/toggle-arr (into b-pth pth) value]))]
    (fn [{opts :opts :as props}]
      [:div.checkbox-control
       (style
        [:.checkbox
         [:input {:display :inline-block
                  :vertical-align :top
                  :margin-top "5px" }]
         [:.service {:display :inline-block
                     :margin-left "10px" }] ])
       (doall
        (for [o opts]
          [:div.checkbox {:key (value-fn o)}
           [:label
            [:input.checkbox-box {:type (or (name (:type props)) "checkbox")
                                  :name (name (last pth))
                                  :value (value-fn o)
                                  :checked (contains? (set @sub) o)
                                  :on-change #(on-change o)}]
            (label-fn o)

            ]]))])))

(defn row [{errors :errors lbl :label desc :description
            multiple :multiple
            base-path  :base-path path :path inp :as :as opts}]
  (let [sub (rf/subscribe [:re-form/data (into base-path path)])
        remove (fn [pos]
                 (rf/dispatch [:re-form/change (into base-path path)
                               (vec (concat (subvec @sub 0 pos) (subvec @sub (inc pos))))]))
        add (fn [] (rf/dispatch [:re-form/change (into base-path path) (conj @sub "")]))]
    (fn [arg]
      [:div.form-group.row
       {:class (when (has-errors? errors path) "has-error")}
       [:label.col-sm-4.col-form-label lbl]
       [:div.col-8
        (-> [:div.row]
            (into
             (if multiple
               (reduce-kv (fn [acc i v]
                            (conj acc
                                  [:div.col-10 [inp (update opts :path conj i)]]
                                  [:div.col-1
                                   [:a.add-one.btn.btn-sm.btn-secondary
                                    {:on-click #(remove i) :title "Remove"} "-"]]))
                          [] @sub)
               [[:div.col-10 [inp opts]]]))
            (into
             (when multiple
               [[:div.col-10 [:a.add-one.btn.btn-block.btn-sm.btn-secondary {:on-click add
                                                                             :title "Add another"} "Add"]]])
             ))]])))

(defn submit-btn [submit-fn title]
  [:button.btn.btn-primary {:type "submit" :on-click submit-fn} title])

(defn cancel-btn [submit-fn title]
  [:button.btn.btn-secondary {:type "submit" :on-click submit-fn} title])
