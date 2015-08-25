(ns minx.main
  (:require [clojure.core.async :refer [chan >!! <!!]])
  (:import (java.net InetSocketAddress)))

(defn start-server! []
  (let [echo (chan)]
    (minx.udp/open! echo echo 2048)
    echo))

(defn start-client! [host-name]
  (let [in (chan)
        out (chan)
        socket-address (InetSocketAddress. ^String host-name 2048)]
    (minx.udp/open! in out)
    (while true
      (>!! out [socket-address "oi"])
      (Thread/sleep 2000))))
