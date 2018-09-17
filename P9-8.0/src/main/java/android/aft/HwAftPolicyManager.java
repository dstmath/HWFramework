package android.aft;

import android.aft.IHwAftPolicyService.Stub;
import android.os.ServiceManager;
import android.util.Singleton;

public class HwAftPolicyManager {
    public static final String SERVICE_NAME = "hwaftpolicy";
    private static final String TAG = "HwAftPolicyManager";
    private static final Singleton<IHwAftPolicyService> gDefault = new Singleton<IHwAftPolicyService>() {
        protected IHwAftPolicyService create() {
            return Stub.asInterface(ServiceManager.getService(HwAftPolicyManager.SERVICE_NAME));
        }
    };

    public static IHwAftPolicyService getService() {
        return (IHwAftPolicyService) gDefault.get();
    }
}
