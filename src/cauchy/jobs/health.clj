(ns cauchy.jobs.health
  (:require [cauchy.jobs.utils :as utils]
            [sigmund.core :as sig]))

(defn load-average
  ([{:keys [warn crit] :as conf :or {warn 1 crit 2}}]
     (let [services ["load_1" "load_5" "load_15"]
           metrics (vec (sig/os-load-avg))
           tconf {:comp > :crit crit :warn warn}]
       (map (fn [s m]
              {:service s
               :metric m
               :state (utils/threshold tconf m)})
            services metrics)))
  ([] (load-average {})))
