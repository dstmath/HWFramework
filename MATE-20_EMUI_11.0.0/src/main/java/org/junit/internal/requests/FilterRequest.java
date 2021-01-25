package org.junit.internal.requests;

import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

public final class FilterRequest extends Request {
    private final Filter fFilter;
    private final Request request;

    public FilterRequest(Request request2, Filter filter) {
        this.request = request2;
        this.fFilter = filter;
    }

    @Override // org.junit.runner.Request
    public Runner getRunner() {
        try {
            Runner runner = this.request.getRunner();
            this.fFilter.apply(runner);
            return runner;
        } catch (NoTestsRemainException e) {
            return new ErrorReportingRunner(Filter.class, new Exception(String.format("No tests found matching %s from %s", this.fFilter.describe(), this.request.toString())));
        }
    }
}
