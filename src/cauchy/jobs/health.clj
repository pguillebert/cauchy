(ns cauchy.jobs.health
  (:require [cauchy.jobs.utils :as utils]
            [clojure.string :as str]
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

(defn memory
  ([{:keys [warn crit] :as conf :or {warn 80 crit 90}}]
     (let [{:keys [total free] :as data} (sig/os-memory)
           used (- total free)
           used-pct (double (* 100 (/ used total)))
           tconf {:comp > :crit crit :warn warn}]

       [{:service "memory_total"
         :metric total}

        {:service "memory_free"
         :metric free}

        {:service "memory_used"
         :metric used}

        {:service "memory_used_pct"
         :metric used-pct
         :state (utils/threshold tconf used-pct)}]))
  ([] (memory {})))

(defn swap
  ([{:keys [warn crit] :as conf :or {warn 80 crit 90}}]
     (let [{:keys [total used] :as data} (sig/os-swap)
           free (- total used)
           used-pct (double (* 100 (/ used total)))
           tconf {:comp > :crit crit :warn warn}]

       [{:service "swap_total"
         :metric total}

        {:service "swap_free"
         :metric free}

        {:service "swap_used"
         :metric used}

        {:service "swap_used_pct"
         :metric used-pct
         :state (utils/threshold tconf used-pct)}]))
  ([] (swap {})))


(defn disk-entry
  [{:keys [warn crit] :as conf :or {warn 80 crit 90}}
   {:keys [dir-name dev-name] :as device}]
  (let [{:keys [total free]} (sig/fs-usage dir-name)
        total (bit-shift-left total 10) ;; kB
        free (bit-shift-left free 10) ;; kB
        used  (- total free)
        used-pct (double (* 100 (/ used total)))
        tconf {:comp > :crit crit :warn warn}
        sname (str "disk" (str/replace dir-name #"\/" "_"))]
    [{:service (str sname "_total")
      :metric total}

     {:service (str sname "_free")
      :metric free}

     {:service (str sname "_used")
      :metric used}

     {:service (str sname "_used_pct")
      :metric used-pct
      :state (utils/threshold tconf used-pct)}]))

(defn disk
  ([tconf]
     (->> (sig/fs-devices)
          (remove (fn [{:keys [dev-name] :as entry}]
                    (.startsWith dev-name "devfs")))
          (map #(disk-entry tconf %))
          (flatten)))
  ([]
     (disk {})))
