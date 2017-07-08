package com.android.internal.os;

import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.UidTraffic;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
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
import android.os.BatteryStats.PackageChange;
import android.os.BatteryStats.Uid.Pid;
import android.os.BatteryStats.Uid.Proc.ExcessivePower;
import android.os.Build;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.HwLog;
import android.util.Log;
import android.util.LogWriter;
import android.util.MutableInt;
import android.util.Printer;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import android.util.Xml;
import android.view.SurfaceControl;
import android.view.inputmethod.EditorInfo;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.os.KernelUidCpuTimeReader.Callback;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncService;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.Protocol;
import com.android.internal.util.XmlUtils;
import com.android.server.NetworkManagementSocketTagger;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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
    public static final Creator<BatteryStatsImpl> CREATOR = null;
    private static final boolean DEBUG = false;
    public static final boolean DEBUG_ENERGY = false;
    private static final boolean DEBUG_ENERGY_CPU = false;
    private static final boolean DEBUG_HISTORY = false;
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
    private static final int NETWORK_STATS_DELTA = 2;
    private static final int NETWORK_STATS_LAST = 0;
    private static final int NETWORK_STATS_NEXT = 1;
    private static final int NUM_BT_TX_LEVELS = 1;
    private static final int NUM_WIFI_TX_LEVELS = 1;
    static final int STATE_BATTERY_HEALTH_MASK = 7;
    static final int STATE_BATTERY_HEALTH_SHIFT = 26;
    static final int STATE_BATTERY_MASK = -16777216;
    static final int STATE_BATTERY_PLUG_MASK = 3;
    static final int STATE_BATTERY_PLUG_SHIFT = 24;
    static final int STATE_BATTERY_STATUS_MASK = 7;
    static final int STATE_BATTERY_STATUS_SHIFT = 29;
    private static final String TAG = "BatteryStatsImpl";
    private static final boolean USE_OLD_HISTORY = false;
    private static final int VERSION = 147;
    final HistoryEventTracker mActiveEvents;
    int mActiveHistoryStates;
    int mActiveHistoryStates2;
    int mAudioOnNesting;
    StopwatchTimer mAudioOnTimer;
    final ArrayList<StopwatchTimer> mAudioTurnedOnTimers;
    ControllerActivityCounterImpl mBluetoothActivity;
    int mBluetoothScanNesting;
    final ArrayList<StopwatchTimer> mBluetoothScanOnTimers;
    StopwatchTimer mBluetoothScanTimer;
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
    long mLastWriteTime;
    private int mLoadedNumConnectivityChange;
    long mLongestFullIdleTime;
    long mLongestLightIdleTime;
    int mLowDischargeAmountSinceCharge;
    int mMaxChargeStepLevel;
    int mMinDischargeStepLevel;
    private String[] mMobileIfaces;
    private NetworkStats[] mMobileNetworkStats;
    LongSamplingCounter mMobileRadioActiveAdjustedTime;
    StopwatchTimer mMobileRadioActivePerAppTimer;
    long mMobileRadioActiveStartTime;
    StopwatchTimer mMobileRadioActiveTimer;
    LongSamplingCounter mMobileRadioActiveUnknownCount;
    LongSamplingCounter mMobileRadioActiveUnknownTime;
    int mMobileRadioPowerState;
    int mModStepMode;
    ControllerActivityCounterImpl mModemActivity;
    final LongSamplingCounter[] mNetworkByteActivityCounters;
    final LongSamplingCounter[] mNetworkPacketActivityCounters;
    private final NetworkStatsFactory mNetworkStatsFactory;
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
    private final Entry mTmpNetworkStatsEntry;
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
    private String[] mWifiIfaces;
    int mWifiMulticastNesting;
    final ArrayList<StopwatchTimer> mWifiMulticastTimers;
    private NetworkStats[] mWifiNetworkStats;
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

    /* renamed from: com.android.internal.os.BatteryStatsImpl.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ ByteArrayOutputStream val$memStream;

        AnonymousClass2(ByteArrayOutputStream val$memStream) {
            this.val$memStream = val$memStream;
        }

        public void run() {
            synchronized (BatteryStatsImpl.this.mCheckinFile) {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = BatteryStatsImpl.this.mDailyFile.startWrite();
                    this.val$memStream.writeTo(fileOutputStream);
                    fileOutputStream.flush();
                    FileUtils.sync(fileOutputStream);
                    fileOutputStream.close();
                    BatteryStatsImpl.this.mDailyFile.finishWrite(fileOutputStream);
                } catch (IOException e) {
                    Slog.w("BatteryStats", "Error writing battery daily items", e);
                    BatteryStatsImpl.this.mDailyFile.failWrite(fileOutputStream);
                }
            }
        }
    }

    /* renamed from: com.android.internal.os.BatteryStatsImpl.3 */
    class AnonymousClass3 implements Callback {
        final /* synthetic */ long[][] val$clusterSpeeds;
        final /* synthetic */ int val$numWakelocksF;

        AnonymousClass3(int val$numWakelocksF, long[][] val$clusterSpeeds) {
            this.val$numWakelocksF = val$numWakelocksF;
            this.val$clusterSpeeds = val$clusterSpeeds;
        }

        public void onUidCpuTime(int uid, long userTimeUs, long systemTimeUs, long powerMaUs) {
            Uid u = BatteryStatsImpl.this.getUidStatsLocked(BatteryStatsImpl.this.mapUid(uid));
            BatteryStatsImpl batteryStatsImpl = BatteryStatsImpl.this;
            batteryStatsImpl.mTempTotalCpuUserTimeUs += userTimeUs;
            batteryStatsImpl = BatteryStatsImpl.this;
            batteryStatsImpl.mTempTotalCpuSystemTimeUs += systemTimeUs;
            if (this.val$numWakelocksF > 0) {
                userTimeUs = (50 * userTimeUs) / 100;
                systemTimeUs = (50 * systemTimeUs) / 100;
            }
            u.mUserCpuTime.addCountLocked(userTimeUs);
            u.mSystemCpuTime.addCountLocked(systemTimeUs);
            u.mCpuPower.addCountLocked(powerMaUs);
            int numClusters = BatteryStatsImpl.this.mPowerProfile.getNumCpuClusters();
            if (u.mCpuClusterSpeed == null || u.mCpuClusterSpeed.length != numClusters) {
                u.mCpuClusterSpeed = new LongSamplingCounter[numClusters][];
            }
            int cluster = BatteryStatsImpl.NETWORK_STATS_LAST;
            while (cluster < this.val$clusterSpeeds.length) {
                int speedsInCluster = BatteryStatsImpl.this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster);
                if (u.mCpuClusterSpeed[cluster] == null || speedsInCluster != u.mCpuClusterSpeed[cluster].length) {
                    u.mCpuClusterSpeed[cluster] = new LongSamplingCounter[speedsInCluster];
                }
                LongSamplingCounter[] cpuSpeeds = u.mCpuClusterSpeed[cluster];
                for (int speed = BatteryStatsImpl.NETWORK_STATS_LAST; speed < this.val$clusterSpeeds[cluster].length; speed += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                    if (cpuSpeeds[speed] == null) {
                        cpuSpeeds[speed] = new LongSamplingCounter(BatteryStatsImpl.this.mOnBatteryTimeBase);
                    }
                    cpuSpeeds[speed].addCountLocked(this.val$clusterSpeeds[cluster][speed]);
                }
                cluster += BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
            }
        }
    }

    /* renamed from: com.android.internal.os.BatteryStatsImpl.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ Parcel val$parcel;

        AnonymousClass4(Parcel val$parcel) {
            this.val$parcel = val$parcel;
        }

        public void run() {
            synchronized (BatteryStatsImpl.this.mCheckinFile) {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = BatteryStatsImpl.this.mCheckinFile.startWrite();
                    fileOutputStream.write(this.val$parcel.marshall());
                    fileOutputStream.flush();
                    FileUtils.sync(fileOutputStream);
                    fileOutputStream.close();
                    BatteryStatsImpl.this.mCheckinFile.finishWrite(fileOutputStream);
                    this.val$parcel.recycle();
                } catch (IOException e) {
                    Slog.w("BatteryStats", "Error writing checkin battery statistics", e);
                    BatteryStatsImpl.this.mCheckinFile.failWrite(fileOutputStream);
                    this.val$parcel.recycle();
                } catch (Throwable th) {
                    this.val$parcel.recycle();
                }
            }
        }
    }

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
            this.mLastCount = BatteryStatsImpl.NETWORK_STATS_LAST;
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
            this.mLastCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mLoadedCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mCount = BatteryStatsImpl.NETWORK_STATS_LAST;
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
                out.writeInt(BatteryStatsImpl.NETWORK_STATS_LAST);
                return;
            }
            out.writeInt(BatteryStatsImpl.NUM_WIFI_TX_LEVELS);
            timer.writeToParcel(out, elapsedRealtimeUs);
        }

        public long getTotalTimeLocked(long elapsedRealtimeUs, int which) {
            long val = computeRunTimeLocked(this.mTimeBase.getRealtime(elapsedRealtimeUs));
            if (which == BatteryStatsImpl.NETWORK_STATS_DELTA) {
                return val - this.mUnpluggedTime;
            }
            if (which != 0) {
                return val - this.mLoadedTime;
            }
            return val;
        }

        public int getCountLocked(int which) {
            int val = computeCurrentCountLocked();
            if (which == BatteryStatsImpl.NETWORK_STATS_DELTA) {
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
            this.mLastCount = BatteryStatsImpl.NETWORK_STATS_LAST;
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
            recomputeLastDuration(this.mClocks.elapsedRealtime() * 1000, BatteryStatsImpl.USE_OLD_HISTORY);
            this.mInDischarge = BatteryStatsImpl.USE_OLD_HISTORY;
            super.onTimeStopped(elapsedRealtime, baseUptime, baseRealtime);
        }

        public void onTimeStarted(long elapsedRealtime, long baseUptime, long baseRealtime) {
            recomputeLastDuration(elapsedRealtime, BatteryStatsImpl.USE_OLD_HISTORY);
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
                this.mCount += BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
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
            boolean stillActive;
            long now = this.mClocks.elapsedRealtime() * 1000;
            recomputeLastDuration(now, true);
            if (this.mLastAddedTime == now) {
                stillActive = true;
            } else {
                stillActive = BatteryStatsImpl.USE_OLD_HISTORY;
            }
            if (stillActive) {
                detachIfReset = BatteryStatsImpl.USE_OLD_HISTORY;
            }
            super.reset(detachIfReset);
            if (stillActive) {
                return BatteryStatsImpl.USE_OLD_HISTORY;
            }
            return true;
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
            for (int i = BatteryStatsImpl.NETWORK_STATS_LAST; i < numTxStates; i += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
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
            for (int i = BatteryStatsImpl.NETWORK_STATS_LAST; i < numTxStates; i += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
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
            LongSamplingCounter[] longSamplingCounterArr = this.mTxTimeMillis;
            int length = longSamplingCounterArr.length;
            for (int i = BatteryStatsImpl.NETWORK_STATS_LAST; i < length; i += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                longSamplingCounterArr[i].readSummaryFromParcelLocked(in);
            }
            this.mPowerDrainMaMs.readSummaryFromParcelLocked(in);
        }

        public int describeContents() {
            return BatteryStatsImpl.NETWORK_STATS_LAST;
        }

        public void writeSummaryToParcel(Parcel dest) {
            this.mIdleTimeMillis.writeSummaryFromParcelLocked(dest);
            this.mRxTimeMillis.writeSummaryFromParcelLocked(dest);
            dest.writeInt(this.mTxTimeMillis.length);
            LongSamplingCounter[] longSamplingCounterArr = this.mTxTimeMillis;
            int length = longSamplingCounterArr.length;
            for (int i = BatteryStatsImpl.NETWORK_STATS_LAST; i < length; i += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                longSamplingCounterArr[i].writeSummaryFromParcelLocked(dest);
            }
            this.mPowerDrainMaMs.writeSummaryFromParcelLocked(dest);
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.mIdleTimeMillis.writeToParcel(dest);
            this.mRxTimeMillis.writeToParcel(dest);
            dest.writeInt(this.mTxTimeMillis.length);
            LongSamplingCounter[] longSamplingCounterArr = this.mTxTimeMillis;
            int length = longSamplingCounterArr.length;
            for (int i = BatteryStatsImpl.NETWORK_STATS_LAST; i < length; i += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                longSamplingCounterArr[i].writeToParcel(dest);
            }
            this.mPowerDrainMaMs.writeToParcel(dest);
        }

        public void reset(boolean detachIfReset) {
            this.mIdleTimeMillis.reset(detachIfReset);
            this.mRxTimeMillis.reset(detachIfReset);
            LongSamplingCounter[] longSamplingCounterArr = this.mTxTimeMillis;
            int length = longSamplingCounterArr.length;
            for (int i = BatteryStatsImpl.NETWORK_STATS_LAST; i < length; i += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                longSamplingCounterArr[i].reset(detachIfReset);
            }
            this.mPowerDrainMaMs.reset(detachIfReset);
        }

        public void detach() {
            this.mIdleTimeMillis.detach();
            this.mRxTimeMillis.detach();
            LongSamplingCounter[] longSamplingCounterArr = this.mTxTimeMillis;
            int length = longSamplingCounterArr.length;
            for (int i = BatteryStatsImpl.NETWORK_STATS_LAST; i < length; i += BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                longSamplingCounterArr[i].detach();
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
        final AtomicInteger mCount;
        int mLastCount;
        int mLoadedCount;
        int mPluggedCount;
        final TimeBase mTimeBase;
        int mUnpluggedCount;

        Counter(TimeBase timeBase, Parcel in) {
            this.mCount = new AtomicInteger();
            this.mTimeBase = timeBase;
            this.mPluggedCount = in.readInt();
            this.mCount.set(this.mPluggedCount);
            this.mLoadedCount = in.readInt();
            this.mLastCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mUnpluggedCount = in.readInt();
            timeBase.add(this);
        }

        Counter(TimeBase timeBase) {
            this.mCount = new AtomicInteger();
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
            this.mCount.set(this.mPluggedCount);
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mPluggedCount = this.mCount.get();
        }

        public static void writeCounterToParcel(Parcel out, Counter counter) {
            if (counter == null) {
                out.writeInt(BatteryStatsImpl.NETWORK_STATS_LAST);
                return;
            }
            out.writeInt(BatteryStatsImpl.NUM_WIFI_TX_LEVELS);
            counter.writeToParcel(out);
        }

        public int getCountLocked(int which) {
            int val = this.mCount.get();
            if (which == BatteryStatsImpl.NETWORK_STATS_DELTA) {
                return val - this.mUnpluggedCount;
            }
            if (which != 0) {
                return val - this.mLoadedCount;
            }
            return val;
        }

        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCount=" + this.mCount.get() + " mLoadedCount=" + this.mLoadedCount + " mLastCount=" + this.mLastCount + " mUnpluggedCount=" + this.mUnpluggedCount + " mPluggedCount=" + this.mPluggedCount);
        }

        void stepAtomic() {
            this.mCount.incrementAndGet();
        }

        void reset(boolean detachIfReset) {
            this.mCount.set(BatteryStatsImpl.NETWORK_STATS_LAST);
            this.mUnpluggedCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mPluggedCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mLastCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mLoadedCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            if (detachIfReset) {
                detach();
            }
        }

        void detach() {
            this.mTimeBase.remove(this);
        }

        void writeSummaryFromParcelLocked(Parcel out) {
            out.writeInt(this.mCount.get());
        }

        void readSummaryFromParcelLocked(Parcel in) {
            this.mLoadedCount = in.readInt();
            this.mCount.set(this.mLoadedCount);
            this.mLastCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            int i = this.mLoadedCount;
            this.mPluggedCount = i;
            this.mUnpluggedCount = i;
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
            this.mCount = this.mPluggedCount;
        }

        public void onTimeStopped(long elapsedRealtime, long baseUptime, long baseRealtime) {
            this.mPluggedCount = this.mCount;
        }

        public long getCountLocked(int which) {
            long val = this.mTimeBase.isRunning() ? this.mCount : this.mPluggedCount;
            if (which == BatteryStatsImpl.NETWORK_STATS_DELTA) {
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
            this.mCount += count;
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

    final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean z = BatteryStatsImpl.USE_OLD_HISTORY;
            BatteryCallback cb = BatteryStatsImpl.this.mCallback;
            switch (msg.what) {
                case BatteryStatsImpl.NUM_WIFI_TX_LEVELS /*1*/:
                    synchronized (BatteryStatsImpl.this) {
                        BatteryStatsImpl.this.updateCpuTimeLocked();
                        break;
                    }
                    if (cb != null) {
                        cb.batteryNeedsCpuUpdate();
                    }
                case BatteryStatsImpl.NETWORK_STATS_DELTA /*2*/:
                    if (cb != null) {
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        cb.batteryPowerChanged(z);
                    }
                case BatteryStatsImpl.STATE_BATTERY_PLUG_MASK /*3*/:
                    if (cb != null) {
                        String action;
                        synchronized (BatteryStatsImpl.this) {
                            if (!BatteryStatsImpl.this.mCharging) {
                                action = "android.os.action.DISCHARGING";
                                break;
                            }
                            action = "android.os.action.CHARGING";
                            break;
                        }
                        Intent intent = new Intent(action);
                        intent.addFlags(EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS);
                        cb.batterySendBroadcast(intent);
                    }
                default:
            }
        }
    }

    public abstract class OverflowArrayMap<T> {
        private static final String OVERFLOW_NAME = "*overflow*";
        private static final String OVERFLOW_WEIXIN = "WakerLock:overflow";
        int M;
        ArrayMap<String, MutableInt> mActiveOverflow;
        ArrayMap<String, MutableInt> mActiveOverflowWeixin;
        T mCurOverflow;
        T mCurOverflowWeixin;
        final ArrayMap<String, T> mMap;

        public abstract T instantiateObject();

        public OverflowArrayMap() {
            this.M = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mMap = new ArrayMap();
        }

        public ArrayMap<String, T> getMap() {
            return this.mMap;
        }

        public void clear() {
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
                this.M += BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
            }
        }

        public void cleanup() {
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
                    over.value += BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
                    return obj;
                }
            }
            if (name.startsWith("WakerLock:")) {
                this.M += BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
                if (this.M > BatteryStatsImpl.MAX_WAKERLOCKS_WEIXIN) {
                    obj = this.mCurOverflowWeixin;
                    if (obj == null) {
                        obj = instantiateObject();
                        this.mCurOverflowWeixin = obj;
                        this.mMap.put(OVERFLOW_WEIXIN, obj);
                    }
                    if (this.mActiveOverflowWeixin == null) {
                        this.mActiveOverflowWeixin = new ArrayMap();
                    }
                    this.mActiveOverflowWeixin.put(name, new MutableInt(BatteryStatsImpl.NUM_WIFI_TX_LEVELS));
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
                    over.value += BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
                    return obj;
                }
            }
            if (this.mMap.size() >= BatteryStatsImpl.MAX_WAKELOCKS_PER_UID) {
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
                this.mActiveOverflow.put(name, new MutableInt(BatteryStatsImpl.NUM_WIFI_TX_LEVELS));
                return obj;
            }
            obj = instantiateObject();
            this.mMap.put(name, obj);
            return obj;
        }

        public T stopObject(String name) {
            if (name == null) {
                name = "";
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
                        }
                        return obj;
                    }
                }
            }
            Slog.wtf(BatteryStatsImpl.TAG, "Unable to find object for " + name + " mapsize=" + this.mMap.size() + " activeoverflow=" + this.mActiveOverflow + " curoverflow=" + this.mCurOverflow);
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
            super(clocks, BatteryStatsImpl.NETWORK_STATS_LAST, timeBase, in);
            this.mCurrentReportedCount = in.readInt();
            this.mUnpluggedReportedCount = in.readInt();
            this.mCurrentReportedTotalTime = in.readLong();
            this.mUnpluggedReportedTotalTime = in.readLong();
            if (in.readInt() != BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                z = BatteryStatsImpl.USE_OLD_HISTORY;
            }
            this.mTrackingReportedValues = z;
            this.mTimeBaseRunning = timeBase.isRunning();
        }

        public SamplingTimer(Clocks clocks, TimeBase timeBase) {
            super(clocks, BatteryStatsImpl.NETWORK_STATS_LAST, timeBase);
            this.mTrackingReportedValues = BatteryStatsImpl.USE_OLD_HISTORY;
            this.mTimeBaseRunning = timeBase.isRunning();
        }

        public void endSample() {
            this.mTotalTime = computeRunTimeLocked(0);
            this.mCount = computeCurrentCountLocked();
            this.mCurrentReportedTotalTime = 0;
            this.mUnpluggedReportedTotalTime = 0;
            this.mCurrentReportedCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            this.mUnpluggedReportedCount = BatteryStatsImpl.NETWORK_STATS_LAST;
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
            this.mTimeBaseRunning = BatteryStatsImpl.USE_OLD_HISTORY;
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
            int i2 = (this.mTimeBaseRunning && this.mTrackingReportedValues) ? this.mCurrentReportedCount - this.mUnpluggedReportedCount : BatteryStatsImpl.NETWORK_STATS_LAST;
            return i2 + i;
        }

        public void writeToParcel(Parcel out, long elapsedRealtimeUs) {
            int i;
            super.writeToParcel(out, elapsedRealtimeUs);
            out.writeInt(this.mCurrentReportedCount);
            out.writeInt(this.mUnpluggedReportedCount);
            out.writeLong(this.mCurrentReportedTotalTime);
            out.writeLong(this.mUnpluggedReportedTotalTime);
            if (this.mTrackingReportedValues) {
                i = BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
            } else {
                i = BatteryStatsImpl.NETWORK_STATS_LAST;
            }
            out.writeInt(i);
        }

        public boolean reset(boolean detachIfReset) {
            super.reset(detachIfReset);
            this.mTrackingReportedValues = BatteryStatsImpl.USE_OLD_HISTORY;
            this.mUnpluggedReportedTotalTime = 0;
            this.mUnpluggedReportedCount = BatteryStatsImpl.NETWORK_STATS_LAST;
            return true;
        }

        public void writeSummaryFromParcelLocked(Parcel out, long batteryRealtime) {
            super.writeSummaryFromParcelLocked(out, batteryRealtime);
            out.writeLong(this.mCurrentReportedTotalTime);
            out.writeInt(this.mCurrentReportedCount);
            out.writeInt(this.mTrackingReportedValues ? BatteryStatsImpl.NUM_WIFI_TX_LEVELS : BatteryStatsImpl.NETWORK_STATS_LAST);
        }

        public void readSummaryFromParcelLocked(Parcel in) {
            boolean z = true;
            super.readSummaryFromParcelLocked(in);
            long readLong = in.readLong();
            this.mCurrentReportedTotalTime = readLong;
            this.mUnpluggedReportedTotalTime = readLong;
            int readInt = in.readInt();
            this.mCurrentReportedCount = readInt;
            this.mUnpluggedReportedCount = readInt;
            if (in.readInt() != BatteryStatsImpl.NUM_WIFI_TX_LEVELS) {
                z = BatteryStatsImpl.USE_OLD_HISTORY;
            }
            this.mTrackingReportedValues = z;
        }
    }

    public static class StopwatchTimer extends Timer {
        long mAcquireTime;
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
            this.mNesting = i + BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
            if (i == 0) {
                long batteryRealtime = this.mTimeBase.getRealtime(1000 * elapsedRealtimeMs);
                this.mUpdateTime = batteryRealtime;
                if (this.mTimerPool != null) {
                    refreshTimersLocked(batteryRealtime, this.mTimerPool, null);
                    this.mTimerPool.add(this);
                }
                this.mCount += BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
                this.mAcquireTime = this.mTotalTime;
            }
        }

        public boolean isRunningLocked() {
            return this.mNesting > 0 ? true : BatteryStatsImpl.USE_OLD_HISTORY;
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
                        this.mNesting = BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
                        this.mTotalTime = computeRunTimeLocked(batteryRealtime);
                        this.mNesting = BatteryStatsImpl.NETWORK_STATS_LAST;
                    }
                    if (this.mTotalTime == this.mAcquireTime) {
                        this.mCount--;
                    }
                }
            }
        }

        public void stopAllRunningLocked(long elapsedRealtimeMs) {
            if (this.mNesting > 0) {
                this.mNesting = BatteryStatsImpl.NUM_WIFI_TX_LEVELS;
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
            long j2 = this.mTotalTime;
            if (this.mNesting > 0) {
                long j3 = curBatteryRealtime - this.mUpdateTime;
                int size = (this.mTimerPool == null || this.mTimerPool.size() <= 0) ? BatteryStatsImpl.NUM_WIFI_TX_LEVELS : this.mTimerPool.size();
                j = j3 / ((long) size);
            }
            return j + j2;
        }

        protected int computeCurrentCountLocked() {
            return this.mCount;
        }

        public boolean reset(boolean detachIfReset) {
            boolean canDetach;
            if (this.mNesting <= 0) {
                canDetach = true;
            } else {
                canDetach = BatteryStatsImpl.USE_OLD_HISTORY;
            }
            if (!canDetach) {
                detachIfReset = BatteryStatsImpl.USE_OLD_HISTORY;
            }
            super.reset(detachIfReset);
            if (this.mNesting > 0) {
                this.mUpdateTime = this.mTimeBase.getRealtime(this.mClocks.elapsedRealtime() * 1000);
            }
            this.mAcquireTime = this.mTotalTime;
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
            this.mNesting = BatteryStatsImpl.NETWORK_STATS_LAST;
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
        protected final ArrayList<TimeBaseObs> mObservers;
        protected long mPastRealtime;
        protected long mPastUptime;
        protected long mRealtime;
        protected long mRealtimeStart;
        protected boolean mRunning;
        protected long mUnpluggedRealtime;
        protected long mUnpluggedUptime;
        protected long mUptime;
        protected long mUptimeStart;

        public TimeBase() {
            this.mObservers = new ArrayList();
        }

        public void dump(PrintWriter pw, String prefix) {
            StringBuilder sb = new StringBuilder(LogPower.START_CHG_ROTATION);
            pw.print(prefix);
            pw.print("mRunning=");
            pw.println(this.mRunning);
            sb.setLength(BatteryStatsImpl.NETWORK_STATS_LAST);
            sb.append(prefix);
            sb.append("mUptime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mUptime / 1000);
            pw.println(sb.toString());
            sb.setLength(BatteryStatsImpl.NETWORK_STATS_LAST);
            sb.append(prefix);
            sb.append("mRealtime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mRealtime / 1000);
            pw.println(sb.toString());
            sb.setLength(BatteryStatsImpl.NETWORK_STATS_LAST);
            sb.append(prefix);
            sb.append("mPastUptime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mPastUptime / 1000);
            sb.append("mUptimeStart=");
            BatteryStatsImpl.formatTimeMs(sb, this.mUptimeStart / 1000);
            sb.append("mUnpluggedUptime=");
            BatteryStatsImpl.formatTimeMs(sb, this.mUnpluggedUptime / 1000);
            pw.println(sb.toString());
            sb.setLength(BatteryStatsImpl.NETWORK_STATS_LAST);
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
                case BatteryStatsImpl.NETWORK_STATS_LAST /*0*/:
                    return this.mUptime + getUptime(curTime);
                case BatteryStatsImpl.NUM_WIFI_TX_LEVELS /*1*/:
                    return getUptime(curTime);
                case BatteryStatsImpl.NETWORK_STATS_DELTA /*2*/:
                    return getUptime(curTime) - this.mUnpluggedUptime;
                default:
                    return 0;
            }
        }

        public long computeRealtime(long curTime, int which) {
            switch (which) {
                case BatteryStatsImpl.NETWORK_STATS_LAST /*0*/:
                    return this.mRealtime + getRealtime(curTime);
                case BatteryStatsImpl.NUM_WIFI_TX_LEVELS /*1*/:
                    return getRealtime(curTime);
                case BatteryStatsImpl.NETWORK_STATS_DELTA /*2*/:
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
                return BatteryStatsImpl.USE_OLD_HISTORY;
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
            out.writeLong(computeUptime(uptime, BatteryStatsImpl.NETWORK_STATS_LAST));
            out.writeLong(computeRealtime(realtime, BatteryStatsImpl.NETWORK_STATS_LAST));
        }

        public void readFromParcel(Parcel in) {
            this.mRunning = BatteryStatsImpl.USE_OLD_HISTORY;
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
        StopwatchTimer mAudioTurnedOnTimer;
        private ControllerActivityCounterImpl mBluetoothControllerActivity;
        StopwatchTimer mBluetoothScanTimer;
        protected BatteryStatsImpl mBsi;
        StopwatchTimer mCameraTurnedOnTimer;
        LongSamplingCounter[][] mCpuClusterSpeed;
        LongSamplingCounter mCpuPower;
        long mCurStepSystemTime;
        long mCurStepUserTime;
        StopwatchTimer mFlashlightTurnedOnTimer;
        StopwatchTimer mForegroundActivityTimer;
        boolean mFullWifiLockOut;
        StopwatchTimer mFullWifiLockTimer;
        final OverflowArrayMap<StopwatchTimer> mJobStats;
        long mLastStepSystemTime;
        long mLastStepUserTime;
        LongSamplingCounter mMobileRadioActiveCount;
        LongSamplingCounter mMobileRadioActiveTime;
        private ControllerActivityCounterImpl mModemControllerActivity;
        LongSamplingCounter[] mNetworkByteActivityCounters;
        LongSamplingCounter[] mNetworkPacketActivityCounters;
        final ArrayMap<String, Pkg> mPackageStats;
        final SparseArray<Pid> mPids;
        int mProcessState;
        StopwatchTimer[] mProcessStateTimer;
        final ArrayMap<String, Proc> mProcessStats;
        final SparseArray<Sensor> mSensorStats;
        final OverflowArrayMap<StopwatchTimer> mSyncStats;
        LongSamplingCounter mSystemCpuTime;
        final int mUid;
        Counter[] mUserActivityCounters;
        LongSamplingCounter mUserCpuTime;
        BatchTimer mVibratorOnTimer;
        StopwatchTimer mVideoTurnedOnTimer;
        final OverflowArrayMap<Wakelock> mWakelockStats;
        int mWifiBatchedScanBinStarted;
        StopwatchTimer[] mWifiBatchedScanTimer;
        private ControllerActivityCounterImpl mWifiControllerActivity;
        boolean mWifiMulticastEnabled;
        StopwatchTimer mWifiMulticastTimer;
        boolean mWifiRunning;
        StopwatchTimer mWifiRunningTimer;
        boolean mWifiScanStarted;
        StopwatchTimer mWifiScanTimer;

        /* renamed from: com.android.internal.os.BatteryStatsImpl.Uid.1 */
        class AnonymousClass1 extends OverflowArrayMap<Wakelock> {
            AnonymousClass1(com.android.internal.os.BatteryStatsImpl.Uid r1, com.android.internal.os.BatteryStatsImpl r2) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                com.android.internal.os.BatteryStatsImpl.Uid.this = r1;
                r0.<init>();
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.1.<init>(com.android.internal.os.BatteryStatsImpl$Uid, com.android.internal.os.BatteryStatsImpl):void");
            }

            public com.android.internal.os.BatteryStatsImpl.Uid.Wakelock instantiateObject() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r3 = this;
                r0 = new com.android.internal.os.BatteryStatsImpl$Uid$Wakelock;
                r1 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r1 = r1.mBsi;
                r2 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r0.<init>(r1, r2);
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.1.instantiateObject():com.android.internal.os.BatteryStatsImpl$Uid$Wakelock");
            }
        }

        /* renamed from: com.android.internal.os.BatteryStatsImpl.Uid.2 */
        class AnonymousClass2 extends OverflowArrayMap<StopwatchTimer> {
            AnonymousClass2(com.android.internal.os.BatteryStatsImpl.Uid r1, com.android.internal.os.BatteryStatsImpl r2) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                com.android.internal.os.BatteryStatsImpl.Uid.this = r1;
                r0.<init>();
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.2.<init>(com.android.internal.os.BatteryStatsImpl$Uid, com.android.internal.os.BatteryStatsImpl):void");
            }

            public com.android.internal.os.BatteryStatsImpl.StopwatchTimer instantiateObject() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r6 = this;
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r1 = r1.mBsi;
                r1 = r1.mClocks;
                r2 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r3 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r3 = r3.mBsi;
                r5 = r3.mOnBatteryTimeBase;
                r3 = 13;
                r4 = 0;
                r0.<init>(r1, r2, r3, r4, r5);
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.2.instantiateObject():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
            }
        }

        /* renamed from: com.android.internal.os.BatteryStatsImpl.Uid.3 */
        class AnonymousClass3 extends OverflowArrayMap<StopwatchTimer> {
            AnonymousClass3(com.android.internal.os.BatteryStatsImpl.Uid r1, com.android.internal.os.BatteryStatsImpl r2) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                com.android.internal.os.BatteryStatsImpl.Uid.this = r1;
                r0.<init>();
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.3.<init>(com.android.internal.os.BatteryStatsImpl$Uid, com.android.internal.os.BatteryStatsImpl):void");
            }

            public com.android.internal.os.BatteryStatsImpl.StopwatchTimer instantiateObject() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r6 = this;
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r1 = r1.mBsi;
                r1 = r1.mClocks;
                r2 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r3 = com.android.internal.os.BatteryStatsImpl.Uid.this;
                r3 = r3.mBsi;
                r5 = r3.mOnBatteryTimeBase;
                r3 = 14;
                r4 = 0;
                r0.<init>(r1, r2, r3, r4, r5);
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.3.instantiateObject():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
            }
        }

        public static class Pkg extends android.os.BatteryStats.Uid.Pkg implements TimeBaseObs {
            protected BatteryStatsImpl mBsi;
            final ArrayMap<String, Serv> mServiceStats;
            ArrayMap<String, Counter> mWakeupAlarms;

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

                public Serv(com.android.internal.os.BatteryStatsImpl r2) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r1 = this;
                    r1.<init>();
                    r1.mBsi = r2;
                    r0 = r1.mBsi;
                    r0 = r0.mOnBatteryTimeBase;
                    r0.add(r1);
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.<init>(com.android.internal.os.BatteryStatsImpl):void");
                }

                public void onTimeStarted(long r4, long r6, long r8) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r3 = this;
                    r0 = r3.getStartTimeToNowLocked(r6);
                    r3.mUnpluggedStartTime = r0;
                    r0 = r3.mStarts;
                    r3.mUnpluggedStarts = r0;
                    r0 = r3.mLaunches;
                    r3.mUnpluggedLaunches = r0;
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.onTimeStarted(long, long, long):void");
                }

                public void onTimeStopped(long r1, long r3, long r5) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r0 = this;
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.onTimeStopped(long, long, long):void");
                }

                public void detach() {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r1 = this;
                    r0 = r1.mBsi;
                    r0 = r0.mOnBatteryTimeBase;
                    r0.remove(r1);
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.detach():void");
                }

                public void readFromParcelLocked(android.os.Parcel r7) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r6 = this;
                    r1 = 1;
                    r2 = 0;
                    r4 = r7.readLong();
                    r6.mStartTime = r4;
                    r4 = r7.readLong();
                    r6.mRunningSince = r4;
                    r0 = r7.readInt();
                    if (r0 == 0) goto L_0x0064;
                L_0x0014:
                    r0 = r1;
                L_0x0015:
                    r6.mRunning = r0;
                    r0 = r7.readInt();
                    r6.mStarts = r0;
                    r4 = r7.readLong();
                    r6.mLaunchedTime = r4;
                    r4 = r7.readLong();
                    r6.mLaunchedSince = r4;
                    r0 = r7.readInt();
                    if (r0 == 0) goto L_0x0066;
                L_0x002f:
                    r6.mLaunched = r1;
                    r0 = r7.readInt();
                    r6.mLaunches = r0;
                    r0 = r7.readLong();
                    r6.mLoadedStartTime = r0;
                    r0 = r7.readInt();
                    r6.mLoadedStarts = r0;
                    r0 = r7.readInt();
                    r6.mLoadedLaunches = r0;
                    r0 = 0;
                    r6.mLastStartTime = r0;
                    r6.mLastStarts = r2;
                    r6.mLastLaunches = r2;
                    r0 = r7.readLong();
                    r6.mUnpluggedStartTime = r0;
                    r0 = r7.readInt();
                    r6.mUnpluggedStarts = r0;
                    r0 = r7.readInt();
                    r6.mUnpluggedLaunches = r0;
                    return;
                L_0x0064:
                    r0 = r2;
                    goto L_0x0015;
                L_0x0066:
                    r1 = r2;
                    goto L_0x002f;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.readFromParcelLocked(android.os.Parcel):void");
                }

                public void writeToParcelLocked(android.os.Parcel r7) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r6 = this;
                    r1 = 1;
                    r2 = 0;
                    r4 = r6.mStartTime;
                    r7.writeLong(r4);
                    r4 = r6.mRunningSince;
                    r7.writeLong(r4);
                    r0 = r6.mRunning;
                    if (r0 == 0) goto L_0x004e;
                L_0x0010:
                    r0 = r1;
                L_0x0011:
                    r7.writeInt(r0);
                    r0 = r6.mStarts;
                    r7.writeInt(r0);
                    r4 = r6.mLaunchedTime;
                    r7.writeLong(r4);
                    r4 = r6.mLaunchedSince;
                    r7.writeLong(r4);
                    r0 = r6.mLaunched;
                    if (r0 == 0) goto L_0x0050;
                L_0x0027:
                    r7.writeInt(r1);
                    r0 = r6.mLaunches;
                    r7.writeInt(r0);
                    r0 = r6.mLoadedStartTime;
                    r7.writeLong(r0);
                    r0 = r6.mLoadedStarts;
                    r7.writeInt(r0);
                    r0 = r6.mLoadedLaunches;
                    r7.writeInt(r0);
                    r0 = r6.mUnpluggedStartTime;
                    r7.writeLong(r0);
                    r0 = r6.mUnpluggedStarts;
                    r7.writeInt(r0);
                    r0 = r6.mUnpluggedLaunches;
                    r7.writeInt(r0);
                    return;
                L_0x004e:
                    r0 = r2;
                    goto L_0x0011;
                L_0x0050:
                    r1 = r2;
                    goto L_0x0027;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.writeToParcelLocked(android.os.Parcel):void");
                }

                public long getLaunchTimeToNowLocked(long r6) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r5 = this;
                    r0 = r5.mLaunched;
                    if (r0 != 0) goto L_0x0007;
                L_0x0004:
                    r0 = r5.mLaunchedTime;
                    return r0;
                L_0x0007:
                    r0 = r5.mLaunchedTime;
                    r0 = r0 + r6;
                    r2 = r5.mLaunchedSince;
                    r0 = r0 - r2;
                    return r0;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.getLaunchTimeToNowLocked(long):long");
                }

                public long getStartTimeToNowLocked(long r6) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r5 = this;
                    r0 = r5.mRunning;
                    if (r0 != 0) goto L_0x0007;
                L_0x0004:
                    r0 = r5.mStartTime;
                    return r0;
                L_0x0007:
                    r0 = r5.mStartTime;
                    r0 = r0 + r6;
                    r2 = r5.mRunningSince;
                    r0 = r0 - r2;
                    return r0;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.getStartTimeToNowLocked(long):long");
                }

                public void startLaunchedLocked() {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r2 = this;
                    r0 = r2.mLaunched;
                    if (r0 != 0) goto L_0x0015;
                L_0x0004:
                    r0 = r2.mLaunches;
                    r0 = r0 + 1;
                    r2.mLaunches = r0;
                    r0 = r2.mBsi;
                    r0 = r0.getBatteryUptimeLocked();
                    r2.mLaunchedSince = r0;
                    r0 = 1;
                    r2.mLaunched = r0;
                L_0x0015:
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.startLaunchedLocked():void");
                }

                public void stopLaunchedLocked() {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r6 = this;
                    r2 = r6.mLaunched;
                    if (r2 == 0) goto L_0x001c;
                L_0x0004:
                    r2 = r6.mBsi;
                    r2 = r2.getBatteryUptimeLocked();
                    r4 = r6.mLaunchedSince;
                    r0 = r2 - r4;
                    r2 = 0;
                    r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
                    if (r2 <= 0) goto L_0x001d;
                L_0x0014:
                    r2 = r6.mLaunchedTime;
                    r2 = r2 + r0;
                    r6.mLaunchedTime = r2;
                L_0x0019:
                    r2 = 0;
                    r6.mLaunched = r2;
                L_0x001c:
                    return;
                L_0x001d:
                    r2 = r6.mLaunches;
                    r2 = r2 + -1;
                    r6.mLaunches = r2;
                    goto L_0x0019;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.stopLaunchedLocked():void");
                }

                public void startRunningLocked() {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r2 = this;
                    r0 = r2.mRunning;
                    if (r0 != 0) goto L_0x0015;
                L_0x0004:
                    r0 = r2.mStarts;
                    r0 = r0 + 1;
                    r2.mStarts = r0;
                    r0 = r2.mBsi;
                    r0 = r0.getBatteryUptimeLocked();
                    r2.mRunningSince = r0;
                    r0 = 1;
                    r2.mRunning = r0;
                L_0x0015:
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.startRunningLocked():void");
                }

                public void stopRunningLocked() {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r6 = this;
                    r2 = r6.mRunning;
                    if (r2 == 0) goto L_0x001c;
                L_0x0004:
                    r2 = r6.mBsi;
                    r2 = r2.getBatteryUptimeLocked();
                    r4 = r6.mRunningSince;
                    r0 = r2 - r4;
                    r2 = 0;
                    r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
                    if (r2 <= 0) goto L_0x001d;
                L_0x0014:
                    r2 = r6.mStartTime;
                    r2 = r2 + r0;
                    r6.mStartTime = r2;
                L_0x0019:
                    r2 = 0;
                    r6.mRunning = r2;
                L_0x001c:
                    return;
                L_0x001d:
                    r2 = r6.mStarts;
                    r2 = r2 + -1;
                    r6.mStarts = r2;
                    goto L_0x0019;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.stopRunningLocked():void");
                }

                public com.android.internal.os.BatteryStatsImpl getBatteryStats() {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r1 = this;
                    r0 = r1.mBsi;
                    return r0;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.getBatteryStats():com.android.internal.os.BatteryStatsImpl");
                }

                public int getLaunches(int r3) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r2 = this;
                    r0 = r2.mLaunches;
                    r1 = 1;
                    if (r3 != r1) goto L_0x0009;
                L_0x0005:
                    r1 = r2.mLoadedLaunches;
                    r0 = r0 - r1;
                L_0x0008:
                    return r0;
                L_0x0009:
                    r1 = 2;
                    if (r3 != r1) goto L_0x0008;
                L_0x000c:
                    r1 = r2.mUnpluggedLaunches;
                    r0 = r0 - r1;
                    goto L_0x0008;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.getLaunches(int):int");
                }

                public long getStartTime(long r6, int r8) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r5 = this;
                    r0 = r5.getStartTimeToNowLocked(r6);
                    r2 = 1;
                    if (r8 != r2) goto L_0x000b;
                L_0x0007:
                    r2 = r5.mLoadedStartTime;
                    r0 = r0 - r2;
                L_0x000a:
                    return r0;
                L_0x000b:
                    r2 = 2;
                    if (r8 != r2) goto L_0x000a;
                L_0x000e:
                    r2 = r5.mUnpluggedStartTime;
                    r0 = r0 - r2;
                    goto L_0x000a;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.getStartTime(long, int):long");
                }

                public int getStarts(int r3) {
                    /* JADX: method processing error */
/*
                    Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                    /*
                    r2 = this;
                    r0 = r2.mStarts;
                    r1 = 1;
                    if (r3 != r1) goto L_0x0009;
                L_0x0005:
                    r1 = r2.mLoadedStarts;
                    r0 = r0 - r1;
                L_0x0008:
                    return r0;
                L_0x0009:
                    r1 = 2;
                    if (r3 != r1) goto L_0x0008;
                L_0x000c:
                    r1 = r2.mUnpluggedStarts;
                    r0 = r0 - r1;
                    goto L_0x0008;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv.getStarts(int):int");
                }
            }

            public Pkg(com.android.internal.os.BatteryStatsImpl r2) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r1.<init>();
                r0 = new android.util.ArrayMap;
                r0.<init>();
                r1.mWakeupAlarms = r0;
                r0 = new android.util.ArrayMap;
                r0.<init>();
                r1.mServiceStats = r0;
                r1.mBsi = r2;
                r0 = r1.mBsi;
                r0 = r0.mOnBatteryScreenOffTimeBase;
                r0.add(r1);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.<init>(com.android.internal.os.BatteryStatsImpl):void");
            }

            public void onTimeStarted(long r1, long r3, long r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.onTimeStarted(long, long, long):void");
            }

            public void onTimeStopped(long r1, long r3, long r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.onTimeStopped(long, long, long):void");
            }

            void detach() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mBsi;
                r0 = r0.mOnBatteryScreenOffTimeBase;
                r0.remove(r1);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.detach():void");
            }

            void readFromParcelLocked(android.os.Parcel r11) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r10 = this;
                r3 = r11.readInt();
                r7 = r10.mWakeupAlarms;
                r7.clear();
                r0 = 0;
            L_0x000a:
                if (r0 >= r3) goto L_0x0021;
            L_0x000c:
                r6 = r11.readString();
                r7 = r10.mWakeupAlarms;
                r8 = new com.android.internal.os.BatteryStatsImpl$Counter;
                r9 = r10.mBsi;
                r9 = r9.mOnBatteryTimeBase;
                r8.<init>(r9, r11);
                r7.put(r6, r8);
                r0 = r0 + 1;
                goto L_0x000a;
            L_0x0021:
                r2 = r11.readInt();
                r7 = r10.mServiceStats;
                r7.clear();
                r1 = 0;
            L_0x002b:
                if (r1 >= r2) goto L_0x0043;
            L_0x002d:
                r5 = r11.readString();
                r4 = new com.android.internal.os.BatteryStatsImpl$Uid$Pkg$Serv;
                r7 = r10.mBsi;
                r4.<init>(r7);
                r7 = r10.mServiceStats;
                r7.put(r5, r4);
                r4.readFromParcelLocked(r11);
                r1 = r1 + 1;
                goto L_0x002b;
            L_0x0043:
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.readFromParcelLocked(android.os.Parcel):void");
            }

            void writeToParcelLocked(android.os.Parcel r6) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r5 = this;
                r4 = r5.mWakeupAlarms;
                r2 = r4.size();
                r6.writeInt(r2);
                r1 = 0;
            L_0x000a:
                if (r1 >= r2) goto L_0x0025;
            L_0x000c:
                r4 = r5.mWakeupAlarms;
                r4 = r4.keyAt(r1);
                r4 = (java.lang.String) r4;
                r6.writeString(r4);
                r4 = r5.mWakeupAlarms;
                r4 = r4.valueAt(r1);
                r4 = (com.android.internal.os.BatteryStatsImpl.Counter) r4;
                r4.writeToParcel(r6);
                r1 = r1 + 1;
                goto L_0x000a;
            L_0x0025:
                r4 = r5.mServiceStats;
                r0 = r4.size();
                r6.writeInt(r0);
                r1 = 0;
            L_0x002f:
                if (r1 >= r0) goto L_0x004a;
            L_0x0031:
                r4 = r5.mServiceStats;
                r4 = r4.keyAt(r1);
                r4 = (java.lang.String) r4;
                r6.writeString(r4);
                r4 = r5.mServiceStats;
                r3 = r4.valueAt(r1);
                r3 = (com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv) r3;
                r3.writeToParcelLocked(r6);
                r1 = r1 + 1;
                goto L_0x002f;
            L_0x004a:
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.writeToParcelLocked(android.os.Parcel):void");
            }

            public android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats.Counter> getWakeupAlarmStats() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mWakeupAlarms;
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.getWakeupAlarmStats():android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats$Counter>");
            }

            public void noteWakeupAlarmLocked(java.lang.String r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r1 = r2.mWakeupAlarms;
                r0 = r1.get(r3);
                r0 = (com.android.internal.os.BatteryStatsImpl.Counter) r0;
                if (r0 != 0) goto L_0x0018;
            L_0x000a:
                r0 = new com.android.internal.os.BatteryStatsImpl$Counter;
                r1 = r2.mBsi;
                r1 = r1.mOnBatteryTimeBase;
                r0.<init>(r1);
                r1 = r2.mWakeupAlarms;
                r1.put(r3, r0);
            L_0x0018:
                r0.stepAtomic();
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.noteWakeupAlarmLocked(java.lang.String):void");
            }

            public android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats.Uid.Pkg.Serv> getServiceStats() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mServiceStats;
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.getServiceStats():android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats$Uid$Pkg$Serv>");
            }

            final com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv newServiceStatsLocked() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = new com.android.internal.os.BatteryStatsImpl$Uid$Pkg$Serv;
                r1 = r2.mBsi;
                r0.<init>(r1);
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Pkg.newServiceStatsLocked():com.android.internal.os.BatteryStatsImpl$Uid$Pkg$Serv");
            }
        }

        public static class Proc extends android.os.BatteryStats.Uid.Proc implements TimeBaseObs {
            boolean mActive;
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

            public Proc(com.android.internal.os.BatteryStatsImpl r2, java.lang.String r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r1.<init>();
                r0 = 1;
                r1.mActive = r0;
                r1.mBsi = r2;
                r1.mName = r3;
                r0 = r1.mBsi;
                r0 = r0.mOnBatteryTimeBase;
                r0.add(r1);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.<init>(com.android.internal.os.BatteryStatsImpl, java.lang.String):void");
            }

            public void onTimeStarted(long r3, long r5, long r7) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mUserTime;
                r2.mUnpluggedUserTime = r0;
                r0 = r2.mSystemTime;
                r2.mUnpluggedSystemTime = r0;
                r0 = r2.mForegroundTime;
                r2.mUnpluggedForegroundTime = r0;
                r0 = r2.mStarts;
                r2.mUnpluggedStarts = r0;
                r0 = r2.mNumCrashes;
                r2.mUnpluggedNumCrashes = r0;
                r0 = r2.mNumAnrs;
                r2.mUnpluggedNumAnrs = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.onTimeStarted(long, long, long):void");
            }

            public void onTimeStopped(long r1, long r3, long r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.onTimeStopped(long, long, long):void");
            }

            void detach() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = 0;
                r1.mActive = r0;
                r0 = r1.mBsi;
                r0 = r0.mOnBatteryTimeBase;
                r0.remove(r1);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.detach():void");
            }

            public int countExcessivePowers() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mExcessivePower;
                if (r0 == 0) goto L_0x000b;
            L_0x0004:
                r0 = r1.mExcessivePower;
                r0 = r0.size();
            L_0x000a:
                return r0;
            L_0x000b:
                r0 = 0;
                goto L_0x000a;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.countExcessivePowers():int");
            }

            public android.os.BatteryStats.Uid.Proc.ExcessivePower getExcessivePower(int r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r1 = 0;
                r0 = r2.mExcessivePower;
                if (r0 == 0) goto L_0x000e;
            L_0x0005:
                r0 = r2.mExcessivePower;
                r0 = r0.get(r3);
                r0 = (android.os.BatteryStats.Uid.Proc.ExcessivePower) r0;
                return r0;
            L_0x000e:
                return r1;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.getExcessivePower(int):android.os.BatteryStats$Uid$Proc$ExcessivePower");
            }

            public void addExcessiveWake(long r4, long r6) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r3 = this;
                r1 = r3.mExcessivePower;
                if (r1 != 0) goto L_0x000b;
            L_0x0004:
                r1 = new java.util.ArrayList;
                r1.<init>();
                r3.mExcessivePower = r1;
            L_0x000b:
                r0 = new android.os.BatteryStats$Uid$Proc$ExcessivePower;
                r0.<init>();
                r1 = 1;
                r0.type = r1;
                r0.overTime = r4;
                r0.usedTime = r6;
                r1 = r3.mExcessivePower;
                r1.add(r0);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.addExcessiveWake(long, long):void");
            }

            public void addExcessiveCpu(long r4, long r6) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r3 = this;
                r1 = r3.mExcessivePower;
                if (r1 != 0) goto L_0x000b;
            L_0x0004:
                r1 = new java.util.ArrayList;
                r1.<init>();
                r3.mExcessivePower = r1;
            L_0x000b:
                r0 = new android.os.BatteryStats$Uid$Proc$ExcessivePower;
                r0.<init>();
                r1 = 2;
                r0.type = r1;
                r0.overTime = r4;
                r0.usedTime = r6;
                r1 = r3.mExcessivePower;
                r1.add(r0);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.addExcessiveCpu(long, long):void");
            }

            void writeExcessivePowerToParcelLocked(android.os.Parcel r7) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r6 = this;
                r3 = r6.mExcessivePower;
                if (r3 != 0) goto L_0x0009;
            L_0x0004:
                r3 = 0;
                r7.writeInt(r3);
                return;
            L_0x0009:
                r3 = r6.mExcessivePower;
                r0 = r3.size();
                r7.writeInt(r0);
                r2 = 0;
            L_0x0013:
                if (r2 >= r0) goto L_0x002f;
            L_0x0015:
                r3 = r6.mExcessivePower;
                r1 = r3.get(r2);
                r1 = (android.os.BatteryStats.Uid.Proc.ExcessivePower) r1;
                r3 = r1.type;
                r7.writeInt(r3);
                r4 = r1.overTime;
                r7.writeLong(r4);
                r4 = r1.usedTime;
                r7.writeLong(r4);
                r2 = r2 + 1;
                goto L_0x0013;
            L_0x002f:
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.writeExcessivePowerToParcelLocked(android.os.Parcel):void");
            }

            void readExcessivePowerFromParcelLocked(android.os.Parcel r7) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r6 = this;
                r0 = r7.readInt();
                if (r0 != 0) goto L_0x000a;
            L_0x0006:
                r3 = 0;
                r6.mExcessivePower = r3;
                return;
            L_0x000a:
                r3 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
                if (r0 <= r3) goto L_0x0028;
            L_0x000e:
                r3 = new android.os.ParcelFormatException;
                r4 = new java.lang.StringBuilder;
                r4.<init>();
                r5 = "File corrupt: too many excessive power entries ";
                r4 = r4.append(r5);
                r4 = r4.append(r0);
                r4 = r4.toString();
                r3.<init>(r4);
                throw r3;
            L_0x0028:
                r3 = new java.util.ArrayList;
                r3.<init>();
                r6.mExcessivePower = r3;
                r2 = 0;
            L_0x0030:
                if (r2 >= r0) goto L_0x0051;
            L_0x0032:
                r1 = new android.os.BatteryStats$Uid$Proc$ExcessivePower;
                r1.<init>();
                r3 = r7.readInt();
                r1.type = r3;
                r4 = r7.readLong();
                r1.overTime = r4;
                r4 = r7.readLong();
                r1.usedTime = r4;
                r3 = r6.mExcessivePower;
                r3.add(r1);
                r2 = r2 + 1;
                goto L_0x0030;
            L_0x0051:
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.readExcessivePowerFromParcelLocked(android.os.Parcel):void");
            }

            void writeToParcelLocked(android.os.Parcel r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mUserTime;
                r3.writeLong(r0);
                r0 = r2.mSystemTime;
                r3.writeLong(r0);
                r0 = r2.mForegroundTime;
                r3.writeLong(r0);
                r0 = r2.mStarts;
                r3.writeInt(r0);
                r0 = r2.mNumCrashes;
                r3.writeInt(r0);
                r0 = r2.mNumAnrs;
                r3.writeInt(r0);
                r0 = r2.mLoadedUserTime;
                r3.writeLong(r0);
                r0 = r2.mLoadedSystemTime;
                r3.writeLong(r0);
                r0 = r2.mLoadedForegroundTime;
                r3.writeLong(r0);
                r0 = r2.mLoadedStarts;
                r3.writeInt(r0);
                r0 = r2.mLoadedNumCrashes;
                r3.writeInt(r0);
                r0 = r2.mLoadedNumAnrs;
                r3.writeInt(r0);
                r0 = r2.mUnpluggedUserTime;
                r3.writeLong(r0);
                r0 = r2.mUnpluggedSystemTime;
                r3.writeLong(r0);
                r0 = r2.mUnpluggedForegroundTime;
                r3.writeLong(r0);
                r0 = r2.mUnpluggedStarts;
                r3.writeInt(r0);
                r0 = r2.mUnpluggedNumCrashes;
                r3.writeInt(r0);
                r0 = r2.mUnpluggedNumAnrs;
                r3.writeInt(r0);
                r2.writeExcessivePowerToParcelLocked(r3);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.writeToParcelLocked(android.os.Parcel):void");
            }

            void readFromParcelLocked(android.os.Parcel r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r3.readLong();
                r2.mUserTime = r0;
                r0 = r3.readLong();
                r2.mSystemTime = r0;
                r0 = r3.readLong();
                r2.mForegroundTime = r0;
                r0 = r3.readInt();
                r2.mStarts = r0;
                r0 = r3.readInt();
                r2.mNumCrashes = r0;
                r0 = r3.readInt();
                r2.mNumAnrs = r0;
                r0 = r3.readLong();
                r2.mLoadedUserTime = r0;
                r0 = r3.readLong();
                r2.mLoadedSystemTime = r0;
                r0 = r3.readLong();
                r2.mLoadedForegroundTime = r0;
                r0 = r3.readInt();
                r2.mLoadedStarts = r0;
                r0 = r3.readInt();
                r2.mLoadedNumCrashes = r0;
                r0 = r3.readInt();
                r2.mLoadedNumAnrs = r0;
                r0 = r3.readLong();
                r2.mUnpluggedUserTime = r0;
                r0 = r3.readLong();
                r2.mUnpluggedSystemTime = r0;
                r0 = r3.readLong();
                r2.mUnpluggedForegroundTime = r0;
                r0 = r3.readInt();
                r2.mUnpluggedStarts = r0;
                r0 = r3.readInt();
                r2.mUnpluggedNumCrashes = r0;
                r0 = r3.readInt();
                r2.mUnpluggedNumAnrs = r0;
                r2.readExcessivePowerFromParcelLocked(r3);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.readFromParcelLocked(android.os.Parcel):void");
            }

            public void addCpuTimeLocked(int r5, int r6) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r4 = this;
                r0 = r4.mUserTime;
                r2 = (long) r5;
                r0 = r0 + r2;
                r4.mUserTime = r0;
                r0 = r4.mSystemTime;
                r2 = (long) r6;
                r0 = r0 + r2;
                r4.mSystemTime = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.addCpuTimeLocked(int, int):void");
            }

            public void addForegroundTimeLocked(long r4) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r3 = this;
                r0 = r3.mForegroundTime;
                r0 = r0 + r4;
                r3.mForegroundTime = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.addForegroundTimeLocked(long):void");
            }

            public void incStartsLocked() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mStarts;
                r0 = r0 + 1;
                r1.mStarts = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.incStartsLocked():void");
            }

            public void incNumCrashesLocked() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mNumCrashes;
                r0 = r0 + 1;
                r1.mNumCrashes = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.incNumCrashesLocked():void");
            }

            public void incNumAnrsLocked() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mNumAnrs;
                r0 = r0 + 1;
                r1.mNumAnrs = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.incNumAnrsLocked():void");
            }

            public boolean isActive() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mActive;
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.isActive():boolean");
            }

            public long getUserTime(int r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r4 = this;
                r0 = r4.mUserTime;
                r2 = 1;
                if (r5 != r2) goto L_0x0009;
            L_0x0005:
                r2 = r4.mLoadedUserTime;
                r0 = r0 - r2;
            L_0x0008:
                return r0;
            L_0x0009:
                r2 = 2;
                if (r5 != r2) goto L_0x0008;
            L_0x000c:
                r2 = r4.mUnpluggedUserTime;
                r0 = r0 - r2;
                goto L_0x0008;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.getUserTime(int):long");
            }

            public long getSystemTime(int r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r4 = this;
                r0 = r4.mSystemTime;
                r2 = 1;
                if (r5 != r2) goto L_0x0009;
            L_0x0005:
                r2 = r4.mLoadedSystemTime;
                r0 = r0 - r2;
            L_0x0008:
                return r0;
            L_0x0009:
                r2 = 2;
                if (r5 != r2) goto L_0x0008;
            L_0x000c:
                r2 = r4.mUnpluggedSystemTime;
                r0 = r0 - r2;
                goto L_0x0008;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.getSystemTime(int):long");
            }

            public long getForegroundTime(int r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r4 = this;
                r0 = r4.mForegroundTime;
                r2 = 1;
                if (r5 != r2) goto L_0x0009;
            L_0x0005:
                r2 = r4.mLoadedForegroundTime;
                r0 = r0 - r2;
            L_0x0008:
                return r0;
            L_0x0009:
                r2 = 2;
                if (r5 != r2) goto L_0x0008;
            L_0x000c:
                r2 = r4.mUnpluggedForegroundTime;
                r0 = r0 - r2;
                goto L_0x0008;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.getForegroundTime(int):long");
            }

            public int getStarts(int r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mStarts;
                r1 = 1;
                if (r3 != r1) goto L_0x0009;
            L_0x0005:
                r1 = r2.mLoadedStarts;
                r0 = r0 - r1;
            L_0x0008:
                return r0;
            L_0x0009:
                r1 = 2;
                if (r3 != r1) goto L_0x0008;
            L_0x000c:
                r1 = r2.mUnpluggedStarts;
                r0 = r0 - r1;
                goto L_0x0008;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.getStarts(int):int");
            }

            public int getNumCrashes(int r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mNumCrashes;
                r1 = 1;
                if (r3 != r1) goto L_0x0009;
            L_0x0005:
                r1 = r2.mLoadedNumCrashes;
                r0 = r0 - r1;
            L_0x0008:
                return r0;
            L_0x0009:
                r1 = 2;
                if (r3 != r1) goto L_0x0008;
            L_0x000c:
                r1 = r2.mUnpluggedNumCrashes;
                r0 = r0 - r1;
                goto L_0x0008;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.getNumCrashes(int):int");
            }

            public int getNumAnrs(int r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mNumAnrs;
                r1 = 1;
                if (r3 != r1) goto L_0x0009;
            L_0x0005:
                r1 = r2.mLoadedNumAnrs;
                r0 = r0 - r1;
            L_0x0008:
                return r0;
            L_0x0009:
                r1 = 2;
                if (r3 != r1) goto L_0x0008;
            L_0x000c:
                r1 = r2.mUnpluggedNumAnrs;
                r0 = r0 - r1;
                goto L_0x0008;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Proc.getNumAnrs(int):int");
            }
        }

        public static class Sensor extends android.os.BatteryStats.Uid.Sensor {
            protected BatteryStatsImpl mBsi;
            final int mHandle;
            StopwatchTimer mTimer;
            protected Uid mUid;

            public Sensor(com.android.internal.os.BatteryStatsImpl r1, com.android.internal.os.BatteryStatsImpl.Uid r2, int r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                r0.<init>();
                r0.mBsi = r1;
                r0.mUid = r2;
                r0.mHandle = r3;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Sensor.<init>(com.android.internal.os.BatteryStatsImpl, com.android.internal.os.BatteryStatsImpl$Uid, int):void");
            }

            private com.android.internal.os.BatteryStatsImpl.StopwatchTimer readTimerFromParcel(com.android.internal.os.BatteryStatsImpl.TimeBase r8, android.os.Parcel r9) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r7 = this;
                r1 = 0;
                r3 = 0;
                r0 = r9.readInt();
                if (r0 != 0) goto L_0x0009;
            L_0x0008:
                return r1;
            L_0x0009:
                r0 = r7.mBsi;
                r0 = r0.mSensorTimers;
                r1 = r7.mHandle;
                r4 = r0.get(r1);
                r4 = (java.util.ArrayList) r4;
                if (r4 != 0) goto L_0x0025;
            L_0x0017:
                r4 = new java.util.ArrayList;
                r4.<init>();
                r0 = r7.mBsi;
                r0 = r0.mSensorTimers;
                r1 = r7.mHandle;
                r0.put(r1, r4);
            L_0x0025:
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = r7.mBsi;
                r1 = r1.mClocks;
                r2 = r7.mUid;
                r5 = r8;
                r6 = r9;
                r0.<init>(r1, r2, r3, r4, r5, r6);
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Sensor.readTimerFromParcel(com.android.internal.os.BatteryStatsImpl$TimeBase, android.os.Parcel):com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
            }

            boolean reset() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r1 = 1;
                r0 = r2.mTimer;
                r0 = r0.reset(r1);
                if (r0 == 0) goto L_0x000d;
            L_0x0009:
                r0 = 0;
                r2.mTimer = r0;
                return r1;
            L_0x000d:
                r0 = 0;
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Sensor.reset():boolean");
            }

            void readFromParcelLocked(com.android.internal.os.BatteryStatsImpl.TimeBase r2, android.os.Parcel r3) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.readTimerFromParcel(r2, r3);
                r1.mTimer = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Sensor.readFromParcelLocked(com.android.internal.os.BatteryStatsImpl$TimeBase, android.os.Parcel):void");
            }

            void writeToParcelLocked(android.os.Parcel r3, long r4) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mTimer;
                com.android.internal.os.BatteryStatsImpl.Timer.writeTimerToParcel(r3, r0, r4);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Sensor.writeToParcelLocked(android.os.Parcel, long):void");
            }

            public com.android.internal.os.BatteryStatsImpl.Timer getSensorTime() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mTimer;
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Sensor.getSensorTime():com.android.internal.os.BatteryStatsImpl$Timer");
            }

            public int getHandle() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r1 = this;
                r0 = r1.mHandle;
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Sensor.getHandle():int");
            }
        }

        public static class Wakelock extends android.os.BatteryStats.Uid.Wakelock {
            protected BatteryStatsImpl mBsi;
            StopwatchTimer mTimerDraw;
            StopwatchTimer mTimerFull;
            StopwatchTimer mTimerPartial;
            StopwatchTimer mTimerWindow;
            protected Uid mUid;

            public Wakelock(com.android.internal.os.BatteryStatsImpl r1, com.android.internal.os.BatteryStatsImpl.Uid r2) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r0 = this;
                r0.<init>();
                r0.mBsi = r1;
                r0.mUid = r2;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Wakelock.<init>(com.android.internal.os.BatteryStatsImpl, com.android.internal.os.BatteryStatsImpl$Uid):void");
            }

            private com.android.internal.os.BatteryStatsImpl.StopwatchTimer readTimerFromParcel(int r8, java.util.ArrayList<com.android.internal.os.BatteryStatsImpl.StopwatchTimer> r9, com.android.internal.os.BatteryStatsImpl.TimeBase r10, android.os.Parcel r11) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r7 = this;
                r0 = r11.readInt();
                if (r0 != 0) goto L_0x0008;
            L_0x0006:
                r0 = 0;
                return r0;
            L_0x0008:
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = r7.mBsi;
                r1 = r1.mClocks;
                r2 = r7.mUid;
                r3 = r8;
                r4 = r9;
                r5 = r10;
                r6 = r11;
                r0.<init>(r1, r2, r3, r4, r5, r6);
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Wakelock.readTimerFromParcel(int, java.util.ArrayList, com.android.internal.os.BatteryStatsImpl$TimeBase, android.os.Parcel):com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
            }

            boolean reset() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r5 = this;
                r2 = 1;
                r1 = 0;
                r4 = 0;
                r0 = 0;
                r3 = r5.mTimerFull;
                if (r3 == 0) goto L_0x0011;
            L_0x0008:
                r3 = r5.mTimerFull;
                r3 = r3.reset(r1);
                if (r3 == 0) goto L_0x006c;
            L_0x0010:
                r0 = r1;
            L_0x0011:
                r3 = r5.mTimerPartial;
                if (r3 == 0) goto L_0x001f;
            L_0x0015:
                r3 = r5.mTimerPartial;
                r3 = r3.reset(r1);
                if (r3 == 0) goto L_0x006e;
            L_0x001d:
                r3 = r1;
            L_0x001e:
                r0 = r0 | r3;
            L_0x001f:
                r3 = r5.mTimerWindow;
                if (r3 == 0) goto L_0x002d;
            L_0x0023:
                r3 = r5.mTimerWindow;
                r3 = r3.reset(r1);
                if (r3 == 0) goto L_0x0070;
            L_0x002b:
                r3 = r1;
            L_0x002c:
                r0 = r0 | r3;
            L_0x002d:
                r3 = r5.mTimerDraw;
                if (r3 == 0) goto L_0x003b;
            L_0x0031:
                r3 = r5.mTimerDraw;
                r3 = r3.reset(r1);
                if (r3 == 0) goto L_0x0072;
            L_0x0039:
                r3 = r1;
            L_0x003a:
                r0 = r0 | r3;
            L_0x003b:
                if (r0 != 0) goto L_0x0069;
            L_0x003d:
                r3 = r5.mTimerFull;
                if (r3 == 0) goto L_0x0048;
            L_0x0041:
                r3 = r5.mTimerFull;
                r3.detach();
                r5.mTimerFull = r4;
            L_0x0048:
                r3 = r5.mTimerPartial;
                if (r3 == 0) goto L_0x0053;
            L_0x004c:
                r3 = r5.mTimerPartial;
                r3.detach();
                r5.mTimerPartial = r4;
            L_0x0053:
                r3 = r5.mTimerWindow;
                if (r3 == 0) goto L_0x005e;
            L_0x0057:
                r3 = r5.mTimerWindow;
                r3.detach();
                r5.mTimerWindow = r4;
            L_0x005e:
                r3 = r5.mTimerDraw;
                if (r3 == 0) goto L_0x0069;
            L_0x0062:
                r3 = r5.mTimerDraw;
                r3.detach();
                r5.mTimerDraw = r4;
            L_0x0069:
                if (r0 == 0) goto L_0x0074;
            L_0x006b:
                return r1;
            L_0x006c:
                r0 = r2;
                goto L_0x0011;
            L_0x006e:
                r3 = r2;
                goto L_0x001e;
            L_0x0070:
                r3 = r2;
                goto L_0x002c;
            L_0x0072:
                r3 = r2;
                goto L_0x003a;
            L_0x0074:
                r1 = r2;
                goto L_0x006b;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Wakelock.reset():boolean");
            }

            void readFromParcelLocked(com.android.internal.os.BatteryStatsImpl.TimeBase r3, com.android.internal.os.BatteryStatsImpl.TimeBase r4, android.os.Parcel r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mBsi;
                r0 = r0.mPartialTimers;
                r1 = 0;
                r0 = r2.readTimerFromParcel(r1, r0, r4, r5);
                r2.mTimerPartial = r0;
                r0 = r2.mBsi;
                r0 = r0.mFullTimers;
                r1 = 1;
                r0 = r2.readTimerFromParcel(r1, r0, r3, r5);
                r2.mTimerFull = r0;
                r0 = r2.mBsi;
                r0 = r0.mWindowTimers;
                r1 = 2;
                r0 = r2.readTimerFromParcel(r1, r0, r3, r5);
                r2.mTimerWindow = r0;
                r0 = r2.mBsi;
                r0 = r0.mDrawTimers;
                r1 = 18;
                r0 = r2.readTimerFromParcel(r1, r0, r3, r5);
                r2.mTimerDraw = r0;
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Wakelock.readFromParcelLocked(com.android.internal.os.BatteryStatsImpl$TimeBase, com.android.internal.os.BatteryStatsImpl$TimeBase, android.os.Parcel):void");
            }

            void writeToParcelLocked(android.os.Parcel r3, long r4) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r2 = this;
                r0 = r2.mTimerPartial;
                com.android.internal.os.BatteryStatsImpl.Timer.writeTimerToParcel(r3, r0, r4);
                r0 = r2.mTimerFull;
                com.android.internal.os.BatteryStatsImpl.Timer.writeTimerToParcel(r3, r0, r4);
                r0 = r2.mTimerWindow;
                com.android.internal.os.BatteryStatsImpl.Timer.writeTimerToParcel(r3, r0, r4);
                r0 = r2.mTimerDraw;
                com.android.internal.os.BatteryStatsImpl.Timer.writeTimerToParcel(r3, r0, r4);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Wakelock.writeToParcelLocked(android.os.Parcel, long):void");
            }

            public com.android.internal.os.BatteryStatsImpl.Timer getWakeTime(int r4) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r3 = this;
                switch(r4) {
                    case 0: goto L_0x0020;
                    case 1: goto L_0x001d;
                    case 2: goto L_0x0023;
                    case 18: goto L_0x0026;
                    default: goto L_0x0003;
                };
            L_0x0003:
                r0 = new java.lang.IllegalArgumentException;
                r1 = new java.lang.StringBuilder;
                r1.<init>();
                r2 = "type = ";
                r1 = r1.append(r2);
                r1 = r1.append(r4);
                r1 = r1.toString();
                r0.<init>(r1);
                throw r0;
            L_0x001d:
                r0 = r3.mTimerFull;
                return r0;
            L_0x0020:
                r0 = r3.mTimerPartial;
                return r0;
            L_0x0023:
                r0 = r3.mTimerWindow;
                return r0;
            L_0x0026:
                r0 = r3.mTimerDraw;
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Wakelock.getWakeTime(int):com.android.internal.os.BatteryStatsImpl$Timer");
            }

            public com.android.internal.os.BatteryStatsImpl.StopwatchTimer getStopwatchTimer(int r7) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r6 = this;
                switch(r7) {
                    case 0: goto L_0x001d;
                    case 1: goto L_0x0038;
                    case 2: goto L_0x0053;
                    case 18: goto L_0x006e;
                    default: goto L_0x0003;
                };
            L_0x0003:
                r1 = new java.lang.IllegalArgumentException;
                r2 = new java.lang.StringBuilder;
                r2.<init>();
                r3 = "type=";
                r2 = r2.append(r3);
                r2 = r2.append(r7);
                r2 = r2.toString();
                r1.<init>(r2);
                throw r1;
            L_0x001d:
                r0 = r6.mTimerPartial;
                if (r0 != 0) goto L_0x0037;
            L_0x0021:
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = r6.mBsi;
                r1 = r1.mClocks;
                r2 = r6.mUid;
                r3 = r6.mBsi;
                r4 = r3.mPartialTimers;
                r3 = r6.mBsi;
                r5 = r3.mOnBatteryScreenOffTimeBase;
                r3 = 0;
                r0.<init>(r1, r2, r3, r4, r5);
                r6.mTimerPartial = r0;
            L_0x0037:
                return r0;
            L_0x0038:
                r0 = r6.mTimerFull;
                if (r0 != 0) goto L_0x0052;
            L_0x003c:
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = r6.mBsi;
                r1 = r1.mClocks;
                r2 = r6.mUid;
                r3 = r6.mBsi;
                r4 = r3.mFullTimers;
                r3 = r6.mBsi;
                r5 = r3.mOnBatteryTimeBase;
                r3 = 1;
                r0.<init>(r1, r2, r3, r4, r5);
                r6.mTimerFull = r0;
            L_0x0052:
                return r0;
            L_0x0053:
                r0 = r6.mTimerWindow;
                if (r0 != 0) goto L_0x006d;
            L_0x0057:
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = r6.mBsi;
                r1 = r1.mClocks;
                r2 = r6.mUid;
                r3 = r6.mBsi;
                r4 = r3.mWindowTimers;
                r3 = r6.mBsi;
                r5 = r3.mOnBatteryTimeBase;
                r3 = 2;
                r0.<init>(r1, r2, r3, r4, r5);
                r6.mTimerWindow = r0;
            L_0x006d:
                return r0;
            L_0x006e:
                r0 = r6.mTimerDraw;
                if (r0 != 0) goto L_0x0089;
            L_0x0072:
                r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
                r1 = r6.mBsi;
                r1 = r1.mClocks;
                r2 = r6.mUid;
                r3 = r6.mBsi;
                r4 = r3.mDrawTimers;
                r3 = r6.mBsi;
                r5 = r3.mOnBatteryTimeBase;
                r3 = 18;
                r0.<init>(r1, r2, r3, r4, r5);
                r6.mTimerDraw = r0;
            L_0x0089:
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.Wakelock.getStopwatchTimer(int):com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
            }
        }

        public Uid(com.android.internal.os.BatteryStatsImpl r9, int r10) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r8 = this;
            r7 = 6;
            r6 = 5;
            r0 = -1;
            r8.<init>();
            r8.mWifiBatchedScanBinStarted = r0;
            r8.mProcessState = r0;
            r0 = new android.util.SparseArray;
            r0.<init>();
            r8.mSensorStats = r0;
            r0 = new android.util.ArrayMap;
            r0.<init>();
            r8.mProcessStats = r0;
            r0 = new android.util.ArrayMap;
            r0.<init>();
            r8.mPackageStats = r0;
            r0 = new android.util.SparseArray;
            r0.<init>();
            r8.mPids = r0;
            r8.mBsi = r9;
            r8.mUid = r10;
            r0 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r1 = r8.mBsi;
            r1 = r1.mOnBatteryTimeBase;
            r0.<init>(r1);
            r8.mUserCpuTime = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r1 = r8.mBsi;
            r1 = r1.mOnBatteryTimeBase;
            r0.<init>(r1);
            r8.mSystemCpuTime = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r1 = r8.mBsi;
            r1 = r1.mOnBatteryTimeBase;
            r0.<init>(r1);
            r8.mCpuPower = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$Uid$1;
            r1 = r8.mBsi;
            r1.getClass();
            r0.<init>(r1);
            r8.mWakelockStats = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$Uid$2;
            r1 = r8.mBsi;
            r1.getClass();
            r0.<init>(r1);
            r8.mSyncStats = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$Uid$3;
            r1 = r8.mBsi;
            r1.getClass();
            r0.<init>(r1);
            r8.mJobStats = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r4 = r2.mWifiRunningTimers;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 4;
            r2 = r8;
            r0.<init>(r1, r2, r3, r4, r5);
            r8.mWifiRunningTimer = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r4 = r2.mFullWifiLockTimers;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r2 = r8;
            r3 = r6;
            r0.<init>(r1, r2, r3, r4, r5);
            r8.mFullWifiLockTimer = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r4 = r2.mWifiScanTimers;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r2 = r8;
            r3 = r7;
            r0.<init>(r1, r2, r3, r4, r5);
            r8.mWifiScanTimer = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl.StopwatchTimer[r6];
            r8.mWifiBatchedScanTimer = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r4 = r2.mWifiMulticastTimers;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 7;
            r2 = r8;
            r0.<init>(r1, r2, r3, r4, r5);
            r8.mWifiMulticastTimer = r0;
            r0 = new com.android.internal.os.BatteryStatsImpl.StopwatchTimer[r7];
            r8.mProcessStateTimer = r0;
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.<init>(com.android.internal.os.BatteryStatsImpl, int):void");
        }

        public android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats.Uid.Wakelock> getWakelockStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mWakelockStats;
            r0 = r0.getMap();
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWakelockStats():android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats$Uid$Wakelock>");
        }

        public android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats.Timer> getSyncStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mSyncStats;
            r0 = r0.getMap();
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getSyncStats():android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats$Timer>");
        }

        public android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats.Timer> getJobStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mJobStats;
            r0 = r0.getMap();
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getJobStats():android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats$Timer>");
        }

        public android.util.SparseArray<? extends android.os.BatteryStats.Uid.Sensor> getSensorStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mSensorStats;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getSensorStats():android.util.SparseArray<? extends android.os.BatteryStats$Uid$Sensor>");
        }

        public android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats.Uid.Proc> getProcessStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mProcessStats;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getProcessStats():android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats$Uid$Proc>");
        }

        public android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats.Uid.Pkg> getPackageStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mPackageStats;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getPackageStats():android.util.ArrayMap<java.lang.String, ? extends android.os.BatteryStats$Uid$Pkg>");
        }

        public int getUid() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mUid;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getUid():int");
        }

        public void noteWifiRunningLocked(long r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r7 = this;
            r0 = r7.mWifiRunning;
            if (r0 != 0) goto L_0x0025;
        L_0x0004:
            r0 = 1;
            r7.mWifiRunning = r0;
            r0 = r7.mWifiRunningTimer;
            if (r0 != 0) goto L_0x0020;
        L_0x000b:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r7.mBsi;
            r1 = r1.mClocks;
            r2 = r7.mBsi;
            r4 = r2.mWifiRunningTimers;
            r2 = r7.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 4;
            r2 = r7;
            r0.<init>(r1, r2, r3, r4, r5);
            r7.mWifiRunningTimer = r0;
        L_0x0020:
            r0 = r7.mWifiRunningTimer;
            r0.startRunningLocked(r8);
        L_0x0025:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiRunningLocked(long):void");
        }

        public void noteWifiStoppedLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mWifiRunning;
            if (r0 == 0) goto L_0x000c;
        L_0x0004:
            r0 = 0;
            r1.mWifiRunning = r0;
            r0 = r1.mWifiRunningTimer;
            r0.stopRunningLocked(r2);
        L_0x000c:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiStoppedLocked(long):void");
        }

        public void noteFullWifiLockAcquiredLocked(long r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r7 = this;
            r0 = r7.mFullWifiLockOut;
            if (r0 != 0) goto L_0x0025;
        L_0x0004:
            r0 = 1;
            r7.mFullWifiLockOut = r0;
            r0 = r7.mFullWifiLockTimer;
            if (r0 != 0) goto L_0x0020;
        L_0x000b:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r7.mBsi;
            r1 = r1.mClocks;
            r2 = r7.mBsi;
            r4 = r2.mFullWifiLockTimers;
            r2 = r7.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 5;
            r2 = r7;
            r0.<init>(r1, r2, r3, r4, r5);
            r7.mFullWifiLockTimer = r0;
        L_0x0020:
            r0 = r7.mFullWifiLockTimer;
            r0.startRunningLocked(r8);
        L_0x0025:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteFullWifiLockAcquiredLocked(long):void");
        }

        public void noteFullWifiLockReleasedLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mFullWifiLockOut;
            if (r0 == 0) goto L_0x000c;
        L_0x0004:
            r0 = 0;
            r1.mFullWifiLockOut = r0;
            r0 = r1.mFullWifiLockTimer;
            r0.stopRunningLocked(r2);
        L_0x000c:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteFullWifiLockReleasedLocked(long):void");
        }

        public void noteWifiScanStartedLocked(long r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r7 = this;
            r0 = r7.mWifiScanStarted;
            if (r0 != 0) goto L_0x0025;
        L_0x0004:
            r0 = 1;
            r7.mWifiScanStarted = r0;
            r0 = r7.mWifiScanTimer;
            if (r0 != 0) goto L_0x0020;
        L_0x000b:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r7.mBsi;
            r1 = r1.mClocks;
            r2 = r7.mBsi;
            r4 = r2.mWifiScanTimers;
            r2 = r7.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 6;
            r2 = r7;
            r0.<init>(r1, r2, r3, r4, r5);
            r7.mWifiScanTimer = r0;
        L_0x0020:
            r0 = r7.mWifiScanTimer;
            r0.startRunningLocked(r8);
        L_0x0025:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiScanStartedLocked(long):void");
        }

        public void noteWifiScanStoppedLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mWifiScanStarted;
            if (r0 == 0) goto L_0x000c;
        L_0x0004:
            r0 = 0;
            r1.mWifiScanStarted = r0;
            r0 = r1.mWifiScanTimer;
            r0.stopRunningLocked(r2);
        L_0x000c:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiScanStoppedLocked(long):void");
        }

        public void noteWifiBatchedScanStartedLocked(int r5, long r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r4 = this;
            r3 = 0;
            r0 = 0;
        L_0x0002:
            r1 = 8;
            if (r5 <= r1) goto L_0x000e;
        L_0x0006:
            r1 = 4;
            if (r0 >= r1) goto L_0x000e;
        L_0x0009:
            r5 = r5 >> 3;
            r0 = r0 + 1;
            goto L_0x0002;
        L_0x000e:
            r1 = r4.mWifiBatchedScanBinStarted;
            if (r1 != r0) goto L_0x0013;
        L_0x0012:
            return;
        L_0x0013:
            r1 = r4.mWifiBatchedScanBinStarted;
            r2 = -1;
            if (r1 == r2) goto L_0x0021;
        L_0x0018:
            r1 = r4.mWifiBatchedScanTimer;
            r2 = r4.mWifiBatchedScanBinStarted;
            r1 = r1[r2];
            r1.stopRunningLocked(r6);
        L_0x0021:
            r4.mWifiBatchedScanBinStarted = r0;
            r1 = r4.mWifiBatchedScanTimer;
            r1 = r1[r0];
            if (r1 != 0) goto L_0x002c;
        L_0x0029:
            r4.makeWifiBatchedScanBin(r0, r3);
        L_0x002c:
            r1 = r4.mWifiBatchedScanTimer;
            r1 = r1[r0];
            r1.startRunningLocked(r6);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiBatchedScanStartedLocked(int, long):void");
        }

        public void noteWifiBatchedScanStoppedLocked(long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r2 = -1;
            r0 = r3.mWifiBatchedScanBinStarted;
            if (r0 == r2) goto L_0x0010;
        L_0x0005:
            r0 = r3.mWifiBatchedScanTimer;
            r1 = r3.mWifiBatchedScanBinStarted;
            r0 = r0[r1];
            r0.stopRunningLocked(r4);
            r3.mWifiBatchedScanBinStarted = r2;
        L_0x0010:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiBatchedScanStoppedLocked(long):void");
        }

        public void noteWifiMulticastEnabledLocked(long r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r7 = this;
            r0 = r7.mWifiMulticastEnabled;
            if (r0 != 0) goto L_0x0025;
        L_0x0004:
            r0 = 1;
            r7.mWifiMulticastEnabled = r0;
            r0 = r7.mWifiMulticastTimer;
            if (r0 != 0) goto L_0x0020;
        L_0x000b:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r7.mBsi;
            r1 = r1.mClocks;
            r2 = r7.mBsi;
            r4 = r2.mWifiMulticastTimers;
            r2 = r7.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 7;
            r2 = r7;
            r0.<init>(r1, r2, r3, r4, r5);
            r7.mWifiMulticastTimer = r0;
        L_0x0020:
            r0 = r7.mWifiMulticastTimer;
            r0.startRunningLocked(r8);
        L_0x0025:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiMulticastEnabledLocked(long):void");
        }

        public void noteWifiMulticastDisabledLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mWifiMulticastEnabled;
            if (r0 == 0) goto L_0x000c;
        L_0x0004:
            r0 = 0;
            r1.mWifiMulticastEnabled = r0;
            r0 = r1.mWifiMulticastTimer;
            r0.stopRunningLocked(r2);
        L_0x000c:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteWifiMulticastDisabledLocked(long):void");
        }

        public android.os.BatteryStats.ControllerActivityCounter getWifiControllerActivity() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mWifiControllerActivity;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWifiControllerActivity():android.os.BatteryStats$ControllerActivityCounter");
        }

        public android.os.BatteryStats.ControllerActivityCounter getBluetoothControllerActivity() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mBluetoothControllerActivity;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getBluetoothControllerActivity():android.os.BatteryStats$ControllerActivityCounter");
        }

        public android.os.BatteryStats.ControllerActivityCounter getModemControllerActivity() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mModemControllerActivity;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getModemControllerActivity():android.os.BatteryStats$ControllerActivityCounter");
        }

        public com.android.internal.os.BatteryStatsImpl.ControllerActivityCounterImpl getOrCreateWifiControllerActivityLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mWifiControllerActivity;
            if (r0 != 0) goto L_0x0010;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl;
            r1 = r3.mBsi;
            r1 = r1.mOnBatteryTimeBase;
            r2 = 1;
            r0.<init>(r1, r2);
            r3.mWifiControllerActivity = r0;
        L_0x0010:
            r0 = r3.mWifiControllerActivity;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getOrCreateWifiControllerActivityLocked():com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl");
        }

        public com.android.internal.os.BatteryStatsImpl.ControllerActivityCounterImpl getOrCreateBluetoothControllerActivityLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mBluetoothControllerActivity;
            if (r0 != 0) goto L_0x0010;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl;
            r1 = r3.mBsi;
            r1 = r1.mOnBatteryTimeBase;
            r2 = 1;
            r0.<init>(r1, r2);
            r3.mBluetoothControllerActivity = r0;
        L_0x0010:
            r0 = r3.mBluetoothControllerActivity;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getOrCreateBluetoothControllerActivityLocked():com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl");
        }

        public com.android.internal.os.BatteryStatsImpl.ControllerActivityCounterImpl getOrCreateModemControllerActivityLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mModemControllerActivity;
            if (r0 != 0) goto L_0x0010;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl;
            r1 = r3.mBsi;
            r1 = r1.mOnBatteryTimeBase;
            r2 = 5;
            r0.<init>(r1, r2);
            r3.mModemControllerActivity = r0;
        L_0x0010:
            r0 = r3.mModemControllerActivity;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getOrCreateModemControllerActivityLocked():com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl");
        }

        public com.android.internal.os.BatteryStatsImpl.StopwatchTimer createAudioTurnedOnTimerLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r6 = this;
            r0 = r6.mAudioTurnedOnTimer;
            if (r0 != 0) goto L_0x001a;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r6.mBsi;
            r1 = r1.mClocks;
            r2 = r6.mBsi;
            r4 = r2.mAudioTurnedOnTimers;
            r2 = r6.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 15;
            r2 = r6;
            r0.<init>(r1, r2, r3, r4, r5);
            r6.mAudioTurnedOnTimer = r0;
        L_0x001a:
            r0 = r6.mAudioTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.createAudioTurnedOnTimerLocked():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
        }

        public void noteAudioTurnedOnLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.createAudioTurnedOnTimerLocked();
            r0.startRunningLocked(r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteAudioTurnedOnLocked(long):void");
        }

        public void noteAudioTurnedOffLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mAudioTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mAudioTurnedOnTimer;
            r0.stopRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteAudioTurnedOffLocked(long):void");
        }

        public void noteResetAudioLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mAudioTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mAudioTurnedOnTimer;
            r0.stopAllRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteResetAudioLocked(long):void");
        }

        public com.android.internal.os.BatteryStatsImpl.StopwatchTimer createVideoTurnedOnTimerLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r6 = this;
            r0 = r6.mVideoTurnedOnTimer;
            if (r0 != 0) goto L_0x001a;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r6.mBsi;
            r1 = r1.mClocks;
            r2 = r6.mBsi;
            r4 = r2.mVideoTurnedOnTimers;
            r2 = r6.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 8;
            r2 = r6;
            r0.<init>(r1, r2, r3, r4, r5);
            r6.mVideoTurnedOnTimer = r0;
        L_0x001a:
            r0 = r6.mVideoTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.createVideoTurnedOnTimerLocked():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
        }

        public void noteVideoTurnedOnLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.createVideoTurnedOnTimerLocked();
            r0.startRunningLocked(r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteVideoTurnedOnLocked(long):void");
        }

        public void noteVideoTurnedOffLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mVideoTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mVideoTurnedOnTimer;
            r0.stopRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteVideoTurnedOffLocked(long):void");
        }

        public void noteResetVideoLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mVideoTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mVideoTurnedOnTimer;
            r0.stopAllRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteResetVideoLocked(long):void");
        }

        public com.android.internal.os.BatteryStatsImpl.StopwatchTimer createFlashlightTurnedOnTimerLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r6 = this;
            r0 = r6.mFlashlightTurnedOnTimer;
            if (r0 != 0) goto L_0x001a;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r6.mBsi;
            r1 = r1.mClocks;
            r2 = r6.mBsi;
            r4 = r2.mFlashlightTurnedOnTimers;
            r2 = r6.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 16;
            r2 = r6;
            r0.<init>(r1, r2, r3, r4, r5);
            r6.mFlashlightTurnedOnTimer = r0;
        L_0x001a:
            r0 = r6.mFlashlightTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.createFlashlightTurnedOnTimerLocked():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
        }

        public void noteFlashlightTurnedOnLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.createFlashlightTurnedOnTimerLocked();
            r0.startRunningLocked(r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteFlashlightTurnedOnLocked(long):void");
        }

        public void noteFlashlightTurnedOffLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mFlashlightTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mFlashlightTurnedOnTimer;
            r0.stopRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteFlashlightTurnedOffLocked(long):void");
        }

        public void noteResetFlashlightLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mFlashlightTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mFlashlightTurnedOnTimer;
            r0.stopAllRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteResetFlashlightLocked(long):void");
        }

        public com.android.internal.os.BatteryStatsImpl.StopwatchTimer createCameraTurnedOnTimerLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r6 = this;
            r0 = r6.mCameraTurnedOnTimer;
            if (r0 != 0) goto L_0x001a;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r6.mBsi;
            r1 = r1.mClocks;
            r2 = r6.mBsi;
            r4 = r2.mCameraTurnedOnTimers;
            r2 = r6.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 17;
            r2 = r6;
            r0.<init>(r1, r2, r3, r4, r5);
            r6.mCameraTurnedOnTimer = r0;
        L_0x001a:
            r0 = r6.mCameraTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.createCameraTurnedOnTimerLocked():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
        }

        public void noteCameraTurnedOnLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.createCameraTurnedOnTimerLocked();
            r0.startRunningLocked(r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteCameraTurnedOnLocked(long):void");
        }

        public void noteCameraTurnedOffLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mCameraTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mCameraTurnedOnTimer;
            r0.stopRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteCameraTurnedOffLocked(long):void");
        }

        public void noteResetCameraLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mCameraTurnedOnTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mCameraTurnedOnTimer;
            r0.stopAllRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteResetCameraLocked(long):void");
        }

        public com.android.internal.os.BatteryStatsImpl.StopwatchTimer createForegroundActivityTimerLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r6 = this;
            r4 = 0;
            r0 = r6.mForegroundActivityTimer;
            if (r0 != 0) goto L_0x0017;
        L_0x0005:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r6.mBsi;
            r1 = r1.mClocks;
            r2 = r6.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 10;
            r2 = r6;
            r0.<init>(r1, r2, r3, r4, r5);
            r6.mForegroundActivityTimer = r0;
        L_0x0017:
            r0 = r6.mForegroundActivityTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.createForegroundActivityTimerLocked():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
        }

        public com.android.internal.os.BatteryStatsImpl.StopwatchTimer createBluetoothScanTimerLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r6 = this;
            r0 = r6.mBluetoothScanTimer;
            if (r0 != 0) goto L_0x001a;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r6.mBsi;
            r1 = r1.mClocks;
            r2 = r6.mBsi;
            r4 = r2.mBluetoothScanOnTimers;
            r2 = r6.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 19;
            r2 = r6;
            r0.<init>(r1, r2, r3, r4, r5);
            r6.mBluetoothScanTimer = r0;
        L_0x001a:
            r0 = r6.mBluetoothScanTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.createBluetoothScanTimerLocked():com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
        }

        public void noteBluetoothScanStartedLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.createBluetoothScanTimerLocked();
            r0.startRunningLocked(r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteBluetoothScanStartedLocked(long):void");
        }

        public void noteBluetoothScanStoppedLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mBluetoothScanTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mBluetoothScanTimer;
            r0.stopRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteBluetoothScanStoppedLocked(long):void");
        }

        public void noteResetBluetoothScanLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mBluetoothScanTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mBluetoothScanTimer;
            r0.stopAllRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteResetBluetoothScanLocked(long):void");
        }

        public void noteActivityResumedLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.createForegroundActivityTimerLocked();
            r0.startRunningLocked(r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteActivityResumedLocked(long):void");
        }

        public void noteActivityPausedLocked(long r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mForegroundActivityTimer;
            if (r0 == 0) goto L_0x0009;
        L_0x0004:
            r0 = r1.mForegroundActivityTimer;
            r0.stopRunningLocked(r2);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteActivityPausedLocked(long):void");
        }

        public com.android.internal.os.BatteryStatsImpl.BatchTimer createVibratorOnTimerLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r4 = this;
            r0 = r4.mVibratorOnTimer;
            if (r0 != 0) goto L_0x0015;
        L_0x0004:
            r0 = new com.android.internal.os.BatteryStatsImpl$BatchTimer;
            r1 = r4.mBsi;
            r1 = r1.mClocks;
            r2 = r4.mBsi;
            r2 = r2.mOnBatteryTimeBase;
            r3 = 9;
            r0.<init>(r1, r4, r3, r2);
            r4.mVibratorOnTimer = r0;
        L_0x0015:
            r0 = r4.mVibratorOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.createVibratorOnTimerLocked():com.android.internal.os.BatteryStatsImpl$BatchTimer");
        }

        public void noteVibratorOnLocked(long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.createVibratorOnTimerLocked();
            r1 = r3.mBsi;
            r0.addDuration(r1, r4);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteVibratorOnLocked(long):void");
        }

        public void noteVibratorOffLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mVibratorOnTimer;
            if (r0 == 0) goto L_0x000b;
        L_0x0004:
            r0 = r2.mVibratorOnTimer;
            r1 = r2.mBsi;
            r0.abortLastDuration(r1);
        L_0x000b:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteVibratorOffLocked():void");
        }

        public long getWifiRunningTime(long r4, int r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mWifiRunningTimer;
            if (r0 != 0) goto L_0x0007;
        L_0x0004:
            r0 = 0;
            return r0;
        L_0x0007:
            r0 = r3.mWifiRunningTimer;
            r0 = r0.getTotalTimeLocked(r4, r6);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWifiRunningTime(long, int):long");
        }

        public long getFullWifiLockTime(long r4, int r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mFullWifiLockTimer;
            if (r0 != 0) goto L_0x0007;
        L_0x0004:
            r0 = 0;
            return r0;
        L_0x0007:
            r0 = r3.mFullWifiLockTimer;
            r0 = r0.getTotalTimeLocked(r4, r6);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getFullWifiLockTime(long, int):long");
        }

        public long getWifiScanTime(long r4, int r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mWifiScanTimer;
            if (r0 != 0) goto L_0x0007;
        L_0x0004:
            r0 = 0;
            return r0;
        L_0x0007:
            r0 = r3.mWifiScanTimer;
            r0 = r0.getTotalTimeLocked(r4, r6);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWifiScanTime(long, int):long");
        }

        public int getWifiScanCount(int r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mWifiScanTimer;
            if (r0 != 0) goto L_0x0006;
        L_0x0004:
            r0 = 0;
            return r0;
        L_0x0006:
            r0 = r1.mWifiScanTimer;
            r0 = r0.getCountLocked(r2);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWifiScanCount(int):int");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long getWifiBatchedScanTime(int r5, long r6, int r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r4 = this;
            r2 = 0;
            if (r5 < 0) goto L_0x0007;
        L_0x0004:
            r0 = 5;
            if (r5 < r0) goto L_0x0008;
        L_0x0007:
            return r2;
        L_0x0008:
            r0 = r4.mWifiBatchedScanTimer;
            r0 = r0[r5];
            if (r0 != 0) goto L_0x000f;
        L_0x000e:
            return r2;
        L_0x000f:
            r0 = r4.mWifiBatchedScanTimer;
            r0 = r0[r5];
            r0 = r0.getTotalTimeLocked(r6, r8);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWifiBatchedScanTime(int, long, int):long");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getWifiBatchedScanCount(int r3, int r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = 0;
            if (r3 < 0) goto L_0x0006;
        L_0x0003:
            r0 = 5;
            if (r3 < r0) goto L_0x0007;
        L_0x0006:
            return r1;
        L_0x0007:
            r0 = r2.mWifiBatchedScanTimer;
            r0 = r0[r3];
            if (r0 != 0) goto L_0x000e;
        L_0x000d:
            return r1;
        L_0x000e:
            r0 = r2.mWifiBatchedScanTimer;
            r0 = r0[r3];
            r0 = r0.getCountLocked(r4);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWifiBatchedScanCount(int, int):int");
        }

        public long getWifiMulticastTime(long r4, int r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mWifiMulticastTimer;
            if (r0 != 0) goto L_0x0007;
        L_0x0004:
            r0 = 0;
            return r0;
        L_0x0007:
            r0 = r3.mWifiMulticastTimer;
            r0 = r0.getTotalTimeLocked(r4, r6);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getWifiMulticastTime(long, int):long");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getAudioTurnedOnTimer() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mAudioTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getAudioTurnedOnTimer():com.android.internal.os.BatteryStatsImpl$Timer");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getVideoTurnedOnTimer() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mVideoTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getVideoTurnedOnTimer():com.android.internal.os.BatteryStatsImpl$Timer");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getFlashlightTurnedOnTimer() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mFlashlightTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getFlashlightTurnedOnTimer():com.android.internal.os.BatteryStatsImpl$Timer");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getCameraTurnedOnTimer() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mCameraTurnedOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getCameraTurnedOnTimer():com.android.internal.os.BatteryStatsImpl$Timer");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getForegroundActivityTimer() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mForegroundActivityTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getForegroundActivityTimer():com.android.internal.os.BatteryStatsImpl$Timer");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getBluetoothScanTimer() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mBluetoothScanTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getBluetoothScanTimer():com.android.internal.os.BatteryStatsImpl$Timer");
        }

        void makeProcessState(int r9, android.os.Parcel r10) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r8 = this;
            r3 = 12;
            r4 = 0;
            if (r9 < 0) goto L_0x0008;
        L_0x0005:
            r0 = 6;
            if (r9 < r0) goto L_0x0009;
        L_0x0008:
            return;
        L_0x0009:
            if (r10 != 0) goto L_0x001e;
        L_0x000b:
            r6 = r8.mProcessStateTimer;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r2 = r8;
            r0.<init>(r1, r2, r3, r4, r5);
            r6[r9] = r0;
        L_0x001d:
            return;
        L_0x001e:
            r7 = r8.mProcessStateTimer;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r2 = r8;
            r6 = r10;
            r0.<init>(r1, r2, r3, r4, r5, r6);
            r7[r9] = r0;
            goto L_0x001d;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.makeProcessState(int, android.os.Parcel):void");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long getProcessStateTime(int r5, long r6, int r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r4 = this;
            r2 = 0;
            if (r5 < 0) goto L_0x0007;
        L_0x0004:
            r0 = 6;
            if (r5 < r0) goto L_0x0008;
        L_0x0007:
            return r2;
        L_0x0008:
            r0 = r4.mProcessStateTimer;
            r0 = r0[r5];
            if (r0 != 0) goto L_0x000f;
        L_0x000e:
            return r2;
        L_0x000f:
            r0 = r4.mProcessStateTimer;
            r0 = r0[r5];
            r0 = r0.getTotalTimeLocked(r6, r8);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getProcessStateTime(int, long, int):long");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getProcessStateTimer(int r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            if (r2 < 0) goto L_0x0005;
        L_0x0002:
            r0 = 6;
            if (r2 < r0) goto L_0x0007;
        L_0x0005:
            r0 = 0;
            return r0;
        L_0x0007:
            r0 = r1.mProcessStateTimer;
            r0 = r0[r2];
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getProcessStateTimer(int):com.android.internal.os.BatteryStatsImpl$Timer");
        }

        public com.android.internal.os.BatteryStatsImpl.Timer getVibratorOnTimer() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mVibratorOnTimer;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getVibratorOnTimer():com.android.internal.os.BatteryStatsImpl$Timer");
        }

        public void noteUserActivityLocked(int r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.mUserActivityCounters;
            if (r0 != 0) goto L_0x0007;
        L_0x0004:
            r3.initUserActivityLocked();
        L_0x0007:
            if (r4 < 0) goto L_0x0014;
        L_0x0009:
            r0 = 4;
            if (r4 >= r0) goto L_0x0014;
        L_0x000c:
            r0 = r3.mUserActivityCounters;
            r0 = r0[r4];
            r0.stepAtomic();
        L_0x0013:
            return;
        L_0x0014:
            r0 = "BatteryStatsImpl";
            r1 = new java.lang.StringBuilder;
            r1.<init>();
            r2 = "Unknown user activity type ";
            r1 = r1.append(r2);
            r1 = r1.append(r4);
            r2 = " was specified.";
            r1 = r1.append(r2);
            r1 = r1.toString();
            r2 = new java.lang.Throwable;
            r2.<init>();
            android.util.Slog.w(r0, r1, r2);
            goto L_0x0013;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteUserActivityLocked(int):void");
        }

        public boolean hasUserActivity() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mUserActivityCounters;
            if (r0 == 0) goto L_0x0006;
        L_0x0004:
            r0 = 1;
        L_0x0005:
            return r0;
        L_0x0006:
            r0 = 0;
            goto L_0x0005;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.hasUserActivity():boolean");
        }

        public int getUserActivityCount(int r2, int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mUserActivityCounters;
            if (r0 != 0) goto L_0x0006;
        L_0x0004:
            r0 = 0;
            return r0;
        L_0x0006:
            r0 = r1.mUserActivityCounters;
            r0 = r0[r2];
            r0 = r0.getCountLocked(r3);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getUserActivityCount(int, int):int");
        }

        void makeWifiBatchedScanBin(int r9, android.os.Parcel r10) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r8 = this;
            r3 = 11;
            if (r9 < 0) goto L_0x0007;
        L_0x0004:
            r0 = 5;
            if (r9 < r0) goto L_0x0008;
        L_0x0007:
            return;
        L_0x0008:
            r0 = r8.mBsi;
            r0 = r0.mWifiBatchedScanTimers;
            r4 = r0.get(r9);
            r4 = (java.util.ArrayList) r4;
            if (r4 != 0) goto L_0x0020;
        L_0x0014:
            r4 = new java.util.ArrayList;
            r4.<init>();
            r0 = r8.mBsi;
            r0 = r0.mWifiBatchedScanTimers;
            r0.put(r9, r4);
        L_0x0020:
            if (r10 != 0) goto L_0x0035;
        L_0x0022:
            r6 = r8.mWifiBatchedScanTimer;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r2 = r8;
            r0.<init>(r1, r2, r3, r4, r5);
            r6[r9] = r0;
        L_0x0034:
            return;
        L_0x0035:
            r7 = r8.mWifiBatchedScanTimer;
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r8.mBsi;
            r1 = r1.mClocks;
            r2 = r8.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r2 = r8;
            r6 = r10;
            r0.<init>(r1, r2, r3, r4, r5, r6);
            r7[r9] = r0;
            goto L_0x0034;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.makeWifiBatchedScanBin(int, android.os.Parcel):void");
        }

        void initUserActivityLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r5 = this;
            r4 = 4;
            r1 = new com.android.internal.os.BatteryStatsImpl.Counter[r4];
            r5.mUserActivityCounters = r1;
            r0 = 0;
        L_0x0006:
            if (r0 >= r4) goto L_0x0018;
        L_0x0008:
            r1 = r5.mUserActivityCounters;
            r2 = new com.android.internal.os.BatteryStatsImpl$Counter;
            r3 = r5.mBsi;
            r3 = r3.mOnBatteryTimeBase;
            r2.<init>(r3);
            r1[r0] = r2;
            r0 = r0 + 1;
            goto L_0x0006;
        L_0x0018:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.initUserActivityLocked():void");
        }

        void noteNetworkActivityLocked(int r5, long r6, long r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r4 = this;
            r0 = r4.mNetworkByteActivityCounters;
            if (r0 != 0) goto L_0x0007;
        L_0x0004:
            r4.initNetworkActivityLocked();
        L_0x0007:
            if (r5 < 0) goto L_0x001b;
        L_0x0009:
            r0 = 6;
            if (r5 >= r0) goto L_0x001b;
        L_0x000c:
            r0 = r4.mNetworkByteActivityCounters;
            r0 = r0[r5];
            r0.addCountLocked(r6);
            r0 = r4.mNetworkPacketActivityCounters;
            r0 = r0[r5];
            r0.addCountLocked(r8);
        L_0x001a:
            return;
        L_0x001b:
            r0 = "BatteryStatsImpl";
            r1 = new java.lang.StringBuilder;
            r1.<init>();
            r2 = "Unknown network activity type ";
            r1 = r1.append(r2);
            r1 = r1.append(r5);
            r2 = " was specified.";
            r1 = r1.append(r2);
            r1 = r1.toString();
            r2 = new java.lang.Throwable;
            r2.<init>();
            android.util.Slog.w(r0, r1, r2);
            goto L_0x001a;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteNetworkActivityLocked(int, long, long):void");
        }

        void noteMobileRadioActiveTimeLocked(long r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r5 = this;
            r0 = r5.mNetworkByteActivityCounters;
            if (r0 != 0) goto L_0x0007;
        L_0x0004:
            r5.initNetworkActivityLocked();
        L_0x0007:
            r0 = r5.mMobileRadioActiveTime;
            r0.addCountLocked(r6);
            r0 = r5.mMobileRadioActiveCount;
            r2 = 1;
            r0.addCountLocked(r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteMobileRadioActiveTimeLocked(long):void");
        }

        public boolean hasNetworkActivity() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mNetworkByteActivityCounters;
            if (r0 == 0) goto L_0x0006;
        L_0x0004:
            r0 = 1;
        L_0x0005:
            return r0;
        L_0x0006:
            r0 = 0;
            goto L_0x0005;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.hasNetworkActivity():boolean");
        }

        public long getNetworkActivityBytes(int r3, int r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mNetworkByteActivityCounters;
            if (r0 == 0) goto L_0x0014;
        L_0x0004:
            if (r3 < 0) goto L_0x0014;
        L_0x0006:
            r0 = r2.mNetworkByteActivityCounters;
            r0 = r0.length;
            if (r3 >= r0) goto L_0x0014;
        L_0x000b:
            r0 = r2.mNetworkByteActivityCounters;
            r0 = r0[r3];
            r0 = r0.getCountLocked(r4);
            return r0;
        L_0x0014:
            r0 = 0;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getNetworkActivityBytes(int, int):long");
        }

        public long getNetworkActivityPackets(int r3, int r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mNetworkPacketActivityCounters;
            if (r0 == 0) goto L_0x0014;
        L_0x0004:
            if (r3 < 0) goto L_0x0014;
        L_0x0006:
            r0 = r2.mNetworkPacketActivityCounters;
            r0 = r0.length;
            if (r3 >= r0) goto L_0x0014;
        L_0x000b:
            r0 = r2.mNetworkPacketActivityCounters;
            r0 = r0[r3];
            r0 = r0.getCountLocked(r4);
            return r0;
        L_0x0014:
            r0 = 0;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getNetworkActivityPackets(int, int):long");
        }

        public long getMobileRadioActiveTime(int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mMobileRadioActiveTime;
            if (r0 == 0) goto L_0x000b;
        L_0x0004:
            r0 = r2.mMobileRadioActiveTime;
            r0 = r0.getCountLocked(r3);
        L_0x000a:
            return r0;
        L_0x000b:
            r0 = 0;
            goto L_0x000a;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getMobileRadioActiveTime(int):long");
        }

        public int getMobileRadioActiveCount(int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mMobileRadioActiveCount;
            if (r0 == 0) goto L_0x000c;
        L_0x0004:
            r0 = r2.mMobileRadioActiveCount;
            r0 = r0.getCountLocked(r3);
            r0 = (int) r0;
        L_0x000b:
            return r0;
        L_0x000c:
            r0 = 0;
            goto L_0x000b;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getMobileRadioActiveCount(int):int");
        }

        public long getUserCpuTimeUs(int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mUserCpuTime;
            r0 = r0.getCountLocked(r3);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getUserCpuTimeUs(int):long");
        }

        public long getSystemCpuTimeUs(int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mSystemCpuTime;
            r0 = r0.getCountLocked(r3);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getSystemCpuTimeUs(int):long");
        }

        public long getCpuPowerMaUs(int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.mCpuPower;
            r0 = r0.getCountLocked(r3);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getCpuPowerMaUs(int):long");
        }

        public long getTimeAtCpuSpeed(int r5, int r6, int r7) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r4 = this;
            r2 = r4.mCpuClusterSpeed;
            if (r2 == 0) goto L_0x001f;
        L_0x0004:
            if (r5 < 0) goto L_0x001f;
        L_0x0006:
            r2 = r4.mCpuClusterSpeed;
            r2 = r2.length;
            if (r5 >= r2) goto L_0x001f;
        L_0x000b:
            r2 = r4.mCpuClusterSpeed;
            r1 = r2[r5];
            if (r1 == 0) goto L_0x001f;
        L_0x0011:
            if (r6 < 0) goto L_0x001f;
        L_0x0013:
            r2 = r1.length;
            if (r6 >= r2) goto L_0x001f;
        L_0x0016:
            r0 = r1[r6];
            if (r0 == 0) goto L_0x001f;
        L_0x001a:
            r2 = r0.getCountLocked(r7);
            return r2;
        L_0x001f:
            r2 = 0;
            return r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getTimeAtCpuSpeed(int, int, int):long");
        }

        void initNetworkActivityLocked() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r5 = this;
            r4 = 6;
            r1 = new com.android.internal.os.BatteryStatsImpl.LongSamplingCounter[r4];
            r5.mNetworkByteActivityCounters = r1;
            r1 = new com.android.internal.os.BatteryStatsImpl.LongSamplingCounter[r4];
            r5.mNetworkPacketActivityCounters = r1;
            r0 = 0;
        L_0x000a:
            if (r0 >= r4) goto L_0x0029;
        L_0x000c:
            r1 = r5.mNetworkByteActivityCounters;
            r2 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r3 = r5.mBsi;
            r3 = r3.mOnBatteryTimeBase;
            r2.<init>(r3);
            r1[r0] = r2;
            r1 = r5.mNetworkPacketActivityCounters;
            r2 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r3 = r5.mBsi;
            r3 = r3.mOnBatteryTimeBase;
            r2.<init>(r3);
            r1[r0] = r2;
            r0 = r0 + 1;
            goto L_0x000a;
        L_0x0029:
            r1 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r2 = r5.mBsi;
            r2 = r2.mOnBatteryTimeBase;
            r1.<init>(r2);
            r5.mMobileRadioActiveTime = r1;
            r1 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r2 = r5.mBsi;
            r2 = r2.mOnBatteryTimeBase;
            r1.<init>(r2);
            r5.mMobileRadioActiveCount = r1;
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.initNetworkActivityLocked():void");
        }

        boolean reset() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r34 = this;
            r4 = 0;
            r0 = r34;
            r0 = r0.mWifiRunningTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0020;
        L_0x0009:
            r0 = r34;
            r0 = r0.mWifiRunningTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x0094;
        L_0x0017:
            r4 = 0;
        L_0x0018:
            r0 = r34;
            r0 = r0.mWifiRunning;
            r28 = r0;
            r4 = r4 | r28;
        L_0x0020:
            r0 = r34;
            r0 = r0.mFullWifiLockTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0042;
        L_0x0028:
            r0 = r34;
            r0 = r0.mFullWifiLockTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x0096;
        L_0x0036:
            r28 = 0;
        L_0x0038:
            r4 = r4 | r28;
            r0 = r34;
            r0 = r0.mFullWifiLockOut;
            r28 = r0;
            r4 = r4 | r28;
        L_0x0042:
            r0 = r34;
            r0 = r0.mWifiScanTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0064;
        L_0x004a:
            r0 = r34;
            r0 = r0.mWifiScanTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x0099;
        L_0x0058:
            r28 = 0;
        L_0x005a:
            r4 = r4 | r28;
            r0 = r34;
            r0 = r0.mWifiScanStarted;
            r28 = r0;
            r4 = r4 | r28;
        L_0x0064:
            r0 = r34;
            r0 = r0.mWifiBatchedScanTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x00b1;
        L_0x006c:
            r7 = 0;
        L_0x006d:
            r28 = 5;
            r0 = r28;
            if (r7 >= r0) goto L_0x009f;
        L_0x0073:
            r0 = r34;
            r0 = r0.mWifiBatchedScanTimer;
            r28 = r0;
            r28 = r28[r7];
            if (r28 == 0) goto L_0x0091;
        L_0x007d:
            r0 = r34;
            r0 = r0.mWifiBatchedScanTimer;
            r28 = r0;
            r28 = r28[r7];
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x009c;
        L_0x008d:
            r28 = 0;
        L_0x008f:
            r4 = r4 | r28;
        L_0x0091:
            r7 = r7 + 1;
            goto L_0x006d;
        L_0x0094:
            r4 = 1;
            goto L_0x0018;
        L_0x0096:
            r28 = 1;
            goto L_0x0038;
        L_0x0099:
            r28 = 1;
            goto L_0x005a;
        L_0x009c:
            r28 = 1;
            goto L_0x008f;
        L_0x009f:
            r0 = r34;
            r0 = r0.mWifiBatchedScanBinStarted;
            r28 = r0;
            r29 = -1;
            r0 = r28;
            r1 = r29;
            if (r0 == r1) goto L_0x019f;
        L_0x00ad:
            r28 = 1;
        L_0x00af:
            r4 = r4 | r28;
        L_0x00b1:
            r0 = r34;
            r0 = r0.mWifiMulticastTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x00d3;
        L_0x00b9:
            r0 = r34;
            r0 = r0.mWifiMulticastTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01a3;
        L_0x00c7:
            r28 = 0;
        L_0x00c9:
            r4 = r4 | r28;
            r0 = r34;
            r0 = r0.mWifiMulticastEnabled;
            r28 = r0;
            r4 = r4 | r28;
        L_0x00d3:
            r0 = r34;
            r0 = r0.mAudioTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x00ed;
        L_0x00db:
            r0 = r34;
            r0 = r0.mAudioTurnedOnTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01a7;
        L_0x00e9:
            r28 = 0;
        L_0x00eb:
            r4 = r4 | r28;
        L_0x00ed:
            r0 = r34;
            r0 = r0.mVideoTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0107;
        L_0x00f5:
            r0 = r34;
            r0 = r0.mVideoTurnedOnTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01ab;
        L_0x0103:
            r28 = 0;
        L_0x0105:
            r4 = r4 | r28;
        L_0x0107:
            r0 = r34;
            r0 = r0.mFlashlightTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0121;
        L_0x010f:
            r0 = r34;
            r0 = r0.mFlashlightTurnedOnTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01af;
        L_0x011d:
            r28 = 0;
        L_0x011f:
            r4 = r4 | r28;
        L_0x0121:
            r0 = r34;
            r0 = r0.mCameraTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x013b;
        L_0x0129:
            r0 = r34;
            r0 = r0.mCameraTurnedOnTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01b3;
        L_0x0137:
            r28 = 0;
        L_0x0139:
            r4 = r4 | r28;
        L_0x013b:
            r0 = r34;
            r0 = r0.mForegroundActivityTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0155;
        L_0x0143:
            r0 = r34;
            r0 = r0.mForegroundActivityTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01b6;
        L_0x0151:
            r28 = 0;
        L_0x0153:
            r4 = r4 | r28;
        L_0x0155:
            r0 = r34;
            r0 = r0.mBluetoothScanTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x016f;
        L_0x015d:
            r0 = r34;
            r0 = r0.mBluetoothScanTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01b9;
        L_0x016b:
            r28 = 0;
        L_0x016d:
            r4 = r4 | r28;
        L_0x016f:
            r0 = r34;
            r0 = r0.mProcessStateTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x01d1;
        L_0x0177:
            r7 = 0;
        L_0x0178:
            r28 = 6;
            r0 = r28;
            if (r7 >= r0) goto L_0x01bf;
        L_0x017e:
            r0 = r34;
            r0 = r0.mProcessStateTimer;
            r28 = r0;
            r28 = r28[r7];
            if (r28 == 0) goto L_0x019c;
        L_0x0188:
            r0 = r34;
            r0 = r0.mProcessStateTimer;
            r28 = r0;
            r28 = r28[r7];
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x01bc;
        L_0x0198:
            r28 = 0;
        L_0x019a:
            r4 = r4 | r28;
        L_0x019c:
            r7 = r7 + 1;
            goto L_0x0178;
        L_0x019f:
            r28 = 0;
            goto L_0x00af;
        L_0x01a3:
            r28 = 1;
            goto L_0x00c9;
        L_0x01a7:
            r28 = 1;
            goto L_0x00eb;
        L_0x01ab:
            r28 = 1;
            goto L_0x0105;
        L_0x01af:
            r28 = 1;
            goto L_0x011f;
        L_0x01b3:
            r28 = 1;
            goto L_0x0139;
        L_0x01b6:
            r28 = 1;
            goto L_0x0153;
        L_0x01b9:
            r28 = 1;
            goto L_0x016d;
        L_0x01bc:
            r28 = 1;
            goto L_0x019a;
        L_0x01bf:
            r0 = r34;
            r0 = r0.mProcessState;
            r28 = r0;
            r29 = -1;
            r0 = r28;
            r1 = r29;
            if (r0 == r1) goto L_0x0217;
        L_0x01cd:
            r28 = 1;
        L_0x01cf:
            r4 = r4 | r28;
        L_0x01d1:
            r0 = r34;
            r0 = r0.mVibratorOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x01f8;
        L_0x01d9:
            r0 = r34;
            r0 = r0.mVibratorOnTimer;
            r28 = r0;
            r29 = 0;
            r28 = r28.reset(r29);
            if (r28 == 0) goto L_0x021a;
        L_0x01e7:
            r0 = r34;
            r0 = r0.mVibratorOnTimer;
            r28 = r0;
            r28.detach();
            r28 = 0;
            r0 = r28;
            r1 = r34;
            r1.mVibratorOnTimer = r0;
        L_0x01f8:
            r0 = r34;
            r0 = r0.mUserActivityCounters;
            r28 = r0;
            if (r28 == 0) goto L_0x021c;
        L_0x0200:
            r7 = 0;
        L_0x0201:
            r28 = 4;
            r0 = r28;
            if (r7 >= r0) goto L_0x021c;
        L_0x0207:
            r0 = r34;
            r0 = r0.mUserActivityCounters;
            r28 = r0;
            r28 = r28[r7];
            r29 = 0;
            r28.reset(r29);
            r7 = r7 + 1;
            goto L_0x0201;
        L_0x0217:
            r28 = 0;
            goto L_0x01cf;
        L_0x021a:
            r4 = 1;
            goto L_0x01f8;
        L_0x021c:
            r0 = r34;
            r0 = r0.mNetworkByteActivityCounters;
            r28 = r0;
            if (r28 == 0) goto L_0x025e;
        L_0x0224:
            r7 = 0;
        L_0x0225:
            r28 = 6;
            r0 = r28;
            if (r7 >= r0) goto L_0x0248;
        L_0x022b:
            r0 = r34;
            r0 = r0.mNetworkByteActivityCounters;
            r28 = r0;
            r28 = r28[r7];
            r29 = 0;
            r28.reset(r29);
            r0 = r34;
            r0 = r0.mNetworkPacketActivityCounters;
            r28 = r0;
            r28 = r28[r7];
            r29 = 0;
            r28.reset(r29);
            r7 = r7 + 1;
            goto L_0x0225;
        L_0x0248:
            r0 = r34;
            r0 = r0.mMobileRadioActiveTime;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
            r0 = r34;
            r0 = r0.mMobileRadioActiveCount;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
        L_0x025e:
            r0 = r34;
            r0 = r0.mWifiControllerActivity;
            r28 = r0;
            if (r28 == 0) goto L_0x0271;
        L_0x0266:
            r0 = r34;
            r0 = r0.mWifiControllerActivity;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
        L_0x0271:
            r0 = r34;
            r0 = r0.mBluetoothControllerActivity;
            r28 = r0;
            if (r28 == 0) goto L_0x0284;
        L_0x0279:
            r0 = r34;
            r0 = r0.mBluetoothControllerActivity;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
        L_0x0284:
            r0 = r34;
            r0 = r0.mModemControllerActivity;
            r28 = r0;
            if (r28 == 0) goto L_0x0297;
        L_0x028c:
            r0 = r34;
            r0 = r0.mModemControllerActivity;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
        L_0x0297:
            r0 = r34;
            r0 = r0.mUserCpuTime;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
            r0 = r34;
            r0 = r0.mSystemCpuTime;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
            r0 = r34;
            r0 = r0.mCpuPower;
            r28 = r0;
            r29 = 0;
            r28.reset(r29);
            r0 = r34;
            r0 = r0.mCpuClusterSpeed;
            r28 = r0;
            if (r28 == 0) goto L_0x02fb;
        L_0x02c0:
            r0 = r34;
            r0 = r0.mCpuClusterSpeed;
            r30 = r0;
            r28 = 0;
            r0 = r30;
            r0 = r0.length;
            r31 = r0;
            r29 = r28;
        L_0x02cf:
            r0 = r29;
            r1 = r31;
            if (r0 >= r1) goto L_0x02fb;
        L_0x02d5:
            r23 = r30[r29];
            if (r23 == 0) goto L_0x02f6;
        L_0x02d9:
            r28 = 0;
            r0 = r23;
            r0 = r0.length;
            r32 = r0;
        L_0x02e0:
            r0 = r28;
            r1 = r32;
            if (r0 >= r1) goto L_0x02f6;
        L_0x02e6:
            r22 = r23[r28];
            if (r22 == 0) goto L_0x02f3;
        L_0x02ea:
            r33 = 0;
            r0 = r22;
            r1 = r33;
            r0.reset(r1);
        L_0x02f3:
            r28 = r28 + 1;
            goto L_0x02e0;
        L_0x02f6:
            r28 = r29 + 1;
            r29 = r28;
            goto L_0x02cf;
        L_0x02fb:
            r0 = r34;
            r0 = r0.mWakelockStats;
            r28 = r0;
            r26 = r28.getMap();
            r28 = r26.size();
            r14 = r28 + -1;
        L_0x030b:
            if (r14 < 0) goto L_0x0325;
        L_0x030d:
            r0 = r26;
            r27 = r0.valueAt(r14);
            r27 = (com.android.internal.os.BatteryStatsImpl.Uid.Wakelock) r27;
            r28 = r27.reset();
            if (r28 == 0) goto L_0x0323;
        L_0x031b:
            r0 = r26;
            r0.removeAt(r14);
        L_0x0320:
            r14 = r14 + -1;
            goto L_0x030b;
        L_0x0323:
            r4 = 1;
            goto L_0x0320;
        L_0x0325:
            r0 = r34;
            r0 = r0.mWakelockStats;
            r28 = r0;
            r28.cleanup();
            r0 = r34;
            r0 = r0.mSyncStats;
            r28 = r0;
            r24 = r28.getMap();
            r28 = r24.size();
            r10 = r28 + -1;
        L_0x033e:
            if (r10 < 0) goto L_0x0361;
        L_0x0340:
            r0 = r24;
            r25 = r0.valueAt(r10);
            r25 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r25;
            r28 = 0;
            r0 = r25;
            r1 = r28;
            r28 = r0.reset(r1);
            if (r28 == 0) goto L_0x035f;
        L_0x0354:
            r0 = r24;
            r0.removeAt(r10);
            r25.detach();
        L_0x035c:
            r10 = r10 + -1;
            goto L_0x033e;
        L_0x035f:
            r4 = 1;
            goto L_0x035c;
        L_0x0361:
            r0 = r34;
            r0 = r0.mSyncStats;
            r28 = r0;
            r28.cleanup();
            r0 = r34;
            r0 = r0.mJobStats;
            r28 = r0;
            r15 = r28.getMap();
            r28 = r15.size();
            r8 = r28 + -1;
        L_0x037a:
            if (r8 < 0) goto L_0x0399;
        L_0x037c:
            r25 = r15.valueAt(r8);
            r25 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r25;
            r28 = 0;
            r0 = r25;
            r1 = r28;
            r28 = r0.reset(r1);
            if (r28 == 0) goto L_0x0397;
        L_0x038e:
            r15.removeAt(r8);
            r25.detach();
        L_0x0394:
            r8 = r8 + -1;
            goto L_0x037a;
        L_0x0397:
            r4 = 1;
            goto L_0x0394;
        L_0x0399:
            r0 = r34;
            r0 = r0.mJobStats;
            r28 = r0;
            r28.cleanup();
            r0 = r34;
            r0 = r0.mSensorStats;
            r28 = r0;
            r28 = r28.size();
            r11 = r28 + -1;
        L_0x03ae:
            if (r11 < 0) goto L_0x03d4;
        L_0x03b0:
            r0 = r34;
            r0 = r0.mSensorStats;
            r28 = r0;
            r0 = r28;
            r20 = r0.valueAt(r11);
            r20 = (com.android.internal.os.BatteryStatsImpl.Uid.Sensor) r20;
            r28 = r20.reset();
            if (r28 == 0) goto L_0x03d2;
        L_0x03c4:
            r0 = r34;
            r0 = r0.mSensorStats;
            r28 = r0;
            r0 = r28;
            r0.removeAt(r11);
        L_0x03cf:
            r11 = r11 + -1;
            goto L_0x03ae;
        L_0x03d2:
            r4 = 1;
            goto L_0x03cf;
        L_0x03d4:
            r0 = r34;
            r0 = r0.mProcessStats;
            r28 = r0;
            r28 = r28.size();
            r9 = r28 + -1;
        L_0x03e0:
            if (r9 < 0) goto L_0x03f6;
        L_0x03e2:
            r0 = r34;
            r0 = r0.mProcessStats;
            r28 = r0;
            r0 = r28;
            r19 = r0.valueAt(r9);
            r19 = (com.android.internal.os.BatteryStatsImpl.Uid.Proc) r19;
            r19.detach();
            r9 = r9 + -1;
            goto L_0x03e0;
        L_0x03f6:
            r0 = r34;
            r0 = r0.mProcessStats;
            r28 = r0;
            r28.clear();
            r0 = r34;
            r0 = r0.mPids;
            r28 = r0;
            r28 = r28.size();
            if (r28 <= 0) goto L_0x043f;
        L_0x040b:
            r0 = r34;
            r0 = r0.mPids;
            r28 = r0;
            r28 = r28.size();
            r7 = r28 + -1;
        L_0x0417:
            if (r7 < 0) goto L_0x043f;
        L_0x0419:
            r0 = r34;
            r0 = r0.mPids;
            r28 = r0;
            r0 = r28;
            r17 = r0.valueAt(r7);
            r17 = (android.os.BatteryStats.Uid.Pid) r17;
            r0 = r17;
            r0 = r0.mWakeNesting;
            r28 = r0;
            if (r28 <= 0) goto L_0x0433;
        L_0x042f:
            r4 = 1;
        L_0x0430:
            r7 = r7 + -1;
            goto L_0x0417;
        L_0x0433:
            r0 = r34;
            r0 = r0.mPids;
            r28 = r0;
            r0 = r28;
            r0.removeAt(r7);
            goto L_0x0430;
        L_0x043f:
            r0 = r34;
            r0 = r0.mPackageStats;
            r28 = r0;
            r28 = r28.size();
            if (r28 <= 0) goto L_0x04a7;
        L_0x044b:
            r0 = r34;
            r0 = r0.mPackageStats;
            r28 = r0;
            r28 = r28.entrySet();
            r12 = r28.iterator();
        L_0x0459:
            r28 = r12.hasNext();
            if (r28 == 0) goto L_0x049e;
        L_0x045f:
            r18 = r12.next();
            r18 = (java.util.Map.Entry) r18;
            r16 = r18.getValue();
            r16 = (com.android.internal.os.BatteryStatsImpl.Uid.Pkg) r16;
            r16.detach();
            r0 = r16;
            r0 = r0.mServiceStats;
            r28 = r0;
            r28 = r28.size();
            if (r28 <= 0) goto L_0x0459;
        L_0x047a:
            r0 = r16;
            r0 = r0.mServiceStats;
            r28 = r0;
            r28 = r28.entrySet();
            r13 = r28.iterator();
        L_0x0488:
            r28 = r13.hasNext();
            if (r28 == 0) goto L_0x0459;
        L_0x048e:
            r21 = r13.next();
            r21 = (java.util.Map.Entry) r21;
            r28 = r21.getValue();
            r28 = (com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv) r28;
            r28.detach();
            goto L_0x0488;
        L_0x049e:
            r0 = r34;
            r0 = r0.mPackageStats;
            r28 = r0;
            r28.clear();
        L_0x04a7:
            r28 = 0;
            r0 = r28;
            r2 = r34;
            r2.mLastStepSystemTime = r0;
            r28 = 0;
            r0 = r28;
            r2 = r34;
            r2.mLastStepUserTime = r0;
            r28 = 0;
            r0 = r28;
            r2 = r34;
            r2.mCurStepSystemTime = r0;
            r28 = 0;
            r0 = r28;
            r2 = r34;
            r2.mCurStepUserTime = r0;
            if (r4 != 0) goto L_0x0699;
        L_0x04c9:
            r0 = r34;
            r0 = r0.mWifiRunningTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x04da;
        L_0x04d1:
            r0 = r34;
            r0 = r0.mWifiRunningTimer;
            r28 = r0;
            r28.detach();
        L_0x04da:
            r0 = r34;
            r0 = r0.mFullWifiLockTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x04eb;
        L_0x04e2:
            r0 = r34;
            r0 = r0.mFullWifiLockTimer;
            r28 = r0;
            r28.detach();
        L_0x04eb:
            r0 = r34;
            r0 = r0.mWifiScanTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x04fc;
        L_0x04f3:
            r0 = r34;
            r0 = r0.mWifiScanTimer;
            r28 = r0;
            r28.detach();
        L_0x04fc:
            r7 = 0;
        L_0x04fd:
            r28 = 5;
            r0 = r28;
            if (r7 >= r0) goto L_0x051b;
        L_0x0503:
            r0 = r34;
            r0 = r0.mWifiBatchedScanTimer;
            r28 = r0;
            r28 = r28[r7];
            if (r28 == 0) goto L_0x0518;
        L_0x050d:
            r0 = r34;
            r0 = r0.mWifiBatchedScanTimer;
            r28 = r0;
            r28 = r28[r7];
            r28.detach();
        L_0x0518:
            r7 = r7 + 1;
            goto L_0x04fd;
        L_0x051b:
            r0 = r34;
            r0 = r0.mWifiMulticastTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x052c;
        L_0x0523:
            r0 = r34;
            r0 = r0.mWifiMulticastTimer;
            r28 = r0;
            r28.detach();
        L_0x052c:
            r0 = r34;
            r0 = r0.mAudioTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0545;
        L_0x0534:
            r0 = r34;
            r0 = r0.mAudioTurnedOnTimer;
            r28 = r0;
            r28.detach();
            r28 = 0;
            r0 = r28;
            r1 = r34;
            r1.mAudioTurnedOnTimer = r0;
        L_0x0545:
            r0 = r34;
            r0 = r0.mVideoTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x055e;
        L_0x054d:
            r0 = r34;
            r0 = r0.mVideoTurnedOnTimer;
            r28 = r0;
            r28.detach();
            r28 = 0;
            r0 = r28;
            r1 = r34;
            r1.mVideoTurnedOnTimer = r0;
        L_0x055e:
            r0 = r34;
            r0 = r0.mFlashlightTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0577;
        L_0x0566:
            r0 = r34;
            r0 = r0.mFlashlightTurnedOnTimer;
            r28 = r0;
            r28.detach();
            r28 = 0;
            r0 = r28;
            r1 = r34;
            r1.mFlashlightTurnedOnTimer = r0;
        L_0x0577:
            r0 = r34;
            r0 = r0.mCameraTurnedOnTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x0590;
        L_0x057f:
            r0 = r34;
            r0 = r0.mCameraTurnedOnTimer;
            r28 = r0;
            r28.detach();
            r28 = 0;
            r0 = r28;
            r1 = r34;
            r1.mCameraTurnedOnTimer = r0;
        L_0x0590:
            r0 = r34;
            r0 = r0.mForegroundActivityTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x05a9;
        L_0x0598:
            r0 = r34;
            r0 = r0.mForegroundActivityTimer;
            r28 = r0;
            r28.detach();
            r28 = 0;
            r0 = r28;
            r1 = r34;
            r1.mForegroundActivityTimer = r0;
        L_0x05a9:
            r0 = r34;
            r0 = r0.mBluetoothScanTimer;
            r28 = r0;
            if (r28 == 0) goto L_0x05c2;
        L_0x05b1:
            r0 = r34;
            r0 = r0.mBluetoothScanTimer;
            r28 = r0;
            r28.detach();
            r28 = 0;
            r0 = r28;
            r1 = r34;
            r1.mBluetoothScanTimer = r0;
        L_0x05c2:
            r0 = r34;
            r0 = r0.mUserActivityCounters;
            r28 = r0;
            if (r28 == 0) goto L_0x05df;
        L_0x05ca:
            r7 = 0;
        L_0x05cb:
            r28 = 4;
            r0 = r28;
            if (r7 >= r0) goto L_0x05df;
        L_0x05d1:
            r0 = r34;
            r0 = r0.mUserActivityCounters;
            r28 = r0;
            r28 = r28[r7];
            r28.detach();
            r7 = r7 + 1;
            goto L_0x05cb;
        L_0x05df:
            r0 = r34;
            r0 = r0.mNetworkByteActivityCounters;
            r28 = r0;
            if (r28 == 0) goto L_0x0607;
        L_0x05e7:
            r7 = 0;
        L_0x05e8:
            r28 = 6;
            r0 = r28;
            if (r7 >= r0) goto L_0x0607;
        L_0x05ee:
            r0 = r34;
            r0 = r0.mNetworkByteActivityCounters;
            r28 = r0;
            r28 = r28[r7];
            r28.detach();
            r0 = r34;
            r0 = r0.mNetworkPacketActivityCounters;
            r28 = r0;
            r28 = r28[r7];
            r28.detach();
            r7 = r7 + 1;
            goto L_0x05e8;
        L_0x0607:
            r0 = r34;
            r0 = r0.mWifiControllerActivity;
            r28 = r0;
            if (r28 == 0) goto L_0x0618;
        L_0x060f:
            r0 = r34;
            r0 = r0.mWifiControllerActivity;
            r28 = r0;
            r28.detach();
        L_0x0618:
            r0 = r34;
            r0 = r0.mBluetoothControllerActivity;
            r28 = r0;
            if (r28 == 0) goto L_0x0629;
        L_0x0620:
            r0 = r34;
            r0 = r0.mBluetoothControllerActivity;
            r28 = r0;
            r28.detach();
        L_0x0629:
            r0 = r34;
            r0 = r0.mModemControllerActivity;
            r28 = r0;
            if (r28 == 0) goto L_0x063a;
        L_0x0631:
            r0 = r34;
            r0 = r0.mModemControllerActivity;
            r28 = r0;
            r28.detach();
        L_0x063a:
            r0 = r34;
            r0 = r0.mPids;
            r28 = r0;
            r28.clear();
            r0 = r34;
            r0 = r0.mUserCpuTime;
            r28 = r0;
            r28.detach();
            r0 = r34;
            r0 = r0.mSystemCpuTime;
            r28 = r0;
            r28.detach();
            r0 = r34;
            r0 = r0.mCpuPower;
            r28 = r0;
            r28.detach();
            r0 = r34;
            r0 = r0.mCpuClusterSpeed;
            r28 = r0;
            if (r28 == 0) goto L_0x0699;
        L_0x0666:
            r0 = r34;
            r0 = r0.mCpuClusterSpeed;
            r30 = r0;
            r28 = 0;
            r0 = r30;
            r0 = r0.length;
            r31 = r0;
            r29 = r28;
        L_0x0675:
            r0 = r29;
            r1 = r31;
            if (r0 >= r1) goto L_0x0699;
        L_0x067b:
            r6 = r30[r29];
            if (r6 == 0) goto L_0x0694;
        L_0x067f:
            r28 = 0;
            r0 = r6.length;
            r32 = r0;
        L_0x0684:
            r0 = r28;
            r1 = r32;
            if (r0 >= r1) goto L_0x0694;
        L_0x068a:
            r5 = r6[r28];
            if (r5 == 0) goto L_0x0691;
        L_0x068e:
            r5.detach();
        L_0x0691:
            r28 = r28 + 1;
            goto L_0x0684;
        L_0x0694:
            r28 = r29 + 1;
            r29 = r28;
            goto L_0x0675;
        L_0x0699:
            if (r4 == 0) goto L_0x069e;
        L_0x069b:
            r28 = 0;
        L_0x069d:
            return r28;
        L_0x069e:
            r28 = 1;
            goto L_0x069d;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.reset():boolean");
        }

        void writeToParcelLocked(android.os.Parcel r35, long r36) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r34 = this;
            r0 = r34;
            r0 = r0.mWakelockStats;
            r27 = r0;
            r25 = r27.getMap();
            r8 = r25.size();
            r0 = r35;
            r0.writeInt(r8);
            r16 = 0;
        L_0x0015:
            r0 = r16;
            if (r0 >= r8) goto L_0x0040;
        L_0x0019:
            r0 = r25;
            r1 = r16;
            r27 = r0.keyAt(r1);
            r27 = (java.lang.String) r27;
            r0 = r35;
            r1 = r27;
            r0.writeString(r1);
            r0 = r25;
            r1 = r16;
            r26 = r0.valueAt(r1);
            r26 = (com.android.internal.os.BatteryStatsImpl.Uid.Wakelock) r26;
            r0 = r26;
            r1 = r35;
            r2 = r36;
            r0.writeToParcelLocked(r1, r2);
            r16 = r16 + 1;
            goto L_0x0015;
        L_0x0040:
            r0 = r34;
            r0 = r0.mSyncStats;
            r27 = r0;
            r23 = r27.getMap();
            r6 = r23.size();
            r0 = r35;
            r0.writeInt(r6);
            r14 = 0;
        L_0x0054:
            if (r14 >= r6) goto L_0x0079;
        L_0x0056:
            r0 = r23;
            r27 = r0.keyAt(r14);
            r27 = (java.lang.String) r27;
            r0 = r35;
            r1 = r27;
            r0.writeString(r1);
            r0 = r23;
            r24 = r0.valueAt(r14);
            r24 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r24;
            r0 = r35;
            r1 = r24;
            r2 = r36;
            com.android.internal.os.BatteryStatsImpl.Timer.writeTimerToParcel(r0, r1, r2);
            r14 = r14 + 1;
            goto L_0x0054;
        L_0x0079:
            r0 = r34;
            r0 = r0.mJobStats;
            r27 = r0;
            r17 = r27.getMap();
            r4 = r17.size();
            r0 = r35;
            r0.writeInt(r4);
            r12 = 0;
        L_0x008d:
            if (r12 >= r4) goto L_0x00b2;
        L_0x008f:
            r0 = r17;
            r27 = r0.keyAt(r12);
            r27 = (java.lang.String) r27;
            r0 = r35;
            r1 = r27;
            r0.writeString(r1);
            r0 = r17;
            r24 = r0.valueAt(r12);
            r24 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r24;
            r0 = r35;
            r1 = r24;
            r2 = r36;
            com.android.internal.os.BatteryStatsImpl.Timer.writeTimerToParcel(r0, r1, r2);
            r12 = r12 + 1;
            goto L_0x008d;
        L_0x00b2:
            r0 = r34;
            r0 = r0.mSensorStats;
            r27 = r0;
            r7 = r27.size();
            r0 = r35;
            r0.writeInt(r7);
            r15 = 0;
        L_0x00c2:
            if (r15 >= r7) goto L_0x00f1;
        L_0x00c4:
            r0 = r34;
            r0 = r0.mSensorStats;
            r27 = r0;
            r0 = r27;
            r27 = r0.keyAt(r15);
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mSensorStats;
            r27 = r0;
            r0 = r27;
            r22 = r0.valueAt(r15);
            r22 = (com.android.internal.os.BatteryStatsImpl.Uid.Sensor) r22;
            r0 = r22;
            r1 = r35;
            r2 = r36;
            r0.writeToParcelLocked(r1, r2);
            r15 = r15 + 1;
            goto L_0x00c2;
        L_0x00f1:
            r0 = r34;
            r0 = r0.mProcessStats;
            r27 = r0;
            r5 = r27.size();
            r0 = r35;
            r0.writeInt(r5);
            r13 = 0;
        L_0x0101:
            if (r13 >= r5) goto L_0x0130;
        L_0x0103:
            r0 = r34;
            r0 = r0.mProcessStats;
            r27 = r0;
            r0 = r27;
            r27 = r0.keyAt(r13);
            r27 = (java.lang.String) r27;
            r0 = r35;
            r1 = r27;
            r0.writeString(r1);
            r0 = r34;
            r0 = r0.mProcessStats;
            r27 = r0;
            r0 = r27;
            r21 = r0.valueAt(r13);
            r21 = (com.android.internal.os.BatteryStatsImpl.Uid.Proc) r21;
            r0 = r21;
            r1 = r35;
            r0.writeToParcelLocked(r1);
            r13 = r13 + 1;
            goto L_0x0101;
        L_0x0130:
            r0 = r34;
            r0 = r0.mPackageStats;
            r27 = r0;
            r27 = r27.size();
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mPackageStats;
            r27 = r0;
            r27 = r27.entrySet();
            r20 = r27.iterator();
        L_0x014f:
            r27 = r20.hasNext();
            if (r27 == 0) goto L_0x0176;
        L_0x0155:
            r19 = r20.next();
            r19 = (java.util.Map.Entry) r19;
            r27 = r19.getKey();
            r27 = (java.lang.String) r27;
            r0 = r35;
            r1 = r27;
            r0.writeString(r1);
            r18 = r19.getValue();
            r18 = (com.android.internal.os.BatteryStatsImpl.Uid.Pkg) r18;
            r0 = r18;
            r1 = r35;
            r0.writeToParcelLocked(r1);
            goto L_0x014f;
        L_0x0176:
            r0 = r34;
            r0 = r0.mWifiRunningTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x0204;
        L_0x017e:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mWifiRunningTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x0196:
            r0 = r34;
            r0 = r0.mFullWifiLockTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x020e;
        L_0x019e:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mFullWifiLockTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x01b6:
            r0 = r34;
            r0 = r0.mWifiScanTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x0218;
        L_0x01be:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mWifiScanTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x01d6:
            r11 = 0;
        L_0x01d7:
            r27 = 5;
            r0 = r27;
            if (r11 >= r0) goto L_0x022c;
        L_0x01dd:
            r0 = r34;
            r0 = r0.mWifiBatchedScanTimer;
            r27 = r0;
            r27 = r27[r11];
            if (r27 == 0) goto L_0x0222;
        L_0x01e7:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mWifiBatchedScanTimer;
            r27 = r0;
            r27 = r27[r11];
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x0201:
            r11 = r11 + 1;
            goto L_0x01d7;
        L_0x0204:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x0196;
        L_0x020e:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x01b6;
        L_0x0218:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x01d6;
        L_0x0222:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x0201;
        L_0x022c:
            r0 = r34;
            r0 = r0.mWifiMulticastTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x033a;
        L_0x0234:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mWifiMulticastTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x024c:
            r0 = r34;
            r0 = r0.mAudioTurnedOnTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x0345;
        L_0x0254:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mAudioTurnedOnTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x026c:
            r0 = r34;
            r0 = r0.mVideoTurnedOnTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x0350;
        L_0x0274:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mVideoTurnedOnTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x028c:
            r0 = r34;
            r0 = r0.mFlashlightTurnedOnTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x035b;
        L_0x0294:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mFlashlightTurnedOnTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x02ac:
            r0 = r34;
            r0 = r0.mCameraTurnedOnTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x0366;
        L_0x02b4:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mCameraTurnedOnTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x02cc:
            r0 = r34;
            r0 = r0.mForegroundActivityTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x0371;
        L_0x02d4:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mForegroundActivityTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x02ec:
            r0 = r34;
            r0 = r0.mBluetoothScanTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x037c;
        L_0x02f4:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mBluetoothScanTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x030c:
            r11 = 0;
        L_0x030d:
            r27 = 6;
            r0 = r27;
            if (r11 >= r0) goto L_0x0390;
        L_0x0313:
            r0 = r34;
            r0 = r0.mProcessStateTimer;
            r27 = r0;
            r27 = r27[r11];
            if (r27 == 0) goto L_0x0386;
        L_0x031d:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mProcessStateTimer;
            r27 = r0;
            r27 = r27[r11];
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x0337:
            r11 = r11 + 1;
            goto L_0x030d;
        L_0x033a:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x024c;
        L_0x0345:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x026c;
        L_0x0350:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x028c;
        L_0x035b:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x02ac;
        L_0x0366:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x02cc;
        L_0x0371:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x02ec;
        L_0x037c:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x030c;
        L_0x0386:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x0337;
        L_0x0390:
            r0 = r34;
            r0 = r0.mVibratorOnTimer;
            r27 = r0;
            if (r27 == 0) goto L_0x03da;
        L_0x0398:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mVibratorOnTimer;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r2 = r36;
            r0.writeToParcel(r1, r2);
        L_0x03b0:
            r0 = r34;
            r0 = r0.mUserActivityCounters;
            r27 = r0;
            if (r27 == 0) goto L_0x03e4;
        L_0x03b8:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r11 = 0;
        L_0x03c2:
            r27 = 4;
            r0 = r27;
            if (r11 >= r0) goto L_0x03ed;
        L_0x03c8:
            r0 = r34;
            r0 = r0.mUserActivityCounters;
            r27 = r0;
            r27 = r27[r11];
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
            r11 = r11 + 1;
            goto L_0x03c2;
        L_0x03da:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x03b0;
        L_0x03e4:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
        L_0x03ed:
            r0 = r34;
            r0 = r0.mNetworkByteActivityCounters;
            r27 = r0;
            if (r27 == 0) goto L_0x053c;
        L_0x03f5:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r11 = 0;
        L_0x03ff:
            r27 = 6;
            r0 = r27;
            if (r11 >= r0) goto L_0x0426;
        L_0x0405:
            r0 = r34;
            r0 = r0.mNetworkByteActivityCounters;
            r27 = r0;
            r27 = r27[r11];
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
            r0 = r34;
            r0 = r0.mNetworkPacketActivityCounters;
            r27 = r0;
            r27 = r27[r11];
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
            r11 = r11 + 1;
            goto L_0x03ff;
        L_0x0426:
            r0 = r34;
            r0 = r0.mMobileRadioActiveTime;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
            r0 = r34;
            r0 = r0.mMobileRadioActiveCount;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
        L_0x0440:
            r0 = r34;
            r0 = r0.mWifiControllerActivity;
            r27 = r0;
            if (r27 == 0) goto L_0x0547;
        L_0x0448:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mWifiControllerActivity;
            r27 = r0;
            r28 = 0;
            r0 = r27;
            r1 = r35;
            r2 = r28;
            r0.writeToParcel(r1, r2);
        L_0x0462:
            r0 = r34;
            r0 = r0.mBluetoothControllerActivity;
            r27 = r0;
            if (r27 == 0) goto L_0x0552;
        L_0x046a:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mBluetoothControllerActivity;
            r27 = r0;
            r28 = 0;
            r0 = r27;
            r1 = r35;
            r2 = r28;
            r0.writeToParcel(r1, r2);
        L_0x0484:
            r0 = r34;
            r0 = r0.mModemControllerActivity;
            r27 = r0;
            if (r27 == 0) goto L_0x055d;
        L_0x048c:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mModemControllerActivity;
            r27 = r0;
            r28 = 0;
            r0 = r27;
            r1 = r35;
            r2 = r28;
            r0.writeToParcel(r1, r2);
        L_0x04a6:
            r0 = r34;
            r0 = r0.mUserCpuTime;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
            r0 = r34;
            r0 = r0.mSystemCpuTime;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
            r0 = r34;
            r0 = r0.mCpuPower;
            r27 = r0;
            r0 = r27;
            r1 = r35;
            r0.writeToParcel(r1);
            r0 = r34;
            r0 = r0.mCpuClusterSpeed;
            r27 = r0;
            if (r27 == 0) goto L_0x0580;
        L_0x04d5:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mCpuClusterSpeed;
            r27 = r0;
            r0 = r27;
            r0 = r0.length;
            r27 = r0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r34;
            r0 = r0.mCpuClusterSpeed;
            r29 = r0;
            r27 = 0;
            r0 = r29;
            r0 = r0.length;
            r30 = r0;
            r28 = r27;
        L_0x04ff:
            r0 = r28;
            r1 = r30;
            if (r0 >= r1) goto L_0x0589;
        L_0x0505:
            r10 = r29[r28];
            if (r10 == 0) goto L_0x0572;
        L_0x0509:
            r27 = 1;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r0 = r10.length;
            r27 = r0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            r27 = 0;
            r0 = r10.length;
            r31 = r0;
        L_0x0521:
            r0 = r27;
            r1 = r31;
            if (r0 >= r1) goto L_0x057b;
        L_0x0527:
            r9 = r10[r27];
            if (r9 == 0) goto L_0x0568;
        L_0x052b:
            r32 = 1;
            r0 = r35;
            r1 = r32;
            r0.writeInt(r1);
            r0 = r35;
            r9.writeToParcel(r0);
        L_0x0539:
            r27 = r27 + 1;
            goto L_0x0521;
        L_0x053c:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x0440;
        L_0x0547:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x0462;
        L_0x0552:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x0484;
        L_0x055d:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
            goto L_0x04a6;
        L_0x0568:
            r32 = 0;
            r0 = r35;
            r1 = r32;
            r0.writeInt(r1);
            goto L_0x0539;
        L_0x0572:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
        L_0x057b:
            r27 = r28 + 1;
            r28 = r27;
            goto L_0x04ff;
        L_0x0580:
            r27 = 0;
            r0 = r35;
            r1 = r27;
            r0.writeInt(r1);
        L_0x0589:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.writeToParcelLocked(android.os.Parcel, long):void");
        }

        void readFromParcelLocked(com.android.internal.os.BatteryStatsImpl.TimeBase r38, com.android.internal.os.BatteryStatsImpl.TimeBase r39, android.os.Parcel r40) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r37 = this;
            r25 = r40.readInt();
            r0 = r37;
            r4 = r0.mWakelockStats;
            r4.clear();
            r14 = 0;
        L_0x000c:
            r0 = r25;
            if (r14 >= r0) goto L_0x003a;
        L_0x0010:
            r35 = r40.readString();
            r34 = new com.android.internal.os.BatteryStatsImpl$Uid$Wakelock;
            r0 = r37;
            r4 = r0.mBsi;
            r0 = r34;
            r1 = r37;
            r0.<init>(r4, r1);
            r0 = r34;
            r1 = r38;
            r2 = r39;
            r3 = r40;
            r0.readFromParcelLocked(r1, r2, r3);
            r0 = r37;
            r4 = r0.mWakelockStats;
            r0 = r35;
            r1 = r34;
            r4.add(r0, r1);
            r14 = r14 + 1;
            goto L_0x000c;
        L_0x003a:
            r24 = r40.readInt();
            r0 = r37;
            r4 = r0.mSyncStats;
            r4.clear();
            r14 = 0;
        L_0x0046:
            r0 = r24;
            if (r14 >= r0) goto L_0x0078;
        L_0x004a:
            r33 = r40.readString();
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0075;
        L_0x0054:
            r0 = r37;
            r0 = r0.mSyncStats;
            r36 = r0;
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r7 = 13;
            r8 = 0;
            r6 = r37;
            r9 = r38;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r36;
            r1 = r33;
            r0.add(r1, r4);
        L_0x0075:
            r14 = r14 + 1;
            goto L_0x0046;
        L_0x0078:
            r19 = r40.readInt();
            r0 = r37;
            r4 = r0.mJobStats;
            r4.clear();
            r14 = 0;
        L_0x0084:
            r0 = r19;
            if (r14 >= r0) goto L_0x00b4;
        L_0x0088:
            r15 = r40.readString();
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x00b1;
        L_0x0092:
            r0 = r37;
            r0 = r0.mJobStats;
            r36 = r0;
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r7 = 14;
            r8 = 0;
            r6 = r37;
            r9 = r38;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r36;
            r0.add(r15, r4);
        L_0x00b1:
            r14 = r14 + 1;
            goto L_0x0084;
        L_0x00b4:
            r22 = r40.readInt();
            r0 = r37;
            r4 = r0.mSensorStats;
            r4.clear();
            r16 = 0;
        L_0x00c1:
            r0 = r16;
            r1 = r22;
            if (r0 >= r1) goto L_0x00f5;
        L_0x00c7:
            r31 = r40.readInt();
            r30 = new com.android.internal.os.BatteryStatsImpl$Uid$Sensor;
            r0 = r37;
            r4 = r0.mBsi;
            r0 = r30;
            r1 = r37;
            r2 = r31;
            r0.<init>(r4, r1, r2);
            r0 = r37;
            r4 = r0.mBsi;
            r4 = r4.mOnBatteryTimeBase;
            r0 = r30;
            r1 = r40;
            r0.readFromParcelLocked(r4, r1);
            r0 = r37;
            r4 = r0.mSensorStats;
            r0 = r31;
            r1 = r30;
            r4.put(r0, r1);
            r16 = r16 + 1;
            goto L_0x00c1;
        L_0x00f5:
            r21 = r40.readInt();
            r0 = r37;
            r4 = r0.mProcessStats;
            r4.clear();
            r16 = 0;
        L_0x0102:
            r0 = r16;
            r1 = r21;
            if (r0 >= r1) goto L_0x012e;
        L_0x0108:
            r29 = r40.readString();
            r28 = new com.android.internal.os.BatteryStatsImpl$Uid$Proc;
            r0 = r37;
            r4 = r0.mBsi;
            r0 = r28;
            r1 = r29;
            r0.<init>(r4, r1);
            r0 = r28;
            r1 = r40;
            r0.readFromParcelLocked(r1);
            r0 = r37;
            r4 = r0.mProcessStats;
            r0 = r29;
            r1 = r28;
            r4.put(r0, r1);
            r16 = r16 + 1;
            goto L_0x0102;
        L_0x012e:
            r20 = r40.readInt();
            r0 = r37;
            r4 = r0.mPackageStats;
            r4.clear();
            r17 = 0;
        L_0x013b:
            r0 = r17;
            r1 = r20;
            if (r0 >= r1) goto L_0x0165;
        L_0x0141:
            r26 = r40.readString();
            r27 = new com.android.internal.os.BatteryStatsImpl$Uid$Pkg;
            r0 = r37;
            r4 = r0.mBsi;
            r0 = r27;
            r0.<init>(r4);
            r0 = r27;
            r1 = r40;
            r0.readFromParcelLocked(r1);
            r0 = r37;
            r4 = r0.mPackageStats;
            r0 = r26;
            r1 = r27;
            r4.put(r0, r1);
            r17 = r17 + 1;
            goto L_0x013b;
        L_0x0165:
            r4 = 0;
            r0 = r37;
            r0.mWifiRunning = r4;
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x01ff;
        L_0x0170:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mWifiRunningTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 4;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mWifiRunningTimer = r4;
        L_0x0190:
            r4 = 0;
            r0 = r37;
            r0.mFullWifiLockOut = r4;
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0205;
        L_0x019b:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mFullWifiLockTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 5;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mFullWifiLockTimer = r4;
        L_0x01bb:
            r4 = 0;
            r0 = r37;
            r0.mWifiScanStarted = r4;
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x020b;
        L_0x01c6:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mWifiScanTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 6;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mWifiScanTimer = r4;
        L_0x01e6:
            r4 = -1;
            r0 = r37;
            r0.mWifiBatchedScanBinStarted = r4;
            r13 = 0;
        L_0x01ec:
            r4 = 5;
            if (r13 >= r4) goto L_0x0219;
        L_0x01ef:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0211;
        L_0x01f5:
            r0 = r37;
            r1 = r40;
            r0.makeWifiBatchedScanBin(r13, r1);
        L_0x01fc:
            r13 = r13 + 1;
            goto L_0x01ec;
        L_0x01ff:
            r4 = 0;
            r0 = r37;
            r0.mWifiRunningTimer = r4;
            goto L_0x0190;
        L_0x0205:
            r4 = 0;
            r0 = r37;
            r0.mFullWifiLockTimer = r4;
            goto L_0x01bb;
        L_0x020b:
            r4 = 0;
            r0 = r37;
            r0.mWifiScanTimer = r4;
            goto L_0x01e6;
        L_0x0211:
            r0 = r37;
            r4 = r0.mWifiBatchedScanTimer;
            r5 = 0;
            r4[r13] = r5;
            goto L_0x01fc;
        L_0x0219:
            r4 = 0;
            r0 = r37;
            r0.mWifiMulticastEnabled = r4;
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0342;
        L_0x0224:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mWifiMulticastTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 7;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mWifiMulticastTimer = r4;
        L_0x0244:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0349;
        L_0x024a:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mAudioTurnedOnTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 15;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mAudioTurnedOnTimer = r4;
        L_0x026b:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0350;
        L_0x0271:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mVideoTurnedOnTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 8;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mVideoTurnedOnTimer = r4;
        L_0x0292:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0357;
        L_0x0298:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mFlashlightTurnedOnTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 16;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mFlashlightTurnedOnTimer = r4;
        L_0x02b9:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x035e;
        L_0x02bf:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mCameraTurnedOnTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 17;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mCameraTurnedOnTimer = r4;
        L_0x02e0:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0365;
        L_0x02e6:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 10;
            r8 = 0;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mForegroundActivityTimer = r4;
        L_0x0302:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x036b;
        L_0x0308:
            r4 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mBluetoothScanOnTimers;
            r0 = r37;
            r6 = r0.mBsi;
            r9 = r6.mOnBatteryTimeBase;
            r7 = 19;
            r6 = r37;
            r10 = r40;
            r4.<init>(r5, r6, r7, r8, r9, r10);
            r0 = r37;
            r0.mBluetoothScanTimer = r4;
        L_0x0329:
            r4 = -1;
            r0 = r37;
            r0.mProcessState = r4;
            r13 = 0;
        L_0x032f:
            r4 = 6;
            if (r13 >= r4) goto L_0x0379;
        L_0x0332:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0371;
        L_0x0338:
            r0 = r37;
            r1 = r40;
            r0.makeProcessState(r13, r1);
        L_0x033f:
            r13 = r13 + 1;
            goto L_0x032f;
        L_0x0342:
            r4 = 0;
            r0 = r37;
            r0.mWifiMulticastTimer = r4;
            goto L_0x0244;
        L_0x0349:
            r4 = 0;
            r0 = r37;
            r0.mAudioTurnedOnTimer = r4;
            goto L_0x026b;
        L_0x0350:
            r4 = 0;
            r0 = r37;
            r0.mVideoTurnedOnTimer = r4;
            goto L_0x0292;
        L_0x0357:
            r4 = 0;
            r0 = r37;
            r0.mFlashlightTurnedOnTimer = r4;
            goto L_0x02b9;
        L_0x035e:
            r4 = 0;
            r0 = r37;
            r0.mCameraTurnedOnTimer = r4;
            goto L_0x02e0;
        L_0x0365:
            r4 = 0;
            r0 = r37;
            r0.mForegroundActivityTimer = r4;
            goto L_0x0302;
        L_0x036b:
            r4 = 0;
            r0 = r37;
            r0.mBluetoothScanTimer = r4;
            goto L_0x0329;
        L_0x0371:
            r0 = r37;
            r4 = r0.mProcessStateTimer;
            r5 = 0;
            r4[r13] = r5;
            goto L_0x033f;
        L_0x0379:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x03c1;
        L_0x037f:
            r4 = new com.android.internal.os.BatteryStatsImpl$BatchTimer;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mClocks;
            r0 = r37;
            r6 = r0.mBsi;
            r8 = r6.mOnBatteryTimeBase;
            r7 = 9;
            r6 = r37;
            r9 = r40;
            r4.<init>(r5, r6, r7, r8, r9);
            r0 = r37;
            r0.mVibratorOnTimer = r4;
        L_0x039a:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x03c7;
        L_0x03a0:
            r4 = 4;
            r4 = new com.android.internal.os.BatteryStatsImpl.Counter[r4];
            r0 = r37;
            r0.mUserActivityCounters = r4;
            r13 = 0;
        L_0x03a8:
            r4 = 4;
            if (r13 >= r4) goto L_0x03cc;
        L_0x03ab:
            r0 = r37;
            r4 = r0.mUserActivityCounters;
            r5 = new com.android.internal.os.BatteryStatsImpl$Counter;
            r0 = r37;
            r6 = r0.mBsi;
            r6 = r6.mOnBatteryTimeBase;
            r0 = r40;
            r5.<init>(r6, r0);
            r4[r13] = r5;
            r13 = r13 + 1;
            goto L_0x03a8;
        L_0x03c1:
            r4 = 0;
            r0 = r37;
            r0.mVibratorOnTimer = r4;
            goto L_0x039a;
        L_0x03c7:
            r4 = 0;
            r0 = r37;
            r0.mUserActivityCounters = r4;
        L_0x03cc:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x04d7;
        L_0x03d2:
            r4 = 6;
            r4 = new com.android.internal.os.BatteryStatsImpl.LongSamplingCounter[r4];
            r0 = r37;
            r0.mNetworkByteActivityCounters = r4;
            r4 = 6;
            r4 = new com.android.internal.os.BatteryStatsImpl.LongSamplingCounter[r4];
            r0 = r37;
            r0.mNetworkPacketActivityCounters = r4;
            r13 = 0;
        L_0x03e1:
            r4 = 6;
            if (r13 >= r4) goto L_0x040d;
        L_0x03e4:
            r0 = r37;
            r4 = r0.mNetworkByteActivityCounters;
            r5 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r6 = r0.mBsi;
            r6 = r6.mOnBatteryTimeBase;
            r0 = r40;
            r5.<init>(r6, r0);
            r4[r13] = r5;
            r0 = r37;
            r4 = r0.mNetworkPacketActivityCounters;
            r5 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r6 = r0.mBsi;
            r6 = r6.mOnBatteryTimeBase;
            r0 = r40;
            r5.<init>(r6, r0);
            r4[r13] = r5;
            r13 = r13 + 1;
            goto L_0x03e1;
        L_0x040d:
            r4 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r0 = r40;
            r4.<init>(r5, r0);
            r0 = r37;
            r0.mMobileRadioActiveTime = r4;
            r4 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r0 = r40;
            r4.<init>(r5, r0);
            r0 = r37;
            r0.mMobileRadioActiveCount = r4;
        L_0x042f:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x04e3;
        L_0x0435:
            r4 = new com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r6 = 1;
            r0 = r40;
            r4.<init>(r5, r6, r0);
            r0 = r37;
            r0.mWifiControllerActivity = r4;
        L_0x0447:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x04ea;
        L_0x044d:
            r4 = new com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r6 = 1;
            r0 = r40;
            r4.<init>(r5, r6, r0);
            r0 = r37;
            r0.mBluetoothControllerActivity = r4;
        L_0x045f:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x04f1;
        L_0x0465:
            r4 = new com.android.internal.os.BatteryStatsImpl$ControllerActivityCounterImpl;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r6 = 5;
            r0 = r40;
            r4.<init>(r5, r6, r0);
            r0 = r37;
            r0.mModemControllerActivity = r4;
        L_0x0477:
            r4 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r0 = r40;
            r4.<init>(r5, r0);
            r0 = r37;
            r0.mUserCpuTime = r4;
            r4 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r0 = r40;
            r4.<init>(r5, r0);
            r0 = r37;
            r0.mSystemCpuTime = r4;
            r4 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r0 = r40;
            r4.<init>(r5, r0);
            r0 = r37;
            r0.mCpuPower = r4;
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0565;
        L_0x04b0:
            r18 = r40.readInt();
            r0 = r37;
            r4 = r0.mBsi;
            r4 = r4.mPowerProfile;
            if (r4 == 0) goto L_0x04f7;
        L_0x04be:
            r0 = r37;
            r4 = r0.mBsi;
            r4 = r4.mPowerProfile;
            r4 = r4.getNumCpuClusters();
            r0 = r18;
            if (r4 == r0) goto L_0x04f7;
        L_0x04ce:
            r4 = new android.os.ParcelFormatException;
            r5 = "Incompatible number of cpu clusters";
            r4.<init>(r5);
            throw r4;
        L_0x04d7:
            r4 = 0;
            r0 = r37;
            r0.mNetworkByteActivityCounters = r4;
            r4 = 0;
            r0 = r37;
            r0.mNetworkPacketActivityCounters = r4;
            goto L_0x042f;
        L_0x04e3:
            r4 = 0;
            r0 = r37;
            r0.mWifiControllerActivity = r4;
            goto L_0x0447;
        L_0x04ea:
            r4 = 0;
            r0 = r37;
            r0.mBluetoothControllerActivity = r4;
            goto L_0x045f;
        L_0x04f1:
            r4 = 0;
            r0 = r37;
            r0.mModemControllerActivity = r4;
            goto L_0x0477;
        L_0x04f7:
            r0 = r18;
            r4 = new com.android.internal.os.BatteryStatsImpl.LongSamplingCounter[r0][];
            r0 = r37;
            r0.mCpuClusterSpeed = r4;
            r11 = 0;
        L_0x0500:
            r0 = r18;
            if (r11 >= r0) goto L_0x056a;
        L_0x0504:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x055b;
        L_0x050a:
            r23 = r40.readInt();
            r0 = r37;
            r4 = r0.mBsi;
            r4 = r4.mPowerProfile;
            if (r4 == 0) goto L_0x0531;
        L_0x0518:
            r0 = r37;
            r4 = r0.mBsi;
            r4 = r4.mPowerProfile;
            r4 = r4.getNumSpeedStepsInCpuCluster(r11);
            r0 = r23;
            if (r4 == r0) goto L_0x0531;
        L_0x0528:
            r4 = new android.os.ParcelFormatException;
            r5 = "Incompatible number of cpu speeds";
            r4.<init>(r5);
            throw r4;
        L_0x0531:
            r0 = r23;
            r12 = new com.android.internal.os.BatteryStatsImpl.LongSamplingCounter[r0];
            r0 = r37;
            r4 = r0.mCpuClusterSpeed;
            r4[r11] = r12;
            r32 = 0;
        L_0x053d:
            r0 = r32;
            r1 = r23;
            if (r0 >= r1) goto L_0x0562;
        L_0x0543:
            r4 = r40.readInt();
            if (r4 == 0) goto L_0x0558;
        L_0x0549:
            r4 = new com.android.internal.os.BatteryStatsImpl$LongSamplingCounter;
            r0 = r37;
            r5 = r0.mBsi;
            r5 = r5.mOnBatteryTimeBase;
            r0 = r40;
            r4.<init>(r5, r0);
            r12[r32] = r4;
        L_0x0558:
            r32 = r32 + 1;
            goto L_0x053d;
        L_0x055b:
            r0 = r37;
            r4 = r0.mCpuClusterSpeed;
            r5 = 0;
            r4[r11] = r5;
        L_0x0562:
            r11 = r11 + 1;
            goto L_0x0500;
        L_0x0565:
            r4 = 0;
            r0 = r37;
            r0.mCpuClusterSpeed = r4;
        L_0x056a:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.readFromParcelLocked(com.android.internal.os.BatteryStatsImpl$TimeBase, com.android.internal.os.BatteryStatsImpl$TimeBase, android.os.Parcel):void");
        }

        public com.android.internal.os.BatteryStatsImpl.Uid.Proc getProcessStatsLocked(java.lang.String r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mProcessStats;
            r0 = r1.get(r3);
            r0 = (com.android.internal.os.BatteryStatsImpl.Uid.Proc) r0;
            if (r0 != 0) goto L_0x0016;
        L_0x000a:
            r0 = new com.android.internal.os.BatteryStatsImpl$Uid$Proc;
            r1 = r2.mBsi;
            r0.<init>(r1, r3);
            r1 = r2.mProcessStats;
            r1.put(r3, r0);
        L_0x0016:
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getProcessStatsLocked(java.lang.String):com.android.internal.os.BatteryStatsImpl$Uid$Proc");
        }

        public void updateUidProcessStateLocked(int r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r7 = this;
            r6 = 0;
            r5 = -1;
            if (r8 != r5) goto L_0x000a;
        L_0x0004:
            r2 = -1;
        L_0x0005:
            r3 = r7.mProcessState;
            if (r3 != r2) goto L_0x0026;
        L_0x0009:
            return;
        L_0x000a:
            r3 = 2;
            if (r8 != r3) goto L_0x000f;
        L_0x000d:
            r2 = 0;
            goto L_0x0005;
        L_0x000f:
            r3 = 4;
            if (r8 > r3) goto L_0x0014;
        L_0x0012:
            r2 = 1;
            goto L_0x0005;
        L_0x0014:
            r3 = 5;
            if (r8 > r3) goto L_0x0019;
        L_0x0017:
            r2 = 2;
            goto L_0x0005;
        L_0x0019:
            r3 = 6;
            if (r8 > r3) goto L_0x001e;
        L_0x001c:
            r2 = 3;
            goto L_0x0005;
        L_0x001e:
            r3 = 11;
            if (r8 > r3) goto L_0x0024;
        L_0x0022:
            r2 = 4;
            goto L_0x0005;
        L_0x0024:
            r2 = 5;
            goto L_0x0005;
        L_0x0026:
            r3 = r7.mBsi;
            r3 = r3.mClocks;
            r0 = r3.elapsedRealtime();
            r3 = r7.mProcessState;
            if (r3 == r5) goto L_0x003b;
        L_0x0032:
            r3 = r7.mProcessStateTimer;
            r4 = r7.mProcessState;
            r3 = r3[r4];
            r3.stopRunningLocked(r0);
        L_0x003b:
            r7.mProcessState = r2;
            if (r2 == r5) goto L_0x004f;
        L_0x003f:
            r3 = r7.mProcessStateTimer;
            r3 = r3[r2];
            if (r3 != 0) goto L_0x0048;
        L_0x0045:
            r7.makeProcessState(r2, r6);
        L_0x0048:
            r3 = r7.mProcessStateTimer;
            r3 = r3[r2];
            r3.startRunningLocked(r0);
        L_0x004f:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.updateUidProcessStateLocked(int):void");
        }

        public android.util.SparseArray<? extends android.os.BatteryStats.Uid.Pid> getPidStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mPids;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getPidStats():android.util.SparseArray<? extends android.os.BatteryStats$Uid$Pid>");
        }

        public android.os.BatteryStats.Uid.Pid getPidStatsLocked(int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mPids;
            r0 = r1.get(r3);
            r0 = (android.os.BatteryStats.Uid.Pid) r0;
            if (r0 != 0) goto L_0x0014;
        L_0x000a:
            r0 = new android.os.BatteryStats$Uid$Pid;
            r0.<init>(r2);
            r1 = r2.mPids;
            r1.put(r3, r0);
        L_0x0014:
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getPidStatsLocked(int):android.os.BatteryStats$Uid$Pid");
        }

        public com.android.internal.os.BatteryStatsImpl.Uid.Pkg getPackageStatsLocked(java.lang.String r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mPackageStats;
            r0 = r1.get(r3);
            r0 = (com.android.internal.os.BatteryStatsImpl.Uid.Pkg) r0;
            if (r0 != 0) goto L_0x0016;
        L_0x000a:
            r0 = new com.android.internal.os.BatteryStatsImpl$Uid$Pkg;
            r1 = r2.mBsi;
            r0.<init>(r1);
            r1 = r2.mPackageStats;
            r1.put(r3, r0);
        L_0x0016:
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getPackageStatsLocked(java.lang.String):com.android.internal.os.BatteryStatsImpl$Uid$Pkg");
        }

        public com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv getServiceStatsLocked(java.lang.String r4, java.lang.String r5) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r0 = r3.getPackageStatsLocked(r4);
            r2 = r0.mServiceStats;
            r1 = r2.get(r5);
            r1 = (com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv) r1;
            if (r1 != 0) goto L_0x0017;
        L_0x000e:
            r1 = r0.newServiceStatsLocked();
            r2 = r0.mServiceStats;
            r2.put(r5, r1);
        L_0x0017:
            return r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getServiceStatsLocked(java.lang.String, java.lang.String):com.android.internal.os.BatteryStatsImpl$Uid$Pkg$Serv");
        }

        public void readSyncSummaryFromParcelLocked(java.lang.String r3, android.os.Parcel r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mSyncStats;
            r0 = r1.instantiateObject();
            r0 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r0;
            r0.readSummaryFromParcelLocked(r4);
            r1 = r2.mSyncStats;
            r1.add(r3, r0);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.readSyncSummaryFromParcelLocked(java.lang.String, android.os.Parcel):void");
        }

        public void readJobSummaryFromParcelLocked(java.lang.String r3, android.os.Parcel r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mJobStats;
            r0 = r1.instantiateObject();
            r0 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r0;
            r0.readSummaryFromParcelLocked(r4);
            r1 = r2.mJobStats;
            r1.add(r3, r0);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.readJobSummaryFromParcelLocked(java.lang.String, android.os.Parcel):void");
        }

        public void readWakeSummaryFromParcelLocked(java.lang.String r4, android.os.Parcel r5) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r2 = 0;
            r0 = new com.android.internal.os.BatteryStatsImpl$Uid$Wakelock;
            r1 = r3.mBsi;
            r0.<init>(r1, r3);
            r1 = r3.mWakelockStats;
            r1.add(r4, r0);
            r1 = r5.readInt();
            if (r1 == 0) goto L_0x001b;
        L_0x0013:
            r1 = 1;
            r1 = r0.getStopwatchTimer(r1);
            r1.readSummaryFromParcelLocked(r5);
        L_0x001b:
            r1 = r5.readInt();
            if (r1 == 0) goto L_0x0028;
        L_0x0021:
            r1 = r0.getStopwatchTimer(r2);
            r1.readSummaryFromParcelLocked(r5);
        L_0x0028:
            r1 = r5.readInt();
            if (r1 == 0) goto L_0x0036;
        L_0x002e:
            r1 = 2;
            r1 = r0.getStopwatchTimer(r1);
            r1.readSummaryFromParcelLocked(r5);
        L_0x0036:
            r1 = r5.readInt();
            if (r1 == 0) goto L_0x0045;
        L_0x003c:
            r1 = 18;
            r1 = r0.getStopwatchTimer(r1);
            r1.readSummaryFromParcelLocked(r5);
        L_0x0045:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.readWakeSummaryFromParcelLocked(java.lang.String, android.os.Parcel):void");
        }

        public com.android.internal.os.BatteryStatsImpl.StopwatchTimer getSensorTimerLocked(int r8, boolean r9) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r7 = this;
            r2 = 0;
            r1 = r7.mSensorStats;
            r6 = r1.get(r8);
            r6 = (com.android.internal.os.BatteryStatsImpl.Uid.Sensor) r6;
            if (r6 != 0) goto L_0x001a;
        L_0x000b:
            if (r9 != 0) goto L_0x000e;
        L_0x000d:
            return r2;
        L_0x000e:
            r6 = new com.android.internal.os.BatteryStatsImpl$Uid$Sensor;
            r1 = r7.mBsi;
            r6.<init>(r1, r7, r8);
            r1 = r7.mSensorStats;
            r1.put(r8, r6);
        L_0x001a:
            r0 = r6.mTimer;
            if (r0 == 0) goto L_0x001f;
        L_0x001e:
            return r0;
        L_0x001f:
            r1 = r7.mBsi;
            r1 = r1.mSensorTimers;
            r4 = r1.get(r8);
            r4 = (java.util.ArrayList) r4;
            if (r4 != 0) goto L_0x0037;
        L_0x002b:
            r4 = new java.util.ArrayList;
            r4.<init>();
            r1 = r7.mBsi;
            r1 = r1.mSensorTimers;
            r1.put(r8, r4);
        L_0x0037:
            r0 = new com.android.internal.os.BatteryStatsImpl$StopwatchTimer;
            r1 = r7.mBsi;
            r1 = r1.mClocks;
            r2 = r7.mBsi;
            r5 = r2.mOnBatteryTimeBase;
            r3 = 3;
            r2 = r7;
            r0.<init>(r1, r2, r3, r4, r5);
            r6.mTimer = r0;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getSensorTimerLocked(int, boolean):com.android.internal.os.BatteryStatsImpl$StopwatchTimer");
        }

        public void noteStartSyncLocked(java.lang.String r3, long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mSyncStats;
            r0 = r1.startObject(r3);
            r0 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r0;
            if (r0 == 0) goto L_0x000d;
        L_0x000a:
            r0.startRunningLocked(r4);
        L_0x000d:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStartSyncLocked(java.lang.String, long):void");
        }

        public void noteStopSyncLocked(java.lang.String r3, long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mSyncStats;
            r0 = r1.stopObject(r3);
            r0 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r0;
            if (r0 == 0) goto L_0x000d;
        L_0x000a:
            r0.stopRunningLocked(r4);
        L_0x000d:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStopSyncLocked(java.lang.String, long):void");
        }

        public void noteStartJobLocked(java.lang.String r3, long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mJobStats;
            r0 = r1.startObject(r3);
            r0 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r0;
            if (r0 == 0) goto L_0x000d;
        L_0x000a:
            r0.startRunningLocked(r4);
        L_0x000d:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStartJobLocked(java.lang.String, long):void");
        }

        public void noteStopJobLocked(java.lang.String r3, long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = r2.mJobStats;
            r0 = r1.stopObject(r3);
            r0 = (com.android.internal.os.BatteryStatsImpl.StopwatchTimer) r0;
            if (r0 == 0) goto L_0x000d;
        L_0x000a:
            r0.stopRunningLocked(r4);
        L_0x000d:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStopJobLocked(java.lang.String, long):void");
        }

        public void noteStartWakeLocked(int r5, java.lang.String r6, int r7, long r8) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r4 = this;
            r2 = r4.mWakelockStats;
            r1 = r2.startObject(r6);
            r1 = (com.android.internal.os.BatteryStatsImpl.Uid.Wakelock) r1;
            if (r1 == 0) goto L_0x0011;
        L_0x000a:
            r2 = r1.getStopwatchTimer(r7);
            r2.startRunningLocked(r8);
        L_0x0011:
            if (r5 < 0) goto L_0x0023;
        L_0x0013:
            if (r7 != 0) goto L_0x0023;
        L_0x0015:
            r0 = r4.getPidStatsLocked(r5);
            r2 = r0.mWakeNesting;
            r3 = r2 + 1;
            r0.mWakeNesting = r3;
            if (r2 != 0) goto L_0x0023;
        L_0x0021:
            r0.mWakeStartMs = r8;
        L_0x0023:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStartWakeLocked(int, java.lang.String, int, long):void");
        }

        public void noteStopWakeLocked(int r7, java.lang.String r8, int r9, long r10) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r6 = this;
            r2 = r6.mWakelockStats;
            r1 = r2.stopObject(r8);
            r1 = (com.android.internal.os.BatteryStatsImpl.Uid.Wakelock) r1;
            if (r1 == 0) goto L_0x0011;
        L_0x000a:
            r2 = r1.getStopwatchTimer(r9);
            r2.stopRunningLocked(r10);
        L_0x0011:
            if (r7 < 0) goto L_0x0039;
        L_0x0013:
            if (r9 != 0) goto L_0x0039;
        L_0x0015:
            r2 = r6.mPids;
            r0 = r2.get(r7);
            r0 = (android.os.BatteryStats.Uid.Pid) r0;
            if (r0 == 0) goto L_0x0039;
        L_0x001f:
            r2 = r0.mWakeNesting;
            if (r2 <= 0) goto L_0x0039;
        L_0x0023:
            r2 = r0.mWakeNesting;
            r3 = r2 + -1;
            r0.mWakeNesting = r3;
            r3 = 1;
            if (r2 != r3) goto L_0x0039;
        L_0x002c:
            r2 = r0.mWakeSumMs;
            r4 = r0.mWakeStartMs;
            r4 = r10 - r4;
            r2 = r2 + r4;
            r0.mWakeSumMs = r2;
            r2 = 0;
            r0.mWakeStartMs = r2;
        L_0x0039:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStopWakeLocked(int, java.lang.String, int, long):void");
        }

        public void reportExcessiveWakeLocked(java.lang.String r3, long r4, long r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.getProcessStatsLocked(r3);
            if (r0 == 0) goto L_0x0009;
        L_0x0006:
            r0.addExcessiveWake(r4, r6);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.reportExcessiveWakeLocked(java.lang.String, long, long):void");
        }

        public void reportExcessiveCpuLocked(java.lang.String r3, long r4, long r6) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r0 = r2.getProcessStatsLocked(r3);
            if (r0 == 0) goto L_0x0009;
        L_0x0006:
            r0.addExcessiveCpu(r4, r6);
        L_0x0009:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.reportExcessiveCpuLocked(java.lang.String, long, long):void");
        }

        public void noteStartSensor(int r3, long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = 1;
            r0 = r2.getSensorTimerLocked(r3, r1);
            if (r0 == 0) goto L_0x000a;
        L_0x0007:
            r0.startRunningLocked(r4);
        L_0x000a:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStartSensor(int, long):void");
        }

        public void noteStopSensor(int r3, long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r2 = this;
            r1 = 0;
            r0 = r2.getSensorTimerLocked(r3, r1);
            if (r0 == 0) goto L_0x000a;
        L_0x0007:
            r0.stopRunningLocked(r4);
        L_0x000a:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStopSensor(int, long):void");
        }

        public void noteStartGps(long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r1 = -10000; // 0xffffffffffffd8f0 float:NaN double:NaN;
            r2 = 1;
            r0 = r3.getSensorTimerLocked(r1, r2);
            if (r0 == 0) goto L_0x000c;
        L_0x0009:
            r0.startRunningLocked(r4);
        L_0x000c:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStartGps(long):void");
        }

        public void noteStopGps(long r4) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r3 = this;
            r1 = -10000; // 0xffffffffffffd8f0 float:NaN double:NaN;
            r2 = 0;
            r0 = r3.getSensorTimerLocked(r1, r2);
            if (r0 == 0) goto L_0x000c;
        L_0x0009:
            r0.stopRunningLocked(r4);
        L_0x000c:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.noteStopGps(long):void");
        }

        public com.android.internal.os.BatteryStatsImpl getBatteryStats() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:119)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r1 = this;
            r0 = r1.mBsi;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.Uid.getBatteryStats():com.android.internal.os.BatteryStatsImpl");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.BatteryStatsImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.BatteryStatsImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsImpl.<clinit>():void");
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

    public BatteryStatsImpl() {
        this(new SystemClocks());
    }

    public BatteryStatsImpl(Clocks clocks) {
        this.mKernelWakelockReader = new KernelWakelockReader();
        this.mTmpWakelockStats = new KernelWakelockStats();
        this.mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
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
        this.mHaveBatteryLevel = USE_OLD_HISTORY;
        this.mRecordingHistory = USE_OLD_HISTORY;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new HistoryItem();
        this.mHistoryLastLastWritten = new HistoryItem();
        this.mHistoryReadTmp = new HistoryItem();
        this.mHistoryAddTmp = new HistoryItem();
        this.mHistoryTagPool = new HashMap();
        this.mNextHistoryTagIdx = NETWORK_STATS_LAST;
        this.mNumHistoryTagChars = NETWORK_STATS_LAST;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = USE_OLD_HISTORY;
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
        this.mScreenState = NETWORK_STATS_LAST;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[17];
        this.mNetworkByteActivityCounters = new LongSamplingCounter[6];
        this.mNetworkPacketActivityCounters = new LongSamplingCounter[6];
        this.mHasWifiReporting = USE_OLD_HISTORY;
        this.mHasBluetoothReporting = USE_OLD_HISTORY;
        this.mHasModemReporting = USE_OLD_HISTORY;
        this.mWifiState = -1;
        this.mWifiStateTimer = new StopwatchTimer[8];
        this.mWifiSupplState = -1;
        this.mWifiSupplStateTimer = new StopwatchTimer[13];
        this.mWifiSignalStrengthBin = -1;
        this.mWifiSignalStrengthsTimer = new StopwatchTimer[5];
        this.mMobileRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mWifiRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mCharging = true;
        this.mInitStepMode = NETWORK_STATS_LAST;
        this.mCurStepMode = NETWORK_STATS_LAST;
        this.mModStepMode = NETWORK_STATS_LAST;
        this.mDischargeStepTracker = new LevelStepTracker(MAX_LEVEL_STEPS);
        this.mDailyDischargeStepTracker = new LevelStepTracker(StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION);
        this.mChargeStepTracker = new LevelStepTracker(MAX_LEVEL_STEPS);
        this.mDailyChargeStepTracker = new LevelStepTracker(StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mTmpNetworkStatsEntry = new Entry();
        this.mKernelWakelockStats = new HashMap();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap();
        this.mChangedStates = NETWORK_STATS_LAST;
        this.mChangedStates2 = NETWORK_STATS_LAST;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = NETWORK_STATS_LAST;
        this.mWifiScanNesting = NETWORK_STATS_LAST;
        this.mWifiMulticastNesting = NETWORK_STATS_LAST;
        this.mMobileIfaces = EmptyArray.STRING;
        this.mWifiIfaces = EmptyArray.STRING;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
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
        NetworkStats[] networkStatsArr = new NetworkStats[STATE_BATTERY_PLUG_MASK];
        networkStatsArr[NETWORK_STATS_LAST] = new NetworkStats(this.mClocks.elapsedRealtime(), 50);
        networkStatsArr[NUM_WIFI_TX_LEVELS] = new NetworkStats(this.mClocks.elapsedRealtime(), 50);
        networkStatsArr[NETWORK_STATS_DELTA] = new NetworkStats(this.mClocks.elapsedRealtime(), 50);
        this.mMobileNetworkStats = networkStatsArr;
        networkStatsArr = new NetworkStats[STATE_BATTERY_PLUG_MASK];
        networkStatsArr[NETWORK_STATS_LAST] = new NetworkStats(this.mClocks.elapsedRealtime(), 50);
        networkStatsArr[NUM_WIFI_TX_LEVELS] = new NetworkStats(this.mClocks.elapsedRealtime(), 50);
        networkStatsArr[NETWORK_STATS_DELTA] = new NetworkStats(this.mClocks.elapsedRealtime(), 50);
        this.mWifiNetworkStats = networkStatsArr;
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
        this.mNextHistoryTagIdx += NUM_WIFI_TX_LEVELS;
        this.mNumHistoryTagChars += key.string.length() + NUM_WIFI_TX_LEVELS;
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
        if (last == null || cur.cmd != null) {
            dest.writeInt(DELTA_TIME_ABS);
            cur.writeToParcel(dest, NETWORK_STATS_LAST);
            return;
        }
        int deltaTimeToken;
        long deltaTime = cur.time - last.time;
        int lastBatteryLevelInt = buildBatteryLevelInt(last);
        int lastStateInt = buildStateInt(last);
        if (deltaTime < 0 || deltaTime > 2147483647L) {
            deltaTimeToken = DELTA_TIME_MASK;
        } else if (deltaTime >= 524285) {
            deltaTimeToken = DELTA_TIME_INT;
        } else {
            deltaTimeToken = (int) deltaTime;
        }
        int firstToken = deltaTimeToken | (cur.states & DELTA_STATE_MASK);
        int includeStepDetails = this.mLastHistoryStepLevel > cur.batteryLevel ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST;
        boolean computeStepDetails = includeStepDetails == 0 ? this.mLastHistoryStepDetails == null ? true : USE_OLD_HISTORY : true;
        int batteryLevelInt = buildBatteryLevelInt(cur) | includeStepDetails;
        boolean batteryLevelIntChanged = batteryLevelInt != lastBatteryLevelInt ? true : USE_OLD_HISTORY;
        if (batteryLevelIntChanged) {
            firstToken |= DELTA_BATTERY_LEVEL_FLAG;
        }
        int stateInt = buildStateInt(cur);
        boolean stateIntChanged = stateInt != lastStateInt ? true : USE_OLD_HISTORY;
        if (stateIntChanged) {
            firstToken |= DELTA_STATE_FLAG;
        }
        boolean state2IntChanged = cur.states2 != last.states2 ? true : USE_OLD_HISTORY;
        if (state2IntChanged) {
            firstToken |= DELTA_STATE2_FLAG;
        }
        if (!(cur.wakelockTag == null && cur.wakeReasonTag == null)) {
            firstToken |= DELTA_WAKELOCK_FLAG;
        }
        if (cur.eventCode != 0) {
            firstToken |= DELTA_EVENT_FLAG;
        }
        boolean batteryChargeChanged = cur.batteryChargeUAh != last.batteryChargeUAh ? true : USE_OLD_HISTORY;
        if (batteryChargeChanged) {
            firstToken |= DELTA_BATTERY_CHARGE_FLAG;
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
                wakeLockIndex = Protocol.MAX_MESSAGE;
            }
            if (cur.wakeReasonTag != null) {
                wakeReasonIndex = writeHistoryTag(cur.wakeReasonTag);
            } else {
                wakeReasonIndex = Protocol.MAX_MESSAGE;
            }
            dest.writeInt((wakeReasonIndex << 16) | wakeLockIndex);
        }
        if (cur.eventCode != 0) {
            dest.writeInt((cur.eventCode & Protocol.MAX_MESSAGE) | (writeHistoryTag(cur.eventTag) << 16));
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
        return (((h.batteryLevel << 25) & DELTA_STATE_MASK) | ((h.batteryTemperature << 15) & 33521664)) | ((h.batteryVoltage << NUM_WIFI_TX_LEVELS) & 32766);
    }

    private void readBatteryLevelInt(int batteryLevelInt, HistoryItem out) {
        out.batteryLevel = (byte) ((DELTA_STATE_MASK & batteryLevelInt) >>> 25);
        out.batteryTemperature = (short) ((33521664 & batteryLevelInt) >>> 15);
        out.batteryVoltage = (char) ((batteryLevelInt & 32766) >>> NUM_WIFI_TX_LEVELS);
    }

    private int buildStateInt(HistoryItem h) {
        int plugType = NETWORK_STATS_LAST;
        if ((h.batteryPlugType & NUM_WIFI_TX_LEVELS) != 0) {
            plugType = NUM_WIFI_TX_LEVELS;
        } else if ((h.batteryPlugType & NETWORK_STATS_DELTA) != 0) {
            plugType = NETWORK_STATS_DELTA;
        } else if ((h.batteryPlugType & 4) != 0) {
            plugType = STATE_BATTERY_PLUG_MASK;
        }
        return ((((h.batteryStatus & STATE_BATTERY_STATUS_MASK) << STATE_BATTERY_STATUS_SHIFT) | ((h.batteryHealth & STATE_BATTERY_STATUS_MASK) << STATE_BATTERY_HEALTH_SHIFT)) | ((plugType & STATE_BATTERY_PLUG_MASK) << STATE_BATTERY_PLUG_SHIFT)) | (h.states & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT);
    }

    private void computeHistoryStepDetails(HistoryStepDetails out, HistoryStepDetails last) {
        HistoryStepDetails tmp = last != null ? this.mTmpHistoryStepDetails : out;
        requestImmediateCpuUpdate();
        int NU;
        int i;
        if (last == null) {
            NU = this.mUidStats.size();
            for (i = NETWORK_STATS_LAST; i < NU; i += NUM_WIFI_TX_LEVELS) {
                Uid uid = (Uid) this.mUidStats.valueAt(i);
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
        out.appCpuUTime3 = NETWORK_STATS_LAST;
        out.appCpuUTime2 = NETWORK_STATS_LAST;
        out.appCpuUTime1 = NETWORK_STATS_LAST;
        out.appCpuSTime3 = NETWORK_STATS_LAST;
        out.appCpuSTime2 = NETWORK_STATS_LAST;
        out.appCpuSTime1 = NETWORK_STATS_LAST;
        NU = this.mUidStats.size();
        for (i = NETWORK_STATS_LAST; i < NU; i += NUM_WIFI_TX_LEVELS) {
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
        int deltaTimeToken = firstToken & DELTA_TIME_MASK;
        cur.cmd = (byte) 0;
        cur.numReadInts = NUM_WIFI_TX_LEVELS;
        if (deltaTimeToken < DELTA_TIME_ABS) {
            cur.time += (long) deltaTimeToken;
        } else if (deltaTimeToken == DELTA_TIME_ABS) {
            cur.time = src.readLong();
            cur.numReadInts += NETWORK_STATS_DELTA;
            cur.readFromParcel(src);
            return;
        } else if (deltaTimeToken == DELTA_TIME_INT) {
            cur.time += (long) src.readInt();
            cur.numReadInts += NUM_WIFI_TX_LEVELS;
        } else {
            cur.time += src.readLong();
            cur.numReadInts += NETWORK_STATS_DELTA;
        }
        if ((DELTA_BATTERY_LEVEL_FLAG & firstToken) != 0) {
            batteryLevelInt = src.readInt();
            readBatteryLevelInt(batteryLevelInt, cur);
            cur.numReadInts += NUM_WIFI_TX_LEVELS;
        } else {
            batteryLevelInt = NETWORK_STATS_LAST;
        }
        if ((DELTA_STATE_FLAG & firstToken) != 0) {
            int stateInt = src.readInt();
            cur.states = (DELTA_STATE_MASK & firstToken) | (AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT & stateInt);
            cur.batteryStatus = (byte) ((stateInt >> STATE_BATTERY_STATUS_SHIFT) & STATE_BATTERY_STATUS_MASK);
            cur.batteryHealth = (byte) ((stateInt >> STATE_BATTERY_HEALTH_SHIFT) & STATE_BATTERY_STATUS_MASK);
            cur.batteryPlugType = (byte) ((stateInt >> STATE_BATTERY_PLUG_SHIFT) & STATE_BATTERY_PLUG_MASK);
            switch (cur.batteryPlugType) {
                case NUM_WIFI_TX_LEVELS /*1*/:
                    cur.batteryPlugType = (byte) 1;
                    break;
                case NETWORK_STATS_DELTA /*2*/:
                    cur.batteryPlugType = (byte) 2;
                    break;
                case STATE_BATTERY_PLUG_MASK /*3*/:
                    cur.batteryPlugType = (byte) 4;
                    break;
            }
            cur.numReadInts += NUM_WIFI_TX_LEVELS;
        } else {
            cur.states = (DELTA_STATE_MASK & firstToken) | (cur.states & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT);
        }
        if ((DELTA_STATE2_FLAG & firstToken) != 0) {
            cur.states2 = src.readInt();
        }
        if ((DELTA_WAKELOCK_FLAG & firstToken) != 0) {
            int indexes = src.readInt();
            int wakeLockIndex = indexes & Protocol.MAX_MESSAGE;
            int wakeReasonIndex = (indexes >> 16) & Protocol.MAX_MESSAGE;
            if (wakeLockIndex != Protocol.MAX_MESSAGE) {
                cur.wakelockTag = cur.localWakelockTag;
                readHistoryTag(wakeLockIndex, cur.wakelockTag);
            } else {
                cur.wakelockTag = null;
            }
            if (wakeReasonIndex != Protocol.MAX_MESSAGE) {
                cur.wakeReasonTag = cur.localWakeReasonTag;
                readHistoryTag(wakeReasonIndex, cur.wakeReasonTag);
            } else {
                cur.wakeReasonTag = null;
            }
            cur.numReadInts += NUM_WIFI_TX_LEVELS;
        } else {
            cur.wakelockTag = null;
            cur.wakeReasonTag = null;
        }
        if ((DELTA_EVENT_FLAG & firstToken) != 0) {
            cur.eventTag = cur.localEventTag;
            int codeAndIndex = src.readInt();
            cur.eventCode = Protocol.MAX_MESSAGE & codeAndIndex;
            readHistoryTag((codeAndIndex >> 16) & Protocol.MAX_MESSAGE, cur.eventTag);
            cur.numReadInts += NUM_WIFI_TX_LEVELS;
        } else {
            cur.eventCode = NETWORK_STATS_LAST;
        }
        if ((batteryLevelInt & NUM_WIFI_TX_LEVELS) != 0) {
            cur.stepDetails = this.mReadHistoryStepDetails;
            cur.stepDetails.readFromParcel(src);
        } else {
            cur.stepDetails = null;
        }
        if ((DELTA_BATTERY_CHARGE_FLAG & firstToken) != 0) {
            cur.batteryChargeUAh = src.readInt();
        }
    }

    public void commitCurrentHistoryBatchLocked() {
        this.mHistoryLastWritten.cmd = (byte) -1;
    }

    void addHistoryBufferLocked(long elapsedRealtimeMs, long uptimeMs, HistoryItem cur) {
        if (this.mHaveBatteryLevel && this.mRecordingHistory) {
            long timeDiff = (this.mHistoryBaseTime + elapsedRealtimeMs) - this.mHistoryLastWritten.time;
            int diffStates = this.mHistoryLastWritten.states ^ (cur.states & this.mActiveHistoryStates);
            int diffStates2 = this.mHistoryLastWritten.states2 ^ (cur.states2 & this.mActiveHistoryStates2);
            int lastDiffStates = this.mHistoryLastWritten.states ^ this.mHistoryLastLastWritten.states;
            int lastDiffStates2 = this.mHistoryLastWritten.states2 ^ this.mHistoryLastLastWritten.states2;
            if (this.mHistoryBufferLastPos >= 0 && this.mHistoryLastWritten.cmd == null && timeDiff < 1000 && (diffStates & lastDiffStates) == 0 && (diffStates2 & lastDiffStates2) == 0 && ((this.mHistoryLastWritten.wakelockTag == null || cur.wakelockTag == null) && ((this.mHistoryLastWritten.wakeReasonTag == null || cur.wakeReasonTag == null) && this.mHistoryLastWritten.stepDetails == null && ((this.mHistoryLastWritten.eventCode == 0 || cur.eventCode == 0) && this.mHistoryLastWritten.batteryLevel == cur.batteryLevel && this.mHistoryLastWritten.batteryStatus == cur.batteryStatus && this.mHistoryLastWritten.batteryHealth == cur.batteryHealth && this.mHistoryLastWritten.batteryPlugType == cur.batteryPlugType && this.mHistoryLastWritten.batteryTemperature == cur.batteryTemperature && this.mHistoryLastWritten.batteryVoltage == cur.batteryVoltage)))) {
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
            int dataSize = this.mHistoryBuffer.dataSize();
            if (dataSize < MAX_HISTORY_BUFFER) {
                if (dataSize == 0) {
                    cur.currentTime = System.currentTimeMillis();
                    addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 7, cur);
                }
                addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 0, cur);
            } else if (this.mHistoryOverflow) {
                int old;
                int writeAnyway = NETWORK_STATS_LAST;
                int curStates = (cur.states & -1638400) & this.mActiveHistoryStates;
                if (this.mHistoryLastWritten.states != curStates) {
                    old = this.mActiveHistoryStates;
                    this.mActiveHistoryStates &= 1638399 | curStates;
                    writeAnyway = old != this.mActiveHistoryStates ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST;
                }
                int curStates2 = (cur.states2 & 1748959232) & this.mActiveHistoryStates2;
                if (this.mHistoryLastWritten.states2 != curStates2) {
                    old = this.mActiveHistoryStates2;
                    this.mActiveHistoryStates2 &= -1748959233 | curStates2;
                    writeAnyway |= old != this.mActiveHistoryStates2 ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST;
                }
                if (writeAnyway != 0 || this.mHistoryLastWritten.batteryLevel != cur.batteryLevel || (dataSize < MAX_MAX_HISTORY_BUFFER && ((this.mHistoryLastWritten.states ^ cur.states) & 1572864) != 0 && ((this.mHistoryLastWritten.states2 ^ cur.states2) & -1749024768) != 0)) {
                    addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 0, cur);
                }
            } else {
                this.mHistoryOverflow = true;
                addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 0, cur);
                addHistoryBufferLocked(elapsedRealtimeMs, uptimeMs, (byte) 6, cur);
            }
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
        cur.eventCode = NETWORK_STATS_LAST;
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
                this.mHistoryAddTmp.eventCode = NETWORK_STATS_LAST;
                historyItem = this.mHistoryAddTmp;
                historyItem.states &= HwBootFail.STAGE_BOOT_SUCCESS;
                addHistoryRecordInnerLocked(wakeElapsedTime, uptimeMs, this.mHistoryAddTmp);
            }
        }
        historyItem = this.mHistoryCur;
        historyItem.states |= RtlSpacingHelper.UNDEFINED;
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
        this.mNumHistoryItems += NUM_WIFI_TX_LEVELS;
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
        this.mHistoryBuffer.setDataSize(NETWORK_STATS_LAST);
        this.mHistoryBuffer.setDataPosition(NETWORK_STATS_LAST);
        this.mHistoryBuffer.setDataCapacity(Protocol.BASE_WIFI);
        this.mHistoryLastLastWritten.clear();
        this.mHistoryLastWritten.clear();
        this.mHistoryTagPool.clear();
        this.mNextHistoryTagIdx = NETWORK_STATS_LAST;
        this.mNumHistoryTagChars = NETWORK_STATS_LAST;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = USE_OLD_HISTORY;
        this.mActiveHistoryStates = -1;
        this.mActiveHistoryStates2 = -1;
    }

    public void updateTimeBasesLocked(boolean unplugged, boolean screenOff, long uptime, long realtime) {
        this.mOnBatteryTimeBase.setRunning(unplugged, uptime, realtime);
        boolean z = unplugged ? screenOff : USE_OLD_HISTORY;
        if (z != this.mOnBatteryScreenOffTimeBase.isRunning()) {
            updateKernelWakelocksLocked();
            updateCpuTimeLocked();
            this.mOnBatteryScreenOffTimeBase.setRunning(z, uptime, realtime);
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
    }

    public int mapUid(int uid) {
        int isolated = this.mIsolatedUids.get(uid, -1);
        return isolated > 0 ? isolated : uid;
    }

    public void noteEventLocked(int code, String name, int uid) {
        uid = mapUid(uid);
        if (this.mActiveEvents.updateState(code, name, uid, NETWORK_STATS_LAST)) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), code, name, uid);
        }
    }

    boolean ensureStartClockTime(long currentTime) {
        if (currentTime <= 31536000000L || this.mStartClockTime >= currentTime - 31536000000L) {
            return USE_OLD_HISTORY;
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
        if (this.mActiveEvents.updateState(32769, name, uid, NETWORK_STATS_LAST) && this.mRecordAllHistory) {
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
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT1, name, uid, NETWORK_STATS_LAST) && this.mRecordAllHistory) {
            addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), GL10.GL_LIGHT1, name, uid);
        }
    }

    public void noteSyncStartLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStartSyncLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(32772, name, uid, NETWORK_STATS_LAST)) {
            addHistoryEventLocked(elapsedRealtime, uptime, 32772, name, uid);
        }
    }

    public void noteSyncFinishLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStopSyncLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT4, name, uid, NETWORK_STATS_LAST)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT4, name, uid);
        }
    }

    public void noteJobStartLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStartJobLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL11ExtensionPack.GL_FUNC_ADD, name, uid, NETWORK_STATS_LAST)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL11ExtensionPack.GL_FUNC_ADD, name, uid);
        }
    }

    public void noteJobFinishLocked(String name, int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        getUidStatsLocked(uid).noteStopJobLocked(name, elapsedRealtime);
        if (this.mActiveEvents.updateState(GL10.GL_LIGHT6, name, uid, NETWORK_STATS_LAST)) {
            addHistoryEventLocked(elapsedRealtime, uptime, GL10.GL_LIGHT6, name, uid);
        }
    }

    public void noteAlarmStartLocked(String name, int uid) {
        if (this.mRecordAllHistory) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            if (this.mActiveEvents.updateState(32781, name, uid, NETWORK_STATS_LAST)) {
                addHistoryEventLocked(elapsedRealtime, uptime, 32781, name, uid);
            }
        }
    }

    public void noteAlarmFinishLocked(String name, int uid) {
        if (this.mRecordAllHistory) {
            uid = mapUid(uid);
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            if (this.mActiveEvents.updateState(16397, name, uid, NETWORK_STATS_LAST)) {
                addHistoryEventLocked(elapsedRealtime, uptime, 16397, name, uid);
            }
        }
    }

    private void requestWakelockCpuUpdate() {
        if (!this.mHandler.hasMessages(NUM_WIFI_TX_LEVELS)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(NUM_WIFI_TX_LEVELS), DELAY_UPDATE_WAKELOCKS);
        }
    }

    private void requestImmediateCpuUpdate() {
        this.mHandler.removeMessages(NUM_WIFI_TX_LEVELS);
        this.mHandler.sendEmptyMessage(NUM_WIFI_TX_LEVELS);
    }

    public void setRecordAllHistoryLocked(boolean enabled) {
        this.mRecordAllHistory = enabled;
        HashMap<String, SparseIntArray> active;
        long mSecRealtime;
        long mSecUptime;
        SparseIntArray uids;
        int j;
        if (enabled) {
            active = this.mActiveEvents.getStateForEvent(NUM_WIFI_TX_LEVELS);
            if (active != null) {
                mSecRealtime = this.mClocks.elapsedRealtime();
                mSecUptime = this.mClocks.uptimeMillis();
                for (Map.Entry<String, SparseIntArray> ent : active.entrySet()) {
                    uids = (SparseIntArray) ent.getValue();
                    for (j = NETWORK_STATS_LAST; j < uids.size(); j += NUM_WIFI_TX_LEVELS) {
                        addHistoryEventLocked(mSecRealtime, mSecUptime, 32769, (String) ent.getKey(), uids.keyAt(j));
                    }
                }
                return;
            }
            return;
        }
        this.mActiveEvents.removeEvents(5);
        this.mActiveEvents.removeEvents(13);
        active = this.mActiveEvents.getStateForEvent(NUM_WIFI_TX_LEVELS);
        if (active != null) {
            mSecRealtime = this.mClocks.elapsedRealtime();
            mSecUptime = this.mClocks.uptimeMillis();
            for (Map.Entry<String, SparseIntArray> ent2 : active.entrySet()) {
                uids = (SparseIntArray) ent2.getValue();
                for (j = NETWORK_STATS_LAST; j < uids.size(); j += NUM_WIFI_TX_LEVELS) {
                    addHistoryEventLocked(mSecRealtime, mSecUptime, GL10.GL_LIGHT1, (String) ent2.getKey(), uids.keyAt(j));
                }
            }
        }
    }

    public void setNoAutoReset(boolean enabled) {
        this.mNoAutoReset = enabled;
    }

    public void noteStartWakeLocked(int uid, int pid, String name, String historyName, int type, boolean unimportantForLogging, long elapsedRealtime, long uptime) {
        uid = mapUid(uid);
        if (type == 0) {
            aggregateLastWakeupUptimeLocked(uptime);
            if (historyName == null) {
                historyName = name;
            }
            if (this.mRecordAllHistory && this.mActiveEvents.updateState(32773, historyName, uid, NETWORK_STATS_LAST)) {
                addHistoryEventLocked(elapsedRealtime, uptime, 32773, historyName, uid);
            }
            HistoryTag historyTag;
            if (this.mWakeLockNesting == 0) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                this.mHistoryCur.wakelockTag = this.mHistoryCur.localWakelockTag;
                historyTag = this.mHistoryCur.wakelockTag;
                this.mInitialAcquireWakeName = historyName;
                historyTag.string = historyName;
                historyTag = this.mHistoryCur.wakelockTag;
                this.mInitialAcquireWakeUid = uid;
                historyTag.uid = uid;
                this.mWakeLockImportant = unimportantForLogging ? USE_OLD_HISTORY : true;
                addHistoryRecordLocked(elapsedRealtime, uptime);
            } else if (!(this.mWakeLockImportant || unimportantForLogging || this.mHistoryLastWritten.cmd != null)) {
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
            this.mWakeLockNesting += NUM_WIFI_TX_LEVELS;
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
                if (this.mActiveEvents.updateState(GL10.GL_LIGHT5, historyName, uid, NETWORK_STATS_LAST)) {
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
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteStartWakeLocked(ws.get(i), pid, name, historyName, type, unimportantForLogging, elapsedRealtime, uptime);
        }
    }

    public void noteChangeWakelockFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) {
        int i;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int NN = newWs.size();
        for (i = NETWORK_STATS_LAST; i < NN; i += NUM_WIFI_TX_LEVELS) {
            noteStartWakeLocked(newWs.get(i), newPid, newName, newHistoryName, newType, newUnimportantForLogging, elapsedRealtime, uptime);
        }
        int NO = ws.size();
        for (i = NETWORK_STATS_LAST; i < NO; i += NUM_WIFI_TX_LEVELS) {
            noteStopWakeLocked(ws.get(i), pid, name, historyName, type, elapsedRealtime, uptime);
        }
    }

    public void noteStopWakeFromSourceLocked(WorkSource ws, int pid, String name, String historyName, int type) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteStopWakeLocked(ws.get(i), pid, name, historyName, type, elapsedRealtime, uptime);
        }
    }

    void aggregateLastWakeupUptimeLocked(long uptimeMs) {
        if (this.mLastWakeupReason != null) {
            getWakeupReasonTimerLocked(this.mLastWakeupReason).add(1000 * (uptimeMs - this.mLastWakeupUptimeMs), NUM_WIFI_TX_LEVELS);
            this.mLastWakeupReason = null;
        }
    }

    public void noteWakeupReasonLocked(String reason) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        aggregateLastWakeupUptimeLocked(uptime);
        this.mHistoryCur.wakeReasonTag = this.mHistoryCur.localWakeReasonTag;
        this.mHistoryCur.wakeReasonTag.string = reason;
        this.mHistoryCur.wakeReasonTag.uid = NETWORK_STATS_LAST;
        this.mLastWakeupReason = reason;
        this.mLastWakeupUptimeMs = uptime;
        addHistoryRecordLocked(elapsedRealtime, uptime);
    }

    public boolean startAddingCpuLocked() {
        this.mHandler.removeMessages(NUM_WIFI_TX_LEVELS);
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
            historyItem.states |= DELTA_EVENT_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mSensorNesting += NUM_WIFI_TX_LEVELS;
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
            historyItem.states |= EditorInfo.IME_FLAG_NO_ACCESSORY_ACTION;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mGpsNesting += NUM_WIFI_TX_LEVELS;
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
        if (this.mScreenState != state) {
            recordDailyStatsIfNeededLocked(true);
            int oldState = this.mScreenState;
            this.mScreenState = state;
            if (state != 0) {
                int stepState = state - 1;
                if (stepState < 4) {
                    this.mModStepMode |= (this.mCurStepMode & STATE_BATTERY_PLUG_MASK) ^ stepState;
                    this.mCurStepMode = (this.mCurStepMode & -4) | stepState;
                } else {
                    Slog.wtf(TAG, "Unexpected screen state: " + state);
                }
            }
            long elapsedRealtime;
            long uptime;
            HistoryItem historyItem;
            if (state == NETWORK_STATS_DELTA) {
                elapsedRealtime = this.mClocks.elapsedRealtime();
                uptime = this.mClocks.uptimeMillis();
                historyItem = this.mHistoryCur;
                historyItem.states |= DELTA_STATE_FLAG;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mScreenOnTimer.startRunningLocked(elapsedRealtime);
                if (this.mScreenBrightnessBin >= 0) {
                    this.mScreenBrightnessTimer[this.mScreenBrightnessBin].startRunningLocked(elapsedRealtime);
                }
                updateTimeBasesLocked(this.mOnBatteryTimeBase.isRunning(), USE_OLD_HISTORY, this.mClocks.uptimeMillis() * 1000, 1000 * elapsedRealtime);
                noteStartWakeLocked(-1, -1, "screen", null, NETWORK_STATS_LAST, USE_OLD_HISTORY, elapsedRealtime, uptime);
                if (this.mOnBatteryInternal) {
                    updateDischargeScreenLevelsLocked(USE_OLD_HISTORY, true);
                }
            } else if (oldState == NETWORK_STATS_DELTA) {
                elapsedRealtime = this.mClocks.elapsedRealtime();
                uptime = this.mClocks.uptimeMillis();
                historyItem = this.mHistoryCur;
                historyItem.states &= -1048577;
                addHistoryRecordLocked(elapsedRealtime, uptime);
                this.mScreenOnTimer.stopRunningLocked(elapsedRealtime);
                if (this.mScreenBrightnessBin >= 0) {
                    this.mScreenBrightnessTimer[this.mScreenBrightnessBin].stopRunningLocked(elapsedRealtime);
                }
                noteStopWakeLocked(-1, -1, "screen", "screen", NETWORK_STATS_LAST, elapsedRealtime, uptime);
                updateTimeBasesLocked(this.mOnBatteryTimeBase.isRunning(), true, this.mClocks.uptimeMillis() * 1000, 1000 * elapsedRealtime);
                if (this.mOnBatteryInternal) {
                    updateDischargeScreenLevelsLocked(true, USE_OLD_HISTORY);
                }
            }
        }
    }

    public void noteScreenBrightnessLocked(int brightness) {
        int bin = brightness / 51;
        if (bin < 0) {
            bin = NETWORK_STATS_LAST;
        } else if (bin >= 5) {
            bin = 4;
        }
        if (this.mScreenBrightnessBin != bin) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mHistoryCur.states = (this.mHistoryCur.states & -8) | (bin << NETWORK_STATS_LAST);
            addHistoryRecordLocked(elapsedRealtime, uptime);
            if (this.mScreenState == NETWORK_STATS_DELTA) {
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
        this.mNumConnectivityChange += NUM_WIFI_TX_LEVELS;
    }

    public void noteMobileRadioPowerState(int powerState, long timestampNs, int uid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mMobileRadioPowerState != powerState) {
            long realElapsedRealtimeMs;
            boolean active = powerState != NETWORK_STATS_DELTA ? powerState == STATE_BATTERY_PLUG_MASK ? true : USE_OLD_HISTORY : true;
            HistoryItem historyItem;
            if (active) {
                realElapsedRealtimeMs = timestampNs / TimeUtils.NANOS_PER_MS;
                this.mMobileRadioActiveStartTime = realElapsedRealtimeMs;
                historyItem = this.mHistoryCur;
                historyItem.states |= EditorInfo.IME_FLAG_NO_FULLSCREEN;
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
                return;
            }
            this.mMobileRadioActiveTimer.stopRunningLocked(realElapsedRealtimeMs);
            updateMobileRadioStateLocked(realElapsedRealtimeMs, null);
            this.mMobileRadioActivePerAppTimer.stopRunningLocked(realElapsedRealtimeMs);
        }
    }

    public void notePowerSaveMode(boolean enabled) {
        if (this.mPowerSaveModeEnabled != enabled) {
            int stepState = enabled ? 4 : NETWORK_STATS_LAST;
            this.mModStepMode |= (this.mCurStepMode & 4) ^ stepState;
            this.mCurStepMode = (this.mCurStepMode & -5) | stepState;
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mPowerSaveModeEnabled = enabled;
            HistoryItem historyItem;
            if (enabled) {
                historyItem = this.mHistoryCur;
                historyItem.states2 |= RtlSpacingHelper.UNDEFINED;
                this.mPowerSaveModeEnabledTimer.startRunningLocked(elapsedRealtime);
            } else {
                historyItem = this.mHistoryCur;
                historyItem.states2 &= HwBootFail.STAGE_BOOT_SUCCESS;
                this.mPowerSaveModeEnabledTimer.stopRunningLocked(elapsedRealtime);
            }
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
    }

    public void noteDeviceIdleModeLocked(int mode, String activeReason, int activeUid) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        boolean nowIdling = mode == NETWORK_STATS_DELTA ? true : USE_OLD_HISTORY;
        if (this.mDeviceIdling && !nowIdling && activeReason == null) {
            nowIdling = true;
        }
        boolean nowLightIdling = mode == NUM_WIFI_TX_LEVELS ? true : USE_OLD_HISTORY;
        if (this.mDeviceLightIdling && !nowLightIdling && !nowIdling && activeReason == null) {
            nowLightIdling = true;
        }
        if (activeReason != null && (this.mDeviceIdling || this.mDeviceLightIdling)) {
            addHistoryEventLocked(elapsedRealtime, uptime, MAX_DAILY_ITEMS, activeReason, activeUid);
        }
        if (this.mDeviceIdling != nowIdling) {
            this.mDeviceIdling = nowIdling;
            int stepState = nowIdling ? 8 : NETWORK_STATS_LAST;
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
            if (this.mDeviceIdleMode == NUM_WIFI_TX_LEVELS) {
                if (lastDuration > this.mLongestLightIdleTime) {
                    this.mLongestLightIdleTime = lastDuration;
                }
                this.mDeviceIdleModeLightTimer.stopRunningLocked(elapsedRealtime);
            } else if (this.mDeviceIdleMode == NETWORK_STATS_DELTA) {
                if (lastDuration > this.mLongestFullIdleTime) {
                    this.mLongestFullIdleTime = lastDuration;
                }
                this.mDeviceIdleModeFullTimer.stopRunningLocked(elapsedRealtime);
            }
            if (mode == NUM_WIFI_TX_LEVELS) {
                this.mDeviceIdleModeLightTimer.startRunningLocked(elapsedRealtime);
            } else if (mode == NETWORK_STATS_DELTA) {
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
        addHistoryEventLocked(this.mClocks.elapsedRealtime(), this.mClocks.uptimeMillis(), 12, pkgName, NETWORK_STATS_LAST);
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
            historyItem.states2 |= DELTA_EVENT_FLAG;
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
            this.mPhoneOn = USE_OLD_HISTORY;
            this.mPhoneOnTimer.stopRunningLocked(elapsedRealtime);
        }
    }

    void stopAllPhoneSignalStrengthTimersLocked(int except) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        for (int i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            if (i != except) {
                while (this.mPhoneSignalStrengthsTimer[i].isRunningLocked()) {
                    this.mPhoneSignalStrengthsTimer[i].stopRunningLocked(elapsedRealtime);
                }
            }
        }
    }

    private int fixPhoneServiceState(int state, int signalBin) {
        if (this.mPhoneSimStateRaw == NUM_WIFI_TX_LEVELS && state == NUM_WIFI_TX_LEVELS && signalBin > 0) {
            return NETWORK_STATS_LAST;
        }
        return state;
    }

    private void updateAllPhoneStateLocked(int state, int simState, int strengthBin) {
        boolean scanning = USE_OLD_HISTORY;
        boolean newHistory = USE_OLD_HISTORY;
        this.mPhoneServiceStateRaw = state;
        this.mPhoneSimStateRaw = simState;
        this.mPhoneSignalStrengthBinRaw = strengthBin;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (simState == NUM_WIFI_TX_LEVELS && state == NUM_WIFI_TX_LEVELS && strengthBin > 0) {
            state = NETWORK_STATS_LAST;
        }
        if (state == STATE_BATTERY_PLUG_MASK) {
            strengthBin = -1;
        } else if (state != 0 && state == NUM_WIFI_TX_LEVELS) {
            scanning = true;
            strengthBin = NETWORK_STATS_LAST;
            if (!this.mPhoneSignalScanningTimer.isRunningLocked()) {
                HistoryItem historyItem = this.mHistoryCur;
                historyItem.states |= DELTA_STATE2_FLAG;
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
                this.mHistoryCur.states = (this.mHistoryCur.states & -57) | (strengthBin << STATE_BATTERY_PLUG_MASK);
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
        int bin = NETWORK_STATS_LAST;
        if (hasData) {
            switch (dataType) {
                case NUM_WIFI_TX_LEVELS /*1*/:
                    bin = NUM_WIFI_TX_LEVELS;
                    break;
                case NETWORK_STATS_DELTA /*2*/:
                    bin = NETWORK_STATS_DELTA;
                    break;
                case STATE_BATTERY_PLUG_MASK /*3*/:
                    bin = STATE_BATTERY_PLUG_MASK;
                    break;
                case HwCfgFilePolicy.CUST /*4*/:
                    bin = 4;
                    break;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    bin = 5;
                    break;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    bin = 6;
                    break;
                case STATE_BATTERY_STATUS_MASK /*7*/:
                    bin = STATE_BATTERY_STATUS_MASK;
                    break;
                case PGSdk.TYPE_VIDEO /*8*/:
                    bin = 8;
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    bin = 9;
                    break;
                case MAX_DAILY_ITEMS /*10*/:
                    bin = MAX_DAILY_ITEMS;
                    break;
                case PGSdk.TYPE_IM /*11*/:
                    bin = 11;
                    break;
                case PGSdk.TYPE_MUSIC /*12*/:
                    bin = 12;
                    break;
                case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                    bin = 13;
                    break;
                case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                    bin = 14;
                    break;
                case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
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
            historyItem.states2 |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mWifiOn = true;
            this.mWifiOnTimer.startRunningLocked(elapsedRealtime);
            scheduleSyncExternalStatsLocked("wifi-off", NETWORK_STATS_DELTA);
        }
    }

    public void noteWifiOffLocked() {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiOn) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -268435457;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mWifiOn = USE_OLD_HISTORY;
            this.mWifiOnTimer.stopRunningLocked(elapsedRealtime);
            scheduleSyncExternalStatsLocked("wifi-on", NETWORK_STATS_DELTA);
        }
    }

    public void noteAudioOnLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mAudioOnNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states |= DELTA_WAKELOCK_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mAudioOnTimer.startRunningLocked(elapsedRealtime);
        }
        this.mAudioOnNesting += NUM_WIFI_TX_LEVELS;
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
            historyItem.states2 |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mVideoOnTimer.startRunningLocked(elapsedRealtime);
        }
        this.mVideoOnNesting += NUM_WIFI_TX_LEVELS;
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
            this.mAudioOnNesting = NETWORK_STATS_LAST;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states &= -4194305;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mAudioOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = NETWORK_STATS_LAST; i < this.mUidStats.size(); i += NUM_WIFI_TX_LEVELS) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetAudioLocked(elapsedRealtime);
            }
        }
    }

    public void noteResetVideoLocked() {
        if (this.mVideoOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mAudioOnNesting = NETWORK_STATS_LAST;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -1073741825;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mVideoOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = NETWORK_STATS_LAST; i < this.mUidStats.size(); i += NUM_WIFI_TX_LEVELS) {
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
        this.mFlashlightOnNesting = i + NUM_WIFI_TX_LEVELS;
        if (i == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= EditorInfo.IME_FLAG_NAVIGATE_NEXT;
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
        this.mCameraOnNesting = i + NUM_WIFI_TX_LEVELS;
        if (i == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= DELTA_STATE2_FLAG;
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
            this.mCameraOnNesting = NETWORK_STATS_LAST;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -2097153;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mCameraOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = NETWORK_STATS_LAST; i < this.mUidStats.size(); i += NUM_WIFI_TX_LEVELS) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetCameraLocked(elapsedRealtime);
            }
        }
    }

    public void noteResetFlashlightLocked() {
        if (this.mFlashlightOnNesting > 0) {
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            long uptime = this.mClocks.uptimeMillis();
            this.mFlashlightOnNesting = NETWORK_STATS_LAST;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -134217729;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mFlashlightOnTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = NETWORK_STATS_LAST; i < this.mUidStats.size(); i += NUM_WIFI_TX_LEVELS) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetFlashlightLocked(elapsedRealtime);
            }
        }
    }

    private void noteBluetoothScanStartedLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long uptime = SystemClock.uptimeMillis();
        if (this.mBluetoothScanNesting == 0) {
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 |= DELTA_STATE_FLAG;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.startRunningLocked(elapsedRealtime);
        }
        this.mBluetoothScanNesting += NUM_WIFI_TX_LEVELS;
        getUidStatsLocked(uid).noteBluetoothScanStartedLocked(elapsedRealtime);
    }

    public void noteBluetoothScanStartedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteBluetoothScanStartedLocked(ws.get(i));
        }
    }

    private void noteBluetoothScanStoppedLocked(int uid) {
        uid = mapUid(uid);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long uptime = SystemClock.uptimeMillis();
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
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteBluetoothScanStoppedLocked(ws.get(i));
        }
    }

    public void noteResetBluetoothScanLocked() {
        if (this.mBluetoothScanNesting > 0) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long uptime = SystemClock.uptimeMillis();
            this.mBluetoothScanNesting = NETWORK_STATS_LAST;
            HistoryItem historyItem = this.mHistoryCur;
            historyItem.states2 &= -1048577;
            addHistoryRecordLocked(elapsedRealtime, uptime);
            this.mBluetoothScanTimer.stopAllRunningLocked(elapsedRealtime);
            for (int i = NETWORK_STATS_LAST; i < this.mUidStats.size(); i += NUM_WIFI_TX_LEVELS) {
                ((Uid) this.mUidStats.valueAt(i)).noteResetBluetoothScanLocked(elapsedRealtime);
            }
        }
    }

    public void noteWifiRadioPowerState(int powerState, long timestampNs) {
        boolean active = true;
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        long uptime = this.mClocks.uptimeMillis();
        if (this.mWifiRadioPowerState != powerState) {
            if (!(powerState == NETWORK_STATS_DELTA || powerState == STATE_BATTERY_PLUG_MASK)) {
                active = USE_OLD_HISTORY;
            }
            HistoryItem historyItem;
            if (active) {
                historyItem = this.mHistoryCur;
                historyItem.states |= EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS;
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
        historyItem.states2 |= EditorInfo.IME_FLAG_NO_ACCESSORY_ACTION;
        addHistoryRecordLocked(elapsedRealtime, uptime);
        this.mGlobalWifiRunning = true;
        this.mGlobalWifiRunningTimer.startRunningLocked(elapsedRealtime);
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            getUidStatsLocked(mapUid(ws.get(i))).noteWifiRunningLocked(elapsedRealtime);
        }
        scheduleSyncExternalStatsLocked("wifi-running", NETWORK_STATS_DELTA);
    }

    public void noteWifiRunningChangedLocked(WorkSource oldWs, WorkSource newWs) {
        if (this.mGlobalWifiRunning) {
            int i;
            long elapsedRealtime = this.mClocks.elapsedRealtime();
            int N = oldWs.size();
            for (i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
                getUidStatsLocked(mapUid(oldWs.get(i))).noteWifiStoppedLocked(elapsedRealtime);
            }
            N = newWs.size();
            for (i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
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
            this.mGlobalWifiRunning = USE_OLD_HISTORY;
            this.mGlobalWifiRunningTimer.stopRunningLocked(elapsedRealtime);
            int N = ws.size();
            for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
                getUidStatsLocked(mapUid(ws.get(i))).noteWifiStoppedLocked(elapsedRealtime);
            }
            scheduleSyncExternalStatsLocked("wifi-stopped", NETWORK_STATS_DELTA);
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
            scheduleSyncExternalStatsLocked("wifi-state", NETWORK_STATS_DELTA);
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
            this.mHistoryCur.states2 = (this.mHistoryCur.states2 & -16) | (supplState << NETWORK_STATS_LAST);
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
    }

    void stopAllWifiSignalStrengthTimersLocked(int except) {
        long elapsedRealtime = this.mClocks.elapsedRealtime();
        for (int i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
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
            historyItem.states |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiFullLockNesting += NUM_WIFI_TX_LEVELS;
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
            historyItem.states |= EditorInfo.IME_FLAG_NAVIGATE_NEXT;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiScanNesting += NUM_WIFI_TX_LEVELS;
        getUidStatsLocked(uid).noteWifiScanStartedLocked(elapsedRealtime);
        LogPower.push(LogPower.WIFI_SCAN_START, Integer.toString(uid));
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
        LogPower.push(LogPower.WIFI_SCAN_END, Integer.toString(uid));
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
            historyItem.states |= Protocol.BASE_SYSTEM_RESERVED;
            addHistoryRecordLocked(elapsedRealtime, uptime);
        }
        this.mWifiMulticastNesting += NUM_WIFI_TX_LEVELS;
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
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteFullWifiLockAcquiredLocked(ws.get(i));
        }
    }

    public void noteFullWifiLockReleasedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteFullWifiLockReleasedLocked(ws.get(i));
        }
    }

    public void noteWifiScanStartedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteWifiScanStartedLocked(ws.get(i));
        }
    }

    public void noteWifiScanStoppedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteWifiScanStoppedLocked(ws.get(i));
        }
    }

    public void noteWifiBatchedScanStartedFromSourceLocked(WorkSource ws, int csph) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteWifiBatchedScanStartedLocked(ws.get(i), csph);
        }
    }

    public void noteWifiBatchedScanStoppedFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteWifiBatchedScanStoppedLocked(ws.get(i));
        }
    }

    public void noteWifiMulticastEnabledFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteWifiMulticastEnabledLocked(ws.get(i));
        }
    }

    public void noteWifiMulticastDisabledFromSourceLocked(WorkSource ws) {
        int N = ws.size();
        for (int i = NETWORK_STATS_LAST; i < N; i += NUM_WIFI_TX_LEVELS) {
            noteWifiMulticastDisabledLocked(ws.get(i));
        }
    }

    private static String[] includeInStringArray(String[] array, String str) {
        if (ArrayUtils.indexOf(array, str) >= 0) {
            return array;
        }
        String[] newArray = new String[(array.length + NUM_WIFI_TX_LEVELS)];
        System.arraycopy(array, NETWORK_STATS_LAST, newArray, NETWORK_STATS_LAST, array.length);
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
            System.arraycopy(array, NETWORK_STATS_LAST, newArray, NETWORK_STATS_LAST, index);
        }
        if (index < array.length - 1) {
            System.arraycopy(array, index + NUM_WIFI_TX_LEVELS, newArray, index, (array.length - index) - 1);
        }
        return newArray;
    }

    public void noteNetworkInterfaceTypeLocked(String iface, int networkType) {
        if (!TextUtils.isEmpty(iface)) {
            if (ConnectivityManager.isNetworkTypeMobile(networkType)) {
                this.mMobileIfaces = includeInStringArray(this.mMobileIfaces, iface);
            } else {
                this.mMobileIfaces = excludeFromStringArray(this.mMobileIfaces, iface);
            }
            if (ConnectivityManager.isNetworkTypeWifi(networkType)) {
                this.mWifiIfaces = includeInStringArray(this.mWifiIfaces, iface);
            } else {
                this.mWifiIfaces = excludeFromStringArray(this.mWifiIfaces, iface);
            }
        }
    }

    public void noteNetworkStatsEnabledLocked() {
        updateMobileRadioStateLocked(this.mClocks.elapsedRealtime(), null);
        updateWifiStateLocked(null);
    }

    public long getScreenOnTime(long elapsedRealtimeUs, int which) {
        if (this.mScreenOnTimer != null) {
            return this.mScreenOnTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
        }
        return 0;
    }

    public int getScreenOnCount(int which) {
        return this.mScreenOnTimer.getCountLocked(which);
    }

    public long getScreenBrightnessTime(int brightnessBin, long elapsedRealtimeUs, int which) {
        return this.mScreenBrightnessTimer[brightnessBin].getTotalTimeLocked(elapsedRealtimeUs, which);
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
        return this.mPowerSaveModeEnabledTimer.getCountLocked(which);
    }

    public long getDeviceIdleModeTime(int mode, long elapsedRealtimeUs, int which) {
        switch (mode) {
            case NUM_WIFI_TX_LEVELS /*1*/:
                return this.mDeviceIdleModeLightTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            case NETWORK_STATS_DELTA /*2*/:
                return this.mDeviceIdleModeFullTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            default:
                return 0;
        }
    }

    public int getDeviceIdleModeCount(int mode, int which) {
        switch (mode) {
            case NUM_WIFI_TX_LEVELS /*1*/:
                return this.mDeviceIdleModeLightTimer.getCountLocked(which);
            case NETWORK_STATS_DELTA /*2*/:
                return this.mDeviceIdleModeFullTimer.getCountLocked(which);
            default:
                return NETWORK_STATS_LAST;
        }
    }

    public long getLongestDeviceIdleModeTime(int mode) {
        switch (mode) {
            case NUM_WIFI_TX_LEVELS /*1*/:
                return this.mLongestLightIdleTime;
            case NETWORK_STATS_DELTA /*2*/:
                return this.mLongestFullIdleTime;
            default:
                return 0;
        }
    }

    public long getDeviceIdlingTime(int mode, long elapsedRealtimeUs, int which) {
        switch (mode) {
            case NUM_WIFI_TX_LEVELS /*1*/:
                return this.mDeviceLightIdlingTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            case NETWORK_STATS_DELTA /*2*/:
                return this.mDeviceIdlingTimer.getTotalTimeLocked(elapsedRealtimeUs, which);
            default:
                return 0;
        }
    }

    public int getDeviceIdlingCount(int mode, int which) {
        switch (mode) {
            case NUM_WIFI_TX_LEVELS /*1*/:
                return this.mDeviceLightIdlingTimer.getCountLocked(which);
            case NETWORK_STATS_DELTA /*2*/:
                return this.mDeviceIdlingTimer.getCountLocked(which);
            default:
                return NETWORK_STATS_LAST;
        }
    }

    public int getNumConnectivityChange(int which) {
        int val = this.mNumConnectivityChange;
        if (which == NUM_WIFI_TX_LEVELS) {
            return val - this.mLoadedNumConnectivityChange;
        }
        if (which == NETWORK_STATS_DELTA) {
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
        return NETWORK_STATS_LAST;
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
        return VERSION;
    }

    public boolean getIsOnBattery() {
        return this.mOnBattery;
    }

    public SparseArray<? extends android.os.BatteryStats.Uid> getUidStats() {
        return this.mUidStats;
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
        this.mHaveBatteryLevel = USE_OLD_HISTORY;
        this.mRecordingHistory = USE_OLD_HISTORY;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new HistoryItem();
        this.mHistoryLastLastWritten = new HistoryItem();
        this.mHistoryReadTmp = new HistoryItem();
        this.mHistoryAddTmp = new HistoryItem();
        this.mHistoryTagPool = new HashMap();
        this.mNextHistoryTagIdx = NETWORK_STATS_LAST;
        this.mNumHistoryTagChars = NETWORK_STATS_LAST;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = USE_OLD_HISTORY;
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
        this.mScreenState = NETWORK_STATS_LAST;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[17];
        this.mNetworkByteActivityCounters = new LongSamplingCounter[6];
        this.mNetworkPacketActivityCounters = new LongSamplingCounter[6];
        this.mHasWifiReporting = USE_OLD_HISTORY;
        this.mHasBluetoothReporting = USE_OLD_HISTORY;
        this.mHasModemReporting = USE_OLD_HISTORY;
        this.mWifiState = -1;
        this.mWifiStateTimer = new StopwatchTimer[8];
        this.mWifiSupplState = -1;
        this.mWifiSupplStateTimer = new StopwatchTimer[13];
        this.mWifiSignalStrengthBin = -1;
        this.mWifiSignalStrengthsTimer = new StopwatchTimer[5];
        this.mMobileRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mWifiRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mCharging = true;
        this.mInitStepMode = NETWORK_STATS_LAST;
        this.mCurStepMode = NETWORK_STATS_LAST;
        this.mModStepMode = NETWORK_STATS_LAST;
        this.mDischargeStepTracker = new LevelStepTracker(MAX_LEVEL_STEPS);
        this.mDailyDischargeStepTracker = new LevelStepTracker(StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION);
        this.mChargeStepTracker = new LevelStepTracker(MAX_LEVEL_STEPS);
        this.mDailyChargeStepTracker = new LevelStepTracker(StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mTmpNetworkStatsEntry = new Entry();
        this.mKernelWakelockStats = new HashMap();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap();
        this.mChangedStates = NETWORK_STATS_LAST;
        this.mChangedStates2 = NETWORK_STATS_LAST;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = NETWORK_STATS_LAST;
        this.mWifiScanNesting = NETWORK_STATS_LAST;
        this.mWifiMulticastNesting = NETWORK_STATS_LAST;
        this.mMobileIfaces = EmptyArray.STRING;
        this.mWifiIfaces = EmptyArray.STRING;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
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
        this.mStartCount += NUM_WIFI_TX_LEVELS;
        this.mScreenOnTimer = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mScreenBrightnessTimer[i] = new StopwatchTimer(this.mClocks, null, -100 - i, null, this.mOnBatteryTimeBase);
        }
        this.mInteractiveTimer = new StopwatchTimer(this.mClocks, null, -10, null, this.mOnBatteryTimeBase);
        this.mPowerSaveModeEnabledTimer = new StopwatchTimer(this.mClocks, null, -2, null, this.mOnBatteryTimeBase);
        this.mDeviceIdleModeLightTimer = new StopwatchTimer(this.mClocks, null, -11, null, this.mOnBatteryTimeBase);
        this.mDeviceIdleModeFullTimer = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase);
        this.mDeviceLightIdlingTimer = new StopwatchTimer(this.mClocks, null, -15, null, this.mOnBatteryTimeBase);
        this.mDeviceIdlingTimer = new StopwatchTimer(this.mClocks, null, -12, null, this.mOnBatteryTimeBase);
        this.mPhoneOnTimer = new StopwatchTimer(this.mClocks, null, -3, null, this.mOnBatteryTimeBase);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -200 - i, null, this.mOnBatteryTimeBase);
        }
        this.mPhoneSignalScanningTimer = new StopwatchTimer(this.mClocks, null, -199, null, this.mOnBatteryTimeBase);
        for (i = NETWORK_STATS_LAST; i < 17; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneDataConnectionsTimer[i] = new StopwatchTimer(this.mClocks, null, -300 - i, null, this.mOnBatteryTimeBase);
        }
        for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
            this.mNetworkByteActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase);
            this.mNetworkPacketActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase);
        }
        this.mWifiActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, NUM_WIFI_TX_LEVELS);
        this.mBluetoothActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, NUM_WIFI_TX_LEVELS);
        this.mModemActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 5);
        this.mMobileRadioActiveTimer = new StopwatchTimer(this.mClocks, null, -400, null, this.mOnBatteryTimeBase);
        this.mMobileRadioActivePerAppTimer = new StopwatchTimer(this.mClocks, null, -401, null, this.mOnBatteryTimeBase);
        this.mMobileRadioActiveAdjustedTime = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mMobileRadioActiveUnknownTime = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mMobileRadioActiveUnknownCount = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mWifiOnTimer = new StopwatchTimer(this.mClocks, null, -4, null, this.mOnBatteryTimeBase);
        this.mGlobalWifiRunningTimer = new StopwatchTimer(this.mClocks, null, -5, null, this.mOnBatteryTimeBase);
        for (i = NETWORK_STATS_LAST; i < 8; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiStateTimer[i] = new StopwatchTimer(this.mClocks, null, -600 - i, null, this.mOnBatteryTimeBase);
        }
        for (i = NETWORK_STATS_LAST; i < 13; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSupplStateTimer[i] = new StopwatchTimer(this.mClocks, null, -700 - i, null, this.mOnBatteryTimeBase);
        }
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -800 - i, null, this.mOnBatteryTimeBase);
        }
        this.mAudioOnTimer = new StopwatchTimer(this.mClocks, null, -7, null, this.mOnBatteryTimeBase);
        this.mVideoOnTimer = new StopwatchTimer(this.mClocks, null, -8, null, this.mOnBatteryTimeBase);
        this.mFlashlightOnTimer = new StopwatchTimer(this.mClocks, null, -9, null, this.mOnBatteryTimeBase);
        this.mCameraOnTimer = new StopwatchTimer(this.mClocks, null, -13, null, this.mOnBatteryTimeBase);
        this.mBluetoothScanTimer = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase);
        this.mDischargeScreenOffCounter = new LongSamplingCounter(this.mOnBatteryScreenOffTimeBase);
        this.mDischargeCounter = new LongSamplingCounter(this.mOnBatteryTimeBase);
        this.mOnBatteryInternal = USE_OLD_HISTORY;
        this.mOnBattery = USE_OLD_HISTORY;
        initTimes(this.mClocks.uptimeMillis() * 1000, this.mClocks.elapsedRealtime() * 1000);
        String str = Build.ID;
        this.mEndPlatformVersion = str;
        this.mStartPlatformVersion = str;
        this.mDischargeStartLevel = NETWORK_STATS_LAST;
        this.mDischargeUnplugLevel = NETWORK_STATS_LAST;
        this.mDischargePlugLevel = -1;
        this.mDischargeCurrentLevel = NETWORK_STATS_LAST;
        this.mCurrentBatteryLevel = NETWORK_STATS_LAST;
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
        this.mHaveBatteryLevel = USE_OLD_HISTORY;
        this.mRecordingHistory = USE_OLD_HISTORY;
        this.mHistoryBuffer = Parcel.obtain();
        this.mHistoryLastWritten = new HistoryItem();
        this.mHistoryLastLastWritten = new HistoryItem();
        this.mHistoryReadTmp = new HistoryItem();
        this.mHistoryAddTmp = new HistoryItem();
        this.mHistoryTagPool = new HashMap();
        this.mNextHistoryTagIdx = NETWORK_STATS_LAST;
        this.mNumHistoryTagChars = NETWORK_STATS_LAST;
        this.mHistoryBufferLastPos = -1;
        this.mHistoryOverflow = USE_OLD_HISTORY;
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
        this.mScreenState = NETWORK_STATS_LAST;
        this.mScreenBrightnessBin = -1;
        this.mScreenBrightnessTimer = new StopwatchTimer[5];
        this.mPhoneSignalStrengthBin = -1;
        this.mPhoneSignalStrengthBinRaw = -1;
        this.mPhoneSignalStrengthsTimer = new StopwatchTimer[5];
        this.mPhoneDataConnectionType = -1;
        this.mPhoneDataConnectionsTimer = new StopwatchTimer[17];
        this.mNetworkByteActivityCounters = new LongSamplingCounter[6];
        this.mNetworkPacketActivityCounters = new LongSamplingCounter[6];
        this.mHasWifiReporting = USE_OLD_HISTORY;
        this.mHasBluetoothReporting = USE_OLD_HISTORY;
        this.mHasModemReporting = USE_OLD_HISTORY;
        this.mWifiState = -1;
        this.mWifiStateTimer = new StopwatchTimer[8];
        this.mWifiSupplState = -1;
        this.mWifiSupplStateTimer = new StopwatchTimer[13];
        this.mWifiSignalStrengthBin = -1;
        this.mWifiSignalStrengthsTimer = new StopwatchTimer[5];
        this.mMobileRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mWifiRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mCharging = true;
        this.mInitStepMode = NETWORK_STATS_LAST;
        this.mCurStepMode = NETWORK_STATS_LAST;
        this.mModStepMode = NETWORK_STATS_LAST;
        this.mDischargeStepTracker = new LevelStepTracker(MAX_LEVEL_STEPS);
        this.mDailyDischargeStepTracker = new LevelStepTracker(StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION);
        this.mChargeStepTracker = new LevelStepTracker(MAX_LEVEL_STEPS);
        this.mDailyChargeStepTracker = new LevelStepTracker(StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION);
        this.mDailyStartTime = 0;
        this.mNextMinDailyDeadline = 0;
        this.mNextMaxDailyDeadline = 0;
        this.mDailyItems = new ArrayList();
        this.mLastWriteTime = 0;
        this.mPhoneServiceState = -1;
        this.mPhoneServiceStateRaw = -1;
        this.mPhoneSimStateRaw = -1;
        this.mEstimatedBatteryCapacity = -1;
        this.mTmpNetworkStatsEntry = new Entry();
        this.mKernelWakelockStats = new HashMap();
        this.mLastWakeupReason = null;
        this.mLastWakeupUptimeMs = 0;
        this.mWakeupReasonStats = new HashMap();
        this.mChangedStates = NETWORK_STATS_LAST;
        this.mChangedStates2 = NETWORK_STATS_LAST;
        this.mInitialAcquireWakeUid = -1;
        this.mWifiFullLockNesting = NETWORK_STATS_LAST;
        this.mWifiScanNesting = NETWORK_STATS_LAST;
        this.mWifiMulticastNesting = NETWORK_STATS_LAST;
        this.mMobileIfaces = EmptyArray.STRING;
        this.mWifiIfaces = EmptyArray.STRING;
        this.mNetworkStatsFactory = new NetworkStatsFactory();
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

    public void setPowerProfile(PowerProfile profile) {
        synchronized (this) {
            this.mPowerProfile = profile;
            int numClusters = this.mPowerProfile.getNumCpuClusters();
            this.mKernelCpuSpeedReaders = new KernelCpuSpeedReader[numClusters];
            int firstCpuOfCluster = NETWORK_STATS_LAST;
            for (int i = NETWORK_STATS_LAST; i < numClusters; i += NUM_WIFI_TX_LEVELS) {
                this.mKernelCpuSpeedReaders[i] = new KernelCpuSpeedReader(firstCpuOfCluster, this.mPowerProfile.getNumSpeedStepsInCpuCluster(i));
                firstCpuOfCluster += this.mPowerProfile.getNumCoresInCpuCluster(i);
            }
            if (this.mEstimatedBatteryCapacity == -1) {
                this.mEstimatedBatteryCapacity = (int) this.mPowerProfile.getBatteryCapacity();
            }
        }
    }

    public void setCallback(BatteryCallback cb) {
        this.mCallback = cb;
    }

    public void setRadioScanningTimeout(long timeout) {
        if (this.mPhoneSignalScanningTimer != null) {
            this.mPhoneSignalScanningTimer.setTimeout(timeout);
        }
    }

    public void updateDailyDeadlineLocked() {
        long currentTime = System.currentTimeMillis();
        this.mDailyStartTime = currentTime;
        Calendar calDeadline = Calendar.getInstance();
        calDeadline.setTimeInMillis(currentTime);
        calDeadline.set(6, calDeadline.get(6) + NUM_WIFI_TX_LEVELS);
        calDeadline.set(14, NETWORK_STATS_LAST);
        calDeadline.set(13, NETWORK_STATS_LAST);
        calDeadline.set(12, NETWORK_STATS_LAST);
        calDeadline.set(11, NUM_WIFI_TX_LEVELS);
        this.mNextMinDailyDeadline = calDeadline.getTimeInMillis();
        calDeadline.set(11, STATE_BATTERY_PLUG_MASK);
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
        boolean hasData = USE_OLD_HISTORY;
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
            while (this.mDailyItems.size() > MAX_DAILY_ITEMS) {
                this.mDailyItems.remove(NETWORK_STATS_LAST);
            }
            ByteArrayOutputStream memStream = new ByteArrayOutputStream();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(memStream, StandardCharsets.UTF_8.name());
                writeDailyItemsLocked(out);
                BackgroundThread.getHandler().post(new AnonymousClass2(memStream));
            } catch (IOException e) {
            }
        }
    }

    private void writeDailyItemsLocked(XmlSerializer out) throws IOException {
        StringBuilder sb = new StringBuilder(64);
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, "daily-items");
        for (int i = NETWORK_STATS_LAST; i < this.mDailyItems.size(); i += NUM_WIFI_TX_LEVELS) {
            DailyItem dit = (DailyItem) this.mDailyItems.get(i);
            out.startTag(null, "item");
            out.attribute(null, "start", Long.toString(dit.mStartTime));
            out.attribute(null, "end", Long.toString(dit.mEndTime));
            writeDailyLevelSteps(out, "dis", dit.mDischargeSteps, sb);
            writeDailyLevelSteps(out, "chg", dit.mChargeSteps, sb);
            if (dit.mPackageChanges != null) {
                for (int j = NETWORK_STATS_LAST; j < dit.mPackageChanges.size(); j += NUM_WIFI_TX_LEVELS) {
                    PackageChange pc = (PackageChange) dit.mPackageChanges.get(j);
                    if (pc.mUpdate) {
                        out.startTag(null, "upd");
                        out.attribute(null, "pkg", pc.mPackageName);
                        out.attribute(null, "ver", Integer.toString(pc.mVersionCode));
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

    private void writeDailyLevelSteps(XmlSerializer out, String tag, LevelStepTracker steps, StringBuilder tmpBuilder) throws IOException {
        if (steps != null) {
            out.startTag(null, tag);
            out.attribute(null, "n", Integer.toString(steps.mNumStepDurations));
            for (int i = NETWORK_STATS_LAST; i < steps.mNumStepDurations; i += NUM_WIFI_TX_LEVELS) {
                out.startTag(null, "s");
                tmpBuilder.setLength(NETWORK_STATS_LAST);
                steps.encodeEntryAt(i, tmpBuilder);
                out.attribute(null, "v", tmpBuilder.toString());
                out.endTag(null, "s");
            }
            out.endTag(null, tag);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readDailyStatsLocked() {
        Slog.d(TAG, "Reading daily items from " + this.mDailyFile.getBaseFile());
        this.mDailyItems.clear();
        try {
            FileInputStream stream = this.mDailyFile.openRead();
            return;
        } catch (FileNotFoundException e) {
            return;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            readDailyItemsLocked(parser);
            try {
                stream.close();
            } catch (IOException e2) {
            }
        } catch (XmlPullParserException e3) {
        } catch (Throwable th) {
            try {
                stream.close();
            } catch (IOException e4) {
            }
        }
    }

    private void readDailyItemsLocked(XmlPullParser parser) {
        int type;
        do {
            try {
                type = parser.next();
                if (type == NETWORK_STATS_DELTA) {
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
        } while (type != NUM_WIFI_TX_LEVELS);
        if (type != NETWORK_STATS_DELTA) {
            throw new IllegalStateException("no start tag found");
        }
        int outerDepth = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == NUM_WIFI_TX_LEVELS) {
                return;
            }
            if (type == STATE_BATTERY_PLUG_MASK && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == STATE_BATTERY_PLUG_MASK || type == 4)) {
                if (parser.getName().equals("item")) {
                    readDailyItemTagLocked(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <daily-items>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readDailyItemTagLocked(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        DailyItem dit = new DailyItem();
        String attr = parser.getAttributeValue(null, "start");
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
            if (type == NUM_WIFI_TX_LEVELS || (type == STATE_BATTERY_PLUG_MASK && parser.getDepth() <= outerDepth)) {
                this.mDailyItems.add(dit);
            } else if (!(type == STATE_BATTERY_PLUG_MASK || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals("dis")) {
                    readDailyItemTagDetailsLocked(parser, dit, USE_OLD_HISTORY, "dis");
                } else if (tagName.equals("chg")) {
                    readDailyItemTagDetailsLocked(parser, dit, true, "chg");
                } else if (tagName.equals("upd")) {
                    if (dit.mPackageChanges == null) {
                        dit.mPackageChanges = new ArrayList();
                    }
                    pc = new PackageChange();
                    pc.mUpdate = true;
                    pc.mPackageName = parser.getAttributeValue(null, "pkg");
                    String verStr = parser.getAttributeValue(null, "ver");
                    pc.mVersionCode = verStr != null ? Integer.parseInt(verStr) : NETWORK_STATS_LAST;
                    dit.mPackageChanges.add(pc);
                    XmlUtils.skipCurrentTag(parser);
                } else if (tagName.equals("rem")) {
                    if (dit.mPackageChanges == null) {
                        dit.mPackageChanges = new ArrayList();
                    }
                    pc = new PackageChange();
                    pc.mUpdate = USE_OLD_HISTORY;
                    pc.mPackageName = parser.getAttributeValue(null, "pkg");
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        int i = NETWORK_STATS_LAST;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == NUM_WIFI_TX_LEVELS || (type == STATE_BATTERY_PLUG_MASK && parser.getDepth() <= outerDepth)) {
                steps.mNumStepDurations = i;
            } else if (!(type == STATE_BATTERY_PLUG_MASK || type == 4)) {
                if (!"s".equals(parser.getName())) {
                    Slog.w(TAG, "Unknown element under <" + tag + ">: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                } else if (i < num) {
                    String valueAttr = parser.getAttributeValue(null, "v");
                    if (valueAttr != null) {
                        steps.decodeEntryAt(i, valueAttr);
                        i += NUM_WIFI_TX_LEVELS;
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
            return USE_OLD_HISTORY;
        }
        this.mHistoryBuffer.setDataPosition(NETWORK_STATS_LAST);
        this.mHistoryReadTmp.clear();
        this.mReadOverflow = USE_OLD_HISTORY;
        this.mIteratingHistory = true;
        return true;
    }

    public boolean getNextOldHistoryLocked(HistoryItem out) {
        boolean end;
        if (this.mHistoryBuffer.dataPosition() >= this.mHistoryBuffer.dataSize()) {
            end = true;
        } else {
            end = USE_OLD_HISTORY;
        }
        if (!end) {
            int i;
            readHistoryDelta(this.mHistoryBuffer, this.mHistoryReadTmp);
            boolean z = this.mReadOverflow;
            if (this.mHistoryReadTmp.cmd == 6) {
                i = NUM_WIFI_TX_LEVELS;
            } else {
                i = NETWORK_STATS_LAST;
            }
            this.mReadOverflow = i | z;
        }
        HistoryItem cur = this.mHistoryIterator;
        if (cur == null) {
            if (!(this.mReadOverflow || end)) {
                Slog.w(TAG, "Old history ends before new history!");
            }
            return USE_OLD_HISTORY;
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
                new HistoryPrinter().printNextItem(pw, out, 0, USE_OLD_HISTORY, true);
                pw.println("New history:");
                new HistoryPrinter().printNextItem(pw, this.mHistoryReadTmp, 0, USE_OLD_HISTORY, true);
                pw.flush();
            }
        }
        return true;
    }

    public void finishIteratingOldHistoryLocked() {
        this.mIteratingHistory = USE_OLD_HISTORY;
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
            return USE_OLD_HISTORY;
        }
        this.mHistoryBuffer.setDataPosition(NETWORK_STATS_LAST);
        this.mReadOverflow = USE_OLD_HISTORY;
        this.mIteratingHistory = true;
        this.mReadHistoryStrings = new String[this.mHistoryTagPool.size()];
        this.mReadHistoryUids = new int[this.mHistoryTagPool.size()];
        this.mReadHistoryChars = NETWORK_STATS_LAST;
        for (Map.Entry<HistoryTag, Integer> ent : this.mHistoryTagPool.entrySet()) {
            HistoryTag tag = (HistoryTag) ent.getKey();
            int idx = ((Integer) ent.getValue()).intValue();
            this.mReadHistoryStrings[idx] = tag.string;
            this.mReadHistoryUids[idx] = tag.uid;
            this.mReadHistoryChars += tag.string.length() + NUM_WIFI_TX_LEVELS;
        }
        return true;
    }

    public int getHistoryStringPoolSize() {
        return this.mReadHistoryStrings.length;
    }

    public int getHistoryStringPoolBytes() {
        return (this.mReadHistoryStrings.length * 12) + (this.mReadHistoryChars * NETWORK_STATS_DELTA);
    }

    public String getHistoryTagPoolString(int index) {
        return this.mReadHistoryStrings[index];
    }

    public int getHistoryTagPoolUid(int index) {
        return this.mReadHistoryUids[index];
    }

    public boolean getNextHistoryLocked(HistoryItem out) {
        boolean end;
        int pos = this.mHistoryBuffer.dataPosition();
        if (pos == 0) {
            out.clear();
        }
        if (pos >= this.mHistoryBuffer.dataSize()) {
            end = true;
        } else {
            end = USE_OLD_HISTORY;
        }
        if (end) {
            return USE_OLD_HISTORY;
        }
        long lastRealtime = out.time;
        long lastWalltime = out.currentTime;
        readHistoryDelta(this.mHistoryBuffer, out);
        if (!(out.cmd == 5 || out.cmd == STATE_BATTERY_STATUS_MASK || lastWalltime == 0)) {
            out.currentTime = (out.time - lastRealtime) + lastWalltime;
        }
        return true;
    }

    public void finishIteratingHistoryLocked() {
        this.mIteratingHistory = USE_OLD_HISTORY;
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
        return this.mScreenState == NETWORK_STATS_DELTA ? true : USE_OLD_HISTORY;
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
        this.mLowDischargeAmountSinceCharge = NETWORK_STATS_LAST;
        this.mHighDischargeAmountSinceCharge = NETWORK_STATS_LAST;
        this.mDischargeAmountScreenOn = NETWORK_STATS_LAST;
        this.mDischargeAmountScreenOnSinceCharge = NETWORK_STATS_LAST;
        this.mDischargeAmountScreenOff = NETWORK_STATS_LAST;
        this.mDischargeAmountScreenOffSinceCharge = NETWORK_STATS_LAST;
        this.mDischargeStepTracker.init();
        this.mChargeStepTracker.init();
        this.mDischargeScreenOffCounter.reset(USE_OLD_HISTORY);
        this.mDischargeCounter.reset(USE_OLD_HISTORY);
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
        if ((this.mHistoryCur.states & DELTA_BATTERY_LEVEL_FLAG) == 0) {
            if (this.mScreenState == NETWORK_STATS_DELTA) {
                this.mDischargeScreenOnUnplugLevel = this.mHistoryCur.batteryLevel;
                this.mDischargeScreenOffUnplugLevel = NETWORK_STATS_LAST;
            } else {
                this.mDischargeScreenOnUnplugLevel = NETWORK_STATS_LAST;
                this.mDischargeScreenOffUnplugLevel = this.mHistoryCur.batteryLevel;
            }
            this.mDischargeAmountScreenOn = NETWORK_STATS_LAST;
            this.mDischargeAmountScreenOff = NETWORK_STATS_LAST;
        }
        initActiveHistoryEventsLocked(mSecRealtime, mSecUptime);
    }

    private void resetAllStatsLocked() {
        int i;
        long uptimeMillis = this.mClocks.uptimeMillis();
        long elapsedRealtimeMillis = this.mClocks.elapsedRealtime();
        this.mStartCount = NETWORK_STATS_LAST;
        initTimes(1000 * uptimeMillis, 1000 * elapsedRealtimeMillis);
        this.mScreenOnTimer.reset(USE_OLD_HISTORY);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mScreenBrightnessTimer[i].reset(USE_OLD_HISTORY);
        }
        if (this.mPowerProfile != null) {
            this.mEstimatedBatteryCapacity = (int) this.mPowerProfile.getBatteryCapacity();
        } else {
            this.mEstimatedBatteryCapacity = -1;
        }
        this.mInteractiveTimer.reset(USE_OLD_HISTORY);
        this.mPowerSaveModeEnabledTimer.reset(USE_OLD_HISTORY);
        this.mLastIdleTimeStart = elapsedRealtimeMillis;
        this.mLongestLightIdleTime = 0;
        this.mLongestFullIdleTime = 0;
        this.mDeviceIdleModeLightTimer.reset(USE_OLD_HISTORY);
        this.mDeviceIdleModeFullTimer.reset(USE_OLD_HISTORY);
        this.mDeviceLightIdlingTimer.reset(USE_OLD_HISTORY);
        this.mDeviceIdlingTimer.reset(USE_OLD_HISTORY);
        this.mPhoneOnTimer.reset(USE_OLD_HISTORY);
        this.mAudioOnTimer.reset(USE_OLD_HISTORY);
        this.mVideoOnTimer.reset(USE_OLD_HISTORY);
        this.mFlashlightOnTimer.reset(USE_OLD_HISTORY);
        this.mCameraOnTimer.reset(USE_OLD_HISTORY);
        this.mBluetoothScanTimer.reset(USE_OLD_HISTORY);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneSignalStrengthsTimer[i].reset(USE_OLD_HISTORY);
        }
        this.mPhoneSignalScanningTimer.reset(USE_OLD_HISTORY);
        for (i = NETWORK_STATS_LAST; i < 17; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneDataConnectionsTimer[i].reset(USE_OLD_HISTORY);
        }
        for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
            this.mNetworkByteActivityCounters[i].reset(USE_OLD_HISTORY);
            this.mNetworkPacketActivityCounters[i].reset(USE_OLD_HISTORY);
        }
        this.mMobileRadioActiveTimer.reset(USE_OLD_HISTORY);
        this.mMobileRadioActivePerAppTimer.reset(USE_OLD_HISTORY);
        this.mMobileRadioActiveAdjustedTime.reset(USE_OLD_HISTORY);
        this.mMobileRadioActiveUnknownTime.reset(USE_OLD_HISTORY);
        this.mMobileRadioActiveUnknownCount.reset(USE_OLD_HISTORY);
        this.mWifiOnTimer.reset(USE_OLD_HISTORY);
        this.mGlobalWifiRunningTimer.reset(USE_OLD_HISTORY);
        for (i = NETWORK_STATS_LAST; i < 8; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiStateTimer[i].reset(USE_OLD_HISTORY);
        }
        for (i = NETWORK_STATS_LAST; i < 13; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSupplStateTimer[i].reset(USE_OLD_HISTORY);
        }
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSignalStrengthsTimer[i].reset(USE_OLD_HISTORY);
        }
        this.mWifiActivity.reset(USE_OLD_HISTORY);
        this.mBluetoothActivity.reset(USE_OLD_HISTORY);
        this.mModemActivity.reset(USE_OLD_HISTORY);
        this.mUnpluggedNumConnectivityChange = NETWORK_STATS_LAST;
        this.mLoadedNumConnectivityChange = NETWORK_STATS_LAST;
        this.mNumConnectivityChange = NETWORK_STATS_LAST;
        i = NETWORK_STATS_LAST;
        while (i < this.mUidStats.size()) {
            if (((Uid) this.mUidStats.valueAt(i)).reset()) {
                this.mUidStats.remove(this.mUidStats.keyAt(i));
                i--;
            }
            i += NUM_WIFI_TX_LEVELS;
        }
        if (this.mKernelWakelockStats.size() > 0) {
            for (SamplingTimer timer : this.mKernelWakelockStats.values()) {
                this.mOnBatteryScreenOffTimeBase.remove(timer);
            }
            this.mKernelWakelockStats.clear();
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
        HwLog.bdate("BDAT_TAG_RESET_BATTERY_STAT", "reset");
    }

    private void initActiveHistoryEventsLocked(long elapsedRealtimeMs, long uptimeMs) {
        int i = NETWORK_STATS_LAST;
        while (i < 19) {
            if (this.mRecordAllHistory || i != NUM_WIFI_TX_LEVELS) {
                HashMap<String, SparseIntArray> active = this.mActiveEvents.getStateForEvent(i);
                if (active != null) {
                    for (Map.Entry<String, SparseIntArray> ent : active.entrySet()) {
                        SparseIntArray uids = (SparseIntArray) ent.getValue();
                        for (int j = NETWORK_STATS_LAST; j < uids.size(); j += NUM_WIFI_TX_LEVELS) {
                            addHistoryEventLocked(elapsedRealtimeMs, uptimeMs, i, (String) ent.getKey(), uids.keyAt(j));
                        }
                    }
                }
            }
            i += NUM_WIFI_TX_LEVELS;
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
            this.mDischargeScreenOffUnplugLevel = NETWORK_STATS_LAST;
            return;
        }
        this.mDischargeScreenOnUnplugLevel = NETWORK_STATS_LAST;
        this.mDischargeScreenOffUnplugLevel = this.mDischargeCurrentLevel;
    }

    public void pullPendingStateUpdatesLocked() {
        if (this.mOnBatteryInternal) {
            boolean screenOn = this.mScreenState == NETWORK_STATS_DELTA ? true : USE_OLD_HISTORY;
            updateDischargeScreenLevelsLocked(screenOn, screenOn);
        }
    }

    private NetworkStats getNetworkStatsDeltaLocked(String[] ifaces, NetworkStats[] networkStatsBuffer) throws IOException {
        if (!SystemProperties.getBoolean(NetworkManagementSocketTagger.PROP_QTAGUID_ENABLED, USE_OLD_HISTORY)) {
            return null;
        }
        NetworkStats stats = this.mNetworkStatsFactory.readNetworkStatsDetail(-1, ifaces, NETWORK_STATS_LAST, networkStatsBuffer[NUM_WIFI_TX_LEVELS]);
        networkStatsBuffer[NETWORK_STATS_DELTA] = NetworkStats.subtract(stats, networkStatsBuffer[NETWORK_STATS_LAST], null, null, networkStatsBuffer[NETWORK_STATS_DELTA]);
        networkStatsBuffer[NUM_WIFI_TX_LEVELS] = networkStatsBuffer[NETWORK_STATS_LAST];
        networkStatsBuffer[NETWORK_STATS_LAST] = stats;
        return networkStatsBuffer[NETWORK_STATS_DELTA];
    }

    public void updateWifiStateLocked(WifiActivityEnergyInfo info) {
        long elapsedRealtimeMs = this.mClocks.elapsedRealtime();
        NetworkStats delta = null;
        try {
            if (!ArrayUtils.isEmpty(this.mWifiIfaces)) {
                delta = getNetworkStatsDeltaLocked(this.mWifiIfaces, this.mWifiNetworkStats);
            }
            if (this.mOnBatteryInternal) {
                int i;
                SparseLongArray rxPackets = new SparseLongArray();
                SparseLongArray txPackets = new SparseLongArray();
                long totalTxPackets = 0;
                long totalRxPackets = 0;
                if (delta != null) {
                    int size = delta.size();
                    for (i = NETWORK_STATS_LAST; i < size; i += NUM_WIFI_TX_LEVELS) {
                        Entry entry = delta.getValues(i, this.mTmpNetworkStatsEntry);
                        if (entry.rxBytes != 0 || entry.txBytes != 0) {
                            Uid u = getUidStatsLocked(mapUid(entry.uid));
                            if (entry.rxBytes != 0) {
                                u.noteNetworkActivityLocked(NETWORK_STATS_DELTA, entry.rxBytes, entry.rxPackets);
                                this.mNetworkByteActivityCounters[NETWORK_STATS_DELTA].addCountLocked(entry.rxBytes);
                                this.mNetworkPacketActivityCounters[NETWORK_STATS_DELTA].addCountLocked(entry.rxPackets);
                                rxPackets.put(u.getUid(), entry.rxPackets);
                                totalRxPackets += entry.rxPackets;
                            }
                            if (entry.txBytes != 0) {
                                u.noteNetworkActivityLocked(STATE_BATTERY_PLUG_MASK, entry.txBytes, entry.txPackets);
                                this.mNetworkByteActivityCounters[STATE_BATTERY_PLUG_MASK].addCountLocked(entry.txBytes);
                                this.mNetworkPacketActivityCounters[STATE_BATTERY_PLUG_MASK].addCountLocked(entry.txPackets);
                                txPackets.put(u.getUid(), entry.txPackets);
                                totalTxPackets += entry.txPackets;
                            }
                        }
                    }
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
                    for (i = NETWORK_STATS_LAST; i < uidStatsSize; i += NUM_WIFI_TX_LEVELS) {
                        uid = (Uid) this.mUidStats.valueAt(i);
                        totalScanTimeMs += uid.mWifiScanTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                        totalWifiLockTimeMs += uid.mFullWifiLockTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                    }
                    for (i = NETWORK_STATS_LAST; i < uidStatsSize; i += NUM_WIFI_TX_LEVELS) {
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
                            activityCounter.getTxTimeCounters()[NETWORK_STATS_LAST].addCountLocked(scanTxTimeSinceMarkMs);
                            leftOverRxTimeMs -= scanRxTimeSinceMarkMs;
                            leftOverTxTimeMs -= scanTxTimeSinceMarkMs;
                        }
                        long wifiLockTimeSinceMarkMs = uid.mFullWifiLockTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                        if (wifiLockTimeSinceMarkMs > 0) {
                            uid.mFullWifiLockTimer.setMark(elapsedRealtimeMs);
                            uid.getOrCreateWifiControllerActivityLocked().getIdleTimeCounter().addCountLocked((wifiLockTimeSinceMarkMs * idleTimeMs) / totalWifiLockTimeMs);
                        }
                    }
                    for (i = NETWORK_STATS_LAST; i < txPackets.size(); i += NUM_WIFI_TX_LEVELS) {
                        long myTxTimeMs = (txPackets.valueAt(i) * leftOverTxTimeMs) / totalTxPackets;
                        getUidStatsLocked(txPackets.keyAt(i)).getOrCreateWifiControllerActivityLocked().getTxTimeCounters()[NETWORK_STATS_LAST].addCountLocked(myTxTimeMs);
                    }
                    for (i = NETWORK_STATS_LAST; i < rxPackets.size(); i += NUM_WIFI_TX_LEVELS) {
                        long myRxTimeMs = (rxPackets.valueAt(i) * leftOverRxTimeMs) / totalRxPackets;
                        getUidStatsLocked(rxPackets.keyAt(i)).getOrCreateWifiControllerActivityLocked().getRxTimeCounter().addCountLocked(myRxTimeMs);
                    }
                    this.mWifiActivity.getRxTimeCounter().addCountLocked(info.getControllerRxTimeMillis());
                    this.mWifiActivity.getTxTimeCounters()[NETWORK_STATS_LAST].addCountLocked(info.getControllerTxTimeMillis());
                    this.mWifiActivity.getIdleTimeCounter().addCountLocked(info.getControllerIdleTimeMillis());
                    double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
                    if (opVolt != 0.0d) {
                        this.mWifiActivity.getPowerCounter().addCountLocked((long) (((double) info.getControllerEnergyUsed()) / opVolt));
                    }
                }
            }
        } catch (IOException e) {
            Slog.wtf(TAG, "Failed to get wifi network stats", e);
        }
    }

    public void updateMobileRadioStateLocked(long elapsedRealtimeMs, ModemActivityInfo activityInfo) {
        NetworkStats delta = null;
        try {
            if (!ArrayUtils.isEmpty(this.mMobileIfaces)) {
                delta = getNetworkStatsDeltaLocked(this.mMobileIfaces, this.mMobileNetworkStats);
            }
            if (this.mOnBatteryInternal) {
                int lvl;
                long radioTime = this.mMobileRadioActivePerAppTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs);
                this.mMobileRadioActivePerAppTimer.setMark(elapsedRealtimeMs);
                long totalRxPackets = 0;
                long totalTxPackets = 0;
                if (delta != null) {
                    int i;
                    Entry entry;
                    Uid u;
                    int size = delta.size();
                    for (i = NETWORK_STATS_LAST; i < size; i += NUM_WIFI_TX_LEVELS) {
                        entry = delta.getValues(i, this.mTmpNetworkStatsEntry);
                        if (entry.rxPackets != 0 || entry.txPackets != 0) {
                            totalRxPackets += entry.rxPackets;
                            totalTxPackets += entry.txPackets;
                            u = getUidStatsLocked(mapUid(entry.uid));
                            u.noteNetworkActivityLocked(NETWORK_STATS_LAST, entry.rxBytes, entry.rxPackets);
                            u.noteNetworkActivityLocked(NUM_WIFI_TX_LEVELS, entry.txBytes, entry.txPackets);
                            this.mNetworkByteActivityCounters[NETWORK_STATS_LAST].addCountLocked(entry.rxBytes);
                            this.mNetworkByteActivityCounters[NUM_WIFI_TX_LEVELS].addCountLocked(entry.txBytes);
                            this.mNetworkPacketActivityCounters[NETWORK_STATS_LAST].addCountLocked(entry.rxPackets);
                            this.mNetworkPacketActivityCounters[NUM_WIFI_TX_LEVELS].addCountLocked(entry.txPackets);
                        }
                    }
                    long totalPackets = totalRxPackets + totalTxPackets;
                    if (totalPackets > 0) {
                        for (i = NETWORK_STATS_LAST; i < size; i += NUM_WIFI_TX_LEVELS) {
                            entry = delta.getValues(i, this.mTmpNetworkStatsEntry);
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
                                        for (lvl = NETWORK_STATS_LAST; lvl < 5; lvl += NUM_WIFI_TX_LEVELS) {
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
                }
                if (activityInfo != null) {
                    this.mHasModemReporting = true;
                    this.mModemActivity.getIdleTimeCounter().addCountLocked((long) activityInfo.getIdleTimeMillis());
                    this.mModemActivity.getRxTimeCounter().addCountLocked((long) activityInfo.getRxTimeMillis());
                    for (lvl = NETWORK_STATS_LAST; lvl < 5; lvl += NUM_WIFI_TX_LEVELS) {
                        this.mModemActivity.getTxTimeCounters()[lvl].addCountLocked((long) activityInfo.getTxTimeMillis()[lvl]);
                    }
                    double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
                    if (opVolt != 0.0d) {
                        this.mModemActivity.getPowerCounter().addCountLocked((long) (((double) activityInfo.getEnergyUsed()) / opVolt));
                    }
                }
            }
        } catch (IOException e) {
            Slog.wtf(TAG, "Failed to get mobile network stats", e);
        }
    }

    public void updateBluetoothStateLocked(BluetoothActivityEnergyInfo info) {
        if (info != null && this.mOnBatteryInternal) {
            int i;
            Uid u;
            ControllerActivityCounterImpl counter;
            UidTraffic traffic;
            this.mHasBluetoothReporting = true;
            long elapsedRealtimeMs = SystemClock.elapsedRealtime();
            long rxTimeMs = info.getControllerRxTimeMillis();
            long txTimeMs = info.getControllerTxTimeMillis();
            long totalScanTimeMs = 0;
            int uidCount = this.mUidStats.size();
            for (i = NETWORK_STATS_LAST; i < uidCount; i += NUM_WIFI_TX_LEVELS) {
                u = (Uid) this.mUidStats.valueAt(i);
                if (u.mBluetoothScanTimer != null) {
                    totalScanTimeMs += u.mBluetoothScanTimer.getTimeSinceMarkLocked(1000 * elapsedRealtimeMs) / 1000;
                }
            }
            boolean normalizeScanRxTime = totalScanTimeMs > rxTimeMs ? true : USE_OLD_HISTORY;
            boolean normalizeScanTxTime = totalScanTimeMs > txTimeMs ? true : USE_OLD_HISTORY;
            long leftOverRxTimeMs = rxTimeMs;
            long leftOverTxTimeMs = txTimeMs;
            for (i = NETWORK_STATS_LAST; i < uidCount; i += NUM_WIFI_TX_LEVELS) {
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
                        counter.getTxTimeCounters()[NETWORK_STATS_LAST].addCountLocked(scanTimeTxSinceMarkMs);
                        leftOverRxTimeMs -= scanTimeRxSinceMarkMs;
                        leftOverTxTimeMs -= scanTimeTxSinceMarkMs;
                    }
                }
            }
            long totalTxBytes = 0;
            long totalRxBytes = 0;
            UidTraffic[] uidTraffic = info.getUidTraffic();
            int numUids = uidTraffic != null ? uidTraffic.length : NETWORK_STATS_LAST;
            for (i = NETWORK_STATS_LAST; i < numUids; i += NUM_WIFI_TX_LEVELS) {
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
                for (i = NETWORK_STATS_LAST; i < numUids; i += NUM_WIFI_TX_LEVELS) {
                    traffic = uidTraffic[i];
                    counter = getUidStatsLocked(mapUid(traffic.getUid())).getOrCreateBluetoothControllerActivityLocked();
                    if (totalRxBytes > 0 && traffic.getRxBytes() > 0) {
                        long timeRxMs = (traffic.getRxBytes() * leftOverRxTimeMs) / totalRxBytes;
                        counter.getRxTimeCounter().addCountLocked(timeRxMs);
                        leftOverRxTimeMs -= timeRxMs;
                    }
                    if (totalTxBytes > 0 && traffic.getTxBytes() > 0) {
                        long timeTxMs = (traffic.getTxBytes() * leftOverTxTimeMs) / totalTxBytes;
                        counter.getTxTimeCounters()[NETWORK_STATS_LAST].addCountLocked(timeTxMs);
                        leftOverTxTimeMs -= timeTxMs;
                    }
                }
            }
            this.mBluetoothActivity.getRxTimeCounter().addCountLocked(info.getControllerRxTimeMillis());
            this.mBluetoothActivity.getTxTimeCounters()[NETWORK_STATS_LAST].addCountLocked(info.getControllerTxTimeMillis());
            this.mBluetoothActivity.getIdleTimeCounter().addCountLocked(info.getControllerIdleTimeMillis());
            double opVolt = this.mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE) / 1000.0d;
            if (opVolt != 0.0d) {
                this.mBluetoothActivity.getPowerCounter().addCountLocked((long) (((double) info.getControllerEnergyUsed()) / opVolt));
            }
        }
    }

    public void updateKernelWakelocksLocked() {
        KernelWakelockStats wakelockStats = this.mKernelWakelockReader.readKernelWakelockStats(this.mTmpWakelockStats);
        if (wakelockStats == null) {
            Slog.w(TAG, "Couldn't get kernel wake lock stats");
            return;
        }
        for (Map.Entry<String, KernelWakelockStats.Entry> ent : wakelockStats.entrySet()) {
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
        int numWakelocksSetStale = NETWORK_STATS_LAST;
        for (Map.Entry<String, SamplingTimer> ent2 : this.mKernelWakelockStats.entrySet()) {
            SamplingTimer st = (SamplingTimer) ent2.getValue();
            if (st.getUpdateVersion() != wakelockStats.kernelWakelockVersion) {
                st.endSample();
                numWakelocksSetStale += NUM_WIFI_TX_LEVELS;
            }
        }
        if (wakelockStats.isEmpty()) {
            Slog.wtf(TAG, "All kernel wakelocks had time of zero");
        }
        if (numWakelocksSetStale == this.mKernelWakelockStats.size()) {
            Slog.wtf(TAG, "All kernel wakelocks were set stale. new version=" + wakelockStats.kernelWakelockVersion);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateCpuTimeLocked() {
        if (this.mPowerProfile != null) {
            int length;
            int i;
            StopwatchTimer timer;
            AnonymousClass3 anonymousClass3;
            long[][] clusterSpeeds = new long[this.mKernelCpuSpeedReaders.length][];
            int cluster = NETWORK_STATS_LAST;
            while (true) {
                length = this.mKernelCpuSpeedReaders.length;
                if (cluster >= r0) {
                    break;
                }
                clusterSpeeds[cluster] = this.mKernelCpuSpeedReaders[cluster].readDelta();
                cluster += NUM_WIFI_TX_LEVELS;
            }
            int numWakelocks = NETWORK_STATS_LAST;
            int numPartialTimers = this.mPartialTimers.size();
            if (this.mOnBatteryScreenOffTimeBase.isRunning()) {
                for (i = NETWORK_STATS_LAST; i < numPartialTimers; i += NUM_WIFI_TX_LEVELS) {
                    timer = (StopwatchTimer) this.mPartialTimers.get(i);
                    if (timer.mInList && timer.mUid != null) {
                        length = timer.mUid.mUid;
                        if (r0 != 1000) {
                            numWakelocks += NUM_WIFI_TX_LEVELS;
                        }
                    }
                }
            }
            int numWakelocksF = numWakelocks;
            this.mTempTotalCpuUserTimeUs = 0;
            this.mTempTotalCpuSystemTimeUs = 0;
            long startTimeMs = this.mClocks.elapsedRealtime();
            KernelUidCpuTimeReader kernelUidCpuTimeReader = this.mKernelUidCpuTimeReader;
            if (this.mOnBatteryInternal) {
                AnonymousClass3 anonymousClass32 = new AnonymousClass3(numWakelocksF, clusterSpeeds);
            } else {
                anonymousClass3 = null;
            }
            kernelUidCpuTimeReader.readDelta(anonymousClass3);
            if (this.mOnBatteryInternal && numWakelocks > 0) {
                this.mTempTotalCpuUserTimeUs = (this.mTempTotalCpuUserTimeUs * 50) / 100;
                this.mTempTotalCpuSystemTimeUs = (this.mTempTotalCpuSystemTimeUs * 50) / 100;
                for (i = NETWORK_STATS_LAST; i < numPartialTimers; i += NUM_WIFI_TX_LEVELS) {
                    timer = (StopwatchTimer) this.mPartialTimers.get(i);
                    if (timer.mInList && timer.mUid != null) {
                        length = timer.mUid.mUid;
                        if (r0 != 1000) {
                            int userTimeUs = (int) (this.mTempTotalCpuUserTimeUs / ((long) numWakelocks));
                            int systemTimeUs = (int) (this.mTempTotalCpuSystemTimeUs / ((long) numWakelocks));
                            timer.mUid.mUserCpuTime.addCountLocked((long) userTimeUs);
                            timer.mUid.mSystemCpuTime.addCountLocked((long) systemTimeUs);
                            timer.mUid.getProcessStatsLocked("*wakelock*").addCpuTimeLocked(userTimeUs / RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, systemTimeUs / RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED);
                            this.mTempTotalCpuUserTimeUs -= (long) userTimeUs;
                            this.mTempTotalCpuSystemTimeUs -= (long) systemTimeUs;
                            numWakelocks--;
                        }
                    }
                }
                if (this.mTempTotalCpuUserTimeUs <= 0) {
                }
                Uid u = getUidStatsLocked(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED);
                u.mUserCpuTime.addCountLocked(this.mTempTotalCpuUserTimeUs);
                u.mSystemCpuTime.addCountLocked(this.mTempTotalCpuSystemTimeUs);
                u.getProcessStatsLocked("*lost*").addCpuTimeLocked(((int) this.mTempTotalCpuUserTimeUs) / RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, ((int) this.mTempTotalCpuSystemTimeUs) / RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED);
            }
            if (ArrayUtils.referenceEquals(this.mPartialTimers, this.mLastPartialTimers)) {
                for (i = NETWORK_STATS_LAST; i < numPartialTimers; i += NUM_WIFI_TX_LEVELS) {
                    ((StopwatchTimer) this.mPartialTimers.get(i)).mInList = true;
                }
            } else {
                int numLastPartialTimers = this.mLastPartialTimers.size();
                for (i = NETWORK_STATS_LAST; i < numLastPartialTimers; i += NUM_WIFI_TX_LEVELS) {
                    ((StopwatchTimer) this.mLastPartialTimers.get(i)).mInList = USE_OLD_HISTORY;
                }
                this.mLastPartialTimers.clear();
                for (i = NETWORK_STATS_LAST; i < numPartialTimers; i += NUM_WIFI_TX_LEVELS) {
                    timer = (StopwatchTimer) this.mPartialTimers.get(i);
                    timer.mInList = true;
                    this.mLastPartialTimers.add(timer);
                }
            }
        }
    }

    boolean setChargingLocked(boolean charging) {
        if (this.mCharging == charging) {
            return USE_OLD_HISTORY;
        }
        this.mCharging = charging;
        HistoryItem historyItem;
        if (charging) {
            historyItem = this.mHistoryCur;
            historyItem.states2 |= DELTA_BATTERY_CHARGE_FLAG;
        } else {
            historyItem = this.mHistoryCur;
            historyItem.states2 &= -16777217;
        }
        this.mHandler.sendEmptyMessage(STATE_BATTERY_PLUG_MASK);
        return true;
    }

    void setOnBatteryLocked(long mSecRealtime, long mSecUptime, boolean onBattery, int oldStatus, int level, int chargeUAh) {
        boolean doWrite = USE_OLD_HISTORY;
        Message m = this.mHandler.obtainMessage(NETWORK_STATS_DELTA);
        m.arg1 = onBattery ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST;
        this.mHandler.sendMessage(m);
        long uptime = mSecUptime * 1000;
        long realtime = mSecRealtime * 1000;
        boolean screenOn = this.mScreenState == NETWORK_STATS_DELTA ? true : USE_OLD_HISTORY;
        HistoryItem historyItem;
        if (onBattery) {
            boolean z;
            boolean reset = USE_OLD_HISTORY;
            if (!this.mNoAutoReset) {
                if (oldStatus != 5 && level < 90 && (this.mDischargeCurrentLevel >= 20 || level < 80)) {
                    if (getHighDischargeAmountSinceCharge() >= MAX_LEVEL_STEPS && this.mHistoryBuffer.dataSize() >= MAX_HISTORY_BUFFER) {
                    }
                }
                Slog.i(TAG, "Resetting battery stats: level=" + level + " status=" + oldStatus + " dischargeLevel=" + this.mDischargeCurrentLevel + " lowAmount=" + getLowDischargeAmountSinceCharge() + " highAmount=" + getHighDischargeAmountSinceCharge());
                if (getLowDischargeAmountSinceCharge() >= 20) {
                    Parcel parcel = Parcel.obtain();
                    writeSummaryToParcel(parcel, true);
                    BackgroundThread.getHandler().post(new AnonymousClass4(parcel));
                }
                doWrite = true;
                resetAllStatsLocked();
                if (chargeUAh > 0) {
                    this.mEstimatedBatteryCapacity = (int) ((((double) level) / 100.0d) * ((double) (chargeUAh / RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED)));
                }
                this.mDischargeStartLevel = level;
                reset = true;
                this.mDischargeStepTracker.init();
            }
            if (this.mCharging) {
                setChargingLocked(USE_OLD_HISTORY);
            }
            this.mLastChargingStateLevel = level;
            this.mOnBatteryInternal = true;
            this.mOnBattery = true;
            this.mLastDischargeStepLevel = level;
            this.mMinDischargeStepLevel = level;
            this.mDischargeStepTracker.clearTime();
            this.mDailyDischargeStepTracker.clearTime();
            this.mInitStepMode = this.mCurStepMode;
            this.mModStepMode = NETWORK_STATS_LAST;
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
                this.mDischargeScreenOffUnplugLevel = NETWORK_STATS_LAST;
            } else {
                this.mDischargeScreenOnUnplugLevel = NETWORK_STATS_LAST;
                this.mDischargeScreenOffUnplugLevel = level;
            }
            this.mDischargeAmountScreenOn = NETWORK_STATS_LAST;
            this.mDischargeAmountScreenOff = NETWORK_STATS_LAST;
            if (screenOn) {
                z = USE_OLD_HISTORY;
            } else {
                z = true;
            }
            updateTimeBasesLocked(true, z, uptime, realtime);
        } else {
            this.mLastChargingStateLevel = level;
            this.mOnBatteryInternal = USE_OLD_HISTORY;
            this.mOnBattery = USE_OLD_HISTORY;
            pullPendingStateUpdatesLocked();
            this.mHistoryCur.batteryLevel = (byte) level;
            historyItem = this.mHistoryCur;
            historyItem.states |= DELTA_BATTERY_LEVEL_FLAG;
            addHistoryRecordLocked(mSecRealtime, mSecUptime);
            this.mDischargePlugLevel = level;
            this.mDischargeCurrentLevel = level;
            if (level < this.mDischargeUnplugLevel) {
                this.mLowDischargeAmountSinceCharge += (this.mDischargeUnplugLevel - level) - 1;
                this.mHighDischargeAmountSinceCharge += this.mDischargeUnplugLevel - level;
            }
            updateDischargeScreenLevelsLocked(screenOn, screenOn);
            updateTimeBasesLocked(USE_OLD_HISTORY, screenOn ? USE_OLD_HISTORY : true, uptime, realtime);
            this.mChargeStepTracker.init();
            this.mLastChargeStepLevel = level;
            this.mMaxChargeStepLevel = level;
            this.mInitStepMode = this.mCurStepMode;
            this.mModStepMode = NETWORK_STATS_LAST;
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

    public void setBatteryStateLocked(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh) {
        boolean onBattery = plugType == 0 ? true : USE_OLD_HISTORY;
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
                    historyItem.states |= DELTA_BATTERY_LEVEL_FLAG;
                }
            }
            historyItem = this.mHistoryCur;
            historyItem.states2 |= DELTA_BATTERY_CHARGE_FLAG;
            this.mHistoryCur.batteryStatus = (byte) status;
            this.mHistoryCur.batteryLevel = (byte) level;
            this.mHistoryCur.batteryChargeUAh = chargeUAh;
            this.mLastDischargeStepLevel = level;
            this.mLastChargeStepLevel = level;
            this.mMinDischargeStepLevel = level;
            this.mMaxChargeStepLevel = level;
            this.mLastChargingStateLevel = level;
        } else if (!(this.mCurrentBatteryLevel == level && this.mOnBattery == onBattery)) {
            recordDailyStatsIfNeededLocked(level >= MAX_WAKELOCKS_PER_UID ? onBattery : USE_OLD_HISTORY);
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
            boolean changed = USE_OLD_HISTORY;
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
            if (temp >= this.mHistoryCur.batteryTemperature + MAX_DAILY_ITEMS || temp <= this.mHistoryCur.batteryTemperature - 10) {
                this.mHistoryCur.batteryTemperature = (short) temp;
                changed = true;
            }
            if (volt > this.mHistoryCur.batteryVoltage + 20 || volt < this.mHistoryCur.batteryVoltage - 20) {
                this.mHistoryCur.batteryVoltage = (char) volt;
                changed = true;
            }
            if (chargeUAh >= this.mHistoryCur.batteryChargeUAh + MAX_DAILY_ITEMS || chargeUAh <= this.mHistoryCur.batteryChargeUAh - 10) {
                if (chargeUAh < this.mHistoryCur.batteryChargeUAh) {
                    chargeDiff = (long) (this.mHistoryCur.batteryChargeUAh - chargeUAh);
                    this.mDischargeCounter.addCountLocked(chargeDiff);
                    this.mDischargeScreenOffCounter.addCountLocked(chargeDiff);
                }
                this.mHistoryCur.batteryChargeUAh = chargeUAh;
                changed = true;
            }
            long modeBits = ((((long) this.mInitStepMode) << 48) | (((long) this.mModStepMode) << 56)) | (((long) (level & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE)) << 40);
            if (onBattery) {
                changed |= setChargingLocked(USE_OLD_HISTORY);
                if (this.mLastDischargeStepLevel != level && this.mMinDischargeStepLevel > level) {
                    this.mDischargeStepTracker.addLevelSteps(this.mLastDischargeStepLevel - level, modeBits, elapsedRealtime);
                    this.mDailyDischargeStepTracker.addLevelSteps(this.mLastDischargeStepLevel - level, modeBits, elapsedRealtime);
                    this.mLastDischargeStepLevel = level;
                    this.mMinDischargeStepLevel = level;
                    this.mInitStepMode = this.mCurStepMode;
                    this.mModStepMode = NETWORK_STATS_LAST;
                }
            } else {
                if (level >= 90) {
                    changed |= setChargingLocked(true);
                    this.mLastChargeStepLevel = level;
                }
                if (this.mCharging) {
                    if (this.mLastChargeStepLevel > level) {
                        changed |= setChargingLocked(USE_OLD_HISTORY);
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
                    this.mModStepMode = NETWORK_STATS_LAST;
                }
            }
            if (changed) {
                addHistoryRecordLocked(elapsedRealtime, uptime);
            }
        }
        if (!onBattery && status == 5) {
            this.mRecordingHistory = USE_OLD_HISTORY;
        }
    }

    public long getAwakeTimeBattery() {
        return computeBatteryUptime(getBatteryUptimeLocked(), NUM_WIFI_TX_LEVELS);
    }

    public long getAwakeTimePlugged() {
        return (this.mClocks.uptimeMillis() * 1000) - getAwakeTimeBattery();
    }

    public long computeUptime(long curTime, int which) {
        switch (which) {
            case NETWORK_STATS_LAST /*0*/:
                return this.mUptime + (curTime - this.mUptimeStart);
            case NUM_WIFI_TX_LEVELS /*1*/:
                return curTime - this.mUptimeStart;
            case NETWORK_STATS_DELTA /*2*/:
                return curTime - this.mOnBatteryTimeBase.getUptimeStart();
            default:
                return 0;
        }
    }

    public long computeRealtime(long curTime, int which) {
        switch (which) {
            case NETWORK_STATS_LAST /*0*/:
                return this.mRealtime + (curTime - this.mRealtimeStart);
            case NUM_WIFI_TX_LEVELS /*1*/:
                return curTime - this.mRealtimeStart;
            case NETWORK_STATS_DELTA /*2*/:
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
            for (int i = NETWORK_STATS_LAST; i < numSteps; i += NUM_WIFI_TX_LEVELS) {
                total += steps[i] & 1099511627775L;
            }
            return total / ((long) numSteps);
        }
        Slog.wtf(TAG, "numSteps > steps.length, numSteps = " + numSteps + ",steps.length = " + steps.length);
        return -1;
    }

    public long computeBatteryTimeRemaining(long curTime) {
        if (!this.mOnBattery || this.mDischargeStepTracker.mNumStepDurations < NUM_WIFI_TX_LEVELS) {
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
        if (this.mOnBattery || this.mChargeStepTracker.mNumStepDurations < NUM_WIFI_TX_LEVELS) {
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
            return NETWORK_STATS_LAST;
        }
        return dischargeAmount;
    }

    public int getDischargeAmountScreenOn() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOn;
            if (this.mOnBattery && this.mScreenState == NETWORK_STATS_DELTA && this.mDischargeCurrentLevel < this.mDischargeScreenOnUnplugLevel) {
                val += this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOnSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOnSinceCharge;
            if (this.mOnBattery && this.mScreenState == NETWORK_STATS_DELTA && this.mDischargeCurrentLevel < this.mDischargeScreenOnUnplugLevel) {
                val += this.mDischargeScreenOnUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOff() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOff;
            if (this.mOnBattery && this.mScreenState != NETWORK_STATS_DELTA && this.mDischargeCurrentLevel < this.mDischargeScreenOffUnplugLevel) {
                val += this.mDischargeScreenOffUnplugLevel - this.mDischargeCurrentLevel;
            }
        }
        return val;
    }

    public int getDischargeAmountScreenOffSinceCharge() {
        int val;
        synchronized (this) {
            val = this.mDischargeAmountScreenOffSinceCharge;
            if (this.mOnBattery && this.mScreenState != NETWORK_STATS_DELTA && this.mDischargeCurrentLevel < this.mDischargeScreenOffUnplugLevel) {
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
        writeLocked(USE_OLD_HISTORY);
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

    public void commitPendingDataToDisk() {
        synchronized (this) {
            Parcel next = this.mPendingWrite;
            this.mPendingWrite = null;
            if (next == null) {
                return;
            }
            this.mWriteLock.lock();
            try {
                FileOutputStream stream = new FileOutputStream(this.mFile.chooseForWrite());
                stream.write(next.marshall());
                stream.flush();
                FileUtils.sync(stream);
                stream.close();
                this.mFile.commit();
            } catch (IOException e) {
                Slog.w("BatteryStats", "Error writing battery statistics", e);
                this.mFile.rollback();
            } finally {
                next.recycle();
                this.mWriteLock.unlock();
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
                in.unmarshall(raw, NETWORK_STATS_LAST, raw.length);
                in.setDataPosition(NETWORK_STATS_LAST);
                stream.close();
                readSummaryFromParcel(in);
                this.mEndPlatformVersion = Build.ID;
                if (this.mHistoryBuffer.dataPosition() > 0) {
                    this.mRecordingHistory = true;
                    long elapsedRealtime = this.mClocks.elapsedRealtime();
                    long uptime = this.mClocks.uptimeMillis();
                    addHistoryBufferLocked(elapsedRealtime, uptime, (byte) 4, this.mHistoryCur);
                    startRecordingHistory(elapsedRealtime, uptime, USE_OLD_HISTORY);
                }
                recordDailyStatsIfNeededLocked(USE_OLD_HISTORY);
            }
        } catch (Exception e) {
            Slog.e("BatteryStats", "Error reading battery statistics", e);
            resetAllStatsLocked();
        }
    }

    public int describeContents() {
        return NETWORK_STATS_LAST;
    }

    void readHistory(Parcel in, boolean andOldHistory) throws ParcelFormatException {
        long historyBaseTime = in.readLong();
        this.mHistoryBuffer.setDataSize(NETWORK_STATS_LAST);
        this.mHistoryBuffer.setDataPosition(NETWORK_STATS_LAST);
        this.mHistoryTagPool.clear();
        this.mNextHistoryTagIdx = NETWORK_STATS_LAST;
        this.mNumHistoryTagChars = NETWORK_STATS_LAST;
        int numTags = in.readInt();
        for (int i = NETWORK_STATS_LAST; i < numTags; i += NUM_WIFI_TX_LEVELS) {
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
                this.mNextHistoryTagIdx = idx + NUM_WIFI_TX_LEVELS;
            }
            this.mNumHistoryTagChars += tag.string.length() + NUM_WIFI_TX_LEVELS;
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
            for (Map.Entry<HistoryTag, Integer> ent : this.mHistoryTagPool.entrySet()) {
                HistoryTag tag = (HistoryTag) ent.getKey();
                out.writeInt(((Integer) ent.getValue()).intValue());
                out.writeString(tag.string);
                out.writeInt(tag.uid);
            }
            out.writeInt(this.mHistoryBuffer.dataSize());
            out.appendFrom(this.mHistoryBuffer, NETWORK_STATS_LAST, this.mHistoryBuffer.dataSize());
            if (andOldHistory) {
                writeOldHistory(out);
            }
            return;
        }
        out.writeInt(NETWORK_STATS_LAST);
        out.writeInt(NETWORK_STATS_LAST);
    }

    void writeOldHistory(Parcel out) {
    }

    public void readSummaryFromParcel(Parcel in) throws ParcelFormatException {
        int version = in.readInt();
        if (version != VERSION) {
            Slog.w("BatteryStats", "readFromParcel: version got " + version + ", expected " + VERSION + "; erasing old stats");
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
                pc.mUpdate = in.readInt() != 0 ? true : USE_OLD_HISTORY;
                pc.mVersionCode = in.readInt();
                this.mDailyPackageChanges.add(pc);
            }
        } else {
            this.mDailyPackageChanges = null;
        }
        this.mDailyStartTime = in.readLong();
        this.mNextMinDailyDeadline = in.readLong();
        this.mNextMaxDailyDeadline = in.readLong();
        this.mStartCount += NUM_WIFI_TX_LEVELS;
        this.mScreenState = NETWORK_STATS_LAST;
        this.mScreenOnTimer.readSummaryFromParcelLocked(in);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mScreenBrightnessTimer[i].readSummaryFromParcelLocked(in);
        }
        this.mInteractive = USE_OLD_HISTORY;
        this.mInteractiveTimer.readSummaryFromParcelLocked(in);
        this.mPhoneOn = USE_OLD_HISTORY;
        this.mPowerSaveModeEnabledTimer.readSummaryFromParcelLocked(in);
        this.mLongestLightIdleTime = in.readLong();
        this.mLongestFullIdleTime = in.readLong();
        this.mDeviceIdleModeLightTimer.readSummaryFromParcelLocked(in);
        this.mDeviceIdleModeFullTimer.readSummaryFromParcelLocked(in);
        this.mDeviceLightIdlingTimer.readSummaryFromParcelLocked(in);
        this.mDeviceIdlingTimer.readSummaryFromParcelLocked(in);
        this.mPhoneOnTimer.readSummaryFromParcelLocked(in);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneSignalStrengthsTimer[i].readSummaryFromParcelLocked(in);
        }
        this.mPhoneSignalScanningTimer.readSummaryFromParcelLocked(in);
        for (i = NETWORK_STATS_LAST; i < 17; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneDataConnectionsTimer[i].readSummaryFromParcelLocked(in);
        }
        for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
            this.mNetworkByteActivityCounters[i].readSummaryFromParcelLocked(in);
            this.mNetworkPacketActivityCounters[i].readSummaryFromParcelLocked(in);
        }
        this.mMobileRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mMobileRadioActiveTimer.readSummaryFromParcelLocked(in);
        this.mMobileRadioActivePerAppTimer.readSummaryFromParcelLocked(in);
        this.mMobileRadioActiveAdjustedTime.readSummaryFromParcelLocked(in);
        this.mMobileRadioActiveUnknownTime.readSummaryFromParcelLocked(in);
        this.mMobileRadioActiveUnknownCount.readSummaryFromParcelLocked(in);
        this.mWifiRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mWifiOn = USE_OLD_HISTORY;
        this.mWifiOnTimer.readSummaryFromParcelLocked(in);
        this.mGlobalWifiRunning = USE_OLD_HISTORY;
        this.mGlobalWifiRunningTimer.readSummaryFromParcelLocked(in);
        for (i = NETWORK_STATS_LAST; i < 8; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiStateTimer[i].readSummaryFromParcelLocked(in);
        }
        for (i = NETWORK_STATS_LAST; i < 13; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSupplStateTimer[i].readSummaryFromParcelLocked(in);
        }
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSignalStrengthsTimer[i].readSummaryFromParcelLocked(in);
        }
        this.mWifiActivity.readSummaryFromParcel(in);
        this.mBluetoothActivity.readSummaryFromParcel(in);
        this.mModemActivity.readSummaryFromParcel(in);
        this.mHasWifiReporting = in.readInt() != 0 ? true : USE_OLD_HISTORY;
        this.mHasBluetoothReporting = in.readInt() != 0 ? true : USE_OLD_HISTORY;
        this.mHasModemReporting = in.readInt() != 0 ? true : USE_OLD_HISTORY;
        int readInt = in.readInt();
        this.mLoadedNumConnectivityChange = readInt;
        this.mNumConnectivityChange = readInt;
        this.mFlashlightOnNesting = NETWORK_STATS_LAST;
        this.mFlashlightOnTimer.readSummaryFromParcelLocked(in);
        this.mCameraOnNesting = NETWORK_STATS_LAST;
        this.mCameraOnTimer.readSummaryFromParcelLocked(in);
        this.mBluetoothScanNesting = NETWORK_STATS_LAST;
        this.mBluetoothScanTimer.readSummaryFromParcelLocked(in);
        int NKW = in.readInt();
        if (NKW > 10000) {
            throw new ParcelFormatException("File corrupt: too many kernel wake locks " + NKW);
        }
        for (int ikw = NETWORK_STATS_LAST; ikw < NKW; ikw += NUM_WIFI_TX_LEVELS) {
            if (in.readInt() != 0) {
                getKernelWakelockTimerLocked(in.readString()).readSummaryFromParcelLocked(in);
            }
        }
        int NWR = in.readInt();
        if (NWR > 10000) {
            throw new ParcelFormatException("File corrupt: too many wakeup reasons " + NWR);
        }
        for (int iwr = NETWORK_STATS_LAST; iwr < NWR; iwr += NUM_WIFI_TX_LEVELS) {
            if (in.readInt() != 0) {
                getWakeupReasonTimerLocked(in.readString()).readSummaryFromParcelLocked(in);
            }
        }
        int NU = in.readInt();
        if (NU > 10000) {
            throw new ParcelFormatException("File corrupt: too many uids " + NU);
        }
        for (int iu = NETWORK_STATS_LAST; iu < NU; iu += NUM_WIFI_TX_LEVELS) {
            int uid = in.readInt();
            Uid uid2 = new Uid(this, uid);
            this.mUidStats.put(uid, uid2);
            uid2.mWifiRunning = USE_OLD_HISTORY;
            if (in.readInt() != 0) {
                uid2.mWifiRunningTimer.readSummaryFromParcelLocked(in);
            }
            uid2.mFullWifiLockOut = USE_OLD_HISTORY;
            if (in.readInt() != 0) {
                uid2.mFullWifiLockTimer.readSummaryFromParcelLocked(in);
            }
            uid2.mWifiScanStarted = USE_OLD_HISTORY;
            if (in.readInt() != 0) {
                uid2.mWifiScanTimer.readSummaryFromParcelLocked(in);
            }
            uid2.mWifiBatchedScanBinStarted = -1;
            for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
                if (in.readInt() != 0) {
                    uid2.makeWifiBatchedScanBin(i, null);
                    uid2.mWifiBatchedScanTimer[i].readSummaryFromParcelLocked(in);
                }
            }
            uid2.mWifiMulticastEnabled = USE_OLD_HISTORY;
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
                uid2.createBluetoothScanTimerLocked().readSummaryFromParcelLocked(in);
            }
            uid2.mProcessState = -1;
            for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
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
                for (i = NETWORK_STATS_LAST; i < 4; i += NUM_WIFI_TX_LEVELS) {
                    uid2.mUserActivityCounters[i].readSummaryFromParcelLocked(in);
                }
            }
            if (in.readInt() != 0) {
                if (uid2.mNetworkByteActivityCounters == null) {
                    uid2.initNetworkActivityLocked();
                }
                for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
                    uid2.mNetworkByteActivityCounters[i].readSummaryFromParcelLocked(in);
                    uid2.mNetworkPacketActivityCounters[i].readSummaryFromParcelLocked(in);
                }
                uid2.mMobileRadioActiveTime.readSummaryFromParcelLocked(in);
                uid2.mMobileRadioActiveCount.readSummaryFromParcelLocked(in);
            }
            uid2.mUserCpuTime.readSummaryFromParcelLocked(in);
            uid2.mSystemCpuTime.readSummaryFromParcelLocked(in);
            uid2.mCpuPower.readSummaryFromParcelLocked(in);
            if (in.readInt() != 0) {
                int numClusters = in.readInt();
                if (this.mPowerProfile != null) {
                    if (this.mPowerProfile.getNumCpuClusters() != numClusters) {
                        throw new ParcelFormatException("Incompatible cpu cluster arrangement");
                    }
                }
                uid2.mCpuClusterSpeed = new LongSamplingCounter[numClusters][];
                for (int cluster = NETWORK_STATS_LAST; cluster < numClusters; cluster += NUM_WIFI_TX_LEVELS) {
                    if (in.readInt() != 0) {
                        int NSB = in.readInt();
                        if (this.mPowerProfile != null) {
                            if (this.mPowerProfile.getNumSpeedStepsInCpuCluster(cluster) != NSB) {
                                throw new ParcelFormatException("File corrupt: too many speed bins " + NSB);
                            }
                        }
                        uid2.mCpuClusterSpeed[cluster] = new LongSamplingCounter[NSB];
                        for (int speed = NETWORK_STATS_LAST; speed < NSB; speed += NUM_WIFI_TX_LEVELS) {
                            if (in.readInt() != 0) {
                                uid2.mCpuClusterSpeed[cluster][speed] = new LongSamplingCounter(this.mOnBatteryTimeBase);
                                uid2.mCpuClusterSpeed[cluster][speed].readSummaryFromParcelLocked(in);
                            }
                        }
                    } else {
                        uid2.mCpuClusterSpeed[cluster] = null;
                    }
                }
            } else {
                uid2.mCpuClusterSpeed = null;
            }
            int NW = in.readInt();
            if (NW > MAX_WAKELOCKS_PER_UID) {
                Slog.i(TAG, "NW > 100, uid: " + uid);
                throw new ParcelFormatException("File corrupt: too many wake locks " + NW);
            }
            for (int iw = NETWORK_STATS_LAST; iw < NW; iw += NUM_WIFI_TX_LEVELS) {
                uid2.readWakeSummaryFromParcelLocked(in.readString(), in);
            }
            int NS = in.readInt();
            if (NS > MAX_WAKELOCKS_PER_UID) {
                throw new ParcelFormatException("File corrupt: too many syncs " + NS);
            }
            int is;
            for (is = NETWORK_STATS_LAST; is < NS; is += NUM_WIFI_TX_LEVELS) {
                uid2.readSyncSummaryFromParcelLocked(in.readString(), in);
            }
            int NJ = in.readInt();
            if (NJ > MAX_WAKELOCKS_PER_UID) {
                throw new ParcelFormatException("File corrupt: too many job timers " + NJ);
            }
            for (int ij = NETWORK_STATS_LAST; ij < NJ; ij += NUM_WIFI_TX_LEVELS) {
                uid2.readJobSummaryFromParcelLocked(in.readString(), in);
            }
            int NP = in.readInt();
            if (NP > 1000) {
                throw new ParcelFormatException("File corrupt: too many sensors " + NP);
            }
            for (is = NETWORK_STATS_LAST; is < NP; is += NUM_WIFI_TX_LEVELS) {
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
            for (ip = NETWORK_STATS_LAST; ip < NP; ip += NUM_WIFI_TX_LEVELS) {
                Proc p = uid2.getProcessStatsLocked(in.readString());
                long readLong = in.readLong();
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
            for (ip = NETWORK_STATS_LAST; ip < NP; ip += NUM_WIFI_TX_LEVELS) {
                String pkgName = in.readString();
                Pkg p2 = uid2.getPackageStatsLocked(pkgName);
                int NWA = in.readInt();
                if (NWA > 1000) {
                    throw new ParcelFormatException("File corrupt: too many wakeup alarms " + NWA);
                }
                p2.mWakeupAlarms.clear();
                for (int iwa = NETWORK_STATS_LAST; iwa < NWA; iwa += NUM_WIFI_TX_LEVELS) {
                    String tag = in.readString();
                    Counter c = new Counter(this.mOnBatteryTimeBase);
                    c.readSummaryFromParcelLocked(in);
                    p2.mWakeupAlarms.put(tag, c);
                }
                NS = in.readInt();
                if (NS > 1000) {
                    throw new ParcelFormatException("File corrupt: too many services " + NS);
                }
                for (is = NETWORK_STATS_LAST; is < NS; is += NUM_WIFI_TX_LEVELS) {
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
        out.writeInt(VERSION);
        writeHistory(out, inclHistory, true);
        out.writeInt(this.mStartCount);
        out.writeLong(computeUptime(NOW_SYS, NETWORK_STATS_LAST));
        out.writeLong(computeRealtime(NOWREAL_SYS, NETWORK_STATS_LAST));
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
            for (i = NETWORK_STATS_LAST; i < NPKG; i += NUM_WIFI_TX_LEVELS) {
                PackageChange pc = (PackageChange) this.mDailyPackageChanges.get(i);
                out.writeString(pc.mPackageName);
                out.writeInt(pc.mUpdate ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
                out.writeInt(pc.mVersionCode);
            }
        } else {
            out.writeInt(NETWORK_STATS_LAST);
        }
        out.writeLong(this.mDailyStartTime);
        out.writeLong(this.mNextMinDailyDeadline);
        out.writeLong(this.mNextMaxDailyDeadline);
        this.mScreenOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
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
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneSignalStrengthsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        this.mPhoneSignalScanningTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        for (i = NETWORK_STATS_LAST; i < 17; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneDataConnectionsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
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
        for (i = NETWORK_STATS_LAST; i < 8; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiStateTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        for (i = NETWORK_STATS_LAST; i < 13; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSupplStateTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSignalStrengthsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        }
        this.mWifiActivity.writeSummaryToParcel(out);
        this.mBluetoothActivity.writeSummaryToParcel(out);
        this.mModemActivity.writeSummaryToParcel(out);
        out.writeInt(this.mHasWifiReporting ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
        out.writeInt(this.mHasBluetoothReporting ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
        out.writeInt(this.mHasModemReporting ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
        out.writeInt(this.mNumConnectivityChange);
        this.mFlashlightOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mCameraOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        this.mBluetoothScanTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
        out.writeInt(this.mKernelWakelockStats.size());
        for (Map.Entry<String, SamplingTimer> ent : this.mKernelWakelockStats.entrySet()) {
            Timer kwlt = (Timer) ent.getValue();
            if (kwlt != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                out.writeString((String) ent.getKey());
                kwlt.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
        }
        out.writeInt(this.mWakeupReasonStats.size());
        for (Map.Entry<String, SamplingTimer> ent2 : this.mWakeupReasonStats.entrySet()) {
            SamplingTimer timer = (SamplingTimer) ent2.getValue();
            if (timer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                out.writeString((String) ent2.getKey());
                timer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
        }
        int NU = this.mUidStats.size();
        out.writeInt(NU);
        for (int iu = NETWORK_STATS_LAST; iu < NU; iu += NUM_WIFI_TX_LEVELS) {
            int is;
            out.writeInt(this.mUidStats.keyAt(iu));
            Uid u = (Uid) this.mUidStats.valueAt(iu);
            if (u.mWifiRunningTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mWifiRunningTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mFullWifiLockTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mFullWifiLockTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mWifiScanTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mWifiScanTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
                if (u.mWifiBatchedScanTimer[i] != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    u.mWifiBatchedScanTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
            }
            if (u.mWifiMulticastTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mWifiMulticastTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mAudioTurnedOnTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mAudioTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mVideoTurnedOnTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mVideoTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mFlashlightTurnedOnTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mFlashlightTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mCameraTurnedOnTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mCameraTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mForegroundActivityTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mForegroundActivityTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mBluetoothScanTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mBluetoothScanTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
                if (u.mProcessStateTimer[i] != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    u.mProcessStateTimer[i].writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
            }
            if (u.mVibratorOnTimer != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                u.mVibratorOnTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            if (u.mUserActivityCounters == null) {
                out.writeInt(NETWORK_STATS_LAST);
            } else {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                for (i = NETWORK_STATS_LAST; i < 4; i += NUM_WIFI_TX_LEVELS) {
                    u.mUserActivityCounters[i].writeSummaryFromParcelLocked(out);
                }
            }
            if (u.mNetworkByteActivityCounters == null) {
                out.writeInt(NETWORK_STATS_LAST);
            } else {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
                    u.mNetworkByteActivityCounters[i].writeSummaryFromParcelLocked(out);
                    u.mNetworkPacketActivityCounters[i].writeSummaryFromParcelLocked(out);
                }
                u.mMobileRadioActiveTime.writeSummaryFromParcelLocked(out);
                u.mMobileRadioActiveCount.writeSummaryFromParcelLocked(out);
            }
            u.mUserCpuTime.writeSummaryFromParcelLocked(out);
            u.mSystemCpuTime.writeSummaryFromParcelLocked(out);
            u.mCpuPower.writeSummaryFromParcelLocked(out);
            if (u.mCpuClusterSpeed != null) {
                out.writeInt(NUM_WIFI_TX_LEVELS);
                out.writeInt(u.mCpuClusterSpeed.length);
                LongSamplingCounter[][] longSamplingCounterArr = u.mCpuClusterSpeed;
                int length = longSamplingCounterArr.length;
                for (int i2 = NETWORK_STATS_LAST; i2 < length; i2 += NUM_WIFI_TX_LEVELS) {
                    LongSamplingCounter[] cpuSpeeds = longSamplingCounterArr[i2];
                    if (cpuSpeeds != null) {
                        out.writeInt(NUM_WIFI_TX_LEVELS);
                        out.writeInt(cpuSpeeds.length);
                        int length2 = cpuSpeeds.length;
                        for (int i3 = NETWORK_STATS_LAST; i3 < length2; i3 += NUM_WIFI_TX_LEVELS) {
                            LongSamplingCounter c = cpuSpeeds[i3];
                            if (c != null) {
                                out.writeInt(NUM_WIFI_TX_LEVELS);
                                c.writeSummaryFromParcelLocked(out);
                            } else {
                                out.writeInt(NETWORK_STATS_LAST);
                            }
                        }
                    } else {
                        out.writeInt(NETWORK_STATS_LAST);
                    }
                }
            } else {
                out.writeInt(NETWORK_STATS_LAST);
            }
            ArrayMap<String, Wakelock> wakeStats = u.mWakelockStats.getMap();
            int NW = wakeStats.size();
            out.writeInt(NW);
            for (int iw = NETWORK_STATS_LAST; iw < NW; iw += NUM_WIFI_TX_LEVELS) {
                out.writeString((String) wakeStats.keyAt(iw));
                Wakelock wl = (Wakelock) wakeStats.valueAt(iw);
                if (wl.mTimerFull != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    wl.mTimerFull.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
                if (wl.mTimerPartial != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    wl.mTimerPartial.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
                if (wl.mTimerWindow != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    wl.mTimerWindow.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
                if (wl.mTimerDraw != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    wl.mTimerDraw.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
            }
            ArrayMap<String, StopwatchTimer> syncStats = u.mSyncStats.getMap();
            int NS = syncStats.size();
            out.writeInt(NS);
            for (is = NETWORK_STATS_LAST; is < NS; is += NUM_WIFI_TX_LEVELS) {
                out.writeString((String) syncStats.keyAt(is));
                ((StopwatchTimer) syncStats.valueAt(is)).writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            }
            ArrayMap<String, StopwatchTimer> jobStats = u.mJobStats.getMap();
            int NJ = jobStats.size();
            out.writeInt(NJ);
            for (int ij = NETWORK_STATS_LAST; ij < NJ; ij += NUM_WIFI_TX_LEVELS) {
                out.writeString((String) jobStats.keyAt(ij));
                ((StopwatchTimer) jobStats.valueAt(ij)).writeSummaryFromParcelLocked(out, NOWREAL_SYS);
            }
            int NSE = u.mSensorStats.size();
            out.writeInt(NSE);
            for (int ise = NETWORK_STATS_LAST; ise < NSE; ise += NUM_WIFI_TX_LEVELS) {
                out.writeInt(u.mSensorStats.keyAt(ise));
                Sensor se = (Sensor) u.mSensorStats.valueAt(ise);
                if (se.mTimer != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    se.mTimer.writeSummaryFromParcelLocked(out, NOWREAL_SYS);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
            }
            int NP = u.mProcessStats.size();
            out.writeInt(NP);
            for (int ip = NETWORK_STATS_LAST; ip < NP; ip += NUM_WIFI_TX_LEVELS) {
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
                for (Map.Entry<String, Pkg> ent3 : u.mPackageStats.entrySet()) {
                    out.writeString((String) ent3.getKey());
                    Pkg ps2 = (Pkg) ent3.getValue();
                    int NWA = ps2.mWakeupAlarms.size();
                    out.writeInt(NWA);
                    for (int iwa = NETWORK_STATS_LAST; iwa < NWA; iwa += NUM_WIFI_TX_LEVELS) {
                        out.writeString((String) ps2.mWakeupAlarms.keyAt(iwa));
                        ((Counter) ps2.mWakeupAlarms.valueAt(iwa)).writeSummaryFromParcelLocked(out);
                    }
                    NS = ps2.mServiceStats.size();
                    out.writeInt(NS);
                    for (is = NETWORK_STATS_LAST; is < NS; is += NUM_WIFI_TX_LEVELS) {
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
        readHistory(in, USE_OLD_HISTORY);
        this.mStartCount = in.readInt();
        this.mStartClockTime = in.readLong();
        this.mStartPlatformVersion = in.readString();
        this.mEndPlatformVersion = in.readString();
        this.mUptime = in.readLong();
        this.mUptimeStart = in.readLong();
        this.mRealtime = in.readLong();
        this.mRealtimeStart = in.readLong();
        this.mOnBattery = in.readInt() != 0 ? true : USE_OLD_HISTORY;
        this.mEstimatedBatteryCapacity = in.readInt();
        this.mOnBatteryInternal = USE_OLD_HISTORY;
        this.mOnBatteryTimeBase.readFromParcel(in);
        this.mOnBatteryScreenOffTimeBase.readFromParcel(in);
        this.mScreenState = NETWORK_STATS_LAST;
        this.mScreenOnTimer = new StopwatchTimer(this.mClocks, null, -1, null, this.mOnBatteryTimeBase, in);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mScreenBrightnessTimer[i] = new StopwatchTimer(this.mClocks, null, -100 - i, null, this.mOnBatteryTimeBase, in);
        }
        this.mInteractive = USE_OLD_HISTORY;
        this.mInteractiveTimer = new StopwatchTimer(this.mClocks, null, -10, null, this.mOnBatteryTimeBase, in);
        this.mPhoneOn = USE_OLD_HISTORY;
        this.mPowerSaveModeEnabledTimer = new StopwatchTimer(this.mClocks, null, -2, null, this.mOnBatteryTimeBase, in);
        this.mLongestLightIdleTime = in.readLong();
        this.mLongestFullIdleTime = in.readLong();
        this.mDeviceIdleModeLightTimer = new StopwatchTimer(this.mClocks, null, -14, null, this.mOnBatteryTimeBase, in);
        this.mDeviceIdleModeFullTimer = new StopwatchTimer(this.mClocks, null, -11, null, this.mOnBatteryTimeBase, in);
        this.mDeviceLightIdlingTimer = new StopwatchTimer(this.mClocks, null, -15, null, this.mOnBatteryTimeBase, in);
        this.mDeviceIdlingTimer = new StopwatchTimer(this.mClocks, null, -12, null, this.mOnBatteryTimeBase, in);
        this.mPhoneOnTimer = new StopwatchTimer(this.mClocks, null, -3, null, this.mOnBatteryTimeBase, in);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -200 - i, null, this.mOnBatteryTimeBase, in);
        }
        this.mPhoneSignalScanningTimer = new StopwatchTimer(this.mClocks, null, -199, null, this.mOnBatteryTimeBase, in);
        for (i = NETWORK_STATS_LAST; i < 17; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneDataConnectionsTimer[i] = new StopwatchTimer(this.mClocks, null, -300 - i, null, this.mOnBatteryTimeBase, in);
        }
        for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
            this.mNetworkByteActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
            this.mNetworkPacketActivityCounters[i] = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        }
        this.mMobileRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mMobileRadioActiveTimer = new StopwatchTimer(this.mClocks, null, -400, null, this.mOnBatteryTimeBase, in);
        this.mMobileRadioActivePerAppTimer = new StopwatchTimer(this.mClocks, null, -401, null, this.mOnBatteryTimeBase, in);
        this.mMobileRadioActiveAdjustedTime = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mMobileRadioActiveUnknownTime = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mMobileRadioActiveUnknownCount = new LongSamplingCounter(this.mOnBatteryTimeBase, in);
        this.mWifiRadioPowerState = NUM_WIFI_TX_LEVELS;
        this.mWifiOn = USE_OLD_HISTORY;
        this.mWifiOnTimer = new StopwatchTimer(this.mClocks, null, -4, null, this.mOnBatteryTimeBase, in);
        this.mGlobalWifiRunning = USE_OLD_HISTORY;
        this.mGlobalWifiRunningTimer = new StopwatchTimer(this.mClocks, null, -5, null, this.mOnBatteryTimeBase, in);
        for (i = NETWORK_STATS_LAST; i < 8; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiStateTimer[i] = new StopwatchTimer(this.mClocks, null, -600 - i, null, this.mOnBatteryTimeBase, in);
        }
        for (i = NETWORK_STATS_LAST; i < 13; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSupplStateTimer[i] = new StopwatchTimer(this.mClocks, null, -700 - i, null, this.mOnBatteryTimeBase, in);
        }
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSignalStrengthsTimer[i] = new StopwatchTimer(this.mClocks, null, -800 - i, null, this.mOnBatteryTimeBase, in);
        }
        this.mWifiActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, NUM_WIFI_TX_LEVELS, in);
        this.mBluetoothActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, NUM_WIFI_TX_LEVELS, in);
        this.mModemActivity = new ControllerActivityCounterImpl(this.mOnBatteryTimeBase, 5, in);
        this.mHasWifiReporting = in.readInt() != 0 ? true : USE_OLD_HISTORY;
        this.mHasBluetoothReporting = in.readInt() != 0 ? true : USE_OLD_HISTORY;
        this.mHasModemReporting = in.readInt() != 0 ? true : USE_OLD_HISTORY;
        this.mNumConnectivityChange = in.readInt();
        this.mLoadedNumConnectivityChange = in.readInt();
        this.mUnpluggedNumConnectivityChange = in.readInt();
        this.mAudioOnNesting = NETWORK_STATS_LAST;
        this.mAudioOnTimer = new StopwatchTimer(this.mClocks, null, -7, null, this.mOnBatteryTimeBase);
        this.mVideoOnNesting = NETWORK_STATS_LAST;
        this.mVideoOnTimer = new StopwatchTimer(this.mClocks, null, -8, null, this.mOnBatteryTimeBase);
        this.mFlashlightOnNesting = NETWORK_STATS_LAST;
        this.mFlashlightOnTimer = new StopwatchTimer(this.mClocks, null, -9, null, this.mOnBatteryTimeBase, in);
        this.mCameraOnNesting = NETWORK_STATS_LAST;
        this.mCameraOnTimer = new StopwatchTimer(this.mClocks, null, -13, null, this.mOnBatteryTimeBase, in);
        this.mBluetoothScanNesting = NETWORK_STATS_LAST;
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
        for (int ikw = NETWORK_STATS_LAST; ikw < NKW; ikw += NUM_WIFI_TX_LEVELS) {
            if (in.readInt() != 0) {
                this.mKernelWakelockStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryScreenOffTimeBase, in));
            }
        }
        this.mWakeupReasonStats.clear();
        int NWR = in.readInt();
        for (int iwr = NETWORK_STATS_LAST; iwr < NWR; iwr += NUM_WIFI_TX_LEVELS) {
            if (in.readInt() != 0) {
                this.mWakeupReasonStats.put(in.readString(), new SamplingTimer(this.mClocks, this.mOnBatteryTimeBase, in));
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
        for (i = NETWORK_STATS_LAST; i < numUids; i += NUM_WIFI_TX_LEVELS) {
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
        writeToParcelLocked(out, USE_OLD_HISTORY, flags);
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
        writeHistory(out, true, USE_OLD_HISTORY);
        out.writeInt(this.mStartCount);
        out.writeLong(startClockTime);
        out.writeString(this.mStartPlatformVersion);
        out.writeString(this.mEndPlatformVersion);
        out.writeLong(this.mUptime);
        out.writeLong(this.mUptimeStart);
        out.writeLong(this.mRealtime);
        out.writeLong(this.mRealtimeStart);
        out.writeInt(this.mOnBattery ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
        out.writeInt(this.mEstimatedBatteryCapacity);
        this.mOnBatteryTimeBase.writeToParcel(out, uSecUptime, uSecRealtime);
        this.mOnBatteryScreenOffTimeBase.writeToParcel(out, uSecUptime, uSecRealtime);
        this.mScreenOnTimer.writeToParcel(out, uSecRealtime);
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
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
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneSignalStrengthsTimer[i].writeToParcel(out, uSecRealtime);
        }
        this.mPhoneSignalScanningTimer.writeToParcel(out, uSecRealtime);
        for (i = NETWORK_STATS_LAST; i < 17; i += NUM_WIFI_TX_LEVELS) {
            this.mPhoneDataConnectionsTimer[i].writeToParcel(out, uSecRealtime);
        }
        for (i = NETWORK_STATS_LAST; i < 6; i += NUM_WIFI_TX_LEVELS) {
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
        for (i = NETWORK_STATS_LAST; i < 8; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiStateTimer[i].writeToParcel(out, uSecRealtime);
        }
        for (i = NETWORK_STATS_LAST; i < 13; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSupplStateTimer[i].writeToParcel(out, uSecRealtime);
        }
        for (i = NETWORK_STATS_LAST; i < 5; i += NUM_WIFI_TX_LEVELS) {
            this.mWifiSignalStrengthsTimer[i].writeToParcel(out, uSecRealtime);
        }
        this.mWifiActivity.writeToParcel(out, NETWORK_STATS_LAST);
        this.mBluetoothActivity.writeToParcel(out, NETWORK_STATS_LAST);
        this.mModemActivity.writeToParcel(out, NETWORK_STATS_LAST);
        out.writeInt(this.mHasWifiReporting ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
        out.writeInt(this.mHasBluetoothReporting ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
        out.writeInt(this.mHasModemReporting ? NUM_WIFI_TX_LEVELS : NETWORK_STATS_LAST);
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
            for (Map.Entry<String, SamplingTimer> ent : this.mKernelWakelockStats.entrySet()) {
                SamplingTimer kwlt = (SamplingTimer) ent.getValue();
                if (kwlt != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    out.writeString((String) ent.getKey());
                    kwlt.writeToParcel(out, uSecRealtime);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
            }
            out.writeInt(this.mWakeupReasonStats.size());
            for (Map.Entry<String, SamplingTimer> ent2 : this.mWakeupReasonStats.entrySet()) {
                SamplingTimer timer = (SamplingTimer) ent2.getValue();
                if (timer != null) {
                    out.writeInt(NUM_WIFI_TX_LEVELS);
                    out.writeString((String) ent2.getKey());
                    timer.writeToParcel(out, uSecRealtime);
                } else {
                    out.writeInt(NETWORK_STATS_LAST);
                }
            }
        } else {
            out.writeInt(NETWORK_STATS_LAST);
        }
        if (inclUids) {
            int size = this.mUidStats.size();
            out.writeInt(size);
            for (i = NETWORK_STATS_LAST; i < size; i += NUM_WIFI_TX_LEVELS) {
                out.writeInt(this.mUidStats.keyAt(i));
                ((Uid) this.mUidStats.valueAt(i)).writeToParcelLocked(out, uSecRealtime);
            }
            return;
        }
        out.writeInt(NETWORK_STATS_LAST);
    }

    public void prepareForDumpLocked() {
        pullPendingStateUpdatesLocked();
        getStartClockTime();
    }

    public void dumpLocked(Context context, PrintWriter pw, int flags, int reqUid, long histStart) {
        super.dumpLocked(context, pw, flags, reqUid, histStart);
    }
}
