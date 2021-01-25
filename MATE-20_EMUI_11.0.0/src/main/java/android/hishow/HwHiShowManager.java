package android.hishow;

import android.hishow.IHwHiShowManager;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;

public class HwHiShowManager {
    private static final Singleton<IHwHiShowManager> IHWHISHOW_MANAGER_SINGLETON = new Singleton<IHwHiShowManager>() {
        /* class android.hishow.HwHiShowManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IHwHiShowManager create() {
            IHwHiShowManager hsm = IHwHiShowManager.Stub.asInterface(ServiceManager.getService(HwHiShowManager.SVC_NAME));
            Log.d(HwHiShowManager.TAG, "GS:create hsm is " + hsm);
            return hsm;
        }
    };
    private static final String SVC_NAME = "HwHiShowManagerService";
    private static final String TAG = "HwHiShowManager";

    public static IHwHiShowManager getService() {
        Log.d(TAG, "GS:enter");
        return (IHwHiShowManager) IHWHISHOW_MANAGER_SINGLETON.get();
    }
}
