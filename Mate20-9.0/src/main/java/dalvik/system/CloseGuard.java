package dalvik.system;

public final class CloseGuard {
    private static volatile Tracker currentTracker = null;
    private static volatile Reporter reporter = new DefaultReporter();
    private static volatile boolean stackAndTrackingEnabled = true;
    private Object closerNameOrAllocationInfo;

    private static final class DefaultReporter implements Reporter {
        private DefaultReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            System.logW(message, allocationSite);
        }
    }

    public interface Reporter {
        void report(String str, Throwable th);
    }

    public interface Tracker {
        void close(Throwable th);

        void open(Throwable th);
    }

    public static CloseGuard get() {
        return new CloseGuard();
    }

    public static void setEnabled(boolean enabled) {
        stackAndTrackingEnabled = enabled;
    }

    public static boolean isEnabled() {
        return stackAndTrackingEnabled;
    }

    public static void setReporter(Reporter rep) {
        if (rep != null) {
            reporter = rep;
            return;
        }
        throw new NullPointerException("reporter == null");
    }

    public static Reporter getReporter() {
        return reporter;
    }

    public static void setTracker(Tracker tracker) {
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
        } else if (!stackAndTrackingEnabled) {
            this.closerNameOrAllocationInfo = closer;
        } else {
            Throwable stack = new Throwable("Explicit termination method '" + closer + "' not called");
            this.closerNameOrAllocationInfo = stack;
            Tracker tracker = currentTracker;
            if (tracker != null) {
                tracker.open(stack);
            }
        }
    }

    public void close() {
        Tracker tracker = currentTracker;
        if (tracker != null && (this.closerNameOrAllocationInfo instanceof Throwable)) {
            tracker.close((Throwable) this.closerNameOrAllocationInfo);
        }
        this.closerNameOrAllocationInfo = null;
    }

    public void warnIfOpen() {
        if (this.closerNameOrAllocationInfo == null) {
            return;
        }
        if (this.closerNameOrAllocationInfo instanceof String) {
            System.logW("A resource failed to call " + ((String) this.closerNameOrAllocationInfo) + ". ");
            return;
        }
        reporter.report("A resource was acquired at attached stack trace but never released. " + "See java.io.Closeable for information on avoiding resource leaks.", (Throwable) this.closerNameOrAllocationInfo);
    }
}
