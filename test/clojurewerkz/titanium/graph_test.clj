(ns clojurewerkz.titanium.graph-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted]
            [clojurewerkz.titanium.types    :as tt]            
            [clojurewerkz.support.io        :as sio]
            [archimedes.core                :as c])
  (:use clojure.test
        [clojurewerkz.titanium.conf :only (conf clear-db)])
  (:import java.io.File
           (com.thinkaurelius.titan.graphdb.vertices StandardVertex)
           (com.thinkaurelius.titan.graphdb.database StandardTitanGraph)
           [java.util.concurrent CountDownLatch TimeUnit]))


(deftest open-and-close-a-local-graph-with-a-directory-path
  (let [p (sio/create-temp-dir)
        d (do (.deleteOnExit p)
              p)]
    (tg/open d)
    (is (tg/open?))
    (tg/shutdown)))

(deftest test-open-and-close-a-local-graph-with-a-connfiguration-map
  (let [p (sio/create-temp-dir)
        d (do (.deleteOnExit p)
              (.getPath p))]
    (tg/open {"storage.directory" d
              "storage.backend"  "berkeleyje"})
    (is (tg/open?))
    (tg/shutdown)))


(deftest test-conf-graph
  (clear-db)
  (tg/open conf)

  (testing "Is the graph open?"
    (is (tg/open?)))

  (testing "Stored graph"
    (is (= (type c/*graph*)
           StandardTitanGraph)))

  (testing "Stored graph"
    (let [vertex (tg/transact! (.addVertex c/*graph*))]      
      (is (= StandardVertex (type vertex)))))

  (testing "Stored graph"
    (is (thrown? Throwable #"transact!" (tv/create!))))

  ;; (testing "Dueling transactions"
  ;;   (testing "Without retries"
  ;;     (tg/transact!
  ;;      (tt/create-vertex-key-once :vertex-id Long {:indexed true
  ;;                                                  :unique true}))
  ;;     (let [random-long (long (rand-int 100000))
  ;;           f1 (future (tg/transact! (tv/upsert! :vertex-id {:vertex-id random-long})))
  ;;           f2 (future (tg/transact! (tv/upsert! :vertex-id {:vertex-id random-long})))]

  ;;       (is (thrown? java.util.concurrent.ExecutionException
  ;;                    (do @f1 @f2)) "The futures throw errors.")))
  ;;   (testing "With retries"
  ;;     (tg/open conf)
  ;;     (tg/transact!
  ;;      (tt/create-vertex-key-once :vertex-id Long {:indexed true
  ;;                                                  :unique true}))
  ;;     (let [random-long (long (rand-int 100000))
  ;;           f1 (future (tg/retry-transact! 3 100 (tv/upsert! :vertex-id {:vertex-id random-long})))
  ;;           f2 (future (tg/retry-transact! 3 100 (tv/upsert! :vertex-id {:vertex-id random-long})))]

  ;;       (is (= random-long
  ;;              (tg/transact!
  ;;               (tv/get (tv/refresh (first @f1)) :vertex-id))
  ;;              (tg/transact!
  ;;               (tv/get (tv/refresh (first @f2)) :vertex-id))) "The futures have the correct values.")

  ;;       (is (= 1 (count
  ;;                 (tg/transact! (tv/find-by-kv :vertex-id random-long))))
  ;;           "*graph* has only one vertex with the specified vertex-id"))))
  ;; (testing "With retries and an exponential backoff function"
  ;;   (tg/transact!
  ;;    (tt/create-vertex-key-once :vertex-id Long {:indexed true
  ;;                                                :unique true}))
  ;;   (let [backoff-fn (fn [try-count] (+ (Math/pow 10 try-count) (* try-count (rand-int 100))))
  ;;         random-long (long (rand-int 100000))
  ;;         f1 (future (tg/retry-transact! 3 backoff-fn (tv/upsert! :vertex-id {:vertex-id random-long})))
  ;;         f2 (future (tg/retry-transact! 3 backoff-fn (tv/upsert! :vertex-id {:vertex-id random-long})))]

  ;;     (is (= random-long
  ;;            (tg/transact!
  ;;             (tv/get (tv/refresh (first @f1)) :vertex-id))
  ;;            (tg/transact!
  ;;             (tv/get (tv/refresh (first @f2)) :vertex-id))) "The futures have the correct values.")

  ;;     (is (= 1 (count
  ;;               (tg/transact! (tv/find-by-kv :vertex-id random-long))))
  ;;         "*graph* has only one vertex with the specified vertex-id")))
  (tg/shutdown)
  (clear-db))