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
           :current-user nil}))

(defn add-message! [message]
  (swap! app-state #(update % :messages conj message)))

(defn add-message-from-user! [text]
  (chsk-send! [:message/send {:text text :author (:current-user @app-state)}]))

(defn handle-event [[event-type event-data]]
  (case event-type
    :chsk/recv (handle-event event-data)
    :message/send (add-message! event-data)
    nil))

(defn listen-for-events! []
  (go-loop [msg (<! chsk-ch)]
    (handle-event (:event msg))
    (recur (<! chsk-ch))))

(defn message [m]
  [:li
   [:span.username (:author m)]
   [:p.message (:text m)]])

(defn text-input [input-name on-submit]
  (let [val (r/atom "")]
    (fn []
      [:div
       [:input {:type "text"
                :name input-name
                :value @val
                :default-value "Type message here"
                :on-change #(reset! val (-> % .-target .-value))
                :on-key-down #(case (.-which %)
                                13 (do (on-submit @val)
                                       (reset! val ""))
                                nil)}]])))

(defn new-message []
  (text-input "new-message" (fn [text] (add-message-from-user! text))))

(defn message-list []
  [:div
   [:p (str "Logged in as " (:current-user @app-state))]
   [:button {:on-click #(swap! app-state assoc :current-user nil)}
    "Log out"]
   [:h1 "Messages"]
   [:ul.message-list
    (for [m (:messages @app-state)]
      ^{:key m} [message m])
    [new-message]]])
;;TODO: play sound when message comes in
(defn choose-username []
  [:div
   [:p "Choose a username:"]
   [(text-input "new-username" (fn [text]
                                 (swap! app-state assoc :current-user text)))]])

(defn chat []
  [:div.app
   (if (:current-user @app-state)
     [message-list]
     [choose-username])])

(defn start []
  (listen-for-events!)
  (r/render-component
   [chat]
   (.getElementById js/document "app")))

(start)
