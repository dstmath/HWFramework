package com.huawei.server.security.hsm;

import android.app.AppOpsManager;
import android.content.Context;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hsm.permission.monitor.PermRecordHandler;
import com.huawei.server.security.permissionmanager.HwPermDbAdapter;
import com.huawei.server.security.permissionmanager.util.PermissionType;

public class HwAddViewHelper extends DefaultHwAddViewHelper {
    public static final int ACTIVITY_BG_MODE = 4;
    public static final int ACTIVITY_LS_MODE = 8;
    private static final long ADDVIEW_SWITCH = 17179869184L;
    private static final int ADDVIEW_SWITCH_OFF = 1;
    private static final int ADDVIEW_SWITCH_ON = 0;
    private static final int APPLY_PERMISSION = 1;
    public static final String DESC_ACTIVITY_BG_MODE = "ACTIVITY_BACKGROUND_MODE";
    public static final String DESC_ACTIVITY_LS_MODE = "ACTIVITY_LOCKSCREEN_MODE";
    public static final String DESC_TOAST_MODE = "TOAST_MODE";
    private static final int DO_NOT_SHOW_ADDVIEW = -1;
    private static final int NO_APPLY_PERMISSION = 0;
    private static final int OPS_MODE_ALLOWED = 0;
    private static final int OPS_MODE_ERRORED = 2;
    private static final String PERMISSION = "com.huawei.permission.HWSYSTEMMANAGER_PLUGIN";
    private static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private static final String TAG = "HwAddViewHelper";
    public static final int TOAST_MODE = 2;
    private static HwAddViewHelper mHwAddViewHelper = null;
    private AppOpsManager mAppOps;
    private Context mContext = null;

    public static final HwAddViewHelper getInstance(Context context) {
        HwAddViewHelper hwAddViewHelper;
        synchronized (HwAddViewHelper.class) {
            if (mHwAddViewHelper == null) {
                mHwAddViewHelper = new HwAddViewHelper(context);
            }
            hwAddViewHelper = mHwAddViewHelper;
        }
        return hwAddViewHelper;
    }

    private HwAddViewHelper(Context context) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
    }

    public boolean addViewPermissionCheck(String packageName, int type, int calleruid) {
        return HwAddViewManager.getInstance(this.mContext).addViewPermissionCheck(packageName, type, calleruid);
    }

    public void setAddviewPermission(String pkgName, boolean value, int userId, int uid) {
        if (pkgName != null) {
            int operation = 2;
            int opMode = value ? 0 : 2;
            AppOpsManager appOpsManager = this.mAppOps;
            if (appOpsManager == null) {
                SlogEx.e(TAG, "setAddviewPermission AppOps is null");
                return;
            }
            AppOpsManagerEx.setMode(appOpsManager, "android:system_alert_window", uid, pkgName, opMode);
            SlogEx.i(TAG, pkgName + " setOp complete");
            if (HwPermDbAdapter.getInstance(this.mContext).checkHwPermCode(pkgName, 17179869184L, userId) != 0) {
                if (value) {
                    operation = 1;
                }
                SlogEx.v(TAG, "setAddviewPermission: " + pkgName + " operation: " + operation);
                HwPermDbAdapter.getInstance(this.mContext).setHwPermission(pkgName, userId, 17179869184L, operation, uid);
            }
        }
    }

    public int checkAddviewPermission(String pkgName, int uid) {
        int userId = UserHandleEx.getUserId(uid);
        if (pkgName == null) {
            return -1;
        }
        int addViewValue = HwPermDbAdapter.getInstance(this.mContext).checkHwPermCode(pkgName, PermissionType.SYSTEM_ALERT_WINDOW, userId);
        int blackListValue = HwPermDbAdapter.getInstance(this.mContext).checkHwPermCode(pkgName, 17179869184L, userId);
        if (addViewValue != 1 && blackListValue != 1) {
            SlogEx.v(TAG, pkgName + " doesn't apply addview permission and is not in blacklist");
            return -1;
        } else if (addViewValue == 1) {
            return ((AppOpsManager) this.mContext.getSystemService("appops")).checkOpNoThrow("android:system_alert_window", uid, pkgName) == 0 ? 0 : 2;
        } else {
            if (blackListValue == 1) {
                return HwPermDbAdapter.getInstance(this.mContext).checkHwPerm(pkgName, 17179869184L, userId) == 1 ? 0 : 2;
            }
            return -1;
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    private void recordPermissionUsed(String pkg, boolean isAllow, int type, int calleruid) {
        String desc = null;
        if (type == 2) {
            desc = DESC_TOAST_MODE;
        } else if (type == 4) {
            desc = DESC_ACTIVITY_BG_MODE;
        } else if (type == 8) {
            desc = DESC_ACTIVITY_LS_MODE;
        }
        PermRecordHandler mPermRecHandler = PermRecordHandler.getHandleInstance();
        if (mPermRecHandler != null) {
            mPermRecHandler.accessPermission(pkg, SYSTEM_ALERT_WINDOW, isAllow, calleruid, desc);
        }
    }
}
