package com.huawei.msdp;

import com.android.server.devicepolicy.StorageUtils;

public final class BuildConfig {
    public static final String APPLICATION_ID = "com.huawei.msdp";
    public static final String BUILD_TYPE = "debug";
    public static final boolean DEBUG = Boolean.parseBoolean(StorageUtils.SDCARD_ROMOUNTED_STATE);
    public static final String FLAVOR = "";
    public static final int VERSION_CODE = 1;
    public static final String VERSION_NAME = "1.0";
}
