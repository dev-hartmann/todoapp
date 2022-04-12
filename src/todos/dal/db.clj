(ns todos.dal.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [todos.config :as config]))

(defonce db (atom nil))

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
    (reset! db (jdbc/get-datasource db-config))))

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


(defn- create-todo-table []
  (execute-db! ["CREATE TABLE todo (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL)"]))



(defn- create-task-table []
  (execute-db! ["CREATE TABLE task (
  id SERIAL PRIMARY KEY,
  todo_id INT NOT NULL REFERENCES todo ON DELETE CASCADE,
  name TEXT NOT NULL,
  CONSTRAINT fk_todo FOREIGN KEY(todo_id) REFERENCES todo(id))"]))

(defn- drop-db []
  (execute-db! ["DROP TABLE IF EXISTS todo CASCADE"])
  (execute-db! ["DROP TABLE IF EXISTS task CASCADE"]))


(comment
  (start-db (config/load-default-config))
  (drop-db)
  (def con (.getConnection @db))
  con
  (.stop con)
  (stop-db)
  @db
  (create-todo-table)
  (create-task-table)
  (jdbc/execute! @db ["INSERT INTO tasks"]))