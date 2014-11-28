(defproject cauchy "0.1.0-SNAPSHOT"
  :description "Cauchy is a client for Riemann"
  :url "https://github.com/pguillebert/cauchy"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.0"]
                 [riemann-clojure-client "0.2.11"]
                 [jarohen/chime "0.1.6"]
                 [puppetlabs/trapperkeeper "1.0.0"]
                 [bultitude "0.2.6"]
                 [sigmund "0.1.1"]
                 [stask/sigar-native-deps "1.6.4"]]
  :aot [puppetlabs.trapperkeeper.main]
  :main puppetlabs.trapperkeeper.main)
