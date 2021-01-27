package android.aft;

import android.aft.IHwAftPolicyService;
import android.os.ServiceManager;
import android.util.Singleton;

public class HwAftPolicyManager {
    private static final Singleton<IHwAftPolicyService> I_HW_AFT_POLICY_SERVICE = new Singleton<IHwAftPolicyService>() {
        /* class android.aft.HwAftPolicyManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwAftPolicyService create() {
            return IHwAftPolicyService.Stub.asInterface(ServiceManager.getService(HwAftPolicyManager.SERVICE_NAME));
        }
    };
    public static final String SERVICE_NAME = "hwaftpolicy";
    private static final String TAG = "HwAftPolicyManager";

    public static IHwAftPolicyService getService() {
        return I_HW_AFT_POLICY_SERVICE.get();
    }
}
