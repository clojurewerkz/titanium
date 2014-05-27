(ns clojurewerkz.titanium.test.conf
  (:require [clojure.java.io :as io])
  (:import (org.apache.commons.io FileUtils)))

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
  (FileUtils/deleteDirectory (java.io.File. "/tmp/titanium-test")))
