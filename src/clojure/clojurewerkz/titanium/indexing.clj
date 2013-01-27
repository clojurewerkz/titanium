(ns clojurewerkz.titanium.indexing
  (:refer-clojure :exclude [get remove count])
  (:import [com.tinkerpop.blueprints Graph Index Element]))


;;
;; API
;;

(defn put
  "Adds graph element to an index"
  [^Index idx k v ^Element el]
  (.put idx k v el))

(defn get
  "Looks graph element up in an index"
  [^Index idx k v]
  (.get idx k v))

(defn remove
  "Removes graph element from an index"
  [^Index idx k v el]
  (.remove idx k v el))

(defn count
  "Counts graph elements in an index"
  [^Index idx k v]
  (.count idx k v))
