(ns clojurewerkz.titanium.conf
  (:import (org.apache.commons.io FileUtils)))

(def conf {:storage {:backend "embeddedcassandra"
                     :hostname "127.0.0.1"
                     :keyspace "titaniumtest"
                     :cassandra-config-dir
                     (str "file://"
                          (System/getProperty "user.dir")
                          "/resources/test-cassandra.yaml")}})

(defn clear-db []
  (FileUtils/deleteDirectory (java.io.File. "/tmp/titanium-test")))
