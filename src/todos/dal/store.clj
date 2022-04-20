(ns todos.dal.store
  (:require [todos.dal.db :as db]
            [honey.sql :as sql]
            [honey.sql.helpers :as hh]))

;; transform fns
(defn- remove-nil-values [entries]
  (map #(into {} (remove (fn [[_ v]] (nil? v)) %)) entries))

(defn- entry->todo [entry]
  {:id          (:todo/id entry)
   :name        (:todo/name entry)
   :description (:todo/description entry)})

(defn- entry->task [entry]
  (when (and (:task/id entry)
             (:task/name entry))
    {:id   (:task/id entry)
     :name (:task/name entry)}))

(defn- entries->tasks [entries]
  (let [tasks (reduce #(conj %1 (entry->task %2)) [] entries)]
    (if tasks
      (into [] (filter #(not (nil? %1)) tasks))
      [])))

(defn- result->todos [entries]
  (let [cleaned-entries (remove-nil-values entries)
        todo            (entry->todo (first cleaned-entries))
        tasks           (entries->tasks cleaned-entries)]
    (assoc todo :tasks tasks)))

(defn task-exists-for-todo? [todo-id task-id]
  (let [result (db/execute-one-db! (-> (hh/select :%count.*)
                                       (hh/from :task)
                                       (hh/where [:= :todo_id :?todo_id]
                                                 [:= :id :?id])
                                       (sql/format {:params {:id      task-id
                                                             :todo_id todo-id}})))]
    (> (:count result) 0)))


(defn todo-exists? [id]
  (let [result (db/execute-one-db! (-> (hh/select :%count.*)
                                       (hh/from :todo)
                                       (hh/where [:= :id :?id])
                                       (sql/format {:params {:id id}})))]
    (> (:count result) 0)))

;; public access fns

;; todo CRUD
(defn get-all-todos []
  (let [result (db/execute-db! (-> (hh/select :*)
                                   (hh/from :todo)
                                   (hh/left-join :task [:= :todo.id :task.todo_id])
                                   sql/format))]
    (when-not (empty? result)
      (->> result
           (group-by :todo/id)
           (map #(result->todos (second %)))
           (into [])))))

(defn get-todo [id]
  (let [result (db/execute-db! (-> (hh/select :*)
                                   (hh/from :todo)
                                   (hh/left-join :task [:= :todo.id :task.todo_id])
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
                          (hh/set {:name        name
                                   :description description})
                          (hh/where [:= :id :?id])
                          (sql/format {:params {:id id}}))))

(defn delete-todo [id]
  (let [result (db/execute-one-db! (-> (hh/delete-from :todo)
                                       (hh/where [:= :id :?id])
                                       (sql/format {:params {:id id}})))]
    (:id result)))

;; task CRUD
(defn get-tasks-for-todo [id]
  (let [result (db/execute-db! (-> (hh/select :task/*)
                                   (hh/from :task)
                                   (hh/where [:= :todo_id :?id])
                                   (sql/format {:params {:id id}})))]
    (when-not (empty? result)
      (map #(entry->task %) result))))

(defn add-task [todo-id task]
  (db/execute-one-db! (-> (hh/insert-into :task)
                          (hh/values [(assoc task :todo_id todo-id)])
                          (sql/format))))

(defn delete-task [task-id]
  (let [result (db/execute-one-db! (-> (hh/delete-from :task)
                                       (hh/where [:= :id :?id])
                                       (sql/format {:params {:id task-id}})))]
    (:id result)))

(defn update-task [task-id {:keys [name]}]
  (db/execute-one-db! (-> (hh/update :task)
                          (hh/set {:name name})
                          (hh/where [:= :id :?id])
                          (sql/format {:params {:id task-id}}))))
