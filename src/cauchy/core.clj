(ns cauchy.core
  (:require [clojure.tools.logging :as log]
            [sigmund.core :as sig]
            [bultitude.core :as bult]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))

(defn format-output*
  [defaults job out-map]
  (let [ttl (* 2 (:interval job))
        service (:service job)]
    (->> (merge defaults {:service service :ttl ttl} out-map)
         (remove (fn [[k v]] (nil? v)))
         (into {}))))

(defn format-output
  [defaults job out]
  (if (sequential? out)
    (map (partial format-output* defaults job) out)
    [(format-output* defaults job out)]))

(defn mk-fun
  [myns func args]
  (if myns
    ;; qualified function
    (let [syms (bult/namespaces-on-classpath :prefix "cauchy.jobs")
          good-ns (first (filter (fn [sym] (= (str sym) myns)) syms))
          _ (require good-ns)
          func (ns-resolve good-ns (symbol func))]
      ;; return a thunk
      (fn [] (apply func args)))
    ;; anonymous function defined in-line
    (let [func (eval func)]
      ;; return a thunk
      (fn [] (apply func args)))))

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
                          :host (sig/net-fqdn))]
           (log/info "Cauchy Service start with jobs" jobs)

            (->> jobs
                 (map (fn [{:keys [service type interval job-ns job-fn args] :as job}]
                        (log/info "Scheduling job" job)
                        (let [active (get job :active true)
                              job-thunk (mk-fun job-ns job-fn args)
                              job-fn #(->> (job-thunk)
                                           (format-output defaults job)
                                           (map send!)
                                           (doall))]
                          {:label service
                           :active active
                           :interval interval
                           :job-fn job-fn})))
                 (map schedule)
                 (doall))

           {} ;; context map
           ))

  (stop [this context]
        (clear))

  ;; implement our protocol functions
  (reload [this]
          (log/error "TODO reload")))
