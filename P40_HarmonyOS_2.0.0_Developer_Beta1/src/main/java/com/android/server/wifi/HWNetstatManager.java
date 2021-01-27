package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkStats;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.wifi.HwHiLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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
    private static final String WIFI_INTERFACE = "wlan0";
    private static HWNetstatManager mHWNetstatManager;
    private static ArrayList<NetworkStats.Entry> mNetworkStatsEntryList = new ArrayList<>();
    private static NetworkStats mStats;
    private static long mTotalRxByte = 0;
    private static long mTotalTxByte = 0;
    private Comparator<NetworkStats.Entry> mComparator = new Comparator<NetworkStats.Entry>() {
        /* class com.android.server.wifi.HWNetstatManager.AnonymousClass1 */

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

    private HWNetstatManager(Context cxt) {
        this.mContext = cxt;
        this.mPackageTables = new SparseArray<>(49);
        native_init();
    }

    private NetworkStats getNetworkStatsDetail() {
        INetworkManagementService networkManagementService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        if (networkManagementService == null) {
            return null;
        }
        try {
            return networkManagementService.getNetworkStatsDetail();
        } catch (RemoteException | IllegalStateException e) {
            HwHiLog.d(TAG_STAT, false, "get network stats wrong", new Object[0]);
            return null;
        }
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

    public static HWNetstatManager getInstance(Context cxt) {
        if (mHWNetstatManager == null) {
            mHWNetstatManager = new HWNetstatManager(cxt);
        }
        return mHWNetstatManager;
    }

    public void resetNetstats() {
        this.mLastStats = null;
    }

    public void performPollAndLog() {
        try {
            NetworkStats stats = getNetworkStatsDetail();
            if (this.mLastStats != null) {
                ArrayList<NetworkStats.Entry> entryList = getIncrementalStats(stats, this.mLastStats);
                mNetworkStatsEntryList = (ArrayList) entryList.clone();
                logNetstatTraffic(entryList);
                entryList.clear();
            } else {
                HwHiLog.d(TAG, false, "get base netstat", new Object[0]);
            }
            this.mLastStats = stats;
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "performPollAndLog failed", new Object[0]);
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
        HwHiLog.d(TAG_STAT, false, "%{public}s", new Object[]{sb.toString()});
    }

    private NetworkStats.Entry getIncrementalStatsTotal(ArrayList<NetworkStats.Entry> entryList) {
        NetworkStats.Entry entry = new NetworkStats.Entry();
        for (int i = 0; i < entryList.size(); i++) {
            entry.rxBytes += entryList.get(i).rxBytes;
            entry.rxPackets += entryList.get(i).rxPackets;
            entry.txBytes += entryList.get(i).txBytes;
            entry.txPackets += entryList.get(i).txPackets;
        }
        return entry;
    }

    private ArrayList<NetworkStats.Entry> getIncrementalStats(NetworkStats left, NetworkStats right) {
        HashMap<Integer, NetworkStats.Entry> networkStatsMap = new HashMap<>();
        ArrayList<NetworkStats.Entry> list = new ArrayList<>();
        if (left == null) {
            list.add(new NetworkStats.Entry());
            return list;
        }
        for (int i = 0; i < left.size(); i++) {
            NetworkStats.Entry entry = left.getValues(i, new NetworkStats.Entry());
            if (entry.tag == 0 && WIFI_INTERFACE.equals(entry.iface)) {
                int idx = right == null ? -1 : right.findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, entry.defaultNetwork);
                if (idx >= 0) {
                    NetworkStats.Entry baseEntry = right.getValues(idx, new NetworkStats.Entry());
                    entry.rxBytes -= baseEntry.rxBytes;
                    entry.rxPackets -= baseEntry.rxPackets;
                    entry.txBytes -= baseEntry.txBytes;
                    entry.txPackets -= baseEntry.txPackets;
                }
                if (networkStatsMap.containsKey(Integer.valueOf(entry.uid))) {
                    NetworkStats.Entry entryTemp = networkStatsMap.get(Integer.valueOf(entry.uid));
                    entryTemp.rxBytes += entry.rxBytes;
                    entryTemp.rxPackets += entry.rxPackets;
                    entryTemp.txBytes += entry.txBytes;
                    entryTemp.txPackets += entry.txPackets;
                } else {
                    networkStatsMap.put(Integer.valueOf(entry.uid), entry);
                }
            }
        }
        ArrayList<NetworkStats.Entry> list2 = new ArrayList<>((Collection<? extends NetworkStats.Entry>) networkStatsMap.values());
        Collections.sort(list2, this.mComparator);
        NetworkStats.Entry entry2 = getIncrementalStatsTotal(list2);
        mTotalTxByte = entry2.txBytes;
        mTotalRxByte = entry2.rxBytes;
        list2.add(0, entry2);
        return list2;
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
        ArrayList<NetworkStats.Entry> arrayList = mNetworkStatsEntryList;
        if (arrayList != null) {
            size = arrayList.size();
        }
        for (int i = 0; i < size; i++) {
            NetworkStats.Entry entry = mNetworkStatsEntryList.get(i);
            if (entry.uid == uid) {
                return entry.rxBytes;
            }
        }
        HwHiLog.d(TAG_STAT, false, "Not found the uid's RxBytes %{public}d", new Object[]{Integer.valueOf(uid)});
        return 0;
    }

    public long getUidTxBytes(int uid) {
        int size = 0;
        ArrayList<NetworkStats.Entry> arrayList = mNetworkStatsEntryList;
        if (arrayList != null) {
            size = arrayList.size();
        }
        for (int i = 0; i < size; i++) {
            NetworkStats.Entry entry = mNetworkStatsEntryList.get(i);
            if (entry.uid == uid) {
                return entry.txBytes;
            }
        }
        HwHiLog.d(TAG_STAT, false, "Not found the uid's TxBytes %{public}d", new Object[]{Integer.valueOf(uid)});
        return 0;
    }

    public long getTxBytes() {
        return mTotalTxByte;
    }

    public long getRxBytes() {
        return mTotalRxByte;
    }
}
