package com.huawei.android.permission;

public class ZosPermissionAdapterEx {
    public static String nativeGetAosPermissionName(String zosPermissionName) {
        return ZosPermissionAdapter.getInstance().nativeGetAosPermissionName(zosPermissionName);
    }
}
