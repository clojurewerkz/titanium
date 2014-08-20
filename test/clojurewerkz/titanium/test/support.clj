(ns clojurewerkz.titanium.test.support
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojurewerkz.titanium.graph :as tg]
            [me.raynes.fs :as fs]))

(def ^:dynamic *graph*)

(defn mk-tmp-cassandra-config
  []
  (let [tmp (fs/temp-dir "titanium-test")
        config-file (io/file tmp "test.yaml")
        config {;; Embedded cassandra settings
                "storage.backend"  "embeddedcassandra"
                "storage.conf-file" (.getPath config-file)
                ;; Embedded elasticsearch settings
                "index.search.backend" "elasticsearch"
                "index.search.directory" (.getPath (io/file tmp "elasticsearch"))
                "index.search.client-only" false
                "index.search.local-mode" true}]
    (spit config-file
          (-> (io/resource "test-cassandra.yaml")
              slurp
              (str/replace "/var/lib/cassandra"
                           (str (.getPath tmp) "/cassandra"))))
    [tmp config]))

(defn mk-tmp-berkeleyje-config
  []
  (let [tmp (fs/temp-dir "titanium-test")
        config {"storage.backend" "berkeleyje"
                "storage.directory" (.getPath (io/file tmp "bdb"))
                "index.search.backend" "elasticsearch"
                "index.search.directory" (.getPath (io/file tmp "es"))
                "index.search.elasticsearch.client-only" false
                "index.search.elasticsearch.local-mode" true}]
    [tmp config]))

(defn fixture
  [make-config]
  (fn [f]
    (let [[tmpdir config] (make-config)]
      (try
        (binding [*graph* (tg/open config)]
          (try
            (f)
            (finally
              (tg/shutdown *graph*))))
        (finally
          (fs/delete-dir tmpdir))))))

(def cassandra-fixture (fixture mk-tmp-cassandra-config))

(def graph-fixture (fixture mk-tmp-berkeleyje-config))
