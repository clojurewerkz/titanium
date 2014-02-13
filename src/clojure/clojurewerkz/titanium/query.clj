;; Copyright (c) 2013-2014 Michael S. Klishin, Alex Petrov, Zack Maril, and The ClojureWerkz
;; Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.titanium.query
  (:refer-clojure :exclude [count])
  (:import  [com.tinkerpop.blueprints Vertex Edge Direction Query])
  (:require [potemkin :as po]
            [archimedes.query :as q]))

(po/import-fn q/start-at)
(po/import-macro q/has)
(po/import-fn q/interval)
(po/import-fn q/direction)
(po/import-fn q/labels)
(po/import-fn q/limit)
(po/import-macro q/find-vertices)
(po/import-macro q/find-edges)
(po/import-macro q/count)
