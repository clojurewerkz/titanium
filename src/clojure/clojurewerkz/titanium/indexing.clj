;; Copyright (c) 2013-2014 Michael S. Klishin, Alex Petrov, Zack Maril, and The ClojureWerkz
;; Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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
