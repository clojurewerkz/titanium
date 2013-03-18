(ns clojurewerkz.titanium.elements
  (:refer-clojure :exclude [assoc! dissoc!])
  (:require [clojure.walk :as w])
  (:import [com.tinkerpop.blueprints Element Vertex]
           [com.thinkaurelius.titan.core TitanElement]))


;;
;; API
;;

(defn new?
  "Returns true if entity has been newly created, false otherwise"
  [^TitanElement e]
  (.isNew e))

(defn loaded?
  "Returns true if entity has been loaded and not yet modified in the current transaction,
   false otherwise"
  [^TitanElement e]
  (.isLoaded e))

(defn modified?
  "Returns true if entity has been loaded and modified in the current transaction,
   false otherwise"
  [^TitanElement e]
  (.isModified e))

(defn removed?
  "Returns true if entity has been deleted in the current transaction,
   false otherwise"
  [^TitanElement e]
  (.isRemoved e))
