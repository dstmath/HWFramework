package android.hardware.fmradio;

import android.os.Process;
import android.util.Log;

public class FmReceiver extends FmTransceiver {
    public static final int FM_RX_AUDIO_MODE_MONO = 1;
    public static final int FM_RX_AUDIO_MODE_STEREO = 0;
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
    private static final int FM_RX_RSSI_LEVEL_STRONG = -96;
    private static final int FM_RX_RSSI_LEVEL_VERY_STRONG = -90;
    private static final int FM_RX_RSSI_LEVEL_VERY_WEAK = -105;
    private static final int FM_RX_RSSI_LEVEL_WEAK = -100;
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
    static final int STD_BUF_SIZE = 128;
    private static final String TAG = "FMRadio";
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
    public static int mSearchState;
    private FmRxEvCallbacksAdaptor mCallback;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.fmradio.FmReceiver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.fmradio.FmReceiver.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.fmradio.FmReceiver.<clinit>():void");
    }

    public FmReceiver() {
        this.mControl = new FmRxControls();
        this.mRdsData = new FmRxRdsData(sFd);
        this.mRxEvents = new FmRxEventListner();
    }

    public FmReceiver(String devicePath, FmRxEvCallbacksAdaptor callback) throws InstantiationException {
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

    public boolean enable(FmConfig configSettings) {
        if (!super.enable(configSettings, TAVARUA_BUF_EVENTS)) {
            return false;
        }
        boolean status = registerClient(this.mCallback);
        this.mRdsData = new FmRxRdsData(sFd);
        return status;
    }

    public boolean reset() {
        if (getFMState() == 0) {
            Log.d(TAG, "FM already turned Off.");
            return false;
        }
        FmTransceiver.setFMPowerState(TAVARUA_BUF_SRCH_LIST);
        Log.v(TAG, "reset: NEW-STATE : FMState_Turned_Off");
        boolean status = unregisterClient();
        FmTransceiver.release("/dev/radio0");
        return status;
    }

    public boolean disable() {
        if (unregisterClient()) {
            return super.disable();
        }
        return false;
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction) {
        boolean bStatus = true;
        Log.d(TAG, "Basic search...");
        if (!(mode == 0 || mode == TAVARUA_BUF_EVENTS)) {
            Log.d(TAG, "Invalid search mode: " + mode);
            bStatus = false;
        }
        if (dwellPeriod < TAVARUA_BUF_EVENTS || dwellPeriod > FM_RX_SRCHRDS_MODE_SEEK_AF) {
            Log.d(TAG, "Invalid dwelling time: " + dwellPeriod);
            bStatus = false;
        }
        if (!(direction == 0 || direction == TAVARUA_BUF_EVENTS)) {
            Log.d(TAG, "Invalid search direction: " + direction);
            bStatus = false;
        }
        if (bStatus) {
            Log.d(TAG, "searchStations: mode " + mode + "direction:  " + direction);
            this.mControl.searchStations(sFd, mode, dwellPeriod, direction, TAVARUA_BUF_SRCH_LIST, TAVARUA_BUF_SRCH_LIST);
        }
        return true;
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        boolean bStatus = true;
        Log.d(TAG, "RDS search...");
        if (!(mode == TAVARUA_BUF_RAW_RDS || mode == TAVARUA_BUF_AF_LIST || mode == TAVARUA_BUF_MAX || mode == FM_RX_SRCHRDS_MODE_SEEK_AF)) {
            Log.d(TAG, "Invalid search mode: " + mode);
            bStatus = false;
        }
        if (dwellPeriod < TAVARUA_BUF_EVENTS || dwellPeriod > FM_RX_SRCHRDS_MODE_SEEK_AF) {
            Log.d(TAG, "Invalid dwelling time: " + dwellPeriod);
            bStatus = false;
        }
        if (!(direction == 0 || direction == TAVARUA_BUF_EVENTS)) {
            Log.d(TAG, "Invalid search direction: " + direction);
            bStatus = false;
        }
        if (bStatus) {
            Log.d(TAG, "searchStations: mode " + mode);
            Log.d(TAG, "searchStations: dwellPeriod " + dwellPeriod);
            Log.d(TAG, "searchStations: direction " + direction);
            Log.d(TAG, "searchStations: pty " + pty);
            Log.d(TAG, "searchStations: pi " + pi);
            this.mControl.searchStations(sFd, mode, dwellPeriod, direction, pty, pi);
        }
        return true;
    }

    public boolean searchStationList(int mode, int direction, int maximumStations, int pty) {
        boolean bStatus = true;
        int re = TAVARUA_BUF_SRCH_LIST;
        Log.d(TAG, "searchStations: mode " + mode);
        Log.d(TAG, "searchStations: direction " + direction);
        Log.d(TAG, "searchStations: maximumStations " + maximumStations);
        Log.d(TAG, "searchStations: pty " + pty);
        if (!(mode == TAVARUA_BUF_RT_RDS || mode == TAVARUA_BUF_PS_RDS || mode == FM_RX_SRCHLIST_MODE_STRONGEST || mode == FM_RX_SRCHLIST_MODE_WEAKEST)) {
            bStatus = false;
        }
        if (maximumStations < 0 || maximumStations > FM_RX_SRCHLIST_MAX_STATIONS) {
            bStatus = false;
        }
        if (!(direction == 0 || direction == TAVARUA_BUF_EVENTS)) {
            bStatus = false;
        }
        if (bStatus) {
            if (mode == FM_RX_SRCHLIST_MODE_STRONGEST || mode == FM_RX_SRCHLIST_MODE_WEAKEST) {
                re = this.mControl.searchStationList(sFd, mode, TAVARUA_BUF_SRCH_LIST, direction, pty);
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
            case TAVARUA_BUF_SRCH_LIST /*0*/:
                this.mControl.muteControl(sFd, false);
                break;
            case TAVARUA_BUF_EVENTS /*1*/:
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
        boolean bStatus = true;
        Log.d(TAG, "Signal Threshhold input: " + threshold);
        int rssiLev;
        switch (threshold) {
            case TAVARUA_BUF_SRCH_LIST /*0*/:
                rssiLev = FM_RX_RSSI_LEVEL_VERY_WEAK;
                break;
            case TAVARUA_BUF_EVENTS /*1*/:
                rssiLev = FM_RX_RSSI_LEVEL_WEAK;
                break;
            case TAVARUA_BUF_RT_RDS /*2*/:
                rssiLev = FM_RX_RSSI_LEVEL_STRONG;
                break;
            case TAVARUA_BUF_PS_RDS /*3*/:
                rssiLev = FM_RX_RSSI_LEVEL_VERY_STRONG;
                break;
            default:
                Log.d(TAG, "Invalid threshold: " + threshold);
                return false;
        }
        if (!(TAVARUA_BUF_EVENTS == null || FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH, rssiLev) == 0)) {
            bStatus = false;
        }
        return bStatus;
    }

    public int getTunedFrequency() {
        int frequency = FmReceiverJNI.getFreqNative(sFd);
        Log.d(TAG, "getFrequency: " + frequency);
        return frequency;
    }

    public FmRxRdsData getPSInfo() {
        byte[] buff = new byte[STD_BUF_SIZE];
        FmReceiverJNI.getBufferNative(sFd, buff, TAVARUA_BUF_PS_RDS);
        this.mRdsData.setPrgmId(((buff[TAVARUA_BUF_RT_RDS] & Process.PROC_TERM_MASK) << FM_RX_SRCHLIST_MODE_STRONGEST) | (buff[TAVARUA_BUF_PS_RDS] & Process.PROC_TERM_MASK));
        this.mRdsData.setPrgmType(buff[TAVARUA_BUF_EVENTS] & 31);
        int numOfPs = buff[TAVARUA_BUF_SRCH_LIST] & 15;
        try {
            this.mRdsData.setPrgmServices(new String(buff, TAVARUA_BUF_AF_LIST, numOfPs * FM_RX_SRCHLIST_MODE_STRONGEST));
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "Number of PS names " + numOfPs);
        }
        return this.mRdsData;
    }

    public FmRxRdsData getRTInfo() {
        byte[] buff = new byte[STD_BUF_SIZE];
        FmReceiverJNI.getBufferNative(sFd, buff, TAVARUA_BUF_RT_RDS);
        String rdsStr = new String(buff);
        this.mRdsData.setPrgmId(((buff[TAVARUA_BUF_RT_RDS] & Process.PROC_TERM_MASK) << FM_RX_SRCHLIST_MODE_STRONGEST) | (buff[TAVARUA_BUF_PS_RDS] & Process.PROC_TERM_MASK));
        this.mRdsData.setPrgmType(buff[TAVARUA_BUF_EVENTS] & 31);
        try {
            this.mRdsData.setRadioText(rdsStr.substring(TAVARUA_BUF_AF_LIST, buff[TAVARUA_BUF_SRCH_LIST] + TAVARUA_BUF_AF_LIST));
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "StringIndexOutOfBoundsException ...");
        }
        return this.mRdsData;
    }

    public int[] getAFInfo() {
        byte[] buff = new byte[STD_BUF_SIZE];
        int[] AfList = new int[40];
        FmReceiverJNI.getBufferNative(sFd, buff, TAVARUA_BUF_AF_LIST);
        if (buff[TAVARUA_BUF_RAW_RDS] <= null || buff[TAVARUA_BUF_RAW_RDS] > 25) {
            return null;
        }
        int lowerBand = FmReceiverJNI.getLowerBandNative(sFd);
        Log.d(TAG, "Low band " + lowerBand);
        Log.d(TAG, "AF_buff 0: " + (buff[TAVARUA_BUF_SRCH_LIST] & Process.PROC_TERM_MASK));
        Log.d(TAG, "AF_buff 1: " + (buff[TAVARUA_BUF_EVENTS] & Process.PROC_TERM_MASK));
        Log.d(TAG, "AF_buff 2: " + (buff[TAVARUA_BUF_RT_RDS] & Process.PROC_TERM_MASK));
        Log.d(TAG, "AF_buff 3: " + (buff[TAVARUA_BUF_PS_RDS] & Process.PROC_TERM_MASK));
        Log.d(TAG, "AF_buff 4: " + (buff[TAVARUA_BUF_RAW_RDS] & Process.PROC_TERM_MASK));
        for (byte i = (byte) 0; i < buff[TAVARUA_BUF_RAW_RDS]; i += TAVARUA_BUF_EVENTS) {
            AfList[i] = ((buff[i + TAVARUA_BUF_RAW_RDS] & Process.PROC_TERM_MASK) * Process.SYSTEM_UID) + lowerBand;
            Log.d(TAG, "AF : " + AfList[i]);
        }
        return AfList;
    }

    public boolean setPowerMode(int powerMode) {
        int re;
        if (powerMode == TAVARUA_BUF_EVENTS) {
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
        return new int[]{TAVARUA_BUF_SRCH_LIST, 100};
    }

    public int getSignalThreshold() {
        int signalStrength;
        int rmssiThreshold = FmReceiverJNI.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH);
        Log.d(TAG, "Signal Threshhold: " + rmssiThreshold);
        if (FM_RX_RSSI_LEVEL_VERY_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_WEAK) {
            signalStrength = FM_RX_RSSI_LEVEL_WEAK;
        } else if (FM_RX_RSSI_LEVEL_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_STRONG) {
            signalStrength = FM_RX_RSSI_LEVEL_STRONG;
        } else if (FM_RX_RSSI_LEVEL_STRONG < rmssiThreshold) {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_STRONG;
        } else {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_WEAK;
        }
        switch (signalStrength) {
            case FM_RX_RSSI_LEVEL_VERY_WEAK /*-105*/:
                return TAVARUA_BUF_SRCH_LIST;
            case FM_RX_RSSI_LEVEL_WEAK /*-100*/:
                return TAVARUA_BUF_EVENTS;
            case FM_RX_RSSI_LEVEL_STRONG /*-96*/:
                return TAVARUA_BUF_RT_RDS;
            case FM_RX_RSSI_LEVEL_VERY_STRONG /*-90*/:
                return TAVARUA_BUF_PS_RDS;
            default:
                return TAVARUA_BUF_SRCH_LIST;
        }
    }

    public boolean setRdsGroupOptions(int enRdsGrpsMask, int rdsBuffSize, boolean enRdsChangeFilter) {
        return this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsGrpOptions(enRdsGrpsMask, rdsBuffSize, enRdsChangeFilter) == 0;
    }

    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        return this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsOptions(fmGrpsToProc) == 0;
    }

    public boolean enableAFjump(boolean enable) {
        return this.mRdsData.rdsOn(true) == 0 && this.mRdsData.enableAFjump(enable) == 0;
    }

    public int[] getStationList() {
        int[] stnList = new int[100];
        return this.mControl.stationList(sFd);
    }

    public int getRssi() {
        return FmReceiverJNI.getRSSINative(sFd);
    }

    public boolean getInternalAntenna() {
        if (FmReceiverJNI.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_ANTENNA) == TAVARUA_BUF_EVENTS) {
            return true;
        }
        return false;
    }

    public boolean setInternalAntenna(boolean intAnt) {
        int iAntenna;
        if (intAnt) {
            iAntenna = TAVARUA_BUF_EVENTS;
        } else {
            iAntenna = TAVARUA_BUF_SRCH_LIST;
        }
        if (FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_ANTENNA, iAntenna) == 0) {
            return true;
        }
        return false;
    }

    public byte[] getRawRDS(int numBlocks) {
        byte[] rawRds = new byte[(numBlocks * TAVARUA_BUF_PS_RDS)];
        int re = FmReceiverJNI.getRawRdsNative(sFd, rawRds, numBlocks * TAVARUA_BUF_PS_RDS);
        if (re == numBlocks * TAVARUA_BUF_PS_RDS) {
            return rawRds;
        }
        if (re <= 0) {
            return null;
        }
        byte[] buff = new byte[re];
        System.arraycopy(rawRds, TAVARUA_BUF_SRCH_LIST, buff, TAVARUA_BUF_SRCH_LIST, re);
        return buff;
    }

    public int getFMState() {
        return FmTransceiver.getFMPowerState();
    }

    public int getAudioQuilty(int value) {
        return FmReceiverJNI.getAudioQuiltyNative(sFd, value);
    }

    public int setFmSnrThresh(int value) {
        return FmReceiverJNI.setFmSnrThreshNative(sFd, value);
    }

    public int setFmRssiThresh(int value) {
        return FmReceiverJNI.setFmRssiThreshNative(sFd, value);
    }
}
