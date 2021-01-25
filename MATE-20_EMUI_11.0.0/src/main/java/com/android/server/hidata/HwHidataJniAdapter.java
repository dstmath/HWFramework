package com.android.server.hidata;

import android.util.wifi.HwHiLog;
import com.android.server.hidata.histream.IHwHiStreamJniCallback;

public class HwHidataJniAdapter {
    private static final String TAG = "HiData_JniAdapter";
    private static HwHidataJniAdapter mHwHidataJniAdapter = null;
    private IHwHiStreamJniCallback mHiStreamJniCallback = null;

    private native int nativeBindUidProcessToNetwork(int i, int i2);

    private native int nativeCloseProcessSockets(int i, int i2);

    private native String nativeGetWmpCoreTrainData(String[] strArr, String[] strArr2);

    private native HwQoEUdpNetWorkInfo nativeReadUdpNetworkStatsDetail(int i, int i2);

    private native int nativeResetProcessSockets(int i);

    private native int[] nativeSendQoECmd(int i, int i2);

    private native int nativeSendQoeStallDetectCmd(int i, int i2, int i3, int i4, int i5);

    private native int nativeSetDpiMarkRule(int i, int i2, int i3);

    private native String nativeWmpCoreClusterData(String[] strArr, String[] strArr2);

    private native String nativeWmpCoreTrainData(String[] strArr, String[] strArr2);

    private native int nativehandleSocketStrategy(int i, int i2);

    public native int nativeGetCurFrameDetectResult();

    public native long[] nativeGetCurrTotalTraffic();

    public native int nativeStartFrameDetect(int i);

    public native int nativeStopFrameDetect();

    static {
        try {
            System.loadLibrary("hwqoe_jni");
            logD(false, "loading JNI succ", new Object[0]);
        } catch (UnsatisfiedLinkError e) {
            logD(false, "LoadLibrary is error %{public}s", e.getMessage());
        }
    }

    private HwHidataJniAdapter() {
    }

    public static synchronized HwHidataJniAdapter getInstance() {
        HwHidataJniAdapter hwHidataJniAdapter;
        synchronized (HwHidataJniAdapter.class) {
            if (mHwHidataJniAdapter == null) {
                mHwHidataJniAdapter = new HwHidataJniAdapter();
            }
            hwHidataJniAdapter = mHwHidataJniAdapter;
        }
        return hwHidataJniAdapter;
    }

    public void registerHiStreamJniCallback(IHwHiStreamJniCallback callback) {
        this.mHiStreamJniCallback = callback;
    }

    public synchronized int[] sendQoECmd(int cmd, int arg) {
        return nativeSendQoECmd(cmd, arg);
    }

    public synchronized int setDpiMarkRule(int uid, int protocol, int enable) {
        return nativeSetDpiMarkRule(uid, protocol, enable);
    }

    public synchronized HwQoEUdpNetWorkInfo readUdpNetworkStatsDetail(int uid, int network) {
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
        nativehandleSocketStrategy(strategy, uid);
        return nativeCloseProcessSockets(strategy, uid);
    }

    public int sendStallDetectCmd(int cmd, int uid, int appScene, int algoType, int userType) {
        return 0;
    }

    public long[] getCurrTotalTraffic() {
        return nativeGetCurrTotalTraffic();
    }

    public int startFrameDetect(int appScene) {
        return nativeStartFrameDetect(appScene);
    }

    public int stopFrameDetect() {
        return nativeStopFrameDetect();
    }

    public String wmpCoreTrainData(String[] localFingerData, String[] jsonParam) {
        return nativeWmpCoreTrainData(localFingerData, jsonParam);
    }

    public String getWmpCoreTrainData(String[] localFingerData, String[] jsonParam) {
        return nativeGetWmpCoreTrainData(localFingerData, jsonParam);
    }

    public int getCurFrameDetectResult() {
        return nativeGetCurFrameDetectResult();
    }

    public String wmpCoreClusterData(String[] localFingerData, String[] jsonParam) {
        return nativeWmpCoreClusterData(localFingerData, jsonParam);
    }

    public static void handleReportStallInfo(int stallTime, int appScene, int algo, boolean isVideoStart) {
        IHwHiStreamJniCallback iHwHiStreamJniCallback;
        HwHidataJniAdapter hwHidataJniAdapter = mHwHidataJniAdapter;
        if (hwHidataJniAdapter == null || (iHwHiStreamJniCallback = hwHidataJniAdapter.mHiStreamJniCallback) == null) {
            logD(false, "handleReportStallInfo: mHiStreamJniCallback is null", new Object[0]);
            return;
        }
        iHwHiStreamJniCallback.onStallInfoReportCallback(stallTime, appScene, algo, isVideoStart);
        logD(false, "detect stall, stallTime: %{public}d, appScene: %{public}d, algo: %{public}d,isVideoStart: %{public}s", Integer.valueOf(stallTime), Integer.valueOf(appScene), Integer.valueOf(algo), String.valueOf(isVideoStart));
    }

    public static void logD(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.d(TAG, isFmtStrPrivate, info, args);
    }
}
