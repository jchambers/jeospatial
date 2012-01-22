package com.eatthepath.jeospatial;

public interface SearchCriteria<T> {
    public boolean matches(T obj);
}
