(ns clojurewerkz.titanium.graph-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted]
            [clojurewerkz.titanium.types    :as tt]
            [clojurewerkz.support.io        :as sio]
            [clojurewerkz.archimedes.graph  :as c])
  (:use clojure.test
        [clojurewerkz.titanium.test.conf :only (conf clear-db)])
  (:import java.io.File
           (com.thinkaurelius.titan.graphdb.vertices StandardVertex)
           (com.thinkaurelius.titan.graphdb.database StandardTitanGraph)
           [java.util.concurrent CountDownLatch TimeUnit]))


(deftest open-and-close-a-local-graph-with-a-directory-path
  (let [d (doto (sio/create-temp-dir) (.deleteOnExit))
        graph (tg/open d)]
    (is graph)
    (is (nil? (tg/shutdown graph)))))

(deftest test-open-and-close-a-local-graph-with-a-connfiguration-map
  (let [d (doto (sio/create-temp-dir) (.deleteOnExit))
        graph  (tg/open {"storage.directory" (.getPath d)
                         "storage.backend"  "berkeleyje"})]
    (is graph)
    (is (nil? (tg/shutdown graph)))))

(deftest test-conf-graph
  (clear-db)
  (let [graph (tg/open conf)]

    (testing "Graph type"
      (is (= (type graph) StandardTitanGraph)))

    (testing "Vertex type"
      (tg/with-transaction [tx graph]
        (let [vertex (tv/create! tx)]
          (is (= StandardVertex (type vertex))))))

    (testing "Dueling transactions"
      (tg/with-transaction [tx graph]
        (tt/defkey-once tx :vertex-id Long {:unique? true}))

      (testing "Without retries"
        (let [random-long (long (rand-int 100000))
              f1 (future (tg/with-transaction [tx graph] (tv/upsert! tx :vertex-id {:vertex-id random-long})))
              f2 (future (tg/with-transaction [tx graph] (tv/upsert! tx :vertex-id {:vertex-id random-long})))]

          (is (thrown? java.util.concurrent.ExecutionException
                       (do @f1 @f2)) "The futures throw errors.")))

      (testing "With retries"
        (let [random-long (long (rand-int 100000))
              f1 (future (tg/with-transaction-retry [tx graph :max-attempts 3 :wait-time 100]
                           (tv/upsert! tx :vertex-id {:vertex-id random-long})))
              f2 (future (tg/with-transaction-retry [tx graph :max-attempts 3 :wait-time 100]
                           (tv/upsert! tx :vertex-id {:vertex-id random-long})))]
          (is (= random-long
                 (tg/with-transaction [tx graph]
                   (tv/get (tv/refresh tx (first @f1)) :vertex-id))
                 (tg/with-transaction [tx graph]
                   (tv/get (tv/refresh tx (first @f2)) :vertex-id)))
              "The futures have the correct values.")
          (is (= 1 (count (tg/with-transaction [tx graph]
                            (tv/find-by-kv tx :vertex-id random-long))))
              "The graph has only one vertex with the specified vertex-id")))

      (testing "With retries and an exponential backoff function"
        (let [backoff-fn (fn [try-count] (+ (Math/pow 10 try-count) (* try-count (rand-int 100))))
              random-long (long (rand-int 100000))
              f1 (future (tg/with-transaction-retry [tx graph :max-attempts 3 :wait-time backoff-fn]
                           (tv/upsert! tx :vertex-id {:vertex-id random-long})))
              f2 (future (tg/with-transaction-retry [tx graph :max-attempts 3 :wait-time backoff-fn]
                           (tv/upsert! :vertex-id {:vertex-id random-long})))]
          (is (= random-long
                 (tg/with-transaction [tx graph]
                   (tv/get (tv/refresh tx (first @f1)) :vertex-id))
                 (tg/with-transaction [tx graph]
                   (tv/get (tv/refresh tx (first @f2)) :vertex-id)))
              "The futures have the correct values.")
          (is (= 1 (count (tg/with-transaction [tx graph]
                            (tv/find-by-kv tx :vertex-id random-long))))
              "The graph has only one vertex with the specified vertex-id"))))

    (tg/shutdown graph))
  (clear-db))
