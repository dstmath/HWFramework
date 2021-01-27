package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@Deprecated
public class MethodValidator {
    private final List<Throwable> errors = new ArrayList();
    private TestClass testClass;

    public MethodValidator(TestClass testClass2) {
        this.testClass = testClass2;
    }

    public void validateInstanceMethods() {
        validateTestMethods(After.class, false);
        validateTestMethods(Before.class, false);
        validateTestMethods(Test.class, false);
        if (this.testClass.getAnnotatedMethods(Test.class).size() == 0) {
            this.errors.add(new Exception("No runnable methods"));
        }
    }

    public void validateStaticMethods() {
        validateTestMethods(BeforeClass.class, true);
        validateTestMethods(AfterClass.class, true);
    }

    public List<Throwable> validateMethodsForDefaultRunner() {
        validateNoArgConstructor();
        validateStaticMethods();
        validateInstanceMethods();
        return this.errors;
    }

    public void assertValid() throws InitializationError {
        if (!this.errors.isEmpty()) {
            throw new InitializationError(this.errors);
        }
    }

    public void validateNoArgConstructor() {
        try {
            this.testClass.getConstructor();
        } catch (Exception e) {
            this.errors.add(new Exception("Test class should have public zero-argument constructor", e));
        }
    }

    private void validateTestMethods(Class<? extends Annotation> annotation, boolean isStatic) {
        for (Method each : this.testClass.getAnnotatedMethods(annotation)) {
            if (Modifier.isStatic(each.getModifiers()) != isStatic) {
                String state = isStatic ? "should" : "should not";
                List<Throwable> list = this.errors;
                list.add(new Exception("Method " + each.getName() + "() " + state + " be static"));
            }
            if (!Modifier.isPublic(each.getDeclaringClass().getModifiers())) {
                List<Throwable> list2 = this.errors;
                list2.add(new Exception("Class " + each.getDeclaringClass().getName() + " should be public"));
            }
            if (!Modifier.isPublic(each.getModifiers())) {
                List<Throwable> list3 = this.errors;
                list3.add(new Exception("Method " + each.getName() + " should be public"));
            }
            if (each.getReturnType() != Void.TYPE) {
                List<Throwable> list4 = this.errors;
                list4.add(new Exception("Method " + each.getName() + " should be void"));
            }
            if (each.getParameterTypes().length != 0) {
                List<Throwable> list5 = this.errors;
                list5.add(new Exception("Method " + each.getName() + " should have no parameters"));
            }
        }
    }
}
