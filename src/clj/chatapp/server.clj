(ns chatapp.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.core.async :refer [go-loop <!]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as resp]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit
             :refer [get-sch-adapter]]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter))]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def connected-uids connected-uids))

(defroutes app-routes
  (GET "/" [] (resp/resource-response "public/index.html"))
  (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-keyword-params
      wrap-params))


(defn handle-messages []
  (go-loop []
    (let [uids (:any @connected-uids)
          msg (<! ch-chsk)]
      (doseq [uid uids]
        (chsk-send! uid (:event msg)))
      (recur))))

(defn -main []
  (handle-messages)
  (org.httpkit.server/run-server app {:port 3000}))
