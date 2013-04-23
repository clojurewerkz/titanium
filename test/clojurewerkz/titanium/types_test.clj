(ns clojurewerkz.titanium.types-test
  (:import (com.tinkerpop.blueprints Vertex Edge Direction Graph))
  (:use [clojure.test]
        [clojurewerkz.titanium.conf :only (clear-db conf)])
  (:require [clojurewerkz.titanium.types :as tt]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.graph :as tg]))

(deftest test-types
  (clear-db)
  (tg/open conf)

  (testing "Create group."
    (tg/transact!
     (let [my-group-name "My-Group-Name"
           my-group (tt/defgroup 100 my-group-name)
           default-group-id (.getID tt/default-group)]
       (is (= my-group-name (.getName my-group))
           "The group has the correct name")
       (is (= 100 (.getID my-group))
           "The group has the correct ID")
       (is (not (= default-group-id (.getID my-group)))
           "my-group has a different ID from the default group"))))

  (testing "Create property key."
    (testing "With no parameters."
      (tg/transact!
       (tt/defkey :first-key Integer)
       (let [k (tt/get-type :first-key)]
         (is (.isPropertyKey k))
         (is (not (.isEdgeLabel k)))
         (is (= "first-key" (.getName k)))
         (is (not (.hasIndex k "first-key" Vertex)))
         (is (not (.hasIndex k "first-key" Edge)))
         (is (not (.isUnique k Direction/OUT)))
         (is (not (.isUnique k Direction/IN))))))

    (testing "With indexed vertex."
      (tg/transact!    
       (tt/defkey :second-key Integer
                               {:indexed-vertex? true})
       (let [k (tt/get-type :second-key)]
         (is (.isPropertyKey k))
         (is (not (.isEdgeLabel k)))
         (is (= "second-key" (.getName k)))
         (is (.hasIndex k "standard" Vertex))
         (is (not (.hasIndex k "search" Edge)))
         (is (not (.isUnique k Direction/OUT)))
         (is (not (.isUnique k Direction/IN))))))

    (testing "With indexed vertex."
      (tg/transact! 
       (tt/defkey :third-key Integer
                               {:indexed-vertex? true
                                :unique-direction :out})
       (let [k (tt/get-type :third-key)]
         (is (.isPropertyKey k))
         (is (not (.isEdgeLabel k)))
         (is (= "third-key" (.getName k)))
         (is (.hasIndex k "standard" Vertex))
         (is (not (.hasIndex k "standard" Edge)))
         (is  (.isUnique k Direction/OUT))
         (is (not (.isUnique k Direction/IN))))))

    (testing "With indexed edge."
      (tg/transact! 
       (tt/defkey :fourth-key Integer
                               {:indexed-edge? true
                                :unique-direction :out})
       (let [k (tt/get-type :fourth-key)]
         (is (.isPropertyKey k) )
         (is (not (.isEdgeLabel k)))
         (is (= "fourth-key" (.getName k)))
         (is (not (.hasIndex k "standard" Vertex)))
         (is (.hasIndex k "standard" Edge))
         (is (.isUnique k Direction/OUT))
         (is (not (.isUnique k Direction/IN))))))

    (testing "With searchable vertex."
      (tg/transact! 
       (tt/defkey :fifth-key Integer
                               {:indexed-vertex? true
                                :searchable? true
                                :unique-direction :out})
       (let [k (tt/get-type :fifth-key)]
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
      (tg/transact! 
       (tt/defkey :sixth-key Integer
                               {:indexed-edge? true
                                :searchable? true
                                :unique-direction :out})
       (let [k (tt/get-type :sixth-key)]
         (is (.isPropertyKey k))
         (is (not (.isEdgeLabel k)))
         (is (= "sixth-key" (.getName k)) )
         (is (not (.hasIndex k "standard" Vertex)))
         (is (not (.hasIndex k "search" Vertex)))
         (is (not (.hasIndex k "standard" Edge)))
         (is (.hasIndex k "search" Edge))
         (is (.isUnique k Direction/OUT))
         (is (not (.isUnique k Direction/IN))))))

    (testing "Unique property in both directions."
      (tg/transact! 
       (tt/defkey :seventh-key Long
                               {:indexed-vertex? true
                                :indexed-edge? true
                                :unique-direction :both})
       (let [k (tt/get-type :seventh-key)]
         (tv/create! {:seventh-key 1})
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
                      (tv/create! {:seventh-key 1}))))))

    (testing "Search all the things."
      (tg/transact! 
       (tt/defkey :eighth-key Integer
                               {:indexed-edge? true
                                :indexed-vertex? true
                                :searchable? true
                                :unique-direction :out})
       (let [k (tt/get-type :eighth-key)]
         (is (.isPropertyKey k))
         (is (not (.isEdgeLabel k)))
         (is (= "eighth-key" (.getName k)) )
         (is (not (.hasIndex k "standard" Vertex)))
         (is (.hasIndex k "search" Vertex))
         (is (not (.hasIndex k "standard" Edge)))
         (is (.hasIndex k "search" Edge))
         (is (.isUnique k Direction/OUT))
         (is (not (.isUnique k Direction/IN)))))))

  ;;TODO add in tests for unique-direction in
  ;;TODO add in tests for unique-locking

  (testing "Create edge labels."
    (testing "With no parameters"
      (tg/transact!
       (tt/deflabel :first-label)
       (let [lab (tt/get-type :first-label)]
         (is (.isEdgeLabel lab))
         (is (not (.isPropertyKey lab)))
         (is (= "first-label" (.getName lab)))
         (is (.isDirected lab))
         (is (not (.isUnidirected lab)))
         (is (not (.isUnique lab Direction/IN)))
         (is (not (.isUnique lab Direction/OUT)))
         (is (= tt/default-group (.getGroup lab)) "the label has the default group"))))

    (testing "Unidirected, nondefault group, unique direction."
      (tg/transact!
       (let [test-group (tt/defgroup 60 "test")
             label (tt/deflabel :second-label {:direction "unidirected"
                                                        :group test-group
                                                        :unique-direction :out})
             lab (tt/get-type :second-label)]
         (is (.isEdgeLabel lab))
         (is (not (.isPropertyKey lab)))
         (is (= "second-label" (.getName lab)))
         (is (not (.isDirected lab)))
         (is (.isUnidirected lab))
         (is (not (.isUnique lab Direction/IN)))
         (is (.isUnique lab Direction/OUT))
         (is (= test-group (.getGroup lab)) "the label has the default group")))))
  (tg/shutdown))