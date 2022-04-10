(ns todos.db
  (:require [next.jdbc :as jdbc]))

(defonce db (atom nil))

(defn create-db-config [config]
  (let [db-config (:db config)
        name      (:name db-config)
        host      (or (:host db-config) "localhost")
        user      (:user db-config)
        password  (:password db-config)]
    {:dbtype "postgresql"
     :dbname name
     :host host
     :user user
     :password password}))

(defn start-db [config]
  (let [db-config (create-db-config config)]
    (println (str "Connecting to database ..."))
    (reset! db (jdbc/get-datasource db-config))))

(defn stop-db []
  (println (str "Disconnecting from database ..."))
  (when @db
    (.close @db)))