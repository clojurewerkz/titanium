(ns clojurewerkz.titanium.vertices
  (:refer-clojure :exclude [keys vals assoc! dissoc! get find])
  (:require [mikera.cljutils.namespace :as n]))

(n/pull-all archimedes.vertex)
(n/pull-all clojurewerkz.titanium.elements)
