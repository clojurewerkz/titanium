(ns clojurewerkz.titanium.vertices
  (:refer-clojure :exclude [keys vals assoc! dissoc! get find])
  (:require [potemkin :as po]
            [archimedes.vertex :as vertex]))

;;Tinkerpop elements 
(po/import-fn vertex/get)
(po/import-fn vertex/keys)
(po/import-fn vertex/vals)
(po/import-fn vertex/id-of)
(po/import-fn vertex/assoc!)
(po/import-fn vertex/merge!)
(po/import-fn vertex/dissoc!)
(po/import-fn vertex/update!)
(po/import-fn vertex/clear!)

;;Tinkerpop Vertex
(po/import-fn vertex/refresh)
(po/import-fn vertex/delete!)
(po/import-fn vertex/to-map)

(po/import-fn vertex/find-by-id)
(po/import-fn vertex/find-by-kv)
(po/import-fn vertex/get-all-vertices)

(po/import-fn vertex/edges-of)
(po/import-fn vertex/all-edges-of)
(po/import-fn vertex/outgoing-edges-of)
(po/import-fn vertex/incoming-edges-of)
(po/import-fn vertex/connected-vertices-of)
(po/import-fn vertex/connected-out-vertices)
(po/import-fn vertex/connected-in-vertices)
(po/import-fn vertex/all-connected-vertices)

(po/import-fn vertex/create!)
(po/import-fn vertex/upsert!)
(po/import-fn vertex/unique-upsert!)
