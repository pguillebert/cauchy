# Cauchy

> Cauchy is Riemann's colleague

## Rationale

[Riemann](http://riemann.io) is a powerful event processing engine, in
which rules are written in [Clojure](http://clojure.org).
[Riemann Events](http://riemann.io/concepts.html) are data structures
conveying statuses, metrics or logs sent to the server by
[Riemann Clients](http://riemann.io/clients.html).

Riemann is very interesting in a cloud environment because it can be
used to monitor systems without having to store and update an inventory,
unlike traditional solutions (nagios, zabbix, etc..) which only handle
hosts that were defined in the system first. Since we use instance
auto-scaling this is a must-have.

Riemann had many input and output plugins, allowing a rich ecosystem to
thrive on your collected data. As an example, we use Graphite interop at
[linkfluence](http://linkfluence.com).

Although there are a lot of clients for the Riemann protocol, we didn't
find an already existing piece of software to act as an agent (an
autonomous agent gathers and then sends metrics and statuses to
Riemann). That is why we started working on **Cauchy**.

**Cauchy** is a Riemann system agent with batteries included. Once
deployed on your servers, we hope it'll provide a better monitoring than
the usual solutions. It allows inventory-less, decentralized processing
of your statuses and metrics (for both system and application
monitoring). It is extensible with community-provided or custom plugins
to monitor specific application data, and these extensions can benefit
from the advanced capabilities of Clojure whereas with classic
monitoring plugins we're often hindered by bash scripting.

## Jobs

**Cauchy** is basically a job scheduler, in which jobs are the periodic
checks that you'd like configure on your host. Jobs are clojure
functions.

Jobs can be :
* built-in into cauchy,
* defined inline in the configuration,
* defined in some extra JARs provided by the community (see examples of
  extra cauchy-jobs below)
* or defined in a JAR provided by your own means.

Jobs can be scheduled with intervals way lower than a minute, and Cauchy
spreads the load randomly to prevent swarming your Riemann server with
events.

### Built-ins

**Cauchy** provides, out of the box, the usual checks (jobs) that you
expect from a server monitoring solution.

* CPU load (1 minute, 5 minutes, 15 minutes)
* Memory usage
* Swap Usage
* Disk usage for all mountpoints
* Disk IO throughput
* Network IO throughput
* Check for process existence

### Extra jobs

Additionally, I wrote a few extra jars providing additional checks
to cauchy :

* [cauchy-jobs-elasticsearch](https://github.com/pguillebert/cauchy-jobs-elasticsearch)
* [cauchy-jobs-kestrel](https://github.com/pguillebert/cauchy-jobs-kestrel)

Please have a look at them if you intend to write your own jobs. Please
get in touch if you defined a job that can be useful to others and want
to share.

## Configuration profiles


## Example configuration

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

## Startup

Cauchy is a trapperkeeper application and benefits from trapperkeeper's configuration system to easily load templates:

      /usr/bin/java -Xmx256m -cp /opt/cauchy/jar/cauchy/lib/cauchy-jobs-kestrel-0.1.0-SNAPSHOT-standalone.jar:/opt/cauchy/jar/cauchy/cauchy-0.1.0-SNAPSHOT-standalone.jar puppetlabs.trapperkeeper.main --config /opt/cauchy/conf/cauchy.edn,/opt/cauchy/conf/cauchy-profiles


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
