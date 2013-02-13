(ns clojurewerkz.titanium.graph
  (:require [clojurewerkz.titanium.elements :as te])
  (:import [com.thinkaurelius.titan.core TitanFactory TitanGraph]
           [com.tinkerpop.blueprints Vertex Edge
            Graph KeyIndexableGraph
            TransactionalGraph TransactionalGraph$Conclusion]))


;;
;; API
;;

(defn open-in-memory-graph
  []
  (TitanFactory/openInMemoryGraph))

(defprotocol TitaniumGraph
  (^KeyIndexableGraph open [input] "Opens a new graph")
  (open? [input] "Returns true if the graph is open (ready to be used)")
  (close [graph] "Shuts down the given graph"))

(extend-protocol TitaniumGraph
  String
  (open [^String path]
    (TitanFactory/open path))

  java.io.File
  (open [^java.io.File f]
    (TitanFactory/open (.getPath f)))

  org.apache.commons.configuration.Configuration
  (open [^org.apache.commons.configuration.Configuration conf]
    (TitanFactory/open conf))

  java.util.Map
  (open [^java.util.Map m]
    (let [bc (org.apache.commons.configuration.BaseConfiguration.)]
      (doseq [[k v] m]
        (.setProperty bc (name k) v))
      (TitanFactory/open bc)))

  com.tinkerpop.blueprints.Graph
  (close [^Graph g]
    (.shutdown g))

  TitanGraph
  (open? [^TitanGraph g]
    (.isOpen g))
  (close [^TitanGraph g]
    (.shutdown g)))


;;
;; Populating
;;

(defn ^Vertex add-vertex
  "Adds a vertex to graph"
  ([^Graph g m]
     (let [vtx (.addVertex g nil)]
       (doseq [[k v] m]
         (.setProperty vtx (name k) v))
       vtx))
  ([^Graph g id m]
     (let [vtx (.addVertex g id)]
       (doseq [[k v] m]
         (.setProperty vtx (name k) v))
       vtx)))

(defn ^Edge add-edge
  "Adds an edge to graph"
  ([^Graph g ^Vertex head ^Vertex tail ^String label]
     (.addEdge g nil head tail label))
  ([^Graph g ^Vertex head ^Vertex tail ^String label properties]
     (let [e (.addEdge g nil head tail label)]
       (te/merge! e properties)
       e)))


;;
;; Deleting
;;

(defn remove-vertex
  "Removes a vertex from graph"
  [^Graph g ^Vertex el]
  (.removeVertex g el))

(defn remove-edge
  "Removes an edge from graph"
  [^Graph g ^Edge el]
  (.removeEdge g el))


;;
;; Querying
;;

(defn ^Vertex get-vertex
  "Looks up a vertex by id"
  [^Graph g id]
  (.getVertex g id))

(defn ^Iterable get-vertices
  "Returns a sequence of vertices where given key has the provided value"
  ([^Graph g]
     (.getVertices g))
  ([^Graph g k v]
     (.getVertices g (name k) v)))

(defn ^Edge get-edge
  "Looks up an edge by id"
  [^Graph g id]
  (.getEdge g id))

(defn ^Iterable get-edges
  "Returns a sequence of edges where given key has the provided value"
  ([^Graph g]
     (.getEdges g))
  ([^Graph g k v]
     (.getEdges g (name k) v)))



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


;;
;; Transactions
;;

(defn commit-tx!
  "Commits current transaction"
  [^TransactionalGraph g]
  (.stopTransaction g TransactionalGraph$Conclusion/SUCCESS))

(defn rollback-tx!
  "Rolls back current transaction"
  [^TransactionalGraph g]
  (.stopTransaction g TransactionalGraph$Conclusion/FAILURE))

(defn start-tx
  "Starts a transaction. Only necessary if operations in a transaction
   will be used across threads."
  [^TransactionalGraph g]
  (.startTransaction g))

(defn- perform-transaction
  [^TransactionalGraph g f]
  (let [tx (start-tx g)]
    (try
      (f tx)
      (commit-tx! tx)
      (catch Exception e
        (rollback-tx! tx)
        (throw e)))))

(defn run-transactionally
  "Evaluates provided function in a transaction"
  [^Graph g f]
  (if (supports-transactions? g)
    (perform-transaction g f)
    (f g)))
