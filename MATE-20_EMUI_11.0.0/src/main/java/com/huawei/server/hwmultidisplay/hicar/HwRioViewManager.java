package com.huawei.server.hwmultidisplay.hicar;

import android.app.HwRioClientInfo;
import android.app.IHwRioRuleCb;
import android.os.RemoteException;
import android.util.HwPCUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class HwRioViewManager {
    private static final String TAG = "HwRioViewManager";
    private static HwRioViewManager sInstance;
    private IHwRioRuleCb mHwRioRuleCb;
    private Map<String, RioInfo> mRioMap = new HashMap();

    /* access modifiers changed from: private */
    public final class RioInfo {
        private IHwRioRuleCb callback;
        private int displayId;
        private String mode;
        private List<String> whiteList;

        private RioInfo() {
        }
    }

    private HwRioViewManager() {
    }

    public static synchronized HwRioViewManager getInstance() {
        HwRioViewManager hwRioViewManager;
        synchronized (HwRioViewManager.class) {
            if (sInstance == null) {
                sInstance = new HwRioViewManager();
            }
            hwRioViewManager = sInstance;
        }
        return hwRioViewManager;
    }

    public String getHwRioRule(HwRioClientInfo info) {
        IHwRioRuleCb iHwRioRuleCb = this.mHwRioRuleCb;
        if (iHwRioRuleCb == null) {
            return null;
        }
        try {
            return iHwRioRuleCb.getRemoteRioRule(info);
        } catch (RemoteException exception) {
            HwPCUtils.log(TAG, "get DA rule callback exception " + exception);
            return null;
        }
    }

    public void enableRio(String mode, int displayId, IHwRioRuleCb callback, List<String> whiteList) {
        HwPCUtils.log(TAG, "enableRio displayId = " + displayId + "; callback = " + callback);
        RioInfo info = this.mRioMap.computeIfAbsent(mode, new Function() {
            /* class com.huawei.server.hwmultidisplay.hicar.$$Lambda$HwRioViewManager$Tty1BMAWMHUz0eo9qaD9kV0zv1w */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return HwRioViewManager.this.lambda$enableRio$0$HwRioViewManager((String) obj);
            }
        });
        info.mode = mode;
        info.displayId = displayId;
        info.callback = callback;
        info.whiteList = whiteList;
        this.mHwRioRuleCb = callback;
    }

    public /* synthetic */ RioInfo lambda$enableRio$0$HwRioViewManager(String key) {
        return new RioInfo();
    }

    public void disableRio(String mode) {
        HwPCUtils.log(TAG, "disableRio mode = " + mode);
        this.mRioMap.remove(mode);
    }

    public boolean isRioEnable(int displayId, String packageName) {
        HwPCUtils.log(TAG, "check if rio enable, displayId = " + displayId + "， packageName = " + packageName);
        for (Map.Entry<String, RioInfo> entry : this.mRioMap.entrySet()) {
            if (entry.getValue().displayId == displayId) {
                if (entry.getValue().whiteList == null || !entry.getValue().whiteList.contains(packageName)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
