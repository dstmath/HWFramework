package huawei.com.android.server.policy;

public class HwGlobalActionsData {
    private static final boolean DEBUG = false;
    public static final int FLAG_AIRPLANEMODE_OFF = 2;
    public static final int FLAG_AIRPLANEMODE_ON = 1;
    public static final int FLAG_AIRPLANEMODE_TRANSITING = 4;
    public static final int FLAG_REBOOT = 65536;
    public static final int FLAG_REBOOT_CONFIRM = 131072;
    public static final int FLAG_SHUTDOWN = 16777216;
    public static final int FLAG_SHUTDOWN_CONFIRM = 33554432;
    public static final int FLAG_SILENTMODE_NORMAL = 1024;
    public static final int FLAG_SILENTMODE_SILENT = 256;
    public static final int FLAG_SILENTMODE_TRANSITING = 2048;
    public static final int FLAG_SILENTMODE_VIBRATE = 512;
    public static final int MASK_FLAG_AIRPLANE = -256;
    public static final int MASK_FLAG_REBOOT = -16711681;
    public static final int MASK_FLAG_SHUTDOWN = 16777215;
    public static final int MASK_FLAG_SILENT = -65281;
    private static final String TAG = "HwGlobalActions";
    private static HwGlobalActionsData sSingleton;
    private boolean mDeviceProvisioned = false;
    private boolean mKeyguardSecure = false;
    private ActionStateObserver mObserver = null;
    private int mState = 16859138;

    public interface ActionStateObserver {
        void onAirplaneModeActionStateChanged();

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
        int prevState = this.mState & -5;
        this.mState &= MASK_FLAG_AIRPLANE;
        this.mState |= state;
        if ((prevState & state) == 0 && this.mObserver != null) {
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

    public int getState() {
        return this.mState;
    }

    void init(boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
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
