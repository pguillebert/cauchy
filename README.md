# Cauchy

> Cauchy is Riemann's colleague

[Riemann](http://riemann.io) is a powerful event processing engine,
in which rules are written in [Clojure](http://clojure.org).
[Riemann Events](http://riemann.io/concepts.html) are data
structures conveying statuses, metrics or logs sent to the server
by [Riemann Clients](http://riemann.io/clients.html).


**Cauchy** is a simple Riemann agent with batteries included.
Once deployed on your servers, it provides a better monitoring
than the usual solutions, like Nagios. It allows decentralized
processing of your statuses and metrics and you can use
all the advanced capabilities of Clojure.


**Cauchy** is basically a job scheduler, in which jobs
are the periodic checks that you'd like configure on your host.
Jobs can be scheduled with intervals way lower than a minute,
and Cauchy spreads the load randomly to prevent swarming
your Riemann server with events.


**Cauchy** provides, out of the box, the usual checks (jobs)
that you expect from a server monitoring solution.

* CPU load (1 minute, 5 minutes, 15 minutes)
* Memory usage
* Swap Usage
* Disk usage for all mountpoints
* Disk IO throughput
* Network IO throughput
* Check for process existence

... And more to come.

In addition, you can extend **Cauchy** with jobs written
in pure clojure (using a dynamic plugin system).

Soon, You'll be able to run good old NRPE checks
in a shell environment (Todo)


## Job types

### clojure jobs

#### Example configuration

        {:service "Service1 check"
         :interval 12 :type :clj
         :job-fn (fn [] )}

#### Job return value

A job must return a map, or a seq of maps if returning more than one event, with the following keys :

     :state       ;; Can be skipped if only a metric
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

    {:global {:logging-config "/....logging.xml"}
     :riemann {:host "..."}
     :defaults {:tags ["devel" "myapp"]}
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

* Web server to show current (local) state of the agent.
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
