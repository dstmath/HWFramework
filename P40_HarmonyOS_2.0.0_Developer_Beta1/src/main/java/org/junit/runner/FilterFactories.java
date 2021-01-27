package org.junit.runner;

import org.junit.internal.Classes;
import org.junit.runner.FilterFactory;
import org.junit.runner.manipulation.Filter;

class FilterFactories {
    FilterFactories() {
    }

    public static Filter createFilterFromFilterSpec(Request request, String filterSpec) throws FilterFactory.FilterNotCreatedException {
        Description topLevelDescription = request.getRunner().getDescription();
        String[] tuple = filterSpec.contains("=") ? filterSpec.split("=", 2) : new String[]{filterSpec, ""};
        return createFilter(tuple[0], new FilterFactoryParams(topLevelDescription, tuple[1]));
    }

    public static Filter createFilter(String filterFactoryFqcn, FilterFactoryParams params) throws FilterFactory.FilterNotCreatedException {
        return createFilterFactory(filterFactoryFqcn).createFilter(params);
    }

    public static Filter createFilter(Class<? extends FilterFactory> filterFactoryClass, FilterFactoryParams params) throws FilterFactory.FilterNotCreatedException {
        return createFilterFactory(filterFactoryClass).createFilter(params);
    }

    static FilterFactory createFilterFactory(String filterFactoryFqcn) throws FilterFactory.FilterNotCreatedException {
        try {
            return createFilterFactory(Classes.getClass(filterFactoryFqcn).asSubclass(FilterFactory.class));
        } catch (Exception e) {
            throw new FilterFactory.FilterNotCreatedException(e);
        }
    }

    static FilterFactory createFilterFactory(Class<? extends FilterFactory> filterFactoryClass) throws FilterFactory.FilterNotCreatedException {
        try {
            return (FilterFactory) filterFactoryClass.getConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception e) {
            throw new FilterFactory.FilterNotCreatedException(e);
        }
    }
}
