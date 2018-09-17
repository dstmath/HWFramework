package org.junit.runner.manipulation;

import org.junit.runner.Description;

public abstract class Filter {
    public static final Filter ALL = new Filter() {
        public boolean shouldRun(Description description) {
            return true;
        }

        public String describe() {
            return "all tests";
        }

        public void apply(Object child) throws NoTestsRemainException {
        }

        public Filter intersect(Filter second) {
            return second;
        }
    };

    public abstract String describe();

    public abstract boolean shouldRun(Description description);

    public static Filter matchMethodDescription(final Description desiredDescription) {
        return new Filter() {
            public boolean shouldRun(Description description) {
                if (description.isTest()) {
                    return desiredDescription.equals(description);
                }
                for (Description each : description.getChildren()) {
                    if (shouldRun(each)) {
                        return true;
                    }
                }
                return false;
            }

            public String describe() {
                return String.format("Method %s", new Object[]{desiredDescription.getDisplayName()});
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
            public boolean shouldRun(Description description) {
                if (this.shouldRun(description)) {
                    return second.shouldRun(description);
                }
                return false;
            }

            public String describe() {
                return this.describe() + " and " + second.describe();
            }
        };
    }
}
