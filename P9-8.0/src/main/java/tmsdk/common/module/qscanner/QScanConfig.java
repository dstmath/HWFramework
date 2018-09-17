package tmsdk.common.module.qscanner;

public interface QScanConfig {
    public static final int EADT_BANNER = 13;
    public static final int EADT_FLOAT = 16;
    public static final int EADT_NOTIFY = 7;
    public static final int EADT_RECOMMENDATION = 17;
    public static final int EADT_SPOT = 14;
    public static final int EADT_WALL = 15;
    public static final int ERR_EXPIRED = -101;
    public static final int ERR_FROM_SERVER = -205;
    public static final int ERR_ILLEGAL_ARG = -102;
    public static final int ERR_INIT = -104;
    public static final int ERR_NATIVE_LOAD = -103;
    public static final int ERR_UNKNOW = -999;
    public static final int ERT_FAST = 4;
    public static final int ERT_FULL_STEP2 = 12;
    public static final int ERT_INSTALL = 3;
    public static final int ERT_NONE = 0;
    public static final int RET_NOT_OFFICIAL = 258;
    public static final int RET_OTHER_RISKS = 262;
    public static final int RET_PAY_RISKS = 259;
    public static final int RET_SAFE = 257;
    public static final int RET_STEALACCOUNT_RISKS = 261;
    public static final int RET_UNKNOWN = 263;
    public static final int RET_VIRUSES = 260;
    public static final int SCAN_CLOUD = 4;
    public static final int SCAN_LOCAL = 2;
    public static final int S_OK = 0;
    public static final int W_CANNOT_FEATCH_PKGINFO = -202;
    public static final int W_GET_SDCARD_QSCANNER = -203;
    public static final int W_IS_SCANNING = -201;
    public static final int W_NOT_INIT = -204;
    public static final int W_TIMEOUT = -206;
}
