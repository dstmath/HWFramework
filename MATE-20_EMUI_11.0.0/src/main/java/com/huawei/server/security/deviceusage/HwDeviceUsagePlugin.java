package com.huawei.server.security.deviceusage;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.util.LogEx;
import huawei.android.security.IHwDeviceUsagePlugin;

public class HwDeviceUsagePlugin extends IHwDeviceUsagePlugin.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.deviceusage.HwDeviceUsagePlugin.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            if (HwDeviceUsagePlugin.IS_HW_DEBUG) {
                Log.d(HwDeviceUsagePlugin.TAG, "createPlugin");
            }
            return new HwDeviceUsagePlugin(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return HwDeviceUsagePlugin.MANAGE_USE_SECURITY;
        }
    };
    private static final int INVALID_TIME = -1;
    private static final boolean IS_CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    private static final boolean IS_HW_DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final boolean IS_PHONE = "default".equals(SystemPropertiesEx.get("ro.build.characteristics"));
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final String TAG = "HwDeviceUsagePlugin";
    private final Context mContext;
    private ActivationMonitor mMonitor = new ActivationMonitor(this.mContext);

    public HwDeviceUsagePlugin(Context context) {
        this.mContext = context;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.deviceusage.HwDeviceUsagePlugin */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        if (IS_HW_DEBUG) {
            Log.d(TAG, "HwDeviceUsagePlugin is Start");
        }
        this.mMonitor.start();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        if (IS_HW_DEBUG) {
            Log.d(TAG, "HwDeviceUsagePlugin onStop");
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    public boolean isDeviceActivated() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mMonitor.isActivated()) {
            return true;
        }
        this.mMonitor.restart(ActivationMonitor.ACTIVATION_TIME);
        return false;
    }

    public void resetActivation() {
        checkPermission(MANAGE_USE_SECURITY);
        this.mMonitor.resetActivation();
    }

    public void detectActivationWithDuration(long duration) {
        checkPermission(MANAGE_USE_SECURITY);
        this.mMonitor.restart(duration);
    }
}
