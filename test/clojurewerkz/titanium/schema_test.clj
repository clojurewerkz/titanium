(ns clojurewerkz.titanium.schema-test
  (:use [clojure.test]
        [clojurewerkz.titanium.test.support :only (graph-fixture *graph*)])
  (:require [clojurewerkz.titanium.schema :as ts]))

(use-fixtures :once graph-fixture)

(let [count (atom 0)]
  (defn next-name
    "Returns a new name each time the function is called, by incrementing a counter."
    []
    (str "label" (swap! count inc))))

(deftest test-schema

  (testing "Make vertex label (not partitioned, not static)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            label (ts/make-vertex-label mgmt (keyword label-name))]
        (is (= label-name (.getName label)))
        (is (not (.isPartitioned label)))
        (is (not (.isStatic label))))))

  (testing "Make vertex label (partitioned, not static)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            label (ts/make-vertex-label mgmt label-name :partition? true)]
        (is (= label-name (.getName label)))
        (is (.isPartitioned label))
        (is (not (.isStatic label))))))

  (testing "Make vertex label (not partitioned, static)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            label (ts/make-vertex-label mgmt label-name :static? true)]
        (is (= label-name (.getName label)))
        (is (not (.isPartitioned label)))
        (is (.isStatic label)))))

  (testing "Make vertex label (partitioned, static)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)]
        (is (thrown-with-msg? java.lang.IllegalArgumentException
                              #"A vertex label cannot be partitioned and static at the same time"
                              (ts/make-vertex-label mgmt label-name :partition? true :static? true))))))

  (testing "Make vertex label (dupliacte name)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)]
        (is (ts/make-vertex-label mgmt label-name))
        (is (thrown-with-msg? java.lang.IllegalArgumentException
                              #"violates a uniqueness constraint"
                              (ts/make-vertex-label mgmt label-name))))))

  (testing "Make edge label (default multiplicity)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            label (ts/make-edge-label mgmt label-name)]
        (is (= label-name (.getName label)))
        (is (= (ts/keyword->multiplicity :multi) (.getMultiplicity label)))
        (is (.isDirected label))
        (is (not (.isUnidirected label))))))

  (testing "Make edge label (duplicate name)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)]
        (is (ts/make-edge-label mgmt label-name))
        (is (thrown-with-msg? java.lang.IllegalArgumentException
                              #"violates a uniqueness constraint"
                              (ts/make-edge-label mgmt label-name))))))

  (testing "Make edge label (various multiplicities)"
    (ts/with-management-system [mgmt *graph*]
      (doseq [multiplicity [:multi :simple :many-to-many :many-to-one :one-to-many :one-to-one]]
        (let [label-name (next-name)
              label (ts/make-edge-label mgmt label-name :multiplicity multiplicity)]
          (is (= label-name (.getName label)))
          (is (= (ts/keyword->multiplicity multiplicity) (.getMultiplicity label)))
          (is (.isDirected label))
          (is (not (.isUnidirected label)))))))

  (testing "Make edge label (unidirected)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            label (ts/make-edge-label mgmt label-name :unidirected? true)]
        (is (= label-name (.getName label)))
          (is (= (ts/keyword->multiplicity :multi) (.getMultiplicity label)))
          (is (not (.isDirected label)))
          (is (.isUnidirected label)))))

  (testing "Make property key (default cardinality)"
    (ts/with-management-system [mgmt *graph*]
      (let [key-name (next-name)
            key (ts/make-property-key mgmt key-name String)]
        (is (= key-name (.getName key)))
        (is (= String (.getDataType key)))
        (is (= (ts/keyword->cardinality :single) (.getCardinality key))))))

  (testing "Make property key (various cardinalities)"
    (ts/with-management-system [mgmt *graph*]
      (doseq [cardinality [:single :list :set]]
        (let [key-name (next-name)
              key (ts/make-property-key mgmt key-name String :cardinality cardinality)]
          (is (= (name key-name) (.getName key)))
          (is (= String (.getDataType key)))
          (is (= (ts/keyword->cardinality cardinality) (.getCardinality key)))))))

  (testing "Build composite vertex index (single key)"
    (ts/with-management-system [mgmt *graph*]
      (let [ix-name (next-name)
            key-name (next-name)]
        (ts/make-property-key mgmt key-name String)
        (let [ix (ts/build-composite-index mgmt ix-name :vertex key-name)]
          (is (= ix-name (.getName ix)))
          (is (.isCompositeIndex ix))
          (is (not (.isMixedIndex ix)))
          (is (not (.isUnique ix)))))))

  (testing "Build composite vertex index (multiple keys)"
    (ts/with-management-system [mgmt *graph*]
      (let [ix-name (next-name)
            k1-name (next-name)
            k2-name (next-name)]
        (ts/make-property-key mgmt k1-name String)
        (ts/make-property-key mgmt k2-name String)
        (let [ix (ts/build-composite-index mgmt ix-name :vertex [k1-name k2-name])]
          (is (= ix-name (.getName ix)))
          (is (.isCompositeIndex ix))
          (is (not (.isMixedIndex ix)))
          (is (not (.isUnique ix)))))))

  (testing "Build composite vertex index (single key, unique)"
    (ts/with-management-system [mgmt *graph*]
      (let [ix-name (next-name)
            key-name (next-name)]
        (ts/make-property-key mgmt key-name String)
        (let [ix (ts/build-composite-index mgmt ix-name :vertex [key-name] :unique? true)]
          (is (= ix-name (.getName ix)))
          (is (.isCompositeIndex ix))
          (is (not (.isMixedIndex ix)))
          (is (.isUnique ix))))))

  (testing "Bulid composite vertex index (multiple keys, unique)"
    (ts/with-management-system [mgmt *graph*]
      (let [ix-name (next-name)
            k1-name (next-name)
            k2-name (next-name)]
        (ts/make-property-key mgmt k1-name String)
        (ts/make-property-key mgmt k2-name Long)
        (let [ix (ts/build-composite-index mgmt ix-name :vertex [k1-name k2-name] :unique? true)]
          (is (= ix-name (.getName ix)))
          (is (.isCompositeIndex ix))
          (is (not (.isMixedIndex ix)))
          (is (.isUnique ix))))))

  (testing "Build composite vertex index (single key, index-only)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            key-name   (next-name)
            ix-name    (next-name)]
        (ts/make-vertex-label mgmt label-name)
        (ts/make-property-key mgmt key-name Long)
        (let [ix (ts/build-composite-index mgmt ix-name :vertex [key-name] :index-only label-name)]
          (is (= ix-name (.getName ix)))
          (is (.isCompositeIndex ix))
          (is (not (.isMixedIndex ix)))
          (is (not (.isUnique ix)))))))

  (testing "Build composite edge index (single key, index-only)"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            key-name   (next-name)
            ix-name    (next-name)]
        (ts/make-edge-label mgmt label-name)
        (ts/make-property-key mgmt key-name String)
        (let [ix (ts/build-composite-index mgmt ix-name :edge [key-name] :index-only label-name)]
          (is (= ix-name (.getName ix)))
          (is (.isCompositeIndex ix))
          (is (not (.isMixedIndex ix)))
          (is (not (.isUnique ix)))))))

  (testing "Build mixed edge index"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            k1-name    (next-name)
            k2-name    (next-name)
            k3-name    (next-name)
            ix-name    (next-name)]
        (ts/make-edge-label mgmt label-name)
        (ts/make-property-key mgmt k1-name String)
        (ts/make-property-key mgmt k2-name Long)
        (ts/make-property-key mgmt k3-name Double)
        (let [ix (ts/build-mixed-index mgmt ix-name :edge [k1-name k2-name k3-name]
                                       "search" :index-only label-name)]
          (is (= ix-name (.getName ix)))
          (is (not (.isCompositeIndex ix)))
          (is (.isMixedIndex ix))
          (is (not (.isUnique ix)))))))

  (testing "Build mixed vertex index"
    (ts/with-management-system [mgmt *graph*]
      (let [label-name (next-name)
            k1-name    (next-name)
            k2-name    (next-name)
            k3-name    (next-name)
            ix-name    (next-name)]
        (ts/make-vertex-label mgmt label-name)
        (ts/make-property-key mgmt k1-name String)
        (ts/make-property-key mgmt k2-name Long)
        (ts/make-property-key mgmt k3-name Double)
        (let [ix (ts/build-mixed-index mgmt ix-name :vertex [k1-name k2-name k3-name]
                                       "search" :index-only label-name)]
          (is (= ix-name (.getName ix)))
          (is (not (.isCompositeIndex ix)))
          (is (.isMixedIndex ix))
          (is (not (.isUnique ix))))))))

;; TODO: Tests for make-edge-label with signature

;; TODO: Tests for make-property-key with signature

;; TODO: Tests for vertex-centric indices - build-edge-index and build-property-index
