package com.android.server.pm.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageParserEx;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.pm.permission.DefaultAppPermission;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.permission.ZosPermissionAdapterEx;
import com.huawei.android.util.SlogEx;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class HwDefaultPermissionGrantPolicy extends DefaultPermissionGrantPolicyEx {
    private static final Set<String> CALENDAR_PERMISSIONS = new ArraySet();
    private static final Set<String> CAMERA_PERMISSIONS = new ArraySet();
    private static final Set<String> CONTACTS_PERMISSIONS = new ArraySet();
    private static final boolean IS_HOSP_ENABLE = SystemPropertiesEx.getBoolean("hw_sc.build.os.enable", false);
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

    public HwDefaultPermissionGrantPolicy(Context context, Looper looper, PermissionManagerServiceEx permissionManager) {
        super(context, looper, permissionManager);
        this.mContext = context;
    }

    public void grantDefaultPermissions(int userId) {
        try {
            grantCustomizedPermissions(userId);
        } catch (IllegalArgumentException e) {
            SlogEx.e(TAG, "Grant customized permission fail by IllegalArgumentException.");
        } catch (Exception e2) {
            SlogEx.e(TAG, "Grant customized permission fail. ");
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(PackageParserEx.PackageEx pkg) {
        return pkg.getApplicationInfo().targetSdkVersion > 22;
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
                SlogEx.e(TAG, "Granting permission fail by IllegalArgumentException.");
            } catch (Exception e2) {
                SlogEx.w(TAG, "Granting permission fail.");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b6  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c2  */
    @SuppressLint({"PreferForInArrayList"})
    private void grantCustomizedPermissions(int userId, DefaultAppPermission perm, boolean isHarmony) {
        String groupName;
        Set<String> permSet;
        Set<String> permSet2;
        String pkgName = perm.mPackageName;
        PackageParserEx.PackageEx pkg = getSystemPackage(pkgName);
        if (pkg == null || pkg.getApplicationInfo() == null) {
            SlogEx.w(TAG, "Invalid pkg name or system component :" + perm.mPackageName);
        } else if (!doesPackageSupportRuntimePermissions(pkg)) {
            SlogEx.d(TAG, "don't support runtime permission:" + perm.mPackageName);
        } else if (!isHarmony && (ApplicationInfoEx.getHwFlags(pkg.getApplicationInfo()) & 1048576) != 0) {
            SlogEx.i(TAG, perm.mPackageName + " is not default application.");
        } else if (!isHarmony || (1048576 & ApplicationInfoEx.getHwFlags(pkg.getApplicationInfo())) != 0) {
            Iterator<DefaultAppPermission.DefaultPermissionGroup> it = perm.mGrantedGroups.iterator();
            while (it.hasNext()) {
                DefaultAppPermission.DefaultPermissionGroup group = it.next();
                if (group.mIsGrant) {
                    boolean isFixed = group.mIsSystemFixed;
                    String groupName2 = group.mName;
                    if (isHarmony) {
                        String permName = ZosPermissionAdapterEx.nativeGetAosPermissionName(groupName2);
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
                                SlogEx.w(TAG, "Granting runtime permission failed.");
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
                        SlogEx.e(TAG, "Granting runtime permission failed by IllegalArgumentException.");
                    } catch (Exception e4) {
                        SlogEx.w(TAG, "Granting runtime permission failed.");
                    }
                } else {
                    return;
                }
            }
            grantOtherPermissions(userId, perm, isHarmony);
        } else {
            SlogEx.i(TAG, perm.mPackageName + " is not harmony application.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0060  */
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
                        String permName = ZosPermissionAdapterEx.nativeGetAosPermissionName(singlePerName2);
                        if (!TextUtils.isEmpty(permName)) {
                            singlePerName = permName.intern();
                            if (SINGLE_PERMISSION_SET.contains(singlePerName)) {
                                SlogEx.i(TAG, "Invalid permission: " + singlePerName);
                            } else {
                                singlePermSet.add(singlePerName);
                                try {
                                    SlogEx.i(TAG, "Granting customized permission:" + single + " to " + pkgName);
                                    Set[] setArr = {singlePermSet};
                                    str = TAG;
                                    try {
                                        grantPermissionsToPackage(pkgName, userId, isSinglefiexd, false, true, setArr);
                                    } catch (IllegalArgumentException e) {
                                    } catch (Exception e2) {
                                        try {
                                            SlogEx.w(str, "Granting runtime permission failed.");
                                        } finally {
                                            singlePermSet.clear();
                                        }
                                    }
                                } catch (IllegalArgumentException e3) {
                                    str = TAG;
                                    SlogEx.e(str, "Granting runtime permission failed by IllegalArgumentException.");
                                    singlePermSet.clear();
                                } catch (Exception e4) {
                                    str = TAG;
                                    SlogEx.w(str, "Granting runtime permission failed.");
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
                SlogEx.e(TAG, "Granting one application permission fail by IllegalArgumentException.");
            } catch (Exception e2) {
                SlogEx.w(TAG, "Granting one application permission fail.");
            }
        }
    }
}
