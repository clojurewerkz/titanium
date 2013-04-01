(ns clojurewerkz.titanium.types
  (:import (com.thinkaurelius.titan.core TypeGroup TitanType TypeMaker$UniquenessConsistency)
           (com.tinkerpop.blueprints Vertex Edge Direction Graph))
  (:use [clojurewerkz.titanium.graph :only (get-graph ensure-graph-is-transaction-safe)]))

(defn get-type [tname]
  (ensure-graph-is-transaction-safe)
  (.getType (get-graph) (name tname)))

;; The default type group when no group is specified during type construction.
(def default-group (TypeGroup/DEFAULT_GROUP))

(defn create-group
  "Create a TitanGroup."
  [group-id group-name]
  (ensure-graph-is-transaction-safe)
  (TypeGroup/of group-id group-name))

(defn- keyword-to-direction [k]
  (case k
    :in  Direction/IN
    :out Direction/IN
    ;;TODO throw an error here
    ))

(defn- convert-bool-to-lock [b]
  (if b
    TypeMaker$UniquenessConsistency/LOCK
    TypeMaker$UniquenessConsistency/NO_LOCK))


(defn create-edge-label
  "Creates a edge label with the given properties."
  ([tname] (create-edge-label name {}))
  ([tname {:keys [simple direction primary-key signature
                 unique-direction unique-locked group]
          :as m
          :or {simple false
               direction "directed"
               primary-key nil
               signature   nil
               unique-direction false
               unique-locked    true
               group       default-group}}]
     (ensure-graph-is-transaction-safe)
     (let [type-maker (.. (get-graph)
                          makeType
                          (name (name tname))
                          (group group))]

       (when simple (.simple type-maker))

       (when unique-direction                           
         (.unique type-maker
                  (keyword-to-direction unique-direction)
                  (convert-bool-to-lock unique-locked)))
       (case direction
         "directed"    (.directed type-maker)
         "unidirected" (.unidirected type-maker)
         "undirected"  (.undirected type-maker))
       (when signature (.signature type-maker signature))
       (when primary-key (.primaryKey type-maker (into-array TitanType primary-key)))
       (.makeEdgeLabel type-maker))))

(defn create-property-key
  "Creates a property key with the given properties."
  ([tname data-type] (create-property-key tname data-type {}))
  ([tname data-type {:keys [unique-direction 
                            unique-locked 
                            group
                            indexed-vertex?
                            indexed-edge?
                            searchable?]
                     :or   {unique-direction false
                            unique-locked    true
                            group       default-group}}]
     (ensure-graph-is-transaction-safe)
     (let [type-maker   (.. (get-graph)
                            makeType
                            (name (name tname))
                            (group group)
                            (dataType data-type))]
       (println indexed-vertex? indexed-edge? searchable? unique-direction)
       (when indexed-vertex? 
         (if searchable?
           (.indexed type-maker "search" Vertex)
           (.indexed type-maker Vertex)))

       (when indexed-edge? 
         (if searchable?
           (.indexed type-maker "search" Edge)
           (.indexed type-maker Edge)))

       (when unique-direction
         (.unique type-maker 
                  (keyword-to-direction unique-direction) 
                  (convert-bool-to-lock unique-locked)))

       (.makePropertyKey type-maker))))

(defn create-edge-label-once
  "Checks to see if a edge label with the given name exists already.
  If so, nothing happens, otherwise it is created."
  ([name] (create-edge-label-once name {}))
  ([name m]
     (ensure-graph-is-transaction-safe)
     (if-let [named-type (get-type name)]
       named-type
       (create-edge-label name m))))

(defn create-property-key-once
  "Checks to see if a property key with the given name exists already.
  If so, nothing happens, otherwise it is created."
  ([name] (create-property-key-once name {}))
  ([name m]
     (ensure-graph-is-transaction-safe)
     (if-let [named-type (get-type name)]
       named-type
       (create-property-key m))))
