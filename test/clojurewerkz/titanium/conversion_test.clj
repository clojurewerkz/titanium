(ns clojurewerkz.titanium.conversion-test
  (:require [clojurewerkz.titanium.graph :as tg]
            [clojurewerkz.titanium.conversion :as tc])
  (:use clojure.test)
  (:import [com.tinkerpop.blueprints Direction Query$Compare]))


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

(deftest test-conversion-to-query-compare
  (are [in out] (is (= out (tc/to-query-compare in)))
       :equal          Query$Compare/EQUAL
       Query$Compare/EQUAL Query$Compare/EQUAL
       "equal"         Query$Compare/EQUAL
       "EQUAL"         Query$Compare/EQUAL
       "="         Query$Compare/EQUAL

       :not_equal          Query$Compare/NOT_EQUAL
       Query$Compare/NOT_EQUAL Query$Compare/NOT_EQUAL
       "not_equal"         Query$Compare/NOT_EQUAL
       "NOT_EQUAL"         Query$Compare/NOT_EQUAL
       "!="         Query$Compare/NOT_EQUAL
       "=/="         Query$Compare/NOT_EQUAL

       :greater_than          Query$Compare/GREATER_THAN
       Query$Compare/GREATER_THAN Query$Compare/GREATER_THAN
       "greater_than"         Query$Compare/GREATER_THAN
       "GREATER_THAN"         Query$Compare/GREATER_THAN
       ">"                    Query$Compare/GREATER_THAN
       '>                     Query$Compare/GREATER_THAN

       :greater_than_equal          Query$Compare/GREATER_THAN_EQUAL
       Query$Compare/GREATER_THAN_EQUAL Query$Compare/GREATER_THAN_EQUAL
       "greater_than_equal"         Query$Compare/GREATER_THAN_EQUAL
       "GREATER_THAN_EQUAL"         Query$Compare/GREATER_THAN_EQUAL
       ">="                    Query$Compare/GREATER_THAN_EQUAL
       '>=                     Query$Compare/GREATER_THAN_EQUAL

       :less_than          Query$Compare/LESS_THAN
       Query$Compare/LESS_THAN Query$Compare/LESS_THAN
       "less_than"         Query$Compare/LESS_THAN
       "LESS_THAN"         Query$Compare/LESS_THAN
       "<"                    Query$Compare/LESS_THAN
       '<                     Query$Compare/LESS_THAN

       :less_than_equal          Query$Compare/LESS_THAN_EQUAL
       Query$Compare/LESS_THAN_EQUAL Query$Compare/LESS_THAN_EQUAL
       "less_than_equal"         Query$Compare/LESS_THAN_EQUAL
       "LESS_THAN_EQUAL"         Query$Compare/LESS_THAN_EQUAL
       "<="                    Query$Compare/LESS_THAN_EQUAL
       '<=                     Query$Compare/LESS_THAN_EQUAL))
