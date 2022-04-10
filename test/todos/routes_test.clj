(ns todos.routes-test
  (:require [clojure.test :refer [deftest testing is]]
            [todos.routes :as SUT]))

(deftest routes-match
  (testing "return 404 on non-existent route"
    (is (= (SUT/app-routes {:uri "/non-existent-route"}) 
           {:status 404, :body "Not found"}))))

