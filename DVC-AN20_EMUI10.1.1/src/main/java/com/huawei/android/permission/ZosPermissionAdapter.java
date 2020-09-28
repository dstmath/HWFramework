package com.huawei.android.permission;

import android.Manifest;
import android.content.pm.PackageParser;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;

public final class ZosPermissionAdapter {
    private static final int INSTALL_FAILED_INVALID_APK = -2;
    private static final int MAX_PERMISSION_NUM = 1024;
    private static final int RESTRICT_PERM_LENGTH = 16;
    private static final String TAG = "ZosPermissionAdapter";
    private static ZosPermissionAdapter sInstance;
    private static boolean sIsLibLoaded;
    private static ArrayList<String> sRestrictPermList = new ArrayList<>(16);

    public native String nativeGetAosPermissionName(String str);

    public native String nativeGetZosPermissionName(String str);

    static {
        sRestrictPermList.add(Manifest.permission.READ_CONTACTS);
        sRestrictPermList.add(Manifest.permission.WRITE_CONTACTS);
        sRestrictPermList.add(Manifest.permission.GET_ACCOUNTS);
        sRestrictPermList.add(Manifest.permission.SEND_SMS);
        sRestrictPermList.add(Manifest.permission.RECEIVE_SMS);
        sRestrictPermList.add(Manifest.permission.READ_SMS);
        sRestrictPermList.add(Manifest.permission.RECEIVE_MMS);
        sRestrictPermList.add(Manifest.permission.RECEIVE_WAP_PUSH);
        sRestrictPermList.add(Manifest.permission.READ_CELL_BROADCASTS);
        sRestrictPermList.add(Manifest.permission.READ_PHONE_STATE);
        sRestrictPermList.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        sRestrictPermList.add(Manifest.permission.READ_CALL_LOG);
        sRestrictPermList.add(Manifest.permission.WRITE_CALL_LOG);
        try {
            System.loadLibrary("PermissionTranslation");
            sIsLibLoaded = true;
            getInstance().nativeGetAosPermissionName("PermissionTranslation");
        } catch (UnsatisfiedLinkError e) {
            sIsLibLoaded = false;
            Slog.e(TAG, "ERROR: Could not load PermissionTranslation.so ");
        }
    }

    private ZosPermissionAdapter() {
    }

    public static ZosPermissionAdapter getInstance() {
        ZosPermissionAdapter zosPermissionAdapter;
        synchronized (ZosPermissionAdapter.class) {
            if (sInstance == null) {
                sInstance = new ZosPermissionAdapter();
            }
            zosPermissionAdapter = sInstance;
        }
        return zosPermissionAdapter;
    }

    public void restrictPermissions(PackageParser.Package pkg) {
        if (pkg == null || pkg.requestedPermissions.size() == 0 || pkg.applicationInfo == null) {
            Slog.i(TAG, "restrictPermissions the requested permissions is empty!");
        } else if (pkg.applicationInfo.isSignedWithPlatformKey()) {
            Slog.i(TAG, "restrictPermissions " + pkg.packageName + " is PlatformSigned");
        } else if (UserHandle.getAppId(pkg.applicationInfo.uid) < 10000) {
            Slog.i(TAG, "restrictPermissions " + pkg.packageName + " is < 10000");
        } else if (pkg.isPrivileged()) {
            Slog.i(TAG, "restrictPermissions " + pkg.packageName + " is privilegedApp");
        } else if (pkg.isSystem()) {
            Slog.i(TAG, "restrictPermissions " + pkg.packageName + " is systemApp");
        } else {
            restrictPermissionsInner(pkg);
        }
    }

    private void restrictPermissionsInner(PackageParser.Package pkg) {
        Slog.e(TAG, "restrictPermissions " + pkg.packageName);
        int size = sRestrictPermList.size();
        for (int i = 0; i < size; i++) {
            String perm = sRestrictPermList.get(i);
            if (!pkg.requestedPermissions.isEmpty() && pkg.requestedPermissions.contains(perm)) {
                Slog.i(TAG, "restrictPermissions " + pkg.packageName + " remove " + perm);
                pkg.requestedPermissions.remove(perm);
            }
        }
    }

    public void translatePermissionName(PackageParser.Package pkg) throws PackageParser.PackageParserException {
        if (!sIsLibLoaded || pkg == null) {
            Slog.e(TAG, "translatePermissionName the lib load failed or pkg is null!");
            return;
        }
        String permName = nativeGetAosPermissionName(pkg.applicationInfo.permission);
        if (!TextUtils.isEmpty(permName)) {
            pkg.applicationInfo.permission = permName.intern();
        }
        Iterator<PackageParser.Permission> it = pkg.permissions.iterator();
        while (it.hasNext()) {
            PackageParser.Permission perm = it.next();
            String permName2 = nativeGetAosPermissionName(perm.info.name);
            if (!TextUtils.isEmpty(permName2)) {
                perm.info.name = permName2.intern();
            }
        }
        Iterator<PackageParser.Activity> it2 = pkg.activities.iterator();
        while (it2.hasNext()) {
            PackageParser.Activity activity = it2.next();
            String permName3 = nativeGetAosPermissionName(activity.info.permission);
            if (!TextUtils.isEmpty(permName3)) {
                activity.info.permission = permName3.intern();
            }
        }
        Iterator<PackageParser.Activity> it3 = pkg.receivers.iterator();
        while (it3.hasNext()) {
            PackageParser.Activity receiver = it3.next();
            String permName4 = nativeGetAosPermissionName(receiver.info.permission);
            if (!TextUtils.isEmpty(permName4)) {
                receiver.info.permission = permName4.intern();
            }
        }
        Iterator<PackageParser.Provider> it4 = pkg.providers.iterator();
        while (it4.hasNext()) {
            PackageParser.Provider provider = it4.next();
            String permName5 = nativeGetAosPermissionName(provider.info.readPermission);
            if (!TextUtils.isEmpty(permName5)) {
                provider.info.readPermission = permName5.intern();
            }
            String permName6 = nativeGetAosPermissionName(provider.info.writePermission);
            if (!TextUtils.isEmpty(permName6)) {
                provider.info.writePermission = permName6.intern();
            }
        }
        Iterator<PackageParser.Service> it5 = pkg.services.iterator();
        while (it5.hasNext()) {
            PackageParser.Service service = it5.next();
            String permName7 = nativeGetAosPermissionName(service.info.permission);
            if (!TextUtils.isEmpty(permName7)) {
                service.info.permission = permName7.intern();
            }
        }
        translateRequestPermissions(pkg);
    }

    private void translateRequestPermissions(PackageParser.Package pkg) throws PackageParser.PackageParserException {
        int size = pkg.requestedPermissions.size();
        if (size <= 1024) {
            for (int i = 0; i < size; i++) {
                String permName = pkg.requestedPermissions.get(i);
                String changedPermName = nativeGetAosPermissionName(permName);
                Slog.e(TAG, "translateRequestPermissions nativeGetAosPermissionName z perm " + permName + " changedPermName " + changedPermName);
                if (changedPermName != null) {
                    pkg.requestedPermissions.set(i, changedPermName.intern());
                }
            }
            return;
        }
        Slog.e(TAG, "translatePermissionName the requestedPermissions size is: " + size + " larger than " + 1024);
        throw new PackageParser.PackageParserException(-2, "pkg requestedPermissions size is larger than 1024");
    }
}
