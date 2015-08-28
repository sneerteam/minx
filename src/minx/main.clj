(ns minx.main
  (:require
    [minx.udp :refer [while-let]]
    [clojure.core.async :refer [thread chan >!! <!!]])
  (:import (java.net InetSocketAddress)))

; ctrl+0: slurp right
; ctrl+9: slurp left
; alt+0: barf right
; alt+9: farf left

(defn start-server! []
  (let [in (chan)
        out (chan)]
    (minx.udp/open! in out 2048)
    (thread
      (while-let [message (<!! in)]
          (println message "recebido")
          (>!! out message)))
    out))

(defn start-client! [host-name]
  (let [in (chan)
        out (chan)
        socket-address (InetSocketAddress. ^String host-name 2048)]
    (minx.udp/open! in out)
    (thread
      (while-let [message (<!! in)]
         (println message "recebido")))
    (>!! out [socket-address "oi"])
    (Thread/sleep 4000)))
