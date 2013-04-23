(ns clojurewerkz.titanium.query
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
(po/import-macro q/count-edges)