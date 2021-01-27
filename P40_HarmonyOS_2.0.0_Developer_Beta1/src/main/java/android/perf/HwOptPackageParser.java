package android.perf;

public interface HwOptPackageParser {
    public static final int OPT_TYPE_ALL = 1;
    public static final int OPT_TYPE_DECODEBITMAP = 1;
    public static final int OPT_TYPE_INVALID = 0;
    public static final int PERF_OPT_DISABLE = 0;
    public static final int PERF_OPT_ENABLE = 1;
    public static final int UNKNOWN = -1;

    void getOptPackages();

    boolean isPerfOptEnable(String str, int i);
}
