package com.android.server.hidata.appqoe;

import android.common.HwFrameworkFactory;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;

public class HwAPKQoEQualityMonitorCell {
    public static final int DATA_SEND_TO_HIDATA_APP_QOE_CHR_PARAMS = 2;
    public static final int DATA_SEND_TO_HIDATA_APP_QOE_EXPERIENCE_LEVEL = 1;
    public static final int HIDATA_APP_QOE_EXPERIENCE_LEVEL_BAD = 1;
    public static final int HIDATA_APP_QOE_EXPERIENCE_LEVEL_GOOD = 2;
    public static final int HIDATA_APP_QOE_EXPERIENCE_LEVEL_UNKNOW = 3;
    public static final int HIDATA_GET_CELLULAR_APP_QOE_CHR_PARAMS = 305;
    public static final int HIDATA_START_CELLULAR_APP_QOE_MONITOR = 301;
    public static final int HIDATA_STOP_CELLULAR_APP_QOE_MONITOR = 302;
    /* access modifiers changed from: private */
    public static String TAG = "HiData_HwAPKQoEQualityMonitorCell";
    private static HwAPKQoEQualityMonitorCell mHwAPKQoEQualityMonitorCell = null;
    private IHwCommBoosterServiceManager bm = null;
    /* access modifiers changed from: private */
    public HwAPPStateInfo curAppStateInfo = new HwAPPStateInfo();
    /* access modifiers changed from: private */
    public HwAPPChrExcpInfo mAPPQoEInfo = new HwAPPChrExcpInfo();
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        public void callBack(int type, Bundle b) throws RemoteException {
            if (b != null) {
                switch (type) {
                    case 1:
                        int level = b.getInt("cellularAppQoeExpLevel");
                        String access$000 = HwAPKQoEQualityMonitorCell.TAG;
                        HwAPPQoEUtils.logD(access$000, "Call Back, level:" + level);
                        if (1 != level) {
                            if (2 == level) {
                                HwAPKQoEQualityMonitorCell.this.stmHandler.sendMessage(HwAPKQoEQualityMonitorCell.this.stmHandler.obtainMessage(106, HwAPKQoEQualityMonitorCell.this.curAppStateInfo));
                                break;
                            }
                        } else {
                            HwAPKQoEQualityMonitorCell.this.stmHandler.sendMessage(HwAPKQoEQualityMonitorCell.this.stmHandler.obtainMessage(107, HwAPKQoEQualityMonitorCell.this.curAppStateInfo));
                            break;
                        }
                        break;
                    case 2:
                        synchronized (HwAPKQoEQualityMonitorCell.this.mLock) {
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.netType = 801;
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.rtt = b.getInt("rtt", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.txPacket = b.getInt("txPacket", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.txByte = b.getInt("txByte", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.rxPacket = b.getInt("rxPacket", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.rxByte = b.getInt("rxByte", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.rsPacket = b.getInt("rtsPacket", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.para1 = b.getInt("para1", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.para2 = b.getInt("para2", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.para3 = b.getInt("para3", -1);
                            HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.para4 = b.getInt("para4", -1);
                            String access$0002 = HwAPKQoEQualityMonitorCell.TAG;
                            HwAPPQoEUtils.logD(access$0002, "DATA_SEND_TO_HIDATA_APP_QOE_CHR_PARAMS, mAPPQoEInfo:" + HwAPKQoEQualityMonitorCell.this.mAPPQoEInfo.toString());
                        }
                        HwAPKQoEQualityMonitorCell.this.stmHandler.sendEmptyMessage(204);
                        break;
                }
            }
        }
    };
    private HwAPPQoEUserLearning mLearningManager;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    /* access modifiers changed from: private */
    public Handler stmHandler = null;

    private HwAPKQoEQualityMonitorCell(Handler handler) {
        this.stmHandler = handler;
        initCommBoosterManager();
    }

    protected static HwAPKQoEQualityMonitorCell createQualityMonitorCell(Handler handler) {
        if (mHwAPKQoEQualityMonitorCell == null) {
            mHwAPKQoEQualityMonitorCell = new HwAPKQoEQualityMonitorCell(handler);
        }
        return mHwAPKQoEQualityMonitorCell;
    }

    public static HwAPKQoEQualityMonitorCell getInstance() {
        return mHwAPKQoEQualityMonitorCell;
    }

    public void startMonitor(HwAPPStateInfo newAPPStateInfo) {
        if (newAPPStateInfo == null) {
            HwAPPQoEUtils.logD(TAG, "startMonitor, input null");
            return;
        }
        String str = TAG;
        HwAPPQoEUtils.logD(str, "startAPPMonitor -- newAPPStateInfo:" + newAPPStateInfo.toString());
        if (newAPPStateInfo.mAppId != this.curAppStateInfo.mAppId) {
            sendScenceStartToCellMonitor(newAPPStateInfo);
        } else if (newAPPStateInfo.mAppPeriod != this.curAppStateInfo.mAppPeriod) {
            sendScenceEndToCellMonitor(this.curAppStateInfo);
            sendScenceStartToCellMonitor(newAPPStateInfo);
        }
        this.curAppStateInfo.copyObjectValue(newAPPStateInfo);
    }

    public void stopMonitor() {
        String str = TAG;
        HwAPPQoEUtils.logD(str, "stopAPPMonitor -- curAppStateInfo:" + this.curAppStateInfo.toString());
        sendScenceEndToCellMonitor(this.curAppStateInfo);
        this.curAppStateInfo = new HwAPPStateInfo();
    }

    public HwAPPChrExcpInfo getAPPQoEInfo() {
        HwAPPChrExcpInfo hwAPPChrExcpInfo;
        synchronized (this.mLock) {
            String str = TAG;
            HwAPPQoEUtils.logD(str, "curAPPQoEInfo:" + this.mAPPQoEInfo.toString());
            hwAPPChrExcpInfo = this.mAPPQoEInfo;
        }
        return hwAPPChrExcpInfo;
    }

    public void sendScenceStartToCellMonitor(HwAPPStateInfo newAPPStateInfo) {
        int appPeriod;
        HwAPPQoEUtils.logD(TAG, "sendScenceStartToCellMonitor enter");
        if (this.bm == null) {
            HwAPPQoEUtils.logD(TAG, "sendScenceStartToCellMonitor:null HwCommBoosterServiceManager");
        } else if (-1 != newAPPStateInfo.mAppPeriod) {
            if (this.mLearningManager == null) {
                appPeriod = newAPPStateInfo.mAppPeriod * 1000;
            } else if (this.mLearningManager.getUserTypeByAppId(newAPPStateInfo.mAppId) == 1) {
                HwAPPQoEUtils.logD(TAG, " sendScenceStartToCellMonitor is a COMMON user");
                appPeriod = newAPPStateInfo.mAppPeriod * 2 * 1000;
            } else {
                appPeriod = newAPPStateInfo.mAppPeriod * 1000;
                HwAPPQoEUtils.logD(TAG, " sendScenceStartToCellMonitor is a USER_TYPE_RADICAL user");
            }
            String str = TAG;
            HwAPPQoEUtils.logD(str, "sendScenceStartToCellMonitor:appPeriod is " + appPeriod);
            Bundle data = new Bundle();
            data.putInt("appUid", newAPPStateInfo.mAppUID);
            data.putInt("qoeInfoReportPeriod", appPeriod);
            int ret = this.bm.reportBoosterPara("com.android.server.hidata.appqoe", 301, data);
            if (ret != 0) {
                String str2 = TAG;
                HwAPPQoEUtils.logD(str2, "reportBoosterPara failed, ret=" + ret);
            }
        } else {
            HwAPPQoEUtils.logD(TAG, "sendScenceStartToCellMonitor:invalid appPeriod");
        }
    }

    public void sendScenceEndToCellMonitor(HwAPPStateInfo newAPPStateInfo) {
        HwAPPQoEUtils.logD(TAG, "sendScenceEndToCellMonitor enter");
        if (this.bm == null) {
            HwAPPQoEUtils.logD(TAG, "sendScenceEndToCellMonitor:null HwCommBoosterServiceManager");
            return;
        }
        Bundle data = new Bundle();
        data.putInt("appUid", newAPPStateInfo.mAppUID);
        data.putInt("qoeInfoReportPeriod", -1);
        int ret = this.bm.reportBoosterPara("com.android.server.hidata.appqoe", 302, data);
        if (ret != 0) {
            String str = TAG;
            HwAPPQoEUtils.logD(str, "reportBoosterPara failed, ret=" + ret);
        }
    }

    public void sendGetAppQoeChrMsg() {
        HwAPPQoEUtils.logD(TAG, "sendGetAppQoeChrMsg enter");
        if (this.bm == null) {
            HwAPPQoEUtils.logD(TAG, "sendGetAppQoeChrMsg:null HwCommBoosterServiceManager");
            return;
        }
        Bundle data = new Bundle();
        data.putInt("getChr", 1);
        int ret = this.bm.reportBoosterPara("com.android.server.hidata.appqoe", 305, data);
        if (ret != 0) {
            String str = TAG;
            HwAPPQoEUtils.logD(str, "reportBoosterPara failed, ret=" + ret);
        }
    }

    private void registerBoosterCallback() {
        HwAPPQoEUtils.logD(TAG, "registerBoosterCallback enter");
        if (this.bm != null) {
            int ret = this.bm.registerCallBack("com.android.server.hidata.appqoe", this.mIHwCommBoosterCallback);
            if (ret != 0) {
                String str = TAG;
                HwAPPQoEUtils.logD(str, "registerBoosterCallback:registerCallBack failed, ret=" + ret);
                return;
            }
            return;
        }
        HwAPPQoEUtils.logD(TAG, "registerBoosterCallback:null HwCommBoosterServiceManager");
    }

    private void initCommBoosterManager() {
        this.bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
        this.mLearningManager = HwAPPQoEUserLearning.getInstance();
        registerBoosterCallback();
    }
}
