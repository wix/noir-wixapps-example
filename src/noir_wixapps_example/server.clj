(ns noir-wixapps-example.server
  (:import [org.apache.commons.codec.binary Base64])
  (:require [noir.server :as server]
             [clj-json.core :as json])
  (:use noir.core
        ring.middleware.params
        ring.middleware.wixapps-middleware
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpartial main-layout [title content]
  (html5
    [:head
     [:title "WixApps example: " title]]
    [:body
     content]))

(defn parse-instance [instance]
  (let [[given-hmac signed-instance] (clojure.string/split  instance #"\.")]
    (json/parse-string (String. (Base64/decodeBase64 (.getBytes signed-instance))))))

(defn check-owner [instance]
  (let [parsed-instance (parse-instance instance)]
  (get parsed-instance "permissions")))

(defpage "/widget" {:keys [instance]}
  (main-layout "Widget"
    [:p (parse-instance  instance) "widget test"]))

(defpage "/settings" {:keys [instance]}
  (if (check-owner instance)
    (main-layout  "Owner"
      [:p  (parse-instance instance) "settings test"])
    {:status 403 :body "Owner permissions required"}))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'noir-wixapps-example})))

(server/add-middleware wrap-params)
(server/add-middleware wrap-wixapps-middleware {:algorithm "HmacSHA256" :secret-key "49ac16a2-1fb3-44c6-9f4a-30ac8cc45562"})
