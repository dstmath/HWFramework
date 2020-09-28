package com.huawei.android.hardware.hisifmradio;

import android.util.Log;
import com.huawei.android.hardware.fmradio.FmConfig;
import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.hardware.fmradio.common.BaseFmReceiver;
import com.huawei.android.util.SlogEx;
import java.nio.charset.Charset;

public class HisiFmReceiver extends FmTransceiver implements BaseFmReceiver {
    private static final int FM_HEADSET_MODE = 3;
    private static final int FM_HEADSET_VALUE = 1;
    public static final int FM_RX_RDS_GRP_AF_EBL = 4;
    public static final int FM_RX_RDS_GRP_PS_EBL = 2;
    public static final int FM_RX_RDS_GRP_PS_SIMPLE_EBL = 16;
    public static final int FM_RX_RDS_GRP_RT_EBL = 1;
    private static final int FM_RX_RSSI_LEVEL_STRONG = -96;
    private static final int FM_RX_RSSI_LEVEL_VERY_STRONG = -90;
    private static final int FM_RX_RSSI_LEVEL_VERY_WEAK = -105;
    private static final int FM_RX_RSSI_LEVEL_WEAK = -100;
    private static final int FM_SPEAKER_MODE = 2;
    private static final int FM_SPEAKER_VALUE = 0;
    private static final int RDS_COUNT = 1024;
    private static final int STD_BUF_SIZE = 128;
    private static final int SWITCH_DEVICE_MODE = 134217776;
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
    public static final int mSearchState = 0;
    private FmRxEvCallbacks mCallback;

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean configure(FmConfig fmConfig) {
        return super.configure(fmConfig);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean configure(BaseFmConfig baseFmConfig) {
        return super.configure(baseFmConfig);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean enable(FmConfig fmConfig, int i) {
        return super.enable(fmConfig, i);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean enable(BaseFmConfig baseFmConfig, int i) {
        return super.enable(baseFmConfig, i);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean registerTransmitClient(FmRxEvCallbacks fmRxEvCallbacks) {
        return super.registerTransmitClient(fmRxEvCallbacks);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver
    public /* bridge */ /* synthetic */ void setNotchFilter(boolean z) {
        super.setNotchFilter(z);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public /* bridge */ /* synthetic */ boolean setStation(int i) {
        return super.setStation(i);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean unregisterTransmitClient() {
        return super.unregisterTransmitClient();
    }

    public HisiFmReceiver() {
        this.mControl = new FmRxControls();
        this.mRdsData = new FmRxRdsData(sFd);
        this.mRxEvents = new FmRxEventListner();
    }

    public HisiFmReceiver(String devicePath, FmRxEvCallbacks callback) throws InstantiationException {
        this.mControl = new FmRxControls();
        this.mRxEvents = new FmRxEventListner();
        this.mCallback = callback;
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean registerClient(FmRxEvCallbacks callback) {
        return super.registerClient(callback);
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean unregisterClient() {
        return super.unregisterClient();
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean enable(BaseFmConfig configSettings) {
        boolean status = super.enable(configSettings, 1);
        SlogEx.e(TAG, "enable status = " + status);
        if (!status) {
            return false;
        }
        boolean status2 = registerClient(this.mCallback);
        this.mRdsData = new FmRxRdsData(sFd);
        return status2;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean reset() {
        if (getFMState() == 0) {
            return false;
        }
        setFMPowerState(0);
        boolean status = unregisterClient();
        release("/dev/radio0");
        return status;
    }

    @Override // com.huawei.android.hardware.hisifmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean disable() {
        unregisterClient();
        super.disable();
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean cancelSearch() {
        this.mControl.cancelSearch(sFd);
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setMuteMode(int mode) {
        if (mode == 0) {
            this.mControl.muteControl(sFd, false);
        } else if (mode == 1) {
            this.mControl.muteControl(sFd, true);
        }
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setStereoMode(boolean stereoEnable) {
        if (this.mControl.stereoControl(sFd, stereoEnable) == 0) {
            return true;
        }
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setSignalThreshold(int threshold) {
        int rssiLev;
        if (threshold == 0) {
            rssiLev = FM_RX_RSSI_LEVEL_VERY_WEAK;
        } else if (threshold == 1) {
            rssiLev = FM_RX_RSSI_LEVEL_WEAK;
        } else if (threshold == 2) {
            rssiLev = FM_RX_RSSI_LEVEL_STRONG;
        } else if (threshold != 3) {
            return false;
        } else {
            rssiLev = FM_RX_RSSI_LEVEL_VERY_STRONG;
        }
        if (FmReceiverWrapper.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH, rssiLev) != 0) {
            return false;
        }
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
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
            int endIndex = buff[0] + 5;
            if (rdsStr.length() >= endIndex && endIndex >= 5) {
                this.mRdsData.setRadioText(rdsStr.substring(5, endIndex));
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "StringIndexOutOfBoundsException ...");
        }
        return this.mRdsData;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setPowerMode(int powerMode) {
        int re;
        if (powerMode == 1) {
            re = this.mControl.setLowPwrMode(sFd, true);
        } else {
            re = this.mControl.setLowPwrMode(sFd, false);
        }
        return re == 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getPowerMode() {
        return this.mControl.getPwrMode(sFd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getRssiLimit() {
        return new int[]{0, 100};
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getSignalThreshold() {
        int signalStrength;
        int rmssiThreshold = FmReceiverWrapper.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH);
        if (FM_RX_RSSI_LEVEL_VERY_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_WEAK) {
            signalStrength = FM_RX_RSSI_LEVEL_WEAK;
        } else if (FM_RX_RSSI_LEVEL_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_STRONG) {
            signalStrength = FM_RX_RSSI_LEVEL_STRONG;
        } else if (FM_RX_RSSI_LEVEL_STRONG < rmssiThreshold) {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_STRONG;
        } else {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_WEAK;
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setRdsGroupOptions(int enRdsGrpsMask, int rdsBuffSize, boolean enRdsChangeFilter) {
        if (this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsGrpOptions(enRdsGrpsMask, rdsBuffSize, enRdsChangeFilter) == 0) {
            return true;
        }
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        if (this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsOptions(fmGrpsToProc) == 0) {
            return true;
        }
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean enableAFjump(boolean enable) {
        if (this.mRdsData.rdsOn(true) != 0) {
            return false;
        }
        this.mRdsData.enableAFjump(enable);
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getStationList() {
        return this.mControl.stationList(sFd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getRssi() {
        return FmReceiverWrapper.getRSSINative(sFd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean getInternalAntenna() {
        if (FmReceiverWrapper.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_ANTENNA) == 1) {
            return true;
        }
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public byte[] getRawRDS(int numBlocks) {
        if (numBlocks <= 0 || numBlocks > RDS_COUNT) {
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getFMState() {
        return getFMPowerState();
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getAudioQuilty(int value) {
        return FmReceiverWrapper.getAudioQuiltyNative(sFd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int setFmSnrThresh(int value) {
        return FmReceiverWrapper.setFmSnrThreshNative(sFd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int setFmRssiThresh(int value) {
        return FmReceiverWrapper.setFmRssiThreshNative(sFd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public void setFmDeviceConnectionState(int state) {
        if (state == 2) {
            FmReceiverWrapper.setControlNative(sFd, SWITCH_DEVICE_MODE, 0);
        } else if (state == 3) {
            FmReceiverWrapper.setControlNative(sFd, SWITCH_DEVICE_MODE, 1);
        } else {
            FmReceiverWrapper.setFmDeviceConnectionState(state);
        }
    }
}
