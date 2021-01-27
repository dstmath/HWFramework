package com.android.server.hidata;

import android.util.wifi.HwHiLog;
import com.android.server.hidata.arbitration.HwArbitrationDefs;

public class HwHiDataJniAdapter {
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwHiDataJniAdapter.class.getSimpleName());
    private static HwHiDataJniAdapter sHwHiDataJniAdapter = null;

    private native int nativeBindUidProcessToNetwork(int i, int i2);

    private native int nativeCloseProcessSockets(int i, int i2);

    private native int nativeGetCurFrameDetectResult();

    private native long[] nativeGetCurrTotalTraffic();

    private native String nativeGetWmpCoreTrainData(String[] strArr, String[] strArr2);

    private native int nativeHandleSocketStrategy(int i, int i2);

    private native HwQoeUdpNetworkInfo nativeReadUdpNetworkStatsDetail(int i, int i2);

    private native int nativeResetProcessSockets(int i);

    private native int[] nativeSendQoeCmd(int i, int i2);

    private native int nativeSendQoeStallDetectCmd(int i, int i2, int i3, int i4, int i5);

    private native int nativeSetDpiMarkRule(int i, int i2, int i3);

    private native int nativeStartFrameDetect(int i);

    private native int nativeStopFrameDetect();

    private native String nativeWmpCoreClusterData(String[] strArr, String[] strArr2);

    private native String nativeWmpCoreTrainData(String[] strArr, String[] strArr2);

    static {
        try {
            System.loadLibrary("hwqoe_jni");
            HwHiLog.d(TAG, false, "loading JNI success", new Object[0]);
        } catch (SecurityException | UnsatisfiedLinkError e) {
            HwHiLog.d(TAG, false, "LoadLibrary is error %{public}s", new Object[]{e.getMessage()});
        }
    }

    private HwHiDataJniAdapter() {
    }

    public static synchronized HwHiDataJniAdapter getInstance() {
        HwHiDataJniAdapter hwHiDataJniAdapter;
        synchronized (HwHiDataJniAdapter.class) {
            if (sHwHiDataJniAdapter == null) {
                sHwHiDataJniAdapter = new HwHiDataJniAdapter();
            }
            hwHiDataJniAdapter = sHwHiDataJniAdapter;
        }
        return hwHiDataJniAdapter;
    }

    public synchronized int[] sendQoeCmd(int cmd, int arg) {
        return nativeSendQoeCmd(cmd, arg);
    }

    public synchronized int setDpiMarkRule(int uid, int protocol, int enable) {
        return nativeSetDpiMarkRule(uid, protocol, enable);
    }

    public synchronized HwQoeUdpNetworkInfo readUdpNetworkStatsDetail(int uid, int network) {
        return nativeReadUdpNetworkStatsDetail(uid, network);
    }

    public synchronized int bindUidProcessToNetwork(int netId, int uid) {
        return nativeBindUidProcessToNetwork(netId, uid);
    }

    public synchronized int resetProcessSockets(int uid) {
        return nativeResetProcessSockets(uid);
    }

    public synchronized int closeProcessSockets(int strategy, int uid) {
        return nativeCloseProcessSockets(strategy, uid);
    }

    public synchronized int handleSocketStrategy(int strategy, int uid) {
        nativeHandleSocketStrategy(strategy, uid);
        return nativeCloseProcessSockets(strategy, uid);
    }

    public String wmpCoreTrainData(String[] localFingerData, String[] jsonParam) {
        return nativeWmpCoreTrainData(localFingerData, jsonParam);
    }

    public String getWmpCoreTrainData(String[] localFingerData, String[] jsonParam) {
        return nativeGetWmpCoreTrainData(localFingerData, jsonParam);
    }

    public String wmpCoreClusterData(String[] localFingerData, String[] jsonParam) {
        return nativeWmpCoreClusterData(localFingerData, jsonParam);
    }
}
