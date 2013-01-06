(ns clojurewerkz.titanium.graph-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.elements :as te])
  (:use clojure.test)
  (:import java.io.File))


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
