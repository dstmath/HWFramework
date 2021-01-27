package com.huawei.android.hardware.fmradio;

import com.huawei.android.hardware.fmradio.common.BaseFmReceiver;
import com.huawei.android.hardware.fmradio.common.FmUtils;
import com.huawei.android.hardware.hisifmradio.HisiFmReceiver;
import com.huawei.android.hardware.mtkfmradio.MtkFmReceiver;
import com.huawei.android.hardware.qcomfmradio.QcomFmReceiver;
import com.huawei.android.util.NoExtAPIException;

public class FmReceiver {
    public static final int FM_CHSPACE_100_KHZ = platformFmChsSpace100Khz;
    public static final int FM_CHSPACE_200_KHZ = platformFmChsSpace200Khz;
    public static final int FM_CHSPACE_50_KHZ = platformFmChsSpace20Khz;
    public static final int FM_DE_EMP50 = platformFmDeEmp50;
    public static final int FM_DE_EMP75 = platformFmDeEmp75;
    public static final int FM_EU_BAND = 1;
    public static final int FM_JAPAN_STANDARD_BAND = 3;
    public static final int FM_JAPAN_WIDE_BAND = 2;
    public static final int FM_RDS_STD_NONE = 2;
    public static final int FM_RDS_STD_RBDS = 0;
    public static final int FM_RDS_STD_RDS = 1;
    public static final int FM_RX_AUDIO_MODE_MONO = 1;
    public static final int FM_RX_AUDIO_MODE_STEREO = 0;
    public static final int FM_RX_DWELL_PERIOD_0S = 0;
    public static final int FM_RX_DWELL_PERIOD_1S = 1;
    public static final int FM_RX_DWELL_PERIOD_2S = 2;
    public static final int FM_RX_DWELL_PERIOD_3S = 3;
    public static final int FM_RX_DWELL_PERIOD_4S = 4;
    public static final int FM_RX_DWELL_PERIOD_5S = 5;
    public static final int FM_RX_DWELL_PERIOD_6S = 6;
    public static final int FM_RX_DWELL_PERIOD_7S = 7;
    public static final int FM_RX_LOW_POWER_MODE = 1;
    public static final int FM_RX_MUTE = 1;
    public static final int FM_RX_NORMAL_POWER_MODE = 0;
    public static final int FM_RX_RDS_GRP_AF_EBL = platformFmRxRdsGrpAfEbl;
    public static final int FM_RX_RDS_GRP_PS_EBL = platformFmRxRdsGrpPsEbl;
    public static final int FM_RX_RDS_GRP_PS_SIMPLE_EBL = platformFmRxRdsGrpPsSimpleEbl;
    public static final int FM_RX_RDS_GRP_RT_EBL = platformFmRxRdsGrpTrEbl;
    public static final int FM_RX_SCREEN_OFF_MODE = 0;
    public static final int FM_RX_SCREEN_ON_MODE = 1;
    public static final int FM_RX_SEARCHDIR_DOWN = 0;
    public static final int FM_RX_SEARCHDIR_UP = 1;
    public static final int FM_RX_SIGNAL_STRENGTH_STRONG = 2;
    public static final int FM_RX_SIGNAL_STRENGTH_VERY_STRONG = 3;
    public static final int FM_RX_SIGNAL_STRENGTH_VERY_WEAK = 0;
    public static final int FM_RX_SIGNAL_STRENGTH_WEAK = 1;
    public static final int FM_RX_SRCHLIST_MAX_STATIONS = 12;
    public static final int FM_RX_SRCHLIST_MODE_STRONG = 2;
    public static final int FM_RX_SRCHLIST_MODE_STRONGEST = 8;
    public static final int FM_RX_SRCHLIST_MODE_WEAK = 3;
    public static final int FM_RX_SRCHLIST_MODE_WEAKEST = 9;
    public static final int FM_RX_SRCHRDS_MODE_SCAN_PTY = 5;
    public static final int FM_RX_SRCHRDS_MODE_SEEK_AF = 7;
    public static final int FM_RX_SRCHRDS_MODE_SEEK_PI = 6;
    public static final int FM_RX_SRCHRDS_MODE_SEEK_PTY = 4;
    public static final int FM_RX_SRCH_MODE_SCAN = 1;
    public static final int FM_RX_SRCH_MODE_SEEK = 0;
    public static final int FM_RX_UNMUTE = 0;
    public static final int FM_USER_DEFINED_BAND = 4;
    public static final int FM_US_BAND = 0;
    private static final String TAG = "FmReceiver";
    public static final int mSearchState = 0;
    private static int platformFmChsSpace100Khz;
    private static int platformFmChsSpace200Khz;
    private static int platformFmChsSpace20Khz;
    private static int platformFmDeEmp50;
    private static int platformFmDeEmp75;
    private static int platformFmRxRdsGrpAfEbl;
    private static int platformFmRxRdsGrpPsEbl;
    private static int platformFmRxRdsGrpPsSimpleEbl;
    private static int platformFmRxRdsGrpTrEbl;
    private BaseFmReceiver mBaseFmReceiver;

    static {
        if (FmUtils.isMtkPlatform()) {
            platformFmChsSpace200Khz = 2;
            platformFmChsSpace100Khz = 1;
            platformFmChsSpace20Khz = 0;
            platformFmDeEmp75 = 2;
            platformFmDeEmp50 = 1;
            platformFmRxRdsGrpTrEbl = 1;
            platformFmRxRdsGrpPsEbl = 2;
            platformFmRxRdsGrpAfEbl = 4;
            platformFmRxRdsGrpPsSimpleEbl = 16;
        } else if (FmUtils.isQcomPlatform()) {
            platformFmChsSpace200Khz = 0;
            platformFmChsSpace100Khz = 1;
            platformFmChsSpace20Khz = 2;
            platformFmDeEmp75 = 0;
            platformFmDeEmp50 = 1;
            platformFmRxRdsGrpTrEbl = 1;
            platformFmRxRdsGrpPsEbl = 2;
            platformFmRxRdsGrpAfEbl = 8;
            platformFmRxRdsGrpPsSimpleEbl = 4;
        } else {
            platformFmChsSpace200Khz = 0;
            platformFmChsSpace100Khz = 1;
            platformFmChsSpace20Khz = 2;
            platformFmDeEmp75 = 0;
            platformFmDeEmp50 = 1;
            platformFmRxRdsGrpTrEbl = 1;
            platformFmRxRdsGrpPsEbl = 2;
            platformFmRxRdsGrpAfEbl = 4;
            platformFmRxRdsGrpPsSimpleEbl = 16;
        }
    }

    public FmReceiver() {
        if (FmUtils.isMtkPlatform()) {
            this.mBaseFmReceiver = new MtkFmReceiver();
        } else if (FmUtils.isQcomPlatform()) {
            this.mBaseFmReceiver = new QcomFmReceiver();
        } else {
            this.mBaseFmReceiver = new HisiFmReceiver();
        }
    }

    public FmReceiver(String device_path, FmRxEvCallbacksAdaptor callback) throws InstantiationException {
        if (callback == null) {
            return;
        }
        if (FmUtils.isMtkPlatform()) {
            this.mBaseFmReceiver = new MtkFmReceiver(device_path, callback.getBaseFmRxEvCallbacks());
        } else if (FmUtils.isQcomPlatform()) {
            this.mBaseFmReceiver = new QcomFmReceiver(device_path, callback.getBaseFmRxEvCallbacks());
        } else {
            this.mBaseFmReceiver = new HisiFmReceiver(device_path, callback.getBaseFmRxEvCallbacks());
        }
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        return this.mBaseFmReceiver.registerClient(callback);
    }

    public boolean unregisterClient() {
        return this.mBaseFmReceiver.unregisterClient();
    }

    public boolean enable(FmConfig configSettings) {
        return this.mBaseFmReceiver.enable(FmConfig.mBaseFmConfig);
    }

    public boolean disable() {
        return this.mBaseFmReceiver.disable();
    }

    public boolean setStation(int frequencyKHz) {
        return this.mBaseFmReceiver.setStation(frequencyKHz);
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction) {
        return this.mBaseFmReceiver.searchStations(mode, dwellPeriod, direction);
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        return this.mBaseFmReceiver.searchStations(mode, dwellPeriod, direction, pty, pi);
    }

    public boolean cancelSearch() {
        return this.mBaseFmReceiver.cancelSearch();
    }

    public boolean setPowerMode(int powerMode) {
        return this.mBaseFmReceiver.setPowerMode(powerMode);
    }

    public FmRxRdsData getRTInfo() {
        throw new NoExtAPIException("method not support");
    }

    public FmRxRdsData getPSInfo() {
        throw new NoExtAPIException("method not support");
    }

    public int[] getAFInfo() {
        return this.mBaseFmReceiver.getAFInfo();
    }

    public int getRssi() {
        return this.mBaseFmReceiver.getRssi();
    }

    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        return this.mBaseFmReceiver.registerRdsGroupProcessing(fmGrpsToProc);
    }

    public boolean reset() {
        return this.mBaseFmReceiver.reset();
    }

    public boolean searchStationList(int mode, int direction, int maximumStations, int pty) {
        return this.mBaseFmReceiver.searchStationList(mode, direction, maximumStations, pty);
    }

    public boolean setMuteMode(int mode) {
        return this.mBaseFmReceiver.setMuteMode(mode);
    }

    public boolean setStereoMode(boolean stereoEnable) {
        return this.mBaseFmReceiver.setStereoMode(stereoEnable);
    }

    public boolean setSignalThreshold(int threshold) {
        return this.mBaseFmReceiver.setSignalThreshold(threshold);
    }

    public int getTunedFrequency() {
        return this.mBaseFmReceiver.getTunedFrequency();
    }

    public int getPowerMode() {
        return this.mBaseFmReceiver.getPowerMode();
    }

    public int[] getRssiLimit() {
        return this.mBaseFmReceiver.getRssiLimit();
    }

    public int getSignalThreshold() {
        return this.mBaseFmReceiver.getSignalThreshold();
    }

    public boolean setRdsGroupOptions(int enRdsGrpsMask, int rdsBuffSize, boolean enRdsChangeFilter) {
        return this.mBaseFmReceiver.setRdsGroupOptions(enRdsGrpsMask, rdsBuffSize, enRdsChangeFilter);
    }

    public boolean enableAFjump(boolean enable) {
        return this.mBaseFmReceiver.enableAFjump(enable);
    }

    public int[] getStationList() {
        return this.mBaseFmReceiver.getStationList();
    }

    public boolean getInternalAntenna() {
        return this.mBaseFmReceiver.getInternalAntenna();
    }

    public boolean setInternalAntenna(boolean intAnt) {
        return this.mBaseFmReceiver.setInternalAntenna(intAnt);
    }

    public byte[] getRawRDS(int numBlocks) {
        return this.mBaseFmReceiver.getRawRDS(numBlocks);
    }

    public int getFMState() {
        return this.mBaseFmReceiver.getFMState();
    }

    public int getAudioQuilty(int value) {
        return this.mBaseFmReceiver.getAudioQuilty(value);
    }

    public int setFmSnrThresh(int value) {
        return this.mBaseFmReceiver.setFmSnrThresh(value);
    }

    public int setFmRssiThresh(int value) {
        return this.mBaseFmReceiver.setFmRssiThresh(value);
    }

    public void setFmDeviceConnectionState(int state) {
        this.mBaseFmReceiver.setFmDeviceConnectionState(state);
    }
}
