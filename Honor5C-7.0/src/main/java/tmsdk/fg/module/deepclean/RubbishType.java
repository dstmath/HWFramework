package tmsdk.fg.module.deepclean;

/* compiled from: Unknown */
public class RubbishType {
    public static final int INDEX_APK = 2;
    public static final int INDEX_SOFTWARE_CACHE = 0;
    public static final int INDEX_SOFT_RUNTIMG_RUBBISH = 1;
    public static final int INDEX_UNINSTALL_RETAIL = 4;
    public static final int MODEL_TYPE_DELETED = 2;
    public static final int MODEL_TYPE_SELECTED = 1;
    public static final int MODEL_TYPE_UNSELECTED = 0;
    public static final int SCAN_FLAG_ALL = 15;
    public static final int SCAN_FLAG_APK = 8;
    public static final int SCAN_FLAG_GENERAL_CACHE = 4;
    public static final int SCAN_FLAG_INSTALL_SOFTWARE_CACHE = 1;
    public static final int SCAN_FLAG_UNINSTALL_SOFTWARE_CACHE = 2;

    public static boolean isOn(int i, int i2) {
        return (i2 & i) == i;
    }

    public static void setScanFlag(int i, int i2) {
    }

    public static void unsetScanFlag(int i, int i2) {
        int i3 = i ^ -1;
    }
}
