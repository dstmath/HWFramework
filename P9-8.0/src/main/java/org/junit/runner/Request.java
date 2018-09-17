package org.junit.runner;

import java.util.Comparator;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.requests.ClassRequest;
import org.junit.internal.requests.FilterRequest;
import org.junit.internal.requests.SortingRequest;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.model.InitializationError;

public abstract class Request {
    public abstract Runner getRunner();

    public static Request method(Class<?> clazz, String methodName) {
        return aClass(clazz).filterWith(Description.createTestDescription(clazz, methodName));
    }

    public static Request aClass(Class<?> clazz) {
        return new ClassRequest(clazz);
    }

    public static Request classWithoutSuiteMethod(Class<?> clazz) {
        return new ClassRequest(clazz, false);
    }

    public static Request classes(Computer computer, Class<?>... classes) {
        try {
            return runner(computer.getSuite(new AllDefaultPossibilitiesBuilder(true), classes));
        } catch (InitializationError e) {
            throw new RuntimeException("Bug in saff's brain: Suite constructor, called as above, should always complete");
        }
    }

    public static Request classes(Class<?>... classes) {
        return classes(JUnitCore.defaultComputer(), classes);
    }

    public static Request errorReport(Class<?> klass, Throwable cause) {
        return runner(new ErrorReportingRunner(klass, cause));
    }

    public static Request runner(final Runner runner) {
        return new Request() {
            public Runner getRunner() {
                return runner;
            }
        };
    }

    public Request filterWith(Filter filter) {
        return new FilterRequest(this, filter);
    }

    public Request filterWith(Description desiredDescription) {
        return filterWith(Filter.matchMethodDescription(desiredDescription));
    }

    public Request sortWith(Comparator<Description> comparator) {
        return new SortingRequest(this, comparator);
    }
}
