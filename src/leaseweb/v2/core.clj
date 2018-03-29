(ns leaseweb.v2.core
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [clj-http.client :as http]
            [digest]))

(def endpoint "https://api.leaseweb.com")

(def request-conf {:accept :json
                   :as :json
                   :throw-exceptions false})

(def request-alt-conf {:accept :json
                  :as :json
                  :throw-exceptions false})

(defn mk-headers [client] {"X-Lsw-Auth" (:token client)})

(defn mk-client [token] {:token token})

(defn validate
  ([res code]
    (validate res code nil))
  ([res code fallback-value]
  (if (= code (:status res))
    (:body res)
    fallback-value)))


(defmulti call (fn [_ params] (:method params)))

(defmethod call "GET" [client params]
  (let [url (str endpoint (:resource params))
        opts* (merge {:headers (mk-headers client) :accept :json :as :json} request-conf)
        opts (if-not (nil? (:query-params params))
              (merge {:query-params (:query-params params)} opts*)
              opts*)]
        (try
          (http/get url opts)
          (catch Exception e
            (log/error "Ressource : "url "- Error :" e)
            {:status 500}))))

(defmethod call "PUT" [client params]
  (let [url (str endpoint (:resource params))
        opts (merge {:body (generate-string (:body params)) :headers (mk-headers client)} request-alt-conf)]
      (try
        (http/put url opts)
        (catch Exception e
          (log/error "Ressource : "url "- Error :" e)
          {:status 500}))))

(defmethod call "POST" [client params]
  (let [url (str endpoint (:resource params))
        opts (merge {:body (generate-string (:body params)) :headers (mk-headers client)} request-alt-conf)]
      (try
        (http/post url opts)
        (catch Exception e
          (log/error "Ressource : "url "- Error :" e)
          {:status 500}))))

(defmethod call "DELETE" [client params]
 (let [url (str endpoint (:resource params))
       opts (merge {:headers (mk-headers client)} request-conf)]
    (try
      (http/delete url opts)
      (catch Exception e
        (log/error "Ressource : "url "- Error :" e)
        {:status 500}))))

(defmethod call :default [client params]
  (log/info "Unsupported http verb"))
