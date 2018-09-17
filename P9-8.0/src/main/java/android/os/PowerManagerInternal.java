package android.os;

public abstract class PowerManagerInternal {
    public static final int WAKEFULNESS_ASLEEP = 0;
    public static final int WAKEFULNESS_AWAKE = 1;
    public static final int WAKEFULNESS_DOZING = 3;
    public static final int WAKEFULNESS_DREAMING = 2;

    public interface LowPowerModeListener {
        int getServiceType();

        void onLowPowerModeChanged(PowerSaveState powerSaveState);
    }

    public abstract void finishUidChanges();

    public abstract PowerSaveState getLowPowerState(int i);

    public abstract boolean isUserActivityScreenDimOrDream();

    public abstract void powerHint(int i, int i2);

    public abstract void registerLowPowerModeObserver(LowPowerModeListener lowPowerModeListener);

    public abstract boolean setDeviceIdleMode(boolean z);

    public abstract void setDeviceIdleTempWhitelist(int[] iArr);

    public abstract void setDeviceIdleWhitelist(int[] iArr);

    public abstract void setDozeOverrideFromDreamManager(int i, int i2);

    public abstract boolean setLightDeviceIdleMode(boolean z);

    public abstract void setMaximumScreenOffTimeoutFromDeviceAdmin(int i);

    public abstract void setScreenBrightnessOverrideFromWindowManager(int i);

    public abstract void setUserActivityTimeoutOverrideFromWindowManager(long j);

    public abstract void setUserInactiveOverrideFromWindowManager();

    public abstract boolean shouldUpdatePCScreenState();

    public abstract void startUidChanges();

    public abstract void uidActive(int i);

    public abstract void uidGone(int i);

    public abstract void uidIdle(int i);

    public abstract void updateUidProcState(int i, int i2);

    public static String wakefulnessToString(int wakefulness) {
        switch (wakefulness) {
            case 0:
                return "Asleep";
            case 1:
                return "Awake";
            case 2:
                return "Dreaming";
            case 3:
                return "Dozing";
            default:
                return Integer.toString(wakefulness);
        }
    }

    public static boolean isInteractive(int wakefulness) {
        return wakefulness == 1 || wakefulness == 2;
    }
}
