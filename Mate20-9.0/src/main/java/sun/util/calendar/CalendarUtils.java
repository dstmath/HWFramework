package sun.util.calendar;

import java.net.HttpURLConnection;

public class CalendarUtils {
    public static final boolean isGregorianLeapYear(int gregorianYear) {
        return gregorianYear % 4 == 0 && (gregorianYear % 100 != 0 || gregorianYear % HttpURLConnection.HTTP_BAD_REQUEST == 0);
    }

    public static final boolean isJulianLeapYear(int normalizedJulianYear) {
        return normalizedJulianYear % 4 == 0;
    }

    public static final long floorDivide(long n, long d) {
        return n >= 0 ? n / d : ((n + 1) / d) - 1;
    }

    public static final int floorDivide(int n, int d) {
        return n >= 0 ? n / d : ((n + 1) / d) - 1;
    }

    public static final int floorDivide(int n, int d, int[] r) {
        if (n >= 0) {
            r[0] = n % d;
            return n / d;
        }
        int q = ((n + 1) / d) - 1;
        r[0] = n - (q * d);
        return q;
    }

    public static final int floorDivide(long n, int d, int[] r) {
        if (n >= 0) {
            r[0] = (int) (n % ((long) d));
            return (int) (n / ((long) d));
        }
        int q = (int) (((n + 1) / ((long) d)) - 1);
        r[0] = (int) (n - ((long) (q * d)));
        return q;
    }

    public static final long mod(long x, long y) {
        return x - (floorDivide(x, y) * y);
    }

    public static final int mod(int x, int y) {
        return x - (floorDivide(x, y) * y);
    }

    public static final int amod(int x, int y) {
        int z = mod(x, y);
        return z == 0 ? y : z;
    }

    public static final long amod(long x, long y) {
        long z = mod(x, y);
        return z == 0 ? y : z;
    }

    public static final StringBuilder sprintf0d(StringBuilder sb, int value, int width) {
        long d = (long) value;
        if (d < 0) {
            sb.append('-');
            d = -d;
            width--;
        }
        int n = 10;
        for (int i = 2; i < width; i++) {
            n *= 10;
        }
        for (int i2 = 1; i2 < width && d < ((long) n); i2++) {
            sb.append('0');
            n /= 10;
        }
        sb.append(d);
        return sb;
    }

    public static final StringBuffer sprintf0d(StringBuffer sb, int value, int width) {
        long d = (long) value;
        if (d < 0) {
            sb.append('-');
            d = -d;
            width--;
        }
        int n = 10;
        for (int i = 2; i < width; i++) {
            n *= 10;
        }
        for (int i2 = 1; i2 < width && d < ((long) n); i2++) {
            sb.append('0');
            n /= 10;
        }
        sb.append(d);
        return sb;
    }
}
