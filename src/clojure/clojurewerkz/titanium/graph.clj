(ns clojurewerkz.titanium.graph
  (:require [potemkin :as po]
            [archimedes.core :as g])
  (:import  [com.thinkaurelius.titan.core TitanFactory TitanGraph]
            [com.tinkerpop.blueprints Vertex Edge
             Graph KeyIndexableGraph
             TransactionalGraph TransactionalGraph$Conclusion]
            [com.thinkaurelius.titan.core TitanTransaction]))

(po/import-fn g/get-graph)
(po/import-fn g/shutdown)
(po/import-fn g/get-feature)
(po/import-fn g/get-features)
(po/import-macro g/transact!)
(po/import-macro g/retry-transact!)
(po/import-macro g/with-graph)

;;
;; API
;;
(defn ensure-graph-is-transaction-safe
  "Ensure that we are either in a transaction or using an in-memory graph."
  []
  (when-not (instance? TitanTransaction (get-graph))
    (throw
      (Throwable.
       "All actions on a persistent graph must be wrapped in transact! "))))

(g/set-pre-fn! ensure-graph-is-transaction-safe)

(defn convert-config-map [m]
  (let [conf (org.apache.commons.configuration.BaseConfiguration.)]
    (doseq [[k1 v1] m]
            (if (string? v1)
              (.setProperty conf (name k1) v1)
              (doseq [[k2 v2] v1]
                (.setProperty conf (str (name k1) "." (name k2)) v2))))
    conf))

(defprotocol TitaniumGraph
  (^KeyIndexableGraph open [input] "Opens a new graph"))

(extend-protocol TitaniumGraph
  String
  (open [^String path]
    (g/set-graph! (TitanFactory/open path)))

  java.io.File
  (open [^java.io.File f]
    (g/set-graph! (TitanFactory/open (.getPath f))))

  org.apache.commons.configuration.Configuration
  (open [^org.apache.commons.configuration.Configuration conf]
    (g/set-graph! (TitanFactory/open conf)))

  java.util.Map
  (open [^java.util.Map m]
    (g/set-graph! (TitanFactory/open (convert-config-map m)))))

(defn open? []
  (.isOpen archimedes.core/*graph*))


;;
;; Automatic Indexing
;;

(defn index-vertices-by-key!
  [^KeyIndexableGraph g ^String k]
  (.createKeyIndex g k com.tinkerpop.blueprints.Vertex))

(defn deindex-vertices-by-key!
  [^KeyIndexableGraph g ^String k]
  (.dropKeyIndex g k com.tinkerpop.blueprints.Vertex))

(defn index-edges-by-key!
  [^KeyIndexableGraph g ^String k]
  (.createKeyIndex g k com.tinkerpop.blueprints.Edge))

(defn deindex-edges-by-key!
  [^KeyIndexableGraph g ^String k]
  (.dropKeyIndex g k com.tinkerpop.blueprints.Edge))
