package com.android.server.security.hsm;

import android.content.Context;
import com.huawei.hsm.permission.monitor.PermRecordHandler;

public class HwAddViewHelper {
    public static final int ACTIVITY_BG_MODE = 4;
    public static final int ACTIVITY_LS_MODE = 8;
    public static final String DESC_ACTIVITY_BG_MODE = "ACTIVITY_BACKGROUND_MODE";
    public static final String DESC_ACTIVITY_LS_MODE = "ACTIVITY_LOCKSCREEN_MODE";
    public static final String DESC_TOAST_MODE = "TOAST_MODE";
    private static final String PERMISSION = "com.huawei.permission.HWSYSTEMMANAGER_PLUGIN";
    private static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private static final String TAG = "HwAddViewHelper";
    public static final int TOAST_MODE = 2;
    private static HwAddViewHelper mHwAddViewHelper = null;
    Context mContext = null;

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
    }

    public boolean addViewPermissionCheck(String packageName, int type, int calleruid) {
        return HwAddViewManager.getInstance(this.mContext).addViewPermissionCheck(packageName, type, calleruid);
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
