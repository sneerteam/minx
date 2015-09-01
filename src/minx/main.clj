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
    (minx.udp/open! in out 2049)
    (thread
      (loop [addresses #{}]
        (when-let [[address _] (<!! in)]
          (>!! out [address addresses])
          (println "recebido" address)
          (recur (conj addresses {:ip (.getHostString address)
                                  :port (.getPort address)})))))
    out))


(defn start-client! [host-name]
  (let [in (chan)
        out (chan)
        server-address (InetSocketAddress. ^String host-name 2049)]
    (minx.udp/open! in out)
    (thread
      (while-let [[address value] (<!! in)]
        (println "recebido" address)
        (doseq [peer value]
          (>!! out [peer #{}]))))
    (thread
      (while (>!! out [server-address nil])
        (Thread/sleep 4000)))
    out))


(comment
  (def srv (start-server!))
  (clojure.core.async/close! srv)
  (def client (minx.main/start-client! "127.0.0.1")))

