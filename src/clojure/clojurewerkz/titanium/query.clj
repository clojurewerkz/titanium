(ns clojurewerkz.titanium.query
  (:require [clojurewerkz.titanium.conversion :as cnv])
  (:import [com.tinkerpop.blueprints Vertex Edge Direction Query]
           [mikera.cljutils.namespace :as n]))

(n/pull-all archimedes.query)