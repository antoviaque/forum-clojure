; Includes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns forum.views.api
  (:require [forum.views.auth :as auth]
            [forum.views.thread :as thread]
            [forum.views.message :as message]
            [noir.response :as response])
  (:use [noir.core :only [defpage pre-route]]
        [hiccup.core :only [html]]
        [cheshire.custom]))

(add-encoder org.bson.types.ObjectId encode-str)

; Auth ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(pre-route [:post   "/api/applications/*"
            :put    "/api/applications/*"
            :delete "/api/applications/*"] {} (auth/require-group "admin"))


; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Auth ;;

(defpage [:get  "/api/auth/login/"
          :post "/api/auth/login/"] {:keys [login password]}
  (response/json (auth/authenticate login password)))

(defpage [:get  "/api/auth/logout/"
          :post "/api/auth/logout/"] {}
  (response/json (auth/logout)))

;; Static ;;

; Static files are normally handled by nginx, but add rules to get it
; to work in dev mode out of the box.

(defn load-index []
  (let [current_dir (System/getProperty "user.dir")
        index_path (str current_dir "/resources/public/index.html")]
    (slurp index_path)))

(defpage [:get "/"] {}
  (load-index))

(defpage [:get "/thread/:id/"] {:keys [id]}
  (load-index))

;; Threads ;;

(defpage [:get "/api/thread"] {}
  (encode (thread/get-all)))

(defpage [:get "/api/thread/:id"] {:keys [id]}
  (generate-string (thread/get-one id)))

(defpage [:post "/api/thread"] {:keys [title]}
  (generate-string (thread/new title)))

;; Messages ;;

(defpage [:post "/api/message"] {:keys [thread author text]}
  (generate-string (message/new thread author text)))

