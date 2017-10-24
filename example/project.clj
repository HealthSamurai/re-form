(defproject ui "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.521" :scope "provided"]
                 [cljsjs/react-with-addons "15.4.2-2"]
                 [reagent "0.6.1" :exclusions [cljsjs/react]]
                 [re-frame "0.9.2" :exclusions [cljsjs/react]]
                 [reagent-utils "0.2.1" :exclusions [cljsjs/react]]
                 [re-frisk "0.4.4" :exclusions [cljsjs/react]]
                 [binaryage/devtools "0.9.2"]
                 [cljs-http "0.1.43"]
                 [hiccup "1.0.5"]
                 [garden "1.3.2"]
                 [route-map "0.0.4"]
                 [bouncer "1.0.1"]]

  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.6"]
            [lein-ancient "0.6.10"]]

  :min-lein-version "2.5.0"

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["../src"]
  :resource-paths ["resources"]

  :figwheel
  {:http-server-root "public"
   :server-port 3002
   :nrepl-port 7005
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                      "cider.nrepl/cider-middleware"]
   :css-dirs ["resources/public/css"]}


  :profiles {:dev {:repl-options {:init-ns ui.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.5.1"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar "0.5.10-SNAPSHOT"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                  [cider/cider-nrepl "0.15.0"]
                                  [pjstadig/humane-test-output "0.8.1"]]

                   :source-paths ["srcs" "srcc" "../src/cljc" "../src/cljs" "env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.10-SNAPSHOT"]]

                   :env {:dev true}

                   :cljsbuild
                   {:builds
                    {:ui {:source-paths ["srcs" "srcc" "env/dev/cljs" "../src/cljs"]
                          :compiler
                          {:main "ui.dev"
                           :asset-path "/js/out"
                           :output-to "resources/public/js/ui.js"
                           :output-dir "resources/public/js/out"
                           :source-map true
                           :optimizations :none
                           :pretty-print  true

                           ;; :foreign-libs []

                           ;; :externs []
                           ;; :externs []

                           }}}}}

             :prod {:cljsbuild

                    {:builds
                     {:ui {:source-paths ["srcs" "env/prod/cljs" "../src/cljc" "../src/cljs"]
                           :verbose true
                           :compiler
                           {:main "ui.prod"
                            :verbose true
                            ;; :foreign-libs []
                            ;; :externs []
                            :output-to "build/js/ui.js"
                            ;; :output-dir "build/js/out"
                            :optimizations :advanced
                            :pretty-print  false}}}}}})
