; Includes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns forum.views.api
  (:require [forum.auth :as auth]
            [forum.thread :as thread]
            [forum.message :as message]
            [forum.json :as json]
            [forum.mail :as mail]
            [noir.response :as response])
  (:use [noir.core :only [defpage pre-route pre-routes]]
        [hiccup.core :only [html]]))


; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Auth ;;

(defpage [:get "/api/auth"] {}
  (json/response (auth/credentials)))

(defpage [:put "/api/auth"] {}
  (let [params (json/body-params)
        result (auth/login (params "login") (params "password"))]
    (json/encode result)))

(defpage [:delete "/api/auth"] {}
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

(defpage [:post "/api/thread"] {}
  (if (not (auth/group-member? "admin"))
    (auth/response-denied)
    (let [params (json/body-params)]
      (json/response (thread/new (params "title"))))))

;; Messages ;;

(defpage [:post "/api/message"] {}
  (if (not (auth/group-member? "admin"))
    (auth/response-denied)
    (let [params (json/body-params)]
      (json/response (message/new (params "thread")
                                  (params "author")
                                  (params "text"))))))

