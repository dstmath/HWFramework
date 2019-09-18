package android.app.admin;

import com.android.server.LocalServices;

public abstract class DevicePolicyCache {

    private static class EmptyDevicePolicyCache extends DevicePolicyCache {
        /* access modifiers changed from: private */
        public static final EmptyDevicePolicyCache INSTANCE = new EmptyDevicePolicyCache();

        private EmptyDevicePolicyCache() {
        }

        public boolean getScreenCaptureDisabled(int userHandle) {
            return false;
        }
    }

    public abstract boolean getScreenCaptureDisabled(int i);

    protected DevicePolicyCache() {
    }

    public static DevicePolicyCache getInstance() {
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        return dpmi != null ? dpmi.getDevicePolicyCache() : EmptyDevicePolicyCache.INSTANCE;
    }
}
