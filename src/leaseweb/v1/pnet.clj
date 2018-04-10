(ns leaseweb.v1.pnet
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.v1.core :as l]))

(def api-path "/privateNetworks")

(defn list
  []
  (:privateNetworks (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path})
      {:status 403}) 200 {:privateNetworks []} )))

(defn create
  []
  (:id (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource api-path})
      {:status 403}) 200 {:id nil} )))

(defn delete
  [pnet-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "DELETE"
               :resource api-path "/" pnet-id})
      {:status 403}) 200 {:message "Deletion failed"} ))

(defn describe
  [pnet-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" pnet-id)})
      {:status 403}) 200 {:id nil :name nil :bareMetals []}))

(defn add-server
  [pnet-id server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource (str api-path "/" pnet-id "/bareMetals" )
               :body {:bareMetalId server-id}})
      {:status 403}) 204 {} ))

(defn remove-server
  [pnet-id server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "DELETE"
               :resource (str api-path "/" pnet-id "/bareMetals/" server-id ) })
      {:status 403}) 204 {} ))
