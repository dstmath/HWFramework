package sun.nio.ch;

public final class IOStatus {
    public static final int EOF = -1;
    public static final int INTERRUPTED = -3;
    public static final int THROWN = -5;
    public static final int UNAVAILABLE = -2;
    public static final int UNSUPPORTED = -4;
    public static final int UNSUPPORTED_CASE = -6;

    private IOStatus() {
    }

    public static int normalize(int n) {
        if (n == -2) {
            return 0;
        }
        return n;
    }

    public static boolean check(int n) {
        return n >= -2;
    }

    public static long normalize(long n) {
        if (n == -2) {
            return 0;
        }
        return n;
    }

    public static boolean check(long n) {
        return n >= -2;
    }

    public static boolean checkAll(long n) {
        return n > -1 || n < -6;
    }
}
