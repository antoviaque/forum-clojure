(ns forum.db
  (:require [somnium.congomongo :as mongo]))

; Database ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def db
  (mongo/make-connection "forum"
                         :host "127.0.0.1"
                         :port 27017))

