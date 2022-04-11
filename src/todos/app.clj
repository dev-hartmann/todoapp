(ns todos.app
  (:require [todos.config :as config]
            [todos.server :as server]
            [todos.api :refer [api]])
  (:gen-class))

(defn -main
  [& args]
  (let [config (if (nil? args)
                 (config/load-default-config)
                 (config/load-config (first args)))]
    
    (server/start-server config api)))

(comment
  (-main)
  (server/stop-server)
  )


