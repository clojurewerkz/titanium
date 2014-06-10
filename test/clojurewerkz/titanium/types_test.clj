(ns clojurewerkz.titanium.types-test
  (:import (com.tinkerpop.blueprints Vertex Edge Direction Graph))
  (:use [clojure.test]
        [clojurewerkz.titanium.test.conf :only (clear-db conf)])
  (:require [clojurewerkz.titanium.types :as tt]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges :as te]
            [clojurewerkz.titanium.graph :as tg]))

(deftest test-types
  (clear-db)

  (let [graph (tg/open conf)]
    (testing "Create property key."

      (testing "With no parameters."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :first-key Integer)
          (let [k (tt/get-type tx :first-key)]
            (is (= (.getDataType k) Integer))
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "first-key" (.getName k)))
            (is (not (.hasIndex k "standard" Vertex)))
            (is (not (.hasIndex k "standard" Edge)))
            (is (.isUnique k Direction/OUT))
            (is (not (.isUnique k Direction/IN))))))

      (testing "With indexed vertex."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :second-key Integer {:vertex-index true})
          (let [k (tt/get-type tx :second-key)]
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "second-key" (.getName k)))
            (is (.hasIndex k "standard" Vertex))
            (is (not (.hasIndex k "standard" Edge)))
            (is (not (.hasIndex k "search" Vertex)))
            (is (.isUnique k Direction/OUT))
            (is (not (.isUnique k Direction/IN))))))

      (testing "With indexed-unique vertex."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :third-key Integer {:unique? true})
          (let [k (tt/get-type tx :third-key)]
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "third-key" (.getName k)))
            (is (.hasIndex k "standard" Vertex))
            (is (not (.hasIndex k "standard" Edge)))
            (is (.isUnique k Direction/OUT))
            (is (.isUnique k Direction/IN)))))

      (testing "With indexed edge."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :fourth-key Integer {:edge-index true})
          (let [k (tt/get-type tx :fourth-key)]
            (is (.isPropertyKey k) )
            (is (not (.isEdgeLabel k)))
            (is (= "fourth-key" (.getName k)))
            (is (not (.hasIndex k "standard" Vertex)))
            (is (.hasIndex k "standard" Edge))
            (is (.isUnique k Direction/OUT))
            (is (not (.isUnique k Direction/IN))))))

      (testing "With searchable vertex."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :fifth-key Integer {:vertex-index "search"})
          (let [k (tt/get-type tx :fifth-key)]
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "fifth-key" (.getName k)) )
            (is (not (.hasIndex k "standard" Vertex)))
            (is (.hasIndex k "search" Vertex))
            (is (not (.hasIndex k "standard" Edge)))
            (is (not (.hasIndex k "search" Edge)))
            (is (.isUnique k Direction/OUT))
            (is (not (.isUnique k Direction/IN))))))

      (testing "With searchable edge."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :sixth-key Integer {:edge-index "search"})
          (let [k (tt/get-type tx :sixth-key)]
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "sixth-key" (.getName k)) )
            (is (not (.hasIndex k "standard" Vertex)))
            (is (not (.hasIndex k "search" Vertex)))
            (is (not (.hasIndex k "standard" Edge)))
            (is (.hasIndex k "search" Edge))
            (is (.isUnique k Direction/OUT))
            (is (not (.isUnique k Direction/IN))))))

      (testing "With unique indexed vertex and edge. (Note: uniqueness only applies to vertex.)"
        (tg/with-transaction [tx graph]
          (tt/defkey tx :seventh-key Long {:vertex-index true :edge-index true :unique? true})
          (let [k (tt/get-type tx :seventh-key)]
            (tv/create! tx {:seventh-key 1})
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "seventh-key" (.getName k)))
            (is (.hasIndex k "standard" Vertex))
            (is (not (.hasIndex k "search" Vertex)))
            (is (.hasIndex k "standard" Edge))
            (is (not (.hasIndex k "search" Edge)))
            (is (.isUnique k Direction/OUT))
            (is (.isUnique k Direction/IN))
            (is (thrown? java.lang.IllegalArgumentException
                         (tv/create! tx {:seventh-key 1}))))))

      (testing "Search all the things."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :eighth-key Integer {:edge-index "search" :vertex-index "search" :unique? true})
          (let [k (tt/get-type tx :eighth-key)]
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "eighth-key" (.getName k)))
            (is (.hasIndex k "standard" Vertex)) ;; Required for unique constraint
            (is (.hasIndex k "search" Vertex))
            (is (not (.hasIndex k "standard" Edge)))
            (is (.hasIndex k "search" Edge))
            (is (.isUnique k Direction/OUT))
            (is (.isUnique k Direction/IN)))))

      (testing "Unique vertex, no unique locking."
        (tg/with-transaction [tx graph]
          (tt/defkey tx :ninth-key Integer {:unique? true :unique-locked? false})
          (let [k (tt/get-type tx :ninth-key)]
            (is (.isPropertyKey k))
            (is (not (.isEdgeLabel k)))
            (is (= "ninth-key" (.getName k)))
            (is (.hasIndex k "standard" Vertex)) ;; Required for unique constraint
            (is (not (.hasIndex k "search" Vertex)))
            (is (not (.hasIndex k "standard" Edge)))
            (is (not (.hasIndex k "search" Edge)))
            (is (.isUnique k Direction/OUT))
            (is (.isUnique k Direction/IN)))))

      (testing "Attemp to re-use key name throws exception"
        (tg/with-transaction [tx graph]
          (tt/defkey tx :tenth-key Integer)
          (is (thrown-with-msg? java.lang.IllegalArgumentException
                                #"The given value .* is already used as a property"
                                (tt/defkey tx :tenth-key Integer))))))

    (testing "Create edge labels."

      (testing "With no parameters"
        (tg/with-transaction [tx graph]
         (tt/deflabel tx :first-label)
         (let [lab (tt/get-type tx :first-label)]
           (is (.isEdgeLabel lab))
           (is (not (.isPropertyKey lab)))
           (is (= "first-label" (.getName lab)))
           (is (.isDirected lab))
           (is (not (.isUnidirected lab)))
           (is (not (.isUnique lab Direction/IN)))
           (is (not (.isUnique lab Direction/OUT))))))

      ;; An edge lable is out-unique, if a vertex has at most one
      ;; outgoing edge for that label. For edges, out- and in-uniqueness
      ;; translate to cardinality constraints. Father is an exmaple of a
      ;; functional edge label, since each person has at most one
      ;; father.

      (testing "Cardinality one-to-many"
        (tg/with-transaction [tx graph]
          (tt/deflabel tx :second-label {:cardinality :one-to-many})
          (let [lab (tt/get-type tx :second-label)]
            (is (.isEdgeLabel lab))
            (is (not (.isPropertyKey lab)))
            (is (= "second-label" (.getName lab)))
            (is (.isDirected lab))
            (is (not (.isUnidirected lab)))
            (is (.isUnique lab Direction/IN))
            (is (not (.isUnique lab Direction/OUT))))))

      (testing "Cardinality many-to-one"
        (tg/with-transaction [tx graph]
         (tt/deflabel tx :third-label {:cardinality :many-to-one})
         (let [lab (tt/get-type tx :third-label)]
           (is (.isEdgeLabel lab))
           (is (not (.isPropertyKey lab)))
           (is (= "third-label" (.getName lab)))
           (is (.isDirected lab))
           (is (not (.isUnidirected lab)))
           (is (not (.isUnique lab Direction/IN)))
           (is (.isUnique lab Direction/OUT)))))

      (testing "Cardinality one-to-one"
        (tg/with-transaction [tx graph]
          (tt/deflabel tx :fourth-label {:cardinality :one-to-one})
          (let [lab (tt/get-type tx :fourth-label)]
            (is (.isEdgeLabel lab))
            (is (not (.isPropertyKey lab)))
            (is (= "fourth-label" (.getName lab)))
            (is (.isDirected lab))
            (is (not (.isUnidirected lab)))
            (is (.isUnique lab Direction/IN))
            (is (.isUnique lab Direction/OUT)))))

      (testing "Sort key"
        (tg/with-transaction [tx graph]
          (tt/defkey tx :rank Integer)
          (tt/deflabel tx :fifth-label {:sort-key :rank})
          (tt/deflabel tx :fifth-label-r {:sort-key :rank, :sort-order :desc})
          (let [lab (tt/get-type tx :fifth-label)]
            (is (.isEdgeLabel lab))
            (is (not (.isPropertyKey lab)))
            (is (= "fifth-label" (.getName lab)))
            (is (.isDirected lab))
            (is (not (.isUnidirected lab)))
            (is (not (.isUnique lab Direction/IN)))
            (is (not (.isUnique lab Direction/OUT))))
          (let [v1 (tv/create! tx {:name "v1" :rank 3})
                v2 (tv/create! tx {:name "v2" :rank 2})
                v3 (tv/create! tx {:name "v3" :rank 1})]
            (te/connect! tx v1 :fifth-label v2)
            (te/connect! tx v1 :fifth-label v3)
            (te/connect! tx v1 :fifth-label-r v2)
            (te/connect! tx v1 :fifth-label-r v3)
            ;; TODO @ray1729: is there a method we can call to return
            ;; vertices sorted by the sort-key defined on the label? If
            ;; so, we should add tests here.
            (let [vs (seq (tv/connected-out-vertices v1 :fifth-label))]
              (is (= (count vs) 2)))
            (let [vs (seq (tv/connected-out-vertices v1 :fifth-label-r))]
              (is (= (count vs) 2))))))

      ;; TODO
      ;; Test composite sort-key
      ;; Test signature
      ;; Test unique-locked? false

      )

    (tg/shutdown graph)))
