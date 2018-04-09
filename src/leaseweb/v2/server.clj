(ns leaseweb.v2.server
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.v2.core :as l]))

(def api-path "/bareMetals/v2")

;;updated

(defn list
  [client & {:keys [offset limit ip macAddress reference site] :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource (str api-path "/servers")
                      :query-params (into {} (remove (comp nil? second) query-params))})
       200 {:servers nil :_metadata {:error true}}))

(defn list-all
    [client & {:keys [batch-size] :or {batch-size 50}}]
    (let [servers (loop [ss []
                        offset 0]
          (let [ss-batch (leaseweb.v2.server/list client :limit batch-size :offset offset)]
            (if (= (count (:servers ss-batch)) batch-size)
                (recur (concat ss (:servers ss-batch)) (+ offset batch-size))
                (concat ss (:servers ss-batch)))))]
        {:servers servers :_metadata {:totalCount (count servers) :offset 0}}))

(defn describe
  [client server-id]
    (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/servers/" server-id)})
     200 {:id nil}))

(defn set-reference
  [client server-id reference]
    (l/call client {:method "PUT"
             :resource (str api-path "/servers/" server-id)
             :body {:reference reference}}))


(defn suggested-raid-configuration
  [server-description]
  (let [disks-conf (get-in server-description [:specs :hdd 0] nil)]
          (let [disk-nb (:amount disks-conf)]
            {:number-disks disk-nb
             :raid-level (cond
                            (= 2 disk-nb) 1
                            (= 3 disk-nb) 5
                            (= 4 disk-nb) 5
                            (> disk-nb 4) 6
                            :else nil)})))

(defn power-status
    [client server-id]
    (:pdu (l/validate
        (l/call client {:method "GET"
            :resource (str api-path "/" server-id "/powerInfo")})
            200 {:pdu nil})))

(defn ips
  [client server-id]
  (:ips (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/ips")})
               200 {:ips nil})))

(defn get-os-user
    [client server-id user]
    (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/credentials/OPERATING_SYSTEM/" user)})
               200))

(defn get-init-root-password
  "retrieve initial rootpassword"
  [client server]
  (when-let [user (get-os-user client server "root")]
    (:password user)))


(defn reboot
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/powerCycle")})
                202))

(defn open-interfaces
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/networkInterfaces/open")})
               2004))

(defn close-interfaces
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/networkInterfaces/close")})
                204 ))

(defn list-jobs
    [client server & {:keys [offset limit] :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource (str api-path "/servers/" server "/jobs")
                      :query-params (into {} (remove (comp nil? second) query-params))})
       200 {:servers nil :_metadata {:error true}}))

(defn list-all-jobs
    [client & {:keys [batch-size] :or {batch-size 50}}]
    (let [jobs (loop [js []
                        offset 0]
          (let [js-batch (leaseweb.v2.server/list-jobs client :limit batch-size :offset offset)]
            (if (= (count (:jobs js-batch)) batch-size)
                (recur (concat js (:jobs js-batch)) (+ offset batch-size))
                (concat js (:jobs js-batch)))))]
        {:jobs jobs :_metadata {:totalCount (count jobs) :offset 0}}))

(defn bandwidth-usage
  [client server-id & {:keys [from to granularity aggregation] :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource (str api-path "/servers/" server-id "/metrics/bandwidth")
                      :query-params (into {} (remove (comp nil? second) query-params))}) 200))

(defn datatraffic-usage
  [client server-id & {:keys [from to granularity aggregation] :as query-params}]
    (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/servers/" server-id "/metrics/datatraffic")
               :query-params (into {} (remove (comp nil? second) query-params))}) 200))

;; to be updated (need to update also client)

(defn install
  "Install a serveur"
  [client server-id os-id hdd raid-level number-disks]
  (l/validate
      (let [res (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/install" )
               :body  {:osId os-id
                       :hdd hdd
                       :raidLevel raid-level
                       :numberDisks number-disks}})]
            (if (not (= 404 (:status res)))
              res
              (do
                (log/error "[LSW]" (:body res))
                res))) 200 "error"))

(defn install-status
  [client server-id]
  (:installationStatus (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/installationStatus")})
               200 {:installationStatus
                            {
                              :code 404
                              :description "unknown"
                              :serverPackId "unknown"
                              :serverName "unknown" }})))
