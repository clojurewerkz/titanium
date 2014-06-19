(ns clojurewerkz.titanium.test.support
  (:require [clojure.java.io :as io]
            [clojurewerkz.titanium.graph :as tg])
  (:import (org.apache.commons.io FileUtils)))

(def ^:dynamic *graph*)

(def cs-dir (str (io/resource "test-cassandra.yaml")))

(def conf {;; Embedded cassandra settings
           "storage.backend"  "embeddedcassandra"
           "storage.cassandra-config-dir" cs-dir
           ;; Embedded elasticsearch settings
           "storage.index.search.backend" "elasticsearch"
           "storage.index.search.directory" "/tmp/cassandra/elasticsearch"
           "storage.index.search.client-only" false
           "storage.index.search.local-mode" true})

(defn clear-db []
  (Thread/sleep 1000)
  (FileUtils/deleteDirectory (io/file "/tmp/titanium-test"))
  (Thread/sleep 1000))

(defn graph-fixture
  [f]
  (clear-db)
  (binding [*graph* (tg/open conf)]
    (f)
    (tg/shutdown *graph*)))
