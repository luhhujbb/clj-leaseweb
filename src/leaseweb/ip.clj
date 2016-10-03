(ns leaseweb.os
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.core :as l]))

(def api-path "/ips")

(defn list
  []
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path})
      {:status 403}) 200 {} ))

(defn describe
  [ip-address]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path "/" ip-address})
      {:status 403}) 200))

(defn update
  [ip-address reverse-lookup null-routed]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "PUT"
               :resource api-path "/" ip-address
               :body {:reverseLookup reverse-lookup
                      :nullRouted null-routed}})
      {:status 403}) 200))
