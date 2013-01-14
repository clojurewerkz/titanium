(ns clojurewerkz.titanium.graph
  (:import [com.thinkaurelius.titan.core TitanFactory TitanGraph]
           [com.tinkerpop.blueprints Graph Vertex Edge]))


;;
;; API
;;

(defn open-in-memory-graph
  []
  (TitanFactory/openInMemoryGraph))

(defprotocol TitaniumGraph
  (open [input] "Opens a new graph")
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




(defn ^com.tinkerpop.blueprints.Vertex
  add-vertex
  [^Graph g m]
  (let [vtx (.addVertex g nil)]
    (doseq [[k v] m]
      (.setProperty vtx (name k) v))
    vtx))

(defn ^com.tinkerpop.blueprints.Edge
  add-edge
  [^Graph g ^Edge edge-a ^Edge edge-b ^String label]
  (.addEdge g nil edge-a edge-b label))
