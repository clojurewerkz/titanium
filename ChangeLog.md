## Changes between Titanium 1.0.0-alpha3 and 1.0.0-beta1

* Archimedes and Ogre have been updated as development has continued.
* Titan is now version 0.3.0.
* `delete!` has become `remove!`, `create-group` is now
  `defgroup`, `create-property-key`/`-once` is `defkey`/`-once`, and
  `create-edge-label` is now `deflabel`/`-once`.
* Open now just takes a simple map from strings to strings for
  configuration. 	
* Tests now use embeddeded cassandra for the most part. 	
	
## Changes between Titanium 1.0.0-alpha2 and 1.0.0-alpha3

*Major breaking changes for almost everything*

Titanium has been completely overhauled to depend on
[Archimedes](https://github.com/zmaril/archimedes) and
[Ogre](https://github.com/zmaril/ogre). For more details, see this
[Github issue thread](https://github.com/clojurewerkz/titanium/issues/1)
about the philosophy and efforts behind the switch. Titanium now depends
on Titan 0.2.1, and Gremlin/Blueprints 2.3.0.


## Changes between Titanium 1.0.0-alpha1 and 1.0.0-alpha2

### Transaction Control Functions

`clojurewerkz.titanium.graph/commit-tx!` and `clojurewerkz.titanium.graph/rollback-tx!`
commit and roll back current transaction, respectively. Note that closing a
graph will automatically commit current transaction. Every operation
that modifies the graph will automatically start a transaction if needed.


### clojurewerkz.titanium.graph/get-vertices Now Accepts Keywords For Keys

`clojurewerkz.titanium.graph/get-vertices` now accepts keywords for keys,
like many other functions in Titanium.


## Changes in Titanium 1.0.0-alpha1

Initial release. The following features are reasonably well supported:

 * Essential graph operations (open, close), configuration via Clojure maps
 * Blueprints API operations on vertices and edges
 * Blueprints queries
 * Essential GremlinPipeline operations with a nice Clojure DSL

Which makes Titanium useful for prototype development. Initial documentation
will be written before `1.0.0-alpha1` is announced.
