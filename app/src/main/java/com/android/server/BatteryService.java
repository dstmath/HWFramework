package com.android.server;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.BatteryManagerInternal;
import android.os.BatteryProperties;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBatteryPropertiesListener.Stub;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.EventLog;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import com.android.server.am.BatteryStatsService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.power.PowerManagerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class BatteryService extends AbsBatteryService {
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int BATTERY_SCALE = 100;
    private static final boolean DEBUG = false;
    private static final String[] DUMPSYS_ARGS = null;
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    private static final boolean IS_EMU = false;
    private static final boolean IS_FPGA = false;
    private static final boolean IS_UDP = false;
    private static final int SHUTDOWN_DELAY_TIMEOUT = 20000;
    private static final String TAG = null;
    private static String boardname;
    private boolean mBatteryLevelCritical;
    private boolean mBatteryLevelLow;
    private BatteryProperties mBatteryProps;
    private final IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private final Context mContext;
    private int mCriticalBatteryLevel;
    private int mDischargeStartLevel;
    private long mDischargeStartTime;
    private final Handler mHandler;
    private int mInvalidCharger;
    private int mLastBatteryHealth;
    private int mLastBatteryLevel;
    private boolean mLastBatteryLevelCritical;
    private boolean mLastBatteryPresent;
    private final BatteryProperties mLastBatteryProps;
    private int mLastBatteryStatus;
    private int mLastBatteryTemperature;
    private int mLastBatteryVoltage;
    private int mLastChargeCounter;
    private int mLastInvalidCharger;
    private int mLastMaxChargingCurrent;
    private int mLastMaxChargingVoltage;
    private int mLastPlugType;
    private Led mLed;
    private final Object mLock;
    private int mLowBatteryCloseWarningLevel;
    private int mLowBatteryWarningLevel;
    private CheckForShutdown mPendingCheckForShutdown;
    private int mPlugType;
    private boolean mSentLowBatteryBroadcast;
    private int mShutdownBatteryTemperature;
    private boolean mUpdatesStopped;

    /* renamed from: com.android.server.BatteryService.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            synchronized (BatteryService.this.mLock) {
                BatteryService.this.updateBatteryWarningLevelLocked();
            }
        }
    }

    /* renamed from: com.android.server.BatteryService.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ Intent val$intent;

        AnonymousClass9(Intent val$intent) {
            this.val$intent = val$intent;
        }

        public void run() {
            ActivityManagerNative.broadcastStickyIntent(this.val$intent, null, -1);
        }
    }

    protected final class BatteryListener extends Stub {
        protected BatteryListener() {
        }

        public void batteryPropertiesChanged(BatteryProperties props) {
            long identity = Binder.clearCallingIdentity();
            try {
                BatteryService.this.update(props);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private final class BinderService extends Binder {
        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (BatteryService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump Battery service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else {
                BatteryService.this.dumpInternal(fd, pw, args);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, resultReceiver);
        }
    }

    private final class CheckForShutdown implements Runnable {
        private CheckForShutdown() {
        }

        public void run() {
            PowerManagerService.lowLevelShutdown("battery");
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
            this.mBatteryLowARGB = context.getResources().getInteger(17694811);
            this.mBatteryMediumARGB = context.getResources().getInteger(17694812);
            this.mBatteryFullARGB = context.getResources().getInteger(17694813);
            this.mBatteryLedOn = context.getResources().getInteger(17694814);
            this.mBatteryLedOff = context.getResources().getInteger(17694815);
        }

        public void updateLightsLocked() {
            int level = BatteryService.this.mBatteryProps.batteryLevel;
            int status = BatteryService.this.mBatteryProps.batteryStatus;
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
            boolean -wrap0;
            synchronized (BatteryService.this.mLock) {
                -wrap0 = BatteryService.this.isPoweredLocked(plugTypeSet);
            }
            return -wrap0;
        }

        public int getPlugType() {
            int -get7;
            synchronized (BatteryService.this.mLock) {
                -get7 = BatteryService.this.mPlugType;
            }
            return -get7;
        }

        public int getBatteryLevel() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mBatteryProps.batteryLevel;
            }
            return i;
        }

        public boolean getBatteryLevelLow() {
            boolean -get1;
            synchronized (BatteryService.this.mLock) {
                -get1 = BatteryService.this.mBatteryLevelLow;
            }
            return -get1;
        }

        public int getInvalidCharger() {
            int -get4;
            synchronized (BatteryService.this.mLock) {
                -get4 = BatteryService.this.mInvalidCharger;
            }
            return -get4;
        }

        public void updateBatteryLight(boolean enable, int ledOnMS, int ledOffMS) {
            synchronized (BatteryService.this.mLock) {
                BatteryService.this.updateLight(enable, ledOnMS, ledOffMS);
            }
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.BatteryService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.BatteryService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BatteryService.<clinit>():void");
    }

    public BatteryService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mLastBatteryProps = new BatteryProperties();
        this.mLastPlugType = -1;
        this.mSentLowBatteryBroadcast = IS_UDP;
        this.mContext = context;
        this.mHandler = new Handler(true);
        this.mLed = new Led(context, (LightsManager) getLocalService(LightsManager.class));
        this.mBatteryStats = BatteryStatsService.getService();
        this.mCriticalBatteryLevel = this.mContext.getResources().getInteger(17694805);
        this.mLowBatteryWarningLevel = this.mContext.getResources().getInteger(17694807);
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694808);
        this.mShutdownBatteryTemperature = this.mContext.getResources().getInteger(17694806);
        if (new File("/sys/devices/virtual/switch/invalid_charger/state").exists()) {
            new UEventObserver() {
                public void onUEvent(UEvent event) {
                    int invalidCharger = "1".equals(event.get("SWITCH_STATE")) ? 1 : BatteryService.BATTERY_PLUGGED_NONE;
                    synchronized (BatteryService.this.mLock) {
                        if (BatteryService.this.mInvalidCharger != invalidCharger) {
                            BatteryService.this.mInvalidCharger = invalidCharger;
                        }
                    }
                }
            }.startObserving("DEVPATH=/devices/virtual/switch/invalid_charger");
        }
    }

    public void onStart() {
        try {
            IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getService("batteryproperties")).registerListener(new BatteryListener());
        } catch (RemoteException e) {
        }
        this.mBinderService = new BinderService();
        publishBinderService("battery", this.mBinderService);
        publishLocalService(BatteryManagerInternal.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
            synchronized (this.mLock) {
                this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("low_power_trigger_level"), IS_UDP, new AnonymousClass2(this.mHandler), -1);
                updateBatteryWarningLevelLocked();
            }
        }
    }

    private void updateBatteryWarningLevelLocked() {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defWarnLevel = this.mContext.getResources().getInteger(17694807);
        this.mLowBatteryWarningLevel = Global.getInt(resolver, "low_power_trigger_level", defWarnLevel);
        if (this.mLowBatteryWarningLevel == 0) {
            this.mLowBatteryWarningLevel = defWarnLevel;
        }
        if (this.mLowBatteryWarningLevel < this.mCriticalBatteryLevel) {
            this.mLowBatteryWarningLevel = this.mCriticalBatteryLevel;
        }
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694808);
        processValuesLocked(true);
    }

    private boolean isPoweredLocked(int plugTypeSet) {
        if (this.mBatteryProps.batteryStatus == 1) {
            return true;
        }
        if ((plugTypeSet & 1) != 0 && this.mBatteryProps.chargerAcOnline) {
            return true;
        }
        if ((plugTypeSet & 2) == 0 || !this.mBatteryProps.chargerUsbOnline) {
            return ((plugTypeSet & 4) == 0 || !this.mBatteryProps.chargerWirelessOnline) ? IS_UDP : true;
        } else {
            return true;
        }
    }

    private boolean shouldSendBatteryLowLocked() {
        boolean plugged = this.mPlugType != 0 ? true : IS_UDP;
        boolean oldPlugged = this.mLastPlugType != 0 ? true : IS_UDP;
        if (plugged || this.mBatteryProps.batteryStatus == 1 || this.mBatteryProps.batteryLevel > this.mLowBatteryWarningLevel) {
            return IS_UDP;
        }
        if (oldPlugged || this.mLastBatteryLevel > this.mLowBatteryWarningLevel) {
            return true;
        }
        return IS_UDP;
    }

    private void shutdownIfNoPowerLocked() {
        if (this.mBatteryProps.batteryLevel == 0 && !IS_EMU && !IS_FPGA && !IS_UDP) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (ActivityManagerNative.isSystemReady()) {
                        Slog.e(BatteryService.TAG, "Shutdown because of: batteryLevel =" + BatteryService.this.mBatteryProps.batteryLevel);
                        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", BatteryService.IS_UDP);
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
        if (this.mBatteryProps.batteryTemperature > this.mShutdownBatteryTemperature) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (ActivityManagerNative.isSystemReady()) {
                        Slog.e(BatteryService.TAG, "Shutdown because of: batteryTemperature =" + BatteryService.this.mBatteryProps.batteryTemperature);
                        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", BatteryService.IS_UDP);
                        intent.setFlags(268435456);
                        BatteryService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    }
                }
            });
        }
    }

    private void update(BatteryProperties props) {
        printBatteryLog(this.mBatteryProps, props, this.mPlugType, this.mUpdatesStopped);
        synchronized (this.mLock) {
            if (this.mUpdatesStopped) {
                this.mLastBatteryProps.set(props);
            } else {
                this.mBatteryProps = props;
                processValuesLocked(IS_UDP);
            }
        }
    }

    private void processValuesLocked(boolean force) {
        boolean logOutlier = IS_UDP;
        long dischargeDuration = 0;
        this.mBatteryLevelCritical = this.mBatteryProps.batteryLevel <= this.mCriticalBatteryLevel ? true : IS_UDP;
        if (this.mBatteryProps.chargerAcOnline) {
            this.mPlugType = 1;
        } else if (this.mBatteryProps.chargerUsbOnline) {
            this.mPlugType = 2;
        } else if (this.mBatteryProps.chargerWirelessOnline) {
            this.mPlugType = 4;
        } else {
            this.mPlugType = BATTERY_PLUGGED_NONE;
        }
        try {
            this.mBatteryStats.setBatteryState(this.mBatteryProps.batteryStatus, this.mBatteryProps.batteryHealth, this.mPlugType, this.mBatteryProps.batteryLevel, this.mBatteryProps.batteryTemperature, this.mBatteryProps.batteryVoltage, this.mBatteryProps.batteryChargeCounter);
        } catch (RemoteException e) {
        }
        shutdownIfNoPowerLocked();
        shutdownIfOverTempLocked();
        if (!force && this.mBatteryProps.batteryStatus == this.mLastBatteryStatus && this.mBatteryProps.batteryHealth == this.mLastBatteryHealth && this.mBatteryProps.batteryPresent == this.mLastBatteryPresent && this.mBatteryProps.batteryLevel == this.mLastBatteryLevel && this.mPlugType == this.mLastPlugType && this.mBatteryProps.batteryVoltage == this.mLastBatteryVoltage && this.mBatteryProps.batteryTemperature == this.mLastBatteryTemperature && this.mBatteryProps.maxChargingCurrent == this.mLastMaxChargingCurrent && this.mBatteryProps.maxChargingVoltage == this.mLastMaxChargingVoltage && this.mBatteryProps.batteryChargeCounter == this.mLastChargeCounter) {
            if (this.mInvalidCharger == this.mLastInvalidCharger) {
                return;
            }
        }
        if (this.mPlugType != this.mLastPlugType) {
            if (this.mLastPlugType == 0) {
                if (!(this.mDischargeStartTime == 0 || this.mDischargeStartLevel == this.mBatteryProps.batteryLevel)) {
                    dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                    logOutlier = true;
                    EventLog.writeEvent(EventLogTags.BATTERY_DISCHARGE, new Object[]{Long.valueOf(dischargeDuration), Integer.valueOf(this.mDischargeStartLevel), Integer.valueOf(this.mBatteryProps.batteryLevel)});
                    this.mDischargeStartTime = 0;
                }
            } else if (this.mPlugType == 0) {
                this.mDischargeStartTime = SystemClock.elapsedRealtime();
                this.mDischargeStartLevel = this.mBatteryProps.batteryLevel;
            }
        }
        if (this.mBatteryProps.batteryStatus == this.mLastBatteryStatus && this.mBatteryProps.batteryHealth == this.mLastBatteryHealth && this.mBatteryProps.batteryPresent == this.mLastBatteryPresent) {
            if (this.mPlugType != this.mLastPlugType) {
            }
            if (this.mBatteryProps.batteryLevel != this.mLastBatteryLevel) {
                EventLog.writeEvent(EventLogTags.BATTERY_LEVEL, new Object[]{Integer.valueOf(this.mBatteryProps.batteryLevel), Integer.valueOf(this.mBatteryProps.batteryVoltage), Integer.valueOf(this.mBatteryProps.batteryTemperature)});
            }
            if (this.mBatteryLevelCritical && !this.mLastBatteryLevelCritical && this.mPlugType == 0) {
                dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                logOutlier = true;
            }
            if (!this.mBatteryLevelLow) {
                if (this.mPlugType == 0 && this.mBatteryProps.batteryLevel <= this.mLowBatteryWarningLevel) {
                    this.mBatteryLevelLow = true;
                }
            } else if (this.mPlugType != 0) {
                this.mBatteryLevelLow = IS_UDP;
            } else if (this.mBatteryProps.batteryLevel < this.mLowBatteryCloseWarningLevel) {
                this.mBatteryLevelLow = IS_UDP;
            } else if (force && this.mBatteryProps.batteryLevel >= this.mLowBatteryWarningLevel) {
                this.mBatteryLevelLow = IS_UDP;
            }
            sendIntentLocked();
            if (this.mPlugType == 0 && this.mLastPlugType == 0) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.ACTION_POWER_CONNECTED");
                        statusIntent.setFlags(67108864);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
                playRing();
            } else if (this.mPlugType == 0 && this.mLastPlugType != 0) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.ACTION_POWER_DISCONNECTED");
                        statusIntent.setFlags(67108864);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
                stopRing();
            }
            if (shouldSendBatteryLowLocked()) {
                this.mSentLowBatteryBroadcast = true;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.BATTERY_LOW");
                        statusIntent.setFlags(67108864);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            } else if (this.mSentLowBatteryBroadcast && this.mLastBatteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mSentLowBatteryBroadcast = IS_UDP;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.BATTERY_OKAY");
                        statusIntent.setFlags(67108864);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            }
            updateLight();
            if (logOutlier && dischargeDuration != 0) {
                logOutlierLocked(dischargeDuration);
            }
            this.mLastBatteryStatus = this.mBatteryProps.batteryStatus;
            this.mLastBatteryHealth = this.mBatteryProps.batteryHealth;
            this.mLastBatteryPresent = this.mBatteryProps.batteryPresent;
            this.mLastBatteryLevel = this.mBatteryProps.batteryLevel;
            this.mLastPlugType = this.mPlugType;
            this.mLastBatteryVoltage = this.mBatteryProps.batteryVoltage;
            this.mLastBatteryTemperature = this.mBatteryProps.batteryTemperature;
            this.mLastMaxChargingCurrent = this.mBatteryProps.maxChargingCurrent;
            this.mLastMaxChargingVoltage = this.mBatteryProps.maxChargingVoltage;
            this.mLastChargeCounter = this.mBatteryProps.batteryChargeCounter;
            this.mLastBatteryLevelCritical = this.mBatteryLevelCritical;
            this.mLastInvalidCharger = this.mInvalidCharger;
        }
        Object[] objArr = new Object[5];
        objArr[BATTERY_PLUGGED_NONE] = Integer.valueOf(this.mBatteryProps.batteryStatus);
        objArr[1] = Integer.valueOf(this.mBatteryProps.batteryHealth);
        objArr[2] = Integer.valueOf(this.mBatteryProps.batteryPresent ? 1 : BATTERY_PLUGGED_NONE);
        objArr[3] = Integer.valueOf(this.mPlugType);
        objArr[4] = this.mBatteryProps.batteryTechnology;
        EventLog.writeEvent(EventLogTags.BATTERY_STATUS, objArr);
        if (this.mBatteryProps.batteryLevel != this.mLastBatteryLevel) {
            EventLog.writeEvent(EventLogTags.BATTERY_LEVEL, new Object[]{Integer.valueOf(this.mBatteryProps.batteryLevel), Integer.valueOf(this.mBatteryProps.batteryVoltage), Integer.valueOf(this.mBatteryProps.batteryTemperature)});
        }
        dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
        logOutlier = true;
        if (!this.mBatteryLevelLow) {
            this.mBatteryLevelLow = true;
        } else if (this.mPlugType != 0) {
            this.mBatteryLevelLow = IS_UDP;
        } else if (this.mBatteryProps.batteryLevel < this.mLowBatteryCloseWarningLevel) {
            this.mBatteryLevelLow = IS_UDP;
        } else {
            this.mBatteryLevelLow = IS_UDP;
        }
        sendIntentLocked();
        if (this.mPlugType == 0) {
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                Intent statusIntent = new Intent("android.intent.action.ACTION_POWER_DISCONNECTED");
                statusIntent.setFlags(67108864);
                BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
            }
        });
        stopRing();
        if (shouldSendBatteryLowLocked()) {
            this.mSentLowBatteryBroadcast = true;
            this.mHandler.post(new Runnable() {
                public void run() {
                    Intent statusIntent = new Intent("android.intent.action.BATTERY_LOW");
                    statusIntent.setFlags(67108864);
                    BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                }
            });
        } else {
            this.mSentLowBatteryBroadcast = IS_UDP;
            this.mHandler.post(new Runnable() {
                public void run() {
                    Intent statusIntent = new Intent("android.intent.action.BATTERY_OKAY");
                    statusIntent.setFlags(67108864);
                    BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                }
            });
        }
        updateLight();
        logOutlierLocked(dischargeDuration);
        this.mLastBatteryStatus = this.mBatteryProps.batteryStatus;
        this.mLastBatteryHealth = this.mBatteryProps.batteryHealth;
        this.mLastBatteryPresent = this.mBatteryProps.batteryPresent;
        this.mLastBatteryLevel = this.mBatteryProps.batteryLevel;
        this.mLastPlugType = this.mPlugType;
        this.mLastBatteryVoltage = this.mBatteryProps.batteryVoltage;
        this.mLastBatteryTemperature = this.mBatteryProps.batteryTemperature;
        this.mLastMaxChargingCurrent = this.mBatteryProps.maxChargingCurrent;
        this.mLastMaxChargingVoltage = this.mBatteryProps.maxChargingVoltage;
        this.mLastChargeCounter = this.mBatteryProps.batteryChargeCounter;
        this.mLastBatteryLevelCritical = this.mBatteryLevelCritical;
        this.mLastInvalidCharger = this.mInvalidCharger;
    }

    private void sendIntentLocked() {
        Intent intent = new Intent("android.intent.action.BATTERY_CHANGED");
        intent.addFlags(1610612736);
        int icon = getIconLocked(this.mBatteryProps.batteryLevel);
        intent.putExtra("status", this.mBatteryProps.batteryStatus);
        intent.putExtra("health", this.mBatteryProps.batteryHealth);
        intent.putExtra("present", this.mBatteryProps.batteryPresent);
        intent.putExtra("level", this.mBatteryProps.batteryLevel);
        intent.putExtra("scale", BATTERY_SCALE);
        intent.putExtra("icon-small", icon);
        intent.putExtra("plugged", this.mPlugType);
        intent.putExtra("voltage", this.mBatteryProps.batteryVoltage);
        intent.putExtra("temperature", this.mBatteryProps.batteryTemperature);
        intent.putExtra("technology", this.mBatteryProps.batteryTechnology);
        intent.putExtra("invalid_charger", this.mInvalidCharger);
        intent.putExtra("max_charging_current", this.mBatteryProps.maxChargingCurrent);
        intent.putExtra("max_charging_voltage", this.mBatteryProps.maxChargingVoltage);
        intent.putExtra("charge_counter", this.mBatteryProps.batteryChargeCounter);
        this.mHandler.post(new AnonymousClass9(intent));
    }

    private void logBatteryStatsLocked() {
        RemoteException e;
        IOException e2;
        Throwable th;
        IBinder batteryInfoService = ServiceManager.getService("batterystats");
        if (batteryInfoService != null) {
            DropBoxManager db = (DropBoxManager) this.mContext.getSystemService("dropbox");
            if (db != null && db.isTagEnabled("BATTERY_DISCHARGE_INFO")) {
                File file = null;
                FileOutputStream fileOutputStream = null;
                try {
                    File dumpFile = new File("/data/system/batterystats.dump");
                    try {
                        FileOutputStream dumpStream = new FileOutputStream(dumpFile);
                        try {
                            batteryInfoService.dump(dumpStream.getFD(), DUMPSYS_ARGS);
                            FileUtils.sync(dumpStream);
                            db.addFile("BATTERY_DISCHARGE_INFO", dumpFile, 2);
                            if (dumpStream != null) {
                                try {
                                    dumpStream.close();
                                } catch (IOException e3) {
                                    Slog.e(TAG, "failed to close dumpsys output stream");
                                }
                            }
                            if (!(dumpFile == null || dumpFile.delete())) {
                                Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                            }
                            file = dumpFile;
                        } catch (RemoteException e4) {
                            e = e4;
                            fileOutputStream = dumpStream;
                            file = dumpFile;
                            Slog.e(TAG, "failed to dump battery service", e);
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e5) {
                                    Slog.e(TAG, "failed to close dumpsys output stream");
                                }
                            }
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                        } catch (IOException e6) {
                            e2 = e6;
                            fileOutputStream = dumpStream;
                            file = dumpFile;
                            try {
                                Slog.e(TAG, "failed to write dumpsys file", e2);
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e7) {
                                        Slog.e(TAG, "failed to close dumpsys output stream");
                                    }
                                }
                                Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                            } catch (Throwable th2) {
                                th = th2;
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e8) {
                                        Slog.e(TAG, "failed to close dumpsys output stream");
                                    }
                                }
                                if (!(file == null || file.delete())) {
                                    Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            fileOutputStream = dumpStream;
                            file = dumpFile;
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                            throw th;
                        }
                    } catch (RemoteException e9) {
                        e = e9;
                        file = dumpFile;
                        Slog.e(TAG, "failed to dump battery service", e);
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                    } catch (IOException e10) {
                        e2 = e10;
                        file = dumpFile;
                        Slog.e(TAG, "failed to write dumpsys file", e2);
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                    } catch (Throwable th4) {
                        th = th4;
                        file = dumpFile;
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                        throw th;
                    }
                } catch (RemoteException e11) {
                    e = e11;
                    Slog.e(TAG, "failed to dump battery service", e);
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (!(file == null || file.delete())) {
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                    }
                } catch (IOException e12) {
                    e2 = e12;
                    Slog.e(TAG, "failed to write dumpsys file", e2);
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (!(file == null || file.delete())) {
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void logOutlierLocked(long duration) {
        ContentResolver cr = this.mContext.getContentResolver();
        String dischargeThresholdString = Global.getString(cr, "battery_discharge_threshold");
        String durationThresholdString = Global.getString(cr, "battery_discharge_duration_threshold");
        if (dischargeThresholdString != null && durationThresholdString != null) {
            try {
                long durationThreshold = Long.parseLong(durationThresholdString);
                int dischargeThreshold = Integer.parseInt(dischargeThresholdString);
                if (duration <= durationThreshold && this.mDischargeStartLevel - this.mBatteryProps.batteryLevel >= dischargeThreshold) {
                    logBatteryStatsLocked();
                }
            } catch (NumberFormatException e) {
                Slog.e(TAG, "Invalid DischargeThresholds GService string: " + durationThresholdString + " or " + dischargeThresholdString);
            }
        }
    }

    private int getIconLocked(int level) {
        if (this.mBatteryProps.batteryStatus == 2) {
            return 17303234;
        }
        if (this.mBatteryProps.batteryStatus == 3) {
            return 17303220;
        }
        if (this.mBatteryProps.batteryStatus == 4 || this.mBatteryProps.batteryStatus == 5) {
            return (!isPoweredLocked(7) || this.mBatteryProps.batteryLevel < BATTERY_SCALE) ? 17303220 : 17303234;
        } else {
            return 17303248;
        }
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Battery service (battery) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set [ac|usb|wireless|status|level|invalid] <value>");
        pw.println("    Force a battery property value, freezing battery state.");
        pw.println("  unplug");
        pw.println("    Force battery unplugged, freezing battery state.");
        pw.println("  reset");
        pw.println("    Unfreeze battery state, returning to current hardware values.");
    }

    int onShellCommand(Shell shell, String cmd) {
        boolean z = true;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        long ident;
        if (cmd.equals("unplug")) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (!this.mUpdatesStopped) {
                this.mLastBatteryProps.set(this.mBatteryProps);
            }
            this.mBatteryProps.chargerAcOnline = IS_UDP;
            this.mBatteryProps.chargerUsbOnline = IS_UDP;
            this.mBatteryProps.chargerWirelessOnline = IS_UDP;
            ident = Binder.clearCallingIdentity();
            try {
                this.mUpdatesStopped = true;
                processValuesLocked(IS_UDP);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (cmd.equals("set")) {
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
                    this.mLastBatteryProps.set(this.mBatteryProps);
                }
                boolean update = true;
                BatteryProperties batteryProperties;
                if (key.equals("ac")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) == 0) {
                        z = IS_UDP;
                    }
                    batteryProperties.chargerAcOnline = z;
                } else if (key.equals("usb")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) == 0) {
                        z = IS_UDP;
                    }
                    batteryProperties.chargerUsbOnline = z;
                } else if (key.equals("wireless")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) == 0) {
                        z = IS_UDP;
                    }
                    batteryProperties.chargerWirelessOnline = z;
                } else if (key.equals("status")) {
                    this.mBatteryProps.batteryStatus = Integer.parseInt(value);
                } else if (key.equals("level")) {
                    this.mBatteryProps.batteryLevel = Integer.parseInt(value);
                } else if (key.equals("invalid")) {
                    this.mInvalidCharger = Integer.parseInt(value);
                } else {
                    pw.println("Unknown set option: " + key);
                    update = IS_UDP;
                }
                if (update) {
                    ident = Binder.clearCallingIdentity();
                    this.mUpdatesStopped = true;
                    processValuesLocked(IS_UDP);
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (NumberFormatException e) {
                pw.println("Bad value: " + value);
                return -1;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (!cmd.equals("reset")) {
            return shell.handleDefaultCommands(cmd);
        } else {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            ident = Binder.clearCallingIdentity();
            try {
                if (this.mUpdatesStopped) {
                    this.mUpdatesStopped = IS_UDP;
                    this.mBatteryProps.set(this.mLastBatteryProps);
                    processValuesLocked(IS_UDP);
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return BATTERY_PLUGGED_NONE;
    }

    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            if (args != null) {
                if (args.length != 0) {
                    Flog.i(NativeResponseCode.SERVICE_REGISTRATION_FAILED, "dumpInternal args[0]: " + args[BATTERY_PLUGGED_NONE] + " mUpdatesStopped: " + this.mUpdatesStopped);
                }
            }
            if (!(args == null || args.length == 0)) {
                if (!"-a".equals(args[BATTERY_PLUGGED_NONE])) {
                    new Shell().exec(this.mBinderService, null, fd, null, args, new ResultReceiver(null));
                }
            }
            pw.println("Current Battery Service state:");
            if (this.mUpdatesStopped) {
                pw.println("  (UPDATES STOPPED -- use 'reset' to restart)");
            }
            pw.println("  AC powered: " + this.mBatteryProps.chargerAcOnline);
            pw.println("  USB powered: " + this.mBatteryProps.chargerUsbOnline);
            pw.println("  Wireless powered: " + this.mBatteryProps.chargerWirelessOnline);
            pw.println("  Max charging current: " + this.mBatteryProps.maxChargingCurrent);
            pw.println("  Max charging voltage: " + this.mBatteryProps.maxChargingVoltage);
            pw.println("  Charge counter: " + this.mBatteryProps.batteryChargeCounter);
            pw.println("  status: " + this.mBatteryProps.batteryStatus);
            pw.println("  health: " + this.mBatteryProps.batteryHealth);
            pw.println("  present: " + this.mBatteryProps.batteryPresent);
            pw.println("  level: " + this.mBatteryProps.batteryLevel);
            pw.println("  scale: 100");
            pw.println("  voltage: " + this.mBatteryProps.batteryVoltage);
            pw.println("  temperature: " + this.mBatteryProps.batteryTemperature);
            pw.println("  technology: " + this.mBatteryProps.batteryTechnology);
        }
    }

    protected BatteryProperties getBatteryProps() {
        return this.mBatteryProps;
    }

    protected int getLowBatteryWarningLevel() {
        return this.mLowBatteryWarningLevel;
    }

    protected void updateLight() {
        this.mLed.updateLightsLocked();
    }
}
