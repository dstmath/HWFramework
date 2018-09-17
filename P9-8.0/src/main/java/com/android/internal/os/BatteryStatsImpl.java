package com.android.internal.os;

import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.UidTraffic;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkStats;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryStats;
import android.os.BatteryStats.ControllerActivityCounter;
import android.os.BatteryStats.DailyItem;
import android.os.BatteryStats.HistoryEventTracker;
import android.os.BatteryStats.HistoryItem;
import android.os.BatteryStats.HistoryPrinter;
import android.os.BatteryStats.HistoryStepDetails;
import android.os.BatteryStats.HistoryTag;
import android.os.BatteryStats.LevelStepTracker;
import android.os.BatteryStats.LongCounter;
import android.os.BatteryStats.LongCounterArray;
import android.os.BatteryStats.PackageChange;
import android.os.BatteryStats.Uid.Pid;
import android.os.BatteryStats.Uid.Proc.ExcessivePower;
import android.os.Build;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBatteryPropertiesRegistrar.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.WorkSource;
import android.provider.Telephony.BaseMmsColumns;
import android.rms.HwSysResource;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.HwLog;
import android.util.Log;
import android.util.LogException;
import android.util.LogWriter;
import android.util.LongSparseArray;
import android.util.LongSparseLongArray;
import android.util.MutableInt;
import android.util.Pools.Pool;
import android.util.Pools.SynchronizedPool;
import android.util.Printer;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import android.util.Xml;
import android.view.SurfaceControl;
import com.android.ims.ImsConfig;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.EventLogTags;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.os.KernelUidCpuTimeReader.Callback;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.huawei.pgmng.log.LogPower;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
    public static final Creator<BatteryStatsImpl> CREATOR = new Creator<BatteryStatsImpl>() {
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
    static final int MAX_HISTORY_BUFFER = 262144;
    private static final int MAX_HISTORY_ITEMS = 2000;
    static final int MAX_LEVEL_STEPS = 200;
    static final int MAX_MAX_HISTORY_BUFFER = 327680;
    private static final int MAX_MAX_HISTORY_ITEMS = 3000;
    private static final int MAX_WAKELOCKS_PER_UID = 100;
    private static final int MAX_WAKERLOCKS_WEIXIN = 60;
    static final int MSG_REPORT_CHARGING = 3;
    static final int MSG_REPORT_POWER_CHANGE = 2;
    static final int MSG_UPDATE_WAKELOCKS = 1;
    private static final int NUM_BT_TX_LEVELS = 1;
    private static final int NUM_WIFI_TX_LEVELS = 1;
    private static final int READ_KERNEL_WAKELOCK_STATS_MAX_TIMEOUT = 500;
    static final int STATE_BATTERY_HEALTH_MASK = 7;
    static final int STATE_BATTERY_HEALTH_SHIFT = 26;
    static final int STATE_BATTERY_MASK = -16777216;
    static final int STATE_BATTERY_PLUG_MASK = 3;
    static final int STATE_BATTERY_PLUG_SHIFT = 24;
    static final int STATE_BATTERY_STATUS_MASK = 7;
    static final int STATE_BATTERY_STATUS_SHIFT = 29;
    private static final String TAG = "BatteryStatsImpl";
    private static final boolean USE_OLD_HISTORY = false;
    private static final int VERSION = 159;
    final HistoryEventTracker mActiveEvents;
    int mActiveHistoryStates;
    int mActiveHistoryStates2;
    int mAudioOnNesting;
    StopwatchTimer mAudioOnTimer;
    final ArrayList<StopwatchTimer> mAudioTurnedOnTimers;
    ControllerActivityCounterImpl mBluetoothActivity;
    int mBluetoothScanNesting;
    final ArrayList<StopwatchTimer> mBluetoothScanOnTimers;
    protected StopwatchTimer mBluetoothScanTimer;
    private BatteryCallback mCallback;
    int mCameraOnNesting;
    StopwatchTimer mCameraOnTimer;
    final ArrayList<StopwatchTimer> mCameraTurnedOnTimers;
    int mChangedStates;
    int mChangedStates2;
    final LevelStepTracker mChargeStepTracker;
    boolean mCharging;
    public final AtomicFile mCheckinFile;
    protected Clocks mClocks;
    private long[] mCpuFreqs;
    final HistoryStepDetails mCurHistoryStepDetails;
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
    final LevelStepTracker mDailyChargeStepTracker;
    final LevelStepTracker mDailyDischargeStepTracker;
    public final AtomicFile mDailyFile;
    final ArrayList<DailyItem> mDailyItems;
    ArrayList<PackageChange> mDailyPackageChanges;
    long mDailyStartTime;
    int mDeviceIdleMode;
    StopwatchTimer mDeviceIdleModeFullTimer;
    StopwatchTimer mDeviceIdleModeLightTimer;
    boolean mDeviceIdling;
    StopwatchTimer mDeviceIdlingTimer;
    boolean mDeviceLightIdling;
    StopwatchTimer mDeviceLightIdlingTimer;
    int mDischargeAmountScreenOff;
    int mDischargeAmountScreenOffSinceCharge;
    int mDischargeAmountScreenOn;
    int mDischargeAmountScreenOnSinceCharge;
    private LongSamplingCounter mDischargeCounter;
    int mDischargeCurrentLevel;
    int mDischargePlugLevel;
    private LongSamplingCounter mDischargeScreenOffCounter;
    int mDischargeScreenOffUnplugLevel;
    int mDischargeScreenOnUnplugLevel;
    int mDischargeStartLevel;
    final LevelStepTracker mDischargeStepTracker;
    int mDischargeUnplugLevel;
    boolean mDistributeWakelockCpu;
    final ArrayList<StopwatchTimer> mDrawTimers;
    String mEndPlatformVersion;
    private int mEstimatedBatteryCapacity;
    private final ExternalStatsSync mExternalSync;
    private final JournaledFile mFile;
    int mFlashlightOnNesting;
    StopwatchTimer mFlashlightOnTimer;
    final ArrayList<StopwatchTimer> mFlashlightTurnedOnTimers;
    final ArrayList<StopwatchTimer> mFullTimers;
    final ArrayList<StopwatchTimer> mFullWifiLockTimers;
    boolean mGlobalWifiRunning;
    StopwatchTimer mGlobalWifiRunningTimer;
    int mGpsNesting;
    public final MyHandler mHandler;
    boolean mHasBluetoothReporting;
    boolean mHasModemReporting;
    boolean mHasWifiReporting;
    boolean mHaveBatteryLevel;
    int mHighDischargeAmountSinceCharge;
    HistoryItem mHistory;
    final HistoryItem mHistoryAddTmp;
    long mHistoryBaseTime;
    final Parcel mHistoryBuffer;
    int mHistoryBufferLastPos;
    HistoryItem mHistoryCache;
    final HistoryItem mHistoryCur;
    HistoryItem mHistoryEnd;
    private HistoryItem mHistoryIterator;
    HistoryItem mHistoryLastEnd;
    final HistoryItem mHistoryLastLastWritten;
    final HistoryItem mHistoryLastWritten;
    boolean mHistoryOverflow;
    final HistoryItem mHistoryReadTmp;
    final HashMap<HistoryTag, Integer> mHistoryTagPool;
    int mInitStepMode;
    private String mInitialAcquireWakeName;
    private int mInitialAcquireWakeUid;
    boolean mInteractive;
    StopwatchTimer mInteractiveTimer;
    final SparseIntArray mIsolatedUids;
    private boolean mIteratingHistory;
    private KernelCpuSpeedReader[] mKernelCpuSpeedReaders;
    private final KernelMemoryBandwidthStats mKernelMemoryBandwidthStats;
    private final LongSparseArray<SamplingTimer> mKernelMemoryStats;
    private final KernelUidCpuFreqTimeReader mKernelUidCpuFreqTimeReader;
    private final KernelUidCpuTimeReader mKernelUidCpuTimeReader;
    private final KernelWakelockReader mKernelWakelockReader;
    private final HashMap<String, SamplingTimer> mKernelWakelockStats;
    int mLastChargeStepLevel;
    int mLastChargingStateLevel;
    int mLastDischargeStepLevel;
    long mLastHistoryElapsedRealtime;
    HistoryStepDetails mLastHistoryStepDetails;
    byte mLastHistoryStepLevel;
    long mLastIdleTimeStart;
    @GuardedBy("mModemNetworkLock")
    private NetworkStats mLastModemNetworkStats;
    final ArrayList<StopwatchTimer> mLastPartialTimers;
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
    private final Pool<NetworkStats> mNetworkStatsPool;
    int mNextHistoryTagIdx;
    long mNextMaxDailyDeadline;
    long mNextMinDailyDeadline;
    boolean mNoAutoReset;
    private int mNumConnectivityChange;
    int mNumHistoryItems;
    int mNumHistoryTagChars;
    boolean mOnBattery;
    boolean mOnBatteryInternal;
    final TimeBase mOnBatteryScreenOffTimeBase;
    protected final TimeBase mOnBatteryTimeBase;
    final ArrayList<StopwatchTimer> mPartialTimers;
    Parcel mPendingWrite;
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
    private PowerProfile mPowerProfile;
    boolean mPowerSaveModeEnabled;
    StopwatchTimer mPowerSaveModeEnabledTimer;
    boolean mPretendScreenOff;
    int mReadHistoryChars;
    final HistoryStepDetails mReadHistoryStepDetails;
    String[] mReadHistoryStrings;
    int[] mReadHistoryUids;
    private boolean mReadOverflow;
    long mRealtime;
    long mRealtimeStart;
    public boolean mRecordAllHistory;
    boolean mRecordingHistory;
    int mScreenBrightnessBin;
    final StopwatchTimer[] mScreenBrightnessTimer;
    StopwatchTimer mScreenOnTimer;
    int mScreenState;
    int mSensorNesting;
    final SparseArray<ArrayList<StopwatchTimer>> mSensorTimers;
    boolean mShuttingDown;
    long mStartClockTime;
    int mStartCount;
    String mStartPlatformVersion;
    long mTempTotalCpuSystemTimeUs;
    long mTempTotalCpuUserTimeUs;
    final HistoryStepDetails mTmpHistoryStepDetails;
    private final KernelWakelockStats mTmpWakelockStats;
    long mTrackRunningHistoryElapsedRealtime;
    long mTrackRunningHistoryUptime;
    final SparseArray<Uid> mUidStats;
    private int mUnpluggedNumConnectivityChange;
    long mUptime;
    long mUptimeStart;
    int mVideoOnNesting;
    StopwatchTimer mVideoOnTimer;
    final ArrayList<StopwatchTimer> mVideoTurnedOnTimers;
    boolean mWakeLockImportant;
    int mWakeLockNesting;
    private final HashMap<String, SamplingTimer> mWakeupReasonStats;
    ControllerActivityCounterImpl mWifiActivity;
    final SparseArray<ArrayList<StopwatchTimer>> mWifiBatchedScanTimers;
    int mWifiFullLockNesting;
    @GuardedBy("mWifiNetworkLock")
    private String[] mWifiIfaces;
    int mWifiMulticastNesting;
    final ArrayList<StopwatchTimer> mWifiMulticastTimers;
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

    public interface TimeBaseObs {
        void onTimeStarted(long j, long j2, long j3);

        void onTimeStopped(long j, long j2, long j3);
    }

    public static abstract class Timer extends android.os.BatteryStats.Timer implements TimeBaseObs {
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

        protected abstract int computeCurrentCountLocked();

        protected abstract long computeRunTimeLocked(long j);

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
            pw.println(prefix + "mTotalTime=" + this.mTotalTime + " mLoadedTime=" + this.mLoadedTime);
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
            this.mLastAddedDuration = durationMillis * 1000;
            if (this.mInDischarge) {
                this.mTotalTime += this.mLastAddedDuration;
                this.mCount++;
            }
        }

        public void abortLastDuration(BatteryStatsImpl stats) {
            recomputeLastDuration(this.mClocks.elapsedRealtime() * 1000, true);
        }

        protected int computeCurrentCountLocked() {
            return this.mCount;
        }

        protected long computeRunTimeLocked(long curBatteryRealtime) {
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
            if (stillActive) {
                detachIfReset = false;
            }
            super.reset(detachIfReset);
            return stillActive ^ 1;
        }
    }

    public interface BatteryCallback {
        void batteryNeedsCpuUpdate();

        void batteryPowerChanged(boolean z);

        void batterySendBroadcast(Intent intent);
    }

    public interface Clocks {
        long elapsedRealtime();

        long uptimeMillis();
    }

    public static class ControllerActivityCounterImpl extends ControllerActivityCounter implements Parcelable {
        private final LongSamplingCounter mIdleTimeMillis;
        private final LongSamplingCounter mPowerDrainMaMs;
        private final LongSamplingCounter mRxTimeMillis;
        private final LongSamplingCounter[] mTxTimeMillis;

        public ControllerActivityCounterImpl(TimeBase timeBase, int numTxStates) {
            this.mIdleTimeMillis = new LongSamplingCounter(timeBase);
            this.mRxTimeMillis = new LongSamplingCounter(timeBase);
            this.mTxTimeMillis = new LongSamplingCounter[numTxStates];
            for (int i = 0; i < numTxStates; i++) {
                this.mTxTimeMillis[i] = new LongSamplingCounter(timeBase);
            }
            this.mPowerDrainMaMs = new LongSamplingCounter(timeBase);
        }

        public ControllerActivityCounterImpl(TimeBase timeBase, int numTxStates, Parcel in) {
            this.mIdleTimeMillis = new LongSamplingCounter(timeBase, in);
            this.mRxTimeMillis = new LongSamplingCounter(timeBase, in);
            if (in.readInt() != numTxStates) {
                throw new ParcelFormatException("inconsistent tx state lengths");
            }
            this.mTxTimeMillis = new LongSamplingCounter[numTxStates];
            for (int i = 0; i < numTxStates; i++) {
                this.mTxTimeMillis[i] = new LongSamplingCounter(timeBase, in);
            }
            this.mPowerDrainMaMs = new LongSamplingCounter(timeBase, in);
        }

        public void readSummaryFromParcel(Parcel in) {
            this.mIdleTimeMillis.readSummaryFromParcelLocked(in);
            this.mRxTimeMillis.readSummaryFromParcelLocked(in);
            if (in.readInt() != this.mTxTimeMillis.length) {
                throw new ParcelFormatException("inconsistent tx state lengths");
            }
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.readSummaryFromParcelLocked(in);
            }
            this.mPowerDrainMaMs.readSummaryFromParcelLocked(in);
        }

        public int describeContents() {
            return 0;
        }

        public void writeSummaryToParcel(Parcel dest) {
            this.mIdleTimeMillis.writeSummaryFromParcelLocked(dest);
            this.mRxTimeMillis.writeSummaryFromParcelLocked(dest);
            dest.writeInt(this.mTxTimeMillis.length);
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.writeSummaryFromParcelLocked(dest);
            }
            this.mPowerDrainMaMs.writeSummaryFromParcelLocked(dest);
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.mIdleTimeMillis.writeToParcel(dest);
            this.mRxTimeMillis.writeToParcel(dest);
            dest.writeInt(this.mTxTimeMillis.length);
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.writeToParcel(dest);
            }
            this.mPowerDrainMaMs.writeToParcel(dest);
        }

        public void reset(boolean detachIfReset) {
            this.mIdleTimeMillis.reset(detachIfReset);
            this.mRxTimeMillis.reset(detachIfReset);
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.reset(detachIfReset);
            }
            this.mPowerDrainMaMs.reset(detachIfReset);
        }

        public void detach() {
            this.mIdleTimeMillis.detach();
            this.mRxTimeMillis.detach();
            for (LongSamplingCounter counter : this.mTxTimeMillis) {
                counter.detach();
            }
            this.mPowerDrainMaMs.detach();
        }

        public LongSamplingCounter getIdleTimeCounter() {
            return this.mIdleTimeMillis;
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

    public static class Counter extends android.os.BatteryStats.Counter implements TimeBaseObs {
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

        public void stepAtomic() {
            if (this.mTimeBase.isRunning()) {
                this.mCount.incrementAndGet();
            }
        }

        void addAtomic(int delta) {
            if (this.mTimeBase.isRunning()) {
                this.mCount.addAndGet(delta);
            }
        }

        void reset(boolean detachIfReset) {
            this.mCount.set(0);
            this.mUnpluggedCount = 0;
            this.mPluggedCount = 0;
            this.mLoadedCount = 0;
            if (detachIfReset) {
                detach();
            }
        }

        void detach() {
            this.mTimeBase.remove(this);
        }

        public void writeSummaryFromParcelLocked(Parcel out) {
            out.writeInt(this.mCount.get());
        }

        public void readSummaryFromParcelLocked(Parcel in) {
            this.mLoadedCount = in.readInt();
            this.mCount.set(this.mLoadedCount);
            int i = this.mLoadedCount;
            this.mPluggedCount = i;
            this.mUnpluggedCount = i;
        }
    }

    public static class StopwatchTimer extends Timer {
        long mAcquireTime = -1;
        boolean mInList;
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
                StopwatchTimer t = (StopwatchTimer) pool.get(i);
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

        protected long computeRunTimeLocked(long curBatteryRealtime) {
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
                j = curBatteryRealtime - this.mUpdateTime;
                if (size <= 0) {
                    size = 1;
                }
                j /= (long) size;
            }
            return j + j2;
        }

        protected int computeCurrentCountLocked() {
            return this.mCount;
        }

        public boolean reset(boolean detachIfReset) {
            boolean canDetach = this.mNesting <= 0;
            if (!canDetach) {
                detachIfReset = false;
            }
            super.reset(detachIfReset);
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

    public static class DurationTimer extends StopwatchTimer {
        long mCurrentDurationMs;
        long mMaxDurationMs;
        long mStartTimeMs = -1;
        long mTotalDurationMs;

        public DurationTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase, Parcel in) {
            super(clocks, uid, type, timerPool, timeBase, in);
            this.mMaxDurationMs = in.readLong();
            this.mTotalDurationMs = in.readLong();
        }

        public DurationTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase) {
            super(clocks, uid, type, timerPool, timeBase);
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            super.writeToParcel(out, elapsedRealtimeUs);
            out.writeLong(getMaxDurationMsLocked(elapsedRealtimeUs / 1000));
            out.writeLong(getTotalDurationMsLocked(elapsedRealtimeUs / 1000));
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

    public static class DualTimer extends DurationTimer {
        private final DurationTimer mSubTimer;

        public DualTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase, TimeBase subTimeBase, Parcel in) {
            super(clocks, uid, type, timerPool, timeBase, in);
            this.mSubTimer = new DurationTimer(clocks, uid, type, null, subTimeBase, in);
        }

        public DualTimer(Clocks clocks, Uid uid, int type, ArrayList<StopwatchTimer> timerPool, TimeBase timeBase, TimeBase subTimeBase) {
            super(clocks, uid, type, timerPool, timeBase);
            this.mSubTimer = new DurationTimer(clocks, uid, type, null, subTimeBase);
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
            return ((this.mSubTimer.reset(false) ^ 1) | (super.reset(detachIfReset) ^ 1)) ^ 1;
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

    public interface ExternalStatsSync {
        public static final int UPDATE_ALL = 15;
        public static final int UPDATE_BT = 8;
        public static final int UPDATE_CPU = 1;
        public static final int UPDATE_RADIO = 4;
        public static final int UPDATE_WIFI = 2;

        void scheduleCpuSyncDueToRemovedUid(int i);

        void scheduleSync(String str, int i);
    }

    public static class LongSamplingCounter extends LongCounter implements TimeBaseObs {
        long mCount;
        long mLoadedCount;
        long mPluggedCount;
        final TimeBase mTimeBase;
        long mUnpluggedCount;

        LongSamplingCounter(TimeBase timeBase, Parcel in) {
            this.mTimeBase = timeBase;
            this.mPluggedCount = in.readLong();
            this.mCount = this.mPluggedCount;
            this.mLoadedCount = in.readLong();
            this.mUnpluggedCount = in.readLong();
            timeBase.add(this);
        }

        LongSamplingCounter(TimeBase timeBase) {
            this.mTimeBase = timeBase;
            timeBase.add(this);
        }

        public void writeToParcel(Parcel out) {
            out.writeLong(this.mCount);
            out.writeLong(this.mLoadedCount);
            out.writeLong(this.mUnpluggedCount);
        }

        public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mUnpluggedCount = this.mPluggedCount;
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mPluggedCount = this.mCount;
        }

        public long getCountLocked(int which) {
            long val = this.mTimeBase.isRunning() ? this.mCount : this.mPluggedCount;
            if (which == 2) {
                return val - this.mUnpluggedCount;
            }
            if (which != 0) {
                return val - this.mLoadedCount;
            }
            return val;
        }

        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCount=" + this.mCount + " mLoadedCount=" + this.mLoadedCount + " mUnpluggedCount=" + this.mUnpluggedCount + " mPluggedCount=" + this.mPluggedCount);
        }

        void addCountLocked(long count) {
            if (this.mTimeBase.isRunning()) {
                this.mCount += count;
            }
        }

        void reset(boolean detachIfReset) {
            this.mCount = 0;
            this.mUnpluggedCount = 0;
            this.mPluggedCount = 0;
            this.mLoadedCount = 0;
            if (detachIfReset) {
                detach();
            }
        }

        void detach() {
            this.mTimeBase.remove(this);
        }

        void writeSummaryFromParcelLocked(Parcel out) {
            out.writeLong(this.mCount);
        }

        void readSummaryFromParcelLocked(Parcel in) {
            this.mLoadedCount = in.readLong();
            this.mCount = this.mLoadedCount;
            long j = this.mLoadedCount;
            this.mPluggedCount = j;
            this.mUnpluggedCount = j;
        }
    }

    public static class LongSamplingCounterArray extends LongCounterArray implements TimeBaseObs {
        public long[] mCounts;
        public long[] mLoadedCounts;
        public long[] mPluggedCounts;
        final TimeBase mTimeBase;
        public long[] mUnpluggedCounts;

        private LongSamplingCounterArray(TimeBase timeBase, Parcel in) {
            this.mTimeBase = timeBase;
            this.mPluggedCounts = in.createLongArray();
            this.mCounts = copyArray(this.mPluggedCounts, this.mCounts);
            this.mLoadedCounts = in.createLongArray();
            this.mUnpluggedCounts = in.createLongArray();
            timeBase.add(this);
        }

        public LongSamplingCounterArray(TimeBase timeBase) {
            this.mTimeBase = timeBase;
            timeBase.add(this);
        }

        private void writeToParcel(Parcel out) {
            out.writeLongArray(this.mCounts);
            out.writeLongArray(this.mLoadedCounts);
            out.writeLongArray(this.mUnpluggedCounts);
        }

        public void onTimeStarted(long elapsedRealTime, long baseUptime, long baseRealtime) {
            this.mUnpluggedCounts = copyArray(this.mPluggedCounts, this.mUnpluggedCounts);
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mPluggedCounts = copyArray(this.mCounts, this.mPluggedCounts);
        }

        public long[] getCountsLocked(int which) {
            long[] val = copyArray(this.mTimeBase.isRunning() ? this.mCounts : this.mPluggedCounts, null);
            if (which == 2) {
                subtract(val, this.mUnpluggedCounts);
            } else if (which != 0) {
                subtract(val, this.mLoadedCounts);
            }
            return val;
        }

        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCounts=" + Arrays.toString(this.mCounts) + " mLoadedCounts=" + Arrays.toString(this.mLoadedCounts) + " mUnpluggedCounts=" + Arrays.toString(this.mUnpluggedCounts) + " mPluggedCounts=" + Arrays.toString(this.mPluggedCounts));
        }

        public void addCountLocked(long[] counts) {
            if (counts != null && this.mTimeBase.isRunning()) {
                if (this.mCounts == null) {
                    this.mCounts = new long[counts.length];
                }
                for (int i = 0; i < counts.length; i++) {
                    long[] jArr = this.mCounts;
                    jArr[i] = jArr[i] + counts[i];
                }
            }
        }

        public void reset(boolean detachIfReset) {
            fillArray(this.mCounts, 0);
            fillArray(this.mLoadedCounts, 0);
            fillArray(this.mPluggedCounts, 0);
            fillArray(this.mUnpluggedCounts, 0);
            if (detachIfReset) {
                detach();
            }
        }

        public void detach() {
            this.mTimeBase.remove(this);
        }

        private void writeSummaryToParcelLocked(Parcel out) {
            out.writeLongArray(this.mCounts);
        }

        private void readSummaryFromParcelLocked(Parcel in) {
            this.mCounts = in.createLongArray();
            this.mLoadedCounts = copyArray(this.mCounts, this.mLoadedCounts);
            this.mUnpluggedCounts = copyArray(this.mCounts, this.mUnpluggedCounts);
            this.mPluggedCounts = copyArray(this.mCounts, this.mPluggedCounts);
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
            boolean z = false;
            BatteryCallback cb = BatteryStatsImpl.this.mCallback;
            switch (msg.what) {
                case 1:
                    synchronized (BatteryStatsImpl.this) {
                        BatteryStatsImpl.this.updateCpuTimeLocked(false);
                    }
                    if (cb != null) {
                        cb.batteryNeedsCpuUpdate();
                        return;
                    }
                    return;
                case 2:
                    if (cb != null) {
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        cb.batteryPowerChanged(z);
                        return;
                    }
                    return;
                case 3:
                    if (cb != null) {
                        String action;
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
        final ArrayMap<String, T> mMap = new ArrayMap();
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
                name = LogException.NO_VALUE;
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
            } else if (this.mCurOverflowWeixin == null || (this.mMap.containsKey(OVERFLOW_WEIXIN) ^ 1) != 0) {
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
            } else if (this.mCurOverflow == null || (this.mMap.containsKey(OVERFLOW_NAME) ^ 1) != 0) {
                Slog.wtf(BatteryStatsImpl.TAG, "Cleaning up with active overflow, but no overflow entry: cur=" + this.mCurOverflow + " map=" + this.mMap.get(OVERFLOW_NAME));
            }
        }

        public T startObject(String name) {
            if (name == null) {
                name = LogException.NO_VALUE;
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            MutableInt over;
            if (this.mActiveOverflowWeixin != null) {
                over = (MutableInt) this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    obj = this.mCurOverflowWeixin;
                    if (obj == null) {
                        Slog.wtf(BatteryStatsImpl.TAG, "Have active overflow " + name + " but null overflow weixin");
                        obj = instantiateObject();
                        this.mCurOverflowWeixin = obj;
                        this.mMap.put(OVERFLOW_WEIXIN, obj);
                    }
                    over.value++;
                    return obj;
                }
            }
            if (name.startsWith("WakerLock:")) {
                this.M++;
                if (this.M > 60) {
                    obj = this.mCurOverflowWeixin;
                    if (obj == null) {
                        obj = instantiateObject();
                        this.mCurOverflowWeixin = obj;
                        this.mMap.put(OVERFLOW_WEIXIN, obj);
                    }
                    if (this.mActiveOverflowWeixin == null) {
                        this.mActiveOverflowWeixin = new ArrayMap();
                    }
                    this.mActiveOverflowWeixin.put(name, new MutableInt(1));
                    return obj;
                }
            }
            if (this.mActiveOverflow != null) {
                over = (MutableInt) this.mActiveOverflow.get(name);
                if (over != null) {
                    obj = this.mCurOverflow;
                    if (obj == null) {
                        Slog.wtf(BatteryStatsImpl.TAG, "Have active overflow " + name + " but null overflow");
                        obj = instantiateObject();
                        this.mCurOverflow = obj;
                        this.mMap.put(OVERFLOW_NAME, obj);
                    }
                    over.value++;
                    return obj;
                }
            }
            if (this.mMap.size() >= 100) {
                Slog.i(BatteryStatsImpl.TAG, "wakelocks more than 100, name: " + name);
                obj = this.mCurOverflow;
                if (obj == null) {
                    obj = instantiateObject();
                    this.mCurOverflow = obj;
                    this.mMap.put(OVERFLOW_NAME, obj);
                }
                if (this.mActiveOverflow == null) {
                    this.mActiveOverflow = new ArrayMap();
                }
                this.mActiveOverflow.put(name, new MutableInt(1));
                this.mLastOverflowTime = SystemClock.elapsedRealtime();
                return obj;
            }
            obj = instantiateObject();
            this.mMap.put(name, obj);
            return obj;
        }

        public T stopObject(String name) {
            if (name == null) {
                name = LogException.NO_VALUE;
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            MutableInt over;
            if (this.mActiveOverflowWeixin != null) {
                over = (MutableInt) this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    obj = this.mCurOverflowWeixin;
                    if (obj != null) {
                        over.value--;
                        if (over.value <= 0) {
                            this.mActiveOverflowWeixin.remove(name);
                        }
                        return obj;
                    }
                }
            }
            if (this.mActiveOverflow != null) {
                over = (MutableInt) this.mActiveOverflow.get(name);
                if (over != null) {
                    obj = this.mCurOverflow;
                    if (obj != null) {
                        over.value--;
                        if (over.value <= 0) {
                            this.mActiveOverflow.remove(name);
                            this.mLastOverflowFinishTime = SystemClock.elapsedRealtime();
                        }
                        return obj;
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
        String getPlatformLowPowerStats();
    }

    public static class SamplingTimer extends Timer {
        int mCurrentReportedCount;
        long mCurrentReportedTotalTime;
        boolean mTimeBaseRunning;
        boolean mTrackingReportedValues;
        int mUnpluggedReportedCount;
        long mUnpluggedReportedTotalTime;
        int mUpdateVersion;

        public SamplingTimer(Clocks clocks, TimeBase timeBase, Parcel in) {
            boolean z = true;
            super(clocks, 0, timeBase, in);
            this.mCurrentReportedCount = in.readInt();
            this.mUnpluggedReportedCount = in.readInt();
            this.mCurrentReportedTotalTime = in.readLong();
            this.mUnpluggedReportedTotalTime = in.readLong();
            if (in.readInt() != 1) {
                z = false;
            }
            this.mTrackingReportedValues = z;
            this.mTimeBaseRunning = timeBase.isRunning();
        }

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
            if (this.mTimeBaseRunning && (this.mTrackingReportedValues ^ 1) != 0) {
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

        protected long computeRunTimeLocked(long curBatteryRealtime) {
            long j = this.mTotalTime;
            long j2 = (this.mTimeBaseRunning && this.mTrackingReportedValues) ? this.mCurrentReportedTotalTime - this.mUnpluggedReportedTotalTime : 0;
            return j2 + j;
        }

        protected int computeCurrentCountLocked() {
            int i = this.mCount;
            int i2 = (this.mTimeBaseRunning && this.mTrackingReportedValues) ? this.mCurrentReportedCount - this.mUnpluggedReportedCount : 0;
            return i2 + i;
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

    public static class SystemClocks implements Clocks {
        public long elapsedRealtime() {
            return SystemClock.elapsedRealtime();
        }

        public long uptimeMillis() {
            return SystemClock.uptimeMillis();
        }
    }

    public static class TimeBase {
        protected final ArrayList<TimeBaseObs> mObservers = new ArrayList();
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
            BatteryStatsImpl.formatTimeMs(sb, this.mUptime / 1000);
            pw.println(sb.toString());
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mRealtime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mRealtime / 1000);
            pw.println(sb.toString());
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mPastUptime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mPastUptime / 1000);
            sb.append("mUptimeStart=");
            BatteryStatsImpl.formatTimeMs(sb, this.mUptimeStart / 1000);
            sb.append("mUnpluggedUptime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mUnpluggedUptime / 1000);
            pw.println(sb.toString());
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mPastRealtime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mPastRealtime / 1000);
            sb.append("mRealtimeStart=");
            BatteryStatsImpl.formatTimeMs(sb, this.mRealtimeStart / 1000);
            sb.append("mUnpluggedRealtime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mUnpluggedRealtime / 1000);
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
            if (this.mRunning) {
                this.mUptimeStart = uptime;
                this.mRealtimeStart = realtime;
                this.mUnpluggedUptime = getUptime(uptime);
                this.mUnpluggedRealtime = getRealtime(realtime);
                return;
            }
            this.mPastUptime = 0;
            this.mPastRealtime = 0;
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

        public synchronized boolean setRunning(boolean running, long uptime, long realtime) {
            if (this.mRunning == running) {
                return false;
            }
            this.mRunning = running;
            long batteryUptime;
            long batteryRealtime;
            int i;
            if (running) {
                this.mUptimeStart = uptime;
                this.mRealtimeStart = realtime;
                batteryUptime = getUptime(uptime);
                this.mUnpluggedUptime = batteryUptime;
                batteryRealtime = getRealtime(realtime);
                this.mUnpluggedRealtime = batteryRealtime;
                for (i = this.mObservers.size() - 1; i >= 0; i--) {
                    ((TimeBaseObs) this.mObservers.get(i)).onTimeStarted(realtime, batteryUptime, batteryRealtime);
                }
            } else {
                this.mPastUptime += uptime - this.mUptimeStart;
                this.mPastRealtime += realtime - this.mRealtimeStart;
                batteryUptime = getUptime(uptime);
                batteryRealtime = getRealtime(realtime);
                for (i = this.mObservers.size() - 1; i >= 0; i--) {
                    ((TimeBaseObs) this.mObservers.get(i)).onTimeStopped(realtime, batteryUptime, batteryRealtime);
                }
            }
            return true;
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

    public static class Uid extends android.os.BatteryStats.Uid {
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
        LongSamplingCounter[][] mCpuClusterSpeed;
        LongSamplingCounterArray mCpuFreqTimeMs;
        long mCurStepSystemTime;
        long mCurStepUserTime;
        StopwatchTimer mFlashlightTurnedOnTimer;
        StopwatchTimer mForegroundActivityTimer;
        boolean mFullWifiLockOut;
        StopwatchTimer mFullWifiLockTimer;
        final OverflowArrayMap<DualTimer> mJobStats;
        long mLastStepSystemTime;
        long mLastStepUserTime;
        LongSamplingCounter mMobileRadioActiveCount;
        LongSamplingCounter mMobileRadioActiveTime;
        private LongSamplingCounter mMobileRadioApWakeupCount;
        private ControllerActivityCounterImpl mModemControllerActivity;
        LongSamplingCounter[] mNetworkByteActivityCounters;
        LongSamplingCounter[] mNetworkPacketActivityCounters;
        public final TimeBase mOnBatteryBackgroundTimeBase;
        public final TimeBase mOnBatteryScreenOffBackgroundTimeBase;
        final ArrayMap<String, Pkg> mPackageStats = new ArrayMap();
        final SparseArray<Pid> mPids = new SparseArray();
        int mProcessState = 18;
        StopwatchTimer[] mProcessStateTimer;
        final ArrayMap<String, Proc> mProcessStats = new ArrayMap();
        LongSamplingCounterArray mScreenOffCpuFreqTimeMs;
        final SparseArray<Sensor> mSensorStats = new SparseArray();
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
        private LongSamplingCounter mWifiRadioApWakeupCount;
        boolean mWifiRunning;
        StopwatchTimer mWifiRunningTimer;
        boolean mWifiScanStarted;
        DualTimer mWifiScanTimer;

        public static class Pkg extends android.os.BatteryStats.Uid.Pkg implements TimeBaseObs {
            protected BatteryStatsImpl mBsi;
            final ArrayMap<String, Serv> mServiceStats = new ArrayMap();
            ArrayMap<String, Counter> mWakeupAlarms = new ArrayMap();

            public static class Serv extends android.os.BatteryStats.Uid.Pkg.Serv implements TimeBaseObs {
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
                    boolean z = true;
                    this.mStartTime = in.readLong();
                    this.mRunningSince = in.readLong();
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
                    int i = 1;
                    out.writeLong(this.mStartTime);
                    out.writeLong(this.mRunningSince);
                    out.writeInt(this.mRunning ? 1 : 0);
                    out.writeInt(this.mStarts);
                    out.writeLong(this.mLaunchedTime);
                    out.writeLong(this.mLaunchedSince);
                    if (!this.mLaunched) {
                        i = 0;
                    }
                    out.writeInt(i);
                    out.writeInt(this.mLaunches);
                    out.writeLong(this.mLoadedStartTime);
                    out.writeInt(this.mLoadedStarts);
                    out.writeInt(this.mLoadedLaunches);
                    out.writeLong(this.mUnpluggedStartTime);
                    out.writeInt(this.mUnpluggedStarts);
                    out.writeInt(this.mUnpluggedLaunches);
                }

                public long getLaunchTimeToNowLocked(long batteryUptime) {
                    if (this.mLaunched) {
                        return (this.mLaunchedTime + batteryUptime) - this.mLaunchedSince;
                    }
                    return this.mLaunchedTime;
                }

                public long getStartTimeToNowLocked(long batteryUptime) {
                    if (this.mRunning) {
                        return (this.mStartTime + batteryUptime) - this.mRunningSince;
                    }
                    return this.mStartTime;
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

            void detach() {
                this.mBsi.mOnBatteryScreenOffTimeBase.remove(this);
            }

            void readFromParcelLocked(Parcel in) {
                int numWA = in.readInt();
                this.mWakeupAlarms.clear();
                for (int i = 0; i < numWA; i++) {
                    this.mWakeupAlarms.put(in.readString(), new Counter(this.mBsi.mOnBatteryTimeBase, in));
                }
                int numServs = in.readInt();
                this.mServiceStats.clear();
                for (int m = 0; m < numServs; m++) {
                    String serviceName = in.readString();
                    Serv serv = new Serv(this.mBsi);
                    this.mServiceStats.put(serviceName, serv);
                    serv.readFromParcelLocked(in);
                }
            }

            void writeToParcelLocked(Parcel out) {
                int i;
                int numWA = this.mWakeupAlarms.size();
                out.writeInt(numWA);
                for (i = 0; i < numWA; i++) {
                    out.writeString((String) this.mWakeupAlarms.keyAt(i));
                    ((Counter) this.mWakeupAlarms.valueAt(i)).writeToParcel(out);
                }
                int NS = this.mServiceStats.size();
                out.writeInt(NS);
                for (i = 0; i < NS; i++) {
                    out.writeString((String) this.mServiceStats.keyAt(i));
                    ((Serv) this.mServiceStats.valueAt(i)).writeToParcelLocked(out);
                }
            }

            public ArrayMap<String, ? extends android.os.BatteryStats.Counter> getWakeupAlarmStats() {
                return this.mWakeupAlarms;
            }

            public void noteWakeupAlarmLocked(String tag) {
                Counter c = (Counter) this.mWakeupAlarms.get(tag);
                if (c == null) {
                    c = new Counter(this.mBsi.mOnBatteryTimeBase);
                    this.mWakeupAlarms.put(tag, c);
                }
                c.stepAtomic();
            }

            public ArrayMap<String, ? extends android.os.BatteryStats.Uid.Pkg.Serv> getServiceStats() {
                return this.mServiceStats;
            }

            final Serv newServiceStatsLocked() {
                return new Serv(this.mBsi);
            }
        }

        public static class Proc extends android.os.BatteryStats.Uid.Proc implements TimeBaseObs {
            boolean mActive = true;
            protected BatteryStatsImpl mBsi;
            ArrayList<ExcessivePower> mExcessivePower;
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

            void detach() {
                this.mActive = false;
                this.mBsi.mOnBatteryTimeBase.remove(this);
            }

            public int countExcessivePowers() {
                return this.mExcessivePower != null ? this.mExcessivePower.size() : 0;
            }

            public ExcessivePower getExcessivePower(int i) {
                if (this.mExcessivePower != null) {
                    return (ExcessivePower) this.mExcessivePower.get(i);
                }
                return null;
            }

            public void addExcessiveWake(long overTime, long usedTime) {
                if (this.mExcessivePower == null) {
                    this.mExcessivePower = new ArrayList();
                }
                ExcessivePower ew = new ExcessivePower();
                ew.type = 1;
                ew.overTime = overTime;
                ew.usedTime = usedTime;
                this.mExcessivePower.add(ew);
            }

            public void addExcessiveCpu(long overTime, long usedTime) {
                if (this.mExcessivePower == null) {
                    this.mExcessivePower = new ArrayList();
                }
                ExcessivePower ew = new ExcessivePower();
                ew.type = 2;
                ew.overTime = overTime;
                ew.usedTime = usedTime;
                this.mExcessivePower.add(ew);
            }

            void writeExcessivePowerToParcelLocked(Parcel out) {
                if (this.mExcessivePower == null) {
                    out.writeInt(0);
                    return;
                }
                int N = this.mExcessivePower.size();
                out.writeInt(N);
                for (int i = 0; i < N; i++) {
                    ExcessivePower ew = (ExcessivePower) this.mExcessivePower.get(i);
                    out.writeInt(ew.type);
                    out.writeLong(ew.overTime);
                    out.writeLong(ew.usedTime);
                }
            }

            void readExcessivePowerFromParcelLocked(Parcel in) {
                int N = in.readInt();
                if (N == 0) {
                    this.mExcessivePower = null;
                } else if (N > 10000) {
                    throw new ParcelFormatException("File corrupt: too many excessive power entries " + N);
                } else {
                    this.mExcessivePower = new ArrayList();
                    for (int i = 0; i < N; i++) {
                        ExcessivePower ew = new ExcessivePower();
                        ew.type = in.readInt();
                        ew.overTime = in.readLong();
                        ew.usedTime = in.readLong();
                        this.mExcessivePower.add(ew);
                    }
                }
            }

            void writeToParcelLocked(Parcel out) {
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

            void readFromParcelLocked(Parcel in) {
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
                this.mUserTime += (long) utime;
                this.mSystemTime += (long) stime;
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

        public static class Sensor extends android.os.BatteryStats.Uid.Sensor {
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
                ArrayList<StopwatchTimer> pool = (ArrayList) this.mBsi.mSensorTimers.get(this.mHandle);
                if (pool == null) {
                    pool = new ArrayList();
                    this.mBsi.mSensorTimers.put(this.mHandle, pool);
                }
                return new DualTimer(this.mBsi.mClocks, this.mUid, 0, pool, timeBase, bgTimeBase, in);
            }

            boolean reset() {
                if (!this.mTimer.reset(true)) {
                    return false;
                }
                this.mTimer = null;
                return true;
            }

            void readFromParcelLocked(TimeBase timeBase, TimeBase bgTimeBase, Parcel in) {
                this.mTimer = readTimersFromParcel(timeBase, bgTimeBase, in);
            }

            void writeToParcelLocked(Parcel out, long elapsedRealtimeUs) {
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

        public static class Wakelock extends android.os.BatteryStats.Uid.Wakelock {
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
                return new StopwatchTimer(this.mBsi.mClocks, this.mUid, type, pool, timeBase, in);
            }

            private DualTimer readDualTimerFromParcel(int type, ArrayList<StopwatchTimer> pool, TimeBase timeBase, TimeBase bgTimeBase, Parcel in) {
                if (in.readInt() == 0) {
                    return null;
                }
                return new DualTimer(this.mBsi.mClocks, this.mUid, type, pool, timeBase, bgTimeBase, in);
            }

            boolean reset() {
                int wlactive = 0;
                if (this.mTimerFull != null) {
                    wlactive = this.mTimerFull.reset(false) ^ 1;
                }
                if (this.mTimerPartial != null) {
                    wlactive |= this.mTimerPartial.reset(false) ^ 1;
                }
                if (this.mTimerWindow != null) {
                    wlactive |= this.mTimerWindow.reset(false) ^ 1;
                }
                if (this.mTimerDraw != null) {
                    wlactive |= this.mTimerDraw.reset(false) ^ 1;
                }
                if (wlactive == 0) {
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
                return wlactive ^ 1;
            }

            void readFromParcelLocked(TimeBase timeBase, TimeBase screenOffTimeBase, TimeBase screenOffBgTimeBase, Parcel in) {
                this.mTimerPartial = readDualTimerFromParcel(0, this.mBsi.mPartialTimers, screenOffTimeBase, screenOffBgTimeBase, in);
                this.mTimerFull = readStopwatchTimerFromParcel(1, this.mBsi.mFullTimers, timeBase, in);
                this.mTimerWindow = readStopwatchTimerFromParcel(2, this.mBsi.mWindowTimers, timeBase, in);
                this.mTimerDraw = readStopwatchTimerFromParcel(18, this.mBsi.mDrawTimers, timeBase, in);
            }

            void writeToParcelLocked(Parcel out, long elapsedRealtimeUs) {
                Timer.writeTimerToParcel(out, this.mTimerPartial, elapsedRealtimeUs);
                Timer.writeTimerToParcel(out, this.mTimerFull, elapsedRealtimeUs);
                Timer.writeTimerToParcel(out, this.mTimerWindow, elapsedRealtimeUs);
                Timer.writeTimerToParcel(out, this.mTimerDraw, elapsedRealtimeUs);
            }

            public Timer getWakeTime(int type) {
                switch (type) {
                    case 0:
                        return this.mTimerPartial;
                    case 1:
                        return this.mTimerFull;
                    case 2:
                        return this.mTimerWindow;
                    case 18:
                        return this.mTimerDraw;
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
            BatteryStatsImpl batteryStatsImpl = this.mBsi;
            batteryStatsImpl.getClass();
            this.mWakelockStats = new OverflowArrayMap<Wakelock>(batteryStatsImpl, uid) {
                public Wakelock instantiateObject() {
                    return new Wakelock(Uid.this.mBsi, Uid.this);
                }
            };
            batteryStatsImpl = this.mBsi;
            batteryStatsImpl.getClass();
            this.mSyncStats = new OverflowArrayMap<DualTimer>(batteryStatsImpl, uid) {
                public DualTimer instantiateObject() {
                    return new DualTimer(Uid.this.mBsi.mClocks, Uid.this, 13, null, Uid.this.mBsi.mOnBatteryTimeBase, Uid.this.mOnBatteryBackgroundTimeBase);
                }
            };
            batteryStatsImpl = this.mBsi;
            batteryStatsImpl.getClass();
            this.mJobStats = new OverflowArrayMap<DualTimer>(batteryStatsImpl, uid) {
                public DualTimer instantiateObject() {
                    return new DualTimer(Uid.this.mBsi.mClocks, Uid.this, 14, null, Uid.this.mBsi.mOnBatteryTimeBase, Uid.this.mOnBatteryBackgroundTimeBase);
                }
            };
            this.mWifiRunningTimer = new StopwatchTimer(this.mBsi.mClocks, this, 4, this.mBsi.mWifiRunningTimers, this.mBsi.mOnBatteryTimeBase);
            this.mFullWifiLockTimer = new StopwatchTimer(this.mBsi.mClocks, this, 5, this.mBsi.mFullWifiLockTimers, this.mBsi.mOnBatteryTimeBase);
            this.mWifiScanTimer = new DualTimer(this.mBsi.mClocks, this, 6, this.mBsi.mWifiScanTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
            this.mWifiBatchedScanTimer = new StopwatchTimer[5];
            this.mWifiMulticastTimer = new StopwatchTimer(this.mBsi.mClocks, this, 7, this.mBsi.mWifiMulticastTimers, this.mBsi.mOnBatteryTimeBase);
            this.mProcessStateTimer = new StopwatchTimer[6];
        }

        public long[] getCpuFreqTimes(int which) {
            if (this.mCpuFreqTimeMs == null) {
                return null;
            }
            long[] cpuFreqTimes = this.mCpuFreqTimeMs.getCountsLocked(which);
            if (cpuFreqTimes == null) {
                return null;
            }
            for (long j : cpuFreqTimes) {
                if (j != 0) {
                    return cpuFreqTimes;
                }
            }
            return null;
        }

        public long[] getScreenOffCpuFreqTimes(int which) {
            if (this.mScreenOffCpuFreqTimeMs == null) {
                return null;
            }
            long[] cpuFreqTimes = this.mScreenOffCpuFreqTimeMs.getCountsLocked(which);
            if (cpuFreqTimes == null) {
                return null;
            }
            for (long j : cpuFreqTimes) {
                if (j != 0) {
                    return cpuFreqTimes;
                }
            }
            return null;
        }

        public Timer getAggregatedPartialWakelockTimer() {
            return this.mAggregatedPartialWakelockTimer;
        }

        public ArrayMap<String, ? extends android.os.BatteryStats.Uid.Wakelock> getWakelockStats() {
            return this.mWakelockStats.getMap();
        }

        public ArrayMap<String, ? extends android.os.BatteryStats.Timer> getSyncStats() {
            return this.mSyncStats.getMap();
        }

        public ArrayMap<String, ? extends android.os.BatteryStats.Timer> getJobStats() {
            return this.mJobStats.getMap();
        }

        public SparseArray<? extends android.os.BatteryStats.Uid.Sensor> getSensorStats() {
            return this.mSensorStats;
        }

        public ArrayMap<String, ? extends android.os.BatteryStats.Uid.Proc> getProcessStats() {
            return this.mProcessStats;
        }

        public ArrayMap<String, ? extends android.os.BatteryStats.Uid.Pkg> getPackageStats() {
            return this.mPackageStats;
        }

        public int getUid() {
            return this.mUid;
        }

        public void noteWifiRunningLocked(long elapsedRealtimeMs) {
            if (!this.mWifiRunning) {
                this.mWifiRunning = true;
                if (this.mWifiRunningTimer == null) {
                    this.mWifiRunningTimer = new StopwatchTimer(this.mBsi.mClocks, this, 4, this.mBsi.mWifiRunningTimers, this.mBsi.mOnBatteryTimeBase);
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
                    this.mFullWifiLockTimer = new StopwatchTimer(this.mBsi.mClocks, this, 5, this.mBsi.mFullWifiLockTimers, this.mBsi.mOnBatteryTimeBase);
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
                    this.mWifiScanTimer = new DualTimer(this.mBsi.mClocks, this, 6, this.mBsi.mWifiScanTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
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
                    this.mWifiMulticastTimer = new StopwatchTimer(this.mBsi.mClocks, this, 7, this.mBsi.mWifiMulticastTimers, this.mBsi.mOnBatteryTimeBase);
                }
                this.mWifiMulticastTimer.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteWifiMulticastDisabledLocked(long elapsedRealtimeMs) {
            if (this.mWifiMulticastEnabled) {
                this.mWifiMulticastEnabled = false;
                this.mWifiMulticastTimer.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public ControllerActivityCounter getWifiControllerActivity() {
            return this.mWifiControllerActivity;
        }

        public ControllerActivityCounter getBluetoothControllerActivity() {
            return this.mBluetoothControllerActivity;
        }

        public ControllerActivityCounter getModemControllerActivity() {
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
                this.mAudioTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 15, this.mBsi.mAudioTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
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
                this.mVideoTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 8, this.mBsi.mVideoTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
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
                this.mFlashlightTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 16, this.mBsi.mFlashlightTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
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
                this.mCameraTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 17, this.mBsi.mCameraTurnedOnTimers, this.mBsi.mOnBatteryTimeBase);
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
                this.mForegroundActivityTimer = new StopwatchTimer(this.mBsi.mClocks, this, 10, null, this.mBsi.mOnBatteryTimeBase);
            }
            return this.mForegroundActivityTimer;
        }

        public DualTimer createAggregatedPartialWakelockTimerLocked() {
            if (this.mAggregatedPartialWakelockTimer == null) {
                this.mAggregatedPartialWakelockTimer = new DualTimer(this.mBsi.mClocks, this, 20, null, this.mBsi.mOnBatteryScreenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase);
            }
            return this.mAggregatedPartialWakelockTimer;
        }

        public DualTimer createBluetoothScanTimerLocked() {
            if (this.mBluetoothScanTimer == null) {
                this.mBluetoothScanTimer = new DualTimer(this.mBsi.mClocks, this, 19, this.mBsi.mBluetoothScanOnTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
            }
            return this.mBluetoothScanTimer;
        }

        public DualTimer createBluetoothUnoptimizedScanTimerLocked() {
            if (this.mBluetoothUnoptimizedScanTimer == null) {
                this.mBluetoothUnoptimizedScanTimer = new DualTimer(this.mBsi.mClocks, this, 21, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
            }
            return this.mBluetoothUnoptimizedScanTimer;
        }

        public void noteBluetoothScanStartedLocked(long elapsedRealtimeMs, boolean isUnoptimized) {
            createBluetoothScanTimerLocked().startRunningLocked(elapsedRealtimeMs);
            if (isUnoptimized) {
                createBluetoothUnoptimizedScanTimerLocked().startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteBluetoothScanStoppedLocked(long elapsedRealtimeMs) {
            if (this.mBluetoothScanTimer != null) {
                this.mBluetoothScanTimer.stopRunningLocked(elapsedRealtimeMs);
            }
            if (this.mBluetoothUnoptimizedScanTimer != null && this.mBluetoothUnoptimizedScanTimer.isRunningLocked()) {
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

        /* JADX WARNING: Missing block: B:4:0x0007, code:
            return 0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long getWifiBatchedScanTime(int csphBin, long elapsedRealtimeUs, int which) {
            if (csphBin < 0 || csphBin >= 5 || this.mWifiBatchedScanTimer[csphBin] == null) {
                return 0;
            }
            return this.mWifiBatchedScanTimer[csphBin].getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        /* JADX WARNING: Missing block: B:4:0x0006, code:
            return 0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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

        void makeProcessState(int i, Parcel in) {
            if (i >= 0 && i < 6) {
                if (in == null) {
                    this.mProcessStateTimer[i] = new StopwatchTimer(this.mBsi.mClocks, this, 12, null, this.mBsi.mOnBatteryTimeBase);
                } else {
                    this.mProcessStateTimer[i] = new StopwatchTimer(this.mBsi.mClocks, this, 12, null, this.mBsi.mOnBatteryTimeBase, in);
                }
            }
        }

        /* JADX WARNING: Missing block: B:4:0x0007, code:
            return 0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long getProcessStateTime(int state, long elapsedRealtimeUs, int which) {
            if (state < 0 || state >= 6 || this.mProcessStateTimer[state] == null) {
                return 0;
            }
            return this.mProcessStateTimer[state].getTotalTimeLocked(elapsedRealtimeUs, which);
        }

        public Timer getProcessStateTimer(int state) {
            if (state < 0 || state >= 6) {
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
            } else {
                this.mUserActivityCounters[type].stepAtomic();
            }
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

        void makeWifiBatchedScanBin(int i, Parcel in) {
            if (i >= 0 && i < 5) {
                ArrayList<StopwatchTimer> collected = (ArrayList) this.mBsi.mWifiBatchedScanTimers.get(i);
                if (collected == null) {
                    collected = new ArrayList();
                    this.mBsi.mWifiBatchedScanTimers.put(i, collected);
                }
                if (in == null) {
                    this.mWifiBatchedScanTimer[i] = new StopwatchTimer(this.mBsi.mClocks, this, 11, collected, this.mBsi.mOnBatteryTimeBase);
                } else {
                    this.mWifiBatchedScanTimer[i] = new StopwatchTimer(this.mBsi.mClocks, this, 11, collected, this.mBsi.mOnBatteryTimeBase, in);
                }
            }
        }

        void initUserActivityLocked() {
            this.mUserActivityCounters = new Counter[4];
            for (int i = 0; i < 4; i++) {
                this.mUserActivityCounters[i] = new Counter(this.mBsi.mOnBatteryTimeBase);
            }
        }

        void noteNetworkActivityLocked(int type, long deltaBytes, long deltaPackets) {
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

        void noteMobileRadioActiveTimeLocked(long batteryUptime) {
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
            return this.mMobileRadioActiveTime != null ? this.mMobileRadioActiveTime.getCountLocked(which) : 0;
        }

        public int getMobileRadioActiveCount(int which) {
            return this.mMobileRadioActiveCount != null ? (int) this.mMobileRadioActiveCount.getCountLocked(which) : 0;
        }

        public long getUserCpuTimeUs(int which) {
            return this.mUserCpuTime.getCountLocked(which);
        }

        public long getSystemCpuTimeUs(int which) {
            return this.mSystemCpuTime.getCountLocked(which);
        }

        public long getTimeAtCpuSpeed(int cluster, int step, int which) {
            if (this.mCpuClusterSpeed != null && cluster >= 0 && cluster < this.mCpuClusterSpeed.length) {
                LongSamplingCounter[] cpuSpeeds = this.mCpuClusterSpeed[cluster];
                if (cpuSpeeds != null && step >= 0 && step < cpuSpeeds.length) {
                    LongSamplingCounter c = cpuSpeeds[step];
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

        void initNetworkActivityLocked() {
            this.mNetworkByteActivityCounters = new LongSamplingCounter[10];
            this.mNetworkPacketActivityCounters = new LongSamplingCounter[10];
            for (int i = 0; i < 10; i++) {
                this.mNetworkByteActivityCounters[i] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
                this.mNetworkPacketActivityCounters[i] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            }
            this.mMobileRadioActiveTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
            this.mMobileRadioActiveCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase);
        }

        public boolean reset() {
            int i;
            LongSamplingCounter[][] longSamplingCounterArr;
            int i2;
            int length;
            int i3;
            DualTimer timer;
            int active = 0;
            if (this.mWifiRunningTimer != null) {
                active = (this.mWifiRunningTimer.reset(false) ^ 1) | this.mWifiRunning;
            }
            if (this.mFullWifiLockTimer != null) {
                active = (active | (this.mFullWifiLockTimer.reset(false) ^ 1)) | this.mFullWifiLockOut;
            }
            if (this.mWifiScanTimer != null) {
                active = (active | (this.mWifiScanTimer.reset(false) ^ 1)) | this.mWifiScanStarted;
            }
            if (this.mWifiBatchedScanTimer != null) {
                for (i = 0; i < 5; i++) {
                    if (this.mWifiBatchedScanTimer[i] != null) {
                        active |= this.mWifiBatchedScanTimer[i].reset(false) ^ 1;
                    }
                }
                active |= this.mWifiBatchedScanBinStarted != -1 ? 1 : 0;
            }
            if (this.mWifiMulticastTimer != null) {
                active = (active | (this.mWifiMulticastTimer.reset(false) ^ 1)) | this.mWifiMulticastEnabled;
            }
            active = (((((((active | (BatteryStatsImpl.resetTimerIfNotNull(this.mAudioTurnedOnTimer, false) ^ 1)) | (BatteryStatsImpl.resetTimerIfNotNull(this.mVideoTurnedOnTimer, false) ^ 1)) | (BatteryStatsImpl.resetTimerIfNotNull(this.mFlashlightTurnedOnTimer, false) ^ 1)) | (BatteryStatsImpl.resetTimerIfNotNull(this.mCameraTurnedOnTimer, false) ^ 1)) | (BatteryStatsImpl.resetTimerIfNotNull(this.mForegroundActivityTimer, false) ^ 1)) | (BatteryStatsImpl.resetTimerIfNotNull(this.mAggregatedPartialWakelockTimer, false) ^ 1)) | (BatteryStatsImpl.resetTimerIfNotNull(this.mBluetoothScanTimer, false) ^ 1)) | (BatteryStatsImpl.resetTimerIfNotNull(this.mBluetoothUnoptimizedScanTimer, false) ^ 1);
            if (this.mBluetoothScanResultCounter != null) {
                this.mBluetoothScanResultCounter.reset(false);
            }
            if (this.mBluetoothScanResultBgCounter != null) {
                this.mBluetoothScanResultBgCounter.reset(false);
            }
            if (this.mProcessStateTimer != null) {
                boolean active2;
                for (i = 0; i < 6; i++) {
                    if (this.mProcessStateTimer[i] != null) {
                        active2 |= this.mProcessStateTimer[i].reset(false) ^ 1;
                    }
                }
                active = active2 | (this.mProcessState != 18 ? 1 : 0);
            }
            if (this.mVibratorOnTimer != null) {
                if (this.mVibratorOnTimer.reset(false)) {
                    this.mVibratorOnTimer.detach();
                    this.mVibratorOnTimer = null;
                } else {
                    active = true;
                }
            }
            if (this.mUserActivityCounters != null) {
                for (i = 0; i < 4; i++) {
                    this.mUserActivityCounters[i].reset(false);
                }
            }
            if (this.mNetworkByteActivityCounters != null) {
                for (i = 0; i < 10; i++) {
                    this.mNetworkByteActivityCounters[i].reset(false);
                    this.mNetworkPacketActivityCounters[i].reset(false);
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
            if (this.mCpuClusterSpeed != null) {
                longSamplingCounterArr = this.mCpuClusterSpeed;
                i2 = 0;
                length = longSamplingCounterArr.length;
                while (true) {
                    i3 = i2;
                    if (i3 >= length) {
                        break;
                    }
                    LongSamplingCounter[] speeds = longSamplingCounterArr[i3];
                    if (speeds != null) {
                        for (LongSamplingCounter speed : speeds) {
                            if (speed != null) {
                                speed.reset(false);
                            }
                        }
                    }
                    i2 = i3 + 1;
                }
            }
            if (this.mCpuFreqTimeMs != null) {
                this.mCpuFreqTimeMs.reset(false);
            }
            if (this.mScreenOffCpuFreqTimeMs != null) {
                this.mScreenOffCpuFreqTimeMs.reset(false);
            }
            BatteryStatsImpl.resetLongCounterIfNotNull(this.mMobileRadioApWakeupCount, false);
            BatteryStatsImpl.resetLongCounterIfNotNull(this.mWifiRadioApWakeupCount, false);
            ArrayMap<String, Wakelock> wakeStats = this.mWakelockStats.getMap();
            for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                if (((Wakelock) wakeStats.valueAt(iw)).reset()) {
                    wakeStats.removeAt(iw);
                } else {
                    active = true;
                }
            }
            this.mWakelockStats.cleanup();
            ArrayMap<String, DualTimer> syncStats = this.mSyncStats.getMap();
            for (int is = syncStats.size() - 1; is >= 0; is--) {
                timer = (DualTimer) syncStats.valueAt(is);
                if (timer.reset(false)) {
                    syncStats.removeAt(is);
                    timer.detach();
                } else {
                    active = true;
                }
            }
            this.mSyncStats.cleanup();
            ArrayMap<String, DualTimer> jobStats = this.mJobStats.getMap();
            for (int ij = jobStats.size() - 1; ij >= 0; ij--) {
                timer = (DualTimer) jobStats.valueAt(ij);
                if (timer.reset(false)) {
                    jobStats.removeAt(ij);
                    timer.detach();
                } else {
                    active = true;
                }
            }
            this.mJobStats.cleanup();
            for (int ise = this.mSensorStats.size() - 1; ise >= 0; ise--) {
                if (((Sensor) this.mSensorStats.valueAt(ise)).reset()) {
                    this.mSensorStats.removeAt(ise);
                } else {
                    active = true;
                }
            }
            for (int ip = this.mProcessStats.size() - 1; ip >= 0; ip--) {
                ((Proc) this.mProcessStats.valueAt(ip)).detach();
            }
            this.mProcessStats.clear();
            if (this.mPids.size() > 0) {
                for (i = this.mPids.size() - 1; i >= 0; i--) {
                    if (((Pid) this.mPids.valueAt(i)).mWakeNesting > 0) {
                        active = true;
                    } else {
                        this.mPids.removeAt(i);
                    }
                }
            }
            if (this.mPackageStats.size() > 0) {
                for (Entry<String, Pkg> pkgEntry : this.mPackageStats.entrySet()) {
                    Pkg p = (Pkg) pkgEntry.getValue();
                    p.detach();
                    if (p.mServiceStats.size() > 0) {
                        for (Entry<String, Serv> servEntry : p.mServiceStats.entrySet()) {
                            ((Serv) servEntry.getValue()).detach();
                        }
                    }
                }
                this.mPackageStats.clear();
            }
            this.mLastStepSystemTime = 0;
            this.mLastStepUserTime = 0;
            this.mCurStepSystemTime = 0;
            this.mCurStepUserTime = 0;
            this.mOnBatteryBackgroundTimeBase.reset(this.mBsi.mClocks.elapsedRealtime() * 1000, this.mBsi.mClocks.uptimeMillis() * 1000);
            this.mOnBatteryScreenOffBackgroundTimeBase.reset(this.mBsi.mClocks.elapsedRealtime() * 1000, this.mBsi.mClocks.uptimeMillis() * 1000);
            if (active == 0) {
                if (this.mWifiRunningTimer != null) {
                    this.mWifiRunningTimer.detach();
                }
                if (this.mFullWifiLockTimer != null) {
                    this.mFullWifiLockTimer.detach();
                }
                if (this.mWifiScanTimer != null) {
                    this.mWifiScanTimer.detach();
                }
                for (i = 0; i < 5; i++) {
                    if (this.mWifiBatchedScanTimer[i] != null) {
                        this.mWifiBatchedScanTimer[i].detach();
                    }
                }
                if (this.mWifiMulticastTimer != null) {
                    this.mWifiMulticastTimer.detach();
                }
                if (this.mAudioTurnedOnTimer != null) {
                    this.mAudioTurnedOnTimer.detach();
                    this.mAudioTurnedOnTimer = null;
                }
                if (this.mVideoTurnedOnTimer != null) {
                    this.mVideoTurnedOnTimer.detach();
                    this.mVideoTurnedOnTimer = null;
                }
                if (this.mFlashlightTurnedOnTimer != null) {
                    this.mFlashlightTurnedOnTimer.detach();
                    this.mFlashlightTurnedOnTimer = null;
                }
                if (this.mCameraTurnedOnTimer != null) {
                    this.mCameraTurnedOnTimer.detach();
                    this.mCameraTurnedOnTimer = null;
                }
                if (this.mForegroundActivityTimer != null) {
                    this.mForegroundActivityTimer.detach();
                    this.mForegroundActivityTimer = null;
                }
                if (this.mAggregatedPartialWakelockTimer != null) {
                    this.mAggregatedPartialWakelockTimer.detach();
                    this.mAggregatedPartialWakelockTimer = null;
                }
                if (this.mBluetoothScanTimer != null) {
                    this.mBluetoothScanTimer.detach();
                    this.mBluetoothScanTimer = null;
                }
                if (this.mBluetoothUnoptimizedScanTimer != null) {
                    this.mBluetoothUnoptimizedScanTimer.detach();
                    this.mBluetoothUnoptimizedScanTimer = null;
                }
                if (this.mBluetoothScanResultCounter != null) {
                    this.mBluetoothScanResultCounter.detach();
                    this.mBluetoothScanResultCounter = null;
                }
                if (this.mBluetoothScanResultBgCounter != null) {
                    this.mBluetoothScanResultBgCounter.detach();
                    this.mBluetoothScanResultBgCounter = null;
                }
                if (this.mUserActivityCounters != null) {
                    for (i = 0; i < 4; i++) {
                        this.mUserActivityCounters[i].detach();
                    }
                }
                if (this.mNetworkByteActivityCounters != null) {
                    for (i = 0; i < 10; i++) {
                        this.mNetworkByteActivityCounters[i].detach();
                        this.mNetworkPacketActivityCounters[i].detach();
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
                if (this.mCpuClusterSpeed != null) {
                    longSamplingCounterArr = this.mCpuClusterSpeed;
                    i2 = 0;
                    length = longSamplingCounterArr.length;
                    while (true) {
                        i3 = i2;
                        if (i3 >= length) {
                            break;
                        }
                        LongSamplingCounter[] cpuSpeeds = longSamplingCounterArr[i3];
                        if (cpuSpeeds != null) {
                            for (LongSamplingCounter c : cpuSpeeds) {
                                if (c != null) {
                                    c.detach();
                                }
                            }
                        }
                        i2 = i3 + 1;
                    }
                }
                if (this.mCpuFreqTimeMs != null) {
                    this.mCpuFreqTimeMs.detach();
                }
                if (this.mScreenOffCpuFreqTimeMs != null) {
                    this.mScreenOffCpuFreqTimeMs.detach();
                }
                BatteryStatsImpl.detachLongCounterIfNotNull(this.mMobileRadioApWakeupCount);
                BatteryStatsImpl.detachLongCounterIfNotNull(this.mWifiRadioApWakeupCount);
            }
            return active ^ 1;
        }

        void writeToParcelLocked(Parcel out, long uptimeUs, long elapsedRealtimeUs) {
            int i;
            this.mOnBatteryBackgroundTimeBase.writeToParcel(out, uptimeUs, elapsedRealtimeUs);
            this.mOnBatteryScreenOffBackgroundTimeBase.writeToParcel(out, uptimeUs, elapsedRealtimeUs);
            ArrayMap<String, Wakelock> wakeStats = this.mWakelockStats.getMap();
            int NW = wakeStats.size();
            out.writeInt(NW);
            for (int iw = 0; iw < NW; iw++) {
                out.writeString((String) wakeStats.keyAt(iw));
                ((Wakelock) wakeStats.valueAt(iw)).writeToParcelLocked(out, elapsedRealtimeUs);
            }
            ArrayMap<String, DualTimer> syncStats = this.mSyncStats.getMap();
            int NS = syncStats.size();
            out.writeInt(NS);
            for (int is = 0; is < NS; is++) {
                out.writeString((String) syncStats.keyAt(is));
                Timer.writeTimerToParcel(out, (DualTimer) syncStats.valueAt(is), elapsedRealtimeUs);
            }
            ArrayMap<String, DualTimer> jobStats = this.mJobStats.getMap();
            int NJ = jobStats.size();
            out.writeInt(NJ);
            for (int ij = 0; ij < NJ; ij++) {
                out.writeString((String) jobStats.keyAt(ij));
                Timer.writeTimerToParcel(out, (DualTimer) jobStats.valueAt(ij), elapsedRealtimeUs);
            }
            int NSE = this.mSensorStats.size();
            out.writeInt(NSE);
            for (int ise = 0; ise < NSE; ise++) {
                out.writeInt(this.mSensorStats.keyAt(ise));
                ((Sensor) this.mSensorStats.valueAt(ise)).writeToParcelLocked(out, elapsedRealtimeUs);
            }
            int NP = this.mProcessStats.size();
            out.writeInt(NP);
            for (int ip = 0; ip < NP; ip++) {
                out.writeString((String) this.mProcessStats.keyAt(ip));
                ((Proc) this.mProcessStats.valueAt(ip)).writeToParcelLocked(out);
            }
            out.writeInt(this.mPackageStats.size());
            for (Entry<String, Pkg> pkgEntry : this.mPackageStats.entrySet()) {
                out.writeString((String) pkgEntry.getKey());
                ((Pkg) pkgEntry.getValue()).writeToParcelLocked(out);
            }
            if (this.mWifiRunningTimer != null) {
                out.writeInt(1);
                this.mWifiRunningTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mFullWifiLockTimer != null) {
                out.writeInt(1);
                this.mFullWifiLockTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mWifiScanTimer != null) {
                out.writeInt(1);
                this.mWifiScanTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            for (i = 0; i < 5; i++) {
                if (this.mWifiBatchedScanTimer[i] != null) {
                    out.writeInt(1);
                    this.mWifiBatchedScanTimer[i].writeToParcel(out, elapsedRealtimeUs);
                } else {
                    out.writeInt(0);
                }
            }
            if (this.mWifiMulticastTimer != null) {
                out.writeInt(1);
                this.mWifiMulticastTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mAudioTurnedOnTimer != null) {
                out.writeInt(1);
                this.mAudioTurnedOnTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mVideoTurnedOnTimer != null) {
                out.writeInt(1);
                this.mVideoTurnedOnTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mFlashlightTurnedOnTimer != null) {
                out.writeInt(1);
                this.mFlashlightTurnedOnTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mCameraTurnedOnTimer != null) {
                out.writeInt(1);
                this.mCameraTurnedOnTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mForegroundActivityTimer != null) {
                out.writeInt(1);
                this.mForegroundActivityTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mAggregatedPartialWakelockTimer != null) {
                out.writeInt(1);
                this.mAggregatedPartialWakelockTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mBluetoothScanTimer != null) {
                out.writeInt(1);
                this.mBluetoothScanTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mBluetoothUnoptimizedScanTimer != null) {
                out.writeInt(1);
                this.mBluetoothUnoptimizedScanTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mBluetoothScanResultCounter != null) {
                out.writeInt(1);
                this.mBluetoothScanResultCounter.writeToParcel(out);
            } else {
                out.writeInt(0);
            }
            if (this.mBluetoothScanResultBgCounter != null) {
                out.writeInt(1);
                this.mBluetoothScanResultBgCounter.writeToParcel(out);
            } else {
                out.writeInt(0);
            }
            for (i = 0; i < 6; i++) {
                if (this.mProcessStateTimer[i] != null) {
                    out.writeInt(1);
                    this.mProcessStateTimer[i].writeToParcel(out, elapsedRealtimeUs);
                } else {
                    out.writeInt(0);
                }
            }
            if (this.mVibratorOnTimer != null) {
                out.writeInt(1);
                this.mVibratorOnTimer.writeToParcel(out, elapsedRealtimeUs);
            } else {
                out.writeInt(0);
            }
            if (this.mUserActivityCounters != null) {
                out.writeInt(1);
                for (i = 0; i < 4; i++) {
                    this.mUserActivityCounters[i].writeToParcel(out);
                }
            } else {
                out.writeInt(0);
            }
            if (this.mNetworkByteActivityCounters != null) {
                out.writeInt(1);
                for (i = 0; i < 10; i++) {
                    this.mNetworkByteActivityCounters[i].writeToParcel(out);
                    this.mNetworkPacketActivityCounters[i].writeToParcel(out);
                }
                this.mMobileRadioActiveTime.writeToParcel(out);
                this.mMobileRadioActiveCount.writeToParcel(out);
            } else {
                out.writeInt(0);
            }
            if (this.mWifiControllerActivity != null) {
                out.writeInt(1);
                this.mWifiControllerActivity.writeToParcel(out, 0);
            } else {
                out.writeInt(0);
            }
            if (this.mBluetoothControllerActivity != null) {
                out.writeInt(1);
                this.mBluetoothControllerActivity.writeToParcel(out, 0);
            } else {
                out.writeInt(0);
            }
            if (this.mModemControllerActivity != null) {
                out.writeInt(1);
                this.mModemControllerActivity.writeToParcel(out, 0);
            } else {
                out.writeInt(0);
            }
            this.mUserCpuTime.writeToParcel(out);
            this.mSystemCpuTime.writeToParcel(out);
            if (this.mCpuClusterSpeed != null) {
                out.writeInt(1);
                out.writeInt(this.mCpuClusterSpeed.length);
                for (LongSamplingCounter[] cpuSpeeds : this.mCpuClusterSpeed) {
                    if (cpuSpeeds != null) {
                        out.writeInt(1);
                        out.writeInt(cpuSpeeds.length);
                        for (LongSamplingCounter c : cpuSpeeds) {
                            if (c != null) {
                                out.writeInt(1);
                                c.writeToParcel(out);
                            } else {
                                out.writeInt(0);
                            }
                        }
                    } else {
                        out.writeInt(0);
                    }
                }
            } else {
                out.writeInt(0);
            }
            LongSamplingCounterArray.writeToParcel(out, this.mCpuFreqTimeMs);
            LongSamplingCounterArray.writeToParcel(out, this.mScreenOffCpuFreqTimeMs);
            if (this.mMobileRadioApWakeupCount != null) {
                out.writeInt(1);
                this.mMobileRadioApWakeupCount.writeToParcel(out);
            } else {
                out.writeInt(0);
            }
            if (this.mWifiRadioApWakeupCount != null) {
                out.writeInt(1);
                this.mWifiRadioApWakeupCount.writeToParcel(out);
                return;
            }
            out.writeInt(0);
        }

        void readFromParcelLocked(TimeBase timeBase, TimeBase screenOffTimeBase, Parcel in) {
            int j;
            OverflowArrayMap overflowArrayMap;
            String str;
            int k;
            int i;
            this.mOnBatteryBackgroundTimeBase.readFromParcel(in);
            this.mOnBatteryScreenOffBackgroundTimeBase.readFromParcel(in);
            int numWakelocks = in.readInt();
            this.mWakelockStats.clear();
            for (j = 0; j < numWakelocks; j++) {
                String wakelockName = in.readString();
                Wakelock wakelock = new Wakelock(this.mBsi, this);
                wakelock.readFromParcelLocked(timeBase, screenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase, in);
                this.mWakelockStats.add(wakelockName, wakelock);
            }
            int numSyncs = in.readInt();
            this.mSyncStats.clear();
            for (j = 0; j < numSyncs; j++) {
                String syncName = in.readString();
                if (in.readInt() != 0) {
                    overflowArrayMap = this.mSyncStats;
                    str = syncName;
                    overflowArrayMap.add(str, new DualTimer(this.mBsi.mClocks, this, 13, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, in));
                }
            }
            int numJobs = in.readInt();
            this.mJobStats.clear();
            for (j = 0; j < numJobs; j++) {
                String jobName = in.readString();
                if (in.readInt() != 0) {
                    overflowArrayMap = this.mJobStats;
                    str = jobName;
                    overflowArrayMap.add(str, new DualTimer(this.mBsi.mClocks, this, 14, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, in));
                }
            }
            int numSensors = in.readInt();
            this.mSensorStats.clear();
            for (k = 0; k < numSensors; k++) {
                int sensorNumber = in.readInt();
                Sensor sensor = new Sensor(this.mBsi, this, sensorNumber);
                sensor.readFromParcelLocked(this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, in);
                this.mSensorStats.put(sensorNumber, sensor);
            }
            int numProcs = in.readInt();
            this.mProcessStats.clear();
            for (k = 0; k < numProcs; k++) {
                String processName = in.readString();
                Proc proc = new Proc(this.mBsi, processName);
                proc.readFromParcelLocked(in);
                this.mProcessStats.put(processName, proc);
            }
            int numPkgs = in.readInt();
            this.mPackageStats.clear();
            for (int l = 0; l < numPkgs; l++) {
                String packageName = in.readString();
                Pkg pkg = new Pkg(this.mBsi);
                pkg.readFromParcelLocked(in);
                this.mPackageStats.put(packageName, pkg);
            }
            this.mWifiRunning = false;
            if (in.readInt() != 0) {
                this.mWifiRunningTimer = new StopwatchTimer(this.mBsi.mClocks, this, 4, this.mBsi.mWifiRunningTimers, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mWifiRunningTimer = null;
            }
            this.mFullWifiLockOut = false;
            if (in.readInt() != 0) {
                this.mFullWifiLockTimer = new StopwatchTimer(this.mBsi.mClocks, this, 5, this.mBsi.mFullWifiLockTimers, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mFullWifiLockTimer = null;
            }
            this.mWifiScanStarted = false;
            if (in.readInt() != 0) {
                this.mWifiScanTimer = new DualTimer(this.mBsi.mClocks, this, 6, this.mBsi.mWifiScanTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, in);
            } else {
                this.mWifiScanTimer = null;
            }
            this.mWifiBatchedScanBinStarted = -1;
            for (i = 0; i < 5; i++) {
                if (in.readInt() != 0) {
                    makeWifiBatchedScanBin(i, in);
                } else {
                    this.mWifiBatchedScanTimer[i] = null;
                }
            }
            this.mWifiMulticastEnabled = false;
            if (in.readInt() != 0) {
                this.mWifiMulticastTimer = new StopwatchTimer(this.mBsi.mClocks, this, 7, this.mBsi.mWifiMulticastTimers, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mWifiMulticastTimer = null;
            }
            if (in.readInt() != 0) {
                this.mAudioTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 15, this.mBsi.mAudioTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mAudioTurnedOnTimer = null;
            }
            if (in.readInt() != 0) {
                this.mVideoTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 8, this.mBsi.mVideoTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mVideoTurnedOnTimer = null;
            }
            if (in.readInt() != 0) {
                this.mFlashlightTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 16, this.mBsi.mFlashlightTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mFlashlightTurnedOnTimer = null;
            }
            if (in.readInt() != 0) {
                this.mCameraTurnedOnTimer = new StopwatchTimer(this.mBsi.mClocks, this, 17, this.mBsi.mCameraTurnedOnTimers, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mCameraTurnedOnTimer = null;
            }
            if (in.readInt() != 0) {
                this.mForegroundActivityTimer = new StopwatchTimer(this.mBsi.mClocks, this, 10, null, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mForegroundActivityTimer = null;
            }
            if (in.readInt() != 0) {
                this.mAggregatedPartialWakelockTimer = new DualTimer(this.mBsi.mClocks, this, 20, null, this.mBsi.mOnBatteryScreenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase, in);
            } else {
                this.mAggregatedPartialWakelockTimer = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothScanTimer = new DualTimer(this.mBsi.mClocks, this, 19, this.mBsi.mBluetoothScanOnTimers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, in);
            } else {
                this.mBluetoothScanTimer = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothUnoptimizedScanTimer = new DualTimer(this.mBsi.mClocks, this, 21, null, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase, in);
            } else {
                this.mBluetoothUnoptimizedScanTimer = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothScanResultCounter = new Counter(this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mBluetoothScanResultCounter = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothScanResultBgCounter = new Counter(this.mOnBatteryBackgroundTimeBase, in);
            } else {
                this.mBluetoothScanResultBgCounter = null;
            }
            this.mProcessState = 18;
            for (i = 0; i < 6; i++) {
                if (in.readInt() != 0) {
                    makeProcessState(i, in);
                } else {
                    this.mProcessStateTimer[i] = null;
                }
            }
            if (in.readInt() != 0) {
                this.mVibratorOnTimer = new BatchTimer(this.mBsi.mClocks, this, 9, this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mVibratorOnTimer = null;
            }
            if (in.readInt() != 0) {
                this.mUserActivityCounters = new Counter[4];
                for (i = 0; i < 4; i++) {
                    this.mUserActivityCounters[i] = new Counter(this.mBsi.mOnBatteryTimeBase, in);
                }
            } else {
                this.mUserActivityCounters = null;
            }
            if (in.readInt() != 0) {
                this.mNetworkByteActivityCounters = new LongSamplingCounter[10];
                this.mNetworkPacketActivityCounters = new LongSamplingCounter[10];
                for (i = 0; i < 10; i++) {
                    this.mNetworkByteActivityCounters[i] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
                    this.mNetworkPacketActivityCounters[i] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
                }
                this.mMobileRadioActiveTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
                this.mMobileRadioActiveCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mNetworkByteActivityCounters = null;
                this.mNetworkPacketActivityCounters = null;
            }
            if (in.readInt() != 0) {
                this.mWifiControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 1, in);
            } else {
                this.mWifiControllerActivity = null;
            }
            if (in.readInt() != 0) {
                this.mBluetoothControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 1, in);
            } else {
                this.mBluetoothControllerActivity = null;
            }
            if (in.readInt() != 0) {
                this.mModemControllerActivity = new ControllerActivityCounterImpl(this.mBsi.mOnBatteryTimeBase, 5, in);
            } else {
                this.mModemControllerActivity = null;
            }
            this.mUserCpuTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
            this.mSystemCpuTime = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
            if (in.readInt() != 0) {
                int numCpuClusters = in.readInt();
                if (this.mBsi.mPowerProfile == null || this.mBsi.mPowerProfile.getNumCpuClusters() == numCpuClusters) {
                    this.mCpuClusterSpeed = new LongSamplingCounter[numCpuClusters][];
                    int cluster = 0;
                    while (cluster < numCpuClusters) {
                        if (in.readInt() != 0) {
                            int numSpeeds = in.readInt();
                            if (this.mBsi.mPowerProfile == null || this.mBsi.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster) == numSpeeds) {
                                LongSamplingCounter[] cpuSpeeds = new LongSamplingCounter[numSpeeds];
                                this.mCpuClusterSpeed[cluster] = cpuSpeeds;
                                for (int speed = 0; speed < numSpeeds; speed++) {
                                    if (in.readInt() != 0) {
                                        cpuSpeeds[speed] = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
                                    }
                                }
                            } else {
                                throw new ParcelFormatException("Incompatible number of cpu speeds");
                            }
                        }
                        this.mCpuClusterSpeed[cluster] = null;
                        cluster++;
                    }
                } else {
                    throw new ParcelFormatException("Incompatible number of cpu clusters");
                }
            }
            this.mCpuClusterSpeed = null;
            this.mCpuFreqTimeMs = LongSamplingCounterArray.readFromParcel(in, this.mBsi.mOnBatteryTimeBase);
            this.mScreenOffCpuFreqTimeMs = LongSamplingCounterArray.readFromParcel(in, this.mBsi.mOnBatteryScreenOffTimeBase);
            if (in.readInt() != 0) {
                this.mMobileRadioApWakeupCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mMobileRadioApWakeupCount = null;
            }
            if (in.readInt() != 0) {
                this.mWifiRadioApWakeupCount = new LongSamplingCounter(this.mBsi.mOnBatteryTimeBase, in);
            } else {
                this.mWifiRadioApWakeupCount = null;
            }
        }

        public Proc getProcessStatsLocked(String name) {
            Proc ps = (Proc) this.mProcessStats.get(name);
            if (ps != null) {
                return ps;
            }
            ps = new Proc(this.mBsi, name);
            this.mProcessStats.put(name, ps);
            return ps;
        }

        public void updateUidProcessStateLocked(int procState) {
            int uidRunningState;
            if (procState == 18) {
                uidRunningState = 18;
            } else if (procState == 2) {
                uidRunningState = 0;
            } else if (procState <= 4) {
                uidRunningState = 1;
            } else if (procState <= 5) {
                uidRunningState = 2;
            } else if (procState <= 6) {
                uidRunningState = 3;
            } else if (procState <= 12) {
                uidRunningState = 4;
            } else {
                uidRunningState = 5;
            }
            if (this.mProcessState != uidRunningState) {
                long elapsedRealtimeMs = this.mBsi.mClocks.elapsedRealtime();
                long uptimeMs = this.mBsi.mClocks.uptimeMillis();
                if (this.mProcessState != 18) {
                    this.mProcessStateTimer[this.mProcessState].stopRunningLocked(elapsedRealtimeMs);
                }
                this.mProcessState = uidRunningState;
                if (uidRunningState != 18) {
                    if (this.mProcessStateTimer[uidRunningState] == null) {
                        makeProcessState(uidRunningState, null);
                    }
                    this.mProcessStateTimer[uidRunningState].startRunningLocked(elapsedRealtimeMs);
                }
                updateOnBatteryBgTimeBase(uptimeMs * 1000, elapsedRealtimeMs * 1000);
                updateOnBatteryScreenOffBgTimeBase(uptimeMs * 1000, elapsedRealtimeMs * 1000);
            }
        }

        public boolean isInBackground() {
            return this.mProcessState >= 4;
        }

        public boolean updateOnBatteryBgTimeBase(long uptimeUs, long realtimeUs) {
            return this.mOnBatteryBackgroundTimeBase.setRunning(this.mBsi.mOnBatteryTimeBase.isRunning() ? isInBackground() : false, uptimeUs, realtimeUs);
        }

        public boolean updateOnBatteryScreenOffBgTimeBase(long uptimeUs, long realtimeUs) {
            return this.mOnBatteryScreenOffBackgroundTimeBase.setRunning(this.mBsi.mOnBatteryScreenOffTimeBase.isRunning() ? isInBackground() : false, uptimeUs, realtimeUs);
        }

        public SparseArray<? extends Pid> getPidStats() {
            return this.mPids;
        }

        public Pid getPidStatsLocked(int pid) {
            Pid p = (Pid) this.mPids.get(pid);
            if (p != null) {
                return p;
            }
            p = new Pid(this);
            this.mPids.put(pid, p);
            return p;
        }

        public Pkg getPackageStatsLocked(String name) {
            Pkg ps = (Pkg) this.mPackageStats.get(name);
            if (ps != null) {
                return ps;
            }
            ps = new Pkg(this.mBsi);
            this.mPackageStats.put(name, ps);
            return ps;
        }

        public Serv getServiceStatsLocked(String pkg, String serv) {
            Pkg ps = getPackageStatsLocked(pkg);
            Serv ss = (Serv) ps.mServiceStats.get(serv);
            if (ss != null) {
                return ss;
            }
            ss = ps.newServiceStatsLocked();
            ps.mServiceStats.put(serv, ss);
            return ss;
        }

        public void readSyncSummaryFromParcelLocked(String name, Parcel in) {
            DualTimer timer = (DualTimer) this.mSyncStats.instantiateObject();
            timer.readSummaryFromParcelLocked(in);
            this.mSyncStats.add(name, timer);
        }

        public void readJobSummaryFromParcelLocked(String name, Parcel in) {
            DualTimer timer = (DualTimer) this.mJobStats.instantiateObject();
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
            Sensor se = (Sensor) this.mSensorStats.get(sensor);
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
            ArrayList<StopwatchTimer> timers = (ArrayList) this.mBsi.mSensorTimers.get(sensor);
            if (timers == null) {
                timers = new ArrayList();
                this.mBsi.mSensorTimers.put(sensor, timers);
            }
            t = new DualTimer(this.mBsi.mClocks, this, 3, timers, this.mBsi.mOnBatteryTimeBase, this.mOnBatteryBackgroundTimeBase);
            se.mTimer = t;
            return t;
        }

        public void noteStartSyncLocked(String name, long elapsedRealtimeMs) {
            DualTimer t = (DualTimer) this.mSyncStats.startObject(name);
            if (t != null) {
                t.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteStopSyncLocked(String name, long elapsedRealtimeMs) {
            DualTimer t = (DualTimer) this.mSyncStats.stopObject(name);
            if (t != null) {
                t.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteStartJobLocked(String name, long elapsedRealtimeMs) {
            DualTimer t = (DualTimer) this.mJobStats.startObject(name);
            if (t != null) {
                t.startRunningLocked(elapsedRealtimeMs);
            }
        }

        public void noteStopJobLocked(String name, long elapsedRealtimeMs) {
            DualTimer t = (DualTimer) this.mJobStats.stopObject(name);
            if (t != null) {
                t.stopRunningLocked(elapsedRealtimeMs);
            }
        }

        public StopwatchTimer getWakelockTimerLocked(Wakelock wl, int type) {
            if (wl == null) {
                return null;
            }
            StopwatchTimer t;
            switch (type) {
                case 0:
                    DualTimer t2 = wl.mTimerPartial;
                    if (t2 == null) {
                        t2 = new DualTimer(this.mBsi.mClocks, this, 0, this.mBsi.mPartialTimers, this.mBsi.mOnBatteryScreenOffTimeBase, this.mOnBatteryScreenOffBackgroundTimeBase);
                        wl.mTimerPartial = t2;
                    }
                    return t2;
                case 1:
                    t = wl.mTimerFull;
                    if (t == null) {
                        t = new StopwatchTimer(this.mBsi.mClocks, this, 1, this.mBsi.mFullTimers, this.mBsi.mOnBatteryTimeBase);
                        wl.mTimerFull = t;
                    }
                    return t;
                case 2:
                    t = wl.mTimerWindow;
                    if (t == null) {
                        t = new StopwatchTimer(this.mBsi.mClocks, this, 2, this.mBsi.mWindowTimers, this.mBsi.mOnBatteryTimeBase);
                        wl.mTimerWindow = t;
                    }
                    return t;
                case 18:
                    t = wl.mTimerDraw;
                    if (t == null) {
                        t = new StopwatchTimer(this.mBsi.mClocks, this, 18, this.mBsi.mDrawTimers, this.mBsi.mOnBatteryTimeBase);
                        wl.mTimerDraw = t;
                    }
                    return t;
                default:
                    throw new IllegalArgumentException("type=" + type);
            }
        }

        public void noteStartWakeLocked(int pid, String name, int type, long elapsedRealtimeMs) {
            Wakelock wl = (Wakelock) this.mWakelockStats.startObject(name);
            if (wl != null) {
                getWakelockTimerLocked(wl, type).startRunningLocked(elapsedRealtimeMs);
            }
            if (type == 0) {
                createAggregatedPartialWakelockTimerLocked().startRunningLocked(elapsedRealtimeMs);
                if (pid >= 0) {
                    Pid p = getPidStatsLocked(pid);
                    int i = p.mWakeNesting;
                    p.mWakeNesting = i + 1;
                    if (i == 0) {
                        p.mWakeStartMs = elapsedRealtimeMs;
                    }
                }
            }
        }

        public void noteStopWakeLocked(int pid, String name, int type, long elapsedRealtimeMs) {
            Wakelock wl = (Wakelock) this.mWakelockStats.stopObject(name);
            if (wl != null) {
                getWakelockTimerLocked(wl, type).stopRunningLocked(elapsedRealtimeMs);
            }
            if (type == 0) {
                if (this.mAggregatedPartialWakelockTimer != null) {
                    this.mAggregatedPartialWakelockTimer.stopRunningLocked(elapsedRealtimeMs);
                }
                if (pid >= 0) {
                    Pid p = (Pid) this.mPids.get(pid);
                    if (p != null && p.mWakeNesting > 0) {
                        int i = p.mWakeNesting;
                        p.mWakeNesting = i - 1;
                        if (i == 1) {
                            p.mWakeSumMs += elapsedRealtimeMs - p.mWakeStartMs;
                            p.mWakeStartMs = 0;
                        }
                    }
                }
            }
        }

        public void reportExcessiveWakeLocked(String proc, long overTime, long usedTime) {
            Proc p = getProcessStatsLocked(proc);
            if (p != null) {
                p.addExcessiveWake(overTime, usedTime);
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

    public LongSparseArray<SamplingTimer> getKernelMemoryStats() {
        return this.mKernelMemoryStats;
    }

    public Map<String, ? extends Timer> getKernelWakelockStats() {
        return this.mKernelWakelockStats;
    }

    public Map<String, ? extends Timer> getWakeupReasonStats() {
        return this.mWakeupReasonStats;
    }

    public LongCounter getDischargeScreenOffCoulombCounter() {
        return this.mDischargeScreenOffCounter;
    }

    public LongCounter getDischargeCoulombCounter() {
        return this.mDischargeCounter;
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
        this(new SystemClocks());
    }

    public BatteryStatsImpl(Clocks clocks) {
        this.mKernelWakelockReader = new KernelWakelockReader();
        this.mTmpWakelockStats = new KernelWakelockStats();
        this.mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
        this.mKernelUidCpuFreqTimeReader = new KernelUidCpuFreqTimeReader();
        this.mKernelMemoryBandwidthStats = new KernelMemoryBandwidthStats();
        this.mKernelMemoryStats = new LongSparseArray();
        this.mIsolatedUids = new SparseIntArray();
        this.mUidStats = new SparseArray();
        this.mPartialTimers = new ArrayList();
        this.mFullTimers = new ArrayList();
        this.mWindowTimers = new ArrayList();
        this.mDrawTimers = new ArrayList();
        this.mSensorTimers = new SparseArray();
        this.mWifiRunningTimers = new ArrayList();
        this.mFullWifiLockTimers = new ArrayList();
        this.mWifiMulticastTimers = new ArrayList();
        this.mWifiScanTimers = new ArrayList();
        this.mWifiBatchedScanTimers = new SparseArray();
        this.mAudioTurnedOnTimers = new ArrayList();
        this.mVideoTurnedOnTimers = new ArrayList();
        this.mFlashlightTurnedOnTimers = new ArrayList();
        this.mCameraTurnedOnTimers = new ArrayList();
        this.mBluetoothScanOnTimers = new ArrayList();
        this.mLastPartialTimers = new ArrayList();
        this.mOnBatteryTimeBase = new TimeBase();
        this.mOnBatteryScreenOffTimeBase = new TimeBase();
        this.mActiveEvents = new HistoryEventTracker();
        this.mHaveBatteryLevel = false;
        this.mRecordingHistory = false;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new HistoryItem();
        this.mHistoryLastLastWritten = new HistoryItem();
        this.mHistoryReadTmp = new HistoryItem();
        this.mHistoryAddTmp = new HistoryItem();
        this.mHistoryTagPool = new HashMap();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = false;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryCur = new HistoryItem();
        this.mLastHistoryStepDetails = null;
        this.mLastHistoryStepLevel = (byte) 0;
        this.mCurHistoryStepDetails = new HistoryStepDetails();
        this.mReadHistoryStepDetails = new HistoryStepDetails();
        this.mTmpHistoryStepDetails = new HistoryStepDetails();
        this.mScreenState = 0;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[17];
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
        this.mMobileRadioPowerState = 1;
        this.mWifiRadioPowerState = 1;
        this.mCharging = true;
        this.mInitStepMode = 0;
        this.mCurStepMode = 0;
        this.mModStepMode = 0;
        this.mDischargeStepTracker = new LevelStepTracker(200);
        this.mDailyDischargeStepTracker = new LevelStepTracker(400);
        this.mChargeStepTracker = new LevelStepTracker(200);
        this.mDailyChargeStepTracker = new LevelStepTracker(400);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mMinLearnedBatteryCapacity = -1;
        this.mMaxLearnedBatteryCapacity = -1;
        this.mKernelWakelockStats = new HashMap();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap();
        this.mChangedStates = 0;
        this.mChangedStates2 = 0;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = 0;
        this.mWifiScanNesting = 0;
        this.mWifiMulticastNesting = 0;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        this.mNetworkStatsPool = new SynchronizedPool(6);
        this.mWifiNetworkLock = new Object();
        this.mWifiIfaces = EmptyArray.STRING;
        this.mLastWifiNetworkStats = new NetworkStats(0, -1);
        this.mModemNetworkLock = new Object();
        this.mModemIfaces = EmptyArray.STRING;
        this.mLastModemNetworkStats = new NetworkStats(0, -1);
        this.mPendingWrite = null;
        this.mWriteLock = new ReentrantLock();
        init(clocks);
        this.mFile = null;
        this.mCheckinFile = null;
        this.mDailyFile = null;
        this.mHandler = null;
        this.mExternalSync = null;
        this.mPlatformIdleStateCallback = null;
        clearHistoryLocked();
    }

    private void init(Clocks clocks) {
        this.mClocks = clocks;
    }

    public SamplingTimer getWakeupReasonTimerLocked(String name) {
        SamplingTimer timer = (SamplingTimer) this.mWakeupReasonStats.get(name);
        if (timer != null) {
            return timer;
        }
        timer = new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase);
        this.mWakeupReasonStats.put(name, timer);
        return timer;
    }

    public SamplingTimer getKernelWakelockTimerLocked(String name) {
        SamplingTimer kwlt = (SamplingTimer) this.mKernelWakelockStats.get(name);
        if (kwlt != null) {
            return kwlt;
        }
        kwlt = new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase);
        this.mKernelWakelockStats.put(name, kwlt);
        return kwlt;
    }

    public SamplingTimer getKernelMemoryTimerLocked(long bucket) {
        SamplingTimer kmt = (SamplingTimer) this.mKernelMemoryStats.get(bucket);
        if (kmt != null) {
            return kmt;
        }
        kmt = new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase);
        this.mKernelMemoryStats.put(bucket, kmt);
        return kmt;
    }

    private int writeHistoryTag(HistoryTag tag) {
        Integer idxObj = (Integer) this.mHistoryTagPool.get(tag);
        if (idxObj != null) {
            return idxObj.intValue();
        }
        int idx = this.mNextHistoryTagIdx;
        HistoryTag key = new HistoryTag();
        key.setTo(tag);
        tag.poolIdx = idx;
        this.mHistoryTagPool.put(key, Integer.valueOf(idx));
        this.mNextHistoryTagIdx++;
        this.mNumHistoryTagChars += key.string.length() + 1;
        return idx;
    }

    private void readHistoryTag(int index, HistoryTag tag) {
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

    public void writeHistoryDelta(Parcel dest, HistoryItem cur, HistoryItem last) {
        if (last == null || cur.cmd != (byte) 0) {
            dest.writeInt(DELTA_TIME_ABS);
            cur.writeToParcel(dest, 0);
            return;
        }
        int deltaTimeToken;
        long deltaTime = cur.time - last.time;
        int lastBatteryLevelInt = buildBatteryLevelInt(last);
        int lastStateInt = buildStateInt(last);
        if (deltaTime < 0 || deltaTime > 2147483647L) {
            deltaTimeToken = EventLogTags.SYSUI_VIEW_VISIBILITY;
        } else if (deltaTime >= 524285) {
            deltaTimeToken = DELTA_TIME_INT;
        } else {
            deltaTimeToken = (int) deltaTime;
        }
        int firstToken = deltaTimeToken | (cur.states & DELTA_STATE_MASK);
        int includeStepDetails = this.mLastHistoryStepLevel > cur.batteryLevel ? 1 : 0;
        boolean computeStepDetails = includeStepDetails == 0 ? this.mLastHistoryStepDetails == null : true;
        int batteryLevelInt = buildBatteryLevelInt(cur) | includeStepDetails;
        boolean batteryLevelIntChanged = batteryLevelInt != lastBatteryLevelInt;
        if (batteryLevelIntChanged) {
            firstToken |= 524288;
        }
        int stateInt = buildStateInt(cur);
        boolean stateIntChanged = stateInt != lastStateInt;
        if (stateIntChanged) {
            firstToken |= 1048576;
        }
        boolean state2IntChanged = cur.states2 != last.states2;
        if (state2IntChanged) {
            firstToken |= 2097152;
        }
        if (!(cur.wakelockTag == null && cur.wakeReasonTag == null)) {
            firstToken |= 4194304;
        }
        if (cur.eventCode != 0) {
            firstToken |= 8388608;
        }
        boolean batteryChargeChanged = cur.batteryChargeUAh != last.batteryChargeUAh;
        if (batteryChargeChanged) {
            firstToken |= 16777216;
        }
        dest.writeInt(firstToken);
        if (deltaTimeToken >= DELTA_TIME_INT) {
            if (deltaTimeToken == DELTA_TIME_INT) {
                dest.writeInt((int) deltaTime);
            } else {
                dest.writeLong(deltaTime);
            }
        }
        if (batteryLevelIntChanged) {
            dest.writeInt(batteryLevelInt);
        }
        if (stateIntChanged) {
            dest.writeInt(stateInt);
        }
        if (state2IntChanged) {
            dest.writeInt(cur.states2);
        }
        if (!(cur.wakelockTag == null && cur.wakeReasonTag == null)) {
            int wakeLockIndex;
            int wakeReasonIndex;
            if (cur.wakelockTag != null) {
                wakeLockIndex = writeHistoryTag(cur.wakelockTag);
            } else {
                wakeLockIndex = 65535;
            }
            if (cur.wakeReasonTag != null) {
                wakeReasonIndex = writeHistoryTag(cur.wakeReasonTag);
            } else {
                wakeReasonIndex = 65535;
            }
            dest.writeInt((wakeReasonIndex << 16) | wakeLockIndex);
        }
        if (cur.eventCode != 0) {
            dest.writeInt((cur.eventCode & 65535) | (writeHistoryTag(cur.eventTag) << 16));
        }
        if (computeStepDetails) {
            if (this.mPlatformIdleStateCallback != null) {
                this.mCurHistoryStepDetails.statPlatformIdleState = this.mPlatformIdleStateCallback.getPlatformLowPowerStats();
            }
            computeHistoryStepDetails(this.mCurHistoryStepDetails, this.mLastHistoryStepDetails);
            if (includeStepDetails != 0) {
                this.mCurHistoryStepDetails.writeToParcel(dest);
            }
            cur.stepDetails = this.mCurHistoryStepDetails;
            this.mLastHistoryStepDetails = this.mCurHistoryStepDetails;
        } else {
            cur.stepDetails = null;
        }
        if (this.mLastHistoryStepLevel < cur.batteryLevel) {
            this.mLastHistoryStepDetails = null;
        }
        this.mLastHistoryStepLevel = cur.batteryLevel;
        if (batteryChargeChanged) {
            dest.writeInt(cur.batteryChargeUAh);
        }
    }

    private int buildBatteryLevelInt(HistoryItem h) {
        return (((h.batteryLevel << 25) & DELTA_STATE_MASK) | ((h.batteryTemperature << 15) & 33521664)) | ((h.batteryVoltage << 1) & 32766);
    }

    private void readBatteryLevelInt(int batteryLevelInt, HistoryItem out) {
        out.batteryLevel = (byte) ((DELTA_STATE_MASK & batteryLevelInt) >>> 25);
        out.batteryTemperature = (short) ((33521664 & batteryLevelInt) >>> 15);
        out.batteryVoltage = (char) ((batteryLevelInt & 32766) >>> 1);
    }

    private int buildStateInt(HistoryItem h) {
        int plugType = 0;
        if ((h.batteryPlugType & 1) != 0) {
            plugType = 1;
        } else if ((h.batteryPlugType & 2) != 0) {
            plugType = 2;
        } else if ((h.batteryPlugType & 4) != 0) {
            plugType = 3;
        }
        return ((((h.batteryStatus & 7) << 29) | ((h.batteryHealth & 7) << 26)) | ((plugType & 3) << 24)) | (h.states & 16777215);
    }

    private void computeHistoryStepDetails(HistoryStepDetails out, HistoryStepDetails last) {
        HistoryStepDetails tmp = last != null ? this.mTmpHistoryStepDetails : out;
        requestImmediateCpuUpdate();
        int NU;
        int i;
        Uid uid;
        if (last == null) {
            NU = this.mUidStats.size();
            for (i = 0; i < NU; i++) {
                uid = (Uid) this.mUidStats.valueAt(i);
                uid.mLastStepUserTime = uid.mCurStepUserTime;
                uid.mLastStepSystemTime = uid.mCurStepSystemTime;
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
        NU = this.mUidStats.size();
        for (i = 0; i < NU; i++) {
            uid = (Uid) this.mUidStats.valueAt(i);
            int totalUTime = (int) (uid.mCurStepUserTime - uid.mLastStepUserTime);
            int totalSTime = (int) (uid.mCurStepSystemTime - uid.mLastStepSystemTime);
            int totalTime = totalUTime + totalSTime;
            uid.mLastStepUserTime = uid.mCurStepUserTime;
            uid.mLastStepSystemTime = uid.mCurStepSystemTime;
            if (totalTime > out.appCpuUTime3 + out.appCpuSTime3) {
                if (totalTime <= out.appCpuUTime2 + out.appCpuSTime2) {
                    out.appCpuUid3 = uid.mUid;
                    out.appCpuUTime3 = totalUTime;
                    out.appCpuSTime3 = totalSTime;
                } else {
                    out.appCpuUid3 = out.appCpuUid2;
                    out.appCpuUTime3 = out.appCpuUTime2;
                    out.appCpuSTime3 = out.appCpuSTime2;
                    if (totalTime <= out.appCpuUTime1 + out.appCpuSTime1) {
                        out.appCpuUid2 = uid.mUid;
                        out.appCpuUTime2 = totalUTime;
                        out.appCpuSTime2 = totalSTime;
                    } else {
                        out.appCpuUid2 = out.appCpuUid1;
                        out.appCpuUTime2 = out.appCpuUTime1;
                        out.appCpuSTime2 = out.appCpuSTime1;
                        out.appCpuUid1 = uid.mUid;
                        out.appCpuUTime1 = totalUTime;
                        out.appCpuSTime1 = totalSTime;
                    }
                }
            }
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

    public void readHistoryDelta(Parcel src, HistoryItem cur) {
        int batteryLevelInt;
        int firstToken = src.readInt();
        int deltaTimeToken = firstToken & EventLogTags.SYSUI_VIEW_VISIBILITY;
        cur.cmd = (byte) 0;
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
        if ((1048576 & firstToken) != 0) {
            int stateInt = src.readInt();
            cur.states = (DELTA_STATE_MASK & firstToken) | (16777215 & stateInt);
            cur.batteryStatus = (byte) ((stateInt >> 29) & 7);
            cur.batteryHealth = (byte) ((stateInt >> 26) & 7);
            cur.batteryPlugType = (byte) ((stateInt >> 24) & 3);
            switch (cur.batteryPlugType) {
                case (byte) 1:
                    cur.batteryPlugType = (byte) 1;
                    break;
                case (byte) 2:
                    cur.batteryPlugType = (byte) 2;
                    break;
                case (byte) 3:
                    cur.batteryPlugType = (byte) 4;
                    break;
            }
            cur.numReadInts++;
        } else {
            cur.states = (DELTA_STATE_MASK & firstToken) | (cur.states & 16777215);
        }
        if ((2097152 & firstToken) != 0) {
            cur.states2 = src.readInt();
        }
        if ((4194304 & firstToken) != 0) {
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
        if ((8388608 & firstToken) != 0) {
            cur.eventTag = cur.localEventTag;
            int codeAndIndex = src.readInt();
            cur.eventCode = 65535 & codeAndIndex;
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
        this.mHistoryLastWritten.cmd = (byte) -1;
    }

    void addHistoryBufferLocked(long elapsedRealtimeMs, long uptimeMs, HistoryItem cur) {
        if (this.mHaveBatteryLevel && (this.mRecordingHistory ^ 1) == 0) {
            long timeDiff = (this.mHistoryBaseTime + elapsedRealtimeMs) - this.mHistoryLastWritten.time;
            int diffStates = this.mHistoryLastWritten.states ^ (cur.states & this.mActiveHistoryStates);
            int diffStates2 = this.mHistoryLastWritten.states2 ^ (cur.states2 & this.mActiveHistoryStates2);
            int lastDiffStates = this.mHistoryLastWritten.states ^ this.mHistoryLastLastWritten.states;
            int lastDiffStates2 = this.mHistoryLastWritten.states2 ^ this.mHistoryLastLastWritten.states2;
            if (this.mHistoryBufferLastPos >= 0 && this.mHistoryLastWritten.cmd == (byte) 0 && timeDiff < 1000 && (diffStates & lastDiffStates) == 0 && (diffStates2 & lastDiffStates2) == 0 && ((this.mHistoryLastWritten.wakelockTag == null || cur.wakelockTag == null) && ((this.mHistoryLastWritten.wakeReasonTag == null || cur.wakeReasonTag == null) && this.mHistoryLastWritten.stepDetails == null && ((this.mHistoryLastWritten.eventCode == 0 || cur.eventCode == 0) && this.mHistoryLastWritten.batteryLevel == cur.batteryLevel && this.mHistoryLastWritten.batteryStatus == cur.batteryStatus && this.mHistoryLastWritten.batteryHealth == cur.batteryHealth && this.mHistoryLastWritten.batteryPlugType == cur.batteryPlugType && this.mHistoryLastWritten.batteryTemperature == cur.batteryTemperature && this.mHistoryLastWritten.batteryVoltage == cur.batteryVoltage)))) {
                this.mHistoryBuffer.setDataSize(this.mHistoryBufferLastPos);
                this.mHistoryBuffer.setDataPosition(this.mHistoryBufferLastPos);
                this.mHistoryBufferLastPos = -1;
                elapsedRealtimeMs = this.mHistoryLastWritten.time - this.mHistoryBaseTime;
                if (this.mHistoryLastWritten.wakelockTag != null) {
                    cur.wakelockTag = cur.localWakelockTag;
                    cur.wakelockTag.setTo(this.mHistoryLastWritten.wakelockTag);
                }
                if (this.mHistoryLastWritten.wakeReasonTag != null) {
                    cur.wakeReasonTag = cur.localWakeReasonTag;
                    cur.wakeReasonTag.setTo(this.mHistoryLastWritten.wakeReasonTag);
                }
                if (this.mHistoryLastWritten.eventCode != 0) {
                    cur.eventCode = this.mHistoryLastWritten.eventCode;
                    cur.eventTag = cur.localEventTag;
                    cur.eventTag.setTo(this.mHistoryLastWritten.eventTag);
                }
                this.mHistoryLastWritten.setTo(this.mHistoryLastLastWritten);
            }
            boolean recordResetDueToOverflow = false;
            int dataSize = this.mHistoryBuffer.dataSize();
            if (dataSize >= SurfaceControl.FX_SURFACE_MASK) {
                resetAllStatsLocked();
                recordResetDueToOverflow = true;
            } else if (dataSize >= 262144) {
                if (this.mHistoryOverflow) {
                    int old;
                    int writeAnyway = 0;
                    int curStates = (cur.states & -1638400) & this.mActiveHistoryStates;
                    if (this.mHistoryLastWritten.states != curStates) {
                        old = this.mActiveHistoryStates;
                        this.mActiveHistoryStates &= 1638399 | curStates;
                        writeAnyway = old != this.mActiveHistoryStates ? 1 : 0;
                    }
                    int curStates2 = (cur.states2 & 1748959232) & this.mActiveHistoryStates2;
                    if (this.mHistoryLastWritten.states2 != curStates2) {
                        old = this.mActiveHistoryStates2;
                        this.mActiveHistoryStates2 &= -1748959233 | curStates2;
                        writeAnyway |= old != this.mActiveHistoryStates2 ? 1 : 0;
                    }
                    if (writeAnyway != 0 || this.mHistoryLastWritten.batteryLevel != cur.batteryLevel || (dataSize < 327680 && ((this.mHistoryLastWritten.states ^ cur.states) & 1572864) != 0 && ((this.mHistoryLastWritten.states2 ^ cur.states2) & -1749024768) != 0)) {
                        addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 0, cur);
                        return;
                    }
                    return;
                }
                this.mHistoryOverflow = true;
                addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 0, cur);
                addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 6, cur);
                return;
            }
            if (dataSize == 0 || recordResetDueToOverflow) {
                cur.currentTime = System.currentTimeMillis();
                if (recordResetDueToOverflow) {
                    addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 6, cur);
                }
                addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 7, cur);
            }
            addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 0, cur);
        }
    }

    private void addHistoryBufferLocked(long elapsedRealtimeMs, long uptimeMs, byte cmd, HistoryItem cur) {
        if (this.mIteratingHistory) {
            throw new IllegalStateException("Can't do this while iterating history!");
        }
        this.mHistoryBufferLastPos = this.mHistoryBuffer.dataPosition();
        this.mHistoryLastLastWritten.setTo(this.mHistoryLastWritten);
        this.mHistoryLastWritten.setTo(this.mHistoryBaseTime + elapsedRealtimeMs, cmd, cur);
        HistoryItem historyItem = this.mHistoryLastWritten;
        historyItem.states &= this.mActiveHistoryStates;
        historyItem = this.mHistoryLastWritten;
        historyItem.states2 &= this.mActiveHistoryStates2;
        writeHistoryDelta(this.mHistoryBuffer, this.mHistoryLastWritten, this.mHistoryLastLastWritten);
        this.mLastHistoryElapsedRealtime = elapsedRealtimeMs;
        cur.wakelockTag = null;
        cur.wakeReasonTag = null;
        cur.eventCode = 0;
        cur.eventTag = null;
    }

    void addHistoryRecordLocked(long elapsedRealtimeMs, long uptimeMs) {
        HistoryItem historyItem;
        if (this.mTrackRunningHistoryElapsedRealtime != 0) {
            long diffElapsed = elapsedRealtimeMs - this.mTrackRunningHistoryElapsedRealtime;
            long diffUptime = uptimeMs - this.mTrackRunningHistoryUptime;
            if (diffUptime < diffElapsed - 20) {
                long wakeElapsedTime = elapsedRealtimeMs - (diffElapsed - diffUptime);
                this.mHistoryAddTmp.setTo(this.mHistoryLastWritten);
                this.mHistoryAddTmp.wakelockTag = null;
                this.mHistoryAddTmp.wakeReasonTag = null;
                this.mHistoryAddTmp.eventCode = 0;
                historyItem = this.mHistoryAddTmp;
                historyItem.states &= Integer.MAX_VALUE;
                addHistoryRecordInnerLocked(wakeElapsedTime, uptimeMs, this.mHistoryAddTmp);
            }
        }
        historyItem = this.mHistoryCur;
        historyItem.states |= Integer.MIN_VALUE;
        this.mTrackRunningHistoryElapsedRealtime = elapsedRealtimeMs;
        this.mTrackRunningHistoryUptime = uptimeMs;
        addHistoryRecordInnerLocked(elapsedRealtimeMs, uptimeMs, this.mHistoryCur);
    }

    void addHistoryRecordInnerLocked(long elapsedRealtimeMs, long uptimeMs, HistoryItem cur) {
        addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, cur);
    }

    public void addHistoryEventLocked(long elapsedRealtimeMs, long uptimeMs, int code, String name, int uid) {
        this.mHistoryCur.eventCode = code;
        this.mHistoryCur.eventTag = this.mHistoryCur.localEventTag;
        this.mHistoryCur.eventTag.string = name;
        this.mHistoryCur.eventTag.uid = uid;
        addHistoryRecordLocked(elapsedRealtimeMs, uptimeMs);
    }

    void addHistoryRecordLocked(long elapsedRealtimeMs, long uptimeMs, byte cmd, HistoryItem cur) {
        HistoryItem rec = this.mHistoryCache;
        if (rec != null) {
            this.mHistoryCache = rec.next;
        } else {
            rec = new HistoryItem();
        }
        rec.setTo(this.mHistoryBaseTime + elapsedRealtimeMs, cmd, cur);
        addHistoryRecordLocked(rec);
    }

    void addHistoryRecordLocked(HistoryItem rec) {
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

    void clearHistoryLocked() {
        this.mHistoryBaseTime = 0;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryBuffer.setDataSize(0);
        this.mHistoryBuffer.setDataPosition(0);
        this.mHistoryBuffer.setDataCapacity(131072);
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

    public void updateTimeBasesLocked(boolean unplugged, boolean screenOff, long uptime, long realtime) {
        int i;
        if (this.mOnBatteryTimeBase.setRunning(unplugged, uptime, realtime)) {
            for (i = 0; i < this.mUidStats.size(); i++) {
                ((Uid) this.mUidStats.valueAt(i)).updateOnBatteryBgTimeBase(uptime, realtime);
            }
        }
        boolean unpluggedScreenOff = unplugged ? screenOff : false;
        if (unpluggedScreenOff != this.mOnBatteryScreenOffTimeBase.isRunning()) {
            updateKernelWakelocksLocked();
            updateBatteryPropertiesLocked();
            updateCpuTimeLocked(true);
            this.mOnBatteryScreenOffTimeBase.setRunning(unpluggedScreenOff, uptime, realtime);
            for (i = 0; i < this.mUidStats.size(); i++) {
                ((Uid) this.mUidStats.valueAt(i)).updateOnBatteryScreenOffBgTimeBase(uptime, realtime);
            }
        }
    }

    private void updateBatteryPropertiesLocked() {
        try {
            IBatteryPropertiesRegistrar registrar = Stub.asInterface(ServiceManager.getService("batteryproperties"));
            if (registrar != null) {
                registrar.scheduleUpdate();
            }
        } catch (RemoteException e) {
        }
    }

    public void addIsolatedUidLocked(int isolatedUid, int appUid) {
        this.mIsolatedUids.put(isolatedUid, appUid);
    }

    public void scheduleRemoveIsolatedUidLocked(int isolatedUid, int appUid) {
        if (this.mIsolatedUids.get(isolatedUid, -1) == appUid && this.mExternalSync != null) {
            this.mExternalSync.scheduleCpuSyncDueToRemovedUid(isolatedUid);
        }
    }

    public void removeIsolatedUidLocked(int isolatedUid) {
        this.mIsolatedUids.delete(isolatedUid);
        this.mKernelUidCpuTimeReader.removeUid(isolatedUid);
        this.mKernelUidCpuFreqTimeReader.removeUid(isolatedUid);
    }

    public int mapUid(int uid) {
        int isolated = this.mIsolatedUids.get(uid, -1);
        return isolated > 0 ? isolated : uid;
    }

    public void noteEventLocked(int code, String name, int uid) {
        uid = mapUid(uid);
        if (this.mActiveEvents.updateState(code, name, uid, 0)) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), code, name, uid);
        }
    }

    boolean ensureStartClockTime(long currentTime) {
        if (currentTime <= 31536000000L || this.mStartClockTime >= currentTime - 31536000000L) {
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
        uid = mapUid(uid);
        if (isOnBattery()) {
            getUidStatsLocked(uid).getProcessStatsLocked(name).incStartsLocked();
        }
        if (this.mActiveEvents.updateState(32769, name, uid, 0) && this.mRecordAllHistory) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 32769, name, uid);
        }
    }

    public void noteProcessCrashLocked(String name, int uid) {
        uid = mapUid(uid);
        if (isOnBattery()) {
            getUidStatsLocked(uid).getProcessStatsLocked(name).incNumCrashesLocked();
        }
    }

    public void noteProcessAnrLocked(String name, int uid) {
        uid = mapUid(uid);
        if (isOnBattery()) {
            getUidStatsLocked(uid).getProcessStatsLocked(name).incNumAnrsLocked();
        }
    }

    public void noteUidProcessStateLocked(int uid, int state) {
        getUidStatsLocked(mapUid(uid)).updateUidProcessStateLocked(state);
    }

    public void noteProcessFinishLocked(String name, int uid) {
        uid = mapUid(uid);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT1, name, uid, 0) && this.mRecordAllHistory) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), GL10.GL_LIGHT1, name, uid);
        }
    }

    public void noteSyncStartLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStartSyncLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(32772, name, uid, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 32772, name, uid);
        }
    }

    public void noteSyncFinishLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStopSyncLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT4, name, uid, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT4, name, uid);
        }
    }

    public void noteJobStartLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStartJobLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL11ExtensionPack.GL_FUNC_ADD, name, uid, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL11ExtensionPack.GL_FUNC_ADD, name, uid);
        }
    }

    public void noteJobFinishLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStopJobLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT6, name, uid, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT6, name, uid);
        }
    }

    public void noteAlarmStartLocked(String name, int uid) {
        if (this.mRecordAllHistory) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            if (this.mActiveEvents.updateState(32781, name, uid, 0)) {
                addHistoryEventLocked(elapsedRealtime, uptime, 32781, name, uid);
            }
        }
    }

    public void noteAlarmFinishLocked(String name, int uid) {
        if (this.mRecordAllHistory) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            if (this.mActiveEvents.updateState(16397, name, uid, 0)) {
                addHistoryEventLocked(elapsedRealtime, uptime, 16397, name, uid);
            }
        }
    }

    private void requestWakelockCpuUpdate() {
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 5000);
        }
    }

    private void requestImmediateCpuUpdate() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    public void setRecordAllHistoryLocked(boolean enabled) {
        this.mRecordAllHistory = enabled;
        HashMap<String, SparseIntArray> active;
        long mSecRealtime;
        long mSecUptime;
        SparseIntArray uids;
        int j;
        if (enabled) {
            active = this.mActiveEvents.getStateForEvent(1);
            if (active != null) {
                mSecRealtime = this.mClocks.elapsedRealtime();
                mSecUptime = this.mClocks.uptimeMillis();
                for (Entry<String, SparseIntArray> ent : active.entrySet()) {
                    uids = (SparseIntArray) ent.getValue();
                    for (j = 0; j < uids.size(); j++) {
                        addHistoryEventLocked(mSecRealtime, mSecUptime, 32769, (String) ent.getKey(), uids.keyAt(j));
                    }
                }
                return;
            }
            return;
        }
        this.mActiveEvents.removeEvents(5);
        this.mActiveEvents.removeEvents(13);
        active = this.mActiveEvents.getStateForEvent(1);
        if (active != null) {
            mSecRealtime = this.mClocks.elapsedRealtime();
            mSecUptime = this.mClocks.uptimeMillis();
            for (Entry<String, SparseIntArray> ent2 : active.entrySet()) {
                uids = (SparseIntArray) ent2.getValue();
                for (j = 0; j < uids.size(); j++) {
                    addHistoryEventLocked(mSecRealtime, mSecUptime, GL10.GL_LIGHT1, (String) ent2.getKey(), uids.keyAt(j));
                }
            }
        }
    }

    public void setNoAutoReset(boolean enabled) {
        this.mNoAutoReset = enabled;
    }

    public void setPretendScreenOff(boolean pretendScreenOff) {
        this.mPretendScreenOff = pretendScreenOff;
        noteScreenStateLocked(pretendScreenOff ? 1 : 2);
    }

    public void noteStartWakeLocked(int uid, int pid, String name, String historyName, int type, boolean unimportantForLogging, long elapsedRealtime, long uptime) {
        uid = mapUid(uid);
        if (type == 0) {
            aggregateLastWakeupUptimeLocked(uptime);
            if (historyName == null) {
                historyName = name;
            }
            if (this.mRecordAllHistory && this.mActiveEvents.updateState(32773, historyName, uid, 0)) {
                addHistoryEventLocked(elapsedRealtime, uptime, 32773, historyName, uid);
            }
            HistoryTag historyTag;
            if (this.mWakeLockNesting == 0) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states |= 1073741824;
                this.mHistoryCur.wakelockTag = this.mHistoryCur.localWakelockTag;
                historyTag = this.mHistoryCur.wakelockTag;
                this.mInitialAcquireWakeName = historyName;
                historyTag.string = historyName;
                historyTag = this.mHistoryCur.wakelockTag;
                this.mInitialAcquireWakeUid = uid;
                historyTag.uid = uid;
                this.mWakeLockImportant = unimportantForLogging ^ 1;
                addHistoryRecordLocked(elapsedRealtime, uptime);
            } else if (!(this.mWakeLockImportant || (unimportantForLogging ^ 1) == 0 || this.mHistoryLastWritten.cmd != (byte) 0)) {
                if (this.mHistoryLastWritten.wakelockTag != null) {
                    this.mHistoryLastWritten.wakelockTag = null;
                    this.mHistoryCur.wakelockTag = this.mHistoryCur.localWakelockTag;
                    historyTag = this.mHistoryCur.wakelockTag;
                    this.mInitialAcquireWakeName = historyName;
                    historyTag.string = historyName;
                    historyTag = this.mHistoryCur.wakelockTag;
                    this.mInitialAcquireWakeUid = uid;
                    historyTag.uid = uid;
                    addHistoryRecordLocked(elapsedRealtime, uptime);
                }
                this.mWakeLockImportant = true;
            }
            this.mWakeLockNesting++;
        }
        if (uid >= 0) {
            if (this.mOnBatteryScreenOffTimeBase.isRunning()) {
                requestWakelockCpuUpdate();
            }
            getUidStatsLocked(uid).noteStartWakeLocked(pid, name, type, elapsedRealtime);
        }
    }

    public void noteStopWakeLocked(int uid, int pid, String name, String historyName, int type, long elapsedRealtime, long uptime) {
        uid = mapUid(uid);
        if (type == 0) {
            this.mWakeLockNesting--;
            if (this.mRecordAllHistory) {
                if (historyName == null) {
                    historyName = name;
                }
                if (this.mActiveEvents.updateState(GL10.GL_LIGHT5, historyName, uid, 0)) {
                    addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT5, historyName, uid);
                }
            }
            if (this.mWakeLockNesting == 0) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states &= -1073741825;
                this.mInitialAcquireWakeName = null;
                this.mInitialAcquireWakeUid = -1;
                addHistoryRecordLocked(elapsedRealtime, uptime);
            }
        }
        if (uid >= 0) {
            if (this.mOnBatteryScreenOffTimeBase.isRunning()) {
                requestWakelockCpuUpdate();
            }
            getUidStatsLocked(uid).noteStopWakeLocked(pid, name, type, elapsedRealtime);
        }
    }

    public void noteStartWakeFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteStartWakeLocked(ws.get(i), pid, name, historyName, type, unimportantForLogging, elapsedRealtime, uptime);
        }
    }

    public void noteChangeWakelockFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) {
        int i;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int NN = newWs.size();
        for (i = 0; i < NN; i++) {
            noteStartWakeLocked(newWs.get(i), newPid, newName, newHistoryName, newType, newUnimportantForLogging, elapsedRealtime, uptime);
        }
        int NO = ws.size();
        for (i = 0; i < NO; i++) {
            noteStopWakeLocked(ws.get(i), pid, name, historyName, type, elapsedRealtime, uptime);
        }
    }

    public void noteStopWakeFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteStopWakeLocked(ws.get(i), pid, name, historyName, type, elapsedRealtime, uptime);
        }
    }

    public void noteLongPartialWakelockStart(String name, String historyName, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (historyName == null) {
            historyName = name;
        }
        if (this.mActiveEvents.updateState(32788, historyName, uid, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 32788, historyName, uid);
        }
    }

    public void noteLongPartialWakelockFinish(String name, String historyName, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (historyName == null) {
            historyName = name;
        }
        if (this.mActiveEvents.updateState(16404, historyName, uid, 0)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 16404, historyName, uid);
        }
    }

    void aggregateLastWakeupUptimeLocked(long uptimeMs) {
        if (this.mLastWakeupReason != null) {
            getWakeupReasonTimerLocked(this.mLastWakeupReason).add(1000 * (uptimeMs - this.mLastWakeupUptimeMs), 1);
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
        this.mHandler.removeMessages(1);
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
        Uid u = (Uid) this.mUidStats.get(mapUid(uid));
        if (u != null) {
            u.mPids.remove(pid);
        }
    }

    public long getProcessWakeTime(int uid, int pid, long realtime) {
        long j = 0;
        Uid u = (Uid) this.mUidStats.get(mapUid(uid));
        if (u != null) {
            Pid p = (Pid) u.mPids.get(pid);
            if (p != null) {
                long j2 = p.mWakeSumMs;
                if (p.mWakeNesting > 0) {
                    j = realtime - p.mWakeStartMs;
                }
                return j + j2;
            }
        }
        return 0;
    }

    public void reportExcessiveWakeLocked(int uid, String proc, long overTime, long usedTime) {
        Uid u = (Uid) this.mUidStats.get(mapUid(uid));
        if (u != null) {
            u.reportExcessiveWakeLocked(proc, overTime, usedTime);
        }
    }

    public void reportExcessiveCpuLocked(int uid, String proc, long overTime, long usedTime) {
        Uid u = (Uid) this.mUidStats.get(mapUid(uid));
        if (u != null) {
            u.reportExcessiveCpuLocked(proc, overTime, usedTime);
        }
    }

    public void noteStartSensorLocked(int uid, int sensor) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mSensorNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states |= 8388608;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mSensorNesting++;
        getUidStatsLocked(uid).noteStartSensor(sensor, elapsedRealtime);
    }

    public void noteStopSensorLocked(int uid, int sensor) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mSensorNesting--;
        if (this.mSensorNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states &= -8388609;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid).noteStopSensor(sensor, elapsedRealtime);
    }

    public void noteStartGpsLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mGpsNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states |= 536870912;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mGpsNesting++;
        getUidStatsLocked(uid).noteStartGps(elapsedRealtime);
    }

    public void noteStopGpsLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mGpsNesting--;
        if (this.mGpsNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states &= -536870913;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid).noteStopGps(elapsedRealtime);
    }

    public void noteScreenStateLocked(int state) {
        if (this.mPretendScreenOff) {
            state = 1;
        }
        if (state > 4) {
            switch (state) {
                case 5:
                    state = 2;
                    break;
                default:
                    Slog.wtf(TAG, "Unknown screen state (not mapped): " + state);
                    break;
            }
        }
        if (this.mScreenState != state) {
            recordDailyStatsIfNeededLocked(true);
            int oldState = this.mScreenState;
            this.mScreenState = state;
            if (state != 0) {
                int stepState = state - 1;
                if ((stepState & 3) == stepState) {
                    this.mModStepMode |= (this.mCurStepMode & 3) ^ stepState;
                    this.mCurStepMode = (this.mCurStepMode & -4) | stepState;
                } else {
                    Slog.wtf(TAG, "Unexpected screen state: " + state);
                }
            }
            long elapsedRealtime;
            long uptime;
            HistoryItem historyItem;
            if (state == 2) {
                elapsedRealtime = this.mClocks.elapsedRealtime();
                uptime = this.mClocks.uptimeMillis();
                historyItem = this.mHistoryCur;
                historyItem.states |= 1048576;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mScreenOnTimer.startRunningLocked(elapsedRealtime);
                if (this.mScreenBrightnessBin >= 0) {
                    this.mScreenBrightnessTimer[this.mScreenBrightnessBin].startRunningLocked(elapsedRealtime);
                }
                updateTimeBasesLocked(this.mOnBatteryTimeBase.isRunning(), false, this.mClocks.uptimeMillis() * 1000, 1000 * elapsedRealtime);
                noteStartWakeLocked(-1, -1, "screen", null, 0, false, elapsedRealtime, uptime);
                if (this.mOnBatteryInternal) {
                    updateDischargeScreenLevelsLocked(false, true);
                }
            } else if (oldState == 2) {
                elapsedRealtime = this.mClocks.elapsedRealtime();
                uptime = this.mClocks.uptimeMillis();
                historyItem = this.mHistoryCur;
                historyItem.states &= -1048577;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mScreenOnTimer.stopRunningLocked(elapsedRealtime);
                if (this.mScreenBrightnessBin >= 0) {
                    this.mScreenBrightnessTimer[this.mScreenBrightnessBin].stopRunningLocked(elapsedRealtime);
                }
                noteStopWakeLocked(-1, -1, "screen", "screen", 0, elapsedRealtime, uptime);
                updateTimeBasesLocked(this.mOnBatteryTimeBase.isRunning(), true, this.mClocks.uptimeMillis() * 1000, 1000 * elapsedRealtime);
                if (this.mOnBatteryInternal) {
                    updateDischargeScreenLevelsLocked(true, false);
                }
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
        uid = mapUid(uid);
        addHistoryEventLocked(elapsedRealtimeMillis, uptimeMillis, 19, LogException.NO_VALUE, uid);
        getUidStatsLocked(uid).noteMobileRadioApWakeupLocked();
    }

    public boolean noteMobileRadioPowerStateLocked(int powerState, long timestampNs, int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mMobileRadioPowerState != powerState) {
            long realElapsedRealtimeMs;
            boolean active = powerState != 2 ? powerState == 3 : true;
            HistoryItem historyItem;
            if (active) {
                if (uid > 0) {
                    noteMobileRadioApWakeupLocked(elapsedRealtime, uptime, uid);
                }
                realElapsedRealtimeMs = timestampNs / TimeUtils.NANOS_PER_MS;
                this.mMobileRadioActiveStartTime = realElapsedRealtimeMs;
                historyItem = this.mHistoryCur;
                historyItem.states |= 33554432;
            } else {
                realElapsedRealtimeMs = timestampNs / TimeUtils.NANOS_PER_MS;
                long lastUpdateTimeMs = this.mMobileRadioActiveStartTime;
                if (realElapsedRealtimeMs < lastUpdateTimeMs) {
                    Slog.wtf(TAG, "Data connection inactive timestamp " + realElapsedRealtimeMs + " is before start time " + lastUpdateTimeMs);
                    realElapsedRealtimeMs = elapsedRealtime;
                } else if (realElapsedRealtimeMs < elapsedRealtime) {
                    this.mMobileRadioActiveAdjustedTime.addCountLocked(elapsedRealtime - realElapsedRealtimeMs);
                }
                historyItem = this.mHistoryCur;
                historyItem.states &= -33554433;
            }
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mMobileRadioPowerState = powerState;
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
            int stepState = enabled ? 4 : 0;
            this.mModStepMode |= (this.mCurStepMode & 4) ^ stepState;
            this.mCurStepMode = (this.mCurStepMode & -5) | stepState;
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mPowerSaveModeEnabled = enabled;
            HistoryItem historyItem;
            if (enabled) {
                historyItem = this.mHistoryCur;
                historyItem.states2 |= Integer.MIN_VALUE;
                this.mPowerSaveModeEnabledTimer.startRunningLocked(elapsedRealtime);
            } else {
                historyItem = this.mHistoryCur;
                historyItem.states2 &= Integer.MAX_VALUE;
                this.mPowerSaveModeEnabledTimer.stopRunningLocked(elapsedRealtime);
            }
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
    }

    public void noteDeviceIdleModeLocked(int mode, String activeReason, int activeUid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        boolean nowIdling = mode == 2;
        if (this.mDeviceIdling && (nowIdling ^ 1) != 0 && activeReason == null) {
            nowIdling = true;
        }
        boolean nowLightIdling = mode == 1;
        if (this.mDeviceLightIdling && (nowLightIdling ^ 1) != 0 && (nowIdling ^ 1) != 0 && activeReason == null) {
            nowLightIdling = true;
        }
        if (activeReason != null && (this.mDeviceIdling || this.mDeviceLightIdling)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 10, activeReason, activeUid);
        }
        if (this.mDeviceIdling != nowIdling) {
            this.mDeviceIdling = nowIdling;
            int stepState = nowIdling ? 8 : 0;
            this.mModStepMode |= (this.mCurStepMode & 8) ^ stepState;
            this.mCurStepMode = (this.mCurStepMode & -9) | stepState;
            if (nowIdling) {
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
        if (this.mDeviceIdleMode != mode) {
            this.mHistoryCur.states2 = (this.mHistoryCur.states2 & -100663297) | (mode << 25);
            addHistoryRecordLocked(elapsedRealtime, uptime);
            long lastDuration = elapsedRealtime - this.mLastIdleTimeStart;
            this.mLastIdleTimeStart = elapsedRealtime;
            if (this.mDeviceIdleMode == 1) {
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
            if (mode == 1) {
                this.mDeviceIdleModeLightTimer.startRunningLocked(elapsedRealtime);
            } else if (mode == 2) {
                this.mDeviceIdleModeFullTimer.startRunningLocked(elapsedRealtime);
            }
            this.mDeviceIdleMode = mode;
        }
    }

    public void notePackageInstalledLocked(String pkgName, int versionCode) {
        addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 11, pkgName, versionCode);
        PackageChange pc = new PackageChange();
        pc.mPackageName = pkgName;
        pc.mUpdate = true;
        pc.mVersionCode = versionCode;
        addPackageChange(pc);
    }

    public void notePackageUninstalledLocked(String pkgName) {
        addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 12, pkgName, 0);
        PackageChange pc = new PackageChange();
        pc.mPackageName = pkgName;
        pc.mUpdate = true;
        addPackageChange(pc);
    }

    private void addPackageChange(PackageChange pc) {
        if (this.mDailyPackageChanges == null) {
            this.mDailyPackageChanges = new ArrayList();
        }
        this.mDailyPackageChanges.add(pc);
    }

    public void notePhoneOnLocked() {
        if (!this.mPhoneOn) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= 8388608;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mPhoneOn = true;
            this.mPhoneOnTimer.startRunningLocked(elapsedRealtime);
        }
    }

    public void notePhoneOffLocked() {
        if (this.mPhoneOn) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -8388609;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mPhoneOn = false;
            this.mPhoneOnTimer.stopRunningLocked(elapsedRealtime);
        }
    }

    void stopAllPhoneSignalStrengthTimersLocked(int except) {
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
        HistoryItem historyItem;
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
                historyItem = this.mHistoryCur;
                historyItem.states |= 2097152;
                newHistory = true;
                this.mPhoneSignalScanningTimer.startRunningLocked(elapsedRealtime);
            }
        }
        if (!scanning && this.mPhoneSignalScanningTimer.isRunningLocked()) {
            historyItem = this.mHistoryCur;
            historyItem.states &= -2097153;
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
            switch (dataType) {
                case 1:
                    bin = 1;
                    break;
                case 2:
                    bin = 2;
                    break;
                case 3:
                    bin = 3;
                    break;
                case 4:
                    bin = 4;
                    break;
                case 5:
                    bin = 5;
                    break;
                case 6:
                    bin = 6;
                    break;
                case 7:
                    bin = 7;
                    break;
                case 8:
                    bin = 8;
                    break;
                case 9:
                    bin = 9;
                    break;
                case 10:
                    bin = 10;
                    break;
                case 11:
                    bin = 11;
                    break;
                case 12:
                    bin = 12;
                    break;
                case 13:
                    bin = 13;
                    break;
                case 14:
                    bin = 14;
                    break;
                case 15:
                    bin = 15;
                    break;
                default:
                    bin = 16;
                    break;
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
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= 268435456;
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
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -268435457;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mWifiOn = false;
            this.mWifiOnTimer.stopRunningLocked(elapsedRealtime);
            scheduleSyncExternalStatsLocked("wifi-on", 2);
        }
    }

    public void noteAudioOnLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mAudioOnNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states |= 4194304;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mAudioOnTimer.startRunningLocked(elapsedRealtime);
        }
        this.mAudioOnNesting++;
        getUidStatsLocked(uid).noteAudioTurnedOnLocked(elapsedRealtime);
    }

    public void noteAudioOffLocked(int uid) {
        if (this.mAudioOnNesting != 0) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mAudioOnNesting - 1;
            this.mAudioOnNesting = i;
            if (i == 0) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states &= -4194305;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mAudioOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid).noteAudioTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteVideoOnLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mVideoOnNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= 1073741824;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mVideoOnTimer.startRunningLocked(elapsedRealtime);
        }
        this.mVideoOnNesting++;
        getUidStatsLocked(uid).noteVideoTurnedOnLocked(elapsedRealtime);
    }

    public void noteVideoOffLocked(int uid) {
        if (this.mVideoOnNesting != 0) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mVideoOnNesting - 1;
            this.mVideoOnNesting = i;
            if (i == 0) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states2 &= -1073741825;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mVideoOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid).noteVideoTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteResetAudioLocked() {
        if (this.mAudioOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mAudioOnNesting = 0;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states &= -4194305;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mAudioOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetAudioLocked(elapsedRealtime);
            }
        }
    }

    public void noteResetVideoLocked() {
        if (this.mVideoOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mAudioOnNesting = 0;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -1073741825;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mVideoOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetVideoLocked(elapsedRealtime);
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
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int i = this.mFlashlightOnNesting;
        this.mFlashlightOnNesting = i + 1;
        if (i == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= 134217728;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mFlashlightOnTimer.startRunningLocked(elapsedRealtime);
        }
        getUidStatsLocked(uid).noteFlashlightTurnedOnLocked(elapsedRealtime);
    }

    public void noteFlashlightOffLocked(int uid) {
        if (this.mFlashlightOnNesting != 0) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mFlashlightOnNesting - 1;
            this.mFlashlightOnNesting = i;
            if (i == 0) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states2 &= -134217729;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mFlashlightOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid).noteFlashlightTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteCameraOnLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int i = this.mCameraOnNesting;
        this.mCameraOnNesting = i + 1;
        if (i == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= 2097152;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mCameraOnTimer.startRunningLocked(elapsedRealtime);
        }
        getUidStatsLocked(uid).noteCameraTurnedOnLocked(elapsedRealtime);
    }

    public void noteCameraOffLocked(int uid) {
        if (this.mCameraOnNesting != 0) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            int i = this.mCameraOnNesting - 1;
            this.mCameraOnNesting = i;
            if (i == 0) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states2 &= -2097153;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mCameraOnTimer.stopRunningLocked(elapsedRealtime);
            }
            getUidStatsLocked(uid).noteCameraTurnedOffLocked(elapsedRealtime);
        }
    }

    public void noteResetCameraLocked() {
        if (this.mCameraOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mCameraOnNesting = 0;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -2097153;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mCameraOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetCameraLocked(elapsedRealtime);
            }
        }
    }

    public void noteResetFlashlightLocked() {
        if (this.mFlashlightOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mFlashlightOnNesting = 0;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -134217729;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mFlashlightOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetFlashlightLocked(elapsedRealtime);
            }
        }
    }

    private void noteBluetoothScanStartedLocked(int uid, boolean isUnoptimized) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mBluetoothScanNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= 1048576;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.startRunningLocked(elapsedRealtime);
        }
        this.mBluetoothScanNesting++;
        getUidStatsLocked(uid).noteBluetoothScanStartedLocked(elapsedRealtime, isUnoptimized);
    }

    public void noteBluetoothScanStartedFromSourceLocked(WorkSource ws, boolean isUnoptimized) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteBluetoothScanStartedLocked(ws.get(i), isUnoptimized);
        }
    }

    private void noteBluetoothScanStoppedLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mBluetoothScanNesting--;
        if (this.mBluetoothScanNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -1048577;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.stopRunningLocked(elapsedRealtime);
        }
        getUidStatsLocked(uid).noteBluetoothScanStoppedLocked(elapsedRealtime);
    }

    public void noteBluetoothScanStoppedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteBluetoothScanStoppedLocked(ws.get(i));
        }
    }

    public void noteResetBluetoothScanLocked() {
        if (this.mBluetoothScanNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mBluetoothScanNesting = 0;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -1048577;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = 0; i < this.mUidStats.size(); i++) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetBluetoothScanLocked(elapsedRealtime);
            }
        }
    }

    public void noteBluetoothScanResultsFromSourceLocked(WorkSource ws, int numNewResults) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            getUidStatsLocked(mapUid(ws.get(i))).noteBluetoothScanResultsLocked(numNewResults);
        }
    }

    private void noteWifiRadioApWakeupLocked(long elapsedRealtimeMillis, long uptimeMillis, int uid) {
        uid = mapUid(uid);
        addHistoryEventLocked(elapsedRealtimeMillis, uptimeMillis, 19, LogException.NO_VALUE, uid);
        getUidStatsLocked(uid).noteWifiRadioApWakeupLocked();
    }

    public void noteWifiRadioPowerState(int powerState, long timestampNs, int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiRadioPowerState != powerState) {
            boolean active = powerState != 2 ? powerState == 3 : true;
            HistoryItem historyItem;
            if (active) {
                if (uid > 0) {
                    noteWifiRadioApWakeupLocked(elapsedRealtime, uptime, uid);
                }
                historyItem = this.mHistoryCur;
                historyItem.states |= 67108864;
            } else {
                historyItem = this.mHistoryCur;
                historyItem.states &= -67108865;
            }
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mWifiRadioPowerState = powerState;
        }
    }

    public void noteWifiRunningLocked(WorkSource ws) {
        if (this.mGlobalWifiRunning) {
            Log.w(TAG, "noteWifiRunningLocked -- called while WIFI running");
            return;
        }
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        HistoryItem historyItem = this.mHistoryCur;
        historyItem.states2 |= 536870912;
        addHistoryRecordLocked(elapsedRealtime, uptime);
        this.mGlobalWifiRunning = true;
        this.mGlobalWifiRunningTimer.startRunningLocked(elapsedRealtime);
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            getUidStatsLocked(mapUid(ws.get(i))).noteWifiRunningLocked(elapsedRealtime);
        }
        scheduleSyncExternalStatsLocked("wifi-running", 2);
    }

    public void noteWifiRunningChangedLocked(WorkSource oldWs, WorkSource newWs) {
        if (this.mGlobalWifiRunning) {
            int i;
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            int N = oldWs.size();
            for (i = 0; i < N; i++) {
                getUidStatsLocked(mapUid(oldWs.get(i))).noteWifiStoppedLocked(elapsedRealtime);
            }
            N = newWs.size();
            for (i = 0; i < N; i++) {
                getUidStatsLocked(mapUid(newWs.get(i))).noteWifiRunningLocked(elapsedRealtime);
            }
            return;
        }
        Log.w(TAG, "noteWifiRunningChangedLocked -- called while WIFI not running");
    }

    public void noteWifiStoppedLocked(WorkSource ws) {
        if (this.mGlobalWifiRunning) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -536870913;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mGlobalWifiRunning = false;
            this.mGlobalWifiRunningTimer.stopRunningLocked(elapsedRealtime);
            int N = ws.size();
            for (int i = 0; i < N; i++) {
                getUidStatsLocked(mapUid(ws.get(i))).noteWifiStoppedLocked(elapsedRealtime);
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

    void stopAllWifiSignalStrengthTimersLocked(int except) {
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
            this.mWifiSignalStrengthBin = strengthBin;
        }
    }

    public void noteFullWifiLockAcquiredLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiFullLockNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states |= 268435456;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiFullLockNesting++;
        getUidStatsLocked(uid).noteFullWifiLockAcquiredLocked(elapsedRealtime);
    }

    public void noteFullWifiLockReleasedLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mWifiFullLockNesting--;
        if (this.mWifiFullLockNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states &= -268435457;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid).noteFullWifiLockReleasedLocked(elapsedRealtime);
    }

    public void noteWifiScanStartedLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiScanNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states |= 134217728;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiScanNesting++;
        getUidStatsLocked(uid).noteWifiScanStartedLocked(elapsedRealtime);
        LogPower.push(158, Integer.toString(uid));
    }

    public void noteWifiScanStoppedLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mWifiScanNesting--;
        if (this.mWifiScanNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states &= -134217729;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid).noteWifiScanStoppedLocked(elapsedRealtime);
        LogPower.push(159, Integer.toString(uid));
    }

    public void noteWifiBatchedScanStartedLocked(int uid, int csph) {
        uid = mapUid(uid);
        getUidStatsLocked(uid).noteWifiBatchedScanStartedLocked(csph, this.mClocks.elapsedRealtime());
    }

    public void noteWifiBatchedScanStoppedLocked(int uid) {
        uid = mapUid(uid);
        getUidStatsLocked(uid).noteWifiBatchedScanStoppedLocked(this.mClocks.elapsedRealtime());
    }

    public void noteWifiMulticastEnabledLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiMulticastNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states |= 65536;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiMulticastNesting++;
        getUidStatsLocked(uid).noteWifiMulticastEnabledLocked(elapsedRealtime);
    }

    public void noteWifiMulticastDisabledLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        this.mWifiMulticastNesting--;
        if (this.mWifiMulticastNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states &= -65537;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        getUidStatsLocked(uid).noteWifiMulticastDisabledLocked(elapsedRealtime);
    }

    public void noteFullWifiLockAcquiredFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteFullWifiLockAcquiredLocked(ws.get(i));
        }
    }

    public void noteFullWifiLockReleasedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteFullWifiLockReleasedLocked(ws.get(i));
        }
    }

    public void noteWifiScanStartedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiScanStartedLocked(ws.get(i));
        }
    }

    public void noteWifiScanStoppedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiScanStoppedLocked(ws.get(i));
        }
    }

    public void noteWifiBatchedScanStartedFromSourceLocked(WorkSource ws, int csph) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiBatchedScanStartedLocked(ws.get(i), csph);
        }
    }

    public void noteWifiBatchedScanStoppedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiBatchedScanStoppedLocked(ws.get(i));
        }
    }

    public void noteWifiMulticastEnabledFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiMulticastEnabledLocked(ws.get(i));
        }
    }

    public void noteWifiMulticastDisabledFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = 0; i < N; i++) {
            noteWifiMulticastDisabledLocked(ws.get(i));
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

    public long getScreenBrightnessTime(int brightnessBin, long elapsedRealtimeUs, int which) {
        if (this.mScreenBrightnessTimer[brightnessBin] != null) {
            return this.mScreenBrightnessTimer[brightnessBin].getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
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

    public int getPhoneSignalStrengthCount(int strengthBin, int which) {
        return this.mPhoneSignalStrengthsTimer[strengthBin].getCountLocked(which);
    }

    public long getPhoneDataConnectionTime(int dataType, long elapsedRealtimeUs, int which) {
        return this.mPhoneDataConnectionsTimer[dataType].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getPhoneDataConnectionCount(int dataType, int which) {
        return this.mPhoneDataConnectionsTimer[dataType].getCountLocked(which);
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

    public long getWifiOnTime(long elapsedRealtimeUs, int which) {
        return this.mWifiOnTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
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

    public long getWifiSupplStateTime(int state, long elapsedRealtimeUs, int which) {
        return this.mWifiSupplStateTimer[state].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getWifiSupplStateCount(int state, int which) {
        return this.mWifiSupplStateTimer[state].getCountLocked(which);
    }

    public long getWifiSignalStrengthTime(int strengthBin, long elapsedRealtimeUs, int which) {
        return this.mWifiSignalStrengthsTimer[strengthBin].getTotalTimeLocked(elapsedRealtimeUs, which);
    }

    public int getWifiSignalStrengthCount(int strengthBin, int which) {
        return this.mWifiSignalStrengthsTimer[strengthBin].getCountLocked(which);
    }

    public ControllerActivityCounter getBluetoothControllerActivity() {
        return this.mBluetoothActivity;
    }

    public ControllerActivityCounter getWifiControllerActivity() {
        return this.mWifiActivity;
    }

    public ControllerActivityCounter getModemControllerActivity() {
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
        return 159;
    }

    public boolean getIsOnBattery() {
        return this.mOnBattery;
    }

    public SparseArray<? extends android.os.BatteryStats.Uid> getUidStats() {
        return this.mUidStats;
    }

    private static void detachTimerIfNotNull(Timer timer) {
        if (timer != null) {
            timer.detach();
        }
    }

    private static boolean resetTimerIfNotNull(Timer timer, boolean detachIfReset) {
        if (timer != null) {
            return timer.reset(detachIfReset);
        }
        return true;
    }

    private static boolean resetTimerIfNotNull(DualTimer timer, boolean detachIfReset) {
        if (timer != null) {
            return timer.reset(detachIfReset);
        }
        return true;
    }

    private static void detachLongCounterIfNotNull(LongSamplingCounter counter) {
        if (counter != null) {
            counter.detach();
        }
    }

    private static void resetLongCounterIfNotNull(LongSamplingCounter counter, boolean detachIfReset) {
        if (counter != null) {
            counter.reset(detachIfReset);
        }
    }

    public long[] getCpuFreqs() {
        return this.mCpuFreqs;
    }

    public BatteryStatsImpl(File systemDir, Handler handler, ExternalStatsSync externalSync) {
        this(new SystemClocks(), systemDir, handler, externalSync, null);
    }

    public BatteryStatsImpl(File systemDir, Handler handler, ExternalStatsSync externalSync, PlatformIdleStateCallback cb) {
        this(new SystemClocks(), systemDir, handler, externalSync, cb);
    }

    public BatteryStatsImpl(Clocks clocks, File systemDir, Handler handler, ExternalStatsSync externalSync, PlatformIdleStateCallback cb) {
        int i;
        this.mKernelWakelockReader = new KernelWakelockReader();
        this.mTmpWakelockStats = new KernelWakelockStats();
        this.mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
        this.mKernelUidCpuFreqTimeReader = new KernelUidCpuFreqTimeReader();
        this.mKernelMemoryBandwidthStats = new KernelMemoryBandwidthStats();
        this.mKernelMemoryStats = new LongSparseArray();
        this.mIsolatedUids = new SparseIntArray();
        this.mUidStats = new SparseArray();
        this.mPartialTimers = new ArrayList();
        this.mFullTimers = new ArrayList();
        this.mWindowTimers = new ArrayList();
        this.mDrawTimers = new ArrayList();
        this.mSensorTimers = new SparseArray();
        this.mWifiRunningTimers = new ArrayList();
        this.mFullWifiLockTimers = new ArrayList();
        this.mWifiMulticastTimers = new ArrayList();
        this.mWifiScanTimers = new ArrayList();
        this.mWifiBatchedScanTimers = new SparseArray();
        this.mAudioTurnedOnTimers = new ArrayList();
        this.mVideoTurnedOnTimers = new ArrayList();
        this.mFlashlightTurnedOnTimers = new ArrayList();
        this.mCameraTurnedOnTimers = new ArrayList();
        this.mBluetoothScanOnTimers = new ArrayList();
        this.mLastPartialTimers = new ArrayList();
        this.mOnBatteryTimeBase = new TimeBase();
        this.mOnBatteryScreenOffTimeBase = new TimeBase();
        this.mActiveEvents = new HistoryEventTracker();
        this.mHaveBatteryLevel = false;
        this.mRecordingHistory = false;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new HistoryItem();
        this.mHistoryLastLastWritten = new HistoryItem();
        this.mHistoryReadTmp = new HistoryItem();
        this.mHistoryAddTmp = new HistoryItem();
        this.mHistoryTagPool = new HashMap();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = false;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryCur = new HistoryItem();
        this.mLastHistoryStepDetails = null;
        this.mLastHistoryStepLevel = (byte) 0;
        this.mCurHistoryStepDetails = new HistoryStepDetails();
        this.mReadHistoryStepDetails = new HistoryStepDetails();
        this.mTmpHistoryStepDetails = new HistoryStepDetails();
        this.mScreenState = 0;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[17];
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
        this.mMobileRadioPowerState = 1;
        this.mWifiRadioPowerState = 1;
        this.mCharging = true;
        this.mInitStepMode = 0;
        this.mCurStepMode = 0;
        this.mModStepMode = 0;
        this.mDischargeStepTracker = new LevelStepTracker(200);
        this.mDailyDischargeStepTracker = new LevelStepTracker(400);
        this.mChargeStepTracker = new LevelStepTracker(200);
        this.mDailyChargeStepTracker = new LevelStepTracker(400);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mMinLearnedBatteryCapacity = -1;
        this.mMaxLearnedBatteryCapacity = -1;
        this.mKernelWakelockStats = new HashMap();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap();
        this.mChangedStates = 0;
        this.mChangedStates2 = 0;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = 0;
        this.mWifiScanNesting = 0;
        this.mWifiMulticastNesting = 0;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        this.mNetworkStatsPool = new SynchronizedPool(6);
        this.mWifiNetworkLock = new Object();
        this.mWifiIfaces = EmptyArray.STRING;
        this.mLastWifiNetworkStats = new NetworkStats(0, -1);
        this.mModemNetworkLock = new Object();
        this.mModemIfaces = EmptyArray.STRING;
        this.mLastModemNetworkStats = new NetworkStats(0, -1);
        this.mPendingWrite = null;
        this.mWriteLock = new ReentrantLock();
        init(clocks);
        if (systemDir != null) {
            this.mFile = new JournaledFile(new File(systemDir, "batterystats.bin"), new File(systemDir, "batterystats.bin.tmp"));
        } else {
            this.mFile = null;
        }
        this.mCheckinFile = new AtomicFile(new File(systemDir, "batterystats-checkin.bin"));
        this.mDailyFile = new AtomicFile(new File(systemDir, "batterystats-daily.xml"));
        this.mExternalSync = externalSync;
        this.mHandler = new MyHandler(handler.getLooper());
        this.mStartCount++;
        this.mScreenOnTimer = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase);
        for (i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i] = new StopwatchTimer(this.mClocks, null, -100 - i, null, this.mOnBatteryTimeBase);
        }
        this.mInteractiveTimer = new StopwatchTimer(this.mClocks, null, -10, null, this.mOnBatteryTimeBase);
        this.mPowerSaveModeEnabledTimer = new StopwatchTimer(this.mClocks, null, -2, null, this.mOnBatteryTimeBase);
        this.mDeviceIdleModeLightTimer = new StopwatchTimer(this.mClocks, null, -11, null, this.mOnBatteryTimeBase);
        this.mDeviceIdleModeFullTimer = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase);
        this.mDeviceLightIdlingTimer = new StopwatchTimer(this.mClocks, null, -15, null, this.mOnBatteryTimeBase);
        this.mDeviceIdlingTimer = new StopwatchTimer(this.mClocks, null, -12, null, this.mOnBatteryTimeBase);
        this.mPhoneOnTimer = new StopwatchTimer(this.mClocks, null, -3, null, this.mOnBatteryTimeBase);
        for (i = 0; i < 5; i++) {
            this.mPhoneSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -200 - i, null, this.mOnBatteryTimeBase);
        }
        this.mPhoneSignalScanningTimer = new StopwatchTimer(this.mClocks, null, -199, null, this.mOnBatteryTimeBase);
        for (i = 0; i < 17; i++) {
            this.mPhoneDataConnectionsTimer[i] = new StopwatchTimer(this.mClocks, null, -300 - i, null, this.mOnBatteryTimeBase);
        }
        for (i = 0; i < 10; i++) {
            this.mNetworkByteActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase);
            this.mNetworkPacketActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase);
        }
        this.mWifiActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1);
        this.mBluetoothActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1);
        this.mModemActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 5);
        this.mMobileRadioActiveTimer = new StopwatchTimer(this.mClocks, null, -400, null, this.mOnBatteryTimeBase);
        this.mMobileRadioActivePerAppTimer = new StopwatchTimer(this.mClocks, null, -401, null, this.mOnBatteryTimeBase);
        this.mMobileRadioActiveAdjustedTime = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mMobileRadioActiveUnknownTime = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mMobileRadioActiveUnknownCount = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mWifiOnTimer = new StopwatchTimer(this.mClocks, null, -4, null, this.mOnBatteryTimeBase);
        this.mGlobalWifiRunningTimer = new StopwatchTimer(this.mClocks, null, -5, null, this.mOnBatteryTimeBase);
        for (i = 0; i < 8; i++) {
            this.mWifiStateTimer[i] = new StopwatchTimer(this.mClocks, null, -600 - i, null, this.mOnBatteryTimeBase);
        }
        for (i = 0; i < 13; i++) {
            this.mWifiSupplStateTimer[i] = new StopwatchTimer(this.mClocks, null, -700 - i, null, this.mOnBatteryTimeBase);
        }
        for (i = 0; i < 5; i++) {
            this.mWifiSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -800 - i, null, this.mOnBatteryTimeBase);
        }
        this.mAudioOnTimer = new StopwatchTimer(this.mClocks, null, -7, null, this.mOnBatteryTimeBase);
        this.mVideoOnTimer = new StopwatchTimer(this.mClocks, null, -8, null, this.mOnBatteryTimeBase);
        this.mFlashlightOnTimer = new StopwatchTimer(this.mClocks, null, -9, null, this.mOnBatteryTimeBase);
        this.mCameraOnTimer = new StopwatchTimer(this.mClocks, null, -13, null, this.mOnBatteryTimeBase);
        this.mBluetoothScanTimer = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase);
        this.mDischargeScreenOffCounter = new LongSamplingCounter(this.mOnBatteryScreenOffTimeBase);
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
    }

    public BatteryStatsImpl(Parcel p) {
        this(new SystemClocks(), p);
    }

    public BatteryStatsImpl(Clocks clocks, Parcel p) {
        this.mKernelWakelockReader = new KernelWakelockReader();
        this.mTmpWakelockStats = new KernelWakelockStats();
        this.mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
        this.mKernelUidCpuFreqTimeReader = new KernelUidCpuFreqTimeReader();
        this.mKernelMemoryBandwidthStats = new KernelMemoryBandwidthStats();
        this.mKernelMemoryStats = new LongSparseArray();
        this.mIsolatedUids = new SparseIntArray();
        this.mUidStats = new SparseArray();
        this.mPartialTimers = new ArrayList();
        this.mFullTimers = new ArrayList();
        this.mWindowTimers = new ArrayList();
        this.mDrawTimers = new ArrayList();
        this.mSensorTimers = new SparseArray();
        this.mWifiRunningTimers = new ArrayList();
        this.mFullWifiLockTimers = new ArrayList();
        this.mWifiMulticastTimers = new ArrayList();
        this.mWifiScanTimers = new ArrayList();
        this.mWifiBatchedScanTimers = new SparseArray();
        this.mAudioTurnedOnTimers = new ArrayList();
        this.mVideoTurnedOnTimers = new ArrayList();
        this.mFlashlightTurnedOnTimers = new ArrayList();
        this.mCameraTurnedOnTimers = new ArrayList();
        this.mBluetoothScanOnTimers = new ArrayList();
        this.mLastPartialTimers = new ArrayList();
        this.mOnBatteryTimeBase = new TimeBase();
        this.mOnBatteryScreenOffTimeBase = new TimeBase();
        this.mActiveEvents = new HistoryEventTracker();
        this.mHaveBatteryLevel = false;
        this.mRecordingHistory = false;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new HistoryItem();
        this.mHistoryLastLastWritten = new HistoryItem();
        this.mHistoryReadTmp = new HistoryItem();
        this.mHistoryAddTmp = new HistoryItem();
        this.mHistoryTagPool = new HashMap();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = false;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
        this.mLastHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryElapsedRealtime = 0;
        this.mTrackRunningHistoryUptime = 0;
        this.mHistoryCur = new HistoryItem();
        this.mLastHistoryStepDetails = null;
        this.mLastHistoryStepLevel = (byte) 0;
        this.mCurHistoryStepDetails = new HistoryStepDetails();
        this.mReadHistoryStepDetails = new HistoryStepDetails();
        this.mTmpHistoryStepDetails = new HistoryStepDetails();
        this.mScreenState = 0;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[17];
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
        this.mMobileRadioPowerState = 1;
        this.mWifiRadioPowerState = 1;
        this.mCharging = true;
        this.mInitStepMode = 0;
        this.mCurStepMode = 0;
        this.mModStepMode = 0;
        this.mDischargeStepTracker = new LevelStepTracker(200);
        this.mDailyDischargeStepTracker = new LevelStepTracker(400);
        this.mChargeStepTracker = new LevelStepTracker(200);
        this.mDailyChargeStepTracker = new LevelStepTracker(400);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mMinLearnedBatteryCapacity = -1;
        this.mMaxLearnedBatteryCapacity = -1;
        this.mKernelWakelockStats = new HashMap();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap();
        this.mChangedStates = 0;
        this.mChangedStates2 = 0;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = 0;
        this.mWifiScanNesting = 0;
        this.mWifiMulticastNesting = 0;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        this.mNetworkStatsPool = new SynchronizedPool(6);
        this.mWifiNetworkLock = new Object();
        this.mWifiIfaces = EmptyArray.STRING;
        this.mLastWifiNetworkStats = new NetworkStats(0, -1);
        this.mModemNetworkLock = new Object();
        this.mModemIfaces = EmptyArray.STRING;
        this.mLastModemNetworkStats = new NetworkStats(0, -1);
        this.mPendingWrite = null;
        this.mWriteLock = new ReentrantLock();
        init(clocks);
        this.mFile = null;
        this.mCheckinFile = null;
        this.mDailyFile = null;
        this.mHandler = null;
        this.mExternalSync = null;
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
        } else if (currentTime < this.mDailyStartTime - DateUtils.DAY_IN_MILLIS) {
            recordDailyStatsLocked();
        }
    }

    public void recordDailyStatsLocked() {
        DailyItem item = new DailyItem();
        item.mStartTime = this.mDailyStartTime;
        item.mEndTime = System.currentTimeMillis();
        boolean hasData = false;
        if (this.mDailyDischargeStepTracker.mNumStepDurations > 0) {
            hasData = true;
            item.mDischargeSteps = new LevelStepTracker(this.mDailyDischargeStepTracker.mNumStepDurations, this.mDailyDischargeStepTracker.mStepDurations);
        }
        if (this.mDailyChargeStepTracker.mNumStepDurations > 0) {
            hasData = true;
            item.mChargeSteps = new LevelStepTracker(this.mDailyChargeStepTracker.mNumStepDurations, this.mDailyChargeStepTracker.mStepDurations);
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
            this.mDailyItems.add(item);
            while (this.mDailyItems.size() > 10) {
                this.mDailyItems.remove(0);
            }
            final ByteArrayOutputStream memStream = new ByteArrayOutputStream();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(memStream, StandardCharsets.UTF_8.name());
                writeDailyItemsLocked(out);
                BackgroundThread.getHandler().post(new Runnable() {
                    public void run() {
                        synchronized (BatteryStatsImpl.this.mCheckinFile) {
                            FileOutputStream fileOutputStream = null;
                            try {
                                fileOutputStream = BatteryStatsImpl.this.mDailyFile.startWrite();
                                memStream.writeTo(fileOutputStream);
                                fileOutputStream.flush();
                                FileUtils.sync(fileOutputStream);
                                fileOutputStream.close();
                                BatteryStatsImpl.this.mDailyFile.finishWrite(fileOutputStream);
                            } catch (IOException e) {
                                Slog.w("BatteryStats", "Error writing battery daily items", e);
                                BatteryStatsImpl.this.mDailyFile.failWrite(fileOutputStream);
                            }
                        }
                        return;
                    }
                });
            } catch (IOException e) {
            }
        }
    }

    private void writeDailyItemsLocked(XmlSerializer out) throws IOException {
        StringBuilder sb = new StringBuilder(64);
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, "daily-items");
        for (int i = 0; i < this.mDailyItems.size(); i++) {
            DailyItem dit = (DailyItem) this.mDailyItems.get(i);
            out.startTag(null, ImsConfig.EXTRA_CHANGED_ITEM);
            out.attribute(null, BaseMmsColumns.START, Long.toString(dit.mStartTime));
            out.attribute(null, "end", Long.toString(dit.mEndTime));
            writeDailyLevelSteps(out, "dis", dit.mDischargeSteps, sb);
            writeDailyLevelSteps(out, "chg", dit.mChargeSteps, sb);
            if (dit.mPackageChanges != null) {
                for (int j = 0; j < dit.mPackageChanges.size(); j++) {
                    PackageChange pc = (PackageChange) dit.mPackageChanges.get(j);
                    if (pc.mUpdate) {
                        out.startTag(null, "upd");
                        out.attribute(null, HwSysResource.KEY_PKG, pc.mPackageName);
                        out.attribute(null, "ver", Integer.toString(pc.mVersionCode));
                        out.endTag(null, "upd");
                    } else {
                        out.startTag(null, "rem");
                        out.attribute(null, HwSysResource.KEY_PKG, pc.mPackageName);
                        out.endTag(null, "rem");
                    }
                }
            }
            out.endTag(null, ImsConfig.EXTRA_CHANGED_ITEM);
        }
        out.endTag(null, "daily-items");
        out.endDocument();
    }

    private void writeDailyLevelSteps(XmlSerializer out, String tag, LevelStepTracker steps, StringBuilder tmpBuilder) throws IOException {
        if (steps != null) {
            out.startTag(null, tag);
            out.attribute(null, "n", Integer.toString(steps.mNumStepDurations));
            for (int i = 0; i < steps.mNumStepDurations; i++) {
                out.startTag(null, "s");
                tmpBuilder.setLength(0);
                steps.encodeEntryAt(i, tmpBuilder);
                out.attribute(null, BaseMmsColumns.MMS_VERSION, tmpBuilder.toString());
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
                try {
                    stream.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    stream.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
        }
    }

    private void readDailyItemsLocked(XmlPullParser parser) {
        int type;
        do {
            try {
                type = parser.next();
                if (type == 2) {
                    break;
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
        } while (type != 1);
        if (type != 2) {
            throw new IllegalStateException("no start tag found");
        }
        int outerDepth = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(ImsConfig.EXTRA_CHANGED_ITEM)) {
                    readDailyItemTagLocked(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <daily-items>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void readDailyItemTagLocked(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        DailyItem dit = new DailyItem();
        String attr = parser.getAttributeValue(null, BaseMmsColumns.START);
        if (attr != null) {
            dit.mStartTime = Long.parseLong(attr);
        }
        attr = parser.getAttributeValue(null, "end");
        if (attr != null) {
            dit.mEndTime = Long.parseLong(attr);
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                this.mDailyItems.add(dit);
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                PackageChange pc;
                if (tagName.equals("dis")) {
                    readDailyItemTagDetailsLocked(parser, dit, false, "dis");
                } else if (tagName.equals("chg")) {
                    readDailyItemTagDetailsLocked(parser, dit, true, "chg");
                } else if (tagName.equals("upd")) {
                    if (dit.mPackageChanges == null) {
                        dit.mPackageChanges = new ArrayList();
                    }
                    pc = new PackageChange();
                    pc.mUpdate = true;
                    pc.mPackageName = parser.getAttributeValue(null, HwSysResource.KEY_PKG);
                    String verStr = parser.getAttributeValue(null, "ver");
                    pc.mVersionCode = verStr != null ? Integer.parseInt(verStr) : 0;
                    dit.mPackageChanges.add(pc);
                    XmlUtils.skipCurrentTag(parser);
                } else if (tagName.equals("rem")) {
                    if (dit.mPackageChanges == null) {
                        dit.mPackageChanges = new ArrayList();
                    }
                    pc = new PackageChange();
                    pc.mUpdate = false;
                    pc.mPackageName = parser.getAttributeValue(null, HwSysResource.KEY_PKG);
                    dit.mPackageChanges.add(pc);
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <item>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        this.mDailyItems.add(dit);
    }

    void readDailyItemTagDetailsLocked(XmlPullParser parser, DailyItem dit, boolean isCharge, String tag) throws NumberFormatException, XmlPullParserException, IOException {
        String numAttr = parser.getAttributeValue(null, "n");
        if (numAttr == null) {
            Slog.w(TAG, "Missing 'n' attribute at " + parser.getPositionDescription());
            XmlUtils.skipCurrentTag(parser);
            return;
        }
        int num = Integer.parseInt(numAttr);
        LevelStepTracker steps = new LevelStepTracker(num);
        if (isCharge) {
            dit.mChargeSteps = steps;
        } else {
            dit.mDischargeSteps = steps;
        }
        int i = 0;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                steps.mNumStepDurations = i;
            } else if (!(type == 3 || type == 4)) {
                if (!"s".equals(parser.getName())) {
                    Slog.w(TAG, "Unknown element under <" + tag + ">: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                } else if (i < num) {
                    String valueAttr = parser.getAttributeValue(null, BaseMmsColumns.MMS_VERSION);
                    if (valueAttr != null) {
                        steps.decodeEntryAt(i, valueAttr);
                        i++;
                    }
                }
            }
        }
        steps.mNumStepDurations = i;
    }

    public DailyItem getDailyItemLocked(int daysAgo) {
        int index = (this.mDailyItems.size() - 1) - daysAgo;
        return index >= 0 ? (DailyItem) this.mDailyItems.get(index) : null;
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
        HistoryItem historyItem = this.mHistory;
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

    public boolean getNextOldHistoryLocked(HistoryItem out) {
        boolean end = this.mHistoryBuffer.dataPosition() >= this.mHistoryBuffer.dataSize();
        if (!end) {
            int i;
            readHistoryDelta(this.mHistoryBuffer, this.mHistoryReadTmp);
            boolean z = this.mReadOverflow;
            if (this.mHistoryReadTmp.cmd == (byte) 6) {
                i = 1;
            } else {
                i = 0;
            }
            this.mReadOverflow = i | z;
        }
        HistoryItem cur = this.mHistoryIterator;
        if (cur == null) {
            if (!(this.mReadOverflow || (end ^ 1) == 0)) {
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
                PrintWriter pw = new FastPrintWriter(new LogWriter(5, TAG));
                pw.println("Histories differ!");
                pw.println("Old history:");
                new HistoryPrinter().printNextItem(pw, out, 0, false, true);
                pw.println("New history:");
                new HistoryPrinter().printNextItem(pw, this.mHistoryReadTmp, 0, false, true);
                pw.flush();
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
        return 262144;
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
        for (Entry<HistoryTag, Integer> ent : this.mHistoryTagPool.entrySet()) {
            HistoryTag tag = (HistoryTag) ent.getKey();
            int idx = ((Integer) ent.getValue()).intValue();
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

    public boolean getNextHistoryLocked(HistoryItem out) {
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
        if (!(out.cmd == (byte) 5 || out.cmd == (byte) 7 || lastWalltime == 0)) {
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

    void initTimes(long uptime, long realtime) {
        this.mStartClockTime = System.currentTimeMillis();
        this.mOnBatteryTimeBase.init(uptime, realtime);
        this.mOnBatteryScreenOffTimeBase.init(uptime, realtime);
        this.mRealtime = 0;
        this.mUptime = 0;
        this.mRealtimeStart = realtime;
        this.mUptimeStart = uptime;
    }

    void initDischarge() {
        this.mLowDischargeAmountSinceCharge = 0;
        this.mHighDischargeAmountSinceCharge = 0;
        this.mDischargeAmountScreenOn = 0;
        this.mDischargeAmountScreenOnSinceCharge = 0;
        this.mDischargeAmountScreenOff = 0;
        this.mDischargeAmountScreenOffSinceCharge = 0;
        this.mDischargeStepTracker.init();
        this.mChargeStepTracker.init();
        this.mDischargeScreenOffCounter.reset(false);
        this.mDischargeCounter.reset(false);
    }

    public void resetAllStatsCmdLocked() {
        resetAllStatsLocked();
        long mSecUptime = this.mClocks.uptimeMillis();
        long uptime = mSecUptime * 1000;
        long mSecRealtime = this.mClocks.elapsedRealtime();
        long realtime = mSecRealtime * 1000;
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
            if (this.mScreenState == 2) {
                this.mDischargeScreenOnUnplugLevel = this.mHistoryCur.batteryLevel;
                this.mDischargeScreenOffUnplugLevel = 0;
            } else {
                this.mDischargeScreenOnUnplugLevel = 0;
                this.mDischargeScreenOffUnplugLevel = this.mHistoryCur.batteryLevel;
            }
            this.mDischargeAmountScreenOn = 0;
            this.mDischargeAmountScreenOff = 0;
        }
        initActiveHistoryEventsLocked(mSecRealtime, mSecUptime);
    }

    private void resetAllStatsLocked() {
        int i;
        long uptimeMillis = this.mClocks.uptimeMillis();
        long elapsedRealtimeMillis = this.mClocks.elapsedRealtime();
        this.mStartCount = 0;
        initTimes(1000 * uptimeMillis, 1000 * elapsedRealtimeMillis);
        this.mScreenOnTimer.reset(false);
        for (i = 0; i < 5; i++) {
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
        for (i = 0; i < 5; i++) {
            this.mPhoneSignalStrengthsTimer[i].reset(false);
        }
        this.mPhoneSignalScanningTimer.reset(false);
        for (i = 0; i < 17; i++) {
            this.mPhoneDataConnectionsTimer[i].reset(false);
        }
        for (i = 0; i < 10; i++) {
            this.mNetworkByteActivityCounters[i].reset(false);
            this.mNetworkPacketActivityCounters[i].reset(false);
        }
        this.mMobileRadioActiveTimer.reset(false);
        this.mMobileRadioActivePerAppTimer.reset(false);
        this.mMobileRadioActiveAdjustedTime.reset(false);
        this.mMobileRadioActiveUnknownTime.reset(false);
        this.mMobileRadioActiveUnknownCount.reset(false);
        this.mWifiOnTimer.reset(false);
        this.mGlobalWifiRunningTimer.reset(false);
        for (i = 0; i < 8; i++) {
            this.mWifiStateTimer[i].reset(false);
        }
        for (i = 0; i < 13; i++) {
            this.mWifiSupplStateTimer[i].reset(false);
        }
        for (i = 0; i < 5; i++) {
            this.mWifiSignalStrengthsTimer[i].reset(false);
        }
        this.mWifiActivity.reset(false);
        this.mBluetoothActivity.reset(false);
        this.mModemActivity.reset(false);
        this.mUnpluggedNumConnectivityChange = 0;
        this.mLoadedNumConnectivityChange = 0;
        this.mNumConnectivityChange = 0;
        i = 0;
        while (i < this.mUidStats.size()) {
            if (((Uid) this.mUidStats.valueAt(i)).reset()) {
                this.mUidStats.remove(this.mUidStats.keyAt(i));
                i--;
            }
            i++;
        }
        if (this.mKernelWakelockStats.size() > 0) {
            for (SamplingTimer timer : this.mKernelWakelockStats.values()) {
                this.mOnBatteryScreenOffTimeBase.remove(timer);
            }
            this.mKernelWakelockStats.clear();
        }
        if (this.mKernelMemoryStats.size() > 0) {
            for (i = 0; i < this.mKernelMemoryStats.size(); i++) {
                this.mOnBatteryTimeBase.remove((TimeBaseObs) this.mKernelMemoryStats.valueAt(i));
            }
            this.mKernelMemoryStats.clear();
        }
        if (this.mWakeupReasonStats.size() > 0) {
            for (SamplingTimer timer2 : this.mWakeupReasonStats.values()) {
                this.mOnBatteryTimeBase.remove(timer2);
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
        initDischarge();
        clearHistoryLocked();
        HwLog.dubaie("DUBAI_TAG_RESET_BATTERY_STAT", LogException.NO_VALUE);
    }

    private void initActiveHistoryEventsLocked(long elapsedRealtimeMs, long uptimeMs) {
        int i = 0;
        while (i < 22) {
            if (this.mRecordAllHistory || i != 1) {
                HashMap<String, SparseIntArray> active = this.mActiveEvents.getStateForEvent(i);
                if (active != null) {
                    for (Entry<String, SparseIntArray> ent : active.entrySet()) {
                        SparseIntArray uids = (SparseIntArray) ent.getValue();
                        for (int j = 0; j < uids.size(); j++) {
                            addHistoryEventLocked(elapsedRealtimeMs, uptimeMs, i, (String) ent.getKey(), uids.keyAt(j));
                        }
                    }
                }
            }
            i++;
        }
    }

    void updateDischargeScreenLevelsLocked(boolean oldScreenOn, boolean newScreenOn) {
        int diff;
        if (oldScreenOn) {
            diff = this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            if (diff > 0) {
                this.mDischargeAmountScreenOn += diff;
                this.mDischargeAmountScreenOnSinceCharge += diff;
            }
        } else {
            diff = this.mDischargeScreenOffUnplugLevel - this.mDischargeCurrentLevel;
            if (diff > 0) {
                this.mDischargeAmountScreenOff += diff;
                this.mDischargeAmountScreenOffSinceCharge += diff;
            }
        }
        if (newScreenOn) {
            this.mDischargeScreenOnUnplugLevel = this.mDischargeCurrentLevel;
            this.mDischargeScreenOffUnplugLevel = 0;
            return;
        }
        this.mDischargeScreenOnUnplugLevel = 0;
        this.mDischargeScreenOffUnplugLevel = this.mDischargeCurrentLevel;
    }

    public void pullPendingStateUpdatesLocked() {
        if (this.mOnBatteryInternal) {
            boolean screenOn = this.mScreenState == 2;
            updateDischargeScreenLevelsLocked(screenOn, screenOn);
        }
    }

    private NetworkStats readNetworkStatsLocked(String[] ifaces) {
        try {
            if (!ArrayUtils.isEmpty((Object[]) ifaces)) {
                return this.mNetworkStatsFactory.readNetworkStatsDetail(-1, ifaces, 0, (NetworkStats) this.mNetworkStatsPool.acquire());
            }
        } catch (IOException e) {
            Slog.e(TAG, "failed to read network stats for ifaces: " + Arrays.toString(ifaces));
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:14:0x004b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:75:0x02cb, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateWifiState(WifiActivityEnergyInfo info) {
        NetworkStats delta = null;
        synchronized (this.mWifiNetworkLock) {
            NetworkStats latestStats = readNetworkStatsLocked(this.mWifiIfaces);
            if (latestStats != null) {
                delta = NetworkStats.subtract(latestStats, this.mLastWifiNetworkStats, null, null, (NetworkStats) this.mNetworkStatsPool.acquire());
                this.mNetworkStatsPool.release(this.mLastWifiNetworkStats);
                this.mLastWifiNetworkStats = latestStats;
            }
        }
        synchronized (this) {
            if (this.mOnBatteryInternal) {
                int i;
                long elapsedRealtimeMs = this.mClocks.elapsedRealtime();
                SparseLongArray rxPackets = new SparseLongArray();
                SparseLongArray txPackets = new SparseLongArray();
                long totalTxPackets = 0;
                long totalRxPackets = 0;
                if (delta != null) {
                    NetworkStats.Entry entry = new NetworkStats.Entry();
                    int size = delta.size();
                    for (i = 0; i < size; i++) {
                        entry = delta.getValues(i, entry);
                        if (entry.rxBytes != 0 || entry.txBytes != 0) {
                            Uid u = getUidStatsLocked(mapUid(entry.uid));
                            if (entry.rxBytes != 0) {
                                u.noteNetworkActivityLocked(2, entry.rxBytes, entry.rxPackets);
                                if (entry.set == 0) {
                                    u.noteNetworkActivityLocked(8, entry.rxBytes, entry.rxPackets);
                                }
                                this.mNetworkByteActivityCounters[2].addCountLocked(entry.rxBytes);
                                this.mNetworkPacketActivityCounters[2].addCountLocked(entry.rxPackets);
                                rxPackets.put(u.getUid(), entry.rxPackets);
                                totalRxPackets += entry.rxPackets;
                            }
                            if (entry.txBytes != 0) {
                                u.noteNetworkActivityLocked(3, entry.txBytes, entry.txPackets);
                                if (entry.set == 0) {
                                    u.noteNetworkActivityLocked(9, entry.txBytes, entry.txPackets);
                                }
                                this.mNetworkByteActivityCounters[3].addCountLocked(entry.txBytes);
                                this.mNetworkPacketActivityCounters[3].addCountLocked(entry.txPackets);
                                txPackets.put(u.getUid(), entry.txPackets);
                                totalTxPackets += entry.txPackets;
                            }
                        }
                    }
                    this.mNetworkStatsPool.release(delta);
                }
                if (info != null) {
                    Uid uid;
                    this.mHasWifiReporting = true;
                    long txTimeMs = info.getControllerTxTimeMillis();
                    long rxTimeMs = info.getControllerRxTimeMillis();
                    long idleTimeMs = info.getControllerIdleTimeMillis();
                    long totalTimeMs = (txTimeMs + rxTimeMs) + idleTimeMs;
                    long leftOverRxTimeMs = rxTimeMs;
                    long leftOverTxTimeMs = txTimeMs;
                    long totalWifiLockTimeMs = 0;
                    long totalScanTimeMs = 0;
                    int uidStatsSize = this.mUidStats.size();
                    for (i = 0; i < uidStatsSize; i++) {
                        uid = (Uid) this.mUidStats.valueAt(i);
                        totalScanTimeMs += uid.mWifiScanTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                        totalWifiLockTimeMs += uid.mFullWifiLockTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                    }
                    for (i = 0; i < uidStatsSize; i++) {
                        uid = (Uid) this.mUidStats.valueAt(i);
                        long scanTimeSinceMarkMs = uid.mWifiScanTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                        if (scanTimeSinceMarkMs > 0) {
                            uid.mWifiScanTimer.setMark(elapsedRealtimeMs);
                            long scanRxTimeSinceMarkMs = scanTimeSinceMarkMs;
                            long scanTxTimeSinceMarkMs = scanTimeSinceMarkMs;
                            if (totalScanTimeMs > rxTimeMs) {
                                scanRxTimeSinceMarkMs = (rxTimeMs * scanTimeSinceMarkMs) / totalScanTimeMs;
                            }
                            if (totalScanTimeMs > txTimeMs) {
                                scanTxTimeSinceMarkMs = (txTimeMs * scanTimeSinceMarkMs) / totalScanTimeMs;
                            }
                            ControllerActivityCounterImpl activityCounter = uid.getOrCreateWifiControllerActivityLocked();
                            activityCounter.getRxTimeCounter().addCountLocked(scanRxTimeSinceMarkMs);
                            activityCounter.getTxTimeCounters()[0].addCountLocked(scanTxTimeSinceMarkMs);
                            leftOverRxTimeMs -= scanRxTimeSinceMarkMs;
                            leftOverTxTimeMs -= scanTxTimeSinceMarkMs;
                        }
                        long wifiLockTimeSinceMarkMs = uid.mFullWifiLockTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                        if (wifiLockTimeSinceMarkMs > 0) {
                            uid.mFullWifiLockTimer.setMark(elapsedRealtimeMs);
                            uid.getOrCreateWifiControllerActivityLocked().getIdleTimeCounter().addCountLocked((wifiLockTimeSinceMarkMs * idleTimeMs) / totalWifiLockTimeMs);
                        }
                    }
                    for (i = 0; i < txPackets.size(); i++) {
                        long myTxTimeMs = (txPackets.valueAt(i) * leftOverTxTimeMs) / totalTxPackets;
                        getUidStatsLocked(txPackets.keyAt(i)).getOrCreateWifiControllerActivityLocked().getTxTimeCounters()[0].addCountLocked(myTxTimeMs);
                    }
                    for (i = 0; i < rxPackets.size(); i++) {
                        long myRxTimeMs = (rxPackets.valueAt(i) * leftOverRxTimeMs) / totalRxPackets;
                        getUidStatsLocked(rxPackets.keyAt(i)).getOrCreateWifiControllerActivityLocked().getRxTimeCounter().addCountLocked(myRxTimeMs);
                    }
                    this.mWifiActivity.getRxTimeCounter().addCountLocked(info.getControllerRxTimeMillis());
                    this.mWifiActivity.getTxTimeCounters()[0].addCountLocked(info.getControllerTxTimeMillis());
                    this.mWifiActivity.getIdleTimeCounter().addCountLocked(info.getControllerIdleTimeMillis());
                    double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
                    if (opVolt != 0.0d) {
                        this.mWifiActivity.getPowerCounter().addCountLocked((long) (((double) info.getControllerEnergyUsed()) / opVolt));
                    }
                }
            } else if (delta != null) {
                this.mNetworkStatsPool.release(delta);
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x004b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:75:0x026e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateMobileRadioState(ModemActivityInfo activityInfo) {
        NetworkStats delta = null;
        synchronized (this.mModemNetworkLock) {
            NetworkStats latestStats = readNetworkStatsLocked(this.mModemIfaces);
            if (latestStats != null) {
                delta = NetworkStats.subtract(latestStats, this.mLastModemNetworkStats, null, null, (NetworkStats) this.mNetworkStatsPool.acquire());
                this.mNetworkStatsPool.release(this.mLastModemNetworkStats);
                this.mLastModemNetworkStats = latestStats;
            }
        }
        synchronized (this) {
            if (this.mOnBatteryInternal) {
                int lvl;
                long elapsedRealtimeMs = SystemClock.elapsedRealtime();
                long radioTime = this.mMobileRadioActivePerAppTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs);
                this.mMobileRadioActivePerAppTimer.setMark(elapsedRealtimeMs);
                long totalRxPackets = 0;
                long totalTxPackets = 0;
                if (delta != null) {
                    int i;
                    Uid u;
                    NetworkStats.Entry entry = new NetworkStats.Entry();
                    int size = delta.size();
                    for (i = 0; i < size; i++) {
                        entry = delta.getValues(i, entry);
                        if (entry.rxPackets != 0 || entry.txPackets != 0) {
                            totalRxPackets += entry.rxPackets;
                            totalTxPackets += entry.txPackets;
                            u = getUidStatsLocked(mapUid(entry.uid));
                            u.noteNetworkActivityLocked(0, entry.rxBytes, entry.rxPackets);
                            u.noteNetworkActivityLocked(1, entry.txBytes, entry.txPackets);
                            if (entry.set == 0) {
                                u.noteNetworkActivityLocked(6, entry.rxBytes, entry.rxPackets);
                                u.noteNetworkActivityLocked(7, entry.txBytes, entry.txPackets);
                            }
                            this.mNetworkByteActivityCounters[0].addCountLocked(entry.rxBytes);
                            this.mNetworkByteActivityCounters[1].addCountLocked(entry.txBytes);
                            this.mNetworkPacketActivityCounters[0].addCountLocked(entry.rxPackets);
                            this.mNetworkPacketActivityCounters[1].addCountLocked(entry.txPackets);
                        }
                    }
                    long totalPackets = totalRxPackets + totalTxPackets;
                    if (totalPackets > 0) {
                        for (i = 0; i < size; i++) {
                            entry = delta.getValues(i, entry);
                            if (entry.rxPackets != 0 || entry.txPackets != 0) {
                                u = getUidStatsLocked(mapUid(entry.uid));
                                long appPackets = entry.rxPackets + entry.txPackets;
                                long appRadioTime = (radioTime * appPackets) / totalPackets;
                                u.noteMobileRadioActiveTimeLocked(appRadioTime);
                                radioTime -= appRadioTime;
                                totalPackets -= appPackets;
                                if (activityInfo != null) {
                                    ControllerActivityCounterImpl activityCounter = u.getOrCreateModemControllerActivityLocked();
                                    if (totalRxPackets > 0 && entry.rxPackets > 0) {
                                        activityCounter.getRxTimeCounter().addCountLocked((entry.rxPackets * ((long) activityInfo.getRxTimeMillis())) / totalRxPackets);
                                    }
                                    if (totalTxPackets > 0 && entry.txPackets > 0) {
                                        for (lvl = 0; lvl < 5; lvl++) {
                                            activityCounter.getTxTimeCounters()[lvl].addCountLocked((entry.txPackets * ((long) activityInfo.getTxTimeMillis()[lvl])) / totalTxPackets);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (radioTime > 0) {
                        this.mMobileRadioActiveUnknownTime.addCountLocked(radioTime);
                        this.mMobileRadioActiveUnknownCount.addCountLocked(1);
                    }
                    this.mNetworkStatsPool.release(delta);
                }
                if (activityInfo != null) {
                    this.mHasModemReporting = true;
                    this.mModemActivity.getIdleTimeCounter().addCountLocked((long) activityInfo.getIdleTimeMillis());
                    this.mModemActivity.getRxTimeCounter().addCountLocked((long) activityInfo.getRxTimeMillis());
                    for (lvl = 0; lvl < 5; lvl++) {
                        this.mModemActivity.getTxTimeCounters()[lvl].addCountLocked((long) activityInfo.getTxTimeMillis()[lvl]);
                    }
                    double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
                    if (opVolt != 0.0d) {
                        this.mModemActivity.getPowerCounter().addCountLocked((long) (((double) activityInfo.getEnergyUsed()) / opVolt));
                    }
                }
            } else if (delta != null) {
                this.mNetworkStatsPool.release(delta);
            }
        }
    }

    public void updateBluetoothStateLocked(BluetoothActivityEnergyInfo info) {
        if (info != null && (this.mOnBatteryInternal ^ 1) == 0) {
            int i;
            Uid u;
            ControllerActivityCounterImpl counter;
            UidTraffic traffic;
            this.mHasBluetoothReporting = true;
            long elapsedRealtimeMs = this.mClocks.elapsedRealtime();
            long rxTimeMs = info.getControllerRxTimeMillis();
            long txTimeMs = info.getControllerTxTimeMillis();
            long totalScanTimeMs = 0;
            int uidCount = this.mUidStats.size();
            for (i = 0; i < uidCount; i++) {
                u = (Uid) this.mUidStats.valueAt(i);
                if (u.mBluetoothScanTimer != null) {
                    totalScanTimeMs += u.mBluetoothScanTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                }
            }
            boolean normalizeScanRxTime = totalScanTimeMs > rxTimeMs;
            boolean normalizeScanTxTime = totalScanTimeMs > txTimeMs;
            long leftOverRxTimeMs = rxTimeMs;
            long leftOverTxTimeMs = txTimeMs;
            for (i = 0; i < uidCount; i++) {
                u = (Uid) this.mUidStats.valueAt(i);
                if (u.mBluetoothScanTimer != null) {
                    long scanTimeSinceMarkMs = u.mBluetoothScanTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                    if (scanTimeSinceMarkMs > 0) {
                        u.mBluetoothScanTimer.setMark(elapsedRealtimeMs);
                        long scanTimeRxSinceMarkMs = scanTimeSinceMarkMs;
                        long scanTimeTxSinceMarkMs = scanTimeSinceMarkMs;
                        if (normalizeScanRxTime) {
                            scanTimeRxSinceMarkMs = (rxTimeMs * scanTimeSinceMarkMs) / totalScanTimeMs;
                        }
                        if (normalizeScanTxTime) {
                            scanTimeTxSinceMarkMs = (txTimeMs * scanTimeSinceMarkMs) / totalScanTimeMs;
                        }
                        counter = u.getOrCreateBluetoothControllerActivityLocked();
                        counter.getRxTimeCounter().addCountLocked(scanTimeRxSinceMarkMs);
                        counter.getTxTimeCounters()[0].addCountLocked(scanTimeTxSinceMarkMs);
                        leftOverRxTimeMs -= scanTimeRxSinceMarkMs;
                        leftOverTxTimeMs -= scanTimeTxSinceMarkMs;
                    }
                }
            }
            long totalTxBytes = 0;
            long totalRxBytes = 0;
            UidTraffic[] uidTraffic = info.getUidTraffic();
            int numUids = uidTraffic != null ? uidTraffic.length : 0;
            for (i = 0; i < numUids; i++) {
                traffic = uidTraffic[i];
                this.mNetworkByteActivityCounters[4].addCountLocked(traffic.getRxBytes());
                this.mNetworkByteActivityCounters[5].addCountLocked(traffic.getTxBytes());
                u = getUidStatsLocked(mapUid(traffic.getUid()));
                u.noteNetworkActivityLocked(4, traffic.getRxBytes(), 0);
                u.noteNetworkActivityLocked(5, traffic.getTxBytes(), 0);
                totalTxBytes += traffic.getTxBytes();
                totalRxBytes += traffic.getRxBytes();
            }
            if (!((totalTxBytes == 0 && totalRxBytes == 0) || (leftOverRxTimeMs == 0 && leftOverTxTimeMs == 0))) {
                for (i = 0; i < numUids; i++) {
                    traffic = uidTraffic[i];
                    counter = getUidStatsLocked(mapUid(traffic.getUid())).getOrCreateBluetoothControllerActivityLocked();
                    if (totalRxBytes > 0 && traffic.getRxBytes() > 0) {
                        long timeRxMs = (traffic.getRxBytes() * leftOverRxTimeMs) / totalRxBytes;
                        counter.getRxTimeCounter().addCountLocked(timeRxMs);
                        leftOverRxTimeMs -= timeRxMs;
                    }
                    if (totalTxBytes > 0 && traffic.getTxBytes() > 0) {
                        long timeTxMs = (traffic.getTxBytes() * leftOverTxTimeMs) / totalTxBytes;
                        counter.getTxTimeCounters()[0].addCountLocked(timeTxMs);
                        leftOverTxTimeMs -= timeTxMs;
                    }
                }
            }
            this.mBluetoothActivity.getRxTimeCounter().addCountLocked(info.getControllerRxTimeMillis());
            this.mBluetoothActivity.getTxTimeCounters()[0].addCountLocked(info.getControllerTxTimeMillis());
            this.mBluetoothActivity.getIdleTimeCounter().addCountLocked(info.getControllerIdleTimeMillis());
            double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
            if (opVolt != 0.0d) {
                this.mBluetoothActivity.getPowerCounter().addCountLocked((long) (((double) info.getControllerEnergyUsed()) / opVolt));
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
        for (Entry<String, KernelWakelockStats.Entry> ent : wakelockStats.entrySet()) {
            String name = (String) ent.getKey();
            KernelWakelockStats.Entry kws = (KernelWakelockStats.Entry) ent.getValue();
            SamplingTimer kwlt = (SamplingTimer) this.mKernelWakelockStats.get(name);
            if (kwlt == null) {
                kwlt = new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase);
                this.mKernelWakelockStats.put(name, kwlt);
            }
            kwlt.update(kws.mTotalTime, kws.mCount);
            kwlt.setUpdateVersion(kws.mVersion);
        }
        int numWakelocksSetStale = 0;
        for (Entry<String, SamplingTimer> ent2 : this.mKernelWakelockStats.entrySet()) {
            SamplingTimer st = (SamplingTimer) ent2.getValue();
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
        this.mKernelMemoryBandwidthStats.updateStats();
        LongSparseLongArray bandwidthEntries = this.mKernelMemoryBandwidthStats.getBandwidthEntries();
        int bandwidthEntryCount = bandwidthEntries.size();
        for (int i = 0; i < bandwidthEntryCount; i++) {
            SamplingTimer timer;
            int index = this.mKernelMemoryStats.indexOfKey(bandwidthEntries.keyAt(i));
            if (index >= 0) {
                timer = (SamplingTimer) this.mKernelMemoryStats.valueAt(index);
            } else {
                timer = new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase);
                this.mKernelMemoryStats.put(bandwidthEntries.keyAt(i), timer);
            }
            timer.update(bandwidthEntries.valueAt(i), 1);
        }
    }

    public void updateCpuTimeLocked(boolean updateCpuFreqData) {
        if (this.mPowerProfile != null) {
            int i;
            StopwatchTimer timer;
            AnonymousClass3 anonymousClass3;
            final long[][] clusterSpeeds = new long[this.mKernelCpuSpeedReaders.length][];
            for (int cluster = 0; cluster < this.mKernelCpuSpeedReaders.length; cluster++) {
                clusterSpeeds[cluster] = this.mKernelCpuSpeedReaders[cluster].readDelta();
            }
            int numWakelocks = 0;
            int numPartialTimers = this.mPartialTimers.size();
            if (this.mOnBatteryScreenOffTimeBase.isRunning()) {
                for (i = 0; i < numPartialTimers; i++) {
                    timer = (StopwatchTimer) this.mPartialTimers.get(i);
                    if (!(!timer.mInList || timer.mUid == null || timer.mUid.mUid == 1000)) {
                        numWakelocks++;
                    }
                }
            }
            final int numWakelocksF = numWakelocks;
            this.mTempTotalCpuUserTimeUs = 0;
            this.mTempTotalCpuSystemTimeUs = 0;
            long startTimeMs = this.mClocks.uptimeMillis();
            KernelUidCpuTimeReader kernelUidCpuTimeReader = this.mKernelUidCpuTimeReader;
            if (this.mOnBatteryInternal) {
                AnonymousClass3 anonymousClass32 = new Callback() {
                    public void onUidCpuTime(int uid, long userTimeUs, long systemTimeUs) {
                        uid = BatteryStatsImpl.this.mapUid(uid);
                        if (Process.isIsolated(uid)) {
                            BatteryStatsImpl.this.mKernelUidCpuTimeReader.removeUid(uid);
                            Slog.d(BatteryStatsImpl.TAG, "Got readings for an isolated uid with no mapping to owning uid: " + uid);
                            return;
                        }
                        Uid u = BatteryStatsImpl.this.getUidStatsLocked(uid);
                        BatteryStatsImpl batteryStatsImpl = BatteryStatsImpl.this;
                        batteryStatsImpl.mTempTotalCpuUserTimeUs += userTimeUs;
                        batteryStatsImpl = BatteryStatsImpl.this;
                        batteryStatsImpl.mTempTotalCpuSystemTimeUs += systemTimeUs;
                        if (numWakelocksF > 0) {
                            userTimeUs = (50 * userTimeUs) / 100;
                            systemTimeUs = (50 * systemTimeUs) / 100;
                        }
                        u.mUserCpuTime.addCountLocked(userTimeUs);
                        u.mSystemCpuTime.addCountLocked(systemTimeUs);
                        int numClusters = BatteryStatsImpl.this.mPowerProfile.getNumCpuClusters();
                        if (u.mCpuClusterSpeed == null || u.mCpuClusterSpeed.length != numClusters) {
                            u.mCpuClusterSpeed = new LongSamplingCounter[numClusters][];
                        }
                        int cluster = 0;
                        while (cluster < clusterSpeeds.length) {
                            int speedsInCluster = BatteryStatsImpl.this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
                            if (u.mCpuClusterSpeed[cluster] == null || speedsInCluster != u.mCpuClusterSpeed[cluster].length) {
                                u.mCpuClusterSpeed[cluster] = new LongSamplingCounter[speedsInCluster];
                            }
                            LongSamplingCounter[] cpuSpeeds = u.mCpuClusterSpeed[cluster];
                            for (int speed = 0; speed < clusterSpeeds[cluster].length; speed++) {
                                if (cpuSpeeds[speed] == null) {
                                    cpuSpeeds[speed] = new LongSamplingCounter(BatteryStatsImpl.this.mOnBatteryTimeBase);
                                }
                                cpuSpeeds[speed].addCountLocked(clusterSpeeds[cluster][speed]);
                            }
                            cluster++;
                        }
                    }
                };
            } else {
                anonymousClass3 = null;
            }
            kernelUidCpuTimeReader.readDelta(anonymousClass3);
            if (updateCpuFreqData) {
                readKernelUidCpuFreqTimesLocked();
            }
            long elapse = this.mClocks.uptimeMillis() - startTimeMs;
            if (elapse >= 100) {
                Slog.d(TAG, "Reading cpu stats took " + elapse + " ms");
            }
            if (this.mOnBatteryInternal && numWakelocks > 0) {
                this.mTempTotalCpuUserTimeUs = (this.mTempTotalCpuUserTimeUs * 50) / 100;
                this.mTempTotalCpuSystemTimeUs = (this.mTempTotalCpuSystemTimeUs * 50) / 100;
                for (i = 0; i < numPartialTimers; i++) {
                    timer = (StopwatchTimer) this.mPartialTimers.get(i);
                    if (!(!timer.mInList || timer.mUid == null || timer.mUid.mUid == 1000)) {
                        int userTimeUs = (int) (this.mTempTotalCpuUserTimeUs / ((long) numWakelocks));
                        int systemTimeUs = (int) (this.mTempTotalCpuSystemTimeUs / ((long) numWakelocks));
                        timer.mUid.mUserCpuTime.addCountLocked((long) userTimeUs);
                        timer.mUid.mSystemCpuTime.addCountLocked((long) systemTimeUs);
                        timer.mUid.getProcessStatsLocked("*wakelock*").addCpuTimeLocked(userTimeUs / 1000, systemTimeUs / 1000);
                        this.mTempTotalCpuUserTimeUs -= (long) userTimeUs;
                        this.mTempTotalCpuSystemTimeUs -= (long) systemTimeUs;
                        numWakelocks--;
                    }
                }
                if (this.mTempTotalCpuUserTimeUs > 0 || this.mTempTotalCpuSystemTimeUs > 0) {
                    Uid u = getUidStatsLocked(1000);
                    u.mUserCpuTime.addCountLocked(this.mTempTotalCpuUserTimeUs);
                    u.mSystemCpuTime.addCountLocked(this.mTempTotalCpuSystemTimeUs);
                    u.getProcessStatsLocked("*lost*").addCpuTimeLocked(((int) this.mTempTotalCpuUserTimeUs) / 1000, ((int) this.mTempTotalCpuSystemTimeUs) / 1000);
                }
            }
            if (ArrayUtils.referenceEquals(this.mPartialTimers, this.mLastPartialTimers)) {
                for (i = 0; i < numPartialTimers; i++) {
                    ((StopwatchTimer) this.mPartialTimers.get(i)).mInList = true;
                }
            } else {
                int numLastPartialTimers = this.mLastPartialTimers.size();
                for (i = 0; i < numLastPartialTimers; i++) {
                    ((StopwatchTimer) this.mLastPartialTimers.get(i)).mInList = false;
                }
                this.mLastPartialTimers.clear();
                for (i = 0; i < numPartialTimers; i++) {
                    timer = (StopwatchTimer) this.mPartialTimers.get(i);
                    timer.mInList = true;
                    this.mLastPartialTimers.add(timer);
                }
            }
        }
    }

    void readKernelUidCpuFreqTimesLocked() {
        KernelUidCpuFreqTimeReader.Callback anonymousClass4;
        KernelUidCpuFreqTimeReader kernelUidCpuFreqTimeReader = this.mKernelUidCpuFreqTimeReader;
        if (this.mOnBatteryInternal) {
            anonymousClass4 = new KernelUidCpuFreqTimeReader.Callback() {
                public void onCpuFreqs(long[] cpuFreqs) {
                    BatteryStatsImpl.this.mCpuFreqs = cpuFreqs;
                }

                public void onUidCpuFreqTime(int uid, long[] cpuFreqTimeMs) {
                    uid = BatteryStatsImpl.this.mapUid(uid);
                    if (Process.isIsolated(uid)) {
                        BatteryStatsImpl.this.mKernelUidCpuFreqTimeReader.removeUid(uid);
                        Slog.d(BatteryStatsImpl.TAG, "Got freq readings for an isolated uid with no mapping to owning uid: " + uid);
                        return;
                    }
                    Uid u = BatteryStatsImpl.this.getUidStatsLocked(uid);
                    if (u.mCpuFreqTimeMs == null) {
                        u.mCpuFreqTimeMs = new LongSamplingCounterArray(BatteryStatsImpl.this.mOnBatteryTimeBase);
                    }
                    u.mCpuFreqTimeMs.addCountLocked(cpuFreqTimeMs);
                    if (u.mScreenOffCpuFreqTimeMs == null) {
                        u.mScreenOffCpuFreqTimeMs = new LongSamplingCounterArray(BatteryStatsImpl.this.mOnBatteryScreenOffTimeBase);
                    }
                    u.mScreenOffCpuFreqTimeMs.addCountLocked(cpuFreqTimeMs);
                }
            };
        } else {
            anonymousClass4 = null;
        }
        kernelUidCpuFreqTimeReader.readDelta(anonymousClass4);
    }

    boolean setChargingLocked(boolean charging) {
        if (this.mCharging == charging) {
            return false;
        }
        this.mCharging = charging;
        HistoryItem historyItem;
        if (charging) {
            historyItem = this.mHistoryCur;
            historyItem.states2 |= 16777216;
        } else {
            historyItem = this.mHistoryCur;
            historyItem.states2 &= -16777217;
        }
        this.mHandler.sendEmptyMessage(3);
        return true;
    }

    void setOnBatteryLocked(long mSecRealtime, long mSecUptime, boolean onBattery, int oldStatus, int level, int chargeUAh) {
        boolean doWrite = false;
        Message m = this.mHandler.obtainMessage(2);
        m.arg1 = onBattery ? 1 : 0;
        this.mHandler.sendMessage(m);
        long uptime = mSecUptime * 1000;
        long realtime = mSecRealtime * 1000;
        boolean screenOn = this.mScreenState == 2;
        HistoryItem historyItem;
        if (onBattery) {
            boolean reset = false;
            if (!this.mNoAutoReset && (oldStatus == 5 || level >= 90 || ((this.mDischargeCurrentLevel < 20 && level >= 80) || (getHighDischargeAmountSinceCharge() >= 200 && this.mHistoryBuffer.dataSize() >= 262144)))) {
                Slog.i(TAG, "Resetting battery stats: level=" + level + " status=" + oldStatus + " dischargeLevel=" + this.mDischargeCurrentLevel + " lowAmount=" + getLowDischargeAmountSinceCharge() + " highAmount=" + getHighDischargeAmountSinceCharge());
                if (getLowDischargeAmountSinceCharge() >= 20) {
                    Parcel parcel = Parcel.obtain();
                    writeSummaryToParcel(parcel, true);
                    final Parcel parcel2 = parcel;
                    BackgroundThread.getHandler().post(new Runnable() {
                        public void run() {
                            synchronized (BatteryStatsImpl.this.mCheckinFile) {
                                FileOutputStream fileOutputStream = null;
                                try {
                                    fileOutputStream = BatteryStatsImpl.this.mCheckinFile.startWrite();
                                    fileOutputStream.write(parcel2.marshall());
                                    fileOutputStream.flush();
                                    FileUtils.sync(fileOutputStream);
                                    fileOutputStream.close();
                                    BatteryStatsImpl.this.mCheckinFile.finishWrite(fileOutputStream);
                                    parcel2.recycle();
                                } catch (IOException e) {
                                    Slog.w("BatteryStats", "Error writing checkin battery statistics", e);
                                    BatteryStatsImpl.this.mCheckinFile.failWrite(fileOutputStream);
                                    parcel2.recycle();
                                } catch (Throwable th) {
                                    parcel2.recycle();
                                }
                            }
                            return;
                        }
                    });
                }
                doWrite = true;
                resetAllStatsLocked();
                if (chargeUAh > 0 && level > 0) {
                    this.mEstimatedBatteryCapacity = (int) (((double) (chargeUAh / 1000)) / (((double) level) / 100.0d));
                }
                this.mDischargeStartLevel = level;
                reset = true;
                LogPower.push(190);
                this.mDischargeStepTracker.init();
            }
            if (this.mCharging) {
                setChargingLocked(false);
            }
            this.mLastChargingStateLevel = level;
            this.mOnBatteryInternal = true;
            this.mOnBattery = true;
            this.mLastDischargeStepLevel = level;
            this.mMinDischargeStepLevel = level;
            this.mDischargeStepTracker.clearTime();
            this.mDailyDischargeStepTracker.clearTime();
            this.mInitStepMode = this.mCurStepMode;
            this.mModStepMode = 0;
            pullPendingStateUpdatesLocked();
            this.mHistoryCur.batteryLevel = (byte) level;
            historyItem = this.mHistoryCur;
            historyItem.states &= -524289;
            if (reset) {
                this.mRecordingHistory = true;
                startRecordingHistory(mSecRealtime, mSecUptime, reset);
            }
            addHistoryRecordLocked(mSecRealtime, mSecUptime);
            this.mDischargeUnplugLevel = level;
            this.mDischargeCurrentLevel = level;
            if (screenOn) {
                this.mDischargeScreenOnUnplugLevel = level;
                this.mDischargeScreenOffUnplugLevel = 0;
            } else {
                this.mDischargeScreenOnUnplugLevel = 0;
                this.mDischargeScreenOffUnplugLevel = level;
            }
            this.mDischargeAmountScreenOn = 0;
            this.mDischargeAmountScreenOff = 0;
            updateTimeBasesLocked(true, screenOn ^ 1, uptime, realtime);
        } else {
            this.mLastChargingStateLevel = level;
            this.mOnBatteryInternal = false;
            this.mOnBattery = false;
            pullPendingStateUpdatesLocked();
            this.mHistoryCur.batteryLevel = (byte) level;
            historyItem = this.mHistoryCur;
            historyItem.states |= 524288;
            addHistoryRecordLocked(mSecRealtime, mSecUptime);
            this.mDischargePlugLevel = level;
            this.mDischargeCurrentLevel = level;
            if (level < this.mDischargeUnplugLevel) {
                this.mLowDischargeAmountSinceCharge += (this.mDischargeUnplugLevel - level) - 1;
                this.mHighDischargeAmountSinceCharge += this.mDischargeUnplugLevel - level;
            }
            updateDischargeScreenLevelsLocked(screenOn, screenOn);
            updateTimeBasesLocked(false, screenOn ^ 1, uptime, realtime);
            this.mChargeStepTracker.init();
            this.mLastChargeStepLevel = level;
            this.mMaxChargeStepLevel = level;
            this.mInitStepMode = this.mCurStepMode;
            this.mModStepMode = 0;
        }
        if ((doWrite || this.mLastWriteTime + DateUtils.MINUTE_IN_MILLIS < mSecRealtime) && this.mFile != null) {
            writeAsyncLocked();
        }
    }

    private void startRecordingHistory(long elapsedRealtimeMs, long uptimeMs, boolean reset) {
        this.mRecordingHistory = true;
        this.mHistoryCur.currentTime = System.currentTimeMillis();
        addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, reset ? (byte) 7 : (byte) 5, this.mHistoryCur);
        this.mHistoryCur.currentTime = 0;
        if (reset) {
            initActiveHistoryEventsLocked(elapsedRealtimeMs, uptimeMs);
        }
    }

    private void recordCurrentTimeChangeLocked(long currentTime, long elapsedRealtimeMs, long uptimeMs) {
        if (this.mRecordingHistory) {
            this.mHistoryCur.currentTime = currentTime;
            addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 5, this.mHistoryCur);
            this.mHistoryCur.currentTime = 0;
        }
    }

    private void recordShutdownLocked(long elapsedRealtimeMs, long uptimeMs) {
        if (this.mRecordingHistory) {
            this.mHistoryCur.currentTime = System.currentTimeMillis();
            addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 8, this.mHistoryCur);
            this.mHistoryCur.currentTime = 0;
        }
    }

    private void scheduleSyncExternalStatsLocked(String reason, int updateFlags) {
        if (this.mExternalSync != null) {
            this.mExternalSync.scheduleSync(reason, updateFlags);
        }
    }

    public void setBatteryStateLocked(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        temp = Math.max(0, temp);
        boolean onBattery = plugType == 0;
        long uptime = this.mClocks.uptimeMillis();
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        if (!this.mHaveBatteryLevel) {
            HistoryItem historyItem;
            this.mHaveBatteryLevel = true;
            if (onBattery == this.mOnBattery) {
                if (onBattery) {
                    historyItem = this.mHistoryCur;
                    historyItem.states &= -524289;
                } else {
                    historyItem = this.mHistoryCur;
                    historyItem.states |= 524288;
                }
            }
            historyItem = this.mHistoryCur;
            historyItem.states2 |= 16777216;
            this.mHistoryCur.batteryStatus = (byte) status;
            this.mHistoryCur.batteryLevel = (byte) level;
            this.mHistoryCur.batteryChargeUAh = chargeUAh;
            this.mLastDischargeStepLevel = level;
            this.mLastChargeStepLevel = level;
            this.mMinDischargeStepLevel = level;
            this.mMaxChargeStepLevel = level;
            this.mLastChargingStateLevel = level;
        } else if (!(this.mCurrentBatteryLevel == level && this.mOnBattery == onBattery)) {
            recordDailyStatsIfNeededLocked(level >= 100 ? onBattery : false);
        }
        int oldStatus = this.mHistoryCur.batteryStatus;
        if (onBattery) {
            this.mDischargeCurrentLevel = level;
            if (!this.mRecordingHistory) {
                this.mRecordingHistory = true;
                startRecordingHistory(elapsedRealtime, uptime, true);
            }
        } else if (level < 96 && !this.mRecordingHistory) {
            this.mRecordingHistory = true;
            startRecordingHistory(elapsedRealtime, uptime, true);
        }
        this.mCurrentBatteryLevel = level;
        if (this.mDischargePlugLevel < 0) {
            this.mDischargePlugLevel = level;
        }
        long chargeDiff;
        if (onBattery != this.mOnBattery) {
            this.mHistoryCur.batteryLevel = (byte) level;
            this.mHistoryCur.batteryStatus = (byte) status;
            this.mHistoryCur.batteryHealth = (byte) health;
            this.mHistoryCur.batteryPlugType = (byte) plugType;
            this.mHistoryCur.batteryTemperature = (short) temp;
            this.mHistoryCur.batteryVoltage = (char) volt;
            if (chargeUAh < this.mHistoryCur.batteryChargeUAh) {
                chargeDiff = (long) (this.mHistoryCur.batteryChargeUAh - chargeUAh);
                this.mDischargeCounter.addCountLocked(chargeDiff);
                this.mDischargeScreenOffCounter.addCountLocked(chargeDiff);
            }
            this.mHistoryCur.batteryChargeUAh = chargeUAh;
            setOnBatteryLocked(elapsedRealtime, uptime, onBattery, oldStatus, level, chargeUAh);
        } else {
            boolean changed = false;
            if (this.mHistoryCur.batteryLevel != level) {
                this.mHistoryCur.batteryLevel = (byte) level;
                changed = true;
                scheduleSyncExternalStatsLocked("battery-level", 15);
            }
            if (this.mHistoryCur.batteryStatus != status) {
                this.mHistoryCur.batteryStatus = (byte) status;
                changed = true;
            }
            if (this.mHistoryCur.batteryHealth != health) {
                this.mHistoryCur.batteryHealth = (byte) health;
                changed = true;
            }
            if (this.mHistoryCur.batteryPlugType != plugType) {
                this.mHistoryCur.batteryPlugType = (byte) plugType;
                changed = true;
            }
            if (temp >= this.mHistoryCur.batteryTemperature + 10 || temp <= this.mHistoryCur.batteryTemperature - 10) {
                this.mHistoryCur.batteryTemperature = (short) temp;
                changed = true;
            }
            if (volt > this.mHistoryCur.batteryVoltage + 20 || volt < this.mHistoryCur.batteryVoltage - 20) {
                this.mHistoryCur.batteryVoltage = (char) volt;
                changed = true;
            }
            if (chargeUAh >= this.mHistoryCur.batteryChargeUAh + 10 || chargeUAh <= this.mHistoryCur.batteryChargeUAh - 10) {
                if (chargeUAh < this.mHistoryCur.batteryChargeUAh) {
                    chargeDiff = (long) (this.mHistoryCur.batteryChargeUAh - chargeUAh);
                    this.mDischargeCounter.addCountLocked(chargeDiff);
                    this.mDischargeScreenOffCounter.addCountLocked(chargeDiff);
                }
                this.mHistoryCur.batteryChargeUAh = chargeUAh;
                changed = true;
            }
            long modeBits = ((((long) this.mInitStepMode) << 48) | (((long) this.mModStepMode) << 56)) | (((long) (level & 255)) << 40);
            if (onBattery) {
                changed |= setChargingLocked(false);
                if (this.mLastDischargeStepLevel != level && this.mMinDischargeStepLevel > level) {
                    this.mDischargeStepTracker.addLevelSteps(this.mLastDischargeStepLevel - level, modeBits, elapsedRealtime);
                    this.mDailyDischargeStepTracker.addLevelSteps(this.mLastDischargeStepLevel - level, modeBits, elapsedRealtime);
                    this.mLastDischargeStepLevel = level;
                    this.mMinDischargeStepLevel = level;
                    this.mInitStepMode = this.mCurStepMode;
                    this.mModStepMode = 0;
                }
            } else {
                if (level >= 90) {
                    changed |= setChargingLocked(true);
                    this.mLastChargeStepLevel = level;
                }
                if (this.mCharging) {
                    if (this.mLastChargeStepLevel > level) {
                        changed |= setChargingLocked(false);
                        this.mLastChargeStepLevel = level;
                    }
                } else if (this.mLastChargeStepLevel < level) {
                    changed |= setChargingLocked(true);
                    this.mLastChargeStepLevel = level;
                }
                if (this.mLastChargeStepLevel != level && this.mMaxChargeStepLevel < level) {
                    this.mChargeStepTracker.addLevelSteps(level - this.mLastChargeStepLevel, modeBits, elapsedRealtime);
                    this.mDailyChargeStepTracker.addLevelSteps(level - this.mLastChargeStepLevel, modeBits, elapsedRealtime);
                    this.mLastChargeStepLevel = level;
                    this.mMaxChargeStepLevel = level;
                    this.mInitStepMode = this.mCurStepMode;
                    this.mModStepMode = 0;
                }
            }
            if (changed) {
                addHistoryRecordLocked(elapsedRealtime, uptime);
            }
        }
        if (!onBattery && status == 5) {
            this.mRecordingHistory = false;
        }
        if (this.mMinLearnedBatteryCapacity == -1) {
            this.mMinLearnedBatteryCapacity = chargeFullUAh;
        } else {
            Math.min(this.mMinLearnedBatteryCapacity, chargeFullUAh);
        }
        this.mMaxLearnedBatteryCapacity = Math.max(this.mMaxLearnedBatteryCapacity, chargeFullUAh);
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
        return (((long) this.mCurrentBatteryLevel) * msPerLevel) * 1000;
    }

    public LevelStepTracker getDischargeLevelStepTracker() {
        return this.mDischargeStepTracker;
    }

    public LevelStepTracker getDailyDischargeLevelStepTracker() {
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
        return (((long) (100 - this.mCurrentBatteryLevel)) * msPerLevel) * 1000;
    }

    public LevelStepTracker getChargeLevelStepTracker() {
        return this.mChargeStepTracker;
    }

    public LevelStepTracker getDailyChargeLevelStepTracker() {
        return this.mDailyChargeStepTracker;
    }

    public ArrayList<PackageChange> getDailyPackageChanges() {
        return this.mDailyPackageChanges;
    }

    protected long getBatteryUptimeLocked() {
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
            if (this.mOnBattery && this.mScreenState == 2 && this.mDischargeCurrentLevel < this.mDischargeScreenOnUnplugLevel) {
                val += this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOnSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOnSinceCharge;
            if (this.mOnBattery && this.mScreenState == 2 && this.mDischargeCurrentLevel < this.mDischargeScreenOnUnplugLevel) {
                val += this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOff() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOff;
            if (this.mOnBattery && this.mScreenState != 2 && this.mDischargeCurrentLevel < this.mDischargeScreenOffUnplugLevel) {
                val += this.mDischargeScreenOffUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOffSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOffSinceCharge;
            if (this.mOnBattery && this.mScreenState != 2 && this.mDischargeCurrentLevel < this.mDischargeScreenOffUnplugLevel) {
                val += this.mDischargeScreenOffUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public Uid getUidStatsLocked(int uid) {
        Uid u = (Uid) this.mUidStats.get(uid);
        if (u != null) {
            return u;
        }
        u = new Uid(this, uid);
        this.mUidStats.put(uid, u);
        return u;
    }

    public void removeUidStatsLocked(int uid) {
        this.mKernelUidCpuTimeReader.removeUid(uid);
        this.mKernelUidCpuFreqTimeReader.removeUid(uid);
        this.mUidStats.remove(uid);
    }

    public Proc getProcessStatsLocked(int uid, String name) {
        return getUidStatsLocked(mapUid(uid)).getProcessStatsLocked(name);
    }

    public Pkg getPackageStatsLocked(int uid, String pkg) {
        return getUidStatsLocked(mapUid(uid)).getPackageStatsLocked(pkg);
    }

    public Serv getServiceStatsLocked(int uid, String pkg, String name) {
        return getUidStatsLocked(mapUid(uid)).getServiceStatsLocked(pkg, name);
    }

    public void shutdownLocked() {
        recordShutdownLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis());
        writeSyncLocked();
        this.mShuttingDown = true;
    }

    public void writeAsyncLocked() {
        writeLocked(false);
    }

    public void writeSyncLocked() {
        writeLocked(true);
    }

    void writeLocked(boolean sync) {
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

    /* JADX WARNING: Missing block: B:7:0x000b, code:
            r5.mWriteLock.lock();
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            r2 = new java.io.FileOutputStream(r5.mFile.chooseForWrite());
            r2.write(r1.marshall());
            r2.flush();
            android.os.FileUtils.sync(r2);
            r2.close();
            r5.mFile.commit();
     */
    /* JADX WARNING: Missing block: B:10:0x0038, code:
            return;
     */
    /* JADX WARNING: Missing block: B:14:0x003c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            android.util.Slog.w("BatteryStats", "Error writing battery statistics", r0);
            r5.mFile.rollback();
     */
    /* JADX WARNING: Missing block: B:18:0x0055, code:
            r1.recycle();
            r5.mWriteLock.unlock();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void commitPendingDataToDisk() {
        synchronized (this) {
            Parcel next = this.mPendingWrite;
            this.mPendingWrite = null;
            if (next == null) {
            }
        }
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
                    addHistoryBufferLocked(elapsedRealtime, uptime, (byte) 4, this.mHistoryCur);
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

    void readHistory(Parcel in, boolean andOldHistory) throws ParcelFormatException {
        long historyBaseTime = in.readLong();
        this.mHistoryBuffer.setDataSize(0);
        this.mHistoryBuffer.setDataPosition(0);
        this.mHistoryTagPool.clear();
        this.mNextHistoryTagIdx = 0;
        this.mNumHistoryTagChars = 0;
        int numTags = in.readInt();
        for (int i = 0; i < numTags; i++) {
            int idx = in.readInt();
            String str = in.readString();
            if (str == null) {
                throw new ParcelFormatException("null history tag string");
            }
            int uid = in.readInt();
            HistoryTag tag = new HistoryTag();
            tag.string = str;
            tag.uid = uid;
            tag.poolIdx = idx;
            this.mHistoryTagPool.put(tag, Integer.valueOf(idx));
            if (idx >= this.mNextHistoryTagIdx) {
                this.mNextHistoryTagIdx = idx + 1;
            }
            this.mNumHistoryTagChars += tag.string.length() + 1;
        }
        int bufSize = in.readInt();
        int curPos = in.dataPosition();
        if (bufSize >= SurfaceControl.FX_SURFACE_MASK) {
            throw new ParcelFormatException("File corrupt: history data buffer too large " + bufSize);
        } else if ((bufSize & -4) != bufSize) {
            throw new ParcelFormatException("File corrupt: history data buffer not aligned " + bufSize);
        } else {
            this.mHistoryBuffer.appendFrom(in, curPos, bufSize);
            in.setDataPosition(curPos + bufSize);
            if (andOldHistory) {
                readOldHistory(in);
            }
            this.mHistoryBaseTime = historyBaseTime;
            if (this.mHistoryBaseTime > 0) {
                this.mHistoryBaseTime = (this.mHistoryBaseTime - this.mClocks.elapsedRealtime()) + 1;
            }
        }
    }

    void readOldHistory(Parcel in) {
    }

    void writeHistory(Parcel out, boolean inclData, boolean andOldHistory) {
        out.writeLong(this.mHistoryBaseTime + this.mLastHistoryElapsedRealtime);
        if (inclData) {
            out.writeInt(this.mHistoryTagPool.size());
            for (Entry<HistoryTag, Integer> ent : this.mHistoryTagPool.entrySet()) {
                HistoryTag tag = (HistoryTag) ent.getKey();
                out.writeInt(((Integer) ent.getValue()).intValue());
                out.writeString(tag.string);
                out.writeInt(tag.uid);
            }
            out.writeInt(this.mHistoryBuffer.dataSize());
            out.appendFrom(this.mHistoryBuffer, 0, this.mHistoryBuffer.dataSize());
            if (andOldHistory) {
                writeOldHistory(out);
            }
            return;
        }
        out.writeInt(0);
        out.writeInt(0);
    }

    void writeOldHistory(Parcel out) {
    }

    public void readSummaryFromParcel(Parcel in) throws ParcelFormatException {
        int version = in.readInt();
        if (version != 159) {
            Slog.w("BatteryStats", "readFromParcel: version got " + version + ", expected " + 159 + "; erasing old stats");
            return;
        }
        int i;
        readHistory(in, true);
        this.mStartCount = in.readInt();
        this.mUptime = in.readLong();
        this.mRealtime = in.readLong();
        this.mStartClockTime = in.readLong();
        this.mStartPlatformVersion = in.readString();
        this.mEndPlatformVersion = in.readString();
        this.mOnBatteryTimeBase.readSummaryFromParcel(in);
        this.mOnBatteryScreenOffTimeBase.readSummaryFromParcel(in);
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
        this.mDischargeStepTracker.readFromParcel(in);
        this.mChargeStepTracker.readFromParcel(in);
        this.mDailyDischargeStepTracker.readFromParcel(in);
        this.mDailyChargeStepTracker.readFromParcel(in);
        this.mDischargeCounter.readSummaryFromParcelLocked(in);
        this.mDischargeScreenOffCounter.readSummaryFromParcelLocked(in);
        int NPKG = in.readInt();
        if (NPKG > 0) {
            this.mDailyPackageChanges = new ArrayList(NPKG);
            while (NPKG > 0) {
                NPKG--;
                PackageChange pc = new PackageChange();
                pc.mPackageName = in.readString();
                pc.mUpdate = in.readInt() != 0;
                pc.mVersionCode = in.readInt();
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
        this.mScreenOnTimer.readSummaryFromParcelLocked(in);
        for (i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i].readSummaryFromParcelLocked(in);
        }
        this.mInteractive = false;
        this.mInteractiveTimer.readSummaryFromParcelLocked(in);
        this.mPhoneOn = false;
        this.mPowerSaveModeEnabledTimer.readSummaryFromParcelLocked(in);
        this.mLongestLightIdleTime = in.readLong();
        this.mLongestFullIdleTime = in.readLong();
        this.mDeviceIdleModeLightTimer.readSummaryFromParcelLocked(in);
        this.mDeviceIdleModeFullTimer.readSummaryFromParcelLocked(in);
        this.mDeviceLightIdlingTimer.readSummaryFromParcelLocked(in);
        this.mDeviceIdlingTimer.readSummaryFromParcelLocked(in);
        this.mPhoneOnTimer.readSummaryFromParcelLocked(in);
        for (i = 0; i < 5; i++) {
            this.mPhoneSignalStrengthsTimer[i].readSummaryFromParcelLocked(in);
        }
        this.mPhoneSignalScanningTimer.readSummaryFromParcelLocked(in);
        for (i = 0; i < 17; i++) {
            this.mPhoneDataConnectionsTimer[i].readSummaryFromParcelLocked(in);
        }
        for (i = 0; i < 10; i++) {
            this.mNetworkByteActivityCounters[i].readSummaryFromParcelLocked(in);
            this.mNetworkPacketActivityCounters[i].readSummaryFromParcelLocked(in);
        }
        this.mMobileRadioPowerState = 1;
        this.mMobileRadioActiveTimer.readSummaryFromParcelLocked(in);
        this.mMobileRadioActivePerAppTimer.readSummaryFromParcelLocked(in);
        this.mMobileRadioActiveAdjustedTime.readSummaryFromParcelLocked(in);
        this.mMobileRadioActiveUnknownTime.readSummaryFromParcelLocked(in);
        this.mMobileRadioActiveUnknownCount.readSummaryFromParcelLocked(in);
        this.mWifiRadioPowerState = 1;
        this.mWifiOn = false;
        this.mWifiOnTimer.readSummaryFromParcelLocked(in);
        this.mGlobalWifiRunning = false;
        this.mGlobalWifiRunningTimer.readSummaryFromParcelLocked(in);
        for (i = 0; i < 8; i++) {
            this.mWifiStateTimer[i].readSummaryFromParcelLocked(in);
        }
        for (i = 0; i < 13; i++) {
            this.mWifiSupplStateTimer[i].readSummaryFromParcelLocked(in);
        }
        for (i = 0; i < 5; i++) {
            this.mWifiSignalStrengthsTimer[i].readSummaryFromParcelLocked(in);
        }
        this.mWifiActivity.readSummaryFromParcel(in);
        this.mBluetoothActivity.readSummaryFromParcel(in);
        this.mModemActivity.readSummaryFromParcel(in);
        this.mHasWifiReporting = in.readInt() != 0;
        this.mHasBluetoothReporting = in.readInt() != 0;
        this.mHasModemReporting = in.readInt() != 0;
        int readInt = in.readInt();
        this.mLoadedNumConnectivityChange = readInt;
        this.mNumConnectivityChange = readInt;
        this.mFlashlightOnNesting = 0;
        this.mFlashlightOnTimer.readSummaryFromParcelLocked(in);
        this.mCameraOnNesting = 0;
        this.mCameraOnTimer.readSummaryFromParcelLocked(in);
        this.mBluetoothScanNesting = 0;
        this.mBluetoothScanTimer.readSummaryFromParcelLocked(in);
        int NKW = in.readInt();
        if (NKW > 10000) {
            throw new ParcelFormatException("File corrupt: too many kernel wake locks " + NKW);
        }
        for (int ikw = 0; ikw < NKW; ikw++) {
            if (in.readInt() != 0) {
                getKernelWakelockTimerLocked(in.readString()).readSummaryFromParcelLocked(in);
            }
        }
        int NWR = in.readInt();
        if (NWR > 10000) {
            throw new ParcelFormatException("File corrupt: too many wakeup reasons " + NWR);
        }
        for (int iwr = 0; iwr < NWR; iwr++) {
            if (in.readInt() != 0) {
                getWakeupReasonTimerLocked(in.readString()).readSummaryFromParcelLocked(in);
            }
        }
        int NMS = in.readInt();
        for (int ims = 0; ims < NMS; ims++) {
            if (in.readInt() != 0) {
                getKernelMemoryTimerLocked(in.readLong()).readSummaryFromParcelLocked(in);
            }
        }
        this.mCpuFreqs = in.createLongArray();
        int NU = in.readInt();
        if (NU > 10000) {
            throw new ParcelFormatException("File corrupt: too many uids " + NU);
        }
        for (int iu = 0; iu < NU; iu++) {
            int uid = in.readInt();
            Uid uid2 = new Uid(this, uid);
            this.mUidStats.put(uid, uid2);
            uid2.mOnBatteryBackgroundTimeBase.readSummaryFromParcel(in);
            uid2.mOnBatteryScreenOffBackgroundTimeBase.readSummaryFromParcel(in);
            uid2.mWifiRunning = false;
            if (in.readInt() != 0) {
                uid2.mWifiRunningTimer.readSummaryFromParcelLocked(in);
            }
            uid2.mFullWifiLockOut = false;
            if (in.readInt() != 0) {
                uid2.mFullWifiLockTimer.readSummaryFromParcelLocked(in);
            }
            uid2.mWifiScanStarted = false;
            if (in.readInt() != 0) {
                uid2.mWifiScanTimer.readSummaryFromParcelLocked(in);
            }
            uid2.mWifiBatchedScanBinStarted = -1;
            for (i = 0; i < 5; i++) {
                if (in.readInt() != 0) {
                    uid2.makeWifiBatchedScanBin(i, null);
                    uid2.mWifiBatchedScanTimer[i].readSummaryFromParcelLocked(in);
                }
            }
            uid2.mWifiMulticastEnabled = false;
            if (in.readInt() != 0) {
                uid2.mWifiMulticastTimer.readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createAudioTurnedOnTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createVideoTurnedOnTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createFlashlightTurnedOnTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createCameraTurnedOnTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createForegroundActivityTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createAggregatedPartialWakelockTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createBluetoothScanTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createBluetoothUnoptimizedScanTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createBluetoothScanResultCounterLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                uid2.createBluetoothScanResultBgCounterLocked().readSummaryFromParcelLocked(in);
            }
            uid2.mProcessState = 18;
            for (i = 0; i < 6; i++) {
                if (in.readInt() != 0) {
                    uid2.makeProcessState(i, null);
                    uid2.mProcessStateTimer[i].readSummaryFromParcelLocked(in);
                }
            }
            if (in.readInt() != 0) {
                uid2.createVibratorOnTimerLocked().readSummaryFromParcelLocked(in);
            }
            if (in.readInt() != 0) {
                if (uid2.mUserActivityCounters == null) {
                    uid2.initUserActivityLocked();
                }
                for (i = 0; i < 4; i++) {
                    uid2.mUserActivityCounters[i].readSummaryFromParcelLocked(in);
                }
            }
            if (in.readInt() != 0) {
                if (uid2.mNetworkByteActivityCounters == null) {
                    uid2.initNetworkActivityLocked();
                }
                for (i = 0; i < 10; i++) {
                    uid2.mNetworkByteActivityCounters[i].readSummaryFromParcelLocked(in);
                    uid2.mNetworkPacketActivityCounters[i].readSummaryFromParcelLocked(in);
                }
                uid2.mMobileRadioActiveTime.readSummaryFromParcelLocked(in);
                uid2.mMobileRadioActiveCount.readSummaryFromParcelLocked(in);
            }
            uid2.mUserCpuTime.readSummaryFromParcelLocked(in);
            uid2.mSystemCpuTime.readSummaryFromParcelLocked(in);
            if (in.readInt() != 0) {
                int numClusters = in.readInt();
                if (this.mPowerProfile == null || this.mPowerProfile.getNumCpuClusters() == numClusters) {
                    uid2.mCpuClusterSpeed = new LongSamplingCounter[numClusters][];
                    int cluster = 0;
                    while (cluster < numClusters) {
                        if (in.readInt() != 0) {
                            int NSB = in.readInt();
                            if (this.mPowerProfile == null || this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster) == NSB) {
                                uid2.mCpuClusterSpeed[cluster] = new LongSamplingCounter[NSB];
                                for (int speed = 0; speed < NSB; speed++) {
                                    if (in.readInt() != 0) {
                                        uid2.mCpuClusterSpeed[cluster][speed] = new LongSamplingCounter(this.mOnBatteryTimeBase);
                                        uid2.mCpuClusterSpeed[cluster][speed].readSummaryFromParcelLocked(in);
                                    }
                                }
                            } else {
                                throw new ParcelFormatException("File corrupt: too many speed bins " + NSB);
                            }
                        }
                        uid2.mCpuClusterSpeed[cluster] = null;
                        cluster++;
                    }
                } else {
                    throw new ParcelFormatException("Incompatible cpu cluster arrangement");
                }
            }
            uid2.mCpuClusterSpeed = null;
            uid2.mCpuFreqTimeMs = LongSamplingCounterArray.readSummaryFromParcelLocked(in, this.mOnBatteryTimeBase);
            uid2.mScreenOffCpuFreqTimeMs = LongSamplingCounterArray.readSummaryFromParcelLocked(in, this.mOnBatteryScreenOffTimeBase);
            if (in.readInt() != 0) {
                uid2.mMobileRadioApWakeupCount = new LongSamplingCounter(this.mOnBatteryTimeBase);
                uid2.mMobileRadioApWakeupCount.readSummaryFromParcelLocked(in);
            } else {
                uid2.mMobileRadioApWakeupCount = null;
            }
            if (in.readInt() != 0) {
                uid2.mWifiRadioApWakeupCount = new LongSamplingCounter(this.mOnBatteryTimeBase);
                uid2.mWifiRadioApWakeupCount.readSummaryFromParcelLocked(in);
            } else {
                uid2.mWifiRadioApWakeupCount = null;
            }
            int NW = in.readInt();
            if (NW > 101) {
                Slog.i(TAG, "NW > 101, uid: " + uid);
                throw new ParcelFormatException("File corrupt: too many wake locks " + NW);
            }
            for (int iw = 0; iw < NW; iw++) {
                uid2.readWakeSummaryFromParcelLocked(in.readString(), in);
            }
            int NS = in.readInt();
            if (NS > 101) {
                throw new ParcelFormatException("File corrupt: too many syncs " + NS);
            }
            int is;
            for (is = 0; is < NS; is++) {
                uid2.readSyncSummaryFromParcelLocked(in.readString(), in);
            }
            int NJ = in.readInt();
            if (NJ > 101) {
                throw new ParcelFormatException("File corrupt: too many job timers " + NJ);
            }
            for (int ij = 0; ij < NJ; ij++) {
                uid2.readJobSummaryFromParcelLocked(in.readString(), in);
            }
            int NP = in.readInt();
            if (NP > 1000) {
                throw new ParcelFormatException("File corrupt: too many sensors " + NP);
            }
            for (is = 0; is < NP; is++) {
                int seNumber = in.readInt();
                if (in.readInt() != 0) {
                    uid2.getSensorTimerLocked(seNumber, true).readSummaryFromParcelLocked(in);
                }
            }
            NP = in.readInt();
            if (NP > 1000) {
                throw new ParcelFormatException("File corrupt: too many processes " + NP);
            }
            int ip;
            long readLong;
            for (ip = 0; ip < NP; ip++) {
                Proc p = uid2.getProcessStatsLocked(in.readString());
                readLong = in.readLong();
                p.mLoadedUserTime = readLong;
                p.mUserTime = readLong;
                readLong = in.readLong();
                p.mLoadedSystemTime = readLong;
                p.mSystemTime = readLong;
                readLong = in.readLong();
                p.mLoadedForegroundTime = readLong;
                p.mForegroundTime = readLong;
                readInt = in.readInt();
                p.mLoadedStarts = readInt;
                p.mStarts = readInt;
                readInt = in.readInt();
                p.mLoadedNumCrashes = readInt;
                p.mNumCrashes = readInt;
                readInt = in.readInt();
                p.mLoadedNumAnrs = readInt;
                p.mNumAnrs = readInt;
                p.readExcessivePowerFromParcelLocked(in);
            }
            NP = in.readInt();
            if (NP > 10000) {
                throw new ParcelFormatException("File corrupt: too many packages " + NP);
            }
            for (ip = 0; ip < NP; ip++) {
                String pkgName = in.readString();
                Pkg p2 = uid2.getPackageStatsLocked(pkgName);
                int NWA = in.readInt();
                if (NWA > 1000) {
                    throw new ParcelFormatException("File corrupt: too many wakeup alarms " + NWA);
                }
                p2.mWakeupAlarms.clear();
                for (int iwa = 0; iwa < NWA; iwa++) {
                    String tag = in.readString();
                    Counter c = new Counter(this.mOnBatteryTimeBase);
                    c.readSummaryFromParcelLocked(in);
                    p2.mWakeupAlarms.put(tag, c);
                }
                NS = in.readInt();
                if (NS > 1000) {
                    throw new ParcelFormatException("File corrupt: too many services " + NS);
                }
                for (is = 0; is < NS; is++) {
                    Serv s = uid2.getServiceStatsLocked(pkgName, in.readString());
                    readLong = in.readLong();
                    s.mLoadedStartTime = readLong;
                    s.mStartTime = readLong;
                    readInt = in.readInt();
                    s.mLoadedStarts = readInt;
                    s.mStarts = readInt;
                    readInt = in.readInt();
                    s.mLoadedLaunches = readInt;
                    s.mLaunches = readInt;
                }
            }
        }
    }

    public void writeSummaryToParcel(Parcel out, boolean inclHistory) {
        int i;
        pullPendingStateUpdatesLocked();
        long startClockTime = getStartClockTime();
        long NOW_SYS = this.mClocks.uptimeMillis() * 1000;
        long NOWREAL_SYS = this.mClocks.elapsedRealtime() * 1000;
        out.writeInt(159);
        writeHistory(out, inclHistory, true);
        out.writeInt(this.mStartCount);
        out.writeLong(computeUptime(NOW_SYS, 0));
        out.writeLong(computeRealtime(NOWREAL_SYS, 0));
        out.writeLong(startClockTime);
        out.writeString(this.mStartPlatformVersion);
        out.writeString(this.mEndPlatformVersion);
        this.mOnBatteryTimeBase.writeSummaryToParcel(out, NOW_SYS, NOWREAL_SYS);
        this.mOnBatteryScreenOffTimeBase.writeSummaryToParcel(out, NOW_SYS, NOWREAL_SYS);
        out.writeInt(this.mDischargeUnplugLevel);
        out.writeInt(this.mDischargePlugLevel);
        out.writeInt(this.mDischargeCurrentLevel);
        out.writeInt(this.mCurrentBatteryLevel);
        out.writeInt(this.mEstimatedBatteryCapacity);
        out.writeInt(this.mMinLearnedBatteryCapacity);
        out.writeInt(this.mMaxLearnedBatteryCapacity);
        out.writeInt(getLowDischargeAmountSinceCharge());
        out.writeInt(getHighDischargeAmountSinceCharge());
        out.writeInt(getDischargeAmountScreenOnSinceCharge());
        out.writeInt(getDischargeAmountScreenOffSinceCharge());
        this.mDischargeStepTracker.writeToParcel(out);
        this.mChargeStepTracker.writeToParcel(out);
        this.mDailyDischargeStepTracker.writeToParcel(out);
        this.mDailyChargeStepTracker.writeToParcel(out);
        this.mDischargeCounter.writeSummaryFromParcelLocked(out);
        this.mDischargeScreenOffCounter.writeSummaryFromParcelLocked(out);
        if (this.mDailyPackageChanges != null) {
            int NPKG = this.mDailyPackageChanges.size();
            out.writeInt(NPKG);
            for (i = 0; i < NPKG; i++) {
                PackageChange pc = (PackageChange) this.mDailyPackageChanges.get(i);
                out.writeString(pc.mPackageName);
                out.writeInt(pc.mUpdate ? 1 : 0);
                out.writeInt(pc.mVersionCode);
            }
        } else {
            out.writeInt(0);
        }
        out.writeLong(this.mDailyStartTime);
        out.writeLong(this.mNextMinDailyDeadline);
        out.writeLong(this.mNextMaxDailyDeadline);
        this.mScreenOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        for (i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        this.mInteractiveTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mPowerSaveModeEnabledTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        out.writeLong(this.mLongestLightIdleTime);
        out.writeLong(this.mLongestFullIdleTime);
        this.mDeviceIdleModeLightTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mDeviceIdleModeFullTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mDeviceLightIdlingTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mDeviceIdlingTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mPhoneOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        for (i = 0; i < 5; i++) {
            this.mPhoneSignalStrengthsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        this.mPhoneSignalScanningTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        for (i = 0; i < 17; i++) {
            this.mPhoneDataConnectionsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        for (i = 0; i < 10; i++) {
            this.mNetworkByteActivityCounters[i].writeSummaryFromParcelLocked(out);
            this.mNetworkPacketActivityCounters[i].writeSummaryFromParcelLocked(out);
        }
        this.mMobileRadioActiveTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mMobileRadioActivePerAppTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mMobileRadioActiveAdjustedTime.writeSummaryFromParcelLocked(out);
        this.mMobileRadioActiveUnknownTime.writeSummaryFromParcelLocked(out);
        this.mMobileRadioActiveUnknownCount.writeSummaryFromParcelLocked(out);
        this.mWifiOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mGlobalWifiRunningTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        for (i = 0; i < 8; i++) {
            this.mWifiStateTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        for (i = 0; i < 13; i++) {
            this.mWifiSupplStateTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        for (i = 0; i < 5; i++) {
            this.mWifiSignalStrengthsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        this.mWifiActivity.writeSummaryToParcel(out);
        this.mBluetoothActivity.writeSummaryToParcel(out);
        this.mModemActivity.writeSummaryToParcel(out);
        out.writeInt(this.mHasWifiReporting ? 1 : 0);
        out.writeInt(this.mHasBluetoothReporting ? 1 : 0);
        out.writeInt(this.mHasModemReporting ? 1 : 0);
        out.writeInt(this.mNumConnectivityChange);
        this.mFlashlightOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mCameraOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mBluetoothScanTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        out.writeInt(this.mKernelWakelockStats.size());
        for (Entry<String, SamplingTimer> ent : this.mKernelWakelockStats.entrySet()) {
            Timer kwlt = (Timer) ent.getValue();
            if (kwlt != null) {
                out.writeInt(1);
                out.writeString((String) ent.getKey());
                kwlt.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
        }
        out.writeInt(this.mWakeupReasonStats.size());
        for (Entry<String, SamplingTimer> ent2 : this.mWakeupReasonStats.entrySet()) {
            SamplingTimer timer = (SamplingTimer) ent2.getValue();
            if (timer != null) {
                out.writeInt(1);
                out.writeString((String) ent2.getKey());
                timer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
        }
        out.writeInt(this.mKernelMemoryStats.size());
        for (i = 0; i < this.mKernelMemoryStats.size(); i++) {
            Timer kmt = (Timer) this.mKernelMemoryStats.valueAt(i);
            if (kmt != null) {
                out.writeInt(1);
                out.writeLong(this.mKernelMemoryStats.keyAt(i));
                kmt.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
        }
        out.writeLongArray(this.mCpuFreqs);
        int NU = this.mUidStats.size();
        out.writeInt(NU);
        for (int iu = 0; iu < NU; iu++) {
            int is;
            out.writeInt(this.mUidStats.keyAt(iu));
            Uid u = (Uid) this.mUidStats.valueAt(iu);
            u.mOnBatteryBackgroundTimeBase.writeSummaryToParcel(out, NOW_SYS, NOWREAL_SYS);
            u.mOnBatteryScreenOffBackgroundTimeBase.writeSummaryToParcel(out, NOW_SYS, NOWREAL_SYS);
            if (u.mWifiRunningTimer != null) {
                out.writeInt(1);
                u.mWifiRunningTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mFullWifiLockTimer != null) {
                out.writeInt(1);
                u.mFullWifiLockTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mWifiScanTimer != null) {
                out.writeInt(1);
                u.mWifiScanTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            for (i = 0; i < 5; i++) {
                if (u.mWifiBatchedScanTimer[i] != null) {
                    out.writeInt(1);
                    u.mWifiBatchedScanTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(0);
                }
            }
            if (u.mWifiMulticastTimer != null) {
                out.writeInt(1);
                u.mWifiMulticastTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mAudioTurnedOnTimer != null) {
                out.writeInt(1);
                u.mAudioTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mVideoTurnedOnTimer != null) {
                out.writeInt(1);
                u.mVideoTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mFlashlightTurnedOnTimer != null) {
                out.writeInt(1);
                u.mFlashlightTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mCameraTurnedOnTimer != null) {
                out.writeInt(1);
                u.mCameraTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mForegroundActivityTimer != null) {
                out.writeInt(1);
                u.mForegroundActivityTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mAggregatedPartialWakelockTimer != null) {
                out.writeInt(1);
                u.mAggregatedPartialWakelockTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mBluetoothScanTimer != null) {
                out.writeInt(1);
                u.mBluetoothScanTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mBluetoothUnoptimizedScanTimer != null) {
                out.writeInt(1);
                u.mBluetoothUnoptimizedScanTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mBluetoothScanResultCounter != null) {
                out.writeInt(1);
                u.mBluetoothScanResultCounter.writeSummaryFromParcelLocked(out);
            } else {
                out.writeInt(0);
            }
            if (u.mBluetoothScanResultBgCounter != null) {
                out.writeInt(1);
                u.mBluetoothScanResultBgCounter.writeSummaryFromParcelLocked(out);
            } else {
                out.writeInt(0);
            }
            for (i = 0; i < 6; i++) {
                if (u.mProcessStateTimer[i] != null) {
                    out.writeInt(1);
                    u.mProcessStateTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(0);
                }
            }
            if (u.mVibratorOnTimer != null) {
                out.writeInt(1);
                u.mVibratorOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(0);
            }
            if (u.mUserActivityCounters == null) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                for (i = 0; i < 4; i++) {
                    u.mUserActivityCounters[i].writeSummaryFromParcelLocked(out);
                }
            }
            if (u.mNetworkByteActivityCounters == null) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                for (i = 0; i < 10; i++) {
                    u.mNetworkByteActivityCounters[i].writeSummaryFromParcelLocked(out);
                    u.mNetworkPacketActivityCounters[i].writeSummaryFromParcelLocked(out);
                }
                u.mMobileRadioActiveTime.writeSummaryFromParcelLocked(out);
                u.mMobileRadioActiveCount.writeSummaryFromParcelLocked(out);
            }
            u.mUserCpuTime.writeSummaryFromParcelLocked(out);
            u.mSystemCpuTime.writeSummaryFromParcelLocked(out);
            if (u.mCpuClusterSpeed != null) {
                out.writeInt(1);
                out.writeInt(u.mCpuClusterSpeed.length);
                LongSamplingCounter[][] longSamplingCounterArr = u.mCpuClusterSpeed;
                int i2 = 0;
                int length = longSamplingCounterArr.length;
                while (true) {
                    int i3 = i2;
                    if (i3 >= length) {
                        break;
                    }
                    LongSamplingCounter[] cpuSpeeds = longSamplingCounterArr[i3];
                    if (cpuSpeeds != null) {
                        out.writeInt(1);
                        out.writeInt(cpuSpeeds.length);
                        for (LongSamplingCounter c : cpuSpeeds) {
                            if (c != null) {
                                out.writeInt(1);
                                c.writeSummaryFromParcelLocked(out);
                            } else {
                                out.writeInt(0);
                            }
                        }
                    } else {
                        out.writeInt(0);
                    }
                    i2 = i3 + 1;
                }
            } else {
                out.writeInt(0);
            }
            LongSamplingCounterArray.writeSummaryToParcelLocked(out, u.mCpuFreqTimeMs);
            LongSamplingCounterArray.writeSummaryToParcelLocked(out, u.mScreenOffCpuFreqTimeMs);
            if (u.mMobileRadioApWakeupCount != null) {
                out.writeInt(1);
                u.mMobileRadioApWakeupCount.writeSummaryFromParcelLocked(out);
            } else {
                out.writeInt(0);
            }
            if (u.mWifiRadioApWakeupCount != null) {
                out.writeInt(1);
                u.mWifiRadioApWakeupCount.writeSummaryFromParcelLocked(out);
            } else {
                out.writeInt(0);
            }
            ArrayMap<String, Wakelock> wakeStats = u.mWakelockStats.getMap();
            int NW = wakeStats.size();
            out.writeInt(NW);
            for (int iw = 0; iw < NW; iw++) {
                out.writeString((String) wakeStats.keyAt(iw));
                Wakelock wl = (Wakelock) wakeStats.valueAt(iw);
                if (wl.mTimerFull != null) {
                    out.writeInt(1);
                    wl.mTimerFull.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(0);
                }
                if (wl.mTimerPartial != null) {
                    out.writeInt(1);
                    wl.mTimerPartial.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(0);
                }
                if (wl.mTimerWindow != null) {
                    out.writeInt(1);
                    wl.mTimerWindow.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(0);
                }
                if (wl.mTimerDraw != null) {
                    out.writeInt(1);
                    wl.mTimerDraw.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(0);
                }
            }
            ArrayMap<String, DualTimer> syncStats = u.mSyncStats.getMap();
            int NS = syncStats.size();
            out.writeInt(NS);
            for (is = 0; is < NS; is++) {
                out.writeString((String) syncStats.keyAt(is));
                ((DualTimer) syncStats.valueAt(is)).writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            }
            ArrayMap<String, DualTimer> jobStats = u.mJobStats.getMap();
            int NJ = jobStats.size();
            out.writeInt(NJ);
            for (int ij = 0; ij < NJ; ij++) {
                out.writeString((String) jobStats.keyAt(ij));
                ((DualTimer) jobStats.valueAt(ij)).writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            }
            int NSE = u.mSensorStats.size();
            out.writeInt(NSE);
            for (int ise = 0; ise < NSE; ise++) {
                out.writeInt(u.mSensorStats.keyAt(ise));
                Sensor se = (Sensor) u.mSensorStats.valueAt(ise);
                if (se.mTimer != null) {
                    out.writeInt(1);
                    se.mTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(0);
                }
            }
            int NP = u.mProcessStats.size();
            out.writeInt(NP);
            for (int ip = 0; ip < NP; ip++) {
                out.writeString((String) u.mProcessStats.keyAt(ip));
                Proc ps = (Proc) u.mProcessStats.valueAt(ip);
                out.writeLong(ps.mUserTime);
                out.writeLong(ps.mSystemTime);
                out.writeLong(ps.mForegroundTime);
                out.writeInt(ps.mStarts);
                out.writeInt(ps.mNumCrashes);
                out.writeInt(ps.mNumAnrs);
                ps.writeExcessivePowerToParcelLocked(out);
            }
            NP = u.mPackageStats.size();
            out.writeInt(NP);
            if (NP > 0) {
                for (Entry<String, Pkg> ent3 : u.mPackageStats.entrySet()) {
                    out.writeString((String) ent3.getKey());
                    Pkg ps2 = (Pkg) ent3.getValue();
                    int NWA = ps2.mWakeupAlarms.size();
                    out.writeInt(NWA);
                    for (int iwa = 0; iwa < NWA; iwa++) {
                        out.writeString((String) ps2.mWakeupAlarms.keyAt(iwa));
                        ((Counter) ps2.mWakeupAlarms.valueAt(iwa)).writeSummaryFromParcelLocked(out);
                    }
                    NS = ps2.mServiceStats.size();
                    out.writeInt(NS);
                    for (is = 0; is < NS; is++) {
                        out.writeString((String) ps2.mServiceStats.keyAt(is));
                        Serv ss = (Serv) ps2.mServiceStats.valueAt(is);
                        out.writeLong(ss.getStartTimeToNowLocked(this.mOnBatteryTimeBase.getUptime(NOW_SYS)));
                        out.writeInt(ss.mStarts);
                        out.writeInt(ss.mLaunches);
                    }
                }
            }
        }
    }

    public void readFromParcel(Parcel in) {
        readFromParcelLocked(in);
    }

    void readFromParcelLocked(Parcel in) {
        int magic = in.readInt();
        if (magic != MAGIC) {
            throw new ParcelFormatException("Bad magic number: #" + Integer.toHexString(magic));
        }
        int i;
        readHistory(in, false);
        this.mStartCount = in.readInt();
        this.mStartClockTime = in.readLong();
        this.mStartPlatformVersion = in.readString();
        this.mEndPlatformVersion = in.readString();
        this.mUptime = in.readLong();
        this.mUptimeStart = in.readLong();
        this.mRealtime = in.readLong();
        this.mRealtimeStart = in.readLong();
        this.mOnBattery = in.readInt() != 0;
        this.mEstimatedBatteryCapacity = in.readInt();
        this.mMinLearnedBatteryCapacity = in.readInt();
        this.mMaxLearnedBatteryCapacity = in.readInt();
        this.mOnBatteryInternal = false;
        this.mOnBatteryTimeBase.readFromParcel(in);
        this.mOnBatteryScreenOffTimeBase.readFromParcel(in);
        this.mScreenState = 0;
        this.mScreenOnTimer = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase, in);
        for (i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i] = new StopwatchTimer(this.mClocks, null, -100 - i, null, this.mOnBatteryTimeBase, in);
        }
        this.mInteractive = false;
        this.mInteractiveTimer = new StopwatchTimer(this.mClocks, null, -10, null, this.mOnBatteryTimeBase, in);
        this.mPhoneOn = false;
        this.mPowerSaveModeEnabledTimer = new StopwatchTimer(this.mClocks, null, -2, null, this.mOnBatteryTimeBase, in);
        this.mLongestLightIdleTime = in.readLong();
        this.mLongestFullIdleTime = in.readLong();
        this.mDeviceIdleModeLightTimer = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase, in);
        this.mDeviceIdleModeFullTimer = new StopwatchTimer(this.mClocks, null, -11, null, this.mOnBatteryTimeBase, in);
        this.mDeviceLightIdlingTimer = new StopwatchTimer(this.mClocks, null, -15, null, this.mOnBatteryTimeBase, in);
        this.mDeviceIdlingTimer = new StopwatchTimer(this.mClocks, null, -12, null, this.mOnBatteryTimeBase, in);
        this.mPhoneOnTimer = new StopwatchTimer(this.mClocks, null, -3, null, this.mOnBatteryTimeBase, in);
        for (i = 0; i < 5; i++) {
            this.mPhoneSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -200 - i, null, this.mOnBatteryTimeBase, in);
        }
        this.mPhoneSignalScanningTimer = new StopwatchTimer(this.mClocks, null, -199, null, this.mOnBatteryTimeBase, in);
        for (i = 0; i < 17; i++) {
            this.mPhoneDataConnectionsTimer[i] = new StopwatchTimer(this.mClocks, null, -300 - i, null, this.mOnBatteryTimeBase, in);
        }
        for (i = 0; i < 10; i++) {
            this.mNetworkByteActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
            this.mNetworkPacketActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        }
        this.mMobileRadioPowerState = 1;
        this.mMobileRadioActiveTimer = new StopwatchTimer(this.mClocks, null, -400, null, this.mOnBatteryTimeBase, in);
        this.mMobileRadioActivePerAppTimer = new StopwatchTimer(this.mClocks, null, -401, null, this.mOnBatteryTimeBase, in);
        this.mMobileRadioActiveAdjustedTime = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mMobileRadioActiveUnknownTime = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mMobileRadioActiveUnknownCount = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mWifiRadioPowerState = 1;
        this.mWifiOn = false;
        this.mWifiOnTimer = new StopwatchTimer(this.mClocks, null, -4, null, this.mOnBatteryTimeBase, in);
        this.mGlobalWifiRunning = false;
        this.mGlobalWifiRunningTimer = new StopwatchTimer(this.mClocks, null, -5, null, this.mOnBatteryTimeBase, in);
        for (i = 0; i < 8; i++) {
            this.mWifiStateTimer[i] = new StopwatchTimer(this.mClocks, null, -600 - i, null, this.mOnBatteryTimeBase, in);
        }
        for (i = 0; i < 13; i++) {
            this.mWifiSupplStateTimer[i] = new StopwatchTimer(this.mClocks, null, -700 - i, null, this.mOnBatteryTimeBase, in);
        }
        for (i = 0; i < 5; i++) {
            this.mWifiSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -800 - i, null, this.mOnBatteryTimeBase, in);
        }
        this.mWifiActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1, in);
        this.mBluetoothActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 1, in);
        this.mModemActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 5, in);
        this.mHasWifiReporting = in.readInt() != 0;
        this.mHasBluetoothReporting = in.readInt() != 0;
        this.mHasModemReporting = in.readInt() != 0;
        this.mNumConnectivityChange = in.readInt();
        this.mLoadedNumConnectivityChange = in.readInt();
        this.mUnpluggedNumConnectivityChange = in.readInt();
        this.mAudioOnNesting = 0;
        this.mAudioOnTimer = new StopwatchTimer(this.mClocks, null, -7, null, this.mOnBatteryTimeBase);
        this.mVideoOnNesting = 0;
        this.mVideoOnTimer = new StopwatchTimer(this.mClocks, null, -8, null, this.mOnBatteryTimeBase);
        this.mFlashlightOnNesting = 0;
        this.mFlashlightOnTimer = new StopwatchTimer(this.mClocks, null, -9, null, this.mOnBatteryTimeBase, in);
        this.mCameraOnNesting = 0;
        this.mCameraOnTimer = new StopwatchTimer(this.mClocks, null, -13, null, this.mOnBatteryTimeBase, in);
        this.mBluetoothScanNesting = 0;
        this.mBluetoothScanTimer = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase, in);
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
        this.mDischargeStepTracker.readFromParcel(in);
        this.mChargeStepTracker.readFromParcel(in);
        this.mDischargeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mDischargeScreenOffCounter = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mLastWriteTime = in.readLong();
        this.mKernelWakelockStats.clear();
        int NKW = in.readInt();
        for (int ikw = 0; ikw < NKW; ikw++) {
            if (in.readInt() != 0) {
                this.mKernelWakelockStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase, in));
            }
        }
        this.mWakeupReasonStats.clear();
        int NWR = in.readInt();
        for (int iwr = 0; iwr < NWR; iwr++) {
            if (in.readInt() != 0) {
                this.mWakeupReasonStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase, in));
            }
        }
        this.mKernelMemoryStats.clear();
        int nmt = in.readInt();
        for (int imt = 0; imt < nmt; imt++) {
            if (in.readInt() != 0) {
                this.mKernelMemoryStats.put(Long.valueOf(in.readLong()).longValue(), new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase, in));
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
        this.mCpuFreqs = in.createLongArray();
        int numUids = in.readInt();
        this.mUidStats.clear();
        for (i = 0; i < numUids; i++) {
            int uid = in.readInt();
            Uid uid2 = new Uid(this, uid);
            uid2.readFromParcelLocked(this.mOnBatteryTimeBase, this.mOnBatteryScreenOffTimeBase, in);
            this.mUidStats.append(uid, uid2);
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        writeToParcelLocked(out, true, flags);
    }

    public void writeToParcelWithoutUids(Parcel out, int flags) {
        writeToParcelLocked(out, false, flags);
    }

    void writeToParcelLocked(Parcel out, boolean inclUids, int flags) {
        int i;
        pullPendingStateUpdatesLocked();
        long startClockTime = getStartClockTime();
        long uSecUptime = this.mClocks.uptimeMillis() * 1000;
        long uSecRealtime = this.mClocks.elapsedRealtime() * 1000;
        long batteryRealtime = this.mOnBatteryTimeBase.getRealtime(uSecRealtime);
        long batteryScreenOffRealtime = this.mOnBatteryScreenOffTimeBase.getRealtime(uSecRealtime);
        out.writeInt(MAGIC);
        writeHistory(out, true, false);
        out.writeInt(this.mStartCount);
        out.writeLong(startClockTime);
        out.writeString(this.mStartPlatformVersion);
        out.writeString(this.mEndPlatformVersion);
        out.writeLong(this.mUptime);
        out.writeLong(this.mUptimeStart);
        out.writeLong(this.mRealtime);
        out.writeLong(this.mRealtimeStart);
        out.writeInt(this.mOnBattery ? 1 : 0);
        out.writeInt(this.mEstimatedBatteryCapacity);
        out.writeInt(this.mMinLearnedBatteryCapacity);
        out.writeInt(this.mMaxLearnedBatteryCapacity);
        this.mOnBatteryTimeBase.writeToParcel(out, uSecUptime, uSecRealtime);
        this.mOnBatteryScreenOffTimeBase.writeToParcel(out, uSecUptime, uSecRealtime);
        this.mScreenOnTimer.writeToParcel(out, uSecRealtime);
        for (i = 0; i < 5; i++) {
            this.mScreenBrightnessTimer[i].writeToParcel(out, uSecRealtime);
        }
        this.mInteractiveTimer.writeToParcel(out, uSecRealtime);
        this.mPowerSaveModeEnabledTimer.writeToParcel(out, uSecRealtime);
        out.writeLong(this.mLongestLightIdleTime);
        out.writeLong(this.mLongestFullIdleTime);
        this.mDeviceIdleModeLightTimer.writeToParcel(out, uSecRealtime);
        this.mDeviceIdleModeFullTimer.writeToParcel(out, uSecRealtime);
        this.mDeviceLightIdlingTimer.writeToParcel(out, uSecRealtime);
        this.mDeviceIdlingTimer.writeToParcel(out, uSecRealtime);
        this.mPhoneOnTimer.writeToParcel(out, uSecRealtime);
        for (i = 0; i < 5; i++) {
            this.mPhoneSignalStrengthsTimer[i].writeToParcel(out, uSecRealtime);
        }
        this.mPhoneSignalScanningTimer.writeToParcel(out, uSecRealtime);
        for (i = 0; i < 17; i++) {
            this.mPhoneDataConnectionsTimer[i].writeToParcel(out, uSecRealtime);
        }
        for (i = 0; i < 10; i++) {
            this.mNetworkByteActivityCounters[i].writeToParcel(out);
            this.mNetworkPacketActivityCounters[i].writeToParcel(out);
        }
        this.mMobileRadioActiveTimer.writeToParcel(out, uSecRealtime);
        this.mMobileRadioActivePerAppTimer.writeToParcel(out, uSecRealtime);
        this.mMobileRadioActiveAdjustedTime.writeToParcel(out);
        this.mMobileRadioActiveUnknownTime.writeToParcel(out);
        this.mMobileRadioActiveUnknownCount.writeToParcel(out);
        this.mWifiOnTimer.writeToParcel(out, uSecRealtime);
        this.mGlobalWifiRunningTimer.writeToParcel(out, uSecRealtime);
        for (i = 0; i < 8; i++) {
            this.mWifiStateTimer[i].writeToParcel(out, uSecRealtime);
        }
        for (i = 0; i < 13; i++) {
            this.mWifiSupplStateTimer[i].writeToParcel(out, uSecRealtime);
        }
        for (i = 0; i < 5; i++) {
            this.mWifiSignalStrengthsTimer[i].writeToParcel(out, uSecRealtime);
        }
        this.mWifiActivity.writeToParcel(out, 0);
        this.mBluetoothActivity.writeToParcel(out, 0);
        this.mModemActivity.writeToParcel(out, 0);
        out.writeInt(this.mHasWifiReporting ? 1 : 0);
        out.writeInt(this.mHasBluetoothReporting ? 1 : 0);
        out.writeInt(this.mHasModemReporting ? 1 : 0);
        out.writeInt(this.mNumConnectivityChange);
        out.writeInt(this.mLoadedNumConnectivityChange);
        out.writeInt(this.mUnpluggedNumConnectivityChange);
        this.mFlashlightOnTimer.writeToParcel(out, uSecRealtime);
        this.mCameraOnTimer.writeToParcel(out, uSecRealtime);
        this.mBluetoothScanTimer.writeToParcel(out, uSecRealtime);
        out.writeInt(this.mDischargeUnplugLevel);
        out.writeInt(this.mDischargePlugLevel);
        out.writeInt(this.mDischargeCurrentLevel);
        out.writeInt(this.mCurrentBatteryLevel);
        out.writeInt(this.mLowDischargeAmountSinceCharge);
        out.writeInt(this.mHighDischargeAmountSinceCharge);
        out.writeInt(this.mDischargeAmountScreenOn);
        out.writeInt(this.mDischargeAmountScreenOnSinceCharge);
        out.writeInt(this.mDischargeAmountScreenOff);
        out.writeInt(this.mDischargeAmountScreenOffSinceCharge);
        this.mDischargeStepTracker.writeToParcel(out);
        this.mChargeStepTracker.writeToParcel(out);
        this.mDischargeCounter.writeToParcel(out);
        this.mDischargeScreenOffCounter.writeToParcel(out);
        out.writeLong(this.mLastWriteTime);
        if (inclUids) {
            out.writeInt(this.mKernelWakelockStats.size());
            for (Entry<String, SamplingTimer> ent : this.mKernelWakelockStats.entrySet()) {
                SamplingTimer kwlt = (SamplingTimer) ent.getValue();
                if (kwlt != null) {
                    out.writeInt(1);
                    out.writeString((String) ent.getKey());
                    kwlt.writeToParcel(out, uSecRealtime);
                } else {
                    out.writeInt(0);
                }
            }
            out.writeInt(this.mWakeupReasonStats.size());
            for (Entry<String, SamplingTimer> ent2 : this.mWakeupReasonStats.entrySet()) {
                SamplingTimer timer = (SamplingTimer) ent2.getValue();
                if (timer != null) {
                    out.writeInt(1);
                    out.writeString((String) ent2.getKey());
                    timer.writeToParcel(out, uSecRealtime);
                } else {
                    out.writeInt(0);
                }
            }
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.mKernelMemoryStats.size());
        for (i = 0; i < this.mKernelMemoryStats.size(); i++) {
            SamplingTimer kmt = (SamplingTimer) this.mKernelMemoryStats.valueAt(i);
            if (kmt != null) {
                out.writeInt(1);
                out.writeLong(this.mKernelMemoryStats.keyAt(i));
                kmt.writeToParcel(out, uSecRealtime);
            } else {
                out.writeInt(0);
            }
        }
        out.writeLongArray(this.mCpuFreqs);
        if (inclUids) {
            int size = this.mUidStats.size();
            out.writeInt(size);
            for (i = 0; i < size; i++) {
                out.writeInt(this.mUidStats.keyAt(i));
                ((Uid) this.mUidStats.valueAt(i)).writeToParcelLocked(out, uSecUptime, uSecRealtime);
            }
            return;
        }
        out.writeInt(0);
    }

    public void prepareForDumpLocked() {
        pullPendingStateUpdatesLocked();
        getStartClockTime();
    }

    public void dumpLocked(Context context, PrintWriter pw, int flags, int reqUid, long histStart) {
        super.dumpLocked(context, pw, flags, reqUid, histStart);
    }
}
