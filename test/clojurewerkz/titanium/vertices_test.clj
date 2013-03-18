;; (ns clojurewerkz.titanium.vertices-test
;;   (:require [clojurewerkz.titanium.graph    :as tg]
;;             [clojurewerkz.titanium.elements :as te]
;;             [clojurewerkz.titanium.indexing :as ti])
;;   (:use clojure.test))


;; ;;
;; ;; Working with vertices (nodes),
;; ;; heavily inspired by the Neocons test suite
;; ;;

;; (deftest test-creating-and-immediately-accessing-a-node-without-properties
;;   (let [g       (tg/open-in-memory-graph)
;;         m       {}
;;         created (tg/add-vertex g m)
;;         fetched (tg/get-vertex g (te/id-of created))]
;;     (is (= (te/id-of created) (te/id-of fetched)))
;;     (is (= (te/properties-of created) (te/properties-of fetched)))))


;; (deftest test-creating-and-immediately-accessing-a-node-with-properties
;;   (let [g       (tg/open-in-memory-graph)
;;         m       {:key "value"}
;;         created (tg/add-vertex g m)
;;         fetched (tg/get-vertex g (te/id-of created))]
;;     (is (= (te/id-of created) (te/id-of fetched)))
;;     (is (= (te/properties-of created) (te/properties-of fetched)))))

;; (deftest test-creating-and-immediately-accessing-a-node-via-key-index
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :value "test"}
;;         _        (tg/index-vertices-by-key! g "name")
;;         created  (tg/add-vertex g data)
;;         fetched1 (tg/get-vertices g "name" "Gerard")
;;         fetched2 (tg/get-vertices g "name" "Roger")]
;;     (is (= (first fetched1) created))
;;     (is (empty? fetched2))))

;; (deftest test-accessing-a-non-existent-node
;;   (let [g    (tg/open-in-memory-graph)
;;         v    (tg/get-vertex g 12388888888)]
;;     (is (nil? v))))

;; (deftest test-creating-and-deleting-a-node-with-properties
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :value "test"}
;;         v        (tg/add-vertex g data)
;;         id       (te/id-of v)]
;;     (tg/remove-vertex g v)
;;     (is (nil? (tg/get-vertex g id)))))

;; (deftest test-accessing-node-properties
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tg/add-vertex g data)]
;;     (is (= {"name" "Gerard" "age" 30} (te/properties-of v)))
;;     (is (= data (te/properties-of v true)))
;;     (are [k val] (is (= val (te/property-of v k)))
;;          :name "Gerard" :age 30)))

;; (deftest test-associng-node-properties
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tg/add-vertex g data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (te/assoc! v :age 31)]
;;     (is (= v v'))
;;     (is (= {"name" "Gerard" "age" 31} (te/properties-of v)))
;;     (are [k val] (is (= val (te/property-of v k)))
;;          :name "Gerard" :age 31)))

;; (deftest test-mutating-node-properties-with-fn
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tg/add-vertex g data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (te/mutate-with! v :age inc)]
;;     (is (= v v'))
;;     (is (= {"name" "Gerard" "age" 31} (te/properties-of v)))
;;     (are [k val] (is (= val (te/property-of v k)))
;;          :name "Gerard" :age 31)))

;; (deftest test-merging-node-properties-with-a-single-map
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tg/add-vertex g data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (te/merge! v {:age 31})]
;;     (is (= v v'))
;;     (is (= {"name" "Gerard" "age" 31} (te/properties-of v)))
;;     (are [k val] (is (= val (te/property-of v k)))
;;          :name "Gerard" :age 31)))

;; (deftest test-merging-node-properties-with-multiple-maps
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tg/add-vertex g data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (te/merge! v {:age 31} {:position "Industrial Designer"})]
;;     (is (= v v'))
;;     (is (= {"name" "Gerard" "age" 31 "position" "Industrial Designer"} (te/properties-of v)))
;;     (are [k val] (is (= val (te/property-of v k)))
;;          :name "Gerard" :age 31)))

;; (deftest test-clearing-node-properties
;;   (let [g        (tg/open-in-memory-graph)
;;         data     {:name "Gerard" :age 30}
;;         v        (tg/add-vertex g data)
;;         ;; just like transients, this modifies in place, but we
;;         ;; also test the return value
;;         v'       (te/clear! v)]
;;     (is (= v v'))
;;     (is (= {} (te/properties-of v)))))
