(ns cauchy.core
  (:require [clojure.tools.logging :as log]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))


(defn get-hostname
  []
  (.. java.net.InetAddress getLocalHost getHostName))

(defn format-output
  [defaults job out-map]
  (let [ttl (* 2 (:interval job))
        service (:service job)]
    (->> (merge defaults {:service service :ttl ttl} out-map)
         (remove (fn [[k v]] (nil? v)))
         (into {}))))

;; A protocol that defines what functions our service will provide
(defprotocol CauchyService
  (reload [this]))

(defservice cauchy-service
  CauchyService
  ;; dependencies
  [[:ConfigService get-in-config]
   [:SchedulerService schedule clear]
   [:SenderService send!]]

  ;; Lifecycle functions that we implement
  (start [this context]
         (let [jobs (get-in-config [:jobs])
               defaults (assoc (get-in-config [:defaults])
                        :host (get-hostname))]
           (log/info "Cauchy Service start with jobs" jobs)

           (doall
            (->> jobs
                 (map (fn [{:keys [service type interval job-fn args] :as job}]
                        (log/info "Scheduling job" job)
                        (let [active (get job :active true)
                              job-fn #(->> (apply job-fn args)
                                           (format-output defaults job)
                                           (send!))]
                          {:label service
                           :active active
                           :interval interval
                           :job-fn job-fn})))
                 (map schedule)))
           {} ;; context map
           ))

  (stop [this context]
        (clear))

  ;; implement our protocol functions
  (reload [this]
          (log/error "TODO reload")))
