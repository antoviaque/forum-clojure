(defproject forum "0.1.0-SNAPSHOT"
            :description "Simple forum based on Clojure and Backbone.js"
            :dependencies [
                    [org.clojure/clojure "1.3.0"]
                    [noir "1.2.1"]
                    [lein-eclipse "1.0.0"]
                    [cheshire "3.1.0"]
            ]
            :dev-dependencies [
                    [congomongo "0.1.8"]
            ]
            :main forum.server)

