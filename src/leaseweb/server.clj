(ns leaseweb.server
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.core :as l]))

(def api-path "/bareMetals")

(defn list
  []
  (:bareMetals (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path})
      {:status 403}) 200 {:bareMetals []} )))

(defn describe
  [server-id]
  (:bareMetal (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id)})
      {:status 403}) 200 {:bareMetals nil})))

(defn ips
  [server-id]
  (:ips (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id "/ips")})
      {:status 403}) 200 {:ips []})))

(defn power-status
  [server-id]
  (:powerStatus (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id "/powerStatus")})
      {:status 403}) 200 {:powerStatus nil})))

(defn open-switchport
  [server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource (str api-path "/" server-id "/switchPort/open")})
      {:status 403}) 200 ))

(defn close-switchport
  [server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource (str api-path "/" server-id "/switchPort/close")})
      {:status 403}) 200 ))

(defn network-usage
  [server-id date-from date-to]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id "/networkUsage?" )
               :body (if-not (and (nil? date-from) (nil? date-to))
                        (let [body (if-not (nil? date-from)
                                      {:dateFrom date-from}
                                      {})
                              body* (if-not (nil? date-to)
                                            (assoc body :dateTo date-to)
                                            body)]
                          body*)
                        nil)})
      {:status 403}) 200 ))

(defn bandwidth-usage
  [server-id date-from date-to]
  (:bandwidth (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id "/networkUsage/bandwidth" )
               :body (if-not (and (nil? date-from) (nil? date-to))
                        (let [body (if-not (nil? date-from)
                                      {:dateFrom date-from}
                                      {})
                              body* (if-not (nil? date-to)
                                            (assoc body :dateTo date-to)
                                            body)]
                          body*)
                        nil)})
      {:status 403}) 200 {:bandwidth nil})))

(defn mk-partition-scheme
  "helper to build partition scheme object"
  [])

(defn get-init-root-password
  "retrieve initial rootpassword"
  [server-id]
  (:rootpassword
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id "/rootPassowrd")})
      {:status 403}) 200 {:rootpassword nil})))


(defn install
  "Install a serveur"
  [server-id os-id hdd raid-level number-disks]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource (str api-path "/" server-id "/install" )
               :body {:osId os-id
                      :hdd (generate-string hdd)
                      :raidLevel raid-level
                      :numberDisks number-disks}})
      {:status 403}) 200))

(defn install-status
  [server-id]
  (:installationStatus (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id "/installationStatus")})
      {:status 403}) 200 {:installationStatus
                            {
                              :code 404
                              :description "unknown"
                              :serverPackId "unknown"
                              :serverName "unknown" }})))

(defn reboot
  [server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource (str api-path "/" server-id "/powerCycle")})
      {:status 403}) 202 ))
