;; Copyright (c) 2013-2014 Michael S. Klishin, Alex Petrov, Zack Maril, and The ClojureWerkz
;; Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.titanium.graph
  (:require [potemkin :as po]
            [clojurewerks.archimedes.graph :as g])
  (:import  [com.thinkaurelius.titan.core TitanFactory TitanGraph]
            [com.tinkerpop.blueprints Vertex Edge
             Graph KeyIndexableGraph
             TransactionalGraph TransactionalGraph$Conclusion]
            [com.thinkaurelius.titan.core TitanTransaction]))

(po/import-fn g/shutdown)
(po/import-fn g/get-feature)
(po/import-fn g/get-features)
(po/import-fn g/new-transaction)
(po/import-fn g/commit)
(po/import-fn g/rollback)
(po/import-macro g/with-transaction)

;;
;; API
;;

(defn convert-config-map
  [m]
  (let [conf (org.apache.commons.configuration.BaseConfiguration.)]
    (doseq [[k1 v1] m]
      (.setProperty conf (name k1) v1))
    conf))

(defprotocol TitaniumGraph
  (^com.tinkerpop.blueprints.KeyIndexableGraph open [input] "Opens a new graph"))

(extend-protocol TitaniumGraph
  String
  (open [^String path]
    (TitanFactory/open path))

  java.io.File
  (open [^java.io.File f]
    (TitanFactory/open (.getPath f)))

  org.apache.commons.configuration.Configuration
  (open [^org.apache.commons.configuration.Configuration conf]
    (TitanFactory/open conf))

  java.util.Map
  (open [^java.util.Map m]
    (TitanFactory/open (convert-config-map m))))

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
