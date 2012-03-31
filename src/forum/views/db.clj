(ns forum.views.db
  (:require [somnium.congomongo :as mongo]))

; Database ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def db
  (mongo/make-connection "forum"
                         :host "127.0.0.1"
                         :port 27017))

;(defn str-id [item]
;  (assoc item :_id (str (item :_id))))

;(defn clean-single [item]
;  (let [item-cleanstr (str-id item)]
;    (str-date item-cleanstr)))

;(defn clean [set]
;  (map clean-single set))
