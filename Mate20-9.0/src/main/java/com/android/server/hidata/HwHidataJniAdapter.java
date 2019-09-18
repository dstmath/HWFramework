package com.android.server.hidata;

import android.util.Log;
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

    public native void nativeSetIsStop(boolean z);

    public native int nativeStartFrameDetect(int i);

    public native int nativeStopFrameDetect();

    static {
        try {
            System.loadLibrary("hwqoe_jni");
            logD("loading JNI succ");
        } catch (UnsatisfiedLinkError e) {
            logD("LoadLibrary is error " + e.toString());
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
        return nativeSendQoeStallDetectCmd(cmd, uid, appScene, algoType, userType);
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

    public void wmpSetIsStop(boolean isStop) {
        nativeSetIsStop(isStop);
    }

    public static void handleReportStallInfo(int stallTime, int appScene, int algo, int isDouyinStart) {
        if (mHwHidataJniAdapter == null || mHwHidataJniAdapter.mHiStreamJniCallback == null) {
            logD("handleReportStallInfo: mHiStreamJniCallback is null");
            return;
        }
        mHwHidataJniAdapter.mHiStreamJniCallback.onStallInfoReportCallback(stallTime, appScene, algo, isDouyinStart);
        logD("detect stall");
    }

    public static void logD(String info) {
        Log.d(TAG, info);
    }
}
