(ns todos.server
  (:require [ring.adapter.jetty :as jetty]))

(defonce server (atom nil))

(defn start-server [config handlers]
  (let [port (:port config)
        jetty (jetty/run-jetty handlers {:port port
                               :join? false})]
    (println (str "Starting server on port " (:port config)) " ...")
    (reset! server jetty)))

(defn stop-server []
  (println (str "Server shutting down ..."))
  (when @server
    (.stop @server))
  (reset! server nil))