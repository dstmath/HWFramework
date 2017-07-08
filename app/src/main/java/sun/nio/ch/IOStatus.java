package sun.nio.ch;

final class IOStatus {
    static final int EOF = -1;
    static final int INTERRUPTED = -3;
    static final int THROWN = -5;
    static final int UNAVAILABLE = -2;
    static final int UNSUPPORTED = -4;
    static final int UNSUPPORTED_CASE = -6;

    private IOStatus() {
    }

    static int normalize(int n) {
        if (n == UNAVAILABLE) {
            return 0;
        }
        return n;
    }

    static boolean check(int n) {
        return n >= UNAVAILABLE;
    }

    static long normalize(long n) {
        if (n == -2) {
            return 0;
        }
        return n;
    }

    static boolean check(long n) {
        return n >= -2;
    }

    static boolean checkAll(long n) {
        return n > -1 || n < -6;
    }
}
