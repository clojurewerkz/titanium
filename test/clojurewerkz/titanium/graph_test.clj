(ns clojurewerkz.titanium.graph-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.elements :as te]
            [clojurewerkz.titanium.edges    :as ted])
  (:use clojure.test))


(deftest test-open-and-close-a-local-graph
  (let [g (tg/open-in-memory-graph)]
    (is (tg/open? g))
    (tg/close g)))



;;
;; Vertices
;;

(deftest test-adding-a-vertex
  (let [g (tg/open-in-memory-graph)
        v (tg/add-vertex g {:name "Titanium" :language "Clojure"})]
    (is (.getId v))
    (is (= "Titanium" (.getProperty v "name")))))

(deftest test-getting-property-names
  (let [g  (tg/open-in-memory-graph)
        v  (tg/add-vertex g {:station "Boston Manor" :lines #{"Piccadilly"}})
        xs (te/property-names v)]
    (is (= #{"station" "lines"} xs))))

(deftest test-getting-properties-map
  (let [g  (tg/open-in-memory-graph)
        m  {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        v  (tg/add-vertex g m)
        m' (te/properties-of v)]
    (is (= m m'))))

(deftest test-getting-vertex-id
  (let [g  (tg/open-in-memory-graph)
        m  {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        v  (tg/add-vertex g m)]
    (is (te/id-of v))))

(deftest test-associng-properties-map
  (let [g  (tg/open-in-memory-graph)
        m  {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        v  (tg/add-vertex g m)
        _  (te/assoc! v "opened-in" 1883 "has-wifi?" false)
        m' (te/properties-of v)]
    (is (= (assoc m "opened-in" 1883 "has-wifi?" false) m'))))

(deftest test-dissocing-properties-map
  (let [g  (tg/open-in-memory-graph)
        m  {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        v  (tg/add-vertex g m)
        _  (te/dissoc! v "lines")
        m' (te/properties-of v)]
    (is (= {"station" "Boston Manor"} m'))))

(deftest test-adding-vertices-with-the-same-id-twice
  (let [g   (tg/open-in-memory-graph)
        m   {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        v1  (tg/add-vertex g 50 m)
        v2  (tg/add-vertex g 50 m)]
    ;; Titan seems to be ignoring provided ids, which the Blueprints API
    ;; implementations are allowed to ignore according to the docs. MK.
    (is (not (= (te/id-of v1) (te/id-of v2))))))

(deftest test-get-all-vertices
  (let [g  (tg/open-in-memory-graph)
        m1 {:age 28 :name "Michael"}
        m2 {:age 26 :name "Alex"}
        v1 (tg/add-vertex g m1)
        v2 (tg/add-vertex g m2)]
    ))

;;
;; Edges
;;

(deftest test-getting-edge-head-and-tail
  (let [g  (tg/open-in-memory-graph)
        m1 {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        m2 {"station" "Northfields"  "lines" #{"Piccadilly"}}
        v1 (tg/add-vertex g m1)
        v2 (tg/add-vertex g m2)
        e  (tg/add-edge g v1 v2 "links")]
    (is (= "links" (ted/label-of e)))
    (is (= v2 (ted/head-vertex e)))
    (is (= v1 (ted/tail-vertex e)))))

#_ (deftest test-getting-edge-head-and-tail-via-fancy-macro
  (let [g  (tg/open-in-memory-graph)
        m1 {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        m2 {"station" "Northfields"  "lines" #{"Piccadilly"}}]
    (tg/populate g
                 (m1 -links-> m2))))
