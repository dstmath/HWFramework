package java.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import libcore.util.EmptyArray;

public class Throwable implements Serializable {
    private static final String CAUSE_CAPTION = "Caused by: ";
    private static Throwable[] EMPTY_THROWABLE_ARRAY = null;
    private static final String NULL_CAUSE_MESSAGE = "Cannot suppress a null exception.";
    private static final String SELF_SUPPRESSION_MESSAGE = "Self-suppression not permitted";
    private static final String SUPPRESSED_CAPTION = "Suppressed: ";
    private static final long serialVersionUID = -3042686055658047285L;
    private volatile transient Object backtrace;
    private Throwable cause = this;
    private String detailMessage;
    private StackTraceElement[] stackTrace = EmptyArray.STACK_TRACE_ELEMENT;
    private List<Throwable> suppressedExceptions = Collections.emptyList();

    private static abstract class PrintStreamOrWriter {
        /* synthetic */ PrintStreamOrWriter(PrintStreamOrWriter -this0) {
            this();
        }

        abstract Object lock();

        abstract void println(Object obj);

        private PrintStreamOrWriter() {
        }
    }

    private static class SentinelHolder {
        public static final StackTraceElement STACK_TRACE_ELEMENT_SENTINEL = new StackTraceElement("", "", null, Integer.MIN_VALUE);
        public static final StackTraceElement[] STACK_TRACE_SENTINEL = new StackTraceElement[]{STACK_TRACE_ELEMENT_SENTINEL};

        private SentinelHolder() {
        }
    }

    private static class WrappedPrintStream extends PrintStreamOrWriter {
        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            super();
            this.printStream = printStream;
        }

        Object lock() {
            return this.printStream;
        }

        void println(Object o) {
            this.printStream.println(o);
        }
    }

    private static class WrappedPrintWriter extends PrintStreamOrWriter {
        private final PrintWriter printWriter;

        WrappedPrintWriter(PrintWriter printWriter) {
            super();
            this.printWriter = printWriter;
        }

        Object lock() {
            return this.printWriter;
        }

        void println(Object o) {
            this.printWriter.println(o);
        }
    }

    private static native Object nativeFillInStackTrace();

    private static native StackTraceElement[] nativeGetStackTrace(Object obj);

    public Throwable() {
        fillInStackTrace();
    }

    public Throwable(String message) {
        fillInStackTrace();
        this.detailMessage = message;
    }

    public Throwable(String message, Throwable cause) {
        fillInStackTrace();
        this.detailMessage = message;
        this.cause = cause;
    }

    public Throwable(Throwable cause) {
        String str = null;
        fillInStackTrace();
        if (cause != null) {
            str = cause.toString();
        }
        this.detailMessage = str;
        this.cause = cause;
    }

    protected Throwable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        if (writableStackTrace) {
            fillInStackTrace();
        } else {
            this.stackTrace = null;
        }
        this.detailMessage = message;
        this.cause = cause;
        if (!enableSuppression) {
            this.suppressedExceptions = null;
        }
    }

    public String getMessage() {
        return this.detailMessage;
    }

    public String getLocalizedMessage() {
        return getMessage();
    }

    public synchronized Throwable getCause() {
        return this.cause == this ? null : this.cause;
    }

    public synchronized Throwable initCause(Throwable cause) {
        if (this.cause != this) {
            throw new IllegalStateException("Can't overwrite cause with " + Objects.toString(cause, "a null"), this);
        } else if (cause == this) {
            throw new IllegalArgumentException("Self-causation not permitted", this);
        } else {
            this.cause = cause;
        }
        return this;
    }

    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return message != null ? s + ": " + message : s;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream s) {
        printStackTrace(new WrappedPrintStream(s));
    }

    private void printStackTrace(PrintStreamOrWriter s) {
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap());
        dejaVu.add(this);
        synchronized (s.lock()) {
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (Object traceElement : trace) {
                s.println("\tat " + traceElement);
            }
            for (Throwable se : getSuppressed()) {
                se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu);
            }
            Throwable ourCause = getCause();
            if (ourCause != null) {
                ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu);
            }
        }
    }

    private void printEnclosedStackTrace(PrintStreamOrWriter s, StackTraceElement[] enclosingTrace, String caption, String prefix, Set<Throwable> dejaVu) {
        if (dejaVu.contains(this)) {
            s.println("\t[CIRCULAR REFERENCE:" + this + "]");
            return;
        }
        dejaVu.add(this);
        StackTraceElement[] trace = getOurStackTrace();
        int m = trace.length - 1;
        int n = enclosingTrace.length - 1;
        while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
            m--;
            n--;
        }
        int framesInCommon = (trace.length - 1) - m;
        s.println(prefix + caption + this);
        for (int i = 0; i <= m; i++) {
            s.println(prefix + "\tat " + trace[i]);
        }
        if (framesInCommon != 0) {
            s.println(prefix + "\t... " + framesInCommon + " more");
        }
        for (Throwable se : getSuppressed()) {
            se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, prefix + "\t", dejaVu);
        }
        Throwable ourCause = getCause();
        if (ourCause != null) {
            ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, prefix, dejaVu);
        }
    }

    public void printStackTrace(PrintWriter s) {
        printStackTrace(new WrappedPrintWriter(s));
    }

    public synchronized Throwable fillInStackTrace() {
        if (!(this.stackTrace == null && this.backtrace == null)) {
            this.backtrace = nativeFillInStackTrace();
            this.stackTrace = EmptyArray.STACK_TRACE_ELEMENT;
        }
        return this;
    }

    public StackTraceElement[] getStackTrace() {
        return (StackTraceElement[]) getOurStackTrace().clone();
    }

    private synchronized StackTraceElement[] getOurStackTrace() {
        if (this.stackTrace == EmptyArray.STACK_TRACE_ELEMENT || (this.stackTrace == null && this.backtrace != null)) {
            this.stackTrace = nativeGetStackTrace(this.backtrace);
            this.backtrace = null;
        }
        if (this.stackTrace == null) {
            return EmptyArray.STACK_TRACE_ELEMENT;
        }
        return this.stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        StackTraceElement[] defensiveCopy = (StackTraceElement[]) stackTrace.clone();
        for (int i = 0; i < defensiveCopy.length; i++) {
            if (defensiveCopy[i] == null) {
                throw new NullPointerException("stackTrace[" + i + "]");
            }
        }
        synchronized (this) {
            if (this.stackTrace == null && this.backtrace == null) {
                return;
            }
            this.stackTrace = defensiveCopy;
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        int i = 0;
        s.defaultReadObject();
        if (this.suppressedExceptions != null) {
            List<Throwable> suppressed;
            if (this.suppressedExceptions.isEmpty()) {
                suppressed = Collections.emptyList();
            } else {
                suppressed = new ArrayList(1);
                for (Throwable t : this.suppressedExceptions) {
                    if (t == null) {
                        throw new NullPointerException(NULL_CAUSE_MESSAGE);
                    } else if (t == this) {
                        throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE);
                    } else {
                        suppressed.add(t);
                    }
                }
            }
            this.suppressedExceptions = suppressed;
        }
        if (this.stackTrace == null) {
            this.stackTrace = new StackTraceElement[0];
        } else if (this.stackTrace.length != 0) {
            if (this.stackTrace.length == 1 && SentinelHolder.STACK_TRACE_ELEMENT_SENTINEL.equals(this.stackTrace[0])) {
                this.stackTrace = null;
                return;
            }
            StackTraceElement[] stackTraceElementArr = this.stackTrace;
            int length = stackTraceElementArr.length;
            while (i < length) {
                if (stackTraceElementArr[i] == null) {
                    throw new NullPointerException("null StackTraceElement in serial stream. ");
                }
                i++;
            }
        }
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        getOurStackTrace();
        StackTraceElement[] oldStackTrace = this.stackTrace;
        try {
            if (this.stackTrace == null) {
                this.stackTrace = SentinelHolder.STACK_TRACE_SENTINEL;
            }
            s.defaultWriteObject();
            this.stackTrace = oldStackTrace;
        } catch (Throwable th) {
            this.stackTrace = oldStackTrace;
        }
    }

    public final synchronized void addSuppressed(Throwable exception) {
        if (exception == this) {
            throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE, exception);
        } else if (exception == null) {
            throw new NullPointerException(NULL_CAUSE_MESSAGE);
        } else if (this.suppressedExceptions != null) {
            if (this.suppressedExceptions.isEmpty()) {
                this.suppressedExceptions = new ArrayList(1);
            }
            this.suppressedExceptions.add(exception);
        }
    }

    public final synchronized Throwable[] getSuppressed() {
        if (EMPTY_THROWABLE_ARRAY == null) {
            EMPTY_THROWABLE_ARRAY = new Throwable[0];
        }
        if (this.suppressedExceptions == null || this.suppressedExceptions.isEmpty()) {
            return EMPTY_THROWABLE_ARRAY;
        }
        return (Throwable[]) this.suppressedExceptions.toArray(EMPTY_THROWABLE_ARRAY);
    }
}
