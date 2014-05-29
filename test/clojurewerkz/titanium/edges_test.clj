(ns clojurewerkz.titanium.edges-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.indexing :as ti]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted])
  (:use clojure.test
        [clojurewerkz.titanium.test.conf :only (clear-db conf)]))


(deftest test-edges
  (clear-db)
  (let [graph (tg/open conf)]

    (testing "creating and immediately finding a relationship without properties"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             created   (ted/connect! tx from-node :links to-node)
             fetched   (ted/find-by-id tx (ted/id-of created))]
         (is (= (ted/id-of created) (ted/id-of fetched))))))

    (testing "creating and immediately finding a relationship without properties twice"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             created   (ted/connect! tx from-node :links to-node)
             fetched1  (ted/find-by-id tx (ted/id-of created))
             fetched2  (ted/find-by-id tx (ted/id-of created))]
         (is (= (ted/get created :url) (ted/get fetched1 :url) (ted/get fetched2 :url))))))

    (testing "creating and immediately finding a relationship with properties"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             created   (ted/connect! tx from-node :links to-node  {:since "08 Nov, 2011"})
             fetched   (ted/find-by-id tx (ted/id-of created))]
         (is (= (:since (ted/to-map fetched)) "08 Nov, 2011"))
         (is (= (ted/id-of created) (ted/id-of fetched))))))

    (testing "creating and immediately deleting a relationship with properties"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             created   (ted/connect! tx from-node :links to-node  {:since "08 Nov, 2011"})
             fetched   (ted/find-by-id tx (ted/id-of created))]
         (is (= created (ted/find-by-id tx (ted/id-of created))))
         (ted/remove! tx created)
         (is (nil? (ted/find-by-id tx (ted/id-of created)))))))

    (testing "listing all relationships of a kind"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             created   (ted/connect! tx from-node :links to-node  {:since "08 Nov, 2011"})
             rs1       (tv/all-edges-of from-node :links)
             rs2       (tv/all-edges-of to-node   :links)
             rs3       (tv/all-edges-of to-node   :knows)]
         (is ((set rs1) created))
         (is ((set rs2) created))
         (is (empty? rs3)))))

    (testing "listing incoming relationships of a kind"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             created   (ted/connect! tx from-node :links to-node  {:since "08 Nov, 2011"})
             rs1       (tv/incoming-edges-of to-node :links)
             rs2       (tv/incoming-edges-of to-node :linkes)
             rs3       (tv/all-edges-of to-node   :knows)]
         (is ((set rs1) created))
         (is (empty? rs2)))))

    (testing "listing incoming relationships of a kind"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             created   (ted/connect! tx from-node :links to-node  {:since "08 Nov, 2011"})
             rs1       (tv/outgoing-edges-of from-node :links)
             rs2       (tv/outgoing-edges-of from-node :linkes)
             rs3       (tv/all-edges-of to-node   :knows)]
         (is ((set rs1) created))
         (is (empty? rs2)))))

    (testing "updating relationship properties"
      (tg/with-transaction [tx graph]
       (let [from-node (tv/create! tx {:url "http://clojurewerkz.org/"})
             to-node   (tv/create! tx {:url "http://clojurewerkz.org/about.html"})
             edge      (ted/connect! tx from-node :links to-node {:since "08 Nov, 2011"})
             edge'     (ted/assoc! edge :since "04 Nov, 2011")]
         (is (= edge edge'))
         (is (= (:since (ted/to-map edge)) "04 Nov, 2011")))))

    ;; ;; TODO: add multiple edges at once
    ;; ;; TODO maybe-add-edge
    ;; ;; TODO: maybe-delete-outgoing

    ;; ;;
    ;; ;; Edges
    ;; ;;

    (testing "getting edge head and tail"
      (tg/with-transaction [tx graph]
       (let [m1 {"station" "Boston Manor" "lines" #{"Piccadilly"}}
             m2 {"station" "Northfields"  "lines" #{"Piccadilly"}}
             v1 (tv/create! tx m1)
             v2 (tv/create! tx m2)
             e  (ted/connect! tx v1 :links v2)]
         (is (= :links (ted/label-of e)))
         (is (= v2 (ted/head-vertex e)))
         (is (= v1 (ted/tail-vertex e))))))

    ;; #_ (testing getting-edge-head-and-tail-via-fancy-macro
    ;;      (let [g
    ;;            m1 {"station" "Boston Manor" "lines" #{"Piccadilly"}}
    ;;            m2 {"station" "Northfields"  "lines" #{"Piccadilly"}}]
    ;;        (tg/populate g
    ;;                     (m1 -links-> m2))))

    (testing "Edge deletion"
      (tg/with-transaction [tx graph]
       (let [u (tv/create! tx)
             w (tv/create! tx)
             a (ted/connect! tx u :test w)
             a-id (ted/id-of a)]
         (ted/remove! tx a)
         (is (nil? (ted/find-by-id tx a-id))))))

    (testing "Single property mutation"
      (tg/with-transaction [tx graph]
       (let [v1 (tv/create! tx {:name "v1"})
             v2 (tv/create! tx {:name "v2"})
             edge (ted/connect! tx v1 :test v2 {:a 1})]
         (ted/assoc! edge :b 2)
         (ted/dissoc! edge :a)
         (is (= 2   (ted/get edge :b)))
         (is (nil? (ted/get edge :a))))))

    (testing "Multiple property mutation"
      (tg/with-transaction [tx graph]
       (let [v1 (tv/create! tx {:name "v1"})
             v2 (tv/create! tx {:name "v2"})
             edge (ted/connect! tx v1 :test v2 {:a 0})]
         (ted/merge! edge {:a 1 :b 2 :c 3})
         (is (= 1 (ted/get edge :a)))
         (is (= 2 (ted/get edge :b)))
         (is (= 3 (ted/get edge :c))))))

    (testing "Property map"
      (tg/with-transaction [tx graph]
       (let [v1 (tv/create! tx {:name "v1"})
             v2 (tv/create! tx {:name "v2"})
             edge (ted/connect! tx v1 :test v2 {:a 1 :b 2 :c 3})
             prop-map (ted/to-map edge)]
         (is (= {:a 1 :b 2 :c 3} (dissoc prop-map :__id__ :__label__))))))

    (testing "Endpoints"
      (tg/with-transaction [tx graph]
       (let [v1 (tv/create! tx {:name "v1"})
             v2 (tv/create! tx {:name "v2"})
             edge (ted/connect! tx v1 :connexion v2)]
         (is (= ["v1" "v2"] (map #(ted/get % :name) (ted/endpoints edge)))))))

    (testing "Refresh"
      (let [v1 (tg/with-transaction [tx graph] (tv/create! tx {:name "v1"}))
            v2 (tg/with-transaction [tx graph] (tv/create! tx {:name "v2"}))
            edge (tg/with-transaction [tx graph]
                   (ted/connect! tx (tv/refresh tx v1) :connexion (tv/refresh tx v2) {:name "bob"}))]
        (is (tg/with-transaction [tx graph]
              (= (.getId edge) (.getId (ted/refresh tx edge)))))
        (is (tg/with-transaction [tx graph]
             (is (= "bob" (:name (ted/to-map (ted/refresh tx edge)))))))))

    (testing "Edges between"
      (let [v1 (tg/with-transaction [tx graph] (tv/create! tx {:name "v1"}))
            v2 (tg/with-transaction [tx graph] (tv/create! tx {:name "v2"}))
            edge (tg/with-transaction [tx graph] (ted/connect! tx (tv/refresh tx v1) :connexion (tv/refresh tx v2)))]
        (is edge)
        (is (tg/with-transaction [tx graph] (= (ted/to-map (ted/refresh tx edge))
                                               (ted/to-map (first
                                                            (ted/edges-between (tv/refresh tx v1) (tv/refresh tx v2)))))))))

    (testing "Upconnect!"
      (testing "Upconnecting once without data"
        (tg/with-transaction [tx graph]
         (let [v1 (tv/create! tx {:name "v1"})
               v2 (tv/create! tx {:name "v2"})
               edge (first (ted/upconnect! tx v1 :connexion v2))]
           (is (ted/connected? v1 v2))
           (is (ted/connected? v1 :connexion v2))
           (is (not (ted/connected? v2 v1))))))

      (testing "Upconnecting once"
        (tg/with-transaction [tx graph]
         (let [v1 (tv/create! tx {:name "v1"})
               v2 (tv/create! tx {:name "v2"})
               edge (first (ted/upconnect! tx v1 :connexion v2 {:prop "the edge"}))]
           (is (ted/connected? v1 v2))
           (is (ted/connected? v1 :connexion v2))
           (is (not (ted/connected? v2 v1)))
           (is (= "the edge" (ted/get edge :prop)))))))

    (testing "Upconnecting multiple times"
      (tg/with-transaction [tx graph]
       (let [v1 (tv/create! tx {:name "v1"})
             v2 (tv/create! tx {:name "v2"})
             edge (first (ted/upconnect! tx v1 :connexion v2 {:prop "the edge"}))
             edge (first (ted/upconnect! tx v1 :connexion v2 {:a 1 :b 2}))
             edge (first (ted/upconnect! tx v1 :connexion v2 {:b 0}))]
         (is (ted/connected? v1 v2))
         (is (ted/connected? v1 :connexion v2))
         (is (not (ted/connected? v2 v1)))
         (is (= "the edge" (ted/get edge :prop)))
         (is (= 1 (ted/get edge :a)))
         (is (= 0 (ted/get edge :b))))))
    (testing "unique-upconnect!"
      (testing "Once"
        (tg/with-transaction [tx graph]
         (let [v1 (tv/create! tx {:name "v1"})
               v2 (tv/create! tx {:name "v2"})
               edge (ted/unique-upconnect! tx v1 :connexion v2 {:prop "the edge"})]
           (is (ted/connected? v1 v2))
           (is (ted/connected? v1 :connexion v2))
           (is (not (ted/connected? v2 v1)))
           (is (= "the edge" (ted/get edge :prop))))))

      (testing "Multiple times"
        (tg/with-transaction [tx graph]
         (let [v1 (tv/create! tx {:name "v1"})
               v2 (tv/create! tx {:name "v2"})
               edge (ted/unique-upconnect! tx v1 :connexion v2 {:prop "the edge"})
               edge (ted/unique-upconnect! tx v1 :connexion v2 {:a 1 :b 2})
               edge (ted/unique-upconnect! tx v1 :connexion v2 {:b 0})]
           (is (ted/connected? v1 v2))
           (is (ted/connected? v1 :connexion v2))
           (is (not (ted/connected? v2 v1)))
           (is (= "the edge" (ted/get edge :prop)))
           (is (= 1 (ted/get edge :a)))
           (is (= 0 (ted/get edge :b)))
           (ted/connect! tx v1 :connexion v2)
           (is (thrown-with-msg? Throwable #"There were 2 edges returned"
                                 (ted/unique-upconnect! tx v1 :connexion v2))))))
      (tg/shutdown graph)
      (clear-db))))
