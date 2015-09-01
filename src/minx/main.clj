(ns minx.main
  (:require
    [minx.udp :refer [while-let]]
    [clojure.core.async :refer [thread chan >!! <!!]])
  (:import (java.net InetSocketAddress)))

; ctrl+0: slurp right
; ctrl+9: slurp left
; alt+0: barf right
; alt+9: barf left


(defn start-server! []
  (let [in (chan)
        out (chan)]
    (minx.udp/open! in out 2152)
    (thread
      (loop [addresses #{}]
        (when-let [[address _] (<!! in)]
          (>!! out [address addresses])
          (println "received from" (.getHostString address))
          (recur (conj addresses {:ip (.getHostString address)
                                  :port (.getPort address)})))))
    out))


(defn start-client! [host-name]
  (let [in (chan)
        out (chan)
        server-address (InetSocketAddress. ^String host-name 2152)]
    (minx.udp/open! in out)
    (thread
      (while-let [[address value] (<!! in)]
        (println "received from" (.getHostString address))
        (doseq [peer value]
          (>!! out [(InetSocketAddress. (peer :ip) (peer :port)) #{}]))))
    (thread
      (while (>!! out [server-address nil])
        (Thread/sleep 4000)))
    out))


(comment
  (def srv (start-server!))
  (clojure.core.async/close! srv)
  (def client (minx.main/start-client! "127.0.0.1"))
  (clojure.core.async/close! client))

