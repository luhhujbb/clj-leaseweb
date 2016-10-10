(ns leaseweb.os
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.core :as l]))

(def api-path "/operatingSystems")

(defn list
  []
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path})
      {:status 403}) 200 {} ))

(defn describe
  [os-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id)})
      {:status 403}) 200))

(defn list-control-panels
  [os-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id "/controlPanels")})
      {:status 403}) 200 {} ))

(defn describe-control-panel
  [os-id cp-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id "/controlPanels/" cp-id)})
      {:status 403}) 200 {} ))

(defn partition-schema
  [os-id server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id "/partitionSchema")
               :body {:serverPackId server-id} })
      {:status 403}) 200 {} ))
