(ns leaseweb.v2.os
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.v2.core :as l]))

(def api-path-os "/bareMetals/v2/operatingSystems")
(def api-path-cp "/bareMetals/v2/controlPanels")

(defn list
  [client & {:keys [offset limit] :as query-params}]
    (l/validate
      (l/call client {:method "GET"
                      :resource api-path-os
                      :query-params (into {} (remove (comp nil? second) query-params))})
       200))

(defn describe
  [client os-id]
  (l/validate
      (l/call client {:method "GET"
               :resource (str api-path-os "/" os-id)})
       200))

(defn list-control-panels
  [client & {:keys [offset limit operatingSystemId] :as query-params}]
  (l/validate
      (l/call client {:method "GET"
               :resource (str api-path-cp)
               :query-params (into {} (remove (comp nil? second) query-params))})
       200))
