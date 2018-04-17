(ns leaseweb.v2.pnet
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.v2.core :as l]))

(def api-path "/bareMetals/v2/privateNetworks")

(defn list
  [client & {:keys [offset limit] :as query-params}]
  (l/validate
      (l/call client {:method "GET"
                      :query-params (into {} (remove (comp nil? second) query-params))
                      :resource api-path}) 200))

(defn create
  [client]
  (l/validate
      (l/call client {:method "POST"
               :resource api-path})
               200))

(defn delete
  [client pnet-id]
  (l/validate
      (l/call client {:method "DELETE"
               :resource api-path "/" pnet-id})
               200 {:message "Deletion failed"}))

(defn describe
  [client pnet-id]
  (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" pnet-id)})
                200))

(defn add-server
  [client pnet-id server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" pnet-id "/servers" )
               :body {:id server-id}})204 {} ))

(defn remove-server
  [client pnet-id server-id]
  (l/validate
      (l/call client {:method "DELETE"
               :resource (str api-path "/" pnet-id "/servers/" server-id ) }) 204 {}))
