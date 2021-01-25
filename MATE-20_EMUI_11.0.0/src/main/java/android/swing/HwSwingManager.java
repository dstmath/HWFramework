package android.swing;

import android.os.ServiceManager;
import android.os.SystemProperties;
import android.swing.IHwSwingService;
import android.util.Singleton;

public class HwSwingManager {
    private static final boolean IS_SWING_ENABLED;
    private static final boolean IS_TV_AIVISION_ENABLED = SystemProperties.getBoolean("hw_mc.visionkit.tv_aivision_enable", false);
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

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.swing_enabled", 0) != 1) {
            z = false;
        }
        IS_SWING_ENABLED = z;
    }

    private HwSwingManager() {
    }

    public static IHwSwingService getService() {
        if (IS_SWING_ENABLED || IS_TV_AIVISION_ENABLED) {
            return sDefault.get();
        }
        return null;
    }
}
