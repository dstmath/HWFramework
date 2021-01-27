package com.huawei.android.hardware.qcomfmradio;

import android.util.Log;
import java.util.Arrays;

/* access modifiers changed from: package-private */
public class FmReceiverJNI {
    static final int FM_JNI_FAILURE = -1;
    static final int FM_JNI_SUCCESS = 0;
    private static final int STD_BUF_SIZE = 256;
    private static final String TAG = "FmReceiverJNI";
    private static byte[] mRdsBuffer = new byte[256];
    private final QcomFmRxEvCallbacksAdaptor mCallback;

    static {
        Log.d(TAG, "classinit native called");
        classInitNative();
    }

    static void classInitNative() {
        FmReceiverJniAdapter.classInitNativeAdapter();
    }

    static void initNative() {
        FmReceiverJniAdapter.initNativeAdapter();
    }

    static void cleanupNative() {
        FmReceiverJniAdapter.cleanupNativeAdapter();
    }

    public static byte[] getPsBuffer(byte[] buff) {
        Log.d(TAG, "getPsBuffer enter");
        byte[] bArr = mRdsBuffer;
        byte[] buff2 = Arrays.copyOf(bArr, bArr.length);
        Log.d(TAG, "getPsBuffer exit");
        return buff2;
    }

    public void AflistCallback(byte[] aflist) {
        Log.e(TAG, "AflistCallback enter ");
        if (aflist == null) {
            Log.e(TAG, "aflist null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(aflist, aflist.length);
        QcomFmReceiver.mCallback.FmRxEvRdsAfInfo();
        Log.e(TAG, "AflistCallback exit ");
    }

    public void getSigThCallback(int val, int status) {
        Log.d(TAG, "get Signal Threshold callback");
        QcomFmReceiver.mCallback.FmRxEvGetSignalThreshold(val, status);
    }

    public void getChDetThCallback(int val, int status) {
        QcomFmReceiver.mCallback.FmRxEvGetChDetThreshold(val, status);
    }

    public void setChDetThCallback(int status) {
        QcomFmReceiver.mCallback.FmRxEvSetChDetThreshold(status);
    }

    public void DefDataRdCallback(int val, int status) {
        QcomFmReceiver.mCallback.FmRxEvDefDataRead(val, status);
    }

    public void DefDataWrtCallback(int status) {
        QcomFmReceiver.mCallback.FmRxEvDefDataWrite(status);
    }

    public void getBlendCallback(int val, int status) {
        QcomFmReceiver.mCallback.FmRxEvGetBlend(val, status);
    }

    public void setBlendCallback(int status) {
        QcomFmReceiver.mCallback.FmRxEvSetBlend(status);
    }

    public void getStnParamCallback(int val, int status) {
        QcomFmReceiver.mCallback.FmRxGetStationParam(val, status);
    }

    public void getStnDbgParamCallback(int val, int status) {
        QcomFmReceiver.mCallback.FmRxGetStationDbgParam(val, status);
    }

    public void enableSlimbusCallback(int status) {
        Log.d(TAG, "++enableSlimbusCallback");
        QcomFmReceiver.mCallback.FmRxEvEnableSlimbus(status);
        Log.d(TAG, "--enableSlimbusCallback");
    }

    public void RtPlusCallback(byte[] rtplus) {
        Log.d(TAG, "RtPlusCallback enter ");
        if (rtplus == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(rtplus, rtplus.length);
        QcomFmReceiver.mCallback.FmRxEvRTPlus();
        Log.d(TAG, "RtPlusCallback exit ");
    }

    public void RtCallback(byte[] rt) {
        Log.d(TAG, "RtCallback enter ");
        if (rt == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(rt, rt.length);
        QcomFmReceiver.mCallback.FmRxEvRdsRtInfo();
        Log.d(TAG, "RtCallback exit ");
    }

    public void ErtCallback(byte[] ert) {
        Log.d(TAG, "ErtCallback enter ");
        if (ert == null) {
            Log.e(TAG, "ERT null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(ert, ert.length);
        QcomFmReceiver.mCallback.FmRxEvERTInfo();
        Log.d(TAG, "RtCallback exit ");
    }

    public void EccCallback(byte[] ecc) {
        Log.i(TAG, "EccCallback enter ");
        if (ecc == null) {
            Log.e(TAG, "ECC null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(ecc, ecc.length);
        QcomFmReceiver.mCallback.FmRxEvECCInfo();
        Log.i(TAG, "EccCallback exit ");
    }

    public void PsInfoCallback(byte[] psInfo) {
        Log.d(TAG, "PsInfoCallback enter ");
        if (psInfo == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        Log.e(TAG, "length =  " + psInfo.length);
        mRdsBuffer = Arrays.copyOf(psInfo, psInfo.length);
        QcomFmReceiver.mCallback.FmRxEvRdsPsInfo();
        Log.d(TAG, "PsInfoCallback exit");
    }

    public void enableCallback() {
        Log.d(TAG, "enableCallback enter");
        FmTransceiver.setFMPowerState(1);
        Log.v(TAG, "RxEvtList: CURRENT-STATE : FMRxStarting ---> NEW-STATE : FMRxOn");
        QcomFmReceiver.mCallback.FmRxEvEnableReceiver();
        Log.d(TAG, "enableCallback exit");
    }

    public void tuneCallback(int freq) {
        Log.d(TAG, "tuneCallback enter");
        int state = QcomFmReceiver.getSearchState();
        if (state != 0) {
            if (state != 4) {
                if (freq > 0) {
                    QcomFmReceiver.mCallback.FmRxEvRadioTuneStatus(freq);
                } else {
                    Log.e(TAG, "get frequency command failed");
                }
                Log.d(TAG, "tuneCallback exit");
            }
            Log.v(TAG, "Current state is SRCH_ABORTED");
            Log.v(TAG, "Aborting on-going search command...");
        }
        Log.v(TAG, "Current state is " + state);
        QcomFmReceiver.setSearchState(3);
        Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
        QcomFmReceiver.mCallback.FmRxEvSearchComplete(freq);
        Log.d(TAG, "tuneCallback exit");
    }

    public void seekCmplCallback(int freq) {
        Log.d(TAG, "seekCmplCallback enter");
        int state = QcomFmReceiver.getSearchState();
        if (state == 1) {
            Log.v(TAG, "Current state is " + state);
            QcomFmReceiver.setSearchState(3);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE :FMRxOn");
            QcomFmReceiver.mCallback.FmRxEvSearchComplete(freq);
        } else if (state == 4) {
            Log.v(TAG, "Current state is SRCH_ABORTED");
            Log.v(TAG, "Aborting on-going search command...");
            QcomFmReceiver.setSearchState(3);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
            QcomFmReceiver.mCallback.FmRxEvSearchComplete(freq);
        }
        Log.d(TAG, "seekCmplCallback exit");
    }

    public void srchListCallback(byte[] scan_tbl) {
        int state = QcomFmReceiver.getSearchState();
        if (state == 2) {
            Log.v(TAG, "FmRxEventListener: Current state is AUTO_PRESET_INPROGRESS");
            QcomFmReceiver.setSearchState(3);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
            QcomFmReceiver.mCallback.FmRxEvSearchListComplete();
        } else if (state == 4) {
            Log.v(TAG, "Current state is SRCH_ABORTED");
            Log.v(TAG, "Aborting on-going SearchList command...");
            QcomFmReceiver.setSearchState(3);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
            QcomFmReceiver.mCallback.FmRxEvSearchCancelled();
        }
    }

    public void scanNxtCallback() {
        Log.d(TAG, "scanNxtCallback enter");
        QcomFmReceiver.mCallback.FmRxEvSearchInProgress();
        Log.d(TAG, "scanNxtCallback exit");
    }

    public void stereostsCallback(boolean stereo) {
        Log.d(TAG, "stereostsCallback enter");
        QcomFmReceiver.mCallback.FmRxEvStereoStatus(stereo);
        Log.d(TAG, "stereostsCallback exit");
    }

    public void rdsAvlStsCallback(boolean rdsAvl) {
        Log.d(TAG, "rdsAvlStsCallback enter");
        QcomFmReceiver.mCallback.FmRxEvRdsLockStatus(rdsAvl);
        Log.d(TAG, "rdsAvlStsCallback exit");
    }

    public void disableCallback() {
        Log.d(TAG, "disableCallback enter");
        if (FmTransceiver.getFMPowerState() == 6) {
            FmTransceiver.setFMPowerState(0);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : FMTurningOff ---> NEW-STATE : FMOff");
            QcomFmReceiver.mCallback.FmRxEvDisableReceiver();
            return;
        }
        FmTransceiver.setFMPowerState(0);
        Log.d(TAG, "Unexpected RADIO_DISABLED recvd");
        Log.v(TAG, "RxEvtList: CURRENT-STATE : FMRxOn ---> NEW-STATE : FMOff");
        QcomFmReceiver.mCallback.FmRxEvRadioReset();
        Log.d(TAG, "disableCallback exit");
    }

    public FmReceiverJNI(QcomFmRxEvCallbacksAdaptor callback) {
        this.mCallback = callback;
        if (this.mCallback == null) {
            Log.e(TAG, "mCallback is null in JNI");
        }
        Log.d(TAG, "init native called");
        initNative();
    }

    static int acquireFdNative(String path) {
        return FmReceiverJniAdapter.acquireFdNativeAdapter(path);
    }

    static int audioControlNative(int fd, int control, int field) {
        return 0;
    }

    static int cancelSearchNative(int fd) {
        return FmReceiverJniAdapter.cancelSearchNativeAdapter(fd);
    }

    static int closeFdNative(int fd) {
        return FmReceiverJniAdapter.closeFdNativeAdapter(fd);
    }

    static int getFreqNative(int fd) {
        return FmReceiverJniAdapter.getFreqNativeAdapter(fd);
    }

    static int setFreqNative(int fd, int freq) {
        return FmReceiverJniAdapter.setFreqNativeAdapter(fd, freq);
    }

    static int getControlNative(int fd, int id) {
        return FmReceiverJniAdapter.getControlNativeAdapter(fd, id);
    }

    static int setControlNative(int fd, int id, int value) {
        return FmReceiverJniAdapter.setControlNativeAdapter(fd, id, value);
    }

    static int startSearchNative(int fd, int dir) {
        return FmReceiverJniAdapter.startSearchNativeAdapter(fd, dir);
    }

    static int getBufferNative(int fd, byte[] buff, int index) {
        return FmReceiverJniAdapter.getBufferNativeAdapter(fd, buff, index);
    }

    static int getRSSINative(int fd) {
        return FmReceiverJniAdapter.getRSSINativeAdapter(fd);
    }

    static int setBandNative(int fd, int low, int high) {
        return FmReceiverJniAdapter.setBandNativeAdapter(fd, low, high);
    }

    static int getLowerBandNative(int fd) {
        return FmReceiverJniAdapter.getLowerBandNativeAdapter(fd);
    }

    static int getUpperBandNative(int fd) {
        return FmReceiverJniAdapter.getUpperBandNativeAdapter(fd);
    }

    static int setMonoStereoNative(int fd, int val) {
        return FmReceiverJniAdapter.setMonoStereoNativeAdapter(fd, val);
    }

    static int getRawRdsNative(int fd, byte[] buff, int count) {
        return FmReceiverJniAdapter.getRawRdsNativeAdapter(fd, buff, count);
    }

    static int setNotchFilterNative(int fd, int id, boolean value) {
        return FmReceiverJniAdapter.setNotchFilterNativeAdapter(fd, id, value);
    }

    static void setNotchFilterNative(boolean value) {
    }

    static int getAudioQuiltyNative(int fd, int value) {
        return FmReceiverJniAdapter.getAudioQuiltyNativeAdapter(fd, value);
    }

    static int setFmSnrThreshNative(int fd, int value) {
        return FmReceiverJniAdapter.setFmSnrThreshNativeAdapter(fd, value);
    }

    static int setFmRssiThreshNative(int fd, int value) {
        return FmReceiverJniAdapter.setFmRssiThreshNativeAdapter(fd, value);
    }

    static int setAnalogModeNative(boolean value) {
        return FmReceiverJniAdapter.setAnalogModeNativeAdapter(value);
    }

    static int startRTNative(int fd, String str, int count) {
        return FmReceiverJniAdapter.startRTNativeAdapter(fd, str, count);
    }

    static int stopRTNative(int fd) {
        return FmReceiverJniAdapter.stopRTNativeAdapter(fd);
    }

    static int startPSNative(int fd, String str, int count) {
        return FmReceiverJniAdapter.startPSNativeAdapter(fd, str, count);
    }

    static int stopPSNative(int fd) {
        return FmReceiverJniAdapter.stopPSNativeAdapter(fd);
    }

    static int setPTYNative(int fd, int pty) {
        return FmReceiverJniAdapter.setPTYNativeAdapter(fd, pty);
    }

    static int setPINative(int fd, int pi) {
        return FmReceiverJniAdapter.setPINativeAdapter(fd, pi);
    }

    static int setPSRepeatCountNative(int fd, int repeatCount) {
        return FmReceiverJniAdapter.setPSRepeatCountNativeAdapter(fd, repeatCount);
    }

    static int setTxPowerLevelNative(int fd, int powLevel) {
        return FmReceiverJniAdapter.setTxPowerLevelNativeAdapter(fd, powLevel);
    }

    static int setCalibrationNative(int fd) {
        return FmReceiverJniAdapter.setCalibrationNativeAdapter(fd);
    }

    static int configureSpurTable(int fd) {
        return FmReceiverJniAdapter.configureSpurTableAdapter(fd);
    }

    static int setSpurDataNative(int fd, short[] buff, int len) {
        return FmReceiverJniAdapter.setSpurDataNativeAdapter(fd, buff, len);
    }

    static void configurePerformanceParams(int fd) {
        FmReceiverJniAdapter.configurePerformanceParamsAdapter(fd);
    }

    static int enableSlimbus(int fd, int val) {
        return FmReceiverJniAdapter.enableSlimbusAdapter(fd, val);
    }
}
