(ns clojurewerkz.titanium.edges-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.indexing :as ti]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted])
  (:use clojure.test))


(deftest test-creating-and-immediately-finding-a-relationship-without-properties
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (ted/connect! from-node :links to-node)
        fetched   (ted/find-by-id  (ted/id-of created))]
    (is (= (ted/id-of created) (ted/id-of fetched)))))

(deftest test-creating-and-immediately-finding-a-relationship-without-properties-twice
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (ted/connect! from-node :links to-node)
        fetched1  (ted/find-by-id  (ted/id-of created))
        fetched2  (ted/find-by-id  (ted/id-of created))]
    (is (= (ted/get created :url) (ted/get fetched1 :url) (ted/get fetched2 :url)))))

(deftest  test-creating-and-immediately-finding-a-relationship-with-properties
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
        fetched   (ted/find-by-id  (ted/id-of created))]
    (is (= (:since (ted/to-map fetched)) "08 Nov, 2011"))
    (is (= (ted/id-of created) (ted/id-of fetched)))))

(deftest  test-creating-and-immediately-deleting-a-relationship-with-properties
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
        fetched   (ted/find-by-id  (ted/id-of created))]
    (is (= created (ted/find-by-id (ted/id-of created))))
    (ted/delete! created)
    (is (nil? (ted/find-by-id (ted/id-of created))))))

(deftest test-listing-all-relationships-of-a-kind
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
        rs1       (tv/all-edges-of from-node :links)
        rs2       (tv/all-edges-of to-node   :links)
        rs3       (tv/all-edges-of to-node   :knows)]
    (is ((set rs1) created))
    (is ((set rs2) created))
    (is (empty? rs3))))

(deftest test-listing-incoming-relationships-of-a-kind
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
        rs1       (tv/incoming-edges-of to-node :links)
        rs2       (tv/incoming-edges-of to-node :linkes)
        rs3       (tv/all-edges-of to-node   :knows)]
    (is ((set rs1) created))
    (is (empty? rs2))))

(deftest test-listing-incoming-relationships-of-a-kind
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
        rs1       (tv/outgoing-edges-of from-node :links)
        rs2       (tv/outgoing-edges-of from-node :linkes)
        rs3       (tv/all-edges-of to-node   :knows)]
    (is ((set rs1) created))
    (is (empty? rs2))))

(deftest test-updating-relationship-properties
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        edge      (ted/connect! from-node :links to-node {:since "08 Nov, 2011"})
        edge'     (ted/assoc! edge :since "04 Nov, 2011")]
    (is (= edge edge'))
    (is (= (:since (ted/to-map edge)) "04 Nov, 2011"))))

;; ;; TODO: add multiple edges at once
;; ;; TODO maybe-add-edge
;; ;; TODO: maybe-delete-outgoing

;; ;;
;; ;; Edges
;; ;;

(deftest test-getting-edge-head-and-tail
  (tg/open-in-memory-graph)
  (let [m1 {"station" "Boston Manor" "lines" #{"Piccadilly"}}
        m2 {"station" "Northfields"  "lines" #{"Piccadilly"}}
        v1 (tv/create! m1)
        v2 (tv/create! m2)
        e  (ted/connect! v1 :links v2)]
    (is (= :links (ted/label-of e)))
    (is (= v2 (ted/head-vertex e)))
    (is (= v1 (ted/tail-vertex e)))))

;; #_ (deftest test-getting-edge-head-and-tail-via-fancy-macro
;;      (tg/open-in-memory-graph)
;;      (let [g
;;            m1 {"station" "Boston Manor" "lines" #{"Piccadilly"}}
;;            m2 {"station" "Northfields"  "lines" #{"Piccadilly"}}]
;;        (tg/populate g
;;                     (m1 -links-> m2))))
