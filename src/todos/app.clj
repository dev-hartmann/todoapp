(ns todos.app
  (:require [todos.config :as config]
            [todos.server :as server]
            [todos.dal.db :as db]
            [todos.api :refer [api]])
  (:gen-class))

(defn- add-shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(do
                                (println "Shutting down with grace...")
                                (db/stop-db)
                                (server/stop-server)))))

(defn -main [& args]
  (println args)
  (let [config (if (nil? args)
                 (config/load-default-config)
                 (config/load-config (first args)))]
    (add-shutdown-hook)
    (db/start-db config)
    (server/start-server config api)))

(defn stop []
  (println "Stopping...")
  (db/stop-db)
  (server/stop-server))

(comment
  (-main)
  (stop)
  (db/stop-db)
  (server/stop-server))

