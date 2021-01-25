package ohos.net;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class DataFlowStatistics {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "DATASTATIS");
    private static final int TAG_RX_BYTES = 0;
    private static final int TAG_TX_BYTES = 2;

    public static long getCellularRxBytes() {
        long j = 0;
        for (String str : getCellularIfaces()) {
            j += getIfaceRxBytes(str);
        }
        return j;
    }

    public static long getCellularTxBytes() {
        long j = 0;
        for (String str : getCellularIfaces()) {
            j += getIfaceTxBytes(str);
        }
        return j;
    }

    public static long getAllRxBytes() {
        try {
            return NetManagerProxy.getInstance().getAllStatis(0);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getAllRxBytes.", new Object[0]);
            return 0;
        }
    }

    public static long getAllTxBytes() {
        try {
            return NetManagerProxy.getInstance().getAllStatis(2);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getAllTxBytes.", new Object[0]);
            return 0;
        }
    }

    public static long getUidRxBytes(int i) {
        try {
            return NetManagerProxy.getInstance().getUidStatis(i, 0);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getUidRxBytes.", new Object[0]);
            return 0;
        }
    }

    public static long getUidTxBytes(int i) {
        try {
            return NetManagerProxy.getInstance().getUidStatis(i, 2);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getUidTxBytes.", new Object[0]);
            return 0;
        }
    }

    public static long getIfaceRxBytes(String str) {
        try {
            return NetManagerProxy.getInstance().getIfaceStatis(str, 0);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getIfaceRxBytes.", new Object[0]);
            return 0;
        }
    }

    public static long getIfaceTxBytes(String str) {
        try {
            return NetManagerProxy.getInstance().getIfaceStatis(str, 2);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getIfaceTxBytes.", new Object[0]);
            return 0;
        }
    }

    private static String[] getCellularIfaces() {
        String[] strArr = new String[0];
        try {
            return NetManagerProxy.getInstance().getCellularIfaces();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getCellularIfaces.", new Object[0]);
            return strArr;
        }
    }
}
