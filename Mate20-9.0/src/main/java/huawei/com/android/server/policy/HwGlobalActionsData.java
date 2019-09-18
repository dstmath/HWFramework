package huawei.com.android.server.policy;

public class HwGlobalActionsData {
    private static final boolean DEBUG = false;
    public static final int FLAG_AIRPLANEMODE_OFF = 2;
    public static final int FLAG_AIRPLANEMODE_ON = 1;
    public static final int FLAG_AIRPLANEMODE_TRANSITING = 4;
    public static final int FLAG_LOCKDOWN = 65536;
    public static final int FLAG_LOCKDOWN_CONFIRM = 131072;
    public static final int FLAG_REBOOT = 256;
    public static final int FLAG_REBOOT_CONFIRM = 512;
    public static final int FLAG_SHUTDOWN = 4096;
    public static final int FLAG_SHUTDOWN_CONFIRM = 8192;
    public static final int FLAG_SILENTMODE_NORMAL = 64;
    public static final int FLAG_SILENTMODE_SILENT = 16;
    public static final int FLAG_SILENTMODE_TRANSITING = 128;
    public static final int FLAG_SILENTMODE_VIBRATE = 32;
    public static final int MASK_FLAG_AIRPLANE = -16;
    public static final int MASK_FLAG_LOCKDOWN = -983041;
    public static final int MASK_FLAG_REBOOT = -3841;
    public static final int MASK_FLAG_SHUTDOWN = -61441;
    public static final int MASK_FLAG_SILENT = -241;
    private static final String TAG = "HwGlobalActions";
    private static HwGlobalActionsData sSingleton;
    private boolean mDeviceProvisioned = false;
    private boolean mKeyguardSecure = false;
    private ActionStateObserver mObserver = null;
    private int mState = 0;

    public interface ActionStateObserver {
        void onAirplaneModeActionStateChanged();

        void onLockdownActionStateChanged();

        void onRebootActionStateChanged();

        void onShutdownActionStateChanged();

        void onSilentModeActionStateChanged();
    }

    public void registerActionStateObserver(ActionStateObserver observer) {
        this.mObserver = observer;
    }

    public void unregisterActionStateObserver() {
        this.mObserver = null;
    }

    public void setAirplaneMode(int state) {
        this.mState &= -16;
        this.mState |= state;
        if ((this.mState & -5 & state) == 0 && this.mObserver != null) {
            this.mObserver.onAirplaneModeActionStateChanged();
        }
    }

    public void setSilentMode(int state) {
        int prevState = this.mState;
        this.mState &= MASK_FLAG_SILENT;
        this.mState |= state;
        if ((prevState & state) == 0 && this.mObserver != null) {
            this.mObserver.onSilentModeActionStateChanged();
        }
    }

    public void setRebootMode(int state) {
        int prevState = this.mState;
        this.mState &= MASK_FLAG_REBOOT;
        this.mState |= state;
        if ((prevState & state) == 0 && this.mObserver != null) {
            this.mObserver.onRebootActionStateChanged();
        }
    }

    public void setShutdownMode(int state) {
        int prevState = this.mState;
        this.mState &= MASK_FLAG_SHUTDOWN;
        this.mState |= state;
        if ((prevState & state) == 0 && this.mObserver != null) {
            this.mObserver.onShutdownActionStateChanged();
        }
    }

    public void setLockdownMode(int state) {
        int prevState = this.mState;
        this.mState &= MASK_FLAG_LOCKDOWN;
        this.mState |= state;
        if ((prevState & state) == 0 && this.mObserver != null) {
            this.mObserver.onLockdownActionStateChanged();
        }
    }

    public int getState() {
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public void init(boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
        this.mKeyguardSecure = keyguardSecure;
        this.mDeviceProvisioned = isDeviceProvisioned;
    }

    private HwGlobalActionsData() {
    }

    public static synchronized HwGlobalActionsData getSingletoneInstance() {
        HwGlobalActionsData hwGlobalActionsData;
        synchronized (HwGlobalActionsData.class) {
            if (sSingleton == null) {
                sSingleton = new HwGlobalActionsData();
            }
            hwGlobalActionsData = sSingleton;
        }
        return hwGlobalActionsData;
    }

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisioned;
    }

    public boolean isKeyguardSecure() {
        return this.mKeyguardSecure;
    }
}
