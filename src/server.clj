(ns server
  (:require [ring.adapter.jetty :as j]
            [ring.middleware.params :as p]
            [ring.util.response :refer [resource-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as h]))

(defroutes app-routes
           (GET "/" [] (h/html [:h1 {:style "margin: 50 0 0 100;"} " Hello, Greenlabs! "]))
           (GET "/favicon.ico" [] "")
           (route/resources "/"))

(def app (-> app-routes
             p/wrap-params))

(def ^:dynamic server (j/run-jetty #'app {:port 3000 :join? false}))

(comment
  (.start server)
  (.stop server))