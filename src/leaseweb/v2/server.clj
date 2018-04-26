(ns leaseweb.v2.server
  (:import [java.util Base64 Base64$Encoder])
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.v2.core :as l]))

(def api-path "/bareMetals/v2/servers")

;;updated

(defn list
  "[TESTED] Return a batch of servers"
  [client & {:keys [offset limit ip macAddress reference site] :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource api-path
                      :query-params (into {} (remove (comp nil? second) query-params))})
       200 {:servers nil :_metadata {:error true}}))

(defn list-all
    "[TESTED] Return all servers"
    [client & {:keys [batch-size] :or {batch-size 50}}]
    (let [servers (loop [ss []
                        offset 0]
          (let [ss-batch (leaseweb.v2.server/list client :limit batch-size :offset offset)]
            (if-not (nil? (:servers ss-batch))
                (if (= (count (:servers ss-batch)) batch-size)
                    (recur (concat ss (:servers ss-batch)) (+ offset batch-size))
                    (concat ss (:servers ss-batch)))
                nil)))]
        {:servers servers :_metadata {:totalCount (count servers) :offset 0}}))

(defn describe
  "[TESTED] get server description"
  [client server-id]
    (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id)})
     200 {:id nil}))

(defn set-reference
  "[TESTED] set server reference"
  [client server-id reference]
    (l/call client {:method "PUT"
             :resource (str api-path "/" server-id)
             :body {:reference reference}}))


(defn suggested-raid-configuration
  "[TESTED] return a raid configuration suggestion"
  [server-description]
  (let [disks-conf (first (get-in server-description [:specs :hdd] nil))]
          (let [disk-nb (:amount disks-conf)]
            {:number-disks disk-nb
             :raid-level (cond
                            (= 2 disk-nb) 1
                            (= 3 disk-nb) 5
                            (= 4 disk-nb) 5
                            (> disk-nb 4) 6
                            :else nil)})))

(defn power-status
    "[TESTED] power status of a server"
    [client server-id]
    (:pdu (l/validate
        (l/call client {:method "GET"
            :resource (str api-path "/" server-id "/powerInfo")})
            200 {:pdu nil})))

(defn ips
  "[TESTED] return server ip"
  [client server-id]
  (:ips (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/ips")})
               200 {:ips nil})))

(defn get-os-user
    "[TESTED]"
    [client server-id user]
    (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/credentials/OPERATING_SYSTEM/" user)})
               200))

(defn get-init-root-password
  "[TESTED] retrieve initial rootpassword"
  [client server]
  (when-let [user (get-os-user client server "root")]
    (:password user)))


(defn reboot
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/powerCycle")
               :body {}})
                202))

(defn open-interfaces
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/networkInterfaces/open")})
               204))

(defn close-interfaces
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/networkInterfaces/close")})
                204))

(defn list-jobs
    "Return a batch of jobs"
    [client server & {:keys [offset limit] :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource (str api-path "/" server "/jobs")
                      :query-params (into {} (remove (comp nil? second) query-params))})
       200 {:servers nil :_metadata {:error true}}))

(defn describe-job
    "[TESTED] describe a single job identified by uuid"
    [client server job-uuid]
    (l/validate
        (l/call client {:method "GET"
                        :resource (str api-path "/" server "/jobs/" job-uuid)}) 200))

(defn list-all-jobs
    "[TESTED] list all job of server"
    [client server & {:keys [batch-size] :or {batch-size 50}}]
    (let [jobs (loop [js []
                        offset 0]
          (let [js-batch (leaseweb.v2.server/list-jobs client server :limit batch-size :offset offset)]
            (if-not (nil? (:jobs js-batch))
                (if (= (count (:jobs js-batch)) batch-size)
                    (recur (concat js (:jobs js-batch)) (+ offset batch-size))
                    (concat js (:jobs js-batch)))
                nil)))]
        {:jobs jobs :_metadata {:totalCount (count jobs) :offset 0}}))

(defn bandwidth-usage
  "[TESTED] From and to are mandatory"
  [client server-id & {:keys [from to granularity aggregation] :or {aggregation "AVG"} :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource (str api-path "/" server-id "/metrics/bandwidth")
                      :query-params (into {} (remove (comp nil? second) query-params))}) 200))

(defn datatraffic-usage
  "[TESTED] From and to are mandatory"
  [client server-id & {:keys [from to granularity aggregation] :or {aggregation "SUM"} :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource (str api-path "/" server-id "/metrics/datatraffic")
                      :query-params (into {} (remove (comp nil? second) query-params))}) 200))

(defn mk-post-install-script
    "[TESTED] Helper to build postinstall script"
    [commands]
    (let [^Base64$Encoder encoder (Base64/getEncoder)
          commands-string (str "#!/bin/bash\n"
                               (str/join ";" commands))]
            (.encodeToString encoder (.getBytes commands-string))))

(defn install
  "Install a serveur"
  [client server-id os-id hdd raid-level number-disks raid-type ssh-keys post-install-script]
  (l/validate
      (let [res (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/install")
               :body  {:operatingSystemId os-id
                       :sshKeys ssh-keys
                       :partitions hdd
                       :raid {:level raid-level
                              :numberOfDisks number-disks
                              :type raid-type}
                       :postInstallScript post-install-script}})]
            (if (not (= 404 (:status res)))
              res
              (do
                (log/error "[LSW]" (:body res))
                res))) 202 "error"))
