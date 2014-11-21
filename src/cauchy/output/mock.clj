(ns cauchy.output.mock
  (:require [clojure.tools.logging :as log]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))

;; A protocol that defines what functions our service will provide
(defprotocol SenderService
  (send! [this msg]))

(defservice sender-service
  SenderService
  ;; dependencies
  [[:ConfigService get-in-config]]
  ;; Lifecycle functions that we implement
  (init [this context]
        (let [conf (get-in-config [:riemann])]
          (log/info "Mockup Output Service initializing with conf" conf)
          {} ;; context map
          ))
  ;; implement our protocol functions
  (send! [this msg]
        (log/info "MOCK" msg)))
