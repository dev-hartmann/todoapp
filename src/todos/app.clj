(ns todos.app
  (:require [todos.config :as config]
            [todos.server :as server]
            [todos.handlers :refer [base-handler]])
  (:gen-class))

(defn -main
  [& args]
  (let [config (if (nil? args) 
                 (config/load-default-config)
                 (config/load-config (first args)))
        handlers  base-handler]
    (server/start-server config base-handler)))

(comment
  (-main)
  (server/stop-server)
  )


