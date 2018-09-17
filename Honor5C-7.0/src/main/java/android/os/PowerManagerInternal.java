package android.os;

public abstract class PowerManagerInternal {
    public static final int POWER_HINT_INTERACTION = 2;
    public static final int POWER_HINT_SUSTAINED_PERFORMANCE_MODE = 6;
    public static final int WAKEFULNESS_ASLEEP = 0;
    public static final int WAKEFULNESS_AWAKE = 1;
    public static final int WAKEFULNESS_DOZING = 3;
    public static final int WAKEFULNESS_DREAMING = 2;

    public interface LowPowerModeListener {
        void onLowPowerModeChanged(boolean z);
    }

    public abstract boolean getLowPowerModeEnabled();

    public abstract void powerHint(int i, int i2);

    public abstract void registerLowPowerModeObserver(LowPowerModeListener lowPowerModeListener);

    public abstract void setButtonBrightnessOverrideFromWindowManager(int i);

    public abstract boolean setDeviceIdleMode(boolean z);

    public abstract void setDeviceIdleTempWhitelist(int[] iArr);

    public abstract void setDeviceIdleWhitelist(int[] iArr);

    public abstract void setDozeOverrideFromDreamManager(int i, int i2);

    public abstract boolean setLightDeviceIdleMode(boolean z);

    public abstract void setMaximumScreenOffTimeoutFromDeviceAdmin(int i);

    public abstract void setScreenBrightnessOverrideFromWindowManager(int i);

    public abstract void setUserActivityTimeoutOverrideFromWindowManager(long j);

    public abstract void setUserInactiveOverrideFromWindowManager();

    public abstract void uidGone(int i);

    public abstract void updateUidProcState(int i, int i2);

    public static String wakefulnessToString(int wakefulness) {
        switch (wakefulness) {
            case WAKEFULNESS_ASLEEP /*0*/:
                return "Asleep";
            case WAKEFULNESS_AWAKE /*1*/:
                return "Awake";
            case WAKEFULNESS_DREAMING /*2*/:
                return "Dreaming";
            case WAKEFULNESS_DOZING /*3*/:
                return "Dozing";
            default:
                return Integer.toString(wakefulness);
        }
    }

    public static boolean isInteractive(int wakefulness) {
        return wakefulness == WAKEFULNESS_AWAKE || wakefulness == WAKEFULNESS_DREAMING;
    }
}
