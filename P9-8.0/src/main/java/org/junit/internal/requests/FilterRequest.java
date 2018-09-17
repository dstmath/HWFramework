package org.junit.internal.requests;

import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

public final class FilterRequest extends Request {
    private final Filter fFilter;
    private final Request request;

    public FilterRequest(Request request, Filter filter) {
        this.request = request;
        this.fFilter = filter;
    }

    public Runner getRunner() {
        try {
            Runner runner = this.request.getRunner();
            this.fFilter.apply(runner);
            return runner;
        } catch (NoTestsRemainException e) {
            return new ErrorReportingRunner(Filter.class, new Exception(String.format("No tests found matching %s from %s", new Object[]{this.fFilter.describe(), this.request.toString()})));
        }
    }
}
