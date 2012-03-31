(ns forum.json
  (:require [noir.response :as response]
            [cheshire.custom :as json])

; Allows to decode _id attributes of DB results
(json/add-encoder org.bson.types.ObjectId 
  (fn [c jsonGenerator]
    (json/encode-str (.toString c) jsonGenerator)))

; JSON Conversion ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn response [db-result]
  (let [json-str (json/encode db-result)]
    (response/content-type "application/json" json-str)))
