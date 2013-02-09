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
