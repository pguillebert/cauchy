(ns cauchy.output.riemann
  (:require [riemann.client :as rc]
            [clojure.tools.logging :as log]
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
          (log/info "Riemann Output Service initializing with conf" conf)
          (assoc context :rc (rc/tcp-client conf))))
  ;; implement our protocol functions
  (send! [this msg]
         (rc/send-event (:rc (service-context this)) msg)))
