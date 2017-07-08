package huawei.android.app.admin;

import android.app.admin.DeviceAdminInfo.PolicyInfo;
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
import huawei.android.view.inputmethod.HwSecImmHelper;
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
    public static final int USES_POLICY_SET_MDM_MMS = 6;
    public static final int USES_POLICY_SET_MDM_NFC = 3;
    public static final int USES_POLICY_SET_MDM_PHONE = 5;
    public static final int USES_POLICY_SET_MDM_SDCARD = 2;
    public static final int USES_POLICY_SET_MDM_USB = 1;
    public static final int USES_POLICY_SET_MDM_WIFI = 0;
    private static HashMap<String, PolicyInfo> sKnownPolicies;
    private static ArrayList<PolicyInfo> sPoliciesDisplayOrder;
    private ArrayList<PolicyInfo> mUsedPoliciesList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.app.admin.HwDeviceAdminInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.app.admin.HwDeviceAdminInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.app.admin.HwDeviceAdminInfo.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HwDeviceAdminInfo(Context context, ActivityInfo activityInfo) {
        this.mUsedPoliciesList = new ArrayList();
        long id = Binder.clearCallingIdentity();
        try {
            int uid = activityInfo.applicationInfo.uid;
            if (context.getUserId() != UserHandle.getUserId(uid)) {
                context = context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.getUserHandleForUid(uid));
            }
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(activityInfo.packageName, HwSecImmHelper.SECURE_IME_NO_HIDE_FLAG);
            if (packageInfo != null && (ArrayUtils.contains(packageInfo.requestedPermissions, "com.huawei.permission.sec.MDM") || ArrayUtils.contains(packageInfo.requestedPermissions, "com.huawei.permission.sec.MDM.v2"))) {
                for (String permission : HwManifest.PERMIISONS_LIST) {
                    if (pm.checkPermission(permission, activityInfo.packageName) == 0) {
                        PolicyInfo pi = (PolicyInfo) sKnownPolicies.get(permission);
                        if (pi != null) {
                            this.mUsedPoliciesList.add(pi);
                        } else {
                            Log.i(TAG, "permission = " + permission + " is not KnownPolicies");
                        }
                    }
                }
            }
            Binder.restoreCallingIdentity(id);
        } catch (Exception e) {
            Log.e(TAG, "error init HwDeviceAdminInfo", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
        }
    }

    public ArrayList<PolicyInfo> getHwUsedPoliciesList() {
        return this.mUsedPoliciesList;
    }
}
