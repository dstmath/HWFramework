package com.android.server.pm.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageParser;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.server.pm.permission.DefaultAppPermission;
import com.huawei.android.permission.ZosPermissionAdapter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class HwDefaultPermissionGrantPolicy extends DefaultPermissionGrantPolicy {
    private static final Set<String> CALENDAR_PERMISSIONS = new ArraySet();
    private static final Set<String> CAMERA_PERMISSIONS = new ArraySet();
    private static final Set<String> CONTACTS_PERMISSIONS = new ArraySet();
    private static final boolean IS_HOSP_ENABLE = SystemProperties.getBoolean("ro.build.harmony.enable", false);
    private static final Set<String> LOCATION_PERMISSIONS = new ArraySet();
    private static final Set<String> MICROPHONE_PERMISSIONS = new ArraySet();
    private static final Map<String, Set<String>> NAME_TO_SET = new HashMap();
    private static final Set<String> PHONE_PERMISSIONS = new ArraySet();
    private static final Set<String> SENSORS_PERMISSIONS = new ArraySet();
    private static final Set<String> SINGLE_PERMISSION_SET = new ArraySet();
    private static final Set<String> SMS_PERMISSIONS = new ArraySet();
    private static final Set<String> STORAGE_PERMISSIONS = new ArraySet();
    private static final String TAG = "HwDefaultPermissionGrantPolicy";
    private Context mContext = null;
    private ZosPermissionAdapter zosPermissionAdapter;

    static {
        PHONE_PERMISSIONS.add("android.permission.READ_PHONE_STATE");
        PHONE_PERMISSIONS.add("android.permission.CALL_PHONE");
        PHONE_PERMISSIONS.add("android.permission.READ_CALL_LOG");
        PHONE_PERMISSIONS.add("android.permission.WRITE_CALL_LOG");
        PHONE_PERMISSIONS.add("com.android.voicemail.permission.ADD_VOICEMAIL");
        PHONE_PERMISSIONS.add("android.permission.USE_SIP");
        PHONE_PERMISSIONS.add("android.permission.PROCESS_OUTGOING_CALLS");
        CONTACTS_PERMISSIONS.add("android.permission.READ_CONTACTS");
        CONTACTS_PERMISSIONS.add("android.permission.WRITE_CONTACTS");
        CONTACTS_PERMISSIONS.add("android.permission.GET_ACCOUNTS");
        LOCATION_PERMISSIONS.add("android.permission.ACCESS_FINE_LOCATION");
        LOCATION_PERMISSIONS.add("android.permission.ACCESS_COARSE_LOCATION");
        CALENDAR_PERMISSIONS.add("android.permission.READ_CALENDAR");
        CALENDAR_PERMISSIONS.add("android.permission.WRITE_CALENDAR");
        SMS_PERMISSIONS.add("android.permission.SEND_SMS");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_SMS");
        SMS_PERMISSIONS.add("android.permission.READ_SMS");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_WAP_PUSH");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_MMS");
        SMS_PERMISSIONS.add("android.permission.READ_CELL_BROADCASTS");
        MICROPHONE_PERMISSIONS.add("android.permission.RECORD_AUDIO");
        CAMERA_PERMISSIONS.add("android.permission.CAMERA");
        SENSORS_PERMISSIONS.add("android.permission.BODY_SENSORS");
        STORAGE_PERMISSIONS.add("android.permission.READ_EXTERNAL_STORAGE");
        STORAGE_PERMISSIONS.add("android.permission.WRITE_EXTERNAL_STORAGE");
        NAME_TO_SET.put("android.permission-group.PHONE", PHONE_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.CONTACTS", CONTACTS_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.LOCATION", LOCATION_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.CALENDAR", CALENDAR_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.SMS", SMS_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.MICROPHONE", MICROPHONE_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.CAMERA", CAMERA_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.SENSORS", SENSORS_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.STORAGE", STORAGE_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(PHONE_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(CONTACTS_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(LOCATION_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(CALENDAR_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(SMS_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(MICROPHONE_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(CAMERA_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(SENSORS_PERMISSIONS);
        SINGLE_PERMISSION_SET.addAll(STORAGE_PERMISSIONS);
    }

    public HwDefaultPermissionGrantPolicy(Context context, Looper looper, PermissionManagerService permissionManager) {
        super(context, looper, permissionManager);
        this.mContext = context;
        this.zosPermissionAdapter = ZosPermissionAdapter.getInstance();
    }

    public void grantDefaultPermissions(int userId) {
        HwDefaultPermissionGrantPolicy.super.grantDefaultPermissions(userId);
        try {
            grantCustomizedPermissions(userId);
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Grant customized permission fail by IllegalArgumentException.");
        } catch (Exception e2) {
            Slog.e(TAG, "Grant customized permission fail. ");
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(PackageParser.Package pkg) {
        return pkg.applicationInfo.targetSdkVersion > 22;
    }

    private void grantCustomizedPermissions(int userId) {
        Log.i(TAG, "Granting customized permissions for user " + userId);
        grantPermissions(userId, DefaultPermissionPolicyParser.parseConfig(this.mContext), false);
        if (IS_HOSP_ENABLE) {
            Log.i(TAG, "Granting harmony permissions for user " + userId);
            grantPermissions(userId, DefaultPermissionPolicyParser.parseHarmonyConfig(this.mContext), true);
        }
    }

    private void grantPermissions(int userId, Map permissionMap, boolean isHarmony) {
        for (DefaultAppPermission perm : permissionMap.values()) {
            try {
                grantCustomizedPermissions(userId, perm, isHarmony);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "Granting permission fail by IllegalArgumentException.");
            } catch (Exception e2) {
                Slog.w(TAG, "Granting permission fail.");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ba  */
    @SuppressLint({"PreferForInArrayList"})
    private void grantCustomizedPermissions(int userId, DefaultAppPermission perm, boolean isHarmony) {
        String groupName;
        Set<String> permSet;
        Set<String> permSet2;
        String pkgName = perm.mPackageName;
        PackageParser.Package pkg = getSystemPackage(pkgName);
        if (pkg == null || pkg.applicationInfo == null) {
            Slog.w(TAG, "Invalid pkg name or system component :" + perm.mPackageName);
        } else if (!doesPackageSupportRuntimePermissions(pkg)) {
            Slog.d(TAG, "don't support runtime permission:" + perm.mPackageName);
        } else if (!isHarmony && (pkg.applicationInfo.hwFlags & 1048576) != 0) {
            Slog.i(TAG, perm.mPackageName + " is not default application.");
        } else if (!isHarmony || (1048576 & pkg.applicationInfo.hwFlags) != 0) {
            Iterator<DefaultAppPermission.DefaultPermissionGroup> it = perm.mGrantedGroups.iterator();
            while (it.hasNext()) {
                DefaultAppPermission.DefaultPermissionGroup group = it.next();
                if (group.mIsGrant) {
                    boolean isFixed = group.mIsSystemFixed;
                    String groupName2 = group.mName;
                    if (isHarmony) {
                        String permName = this.zosPermissionAdapter.nativeGetAosPermissionName(groupName2);
                        if (!TextUtils.isEmpty(permName)) {
                            groupName = permName.intern();
                            permSet = NAME_TO_SET.get(groupName);
                            if (permSet != null) {
                                Set<String> arraySet = new ArraySet<>();
                                arraySet.add(groupName);
                                permSet2 = arraySet;
                            } else {
                                permSet2 = permSet;
                            }
                            try {
                                grantPermissionsToPackage(pkgName, userId, isFixed, false, true, new Set[]{permSet2});
                            } catch (IllegalArgumentException e) {
                            } catch (Exception e2) {
                                Slog.w(TAG, "Granting runtime permission failed.");
                            }
                        }
                    }
                    groupName = groupName2;
                    permSet = NAME_TO_SET.get(groupName);
                    if (permSet != null) {
                    }
                    try {
                        grantPermissionsToPackage(pkgName, userId, isFixed, false, true, new Set[]{permSet2});
                    } catch (IllegalArgumentException e3) {
                        Slog.e(TAG, "Granting runtime permission failed by IllegalArgumentException.");
                    } catch (Exception e4) {
                        Slog.w(TAG, "Granting runtime permission failed.");
                    }
                } else {
                    return;
                }
            }
            grantOtherPermissions(userId, perm, isHarmony);
        } else {
            Slog.i(TAG, perm.mPackageName + " is not harmony application.");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ac, code lost:
        r3 = com.android.server.pm.permission.HwDefaultPermissionGrantPolicy.TAG;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b5, code lost:
        r17 = com.android.server.pm.permission.HwDefaultPermissionGrantPolicy.TAG;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b0 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:21:0x006e] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00b4 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:21:0x006e] */
    private void grantOtherPermissions(int userId, DefaultAppPermission perm, boolean isHarmony) {
        String singlePerName;
        String str;
        String pkgName = perm.mPackageName;
        if (DefaultPermissionPolicyParser.IS_ATT || DefaultPermissionPolicyParser.IS_SINGLE_PERMISSION) {
            Set<String> singlePermSet = new ArraySet<>();
            Iterator<DefaultAppPermission.DefaultPermissionSingle> it = perm.mGrantedSingles.iterator();
            while (it.hasNext()) {
                DefaultAppPermission.DefaultPermissionSingle single = it.next();
                if (single.mIsGrant) {
                    boolean isSinglefiexd = single.mIsSystemFixed;
                    String singlePerName2 = single.mName;
                    if (isHarmony) {
                        String permName = this.zosPermissionAdapter.nativeGetAosPermissionName(singlePerName2);
                        if (!TextUtils.isEmpty(permName)) {
                            singlePerName = permName.intern();
                            if (SINGLE_PERMISSION_SET.contains(singlePerName)) {
                                Slog.i(TAG, "Invalid permission: " + singlePerName);
                            } else {
                                singlePermSet.add(singlePerName);
                                try {
                                    Slog.i(TAG, "Granting customized permission:" + single + " to " + pkgName);
                                    Set[] setArr = {singlePermSet};
                                    String str2 = TAG;
                                    try {
                                        grantPermissionsToPackage(pkgName, userId, isSinglefiexd, false, true, setArr);
                                    } catch (IllegalArgumentException e) {
                                        str = str2;
                                    } catch (Exception e2) {
                                        try {
                                            Slog.w(str2, "Granting runtime permission failed.");
                                            singlePermSet.clear();
                                        } catch (Throwable th) {
                                            Throwable th2 = th;
                                            singlePermSet.clear();
                                            throw th2;
                                        }
                                    }
                                } catch (IllegalArgumentException e3) {
                                    str = TAG;
                                    Slog.e(str, "Granting runtime permission failed by IllegalArgumentException.");
                                    singlePermSet.clear();
                                } catch (Exception e4) {
                                } catch (Throwable th3) {
                                }
                                singlePermSet.clear();
                            }
                        }
                    }
                    singlePerName = singlePerName2;
                    if (SINGLE_PERMISSION_SET.contains(singlePerName)) {
                    }
                }
            }
        }
    }

    public void grantCustDefaultPermissions(int uid) {
        for (DefaultAppPermission perm : DefaultPermissionPolicyParser.parseCustConfig(this.mContext).values()) {
            try {
                grantCustomizedPermissions(uid, perm, false);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "Granting one application permission fail by IllegalArgumentException.");
            } catch (Exception e2) {
                Slog.w(TAG, "Granting one application permission fail.");
            }
        }
    }
}
