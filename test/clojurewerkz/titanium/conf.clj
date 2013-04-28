(ns clojurewerkz.titanium.conf
  (:import (org.apache.commons.io FileUtils)))


(def cs-dir (str (clojure.java.io/as-url 
              (clojure.java.io/as-file 
                (str (System/getProperty "user.dir") 
                  "/resources/test-cassandra.yaml")))))

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
