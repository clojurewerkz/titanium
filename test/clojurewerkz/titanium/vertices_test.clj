(ns clojurewerkz.titanium.vertices-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.elements :as te]
            [clojurewerkz.titanium.edges    :as ted])
  (:use clojure.test))


;;
;; Working with vertices (nodes),
;; heavily inspired by the Neocons test suite
;;

(deftest test-creating-and-immediately-accessing-a-node-without-properties
  (let [g       (tg/open-in-memory-graph)
        m       {}
        created (tg/add-vertex g m)
        fetched (tg/get-vertex g (te/id-of created))]
    (is (= (te/id-of created) (te/id-of fetched)))
    (is (= (te/properties-of created) (te/properties-of fetched)))))


(deftest test-creating-and-immediately-accessing-a-node-with-properties
  (let [g       (tg/open-in-memory-graph)
        m       {:key "value"}
        created (tg/add-vertex g m)
        fetched (tg/get-vertex g (te/id-of created))]
    (is (= (te/id-of created) (te/id-of fetched)))
    (is (= (te/properties-of created) (te/properties-of fetched)))))
