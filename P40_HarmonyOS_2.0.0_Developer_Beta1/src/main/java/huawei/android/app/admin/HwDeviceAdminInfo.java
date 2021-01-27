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
import java.util.Iterator;

public class HwDeviceAdminInfo implements IHwDeviceAdminInfo {
    private static final String TAG = "HwDeviceAdminInfo";
    private static final int USES_POLICY_SET_ACCESS_INTERFACE = 27;
    private static final int USES_POLICY_SET_MDM = 25;
    private static final int USES_POLICY_SET_MDM_APN = 11;
    private static final int USES_POLICY_SET_MDM_APP_MANAGER = 8;
    private static final int USES_POLICY_SET_MDM_BLUETOOTH = 10;
    private static final int USES_POLICY_SET_MDM_CAMERA = 26;
    private static final int USES_POLICY_SET_MDM_CAPTURE_SCREEN = 16;
    private static final int USES_POLICY_SET_MDM_CLIPBOARD = 20;
    private static final int USES_POLICY_SET_MDM_CONNECTIVITY = 4;
    private static final int USES_POLICY_SET_MDM_DEVICE_MANAGER = 7;
    private static final int USES_POLICY_SET_MDM_DEVICE_OWNER = 30;
    private static final int USES_POLICY_SET_MDM_EMAIL = 9;
    private static final int USES_POLICY_SET_MDM_FIREWALL = 18;
    private static final int USES_POLICY_SET_MDM_GOOGLE_ACCOUNT = 21;
    private static final int USES_POLICY_SET_MDM_INSTALL_SYS = 28;
    private static final int USES_POLICY_SET_MDM_INSTALL_UNDETACHABLE_SYS = 29;
    private static final int USES_POLICY_SET_MDM_KEYGUARD = 24;
    private static final int USES_POLICY_SET_MDM_LOCATION = 12;
    private static final int USES_POLICY_SET_MDM_MMS = 6;
    private static final int USES_POLICY_SET_MDM_NETWORK_MANAGER = 13;
    private static final int USES_POLICY_SET_MDM_NFC = 3;
    private static final int USES_POLICY_SET_MDM_PHONE = 5;
    private static final int USES_POLICY_SET_MDM_PHONE_MANAGER = 14;
    private static final int USES_POLICY_SET_MDM_SDCARD = 2;
    private static final int USES_POLICY_SET_MDM_SETTINGS_RESTRICTION = 22;
    private static final int USES_POLICY_SET_MDM_TELEPHONY = 23;
    private static final int USES_POLICY_SET_MDM_UPDATESTATE_MANAGER = 19;
    private static final int USES_POLICY_SET_MDM_USB = 1;
    private static final int USES_POLICY_SET_MDM_VOICEASSISTANT = 31;
    private static final int USES_POLICY_SET_MDM_VPN = 17;
    private static final int USES_POLICY_SET_MDM_WIFI = 0;
    private static final int USES_POLICY_SET_SDK_LAUNCHER = 15;
    private static HashMap<String, IHwDeviceAdminInfo.PolicyInfo> sKnownPolicies = new HashMap<>();
    private static ArrayList<IHwDeviceAdminInfo.PolicyInfo> sPoliciesDisplayOrders = new ArrayList<>();
    private ArrayList<IHwDeviceAdminInfo.PolicyInfo> mUsedPoliciesList = new ArrayList<>();

    static {
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo(0, "com.huawei.permission.sec.MDM_WIFI", 33685853, 33685854));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo(1, "com.huawei.permission.sec.MDM_USB", 33685855, 33685856));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo(2, "com.huawei.permission.sec.MDM_SDCARD", 33685857, 33685858));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo(3, "com.huawei.permission.sec.MDM_NFC", 33685859, 33685860));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo(4, "com.huawei.permission.sec.MDM_CONNECTIVITY", 33685861, 33685862));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo(5, "com.huawei.permission.sec.MDM_PHONE", 33685863, 33685864));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo(6, "com.huawei.permission.sec.MDM_MMS", 33685865, 33685866));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_DEVICE_MANAGER, "com.huawei.permission.sec.MDM_DEVICE_MANAGER", 33685867, 33686059));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_APP_MANAGER, "com.huawei.permission.sec.MDM_APP_MANAGEMENT", 33685869, 33685870));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_EMAIL, "com.huawei.permission.sec.MDM_EMAIL", 33685871, 33685872));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_BLUETOOTH, "com.huawei.permission.sec.MDM_BLUETOOTH", 33685873, 33685874));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_APN, "com.huawei.permission.sec.MDM_APN", 33685875, 33685876));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_LOCATION, "com.huawei.permission.sec.MDM_LOCATION", 33685877, 33685878));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_NETWORK_MANAGER, "com.huawei.permission.sec.MDM_NETWORK_MANAGER", 33685879, 33685880));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_PHONE_MANAGER, "com.huawei.permission.sec.MDM_PHONE_MANAGER", 33686199, 33686183));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_SDK_LAUNCHER, "com.huawei.permission.sec.SDK_LAUNCHER", 33686198, 33686181));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_CAPTURE_SCREEN, "com.huawei.permission.sec.MDM_CAPTURE_SCREEN", 33686192, 33686174));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_VPN, "com.huawei.permission.sec.MDM_VPN", 33686204, 33686188));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_FIREWALL, "com.huawei.permission.sec.MDM_FIREWALL", 33686195, 33686177));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_UPDATESTATE_MANAGER, "com.huawei.permission.sec.MDM_UPDATESTATE_MANAGER", 33686202, 33686186));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_CLIPBOARD, "com.huawei.permission.sec.MDM_CLIPBOARD", 33686193, 33686175));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_GOOGLE_ACCOUNT, "com.huawei.permission.sec.MDM_GOOGLE_ACCOUNT", 33686196, 33686178));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_SETTINGS_RESTRICTION, "com.huawei.permission.sec.MDM_SETTINGS_RESTRICTION", 33686200, 33686184));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_TELEPHONY, "com.huawei.permission.sec.MDM_TELEPHONY", 33686201, 33686185));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_KEYGUARD, "com.huawei.permission.sec.MDM_KEYGUARD", 33686197, 33686180));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM, "com.huawei.permission.sec.MDM_INFRARED", 33686189, 33686172));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_CAMERA, "com.huawei.permission.sec.MDM_CAMERA", 33686191, 33686173));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_ACCESS_INTERFACE, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", 33686190, 33686179));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_INSTALL_SYS, "com.huawei.permission.sec.MDM_INSTALL_SYS_APP", 33686057, 33686057));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_INSTALL_UNDETACHABLE_SYS, "com.huawei.permission.sec.MDM_INSTALL_UNDETACHABLE_APP", 33686058, 33686058));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_DEVICE_OWNER, "com.huawei.permission.sec.MDM_DEVICE_OWNER", 33686194, 33686176));
        sPoliciesDisplayOrders.add(new IHwDeviceAdminInfo.PolicyInfo((int) USES_POLICY_SET_MDM_VOICEASSISTANT, "com.huawei.permission.sec.MDM_VOICEASSISTANT", 33686203, 33686187));
        Iterator<IHwDeviceAdminInfo.PolicyInfo> it = sPoliciesDisplayOrders.iterator();
        while (it.hasNext()) {
            IHwDeviceAdminInfo.PolicyInfo pi = it.next();
            sKnownPolicies.put(pi.tag, pi);
        }
    }

    public HwDeviceAdminInfo(Context userContext, ActivityInfo activityInfo) {
        Context context = userContext;
        long id = Binder.clearCallingIdentity();
        try {
            int uid = activityInfo.applicationInfo.uid;
            PackageManager pm = (context.getUserId() != UserHandle.getUserId(uid) ? context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.getUserHandleForUid(uid)) : context).getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(activityInfo.packageName, 4096);
            if (packageInfo == null) {
                Binder.restoreCallingIdentity(id);
            } else if (ArrayUtils.contains(packageInfo.requestedPermissions, "com.huawei.permission.sec.MDM") || ArrayUtils.contains(packageInfo.requestedPermissions, "com.huawei.permission.sec.MDM.v2")) {
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
                Binder.restoreCallingIdentity(id);
            } else {
                Binder.restoreCallingIdentity(id);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "get package info exception init HwDeviceAdminInfo");
        } catch (Exception e2) {
            Log.e(TAG, "other exception init HwDeviceAdminInfo");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
    }

    public ArrayList<IHwDeviceAdminInfo.PolicyInfo> getHwUsedPoliciesList() {
        return this.mUsedPoliciesList;
    }
}
