package com.android.server.hidata;

import android.emcom.EmcomManager;
import com.android.server.hidata.appqoe.HwAPPQoEAPKConfig;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationManager;

public class HwHidataManager {
    private static final int ACTION_CONTROL_HIDATA_MPLINK = 0;
    private static final int ACTION_CONTROL_HIDATA_WIFI_BOOST = 1;
    public static final int ACTION_ONEHOP_TV_MIRACAST = 6060;
    private static final String HIDATA_WIFI_BOOST_HICALL_PACKAGENAME = "com.huawei.hwvoipservice";
    private static final String TAG = "HiData_HwHidataManager";
    private static HwHidataManager mHwHidataManager = null;

    private HwHidataManager() {
    }

    public static synchronized HwHidataManager getInstance() {
        HwHidataManager hwHidataManager;
        synchronized (HwHidataManager.class) {
            if (mHwHidataManager == null) {
                mHwHidataManager = new HwHidataManager();
            }
            hwHidataManager = mHwHidataManager;
        }
        return hwHidataManager;
    }

    public boolean registerHidataMonitor(IHwHidataCallback callback) {
        HwAPPQoEManager appQoeManager = HwAPPQoEManager.getInstance();
        if (appQoeManager != null) {
            return appQoeManager.registerHidataMonitor(callback);
        }
        HwAPPQoEUtils.logE(TAG, false, "HwAPPQoEManager error", new Object[0]);
        return false;
    }

    public boolean controlHidataOptimize(String pkgName, int action, boolean enable) {
        if (!HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
            HwAPPQoEUtils.logD(TAG, false, "HIMOS2 not on", new Object[0]);
            return false;
        } else if (pkgName == null || pkgName.isEmpty()) {
            HwAPPQoEUtils.logE(TAG, false, "pkgName Error", new Object[0]);
            return false;
        } else if (!isControlable(pkgName, action)) {
            HwAPPQoEUtils.logE(TAG, false, "checkIfCanControl false", new Object[0]);
            return false;
        } else if (action == 0) {
            if (HwAPPQoEManager.getInstance() == null) {
                HwAPPQoEUtils.logE(TAG, false, "HwAPPQoEManager error", new Object[0]);
                return false;
            }
            HwAPPQoEUtils.logD(TAG, false, "pkg: %{public}s Enable: %{public}s", pkgName, Boolean.valueOf(enable));
            if (EmcomManager.getInstance().controlMpLink(pkgName, enable) == 0) {
                return true;
            }
            return false;
        } else if (action == 1) {
            HwArbitrationManager hwArbitrationManager = HwArbitrationManager.getInstance();
            if (hwArbitrationManager != null) {
                return hwArbitrationManager.isHandleWifiBoostSuccessful(pkgName, enable);
            }
            return false;
        } else {
            HwAPPQoEUtils.logE(TAG, false, "not support action: %{public}d", Integer.valueOf(action));
            return false;
        }
    }

    private boolean isControlable(String pkgName, int action) {
        if (action == 0) {
            HwAPPQoEAPKConfig hwAPKScence = HwAPPQoEResourceManger.getInstance().checkIsMonitorAPKScence(pkgName, null);
            if (hwAPKScence == null) {
                HwAPPQoEUtils.logE(TAG, false, "app is not the MonitorAPK", new Object[0]);
                return false;
            } else if (!(hwAPKScence.mScenceType == 4 || hwAPKScence.mScenceType == 5)) {
                HwAPPQoEUtils.logE(TAG, false, "ScenceType: %{public}d", Integer.valueOf(hwAPKScence.mScenceType));
                return false;
            }
        } else if (action != 1) {
            HwAPPQoEUtils.logE(TAG, false, "not support action: %{public}d", Integer.valueOf(action));
            return false;
        } else if (!HIDATA_WIFI_BOOST_HICALL_PACKAGENAME.equals(pkgName)) {
            HwAPPQoEUtils.logE(TAG, false, "not in control app %{public}s", pkgName);
            return false;
        }
        return true;
    }
}
