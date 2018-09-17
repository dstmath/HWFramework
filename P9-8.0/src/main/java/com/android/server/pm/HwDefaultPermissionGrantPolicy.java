package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.pm.DefaultAppPermission.DefaultPermissionGroup;
import com.android.server.pm.DefaultAppPermission.DefaultPermissionSingle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class HwDefaultPermissionGrantPolicy extends DefaultPermissionGrantPolicy {
    private static final Set<String> CALENDAR_PERMISSIONS = new ArraySet();
    private static final Set<String> CAMERA_PERMISSIONS = new ArraySet();
    private static final Set<String> CONTACTS_PERMISSIONS = new ArraySet();
    private static final Set<String> LOCATION_PERMISSIONS = new ArraySet();
    private static final Set<String> MICROPHONE_PERMISSIONS = new ArraySet();
    private static final Set<String> PHONE_PERMISSIONS = new ArraySet();
    private static final Set<String> SENSORS_PERMISSIONS = new ArraySet();
    private static final Set<String> SMS_PERMISSIONS = new ArraySet();
    private static final Set<String> STORAGE_PERMISSIONS = new ArraySet();
    private static final String TAG = "HwDefaultPermissionGrantPolicy";
    private static final Map<String, Set<String>> nameToSet = new HashMap();
    private static final Set<String> singlePermissionSet = new ArraySet();
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
        nameToSet.put("android.permission-group.PHONE", PHONE_PERMISSIONS);
        nameToSet.put("android.permission-group.CONTACTS", CONTACTS_PERMISSIONS);
        nameToSet.put("android.permission-group.LOCATION", LOCATION_PERMISSIONS);
        nameToSet.put("android.permission-group.CALENDAR", CALENDAR_PERMISSIONS);
        nameToSet.put("android.permission-group.SMS", SMS_PERMISSIONS);
        nameToSet.put("android.permission-group.MICROPHONE", MICROPHONE_PERMISSIONS);
        nameToSet.put("android.permission-group.CAMERA", CAMERA_PERMISSIONS);
        nameToSet.put("android.permission-group.SENSORS", SENSORS_PERMISSIONS);
        nameToSet.put("android.permission-group.STORAGE", STORAGE_PERMISSIONS);
        singlePermissionSet.addAll(PHONE_PERMISSIONS);
        singlePermissionSet.addAll(CONTACTS_PERMISSIONS);
        singlePermissionSet.addAll(LOCATION_PERMISSIONS);
        singlePermissionSet.addAll(CALENDAR_PERMISSIONS);
        singlePermissionSet.addAll(SMS_PERMISSIONS);
        singlePermissionSet.addAll(MICROPHONE_PERMISSIONS);
        singlePermissionSet.addAll(CAMERA_PERMISSIONS);
        singlePermissionSet.addAll(SENSORS_PERMISSIONS);
        singlePermissionSet.addAll(STORAGE_PERMISSIONS);
    }

    public HwDefaultPermissionGrantPolicy(Context context, PackageManagerService service) {
        super(service);
        this.mContext = context;
    }

    public void grantDefaultPermissions(int userId) {
        super.grantDefaultPermissions(userId);
        try {
            grantCustomizedPermissions(userId);
        } catch (Exception e) {
            Slog.e(TAG, "Grant customized permission fail. ");
        }
    }

    private void grantCustomizedPermissions(int userId) {
        for (DefaultAppPermission perm : DefaultPermissionPolicyParser.parseConfig(this.mContext).values()) {
            try {
                grantCustomizedPermissions(userId, perm);
            } catch (Exception e) {
                Slog.w(TAG, "Granting permission fail.");
            }
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(Package pkg) {
        return pkg.applicationInfo.targetSdkVersion > 22;
    }

    @SuppressLint({"PreferForInArrayList"})
    private void grantCustomizedPermissions(int userId, DefaultAppPermission perm) {
        String pkgName = perm.mPackageName;
        Package pkg = getSystemPackageLPr(pkgName);
        if (pkg == null) {
            Slog.w(TAG, "Invalid pkg name or system component :" + perm.mPackageName);
        } else if (doesPackageSupportRuntimePermissions(pkg)) {
            for (DefaultPermissionGroup group : perm.mGrantedGroups) {
                if (group.mGrant) {
                    boolean fixed = group.mSystemFixed;
                    String groupName = group.mName;
                    Set<String> permSet = (Set) nameToSet.get(groupName);
                    if (permSet == null) {
                        permSet = new ArraySet();
                        permSet.add(groupName);
                    }
                    try {
                        grantRuntimePermissionsLPw(pkg, permSet, fixed, userId);
                    } catch (Exception e) {
                        Slog.w(TAG, "Granting runtime permission failed.");
                    }
                } else {
                    return;
                }
            }
            if (DefaultPermissionPolicyParser.IS_ATT || DefaultPermissionPolicyParser.IS_SINGLE_PERMISSION) {
                Set<String> singlePermSet = new ArraySet();
                for (DefaultPermissionSingle single : perm.mGrantedSingles) {
                    if (single.mGrant) {
                        boolean singlefiexd = single.mSystemFixed;
                        String singlePerName = single.mName;
                        if (singlePermissionSet.contains(singlePerName)) {
                            singlePermSet.add(singlePerName);
                            try {
                                Slog.i(TAG, "Granting customized permission:" + single + " to " + pkgName);
                                grantRuntimePermissionsLPw(pkg, singlePermSet, singlefiexd, userId);
                            } catch (Exception e2) {
                                Slog.w(TAG, "Granting runtime permission failed.");
                            } finally {
                                singlePermSet.clear();
                            }
                        } else {
                            Slog.i(TAG, "Invalid permission: " + singlePerName);
                        }
                    }
                }
            }
        } else {
            Slog.d(TAG, "don't support runtime permission:" + perm.mPackageName);
        }
    }

    public void grantCustDefaultPermissions(int uid) {
        for (DefaultAppPermission perm : DefaultPermissionPolicyParser.parseCustConfig(this.mContext).values()) {
            try {
                grantCustomizedPermissions(uid, perm);
            } catch (Exception e) {
                Slog.w(TAG, "Granting one application permission fail.");
            }
        }
        grantDefaultPermissionsForGmsCore(uid);
    }

    private void grantDefaultPermissionsForGmsCore(int uid) {
        Package pkg = getSystemPackageLPr(LocationManagerServiceUtil.GOOGLE_GMS_PROCESS);
        if (pkg != null) {
            String groupName = "android.permission-group.LOCATION";
            Set<String> permSet = (Set) nameToSet.get("android.permission-group.LOCATION");
            if (permSet == null) {
                Slog.w(TAG, "customized invalid group name. must check :android.permission-group.LOCATION");
                permSet = new ArraySet();
                permSet.add("android.permission-group.LOCATION");
            }
            try {
                grantRuntimePermissionsLPw(pkg, permSet, false, uid);
            } catch (Exception e) {
                Slog.w(TAG, "Granting runtime permission failed.");
            }
        }
    }
}
