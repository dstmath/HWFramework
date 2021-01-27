package android.os;

import android.annotation.UnsupportedAppUsage;

public interface IPowerManager extends IInterface {
    void acquireWakeLock(IBinder iBinder, int i, String str, String str2, WorkSource workSource, String str3) throws RemoteException;

    void acquireWakeLockWithUid(IBinder iBinder, int i, String str, String str2, int i2) throws RemoteException;

    void boostScreenBrightness(long j) throws RemoteException;

    float convertBrightnessToSeekbarPercentage(float f) throws RemoteException;

    int convertSeekbarProgressToBrightness(int i) throws RemoteException;

    void crash(String str) throws RemoteException;

    boolean forceSuspend() throws RemoteException;

    int getAodState(String str) throws RemoteException;

    int getCoverModeBrightnessFromLastScreenBrightness() throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    int getLastShutdownReason() throws RemoteException;

    int getLastSleepReason() throws RemoteException;

    int getPowerSaveModeTrigger() throws RemoteException;

    PowerSaveState getPowerSaveState(int i) throws RemoteException;

    @UnsupportedAppUsage
    void goToSleep(long j, int i, int i2) throws RemoteException;

    int hwBrightnessGetData(String str, Bundle bundle) throws RemoteException;

    int hwBrightnessSetData(String str, Bundle bundle) throws RemoteException;

    boolean isDeviceIdleMode() throws RemoteException;

    @UnsupportedAppUsage
    boolean isInteractive() throws RemoteException;

    boolean isLightDeviceIdleMode() throws RemoteException;

    boolean isPowerSaveMode() throws RemoteException;

    boolean isScreenBrightnessBoosted() throws RemoteException;

    boolean isWakeLockLevelSupported(int i) throws RemoteException;

    void nap(long j) throws RemoteException;

    void onCoverModeChanged(boolean z) throws RemoteException;

    void powerHint(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void reboot(boolean z, String str, boolean z2) throws RemoteException;

    void rebootSafeMode(boolean z, boolean z2) throws RemoteException;

    @UnsupportedAppUsage
    void releaseWakeLock(IBinder iBinder, int i) throws RemoteException;

    boolean setAdaptivePowerSaveEnabled(boolean z) throws RemoteException;

    boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig batterySaverPolicyConfig) throws RemoteException;

    void setAodAlpmState(int i) throws RemoteException;

    void setAodState(int i, int i2) throws RemoteException;

    void setAttentionLight(boolean z, int i) throws RemoteException;

    void setBrightnessAnimationTime(boolean z, int i) throws RemoteException;

    void setBrightnessNoLimit(int i, int i2) throws RemoteException;

    void setDozeAfterScreenOff(boolean z) throws RemoteException;

    void setDozeOverrideFromAod(int i, int i2, IBinder iBinder) throws RemoteException;

    boolean setDynamicPowerSaveHint(boolean z, int i) throws RemoteException;

    void setMaxBrightnessFromThermal(int i) throws RemoteException;

    void setModeToAutoNoClearOffsetEnable(boolean z) throws RemoteException;

    boolean setPowerSaveModeEnabled(boolean z) throws RemoteException;

    void setStayOnSetting(int i) throws RemoteException;

    void setTemporaryScreenAutoBrightnessSettingOverride(int i) throws RemoteException;

    void setTemporaryScreenBrightnessSettingOverride(int i) throws RemoteException;

    void shutdown(boolean z, String str, boolean z2) throws RemoteException;

    void updateWakeLockUids(IBinder iBinder, int[] iArr) throws RemoteException;

    void updateWakeLockWorkSource(IBinder iBinder, WorkSource workSource, String str) throws RemoteException;

    @UnsupportedAppUsage
    void userActivity(long j, int i, int i2) throws RemoteException;

    void wakeUp(long j, int i, String str, String str2) throws RemoteException;

    public static class Default implements IPowerManager {
        @Override // android.os.IPowerManager
        public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uidtoblame) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void releaseWakeLock(IBinder lock, int flags) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void updateWakeLockUids(IBinder lock, int[] uids) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void powerHint(int hintId, int data) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean isWakeLockLevelSupported(int level) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public void userActivity(long time, int event, int flags) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void wakeUp(long time, int reason, String details, String opPackageName) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void goToSleep(long time, int reason, int flags) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void nap(long time) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean isInteractive() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean isPowerSaveMode() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public PowerSaveState getPowerSaveState(int serviceType) throws RemoteException {
            return null;
        }

        @Override // android.os.IPowerManager
        public boolean setPowerSaveModeEnabled(boolean mode) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean setDynamicPowerSaveHint(boolean powerSaveHint, int disableThreshold) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig config) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean setAdaptivePowerSaveEnabled(boolean enabled) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public int getPowerSaveModeTrigger() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public boolean isDeviceIdleMode() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean isLightDeviceIdleMode() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public void reboot(boolean confirm, String reason, boolean wait) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void rebootSafeMode(boolean confirm, boolean wait) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void shutdown(boolean confirm, String reason, boolean wait) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void crash(String message) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public int getLastShutdownReason() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public int getLastSleepReason() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public void setStayOnSetting(int val) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void boostScreenBrightness(long time) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean isScreenBrightnessBoosted() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public void setTemporaryScreenBrightnessSettingOverride(int brightness) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setTemporaryScreenAutoBrightnessSettingOverride(int brightness) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setAttentionLight(boolean on, int color) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setAodAlpmState(int globalState) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setAodState(int globalState, int alpmMode) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setDozeOverrideFromAod(int screenState, int screenBrightness, IBinder binder) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public int getAodState(String file) throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public int convertSeekbarProgressToBrightness(int progress) throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public float convertBrightnessToSeekbarPercentage(float brightness) throws RemoteException {
            return 0.0f;
        }

        @Override // android.os.IPowerManager
        public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void onCoverModeChanged(boolean status) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public int getCoverModeBrightnessFromLastScreenBrightness() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public void setMaxBrightnessFromThermal(int brightness) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setBrightnessNoLimit(int brightness, int time) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setModeToAutoNoClearOffsetEnable(boolean enable) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public int hwBrightnessSetData(String name, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public int hwBrightnessGetData(String name, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public void setDozeAfterScreenOff(boolean on) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean forceSuspend() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPowerManager {
        private static final String DESCRIPTOR = "android.os.IPowerManager";
        static final int TRANSACTION_acquireWakeLock = 1;
        static final int TRANSACTION_acquireWakeLockWithUid = 2;
        static final int TRANSACTION_boostScreenBrightness = 29;
        static final int TRANSACTION_convertBrightnessToSeekbarPercentage = 39;
        static final int TRANSACTION_convertSeekbarProgressToBrightness = 38;
        static final int TRANSACTION_crash = 25;
        static final int TRANSACTION_forceSuspend = 49;
        static final int TRANSACTION_getAodState = 37;
        static final int TRANSACTION_getCoverModeBrightnessFromLastScreenBrightness = 42;
        static final int TRANSACTION_getHwInnerService = 50;
        static final int TRANSACTION_getLastShutdownReason = 26;
        static final int TRANSACTION_getLastSleepReason = 27;
        static final int TRANSACTION_getPowerSaveModeTrigger = 19;
        static final int TRANSACTION_getPowerSaveState = 14;
        static final int TRANSACTION_goToSleep = 10;
        static final int TRANSACTION_hwBrightnessGetData = 47;
        static final int TRANSACTION_hwBrightnessSetData = 46;
        static final int TRANSACTION_isDeviceIdleMode = 20;
        static final int TRANSACTION_isInteractive = 12;
        static final int TRANSACTION_isLightDeviceIdleMode = 21;
        static final int TRANSACTION_isPowerSaveMode = 13;
        static final int TRANSACTION_isScreenBrightnessBoosted = 30;
        static final int TRANSACTION_isWakeLockLevelSupported = 7;
        static final int TRANSACTION_nap = 11;
        static final int TRANSACTION_onCoverModeChanged = 41;
        static final int TRANSACTION_powerHint = 5;
        static final int TRANSACTION_reboot = 22;
        static final int TRANSACTION_rebootSafeMode = 23;
        static final int TRANSACTION_releaseWakeLock = 3;
        static final int TRANSACTION_setAdaptivePowerSaveEnabled = 18;
        static final int TRANSACTION_setAdaptivePowerSavePolicy = 17;
        static final int TRANSACTION_setAodAlpmState = 34;
        static final int TRANSACTION_setAodState = 35;
        static final int TRANSACTION_setAttentionLight = 33;
        static final int TRANSACTION_setBrightnessAnimationTime = 40;
        static final int TRANSACTION_setBrightnessNoLimit = 44;
        static final int TRANSACTION_setDozeAfterScreenOff = 48;
        static final int TRANSACTION_setDozeOverrideFromAod = 36;
        static final int TRANSACTION_setDynamicPowerSaveHint = 16;
        static final int TRANSACTION_setMaxBrightnessFromThermal = 43;
        static final int TRANSACTION_setModeToAutoNoClearOffsetEnable = 45;
        static final int TRANSACTION_setPowerSaveModeEnabled = 15;
        static final int TRANSACTION_setStayOnSetting = 28;
        static final int TRANSACTION_setTemporaryScreenAutoBrightnessSettingOverride = 32;
        static final int TRANSACTION_setTemporaryScreenBrightnessSettingOverride = 31;
        static final int TRANSACTION_shutdown = 24;
        static final int TRANSACTION_updateWakeLockUids = 4;
        static final int TRANSACTION_updateWakeLockWorkSource = 6;
        static final int TRANSACTION_userActivity = 8;
        static final int TRANSACTION_wakeUp = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPowerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPowerManager)) {
                return new Proxy(obj);
            }
            return (IPowerManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "acquireWakeLock";
                case 2:
                    return "acquireWakeLockWithUid";
                case 3:
                    return "releaseWakeLock";
                case 4:
                    return "updateWakeLockUids";
                case 5:
                    return "powerHint";
                case 6:
                    return "updateWakeLockWorkSource";
                case 7:
                    return "isWakeLockLevelSupported";
                case 8:
                    return "userActivity";
                case 9:
                    return "wakeUp";
                case 10:
                    return "goToSleep";
                case 11:
                    return "nap";
                case 12:
                    return "isInteractive";
                case 13:
                    return "isPowerSaveMode";
                case 14:
                    return "getPowerSaveState";
                case 15:
                    return "setPowerSaveModeEnabled";
                case 16:
                    return "setDynamicPowerSaveHint";
                case 17:
                    return "setAdaptivePowerSavePolicy";
                case 18:
                    return "setAdaptivePowerSaveEnabled";
                case 19:
                    return "getPowerSaveModeTrigger";
                case 20:
                    return "isDeviceIdleMode";
                case 21:
                    return "isLightDeviceIdleMode";
                case 22:
                    return "reboot";
                case 23:
                    return "rebootSafeMode";
                case 24:
                    return "shutdown";
                case 25:
                    return "crash";
                case 26:
                    return "getLastShutdownReason";
                case 27:
                    return "getLastSleepReason";
                case 28:
                    return "setStayOnSetting";
                case 29:
                    return "boostScreenBrightness";
                case 30:
                    return "isScreenBrightnessBoosted";
                case 31:
                    return "setTemporaryScreenBrightnessSettingOverride";
                case 32:
                    return "setTemporaryScreenAutoBrightnessSettingOverride";
                case 33:
                    return "setAttentionLight";
                case 34:
                    return "setAodAlpmState";
                case 35:
                    return "setAodState";
                case 36:
                    return "setDozeOverrideFromAod";
                case 37:
                    return "getAodState";
                case 38:
                    return "convertSeekbarProgressToBrightness";
                case 39:
                    return "convertBrightnessToSeekbarPercentage";
                case 40:
                    return "setBrightnessAnimationTime";
                case 41:
                    return "onCoverModeChanged";
                case 42:
                    return "getCoverModeBrightnessFromLastScreenBrightness";
                case 43:
                    return "setMaxBrightnessFromThermal";
                case 44:
                    return "setBrightnessNoLimit";
                case 45:
                    return "setModeToAutoNoClearOffsetEnable";
                case 46:
                    return "hwBrightnessSetData";
                case 47:
                    return "hwBrightnessGetData";
                case 48:
                    return "setDozeAfterScreenOff";
                case 49:
                    return "forceSuspend";
                case 50:
                    return "getHwInnerService";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            WorkSource _arg4;
            WorkSource _arg1;
            BatterySaverPolicyConfig _arg0;
            Bundle _arg12;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg03 = data.readStrongBinder();
                        int _arg13 = data.readInt();
                        String _arg2 = data.readString();
                        String _arg3 = data.readString();
                        if (data.readInt() != 0) {
                            _arg4 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        acquireWakeLock(_arg03, _arg13, _arg2, _arg3, _arg4, data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        acquireWakeLockWithUid(data.readStrongBinder(), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        releaseWakeLock(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        updateWakeLockUids(data.readStrongBinder(), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        powerHint(data.readInt(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        updateWakeLockWorkSource(_arg04, _arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWakeLockLevelSupported = isWakeLockLevelSupported(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isWakeLockLevelSupported ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        userActivity(data.readLong(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        wakeUp(data.readLong(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        goToSleep(data.readLong(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        nap(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInteractive = isInteractive();
                        reply.writeNoException();
                        reply.writeInt(isInteractive ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPowerSaveMode = isPowerSaveMode();
                        reply.writeNoException();
                        reply.writeInt(isPowerSaveMode ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        PowerSaveState _result = getPowerSaveState(data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean powerSaveModeEnabled = setPowerSaveModeEnabled(_arg02);
                        reply.writeNoException();
                        reply.writeInt(powerSaveModeEnabled ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean dynamicPowerSaveHint = setDynamicPowerSaveHint(_arg02, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(dynamicPowerSaveHint ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BatterySaverPolicyConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean adaptivePowerSavePolicy = setAdaptivePowerSavePolicy(_arg0);
                        reply.writeNoException();
                        reply.writeInt(adaptivePowerSavePolicy ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean adaptivePowerSaveEnabled = setAdaptivePowerSaveEnabled(_arg02);
                        reply.writeNoException();
                        reply.writeInt(adaptivePowerSaveEnabled ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getPowerSaveModeTrigger();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceIdleMode = isDeviceIdleMode();
                        reply.writeNoException();
                        reply.writeInt(isDeviceIdleMode ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isLightDeviceIdleMode = isLightDeviceIdleMode();
                        reply.writeNoException();
                        reply.writeInt(isLightDeviceIdleMode ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg05 = data.readInt() != 0;
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        reboot(_arg05, _arg14, _arg02);
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg06 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        rebootSafeMode(_arg06, _arg02);
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg07 = data.readInt() != 0;
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        shutdown(_arg07, _arg15, _arg02);
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        crash(data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getLastShutdownReason();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getLastSleepReason();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        setStayOnSetting(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        boostScreenBrightness(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isScreenBrightnessBoosted = isScreenBrightnessBoosted();
                        reply.writeNoException();
                        reply.writeInt(isScreenBrightnessBoosted ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        setTemporaryScreenBrightnessSettingOverride(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        setTemporaryScreenAutoBrightnessSettingOverride(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setAttentionLight(_arg02, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        setAodAlpmState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        setAodState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        setDozeOverrideFromAod(data.readInt(), data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getAodState(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = convertSeekbarProgressToBrightness(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        float _result7 = convertBrightnessToSeekbarPercentage(data.readFloat());
                        reply.writeNoException();
                        reply.writeFloat(_result7);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setBrightnessAnimationTime(_arg02, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        onCoverModeChanged(_arg02);
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getCoverModeBrightnessFromLastScreenBrightness();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        setMaxBrightnessFromThermal(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        setBrightnessNoLimit(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setModeToAutoNoClearOffsetEnable(_arg02);
                        reply.writeNoException();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result9 = hwBrightnessSetData(_arg08, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        Bundle _arg16 = new Bundle();
                        int _result10 = hwBrightnessGetData(_arg09, _arg16);
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        reply.writeInt(1);
                        _arg16.writeToParcel(reply, 1);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setDozeAfterScreenOff(_arg02);
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        boolean forceSuspend = forceSuspend();
                        reply.writeNoException();
                        reply.writeInt(forceSuspend ? 1 : 0);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result11 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result11);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPowerManager {
            public static IPowerManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.os.IPowerManager
            public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(lock);
                        try {
                            _data.writeInt(flags);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(tag);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                        if (ws != null) {
                            _data.writeInt(1);
                            ws.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(historyTag);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().acquireWakeLock(lock, flags, tag, packageName, ws, historyTag);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IPowerManager
            public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uidtoblame) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeInt(flags);
                    _data.writeString(tag);
                    _data.writeString(packageName);
                    _data.writeInt(uidtoblame);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().acquireWakeLockWithUid(lock, flags, tag, packageName, uidtoblame);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void releaseWakeLock(IBinder lock, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseWakeLock(lock, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void updateWakeLockUids(IBinder lock, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeIntArray(uids);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateWakeLockUids(lock, uids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void powerHint(int hintId, int data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hintId);
                    _data.writeInt(data);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().powerHint(hintId, data);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(historyTag);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateWakeLockWorkSource(lock, ws, historyTag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isWakeLockLevelSupported(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWakeLockLevelSupported(level);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void userActivity(long time, int event, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    _data.writeInt(event);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().userActivity(time, event, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void wakeUp(long time, int reason, String details, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    _data.writeInt(reason);
                    _data.writeString(details);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().wakeUp(time, reason, details, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void goToSleep(long time, int reason, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    _data.writeInt(reason);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().goToSleep(time, reason, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void nap(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().nap(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isInteractive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInteractive();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isPowerSaveMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPowerSaveMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public PowerSaveState getPowerSaveState(int serviceType) throws RemoteException {
                PowerSaveState _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceType);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPowerSaveState(serviceType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PowerSaveState.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean setPowerSaveModeEnabled(boolean mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(mode ? 1 : 0);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPowerSaveModeEnabled(mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean setDynamicPowerSaveHint(boolean powerSaveHint, int disableThreshold) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(powerSaveHint ? 1 : 0);
                    _data.writeInt(disableThreshold);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDynamicPowerSaveHint(powerSaveHint, disableThreshold);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAdaptivePowerSavePolicy(config);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean setAdaptivePowerSaveEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enabled ? 1 : 0);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAdaptivePowerSaveEnabled(enabled);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getPowerSaveModeTrigger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPowerSaveModeTrigger();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isDeviceIdleMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceIdleMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isLightDeviceIdleMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLightDeviceIdleMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void reboot(boolean confirm, String reason, boolean wait) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    _data.writeInt(confirm ? 1 : 0);
                    _data.writeString(reason);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reboot(confirm, reason, wait);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void rebootSafeMode(boolean confirm, boolean wait) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    _data.writeInt(confirm ? 1 : 0);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rebootSafeMode(confirm, wait);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void shutdown(boolean confirm, String reason, boolean wait) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    _data.writeInt(confirm ? 1 : 0);
                    _data.writeString(reason);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().shutdown(confirm, reason, wait);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void crash(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().crash(message);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getLastShutdownReason() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastShutdownReason();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getLastSleepReason() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastSleepReason();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setStayOnSetting(int val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(val);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStayOnSetting(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void boostScreenBrightness(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().boostScreenBrightness(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isScreenBrightnessBoosted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScreenBrightnessBoosted();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setTemporaryScreenBrightnessSettingOverride(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTemporaryScreenBrightnessSettingOverride(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setTemporaryScreenAutoBrightnessSettingOverride(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTemporaryScreenAutoBrightnessSettingOverride(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setAttentionLight(boolean on, int color) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(on ? 1 : 0);
                    _data.writeInt(color);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAttentionLight(on, color);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setAodAlpmState(int globalState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(globalState);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAodAlpmState(globalState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setAodState(int globalState, int alpmMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(globalState);
                    _data.writeInt(alpmMode);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAodState(globalState, alpmMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setDozeOverrideFromAod(int screenState, int screenBrightness, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(screenState);
                    _data.writeInt(screenBrightness);
                    _data.writeStrongBinder(binder);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDozeOverrideFromAod(screenState, screenBrightness, binder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getAodState(String file) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(file);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAodState(file);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int convertSeekbarProgressToBrightness(int progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(progress);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().convertSeekbarProgressToBrightness(progress);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public float convertBrightnessToSeekbarPercentage(float brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(brightness);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().convertBrightnessToSeekbarPercentage(brightness);
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(animationEnabled ? 1 : 0);
                    _data.writeInt(millisecond);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBrightnessAnimationTime(animationEnabled, millisecond);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void onCoverModeChanged(boolean status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status ? 1 : 0);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCoverModeChanged(status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getCoverModeBrightnessFromLastScreenBrightness() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCoverModeBrightnessFromLastScreenBrightness();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setMaxBrightnessFromThermal(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMaxBrightnessFromThermal(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setBrightnessNoLimit(int brightness, int time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    _data.writeInt(time);
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBrightnessNoLimit(brightness, time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setModeToAutoNoClearOffsetEnable(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setModeToAutoNoClearOffsetEnable(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int hwBrightnessSetData(String name, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hwBrightnessSetData(name, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int hwBrightnessGetData(String name, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hwBrightnessGetData(name, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        data.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setDozeAfterScreenOff(boolean on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(on ? 1 : 0);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDozeAfterScreenOff(on);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean forceSuspend() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forceSuspend();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPowerManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPowerManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
