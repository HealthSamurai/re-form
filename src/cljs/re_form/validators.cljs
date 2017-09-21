(ns re-form.validators
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [clojure.string :as str]
            [bouncer.validators :as v]
            [cljs.core.async :refer [<! >! chan alts! timeout]]))

(defn not-blank [& {:keys [message] :or {message "Should not be blank"}}]
  (fn [v _]
    (when (str/blank? v) message)))

(defn email [& {:keys [message] :or {message "Not a valid email"}}]
  (fn [v _]
    (when-not (v/email v) message)))

(defn regex [rx & {:keys [message] :or {message (str "Should match regex: " rx)}}]
  (fn [v _]
    (when-not (and v (re-matches rx v)) message)))

(defn min-count [n count-f & {:keys [message] :or {message (str "Must be >= " n)}}]
  (fn [v _]
    (when (< (count-f v) n) message)))

(defn debounce [in ms]
  (let [out (chan)]
    (go-loop [last-val nil]
      (let [val (if (nil? last-val) (<! in) last-val)
            timer (timeout ms)
            [new-val ch] (alts! [in timer])]
        (condp = ch
          timer (do (>! out val) (recur nil))
          in (if new-val (recur new-val)))))
    out))

(defn debounced-async-validator [debounce-treshold validate-fn]
  (let [in (chan)
        ch (debounce in debounce-treshold)]

    (fn [v path]
      (let [errors-ch (chan)]
        (go (when v (>! in [v path]))
            (let [[val path] (<! ch)]
              (>! errors-ch (<! (validate-fn val path)))))
        errors-ch))))
