(ns todos.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [todos.api :as SUT]))

(deftest routes-match
  (testing "return 404 on non-existent route"
    (is (= (SUT/api {:uri "/non-existent-route"})
           {:status 404
            :body   "Not found"}))))