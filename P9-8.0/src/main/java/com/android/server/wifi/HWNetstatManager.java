package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.wifi.HwCHRWifiGroundTraffic.HwCHRWifiTrafficItem;
import com.huawei.device.connectivitychrlog.CSubTRAFFIC_GROUND;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private static HwCHRWifiGroundTraffic mGound = new HwCHRWifiGroundTraffic();
    private static List<HwCHRWifiTrafficItem> mGroud_delta = null;
    private static ArrayList<Entry> mNetworkStatsEntryList = new ArrayList();
    private static NetworkStats mStats;
    private static long mTotalRxByte = 0;
    private static long mTotalTxByte = 0;
    private Comparator<Entry> mComparator = new Comparator<Entry>() {
        public int compare(Entry left, Entry right) {
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
        class_init_native();
    }

    private static NetworkStats readNetworkStatsDetail() {
        mStats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
        if (nativeReadWifiNetworkStatsDetail() != 0) {
            return null;
        }
        mGroud_delta = mGound.getDelta();
        return mStats.groupedByUid();
    }

    private void reportNetworkStatsDetail(int uid, int set, long rxBytes, long rxPackets, long txBytes, long txPackets) {
        Entry entry = new Entry();
        entry.uid = uid;
        entry.set = set;
        entry.rxBytes = rxBytes;
        entry.rxPackets = rxPackets;
        entry.txBytes = txBytes;
        entry.txPackets = txPackets;
        mStats.addValues(entry);
        mGound.add(entry.set, entry.txBytes, entry.rxBytes);
    }

    public HWNetstatManager(Context cxt) {
        this.mContext = cxt;
        this.mPackageTables = new SparseArray(49);
        native_init();
    }

    public void resetNetstats() {
        this.mLastStats = null;
    }

    public void performPollAndLog() {
        try {
            NetworkStats stats = readNetworkStatsDetail();
            if (this.mLastStats != null) {
                ArrayList<Entry> entryList = getIncrementalStats(stats, this.mLastStats);
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

    private void logNetstatTraffic(ArrayList<Entry> entryList) {
        int size = entryList.size();
        int maxcount = size < 11 ? size : 11;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxcount; i++) {
            Entry entry = (Entry) entryList.get(i);
            if (i != 0) {
                if (entry.txBytes <= 0 && entry.rxBytes <= 0) {
                    break;
                }
                sb.append(",").append(getPackageName(entry.uid)).append("/").append(entry.rxBytes).append("/").append(entry.txBytes);
            } else {
                sb.append(getPackageName(entry.uid)).append("/").append(entry.rxBytes).append("/").append(entry.txBytes);
            }
        }
        Log.d(TAG_STAT, sb.toString());
    }

    private Entry getIncrementalStatsTotal(NetworkStats left, NetworkStats right) {
        if (left == null) {
            return new Entry();
        }
        Entry leftEntry = left.getTotal(null);
        if (right != null) {
            Entry rightEntry = right.getTotal(null);
            leftEntry.rxBytes -= rightEntry.rxBytes;
            leftEntry.rxPackets -= rightEntry.rxPackets;
            leftEntry.txBytes -= rightEntry.txBytes;
            leftEntry.txPackets -= rightEntry.txPackets;
        }
        return leftEntry;
    }

    private ArrayList<Entry> getIncrementalStats(NetworkStats left, NetworkStats right) {
        ArrayList<Entry> list = new ArrayList();
        if (left == null) {
            list.add(new Entry());
            return list;
        }
        Entry entry;
        for (int i = 0; i < left.size(); i++) {
            int idx;
            entry = left.getValues(i, new Entry());
            if (right == null) {
                idx = -1;
            } else {
                idx = right.findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming);
            }
            if (idx >= 0) {
                Entry baseentry = right.getValues(idx, new Entry());
                entry.rxBytes -= baseentry.rxBytes;
                entry.rxPackets -= baseentry.rxPackets;
                entry.txBytes -= baseentry.txBytes;
                entry.txPackets -= baseentry.txPackets;
            }
            list.add(entry);
        }
        Collections.sort(list, this.mComparator);
        entry = getIncrementalStatsTotal(left, right);
        mTotalTxByte = entry.txBytes;
        mTotalRxByte = entry.rxBytes;
        list.add(0, entry);
        return list;
    }

    private String getPackageName(int uid) {
        if (uid == -1) {
            return "total";
        }
        int keyIdx = this.mPackageTables.indexOfKey(uid);
        if (keyIdx >= 0) {
            return (String) this.mPackageTables.valueAt(keyIdx);
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
            Entry entry = (Entry) mNetworkStatsEntryList.get(i);
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
            Entry entry = (Entry) mNetworkStatsEntryList.get(i);
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

    public static CSubTRAFFIC_GROUND getBack_Front_Summery() {
        CSubTRAFFIC_GROUND groud = new CSubTRAFFIC_GROUND();
        if (mGroud_delta != null) {
            groud.ifore_send_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(0)).getTx());
            groud.ifore_recv_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(0)).getRx());
            groud.iback_send_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(1)).getTx());
            groud.iback_recv_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(1)).getRx());
        }
        return groud;
    }
}
