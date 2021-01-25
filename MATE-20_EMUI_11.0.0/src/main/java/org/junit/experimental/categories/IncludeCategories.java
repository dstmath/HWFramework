package org.junit.experimental.categories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.experimental.categories.Categories;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

public final class IncludeCategories extends CategoryFilterFactory {
    @Override // org.junit.experimental.categories.CategoryFilterFactory, org.junit.runner.FilterFactory
    public /* bridge */ /* synthetic */ Filter createFilter(FilterFactoryParams filterFactoryParams) throws FilterFactory.FilterNotCreatedException {
        return super.createFilter(filterFactoryParams);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.experimental.categories.CategoryFilterFactory
    public Filter createFilter(List<Class<?>> categories) {
        return new IncludesAny(categories);
    }

    private static class IncludesAny extends Categories.CategoryFilter {
        public IncludesAny(List<Class<?>> categories) {
            this(new HashSet(categories));
        }

        public IncludesAny(Set<Class<?>> categories) {
            super(true, categories, true, null);
        }

        @Override // org.junit.experimental.categories.Categories.CategoryFilter, org.junit.runner.manipulation.Filter
        public String describe() {
            return "includes " + super.describe();
        }
    }
}
