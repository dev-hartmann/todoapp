(ns todos.app
  (:require [todos.config :as config]
            [todos.server :as server]
            [todos.routes :refer [app-routes]])
  (:gen-class))

(defn -main
  [& args]
  (let [config (if (nil? args)
                 (config/load-default-config)
                 (config/load-config (first args)))]
    
    (server/start-server config app-routes)))

(comment
  (-main)
  (server/stop-server)
  )


