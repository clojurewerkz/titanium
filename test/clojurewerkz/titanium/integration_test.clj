(ns clojurewerkz.titanium.integration-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.vertices :as tv]
            [clojurewerkz.titanium.edges    :as ted]
            [ogre.core :as g])
  (:use clojure.test)
  (:import java.io.File))

;; The Graph of the Gods example from the Titan wiki
(deftest test-integration-example1
  (tg/open-in-memory-graph)
  (let [saturn   (tv/create! {:name "Saturn"   :type "titan"})
        jupiter  (tv/create! {:name "Jupiter"  :type "god"})
        hercules (tv/create! {:name "Hercules" :type "demigod"})
        alcmene  (tv/create! {:name "Alcmene"  :type "human"})
        neptune  (tv/create! {:name "Neptune"  :type "god"})
        pluto    (tv/create! {:name "Pluto"    :type "god"})
        sea      (tv/create! {:name "Sea"      :type "location"})
        sky      (tv/create! {:name "Sky"      :type "location"})
        tartarus (tv/create! {:name "Tartarus" :type "location"})
        nemean   (tv/create! {:name "Nemean"   :type "monster"})
        hydra    (tv/create! {:name "Hydra"    :type "monster"})
        cerberus (tv/create! {:name "Cerberus" :type "monster"})]
    (ted/connect! neptune :lives sea)
    (ted/connect! jupiter :lives sky)
    (ted/connect! pluto :lives tartarus)
    (ted/connect! jupiter :father saturn)
    (ted/connect! hercules :father jupiter)
    (ted/connect! hercules :mother alcmene)
    (ted/connect! jupiter :brother pluto)
    (ted/connect! pluto :brother jupiter)
    (ted/connect! neptune :brother pluto)
    (ted/connect! pluto :brother neptune)
    (ted/connect! jupiter :brother neptune)
    (ted/connect! neptune :brother jupiter)
    (ted/connect! cerberus :lives tartarus)
    (ted/connect! pluto :pet cerberus)
    (ted/connect! hercules :battled nemean   {:times 1})
    (ted/connect! hercules :battled hydra    {:times 2})
    (ted/connect! hercules :battled cerberus {:times 12})
    (let [r1 (g/query saturn
                      (g/in :father)
                      (g/in :father)
                      g/first-of!)
          r2 (g/query hercules
                      (g/out :father :mother)
                      (g/property :name)
                      g/into-set!)
          r3 (g/query hercules
                      (g/--E> :battled)
                      (g/has :times > 1)
                      (g/in-vertex)
                      (g/property :name)
                      g/into-set!)
          c3 (g/query hercules
                      (g/--E> :battled)
                      (g/has :times > 1)
                      (g/in-vertex)
                      g/count)
          ;;TODO: count should have a bang.
          ;;Error in ogre. Simple fix for the next release. 
          r4 (g/query pluto
                      (g/--> :lives)
                      (g/<-- :lives)
                      (g/except [pluto])
                      (g/property :name)
                      g/into-set!)
          r5 (g/query pluto
                      (g/--> :brother)
                      (g/as  "god")
                      (g/out :lives)
                      (g/as  "place")
                      (g/select (g/prop :name))
                      g/all-into-vecs!)]
      (is (= r1 hercules))
      (is (= r2 #{"Alcmene" "Jupiter"}))
      (is (= r3 #{"Cerberus" "Hydra"}))
      (is (= c3 2))
      (is (= r4 #{"Cerberus"}))
      ;; when https://github.com/tinkerpop/pipes/issues/75 is fixed,
      ;; we will be able to turn tables into vectors of maps, as they
      ;; should be represented (Neocons does it for Cypher responses). MK.
      (is (= '(["Jupiter" "Sky"] ["Neptune" "Sea"])
             r5)))
    (tg/shutdown)))
