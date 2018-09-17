package dalvik.system;

public final class CloseGuard {
    private static final DefaultTracker DEFAULT_TRACKER = new DefaultTracker();
    private static volatile boolean ENABLED = true;
    private static final CloseGuard NOOP = new CloseGuard();
    private static volatile Reporter REPORTER = new DefaultReporter();
    private static volatile Tracker currentTracker = DEFAULT_TRACKER;
    private Throwable allocationSite;

    public interface Reporter {
        void report(String str, Throwable th);
    }

    private static final class DefaultReporter implements Reporter {
        /* synthetic */ DefaultReporter(DefaultReporter -this0) {
            this();
        }

        private DefaultReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            System.logW(message, allocationSite);
        }
    }

    public interface Tracker {
        void close(Throwable th);

        void open(Throwable th);
    }

    private static final class DefaultTracker implements Tracker {
        /* synthetic */ DefaultTracker(DefaultTracker -this0) {
            this();
        }

        private DefaultTracker() {
        }

        public void open(Throwable allocationSite) {
        }

        public void close(Throwable allocationSite) {
        }
    }

    public static CloseGuard get() {
        if (ENABLED) {
            return new CloseGuard();
        }
        return NOOP;
    }

    public static void setEnabled(boolean enabled) {
        ENABLED = enabled;
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static void setReporter(Reporter reporter) {
        if (reporter == null) {
            throw new NullPointerException("reporter == null");
        }
        REPORTER = reporter;
    }

    public static Reporter getReporter() {
        return REPORTER;
    }

    public static void setTracker(Tracker tracker) {
        if (tracker == null) {
            throw new NullPointerException("tracker == null");
        }
        currentTracker = tracker;
    }

    public static Tracker getTracker() {
        return currentTracker;
    }

    private CloseGuard() {
    }

    public void open(String closer) {
        if (closer == null) {
            throw new NullPointerException("closer == null");
        } else if (this != NOOP && (ENABLED ^ 1) == 0) {
            this.allocationSite = new Throwable("Explicit termination method '" + closer + "' not called");
            currentTracker.open(this.allocationSite);
        }
    }

    public void close() {
        currentTracker.close(this.allocationSite);
        this.allocationSite = null;
    }

    public void warnIfOpen() {
        if (this.allocationSite != null && (ENABLED ^ 1) == 0) {
            REPORTER.report("A resource was acquired at attached stack trace but never released. See java.io.Closeable for information on avoiding resource leaks.", this.allocationSite);
        }
    }
}
