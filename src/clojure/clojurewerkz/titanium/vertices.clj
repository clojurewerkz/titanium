(ns clojurewerkz.titanium.vertices
  (:require [clojurewerkz.titanium.conversion :as cnv])
  (:import [com.tinkerpop.blueprints Vertex Direction]))

;;
;; API
;;

(defn edges-of
  "Returns edges that this vertex is part of with direction and with given labels"
  [^Vertex v direction labels]
  (.getEdges v (cnv/to-edge-direction direction) (into-array String labels)))

(defn outgoing-edges-of
  [^Vertex v labels]
  (.getEdges v Direction/OUT (into-array String labels)))

(defn incoming-edges-of
  [^Vertex v labels]
  (.getEdges v Direction/IN (into-array String labels)))

(defn connected-vertices-of
  [^Vertex v direction labels]
  (.getVertices v (cnv/to-edge-direction direction) (into-array String labels)))

(defn connected-out-vertices
  [^Vertex v labels]
  (.getVertices v Direction/OUT (into-array String labels)))

(defn connected-in-vertices
  [^Vertex v labels]
  (.getVertices v Direction/IN (into-array String labels)))
