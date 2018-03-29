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


;;not yet updated

(defn suggested-raid-configuration
  [client server-description]
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
  [client server-id]
  (:ips (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/ips")})
               200 {:ips []})))

(defn power-status
  [client server-id]
  (:powerStatus (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/powerStatus")})
               200 {:powerStatus nil})))

(defn open-switchport
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/switchPort/open")})
               200 ))

(defn close-switchport
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/switchPort/close")})
                200 ))

(defn network-usage
  [client server-id date-from date-to]
   (l/validate
      (l/call client {:method "GET"
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
    200 {:bandwidth nil :datatraffic nil}))

(defn bandwidth-usage
  [client server-id date-from date-to]
  (:bandwidth (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/networkUsage/bandwidth?" )
               :body (if-not (and (nil? date-from) (nil? date-to))
                        (let [body (if-not (nil? date-from)
                                      {:dateFrom date-from}
                                      {})
                              body* (if-not (nil? date-to)
                                            (assoc body :dateTo date-to)
                                            body)]
                          body*)
                        nil)})
                        200 {:bandwidth nil})))

(defn datatraffic-usage
  [client server-id date-from date-to]
  (:datatraffic (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/networkUsage/datatraffic?" )
               :body (if-not (and (nil? date-from) (nil? date-to))
                        (let [body (if-not (nil? date-from)
                                      {:dateFrom date-from}
                                      {})
                              body* (if-not (nil? date-to)
                                            (assoc body :dateTo date-to)
                                            body)]
                          body*)
                        nil)})
                        200 {:datatraffic nil})))

(defn mk-partition-scheme
  "helper to build partition scheme object"
  [])

(defn get-init-root-password
  "retrieve initial rootpassword"
  [client server-id]
  (:rootPassword
  (l/validate
      (l/call client {:method "GET"
               :resource (str api-path "/" server-id "/rootPassword")})
               200 {:rootPassword nil})))



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
                res)))
                200 "error"))

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

(defn reboot
  [client server-id]
  (l/validate
      (l/call client {:method "POST"
               :resource (str api-path "/" server-id "/powerCycle")})
                202 ))
