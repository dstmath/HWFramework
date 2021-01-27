package com.android.server.wifi.hwcoex;

import android.content.Context;
import android.os.Bundle;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;

public class HiCoexChrImpl {
    private static final int EID_NR_STATE_INFO = 909009120;
    private static final int EID_NR_STATISTIC_INFO = 909009121;
    private static final int MILLISECOND_UNIT = 1000;
    private static final int MIN_TRIGGER_INTERVAL = 1000;
    private static final String TAG = "HiCoexChrImpl";
    private static HiCoexChrImpl mHiCoexChrImpl;
    private int mApBand = -1;
    private int mApChannelCnt = 0;
    private int mApChannelOptimizationCnt = 0;
    private int mCellBw = 0;
    private int mCellFreq = 0;
    private Context mContext;
    private HwWifiCHRService mHwWifiCHRService;
    private int mNrPriority = 0;
    private int mNrState = 0;
    private int mNrTxBlockTime = 0;
    private int mNrTxTotalTime = 0;
    private int mP2pChannelCnt = 0;
    private int mP2pChannelOptimizationCnt = 0;
    private int mWifiAct = 0;
    private int mWifiPriorSceneId = 0;
    private int mWifiPriorTime = 0;
    private int mWifiPriorTotalTime = 0;

    private HiCoexChrImpl(Context context) {
        this.mContext = context;
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
    }

    public static void createHiCoexChrImpl(Context context) {
        if (mHiCoexChrImpl == null) {
            mHiCoexChrImpl = new HiCoexChrImpl(context);
        }
    }

    public static HiCoexChrImpl getInstance() {
        return mHiCoexChrImpl;
    }

    public void setNrState(int nrState) {
        this.mNrState = nrState;
    }

    public void setNrPriority(int nrPriority) {
        this.mNrPriority = nrPriority;
    }

    public void setCellFreq(int cellFreq) {
        this.mCellFreq = cellFreq;
    }

    public void setCellBw(int cellBw) {
        this.mCellBw = cellBw;
    }

    public void setWifiAct(int wifiAct) {
        this.mWifiAct = wifiAct;
    }

    public void setWifiPriorSceneId(int wifiPriorSceneId) {
        this.mWifiPriorSceneId = wifiPriorSceneId;
    }

    public void setApBand(int apBand) {
        this.mApBand = apBand;
    }

    public void updateCoexPriority(int time, int priority, int wifiScene, boolean isNrNetwork) {
        HiCoexUtils.logD(TAG, "updateCoexPriority:" + time + ",priority:" + priority + ",wifiScene:" + wifiScene + ",isNrNetwork:" + isNrNetwork);
        boolean needUpload = true;
        if (priority == 1) {
            this.mNrTxTotalTime += time;
            this.mWifiPriorTotalTime += time;
        } else if (priority == 0) {
            this.mWifiPriorTime += time;
            this.mWifiPriorTotalTime += time;
            this.mWifiPriorSceneId = wifiScene;
        } else if (priority != 2) {
            needUpload = false;
        } else if (isNrNetwork) {
            this.mWifiPriorTotalTime += time;
        } else {
            needUpload = false;
        }
        if (needUpload && time > 1000) {
            uploadHiCoexStatisticChr();
        }
    }

    public void updateApChannelOptimization(int apBand, boolean isOptimized) {
        int i = this.mApBand;
        if (i == apBand || i == -1) {
            HiCoexUtils.logD(TAG, "updateApChannelOptimization: band:" + apBand + ", isOptimized:" + isOptimized);
            this.mApChannelCnt = this.mApChannelCnt + 1;
            if (isOptimized) {
                this.mApChannelOptimizationCnt++;
            }
            uploadHiCoexStatisticChr();
        }
    }

    public void updateP2pChannelOptimization(boolean isOptimized) {
        this.mP2pChannelCnt++;
        if (isOptimized) {
            this.mP2pChannelOptimizationCnt++;
        }
        uploadHiCoexStatisticChr();
    }

    public void updateApChannelOptimizationCnt() {
        this.mApChannelOptimizationCnt++;
    }

    public void updateApChannelCnt() {
        this.mApChannelCnt++;
    }

    public void updateP2pChannelOptimizationCnt() {
        this.mP2pChannelOptimizationCnt++;
    }

    public void updateP2pChannelCnt() {
        this.mP2pChannelCnt++;
    }

    public void updateWifiPriorTime(int wifiPriorTime) {
        this.mWifiPriorTime += wifiPriorTime;
    }

    public void updateWifiPriorTotalTime(int nrTime) {
        this.mWifiPriorTotalTime += nrTime;
    }

    public void updateNrTxBlockTime(int blockTime) {
        this.mNrTxBlockTime += blockTime;
    }

    public void updateNrRxTotalTime(int txTotalTime) {
        this.mNrTxTotalTime += txTotalTime;
    }

    public void uploadHiCoexNrStateChr() {
        if (this.mHwWifiCHRService != null) {
            HiCoexUtils.logD(TAG, "upload HiCoex NR State CHR");
            this.mHwWifiCHRService.uploadDFTEvent((int) EID_NR_STATE_INFO, buildHiCoexNrStateChrBundle());
            resetNrStateParameters();
        }
    }

    public void uploadHiCoexStatisticChr() {
        if (this.mHwWifiCHRService != null) {
            HiCoexUtils.logD(TAG, "upload HiCoex Statistic CHR");
            this.mHwWifiCHRService.uploadDFTEvent((int) EID_NR_STATISTIC_INFO, buildHiCoexStatisticChrBundle());
            resetStatisticParameters();
        }
    }

    private void resetNrStateParameters() {
        this.mNrState = 0;
        this.mNrPriority = 0;
        this.mCellFreq = 0;
        this.mCellBw = 0;
        this.mWifiAct = 0;
    }

    private void resetStatisticParameters() {
        this.mApChannelOptimizationCnt = 0;
        this.mApChannelCnt = 0;
        this.mP2pChannelOptimizationCnt = 0;
        this.mP2pChannelCnt = 0;
        this.mWifiPriorTime = 0;
        this.mWifiPriorTotalTime = 0;
        this.mWifiPriorSceneId = 0;
        this.mNrTxBlockTime = 0;
        this.mNrTxTotalTime = 0;
    }

    private Bundle buildHiCoexNrStateChrBundle() {
        Bundle data = new Bundle();
        data.putInt("nrState", this.mNrState);
        data.putInt("nrPriority", this.mNrPriority);
        data.putInt("cellFreq", this.mCellFreq);
        data.putInt("cellBW", this.mCellBw);
        data.putInt("wifiAct", this.mWifiAct);
        return data;
    }

    private Bundle buildHiCoexStatisticChrBundle() {
        Bundle data = new Bundle();
        data.putInt("apChanOpCnt", this.mApChannelOptimizationCnt);
        data.putInt("apChanCnt", this.mApChannelCnt);
        data.putInt("p2pChanOpCnt", this.mP2pChannelOptimizationCnt);
        data.putInt("p2pChanCnt", this.mP2pChannelCnt);
        data.putInt("wifiProrTime", this.mWifiPriorTime / 1000);
        data.putInt("wifiProrTotalTime", this.mWifiPriorTotalTime / 1000);
        data.putInt("wifiProrSce1", this.mWifiPriorSceneId);
        data.putInt("nrTxBlockTime", this.mNrTxBlockTime / 1000);
        data.putInt("nrTxTotalTime", this.mNrTxTotalTime / 1000);
        return data;
    }
}
