package com.eatthepath.jeospatial.vptree;

public class PartitionException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public PartitionException() {}
    
    public PartitionException(String message) {
        super(message);
    }
    
    public PartitionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PartitionException(Throwable cause) {
        super(cause);
    }
}
