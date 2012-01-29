/**
 * <p>Provides {@link com.eatthepath.jeospatial.GeospatialPointDatabase}
 * implementations based on vantage point trees.</p>
 * 
 * <p>Vantage point trees (or vp-trees) are data structures that perform binary
 * partitioning of a metric space. Vantage point trees can be constructed in O(n
 * log(n)) time and allow nearest-neighbor searches to execute in O(log(n))
 * time. The implementations included in this package implement the
 * {@link java.util.Collection} interface and support all optional
 * operations.</p>
 * 
 * <p>This package includes two vp-tree implementations:
 * {@link com.eatthepath.jeospatial.vptree.VPTree} and
 * {@link com.eatthepath.jeospatial.vptree.LockingVPTree}. The former is
 * <strong>not</strong> thread safe, but can be expected to offer modestly
 * higher search throughput due to the absence of any overhead associated with
 * acquiring or releasing locks. The latter is thread-safe, but may offer lower
 * search throughput.</p>
 */
package com.eatthepath.jeospatial.vptree;