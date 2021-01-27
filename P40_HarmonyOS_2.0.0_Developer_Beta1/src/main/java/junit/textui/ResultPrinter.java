package junit.textui;

import java.io.PrintStream;
import java.util.Enumeration;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;

public class ResultPrinter implements TestListener {
    int fColumn = 0;
    PrintStream fWriter;

    public ResultPrinter(PrintStream writer) {
        this.fWriter = writer;
    }

    /* access modifiers changed from: package-private */
    public synchronized void print(TestResult result, long runTime) {
        printHeader(runTime);
        printErrors(result);
        printFailures(result);
        printFooter(result);
    }

    /* access modifiers changed from: package-private */
    public void printWaitPrompt() {
        getWriter().println();
        getWriter().println("<RETURN> to continue");
    }

    /* access modifiers changed from: protected */
    public void printHeader(long runTime) {
        getWriter().println();
        PrintStream writer = getWriter();
        writer.println("Time: " + elapsedTimeAsString(runTime));
    }

    /* access modifiers changed from: protected */
    public void printErrors(TestResult result) {
        printDefects(result.errors(), result.errorCount(), "error");
    }

    /* access modifiers changed from: protected */
    public void printFailures(TestResult result) {
        printDefects(result.failures(), result.failureCount(), "failure");
    }

    /* access modifiers changed from: protected */
    public void printDefects(Enumeration<TestFailure> booBoos, int count, String type) {
        if (count != 0) {
            if (count == 1) {
                PrintStream writer = getWriter();
                writer.println("There was " + count + " " + type + ":");
            } else {
                PrintStream writer2 = getWriter();
                writer2.println("There were " + count + " " + type + "s:");
            }
            int i = 1;
            while (booBoos.hasMoreElements()) {
                printDefect(booBoos.nextElement(), i);
                i++;
            }
        }
    }

    public void printDefect(TestFailure booBoo, int count) {
        printDefectHeader(booBoo, count);
        printDefectTrace(booBoo);
    }

    /* access modifiers changed from: protected */
    public void printDefectHeader(TestFailure booBoo, int count) {
        PrintStream writer = getWriter();
        writer.print(count + ") " + booBoo.failedTest());
    }

    /* access modifiers changed from: protected */
    public void printDefectTrace(TestFailure booBoo) {
        getWriter().print(BaseTestRunner.getFilteredTrace(booBoo.trace()));
    }

    /* access modifiers changed from: protected */
    public void printFooter(TestResult result) {
        if (result.wasSuccessful()) {
            getWriter().println();
            getWriter().print("OK");
            PrintStream writer = getWriter();
            StringBuilder sb = new StringBuilder();
            sb.append(" (");
            sb.append(result.runCount());
            sb.append(" test");
            sb.append(result.runCount() == 1 ? "" : "s");
            sb.append(")");
            writer.println(sb.toString());
        } else {
            getWriter().println();
            getWriter().println("FAILURES!!!");
            PrintStream writer2 = getWriter();
            writer2.println("Tests run: " + result.runCount() + ",  Failures: " + result.failureCount() + ",  Errors: " + result.errorCount());
        }
        getWriter().println();
    }

    /* access modifiers changed from: protected */
    public String elapsedTimeAsString(long runTime) {
        return Double.toString(((double) runTime) / 1000.0d);
    }

    public PrintStream getWriter() {
        return this.fWriter;
    }

    public void addError(Test test, Throwable t) {
        getWriter().print("E");
    }

    public void addFailure(Test test, AssertionFailedError t) {
        getWriter().print("F");
    }

    public void endTest(Test test) {
    }

    public void startTest(Test test) {
        getWriter().print(".");
        int i = this.fColumn;
        this.fColumn = i + 1;
        if (i >= 40) {
            getWriter().println();
            this.fColumn = 0;
        }
    }
}
