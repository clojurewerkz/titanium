(ns clojurewerkz.titanium.elements
  (:refer-clojure :exclude [assoc! dissoc!])
  (:require [clojure.walk :as w])
  (:import [com.tinkerpop.blueprints Element Vertex]))

;;
;; API
;;

(defn property-names
  "Returns a mutable set of property names for an element"
  [^Element e]
  (.getPropertyKeys e))

(defn property-of
  "Returns value of a single property"
  [^Element e property]
  (.getProperty e (name property)))

(defn ^clojure.lang.IPersistentMap properties-of
  "Returns all properties of an element as an immutable map"
  ([^Element e]
     (let [m (transient {})]
       (doseq [k (.getPropertyKeys e)]
         (clojure.core/assoc! m k (.getProperty e k)))
       (persistent! m)))
  ([^Element e keywordize-keys]
     (if keywordize-keys
       (w/keywordize-keys (properties-of e))
       (properties-of e))))

(defn assoc!
  "Sets multiple properties on an element, mutating it in place (like clojure.core/assoc!)"
  [^Element e & {:as properties}]
  (doseq [[k v] properties]
    (.setProperty e (name k) v))
  e)

(defn dissoc!
  "Unsets multiple properties on an element, mutating it in place (like clojure.core/dissoc!)"
  ([^Element e prop]
     (.removeProperty e (name prop))
     e)
  ([^Element e prop & props]
     (doseq [k (conj props prop)]
       (.removeProperty e (name k)))
     e))

(defn mutate-with!
  "Like assoc! but new value is calculated using a function of two arguments: old value and new value."
  [^Element e k f]
  (let [k' (name k)
        ov (.getProperty e k')]
    (.setProperty e k' (f ov))
    e))

(defn merge!
  "Like clojure.core/merge but first argument is a graph element that is mutated in place
   (like a transient map)"
  ([^Element e m]
     (doseq [[k v] m]
       (.setProperty e (name k) v))
     e)
  ([^Element e m & maps]
     (doseq [i (conj maps m)]
       (doseq [[k v] i]
         (.setProperty e (name k) v)))
     e))

(defn id-of
  "Returns id of an element. The id is guaranteed to be unique in the graph"
  [^Element e]
  (.getId e))

(defn clear!
  "Clears (removes) all properties from an element"
  [^Element e]
  (doseq [k (.getPropertyKeys e)]
    (.removeProperty e k))
  e)
