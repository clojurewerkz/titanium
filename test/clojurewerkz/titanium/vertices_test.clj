(ns clojurewerkz.titanium.vertices-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted]
            [clojurewerkz.titanium.types    :as tt])
  (:use clojure.test
        [clojurewerkz.titanium.conf :only (clear-db conf)])
  (:import (com.thinkaurelius.titan.graphdb.vertices StandardVertex)))

(deftest vertex-test
  (clear-db)
  (tg/open conf)

  (tg/transact!
   (tt/defkey-once :vname String {:indexed-vertex? true
                                  :unique-direction :out})
   (tt/defkey-once :age Long {:indexed-vertex? true
                              :unique-direction :out})
   (tt/defkey-once :first-name String {:indexed-vertex? true
                                       :unique-direction :out})
   (tt/defkey-once :last-name String {:indexed-vertex? true
                                      :unique-direction :out}))

  (testing "Adding a vertex."
    (tg/transact!
     (let [v (tv/create! {:name "Titanium" :language "Clojure"})]
       (is (.getId v))
       (is (= "Titanium" (.getProperty v "name"))))))

  (testing "Deletion of vertices."
    (tg/transact!
     (let [u (tv/create! {:vname "uniquename"})
           u-id (tv/id-of u)]
       (tv/remove! u)
       (is (=  nil (tv/find-by-id u-id)))
       (is (empty? (tv/find-by-kv :vname "uniquename"))))))

  (testing "Creating and deleting a node with properties"
    (tg/transact!
     (let [v        (tv/create! {:name "Gerard" :value "test"})
           id       (tv/id-of v)]
       (tv/remove! v)
       (is (nil? (tv/find-by-id id))))))

  (testing "Simple property mutation."
    (tg/transact!
     (let [u (tv/create! {:a 1 :b 1})]
       (tv/assoc! u :b 2)
       (tv/dissoc! u :a)
       (is (= 2   (tv/get u :b)))
       (is (= nil (tv/get u :a))))))

  (testing "Multiple property mutation."
    (tg/transact!
     (let [u (tv/create! {:a 1 :b 1 :d 1})]
       (tv/assoc! u :b 2 :c 3)
       (tv/dissoc! u :a :d)
       (is (= nil (tv/get u :a)))
       (is (= 2   (tv/get u :b)))
       (is (= 3   (tv/get u :c)))
       (is (= nil (tv/get u :d))))))

  (testing "Merge single map."
    (tg/transact!
     (let [u (tv/create! {:a 0 :b 2})]
       (tv/merge! u {:a 1 :b 2 :c 3})
       (is (= 1   (tv/get u :a)))
       (is (= 2   (tv/get u :b)))
       (is (= 3   (tv/get u :c))))))

  (testing "Merging multiple maps."
    (tg/transact!
     (let [data     {:name "Gerard" :age 30}
           v        (tv/create! data)
           v'       (tv/merge! v {:age 31} {:position "Industrial Designer"})]
       (is (= v v'))
       (is (= {:name "Gerard" :age 31 :position "Industrial Designer"}
              (dissoc (tv/to-map v) :__id__)))
       (are [k val] (is (= val (tv/get v k)))
            :name "Gerard" :age 31))))

  (testing "Updating node properties with fn."
    (tg/transact!
     (let [data     {:name "Gerard" :age 30}
           v        (tv/create! data)
           v'       (tv/update! v :age inc)]
       (is (= v v'))
       (is (= {:name "Gerard" :age 31} (dissoc (tv/to-map v) :__id__)))
       (are [k val] (is (= val (tv/get v k)))
            :name "Gerard" :age 31))))

  (testing "Clearing node properties."
    (tg/transact!
     (let [data     {:name "Gerard" :age 30}
           v        (tv/create! data)
           v'       (tv/clear! v)]
       (is (= v v'))
       (is (= {} (dissoc (tv/to-map v) :__id__))))))

  (testing "Property map."
    (tg/transact!
     (let [v1 (tv/create! {:a 1 :b 2 :c 3})
           prop-map (tv/to-map v1)]
       (is (= 1 (prop-map :a)))
       (is (= 2 (prop-map :b)))
       (is (= 3 (prop-map :c))))))

  (testing "Associng property map."
    (tg/transact!
     (let [m  {:station "Boston Manor" :lines #{"Piccadilly"}}
           v  (tv/create! m)]
       ;;TODO this should be false, but for some reason it is
       ;;returning null.
       (ted/assoc! v :opened-in 1883  :has-wifi? "false")
       (is (= (assoc m :opened-in 1883 :has-wifi? "false")
              (dissoc (tv/to-map v) :__id__))))))

  (testing "Dissocing property map."
    (tg/transact!
     (let [m {:station "Boston Manor" :lines #{"Piccadilly"}}
           v (tv/create! m)]
       (ted/dissoc! v "lines")
       (is (= {:station "Boston Manor"} (dissoc (tv/to-map v) :__id__))))))

  (testing "Accessing a non existent node."
    (tg/transact!
     (is (nil? (tv/find-by-id 12388888888)))))

  (testing "Find by single id."
    (tg/transact!
     (let [v1 (tv/create! {:prop 1})
           v1-id (tv/id-of v1)
           v1-maybe (tv/find-by-id v1-id)]
       (is (= 1 (tv/get v1-maybe :prop))))))

  (testing "Find by multiple ids."
    (tg/transact!
     (let [v1 (tv/create! {:prop 1})
           v2 (tv/create! {:prop 2})
           v3 (tv/create! {:prop 3})
           ids (map tv/id-of [v1 v2 v3])
           v-maybes (apply tv/find-by-id ids)]
       (is (= (range 1 4) (map #(tv/get % :prop) v-maybes))))))

  (testing "Find by kv."
    (tg/transact!
     (let [v1 (tv/create! {:age 1 :vname "A"})
           v2 (tv/create! {:age 2 :vname "B"})
           v3 (tv/create! {:age 2 :vname "C"})]
       (is (= #{"A"}
              (set (map #(tv/get % :vname) (tv/find-by-kv :age 1)))))
       (is (= #{"B" "C"}
              (set (map #(tv/get % :vname) (tv/find-by-kv :age 2))))))))

  (testing "Get all vertices."
    (tg/transact!
     (let [v1 (tv/create! {:age 28 :name "Michael"})
           v2 (tv/create! {:age 26 :name "Alex"})
           xs (set (tv/get-all-vertices))]
       ;; TODO CacheVertex's are hanging around
       (is (= #{v1 v2} (set (filter #(= (type %) StandardVertex) xs)))))))

  (testing "Creating then immediately accessing a node without properties."
    (tg/transact!
     (let [created (tv/create! {})
           fetched (tv/find-by-id (tv/id-of created))]
       (is (= (tv/id-of created) (tv/id-of fetched)))
       (is (= (tv/to-map created) (tv/to-map fetched))))))

  (testing "Creating and immediately accessing a node with properties."
    (tg/transact!
     (let [created (tv/create! {:key "value"})
           fetched (tv/find-by-id (tv/id-of created))]
       (is (= (tv/id-of created) (tv/id-of fetched)))
       (is (= (tv/to-map created) (tv/to-map fetched))))))

  (testing "Upsert!"
    (tg/transact!
     (let [v1-a (tv/upsert! :first-name
                            {:first-name "Zack" :last-name "Maril" :age 21})
           v1-b (tv/upsert! :first-name
                            {:first-name "Zack" :last-name "Maril" :age 22})
           v2   (tv/upsert! :first-name
                            {:first-name "Brooke" :last-name "Maril" :age 19})]
       (is (= 22
              (tv/get (tv/refresh (first v1-a)) :age)
              (tv/get (tv/refresh (first v1-b)) :age)))
       (tv/upsert! :last-name {:last-name "Maril"
                               :heritage "Some German Folks"})
       (is (= "Some German Folks"
              (tv/get (tv/refresh (first v1-a)) :heritage)
              (tv/get (tv/refresh (first v1-b)) :heritage)
              (tv/get (tv/refresh (first v2)) :heritage))))))

  (testing "Unique upsert!"
    (tg/transact!
     (let [v1-a (tv/unique-upsert! :first-name
                                   {:first-name "Zack" :last-name "Maril" :age 21})
           v1-b (tv/unique-upsert! :first-name
                                   {:first-name "Zack" :last-name "Maril" :age 22})
           v2   (tv/unique-upsert! :first-name
                                   {:first-name "Brooke" :last-name "Maril" :age 19})]
       (is (= 22
              (tv/get (tv/refresh v1-a) :age)
              (tv/get (tv/refresh v1-b) :age)))
       (is (thrown-with-msg? Throwable #"There were 2 vertices returned."
                             (tv/unique-upsert! :last-name {:last-name "Maril"}))))))

  (tg/shutdown)
  (clear-db))
