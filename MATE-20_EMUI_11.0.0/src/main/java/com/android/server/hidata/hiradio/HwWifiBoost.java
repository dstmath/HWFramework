package com.android.server.hidata.hiradio;

import android.content.Context;
import android.net.wifi.RssiPacketCountInfo;
import android.os.Binder;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.SparseArray;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.IHidataCallback;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import huawei.android.net.hwmplink.HwHiDataCommonUtils;

public class HwWifiBoost {
    private static final String TAG = "HiData_HwWifiBoost";
    private static HwWifiBoost mHwWifiBoost = null;
    private AppTypeRecoManager mAppTypeRecoManager;
    private SparseArray<Integer> mBGLimitModeRecords;
    private Context mContext;
    private boolean mGameBoosting = false;
    private IHidataCallback mHidataCallback;
    private HwHidataJniAdapter mHwHidataJniAdapter;
    private int mStreamingBoostingUid = 0;

    private HwWifiBoost(Context context) {
        this.mContext = context;
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
    }

    public static synchronized HwWifiBoost getInstance(Context context) {
        HwWifiBoost hwWifiBoost;
        synchronized (HwWifiBoost.class) {
            if (mHwWifiBoost == null) {
                mHwWifiBoost = new HwWifiBoost(context);
                mHwWifiBoost.initialBGLimitModeRecords();
                HwHiDataCommonUtils.logD(TAG, false, "initialBGLimitModeRecords complete", new Object[0]);
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
                        HwHiDataCommonUtils.logD(TAG, false, "BG Limit id=%{public}d mode=%{public}d > max mode=%{public}d", new Object[]{Integer.valueOf(key), this.mBGLimitModeRecords.get(key), Integer.valueOf(max_mode)});
                        max_mode = this.mBGLimitModeRecords.get(key).intValue();
                    }
                }
                return max_mode;
            }
        }
        HwHiDataCommonUtils.logD(TAG, false, " mBGLimitModeRecords is null", new Object[0]);
        return -1;
    }

    private synchronized void dumpModeTable() {
        if (this.mBGLimitModeRecords != null) {
            if (this.mBGLimitModeRecords.size() != 0) {
                int size = this.mBGLimitModeRecords.size();
                for (int i = 0; i < size; i++) {
                    this.mBGLimitModeRecords.keyAt(i);
                }
                return;
            }
        }
        HwHiDataCommonUtils.logD(TAG, false, " mBGLimitModeRecords is null", new Object[0]);
    }

    public synchronized void limitedSpeed(int controlId, int enable, int mode) {
        int i = 0;
        HwHiDataCommonUtils.logD(TAG, false, "LimitedSpeed: %{public}d mode=%{public}d", new Object[]{Integer.valueOf(enable), Integer.valueOf(mode)});
        if (!HwArbitrationFunction.isInVPNMode(this.mContext) || enable != 1) {
            if (this.mBGLimitModeRecords != null) {
                SparseArray<Integer> sparseArray = this.mBGLimitModeRecords;
                if (enable != 0) {
                    i = mode;
                }
                sparseArray.put(controlId, Integer.valueOf(i));
            }
            dumpModeTable();
            int cmd_mode = getBGLimitMaxMode();
            Bundle args = new Bundle();
            args.putInt("enbale", enable);
            args.putInt("mode", cmd_mode);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_NET_MANAGE), System.currentTimeMillis(), args);
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(id);
            return;
        }
        HwHiDataCommonUtils.logD(TAG, false, "Vpn Connected,can not limit speed!", new Object[0]);
    }

    public synchronized void highPriorityTransmit(int uid, int type, int enable) {
        HwHiDataCommonUtils.logD(TAG, false, "highPriorityTransmit uid: %{public}d enable: %{public}d", new Object[]{Integer.valueOf(uid), Integer.valueOf(enable)});
        this.mHwHidataJniAdapter = HwHidataJniAdapter.getInstance();
        this.mHwHidataJniAdapter.setDpiMarkRule(uid, type, enable);
    }

    public synchronized void setPMMode(int mode) {
        HwHiDataCommonUtils.logD(TAG, false, "setPMMode:  mode: %{public}d", new Object[]{Integer.valueOf(mode)});
        this.mHidataCallback.onSetPMMode(mode);
    }

    public synchronized void setGameBoostMode(int enable, int uid, int type, int limitMode) {
        int i = 3;
        HwHiDataCommonUtils.logD(TAG, false, "setGameBoostMode:  enable: %{public}d uid: %{public}d type: %{public}d", new Object[]{Integer.valueOf(enable), Integer.valueOf(uid), Integer.valueOf(type)});
        this.mHidataCallback.onSetTXPower(enable);
        IHidataCallback iHidataCallback = this.mHidataCallback;
        if (enable == 1) {
            i = 4;
        }
        iHidataCallback.onSetPMMode(i);
        highPriorityTransmit(uid, type, enable);
        limitedSpeed(1, enable, limitMode);
    }

    public synchronized void setStreamingBoostMode(int enable, int uid, int type) {
        HwHiDataCommonUtils.logD(TAG, false, "setStreamingBoostMode:  enable: %{public}d uid: %{public}d type: %{public}d", new Object[]{Integer.valueOf(enable), Integer.valueOf(uid), Integer.valueOf(type)});
        this.mHidataCallback.onSetTXPower(enable);
        highPriorityTransmit(uid, type, enable);
    }

    public synchronized void startGameBoost(int uid) {
        if (!isGameBoosting()) {
            setGameBoostMode(1, uid, 17, 7);
            HwHiDataCommonUtils.logD(TAG, false, "start game boost:  mode: %{public}d uid: %{public}d", new Object[]{7, Integer.valueOf(uid)});
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
        setStreamingBoostMode(1, uid, 17);
        this.mStreamingBoostingUid = uid;
    }

    public synchronized void stopStreamingBoost(int uid) {
        if (uid == this.mStreamingBoostingUid) {
            setStreamingBoostMode(0, uid, 17);
            this.mStreamingBoostingUid = 0;
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

    public void setGameBoosting(boolean isGameBoosting) {
        this.mGameBoosting = isGameBoosting;
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
        return this.mHidataCallback.onGetOtaInfo();
    }

    public synchronized void setHiLinkAccGameMode(boolean enable, int uid) {
        String pacakgeName = this.mContext.getPackageManager().getNameForUid(uid);
        if (pacakgeName == null || this.mAppTypeRecoManager.getAppType(pacakgeName) == 9) {
            this.mHidataCallback.OnSetHiLinkAccGameMode(enable, pacakgeName);
        } else {
            HwHiDataCommonUtils.logD(TAG, false, "not game type:%{public}d", new Object[]{Integer.valueOf(this.mAppTypeRecoManager.getAppType(pacakgeName))});
        }
    }
}
