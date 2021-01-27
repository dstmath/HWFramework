package ohos.ace.runtime;

import ohos.app.Context;
import ohos.bundle.BundleInfo;
import ohos.bundle.IBundleManager;
import ohos.rpc.RemoteException;

public final class AceBundleInfo {
    private final Context context;

    public static class BundleInfoResult {
        public int versionCode = 0;
        public String versionName = "";
    }

    /* access modifiers changed from: protected */
    public native void nativeInitialize();

    public AceBundleInfo(Context context2) {
        this.context = context2;
        nativeInitialize();
    }

    public BundleInfoResult aceGetBundleInfo(String str) {
        IBundleManager bundleManager = this.context.getBundleManager();
        if (bundleManager == null) {
            return null;
        }
        try {
            BundleInfo bundleInfo = bundleManager.getBundleInfo(str, 0);
            if (bundleInfo != null && bundleInfo.getName().equals(str)) {
                BundleInfoResult bundleInfoResult = new BundleInfoResult();
                bundleInfoResult.versionCode = bundleInfo.getVersionCode();
                bundleInfoResult.versionName = bundleInfo.getVersionName();
                return bundleInfoResult;
            }
        } catch (SecurityException | RemoteException unused) {
        }
        return null;
    }
}
