package org.junit.runner;

import org.junit.runner.manipulation.Filter;

public interface FilterFactory {

    public static class FilterNotCreatedException extends Exception {
        public FilterNotCreatedException(Exception exception) {
            super(exception.getMessage(), exception);
        }
    }

    Filter createFilter(FilterFactoryParams filterFactoryParams) throws FilterNotCreatedException;
}
