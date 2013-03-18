(ns clojurewerkz.titanium.edges-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.indexing :as ti]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as te])
  (:use clojure.test))


(deftest test-creating-and-immediately-finding-a-relationship-without-properties
  (tg/open-in-memory-graph)
  (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
        to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
        created   (te/connect! from-node :links to-node)
        fetched   (te/find-by-id  (te/id-of created))]
    (is (= (te/id-of created) (te/id-of fetched)))))

;; (deftest test-creating-and-immediately-finding-a-relationship-without-properties-twice
;;   (let [g         (tg/open-in-memory-graph)
;;         from-node (tg/add-vertex g {:url "http://clojurewerkz.org/"})
;;         to-node   (tg/add-vertex g {:url "http://clojurewerkz.org/about.html"})
;;         created   (tg/add-edge   g from-node to-node "links")
;;         fetched1  (tg/get-edge   g (te/id-of created))
;;         fetched2  (tg/get-edge   g (te/id-of created))]
;;     (is (= (te/property-of created :url) (te/property-of fetched1 :url) (te/property-of fetched2 :url)))))

;; (deftest test-creating-and-immediately-finding-a-relationship-with-properties
;;   (let [g         (tg/open-in-memory-graph)
;;         from-node (tg/add-vertex g {:url "http://clojurewerkz.org/"})
;;         to-node   (tg/add-vertex g {:url "http://clojurewerkz.org/about.html"})
;;         created   (tg/add-edge   g from-node to-node "links" {:since "08 Nov, 2011"})
;;         fetched   (tg/get-edge   g (te/id-of created))]
;;     (is (= (te/properties-of fetched) {"since" "08 Nov, 2011"}))
;;     (is (= (te/id-of created) (te/id-of fetched)))))

;; (deftest test-creating-and-immediately-deleting-a-relationship-with-properties
;;   (let [g         (tg/open-in-memory-graph)
;;         from-node (tg/add-vertex g {:url "http://clojurewerkz.org/"})
;;         to-node   (tg/add-vertex g {:url "http://clojurewerkz.org/about.html"})
;;         created   (tg/add-edge   g from-node to-node "links" {:since "08 Nov, 2011"})]
;;     (is (= created (tg/get-edge g (te/id-of created))))
;;     (tg/remove-edge g created)
;;     (is (nil? (tg/get-edge g (te/id-of created))))))

;; (deftest test-listing-all-relationships-of-a-kind
;;   (let [g         (tg/open-in-memory-graph)
;;         from-node (tg/add-vertex g {:url "http://clojurewerkz.org/"})
;;         to-node   (tg/add-vertex g {:url "http://clojurewerkz.org/about.html"})
;;         created   (tg/add-edge   g from-node to-node "links" {:since "08 Nov, 2011"})
;;         rs1       (tv/all-edges-of from-node ["links"])
;;         rs2       (tv/all-edges-of to-node   ["links"])
;;         rs3       (tv/all-edges-of to-node   ["knows"])]
;;     (is ((set rs1) created))
;;     (is ((set rs2) created))
;;     (is (empty? rs3))))

;; (deftest test-listing-incoming-relationships-of-a-kind
;;   (let [g         (tg/open-in-memory-graph)
;;         from-node (tg/add-vertex g {:url "http://clojurewerkz.org/"})
;;         to-node   (tg/add-vertex g {:url "http://clojurewerkz.org/about.html"})
;;         created   (tg/add-edge   g from-node to-node "links" {:since "08 Nov, 2011"})
;;         rs1       (tv/incoming-edges-of to-node ["links"])
;;         rs2       (tv/incoming-edges-of to-node ["likes"])]
;;     (is ((set rs1) created))
;;     (is (empty? rs2))))

;; (deftest test-listing-outgoing-relationships-of-a-kind
;;   (let [g         (tg/open-in-memory-graph)
;;         from-node (tg/add-vertex g {:url "http://clojurewerkz.org/"})
;;         to-node   (tg/add-vertex g {:url "http://clojurewerkz.org/about.html"})
;;         created   (tg/add-edge   g from-node to-node "links" {:since "08 Nov, 2011"})
;;         rs1       (tv/outgoing-edges-of from-node ["links"])
;;         rs2       (tv/outgoing-edges-of from-node ["likes"])]
;;     (is ((set rs1) created))
;;     (is (empty? rs2))))

;; (deftest test-updating-relationship-properties
;;   (let [g         (tg/open-in-memory-graph)
;;         from-node (tg/add-vertex g {:url "http://clojurewerkz.org/"})
;;         to-node   (tg/add-vertex g {:url "http://clojurewerkz.org/about.html"})
;;         edge      (tg/add-edge   g from-node to-node "links" {:since "08 Nov, 2011"})
;;         edge'     (te/assoc! edge :since "04 Nov, 2011")]
;;     (is (= edge edge'))
;;     (is (= (te/properties-of edge) {"since" "04 Nov, 2011"}))))

;; ;; TODO: add multiple edges at once
;; ;; TODO maybe-add-edge
;; ;; TODO: maybe-delete-outgoing
