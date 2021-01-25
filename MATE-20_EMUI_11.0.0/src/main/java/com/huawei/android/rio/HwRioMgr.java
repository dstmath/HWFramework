package com.huawei.android.rio;

import android.content.Context;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Log;
import com.huawei.android.app.HwRioClientInfoEx;
import com.huawei.android.app.HwRioRuleCbEx;
import java.util.List;

public class HwRioMgr {
    private static final String TAG = "HwRioMgr";
    private static HwRioRuleCbEx sCallback = new HwRioRuleCbEx() {
        /* class com.huawei.android.rio.HwRioMgr.AnonymousClass1 */

        @Override // com.huawei.android.app.HwRioRuleCbEx
        public String getRemoteRioRule(HwRioClientInfoEx infoEx) {
            Log.d(HwRioMgr.TAG, "getRemoteRioRule:" + infoEx);
            return HwRioMgr.getHwRioTools().loadConfig(infoEx, 0);
        }
    };
    private static IHwRioTools sHwRioTools;

    public static boolean enableRio(String mode, int displayId, List<String> whiteList) {
        Log.d(TAG, "enable Rio.");
        IHwPCManager hwPCManager = HwPCUtils.getHwPCManager();
        if (hwPCManager == null) {
            return true;
        }
        try {
            hwPCManager.enableRio(mode, displayId, sCallback.getInnerListener(), whiteList);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "enableRio RemoteException.");
            return false;
        }
    }

    public static boolean disableRio(String mode) {
        Log.d(TAG, "disable Rio.");
        IHwPCManager hwPCManager = HwPCUtils.getHwPCManager();
        if (hwPCManager == null) {
            return true;
        }
        try {
            hwPCManager.disableRio(mode);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "disableRio RemoteException.");
            return false;
        }
    }

    public static boolean isRioEnable(int displayId, String packageName) {
        IHwPCManager hwPCManager = HwPCUtils.getHwPCManager();
        if (hwPCManager == null) {
            return false;
        }
        try {
            return hwPCManager.isRioEnable(displayId, packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "call isRioEnable error");
            return false;
        }
    }

    public static String getHwRioRule(HwRioClientInfoEx infoEx) {
        IHwPCManager hwPCManager = HwPCUtils.getHwPCManager();
        if (hwPCManager == null) {
            return null;
        }
        try {
            return hwPCManager.getHwRioRule(infoEx.getRioClientInfo());
        } catch (RemoteException e) {
            Log.e(TAG, "call getRioRule error");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static IHwRioTools getHwRioTools() {
        if (sHwRioTools == null) {
            sHwRioTools = DefaultHwRioTools.getInstance();
        }
        return sHwRioTools;
    }

    public static void initContext(Context context) {
        getHwRioTools().initContext(context);
    }

    public static void initHotUpdateFilePath(String filePath) {
        getHwRioTools().setHotUpdateFilePath(filePath);
    }
}
