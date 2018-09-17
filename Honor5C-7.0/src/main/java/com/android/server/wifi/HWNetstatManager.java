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
    private static HwCHRWifiGroundTraffic mGound;
    private static List<HwCHRWifiTrafficItem> mGroud_delta;
    private static NetworkStats mStats;
    private Comparator<Entry> mComparator;
    private Context mContext;
    private NetworkStats mLastStats;
    private SparseArray<String> mPackageTables;
    private long mTotalRxByte;
    private long mTotalTxByte;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HWNetstatManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HWNetstatManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HWNetstatManager.<clinit>():void");
    }

    private static native void class_init_native();

    private static native int nativeReadWifiNetworkStatsDetail();

    private native void native_init();

    private static NetworkStats readNetworkStatsDetail() {
        mStats = new NetworkStats(SystemClock.elapsedRealtime(), INITIAL_SIZE);
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
        this.mContext = null;
        this.mLastStats = null;
        this.mPackageTables = null;
        this.mTotalTxByte = 0;
        this.mTotalRxByte = 0;
        this.mComparator = new Comparator<Entry>() {
            public int compare(Entry left, Entry right) {
                if (left.txBytes + left.rxBytes > right.txBytes + right.rxBytes) {
                    return -1;
                }
                if (left.txBytes + left.rxBytes < right.txBytes + right.rxBytes) {
                    return HWNetstatManager.IDX_IFACE;
                }
                return 0;
            }
        };
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
        int maxcount = size < MAX_LOG_TRAFFIC ? size : MAX_LOG_TRAFFIC;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxcount; i += IDX_IFACE) {
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
        for (int i = 0; i < left.size(); i += IDX_IFACE) {
            int idx;
            entry = left.getValues(i, new Entry());
            if (right == null) {
                idx = -1;
            } else {
                idx = right.findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.roaming);
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
        this.mTotalTxByte = entry.txBytes;
        this.mTotalRxByte = entry.rxBytes;
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

    public long getTxBytes() {
        return this.mTotalTxByte;
    }

    public long getRxBytes() {
        return this.mTotalRxByte;
    }

    public static CSubTRAFFIC_GROUND getBack_Front_Summery() {
        CSubTRAFFIC_GROUND groud = new CSubTRAFFIC_GROUND();
        if (mGroud_delta != null) {
            groud.ifore_send_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(0)).getTx());
            groud.ifore_recv_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(0)).getRx());
            groud.iback_send_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(IDX_IFACE)).getTx());
            groud.iback_recv_bytes.setValue((int) ((HwCHRWifiTrafficItem) mGroud_delta.get(IDX_IFACE)).getRx());
        }
        return groud;
    }
}
