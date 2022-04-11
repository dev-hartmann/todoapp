(ns todos.model-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [todos.model.todo :as SUT]))

(deftest data-validity
  (testing "valid task data"
    (is (true? (s/valid? :todo/task {:name "test task"}))))
  (testing "invalid task data"
    (is (false? (s/valid? :todo/task {:description "test description"}))))
  (testing "valid todo data no optional task"
    (is (true? (s/valid? :model/todo {:name "test todo" :description "test description"}))))
  (testing "valid todo data with optional task"
    (is (true? (s/valid? :model/todo {:name "test todo" :description "test description" :tasks [{:name "test"}]}))))
  (testing "invalid todo data with optional task"
    (is (false? (s/valid? :model/todo {:name "test todo" :tasks [{:name "test"}]}))))
  (testing "valid todo data with invalid optional task"
    (is (false? (s/valid? :model/todo {:name "test todo" :description "test description" :tasks [{:description "test"}]})))))