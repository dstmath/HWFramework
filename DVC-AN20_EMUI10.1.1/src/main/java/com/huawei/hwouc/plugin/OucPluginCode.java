package com.huawei.hwouc.plugin;

public class OucPluginCode {
    public static final int DOWNLOAD_FAIL = -6;
    public static final int DOWNLOAD_PAUSE = 3;
    public static final int DOWNLOAD_START = 2;
    public static final int DOWNLOAD_SUCCESS = 4;
    public static final int FAIL = -11;
    public static final int INSTALL_START = 10;
    public static final int MERGE_DIFF_FAIL = -9;
    public static final int MERGE_DIFF_SUCCESS = 8;
    public static final int MERGE_VERIFY_FAIL = -10;
    public static final int MERGE_VERIFY_SUCCESS = 9;
    public static final int NO_NETWORK = -2;
    public static final int NO_NEW_VERSION = -4;
    public static final int PARAMETER_EXCEPTION = -1;
    public static final int REQUEST_EXCEPTION = -3;
    public static final int REQUEST_SUCCESS = 1;
    public static final int SPACE_NOT_ENOUGH = -5;
    public static final int SUCCESS = 0;
    public static final int UNZIP_FAIL = -8;
    public static final int UNZIP_START = 6;
    public static final int UNZIP_SUCCESS = 7;
    public static final int VERIFY_FAIL = -7;
    public static final int VERIFY_SUCCESS = 5;

    public static class InstallMode {
        public static final int KILL = 0;
        public static final int NOT_KILL = 1;
    }

    public static class TriggerMode {
        public static final int AUTO_MOBILE = 4;
        public static final int AUTO_WIFI = 3;
        public static final int MANUAL_MOBILE = 2;
        public static final int MANUAL_WIFI = 1;
    }
}
