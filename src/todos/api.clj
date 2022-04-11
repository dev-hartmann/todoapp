(ns todos.api
  (:require  [ring.middleware.params :as params]
             [reitit.ring.middleware.muuntaja :as muuntaja]
             [muuntaja.core :as m]
             [reitit.ring.coercion :as coercion]
             [reitit.ring :as ring]))

(defn handler [_]
  {:status 200, :body "ok"})

(def api
    (ring/ring-handler
     (ring/router
      [["/" {:get handler}]]
      {:data {:muuntaja m/instance
              :middleware [params/wrap-params
                           muuntaja/format-middleware
                           coercion/coerce-exceptions-middleware
                           coercion/coerce-request-middleware
                           coercion/coerce-response-middleware]}})
     (ring/create-default-handler
      {:not-found (constantly {:status 404, :body "Not found"})
       :method-not-allowed (constantly {:status 405, :body "Not allowed"})
       :not-acceptable (constantly {:status 406, :body "Not acceptable"})})))
