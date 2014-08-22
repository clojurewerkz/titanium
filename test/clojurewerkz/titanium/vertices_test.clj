(ns clojurewerkz.titanium.vertices-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted]
            [clojurewerkz.titanium.schema   :as ts])
  (:use clojure.test
        [clojurewerkz.titanium.test.support :only (*graph* graph-fixture)])
  (:import (com.thinkaurelius.titan.graphdb.vertices StandardVertex)))

(use-fixtures :once graph-fixture)

(deftest vertex-test
  (ts/with-management-system [mgmt *graph*]
    (ts/make-property-key mgmt :vname      String)
    (ts/make-property-key mgmt :age        Long)
    (ts/make-property-key mgmt :first-name String)
    (ts/make-property-key mgmt :last-name  String)
    (ts/build-composite-index mgmt "vname-ix" :vertex [:vname] :unique? true)
    (ts/build-composite-index mgmt "age-ix" :vertex [:age])
    (ts/build-composite-index mgmt "first-name-ix" :vertex [:first-name])
    (ts/build-composite-index mgmt "last-name-ix" :vertex [:last-name]))

  (testing "Adding a vertex."
    (tg/with-transaction [tx *graph*]
      (let [v (tv/create! tx {:name "Titanium" :language "Clojure"})]
        (is (.getId v))
        (is (= "Titanium" (.getProperty v "name"))))))

  (testing "Deletion of vertices."
    (tg/with-transaction [tx *graph*]
      (let [u  (tv/create! tx {:vname "uniquename"})
            id (tv/id-of u)]
        (tv/remove! tx u)
        (is (nil? (tv/find-by-id tx id)))
        (is (empty? (tv/find-by-kv tx :vname "uniquename"))))))

  (testing "Creating and deleting a vertex with properties"
    (tg/with-transaction [tx *graph*]
      (let [v  (tv/create! tx {:name "Gerard" :value "test"})
            id (tv/id-of v)]
        (tv/remove! tx v)
        (is (nil? (tv/find-by-id tx id))))))

  (testing "Simple property mutation."
    (tg/with-transaction [tx *graph*]
      (let [u (tv/create! tx {:a 1 :b 1})]
        (tv/assoc! u :b 2)
        (tv/dissoc! u :a)
        (is (= 2  (tv/get u :b)))
        (is (nil? (tv/get u :a))))))

  (testing "Multiple property mutation."
    (tg/with-transaction [tx *graph*]
      (let [u (tv/create! tx {:a 1 :b 1 :d 1})]
        (tv/assoc! u :b 2 :c 3)
        (tv/dissoc! u :a :d)
        (is (nil? (tv/get u :a)))
        (is (= 2  (tv/get u :b)))
        (is (= 3  (tv/get u :c)))
        (is (nil? (tv/get u :d))))))

  (testing "Merge single map."
    (tg/with-transaction [tx *graph*]
      (let [u (tv/create! tx {:a 0 :b 2})]
        (tv/merge! u {:a 1 :b 2 :c 3})
        (is (= 1   (tv/get u :a)))
        (is (= 2   (tv/get u :b)))
        (is (= 3   (tv/get u :c))))))

  (testing "Merging multiple maps."
    (tg/with-transaction [tx *graph*]
     (let [data     {:name "Gerard" :age 30}
           v        (tv/create! tx data)
           v'       (tv/merge! v {:age 31} {:position "Industrial Designer"})]
       (is (= v v'))
       (is (= {:name "Gerard" :age 31 :position "Industrial Designer"}
              (dissoc (tv/to-map v) :__id__)))
       (are [k val] (is (= val (tv/get v k)))
            :name "Gerard" :age 31))))

  (testing "Updating node properties with fn."
    (tg/with-transaction [tx *graph*]
     (let [data     {:name "Gerard" :age 30}
           v        (tv/create! tx data)
           v'       (tv/update! v :age inc)]
       (is (= v v'))
       (is (= {:name "Gerard" :age 31} (dissoc (tv/to-map v) :__id__)))
       (are [k val] (is (= val (tv/get v k)))
            :name "Gerard" :age 31))))

  (testing "Clearing node properties."
    (tg/with-transaction [tx *graph*]
     (let [data     {:name "Gerard" :age 30}
           v        (tv/create! tx data)
           v'       (tv/clear! v)]
       (is (= v v'))
       (is (= {} (dissoc (tv/to-map v) :__id__))))))

  (testing "Property map."
    (tg/with-transaction [tx *graph*]
      (let [v1 (tv/create! tx {:a 1 :b 2 :c 3})
            prop-map (tv/to-map v1)]
        (is (= 1 (prop-map :a)))
        (is (= 2 (prop-map :b)))
        (is (= 3 (prop-map :c))))))

  (testing "Associng property map."
    (tg/with-transaction [tx *graph*]
     (let [m  {:station "Boston Manor" :lines #{"Piccadilly"}}
           v  (tv/create! tx m)]
       ;;TODO this should be false, but for some reason it is
       ;;returning null.
       (ted/assoc! v :opened-in 1883  :has-wifi? "false")
       (is (= (assoc m :opened-in 1883 :has-wifi? "false")
              (dissoc (tv/to-map v) :__id__))))))

  (testing "Dissocing property map."
    (tg/with-transaction [tx *graph*]
      (let [m {:station "Boston Manor" :lines #{"Piccadilly"}}
            v (tv/create! tx m)]
        (ted/dissoc! v "lines")
        (is (= {:station "Boston Manor"} (dissoc (tv/to-map v) :__id__))))))

  (testing "Accessing a non existent node."
    (tg/with-transaction [tx *graph*]
      (is (nil? (tv/find-by-id tx 12388888888)))))

  (testing "Find by single id."
    (tg/with-transaction [tx *graph*]
      (let [v1 (tv/create! tx {:prop 1})
            v1-id (tv/id-of v1)
            v1-maybe (tv/find-by-id tx v1-id)]
        (is (= 1 (tv/get v1-maybe :prop))))))

  (testing "Find by multiple ids."
    (tg/with-transaction [tx *graph*]
     (let [v1 (tv/create! tx {:prop 1})
           v2 (tv/create! tx {:prop 2})
           v3 (tv/create! tx {:prop 3})
           ids (map tv/id-of [v1 v2 v3])
           v-maybes (apply tv/find-by-id tx ids)]
       (is (= (range 1 4) (map #(tv/get % :prop) v-maybes))))))

  (testing "Find by kv."
    (tg/with-transaction [tx *graph*]
     (let [v1 (tv/create! tx {:age 1 :vname "A"})
           v2 (tv/create! tx {:age 2 :vname "B"})
           v3 (tv/create! tx {:age 2 :vname "C"})]
       (is (= #{"A"}
              (set (map #(tv/get % :vname) (tv/find-by-kv tx :age 1)))))
       (is (= #{"B" "C"}
              (set (map #(tv/get % :vname) (tv/find-by-kv tx :age 2))))))))

  (testing "Get all vertices."
    (tg/with-transaction [tx *graph*]
      (let [v1 (tv/create! tx {:age 28 :name "Michael"})
            v2 (tv/create! tx {:age 26 :name "Alex"})
            xs (set (tv/get-all-vertices tx))]
        ;; TODO CacheVertex's are hanging around
        (is (= #{v1 v2} (set (filter #(= (type %) StandardVertex) xs)))))))

  (testing "Creating then immediately accessing a node without properties."
    (tg/with-transaction [tx *graph*]
      (let [created (tv/create! tx {})
            fetched (tv/find-by-id tx (tv/id-of created))]
        (is (= (tv/id-of created) (tv/id-of fetched)))
        (is (= (tv/to-map created) (tv/to-map fetched))))))

  (testing "Creating and immediately accessing a node with properties."
    (tg/with-transaction [tx *graph*]
      (let [created (tv/create! tx {:key "value"})
            fetched (tv/find-by-id tx (tv/id-of created))]
        (is (= (tv/id-of created) (tv/id-of fetched)))
        (is (= (tv/to-map created) (tv/to-map fetched))))))

  (testing "Upsert!"
    (tg/with-transaction [tx *graph*]
      (let [v1-a (tv/upsert! tx :first-name
                             {:first-name "Zack" :last-name "Maril" :age 21})
            v1-b (tv/upsert! tx :first-name
                             {:first-name "Zack" :last-name "Maril" :age 22})
            v2   (tv/upsert! tx :first-name
                             {:first-name "Brooke" :last-name "Maril" :age 19})]
        (is (= 22
               (tv/get (tv/refresh tx (first v1-a)) :age)
               (tv/get (tv/refresh tx (first v1-b)) :age)))
        (tv/upsert! tx :last-name {:last-name "Maril"
                                   :heritage "Some German Folks"})
        (is (= "Some German Folks"
               (tv/get (tv/refresh tx (first v1-a)) :heritage)
               (tv/get (tv/refresh tx (first v1-b)) :heritage)
               (tv/get (tv/refresh tx (first v2)) :heritage))))))

  (testing "Unique upsert!"
    (tg/with-transaction [tx *graph*]
     (let [v1-a (tv/unique-upsert! tx :first-name
                                   {:first-name "Zack" :last-name "Maril" :age 21})
           v1-b (tv/unique-upsert! tx :first-name
                                   {:first-name "Zack" :last-name "Maril" :age 22})
           v2   (tv/unique-upsert! tx :first-name
                                   {:first-name "Brooke" :last-name "Maril" :age 19})]
       (is (= 22
              (tv/get (tv/refresh tx v1-a) :age)
              (tv/get (tv/refresh tx v1-b) :age)))
       (is (thrown-with-msg? Throwable #"There were 2 vertices returned."
                             (tv/unique-upsert! tx :last-name {:last-name "Maril"}))))))

  (testing "Add vertex with label"
    (ts/with-management-system [mgmt *graph*]
      (ts/make-vertex-label mgmt "Foo"))
    (tg/with-transaction [tx *graph*]
      (let [v1 (tv/create-with-label! tx "Foo")]
        (is (.getId v1))
        (is (= "Foo" (.getLabel v1))))
      (let [v2 (tv/create-with-label! tx "Foo" {:first-name "Zack"})]
        (is (.getId v2))
        (is (= "Foo" (.getLabel v2)))
        (is (= "Zack" (.getProperty v2 "first-name")))))))
