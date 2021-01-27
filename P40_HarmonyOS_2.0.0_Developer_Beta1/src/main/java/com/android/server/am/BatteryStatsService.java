package com.android.server.am;

import android.app.ActivityManager;
import android.bluetooth.BluetoothActivityEnergyInfo;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.BatteryStats;
import android.os.BatteryStatsInternal;
import android.os.Binder;
import android.os.Handler;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFormatException;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.WorkSource;
import android.os.connectivity.CellularBatteryStats;
import android.os.connectivity.GpsBatteryStats;
import android.os.connectivity.WifiBatteryStats;
import android.os.health.HealthStatsParceler;
import android.os.health.HealthStatsWriter;
import android.os.health.UidHealthStats;
import android.provider.Settings;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.HwLog;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.AtomicFile;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.android.internal.os.RailStats;
import com.android.internal.os.RpmStats;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.ParseUtils;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerService;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.utils.PriorityDump;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class BatteryStatsService extends IBatteryStats.Stub implements PowerManagerInternal.LowPowerModeListener, BatteryStatsImpl.PlatformIdleStateCallback, BatteryStatsImpl.RailEnergyDataCallback {
    static final boolean DBG = false;
    private static final int MAX_LOW_POWER_STATS_SIZE = 2048;
    static final String TAG = "BatteryStatsService";
    private static IBatteryStats sService;
    private ActivityManagerService mActivityManagerService;
    private final Context mContext;
    private CharsetDecoder mDecoderStat = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?");
    final BatteryStatsImpl mStats;
    private final BatteryStatsImpl.UserInfoProvider mUserManagerUserInfoProvider;
    private CharBuffer mUtf16BufferStat = CharBuffer.allocate(2048);
    private ByteBuffer mUtf8BufferStat = ByteBuffer.allocateDirect(2048);
    private final BatteryExternalStatsWorker mWorker;

    private native void getLowPowerStats(RpmStats rpmStats);

    private native int getPlatformLowPowerStats(ByteBuffer byteBuffer);

    private native void getRailEnergyPowerStats(RailStats railStats);

    private native int getSubsystemLowPowerStats(ByteBuffer byteBuffer);

    /* access modifiers changed from: private */
    public static native int nativeWaitWakeup(ByteBuffer byteBuffer);

    public void fillLowPowerStats(RpmStats rpmStats) {
        getLowPowerStats(rpmStats);
    }

    public void fillRailDataStats(RailStats railStats) {
        getRailEnergyPowerStats(railStats);
    }

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

    public String getSubsystemLowPowerStats() {
        this.mUtf8BufferStat.clear();
        this.mUtf16BufferStat.clear();
        this.mDecoderStat.reset();
        int bytesWritten = getSubsystemLowPowerStats(this.mUtf8BufferStat);
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

    BatteryStatsService(Context context, File systemDir, Handler handler) {
        this.mContext = context;
        this.mUserManagerUserInfoProvider = new BatteryStatsImpl.UserInfoProvider() {
            /* class com.android.server.am.BatteryStatsService.AnonymousClass1 */
            private UserManagerInternal umi;

            public int[] getUserIds() {
                if (this.umi == null) {
                    this.umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
                }
                UserManagerInternal userManagerInternal = this.umi;
                if (userManagerInternal != null) {
                    return userManagerInternal.getUserIds();
                }
                return null;
            }
        };
        this.mStats = new BatteryStatsImpl(systemDir, handler, this, this, this.mUserManagerUserInfoProvider);
        this.mWorker = new BatteryExternalStatsWorker(context, this.mStats);
        this.mStats.setExternalStatsSyncLocked(this.mWorker);
        this.mStats.setRadioScanningTimeoutLocked(((long) this.mContext.getResources().getInteger(17694877)) * 1000);
        this.mStats.setPowerProfileLocked(new PowerProfile(context));
    }

    public void publish() {
        LocalServices.addService(BatteryStatsInternal.class, new LocalService());
        ServiceManager.addService("batterystats", asBinder());
    }

    public void systemServicesReady() {
        this.mStats.systemServicesReady(this.mContext);
    }

    private final class LocalService extends BatteryStatsInternal {
        private LocalService() {
        }

        public String[] getWifiIfaces() {
            return (String[]) BatteryStatsService.this.mStats.getWifiIfaces().clone();
        }

        public String[] getMobileIfaces() {
            return (String[]) BatteryStatsService.this.mStats.getMobileIfaces().clone();
        }

        public void noteJobsDeferred(int uid, int numDeferred, long sinceLast) {
            BatteryStatsService.this.noteJobsDeferred(uid, numDeferred, sinceLast);
        }
    }

    private static void awaitUninterruptibly(Future<?> future) {
        while (true) {
            try {
                future.get();
                return;
            } catch (ExecutionException e) {
                return;
            } catch (InterruptedException e2) {
            }
        }
    }

    private void syncStats(String reason, int flags) {
        awaitUninterruptibly(this.mWorker.scheduleSync(reason, flags));
    }

    public void initPowerManagement() {
        PowerManagerInternal powerMgr = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        powerMgr.registerLowPowerModeObserver(this);
        synchronized (this.mStats) {
            this.mStats.notePowerSaveModeLocked(powerMgr.getLowPowerState(9).batterySaverEnabled);
        }
        new WakeupReasonThread().start();
    }

    public void shutdown() {
        Slog.w("BatteryStats", "Writing battery stats before shutdown...");
        syncStats("shutdown", 31);
        synchronized (this.mStats) {
            this.mStats.shutdownLocked();
        }
        this.mWorker.shutdown();
    }

    public static IBatteryStats getService() {
        IBatteryStats iBatteryStats = sService;
        if (iBatteryStats != null) {
            return iBatteryStats;
        }
        sService = asInterface(ServiceManager.getService("batterystats"));
        return sService;
    }

    public int getServiceType() {
        return 9;
    }

    public void onLowPowerModeChanged(PowerSaveState result) {
        synchronized (this.mStats) {
            this.mStats.notePowerSaveModeLocked(result.batterySaverEnabled);
        }
    }

    public BatteryStatsImpl getActiveStatistics() {
        return this.mStats;
    }

    public void scheduleWriteToDisk() {
        this.mWorker.scheduleWrite();
    }

    /* access modifiers changed from: package-private */
    public void removeUid(int uid) {
        synchronized (this.mStats) {
            this.mStats.removeUidStatsLocked(uid);
        }
    }

    /* access modifiers changed from: package-private */
    public void onCleanupUser(int userId) {
        synchronized (this.mStats) {
            this.mStats.onCleanupUserLocked(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserRemoved(int userId) {
        synchronized (this.mStats) {
            this.mStats.onUserRemovedLocked(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void addIsolatedUid(int isolatedUid, int appUid) {
        synchronized (this.mStats) {
            this.mStats.addIsolatedUidLocked(isolatedUid, appUid);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeIsolatedUid(int isolatedUid, int appUid) {
        synchronized (this.mStats) {
            this.mStats.scheduleRemoveIsolatedUidLocked(isolatedUid, appUid);
        }
    }

    /* access modifiers changed from: package-private */
    public void noteProcessStart(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessStartLocked(name, uid);
            StatsLog.write(28, uid, name, 1);
        }
    }

    /* access modifiers changed from: package-private */
    public void noteProcessCrash(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessCrashLocked(name, uid);
            StatsLog.write(28, uid, name, 2);
        }
    }

    /* access modifiers changed from: package-private */
    public void noteProcessAnr(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessAnrLocked(name, uid);
        }
    }

    /* access modifiers changed from: package-private */
    public void noteProcessFinish(String name, int uid) {
        synchronized (this.mStats) {
            this.mStats.noteProcessFinishLocked(name, uid);
            StatsLog.write(28, uid, name, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void noteUidProcessState(int uid, int state) {
        synchronized (this.mStats) {
            StatsLog.write(27, uid, ActivityManager.processStateAmToProto(state));
            this.mStats.noteUidProcessStateLocked(uid, state);
        }
    }

    public byte[] getStatistics() {
        this.mContext.enforceCallingPermission("android.permission.BATTERY_STATS", null);
        Parcel out = Parcel.obtain();
        syncStats("get-stats", 31);
        synchronized (this.mStats) {
            this.mStats.writeToParcel(out, 0);
        }
        byte[] data = out.marshall();
        out.recycle();
        return data;
    }

    public ParcelFileDescriptor getStatisticsStream() {
        this.mContext.enforceCallingPermission("android.permission.BATTERY_STATS", null);
        Parcel out = Parcel.obtain();
        syncStats("get-stats", 31);
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
        long j;
        synchronized (this.mStats) {
            long time = this.mStats.computeBatteryTimeRemaining(SystemClock.elapsedRealtime());
            j = time >= 0 ? time / 1000 : time;
        }
        return j;
    }

    public long computeChargeTimeRemaining() {
        long j;
        synchronized (this.mStats) {
            long time = this.mStats.computeChargeTimeRemaining(SystemClock.elapsedRealtime());
            j = time >= 0 ? time / 1000 : time;
        }
        return j;
    }

    public void noteEvent(int code, String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteEventLocked(code, name, uid);
        }
    }

    public void noteSyncStart(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteSyncStartLocked(name, uid);
            StatsLog.write_non_chained(7, uid, null, name, 1);
        }
    }

    public void noteSyncFinish(String name, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteSyncFinishLocked(name, uid);
            StatsLog.write_non_chained(7, uid, null, name, 0);
        }
    }

    public void noteJobStart(String name, int uid, int standbyBucket, int jobid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteJobStartLocked(name, uid);
            StatsLog.write_non_chained(8, uid, null, name, 1, -1, standbyBucket, jobid);
        }
    }

    public void noteJobFinish(String name, int uid, int stopReason, int standbyBucket, int jobid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteJobFinishLocked(name, uid, stopReason);
            StatsLog.write_non_chained(8, uid, null, name, 0, stopReason, standbyBucket, jobid);
        }
    }

    /* access modifiers changed from: package-private */
    public void noteJobsDeferred(int uid, int numDeferred, long sinceLast) {
        synchronized (this.mStats) {
            this.mStats.noteJobsDeferredLocked(uid, numDeferred, sinceLast);
        }
    }

    public void noteWakupAlarm(String name, int uid, WorkSource workSource, String tag) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWakupAlarmLocked(name, uid, workSource, tag);
        }
    }

    public void noteAlarmStart(String name, WorkSource workSource, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteAlarmStartLocked(name, workSource, uid);
        }
    }

    public void noteAlarmFinish(String name, WorkSource workSource, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteAlarmFinishLocked(name, workSource, uid);
        }
    }

    public void noteStartWakelock(int uid, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStartWakeLocked(uid, pid, (WorkSource.WorkChain) null, name, historyName, type, unimportantForLogging, SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        }
    }

    public void noteStopWakelock(int uid, int pid, String name, String historyName, int type) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStopWakeLocked(uid, pid, (WorkSource.WorkChain) null, name, historyName, type, SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        }
    }

    public void noteStartWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStartWakeFromSourceLocked(ws, pid, name, historyName, type, unimportantForLogging);
        }
    }

    public void noteChangeWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteChangeWakelockFromSourceLocked(ws, pid, name, historyName, type, newWs, newPid, newName, newHistoryName, newType, newUnimportantForLogging);
        }
    }

    public void noteStopWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStopWakeFromSourceLocked(ws, pid, name, historyName, type);
        }
    }

    public void noteLongPartialWakelockStart(String name, String historyName, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteLongPartialWakelockStart(name, historyName, uid);
        }
    }

    public void noteLongPartialWakelockStartFromSource(String name, String historyName, WorkSource workSource) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteLongPartialWakelockStartFromSource(name, historyName, workSource);
        }
    }

    public void noteLongPartialWakelockFinish(String name, String historyName, int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteLongPartialWakelockFinish(name, historyName, uid);
        }
    }

    public void noteLongPartialWakelockFinishFromSource(String name, String historyName, WorkSource workSource) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteLongPartialWakelockFinishFromSource(name, historyName, workSource);
        }
    }

    public void noteStartSensor(int uid, int sensor) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStartSensorLocked(uid, sensor);
            StatsLog.write_non_chained(5, uid, null, sensor, 1);
        }
    }

    public void noteStopSensor(int uid, int sensor) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteStopSensorLocked(uid, sensor);
            StatsLog.write_non_chained(5, uid, null, sensor, 0);
        }
    }

    public void noteVibratorOn(int uid, long durationMillis) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteVibratorOnLocked(uid, durationMillis);
        }
        HwServiceFactory.reportVibratorToIAware(uid);
    }

    public void noteVibratorOff(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteVibratorOffLocked(uid);
        }
    }

    public void noteGpsChanged(WorkSource oldWs, WorkSource newWs) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteGpsChangedLocked(oldWs, newWs);
        }
    }

    public void noteGpsSignalQuality(int signalLevel) {
        synchronized (this.mStats) {
            this.mStats.noteGpsSignalQualityLocked(signalLevel);
        }
    }

    public void noteScreenState(int state) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            StatsLog.write(29, state);
            this.mStats.noteScreenStateLocked(state);
        }
    }

    public void noteScreenBrightness(int brightness) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            StatsLog.write(9, brightness);
            this.mStats.noteScreenBrightnessLocked(brightness);
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
        boolean update;
        enforceCallingPermission();
        synchronized (this.mStats) {
            update = this.mStats.noteMobileRadioPowerStateLocked(powerState, timestampNs, uid);
        }
        if (update) {
            this.mWorker.scheduleSync("modem-data", 4);
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
    }

    public void notePhoneDataConnectionState(int dataType, boolean hasData) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.notePhoneDataConnectionStateLocked(dataType, hasData);
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
        StatsLog.write(HdmiCecKeycode.CEC_KEYCODE_F1_BLUE, 1);
    }

    public void noteWifiOff() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiOffLocked();
        }
        StatsLog.write(HdmiCecKeycode.CEC_KEYCODE_F1_BLUE, 0);
    }

    public void noteStartAudio(int uid) {
        enforceSelfOrCallingPermission(uid);
        synchronized (this.mStats) {
            this.mStats.noteAudioOnLocked(uid);
            StatsLog.write_non_chained(23, uid, null, 1);
        }
    }

    public void noteStopAudio(int uid) {
        Slog.d(TAG, "begin noteStopAudio");
        enforceSelfOrCallingPermission(uid);
        synchronized (this.mStats) {
            this.mStats.noteAudioOffLocked(uid);
            StatsLog.write_non_chained(23, uid, null, 0);
        }
        Slog.d(TAG, "end noteStopAudio");
    }

    public void noteStartVideo(int uid) {
        LogPower.push(136, Integer.toString(Binder.getCallingUid()));
        enforceSelfOrCallingPermission(uid);
        synchronized (this.mStats) {
            this.mStats.noteVideoOnLocked(uid);
            StatsLog.write_non_chained(24, uid, null, 1);
        }
    }

    public void noteStopVideo(int uid) {
        LogPower.push(137, Integer.toString(Binder.getCallingUid()));
        enforceSelfOrCallingPermission(uid);
        synchronized (this.mStats) {
            this.mStats.noteVideoOffLocked(uid);
            StatsLog.write_non_chained(24, uid, null, 0);
        }
    }

    public void noteResetAudio() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetAudioLocked();
            StatsLog.write_non_chained(23, -1, null, 2);
        }
    }

    public void noteResetVideo() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetVideoLocked();
            StatsLog.write_non_chained(24, -1, null, 2);
        }
    }

    public void noteFlashlightOn(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFlashlightOnLocked(uid);
            StatsLog.write_non_chained(26, uid, null, 1);
        }
    }

    public void noteFlashlightOff(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFlashlightOffLocked(uid);
            StatsLog.write_non_chained(26, uid, null, 0);
        }
    }

    public void noteStartCamera(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteCameraOnLocked(uid);
            StatsLog.write_non_chained(25, uid, null, 1);
        }
        this.mActivityManagerService.reportCamera(uid, 1);
        LogPower.push(204, (String) null, String.valueOf(uid), String.valueOf(1));
    }

    public void noteStopCamera(int uid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteCameraOffLocked(uid);
            StatsLog.write_non_chained(25, uid, null, 0);
        }
        this.mActivityManagerService.reportCamera(uid, 0);
        LogPower.push(205, (String) null, String.valueOf(uid), String.valueOf(1));
    }

    public void noteResetCamera() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetCameraLocked();
            StatsLog.write_non_chained(25, -1, null, 2);
        }
    }

    public void noteResetFlashlight() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetFlashlightLocked();
            StatsLog.write_non_chained(26, -1, null, 2);
        }
    }

    public void noteWifiRadioPowerState(int powerState, long tsNanos, int uid) {
        String type;
        enforceCallingPermission();
        synchronized (this.mStats) {
            if (this.mStats.isOnBattery()) {
                if (powerState != 3) {
                    if (powerState != 2) {
                        type = "inactive";
                        BatteryExternalStatsWorker batteryExternalStatsWorker = this.mWorker;
                        batteryExternalStatsWorker.scheduleSync("wifi-data: " + type, 2);
                    }
                }
                type = "active";
                BatteryExternalStatsWorker batteryExternalStatsWorker2 = this.mWorker;
                batteryExternalStatsWorker2.scheduleSync("wifi-data: " + type, 2);
            }
            this.mStats.noteWifiRadioPowerState(powerState, tsNanos, uid);
        }
    }

    public void noteWifiRunning(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiRunningLocked(ws);
        }
        StatsLog.write(HdmiCecKeycode.CEC_KEYCODE_F2_RED, ws, 1);
    }

    public void noteWifiRunningChanged(WorkSource oldWs, WorkSource newWs) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiRunningChangedLocked(oldWs, newWs);
        }
        StatsLog.write(HdmiCecKeycode.CEC_KEYCODE_F2_RED, newWs, 1);
        StatsLog.write(HdmiCecKeycode.CEC_KEYCODE_F2_RED, oldWs, 0);
    }

    public void noteWifiStopped(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiStoppedLocked(ws);
        }
        StatsLog.write(HdmiCecKeycode.CEC_KEYCODE_F2_RED, ws, 0);
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

    public void noteFullWifiLockAcquiredFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFullWifiLockAcquiredFromSourceLocked(ws);
        }
    }

    public void noteFullWifiLockReleasedFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteFullWifiLockReleasedFromSourceLocked(ws);
        }
    }

    public void noteWifiScanStartedFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            int wsSize = ws.size();
            for (int i = 0; i < wsSize; i++) {
                HwLog.dubaie("DUBAI_TAG_WIFI_SCAN_STARTED", "uid=" + ws.get(i));
            }
            this.mStats.noteWifiScanStartedFromSourceLocked(ws);
        }
    }

    public void noteWifiScanStoppedFromSource(WorkSource ws) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteWifiScanStoppedFromSourceLocked(ws);
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

    public void noteNetworkInterfaceType(String iface, int networkType) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteNetworkInterfaceTypeLocked(iface, networkType);
        }
    }

    public void noteNetworkStatsEnabled() {
        enforceCallingPermission();
        this.mWorker.scheduleSync("network-stats-enabled", 6);
    }

    public void noteDeviceIdleMode(int mode, String activeReason, int activeUid) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteDeviceIdleModeLocked(mode, activeReason, activeUid);
        }
    }

    public void notePackageInstalled(String pkgName, long versionCode) {
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

    public void noteBleScanStarted(WorkSource ws, boolean isUnoptimized) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteBluetoothScanStartedFromSourceLocked(ws, isUnoptimized);
        }
    }

    public void noteBleScanStopped(WorkSource ws, boolean isUnoptimized) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteBluetoothScanStoppedFromSourceLocked(ws, isUnoptimized);
        }
    }

    public void noteResetBleScan() {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteResetBluetoothScanLocked();
        }
    }

    public void noteBleScanResults(WorkSource ws, int numNewResults) {
        enforceCallingPermission();
        synchronized (this.mStats) {
            this.mStats.noteBluetoothScanResultsFromSourceLocked(ws, numNewResults);
        }
    }

    public void noteWifiControllerActivity(WifiActivityEnergyInfo info) {
        enforceCallingPermission();
        if (info == null || !info.isValid()) {
            Slog.e(TAG, "invalid wifi data given: " + info);
            return;
        }
        this.mStats.updateWifiState(info);
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
        this.mStats.updateMobileRadioState(info);
    }

    public boolean isOnBattery() {
        return this.mStats.isOnBattery();
    }

    public void setHwChargeTimeRemaining(long time) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        synchronized (this) {
            this.mStats.setHwChargeTimeRemainingLocked(time);
        }
    }

    public void setBatteryState(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        enforceCallingPermission();
        this.mWorker.scheduleRunnable(new Runnable(plugType, status, health, level, temp, volt, chargeUAh, chargeFullUAh) {
            /* class com.android.server.am.$$Lambda$BatteryStatsService$ZxbqtJ7ozYmzYFkkNV3m_QRd0Sk */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;
            private final /* synthetic */ int f$5;
            private final /* synthetic */ int f$6;
            private final /* synthetic */ int f$7;
            private final /* synthetic */ int f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BatteryStatsService.this.lambda$setBatteryState$1$BatteryStatsService(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    public /* synthetic */ void lambda$setBatteryState$1$BatteryStatsService(int plugType, int status, int health, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        synchronized (this.mStats) {
            if (this.mStats.isOnBattery() == BatteryStatsImpl.isOnBattery(plugType, status)) {
                this.mStats.setBatteryStateLocked(status, health, plugType, level, temp, volt, chargeUAh, chargeFullUAh);
                return;
            }
            this.mWorker.scheduleSync("battery-state", 31);
            this.mWorker.scheduleRunnable(new Runnable(status, health, plugType, level, temp, volt, chargeUAh, chargeFullUAh) {
                /* class com.android.server.am.$$Lambda$BatteryStatsService$rRONgIFHr4sujxPESRmo9P5RJ6w */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;
                private final /* synthetic */ int f$4;
                private final /* synthetic */ int f$5;
                private final /* synthetic */ int f$6;
                private final /* synthetic */ int f$7;
                private final /* synthetic */ int f$8;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                    this.f$7 = r8;
                    this.f$8 = r9;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BatteryStatsService.this.lambda$setBatteryState$0$BatteryStatsService(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
                }
            });
        }
    }

    public /* synthetic */ void lambda$setBatteryState$0$BatteryStatsService(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        synchronized (this.mStats) {
            this.mStats.setBatteryStateLocked(status, health, plugType, level, temp, volt, chargeUAh, chargeFullUAh);
        }
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

    private void enforceSelfOrCallingPermission(int uid) {
        if (Binder.getCallingUid() != uid) {
            enforceCallingPermission();
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

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Process.setThreadPriority(-2);
            this.mDecoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?");
            this.mUtf8Buffer = ByteBuffer.allocateDirect(512);
            this.mUtf16Buffer = CharBuffer.allocate(512);
            while (true) {
                try {
                    String reason = waitWakeup();
                    if (reason != null) {
                        synchronized (BatteryStatsService.this.mStats) {
                            BatteryStatsService.this.mStats.noteWakeupReasonLocked(reason);
                            HwServiceFactory.reportSysWakeUp(reason);
                        }
                    } else {
                        return;
                    }
                } catch (RuntimeException e) {
                    Slog.e(BatteryStatsService.TAG, "Failure reading wakeup reasons", e);
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
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            this.mUtf8Buffer.limit(bytesWritten);
            this.mDecoder.decode(this.mUtf8Buffer, this.mUtf16Buffer, true);
            this.mUtf16Buffer.flip();
            return this.mUtf16Buffer.toString();
        }
    }

    private void dumpHelp(PrintWriter pw) {
        pw.println("Battery stats (batterystats) dump options:");
        pw.println("  [--checkin] [--proto] [--history] [--history-start] [--charged] [-c]");
        pw.println("  [--daily] [--reset] [--write] [--new-daily] [--read-daily] [-h] [<package.name>]");
        pw.println("  --checkin: generate output for a checkin report; will write (and clear) the");
        pw.println("             last old completed stats when they had been reset.");
        pw.println("  -c: write the current stats in checkin format.");
        pw.println("  --proto: write the current aggregate stats (without history) in proto format.");
        pw.println("  --history: show only history data.");
        pw.println("  --history-start <num>: show only history data starting at given time offset.");
        pw.println("  --history-create-events <num>: create <num> of battery history events.");
        pw.println("  --charged: only output data since last charged.");
        pw.println("  --daily: only output full daily data.");
        pw.println("  --reset: reset the stats, clearing all current data.");
        pw.println("  --write: force write current collected stats to disk.");
        pw.println("  --new-daily: immediately create and write new daily stats record.");
        pw.println("  --read-daily: read-load last written daily stats.");
        pw.println("  --settings: dump the settings key/values related to batterystats");
        pw.println("  --cpu: dump cpu stats for debugging purpose");
        pw.println("  <package.name>: optional name of package to filter output by.");
        pw.println("  -h: print this help text.");
        pw.println("Battery stats (batterystats) commands:");
        pw.println("  enable|disable <option>");
        pw.println("    Enable or disable a running option.  Option state is not saved across boots.");
        pw.println("    Options are:");
        pw.println("      full-history: include additional detailed events in battery history:");
        pw.println("          wake_lock_in, alarms and proc events");
        pw.println("      no-auto-reset: don't automatically reset stats when unplugged");
        pw.println("      pretend-screen-off: pretend the screen is off, even if screen state changes");
    }

    private void dumpSettings(PrintWriter pw) {
        synchronized (this.mStats) {
            this.mStats.dumpConstantsLocked(pw);
        }
    }

    private void dumpCpuStats(PrintWriter pw) {
        synchronized (this.mStats) {
            this.mStats.dumpCpuStatsLocked(pw);
        }
    }

    private int doEnableOrDisable(PrintWriter pw, int i, String[] args, boolean enable) {
        int i2 = i + 1;
        if (i2 >= args.length) {
            StringBuilder sb = new StringBuilder();
            sb.append("Missing option argument for ");
            sb.append(enable ? "--enable" : "--disable");
            pw.println(sb.toString());
            dumpHelp(pw);
            return -1;
        }
        if ("full-wake-history".equals(args[i2]) || "full-history".equals(args[i2])) {
            synchronized (this.mStats) {
                this.mStats.setRecordAllHistoryLocked(enable);
            }
        } else if ("no-auto-reset".equals(args[i2])) {
            synchronized (this.mStats) {
                this.mStats.setNoAutoReset(enable);
            }
        } else if ("pretend-screen-off".equals(args[i2])) {
            synchronized (this.mStats) {
                this.mStats.setPretendScreenOff(enable);
            }
        } else {
            pw.println("Unknown enable/disable option: " + args[i2]);
            dumpHelp(pw);
            return -1;
        }
        return i2;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x03f4 A[SYNTHETIC, Splitter:B:220:0x03f4] */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        boolean useCheckinFormat;
        long historyStart;
        boolean toProto;
        int reqUid;
        boolean writeData;
        boolean isRealCheckin;
        boolean writeData2;
        int flags;
        int flags2;
        BatteryStatsImpl batteryStatsImpl;
        BatteryStatsImpl batteryStatsImpl2;
        BatteryStatsImpl batteryStatsImpl3;
        AtomicFile atomicFile;
        Exception e;
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            long historyStart2 = -1;
            if (args != null) {
                int i = 0;
                reqUid = -1;
                boolean writeData3 = false;
                boolean noOutput = false;
                boolean isRealCheckin2 = false;
                boolean toProto2 = false;
                boolean useCheckinFormat2 = false;
                flags = 0;
                while (i < args.length) {
                    String arg = args[i];
                    if ("--checkin".equals(arg)) {
                        useCheckinFormat2 = true;
                        isRealCheckin2 = true;
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
                        historyStart2 = ParseUtils.parseLong(args[i], 0);
                        writeData3 = true;
                    } else if ("--history-create-events".equals(arg)) {
                        int i2 = i + 1;
                        if (i2 >= args.length) {
                            pw.println("Missing events argument for --history-create-events");
                            dumpHelp(pw);
                            return;
                        }
                        long events = ParseUtils.parseLong(args[i2], 0);
                        synchronized (this.mStats) {
                            this.mStats.createFakeHistoryEvents(events);
                            pw.println("Battery history create events started.");
                            noOutput = true;
                        }
                        i = i2;
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("-c".equals(arg)) {
                        useCheckinFormat2 = true;
                        flags |= 16;
                        toProto2 = toProto2;
                    } else if (PriorityDump.PROTO_ARG.equals(arg)) {
                        toProto2 = true;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("--charged".equals(arg)) {
                        flags |= 2;
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("--daily".equals(arg)) {
                        flags |= 4;
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("--reset".equals(arg)) {
                        synchronized (this.mStats) {
                            this.mStats.resetAllStatsCmdLocked();
                            pw.println("Battery stats reset.");
                            noOutput = true;
                        }
                        this.mWorker.scheduleSync("dump", 31);
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("--write".equals(arg)) {
                        syncStats("dump", 31);
                        synchronized (this.mStats) {
                            this.mStats.writeSyncLocked();
                            pw.println("Battery stats written.");
                            noOutput = true;
                        }
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("--new-daily".equals(arg)) {
                        synchronized (this.mStats) {
                            this.mStats.recordDailyStatsLocked();
                            pw.println("New daily stats written.");
                            noOutput = true;
                        }
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("--read-daily".equals(arg)) {
                        synchronized (this.mStats) {
                            this.mStats.readDailyStatsLocked();
                            pw.println("Last daily stats read.");
                            noOutput = true;
                        }
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if ("--enable".equals(arg) || "enable".equals(arg)) {
                        int i3 = doEnableOrDisable(pw, i, args, true);
                        if (i3 >= 0) {
                            pw.println("Enabled: " + args[i3]);
                            return;
                        }
                        return;
                    } else if ("--disable".equals(arg) || "disable".equals(arg)) {
                        int i4 = doEnableOrDisable(pw, i, args, false);
                        if (i4 >= 0) {
                            pw.println("Disabled: " + args[i4]);
                            return;
                        }
                        return;
                    } else if ("-h".equals(arg)) {
                        dumpHelp(pw);
                        return;
                    } else if ("--settings".equals(arg)) {
                        dumpSettings(pw);
                        return;
                    } else if ("--cpu".equals(arg)) {
                        dumpCpuStats(pw);
                        return;
                    } else if ("-a".equals(arg)) {
                        flags |= 32;
                        toProto2 = toProto2;
                        useCheckinFormat2 = useCheckinFormat2;
                    } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                        try {
                            reqUid = this.mContext.getPackageManager().getPackageUidAsUser(arg, UserHandle.getCallingUserId());
                            toProto2 = toProto2;
                            useCheckinFormat2 = useCheckinFormat2;
                        } catch (PackageManager.NameNotFoundException e2) {
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
                useCheckinFormat = useCheckinFormat2;
                toProto = toProto2;
                isRealCheckin = isRealCheckin2;
                historyStart = historyStart2;
                writeData = writeData3;
                writeData2 = noOutput;
            } else {
                useCheckinFormat = false;
                toProto = false;
                isRealCheckin = false;
                writeData = false;
                historyStart = -1;
                reqUid = -1;
                flags = 0;
                writeData2 = false;
            }
            if (!writeData2) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (BatteryStatsHelper.checkWifiOnly(this.mContext)) {
                        flags |= 64;
                    }
                    syncStats("dump", 31);
                    if (reqUid < 0 || (flags & 10) != 0) {
                        flags2 = flags;
                    } else {
                        flags2 = (flags | 2) & -17;
                    }
                    if (toProto) {
                        List<ApplicationInfo> apps = this.mContext.getPackageManager().getInstalledApplications(4325376);
                        if (isRealCheckin) {
                            synchronized (this.mStats.mCheckinFile) {
                                if (this.mStats.mCheckinFile.exists()) {
                                    try {
                                        byte[] raw = this.mStats.mCheckinFile.readFully();
                                        if (raw != null) {
                                            Parcel in = Parcel.obtain();
                                            in.unmarshall(raw, 0, raw.length);
                                            in.setDataPosition(0);
                                            BatteryStatsImpl checkinStats = new BatteryStatsImpl((File) null, this.mStats.mHandler, (BatteryStatsImpl.PlatformIdleStateCallback) null, (BatteryStatsImpl.RailEnergyDataCallback) null, this.mUserManagerUserInfoProvider);
                                            checkinStats.readSummaryFromParcel(in);
                                            in.recycle();
                                            checkinStats.dumpProtoLocked(this.mContext, fd, apps, flags2, historyStart);
                                            this.mStats.mCheckinFile.delete();
                                            return;
                                        }
                                    } catch (ParcelFormatException | IOException e3) {
                                        Slog.w(TAG, "Failure reading checkin file " + this.mStats.mCheckinFile.getBaseFile(), e3);
                                    }
                                }
                            }
                        }
                        synchronized (this.mStats) {
                            this.mStats.dumpProtoLocked(this.mContext, fd, apps, flags2, historyStart);
                            if (writeData) {
                                this.mStats.writeAsyncLocked();
                            }
                        }
                    } else if (useCheckinFormat) {
                        List<ApplicationInfo> apps2 = this.mContext.getPackageManager().getInstalledApplications(4325376);
                        if (isRealCheckin) {
                            AtomicFile atomicFile2 = this.mStats.mCheckinFile;
                            synchronized (atomicFile2) {
                                try {
                                    if (this.mStats.mCheckinFile.exists()) {
                                        try {
                                            byte[] raw2 = this.mStats.mCheckinFile.readFully();
                                            if (raw2 != null) {
                                                Parcel in2 = Parcel.obtain();
                                                in2.unmarshall(raw2, 0, raw2.length);
                                                in2.setDataPosition(0);
                                                BatteryStatsImpl checkinStats2 = new BatteryStatsImpl((File) null, this.mStats.mHandler, (BatteryStatsImpl.PlatformIdleStateCallback) null, (BatteryStatsImpl.RailEnergyDataCallback) null, this.mUserManagerUserInfoProvider);
                                                checkinStats2.readSummaryFromParcel(in2);
                                                in2.recycle();
                                                batteryStatsImpl3 = checkinStats2;
                                                atomicFile = atomicFile2;
                                                try {
                                                    checkinStats2.dumpCheckinLocked(this.mContext, pw, apps2, flags2, historyStart);
                                                    this.mStats.mCheckinFile.delete();
                                                    return;
                                                } catch (ParcelFormatException | IOException e4) {
                                                    e = e4;
                                                    Slog.w(TAG, "Failure reading checkin file " + this.mStats.mCheckinFile.getBaseFile(), e);
                                                    batteryStatsImpl2 = this.mStats;
                                                    synchronized (batteryStatsImpl2) {
                                                    }
                                                } catch (Throwable th) {
                                                    th = th;
                                                    throw th;
                                                }
                                            } else {
                                                atomicFile = atomicFile2;
                                            }
                                        } catch (ParcelFormatException | IOException e5) {
                                            e = e5;
                                            atomicFile = atomicFile2;
                                            Slog.w(TAG, "Failure reading checkin file " + this.mStats.mCheckinFile.getBaseFile(), e);
                                            batteryStatsImpl2 = this.mStats;
                                            synchronized (batteryStatsImpl2) {
                                            }
                                        }
                                    } else {
                                        atomicFile = atomicFile2;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    atomicFile = atomicFile2;
                                    throw th;
                                }
                            }
                        }
                        batteryStatsImpl2 = this.mStats;
                        synchronized (batteryStatsImpl2) {
                            try {
                                batteryStatsImpl3 = batteryStatsImpl2;
                                this.mStats.dumpCheckinLocked(this.mContext, pw, apps2, flags2, historyStart);
                                if (writeData) {
                                    this.mStats.writeAsyncLocked();
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        }
                    } else {
                        BatteryStatsImpl batteryStatsImpl4 = this.mStats;
                        synchronized (batteryStatsImpl4) {
                            try {
                                batteryStatsImpl = batteryStatsImpl4;
                                this.mStats.dumpLocked(this.mContext, pw, flags2, reqUid, historyStart);
                                if (writeData) {
                                    this.mStats.writeAsyncLocked();
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    public CellularBatteryStats getCellularBatteryStats() {
        CellularBatteryStats cellularBatteryStats;
        synchronized (this.mStats) {
            cellularBatteryStats = this.mStats.getCellularBatteryStats();
        }
        return cellularBatteryStats;
    }

    public WifiBatteryStats getWifiBatteryStats() {
        WifiBatteryStats wifiBatteryStats;
        synchronized (this.mStats) {
            wifiBatteryStats = this.mStats.getWifiBatteryStats();
        }
        return wifiBatteryStats;
    }

    public GpsBatteryStats getGpsBatteryStats() {
        GpsBatteryStats gpsBatteryStats;
        synchronized (this.mStats) {
            gpsBatteryStats = this.mStats.getGpsBatteryStats();
        }
        return gpsBatteryStats;
    }

    public HealthStatsParceler takeUidSnapshot(int requestUid) {
        HealthStatsParceler healthStatsForUidLocked;
        if (requestUid != Binder.getCallingUid()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            if (shouldCollectExternalStats()) {
                syncStats("get-health-stats-for-uids", 31);
            }
            synchronized (this.mStats) {
                healthStatsForUidLocked = getHealthStatsForUidLocked(requestUid);
            }
            Binder.restoreCallingIdentity(ident);
            return healthStatsForUidLocked;
        } catch (Exception ex) {
            Slog.w(TAG, "Crashed while writing for takeUidSnapshot(" + requestUid + ")", ex);
            throw ex;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public HealthStatsParceler[] takeUidSnapshots(int[] requestUids) {
        HealthStatsParceler[] results;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.BATTERYSTATS_TAKEUIDSNAPSHOTS);
        if (!onlyCaller(requestUids)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.BATTERY_STATS", null);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            if (shouldCollectExternalStats()) {
                syncStats("get-health-stats-for-uids", 31);
            }
            synchronized (this.mStats) {
                int N = requestUids.length;
                results = new HealthStatsParceler[N];
                for (int i = 0; i < N; i++) {
                    results[i] = getHealthStatsForUidLocked(requestUids[i]);
                }
            }
            Binder.restoreCallingIdentity(ident);
            return results;
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    private boolean shouldCollectExternalStats() {
        return SystemClock.elapsedRealtime() - this.mWorker.getLastCollectionTimeStamp() > this.mStats.getExternalStatsCollectionRateLimitMs();
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

    /* access modifiers changed from: package-private */
    public HealthStatsParceler getHealthStatsForUidLocked(int requestUid) {
        HealthStatsBatteryStatsWriter writer = new HealthStatsBatteryStatsWriter();
        HealthStatsWriter uidWriter = new HealthStatsWriter(UidHealthStats.CONSTANTS);
        BatteryStats.Uid uid = (BatteryStats.Uid) this.mStats.getUidStats().get(requestUid);
        if (uid != null) {
            writer.writeUid(uidWriter, this.mStats, uid);
        }
        return new HealthStatsParceler(uidWriter);
    }

    /* access modifiers changed from: protected */
    public void setActivityService(ActivityManagerService ams) {
        this.mActivityManagerService = ams;
    }

    public boolean setChargingStateUpdateDelayMillis(int delayMillis) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.POWER_SAVER", null);
        long ident = Binder.clearCallingIdentity();
        try {
            return Settings.Global.putLong(this.mContext.getContentResolver(), "battery_charging_state_update_delay", (long) delayMillis);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
}
