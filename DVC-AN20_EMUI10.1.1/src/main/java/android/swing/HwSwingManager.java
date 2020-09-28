package android.swing;

import android.os.ServiceManager;
import android.swing.IHwSwingService;
import android.util.Singleton;

public class HwSwingManager {
    public static final String SERVICE_NAME = "hwswing";
    private static final String TAG = "HwSwingManager";
    private static Singleton<IHwSwingService> sDefault = new Singleton<IHwSwingService>() {
        /* class android.swing.HwSwingManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwSwingService create() {
            return IHwSwingService.Stub.asInterface(ServiceManager.getService(HwSwingManager.SERVICE_NAME));
        }
    };

    private HwSwingManager() {
    }

    public static IHwSwingService getService() {
        return sDefault.get();
    }
}
