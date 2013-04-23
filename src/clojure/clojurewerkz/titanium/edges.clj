(ns clojurewerkz.titanium.edges
  (:refer-clojure :exclude [keys vals assoc! dissoc! get find])
  (:require [potemkin :as po]
            [archimedes.edge :as edge]
            [clojurewerkz.titanium.elements :as elem]))

;;Titan elements
(po/import-fn elem/new?)
(po/import-fn elem/loaded?)
(po/import-fn elem/modified?)
(po/import-fn elem/removed?)

;;Reading properties
(po/import-fn edge/get)
(po/import-fn edge/keys)
(po/import-fn edge/vals)
(po/import-fn edge/id-of)
(po/import-fn edge/to-map)
(po/import-fn edge/label-of)

;;Modifying properties
(po/import-fn edge/assoc!)
(po/import-fn edge/merge!)
(po/import-fn edge/dissoc!)
(po/import-fn edge/update!)
(po/import-fn edge/clear!)

;;Transactions
(po/import-fn edge/refresh)


;;Retrieval
(po/import-fn edge/find-by-id)
(po/import-fn edge/get-all-edges)

;;Vertex
(po/import-fn edge/get-vertex)
(po/import-fn edge/head-vertex)
(po/import-fn edge/tail-vertex)
(po/import-fn edge/endpoints)
(po/import-fn edge/edges-between)
(po/import-fn edge/connected?)

;;Removal
(po/import-fn edge/remove!)

;;Creation
(po/import-fn edge/connect!)
(po/import-fn edge/upconnect!)
(po/import-fn edge/unique-upconnect!)