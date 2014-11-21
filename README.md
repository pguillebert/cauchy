# Cauchy

> Cauchy is Riemann's colleague

Riemann is a powerful event processing engine, in which rules are written in Clojure.
Clients send events (statuses, metrics, logs, you name it) to Riemann.


Cauchy is a simple Riemann client deployed on your servers,
intended to provide a better monitoring than the usual solutions (like Nagios).


Cauchy is basically a job scheduler, in which jobs
are the checks that you'd configure on your host.


Jobs can be scheduled with intervals lower than a minute,
and Cauchy spreads the load randomly to prevent swarming your Riemann server.


Jobs can be written in pure clojure using a plugin system,
or can run good old NRPE checks in a shell environment.


Then, Cauchy sends over the job results to your Riemann server.

## Job types

### clojure jobs

#### Example configuration

        {:service "Service1 check"
         :interval 12 :type :clj
         :job-fn (fn [] )}

#### Job return value

A job must return a map with the following keys :

     :state       ;; Mandatory (?)
     :metric      ;; Optional, default : not sent
     :description ;; Optional, default : not sent
     :ttl         ;; Optional, default : (* 2 interval)
     :service     ;; Optional, default : (:service job)
     :host        ;; Optional, default : hostname
     :tags        ;; Optional, default : not sent

This return map is merged with the :defaults from the
configuration file before being sent to the Riemann server.

### NRPE jobs (TODO)

#### features

sudo user, command, Perf data and status parser

## Full Configuration Example

    {:riemann { config serveur commune }e
     :default
     :jobs [{:service "Service 1"
             :interval 12 :type :clj
             :job-fn (fn [] )}

            {:service "Service 2"
             :interval 23 :type :clj
             :job-fn 'cauchy.jobs.extra.myfunction
             :args ["arg1" "arg2" 3]}

            {:service "NRPE check 3"
             :interval 34 :type :nrpe
             :sudoer "root"
             :cmd "/rtgi/nagios-probes/check_something" }
            ]}

## Ideas for the future

* Web server to show current (local) state of the server.
* route to reload config (ou signal?)
* buffer events when they cannot be sent to riemann

## License

Copyright © 2014 Philippe Guillebert

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
