package org.junit.runners.parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.runners.model.TestClass;

public class TestWithParameters {
    private final String name;
    private final List<Object> parameters;
    private final TestClass testClass;

    public TestWithParameters(String name2, TestClass testClass2, List<Object> parameters2) {
        notNull(name2, "The name is missing.");
        notNull(testClass2, "The test class is missing.");
        notNull(parameters2, "The parameters are missing.");
        this.name = name2;
        this.testClass = testClass2;
        this.parameters = Collections.unmodifiableList(new ArrayList(parameters2));
    }

    public String getName() {
        return this.name;
    }

    public TestClass getTestClass() {
        return this.testClass;
    }

    public List<Object> getParameters() {
        return this.parameters;
    }

    public int hashCode() {
        return (14747 * ((14747 * (this.name.hashCode() + 14747)) + this.testClass.hashCode())) + this.parameters.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TestWithParameters other = (TestWithParameters) obj;
        if (!this.name.equals(other.name) || !this.parameters.equals(other.parameters) || !this.testClass.equals(other.testClass)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return this.testClass.getName() + " '" + this.name + "' with parameters " + this.parameters;
    }

    private static void notNull(Object value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }
}
