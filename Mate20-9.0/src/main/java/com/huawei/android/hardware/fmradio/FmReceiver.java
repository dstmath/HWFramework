package com.huawei.android.hardware.fmradio;

public class FmReceiver {
    public static final int FM_CHSPACE_100_KHZ = 1;
    public static final int FM_CHSPACE_200_KHZ = 0;
    public static final int FM_CHSPACE_50_KHZ = 2;
    public static final int FM_DE_EMP50 = 1;
    public static final int FM_DE_EMP75 = 0;
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
    public static final int FM_RX_RDS_GRP_AF_EBL = 4;
    public static final int FM_RX_RDS_GRP_PS_EBL = 2;
    public static final int FM_RX_RDS_GRP_PS_SIMPLE_EBL = 16;
    public static final int FM_RX_RDS_GRP_RT_EBL = 1;
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
    public static int mSearchState = 0;
    private BaseFmReceiver mBaseFmReceiver;

    public FmReceiver() {
        this.mBaseFmReceiver = new BaseFmReceiver();
    }

    public FmReceiver(String device_path, FmRxEvCallbacksAdaptor callback) throws InstantiationException {
        this.mBaseFmReceiver = new BaseFmReceiver(device_path, FmRxEvCallbacksAdaptor.mBaseFmRxEvCallbacksAdaptor);
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
        return this.mBaseFmReceiver.getRTInfo();
    }

    public FmRxRdsData getPSInfo() {
        return this.mBaseFmReceiver.getPSInfo();
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
