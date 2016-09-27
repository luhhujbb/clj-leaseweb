(ns leaseweb.core)

(def creds (atom {}))
(def initialized (atom false))

(defn initialized?
  []
  @initialized)

(def endpoint "https://api.leaseweb.com/v1")

(def request-conf {:accept :json
                   :as :json
                   :throw-exceptions false})

(defn mk-headers [] {"X-Lsw-Auth" (:token @creds)})
