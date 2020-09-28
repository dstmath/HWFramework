package android.os;

import android.os.PowerManager;
import java.util.function.Consumer;

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

    public abstract PowerManager.WakeData getLastWakeup();

    public abstract PowerSaveState getLowPowerState(int i);

    public abstract boolean isUserActivityScreenDimOrDream();

    public abstract void powerHint(int i, int i2);

    public abstract void powerWakeup(long j, int i, String str, int i2, String str2, int i3);

    public abstract void registerLowPowerModeObserver(LowPowerModeListener lowPowerModeListener);

    public abstract boolean setDeviceIdleMode(boolean z);

    public abstract void setDeviceIdleTempWhitelist(int[] iArr);

    public abstract void setDeviceIdleWhitelist(int[] iArr);

    public abstract void setDozeOverrideFromDreamManager(int i, int i2);

    public abstract void setDrawWakeLockOverrideFromSidekick(boolean z);

    public abstract boolean setLightDeviceIdleMode(boolean z);

    public abstract void setMaximumScreenOffTimeoutFromDeviceAdmin(int i, long j);

    public abstract void setScreenBrightnessOverrideFromWindowManager(int i);

    public abstract void setUserActivityTimeoutOverrideFromWindowManager(long j);

    public abstract void setUserInactiveOverrideFromWindowManager();

    public abstract boolean shouldUpdatePCScreenState();

    public abstract void startUidChanges();

    public abstract void uidActive(int i);

    public abstract void uidGone(int i);

    public abstract void uidIdle(int i);

    public abstract void updateUidProcState(int i, int i2);

    public abstract boolean wasDeviceIdleFor(long j);

    public static String wakefulnessToString(int wakefulness) {
        if (wakefulness == 0) {
            return "Asleep";
        }
        if (wakefulness == 1) {
            return "Awake";
        }
        if (wakefulness == 2) {
            return "Dreaming";
        }
        if (wakefulness != 3) {
            return Integer.toString(wakefulness);
        }
        return "Dozing";
    }

    public static int wakefulnessToProtoEnum(int wakefulness) {
        if (wakefulness == 0) {
            return 0;
        }
        if (wakefulness == 1) {
            return 1;
        }
        if (wakefulness == 2) {
            return 2;
        }
        if (wakefulness != 3) {
            return wakefulness;
        }
        return 3;
    }

    public static boolean isInteractive(int wakefulness) {
        return wakefulness == 1 || wakefulness == 2;
    }

    public void registerLowPowerModeObserver(final int serviceType, final Consumer<PowerSaveState> listener) {
        registerLowPowerModeObserver(new LowPowerModeListener() {
            /* class android.os.PowerManagerInternal.AnonymousClass1 */

            @Override // android.os.PowerManagerInternal.LowPowerModeListener
            public int getServiceType() {
                return serviceType;
            }

            @Override // android.os.PowerManagerInternal.LowPowerModeListener
            public void onLowPowerModeChanged(PowerSaveState state) {
                listener.accept(state);
            }
        });
    }
}
