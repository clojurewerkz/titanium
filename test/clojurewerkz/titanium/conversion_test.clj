(ns clojurewerkz.titanium.conversion-test
  (:require [clojurewerkz.titanium.graph :as tg]
            [clojurewerkz.titanium.conversion :as tc])
  (:use clojure.test)
  (:import [com.tinkerpop.blueprints Direction]))


(deftest test-conversion-to-direction
  (are [in out] (is (= out (tc/to-edge-direction in)))
    :in          Direction/IN
    Direction/IN Direction/IN
    "in"         Direction/IN
    "IN"         Direction/IN

    :out          Direction/OUT
    Direction/OUT Direction/OUT
    "out"         Direction/OUT
    "OUT"         Direction/OUT

    :both          Direction/BOTH
    Direction/BOTH Direction/BOTH
    "both"         Direction/BOTH
    "BOTH"         Direction/BOTH))
