;; Copyright (c) 2013-2014 Michael S. Klishin, Alex Petrov, Zack Maril, and The ClojureWerkz
;; Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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
