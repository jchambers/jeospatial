# jeospatial

jeospatial is a simple geospatial index library for Java. It aims to provide an easy-to-use and reasonably-high-performance set of tools for solving the _k_-nearest-neighbor problem on the earth's surface.

Geospatial indices in this library are implemented using [vantage point trees](http://pnylab.com/pny/papers/vptree/main.html) (or vp-trees), which are binary space partitioning data structures that operate on any metric space. Construction of a geospatial index executes in _O(n log(n))_ time and searches against that index execute in _O(log(n))_ time. As a practical point of reference, it takes about a half second to construct a vp-tree-backed index that contains 100,000 geospatial points on a 2012 MacBook Pro, and that index has a search throughput of about 3,000 queries/second. By contrast, putting all of the points in a list, sorting by distance from a query point, and grabbing the first N results has a search throughput of about 0.6 queries/second.

## Major concepts

The two most important interfaces in the jeospatial library are in the `com.eatthepath.jeospatial` package.

The `GeospatialPoint` interface defines a single point on the earth's surface; concrete implementations of the `GeospatialPoint` interface can be found in the `com.eatthepath.jeospatial.util` package.

The `GeospatialIndex` interface defines the behavioral contract for classes that index collections of `GeospatialPoints` and provide facilities for performing nearest-neighbor searches among those points. The `VPTreeGeospatialIndex` class is a concrete implementation of the `GeospatialIndex` interface and can be found in the `com.eatthepath.jeospatial.vptree` package.

For additional details, see the [API documentation](http://jchambers.github.com/jeospatial/javadoc).

## Examples

### Finding nearest neighbors

Let's say we have a list of all of the ZIP codes (postal codes) in the United States and we want to find the ten closest ZIP codes to some point in the world (let's say [Davis Square in Somerville, MA, USA](http://maps.google.com/maps?q=Davis+Square,+Somerville,+MA&hl=en&sll=42.39358,-71.116902&sspn=0.010824,0.017509&oq=Davis+Square,+Somer&t=w&hnear=Davis+Square,+Somerville,+Middlesex,+Massachusetts&z=15)). We might do something like this:

```java
VPTree<ZipCode> index = new VPTree<ZipCode>(zipCodes);

// Pick a query point (Davis Square in Somerville, MA, USA)
final GeospatialPoint davisSquare = new GeospatialPoint() { ... };

// Find the ten nearest zip codes to Davis Square
List<ZipCode> nearestZipCodes = index.getNearestNeighbors(davisSquare, 10);
```

The `nearestZipCodes` list will have ten elements; the first will be the closest zip code to Davis Square, the second will be the second closest, and so on.

### Finding all neighbors within a certain radius

Assuming we have the same set of zip codes, we might want to find all of the zip codes that are within a fixed distance Davis Square. For example:

```java
// Find all zip codes within ten kilometers of Davis Square
final List<ZipCode> zipCodesWithinRange =
    index.getAllNeighborsWithinDistance(davisSquare, 10 * 1000);
```

The `zipCodesWithinRange` list will contain all of the zip codes—sorted in order of increasing distance from Davis Square—that are within ten kilometers of Davis Square.

### Finding points inside a bounding box

If you're working with a section of a map with a cylindrical projection (e.g. a Google or Bing map), you might want to find all of the zip codes that are visible in that section of the map. A set of bounding box search methods comes in handy here:

```java
// Find all of the zip codes in a bounding "box"
final List<ZipCode> inBoundingBox =
    index.getAllPointsInBoundingBox(-75, -70, 43, 42);
```

As might expected, the `inBoundingBox` list contains all of the zip codes that fall between the longitude lines of -75 and -70 degrees and the latitude lines of 42 and 43 degrees. Other variants of the `getAllPointsInBoundingBox` method allow for sorting the results by proximity to some point and applying additional search criteria.

## License

jeospatial is an open-source project provided under the [BSD License](http://www.opensource.org/licenses/bsd-license.php).
