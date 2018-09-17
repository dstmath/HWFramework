package libcore.io;

public final class EventLogger {
    private static volatile Reporter REPORTER = new DefaultReporter();

    public interface Reporter {
        void report(int i, Object... objArr);
    }

    private static final class DefaultReporter implements Reporter {
        /* synthetic */ DefaultReporter(DefaultReporter -this0) {
            this();
        }

        private DefaultReporter() {
        }

        public void report(int code, Object... list) {
            StringBuilder sb = new StringBuilder();
            sb.append(code);
            for (Object o : list) {
                sb.append(",");
                sb.append(o.toString());
            }
            System.out.println(sb);
        }
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

    public static void writeEvent(int code, Object... list) {
        getReporter().report(code, list);
    }
}
