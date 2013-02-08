(ns clojurewerkz.titanium.edges
  (:refer-clojure :exclude [find])
  (:require [clojurewerkz.titanium.conversion :as cnv])
  (:import [com.tinkerpop.blueprints Vertex Edge Direction]))

;;
;; API
;;

(defn ^Vertex get-vertex
  [^Edge e direction]
  (.getVertex e (cnv/to-edge-direction direction)))

(defn ^Vertex head-vertex
  [^Edge e]
  (.getVertex e Direction/IN))

(defn ^Vertex tail-vertex
  [^Edge e]
  (.getVertex e Direction/OUT))

(defn ^String label-of
  [^Edge e]
  (.getLabel e))
