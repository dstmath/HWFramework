package android.app.admin;

import com.android.server.LocalServices;

public abstract class DevicePolicyCache {
    public abstract int getPasswordQuality(int i);

    public abstract boolean getScreenCaptureDisabled(int i);

    protected DevicePolicyCache() {
    }

    public static DevicePolicyCache getInstance() {
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        return dpmi != null ? dpmi.getDevicePolicyCache() : EmptyDevicePolicyCache.INSTANCE;
    }

    private static class EmptyDevicePolicyCache extends DevicePolicyCache {
        private static final EmptyDevicePolicyCache INSTANCE = new EmptyDevicePolicyCache();

        private EmptyDevicePolicyCache() {
        }

        @Override // android.app.admin.DevicePolicyCache
        public boolean getScreenCaptureDisabled(int userHandle) {
            return false;
        }

        @Override // android.app.admin.DevicePolicyCache
        public int getPasswordQuality(int userHandle) {
            return 0;
        }
    }
}
