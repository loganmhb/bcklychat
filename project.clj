(defproject chatapp "0.1.0-SNAPSHOT"
  :description "Sample websocket chat app using sente + httpkit"
  :url "http://github.com/loganmhb/chatapp"
  :author "Logan Buckley"
  :aliases {"fig" ["run" "-m" "clojure.main" "script/figwheel.clj"]}
  :main chatapp.server
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [figwheel-sidecar "0.5.7" :scope "test"]
                 [compojure "1.5.1"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [ring "1.5.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.5.1"]
                 [com.taoensso/sente "1.10.0"]]
  :source-paths ["src/clj"]
  :plugins [[lein-cljsbuild "1.1.4"]]
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:main 'chatapp.core
                                   :asset-path "js"
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js"
                                   :verbose true}}]}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]}})
