(defproject cauchy "0.1.1"
  :description "Cauchy is an agent for Riemann"
  :url "https://github.com/pguillebert/cauchy"
  :scm {:name "git"
        :url "https://github.com/pguillebert/cauchy"}
  :pom-addition [:developers
                 [:developer
                  [:name "Philippe Guillebert"]
                  [:url "https://github.com/pguillebert"]
                  [:timezone "+1"]]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :signing {:gpg-key "93FEB8D7"}
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/log4j-over-slf4j "1.7.12"]
                 [riemann-clojure-client "0.3.2"]
                 [jarohen/chime "0.1.6"]
                 [puppetlabs/trapperkeeper "1.1.1"]
                 [indigenous "0.1.0"]
                 [bultitude "0.2.6"]
                 [sigmund "0.1.1" :exclusions [log4j sigar/sigar-native-deps]]
                 [stask/sigar-native-deps "1.6.4"]]
  :aot [puppetlabs.trapperkeeper.main]
  :main puppetlabs.trapperkeeper.main)
