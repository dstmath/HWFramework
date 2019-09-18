package com.android.internal.os;

import android.app.ActivityManager;
import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.UidTraffic;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkStats;
import android.net.Uri;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryStats;
import android.os.Build;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBatteryPropertiesRegistrar;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.os.connectivity.CellularBatteryStats;
import android.os.connectivity.GpsBatteryStats;
import android.os.connectivity.WifiBatteryStats;
import android.provider.Settings;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.HwLog;
import android.util.IntArray;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.LogWriter;
import android.util.LongSparseArray;
import android.util.LongSparseLongArray;
import android.util.MutableInt;
import android.util.Pools;
import android.util.Printer;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.EventLogTags;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.os.KernelUidCpuActiveTimeReader;
import com.android.internal.os.KernelUidCpuClusterTimeReader;
import com.android.internal.os.KernelUidCpuFreqTimeReader;
import com.android.internal.os.KernelUidCpuTimeReader;
import com.android.internal.os.KernelWakelockStats;
import com.android.internal.os.RpmStats;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncService;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.Protocol;
import com.android.internal.util.XmlUtils;
import com.huawei.pgmng.log.LogPower;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class BatteryStatsImpl extends BatteryStats {
    static final int BATTERY_DELTA_LEVEL_FLAG = 1;
    public static final int BATTERY_PLUGGED_NONE = 0;
    public static final Parcelable.Creator<BatteryStatsImpl> CREATOR = new Parcelable.Creator<BatteryStatsImpl>() {
        public BatteryStatsImpl createFromParcel(Parcel in) {
            return new BatteryStatsImpl(in);
        }

        public BatteryStatsImpl[] newArray(int size) {
            return new BatteryStatsImpl[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final boolean DEBUG_ENERGY = false;
    private static final boolean DEBUG_ENERGY_CPU = false;
    private static final boolean DEBUG_HISTORY = false;
    private static final boolean DEBUG_MEMORY = false;
    static final long DELAY_UPDATE_WAKELOCKS = 5000;
    static final int DELTA_BATTERY_CHARGE_FLAG = 16777216;
    static final int DELTA_BATTERY_LEVEL_FLAG = 524288;
    static final int DELTA_EVENT_FLAG = 8388608;
    static final int DELTA_STATE2_FLAG = 2097152;
    static final int DELTA_STATE_FLAG = 1048576;
    static final int DELTA_STATE_MASK = -33554432;
    static final int DELTA_TIME_ABS = 524285;
    static final int DELTA_TIME_INT = 524286;
    static final int DELTA_TIME_LONG = 524287;
    static final int DELTA_TIME_MASK = 524287;
    static final int DELTA_WAKELOCK_FLAG = 4194304;
    private static final int MAGIC = -1166707595;
    static final int MAX_DAILY_ITEMS = 10;
    static final int MAX_HISTORY_BUFFER;
    private static final int MAX_HISTORY_ITEMS;
    static final int MAX_LEVEL_STEPS = 200;
    static final int MAX_MAX_HISTORY_BUFFER;
    private static final int MAX_MAX_HISTORY_ITEMS;
    /* access modifiers changed from: private */
    public static final int MAX_WAKELOCKS_PER_UID;
    private static final int MAX_WAKERLOCKS_WEIXIN = 60;
    static final int MSG_REPORT_CHARGING = 3;
    static final int MSG_REPORT_CPU_UPDATE_NEEDED = 1;
    static final int MSG_REPORT_POWER_CHANGE = 2;
    static final int MSG_REPORT_RESET_STATS = 4;
    private static final int NUM_BT_TX_LEVELS = 1;
    private static final int NUM_WIFI_TX_LEVELS = 1;
    private static final int READ_KERNEL_WAKELOCK_STATS_MAX_TIMEOUT = 500;
    private static final long RPM_STATS_UPDATE_FREQ_MS = 1000;
    static final int STATE_BATTERY_HEALTH_MASK = 7;
    static final int STATE_BATTERY_HEALTH_SHIFT = 26;
    static final int STATE_BATTERY_MASK = -16777216;
    static final int STATE_BATTERY_PLUG_MASK = 3;
    static final int STATE_BATTERY_PLUG_SHIFT = 24;
    static final int STATE_BATTERY_STATUS_MASK = 7;
    static final int STATE_BATTERY_STATUS_SHIFT = 29;
    private static final String TAG = "BatteryStatsImpl";
    private static final int USB_DATA_CONNECTED = 2;
    private static final int USB_DATA_DISCONNECTED = 1;
    private static final int USB_DATA_UNKNOWN = 0;
    private static final boolean USE_OLD_HISTORY = false;
    private static final int VERSION = 177;
    @VisibleForTesting
    public static final int WAKE_LOCK_WEIGHT = 50;
    final BatteryStats.HistoryEventTracker mActiveEvents;
    int mActiveHistoryStates;
    int mActiveHistoryStates2;
    int mAudioOnNesting;
    StopwatchTimer mAudioOnTimer;
    final ArrayList<StopwatchTimer> mAudioTurnedOnTimers;
    ControllerActivityCounterImpl mBluetoothActivity;
    int mBluetoothScanNesting;
    final ArrayList<StopwatchTimer> mBluetoothScanOnTimers;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    protected StopwatchTimer mBluetoothScanTimer;
    /* access modifiers changed from: private */
    public BatteryCallback mCallback;
    int mCameraOnNesting;
    StopwatchTimer mCameraOnTimer;
    final ArrayList<StopwatchTimer> mCameraTurnedOnTimers;
    int mChangedStates;
    int mChangedStates2;
    final BatteryStats.LevelStepTracker mChargeStepTracker;
    boolean mCharging;
    public final AtomicFile mCheckinFile;
    protected Clocks mClocks;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public final Constants mConstants;
    private long[] mCpuFreqs;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public long mCpuTimeReadsTrackingStartTime;
    final BatteryStats.HistoryStepDetails mCurHistoryStepDetails;
    long mCurStepCpuSystemTime;
    long mCurStepCpuUserTime;
    int mCurStepMode;
    long mCurStepStatIOWaitTime;
    long mCurStepStatIdleTime;
    long mCurStepStatIrqTime;
    long mCurStepStatSoftIrqTime;
    long mCurStepStatSystemTime;
    long mCurStepStatUserTime;
    int mCurrentBatteryLevel;
    final BatteryStats.LevelStepTracker mDailyChargeStepTracker;
    final BatteryStats.LevelStepTracker mDailyDischargeStepTracker;
    public final AtomicFile mDailyFile;
    final ArrayList<BatteryStats.DailyItem> mDailyItems;
    ArrayList<BatteryStats.PackageChange> mDailyPackageChanges;
    long mDailyStartTime;
    int mDeviceIdleMode;
    StopwatchTimer mDeviceIdleModeFullTimer;
    StopwatchTimer mDeviceIdleModeLightTimer;
    boolean mDeviceIdling;
    StopwatchTimer mDeviceIdlingTimer;
    boolean mDeviceLightIdling;
    StopwatchTimer mDeviceLightIdlingTimer;
    int mDischargeAmountScreenDoze;
    int mDischargeAmountScreenDozeSinceCharge;
    int mDischargeAmountScreenOff;
    int mDischargeAmountScreenOffSinceCharge;
    int mDischargeAmountScreenOn;
    int mDischargeAmountScreenOnSinceCharge;
    private LongSamplingCounter mDischargeCounter;
    int mDischargeCurrentLevel;
    private LongSamplingCounter mDischargeDeepDozeCounter;
    private LongSamplingCounter mDischargeLightDozeCounter;
    int mDischargePlugLevel;
    private LongSamplingCounter mDischargeScreenDozeCounter;
    int mDischargeScreenDozeUnplugLevel;
    private LongSamplingCounter mDischargeScreenOffCounter;
    int mDischargeScreenOffUnplugLevel;
    int mDischargeScreenOnUnplugLevel;
    int mDischargeStartLevel;
    final BatteryStats.LevelStepTracker mDischargeStepTracker;
    int mDischargeUnplugLevel;
    boolean mDistributeWakelockCpu;
    final ArrayList<StopwatchTimer> mDrawTimers;
    String mEndPlatformVersion;
    private int mEstimatedBatteryCapacity;
    /* access modifiers changed from: private */
    public ExternalStatsSync mExternalSync;
    private final JournaledFile mFile;
    int mFlashlightOnNesting;
    StopwatchTimer mFlashlightOnTimer;
    final ArrayList<StopwatchTimer> mFlashlightTurnedOnTimers;
    final ArrayList<StopwatchTimer> mFullTimers;
    final ArrayList<StopwatchTimer> mFullWifiLockTimers;
    boolean mGlobalWifiRunning;
    StopwatchTimer mGlobalWifiRunningTimer;
    int mGpsNesting;
    int mGpsSignalQualityBin;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    protected final StopwatchTimer[] mGpsSignalQualityTimer;
    public Handler mHandler;
    boolean mHasBluetoothReporting;
    boolean mHasModemReporting;
    boolean mHasWifiReporting;
    protected boolean mHaveBatteryLevel;
    int mHighDischargeAmountSinceCharge;
    BatteryStats.HistoryItem mHistory;
    final BatteryStats.HistoryItem mHistoryAddTmp;
    long mHistoryBaseTime;
    final Parcel mHistoryBuffer;
    int mHistoryBufferLastPos;
    BatteryStats.HistoryItem mHistoryCache;
    final BatteryStats.HistoryItem mHistoryCur;
    BatteryStats.HistoryItem mHistoryEnd;
    private BatteryStats.HistoryItem mHistoryIterator;
    BatteryStats.HistoryItem mHistoryLastEnd;
    final BatteryStats.HistoryItem mHistoryLastLastWritten;
    final BatteryStats.HistoryItem mHistoryLastWritten;
    boolean mHistoryOverflow;
    final BatteryStats.HistoryItem mHistoryReadTmp;
    final HashMap<BatteryStats.HistoryTag, Integer> mHistoryTagPool;
    int mInitStepMode;
    private String mInitialAcquireWakeName;
    private int mInitialAcquireWakeUid;
    boolean mInteractive;
    StopwatchTimer mInteractiveTimer;
    boolean mIsCellularTxPowerHigh;
    final SparseIntArray mIsolatedUids;
    private boolean mIteratingHistory;
    @VisibleForTesting
    protected KernelCpuSpeedReader[] mKernelCpuSpeedReaders;
    private final KernelMemoryBandwidthStats mKernelMemoryBandwidthStats;
    private final LongSparseArray<SamplingTimer> mKernelMemoryStats;
    @VisibleForTesting
    protected KernelSingleUidTimeReader mKernelSingleUidTimeReader;
    @VisibleForTesting
    protected KernelUidCpuActiveTimeReader mKernelUidCpuActiveTimeReader;
    @VisibleForTesting
    protected KernelUidCpuClusterTimeReader mKernelUidCpuClusterTimeReader;
    @VisibleForTesting
    protected KernelUidCpuFreqTimeReader mKernelUidCpuFreqTimeReader;
    @VisibleForTesting
    protected KernelUidCpuTimeReader mKernelUidCpuTimeReader;
    private final KernelWakelockReader mKernelWakelockReader;
    private final HashMap<String, SamplingTimer> mKernelWakelockStats;
    private final BluetoothActivityInfoCache mLastBluetoothActivityInfo;
    int mLastChargeStepLevel;
    int mLastChargingStateLevel;
    int mLastDischargeStepLevel;
    long mLastHistoryElapsedRealtime;
    BatteryStats.HistoryStepDetails mLastHistoryStepDetails;
    byte mLastHistoryStepLevel;
    long mLastIdleTimeStart;
    private ModemActivityInfo mLastModemActivityInfo;
    @GuardedBy("mModemNetworkLock")
    private NetworkStats mLastModemNetworkStats;
    @VisibleForTesting
    protected ArrayList<StopwatchTimer> mLastPartialTimers;
    private long mLastRpmStatsUpdateTimeMs;
    long mLastStepCpuSystemTime;
    long mLastStepCpuUserTime;
    long mLastStepStatIOWaitTime;
    long mLastStepStatIdleTime;
    long mLastStepStatIrqTime;
    long mLastStepStatSoftIrqTime;
    long mLastStepStatSystemTime;
    long mLastStepStatUserTime;
    String mLastWakeupReason;
    long mLastWakeupUptimeMs;
    @GuardedBy("mWifiNetworkLock")
    private NetworkStats mLastWifiNetworkStats;
    long mLastWriteTime;
    private int mLoadedNumConnectivityChange;
    long mLongestFullIdleTime;
    long mLongestLightIdleTime;
    int mLowDischargeAmountSinceCharge;
    int mMaxChargeStepLevel;
    private int mMaxLearnedBatteryCapacity;
    int mMinDischargeStepLevel;
    private int mMinLearnedBatteryCapacity;
    LongSamplingCounter mMobileRadioActiveAdjustedTime;
    StopwatchTimer mMobileRadioActivePerAppTimer;
    long mMobileRadioActiveStartTime;
    StopwatchTimer mMobileRadioActiveTimer;
    LongSamplingCounter mMobileRadioActiveUnknownCount;
    LongSamplingCounter mMobileRadioActiveUnknownTime;
    int mMobileRadioPowerState;
    int mModStepMode;
    ControllerActivityCounterImpl mModemActivity;
    @GuardedBy("mModemNetworkLock")
    private String[] mModemIfaces;
    private final Object mModemNetworkLock;
    final LongSamplingCounter[] mNetworkByteActivityCounters;
    final LongSamplingCounter[] mNetworkPacketActivityCounters;
    private final NetworkStatsFactory mNetworkStatsFactory;
    private final Pools.Pool<NetworkStats> mNetworkStatsPool;
    int mNextHistoryTagIdx;
    long mNextMaxDailyDeadline;
    long mNextMinDailyDeadline;
    boolean mNoAutoReset;
    @GuardedBy("this")
    private int mNumAllUidCpuTimeReads;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public long mNumBatchedSingleUidCpuTimeReads;
    private int mNumConnectivityChange;
    int mNumHistoryItems;
    int mNumHistoryTagChars;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public long mNumSingleUidCpuTimeReads;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public int mNumUidsRemoved;
    boolean mOnBattery;
    @VisibleForTesting
    protected boolean mOnBatteryInternal;
    protected final TimeBase mOnBatteryScreenOffTimeBase;
    protected final TimeBase mOnBatteryTimeBase;
    @VisibleForTesting
    protected ArrayList<StopwatchTimer> mPartialTimers;
    @GuardedBy("this")
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    protected Queue<UidToRemove> mPendingRemovedUids;
    @GuardedBy("this")
    @VisibleForTesting
    protected final SparseIntArray mPendingUids;
    Parcel mPendingWrite;
    @GuardedBy("this")
    public boolean mPerProcStateCpuTimesAvailable;
    int mPhoneDataConnectionType;
    final StopwatchTimer[] mPhoneDataConnectionsTimer;
    boolean mPhoneOn;
    StopwatchTimer mPhoneOnTimer;
    private int mPhoneServiceState;
    private int mPhoneServiceStateRaw;
    StopwatchTimer mPhoneSignalScanningTimer;
    int mPhoneSignalStrengthBin;
    int mPhoneSignalStrengthBinRaw;
    final StopwatchTimer[] mPhoneSignalStrengthsTimer;
    private int mPhoneSimStateRaw;
    private final PlatformIdleStateCallback mPlatformIdleStateCallback;
    @VisibleForTesting
    protected PowerProfile mPowerProfile;
    boolean mPowerSaveModeEnabled;
    StopwatchTimer mPowerSaveModeEnabledTimer;
    boolean mPretendScreenOff;
    int mReadHistoryChars;
    final BatteryStats.HistoryStepDetails mReadHistoryStepDetails;
    String[] mReadHistoryStrings;
    int[] mReadHistoryUids;
    private boolean mReadOverflow;
    long mRealtime;
    long mRealtimeStart;
    public boolean mRecordAllHistory;
    protected boolean mRecordingHistory;
    private final HashMap<String, SamplingTimer> mRpmStats;
    int mScreenBrightnessBin;
    final StopwatchTimer[] mScreenBrightnessTimer;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    protected StopwatchTimer mScreenDozeTimer;
    private final HashMap<String, SamplingTimer> mScreenOffRpmStats;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    protected StopwatchTimer mScreenOnTimer;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    protected int mScreenState;
    int mSensorNesting;
    final SparseArray<ArrayList<StopwatchTimer>> mSensorTimers;
    boolean mShuttingDown;
    long mStartClockTime;
    int mStartCount;
    String mStartPlatformVersion;
    long mTempTotalCpuSystemTimeUs;
    long mTempTotalCpuUserTimeUs;
    final BatteryStats.HistoryStepDetails mTmpHistoryStepDetails;
    private final RpmStats mTmpRpmStats;
    private final KernelWakelockStats mTmpWakelockStats;
    long mTrackRunningHistoryElapsedRealtime;
    long mTrackRunningHistoryUptime;
    final SparseArray<Uid> mUidStats;
    private int mUnpluggedNumConnectivityChange;
    long mUptime;
    long mUptimeStart;
    int mUsbDataState;
    @VisibleForTesting
    protected UserInfoProvider mUserInfoProvider;
    int mVideoOnNesting;
    StopwatchTimer mVideoOnTimer;
    final ArrayList<StopwatchTimer> mVideoTurnedOnTimers;
    long[][] mWakeLockAllocationsUs;
    boolean mWakeLockImportant;
    int mWakeLockNesting;
    private final HashMap<String, SamplingTimer> mWakeupReasonStats;
    StopwatchTimer mWifiActiveTimer;
    ControllerActivityCounterImpl mWifiActivity;
    final SparseArray<ArrayList<StopwatchTimer>> mWifiBatchedScanTimers;
    int mWifiFullLockNesting;
    @GuardedBy("mWifiNetworkLock")
    private String[] mWifiIfaces;
    int mWifiMulticastNesting;
    final ArrayList<StopwatchTimer> mWifiMulticastTimers;
    StopwatchTimer mWifiMulticastWakelockTimer;
    private final Object mWifiNetworkLock;
    boolean mWifiOn;
    StopwatchTimer mWifiOnTimer;
    int mWifiRadioPowerState;
    final ArrayList<StopwatchTimer> mWifiRunningTimers;
    int mWifiScanNesting;
    final ArrayList<StopwatchTimer> mWifiScanTimers;
    int mWifiSignalStrengthBin;
    final StopwatchTimer[] mWifiSignalStrengthsTimer;
    int mWifiState;
    final StopwatchTimer[] mWifiStateTimer;
    int mWifiSupplState;
    final StopwatchTimer[] mWifiSupplStateTimer;
    final ArrayList<StopwatchTimer> mWindowTimers;
    final ReentrantLock mWriteLock;

    public static class BatchTimer extends Timer {
        boolean mInDischarge;
        long mLastAddedDuration;
        long mLastAddedTime;
        final Uid mUid;

        BatchTimer(Clocks clocks, Uid uid, int type, TimeBase timeBase, Parcel in) {
            super(clocks, type, timeBase, in);
            this.mUid = uid;
            this.mLastAddedTime = in.readLong();
            this.mLastAddedDuration = in.readLong();
            this.mInDischarge = timeBase.isRunning();
        }

        BatchTimer(Clocks clocks, Uid uid, int type, TimeBase timeBase) {
            super(clocks, type, timeBase);
            this.mUid = uid;
            this.mInDischarge = timeBase.isRunning();
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            super.writeToParcel(out, elapsedRealtimeUs);
            out.writeLong(this.mLastAddedTime);
            out.writeLong(this.mLastAddedDuration);
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            recomputeLastDuration(this.mClocks.elapsedRealtime() * 1000, false);
            this.mInDischarge = false;
            super.onTimeStopped(elapsedRealtime, baseUptime, baseRealtime);
        }

        public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
            recomputeLastDuration(elapsedRealtime, false);
            this.mInDischarge = true;
            if (this.mLastAddedTime == elapsedRealtime) {
                this.mTotalTime += this.mLastAddedDuration;
            }
            super.onTimeStarted(elapsedRealtime, baseUptime, baseRealtime);
        }

        public void logState(Printer pw, String prefix) {
            super.logState(pw, prefix);
            pw.println(prefix + "mLastAddedTime=" + this.mLastAddedTime + " mLastAddedDuration=" + this.mLastAddedDuration);
        }

        private long computeOverage(long curTime) {
            if (this.mLastAddedTime > 0) {
                return (this.mLastTime + this.mLastAddedDuration) - curTime;
            }
            return 0;
        }

        private void recomputeLastDuration(long curTime, boolean abort) {
            long overage = computeOverage(curTime);
            if (overage > 0) {
                if (this.mInDischarge) {
                    this.mTotalTime -= overage;
                }
                if (abort) {
                    this.mLastAddedTime = 0;
                    return;
                }
                this.mLastAddedTime = curTime;
                this.mLastAddedDuration -= overage;
            }
        }

        public void addDuration(BatteryStatsImpl stats, long durationMillis) {
            long now = this.mClocks.elapsedRealtime() * 1000;
            recomputeLastDuration(now, true);
            this.mLastAddedTime = now;
            this.mLastAddedDuration = 1000 * durationMillis;
            if (this.mInDischarge) {
                this.mTotalTime += this.mLastAddedDuration;
                this.mCount++;
            }
        }

        public void abortLastDuration(BatteryStatsImpl stats) {
            recomputeLastDuration(this.mClocks.elapsedRealtime() * 1000, true);
        }

        /* access modifiers changed from: protected */
        public int computeCurrentCountLocked() {
            return this.mCount;
        }

        /* access modifiers changed from: protected */
        public long computeRunTimeLocked(long curBatteryRealtime) {
            long overage = computeOverage(this.mClocks.elapsedRealtime() * 1000);
            if (overage <= 0) {
                return this.mTotalTime;
            }
            this.mTotalTime = overage;
            return overage;
        }

        public boolean reset(boolean detachIfReset) {
            long now = this.mClocks.elapsedRealtime() * 1000;
            recomputeLastDuration(now, true);
            boolean stillActive = this.mLastAddedTime == now;
            super.reset(!stillActive && detachIfReset);
            if (!stillActive) {
                return true;
            }
            return false;
        }
    }

    public interface BatteryCallback {
        void batteryNeedsCpuUpdate();

        void batteryPowerChanged(boolean z);

        void batterySendBroadcast(Intent intent);

        void batteryStatsReset();
    }

    private final class BluetoothActivityInfoCache {
        long energy;
        long idleTimeMs;
        long rxTimeMs;
        long txTimeMs;
        SparseLongArray uidRxBytes;
        SparseLongArray uidTxBytes;

        private BluetoothActivityInfoCache() {
            this.uidRxBytes = new SparseLongArray();
            this.uidTxBytes = new SparseLongArray();
        }

        /* access modifiers changed from: package-private */
        public void set(BluetoothActivityEnergyInfo info) {
            this.idleTimeMs = info.getControllerIdleTimeMillis();
            this.rxTimeMs = info.getControllerRxTimeMillis();
            this.txTimeMs = info.getControllerTxTimeMillis();
            this.energy = info.getControllerEnergyUsed();
            if (info.getUidTraffic() != null) {
                for (UidTraffic traffic : info.getUidTraffic()) {
                    this.uidRxBytes.put(traffic.getUid(), traffic.getRxBytes());
                    this.uidTxBytes.put(traffic.getUid(), traffic.getTxBytes());
                }
            }
        }
    }

    public interface Clocks {
        long elapsedRealtime();

        long uptimeMillis();
    }

    @VisibleForTesting
    public final class Constants extends ContentObserver {
        private static final long DEFAULT_BATTERY_LEVEL_COLLECTION_DELAY_MS = 300000;
        private static final long DEFAULT_EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS = 600000;
        private static final long DEFAULT_KERNEL_UID_READERS_THROTTLE_TIME = 10000;
        private static final long DEFAULT_PROC_STATE_CPU_TIMES_READ_DELAY_MS = 5000;
        private static final boolean DEFAULT_TRACK_CPU_ACTIVE_CLUSTER_TIME = true;
        private static final boolean DEFAULT_TRACK_CPU_TIMES_BY_PROC_STATE = true;
        private static final long DEFAULT_UID_REMOVE_DELAY_MS = 300000;
        public static final String KEY_BATTERY_LEVEL_COLLECTION_DELAY_MS = "battery_level_collection_delay_ms";
        public static final String KEY_EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS = "external_stats_collection_rate_limit_ms";
        public static final String KEY_KERNEL_UID_READERS_THROTTLE_TIME = "kernel_uid_readers_throttle_time";
        public static final String KEY_PROC_STATE_CPU_TIMES_READ_DELAY_MS = "proc_state_cpu_times_read_delay_ms";
        public static final String KEY_TRACK_CPU_ACTIVE_CLUSTER_TIME = "track_cpu_active_cluster_time";
        public static final String KEY_TRACK_CPU_TIMES_BY_PROC_STATE = "track_cpu_times_by_proc_state";
        public static final String KEY_UID_REMOVE_DELAY_MS = "uid_remove_delay_ms";
        public long BATTERY_LEVEL_COLLECTION_DELAY_MS = 300000;
        public long EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS = DEFAULT_EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS;
        public long KERNEL_UID_READERS_THROTTLE_TIME = DEFAULT_KERNEL_UID_READERS_THROTTLE_TIME;
        public long PROC_STATE_CPU_TIMES_READ_DELAY_MS = DEFAULT_PROC_STATE_CPU_TIMES_READ_DELAY_MS;
        public boolean TRACK_CPU_ACTIVE_CLUSTER_TIME = true;
        public boolean TRACK_CPU_TIMES_BY_PROC_STATE = true;
        public long UID_REMOVE_DELAY_MS = 300000;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
        }

        public void startObserving(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("battery_stats_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (BatteryStatsImpl.this) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mResolver, "battery_stats_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(BatteryStatsImpl.TAG, "Bad batterystats settings", e);
                }
                updateTrackCpuTimesByProcStateLocked(this.TRACK_CPU_TIMES_BY_PROC_STATE, this.mParser.getBoolean(KEY_TRACK_CPU_TIMES_BY_PROC_STATE, true));
                this.TRACK_CPU_ACTIVE_CLUSTER_TIME = this.mParser.getBoolean(KEY_TRACK_CPU_ACTIVE_CLUSTER_TIME, true);
                updateProcStateCpuTimesReadDelayMs(this.PROC_STATE_CPU_TIMES_READ_DELAY_MS, this.mParser.getLong(KEY_PROC_STATE_CPU_TIMES_READ_DELAY_MS, DEFAULT_PROC_STATE_CPU_TIMES_READ_DELAY_MS));
                updateKernelUidReadersThrottleTime(this.KERNEL_UID_READERS_THROTTLE_TIME, this.mParser.getLong(KEY_KERNEL_UID_READERS_THROTTLE_TIME, DEFAULT_KERNEL_UID_READERS_THROTTLE_TIME));
                updateUidRemoveDelay(this.mParser.getLong(KEY_UID_REMOVE_DELAY_MS, 300000));
                this.EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS = this.mParser.getLong(KEY_EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS, DEFAULT_EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS);
                this.BATTERY_LEVEL_COLLECTION_DELAY_MS = this.mParser.getLong(KEY_BATTERY_LEVEL_COLLECTION_DELAY_MS, 300000);
            }
        }

        private void updateTrackCpuTimesByProcStateLocked(boolean wasEnabled, boolean isEnabled) {
            this.TRACK_CPU_TIMES_BY_PROC_STATE = isEnabled;
            if (isEnabled && !wasEnabled) {
                if (BatteryStatsImpl.this.mKernelSingleUidTimeReader != null) {
                    BatteryStatsImpl.this.mKernelSingleUidTimeReader.markDataAsStale(true);
                }
                if (BatteryStatsImpl.this.mExternalSync != null) {
                    BatteryStatsImpl.this.mExternalSync.scheduleCpuSyncDueToSettingChange();
                }
                long unused = BatteryStatsImpl.this.mNumSingleUidCpuTimeReads = 0;
                long unused2 = BatteryStatsImpl.this.mNumBatchedSingleUidCpuTimeReads = 0;
                long unused3 = BatteryStatsImpl.this.mCpuTimeReadsTrackingStartTime = BatteryStatsImpl.this.mClocks.uptimeMillis();
            }
        }

        private void updateProcStateCpuTimesReadDelayMs(long oldDelayMillis, long newDelayMillis) {
            this.PROC_STATE_CPU_TIMES_READ_DELAY_MS = newDelayMillis;
            if (oldDelayMillis != newDelayMillis) {
                long unused = BatteryStatsImpl.this.mNumSingleUidCpuTimeReads = 0;
                long unused2 = BatteryStatsImpl.this.mNumBatchedSingleUidCpuTimeReads = 0;
                long unused3 = BatteryStatsImpl.this.mCpuTimeReadsTrackingStartTime = BatteryStatsImpl.this.mClocks.uptimeMillis();
            }
        }

        private void updateKernelUidReadersThrottleTime(long oldTimeMs, long newTimeMs) {
            this.KERNEL_UID_READERS_THROTTLE_TIME = newTimeMs;
            if (oldTimeMs != newTimeMs) {
                BatteryStatsImpl.this.mKernelUidCpuTimeReader.setThrottleInterval(this.KERNEL_UID_READERS_THROTTLE_TIME);
                BatteryStatsImpl.this.mKernelUidCpuFreqTimeReader.setThrottleInterval(this.KERNEL_UID_READERS_THROTTLE_TIME);
                BatteryStatsImpl.this.mKernelUidCpuActiveTimeReader.setThrottleInterval(this.KERNEL_UID_READERS_THROTTLE_TIME);
                BatteryStatsImpl.this.mKernelUidCpuClusterTimeReader.setThrottleInterval(this.KERNEL_UID_READERS_THROTTLE_TIME);
            }
        }

        private void updateUidRemoveDelay(long newTimeMs) {
            this.UID_REMOVE_DELAY_MS = newTimeMs;
            BatteryStatsImpl.this.clearPendingRemovedUids();
        }

        public void dumpLocked(PrintWriter pw) {
            pw.print(KEY_TRACK_CPU_TIMES_BY_PROC_STATE);
            pw.print("=");
            pw.println(this.TRACK_CPU_TIMES_BY_PROC_STATE);
            pw.print(KEY_TRACK_CPU_ACTIVE_CLUSTER_TIME);
            pw.print("=");
            pw.println(this.TRACK_CPU_ACTIVE_CLUSTER_TIME);
            pw.print(KEY_PROC_STATE_CPU_TIMES_READ_DELAY_MS);
            pw.print("=");
            pw.println(this.PROC_STATE_CPU_TIMES_READ_DELAY_MS);
            pw.print(KEY_KERNEL_UID_READERS_THROTTLE_TIME);
            pw.print("=");
            pw.println(this.KERNEL_UID_READERS_THROTTLE_TIME);
            pw.print(KEY_EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS);
            pw.print("=");
            pw.println(this.EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS);
            pw.print(KEY_BATTERY_LEVEL_COLLECTION_DELAY_MS);
            pw.print("=");
            pw.println(this.BATTERY_LEVEL_COLLECTION_DELAY_MS);
        }
    }

    public static class ControllerActivityCounterImpl extends BatteryStats.ControllerActivityCounter implements Parcelable {
        private final LongSamplingCounter mIdleTimeMillis;
        private final LongSamplingCounter mPowerDrainMaMs;
        private final LongSamplingCounter mRxTimeMillis;
        private final LongSamplingCounter mScanTimeMillis;
        private final LongSamplingCounter mSleepTimeMillis;
        private final LongSamplingCounter[] mTxTimeMillis;

        public ControllerActivityCounterImpl(TimeBase timeBase, int numTxStates) {
            this.mIdleTimeMillis = new LongSamplingCounter(timeBase);
            this.mScanTimeMillis = new LongSamplingCounter(timeBase);
            this.mSleepTimeMillis = new LongSamplingCounter(timeBase);
            this.mRxTimeMillis = new LongSamplingCounter(timeBase);
            this.mTxTimeMillis = new LongSamplingCounter[numTxStates];
            for (int i = 0; i < numTxStates; i++) {
                this.mTxTimeMillis[i] = new LongSamplingCounter(timeBase);
            }
            this.mPowerDrainMaMs = new LongSamplingCounter(timeBase);
        }

        public ControllerActivityCounterImpl(TimeBase timeBase, int numTxStates, Parcel in) {
            this.mIdleTimeMillis = new LongSamplingCounter(timeBase, in);
            this.mScanTimeMillis = new LongSamplingCounter(timeBase, in);
            this.mSleepTimeMillis = new LongSamplingCounter(timeBase, in);
            this.mRxTimeMillis = new LongSamplingCounter(timeBase, in);
            if (in.readInt() == numTxStates) {
                this.mTxTimeMillis = new LongSamplingCounter[numTxStates];
                for (int i = 0; i < numTxStates; i++) {
                    this.mTxTimeMillis[i] = new LongSamplingCounter(timeBase, in);
                }
                this.mPowerDrainMaMs = new LongSamplingCounter(timeBase, in);
                return;
            }
            throw new ParcelFormatException("inconsistent tx state lengths");
        }

        public void readSummaryFromParcel(Parcel in) {
            this.mIdleTimeMillis.readSummaryFromParcelLocked(in);
            this.mScanTimeMillis.readSummaryFromParcelLocked(in);
            this.mSleepTimeMillis.readSummaryFromParcelLocked(in);
            this.mRxTimeMillis.readSummaryFromParcelLocked(in);
            if (in.readInt() == this.mTxTimeMillis.length) {
                for (LongSamplingCounter counter : this.mTxTimeMillis) {
                    counter.readSummaryFromParcelLocked(in);
                }
                this.mPowerDrainMaMs.readSummaryFromParcelLocked(in);
                return;
            }
            throw new ParcelFormatException("inconsistent tx state lengths");
        }

        public int describeContents() {
            return 0;
        }

        public void writeSummaryToParcel(Parcel dest) {
            this.mIdleTimeMillis.writeSummaryFromParcelLocked(dest);
            this.mScanTimeMillis.writeSummaryFromParcelLocked(dest);
            this.mSleepTimeMillis.writeSummaryFromParcelLocked(dest);
            this.mRxTimeMillis.writeSummaryFromParcelLocked(dest);
            dest.writeInt(this.mTxTimeMillis.length);
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.writeSummaryFromParcelLocked(dest);
            }
            this.mPowerDrainMaMs.writeSummaryFromParcelLocked(dest);
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.mIdleTimeMillis.writeToParcel(dest);
            this.mScanTimeMillis.writeToParcel(dest);
            this.mSleepTimeMillis.writeToParcel(dest);
            this.mRxTimeMillis.writeToParcel(dest);
            dest.writeInt(this.mTxTimeMillis.length);
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.writeToParcel(dest);
            }
            this.mPowerDrainMaMs.writeToParcel(dest);
        }

        public void reset(boolean detachIfReset) {
            this.mIdleTimeMillis.reset(detachIfReset);
            this.mScanTimeMillis.reset(detachIfReset);
            this.mSleepTimeMillis.reset(detachIfReset);
            this.mRxTimeMillis.reset(detachIfReset);
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.reset(detachIfReset);
            }
            this.mPowerDrainMaMs.reset(detachIfReset);
        }

        public void detach() {
            this.mIdleTimeMillis.detach();
            this.mScanTimeMillis.detach();
            this.mSleepTimeMillis.detach();
            this.mRxTimeMillis.detach();
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.detach();
            }
            this.mPowerDrainMaMs.detach();
        }

        public LongSamplingCounter getIdleTimeCounter() {
            return this.mIdleTimeMillis;
        }

        public LongSamplingCounter getScanTimeCounter() {
            return this.mScanTimeMillis;
        }

        public LongSamplingCounter getSleepTimeCounter() {
            return this.mSleepTimeMillis;
        }

        public LongSamplingCounter getRxTimeCounter() {
            return this.mRxTimeMillis;
        }

        public LongSamplingCounter[] getTxTimeCounters() {
            return this.mTxTimeMillis;
        }

        public LongSamplingCounter getPowerCounter() {
            return this.mPowerDrainMaMs;
        }
    }

    public static class Counter extends BatteryStats.Counter implements TimeBaseObs {
        final AtomicInteger mCount = new AtomicInteger();
        int mLoadedCount;
        int mPluggedCount;
        final TimeBase mTimeBase;
        int mUnpluggedCount;

        public Counter(TimeBase timeBase, Parcel in) {
            this.mTimeBase = timeBase;
            this.mPluggedCount = in.readInt();
            this.mCount.set(this.mPluggedCount);
            this.mLoadedCount = in.readInt();
            this.mUnpluggedCount = in.readInt();
            timeBase.add(this);
        }

        public Counter(TimeBase timeBase) {
            this.mTimeBase = timeBase;
            timeBase.add(this);
        }

        public void writeToParcel(Parcel out) {
            out.writeInt(this.mCount.get());
            out.writeInt(this.mLoadedCount);
            out.writeInt(this.mUnpluggedCount);
        }

        public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mUnpluggedCount = this.mPluggedCount;
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mPluggedCount = this.mCount.get();
        }

        public static void writeCounterToParcel(Parcel out, Counter counter) {
            if (counter == null) {
                out.writeInt(0);
                return;
            }
            out.writeInt(1);
            counter.writeToParcel(out);
        }

        public static Counter readCounterFromParcel(TimeBase timeBase, Parcel in) {
            if (in.readInt() == 0) {
                return null;
            }
            return new Counter(timeBase, in);
        }

        public int getCountLocked(int which) {
            int val = this.mCount.get();
            if (which == 2) {
                return val - this.mUnpluggedCount;
            }
            if (which != 0) {
                return val - this.mLoadedCount;
            }
            return val;
        }

        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCount=" + this.mCount.get() + " mLoadedCount=" + this.mLoadedCount + " mUnpluggedCount=" + this.mUnpluggedCount + " mPluggedCount=" + this.mPluggedCount);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void stepAtomic() {
            if (this.mTimeBase.isRunning()) {
                this.mCount.incrementAndGet();
            }
        }

        /* access modifiers changed from: package-private */
        public void addAtomic(int delta) {
            if (this.mTimeBase.isRunning()) {
                this.mCount.addAndGet(delta);
            }
        }

        /* access modifiers changed from: package-private */
        public void reset(boolean detachIfReset) {
            this.mCount.set(0);
            this.mUnpluggedCount = 0;
            this.mPluggedCount = 0;
            this.mLoadedCount = 0;
            if (detachIfReset) {
                detach();
            }
        }

        /* access modifiers changed from: package-private */
        public void detach() {
            this.mTimeBase.remove(this);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void writeSummaryFromParcelLocked(Parcel out) {
            out.writeInt(this.mCount.get());
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void readSummaryFromParcelLocked(Parcel in) {
            this.mLoadedCount = in.readInt();
            this.mCount.set(this.mLoadedCount);
            int i = this.mLoadedCount;
            this.mPluggedCount = i;
            this.mUnpluggedCount = i;
        }
    }

    public static class DualTimer extends DurationTimer {
        private final DurationTimer mSubTimer;

        public DualTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase, TimeBase subTimeBase, Parcel in) {
            super(clocks, uid, type, timerPool, timeBase, in);
            DurationTimer durationTimer = new DurationTimer(clocks, uid, type, null, subTimeBase, in);
            this.mSubTimer = durationTimer;
        }

        public DualTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase, TimeBase subTimeBase) {
            super(clocks, uid, type, timerPool, timeBase);
            DurationTimer durationTimer = new DurationTimer(clocks, uid, type, null, subTimeBase);
            this.mSubTimer = durationTimer;
        }

        public DurationTimer getSubTimer() {
            return this.mSubTimer;
        }

        public void startRunningLocked(long elapsedRealtimeMs) {
            super.startRunningLocked(elapsedRealtimeMs);
            this.mSubTimer.startRunningLocked(elapsedRealtimeMs);
        }

        public void stopRunningLocked(long elapsedRealtimeMs) {
            super.stopRunningLocked(elapsedRealtimeMs);
            this.mSubTimer.stopRunningLocked(elapsedRealtimeMs);
        }

        public void stopAllRunningLocked(long elapsedRealtimeMs) {
            super.stopAllRunningLocked(elapsedRealtimeMs);
            this.mSubTimer.stopAllRunningLocked(elapsedRealtimeMs);
        }

        public boolean reset(boolean detachIfReset) {
            if (!(false | (!this.mSubTimer.reset(false))) && !(!super.reset(detachIfReset))) {
                return true;
            }
            return false;
        }

        public void detach() {
            this.mSubTimer.detach();
            super.detach();
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            super.writeToParcel(out, elapsedRealtimeUs);
            this.mSubTimer.writeToParcel(out, elapsedRealtimeUs);
        }

        public void writeSummaryFromParcelLocked(Parcel out, long elapsedRealtimeUs) {
            super.writeSummaryFromParcelLocked(out, elapsedRealtimeUs);
            this.mSubTimer.writeSummaryFromParcelLocked(out, elapsedRealtimeUs);
        }

        public void readSummaryFromParcelLocked(Parcel in) {
            super.readSummaryFromParcelLocked(in);
            this.mSubTimer.readSummaryFromParcelLocked(in);
        }
    }

    public static class DurationTimer extends StopwatchTimer {
        long mCurrentDurationMs;
        long mMaxDurationMs;
        long mStartTimeMs = -1;
        long mTotalDurationMs;

        public DurationTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase, Parcel in) {
            super(clocks, uid, type, timerPool, timeBase, in);
            this.mMaxDurationMs = in.readLong();
            this.mTotalDurationMs = in.readLong();
            this.mCurrentDurationMs = in.readLong();
        }

        public DurationTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase) {
            super(clocks, uid, type, timerPool, timeBase);
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            super.writeToParcel(out, elapsedRealtimeUs);
            out.writeLong(getMaxDurationMsLocked(elapsedRealtimeUs / 1000));
            out.writeLong(this.mTotalDurationMs);
            out.writeLong(getCurrentDurationMsLocked(elapsedRealtimeUs / 1000));
        }

        public void writeSummaryFromParcelLocked(Parcel out, long elapsedRealtimeUs) {
            super.writeSummaryFromParcelLocked(out, elapsedRealtimeUs);
            out.writeLong(getMaxDurationMsLocked(elapsedRealtimeUs / 1000));
            out.writeLong(getTotalDurationMsLocked(elapsedRealtimeUs / 1000));
        }

        public void readSummaryFromParcelLocked(Parcel in) {
            super.readSummaryFromParcelLocked(in);
            this.mMaxDurationMs = in.readLong();
            this.mTotalDurationMs = in.readLong();
            this.mStartTimeMs = -1;
            this.mCurrentDurationMs = 0;
        }

        public void onTimeStarted(long elapsedRealtimeUs, long baseUptime, long baseRealtime) {
            super.onTimeStarted(elapsedRealtimeUs, baseUptime, baseRealtime);
            if (this.mNesting > 0) {
                this.mStartTimeMs = baseRealtime / 1000;
            }
        }

        public void onTimeStopped(long elapsedRealtimeUs, long baseUptime, long baseRealtimeUs) {
            super.onTimeStopped(elapsedRealtimeUs, baseUptime, baseRealtimeUs);
            if (this.mNesting > 0) {
                this.mCurrentDurationMs += (baseRealtimeUs / 1000) - this.mStartTimeMs;
            }
            this.mStartTimeMs = -1;
        }

        public void logState(Printer pw, String prefix) {
            super.logState(pw, prefix);
        }

        public void startRunningLocked(long elapsedRealtimeMs) {
            super.startRunningLocked(elapsedRealtimeMs);
            if (this.mNesting == 1 && this.mTimeBase.isRunning()) {
                this.mStartTimeMs = this.mTimeBase.getRealtime(elapsedRealtimeMs * 1000) / 1000;
            }
        }

        public void stopRunningLocked(long elapsedRealtimeMs) {
            if (this.mNesting == 1) {
                long durationMs = getCurrentDurationMsLocked(elapsedRealtimeMs);
                this.mTotalDurationMs += durationMs;
                if (durationMs > this.mMaxDurationMs) {
                    this.mMaxDurationMs = durationMs;
                }
                this.mStartTimeMs = -1;
                this.mCurrentDurationMs = 0;
            }
            super.stopRunningLocked(elapsedRealtimeMs);
        }

        public boolean reset(boolean detachIfReset) {
            boolean result = super.reset(detachIfReset);
            this.mMaxDurationMs = 0;
            this.mTotalDurationMs = 0;
            this.mCurrentDurationMs = 0;
            if (this.mNesting > 0) {
                this.mStartTimeMs = this.mTimeBase.getRealtime(this.mClocks.elapsedRealtime() * 1000) / 1000;
            } else {
                this.mStartTimeMs = -1;
            }
            return result;
        }

        public long getMaxDurationMsLocked(long elapsedRealtimeMs) {
            if (this.mNesting > 0) {
                long durationMs = getCurrentDurationMsLocked(elapsedRealtimeMs);
                if (durationMs > this.mMaxDurationMs) {
                    return durationMs;
                }
            }
            return this.mMaxDurationMs;
        }

        public long getCurrentDurationMsLocked(long elapsedRealtimeMs) {
            long durationMs = this.mCurrentDurationMs;
            if (this.mNesting <= 0 || !this.mTimeBase.isRunning()) {
                return durationMs;
            }
            return durationMs + ((this.mTimeBase.getRealtime(elapsedRealtimeMs * 1000) / 1000) - this.mStartTimeMs);
        }

        public long getTotalDurationMsLocked(long elapsedRealtimeMs) {
            return this.mTotalDurationMs + getCurrentDurationMsLocked(elapsedRealtimeMs);
        }
    }

    public interface ExternalStatsSync {
        public static final int UPDATE_ALL = 31;
        public static final int UPDATE_BT = 8;
        public static final int UPDATE_CPU = 1;
        public static final int UPDATE_RADIO = 4;
        public static final int UPDATE_RPM = 16;
        public static final int UPDATE_WIFI = 2;

        void cancelCpuSyncDueToWakelockChange();

        Future<?> scheduleCopyFromAllUidsCpuTimes(boolean z, boolean z2);

        Future<?> scheduleCpuSyncDueToRemovedUid(int i);

        Future<?> scheduleCpuSyncDueToScreenStateChange(boolean z, boolean z2);

        Future<?> scheduleCpuSyncDueToSettingChange();

        Future<?> scheduleCpuSyncDueToWakelockChange(long j);

        Future<?> scheduleReadProcStateCpuTimes(boolean z, boolean z2, long j);

        Future<?> scheduleSync(String str, int i);

        Future<?> scheduleSyncDueToBatteryLevelChange(long j);
    }

    @VisibleForTesting
    public static class LongSamplingCounter extends BatteryStats.LongCounter implements TimeBaseObs {
        public long mCount;
        public long mCurrentCount;
        public long mLoadedCount;
        final TimeBase mTimeBase;
        public long mUnpluggedCount;

        public LongSamplingCounter(TimeBase timeBase, Parcel in) {
            this.mTimeBase = timeBase;
            this.mCount = in.readLong();
            this.mCurrentCount = in.readLong();
            this.mLoadedCount = in.readLong();
            this.mUnpluggedCount = in.readLong();
            timeBase.add(this);
        }

        public LongSamplingCounter(TimeBase timeBase) {
            this.mTimeBase = timeBase;
            timeBase.add(this);
        }

        public void writeToParcel(Parcel out) {
            out.writeLong(this.mCount);
            out.writeLong(this.mCurrentCount);
            out.writeLong(this.mLoadedCount);
            out.writeLong(this.mUnpluggedCount);
        }

        public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mUnpluggedCount = this.mCount;
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
        }

        public long getCountLocked(int which) {
            long val = this.mCount;
            if (which == 2) {
                return val - this.mUnpluggedCount;
            }
            if (which != 0) {
                return val - this.mLoadedCount;
            }
            return val;
        }

        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCount=" + this.mCount + " mCurrentCount=" + this.mCurrentCount + " mLoadedCount=" + this.mLoadedCount + " mUnpluggedCount=" + this.mUnpluggedCount);
        }

        public void addCountLocked(long count) {
            update(this.mCurrentCount + count, this.mTimeBase.isRunning());
        }

        public void addCountLocked(long count, boolean isRunning) {
            update(this.mCurrentCount + count, isRunning);
        }

        public void update(long count) {
            update(count, this.mTimeBase.isRunning());
        }

        public void update(long count, boolean isRunning) {
            if (count < this.mCurrentCount) {
                this.mCurrentCount = 0;
            }
            if (isRunning) {
                this.mCount += count - this.mCurrentCount;
            }
            this.mCurrentCount = count;
        }

        public void reset(boolean detachIfReset) {
            this.mCount = 0;
            this.mUnpluggedCount = 0;
            this.mLoadedCount = 0;
            if (detachIfReset) {
                detach();
            }
        }

        public void detach() {
            this.mTimeBase.remove(this);
        }

        public void writeSummaryFromParcelLocked(Parcel out) {
            out.writeLong(this.mCount);
        }

        public void readSummaryFromParcelLocked(Parcel in) {
            long readLong = in.readLong();
            this.mLoadedCount = readLong;
            this.mUnpluggedCount = readLong;
            this.mCount = readLong;
        }
    }

    @VisibleForTesting
    public static class LongSamplingCounterArray extends BatteryStats.LongCounterArray implements TimeBaseObs {
        public long[] mCounts;
        public long[] mLoadedCounts;
        final TimeBase mTimeBase;
        public long[] mUnpluggedCounts;

        private LongSamplingCounterArray(TimeBase timeBase, Parcel in) {
            this.mTimeBase = timeBase;
            this.mCounts = in.createLongArray();
            this.mLoadedCounts = in.createLongArray();
            this.mUnpluggedCounts = in.createLongArray();
            timeBase.add(this);
        }

        public LongSamplingCounterArray(TimeBase timeBase) {
            this.mTimeBase = timeBase;
            timeBase.add(this);
        }

        /* access modifiers changed from: private */
        public void writeToParcel(Parcel out) {
            out.writeLongArray(this.mCounts);
            out.writeLongArray(this.mLoadedCounts);
            out.writeLongArray(this.mUnpluggedCounts);
        }

        public void onTimeStarted(long elapsedRealTime, long baseUptime, long baseRealtime) {
            this.mUnpluggedCounts = copyArray(this.mCounts, this.mUnpluggedCounts);
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
        }

        public long[] getCountsLocked(int which) {
            long[] val = copyArray(this.mCounts, null);
            if (which == 2) {
                subtract(val, this.mUnpluggedCounts);
            } else if (which != 0) {
                subtract(val, this.mLoadedCounts);
            }
            return val;
        }

        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCounts=" + Arrays.toString(this.mCounts) + " mLoadedCounts=" + Arrays.toString(this.mLoadedCounts) + " mUnpluggedCounts=" + Arrays.toString(this.mUnpluggedCounts));
        }

        public void addCountLocked(long[] counts) {
            addCountLocked(counts, this.mTimeBase.isRunning());
        }

        public void addCountLocked(long[] counts, boolean isRunning) {
            if (counts != null && isRunning) {
                if (this.mCounts == null) {
                    this.mCounts = new long[counts.length];
                }
                for (int i = 0; i < counts.length; i++) {
                    long[] jArr = this.mCounts;
                    jArr[i] = jArr[i] + counts[i];
                }
            }
        }

        public int getSize() {
            if (this.mCounts == null) {
                return 0;
            }
            return this.mCounts.length;
        }

        public void reset(boolean detachIfReset) {
            fillArray(this.mCounts, 0);
            fillArray(this.mLoadedCounts, 0);
            fillArray(this.mUnpluggedCounts, 0);
            if (detachIfReset) {
                detach();
            }
        }

        public void detach() {
            this.mTimeBase.remove(this);
        }

        /* access modifiers changed from: private */
        public void writeSummaryToParcelLocked(Parcel out) {
            out.writeLongArray(this.mCounts);
        }

        /* access modifiers changed from: private */
        public void readSummaryFromParcelLocked(Parcel in) {
            this.mCounts = in.createLongArray();
            this.mLoadedCounts = copyArray(this.mCounts, this.mLoadedCounts);
            this.mUnpluggedCounts = copyArray(this.mCounts, this.mUnpluggedCounts);
        }

        public static void writeToParcel(Parcel out, LongSamplingCounterArray counterArray) {
            if (counterArray != null) {
                out.writeInt(1);
                counterArray.writeToParcel(out);
                return;
            }
            out.writeInt(0);
        }

        public static LongSamplingCounterArray readFromParcel(Parcel in, TimeBase timeBase) {
            if (in.readInt() != 0) {
                return new LongSamplingCounterArray(timeBase, in);
            }
            return null;
        }

        public static void writeSummaryToParcelLocked(Parcel out, LongSamplingCounterArray counterArray) {
            if (counterArray != null) {
                out.writeInt(1);
                counterArray.writeSummaryToParcelLocked(out);
                return;
            }
            out.writeInt(0);
        }

        public static LongSamplingCounterArray readSummaryFromParcelLocked(Parcel in, TimeBase timeBase) {
            if (in.readInt() == 0) {
                return null;
            }
            LongSamplingCounterArray counterArray = new LongSamplingCounterArray(timeBase);
            counterArray.readSummaryFromParcelLocked(in);
            return counterArray;
        }

        private static void fillArray(long[] a, long val) {
            if (a != null) {
                Arrays.fill(a, val);
            }
        }

        private static void subtract(long[] val, long[] toSubtract) {
            if (toSubtract != null) {
                for (int i = 0; i < val.length; i++) {
                    val[i] = val[i] - toSubtract[i];
                }
            }
        }

        private static long[] copyArray(long[] src, long[] dest) {
            if (src == null) {
                return null;
            }
            if (dest == null) {
                dest = new long[src.length];
            }
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
        }
    }

    final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            String action;
            BatteryCallback cb = BatteryStatsImpl.this.mCallback;
            switch (msg.what) {
                case 1:
                    if (cb != null) {
                        cb.batteryNeedsCpuUpdate();
                        return;
                    }
                    return;
                case 2:
                    if (cb != null) {
                        cb.batteryPowerChanged(msg.arg1 != 0);
                        return;
                    }
                    return;
                case 3:
                    if (cb != null) {
                        synchronized (BatteryStatsImpl.this) {
                            if (BatteryStatsImpl.this.mCharging) {
                                action = "android.os.action.CHARGING";
                            } else {
                                action = "android.os.action.DISCHARGING";
                            }
                        }
                        Intent intent = new Intent(action);
                        intent.addFlags(67108864);
                        cb.batterySendBroadcast(intent);
                        return;
                    }
                    return;
                case 4:
                    if (cb != null) {
                        cb.batteryStatsReset();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public abstract class OverflowArrayMap<T> {
        private static final String OVERFLOW_NAME = "*overflow*";
        private static final String OVERFLOW_WEIXIN = "WakerLock:overflow";
        int M = 0;
        ArrayMap<String, MutableInt> mActiveOverflow;
        ArrayMap<String, MutableInt> mActiveOverflowWeixin;
        T mCurOverflow;
        T mCurOverflowWeixin;
        long mLastCleanupTime;
        long mLastClearTime;
        long mLastOverflowFinishTime;
        long mLastOverflowTime;
        final ArrayMap<String, T> mMap = new ArrayMap<>();
        final int mUid;

        public abstract T instantiateObject();

        public OverflowArrayMap(int uid) {
            this.mUid = uid;
        }

        public ArrayMap<String, T> getMap() {
            return this.mMap;
        }

        public void clear() {
            this.mLastClearTime = SystemClock.elapsedRealtime();
            this.mMap.clear();
            this.mCurOverflow = null;
            this.mActiveOverflow = null;
            this.mCurOverflowWeixin = null;
            this.mActiveOverflowWeixin = null;
        }

        public void add(String name, T obj) {
            if (name == null) {
                name = "";
            }
            this.mMap.put(name, obj);
            if (OVERFLOW_NAME.equals(name)) {
                this.mCurOverflow = obj;
            } else if (OVERFLOW_WEIXIN.equals(name)) {
                this.mCurOverflowWeixin = obj;
            }
            if (name.startsWith("WakerLock:")) {
                this.M++;
            }
        }

        public void cleanup() {
            this.mLastCleanupTime = SystemClock.elapsedRealtime();
            if (this.mActiveOverflowWeixin != null && this.mActiveOverflowWeixin.size() == 0) {
                this.mActiveOverflowWeixin = null;
            }
            if (this.mActiveOverflowWeixin == null) {
                if (this.mMap.containsKey(OVERFLOW_WEIXIN)) {
                    Slog.wtf(BatteryStatsImpl.TAG, "Cleaning up with no active overflow weixin, but have overflow entry " + this.mMap.get(OVERFLOW_WEIXIN));
                    this.mMap.remove(OVERFLOW_WEIXIN);
                }
                this.mCurOverflowWeixin = null;
            } else if (this.mCurOverflowWeixin == null || !this.mMap.containsKey(OVERFLOW_WEIXIN)) {
                Slog.wtf(BatteryStatsImpl.TAG, "Cleaning up with active overflow weixin, but no overflow entry: cur=" + this.mCurOverflowWeixin + " map=" + this.mMap.get(OVERFLOW_WEIXIN));
            }
            if (this.mActiveOverflow != null && this.mActiveOverflow.size() == 0) {
                this.mActiveOverflow = null;
            }
            if (this.mActiveOverflow == null) {
                if (this.mMap.containsKey(OVERFLOW_NAME)) {
                    Slog.wtf(BatteryStatsImpl.TAG, "Cleaning up with no active overflow, but have overflow entry " + this.mMap.get(OVERFLOW_NAME));
                    this.mMap.remove(OVERFLOW_NAME);
                }
                this.mCurOverflow = null;
            } else if (this.mCurOverflow == null || !this.mMap.containsKey(OVERFLOW_NAME)) {
                Slog.wtf(BatteryStatsImpl.TAG, "Cleaning up with active overflow, but no overflow entry: cur=" + this.mCurOverflow + " map=" + this.mMap.get(OVERFLOW_NAME));
            }
        }

        public T startObject(String name) {
            if (name == null) {
                name = "";
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            if (this.mActiveOverflowWeixin != null) {
                MutableInt over = this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    T obj2 = this.mCurOverflowWeixin;
                    if (obj2 == null) {
                        Slog.wtf(BatteryStatsImpl.TAG, "Have active overflow " + name + " but null overflow weixin");
                        T instantiateObject = instantiateObject();
                        this.mCurOverflowWeixin = instantiateObject;
                        obj2 = instantiateObject;
                        this.mMap.put(OVERFLOW_WEIXIN, obj2);
                    }
                    over.value++;
                    return obj2;
                }
            }
            if (name.startsWith("WakerLock:")) {
                this.M++;
                if (this.M > 60) {
                    T obj3 = this.mCurOverflowWeixin;
                    if (obj3 == null) {
                        T instantiateObject2 = instantiateObject();
                        this.mCurOverflowWeixin = instantiateObject2;
                        obj3 = instantiateObject2;
                        this.mMap.put(OVERFLOW_WEIXIN, obj3);
                    }
                    if (this.mActiveOverflowWeixin == null) {
                        this.mActiveOverflowWeixin = new ArrayMap<>();
                    }
                    this.mActiveOverflowWeixin.put(name, new MutableInt(1));
                    return obj3;
                }
            }
            if (this.mActiveOverflow != null) {
                MutableInt over2 = this.mActiveOverflow.get(name);
                if (over2 != null) {
                    T obj4 = this.mCurOverflow;
                    if (obj4 == null) {
                        Slog.wtf(BatteryStatsImpl.TAG, "Have active overflow " + name + " but null overflow");
                        T instantiateObject3 = instantiateObject();
                        this.mCurOverflow = instantiateObject3;
                        obj4 = instantiateObject3;
                        this.mMap.put(OVERFLOW_NAME, obj4);
                    }
                    over2.value++;
                    return obj4;
                }
            }
            if (this.mMap.size() >= BatteryStatsImpl.MAX_WAKELOCKS_PER_UID) {
                Slog.i(BatteryStatsImpl.TAG, "wakelocks more than 100, name: " + name);
                T obj5 = this.mCurOverflow;
                if (obj5 == null) {
                    T instantiateObject4 = instantiateObject();
                    this.mCurOverflow = instantiateObject4;
                    obj5 = instantiateObject4;
                    this.mMap.put(OVERFLOW_NAME, obj5);
                }
                if (this.mActiveOverflow == null) {
                    this.mActiveOverflow = new ArrayMap<>();
                }
                this.mActiveOverflow.put(name, new MutableInt(1));
                this.mLastOverflowTime = SystemClock.elapsedRealtime();
                return obj5;
            }
            T obj6 = instantiateObject();
            this.mMap.put(name, obj6);
            return obj6;
        }

        public T stopObject(String name) {
            if (name == null) {
                name = "";
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            if (this.mActiveOverflowWeixin != null) {
                MutableInt over = this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    T obj2 = this.mCurOverflowWeixin;
                    if (obj2 != null) {
                        over.value--;
                        if (over.value <= 0) {
                            this.mActiveOverflowWeixin.remove(name);
                        }
                        return obj2;
                    }
                }
            }
            if (this.mActiveOverflow != null) {
                MutableInt over2 = this.mActiveOverflow.get(name);
                if (over2 != null) {
                    T obj3 = this.mCurOverflow;
                    if (obj3 != null) {
                        over2.value--;
                        if (over2.value <= 0) {
                            this.mActiveOverflow.remove(name);
                            this.mLastOverflowFinishTime = SystemClock.elapsedRealtime();
                        }
                        return obj3;
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to find object for ");
            sb.append(name);
            sb.append(" in uid ");
            sb.append(this.mUid);
            sb.append(" mapsize=");
            sb.append(this.mMap.size());
            sb.append(" activeoverflow=");
            sb.append(this.mActiveOverflow);
            sb.append(" curoverflow=");
            sb.append(this.mCurOverflow);
            long now = SystemClock.elapsedRealtime();
            if (this.mLastOverflowTime != 0) {
                sb.append(" lastOverflowTime=");
                TimeUtils.formatDuration(this.mLastOverflowTime - now, sb);
            }
            if (this.mLastOverflowFinishTime != 0) {
                sb.append(" lastOverflowFinishTime=");
                TimeUtils.formatDuration(this.mLastOverflowFinishTime - now, sb);
            }
            if (this.mLastClearTime != 0) {
                sb.append(" lastClearTime=");
                TimeUtils.formatDuration(this.mLastClearTime - now, sb);
            }
            if (this.mLastCleanupTime != 0) {
                sb.append(" lastCleanupTime=");
                TimeUtils.formatDuration(this.mLastCleanupTime - now, sb);
            }
            Slog.wtf(BatteryStatsImpl.TAG, sb.toString());
            return null;
        }
    }

    public interface PlatformIdleStateCallback {
        void fillLowPowerStats(RpmStats rpmStats);

        String getPlatformLowPowerStats();

        String getSubsystemLowPowerStats();
    }

    public static class SamplingTimer extends Timer {
        int mCurrentReportedCount;
        long mCurrentReportedTotalTime;
        boolean mTimeBaseRunning;
        boolean mTrackingReportedValues;
        int mUnpluggedReportedCount;
        long mUnpluggedReportedTotalTime;
        int mUpdateVersion;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        @VisibleForTesting
        public SamplingTimer(Clocks clocks, TimeBase timeBase, Parcel in) {
            super(clocks, 0, timeBase, in);
            boolean z = false;
            this.mCurrentReportedCount = in.readInt();
            this.mUnpluggedReportedCount = in.readInt();
            this.mCurrentReportedTotalTime = in.readLong();
            this.mUnpluggedReportedTotalTime = in.readLong();
            this.mTrackingReportedValues = in.readInt() == 1 ? true : z;
            this.mTimeBaseRunning = timeBase.isRunning();
        }

        @VisibleForTesting
        public SamplingTimer(Clocks clocks, TimeBase timeBase) {
            super(clocks, 0, timeBase);
            this.mTrackingReportedValues = false;
            this.mTimeBaseRunning = timeBase.isRunning();
        }

        public void endSample() {
            this.mTotalTime = computeRunTimeLocked(0);
            this.mCount = computeCurrentCountLocked();
            this.mCurrentReportedTotalTime = 0;
            this.mUnpluggedReportedTotalTime = 0;
            this.mCurrentReportedCount = 0;
            this.mUnpluggedReportedCount = 0;
        }

        public void setUpdateVersion(int version) {
            this.mUpdateVersion = version;
        }

        public int getUpdateVersion() {
            return this.mUpdateVersion;
        }

        public void update(long totalTime, int count) {
            if (this.mTimeBaseRunning && !this.mTrackingReportedValues) {
                this.mUnpluggedReportedTotalTime = totalTime;
                this.mUnpluggedReportedCount = count;
            }
            this.mTrackingReportedValues = true;
            if (totalTime < this.mCurrentReportedTotalTime || count < this.mCurrentReportedCount) {
                endSample();
            }
            this.mCurrentReportedTotalTime = totalTime;
            this.mCurrentReportedCount = count;
        }

        public void add(long deltaTime, int deltaCount) {
            update(this.mCurrentReportedTotalTime + deltaTime, this.mCurrentReportedCount + deltaCount);
        }

        public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
            super.onTimeStarted(elapsedRealtime, baseUptime, baseRealtime);
            if (this.mTrackingReportedValues) {
                this.mUnpluggedReportedTotalTime = this.mCurrentReportedTotalTime;
                this.mUnpluggedReportedCount = this.mCurrentReportedCount;
            }
            this.mTimeBaseRunning = true;
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            super.onTimeStopped(elapsedRealtime, baseUptime, baseRealtime);
            this.mTimeBaseRunning = false;
        }

        public void logState(Printer pw, String prefix) {
            super.logState(pw, prefix);
            pw.println(prefix + "mCurrentReportedCount=" + this.mCurrentReportedCount + " mUnpluggedReportedCount=" + this.mUnpluggedReportedCount + " mCurrentReportedTotalTime=" + this.mCurrentReportedTotalTime + " mUnpluggedReportedTotalTime=" + this.mUnpluggedReportedTotalTime);
        }

        /* access modifiers changed from: protected */
        public long computeRunTimeLocked(long curBatteryRealtime) {
            return this.mTotalTime + ((!this.mTimeBaseRunning || !this.mTrackingReportedValues) ? 0 : this.mCurrentReportedTotalTime - this.mUnpluggedReportedTotalTime);
        }

        /* access modifiers changed from: protected */
        public int computeCurrentCountLocked() {
            return this.mCount + ((!this.mTimeBaseRunning || !this.mTrackingReportedValues) ? 0 : this.mCurrentReportedCount - this.mUnpluggedReportedCount);
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            super.writeToParcel(out, elapsedRealtimeUs);
            out.writeInt(this.mCurrentReportedCount);
            out.writeInt(this.mUnpluggedReportedCount);
            out.writeLong(this.mCurrentReportedTotalTime);
            out.writeLong(this.mUnpluggedReportedTotalTime);
            out.writeInt(this.mTrackingReportedValues ? 1 : 0);
        }

        public boolean reset(boolean detachIfReset) {
            super.reset(detachIfReset);
            this.mTrackingReportedValues = false;
            this.mUnpluggedReportedTotalTime = 0;
            this.mUnpluggedReportedCount = 0;
            return true;
        }
    }

    public static class StopwatchTimer extends Timer {
        long mAcquireTime = -1;
        @VisibleForTesting
        public boolean mInList;
        int mNesting;
        long mTimeout;
        final ArrayList<StopwatchTimer> mTimerPool;
        final Uid mUid;
        long mUpdateTime;

        public StopwatchTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase, Parcel in) {
            super(clocks, type, timeBase, in);
            this.mUid = uid;
            this.mTimerPool = timerPool;
            this.mUpdateTime = in.readLong();
        }

        public StopwatchTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase) {
            super(clocks, type, timeBase);
            this.mUid = uid;
            this.mTimerPool = timerPool;
        }

        public void setTimeout(long timeout) {
            this.mTimeout = timeout;
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            super.writeToParcel(out, elapsedRealtimeUs);
            out.writeLong(this.mUpdateTime);
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            if (this.mNesting > 0) {
                super.onTimeStopped(elapsedRealtime, baseUptime, baseRealtime);
                this.mUpdateTime = baseRealtime;
            }
        }

        public void logState(Printer pw, String prefix) {
            super.logState(pw, prefix);
            pw.println(prefix + "mNesting=" + this.mNesting + " mUpdateTime=" + this.mUpdateTime + " mAcquireTime=" + this.mAcquireTime);
        }

        public void startRunningLocked(long elapsedRealtimeMs) {
            int i = this.mNesting;
            this.mNesting = i + 1;
            if (i == 0) {
                long batteryRealtime = this.mTimeBase.getRealtime(1000 * elapsedRealtimeMs);
                this.mUpdateTime = batteryRealtime;
                if (this.mTimerPool != null) {
                    refreshTimersLocked(batteryRealtime, this.mTimerPool, null);
                    this.mTimerPool.add(this);
                }
                if (this.mTimeBase.isRunning()) {
                    this.mCount++;
                    this.mAcquireTime = this.mTotalTime;
                    return;
                }
                this.mAcquireTime = -1;
            }
        }

        public boolean isRunningLocked() {
            return this.mNesting > 0;
        }

        public void stopRunningLocked(long elapsedRealtimeMs) {
            if (this.mNesting != 0) {
                int i = this.mNesting - 1;
                this.mNesting = i;
                if (i == 0) {
                    long batteryRealtime = this.mTimeBase.getRealtime(1000 * elapsedRealtimeMs);
                    if (this.mTimerPool != null) {
                        refreshTimersLocked(batteryRealtime, this.mTimerPool, null);
                        this.mTimerPool.remove(this);
                    } else {
                        this.mNesting = 1;
                        this.mTotalTime = computeRunTimeLocked(batteryRealtime);
                        this.mNesting = 0;
                    }
                    if (this.mAcquireTime >= 0 && this.mTotalTime == this.mAcquireTime) {
                        this.mCount--;
                    }
                }
            }
        }

        public void stopAllRunningLocked(long elapsedRealtimeMs) {
            if (this.mNesting > 0) {
                this.mNesting = 1;
                stopRunningLocked(elapsedRealtimeMs);
            }
        }

        private static long refreshTimersLocked(long batteryRealtime, ArrayList<StopwatchTimer> pool, StopwatchTimer self) {
            long selfTime = 0;
            int N = pool.size();
            for (int i = N - 1; i >= 0; i--) {
                StopwatchTimer t = pool.get(i);
                long heldTime = batteryRealtime - t.mUpdateTime;
                if (heldTime > 0) {
                    long myTime = heldTime / ((long) N);
                    if (t == self) {
                        selfTime = myTime;
                    }
                    t.mTotalTime += myTime;
                }
                t.mUpdateTime = batteryRealtime;
            }
            return selfTime;
        }

        /* access modifiers changed from: protected */
        public long computeRunTimeLocked(long curBatteryRealtime) {
            long j = 0;
            if (this.mTimeout > 0 && curBatteryRealtime > this.mUpdateTime + this.mTimeout) {
                curBatteryRealtime = this.mUpdateTime + this.mTimeout;
            }
            int size = 0;
            if (this.mTimerPool != null) {
                size = this.mTimerPool.size();
            }
            long j2 = this.mTotalTime;
            if (this.mNesting > 0) {
                j = (curBatteryRealtime - this.mUpdateTime) / ((long) (size > 0 ? size : 1));
            }
            return j2 + j;
        }

        /* access modifiers changed from: protected */
        public int computeCurrentCountLocked() {
            return this.mCount;
        }

        public boolean reset(boolean detachIfReset) {
            boolean z = false;
            boolean canDetach = this.mNesting <= 0;
            if (canDetach && detachIfReset) {
                z = true;
            }
            super.reset(z);
            if (this.mNesting > 0) {
                this.mUpdateTime = this.mTimeBase.getRealtime(this.mClocks.elapsedRealtime() * 1000);
            }
            this.mAcquireTime = -1;
            return canDetach;
        }

        public void detach() {
            super.detach();
            if (this.mTimerPool != null) {
                this.mTimerPool.remove(this);
            }
        }

        public void readSummaryFromParcelLocked(Parcel in) {
            super.readSummaryFromParcelLocked(in);
            this.mNesting = 0;
        }

        public void setMark(long elapsedRealtimeMs) {
            long batteryRealtime = this.mTimeBase.getRealtime(1000 * elapsedRealtimeMs);
            if (this.mNesting > 0) {
                if (this.mTimerPool != null) {
                    refreshTimersLocked(batteryRealtime, this.mTimerPool, this);
                } else {
                    this.mTotalTime += batteryRealtime - this.mUpdateTime;
                    this.mUpdateTime = batteryRealtime;
                }
            }
            this.mTimeBeforeMark = this.mTotalTime;
        }
    }

    public static class SystemClocks implements Clocks {
        public long elapsedRealtime() {
            return SystemClock.elapsedRealtime();
        }

        public long uptimeMillis() {
            return SystemClock.uptimeMillis();
        }
    }

    public static class TimeBase {
        protected final ArrayList<TimeBaseObs> mObservers = new ArrayList<>();
        protected long mPastRealtime;
        protected long mPastUptime;
        protected long mRealtime;
        protected long mRealtimeStart;
        protected boolean mRunning;
        protected long mUnpluggedRealtime;
        protected long mUnpluggedUptime;
        protected long mUptime;
        protected long mUptimeStart;

        public void dump(PrintWriter pw, String prefix) {
            StringBuilder sb = new StringBuilder(128);
            pw.print(prefix);
            pw.print("mRunning=");
            pw.println(this.mRunning);
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mUptime=");
            BatteryStats.formatTimeMs(sb, this.mUptime / 1000);
            pw.println(sb.toString());
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mRealtime=");
            BatteryStats.formatTimeMs(sb, this.mRealtime / 1000);
            pw.println(sb.toString());
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mPastUptime=");
            BatteryStats.formatTimeMs(sb, this.mPastUptime / 1000);
            sb.append("mUptimeStart=");
            BatteryStats.formatTimeMs(sb, this.mUptimeStart / 1000);
            sb.append("mUnpluggedUptime=");
            BatteryStats.formatTimeMs(sb, this.mUnpluggedUptime / 1000);
            pw.println(sb.toString());
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mPastRealtime=");
            BatteryStats.formatTimeMs(sb, this.mPastRealtime / 1000);
            sb.append("mRealtimeStart=");
            BatteryStats.formatTimeMs(sb, this.mRealtimeStart / 1000);
            sb.append("mUnpluggedRealtime=");
            BatteryStats.formatTimeMs(sb, this.mUnpluggedRealtime / 1000);
            pw.println(sb.toString());
        }

        public synchronized void add(TimeBaseObs observer) {
            this.mObservers.add(observer);
        }

        public synchronized void remove(TimeBaseObs observer) {
            if (!this.mObservers.remove(observer)) {
                Slog.wtf(BatteryStatsImpl.TAG, "Removed unknown observer: " + observer);
            }
        }

        public synchronized boolean hasObserver(TimeBaseObs observer) {
            return this.mObservers.contains(observer);
        }

        public void init(long uptime, long realtime) {
            this.mRealtime = 0;
            this.mUptime = 0;
            this.mPastUptime = 0;
            this.mPastRealtime = 0;
            this.mUptimeStart = uptime;
            this.mRealtimeStart = realtime;
            this.mUnpluggedUptime = getUptime(this.mUptimeStart);
            this.mUnpluggedRealtime = getRealtime(this.mRealtimeStart);
        }

        public void reset(long uptime, long realtime) {
            if (!this.mRunning) {
                this.mPastUptime = 0;
                this.mPastRealtime = 0;
                return;
            }
            this.mUptimeStart = uptime;
            this.mRealtimeStart = realtime;
            this.mUnpluggedUptime = getUptime(uptime);
            this.mUnpluggedRealtime = getRealtime(realtime);
        }

        public long computeUptime(long curTime, int which) {
            switch (which) {
                case 0:
                    return this.mUptime + getUptime(curTime);
                case 1:
                    return getUptime(curTime);
                case 2:
                    return getUptime(curTime) - this.mUnpluggedUptime;
                default:
                    return 0;
            }
        }

        public long computeRealtime(long curTime, int which) {
            switch (which) {
                case 0:
                    return this.mRealtime + getRealtime(curTime);
                case 1:
                    return getRealtime(curTime);
                case 2:
                    return getRealtime(curTime) - this.mUnpluggedRealtime;
                default:
                    return 0;
            }
        }

        public long getUptime(long curTime) {
            long time = this.mPastUptime;
            if (this.mRunning) {
                return time + (curTime - this.mUptimeStart);
            }
            return time;
        }

        public long getRealtime(long curTime) {
            long time = this.mPastRealtime;
            if (this.mRunning) {
                return time + (curTime - this.mRealtimeStart);
            }
            return time;
        }

        public long getUptimeStart() {
            return this.mUptimeStart;
        }

        public long getRealtimeStart() {
            return this.mRealtimeStart;
        }

        public boolean isRunning() {
            return this.mRunning;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0068, code lost:
            return true;
         */
        public synchronized boolean setRunning(boolean running, long uptime, long realtime) {
            if (this.mRunning == running) {
                return false;
            }
            this.mRunning = running;
            if (running) {
                this.mUptimeStart = uptime;
                this.mRealtimeStart = realtime;
                long batteryUptime = getUptime(uptime);
                this.mUnpluggedUptime = batteryUptime;
                long batteryRealtime = getRealtime(realtime);
                this.mUnpluggedRealtime = batteryRealtime;
                int i = this.mObservers.size() - 1;
                while (true) {
                    int i2 = i;
                    if (i2 < 0) {
                        break;
                    }
                    this.mObservers.get(i2).onTimeStarted(realtime, batteryUptime, batteryRealtime);
                    i = i2 - 1;
                }
            } else {
                this.mPastUptime += uptime - this.mUptimeStart;
                this.mPastRealtime += realtime - this.mRealtimeStart;
                long batteryUptime2 = getUptime(uptime);
                long batteryRealtime2 = getRealtime(realtime);
                for (int i3 = this.mObservers.size() - 1; i3 >= 0; i3--) {
                    this.mObservers.get(i3).onTimeStopped(realtime, batteryUptime2, batteryRealtime2);
                }
            }
        }

        public void readSummaryFromParcel(Parcel in) {
            this.mUptime = in.readLong();
            this.mRealtime = in.readLong();
        }

        public void writeSummaryToParcel(Parcel out, long uptime, long realtime) {
            out.writeLong(computeUptime(uptime, 0));
            out.writeLong(computeRealtime(realtime, 0));
        }

        public void readFromParcel(Parcel in) {
            this.mRunning = false;
            this.mUptime = in.readLong();
            this.mPastUptime = in.readLong();
            this.mUptimeStart = in.readLong();
            this.mRealtime = in.readLong();
            this.mPastRealtime = in.readLong();
            this.mRealtimeStart = in.readLong();
            this.mUnpluggedUptime = in.readLong();
            this.mUnpluggedRealtime = in.readLong();
        }

        public void writeToParcel(Parcel out, long uptime, long realtime) {
            long runningUptime = getUptime(uptime);
            long runningRealtime = getRealtime(realtime);
            out.writeLong(this.mUptime);
            out.writeLong(runningUptime);
            out.writeLong(this.mUptimeStart);
            out.writeLong(this.mRealtime);
            out.writeLong(runningRealtime);
            out.writeLong(this.mRealtimeStart);
            out.writeLong(this.mUnpluggedUptime);
            out.writeLong(this.mUnpluggedRealtime);
        }
    }

    public interface TimeBaseObs {
        void onTimeStarted(long j, long j2, long j3);

        void onTimeStopped(long j, long j2, long j3);
    }

    public static abstract class Timer extends BatteryStats.Timer implements TimeBaseObs {
        protected final Clocks mClocks;
        protected int mCount;
        protected int mLastCount;
        protected long mLastTime;
        protected int mLoadedCount;
        protected long mLoadedTime;
        protected final TimeBase mTimeBase;
        protected long mTimeBeforeMark;
        protected long mTotalTime;
        protected final int mType;
        protected int mUnpluggedCount;
        protected long mUnpluggedTime;

        /* access modifiers changed from: protected */
        public abstract int computeCurrentCountLocked();

        /* access modifiers changed from: protected */
        public abstract long computeRunTimeLocked(long j);

        public Timer(Clocks clocks, int type, TimeBase timeBase, Parcel in) {
            this.mClocks = clocks;
            this.mType = type;
            this.mTimeBase = timeBase;
            this.mCount = in.readInt();
            this.mLoadedCount = in.readInt();
            this.mLastCount = 0;
            this.mUnpluggedCount = in.readInt();
            this.mTotalTime = in.readLong();
            this.mLoadedTime = in.readLong();
            this.mLastTime = 0;
            this.mUnpluggedTime = in.readLong();
            this.mTimeBeforeMark = in.readLong();
            timeBase.add(this);
        }

        public Timer(Clocks clocks, int type, TimeBase timeBase) {
            this.mClocks = clocks;
            this.mType = type;
            this.mTimeBase = timeBase;
            timeBase.add(this);
        }

        public boolean reset(boolean detachIfReset) {
            this.mTimeBeforeMark = 0;
            this.mLastTime = 0;
            this.mLoadedTime = 0;
            this.mTotalTime = 0;
            this.mLastCount = 0;
            this.mLoadedCount = 0;
            this.mCount = 0;
            if (detachIfReset) {
                detach();
            }
            return true;
        }

        public void detach() {
            this.mTimeBase.remove(this);
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            if (this.mTimeBase == null) {
                Slog.w(BatteryStatsImpl.TAG, "writeToParcel, mTimeBase is not exit");
                return;
            }
            out.writeInt(computeCurrentCountLocked());
            out.writeInt(this.mLoadedCount);
            out.writeInt(this.mUnpluggedCount);
            out.writeLong(computeRunTimeLocked(this.mTimeBase.getRealtime(elapsedRealtimeUs)));
            out.writeLong(this.mLoadedTime);
            out.writeLong(this.mUnpluggedTime);
            out.writeLong(this.mTimeBeforeMark);
        }

        public void onTimeStarted(long elapsedRealtime, long timeBaseUptime, long baseRealtime) {
            this.mUnpluggedTime = computeRunTimeLocked(baseRealtime);
            this.mUnpluggedCount = computeCurrentCountLocked();
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mTotalTime = computeRunTimeLocked(baseRealtime);
            this.mCount = computeCurrentCountLocked();
        }

        public static void writeTimerToParcel(Parcel out, Timer timer, long elapsedRealtimeUs) {
            if (timer == null) {
                out.writeInt(0);
                return;
            }
            out.writeInt(1);
            timer.writeToParcel(out, elapsedRealtimeUs);
        }

        public long getTotalTimeLocked(long elapsedRealtimeUs, int which) {
            long val = computeRunTimeLocked(this.mTimeBase.getRealtime(elapsedRealtimeUs));
            if (which == 2) {
                return val - this.mUnpluggedTime;
            }
            if (which != 0) {
                return val - this.mLoadedTime;
            }
            return val;
        }

        public int getCountLocked(int which) {
            int val = computeCurrentCountLocked();
            if (which == 2) {
                return val - this.mUnpluggedCount;
            }
            if (which != 0) {
                return val - this.mLoadedCount;
            }
            return val;
        }

        public long getTimeSinceMarkLocked(long elapsedRealtimeUs) {
            return computeRunTimeLocked(this.mTimeBase.getRealtime(elapsedRealtimeUs)) - this.mTimeBeforeMark;
        }

        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCount=" + this.mCount + " mLoadedCount=" + this.mLoadedCount + " mLastCount=" + this.mLastCount + " mUnpluggedCount=" + this.mUnpluggedCount);
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            sb.append("mTotalTime=");
            sb.append(this.mTotalTime);
            sb.append(" mLoadedTime=");
            sb.append(this.mLoadedTime);
            pw.println(sb.toString());
            pw.println(prefix + "mLastTime=" + this.mLastTime + " mUnpluggedTime=" + this.mUnpluggedTime);
        }

        public void writeSummaryFromParcelLocked(Parcel out, long elapsedRealtimeUs) {
            out.writeLong(computeRunTimeLocked(this.mTimeBase.getRealtime(elapsedRealtimeUs)));
            out.writeInt(computeCurrentCountLocked());
        }

        public void readSummaryFromParcelLocked(Parcel in) {
            long readLong = in.readLong();
            this.mLoadedTime = readLong;
            this.mTotalTime = readLong;
            this.mLastTime = 0;
            this.mUnpluggedTime = this.mTotalTime;
            int readInt = in.readInt();
            this.mLoadedCount = readInt;
            this.mCount = readInt;
            this.mLastCount = 0;
            this.mUnpluggedCount = this.mCount;
            this.mTimeBeforeMark = this.mTotalTime;
        }
    }

    public static class Uid extends BatteryStats.Uid {
        static final int NO_BATCHED_SCAN_STARTED = -1;
        DualTimer mAggregatedPartialWakelockTimer;
        StopwatchTimer mAudioTurnedOnTimer;
        private ControllerActivityCounterImpl mBluetoothControllerActivity;
        Counter mBluetoothScanResultBgCounter;
        Counter mBluetoothScanResultCounter;
        DualTimer mBluetoothScanTimer;
        DualTimer mBluetoothUnoptimizedScanTimer;
        protected BatteryStatsImpl mBsi;
        StopwatchTimer mCameraTurnedOnTimer;
        IntArray mChildUids;
        LongSamplingCounter mCpuActiveTimeMs;
        LongSamplingCounter[][] mCpuClusterSpeedTimesUs;
        LongSamplingCounterArray mCpuClusterTimesMs;
        LongSamplingCounterArray mCpuFreqTimeMs;
        long mCurStepSystemTime;
        long mCurStepUserTime;
        StopwatchTimer mFlashlightTurnedOnTimer;
        StopwatchTimer mForegroundActivityTimer;
        StopwatchTimer mForegroundServiceTimer;
        boolean mFullWifiLockOut;
        StopwatchTimer mFullWifiLockTimer;
        boolean mInForegroundService = false;
        final ArrayMap<String, SparseIntArray> mJobCompletions = new ArrayMap<>();
        final OverflowArrayMap<DualTimer> mJobStats;
        Counter mJobsDeferredCount;
        Counter mJobsDeferredEventCount;
        final Counter[] mJobsFreshnessBuckets;
        LongSamplingCounter mJobsFreshnessTimeMs;
        long mLastStepSystemTime;
        long mLastStepUserTime;
        LongSamplingCounter mMobileRadioActiveCount;
        LongSamplingCounter mMobileRadioActiveTime;
        /* access modifiers changed from: private */
        public LongSamplingCounter mMobileRadioApWakeupCount;
        private ControllerActivityCounterImpl mModemControllerActivity;
        LongSamplingCounter[] mNetworkByteActivityCounters;
        LongSamplingCounter[] mNetworkPacketActivityCounters;
        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public final TimeBase mOnBatteryBackgroundTimeBase;
        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public final TimeBase mOnBatteryScreenOffBackgroundTimeBase;
        final ArrayMap<String, Pkg> mPackageStats = new ArrayMap<>();
        final SparseArray<BatteryStats.Uid.Pid> mPids = new SparseArray<>();
        LongSamplingCounterArray[] mProcStateScreenOffTimeMs;
        LongSamplingCounterArray[] mProcStateTimeMs;
        int mProcessState = 19;
        StopwatchTimer[] mProcessStateTimer;
        final ArrayMap<String, Proc> mProcessStats = new ArrayMap<>();
        LongSamplingCounterArray mScreenOffCpuFreqTimeMs;
        final SparseArray<Sensor> mSensorStats = new SparseArray<>();
        final OverflowArrayMap<DualTimer> mSyncStats;
        LongSamplingCounter mSystemCpuTime;
        final int mUid;
        Counter[] mUserActivityCounters;
        LongSamplingCounter mUserCpuTime;
        BatchTimer mVibratorOnTimer;
        StopwatchTimer mVideoTurnedOnTimer;
        final OverflowArrayMap<Wakelock> mWakelockStats;
        int mWifiBatchedScanBinStarted = -1;
        StopwatchTimer[] mWifiBatchedScanTimer;
        private ControllerActivityCounterImpl mWifiControllerActivity;
        boolean mWifiMulticastEnabled;
        StopwatchTimer mWifiMulticastTimer;
        /* access modifiers changed from: private */
        public LongSamplingCounter mWifiRadioApWakeupCount;
        boolean mWifiRunning;
        StopwatchTimer mWifiRunningTimer;
        boolean mWifiScanStarted;
        DualTimer mWifiScanTimer;

        public static class Pkg extends BatteryStats.Uid.Pkg implements TimeBaseObs {
            protected BatteryStatsImpl mBsi;
            final ArrayMap<String, Serv> mServiceStats = new ArrayMap<>();
            ArrayMap<String, Counter> mWakeupAlarms = new ArrayMap<>();

            public static class Serv extends BatteryStats.Uid.Pkg.Serv implements TimeBaseObs {
                protected BatteryStatsImpl mBsi;
                protected int mLastLaunches;
                protected long mLastStartTime;
                protected int mLastStarts;
                protected boolean mLaunched;
                protected long mLaunchedSince;
                protected long mLaunchedTime;
                protected int mLaunches;
                protected int mLoadedLaunches;
                protected long mLoadedStartTime;
                protected int mLoadedStarts;
                protected Pkg mPkg;
                protected boolean mRunning;
                protected long mRunningSince;
                protected long mStartTime;
                protected int mStarts;
                protected int mUnpluggedLaunches;
                protected long mUnpluggedStartTime;
                protected int mUnpluggedStarts;

                public Serv(BatteryStatsImpl bsi) {
                    this.mBsi = bsi;
                    this.mBsi.mOnBatteryTimeBase.add(this);
                }

                public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
                    this.mUnpluggedStartTime = getStartTimeToNowLocked(baseUptime);
                    this.mUnpluggedStarts = this.mStarts;
                    this.mUnpluggedLaunches = this.mLaunches;
                }

                public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
                }

                public void detach() {
                    this.mBsi.mOnBatteryTimeBase.remove(this);
                }

                public void readFromParcelLocked(Parcel in) {
                    this.mStartTime = in.readLong();
                    this.mRunningSince = in.readLong();
                    boolean z = true;
                    this.mRunning = in.readInt() != 0;
                    this.mStarts = in.readInt();
                    this.mLaunchedTime = in.readLong();
                    this.mLaunchedSince = in.readLong();
                    if (in.readInt() == 0) {
                        z = false;
                    }
                    this.mLaunched = z;
                    this.mLaunches = in.readInt();
                    this.mLoadedStartTime = in.readLong();
                    this.mLoadedStarts = in.readInt();
                    this.mLoadedLaunches = in.readInt();
                    this.mLastStartTime = 0;
                    this.mLastStarts = 0;
                    this.mLastLaunches = 0;
                    this.mUnpluggedStartTime = in.readLong();
                    this.mUnpluggedStarts = in.readInt();
                    this.mUnpluggedLaunches = in.readInt();
                }

                public void writeToParcelLocked(Parcel out) {
                    out.writeLong(this.mStartTime);
                    out.writeLong(this.mRunningSince);
                    out.writeInt(this.mRunning ? 1 : 0);
                    out.writeInt(this.mStarts);
                    out.writeLong(this.mLaunchedTime);
                    out.writeLong(this.mLaunchedSince);
                    out.writeInt(this.mLaunched ? 1 : 0);
                    out.writeInt(this.mLaunches);
                    out.writeLong(this.mLoadedStartTime);
                    out.writeInt(this.mLoadedStarts);
                    out.writeInt(this.mLoadedLaunches);
                    out.writeLong(this.mUnpluggedStartTime);
                    out.writeInt(this.mUnpluggedStarts);
                    out.writeInt(this.mUnpluggedLaunches);
                }

                public long getLaunchTimeToNowLocked(long batteryUptime) {
                    if (!this.mLaunched) {
                        return this.mLaunchedTime;
                    }
                    return (this.mLaunchedTime + batteryUptime) - this.mLaunchedSince;
                }

                public long getStartTimeToNowLocked(long batteryUptime) {
                    if (!this.mRunning) {
                        return this.mStartTime;
                    }
                    return (this.mStartTime + batteryUptime) - this.mRunningSince;
                }

                public void startLaunchedLocked() {
                    if (!this.mLaunched) {
                        this.mLaunches++;
                        this.mLaunchedSince = this.mBsi.getBatteryUptimeLocked();
                        this.mLaunched = true;
                    }
                }

                public void stopLaunchedLocked() {
                    if (this.mLaunched) {
                        long time = this.mBsi.getBatteryUptimeLocked() - this.mLaunchedSince;
                        if (time > 0) {
                            this.mLaunchedTime += time;
                        } else {
                            this.mLaunches--;
                        }
                        this.mLaunched = false;
                    }
                }

                public void startRunningLocked() {
                    if (!this.mRunning) {
                        this.mStarts++;
                        this.mRunningSince = this.mBsi.getBatteryUptimeLocked();
                        this.mRunning = true;
                    }
                }

                public void stopRunningLocked() {
                    if (this.mRunning) {
                        long time = this.mBsi.getBatteryUptimeLocked() - this.mRunningSince;
                        if (time > 0) {
                            this.mStartTime += time;
                        } else {
                            this.mStarts--;
                        }
                        this.mRunning = false;
                    }
                }

                public BatteryStatsImpl getBatteryStats() {
                    return this.mBsi;
                }

                public int getLaunches(int which) {
                    int val = this.mLaunches;
                    if (which == 1) {
                        return val - this.mLoadedLaunches;
                    }
                    if (which == 2) {
                        return val - this.mUnpluggedLaunches;
                    }
                    return val;
                }

                public long getStartTime(long now, int which) {
                    long val = getStartTimeToNowLocked(now);
                    if (which == 1) {
                        return val - this.mLoadedStartTime;
                    }
                    if (which == 2) {
                        return val - this.mUnpluggedStartTime;
                    }
                    return val;
                }

                public int getStarts(int which) {
                    int val = this.mStarts;
                    if (which == 1) {
                        return val - this.mLoadedStarts;
                    }
                    if (which == 2) {
                        return val - this.mUnpluggedStarts;
                    }
                    return val;
                }
            }

            public Pkg(BatteryStatsImpl bsi) {
                this.mBsi = bsi;
                this.mBsi.mOnBatteryScreenOffTimeBase.add(this);
            }

            public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
            }

            public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            }

            /* access modifiers changed from: package-private */
            public void detach() {
                this.mBsi.mOnBatteryScreenOffTimeBase.remove(this);
            }

            /* access modifiers changed from: package-private */
            public void readFromParcelLocked(Parcel in) {
                int numWA = in.readInt();
                this.mWakeupAlarms.clear();
                for (int i = 0; i < numWA; i++) {
                    this.mWakeupAlarms.put(in.readString(), new Counter(this.mBsi.mOnBatteryScreenOffTimeBase, in));
                }
                int i2 = in.readInt();
                this.mServiceStats.clear();
                for (int m = 0; m < i2; m++) {
                    String serviceName = in.readString();
                    Serv serv = new Serv(this.mBsi);
                    this.mServiceStats.put(serviceName, serv);
                    serv.readFromParcelLocked(in);
                }
            }

            /* access modifiers changed from: package-private */
            public void writeToParcelLocked(Parcel out) {
                int numWA = this.mWakeupAlarms.size();
                out.writeInt(numWA);
                for (int i = 0; i < numWA; i++) {
                    out.writeString(this.mWakeupAlarms.keyAt(i));
                    this.mWakeupAlarms.valueAt(i).writeToParcel(out);
                }
                int NS = this.mServiceStats.size();
                out.writeInt(NS);
                for (int i2 = 0; i2 < NS; i2++) {
                    out.writeString(this.mServiceStats.keyAt(i2));
                    this.mServiceStats.valueAt(i2).writeToParcelLocked(out);
                }
            }

            public ArrayMap<String, ? extends BatteryStats.Counter> getWakeupAlarmStats() {
                return this.mWakeupAlarms;
            }

            public void noteWakeupAlarmLocked(String tag) {
                Counter c = this.mWakeupAlarms.get(tag);
                if (c == null) {
                    c = new Counter(this.mBsi.mOnBatteryScreenOffTimeBase);
                    this.mWakeupAlarms.put(tag, c);
                }
                c.stepAtomic();
            }

            public ArrayMap<String, ? extends BatteryStats.Uid.Pkg.Serv> getServiceStats() {
                return this.mServiceStats;
            }

            /* access modifiers changed from: package-private */
            public final Serv newServiceStatsLocked() {
                return new Serv(this.mBsi);
            }
        }

        public static class Proc extends BatteryStats.Uid.Proc implements TimeBaseObs {
            boolean mActive = true;
            protected BatteryStatsImpl mBsi;
            ArrayList<BatteryStats.Uid.Proc.ExcessivePower> mExcessivePower;
            long mForegroundTime;
            long mLoadedForegroundTime;
            int mLoadedNumAnrs;
            int mLoadedNumCrashes;
            int mLoadedStarts;
            long mLoadedSystemTime;
            long mLoadedUserTime;
            final String mName;
            int mNumAnrs;
            int mNumCrashes;
            int mStarts;
            long mSystemTime;
            long mUnpluggedForegroundTime;
            int mUnpluggedNumAnrs;
            int mUnpluggedNumCrashes;
            int mUnpluggedStarts;
            long mUnpluggedSystemTime;
            long mUnpluggedUserTime;
            long mUserTime;

            public Proc(BatteryStatsImpl bsi, String name) {
                this.mBsi = bsi;
                this.mName = name;
                this.mBsi.mOnBatteryTimeBase.add(this);
            }

            public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
                this.mUnpluggedUserTime = this.mUserTime;
                this.mUnpluggedSystemTime = this.mSystemTime;
                this.mUnpluggedForegroundTime = this.mForegroundTime;
                this.mUnpluggedStarts = this.mStarts;
                this.mUnpluggedNumCrashes = this.mNumCrashes;
                this.mUnpluggedNumAnrs = this.mNumAnrs;
            }

            public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            }

            /* access modifiers changed from: package-private */
            public void detach() {
                this.mActive = false;
                this.mBsi.mOnBatteryTimeBase.remove(this);
            }

            public int countExcessivePowers() {
                if (this.mExcessivePower != null) {
                    return this.mExcessivePower.size();
                }
                return 0;
            }

            public BatteryStats.Uid.Proc.ExcessivePower getExcessivePower(int i) {
                if (this.mExcessivePower != null) {
                    return this.mExcessivePower.get(i);
                }
                return null;
            }

            public void addExcessiveCpu(long overTime, long usedTime) {
                if (this.mExcessivePower == null) {
                    this.mExcessivePower = new ArrayList<>();
                }
                BatteryStats.Uid.Proc.ExcessivePower ew = new BatteryStats.Uid.Proc.ExcessivePower();
                ew.type = 2;
                ew.overTime = overTime;
                ew.usedTime = usedTime;
                this.mExcessivePower.add(ew);
            }

            /* access modifiers changed from: package-private */
            public void writeExcessivePowerToParcelLocked(Parcel out) {
                if (this.mExcessivePower == null) {
                    out.writeInt(0);
                    return;
                }
                int N = this.mExcessivePower.size();
                out.writeInt(N);
                for (int i = 0; i < N; i++) {
                    BatteryStats.Uid.Proc.ExcessivePower ew = this.mExcessivePower.get(i);
                    out.writeInt(ew.type);
                    out.writeLong(ew.overTime);
                    out.writeLong(ew.usedTime);
                }
            }

            /* access modifiers changed from: package-private */
            public void readExcessivePowerFromParcelLocked(Parcel in) {
                int N = in.readInt();
                if (N == 0) {
                    this.mExcessivePower = null;
                } else if (N <= 10000) {
                    this.mExcessivePower = new ArrayList<>();
                    for (int i = 0; i < N; i++) {
                        BatteryStats.Uid.Proc.ExcessivePower ew = new BatteryStats.Uid.Proc.ExcessivePower();
                        ew.type = in.readInt();
                        ew.overTime = in.readLong();
                        ew.usedTime = in.readLong();
                        this.mExcessivePower.add(ew);
                    }
                } else {
                    throw new ParcelFormatException("File corrupt: too many excessive power entries " + N);
                }
            }

            /* access modifiers changed from: package-private */
            public void writeToParcelLocked(Parcel out) {
                out.writeLong(this.mUserTime);
                out.writeLong(this.mSystemTime);
                out.writeLong(this.mForegroundTime);
                out.writeInt(this.mStarts);
                out.writeInt(this.mNumCrashes);
                out.writeInt(this.mNumAnrs);
                out.writeLong(this.mLoadedUserTime);
                out.writeLong(this.mLoadedSystemTime);
                out.writeLong(this.mLoadedForegroundTime);
                out.writeInt(this.mLoadedStarts);
                out.writeInt(this.mLoadedNumCrashes);
                out.writeInt(this.mLoadedNumAnrs);
                out.writeLong(this.mUnpluggedUserTime);
                out.writeLong(this.mUnpluggedSystemTime);
                out.writeLong(this.mUnpluggedForegroundTime);
                out.writeInt(this.mUnpluggedStarts);
                out.writeInt(this.mUnpluggedNumCrashes);
                out.writeInt(this.mUnpluggedNumAnrs);
                writeExcessivePowerToParcelLocked(out);
            }

            /* access modifiers changed from: package-private */
            public void readFromParcelLocked(Parcel in) {
                this.mUserTime = in.readLong();
                this.mSystemTime = in.readLong();
                this.mForegroundTime = in.readLong();
                this.mStarts = in.readInt();
                this.mNumCrashes = in.readInt();
                this.mNumAnrs = in.readInt();
                this.mLoadedUserTime = in.readLong();
                this.mLoadedSystemTime = in.readLong();
                this.mLoadedForegroundTime = in.readLong();
                this.mLoadedStarts = in.readInt();
                this.mLoadedNumCrashes = in.readInt();
                this.mLoadedNumAnrs = in.readInt();
                this.mUnpluggedUserTime = in.readLong();
                this.mUnpluggedSystemTime = in.readLong();
                this.mUnpluggedForegroundTime = in.readLong();
                this.mUnpluggedStarts = in.readInt();
                this.mUnpluggedNumCrashes = in.readInt();
                this.mUnpluggedNumAnrs = in.readInt();
                readExcessivePowerFromParcelLocked(in);
            }

            public void addCpuTimeLocked(int utime, int stime) {
                addCpuTimeLocked(utime, stime, this.mBsi.mOnBatteryTimeBase.isRunning());
            }

            public void addCpuTimeLocked(int utime, int stime, boolean isRunning) {
                if (isRunning) {
                    this.mUserTime += (long) utime;
                    this.mSystemTime += (long) stime;
                }
            }

            public void addForegroundTimeLocked(long ttime) {
                this.mForegroundTime += ttime;
            }

            public void incStartsLocked() {
                this.mStarts++;
            }

            public void incNumCrashesLocked() {
                this.mNumCrashes++;
            }

            public void incNumAnrsLocked() {
                this.mNumAnrs++;
            }

            public boolean isActive() {
                return this.mActive;
            }

            public long getUserTime(int which) {
                long val = this.mUserTime;
                if (which == 1) {
                    return val - this.mLoadedUserTime;
                }
                if (which == 2) {
                    return val - this.mUnpluggedUserTime;
                }
                return val;
            }

            public long getSystemTime(int which) {
                long val = this.mSystemTime;
                if (which == 1) {
                    return val - this.mLoadedSystemTime;
                }
                if (which == 2) {
                    return val - this.mUnpluggedSystemTime;
                }
                return val;
            }

            public long getForegroundTime(int which) {
                long val = this.mForegroundTime;
                if (which == 1) {
                    return val - this.mLoadedForegroundTime;
                }
                if (which == 2) {
                    return val - this.mUnpluggedForegroundTime;
                }
                return val;
            }

            public int getStarts(int which) {
                int val = this.mStarts;
                if (which == 1) {
                    return val - this.mLoadedStarts;
                }
                if (which == 2) {
                    return val - this.mUnpluggedStarts;
                }
                return val;
            }

            public int getNumCrashes(int which) {
                int val = this.mNumCrashes;
                if (which == 1) {
                    return val - this.mLoadedNumCrashes;
                }
                if (which == 2) {
                    return val - this.mUnpluggedNumCrashes;
                }
                return val;
            }

            public int getNumAnrs(int which) {
                int val = this.mNumAnrs;
                if (which == 1) {
                    return val - this.mLoadedNumAnrs;
                }
                if (which == 2) {
                    return val - this.mUnpluggedNumAnrs;
                }
                return val;
            }
        }

        public static class Sensor extends BatteryStats.Uid.Sensor {
            protected BatteryStatsImpl mBsi;
            final int mHandle;
            DualTimer mTimer;
            protected Uid mUid;

            public Sensor(BatteryStatsImpl bsi, Uid uid, int handle) {
                this.mBsi = bsi;
                this.mUid = uid;
                this.mHandle = handle;
            }

            private DualTimer readTimersFromParcel(TimeBase timeBase, TimeBase bgTimeBase, Parcel in) {
                if (in.readInt() == 0) {
                    return null;
                }
                ArrayList<StopwatchTimer> pool = this.mBsi.mSensorTimers.get(this.mHandle);
                if (pool == null) {
                    pool = new ArrayList<>();
                    this.mBsi.mSensorTimers.put(this.mHandle, pool);
                }
                DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this.mUid, 0, pool, timeBase, bgTimeBase, in);
                return dualTimer;
            }

            /* access modifiers changed from: package-private */
            public boolean reset() {
                if (!this.mTimer.reset(true)) {
                    return false;
                }
                this.mTimer = null;
                return true;
            }

            /* access modifiers changed from: package-private */
            public void readFromParcelLocked(TimeBase timeBase, TimeBase bgTimeBase, Parcel in) {
                this.mTimer = readTimersFromParcel(timeBase, bgTimeBase, in);
            }

            /* access modifiers changed from: package-private */
            public void writeToParcelLocked(Parcel out, long elapsedRealtimeUs) {
                Timer.writeTimerToParcel(out, this.mTimer, elapsedRealtimeUs);
            }

            public Timer getSensorTime() {
                return this.mTimer;
            }

            public Timer getSensorBackgroundTime() {
                if (this.mTimer == null) {
                    return null;
                }
                return this.mTimer.getSubTimer();
            }

            public int getHandle() {
                return this.mHandle;
            }
        }

        public static class Wakelock extends BatteryStats.Uid.Wakelock {
            protected BatteryStatsImpl mBsi;
            StopwatchTimer mTimerDraw;
            StopwatchTimer mTimerFull;
            DualTimer mTimerPartial;
            StopwatchTimer mTimerWindow;
            protected Uid mUid;

            public Wakelock(BatteryStatsImpl bsi, Uid uid) {
                this.mBsi = bsi;
                this.mUid = uid;
            }

            private StopwatchTimer readStopwatchTimerFromParcel(int type, ArrayList<StopwatchTimer> pool, TimeBase timeBase, Parcel in) {
                if (in.readInt() == 0) {
                    return null;
                }
                StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this.mUid, type, pool, timeBase, in);
                return stopwatchTimer;
            }

            private DualTimer readDualTimerFromParcel(int type, ArrayList<StopwatchTimer> pool, TimeBase timeBase, TimeBase bgTimeBase, Parcel in) {
                if (in.readInt() == 0) {
                    return null;
                }
                DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this.mUid, type, pool, timeBase, bgTimeBase, in);
                return dualTimer;
            }

            /* access modifiers changed from: package-private */
            public boolean reset() {
                boolean wlactive = false;
                if (this.mTimerFull != null) {
                    wlactive = false | (!this.mTimerFull.reset(false));
                }
                if (this.mTimerPartial != null) {
                    wlactive |= !this.mTimerPartial.reset(false);
                }
                if (this.mTimerWindow != null) {
                    wlactive |= !this.mTimerWindow.reset(false);
                }
                if (this.mTimerDraw != null) {
                    wlactive |= !this.mTimerDraw.reset(false);
                }
                if (!wlactive) {
                    if (this.mTimerFull != null) {
                        this.mTimerFull.detach();
                        this.mTimerFull = null;
                    }
                    if (this.mTimerPartial != null) {
                        this.mTimerPartial.detach();
                        this.mTimerPartial = null;
                    }
                    if (this.mTimerWindow != null) {
                        this.mTimerWindow.detach();
                        this.mTimerWindow = null;
                    }
                    if (this.mTimerDraw != null) {
                        this.mTimerDraw.detach();
                        this.mTimerDraw = null;
                    }
                }
                if (!wlactive) {
                    return true;
                }
                return false;
            }

            /* access modifiers changed from: package-private */
            public void readFromParcelLocked(TimeBase timeBase, TimeBase screenOffTimeBase, TimeBase screenOffBgTimeBase, Parcel in) {
                this.mTimerPartial = readDualTimerFromParcel(0, this.mBsi.mPartialTimers, screenOffTimeBase, screenOffBgTimeBase, in);
                this.mTimerFull = readStopwatchTimerFromParcel(1, this.mBsi.mFullTimers, timeBase, in);
                this.mTimerWindow = readStopwatchTimerFromParcel(2, this.mBsi.mWindowTimers, timeBase, in);
                this.mTimerDraw = readStopwatchTimerFromParcel(18, this.mBsi.mDrawTimers, timeBase, in);
            }

            /* access modifiers changed from: package-private */
            public void writeToParcelLocked(Parcel out, long elapsedRealtimeUs) {
                Timer.writeTimerToParcel(out, this.mTimerPartial, elapsedRealtimeUs);
                Timer.writeTimerToParcel(out, this.mTimerFull, elapsedRealtimeUs);
                Timer.writeTimerToParcel(out, this.mTimerWindow, elapsedRealtimeUs);
                Timer.writeTimerToParcel(out, this.mTimerDraw, elapsedRealtimeUs);
            }

            public Timer getWakeTime(int type) {
                if (type == 18) {
                    return this.mTimerDraw;
                }
                switch (type) {
                    case 0:
                        return this.mTimerPartial;
                    case 1:
                        return this.mTimerFull;
                    case 2:
                        return this.mTimerWindow;
                    default:
                        throw new IllegalArgumentException("type = " + type);
                }
            }
        }

        public Uid(BatteryStatsImpl bsi, int uid) {
            this.mBsi = bsi;
            this.mUid = uid;
            this.mOnBatteryBackgroundTimeBase = new TimeBase();
            this.mOnBatteryBackgroundTimeBase.init(this.mBsi.mClocks.uptimeMillis() * 1000, this.mBsi.mClocks.elapsedRealtime() * 1000);
            this.mOnBatteryScreenOffBackgroundTimeBase = new TimeBase();
            this.mOnBatteryScreenOffBackgroundTimeBase.init(this.mBsi.mClocks.uptimeMillis() * 1000, this.mBsi.mClocks.elapsedRealtime() * 1000);
            this.mUserCpuTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            this.mSystemCpuTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            this.mCpuActiveTimeMs = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            this.mCpuClusterTimesMs = new LongSamplingCounterArray(this.mBsi.mOnBatteryTimeBase);
            BatteryStatsImpl batteryStatsImpl = this.mBsi;
            Objects.requireNonNull(batteryStatsImpl);
            this.mWakelockStats = new OverflowArrayMap<Wakelock>(batteryStatsImpl, uid) {
                {
                    Objects.requireNonNull(x0);
                }

                public Wakelock instantiateObject() {
                    return new Wakelock(Uid.this.mBsi, Uid.this);
                }
            };
            BatteryStatsImpl batteryStatsImpl2 = this.mBsi;
            Objects.requireNonNull(batteryStatsImpl2);
            this.mSyncStats = new OverflowArrayMap<DualTimer>(batteryStatsImpl2, uid) {
                {
                    Objects.requireNonNull(x0);
                }

                public DualTimer instantiateObject() {
                    DualTimer dualTimer = new DualTimer(Uid.this.mBsi.mClocks, Uid.this, 13, null, Uid.this.mBsi.mOnBatteryTimeBase, Uid.this.mOnBatteryBackgroundTimeBase);
                    return dualTimer;
                }
            };
            BatteryStatsImpl batteryStatsImpl3 = this.mBsi;
            Objects.requireNonNull(batteryStatsImpl3);
            this.mJobStats = new OverflowArrayMap<DualTimer>(batteryStatsImpl3, uid) {
                {
                    Objects.requireNonNull(x0);
                }

                public DualTimer instantiateObject() {
                    DualTimer dualTimer = new DualTimer(Uid.this.mBsi.mClocks, Uid.this, 14, null, Uid.this.mBsi.mOnBatteryTimeBase, Uid.this.mOnBatteryBackgroundTimeBase);
                    return dualTimer;
                }
            };
            StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 4, this.mBsi.mWifiRunningTimers, this.mBsi.mOnBatteryTimeBase);
            this.mWifiRunningTimer = stopwatchTimer;
            StopwatchTimer stopwatchTimer2 = new StopwatchTimer(this.mBsi.mClocks, this, 5, this.mBsi.mFullWifiLockTimers, this.mBsi.mOnBatteryTimeBase);
            this.mFullWifiLockTimer = stopwatchTimer2;
            DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this, 6, this.mBsi.mWifiScanTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
            this.mWifiScanTimer = dualTimer;
            this.mWifiBatchedScanTimer = new StopwatchTimer[5];
            StopwatchTimer stopwatchTimer3 = new StopwatchTimer(this.mBsi.mClocks, this, 7, this.mBsi.mWifiMulticastTimers, this.mBsi.mOnBatteryTimeBase);
            this.mWifiMulticastTimer = stopwatchTimer3;
            this.mProcessStateTimer = new StopwatchTimer[7];
            this.mJobsDeferredEventCount = new Counter(this.mBsi.mOnBatteryTimeBase);
            this.mJobsDeferredCount = new Counter(this.mBsi.mOnBatteryTimeBase);
            this.mJobsFreshnessTimeMs = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            this.mJobsFreshnessBuckets = new Counter[BatteryStats.JOB_FRESHNESS_BUCKETS.length];
        }

        @VisibleForTesting
        public void setProcessStateForTest(int procState) {
            this.mProcessState = procState;
        }

        public long[] getCpuFreqTimes(int which) {
            return nullIfAllZeros(this.mCpuFreqTimeMs, which);
        }

        public long[] getScreenOffCpuFreqTimes(int which) {
            return nullIfAllZeros(this.mScreenOffCpuFreqTimeMs, which);
        }

        public long getCpuActiveTime() {
            return this.mCpuActiveTimeMs.getCountLocked(0);
        }

        public long[] getCpuClusterTimes() {
            return nullIfAllZeros(this.mCpuClusterTimesMs, 0);
        }

        public long[] getCpuFreqTimes(int which, int procState) {
            if (which < 0 || which >= 7 || this.mProcStateTimeMs == null) {
                return null;
            }
            if (this.mBsi.mPerProcStateCpuTimesAvailable) {
                return nullIfAllZeros(this.mProcStateTimeMs[procState], which);
            }
            this.mProcStateTimeMs = null;
            return null;
        }

        public long[] getScreenOffCpuFreqTimes(int which, int procState) {
            if (which < 0 || which >= 7 || this.mProcStateScreenOffTimeMs == null) {
                return null;
            }
            if (this.mBsi.mPerProcStateCpuTimesAvailable) {
                return nullIfAllZeros(this.mProcStateScreenOffTimeMs[procState], which);
            }
            this.mProcStateScreenOffTimeMs = null;
            return null;
        }

        public void addIsolatedUid(int isolatedUid) {
            if (this.mChildUids == null) {
                this.mChildUids = new IntArray();
            } else if (this.mChildUids.indexOf(isolatedUid) >= 0) {
                return;
            }
            this.mChildUids.add(isolatedUid);
        }

        public void removeIsolatedUid(int isolatedUid) {
            int idx = this.mChildUids == null ? -1 : this.mChildUids.indexOf(isolatedUid);
            if (idx >= 0) {
                this.mChildUids.remove(idx);
            }
        }

        private long[] nullIfAllZeros(LongSamplingCounterArray cpuTimesMs, int which) {
            if (cpuTimesMs == null) {
                return null;
            }
            long[] counts = cpuTimesMs.getCountsLocked(which);
            if (counts == null) {
                return null;
            }
            for (int i = counts.length - 1; i >= 0; i--) {
                if (counts[i] != 0) {
                    return counts;
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        public void addProcStateTimesMs(int procState, long[] cpuTimesMs, boolean onBattery) {
            if (this.mProcStateTimeMs == null) {
                this.mProcStateTimeMs = new LongSamplingCounterArray[7];
            }
            if (this.mProcStateTimeMs[procState] == null || this.mProcStateTimeMs[procState].getSize() != cpuTimesMs.length) {
                this.mProcStateTimeMs[procState] = new LongSamplingCounterArray(this.mBsi.mOnBatteryTimeBase);
            }
            this.mProcStateTimeMs[procState].addCountLocked(cpuTimesMs, onBattery);
        }

        /* access modifiers changed from: private */
        public void addProcStateScreenOffTimesMs(int procState, long[] cpuTimesMs, boolean onBatteryScreenOff) {
            if (this.mProcStateScreenOffTimeMs == null) {
                this.mProcStateScreenOffTimeMs = new LongSamplingCounterArray[7];
            }
            if (this.mProcStateScreenOffTimeMs[procState] == null || this.mProcStateScreenOffTimeMs[procState].getSize() != cpuTimesMs.length) {
                this.mProcStateScreenOffTimeMs[procState] = new LongSamplingCounterArray(this.mBsi.mOnBatteryScreenOffTimeBase);
            }
            this.mProcStateScreenOffTimeMs[procState].addCountLocked(cpuTimesMs, onBatteryScreenOff);
        }

        public Timer getAggregatedPartialWakelockTimer() {
            return this.mAggregatedPartialWakelockTimer;
        }

        public ArrayMap<String, ? extends BatteryStats.Uid.Wakelock> getWakelockStats() {
            return this.mWakelockStats.getMap();
        }

        public Timer getMulticastWakelockStats() {
            return this.mWifiMulticastTimer;
        }

        public ArrayMap<String, ? extends BatteryStats.Timer> getSyncStats() {
            return this.mSyncStats.getMap();
        }

        public ArrayMap<String, ? extends BatteryStats.Timer> getJobStats() {
            return this.mJobStats.getMap();
        }

        public ArrayMap<String, SparseIntArray> getJobCompletionStats() {
            return this.mJobCompletions;
        }

        public SparseArray<? extends BatteryStats.Uid.Sensor> getSensorStats() {
            return this.mSensorStats;
        }

        public ArrayMap<String, ? extends BatteryStats.Uid.Proc> getProcessStats() {
            return this.mProcessStats;
        }

        public ArrayMap<String, ? extends BatteryStats.Uid.Pkg> getPackageStats() {
            return this.mPackageStats;
        }

        public int getUid() {
            return this.mUid;
        }

        public void noteWifiRunningLocked(long elapsedRealtimeMs) {
            if (!this.mWifiRunning) {
                this.mWifiRunning = true;
                if (this.mWifiRunningTimer == null) {
                    StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 4, this.mBsi.mWifiRunningTimers, this.mBsi.mOnBatteryTimeBase);
                    this.mWifiRunningTimer = stopwatchTimer;
                }
                this.mWifiRunningTimer.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteWifiStoppedLocked(long elapsedRealtimeMs) {
            if (this.mWifiRunning) {
                this.mWifiRunning = false;
                this.mWifiRunningTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteFullWifiLockAcquiredLocked(long elapsedRealtimeMs) {
            if (!this.mFullWifiLockOut) {
                this.mFullWifiLockOut = true;
                if (this.mFullWifiLockTimer == null) {
                    StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 5, this.mBsi.mFullWifiLockTimers, this.mBsi.mOnBatteryTimeBase);
                    this.mFullWifiLockTimer = stopwatchTimer;
                }
                this.mFullWifiLockTimer.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteFullWifiLockReleasedLocked(long elapsedRealtimeMs) {
            if (this.mFullWifiLockOut) {
                this.mFullWifiLockOut = false;
                this.mFullWifiLockTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteWifiScanStartedLocked(long elapsedRealtimeMs) {
            if (!this.mWifiScanStarted) {
                this.mWifiScanStarted = true;
                if (this.mWifiScanTimer == null) {
                    DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this, 6, this.mBsi.mWifiScanTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
                    this.mWifiScanTimer = dualTimer;
                }
                this.mWifiScanTimer.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteWifiScanStoppedLocked(long elapsedRealtimeMs) {
            if (this.mWifiScanStarted) {
                this.mWifiScanStarted = false;
                this.mWifiScanTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteWifiBatchedScanStartedLocked(int csph, long elapsedRealtimeMs) {
            int bin = 0;
            while (csph > 8 && bin < 4) {
                csph >>= 3;
                bin++;
            }
            if (this.mWifiBatchedScanBinStarted != bin) {
                if (this.mWifiBatchedScanBinStarted != -1) {
                    this.mWifiBatchedScanTimer[this.mWifiBatchedScanBinStarted].stopRunningLocked(elapsedRealtimeMs);
                }
                this.mWifiBatchedScanBinStarted = bin;
                if (this.mWifiBatchedScanTimer[bin] == null) {
                    makeWifiBatchedScanBin(bin, null);
                }
                this.mWifiBatchedScanTimer[bin].startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteWifiBatchedScanStoppedLocked(long elapsedRealtimeMs) {
            if (this.mWifiBatchedScanBinStarted != -1) {
                this.mWifiBatchedScanTimer[this.mWifiBatchedScanBinStarted].stopRunningLocked(elapsedRealtimeMs);
                this.mWifiBatchedScanBinStarted = -1;
            }
        }

        public void noteWifiMulticastEnabledLocked(long elapsedRealtimeMs) {
            if (!this.mWifiMulticastEnabled) {
                this.mWifiMulticastEnabled = true;
                if (this.mWifiMulticastTimer == null) {
                    StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 7, this.mBsi.mWifiMulticastTimers, this.mBsi.mOnBatteryTimeBase);
                    this.mWifiMulticastTimer = stopwatchTimer;
                }
                this.mWifiMulticastTimer.startRunningLocked(elapsedRealtimeMs);
                StatsLog.write_non_chained(53, getUid(), null, 1);
            }
        }

        public void noteWifiMulticastDisabledLocked(long elapsedRealtimeMs) {
            if (this.mWifiMulticastEnabled) {
                this.mWifiMulticastEnabled = false;
                this.mWifiMulticastTimer.stopRunningLocked(elapsedRealtimeMs);
                StatsLog.write_non_chained(53, getUid(), null, 0);
            }
        }

        public BatteryStats.ControllerActivityCounter getWifiControllerActivity() {
            return this.mWifiControllerActivity;
        }

        public BatteryStats.ControllerActivityCounter getBluetoothControllerActivity() {
            return this.mBluetoothControllerActivity;
        }

        public BatteryStats.ControllerActivityCounter getModemControllerActivity() {
            return this.mModemControllerActivity;
        }

        public ControllerActivityCounterImpl getOrCreateWifiControllerActivityLocked() {
            if (this.mWifiControllerActivity == null) {
                this.mWifiControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 1);
            }
            return this.mWifiControllerActivity;
        }

        public ControllerActivityCounterImpl getOrCreateBluetoothControllerActivityLocked() {
            if (this.mBluetoothControllerActivity == null) {
                this.mBluetoothControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 1);
            }
            return this.mBluetoothControllerActivity;
        }

        public ControllerActivityCounterImpl getOrCreateModemControllerActivityLocked() {
            if (this.mModemControllerActivity == null) {
                this.mModemControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 5);
            }
            return this.mModemControllerActivity;
        }

        public StopwatchTimer createAudioTurnedOnTimerLocked() {
            if (this.mAudioTurnedOnTimer == null) {
                StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 15, this.mBsi.mAudioTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
                this.mAudioTurnedOnTimer = stopwatchTimer;
            }
            return this.mAudioTurnedOnTimer;
        }

        public void noteAudioTurnedOnLocked(long elapsedRealtimeMs) {
            createAudioTurnedOnTimerLocked().startRunningLocked(elapsedRealtimeMs);
        }

        public void noteAudioTurnedOffLocked(long elapsedRealtimeMs) {
            if (this.mAudioTurnedOnTimer != null) {
                this.mAudioTurnedOnTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteResetAudioLocked(long elapsedRealtimeMs) {
            if (this.mAudioTurnedOnTimer != null) {
                this.mAudioTurnedOnTimer.stopAllRunningLocked(elapsedRealtimeMs);
            }
        }

        public StopwatchTimer createVideoTurnedOnTimerLocked() {
            if (this.mVideoTurnedOnTimer == null) {
                StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 8, this.mBsi.mVideoTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
                this.mVideoTurnedOnTimer = stopwatchTimer;
            }
            return this.mVideoTurnedOnTimer;
        }

        public void noteVideoTurnedOnLocked(long elapsedRealtimeMs) {
            createVideoTurnedOnTimerLocked().startRunningLocked(elapsedRealtimeMs);
        }

        public void noteVideoTurnedOffLocked(long elapsedRealtimeMs) {
            if (this.mVideoTurnedOnTimer != null) {
                this.mVideoTurnedOnTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteResetVideoLocked(long elapsedRealtimeMs) {
            if (this.mVideoTurnedOnTimer != null) {
                this.mVideoTurnedOnTimer.stopAllRunningLocked(elapsedRealtimeMs);
            }
        }

        public StopwatchTimer createFlashlightTurnedOnTimerLocked() {
            if (this.mFlashlightTurnedOnTimer == null) {
                StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 16, this.mBsi.mFlashlightTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
                this.mFlashlightTurnedOnTimer = stopwatchTimer;
            }
            return this.mFlashlightTurnedOnTimer;
        }

        public void noteFlashlightTurnedOnLocked(long elapsedRealtimeMs) {
            createFlashlightTurnedOnTimerLocked().startRunningLocked(elapsedRealtimeMs);
        }

        public void noteFlashlightTurnedOffLocked(long elapsedRealtimeMs) {
            if (this.mFlashlightTurnedOnTimer != null) {
                this.mFlashlightTurnedOnTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteResetFlashlightLocked(long elapsedRealtimeMs) {
            if (this.mFlashlightTurnedOnTimer != null) {
                this.mFlashlightTurnedOnTimer.stopAllRunningLocked(elapsedRealtimeMs);
            }
        }

        public StopwatchTimer createCameraTurnedOnTimerLocked() {
            if (this.mCameraTurnedOnTimer == null) {
                StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 17, this.mBsi.mCameraTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
                this.mCameraTurnedOnTimer = stopwatchTimer;
            }
            return this.mCameraTurnedOnTimer;
        }

        public void noteCameraTurnedOnLocked(long elapsedRealtimeMs) {
            createCameraTurnedOnTimerLocked().startRunningLocked(elapsedRealtimeMs);
        }

        public void noteCameraTurnedOffLocked(long elapsedRealtimeMs) {
            if (this.mCameraTurnedOnTimer != null) {
                this.mCameraTurnedOnTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteResetCameraLocked(long elapsedRealtimeMs) {
            if (this.mCameraTurnedOnTimer != null) {
                this.mCameraTurnedOnTimer.stopAllRunningLocked(elapsedRealtimeMs);
            }
        }

        public StopwatchTimer createForegroundActivityTimerLocked() {
            if (this.mForegroundActivityTimer == null) {
                StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 10, null, this.mBsi.mOnBatteryTimeBase);
                this.mForegroundActivityTimer = stopwatchTimer;
            }
            return this.mForegroundActivityTimer;
        }

        public StopwatchTimer createForegroundServiceTimerLocked() {
            if (this.mForegroundServiceTimer == null) {
                StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 22, null, this.mBsi.mOnBatteryTimeBase);
                this.mForegroundServiceTimer = stopwatchTimer;
            }
            return this.mForegroundServiceTimer;
        }

        public DualTimer createAggregatedPartialWakelockTimerLocked() {
            if (this.mAggregatedPartialWakelockTimer == null) {
                DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this, 20, null, this.mBsi.mOnBatteryScreenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase);
                this.mAggregatedPartialWakelockTimer = dualTimer;
            }
            return this.mAggregatedPartialWakelockTimer;
        }

        public DualTimer createBluetoothScanTimerLocked() {
            if (this.mBluetoothScanTimer == null) {
                DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this, 19, this.mBsi.mBluetoothScanOnTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
                this.mBluetoothScanTimer = dualTimer;
            }
            return this.mBluetoothScanTimer;
        }

        public DualTimer createBluetoothUnoptimizedScanTimerLocked() {
            if (this.mBluetoothUnoptimizedScanTimer == null) {
                DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this, 21, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
                this.mBluetoothUnoptimizedScanTimer = dualTimer;
            }
            return this.mBluetoothUnoptimizedScanTimer;
        }

        public void noteBluetoothScanStartedLocked(long elapsedRealtimeMs, boolean isUnoptimized) {
            createBluetoothScanTimerLocked().startRunningLocked(elapsedRealtimeMs);
            if (isUnoptimized) {
                createBluetoothUnoptimizedScanTimerLocked().startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteBluetoothScanStoppedLocked(long elapsedRealtimeMs, boolean isUnoptimized) {
            if (this.mBluetoothScanTimer != null) {
                this.mBluetoothScanTimer.stopRunningLocked(elapsedRealtimeMs);
            }
            if (isUnoptimized && this.mBluetoothUnoptimizedScanTimer != null) {
                this.mBluetoothUnoptimizedScanTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteResetBluetoothScanLocked(long elapsedRealtimeMs) {
            if (this.mBluetoothScanTimer != null) {
                this.mBluetoothScanTimer.stopAllRunningLocked(elapsedRealtimeMs);
            }
            if (this.mBluetoothUnoptimizedScanTimer != null) {
                this.mBluetoothUnoptimizedScanTimer.stopAllRunningLocked(elapsedRealtimeMs);
            }
        }

        public Counter createBluetoothScanResultCounterLocked() {
            if (this.mBluetoothScanResultCounter == null) {
                this.mBluetoothScanResultCounter = new Counter(this.mBsi.mOnBatteryTimeBase);
            }
            return this.mBluetoothScanResultCounter;
        }

        public Counter createBluetoothScanResultBgCounterLocked() {
            if (this.mBluetoothScanResultBgCounter == null) {
                this.mBluetoothScanResultBgCounter = new Counter(this.mOnBatteryBackgroundTimeBase);
            }
            return this.mBluetoothScanResultBgCounter;
        }

        public void noteBluetoothScanResultsLocked(int numNewResults) {
            createBluetoothScanResultCounterLocked().addAtomic(numNewResults);
            createBluetoothScanResultBgCounterLocked().addAtomic(numNewResults);
        }

        public void noteActivityResumedLocked(long elapsedRealtimeMs) {
            createForegroundActivityTimerLocked().startRunningLocked(elapsedRealtimeMs);
        }

        public void noteActivityPausedLocked(long elapsedRealtimeMs) {
            if (this.mForegroundActivityTimer != null) {
                this.mForegroundActivityTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteForegroundServiceResumedLocked(long elapsedRealtimeMs) {
            createForegroundServiceTimerLocked().startRunningLocked(elapsedRealtimeMs);
        }

        public void noteForegroundServicePausedLocked(long elapsedRealtimeMs) {
            if (this.mForegroundServiceTimer != null) {
                this.mForegroundServiceTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public BatchTimer createVibratorOnTimerLocked() {
            if (this.mVibratorOnTimer == null) {
                this.mVibratorOnTimer = new BatchTimer(this.mBsi.mClocks, this, 9, this.mBsi.mOnBatteryTimeBase);
            }
            return this.mVibratorOnTimer;
        }

        public void noteVibratorOnLocked(long durationMillis) {
            createVibratorOnTimerLocked().addDuration(this.mBsi, durationMillis);
        }

        public void noteVibratorOffLocked() {
            if (this.mVibratorOnTimer != null) {
                this.mVibratorOnTimer.abortLastDuration(this.mBsi);
            }
        }

        public long getWifiRunningTime(long elapsedRealtimeUs, int which) {
            if (this.mWifiRunningTimer == null) {
                return 0;
            }
            return this.mWifiRunningTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        public long getFullWifiLockTime(long elapsedRealtimeUs, int which) {
            if (this.mFullWifiLockTimer == null) {
                return 0;
            }
            return this.mFullWifiLockTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        public long getWifiScanTime(long elapsedRealtimeUs, int which) {
            if (this.mWifiScanTimer == null) {
                return 0;
            }
            return this.mWifiScanTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        public int getWifiScanCount(int which) {
            if (this.mWifiScanTimer == null) {
                return 0;
            }
            return this.mWifiScanTimer.getCountLocked(which);
        }

        public Timer getWifiScanTimer() {
            return this.mWifiScanTimer;
        }

        public int getWifiScanBackgroundCount(int which) {
            if (this.mWifiScanTimer == null || this.mWifiScanTimer.getSubTimer() == null) {
                return 0;
            }
            return this.mWifiScanTimer.getSubTimer().getCountLocked(which);
        }

        public long getWifiScanActualTime(long elapsedRealtimeUs) {
            if (this.mWifiScanTimer == null) {
                return 0;
            }
            return this.mWifiScanTimer.getTotalDurationMsLocked((500 + elapsedRealtimeUs) / 1000) * 1000;
        }

        public long getWifiScanBackgroundTime(long elapsedRealtimeUs) {
            if (this.mWifiScanTimer == null || this.mWifiScanTimer.getSubTimer() == null) {
                return 0;
            }
            return this.mWifiScanTimer.getSubTimer().getTotalDurationMsLocked((500 + elapsedRealtimeUs) / 1000) * 1000;
        }

        public Timer getWifiScanBackgroundTimer() {
            if (this.mWifiScanTimer == null) {
                return null;
            }
            return this.mWifiScanTimer.getSubTimer();
        }

        public long getWifiBatchedScanTime(int csphBin, long elapsedRealtimeUs, int which) {
            if (csphBin < 0 || csphBin >= 5 || this.mWifiBatchedScanTimer[csphBin] == null) {
                return 0;
            }
            return this.mWifiBatchedScanTimer[csphBin].getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        public int getWifiBatchedScanCount(int csphBin, int which) {
            if (csphBin < 0 || csphBin >= 5 || this.mWifiBatchedScanTimer[csphBin] == null) {
                return 0;
            }
            return this.mWifiBatchedScanTimer[csphBin].getCountLocked(which);
        }

        public long getWifiMulticastTime(long elapsedRealtimeUs, int which) {
            if (this.mWifiMulticastTimer == null) {
                return 0;
            }
            return this.mWifiMulticastTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        public Timer getAudioTurnedOnTimer() {
            return this.mAudioTurnedOnTimer;
        }

        public Timer getVideoTurnedOnTimer() {
            return this.mVideoTurnedOnTimer;
        }

        public Timer getFlashlightTurnedOnTimer() {
            return this.mFlashlightTurnedOnTimer;
        }

        public Timer getCameraTurnedOnTimer() {
            return this.mCameraTurnedOnTimer;
        }

        public Timer getForegroundActivityTimer() {
            return this.mForegroundActivityTimer;
        }

        public Timer getForegroundServiceTimer() {
            return this.mForegroundServiceTimer;
        }

        public Timer getBluetoothScanTimer() {
            return this.mBluetoothScanTimer;
        }

        public Timer getBluetoothScanBackgroundTimer() {
            if (this.mBluetoothScanTimer == null) {
                return null;
            }
            return this.mBluetoothScanTimer.getSubTimer();
        }

        public Timer getBluetoothUnoptimizedScanTimer() {
            return this.mBluetoothUnoptimizedScanTimer;
        }

        public Timer getBluetoothUnoptimizedScanBackgroundTimer() {
            if (this.mBluetoothUnoptimizedScanTimer == null) {
                return null;
            }
            return this.mBluetoothUnoptimizedScanTimer.getSubTimer();
        }

        public Counter getBluetoothScanResultCounter() {
            return this.mBluetoothScanResultCounter;
        }

        public Counter getBluetoothScanResultBgCounter() {
            return this.mBluetoothScanResultBgCounter;
        }

        /* access modifiers changed from: package-private */
        public void makeProcessState(int i, Parcel in) {
            if (i >= 0 && i < 7) {
                if (in == null) {
                    StopwatchTimer[] stopwatchTimerArr = this.mProcessStateTimer;
                    StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 12, null, this.mBsi.mOnBatteryTimeBase);
                    stopwatchTimerArr[i] = stopwatchTimer;
                } else {
                    StopwatchTimer[] stopwatchTimerArr2 = this.mProcessStateTimer;
                    StopwatchTimer stopwatchTimer2 = new StopwatchTimer(this.mBsi.mClocks, this, 12, null, this.mBsi.mOnBatteryTimeBase, in);
                    stopwatchTimerArr2[i] = stopwatchTimer2;
                }
            }
        }

        public long getProcessStateTime(int state, long elapsedRealtimeUs, int which) {
            if (state < 0 || state >= 7 || this.mProcessStateTimer[state] == null) {
                return 0;
            }
            return this.mProcessStateTimer[state].getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        public Timer getProcessStateTimer(int state) {
            if (state < 0 || state >= 7) {
                return null;
            }
            return this.mProcessStateTimer[state];
        }

        public Timer getVibratorOnTimer() {
            return this.mVibratorOnTimer;
        }

        public void noteUserActivityLocked(int type) {
            if (this.mUserActivityCounters == null) {
                initUserActivityLocked();
            }
            if (type < 0 || type >= 4) {
                Slog.w(BatteryStatsImpl.TAG, "Unknown user activity type " + type + " was specified.", new Throwable());
                return;
            }
            this.mUserActivityCounters[type].stepAtomic();
        }

        public boolean hasUserActivity() {
            return this.mUserActivityCounters != null;
        }

        public int getUserActivityCount(int type, int which) {
            if (this.mUserActivityCounters == null) {
                return 0;
            }
            return this.mUserActivityCounters[type].getCountLocked(which);
        }

        /* access modifiers changed from: package-private */
        public void makeWifiBatchedScanBin(int i, Parcel in) {
            if (i >= 0 && i < 5) {
                ArrayList<StopwatchTimer> collected = this.mBsi.mWifiBatchedScanTimers.get(i);
                if (collected == null) {
                    collected = new ArrayList<>();
                    this.mBsi.mWifiBatchedScanTimers.put(i, collected);
                }
                if (in == null) {
                    StopwatchTimer[] stopwatchTimerArr = this.mWifiBatchedScanTimer;
                    StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 11, collected, this.mBsi.mOnBatteryTimeBase);
                    stopwatchTimerArr[i] = stopwatchTimer;
                } else {
                    StopwatchTimer[] stopwatchTimerArr2 = this.mWifiBatchedScanTimer;
                    StopwatchTimer stopwatchTimer2 = new StopwatchTimer(this.mBsi.mClocks, this, 11, collected, this.mBsi.mOnBatteryTimeBase, in);
                    stopwatchTimerArr2[i] = stopwatchTimer2;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void initUserActivityLocked() {
            this.mUserActivityCounters = new Counter[4];
            for (int i = 0; i < 4; i++) {
                this.mUserActivityCounters[i] = new Counter(this.mBsi.mOnBatteryTimeBase);
            }
        }

        /* access modifiers changed from: package-private */
        public void noteNetworkActivityLocked(int type, long deltaBytes, long deltaPackets) {
            if (this.mNetworkByteActivityCounters == null) {
                initNetworkActivityLocked();
            }
            if (type < 0 || type >= 10) {
                Slog.w(BatteryStatsImpl.TAG, "Unknown network activity type " + type + " was specified.", new Throwable());
                return;
            }
            this.mNetworkByteActivityCounters[type].addCountLocked(deltaBytes);
            this.mNetworkPacketActivityCounters[type].addCountLocked(deltaPackets);
        }

        /* access modifiers changed from: package-private */
        public void noteMobileRadioActiveTimeLocked(long batteryUptime) {
            if (this.mNetworkByteActivityCounters == null) {
                initNetworkActivityLocked();
            }
            this.mMobileRadioActiveTime.addCountLocked(batteryUptime);
            this.mMobileRadioActiveCount.addCountLocked(1);
        }

        public boolean hasNetworkActivity() {
            return this.mNetworkByteActivityCounters != null;
        }

        public long getNetworkActivityBytes(int type, int which) {
            if (this.mNetworkByteActivityCounters == null || type < 0 || type >= this.mNetworkByteActivityCounters.length) {
                return 0;
            }
            return this.mNetworkByteActivityCounters[type].getCountLocked(which);
        }

        public long getNetworkActivityPackets(int type, int which) {
            if (this.mNetworkPacketActivityCounters == null || type < 0 || type >= this.mNetworkPacketActivityCounters.length) {
                return 0;
            }
            return this.mNetworkPacketActivityCounters[type].getCountLocked(which);
        }

        public long getMobileRadioActiveTime(int which) {
            if (this.mMobileRadioActiveTime != null) {
                return this.mMobileRadioActiveTime.getCountLocked(which);
            }
            return 0;
        }

        public int getMobileRadioActiveCount(int which) {
            if (this.mMobileRadioActiveCount != null) {
                return (int) this.mMobileRadioActiveCount.getCountLocked(which);
            }
            return 0;
        }

        public long getUserCpuTimeUs(int which) {
            return this.mUserCpuTime.getCountLocked(which);
        }

        public long getSystemCpuTimeUs(int which) {
            return this.mSystemCpuTime.getCountLocked(which);
        }

        public long getTimeAtCpuSpeed(int cluster, int step, int which) {
            if (this.mCpuClusterSpeedTimesUs != null && cluster >= 0 && cluster < this.mCpuClusterSpeedTimesUs.length) {
                LongSamplingCounter[] cpuSpeedTimesUs = this.mCpuClusterSpeedTimesUs[cluster];
                if (cpuSpeedTimesUs != null && step >= 0 && step < cpuSpeedTimesUs.length) {
                    LongSamplingCounter c = cpuSpeedTimesUs[step];
                    if (c != null) {
                        return c.getCountLocked(which);
                    }
                }
            }
            return 0;
        }

        public void noteMobileRadioApWakeupLocked() {
            if (this.mMobileRadioApWakeupCount == null) {
                this.mMobileRadioApWakeupCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            }
            this.mMobileRadioApWakeupCount.addCountLocked(1);
        }

        public long getMobileRadioApWakeupCount(int which) {
            if (this.mMobileRadioApWakeupCount != null) {
                return this.mMobileRadioApWakeupCount.getCountLocked(which);
            }
            return 0;
        }

        public void noteWifiRadioApWakeupLocked() {
            if (this.mWifiRadioApWakeupCount == null) {
                this.mWifiRadioApWakeupCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            }
            this.mWifiRadioApWakeupCount.addCountLocked(1);
        }

        public long getWifiRadioApWakeupCount(int which) {
            if (this.mWifiRadioApWakeupCount != null) {
                return this.mWifiRadioApWakeupCount.getCountLocked(which);
            }
            return 0;
        }

        public void getDeferredJobsCheckinLineLocked(StringBuilder sb, int which) {
            sb.setLength(0);
            int deferredEventCount = this.mJobsDeferredEventCount.getCountLocked(which);
            if (deferredEventCount != 0) {
                int deferredCount = this.mJobsDeferredCount.getCountLocked(which);
                long totalLatency = this.mJobsFreshnessTimeMs.getCountLocked(which);
                sb.append(deferredEventCount);
                sb.append(',');
                sb.append(deferredCount);
                sb.append(',');
                sb.append(totalLatency);
                for (int i = 0; i < BatteryStats.JOB_FRESHNESS_BUCKETS.length; i++) {
                    if (this.mJobsFreshnessBuckets[i] == null) {
                        sb.append(",0");
                    } else {
                        sb.append(",");
                        sb.append(this.mJobsFreshnessBuckets[i].getCountLocked(which));
                    }
                }
            }
        }

        public void getDeferredJobsLineLocked(StringBuilder sb, int which) {
            sb.setLength(0);
            int deferredEventCount = this.mJobsDeferredEventCount.getCountLocked(which);
            if (deferredEventCount != 0) {
                int deferredCount = this.mJobsDeferredCount.getCountLocked(which);
                long totalLatency = this.mJobsFreshnessTimeMs.getCountLocked(which);
                sb.append("times=");
                sb.append(deferredEventCount);
                sb.append(", ");
                sb.append("count=");
                sb.append(deferredCount);
                sb.append(", ");
                sb.append("totalLatencyMs=");
                sb.append(totalLatency);
                sb.append(", ");
                for (int i = 0; i < BatteryStats.JOB_FRESHNESS_BUCKETS.length; i++) {
                    sb.append("<");
                    sb.append(BatteryStats.JOB_FRESHNESS_BUCKETS[i]);
                    sb.append("ms=");
                    if (this.mJobsFreshnessBuckets[i] == null) {
                        sb.append("0");
                    } else {
                        sb.append(this.mJobsFreshnessBuckets[i].getCountLocked(which));
                    }
                    sb.append(" ");
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void initNetworkActivityLocked() {
            this.mNetworkByteActivityCounters = new LongSamplingCounter[10];
            this.mNetworkPacketActivityCounters = new LongSamplingCounter[10];
            for (int i = 0; i < 10; i++) {
                this.mNetworkByteActivityCounters[i] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
                this.mNetworkPacketActivityCounters[i] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            }
            this.mMobileRadioActiveTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            this.mMobileRadioActiveCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
        }

        /* JADX WARNING: type inference failed for: r7v9, types: [com.android.internal.os.BatteryStatsImpl$DualTimer, com.android.internal.os.BatteryStatsImpl$StopwatchTimer, com.android.internal.os.BatteryStatsImpl$Counter] */
        /* JADX WARNING: type inference failed for: r7v41 */
        /* JADX WARNING: type inference failed for: r7v43 */
        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public boolean reset(long uptime, long realtime) {
            ? r7;
            long j = uptime;
            long j2 = realtime;
            boolean active = false;
            this.mOnBatteryBackgroundTimeBase.init(j, j2);
            this.mOnBatteryScreenOffBackgroundTimeBase.init(j, j2);
            if (this.mWifiRunningTimer != null) {
                active = false | (!this.mWifiRunningTimer.reset(false)) | this.mWifiRunning;
            }
            if (this.mFullWifiLockTimer != null) {
                active = active | (!this.mFullWifiLockTimer.reset(false)) | this.mFullWifiLockOut;
            }
            if (this.mWifiScanTimer != null) {
                active = active | (!this.mWifiScanTimer.reset(false)) | this.mWifiScanStarted;
            }
            if (this.mWifiBatchedScanTimer != null) {
                boolean active2 = active;
                for (int i = 0; i < 5; i++) {
                    if (this.mWifiBatchedScanTimer[i] != null) {
                        active2 |= !this.mWifiBatchedScanTimer[i].reset(false);
                    }
                }
                active = (this.mWifiBatchedScanBinStarted != -1) | active2;
            }
            if (this.mWifiMulticastTimer != null) {
                active = active | (!this.mWifiMulticastTimer.reset(false)) | this.mWifiMulticastEnabled;
            }
            boolean active3 = active | (!BatteryStatsImpl.resetTimerIfNotNull((Timer) this.mAudioTurnedOnTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull((Timer) this.mVideoTurnedOnTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull((Timer) this.mFlashlightTurnedOnTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull((Timer) this.mCameraTurnedOnTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull((Timer) this.mForegroundActivityTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull((Timer) this.mForegroundServiceTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull(this.mAggregatedPartialWakelockTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull(this.mBluetoothScanTimer, false)) | (!BatteryStatsImpl.resetTimerIfNotNull(this.mBluetoothUnoptimizedScanTimer, false));
            if (this.mBluetoothScanResultCounter != null) {
                this.mBluetoothScanResultCounter.reset(false);
            }
            if (this.mBluetoothScanResultBgCounter != null) {
                this.mBluetoothScanResultBgCounter.reset(false);
            }
            if (this.mProcessStateTimer != null) {
                boolean active4 = active3;
                for (int i2 = 0; i2 < 7; i2++) {
                    if (this.mProcessStateTimer[i2] != null) {
                        active4 |= !this.mProcessStateTimer[i2].reset(false);
                    }
                }
                active3 = (this.mProcessState != 19) | active4;
            }
            if (this.mVibratorOnTimer != null) {
                if (this.mVibratorOnTimer.reset(false)) {
                    this.mVibratorOnTimer.detach();
                    this.mVibratorOnTimer = null;
                } else {
                    active3 = true;
                }
            }
            if (this.mUserActivityCounters != null) {
                for (int i3 = 0; i3 < 4; i3++) {
                    this.mUserActivityCounters[i3].reset(false);
                }
            }
            if (this.mNetworkByteActivityCounters != null) {
                for (int i4 = 0; i4 < 10; i4++) {
                    this.mNetworkByteActivityCounters[i4].reset(false);
                    this.mNetworkPacketActivityCounters[i4].reset(false);
                }
                this.mMobileRadioActiveTime.reset(false);
                this.mMobileRadioActiveCount.reset(false);
            }
            if (this.mWifiControllerActivity != null) {
                this.mWifiControllerActivity.reset(false);
            }
            if (this.mBluetoothControllerActivity != null) {
                this.mBluetoothControllerActivity.reset(false);
            }
            if (this.mModemControllerActivity != null) {
                this.mModemControllerActivity.reset(false);
            }
            this.mUserCpuTime.reset(false);
            this.mSystemCpuTime.reset(false);
            if (this.mCpuClusterSpeedTimesUs != null) {
                for (LongSamplingCounter[] speeds : this.mCpuClusterSpeedTimesUs) {
                    if (speeds != null) {
                        for (LongSamplingCounter speed : speeds) {
                            if (speed != null) {
                                speed.reset(false);
                            }
                        }
                    }
                }
            }
            if (this.mCpuFreqTimeMs != null) {
                this.mCpuFreqTimeMs.reset(false);
            }
            if (this.mScreenOffCpuFreqTimeMs != null) {
                this.mScreenOffCpuFreqTimeMs.reset(false);
            }
            this.mCpuActiveTimeMs.reset(false);
            this.mCpuClusterTimesMs.reset(false);
            if (this.mProcStateTimeMs != null) {
                for (LongSamplingCounterArray counters : this.mProcStateTimeMs) {
                    if (counters != null) {
                        counters.reset(false);
                    }
                }
            }
            if (this.mProcStateScreenOffTimeMs != null) {
                for (LongSamplingCounterArray counters2 : this.mProcStateScreenOffTimeMs) {
                    if (counters2 != null) {
                        counters2.reset(false);
                    }
                }
            }
            BatteryStatsImpl.resetLongCounterIfNotNull(this.mMobileRadioApWakeupCount, false);
            BatteryStatsImpl.resetLongCounterIfNotNull(this.mWifiRadioApWakeupCount, false);
            ArrayMap<String, Wakelock> wakeStats = this.mWakelockStats.getMap();
            for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                if (wakeStats.valueAt(iw).reset()) {
                    wakeStats.removeAt(iw);
                } else {
                    active3 = true;
                }
            }
            this.mWakelockStats.cleanup();
            ArrayMap<String, DualTimer> syncStats = this.mSyncStats.getMap();
            for (int is = syncStats.size() - 1; is >= 0; is--) {
                DualTimer timer = syncStats.valueAt(is);
                if (timer.reset(false)) {
                    syncStats.removeAt(is);
                    timer.detach();
                } else {
                    active3 = true;
                }
            }
            this.mSyncStats.cleanup();
            ArrayMap<String, DualTimer> jobStats = this.mJobStats.getMap();
            for (int ij = jobStats.size() - 1; ij >= 0; ij--) {
                DualTimer timer2 = jobStats.valueAt(ij);
                if (timer2.reset(false)) {
                    jobStats.removeAt(ij);
                    timer2.detach();
                } else {
                    active3 = true;
                }
            }
            this.mJobStats.cleanup();
            this.mJobCompletions.clear();
            this.mJobsDeferredEventCount.reset(false);
            this.mJobsDeferredCount.reset(false);
            this.mJobsFreshnessTimeMs.reset(false);
            for (int ij2 = 0; ij2 < BatteryStats.JOB_FRESHNESS_BUCKETS.length; ij2++) {
                if (this.mJobsFreshnessBuckets[ij2] != null) {
                    this.mJobsFreshnessBuckets[ij2].reset(false);
                }
            }
            for (int ise = this.mSensorStats.size() - 1; ise >= 0; ise--) {
                if (this.mSensorStats.valueAt(ise).reset()) {
                    this.mSensorStats.removeAt(ise);
                } else {
                    active3 = true;
                }
            }
            for (int ip = this.mProcessStats.size() - 1; ip >= 0; ip--) {
                this.mProcessStats.valueAt(ip).detach();
            }
            this.mProcessStats.clear();
            if (this.mPids.size() > 0) {
                for (int i5 = this.mPids.size() - 1; i5 >= 0; i5--) {
                    if (this.mPids.valueAt(i5).mWakeNesting > 0) {
                        active3 = true;
                    } else {
                        this.mPids.removeAt(i5);
                    }
                }
            }
            if (this.mPackageStats.size() > 0) {
                for (Map.Entry<String, Pkg> pkgEntry : this.mPackageStats.entrySet()) {
                    Pkg p = pkgEntry.getValue();
                    p.detach();
                    if (p.mServiceStats.size() > 0) {
                        for (Map.Entry<String, Pkg.Serv> servEntry : p.mServiceStats.entrySet()) {
                            servEntry.getValue().detach();
                        }
                    }
                }
                this.mPackageStats.clear();
            }
            this.mLastStepSystemTime = 0;
            this.mLastStepUserTime = 0;
            this.mCurStepSystemTime = 0;
            this.mCurStepUserTime = 0;
            if (!active3) {
                if (this.mWifiRunningTimer != null) {
                    this.mWifiRunningTimer.detach();
                }
                if (this.mFullWifiLockTimer != null) {
                    this.mFullWifiLockTimer.detach();
                }
                if (this.mWifiScanTimer != null) {
                    this.mWifiScanTimer.detach();
                }
                for (int i6 = 0; i6 < 5; i6++) {
                    if (this.mWifiBatchedScanTimer[i6] != null) {
                        this.mWifiBatchedScanTimer[i6].detach();
                    }
                }
                if (this.mWifiMulticastTimer != null) {
                    this.mWifiMulticastTimer.detach();
                }
                if (this.mAudioTurnedOnTimer != null) {
                    this.mAudioTurnedOnTimer.detach();
                    r7 = 0;
                    this.mAudioTurnedOnTimer = null;
                } else {
                    r7 = 0;
                }
                if (this.mVideoTurnedOnTimer != null) {
                    this.mVideoTurnedOnTimer.detach();
                    this.mVideoTurnedOnTimer = r7;
                }
                if (this.mFlashlightTurnedOnTimer != null) {
                    this.mFlashlightTurnedOnTimer.detach();
                    this.mFlashlightTurnedOnTimer = r7;
                }
                if (this.mCameraTurnedOnTimer != null) {
                    this.mCameraTurnedOnTimer.detach();
                    this.mCameraTurnedOnTimer = r7;
                }
                if (this.mForegroundActivityTimer != null) {
                    this.mForegroundActivityTimer.detach();
                    this.mForegroundActivityTimer = r7;
                }
                if (this.mForegroundServiceTimer != null) {
                    this.mForegroundServiceTimer.detach();
                    this.mForegroundServiceTimer = r7;
                }
                if (this.mAggregatedPartialWakelockTimer != null) {
                    this.mAggregatedPartialWakelockTimer.detach();
                    this.mAggregatedPartialWakelockTimer = r7;
                }
                if (this.mBluetoothScanTimer != null) {
                    this.mBluetoothScanTimer.detach();
                    this.mBluetoothScanTimer = r7;
                }
                if (this.mBluetoothUnoptimizedScanTimer != null) {
                    this.mBluetoothUnoptimizedScanTimer.detach();
                    this.mBluetoothUnoptimizedScanTimer = r7;
                }
                if (this.mBluetoothScanResultCounter != null) {
                    this.mBluetoothScanResultCounter.detach();
                    this.mBluetoothScanResultCounter = r7;
                }
                if (this.mBluetoothScanResultBgCounter != null) {
                    this.mBluetoothScanResultBgCounter.detach();
                    this.mBluetoothScanResultBgCounter = r7;
                }
                if (this.mUserActivityCounters != null) {
                    for (int i7 = 0; i7 < 4; i7++) {
                        this.mUserActivityCounters[i7].detach();
                    }
                }
                if (this.mNetworkByteActivityCounters != null) {
                    for (int i8 = 0; i8 < 10; i8++) {
                        this.mNetworkByteActivityCounters[i8].detach();
                        this.mNetworkPacketActivityCounters[i8].detach();
                    }
                }
                if (this.mWifiControllerActivity != null) {
                    this.mWifiControllerActivity.detach();
                }
                if (this.mBluetoothControllerActivity != null) {
                    this.mBluetoothControllerActivity.detach();
                }
                if (this.mModemControllerActivity != null) {
                    this.mModemControllerActivity.detach();
                }
                this.mPids.clear();
                this.mUserCpuTime.detach();
                this.mSystemCpuTime.detach();
                if (this.mCpuClusterSpeedTimesUs != null) {
                    for (LongSamplingCounter[] cpuSpeeds : this.mCpuClusterSpeedTimesUs) {
                        if (cpuSpeeds != null) {
                            for (LongSamplingCounter c : cpuSpeeds) {
                                if (c != null) {
                                    c.detach();
                                }
                            }
                        }
                    }
                }
                if (this.mCpuFreqTimeMs != null) {
                    this.mCpuFreqTimeMs.detach();
                }
                if (this.mScreenOffCpuFreqTimeMs != null) {
                    this.mScreenOffCpuFreqTimeMs.detach();
                }
                this.mCpuActiveTimeMs.detach();
                this.mCpuClusterTimesMs.detach();
                if (this.mProcStateTimeMs != null) {
                    for (LongSamplingCounterArray counters3 : this.mProcStateTimeMs) {
                        if (counters3 != null) {
                            counters3.detach();
                        }
                    }
                }
                if (this.mProcStateScreenOffTimeMs != null) {
                    for (LongSamplingCounterArray counters4 : this.mProcStateScreenOffTimeMs) {
                        if (counters4 != null) {
                            counters4.detach();
                        }
                    }
                }
                BatteryStatsImpl.detachLongCounterIfNotNull(this.mMobileRadioApWakeupCount);
                BatteryStatsImpl.detachLongCounterIfNotNull(this.mWifiRadioApWakeupCount);
            }
            return !active3;
        }

        /* access modifiers changed from: package-private */
        public void writeJobCompletionsToParcelLocked(Parcel out) {
            int NJC = this.mJobCompletions.size();
            out.writeInt(NJC);
            for (int ijc = 0; ijc < NJC; ijc++) {
                out.writeString(this.mJobCompletions.keyAt(ijc));
                SparseIntArray types = this.mJobCompletions.valueAt(ijc);
                int NT = types.size();
                out.writeInt(NT);
                for (int it = 0; it < NT; it++) {
                    out.writeInt(types.keyAt(it));
                    out.writeInt(types.valueAt(it));
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeToParcelLocked(Parcel out, long uptimeUs, long elapsedRealtimeUs) {
            ArrayMap<String, DualTimer> syncStats;
            int NW;
            ArrayMap<String, Wakelock> wakeStats;
            ArrayMap<String, DualTimer> syncStats2;
            Parcel parcel = out;
            long j = elapsedRealtimeUs;
            Parcel parcel2 = parcel;
            long j2 = uptimeUs;
            long j3 = j;
            this.mOnBatteryBackgroundTimeBase.writeToParcel(parcel2, j2, j3);
            this.mOnBatteryScreenOffBackgroundTimeBase.writeToParcel(parcel2, j2, j3);
            ArrayMap<String, Wakelock> wakeStats2 = this.mWakelockStats.getMap();
            int NW2 = wakeStats2.size();
            parcel.writeInt(NW2);
            int i = 0;
            for (int iw = 0; iw < NW2; iw++) {
                parcel.writeString(wakeStats2.keyAt(iw));
                wakeStats2.valueAt(iw).writeToParcelLocked(parcel, j);
            }
            ArrayMap<String, DualTimer> syncStats3 = this.mSyncStats.getMap();
            int NS = syncStats3.size();
            parcel.writeInt(NS);
            for (int is = 0; is < NS; is++) {
                parcel.writeString(syncStats3.keyAt(is));
                Timer.writeTimerToParcel(parcel, syncStats3.valueAt(is), j);
            }
            ArrayMap<String, DualTimer> jobStats = this.mJobStats.getMap();
            int NJ = jobStats.size();
            parcel.writeInt(NJ);
            for (int ij = 0; ij < NJ; ij++) {
                parcel.writeString(jobStats.keyAt(ij));
                Timer.writeTimerToParcel(parcel, jobStats.valueAt(ij), j);
            }
            writeJobCompletionsToParcelLocked(out);
            this.mJobsDeferredEventCount.writeToParcel(parcel);
            this.mJobsDeferredCount.writeToParcel(parcel);
            this.mJobsFreshnessTimeMs.writeToParcel(parcel);
            for (int i2 = 0; i2 < BatteryStats.JOB_FRESHNESS_BUCKETS.length; i2++) {
                Counter.writeCounterToParcel(parcel, this.mJobsFreshnessBuckets[i2]);
            }
            int NSE = this.mSensorStats.size();
            parcel.writeInt(NSE);
            for (int ise = 0; ise < NSE; ise++) {
                parcel.writeInt(this.mSensorStats.keyAt(ise));
                this.mSensorStats.valueAt(ise).writeToParcelLocked(parcel, j);
            }
            int NP = this.mProcessStats.size();
            parcel.writeInt(NP);
            for (int ip = 0; ip < NP; ip++) {
                parcel.writeString(this.mProcessStats.keyAt(ip));
                this.mProcessStats.valueAt(ip).writeToParcelLocked(parcel);
            }
            parcel.writeInt(this.mPackageStats.size());
            for (Map.Entry<String, Pkg> pkgEntry : this.mPackageStats.entrySet()) {
                parcel.writeString(pkgEntry.getKey());
                pkgEntry.getValue().writeToParcelLocked(parcel);
            }
            if (this.mWifiRunningTimer != null) {
                parcel.writeInt(1);
                this.mWifiRunningTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mFullWifiLockTimer != null) {
                parcel.writeInt(1);
                this.mFullWifiLockTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mWifiScanTimer != null) {
                parcel.writeInt(1);
                this.mWifiScanTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            for (int i3 = 0; i3 < 5; i3++) {
                if (this.mWifiBatchedScanTimer[i3] != null) {
                    parcel.writeInt(1);
                    this.mWifiBatchedScanTimer[i3].writeToParcel(parcel, j);
                } else {
                    parcel.writeInt(0);
                }
            }
            if (this.mWifiMulticastTimer != null) {
                parcel.writeInt(1);
                this.mWifiMulticastTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mAudioTurnedOnTimer != null) {
                parcel.writeInt(1);
                this.mAudioTurnedOnTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mVideoTurnedOnTimer != null) {
                parcel.writeInt(1);
                this.mVideoTurnedOnTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mFlashlightTurnedOnTimer != null) {
                parcel.writeInt(1);
                this.mFlashlightTurnedOnTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mCameraTurnedOnTimer != null) {
                parcel.writeInt(1);
                this.mCameraTurnedOnTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mForegroundActivityTimer != null) {
                parcel.writeInt(1);
                this.mForegroundActivityTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mForegroundServiceTimer != null) {
                parcel.writeInt(1);
                this.mForegroundServiceTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mAggregatedPartialWakelockTimer != null) {
                parcel.writeInt(1);
                this.mAggregatedPartialWakelockTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mBluetoothScanTimer != null) {
                parcel.writeInt(1);
                this.mBluetoothScanTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mBluetoothUnoptimizedScanTimer != null) {
                parcel.writeInt(1);
                this.mBluetoothUnoptimizedScanTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mBluetoothScanResultCounter != null) {
                parcel.writeInt(1);
                this.mBluetoothScanResultCounter.writeToParcel(parcel);
            } else {
                parcel.writeInt(0);
            }
            if (this.mBluetoothScanResultBgCounter != null) {
                parcel.writeInt(1);
                this.mBluetoothScanResultBgCounter.writeToParcel(parcel);
            } else {
                parcel.writeInt(0);
            }
            for (int i4 = 0; i4 < 7; i4++) {
                if (this.mProcessStateTimer[i4] != null) {
                    parcel.writeInt(1);
                    this.mProcessStateTimer[i4].writeToParcel(parcel, j);
                } else {
                    parcel.writeInt(0);
                }
            }
            if (this.mVibratorOnTimer != null) {
                parcel.writeInt(1);
                this.mVibratorOnTimer.writeToParcel(parcel, j);
            } else {
                parcel.writeInt(0);
            }
            if (this.mUserActivityCounters != null) {
                parcel.writeInt(1);
                for (int i5 = 0; i5 < 4; i5++) {
                    this.mUserActivityCounters[i5].writeToParcel(parcel);
                }
            } else {
                parcel.writeInt(0);
            }
            if (this.mNetworkByteActivityCounters != null) {
                parcel.writeInt(1);
                for (int i6 = 0; i6 < 10; i6++) {
                    this.mNetworkByteActivityCounters[i6].writeToParcel(parcel);
                    this.mNetworkPacketActivityCounters[i6].writeToParcel(parcel);
                }
                this.mMobileRadioActiveTime.writeToParcel(parcel);
                this.mMobileRadioActiveCount.writeToParcel(parcel);
            } else {
                parcel.writeInt(0);
            }
            if (this.mWifiControllerActivity != null) {
                parcel.writeInt(1);
                this.mWifiControllerActivity.writeToParcel(parcel, 0);
            } else {
                parcel.writeInt(0);
            }
            if (this.mBluetoothControllerActivity != null) {
                parcel.writeInt(1);
                this.mBluetoothControllerActivity.writeToParcel(parcel, 0);
            } else {
                parcel.writeInt(0);
            }
            if (this.mModemControllerActivity != null) {
                parcel.writeInt(1);
                this.mModemControllerActivity.writeToParcel(parcel, 0);
            } else {
                parcel.writeInt(0);
            }
            this.mUserCpuTime.writeToParcel(parcel);
            this.mSystemCpuTime.writeToParcel(parcel);
            if (this.mCpuClusterSpeedTimesUs != null) {
                parcel.writeInt(1);
                parcel.writeInt(this.mCpuClusterSpeedTimesUs.length);
                LongSamplingCounter[][] longSamplingCounterArr = this.mCpuClusterSpeedTimesUs;
                int length = longSamplingCounterArr.length;
                while (i < length) {
                    LongSamplingCounter[] cpuSpeeds = longSamplingCounterArr[i];
                    if (cpuSpeeds != null) {
                        wakeStats = wakeStats2;
                        parcel.writeInt(1);
                        parcel.writeInt(cpuSpeeds.length);
                        int length2 = cpuSpeeds.length;
                        NW = NW2;
                        int NW3 = 0;
                        while (NW3 < length2) {
                            int i7 = length2;
                            LongSamplingCounter c = cpuSpeeds[NW3];
                            if (c != null) {
                                syncStats2 = syncStats3;
                                parcel.writeInt(1);
                                c.writeToParcel(parcel);
                            } else {
                                syncStats2 = syncStats3;
                                parcel.writeInt(0);
                            }
                            NW3++;
                            length2 = i7;
                            syncStats3 = syncStats2;
                        }
                        syncStats = syncStats3;
                    } else {
                        wakeStats = wakeStats2;
                        NW = NW2;
                        syncStats = syncStats3;
                        parcel.writeInt(0);
                    }
                    i++;
                    wakeStats2 = wakeStats;
                    NW2 = NW;
                    syncStats3 = syncStats;
                }
                int i8 = NW2;
                ArrayMap<String, DualTimer> arrayMap = syncStats3;
            } else {
                int i9 = NW2;
                ArrayMap<String, DualTimer> arrayMap2 = syncStats3;
                parcel.writeInt(0);
            }
            LongSamplingCounterArray.writeToParcel(parcel, this.mCpuFreqTimeMs);
            LongSamplingCounterArray.writeToParcel(parcel, this.mScreenOffCpuFreqTimeMs);
            this.mCpuActiveTimeMs.writeToParcel(parcel);
            this.mCpuClusterTimesMs.writeToParcel(parcel);
            if (this.mProcStateTimeMs != null) {
                parcel.writeInt(this.mProcStateTimeMs.length);
                for (LongSamplingCounterArray counters : this.mProcStateTimeMs) {
                    LongSamplingCounterArray.writeToParcel(parcel, counters);
                }
            } else {
                parcel.writeInt(0);
            }
            if (this.mProcStateScreenOffTimeMs != null) {
                parcel.writeInt(this.mProcStateScreenOffTimeMs.length);
                for (LongSamplingCounterArray counters2 : this.mProcStateScreenOffTimeMs) {
                    LongSamplingCounterArray.writeToParcel(parcel, counters2);
                }
            } else {
                parcel.writeInt(0);
            }
            if (this.mMobileRadioApWakeupCount != null) {
                parcel.writeInt(1);
                this.mMobileRadioApWakeupCount.writeToParcel(parcel);
            } else {
                parcel.writeInt(0);
            }
            if (this.mWifiRadioApWakeupCount != null) {
                parcel.writeInt(1);
                this.mWifiRadioApWakeupCount.writeToParcel(parcel);
                return;
            }
            parcel.writeInt(0);
        }

        /* access modifiers changed from: package-private */
        public void readJobCompletionsFromParcelLocked(Parcel in) {
            int numJobCompletions = in.readInt();
            this.mJobCompletions.clear();
            for (int j = 0; j < numJobCompletions; j++) {
                String jobName = in.readString();
                int numTypes = in.readInt();
                if (numTypes > 0) {
                    SparseIntArray types = new SparseIntArray();
                    for (int k = 0; k < numTypes; k++) {
                        types.put(in.readInt(), in.readInt());
                    }
                    this.mJobCompletions.put(jobName, types);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void readFromParcelLocked(TimeBase timeBase, TimeBase screenOffTimeBase, Parcel in) {
            DualTimer dualTimer;
            DualTimer dualTimer2;
            int procState;
            int numProcs;
            int numJobs;
            int numWakelocks;
            Parcel parcel = in;
            this.mOnBatteryBackgroundTimeBase.readFromParcel(parcel);
            this.mOnBatteryScreenOffBackgroundTimeBase.readFromParcel(parcel);
            int numWakelocks2 = in.readInt();
            this.mWakelockStats.clear();
            for (int j = 0; j < numWakelocks2; j++) {
                String wakelockName = in.readString();
                Wakelock wakelock = new Wakelock(this.mBsi, this);
                wakelock.readFromParcelLocked(timeBase, screenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase, parcel);
                this.mWakelockStats.add(wakelockName, wakelock);
            }
            TimeBase timeBase2 = timeBase;
            TimeBase timeBase3 = screenOffTimeBase;
            int numSyncs = in.readInt();
            this.mSyncStats.clear();
            int j2 = 0;
            while (true) {
                int j3 = j2;
                if (j3 >= numSyncs) {
                    break;
                }
                String syncName = in.readString();
                if (in.readInt() != 0) {
                    OverflowArrayMap<DualTimer> overflowArrayMap = this.mSyncStats;
                    DualTimer dualTimer3 = r0;
                    numWakelocks = numWakelocks2;
                    DualTimer dualTimer4 = new DualTimer(this.mBsi.mClocks, this, 13, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, parcel);
                    overflowArrayMap.add(syncName, dualTimer3);
                } else {
                    numWakelocks = numWakelocks2;
                }
                j2 = j3 + 1;
                numWakelocks2 = numWakelocks;
            }
            int numJobs2 = in.readInt();
            this.mJobStats.clear();
            int j4 = 0;
            while (true) {
                int j5 = j4;
                if (j5 >= numJobs2) {
                    break;
                }
                String jobName = in.readString();
                if (in.readInt() != 0) {
                    OverflowArrayMap<DualTimer> overflowArrayMap2 = this.mJobStats;
                    DualTimer dualTimer5 = r0;
                    numJobs = numJobs2;
                    DualTimer dualTimer6 = new DualTimer(this.mBsi.mClocks, this, 14, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, parcel);
                    overflowArrayMap2.add(jobName, dualTimer5);
                } else {
                    numJobs = numJobs2;
                }
                j4 = j5 + 1;
                numJobs2 = numJobs;
            }
            readJobCompletionsFromParcelLocked(parcel);
            this.mJobsDeferredEventCount = new Counter(this.mBsi.mOnBatteryTimeBase, parcel);
            this.mJobsDeferredCount = new Counter(this.mBsi.mOnBatteryTimeBase, parcel);
            this.mJobsFreshnessTimeMs = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
            for (int i = 0; i < BatteryStats.JOB_FRESHNESS_BUCKETS.length; i++) {
                this.mJobsFreshnessBuckets[i] = Counter.readCounterFromParcel(this.mBsi.mOnBatteryTimeBase, parcel);
            }
            int numSensors = in.readInt();
            this.mSensorStats.clear();
            for (int k = 0; k < numSensors; k++) {
                int sensorNumber = in.readInt();
                Sensor sensor = new Sensor(this.mBsi, this, sensorNumber);
                sensor.readFromParcelLocked(this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, parcel);
                this.mSensorStats.put(sensorNumber, sensor);
            }
            int numProcs2 = in.readInt();
            this.mProcessStats.clear();
            for (int k2 = 0; k2 < numProcs2; k2++) {
                String processName = in.readString();
                Proc proc = new Proc(this.mBsi, processName);
                proc.readFromParcelLocked(parcel);
                this.mProcessStats.put(processName, proc);
            }
            int numPkgs = in.readInt();
            this.mPackageStats.clear();
            for (int l = 0; l < numPkgs; l++) {
                String packageName = in.readString();
                Pkg pkg = new Pkg(this.mBsi);
                pkg.readFromParcelLocked(parcel);
                this.mPackageStats.put(packageName, pkg);
            }
            this.mWifiRunning = false;
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer = r0;
                StopwatchTimer stopwatchTimer2 = new StopwatchTimer(this.mBsi.mClocks, this, 4, this.mBsi.mWifiRunningTimers, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mWifiRunningTimer = stopwatchTimer;
            } else {
                this.mWifiRunningTimer = null;
            }
            this.mFullWifiLockOut = false;
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer3 = new StopwatchTimer(this.mBsi.mClocks, this, 5, this.mBsi.mFullWifiLockTimers, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mFullWifiLockTimer = stopwatchTimer3;
                dualTimer = null;
            } else {
                dualTimer = null;
                this.mFullWifiLockTimer = null;
            }
            this.mWifiScanStarted = false;
            if (in.readInt() != 0) {
                DualTimer dualTimer7 = r0;
                int i2 = numSensors;
                dualTimer2 = dualTimer;
                DualTimer dualTimer8 = new DualTimer(this.mBsi.mClocks, this, 6, this.mBsi.mWifiScanTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, parcel);
                this.mWifiScanTimer = dualTimer7;
            } else {
                dualTimer2 = dualTimer;
                this.mWifiScanTimer = dualTimer2;
            }
            this.mWifiBatchedScanBinStarted = -1;
            for (int i3 = 0; i3 < 5; i3++) {
                if (in.readInt() != 0) {
                    makeWifiBatchedScanBin(i3, parcel);
                } else {
                    this.mWifiBatchedScanTimer[i3] = dualTimer2;
                }
            }
            this.mWifiMulticastEnabled = false;
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer4 = r0;
                procState = 0;
                StopwatchTimer stopwatchTimer5 = new StopwatchTimer(this.mBsi.mClocks, this, 7, this.mBsi.mWifiMulticastTimers, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mWifiMulticastTimer = stopwatchTimer4;
            } else {
                procState = 0;
                this.mWifiMulticastTimer = dualTimer2;
            }
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer6 = new StopwatchTimer(this.mBsi.mClocks, this, 15, this.mBsi.mAudioTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mAudioTurnedOnTimer = stopwatchTimer6;
            } else {
                this.mAudioTurnedOnTimer = dualTimer2;
            }
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer7 = new StopwatchTimer(this.mBsi.mClocks, this, 8, this.mBsi.mVideoTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mVideoTurnedOnTimer = stopwatchTimer7;
            } else {
                this.mVideoTurnedOnTimer = dualTimer2;
            }
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer8 = new StopwatchTimer(this.mBsi.mClocks, this, 16, this.mBsi.mFlashlightTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mFlashlightTurnedOnTimer = stopwatchTimer8;
            } else {
                this.mFlashlightTurnedOnTimer = dualTimer2;
            }
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer9 = new StopwatchTimer(this.mBsi.mClocks, this, 17, this.mBsi.mCameraTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mCameraTurnedOnTimer = stopwatchTimer9;
            } else {
                this.mCameraTurnedOnTimer = dualTimer2;
            }
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer10 = new StopwatchTimer(this.mBsi.mClocks, this, 10, null, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mForegroundActivityTimer = stopwatchTimer10;
            } else {
                this.mForegroundActivityTimer = dualTimer2;
            }
            if (in.readInt() != 0) {
                StopwatchTimer stopwatchTimer11 = new StopwatchTimer(this.mBsi.mClocks, this, 22, null, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mForegroundServiceTimer = stopwatchTimer11;
            } else {
                this.mForegroundServiceTimer = dualTimer2;
            }
            if (in.readInt() != 0) {
                DualTimer dualTimer9 = r0;
                int i4 = numProcs2;
                numProcs = 5;
                DualTimer dualTimer10 = new DualTimer(this.mBsi.mClocks, this, 20, null, this.mBsi.mOnBatteryScreenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase, parcel);
                this.mAggregatedPartialWakelockTimer = dualTimer9;
            } else {
                numProcs = 5;
                this.mAggregatedPartialWakelockTimer = null;
            }
            if (in.readInt() != 0) {
                DualTimer dualTimer11 = new DualTimer(this.mBsi.mClocks, this, 19, this.mBsi.mBluetoothScanOnTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, parcel);
                this.mBluetoothScanTimer = dualTimer11;
            } else {
                this.mBluetoothScanTimer = null;
            }
            if (in.readInt() != 0) {
                DualTimer dualTimer12 = new DualTimer(this.mBsi.mClocks, this, 21, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, parcel);
                this.mBluetoothUnoptimizedScanTimer = dualTimer12;
            } else {
                this.mBluetoothUnoptimizedScanTimer = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothScanResultCounter = new Counter(this.mBsi.mOnBatteryTimeBase, parcel);
            } else {
                this.mBluetoothScanResultCounter = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothScanResultBgCounter = new Counter(this.mOnBatteryBackgroundTimeBase, parcel);
            } else {
                this.mBluetoothScanResultBgCounter = null;
            }
            this.mProcessState = 19;
            for (int i5 = procState; i5 < 7; i5++) {
                if (in.readInt() != 0) {
                    makeProcessState(i5, parcel);
                } else {
                    this.mProcessStateTimer[i5] = null;
                }
            }
            if (in.readInt() != 0) {
                BatchTimer batchTimer = new BatchTimer(this.mBsi.mClocks, this, 9, this.mBsi.mOnBatteryTimeBase, parcel);
                this.mVibratorOnTimer = batchTimer;
            } else {
                this.mVibratorOnTimer = null;
            }
            if (in.readInt() != 0) {
                this.mUserActivityCounters = new Counter[4];
                for (int i6 = procState; i6 < 4; i6++) {
                    this.mUserActivityCounters[i6] = new Counter(this.mBsi.mOnBatteryTimeBase, parcel);
                }
            } else {
                this.mUserActivityCounters = null;
            }
            if (in.readInt() != 0) {
                this.mNetworkByteActivityCounters = new LongSamplingCounter[10];
                this.mNetworkPacketActivityCounters = new LongSamplingCounter[10];
                for (int i7 = procState; i7 < 10; i7++) {
                    this.mNetworkByteActivityCounters[i7] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
                    this.mNetworkPacketActivityCounters[i7] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
                }
                this.mMobileRadioActiveTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
                this.mMobileRadioActiveCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
            } else {
                this.mNetworkByteActivityCounters = null;
                this.mNetworkPacketActivityCounters = null;
            }
            if (in.readInt() != 0) {
                this.mWifiControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 1, parcel);
            } else {
                this.mWifiControllerActivity = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 1, parcel);
            } else {
                this.mBluetoothControllerActivity = null;
            }
            if (in.readInt() != 0) {
                this.mModemControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, numProcs, parcel);
            } else {
                this.mModemControllerActivity = null;
            }
            this.mUserCpuTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
            this.mSystemCpuTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
            if (in.readInt() != 0) {
                int numCpuClusters = in.readInt();
                if (this.mBsi.mPowerProfile == null || this.mBsi.mPowerProfile.getNumCpuClusters() == numCpuClusters) {
                    this.mCpuClusterSpeedTimesUs = new LongSamplingCounter[numCpuClusters][];
                    for (int cluster = procState; cluster < numCpuClusters; cluster++) {
                        if (in.readInt() != 0) {
                            int numSpeeds = in.readInt();
                            if (this.mBsi.mPowerProfile == null || this.mBsi.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster) == numSpeeds) {
                                LongSamplingCounter[] cpuSpeeds = new LongSamplingCounter[numSpeeds];
                                this.mCpuClusterSpeedTimesUs[cluster] = cpuSpeeds;
                                for (int speed = procState; speed < numSpeeds; speed++) {
                                    if (in.readInt() != 0) {
                                        cpuSpeeds[speed] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
                                    }
                                }
                            } else {
                                throw new ParcelFormatException("Incompatible number of cpu speeds");
                            }
                        } else {
                            this.mCpuClusterSpeedTimesUs[cluster] = null;
                        }
                    }
                } else {
                    throw new ParcelFormatException("Incompatible number of cpu clusters");
                }
            } else {
                this.mCpuClusterSpeedTimesUs = null;
            }
            this.mCpuFreqTimeMs = LongSamplingCounterArray.readFromParcel(parcel, this.mBsi.mOnBatteryTimeBase);
            this.mScreenOffCpuFreqTimeMs = LongSamplingCounterArray.readFromParcel(parcel, this.mBsi.mOnBatteryScreenOffTimeBase);
            this.mCpuActiveTimeMs = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
            this.mCpuClusterTimesMs = new LongSamplingCounterArray(this.mBsi.mOnBatteryTimeBase, parcel);
            int length = in.readInt();
            if (length == 7) {
                this.mProcStateTimeMs = new LongSamplingCounterArray[length];
                for (int procState2 = procState; procState2 < length; procState2++) {
                    this.mProcStateTimeMs[procState2] = LongSamplingCounterArray.readFromParcel(parcel, this.mBsi.mOnBatteryTimeBase);
                }
            } else {
                this.mProcStateTimeMs = null;
            }
            int length2 = in.readInt();
            if (length2 == 7) {
                this.mProcStateScreenOffTimeMs = new LongSamplingCounterArray[length2];
                while (true) {
                    int procState3 = procState;
                    if (procState3 >= length2) {
                        break;
                    }
                    this.mProcStateScreenOffTimeMs[procState3] = LongSamplingCounterArray.readFromParcel(parcel, this.mBsi.mOnBatteryScreenOffTimeBase);
                    procState = procState3 + 1;
                }
            } else {
                this.mProcStateScreenOffTimeMs = null;
            }
            if (in.readInt() != 0) {
                this.mMobileRadioApWakeupCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
            } else {
                this.mMobileRadioApWakeupCount = null;
            }
            if (in.readInt() != 0) {
                this.mWifiRadioApWakeupCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, parcel);
            } else {
                this.mWifiRadioApWakeupCount = null;
            }
        }

        public void noteJobsDeferredLocked(int numDeferred, long sinceLast) {
            this.mJobsDeferredEventCount.addAtomic(1);
            this.mJobsDeferredCount.addAtomic(numDeferred);
            if (sinceLast != 0) {
                this.mJobsFreshnessTimeMs.addCountLocked(sinceLast);
                for (int i = 0; i < BatteryStats.JOB_FRESHNESS_BUCKETS.length; i++) {
                    if (sinceLast < BatteryStats.JOB_FRESHNESS_BUCKETS[i]) {
                        if (this.mJobsFreshnessBuckets[i] == null) {
                            this.mJobsFreshnessBuckets[i] = new Counter(this.mBsi.mOnBatteryTimeBase);
                        }
                        this.mJobsFreshnessBuckets[i].addAtomic(1);
                        return;
                    }
                }
            }
        }

        public Proc getProcessStatsLocked(String name) {
            Proc ps = this.mProcessStats.get(name);
            if (ps != null) {
                return ps;
            }
            Proc ps2 = new Proc(this.mBsi, name);
            this.mProcessStats.put(name, ps2);
            return ps2;
        }

        @GuardedBy("mBsi")
        public void updateUidProcessStateLocked(int procState) {
            boolean userAwareService = procState == 3;
            int uidRunningState = BatteryStats.mapToInternalProcessState(procState);
            if (this.mProcessState != uidRunningState || userAwareService != this.mInForegroundService) {
                long elapsedRealtimeMs = this.mBsi.mClocks.elapsedRealtime();
                if (this.mProcessState != uidRunningState) {
                    long uptimeMs = this.mBsi.mClocks.uptimeMillis();
                    if (this.mProcessState != 19) {
                        this.mProcessStateTimer[this.mProcessState].stopRunningLocked(elapsedRealtimeMs);
                        if (this.mBsi.trackPerProcStateCpuTimes()) {
                            if (this.mBsi.mPendingUids.size() == 0) {
                                this.mBsi.mExternalSync.scheduleReadProcStateCpuTimes(this.mBsi.mOnBatteryTimeBase.isRunning(), this.mBsi.mOnBatteryScreenOffTimeBase.isRunning(), this.mBsi.mConstants.PROC_STATE_CPU_TIMES_READ_DELAY_MS);
                                long unused = this.mBsi.mNumSingleUidCpuTimeReads = 1 + this.mBsi.mNumSingleUidCpuTimeReads;
                            } else {
                                long unused2 = this.mBsi.mNumBatchedSingleUidCpuTimeReads = 1 + this.mBsi.mNumBatchedSingleUidCpuTimeReads;
                            }
                            if (this.mBsi.mPendingUids.indexOfKey(this.mUid) < 0 || ArrayUtils.contains(CRITICAL_PROC_STATES, this.mProcessState)) {
                                this.mBsi.mPendingUids.put(this.mUid, this.mProcessState);
                            }
                        } else {
                            this.mBsi.mPendingUids.clear();
                        }
                    }
                    this.mProcessState = uidRunningState;
                    if (uidRunningState != 19) {
                        if (this.mProcessStateTimer[uidRunningState] == null) {
                            makeProcessState(uidRunningState, null);
                        }
                        this.mProcessStateTimer[uidRunningState].startRunningLocked(elapsedRealtimeMs);
                    }
                    updateOnBatteryBgTimeBase(uptimeMs * 1000, elapsedRealtimeMs * 1000);
                    updateOnBatteryScreenOffBgTimeBase(uptimeMs * 1000, 1000 * elapsedRealtimeMs);
                }
                if (userAwareService != this.mInForegroundService) {
                    if (userAwareService) {
                        noteForegroundServiceResumedLocked(elapsedRealtimeMs);
                    } else {
                        noteForegroundServicePausedLocked(elapsedRealtimeMs);
                    }
                    this.mInForegroundService = userAwareService;
                }
            }
        }

        public boolean isInBackground() {
            return this.mProcessState >= 3;
        }

        public boolean updateOnBatteryBgTimeBase(long uptimeUs, long realtimeUs) {
            return this.mOnBatteryBackgroundTimeBase.setRunning(this.mBsi.mOnBatteryTimeBase.isRunning() && isInBackground(), uptimeUs, realtimeUs);
        }

        public boolean updateOnBatteryScreenOffBgTimeBase(long uptimeUs, long realtimeUs) {
            return this.mOnBatteryScreenOffBackgroundTimeBase.setRunning(this.mBsi.mOnBatteryScreenOffTimeBase.isRunning() && isInBackground(), uptimeUs, realtimeUs);
        }

        public SparseArray<? extends BatteryStats.Uid.Pid> getPidStats() {
            return this.mPids;
        }

        public BatteryStats.Uid.Pid getPidStatsLocked(int pid) {
            BatteryStats.Uid.Pid p = this.mPids.get(pid);
            if (p != null) {
                return p;
            }
            BatteryStats.Uid.Pid p2 = new BatteryStats.Uid.Pid(this);
            this.mPids.put(pid, p2);
            return p2;
        }

        public Pkg getPackageStatsLocked(String name) {
            Pkg ps = this.mPackageStats.get(name);
            if (ps != null) {
                return ps;
            }
            Pkg ps2 = new Pkg(this.mBsi);
            this.mPackageStats.put(name, ps2);
            return ps2;
        }

        public Pkg.Serv getServiceStatsLocked(String pkg, String serv) {
            Pkg ps = getPackageStatsLocked(pkg);
            Pkg.Serv ss = ps.mServiceStats.get(serv);
            if (ss != null) {
                return ss;
            }
            Pkg.Serv ss2 = ps.newServiceStatsLocked();
            ps.mServiceStats.put(serv, ss2);
            return ss2;
        }

        public void readSyncSummaryFromParcelLocked(String name, Parcel in) {
            DualTimer timer = this.mSyncStats.instantiateObject();
            timer.readSummaryFromParcelLocked(in);
            this.mSyncStats.add(name, timer);
        }

        public void readJobSummaryFromParcelLocked(String name, Parcel in) {
            DualTimer timer = this.mJobStats.instantiateObject();
            timer.readSummaryFromParcelLocked(in);
            this.mJobStats.add(name, timer);
        }

        public void readWakeSummaryFromParcelLocked(String wlName, Parcel in) {
            Wakelock wl = new Wakelock(this.mBsi, this);
            this.mWakelockStats.add(wlName, wl);
            if (in.readInt() != 0) {
                getWakelockTimerLocked(wl, 1).readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                getWakelockTimerLocked(wl, 0).readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                getWakelockTimerLocked(wl, 2).readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                getWakelockTimerLocked(wl, 18).readSummaryFromParcelLocked(in);
            }
        }

        public DualTimer getSensorTimerLocked(int sensor, boolean create) {
            Sensor se = this.mSensorStats.get(sensor);
            if (se == null) {
                if (!create) {
                    return null;
                }
                se = new Sensor(this.mBsi, this, sensor);
                this.mSensorStats.put(sensor, se);
            }
            DualTimer t = se.mTimer;
            if (t != null) {
                return t;
            }
            ArrayList<StopwatchTimer> timers = this.mBsi.mSensorTimers.get(sensor);
            if (timers == null) {
                timers = new ArrayList<>();
                this.mBsi.mSensorTimers.put(sensor, timers);
            }
            DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this, 3, timers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
            DualTimer t2 = dualTimer;
            se.mTimer = t2;
            return t2;
        }

        public void noteStartSyncLocked(String name, long elapsedRealtimeMs) {
            DualTimer t = this.mSyncStats.startObject(name);
            if (t != null) {
                t.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteStopSyncLocked(String name, long elapsedRealtimeMs) {
            DualTimer t = this.mSyncStats.stopObject(name);
            if (t != null) {
                t.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteStartJobLocked(String name, long elapsedRealtimeMs) {
            DualTimer t = this.mJobStats.startObject(name);
            if (t != null) {
                t.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteStopJobLocked(String name, long elapsedRealtimeMs, int stopReason) {
            DualTimer t = this.mJobStats.stopObject(name);
            if (t != null) {
                t.stopRunningLocked(elapsedRealtimeMs);
            }
            if (this.mBsi.mOnBatteryTimeBase.isRunning()) {
                SparseIntArray types = this.mJobCompletions.get(name);
                if (types == null) {
                    types = new SparseIntArray();
                    this.mJobCompletions.put(name, types);
                }
                types.put(stopReason, types.get(stopReason, 0) + 1);
            }
        }

        public StopwatchTimer getWakelockTimerLocked(Wakelock wl, int type) {
            if (wl == null) {
                return null;
            }
            if (type != 18) {
                switch (type) {
                    case 0:
                        DualTimer t = wl.mTimerPartial;
                        if (t == null) {
                            DualTimer dualTimer = new DualTimer(this.mBsi.mClocks, this, 0, this.mBsi.mPartialTimers, this.mBsi.mOnBatteryScreenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase);
                            t = dualTimer;
                            wl.mTimerPartial = t;
                        }
                        return t;
                    case 1:
                        StopwatchTimer t2 = wl.mTimerFull;
                        if (t2 == null) {
                            StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mBsi.mClocks, this, 1, this.mBsi.mFullTimers, this.mBsi.mOnBatteryTimeBase);
                            t2 = stopwatchTimer;
                            wl.mTimerFull = t2;
                        }
                        return t2;
                    case 2:
                        StopwatchTimer t3 = wl.mTimerWindow;
                        if (t3 == null) {
                            StopwatchTimer stopwatchTimer2 = new StopwatchTimer(this.mBsi.mClocks, this, 2, this.mBsi.mWindowTimers, this.mBsi.mOnBatteryTimeBase);
                            t3 = stopwatchTimer2;
                            wl.mTimerWindow = t3;
                        }
                        return t3;
                    default:
                        throw new IllegalArgumentException("type=" + type);
                }
            } else {
                StopwatchTimer t4 = wl.mTimerDraw;
                if (t4 == null) {
                    StopwatchTimer stopwatchTimer3 = new StopwatchTimer(this.mBsi.mClocks, this, 18, this.mBsi.mDrawTimers, this.mBsi.mOnBatteryTimeBase);
                    t4 = stopwatchTimer3;
                    wl.mTimerDraw = t4;
                }
                return t4;
            }
        }

        public void noteStartWakeLocked(int pid, String name, int type, long elapsedRealtimeMs) {
            Wakelock wl = this.mWakelockStats.startObject(name);
            if (wl != null) {
                getWakelockTimerLocked(wl, type).startRunningLocked(elapsedRealtimeMs);
            }
            if (type == 0) {
                createAggregatedPartialWakelockTimerLocked().startRunningLocked(elapsedRealtimeMs);
                if (pid >= 0) {
                    BatteryStats.Uid.Pid p = getPidStatsLocked(pid);
                    int i = p.mWakeNesting;
                    p.mWakeNesting = i + 1;
                    if (i == 0) {
                        p.mWakeStartMs = elapsedRealtimeMs;
                    }
                }
            }
        }

        public void noteStopWakeLocked(int pid, String name, int type, long elapsedRealtimeMs) {
            Wakelock wl = this.mWakelockStats.stopObject(name);
            if (wl != null) {
                getWakelockTimerLocked(wl, type).stopRunningLocked(elapsedRealtimeMs);
            }
            if (type == 0) {
                if (this.mAggregatedPartialWakelockTimer != null) {
                    this.mAggregatedPartialWakelockTimer.stopRunningLocked(elapsedRealtimeMs);
                }
                if (pid >= 0) {
                    BatteryStats.Uid.Pid p = this.mPids.get(pid);
                    if (p != null && p.mWakeNesting > 0) {
                        p.mWakeNesting = p.mWakeNesting - 1;
                        if (p.mWakeNesting == 1) {
                            p.mWakeSumMs += elapsedRealtimeMs - p.mWakeStartMs;
                            p.mWakeStartMs = 0;
                        }
                    }
                }
            }
        }

        public void reportExcessiveCpuLocked(String proc, long overTime, long usedTime) {
            Proc p = getProcessStatsLocked(proc);
            if (p != null) {
                p.addExcessiveCpu(overTime, usedTime);
            }
        }

        public void noteStartSensor(int sensor, long elapsedRealtimeMs) {
            getSensorTimerLocked(sensor, true).startRunningLocked(elapsedRealtimeMs);
        }

        public void noteStopSensor(int sensor, long elapsedRealtimeMs) {
            DualTimer t = getSensorTimerLocked(sensor, false);
            if (t != null) {
                t.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteStartGps(long elapsedRealtimeMs) {
            noteStartSensor(-10000, elapsedRealtimeMs);
        }

        public void noteStopGps(long elapsedRealtimeMs) {
            noteStopSensor(-10000, elapsedRealtimeMs);
        }

        public BatteryStatsImpl getBatteryStats() {
            return this.mBsi;
        }
    }

    @VisibleForTesting
    public final class UidToRemove {
        int endUid;
        int startUid;
        long timeAddedInQueue;

        public UidToRemove(BatteryStatsImpl this$02, int uid, long timestamp) {
            this(uid, uid, timestamp);
        }

        public UidToRemove(int startUid2, int endUid2, long timestamp) {
            this.startUid = startUid2;
            this.endUid = endUid2;
            this.timeAddedInQueue = timestamp;
        }

        /* access modifiers changed from: package-private */
        public void remove() {
            if (this.startUid == this.endUid) {
                BatteryStatsImpl.this.mKernelUidCpuTimeReader.removeUid(this.startUid);
                BatteryStatsImpl.this.mKernelUidCpuFreqTimeReader.removeUid(this.startUid);
                if (BatteryStatsImpl.this.mConstants.TRACK_CPU_ACTIVE_CLUSTER_TIME) {
                    BatteryStatsImpl.this.mKernelUidCpuActiveTimeReader.removeUid(this.startUid);
                    BatteryStatsImpl.this.mKernelUidCpuClusterTimeReader.removeUid(this.startUid);
                }
                if (BatteryStatsImpl.this.mKernelSingleUidTimeReader != null) {
                    BatteryStatsImpl.this.mKernelSingleUidTimeReader.removeUid(this.startUid);
                }
                int unused = BatteryStatsImpl.this.mNumUidsRemoved = BatteryStatsImpl.this.mNumUidsRemoved + 1;
            } else if (this.startUid < this.endUid) {
                BatteryStatsImpl.this.mKernelUidCpuFreqTimeReader.removeUidsInRange(this.startUid, this.endUid);
                BatteryStatsImpl.this.mKernelUidCpuTimeReader.removeUidsInRange(this.startUid, this.endUid);
                if (BatteryStatsImpl.this.mConstants.TRACK_CPU_ACTIVE_CLUSTER_TIME) {
                    BatteryStatsImpl.this.mKernelUidCpuActiveTimeReader.removeUidsInRange(this.startUid, this.endUid);
                    BatteryStatsImpl.this.mKernelUidCpuClusterTimeReader.removeUidsInRange(this.startUid, this.endUid);
                }
                if (BatteryStatsImpl.this.mKernelSingleUidTimeReader != null) {
                    BatteryStatsImpl.this.mKernelSingleUidTimeReader.removeUidsInRange(this.startUid, this.endUid);
                }
                int unused2 = BatteryStatsImpl.this.mNumUidsRemoved = BatteryStatsImpl.this.mNumUidsRemoved + 1;
            } else {
                Slog.w(BatteryStatsImpl.TAG, "End UID " + this.endUid + " is smaller than start UID " + this.startUid);
            }
        }
    }

    public static abstract class UserInfoProvider {
        private int[] userIds;

        /* access modifiers changed from: protected */
        public abstract int[] getUserIds();

        @VisibleForTesting
        public final void refreshUserIds() {
            this.userIds = getUserIds();
        }

        @VisibleForTesting
        public boolean exists(int userId) {
            if (this.userIds != null) {
                return ArrayUtils.contains(this.userIds, userId);
            }
            return true;
        }
    }

    static {
        if (ActivityManager.isLowRamDeviceStatic()) {
            MAX_HISTORY_ITEMS = 800;
            MAX_MAX_HISTORY_ITEMS = 1200;
            MAX_WAKELOCKS_PER_UID = 40;
            MAX_HISTORY_BUFFER = 98304;
            MAX_MAX_HISTORY_BUFFER = Protocol.BASE_WIFI;
        } else {
            MAX_HISTORY_ITEMS = 4000;
            MAX_MAX_HISTORY_ITEMS = 6000;
            MAX_WAKELOCKS_PER_UID = 200;
            MAX_HISTORY_BUFFER = 524288;
            MAX_MAX_HISTORY_BUFFER = 655360;
        }
    }

    public LongSparseArray<SamplingTimer> getKernelMemoryStats() {
        return this.mKernelMemoryStats;
    }

    public void postBatteryNeedsCpuUpdateMsg() {
        this.mHandler.sendEmptyMessage(1);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0036, code lost:
        r1 = r0.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        if (r1 < 0) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003e, code lost:
        r2 = r0.keyAt(r1);
        r3 = r0.valueAt(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0046, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r4 = getAvailableUidStatsLocked(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004b, code lost:
        if (r4 != null) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004d, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0051, code lost:
        if (r4.mChildUids != null) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0053, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0055, code lost:
        r5 = r4.mChildUids.toArray();
        r6 = r5.length - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005e, code lost:
        if (r6 < 0) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0060, code lost:
        r5[r6] = r4.mChildUids.get(r6);
        r6 = r6 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006b, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006c, code lost:
        r6 = r10.mKernelSingleUidTimeReader.readDeltaMs(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0072, code lost:
        if (r5 == null) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0074, code lost:
        r7 = r5.length - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0077, code lost:
        if (r7 < 0) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0079, code lost:
        r6 = addCpuTimes(r6, r10.mKernelSingleUidTimeReader.readDeltaMs(r5[r7]));
        r7 = r7 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0088, code lost:
        if (r11 == false) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x008a, code lost:
        if (r6 == null) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x008c, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        com.android.internal.os.BatteryStatsImpl.Uid.access$300(r4, r3, r6, r11);
        com.android.internal.os.BatteryStatsImpl.Uid.access$400(r4, r3, r6, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0093, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0098, code lost:
        r1 = r1 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x009e, code lost:
        return;
     */
    public void updateProcStateCpuTimes(boolean onBattery, boolean onBatteryScreenOff) {
        synchronized (this) {
            if (this.mConstants.TRACK_CPU_TIMES_BY_PROC_STATE) {
                if (initKernelSingleUidTimeReaderLocked()) {
                    if (this.mKernelSingleUidTimeReader.hasStaleData()) {
                        this.mPendingUids.clear();
                    } else if (this.mPendingUids.size() != 0) {
                        SparseIntArray uidStates = this.mPendingUids.clone();
                        this.mPendingUids.clear();
                    }
                }
            }
        }
    }

    public void clearPendingRemovedUids() {
        long cutOffTime = this.mClocks.elapsedRealtime() - this.mConstants.UID_REMOVE_DELAY_MS;
        while (!this.mPendingRemovedUids.isEmpty() && this.mPendingRemovedUids.peek().timeAddedInQueue < cutOffTime) {
            this.mPendingRemovedUids.poll().remove();
        }
    }

    public void copyFromAllUidsCpuTimes() {
        synchronized (this) {
            copyFromAllUidsCpuTimes(this.mOnBatteryTimeBase.isRunning(), this.mOnBatteryScreenOffTimeBase.isRunning());
        }
    }

    public void copyFromAllUidsCpuTimes(boolean onBattery, boolean onBatteryScreenOff) {
        int procState;
        synchronized (this) {
            if (this.mConstants.TRACK_CPU_TIMES_BY_PROC_STATE) {
                if (initKernelSingleUidTimeReaderLocked()) {
                    SparseArray<long[]> allUidCpuFreqTimesMs = this.mKernelUidCpuFreqTimeReader.getAllUidCpuFreqTimeMs();
                    if (this.mKernelSingleUidTimeReader.hasStaleData()) {
                        this.mKernelSingleUidTimeReader.setAllUidsCpuTimesMs(allUidCpuFreqTimesMs);
                        this.mKernelSingleUidTimeReader.markDataAsStale(false);
                        this.mPendingUids.clear();
                        return;
                    }
                    for (int i = allUidCpuFreqTimesMs.size() - 1; i >= 0; i--) {
                        int uid = allUidCpuFreqTimesMs.keyAt(i);
                        Uid u = getAvailableUidStatsLocked(mapUid(uid));
                        if (u != null) {
                            long[] cpuTimesMs = allUidCpuFreqTimesMs.valueAt(i);
                            if (cpuTimesMs != null) {
                                long[] deltaTimesMs = this.mKernelSingleUidTimeReader.computeDelta(uid, (long[]) cpuTimesMs.clone());
                                if (onBattery && deltaTimesMs != null) {
                                    int idx = this.mPendingUids.indexOfKey(uid);
                                    if (idx >= 0) {
                                        procState = this.mPendingUids.valueAt(idx);
                                        this.mPendingUids.removeAt(idx);
                                    } else {
                                        procState = u.mProcessState;
                                    }
                                    if (procState >= 0 && procState < 7) {
                                        u.addProcStateTimesMs(procState, deltaTimesMs, onBattery);
                                        u.addProcStateScreenOffTimesMs(procState, deltaTimesMs, onBatteryScreenOff);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    public long[] addCpuTimes(long[] timesA, long[] timesB) {
        if (timesA == null || timesB == null) {
            return timesA == null ? timesB == null ? null : timesB : timesA;
        }
        for (int i = timesA.length - 1; i >= 0; i--) {
            timesA[i] = timesA[i] + timesB[i];
        }
        return timesA;
    }

    @GuardedBy("this")
    private boolean initKernelSingleUidTimeReaderLocked() {
        boolean z = false;
        if (this.mKernelSingleUidTimeReader == null) {
            if (this.mPowerProfile == null) {
                return false;
            }
            if (this.mCpuFreqs == null) {
                this.mCpuFreqs = this.mKernelUidCpuFreqTimeReader.readFreqs(this.mPowerProfile);
            }
            if (this.mCpuFreqs != null) {
                this.mKernelSingleUidTimeReader = new KernelSingleUidTimeReader(this.mCpuFreqs.length);
            } else {
                this.mPerProcStateCpuTimesAvailable = this.mKernelUidCpuFreqTimeReader.allUidTimesAvailable();
                return false;
            }
        }
        if (this.mKernelUidCpuFreqTimeReader.allUidTimesAvailable() && this.mKernelSingleUidTimeReader.singleUidCpuTimesAvailable()) {
            z = true;
        }
        this.mPerProcStateCpuTimesAvailable = z;
        return true;
    }

    public Map<String, ? extends Timer> getRpmStats() {
        return this.mRpmStats;
    }

    public Map<String, ? extends Timer> getScreenOffRpmStats() {
        return this.mScreenOffRpmStats;
    }

    public Map<String, ? extends Timer> getKernelWakelockStats() {
        return this.mKernelWakelockStats;
    }

    public Map<String, ? extends Timer> getWakeupReasonStats() {
        return this.mWakeupReasonStats;
    }

    public long getUahDischarge(int which) {
        return this.mDischargeCounter.getCountLocked(which);
    }

    public long getUahDischargeScreenOff(int which) {
        return this.mDischargeScreenOffCounter.getCountLocked(which);
    }

    public long getUahDischargeScreenDoze(int which) {
        return this.mDischargeScreenDozeCounter.getCountLocked(which);
    }

    public long getUahDischargeLightDoze(int which) {
        return this.mDischargeLightDozeCounter.getCountLocked(which);
    }

    public long getUahDischargeDeepDoze(int which) {
        return this.mDischargeDeepDozeCounter.getCountLocked(which);
    }

    public int getEstimatedBatteryCapacity() {
        return this.mEstimatedBatteryCapacity;
    }

    public int getMinLearnedBatteryCapacity() {
        return this.mMinLearnedBatteryCapacity;
    }

    public int getMaxLearnedBatteryCapacity() {
        return this.mMaxLearnedBatteryCapacity;
    }

    public BatteryStatsImpl() {
        this((Clocks) new SystemClocks());
    }

    public BatteryStatsImpl(Clocks clocks) {
        this.mKernelWakelockReader = new KernelWakelockReader();
        this.mTmpWakelockStats = new KernelWakelockStats();
        this.mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
        this.mKernelUidCpuFreqTimeReader = new KernelUidCpuFreqTimeReader();
        this.mKernelUidCpuActiveTimeReader = new KernelUidCpuActiveTimeReader();
        this.mKernelUidCpuClusterTimeReader = new KernelUidCpuClusterTimeReader();
        this.mKernelMemoryBandwidthStats = new KernelMemoryBandwidthStats();
        this.mKernelMemoryStats = new LongSparseArray<>();
        this.mPerProcStateCpuTimesAvailable = true;
        this.mPendingUids = new SparseIntArray();
        this.mCpuTimeReadsTrackingStartTime = SystemClock.uptimeMillis();
        this.mTmpRpmStats = new RpmStats();
        this.mLastRpmStatsUpdateTimeMs = -1000;
        this.mPendingRemovedUids = new LinkedList();
        this.mExternalSync = null;
        this.mUserInfoProvider = null;
        this.mIsolatedUids = new SparseIntArray();
        this.mUidStats = new SparseArray<>();
        this.mPartialTimers = new ArrayList<>();
        this.mFullTimers = new ArrayList<>();
        this.mWindowTimers = new ArrayList<>();
        this.mDrawTimers = new ArrayList<>();
        this.mSensorTimers = new SparseArray<>();
        this.mWifiRunningTimers = new ArrayList<>();
        this.mFullWifiLockTimers = new ArrayList<>();
        this.mWifiMulticastTimers = new ArrayList<>();
        this.mWifiScanTimers = new ArrayList<>();
        this.mWifiBatchedScanTimers = new SparseArray<>();
        this.mAudioTurnedOnTimers = new ArrayList<>();
        this.mVideoTurnedOnTimers = new ArrayList<>();
        this.mFlashlightTurnedOnTimers = new ArrayList<>();
        this.mCameraTurnedOnTimers = new ArrayList<>();
        this.mBluetoothScanOnTimers = new ArrayList<>();
        this.mLastPartialTimers = new ArrayList<>();
        this.mOnBatteryTimeBase = new TimeBase();
        this.mOnBatteryScreenOffTimeBase = new TimeBase();
        this.mActiveEvents = new BatteryStats.HistoryEventTracker();
        this.mHaveBatteryLevel = false;
        this.mRecordingHistory = false;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new BatteryStats.HistoryItem();
        this.mHistoryLastLastWritten = new BatteryStats.HistoryItem();
        this.mHistoryReadTmp = new BatteryStats.HistoryItem();
        this.mHistoryAddTmp = new BatteryStats.HistoryItem();
        this.mHistoryTagPool = new HashMap<>();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = false;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryCur = new BatteryStats.HistoryItem();
        this.mLastHistoryStepDetails = null;
        this.mLastHistoryStepLevel = 0;
        this.mCurHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mReadHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mTmpHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mScreenState = 0;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mUsbDataState = 0;
        this.mGpsSignalQualityBin = -1;
        this.mGpsSignalQualityTimer = new StopwatchTimer[2];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[21];
        this.mNetworkByteActivityCounters = new LongSamplingCounter[10];
        this.mNetworkPacketActivityCounters = new LongSamplingCounter[10];
        this.mHasWifiReporting = false;
        this.mHasBluetoothReporting = false;
        this.mHasModemReporting = false;
        this.mWifiState = -1;
        this.mWifiStateTimer = new StopwatchTimer[8];
        this.mWifiSupplState = -1;
        this.mWifiSupplStateTimer = new StopwatchTimer[13];
        this.mWifiSignalStrengthBin = -1;
        this.mWifiSignalStrengthsTimer = new StopwatchTimer[5];
        this.mIsCellularTxPowerHigh = false;
        this.mMobileRadioPowerState = 1;
        this.mWifiRadioPowerState = 1;
        this.mCharging = true;
        this.mInitStepMode = 0;
        this.mCurStepMode = 0;
        this.mModStepMode = 0;
        this.mDischargeStepTracker = new BatteryStats.LevelStepTracker(200);
        this.mDailyDischargeStepTracker = new BatteryStats.LevelStepTracker(400);
        this.mChargeStepTracker = new BatteryStats.LevelStepTracker(200);
        this.mDailyChargeStepTracker = new BatteryStats.LevelStepTracker(400);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList<>();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mMinLearnedBatteryCapacity = -1;
        this.mMaxLearnedBatteryCapacity = -1;
        this.mRpmStats = new HashMap<>();
        this.mScreenOffRpmStats = new HashMap<>();
        this.mKernelWakelockStats = new HashMap<>();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap<>();
        this.mChangedStates = 0;
        this.mChangedStates2 = 0;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = 0;
        this.mWifiScanNesting = 0;
        this.mWifiMulticastNesting = 0;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        this.mNetworkStatsPool = new Pools.SynchronizedPool(6);
        this.mWifiNetworkLock = new Object();
        this.mWifiIfaces = EmptyArray.STRING;
        this.mLastWifiNetworkStats = new NetworkStats(0, -1);
        this.mModemNetworkLock = new Object();
        this.mModemIfaces = EmptyArray.STRING;
        this.mLastModemNetworkStats = new NetworkStats(0, -1);
        ModemActivityInfo modemActivityInfo = new ModemActivityInfo(0, 0, 0, new int[0], 0, 0);
        this.mLastModemActivityInfo = modemActivityInfo;
        this.mLastBluetoothActivityInfo = new BluetoothActivityInfoCache();
        this.mPendingWrite = null;
        this.mWriteLock = new ReentrantLock();
        init(clocks);
        this.mFile = null;
        this.mCheckinFile = null;
        this.mDailyFile = null;
        this.mHandler = null;
        this.mPlatformIdleStateCallback = null;
        this.mUserInfoProvider = null;
        this.mConstants = new Constants(this.mHandler);
        clearHistoryLocked();
    }

    private void init(Clocks clocks) {
        this.mClocks = clocks;
    }

    public SamplingTimer getRpmTimerLocked(String name) {
        SamplingTimer rpmt = this.mRpmStats.get(name);
        if (rpmt != null) {
            return rpmt;
        }
        SamplingTimer rpmt2 = new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase);
        this.mRpmStats.put(name, rpmt2);
        return rpmt2;
    }

    public SamplingTimer getScreenOffRpmTimerLocked(String name) {
        SamplingTimer rpmt = this.mScreenOffRpmStats.get(name);
        if (rpmt != null) {
            return rpmt;
        }
        SamplingTimer rpmt2 = new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase);
        this.mScreenOffRpmStats.put(name, rpmt2);
        return rpmt2;
    }

    public SamplingTimer getWakeupReasonTimerLocked(String name) {
        SamplingTimer timer = this.mWakeupReasonStats.get(name);
        if (timer != null) {
            return timer;
        }
        SamplingTimer timer2 = new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase);
        this.mWakeupReasonStats.put(name, timer2);
        return timer2;
    }

    public SamplingTimer getKernelWakelockTimerLocked(String name) {
        SamplingTimer kwlt = this.mKernelWakelockStats.get(name);
        if (kwlt != null) {
            return kwlt;
        }
        SamplingTimer kwlt2 = new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase);
        this.mKernelWakelockStats.put(name, kwlt2);
        return kwlt2;
    }

    public SamplingTimer getKernelMemoryTimerLocked(long bucket) {
        SamplingTimer kmt = this.mKernelMemoryStats.get(bucket);
        if (kmt != null) {
            return kmt;
        }
        SamplingTimer kmt2 = new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase);
        this.mKernelMemoryStats.put(bucket, kmt2);
        return kmt2;
    }

    private int writeHistoryTag(BatteryStats.HistoryTag tag) {
        Integer idxObj = this.mHistoryTagPool.get(tag);
        if (idxObj != null) {
            return idxObj.intValue();
        }
        int idx = this.mNextHistoryTagIdx;
        BatteryStats.HistoryTag key = new BatteryStats.HistoryTag();
        key.setTo(tag);
        tag.poolIdx = idx;
        this.mHistoryTagPool.put(key, Integer.valueOf(idx));
        this.mNextHistoryTagIdx++;
        this.mNumHistoryTagChars += key.string.length() + 1;
        return idx;
    }

    private void readHistoryTag(int index, BatteryStats.HistoryTag tag) {
        if (index >= this.mReadHistoryStrings.length) {
            Slog.w(TAG, "readHistoryTag, index >= mReadHistoryStrings.length");
        } else if (index >= this.mReadHistoryUids.length) {
            Slog.w(TAG, "readHistoryTag, index >= mReadHistoryUids.length");
        } else {
            tag.string = this.mReadHistoryStrings[index];
            tag.uid = this.mReadHistoryUids[index];
            tag.poolIdx = index;
        }
    }

    public void writeHistoryDelta(Parcel dest, BatteryStats.HistoryItem cur, BatteryStats.HistoryItem last) {
        int deltaTimeToken;
        int wakeLockIndex;
        int wakeReasonIndex;
        Parcel parcel = dest;
        BatteryStats.HistoryItem historyItem = cur;
        BatteryStats.HistoryItem historyItem2 = last;
        if (historyItem2 == null || historyItem.cmd != 0) {
            parcel.writeInt(DELTA_TIME_ABS);
            historyItem.writeToParcel(parcel, 0);
            return;
        }
        long deltaTime = historyItem.time - historyItem2.time;
        int lastBatteryLevelInt = buildBatteryLevelInt(historyItem2);
        int lastStateInt = buildStateInt(historyItem2);
        if (deltaTime < 0 || deltaTime > 2147483647L) {
            deltaTimeToken = EventLogTags.SYSUI_VIEW_VISIBILITY;
        } else if (deltaTime >= 524285) {
            deltaTimeToken = DELTA_TIME_INT;
        } else {
            deltaTimeToken = (int) deltaTime;
        }
        int firstToken = (historyItem.states & DELTA_STATE_MASK) | deltaTimeToken;
        int includeStepDetails = this.mLastHistoryStepLevel > historyItem.batteryLevel ? 1 : 0;
        boolean computeStepDetails = includeStepDetails != 0 || this.mLastHistoryStepDetails == null;
        int batteryLevelInt = buildBatteryLevelInt(historyItem) | includeStepDetails;
        boolean batteryLevelIntChanged = batteryLevelInt != lastBatteryLevelInt;
        if (batteryLevelIntChanged) {
            firstToken |= 524288;
        }
        int stateInt = buildStateInt(historyItem);
        boolean stateIntChanged = stateInt != lastStateInt;
        if (stateIntChanged) {
            firstToken |= DELTA_STATE_FLAG;
        }
        int i = lastBatteryLevelInt;
        boolean state2IntChanged = historyItem.states2 != historyItem2.states2;
        if (state2IntChanged) {
            firstToken |= DELTA_STATE2_FLAG;
        }
        if (!(historyItem.wakelockTag == null && historyItem.wakeReasonTag == null)) {
            firstToken |= DELTA_WAKELOCK_FLAG;
        }
        if (historyItem.eventCode != 0) {
            firstToken |= DELTA_EVENT_FLAG;
        }
        int i2 = lastStateInt;
        boolean batteryChargeChanged = historyItem.batteryChargeUAh != historyItem2.batteryChargeUAh;
        if (batteryChargeChanged) {
            firstToken |= 16777216;
        }
        parcel.writeInt(firstToken);
        if (deltaTimeToken >= DELTA_TIME_INT) {
            if (deltaTimeToken == DELTA_TIME_INT) {
                parcel.writeInt((int) deltaTime);
            } else {
                parcel.writeLong(deltaTime);
            }
        }
        if (batteryLevelIntChanged) {
            parcel.writeInt(batteryLevelInt);
        }
        if (stateIntChanged) {
            parcel.writeInt(stateInt);
        }
        if (state2IntChanged) {
            parcel.writeInt(historyItem.states2);
        }
        if (!(historyItem.wakelockTag == null && historyItem.wakeReasonTag == null)) {
            if (historyItem.wakelockTag != null) {
                wakeLockIndex = writeHistoryTag(historyItem.wakelockTag);
            } else {
                wakeLockIndex = 65535;
            }
            if (historyItem.wakeReasonTag != null) {
                wakeReasonIndex = writeHistoryTag(historyItem.wakeReasonTag);
            } else {
                wakeReasonIndex = 65535;
            }
            int i3 = wakeReasonIndex;
            parcel.writeInt((wakeReasonIndex << 16) | wakeLockIndex);
        }
        if (historyItem.eventCode != 0) {
            parcel.writeInt((historyItem.eventCode & 65535) | (writeHistoryTag(historyItem.eventTag) << 16));
        }
        if (computeStepDetails) {
            if (this.mPlatformIdleStateCallback != null) {
                this.mCurHistoryStepDetails.statPlatformIdleState = this.mPlatformIdleStateCallback.getPlatformLowPowerStats();
                this.mCurHistoryStepDetails.statSubsystemPowerState = this.mPlatformIdleStateCallback.getSubsystemLowPowerStats();
            }
            computeHistoryStepDetails(this.mCurHistoryStepDetails, this.mLastHistoryStepDetails);
            if (includeStepDetails != 0) {
                this.mCurHistoryStepDetails.writeToParcel(parcel);
            }
            historyItem.stepDetails = this.mCurHistoryStepDetails;
            this.mLastHistoryStepDetails = this.mCurHistoryStepDetails;
        } else {
            historyItem.stepDetails = null;
        }
        if (this.mLastHistoryStepLevel < historyItem.batteryLevel) {
            this.mLastHistoryStepDetails = null;
        }
        this.mLastHistoryStepLevel = historyItem.batteryLevel;
        if (batteryChargeChanged) {
            parcel.writeInt(historyItem.batteryChargeUAh);
        }
    }

    private int buildBatteryLevelInt(BatteryStats.HistoryItem h) {
        return ((h.batteryLevel << 25) & DELTA_STATE_MASK) | ((h.batteryTemperature << 15) & 33521664) | ((h.batteryVoltage << 1) & 32766);
    }

    private void readBatteryLevelInt(int batteryLevelInt, BatteryStats.HistoryItem out) {
        out.batteryLevel = (byte) ((DELTA_STATE_MASK & batteryLevelInt) >>> 25);
        out.batteryTemperature = (short) ((33521664 & batteryLevelInt) >>> 15);
        out.batteryVoltage = (char) ((batteryLevelInt & 32766) >>> 1);
    }

    private int buildStateInt(BatteryStats.HistoryItem h) {
        int plugType = 0;
        if ((h.batteryPlugType & 1) != 0) {
            plugType = 1;
        } else if ((h.batteryPlugType & 2) != 0) {
            plugType = 2;
        } else if ((h.batteryPlugType & 4) != 0) {
            plugType = 3;
        }
        return ((h.batteryStatus & 7) << 29) | ((h.batteryHealth & 7) << 26) | ((plugType & 3) << 24) | (h.states & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT);
    }

    private void computeHistoryStepDetails(BatteryStats.HistoryStepDetails out, BatteryStats.HistoryStepDetails last) {
        BatteryStats.HistoryStepDetails tmp = last != null ? this.mTmpHistoryStepDetails : out;
        requestImmediateCpuUpdate();
        int i = 0;
        if (last == null) {
            int NU = this.mUidStats.size();
            while (i < NU) {
                Uid uid = this.mUidStats.valueAt(i);
                uid.mLastStepUserTime = uid.mCurStepUserTime;
                uid.mLastStepSystemTime = uid.mCurStepSystemTime;
                i++;
            }
            this.mLastStepCpuUserTime = this.mCurStepCpuUserTime;
            this.mLastStepCpuSystemTime = this.mCurStepCpuSystemTime;
            this.mLastStepStatUserTime = this.mCurStepStatUserTime;
            this.mLastStepStatSystemTime = this.mCurStepStatSystemTime;
            this.mLastStepStatIOWaitTime = this.mCurStepStatIOWaitTime;
            this.mLastStepStatIrqTime = this.mCurStepStatIrqTime;
            this.mLastStepStatSoftIrqTime = this.mCurStepStatSoftIrqTime;
            this.mLastStepStatIdleTime = this.mCurStepStatIdleTime;
            tmp.clear();
            return;
        }
        out.userTime = (int) (this.mCurStepCpuUserTime - this.mLastStepCpuUserTime);
        out.systemTime = (int) (this.mCurStepCpuSystemTime - this.mLastStepCpuSystemTime);
        out.statUserTime = (int) (this.mCurStepStatUserTime - this.mLastStepStatUserTime);
        out.statSystemTime = (int) (this.mCurStepStatSystemTime - this.mLastStepStatSystemTime);
        out.statIOWaitTime = (int) (this.mCurStepStatIOWaitTime - this.mLastStepStatIOWaitTime);
        out.statIrqTime = (int) (this.mCurStepStatIrqTime - this.mLastStepStatIrqTime);
        out.statSoftIrqTime = (int) (this.mCurStepStatSoftIrqTime - this.mLastStepStatSoftIrqTime);
        out.statIdlTime = (int) (this.mCurStepStatIdleTime - this.mLastStepStatIdleTime);
        out.appCpuUid3 = -1;
        out.appCpuUid2 = -1;
        out.appCpuUid1 = -1;
        out.appCpuUTime3 = 0;
        out.appCpuUTime2 = 0;
        out.appCpuUTime1 = 0;
        out.appCpuSTime3 = 0;
        out.appCpuSTime2 = 0;
        out.appCpuSTime1 = 0;
        int NU2 = this.mUidStats.size();
        while (i < NU2) {
            Uid uid2 = this.mUidStats.valueAt(i);
            int totalUTime = (int) (uid2.mCurStepUserTime - uid2.mLastStepUserTime);
            int totalSTime = (int) (uid2.mCurStepSystemTime - uid2.mLastStepSystemTime);
            int totalTime = totalUTime + totalSTime;
            uid2.mLastStepUserTime = uid2.mCurStepUserTime;
            uid2.mLastStepSystemTime = uid2.mCurStepSystemTime;
            if (totalTime > out.appCpuUTime3 + out.appCpuSTime3) {
                if (totalTime <= out.appCpuUTime2 + out.appCpuSTime2) {
                    out.appCpuUid3 = uid2.mUid;
                    out.appCpuUTime3 = totalUTime;
                    out.appCpuSTime3 = totalSTime;
                } else {
                    out.appCpuUid3 = out.appCpuUid2;
                    out.appCpuUTime3 = out.appCpuUTime2;
                    out.appCpuSTime3 = out.appCpuSTime2;
                    if (totalTime <= out.appCpuUTime1 + out.appCpuSTime1) {
                        out.appCpuUid2 = uid2.mUid;
                        out.appCpuUTime2 = totalUTime;
                        out.appCpuSTime2 = totalSTime;
                    } else {
                        out.appCpuUid2 = out.appCpuUid1;
                        out.appCpuUTime2 = out.appCpuUTime1;
                        out.appCpuSTime2 = out.appCpuSTime1;
                        out.appCpuUid1 = uid2.mUid;
                        out.appCpuUTime1 = totalUTime;
                        out.appCpuSTime1 = totalSTime;
                    }
                }
            }
            i++;
        }
        this.mLastStepCpuUserTime = this.mCurStepCpuUserTime;
        this.mLastStepCpuSystemTime = this.mCurStepCpuSystemTime;
        this.mLastStepStatUserTime = this.mCurStepStatUserTime;
        this.mLastStepStatSystemTime = this.mCurStepStatSystemTime;
        this.mLastStepStatIOWaitTime = this.mCurStepStatIOWaitTime;
        this.mLastStepStatIrqTime = this.mCurStepStatIrqTime;
        this.mLastStepStatSoftIrqTime = this.mCurStepStatSoftIrqTime;
        this.mLastStepStatIdleTime = this.mCurStepStatIdleTime;
    }

    public void readHistoryDelta(Parcel src, BatteryStats.HistoryItem cur) {
        int batteryLevelInt;
        int firstToken = src.readInt();
        int deltaTimeToken = 524287 & firstToken;
        cur.cmd = 0;
        cur.numReadInts = 1;
        if (deltaTimeToken < DELTA_TIME_ABS) {
            cur.time += (long) deltaTimeToken;
        } else if (deltaTimeToken == DELTA_TIME_ABS) {
            cur.time = src.readLong();
            cur.numReadInts += 2;
            cur.readFromParcel(src);
            return;
        } else if (deltaTimeToken == DELTA_TIME_INT) {
            cur.time += (long) src.readInt();
            cur.numReadInts++;
        } else {
            cur.time += src.readLong();
            cur.numReadInts += 2;
        }
        if ((524288 & firstToken) != 0) {
            batteryLevelInt = src.readInt();
            readBatteryLevelInt(batteryLevelInt, cur);
            cur.numReadInts++;
        } else {
            batteryLevelInt = 0;
        }
        if ((DELTA_STATE_FLAG & firstToken) != 0) {
            int stateInt = src.readInt();
            cur.states = (16777215 & stateInt) | (DELTA_STATE_MASK & firstToken);
            cur.batteryStatus = (byte) ((stateInt >> 29) & 7);
            cur.batteryHealth = (byte) ((stateInt >> 26) & 7);
            cur.batteryPlugType = (byte) ((stateInt >> 24) & 3);
            switch (cur.batteryPlugType) {
                case 1:
                    cur.batteryPlugType = 1;
                    break;
                case 2:
                    cur.batteryPlugType = 2;
                    break;
                case 3:
                    cur.batteryPlugType = 4;
                    break;
            }
            cur.numReadInts++;
        } else {
            cur.states = (firstToken & DELTA_STATE_MASK) | (cur.states & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT);
        }
        if ((DELTA_STATE2_FLAG & firstToken) != 0) {
            cur.states2 = src.readInt();
        }
        if ((DELTA_WAKELOCK_FLAG & firstToken) != 0) {
            int indexes = src.readInt();
            int wakeLockIndex = indexes & 65535;
            int wakeReasonIndex = (indexes >> 16) & 65535;
            if (wakeLockIndex != 65535) {
                cur.wakelockTag = cur.localWakelockTag;
                readHistoryTag(wakeLockIndex, cur.wakelockTag);
            } else {
                cur.wakelockTag = null;
            }
            if (wakeReasonIndex != 65535) {
                cur.wakeReasonTag = cur.localWakeReasonTag;
                readHistoryTag(wakeReasonIndex, cur.wakeReasonTag);
            } else {
                cur.wakeReasonTag = null;
            }
            cur.numReadInts++;
        } else {
            cur.wakelockTag = null;
            cur.wakeReasonTag = null;
        }
        if ((DELTA_EVENT_FLAG & firstToken) != 0) {
            cur.eventTag = cur.localEventTag;
            int codeAndIndex = src.readInt();
            cur.eventCode = codeAndIndex & 65535;
            readHistoryTag((codeAndIndex >> 16) & 65535, cur.eventTag);
            cur.numReadInts++;
        } else {
            cur.eventCode = 0;
        }
        if ((batteryLevelInt & 1) != 0) {
            cur.stepDetails = this.mReadHistoryStepDetails;
            cur.stepDetails.readFromParcel(src);
        } else {
            cur.stepDetails = null;
        }
        if ((16777216 & firstToken) != 0) {
            cur.batteryChargeUAh = src.readInt();
        }
    }

    public void commitCurrentHistoryBatchLocked() {
        this.mHistoryLastWritten.cmd = -1;
    }

    /* access modifiers changed from: package-private */
    public void addHistoryBufferLocked(long elapsedRealtimeMs, BatteryStats.HistoryItem cur) {
        long elapsedRealtimeMs2;
        BatteryStats.HistoryItem historyItem = cur;
        if (this.mHaveBatteryLevel && this.mRecordingHistory) {
            long timeDiff = (this.mHistoryBaseTime + elapsedRealtimeMs) - this.mHistoryLastWritten.time;
            int diffStates = this.mHistoryLastWritten.states ^ (historyItem.states & this.mActiveHistoryStates);
            int diffStates2 = this.mHistoryLastWritten.states2 ^ (historyItem.states2 & this.mActiveHistoryStates2);
            int lastDiffStates = this.mHistoryLastWritten.states ^ this.mHistoryLastLastWritten.states;
            int lastDiffStates2 = this.mHistoryLastWritten.states2 ^ this.mHistoryLastLastWritten.states2;
            if (this.mHistoryBufferLastPos >= 0 && this.mHistoryLastWritten.cmd == 0 && timeDiff < 1000 && (diffStates & lastDiffStates) == 0 && (diffStates2 & lastDiffStates2) == 0 && ((this.mHistoryLastWritten.wakelockTag == null || historyItem.wakelockTag == null) && ((this.mHistoryLastWritten.wakeReasonTag == null || historyItem.wakeReasonTag == null) && this.mHistoryLastWritten.stepDetails == null && ((this.mHistoryLastWritten.eventCode == 0 || historyItem.eventCode == 0) && this.mHistoryLastWritten.batteryLevel == historyItem.batteryLevel && this.mHistoryLastWritten.batteryStatus == historyItem.batteryStatus && this.mHistoryLastWritten.batteryHealth == historyItem.batteryHealth && this.mHistoryLastWritten.batteryPlugType == historyItem.batteryPlugType && this.mHistoryLastWritten.batteryTemperature == historyItem.batteryTemperature && this.mHistoryLastWritten.batteryVoltage == historyItem.batteryVoltage)))) {
                this.mHistoryBuffer.setDataSize(this.mHistoryBufferLastPos);
                this.mHistoryBuffer.setDataPosition(this.mHistoryBufferLastPos);
                this.mHistoryBufferLastPos = -1;
                elapsedRealtimeMs2 = this.mHistoryLastWritten.time - this.mHistoryBaseTime;
                if (this.mHistoryLastWritten.wakelockTag != null) {
                    historyItem.wakelockTag = historyItem.localWakelockTag;
                    historyItem.wakelockTag.setTo(this.mHistoryLastWritten.wakelockTag);
                }
                if (this.mHistoryLastWritten.wakeReasonTag != null) {
                    historyItem.wakeReasonTag = historyItem.localWakeReasonTag;
                    historyItem.wakeReasonTag.setTo(this.mHistoryLastWritten.wakeReasonTag);
                }
                if (this.mHistoryLastWritten.eventCode != 0) {
                    historyItem.eventCode = this.mHistoryLastWritten.eventCode;
                    historyItem.eventTag = historyItem.localEventTag;
                    historyItem.eventTag.setTo(this.mHistoryLastWritten.eventTag);
                }
                this.mHistoryLastWritten.setTo(this.mHistoryLastLastWritten);
            } else {
                elapsedRealtimeMs2 = elapsedRealtimeMs;
            }
            boolean recordResetDueToOverflow = false;
            int dataSize = this.mHistoryBuffer.dataSize();
            if (dataSize >= MAX_MAX_HISTORY_BUFFER * 3) {
                resetAllStatsLocked();
                recordResetDueToOverflow = true;
                long j = timeDiff;
            } else if (dataSize < MAX_HISTORY_BUFFER) {
            } else if (!this.mHistoryOverflow) {
                this.mHistoryOverflow = true;
                addHistoryBufferLocked(elapsedRealtimeMs2, (byte) 0, historyItem);
                addHistoryBufferLocked(elapsedRealtimeMs2, (byte) 6, historyItem);
                return;
            } else {
                boolean writeAnyway = false;
                int curStates = historyItem.states & -1900544 & this.mActiveHistoryStates;
                if (this.mHistoryLastWritten.states != curStates) {
                    int old = this.mActiveHistoryStates;
                    this.mActiveHistoryStates &= curStates | 1900543;
                    writeAnyway = false | (old != this.mActiveHistoryStates);
                }
                int curStates2 = historyItem.states2 & 1748959232 & this.mActiveHistoryStates2;
                if (this.mHistoryLastWritten.states2 != curStates2) {
                    int old2 = this.mActiveHistoryStates2;
                    long j2 = timeDiff;
                    this.mActiveHistoryStates2 &= -1748959233 | curStates2;
                    writeAnyway |= old2 != this.mActiveHistoryStates2;
                }
                if (writeAnyway || this.mHistoryLastWritten.batteryLevel != historyItem.batteryLevel || (dataSize < MAX_MAX_HISTORY_BUFFER && ((this.mHistoryLastWritten.states ^ historyItem.states) & 1835008) != 0 && ((this.mHistoryLastWritten.states2 ^ historyItem.states2) & -1749024768) != 0)) {
                    addHistoryBufferLocked(elapsedRealtimeMs2, (byte) 0, historyItem);
                    return;
                }
                return;
            }
            if (dataSize == 0 || recordResetDueToOverflow) {
                historyItem.currentTime = System.currentTimeMillis();
                if (recordResetDueToOverflow) {
                    addHistoryBufferLocked(elapsedRealtimeMs2, (byte) 6, historyItem);
                }
                addHistoryBufferLocked(elapsedRealtimeMs2, (byte) 7, historyItem);
            }
            addHistoryBufferLocked(elapsedRealtimeMs2, (byte) 0, historyItem);
        }
    }

    private void addHistoryBufferLocked(long elapsedRealtimeMs, byte cmd, BatteryStats.HistoryItem cur) {
        if (!this.mIteratingHistory) {
            this.mHistoryBufferLastPos = this.mHistoryBuffer.dataPosition();
            this.mHistoryLastLastWritten.setTo(this.mHistoryLastWritten);
            this.mHistoryLastWritten.setTo(this.mHistoryBaseTime + elapsedRealtimeMs, cmd, cur);
            this.mHistoryLastWritten.states &= this.mActiveHistoryStates;
            this.mHistoryLastWritten.states2 &= this.mActiveHistoryStates2;
            writeHistoryDelta(this.mHistoryBuffer, this.mHistoryLastWritten, this.mHistoryLastLastWritten);
            this.mLastHistoryElapsedRealtime = elapsedRealtimeMs;
            cur.wakelockTag = null;
            cur.wakeReasonTag = null;
            cur.eventCode = 0;
            cur.eventTag = null;
            return;
        }
        throw new IllegalStateException("Can't do this while iterating history!");
    }

    /* access modifiers changed from: package-private */
    public void addHistoryRecordLocked(long elapsedRealtimeMs, long uptimeMs) {
        if (this.mTrackRunningHistoryElapsedRealtime != 0) {
            long diffElapsed = elapsedRealtimeMs - this.mTrackRunningHistoryElapsedRealtime;
            long diffUptime = uptimeMs - this.mTrackRunningHistoryUptime;
            if (diffUptime < diffElapsed - 20) {
                this.mHistoryAddTmp.setTo(this.mHistoryLastWritten);
                this.mHistoryAddTmp.wakelockTag = null;
                this.mHistoryAddTmp.wakeReasonTag = null;
                this.mHistoryAddTmp.eventCode = 0;
                this.mHistoryAddTmp.states &= Integer.MAX_VALUE;
                addHistoryRecordInnerLocked(elapsedRealtimeMs - (diffElapsed - diffUptime), this.mHistoryAddTmp);
            }
        }
        this.mHistoryCur.states |= Integer.MIN_VALUE;
        this.mTrackRunningHistoryElapsedRealtime = elapsedRealtimeMs;
        this.mTrackRunningHistoryUptime = uptimeMs;
        addHistoryRecordInnerLocked(elapsedRealtimeMs, this.mHistoryCur);
    }

    /* access modifiers changed from: package-private */
    public void addHistoryRecordInnerLocked(long elapsedRealtimeMs, BatteryStats.HistoryItem cur) {
        addHistoryBufferLocked(elapsedRealtimeMs, cur);
    }

    public void addHistoryEventLocked(long elapsedRealtimeMs, long uptimeMs, int code, String name, int uid) {
        this.mHistoryCur.eventCode = code;
        this.mHistoryCur.eventTag = this.mHistoryCur.localEventTag;
        this.mHistoryCur.eventTag.string = name;
        this.mHistoryCur.eventTag.uid = uid;
        addHistoryRecordLocked(elapsedRealtimeMs, uptimeMs);
    }

    /* access modifiers changed from: package-private */
    public void addHistoryRecordLocked(long elapsedRealtimeMs, long uptimeMs, byte cmd, BatteryStats.HistoryItem cur) {
        BatteryStats.HistoryItem rec = this.mHistoryCache;
        if (rec != null) {
            this.mHistoryCache = rec.next;
        } else {
            rec = new BatteryStats.HistoryItem();
        }
        rec.setTo(this.mHistoryBaseTime + elapsedRealtimeMs, cmd, cur);
        addHistoryRecordLocked(rec);
    }

    /* access modifiers changed from: package-private */
    public void addHistoryRecordLocked(BatteryStats.HistoryItem rec) {
        this.mNumHistoryItems++;
        rec.next = null;
        this.mHistoryLastEnd = this.mHistoryEnd;
        if (this.mHistoryEnd != null) {
            this.mHistoryEnd.next = rec;
            this.mHistoryEnd = rec;
            return;
        }
        this.mHistoryEnd = rec;
        this.mHistory = rec;
    }

    /* access modifiers changed from: package-private */
    public void clearHistoryLocked() {
        this.mHistoryBaseTime = 0;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryBuffer.setDataSize(0);
        this.mHistoryBuffer.setDataPosition(0);
        this.mHistoryBuffer.setDataCapacity(MAX_HISTORY_BUFFER / 2);
        this.mHistoryLastLastWritten.clear();
        this.mHistoryLastWritten.clear();
        this.mHistoryTagPool.clear();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = false;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
    }

    @GuardedBy("this")
    public void updateTimeBasesLocked(boolean unplugged, int screenState, long uptime, long realtime) {
        boolean z = unplugged;
        long j = uptime;
        long j2 = realtime;
        boolean screenOff = !isScreenOn(screenState);
        boolean updateOnBatteryTimeBase = z != this.mOnBatteryTimeBase.isRunning();
        boolean updateOnBatteryScreenOffTimeBase = (z && screenOff) != this.mOnBatteryScreenOffTimeBase.isRunning();
        if (updateOnBatteryScreenOffTimeBase || updateOnBatteryTimeBase) {
            if (updateOnBatteryScreenOffTimeBase) {
                updateKernelWakelocksLocked();
                updateBatteryPropertiesLocked();
            }
            if (updateOnBatteryTimeBase) {
                updateRpmStatsLocked();
            }
            this.mOnBatteryTimeBase.setRunning(z, j, j2);
            if (updateOnBatteryTimeBase) {
                for (int i = this.mUidStats.size() - 1; i >= 0; i--) {
                    this.mUidStats.valueAt(i).updateOnBatteryBgTimeBase(j, j2);
                }
            }
            if (updateOnBatteryScreenOffTimeBase) {
                this.mOnBatteryScreenOffTimeBase.setRunning(z && screenOff, j, j2);
                for (int i2 = this.mUidStats.size() - 1; i2 >= 0; i2--) {
                    this.mUidStats.valueAt(i2).updateOnBatteryScreenOffBgTimeBase(j, j2);
                }
            }
        }
    }

    private void updateBatteryPropertiesLocked() {
        try {
            IBatteryPropertiesRegistrar registrar = IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getService("batteryproperties"));
            if (registrar != null) {
                registrar.scheduleUpdate();
            }
        } catch (RemoteException e) {
        }
    }

    public void addIsolatedUidLocked(int isolatedUid, int appUid) {
        this.mIsolatedUids.put(isolatedUid, appUid);
        StatsLog.write(43, appUid, isolatedUid, 1);
        getUidStatsLocked(appUid).addIsolatedUid(isolatedUid);
    }

    public void scheduleRemoveIsolatedUidLocked(int isolatedUid, int appUid) {
        if (this.mIsolatedUids.get(isolatedUid, -1) == appUid && this.mExternalSync != null) {
            this.mExternalSync.scheduleCpuSyncDueToRemovedUid(isolatedUid);
        }
    }

    @GuardedBy("this")
    public void removeIsolatedUidLocked(int isolatedUid) {
        StatsLog.write(43, this.mIsolatedUids.get(isolatedUid, -1), isolatedUid, 0);
        int idx = this.mIsolatedUids.indexOfKey(isolatedUid);
        if (idx >= 0) {
            getUidStatsLocked(this.mIsolatedUids.valueAt(idx)).removeIsolatedUid(isolatedUid);
            this.mIsolatedUids.removeAt(idx);
        }
        this.mPendingRemovedUids.add(new UidToRemove(this, isolatedUid, this.mClocks.elapsedRealtime()));
    }

    public int mapUid(int uid) {
        int isolated = this.mIsolatedUids.get(uid, -1);
        return isolated > 0 ? isolated : uid;
    }

    public void noteEventLocked(int code, String name, int uid) {
        int uid2 = mapUid(uid);
        if (this.mActiveEvents.updateState(code, name, uid2, 0)) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), code, name, uid2);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean ensureStartClockTime(long currentTime) {
        if ((currentTime <= 31536000000L || this.mStartClockTime >= currentTime - 31536000000L) && this.mStartClockTime <= currentTime) {
            return false;
        }
        this.mStartClockTime = currentTime - (this.mClocks.elapsedRealtime() - (this.mRealtimeStart / 1000));
        return true;
    }

    public void noteCurrentTimeChangedLocked() {
        long currentTime = System.currentTimeMillis();
        recordCurrentTimeChangeLocked(currentTime, this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis());
        ensureStartClockTime(currentTime);
    }

    public void noteProcessStartLocked(String name, int uid) {
        int uid2 = mapUid(uid);
        if (isOnBattery()) {
            getUidStatsLocked(uid2).getProcessStatsLocked(name).incStartsLocked();
        }
        if (this.mActiveEvents.updateState(32769, name, uid2, 0) && this.mRecordAllHistory) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 32769, name, uid2);
        }
    }

    public void noteProcessCrashLocked(String name, int uid) {
        int uid2 = mapUid(uid);
        if (isOnBattery()) {
            getUidStatsLocked(uid2).getProcessStatsLocked(name).incNumCrashesLocked();
        }
    }

    public void noteProcessAnrLocked(String name, int uid) {
        int uid2 = mapUid(uid);
        if (isOnBattery()) {
            getUidStatsLocked(uid2).getProcessStatsLocked(name).incNumAnrsLocked();
        }
    }

    public void noteUidProcessStateLocked(int uid, int state) {
        if (uid == mapUid(uid)) {
            getUidStatsLocked(uid).updateUidProcessStateLocked(state);
        }
    }

    public void noteProcessFinishLocked(String name, int uid) {
        int uid2 = mapUid(uid);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT1, name, uid2, 0) && this.mRecordAllHistory) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), GL10.GL_LIGHT1, name, uid2);
        }
    }

    public void noteSyncStartLocked(String name, int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid2).noteStartSyncLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(32772, name, uid2, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 32772, name, uid2);
        }
    }

    public void noteSyncFinishLocked(String name, int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid2).noteStopSyncLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT4, name, uid2, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT4, name, uid2);
        }
    }

    public void noteJobStartLocked(String name, int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid2).noteStartJobLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL11ExtensionPack.GL_FUNC_ADD, name, uid2, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL11ExtensionPack.GL_FUNC_ADD, name, uid2);
        }
    }

    public void noteJobFinishLocked(String name, int uid, int stopReason) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid2).noteStopJobLocked(name, elapsedRealtime, stopReason);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT6, name, uid2, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT6, name, uid2);
        }
    }

    public void noteJobsDeferredLocked(int uid, int numDeferred, long sinceLast) {
        getUidStatsLocked(mapUid(uid)).noteJobsDeferredLocked(numDeferred, sinceLast);
    }

    public void noteAlarmStartLocked(String name, WorkSource workSource, int uid) {
        noteAlarmStartOrFinishLocked(32781, name, workSource, uid);
    }

    public void noteAlarmFinishLocked(String name, WorkSource workSource, int uid) {
        noteAlarmStartOrFinishLocked(16397, name, workSource, uid);
    }

    private void noteAlarmStartOrFinishLocked(int historyItem, String name, WorkSource workSource, int uid) {
        int i;
        int uid2;
        long uptime;
        int i2;
        int uid3;
        int i3;
        int uid4 = historyItem;
        String str = name;
        WorkSource workSource2 = workSource;
        if (this.mRecordAllHistory) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime2 = this.mClocks.uptimeMillis();
            int i4 = 0;
            if (workSource2 != null) {
                int uid5 = uid;
                int i5 = 0;
                while (true) {
                    int i6 = i5;
                    if (i6 >= workSource.size()) {
                        break;
                    }
                    int uid6 = mapUid(workSource2.get(i6));
                    if (this.mActiveEvents.updateState(uid4, str, uid6, i4)) {
                        uid3 = uid6;
                        i2 = i6;
                        uptime = uptime2;
                        i3 = i4;
                        addHistoryEventLocked(elapsedRealtime, uptime2, uid4, str, uid3);
                    } else {
                        uid3 = uid6;
                        i2 = i6;
                        uptime = uptime2;
                        i3 = i4;
                    }
                    i5 = i2 + 1;
                    i4 = i3;
                    uid5 = uid3;
                    uptime2 = uptime;
                }
                long uptime3 = uptime2;
                int i7 = i4;
                List<WorkSource.WorkChain> workChains = workSource.getWorkChains();
                if (workChains != null) {
                    int i8 = i7;
                    while (true) {
                        int i9 = i8;
                        if (i9 >= workChains.size()) {
                            break;
                        }
                        int uid7 = mapUid(workChains.get(i9).getAttributionUid());
                        if (this.mActiveEvents.updateState(uid4, str, uid7, i7)) {
                            uid2 = uid7;
                            i = i9;
                            addHistoryEventLocked(elapsedRealtime, uptime3, uid4, str, uid2);
                        } else {
                            uid2 = uid7;
                            i = i9;
                        }
                        i8 = i + 1;
                        uid5 = uid2;
                    }
                }
                int i10 = uid5;
            } else {
                long uptime4 = uptime2;
                int uid8 = mapUid(uid);
                if (this.mActiveEvents.updateState(uid4, str, uid8, 0)) {
                    addHistoryEventLocked(elapsedRealtime, uptime4, uid4, str, uid8);
                }
            }
        }
    }

    public void noteWakupAlarmLocked(String packageName, int uid, WorkSource workSource, String tag) {
        if (workSource != null) {
            int uid2 = uid;
            for (int i = 0; i < workSource.size(); i++) {
                uid2 = workSource.get(i);
                String workSourceName = workSource.getName(i);
                if (isOnBattery()) {
                    getPackageStatsLocked(uid2, workSourceName != null ? workSourceName : packageName).noteWakeupAlarmLocked(tag);
                }
                StatsLog.write_non_chained(35, workSource.get(i), workSource.getName(i), tag);
            }
            ArrayList<WorkSource.WorkChain> workChains = workSource.getWorkChains();
            if (workChains != null) {
                for (int i2 = 0; i2 < workChains.size(); i2++) {
                    WorkSource.WorkChain wc = workChains.get(i2);
                    uid2 = wc.getAttributionUid();
                    if (isOnBattery()) {
                        getPackageStatsLocked(uid2, packageName).noteWakeupAlarmLocked(tag);
                    }
                    StatsLog.write(35, wc.getUids(), wc.getTags(), tag);
                }
            }
            int i3 = uid2;
            return;
        }
        if (isOnBattery()) {
            getPackageStatsLocked(uid, packageName).noteWakeupAlarmLocked(tag);
        }
        StatsLog.write_non_chained(35, uid, null, tag);
    }

    private void requestWakelockCpuUpdate() {
        this.mExternalSync.scheduleCpuSyncDueToWakelockChange(DELAY_UPDATE_WAKELOCKS);
    }

    private void requestImmediateCpuUpdate() {
        this.mExternalSync.scheduleCpuSyncDueToWakelockChange(0);
    }

    public void setRecordAllHistoryLocked(boolean enabled) {
        boolean z = enabled;
        this.mRecordAllHistory = z;
        if (!z) {
            this.mActiveEvents.removeEvents(5);
            this.mActiveEvents.removeEvents(13);
            HashMap<String, SparseIntArray> active = this.mActiveEvents.getStateForEvent(1);
            if (active != null) {
                long mSecRealtime = this.mClocks.elapsedRealtime();
                long mSecUptime = this.mClocks.uptimeMillis();
                Iterator<Map.Entry<String, SparseIntArray>> it = active.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, SparseIntArray> ent = it.next();
                    SparseIntArray uids = ent.getValue();
                    int j = 0;
                    while (true) {
                        int j2 = j;
                        if (j2 >= uids.size()) {
                            break;
                        }
                        addHistoryEventLocked(mSecRealtime, mSecUptime, GL10.GL_LIGHT1, ent.getKey(), uids.keyAt(j2));
                        j = j2 + 1;
                        ent = ent;
                        it = it;
                        uids = uids;
                    }
                    Iterator<Map.Entry<String, SparseIntArray>> it2 = it;
                }
                return;
            }
            return;
        }
        HashMap<String, SparseIntArray> active2 = this.mActiveEvents.getStateForEvent(1);
        if (active2 != null) {
            long mSecRealtime2 = this.mClocks.elapsedRealtime();
            long mSecUptime2 = this.mClocks.uptimeMillis();
            Iterator<Map.Entry<String, SparseIntArray>> it3 = active2.entrySet().iterator();
            while (it3.hasNext()) {
                Map.Entry<String, SparseIntArray> ent2 = it3.next();
                SparseIntArray uids2 = ent2.getValue();
                int j3 = 0;
                while (true) {
                    int j4 = j3;
                    if (j4 >= uids2.size()) {
                        break;
                    }
                    addHistoryEventLocked(mSecRealtime2, mSecUptime2, 32769, ent2.getKey(), uids2.keyAt(j4));
                    j3 = j4 + 1;
                    ent2 = ent2;
                    it3 = it3;
                    uids2 = uids2;
                }
                Iterator<Map.Entry<String, SparseIntArray>> it4 = it3;
            }
        }
    }

    public void setNoAutoReset(boolean enabled) {
        this.mNoAutoReset = enabled;
    }

    public void setPretendScreenOff(boolean pretendScreenOff) {
        if (this.mPretendScreenOff != pretendScreenOff) {
            this.mPretendScreenOff = pretendScreenOff;
            noteScreenStateLocked(pretendScreenOff ? 1 : 2);
        }
    }

    public void noteStartWakeLocked(int uid, int pid, WorkSource.WorkChain wc, String name, String historyName, int type, boolean unimportantForLogging, long elapsedRealtime, long uptime) {
        String historyName2;
        int i = type;
        long j = elapsedRealtime;
        long j2 = uptime;
        int uid2 = mapUid(uid);
        if (i == 0) {
            aggregateLastWakeupUptimeLocked(j2);
            String historyName3 = historyName == null ? name : historyName;
            if (!this.mRecordAllHistory || !this.mActiveEvents.updateState(32773, historyName3, uid2, 0)) {
                historyName2 = historyName3;
            } else {
                historyName2 = historyName3;
                addHistoryEventLocked(j, j2, 32773, historyName3, uid2);
            }
            if (this.mWakeLockNesting == 0) {
                this.mHistoryCur.states |= 1073741824;
                this.mHistoryCur.wakelockTag = this.mHistoryCur.localWakelockTag;
                BatteryStats.HistoryTag historyTag = this.mHistoryCur.wakelockTag;
                this.mInitialAcquireWakeName = historyName2;
                historyTag.string = historyName2;
                BatteryStats.HistoryTag historyTag2 = this.mHistoryCur.wakelockTag;
                this.mInitialAcquireWakeUid = uid2;
                historyTag2.uid = uid2;
                this.mWakeLockImportant = !unimportantForLogging;
                addHistoryRecordLocked(j, j2);
            } else if (!this.mWakeLockImportant && !unimportantForLogging && this.mHistoryLastWritten.cmd == 0) {
                if (this.mHistoryLastWritten.wakelockTag != null) {
                    this.mHistoryLastWritten.wakelockTag = null;
                    this.mHistoryCur.wakelockTag = this.mHistoryCur.localWakelockTag;
                    BatteryStats.HistoryTag historyTag3 = this.mHistoryCur.wakelockTag;
                    this.mInitialAcquireWakeName = historyName2;
                    historyTag3.string = historyName2;
                    BatteryStats.HistoryTag historyTag4 = this.mHistoryCur.wakelockTag;
                    this.mInitialAcquireWakeUid = uid2;
                    historyTag4.uid = uid2;
                    addHistoryRecordLocked(j, j2);
                }
                this.mWakeLockImportant = true;
            }
            this.mWakeLockNesting++;
        }
        if (uid2 >= 0) {
            if (this.mOnBatteryScreenOffTimeBase.isRunning()) {
                requestWakelockCpuUpdate();
            }
            int i2 = type;
            getUidStatsLocked(uid2).noteStartWakeLocked(pid, name, i2, j);
            if (wc != null) {
                StatsLog.write(10, wc.getUids(), wc.getTags(), getPowerManagerWakeLockLevel(i2), name, 1);
            } else {
                StatsLog.write_non_chained(10, uid2, null, getPowerManagerWakeLockLevel(i2), name, 1);
            }
        } else {
            int i3 = type;
        }
    }

    public void noteStopWakeLocked(int uid, int pid, WorkSource.WorkChain wc, String name, String historyName, int type, long elapsedRealtime, long uptime) {
        long j;
        int i = type;
        int uid2 = mapUid(uid);
        if (i == 0) {
            this.mWakeLockNesting--;
            if (this.mRecordAllHistory) {
                String historyName2 = historyName == null ? name : historyName;
                if (this.mActiveEvents.updateState(GL10.GL_LIGHT5, historyName2, uid2, 0)) {
                    addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT5, historyName2, uid2);
                }
            }
            if (this.mWakeLockNesting == 0) {
                this.mHistoryCur.states &= -1073741825;
                this.mInitialAcquireWakeName = null;
                this.mInitialAcquireWakeUid = -1;
                j = elapsedRealtime;
                addHistoryRecordLocked(j, uptime);
            } else {
                j = elapsedRealtime;
                long j2 = uptime;
            }
        } else {
            j = elapsedRealtime;
            long j3 = uptime;
            String str = historyName;
        }
        if (uid2 >= 0) {
            if (this.mOnBatteryScreenOffTimeBase.isRunning()) {
                requestWakelockCpuUpdate();
            }
            getUidStatsLocked(uid2).noteStopWakeLocked(pid, name, i, j);
            if (wc != null) {
                StatsLog.write(10, wc.getUids(), wc.getTags(), getPowerManagerWakeLockLevel(i), name, 0);
            } else {
                StatsLog.write_non_chained(10, uid2, null, getPowerManagerWakeLockLevel(i), name, 0);
            }
        }
    }

    private int getPowerManagerWakeLockLevel(int battertStatsWakelockType) {
        if (battertStatsWakelockType == 18) {
            return 128;
        }
        switch (battertStatsWakelockType) {
            case 0:
                return 1;
            case 1:
                return 26;
            case 2:
                Slog.e(TAG, "Illegal window wakelock type observed in batterystats.");
                return -1;
            default:
                Slog.e(TAG, "Illegal wakelock type in batterystats: " + battertStatsWakelockType);
                return -1;
        }
    }

    public void noteStartWakeFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int N = ws.size();
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= N) {
                break;
            }
            noteStartWakeLocked(ws.get(i3), pid, null, name, historyName, type, unimportantForLogging, elapsedRealtime, uptime);
            i2 = i3 + 1;
            N = N;
        }
        List<WorkSource.WorkChain> wcs = ws.getWorkChains();
        if (wcs != null) {
            while (true) {
                int i4 = i;
                if (i4 >= wcs.size()) {
                    break;
                }
                WorkSource.WorkChain wc = wcs.get(i4);
                WorkSource.WorkChain workChain = wc;
                noteStartWakeLocked(wc.getAttributionUid(), pid, wc, name, historyName, type, unimportantForLogging, elapsedRealtime, uptime);
                i = i4 + 1;
                wcs = wcs;
            }
        }
    }

    public void noteChangeWakelockFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) {
        WorkSource workSource = ws;
        WorkSource workSource2 = newWs;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        List<WorkSource.WorkChain>[] wcs = WorkSource.diffChains(workSource, workSource2);
        int NN = newWs.size();
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= NN) {
                break;
            }
            noteStartWakeLocked(workSource2.get(i3), newPid, null, newName, newHistoryName, newType, newUnimportantForLogging, elapsedRealtime, uptime);
            i2 = i3 + 1;
            NN = NN;
        }
        if (wcs != null) {
            List<WorkSource.WorkChain> newChains = wcs[0];
            if (newChains != null) {
                int i4 = 0;
                while (true) {
                    int i5 = i4;
                    if (i5 >= newChains.size()) {
                        break;
                    }
                    WorkSource.WorkChain newChain = newChains.get(i5);
                    WorkSource.WorkChain workChain = newChain;
                    noteStartWakeLocked(newChain.getAttributionUid(), newPid, newChain, newName, newHistoryName, newType, newUnimportantForLogging, elapsedRealtime, uptime);
                    i4 = i5 + 1;
                    newChains = newChains;
                }
            }
        }
        int NO = ws.size();
        int i6 = 0;
        while (true) {
            int i7 = i6;
            if (i7 >= NO) {
                break;
            }
            noteStopWakeLocked(workSource.get(i7), pid, null, name, historyName, type, elapsedRealtime, uptime);
            i6 = i7 + 1;
        }
        if (wcs != null) {
            List<WorkSource.WorkChain> goneChains = wcs[1];
            if (goneChains != null) {
                while (true) {
                    int i8 = i;
                    if (i8 < goneChains.size()) {
                        WorkSource.WorkChain goneChain = goneChains.get(i8);
                        WorkSource.WorkChain workChain2 = goneChain;
                        noteStopWakeLocked(goneChain.getAttributionUid(), pid, goneChain, name, historyName, type, elapsedRealtime, uptime);
                        i = i8 + 1;
                        goneChains = goneChains;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public void noteStopWakeFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int N = ws.size();
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= N) {
                break;
            }
            noteStopWakeLocked(ws.get(i3), pid, null, name, historyName, type, elapsedRealtime, uptime);
            i2 = i3 + 1;
            N = N;
        }
        List<WorkSource.WorkChain> wcs = ws.getWorkChains();
        if (wcs != null) {
            while (true) {
                int i4 = i;
                if (i4 >= wcs.size()) {
                    break;
                }
                WorkSource.WorkChain wc = wcs.get(i4);
                WorkSource.WorkChain workChain = wc;
                noteStopWakeLocked(wc.getAttributionUid(), pid, wc, name, historyName, type, elapsedRealtime, uptime);
                i = i4 + 1;
                wcs = wcs;
            }
        }
    }

    public void noteLongPartialWakelockStart(String name, String historyName, int uid) {
        StatsLog.write_non_chained(11, uid, null, name, historyName, 1);
        noteLongPartialWakeLockStartInternal(name, historyName, mapUid(uid));
    }

    public void noteLongPartialWakelockStartFromSource(String name, String historyName, WorkSource workSource) {
        int N = workSource.size();
        for (int i = 0; i < N; i++) {
            noteLongPartialWakeLockStartInternal(name, historyName, mapUid(workSource.get(i)));
            StatsLog.write_non_chained(11, workSource.get(i), workSource.getName(i), name, historyName, 1);
        }
        ArrayList<WorkSource.WorkChain> workChains = workSource.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                WorkSource.WorkChain workChain = workChains.get(i2);
                noteLongPartialWakeLockStartInternal(name, historyName, workChain.getAttributionUid());
                StatsLog.write(11, workChain.getUids(), workChain.getTags(), name, historyName, 1);
            }
        }
    }

    private void noteLongPartialWakeLockStartInternal(String name, String historyName, int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        String historyName2 = historyName == null ? name : historyName;
        int i = uid;
        if (this.mActiveEvents.updateState(32788, historyName2, i, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 32788, historyName2, i);
        }
    }

    public void noteLongPartialWakelockFinish(String name, String historyName, int uid) {
        StatsLog.write_non_chained(11, uid, null, name, historyName, 0);
        noteLongPartialWakeLockFinishInternal(name, historyName, mapUid(uid));
    }

    public void noteLongPartialWakelockFinishFromSource(String name, String historyName, WorkSource workSource) {
        int N = workSource.size();
        for (int i = 0; i < N; i++) {
            noteLongPartialWakeLockFinishInternal(name, historyName, mapUid(workSource.get(i)));
            StatsLog.write_non_chained(11, workSource.get(i), workSource.getName(i), name, historyName, 0);
        }
        ArrayList<WorkSource.WorkChain> workChains = workSource.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                WorkSource.WorkChain workChain = workChains.get(i2);
                noteLongPartialWakeLockFinishInternal(name, historyName, workChain.getAttributionUid());
                StatsLog.write(11, workChain.getUids(), workChain.getTags(), name, historyName, 0);
            }
        }
    }

    private void noteLongPartialWakeLockFinishInternal(String name, String historyName, int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        String historyName2 = historyName == null ? name : historyName;
        int i = uid;
        if (this.mActiveEvents.updateState(16404, historyName2, i, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 16404, historyName2, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void aggregateLastWakeupUptimeLocked(long uptimeMs) {
        if (this.mLastWakeupReason != null) {
            long deltaUptime = uptimeMs - this.mLastWakeupUptimeMs;
            getWakeupReasonTimerLocked(this.mLastWakeupReason).add(deltaUptime * 1000, 1);
            StatsLog.write(36, this.mLastWakeupReason, 1000 * deltaUptime);
            this.mLastWakeupReason = null;
        }
    }

    public void noteWakeupReasonLocked(String reason) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        aggregateLastWakeupUptimeLocked(uptime);
        this.mHistoryCur.wakeReasonTag = this.mHistoryCur.localWakeReasonTag;
        this.mHistoryCur.wakeReasonTag.string = reason;
        this.mHistoryCur.wakeReasonTag.uid = 0;
        this.mLastWakeupReason = reason;
        this.mLastWakeupUptimeMs = uptime;
        addHistoryRecordLocked(elapsedRealtime, uptime);
    }

    public boolean startAddingCpuLocked() {
        this.mExternalSync.cancelCpuSyncDueToWakelockChange();
        return this.mOnBatteryInternal;
    }

    public void finishAddingCpuLocked(int totalUTime, int totalSTime, int statUserTime, int statSystemTime, int statIOWaitTime, int statIrqTime, int statSoftIrqTime, int statIdleTime) {
        this.mCurStepCpuUserTime += (long) totalUTime;
        this.mCurStepCpuSystemTime += (long) totalSTime;
        this.mCurStepStatUserTime += (long) statUserTime;
        this.mCurStepStatSystemTime += (long) statSystemTime;
        this.mCurStepStatIOWaitTime += (long) statIOWaitTime;
        this.mCurStepStatIrqTime += (long) statIrqTime;
        this.mCurStepStatSoftIrqTime += (long) statSoftIrqTime;
        this.mCurStepStatIdleTime += (long) statIdleTime;
    }

    public void noteProcessDiedLocked(int uid, int pid) {
        Uid u = this.mUidStats.get(mapUid(uid));
        if (u != null) {
            u.mPids.remove(pid);
        }
    }

    public long getProcessWakeTime(int uid, int pid, long realtime) {
        Uid u = this.mUidStats.get(mapUid(uid));
        long j = 0;
        if (u != null) {
            BatteryStats.Uid.Pid p = u.mPids.get(pid);
            if (p != null) {
                long j2 = p.mWakeSumMs;
                if (p.mWakeNesting > 0) {
                    j = realtime - p.mWakeStartMs;
                }
                return j2 + j;
            }
        }
        return 0;
    }

    public void reportExcessiveCpuLocked(int uid, String proc, long overTime, long usedTime) {
        Uid u = this.mUidStats.get(mapUid(uid));
        if (u != null) {
            u.reportExcessiveCpuLocked(proc, overTime, usedTime);
        }
    }

    public void noteStartSensorLocked(int uid, int sensor) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mSensorNesting == 0) {
            this.mHistoryCur.states |= DELTA_EVENT_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mSensorNesting++;
        getUidStatsLocked(uid2).noteStartSensor(sensor, elapsedRealtime);
    }

    public void noteStopSensorLocked(int uid, int sensor) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mSensorNesting--;
        if (this.mSensorNesting == 0) {
            this.mHistoryCur.states &= -8388609;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid2).noteStopSensor(sensor, elapsedRealtime);
    }

    public void noteGpsChangedLocked(WorkSource oldWs, WorkSource newWs) {
        for (int i = 0; i < newWs.size(); i++) {
            noteStartGpsLocked(newWs.get(i), null);
        }
        for (int i2 = 0; i2 < oldWs.size(); i2++) {
            noteStopGpsLocked(oldWs.get(i2), null);
        }
        List<WorkSource.WorkChain>[] wcs = WorkSource.diffChains(oldWs, newWs);
        if (wcs != null) {
            if (wcs[0] != null) {
                List<WorkSource.WorkChain> newChains = wcs[0];
                for (int i3 = 0; i3 < newChains.size(); i3++) {
                    noteStartGpsLocked(-1, newChains.get(i3));
                }
            }
            if (wcs[1] != null) {
                List<WorkSource.WorkChain> goneChains = wcs[1];
                for (int i4 = 0; i4 < goneChains.size(); i4++) {
                    noteStopGpsLocked(-1, goneChains.get(i4));
                }
            }
        }
    }

    private void noteStartGpsLocked(int uid, WorkSource.WorkChain workChain) {
        int uid2 = getAttributionUid(uid, workChain);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mGpsNesting == 0) {
            this.mHistoryCur.states |= 536870912;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mGpsNesting++;
        if (workChain == null) {
            StatsLog.write_non_chained(6, uid2, null, 1);
        } else {
            StatsLog.write(6, workChain.getUids(), workChain.getTags(), 1);
        }
        getUidStatsLocked(uid2).noteStartGps(elapsedRealtime);
    }

    private void noteStopGpsLocked(int uid, WorkSource.WorkChain workChain) {
        int uid2 = getAttributionUid(uid, workChain);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mGpsNesting--;
        if (this.mGpsNesting == 0) {
            this.mHistoryCur.states &= -536870913;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            stopAllGpsSignalQualityTimersLocked(-1);
            this.mGpsSignalQualityBin = -1;
        }
        if (workChain == null) {
            StatsLog.write_non_chained(6, uid2, null, 0);
        } else {
            StatsLog.write(6, workChain.getUids(), workChain.getTags(), 0);
        }
        getUidStatsLocked(uid2).noteStopGps(elapsedRealtime);
    }

    public void noteGpsSignalQualityLocked(int signalLevel) {
        if (this.mGpsNesting != 0) {
            if (signalLevel < 0 || signalLevel >= 2) {
                stopAllGpsSignalQualityTimersLocked(-1);
                return;
            }
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            if (this.mGpsSignalQualityBin != signalLevel) {
                if (this.mGpsSignalQualityBin >= 0) {
                    this.mGpsSignalQualityTimer[this.mGpsSignalQualityBin].stopRunningLocked(elapsedRealtime);
                }
                if (!this.mGpsSignalQualityTimer[signalLevel].isRunningLocked()) {
                    this.mGpsSignalQualityTimer[signalLevel].startRunningLocked(elapsedRealtime);
                }
                this.mHistoryCur.states2 = (this.mHistoryCur.states2 & -129) | (signalLevel << 7);
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mGpsSignalQualityBin = signalLevel;
            }
        }
    }

    @GuardedBy("this")
    public void noteScreenStateLocked(int state) {
        int state2 = this.mPretendScreenOff ? 1 : state;
        if (state2 > 4) {
            if (state2 != 5) {
                Slog.wtf(TAG, "Unknown screen state (not mapped): " + state2);
            } else {
                state2 = 2;
            }
        }
        int state3 = state2;
        if (this.mScreenState != state3) {
            recordDailyStatsIfNeededLocked(true);
            int oldState = this.mScreenState;
            this.mScreenState = state3;
            if (state3 != 0) {
                int stepState = state3 - 1;
                if ((stepState & 3) == stepState) {
                    this.mModStepMode |= (this.mCurStepMode & 3) ^ stepState;
                    this.mCurStepMode = (this.mCurStepMode & -4) | stepState;
                } else {
                    Slog.wtf(TAG, "Unexpected screen state: " + state3);
                }
            }
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            boolean updateHistory = false;
            if (isScreenDoze(state3)) {
                this.mHistoryCur.states |= 262144;
                this.mScreenDozeTimer.startRunningLocked(elapsedRealtime);
                updateHistory = true;
            } else if (isScreenDoze(oldState)) {
                this.mHistoryCur.states &= -262145;
                this.mScreenDozeTimer.stopRunningLocked(elapsedRealtime);
                updateHistory = true;
            }
            if (isScreenOn(state3)) {
                this.mHistoryCur.states |= DELTA_STATE_FLAG;
                this.mScreenOnTimer.startRunningLocked(elapsedRealtime);
                if (this.mScreenBrightnessBin >= 0) {
                    this.mScreenBrightnessTimer[this.mScreenBrightnessBin].startRunningLocked(elapsedRealtime);
                }
                updateHistory = true;
            } else if (isScreenOn(oldState)) {
                this.mHistoryCur.states &= -1048577;
                this.mScreenOnTimer.stopRunningLocked(elapsedRealtime);
                if (this.mScreenBrightnessBin >= 0) {
                    this.mScreenBrightnessTimer[this.mScreenBrightnessBin].stopRunningLocked(elapsedRealtime);
                }
                updateHistory = true;
            }
            if (updateHistory) {
                addHistoryRecordLocked(elapsedRealtime, uptime);
            }
            this.mExternalSync.scheduleCpuSyncDueToScreenStateChange(this.mOnBatteryTimeBase.isRunning(), this.mOnBatteryScreenOffTimeBase.isRunning());
            if (isScreenOn(state3)) {
                updateTimeBasesLocked(this.mOnBatteryTimeBase.isRunning(), state3, this.mClocks.uptimeMillis() * 1000, elapsedRealtime * 1000);
                long j = elapsedRealtime;
                noteStartWakeLocked(-1, -1, null, "screen", null, 0, false, elapsedRealtime, uptime);
            } else {
                long uptime2 = uptime;
                long elapsedRealtime2 = elapsedRealtime;
                if (isScreenOn(oldState)) {
                    noteStopWakeLocked(-1, -1, null, "screen", "screen", 0, elapsedRealtime2, uptime2);
                    updateTimeBasesLocked(this.mOnBatteryTimeBase.isRunning(), state3, this.mClocks.uptimeMillis() * 1000, elapsedRealtime2 * 1000);
                }
            }
            if (this.mOnBatteryInternal) {
                updateDischargeScreenLevelsLocked(oldState, state3);
            }
        }
    }

    public void noteScreenBrightnessLocked(int brightness) {
        int bin = brightness / 51;
        if (bin < 0) {
            bin = 0;
        } else if (bin >= 5) {
            bin = 4;
        }
        if (this.mScreenBrightnessBin != bin) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states = (this.mHistoryCur.states & -8) | (bin << 0);
            addHistoryRecordLocked(elapsedRealtime, uptime);
            if (this.mScreenState == 2) {
                if (this.mScreenBrightnessBin >= 0) {
                    this.mScreenBrightnessTimer[this.mScreenBrightnessBin].stopRunningLocked(elapsedRealtime);
                }
                this.mScreenBrightnessTimer[bin].startRunningLocked(elapsedRealtime);
            }
            this.mScreenBrightnessBin = bin;
        }
    }

    public void noteUserActivityLocked(int uid, int event) {
        if (this.mOnBatteryInternal) {
            getUidStatsLocked(mapUid(uid)).noteUserActivityLocked(event);
        }
    }

    public void noteWakeUpLocked(String reason, int reasonUid) {
        addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 18, reason, reasonUid);
    }

    public void noteInteractiveLocked(boolean interactive) {
        if (this.mInteractive != interactive) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            this.mInteractive = interactive;
            if (interactive) {
                this.mInteractiveTimer.startRunningLocked(elapsedRealtime);
            } else {
                this.mInteractiveTimer.stopRunningLocked(elapsedRealtime);
            }
        }
    }

    public void noteConnectivityChangedLocked(int type, String extra) {
        addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 9, extra, type);
        this.mNumConnectivityChange++;
    }

    private void noteMobileRadioApWakeupLocked(long elapsedRealtimeMillis, long uptimeMillis, int uid) {
        int uid2 = mapUid(uid);
        addHistoryEventLocked(elapsedRealtimeMillis, uptimeMillis, 19, "", uid2);
        getUidStatsLocked(uid2).noteMobileRadioApWakeupLocked();
    }

    public boolean noteMobileRadioPowerStateLocked(int powerState, long timestampNs, int uid) {
        long realElapsedRealtimeMs;
        int i = powerState;
        int i2 = uid;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mMobileRadioPowerState != i) {
            boolean active = i == 2 || i == 3;
            if (active) {
                if (i2 > 0) {
                    noteMobileRadioApWakeupLocked(elapsedRealtime, uptime, i2);
                }
                long j = timestampNs / 1000000;
                realElapsedRealtimeMs = j;
                this.mMobileRadioActiveStartTime = j;
                this.mHistoryCur.states |= 33554432;
            } else {
                long realElapsedRealtimeMs2 = timestampNs / 1000000;
                long lastUpdateTimeMs = this.mMobileRadioActiveStartTime;
                if (realElapsedRealtimeMs2 < lastUpdateTimeMs) {
                    Slog.wtf(TAG, "Data connection inactive timestamp " + realElapsedRealtimeMs2 + " is before start time " + lastUpdateTimeMs);
                    realElapsedRealtimeMs2 = elapsedRealtime;
                    long j2 = lastUpdateTimeMs;
                } else if (realElapsedRealtimeMs2 < elapsedRealtime) {
                    long j3 = lastUpdateTimeMs;
                    this.mMobileRadioActiveAdjustedTime.addCountLocked(elapsedRealtime - realElapsedRealtimeMs2);
                }
                realElapsedRealtimeMs = realElapsedRealtimeMs2;
                this.mHistoryCur.states &= -33554433;
            }
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mMobileRadioPowerState = i;
            StatsLog.write_non_chained(12, i2, null, i);
            if (active) {
                this.mMobileRadioActiveTimer.startRunningLocked(elapsedRealtime);
                this.mMobileRadioActivePerAppTimer.startRunningLocked(elapsedRealtime);
                LogPower.push(191, Integer.toString(powerState), Long.toString(timestampNs));
            } else {
                this.mMobileRadioActiveTimer.stopRunningLocked(realElapsedRealtimeMs);
                this.mMobileRadioActivePerAppTimer.stopRunningLocked(realElapsedRealtimeMs);
                return true;
            }
        }
        return false;
    }

    public void notePowerSaveModeLocked(boolean enabled) {
        if (this.mPowerSaveModeEnabled != enabled) {
            int i = 0;
            int stepState = enabled ? 4 : 0;
            this.mModStepMode = ((4 & this.mCurStepMode) ^ stepState) | this.mModStepMode;
            this.mCurStepMode = (this.mCurStepMode & -5) | stepState;
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mPowerSaveModeEnabled = enabled;
            if (enabled) {
                this.mHistoryCur.states2 |= Integer.MIN_VALUE;
                this.mPowerSaveModeEnabledTimer.startRunningLocked(elapsedRealtime);
            } else {
                this.mHistoryCur.states2 &= Integer.MAX_VALUE;
                this.mPowerSaveModeEnabledTimer.stopRunningLocked(elapsedRealtime);
            }
            addHistoryRecordLocked(elapsedRealtime, uptime);
            if (enabled) {
                i = 1;
            }
            StatsLog.write(20, i);
        }
    }

    public void noteDeviceIdleModeLocked(int mode, String activeReason, int activeUid) {
        boolean nowIdling;
        boolean nowLightIdling;
        int i;
        int statsmode;
        int i2 = mode;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int i3 = 0;
        boolean nowIdling2 = i2 == 2;
        if (this.mDeviceIdling && !nowIdling2 && activeReason == null) {
            nowIdling2 = true;
        }
        boolean nowIdling3 = nowIdling2;
        boolean nowLightIdling2 = i2 == 1;
        if (this.mDeviceLightIdling && !nowLightIdling2 && !nowIdling3 && activeReason == null) {
            nowLightIdling2 = true;
        }
        boolean nowLightIdling3 = nowLightIdling2;
        if (activeReason == null) {
            nowLightIdling = nowLightIdling3;
            nowIdling = nowIdling3;
            i = 1;
        } else if (this.mDeviceIdling || this.mDeviceLightIdling) {
            nowLightIdling = nowLightIdling3;
            nowIdling = nowIdling3;
            i = 1;
            addHistoryEventLocked(elapsedRealtime, uptime, 10, activeReason, activeUid);
        } else {
            nowLightIdling = nowLightIdling3;
            nowIdling = nowIdling3;
            i = 1;
        }
        boolean nowIdling4 = nowIdling;
        if (!(this.mDeviceIdling == nowIdling4 && this.mDeviceLightIdling == nowLightIdling)) {
            if (nowIdling4) {
                statsmode = 2;
            } else if (nowLightIdling) {
                statsmode = 1;
            } else {
                statsmode = 0;
            }
            StatsLog.write(22, statsmode);
        }
        if (this.mDeviceIdling != nowIdling4) {
            this.mDeviceIdling = nowIdling4;
            if (nowIdling4) {
                i3 = 8;
            }
            int stepState = i3;
            this.mModStepMode = ((8 & this.mCurStepMode) ^ stepState) | this.mModStepMode;
            this.mCurStepMode = (this.mCurStepMode & -9) | stepState;
            if (nowIdling4) {
                this.mDeviceIdlingTimer.startRunningLocked(elapsedRealtime);
            } else {
                this.mDeviceIdlingTimer.stopRunningLocked(elapsedRealtime);
            }
        }
        if (this.mDeviceLightIdling != nowLightIdling) {
            this.mDeviceLightIdling = nowLightIdling;
            if (nowLightIdling) {
                this.mDeviceLightIdlingTimer.startRunningLocked(elapsedRealtime);
            } else {
                this.mDeviceLightIdlingTimer.stopRunningLocked(elapsedRealtime);
            }
        }
        if (this.mDeviceIdleMode != i2) {
            this.mHistoryCur.states2 = (this.mHistoryCur.states2 & -100663297) | (i2 << 25);
            addHistoryRecordLocked(elapsedRealtime, uptime);
            long lastDuration = elapsedRealtime - this.mLastIdleTimeStart;
            this.mLastIdleTimeStart = elapsedRealtime;
            if (this.mDeviceIdleMode == i) {
                if (lastDuration > this.mLongestLightIdleTime) {
                    this.mLongestLightIdleTime = lastDuration;
                }
                this.mDeviceIdleModeLightTimer.stopRunningLocked(elapsedRealtime);
            } else if (this.mDeviceIdleMode == 2) {
                if (lastDuration > this.mLongestFullIdleTime) {
                    this.mLongestFullIdleTime = lastDuration;
                }
                this.mDeviceIdleModeFullTimer.stopRunningLocked(elapsedRealtime);
            }
            if (i2 == i) {
                this.mDeviceIdleModeLightTimer.startRunningLocked(elapsedRealtime);
            } else if (i2 == 2) {
                this.mDeviceIdleModeFullTimer.startRunningLocked(elapsedRealtime);
            }
            this.mDeviceIdleMode = i2;
            StatsLog.write(21, i2);
        }
    }

    public void notePackageInstalledLocked(String pkgName, long versionCode) {
        long j = versionCode;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        addHistoryEventLocked(elapsedRealtime, this.mClocks.uptimeMillis(), 11, pkgName, (int) j);
        BatteryStats.PackageChange pc = new BatteryStats.PackageChange();
        pc.mPackageName = pkgName;
        pc.mUpdate = true;
        pc.mVersionCode = j;
        addPackageChange(pc);
    }

    public void notePackageUninstalledLocked(String pkgName) {
        addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 12, pkgName, 0);
        BatteryStats.PackageChange pc = new BatteryStats.PackageChange();
        pc.mPackageName = pkgName;
        pc.mUpdate = true;
        addPackageChange(pc);
    }

    private void addPackageChange(BatteryStats.PackageChange pc) {
        if (this.mDailyPackageChanges == null) {
            this.mDailyPackageChanges = new ArrayList<>();
        }
        this.mDailyPackageChanges.add(pc);
    }

    /* access modifiers changed from: package-private */
    public void stopAllGpsSignalQualityTimersLocked(int except) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        for (int i = 0; i < 2; i++) {
            if (i != except) {
                while (this.mGpsSignalQualityTimer[i].isRunningLocked()) {
                    this.mGpsSignalQualityTimer[i].stopRunningLocked(elapsedRealtime);
                }
            }
        }
    }

    public void notePhoneOnLocked() {
        if (!this.mPhoneOn) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states2 |= DELTA_EVENT_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mPhoneOn = true;
            this.mPhoneOnTimer.startRunningLocked(elapsedRealtime);
        }
    }

    public void notePhoneOffLocked() {
        if (this.mPhoneOn) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states2 &= -8388609;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mPhoneOn = false;
            this.mPhoneOnTimer.stopRunningLocked(elapsedRealtime);
        }
    }

    private void registerUsbStateReceiver(Context context) {
        IntentFilter usbStateFilter = new IntentFilter();
        usbStateFilter.addAction("android.hardware.usb.action.USB_STATE");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean state = intent.getBooleanExtra("connected", false);
                synchronized (BatteryStatsImpl.this) {
                    BatteryStatsImpl.this.noteUsbConnectionStateLocked(state);
                }
            }
        }, usbStateFilter);
        synchronized (this) {
            if (this.mUsbDataState == 0) {
                Intent usbState = context.registerReceiver(null, usbStateFilter);
                boolean initState = false;
                if (usbState != null && usbState.getBooleanExtra("connected", false)) {
                    initState = true;
                }
                noteUsbConnectionStateLocked(initState);
            }
        }
    }

    /* access modifiers changed from: private */
    public void noteUsbConnectionStateLocked(boolean connected) {
        int newState = connected ? 2 : 1;
        if (this.mUsbDataState != newState) {
            this.mUsbDataState = newState;
            if (connected) {
                this.mHistoryCur.states2 |= 262144;
            } else {
                this.mHistoryCur.states2 &= -262145;
            }
            addHistoryRecordLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis());
        }
    }

    /* access modifiers changed from: package-private */
    public void stopAllPhoneSignalStrengthTimersLocked(int except) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        for (int i = 0; i < 5; i++) {
            if (i != except) {
                while (this.mPhoneSignalStrengthsTimer[i].isRunningLocked()) {
                    this.mPhoneSignalStrengthsTimer[i].stopRunningLocked(elapsedRealtime);
                }
            }
        }
    }

    private int fixPhoneServiceState(int state, int signalBin) {
        if (this.mPhoneSimStateRaw == 1 && state == 1 && signalBin > 0) {
            return 0;
        }
        return state;
    }

    private void updateAllPhoneStateLocked(int state, int simState, int strengthBin) {
        boolean scanning = false;
        boolean newHistory = false;
        this.mPhoneServiceStateRaw = state;
        this.mPhoneSimStateRaw = simState;
        this.mPhoneSignalStrengthBinRaw = strengthBin;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (simState == 1 && state == 1 && strengthBin > 0) {
            state = 0;
        }
        if (state == 3) {
            strengthBin = -1;
        } else if (state != 0 && state == 1) {
            scanning = true;
            strengthBin = 0;
            if (!this.mPhoneSignalScanningTimer.isRunningLocked()) {
                this.mHistoryCur.states |= DELTA_STATE2_FLAG;
                newHistory = true;
                this.mPhoneSignalScanningTimer.startRunningLocked(elapsedRealtime);
            }
        }
        if (!scanning && this.mPhoneSignalScanningTimer.isRunningLocked()) {
            this.mHistoryCur.states &= -2097153;
            newHistory = true;
            this.mPhoneSignalScanningTimer.stopRunningLocked(elapsedRealtime);
        }
        if (this.mPhoneServiceState != state) {
            this.mHistoryCur.states = (this.mHistoryCur.states & -449) | (state << 6);
            newHistory = true;
            this.mPhoneServiceState = state;
        }
        if (this.mPhoneSignalStrengthBin != strengthBin) {
            if (this.mPhoneSignalStrengthBin >= 0) {
                this.mPhoneSignalStrengthsTimer[this.mPhoneSignalStrengthBin].stopRunningLocked(elapsedRealtime);
            }
            if (strengthBin >= 0) {
                if (!this.mPhoneSignalStrengthsTimer[strengthBin].isRunningLocked()) {
                    this.mPhoneSignalStrengthsTimer[strengthBin].startRunningLocked(elapsedRealtime);
                }
                this.mHistoryCur.states = (this.mHistoryCur.states & -57) | (strengthBin << 3);
                newHistory = true;
                StatsLog.write(40, strengthBin);
            } else {
                stopAllPhoneSignalStrengthTimersLocked(-1);
            }
            this.mPhoneSignalStrengthBin = strengthBin;
        }
        if (newHistory) {
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
    }

    public void notePhoneStateLocked(int state, int simState) {
        updateAllPhoneStateLocked(state, simState, this.mPhoneSignalStrengthBinRaw);
    }

    public void notePhoneSignalStrengthLocked(SignalStrength signalStrength) {
        int bin = signalStrength.getLevel();
        if (bin >= this.mPhoneSignalStrengthsTimer.length) {
            bin = this.mPhoneSignalStrengthsTimer.length - 1;
        }
        updateAllPhoneStateLocked(this.mPhoneServiceStateRaw, this.mPhoneSimStateRaw, bin);
    }

    public void notePhoneDataConnectionStateLocked(int dataType, boolean hasData) {
        int bin = 0;
        if (hasData) {
            if (dataType <= 0 || dataType > 19) {
                bin = 20;
            } else {
                bin = dataType;
            }
        }
        if (this.mPhoneDataConnectionType != bin) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states = (this.mHistoryCur.states & -15873) | (bin << 9);
            addHistoryRecordLocked(elapsedRealtime, uptime);
            if (this.mPhoneDataConnectionType >= 0) {
                this.mPhoneDataConnectionsTimer[this.mPhoneDataConnectionType].stopRunningLocked(elapsedRealtime);
            }
            this.mPhoneDataConnectionType = bin;
            this.mPhoneDataConnectionsTimer[bin].startRunningLocked(elapsedRealtime);
        }
    }

    public void noteWifiOnLocked() {
        if (!this.mWifiOn) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states2 |= 268435456;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mWifiOn = true;
            this.mWifiOnTimer.startRunningLocked(elapsedRealtime);
            scheduleSyncExternalStatsLocked("wifi-off", 2);
        }
    }

    public void noteWifiOffLocked() {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiOn) {
            this.mHistoryCur.states2 &= -268435457;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mWifiOn = false;
            this.mWifiOnTimer.stopRunningLocked(elapsedRealtime);
            scheduleSyncExternalStatsLocked("wifi-on", 2);
        }
    }

    public void noteAudioOnLocked(int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mAudioOnNesting == 0) {
            this.mHistoryCur.states |= DELTA_WAKELOCK_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mAudioOnTimer.startRunningLocked(elapsedRealtime);
        }
        this.mAudioOnNesting++;
        getUidStatsLocked(uid2).noteAudioTurnedOnLocked(elapsedRealtime);
    }

    public void noteAudioOffLocked(int uid) {
        if (this.mAudioOnNesting != 0) {
            int uid2 = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mAudioOnNesting - 1;
            this.mAudioOnNesting = i;
            if (i == 0) {
                this.mHistoryCur.states &= -4194305;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mAudioOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid2).noteAudioTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteVideoOnLocked(int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mVideoOnNesting == 0) {
            this.mHistoryCur.states2 |= 1073741824;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mVideoOnTimer.startRunningLocked(elapsedRealtime);
        }
        this.mVideoOnNesting++;
        getUidStatsLocked(uid2).noteVideoTurnedOnLocked(elapsedRealtime);
    }

    public void noteVideoOffLocked(int uid) {
        if (this.mVideoOnNesting != 0) {
            int uid2 = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mVideoOnNesting - 1;
            this.mVideoOnNesting = i;
            if (i == 0) {
                this.mHistoryCur.states2 &= -1073741825;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mVideoOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid2).noteVideoTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteResetAudioLocked() {
        if (this.mAudioOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mAudioOnNesting = 0;
            this.mHistoryCur.states &= -4194305;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mAudioOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                this.mUidStats.valueAt(i).noteResetAudioLocked(elapsedRealtime);
            }
        }
    }

    public void noteResetVideoLocked() {
        if (this.mVideoOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mAudioOnNesting = 0;
            this.mHistoryCur.states2 &= -1073741825;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mVideoOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                this.mUidStats.valueAt(i).noteResetVideoLocked(elapsedRealtime);
            }
        }
    }

    public void noteActivityResumedLocked(int uid) {
        getUidStatsLocked(mapUid(uid)).noteActivityResumedLocked(this.mClocks.elapsedRealtime());
    }

    public void noteActivityPausedLocked(int uid) {
        getUidStatsLocked(mapUid(uid)).noteActivityPausedLocked(this.mClocks.elapsedRealtime());
    }

    public void noteVibratorOnLocked(int uid, long durationMillis) {
        getUidStatsLocked(mapUid(uid)).noteVibratorOnLocked(durationMillis);
    }

    public void noteVibratorOffLocked(int uid) {
        getUidStatsLocked(mapUid(uid)).noteVibratorOffLocked();
    }

    public void noteFlashlightOnLocked(int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int i = this.mFlashlightOnNesting;
        this.mFlashlightOnNesting = i + 1;
        if (i == 0) {
            this.mHistoryCur.states2 |= 134217728;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mFlashlightOnTimer.startRunningLocked(elapsedRealtime);
        }
        getUidStatsLocked(uid2).noteFlashlightTurnedOnLocked(elapsedRealtime);
    }

    public void noteFlashlightOffLocked(int uid) {
        if (this.mFlashlightOnNesting != 0) {
            int uid2 = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mFlashlightOnNesting - 1;
            this.mFlashlightOnNesting = i;
            if (i == 0) {
                this.mHistoryCur.states2 &= -134217729;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mFlashlightOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid2).noteFlashlightTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteCameraOnLocked(int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int i = this.mCameraOnNesting;
        this.mCameraOnNesting = i + 1;
        if (i == 0) {
            this.mHistoryCur.states2 |= DELTA_STATE2_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mCameraOnTimer.startRunningLocked(elapsedRealtime);
        }
        getUidStatsLocked(uid2).noteCameraTurnedOnLocked(elapsedRealtime);
    }

    public void noteCameraOffLocked(int uid) {
        if (this.mCameraOnNesting != 0) {
            int uid2 = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mCameraOnNesting - 1;
            this.mCameraOnNesting = i;
            if (i == 0) {
                this.mHistoryCur.states2 &= -2097153;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mCameraOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid2).noteCameraTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteResetCameraLocked() {
        if (this.mCameraOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mCameraOnNesting = 0;
            this.mHistoryCur.states2 &= -2097153;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mCameraOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                this.mUidStats.valueAt(i).noteResetCameraLocked(elapsedRealtime);
            }
        }
    }

    public void noteResetFlashlightLocked() {
        if (this.mFlashlightOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mFlashlightOnNesting = 0;
            this.mHistoryCur.states2 &= -134217729;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mFlashlightOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                this.mUidStats.valueAt(i).noteResetFlashlightLocked(elapsedRealtime);
            }
        }
    }

    private void noteBluetoothScanStartedLocked(WorkSource.WorkChain workChain, int uid, boolean isUnoptimized) {
        int uid2 = getAttributionUid(uid, workChain);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mBluetoothScanNesting == 0) {
            this.mHistoryCur.states2 |= DELTA_STATE_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.startRunningLocked(elapsedRealtime);
        }
        this.mBluetoothScanNesting++;
        getUidStatsLocked(uid2).noteBluetoothScanStartedLocked(elapsedRealtime, isUnoptimized);
    }

    public void noteBluetoothScanStartedFromSourceLocked(WorkSource ws, boolean isUnoptimized) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteBluetoothScanStartedLocked(null, ws.get(i), isUnoptimized);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                noteBluetoothScanStartedLocked(workChains.get(i2), -1, isUnoptimized);
            }
        }
    }

    private void noteBluetoothScanStoppedLocked(WorkSource.WorkChain workChain, int uid, boolean isUnoptimized) {
        int uid2 = getAttributionUid(uid, workChain);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mBluetoothScanNesting--;
        if (this.mBluetoothScanNesting == 0) {
            this.mHistoryCur.states2 &= -1048577;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.stopRunningLocked(elapsedRealtime);
        }
        getUidStatsLocked(uid2).noteBluetoothScanStoppedLocked(elapsedRealtime, isUnoptimized);
    }

    private int getAttributionUid(int uid, WorkSource.WorkChain workChain) {
        if (workChain != null) {
            return mapUid(workChain.getAttributionUid());
        }
        return mapUid(uid);
    }

    public void noteBluetoothScanStoppedFromSourceLocked(WorkSource ws, boolean isUnoptimized) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteBluetoothScanStoppedLocked(null, ws.get(i), isUnoptimized);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                noteBluetoothScanStoppedLocked(workChains.get(i2), -1, isUnoptimized);
            }
        }
    }

    public void noteResetBluetoothScanLocked() {
        if (this.mBluetoothScanNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mBluetoothScanNesting = 0;
            this.mHistoryCur.states2 &= -1048577;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                this.mUidStats.valueAt(i).noteResetBluetoothScanLocked(elapsedRealtime);
            }
        }
    }

    public void noteBluetoothScanResultsFromSourceLocked(WorkSource ws, int numNewResults) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            getUidStatsLocked(mapUid(ws.get(i))).noteBluetoothScanResultsLocked(numNewResults);
            StatsLog.write_non_chained(4, ws.get(i), ws.getName(i), numNewResults);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                WorkSource.WorkChain wc = workChains.get(i2);
                getUidStatsLocked(mapUid(wc.getAttributionUid())).noteBluetoothScanResultsLocked(numNewResults);
                StatsLog.write(4, wc.getUids(), wc.getTags(), numNewResults);
            }
        }
    }

    private void noteWifiRadioApWakeupLocked(long elapsedRealtimeMillis, long uptimeMillis, int uid) {
        int uid2 = mapUid(uid);
        addHistoryEventLocked(elapsedRealtimeMillis, uptimeMillis, 19, "", uid2);
        getUidStatsLocked(uid2).noteWifiRadioApWakeupLocked();
    }

    public void noteWifiRadioPowerState(int powerState, long timestampNs, int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiRadioPowerState != powerState) {
            if (powerState == 2 || powerState == 3) {
                if (uid > 0) {
                    noteWifiRadioApWakeupLocked(elapsedRealtime, uptime, uid);
                }
                this.mHistoryCur.states |= 67108864;
                this.mWifiActiveTimer.startRunningLocked(elapsedRealtime);
            } else {
                this.mHistoryCur.states &= -67108865;
                this.mWifiActiveTimer.stopRunningLocked(timestampNs / 1000000);
            }
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mWifiRadioPowerState = powerState;
            StatsLog.write_non_chained(13, uid, null, powerState);
        }
    }

    public void noteWifiRunningLocked(WorkSource ws) {
        if (!this.mGlobalWifiRunning) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states2 |= 536870912;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mGlobalWifiRunning = true;
            this.mGlobalWifiRunningTimer.startRunningLocked(elapsedRealtime);
            int N = ws.size();
            for (int i = 0; i < N; i++) {
                getUidStatsLocked(mapUid(ws.get(i))).noteWifiRunningLocked(elapsedRealtime);
            }
            List<WorkSource.WorkChain> workChains = ws.getWorkChains();
            if (workChains != null) {
                for (int i2 = 0; i2 < workChains.size(); i2++) {
                    getUidStatsLocked(mapUid(workChains.get(i2).getAttributionUid())).noteWifiRunningLocked(elapsedRealtime);
                }
            }
            scheduleSyncExternalStatsLocked("wifi-running", 2);
            return;
        }
        Log.w(TAG, "noteWifiRunningLocked -- called while WIFI running");
    }

    public void noteWifiRunningChangedLocked(WorkSource oldWs, WorkSource newWs) {
        if (this.mGlobalWifiRunning) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            int N = oldWs.size();
            for (int i = 0; i < N; i++) {
                getUidStatsLocked(mapUid(oldWs.get(i))).noteWifiStoppedLocked(elapsedRealtime);
            }
            List<WorkSource.WorkChain> workChains = oldWs.getWorkChains();
            if (workChains != null) {
                for (int i2 = 0; i2 < workChains.size(); i2++) {
                    getUidStatsLocked(mapUid(workChains.get(i2).getAttributionUid())).noteWifiStoppedLocked(elapsedRealtime);
                }
            }
            int N2 = newWs.size();
            for (int i3 = 0; i3 < N2; i3++) {
                getUidStatsLocked(mapUid(newWs.get(i3))).noteWifiRunningLocked(elapsedRealtime);
            }
            List<WorkSource.WorkChain> workChains2 = newWs.getWorkChains();
            if (workChains2 != null) {
                for (int i4 = 0; i4 < workChains2.size(); i4++) {
                    getUidStatsLocked(mapUid(workChains2.get(i4).getAttributionUid())).noteWifiRunningLocked(elapsedRealtime);
                }
                return;
            }
            return;
        }
        Log.w(TAG, "noteWifiRunningChangedLocked -- called while WIFI not running");
    }

    public void noteWifiStoppedLocked(WorkSource ws) {
        if (this.mGlobalWifiRunning) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states2 &= -536870913;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mGlobalWifiRunning = false;
            this.mGlobalWifiRunningTimer.stopRunningLocked(elapsedRealtime);
            int N = ws.size();
            for (int i = 0; i < N; i++) {
                getUidStatsLocked(mapUid(ws.get(i))).noteWifiStoppedLocked(elapsedRealtime);
            }
            List<WorkSource.WorkChain> workChains = ws.getWorkChains();
            if (workChains != null) {
                for (int i2 = 0; i2 < workChains.size(); i2++) {
                    getUidStatsLocked(mapUid(workChains.get(i2).getAttributionUid())).noteWifiStoppedLocked(elapsedRealtime);
                }
            }
            scheduleSyncExternalStatsLocked("wifi-stopped", 2);
            return;
        }
        Log.w(TAG, "noteWifiStoppedLocked -- called while WIFI not running");
    }

    public void noteWifiStateLocked(int wifiState, String accessPoint) {
        if (this.mWifiState != wifiState) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            if (this.mWifiState >= 0) {
                this.mWifiStateTimer[this.mWifiState].stopRunningLocked(elapsedRealtime);
            }
            this.mWifiState = wifiState;
            this.mWifiStateTimer[wifiState].startRunningLocked(elapsedRealtime);
            scheduleSyncExternalStatsLocked("wifi-state", 2);
        }
    }

    public void noteWifiSupplicantStateChangedLocked(int supplState, boolean failedAuth) {
        if (this.mWifiSupplState != supplState) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            if (this.mWifiSupplState >= 0) {
                this.mWifiSupplStateTimer[this.mWifiSupplState].stopRunningLocked(elapsedRealtime);
            }
            this.mWifiSupplState = supplState;
            this.mWifiSupplStateTimer[supplState].startRunningLocked(elapsedRealtime);
            this.mHistoryCur.states2 = (this.mHistoryCur.states2 & -16) | (supplState << 0);
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopAllWifiSignalStrengthTimersLocked(int except) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        for (int i = 0; i < 5; i++) {
            if (i != except) {
                while (this.mWifiSignalStrengthsTimer[i].isRunningLocked()) {
                    this.mWifiSignalStrengthsTimer[i].stopRunningLocked(elapsedRealtime);
                }
            }
        }
    }

    public void noteWifiRssiChangedLocked(int newRssi) {
        int strengthBin = WifiManager.calculateSignalLevel(newRssi, 5);
        if (this.mWifiSignalStrengthBin != strengthBin) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            if (this.mWifiSignalStrengthBin >= 0) {
                this.mWifiSignalStrengthsTimer[this.mWifiSignalStrengthBin].stopRunningLocked(elapsedRealtime);
            }
            if (strengthBin >= 0) {
                if (!this.mWifiSignalStrengthsTimer[strengthBin].isRunningLocked()) {
                    this.mWifiSignalStrengthsTimer[strengthBin].startRunningLocked(elapsedRealtime);
                }
                this.mHistoryCur.states2 = (this.mHistoryCur.states2 & -113) | (strengthBin << 4);
                addHistoryRecordLocked(elapsedRealtime, uptime);
            } else {
                stopAllWifiSignalStrengthTimersLocked(-1);
            }
            StatsLog.write(38, strengthBin);
            this.mWifiSignalStrengthBin = strengthBin;
        }
    }

    public void noteFullWifiLockAcquiredLocked(int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiFullLockNesting == 0) {
            this.mHistoryCur.states |= 268435456;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiFullLockNesting++;
        getUidStatsLocked(uid).noteFullWifiLockAcquiredLocked(elapsedRealtime);
    }

    public void noteFullWifiLockReleasedLocked(int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mWifiFullLockNesting--;
        if (this.mWifiFullLockNesting == 0) {
            this.mHistoryCur.states &= -268435457;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid).noteFullWifiLockReleasedLocked(elapsedRealtime);
    }

    public void noteWifiScanStartedLocked(int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiScanNesting == 0) {
            this.mHistoryCur.states |= 134217728;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiScanNesting++;
        getUidStatsLocked(uid).noteWifiScanStartedLocked(elapsedRealtime);
        LogPower.push(158, Integer.toString(uid));
    }

    public void noteWifiScanStoppedLocked(int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mWifiScanNesting--;
        if (this.mWifiScanNesting == 0) {
            this.mHistoryCur.states &= -134217729;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid).noteWifiScanStoppedLocked(elapsedRealtime);
        LogPower.push(159, Integer.toString(uid));
    }

    public void noteWifiBatchedScanStartedLocked(int uid, int csph) {
        int uid2 = mapUid(uid);
        getUidStatsLocked(uid2).noteWifiBatchedScanStartedLocked(csph, this.mClocks.elapsedRealtime());
    }

    public void noteWifiBatchedScanStoppedLocked(int uid) {
        int uid2 = mapUid(uid);
        getUidStatsLocked(uid2).noteWifiBatchedScanStoppedLocked(this.mClocks.elapsedRealtime());
    }

    public void noteWifiMulticastEnabledLocked(int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiMulticastNesting == 0) {
            this.mHistoryCur.states |= Protocol.BASE_SYSTEM_RESERVED;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            if (!this.mWifiMulticastWakelockTimer.isRunningLocked()) {
                this.mWifiMulticastWakelockTimer.startRunningLocked(elapsedRealtime);
            }
        }
        this.mWifiMulticastNesting++;
        getUidStatsLocked(uid2).noteWifiMulticastEnabledLocked(elapsedRealtime);
    }

    public void noteWifiMulticastDisabledLocked(int uid) {
        int uid2 = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mWifiMulticastNesting--;
        if (this.mWifiMulticastNesting == 0) {
            this.mHistoryCur.states &= -65537;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            if (this.mWifiMulticastWakelockTimer.isRunningLocked()) {
                this.mWifiMulticastWakelockTimer.stopRunningLocked(elapsedRealtime);
            }
        }
        getUidStatsLocked(uid2).noteWifiMulticastDisabledLocked(elapsedRealtime);
    }

    public void noteFullWifiLockAcquiredFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteFullWifiLockAcquiredLocked(mapUid(ws.get(i)));
            StatsLog.write_non_chained(37, ws.get(i), ws.getName(i), 1);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                WorkSource.WorkChain workChain = workChains.get(i2);
                noteFullWifiLockAcquiredLocked(mapUid(workChain.getAttributionUid()));
                StatsLog.write(37, workChain.getUids(), workChain.getTags(), 1);
            }
        }
    }

    public void noteFullWifiLockReleasedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteFullWifiLockReleasedLocked(mapUid(ws.get(i)));
            StatsLog.write_non_chained(37, ws.get(i), ws.getName(i), 0);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                WorkSource.WorkChain workChain = workChains.get(i2);
                noteFullWifiLockReleasedLocked(mapUid(workChain.getAttributionUid()));
                StatsLog.write(37, workChain.getUids(), workChain.getTags(), 0);
            }
        }
    }

    public void noteWifiScanStartedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiScanStartedLocked(mapUid(ws.get(i)));
            StatsLog.write_non_chained(39, ws.get(i), ws.getName(i), 1);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                WorkSource.WorkChain workChain = workChains.get(i2);
                noteWifiScanStartedLocked(mapUid(workChain.getAttributionUid()));
                StatsLog.write(39, workChain.getUids(), workChain.getTags(), 1);
            }
        }
    }

    public void noteWifiScanStoppedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiScanStoppedLocked(mapUid(ws.get(i)));
            StatsLog.write_non_chained(39, ws.get(i), ws.getName(i), 0);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                WorkSource.WorkChain workChain = workChains.get(i2);
                noteWifiScanStoppedLocked(mapUid(workChain.getAttributionUid()));
                StatsLog.write(39, workChain.getUids(), workChain.getTags(), 0);
            }
        }
    }

    public void noteWifiBatchedScanStartedFromSourceLocked(WorkSource ws, int csph) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiBatchedScanStartedLocked(ws.get(i), csph);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                noteWifiBatchedScanStartedLocked(workChains.get(i2).getAttributionUid(), csph);
            }
        }
    }

    public void noteWifiBatchedScanStoppedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiBatchedScanStoppedLocked(ws.get(i));
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                noteWifiBatchedScanStoppedLocked(workChains.get(i2).getAttributionUid());
            }
        }
    }

    private static String[] includeInStringArray(String[] array, String str) {
        if (ArrayUtils.indexOf(array, str) >= 0) {
            return array;
        }
        String[] newArray = new String[(array.length + 1)];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = str;
        return newArray;
    }

    private static String[] excludeFromStringArray(String[] array, String str) {
        int index = ArrayUtils.indexOf(array, str);
        if (index < 0) {
            return array;
        }
        String[] newArray = new String[(array.length - 1)];
        if (index > 0) {
            System.arraycopy(array, 0, newArray, 0, index);
        }
        if (index < array.length - 1) {
            System.arraycopy(array, index + 1, newArray, index, (array.length - index) - 1);
        }
        return newArray;
    }

    public void noteNetworkInterfaceTypeLocked(String iface, int networkType) {
        if (!TextUtils.isEmpty(iface)) {
            synchronized (this.mModemNetworkLock) {
                if (ConnectivityManager.isNetworkTypeMobile(networkType)) {
                    this.mModemIfaces = includeInStringArray(this.mModemIfaces, iface);
                } else {
                    this.mModemIfaces = excludeFromStringArray(this.mModemIfaces, iface);
                }
            }
            synchronized (this.mWifiNetworkLock) {
                if (ConnectivityManager.isNetworkTypeWifi(networkType)) {
                    this.mWifiIfaces = includeInStringArray(this.mWifiIfaces, iface);
                } else {
                    this.mWifiIfaces = excludeFromStringArray(this.mWifiIfaces, iface);
                }
            }
        }
    }

    public String[] getWifiIfaces() {
        String[] strArr;
        synchronized (this.mWifiNetworkLock) {
            strArr = this.mWifiIfaces;
        }
        return strArr;
    }

    public String[] getMobileIfaces() {
        String[] strArr;
        synchronized (this.mModemNetworkLock) {
            strArr = this.mModemIfaces;
        }
        return strArr;
    }

    public long getScreenOnTime(long elapsedRealtimeUs, int which) {
        if (this.mScreenOnTimer != null) {
            return this.mScreenOnTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
    }

    public int getScreenOnCount(int which) {
        if (this.mScreenOnTimer != null) {
            return this.mScreenOnTimer.getCountLocked(which);
        }
        return 0;
    }

    public long getScreenDozeTime(long elapsedRealtimeUs, int which) {
        return this.mScreenDozeTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getScreenDozeCount(int which) {
        return this.mScreenDozeTimer.getCountLocked(which);
    }

    public long getScreenBrightnessTime(int brightnessBin, long elapsedRealtimeUs, int which) {
        if (this.mScreenBrightnessTimer[brightnessBin] != null) {
            return this.mScreenBrightnessTimer[brightnessBin].getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
    }

    public Timer getScreenBrightnessTimer(int brightnessBin) {
        return this.mScreenBrightnessTimer[brightnessBin];
    }

    public long getInteractiveTime(long elapsedRealtimeUs, int which) {
        if (this.mInteractiveTimer != null) {
            return this.mInteractiveTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
    }

    public long getPowerSaveModeEnabledTime(long elapsedRealtimeUs, int which) {
        if (this.mPowerSaveModeEnabledTimer != null) {
            return this.mPowerSaveModeEnabledTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
    }

    public int getPowerSaveModeEnabledCount(int which) {
        if (this.mPowerSaveModeEnabledTimer != null) {
            return this.mPowerSaveModeEnabledTimer.getCountLocked(which);
        }
        return 0;
    }

    public long getDeviceIdleModeTime(int mode, long elapsedRealtimeUs, int which) {
        switch (mode) {
            case 1:
                return this.mDeviceIdleModeLightTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            case 2:
                return this.mDeviceIdleModeFullTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            default:
                return 0;
        }
    }

    public int getDeviceIdleModeCount(int mode, int which) {
        switch (mode) {
            case 1:
                return this.mDeviceIdleModeLightTimer.getCountLocked(which);
            case 2:
                return this.mDeviceIdleModeFullTimer.getCountLocked(which);
            default:
                return 0;
        }
    }

    public long getLongestDeviceIdleModeTime(int mode) {
        switch (mode) {
            case 1:
                return this.mLongestLightIdleTime;
            case 2:
                return this.mLongestFullIdleTime;
            default:
                return 0;
        }
    }

    public long getDeviceIdlingTime(int mode, long elapsedRealtimeUs, int which) {
        switch (mode) {
            case 1:
                return this.mDeviceLightIdlingTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            case 2:
                return this.mDeviceIdlingTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            default:
                return 0;
        }
    }

    public int getDeviceIdlingCount(int mode, int which) {
        switch (mode) {
            case 1:
                return this.mDeviceLightIdlingTimer.getCountLocked(which);
            case 2:
                return this.mDeviceIdlingTimer.getCountLocked(which);
            default:
                return 0;
        }
    }

    public int getNumConnectivityChange(int which) {
        int val = this.mNumConnectivityChange;
        if (which == 1) {
            return val - this.mLoadedNumConnectivityChange;
        }
        if (which == 2) {
            return val - this.mUnpluggedNumConnectivityChange;
        }
        return val;
    }

    public long getGpsSignalQualityTime(int strengthBin, long elapsedRealtimeUs, int which) {
        if (strengthBin < 0 || strengthBin >= 2) {
            return 0;
        }
        return this.mGpsSignalQualityTimer[strengthBin].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public long getGpsBatteryDrainMaMs() {
        if (this.mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_OPERATING_VOLTAGE) / 1000.0d == 0.0d) {
            return 0;
        }
        long rawRealtime = SystemClock.elapsedRealtime() * 1000;
        int i = 0;
        double energyUsedMaMs = 0.0d;
        int i2 = 0;
        while (i2 < 2) {
            energyUsedMaMs += this.mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_SIGNAL_QUALITY_BASED, i2) * ((double) (getGpsSignalQualityTime(i2, rawRealtime, i) / 1000));
            i2++;
            i = 0;
        }
        return (long) energyUsedMaMs;
    }

    public long getPhoneOnTime(long elapsedRealtimeUs, int which) {
        if (this.mPhoneOnTimer != null) {
            return this.mPhoneOnTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
    }

    public int getPhoneOnCount(int which) {
        if (this.mPhoneOnTimer != null) {
            return this.mPhoneOnTimer.getCountLocked(which);
        }
        return 0;
    }

    public long getPhoneSignalStrengthTime(int strengthBin, long elapsedRealtimeUs, int which) {
        return this.mPhoneSignalStrengthsTimer[strengthBin].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public long getPhoneSignalScanningTime(long elapsedRealtimeUs, int which) {
        return this.mPhoneSignalScanningTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public Timer getPhoneSignalScanningTimer() {
        return this.mPhoneSignalScanningTimer;
    }

    public int getPhoneSignalStrengthCount(int strengthBin, int which) {
        return this.mPhoneSignalStrengthsTimer[strengthBin].getCountLocked(which);
    }

    public Timer getPhoneSignalStrengthTimer(int strengthBin) {
        return this.mPhoneSignalStrengthsTimer[strengthBin];
    }

    public long getPhoneDataConnectionTime(int dataType, long elapsedRealtimeUs, int which) {
        return this.mPhoneDataConnectionsTimer[dataType].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getPhoneDataConnectionCount(int dataType, int which) {
        return this.mPhoneDataConnectionsTimer[dataType].getCountLocked(which);
    }

    public Timer getPhoneDataConnectionTimer(int dataType) {
        return this.mPhoneDataConnectionsTimer[dataType];
    }

    public long getMobileRadioActiveTime(long elapsedRealtimeUs, int which) {
        return this.mMobileRadioActiveTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getMobileRadioActiveCount(int which) {
        return this.mMobileRadioActiveTimer.getCountLocked(which);
    }

    public long getMobileRadioActiveAdjustedTime(int which) {
        return this.mMobileRadioActiveAdjustedTime.getCountLocked(which);
    }

    public long getMobileRadioActiveUnknownTime(int which) {
        return this.mMobileRadioActiveUnknownTime.getCountLocked(which);
    }

    public int getMobileRadioActiveUnknownCount(int which) {
        return (int) this.mMobileRadioActiveUnknownCount.getCountLocked(which);
    }

    public long getWifiMulticastWakelockTime(long elapsedRealtimeUs, int which) {
        return this.mWifiMulticastWakelockTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getWifiMulticastWakelockCount(int which) {
        return this.mWifiMulticastWakelockTimer.getCountLocked(which);
    }

    public long getWifiOnTime(long elapsedRealtimeUs, int which) {
        return this.mWifiOnTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public long getWifiActiveTime(long elapsedRealtimeUs, int which) {
        return this.mWifiActiveTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public long getGlobalWifiRunningTime(long elapsedRealtimeUs, int which) {
        if (this.mGlobalWifiRunningTimer != null) {
            return this.mGlobalWifiRunningTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
    }

    public long getWifiStateTime(int wifiState, long elapsedRealtimeUs, int which) {
        return this.mWifiStateTimer[wifiState].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getWifiStateCount(int wifiState, int which) {
        return this.mWifiStateTimer[wifiState].getCountLocked(which);
    }

    public Timer getWifiStateTimer(int wifiState) {
        return this.mWifiStateTimer[wifiState];
    }

    public long getWifiSupplStateTime(int state, long elapsedRealtimeUs, int which) {
        return this.mWifiSupplStateTimer[state].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getWifiSupplStateCount(int state, int which) {
        return this.mWifiSupplStateTimer[state].getCountLocked(which);
    }

    public Timer getWifiSupplStateTimer(int state) {
        return this.mWifiSupplStateTimer[state];
    }

    public long getWifiSignalStrengthTime(int strengthBin, long elapsedRealtimeUs, int which) {
        return this.mWifiSignalStrengthsTimer[strengthBin].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getWifiSignalStrengthCount(int strengthBin, int which) {
        return this.mWifiSignalStrengthsTimer[strengthBin].getCountLocked(which);
    }

    public Timer getWifiSignalStrengthTimer(int strengthBin) {
        return this.mWifiSignalStrengthsTimer[strengthBin];
    }

    public BatteryStats.ControllerActivityCounter getBluetoothControllerActivity() {
        return this.mBluetoothActivity;
    }

    public BatteryStats.ControllerActivityCounter getWifiControllerActivity() {
        return this.mWifiActivity;
    }

    public BatteryStats.ControllerActivityCounter getModemControllerActivity() {
        return this.mModemActivity;
    }

    public boolean hasBluetoothActivityReporting() {
        return this.mHasBluetoothReporting;
    }

    public boolean hasWifiActivityReporting() {
        return this.mHasWifiReporting;
    }

    public boolean hasModemActivityReporting() {
        return this.mHasModemReporting;
    }

    public long getFlashlightOnTime(long elapsedRealtimeUs, int which) {
        return this.mFlashlightOnTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public long getFlashlightOnCount(int which) {
        return (long) this.mFlashlightOnTimer.getCountLocked(which);
    }

    public long getCameraOnTime(long elapsedRealtimeUs, int which) {
        return this.mCameraOnTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public long getBluetoothScanTime(long elapsedRealtimeUs, int which) {
        return this.mBluetoothScanTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public long getNetworkActivityBytes(int type, int which) {
        if (type < 0 || type >= this.mNetworkByteActivityCounters.length) {
            return 0;
        }
        return this.mNetworkByteActivityCounters[type].getCountLocked(which);
    }

    public long getNetworkActivityPackets(int type, int which) {
        if (type < 0 || type >= this.mNetworkPacketActivityCounters.length) {
            return 0;
        }
        return this.mNetworkPacketActivityCounters[type].getCountLocked(which);
    }

    public long getStartClockTime() {
        long currentTime = System.currentTimeMillis();
        if (ensureStartClockTime(currentTime)) {
            recordCurrentTimeChangeLocked(currentTime, this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis());
        }
        return this.mStartClockTime;
    }

    public String getStartPlatformVersion() {
        return this.mStartPlatformVersion;
    }

    public String getEndPlatformVersion() {
        return this.mEndPlatformVersion;
    }

    public int getParcelVersion() {
        return 177;
    }

    public boolean getIsOnBattery() {
        return this.mOnBattery;
    }

    public SparseArray<? extends BatteryStats.Uid> getUidStats() {
        return this.mUidStats;
    }

    private static void detachTimerIfNotNull(Timer timer) {
        if (timer != null) {
            timer.detach();
        }
    }

    /* access modifiers changed from: private */
    public static boolean resetTimerIfNotNull(Timer timer, boolean detachIfReset) {
        if (timer != null) {
            return timer.reset(detachIfReset);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean resetTimerIfNotNull(DualTimer timer, boolean detachIfReset) {
        if (timer != null) {
            return timer.reset(detachIfReset);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static void detachLongCounterIfNotNull(LongSamplingCounter counter) {
        if (counter != null) {
            counter.detach();
        }
    }

    /* access modifiers changed from: private */
    public static void resetLongCounterIfNotNull(LongSamplingCounter counter, boolean detachIfReset) {
        if (counter != null) {
            counter.reset(detachIfReset);
        }
    }

    public long[] getCpuFreqs() {
        return this.mCpuFreqs;
    }

    public BatteryStatsImpl(File systemDir, Handler handler, PlatformIdleStateCallback cb, UserInfoProvider userInfoProvider) {
        this(new SystemClocks(), systemDir, handler, cb, userInfoProvider);
    }

    private BatteryStatsImpl(Clocks clocks, File systemDir, Handler handler, PlatformIdleStateCallback cb, UserInfoProvider userInfoProvider) {
        File file = systemDir;
        this.mKernelWakelockReader = new KernelWakelockReader();
        this.mTmpWakelockStats = new KernelWakelockStats();
        this.mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
        this.mKernelUidCpuFreqTimeReader = new KernelUidCpuFreqTimeReader();
        this.mKernelUidCpuActiveTimeReader = new KernelUidCpuActiveTimeReader();
        this.mKernelUidCpuClusterTimeReader = new KernelUidCpuClusterTimeReader();
        this.mKernelMemoryBandwidthStats = new KernelMemoryBandwidthStats();
        this.mKernelMemoryStats = new LongSparseArray<>();
        this.mPerProcStateCpuTimesAvailable = true;
        this.mPendingUids = new SparseIntArray();
        this.mCpuTimeReadsTrackingStartTime = SystemClock.uptimeMillis();
        this.mTmpRpmStats = new RpmStats();
        this.mLastRpmStatsUpdateTimeMs = -1000;
        this.mPendingRemovedUids = new LinkedList();
        this.mExternalSync = null;
        this.mUserInfoProvider = null;
        this.mIsolatedUids = new SparseIntArray();
        this.mUidStats = new SparseArray<>();
        this.mPartialTimers = new ArrayList<>();
        this.mFullTimers = new ArrayList<>();
        this.mWindowTimers = new ArrayList<>();
        this.mDrawTimers = new ArrayList<>();
        this.mSensorTimers = new SparseArray<>();
        this.mWifiRunningTimers = new ArrayList<>();
        this.mFullWifiLockTimers = new ArrayList<>();
        this.mWifiMulticastTimers = new ArrayList<>();
        this.mWifiScanTimers = new ArrayList<>();
        this.mWifiBatchedScanTimers = new SparseArray<>();
        this.mAudioTurnedOnTimers = new ArrayList<>();
        this.mVideoTurnedOnTimers = new ArrayList<>();
        this.mFlashlightTurnedOnTimers = new ArrayList<>();
        this.mCameraTurnedOnTimers = new ArrayList<>();
        this.mBluetoothScanOnTimers = new ArrayList<>();
        this.mLastPartialTimers = new ArrayList<>();
        this.mOnBatteryTimeBase = new TimeBase();
        this.mOnBatteryScreenOffTimeBase = new TimeBase();
        this.mActiveEvents = new BatteryStats.HistoryEventTracker();
        this.mHaveBatteryLevel = false;
        this.mRecordingHistory = false;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new BatteryStats.HistoryItem();
        this.mHistoryLastLastWritten = new BatteryStats.HistoryItem();
        this.mHistoryReadTmp = new BatteryStats.HistoryItem();
        this.mHistoryAddTmp = new BatteryStats.HistoryItem();
        this.mHistoryTagPool = new HashMap<>();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = false;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryCur = new BatteryStats.HistoryItem();
        this.mLastHistoryStepDetails = null;
        this.mLastHistoryStepLevel = 0;
        this.mCurHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mReadHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mTmpHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mScreenState = 0;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mUsbDataState = 0;
        this.mGpsSignalQualityBin = -1;
        this.mGpsSignalQualityTimer = new StopwatchTimer[2];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[21];
        this.mNetworkByteActivityCounters = new LongSamplingCounter[10];
        this.mNetworkPacketActivityCounters = new LongSamplingCounter[10];
        this.mHasWifiReporting = false;
        this.mHasBluetoothReporting = false;
        this.mHasModemReporting = false;
        this.mWifiState = -1;
        this.mWifiStateTimer = new StopwatchTimer[8];
        this.mWifiSupplState = -1;
        this.mWifiSupplStateTimer = new StopwatchTimer[13];
        this.mWifiSignalStrengthBin = -1;
        this.mWifiSignalStrengthsTimer = new StopwatchTimer[5];
        this.mIsCellularTxPowerHigh = false;
        this.mMobileRadioPowerState = 1;
        this.mWifiRadioPowerState = 1;
        this.mCharging = true;
        this.mInitStepMode = 0;
        this.mCurStepMode = 0;
        this.mModStepMode = 0;
        this.mDischargeStepTracker = new BatteryStats.LevelStepTracker(200);
        this.mDailyDischargeStepTracker = new BatteryStats.LevelStepTracker(400);
        this.mChargeStepTracker = new BatteryStats.LevelStepTracker(200);
        this.mDailyChargeStepTracker = new BatteryStats.LevelStepTracker(400);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList<>();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mMinLearnedBatteryCapacity = -1;
        this.mMaxLearnedBatteryCapacity = -1;
        this.mRpmStats = new HashMap<>();
        this.mScreenOffRpmStats = new HashMap<>();
        this.mKernelWakelockStats = new HashMap<>();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap<>();
        this.mChangedStates = 0;
        this.mChangedStates2 = 0;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = 0;
        this.mWifiScanNesting = 0;
        this.mWifiMulticastNesting = 0;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        this.mNetworkStatsPool = new Pools.SynchronizedPool(6);
        this.mWifiNetworkLock = new Object();
        this.mWifiIfaces = EmptyArray.STRING;
        this.mLastWifiNetworkStats = new NetworkStats(0, -1);
        this.mModemNetworkLock = new Object();
        this.mModemIfaces = EmptyArray.STRING;
        this.mLastModemNetworkStats = new NetworkStats(0, -1);
        ModemActivityInfo modemActivityInfo = new ModemActivityInfo(0, 0, 0, new int[0], 0, 0);
        this.mLastModemActivityInfo = modemActivityInfo;
        this.mLastBluetoothActivityInfo = new BluetoothActivityInfoCache();
        this.mPendingWrite = null;
        this.mWriteLock = new ReentrantLock();
        init(clocks);
        if (file != null) {
            this.mFile = new JournaledFile(new File(file, "batterystats.bin"), new File(file, "batterystats.bin.tmp"));
        } else {
            this.mFile = null;
        }
        this.mCheckinFile = new AtomicFile(new File(file, "batterystats-checkin.bin"));
        this.mDailyFile = new AtomicFile(new File(file, "batterystats-daily.xml"));
        this.mHandler = new MyHandler(handler.getLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mStartCount++;
        StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase);
        this.mScreenOnTimer = stopwatchTimer;
        StopwatchTimer stopwatchTimer2 = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase);
        this.mScreenDozeTimer = stopwatchTimer2;
        for (int i = 0; i < 5; i++) {
            StopwatchTimer[] stopwatchTimerArr = this.mScreenBrightnessTimer;
            StopwatchTimer stopwatchTimer3 = new StopwatchTimer(this.mClocks, null, -100 - i, null, this.mOnBatteryTimeBase);
            stopwatchTimerArr[i] = stopwatchTimer3;
        }
        StopwatchTimer stopwatchTimer4 = new StopwatchTimer(this.mClocks, null, -10, null, this.mOnBatteryTimeBase);
        this.mInteractiveTimer = stopwatchTimer4;
        StopwatchTimer stopwatchTimer5 = new StopwatchTimer(this.mClocks, null, -2, null, this.mOnBatteryTimeBase);
        this.mPowerSaveModeEnabledTimer = stopwatchTimer5;
        StopwatchTimer stopwatchTimer6 = new StopwatchTimer(this.mClocks, null, -11, null, this.mOnBatteryTimeBase);
        this.mDeviceIdleModeLightTimer = stopwatchTimer6;
        StopwatchTimer stopwatchTimer7 = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase);
        this.mDeviceIdleModeFullTimer = stopwatchTimer7;
        StopwatchTimer stopwatchTimer8 = new StopwatchTimer(this.mClocks, null, -15, null, this.mOnBatteryTimeBase);
        this.mDeviceLightIdlingTimer = stopwatchTimer8;
        StopwatchTimer stopwatchTimer9 = new StopwatchTimer(this.mClocks, null, -12, null, this.mOnBatteryTimeBase);
        this.mDeviceIdlingTimer = stopwatchTimer9;
        StopwatchTimer stopwatchTimer10 = new StopwatchTimer(this.mClocks, null, -3, null, this.mOnBatteryTimeBase);
        this.mPhoneOnTimer = stopwatchTimer10;
        for (int i2 = 0; i2 < 5; i2++) {
            StopwatchTimer[] stopwatchTimerArr2 = this.mPhoneSignalStrengthsTimer;
            StopwatchTimer stopwatchTimer11 = new StopwatchTimer(this.mClocks, null, -200 - i2, null, this.mOnBatteryTimeBase);
            stopwatchTimerArr2[i2] = stopwatchTimer11;
        }
        StopwatchTimer stopwatchTimer12 = new StopwatchTimer(this.mClocks, null, -199, null, this.mOnBatteryTimeBase);
        this.mPhoneSignalScanningTimer = stopwatchTimer12;
        for (int i3 = 0; i3 < 21; i3++) {
            StopwatchTimer[] stopwatchTimerArr3 = this.mPhoneDataConnectionsTimer;
            StopwatchTimer stopwatchTimer13 = new StopwatchTimer(this.mClocks, null, -300 - i3, null, this.mOnBatteryTimeBase);
            stopwatchTimerArr3[i3] = stopwatchTimer13;
        }
        for (int i4 = 0; i4 < 10; i4++) {
            this.mNetworkByteActivityCounters[i4] = new LongSamplingCounter(this.mOnBatteryTimeBase);
            this.mNetworkPacketActivityCounters[i4] = new LongSamplingCounter(this.mOnBatteryTimeBase);
        }
        this.mWifiActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1);
        this.mBluetoothActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1);
        this.mModemActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 5);
        StopwatchTimer stopwatchTimer14 = new StopwatchTimer(this.mClocks, null, -400, null, this.mOnBatteryTimeBase);
        this.mMobileRadioActiveTimer = stopwatchTimer14;
        StopwatchTimer stopwatchTimer15 = new StopwatchTimer(this.mClocks, null, -401, null, this.mOnBatteryTimeBase);
        this.mMobileRadioActivePerAppTimer = stopwatchTimer15;
        this.mMobileRadioActiveAdjustedTime = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mMobileRadioActiveUnknownTime = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mMobileRadioActiveUnknownCount = new LongSamplingCounter(this.mOnBatteryTimeBase);
        StopwatchTimer stopwatchTimer16 = new StopwatchTimer(this.mClocks, null, 23, null, this.mOnBatteryTimeBase);
        this.mWifiMulticastWakelockTimer = stopwatchTimer16;
        StopwatchTimer stopwatchTimer17 = new StopwatchTimer(this.mClocks, null, -4, null, this.mOnBatteryTimeBase);
        this.mWifiOnTimer = stopwatchTimer17;
        StopwatchTimer stopwatchTimer18 = new StopwatchTimer(this.mClocks, null, -5, null, this.mOnBatteryTimeBase);
        this.mGlobalWifiRunningTimer = stopwatchTimer18;
        for (int i5 = 0; i5 < 8; i5++) {
            StopwatchTimer[] stopwatchTimerArr4 = this.mWifiStateTimer;
            StopwatchTimer stopwatchTimer19 = new StopwatchTimer(this.mClocks, null, -600 - i5, null, this.mOnBatteryTimeBase);
            stopwatchTimerArr4[i5] = stopwatchTimer19;
        }
        for (int i6 = 0; i6 < 13; i6++) {
            StopwatchTimer[] stopwatchTimerArr5 = this.mWifiSupplStateTimer;
            StopwatchTimer stopwatchTimer20 = new StopwatchTimer(this.mClocks, null, -700 - i6, null, this.mOnBatteryTimeBase);
            stopwatchTimerArr5[i6] = stopwatchTimer20;
        }
        for (int i7 = 0; i7 < 5; i7++) {
            StopwatchTimer[] stopwatchTimerArr6 = this.mWifiSignalStrengthsTimer;
            StopwatchTimer stopwatchTimer21 = new StopwatchTimer(this.mClocks, null, -800 - i7, null, this.mOnBatteryTimeBase);
            stopwatchTimerArr6[i7] = stopwatchTimer21;
        }
        StopwatchTimer stopwatchTimer22 = new StopwatchTimer(this.mClocks, null, -900, null, this.mOnBatteryTimeBase);
        this.mWifiActiveTimer = stopwatchTimer22;
        for (int i8 = 0; i8 < 2; i8++) {
            StopwatchTimer[] stopwatchTimerArr7 = this.mGpsSignalQualityTimer;
            StopwatchTimer stopwatchTimer23 = new StopwatchTimer(this.mClocks, null, -1000 - i8, null, this.mOnBatteryTimeBase);
            stopwatchTimerArr7[i8] = stopwatchTimer23;
        }
        StopwatchTimer stopwatchTimer24 = new StopwatchTimer(this.mClocks, null, -7, null, this.mOnBatteryTimeBase);
        this.mAudioOnTimer = stopwatchTimer24;
        StopwatchTimer stopwatchTimer25 = new StopwatchTimer(this.mClocks, null, -8, null, this.mOnBatteryTimeBase);
        this.mVideoOnTimer = stopwatchTimer25;
        StopwatchTimer stopwatchTimer26 = new StopwatchTimer(this.mClocks, null, -9, null, this.mOnBatteryTimeBase);
        this.mFlashlightOnTimer = stopwatchTimer26;
        StopwatchTimer stopwatchTimer27 = new StopwatchTimer(this.mClocks, null, -13, null, this.mOnBatteryTimeBase);
        this.mCameraOnTimer = stopwatchTimer27;
        StopwatchTimer stopwatchTimer28 = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase);
        this.mBluetoothScanTimer = stopwatchTimer28;
        this.mDischargeScreenOffCounter = new LongSamplingCounter(this.mOnBatteryScreenOffTimeBase);
        this.mDischargeScreenDozeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mDischargeLightDozeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mDischargeDeepDozeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mDischargeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mOnBatteryInternal = false;
        this.mOnBattery = false;
        initTimes(this.mClocks.uptimeMillis() * 1000, this.mClocks.elapsedRealtime() * 1000);
        String str = Build.ID;
        this.mEndPlatformVersion = str;
        this.mStartPlatformVersion = str;
        this.mDischargeStartLevel = 0;
        this.mDischargeUnplugLevel = 0;
        this.mDischargePlugLevel = -1;
        this.mDischargeCurrentLevel = 0;
        this.mCurrentBatteryLevel = 0;
        initDischarge();
        clearHistoryLocked();
        updateDailyDeadlineLocked();
        this.mPlatformIdleStateCallback = cb;
        this.mUserInfoProvider = userInfoProvider;
    }

    public BatteryStatsImpl(Parcel p) {
        this(new SystemClocks(), p);
    }

    public BatteryStatsImpl(Clocks clocks, Parcel p) {
        this.mKernelWakelockReader = new KernelWakelockReader();
        this.mTmpWakelockStats = new KernelWakelockStats();
        this.mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
        this.mKernelUidCpuFreqTimeReader = new KernelUidCpuFreqTimeReader();
        this.mKernelUidCpuActiveTimeReader = new KernelUidCpuActiveTimeReader();
        this.mKernelUidCpuClusterTimeReader = new KernelUidCpuClusterTimeReader();
        this.mKernelMemoryBandwidthStats = new KernelMemoryBandwidthStats();
        this.mKernelMemoryStats = new LongSparseArray<>();
        this.mPerProcStateCpuTimesAvailable = true;
        this.mPendingUids = new SparseIntArray();
        this.mCpuTimeReadsTrackingStartTime = SystemClock.uptimeMillis();
        this.mTmpRpmStats = new RpmStats();
        this.mLastRpmStatsUpdateTimeMs = -1000;
        this.mPendingRemovedUids = new LinkedList();
        this.mExternalSync = null;
        this.mUserInfoProvider = null;
        this.mIsolatedUids = new SparseIntArray();
        this.mUidStats = new SparseArray<>();
        this.mPartialTimers = new ArrayList<>();
        this.mFullTimers = new ArrayList<>();
        this.mWindowTimers = new ArrayList<>();
        this.mDrawTimers = new ArrayList<>();
        this.mSensorTimers = new SparseArray<>();
        this.mWifiRunningTimers = new ArrayList<>();
        this.mFullWifiLockTimers = new ArrayList<>();
        this.mWifiMulticastTimers = new ArrayList<>();
        this.mWifiScanTimers = new ArrayList<>();
        this.mWifiBatchedScanTimers = new SparseArray<>();
        this.mAudioTurnedOnTimers = new ArrayList<>();
        this.mVideoTurnedOnTimers = new ArrayList<>();
        this.mFlashlightTurnedOnTimers = new ArrayList<>();
        this.mCameraTurnedOnTimers = new ArrayList<>();
        this.mBluetoothScanOnTimers = new ArrayList<>();
        this.mLastPartialTimers = new ArrayList<>();
        this.mOnBatteryTimeBase = new TimeBase();
        this.mOnBatteryScreenOffTimeBase = new TimeBase();
        this.mActiveEvents = new BatteryStats.HistoryEventTracker();
        this.mHaveBatteryLevel = false;
        this.mRecordingHistory = false;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new BatteryStats.HistoryItem();
        this.mHistoryLastLastWritten = new BatteryStats.HistoryItem();
        this.mHistoryReadTmp = new BatteryStats.HistoryItem();
        this.mHistoryAddTmp = new BatteryStats.HistoryItem();
        this.mHistoryTagPool = new HashMap<>();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = false;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryCur = new BatteryStats.HistoryItem();
        this.mLastHistoryStepDetails = null;
        this.mLastHistoryStepLevel = 0;
        this.mCurHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mReadHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mTmpHistoryStepDetails = new BatteryStats.HistoryStepDetails();
        this.mScreenState = 0;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mUsbDataState = 0;
        this.mGpsSignalQualityBin = -1;
        this.mGpsSignalQualityTimer = new StopwatchTimer[2];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[21];
        this.mNetworkByteActivityCounters = new LongSamplingCounter[10];
        this.mNetworkPacketActivityCounters = new LongSamplingCounter[10];
        this.mHasWifiReporting = false;
        this.mHasBluetoothReporting = false;
        this.mHasModemReporting = false;
        this.mWifiState = -1;
        this.mWifiStateTimer = new StopwatchTimer[8];
        this.mWifiSupplState = -1;
        this.mWifiSupplStateTimer = new StopwatchTimer[13];
        this.mWifiSignalStrengthBin = -1;
        this.mWifiSignalStrengthsTimer = new StopwatchTimer[5];
        this.mIsCellularTxPowerHigh = false;
        this.mMobileRadioPowerState = 1;
        this.mWifiRadioPowerState = 1;
        this.mCharging = true;
        this.mInitStepMode = 0;
        this.mCurStepMode = 0;
        this.mModStepMode = 0;
        this.mDischargeStepTracker = new BatteryStats.LevelStepTracker(200);
        this.mDailyDischargeStepTracker = new BatteryStats.LevelStepTracker(400);
        this.mChargeStepTracker = new BatteryStats.LevelStepTracker(200);
        this.mDailyChargeStepTracker = new BatteryStats.LevelStepTracker(400);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList<>();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mMinLearnedBatteryCapacity = -1;
        this.mMaxLearnedBatteryCapacity = -1;
        this.mRpmStats = new HashMap<>();
        this.mScreenOffRpmStats = new HashMap<>();
        this.mKernelWakelockStats = new HashMap<>();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap<>();
        this.mChangedStates = 0;
        this.mChangedStates2 = 0;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = 0;
        this.mWifiScanNesting = 0;
        this.mWifiMulticastNesting = 0;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        this.mNetworkStatsPool = new Pools.SynchronizedPool(6);
        this.mWifiNetworkLock = new Object();
        this.mWifiIfaces = EmptyArray.STRING;
        this.mLastWifiNetworkStats = new NetworkStats(0, -1);
        this.mModemNetworkLock = new Object();
        this.mModemIfaces = EmptyArray.STRING;
        this.mLastModemNetworkStats = new NetworkStats(0, -1);
        ModemActivityInfo modemActivityInfo = new ModemActivityInfo(0, 0, 0, new int[0], 0, 0);
        this.mLastModemActivityInfo = modemActivityInfo;
        this.mLastBluetoothActivityInfo = new BluetoothActivityInfoCache();
        this.mPendingWrite = null;
        this.mWriteLock = new ReentrantLock();
        init(clocks);
        this.mFile = null;
        this.mCheckinFile = null;
        this.mDailyFile = null;
        this.mHandler = null;
        this.mExternalSync = null;
        this.mConstants = new Constants(this.mHandler);
        clearHistoryLocked();
        readFromParcel(p);
        this.mPlatformIdleStateCallback = null;
    }

    public void setPowerProfileLocked(PowerProfile profile) {
        this.mPowerProfile = profile;
        int numClusters = this.mPowerProfile.getNumCpuClusters();
        this.mKernelCpuSpeedReaders = new KernelCpuSpeedReader[numClusters];
        int firstCpuOfCluster = 0;
        for (int i = 0; i < numClusters; i++) {
            this.mKernelCpuSpeedReaders[i] = new KernelCpuSpeedReader(firstCpuOfCluster, this.mPowerProfile.getNumSpeedStepsInCpuCluster(i));
            firstCpuOfCluster += this.mPowerProfile.getNumCoresInCpuCluster(i);
        }
        if (this.mEstimatedBatteryCapacity == -1) {
            this.mEstimatedBatteryCapacity = (int) this.mPowerProfile.getBatteryCapacity();
        }
    }

    public void setCallback(BatteryCallback cb) {
        this.mCallback = cb;
    }

    public void setRadioScanningTimeoutLocked(long timeout) {
        if (this.mPhoneSignalScanningTimer != null) {
            this.mPhoneSignalScanningTimer.setTimeout(timeout);
        }
    }

    public void setExternalStatsSyncLocked(ExternalStatsSync sync) {
        this.mExternalSync = sync;
    }

    public void updateDailyDeadlineLocked() {
        long currentTime = System.currentTimeMillis();
        this.mDailyStartTime = currentTime;
        Calendar calDeadline = Calendar.getInstance();
        calDeadline.setTimeInMillis(currentTime);
        calDeadline.set(6, calDeadline.get(6) + 1);
        calDeadline.set(14, 0);
        calDeadline.set(13, 0);
        calDeadline.set(12, 0);
        calDeadline.set(11, 1);
        this.mNextMinDailyDeadline = calDeadline.getTimeInMillis();
        calDeadline.set(11, 3);
        this.mNextMaxDailyDeadline = calDeadline.getTimeInMillis();
    }

    public void recordDailyStatsIfNeededLocked(boolean settled) {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= this.mNextMaxDailyDeadline) {
            recordDailyStatsLocked();
        } else if (settled && currentTime >= this.mNextMinDailyDeadline) {
            recordDailyStatsLocked();
        } else if (currentTime < this.mDailyStartTime - 86400000) {
            recordDailyStatsLocked();
        }
    }

    public void recordDailyStatsLocked() {
        BatteryStats.DailyItem item = new BatteryStats.DailyItem();
        item.mStartTime = this.mDailyStartTime;
        item.mEndTime = System.currentTimeMillis();
        boolean hasData = false;
        if (this.mDailyDischargeStepTracker.mNumStepDurations > 0) {
            hasData = true;
            item.mDischargeSteps = new BatteryStats.LevelStepTracker(this.mDailyDischargeStepTracker.mNumStepDurations, this.mDailyDischargeStepTracker.mStepDurations);
        }
        if (this.mDailyChargeStepTracker.mNumStepDurations > 0) {
            hasData = true;
            item.mChargeSteps = new BatteryStats.LevelStepTracker(this.mDailyChargeStepTracker.mNumStepDurations, this.mDailyChargeStepTracker.mStepDurations);
        }
        if (this.mDailyPackageChanges != null) {
            hasData = true;
            item.mPackageChanges = this.mDailyPackageChanges;
            this.mDailyPackageChanges = null;
        }
        this.mDailyDischargeStepTracker.init();
        this.mDailyChargeStepTracker.init();
        updateDailyDeadlineLocked();
        if (hasData) {
            long startTime = SystemClock.uptimeMillis();
            this.mDailyItems.add(item);
            while (this.mDailyItems.size() > 10) {
                this.mDailyItems.remove(0);
            }
            final ByteArrayOutputStream memStream = new ByteArrayOutputStream();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(memStream, StandardCharsets.UTF_8.name());
                writeDailyItemsLocked(out);
                final long initialTime = SystemClock.uptimeMillis() - startTime;
                BackgroundThread.getHandler().post(new Runnable() {
                    public void run() {
                        synchronized (BatteryStatsImpl.this.mCheckinFile) {
                            long startTime2 = SystemClock.uptimeMillis();
                            FileOutputStream stream = null;
                            try {
                                stream = BatteryStatsImpl.this.mDailyFile.startWrite();
                                memStream.writeTo(stream);
                                stream.flush();
                                FileUtils.sync(stream);
                                stream.close();
                                BatteryStatsImpl.this.mDailyFile.finishWrite(stream);
                                EventLogTags.writeCommitSysConfigFile("batterystats-daily", (initialTime + SystemClock.uptimeMillis()) - startTime2);
                            } catch (IOException e) {
                                Slog.w("BatteryStats", "Error writing battery daily items", e);
                                BatteryStatsImpl.this.mDailyFile.failWrite(stream);
                            }
                        }
                    }
                });
            } catch (IOException e) {
            }
        }
    }

    private void writeDailyItemsLocked(XmlSerializer out) throws IOException {
        StringBuilder sb = new StringBuilder(64);
        out.startDocument(null, true);
        out.startTag(null, "daily-items");
        for (int i = 0; i < this.mDailyItems.size(); i++) {
            BatteryStats.DailyItem dit = this.mDailyItems.get(i);
            out.startTag(null, "item");
            out.attribute(null, "start", Long.toString(dit.mStartTime));
            out.attribute(null, "end", Long.toString(dit.mEndTime));
            writeDailyLevelSteps(out, "dis", dit.mDischargeSteps, sb);
            writeDailyLevelSteps(out, "chg", dit.mChargeSteps, sb);
            if (dit.mPackageChanges != null) {
                for (int j = 0; j < dit.mPackageChanges.size(); j++) {
                    BatteryStats.PackageChange pc = (BatteryStats.PackageChange) dit.mPackageChanges.get(j);
                    if (pc.mUpdate) {
                        out.startTag(null, "upd");
                        out.attribute(null, "pkg", pc.mPackageName);
                        out.attribute(null, "ver", Long.toString(pc.mVersionCode));
                        out.endTag(null, "upd");
                    } else {
                        out.startTag(null, "rem");
                        out.attribute(null, "pkg", pc.mPackageName);
                        out.endTag(null, "rem");
                    }
                }
            }
            out.endTag(null, "item");
        }
        out.endTag(null, "daily-items");
        out.endDocument();
    }

    private void writeDailyLevelSteps(XmlSerializer out, String tag, BatteryStats.LevelStepTracker steps, StringBuilder tmpBuilder) throws IOException {
        if (steps != null) {
            out.startTag(null, tag);
            out.attribute(null, "n", Integer.toString(steps.mNumStepDurations));
            for (int i = 0; i < steps.mNumStepDurations; i++) {
                out.startTag(null, "s");
                tmpBuilder.setLength(0);
                steps.encodeEntryAt(i, tmpBuilder);
                out.attribute(null, "v", tmpBuilder.toString());
                out.endTag(null, "s");
            }
            out.endTag(null, tag);
        }
    }

    public void readDailyStatsLocked() {
        Slog.d(TAG, "Reading daily items from " + this.mDailyFile.getBaseFile());
        this.mDailyItems.clear();
        try {
            FileInputStream stream = this.mDailyFile.openRead();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                readDailyItemsLocked(parser);
                try {
                    stream.close();
                } catch (IOException e) {
                }
            } catch (XmlPullParserException e2) {
                stream.close();
            } catch (Throwable th) {
                try {
                    stream.close();
                } catch (IOException e3) {
                }
                throw th;
            }
        } catch (FileNotFoundException e4) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0056 A[Catch:{ IllegalStateException -> 0x00d7, NullPointerException -> 0x00bf, NumberFormatException -> 0x00a7, XmlPullParserException -> 0x008f, IOException -> 0x0077, IndexOutOfBoundsException -> 0x005e }] */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x000e A[Catch:{ IllegalStateException -> 0x00d7, NullPointerException -> 0x00bf, NumberFormatException -> 0x00a7, XmlPullParserException -> 0x008f, IOException -> 0x0077, IndexOutOfBoundsException -> 0x005e }] */
    private void readDailyItemsLocked(XmlPullParser parser) {
        int type;
        while (true) {
            try {
                int next = parser.next();
                type = next;
                if (next == 2 || type == 1) {
                    if (type != 2) {
                        int outerDepth = parser.getDepth();
                        while (true) {
                            int next2 = parser.next();
                            int type2 = next2;
                            if (next2 == 1) {
                                return;
                            }
                            if (type2 == 3 && parser.getDepth() <= outerDepth) {
                                return;
                            }
                            if (type2 != 3) {
                                if (type2 != 4) {
                                    if (parser.getName().equals("item")) {
                                        readDailyItemTagLocked(parser);
                                    } else {
                                        Slog.w(TAG, "Unknown element under <daily-items>: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                    } else {
                        throw new IllegalStateException("no start tag found");
                    }
                }
            } catch (IllegalStateException e) {
                Slog.w(TAG, "Failed parsing daily " + e);
                return;
            } catch (NullPointerException e2) {
                Slog.w(TAG, "Failed parsing daily " + e2);
                return;
            } catch (NumberFormatException e3) {
                Slog.w(TAG, "Failed parsing daily " + e3);
                return;
            } catch (XmlPullParserException e4) {
                Slog.w(TAG, "Failed parsing daily " + e4);
                return;
            } catch (IOException e5) {
                Slog.w(TAG, "Failed parsing daily " + e5);
                return;
            } catch (IndexOutOfBoundsException e6) {
                Slog.w(TAG, "Failed parsing daily " + e6);
                return;
            }
        }
        if (type != 2) {
        }
    }

    /* access modifiers changed from: package-private */
    public void readDailyItemTagLocked(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        BatteryStats.DailyItem dit = new BatteryStats.DailyItem();
        String attr = parser.getAttributeValue(null, "start");
        if (attr != null) {
            dit.mStartTime = Long.parseLong(attr);
        }
        String attr2 = parser.getAttributeValue(null, "end");
        if (attr2 != null) {
            dit.mEndTime = Long.parseLong(attr2);
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                this.mDailyItems.add(dit);
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals("dis")) {
                    readDailyItemTagDetailsLocked(parser, dit, false, "dis");
                } else if (tagName.equals("chg")) {
                    readDailyItemTagDetailsLocked(parser, dit, true, "chg");
                } else if (tagName.equals("upd")) {
                    if (dit.mPackageChanges == null) {
                        dit.mPackageChanges = new ArrayList();
                    }
                    BatteryStats.PackageChange pc = new BatteryStats.PackageChange();
                    pc.mUpdate = true;
                    pc.mPackageName = parser.getAttributeValue(null, "pkg");
                    String verStr = parser.getAttributeValue(null, "ver");
                    pc.mVersionCode = verStr != null ? Long.parseLong(verStr) : 0;
                    dit.mPackageChanges.add(pc);
                    XmlUtils.skipCurrentTag(parser);
                } else if (tagName.equals("rem")) {
                    if (dit.mPackageChanges == null) {
                        dit.mPackageChanges = new ArrayList();
                    }
                    BatteryStats.PackageChange pc2 = new BatteryStats.PackageChange();
                    pc2.mUpdate = false;
                    pc2.mPackageName = parser.getAttributeValue(null, "pkg");
                    dit.mPackageChanges.add(pc2);
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <item>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        this.mDailyItems.add(dit);
    }

    /* access modifiers changed from: package-private */
    public void readDailyItemTagDetailsLocked(XmlPullParser parser, BatteryStats.DailyItem dit, boolean isCharge, String tag) throws NumberFormatException, XmlPullParserException, IOException {
        String numAttr = parser.getAttributeValue(null, "n");
        if (numAttr == null) {
            Slog.w(TAG, "Missing 'n' attribute at " + parser.getPositionDescription());
            XmlUtils.skipCurrentTag(parser);
            return;
        }
        int num = Integer.parseInt(numAttr);
        BatteryStats.LevelStepTracker steps = new BatteryStats.LevelStepTracker(num);
        if (isCharge) {
            dit.mChargeSteps = steps;
        } else {
            dit.mDischargeSteps = steps;
        }
        int i = 0;
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                steps.mNumStepDurations = i;
            } else if (!(type == 3 || type == 4)) {
                if (!"s".equals(parser.getName())) {
                    Slog.w(TAG, "Unknown element under <" + tag + ">: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                } else if (i < num) {
                    String valueAttr = parser.getAttributeValue(null, "v");
                    if (valueAttr != null) {
                        steps.decodeEntryAt(i, valueAttr);
                        i++;
                    }
                }
            }
        }
        steps.mNumStepDurations = i;
    }

    public BatteryStats.DailyItem getDailyItemLocked(int daysAgo) {
        int index = (this.mDailyItems.size() - 1) - daysAgo;
        if (index >= 0) {
            return this.mDailyItems.get(index);
        }
        return null;
    }

    public long getCurrentDailyStartTime() {
        return this.mDailyStartTime;
    }

    public long getNextMinDailyDeadline() {
        return this.mNextMinDailyDeadline;
    }

    public long getNextMaxDailyDeadline() {
        return this.mNextMaxDailyDeadline;
    }

    public boolean startIteratingOldHistoryLocked() {
        BatteryStats.HistoryItem historyItem = this.mHistory;
        this.mHistoryIterator = historyItem;
        if (historyItem == null) {
            return false;
        }
        this.mHistoryBuffer.setDataPosition(0);
        this.mHistoryReadTmp.clear();
        this.mReadOverflow = false;
        this.mIteratingHistory = true;
        return true;
    }

    public boolean getNextOldHistoryLocked(BatteryStats.HistoryItem out) {
        boolean end = this.mHistoryBuffer.dataPosition() >= this.mHistoryBuffer.dataSize();
        if (!end) {
            readHistoryDelta(this.mHistoryBuffer, this.mHistoryReadTmp);
            this.mReadOverflow |= this.mHistoryReadTmp.cmd == 6;
        }
        BatteryStats.HistoryItem cur = this.mHistoryIterator;
        if (cur == null) {
            if (!this.mReadOverflow && !end) {
                Slog.w(TAG, "Old history ends before new history!");
            }
            return false;
        }
        out.setTo(cur);
        this.mHistoryIterator = cur.next;
        if (!this.mReadOverflow) {
            if (end) {
                Slog.w(TAG, "New history ends before old history!");
            } else if (!out.same(this.mHistoryReadTmp)) {
                FastPrintWriter fastPrintWriter = new FastPrintWriter((Writer) new LogWriter(5, TAG));
                fastPrintWriter.println("Histories differ!");
                fastPrintWriter.println("Old history:");
                FastPrintWriter fastPrintWriter2 = fastPrintWriter;
                new BatteryStats.HistoryPrinter().printNextItem(fastPrintWriter2, out, 0, false, true);
                fastPrintWriter.println("New history:");
                new BatteryStats.HistoryPrinter().printNextItem(fastPrintWriter2, this.mHistoryReadTmp, 0, false, true);
                fastPrintWriter.flush();
            }
        }
        return true;
    }

    public void finishIteratingOldHistoryLocked() {
        this.mIteratingHistory = false;
        this.mHistoryBuffer.setDataPosition(this.mHistoryBuffer.dataSize());
        this.mHistoryIterator = null;
    }

    public int getHistoryTotalSize() {
        return MAX_HISTORY_BUFFER;
    }

    public int getHistoryUsedSize() {
        return this.mHistoryBuffer.dataSize();
    }

    public boolean startIteratingHistoryLocked() {
        if (this.mHistoryBuffer.dataSize() <= 0) {
            return false;
        }
        this.mHistoryBuffer.setDataPosition(0);
        this.mReadOverflow = false;
        this.mIteratingHistory = true;
        this.mReadHistoryStrings = new String[this.mHistoryTagPool.size()];
        this.mReadHistoryUids = new int[this.mHistoryTagPool.size()];
        this.mReadHistoryChars = 0;
        for (Map.Entry<BatteryStats.HistoryTag, Integer> ent : this.mHistoryTagPool.entrySet()) {
            BatteryStats.HistoryTag tag = ent.getKey();
            int idx = ent.getValue().intValue();
            this.mReadHistoryStrings[idx] = tag.string;
            this.mReadHistoryUids[idx] = tag.uid;
            this.mReadHistoryChars += tag.string.length() + 1;
        }
        return true;
    }

    public int getHistoryStringPoolSize() {
        return this.mReadHistoryStrings.length;
    }

    public int getHistoryStringPoolBytes() {
        return (this.mReadHistoryStrings.length * 12) + (this.mReadHistoryChars * 2);
    }

    public String getHistoryTagPoolString(int index) {
        return this.mReadHistoryStrings[index];
    }

    public int getHistoryTagPoolUid(int index) {
        return this.mReadHistoryUids[index];
    }

    public boolean getNextHistoryLocked(BatteryStats.HistoryItem out) {
        int pos = this.mHistoryBuffer.dataPosition();
        if (pos == 0) {
            out.clear();
        }
        if (pos >= this.mHistoryBuffer.dataSize()) {
            return false;
        }
        long lastRealtime = out.time;
        long lastWalltime = out.currentTime;
        readHistoryDelta(this.mHistoryBuffer, out);
        if (!(out.cmd == 5 || out.cmd == 7 || lastWalltime == 0)) {
            out.currentTime = (out.time - lastRealtime) + lastWalltime;
        }
        return true;
    }

    public void finishIteratingHistoryLocked() {
        this.mIteratingHistory = false;
        this.mHistoryBuffer.setDataPosition(this.mHistoryBuffer.dataSize());
        this.mReadHistoryStrings = null;
    }

    public long getHistoryBaseTime() {
        return this.mHistoryBaseTime;
    }

    public int getStartCount() {
        return this.mStartCount;
    }

    public boolean isOnBattery() {
        return this.mOnBattery;
    }

    public boolean isCharging() {
        return this.mCharging;
    }

    public boolean isScreenOn() {
        return this.mScreenState == 2;
    }

    public boolean isScreenOn(int state) {
        return state == 2 || state == 5 || state == 6;
    }

    public boolean isScreenOff(int state) {
        return state == 1;
    }

    public boolean isScreenDoze(int state) {
        return state == 3 || state == 4;
    }

    /* access modifiers changed from: package-private */
    public void initTimes(long uptime, long realtime) {
        this.mStartClockTime = System.currentTimeMillis();
        this.mOnBatteryTimeBase.init(uptime, realtime);
        this.mOnBatteryScreenOffTimeBase.init(uptime, realtime);
        this.mRealtime = 0;
        this.mUptime = 0;
        this.mRealtimeStart = realtime;
        this.mUptimeStart = uptime;
    }

    /* access modifiers changed from: package-private */
    public void initDischarge() {
        this.mLowDischargeAmountSinceCharge = 0;
        this.mHighDischargeAmountSinceCharge = 0;
        this.mDischargeAmountScreenOn = 0;
        this.mDischargeAmountScreenOnSinceCharge = 0;
        this.mDischargeAmountScreenOff = 0;
        this.mDischargeAmountScreenOffSinceCharge = 0;
        this.mDischargeAmountScreenDoze = 0;
        this.mDischargeAmountScreenDozeSinceCharge = 0;
        this.mDischargeStepTracker.init();
        this.mChargeStepTracker.init();
        this.mDischargeScreenOffCounter.reset(false);
        this.mDischargeScreenDozeCounter.reset(false);
        this.mDischargeLightDozeCounter.reset(false);
        this.mDischargeDeepDozeCounter.reset(false);
        this.mDischargeCounter.reset(false);
    }

    public void resetAllStatsCmdLocked() {
        resetAllStatsLocked();
        long mSecUptime = this.mClocks.uptimeMillis();
        long uptime = mSecUptime * 1000;
        long mSecRealtime = this.mClocks.elapsedRealtime();
        long realtime = 1000 * mSecRealtime;
        this.mDischargeStartLevel = this.mHistoryCur.batteryLevel;
        pullPendingStateUpdatesLocked();
        addHistoryRecordLocked(mSecRealtime, mSecUptime);
        byte b = this.mHistoryCur.batteryLevel;
        this.mCurrentBatteryLevel = b;
        this.mDischargePlugLevel = b;
        this.mDischargeUnplugLevel = b;
        this.mDischargeCurrentLevel = b;
        this.mOnBatteryTimeBase.reset(uptime, realtime);
        this.mOnBatteryScreenOffTimeBase.reset(uptime, realtime);
        if ((this.mHistoryCur.states & 524288) == 0) {
            if (isScreenOn(this.mScreenState)) {
                this.mDischargeScreenOnUnplugLevel = this.mHistoryCur.batteryLevel;
                this.mDischargeScreenDozeUnplugLevel = 0;
                this.mDischargeScreenOffUnplugLevel = 0;
            } else if (isScreenDoze(this.mScreenState)) {
                this.mDischargeScreenOnUnplugLevel = 0;
                this.mDischargeScreenDozeUnplugLevel = this.mHistoryCur.batteryLevel;
                this.mDischargeScreenOffUnplugLevel = 0;
            } else {
                this.mDischargeScreenOnUnplugLevel = 0;
                this.mDischargeScreenDozeUnplugLevel = 0;
                this.mDischargeScreenOffUnplugLevel = this.mHistoryCur.batteryLevel;
            }
            this.mDischargeAmountScreenOn = 0;
            this.mDischargeAmountScreenOff = 0;
            this.mDischargeAmountScreenDoze = 0;
        }
        initActiveHistoryEventsLocked(mSecRealtime, mSecUptime);
    }

    private void resetAllStatsLocked() {
        long uptimeMillis = this.mClocks.uptimeMillis();
        long elapsedRealtimeMillis = this.mClocks.elapsedRealtime();
        this.mStartCount = 0;
        initTimes(uptimeMillis * 1000, elapsedRealtimeMillis * 1000);
        this.mScreenOnTimer.reset(false);
        this.mScreenDozeTimer.reset(false);
        for (int i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i].reset(false);
        }
        if (this.mPowerProfile != null) {
            this.mEstimatedBatteryCapacity = (int) this.mPowerProfile.getBatteryCapacity();
        } else {
            this.mEstimatedBatteryCapacity = -1;
        }
        this.mMinLearnedBatteryCapacity = -1;
        this.mMaxLearnedBatteryCapacity = -1;
        this.mInteractiveTimer.reset(false);
        this.mPowerSaveModeEnabledTimer.reset(false);
        this.mLastIdleTimeStart = elapsedRealtimeMillis;
        this.mLongestLightIdleTime = 0;
        this.mLongestFullIdleTime = 0;
        this.mDeviceIdleModeLightTimer.reset(false);
        this.mDeviceIdleModeFullTimer.reset(false);
        this.mDeviceLightIdlingTimer.reset(false);
        this.mDeviceIdlingTimer.reset(false);
        this.mPhoneOnTimer.reset(false);
        this.mAudioOnTimer.reset(false);
        this.mVideoOnTimer.reset(false);
        this.mFlashlightOnTimer.reset(false);
        this.mCameraOnTimer.reset(false);
        this.mBluetoothScanTimer.reset(false);
        for (int i2 = 0; i2 < 5; i2++) {
            this.mPhoneSignalStrengthsTimer[i2].reset(false);
        }
        this.mPhoneSignalScanningTimer.reset(false);
        for (int i3 = 0; i3 < 21; i3++) {
            this.mPhoneDataConnectionsTimer[i3].reset(false);
        }
        for (int i4 = 0; i4 < 10; i4++) {
            this.mNetworkByteActivityCounters[i4].reset(false);
            this.mNetworkPacketActivityCounters[i4].reset(false);
        }
        this.mMobileRadioActiveTimer.reset(false);
        this.mMobileRadioActivePerAppTimer.reset(false);
        this.mMobileRadioActiveAdjustedTime.reset(false);
        this.mMobileRadioActiveUnknownTime.reset(false);
        this.mMobileRadioActiveUnknownCount.reset(false);
        this.mWifiOnTimer.reset(false);
        this.mGlobalWifiRunningTimer.reset(false);
        for (int i5 = 0; i5 < 8; i5++) {
            this.mWifiStateTimer[i5].reset(false);
        }
        for (int i6 = 0; i6 < 13; i6++) {
            this.mWifiSupplStateTimer[i6].reset(false);
        }
        for (int i7 = 0; i7 < 5; i7++) {
            this.mWifiSignalStrengthsTimer[i7].reset(false);
        }
        this.mWifiMulticastWakelockTimer.reset(false);
        this.mWifiActiveTimer.reset(false);
        this.mWifiActivity.reset(false);
        for (int i8 = 0; i8 < 2; i8++) {
            this.mGpsSignalQualityTimer[i8].reset(false);
        }
        this.mBluetoothActivity.reset(false);
        this.mModemActivity.reset(false);
        this.mUnpluggedNumConnectivityChange = 0;
        this.mLoadedNumConnectivityChange = 0;
        this.mNumConnectivityChange = 0;
        int i9 = 0;
        while (i9 < this.mUidStats.size()) {
            if (this.mUidStats.valueAt(i9).reset(uptimeMillis * 1000, elapsedRealtimeMillis * 1000)) {
                this.mUidStats.remove(this.mUidStats.keyAt(i9));
                i9--;
            }
            i9++;
        }
        if (this.mRpmStats.size() > 0) {
            for (SamplingTimer timer : this.mRpmStats.values()) {
                this.mOnBatteryTimeBase.remove(timer);
            }
            this.mRpmStats.clear();
        }
        if (this.mScreenOffRpmStats.size() > 0) {
            for (SamplingTimer timer2 : this.mScreenOffRpmStats.values()) {
                this.mOnBatteryScreenOffTimeBase.remove(timer2);
            }
            this.mScreenOffRpmStats.clear();
        }
        if (this.mKernelWakelockStats.size() > 0) {
            for (SamplingTimer timer3 : this.mKernelWakelockStats.values()) {
                this.mOnBatteryScreenOffTimeBase.remove(timer3);
            }
            this.mKernelWakelockStats.clear();
        }
        if (this.mKernelMemoryStats.size() > 0) {
            for (int i10 = 0; i10 < this.mKernelMemoryStats.size(); i10++) {
                this.mOnBatteryTimeBase.remove(this.mKernelMemoryStats.valueAt(i10));
            }
            this.mKernelMemoryStats.clear();
        }
        if (this.mWakeupReasonStats.size() > 0) {
            for (SamplingTimer timer4 : this.mWakeupReasonStats.values()) {
                this.mOnBatteryTimeBase.remove(timer4);
            }
            this.mWakeupReasonStats.clear();
        }
        this.mLastHistoryStepDetails = null;
        this.mLastStepCpuSystemTime = 0;
        this.mLastStepCpuUserTime = 0;
        this.mCurStepCpuSystemTime = 0;
        this.mCurStepCpuUserTime = 0;
        this.mCurStepCpuUserTime = 0;
        this.mLastStepCpuUserTime = 0;
        this.mCurStepCpuSystemTime = 0;
        this.mLastStepCpuSystemTime = 0;
        this.mCurStepStatUserTime = 0;
        this.mLastStepStatUserTime = 0;
        this.mCurStepStatSystemTime = 0;
        this.mLastStepStatSystemTime = 0;
        this.mCurStepStatIOWaitTime = 0;
        this.mLastStepStatIOWaitTime = 0;
        this.mCurStepStatIrqTime = 0;
        this.mLastStepStatIrqTime = 0;
        this.mCurStepStatSoftIrqTime = 0;
        this.mLastStepStatSoftIrqTime = 0;
        this.mCurStepStatIdleTime = 0;
        this.mLastStepStatIdleTime = 0;
        this.mNumAllUidCpuTimeReads = 0;
        this.mNumUidsRemoved = 0;
        initDischarge();
        clearHistoryLocked();
        HwLog.dubaie("DUBAI_TAG_RESET_BATTERY_STAT", "");
        this.mHandler.sendEmptyMessage(4);
    }

    private void initActiveHistoryEventsLocked(long elapsedRealtimeMs, long uptimeMs) {
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < 22) {
                if (this.mRecordAllHistory || i2 != 1) {
                    HashMap<String, SparseIntArray> active = this.mActiveEvents.getStateForEvent(i2);
                    if (active != null) {
                        for (Map.Entry<String, SparseIntArray> ent : active.entrySet()) {
                            SparseIntArray uids = ent.getValue();
                            int j = 0;
                            while (true) {
                                int j2 = j;
                                if (j2 < uids.size()) {
                                    addHistoryEventLocked(elapsedRealtimeMs, uptimeMs, i2, ent.getKey(), uids.keyAt(j2));
                                    j = j2 + 1;
                                }
                            }
                        }
                    }
                }
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDischargeScreenLevelsLocked(int oldState, int newState) {
        updateOldDischargeScreenLevelLocked(oldState);
        updateNewDischargeScreenLevelLocked(newState);
    }

    private void updateOldDischargeScreenLevelLocked(int state) {
        if (isScreenOn(state)) {
            int diff = this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            if (diff > 0) {
                this.mDischargeAmountScreenOn += diff;
                this.mDischargeAmountScreenOnSinceCharge += diff;
            }
        } else if (isScreenDoze(state)) {
            int diff2 = this.mDischargeScreenDozeUnplugLevel - this.mDischargeCurrentLevel;
            if (diff2 > 0) {
                this.mDischargeAmountScreenDoze += diff2;
                this.mDischargeAmountScreenDozeSinceCharge += diff2;
            }
        } else if (isScreenOff(state)) {
            int diff3 = this.mDischargeScreenOffUnplugLevel - this.mDischargeCurrentLevel;
            if (diff3 > 0) {
                this.mDischargeAmountScreenOff += diff3;
                this.mDischargeAmountScreenOffSinceCharge += diff3;
            }
        }
    }

    private void updateNewDischargeScreenLevelLocked(int state) {
        if (isScreenOn(state)) {
            this.mDischargeScreenOnUnplugLevel = this.mDischargeCurrentLevel;
            this.mDischargeScreenOffUnplugLevel = 0;
            this.mDischargeScreenDozeUnplugLevel = 0;
        } else if (isScreenDoze(state)) {
            this.mDischargeScreenOnUnplugLevel = 0;
            this.mDischargeScreenDozeUnplugLevel = this.mDischargeCurrentLevel;
            this.mDischargeScreenOffUnplugLevel = 0;
        } else if (isScreenOff(state)) {
            this.mDischargeScreenOnUnplugLevel = 0;
            this.mDischargeScreenDozeUnplugLevel = 0;
            this.mDischargeScreenOffUnplugLevel = this.mDischargeCurrentLevel;
        }
    }

    public void pullPendingStateUpdatesLocked() {
        if (this.mOnBatteryInternal) {
            updateDischargeScreenLevelsLocked(this.mScreenState, this.mScreenState);
        }
    }

    private NetworkStats readNetworkStatsLocked(String[] ifaces) {
        try {
            if (!ArrayUtils.isEmpty((T[]) ifaces)) {
                return this.mNetworkStatsFactory.readNetworkStatsDetail(-1, ifaces, 0, (NetworkStats) this.mNetworkStatsPool.acquire());
            }
        } catch (IOException e) {
            Slog.e(TAG, "failed to read network stats for ifaces: " + Arrays.toString(ifaces));
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0035, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x02dd, code lost:
        return;
     */
    public void updateWifiState(WifiActivityEnergyInfo info) {
        long elapsedRealtimeMs;
        long totalRxPackets;
        long txTimeMs;
        long rxTimeMs;
        int uidStatsSize;
        long elapsedRealtimeMs2;
        NetworkStats delta;
        long elapsedRealtimeMs3;
        NetworkStats delta2 = null;
        synchronized (this.mWifiNetworkLock) {
            NetworkStats latestStats = readNetworkStatsLocked(this.mWifiIfaces);
            if (latestStats != null) {
                delta2 = NetworkStats.subtract(latestStats, this.mLastWifiNetworkStats, null, null, (NetworkStats) this.mNetworkStatsPool.acquire());
                this.mNetworkStatsPool.release(this.mLastWifiNetworkStats);
                this.mLastWifiNetworkStats = latestStats;
            }
        }
        synchronized (this) {
            try {
                if (this.mOnBatteryInternal) {
                    long elapsedRealtimeMs4 = this.mClocks.elapsedRealtime();
                    SparseLongArray rxPackets = new SparseLongArray();
                    SparseLongArray txPackets = new SparseLongArray();
                    long totalTxPackets = 0;
                    if (delta2 != null) {
                        NetworkStats.Entry entry = new NetworkStats.Entry();
                        int size = delta2.size();
                        totalRxPackets = 0;
                        long totalTxPackets2 = 0;
                        int i = 0;
                        while (i < size) {
                            entry = delta2.getValues(i, entry);
                            if (entry.rxBytes == 0 && entry.txBytes == 0) {
                                elapsedRealtimeMs3 = elapsedRealtimeMs4;
                            } else {
                                Uid u = getUidStatsLocked(mapUid(entry.uid));
                                if (entry.rxBytes != 0) {
                                    elapsedRealtimeMs3 = elapsedRealtimeMs4;
                                    u.noteNetworkActivityLocked(2, entry.rxBytes, entry.rxPackets);
                                    if (entry.set == 0) {
                                        u.noteNetworkActivityLocked(8, entry.rxBytes, entry.rxPackets);
                                    }
                                    this.mNetworkByteActivityCounters[2].addCountLocked(entry.rxBytes);
                                    this.mNetworkPacketActivityCounters[2].addCountLocked(entry.rxPackets);
                                    rxPackets.put(u.getUid(), entry.rxPackets);
                                    totalRxPackets += entry.rxPackets;
                                } else {
                                    elapsedRealtimeMs3 = elapsedRealtimeMs4;
                                }
                                if (entry.txBytes != 0) {
                                    u.noteNetworkActivityLocked(3, entry.txBytes, entry.txPackets);
                                    if (entry.set == 0) {
                                        u.noteNetworkActivityLocked(9, entry.txBytes, entry.txPackets);
                                    }
                                    this.mNetworkByteActivityCounters[3].addCountLocked(entry.txBytes);
                                    this.mNetworkPacketActivityCounters[3].addCountLocked(entry.txPackets);
                                    txPackets.put(u.getUid(), entry.txPackets);
                                    totalTxPackets2 += entry.txPackets;
                                }
                            }
                            i++;
                            elapsedRealtimeMs4 = elapsedRealtimeMs3;
                        }
                        elapsedRealtimeMs = elapsedRealtimeMs4;
                        this.mNetworkStatsPool.release(delta2);
                        delta2 = null;
                        totalTxPackets = totalTxPackets2;
                    } else {
                        elapsedRealtimeMs = elapsedRealtimeMs4;
                        totalRxPackets = 0;
                    }
                    if (info != null) {
                        try {
                            this.mHasWifiReporting = true;
                            long txTimeMs2 = info.getControllerTxTimeMillis();
                            long rxTimeMs2 = info.getControllerRxTimeMillis();
                            long scanTimeMs = info.getControllerScanTimeMillis();
                            long idleTimeMs = info.getControllerIdleTimeMillis();
                            long j = txTimeMs2 + rxTimeMs2 + idleTimeMs;
                            long leftOverRxTimeMs = rxTimeMs2;
                            long leftOverTxTimeMs = txTimeMs2;
                            long totalScanTimeMs = 0;
                            int uidStatsSize2 = this.mUidStats.size();
                            long totalWifiLockTimeMs = 0;
                            int i2 = 0;
                            while (true) {
                                int i3 = i2;
                                if (i3 >= uidStatsSize2) {
                                    break;
                                }
                                delta = delta2;
                                Uid uid = this.mUidStats.valueAt(i3);
                                totalScanTimeMs += uid.mWifiScanTimer.getTimeSinceMarkLocked(elapsedRealtimeMs * 1000) / 1000;
                                totalWifiLockTimeMs += uid.mFullWifiLockTimer.getTimeSinceMarkLocked(elapsedRealtimeMs * 1000) / 1000;
                                i2 = i3 + 1;
                                delta2 = delta;
                                scanTimeMs = scanTimeMs;
                                totalRxPackets = totalRxPackets;
                            }
                            long j2 = scanTimeMs;
                            long totalRxPackets2 = totalRxPackets;
                            int i4 = 0;
                            while (i4 < uidStatsSize2) {
                                Uid uid2 = this.mUidStats.valueAt(i4);
                                long scanTimeSinceMarkMs = uid2.mWifiScanTimer.getTimeSinceMarkLocked(elapsedRealtimeMs * 1000) / 1000;
                                if (scanTimeSinceMarkMs > 0) {
                                    uidStatsSize = uidStatsSize2;
                                    elapsedRealtimeMs2 = elapsedRealtimeMs;
                                    uid2.mWifiScanTimer.setMark(elapsedRealtimeMs2);
                                    long scanRxTimeSinceMarkMs = scanTimeSinceMarkMs;
                                    long scanTxTimeSinceMarkMs = scanTimeSinceMarkMs;
                                    if (totalScanTimeMs > rxTimeMs2) {
                                        scanRxTimeSinceMarkMs = (rxTimeMs2 * scanRxTimeSinceMarkMs) / totalScanTimeMs;
                                    }
                                    rxTimeMs = rxTimeMs2;
                                    long scanRxTimeSinceMarkMs2 = scanRxTimeSinceMarkMs;
                                    if (totalScanTimeMs > txTimeMs2) {
                                        scanTxTimeSinceMarkMs = (txTimeMs2 * scanTxTimeSinceMarkMs) / totalScanTimeMs;
                                    }
                                    txTimeMs = txTimeMs2;
                                    long scanTxTimeSinceMarkMs2 = scanTxTimeSinceMarkMs;
                                    ControllerActivityCounterImpl activityCounter = uid2.getOrCreateWifiControllerActivityLocked();
                                    long j3 = scanTimeSinceMarkMs;
                                    activityCounter.getRxTimeCounter().addCountLocked(scanRxTimeSinceMarkMs2);
                                    activityCounter.getTxTimeCounters()[0].addCountLocked(scanTxTimeSinceMarkMs2);
                                    leftOverRxTimeMs -= scanRxTimeSinceMarkMs2;
                                    leftOverTxTimeMs -= scanTxTimeSinceMarkMs2;
                                } else {
                                    uidStatsSize = uidStatsSize2;
                                    txTimeMs = txTimeMs2;
                                    rxTimeMs = rxTimeMs2;
                                    long j4 = scanTimeSinceMarkMs;
                                    elapsedRealtimeMs2 = elapsedRealtimeMs;
                                }
                                long wifiLockTimeSinceMarkMs = uid2.mFullWifiLockTimer.getTimeSinceMarkLocked(elapsedRealtimeMs2 * 1000) / 1000;
                                if (wifiLockTimeSinceMarkMs > 0) {
                                    uid2.mFullWifiLockTimer.setMark(elapsedRealtimeMs2);
                                    uid2.getOrCreateWifiControllerActivityLocked().getIdleTimeCounter().addCountLocked((wifiLockTimeSinceMarkMs * idleTimeMs) / totalWifiLockTimeMs);
                                }
                                i4++;
                                elapsedRealtimeMs = elapsedRealtimeMs2;
                                uidStatsSize2 = uidStatsSize;
                                rxTimeMs2 = rxTimeMs;
                                txTimeMs2 = txTimeMs;
                            }
                            long j5 = txTimeMs2;
                            long j6 = rxTimeMs2;
                            long j7 = elapsedRealtimeMs;
                            for (int i5 = 0; i5 < txPackets.size(); i5++) {
                                getUidStatsLocked(txPackets.keyAt(i5)).getOrCreateWifiControllerActivityLocked().getTxTimeCounters()[0].addCountLocked((txPackets.valueAt(i5) * leftOverTxTimeMs) / totalTxPackets);
                            }
                            for (int i6 = 0; i6 < rxPackets.size(); i6++) {
                                getUidStatsLocked(rxPackets.keyAt(i6)).getOrCreateWifiControllerActivityLocked().getRxTimeCounter().addCountLocked((rxPackets.valueAt(i6) * leftOverRxTimeMs) / totalRxPackets2);
                            }
                            this.mWifiActivity.getRxTimeCounter().addCountLocked(info.getControllerRxTimeMillis());
                            this.mWifiActivity.getTxTimeCounters()[0].addCountLocked(info.getControllerTxTimeMillis());
                            this.mWifiActivity.getScanTimeCounter().addCountLocked(info.getControllerScanTimeMillis());
                            this.mWifiActivity.getIdleTimeCounter().addCountLocked(info.getControllerIdleTimeMillis());
                            double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
                            if (opVolt != 0.0d) {
                                this.mWifiActivity.getPowerCounter().addCountLocked((long) (((double) info.getControllerEnergyUsed()) / opVolt));
                            }
                        } catch (Throwable th) {
                            th = th;
                            NetworkStats networkStats = delta;
                            throw th;
                        }
                    }
                } else if (delta2 != null) {
                    this.mNetworkStatsPool.release(delta2);
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private ModemActivityInfo getDeltaModemActivityInfo(ModemActivityInfo activityInfo) {
        if (activityInfo == null) {
            return null;
        }
        int[] txTimeMs = new int[5];
        for (int i = 0; i < 5; i++) {
            txTimeMs[i] = activityInfo.getTxTimeMillis()[i] - this.mLastModemActivityInfo.getTxTimeMillis()[i];
        }
        ModemActivityInfo modemActivityInfo = new ModemActivityInfo(activityInfo.getTimestamp(), activityInfo.getSleepTimeMillis() - this.mLastModemActivityInfo.getSleepTimeMillis(), activityInfo.getIdleTimeMillis() - this.mLastModemActivityInfo.getIdleTimeMillis(), txTimeMs, activityInfo.getRxTimeMillis() - this.mLastModemActivityInfo.getRxTimeMillis(), activityInfo.getEnergyUsed() - this.mLastModemActivityInfo.getEnergyUsed());
        this.mLastModemActivityInfo = activityInfo;
        return modemActivityInfo;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x025b, code lost:
        r0 = r0 + 1;
        r2 = r44;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0260, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0263, code lost:
        r44 = r2;
        r5 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0267, code lost:
        r0 = r38 + 1;
        r2 = r44;
        r1 = r47;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0270, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0271, code lost:
        r44 = r2;
        r1 = r47;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0276, code lost:
        r44 = r2;
        r36 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x027b, code lost:
        r44 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x027f, code lost:
        if (r8 <= 0) goto L_0x0292;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0281, code lost:
        r1 = r47;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:?, code lost:
        r1.mMobileRadioActiveUnknownTime.addCountLocked(r8);
        r45 = r5;
        r1.mMobileRadioActiveUnknownCount.addCountLocked(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0292, code lost:
        r45 = r5;
        r1 = r47;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0296, code lost:
        r1.mNetworkStatsPool.release(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x029d, code lost:
        r44 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x029f, code lost:
        monitor-exit(r47);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x02a0, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x02a1, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x02a2, code lost:
        r44 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0035, code lost:
        monitor-enter(r47);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x02a4, code lost:
        monitor-exit(r47);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x02a5, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02a6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0038, code lost:
        if (r1.mOnBatteryInternal != false) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003a, code lost:
        if (r3 == null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r1.mNetworkStatsPool.release(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        r44 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        monitor-exit(r47);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0048, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        if (r2 == null) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004e, code lost:
        r1.mHasModemReporting = true;
        r1.mModemActivity.getIdleTimeCounter().addCountLocked((long) r2.getIdleTimeMillis());
        r1.mModemActivity.getSleepTimeCounter().addCountLocked((long) r2.getSleepTimeMillis());
        r1.mModemActivity.getRxTimeCounter().addCountLocked((long) r2.getRxTimeMillis());
        r6 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007b, code lost:
        if (r6 >= 5) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007d, code lost:
        r1.mModemActivity.getTxTimeCounters()[r6].addCountLocked((long) r2.getTxTimeMillis()[r6]);
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00a4, code lost:
        if ((r1.mPowerProfile.getAveragePower(com.android.internal.os.PowerProfile.POWER_MODEM_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d) == 0.0d) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a6, code lost:
        r8 = ((((double) r2.getSleepTimeMillis()) * r1.mPowerProfile.getAveragePower(com.android.internal.os.PowerProfile.POWER_MODEM_CONTROLLER_SLEEP)) + (((double) r2.getIdleTimeMillis()) * r1.mPowerProfile.getAveragePower(com.android.internal.os.PowerProfile.POWER_MODEM_CONTROLLER_IDLE))) + (((double) r2.getRxTimeMillis()) * r1.mPowerProfile.getAveragePower(com.android.internal.os.PowerProfile.POWER_MODEM_CONTROLLER_RX));
        r10 = r2.getTxTimeMillis();
        r11 = r8;
        r8 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00de, code lost:
        if (r8 >= java.lang.Math.min(r10.length, 5)) goto L_0x00f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00e0, code lost:
        r11 = r11 + (((double) r10[r8]) * r1.mPowerProfile.getAveragePower(com.android.internal.os.PowerProfile.POWER_MODEM_CONTROLLER_TX, r8));
        r8 = r8 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00f0, code lost:
        r1.mModemActivity.getPowerCounter().addCountLocked((long) r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r6 = r1.mClocks.elapsedRealtime();
        r8 = r1.mMobileRadioActivePerAppTimer.getTimeSinceMarkLocked(1000 * r6);
        r1.mMobileRadioActivePerAppTimer.setMark(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0112, code lost:
        if (r3 == null) goto L_0x029d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0114, code lost:
        r14 = new android.net.NetworkStats.Entry();
        r15 = r3.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x011d, code lost:
        r16 = 0;
        r11 = 0;
        r10 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0123, code lost:
        if (r10 >= r15) goto L_0x01c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r14 = r3.getValues(r10, r14);
        r20 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0130, code lost:
        if (r14.rxPackets != 0) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0136, code lost:
        if (r14.txPackets != 0) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x013c, code lost:
        r11 = r11 + r14.rxPackets;
        r16 = r16 + r14.txPackets;
        r22 = r1.getUidStatsLocked(r1.mapUid(r14.uid));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r22.noteNetworkActivityLocked(0, r14.rxBytes, r14.rxPackets);
        r22.noteNetworkActivityLocked(1, r14.txBytes, r14.txPackets);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x016b, code lost:
        if (r14.set != 0) goto L_0x018b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x016d, code lost:
        r22.noteNetworkActivityLocked(6, r14.rxBytes, r14.rxPackets);
        r22.noteNetworkActivityLocked(7, r14.txBytes, r14.txPackets);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x018b, code lost:
        r1 = r47;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        r1.mNetworkByteActivityCounters[0].addCountLocked(r14.rxBytes);
        r1.mNetworkByteActivityCounters[1].addCountLocked(r14.txBytes);
        r1.mNetworkPacketActivityCounters[0].addCountLocked(r14.rxPackets);
        r1.mNetworkPacketActivityCounters[1].addCountLocked(r14.txPackets);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x01b3, code lost:
        r10 = r10 + 1;
        r6 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x01ba, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01bb, code lost:
        r44 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01bd, code lost:
        r1 = r47;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01c1, code lost:
        r20 = r6;
        r5 = r11 + r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01c7, code lost:
        if (r5 <= 0) goto L_0x027b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x01c9, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01ca, code lost:
        if (r0 >= r15) goto L_0x0276;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        r14 = r3.getValues(r0, r14);
        r36 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01d7, code lost:
        if (r14.rxPackets != 0) goto L_0x01e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x01dd, code lost:
        if (r14.txPackets != 0) goto L_0x01e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01df, code lost:
        r38 = r0;
        r44 = r2;
        r5 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
        r4 = r1.getUidStatsLocked(r1.mapUid(r14.uid));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01f4, code lost:
        r38 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:?, code lost:
        r5 = r14.rxPackets + r14.txPackets;
        r0 = (r8 * r5) / r36;
        r4.noteMobileRadioActiveTimeLocked(r0);
        r8 = r8 - r0;
        r22 = r36 - r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0203, code lost:
        if (r2 == null) goto L_0x0263;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0205, code lost:
        r7 = r4.getOrCreateModemControllerActivityLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x020c, code lost:
        if (r11 <= 0) goto L_0x0230;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x020e, code lost:
        r39 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0214, code lost:
        if (r14.rxPackets <= 0) goto L_0x022b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0216, code lost:
        r41 = r4;
        r42 = r5;
        r7.getRxTimeCounter().addCountLocked((r14.rxPackets * ((long) r2.getRxTimeMillis())) / r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x022b, code lost:
        r41 = r4;
        r42 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0230, code lost:
        r39 = r0;
        r41 = r4;
        r42 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0238, code lost:
        if (r16 <= 0) goto L_0x0263;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x023e, code lost:
        if (r14.txPackets <= 0) goto L_0x0263;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0240, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0242, code lost:
        if (r0 >= 5) goto L_0x0263;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x024c, code lost:
        r44 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
        r7.getTxTimeCounters()[r0].addCountLocked((r14.txPackets * ((long) r2.getTxTimeMillis()[r0])) / r16);
     */
    public void updateMobileRadioState(ModemActivityInfo activityInfo) {
        BatteryStatsImpl batteryStatsImpl = this;
        ModemActivityInfo deltaInfo = getDeltaModemActivityInfo(activityInfo);
        batteryStatsImpl.addModemTxPowerToHistory(deltaInfo);
        NetworkStats delta = null;
        synchronized (batteryStatsImpl.mModemNetworkLock) {
            try {
                NetworkStats latestStats = batteryStatsImpl.readNetworkStatsLocked(batteryStatsImpl.mModemIfaces);
                if (latestStats != null) {
                    try {
                        delta = NetworkStats.subtract(latestStats, batteryStatsImpl.mLastModemNetworkStats, null, null, (NetworkStats) batteryStatsImpl.mNetworkStatsPool.acquire());
                        batteryStatsImpl.mNetworkStatsPool.release(batteryStatsImpl.mLastModemNetworkStats);
                        batteryStatsImpl.mLastModemNetworkStats = latestStats;
                    } catch (Throwable th) {
                        th = th;
                        ModemActivityInfo modemActivityInfo = deltaInfo;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                ModemActivityInfo modemActivityInfo2 = deltaInfo;
                while (true) {
                    try {
                        break;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0043, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0059, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005b, code lost:
        return;
     */
    private synchronized void addModemTxPowerToHistory(ModemActivityInfo activityInfo) {
        if (activityInfo != null) {
            int[] txTimeMs = activityInfo.getTxTimeMillis();
            if (txTimeMs != null) {
                if (txTimeMs.length == 5) {
                    long elapsedRealtime = this.mClocks.elapsedRealtime();
                    long uptime = this.mClocks.uptimeMillis();
                    int levelMaxTimeSpent = 0;
                    for (int i = 1; i < txTimeMs.length; i++) {
                        if (txTimeMs[i] > txTimeMs[levelMaxTimeSpent]) {
                            levelMaxTimeSpent = i;
                        }
                    }
                    if (levelMaxTimeSpent == 4) {
                        if (!this.mIsCellularTxPowerHigh) {
                            this.mHistoryCur.states2 |= 524288;
                            addHistoryRecordLocked(elapsedRealtime, uptime);
                            this.mIsCellularTxPowerHigh = true;
                        }
                    } else if (this.mIsCellularTxPowerHigh) {
                        this.mHistoryCur.states2 &= -524289;
                        addHistoryRecordLocked(elapsedRealtime, uptime);
                        this.mIsCellularTxPowerHigh = false;
                    }
                }
            }
        }
    }

    public void updateBluetoothStateLocked(BluetoothActivityEnergyInfo info) {
        long elapsedRealtimeMs;
        boolean normalizeScanRxTime;
        BatteryStatsImpl batteryStatsImpl = this;
        if (info == null || !batteryStatsImpl.mOnBatteryInternal) {
            BatteryStatsImpl batteryStatsImpl2 = batteryStatsImpl;
            return;
        }
        batteryStatsImpl.mHasBluetoothReporting = true;
        long elapsedRealtimeMs2 = batteryStatsImpl.mClocks.elapsedRealtime();
        long rxTimeMs = info.getControllerRxTimeMillis() - batteryStatsImpl.mLastBluetoothActivityInfo.rxTimeMs;
        long txTimeMs = info.getControllerTxTimeMillis() - batteryStatsImpl.mLastBluetoothActivityInfo.txTimeMs;
        long idleTimeMs = info.getControllerIdleTimeMillis() - batteryStatsImpl.mLastBluetoothActivityInfo.idleTimeMs;
        int uidCount = batteryStatsImpl.mUidStats.size();
        long totalScanTimeMs = 0;
        for (int i = 0; i < uidCount; i++) {
            Uid u = batteryStatsImpl.mUidStats.valueAt(i);
            if (u.mBluetoothScanTimer != null) {
                totalScanTimeMs += u.mBluetoothScanTimer.getTimeSinceMarkLocked(elapsedRealtimeMs2 * 1000) / 1000;
            }
        }
        long totalScanTimeMs2 = totalScanTimeMs;
        boolean normalizeScanRxTime2 = totalScanTimeMs2 > rxTimeMs;
        boolean normalizeScanTxTime = totalScanTimeMs2 > txTimeMs;
        long leftOverRxTimeMs = rxTimeMs;
        long leftOverTxTimeMs = txTimeMs;
        int i2 = 0;
        while (i2 < uidCount) {
            int uidCount2 = uidCount;
            Uid u2 = batteryStatsImpl.mUidStats.valueAt(i2);
            long idleTimeMs2 = idleTimeMs;
            if (u2.mBluetoothScanTimer == null) {
                normalizeScanRxTime = normalizeScanRxTime2;
                elapsedRealtimeMs = elapsedRealtimeMs2;
            } else {
                long scanTimeTxSinceMarkMs = u2.mBluetoothScanTimer.getTimeSinceMarkLocked(elapsedRealtimeMs2 * 1000) / 1000;
                if (scanTimeTxSinceMarkMs > 0) {
                    u2.mBluetoothScanTimer.setMark(elapsedRealtimeMs2);
                    long scanTimeRxSinceMarkMs = scanTimeTxSinceMarkMs;
                    long scanTimeTxSinceMarkMs2 = scanTimeTxSinceMarkMs;
                    if (normalizeScanRxTime2) {
                        scanTimeRxSinceMarkMs = (rxTimeMs * scanTimeRxSinceMarkMs) / totalScanTimeMs2;
                    }
                    if (normalizeScanTxTime) {
                        scanTimeTxSinceMarkMs2 = (txTimeMs * scanTimeTxSinceMarkMs2) / totalScanTimeMs2;
                    }
                    long scanTimeSinceMarkMs = scanTimeTxSinceMarkMs;
                    long scanTimeTxSinceMarkMs3 = scanTimeTxSinceMarkMs2;
                    normalizeScanRxTime = normalizeScanRxTime2;
                    ControllerActivityCounterImpl counter = u2.getOrCreateBluetoothControllerActivityLocked();
                    elapsedRealtimeMs = elapsedRealtimeMs2;
                    counter.getRxTimeCounter().addCountLocked(scanTimeRxSinceMarkMs);
                    counter.getTxTimeCounters()[0].addCountLocked(scanTimeTxSinceMarkMs3);
                    leftOverRxTimeMs -= scanTimeRxSinceMarkMs;
                    leftOverTxTimeMs -= scanTimeTxSinceMarkMs3;
                } else {
                    normalizeScanRxTime = normalizeScanRxTime2;
                    elapsedRealtimeMs = elapsedRealtimeMs2;
                }
            }
            i2++;
            uidCount = uidCount2;
            idleTimeMs = idleTimeMs2;
            normalizeScanRxTime2 = normalizeScanRxTime;
            elapsedRealtimeMs2 = elapsedRealtimeMs;
            batteryStatsImpl = this;
            BluetoothActivityEnergyInfo bluetoothActivityEnergyInfo = info;
        }
        long j = elapsedRealtimeMs2;
        long idleTimeMs3 = idleTimeMs;
        int i3 = uidCount;
        long totalRxBytes = 0;
        BluetoothActivityEnergyInfo bluetoothActivityEnergyInfo2 = info;
        UidTraffic[] uidTraffic = info.getUidTraffic();
        int numUids = uidTraffic != null ? uidTraffic.length : 0;
        long totalTxBytes = 0;
        int i4 = 0;
        while (i4 < numUids) {
            UidTraffic traffic = uidTraffic[i4];
            boolean normalizeScanTxTime2 = normalizeScanTxTime;
            long txTimeMs2 = txTimeMs;
            long rxBytes = traffic.getRxBytes() - this.mLastBluetoothActivityInfo.uidRxBytes.get(traffic.getUid());
            long txBytes = traffic.getTxBytes() - this.mLastBluetoothActivityInfo.uidTxBytes.get(traffic.getUid());
            this.mNetworkByteActivityCounters[4].addCountLocked(rxBytes);
            this.mNetworkByteActivityCounters[5].addCountLocked(txBytes);
            Uid uidStatsLocked = getUidStatsLocked(mapUid(traffic.getUid()));
            uidStatsLocked.noteNetworkActivityLocked(4, rxBytes, 0);
            uidStatsLocked.noteNetworkActivityLocked(5, txBytes, 0);
            totalRxBytes += rxBytes;
            totalTxBytes += txBytes;
            i4++;
            normalizeScanTxTime = normalizeScanTxTime2;
            txTimeMs = txTimeMs2;
            rxTimeMs = rxTimeMs;
            BluetoothActivityEnergyInfo bluetoothActivityEnergyInfo3 = info;
        }
        long rxTimeMs2 = rxTimeMs;
        long txTimeMs3 = txTimeMs;
        boolean z = normalizeScanTxTime;
        if (!((totalTxBytes == 0 && totalRxBytes == 0) || (leftOverRxTimeMs == 0 && leftOverTxTimeMs == 0))) {
            for (int i5 = 0; i5 < numUids; i5++) {
                UidTraffic traffic2 = uidTraffic[i5];
                int uid = traffic2.getUid();
                long rxBytes2 = traffic2.getRxBytes() - this.mLastBluetoothActivityInfo.uidRxBytes.get(uid);
                UidTraffic uidTraffic2 = traffic2;
                long txBytes2 = traffic2.getTxBytes() - this.mLastBluetoothActivityInfo.uidTxBytes.get(uid);
                Uid u3 = getUidStatsLocked(mapUid(uid));
                int i6 = uid;
                ControllerActivityCounterImpl counter2 = u3.getOrCreateBluetoothControllerActivityLocked();
                if (totalRxBytes <= 0 || rxBytes2 <= 0) {
                    long j2 = rxBytes2;
                } else {
                    long j3 = rxBytes2;
                    long rxBytes3 = (leftOverRxTimeMs * rxBytes2) / totalRxBytes;
                    Uid uid2 = u3;
                    counter2.getRxTimeCounter().addCountLocked(rxBytes3);
                    leftOverRxTimeMs -= rxBytes3;
                }
                if (totalTxBytes > 0 && txBytes2 > 0) {
                    long timeTxMs = (leftOverTxTimeMs * txBytes2) / totalTxBytes;
                    counter2.getTxTimeCounters()[0].addCountLocked(timeTxMs);
                    leftOverTxTimeMs -= timeTxMs;
                }
            }
        }
        this.mBluetoothActivity.getRxTimeCounter().addCountLocked(rxTimeMs2);
        this.mBluetoothActivity.getTxTimeCounters()[0].addCountLocked(txTimeMs3);
        long j4 = totalRxBytes;
        long idleTimeMs4 = idleTimeMs3;
        this.mBluetoothActivity.getIdleTimeCounter().addCountLocked(idleTimeMs4);
        double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
        if (opVolt != 0.0d) {
            long j5 = idleTimeMs4;
            this.mBluetoothActivity.getPowerCounter().addCountLocked((long) (((double) (info.getControllerEnergyUsed() - this.mLastBluetoothActivityInfo.energy)) / opVolt));
        }
        this.mLastBluetoothActivityInfo.set(info);
    }

    public void updateRpmStatsLocked() {
        if (this.mPlatformIdleStateCallback != null) {
            long now = SystemClock.elapsedRealtime();
            long j = 1000;
            if (now - this.mLastRpmStatsUpdateTimeMs >= 1000) {
                this.mPlatformIdleStateCallback.fillLowPowerStats(this.mTmpRpmStats);
                this.mLastRpmStatsUpdateTimeMs = now;
            }
            for (Map.Entry<String, RpmStats.PowerStatePlatformSleepState> pstate : this.mTmpRpmStats.mPlatformLowPowerStats.entrySet()) {
                String pName = pstate.getKey();
                getRpmTimerLocked(pName).update(pstate.getValue().mTimeMs * j, pstate.getValue().mCount);
                for (Map.Entry<String, RpmStats.PowerStateElement> voter : pstate.getValue().mVoters.entrySet()) {
                    getRpmTimerLocked(pName + "." + voter.getKey()).update(voter.getValue().mTimeMs * j, voter.getValue().mCount);
                    j = 1000;
                }
                j = 1000;
            }
            for (Map.Entry<String, RpmStats.PowerStateSubsystem> subsys : this.mTmpRpmStats.mSubsystemLowPowerStats.entrySet()) {
                String subsysName = subsys.getKey();
                for (Map.Entry<String, RpmStats.PowerStateElement> sstate : subsys.getValue().mStates.entrySet()) {
                    int count = sstate.getValue().mCount;
                    getRpmTimerLocked(subsysName + "." + sstate.getKey()).update(sstate.getValue().mTimeMs * 1000, count);
                }
            }
        }
    }

    public void updateKernelWakelocksLocked() {
        long startTime = SystemClock.elapsedRealtime();
        KernelWakelockStats wakelockStats = this.mKernelWakelockReader.readKernelWakelockStats(this.mTmpWakelockStats);
        long readKernelWakelockStatsDuration = SystemClock.elapsedRealtime() - startTime;
        if (readKernelWakelockStatsDuration > 500) {
            Slog.w(TAG, "Read kernel wake lock stats duration: " + readKernelWakelockStatsDuration + " ms. ");
        }
        if (wakelockStats == null) {
            Slog.w(TAG, "Couldn't get kernel wake lock stats");
            return;
        }
        for (Map.Entry<String, KernelWakelockStats.Entry> ent : wakelockStats.entrySet()) {
            String name = ent.getKey();
            KernelWakelockStats.Entry kws = ent.getValue();
            SamplingTimer kwlt = this.mKernelWakelockStats.get(name);
            if (kwlt == null) {
                kwlt = new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase);
                this.mKernelWakelockStats.put(name, kwlt);
            }
            kwlt.update(kws.mTotalTime, kws.mCount);
            kwlt.setUpdateVersion(kws.mVersion);
        }
        int numWakelocksSetStale = 0;
        for (Map.Entry<String, SamplingTimer> ent2 : this.mKernelWakelockStats.entrySet()) {
            SamplingTimer st = ent2.getValue();
            if (st.getUpdateVersion() != wakelockStats.kernelWakelockVersion) {
                st.endSample();
                numWakelocksSetStale++;
            }
        }
        if (wakelockStats.isEmpty()) {
            Slog.wtf(TAG, "All kernel wakelocks had time of zero");
        }
        if (numWakelocksSetStale == this.mKernelWakelockStats.size()) {
            Slog.wtf(TAG, "All kernel wakelocks were set stale. new version=" + wakelockStats.kernelWakelockVersion);
        }
    }

    public void updateKernelMemoryBandwidthLocked() {
        SamplingTimer timer;
        this.mKernelMemoryBandwidthStats.updateStats();
        LongSparseLongArray bandwidthEntries = this.mKernelMemoryBandwidthStats.getBandwidthEntries();
        int bandwidthEntryCount = bandwidthEntries.size();
        for (int i = 0; i < bandwidthEntryCount; i++) {
            int indexOfKey = this.mKernelMemoryStats.indexOfKey(bandwidthEntries.keyAt(i));
            int index = indexOfKey;
            if (indexOfKey >= 0) {
                timer = this.mKernelMemoryStats.valueAt(index);
            } else {
                timer = new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase);
                this.mKernelMemoryStats.put(bandwidthEntries.keyAt(i), timer);
            }
            timer.update(bandwidthEntries.valueAt(i), 1);
        }
    }

    public boolean isOnBatteryLocked() {
        return this.mOnBatteryTimeBase.isRunning();
    }

    public boolean isOnBatteryScreenOffLocked() {
        return this.mOnBatteryScreenOffTimeBase.isRunning();
    }

    @GuardedBy("this")
    public void updateCpuTimeLocked(boolean onBattery, boolean onBatteryScreenOff) {
        if (this.mPowerProfile != null) {
            if (this.mCpuFreqs == null) {
                this.mCpuFreqs = this.mKernelUidCpuFreqTimeReader.readFreqs(this.mPowerProfile);
            }
            ArrayList<StopwatchTimer> partialTimersToConsider = null;
            if (onBatteryScreenOff) {
                partialTimersToConsider = new ArrayList<>();
                for (int i = this.mPartialTimers.size() - 1; i >= 0; i--) {
                    StopwatchTimer timer = this.mPartialTimers.get(i);
                    if (!(!timer.mInList || timer.mUid == null || timer.mUid.mUid == 1000)) {
                        partialTimersToConsider.add(timer);
                    }
                }
            }
            markPartialTimersAsEligible();
            SparseLongArray updatedUids = null;
            if (!onBattery) {
                this.mKernelUidCpuTimeReader.readDelta(null);
                this.mKernelUidCpuFreqTimeReader.readDelta(null);
                this.mNumAllUidCpuTimeReads += 2;
                if (this.mConstants.TRACK_CPU_ACTIVE_CLUSTER_TIME) {
                    this.mKernelUidCpuActiveTimeReader.readDelta(null);
                    this.mKernelUidCpuClusterTimeReader.readDelta(null);
                    this.mNumAllUidCpuTimeReads += 2;
                }
                for (int cluster = this.mKernelCpuSpeedReaders.length - 1; cluster >= 0; cluster--) {
                    this.mKernelCpuSpeedReaders[cluster].readDelta();
                }
                return;
            }
            this.mUserInfoProvider.refreshUserIds();
            if (!this.mKernelUidCpuFreqTimeReader.perClusterTimesAvailable()) {
                updatedUids = new SparseLongArray();
            }
            readKernelUidCpuTimesLocked(partialTimersToConsider, updatedUids, onBattery);
            if (updatedUids != null) {
                updateClusterSpeedTimes(updatedUids, onBattery);
            }
            readKernelUidCpuFreqTimesLocked(partialTimersToConsider, onBattery, onBatteryScreenOff);
            this.mNumAllUidCpuTimeReads += 2;
            if (this.mConstants.TRACK_CPU_ACTIVE_CLUSTER_TIME) {
                readKernelUidCpuActiveTimesLocked(onBattery);
                readKernelUidCpuClusterTimesLocked(onBattery);
                this.mNumAllUidCpuTimeReads += 2;
            }
        }
    }

    @VisibleForTesting
    public void markPartialTimersAsEligible() {
        int i;
        if (ArrayUtils.referenceEquals(this.mPartialTimers, this.mLastPartialTimers)) {
            for (int i2 = this.mPartialTimers.size() - 1; i2 >= 0; i2--) {
                this.mPartialTimers.get(i2).mInList = true;
            }
            return;
        }
        int i3 = this.mLastPartialTimers.size() - 1;
        while (true) {
            if (i3 < 0) {
                break;
            }
            this.mLastPartialTimers.get(i3).mInList = false;
            i3--;
        }
        this.mLastPartialTimers.clear();
        int numPartialTimers = this.mPartialTimers.size();
        for (i = 0; i < numPartialTimers; i++) {
            StopwatchTimer timer = this.mPartialTimers.get(i);
            timer.mInList = true;
            this.mLastPartialTimers.add(timer);
        }
    }

    @VisibleForTesting
    public void updateClusterSpeedTimes(SparseLongArray updatedUids, boolean onBattery) {
        BatteryStatsImpl batteryStatsImpl = this;
        SparseLongArray sparseLongArray = updatedUids;
        long[][] clusterSpeedTimesMs = new long[batteryStatsImpl.mKernelCpuSpeedReaders.length][];
        long totalCpuClustersTimeMs = 0;
        for (int cluster = 0; cluster < batteryStatsImpl.mKernelCpuSpeedReaders.length; cluster++) {
            clusterSpeedTimesMs[cluster] = batteryStatsImpl.mKernelCpuSpeedReaders[cluster].readDelta();
            if (clusterSpeedTimesMs[cluster] != null) {
                for (int speed = clusterSpeedTimesMs[cluster].length - 1; speed >= 0; speed--) {
                    totalCpuClustersTimeMs += clusterSpeedTimesMs[cluster][speed];
                }
            }
        }
        if (totalCpuClustersTimeMs != 0) {
            int updatedUidsCount = updatedUids.size();
            int i = 0;
            while (i < updatedUidsCount) {
                Uid u = batteryStatsImpl.getUidStatsLocked(sparseLongArray.keyAt(i));
                long appCpuTimeUs = sparseLongArray.valueAt(i);
                int numClusters = batteryStatsImpl.mPowerProfile.getNumCpuClusters();
                if (u.mCpuClusterSpeedTimesUs == null || u.mCpuClusterSpeedTimesUs.length != numClusters) {
                    u.mCpuClusterSpeedTimesUs = new LongSamplingCounter[numClusters][];
                }
                int cluster2 = 0;
                while (cluster2 < clusterSpeedTimesMs.length) {
                    int speedsInCluster = clusterSpeedTimesMs[cluster2].length;
                    if (u.mCpuClusterSpeedTimesUs[cluster2] == null || speedsInCluster != u.mCpuClusterSpeedTimesUs[cluster2].length) {
                        u.mCpuClusterSpeedTimesUs[cluster2] = new LongSamplingCounter[speedsInCluster];
                    }
                    LongSamplingCounter[] cpuSpeeds = u.mCpuClusterSpeedTimesUs[cluster2];
                    int speed2 = 0;
                    while (speed2 < speedsInCluster) {
                        if (cpuSpeeds[speed2] == null) {
                            cpuSpeeds[speed2] = new LongSamplingCounter(batteryStatsImpl.mOnBatteryTimeBase);
                        }
                        cpuSpeeds[speed2].addCountLocked((clusterSpeedTimesMs[cluster2][speed2] * appCpuTimeUs) / totalCpuClustersTimeMs, onBattery);
                        speed2++;
                        clusterSpeedTimesMs = clusterSpeedTimesMs;
                        batteryStatsImpl = this;
                        SparseLongArray sparseLongArray2 = updatedUids;
                    }
                    boolean z = onBattery;
                    long[][] jArr = clusterSpeedTimesMs;
                    cluster2++;
                    batteryStatsImpl = this;
                    SparseLongArray sparseLongArray3 = updatedUids;
                }
                boolean z2 = onBattery;
                long[][] jArr2 = clusterSpeedTimesMs;
                i++;
                batteryStatsImpl = this;
                sparseLongArray = updatedUids;
            }
        }
        boolean z3 = onBattery;
        long[][] jArr3 = clusterSpeedTimesMs;
    }

    @VisibleForTesting
    public void readKernelUidCpuTimesLocked(ArrayList<StopwatchTimer> partialTimers, SparseLongArray updatedUids, boolean onBattery) {
        ArrayList<StopwatchTimer> arrayList = partialTimers;
        SparseLongArray sparseLongArray = updatedUids;
        boolean z = onBattery;
        this.mTempTotalCpuSystemTimeUs = 0;
        this.mTempTotalCpuUserTimeUs = 0;
        int numWakelocks = arrayList == null ? 0 : partialTimers.size();
        long startTimeMs = this.mClocks.uptimeMillis();
        this.mKernelUidCpuTimeReader.readDelta(new KernelUidCpuTimeReader.Callback(numWakelocks, z, sparseLongArray) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ SparseLongArray f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void onUidCpuTime(int i, long j, long j2) {
                BatteryStatsImpl.lambda$readKernelUidCpuTimesLocked$0(BatteryStatsImpl.this, this.f$1, this.f$2, this.f$3, i, j, j2);
            }
        });
        if (this.mClocks.uptimeMillis() - startTimeMs >= 100) {
            Slog.d(TAG, "Reading cpu stats took " + elapsedTimeMs + "ms");
        }
        if (numWakelocks > 0) {
            this.mTempTotalCpuUserTimeUs = (this.mTempTotalCpuUserTimeUs * 50) / 100;
            this.mTempTotalCpuSystemTimeUs = (this.mTempTotalCpuSystemTimeUs * 50) / 100;
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= numWakelocks) {
                    break;
                }
                StopwatchTimer timer = arrayList.get(i2);
                int userTimeUs = (int) (this.mTempTotalCpuUserTimeUs / ((long) (numWakelocks - i2)));
                int numWakelocks2 = numWakelocks;
                long startTimeMs2 = startTimeMs;
                int systemTimeUs = (int) (this.mTempTotalCpuSystemTimeUs / ((long) (numWakelocks - i2)));
                timer.mUid.mUserCpuTime.addCountLocked((long) userTimeUs, z);
                timer.mUid.mSystemCpuTime.addCountLocked((long) systemTimeUs, z);
                if (sparseLongArray != null) {
                    int uid = timer.mUid.getUid();
                    sparseLongArray.put(uid, sparseLongArray.get(uid, 0) + ((long) userTimeUs) + ((long) systemTimeUs));
                }
                timer.mUid.getProcessStatsLocked("*wakelock*").addCpuTimeLocked(userTimeUs / 1000, systemTimeUs / 1000, z);
                this.mTempTotalCpuUserTimeUs -= (long) userTimeUs;
                this.mTempTotalCpuSystemTimeUs -= (long) systemTimeUs;
                i = i2 + 1;
                numWakelocks = numWakelocks2;
                startTimeMs = startTimeMs2;
            }
        }
        long j = startTimeMs;
    }

    public static /* synthetic */ void lambda$readKernelUidCpuTimesLocked$0(BatteryStatsImpl batteryStatsImpl, int numWakelocks, boolean onBattery, SparseLongArray updatedUids, int uid, long userTimeUs, long systemTimeUs) {
        long systemTimeUs2;
        long userTimeUs2;
        BatteryStatsImpl batteryStatsImpl2 = batteryStatsImpl;
        boolean z = onBattery;
        SparseLongArray sparseLongArray = updatedUids;
        int uid2 = batteryStatsImpl2.mapUid(uid);
        if (Process.isIsolated(uid2)) {
            batteryStatsImpl2.mKernelUidCpuTimeReader.removeUid(uid2);
            Slog.d(TAG, "Got readings for an isolated uid with no mapping: " + uid2);
        } else if (!batteryStatsImpl2.mUserInfoProvider.exists(UserHandle.getUserId(uid2))) {
            Slog.d(TAG, "Got readings for an invalid user's uid " + uid2);
            batteryStatsImpl2.mKernelUidCpuTimeReader.removeUid(uid2);
        } else {
            Uid u = batteryStatsImpl2.getUidStatsLocked(uid2);
            batteryStatsImpl2.mTempTotalCpuUserTimeUs += userTimeUs;
            batteryStatsImpl2.mTempTotalCpuSystemTimeUs += systemTimeUs;
            StringBuilder sb = null;
            if (numWakelocks > 0) {
                userTimeUs2 = (userTimeUs * 50) / 100;
                systemTimeUs2 = (50 * systemTimeUs) / 100;
            } else {
                userTimeUs2 = userTimeUs;
                systemTimeUs2 = systemTimeUs;
            }
            if (sb != null) {
                sb.append("  adding to uid=");
                sb.append(u.mUid);
                sb.append(": u=");
                TimeUtils.formatDuration(userTimeUs2 / 1000, sb);
                sb.append(" s=");
                TimeUtils.formatDuration(systemTimeUs2 / 1000, sb);
                Slog.d(TAG, sb.toString());
            }
            u.mUserCpuTime.addCountLocked(userTimeUs2, z);
            u.mSystemCpuTime.addCountLocked(systemTimeUs2, z);
            if (sparseLongArray != null) {
                sparseLongArray.put(u.getUid(), userTimeUs2 + systemTimeUs2);
            }
        }
    }

    @VisibleForTesting
    public void readKernelUidCpuFreqTimesLocked(ArrayList<StopwatchTimer> partialTimers, boolean onBattery, boolean onBatteryScreenOff) {
        long elapsedTimeMs;
        ArrayList<StopwatchTimer> arrayList = partialTimers;
        boolean perClusterTimesAvailable = this.mKernelUidCpuFreqTimeReader.perClusterTimesAvailable();
        int numWakelocks = arrayList == null ? 0 : partialTimers.size();
        int numClusters = this.mPowerProfile.getNumCpuClusters();
        this.mWakeLockAllocationsUs = null;
        long startTimeMs = this.mClocks.uptimeMillis();
        KernelUidCpuFreqTimeReader kernelUidCpuFreqTimeReader = this.mKernelUidCpuFreqTimeReader;
        $$Lambda$BatteryStatsImpl$qYIdEyLMO9XI4FHBl_g5LWknDZQ r10 = r0;
        $$Lambda$BatteryStatsImpl$qYIdEyLMO9XI4FHBl_g5LWknDZQ r0 = new KernelUidCpuFreqTimeReader.Callback(onBattery, onBatteryScreenOff, perClusterTimesAvailable, numClusters, numWakelocks) {
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ boolean f$3;
            private final /* synthetic */ int f$4;
            private final /* synthetic */ int f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void onUidCpuFreqTime(int i, long[] jArr) {
                BatteryStatsImpl.lambda$readKernelUidCpuFreqTimesLocked$1(BatteryStatsImpl.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, i, jArr);
            }
        };
        kernelUidCpuFreqTimeReader.readDelta(r10);
        long elapsedTimeMs2 = this.mClocks.uptimeMillis() - startTimeMs;
        if (elapsedTimeMs2 >= 100) {
            Slog.d(TAG, "Reading cpu freq times took " + elapsedTimeMs2 + "ms");
        }
        if (this.mWakeLockAllocationsUs != null) {
            int i = 0;
            while (i < numWakelocks) {
                Uid u = arrayList.get(i).mUid;
                if (u.mCpuClusterSpeedTimesUs == null || u.mCpuClusterSpeedTimesUs.length != numClusters) {
                    u.mCpuClusterSpeedTimesUs = new LongSamplingCounter[numClusters][];
                }
                int cluster = 0;
                while (cluster < numClusters) {
                    int speedsInCluster = this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
                    if (u.mCpuClusterSpeedTimesUs[cluster] == null || u.mCpuClusterSpeedTimesUs[cluster].length != speedsInCluster) {
                        u.mCpuClusterSpeedTimesUs[cluster] = new LongSamplingCounter[speedsInCluster];
                    }
                    LongSamplingCounter[] cpuTimeUs = u.mCpuClusterSpeedTimesUs[cluster];
                    int speed = 0;
                    while (speed < speedsInCluster) {
                        if (cpuTimeUs[speed] == null) {
                            elapsedTimeMs = elapsedTimeMs2;
                            cpuTimeUs[speed] = new LongSamplingCounter(this.mOnBatteryTimeBase);
                        } else {
                            elapsedTimeMs = elapsedTimeMs2;
                        }
                        long allocationUs = this.mWakeLockAllocationsUs[cluster][speed] / ((long) (numWakelocks - i));
                        cpuTimeUs[speed].addCountLocked(allocationUs, onBattery);
                        long[] jArr = this.mWakeLockAllocationsUs[cluster];
                        jArr[speed] = jArr[speed] - allocationUs;
                        speed++;
                        elapsedTimeMs2 = elapsedTimeMs;
                        perClusterTimesAvailable = perClusterTimesAvailable;
                        ArrayList<StopwatchTimer> arrayList2 = partialTimers;
                    }
                    boolean perClusterTimesAvailable2 = perClusterTimesAvailable;
                    boolean perClusterTimesAvailable3 = onBattery;
                    cluster++;
                    perClusterTimesAvailable = perClusterTimesAvailable2;
                    ArrayList<StopwatchTimer> arrayList3 = partialTimers;
                }
                boolean perClusterTimesAvailable4 = perClusterTimesAvailable;
                boolean perClusterTimesAvailable5 = onBattery;
                i++;
                perClusterTimesAvailable = perClusterTimesAvailable4;
                arrayList = partialTimers;
            }
        }
        boolean z = perClusterTimesAvailable;
        boolean perClusterTimesAvailable6 = onBattery;
    }

    public static /* synthetic */ void lambda$readKernelUidCpuFreqTimesLocked$1(BatteryStatsImpl batteryStatsImpl, boolean onBattery, boolean onBatteryScreenOff, boolean perClusterTimesAvailable, int numClusters, int numWakelocks, int uid, long[] cpuFreqTimeMs) {
        long appAllocationUs;
        BatteryStatsImpl batteryStatsImpl2 = batteryStatsImpl;
        boolean z = onBattery;
        int i = numClusters;
        long[] jArr = cpuFreqTimeMs;
        int uid2 = batteryStatsImpl2.mapUid(uid);
        if (Process.isIsolated(uid2)) {
            batteryStatsImpl2.mKernelUidCpuFreqTimeReader.removeUid(uid2);
            Slog.d(TAG, "Got freq readings for an isolated uid with no mapping: " + uid2);
        } else if (!batteryStatsImpl2.mUserInfoProvider.exists(UserHandle.getUserId(uid2))) {
            Slog.d(TAG, "Got freq readings for an invalid user's uid " + uid2);
            batteryStatsImpl2.mKernelUidCpuFreqTimeReader.removeUid(uid2);
        } else {
            Uid u = batteryStatsImpl2.getUidStatsLocked(uid2);
            if (u.mCpuFreqTimeMs == null || u.mCpuFreqTimeMs.getSize() != jArr.length) {
                u.mCpuFreqTimeMs = new LongSamplingCounterArray(batteryStatsImpl2.mOnBatteryTimeBase);
            }
            u.mCpuFreqTimeMs.addCountLocked(jArr, z);
            if (u.mScreenOffCpuFreqTimeMs == null || u.mScreenOffCpuFreqTimeMs.getSize() != jArr.length) {
                u.mScreenOffCpuFreqTimeMs = new LongSamplingCounterArray(batteryStatsImpl2.mOnBatteryScreenOffTimeBase);
            }
            u.mScreenOffCpuFreqTimeMs.addCountLocked(jArr, onBatteryScreenOff);
            if (perClusterTimesAvailable) {
                if (u.mCpuClusterSpeedTimesUs == null || u.mCpuClusterSpeedTimesUs.length != i) {
                    u.mCpuClusterSpeedTimesUs = new LongSamplingCounter[i][];
                }
                if (numWakelocks > 0 && batteryStatsImpl2.mWakeLockAllocationsUs == null) {
                    batteryStatsImpl2.mWakeLockAllocationsUs = new long[i][];
                }
                int freqIndex = 0;
                int cluster = 0;
                while (cluster < i) {
                    int speedsInCluster = batteryStatsImpl2.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
                    if (u.mCpuClusterSpeedTimesUs[cluster] == null || u.mCpuClusterSpeedTimesUs[cluster].length != speedsInCluster) {
                        u.mCpuClusterSpeedTimesUs[cluster] = new LongSamplingCounter[speedsInCluster];
                    }
                    if (numWakelocks > 0 && batteryStatsImpl2.mWakeLockAllocationsUs[cluster] == null) {
                        batteryStatsImpl2.mWakeLockAllocationsUs[cluster] = new long[speedsInCluster];
                    }
                    LongSamplingCounter[] cpuTimesUs = u.mCpuClusterSpeedTimesUs[cluster];
                    int freqIndex2 = freqIndex;
                    int speed = 0;
                    while (speed < speedsInCluster) {
                        if (cpuTimesUs[speed] == null) {
                            cpuTimesUs[speed] = new LongSamplingCounter(batteryStatsImpl2.mOnBatteryTimeBase);
                        }
                        if (batteryStatsImpl2.mWakeLockAllocationsUs != null) {
                            appAllocationUs = ((jArr[freqIndex2] * 1000) * 50) / 100;
                            long[] jArr2 = batteryStatsImpl2.mWakeLockAllocationsUs[cluster];
                            jArr2[speed] = jArr2[speed] + ((jArr[freqIndex2] * 1000) - appAllocationUs);
                        } else {
                            appAllocationUs = jArr[freqIndex2] * 1000;
                        }
                        cpuTimesUs[speed].addCountLocked(appAllocationUs, z);
                        freqIndex2++;
                        speed++;
                        int i2 = numClusters;
                    }
                    cluster++;
                    freqIndex = freqIndex2;
                    i = numClusters;
                }
            }
        }
    }

    @VisibleForTesting
    public void readKernelUidCpuActiveTimesLocked(boolean onBattery) {
        long startTimeMs = this.mClocks.uptimeMillis();
        this.mKernelUidCpuActiveTimeReader.readDelta(new KernelUidCpuActiveTimeReader.Callback(onBattery) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void onUidCpuActiveTime(int i, long j) {
                BatteryStatsImpl.lambda$readKernelUidCpuActiveTimesLocked$2(BatteryStatsImpl.this, this.f$1, i, j);
            }
        });
        long elapsedTimeMs = this.mClocks.uptimeMillis() - startTimeMs;
        if (elapsedTimeMs >= 100) {
            Slog.d(TAG, "Reading cpu active times took " + elapsedTimeMs + "ms");
        }
    }

    public static /* synthetic */ void lambda$readKernelUidCpuActiveTimesLocked$2(BatteryStatsImpl batteryStatsImpl, boolean onBattery, int uid, long cpuActiveTimesMs) {
        int uid2 = batteryStatsImpl.mapUid(uid);
        if (Process.isIsolated(uid2)) {
            batteryStatsImpl.mKernelUidCpuActiveTimeReader.removeUid(uid2);
            Slog.w(TAG, "Got active times for an isolated uid with no mapping: " + uid2);
        } else if (!batteryStatsImpl.mUserInfoProvider.exists(UserHandle.getUserId(uid2))) {
            Slog.w(TAG, "Got active times for an invalid user's uid " + uid2);
            batteryStatsImpl.mKernelUidCpuActiveTimeReader.removeUid(uid2);
        } else {
            batteryStatsImpl.getUidStatsLocked(uid2).mCpuActiveTimeMs.addCountLocked(cpuActiveTimesMs, onBattery);
        }
    }

    @VisibleForTesting
    public void readKernelUidCpuClusterTimesLocked(boolean onBattery) {
        long startTimeMs = this.mClocks.uptimeMillis();
        this.mKernelUidCpuClusterTimeReader.readDelta(new KernelUidCpuClusterTimeReader.Callback(onBattery) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void onUidCpuPolicyTime(int i, long[] jArr) {
                BatteryStatsImpl.lambda$readKernelUidCpuClusterTimesLocked$3(BatteryStatsImpl.this, this.f$1, i, jArr);
            }
        });
        long elapsedTimeMs = this.mClocks.uptimeMillis() - startTimeMs;
        if (elapsedTimeMs >= 100) {
            Slog.d(TAG, "Reading cpu cluster times took " + elapsedTimeMs + "ms");
        }
    }

    public static /* synthetic */ void lambda$readKernelUidCpuClusterTimesLocked$3(BatteryStatsImpl batteryStatsImpl, boolean onBattery, int uid, long[] cpuClusterTimesMs) {
        int uid2 = batteryStatsImpl.mapUid(uid);
        if (Process.isIsolated(uid2)) {
            batteryStatsImpl.mKernelUidCpuClusterTimeReader.removeUid(uid2);
            Slog.w(TAG, "Got cluster times for an isolated uid with no mapping: " + uid2);
        } else if (!batteryStatsImpl.mUserInfoProvider.exists(UserHandle.getUserId(uid2))) {
            Slog.w(TAG, "Got cluster times for an invalid user's uid " + uid2);
            batteryStatsImpl.mKernelUidCpuClusterTimeReader.removeUid(uid2);
        } else {
            batteryStatsImpl.getUidStatsLocked(uid2).mCpuClusterTimesMs.addCountLocked(cpuClusterTimesMs, onBattery);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setChargingLocked(boolean charging) {
        if (this.mCharging == charging) {
            return false;
        }
        this.mCharging = charging;
        if (charging) {
            this.mHistoryCur.states2 |= 16777216;
        } else {
            this.mHistoryCur.states2 &= -16777217;
        }
        this.mHandler.sendEmptyMessage(3);
        return true;
    }

    /* access modifiers changed from: protected */
    @GuardedBy("this")
    public void setOnBatteryLocked(long mSecRealtime, long mSecUptime, boolean onBattery, int oldStatus, int level, int chargeUAh) {
        boolean reset;
        int i;
        boolean z = onBattery;
        int i2 = oldStatus;
        int i3 = level;
        int i4 = chargeUAh;
        boolean doWrite = false;
        Message m = this.mHandler.obtainMessage(2);
        m.arg1 = z ? 1 : 0;
        this.mHandler.sendMessage(m);
        long uptime = mSecUptime * 1000;
        long realtime = mSecRealtime * 1000;
        int screenState = this.mScreenState;
        if (z) {
            boolean reset2 = false;
            if (!this.mNoAutoReset) {
                if (i2 == 5 || i3 >= 90 || ((this.mDischargeCurrentLevel < 20 && i3 >= 80) || (getHighDischargeAmountSinceCharge() >= 200 && this.mHistoryBuffer.dataSize() >= MAX_HISTORY_BUFFER))) {
                    Slog.i(TAG, "Resetting battery stats: level=" + i3 + " status=" + i2 + " dischargeLevel=" + this.mDischargeCurrentLevel + " lowAmount=" + getLowDischargeAmountSinceCharge() + " highAmount=" + getHighDischargeAmountSinceCharge());
                    if (getLowDischargeAmountSinceCharge() >= 20) {
                        long startTime = SystemClock.uptimeMillis();
                        final Parcel parcel = Parcel.obtain();
                        writeSummaryToParcel(parcel, true);
                        final long initialTime = SystemClock.uptimeMillis() - startTime;
                        long j = startTime;
                        BackgroundThread.getHandler().post(new Runnable() {
                            public void run() {
                                Parcel parcel;
                                synchronized (BatteryStatsImpl.this.mCheckinFile) {
                                    long startTime2 = SystemClock.uptimeMillis();
                                    FileOutputStream stream = null;
                                    try {
                                        stream = BatteryStatsImpl.this.mCheckinFile.startWrite();
                                        stream.write(parcel.marshall());
                                        stream.flush();
                                        FileUtils.sync(stream);
                                        stream.close();
                                        BatteryStatsImpl.this.mCheckinFile.finishWrite(stream);
                                        EventLogTags.writeCommitSysConfigFile("batterystats-checkin", (initialTime + SystemClock.uptimeMillis()) - startTime2);
                                        parcel = parcel;
                                    } catch (IOException e) {
                                        try {
                                            Slog.w("BatteryStats", "Error writing checkin battery statistics", e);
                                            BatteryStatsImpl.this.mCheckinFile.failWrite(stream);
                                            parcel = parcel;
                                        } catch (Throwable th) {
                                            parcel.recycle();
                                            throw th;
                                        }
                                    }
                                    parcel.recycle();
                                }
                            }
                        });
                    }
                    resetAllStatsLocked();
                    if (i4 > 0 && i3 > 0) {
                        this.mEstimatedBatteryCapacity = (int) (((double) (i4 / 1000)) / (((double) i3) / 100.0d));
                    }
                    this.mDischargeStartLevel = i3;
                    reset2 = true;
                    LogPower.push(190);
                    this.mDischargeStepTracker.init();
                    doWrite = true;
                }
                reset = reset2;
            } else {
                reset = false;
            }
            if (this.mCharging) {
                setChargingLocked(false);
            }
            this.mLastChargingStateLevel = i3;
            this.mOnBatteryInternal = true;
            this.mOnBattery = true;
            this.mLastDischargeStepLevel = i3;
            this.mMinDischargeStepLevel = i3;
            this.mDischargeStepTracker.clearTime();
            this.mDailyDischargeStepTracker.clearTime();
            this.mInitStepMode = this.mCurStepMode;
            this.mModStepMode = 0;
            pullPendingStateUpdatesLocked();
            this.mHistoryCur.batteryLevel = (byte) i3;
            this.mHistoryCur.states &= -524289;
            if (reset) {
                this.mRecordingHistory = true;
                i = 0;
                startRecordingHistory(mSecRealtime, mSecUptime, reset);
            } else {
                i = 0;
            }
            addHistoryRecordLocked(mSecRealtime, mSecUptime);
            this.mDischargeUnplugLevel = i3;
            this.mDischargeCurrentLevel = i3;
            if (isScreenOn(screenState)) {
                this.mDischargeScreenOnUnplugLevel = i3;
                this.mDischargeScreenDozeUnplugLevel = i;
                this.mDischargeScreenOffUnplugLevel = i;
            } else if (isScreenDoze(screenState)) {
                this.mDischargeScreenOnUnplugLevel = i;
                this.mDischargeScreenDozeUnplugLevel = i3;
                this.mDischargeScreenOffUnplugLevel = i;
            } else {
                this.mDischargeScreenOnUnplugLevel = i;
                this.mDischargeScreenDozeUnplugLevel = i;
                this.mDischargeScreenOffUnplugLevel = i3;
            }
            this.mDischargeAmountScreenOn = i;
            this.mDischargeAmountScreenDoze = i;
            this.mDischargeAmountScreenOff = i;
            updateTimeBasesLocked(true, screenState, uptime, realtime);
            int i5 = screenState;
        } else {
            int screenState2 = screenState;
            this.mLastChargingStateLevel = i3;
            this.mOnBatteryInternal = false;
            this.mOnBattery = false;
            pullPendingStateUpdatesLocked();
            this.mHistoryCur.batteryLevel = (byte) i3;
            this.mHistoryCur.states |= 524288;
            addHistoryRecordLocked(mSecRealtime, mSecUptime);
            this.mDischargePlugLevel = i3;
            this.mDischargeCurrentLevel = i3;
            if (i3 < this.mDischargeUnplugLevel) {
                this.mLowDischargeAmountSinceCharge += (this.mDischargeUnplugLevel - i3) - 1;
                this.mHighDischargeAmountSinceCharge += this.mDischargeUnplugLevel - i3;
            }
            updateDischargeScreenLevelsLocked(screenState2, screenState2);
            int i6 = screenState2;
            updateTimeBasesLocked(false, screenState2, uptime, realtime);
            this.mChargeStepTracker.init();
            this.mLastChargeStepLevel = i3;
            this.mMaxChargeStepLevel = i3;
            this.mInitStepMode = this.mCurStepMode;
            this.mModStepMode = 0;
        }
        if ((doWrite || this.mLastWriteTime + 60000 < mSecRealtime) && this.mFile != null) {
            writeAsyncLocked();
        }
    }

    private void startRecordingHistory(long elapsedRealtimeMs, long uptimeMs, boolean reset) {
        this.mRecordingHistory = true;
        this.mHistoryCur.currentTime = System.currentTimeMillis();
        addHistoryBufferLocked(elapsedRealtimeMs, reset ? (byte) 7 : 5, this.mHistoryCur);
        this.mHistoryCur.currentTime = 0;
        if (reset) {
            initActiveHistoryEventsLocked(elapsedRealtimeMs, uptimeMs);
        }
    }

    private void recordCurrentTimeChangeLocked(long currentTime, long elapsedRealtimeMs, long uptimeMs) {
        if (this.mRecordingHistory) {
            this.mHistoryCur.currentTime = currentTime;
            addHistoryBufferLocked(elapsedRealtimeMs, (byte) 5, this.mHistoryCur);
            this.mHistoryCur.currentTime = 0;
        }
    }

    private void recordShutdownLocked(long elapsedRealtimeMs, long uptimeMs) {
        if (this.mRecordingHistory) {
            this.mHistoryCur.currentTime = System.currentTimeMillis();
            addHistoryBufferLocked(elapsedRealtimeMs, (byte) 8, this.mHistoryCur);
            this.mHistoryCur.currentTime = 0;
        }
    }

    private void scheduleSyncExternalStatsLocked(String reason, int updateFlags) {
        if (this.mExternalSync != null) {
            this.mExternalSync.scheduleSync(reason, updateFlags);
        }
    }

    @GuardedBy("this")
    public void setBatteryStateLocked(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        boolean onBattery;
        long uptime;
        long elapsedRealtime;
        boolean onBattery2;
        int i;
        boolean z;
        int i2 = status;
        int i3 = health;
        int i4 = plugType;
        int oldStatus = level;
        int i5 = volt;
        int i6 = chargeUAh;
        int i7 = chargeFullUAh;
        int temp2 = Math.max(0, temp);
        reportChangesToStatsLog(this.mHaveBatteryLevel ? this.mHistoryCur : null, i2, i4, oldStatus);
        boolean onBattery3 = isOnBattery(i4, i2);
        long uptime2 = this.mClocks.uptimeMillis();
        long elapsedRealtime2 = this.mClocks.elapsedRealtime();
        if (!this.mHaveBatteryLevel) {
            this.mHaveBatteryLevel = true;
            if (onBattery3 == this.mOnBattery) {
                if (onBattery3) {
                    this.mHistoryCur.states &= -524289;
                } else {
                    this.mHistoryCur.states |= 524288;
                }
            }
            this.mHistoryCur.states2 |= 16777216;
            this.mHistoryCur.batteryStatus = (byte) i2;
            this.mHistoryCur.batteryLevel = (byte) oldStatus;
            this.mHistoryCur.batteryChargeUAh = i6;
            this.mLastDischargeStepLevel = oldStatus;
            this.mLastChargeStepLevel = oldStatus;
            this.mMinDischargeStepLevel = oldStatus;
            this.mMaxChargeStepLevel = oldStatus;
            this.mLastChargingStateLevel = oldStatus;
        } else if (!(this.mCurrentBatteryLevel == oldStatus && this.mOnBattery == onBattery3)) {
            recordDailyStatsIfNeededLocked(oldStatus >= 100 && onBattery3);
        }
        int temp3 = this.mHistoryCur.batteryStatus;
        if (onBattery3) {
            this.mDischargeCurrentLevel = oldStatus;
            if (!this.mRecordingHistory) {
                this.mRecordingHistory = true;
                elapsedRealtime = elapsedRealtime2;
                uptime = uptime2;
                onBattery = onBattery3;
                startRecordingHistory(elapsedRealtime2, uptime2, true);
            } else {
                elapsedRealtime = elapsedRealtime2;
                uptime = uptime2;
                onBattery = onBattery3;
            }
        } else {
            elapsedRealtime = elapsedRealtime2;
            uptime = uptime2;
            onBattery = onBattery3;
            if (oldStatus < 96 && i2 != 1 && !this.mRecordingHistory) {
                this.mRecordingHistory = true;
                startRecordingHistory(elapsedRealtime, uptime, true);
            }
        }
        this.mCurrentBatteryLevel = oldStatus;
        if (this.mDischargePlugLevel < 0) {
            this.mDischargePlugLevel = oldStatus;
        }
        boolean onBattery4 = onBattery;
        if (onBattery4 != this.mOnBattery) {
            this.mHistoryCur.batteryLevel = (byte) oldStatus;
            this.mHistoryCur.batteryStatus = (byte) i2;
            this.mHistoryCur.batteryHealth = (byte) i3;
            this.mHistoryCur.batteryPlugType = (byte) i4;
            this.mHistoryCur.batteryTemperature = (short) temp2;
            this.mHistoryCur.batteryVoltage = (char) i5;
            if (i6 < this.mHistoryCur.batteryChargeUAh) {
                long chargeDiff = (long) (this.mHistoryCur.batteryChargeUAh - i6);
                this.mDischargeCounter.addCountLocked(chargeDiff);
                this.mDischargeScreenOffCounter.addCountLocked(chargeDiff);
                if (isScreenDoze(this.mScreenState)) {
                    this.mDischargeScreenDozeCounter.addCountLocked(chargeDiff);
                }
                if (this.mDeviceIdleMode == 1) {
                    this.mDischargeLightDozeCounter.addCountLocked(chargeDiff);
                } else if (this.mDeviceIdleMode == 2) {
                    this.mDischargeDeepDozeCounter.addCountLocked(chargeDiff);
                }
            }
            this.mHistoryCur.batteryChargeUAh = i6;
            onBattery2 = onBattery4;
            byte b = temp3;
            setOnBatteryLocked(elapsedRealtime, uptime, onBattery4, temp3, oldStatus, i6);
            int i8 = temp2;
            long j = elapsedRealtime;
            long j2 = uptime;
            int i9 = volt;
        } else {
            onBattery2 = onBattery4;
            int temp4 = temp2;
            int i10 = temp3;
            boolean changed = false;
            if (this.mHistoryCur.batteryLevel != oldStatus) {
                this.mHistoryCur.batteryLevel = (byte) oldStatus;
                changed = true;
                this.mExternalSync.scheduleSyncDueToBatteryLevelChange(this.mConstants.BATTERY_LEVEL_COLLECTION_DELAY_MS);
            }
            if (this.mHistoryCur.batteryStatus != i2) {
                this.mHistoryCur.batteryStatus = (byte) i2;
                changed = true;
            }
            if (this.mHistoryCur.batteryHealth != i3) {
                this.mHistoryCur.batteryHealth = (byte) i3;
                changed = true;
            }
            if (this.mHistoryCur.batteryPlugType != i4) {
                this.mHistoryCur.batteryPlugType = (byte) i4;
                changed = true;
            }
            if (temp4 >= this.mHistoryCur.batteryTemperature + 10 || temp4 <= this.mHistoryCur.batteryTemperature - 10) {
                this.mHistoryCur.batteryTemperature = (short) temp4;
                changed = true;
            }
            int i11 = temp4;
            int i12 = volt;
            if (i12 > this.mHistoryCur.batteryVoltage + 20 || i12 < this.mHistoryCur.batteryVoltage - 20) {
                this.mHistoryCur.batteryVoltage = (char) i12;
                changed = true;
            }
            if (i6 >= this.mHistoryCur.batteryChargeUAh + 10 || i6 <= this.mHistoryCur.batteryChargeUAh - 10) {
                if (i6 < this.mHistoryCur.batteryChargeUAh) {
                    long chargeDiff2 = (long) (this.mHistoryCur.batteryChargeUAh - i6);
                    this.mDischargeCounter.addCountLocked(chargeDiff2);
                    this.mDischargeScreenOffCounter.addCountLocked(chargeDiff2);
                    if (isScreenDoze(this.mScreenState)) {
                        this.mDischargeScreenDozeCounter.addCountLocked(chargeDiff2);
                    }
                    z = true;
                    if (this.mDeviceIdleMode == 1) {
                        this.mDischargeLightDozeCounter.addCountLocked(chargeDiff2);
                    } else if (this.mDeviceIdleMode == 2) {
                        this.mDischargeDeepDozeCounter.addCountLocked(chargeDiff2);
                    }
                } else {
                    z = true;
                }
                this.mHistoryCur.batteryChargeUAh = i6;
                changed = true;
            } else {
                z = true;
            }
            long modeBits = (((long) this.mInitStepMode) << 48) | (((long) this.mModStepMode) << 56) | (((long) (oldStatus & 255)) << 40);
            if (onBattery2) {
                changed |= setChargingLocked(false);
                if (this.mLastDischargeStepLevel != oldStatus && this.mMinDischargeStepLevel > oldStatus) {
                    long j3 = modeBits;
                    long j4 = elapsedRealtime;
                    this.mDischargeStepTracker.addLevelSteps(this.mLastDischargeStepLevel - oldStatus, j3, j4);
                    this.mDailyDischargeStepTracker.addLevelSteps(this.mLastDischargeStepLevel - oldStatus, j3, j4);
                    this.mLastDischargeStepLevel = oldStatus;
                    this.mMinDischargeStepLevel = oldStatus;
                    this.mInitStepMode = this.mCurStepMode;
                    this.mModStepMode = 0;
                }
            } else {
                if (oldStatus >= 90) {
                    changed |= setChargingLocked(z);
                    this.mLastChargeStepLevel = oldStatus;
                }
                if (!this.mCharging) {
                    if (this.mLastChargeStepLevel < oldStatus) {
                        changed |= setChargingLocked(z);
                        this.mLastChargeStepLevel = oldStatus;
                    }
                } else if (this.mLastChargeStepLevel > oldStatus) {
                    changed |= setChargingLocked(false);
                    this.mLastChargeStepLevel = oldStatus;
                }
                if (this.mLastChargeStepLevel != oldStatus && this.mMaxChargeStepLevel < oldStatus) {
                    long j5 = modeBits;
                    long j6 = elapsedRealtime;
                    this.mChargeStepTracker.addLevelSteps(oldStatus - this.mLastChargeStepLevel, j5, j6);
                    this.mDailyChargeStepTracker.addLevelSteps(oldStatus - this.mLastChargeStepLevel, j5, j6);
                    this.mLastChargeStepLevel = oldStatus;
                    this.mMaxChargeStepLevel = oldStatus;
                    this.mInitStepMode = this.mCurStepMode;
                    this.mModStepMode = 0;
                }
            }
            if (changed) {
                addHistoryRecordLocked(elapsedRealtime, uptime);
            } else {
                long j7 = uptime;
            }
        }
        if (!onBattery2 && (i2 == 5 || i2 == 1)) {
            this.mRecordingHistory = false;
        }
        if (this.mMinLearnedBatteryCapacity == -1) {
            i = chargeFullUAh;
            this.mMinLearnedBatteryCapacity = i;
        } else {
            i = chargeFullUAh;
            Math.min(this.mMinLearnedBatteryCapacity, i);
        }
        this.mMaxLearnedBatteryCapacity = Math.max(this.mMaxLearnedBatteryCapacity, i);
    }

    public static boolean isOnBattery(int plugType, int status) {
        return plugType == 0 && status != 1;
    }

    private void reportChangesToStatsLog(BatteryStats.HistoryItem recentPast, int status, int plugType, int level) {
        if (recentPast == null || recentPast.batteryStatus != status) {
            StatsLog.write(31, status);
        }
        if (recentPast == null || recentPast.batteryPlugType != plugType) {
            StatsLog.write(32, plugType);
        }
        if (recentPast == null || recentPast.batteryLevel != level) {
            StatsLog.write(30, level);
        }
    }

    public long getAwakeTimeBattery() {
        return computeBatteryUptime(getBatteryUptimeLocked(), 1);
    }

    public long getAwakeTimePlugged() {
        return (this.mClocks.uptimeMillis() * 1000) - getAwakeTimeBattery();
    }

    public long computeUptime(long curTime, int which) {
        switch (which) {
            case 0:
                return this.mUptime + (curTime - this.mUptimeStart);
            case 1:
                return curTime - this.mUptimeStart;
            case 2:
                return curTime - this.mOnBatteryTimeBase.getUptimeStart();
            default:
                return 0;
        }
    }

    public long computeRealtime(long curTime, int which) {
        switch (which) {
            case 0:
                return this.mRealtime + (curTime - this.mRealtimeStart);
            case 1:
                return curTime - this.mRealtimeStart;
            case 2:
                return curTime - this.mOnBatteryTimeBase.getRealtimeStart();
            default:
                return 0;
        }
    }

    public long computeBatteryUptime(long curTime, int which) {
        if (this.mOnBatteryTimeBase != null) {
            return this.mOnBatteryTimeBase.computeUptime(curTime, which);
        }
        return 0;
    }

    public long computeBatteryRealtime(long curTime, int which) {
        if (this.mOnBatteryTimeBase != null) {
            return this.mOnBatteryTimeBase.computeRealtime(curTime, which);
        }
        return 0;
    }

    public long computeBatteryScreenOffUptime(long curTime, int which) {
        if (this.mOnBatteryScreenOffTimeBase != null) {
            return this.mOnBatteryScreenOffTimeBase.computeUptime(curTime, which);
        }
        return 0;
    }

    public long computeBatteryScreenOffRealtime(long curTime, int which) {
        if (this.mOnBatteryScreenOffTimeBase != null) {
            return this.mOnBatteryScreenOffTimeBase.computeRealtime(curTime, which);
        }
        return 0;
    }

    private long computeTimePerLevel(long[] steps, int numSteps) {
        if (numSteps <= 0 || steps == null) {
            return -1;
        }
        if (steps == null || numSteps <= steps.length) {
            long total = 0;
            for (int i = 0; i < numSteps; i++) {
                total += steps[i] & 1099511627775L;
            }
            return total / ((long) numSteps);
        }
        Slog.wtf(TAG, "numSteps > steps.length, numSteps = " + numSteps + ",steps.length = " + steps.length);
        return -1;
    }

    public long computeBatteryTimeRemaining(long curTime) {
        if (!this.mOnBattery || this.mDischargeStepTracker.mNumStepDurations < 1) {
            return -1;
        }
        long msPerLevel = this.mDischargeStepTracker.computeTimePerLevel();
        if (msPerLevel <= 0) {
            return -1;
        }
        return ((long) this.mCurrentBatteryLevel) * msPerLevel * 1000;
    }

    public BatteryStats.LevelStepTracker getDischargeLevelStepTracker() {
        return this.mDischargeStepTracker;
    }

    public BatteryStats.LevelStepTracker getDailyDischargeLevelStepTracker() {
        return this.mDailyDischargeStepTracker;
    }

    public long computeChargeTimeRemaining(long curTime) {
        if (this.mOnBattery || this.mChargeStepTracker.mNumStepDurations < 1) {
            return -1;
        }
        long msPerLevel = this.mChargeStepTracker.computeTimePerLevel();
        if (msPerLevel <= 0) {
            return -1;
        }
        return ((long) (100 - this.mCurrentBatteryLevel)) * msPerLevel * 1000;
    }

    public CellularBatteryStats getCellularBatteryStats() {
        long[] timeInRatMs;
        CellularBatteryStats s = new CellularBatteryStats();
        int i = 0;
        long rawRealTime = SystemClock.elapsedRealtime() * 1000;
        BatteryStats.ControllerActivityCounter counter = getModemControllerActivity();
        long sleepTimeMs = counter.getSleepTimeCounter().getCountLocked(0);
        long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(0);
        long rxTimeMs = counter.getRxTimeCounter().getCountLocked(0);
        long energyConsumedMaMs = counter.getPowerCounter().getCountLocked(0);
        long[] timeInRatMs2 = new long[21];
        int i2 = 0;
        while (true) {
            int which = i;
            int which2 = i2;
            if (which2 >= timeInRatMs2.length) {
                break;
            }
            timeInRatMs2[which2] = getPhoneDataConnectionTime(which2, rawRealTime, 0) / 1000;
            i2 = which2 + 1;
            i = which;
        }
        long[] timeInRxSignalStrengthLevelMs = new long[5];
        int i3 = 0;
        while (true) {
            timeInRatMs = timeInRatMs2;
            int i4 = i3;
            if (i4 >= timeInRxSignalStrengthLevelMs.length) {
                break;
            }
            timeInRxSignalStrengthLevelMs[i4] = getPhoneSignalStrengthTime(i4, rawRealTime, 0) / 1000;
            i3 = i4 + 1;
            timeInRatMs2 = timeInRatMs;
        }
        long[] txTimeMs = new long[Math.min(5, counter.getTxTimeCounters().length)];
        long totalTxTimeMs = 0;
        int i5 = 0;
        while (true) {
            long[] timeInRxSignalStrengthLevelMs2 = timeInRxSignalStrengthLevelMs;
            if (i5 < txTimeMs.length) {
                txTimeMs[i5] = counter.getTxTimeCounters()[i5].getCountLocked(0);
                totalTxTimeMs += txTimeMs[i5];
                i5++;
                timeInRxSignalStrengthLevelMs = timeInRxSignalStrengthLevelMs2;
                counter = counter;
            } else {
                s.setLoggingDurationMs(computeBatteryRealtime(rawRealTime, 0) / 1000);
                s.setKernelActiveTimeMs(getMobileRadioActiveTime(rawRealTime, 0) / 1000);
                long j = rawRealTime;
                s.setNumPacketsTx(getNetworkActivityPackets(1, 0));
                s.setNumBytesTx(getNetworkActivityBytes(1, 0));
                s.setNumPacketsRx(getNetworkActivityPackets(0, 0));
                s.setNumBytesRx(getNetworkActivityBytes(0, 0));
                s.setSleepTimeMs(sleepTimeMs);
                s.setIdleTimeMs(idleTimeMs);
                s.setRxTimeMs(rxTimeMs);
                s.setEnergyConsumedMaMs(energyConsumedMaMs);
                s.setTimeInRatMs(timeInRatMs);
                s.setTimeInRxSignalStrengthLevelMs(timeInRxSignalStrengthLevelMs2);
                s.setTxTimeMs(txTimeMs);
                return s;
            }
        }
    }

    public WifiBatteryStats getWifiBatteryStats() {
        WifiBatteryStats s = new WifiBatteryStats();
        int which = 0;
        long rawRealTime = SystemClock.elapsedRealtime() * 1000;
        BatteryStats.ControllerActivityCounter counter = getWifiControllerActivity();
        long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(0);
        long scanTimeMs = counter.getScanTimeCounter().getCountLocked(0);
        long rxTimeMs = counter.getRxTimeCounter().getCountLocked(0);
        long txTimeMs = counter.getTxTimeCounters()[0].getCountLocked(0);
        long scanTimeMs2 = scanTimeMs;
        long totalControllerActivityTimeMs = computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, 0) / 1000;
        long idleTimeMs2 = idleTimeMs;
        long sleepTimeMs = totalControllerActivityTimeMs - ((idleTimeMs + rxTimeMs) + txTimeMs);
        BatteryStats.ControllerActivityCounter controllerActivityCounter = counter;
        long energyConsumedMaMs = counter.getPowerCounter().getCountLocked(0);
        long j = totalControllerActivityTimeMs;
        long numAppScanRequest = 0;
        int i = 0;
        while (true) {
            int which2 = which;
            if (i >= this.mUidStats.size()) {
                break;
            }
            numAppScanRequest += (long) this.mUidStats.valueAt(i).mWifiScanTimer.getCountLocked(0);
            i++;
            which = which2;
            energyConsumedMaMs = energyConsumedMaMs;
        }
        long energyConsumedMaMs2 = energyConsumedMaMs;
        long[] timeInStateMs = new long[8];
        for (int i2 = 0; i2 < 8; i2++) {
            timeInStateMs[i2] = getWifiStateTime(i2, rawRealTime, 0) / 1000;
        }
        long[] timeInSupplStateMs = new long[13];
        int i3 = 0;
        for (int i4 = 13; i3 < i4; i4 = 13) {
            timeInSupplStateMs[i3] = getWifiSupplStateTime(i3, rawRealTime, 0) / 1000;
            i3++;
        }
        int i5 = 5;
        long[] timeSignalStrengthTimeMs = new long[5];
        int i6 = 0;
        while (true) {
            long[] timeInSupplStateMs2 = timeInSupplStateMs;
            int i7 = i6;
            if (i7 < i5) {
                timeSignalStrengthTimeMs[i7] = getWifiSignalStrengthTime(i7, rawRealTime, 0) / 1000;
                i6 = i7 + 1;
                timeInSupplStateMs = timeInSupplStateMs2;
                i5 = 5;
            } else {
                s.setLoggingDurationMs(computeBatteryRealtime(rawRealTime, 0) / 1000);
                long rawRealTime2 = rawRealTime;
                long j2 = rawRealTime2;
                s.setKernelActiveTimeMs(getWifiActiveTime(rawRealTime2, 0) / 1000);
                s.setNumPacketsTx(getNetworkActivityPackets(3, 0));
                s.setNumBytesTx(getNetworkActivityBytes(3, 0));
                s.setNumPacketsRx(getNetworkActivityPackets(2, 0));
                s.setNumBytesRx(getNetworkActivityBytes(2, 0));
                s.setSleepTimeMs(sleepTimeMs);
                long idleTimeMs3 = idleTimeMs2;
                s.setIdleTimeMs(idleTimeMs3);
                s.setRxTimeMs(rxTimeMs);
                s.setTxTimeMs(txTimeMs);
                s.setScanTimeMs(scanTimeMs2);
                long j3 = idleTimeMs3;
                s.setEnergyConsumedMaMs(energyConsumedMaMs2);
                s.setNumAppScanRequest(numAppScanRequest);
                s.setTimeInStateMs(timeInStateMs);
                s.setTimeInSupplicantStateMs(timeInSupplStateMs2);
                s.setTimeInRxSignalStrengthLevelMs(timeSignalStrengthTimeMs);
                return s;
            }
        }
    }

    public GpsBatteryStats getGpsBatteryStats() {
        GpsBatteryStats s = new GpsBatteryStats();
        long rawRealTime = SystemClock.elapsedRealtime() * 1000;
        s.setLoggingDurationMs(computeBatteryRealtime(rawRealTime, 0) / 1000);
        s.setEnergyConsumedMaMs(getGpsBatteryDrainMaMs());
        long[] time = new long[2];
        for (int i = 0; i < time.length; i++) {
            time[i] = getGpsSignalQualityTime(i, rawRealTime, 0) / 1000;
        }
        s.setTimeInGpsSignalQualityLevel(time);
        return s;
    }

    public BatteryStats.LevelStepTracker getChargeLevelStepTracker() {
        return this.mChargeStepTracker;
    }

    public BatteryStats.LevelStepTracker getDailyChargeLevelStepTracker() {
        return this.mDailyChargeStepTracker;
    }

    public ArrayList<BatteryStats.PackageChange> getDailyPackageChanges() {
        return this.mDailyPackageChanges;
    }

    /* access modifiers changed from: protected */
    public long getBatteryUptimeLocked() {
        if (this.mOnBatteryTimeBase != null) {
            return this.mOnBatteryTimeBase.getUptime(this.mClocks.uptimeMillis() * 1000);
        }
        return 0;
    }

    public long getBatteryUptime(long curTime) {
        if (this.mOnBatteryTimeBase != null) {
            return this.mOnBatteryTimeBase.getUptime(curTime);
        }
        return 0;
    }

    public long getBatteryRealtime(long curTime) {
        if (this.mOnBatteryTimeBase != null) {
            return this.mOnBatteryTimeBase.getRealtime(curTime);
        }
        return 0;
    }

    public int getDischargeStartLevel() {
        int dischargeStartLevelLocked;
        synchronized (this) {
            dischargeStartLevelLocked = getDischargeStartLevelLocked();
        }
        return dischargeStartLevelLocked;
    }

    public int getDischargeStartLevelLocked() {
        return this.mDischargeUnplugLevel;
    }

    public int getDischargeCurrentLevel() {
        int dischargeCurrentLevelLocked;
        synchronized (this) {
            dischargeCurrentLevelLocked = getDischargeCurrentLevelLocked();
        }
        return dischargeCurrentLevelLocked;
    }

    public int getDischargeCurrentLevelLocked() {
        return this.mDischargeCurrentLevel;
    }

    public int getLowDischargeAmountSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mLowDischargeAmountSinceCharge;
            if (this.mOnBattery && this.mDischargeCurrentLevel < this.mDischargeUnplugLevel) {
                val += (this.mDischargeUnplugLevel - this.mDischargeCurrentLevel) - 1;
            }
        }
        return val;
    }

    public int getHighDischargeAmountSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mHighDischargeAmountSinceCharge;
            if (this.mOnBattery && this.mDischargeCurrentLevel < this.mDischargeUnplugLevel) {
                val += this.mDischargeUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmount(int which) {
        int dischargeAmount;
        if (which == 0) {
            dischargeAmount = getHighDischargeAmountSinceCharge();
        } else {
            dischargeAmount = getDischargeStartLevel() - getDischargeCurrentLevel();
        }
        if (dischargeAmount < 0) {
            return 0;
        }
        return dischargeAmount;
    }

    public int getDischargeAmountScreenOn() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOn;
            if (this.mOnBattery && isScreenOn(this.mScreenState) && this.mDischargeCurrentLevel < this.mDischargeScreenOnUnplugLevel) {
                val += this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOnSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOnSinceCharge;
            if (this.mOnBattery && isScreenOn(this.mScreenState) && this.mDischargeCurrentLevel < this.mDischargeScreenOnUnplugLevel) {
                val += this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOff() {
        int dischargeAmountScreenDoze;
        synchronized (this) {
            int val = this.mDischargeAmountScreenOff;
            if (this.mOnBattery && isScreenOff(this.mScreenState) && this.mDischargeCurrentLevel < this.mDischargeScreenOffUnplugLevel) {
                val += this.mDischargeScreenOffUnplugLevel - this.mDischargeCurrentLevel;
            }
            dischargeAmountScreenDoze = getDischargeAmountScreenDoze() + val;
        }
        return dischargeAmountScreenDoze;
    }

    public int getDischargeAmountScreenOffSinceCharge() {
        int dischargeAmountScreenDozeSinceCharge;
        synchronized (this) {
            int val = this.mDischargeAmountScreenOffSinceCharge;
            if (this.mOnBattery && isScreenOff(this.mScreenState) && this.mDischargeCurrentLevel < this.mDischargeScreenOffUnplugLevel) {
                val += this.mDischargeScreenOffUnplugLevel - this.mDischargeCurrentLevel;
            }
            dischargeAmountScreenDozeSinceCharge = getDischargeAmountScreenDozeSinceCharge() + val;
        }
        return dischargeAmountScreenDozeSinceCharge;
    }

    public int getDischargeAmountScreenDoze() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenDoze;
            if (this.mOnBattery && isScreenDoze(this.mScreenState) && this.mDischargeCurrentLevel < this.mDischargeScreenDozeUnplugLevel) {
                val += this.mDischargeScreenDozeUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenDozeSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenDozeSinceCharge;
            if (this.mOnBattery && isScreenDoze(this.mScreenState) && this.mDischargeCurrentLevel < this.mDischargeScreenDozeUnplugLevel) {
                val += this.mDischargeScreenDozeUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public Uid getUidStatsLocked(int uid) {
        Uid u = this.mUidStats.get(uid);
        if (u != null) {
            return u;
        }
        Uid u2 = new Uid(this, uid);
        this.mUidStats.put(uid, u2);
        return u2;
    }

    public Uid getAvailableUidStatsLocked(int uid) {
        return this.mUidStats.get(uid);
    }

    public void onCleanupUserLocked(int userId) {
        int firstUidForUser = UserHandle.getUid(userId, 0);
        int lastUidForUser = UserHandle.getUid(userId, 99999);
        Queue<UidToRemove> queue = this.mPendingRemovedUids;
        UidToRemove uidToRemove = new UidToRemove(firstUidForUser, lastUidForUser, this.mClocks.elapsedRealtime());
        queue.add(uidToRemove);
    }

    public void onUserRemovedLocked(int userId) {
        int firstUidForUser = UserHandle.getUid(userId, 0);
        int lastUidForUser = UserHandle.getUid(userId, 99999);
        this.mUidStats.put(firstUidForUser, null);
        this.mUidStats.put(lastUidForUser, null);
        int firstIndex = this.mUidStats.indexOfKey(firstUidForUser);
        this.mUidStats.removeAtRange(firstIndex, (this.mUidStats.indexOfKey(lastUidForUser) - firstIndex) + 1);
    }

    public void removeUidStatsLocked(int uid) {
        this.mUidStats.remove(uid);
        this.mPendingRemovedUids.add(new UidToRemove(this, uid, this.mClocks.elapsedRealtime()));
    }

    public Uid.Proc getProcessStatsLocked(int uid, String name) {
        return getUidStatsLocked(mapUid(uid)).getProcessStatsLocked(name);
    }

    public Uid.Pkg getPackageStatsLocked(int uid, String pkg) {
        return getUidStatsLocked(mapUid(uid)).getPackageStatsLocked(pkg);
    }

    public Uid.Pkg.Serv getServiceStatsLocked(int uid, String pkg, String name) {
        return getUidStatsLocked(mapUid(uid)).getServiceStatsLocked(pkg, name);
    }

    public void shutdownLocked() {
        recordShutdownLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis());
        writeSyncLocked();
        this.mShuttingDown = true;
    }

    public boolean trackPerProcStateCpuTimes() {
        return this.mConstants.TRACK_CPU_TIMES_BY_PROC_STATE && this.mPerProcStateCpuTimesAvailable;
    }

    public void systemServicesReady(Context context) {
        this.mConstants.startObserving(context.getContentResolver());
        registerUsbStateReceiver(context);
    }

    public long getExternalStatsCollectionRateLimitMs() {
        long j;
        synchronized (this) {
            j = this.mConstants.EXTERNAL_STATS_COLLECTION_RATE_LIMIT_MS;
        }
        return j;
    }

    @GuardedBy("this")
    public void dumpConstantsLocked(PrintWriter pw) {
        this.mConstants.dumpLocked(pw);
    }

    @GuardedBy("this")
    public void dumpCpuStatsLocked(PrintWriter pw) {
        int size = this.mUidStats.size();
        pw.println("Per UID CPU user & system time in ms:");
        for (int i = 0; i < size; i++) {
            int u = this.mUidStats.keyAt(i);
            Uid uid = this.mUidStats.get(u);
            pw.print("  ");
            pw.print(u);
            pw.print(": ");
            pw.print(uid.getUserCpuTimeUs(0) / 1000);
            pw.print(" ");
            pw.println(uid.getSystemCpuTimeUs(0) / 1000);
        }
        pw.println("Per UID CPU active time in ms:");
        for (int i2 = 0; i2 < size; i2++) {
            int u2 = this.mUidStats.keyAt(i2);
            Uid uid2 = this.mUidStats.get(u2);
            if (uid2.getCpuActiveTime() > 0) {
                pw.print("  ");
                pw.print(u2);
                pw.print(": ");
                pw.println(uid2.getCpuActiveTime());
            }
        }
        pw.println("Per UID CPU cluster time in ms:");
        for (int i3 = 0; i3 < size; i3++) {
            int u3 = this.mUidStats.keyAt(i3);
            long[] times = this.mUidStats.get(u3).getCpuClusterTimes();
            if (times != null) {
                pw.print("  ");
                pw.print(u3);
                pw.print(": ");
                pw.println(Arrays.toString(times));
            }
        }
        pw.println("Per UID CPU frequency time in ms:");
        for (int i4 = 0; i4 < size; i4++) {
            int u4 = this.mUidStats.keyAt(i4);
            long[] times2 = this.mUidStats.get(u4).getCpuFreqTimes(0);
            if (times2 != null) {
                pw.print("  ");
                pw.print(u4);
                pw.print(": ");
                pw.println(Arrays.toString(times2));
            }
        }
    }

    public void writeAsyncLocked() {
        writeLocked(false);
    }

    public void writeSyncLocked() {
        writeLocked(true);
    }

    /* access modifiers changed from: package-private */
    public void writeLocked(boolean sync) {
        if (this.mFile == null) {
            Slog.w("BatteryStats", "writeLocked: no file associated with this instance");
        } else if (!this.mShuttingDown) {
            Parcel out = Parcel.obtain();
            writeSummaryToParcel(out, true);
            this.mLastWriteTime = this.mClocks.elapsedRealtime();
            if (this.mPendingWrite != null) {
                this.mPendingWrite.recycle();
            }
            this.mPendingWrite = out;
            if (sync) {
                commitPendingDataToDisk();
            } else {
                BackgroundThread.getHandler().post(new Runnable() {
                    public void run() {
                        BatteryStatsImpl.this.commitPendingDataToDisk();
                    }
                });
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x003f, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0041, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        android.util.Slog.w("BatteryStats", "Error writing battery statistics", r1);
        r7.mFile.rollback();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        r0.recycle();
        r7.mWriteLock.unlock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000b, code lost:
        r7.mWriteLock.lock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
        r1 = android.os.SystemClock.uptimeMillis();
        r3 = new java.io.FileOutputStream(r7.mFile.chooseForWrite());
        r3.write(r0.marshall());
        r3.flush();
        android.os.FileUtils.sync(r3);
        r3.close();
        r7.mFile.commit();
        com.android.internal.logging.EventLogTags.writeCommitSysConfigFile("batterystats", android.os.SystemClock.uptimeMillis() - r1);
     */
    public void commitPendingDataToDisk() {
        Parcel next;
        synchronized (this) {
            next = this.mPendingWrite;
            this.mPendingWrite = null;
            if (next == null) {
                return;
            }
        }
        next.recycle();
        this.mWriteLock.unlock();
    }

    public void readLocked() {
        if (this.mDailyFile != null) {
            readDailyStatsLocked();
        }
        if (this.mFile == null) {
            Slog.w("BatteryStats", "readLocked: no file associated with this instance");
            return;
        }
        this.mUidStats.clear();
        try {
            File file = this.mFile.chooseForRead();
            if (file.exists()) {
                FileInputStream stream = new FileInputStream(file);
                byte[] raw = BatteryStatsHelper.readFully(stream);
                Parcel in = Parcel.obtain();
                in.unmarshall(raw, 0, raw.length);
                in.setDataPosition(0);
                stream.close();
                readSummaryFromParcel(in);
                this.mEndPlatformVersion = Build.ID;
                if (this.mHistoryBuffer.dataPosition() > 0) {
                    this.mRecordingHistory = true;
                    long elapsedRealtime = this.mClocks.elapsedRealtime();
                    long uptime = this.mClocks.uptimeMillis();
                    addHistoryBufferLocked(elapsedRealtime, (byte) 4, this.mHistoryCur);
                    startRecordingHistory(elapsedRealtime, uptime, false);
                }
                recordDailyStatsIfNeededLocked(false);
            }
        } catch (Exception e) {
            Slog.e("BatteryStats", "Error reading battery statistics", e);
            resetAllStatsLocked();
        }
    }

    public int describeContents() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void readHistory(Parcel in, boolean andOldHistory) throws ParcelFormatException {
        long historyBaseTime = in.readLong();
        int i = 0;
        this.mHistoryBuffer.setDataSize(0);
        this.mHistoryBuffer.setDataPosition(0);
        this.mHistoryTagPool.clear();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        int numTags = in.readInt();
        while (i < numTags) {
            int idx = in.readInt();
            String str = in.readString();
            if (str != null) {
                int uid = in.readInt();
                BatteryStats.HistoryTag tag = new BatteryStats.HistoryTag();
                tag.string = str;
                tag.uid = uid;
                tag.poolIdx = idx;
                this.mHistoryTagPool.put(tag, Integer.valueOf(idx));
                if (idx >= this.mNextHistoryTagIdx) {
                    this.mNextHistoryTagIdx = idx + 1;
                }
                this.mNumHistoryTagChars += tag.string.length() + 1;
                i++;
            } else {
                throw new ParcelFormatException("null history tag string");
            }
        }
        int bufSize = in.readInt();
        int curPos = in.dataPosition();
        if (bufSize >= MAX_MAX_HISTORY_BUFFER * 3) {
            throw new ParcelFormatException("File corrupt: history data buffer too large " + bufSize);
        } else if ((bufSize & -4) == bufSize) {
            this.mHistoryBuffer.appendFrom(in, curPos, bufSize);
            in.setDataPosition(curPos + bufSize);
            if (andOldHistory) {
                readOldHistory(in);
            }
            this.mHistoryBaseTime = historyBaseTime;
            if (this.mHistoryBaseTime > 0) {
                this.mHistoryBaseTime = (this.mHistoryBaseTime - this.mClocks.elapsedRealtime()) + 1;
            }
        } else {
            throw new ParcelFormatException("File corrupt: history data buffer not aligned " + bufSize);
        }
    }

    /* access modifiers changed from: package-private */
    public void readOldHistory(Parcel in) {
    }

    /* access modifiers changed from: package-private */
    public void writeHistory(Parcel out, boolean inclData, boolean andOldHistory) {
        out.writeLong(this.mHistoryBaseTime + this.mLastHistoryElapsedRealtime);
        if (!inclData) {
            out.writeInt(0);
            out.writeInt(0);
            return;
        }
        out.writeInt(this.mHistoryTagPool.size());
        for (Map.Entry<BatteryStats.HistoryTag, Integer> ent : this.mHistoryTagPool.entrySet()) {
            BatteryStats.HistoryTag tag = ent.getKey();
            out.writeInt(ent.getValue().intValue());
            out.writeString(tag.string);
            out.writeInt(tag.uid);
        }
        out.writeInt(this.mHistoryBuffer.dataSize());
        out.appendFrom(this.mHistoryBuffer, 0, this.mHistoryBuffer.dataSize());
        if (andOldHistory) {
            writeOldHistory(out);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeOldHistory(Parcel out) {
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: android.os.Parcel} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v1, resolved type: android.os.Parcel} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v11, resolved type: android.os.Parcel} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v77, resolved type: com.android.internal.os.BatteryStatsImpl$LongSamplingCounter[][]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v18, resolved type: android.os.Parcel} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v19, resolved type: android.os.Parcel} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void readSummaryFromParcel(Parcel in) throws ParcelFormatException {
        int NPKG;
        int NMS;
        int version;
        int NSORPMS;
        int NRPMS;
        int length;
        int NPKG2;
        int NMS2;
        int version2;
        Parcel parcel;
        int NSB;
        Parcel parcel2 = in;
        int version3 = in.readInt();
        if (version3 != 177) {
            Slog.w("BatteryStats", "readFromParcel: version got " + version3 + ", expected " + 177 + "; erasing old stats");
            return;
        }
        readHistory(parcel2, true);
        this.mStartCount = in.readInt();
        this.mUptime = in.readLong();
        this.mRealtime = in.readLong();
        this.mStartClockTime = in.readLong();
        this.mStartPlatformVersion = in.readString();
        this.mEndPlatformVersion = in.readString();
        this.mOnBatteryTimeBase.readSummaryFromParcel(parcel2);
        this.mOnBatteryScreenOffTimeBase.readSummaryFromParcel(parcel2);
        this.mDischargeUnplugLevel = in.readInt();
        this.mDischargePlugLevel = in.readInt();
        this.mDischargeCurrentLevel = in.readInt();
        this.mCurrentBatteryLevel = in.readInt();
        this.mEstimatedBatteryCapacity = in.readInt();
        this.mMinLearnedBatteryCapacity = in.readInt();
        this.mMaxLearnedBatteryCapacity = in.readInt();
        this.mLowDischargeAmountSinceCharge = in.readInt();
        this.mHighDischargeAmountSinceCharge = in.readInt();
        this.mDischargeAmountScreenOnSinceCharge = in.readInt();
        this.mDischargeAmountScreenOffSinceCharge = in.readInt();
        this.mDischargeAmountScreenDozeSinceCharge = in.readInt();
        this.mDischargeStepTracker.readFromParcel(parcel2);
        this.mChargeStepTracker.readFromParcel(parcel2);
        this.mDailyDischargeStepTracker.readFromParcel(parcel2);
        this.mDailyChargeStepTracker.readFromParcel(parcel2);
        this.mDischargeCounter.readSummaryFromParcelLocked(parcel2);
        this.mDischargeScreenOffCounter.readSummaryFromParcelLocked(parcel2);
        this.mDischargeScreenDozeCounter.readSummaryFromParcelLocked(parcel2);
        this.mDischargeLightDozeCounter.readSummaryFromParcelLocked(parcel2);
        this.mDischargeDeepDozeCounter.readSummaryFromParcelLocked(parcel2);
        int NPKG3 = in.readInt();
        Parcel parcel3 = null;
        boolean z = false;
        if (NPKG3 > 0) {
            this.mDailyPackageChanges = new ArrayList<>(NPKG3);
            while (NPKG3 > 0) {
                NPKG3--;
                BatteryStats.PackageChange pc = new BatteryStats.PackageChange();
                pc.mPackageName = in.readString();
                pc.mUpdate = in.readInt() != 0;
                pc.mVersionCode = in.readLong();
                this.mDailyPackageChanges.add(pc);
            }
        } else {
            this.mDailyPackageChanges = null;
        }
        this.mDailyStartTime = in.readLong();
        this.mNextMinDailyDeadline = in.readLong();
        this.mNextMaxDailyDeadline = in.readLong();
        this.mStartCount++;
        this.mScreenState = 0;
        this.mScreenOnTimer.readSummaryFromParcelLocked(parcel2);
        this.mScreenDozeTimer.readSummaryFromParcelLocked(parcel2);
        for (int i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i].readSummaryFromParcelLocked(parcel2);
        }
        this.mInteractive = false;
        this.mInteractiveTimer.readSummaryFromParcelLocked(parcel2);
        this.mPhoneOn = false;
        this.mPowerSaveModeEnabledTimer.readSummaryFromParcelLocked(parcel2);
        this.mLongestLightIdleTime = in.readLong();
        this.mLongestFullIdleTime = in.readLong();
        this.mDeviceIdleModeLightTimer.readSummaryFromParcelLocked(parcel2);
        this.mDeviceIdleModeFullTimer.readSummaryFromParcelLocked(parcel2);
        this.mDeviceLightIdlingTimer.readSummaryFromParcelLocked(parcel2);
        this.mDeviceIdlingTimer.readSummaryFromParcelLocked(parcel2);
        this.mPhoneOnTimer.readSummaryFromParcelLocked(parcel2);
        for (int i2 = 0; i2 < 5; i2++) {
            this.mPhoneSignalStrengthsTimer[i2].readSummaryFromParcelLocked(parcel2);
        }
        this.mPhoneSignalScanningTimer.readSummaryFromParcelLocked(parcel2);
        for (int i3 = 0; i3 < 21; i3++) {
            this.mPhoneDataConnectionsTimer[i3].readSummaryFromParcelLocked(parcel2);
        }
        for (int i4 = 0; i4 < 10; i4++) {
            this.mNetworkByteActivityCounters[i4].readSummaryFromParcelLocked(parcel2);
            this.mNetworkPacketActivityCounters[i4].readSummaryFromParcelLocked(parcel2);
        }
        this.mMobileRadioPowerState = 1;
        this.mMobileRadioActiveTimer.readSummaryFromParcelLocked(parcel2);
        this.mMobileRadioActivePerAppTimer.readSummaryFromParcelLocked(parcel2);
        this.mMobileRadioActiveAdjustedTime.readSummaryFromParcelLocked(parcel2);
        this.mMobileRadioActiveUnknownTime.readSummaryFromParcelLocked(parcel2);
        this.mMobileRadioActiveUnknownCount.readSummaryFromParcelLocked(parcel2);
        this.mWifiMulticastWakelockTimer.readSummaryFromParcelLocked(parcel2);
        this.mWifiRadioPowerState = 1;
        this.mWifiOn = false;
        this.mWifiOnTimer.readSummaryFromParcelLocked(parcel2);
        this.mGlobalWifiRunning = false;
        this.mGlobalWifiRunningTimer.readSummaryFromParcelLocked(parcel2);
        for (int i5 = 0; i5 < 8; i5++) {
            this.mWifiStateTimer[i5].readSummaryFromParcelLocked(parcel2);
        }
        for (int i6 = 0; i6 < 13; i6++) {
            this.mWifiSupplStateTimer[i6].readSummaryFromParcelLocked(parcel2);
        }
        for (int i7 = 0; i7 < 5; i7++) {
            this.mWifiSignalStrengthsTimer[i7].readSummaryFromParcelLocked(parcel2);
        }
        this.mWifiActiveTimer.readSummaryFromParcelLocked(parcel2);
        this.mWifiActivity.readSummaryFromParcel(parcel2);
        for (int i8 = 0; i8 < 2; i8++) {
            this.mGpsSignalQualityTimer[i8].readSummaryFromParcelLocked(parcel2);
        }
        this.mBluetoothActivity.readSummaryFromParcel(parcel2);
        this.mModemActivity.readSummaryFromParcel(parcel2);
        this.mHasWifiReporting = in.readInt() != 0;
        this.mHasBluetoothReporting = in.readInt() != 0;
        this.mHasModemReporting = in.readInt() != 0;
        int readInt = in.readInt();
        this.mLoadedNumConnectivityChange = readInt;
        this.mNumConnectivityChange = readInt;
        this.mFlashlightOnNesting = 0;
        this.mFlashlightOnTimer.readSummaryFromParcelLocked(parcel2);
        this.mCameraOnNesting = 0;
        this.mCameraOnTimer.readSummaryFromParcelLocked(parcel2);
        this.mBluetoothScanNesting = 0;
        this.mBluetoothScanTimer.readSummaryFromParcelLocked(parcel2);
        this.mIsCellularTxPowerHigh = false;
        int NRPMS2 = in.readInt();
        if (NRPMS2 <= 10000) {
            for (int irpm = 0; irpm < NRPMS2; irpm++) {
                if (in.readInt() != 0) {
                    getRpmTimerLocked(in.readString()).readSummaryFromParcelLocked(parcel2);
                }
            }
            int NSORPMS2 = in.readInt();
            if (NSORPMS2 <= 10000) {
                for (int irpm2 = 0; irpm2 < NSORPMS2; irpm2++) {
                    if (in.readInt() != 0) {
                        getScreenOffRpmTimerLocked(in.readString()).readSummaryFromParcelLocked(parcel2);
                    }
                }
                int NKW = in.readInt();
                if (NKW <= 10000) {
                    for (int ikw = 0; ikw < NKW; ikw++) {
                        if (in.readInt() != 0) {
                            getKernelWakelockTimerLocked(in.readString()).readSummaryFromParcelLocked(parcel2);
                        }
                    }
                    int NWR = in.readInt();
                    if (NWR <= 10000) {
                        for (int iwr = 0; iwr < NWR; iwr++) {
                            if (in.readInt() != 0) {
                                getWakeupReasonTimerLocked(in.readString()).readSummaryFromParcelLocked(parcel2);
                            }
                        }
                        int NMS3 = in.readInt();
                        for (int ims = 0; ims < NMS3; ims++) {
                            if (in.readInt() != 0) {
                                getKernelMemoryTimerLocked(in.readLong()).readSummaryFromParcelLocked(parcel2);
                            }
                        }
                        int NU = in.readInt();
                        if (NU <= 10000) {
                            int iu = 0;
                            while (iu < NU) {
                                int uid = in.readInt();
                                Uid u = new Uid(this, uid);
                                this.mUidStats.put(uid, u);
                                u.mOnBatteryBackgroundTimeBase.readSummaryFromParcel(parcel2);
                                u.mOnBatteryScreenOffBackgroundTimeBase.readSummaryFromParcel(parcel2);
                                u.mWifiRunning = z;
                                if (in.readInt() != 0) {
                                    u.mWifiRunningTimer.readSummaryFromParcelLocked(parcel2);
                                }
                                u.mFullWifiLockOut = z;
                                if (in.readInt() != 0) {
                                    u.mFullWifiLockTimer.readSummaryFromParcelLocked(parcel2);
                                }
                                u.mWifiScanStarted = z;
                                if (in.readInt() != 0) {
                                    u.mWifiScanTimer.readSummaryFromParcelLocked(parcel2);
                                }
                                u.mWifiBatchedScanBinStarted = -1;
                                for (int i9 = z; i9 < 5; i9++) {
                                    if (in.readInt() != 0) {
                                        u.makeWifiBatchedScanBin(i9, parcel3);
                                        u.mWifiBatchedScanTimer[i9].readSummaryFromParcelLocked(parcel2);
                                    }
                                }
                                u.mWifiMulticastEnabled = false;
                                if (in.readInt() != 0) {
                                    u.mWifiMulticastTimer.readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createAudioTurnedOnTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createVideoTurnedOnTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createFlashlightTurnedOnTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createCameraTurnedOnTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createForegroundActivityTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createForegroundServiceTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createAggregatedPartialWakelockTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createBluetoothScanTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createBluetoothUnoptimizedScanTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createBluetoothScanResultCounterLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    u.createBluetoothScanResultBgCounterLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                u.mProcessState = 19;
                                for (int i10 = 0; i10 < 7; i10++) {
                                    if (in.readInt() != 0) {
                                        u.makeProcessState(i10, parcel3);
                                        u.mProcessStateTimer[i10].readSummaryFromParcelLocked(parcel2);
                                    }
                                }
                                if (in.readInt() != 0) {
                                    u.createVibratorOnTimerLocked().readSummaryFromParcelLocked(parcel2);
                                }
                                if (in.readInt() != 0) {
                                    if (u.mUserActivityCounters == null) {
                                        u.initUserActivityLocked();
                                    }
                                    for (int i11 = 0; i11 < 4; i11++) {
                                        u.mUserActivityCounters[i11].readSummaryFromParcelLocked(parcel2);
                                    }
                                }
                                if (in.readInt() != 0) {
                                    if (u.mNetworkByteActivityCounters == null) {
                                        u.initNetworkActivityLocked();
                                    }
                                    for (int i12 = 0; i12 < 10; i12++) {
                                        u.mNetworkByteActivityCounters[i12].readSummaryFromParcelLocked(parcel2);
                                        u.mNetworkPacketActivityCounters[i12].readSummaryFromParcelLocked(parcel2);
                                    }
                                    u.mMobileRadioActiveTime.readSummaryFromParcelLocked(parcel2);
                                    u.mMobileRadioActiveCount.readSummaryFromParcelLocked(parcel2);
                                }
                                u.mUserCpuTime.readSummaryFromParcelLocked(parcel2);
                                u.mSystemCpuTime.readSummaryFromParcelLocked(parcel2);
                                if (in.readInt() != 0) {
                                    int numClusters = in.readInt();
                                    if (this.mPowerProfile == null || this.mPowerProfile.getNumCpuClusters() == numClusters) {
                                        u.mCpuClusterSpeedTimesUs = new LongSamplingCounter[numClusters][];
                                        int cluster = 0;
                                        while (cluster < numClusters) {
                                            if (in.readInt() != 0) {
                                                int NSB2 = in.readInt();
                                                version2 = version3;
                                                if (this.mPowerProfile == null) {
                                                    NMS2 = NMS3;
                                                    NPKG2 = NPKG3;
                                                } else if (this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster) == NSB2) {
                                                    NMS2 = NMS3;
                                                    NPKG2 = NPKG3;
                                                } else {
                                                    int i13 = NMS3;
                                                    StringBuilder sb = new StringBuilder();
                                                    int i14 = NPKG3;
                                                    sb.append("File corrupt: too many speed bins ");
                                                    sb.append(NSB2);
                                                    throw new ParcelFormatException(sb.toString());
                                                }
                                                u.mCpuClusterSpeedTimesUs[cluster] = new LongSamplingCounter[NSB2];
                                                int speed = 0;
                                                while (speed < NSB2) {
                                                    if (in.readInt() != 0) {
                                                        NSB = NSB2;
                                                        u.mCpuClusterSpeedTimesUs[cluster][speed] = new LongSamplingCounter(this.mOnBatteryTimeBase);
                                                        u.mCpuClusterSpeedTimesUs[cluster][speed].readSummaryFromParcelLocked(parcel2);
                                                    } else {
                                                        NSB = NSB2;
                                                    }
                                                    speed++;
                                                    NSB2 = NSB;
                                                }
                                                parcel = null;
                                            } else {
                                                version2 = version3;
                                                NMS2 = NMS3;
                                                NPKG2 = NPKG3;
                                                parcel = null;
                                                u.mCpuClusterSpeedTimesUs[cluster] = null;
                                            }
                                            cluster++;
                                            parcel3 = parcel;
                                            version3 = version2;
                                            NMS3 = NMS2;
                                            NPKG3 = NPKG2;
                                        }
                                        version = version3;
                                        NMS = NMS3;
                                        NPKG = NPKG3;
                                        Parcel parcel4 = parcel3;
                                    } else {
                                        throw new ParcelFormatException("Incompatible cpu cluster arrangement");
                                    }
                                } else {
                                    version = version3;
                                    NMS = NMS3;
                                    NPKG = NPKG3;
                                    u.mCpuClusterSpeedTimesUs = parcel3;
                                }
                                u.mCpuFreqTimeMs = LongSamplingCounterArray.readSummaryFromParcelLocked(parcel2, this.mOnBatteryTimeBase);
                                u.mScreenOffCpuFreqTimeMs = LongSamplingCounterArray.readSummaryFromParcelLocked(parcel2, this.mOnBatteryScreenOffTimeBase);
                                u.mCpuActiveTimeMs.readSummaryFromParcelLocked(parcel2);
                                u.mCpuClusterTimesMs.readSummaryFromParcelLocked(parcel2);
                                int length2 = in.readInt();
                                if (length2 == 7) {
                                    u.mProcStateTimeMs = new LongSamplingCounterArray[length2];
                                    for (int procState = 0; procState < length2; procState++) {
                                        u.mProcStateTimeMs[procState] = LongSamplingCounterArray.readSummaryFromParcelLocked(parcel2, this.mOnBatteryTimeBase);
                                    }
                                } else {
                                    u.mProcStateTimeMs = null;
                                }
                                int length3 = in.readInt();
                                if (length3 == 7) {
                                    u.mProcStateScreenOffTimeMs = new LongSamplingCounterArray[length3];
                                    for (int procState2 = 0; procState2 < length3; procState2++) {
                                        u.mProcStateScreenOffTimeMs[procState2] = LongSamplingCounterArray.readSummaryFromParcelLocked(parcel2, this.mOnBatteryScreenOffTimeBase);
                                    }
                                } else {
                                    u.mProcStateScreenOffTimeMs = null;
                                }
                                if (in.readInt() != 0) {
                                    LongSamplingCounter unused = u.mMobileRadioApWakeupCount = new LongSamplingCounter(this.mOnBatteryTimeBase);
                                    u.mMobileRadioApWakeupCount.readSummaryFromParcelLocked(parcel2);
                                } else {
                                    LongSamplingCounter unused2 = u.mMobileRadioApWakeupCount = null;
                                }
                                if (in.readInt() != 0) {
                                    LongSamplingCounter unused3 = u.mWifiRadioApWakeupCount = new LongSamplingCounter(this.mOnBatteryTimeBase);
                                    u.mWifiRadioApWakeupCount.readSummaryFromParcelLocked(parcel2);
                                } else {
                                    LongSamplingCounter unused4 = u.mWifiRadioApWakeupCount = null;
                                }
                                int NW = in.readInt();
                                if (NW <= MAX_WAKELOCKS_PER_UID + 1) {
                                    for (int iw = 0; iw < NW; iw++) {
                                        u.readWakeSummaryFromParcelLocked(in.readString(), parcel2);
                                    }
                                    int NS = in.readInt();
                                    if (NS <= MAX_WAKELOCKS_PER_UID + 1) {
                                        for (int is = 0; is < NS; is++) {
                                            u.readSyncSummaryFromParcelLocked(in.readString(), parcel2);
                                        }
                                        int NJ = in.readInt();
                                        if (NJ <= MAX_WAKELOCKS_PER_UID + 1) {
                                            for (int ij = 0; ij < NJ; ij++) {
                                                u.readJobSummaryFromParcelLocked(in.readString(), parcel2);
                                            }
                                            u.readJobCompletionsFromParcelLocked(parcel2);
                                            u.mJobsDeferredEventCount.readSummaryFromParcelLocked(parcel2);
                                            u.mJobsDeferredCount.readSummaryFromParcelLocked(parcel2);
                                            u.mJobsFreshnessTimeMs.readSummaryFromParcelLocked(parcel2);
                                            int i15 = 0;
                                            while (i15 < JOB_FRESHNESS_BUCKETS.length) {
                                                if (in.readInt() != 0) {
                                                    length = length3;
                                                    NRPMS = NRPMS2;
                                                    u.mJobsFreshnessBuckets[i15] = new Counter(u.mBsi.mOnBatteryTimeBase);
                                                    u.mJobsFreshnessBuckets[i15].readSummaryFromParcelLocked(parcel2);
                                                } else {
                                                    length = length3;
                                                    NRPMS = NRPMS2;
                                                }
                                                i15++;
                                                length3 = length;
                                                NRPMS2 = NRPMS;
                                            }
                                            int NRPMS3 = NRPMS2;
                                            int length4 = in.readInt();
                                            if (length4 <= 1000) {
                                                int is2 = 0;
                                                while (is2 < length4) {
                                                    int seNumber = in.readInt();
                                                    if (in.readInt() != 0) {
                                                        NSORPMS = NSORPMS2;
                                                        u.getSensorTimerLocked(seNumber, true).readSummaryFromParcelLocked(parcel2);
                                                    } else {
                                                        NSORPMS = NSORPMS2;
                                                    }
                                                    is2++;
                                                    NSORPMS2 = NSORPMS;
                                                }
                                                int NSORPMS3 = NSORPMS2;
                                                int NP = in.readInt();
                                                if (NP <= 1000) {
                                                    int ip = 0;
                                                    while (ip < NP) {
                                                        Uid.Proc p = u.getProcessStatsLocked(in.readString());
                                                        long readLong = in.readLong();
                                                        p.mLoadedUserTime = readLong;
                                                        p.mUserTime = readLong;
                                                        long readLong2 = in.readLong();
                                                        p.mLoadedSystemTime = readLong2;
                                                        p.mSystemTime = readLong2;
                                                        long readLong3 = in.readLong();
                                                        p.mLoadedForegroundTime = readLong3;
                                                        p.mForegroundTime = readLong3;
                                                        int readInt2 = in.readInt();
                                                        p.mLoadedStarts = readInt2;
                                                        p.mStarts = readInt2;
                                                        int readInt3 = in.readInt();
                                                        p.mLoadedNumCrashes = readInt3;
                                                        p.mNumCrashes = readInt3;
                                                        int readInt4 = in.readInt();
                                                        p.mLoadedNumAnrs = readInt4;
                                                        p.mNumAnrs = readInt4;
                                                        p.readExcessivePowerFromParcelLocked(parcel2);
                                                        ip++;
                                                        NKW = NKW;
                                                    }
                                                    int NKW2 = NKW;
                                                    int NP2 = in.readInt();
                                                    if (NP2 <= 10000) {
                                                        int NS2 = NS;
                                                        int ip2 = 0;
                                                        while (ip2 < NP2) {
                                                            String pkgName = in.readString();
                                                            Uid.Pkg p2 = u.getPackageStatsLocked(pkgName);
                                                            int NWA = in.readInt();
                                                            if (NWA <= 1000) {
                                                                p2.mWakeupAlarms.clear();
                                                                int iwa = 0;
                                                                while (iwa < NWA) {
                                                                    int NS3 = NS2;
                                                                    String tag = in.readString();
                                                                    int NU2 = NU;
                                                                    Counter c = new Counter(this.mOnBatteryScreenOffTimeBase);
                                                                    c.readSummaryFromParcelLocked(parcel2);
                                                                    p2.mWakeupAlarms.put(tag, c);
                                                                    iwa++;
                                                                    NS2 = NS3;
                                                                    NWR = NWR;
                                                                    NU = NU2;
                                                                }
                                                                int NU3 = NU;
                                                                int NWR2 = NWR;
                                                                NS2 = in.readInt();
                                                                if (NS2 <= 1000) {
                                                                    int is3 = 0;
                                                                    while (is3 < NS2) {
                                                                        Uid.Pkg.Serv s = u.getServiceStatsLocked(pkgName, in.readString());
                                                                        String pkgName2 = pkgName;
                                                                        long readLong4 = in.readLong();
                                                                        s.mLoadedStartTime = readLong4;
                                                                        s.mStartTime = readLong4;
                                                                        int readInt5 = in.readInt();
                                                                        s.mLoadedStarts = readInt5;
                                                                        s.mStarts = readInt5;
                                                                        int readInt6 = in.readInt();
                                                                        s.mLoadedLaunches = readInt6;
                                                                        s.mLaunches = readInt6;
                                                                        is3++;
                                                                        pkgName = pkgName2;
                                                                        p2 = p2;
                                                                    }
                                                                    ip2++;
                                                                    NWR = NWR2;
                                                                    NU = NU3;
                                                                } else {
                                                                    String str = pkgName;
                                                                    Uid.Pkg pkg = p2;
                                                                    throw new ParcelFormatException("File corrupt: too many services " + NS2);
                                                                }
                                                            } else {
                                                                int i16 = NS2;
                                                                int i17 = NU;
                                                                String str2 = pkgName;
                                                                Uid.Pkg pkg2 = p2;
                                                                int i18 = NWR;
                                                                throw new ParcelFormatException("File corrupt: too many wakeup alarms " + NWA);
                                                            }
                                                        }
                                                        int i19 = NWR;
                                                        iu++;
                                                        version3 = version;
                                                        NMS3 = NMS;
                                                        NPKG3 = NPKG;
                                                        NRPMS2 = NRPMS3;
                                                        NSORPMS2 = NSORPMS3;
                                                        NKW = NKW2;
                                                        parcel3 = null;
                                                        z = false;
                                                    } else {
                                                        int i20 = NWR;
                                                        throw new ParcelFormatException("File corrupt: too many packages " + NP2);
                                                    }
                                                } else {
                                                    int i21 = NKW;
                                                    int i22 = NWR;
                                                    throw new ParcelFormatException("File corrupt: too many processes " + NP);
                                                }
                                            } else {
                                                int i23 = NSORPMS2;
                                                int i24 = NKW;
                                                int i25 = NWR;
                                                throw new ParcelFormatException("File corrupt: too many sensors " + length4);
                                            }
                                        } else {
                                            int i26 = NRPMS2;
                                            int i27 = NU;
                                            int i28 = NSORPMS2;
                                            int i29 = NKW;
                                            int i30 = NWR;
                                            throw new ParcelFormatException("File corrupt: too many job timers " + NJ);
                                        }
                                    } else {
                                        int i31 = NRPMS2;
                                        int i32 = NU;
                                        int i33 = NSORPMS2;
                                        int i34 = NKW;
                                        int i35 = NWR;
                                        throw new ParcelFormatException("File corrupt: too many syncs " + NS);
                                    }
                                } else {
                                    int i36 = NRPMS2;
                                    int i37 = NU;
                                    int i38 = NSORPMS2;
                                    int i39 = NKW;
                                    int i40 = NWR;
                                    Slog.i(TAG, "NW > " + (MAX_WAKELOCKS_PER_UID + 1) + ", uid: " + uid);
                                    throw new ParcelFormatException("File corrupt: too many wake locks " + NW);
                                }
                            }
                            int i41 = NMS3;
                            int i42 = NPKG3;
                            int i43 = NRPMS2;
                            int i44 = NU;
                            int i45 = NSORPMS2;
                            int i46 = NKW;
                            int i47 = NWR;
                            return;
                        }
                        int i48 = NMS3;
                        int i49 = NPKG3;
                        int i50 = NRPMS2;
                        int i51 = NSORPMS2;
                        int i52 = NKW;
                        int i53 = NWR;
                        throw new ParcelFormatException("File corrupt: too many uids " + NU);
                    }
                    int i54 = NPKG3;
                    int i55 = NRPMS2;
                    int i56 = NSORPMS2;
                    int i57 = NKW;
                    throw new ParcelFormatException("File corrupt: too many wakeup reasons " + NWR);
                }
                int i58 = NPKG3;
                int i59 = NRPMS2;
                int i60 = NSORPMS2;
                throw new ParcelFormatException("File corrupt: too many kernel wake locks " + NKW);
            }
            int i61 = NPKG3;
            int i62 = NRPMS2;
            throw new ParcelFormatException("File corrupt: too many screen-off rpm stats " + NSORPMS2);
        }
        int i63 = NPKG3;
        throw new ParcelFormatException("File corrupt: too many rpm stats " + NRPMS2);
    }

    public void writeSummaryToParcel(Parcel out, boolean inclHistory) {
        int i;
        int i2;
        int i3;
        int NP;
        BatteryStatsImpl batteryStatsImpl = this;
        Parcel parcel = out;
        pullPendingStateUpdatesLocked();
        long startClockTime = getStartClockTime();
        long NOW_SYS = batteryStatsImpl.mClocks.uptimeMillis() * 1000;
        long NOWREAL_SYS = batteryStatsImpl.mClocks.elapsedRealtime() * 1000;
        parcel.writeInt(177);
        batteryStatsImpl.writeHistory(parcel, inclHistory, true);
        parcel.writeInt(batteryStatsImpl.mStartCount);
        parcel.writeLong(batteryStatsImpl.computeUptime(NOW_SYS, 0));
        parcel.writeLong(batteryStatsImpl.computeRealtime(NOWREAL_SYS, 0));
        parcel.writeLong(startClockTime);
        parcel.writeString(batteryStatsImpl.mStartPlatformVersion);
        parcel.writeString(batteryStatsImpl.mEndPlatformVersion);
        Parcel parcel2 = parcel;
        long j = NOW_SYS;
        long j2 = NOWREAL_SYS;
        batteryStatsImpl.mOnBatteryTimeBase.writeSummaryToParcel(parcel2, j, j2);
        batteryStatsImpl.mOnBatteryScreenOffTimeBase.writeSummaryToParcel(parcel2, j, j2);
        parcel.writeInt(batteryStatsImpl.mDischargeUnplugLevel);
        parcel.writeInt(batteryStatsImpl.mDischargePlugLevel);
        parcel.writeInt(batteryStatsImpl.mDischargeCurrentLevel);
        parcel.writeInt(batteryStatsImpl.mCurrentBatteryLevel);
        parcel.writeInt(batteryStatsImpl.mEstimatedBatteryCapacity);
        parcel.writeInt(batteryStatsImpl.mMinLearnedBatteryCapacity);
        parcel.writeInt(batteryStatsImpl.mMaxLearnedBatteryCapacity);
        parcel.writeInt(getLowDischargeAmountSinceCharge());
        parcel.writeInt(getHighDischargeAmountSinceCharge());
        parcel.writeInt(getDischargeAmountScreenOnSinceCharge());
        parcel.writeInt(getDischargeAmountScreenOffSinceCharge());
        parcel.writeInt(getDischargeAmountScreenDozeSinceCharge());
        batteryStatsImpl.mDischargeStepTracker.writeToParcel(parcel);
        batteryStatsImpl.mChargeStepTracker.writeToParcel(parcel);
        batteryStatsImpl.mDailyDischargeStepTracker.writeToParcel(parcel);
        batteryStatsImpl.mDailyChargeStepTracker.writeToParcel(parcel);
        batteryStatsImpl.mDischargeCounter.writeSummaryFromParcelLocked(parcel);
        batteryStatsImpl.mDischargeScreenOffCounter.writeSummaryFromParcelLocked(parcel);
        batteryStatsImpl.mDischargeScreenDozeCounter.writeSummaryFromParcelLocked(parcel);
        batteryStatsImpl.mDischargeLightDozeCounter.writeSummaryFromParcelLocked(parcel);
        batteryStatsImpl.mDischargeDeepDozeCounter.writeSummaryFromParcelLocked(parcel);
        if (batteryStatsImpl.mDailyPackageChanges != null) {
            int NPKG = batteryStatsImpl.mDailyPackageChanges.size();
            parcel.writeInt(NPKG);
            for (int i4 = 0; i4 < NPKG; i4++) {
                BatteryStats.PackageChange pc = batteryStatsImpl.mDailyPackageChanges.get(i4);
                parcel.writeString(pc.mPackageName);
                parcel.writeInt(pc.mUpdate ? 1 : 0);
                parcel.writeLong(pc.mVersionCode);
            }
        } else {
            parcel.writeInt(0);
        }
        parcel.writeLong(batteryStatsImpl.mDailyStartTime);
        parcel.writeLong(batteryStatsImpl.mNextMinDailyDeadline);
        parcel.writeLong(batteryStatsImpl.mNextMaxDailyDeadline);
        batteryStatsImpl.mScreenOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mScreenDozeTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        int i5 = 0;
        while (true) {
            i = 5;
            if (i5 >= 5) {
                break;
            }
            batteryStatsImpl.mScreenBrightnessTimer[i5].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
            i5++;
        }
        batteryStatsImpl.mInteractiveTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mPowerSaveModeEnabledTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        parcel.writeLong(batteryStatsImpl.mLongestLightIdleTime);
        parcel.writeLong(batteryStatsImpl.mLongestFullIdleTime);
        batteryStatsImpl.mDeviceIdleModeLightTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mDeviceIdleModeFullTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mDeviceLightIdlingTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mDeviceIdlingTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mPhoneOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        for (int i6 = 0; i6 < 5; i6++) {
            batteryStatsImpl.mPhoneSignalStrengthsTimer[i6].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        }
        batteryStatsImpl.mPhoneSignalScanningTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        for (int i7 = 0; i7 < 21; i7++) {
            batteryStatsImpl.mPhoneDataConnectionsTimer[i7].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        }
        int i8 = 0;
        while (true) {
            i2 = 10;
            if (i8 >= 10) {
                break;
            }
            batteryStatsImpl.mNetworkByteActivityCounters[i8].writeSummaryFromParcelLocked(parcel);
            batteryStatsImpl.mNetworkPacketActivityCounters[i8].writeSummaryFromParcelLocked(parcel);
            i8++;
        }
        batteryStatsImpl.mMobileRadioActiveTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mMobileRadioActivePerAppTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mMobileRadioActiveAdjustedTime.writeSummaryFromParcelLocked(parcel);
        batteryStatsImpl.mMobileRadioActiveUnknownTime.writeSummaryFromParcelLocked(parcel);
        batteryStatsImpl.mMobileRadioActiveUnknownCount.writeSummaryFromParcelLocked(parcel);
        batteryStatsImpl.mWifiMulticastWakelockTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mWifiOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mGlobalWifiRunningTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        for (int i9 = 0; i9 < 8; i9++) {
            batteryStatsImpl.mWifiStateTimer[i9].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        }
        for (int i10 = 0; i10 < 13; i10++) {
            batteryStatsImpl.mWifiSupplStateTimer[i10].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        }
        for (int i11 = 0; i11 < 5; i11++) {
            batteryStatsImpl.mWifiSignalStrengthsTimer[i11].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        }
        batteryStatsImpl.mWifiActiveTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mWifiActivity.writeSummaryToParcel(parcel);
        for (int i12 = 0; i12 < 2; i12++) {
            batteryStatsImpl.mGpsSignalQualityTimer[i12].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        }
        batteryStatsImpl.mBluetoothActivity.writeSummaryToParcel(parcel);
        batteryStatsImpl.mModemActivity.writeSummaryToParcel(parcel);
        parcel.writeInt(batteryStatsImpl.mHasWifiReporting ? 1 : 0);
        parcel.writeInt(batteryStatsImpl.mHasBluetoothReporting ? 1 : 0);
        parcel.writeInt(batteryStatsImpl.mHasModemReporting ? 1 : 0);
        parcel.writeInt(batteryStatsImpl.mNumConnectivityChange);
        batteryStatsImpl.mFlashlightOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mCameraOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        batteryStatsImpl.mBluetoothScanTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
        parcel.writeInt(batteryStatsImpl.mRpmStats.size());
        for (Map.Entry<String, SamplingTimer> ent : batteryStatsImpl.mRpmStats.entrySet()) {
            Timer rpmt = ent.getValue();
            if (rpmt != null) {
                parcel.writeInt(1);
                parcel.writeString(ent.getKey());
                rpmt.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
            } else {
                parcel.writeInt(0);
            }
        }
        parcel.writeInt(batteryStatsImpl.mScreenOffRpmStats.size());
        for (Map.Entry<String, SamplingTimer> ent2 : batteryStatsImpl.mScreenOffRpmStats.entrySet()) {
            Timer rpmt2 = ent2.getValue();
            if (rpmt2 != null) {
                parcel.writeInt(1);
                parcel.writeString(ent2.getKey());
                rpmt2.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
            } else {
                parcel.writeInt(0);
            }
        }
        parcel.writeInt(batteryStatsImpl.mKernelWakelockStats.size());
        for (Map.Entry<String, SamplingTimer> ent3 : batteryStatsImpl.mKernelWakelockStats.entrySet()) {
            Timer kwlt = ent3.getValue();
            if (kwlt != null) {
                parcel.writeInt(1);
                parcel.writeString(ent3.getKey());
                kwlt.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
            } else {
                parcel.writeInt(0);
            }
        }
        parcel.writeInt(batteryStatsImpl.mWakeupReasonStats.size());
        for (Map.Entry<String, SamplingTimer> ent4 : batteryStatsImpl.mWakeupReasonStats.entrySet()) {
            SamplingTimer timer = ent4.getValue();
            if (timer != null) {
                parcel.writeInt(1);
                parcel.writeString(ent4.getKey());
                timer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
            } else {
                parcel.writeInt(0);
            }
        }
        parcel.writeInt(batteryStatsImpl.mKernelMemoryStats.size());
        for (int i13 = 0; i13 < batteryStatsImpl.mKernelMemoryStats.size(); i13++) {
            Timer kmt = batteryStatsImpl.mKernelMemoryStats.valueAt(i13);
            if (kmt != null) {
                parcel.writeInt(1);
                parcel.writeLong(batteryStatsImpl.mKernelMemoryStats.keyAt(i13));
                kmt.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
            } else {
                parcel.writeInt(0);
            }
        }
        int NU = batteryStatsImpl.mUidStats.size();
        parcel.writeInt(NU);
        int iu = 0;
        while (true) {
            int iu2 = iu;
            if (iu2 < NU) {
                parcel.writeInt(batteryStatsImpl.mUidStats.keyAt(iu2));
                Uid u = batteryStatsImpl.mUidStats.valueAt(iu2);
                TimeBase timeBase = u.mOnBatteryBackgroundTimeBase;
                Uid u2 = u;
                Parcel parcel3 = parcel;
                int NU2 = NU;
                int iu3 = iu2;
                long j3 = NOW_SYS;
                long startClockTime2 = startClockTime;
                int i14 = i;
                int i15 = i2;
                long j4 = NOWREAL_SYS;
                timeBase.writeSummaryToParcel(parcel3, j3, j4);
                u2.mOnBatteryScreenOffBackgroundTimeBase.writeSummaryToParcel(parcel3, j3, j4);
                if (u2.mWifiRunningTimer != null) {
                    parcel.writeInt(1);
                    u2.mWifiRunningTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mFullWifiLockTimer != null) {
                    parcel.writeInt(1);
                    u2.mFullWifiLockTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mWifiScanTimer != null) {
                    parcel.writeInt(1);
                    u2.mWifiScanTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                for (int i16 = 0; i16 < i14; i16++) {
                    if (u2.mWifiBatchedScanTimer[i16] != null) {
                        parcel.writeInt(1);
                        u2.mWifiBatchedScanTimer[i16].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    } else {
                        parcel.writeInt(0);
                    }
                }
                if (u2.mWifiMulticastTimer != null) {
                    parcel.writeInt(1);
                    u2.mWifiMulticastTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mAudioTurnedOnTimer != null) {
                    parcel.writeInt(1);
                    u2.mAudioTurnedOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mVideoTurnedOnTimer != null) {
                    parcel.writeInt(1);
                    u2.mVideoTurnedOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mFlashlightTurnedOnTimer != null) {
                    parcel.writeInt(1);
                    u2.mFlashlightTurnedOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mCameraTurnedOnTimer != null) {
                    parcel.writeInt(1);
                    u2.mCameraTurnedOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mForegroundActivityTimer != null) {
                    parcel.writeInt(1);
                    u2.mForegroundActivityTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mForegroundServiceTimer != null) {
                    parcel.writeInt(1);
                    u2.mForegroundServiceTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mAggregatedPartialWakelockTimer != null) {
                    parcel.writeInt(1);
                    u2.mAggregatedPartialWakelockTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mBluetoothScanTimer != null) {
                    parcel.writeInt(1);
                    u2.mBluetoothScanTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mBluetoothUnoptimizedScanTimer != null) {
                    parcel.writeInt(1);
                    u2.mBluetoothUnoptimizedScanTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mBluetoothScanResultCounter != null) {
                    parcel.writeInt(1);
                    u2.mBluetoothScanResultCounter.writeSummaryFromParcelLocked(parcel);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mBluetoothScanResultBgCounter != null) {
                    parcel.writeInt(1);
                    u2.mBluetoothScanResultBgCounter.writeSummaryFromParcelLocked(parcel);
                } else {
                    parcel.writeInt(0);
                }
                for (int i17 = 0; i17 < 7; i17++) {
                    if (u2.mProcessStateTimer[i17] != null) {
                        parcel.writeInt(1);
                        u2.mProcessStateTimer[i17].writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    } else {
                        parcel.writeInt(0);
                    }
                }
                if (u2.mVibratorOnTimer != null) {
                    parcel.writeInt(1);
                    u2.mVibratorOnTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    i3 = 0;
                } else {
                    i3 = 0;
                    parcel.writeInt(0);
                }
                if (u2.mUserActivityCounters == null) {
                    parcel.writeInt(i3);
                } else {
                    parcel.writeInt(1);
                    for (int i18 = 0; i18 < 4; i18++) {
                        u2.mUserActivityCounters[i18].writeSummaryFromParcelLocked(parcel);
                    }
                }
                if (u2.mNetworkByteActivityCounters == null) {
                    parcel.writeInt(0);
                } else {
                    parcel.writeInt(1);
                    for (int i19 = 0; i19 < i15; i19++) {
                        u2.mNetworkByteActivityCounters[i19].writeSummaryFromParcelLocked(parcel);
                        u2.mNetworkPacketActivityCounters[i19].writeSummaryFromParcelLocked(parcel);
                    }
                    u2.mMobileRadioActiveTime.writeSummaryFromParcelLocked(parcel);
                    u2.mMobileRadioActiveCount.writeSummaryFromParcelLocked(parcel);
                }
                u2.mUserCpuTime.writeSummaryFromParcelLocked(parcel);
                u2.mSystemCpuTime.writeSummaryFromParcelLocked(parcel);
                if (u2.mCpuClusterSpeedTimesUs != null) {
                    parcel.writeInt(1);
                    parcel.writeInt(u2.mCpuClusterSpeedTimesUs.length);
                    for (LongSamplingCounter[] cpuSpeeds : u2.mCpuClusterSpeedTimesUs) {
                        if (cpuSpeeds != null) {
                            parcel.writeInt(1);
                            parcel.writeInt(cpuSpeeds.length);
                            for (LongSamplingCounter c : cpuSpeeds) {
                                if (c != null) {
                                    parcel.writeInt(1);
                                    c.writeSummaryFromParcelLocked(parcel);
                                } else {
                                    parcel.writeInt(0);
                                }
                            }
                        } else {
                            parcel.writeInt(0);
                        }
                    }
                } else {
                    parcel.writeInt(0);
                }
                LongSamplingCounterArray.writeSummaryToParcelLocked(parcel, u2.mCpuFreqTimeMs);
                LongSamplingCounterArray.writeSummaryToParcelLocked(parcel, u2.mScreenOffCpuFreqTimeMs);
                u2.mCpuActiveTimeMs.writeSummaryFromParcelLocked(parcel);
                u2.mCpuClusterTimesMs.writeSummaryToParcelLocked(parcel);
                if (u2.mProcStateTimeMs != null) {
                    parcel.writeInt(u2.mProcStateTimeMs.length);
                    for (LongSamplingCounterArray counters : u2.mProcStateTimeMs) {
                        LongSamplingCounterArray.writeSummaryToParcelLocked(parcel, counters);
                    }
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mProcStateScreenOffTimeMs != null) {
                    parcel.writeInt(u2.mProcStateScreenOffTimeMs.length);
                    for (LongSamplingCounterArray counters2 : u2.mProcStateScreenOffTimeMs) {
                        LongSamplingCounterArray.writeSummaryToParcelLocked(parcel, counters2);
                    }
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mMobileRadioApWakeupCount != null) {
                    parcel.writeInt(1);
                    u2.mMobileRadioApWakeupCount.writeSummaryFromParcelLocked(parcel);
                } else {
                    parcel.writeInt(0);
                }
                if (u2.mWifiRadioApWakeupCount != null) {
                    parcel.writeInt(1);
                    u2.mWifiRadioApWakeupCount.writeSummaryFromParcelLocked(parcel);
                } else {
                    parcel.writeInt(0);
                }
                ArrayMap<String, Uid.Wakelock> wakeStats = u2.mWakelockStats.getMap();
                int NW = wakeStats.size();
                parcel.writeInt(NW);
                for (int iw = 0; iw < NW; iw++) {
                    parcel.writeString(wakeStats.keyAt(iw));
                    Uid.Wakelock wl = wakeStats.valueAt(iw);
                    if (wl.mTimerFull != null) {
                        parcel.writeInt(1);
                        wl.mTimerFull.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    } else {
                        parcel.writeInt(0);
                    }
                    if (wl.mTimerPartial != null) {
                        parcel.writeInt(1);
                        wl.mTimerPartial.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    } else {
                        parcel.writeInt(0);
                    }
                    if (wl.mTimerWindow != null) {
                        parcel.writeInt(1);
                        wl.mTimerWindow.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    } else {
                        parcel.writeInt(0);
                    }
                    if (wl.mTimerDraw != null) {
                        parcel.writeInt(1);
                        wl.mTimerDraw.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    } else {
                        parcel.writeInt(0);
                    }
                }
                ArrayMap<String, DualTimer> syncStats = u2.mSyncStats.getMap();
                int NS = syncStats.size();
                parcel.writeInt(NS);
                for (int is = 0; is < NS; is++) {
                    parcel.writeString(syncStats.keyAt(is));
                    syncStats.valueAt(is).writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                }
                ArrayMap<String, DualTimer> jobStats = u2.mJobStats.getMap();
                int NJ = jobStats.size();
                parcel.writeInt(NJ);
                for (int ij = 0; ij < NJ; ij++) {
                    parcel.writeString(jobStats.keyAt(ij));
                    jobStats.valueAt(ij).writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                }
                u2.writeJobCompletionsToParcelLocked(parcel);
                u2.mJobsDeferredEventCount.writeSummaryFromParcelLocked(parcel);
                u2.mJobsDeferredCount.writeSummaryFromParcelLocked(parcel);
                u2.mJobsFreshnessTimeMs.writeSummaryFromParcelLocked(parcel);
                for (int i20 = 0; i20 < JOB_FRESHNESS_BUCKETS.length; i20++) {
                    if (u2.mJobsFreshnessBuckets[i20] != null) {
                        parcel.writeInt(1);
                        u2.mJobsFreshnessBuckets[i20].writeSummaryFromParcelLocked(parcel);
                    } else {
                        parcel.writeInt(0);
                    }
                }
                int NSE = u2.mSensorStats.size();
                parcel.writeInt(NSE);
                int ise = 0;
                while (ise < NSE) {
                    ArrayMap<String, Uid.Wakelock> wakeStats2 = wakeStats;
                    parcel.writeInt(u2.mSensorStats.keyAt(ise));
                    Uid.Sensor se = u2.mSensorStats.valueAt(ise);
                    int NW2 = NW;
                    if (se.mTimer != null) {
                        parcel.writeInt(1);
                        se.mTimer.writeSummaryFromParcelLocked(parcel, NOWREAL_SYS);
                    } else {
                        parcel.writeInt(0);
                    }
                    ise++;
                    wakeStats = wakeStats2;
                    NW = NW2;
                }
                int i21 = NW;
                int NP2 = u2.mProcessStats.size();
                parcel.writeInt(NP2);
                int ip = 0;
                while (ip < NP2) {
                    parcel.writeString(u2.mProcessStats.keyAt(ip));
                    Uid.Proc ps = u2.mProcessStats.valueAt(ip);
                    parcel.writeLong(ps.mUserTime);
                    parcel.writeLong(ps.mSystemTime);
                    parcel.writeLong(ps.mForegroundTime);
                    parcel.writeInt(ps.mStarts);
                    parcel.writeInt(ps.mNumCrashes);
                    parcel.writeInt(ps.mNumAnrs);
                    ps.writeExcessivePowerToParcelLocked(parcel);
                    ip++;
                    syncStats = syncStats;
                    NS = NS;
                }
                int i22 = NS;
                int iwa = u2.mPackageStats.size();
                parcel.writeInt(iwa);
                if (iwa > 0) {
                    Iterator<Map.Entry<String, Uid.Pkg>> it = u2.mPackageStats.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Uid.Pkg> ent5 = it.next();
                        parcel.writeString(ent5.getKey());
                        Uid.Pkg ps2 = ent5.getValue();
                        int NWA = ps2.mWakeupAlarms.size();
                        parcel.writeInt(NWA);
                        int iwa2 = 0;
                        while (true) {
                            NP = iwa;
                            int iwa3 = iwa2;
                            if (iwa3 >= NWA) {
                                break;
                            }
                            parcel.writeString(ps2.mWakeupAlarms.keyAt(iwa3));
                            ps2.mWakeupAlarms.valueAt(iwa3).writeSummaryFromParcelLocked(parcel);
                            iwa2 = iwa3 + 1;
                            iwa = NP;
                            it = it;
                        }
                        Iterator<Map.Entry<String, Uid.Pkg>> it2 = it;
                        int NS2 = ps2.mServiceStats.size();
                        parcel.writeInt(NS2);
                        int is2 = 0;
                        while (is2 < NS2) {
                            int NS3 = NS2;
                            parcel.writeString(ps2.mServiceStats.keyAt(is2));
                            Uid.Pkg.Serv ss = ps2.mServiceStats.valueAt(is2);
                            parcel.writeLong(ss.getStartTimeToNowLocked(batteryStatsImpl.mOnBatteryTimeBase.getUptime(NOW_SYS)));
                            parcel.writeInt(ss.mStarts);
                            parcel.writeInt(ss.mLaunches);
                            is2++;
                            NS2 = NS3;
                            ent5 = ent5;
                            ps2 = ps2;
                            batteryStatsImpl = this;
                        }
                        int NS4 = NS2;
                        iwa = NP;
                        it = it2;
                        int i23 = NS4;
                        batteryStatsImpl = this;
                    }
                }
                iu = iu3 + 1;
                NU = NU2;
                startClockTime = startClockTime2;
                batteryStatsImpl = this;
                i = 5;
                i2 = 10;
            } else {
                long j5 = startClockTime;
                return;
            }
        }
    }

    public void readFromParcel(Parcel in) {
        readFromParcelLocked(in);
    }

    /* access modifiers changed from: package-private */
    public void readFromParcelLocked(Parcel in) {
        Parcel parcel = in;
        if (in.readInt() == MAGIC) {
            int uid = 0;
            readHistory(parcel, false);
            this.mStartCount = in.readInt();
            this.mStartClockTime = in.readLong();
            this.mStartPlatformVersion = in.readString();
            this.mEndPlatformVersion = in.readString();
            this.mUptime = in.readLong();
            this.mUptimeStart = in.readLong();
            this.mRealtime = in.readLong();
            this.mRealtimeStart = in.readLong();
            boolean z = true;
            this.mOnBattery = in.readInt() != 0;
            this.mEstimatedBatteryCapacity = in.readInt();
            this.mMinLearnedBatteryCapacity = in.readInt();
            this.mMaxLearnedBatteryCapacity = in.readInt();
            this.mOnBatteryInternal = false;
            this.mOnBatteryTimeBase.readFromParcel(parcel);
            this.mOnBatteryScreenOffTimeBase.readFromParcel(parcel);
            this.mScreenState = 0;
            Parcel parcel2 = parcel;
            StopwatchTimer stopwatchTimer = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase, parcel2);
            this.mScreenOnTimer = stopwatchTimer;
            StopwatchTimer stopwatchTimer2 = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase, parcel2);
            this.mScreenDozeTimer = stopwatchTimer2;
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= 5) {
                    break;
                }
                StopwatchTimer[] stopwatchTimerArr = this.mScreenBrightnessTimer;
                StopwatchTimer stopwatchTimer3 = new StopwatchTimer(this.mClocks, null, -100 - i2, null, this.mOnBatteryTimeBase, parcel);
                stopwatchTimerArr[i2] = stopwatchTimer3;
                i = i2 + 1;
            }
            this.mInteractive = false;
            Parcel parcel3 = parcel;
            StopwatchTimer stopwatchTimer4 = new StopwatchTimer(this.mClocks, null, -10, null, this.mOnBatteryTimeBase, parcel3);
            this.mInteractiveTimer = stopwatchTimer4;
            this.mPhoneOn = false;
            StopwatchTimer stopwatchTimer5 = new StopwatchTimer(this.mClocks, null, -2, null, this.mOnBatteryTimeBase, parcel3);
            this.mPowerSaveModeEnabledTimer = stopwatchTimer5;
            this.mLongestLightIdleTime = in.readLong();
            this.mLongestFullIdleTime = in.readLong();
            StopwatchTimer stopwatchTimer6 = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase, parcel3);
            this.mDeviceIdleModeLightTimer = stopwatchTimer6;
            StopwatchTimer stopwatchTimer7 = new StopwatchTimer(this.mClocks, null, -11, null, this.mOnBatteryTimeBase, parcel3);
            this.mDeviceIdleModeFullTimer = stopwatchTimer7;
            StopwatchTimer stopwatchTimer8 = new StopwatchTimer(this.mClocks, null, -15, null, this.mOnBatteryTimeBase, parcel3);
            this.mDeviceLightIdlingTimer = stopwatchTimer8;
            StopwatchTimer stopwatchTimer9 = new StopwatchTimer(this.mClocks, null, -12, null, this.mOnBatteryTimeBase, parcel3);
            this.mDeviceIdlingTimer = stopwatchTimer9;
            StopwatchTimer stopwatchTimer10 = new StopwatchTimer(this.mClocks, null, -3, null, this.mOnBatteryTimeBase, parcel3);
            this.mPhoneOnTimer = stopwatchTimer10;
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= 5) {
                    break;
                }
                StopwatchTimer[] stopwatchTimerArr2 = this.mPhoneSignalStrengthsTimer;
                StopwatchTimer stopwatchTimer11 = new StopwatchTimer(this.mClocks, null, -200 - i4, null, this.mOnBatteryTimeBase, parcel);
                stopwatchTimerArr2[i4] = stopwatchTimer11;
                i3 = i4 + 1;
            }
            StopwatchTimer stopwatchTimer12 = new StopwatchTimer(this.mClocks, null, -199, null, this.mOnBatteryTimeBase, parcel);
            this.mPhoneSignalScanningTimer = stopwatchTimer12;
            int i5 = 0;
            while (true) {
                int i6 = i5;
                if (i6 >= 21) {
                    break;
                }
                StopwatchTimer[] stopwatchTimerArr3 = this.mPhoneDataConnectionsTimer;
                StopwatchTimer stopwatchTimer13 = new StopwatchTimer(this.mClocks, null, -300 - i6, null, this.mOnBatteryTimeBase, parcel);
                stopwatchTimerArr3[i6] = stopwatchTimer13;
                i5 = i6 + 1;
            }
            for (int i7 = 0; i7 < 10; i7++) {
                this.mNetworkByteActivityCounters[i7] = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
                this.mNetworkPacketActivityCounters[i7] = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            }
            this.mMobileRadioPowerState = 1;
            Parcel parcel4 = parcel;
            StopwatchTimer stopwatchTimer14 = new StopwatchTimer(this.mClocks, null, -400, null, this.mOnBatteryTimeBase, parcel4);
            this.mMobileRadioActiveTimer = stopwatchTimer14;
            StopwatchTimer stopwatchTimer15 = new StopwatchTimer(this.mClocks, null, -401, null, this.mOnBatteryTimeBase, parcel4);
            this.mMobileRadioActivePerAppTimer = stopwatchTimer15;
            this.mMobileRadioActiveAdjustedTime = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            this.mMobileRadioActiveUnknownTime = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            this.mMobileRadioActiveUnknownCount = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            StopwatchTimer stopwatchTimer16 = new StopwatchTimer(this.mClocks, null, -4, null, this.mOnBatteryTimeBase, parcel4);
            this.mWifiMulticastWakelockTimer = stopwatchTimer16;
            this.mWifiRadioPowerState = 1;
            this.mWifiOn = false;
            StopwatchTimer stopwatchTimer17 = new StopwatchTimer(this.mClocks, null, -4, null, this.mOnBatteryTimeBase, parcel4);
            this.mWifiOnTimer = stopwatchTimer17;
            this.mGlobalWifiRunning = false;
            StopwatchTimer stopwatchTimer18 = new StopwatchTimer(this.mClocks, null, -5, null, this.mOnBatteryTimeBase, parcel4);
            this.mGlobalWifiRunningTimer = stopwatchTimer18;
            int i8 = 0;
            while (true) {
                int i9 = i8;
                if (i9 >= 8) {
                    break;
                }
                StopwatchTimer[] stopwatchTimerArr4 = this.mWifiStateTimer;
                StopwatchTimer stopwatchTimer19 = new StopwatchTimer(this.mClocks, null, -600 - i9, null, this.mOnBatteryTimeBase, parcel);
                stopwatchTimerArr4[i9] = stopwatchTimer19;
                i8 = i9 + 1;
            }
            int i10 = 0;
            while (true) {
                int i11 = i10;
                if (i11 >= 13) {
                    break;
                }
                StopwatchTimer[] stopwatchTimerArr5 = this.mWifiSupplStateTimer;
                StopwatchTimer stopwatchTimer20 = new StopwatchTimer(this.mClocks, null, -700 - i11, null, this.mOnBatteryTimeBase, parcel);
                stopwatchTimerArr5[i11] = stopwatchTimer20;
                i10 = i11 + 1;
            }
            int i12 = 0;
            while (true) {
                int i13 = i12;
                if (i13 >= 5) {
                    break;
                }
                StopwatchTimer[] stopwatchTimerArr6 = this.mWifiSignalStrengthsTimer;
                StopwatchTimer stopwatchTimer21 = new StopwatchTimer(this.mClocks, null, -800 - i13, null, this.mOnBatteryTimeBase, parcel);
                stopwatchTimerArr6[i13] = stopwatchTimer21;
                i12 = i13 + 1;
            }
            StopwatchTimer stopwatchTimer22 = new StopwatchTimer(this.mClocks, null, -900, null, this.mOnBatteryTimeBase, parcel);
            this.mWifiActiveTimer = stopwatchTimer22;
            this.mWifiActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1, parcel);
            int i14 = 0;
            while (true) {
                int i15 = i14;
                if (i15 >= 2) {
                    break;
                }
                StopwatchTimer[] stopwatchTimerArr7 = this.mGpsSignalQualityTimer;
                StopwatchTimer stopwatchTimer23 = new StopwatchTimer(this.mClocks, null, -1000 - i15, null, this.mOnBatteryTimeBase, parcel);
                stopwatchTimerArr7[i15] = stopwatchTimer23;
                i14 = i15 + 1;
            }
            this.mBluetoothActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1, parcel);
            this.mModemActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 5, parcel);
            this.mHasWifiReporting = in.readInt() != 0;
            this.mHasBluetoothReporting = in.readInt() != 0;
            if (in.readInt() == 0) {
                z = false;
            }
            this.mHasModemReporting = z;
            this.mNumConnectivityChange = in.readInt();
            this.mLoadedNumConnectivityChange = in.readInt();
            this.mUnpluggedNumConnectivityChange = in.readInt();
            this.mAudioOnNesting = 0;
            StopwatchTimer stopwatchTimer24 = new StopwatchTimer(this.mClocks, null, -7, null, this.mOnBatteryTimeBase);
            this.mAudioOnTimer = stopwatchTimer24;
            this.mVideoOnNesting = 0;
            StopwatchTimer stopwatchTimer25 = new StopwatchTimer(this.mClocks, null, -8, null, this.mOnBatteryTimeBase);
            this.mVideoOnTimer = stopwatchTimer25;
            this.mFlashlightOnNesting = 0;
            Parcel parcel5 = parcel;
            StopwatchTimer stopwatchTimer26 = new StopwatchTimer(this.mClocks, null, -9, null, this.mOnBatteryTimeBase, parcel5);
            this.mFlashlightOnTimer = stopwatchTimer26;
            this.mCameraOnNesting = 0;
            StopwatchTimer stopwatchTimer27 = new StopwatchTimer(this.mClocks, null, -13, null, this.mOnBatteryTimeBase, parcel5);
            this.mCameraOnTimer = stopwatchTimer27;
            this.mBluetoothScanNesting = 0;
            StopwatchTimer stopwatchTimer28 = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase, parcel5);
            this.mBluetoothScanTimer = stopwatchTimer28;
            this.mIsCellularTxPowerHigh = false;
            this.mDischargeUnplugLevel = in.readInt();
            this.mDischargePlugLevel = in.readInt();
            this.mDischargeCurrentLevel = in.readInt();
            this.mCurrentBatteryLevel = in.readInt();
            this.mLowDischargeAmountSinceCharge = in.readInt();
            this.mHighDischargeAmountSinceCharge = in.readInt();
            this.mDischargeAmountScreenOn = in.readInt();
            this.mDischargeAmountScreenOnSinceCharge = in.readInt();
            this.mDischargeAmountScreenOff = in.readInt();
            this.mDischargeAmountScreenOffSinceCharge = in.readInt();
            this.mDischargeAmountScreenDoze = in.readInt();
            this.mDischargeAmountScreenDozeSinceCharge = in.readInt();
            this.mDischargeStepTracker.readFromParcel(parcel);
            this.mChargeStepTracker.readFromParcel(parcel);
            this.mDischargeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            this.mDischargeScreenOffCounter = new LongSamplingCounter(this.mOnBatteryScreenOffTimeBase, parcel);
            this.mDischargeScreenDozeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            this.mDischargeLightDozeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            this.mDischargeDeepDozeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase, parcel);
            this.mLastWriteTime = in.readLong();
            this.mRpmStats.clear();
            int NRPMS = in.readInt();
            for (int irpm = 0; irpm < NRPMS; irpm++) {
                if (in.readInt() != 0) {
                    this.mRpmStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase, parcel));
                }
            }
            this.mScreenOffRpmStats.clear();
            int NSORPMS = in.readInt();
            for (int irpm2 = 0; irpm2 < NSORPMS; irpm2++) {
                if (in.readInt() != 0) {
                    this.mScreenOffRpmStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase, parcel));
                }
            }
            this.mKernelWakelockStats.clear();
            int NKW = in.readInt();
            for (int ikw = 0; ikw < NKW; ikw++) {
                if (in.readInt() != 0) {
                    this.mKernelWakelockStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase, parcel));
                }
            }
            this.mWakeupReasonStats.clear();
            int NWR = in.readInt();
            for (int iwr = 0; iwr < NWR; iwr++) {
                if (in.readInt() != 0) {
                    this.mWakeupReasonStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase, parcel));
                }
            }
            this.mKernelMemoryStats.clear();
            int nmt = in.readInt();
            for (int imt = 0; imt < nmt; imt++) {
                if (in.readInt() != 0) {
                    this.mKernelMemoryStats.put(Long.valueOf(in.readLong()).longValue(), new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase, parcel));
                }
            }
            this.mPartialTimers.clear();
            this.mFullTimers.clear();
            this.mWindowTimers.clear();
            this.mWifiRunningTimers.clear();
            this.mFullWifiLockTimers.clear();
            this.mWifiScanTimers.clear();
            this.mWifiBatchedScanTimers.clear();
            this.mWifiMulticastTimers.clear();
            this.mAudioTurnedOnTimers.clear();
            this.mVideoTurnedOnTimers.clear();
            this.mFlashlightTurnedOnTimers.clear();
            this.mCameraTurnedOnTimers.clear();
            int numUids = in.readInt();
            this.mUidStats.clear();
            while (true) {
                int i16 = uid;
                if (i16 < numUids) {
                    int uid2 = in.readInt();
                    Uid u = new Uid(this, uid2);
                    u.readFromParcelLocked(this.mOnBatteryTimeBase, this.mOnBatteryScreenOffTimeBase, parcel);
                    this.mUidStats.append(uid2, u);
                    uid = i16 + 1;
                } else {
                    return;
                }
            }
        } else {
            throw new ParcelFormatException("Bad magic number: #" + Integer.toHexString(magic));
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        writeToParcelLocked(out, true, flags);
    }

    public void writeToParcelWithoutUids(Parcel out, int flags) {
        writeToParcelLocked(out, false, flags);
    }

    /* access modifiers changed from: package-private */
    public void writeToParcelLocked(Parcel out, boolean inclUids, int flags) {
        Parcel parcel = out;
        pullPendingStateUpdatesLocked();
        long startClockTime = getStartClockTime();
        long uSecUptime = this.mClocks.uptimeMillis() * 1000;
        long uSecRealtime = this.mClocks.elapsedRealtime() * 1000;
        long realtime = this.mOnBatteryTimeBase.getRealtime(uSecRealtime);
        long realtime2 = this.mOnBatteryScreenOffTimeBase.getRealtime(uSecRealtime);
        parcel.writeInt(MAGIC);
        writeHistory(parcel, true, false);
        parcel.writeInt(this.mStartCount);
        parcel.writeLong(startClockTime);
        parcel.writeString(this.mStartPlatformVersion);
        parcel.writeString(this.mEndPlatformVersion);
        parcel.writeLong(this.mUptime);
        parcel.writeLong(this.mUptimeStart);
        parcel.writeLong(this.mRealtime);
        parcel.writeLong(this.mRealtimeStart);
        parcel.writeInt(this.mOnBattery ? 1 : 0);
        parcel.writeInt(this.mEstimatedBatteryCapacity);
        parcel.writeInt(this.mMinLearnedBatteryCapacity);
        parcel.writeInt(this.mMaxLearnedBatteryCapacity);
        Parcel parcel2 = parcel;
        long j = uSecUptime;
        long j2 = startClockTime;
        long j3 = uSecRealtime;
        this.mOnBatteryTimeBase.writeToParcel(parcel2, j, j3);
        this.mOnBatteryScreenOffTimeBase.writeToParcel(parcel2, j, j3);
        this.mScreenOnTimer.writeToParcel(parcel, uSecRealtime);
        this.mScreenDozeTimer.writeToParcel(parcel, uSecRealtime);
        for (int i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i].writeToParcel(parcel, uSecRealtime);
        }
        this.mInteractiveTimer.writeToParcel(parcel, uSecRealtime);
        this.mPowerSaveModeEnabledTimer.writeToParcel(parcel, uSecRealtime);
        parcel.writeLong(this.mLongestLightIdleTime);
        parcel.writeLong(this.mLongestFullIdleTime);
        this.mDeviceIdleModeLightTimer.writeToParcel(parcel, uSecRealtime);
        this.mDeviceIdleModeFullTimer.writeToParcel(parcel, uSecRealtime);
        this.mDeviceLightIdlingTimer.writeToParcel(parcel, uSecRealtime);
        this.mDeviceIdlingTimer.writeToParcel(parcel, uSecRealtime);
        this.mPhoneOnTimer.writeToParcel(parcel, uSecRealtime);
        for (int i2 = 0; i2 < 5; i2++) {
            this.mPhoneSignalStrengthsTimer[i2].writeToParcel(parcel, uSecRealtime);
        }
        this.mPhoneSignalScanningTimer.writeToParcel(parcel, uSecRealtime);
        for (int i3 = 0; i3 < 21; i3++) {
            this.mPhoneDataConnectionsTimer[i3].writeToParcel(parcel, uSecRealtime);
        }
        for (int i4 = 0; i4 < 10; i4++) {
            this.mNetworkByteActivityCounters[i4].writeToParcel(parcel);
            this.mNetworkPacketActivityCounters[i4].writeToParcel(parcel);
        }
        this.mMobileRadioActiveTimer.writeToParcel(parcel, uSecRealtime);
        this.mMobileRadioActivePerAppTimer.writeToParcel(parcel, uSecRealtime);
        this.mMobileRadioActiveAdjustedTime.writeToParcel(parcel);
        this.mMobileRadioActiveUnknownTime.writeToParcel(parcel);
        this.mMobileRadioActiveUnknownCount.writeToParcel(parcel);
        this.mWifiMulticastWakelockTimer.writeToParcel(parcel, uSecRealtime);
        this.mWifiOnTimer.writeToParcel(parcel, uSecRealtime);
        this.mGlobalWifiRunningTimer.writeToParcel(parcel, uSecRealtime);
        for (int i5 = 0; i5 < 8; i5++) {
            this.mWifiStateTimer[i5].writeToParcel(parcel, uSecRealtime);
        }
        for (int i6 = 0; i6 < 13; i6++) {
            this.mWifiSupplStateTimer[i6].writeToParcel(parcel, uSecRealtime);
        }
        for (int i7 = 0; i7 < 5; i7++) {
            this.mWifiSignalStrengthsTimer[i7].writeToParcel(parcel, uSecRealtime);
        }
        this.mWifiActiveTimer.writeToParcel(parcel, uSecRealtime);
        this.mWifiActivity.writeToParcel(parcel, 0);
        for (int i8 = 0; i8 < 2; i8++) {
            this.mGpsSignalQualityTimer[i8].writeToParcel(parcel, uSecRealtime);
        }
        this.mBluetoothActivity.writeToParcel(parcel, 0);
        this.mModemActivity.writeToParcel(parcel, 0);
        parcel.writeInt(this.mHasWifiReporting ? 1 : 0);
        parcel.writeInt(this.mHasBluetoothReporting ? 1 : 0);
        parcel.writeInt(this.mHasModemReporting ? 1 : 0);
        parcel.writeInt(this.mNumConnectivityChange);
        parcel.writeInt(this.mLoadedNumConnectivityChange);
        parcel.writeInt(this.mUnpluggedNumConnectivityChange);
        this.mFlashlightOnTimer.writeToParcel(parcel, uSecRealtime);
        this.mCameraOnTimer.writeToParcel(parcel, uSecRealtime);
        this.mBluetoothScanTimer.writeToParcel(parcel, uSecRealtime);
        parcel.writeInt(this.mDischargeUnplugLevel);
        parcel.writeInt(this.mDischargePlugLevel);
        parcel.writeInt(this.mDischargeCurrentLevel);
        parcel.writeInt(this.mCurrentBatteryLevel);
        parcel.writeInt(this.mLowDischargeAmountSinceCharge);
        parcel.writeInt(this.mHighDischargeAmountSinceCharge);
        parcel.writeInt(this.mDischargeAmountScreenOn);
        parcel.writeInt(this.mDischargeAmountScreenOnSinceCharge);
        parcel.writeInt(this.mDischargeAmountScreenOff);
        parcel.writeInt(this.mDischargeAmountScreenOffSinceCharge);
        parcel.writeInt(this.mDischargeAmountScreenDoze);
        parcel.writeInt(this.mDischargeAmountScreenDozeSinceCharge);
        this.mDischargeStepTracker.writeToParcel(parcel);
        this.mChargeStepTracker.writeToParcel(parcel);
        this.mDischargeCounter.writeToParcel(parcel);
        this.mDischargeScreenOffCounter.writeToParcel(parcel);
        this.mDischargeScreenDozeCounter.writeToParcel(parcel);
        this.mDischargeLightDozeCounter.writeToParcel(parcel);
        this.mDischargeDeepDozeCounter.writeToParcel(parcel);
        parcel.writeLong(this.mLastWriteTime);
        parcel.writeInt(this.mRpmStats.size());
        for (Map.Entry<String, SamplingTimer> ent : this.mRpmStats.entrySet()) {
            SamplingTimer rpmt = ent.getValue();
            if (rpmt != null) {
                parcel.writeInt(1);
                parcel.writeString(ent.getKey());
                rpmt.writeToParcel(parcel, uSecRealtime);
            } else {
                parcel.writeInt(0);
            }
        }
        parcel.writeInt(this.mScreenOffRpmStats.size());
        for (Map.Entry<String, SamplingTimer> ent2 : this.mScreenOffRpmStats.entrySet()) {
            SamplingTimer rpmt2 = ent2.getValue();
            if (rpmt2 != null) {
                parcel.writeInt(1);
                parcel.writeString(ent2.getKey());
                rpmt2.writeToParcel(parcel, uSecRealtime);
            } else {
                parcel.writeInt(0);
            }
        }
        if (inclUids) {
            parcel.writeInt(this.mKernelWakelockStats.size());
            for (Map.Entry<String, SamplingTimer> ent3 : this.mKernelWakelockStats.entrySet()) {
                SamplingTimer kwlt = ent3.getValue();
                if (kwlt != null) {
                    parcel.writeInt(1);
                    parcel.writeString(ent3.getKey());
                    kwlt.writeToParcel(parcel, uSecRealtime);
                } else {
                    parcel.writeInt(0);
                }
            }
            parcel.writeInt(this.mWakeupReasonStats.size());
            for (Map.Entry<String, SamplingTimer> ent4 : this.mWakeupReasonStats.entrySet()) {
                SamplingTimer timer = ent4.getValue();
                if (timer != null) {
                    parcel.writeInt(1);
                    parcel.writeString(ent4.getKey());
                    timer.writeToParcel(parcel, uSecRealtime);
                } else {
                    parcel.writeInt(0);
                }
            }
        } else {
            parcel.writeInt(0);
            parcel.writeInt(0);
        }
        parcel.writeInt(this.mKernelMemoryStats.size());
        for (int i9 = 0; i9 < this.mKernelMemoryStats.size(); i9++) {
            SamplingTimer kmt = this.mKernelMemoryStats.valueAt(i9);
            if (kmt != null) {
                parcel.writeInt(1);
                parcel.writeLong(this.mKernelMemoryStats.keyAt(i9));
                kmt.writeToParcel(parcel, uSecRealtime);
            } else {
                parcel.writeInt(0);
            }
        }
        if (inclUids) {
            int size = this.mUidStats.size();
            parcel.writeInt(size);
            for (int i10 = 0; i10 < size; i10++) {
                parcel.writeInt(this.mUidStats.keyAt(i10));
                this.mUidStats.valueAt(i10).writeToParcelLocked(parcel, uSecUptime, uSecRealtime);
            }
            return;
        }
        parcel.writeInt(0);
    }

    public void prepareForDumpLocked() {
        pullPendingStateUpdatesLocked();
        getStartClockTime();
    }

    public void dumpLocked(Context context, PrintWriter pw, int flags, int reqUid, long histStart) {
        BatteryStatsImpl.super.dumpLocked(context, pw, flags, reqUid, histStart);
        pw.print("Total cpu time reads: ");
        pw.println(this.mNumSingleUidCpuTimeReads);
        pw.print("Batched cpu time reads: ");
        pw.println(this.mNumBatchedSingleUidCpuTimeReads);
        pw.print("Batching Duration (min): ");
        pw.println((this.mClocks.uptimeMillis() - this.mCpuTimeReadsTrackingStartTime) / 60000);
        pw.print("All UID cpu time reads since the later of device start or stats reset: ");
        pw.println(this.mNumAllUidCpuTimeReads);
        pw.print("UIDs removed since the later of device start or stats reset: ");
        pw.println(this.mNumUidsRemoved);
    }
}
