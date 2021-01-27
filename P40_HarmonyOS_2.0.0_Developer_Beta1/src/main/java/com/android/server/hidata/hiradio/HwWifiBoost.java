package com.android.server.hidata.hiradio;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.SparseArray;
import com.android.server.hidata.HwHiDataJniAdapter;
import com.android.server.hidata.IHiDataCallback;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import huawei.android.net.hwmplink.HwHiDataCommonUtils;

public class HwWifiBoost {
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwWifiBoost.class.getSimpleName());
    private static HwWifiBoost sHwWifiBoost = null;
    private AppTypeRecoManager mAppTypeRecoManager;
    private SparseArray<Integer> mBgLimitModeRecords;
    private Context mContext;
    private boolean mGameBoosting = false;
    private IHiDataCallback mHiDataCallback;
    private HwHiDataJniAdapter mHwHiDataJniAdapter;
    private int mStreamingBoostingUid = 0;

    private HwWifiBoost(Context context) {
        this.mContext = context;
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
    }

    public static synchronized HwWifiBoost getInstance(Context context) {
        HwWifiBoost hwWifiBoost;
        synchronized (HwWifiBoost.class) {
            if (sHwWifiBoost == null) {
                sHwWifiBoost = new HwWifiBoost(context);
                sHwWifiBoost.initialBgLimitModeRecords();
                HwHiDataCommonUtils.logD(TAG, false, "init HwWifiBoost complete", new Object[0]);
            }
            hwWifiBoost = sHwWifiBoost;
        }
        return hwWifiBoost;
    }

    public synchronized void initialBgLimitModeRecords() {
        this.mBgLimitModeRecords = new SparseArray<>();
        this.mBgLimitModeRecords.append(1, 0);
        this.mBgLimitModeRecords.append(2, 0);
        this.mBgLimitModeRecords.append(3, 0);
        this.mBgLimitModeRecords.append(4, 0);
    }

    private synchronized int getBgLimitMaxMode() {
        if (this.mBgLimitModeRecords != null) {
            if (this.mBgLimitModeRecords.size() != 0) {
                int size = this.mBgLimitModeRecords.size();
                int max_mode = 0;
                for (int i = 0; i < size; i++) {
                    int key = this.mBgLimitModeRecords.keyAt(i);
                    if (this.mBgLimitModeRecords.get(key).intValue() > max_mode) {
                        HwHiDataCommonUtils.logD(TAG, false, "BG Limit id=%{public}d mode=%{public}d > max mode=%{public}d", new Object[]{Integer.valueOf(key), this.mBgLimitModeRecords.get(key), Integer.valueOf(max_mode)});
                        max_mode = this.mBgLimitModeRecords.get(key).intValue();
                    }
                }
                return max_mode;
            }
        }
        HwHiDataCommonUtils.logD(TAG, false, " mBGLimitModeRecords is null", new Object[0]);
        return -1;
    }

    private synchronized void dumpModeTable() {
        if (this.mBgLimitModeRecords != null) {
            if (this.mBgLimitModeRecords.size() != 0) {
                int size = this.mBgLimitModeRecords.size();
                for (int i = 0; i < size; i++) {
                    this.mBgLimitModeRecords.keyAt(i);
                }
                return;
            }
        }
        HwHiDataCommonUtils.logD(TAG, false, " mBGLimitModeRecords is null", new Object[0]);
    }

    public synchronized void limitedSpeed(int controlId, int enable, int mode) {
        int i = 0;
        HwHiDataCommonUtils.logD(TAG, false, "LimitedSpeed: %{public}d mode=%{public}d", new Object[]{Integer.valueOf(enable), Integer.valueOf(mode)});
        if (!HwArbitrationFunction.isInVpnMode(this.mContext) || enable != 1) {
            if (this.mBgLimitModeRecords != null) {
                SparseArray<Integer> sparseArray = this.mBgLimitModeRecords;
                if (enable != 0) {
                    i = mode;
                }
                sparseArray.put(controlId, Integer.valueOf(i));
            }
            dumpModeTable();
            int cmdMode = getBgLimitMaxMode();
            Bundle args = new Bundle();
            args.putInt("enbale", enable);
            args.putInt("mode", cmdMode);
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
        this.mHwHiDataJniAdapter = HwHiDataJniAdapter.getInstance();
        this.mHwHiDataJniAdapter.setDpiMarkRule(uid, type, enable);
    }

    public synchronized void setPmMode(int mode) {
        HwHiDataCommonUtils.logD(TAG, false, "setPmMode:  mode: %{public}d", new Object[]{Integer.valueOf(mode)});
        this.mHiDataCallback.onSetPmMode(mode);
    }

    public synchronized void setGameBoostMode(int enable, int uid, int type, int limitMode) {
        int i = 3;
        HwHiDataCommonUtils.logD(TAG, false, "setGameBoostMode:  enable: %{public}d uid: %{public}d type: %{public}d", new Object[]{Integer.valueOf(enable), Integer.valueOf(uid), Integer.valueOf(type)});
        this.mHiDataCallback.onSetTxPower(enable);
        IHiDataCallback iHiDataCallback = this.mHiDataCallback;
        if (enable == 1) {
            i = 4;
        }
        iHiDataCallback.onSetPmMode(i);
        highPriorityTransmit(uid, type, enable);
        limitedSpeed(1, enable, limitMode);
    }

    public synchronized void setStreamingBoostMode(int enable, int uid, int type) {
        HwHiDataCommonUtils.logD(TAG, false, "setStreamingBoostMode:  enable: %{public}d uid: %{public}d type: %{public}d", new Object[]{Integer.valueOf(enable), Integer.valueOf(uid), Integer.valueOf(type)});
        this.mHiDataCallback.onSetTxPower(enable);
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

    public synchronized void pauseAbsHandover() {
        this.mHiDataCallback.onPauseAbsHandover();
    }

    public synchronized void restartAbsHandover() {
        this.mHiDataCallback.onRestartAbsHandover();
    }

    public synchronized boolean isGameBoosting() {
        return this.mGameBoosting;
    }

    public synchronized void setGameBoosting(boolean isGameBoosting) {
        this.mGameBoosting = isGameBoosting;
    }

    public synchronized void stopLimitSpeed() {
        limitedSpeed(1, 0, 0);
        initialBgLimitModeRecords();
    }

    public synchronized void stopAllBoost() {
        highPriorityTransmit(-1, 17, 0);
        this.mHiDataCallback.onSetTxPower(0);
        this.mHiDataCallback.onSetPmMode(3);
        restartAbsHandover();
        stopLimitSpeed();
    }

    public synchronized void registerWifiBoostCallback(IHiDataCallback callback) {
        if (this.mHiDataCallback == null) {
            this.mHiDataCallback = callback;
        }
    }

    public synchronized void setHiLinkAccGameMode(boolean enable, int uid) {
        String packageName = this.mContext.getPackageManager().getNameForUid(uid);
        if (packageName == null || this.mAppTypeRecoManager.getAppType(packageName) == 9) {
            this.mHiDataCallback.OnSetHiLinkAccGameMode(enable, packageName);
        } else {
            HwHiDataCommonUtils.logD(TAG, false, "not game type:%{public}d", new Object[]{Integer.valueOf(this.mAppTypeRecoManager.getAppType(packageName))});
        }
    }
}
