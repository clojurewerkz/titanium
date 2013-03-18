(ns clojurewerkz.titanium.graph
  (:require [mikera.cljutils.namespace :as n])
  (:import  [com.thinkaurelius.titan.core TitanFactory TitanGraph]
            [com.tinkerpop.blueprints Vertex Edge
             Graph KeyIndexableGraph
             TransactionalGraph TransactionalGraph$Conclusion]))

(ns/pull-all archimedes.core)

;;
;; API
;;

(defn open-in-memory-graph
  []
  (set-graph! (TitanFactory/openInMemoryGraph)))

(defprotocol TitaniumGraph
  (^KeyIndexableGraph open [input] "Opens a new graph"))

(extend-protocol TitaniumGraph
  String
  (open [^String path]
    (set-graph! (TitanFactory/open path)))

  java.io.File
  (open [^java.io.File f]
    (set-graph! (TitanFactory/open (.getPath f))))

  org.apache.commons.configuration.Configuration
  (open [^org.apache.commons.configuration.Configuration conf]
    (set-graph! (TitanFactory/open conf)))

  ;;TODO: Checkout out convert-config-map in Hermes. Let's you nest
  ;;things a bit deeper. 
  java.util.Map
  (open [^java.util.Map m]
    (let [bc (org.apache.commons.configuration.BaseConfiguration.)]
      (doseq [[k v] m]
        (.setProperty bc (name k) v))
      (set-graph! (TitanFactory/open bc)))))

(defn open? []
  (.isOpen archimedes.core/*graph*))


;;
;; Automatic Indexing
;;

(defn index-vertices-by-key!
  [^KeyIndexableGraph g ^String k]
  (.createKeyIndex g k com.tinkerpop.blueprints.Vertex))

(defn deindex-vertices-by-key!
  [^KeyIndexableGraph g ^String k]
  (.dropKeyIndex g k com.tinkerpop.blueprints.Vertex))

(defn index-edges-by-key!
  [^KeyIndexableGraph g ^String k]
  (.createKeyIndex g k com.tinkerpop.blueprints.Edge))

(defn deindex-edges-by-key!
  [^KeyIndexableGraph g ^String k]
  (.dropKeyIndex g k com.tinkerpop.blueprints.Edge))


;;
;; Features
;;

(defn get-features
  "Returns a map of features supported by provided graph"
  [^Graph g]
  (->> g .getFeatures .toMap (into {})))

(defn supports-feature?
  "Returns true if provided graph supports the given feature,
   false otherwise"
  [^Graph g ^String feature]
  (-> (get (get-features g) feature)
      not
      not))

(defn supports-transactions?
  "Returns true if provided graph supports transactions, false otherwise"
  [^Graph g]
  (supports-feature? g "supportsTransactions"))
