# Titanium, a Clojure Layer On Top of ThinkAurelius Titan

Titanium is a thin Clojure graph library built on top of [Titan](http://thinkaurelius.github.com/titan/).


## Project Goals

 * Make good parts of Titan easier to use from Clojure
 * Roughly match [Neocons](http://clojureneo4j.info) in terms of functionality
 * Be well documented
 * Be well tested
 * Add additional features and DSL bits for convenience


## Community

[Titanium has a mailing list](https://groups.google.com/forum/#!forum/clojure-titanium). Feel free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz) on Twitter.


## Project Maturity

Titanium is *very* young and incomplete. We put it on GitHub to gather feedback. For now, please consider using
a mature library such as [Neocons](http://clojureneo4j.info) instead.

As the project matures, we will update this section.



## Artifacts

Titanium artifacts are [released to Clojars](https://clojars.org/clojurewerkz/titanium). If you are using Maven, add the following repository
definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

    [clojurewerkz/titanium "1.0.0-alpha1"]


With Maven:

    <dependency>
      <groupId>clojurewerkz</groupId>
      <artifactId>titanium</artifactId>
      <version>1.0.0-alpha1</version>
    </dependency>



## Getting Started

Please refer to our [Getting Started guide](...). Don't hesitate to join our [mailing list](https://groups.google.com/forum/#!forum/clojure-titanium) and ask questions, too!


## Documentation & Examples

Our documentation site is not yet live, sorry.



## Supported Clojure versions

Titanium is built from the ground up for Clojure 1.3.0 and up. The most recent stable release
is always recommended.


## Continuous Integration Status

Before Titan developers push a stable release to one of the popular Maven repositories, it is too early to put Titanium on travis-ci.org.
But after that, we will certainly do it, just like with all other [ClojureWerkz projects](http://clojurewerkz.org).



## Titanium Is a ClojureWerkz Project

Titanium is part of the group of [Clojure libraries known as ClojureWerkz](http://clojurewerkz.org), together with
[Monger](http://clojuremongodb.info), [Welle](http://clojureriak.info), [Langohr](http://clojurerabbitmq.info), [Elastisch](https://clojureelasticsearch.info), [Neocons](http://clojureneo4j.info) and several others.


## Development

titanium uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make sure you have it installed and then run tests against
supported Clojure versions using

    lein2 all test

Then create a branch and make your changes on it. Once you are done with your changes and all tests pass, submit a pull request
on Github.



## License

Copyright (C) 2013 Michael S. Klishin, Alex Petrov.

Double licensed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (the same as Clojure) or the [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
