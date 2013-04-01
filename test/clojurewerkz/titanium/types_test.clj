(ns clojurewerkz.titanium.types-test
  (:import (com.tinkerpop.blueprints Vertex Edge Direction Graph))
  (:use [clojure.test]
        [clojurewerkz.titanium.conf :only (clear-db conf)])
  (:require [clojurewerkz.titanium.types :as tt]
            [clojurewerkz.titanium.graph :as tg]))

(deftest test-create-groups
  (clear-db)
  (tg/open conf)
  (tg/transact!
   (let [my-group-name "My-Group-Name"
         my-group (tt/create-group 100 my-group-name)
         default-group-id (.getID tt/default-group)]
     (is (= my-group-name (.getName my-group))
         "The group has the correct name")
     (is (= 100 (.getID my-group))
         "The group has the correct ID")
     (is (not (= default-group-id (.getID my-group)))
         "my-group has a different ID from the default group")))
  (tg/shutdown))

(deftest test-create-vertex-key
  (clear-db)
  (tg/open conf)
  (tg/transact!

   (testing "With no parameters."
     (tt/create-property-key :first-key Integer)
     (let [k (tt/get-type :first-key)]
       (is (.isPropertyKey k))
       (is (not (.isEdgeLabel k)))
       (is (= "first-key" (.getName k)))
       (is (not (.hasIndex k "first-key" Vertex)))
       (is (not (.hasIndex k "first-key" Edge)))
       (is (not (.isUnique k Direction/OUT)))
       (is (not (.isUnique k Direction/IN))))))
  (tg/transact! 
   (testing "With indexed vertex."
     (tt/create-property-key :second-key Integer
                             {:indexed-vertex? true})
     (let [k (tt/get-type :second-key)]
       (is (.isPropertyKey k))
       (is (not (.isEdgeLabel k)))
       (is (= "second-key" (.getName k)))
       (is (.hasIndex k "standard" Vertex))
       (is (not (.hasIndex k "search" Edge)))
       (is (not (.isUnique k Direction/OUT)))
       (is (not (.isUnique k Direction/IN))))))

  (tg/transact! 
   (testing "With indexed vertex."
     (tt/create-property-key :third-key Integer
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

  (tg/transact! 
   (testing "With indexed edge."
     (tt/create-property-key :fourth-key Integer
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

  (tg/transact! 
   (testing "With searchable vertex."
     (tt/create-property-key :fifth-key Integer
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

  (tg/transact! 
   (testing "With searchable edge."
     (tt/create-property-key :sixth-key Integer
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

  (tg/transact! 
   (testing "Search all the things."
     (tt/create-property-key :seventh-key Integer
                             {:indexed-edge? true
                              :indexed-vertex? true
                              :searchable? true
                              :unique-direction :out})
     (let [k (tt/get-type :seventh-key)]
       (is (.isPropertyKey k))
       (is (not (.isEdgeLabel k)))
       (is (= "seventh-key" (.getName k)) )
       (is (not (.hasIndex k "standard" Vertex)))
       (is (.hasIndex k "search" Vertex))
       (is (not (.hasIndex k "standard" Edge)))
       (is (.hasIndex k "search" Edge))
       (is (.isUnique k Direction/OUT))
       (is (not (.isUnique k Direction/IN)))))))
  ;;TODO add in tests for unique-direction in
  ;;TODO add in tests for unique-locking

(deftest test-create-edge-label
  (clear-db)
  (tg/open conf)

  (tg/transact!
   (testing "With no parameters"
     (tt/create-edge-label :first-label)
     (let [lab (tt/get-type :first-label)]
       (is (.isEdgeLabel lab))
       (is (not (.isPropertyKey lab)))
       (is (= "first-label" (.getName lab)))
       (is (.isDirected lab))
       (is (not (.isUnidirected lab)))
       (is (not (.isUnique lab Direction/IN)))
       (is (not (.isUnique lab Direction/OUT)))
       (is (= tt/default-group (.getGroup lab)) "the label has the default group"))))

  (tg/transact!
   (testing "Unidirected "
     (let [test-group (tt/create-group 60 "test")
           label (tt/create-edge-label :second-label {:direction "unidirected"
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
       (is (= test-group (.getGroup lab)) "the label has the default group"))))

  (tg/shutdown))