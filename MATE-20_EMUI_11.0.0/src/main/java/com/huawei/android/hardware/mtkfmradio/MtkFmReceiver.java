package com.huawei.android.hardware.mtkfmradio;

import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.hardware.fmradio.common.BaseFmReceiver;

public class MtkFmReceiver implements BaseFmReceiver {
    public static final int FMRx_Starting = 4;
    public static final int FMTurning_Off = 6;
    public static final int FMTx_Starting = 5;
    public static final int FM_CHSPACE_100_KHZ = 1;
    public static final int FM_CHSPACE_200_KHZ = 2;
    public static final int FM_CHSPACE_50_KHZ = 0;
    public static final int FM_DE_EMP50 = 1;
    public static final int FM_DE_EMP75 = 2;
    public static final int FM_RX_RDS_GRP_AF_EBL = 4;
    public static final int FM_RX_RDS_GRP_PS_EBL = 2;
    public static final int FM_RX_RDS_GRP_PS_SIMPLE_EBL = 16;
    public static final int FM_RX_RDS_GRP_RT_EBL = 1;
    public static final int NoSearch = -1;
    public static final int RDS_EVENT_AF = 128;
    public static final int RDS_EVENT_AF_LIST = 256;
    public static final int RDS_EVENT_LAST_RADIOTEXT = 64;
    public static final int RDS_EVENT_PI_CODE = 2;
    public static final int RDS_EVENT_PROGRAMNAME = 8;
    public static final int RDS_EVENT_PTY_CODE = 4;
    public static final int Rx_Turned_On = 0;
    public static final int ScanInProg = 1;
    public static final int SeekInPrg = 0;
    public static final int SrchAbort = 4;
    public static final int SrchComplete = 3;
    public static final int SrchListInProg = 2;
    public static final int Srch_InProg = 1;
    private static final String TAG = "MTK-BaseFmReceiver";
    public static final int Turned_Off = 2;
    public FmRxEvCallbacks mCallback;
    public HwFmManager mHwFmManager = new HwFmManager(ActivityThreadEx.currentActivityThread().getApplication().getApplicationContext(), FmFgThread.getLooper());
    private final Object mLock = new Object();
    public FmRxRdsData mRdsData = new FmRxRdsData();

    public MtkFmReceiver() {
    }

    public MtkFmReceiver(String device_path, FmRxEvCallbacks callback) throws InstantiationException {
        this.mCallback = callback;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean registerClient(FmRxEvCallbacks callback) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean unregisterClient() {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean enable(BaseFmConfig configSettings) {
        boolean status;
        synchronized (this.mLock) {
            status = this.mHwFmManager.enable(configSettings);
            if (status) {
                this.mHwFmManager.registerListener((MtkFmRxEvCallbacksAdaptor) this.mCallback);
            }
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean disable() {
        boolean status;
        synchronized (this.mLock) {
            status = this.mHwFmManager.disable();
            if (status) {
                this.mHwFmManager.unregisterListener((MtkFmRxEvCallbacksAdaptor) this.mCallback);
            }
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean cancelSearch() {
        boolean status;
        synchronized (this.mLock) {
            status = this.mHwFmManager.cancelSearch();
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        boolean status;
        synchronized (this.mLock) {
            status = this.mHwFmManager.searchStations(mode, dwellPeriod, direction, pty, pi);
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean searchStations(int mode, int dwellPeriod, int direction) {
        boolean status;
        synchronized (this.mLock) {
            status = this.mHwFmManager.searchStations(mode, dwellPeriod, direction, 0, 0);
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setStation(int frequencyKHz) {
        boolean status;
        synchronized (this.mLock) {
            status = this.mHwFmManager.setStation(frequencyKHz);
        }
        return status;
    }

    public boolean setRdsOnOff(int onOff) {
        boolean re;
        synchronized (this.mLock) {
            re = this.mHwFmManager.setRdsOnOff(onOff);
        }
        return re;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setPowerMode(int powerMode) {
        boolean re;
        synchronized (this.mLock) {
            if (powerMode == 1) {
                re = this.mHwFmManager.setLowPwrMode(true);
            } else {
                re = this.mHwFmManager.setLowPwrMode(false);
            }
        }
        return re;
    }

    public FmRxRdsData getRdsDataInfo(int iRdsEvents) {
        FmRxRdsData fmRxRdsData;
        synchronized (this.mLock) {
            if (iRdsEvents != 0) {
                try {
                    Log.d(TAG, "is rds events: " + iRdsEvents);
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (2 == (iRdsEvents & 2)) {
                this.mRdsData.setPrgmId(this.mHwFmManager.getPrgmId());
            }
            if (4 == (iRdsEvents & 4)) {
                this.mRdsData.setPrgmType(this.mHwFmManager.getPrgmType());
            }
            fmRxRdsData = this.mRdsData;
        }
        return fmRxRdsData;
    }

    public FmRxRdsData getRTInfo() {
        FmRxRdsData fmRxRdsData;
        synchronized (this.mLock) {
            String radioText = this.mHwFmManager.getRadioText();
            if (radioText != null) {
                this.mRdsData.setRadioText(radioText);
            }
            fmRxRdsData = this.mRdsData;
        }
        return fmRxRdsData;
    }

    public FmRxRdsData getPSInfo() {
        FmRxRdsData fmRxRdsData;
        synchronized (this.mLock) {
            String prgServices = this.mHwFmManager.getPrgmServices();
            if (prgServices != null) {
                this.mRdsData.setPrgmServices(prgServices);
            }
            fmRxRdsData = this.mRdsData;
        }
        return fmRxRdsData;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getAFInfo() {
        int[] iArr = new int[50];
        int[] lAfList = {0};
        synchronized (this.mLock) {
            int[] af_list = this.mHwFmManager.getAfInfo();
            if (af_list != null) {
                int len = af_list[0];
                lAfList = new int[len];
                for (int i = 0; i < len; i++) {
                    lAfList[i] = af_list[i + 1];
                }
            }
        }
        return lAfList;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getRssi() {
        int rssi;
        synchronized (this.mLock) {
            rssi = this.mHwFmManager.getRssi();
        }
        return rssi;
    }

    public int getRdsStatus() {
        int rdsEvent;
        synchronized (this.mLock) {
            rdsEvent = this.mHwFmManager.getRdsStatus();
        }
        return rdsEvent;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        boolean re;
        synchronized (this.mLock) {
            re = this.mHwFmManager.registerRdsGroupProcessing(fmGrpsToProc);
        }
        return re;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean reset() {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean searchStationList(int mode, int direction, int maximumStations, int pty) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setMuteMode(int mode) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setStereoMode(boolean stereoEnable) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setSignalThreshold(int threshold) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getTunedFrequency() {
        return -1;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getPowerMode() {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getRssiLimit() {
        return null;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getSignalThreshold() {
        return -1;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setRdsGroupOptions(int enRdsGrpsMask, int rdsBuffSize, boolean enRdsChangeFilter) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean enableAFjump(boolean enable) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getStationList() {
        return null;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean getInternalAntenna() {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setInternalAntenna(boolean intAnt) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public byte[] getRawRDS(int numBlocks) {
        return null;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getFMState() {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getAudioQuilty(int value) {
        return -1;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int setFmSnrThresh(int value) {
        return -1;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int setFmRssiThresh(int value) {
        return -1;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public void setFmDeviceConnectionState(int state) {
    }
}
