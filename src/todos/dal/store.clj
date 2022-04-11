(ns todos.dal.store
  (:require [todos.dal.db :as db]
            [honey.sql :as sql]
            [honey.sql.helpers :as hh]))

;; transform fns

(defn- entry->todo [entry]
  {:id          (:todo/id entry)
   :name        (:todo/name entry)
   :description (:todo/description entry)})

(defn- entry->task [entry]
  {:id   (:task/id entry)
   :name (:task/name entry)})

(defn- result->todos [entries]
  (let [todo  (entry->todo (first entries))
        tasks (reduce #(conj %1 (entry->task %2)) [] entries)]
    (assoc todo :tasks tasks)))

;; public access fns

(defn get-all-todos []
  (let [result (db/execute-db! (-> (hh/select :*)
                                   (hh/from :todo)
                                   (hh/inner-join :task [:= :todo.id :task.todo_id])
                                   sql/format))]
    (when-not (empty? result)
      (->> result
           (group-by :todo/id)
           (map #(result->todos (second %)))
           (into [])))))

(defn get-todo [id]
  (let [result (db/execute-db! (-> (hh/select :todo/* :task/*)
                                   (hh/from :todo)
                                   (hh/inner-join :task [:= :todo.id :task.todo_id])
                                   (hh/where [:= :todo.id :?id])
                                   (sql/format {:params {:id id}})))]
    (when-not (empty? result)
      (result->todos result))))

(defn create-todo [todo]
  (db/execute-one-db! (-> (hh/insert-into :todo)
                          (hh/values [todo])
                          (sql/format {:pretty true}))))

(defn update-todo [id {:keys [name description]}]
  (db/execute-one-db! (-> (hh/update :todo)
                          (hh/set {:name name
                                   :description description})
                          (hh/where [:= :id :?id])
                          (sql/format {:params {:id id}}))))

(defn delete-todo [id]
  (db/execute-one-db! (-> (hh/delete-from :todo)
                          (hh/where [:= :id :?id])
                          (sql/format {:params {:id id}}))))

(comment
  (create-todo {:name "test from repl" :description "tested from repl"})
  (get-todo 1)
  (get-all-todos)
  (delete-todo 4)
  (update-todo 3 {:name "updated todo" :description "updated description"}))