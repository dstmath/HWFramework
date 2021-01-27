package org.junit.experimental.categories;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class Categories extends Suite {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExcludeCategory {
        boolean matchAny() default true;

        Class<?>[] value() default {};
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface IncludeCategory {
        boolean matchAny() default true;

        Class<?>[] value() default {};
    }

    public static class CategoryFilter extends Filter {
        private final Set<Class<?>> excluded;
        private final boolean excludedAny;
        private final Set<Class<?>> included;
        private final boolean includedAny;

        public static CategoryFilter include(boolean matchAny, Class<?>... categories) {
            if (!hasNull(categories)) {
                return categoryFilter(matchAny, Categories.createSet(categories), true, null);
            }
            throw new NullPointerException("has null category");
        }

        public static CategoryFilter include(Class<?> category) {
            return include(true, category);
        }

        public static CategoryFilter include(Class<?>... categories) {
            return include(true, categories);
        }

        public static CategoryFilter exclude(boolean matchAny, Class<?>... categories) {
            if (!hasNull(categories)) {
                return categoryFilter(true, null, matchAny, Categories.createSet(categories));
            }
            throw new NullPointerException("has null category");
        }

        public static CategoryFilter exclude(Class<?> category) {
            return exclude(true, category);
        }

        public static CategoryFilter exclude(Class<?>... categories) {
            return exclude(true, categories);
        }

        public static CategoryFilter categoryFilter(boolean matchAnyInclusions, Set<Class<?>> inclusions, boolean matchAnyExclusions, Set<Class<?>> exclusions) {
            return new CategoryFilter(matchAnyInclusions, inclusions, matchAnyExclusions, exclusions);
        }

        protected CategoryFilter(boolean matchAnyIncludes, Set<Class<?>> includes, boolean matchAnyExcludes, Set<Class<?>> excludes) {
            this.includedAny = matchAnyIncludes;
            this.excludedAny = matchAnyExcludes;
            this.included = copyAndRefine(includes);
            this.excluded = copyAndRefine(excludes);
        }

        @Override // org.junit.runner.manipulation.Filter
        public String describe() {
            return toString();
        }

        public String toString() {
            StringBuilder description = new StringBuilder("categories ").append(this.included.isEmpty() ? "[all]" : this.included);
            if (!this.excluded.isEmpty()) {
                description.append(" - ");
                description.append(this.excluded);
            }
            return description.toString();
        }

        @Override // org.junit.runner.manipulation.Filter
        public boolean shouldRun(Description description) {
            if (hasCorrectCategoryAnnotation(description)) {
                return true;
            }
            Iterator<Description> it = description.getChildren().iterator();
            while (it.hasNext()) {
                if (shouldRun(it.next())) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasCorrectCategoryAnnotation(Description description) {
            Set<Class<?>> childCategories = categories(description);
            if (childCategories.isEmpty()) {
                return this.included.isEmpty();
            }
            if (!this.excluded.isEmpty()) {
                if (this.excludedAny) {
                    if (matchesAnyParentCategories(childCategories, this.excluded)) {
                        return false;
                    }
                } else if (matchesAllParentCategories(childCategories, this.excluded)) {
                    return false;
                }
            }
            if (this.included.isEmpty()) {
                return true;
            }
            if (this.includedAny) {
                return matchesAnyParentCategories(childCategories, this.included);
            }
            return matchesAllParentCategories(childCategories, this.included);
        }

        private boolean matchesAnyParentCategories(Set<Class<?>> childCategories, Set<Class<?>> parentCategories) {
            for (Class<?> parentCategory : parentCategories) {
                if (Categories.hasAssignableTo(childCategories, parentCategory)) {
                    return true;
                }
            }
            return false;
        }

        private boolean matchesAllParentCategories(Set<Class<?>> childCategories, Set<Class<?>> parentCategories) {
            for (Class<?> parentCategory : parentCategories) {
                if (!Categories.hasAssignableTo(childCategories, parentCategory)) {
                    return false;
                }
            }
            return true;
        }

        private static Set<Class<?>> categories(Description description) {
            Set<Class<?>> categories = new HashSet<>();
            Collections.addAll(categories, directCategories(description));
            Collections.addAll(categories, directCategories(parentDescription(description)));
            return categories;
        }

        private static Description parentDescription(Description description) {
            Class<?> testClass = description.getTestClass();
            if (testClass == null) {
                return null;
            }
            return Description.createSuiteDescription(testClass);
        }

        private static Class<?>[] directCategories(Description description) {
            if (description == null) {
                return new Class[0];
            }
            Category annotation = (Category) description.getAnnotation(Category.class);
            return annotation == null ? new Class[0] : annotation.value();
        }

        private static Set<Class<?>> copyAndRefine(Set<Class<?>> classes) {
            HashSet<Class<?>> c = new HashSet<>();
            if (classes != null) {
                c.addAll(classes);
            }
            c.remove(null);
            return c;
        }

        private static boolean hasNull(Class<?>... classes) {
            if (classes == null) {
                return false;
            }
            for (Class<?> clazz : classes) {
                if (clazz == null) {
                    return true;
                }
            }
            return false;
        }
    }

    public Categories(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        try {
            filter(CategoryFilter.categoryFilter(isAnyIncluded(klass), getIncludedCategory(klass), isAnyExcluded(klass), getExcludedCategory(klass)));
            assertNoCategorizedDescendentsOfUncategorizeableParents(getDescription());
        } catch (NoTestsRemainException e) {
            throw new InitializationError(e);
        }
    }

    private static Set<Class<?>> getIncludedCategory(Class<?> klass) {
        IncludeCategory annotation = (IncludeCategory) klass.getAnnotation(IncludeCategory.class);
        return createSet(annotation == null ? null : annotation.value());
    }

    private static boolean isAnyIncluded(Class<?> klass) {
        IncludeCategory annotation = (IncludeCategory) klass.getAnnotation(IncludeCategory.class);
        return annotation == null || annotation.matchAny();
    }

    private static Set<Class<?>> getExcludedCategory(Class<?> klass) {
        ExcludeCategory annotation = (ExcludeCategory) klass.getAnnotation(ExcludeCategory.class);
        return createSet(annotation == null ? null : annotation.value());
    }

    private static boolean isAnyExcluded(Class<?> klass) {
        ExcludeCategory annotation = (ExcludeCategory) klass.getAnnotation(ExcludeCategory.class);
        return annotation == null || annotation.matchAny();
    }

    private static void assertNoCategorizedDescendentsOfUncategorizeableParents(Description description) throws InitializationError {
        if (!canHaveCategorizedChildren(description)) {
            assertNoDescendantsHaveCategoryAnnotations(description);
        }
        Iterator<Description> it = description.getChildren().iterator();
        while (it.hasNext()) {
            assertNoCategorizedDescendentsOfUncategorizeableParents(it.next());
        }
    }

    private static void assertNoDescendantsHaveCategoryAnnotations(Description description) throws InitializationError {
        Iterator<Description> it = description.getChildren().iterator();
        while (it.hasNext()) {
            Description each = it.next();
            if (each.getAnnotation(Category.class) == null) {
                assertNoDescendantsHaveCategoryAnnotations(each);
            } else {
                throw new InitializationError("Category annotations on Parameterized classes are not supported on individual methods.");
            }
        }
    }

    private static boolean canHaveCategorizedChildren(Description description) {
        Iterator<Description> it = description.getChildren().iterator();
        while (it.hasNext()) {
            if (it.next().getTestClass() == null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean hasAssignableTo(Set<Class<?>> assigns, Class<?> to) {
        for (Class<?> from : assigns) {
            if (to.isAssignableFrom(from)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static Set<Class<?>> createSet(Class<?>... t) {
        Set<Class<?>> set = new HashSet<>();
        if (t != null) {
            Collections.addAll(set, t);
        }
        return set;
    }
}
