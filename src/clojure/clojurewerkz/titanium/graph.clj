(ns clojurewerkz.titanium.graph
  (:import [com.thinkaurelius.titan.core TitanFactory TitanGraph]
           [com.tinkerpop.blueprints Graph KeyIndexableGraph Vertex Edge]))


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
  [^Graph g ^Edge edge-a ^Edge edge-b ^String label]
  (.addEdge g nil edge-a edge-b label))


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
  ([^Graph g ^String k v]
     (.getVertices g k v)))

(defn ^Edge get-edge
  "Looks up an edge by id"
  [^Graph g id]
  (.getEdge g id))

(defn ^Iterable get-edges
  "Returns a sequence of edges where given key has the provided value"
  ([^Graph g]
     (.getEdges g))
  ([^Graph g ^String k v]
     (.getEdges g k v)))



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
