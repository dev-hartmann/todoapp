(ns todos.api
  (:require  [reitit.ring :as ring]
             [muuntaja.core :as m]
             [reitit.swagger :as swagger]
             [reitit.swagger-ui :as swagger-ui]
             [reitit.ring.coercion :as coercion]
             [reitit.ring.middleware.parameters :as params]
             [reitit.ring.middleware.exception :as exception]
             [reitit.ring.middleware.muuntaja :as muuntaja]
             [reitit.ring.middleware.multipart :as multipart]
             [todos.handler :as h]))

(def api
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc  true
             :swagger {:info {:title       "Todo App"
                              :description "todo app API"
                              :version     "1.0.0"
                              :contact     {:name  "Ben Hartmann"
                                            :email "ben_hartmann@icloud.com"}}}
             :handler (swagger/create-swagger-handler)}}]
     ["/status" {:get h/status-handler}]
     ["/api"
      ["/todos" {:get h/get-todos-handler
                 :post h/create-todo-handler}]
      ["/todos/:id" {:get h/get-todo-handler
                     :put h/update-todo-handler
                     :delete h/delete-todo-handler
                     :parameter {:path {:id :integer}}}]
      ["/todos/:id/tasks" {:get h/get-tasks-for-todo-handler
                           :post h/add-task-handler
                           :parameter {:path {:id :integer}}}]
      ["/todos/:id/tasks/:task-id" {:put h/update-task-handler
                                    :delete h/delete-task-handler
                                    :parameter {:path {:id :integer
                                                       :task-id :integer}}}]]]
    {:data {:muuntaja   m/instance
            :middleware [params/parameters-middleware
                         muuntaja/format-middleware
                         multipart/multipart-middleware
                         (exception/create-exception-middleware
                          {::exception/default (partial exception/wrap-log-to-console exception/default-handler)})
                         coercion/coerce-exceptions-middleware
                         coercion/coerce-request-middleware
                         coercion/coerce-response-middleware]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path   "/swagger-ui"
      :url    "/swagger.json"
      :config {:validatorUrl nil}})
    (ring/create-default-handler
     {:not-found          (constantly {:status 404
                                       :body   "Not found"})
      :method-not-allowed (constantly {:status 405
                                       :body   "Not allowed"})
      :not-acceptable     (constantly {:status 406
                                       :body   "Not acceptable"})}))))

