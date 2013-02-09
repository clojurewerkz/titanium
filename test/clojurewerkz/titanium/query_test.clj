(ns clojurewerkz.titanium.query-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.elements :as te]
            [clojurewerkz.titanium.query    :as tq])
  (:use clojure.test))


(deftest test-basic-vertices-query
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        _    (tg/add-edge   g a b "friend")
        _    (tg/add-edge   g a c "friend")
        vs   (tq/find-vertices a
                               (tq/direction :out)
                               (tq/labels    ["friend"]))
        fv   (first vs)
        sv   (second vs)]
    (is (= 2 (count vs)))
    (is (= fv b))
    (is (= sv c))))

(deftest test-edge-count
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend")
        a-friend-c    (tg/add-edge   g a c "friend")
        a-remember-c  (tg/add-edge   g a c "remember")
        c-remember-a  (tg/add-edge   g c a "remember")
        n    (tq/count-edges a
                             (tq/direction :out)
                             (tq/labels    ["friend" "remember"]))]
    (is (= n 3))))

(deftest test-edge-count-with-default-comparator
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        n1     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["friend"])
                               (tq/has :age 28))
        n2     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["friend"])
                               (tq/has :age 29))
        n3     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["hates"])
                               (tq/has :age 28))]
    (is (= n1 1))
    (is (= n2 0))
    (is (= n3 0))))

(deftest test-edge-count-with-gte-comparator
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        n1     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["friend"])
                               (tq/has :age '>= 28))
        n2     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["friend"])
                               (tq/has :age '>= 29))
        n3     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["hates"])
                               (tq/has :age '>= 28))]
    (is (= n1 2))
    (is (= n2 1))
    (is (= n3 0))))

(deftest test-edge-count-with-lte-comparator
  (let [g    (tg/open-in-memory-graph)
        a    (tg/add-vertex g {:name "Steven" :age 30})
        b    (tg/add-vertex g {:name "Alonso" :age 32})
        c    (tg/add-vertex g {:name "Thomas" :age 38})
        a-friend-b    (tg/add-edge   g a b "friend" {:age 28})
        a-friend-c    (tg/add-edge   g a c "friend" {:age 30})
        n1     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["friend"])
                               (tq/has :age '<= 28))
        n2     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["friend"])
                               (tq/has :age '<= 39))
        n3     (tq/count-edges a
                               (tq/direction :out)
                               (tq/labels    ["hates"])
                               (tq/has :age '<= 28))]
    (is (= n1 1))
    (is (= n2 2))
    (is (= n3 0))))
