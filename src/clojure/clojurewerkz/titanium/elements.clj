(ns clojurewerkz.titanium.elements
  (:refer-clojure :exclude [assoc! dissoc!])
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
  [^Element e ^String property]
  (.getProperty e property))

(defn ^clojure.lang.IPersistentMap properties-of
  "Returns all properties of an element as an immutable map"
  [^Element e]
  (let [m (transient {})]
    (doseq [k (.getPropertyKeys e)]
      (clojure.core/assoc! m k (.getProperty e k)))
    (persistent! m)))

(defn assoc!
  "Sets multiple properties on an element, mutating it in place (like clojure.core/assoc!)"
  [^Element e & {:as properties}]
  (doseq [[k v] properties]
    (.setProperty e k v))
  e)

(defn dissoc!
  "Unsets multiple properties on an element, mutating it in place (like clojure.core/dissoc!)"
  ([^Element e prop]
     (.removeProperty e prop)
     e)
  ([^Element e prop & props]
     (doseq [k (conj props prop)]
       (.removeProperty e k))
     e))

(defn id-of
  "Returns id of an element. The id is guaranteed to be unique in the graph"
  [^Element e]
  (.getId e))
