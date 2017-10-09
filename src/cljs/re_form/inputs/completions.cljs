(ns re-form.inputs.completions)

(defn- search-backwards [line pos regex]
  (loop [p pos]
    (let [cur-pos (dec p)
          char (.charAt line cur-pos)]
      (if (or (neg? cur-pos) (.test #"\s" char))
        nil
        (if (.test regex (.charAt line cur-pos))
          cur-pos
          (recur cur-pos))))))

(defn- options-sw [dict regex word]
  (dict (clojure.string/replace word regex "")))

(defn complete-startswith [regex dict cm option]
  (let [cursor (.getCursor cm)
        end-char (.-ch cursor)
        line (.getLine cm (.-line cursor))
        start-char (search-backwards line end-char regex)
        to-pos #(.Pos js/CodeMirror line %)
        from {:line (.-line cursor) :ch start-char}
        to {:line (.-line cursor) :ch end-char}
        word (.getRange cm (clj->js from) (clj->js to))]
    (when start-char
      (when-let [lst (options-sw dict regex word)]
        (clj->js
         {:list lst
          :from from
          :to to})))))

(defn- options-incr [words value-fn regex word]
  (let [cropped-word (clojure.string/replace word regex "")
        extract-fn (if (map? (first words)) :text identity)]
    (filter #(-> %
                 extract-fn
                 value-fn
                 (clojure.string/starts-with? cropped-word)) words)))

(defn complete-incremental [regex words value-fn cm option]
  (let [cursor (.getCursor cm)
        end-char (.-ch cursor)
        line (.getLine cm (.-line cursor))
        start-char (search-backwards line end-char regex)
        to-pos #(.Pos js/CodeMirror line %)
        from {:line (.-line cursor) :ch start-char}
        to {:line (.-line cursor) :ch end-char}
        word (.getRange cm (clj->js from) (clj->js to))]
    (when start-char
      (when-let [lst (options-incr words value-fn regex word)]
        (clj->js
         {:list lst
          :from from
          :to to})))))
