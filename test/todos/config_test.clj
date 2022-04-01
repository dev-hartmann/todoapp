(ns todos.config-test
    (:require [clojure.test :refer [deftest testing is]]
              [todos.config :as SUT]))

(deftest load-config-from-edn-file
  (testing "with specific path"
    (is (not( nil? (SUT/load-config "test-config.edn")))))
  (testing "with default path"
    (is (not-empty (SUT/load-default-config))))
  (testing "with path to non exitent"
    (is (= nil (SUT/load-config "non-existent-file.edn")))))