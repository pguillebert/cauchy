(ns cauchy.jobs.utils)

(defn threshold
  [{:keys [warn crit comp] :as conf} metric]
  (cond
   (comp metric crit) "critical"
   (comp metric warn) "warn"
   :else "ok"))
