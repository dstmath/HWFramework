package org.junit.experimental.categories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.experimental.categories.Categories.CategoryFilter;
import org.junit.runner.manipulation.Filter;

public final class ExcludeCategories extends CategoryFilterFactory {

    private static class ExcludesAny extends CategoryFilter {
        public ExcludesAny(List<Class<?>> categories) {
            this(new HashSet(categories));
        }

        public ExcludesAny(Set<Class<?>> categories) {
            super(true, null, true, categories);
        }

        public String describe() {
            return "excludes " + super.describe();
        }
    }

    protected Filter createFilter(List<Class<?>> categories) {
        return new ExcludesAny((List) categories);
    }
}
