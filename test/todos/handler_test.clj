(ns todos.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [todos.model.todo :as model]
            [todos.dal.store :as store]
            [todos.handler :as SUT]))

(def test-todo-request {:name        "test todo"
                        :description "this is a test todo"})

(def test-todo-response {:id          1
                         :name        "test todo"
                         :description "this is a test todo"})

(def test-task-request {:name "test task"})

(def test-task-response {:id 1
                         :name "test task"
                         :todo-id 1})

(deftest create-todo
  (testing "create todo handler happy path returns 201 and created todo"
    (with-redefs [store/create-todo (fn [todo] (assoc todo :id 1))]
      (is (= (SUT/create-todo-handler {:body-params test-todo-request})
             {:status 201
              :body   test-todo-response}))))
  (testing "create todo handler invalid request returns 400 and invalid todo data"
    (is (= (SUT/create-todo-handler {:body-params {:body "this is a wrong parameter"}})
           {:status 400
            :body   "invalid todo data"}))))

(deftest get-todo
  (testing "get todo handler happy path returns 200 and todo"
    (with-redefs [store/get-todo (fn [id] (assoc test-todo-request :id id))]
      (is (= (SUT/get-todo-handler {:path-params {:id "1"}})
             {:status 200
              :body   test-todo-response}))))
  (testing "get todo handler for unknown id returns 404 not found"
    (with-redefs [store/get-todo (fn [id] nil)]
      (is (= (SUT/get-todo-handler {:path-params {:id "12"}})
             {:status 404
              :body   "not found"})))))

(deftest update-todo
  (testing "update todo handler happy path returns 200 and updated todo"
    (with-redefs [store/get-todo    (fn [id] test-todo-response)
                  store/update-todo (fn [id update-data] (merge test-todo-response update-data))]
      (is (= (SUT/update-todo-handler {:path-params {:id "1"}
                                       :body-params {:name "updated todo"}})
             {:status 200
              :body   {:id          1
                       :name        "updated todo"
                       :description "this is a test todo"}}))))
  (testing "update todo handler with invalid data returns 400"
    (with-redefs [store/get-todo    (fn [id] test-todo-response)
                  store/update-todo (fn [id update-data] (merge test-todo-response update-data))]
      (is (= (SUT/update-todo-handler {:path-params {:id "1"}
                                       :body-params {:name 1}})
             {:status 400
              :body   "invalid todo data"}))))
  (testing "update todo handler for non existent todo retuns 404 not found"
    (with-redefs [store/get-todo    (fn [id] nil)]
      (is (= (SUT/update-todo-handler {:path-params {:id "15"}
                                       :body-params {:name 1}})
             {:status 404
              :body   "todo not found!"})))))

(deftest delete-todo
  (testing "delete todo handler happy path returns 200 and todo-id"
    (with-redefs [store/todo-exists? (fn [id] true)
                  store/delete-todo  (fn [id] id)]
      (is (= (SUT/delete-todo-handler {:path-params {:id "1"}})
             {:status 200
              :body   1}))))
  (testing "delete todo handler for unknown id returns 404 not found"
    (with-redefs [store/todo-exists? (fn [id] false)]
      (is (= (SUT/delete-todo-handler {:path-params {:id "12"}})
             {:status 404
              :body   "todo not found!"})))))

(deftest create-task
  (testing "create task handler happy path returns 201 and created todo"
    (with-redefs [store/todo-exists? (fn [id] true)
                  store/add-task (fn [id task] (assoc task :id 1 :todo-id id))]
      (is (= (SUT/add-task-handler {:path-params {:id "1"}
                                    :body-params test-task-request})
             {:status 201
              :body   test-task-response}))))
  (testing "create task handler invalid request returns 400 and invalid todo data"
    (with-redefs [store/todo-exists? (fn [id] true)]
      (is (= (SUT/add-task-handler {:path-params {:id "1"}
                                    :body-params {:body "this is a wrong parameter"}})
             {:status 400
              :body   "invalid task data"}))))
  (testing "create task handler invalid request returns 400 and invalid todo data"
    (with-redefs [store/todo-exists? (fn [id] false)]
      (is (= (SUT/add-task-handler {:path-params {:id "15"}
                                    :body-params test-task-request})
             {:status 404
              :body   "todo not found!"})))))

(deftest delete-task
  (testing "delete task handler happy path returns 200 and todo-id"
    (with-redefs [store/task-exists-for-todo? (fn [id task-id] true)
                  store/delete-task  (fn [id] id)]
      (is (= (SUT/delete-task-handler {:path-params {:id "1"
                                                     :task-id "1"}})
             {:status 200
              :body   1}))))
  (testing "delete task handler for unknown id returns 404 not found"
    (with-redefs [store/todo-exists? (fn [id] true)
                  store/task-exists-for-todo? (fn [id task-id] false)]
      (is (= (SUT/delete-task-handler {:path-params {:id "1"
                                                     :task-id "12"}})
             {:status 404
              :body   "task not found!"})))))

(deftest update-task
  (testing "update task handler happy path returns 200 and updated task"
    (with-redefs [store/task-exists-for-todo? (fn [id task-id] true)
                  store/update-task (fn [id update] (merge test-task-response update))]
      (is (= (SUT/update-task-handler {:path-params {:id "1"
                                                     :task-id "1"}
                                       :body-params {:name "updated task"}})
             {:status 200
              :body   {:id         1
                       :name        "updated task"
                       :todo-id 1}}))))
  (testing "update task handler wit invalid data returns 400 and invalid task data"
    (with-redefs [store/task-exists-for-todo? (fn [id task-id] true)
                  store/update-task (fn [id update] (merge test-task-response update))]
      (is (= (SUT/update-task-handler {:path-params {:id "1"
                                                     :task-id "1"}
                                       :body-params {:property "that is wrong"}})
             {:status 400
              :body   "invalid task data"}))))
  (testing "update task handler for unknown task-id returns 404 not found"
    (with-redefs [store/task-exists-for-todo? (fn [id task-id] false)]
      (is (= (SUT/update-task-handler {:path-params {:id "1"
                                                     :task-id "12"}})
             {:status 404
              :body   "task not found!"})))))