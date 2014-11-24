(ns cauchy.test
  (:require [puppetlabs.trapperkeeper.core :as tk]
            [puppetlabs.trapperkeeper.app :as tka]
            [cauchy.scheduler :refer [scheduler-service]]
            [cauchy.output.mock :refer [sender-service]]
            [cauchy.core :refer [cauchy-service]]
            [clojure.tools.namespace.repl :refer (refresh)]))

;; a var to hold the main `TrapperkeeperApp` instance.
(def system nil)

(def conf
  {:nrepl {:enabled true}
   :riemann {:server "localhost"}
   :defaults {:tags ["devel" "appfoo"]}
   :jobs [
          {:service "alive" :interval 12
           :type :clj :active true
           :job-fn '(fn [] {:state "ok"})}

          {:service "plop2" :interval 13
           :type :clj
           :args ["arg1" 223]
           :job-fn '(fn [a1 a2] {:state (str a1 "--" a2)})}

          {:service "plop3" :interval 11
           :type :clj
           :job-ns "cauchy.jobs.mock"
           :job-fn "example-job-noargs"}

          {:service "plop4" :interval 14
           :type :clj
           :job-ns "cauchy.jobs.mock"
           :args [223 "good descr"]
           :job-fn "example-job-2args"}

          {:service "plop5" :interval 10
           :type :clj
           :job-ns "cauchy.jobs.health"
           :job-fn "load-average"}
          ]
   })

(defn init []
  (alter-var-root #'system
                  (fn [_] (tk/build-app
                          [cauchy-service
                           scheduler-service
                           sender-service]
                          conf)))
  (alter-var-root #'system tka/init)
  (tka/check-for-errors! system))

(defn start []
  (alter-var-root #'system
                  (fn [s] (if s (tka/start s))))
  (tka/check-for-errors! system))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (tka/stop s)))))

(defn go []
  (init)
  (start))

(defn context []
  @(tka/app-context system))

;; pretty print the entire application context
(defn print-context []
  (clojure.pprint/pprint (context)))

(defn reset []
  (stop)
  (refresh :after 'cauchy.test/go))
