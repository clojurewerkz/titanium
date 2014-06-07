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
  (:import (com.thinkaurelius.titan.core TitanGraph TitanType KeyMaker LabelMaker Order
                                         Parameter TypeMaker$UniquenessConsistency)
           (com.tinkerpop.blueprints Vertex Edge Graph)))

(defn get-type
  "In Titan, edge labels and property keys are types which can be
   individually configured to provide data verification, better storage
   efficiency, and higher performance. Types are uniquely identified by
   their name and are themselves vertices in the graph. Type vertices
   can be retrieved by their name."
  [^TitanGraph graph tname]
  (.getType graph (name tname)))

(defn- convert-bool-to-lock
  [b]
  (if b
    TypeMaker$UniquenessConsistency/LOCK
    TypeMaker$UniquenessConsistency/NO_LOCK))

(defn- convert-keyword-to-sort-order
  [kw]
  (case kw
    :asc  Order/ASC
    :desc Order/DESC))

(defn- ensure-collection
  "If `x` is a collecion, return that, otherwise return a single-element list
   containing `x`."
  [x]
  (if (coll? x) x (list x)))

(defn deflabel
  "Creates an edge label with the given properties, identified by `tname`.
   Attempting to define a new type with an existing name will result in
   an exception. Note that labels and keys share the same namespace,
   i.e., labels and keys cannot have the same name either. Use
   `deflabel-once` to check for existence of the named property before
   attempting to create it.

   Options:

     :unidirected? (default false)

     The default is for edges to be directed. Specifying `:unidirected
     true` configures this label to be uni-directed; this means that the
     edge is only created in the out-going direction. One can think of
     uni-directed edges as links pointing to another vertex such that
     only the outgoing vertex but not the incoming vertex is aware of
     its existence.

     :cardinality = :many-to-many (default) | :one-to-many | :one-to-one | :many-to-one

     A :one-to-many label allows at most one incoming edge of this label
     for each vertex in the graph. For instance, the label “fatherOf” is
     biologically a oneToMany edge label.

     A :many-to-one label allows at most one outgoing edge of this label
     for each vertex in the graph. For instance, the label “sonOf” is
     biologically a manyToOne edge label.

     A :one-to-one label allows at most one outgoing and one incoming
     edge of this label for each vertex in the graph

     When a :one-to-many, :many-to-one or :one-to-one cardinality is specified,
     we default to using locking to ensure the uniqueness constraint. You can
     override this by specifying `:unique-locked? false`, in which case concurrent
     transactions may overwrite existing uniqueness constraints.

     :sort-key = type-name | [type-name ...]

     Specifying the sort key of a label allows edges with this label to
     be efficiently retrieved in the specified sort-order.

     :sort-order = :asc (default) | :desc

     Defines in which order to sort the relations for efficient
     retrieval, i.e. either increasing (:asc) or decreasing (:desc). This option has no
     effect unless :sort-key is specified.

     :signature = type-name | [type-name ...]

     Specifying the signature of a type tells the graph database to
     expect that relations of this type always have or are likely to
     have an incident property or unidirected edge of the type included
     in the signature. This allows the graph database to store such
     edges more compactly and retrieve them more quickly. The signature
     should not contain any types already included in the sort key. The
     sort key provides the same storage and retrieval efficiency."
  ([graph tname] (deflabel graph tname {}))
  ([^TitanGraph graph tname {:keys [unidirected? cardinality sort-key sort-order
                                    signature unique-locked?]
                             :or {unidirected?   false
                                  cardinality    :many-to-many
                                  sort-order     :asc
                                  unique-locked? true}}]
     {:pre [(#{:many-to-many :many-to-one :one-to-one :one-to-many} cardinality)
            (#{:asc :desc} sort-order)]}
     (let [^LabelMaker label-maker (.makeLabel graph (name tname))
           ^TypeMaker$UniquenessConsistency consistency (convert-bool-to-lock unique-locked?)]
       (case cardinality
         :one-to-many  (.oneToMany label-maker consistency)
         :one-to-one   (.oneToOne  label-maker consistency)
         :many-to-one  (.manyToOne label-maker consistency)
         :many-to-many (.manyToMany label-maker))
       (when unidirected?
         (.unidirected label-maker))
       (when sort-key
         (.sortKey label-maker
                   (into-array TitanType (map (partial get-type graph)
                                              (ensure-collection sort-key))))
         (.sortOrder label-maker (convert-keyword-to-sort-order sort-order)))
       (when signature
         (.signature label-maker
                     (into-array TitanType (map (partial get-type graph)
                                                (ensure-collection signature)))))
       (.make label-maker))))

(defn- parse-index-specification
  [spec include-standard-index?]
  (let [indices (cond
                 (coll? spec) (into #{} (map name spec))
                 (true? spec) (conj #{} "standard")
                 spec         (conj #{} (name spec))
                 :else        #{})]
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
   transactions may overwrite existing uniqueness constraints.

   Note that we do not currently expose `list` types (which are only applicable to
   vertices) - each vertex or edge can have at most one value for a given key type."
  ([graph tname data-type] (defkey graph tname data-type {}))
  ([^TitanGraph graph tname data-type {:keys [vertex-index edge-index unique? unique-locked?]
                                       :or   {unique? false, unique-locked? true}}]
     (let [^KeyMaker key-maker (.makeKey graph (name tname))]
       (.dataType key-maker data-type)
       (doseq [index-name (parse-index-specification vertex-index unique?)]
         (.indexed key-maker index-name Vertex (make-array Parameter 0)))
       (doseq [index-name (parse-index-specification edge-index false)]
         (.indexed key-maker index-name Edge (make-array Parameter 0)))
       (when unique?
         (.unique key-maker (convert-bool-to-lock unique-locked?)))
       (.make key-maker))))

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
