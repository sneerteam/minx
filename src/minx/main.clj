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
      (while-let [[address value] (<!! in)]
          (println value "recebido")
          (>!! out [address {:ip (.getHostString address) :port (.getPort address)}])))
    out))

(defn start-client! [host-name]
  (let [in (chan)
        out (chan)
        socket-address (InetSocketAddress. ^String host-name 2048)]
    (minx.udp/open! in out)
    (thread
      (while-let [message (<!! in)]
         (println message "recebido")))
    (>!! out [socket-address {:ip "123.123" :port 222}])
    (Thread/sleep 4000)))


(comment
  (def srv (start-server!))
  (clojure.core.async/close! srv)
  (minx.main/start-client! "127.0.0.1"))
