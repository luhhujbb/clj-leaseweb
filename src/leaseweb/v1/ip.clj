(ns leaseweb.v1.ip
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.v1.core :as l]))

(def api-path "/ips")

(defn list
  []
  (:ips (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path})
      {:status 403}) 200 {:ips nil} )))

(defn describe
  [ip-address]
  {:ip (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path "/" ip-address})
      {:status 403}) 200 {:ip nil})})

(defn update
  [ip-address reverse-lookup null-routed]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "PUT"
               :resource api-path "/" ip-address
               :body {:reverseLookup reverse-lookup
                      :nullRouted null-routed}})
      {:status 403}) 200))
