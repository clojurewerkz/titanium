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
  (:import (com.thinkaurelius.titan.core TitanType KeyMaker LabelMaker TypeMaker$UniquenessConsistency)
           (com.tinkerpop.blueprints Vertex Edge Direction Graph)))

;; Types are uniquely identified by their name. Attempting to define a
;; new type with an existing name will result in an exception. Note,
;; that labels and keys share the same namespace, i.e., labels and keys
;; cannot have the same name either.

(defn get-type
  "In Titan, edge labels and property keys are types which can be
   individually configured to provide data verification, better storage
   efficiency, and higher performance. Types are uniquely identified by
   their name and are themselves vertices in the graph. Type vertices
   can be retrieved by their name."
  [graph tname]
  (.getType graph (name tname)))

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

(defn- parse-index-specification
  [spec include-standard-index?]
  (let [indices (cond
                 (coll? spec) (into #{} (map name spec))
                 (true? spec) (conj #{} "standard")
                 :else        (conj #{} (name spec)))]
    (if include-standard-index?
      (conj indices "standard")
      indices)))

(defn defkey
  "Creates a property key with the given properties, identified by `tname`.
   Attempting to define a new type with an existing name will result in
   an exception. Note that labels and keys share the same namespace,
   i.e., labels and keys cannot have the same name either. Use
   `defkey-once` to check for existence of the named property before
   attempting to create it.

   To index vertices with this property:

     (defkey graph :name java.lang.String {:vertex-index true})

   and to index edges:

     (defkey graph :name java.lang.String {:edge-index true})

   This will add items to the standard index. To specify a different index,
   give the index name rather than just a boolean:

     (defkey graph :name java.lang.String {:vertex-index \"search\"})

   The same property may be indexed for both edges and vertices, and one or
   other may appear in multiple indices:

     (defkey graph :name java.lang.String {:vertex-index [\"standard\" \"search\"]
                                           :edge-index true})

   Vertices (but not edges!) may be indexed uniquely:

     (defkey graph :ssn java.lang.String {:unique? true})

   Specifying `:unique? true` will automatically add the property to the standard
   index and will, by default, use a lock to ensure the uniqueness constraint. You can
   suppress locking by specifying `:unique-locked? false`, in which case concurrent
   transactions may overwrite existing uniqueness constraints."
  ([graph tname data-type] (defkey graph tname data-type {}))
  ([graph tname data-type {:keys [vertex-index edge-index unique? unique-locked?]
                           :or   {unique? false, unique-locked? true}}]
     (let [^KeyMaker type-maker (.makeType graph (name tname))]
       (.dataType type-maker data-type)
       (doseq [index-name (parse-index-specification vertex-index unique?)]
         (.indexed type-maker index-name Vertex))
       (doseq [index-name (parse-index-specification edge-index false)]
         (.indexed type-maker index-name Edge))
       (when unique?
         (.unique type-maker (convert-bool-to-lock unique-locked?)))
       (.make type-maker))))

(defn deflabel-once
  "Checks to see if a edge label with the given name exists already.
  If so, nothing happens, otherwise it is created. Note that no attempt
  is made to ensure that the pre-existing label has the desired properties."
  ([graph tname] (deflabel-once graph tname {}))
  ([graph tname m]
     (if-let [named-type (get-type graph tname)]
       named-type
       (deflabel graph tname m))))

(defn defkey-once
  "Checks to see if a property key with the given name exists already.
  If so, nothing happens, otherwise it is created. Note that no attempt
  is made to ensure that the pre-existing key has the desired properties."
  ([graph tname data-type] (defkey-once tname data-type {}))
  ([graph tname data-type m]
     (if-let [named-type (get-type graph tname)]
       named-type
       (defkey graph tname data-type m))))
