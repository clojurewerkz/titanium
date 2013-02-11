(ns clojurewerkz.titanium.gpipe
  (:refer-clojure :exclude [filter count next])
  (:require [clojurewerkz.titanium.conversion :as cnv])
  (:import [com.tinkerpop.blueprints Vertex]
           [com.tinkerpop.gremlin.java GremlinPipeline]
           [com.tinkerpop.pipes PipeFunction]
           org.clojurewerkz.titanium.pipes.ClojurePipeFunction))


;;
;; API
;;

(defn has
  ([^GremlinPipeline p k v]
     (.has p (name k) v))
  ([^GremlinPipeline p k op v]
     (.has p (name k) (cnv/to-tokens-t op) v)))

(defn has-not
  ([^GremlinPipeline p k v]
     (.hasNot p (name k) v))
  ([^GremlinPipeline p k op v]
     (.hasNot p (name k) (cnv/to-tokens-t op) v)))

(defn out
  [^GremlinPipeline p & xs]
  (.out p ^"[Ljava.lang.String;" (into-array String xs)))

(defn out-e
  [^GremlinPipeline p & xs]
  (.outE p ^"[Ljava.lang.String;" (into-array String xs)))

(defn out-v
  [^GremlinPipeline p]
  (.outV p))

(defn in
  [^GremlinPipeline p & xs]
  (.in p ^"[Ljava.lang.String;" (into-array String xs)))

(defn in-e
  [^GremlinPipeline p & xs]
  (.inE p ^"[Ljava.lang.String;" (into-array String xs)))

(defn in-v
  [^GremlinPipeline p]
  (.inV p))

(defn both
  [^GremlinPipeline p & xs]
  (.both p ^"[Ljava.lang.String;" (into-array String xs)))

(defn both-e
  [^GremlinPipeline p & xs]
  (.bothE p ^"[Ljava.lang.String;" (into-array String xs)))

(defn both-v
  [^GremlinPipeline p]
  (.bothV p))

(defn label
  [^GremlinPipeline p]
  (.label p))

(defn except
  [^GremlinPipeline p coll]
  (.except p coll))

(defn random
  [^GremlinPipeline p ^double bias]
  (.random p bias))

(defn count
  [^GremlinPipeline p]
  (.count p))

(defn back
  [^GremlinPipeline p ^long steps]
  (.back p steps))

(defn back-to
  [^GremlinPipeline p ^String named-step]
  (.back p named-step))

(defn dorun
  [^GremlinPipeline p]
  (.iterate p))

(defn dedup
  ([^GremlinPipeline p]
     (.dedup p)))

(defn next
  [^GremlinPipeline p ^long n]
  (.next p n))

(defn property
  [^GremlinPipeline p prop]
  (.property p (name prop)))

(defn order
  ([^GremlinPipeline p]
     (.order p))
  ([^GremlinPipeline p op]
     (.order p (cnv/to-tokens-t op))))

(defn ^PipeFunction pipe-fn
  "Constructs a pipe function (as in, Tinkerpop Pipes) out of Clojure function"
  [^clojure.lang.IFn f]
  (ClojurePipeFunction. f))

(defn filter
  [^GremlinPipeline p f]
  (.filter p (pipe-fn f)))

(defn id
  [^GremlinPipeline p]
  (.id p))


(defmacro pipeline
  [& body]
  `(-> (GremlinPipeline.)
       ~@body))

(defmacro start-at
  [starting-point & body]
  `(-> (GremlinPipeline. ~starting-point)
       ~@body))

(defn into-vector
  [^GremlinPipeline p]
  (into [] p))

(defn into-set
  [^GremlinPipeline p]
  (into #{} p))

;; Currently running a pipeline that has a PipeFunction in
;; it hangs with Pipes 2.2.0. Thread dumps suggest it may be
;; a class loaders issue. Needs investigation. MK.
#_ (defn step
     [^GremlinPipeline p f]
     (.step p (pipe-fn f)))
