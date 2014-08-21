(ns clojurewerkz.titanium.graph-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted]
            [clojurewerkz.titanium.schema   :as ts]
            [clojurewerkz.support.io        :as sio]
            [clojurewerkz.archimedes.graph  :as c])
  (:use clojure.test
        [clojurewerkz.titanium.test.support :only (graph-fixture *graph*)])
  (:import org.apache.commons.io.FileUtils
           (com.thinkaurelius.titan.graphdb.vertices StandardVertex)
           (com.thinkaurelius.titan.graphdb.database StandardTitanGraph)
           [java.util.concurrent CountDownLatch TimeUnit]))

(use-fixtures :once graph-fixture)

(deftest open-and-close-a-local-graph-with-a-shortcut
  (let [d (sio/create-temp-dir)]
    (is (let [graph (tg/open (str "berkeleyje:" (.getPath d)))]
          (and graph (nil? (tg/shutdown graph)))))))

(deftest test-open-and-close-a-local-graph-with-a-connfiguration-map
  (let [d (sio/create-temp-dir)]
    (is (let [graph  (tg/open {"storage.directory" (.getPath d)
                               "storage.backend"  "berkeleyje"})]
          (and graph (nil? (tg/shutdown graph)))))))

(deftest test-conf-graph
  (testing "Graph type"
    (is (= (type *graph*) StandardTitanGraph)))

  (testing "Vertex type"
    (tg/with-transaction [tx *graph*]
        (let [vertex (tv/create! tx)]
          (is (= StandardVertex (type vertex)))))))

(deftest graph-of-the-gods

  (testing "Configure edge and vertex properties"
    (ts/with-management-system [mgmt *graph*]
      (is (ts/make-property-key mgmt :name  String :cardinality :single))
      (is (ts/make-property-key mgmt :type  String :cardinality :single))
      (is (ts/make-property-key mgmt :times Long   :cardinality :single))
      (is (ts/make-edge-label mgmt :lives   :multiplicity :many-to-one))
      (is (ts/make-edge-label mgmt :father  :multiplicity :one-to-many))
      (is (ts/make-edge-label mgmt :mother  :multiplicity :one-to-many))
      (is (ts/make-edge-label mgmt :brother :multiplicity :many-to-many))
      (is (ts/make-edge-label mgmt :pet     :multiplicity :one-to-many))
      (is (ts/make-edge-label mgmt :battled :multiplicity :many-to-many))
      (is (ts/build-composite-index mgmt :name-ix :vertex [:name] :unique? true))
      (is (ts/build-composite-index mgmt :type-ix :vertex [:type]))
      (is (ts/build-composite-index mgmt :times-ix :edge [:times]))))

  (testing "Populate graph"
    (is
     (tg/with-transaction [tx *graph*]
       (let [saturn   (tv/create! tx {:name "Saturn"   :type "titan"})
             jupiter  (tv/create! tx {:name "Jupiter"  :type "god"})
             hercules (tv/create! tx {:name "Hercules" :type "demigod"})
             alcmene  (tv/create! tx {:name "Alcmene"  :type "human"})
             neptune  (tv/create! tx {:name "Neptune"  :type "god"})
             pluto    (tv/create! tx {:name "Pluto"    :type "god"})
             sea      (tv/create! tx {:name "Sea"      :type "location"})
             sky      (tv/create! tx {:name "Sky"      :type "location"})
             tartarus (tv/create! tx {:name "Tartarus" :type "location"})
             nemean   (tv/create! tx {:name "Nemean"   :type "monster"})
             hydra    (tv/create! tx {:name "Hydra"    :type "monster"})
             cerberus (tv/create! tx {:name "Cerberus" :type "monster"})]
         (ted/connect! tx neptune :lives sea)
         (ted/connect! tx jupiter :lives sky)
         (ted/connect! tx pluto :lives tartarus)
         (ted/connect! tx jupiter :father saturn)
         (ted/connect! tx hercules :father jupiter)
         (ted/connect! tx hercules :mother alcmene)
         (ted/connect! tx jupiter :brother pluto)
         (ted/connect! tx pluto :brother jupiter)
         (ted/connect! tx neptune :brother pluto)
         (ted/connect! tx pluto :brother neptune)
         (ted/connect! tx jupiter :brother neptune)
         (ted/connect! tx neptune :brother jupiter)
         (ted/connect! tx cerberus :lives tartarus)
         (ted/connect! tx pluto :pet cerberus)
         (ted/connect! tx hercules :battled nemean   {:times 1})
         (ted/connect! tx hercules :battled hydra    {:times 2})
         (ted/connect! tx hercules :battled cerberus {:times 12})
         true))))

  (testing "Query graph"
    (tg/with-transaction [tx *graph*]
      (is (= #{"Jupiter" "Neptune" "Pluto"}
             (set (map (fn [v] (tv/get v :name)) (tv/find-by-kv tx :type "god")))))
      (let [jupiter (first (tv/find-by-kv tx :name "Jupiter"))]
        (is jupiter)
        (let [lives (ted/head-vertex (first (tv/edges-of jupiter :out :lives)))]
          (is "Sky" (tv/get lives :name)))))))
