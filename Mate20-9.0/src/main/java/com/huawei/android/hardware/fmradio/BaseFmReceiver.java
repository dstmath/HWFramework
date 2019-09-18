package com.huawei.android.hardware.fmradio;

import android.util.Log;
import java.nio.charset.Charset;

public class BaseFmReceiver extends FmTransceiver {
    public static final int FM_RX_RDS_GRP_AF_EBL = 4;
    public static final int FM_RX_RDS_GRP_PS_EBL = 2;
    public static final int FM_RX_RDS_GRP_PS_SIMPLE_EBL = 16;
    public static final int FM_RX_RDS_GRP_RT_EBL = 1;
    private static final int FM_RX_RSSI_LEVEL_STRONG = -96;
    private static final int FM_RX_RSSI_LEVEL_VERY_STRONG = -90;
    private static final int FM_RX_RSSI_LEVEL_VERY_WEAK = -105;
    private static final int FM_RX_RSSI_LEVEL_WEAK = -100;
    private static final int STD_BUF_SIZE = 128;
    private static final String TAG = "HISI-BaseFmReceiver";
    private static final int TAVARUA_BUF_AF_LIST = 5;
    private static final int TAVARUA_BUF_EVENTS = 1;
    private static final int TAVARUA_BUF_MAX = 6;
    private static final int TAVARUA_BUF_PS_RDS = 3;
    private static final int TAVARUA_BUF_RAW_RDS = 4;
    private static final int TAVARUA_BUF_RT_RDS = 2;
    private static final int TAVARUA_BUF_SRCH_LIST = 0;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_ANTENNA = 134217746;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH = 134217736;
    public static int mSearchState = 0;
    private BaseFmRxEvCallbacksAdaptor mCallback;

    public BaseFmReceiver() {
        this.mControl = new FmRxControls();
        this.mRdsData = new FmRxRdsData(sFd);
        this.mRxEvents = new FmRxEventListner();
    }

    public BaseFmReceiver(String devicePath, BaseFmRxEvCallbacksAdaptor callback) throws InstantiationException {
        this.mControl = new FmRxControls();
        this.mRxEvents = new FmRxEventListner();
        this.mCallback = callback;
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        return super.registerClient(callback);
    }

    public boolean unregisterClient() {
        return super.unregisterClient();
    }

    public boolean enable(BaseFmConfig configSettings) {
        if (!super.enable(configSettings, 1)) {
            return false;
        }
        boolean status = registerClient(this.mCallback);
        this.mRdsData = new FmRxRdsData(sFd);
        return status;
    }

    public boolean reset() {
        if (getFMState() == 0) {
            return false;
        }
        setFMPowerState(0);
        boolean status = unregisterClient();
        release("/dev/radio0");
        return status;
    }

    public boolean disable() {
        unregisterClient();
        super.disable();
        return true;
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction) {
        boolean bStatus = true;
        if (!(mode == 0 || mode == 1)) {
            bStatus = false;
        }
        if (dwellPeriod < 1 || dwellPeriod > 7) {
            bStatus = false;
        }
        if (!(direction == 0 || direction == 1)) {
            bStatus = false;
        }
        if (bStatus) {
            this.mControl.searchStations(sFd, mode, dwellPeriod, direction, 0, 0);
        }
        return true;
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        boolean bStatus = true;
        if (!(mode == 4 || mode == 5 || mode == 6 || mode == 7)) {
            bStatus = false;
        }
        if (dwellPeriod < 1 || dwellPeriod > 7) {
            bStatus = false;
        }
        if (!(direction == 0 || direction == 1)) {
            bStatus = false;
        }
        if (bStatus) {
            this.mControl.searchStations(sFd, mode, dwellPeriod, direction, pty, pi);
        }
        return true;
    }

    public boolean searchStationList(int mode, int direction, int maximumStations, int pty) {
        boolean bStatus = true;
        int re = 0;
        if (!(mode == 2 || mode == 3 || mode == 8 || mode == 9)) {
            bStatus = false;
        }
        if (maximumStations < 0 || maximumStations > 12) {
            bStatus = false;
        }
        if (!(direction == 0 || direction == 1)) {
            bStatus = false;
        }
        if (bStatus) {
            if (mode == 8 || mode == 9) {
                re = this.mControl.searchStationList(sFd, mode, 0, direction, pty);
            } else {
                re = this.mControl.searchStationList(sFd, mode, maximumStations, direction, pty);
            }
        }
        if (re == 0) {
            return true;
        }
        return false;
    }

    public boolean cancelSearch() {
        this.mControl.cancelSearch(sFd);
        return true;
    }

    public boolean setMuteMode(int mode) {
        switch (mode) {
            case 0:
                this.mControl.muteControl(sFd, false);
                break;
            case 1:
                this.mControl.muteControl(sFd, true);
                break;
        }
        return true;
    }

    public boolean setStereoMode(boolean stereoEnable) {
        if (this.mControl.stereoControl(sFd, stereoEnable) == 0) {
            return true;
        }
        return false;
    }

    public boolean setSignalThreshold(int threshold) {
        int rssiLev;
        boolean bStatus = true;
        switch (threshold) {
            case 0:
                rssiLev = FM_RX_RSSI_LEVEL_VERY_WEAK;
                break;
            case 1:
                rssiLev = FM_RX_RSSI_LEVEL_WEAK;
                break;
            case 2:
                rssiLev = FM_RX_RSSI_LEVEL_STRONG;
                break;
            case 3:
                rssiLev = FM_RX_RSSI_LEVEL_VERY_STRONG;
                break;
            default:
                return false;
        }
        if (FmReceiverWrapper.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH, rssiLev) != 0) {
            bStatus = false;
        }
        return bStatus;
    }

    public int getTunedFrequency() {
        return FmReceiverWrapper.getFreqNative(sFd);
    }

    public FmRxRdsData getPSInfo() {
        byte[] buff = new byte[128];
        FmReceiverWrapper.getBufferNative(sFd, buff, 3);
        int piLower = buff[3] & 255;
        this.mRdsData.setPrgmId(((buff[2] & 255) << 8) | piLower);
        this.mRdsData.setPrgmType(buff[1] & 31);
        int numOfPs = buff[0] & 15;
        try {
            this.mRdsData.setPrgmServices(new String(buff, 5, numOfPs * 8, Charset.forName("UTF-8")));
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "Number of PS names " + numOfPs);
        }
        return this.mRdsData;
    }

    public FmRxRdsData getRTInfo() {
        byte[] buff = new byte[128];
        FmReceiverWrapper.getBufferNative(sFd, buff, 2);
        String rdsStr = new String(buff, Charset.forName("UTF-8"));
        int i = (buff[2] & 255) << 8;
        this.mRdsData.setPrgmId(i | (buff[3] & 255));
        this.mRdsData.setPrgmType(buff[1] & 31);
        try {
            this.mRdsData.setRadioText(rdsStr.substring(5, buff[0] + 5));
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "StringIndexOutOfBoundsException ...");
        }
        return this.mRdsData;
    }

    public int[] getAFInfo() {
        byte[] buff = new byte[128];
        int[] AfList = new int[40];
        FmReceiverWrapper.getBufferNative(sFd, buff, 5);
        if (buff[4] <= 0 || buff[4] > 25) {
            return null;
        }
        int lowerBand = FmReceiverWrapper.getLowerBandNative(sFd);
        for (int i = 0; i < buff[4]; i++) {
            AfList[i] = ((buff[i + 4] & 255) * 1000) + lowerBand;
        }
        return AfList;
    }

    public boolean setPowerMode(int powerMode) {
        int re;
        if (powerMode == 1) {
            re = this.mControl.setLowPwrMode(sFd, true);
        } else {
            re = this.mControl.setLowPwrMode(sFd, false);
        }
        return re == 0;
    }

    public int getPowerMode() {
        return this.mControl.getPwrMode(sFd);
    }

    public int[] getRssiLimit() {
        return new int[]{0, 100};
    }

    public int getSignalThreshold() {
        int signalStrength;
        int rmssiThreshold = FmReceiverWrapper.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH);
        if (FM_RX_RSSI_LEVEL_VERY_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_WEAK) {
            signalStrength = FM_RX_RSSI_LEVEL_WEAK;
        } else if (FM_RX_RSSI_LEVEL_WEAK >= rmssiThreshold || rmssiThreshold > FM_RX_RSSI_LEVEL_STRONG) {
            signalStrength = FM_RX_RSSI_LEVEL_STRONG < rmssiThreshold ? FM_RX_RSSI_LEVEL_VERY_STRONG : FM_RX_RSSI_LEVEL_VERY_WEAK;
        } else {
            signalStrength = FM_RX_RSSI_LEVEL_STRONG;
        }
        if (signalStrength == FM_RX_RSSI_LEVEL_VERY_WEAK) {
            return 0;
        }
        if (signalStrength == FM_RX_RSSI_LEVEL_WEAK) {
            return 1;
        }
        if (signalStrength == FM_RX_RSSI_LEVEL_STRONG) {
            return 2;
        }
        if (signalStrength != FM_RX_RSSI_LEVEL_VERY_STRONG) {
            return 0;
        }
        return 3;
    }

    public boolean setRdsGroupOptions(int enRdsGrpsMask, int rdsBuffSize, boolean enRdsChangeFilter) {
        if (this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsGrpOptions(enRdsGrpsMask, rdsBuffSize, enRdsChangeFilter) == 0) {
            return true;
        }
        return false;
    }

    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        if (this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsOptions(fmGrpsToProc) == 0) {
            return true;
        }
        return false;
    }

    public boolean enableAFjump(boolean enable) {
        if (this.mRdsData.rdsOn(true) != 0) {
            return false;
        }
        this.mRdsData.enableAFjump(enable);
        return true;
    }

    public int[] getStationList() {
        return this.mControl.stationList(sFd);
    }

    public int getRssi() {
        return FmReceiverWrapper.getRSSINative(sFd);
    }

    public boolean getInternalAntenna() {
        if (FmReceiverWrapper.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_ANTENNA) == 1) {
            return true;
        }
        return false;
    }

    public boolean setInternalAntenna(boolean intAnt) {
        int iAntenna;
        if (intAnt) {
            iAntenna = 1;
        } else {
            iAntenna = 0;
        }
        if (FmReceiverWrapper.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_ANTENNA, iAntenna) == 0) {
            return true;
        }
        return false;
    }

    public byte[] getRawRDS(int numBlocks) {
        if (numBlocks <= 0) {
            return null;
        }
        byte[] rawRds = new byte[(numBlocks * 3)];
        int re = FmReceiverWrapper.getRawRdsNative(sFd, rawRds, numBlocks * 3);
        if (re == numBlocks * 3) {
            return rawRds;
        }
        if (re <= 0) {
            return null;
        }
        byte[] buff = new byte[re];
        System.arraycopy(rawRds, 0, buff, 0, re);
        return buff;
    }

    public int getFMState() {
        return FmTransceiver.getFMPowerState();
    }

    public int getAudioQuilty(int value) {
        return FmReceiverWrapper.getAudioQuiltyNative(sFd, value);
    }

    public int setFmSnrThresh(int value) {
        return FmReceiverWrapper.setFmSnrThreshNative(sFd, value);
    }

    public int setFmRssiThresh(int value) {
        return FmReceiverWrapper.setFmRssiThreshNative(sFd, value);
    }

    public void setFmDeviceConnectionState(int state) {
        FmReceiverWrapper.setFmDeviceConnectionState(state);
    }
}
