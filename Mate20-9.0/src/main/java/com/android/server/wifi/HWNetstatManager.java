package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkStats;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HWNetstatManager {
    private static final int IDX_IFACE = 1;
    private static final int IDX_RXBYTES = 5;
    private static final int IDX_RXPACKETS = 6;
    private static final int IDX_SET = 4;
    private static final int IDX_TX_BYTES = 7;
    private static final int IDX_TX_PACKETS = 8;
    private static final int IDX_UID = 3;
    private static final int INITIAL_SIZE = 24;
    private static final int MAX_LOG_TRAFFIC = 11;
    private static final int MIN_SEG_LENGTH = 9;
    private static final String TAG = "HWNetstatManager";
    private static final String TAG_STAT = "hw_netstat";
    private static ArrayList<NetworkStats.Entry> mNetworkStatsEntryList = new ArrayList<>();
    private static NetworkStats mStats;
    private static long mTotalRxByte = 0;
    private static long mTotalTxByte = 0;
    private Comparator<NetworkStats.Entry> mComparator = new Comparator<NetworkStats.Entry>() {
        public int compare(NetworkStats.Entry left, NetworkStats.Entry right) {
            if (left.txBytes + left.rxBytes > right.txBytes + right.rxBytes) {
                return -1;
            }
            if (left.txBytes + left.rxBytes < right.txBytes + right.rxBytes) {
                return 1;
            }
            return 0;
        }
    };
    private Context mContext = null;
    private NetworkStats mLastStats = null;
    private SparseArray<String> mPackageTables = null;

    private static native void class_init_native();

    private static native int nativeReadWifiNetworkStatsDetail();

    private native void native_init();

    static {
        System.loadLibrary("huaweiwifi-service");
        class_init_native();
    }

    private static NetworkStats readNetworkStatsDetail() {
        mStats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
        if (nativeReadWifiNetworkStatsDetail() == 0) {
            return mStats.groupedByUid();
        }
        return null;
    }

    private void reportNetworkStatsDetail(int uid, int set, long rxBytes, long rxPackets, long txBytes, long txPackets) {
        NetworkStats.Entry entry = new NetworkStats.Entry();
        entry.uid = uid;
        entry.set = set;
        entry.rxBytes = rxBytes;
        entry.rxPackets = rxPackets;
        entry.txBytes = txBytes;
        entry.txPackets = txPackets;
        mStats.addValues(entry);
    }

    public HWNetstatManager(Context cxt) {
        this.mContext = cxt;
        this.mPackageTables = new SparseArray<>(49);
        native_init();
    }

    public void resetNetstats() {
        this.mLastStats = null;
    }

    public void performPollAndLog() {
        try {
            NetworkStats stats = readNetworkStatsDetail();
            if (this.mLastStats != null) {
                ArrayList<NetworkStats.Entry> entryList = getIncrementalStats(stats, this.mLastStats);
                mNetworkStatsEntryList = (ArrayList) entryList.clone();
                logNetstatTraffic(entryList);
                entryList.clear();
            } else {
                Log.d(TAG, "get base netstat");
            }
            this.mLastStats = stats;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logNetstatTraffic(ArrayList<NetworkStats.Entry> entryList) {
        int size = entryList.size();
        int maxcount = 11;
        if (size < 11) {
            maxcount = size;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxcount; i++) {
            NetworkStats.Entry entry = entryList.get(i);
            if (i != 0) {
                if (entry.txBytes <= 0 && entry.rxBytes <= 0) {
                    break;
                }
                sb.append("," + getPackageName(entry.uid) + "/" + entry.rxBytes + "/" + entry.txBytes);
            } else {
                sb.append(getPackageName(entry.uid) + "/" + entry.rxBytes + "/" + entry.txBytes);
            }
        }
        Log.d(TAG_STAT, sb.toString());
    }

    private NetworkStats.Entry getIncrementalStatsTotal(NetworkStats left, NetworkStats right) {
        if (left == null) {
            return new NetworkStats.Entry();
        }
        NetworkStats.Entry leftEntry = left.getTotal(null);
        if (right != null) {
            NetworkStats.Entry rightEntry = right.getTotal(null);
            leftEntry.rxBytes -= rightEntry.rxBytes;
            leftEntry.rxPackets -= rightEntry.rxPackets;
            leftEntry.txBytes -= rightEntry.txBytes;
            leftEntry.txPackets -= rightEntry.txPackets;
        }
        return leftEntry;
    }

    private ArrayList<NetworkStats.Entry> getIncrementalStats(NetworkStats left, NetworkStats right) {
        int idx;
        ArrayList<NetworkStats.Entry> list = new ArrayList<>();
        if (left == null) {
            list.add(new NetworkStats.Entry());
            return list;
        }
        for (int i = 0; i < left.size(); i++) {
            NetworkStats.Entry entry = left.getValues(i, new NetworkStats.Entry());
            if (right == null) {
                idx = -1;
            } else {
                idx = right.findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, entry.defaultNetwork);
            }
            if (idx >= 0) {
                NetworkStats.Entry baseentry = right.getValues(idx, new NetworkStats.Entry());
                entry.rxBytes -= baseentry.rxBytes;
                entry.rxPackets -= baseentry.rxPackets;
                entry.txBytes -= baseentry.txBytes;
                entry.txPackets -= baseentry.txPackets;
            }
            list.add(entry);
        }
        Collections.sort(list, this.mComparator);
        NetworkStats.Entry entry2 = getIncrementalStatsTotal(left, right);
        mTotalTxByte = entry2.txBytes;
        mTotalRxByte = entry2.rxBytes;
        list.add(0, entry2);
        return list;
    }

    private String getPackageName(int uid) {
        if (uid == -1) {
            return "total";
        }
        int keyIdx = this.mPackageTables.indexOfKey(uid);
        if (keyIdx >= 0) {
            return this.mPackageTables.valueAt(keyIdx);
        }
        String name = this.mContext.getPackageManager().getNameForUid(uid);
        if (TextUtils.isEmpty(name)) {
            name = "unknown:" + uid;
        }
        this.mPackageTables.put(uid, name);
        return name;
    }

    public long getUidRxBytes(int uid) {
        int size = 0;
        if (mNetworkStatsEntryList != null) {
            size = mNetworkStatsEntryList.size();
        }
        for (int i = 0; i < size; i++) {
            NetworkStats.Entry entry = mNetworkStatsEntryList.get(i);
            if (entry.uid == uid) {
                return entry.rxBytes;
            }
        }
        Log.d(TAG_STAT, "Not found the uid's RxBytes " + uid);
        return 0;
    }

    public long getUidTxBytes(int uid) {
        int size = 0;
        if (mNetworkStatsEntryList != null) {
            size = mNetworkStatsEntryList.size();
        }
        for (int i = 0; i < size; i++) {
            NetworkStats.Entry entry = mNetworkStatsEntryList.get(i);
            if (entry.uid == uid) {
                return entry.txBytes;
            }
        }
        Log.d(TAG_STAT, "Not found the uid's TxBytes " + uid);
        return 0;
    }

    public long getTxBytes() {
        return mTotalTxByte;
    }

    public long getRxBytes() {
        return mTotalRxByte;
    }
}
