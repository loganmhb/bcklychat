(ns chatapp.core
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer [go go-loop]])
  (:require [reagent.core :as r]
            [cljs.core.async :as async :refer [>! <! put! chan]]
            [taoensso.sente :as sente :refer [cb-success?]]))

(enable-console-print!)

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
                                  {:type :auto})]
  (def chsk       chsk)
  (def chsk-ch    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state))

(defonce app-state
  (r/atom {:messages []
           :current-user "logan"}))

(defn add-message! [message]
  (swap! app-state #(update % :messages conj message)))

(defn add-message-from-user! [text]
  (chsk-send! [:message/send {:text text :author (:current-user @app-state)}]))

(defn recv-event [[event-type event-data]]
  (case event-type
    :message/send (add-message! event-data)
    nil))

(defn handle-event [[event-type event-data]]
  (case event-type
    :chsk/recv (recv-event event-data)
    nil))

(defn listen-for-events! []
  (go-loop [msg (<! chsk-ch)]
    (handle-event (:event msg))
    (recur (<! chsk-ch))))

(defn message [m]
  [:li
   [:span {:class "username"} (:author m)]
   [:p {:class "message"} (:text m)]])

(defn new-message []
  (let [val (r/atom "")]
    (fn []
      [:div
       [:input {:type "text"
                :value @val
                :on-change #(reset! val (-> % .-target .-value))}]
       [:button {:on-click (fn [event]
                             (do (add-message-from-user! @val)
                                 (reset! val "")))}
        "send"]])))

(defn message-list []
  [:div
   [:h1 "Messages"]
   [:ul
    (for [m (:messages @app-state)]
      ^{:key m} [message m])
    [new-message]]])

(defn start []
  (listen-for-events!)
  (r/render-component
   [message-list]
   (.getElementById js/document "app")))

(start)
