package org.junit.runner.manipulation;

import java.util.Iterator;
import org.junit.runner.Description;

public abstract class Filter {
    public static final Filter ALL = new Filter() {
        /* class org.junit.runner.manipulation.Filter.AnonymousClass1 */

        @Override // org.junit.runner.manipulation.Filter
        public boolean shouldRun(Description description) {
            return true;
        }

        @Override // org.junit.runner.manipulation.Filter
        public String describe() {
            return "all tests";
        }

        @Override // org.junit.runner.manipulation.Filter
        public void apply(Object child) throws NoTestsRemainException {
        }

        @Override // org.junit.runner.manipulation.Filter
        public Filter intersect(Filter second) {
            return second;
        }
    };

    public abstract String describe();

    public abstract boolean shouldRun(Description description);

    public static Filter matchMethodDescription(final Description desiredDescription) {
        return new Filter() {
            /* class org.junit.runner.manipulation.Filter.AnonymousClass2 */

            @Override // org.junit.runner.manipulation.Filter
            public boolean shouldRun(Description description) {
                if (description.isTest()) {
                    return Description.this.equals(description);
                }
                Iterator<Description> it = description.getChildren().iterator();
                while (it.hasNext()) {
                    if (shouldRun(it.next())) {
                        return true;
                    }
                }
                return false;
            }

            @Override // org.junit.runner.manipulation.Filter
            public String describe() {
                return String.format("Method %s", Description.this.getDisplayName());
            }
        };
    }

    public void apply(Object child) throws NoTestsRemainException {
        if (child instanceof Filterable) {
            ((Filterable) child).filter(this);
        }
    }

    public Filter intersect(final Filter second) {
        if (second == this || second == ALL) {
            return this;
        }
        return new Filter() {
            /* class org.junit.runner.manipulation.Filter.AnonymousClass3 */

            @Override // org.junit.runner.manipulation.Filter
            public boolean shouldRun(Description description) {
                return this.shouldRun(description) && second.shouldRun(description);
            }

            @Override // org.junit.runner.manipulation.Filter
            public String describe() {
                return this.describe() + " and " + second.describe();
            }
        };
    }
}
