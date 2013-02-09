(ns clojurewerkz.titanium.conversion
  (:import [com.tinkerpop.blueprints Graph Vertex Direction Query$Compare]))


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



(defprotocol QueryCompareConversion
  (^Query$Compare to-query-compare [input] "Converts input to a Blueprints query comparison operation"))

(extend-protocol QueryCompareConversion
  Query$Compare
  (to-query-compare [input]
    input)

  clojure.lang.Named
  (to-query-compare [input]
    (to-query-compare (name input)))

  String
  (to-query-compare [input]
    (case (.toLowerCase input)
      "equal" Query$Compare/EQUAL
      "="     Query$Compare/EQUAL
      "=="    Query$Compare/EQUAL

      "not_equal" Query$Compare/NOT_EQUAL
      "!="        Query$Compare/NOT_EQUAL
      "=/="       Query$Compare/NOT_EQUAL

      "greater_than" Query$Compare/GREATER_THAN
      ">"            Query$Compare/GREATER_THAN

      "greater_than_equal" Query$Compare/GREATER_THAN_EQUAL
      ">="                 Query$Compare/GREATER_THAN_EQUAL

      "less_than" Query$Compare/LESS_THAN
      "<"         Query$Compare/LESS_THAN

      "less_than_equal" Query$Compare/LESS_THAN_EQUAL
      "<="              Query$Compare/LESS_THAN_EQUAL)))
