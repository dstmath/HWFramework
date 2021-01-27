package com.android.server.wifi;

import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WakeupConfigStoreData;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WakeupLock {
    @VisibleForTesting
    static final int CONSECUTIVE_MISSED_SCANS_REQUIRED_TO_EVICT = 5;
    @VisibleForTesting
    static final long MAX_LOCK_TIME_MILLIS = 600000;
    private static final String TAG = WakeupLock.class.getSimpleName();
    private final Clock mClock;
    private boolean mIsInitialized;
    private long mLockTimestamp;
    private final Map<ScanResultMatchInfo, Integer> mLockedNetworks = new ArrayMap();
    private int mNumScans;
    private boolean mVerboseLoggingEnabled;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiWakeMetrics mWifiWakeMetrics;

    public WakeupLock(WifiConfigManager wifiConfigManager, WifiWakeMetrics wifiWakeMetrics, Clock clock) {
        this.mWifiConfigManager = wifiConfigManager;
        this.mWifiWakeMetrics = wifiWakeMetrics;
        this.mClock = clock;
    }

    public void setLock(Collection<ScanResultMatchInfo> scanResultList) {
        this.mLockTimestamp = this.mClock.getElapsedSinceBootMillis();
        this.mIsInitialized = false;
        this.mNumScans = 0;
        this.mLockedNetworks.clear();
        for (ScanResultMatchInfo scanResultMatchInfo : scanResultList) {
            this.mLockedNetworks.put(scanResultMatchInfo, 5);
        }
        String str = TAG;
        Log.d(str, "Lock set. Number of networks: " + this.mLockedNetworks.size());
        this.mWifiConfigManager.saveToStore(false);
    }

    private void maybeSetInitializedByScans(int numScans) {
        if (!this.mIsInitialized) {
            if (numScans >= 5) {
                this.mIsInitialized = true;
                String str = TAG;
                Log.d(str, "Lock initialized by handled scans. Scans: " + numScans);
                if (this.mVerboseLoggingEnabled) {
                    String str2 = TAG;
                    Log.d(str2, "State of lock: " + this.mLockedNetworks);
                }
                this.mWifiWakeMetrics.recordInitializeEvent(this.mNumScans, this.mLockedNetworks.size());
            }
        }
    }

    private void maybeSetInitializedByTimeout(long timestampMillis) {
        if (!this.mIsInitialized) {
            long elapsedTime = timestampMillis - this.mLockTimestamp;
            if (elapsedTime > 600000) {
                this.mIsInitialized = true;
                String str = TAG;
                Log.d(str, "Lock initialized by timeout. Elapsed time: " + elapsedTime);
                if (this.mNumScans == 0) {
                    Log.w(TAG, "Lock initialized with 0 handled scans!");
                }
                if (this.mVerboseLoggingEnabled) {
                    String str2 = TAG;
                    Log.d(str2, "State of lock: " + this.mLockedNetworks);
                }
                this.mWifiWakeMetrics.recordInitializeEvent(this.mNumScans, this.mLockedNetworks.size());
            }
        }
    }

    public boolean isInitialized() {
        return this.mIsInitialized;
    }

    private void addToLock(Collection<ScanResultMatchInfo> networkList) {
        if (this.mVerboseLoggingEnabled) {
            String str = TAG;
            Log.d(str, "Initializing lock with networks: " + networkList);
        }
        boolean hasChanged = false;
        for (ScanResultMatchInfo network : networkList) {
            if (!this.mLockedNetworks.containsKey(network)) {
                this.mLockedNetworks.put(network, 5);
                hasChanged = true;
            }
        }
        if (hasChanged) {
            this.mWifiConfigManager.saveToStore(false);
        }
        maybeSetInitializedByScans(this.mNumScans);
    }

    private void removeFromLock(Collection<ScanResultMatchInfo> networkList) {
        if (this.mVerboseLoggingEnabled) {
            String str = TAG;
            Log.d(str, "Filtering lock with networks: " + networkList);
        }
        boolean hasChanged = false;
        Iterator<Map.Entry<ScanResultMatchInfo, Integer>> it = this.mLockedNetworks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ScanResultMatchInfo, Integer> entry = it.next();
            if (networkList.contains(entry.getKey())) {
                if (this.mVerboseLoggingEnabled) {
                    String str2 = TAG;
                    Log.d(str2, "Found network in lock: " + StringUtilEx.safeDisplaySsid(entry.getKey().networkSsid));
                }
                entry.setValue(5);
            } else {
                entry.setValue(Integer.valueOf(entry.getValue().intValue() - 1));
                if (entry.getValue().intValue() <= 0) {
                    String str3 = TAG;
                    Log.d(str3, "Removed network from lock: " + StringUtilEx.safeDisplaySsid(entry.getKey().networkSsid));
                    it.remove();
                    hasChanged = true;
                }
            }
        }
        if (hasChanged) {
            this.mWifiConfigManager.saveToStore(false);
        }
        if (isUnlocked()) {
            Log.d(TAG, "Lock emptied. Recording unlock event.");
            this.mWifiWakeMetrics.recordUnlockEvent(this.mNumScans);
        }
    }

    public void update(Collection<ScanResultMatchInfo> networkList) {
        if (!isUnlocked()) {
            maybeSetInitializedByTimeout(this.mClock.getElapsedSinceBootMillis());
            this.mNumScans++;
            if (this.mIsInitialized) {
                removeFromLock(networkList);
            } else {
                addToLock(networkList);
            }
        }
    }

    public boolean isUnlocked() {
        return this.mIsInitialized && this.mLockedNetworks.isEmpty();
    }

    public WakeupConfigStoreData.DataSource<Set<ScanResultMatchInfo>> getDataSource() {
        return new WakeupLockDataSource();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WakeupLock: ");
        pw.println("mNumScans: " + this.mNumScans);
        pw.println("mIsInitialized: " + this.mIsInitialized);
        pw.println("Locked networks: " + this.mLockedNetworks.size());
        for (Map.Entry<ScanResultMatchInfo, Integer> entry : this.mLockedNetworks.entrySet()) {
            pw.println(entry.getKey() + ", scans to evict: " + entry.getValue());
        }
    }

    public void enableVerboseLogging(boolean enabled) {
        this.mVerboseLoggingEnabled = enabled;
    }

    /* access modifiers changed from: private */
    public class WakeupLockDataSource implements WakeupConfigStoreData.DataSource<Set<ScanResultMatchInfo>> {
        private WakeupLockDataSource() {
        }

        @Override // com.android.server.wifi.WakeupConfigStoreData.DataSource
        public Set<ScanResultMatchInfo> getData() {
            return WakeupLock.this.mLockedNetworks.keySet();
        }

        public void setData(Set<ScanResultMatchInfo> data) {
            WakeupLock.this.mLockedNetworks.clear();
            for (ScanResultMatchInfo network : data) {
                WakeupLock.this.mLockedNetworks.put(network, 5);
            }
            WakeupLock.this.mIsInitialized = true;
        }
    }
}
