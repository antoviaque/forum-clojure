; Includes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns forum.views.api
  (:require [forum.auth :as auth]
            [forum.thread :as thread]
            [forum.message :as message]
            [forum.json :as json])
  (:use [noir.core :only [defpage pre-route]]
        [hiccup.core :only [html]]))

; Auth ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(pre-route [:post   "/api/applications/*"
            :put    "/api/applications/*"
            :delete "/api/applications/*"] {} (auth/require-group "admin"))


; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Auth ;;

(defpage [:get  "/api/auth/login/"
          :post "/api/auth/login/"] {:keys [login password]}
  (json/response (auth/authenticate login password)))

(defpage [:get  "/api/auth/logout/"
          :post "/api/auth/logout/"] {}
  (json/response (auth/logout)))

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
  (json/response (thread/get-all)))

(defpage [:get "/api/thread/:id"] {:keys [id]}
  (json/response (thread/get-one id)))

(defpage [:post "/api/thread"] {:keys [title]}
  (json/response (thread/new title)))

;; Messages ;;

(defpage [:post "/api/message"] {:keys [thread author text]}
  (json/response (message/new thread author text)))

