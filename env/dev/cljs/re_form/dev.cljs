(ns ^:figwheel-no-load re-form.dev
  (:require [re-form.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [re-frisk.core :refer [enable-re-frisk!]]
            [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3333/figwheel-ws"
  :jsload-callback (fn [_] (core/reload)))

(enable-re-frisk!)
(core/init!)
