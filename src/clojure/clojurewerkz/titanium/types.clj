(ns clojurewerkz.titanium.types
  (:import (com.thinkaurelius.titan.core TypeGroup
                                         TitanType))
  (:use [archimedes.core :only (*graph* *pre-fn*)]))

(defn get-type [tname]
  (*pre-fn*)
  (.getType *graph* (name tname)))

;; The default type group when no group is specified during type construction.
(def default-group (TypeGroup/DEFAULT_GROUP))

(defn create-group
  "Create a TitanGroup. \"A TitanGroup is defined with a name and an id, however, two groups with thesame id are considered equivalent. The name is only used for recognition and is not persisted in the database. Group ids must be positive (>0) and the maximum group id allowed is configurable.\"-http://thinkaurelius.github.com/titan/javadoc/current/com/thinkaurelius/titan/core/TypeGroup.html"
  [group-id group-name]
  (*pre-fn*)
  (TypeGroup/of group-id group-name))

(defn- create-type-maker
  "Reduces reduancy in edge-label and vertex-key creation methods"
  [tname {:keys [functional f-locked group]
          :or   {functional  false
                 f-locked    false
                 group       default-group}}]
  (*pre-fn*)
  (let [type-maker   (.. *graph*
                         makeType
                         (name (name tname))
                         (group group))]
    (when functional (.functional type-maker f-locked))
    type-maker))


(defn create-edge-label
  "Creates a edge label with the given properties."
  ([name] (create-edge-label name {}))
  ([name {:keys [simple direction primary-key signature]
          :as m
          :or {simple false
               direction "directed"
               primary-key nil
               signature   nil}}]
     (let [type-maker (create-type-maker name m)]
       (when simple (.simple type-maker))
       (case direction
         "directed"    (.directed type-maker)
         "unidirected" (.unidirected type-maker)
         "undirected"  (.undirected type-maker))
       (when signature (.signature type-maker signature))
       (when primary-key (.primaryKey type-maker (into-array TitanType primary-key)))
       (.makeEdgeLabel type-maker))))

(defn create-vertex-key
  "Creates a vertex key with the given properties."
  ([name data-type] (create-vertex-key name data-type {}))
  ([name data-type {:keys [unique indexed]
                    :as m
                    :or {unique false
                         indexed false}}]
     (let [type-maker (.. (create-type-maker name m)
                          (dataType data-type))]
       (when unique (.unique type-maker))
       (when indexed (.indexed type-maker))
       (.makePropertyKey type-maker))))

(defn create-edge-label-once
  "Checks to see if a edge label with the given name exists already.
  If so, nothing happens, otherwise it is created."
  [name & args]
  (if-let [named-type (get-type name)]
    named-type
    (apply create-edge-label (cons name args))))

(defn create-vertex-key-once
  "Checks to see if a vertex key with the given name exists already.
  If so, nothing happens, otherwise it is created."
  [name & args]
  (if-let [named-type (get-type name)]
    named-type
    (apply create-vertex-key (cons name args))))
