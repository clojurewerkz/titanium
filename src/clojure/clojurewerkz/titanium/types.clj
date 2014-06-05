;; Copyright (c) 2013-2014 Michael S. Klishin, Alex Petrov, Zack Maril, and The ClojureWerkz
;; Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.titanium.types
  (:import (com.thinkaurelius.titan.core TypeGroup TitanType TypeMaker$UniquenessConsistency)
           (com.tinkerpop.blueprints Vertex Edge Direction Graph)))

(defn get-type
  [graph tname]
  (.getType graph (name tname)))

;; The default type group when no group is specified during type construction.
(def default-group (TypeGroup/DEFAULT_GROUP))

(defn defgroup
  "Define a TitanGroup."
  [group-id group-name]
  (TypeGroup/of group-id group-name))

(defn- convert-bool-to-lock
  [b]
  (if b
    TypeMaker$UniquenessConsistency/LOCK
    TypeMaker$UniquenessConsistency/NO_LOCK))


(defn unique-direction-converter
  [type-maker unique-direction unique-locked]
  (when unique-direction
    (when (#{:both :in} unique-direction)
      (.unique type-maker
               Direction/IN
               (convert-bool-to-lock unique-locked)))
    (when (#{:both :out} unique-direction)
      (.unique type-maker
               Direction/OUT
               (convert-bool-to-lock unique-locked)))))

(defn deflabel
  "Creates a edge label with the given properties."
  ([graph tname] (deflabel graph tname {}))
  ([graph tname {:keys [simple direction primary-key signature
                        unique-direction unique-locked group]
           :or {direction "directed"
                primary-key nil
                signature   nil
                unique-direction false
                unique-locked    true
                group       default-group}}]
     (let [type-maker (.. graph
                          makeType
                          (name (name tname))
                          (group group))]
       (unique-direction-converter type-maker unique-direction unique-locked)
       (case direction
         "directed"    (.directed type-maker)
         "unidirected" (.unidirected type-maker))
       (when signature (.signature type-maker signature))
       (when primary-key (.primaryKey type-maker (into-array TitanType primary-key)))
       (.makeEdgeLabel type-maker))))

(defn defkey
  "Creates a property key with the given properties."
  ([graph tname data-type] (defkey graph tname data-type {}))
  ([graph tname data-type {:keys [unique-direction
                            unique-locked
                            group
                            indexed-vertex?
                            indexed-edge?
                            searchable?]
                     :or   {unique-direction false
                            unique-locked    true
                            group       default-group}}]
     (let [type-maker   (.. graph
                            makeType
                            (name (name tname))
                            (group group)
                            (dataType data-type))]
       (when indexed-vertex?
         (if searchable?
           (.indexed type-maker "search" Vertex)
           (.indexed type-maker Vertex)))
       (when indexed-edge?
         (if searchable?
           (.indexed type-maker "search" Edge)
           (.indexed type-maker Edge)))
       (unique-direction-converter type-maker unique-direction unique-locked)
       (.makePropertyKey type-maker))))

(defn deflabel-once
  "Checks to see if a edge label with the given name exists already.
  If so, nothing happens, otherwise it is created."
  ([graph tname] (deflabel-once graph tname {}))
  ([graph tname m]
     (if-let [named-type (get-type graph tname)]
       named-type
       (deflabel graph tname m))))

(defn defkey-once
  "Checks to see if a property key with the given name exists already.
  If so, nothing happens, otherwise it is created."
  ([graph tname data-type] (defkey-once tname data-type {}))
  ([graph tname data-type m]
     (if-let [named-type (get-type graph tname)]
       named-type
       (defkey graph tname data-type m))))
