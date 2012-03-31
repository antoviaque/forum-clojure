(ns forum.thread
  (:use [noir.core :only [defpage pre-route]]
        [forum.db :only [db]]
        [somnium.congomongo]))

; Threads ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-one [id]
  {:_id 123
   :title "Test thread"
   :messages '({:_id 456 :date_added "Now" :author "Xavier" :text "Bla bla bla"}
               {:_id 457 :date_added "Yesterday" :author "Tom" :text "Blo blo blo"})})

(defn get-all []
  (with-mongo db
    (fetch :threads)))

(defn new [title]
  {})
