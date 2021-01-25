package org.junit.experimental.categories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.experimental.categories.Categories;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

public final class ExcludeCategories extends CategoryFilterFactory {
    @Override // org.junit.experimental.categories.CategoryFilterFactory, org.junit.runner.FilterFactory
    public /* bridge */ /* synthetic */ Filter createFilter(FilterFactoryParams filterFactoryParams) throws FilterFactory.FilterNotCreatedException {
        return super.createFilter(filterFactoryParams);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.experimental.categories.CategoryFilterFactory
    public Filter createFilter(List<Class<?>> categories) {
        return new ExcludesAny(categories);
    }

    private static class ExcludesAny extends Categories.CategoryFilter {
        public ExcludesAny(List<Class<?>> categories) {
            this(new HashSet(categories));
        }

        public ExcludesAny(Set<Class<?>> categories) {
            super(true, null, true, categories);
        }

        @Override // org.junit.experimental.categories.Categories.CategoryFilter, org.junit.runner.manipulation.Filter
        public String describe() {
            return "excludes " + super.describe();
        }
    }
}
