package huawei.android.app.admin;

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
    public static final int USES_POLICY_SET_ACCESS_INTERFACE = 27;
    public static final int USES_POLICY_SET_MDM = 25;
    public static final int USES_POLICY_SET_MDM_APN = 11;
    public static final int USES_POLICY_SET_MDM_APP_MANAGER = 8;
    public static final int USES_POLICY_SET_MDM_BLUETOOTH = 10;
    public static final int USES_POLICY_SET_MDM_CAMERA = 26;
    public static final int USES_POLICY_SET_MDM_CAPTURE_SCREEN = 16;
    public static final int USES_POLICY_SET_MDM_CLIPBOARD = 20;
    public static final int USES_POLICY_SET_MDM_CONNECTIVITY = 4;
    public static final int USES_POLICY_SET_MDM_DEVICE_MANAGER = 7;
    public static final int USES_POLICY_SET_MDM_EMAIL = 9;
    public static final int USES_POLICY_SET_MDM_FIREWALL = 18;
    public static final int USES_POLICY_SET_MDM_GOOGLE_ACCOUNT = 21;
    public static final int USES_POLICY_SET_MDM_INSTALL_SYS = 28;
    public static final int USES_POLICY_SET_MDM_INSTALL_UNDETACHABLE_SYS = 29;
    public static final int USES_POLICY_SET_MDM_KEYGUARD = 24;
    public static final int USES_POLICY_SET_MDM_LOCATION = 12;
    public static final int USES_POLICY_SET_MDM_MMS = 6;
    public static final int USES_POLICY_SET_MDM_NETWORK_MANAGER = 13;
    public static final int USES_POLICY_SET_MDM_NFC = 3;
    public static final int USES_POLICY_SET_MDM_PHONE = 5;
    public static final int USES_POLICY_SET_MDM_PHONE_MANAGER = 14;
    public static final int USES_POLICY_SET_MDM_SDCARD = 2;
    public static final int USES_POLICY_SET_MDM_SETTINGS_RESTRICTION = 22;
    public static final int USES_POLICY_SET_MDM_TELEPHONY = 23;
    public static final int USES_POLICY_SET_MDM_UPDATESTATE_MANAGER = 19;
    public static final int USES_POLICY_SET_MDM_USB = 1;
    public static final int USES_POLICY_SET_MDM_VPN = 17;
    public static final int USES_POLICY_SET_MDM_WIFI = 0;
    public static final int USES_POLICY_SET_SDK_LAUNCHER = 15;
    private static HashMap<String, IHwDeviceAdminInfo.PolicyInfo> sKnownPolicies = new HashMap<>();
    private static ArrayList<IHwDeviceAdminInfo.PolicyInfo> sPoliciesDisplayOrder = new ArrayList<>();
    private ArrayList<IHwDeviceAdminInfo.PolicyInfo> mUsedPoliciesList = new ArrayList<>();

    static {
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(0, "com.huawei.permission.sec.MDM_WIFI", 33685853, 33685854));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(1, "com.huawei.permission.sec.MDM_USB", 33685855, 33685856));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(2, "com.huawei.permission.sec.MDM_SDCARD", 33685857, 33685858));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(3, "com.huawei.permission.sec.MDM_NFC", 33685859, 33685860));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(4, "com.huawei.permission.sec.MDM_CONNECTIVITY", 33685861, 33685862));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(5, "com.huawei.permission.sec.MDM_PHONE", 33685863, 33685864));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(6, "com.huawei.permission.sec.MDM_MMS", 33685865, 33685866));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(7, "com.huawei.permission.sec.MDM_DEVICE_MANAGER", 33685867, 33686059));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(8, "com.huawei.permission.sec.MDM_APP_MANAGEMENT", 33685869, 33685870));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(9, "com.huawei.permission.sec.MDM_EMAIL", 33685871, 33685872));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(10, "com.huawei.permission.sec.MDM_BLUETOOTH", 33685873, 33685874));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(11, "com.huawei.permission.sec.MDM_APN", 33685875, 33685876));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(12, "com.huawei.permission.sec.MDM_LOCATION", 33685877, 33685878));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(13, "com.huawei.permission.sec.MDM_NETWORK_MANAGER", 33685879, 33685880));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(14, "com.huawei.permission.sec.MDM_PHONE_MANAGER", 33686160, 33686146));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(15, "com.huawei.permission.sec.SDK_LAUNCHER", 33686159, 33686144));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(16, "com.huawei.permission.sec.MDM_CAPTURE_SCREEN", 33686154, 33686138));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(17, "com.huawei.permission.sec.MDM_VPN", 33686164, 33686150));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(18, "com.huawei.permission.sec.MDM_FIREWALL", 33686156, 33686140));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(19, "com.huawei.permission.sec.MDM_UPDATESTATE_MANAGER", 33686163, 33686149));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(20, "com.huawei.permission.sec.MDM_CLIPBOARD", 33686155, 33686139));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(21, "com.huawei.permission.sec.MDM_GOOGLE_ACCOUNT", 33686157, 33686141));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(22, "com.huawei.permission.sec.MDM_SETTINGS_RESTRICTION", 33686161, 33686147));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(23, "com.huawei.permission.sec.MDM_TELEPHONY", 33686162, 33686148));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(24, "com.huawei.permission.sec.MDM_KEYGUARD", 33686158, 33686143));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(25, "com.huawei.permission.sec.MDM", 33686151, 33686136));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(26, "com.huawei.permission.sec.MDM_CAMERA", 33686153, 33686137));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(27, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", 33686152, 33686142));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(28, "com.huawei.permission.sec.MDM_INSTALL_SYS_APP", 33686057, 33686057));
        sPoliciesDisplayOrder.add(new IHwDeviceAdminInfo.PolicyInfo(29, "com.huawei.permission.sec.MDM_INSTALL_UNDETACHABLE_APP", 33686058, 33686058));
        int sPoliciesDisplayOrderSize = sPoliciesDisplayOrder.size();
        for (int i = 0; i < sPoliciesDisplayOrderSize; i++) {
            IHwDeviceAdminInfo.PolicyInfo pi = sPoliciesDisplayOrder.get(i);
            sKnownPolicies.put(pi.tag, pi);
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
                        IHwDeviceAdminInfo.PolicyInfo pi = sKnownPolicies.get(permission);
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

    public ArrayList<IHwDeviceAdminInfo.PolicyInfo> getHwUsedPoliciesList() {
        return this.mUsedPoliciesList;
    }
}
