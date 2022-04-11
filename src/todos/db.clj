(ns todos.db
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

(defn execute-db! [sql]
  (jdbc/execute! @db sql {:return-keys true
                          :builder-fn rs/as-unqualified-maps}))

(defn execute-one-db! [sql]
  (jdbc/execute-one! @db sql {:return-keys true
                          :builder-fn rs/as-unqualified-maps}))




(defn- create-todo-table []
  (execute-db! ["CREATE TABLE todo (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL)"]))



(defn- create-task-table []
  (execute-db! ["CREATE TABLE task (
  id SERIAL PRIMARY KEY,
  todo_id INT NOT NULL,
  name TEXT NOT NULL,
  CONSTRAINT fk_todo FOREIGN KEY(todo_id) REFERENCES todo(id))"]))


(comment
  (start-db (config/load-default-config))
  @db
  (create-todo-table)
  (create-task-table)
  (jdbc/execute! @db ["INSERT INTO tasks"]))