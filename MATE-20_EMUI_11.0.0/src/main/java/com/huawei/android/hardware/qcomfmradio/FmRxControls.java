package com.huawei.android.hardware.qcomfmradio;

import android.util.Log;

/* access modifiers changed from: package-private */
public class FmRxControls {
    private static final int ENABLE_LOW_PASS_FILTER = 134217797;
    static final int FM_ANALOG_PATH = 1;
    static final int FM_DIGITAL_PATH = 0;
    static final int FREQ_MUL = 1000;
    static final int SCAN_BACKWARD = 3;
    static final int SCAN_FORWARD = 2;
    static final int SEEK_BACKWARD = 1;
    static final int SEEK_FORWARD = 0;
    private static final String TAG = "FmRxControls";
    private static final int V4L2_CID_AUDIO_MUTE = 9963785;
    private static final int V4L2_CID_BASE = 9963776;
    private static final int V4L2_CID_PRIVATE_AF_JUMP_RSSI_TH = 134217791;
    private static final int V4L2_CID_PRIVATE_AF_RMSSI_SAMPLES = 134217783;
    private static final int V4L2_CID_PRIVATE_AF_RMSSI_TH = 134217782;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_BLEND_RMSSIHI = 134217793;
    private static final int V4L2_CID_PRIVATE_BLEND_SINRHI = 134217792;
    private static final int V4L2_CID_PRIVATE_CF0TH12 = 134217786;
    private static final int V4L2_CID_PRIVATE_GOOD_CH_RMSSI_TH = 134217784;
    private static final int V4L2_CID_PRIVATE_RMSSIFIRSTSTAGE = 134217788;
    private static final int V4L2_CID_PRIVATE_RSSI_TH = 134217790;
    private static final int V4L2_CID_PRIVATE_RXREPEATCOUNT = 134217789;
    private static final int V4L2_CID_PRIVATE_SINR = 134217772;
    private static final int V4L2_CID_PRIVATE_SINRFIRSTSTAGE = 134217787;
    private static final int V4L2_CID_PRIVATE_SPUR_FREQ = 134217777;
    private static final int V4L2_CID_PRIVATE_SPUR_FREQ_RMSSI = 134217778;
    private static final int V4L2_CID_PRIVATE_SPUR_SELECTION = 134217779;
    private static final int V4L2_CID_PRIVATE_SRCHALGOTYPE = 134217785;
    private static final int V4L2_CID_PRIVATE_TAVARUA_AF_JUMP = 134217755;
    private static final int V4L2_CID_PRIVATE_TAVARUA_EMPHASIS = 134217740;
    private static final int V4L2_CID_PRIVATE_TAVARUA_HLSI = 134217757;
    private static final int V4L2_CID_PRIVATE_TAVARUA_INTDET = 134217753;
    private static final int V4L2_CID_PRIVATE_TAVARUA_IOVERC = 134217752;
    private static final int V4L2_CID_PRIVATE_TAVARUA_LP_MODE = 134217745;
    private static final int V4L2_CID_PRIVATE_TAVARUA_MPX_DCC = 134217754;
    private static final int V4L2_CID_PRIVATE_TAVARUA_OFF_CHANNEL_THRESHOLD = 134217774;
    private static final int V4L2_CID_PRIVATE_TAVARUA_ON_CHANNEL_THRESHOLD = 134217773;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSD_BUF = 134217747;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK = 134217734;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC = 134217744;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSON = 134217743;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDS_STD = 134217741;
    private static final int V4L2_CID_PRIVATE_TAVARUA_REGION = 134217735;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RSSI_DELTA = 134217756;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SCANDWELL = 134217730;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SET_AUDIO_PATH = 134217769;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH = 134217736;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SINR_SAMPLES = 134217776;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SINR_THRESHOLD = 134217775;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SPACING = 134217742;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SRCHMODE = 134217729;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SRCHON = 134217731;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SRCH_CNT = 134217739;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SRCH_PI = 134217738;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SRCH_PTY = 134217737;
    private static final int V4L2_CID_PRIVATE_TAVARUA_STATE = 134217732;
    private static final int V4L2_CID_PRIVATE_TAVARUA_TRANSMIT_MODE = 134217733;
    private static final int V4L2_CTRL_CLASS_USER = 9961472;
    private int mFreq;
    private int mPrgmId;
    private int mPrgmType;
    private int mScanTime;
    private int mSrchDir;
    private int mSrchListMode;
    private int mSrchMode;
    private boolean mStateMute;
    private boolean mStateStereo;
    private int sOffData;
    private int sOnData;

    FmRxControls() {
    }

    public int fmOn(int fd, int device) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_STATE, device);
        if (re < 0) {
            Log.d(TAG, "setControlNative faile134217732");
            return re;
        }
        setAudioPath(fd, false);
        int re2 = FmReceiverJNI.setCalibrationNative(fd);
        if (re2 != 0) {
            Log.d(TAG, "Calibration failed");
        }
        return re2;
    }

    public void fmOff(int fd) {
        FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_STATE, 0);
    }

    public void muteControl(int fd, boolean on) {
        if (on) {
            FmReceiverJNI.setControlNative(fd, V4L2_CID_AUDIO_MUTE, 3);
        } else {
            FmReceiverJNI.setControlNative(fd, V4L2_CID_AUDIO_MUTE, 0);
        }
    }

    public int IovercControl(int fd) {
        int ioverc = FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_IOVERC);
        Log.d(TAG, "IOVERC value is : " + ioverc);
        return ioverc;
    }

    public int IntDet(int fd) {
        int intdet = FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_INTDET);
        Log.d(TAG, "IOVERC value is : " + intdet);
        return intdet;
    }

    public int Mpx_Dcc(int fd) {
        int mpx_dcc = FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_MPX_DCC);
        Log.d(TAG, "MPX_DCC value is : " + mpx_dcc);
        return mpx_dcc;
    }

    public int setHiLoInj(int fd, int inj) {
        return FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_HLSI, inj);
    }

    public int setOnChannelThreshold(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_ON_CHANNEL_THRESHOLD, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to set On channel threshold data");
        }
        return re;
    }

    public int enableLPF(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, ENABLE_LOW_PASS_FILTER, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to enable LPF");
        }
        return re;
    }

    public int setOffChannelThreshold(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_OFF_CHANNEL_THRESHOLD, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to set Off channel Threshold data");
        }
        return re;
    }

    public int getOnChannelThreshold(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_ON_CHANNEL_THRESHOLD);
    }

    public int getOffChannelThreshold(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_OFF_CHANNEL_THRESHOLD);
    }

    public int setSINRThreshold(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SINR_THRESHOLD, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to set SINR threshold data");
        }
        return re;
    }

    public int setRssiThreshold(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_RSSI_TH, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to set RSSI threshold data");
        }
        return re;
    }

    public int getRssiThreshold(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_RSSI_TH);
    }

    public int setAfJumpRssiThreshold(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_AF_JUMP_RSSI_TH, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to set AF Jump Rssithreshold data");
        }
        return re;
    }

    public int getAfJumpRssiThreshold(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_AF_JUMP_RSSI_TH);
    }

    public int setRdsFifoCnt(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RDSD_BUF, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to set RDS fifo count data");
        }
        return re;
    }

    public int getRdsFifoCnt(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RDSD_BUF);
    }

    public int setSINRsamples(int fd, int sBuff) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SINR_SAMPLES, sBuff);
        if (re < 0) {
            Log.e(TAG, "Failed to set SINR samples ");
        }
        return re;
    }

    public int getSINRThreshold(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SINR_THRESHOLD);
    }

    public int getSINRsamples(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SINR_SAMPLES);
    }

    public int getRmssiDelta(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RSSI_DELTA);
    }

    public int setRmssiDel(int fd, int delta) {
        return FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RSSI_DELTA, delta);
    }

    public int setAudioPath(int fd, boolean value) {
        int mode;
        if (value) {
            mode = 1;
        } else {
            mode = 0;
        }
        return FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SET_AUDIO_PATH, mode);
    }

    public int setStation(int fd) {
        Log.d(TAG, "** Tune Using: " + fd);
        int ret = FmReceiverJNI.setFreqNative(fd, this.mFreq);
        Log.d(TAG, "** Returned: " + ret);
        return ret;
    }

    public int getTunedFrequency(int fd) {
        int frequency = FmReceiverJNI.getFreqNative(fd);
        Log.d(TAG, "getTunedFrequency: " + frequency);
        return frequency;
    }

    public int getFreq() {
        return this.mFreq;
    }

    public void setFreq(int f) {
        this.mFreq = f;
    }

    public int getSINR(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_SINR);
    }

    public int searchStationList(int fd, int mode, int preset_num, int dir, int pty) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCHMODE, mode);
        if (re != 0) {
            return re;
        }
        int re2 = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_CNT, preset_num);
        if (re2 != 0) {
            return re2;
        }
        if (pty > 0) {
            re2 = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_PTY, pty);
        }
        if (re2 != 0) {
            return re2;
        }
        int re3 = FmReceiverJNI.startSearchNative(fd, dir);
        if (re3 != 0) {
            return re3;
        }
        return 0;
    }

    public int[] stationList(int fd) {
        byte b = 0;
        byte[] sList = new byte[100];
        float lowBand = (float) (((double) FmReceiverJNI.getLowerBandNative(fd)) / 1000.0d);
        float highBand = (float) (((double) FmReceiverJNI.getUpperBandNative(fd)) / 1000.0d);
        Log.d(TAG, "lowBand: " + lowBand);
        Log.d(TAG, "highBand: " + highBand);
        FmReceiverJNI.getBufferNative(fd, sList, 0);
        if (sList[0] > 0) {
            b = sList[0];
        }
        int[] stationList = new int[(b + 1)];
        Log.d(TAG, "station_num: " + ((int) b));
        int j = 0;
        for (int i = 0; i < b; i++) {
            Log.d(TAG, " Byte1 = " + ((int) sList[(i * 2) + 1]));
            Log.d(TAG, " Byte2 = " + ((int) sList[(i * 2) + 2]));
            int tmpFreqByte1 = sList[(i * 2) + 1] & 255;
            int tmpFreqByte2 = sList[(i * 2) + 2] & 255;
            Log.d(TAG, " tmpFreqByte1 = " + tmpFreqByte1);
            Log.d(TAG, " tmpFreqByte2 = " + tmpFreqByte2);
            int freq = ((tmpFreqByte1 & 3) << 8) | tmpFreqByte2;
            Log.d(TAG, " freq: " + freq);
            float real_freq = ((float) (freq * 50)) + (lowBand * 1000.0f);
            Log.d(TAG, " real_freq: " + real_freq);
            if (real_freq < lowBand * 1000.0f || real_freq > 1000.0f * highBand) {
                Log.e(TAG, "Frequency out of band limits");
            } else {
                stationList[j] = (int) real_freq;
                Log.d(TAG, " stationList: " + stationList[j]);
                j++;
            }
        }
        try {
            stationList[b] = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "ArrayIndexOutOfBoundsException !!");
        }
        return stationList;
    }

    public int searchStations(int fd, int mode, int dwell, int dir, int pty, int pi) {
        int re;
        int re2;
        Log.d(TAG, "Mode is " + mode + " Dwell is " + dwell);
        Log.d(TAG, "dir is " + dir + " PTY is " + pty);
        Log.d(TAG, "pi is " + pi + " id " + V4L2_CID_PRIVATE_TAVARUA_SRCHMODE);
        int re3 = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCHMODE, mode);
        if (re3 != 0) {
            Log.e(TAG, "setting of search mode failed");
            return re3;
        }
        int re4 = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SCANDWELL, dwell);
        if (re4 != 0) {
            Log.e(TAG, "setting of scan dwell time failed");
            return re4;
        } else if (pty != 0 && (re2 = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_PTY, pty)) != 0) {
            Log.e(TAG, "setting of PTY failed");
            return re2;
        } else if (pi == 0 || (re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_PI, pi)) == 0) {
            return FmReceiverJNI.startSearchNative(fd, dir);
        } else {
            Log.e(TAG, "setting of PI failed");
            return re;
        }
    }

    public int stereoControl(int fd, boolean stereo) {
        if (stereo) {
            return FmReceiverJNI.setMonoStereoNative(fd, 1);
        }
        return FmReceiverJNI.setMonoStereoNative(fd, 0);
    }

    public void searchRdsStations(int mode, int dwelling, int direction, int RdsSrchPty, int RdsSrchPI) {
    }

    public void cancelSearch(int fd) {
        FmReceiverJNI.cancelSearchNative(fd);
    }

    public int setLowPwrMode(int fd, boolean lpmOn) {
        if (lpmOn) {
            return FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_LP_MODE, 1);
        }
        return FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_LP_MODE, 0);
    }

    public int getPwrMode(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_LP_MODE);
    }

    public int updateSpurTable(int fd, int freq, int rmssi, boolean enable) {
        int re;
        int re2 = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_SPUR_FREQ, freq);
        if (re2 < 0) {
            Log.e(TAG, "Failed to program the Spur frequency value");
            return re2;
        }
        int re3 = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_SPUR_FREQ_RMSSI, rmssi);
        if (re3 < 0) {
            Log.e(TAG, "Failed to program the RMSSI level of the Spur frequency");
            return re3;
        }
        if (enable) {
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_SPUR_SELECTION, 1);
        } else {
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_SPUR_SELECTION, 0);
        }
        if (re >= 0) {
            return re;
        }
        Log.e(TAG, "Failed to program Spur selection");
        return re;
    }

    public int configureSpurTable(int fd) {
        return FmReceiverJNI.configureSpurTable(fd);
    }

    public int getAFJumpRmssiTh(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_AF_RMSSI_TH);
    }

    public boolean setAFJumpRmssiTh(int fd, int th) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_AF_RMSSI_TH, th) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting AF jmp Rmssi Threshold");
        return false;
    }

    public int getAFJumpRmssiSamples(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_AF_RMSSI_SAMPLES);
    }

    public boolean setAFJumpRmssiSamples(int fd, int samples) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_AF_RMSSI_SAMPLES, samples) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting AF jmp Rmssi Samples");
        return false;
    }

    public int getGdChRmssiTh(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_GOOD_CH_RMSSI_TH);
    }

    public boolean setGdChRmssiTh(int fd, int th) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_GOOD_CH_RMSSI_TH, th) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting Good channel Rmssi Threshold");
        return false;
    }

    public int getSearchAlgoType(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_SRCHALGOTYPE);
    }

    public boolean setSearchAlgoType(int fd, int saerchType) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_SRCHALGOTYPE, saerchType) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting Search Algo type");
        return false;
    }

    public int getSinrFirstStage(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_SINRFIRSTSTAGE);
    }

    public boolean setSinrFirstStage(int fd, int sinr) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_SINRFIRSTSTAGE, sinr) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting Sinr First Stage Threshold");
        return false;
    }

    public int getRmssiFirstStage(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_RMSSIFIRSTSTAGE);
    }

    public boolean setRmssiFirstStage(int fd, int rmssi) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_RMSSIFIRSTSTAGE, rmssi) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting Rmssi First stage Threshold");
        return false;
    }

    public int getCFOMeanTh(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_CF0TH12);
    }

    public boolean setCFOMeanTh(int fd, int th) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_CF0TH12, th) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting Mean CFO Threshold");
        return false;
    }

    public boolean setPSRxRepeatCount(int fd, int count) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_RXREPEATCOUNT, count) < 0) {
            return false;
        }
        return true;
    }

    public boolean getPSRxRepeatCount(int fd) {
        if (FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_RXREPEATCOUNT) < 0) {
            return false;
        }
        return true;
    }

    public byte getBlendSinr(int fd) {
        return (byte) FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_BLEND_SINRHI);
    }

    public boolean setBlendSinr(int fd, int sinrHi) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_BLEND_SINRHI, sinrHi) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting sinrHi ");
        return false;
    }

    public byte getBlendRmssi(int fd) {
        return (byte) FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_BLEND_RMSSIHI);
    }

    public boolean setBlendRmssi(int fd, int rmssiHi) {
        if (FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_BLEND_RMSSIHI, rmssiHi) >= 0) {
            return true;
        }
        Log.e(TAG, "Error in setting RmssiHi ");
        return false;
    }

    public boolean enableSlimbus(int fd, int enable) {
        Log.d(TAG, "enableSlimbus : enable = " + enable);
        if (FmReceiverJNI.enableSlimbus(fd, enable) == 0) {
            return true;
        }
        return false;
    }
}
