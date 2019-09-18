package org.junit.rules;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.internal.matchers.ThrowableCauseMatcher;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ExpectedException implements TestRule {
    private final ExpectedExceptionMatcherBuilder matcherBuilder = new ExpectedExceptionMatcherBuilder();
    private String missingExceptionMessage = "Expected test to throw %s";

    private class ExpectedExceptionStatement extends Statement {
        private final Statement next;

        public ExpectedExceptionStatement(Statement base) {
            this.next = base;
        }

        public void evaluate() throws Throwable {
            try {
                this.next.evaluate();
                if (ExpectedException.this.isAnyExceptionExpected()) {
                    ExpectedException.this.failDueToMissingException();
                }
            } catch (Throwable e) {
                ExpectedException.this.handleException(e);
            }
        }
    }

    public static ExpectedException none() {
        return new ExpectedException();
    }

    private ExpectedException() {
    }

    @Deprecated
    public ExpectedException handleAssertionErrors() {
        return this;
    }

    @Deprecated
    public ExpectedException handleAssumptionViolatedExceptions() {
        return this;
    }

    public ExpectedException reportMissingExceptionWithMessage(String message) {
        this.missingExceptionMessage = message;
        return this;
    }

    public Statement apply(Statement base, Description description) {
        return new ExpectedExceptionStatement(base);
    }

    public void expect(Matcher<?> matcher) {
        this.matcherBuilder.add(matcher);
    }

    public void expect(Class<? extends Throwable> type) {
        expect((Matcher<?>) CoreMatchers.instanceOf(type));
    }

    public void expectMessage(String substring) {
        expectMessage(CoreMatchers.containsString(substring));
    }

    public void expectMessage(Matcher<String> matcher) {
        expect((Matcher<?>) ThrowableMessageMatcher.hasMessage(matcher));
    }

    public void expectCause(Matcher<? extends Throwable> expectedCause) {
        expect((Matcher<?>) ThrowableCauseMatcher.hasCause(expectedCause));
    }

    /* access modifiers changed from: private */
    public void handleException(Throwable e) throws Throwable {
        if (isAnyExceptionExpected()) {
            Assert.assertThat(e, this.matcherBuilder.build());
            return;
        }
        throw e;
    }

    /* access modifiers changed from: private */
    public boolean isAnyExceptionExpected() {
        return this.matcherBuilder.expectsThrowable();
    }

    /* access modifiers changed from: private */
    public void failDueToMissingException() throws AssertionError {
        Assert.fail(missingExceptionMessage());
    }

    private String missingExceptionMessage() {
        String expectation = StringDescription.toString(this.matcherBuilder.build());
        return String.format(this.missingExceptionMessage, new Object[]{expectation});
    }
}
