package com.eatthepath.jeospatial.vptree;

/**
 * A PartitionException is thrown when partitioning of a vp-tree node fails.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class PartitionException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a @{code PartitionException} with no detail message.
     */
    public PartitionException() {}
    
    /**
     * Constructs a @{code PartitionException} with the given detail message.
     * 
     * @param message a detail message explaining the cause of this exception
     */
    public PartitionException(String message) {
        super(message);
    }
}
