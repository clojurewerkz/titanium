(ns clojurewerkz.titanium.graph-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.edges    :as ted]
            [clojurewerkz.support.io        :as sio])
  (:use clojure.test)
  (:import java.io.File
           [java.util.concurrent CountDownLatch TimeUnit]))


(deftest test-open-and-close-a-ram-graph
  (tg/open-in-memory-graph)
  (is (tg/open?))
  (tg/shutdown))

(deftest test-open-and-close-a-local-graph-with-a-directory-path
  (let [p (sio/create-temp-dir)
        d (do (.deleteOnExit p)
              p)]
    (tg/open d)
    (is (tg/open?))
    (tg/shutdown)))

(deftest test-open-and-close-a-local-graph-with-a-configuration-map
  (let [p (sio/create-temp-dir)
        d (do (.deleteOnExit p)
              (.getPath p))]
    (tg/open {:storage {:directory d
                        :backend  "berkeleyje"}})
    (is (tg/open?))
    (tg/shutdown)))

;;TODO: Bring over tests from Hermes for Transacitons. 