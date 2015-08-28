(ns minx.serialization
  (:refer-clojure :exclude [read write])
  (:import
    [java.io ByteArrayInputStream ByteArrayOutputStream])
  (:require [cognitect.transit :as transit]))

(def ^:private transit-format :json) ; other options are :json-verbose and :msgpack

(def ^:private write-handlers
  {clojure.lang.PersistentQueue
   (transit/write-handler
     (fn [_] "queue")
     (fn [^clojure.lang.PersistentQueue q] (vec q)))})

(def ^:private read-handlers
  {"queue"
   (transit/read-handler
     (fn [coll] (into clojure.lang.PersistentQueue/EMPTY coll)))})

(def ^:private write-opts {:handlers write-handlers})

(def ^:private read-opts {:handlers read-handlers})

(def write transit/write)
(def read transit/read)

(defn writer [output-stream]
  (transit/writer output-stream transit-format write-opts))

(defn reader [input-stream]
  (transit/reader input-stream transit-format read-opts))

(defn serialize [value]
  (let [out (ByteArrayOutputStream.)]
    (write (writer out) value)
    (.toByteArray out)))

(defn deserialize
  ([^bytes bytes]
   (deserialize bytes (alength bytes)))
  ([bytes length]
   (let [in (ByteArrayInputStream. bytes 0 length)]
     (read (reader in)))))

(defn roundtrip [value max-size]
  (let [^bytes bytes (serialize value)
        size (alength bytes)]
    (when (> size max-size)
      (throw (IllegalArgumentException. (str "Value too large (" size " bytes). Maximum is " max-size " bytes."))))
    (deserialize bytes)))


(comment
  (assert (= [1 2 3] (roundtrip [1 2 3] 1024))))