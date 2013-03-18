(ns clojurewerkz.titanium.edges
  (:refer-clojure :exclude [keys vals assoc! dissoc! get find])
  (:require [mikera.cljutils.namespace :as n])
  (:import  [com.tinkerpop.blueprints Vertex Edge Direction]))


(n/pull-all archimedes.edge)
(n/pull-all clojurewerkz.titanium.elements)