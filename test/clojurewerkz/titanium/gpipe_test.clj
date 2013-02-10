(ns clojurewerkz.titanium.gpipe-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.elements :as te]
            [clojurewerkz.titanium.gpipe    :as q])
  (:use clojure.test))


(deftest test-very-basic-gremlin-query
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/property :age)
                         (q/into-set))]
    (is (= #{32 38} xs))
    (tg/close g)))

(deftest test-pipeline-query-with-has-and-default-operator
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/has :age 32)
                         (q/property :name)
                         (q/into-set))]
    (is (= #{"Alonso"} xs))
    (tg/close g)))

(deftest test-pipeline-query-with-has-and-explicit-eq-operator
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/has :age '= 32)
                         (q/property :name)
                         (q/into-set))]
    (is (= #{"Alonso"} xs))
    (tg/close g)))

(deftest test-pipeline-query-with-has-and-explicit-lt-operator
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/has :age '< 36)
                         (q/property :name)
                         (q/into-set))]
    (is (= #{"Alonso"} xs))
    (tg/close g)))

(deftest test-pipeline-query-with-has-and-explicit-gt-operator
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/has :age '> 36)
                         (q/property :name)
                         (q/order)
                         (q/into-set))]
    (is (= #{"Thomas"} xs))
    (tg/close g)))

(deftest test-pipeline-query-that-collects-edge-labels
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out-e "friend")
                         (q/label)
                         (q/into-set))]
    (is (= #{"friend"} xs))
    (tg/close g)))

(deftest test-pipeline-query-with-random-pipe
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/random 0.5)
                         (q/has :age '> 36)
                         (q/property :name)
                         (q/order)
                         (q/into-set))]
    (tg/close g)))

(deftest test-pipeline-query-with-filter-pipe
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/has :age '> 36)
                         (q/property :name)
                         (q/filter (constantly false))
                         (q/order)
                         (q/into-set))]
    (is (empty? xs))
    (tg/close g)))

(deftest test-pipeline-query-with-id-pipe
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        xs   (q/pipeline a
                         (q/out "friend")
                         (q/has :age '> 36)
                         (q/id)
                         (q/into-set))]
    (is (every? number? xs))
    (tg/close g)))