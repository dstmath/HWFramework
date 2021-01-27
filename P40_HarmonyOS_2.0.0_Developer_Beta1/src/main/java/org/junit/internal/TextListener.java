package org.junit.internal;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TextListener extends RunListener {
    private final PrintStream writer;

    public TextListener(JUnitSystem system) {
        this(system.out());
    }

    public TextListener(PrintStream writer2) {
        this.writer = writer2;
    }

    @Override // org.junit.runner.notification.RunListener
    public void testRunFinished(Result result) {
        printHeader(result.getRunTime());
        printFailures(result);
        printFooter(result);
    }

    @Override // org.junit.runner.notification.RunListener
    public void testStarted(Description description) {
        this.writer.append('.');
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFailure(Failure failure) {
        this.writer.append('E');
    }

    @Override // org.junit.runner.notification.RunListener
    public void testIgnored(Description description) {
        this.writer.append('I');
    }

    private PrintStream getWriter() {
        return this.writer;
    }

    /* access modifiers changed from: protected */
    public void printHeader(long runTime) {
        getWriter().println();
        PrintStream writer2 = getWriter();
        writer2.println("Time: " + elapsedTimeAsString(runTime));
    }

    /* access modifiers changed from: protected */
    public void printFailures(Result result) {
        List<Failure> failures = result.getFailures();
        if (failures.size() != 0) {
            if (failures.size() == 1) {
                PrintStream writer2 = getWriter();
                writer2.println("There was " + failures.size() + " failure:");
            } else {
                PrintStream writer3 = getWriter();
                writer3.println("There were " + failures.size() + " failures:");
            }
            int i = 1;
            for (Failure each : failures) {
                printFailure(each, "" + i);
                i++;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void printFailure(Failure each, String prefix) {
        PrintStream writer2 = getWriter();
        writer2.println(prefix + ") " + each.getTestHeader());
        getWriter().print(each.getTrace());
    }

    /* access modifiers changed from: protected */
    public void printFooter(Result result) {
        if (result.wasSuccessful()) {
            getWriter().println();
            getWriter().print("OK");
            PrintStream writer2 = getWriter();
            StringBuilder sb = new StringBuilder();
            sb.append(" (");
            sb.append(result.getRunCount());
            sb.append(" test");
            sb.append(result.getRunCount() == 1 ? "" : "s");
            sb.append(")");
            writer2.println(sb.toString());
        } else {
            getWriter().println();
            getWriter().println("FAILURES!!!");
            PrintStream writer3 = getWriter();
            writer3.println("Tests run: " + result.getRunCount() + ",  Failures: " + result.getFailureCount());
        }
        getWriter().println();
    }

    /* access modifiers changed from: protected */
    public String elapsedTimeAsString(long runTime) {
        return NumberFormat.getInstance().format(((double) runTime) / 1000.0d);
    }
}
