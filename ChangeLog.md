## Changes between Titanium 1.0.0-alpha2 and 1.0.0-alpha3

No changes yet.


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
