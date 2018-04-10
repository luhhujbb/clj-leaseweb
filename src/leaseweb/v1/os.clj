(ns leaseweb.v1.os
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.v1.core :as l]))

(def api-path "/operatingSystems")

(defn list
  []
  (:operatingSystems (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path})
      {:status 403}) 200 {:operatingSystems []} )))

(defn describe
  [os-id]
  (:operatingSystem (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id)})
      {:status 403}) 200 {:operatingSystem nil})))

(defn list-control-panels
  [os-id]
  (:controlPanels (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id "/controlPanels")})
      {:status 403}) 200 {:controlPanels []} )))

(defn describe-control-panel
  [os-id cp-id]
  (:controlPanel (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id "/controlPanels/" cp-id)})
      {:status 403}) 200 {:controlPanel nil} )))

(defn partition-schema
  [os-id server-id]
  (:partitionSchema (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" os-id "/partitionSchema")
               :body {:serverPackId server-id} })
      {:status 403}) 200 {:partitionSchema nil} )))
