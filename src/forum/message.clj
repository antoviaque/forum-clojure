(ns forum.message
  (:require [forum.thread :as thread])
  (:use [somnium.congomongo]
        [forum.db :only [db]]))

; Messages ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new [thread-id author text]
  (let [cur-thread (thread/get-one thread-id)
        message {:author author :text text}
        updated-thread (update-in cur-thread [:messages] conj message)]
    (with-mongo db
      (update! :threads cur-thread updated-thread))
    updated-thread))
