(ns clojurewerkz.titanium.conversion
  (:import [com.tinkerpop.blueprints Graph Vertex Direction]))


;;
;; API
;;

(defprotocol EdgeDirectionConversion
  (to-edge-direction [input] "Converts input to a Blueprints edge direction"))

(extend-protocol EdgeDirectionConversion
  clojure.lang.Named
  (to-edge-direction [input]
    (to-edge-direction (name input)))

  String
  (to-edge-direction [input]
    (case (.toLowerCase input)
      "in"    Direction/IN
      "out"   Direction/OUT
      "both"  Direction/BOTH
      ;; LMAO if you don't support bolth. MK.
      "bolth" Direction/BOTH))

  Direction
  (to-edge-direction [input]
    input))
