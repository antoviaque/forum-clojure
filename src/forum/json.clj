(ns forum.json
  (:require [noir.response :as response]
            [cheshire.custom :as json])
  (:use [noir.request :only [ring-request]]))

; Allows to decode _id attributes of DB results
(json/add-encoder org.bson.types.ObjectId 
  (fn [c jsonGenerator]
    (json/encode-str (.toString c) jsonGenerator)))

; JSON Conversion ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn encode [obj]
  (json/encode obj))

(defn response [db-result]
  (let [json-str (encode db-result)]
    (response/content-type "application/json" json-str)))

(defn body-params []
  (let [request (ring-request)
         body (slurp (:body request))]
    (json/decode body)))
