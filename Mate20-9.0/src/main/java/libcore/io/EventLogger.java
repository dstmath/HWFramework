package libcore.io;

public final class EventLogger {
    private static volatile Reporter REPORTER = new DefaultReporter();

    private static final class DefaultReporter implements Reporter {
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

    public interface Reporter {
        void report(int i, Object... objArr);
    }

    public static void setReporter(Reporter reporter) {
        if (reporter != null) {
            REPORTER = reporter;
            return;
        }
        throw new NullPointerException("reporter == null");
    }

    public static Reporter getReporter() {
        return REPORTER;
    }

    public static void writeEvent(int code, Object... list) {
        getReporter().report(code, list);
    }
}
