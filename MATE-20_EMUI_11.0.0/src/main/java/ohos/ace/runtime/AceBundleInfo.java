package ohos.ace.runtime;

import com.huawei.ace.runtime.ALog;
import ohos.app.Context;
import ohos.bundle.BundleInfo;
import ohos.bundle.IBundleManager;
import ohos.rpc.RemoteException;

public final class AceBundleInfo {
    private static final String LOG_TAG = "Ace_BundleInfo";
    private final Context context;

    /* access modifiers changed from: protected */
    public native void nativeInitialize();

    /* access modifiers changed from: protected */
    public native void nativeSetBundleInfo(int i, String str);

    public AceBundleInfo(Context context2) {
        this.context = context2;
        nativeInitialize();
    }

    public boolean aceGetBundleInfo(String str) {
        IBundleManager bundleManager = this.context.getBundleManager();
        if (bundleManager == null) {
            return false;
        }
        try {
            BundleInfo bundleInfo = bundleManager.getBundleInfo(str, 0);
            if (bundleInfo == null) {
                ALog.i(LOG_TAG, "bundleInfo is null");
                return false;
            } else if (bundleInfo.getName().equals(str)) {
                nativeSetBundleInfo(bundleInfo.getVersionCode(), bundleInfo.getVersionName());
                return true;
            } else {
                ALog.i(LOG_TAG, "bundleName is unavailable");
                return false;
            }
        } catch (RemoteException unused) {
            ALog.e(LOG_TAG, "bms is unavailable");
            return false;
        }
    }
}
