(defproject clojurewerkz/titanium "1.0.0-beta2-SNAPSHOT"
  :description "Titanium a powerful Clojure graph library build on top of Aurelius Titan"
  :url "http://titanium.clojurewerkz.org"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure                "1.5.1"]
                 [clojurewerkz/support               "0.15.0"]
                 [commons-lang                       "2.5"]
                 [com.thinkaurelius.titan/titan-core "0.4.4" :exclusions [commons-lang]]
                 [potemkin "0.2.0"]
                 [clojurewerkz/archimedes "1.0.0-alpha6-SNAPSHOT"]]
  :source-paths  ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :master {:dependencies [[org.clojure/clojure "1.7.0-master-SNAPSHOT"]]}
             :dev {
                   :dependencies [[com.thinkaurelius.titan/titan-cassandra "0.4.4" :exclusions [commons-lang log4j]]
                                  [com.thinkaurelius.titan/titan-berkeleyje "0.4.4"]
                                  [com.thinkaurelius.titan/titan-es "0.4.4"]
                                  [clojurewerkz/ogre "2.3.0.1"]
                                  [log4j "1.2.17"]]
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
