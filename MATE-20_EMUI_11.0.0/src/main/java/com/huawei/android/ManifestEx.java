package com.huawei.android;

import com.huawei.annotation.HwSystemApi;

public class ManifestEx {

    public static final class permission {
        @HwSystemApi
        public static final String GRANT_RUNTIME_PERMISSIONS = "android.permission.GRANT_RUNTIME_PERMISSIONS";
        public static final String READ_SEARCH_INDEXABLES = "android.permission.READ_SEARCH_INDEXABLES";
        public static final String WRITE_MEDIA_STORAGE = "android.permission.WRITE_MEDIA_STORAGE";
    }
}
