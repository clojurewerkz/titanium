(ns clojurewerkz.titanium.edges-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.indexing :as ti]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted])
  (:use clojure.test
        [clojurewerkz.titanium.conf :only (clear-db conf)]))


(deftest test-edges
  (clear-db)
  (tg/open conf)

  (testing "creating and immediately finding a relationship without properties"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           created   (ted/connect! from-node :links to-node)
           fetched   (ted/find-by-id  (ted/id-of created))]
       (is (= (ted/id-of created) (ted/id-of fetched))))))

  (testing "creating and immediately finding a relationship without properties twice"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           created   (ted/connect! from-node :links to-node)
           fetched1  (ted/find-by-id  (ted/id-of created))
           fetched2  (ted/find-by-id  (ted/id-of created))]
       (is (= (ted/get created :url) (ted/get fetched1 :url) (ted/get fetched2 :url))))))

  (testing "creating and immediately finding a relationship with properties"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
           fetched   (ted/find-by-id  (ted/id-of created))]
       (is (= (:since (ted/to-map fetched)) "08 Nov, 2011"))
       (is (= (ted/id-of created) (ted/id-of fetched))))))

  (testing "creating and immediately deleting a relationship with properties"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
           fetched   (ted/find-by-id  (ted/id-of created))]
       (is (= created (ted/find-by-id (ted/id-of created))))
       (ted/delete! created)
       (is (nil? (ted/find-by-id (ted/id-of created)))))))

  (testing "listing all relationships of a kind"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
           rs1       (tv/all-edges-of from-node :links)
           rs2       (tv/all-edges-of to-node   :links)
           rs3       (tv/all-edges-of to-node   :knows)]
       (is ((set rs1) created))
       (is ((set rs2) created))
       (is (empty? rs3)))))

  (testing "listing incoming relationships of a kind"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
           rs1       (tv/incoming-edges-of to-node :links)
           rs2       (tv/incoming-edges-of to-node :linkes)
           rs3       (tv/all-edges-of to-node   :knows)]
       (is ((set rs1) created))
       (is (empty? rs2)))))

  (testing "listing incoming relationships of a kind"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           created   (ted/connect! from-node :links to-node  {:since "08 Nov, 2011"})
           rs1       (tv/outgoing-edges-of from-node :links)
           rs2       (tv/outgoing-edges-of from-node :linkes)
           rs3       (tv/all-edges-of to-node   :knows)]
       (is ((set rs1) created))
       (is (empty? rs2)))))

  (testing "updating relationship properties"
    (tg/transact!
     (let [from-node (tv/create! {:url "http://clojurewerkz.org/"})
           to-node   (tv/create! {:url "http://clojurewerkz.org/about.html"})
           edge      (ted/connect! from-node :links to-node {:since "08 Nov, 2011"})
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
    (tg/transact!
     (let [m1 {"station" "Boston Manor" "lines" #{"Piccadilly"}}
           m2 {"station" "Northfields"  "lines" #{"Piccadilly"}}
           v1 (tv/create! m1)
           v2 (tv/create! m2)
           e  (ted/connect! v1 :links v2)]
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
    (tg/transact!
     (let [u (tv/create!)
           w (tv/create!)
           a (ted/connect! u :test w)
           a-id (ted/id-of a)]
       (ted/delete! a)
       (is (=  nil (ted/find-by-id a-id))))))

  (testing "Single property mutation"
    (tg/transact!
     (let [v1 (tv/create! {:name "v1"})
           v2 (tv/create! {:name "v2"})
           edge (ted/connect! v1 :test v2 {:a 1})]
       (ted/assoc! edge :b 2)
       (ted/dissoc! edge :a)
       (is (= 2   (ted/get edge :b)))
       (is (= nil (ted/get edge :a))))))

  (testing "Multiple property mutation"
    (tg/transact!
     (let [v1 (tv/create! {:name "v1"})
           v2 (tv/create! {:name "v2"})
           edge (ted/connect! v1 :test v2 {:a 0})]
       (ted/merge! edge {:a 1 :b 2 :c 3})
       (is (= 1 (ted/get edge :a)))
       (is (= 2 (ted/get edge :b)))
       (is (= 3 (ted/get edge :c))))))

  (testing "Property map"
    (tg/transact!
     (let [v1 (tv/create! {:name "v1"})
           v2 (tv/create! {:name "v2"})
           edge (ted/connect! v1 :test v2 {:a 1 :b 2 :c 3})
           prop-map (ted/to-map edge)]
       (is (= {:a 1 :b 2 :c 3} (dissoc prop-map :__id__ :__label__))))))

  (testing "Endpoints"
    (tg/transact!
     (let [v1 (tv/create! {:name "v1"})
           v2 (tv/create! {:name "v2"})
           edge (ted/connect! v1 :connexion v2)]
       (is (= ["v1" "v2"] (map #(ted/get % :name) (ted/endpoints edge)))))))

  (testing "Refresh"
    (let [v1 (tg/transact! (tv/create! {:name "v1"}))
          v2 (tg/transact! (tv/create! {:name "v2"}))
          edge (tg/transact! 
                (ted/connect! (tv/refresh v1) :connexion (tv/refresh v2) {:name "bob"}))]
      (is (tg/transact! 
           (= (.getId edge) (.getId (ted/refresh edge)))))
      (is (tg/transact! 
           (is (= "bob" (:name (ted/to-map (ted/refresh edge)))))))))

  (testing "Edges between"
    (let [v1 (tg/transact! (tv/create! {:name "v1"}))
          v2 (tg/transact! (tv/create! {:name "v2"}))
          edge (tg/transact! (ted/connect! (tv/refresh v1) :connexion (tv/refresh v2)))]
      (is edge)
      (is (tg/transact! (= (ted/to-map (ted/refresh edge))
                           (ted/to-map (first  
                                        (ted/edges-between (tv/refresh v1) (tv/refresh v2)))))))))
  
  (testing "Upconnect!"
    (testing "Upconnecting once"
      (tg/transact!
       (let [v1 (tv/create! {:name "v1"})
             v2 (tv/create! {:name "v2"})
             edge (first (ted/upconnect! v1 :connexion v2 {:prop "the edge"}))]
         (is (ted/connected? v1 v2))
         (is (ted/connected? v1 :connexion v2))
         (is (not (ted/connected? v2 v1)))
         (is (= "the edge" (ted/get edge :prop)))))))

  (testing "Upconnecting multiple times"
    (tg/transact!
     (let [v1 (tv/create! {:name "v1"})
           v2 (tv/create! {:name "v2"})
           edge (first (ted/upconnect! v1 :connexion v2 {:prop "the edge"}))
           edge (first (ted/upconnect! v1 :connexion v2 {:a 1 :b 2}))
           edge (first (ted/upconnect! v1 :connexion v2 {:b 0}))]
       (is (ted/connected? v1 v2))
       (is (ted/connected? v1 :connexion v2))
       (is (not (ted/connected? v2 v1)))
       (is (= "the edge" (ted/get edge :prop)))
       (is (= 1 (ted/get edge :a)))
       (is (= 0 (ted/get edge :b))))))
  (testing "unique-upconnect!"
    (testing "Once"
      (tg/transact!
       (let [v1 (tv/create! {:name "v1"})
             v2 (tv/create! {:name "v2"})
             edge (ted/unique-upconnect! v1 :connexion v2 {:prop "the edge"})]
         (is (ted/connected? v1 v2))
         (is (ted/connected? v1 :connexion v2))
         (is (not (ted/connected? v2 v1)))
         (is (= "the edge" (ted/get edge :prop))))))

    (testing "Multiple times"
      (tg/transact!
       (let [v1 (tv/create! {:name "v1"})
             v2 (tv/create! {:name "v2"})
             edge (ted/unique-upconnect! v1 :connexion v2 {:prop "the edge"})
             edge (ted/unique-upconnect! v1 :connexion v2 {:a 1 :b 2})
             edge (ted/unique-upconnect! v1 :connexion v2 {:b 0})]
         (is (ted/connected? v1 v2))
         (is (ted/connected? v1 :connexion v2))
         (is (not (ted/connected? v2 v1)))
         (is (= "the edge" (ted/get edge :prop)))
         (is (= 1 (ted/get edge :a)))
         (is (= 0 (ted/get edge :b)))
         (ted/connect! v1 :connexion v2)
         (is (thrown? Throwable #"There were 2 vertices returned."
                      (ted/unique-upconnect! v1 :connexion v2))))))
    (tg/shutdown)
    (clear-db)))




