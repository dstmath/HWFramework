package android.hardware.fmradio;

import android.os.Process;
import android.util.Log;

class FmRxControls {
    static final int FREQ_MUL = 1000;
    static final int SCAN_BACKWARD = 3;
    static final int SCAN_FORWARD = 2;
    static final int SEEK_BACKWARD = 1;
    static final int SEEK_FORWARD = 0;
    private static final String TAG = "FmRxControls";
    private static final int V4L2_CID_AUDIO_MUTE = 9963785;
    private static final int V4L2_CID_BASE = 9963776;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_EMPHASIS = 134217740;
    private static final int V4L2_CID_PRIVATE_TAVARUA_LP_MODE = 134217745;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK = 134217734;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC = 134217744;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSON = 134217743;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDS_STD = 134217741;
    private static final int V4L2_CID_PRIVATE_TAVARUA_REGION = 134217735;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SCANDWELL = 134217730;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH = 134217736;
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

    FmRxControls() {
    }

    public void fmOn(int fd, int device) {
        FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_STATE, device);
    }

    public void fmOff(int fd) {
        FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_STATE, SEEK_FORWARD);
    }

    public void muteControl(int fd, boolean on) {
        int err;
        if (on) {
            err = FmReceiverJNI.setControlNative(fd, V4L2_CID_AUDIO_MUTE, SCAN_BACKWARD);
        } else {
            err = FmReceiverJNI.setControlNative(fd, V4L2_CID_AUDIO_MUTE, SEEK_FORWARD);
        }
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

    public int searchStationList(int fd, int mode, int preset_num, int dir, int pty) {
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCHMODE, mode);
        if (re != 0) {
            return re;
        }
        re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_CNT, preset_num);
        if (re != 0) {
            return re;
        }
        if (pty > 0) {
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_PTY, pty);
        }
        if (re != 0) {
            return re;
        }
        re = FmReceiverJNI.startSearchNative(fd, dir);
        if (re != 0) {
            return re;
        }
        return SEEK_FORWARD;
    }

    public int[] stationList(int fd) {
        byte[] sList = new byte[100];
        float lowBand = (float) (((double) FmReceiverJNI.getLowerBandNative(fd)) / 1000.0d);
        Log.d(TAG, "lowBand: " + lowBand);
        FmReceiverJNI.getBufferNative(fd, sList, SEEK_FORWARD);
        int station_num = sList[SEEK_FORWARD];
        int[] stationList = new int[(station_num + SEEK_BACKWARD)];
        Log.d(TAG, "station_num: " + station_num);
        for (int i = SEEK_FORWARD; i < station_num; i += SEEK_BACKWARD) {
            Log.d(TAG, " Byte1 = " + sList[(i * SCAN_FORWARD) + SEEK_BACKWARD]);
            Log.d(TAG, " Byte2 = " + sList[(i * SCAN_FORWARD) + SCAN_FORWARD]);
            int tmpFreqByte1 = sList[(i * SCAN_FORWARD) + SEEK_BACKWARD] & Process.PROC_TERM_MASK;
            int tmpFreqByte2 = sList[(i * SCAN_FORWARD) + SCAN_FORWARD] & Process.PROC_TERM_MASK;
            Log.d(TAG, " tmpFreqByte1 = " + tmpFreqByte1);
            Log.d(TAG, " tmpFreqByte2 = " + tmpFreqByte2);
            int freq = ((tmpFreqByte1 & SCAN_BACKWARD) << 8) | tmpFreqByte2;
            Log.d(TAG, " freq: " + freq);
            float real_freq = ((float) (freq * 50)) + (1000.0f * lowBand);
            Log.d(TAG, " real_freq: " + real_freq);
            stationList[i] = (int) real_freq;
            Log.d(TAG, " stationList: " + stationList[i]);
        }
        try {
            stationList[station_num] = SEEK_FORWARD;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "ArrayIndexOutOfBoundsException !!");
        }
        return stationList;
    }

    public void searchStations(int fd, int mode, int dwell, int dir, int pty, int pi) {
        Log.d(TAG, "Mode is " + mode + " Dwell is " + dwell);
        Log.d(TAG, "dir is " + dir + " PTY is " + pty);
        Log.d(TAG, "pi is " + pi + " id " + V4L2_CID_PRIVATE_TAVARUA_SRCHMODE);
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCHMODE, mode);
        re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SCANDWELL, dwell);
        if (pty != 0) {
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_PTY, pty);
        }
        if (pi != 0) {
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_PI, pi);
        }
        re = FmReceiverJNI.startSearchNative(fd, dir);
    }

    public int stereoControl(int fd, boolean stereo) {
        if (stereo) {
            return FmReceiverJNI.setMonoStereoNative(fd, SEEK_BACKWARD);
        }
        return FmReceiverJNI.setMonoStereoNative(fd, SEEK_FORWARD);
    }

    public void searchRdsStations(int mode, int dwelling, int direction, int RdsSrchPty, int RdsSrchPI) {
    }

    public void cancelSearch(int fd) {
        FmReceiverJNI.cancelSearchNative(fd);
    }

    public int setLowPwrMode(int fd, boolean lpmOn) {
        if (lpmOn) {
            return FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_LP_MODE, SEEK_BACKWARD);
        }
        return FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_LP_MODE, SEEK_FORWARD);
    }

    public int getPwrMode(int fd) {
        return FmReceiverJNI.getControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_LP_MODE);
    }
}
