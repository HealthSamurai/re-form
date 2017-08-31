(defproject re-form "0.0.1-SNAPSHOT"
  :description "form builder for re-frame"
  :url "https://github.com/HealthSamurai/re-form"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.0"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.671" :scope "provided"] 
                 [cljsjs/react-with-addons "15.6.1-0"]
                 [reagent "0.7.0" :exclusions [cljsjs/react]]
                 [reagent-utils "0.2.1" :exclusions [cljsjs/react]]
                 [re-frame "0.9.4" :exclusions [cljsjs/react]]
                 [re-frisk "0.4.5" :exclusions [cljsjs/react]]
                 [binaryage/devtools "0.9.4"]
                 [hiccup "1.0.5"]
                 [garden "1.3.2"]
                 [matcho "0.1.0-RC5"]]

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :plugins [[cider/cider-nrepl "0.15.0"]
            [lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.6"]]

  :resource-paths ["resources"]

  :profiles {:dev {:repl-options {:init-ns re-form.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.5.1"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar "0.5.10-SNAPSHOT"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.8.1"]]

                   :source-paths ["src" "env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.12"]]

                   :env {:dev true}

                   :cljsbuild
                   {:builds
                    {:re-form
                     {:source-paths ["src" "env/dev/cljs"]
                      :compiler
                      {:main "re-form.dev"
                       :asset-path "/js/out"
                       :output-to "resources/public/js/re-form.js"
                       :output-dir "resources/public/js/out"
                       :source-map true
                       :optimizations :none
                       :pretty-print  true
                       :externs ["resources/codemirror-externs.js"]}}}}}

             :prod {:cljsbuild
                    {:builds
                     {:re-form
                      {:source-paths ["src" "env/prod/cljs"]
                       :verbose true
                       :compiler
                       {:main "re-form.prod"
                        :verbose true
                        :parallel-build true
                        :output-to "build/public/js/re-form.js"
                        :optimizations :advanced
                        :pretty-print  false
                        :externs ["resources/codemirror-externs.js"]}}}}}})
