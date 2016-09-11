(require '[cljs.build.api :as cljs])

(cljs/build "src/cljs" {:output-to "resources/public/js/main.js"
                        :optimizations :advanced})
