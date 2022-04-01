(ns todos.config
  (:require [aero.core :as a]
            [clojure.java.io :as io]))

(def default-config "config.edn")

(defn- file-exists? [filename]
  (let [resource-filepath (str "resources/" filename)]
     (.exists (io/file resource-filepath))))
  
(defn load-config [filename]
  (when (file-exists? filename)
      (a/read-config (io/resource filename))))

(defn load-default-config []
  (load-config default-config))

(comment
  (load-default-config)
  (file-exists? default-config)
  )