(ns forum.thread
  (:use [noir.core :only [defpage pre-route]]
        [forum.db :only [db]]
        [somnium.congomongo])
  (:import (org.bson.types ObjectId)))

; Threads ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-one [id]
  (with-mongo db
    (fetch-by-id :threads (new ObjectId id))))

(defn get-all []
  (with-mongo db
    (fetch :threads)))

(defn new [title]
  (with-mongo db
    (insert! :threads {:title title})))
