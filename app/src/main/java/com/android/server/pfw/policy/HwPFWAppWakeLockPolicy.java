package com.android.server.pfw.policy;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Wakelock;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.SparseArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.pfw.HwPFWService;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HwPFWAppWakeLockPolicy extends HwPFWPolicy {
    private static final int GATE_UID_WAKELOCK_TIME = 10;
    private static final String GMS_PKG_NAME = "com.google.android.gms";
    private static final long MAX_PARTIAL_WAKELOCK_TIME = 18000000;
    private static final long MAX_UID_CLEAN_PART_WAKELOCK = 60;
    private static final long MICROSECONDS_PER_MIN = 60000000;
    private static final String QQ_PKG_NAME = "com.tencent.mobileqq";
    private static final String TAG = "PFW.HwPFWAppWakeLockPolicy";
    private static final String WECHAT_PKG_NAME = "com.tencent.mm";
    private static final String skipTags = "AudioMix AudioIn AudioDup AudioDirectOut AudioOffload android.media.MediaPlayer LocationManagerService";
    private boolean mEnabled;
    private int mGmsUid;
    private long mInitAppWakeLockTime;
    private boolean mIsScrOff;
    private Map<Integer, Long> mLastAcquireWakelock;
    private int mQQUid;
    private Runnable mScrOffWakeLock;
    private HwPFWService mService;
    private Map<Integer, Long> mUidWakeLockMap;
    private Handler mWakelockHandler;
    private int mWechatUid;

    public HwPFWAppWakeLockPolicy(Context context, HwPFWService service) {
        super(context);
        this.mLastAcquireWakelock = new HashMap();
        this.mUidWakeLockMap = new HashMap();
        this.mInitAppWakeLockTime = 0;
        this.mIsScrOff = false;
        this.mWechatUid = -1;
        this.mQQUid = -1;
        this.mGmsUid = -1;
        this.mWakelockHandler = null;
        this.mEnabled = true;
        this.mScrOffWakeLock = new Runnable() {
            public void run() {
                HwPFWLogger.d(HwPFWAppWakeLockPolicy.TAG, "scroff wakelock runnable");
                if (HwPFWAppWakeLockPolicy.this.mIsScrOff) {
                    new Thread() {
                        public void run() {
                            HwPFWAppWakeLockPolicy.this.calcScrOffWakeLock(false);
                        }
                    }.start();
                }
            }
        };
        this.mContext = context;
        this.mService = service;
        this.mWakelockHandler = new Handler();
    }

    public void handleBroadcastIntent(Intent intent) {
        if (this.mEnabled) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    HwPFWLogger.d(TAG, "receive ACTION_BOOT_COMPLETED");
                    calcScrOffWakeLock(true);
                    this.mInitAppWakeLockTime = SystemClock.elapsedRealtime();
                    this.mWechatUid = getAppUid(WECHAT_PKG_NAME);
                    this.mQQUid = getAppUid(QQ_PKG_NAME);
                    this.mGmsUid = getAppUid(GMS_PKG_NAME);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    this.mIsScrOff = true;
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    this.mIsScrOff = false;
                } else if (action.equals(HwPFWService.ACTION_PFW_WAKEUP_TIMER)) {
                    if (this.mIsScrOff) {
                        HwPFWLogger.d(TAG, "ACTION_PFW_WAKEUP_TIMER");
                        this.mWakelockHandler.postDelayed(this.mScrOffWakeLock, 0);
                    }
                } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                    HwPFWLogger.d(TAG, "receive Intent.ACTION_POWER_DISCONNECTED");
                    if (this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, 0) >= 90) {
                        synchronized (this.mLastAcquireWakelock) {
                            HwPFWLogger.d(TAG, "clear mLastAcquireWakelock");
                            this.mLastAcquireWakelock.clear();
                        }
                    }
                }
            }
        }
    }

    private void cloneWakeLockHashMap(Map<Integer, Long> src, Map<Integer, Long> dst) {
        dst.clear();
        for (Entry<Integer, Long> ent : src.entrySet()) {
            dst.put((Integer) ent.getKey(), (Long) ent.getValue());
        }
    }

    private int getAppUid(String pac) {
        ApplicationInfo ai = null;
        try {
            ai = this.mContext.getPackageManager().getApplicationInfo(pac, 0);
        } catch (Exception e) {
            HwPFWLogger.d(TAG, "failed to get application info");
        }
        if (ai != null) {
            return ai.uid;
        }
        return -1;
    }

    private void calcScrOffWakeLock(boolean isFstTimes) {
        if (isFstTimes || this.mIsScrOff) {
            long now = SystemClock.elapsedRealtime();
            getUidWakeLock(0);
            if (isFstTimes) {
                synchronized (this.mLastAcquireWakelock) {
                    cloneWakeLockHashMap(this.mUidWakeLockMap, this.mLastAcquireWakelock);
                }
                return;
            }
            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
            for (Entry<Integer, Long> ent : this.mUidWakeLockMap.entrySet()) {
                int uid = ((Integer) ent.getKey()).intValue();
                long wakelockTime = ((Long) ent.getValue()).longValue();
                long lastWakelockTime = 0;
                if (this.mLastAcquireWakelock.containsKey(Integer.valueOf(uid))) {
                    lastWakelockTime = ((Long) this.mLastAcquireWakelock.get(Integer.valueOf(uid))).longValue();
                }
                long wakelockDuration = wakelockTime - lastWakelockTime;
                boolean isUsingSkipWl = false;
                for (String tag : skipTags.split(" ")) {
                    if (pm.isUsingSkipWakeLock(uid, tag)) {
                        HwPFWLogger.d(TAG, "uid = " + uid + " wakelock tag = " + tag + " is using one of skipTags!");
                        isUsingSkipWl = true;
                        this.mLastAcquireWakelock.put(Integer.valueOf(uid), Long.valueOf(wakelockTime));
                        break;
                    }
                }
                if (isUsingSkipWl) {
                    HwPFWLogger.d(TAG, "isUsingSkipWl = " + isUsingSkipWl + " uid = " + uid + " skip and continue");
                } else if (!(uid == this.mWechatUid || uid == this.mQQUid || uid == this.mGmsUid || wakelockDuration < MAX_UID_CLEAN_PART_WAKELOCK)) {
                    HwPFWLogger.w(TAG, "uid: " + uid + " wakelock > 60 mins");
                    forceStopAbnormalWakelockApp(uid);
                    synchronized (this.mLastAcquireWakelock) {
                        this.mLastAcquireWakelock.put(Integer.valueOf(uid), Long.valueOf(wakelockTime));
                    }
                }
            }
            if (now - this.mInitAppWakeLockTime >= MAX_PARTIAL_WAKELOCK_TIME) {
                cloneWakeLockHashMap(this.mUidWakeLockMap, this.mLastAcquireWakelock);
                this.mInitAppWakeLockTime = now;
            }
        }
    }

    public void getUidWakeLock(int which) {
        synchronized (this.mService.getBatteryLockObject()) {
            BatteryStatsHelper bsh = this.mService.getBatteryStatsHelper();
            BatteryStatsImpl batteryStatsImpl = null;
            if (bsh != null) {
                bsh.clearStats();
                batteryStatsImpl = (BatteryStatsImpl) bsh.getStats();
            }
            if (batteryStatsImpl == null) {
                return;
            }
            SparseArray<? extends Uid> uidStats = batteryStatsImpl.getUidStats();
            if (uidStats == null) {
                return;
            }
            int NU = uidStats.size();
            long uidWakelockTime = 0;
            for (int iu = 0; iu < NU; iu++) {
                Uid u = (Uid) uidStats.valueAt(iu);
                if (u != null) {
                    int uid = u.getUid();
                    if (uid >= 10000) {
                        Map<String, ? extends Wakelock> wakelocks = u.getWakelockStats();
                        if (wakelocks != null) {
                            if (wakelocks.size() > 0) {
                                for (Entry<String, ? extends Wakelock> ent : wakelocks.entrySet()) {
                                    Wakelock wl = (Wakelock) ent.getValue();
                                    String tag = (String) ent.getKey();
                                    if (!skipTags.contains(tag)) {
                                        Timer partialWakeTimer = wl.getWakeTime(0);
                                        if (partialWakeTimer != null) {
                                            long totalTimeMicros = partialWakeTimer.getTotalTimeLocked(SystemClock.elapsedRealtime(), which);
                                            if (totalTimeMicros > 0) {
                                                uidWakelockTime += totalTimeMicros;
                                            }
                                        }
                                    }
                                }
                            }
                            uidWakelockTime /= MICROSECONDS_PER_MIN;
                            HwPFWLogger.d(TAG, "getUidWakeLock uid: " + uid + " wakelock(minutes): " + uidWakelockTime);
                            if (uidWakelockTime >= 10) {
                                HwPFWLogger.d(TAG, "getUidWakeLock uid: " + u.getUid() + " wakelock >= 10 mins");
                                this.mUidWakeLockMap.put(Integer.valueOf(u.getUid()), Long.valueOf(uidWakelockTime));
                            }
                            uidWakelockTime = 0;
                        }
                    }
                }
            }
        }
    }

    private void forceStopAbnormalWakelockApp(int uid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (packages != null) {
            HwPFWLogger.d(TAG, "force stop abnormal wakelock app uid: " + uid);
            for (String pac : packages) {
                if (!"abbanza.bixpe.orange.dispositivos.android".equals(pac)) {
                    this.mService.getActivityManager().forceStopPackage(pac);
                }
            }
        }
    }
}
