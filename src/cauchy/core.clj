(ns cauchy.core
  (:require [clojure.tools.logging :as log]
            [bultitude.core :as bult]
            [indigenous.core :as indi]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))

(defn load-sigar-native
  []
  (let [path (case (str (indi/os) "-" (indi/arch))
               "linux-x86"    "native/linux/x86/libsigar-x86-linux.so"
               "linux-x86_64" "native/linux/x86_64/libsigar-amd64-linux.so"
               "mac-x86"      "native/macosx/x86/libsigar-universal-macosx.dylib"
               "mac-x86_64"   "native/macosx/x86_64/libsigar-universal64-macosx.dylib"
               "win-x86"      "native/windows/x86/sigar-x86-winnt.dll"
               "win-x86_64"   "native/windows/x86_64/sigar-amd64-winnt.dll")]
    (indi/load-library "sigar" path)
    ;; We loaded OK. Now fire Sigar initialization.
    (require 'sigmund.core)))

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
    ;; qualified function (by ns+name)
    (let [syms (bult/namespaces-on-classpath :prefix "cauchy.jobs")
          good-ns (first (filter (fn [sym] (= (str sym) myns)) syms))
          _ (require good-ns)
          func (ns-resolve good-ns (symbol func))]
      ;; return a thunk executing func using args
      (fn [] (apply func args)))
    ;; anonymous function defined in-line.
    (fn [] (apply (eval func) args))))

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
         (let [profiles (get-in-config [:profiles])
               jobs (-> (get-in-config [:jobs])
                        (select-keys profiles) (vals) (flatten))
               defaults (assoc (get-in-config [:defaults])
                          :host (.. java.net.InetAddress getLocalHost getHostName))]
           (load-sigar-native)
           (log/info "Cauchy Service start with jobs" jobs)

           (->> jobs
                (map (fn [{:keys [service type interval job-ns job-fn args] :as job}]
                       (log/info "Scheduling job" job)
                       (let [active (get job :active true)
                             job-thunk (mk-fun job-ns job-fn args)
                             job-fn #(try (->> (job-thunk)
                                               (format-output defaults job)
                                               (map send!)
                                               (doall))
                                          (catch Exception e
                                            (log/error e "Job" service "failed")))]
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
