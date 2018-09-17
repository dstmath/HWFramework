package tmsdk.common;

public class CallerIdent {
    public static final long DEFAULT = 9223372032559808512L;
    public static final long MERI = 0;
    public static final long TMS = 4294967296L;
    public static final int TMS_IDENT_BG = 2;
    public static final int TMS_IDENT_FG = 1;
    public static final int TMS_IDENT_NONE = 3;

    public static long getIdent(int i, long j) {
        Object obj = null;
        if (i >= 0) {
            if (j >= 0) {
                obj = 1;
            }
            if (obj != null) {
                return ((long) i) + j;
            }
            throw new IllegalStateException("thread pool parent-ident is illegal");
        }
        throw new IllegalStateException("thread pool sub-ident is negative");
    }

    public static long getParentIdent(long j) {
        return (j >>> 32) << 32;
    }

    public static int getSubIdent(long j) {
        return (int) j;
    }
}
