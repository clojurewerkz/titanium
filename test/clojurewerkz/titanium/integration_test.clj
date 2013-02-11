(ns clojurewerkz.titanium.integration-test
  (:require [clojurewerkz.titanium.graph    :as tg]
            [clojurewerkz.titanium.elements :as te]
            [clojurewerkz.titanium.edges    :as ted]
            [clojurewerkz.titanium.query    :as q]
            [clojurewerkz.titanium.gpipe    :as p])
  (:use clojure.test)
  (:import java.io.File))

(defn tmp-dir
  [s]
  (let [f (File. (str (System/getProperty "java.io.tmpdir") File/pathSeparator s))]
    (.mkdirs f)
    (.deleteOnExit f)
    (.getPath f)))

;; The Graph of the Gods example from the Titan wiki
(deftest test-integration-example1
  (let [g        (tg/open-in-memory-graph)
        saturn   (tg/add-vertex g {:name "Saturn"   :type "titan"})
        jupiter  (tg/add-vertex g {:name "Jupiter"  :type "god"})
        hercules (tg/add-vertex g {:name "Hercules" :type "demigod"})
        alcmene  (tg/add-vertex g {:name "Alcmene"  :type "human"})
        neptune  (tg/add-vertex g {:name "Neptune"  :type "god"})
        pluto    (tg/add-vertex g {:name "Pluto"    :type "god"})
        sea      (tg/add-vertex g {:name "Sea"      :type "location"})
        sky      (tg/add-vertex g {:name "Sky"      :type "location"})
        tartarus (tg/add-vertex g {:name "Tartarus" :type "location"})
        nemean   (tg/add-vertex g {:name "Nemean"   :type "monster"})
        hydra    (tg/add-vertex g {:name "Hydra"    :type "monster"})
        cerberus (tg/add-vertex g {:name "Cerberus" :type "monster"})]
    (tg/add-edge g neptune sea "lives")
    (tg/add-edge g jupiter sky "lives")
    (tg/add-edge g pluto tartarus "lives")
    (tg/add-edge g jupiter saturn "father")
    (tg/add-edge g hercules jupiter "father")
    (tg/add-edge g hercules alcmene "mother")
    (tg/add-edge g jupiter pluto "brother")
    (tg/add-edge g pluto jupiter "brother")
    (tg/add-edge g neptune pluto "brother")
    (tg/add-edge g pluto neptune "brother")
    (tg/add-edge g jupiter neptune "brother")
    (tg/add-edge g neptune jupiter "brother")
    (tg/add-edge g cerberus tartarus "lives")
    (tg/add-edge g pluto cerberus "pet")
    (tg/add-edge g hercules nemean   "battled" {:times 1})
    (tg/add-edge g hercules hydra    "battled" {:times 2})
    (tg/add-edge g hercules cerberus "battled" {:times 12})
    (let [r1 (first (p/start-at saturn
                                (p/in "father")
                                (p/in "father")))
          r2 (p/start-at hercules
                         (p/out "father" "mother")
                         (p/property :name)
                         (p/into-set))
          r3 (p/start-at hercules
                         (p/out-e "battled")
                         (p/has :times '> 1)
                         (p/in-v)
                         (p/property :name)
                         (p/into-set))
          c3 (p/start-at hercules
                         (p/out-e "battled")
                         (p/has :times '> 1)
                         (p/in-v)
                         (p/count))
          r4 (p/start-at pluto
                         (p/out "lives")
                         (p/in  "lives")
                         (p/except [pluto])
                         (p/property :name)
                         (p/into-set))
          r5 (->> (p/start-at pluto
                              (p/out "brother")
                              (p/as  "god")
                              (p/out "lives")
                              (p/as  "place")
                              (p/select (fn [v]
                                          (.getProperty v "name")))
                              (p/into-set))
                  (map (fn [row]
                         (into [] row))))]
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
    (tg/close g)))
