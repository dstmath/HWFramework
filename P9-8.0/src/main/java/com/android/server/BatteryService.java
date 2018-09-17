package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
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
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.EventLog;
import android.util.Flog;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.DumpUtils;
import com.android.server.am.BatteryStatsService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.power.PowerManagerService;
import com.android.server.storage.DeviceStorageMonitorService;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class BatteryService extends AbsBatteryService {
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int BATTERY_SCALE = 100;
    private static final boolean DEBUG = false;
    private static final String[] DUMPSYS_ARGS = new String[]{"--checkin", "--unplugged"};
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    private static final boolean IS_EMU = boardname.contains("emulator");
    private static final boolean IS_FPGA = boardname.contains("fpga");
    private static final boolean IS_UDP = boardname.contains("udp");
    static final int OPTION_FORCE_UPDATE = 1;
    private static final int SHUTDOWN_DELAY_TIMEOUT = 20000;
    private static final String TAG = BatteryService.class.getSimpleName();
    private static String boardname = SystemProperties.get("ro.board.boardname", "0");
    private ActivityManagerInternal mActivityManagerInternal;
    private boolean mBatteryLevelCritical;
    private boolean mBatteryLevelLow;
    private BatteryProperties mBatteryProps;
    private final IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private final Context mContext;
    private int mCriticalBatteryLevel;
    private HwCustBatteryService mCust = ((HwCustBatteryService) HwCustUtils.createObj(HwCustBatteryService.class, new Object[0]));
    private int mDischargeStartLevel;
    private long mDischargeStartTime;
    private final Handler mHandler;
    private int mInvalidCharger;
    private int mLastBatteryHealth;
    private int mLastBatteryLevel;
    private boolean mLastBatteryLevelCritical;
    private boolean mLastBatteryPresent;
    private final BatteryProperties mLastBatteryProps = new BatteryProperties();
    private int mLastBatteryStatus;
    private int mLastBatteryTemperature;
    private int mLastBatteryVoltage;
    private int mLastChargeCounter;
    private int mLastInvalidCharger;
    private int mLastMaxChargingCurrent;
    private int mLastMaxChargingVoltage;
    private int mLastPlugType = -1;
    private Led mLed;
    private final Object mLock = new Object();
    private int mLowBatteryCloseWarningLevel;
    private int mLowBatteryWarningLevel;
    private CheckForShutdown mPendingCheckForShutdown;
    private int mPlugType;
    private boolean mSentLowBatteryBroadcast = false;
    private int mSequence = 1;
    private int mShutdownBatteryTemperature;
    private boolean mUpdatesStopped;

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
        /* synthetic */ BinderService(BatteryService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(BatteryService.this.mContext, BatteryService.TAG, pw)) {
                if (args.length <= 0 || !"--proto".equals(args[0])) {
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
        /* synthetic */ CheckForShutdown(BatteryService this$0, CheckForShutdown -this1) {
            this();
        }

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
            this.mBatteryLowARGB = context.getResources().getInteger(17694829);
            this.mBatteryMediumARGB = context.getResources().getInteger(17694830);
            this.mBatteryFullARGB = context.getResources().getInteger(17694826);
            this.mBatteryLedOn = context.getResources().getInteger(17694828);
            this.mBatteryLedOff = context.getResources().getInteger(17694827);
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
        /* synthetic */ LocalService(BatteryService this$0, LocalService -this1) {
            this();
        }

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
            int -get8;
            synchronized (BatteryService.this.mLock) {
                -get8 = BatteryService.this.mPlugType;
            }
            return -get8;
        }

        public int getBatteryLevel() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mBatteryProps.batteryLevel;
            }
            return i;
        }

        public boolean getBatteryLevelLow() {
            boolean -get2;
            synchronized (BatteryService.this.mLock) {
                -get2 = BatteryService.this.mBatteryLevelLow;
            }
            return -get2;
        }

        public int getInvalidCharger() {
            int -get5;
            synchronized (BatteryService.this.mLock) {
                -get5 = BatteryService.this.mInvalidCharger;
            }
            return -get5;
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

    public BatteryService(Context context) {
        super(context);
        this.mContext = context;
        this.mHandler = new Handler(true);
        this.mLed = new Led(context, (LightsManager) -wrap6(LightsManager.class));
        this.mBatteryStats = BatteryStatsService.getService();
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mCriticalBatteryLevel = this.mContext.getResources().getInteger(17694758);
        this.mLowBatteryWarningLevel = this.mContext.getResources().getInteger(17694804);
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694803);
        this.mShutdownBatteryTemperature = this.mContext.getResources().getInteger(17694853);
        if (new File("/sys/devices/virtual/switch/invalid_charger/state").exists()) {
            new UEventObserver() {
                public void onUEvent(UEvent event) {
                    int invalidCharger = "1".equals(event.get("SWITCH_STATE")) ? 1 : 0;
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
        this.mBinderService = new BinderService(this, null);
        publishBinderService("battery", this.mBinderService);
        publishLocalService(BatteryManagerInternal.class, new LocalService(this, null));
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            synchronized (this.mLock) {
                this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("low_power_trigger_level"), false, new ContentObserver(this.mHandler) {
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

    private void updateBatteryWarningLevelLocked() {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defWarnLevel = this.mContext.getResources().getInteger(17694804);
        this.mLowBatteryWarningLevel = Global.getInt(resolver, "low_power_trigger_level", defWarnLevel);
        if (this.mLowBatteryWarningLevel == 0) {
            this.mLowBatteryWarningLevel = defWarnLevel;
        }
        if (this.mLowBatteryWarningLevel < this.mCriticalBatteryLevel) {
            this.mLowBatteryWarningLevel = this.mCriticalBatteryLevel;
        }
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694803);
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
            return (plugTypeSet & 4) != 0 && this.mBatteryProps.chargerWirelessOnline;
        } else {
            return true;
        }
    }

    private boolean shouldSendBatteryLowLocked() {
        boolean plugged = this.mPlugType != 0;
        boolean oldPlugged = this.mLastPlugType != 0;
        if (plugged || this.mBatteryProps.batteryStatus == 1 || this.mBatteryProps.batteryLevel > this.mLowBatteryWarningLevel) {
            return false;
        }
        if (oldPlugged || this.mLastBatteryLevel > this.mLowBatteryWarningLevel) {
            return true;
        }
        return false;
    }

    private void shutdownIfNoPowerLocked() {
        if (this.mBatteryProps.batteryLevel == 0 && (IS_EMU ^ 1) != 0 && (IS_FPGA ^ 1) != 0 && (IS_UDP ^ 1) != 0) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (BatteryService.this.mActivityManagerInternal.isSystemReady()) {
                        Slog.e(BatteryService.TAG, "Shutdown because of: batteryLevel =" + BatteryService.this.mBatteryProps.batteryLevel);
                        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent.setFlags(268435456);
                        BatteryService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    }
                }
            });
            if (this.mPendingCheckForShutdown == null) {
                this.mPendingCheckForShutdown = new CheckForShutdown(this, null);
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
                    if (BatteryService.this.mActivityManagerInternal.isSystemReady()) {
                        Slog.e(BatteryService.TAG, "Shutdown because of: batteryTemperature =" + BatteryService.this.mBatteryProps.batteryTemperature);
                        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
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
                if (this.mCust != null && this.mCust.isBadBatteryWarning()) {
                    this.mCust.sendBadBatteryWarningNotification(this.mContext, this.mBatteryProps, props);
                }
                this.mBatteryProps = props;
                processValuesLocked(false);
            }
        }
    }

    private void processValuesLocked(boolean force) {
        boolean logOutlier = false;
        long dischargeDuration = 0;
        this.mBatteryLevelCritical = this.mBatteryProps.batteryLevel <= this.mCriticalBatteryLevel;
        if (this.mBatteryProps.chargerAcOnline) {
            this.mPlugType = 1;
        } else if (this.mBatteryProps.chargerUsbOnline) {
            this.mPlugType = 2;
        } else if (this.mBatteryProps.chargerWirelessOnline) {
            this.mPlugType = 4;
        } else {
            this.mPlugType = 0;
        }
        try {
            this.mBatteryStats.setBatteryState(this.mBatteryProps.batteryStatus, this.mBatteryProps.batteryHealth, this.mPlugType, this.mBatteryProps.batteryLevel, this.mBatteryProps.batteryTemperature, this.mBatteryProps.batteryVoltage, this.mBatteryProps.batteryChargeCounter, this.mBatteryProps.batteryFullCharge);
        } catch (RemoteException e) {
        }
        shutdownIfNoPowerLocked();
        shutdownIfOverTempLocked();
        if (force || this.mBatteryProps.batteryStatus != this.mLastBatteryStatus || this.mBatteryProps.batteryHealth != this.mLastBatteryHealth || this.mBatteryProps.batteryPresent != this.mLastBatteryPresent || this.mBatteryProps.batteryLevel != this.mLastBatteryLevel || this.mPlugType != this.mLastPlugType || this.mBatteryProps.batteryVoltage != this.mLastBatteryVoltage || this.mBatteryProps.batteryTemperature != this.mLastBatteryTemperature || this.mBatteryProps.maxChargingCurrent != this.mLastMaxChargingCurrent || this.mBatteryProps.maxChargingVoltage != this.mLastMaxChargingVoltage || this.mBatteryProps.batteryChargeCounter != this.mLastChargeCounter || this.mInvalidCharger != this.mLastInvalidCharger) {
            final Intent statusIntent;
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
            if (!(this.mBatteryProps.batteryStatus == this.mLastBatteryStatus && this.mBatteryProps.batteryHealth == this.mLastBatteryHealth && this.mBatteryProps.batteryPresent == this.mLastBatteryPresent && this.mPlugType == this.mLastPlugType)) {
                Object[] objArr = new Object[5];
                objArr[0] = Integer.valueOf(this.mBatteryProps.batteryStatus);
                objArr[1] = Integer.valueOf(this.mBatteryProps.batteryHealth);
                objArr[2] = Integer.valueOf(this.mBatteryProps.batteryPresent ? 1 : 0);
                objArr[3] = Integer.valueOf(this.mPlugType);
                objArr[4] = this.mBatteryProps.batteryTechnology;
                EventLog.writeEvent(EventLogTags.BATTERY_STATUS, objArr);
            }
            if (this.mBatteryProps.batteryLevel != this.mLastBatteryLevel) {
                EventLog.writeEvent(EventLogTags.BATTERY_LEVEL, new Object[]{Integer.valueOf(this.mBatteryProps.batteryLevel), Integer.valueOf(this.mBatteryProps.batteryVoltage), Integer.valueOf(this.mBatteryProps.batteryTemperature)});
            }
            if (this.mBatteryLevelCritical && (this.mLastBatteryLevelCritical ^ 1) != 0 && this.mPlugType == 0) {
                dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                logOutlier = true;
            }
            if (this.mBatteryLevelLow) {
                if (this.mPlugType != 0) {
                    this.mBatteryLevelLow = false;
                } else if (this.mBatteryProps.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                    this.mBatteryLevelLow = false;
                } else if (force && this.mBatteryProps.batteryLevel >= this.mLowBatteryWarningLevel) {
                    this.mBatteryLevelLow = false;
                }
            } else if (this.mPlugType == 0 && this.mBatteryProps.batteryLevel <= this.mLowBatteryWarningLevel) {
                this.mBatteryLevelLow = true;
            }
            this.mSequence++;
            if (this.mPlugType != 0 && this.mLastPlugType == 0) {
                statusIntent = new Intent("android.intent.action.ACTION_POWER_CONNECTED");
                statusIntent.setFlags(67108864);
                statusIntent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
                playRing();
            } else if (this.mPlugType == 0 && this.mLastPlugType != 0) {
                statusIntent = new Intent("android.intent.action.ACTION_POWER_DISCONNECTED");
                statusIntent.setFlags(67108864);
                statusIntent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
                stopRing();
            }
            if (shouldSendBatteryLowLocked()) {
                this.mSentLowBatteryBroadcast = true;
                statusIntent = new Intent("android.intent.action.BATTERY_LOW");
                statusIntent.setFlags(67108864);
                statusIntent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            } else if (this.mSentLowBatteryBroadcast && this.mBatteryProps.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mSentLowBatteryBroadcast = false;
                statusIntent = new Intent("android.intent.action.BATTERY_OKAY");
                statusIntent.setFlags(67108864);
                statusIntent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            }
            sendIntentLocked();
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
    }

    private void sendIntentLocked() {
        final Intent intent = new Intent("android.intent.action.BATTERY_CHANGED");
        intent.addFlags(1610612736);
        int icon = getIconLocked(this.mBatteryProps.batteryLevel);
        intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
        intent.putExtra("status", this.mBatteryProps.batteryStatus);
        intent.putExtra("health", this.mBatteryProps.batteryHealth);
        intent.putExtra("present", this.mBatteryProps.batteryPresent);
        intent.putExtra("level", this.mBatteryProps.batteryLevel);
        intent.putExtra("scale", 100);
        intent.putExtra("icon-small", icon);
        intent.putExtra("plugged", this.mPlugType);
        intent.putExtra("voltage", this.mBatteryProps.batteryVoltage);
        intent.putExtra("temperature", this.mBatteryProps.batteryTemperature);
        intent.putExtra("technology", this.mBatteryProps.batteryTechnology);
        intent.putExtra("invalid_charger", this.mInvalidCharger);
        intent.putExtra("max_charging_current", this.mBatteryProps.maxChargingCurrent);
        intent.putExtra("max_charging_voltage", this.mBatteryProps.maxChargingVoltage);
        intent.putExtra("charge_counter", this.mBatteryProps.batteryChargeCounter);
        this.mHandler.post(new Runnable() {
            public void run() {
                ActivityManager.broadcastStickyIntent(intent, -1);
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c9 A:{SYNTHETIC, Splitter: B:42:0x00c9} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0089 A:{SYNTHETIC, Splitter: B:30:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c9 A:{SYNTHETIC, Splitter: B:42:0x00c9} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0089 A:{SYNTHETIC, Splitter: B:30:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0101 A:{SYNTHETIC, Splitter: B:52:0x0101} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0101 A:{SYNTHETIC, Splitter: B:52:0x0101} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void logBatteryStatsLocked() {
        RemoteException e;
        IOException e2;
        Throwable th;
        IBinder batteryInfoService = ServiceManager.getService("batterystats");
        if (batteryInfoService != null) {
            DropBoxManager db = (DropBoxManager) this.mContext.getSystemService("dropbox");
            if (db != null && (db.isTagEnabled("BATTERY_DISCHARGE_INFO") ^ 1) == 0) {
                File dumpFile = null;
                FileOutputStream dumpStream = null;
                try {
                    FileOutputStream dumpStream2;
                    File dumpFile2 = new File("/data/system/batterystats.dump");
                    try {
                        dumpStream2 = new FileOutputStream(dumpFile2);
                    } catch (RemoteException e3) {
                        e = e3;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to dump battery service", e);
                        if (dumpStream != null) {
                            try {
                                dumpStream.close();
                            } catch (IOException e4) {
                                Slog.e(TAG, "failed to close dumpsys output stream");
                            }
                        }
                        if (!(dumpFile == null || (dumpFile.delete() ^ 1) == 0)) {
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        }
                    } catch (IOException e5) {
                        e2 = e5;
                        dumpFile = dumpFile2;
                        try {
                            Slog.e(TAG, "failed to write dumpsys file", e2);
                            if (dumpStream != null) {
                                try {
                                    dumpStream.close();
                                } catch (IOException e6) {
                                    Slog.e(TAG, "failed to close dumpsys output stream");
                                }
                            }
                            if (!(dumpFile == null || (dumpFile.delete() ^ 1) == 0)) {
                                Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (dumpStream != null) {
                            }
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        dumpFile = dumpFile2;
                        if (dumpStream != null) {
                            try {
                                dumpStream.close();
                            } catch (IOException e7) {
                                Slog.e(TAG, "failed to close dumpsys output stream");
                            }
                        }
                        if (!(dumpFile == null || (dumpFile.delete() ^ 1) == 0)) {
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        }
                        throw th;
                    }
                    try {
                        batteryInfoService.dump(dumpStream2.getFD(), DUMPSYS_ARGS);
                        FileUtils.sync(dumpStream2);
                        db.addFile("BATTERY_DISCHARGE_INFO", dumpFile2, 2);
                        if (dumpStream2 != null) {
                            try {
                                dumpStream2.close();
                            } catch (IOException e8) {
                                Slog.e(TAG, "failed to close dumpsys output stream");
                            }
                        }
                        if (!(dumpFile2 == null || (dumpFile2.delete() ^ 1) == 0)) {
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile2.getAbsolutePath());
                        }
                        dumpFile = dumpFile2;
                    } catch (RemoteException e9) {
                        e = e9;
                        dumpStream = dumpStream2;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to dump battery service", e);
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    } catch (IOException e10) {
                        e2 = e10;
                        dumpStream = dumpStream2;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to write dumpsys file", e2);
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    } catch (Throwable th4) {
                        th = th4;
                        dumpStream = dumpStream2;
                        dumpFile = dumpFile2;
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        throw th;
                    }
                } catch (RemoteException e11) {
                    e = e11;
                    Slog.e(TAG, "failed to dump battery service", e);
                    if (dumpStream != null) {
                    }
                    Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                } catch (IOException e12) {
                    e2 = e12;
                    Slog.e(TAG, "failed to write dumpsys file", e2);
                    if (dumpStream != null) {
                    }
                    Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
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
            return 17303345;
        }
        if (this.mBatteryProps.batteryStatus == 3) {
            return 17303331;
        }
        if (this.mBatteryProps.batteryStatus == 4 || this.mBatteryProps.batteryStatus == 5) {
            return (!isPoweredLocked(7) || this.mBatteryProps.batteryLevel < 100) ? 17303331 : 17303345;
        } else {
            return 17303359;
        }
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

    int parseOptions(Shell shell) {
        int opts = 0;
        while (true) {
            String opt = shell.getNextOption();
            if (opt == null) {
                return opts;
            }
            if ("-f".equals(opt)) {
                opts |= 1;
            }
        }
    }

    int onShellCommand(Shell shell, String cmd) {
        long ident;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        int opts;
        if (cmd.equals("unplug")) {
            opts = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (!this.mUpdatesStopped) {
                this.mLastBatteryProps.set(this.mBatteryProps);
            }
            this.mBatteryProps.chargerAcOnline = false;
            this.mBatteryProps.chargerUsbOnline = false;
            this.mBatteryProps.chargerWirelessOnline = false;
            ident = Binder.clearCallingIdentity();
            try {
                this.mUpdatesStopped = true;
                processValuesFromShellLocked(pw, opts);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (cmd.equals("set")) {
            opts = parseOptions(shell);
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
                boolean z;
                if (key.equals("present")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.batteryPresent = z;
                } else if (key.equals("ac")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.chargerAcOnline = z;
                } else if (key.equals("usb")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.chargerUsbOnline = z;
                } else if (key.equals("wireless")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.chargerWirelessOnline = z;
                } else if (key.equals("status")) {
                    this.mBatteryProps.batteryStatus = Integer.parseInt(value);
                } else if (key.equals("level")) {
                    this.mBatteryProps.batteryLevel = Integer.parseInt(value);
                } else if (key.equals("temp")) {
                    this.mBatteryProps.batteryTemperature = Integer.parseInt(value);
                } else if (key.equals("invalid")) {
                    this.mInvalidCharger = Integer.parseInt(value);
                } else {
                    pw.println("Unknown set option: " + key);
                    update = false;
                }
                if (update) {
                    ident = Binder.clearCallingIdentity();
                    this.mUpdatesStopped = true;
                    processValuesFromShellLocked(pw, opts);
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
            opts = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            ident = Binder.clearCallingIdentity();
            try {
                if (this.mUpdatesStopped) {
                    this.mUpdatesStopped = false;
                    this.mBatteryProps.set(this.mLastBatteryProps);
                    processValuesFromShellLocked(pw, opts);
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return 0;
    }

    private void processValuesFromShellLocked(PrintWriter pw, int opts) {
        boolean z = false;
        if ((opts & 1) != 0) {
            z = true;
        }
        processValuesLocked(z);
        if ((opts & 1) != 0) {
            pw.println(this.mSequence);
        }
    }

    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            if (args != null) {
                if (args.length != 0) {
                    Flog.i(NativeResponseCode.SERVICE_REGISTRATION_FAILED, "dumpInternal args[0]: " + args[0] + " mUpdatesStopped: " + this.mUpdatesStopped);
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

    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mLock) {
            proto.write(1155346202625L, this.mUpdatesStopped);
            int batteryPluggedValue = 0;
            if (this.mBatteryProps.chargerAcOnline) {
                batteryPluggedValue = 1;
            } else if (this.mBatteryProps.chargerUsbOnline) {
                batteryPluggedValue = 2;
            } else if (this.mBatteryProps.chargerWirelessOnline) {
                batteryPluggedValue = 4;
            }
            proto.write(1168231104514L, batteryPluggedValue);
            proto.write(1112396529667L, this.mBatteryProps.maxChargingCurrent);
            proto.write(1112396529668L, this.mBatteryProps.maxChargingVoltage);
            proto.write(1112396529669L, this.mBatteryProps.batteryChargeCounter);
            proto.write(1168231104518L, this.mBatteryProps.batteryStatus);
            proto.write(1168231104519L, this.mBatteryProps.batteryHealth);
            proto.write(1155346202632L, this.mBatteryProps.batteryPresent);
            proto.write(1112396529673L, this.mBatteryProps.batteryLevel);
            proto.write(1112396529674L, 100);
            proto.write(1112396529675L, this.mBatteryProps.batteryVoltage);
            proto.write(1112396529676L, this.mBatteryProps.batteryTemperature);
            proto.write(1159641169933L, this.mBatteryProps.batteryTechnology);
        }
        proto.flush();
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
