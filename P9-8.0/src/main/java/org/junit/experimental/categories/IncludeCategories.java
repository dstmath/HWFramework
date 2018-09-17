package org.junit.experimental.categories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.experimental.categories.Categories.CategoryFilter;
import org.junit.runner.manipulation.Filter;

public final class IncludeCategories extends CategoryFilterFactory {

    private static class IncludesAny extends CategoryFilter {
        public IncludesAny(List<Class<?>> categories) {
            this(new HashSet(categories));
        }

        public IncludesAny(Set<Class<?>> categories) {
            super(true, categories, true, null);
        }

        public String describe() {
            return "includes " + super.describe();
        }
    }

    protected Filter createFilter(List<Class<?>> categories) {
        return new IncludesAny((List) categories);
    }
}
