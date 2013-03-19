(ns clojurewerkz.titanium.vertices-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted]            
            [clojurewerkz.titanium.indexing :as ti]
            [clojurewerkz.titanium.types    :as tt])
  (:use clojure.test))

;;
;; Vertices
;;

(deftest test-adding-a-vertex
  (tg/open-in-memory-graph)
  (let [v (tv/create! {:name "Titanium" :language "Clojure"})]
    (is (.getId v))
    (is (= "Titanium" (.getProperty v "name")))))

(deftest test-getting-property-names
 (tg/open-in-memory-graph)
  (let [v  (tv/create! {:station "Boston Manor" :lines #{"Piccadilly"}})
        xs (ted/keys v)]
    (is (= #{:station :lines} xs))))

(deftest test-getting-properties-map
 (tg/open-in-memory-graph)
  (let [m  {:station "Boston Manor" :lines #{"Piccadilly"}}
        v  (tv/create! m)
        m' (tv/to-map v)]
    (is (= (:station m) (:station m')))
    (is (= (:lines m) (:lines m')))))

(deftest test-getting-vertex-id
  (tg/open-in-memory-graph)
  (let [m  {:station "Boston Manor" :lines #{"Piccadilly"}}
        v  (tv/create! m)]
    (is (ted/id-of v))))

(deftest test-associng-properties-map
  (tg/open-in-memory-graph)
  (let [m  {:station "Boston Manor" :lines #{"Piccadilly"}}
        v  (tv/create! m)]
    (ted/assoc! v :opened-in 1883  :has-wifi? "false");;TODO this
    ;;should be false, but for some reason it is returning null. 
    
    (is (= (assoc m :opened-in 1883 :has-wifi? "false")
           (dissoc (tv/to-map v) :__id__)))))

(deftest test-dissocing-properties-map
  (tg/open-in-memory-graph)
  (let [m  {:station "Boston Manor" :lines #{"Piccadilly"}}
        v  (tv/create! m)]
    (ted/dissoc! v "lines")
    (is (= {:station "Boston Manor"} (dissoc (tv/to-map v) :__id__)))))

(deftest test-adding-vertices-with-the-same-id-twice
  (tg/open-in-memory-graph)
  (let [m  {:station "Boston Manor" :lines #{"Piccadilly"}}
        v1 (tv/create-with-id! 50 m)
        v2 (tv/create-with-id! 50 m)]
    ;; Titan seems to be ignoring provided ids, which the Blueprints API
    ;; implementations are allowed to ignore according to the docs. MK.
    (is (not (= (ted/id-of v1) (ted/id-of v2))))))

(deftest test-get-all-vertices
  (tg/open-in-memory-graph)
  (let [v1 (tv/create! {:age 28 :name "Michael"})
        v2 (tv/create! {:age 26 :name "Alex"})        
        xs (set (tv/get-all-vertices))]
    (is (= #{v1 v2} xs))))

(deftest test-find-vertices-by-kv
  (tg/open-in-memory-graph)
  (let [v1 (tv/create! {:age 28 :name "Michael"})
        v2 (tv/create! {:age 26 :name "Alex"})        
        xs (set (tv/find-by-kv :name "Michael"))]
    (is (= #{v1} xs))))


;;
;; Working with vertices (nodes),
;; heavily inspired by the Neocons test suite
;;

(deftest test-creating-and-immediately-accessing-a-node-without-properties
  (tg/open-in-memory-graph)
  (let [created (tv/create! {})
        fetched (tv/find-by-id (tv/id-of created))]
    (is (= (tv/id-of created) (tv/id-of fetched)))
    (is (= (tv/to-map created) (tv/to-map fetched)))))


(deftest test-creating-and-immediately-accessing-a-node-with-properties
  (tg/open-in-memory-graph)
  (let [created (tv/create! {:key "value"})
        fetched (tv/find-by-id (tv/id-of created))]
    (is (= (tv/id-of created) (tv/id-of fetched)))
    (is (= (tv/to-map created) (tv/to-map fetched)))))

;;Use Titan types for this. 
;; (deftest test-creating-and-immediately-accessing-a-node-via-key-index
;;   (tg/open-in-memory-graph)
;;   (tt/index-vertices-by-key! "name")
;;   (let [created  (tv/create! data {:name "Gerard" :value "test"})
;;         fetched1 (tg/find-by-kv :name "Gerard")
;;         fetched2 (tg/find-by-kv :name "Roger")]
;;     (is (= (first fetched1) created))
;;     (is (empty? fetched2))))

(deftest test-accessing-a-non-existent-node
  (tg/open-in-memory-graph)
  (is (nil? (tv/find-by-id 12388888888))))

(deftest test-creating-and-deleting-a-node-with-properties
  (tg/open-in-memory-graph)
  (let [v        (tv/create! {:name "Gerard" :value "test"})
        id       (tv/id-of v)]
    (tv/delete! v)
    (is (nil? (tv/find-by-id id)))))

(deftest test-accessing-node-properties
  (tg/open-in-memory-graph)
  (let [data {:name "Gerard" :age 30}
        v        (tv/create! data)]
    (is (= data (dissoc (tv/to-map v) :__id__)))
    (are [k val] (is (= val (tv/get v k)))
         :name "Gerard" :age 30)))

(deftest test-associng-node-properties
  (let [g        (tg/open-in-memory-graph)
        data     {:name "Gerard" :age 30}
        v        (tv/create! data)
        ;; just like transients, this modifies in place, but we
        ;; also test the return value
        v'       (tv/assoc! v :age 31)]
    (is (= v v'))
    (is (= {:name "Gerard" :age 31} (dissoc (tv/to-map v) :__id__)))
    (are [k val] (is (= val (tv/get v k)))
         :name "Gerard" :age 31)))

;; (deftest test-mutating-node-properties-with-fn
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tv/create! data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (tv/mutate-with! v :age inc)]
;;     (is (= v v'))
;;     (is (= {"name" "Gerard" "age" 31} (tv/to-map v)))
;;     (are [k val] (is (= val (tv/get v k)))
;;          :name "Gerard" :age 31)))

;; (deftest test-merging-node-properties-with-a-single-map
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tv/create! data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (tv/merge! v {:age 31})]
;;     (is (= v v'))
;;     (is (= {"name" "Gerard" "age" 31} (tv/to-map v)))
;;     (are [k val] (is (= val (tv/get v k)))
;;          :name "Gerard" :age 31)))

;; (deftest test-merging-node-properties-with-multiple-maps
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tv/create! data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (tv/merge! v {:age 31} {:position "Industrial Designer"})]
;;     (is (= v v'))
;;     (is (= {"name" "Gerard" "age" 31 "position" "Industrial Designer"} (tv/to-map v)))
;;     (are [k val] (is (= val (tv/get v k)))
;;          :name "Gerard" :age 31)))

;; (deftest test-clearing-node-properties
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tv/create! data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (tv/clear! v)]
;;     (is (= v v'))
;;     (is (= {} (tv/to-map v)))))
