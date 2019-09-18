package huawei.android.app.admin;

import android.app.admin.DeviceAdminInfo;
import android.app.admin.HwManifest;
import android.app.admin.IHwDeviceAdminInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import java.util.ArrayList;
import java.util.HashMap;

public class HwDeviceAdminInfo implements IHwDeviceAdminInfo {
    public static final String TAG = "HwDeviceAdminInfo";
    public static final int USES_POLICY_SET_MDM_APN = 11;
    public static final int USES_POLICY_SET_MDM_APP_MANAGER = 8;
    public static final int USES_POLICY_SET_MDM_BLUETOOTH = 10;
    public static final int USES_POLICY_SET_MDM_CONNECTIVITY = 4;
    public static final int USES_POLICY_SET_MDM_DEVICE_MANAGER = 7;
    public static final int USES_POLICY_SET_MDM_EMAIL = 9;
    public static final int USES_POLICY_SET_MDM_INSTALL_SYS = 12;
    public static final int USES_POLICY_SET_MDM_INSTALL_UNDETACHABLE_SYS = 13;
    public static final int USES_POLICY_SET_MDM_MMS = 6;
    public static final int USES_POLICY_SET_MDM_NFC = 3;
    public static final int USES_POLICY_SET_MDM_PHONE = 5;
    public static final int USES_POLICY_SET_MDM_SDCARD = 2;
    public static final int USES_POLICY_SET_MDM_USB = 1;
    public static final int USES_POLICY_SET_MDM_WIFI = 0;
    private static HashMap<String, DeviceAdminInfo.PolicyInfo> sKnownPolicies = new HashMap<>();
    private static ArrayList<DeviceAdminInfo.PolicyInfo> sPoliciesDisplayOrder = new ArrayList<>();
    private ArrayList<DeviceAdminInfo.PolicyInfo> mUsedPoliciesList = new ArrayList<>();

    static {
        int i = 0;
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(0, "com.huawei.permission.sec.MDM_WIFI", 33685853, 33685854));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(1, "com.huawei.permission.sec.MDM_USB", 33685855, 33685856));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(2, "com.huawei.permission.sec.MDM_SDCARD", 33685857, 33685858));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(3, "com.huawei.permission.sec.MDM_NFC", 33685859, 33685860));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(4, "com.huawei.permission.sec.MDM_CONNECTIVITY", 33685861, 33685862));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(5, "com.huawei.permission.sec.MDM_PHONE", 33685863, 33685864));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(6, "com.huawei.permission.sec.MDM_MMS", 33685865, 33685866));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(7, "com.huawei.permission.sec.MDM_DEVICE_MANAGER", 33685867, 33685868));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(8, "com.huawei.permission.sec.MDM_APP_MANAGEMENT", 33685869, 33685870));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(9, "com.huawei.permission.sec.MDM_EMAIL", 33685871, 33685872));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(10, "com.huawei.permission.sec.MDM_BLUETOOTH", 33685873, 33685874));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(11, "com.huawei.permission.sec.MDM_APN", 33685875, 33685876));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(12, "com.huawei.permission.sec.MDM_INSTALL_SYS_APP", 33686057, 33686057));
        sPoliciesDisplayOrder.add(new DeviceAdminInfo.PolicyInfo(13, "com.huawei.permission.sec.MDM_INSTALL_UNDETACHABLE_APP", 33686058, 33686058));
        int sPoliciesDisplayOrderSize = sPoliciesDisplayOrder.size();
        while (true) {
            int i2 = i;
            if (i2 < sPoliciesDisplayOrderSize) {
                DeviceAdminInfo.PolicyInfo pi = sPoliciesDisplayOrder.get(i2);
                sKnownPolicies.put(pi.tag, pi);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public HwDeviceAdminInfo(Context context, ActivityInfo activityInfo) {
        long id = Binder.clearCallingIdentity();
        try {
            int uid = activityInfo.applicationInfo.uid;
            PackageManager pm = (context.getUserId() != UserHandle.getUserId(uid) ? context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.getUserHandleForUid(uid)) : context).getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(activityInfo.packageName, 4096);
            if (packageInfo != null && (ArrayUtils.contains(packageInfo.requestedPermissions, "com.huawei.permission.sec.MDM") || ArrayUtils.contains(packageInfo.requestedPermissions, "com.huawei.permission.sec.MDM.v2"))) {
                for (String permission : HwManifest.PERMIISONS_LIST) {
                    if (pm.checkPermission(permission, activityInfo.packageName) == 0) {
                        DeviceAdminInfo.PolicyInfo pi = sKnownPolicies.get(permission);
                        if (pi != null) {
                            this.mUsedPoliciesList.add(pi);
                        } else {
                            Log.i(TAG, "permission = " + permission + " is not KnownPolicies");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error init HwDeviceAdminInfo", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
        Binder.restoreCallingIdentity(id);
    }

    public ArrayList<DeviceAdminInfo.PolicyInfo> getHwUsedPoliciesList() {
        return this.mUsedPoliciesList;
    }
}
