package org.junit.internal.requests;

import java.util.Comparator;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Sorter;

public class SortingRequest extends Request {
    private final Comparator<Description> comparator;
    private final Request request;

    public SortingRequest(Request request, Comparator<Description> comparator) {
        this.request = request;
        this.comparator = comparator;
    }

    public Runner getRunner() {
        Runner runner = this.request.getRunner();
        new Sorter(this.comparator).apply(runner);
        return runner;
    }
}
