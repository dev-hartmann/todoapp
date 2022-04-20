(ns todos.dal.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [ragtime.jdbc :as rjdbc]
            [todos.config :as config]
            [ragtime.repl :as r]))

(defonce db (atom nil))

(defn- create-ragtime-config []
  {:datastore (rjdbc/sql-database {:connection  (.getConnection @db)})
   :migrations (rjdbc/load-resources "migrations")})

(defn- run-migrations! []
  (let [config (create-ragtime-config)]
    (r/migrate config)))
(defn create-db-config [config]
  (let [db-config (:db config)
        name      (:name db-config)
        host      (or (:host db-config) "localhost")
        user      (:user db-config)
        password  (:password db-config)]
    {:dbtype   "postgresql"
     :dbname   name
     :host     host
     :user     user
     :password password}))

(defn start-db [config]
  (let [db-config (create-db-config config)]
    (println (str "Connecting to database ..."))
    (reset! db (jdbc/get-datasource db-config))
    (run-migrations!)))

(defn stop-db []
  (println (str "Disconnecting from database ..."))
  (when @db
    (-> @db
        (.getConnection)
        (.close))
    (reset! db nil)))

(defn execute-db! [sql]
  (jdbc/execute! @db sql {:return-keys true
                          :builder-fn  rs/as-maps}))

(defn execute-one-db! [sql]
  (jdbc/execute-one! @db sql {:return-keys true
                              :builder-fn  rs/as-unqualified-maps}))
