package com.huawei.android;

import com.huawei.annotation.HwSystemApi;

public class ManifestEx {

    public static final class permission {
        @HwSystemApi
        public static final String GRANT_RUNTIME_PERMISSIONS = "android.permission.GRANT_RUNTIME_PERMISSIONS";
        @HwSystemApi
        public static final String INSTALL_GRANT_RUNTIME_PERMISSIONS = "android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS";
        @HwSystemApi
        public static final String MANAGE_PROFILE_AND_DEVICE_OWNERS = "android.permission.MANAGE_PROFILE_AND_DEVICE_OWNERS";
        @HwSystemApi
        public static final String READ_CELL_BROADCASTS = "android.permission.READ_CELL_BROADCASTS";
        public static final String READ_SEARCH_INDEXABLES = "android.permission.READ_SEARCH_INDEXABLES";
        public static final String WRITE_MEDIA_STORAGE = "android.permission.WRITE_MEDIA_STORAGE";
    }
}
