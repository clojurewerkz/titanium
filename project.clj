(defproject clojurewerkz/titanium "1.0.0-alpha3-SNAPSHOT"
  :description "Titanium a powerful Clojure graph library build on top of Aurelius Titan"
  :url "http://titanium.clojurewerkz.org"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure                "1.4.0"]
                 [clojurewerkz/support               "0.13.0"]
                 [com.thinkaurelius.titan/titan      "0.2.0"]
                 [com.tinkerpop.gremlin/gremlin-java "2.2.0"]]
  :source-paths  ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :profiles {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-RC17"]]}
             :dev {:resource-paths ["test/resources"]
                   :plugins [[codox "0.6.1"]]
                   :codox {:sources ["src/clojure"]
                           :output-dir "doc/api"}}}
  :aliases {"all" ["with-profile" "dev:dev,1.3:dev,1.5"]}
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}}
  :warn-on-reflection true)
