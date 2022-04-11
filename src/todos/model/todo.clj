(ns todos.model.todo
  (:require [clojure.spec.alpha :as s]))

;; task spec
(s/def :task/name string?)
(s/def :todo/task (s/keys :req-un [:task/name]))
(s/def :todo/tasks (s/coll-of :todo/task :kind vector?))

;; todo spec
(s/def :todo/description string?)
(s/def :todo/name string?)

(s/def :model/todo (s/keys :req-un [:todo/name :todo/description]
                           :opt-un [:todo/tasks]))
