(ns forum.auth
  (:require [noir.session :as session]))

; Auth ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authenticate [login password]
  (if (and (= login "antoviaque") (= password "testpass"))
    (let [group "admin"]
      (session/put! :login login)
      (session/put! :group group)
      {:result "success" :login login :group group})
    {:result "failure" :login login}))

(defn logout []
  (session/clear!)
  {:result "success" :login nil :group nil})

(defn require-group [authgroup]
  (when-not (= (session/get :group) authgroup)
    {:status 403
     :body "You don't have the permission to do that."}))
  
