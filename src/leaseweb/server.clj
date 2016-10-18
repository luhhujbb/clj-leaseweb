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
      {:status 403}) 200 {:bareMetals nil} )))

(defn describe
  [server-id]
  (:bareMetal (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id)})
      {:status 403}) 200 {:bareMetals nil})))

(defn suggested-raid-configuration
  [server-description]
  (let [disks-conf (get-in server-description [:server :hardDisks] nil)]
        (if-let [match (get (re-matches #"([0-9]+)x.*" disks-conf) 1)]
          (let [disk-nb (Long/parseLong match)]
            {:number-disks disk-nb
             :raid-level (cond
                            (= 2 disk-nb) 1
                            (= 3 disk-nb) 5
                            (= 4 disk-nb) 5
                            (> disk-nb 4) 6
                            :else nil)})
          nil)))

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
  (:rootPassword
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id "/rootPassword")})
      {:status 403}) 200 {:rootPassword nil})))



(defn install
  "Install a serveur"
  [server-id os-id hdd raid-level number-disks]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource (str api-path "/" server-id "/install" )
               :body (l/build-nested-params "" {:osId os-id
                      :hdd hdd
                      :raidLevel raid-level
                      :numberDisks number-disks})})
      {:status 403}) 200 "error"))

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
