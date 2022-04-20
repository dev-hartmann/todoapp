(ns todos.config
  (:require [aero.core :as a]
            [clojure.java.io :as io]))
(defonce config (atom nil))

(def default-config "config.edn")

(defn- file-exists? [filename]
  (let [resource-filepath (str "resources/" filename)]
    (.exists (io/file resource-filepath))))

(defn load-config [filename]
  (when (file-exists? filename)
    (reset! config (a/read-config (io/resource filename)))
    @config))

(defn load-default-config []
  (load-config default-config))

(defn current-config []
  @config)

(comment
  (load-default-config)
  (file-exists? default-config))