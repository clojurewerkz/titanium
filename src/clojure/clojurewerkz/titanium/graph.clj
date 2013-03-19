(ns clojurewerkz.titanium.graph
  (:require [mikera.cljutils.namespace :as n]
            [archimedes.core :as g])
  (:import  [com.thinkaurelius.titan.core TitanFactory TitanGraph]
            [com.tinkerpop.blueprints Vertex Edge
             Graph KeyIndexableGraph
             TransactionalGraph TransactionalGraph$Conclusion]
            [com.thinkaurelius.titan.graphdb.blueprints TitanInMemoryBlueprintsGraph]
            [com.thinkaurelius.titan.graphdb.transaction StandardPersistTitanTx]))

(n/pull-all archimedes.core)

;;
;; API
;;
(defn ensure-graph-is-transaction-safe
  "Ensure that we are either in a transaction or using an in-memory graph."
  []
  (when-not (#{TitanInMemoryBlueprintsGraph StandardPersistTitanTx}
              (type g/*graph*))
    (throw
      (Throwable.
       "All actions on a persistent graph must be wrapped in transact! "))))

(set-pre-fn! ensure-graph-is-transaction-safe)

(defn open-in-memory-graph
  []
  (set-graph! (TitanFactory/openInMemoryGraph)))

(defn convert-config-map [m]
  (let [conf (org.apache.commons.configuration.BaseConfiguration.)]
    (doseq [[k1 v1] m]
            (if (string? v1)
              (.setProperty conf (name k1) v1)
              (doseq [[k2 v2] v1]
                (.setProperty conf (str (name k1) "." (name k2)) v2))))
    conf))

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
    (set-graph! (TitanFactory/open (convert-config-map m)))))

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
