(ns cauchy.jobs.mock)


(defn example-job-noargs
  []
  {:state "alive" :metric (rand 12)})


(defn example-job-2args
  [a b]
  {:state "ok" :metric (* a 2) :description b})
