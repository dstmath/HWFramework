package ohos.powermanager;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.sensor.agent.CategoryLightAgent;
import sun.misc.Cleaner;

public class PowerManager {
    private static final int LOG_DOMAIN = 218114306;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, LOG_DOMAIN, TAG);
    private static final String TAG = "PowerMgrKit";

    public enum PowerState {
        NONE,
        AWAKE,
        INACTIVE,
        SLEEP
    }

    /* access modifiers changed from: private */
    public static native long nativeCreateRunningLock(String str, int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeDestroy(long j);

    private static native boolean nativeIsScreenOn();

    /* access modifiers changed from: private */
    public static native boolean nativeIsUsed(long j);

    /* access modifiers changed from: private */
    public static native long nativeLock(long j, long j2);

    private static native void nativeRebootDevice(String str);

    private static native void nativeShutDownDevice(String str);

    /* access modifiers changed from: private */
    public static native long nativeUnLock(long j);

    static {
        try {
            HiLog.info(LOG_LABEL, "load powerkit_jni.z.so", new Object[0]);
            System.loadLibrary("powerkit_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LOG_LABEL, "Could not load powerkit_jni.z.so", new Object[0]);
        }
    }

    public enum RunningLockType {
        BACKGROUND(1),
        PROXIMITY_SCREEN_CONTROL(2);
        
        private final int value;

        private RunningLockType(int i) {
            this.value = i;
        }
    }

    public void rebootDevice(String str) {
        nativeRebootDevice(str);
    }

    public DevicePowerStatusInfo getCurrentPowerStatusInfo() {
        return DevicePowerStatusInfo.getDevicePowerStatusInfo();
    }

    public boolean isRunningLockTypeSupported(RunningLockType runningLockType) {
        if (runningLockType == RunningLockType.BACKGROUND) {
            return true;
        }
        if (runningLockType != RunningLockType.PROXIMITY_SCREEN_CONTROL || new CategoryLightAgent().getSingleSensor(0) == null) {
            return false;
        }
        return true;
    }

    public final class RunningLock {
        private static final int CREATE_WITH_SCREEN_OFF = 0;
        private static final int CREATE_WITH_SCREEN_ON = 268435456;
        private final String name;
        private final long nativeRunningLock;
        private final RunningLockType type;

        RunningLock(String str, RunningLockType runningLockType) {
            this.name = str;
            this.type = runningLockType;
            this.nativeRunningLock = PowerManager.nativeCreateRunningLock(str, runningLockType.ordinal(), 0);
            if (this.nativeRunningLock == 0) {
                HiLog.error(PowerManager.LOG_LABEL, "Failed to create runninglock in native", new Object[0]);
            }
            HiLog.debug(PowerManager.LOG_LABEL, "RunningLock name: %{public}s, type: %{public}s, nativeRunningLock: %{public}d", str, runningLockType.name(), Long.valueOf(this.nativeRunningLock));
            Cleaner.create(this, new DestroyNativeObjectTask(this.nativeRunningLock));
        }

        public void lock(long j) {
            if (j <= 0) {
                HiLog.error(PowerManager.LOG_LABEL, "lock timeOutMs error, timeOutMs must > 0!!", new Object[0]);
                return;
            }
            HiLog.debug(PowerManager.LOG_LABEL, "lock timeOutMs = %{public}d, ret = %{public}d", Long.valueOf(j), Long.valueOf(PowerManager.nativeLock(this.nativeRunningLock, j)));
        }

        public void unLock() {
            if (!PowerManager.nativeIsUsed(this.nativeRunningLock)) {
                HiLog.error(PowerManager.LOG_LABEL, "unlock error, already released!!", new Object[0]);
                return;
            }
            HiLog.debug(PowerManager.LOG_LABEL, "unlock ret = %{public}d", Long.valueOf(PowerManager.nativeUnLock(this.nativeRunningLock)));
        }

        public boolean isUsed() {
            boolean nativeIsUsed = PowerManager.nativeIsUsed(this.nativeRunningLock);
            HiLog.debug(PowerManager.LOG_LABEL, "isUsed = %{public}b", Boolean.valueOf(nativeIsUsed));
            return nativeIsUsed;
        }
    }

    private static class DestroyNativeObjectTask implements Runnable {
        private final long nativeObj;

        public DestroyNativeObjectTask(long j) {
            this.nativeObj = j;
        }

        @Override // java.lang.Runnable
        public void run() {
            PowerManager.nativeDestroy(this.nativeObj);
        }
    }

    public RunningLock createRunningLock(String str, RunningLockType runningLockType) {
        return new RunningLock(str, runningLockType);
    }

    public boolean isScreenOn() {
        return nativeIsScreenOn();
    }
}
