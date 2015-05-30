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
hosts that were defined in the system first. Since we use auto-scaling
at [linkfluence](http://linkfluence.com) this feature of Riemann was
a must-have.

Riemann has many input and output plugins, allowing a rich ecosystem to
thrive on your collected data. As an example, we use Graphite interop
to collect application metrics, process them in Riemann and send them
to Graphite for visualization.

Although there are a lot of clients for the Riemann protocol, we didn't
find an already existing piece of software to act as an autonomous agent
that would gather and then send metrics and statuses to Riemann.

That is why I started working on **Cauchy**.

**Cauchy** is a Riemann system agent with batteries included. Once
deployed on your servers, it will provide a better monitoring than
the usual solutions. It allows inventory-less, decentralized processing
of your statuses and metrics (for both system and application
monitoring). It is extensible with community-provided or custom-built
plugins (written in clojure !) to monitor specific application data.

These extensions can benefit from the advanced capabilities of Clojure
whereas with classic monitoring plugins we're often hindered by the
limitations of bash scripting.

## Jobs

**Cauchy** is basically a job scheduler, in which jobs are the periodic
checks that you'd like configure on your host. Jobs are clojure
functions.

Jobs can be :
* built-in into cauchy,
* inline-defined functions in the configuration,
* defined in some extra JARs provided by the community (see examples of
  extra cauchy-jobs below)
* or defined in a JAR or class provided by your own means.

Jobs can be scheduled with intervals way lower than a minute, and Cauchy
will spread the load randomly to prevent swarming your Riemann server with
events.

### Built-in jobs

**Cauchy** provides, out of the box, the usual checks (jobs) that you
expect from a server monitoring solution.

* CPU load (1 minute, 5 minutes, 15 minutes, and relative to number of CPUs)
* Memory usage
* Swap Usage
* Disk usage for all mountpoints
* Disk IO throughput
* Network IO throughput
* Check for process existence

They live in the "health.clj" namespace. Please suggest or code
the jobs you need !

### Extra jobs

Additionally, I wrote a few extra jars providing additional checks
for some applications that we use.

* [cauchy-jobs-elasticsearch](https://github.com/pguillebert/cauchy-jobs-elasticsearch)
* [cauchy-jobs-kestrel](https://github.com/pguillebert/cauchy-jobs-kestrel)

Please have a look at them, or at the provided "health.clj" namespace
if you intend to write your own jobs. Also, if you defined a plugin
that can be useful to others, I'll add you here.

## Configuration profiles

The system is based on Trapperkeeper's configuration facilities. We have
several profiles for different types of servers, and only profiles selected
in the `:profiles` key of the configuration will be activated.

There is a minimal configuration example in the `conf/` directory.

#### Job definition

If you want to create your own jobs, just write a clojure function.

The only contract cauchy needs is that a job may return :
* either a Riemann *event* map,
* or a seq of *event* maps if returning more than one Riemann event
for the service.

An event map can have the following keys :

     :state       ;; Optional, Can be skipped if sending a raw metric
     :metric      ;; Optional, default : not sent
     :description ;; Optional, default : not sent
     :ttl         ;; Optional, default : (* 2 interval)
     :service     ;; Optional, default : (:service job)
     :host        ;; Optional, default : hostname
     :tags        ;; Optional, default : not sent

This return map is merged with the `:defaults` key from the
main configuration file before being sent to the Riemann server.

For example if your service defines no tags but your main conf has :

      :defaults {:tags ["devel" "appfoo"]}

events for this agent will have these tags set.

## Startup

Cauchy is a trapperkeeper application and benefits from
trapperkeeper's configuration system to easily load templates:

      /usr/bin/java -Xmx256m -cp /opt/cauchy/jar/cauchy/lib/cauchy-jobs-kestrel-0.1.0-standalone.jar:/opt/cauchy/jar/cauchy/cauchy-0.1.0-standalone.jar puppetlabs.trapperkeeper.main --config /opt/cauchy/conf/cauchy.edn,/opt/cauchy/conf/cauchy-profiles

Here we add 2 additional jars to the classpath, and load the main
configuration file plus a profiles-directory containing many profiles.
Profiles selected in the main conf will be activated.

## Thanks

Cauchy wouldn't exist without these great libraries :

* Riemann, by Kyle Kingsbury
* trapperkeeper, by Puppet labs
* Chime, by James Anderson
* Sigmund, by Chris Zheng and good old Hyperic Sigar.

## Ideas for the future

* Web server to show current (local) state of the agent.
* A route or signal to reload config.
* Buffer events when they cannot be sent to riemann immediately.

## Origin of the name Cauchy

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

Copyright © 2014, 2015 Philippe Guillebert

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
