package com.android.server.hidata.hiradio;

import android.content.Context;
import android.net.wifi.RssiPacketCountInfo;
import android.os.Binder;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.SparseArray;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.IHidataCallback;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import huawei.android.net.hwmplink.HwHiDataCommonUtils;

public class HwWifiBoost {
    private static final String TAG = "HiData_HwWifiBoost";
    private static HwWifiBoost mHwWifiBoost = null;
    private SparseArray<Integer> mBGLimitModeRecords;
    private Context mContext;
    private boolean mGameBoosting = false;
    private IHidataCallback mHidataCallback;
    private HwHidataJniAdapter mHwHidataJniAdapter;
    private boolean mStreamingBoosting = false;

    private HwWifiBoost(Context context) {
        this.mContext = context;
    }

    public static synchronized HwWifiBoost getInstance(Context context) {
        HwWifiBoost hwWifiBoost;
        synchronized (HwWifiBoost.class) {
            if (mHwWifiBoost == null) {
                mHwWifiBoost = new HwWifiBoost(context);
            }
            hwWifiBoost = mHwWifiBoost;
        }
        return hwWifiBoost;
    }

    public synchronized void initialBGLimitModeRecords() {
        this.mBGLimitModeRecords = new SparseArray<>();
        this.mBGLimitModeRecords.append(1, 0);
        this.mBGLimitModeRecords.append(2, 0);
        this.mBGLimitModeRecords.append(3, 0);
        this.mBGLimitModeRecords.append(4, 0);
    }

    private synchronized int getBGLimitMaxMode() {
        if (this.mBGLimitModeRecords != null) {
            if (this.mBGLimitModeRecords.size() != 0) {
                int size = this.mBGLimitModeRecords.size();
                int max_mode = 0;
                for (int i = 0; i < size; i++) {
                    int key = this.mBGLimitModeRecords.keyAt(i);
                    if (this.mBGLimitModeRecords.get(key).intValue() > max_mode) {
                        HwHiDataCommonUtils.logD(TAG, "BG Limit id=" + key + " mode=" + this.mBGLimitModeRecords.get(key) + " > max mode=" + max_mode);
                        max_mode = this.mBGLimitModeRecords.get(key).intValue();
                    }
                }
                return max_mode;
            }
        }
        HwHiDataCommonUtils.logD(TAG, " mBGLimitModeRecords is null");
        return -1;
    }

    private synchronized void dumpModeTable() {
        if (this.mBGLimitModeRecords != null) {
            if (this.mBGLimitModeRecords.size() != 0) {
                int size = this.mBGLimitModeRecords.size();
                for (int i = 0; i < size; i++) {
                    int key = this.mBGLimitModeRecords.keyAt(i);
                }
                return;
            }
        }
        HwHiDataCommonUtils.logD(TAG, " mBGLimitModeRecords is null");
    }

    public synchronized void limitedSpeed(int controlId, int enable, int mode) {
        HwHiDataCommonUtils.logD(TAG, "LimitedSpeed: " + enable + " mode=" + mode);
        if (!HwArbitrationFunction.isInVPNMode(this.mContext) || enable != 1) {
            this.mBGLimitModeRecords.put(controlId, Integer.valueOf(enable == 0 ? 0 : mode));
            dumpModeTable();
            int cmd_mode = getBGLimitMaxMode();
            Bundle args = new Bundle();
            args.putInt("enbale", enable);
            args.putInt(ProcessStopShrinker.MODE_KEY, cmd_mode);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_NET_MANAGE), System.currentTimeMillis(), args);
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(id);
            return;
        }
        HwHiDataCommonUtils.logD(TAG, "Vpn Connected,can not limit speed!");
    }

    public synchronized void highPriorityTransmit(int uid, int type, int enable) {
        HwHiDataCommonUtils.logD(TAG, "highPriorityTransmit uid: " + uid + " enable: " + enable);
        this.mHwHidataJniAdapter = HwHidataJniAdapter.getInstance();
        this.mHwHidataJniAdapter.setDpiMarkRule(uid, type, enable);
    }

    public synchronized void setPMMode(int mode) {
        HwHiDataCommonUtils.logD(TAG, "setPMMode:  mode: " + mode);
        this.mHidataCallback.onSetPMMode(mode);
    }

    public synchronized void setGameBoostMode(int enable, int uid, int type, int limitMode) {
        HwHiDataCommonUtils.logD(TAG, "setGameBoostMode:  enable: " + enable + " uid: " + uid + " type: " + type);
        this.mHidataCallback.onSetTXPower(enable);
        this.mHidataCallback.onSetPMMode(enable == 1 ? 4 : 3);
        highPriorityTransmit(uid, type, enable);
        limitedSpeed(1, enable, limitMode);
    }

    public synchronized void setStreamingBoostMode(int enable, int uid, int type) {
        HwHiDataCommonUtils.logD(TAG, "setStreamingBoostMode:  enable: " + enable + " uid: " + uid + " type: " + type);
        this.mHidataCallback.onSetTXPower(enable);
        highPriorityTransmit(uid, type, enable);
    }

    public synchronized void startGameBoost(int uid) {
        if (!isGameBoosting()) {
            setGameBoostMode(1, uid, 17, 7);
            HwHiDataCommonUtils.logD(TAG, "start game boost:  mode: 7 uid: " + uid);
            this.mGameBoosting = true;
        }
    }

    public synchronized void stopGameBoost(int uid) {
        if (isGameBoosting()) {
            setGameBoostMode(0, uid, 17, 0);
            this.mGameBoosting = false;
        }
    }

    public synchronized void startStreamingBoost(int uid) {
        if (!isStreamingBoosting()) {
            setStreamingBoostMode(1, uid, 17);
            this.mStreamingBoosting = true;
        }
    }

    public synchronized void stopStreamingBoost(int uid) {
        if (isStreamingBoosting()) {
            setStreamingBoostMode(0, uid, 17);
            this.mStreamingBoosting = false;
        }
    }

    public synchronized void pauseABSHandover() {
        this.mHidataCallback.onPauseABSHandover();
    }

    public synchronized void restartABSHandover() {
        this.mHidataCallback.onRestartABSHandover();
    }

    public synchronized boolean isGameBoosting() {
        return this.mGameBoosting;
    }

    public synchronized boolean isStreamingBoosting() {
        return this.mStreamingBoosting;
    }

    public synchronized void stopLimitSpeed() {
        limitedSpeed(1, 0, 0);
        initialBGLimitModeRecords();
    }

    public synchronized void stopAllBoost() {
        highPriorityTransmit(-1, 17, 0);
        this.mHidataCallback.onSetTXPower(0);
        this.mHidataCallback.onSetPMMode(3);
        restartABSHandover();
        stopLimitSpeed();
    }

    public synchronized void registWifiBoostCallback(IHidataCallback callback) {
        if (this.mHidataCallback == null) {
            this.mHidataCallback = callback;
        }
    }

    public synchronized RssiPacketCountInfo getOTAInfo() {
        return this.mHidataCallback.onGetOTAInfo();
    }

    public synchronized void setHiLinkAccGameMode(boolean enable, int uid) {
        this.mHidataCallback.OnSetHiLinkAccGameMode(enable, this.mContext.getPackageManager().getNameForUid(uid));
    }
}
