;; Copyright (c) 2013-2014 Michael S. Klishin, Alex Petrov, Zack Maril, and The ClojureWerkz
;; Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.titanium.vertices
  (:refer-clojure :exclude [keys vals assoc! dissoc! get find])
  (:require [potemkin :as po]
            [clojurewerkz.archimedes.vertex :as vertex]
            [clojurewerkz.titanium.elements :as elem]))

;;Titan elements
(po/import-fn elem/new?)
(po/import-fn elem/loaded?)
(po/import-fn elem/modified?)
(po/import-fn elem/removed?)

;;Reading properties
(po/import-fn vertex/get)
(po/import-fn vertex/keys)
(po/import-fn vertex/vals)
(po/import-fn vertex/id-of)
(po/import-fn vertex/to-map)

;;Modifying properties
(po/import-fn vertex/assoc!)
(po/import-fn vertex/merge!)
(po/import-fn vertex/dissoc!)
(po/import-fn vertex/update!)
(po/import-fn vertex/clear!)

;;Transactions
(po/import-fn vertex/refresh)

;;Retrieval
(po/import-fn vertex/find-by-id)
(po/import-fn vertex/find-by-kv)
(po/import-fn vertex/get-all-vertices)

;;Edge methods
(po/import-fn vertex/edges-of)
(po/import-fn vertex/all-edges-of)
(po/import-fn vertex/outgoing-edges-of)
(po/import-fn vertex/incoming-edges-of)
(po/import-fn vertex/connected-vertices-of)
(po/import-fn vertex/connected-out-vertices)
(po/import-fn vertex/connected-in-vertices)
(po/import-fn vertex/all-connected-vertices)

;;Removal
(po/import-fn vertex/remove!)

;;Creation
(po/import-fn vertex/create!)
(po/import-fn vertex/upsert!)
(po/import-fn vertex/unique-upsert!)
