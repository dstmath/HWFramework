package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.health.V1_0.HealthInfo;
import android.hardware.health.V2_0.IHealth;
import android.hardware.health.V2_0.IHealthInfoCallback;
import android.hardware.health.V2_0.Result;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.metrics.LogMaker;
import android.os.BatteryManager;
import android.os.BatteryManagerInternal;
import android.os.BatteryProperty;
import android.os.Binder;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBatteryPropertiesListener;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.Trace;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.EventLog;
import android.util.Flog;
import android.util.MutableInt;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.DumpUtils;
import com.android.server.BatteryService;
import com.android.server.NsdService;
import com.android.server.am.AbsHwMtmBroadcastResourceManager;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.BatteryStatsService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.pm.DumpState;
import com.android.server.power.PowerManagerService;
import com.android.server.storage.DeviceStorageMonitorService;
import com.android.server.utils.PriorityDump;
import com.huawei.android.os.HwVibrator;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class BatteryService extends AbsBatteryService {
    private static final long BATTERY_LEVEL_CHANGE_THROTTLE_MS = 60000;
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int BATTERY_SCALE = 100;
    private static final String CHARGER_TYPE = "sys/class/hw_power/charger/charge_data/chargerType";
    private static final String CHARGE_STATUS = "charge_status";
    private static final String CHARGE_WIRED_STATUS = "1";
    private static final String CHARGE_WIRELESS_STATUS = "2";
    private static final boolean DEBUG = false;
    private static final String[] DUMPSYS_ARGS = {"--checkin", "--unplugged"};
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    private static final long HEALTH_HAL_WAIT_MS = 1000;
    public static final boolean IS_AUTO_POWEROFF_ON = SystemProperties.getBoolean("ro.config.auto_power_off", false);
    private static final int MAX_BATTERY_LEVELS_QUEUE_SIZE = 100;
    static final int OPTION_FORCE_UPDATE = 1;
    private static final int SHUTDOWN_DELAY_TIMEOUT = 20000;
    /* access modifiers changed from: private */
    public static final String TAG = BatteryService.class.getSimpleName();
    private static final int VIBRATE_LAST_TIME = 300;
    private final String changingVibrateType = "haptic.battery.charging";
    /* access modifiers changed from: private */
    public ActivityManagerInternal mActivityManagerInternal;
    private ActivityManagerService mAms = null;
    private boolean mBatteryLevelCritical;
    /* access modifiers changed from: private */
    public boolean mBatteryLevelLow;
    private ArrayDeque<Bundle> mBatteryLevelsEventQueue;
    private BatteryPropertiesRegistrar mBatteryPropertiesRegistrar;
    private final IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private int mChargeStartLevel;
    private long mChargeStartTime;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCriticalBatteryLevel;
    private int mDischargeStartLevel;
    private long mDischargeStartTime;
    private final Handler mHandler;
    private HealthHalCallback mHealthHalCallback;
    /* access modifiers changed from: private */
    public HealthInfo mHealthInfo;
    /* access modifiers changed from: private */
    public HealthServiceWrapper mHealthServiceWrapper;
    /* access modifiers changed from: private */
    public int mInvalidCharger;
    private int mLastBatteryHealth;
    private int mLastBatteryLevel;
    private long mLastBatteryLevelChangedSentMs;
    private boolean mLastBatteryLevelCritical;
    private boolean mLastBatteryPresent;
    private int mLastBatteryStatus;
    private int mLastBatteryTemperature;
    private int mLastBatteryVoltage;
    private int mLastChargeCounter;
    private final HealthInfo mLastHealthInfo = new HealthInfo();
    private int mLastInvalidCharger;
    private int mLastMaxChargingCurrent;
    private int mLastMaxChargingVoltage;
    private int mLastPlugType = -1;
    private Led mLed;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mLowBatteryCloseWarningLevel;
    /* access modifiers changed from: private */
    public int mLowBatteryWarningLevel;
    private MetricsLogger mMetricsLogger;
    private CheckForShutdown mPendingCheckForShutdown;
    protected int mPlugType;
    private boolean mSentLowBatteryBroadcast = false;
    private int mSequence = 1;
    private int mShutdownBatteryTemperature;
    private boolean mUpdatesStopped;

    private final class BatteryPropertiesRegistrar extends IBatteryPropertiesRegistrar.Stub {
        private BatteryPropertiesRegistrar() {
        }

        public void registerListener(IBatteryPropertiesListener listener) {
            Slog.e(BatteryService.TAG, "health: must not call registerListener on battery properties");
        }

        public void unregisterListener(IBatteryPropertiesListener listener) {
            Slog.e(BatteryService.TAG, "health: must not call unregisterListener on battery properties");
        }

        public int getProperty(int id, BatteryProperty prop) throws RemoteException {
            BatteryService.traceBegin("HealthGetProperty");
            try {
                IHealth service = BatteryService.this.mHealthServiceWrapper.getLastService();
                if (service != null) {
                    MutableInt outResult = new MutableInt(1);
                    switch (id) {
                        case 1:
                            service.getChargeCounter(new IHealth.getChargeCounterCallback(outResult, prop) {
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$0(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 2:
                            service.getCurrentNow(new IHealth.getCurrentNowCallback(outResult, prop) {
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$1(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 3:
                            service.getCurrentAverage(new IHealth.getCurrentAverageCallback(outResult, prop) {
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$2(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 4:
                            service.getCapacity(new IHealth.getCapacityCallback(outResult, prop) {
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$3(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 5:
                            service.getEnergyCounter(new IHealth.getEnergyCounterCallback(outResult, prop) {
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                public final void onValues(int i, long j) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$5(this.f$0, this.f$1, i, j);
                                }
                            });
                            break;
                        case 6:
                            service.getChargeStatus(new IHealth.getChargeStatusCallback(outResult, prop) {
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$4(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                    }
                    return outResult.value;
                }
                throw new RemoteException("no health service");
            } finally {
                BatteryService.traceEnd();
            }
        }

        static /* synthetic */ void lambda$getProperty$0(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$1(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$2(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$3(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$4(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$5(MutableInt outResult, BatteryProperty prop, int result, long value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong(value);
            }
        }

        public void scheduleUpdate() throws RemoteException {
            BatteryService.traceBegin("HealthScheduleUpdate");
            try {
                IHealth service = BatteryService.this.mHealthServiceWrapper.getLastService();
                if (service != null) {
                    service.update();
                    return;
                }
                throw new RemoteException("no health service");
            } finally {
                BatteryService.traceEnd();
            }
        }

        public int alterWirelessTxSwitch(int status) {
            if (Binder.getCallingUid() == 1000) {
                return BatteryService.this.alterWirelessTxSwitch(status);
            }
            String access$800 = BatteryService.TAG;
            Slog.e(access$800, "you have no permission to call alterWirelessTxSwitch from uid:" + Binder.getCallingUid());
            return -1;
        }

        public int getWirelessTxSwitch() {
            if (Binder.getCallingUid() == 1000) {
                return BatteryService.this.getWirelessTxSwitch();
            }
            String access$800 = BatteryService.TAG;
            Slog.e(access$800, "you have no permission to call getWirelessTxSwitch from uid:" + Binder.getCallingUid());
            return 0;
        }

        public boolean supportWirelessTxCharge() {
            if (Binder.getCallingUid() == 1000) {
                return BatteryService.this.supportWirelessTxCharge();
            }
            String access$800 = BatteryService.TAG;
            Slog.e(access$800, "you have no permission to call supportWirelessTxCharge from uid:" + Binder.getCallingUid());
            return false;
        }
    }

    private final class BinderService extends Binder {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(BatteryService.this.mContext, BatteryService.TAG, pw)) {
                if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                    BatteryService.this.dumpInternal(fd, pw, args);
                } else {
                    BatteryService.this.dumpProto(fd);
                }
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    private final class CheckForShutdown implements Runnable {
        private CheckForShutdown() {
        }

        public void run() {
            PowerManagerService.lowLevelShutdown("battery");
        }
    }

    private final class HealthHalCallback extends IHealthInfoCallback.Stub implements HealthServiceWrapper.Callback {
        private HealthHalCallback() {
        }

        public void healthInfoChanged(android.hardware.health.V2_0.HealthInfo props) {
            BatteryService.this.update(props);
        }

        public void onRegistration(IHealth oldService, IHealth newService, String instance) {
            if (newService != null) {
                BatteryService.traceBegin("HealthUnregisterCallback");
                if (oldService != null) {
                    try {
                        int r = oldService.unregisterCallback(this);
                        if (r != 0) {
                            String access$800 = BatteryService.TAG;
                            Slog.w(access$800, "health: cannot unregister previous callback: " + Result.toString(r));
                        }
                    } catch (RemoteException ex) {
                        String access$8002 = BatteryService.TAG;
                        Slog.w(access$8002, "health: cannot unregister previous callback (transaction error): " + ex.getMessage());
                    } catch (Throwable th) {
                        BatteryService.traceEnd();
                        throw th;
                    }
                }
                BatteryService.traceEnd();
                BatteryService.traceBegin("HealthRegisterCallback");
                try {
                    int r2 = newService.registerCallback(this);
                    if (r2 != 0) {
                        String access$8003 = BatteryService.TAG;
                        Slog.w(access$8003, "health: cannot register callback: " + Result.toString(r2));
                        BatteryService.traceEnd();
                        return;
                    }
                    newService.update();
                    BatteryService.traceEnd();
                } catch (RemoteException ex2) {
                    String access$8004 = BatteryService.TAG;
                    Slog.e(access$8004, "health: cannot register callback (transaction error): " + ex2.getMessage());
                } catch (Throwable th2) {
                    BatteryService.traceEnd();
                    throw th2;
                }
            }
        }
    }

    @VisibleForTesting
    static final class HealthServiceWrapper {
        public static final String INSTANCE_HEALTHD = "backup";
        public static final String INSTANCE_VENDOR = "default";
        private static final String TAG = "HealthServiceWrapper";
        private static final List<String> sAllInstances = Arrays.asList(new String[]{INSTANCE_VENDOR, INSTANCE_HEALTHD});
        /* access modifiers changed from: private */
        public Callback mCallback;
        /* access modifiers changed from: private */
        public final HandlerThread mHandlerThread = new HandlerThread("HealthServiceRefresh");
        /* access modifiers changed from: private */
        public IHealthSupplier mHealthSupplier;
        /* access modifiers changed from: private */
        public String mInstanceName;
        /* access modifiers changed from: private */
        public final AtomicReference<IHealth> mLastService = new AtomicReference<>();
        private final IServiceNotification mNotification = new Notification();

        interface Callback {
            void onRegistration(IHealth iHealth, IHealth iHealth2, String str);
        }

        interface IHealthSupplier {
            IHealth get(String name) throws NoSuchElementException, RemoteException {
                return IHealth.getService(name, true);
            }
        }

        interface IServiceManagerSupplier {
            IServiceManager get() throws NoSuchElementException, RemoteException {
                return IServiceManager.getService();
            }
        }

        private class Notification extends IServiceNotification.Stub {
            private Notification() {
            }

            public final void onRegistration(String interfaceName, String instanceName, boolean preexisting) {
                if (IHealth.kInterfaceName.equals(interfaceName) && HealthServiceWrapper.this.mInstanceName.equals(instanceName)) {
                    HealthServiceWrapper.this.mHandlerThread.getThreadHandler().post(new Runnable() {
                        public void run() {
                            try {
                                IHealth newService = HealthServiceWrapper.this.mHealthSupplier.get(HealthServiceWrapper.this.mInstanceName);
                                IHealth oldService = (IHealth) HealthServiceWrapper.this.mLastService.getAndSet(newService);
                                if (!Objects.equals(newService, oldService)) {
                                    Slog.i(HealthServiceWrapper.TAG, "health: new instance registered " + HealthServiceWrapper.this.mInstanceName);
                                    HealthServiceWrapper.this.mCallback.onRegistration(oldService, newService, HealthServiceWrapper.this.mInstanceName);
                                }
                            } catch (RemoteException | NoSuchElementException ex) {
                                Slog.e(HealthServiceWrapper.TAG, "health: Cannot get instance '" + HealthServiceWrapper.this.mInstanceName + "': " + ex.getMessage() + ". Perhaps no permission?");
                            }
                        }
                    });
                }
            }
        }

        HealthServiceWrapper() {
        }

        /* access modifiers changed from: package-private */
        public IHealth getLastService() {
            return this.mLastService.get();
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public void init(Callback callback, IServiceManagerSupplier managerSupplier, IHealthSupplier healthSupplier) throws RemoteException, NoSuchElementException, NullPointerException {
            if (callback == null || managerSupplier == null || healthSupplier == null) {
                throw new NullPointerException();
            }
            this.mCallback = callback;
            this.mHealthSupplier = healthSupplier;
            IHealth newService = null;
            Iterator<String> it = sAllInstances.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String name = it.next();
                BatteryService.traceBegin("HealthInitGetService_" + name);
                try {
                    newService = healthSupplier.get(name);
                } catch (NoSuchElementException e) {
                } catch (Throwable th) {
                    BatteryService.traceEnd();
                    throw th;
                }
                BatteryService.traceEnd();
                if (newService != null) {
                    this.mInstanceName = name;
                    this.mLastService.set(newService);
                    break;
                }
            }
            if (this.mInstanceName == null || newService == null) {
                throw new NoSuchElementException(String.format("No IHealth service instance among %s is available. Perhaps no permission?", new Object[]{sAllInstances.toString()}));
            }
            this.mCallback.onRegistration(null, newService, this.mInstanceName);
            BatteryService.traceBegin("HealthInitRegisterNotification");
            this.mHandlerThread.start();
            try {
                managerSupplier.get().registerForNotifications(IHealth.kInterfaceName, this.mInstanceName, this.mNotification);
                BatteryService.traceEnd();
                Slog.i(TAG, "health: HealthServiceWrapper listening to instance " + this.mInstanceName);
            } catch (Throwable th2) {
                BatteryService.traceEnd();
                throw th2;
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public HandlerThread getHandlerThread() {
            return this.mHandlerThread;
        }
    }

    private final class Led {
        private final int mBatteryFullARGB;
        private final int mBatteryLedOff;
        private final int mBatteryLedOn;
        private final Light mBatteryLight;
        private final int mBatteryLowARGB;
        private final int mBatteryMediumARGB;

        public Led(Context context, LightsManager lights) {
            this.mBatteryLight = lights.getLight(3);
            this.mBatteryLowARGB = context.getResources().getInteger(17694840);
            this.mBatteryMediumARGB = context.getResources().getInteger(17694841);
            this.mBatteryFullARGB = context.getResources().getInteger(17694837);
            this.mBatteryLedOn = context.getResources().getInteger(17694839);
            this.mBatteryLedOff = context.getResources().getInteger(17694838);
        }

        public void updateLightsLocked() {
            int level = BatteryService.this.mHealthInfo.batteryLevel;
            int status = BatteryService.this.mHealthInfo.batteryStatus;
            if (level < BatteryService.this.mLowBatteryWarningLevel) {
                if (status == 2) {
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                } else {
                    this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                }
            } else if (status != 2 && status != 5) {
                this.mBatteryLight.turnOff();
            } else if (status == 5 || level >= 90) {
                this.mBatteryLight.setColor(this.mBatteryFullARGB);
            } else {
                this.mBatteryLight.setColor(this.mBatteryMediumARGB);
            }
        }
    }

    private final class LocalService extends BatteryManagerInternal {
        private LocalService() {
        }

        public boolean isPowered(int plugTypeSet) {
            boolean access$2200;
            synchronized (BatteryService.this.mLock) {
                access$2200 = BatteryService.this.isPoweredLocked(plugTypeSet);
            }
            return access$2200;
        }

        public int getPlugType() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mPlugType;
            }
            return i;
        }

        public int getBatteryLevel() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mHealthInfo.batteryLevel;
            }
            return i;
        }

        public int getBatteryChargeCounter() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mHealthInfo.batteryChargeCounter;
            }
            return i;
        }

        public int getBatteryFullCharge() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mHealthInfo.batteryFullCharge;
            }
            return i;
        }

        public boolean getBatteryLevelLow() {
            boolean access$2300;
            synchronized (BatteryService.this.mLock) {
                access$2300 = BatteryService.this.mBatteryLevelLow;
            }
            return access$2300;
        }

        public int getInvalidCharger() {
            int access$100;
            synchronized (BatteryService.this.mLock) {
                access$100 = BatteryService.this.mInvalidCharger;
            }
            return access$100;
        }

        public void updateBatteryLight(boolean enable, int ledOnMS, int ledOffMS) {
            synchronized (BatteryService.this.mLock) {
                BatteryService.this.updateLight(enable, ledOnMS, ledOffMS);
            }
        }

        public void notifyFrontCameraStates(boolean opened) {
            BatteryService.this.cameraUpdateLight(opened);
        }
    }

    class Shell extends ShellCommand {
        Shell() {
        }

        public int onCommand(String cmd) {
            return BatteryService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            BatteryService.dumpHelp(getOutPrintWriter());
        }
    }

    public BatteryService(Context context) {
        super(context);
        this.mContext = context;
        this.mHandler = new Handler(true);
        this.mLed = new Led(context, (LightsManager) getLocalService(LightsManager.class));
        this.mBatteryStats = BatteryStatsService.getService();
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAms = (ActivityManagerService) ServiceManager.getService("activity");
        this.mCriticalBatteryLevel = this.mContext.getResources().getInteger(17694757);
        this.mLowBatteryWarningLevel = this.mContext.getResources().getInteger(17694805);
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694804);
        this.mShutdownBatteryTemperature = this.mContext.getResources().getInteger(17694866);
        this.mBatteryLevelsEventQueue = new ArrayDeque<>();
        this.mMetricsLogger = new MetricsLogger();
        if (new File("/sys/devices/virtual/switch/invalid_charger/state").exists()) {
            new UEventObserver() {
                public void onUEvent(UEventObserver.UEvent event) {
                    int invalidCharger = BatteryService.CHARGE_WIRED_STATUS.equals(event.get("SWITCH_STATE"));
                    synchronized (BatteryService.this.mLock) {
                        if (BatteryService.this.mInvalidCharger != invalidCharger) {
                            int unused = BatteryService.this.mInvalidCharger = (int) invalidCharger;
                        }
                    }
                }
            }.startObserving("DEVPATH=/devices/virtual/switch/invalid_charger");
        }
    }

    /* JADX WARNING: type inference failed for: r2v1, types: [com.android.server.BatteryService$BatteryPropertiesRegistrar, android.os.IBinder] */
    public void onStart() {
        registerHealthCallback();
        this.mBinderService = new BinderService();
        publishBinderService("battery", this.mBinderService);
        this.mBatteryPropertiesRegistrar = new BatteryPropertiesRegistrar();
        publishBinderService("batteryproperties", this.mBatteryPropertiesRegistrar);
        publishLocalService(BatteryManagerInternal.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            synchronized (this.mLock) {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), false, new ContentObserver(this.mHandler) {
                    public void onChange(boolean selfChange) {
                        synchronized (BatteryService.this.mLock) {
                            BatteryService.this.updateBatteryWarningLevelLocked();
                        }
                    }
                }, -1);
                updateBatteryWarningLevelLocked();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerHealthCallback() {
        traceBegin("HealthInitWrapper");
        this.mHealthServiceWrapper = new HealthServiceWrapper();
        this.mHealthHalCallback = new HealthHalCallback();
        try {
            this.mHealthServiceWrapper.init(this.mHealthHalCallback, new HealthServiceWrapper.IServiceManagerSupplier() {
            }, new HealthServiceWrapper.IHealthSupplier() {
            });
            traceEnd();
            traceBegin("HealthInitWaitUpdate");
            long beforeWait = SystemClock.uptimeMillis();
            synchronized (this.mLock) {
                while (this.mHealthInfo == null) {
                    String str = TAG;
                    Slog.i(str, "health: Waited " + (SystemClock.uptimeMillis() - beforeWait) + "ms for callbacks. Waiting another " + 1000 + " ms...");
                    try {
                        this.mLock.wait(1000);
                    } catch (InterruptedException e) {
                        Slog.i(TAG, "health: InterruptedException when waiting for update.  Continuing...");
                    }
                }
            }
            String str2 = TAG;
            Slog.i(str2, "health: Waited " + (SystemClock.uptimeMillis() - beforeWait) + "ms and received the update.");
            traceEnd();
        } catch (RemoteException ex) {
            Slog.e(TAG, "health: cannot register callback. (RemoteException)");
            throw ex.rethrowFromSystemServer();
        } catch (NoSuchElementException ex2) {
            Slog.e(TAG, "health: cannot register callback. (no supported health HAL service)");
            throw ex2;
        } catch (Throwable th) {
            traceEnd();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void updateBatteryWarningLevelLocked() {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defWarnLevel = this.mContext.getResources().getInteger(17694805);
        this.mLowBatteryWarningLevel = Settings.Global.getInt(resolver, "low_power_trigger_level", defWarnLevel);
        if (this.mLowBatteryWarningLevel == 0) {
            this.mLowBatteryWarningLevel = defWarnLevel;
        }
        if (this.mLowBatteryWarningLevel < this.mCriticalBatteryLevel) {
            this.mLowBatteryWarningLevel = this.mCriticalBatteryLevel;
        }
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694804);
        processValuesLocked(true);
    }

    /* access modifiers changed from: private */
    public boolean isPoweredLocked(int plugTypeSet) {
        if (this.mHealthInfo.batteryStatus == 1) {
            return true;
        }
        if ((plugTypeSet & 1) != 0 && this.mHealthInfo.chargerAcOnline) {
            return true;
        }
        if ((plugTypeSet & 2) != 0 && this.mHealthInfo.chargerUsbOnline) {
            return true;
        }
        if ((plugTypeSet & 4) == 0 || !this.mHealthInfo.chargerWirelessOnline) {
            return false;
        }
        return true;
    }

    private boolean shouldSendBatteryLowLocked() {
        boolean plugged = this.mPlugType != 0;
        boolean oldPlugged = this.mLastPlugType != 0;
        if (plugged || this.mHealthInfo.batteryStatus == 1 || this.mHealthInfo.batteryLevel > this.mLowBatteryWarningLevel) {
            return false;
        }
        if (oldPlugged || this.mLastBatteryLevel > this.mLowBatteryWarningLevel) {
            return true;
        }
        return false;
    }

    private void shutdownIfNoPowerLocked() {
        if (this.mHealthInfo.batteryLevel == 0 && !isPoweredLocked(7)) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (BatteryService.this.mActivityManagerInternal.isSystemReady()) {
                        String access$800 = BatteryService.TAG;
                        Slog.e(access$800, "Shutdown because of: batteryLevel =" + BatteryService.this.mHealthInfo.batteryLevel);
                        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent.putExtra("android.intent.extra.REASON", "battery");
                        intent.setFlags(268435456);
                        BatteryService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    }
                }
            });
            if (this.mPendingCheckForShutdown == null) {
                this.mPendingCheckForShutdown = new CheckForShutdown();
                this.mHandler.postDelayed(this.mPendingCheckForShutdown, 20000);
            }
        } else if (this.mPendingCheckForShutdown != null) {
            this.mHandler.removeCallbacks(this.mPendingCheckForShutdown);
            this.mPendingCheckForShutdown = null;
        }
    }

    private void shutdownIfOverTempLocked() {
        if (this.mHealthInfo.batteryTemperature > this.mShutdownBatteryTemperature) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (BatteryService.this.mActivityManagerInternal.isSystemReady()) {
                        String access$800 = BatteryService.TAG;
                        Slog.e(access$800, "Shutdown because of: batteryTemperature =" + BatteryService.this.mHealthInfo.batteryTemperature);
                        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent.putExtra("android.intent.extra.REASON", "thermal,battery");
                        intent.setFlags(268435456);
                        BatteryService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void update(android.hardware.health.V2_0.HealthInfo info) {
        traceBegin("HealthInfoUpdate");
        printBatteryLog(this.mHealthInfo, info, this.mPlugType, this.mUpdatesStopped);
        synchronized (this.mLock) {
            if (!this.mUpdatesStopped) {
                this.mHealthInfo = info.legacy;
                processValuesLocked(false);
                this.mLock.notifyAll();
            } else {
                copy(this.mLastHealthInfo, info.legacy);
            }
        }
        traceEnd();
    }

    private static void copy(HealthInfo dst, HealthInfo src) {
        dst.chargerAcOnline = src.chargerAcOnline;
        dst.chargerUsbOnline = src.chargerUsbOnline;
        dst.chargerWirelessOnline = src.chargerWirelessOnline;
        dst.maxChargingCurrent = src.maxChargingCurrent;
        dst.maxChargingVoltage = src.maxChargingVoltage;
        dst.batteryStatus = src.batteryStatus;
        dst.batteryHealth = src.batteryHealth;
        dst.batteryPresent = src.batteryPresent;
        dst.batteryLevel = src.batteryLevel;
        dst.batteryVoltage = src.batteryVoltage;
        dst.batteryTemperature = src.batteryTemperature;
        dst.batteryCurrent = src.batteryCurrent;
        dst.batteryCycleCount = src.batteryCycleCount;
        dst.batteryFullCharge = src.batteryFullCharge;
        dst.batteryChargeCounter = src.batteryChargeCounter;
        dst.batteryTechnology = src.batteryTechnology;
    }

    private void processValuesLocked(boolean force) {
        boolean logOutlier = false;
        long dischargeDuration = 0;
        this.mBatteryLevelCritical = this.mHealthInfo.batteryStatus != 1 && this.mHealthInfo.batteryLevel <= this.mCriticalBatteryLevel;
        if (this.mHealthInfo.chargerAcOnline) {
            this.mPlugType = 1;
        } else if (this.mHealthInfo.chargerUsbOnline) {
            this.mPlugType = 2;
        } else if (this.mHealthInfo.chargerWirelessOnline) {
            this.mPlugType = 4;
        } else {
            this.mPlugType = 0;
        }
        try {
            this.mBatteryStats.setBatteryState(this.mHealthInfo.batteryStatus, this.mHealthInfo.batteryHealth, this.mPlugType, this.mHealthInfo.batteryLevel, this.mHealthInfo.batteryTemperature, this.mHealthInfo.batteryVoltage, this.mHealthInfo.batteryChargeCounter, this.mHealthInfo.batteryFullCharge);
        } catch (RemoteException e) {
        }
        shutdownIfNoPowerLocked();
        shutdownIfOverTempLocked();
        if (!(!force && this.mHealthInfo.batteryStatus == this.mLastBatteryStatus && this.mHealthInfo.batteryHealth == this.mLastBatteryHealth && this.mHealthInfo.batteryPresent == this.mLastBatteryPresent && this.mHealthInfo.batteryLevel == this.mLastBatteryLevel && this.mPlugType == this.mLastPlugType && this.mHealthInfo.batteryVoltage == this.mLastBatteryVoltage && this.mHealthInfo.batteryTemperature == this.mLastBatteryTemperature && this.mHealthInfo.maxChargingCurrent == this.mLastMaxChargingCurrent && this.mHealthInfo.maxChargingVoltage == this.mLastMaxChargingVoltage && this.mHealthInfo.batteryChargeCounter == this.mLastChargeCounter && this.mInvalidCharger == this.mLastInvalidCharger)) {
            if (this.mPlugType != this.mLastPlugType) {
                if (this.mLastPlugType == 0) {
                    this.mChargeStartLevel = this.mHealthInfo.batteryLevel;
                    this.mChargeStartTime = SystemClock.elapsedRealtime();
                    LogMaker builder = new LogMaker(1417);
                    builder.setType(4);
                    builder.addTaggedData(1421, Integer.valueOf(this.mPlugType));
                    builder.addTaggedData(1418, Integer.valueOf(this.mHealthInfo.batteryLevel));
                    this.mMetricsLogger.write(builder);
                    if (!(this.mDischargeStartTime == 0 || this.mDischargeStartLevel == this.mHealthInfo.batteryLevel)) {
                        long dischargeDuration2 = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                        logOutlier = true;
                        EventLog.writeEvent(EventLogTags.BATTERY_DISCHARGE, new Object[]{Long.valueOf(dischargeDuration2), Integer.valueOf(this.mDischargeStartLevel), Integer.valueOf(this.mHealthInfo.batteryLevel)});
                        this.mDischargeStartTime = 0;
                        dischargeDuration = dischargeDuration2;
                    }
                } else if (this.mPlugType == 0) {
                    this.mDischargeStartTime = SystemClock.elapsedRealtime();
                    this.mDischargeStartLevel = this.mHealthInfo.batteryLevel;
                    long chargeDuration = SystemClock.elapsedRealtime() - this.mChargeStartTime;
                    if (!(this.mChargeStartTime == 0 || chargeDuration == 0)) {
                        LogMaker builder2 = new LogMaker(1417);
                        builder2.setType(5);
                        builder2.addTaggedData(1421, Integer.valueOf(this.mLastPlugType));
                        builder2.addTaggedData(1420, Long.valueOf(chargeDuration));
                        builder2.addTaggedData(1418, Integer.valueOf(this.mChargeStartLevel));
                        builder2.addTaggedData(1419, Integer.valueOf(this.mHealthInfo.batteryLevel));
                        this.mMetricsLogger.write(builder2);
                    }
                    this.mChargeStartTime = 0;
                }
            }
            if (!(this.mHealthInfo.batteryStatus == this.mLastBatteryStatus && this.mHealthInfo.batteryHealth == this.mLastBatteryHealth && this.mHealthInfo.batteryPresent == this.mLastBatteryPresent && this.mPlugType == this.mLastPlugType)) {
                EventLog.writeEvent(EventLogTags.BATTERY_STATUS, new Object[]{Integer.valueOf(this.mHealthInfo.batteryStatus), Integer.valueOf(this.mHealthInfo.batteryHealth), Integer.valueOf(this.mHealthInfo.batteryPresent ? 1 : 0), Integer.valueOf(this.mPlugType), this.mHealthInfo.batteryTechnology});
            }
            if (this.mHealthInfo.batteryLevel != this.mLastBatteryLevel) {
                if (BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED > 0) {
                    if (this.mHealthInfo.batteryLevel >= BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED && this.mLastBatteryLevel < BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED) {
                        final Intent statusIntent = new Intent("com.huawei.intent.action.BATTERY_LEV_JOB_ALLOWED");
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                            }
                        });
                    } else if (this.mHealthInfo.batteryLevel < BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED && this.mLastBatteryLevel >= BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED) {
                        final Intent statusIntent2 = new Intent("com.huawei.intent.action.BATTERY_LEV_JOB_NOT_ALLOWED");
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                BatteryService.this.mContext.sendBroadcastAsUser(statusIntent2, UserHandle.ALL);
                            }
                        });
                    }
                }
                EventLog.writeEvent(EventLogTags.BATTERY_LEVEL, new Object[]{Integer.valueOf(this.mHealthInfo.batteryLevel), Integer.valueOf(this.mHealthInfo.batteryVoltage), Integer.valueOf(this.mHealthInfo.batteryTemperature)});
            }
            if (this.mBatteryLevelCritical && !this.mLastBatteryLevelCritical && this.mPlugType == 0) {
                logOutlier = true;
                dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
            }
            if (!this.mBatteryLevelLow) {
                if (this.mPlugType == 0 && this.mHealthInfo.batteryStatus != 1 && this.mHealthInfo.batteryLevel <= this.mLowBatteryWarningLevel) {
                    this.mBatteryLevelLow = true;
                }
            } else if (this.mPlugType != 0) {
                this.mBatteryLevelLow = false;
            } else if (this.mHealthInfo.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mBatteryLevelLow = false;
            } else if (force && this.mHealthInfo.batteryLevel >= this.mLowBatteryWarningLevel) {
                this.mBatteryLevelLow = false;
            }
            this.mSequence++;
            if (this.mPlugType != 0 && this.mLastPlugType == 0) {
                final Intent statusIntent3 = new Intent("android.intent.action.ACTION_POWER_CONNECTED");
                statusIntent3.setFlags(67108864);
                statusIntent3.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent3, UserHandle.ALL);
                    }
                });
                playRing();
                boolean isWirelessCharge = isWirelessCharge();
                Slog.d(TAG, "power connected, isWirelessCharge:" + isWirelessCharge);
                if (isWirelessCharge) {
                    new SystemVibrator(this.mContext).vibrate(300);
                } else if (HwVibrator.isSupportHwVibrator("haptic.battery.charging")) {
                    HwVibrator.setHwVibrator(Process.myUid(), this.mContext.getPackageName(), "haptic.battery.charging");
                }
                if (IS_AUTO_POWEROFF_ON) {
                    stopAutoPowerOff();
                }
            } else if (this.mPlugType == 0 && this.mLastPlugType != 0) {
                final Intent statusIntent4 = new Intent("android.intent.action.ACTION_POWER_DISCONNECTED");
                statusIntent4.setFlags(67108864);
                statusIntent4.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent4, UserHandle.ALL);
                    }
                });
                stopRing();
                if (IS_AUTO_POWEROFF_ON) {
                    startAutoPowerOff();
                }
            }
            if (shouldSendBatteryLowLocked()) {
                this.mSentLowBatteryBroadcast = true;
                final Intent statusIntent5 = new Intent("android.intent.action.BATTERY_LOW");
                statusIntent5.setFlags(67108864);
                statusIntent5.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent5, UserHandle.ALL);
                    }
                });
            } else if (this.mSentLowBatteryBroadcast && this.mHealthInfo.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mSentLowBatteryBroadcast = false;
                final Intent statusIntent6 = new Intent("android.intent.action.BATTERY_OKAY");
                statusIntent6.setFlags(67108864);
                statusIntent6.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent6, UserHandle.ALL);
                    }
                });
            }
            if (!force && this.mAms != null) {
                AbsHwMtmBroadcastResourceManager mHwMtmBRManager = this.mAms.getBgBroadcastQueue().getMtmBRManager();
                if (mHwMtmBRManager != null) {
                    if (mHwMtmBRManager.iawareNeedSkipBroadcastSend("android.intent.action.BATTERY_CHANGED", new Object[]{this.mHealthInfo, Integer.valueOf(this.mLastBatteryStatus), Integer.valueOf(this.mLastBatteryHealth), Boolean.valueOf(this.mLastBatteryPresent), Integer.valueOf(this.mLastBatteryLevel), Integer.valueOf(this.mPlugType), Integer.valueOf(this.mLastPlugType), Integer.valueOf(this.mLastBatteryVoltage), Integer.valueOf(this.mLastBatteryTemperature), Integer.valueOf(this.mLastMaxChargingCurrent), Integer.valueOf(this.mLastMaxChargingVoltage), Integer.valueOf(this.mLastChargeCounter), Integer.valueOf(this.mInvalidCharger), Integer.valueOf(this.mLastInvalidCharger)})) {
                        return;
                    }
                }
            }
            sendBatteryChangedIntentLocked();
            if (this.mLastBatteryLevel != this.mHealthInfo.batteryLevel) {
                sendBatteryLevelChangedIntentLocked();
            }
            updateLight();
            if (logOutlier && dischargeDuration != 0) {
                logOutlierLocked(dischargeDuration);
            }
            this.mLastBatteryStatus = this.mHealthInfo.batteryStatus;
            this.mLastBatteryHealth = this.mHealthInfo.batteryHealth;
            this.mLastBatteryPresent = this.mHealthInfo.batteryPresent;
            this.mLastBatteryLevel = this.mHealthInfo.batteryLevel;
            this.mLastPlugType = this.mPlugType;
            this.mLastBatteryVoltage = this.mHealthInfo.batteryVoltage;
            this.mLastBatteryTemperature = this.mHealthInfo.batteryTemperature;
            this.mLastMaxChargingCurrent = this.mHealthInfo.maxChargingCurrent;
            this.mLastMaxChargingVoltage = this.mHealthInfo.maxChargingVoltage;
            this.mLastChargeCounter = this.mHealthInfo.batteryChargeCounter;
            this.mLastBatteryLevelCritical = this.mBatteryLevelCritical;
            this.mLastInvalidCharger = this.mInvalidCharger;
        }
    }

    private void sendBatteryChangedIntentLocked() {
        Intent intent = new Intent("android.intent.action.BATTERY_CHANGED");
        intent.addFlags(1610612736);
        int icon = getIconLocked(this.mHealthInfo.batteryLevel);
        String status = isWirelessCharge() ? CHARGE_WIRELESS_STATUS : CHARGE_WIRED_STATUS;
        intent.putExtra(CHARGE_STATUS, status);
        String str = TAG;
        Slog.i(str, "normal charge status : " + status);
        intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
        intent.putExtra("status", this.mHealthInfo.batteryStatus);
        intent.putExtra("health", this.mHealthInfo.batteryHealth);
        intent.putExtra("present", this.mHealthInfo.batteryPresent);
        intent.putExtra("level", this.mHealthInfo.batteryLevel);
        intent.putExtra("battery_low", this.mSentLowBatteryBroadcast);
        intent.putExtra("scale", 100);
        intent.putExtra("icon-small", icon);
        intent.putExtra("plugged", this.mPlugType);
        intent.putExtra("voltage", this.mHealthInfo.batteryVoltage);
        intent.putExtra("temperature", this.mHealthInfo.batteryTemperature);
        intent.putExtra("technology", this.mHealthInfo.batteryTechnology);
        intent.putExtra("invalid_charger", this.mInvalidCharger);
        intent.putExtra("max_charging_current", this.mHealthInfo.maxChargingCurrent);
        intent.putExtra("max_charging_voltage", this.mHealthInfo.maxChargingVoltage);
        intent.putExtra("charge_counter", this.mHealthInfo.batteryChargeCounter);
        this.mHandler.post(new Runnable(intent) {
            private final /* synthetic */ Intent f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                ActivityManager.broadcastStickyIntent(this.f$0, -1);
            }
        });
    }

    private void sendBatteryLevelChangedIntentLocked() {
        Bundle event = new Bundle();
        long now = SystemClock.elapsedRealtime();
        event.putInt(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
        event.putInt("status", this.mHealthInfo.batteryStatus);
        event.putInt("health", this.mHealthInfo.batteryHealth);
        event.putBoolean("present", this.mHealthInfo.batteryPresent);
        event.putInt("level", this.mHealthInfo.batteryLevel);
        event.putBoolean("battery_low", this.mSentLowBatteryBroadcast);
        event.putInt("scale", 100);
        event.putInt("plugged", this.mPlugType);
        event.putInt("voltage", this.mHealthInfo.batteryVoltage);
        event.putLong("android.os.extra.EVENT_TIMESTAMP", now);
        boolean queueWasEmpty = this.mBatteryLevelsEventQueue.isEmpty();
        this.mBatteryLevelsEventQueue.add(event);
        if (this.mBatteryLevelsEventQueue.size() > 100) {
            this.mBatteryLevelsEventQueue.removeFirst();
        }
        if (queueWasEmpty) {
            this.mHandler.postDelayed(new Runnable() {
                public final void run() {
                    BatteryService.this.sendEnqueuedBatteryLevelChangedEvents();
                }
            }, now - this.mLastBatteryLevelChangedSentMs > 60000 ? 0 : (this.mLastBatteryLevelChangedSentMs + 60000) - now);
        }
    }

    /* access modifiers changed from: private */
    public void sendEnqueuedBatteryLevelChangedEvents() {
        ArrayList<Bundle> events;
        synchronized (this.mLock) {
            events = new ArrayList<>(this.mBatteryLevelsEventQueue);
            this.mBatteryLevelsEventQueue.clear();
        }
        Intent intent = new Intent("android.intent.action.BATTERY_LEVEL_CHANGED");
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        intent.putParcelableArrayListExtra("android.os.extra.EVENTS", events);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.BATTERY_STATS");
        this.mLastBatteryLevelChangedSentMs = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: protected */
    public boolean isWirelessCharge() {
        try {
            return "11".equals(FileUtils.readTextFile(new File(CHARGER_TYPE), 0, null).trim());
        } catch (IOException e) {
            Slog.e(TAG, "Error occurs when read sys/class/hw_power/charger/charge_data/chargerType", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public int alterWirelessTxSwitch(int status) {
        return alterWirelessTxSwitchInternal(status);
    }

    /* access modifiers changed from: private */
    public int getWirelessTxSwitch() {
        return getWirelessTxSwitchInternal();
    }

    /* access modifiers changed from: private */
    public boolean supportWirelessTxCharge() {
        return supportWirelessTxChargeInternal();
    }

    private void logBatteryStatsLocked() {
        String str;
        StringBuilder sb;
        IBinder batteryInfoService = ServiceManager.getService("batterystats");
        if (batteryInfoService != null) {
            DropBoxManager db = (DropBoxManager) this.mContext.getSystemService("dropbox");
            if (db != null && db.isTagEnabled("BATTERY_DISCHARGE_INFO")) {
                File dumpFile = null;
                FileOutputStream dumpStream = null;
                try {
                    dumpFile = new File("/data/system/batterystats.dump");
                    dumpStream = new FileOutputStream(dumpFile);
                    batteryInfoService.dump(dumpStream.getFD(), DUMPSYS_ARGS);
                    FileUtils.sync(dumpStream);
                    db.addFile("BATTERY_DISCHARGE_INFO", dumpFile, 2);
                    try {
                        dumpStream.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "failed to close dumpsys output stream");
                    }
                    if (!dumpFile.delete()) {
                        str = TAG;
                        sb = new StringBuilder();
                        sb.append("failed to delete temporary dumpsys file: ");
                        sb.append(dumpFile.getAbsolutePath());
                        Slog.e(str, sb.toString());
                    }
                } catch (RemoteException e2) {
                    Slog.e(TAG, "failed to dump battery service", e2);
                    if (dumpStream != null) {
                        try {
                            dumpStream.close();
                        } catch (IOException e3) {
                            Slog.e(TAG, "failed to close dumpsys output stream");
                        }
                    }
                    if (dumpFile != null && !dumpFile.delete()) {
                        str = TAG;
                        sb = new StringBuilder();
                    }
                } catch (IOException e4) {
                    Slog.e(TAG, "failed to write dumpsys file", e4);
                    if (dumpStream != null) {
                        try {
                            dumpStream.close();
                        } catch (IOException e5) {
                            Slog.e(TAG, "failed to close dumpsys output stream");
                        }
                    }
                    if (dumpFile != null && !dumpFile.delete()) {
                        str = TAG;
                        sb = new StringBuilder();
                    }
                } catch (Throwable th) {
                    if (dumpStream != null) {
                        try {
                            dumpStream.close();
                        } catch (IOException e6) {
                            Slog.e(TAG, "failed to close dumpsys output stream");
                        }
                    }
                    if (dumpFile != null && !dumpFile.delete()) {
                        String str2 = TAG;
                        Slog.e(str2, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    }
                    throw th;
                }
            }
        }
    }

    private void logOutlierLocked(long duration) {
        ContentResolver cr = this.mContext.getContentResolver();
        String dischargeThresholdString = Settings.Global.getString(cr, "battery_discharge_threshold");
        String durationThresholdString = Settings.Global.getString(cr, "battery_discharge_duration_threshold");
        if (dischargeThresholdString != null && durationThresholdString != null) {
            try {
                long durationThreshold = Long.parseLong(durationThresholdString);
                int dischargeThreshold = Integer.parseInt(dischargeThresholdString);
                if (duration <= durationThreshold && this.mDischargeStartLevel - this.mHealthInfo.batteryLevel >= dischargeThreshold) {
                    logBatteryStatsLocked();
                }
            } catch (NumberFormatException e) {
                String str = TAG;
                Slog.e(str, "Invalid DischargeThresholds GService string: " + durationThresholdString + " or " + dischargeThresholdString);
            }
        }
    }

    private int getIconLocked(int level) {
        if (this.mHealthInfo.batteryStatus == 2) {
            return 17303498;
        }
        if (this.mHealthInfo.batteryStatus == 3) {
            return 17303484;
        }
        if (this.mHealthInfo.batteryStatus != 4 && this.mHealthInfo.batteryStatus != 5) {
            return 17303512;
        }
        if (!isPoweredLocked(7) || this.mHealthInfo.batteryLevel < 100) {
            return 17303484;
        }
        return 17303498;
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Battery service (battery) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set [-f] [ac|usb|wireless|status|level|temp|present|invalid] <value>");
        pw.println("    Force a battery property value, freezing battery state.");
        pw.println("    -f: force a battery change broadcast be sent, prints new sequence.");
        pw.println("  unplug [-f]");
        pw.println("    Force battery unplugged, freezing battery state.");
        pw.println("    -f: force a battery change broadcast be sent, prints new sequence.");
        pw.println("  reset [-f]");
        pw.println("    Unfreeze battery state, returning to current hardware values.");
        pw.println("    -f: force a battery change broadcast be sent, prints new sequence.");
    }

    /* access modifiers changed from: package-private */
    public int parseOptions(Shell shell) {
        int opts = 0;
        while (true) {
            String nextOption = shell.getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                return opts;
            }
            if ("-f".equals(opt)) {
                opts |= 1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x017d, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x017e, code lost:
        r3.batteryPresent = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0181, code lost:
        r0.println("Unknown set option: " + r2);
        r8 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0194, code lost:
        if (r8 == false) goto L_0x01f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0196, code lost:
        r9 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
        r11.mUpdatesStopped = true;
        processValuesFromShellLocked(r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:?, code lost:
        android.os.Binder.restoreCallingIdentity(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0113, code lost:
        r3 = 65535;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0114, code lost:
        switch(r3) {
            case 0: goto L_0x0173;
            case 1: goto L_0x0165;
            case 2: goto L_0x0157;
            case 3: goto L_0x0149;
            case 4: goto L_0x0140;
            case 5: goto L_0x0137;
            case 6: goto L_0x012d;
            case 7: goto L_0x0123;
            case 8: goto L_0x011b;
            default: goto L_0x0117;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x011b, code lost:
        r11.mInvalidCharger = java.lang.Integer.parseInt(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0123, code lost:
        r11.mHealthInfo.batteryTemperature = java.lang.Integer.parseInt(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x012d, code lost:
        r11.mHealthInfo.batteryChargeCounter = java.lang.Integer.parseInt(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0137, code lost:
        r11.mHealthInfo.batteryLevel = java.lang.Integer.parseInt(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0140, code lost:
        r11.mHealthInfo.batteryStatus = java.lang.Integer.parseInt(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0149, code lost:
        r3 = r11.mHealthInfo;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x014f, code lost:
        if (java.lang.Integer.parseInt(r7) == 0) goto L_0x0153;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0151, code lost:
        r9 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0153, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0154, code lost:
        r3.chargerWirelessOnline = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0157, code lost:
        r3 = r11.mHealthInfo;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x015d, code lost:
        if (java.lang.Integer.parseInt(r7) == 0) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x015f, code lost:
        r9 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0161, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0162, code lost:
        r3.chargerUsbOnline = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0165, code lost:
        r3 = r11.mHealthInfo;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x016b, code lost:
        if (java.lang.Integer.parseInt(r7) == 0) goto L_0x016f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x016d, code lost:
        r9 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x016f, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0170, code lost:
        r3.chargerAcOnline = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0173, code lost:
        r3 = r11.mHealthInfo;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0179, code lost:
        if (java.lang.Integer.parseInt(r7) == 0) goto L_0x017d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x017b, code lost:
        r9 = true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x01c0  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007a  */
    public int onShellCommand(Shell shell, String cmd) {
        boolean z;
        long ident;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        int hashCode = cmd.hashCode();
        char c = 2;
        if (hashCode != -840325209) {
            if (hashCode != 113762) {
                if (hashCode == 108404047 && cmd.equals("reset")) {
                    z = true;
                    switch (z) {
                        case false:
                            int opts = parseOptions(shell);
                            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                            if (!this.mUpdatesStopped) {
                                copy(this.mLastHealthInfo, this.mHealthInfo);
                            }
                            this.mHealthInfo.chargerAcOnline = false;
                            this.mHealthInfo.chargerUsbOnline = false;
                            this.mHealthInfo.chargerWirelessOnline = false;
                            long ident2 = Binder.clearCallingIdentity();
                            try {
                                this.mUpdatesStopped = true;
                                processValuesFromShellLocked(pw, opts);
                                break;
                            } finally {
                                Binder.restoreCallingIdentity(ident2);
                            }
                        case true:
                            int opts2 = parseOptions(shell);
                            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                            String key = shell.getNextArg();
                            if (key == null) {
                                pw.println("No property specified");
                                return -1;
                            }
                            String value = shell.getNextArg();
                            if (value == null) {
                                pw.println("No value specified");
                                return -1;
                            }
                            try {
                                if (!this.mUpdatesStopped) {
                                    copy(this.mLastHealthInfo, this.mHealthInfo);
                                }
                                boolean update = true;
                                switch (key.hashCode()) {
                                    case -1000044642:
                                        if (key.equals("wireless")) {
                                            c = 3;
                                            break;
                                        }
                                    case -892481550:
                                        if (key.equals("status")) {
                                            c = 4;
                                            break;
                                        }
                                    case -318277445:
                                        if (key.equals("present")) {
                                            c = 0;
                                            break;
                                        }
                                    case 3106:
                                        if (key.equals("ac")) {
                                            c = 1;
                                            break;
                                        }
                                    case 116100:
                                        if (key.equals("usb")) {
                                            break;
                                        }
                                    case 3556308:
                                        if (key.equals("temp")) {
                                            c = 7;
                                            break;
                                        }
                                    case 102865796:
                                        if (key.equals("level")) {
                                            c = 5;
                                            break;
                                        }
                                    case 957830652:
                                        if (key.equals("counter")) {
                                            c = 6;
                                            break;
                                        }
                                    case 1959784951:
                                        if (key.equals("invalid")) {
                                            c = 8;
                                            break;
                                        }
                                }
                            } catch (NumberFormatException e) {
                                pw.println("Bad value: " + value);
                                return -1;
                            } catch (Throwable th) {
                                Binder.restoreCallingIdentity(ident);
                                throw th;
                            }
                            break;
                        case true:
                            int opts3 = parseOptions(shell);
                            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                            long ident3 = Binder.clearCallingIdentity();
                            try {
                                if (this.mUpdatesStopped) {
                                    this.mUpdatesStopped = false;
                                    copy(this.mHealthInfo, this.mLastHealthInfo);
                                    processValuesFromShellLocked(pw, opts3);
                                }
                                break;
                            } finally {
                                Binder.restoreCallingIdentity(ident3);
                            }
                        default:
                            return shell.handleDefaultCommands(cmd);
                    }
                    return 0;
                }
            } else if (cmd.equals("set")) {
                z = true;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                    case true:
                        break;
                }
                return 0;
            }
        } else if (cmd.equals("unplug")) {
            z = false;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
                case true:
                    break;
            }
            return 0;
        }
        z = true;
        switch (z) {
            case false:
                break;
            case true:
                break;
            case true:
                break;
        }
        return 0;
    }

    private void processValuesFromShellLocked(PrintWriter pw, int opts) {
        processValuesLocked((opts & 1) != 0);
        if ((opts & 1) != 0) {
            pw.println(this.mSequence);
        }
    }

    /* access modifiers changed from: private */
    public void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            if (args != null) {
                try {
                    if (args.length != 0) {
                        Flog.i(NsdService.NativeResponseCode.SERVICE_REGISTRATION_FAILED, "dumpInternal args[0]: " + args[0] + " mUpdatesStopped: " + this.mUpdatesStopped);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (!(args == null || args.length == 0)) {
                if (!"-a".equals(args[0])) {
                    new Shell().exec(this.mBinderService, null, fd, null, args, null, new ResultReceiver(null));
                }
            }
            pw.println("Current Battery Service state:");
            if (this.mUpdatesStopped) {
                pw.println("  (UPDATES STOPPED -- use 'reset' to restart)");
            }
            pw.println("  AC powered: " + this.mHealthInfo.chargerAcOnline);
            pw.println("  USB powered: " + this.mHealthInfo.chargerUsbOnline);
            pw.println("  Wireless powered: " + this.mHealthInfo.chargerWirelessOnline);
            pw.println("  Max charging current: " + this.mHealthInfo.maxChargingCurrent);
            pw.println("  Max charging voltage: " + this.mHealthInfo.maxChargingVoltage);
            pw.println("  Charge counter: " + this.mHealthInfo.batteryChargeCounter);
            pw.println("  status: " + this.mHealthInfo.batteryStatus);
            pw.println("  health: " + this.mHealthInfo.batteryHealth);
            pw.println("  present: " + this.mHealthInfo.batteryPresent);
            pw.println("  level: " + this.mHealthInfo.batteryLevel);
            pw.println("  scale: 100");
            pw.println("  voltage: " + this.mHealthInfo.batteryVoltage);
            pw.println("  temperature: " + this.mHealthInfo.batteryTemperature);
            pw.println("  technology: " + this.mHealthInfo.batteryTechnology);
        }
    }

    /* access modifiers changed from: private */
    public void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mLock) {
            proto.write(1133871366145L, this.mUpdatesStopped);
            int batteryPluggedValue = 0;
            if (this.mHealthInfo.chargerAcOnline) {
                batteryPluggedValue = 1;
            } else if (this.mHealthInfo.chargerUsbOnline) {
                batteryPluggedValue = 2;
            } else if (this.mHealthInfo.chargerWirelessOnline) {
                batteryPluggedValue = 4;
            }
            proto.write(1159641169922L, batteryPluggedValue);
            proto.write(1120986464259L, this.mHealthInfo.maxChargingCurrent);
            proto.write(1120986464260L, this.mHealthInfo.maxChargingVoltage);
            proto.write(1120986464261L, this.mHealthInfo.batteryChargeCounter);
            proto.write(1159641169926L, this.mHealthInfo.batteryStatus);
            proto.write(1159641169927L, this.mHealthInfo.batteryHealth);
            proto.write(1133871366152L, this.mHealthInfo.batteryPresent);
            proto.write(1120986464265L, this.mHealthInfo.batteryLevel);
            proto.write(1120986464266L, 100);
            proto.write(1120986464267L, this.mHealthInfo.batteryVoltage);
            proto.write(1120986464268L, this.mHealthInfo.batteryTemperature);
            proto.write(1138166333453L, this.mHealthInfo.batteryTechnology);
        }
        proto.flush();
    }

    /* access modifiers changed from: private */
    public static void traceBegin(String name) {
        Trace.traceBegin(524288, name);
    }

    /* access modifiers changed from: private */
    public static void traceEnd() {
        Trace.traceEnd(524288);
    }

    /* access modifiers changed from: protected */
    public HealthInfo getHealthInfo() {
        return this.mHealthInfo;
    }

    /* access modifiers changed from: protected */
    public int getLowBatteryWarningLevel() {
        return this.mLowBatteryWarningLevel;
    }

    /* access modifiers changed from: protected */
    public void updateLight() {
        this.mLed.updateLightsLocked();
    }

    /* access modifiers changed from: protected */
    public void cameraUpdateLight(boolean enable) {
    }

    /* access modifiers changed from: protected */
    public void startAutoPowerOff() {
    }

    /* access modifiers changed from: protected */
    public void stopAutoPowerOff() {
    }
}
