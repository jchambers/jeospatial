# jeospatial

jeospatial is a simple geospatial point database library for Java. It aims to provide an easy-to-use and reasonably-high-performance set of tools for solving the _k_-nearest-neighbor problem on the earth's surface.

Geospatial point databases in this library are implemented using [vantage point trees](http://pnylab.com/pny/papers/vptree/main.html) (or vp-trees), which are a data structure that performs binary space partitioning on a metric space. Construction of a geospatial point database executes in _O(n log(n))_ time and searches against that database execute in _O(log(n))_ time. As a practical point of reference, it takes about a second and a half to construct a vp-tree that contains roughly 30,000 geospatial points on a 2007 MacBook Pro, and about two seconds to execute 10,000 searches against that tree (for a search throughput of about 5,000 searches/second). By contrast, it takes between 300 and 400 milliseconds to sort a list of those 30,000 points by distance from a query point (for a search throughput of 2-3 searches/second).

## Major concepts

The two most important interfaces in the jeospatial library are in the `com.eatthepath.jeospatial` package.

The `GeospatialPoint` interface defines a single point on the earth's surface; concrete implementations of the `GeospatialPoint` interface can be found in the `com.eatthepath.jeospatial.util` package.

The `GeospatialPointDatabase` interface defines the behavioral contract for classes that index collections of `GeospatialPoints` and provide facilities for performing nearest-neighbor searches among those points. The `VPTree` and `LockingVPTree` classes are both concrete implementations of the `GeospatialPointDatabase` class and can be found in the `com.eatthepath.jeospatial.vptree` package.

For additional details, see the [API documentation](http://jchambers.github.com/jeospatial/javadoc).

## Examples

### Finding nearest neighbors

Let's say we have a list of all of the zip codes in the United States and we want to find the ten closest zip codes to some point in the world (let's say [Davis Square in Somerville, MA, USA](http://maps.google.com/maps?q=Davis+Square,+Somerville,+MA&hl=en&sll=42.39358,-71.116902&sspn=0.010824,0.017509&oq=Davis+Square,+Somer&t=w&hnear=Davis+Square,+Somerville,+Middlesex,+Massachusetts&z=15)). We might do something like this:

```java
// Load a bunch of zip codes from a file and construct a vp-tree from
// those points
List<ZipCode> zipCodes = ZipCode.loadAllFromCsvFile();
VPTree<ZipCode> pointDatabase = new VPTree<ZipCode>(zipCodes);

// Pick a query point (Davis Square in Somerville, MA, USA)
SimpleGeospatialPoint davisSquare = new SimpleGeospatialPoint(42.396745, -71.122479);

// Find the ten nearest zip codes to Davis Square
List<ZipCode> nearestZipCodes = pointDatabase.getNearestNeighbors(davisSquare, 10);
```

The `nearestZipCodes` list will have ten elements; the first will be the closest zip code to Davis Square, the second will be the second closest, and so on.

### Finding all neighbors within a certain radius

Assuming we have the same set of zip codes, we might want to find all of the zip codes that are within a fixed distance Davis Square. For example:

```java
// Find all zip codes within ten kilometers of Davis Square
List<ZipCode> zipCodesWithinRange = database.getAllNeighborsWithinDistance(davisSquare, 10 * 1000);
```

The `zipCodesWithinRange` list will contain all of the zip codes&mdash;sorted in order of increasing distance from Davis Square&mdash;that are within ten kilometers of Davis Square.

### Finding neighbors by other search criteria

Assuming we still have the now-familiar list of zip codes, we might want to find the closest zip codes to Davis Square that are outside of the state of Massachusetts. We could do that using the `SearchCriteria` class:

```java
// Specify search criteria that matches anything outside of the state of
// Massachusetts
SearchCriteria<ZipCode> searchCriteria = new SearchCriteria<ZipCode>() {
    @Override
    public boolean matches(ZipCode zipCode) {
        return !zipCode.getState().equals("MA");
    }
};

// Find the ten closest zip codes to Davis Square that are outside of
// Massachusetts
List<ZipCode> closestOutsideMA = database.getNearestNeighbors(davisSquare, 10, searchCriteria);
```

The `closestOutsideMA` list will contain the ten closest zip codes to Davis Square sorted in order of increasing distance.

### Finding points inside a bounding box

If you're working with a section of a map with a cylindrical projection (e.g. a Google or Bing map), you might want to find all of the zip codes that are visible in that section of the map. A set of bounding box search methods comes in handy here:

```java
// Find all of the zip codes in a bounding "box"
List<ZipCode> inBoundingBox = database.getAllPointsInBoundingBox(-75, -70, 43, 42);
```

As might expected, the `inBoundingBox` list contains all of the zip codes that fall between the longitude lines of -75 and -70 degrees and the latitude lines of 42 and 43 degrees. Other variants of the `getAllPointsInBoundingBox` method allow for sorting the results by proximity to some point and applying additional search criteria.

## Memory usage

jeospatial tries to keep memory overhead to a reasonable level by using sensible internal collection types and by aggressively trimming internal point lists. Still, any spatial index will necessarily introduce some memory overhead, and it's important&mdash;especially when dealing with large datasets&mdash;to understand how data structures and design decisions affect the memory usage of an application.

A detailed exploration of jeospatial's memory usage will be available soon, but the short version is that the overhead imposed by a `VPTree` is a function of the node capacity (set at construction time) of the tree. The per-point overhead cost of using a `VPTree` is roughly as follows for non-trivial datasets:

<table>
	<thead>
		<tr><th>Node capacity</th><th>Overhead per point</th></tr>
	</thead>
	<tbody>
		<tr><td>1</td><td>112.0&nbsp;bytes</td></tr>
		<tr><td>2</td><td>56.0&nbsp;bytes</td></tr>
		<tr><td>4</td><td>30.0&nbsp;bytes</td></tr>
		<tr><td>8</td><td>17.0&nbsp;bytes</td></tr>
		<tr><td>16</td><td>10.5&nbsp;bytes</td></tr>
		<tr><td>32</td><td>7.3&nbsp;bytes</td></tr>
		<tr><td>64</td><td>5.6&nbsp;bytes</td></tr>
		<tr><td>128</td><td>4.8&nbsp;bytes</td></tr>
		<tr><td>256</td><td>4.4&nbsp;bytes</td></tr>
	</tbody>
</table>

The default node capacity for a `VPTree` (at the time of this writing) is 32 points per node, so it's reasonable to expect that a default database would impose overhead memory usage somewhere between 7.3 and 10.5&nbsp;bytes per point. A tree containing 1,048,576 (2^20) `SimpleGeospatialPoint`s would take up 24&nbsp;MB for the points themselves and an additional 7.3 to 10.5&nbsp;MB in overhead for a total memory footprint somewhere between 31.3 and 34.5&nbsp;MB.

While it may be tempting to set a high node capacity when working with `VPTree`s to keep memory overhead to a minimum, it's important to remember that larger node capacities carry performance tradeoffs. In addition to a lower memory overhead ratio, a larger node capacity means that a search will have to visit fewer nodes of the tree to return the desired number of points, but will have to examine every point in that node for inclusion in the search results. If your use case calls for frequent searches that return large numbers of points, a larger node capacity is a good idea across the board. Otherwise, carefully consider the tradeoffs between memory usage and processing time for your specific application. As a general rule of thumb, a node capacity somewhere around twice the size of your usual search result size strikes a good balance between processing time and memory usage.

## Acknowledgements

[United States ZIP code data](http://www.census.gov/tiger/tms/gazetteer/zips.txt) used in examples and unit tests comes from [The United States Census Bureau](http://www.census.gov/). Examples and unit tests make use of the [opencsv](http://opencsv.sourceforge.net/) library under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## License

jeospatial is an open-source project provided under the [BSD License](http://www.opensource.org/licenses/bsd-license.php).
