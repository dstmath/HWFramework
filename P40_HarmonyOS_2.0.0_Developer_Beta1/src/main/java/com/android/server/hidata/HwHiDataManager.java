package com.android.server.hidata;

import android.emcom.EmcomManager;
import com.android.server.hidata.appqoe.HwAppQoeApkConfig;
import com.android.server.hidata.appqoe.HwAppQoeManager;
import com.android.server.hidata.appqoe.HwAppQoeResourceManager;
import com.android.server.hidata.appqoe.HwAppQoeUtils;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.arbitration.HwArbitrationManager;

public class HwHiDataManager {
    private static final int ACTION_CONTROL_HIDATA_MPLINK = 0;
    private static final int ACTION_CONTROL_HIDATA_WIFI_BOOST = 1;
    public static final int ACTION_ONEHOP_TV_MIRACAST = 6060;
    private static final String HIDATA_WIFI_BOOST_HICALL_PACKAGENAME = "com.huawei.hwvoipservice";
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwHiDataManager.class.getSimpleName());
    private static HwHiDataManager sHwHiDataManager = null;

    private HwHiDataManager() {
    }

    public static synchronized HwHiDataManager getInstance() {
        HwHiDataManager hwHiDataManager;
        synchronized (HwHiDataManager.class) {
            if (sHwHiDataManager == null) {
                sHwHiDataManager = new HwHiDataManager();
            }
            hwHiDataManager = sHwHiDataManager;
        }
        return hwHiDataManager;
    }

    public boolean registerHiDataMonitor(IHwHiDataCallback callback) {
        HwAppQoeManager appQoeManager = HwAppQoeManager.getInstance();
        if (appQoeManager != null) {
            return appQoeManager.registerHiDataMonitor(callback);
        }
        HwAppQoeUtils.logE(TAG, false, "HwAPPQoEManager error", new Object[0]);
        return false;
    }

    public boolean controlHiDataOptimize(String pkgName, int action, boolean enable) {
        if (!HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
            HwAppQoeUtils.logD(TAG, false, "HIMOS2 not on", new Object[0]);
            return false;
        } else if (pkgName == null || pkgName.isEmpty()) {
            HwAppQoeUtils.logE(TAG, false, "pkgName Error", new Object[0]);
            return false;
        } else if (!isControllable(pkgName, action)) {
            HwAppQoeUtils.logE(TAG, false, "checkIfCanControl false", new Object[0]);
            return false;
        } else if (action == 0) {
            if (HwAppQoeManager.getInstance() == null) {
                HwAppQoeUtils.logE(TAG, false, "HwAPPQoEManager error", new Object[0]);
                return false;
            }
            HwAppQoeUtils.logD(TAG, false, "pkg: %{public}s Enable: %{public}s", pkgName, Boolean.valueOf(enable));
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
            HwAppQoeUtils.logE(TAG, false, "not support action: %{public}d", Integer.valueOf(action));
            return false;
        }
    }

    private boolean isControllable(String pkgName, int action) {
        if (action == 0) {
            HwAppQoeApkConfig hwApkScenes = HwAppQoeResourceManager.getInstance().checkIsMonitorApkScenes(pkgName, null);
            if (hwApkScenes == null) {
                HwAppQoeUtils.logE(TAG, false, "app is not the MonitorAPK", new Object[0]);
                return false;
            } else if (!(hwApkScenes.mScenesType == 4 || hwApkScenes.mScenesType == 5)) {
                HwAppQoeUtils.logE(TAG, false, "ScenesType: %{public}d", Integer.valueOf(hwApkScenes.mScenesType));
                return false;
            }
        } else if (action != 1) {
            HwAppQoeUtils.logE(TAG, false, "not support action: %{public}d", Integer.valueOf(action));
            return false;
        } else if (!HIDATA_WIFI_BOOST_HICALL_PACKAGENAME.equals(pkgName)) {
            HwAppQoeUtils.logE(TAG, false, "not in control app %{public}s", pkgName);
            return false;
        }
        return true;
    }
}
