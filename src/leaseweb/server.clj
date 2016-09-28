(ns leaseweb.server
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [leaseweb.core :as l]))

(def api-path "/bareMetals")

(defn list
  []
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource api-path})
      {:status 403}) 200 {} ))

(defn describe
  [server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "GET"
               :resource (str api-path "/" server-id)})
      {:status 403}) 200 ))

(defn reboot
  [server-id]
  (l/validate
    (if (l/initialized?)
      (l/call {:method "POST"
               :resource (str api-path "/" server-id "/powerCycle")})
      {:status 403}) 202 ))
