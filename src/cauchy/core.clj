(ns cauchy.core
  (:require [clojure.tools.logging :as log]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))


(defn get-hostname
  []
  (.. java.net.InetAddress getLocalHost getHostName))

(defn format-output
  [global job out-map]
  (let [ttl (* 2 (:interval job)) ;; "TTL for events", :type => Integer, default interval * 2
        service (:label job) ;; job label
        ;; event {:state (:state out-map) ;; returned by job
        ;;        :metric (:metric out-map) ;; returned by job (optional)
        ;;        :description (:description out-map) ;; returned by job (optional)
        ;;        :host nil ;; will be overwritten by global config during merge
        ;;        :tags (:tags out-map) ;; "Tag to add to events", :type => String, :multi => true
        ;;        }
        ]
    (->> (merge global {:service service :ttl ttl} out-map)
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
               global (assoc (get-in-config [:global])
                        :host (get-hostname))]
           (log/info "Cauchy Service start with jobs" jobs)

           (doall
            (->> jobs
                 (map (fn [[label {:keys [type interval job-fn args] :as job}]]
                        (log/info "Scheduling job" job)
                        (let [active (get job :active true)
                              job-fn #(->> (apply job-fn args)
                                           (format-output global (assoc job :label label))
                                           (send!))]
                          {:label label
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
