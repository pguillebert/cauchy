(ns cauchy.scheduler
  (:require [clojure.tools.logging :as log]
            [chime :as chime]
            [clj-time.core :as time]
            [clj-time.periodic :as periodic]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))

(defn- check-call
  [{:keys [label active interval job-fn] :as job}]
  (when-not label
    (throw (IllegalArgumentException. ":label key must be set")))
  (when-not (string? label)
    (throw (IllegalArgumentException. ":label key must be a String")))
  (when active
    (when-not label
      (throw (IllegalArgumentException. ":label key must be set")))
    (when-not (string? label)
      (throw (IllegalArgumentException. ":label key must be a String")))
    (when-not interval
      (throw (IllegalArgumentException. ":interval key must be set")))
    (when-not (integer? interval)
      (throw (IllegalArgumentException. ":interval key must be an integer")))
    (when-not job-fn
      (throw (IllegalArgumentException. ":job-fn key must be set")))
    (when-not (fn? job-fn)
      (throw (IllegalArgumentException. ":job-fn key must be a function"))))
  true)

(defn do-schedule
  [{:keys [job-registry fuzziness] :as ctx}
   {:keys [label active interval job-fn] :as job}]
  (when (check-call job)
    (when-let [stopfn (get @job-registry label)]
      ;; stop scheduling
      (stopfn)
      ;; del feed from registry
      (swap! job-registry dissoc label)
      (log/info "deleted job" label))

    (when active
      (let [fuzz (+ 2 (rand-int (* interval fuzziness)))
            startdate (time/plus (time/now)
                                 (time/seconds fuzz))
            ev-seq (periodic/periodic-seq
                    startdate
                    (time/seconds interval))
            stopfn (chime/chime-at ev-seq (fn [time] (job-fn)))]
        ;; add feed to registry
        (swap! job-registry assoc label stopfn)
        (log/info "added job" label)))))

(defn clear-scheduler
  [{:keys [job-registry fuzziness] :as ctx}]
  (doseq [[label stopfn] @job-registry]
    (stopfn))
  (reset! job-registry {}))

;;;;;;;;;;;;;;;;;
;;;; Service ;;;;
;;;;;;;;;;;;;;;;;

;; A protocol that defines what functions our service will provide
(defprotocol SchedulerService
  (schedule [this job])
  (clear [this]))

(defservice scheduler-service
  SchedulerService
  ;; dependencies
  [[:ConfigService get-in-config]]
  ;; Lifecycle functions that we implement
  (init [this context]
        (let [conf (get-in-config [:scheduler])]
          (log/info "Scheduler Service initializing with conf" conf)
          (assoc context
            :job-registry (atom {})
            :fuzziness (:fuzziness conf 0.5))))
  ;; implement our protocol functions
  (schedule [this job]
            (do-schedule (service-context this) job))
  (clear [this]
         (clear-scheduler (service-context this))))
