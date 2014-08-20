;; Copyright (c) 2014 Ray Miller and The ClojureWerkz Team.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.titanium.schema
  (:import [com.thinkaurelius.titan.core.schema TitanManagement
                                                EdgeLabelMaker
                                                VertexLabelMaker
                                                PropertyKeyMaker]
           [com.thinkaurelius.titan.core TitanGraph Multiplicity Cardinality Order RelationType]
           [com.tinkerpop.blueprints Direction Edge Vertex]))

(defn- ensure-collection
  "If `x` is a collecion, return that, otherwise return a single-element list
   containing `x`."
  [x]
  (if (coll? x) x (list x)))

(defn with-management-system*
  [^TitanGraph graph f & {:keys [rollback?]}]
  (let [mgmt (.getManagementSystem graph)]
    (try
      (f mgmt)
      (if rollback? (.rollback mgmt) (.commit mgmt))
      (catch Throwable t
        (try (when (.isOpen mgmt) (.rollback mgmt)) (catch Exception _))
        (throw t)))))

(defmacro with-management-system
  [binding & body]
  `(with-management-system*
     ~(second binding)
     (^{:once true} fn* [~(first binding)] ~@body)
     ~@(rest (rest binding))))

(defn get-relation-type
  [^TitanManagement mgmt tname]
  (.getRelationType mgmt (name tname)))

(defn get-edge-label
  [^TitanManagement mgmt tname]
  (.getEdgeLabel mgmt (name tname)))

(defn get-vertex-label
  [^TitanManagement mgmt tname]
  (.getVertexLabel mgmt (name tname)))

(defn get-property-key
  [^TitanManagement mgmt tname]
  (.getPropertyKey mgmt (name tname)))

(defn keyword->multiplicity
  [kw]
  (case kw
    :multi         Multiplicity/MULTI
    :simple        Multiplicity/SIMPLE
    :many-to-many  Multiplicity/MULTI ; synonym
    :many-to-one   Multiplicity/MANY2ONE
    :one-to-many   Multiplicity/ONE2MANY
    :one-to-one    Multiplicity/ONE2ONE))

(defn make-edge-label
  "Creates an edge label with the given properties, identified by `tname`.
   Attempting to define a new type with an existing name will result in
   an exception. Note that labels and keys share the same namespace,
   i.e., labels and keys cannot have the same name either.

   Options:

     :unidirected? (default false)

     The default is for edges to be directed. Specifying `:unidirected
     true` configures this label to be uni-directed; this means that the
     edge is only created in the out-going direction. One can think of
     uni-directed edges as links pointing to another vertex such that
     only the outgoing vertex but not the incoming vertex is aware of
     its existence.

     :multiplicity = :many-to-many (default) | :one-to-many | :one-to-one | :many-to-one

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

     :signature = type-name | [type-name ...]

     Specifying the signature of a type tells the graph database to
     expect that relations of this type always have or are likely to
     have an incident property or unidirected edge of the type included
     in the signature. This allows the graph database to store such
     relations more compactly and retrieve them more quickly.  For
     instance, if all edges with label friend have a property with key
     createdOn, then specifying (createdOn) as the signature for label
     friend allows friend edges to be stored more efficiently.
     RelationTypes used in the signature must be either property
     out-unique keys or out-unique unidirected edge labels."

  [^TitanManagement mgmt tname & {:keys [unidirected? multiplicity signature]
                                  :or {unidirected? false multiplicity :multi}}]
  (let [^EdgeLabelMaker maker (.makeEdgeLabel mgmt (name tname))]
    (.multiplicity maker (keyword->multiplicity multiplicity))
    (when unidirected?
      (.unidirected maker))
    (when signature
      (.signature maker (into-array RelationType (map (partial get-relation-type mgmt)
                                                      (ensure-collection signature)))))
    (.make maker)))

(defn make-vertex-label
  "Creates a vertex label with the given properties.

   Options:

     :partition? (default false)

     Enables partitioning for this vertex label. If a vertex label is
     partitioned, all of its vertices are partitioned across the
     partitions of the graph.

     :static? (default false)

     Makes this vertex label static, which means that vertices of this
     label cannot be modified outside of the transaction in which they
     were created."
  [^TitanManagement mgmt tname & {:keys [partition? static?]}]
  (let [^VertexLabelMaker maker (.makeVertexLabel mgmt (name tname))]
    (when partition? (.partition maker))
    (when static? (.setStatic maker))
    (.make maker)))

(defn keyword->cardinality
  [kw]
  (case kw
    :list   Cardinality/LIST
    :set    Cardinality/SET
    :single Cardinality/SINGLE))

(defn make-property-key
  "Creates a property key with the specified properties.

  `data-type` is required and configures the data type for this property
   key. Property instances for this key will only accept values that are
   instances of this class. Every property key must have its data type
   configured. Setting the data type to Object.class allows any type of
   value but comes at the expense of longer serialization because class
   information is stored with the value.

   Options:

     :cardinality = :single (default) | :list | :set

     :signature = type-name | [type-name ...]

     Specifying the signature of a type tells the graph database to
     expect that relations of this type always have or are likely to
     have an incident property or unidirected edge of the type included
     in the signature. This allows the graph database to store such
     relations more compactly and retrieve them more quickly."
  [^TitanManagement mgmt tname data-type & {:keys [cardinality signature]
                                            :or {cardinality :single}}]
  (let [^PropertyKeyMaker maker (.makePropertyKey mgmt (name tname))]
    (.dataType maker data-type)
    (.cardinality maker (keyword->cardinality cardinality))
    (when signature
      (.signature maker (into-array RelationType (map (partial get-relation-type mgmt)
                                                      (ensure-collection signature)))))
    (.make maker)))

(defn keyword->direction
  [kw]
  (case kw
    :both Direction/BOTH
    :in   Direction/IN
    :out  Direction/OUT))

(defn keyword->order
  [kw]
  (case kw
    :asc  Order/ASC
    :desc Order/DESC))

(defn keyword->element-type
  [kw]
  (case kw
    :edge   Edge
    :vertex Vertex))

(defn- index-builder
  "Helper function providing common functionality for `build-composite-index`
   and `build-mixed-index`."
  [^TitanManagement mgmt index-name element-type keys index-only]
  (let [builder (.buildIndex mgmt (name index-name) (keyword->element-type element-type))]
    (doseq [k (ensure-collection keys)]
      (if-let [property (get-property-key mgmt k)]
        (.addKey builder property)
        (throw (Exception. (format "Property key %s not defined" k)))))
    (when index-only
      (if-let [relation-type (case element-type
                               :edge (get-edge-label mgmt index-only)
                               :vertex (get-vertex-label mgmt index-only))]
        (.indexOnly builder relation-type)
        (throw (Exception. (format "Relation type %s not defined" index-only)))))
    builder))

(defn build-composite-index
  "Composite indices retrieve vertices or edges by one or a (fixed)
   composition of multiple keys. `element-type` (:edge or :vertex) determines
   whether this index is for edges or vertices, and `keys` specifies the property
   keys for this index.

   Options:

     :unique = true | false (default)

     Makes this index unique for the specified element-type.

    :index-only = schemaType

    Restricts this index to only those elements that have the provided
    schemaType. If this graph index indexes vertices, then the argument
    is expected to be a vertex label and only vertices with that label
    will be indexed. Likewise, for edges and properties only those with
    the matching relation type will be indexed."
  [^TitanManagement mgmt index-name element-type keys & {:keys [unique? index-only]}]
  (let [builder (index-builder mgmt index-name element-type keys index-only)]
    (when unique? (.unique builder))
    (.buildCompositeIndex builder)))

(defn build-mixed-index
  "Mixed indices retrieve vertices or edges by any combination of
   previously added property keys. Mixed indexes provide more flexibility
   than composite indexes and support additional condition predicates
   beyond equality.  `element-type` (:edge or :vertex) determines
   whether this index is for edges or vertices, and `keys` specifies the property
   keys for this index. Mixed indices require an indexing backend to be configured;
   this is uniquely identified by `backend-name`."
  [^TitanManagement mgmt index-name element-type keys backend-name & {:keys [index-only]}]
  (let [builder (index-builder mgmt index-name element-type keys index-only)]
    (.buildMixedIndex builder backend-name)))

(defn build-edge-index
  "Creates a vertex-centric index for the specified edge label, i.e. all
   edges of that label will be indexed according to this index
   definition which will speed up certain vertex-centric queries.  An
   index is defined by its name, the direction in which the index should
   be created (:in, :out or :both), the sort order (:asc or :desc) and -
   most importantly - the sort keys that define the index key."
  [^TitanManagement mgmt index-name label-name direction sort-keys  & {:keys [order]
                                                                       :or {order :asc}}]
  (if-let [label (get-edge-label mgmt label-name)]
    (.buildEdgeIndex mgmt
                     label
                     index-name
                     (keyword->direction direction)
                     (keyword->order order)
                     (into-array RelationType (map (partial get-relation-type mgmt)
                                                   (ensure-collection sort-keys))))
    (throw (Exception. (format "Label %s not defined" label-name)))))

(defn build-property-index
  "Creates a vertex-centric index for the provided property key,
   i.e. all properties of that key will be indexed according to this
   index definition which will speed up certain vertex-centric
   queries. An index is defined by its name, the sort order and - most
   importantly - the sort keys that define the index key."
  [^TitanManagement mgmt index-name property-name sort-keys & {:keys [order]
                                                               :or {order :asc}}]
  (if-let [property (get-property-key mgmt property-name)]
    (.buildPropertyIndex mgmt
                         property
                         index-name
                         (keyword->order order)
                         into-array RelationType (map (partial get-relation-type mgmt)
                                                      (ensure-collection sort-keys)))
    (throw (Exception. (format "Property %s not defined" property-name)))))
