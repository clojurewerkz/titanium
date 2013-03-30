(ns clojurewerkz.titanium.element-test
  (:use [clojure.test]
        [clojurewerkz.titanium.conf :only (conf clear-db)])
  (:import [com.thinkaurelius.titan.graphdb.relations RelationIdentifier])
  (:require [clojurewerkz.titanium.graph :as g]
            [clojurewerkz.titanium.vertices :as v]
            [clojurewerkz.titanium.edges :as e]))

(deftest element-test
  (clear-db)
  (g/open conf)  
  (testing "Get keys"
    (g/transact! (let [a (v/create! {:name "v1" :a 1 :b 1})
                       b (v/create! {:name "v2" :a 1 :b 1})
                       c (e/connect! a :test-label b {:prop "e1" :a 1 :b 1})
                       coll-a (v/keys a)
                       coll-b (v/keys b)
                       coll-c (v/keys c)]
                   (is (= #{:name :a :b} coll-a coll-b))
                   (is (= #{:prop :a :b} coll-c))
                   (is (= clojure.lang.PersistentHashSet (type coll-a))))))

  (testing "Get id"
    (g/transact!
     (let [a (v/create!)
           b (v/create!)
           c (e/connect! a :test-label b )]
       (is (= java.lang.Long (type (v/id-of a))))
       (is (= RelationIdentifier (type (e/id-of c)))))))

  (testing "Remove property!"
    (g/transact!
     (let [a (v/create! {:a 1})
           b (v/create!)
           c (e/connect! a :test-label b {:a 1})]
       (v/dissoc! a :a)
       (v/dissoc! c :a)
       (is (nil? (:a (v/to-map a))))
       (is (nil? (:a (v/to-map a)))))))
  (g/shutdown))