(ns forum.auth
  (:require [noir.session :as session]))

; Auth ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn credentials []
  {:login (session/get :login) 
   :group (session/get :group)
   :id 1})

(defn login [login password]
  (if (and (= login "antoviaque") (= password "testpass"))
    (let [group "admin"]
      (session/put! :login login)
      (session/put! :group group)
      (assoc (credentials) :result "success"))
    (assoc (credentials) :result "failure")))

(defn logout []
  (session/clear!)
  {:result "success" :login nil :group nil})

(defn require-group [authgroup]
  (when-not (= (session/get :group) authgroup)
    {:status 403
     :body "You don't have the permission to do that."}))
  
