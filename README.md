# jeospatial

Jeospatial is a simple geospatial point database library for Java. It aims to provide an easy-to-use and reasonably-high-performance set of tools for solving the _k_-nearest-neighbor problem on the earth's surface.

Geospatial point databases in this library are implemented using [vantage point trees](http://pnylab.com/pny/papers/vptree/main.html) (or vp-trees), which are a data structure that performs binary space partitioning on a metric space. Construction of a geospatial point database executes in _O(n log(n))_ time and searches against that database execute in _O(log(n))_ time. As a practical point of reference, it takes about two seconds to construct a vp-tree that contains 30,000 geospatial points on a 2007 MacBook Pro, and about two seconds to execute 10,000 searches against that tree (for a search throughput of about 5,000 searches/second). By contrast, it takes between 300 and 400 milliseconds to sort a list of those 30,000 points by distance from a query point (for a search throughput of 2-3 searches/second).

## Examples

### Finding nearest neighbors

Let's say we have a list of all of the zip codes in the United States and we want to find the ten closest zip codes to some point in the world (let's say [Davis Square in Somerville, MA, USA](http://maps.google.com/maps?q=Davis+Square,+Somerville,+MA&hl=en&sll=42.39358,-71.116902&sspn=0.010824,0.017509&oq=Davis+Square,+Somer&t=w&hnear=Davis+Square,+Somerville,+Middlesex,+Massachusetts&z=15)). We might do something like this:

	// Load a bunch of zip codes from a file and construct a vp-tree from
	// those points
	List<ZipCode> zipCodes = ZipCode.loadAllFromCsvFile();
	VPTree<ZipCode> pointDatabase = new VPTree<ZipCode>(zipCodes);

	// Pick a query point (Davis Square in Somerville, MA, USA)
	SimpleGeospatialPoint davisSquare = new SimpleGeospatialPoint(42.396745, -71.122479);

	// Find the ten nearest zip codes to Davis Square
	List<ZipCode> nearestZipCodes = pointDatabase.getNearestNeighbors(davisSquare, 10);

The `nearestZipCodes` list will have ten elements; the first will be the closest zip code to Davis Square, the second will be the second closest, and so on.

### Finding all neighbors within a certain radius

Assuming we have the same set of zip codes, we might want to find all of the zip codes that are within a fixed distance Davis Square. For example:

	// Find all zip codes within ten kilometers of Davis Square
	List<ZipCode> zipCodesWithinRange = database.getAllNeighborsWithinDistance(davisSquare, 10 * 1000);

The `zipCodesWithinRange` list will contain all of the zip codes -- sorted in order of increasing distance from Davis Square -- that are within ten kilometers of Davis Square.

### Finding neighbors by other search criteria

Assuming we still have the now-familiar list of zip codes, we might want to find the closest zip codes to Davis Square that are outside of the state of Massachusetts. We could do that using the `SearchCriteria` class:

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

The `closestOutsideMA` list will contain the ten closest zip codes to Davis Square sorted in order of increasing distance.

Acknowledgements
----------------

[United States ZIP code data](http://www.census.gov/tiger/tms/gazetteer/zips.txt) used in examples comes from [The United States Census Bureau](http://www.census.gov/). Examples make use of the [opencsv](http://opencsv.sourceforge.net/) library under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).