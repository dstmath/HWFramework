package com.android.server.am;

import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.BatteryStats.Uid;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SynchronousResultReceiver;
import android.os.SynchronousResultReceiver.Result;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.os.health.HealthStatsParceler;
import android.os.health.HealthStatsWriter;
import android.os.health.UidHealthStats;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.HwLog;
import android.util.IntArray;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.ExternalStatsSync;
import com.android.internal.os.BatteryStatsImpl.PlatformIdleStateCallback;
import com.android.internal.os.PowerProfile;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.huawei.pgmng.log.LogPower;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class BatteryStatsService extends Stub implements LowPowerModeListener, PlatformIdleStateCallback {
    private static final long EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS = 2000;
    private static final int MAX_LOW_POWER_STATS_SIZE = 512;
    private static final long MAX_WIFI_STATS_SAMPLE_ERROR_MILLIS = 750;
    static final String TAG = "BatteryStatsService";
    private static IBatteryStats sService;
    private Context mContext;
    private CharsetDecoder mDecoderStat;
    private final Object mExternalStatsLock;
    private final BatteryStatsHandler mHandler;
    @GuardedBy("mExternalStatsLock")
    private WifiActivityEnergyInfo mLastInfo;
    private IHwPowerInfoService mPowerInfoService;
    final BatteryStatsImpl mStats;
    private TelephonyManager mTelephony;
    private CharBuffer mUtf16BufferStat;
    private ByteBuffer mUtf8BufferStat;
    private IWifiManager mWifiManager;
    private boolean misBetaUser;

    /* renamed from: com.android.server.am.BatteryStatsService.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ int val$chargeUAh;
        final /* synthetic */ int val$health;
        final /* synthetic */ int val$level;
        final /* synthetic */ int val$plugType;
        final /* synthetic */ int val$status;
        final /* synthetic */ int val$temp;
        final /* synthetic */ int val$volt;

        AnonymousClass1(int val$plugType, int val$status, int val$health, int val$level, int val$temp, int val$volt, int val$chargeUAh) {
            this.val$plugType = val$plugType;
            this.val$status = val$status;
            this.val$health = val$health;
            this.val$level = val$level;
            this.val$temp = val$temp;
            this.val$volt = val$volt;
            this.val$chargeUAh = val$chargeUAh;
        }

        public void run() {
            synchronized (BatteryStatsService.this.mStats) {
                if (BatteryStatsService.this.mStats.isOnBattery() == (this.val$plugType == 0)) {
                    BatteryStatsService.this.mStats.setBatteryStateLocked(this.val$status, this.val$health, this.val$plugType, this.val$level, this.val$temp, this.val$volt, this.val$chargeUAh);
                    if (BatteryStatsService.this.misBetaUser) {
                        synchronized (BatteryStatsService.this.mPowerInfoService) {
                            BatteryStatsService.this.mPowerInfoService.notePowerInfoBatteryState(this.val$plugType, this.val$level);
                        }
                    }
                    return;
                }
                BatteryStatsService.this.updateExternalStatsSync("battery-state", 15);
                synchronized (BatteryStatsService.this.mStats) {
                    BatteryStatsService.this.mStats.setBatteryStateLocked(this.val$status, this.val$health, this.val$plugType, this.val$level, this.val$temp, this.val$volt, this.val$chargeUAh);
                    if (BatteryStatsService.this.misBetaUser) {
                        synchronized (BatteryStatsService.this.mPowerInfoService) {
                            BatteryStatsService.this.mPowerInfoService.notePowerInfoBatteryState(this.val$plugType, this.val$level);
                        }
                    }
                }
            }
        }
    }

    class BatteryStatsHandler extends Handler implements ExternalStatsSync {
        public static final int MSG_SYNC_EXTERNAL_STATS = 1;
        public static final int MSG_WRITE_TO_DISK = 2;
        private IntArray mUidsToRemove;
        private int mUpdateFlags;

        public BatteryStatsHandler(Looper looper) {
            super(looper);
            this.mUpdateFlags = 0;
            this.mUidsToRemove = new IntArray();
        }

        public void handleMessage(Message msg) {
            BatteryStatsImpl batteryStatsImpl;
            switch (msg.what) {
                case MSG_SYNC_EXTERNAL_STATS /*1*/:
                    int updateFlags;
                    synchronized (this) {
                        removeMessages(MSG_SYNC_EXTERNAL_STATS);
                        updateFlags = this.mUpdateFlags;
                        this.mUpdateFlags = 0;
                        break;
                    }
                    BatteryStatsService.this.updateExternalStatsSync((String) msg.obj, updateFlags);
                    batteryStatsImpl = BatteryStatsService.this.mStats;
                    synchronized (batteryStatsImpl) {
                        break;
                    }
                    synchronized (this) {
                        int numUidsToRemove = this.mUidsToRemove.size();
                        for (int i = 0; i < numUidsToRemove; i += MSG_SYNC_EXTERNAL_STATS) {
                            BatteryStatsService.this.mStats.removeIsolatedUidLocked(this.mUidsToRemove.get(i));
                        }
                        break;
                    }
                    this.mUidsToRemove.clear();
                    break;
                case MSG_WRITE_TO_DISK /*2*/:
                    BatteryStatsService.this.updateExternalStatsSync("write", 15);
                    batteryStatsImpl = BatteryStatsService.this.mStats;
                    synchronized (batteryStatsImpl) {
                        break;
                    }
                    BatteryStatsService.this.mStats.writeAsyncLocked();
                    break;
                default:
                    return;
            }
        }

        public void scheduleSync(String reason, int updateFlags) {
            synchronized (this) {
                scheduleSyncLocked(reason, updateFlags);
            }
        }

        public void scheduleCpuSyncDueToRemovedUid(int uid) {
            synchronized (this) {
                scheduleSyncLocked("remove-uid", MSG_SYNC_EXTERNAL_STATS);
                this.mUidsToRemove.add(uid);
            }
        }

        private void scheduleSyncLocked(String reason, int updateFlags) {
            if (this.mUpdateFlags == 0) {
                sendMessage(Message.obtain(this, MSG_SYNC_EXTERNAL_STATS, reason));
            }
            this.mUpdateFlags |= updateFlags;
        }
    }

    final class WakeupReasonThread extends Thread {
        private static final int MAX_REASON_SIZE = 512;
        private CharsetDecoder mDecoder;
        private CharBuffer mUtf16Buffer;
        private ByteBuffer mUtf8Buffer;

        WakeupReasonThread() {
            super("BatteryStats_wakeupReason");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Process.setThreadPriority(-2);
            this.mDecoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?");
            this.mUtf8Buffer = ByteBuffer.allocateDirect(MAX_REASON_SIZE);
            this.mUtf16Buffer = CharBuffer.allocate(MAX_REASON_SIZE);
            while (true) {
                String reason = waitWakeup();
                if (reason != null) {
                    synchronized (BatteryStatsService.this.mStats) {
                        BatteryStatsService.this.mStats.noteWakeupReasonLocked(reason);
                    }
                    if (BatteryStatsService.this.misBetaUser) {
                        synchronized (BatteryStatsService.this.mPowerInfoService) {
                            BatteryStatsService.this.mPowerInfoService.notePowerInfoWakeupReason(reason);
                            try {
                            } catch (RuntimeException e) {
                                Slog.e(BatteryStatsService.TAG, "Failure reading wakeup reasons", e);
                                return;
                            }
                        }
                    }
                } else {
                    return;
                }
            }
        }

        private String waitWakeup() {
            this.mUtf8Buffer.clear();
            this.mUtf16Buffer.clear();
            this.mDecoder.reset();
            int bytesWritten = BatteryStatsService.nativeWaitWakeup(this.mUtf8Buffer);
            if (bytesWritten < 0) {
                return null;
            }
            if (bytesWritten == 0) {
                return "unknown";
            }
            this.mUtf8Buffer.limit(bytesWritten);
            this.mDecoder.decode(this.mUtf8Buffer, this.mUtf16Buffer, true);
            this.mUtf16Buffer.flip();
            return this.mUtf16Buffer.toString();
        }
    }

    private native int getPlatformLowPowerStats(ByteBuffer byteBuffer);

    private static native int nativeWaitWakeup(ByteBuffer byteBuffer);

    public String getPlatformLowPowerStats() {
        this.mUtf8BufferStat.clear();
        this.mUtf16BufferStat.clear();
        this.mDecoderStat.reset();
        int bytesWritten = getPlatformLowPowerStats(this.mUtf8BufferStat);
        if (bytesWritten < 0) {
            return null;
        }
        if (bytesWritten == 0) {
            return "Empty";
        }
        this.mUtf8BufferStat.limit(bytesWritten);
        this.mDecoderStat.decode(this.mUtf8BufferStat, this.mUtf16BufferStat, true);
        this.mUtf16BufferStat.flip();
        return this.mUtf16BufferStat.toString();
    }

    BatteryStatsService(File systemDir, Handler handler) {
        this.misBetaUser = false;
        this.mExternalStatsLock = new Object();
        this.mLastInfo = new WifiActivityEnergyInfo(0, 0, 0, new long[]{0}, 0, 0, 0);
        this.mDecoderStat = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?");
        this.mUtf8BufferStat = ByteBuffer.allocateDirect(MAX_LOW_POWER_STATS_SIZE);
        this.mUtf16BufferStat = CharBuffer.allocate(MAX_LOW_POWER_STATS_SIZE);
        ServiceThread thread = new ServiceThread("batterystats-sync", 0, true);
        thread.start();
        this.mHandler = new BatteryStatsHandler(thread.getLooper());
        this.mStats = new BatteryStatsImpl(systemDir, handler, this.mHandler, this);
        if (SystemProperties.getBoolean("ro.control.sleeplog", false) && 3 == SystemProperties.getInt("ro.logsystem.usertype", 0)) {
            this.misBetaUser = true;
        }
        if (this.misBetaUser) {
            this.mPowerInfoService = HwServiceFactory.getHwPowerInfoService(null, false);
        }
    }

    public void publish(Context context) {
        this.mContext = context;
        this.mStats.setRadioScanningTimeout(((long) this.mContext.getResources().getInteger(17694734)) * 1000);
        this.mStats.setPowerProfile(new PowerProfile(context));
        ServiceManager.addService("batterystats", asBinder());
    }

    public void initPowerManagement() {
        PowerManagerInternal powerMgr = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        powerMgr.registerLowPowerModeObserver(this);
        this.mStats.notePowerSaveMode(powerMgr.getLowPowerModeEnabled());
        new WakeupReasonThread().start();
    }

    public void shutdown() {
        Slog.w("BatteryStats", "Writing battery stats before shutdown...");
        updateExternalStatsSync("shutdown", 15);
        synchronized (this.mStats) {
            this.mStats.shutdownLocked();
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.noteShutdown();
            }
        }
        this.mHandler.getLooper().quit();
    }

    public static IBatteryStats getService() {
        if (sService != null) {
            return sService;
        }
        sService = asInterface(ServiceManager.getService("batterystats"));
        return sService;
    }

    public void onLowPowerModeChanged(boolean enabled) {
        synchronized (this.mStats) {
            this.mStats.notePowerSaveMode(enabled);
        }
    }

    public BatteryStatsImpl getActiveStatistics() {
        return this.mStats;
    }

    public void scheduleWriteToDisk() {
        this.mHandler.sendEmptyMessage(2);
    }

    void removeUid(int uid) {
        synchronized (this.mStats) {
            this.mStats.removeUidStatsLocked(uid);
        }
    }

    void addIsolatedUid(int isolatedUid, int appUid) {
        synchronized (this.mStats) {
            this.mStats.addIsolatedUidLocked(isolatedUid, appUid);
        }
    }

    void removeIsolatedUid(int isolatedUid, int appUid) {
        synchronized (this.mStats) {
            this.mStats.scheduleRemoveIsolatedUidLocked(isolatedUid, appUid);
        }
    }

    void noteProcessStart(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessStartLocked(name, uid);
        }
    }

    void noteProcessCrash(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessCrashLocked(name, uid);
        }
    }

    void noteProcessAnr(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessAnrLocked(name, uid);
        }
    }

    void noteProcessFinish(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessFinishLocked(name, uid);
        }
    }

    void noteUidProcessState(int uid, int state) {
        synchronized (this.mStats) {
            this.mStats.noteUidProcessStateLocked(uid, state);
        }
    }

    public byte[] getStatistics() {
        this.mContext.enforceCallingPermission("android.permission.BATTERY_STATS", null);
        Parcel out = Parcel.obtain();
        updateExternalStatsSync("get-stats", 15);
        synchronized (this.mStats) {
            this.mStats.writeToParcel(out, 0);
        }
        byte[] data = out.marshall();
        out.recycle();
        return data;
    }

    public ParcelFileDescriptor getStatisticsStream() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        Parcel out = Parcel.obtain();
        updateExternalStatsSync("get-stats", 15);
        synchronized (this.mStats) {
            this.mStats.writeToParcel(out, 0);
        }
        byte[] data = out.marshall();
        out.recycle();
        try {
            return ParcelFileDescriptor.fromData(data, "battery-stats");
        } catch (IOException e) {
            Slog.w(TAG, "Unable to create shared memory", e);
            return null;
        }
    }

    public boolean isCharging() {
        boolean isCharging;
        synchronized (this.mStats) {
            isCharging = this.mStats.isCharging();
        }
        return isCharging;
    }

    public long computeBatteryTimeRemaining() {
        long time;
        synchronized (this.mStats) {
            time = this.mStats.computeBatteryTimeRemaining(SystemClock.elapsedRealtime());
            if (time >= 0) {
                time /= 1000;
            }
        }
        return time;
    }

    public long computeChargeTimeRemaining() {
        long time;
        synchronized (this.mStats) {
            time = this.mStats.computeChargeTimeRemaining(SystemClock.elapsedRealtime());
            if (time >= 0) {
                time /= 1000;
            }
        }
        return time;
    }

    public void noteEvent(int code, String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteEventLocked(code, name, uid);
        }
        if (this.misBetaUser && code == 32771) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoTopApp(name, uid);
            }
        }
    }

    public void noteSyncStart(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteSyncStartLocked(name, uid);
        }
    }

    public void noteSyncFinish(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteSyncFinishLocked(name, uid);
        }
    }

    public void noteJobStart(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteJobStartLocked(name, uid);
        }
    }

    public void noteJobFinish(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteJobFinishLocked(name, uid);
        }
    }

    public void noteAlarmStart(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteAlarmStartLocked(name, uid);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoStartAlarm(name, uid);
            }
        }
    }

    public void noteAlarmFinish(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteAlarmFinishLocked(name, uid);
        }
    }

    public void noteStartWakelock(int uid, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStartWakeLocked(uid, pid, name, historyName, type, unimportantForLogging, SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoAcquireWakeLock(name, pid);
            }
        }
    }

    public void noteStopWakelock(int uid, int pid, String name, String historyName, int type) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStopWakeLocked(uid, pid, name, historyName, type, SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoReleaseWakeLock(name, pid);
            }
        }
    }

    public void noteStartWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStartWakeFromSourceLocked(ws, pid, name, historyName, type, unimportantForLogging);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoAcquireWakeLock(name, pid);
            }
        }
    }

    public void noteChangeWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteChangeWakelockFromSourceLocked(ws, pid, name, historyName, type, newWs, newPid, newName, newHistoryName, newType, newUnimportantForLogging);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoChangeWakeLock(name, pid, newName, newPid);
            }
        }
    }

    public void noteStopWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStopWakeFromSourceLocked(ws, pid, name, historyName, type);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoReleaseWakeLock(name, pid);
            }
        }
    }

    public void noteStartSensor(int uid, int sensor) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStartSensorLocked(uid, sensor);
        }
    }

    public void noteStopSensor(int uid, int sensor) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStopSensorLocked(uid, sensor);
        }
    }

    public void noteVibratorOn(int uid, long durationMillis) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteVibratorOnLocked(uid, durationMillis);
        }
    }

    public void noteVibratorOff(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteVibratorOffLocked(uid);
        }
    }

    public void noteStartGps(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStartGpsLocked(uid);
            LogPower.push(156, Integer.toString(uid));
        }
    }

    public void noteStopGps(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStopGpsLocked(uid);
            LogPower.push(157, Integer.toString(uid));
        }
    }

    public void noteScreenState(int state) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteScreenStateLocked(state);
        }
    }

    public void noteScreenBrightness(int brightness) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteScreenBrightnessLocked(brightness);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoBrightness(brightness);
            }
        }
    }

    public void noteUserActivity(int uid, int event) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteUserActivityLocked(uid, event);
        }
    }

    public void noteWakeUp(String reason, int reasonUid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWakeUpLocked(reason, reasonUid);
        }
    }

    public void noteInteractive(boolean interactive) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteInteractiveLocked(interactive);
        }
    }

    public void noteConnectivityChanged(int type, String extra) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteConnectivityChangedLocked(type, extra);
        }
    }

    public void noteMobileRadioPowerState(int powerState, long timestampNs, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteMobileRadioPowerState(powerState, timestampNs, uid);
        }
    }

    public void notePhoneOn() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.notePhoneOnLocked();
        }
    }

    public void notePhoneOff() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.notePhoneOffLocked();
        }
    }

    public void notePhoneSignalStrength(SignalStrength signalStrength) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.notePhoneSignalStrengthLocked(signalStrength);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoSignalStrength(signalStrength.getLevel());
            }
        }
    }

    public void notePhoneDataConnectionState(int dataType, boolean hasData) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.notePhoneDataConnectionStateLocked(dataType, hasData);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoConnectionState(dataType, hasData);
            }
        }
    }

    public void notePhoneState(int state) {
        enforceCallingPermission();
        int simState = TelephonyManager.getDefault().getSimState();
        synchronized (this.mStats) {
            this.mStats.notePhoneStateLocked(state, simState);
        }
    }

    public void noteWifiOn() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiOnLocked();
        }
    }

    public void noteWifiOff() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiOffLocked();
        }
    }

    public void noteStartAudio(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteAudioOnLocked(uid);
        }
    }

    public void noteStopAudio(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteAudioOffLocked(uid);
        }
    }

    public void noteStartVideo(int uid) {
        enforceCallingPermission();
        LogPower.push(136, Integer.toString(uid));
        synchronized (this.mStats) {
            this.mStats.noteVideoOnLocked(uid);
        }
    }

    public void noteStopVideo(int uid) {
        enforceCallingPermission();
        LogPower.push(137, Integer.toString(uid));
        synchronized (this.mStats) {
            this.mStats.noteVideoOffLocked(uid);
        }
    }

    public void noteResetAudio() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetAudioLocked();
        }
    }

    public void noteResetVideo() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetVideoLocked();
        }
    }

    public void noteFlashlightOn(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFlashlightOnLocked(uid);
        }
    }

    public void noteFlashlightOff(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFlashlightOffLocked(uid);
        }
    }

    public void noteStartCamera(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteCameraOnLocked(uid);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.noteStartCamera();
            }
        }
    }

    public void noteStopCamera(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteCameraOffLocked(uid);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.noteStopCamera();
            }
        }
    }

    public void noteResetCamera() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetCameraLocked();
        }
    }

    public void noteResetFlashlight() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetFlashlightLocked();
        }
    }

    public void noteWifiRadioPowerState(int powerState, long tsNanos) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            if (this.mStats.isOnBattery()) {
                String type;
                if (powerState == 3 || powerState == 2) {
                    type = "active";
                } else {
                    type = "inactive";
                }
                this.mHandler.scheduleSync("wifi-data: " + type, 2);
            }
            this.mStats.noteWifiRadioPowerState(powerState, tsNanos);
        }
    }

    public void noteWifiRunning(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiRunningLocked(ws);
        }
    }

    public void noteWifiRunningChanged(WorkSource oldWs, WorkSource newWs) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiRunningChangedLocked(oldWs, newWs);
        }
    }

    public void noteWifiStopped(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiStoppedLocked(ws);
        }
    }

    public void noteWifiState(int wifiState, String accessPoint) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiStateLocked(wifiState, accessPoint);
        }
    }

    public void noteWifiSupplicantStateChanged(int supplState, boolean failedAuth) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiSupplicantStateChangedLocked(supplState, failedAuth);
        }
        if (this.misBetaUser) {
            synchronized (this.mPowerInfoService) {
                this.mPowerInfoService.notePowerInfoWifiState(supplState);
            }
        }
    }

    public void noteWifiRssiChanged(int newRssi) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiRssiChangedLocked(newRssi);
        }
    }

    public void noteFullWifiLockAcquired(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFullWifiLockAcquiredLocked(uid);
        }
    }

    public void noteFullWifiLockReleased(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFullWifiLockReleasedLocked(uid);
        }
    }

    public void noteWifiScanStarted(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiScanStartedLocked(uid);
        }
    }

    public void noteWifiScanStopped(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiScanStoppedLocked(uid);
        }
    }

    public void noteWifiMulticastEnabled(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiMulticastEnabledLocked(uid);
        }
    }

    public void noteWifiMulticastDisabled(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiMulticastDisabledLocked(uid);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void noteFullWifiLockAcquiredFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            int i = 0;
            while (true) {
                if (i < ws.size()) {
                    HwLog.bdate("BDAT_TAG_WIFI_LOCK_ACQUIRED", "uid=" + ws.get(i));
                    i++;
                } else {
                    this.mStats.noteFullWifiLockAcquiredFromSourceLocked(ws);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void noteFullWifiLockReleasedFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            int i = 0;
            while (true) {
                if (i < ws.size()) {
                    HwLog.bdate("BDAT_TAG_WIFI_LOCK_RELEASED", "uid=" + ws.get(i));
                    i++;
                } else {
                    this.mStats.noteFullWifiLockReleasedFromSourceLocked(ws);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void noteWifiScanStartedFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            int i = 0;
            while (true) {
                if (i < ws.size()) {
                    HwLog.bdate("BDAT_TAG_WIFI_SCAN_STARTED", "uid=" + ws.get(i));
                    i++;
                } else {
                    this.mStats.noteWifiScanStartedFromSourceLocked(ws);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void noteWifiScanStoppedFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            int i = 0;
            while (true) {
                if (i < ws.size()) {
                    HwLog.bdate("BDAT_TAG_WIFI_SCAN_STOPPED", "uid=" + ws.get(i));
                    i++;
                } else {
                    this.mStats.noteWifiScanStoppedFromSourceLocked(ws);
                }
            }
        }
    }

    public void noteWifiBatchedScanStartedFromSource(WorkSource ws, int csph) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiBatchedScanStartedFromSourceLocked(ws, csph);
        }
    }

    public void noteWifiBatchedScanStoppedFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiBatchedScanStoppedFromSourceLocked(ws);
        }
    }

    public void noteWifiMulticastEnabledFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiMulticastEnabledFromSourceLocked(ws);
        }
    }

    public void noteWifiMulticastDisabledFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiMulticastDisabledFromSourceLocked(ws);
        }
    }

    public void noteNetworkInterfaceType(String iface, int networkType) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteNetworkInterfaceTypeLocked(iface, networkType);
        }
    }

    public void noteNetworkStatsEnabled() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteNetworkStatsEnabledLocked();
        }
    }

    public void noteDeviceIdleMode(int mode, String activeReason, int activeUid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteDeviceIdleModeLocked(mode, activeReason, activeUid);
        }
    }

    public void notePackageInstalled(String pkgName, int versionCode) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.notePackageInstalledLocked(pkgName, versionCode);
        }
    }

    public void notePackageUninstalled(String pkgName) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.notePackageUninstalledLocked(pkgName);
        }
    }

    public void noteBleScanStarted(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteBluetoothScanStartedFromSourceLocked(ws);
        }
    }

    public void noteBleScanStopped(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteBluetoothScanStoppedFromSourceLocked(ws);
        }
    }

    public void noteResetBleScan() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetBluetoothScanLocked();
        }
    }

    public void noteWifiControllerActivity(WifiActivityEnergyInfo info) {
        enforceCallingPermission();
        if (info == null || !info.isValid()) {
            Slog.e(TAG, "invalid wifi data given: " + info);
            return;
        }
        synchronized (this.mStats) {
            this.mStats.updateWifiStateLocked(info);
        }
    }

    public void noteBluetoothControllerActivity(BluetoothActivityEnergyInfo info) {
        enforceCallingPermission();
        if (info == null || !info.isValid()) {
            Slog.e(TAG, "invalid bluetooth data given: " + info);
            return;
        }
        synchronized (this.mStats) {
            this.mStats.updateBluetoothStateLocked(info);
        }
    }

    public void noteModemControllerActivity(ModemActivityInfo info) {
        enforceCallingPermission();
        if (info == null || !info.isValid()) {
            Slog.e(TAG, "invalid modem data given: " + info);
            return;
        }
        synchronized (this.mStats) {
            this.mStats.updateMobileRadioStateLocked(SystemClock.elapsedRealtime(), info);
        }
    }

    public boolean isOnBattery() {
        return this.mStats.isOnBattery();
    }

    public void setBatteryState(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh) {
        enforceCallingPermission();
        this.mHandler.post(new AnonymousClass1(plugType, status, health, level, temp, volt, chargeUAh));
    }

    public long getAwakeTimeBattery() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        return this.mStats.getAwakeTimeBattery();
    }

    public long getAwakeTimePlugged() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        return this.mStats.getAwakeTimePlugged();
    }

    public void enforceCallingPermission() {
        if (Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_DEVICE_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
    }

    private void dumpHelp(PrintWriter pw) {
        pw.println("Battery stats (batterystats) dump options:");
        pw.println("  [--checkin] [--history] [--history-start] [--charged] [-c]");
        pw.println("  [--daily] [--reset] [--write] [--new-daily] [--read-daily] [-h] [<package.name>]");
        pw.println("  --checkin: generate output for a checkin report; will write (and clear) the");
        pw.println("             last old completed stats when they had been reset.");
        pw.println("  -c: write the current stats in checkin format.");
        pw.println("  --history: show only history data.");
        pw.println("  --history-start <num>: show only history data starting at given time offset.");
        pw.println("  --charged: only output data since last charged.");
        pw.println("  --daily: only output full daily data.");
        pw.println("  --reset: reset the stats, clearing all current data.");
        pw.println("  --write: force write current collected stats to disk.");
        pw.println("  --new-daily: immediately create and write new daily stats record.");
        pw.println("  --read-daily: read-load last written daily stats.");
        pw.println("  <package.name>: optional name of package to filter output by.");
        pw.println("  -h: print this help text.");
        pw.println("Battery stats (batterystats) commands:");
        pw.println("  enable|disable <option>");
        pw.println("    Enable or disable a running option.  Option state is not saved across boots.");
        pw.println("    Options are:");
        pw.println("      full-history: include additional detailed events in battery history:");
        pw.println("          wake_lock_in, alarms and proc events");
        pw.println("      no-auto-reset: don't automatically reset stats when unplugged");
    }

    private int doEnableOrDisable(PrintWriter pw, int i, String[] args, boolean enable) {
        i++;
        if (i >= args.length) {
            pw.println("Missing option argument for " + (enable ? "--enable" : "--disable"));
            dumpHelp(pw);
            return -1;
        }
        BatteryStatsImpl batteryStatsImpl;
        if ("full-wake-history".equals(args[i]) || "full-history".equals(args[i])) {
            batteryStatsImpl = this.mStats;
            synchronized (batteryStatsImpl) {
            }
            this.mStats.setRecordAllHistoryLocked(enable);
        } else if ("no-auto-reset".equals(args[i])) {
            batteryStatsImpl = this.mStats;
            synchronized (batteryStatsImpl) {
            }
            this.mStats.setNoAutoReset(enable);
        } else {
            pw.println("Unknown enable/disable option: " + args[i]);
            dumpHelp(pw);
            return -1;
        }
        return i;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump BatteryStats from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        int flags = 0;
        boolean useCheckinFormat = false;
        boolean isRealCheckin = false;
        boolean noOutput = false;
        boolean writeData = false;
        long historyStart = -1;
        int reqUid = -1;
        if (args != null) {
            int i = 0;
            while (i < args.length) {
                String arg = args[i];
                if ("--checkin".equals(arg)) {
                    useCheckinFormat = true;
                    isRealCheckin = true;
                } else if ("--history".equals(arg)) {
                    flags |= 8;
                } else if ("--history-start".equals(arg)) {
                    flags |= 8;
                    i++;
                    if (i >= args.length) {
                        pw.println("Missing time argument for --history-since");
                        dumpHelp(pw);
                        return;
                    }
                    historyStart = Long.parseLong(args[i]);
                    writeData = true;
                } else if ("-c".equals(arg)) {
                    useCheckinFormat = true;
                    flags |= 16;
                } else if ("--charged".equals(arg)) {
                    flags |= 2;
                } else if ("--daily".equals(arg)) {
                    flags |= 4;
                } else if ("--reset".equals(arg)) {
                    synchronized (this.mStats) {
                        this.mStats.resetAllStatsCmdLocked();
                        pw.println("Battery stats reset.");
                        noOutput = true;
                    }
                    updateExternalStatsSync("dump", 15);
                } else if ("--write".equals(arg)) {
                    updateExternalStatsSync("dump", 15);
                    synchronized (this.mStats) {
                        this.mStats.writeSyncLocked();
                        pw.println("Battery stats written.");
                        noOutput = true;
                    }
                } else if ("--new-daily".equals(arg)) {
                    synchronized (this.mStats) {
                        this.mStats.recordDailyStatsLocked();
                        pw.println("New daily stats written.");
                        noOutput = true;
                    }
                } else if ("--read-daily".equals(arg)) {
                    synchronized (this.mStats) {
                        this.mStats.readDailyStatsLocked();
                        pw.println("Last daily stats read.");
                        noOutput = true;
                    }
                } else if ("--enable".equals(arg) || "enable".equals(arg)) {
                    i = doEnableOrDisable(pw, i, args, true);
                    if (i >= 0) {
                        pw.println("Enabled: " + args[i]);
                        return;
                    }
                    return;
                } else if ("--disable".equals(arg) || "disable".equals(arg)) {
                    i = doEnableOrDisable(pw, i, args, false);
                    if (i >= 0) {
                        pw.println("Disabled: " + args[i]);
                        return;
                    }
                    return;
                } else if ("-h".equals(arg)) {
                    dumpHelp(pw);
                    return;
                } else if ("-a".equals(arg)) {
                    flags |= 32;
                } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                    try {
                        reqUid = this.mContext.getPackageManager().getPackageUidAsUser(arg, UserHandle.getCallingUserId());
                    } catch (NameNotFoundException e) {
                        pw.println("Unknown package: " + arg);
                        dumpHelp(pw);
                        return;
                    }
                } else {
                    pw.println("Unknown option: " + arg);
                    dumpHelp(pw);
                    return;
                }
                i++;
            }
        }
        if (!noOutput) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (BatteryStatsHelper.checkWifiOnly(this.mContext)) {
                    flags |= 64;
                }
                updateExternalStatsSync("dump", 15);
                if (reqUid >= 0 && (flags & 10) == 0) {
                    flags = (flags | 2) & -17;
                }
                BatteryStatsImpl batteryStatsImpl;
                if (useCheckinFormat) {
                    List<ApplicationInfo> apps = this.mContext.getPackageManager().getInstalledApplications(139264);
                    if (isRealCheckin) {
                        synchronized (this.mStats.mCheckinFile) {
                            if (this.mStats.mCheckinFile.exists()) {
                                try {
                                    byte[] raw = this.mStats.mCheckinFile.readFully();
                                    if (raw != null) {
                                        Parcel in = Parcel.obtain();
                                        in.unmarshall(raw, 0, raw.length);
                                        in.setDataPosition(0);
                                        BatteryStatsImpl checkinStats = new BatteryStatsImpl(null, this.mStats.mHandler, null);
                                        checkinStats.readSummaryFromParcel(in);
                                        in.recycle();
                                        checkinStats.dumpCheckinLocked(this.mContext, pw, apps, flags, historyStart);
                                        this.mStats.mCheckinFile.delete();
                                        return;
                                    }
                                } catch (Throwable e2) {
                                    Slog.w(TAG, "Failure reading checkin file " + this.mStats.mCheckinFile.getBaseFile(), e2);
                                }
                            }
                        }
                    }
                    batteryStatsImpl = this.mStats;
                    synchronized (batteryStatsImpl) {
                    }
                    this.mStats.dumpCheckinLocked(this.mContext, pw, apps, flags, historyStart);
                    if (writeData) {
                        this.mStats.writeAsyncLocked();
                    }
                } else {
                    batteryStatsImpl = this.mStats;
                    synchronized (batteryStatsImpl) {
                    }
                    this.mStats.dumpLocked(this.mContext, pw, flags, reqUid, historyStart);
                    if (writeData) {
                        this.mStats.writeAsyncLocked();
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private WifiActivityEnergyInfo extractDelta(WifiActivityEnergyInfo latest) {
        long timePeriodMs = latest.mTimestamp - this.mLastInfo.mTimestamp;
        long lastIdleMs = this.mLastInfo.mControllerIdleTimeMs;
        long lastTxMs = this.mLastInfo.mControllerTxTimeMs;
        long lastRxMs = this.mLastInfo.mControllerRxTimeMs;
        long lastEnergy = this.mLastInfo.mControllerEnergyUsed;
        WifiActivityEnergyInfo delta = this.mLastInfo;
        delta.mTimestamp = latest.getTimeStamp();
        delta.mStackState = latest.getStackState();
        long txTimeMs = latest.mControllerTxTimeMs - lastTxMs;
        long rxTimeMs = latest.mControllerRxTimeMs - lastRxMs;
        long idleTimeMs = latest.mControllerIdleTimeMs - lastIdleMs;
        if (txTimeMs < 0 || rxTimeMs < 0) {
            delta.mControllerEnergyUsed = latest.mControllerEnergyUsed;
            delta.mControllerRxTimeMs = latest.mControllerRxTimeMs;
            delta.mControllerTxTimeMs = latest.mControllerTxTimeMs;
            delta.mControllerIdleTimeMs = latest.mControllerIdleTimeMs;
            Slog.v(TAG, "WiFi energy data was reset, new WiFi energy data is " + delta);
        } else {
            long maxExpectedIdleTimeMs;
            long totalActiveTimeMs = txTimeMs + rxTimeMs;
            if (totalActiveTimeMs > timePeriodMs) {
                maxExpectedIdleTimeMs = 0;
                if (totalActiveTimeMs > MAX_WIFI_STATS_SAMPLE_ERROR_MILLIS + timePeriodMs) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Total Active time ");
                    TimeUtils.formatDuration(totalActiveTimeMs, sb);
                    sb.append(" is longer than sample period ");
                    TimeUtils.formatDuration(timePeriodMs, sb);
                    sb.append(".\n");
                    sb.append("Previous WiFi snapshot: ").append("idle=");
                    TimeUtils.formatDuration(lastIdleMs, sb);
                    sb.append(" rx=");
                    TimeUtils.formatDuration(lastRxMs, sb);
                    sb.append(" tx=");
                    TimeUtils.formatDuration(lastTxMs, sb);
                    sb.append(" e=").append(lastEnergy);
                    sb.append("\n");
                    sb.append("Current WiFi snapshot: ").append("idle=");
                    TimeUtils.formatDuration(latest.mControllerIdleTimeMs, sb);
                    sb.append(" rx=");
                    TimeUtils.formatDuration(latest.mControllerRxTimeMs, sb);
                    sb.append(" tx=");
                    TimeUtils.formatDuration(latest.mControllerTxTimeMs, sb);
                    sb.append(" e=").append(latest.mControllerEnergyUsed);
                    Slog.wtf(TAG, sb.toString());
                }
            } else {
                maxExpectedIdleTimeMs = timePeriodMs - totalActiveTimeMs;
            }
            delta.mControllerTxTimeMs = txTimeMs;
            delta.mControllerRxTimeMs = rxTimeMs;
            delta.mControllerIdleTimeMs = Math.min(maxExpectedIdleTimeMs, Math.max(0, idleTimeMs));
            delta.mControllerEnergyUsed = Math.max(0, latest.mControllerEnergyUsed - lastEnergy);
        }
        this.mLastInfo = latest;
        return delta;
    }

    private static <T extends Parcelable> T awaitControllerInfo(SynchronousResultReceiver receiver) throws TimeoutException {
        if (receiver == null) {
            return null;
        }
        Result result = receiver.awaitResult(EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS);
        if (result.bundle != null) {
            result.bundle.setDefusable(true);
            T data = result.bundle.getParcelable("controller_activity");
            if (data != null) {
                return data;
            }
        }
        Slog.e(TAG, "no controller energy info supplied");
        return null;
    }

    void updateExternalStatsSync(String reason, int updateFlags) {
        SynchronousResultReceiver synchronousResultReceiver = null;
        SynchronousResultReceiver synchronousResultReceiver2 = null;
        SynchronousResultReceiver synchronousResultReceiver3 = null;
        synchronized (this.mExternalStatsLock) {
            if (this.mContext == null) {
                return;
            }
            if ((updateFlags & 2) != 0) {
                if (this.mWifiManager == null) {
                    this.mWifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
                }
                if (this.mWifiManager != null) {
                    try {
                        SynchronousResultReceiver wifiReceiver = new SynchronousResultReceiver();
                        try {
                            this.mWifiManager.requestActivityInfo(wifiReceiver);
                            synchronousResultReceiver = wifiReceiver;
                        } catch (RemoteException e) {
                            synchronousResultReceiver = wifiReceiver;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            throw th2;
                        }
                    } catch (RemoteException e2) {
                    }
                }
            }
            if ((updateFlags & 8) != 0) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null) {
                    SynchronousResultReceiver bluetoothReceiver = new SynchronousResultReceiver();
                    try {
                        adapter.requestControllerActivityEnergyInfo(bluetoothReceiver);
                        synchronousResultReceiver2 = bluetoothReceiver;
                    } catch (Throwable th3) {
                        th2 = th3;
                        throw th2;
                    }
                }
            }
            if ((updateFlags & 4) != 0) {
                if (this.mTelephony == null) {
                    this.mTelephony = TelephonyManager.from(this.mContext);
                }
                if (this.mTelephony != null) {
                    SynchronousResultReceiver modemReceiver = new SynchronousResultReceiver();
                    try {
                        this.mTelephony.requestModemActivityInfo(modemReceiver);
                        synchronousResultReceiver3 = modemReceiver;
                    } catch (Throwable th4) {
                        th2 = th4;
                        throw th2;
                    }
                }
            }
            WifiActivityEnergyInfo wifiInfo = null;
            BluetoothActivityEnergyInfo bluetoothInfo = null;
            ModemActivityInfo modemInfo = null;
            try {
                wifiInfo = (WifiActivityEnergyInfo) awaitControllerInfo(synchronousResultReceiver);
            } catch (TimeoutException e3) {
                Slog.w(TAG, "Timeout reading wifi stats");
            } catch (Throwable th5) {
                th2 = th5;
                throw th2;
            }
            try {
                bluetoothInfo = (BluetoothActivityEnergyInfo) awaitControllerInfo(synchronousResultReceiver2);
            } catch (TimeoutException e4) {
                Slog.w(TAG, "Timeout reading bt stats");
            }
            try {
                modemInfo = (ModemActivityInfo) awaitControllerInfo(synchronousResultReceiver3);
            } catch (TimeoutException e5) {
                Slog.w(TAG, "Timeout reading modem stats");
            }
            synchronized (this.mStats) {
                this.mStats.addHistoryEventLocked(SystemClock.elapsedRealtime(), SystemClock.uptimeMillis(), 14, reason, 0);
                this.mStats.updateCpuTimeLocked();
                this.mStats.updateKernelWakelocksLocked();
                if (wifiInfo != null) {
                    if (wifiInfo.isValid()) {
                        this.mStats.updateWifiStateLocked(extractDelta(wifiInfo));
                    } else {
                        Slog.e(TAG, "wifi info is invalid: " + wifiInfo);
                    }
                }
                if (bluetoothInfo != null) {
                    if (bluetoothInfo.isValid()) {
                        this.mStats.updateBluetoothStateLocked(bluetoothInfo);
                    } else {
                        Slog.e(TAG, "bluetooth info is invalid: " + bluetoothInfo);
                    }
                }
                if (modemInfo != null) {
                    if (modemInfo.isValid()) {
                        this.mStats.updateMobileRadioStateLocked(SystemClock.elapsedRealtime(), modemInfo);
                    } else {
                        Slog.e(TAG, "modem info is invalid: " + modemInfo);
                    }
                }
            }
        }
    }

    public HealthStatsParceler takeUidSnapshot(int requestUid) {
        if (requestUid != Binder.getCallingUid()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            HealthStatsParceler healthStatsForUidLocked;
            updateExternalStatsSync("get-health-stats-for-uid", 15);
            synchronized (this.mStats) {
                healthStatsForUidLocked = getHealthStatsForUidLocked(requestUid);
            }
            Binder.restoreCallingIdentity(ident);
            return healthStatsForUidLocked;
        } catch (Exception ex) {
            try {
                Slog.d(TAG, "Crashed while writing for takeUidSnapshot(" + requestUid + ")", ex);
                throw ex;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public HealthStatsParceler[] takeUidSnapshots(int[] requestUids) {
        if (!onlyCaller(requestUids)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        }
        long ident = Binder.clearCallingIdentity();
        int i = -1;
        try {
            HealthStatsParceler[] results;
            updateExternalStatsSync("get-health-stats-for-uids", 15);
            synchronized (this.mStats) {
                int N = requestUids.length;
                results = new HealthStatsParceler[N];
                i = 0;
                while (i < N) {
                    results[i] = getHealthStatsForUidLocked(requestUids[i]);
                    i++;
                }
            }
            Binder.restoreCallingIdentity(ident);
            return results;
        } catch (Exception ex) {
            try {
                Slog.d(TAG, "Crashed while writing for takeUidSnapshots(" + Arrays.toString(requestUids) + ") i=" + i, ex);
                throw ex;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private static boolean onlyCaller(int[] requestUids) {
        int caller = Binder.getCallingUid();
        for (int i : requestUids) {
            if (i != caller) {
                return false;
            }
        }
        return true;
    }

    HealthStatsParceler getHealthStatsForUidLocked(int requestUid) {
        HealthStatsBatteryStatsWriter writer = new HealthStatsBatteryStatsWriter();
        HealthStatsWriter uidWriter = new HealthStatsWriter(UidHealthStats.CONSTANTS);
        Uid uid = (Uid) this.mStats.getUidStats().get(requestUid);
        if (uid != null) {
            writer.writeUid(uidWriter, this.mStats, uid);
        }
        return new HealthStatsParceler(uidWriter);
    }
}
