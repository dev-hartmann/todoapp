(ns todos.handler
  (:require [clojure.spec.alpha :as s]
            [todos.dal.store :as store]
            [todos.config :as config]
            [todos.model.todo :as model]))

(defn status-handler [_]
  (let [config (config/current-config)
        status-info (:application config)]
    {:status 200 :bodys status-info}))

(comment (status-handler nil))

(defn get-todos-handler [_]
  (let [todos (store/get-all-todos)]
    {:status 200 :body todos}))

(defn get-todo-handler [{{:keys [id]} :path-params}]
  (let [todo (store/get-todo (Integer/parseInt id))]
    (if (nil? todo)
      {:status 404 :body "not found"}
      {:status 200 :body todo})))

(defn create-todo-handler [request]
  (if (s/valid? :model/todo (:body-params request))
    {:status 201 :body (store/create-todo (:body-params request))}
    {:status 400 :body "invalid todo data"}))

(defn update-todo-handler [request]
  (let [todo-id (Integer/parseInt (get-in request [:path-params :id]))
        update-data (:body-params request)
        todo (store/get-todo todo-id)
        updated-todo (merge todo update-data)]
    (if-not (nil? todo)
      (if (s/valid? :model/todo updated-todo)
        {:status 200 :body (store/update-todo todo-id updated-todo)}
        {:status 400 :body "invalid todo data"})
      {:status 404 :body "todo not found!"})))

(defn delete-todo-handler [{{:keys [id]} :path-params}]
  (let [todo-id (Integer/parseInt id)
        todo-exists? (store/todo-exists? todo-id)]
    (if todo-exists?
      {:status 200 :body (store/delete-todo todo-id)}
      {:status 404 :body "todo not found!"})))

(defn get-tasks-for-todo-handler [todo-id]
  (let [tasks (store/get-tasks-for-todo (Integer/parseInt todo-id))]
    {:status 200 :body tasks}))

(defn add-task-handler [request]
  (let [todo-id (Integer/parseInt (get-in request [:path-params :id]))
        todo-exists? (store/todo-exists? todo-id)
        task (:body-params request)]
    (if todo-exists?
      (if (s/valid? :todo/task task)
        {:status 201 :body (store/add-task todo-id task)}
        {:status 400 :body "invalid task data"})
      {:status 404 :body "todo not found!"})))

(defn delete-task-handler [{{:keys [id task-id]} :path-params}]
  (let [todo-id (Integer/parseInt id)
        task-id (Integer/parseInt task-id)
        task-exists? (store/task-exists-for-todo? todo-id task-id)]
    (if task-exists?
      {:status 200 :body (store/delete-task task-id)}
      {:status 404 :body "task not found!"})))

(defn update-task-handler [request]
  (let [todo-id (Integer/parseInt (get-in request [:path-params :id]))
        task-id (Integer/parseInt (get-in request [:path-params :task-id]))
        update-data (:body-params request)
        task-exists? (store/task-exists-for-todo? todo-id task-id)]
    (if task-exists?
      (if (s/valid? :todo/task update-data)
        {:status 200 :body (store/update-task task-id update-data)}
        {:status 400 :body "invalid task data"})
      {:status 404 :body "task not found!"})))
