(ns clojurewerkz.titanium.query
  (:require [clojurewerkz.titanium.conversion :as cnv])
  (:import [com.tinkerpop.blueprints Vertex Edge Direction Query]))

;;
;; Implementation
;;

(defn query-on
  [^Vertex starting-point]
  (.query starting-point))


;;
;; API
;;

(defn has
  ([^Query q key val]
     (.has q (name key) val))
  ([^Query q key operator val]
     (.has q (name key) val (cnv/to-query-compare operator))))

(defn interval
  [^Query q key start-val end-val]
  (.interval q (name key) start-val end-val))

(defn direction
  [^Query q dir]
  (.direction q (cnv/to-edge-direction dir)))

(defn labels
  [^Query q coll]
  (.labels q (into-array String coll)))

(defn limit
  [^Query q ^long max]
  (.limit q max))

(defmacro find-vertices
  [^Vertex starting-point & body]
  `(let [^com.tinkerpop.blueprints.Query query# (-> (query-on ~starting-point) ~@body)]
     (into [] (.vertices query#))))

(defmacro find-edges
  [^Vertex starting-point & body]
  `(let [^com.tinkerpop.blueprints.Query query# (-> (query-on ~starting-point) ~@body)]
     (into [] (.edges query#))))

(defmacro ^long count-edges
  [^Vertex starting-point & body]
  `(let [^com.tinkerpop.blueprints.Query query# (-> (query-on ~starting-point) ~@body)]
     (.count query#)))
