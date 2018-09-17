package org.junit.runners.parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.runners.model.TestClass;

public class TestWithParameters {
    private final String name;
    private final List<Object> parameters;
    private final TestClass testClass;

    public TestWithParameters(String name, TestClass testClass, List<Object> parameters) {
        notNull(name, "The name is missing.");
        notNull(testClass, "The test class is missing.");
        notNull(parameters, "The parameters are missing.");
        this.name = name;
        this.testClass = testClass;
        this.parameters = Collections.unmodifiableList(new ArrayList(parameters));
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
        return ((((this.name.hashCode() + 14747) * 14747) + this.testClass.hashCode()) * 14747) + this.parameters.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TestWithParameters other = (TestWithParameters) obj;
        if (this.name.equals(other.name) && this.parameters.equals(other.parameters)) {
            z = this.testClass.equals(other.testClass);
        }
        return z;
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
