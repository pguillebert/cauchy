(ns cauchy.jobs.utils
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]))

(defn threshold
  [{:keys [warn crit comp] :as conf} metric]
  (cond
   (comp metric crit) "critical"
   (comp metric warn) "warning"
   :else "ok"))

(def rc->state
  {0 :ok
   1 :warn
   2 :critical
   3 :unknown})

(defn exec-nrpe
  [cmd]
  (let [cmdparts (str/split cmd #"\ ")
        {:keys [exit out err]} (apply sh/sh cmdparts)
        status-line (last (str/split out #"\n"))
        [status perfdata] (map str/trim
                               (str/split status-line #"\|"))
        state (rc->state exit :unknown)]

    (mapv #(let [[metric value]
                 (str/split % #"\=")]
             {:state state
              :status-line status
              :service metric
              :value value})
          (str/split perfdata #"\ "))))

(defn worst-state
  [& states]
  (let [s (set states)]
    (cond
     (contains? s "critical") "critical"
     (contains? s "warning") "warning"
     :else "ok")))

(def rate-store (atom {}))

(defn rate
  [store-path curr-val]
  (let [[last-time last-val] (get-in @rate-store store-path [0 0])
        curr-time (.getTime (java.util.Date.))
        rate (/ (- curr-val last-val)
                (- curr-time last-time)
                0.001)]
    (swap! rate-store assoc-in store-path
           [curr-time curr-val])
    rate))
