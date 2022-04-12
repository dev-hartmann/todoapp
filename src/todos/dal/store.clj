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
  (let [cleaned-entries  (remove-nil-values entries)
        todo  (entry->todo (first cleaned-entries))
        tasks  (entries->tasks cleaned-entries)]
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
    (println result)
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

;; task CRUD
(defn get-tasks-for-todo [id]
  (let [result (db/execute-db! (-> (hh/select :task/*)
                                   (hh/from :task)
                                   (hh/where [:= :todo_id :?id])
                                   (sql/format {:params {:id id}})))]
    (when-not (empty? result)
      (map #(entry->task %) result))))

(defn add-task [todo-id task]
  (if (todo-exists? todo-id)
    (db/execute-one-db! (-> (hh/insert-into :task)
                            (hh/values [(assoc task :todo_id todo-id)])
                            (sql/format)))
    (println (str "todo with id " todo-id " not found!"))))

(defn delete-task [todo-id task-id]
  (if (todo-exists? todo-id)
    (db/execute-one-db! (-> (hh/delete-from :task)
                            (hh/where [:= :id :?id])
                            (sql/format {:params {:id task-id}})))
    (println (str "todo with id " todo-id " not found!"))))

(defn update-task [todo-id task-id {:keys [name]}]
  (if (task-exists-for-todo? todo-id task-id)
    (db/execute-one-db! (-> (hh/update :task)
                            (hh/set {:name name})
                            (hh/where [:= :id :?id])
                            (sql/format {:params {:id task-id}})))
    (println (str "todo with id " todo-id " or task with id" task-id "not found!"))))


(comment
  (create-todo {:name "second test" :description "tested from repl"})
  (get-todo 0)
  (todo-exists? 1)
  (get-todo 1)
  (get-all-todos)
  (def test-data [{:todo/id 1, :todo/name "test from repl", :todo/description "tested from repl", :task/id nil, :task/todo_id nil, :task/name nil}
                  {:todo/id 2, :todo/name "test from repl", :todo/description "tested from repl", :task/id nil, :task/todo_id nil, :task/name nil}])
  (map #(into {} (remove (fn [[_ v]] (nil? v)) %)) test-data)
  (delete-todo 4)
  (get-tasks-for-todo 1)
  (delete-task 1 1)
  (update-task 1 4 {:name "updated task number for with new text"})

  (add-task 1 {:name "test 2 task"})
  (delete-todo 1)
  (get-tasks-for-todo 1)
  (update-todo 3 {:name "updated todo" :description "updated description"}))