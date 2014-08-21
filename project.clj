(defproject clojurewerkz/titanium "1.0.0-beta2-SNAPSHOT"
  :description "Titanium a powerful Clojure graph library build on top of Aurelius Titan"
  :url "http://titanium.clojurewerkz.org"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure                "1.5.1"]
                 [com.thinkaurelius.titan/titan-core "0.5.0"]
                 [potemkin "0.3.8"]
                 [clojurewerkz/archimedes "1.0.0-alpha6-SNAPSHOT"]]
  :source-paths  ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :master {:dependencies [[org.clojure/clojure "1.7.0-master-SNAPSHOT"]]}
             :dev {:dependencies [[com.thinkaurelius.titan/titan-cassandra "0.5.0" :exclusions [org.slf4j/slf4j-log4j12]]
                                  [com.thinkaurelius.titan/titan-berkeleyje "0.5.0"]
                                  [com.thinkaurelius.titan/titan-es "0.5.0"]
                                  [clojurewerkz/ogre "2.5.0.0-SNAPSHOT"]
                                  [clojurewerkz/support "1.0.0" :exclusions [com.google.guava/guava
                                                                             org.clojure/clojure]]

                                  [org.slf4j/slf4j-nop "1.7.5"]
                                  [commons-io/commons-io "2.4"]]
                   :plugins [[codox "0.6.1"]]
                   :codox {:sources ["src/clojure"]
                           :output-dir "doc/api"}}}
  :aliases {"all" ["with-profile" "dev,1.4:dev,1.6:dev,master"]}
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}
                 "clojars"
                 {:url "http://clojars.org/repo"
                  :snapshots true
                  :releases {:checksum :fail :update :always}}}
  ;;  :warn-on-reflection true
  )
