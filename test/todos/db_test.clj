(ns todos.db-test
  (:require [clojure.test :refer [deftest is testing]]
            [todos.config :as config]
            [todos.db :as SUT]))

(deftest db-connection
  (testing "creating database connection from system config"
    (let [test-config (SUT/create-db-config (config/load-config "test-config.edn"))]
      (is (not-empty test-config))
      (is (= "postgresql" (:dbtype test-config)))
      (is (= "test-user" (:user test-config)))
      (is (= "test-pw" (:password test-config))))))