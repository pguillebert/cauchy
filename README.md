# Cauchy

> Cauchy is Riemann's colleague

[Riemann](http://riemann.io) is a powerful event processing engine,
in which rules are written in [Clojure](http://clojure.org).
[Riemann Clients](http://riemann.io/clients.html) send events
to Riemann server. [Riemann Events](http://riemann.io/concepts.html)
are data structures conveying statuses, metrics, logs collected
on your hosts.


**Cauchy** is a simple Riemann client with batteries included.
Once deployed on your servers, it provides a better monitoring
than the usual solutions, like Nagios. It allows decentralized
processing of your statuses and metrics and you can use
all the advanced capabilities of Clojure.


**Cauchy** is basically a job scheduler, in which jobs
are the periodic checks that you'd like configure on your host.
Jobs can be scheduled with intervals way lower than a minute,
and Cauchy spreads the load randomly to prevent swarming
your Riemann server with events.


**Cauchy** provides the usual jobs out of the box :

* CPU load (1 minute, 5 minutes, 15 minutes)
* Memory usage
* Swap Usage
* Disk usage for all mountpoints

... And more to come.

You can extend Cauchy with jobs written in pure clojure
(using a dynamic plugin system). You can also run good old
NRPE checks in a shell environment.


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

## Origin of the name


<img src="http://upload.wikimedia.org/wikipedia/commons/d/d3/Augustin-Louis_Cauchy_1901.jpg"
title="Cauchy" align="right" height="150px"/>


[Augustin-Louis Cauchy](http://en.wikipedia.org/wiki/Augustin-Louis_Cauchy)
(1789 – 1857) was a french mathematician, contemporary of
[Bernhard Riemann](http://en.wikipedia.org/wiki/Bernhard_Riemann).
They often worked on
[similar areas of mathematics](http://en.wikipedia.org/wiki/Cauchy%E2%80%93Riemann_equations).


<div width="100%">&nbsp; </div>


<div width="100%">&nbsp; </div>


## License

Copyright © 2014 Philippe Guillebert

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
