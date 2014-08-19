(ns clojurewerkz.titanium.test.support
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojurewerkz.titanium.graph :as tg]
            [me.raynes.fs :as fs]))

(def ^:dynamic *graph*)

(def config-template  (io/resource "test-cassandra.yaml"))

(defn graph-fixture
  [f]
  (let [tmp         (fs/temp-dir "titanium-test")
        config-file (io/file tmp "test.yaml")
        config      {;; Embedded cassandra settings
                     "storage.backend"  "embeddedcassandra"
                     "storage.conf-file" (.getPath config-file)
                     ;; Embedded elasticsearch settings
                     "storage.index.search.backend" "elasticsearch"
                     "storage.index.search.directory" (.getPath (io/file tmp "elasticsearch"))
                     "storage.index.search.client-only" false
                     "storage.index.search.local-mode" true}]
    (spit config-file
          (-> (slurp config-template) (str/replace "/tmp/titanium-test" (.getPath tmp))))
    (Thread/sleep 5000)
    (binding [*graph* (tg/open config)]
      (f)
      (tg/shutdown *graph*))
    (fs/delete-dir tmp)))

;; (defn graph-fixture
;;   [f]
;;   (let [tmp (fs/temp-dir "titanium-test")]
;;     (binding [*graph* (tg/open tmp)]
;;       (f)
;;       (tg/shutdown *graph*))
;;     (fs/delete-dir tmp)))
