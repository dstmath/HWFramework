package android.aft;

import android.aft.IHwAftPolicyService;
import android.os.ServiceManager;
import android.util.Singleton;

public class HwAftPolicyManager {
    public static final String SERVICE_NAME = "hwaftpolicy";
    private static final String TAG = "HwAftPolicyManager";
    private static final Singleton<IHwAftPolicyService> gDefault = new Singleton<IHwAftPolicyService>() {
        /* class android.aft.HwAftPolicyManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwAftPolicyService create() {
            return IHwAftPolicyService.Stub.asInterface(ServiceManager.getService(HwAftPolicyManager.SERVICE_NAME));
        }
    };

    public static IHwAftPolicyService getService() {
        return gDefault.get();
    }
}
