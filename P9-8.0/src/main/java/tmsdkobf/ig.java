package tmsdkobf;

public class ig {
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
}
