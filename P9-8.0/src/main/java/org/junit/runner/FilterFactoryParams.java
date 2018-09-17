package org.junit.runner;

public final class FilterFactoryParams {
    private final String args;
    private final Description topLevelDescription;

    public FilterFactoryParams(Description topLevelDescription, String args) {
        if (args == null || topLevelDescription == null) {
            throw new NullPointerException();
        }
        this.topLevelDescription = topLevelDescription;
        this.args = args;
    }

    public String getArgs() {
        return this.args;
    }

    public Description getTopLevelDescription() {
        return this.topLevelDescription;
    }
}
