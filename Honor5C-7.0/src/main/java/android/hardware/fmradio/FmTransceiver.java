package android.hardware.fmradio;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class FmTransceiver {
    public static int FMState = 0;
    public static final int FMState_Rx_Turned_On = 1;
    public static final int FMState_Srch_InProg = 3;
    public static final int FMState_Turned_Off = 0;
    public static final int FMState_Tx_Turned_On = 2;
    private static final String FM_CHIPTYPE = null;
    public static final int FM_CHSPACE_100_KHZ = 1;
    public static final int FM_CHSPACE_200_KHZ = 0;
    public static final int FM_CHSPACE_50_KHZ = 2;
    public static final int FM_DE_EMP50 = 1;
    public static final int FM_DE_EMP75 = 0;
    public static final int FM_ENABLE_RETRY_TIMES = 100;
    public static final int FM_EU_BAND = 1;
    public static final int FM_JAPAN_STANDARD_BAND = 3;
    public static final int FM_JAPAN_WIDE_BAND = 2;
    public static final int FM_RDS_STD_NONE = 2;
    public static final int FM_RDS_STD_RBDS = 0;
    public static final int FM_RDS_STD_RDS = 1;
    protected static final int FM_RX = 1;
    protected static final int FM_TX = 2;
    public static final int FM_USER_DEFINED_BAND = 4;
    public static final int FM_US_BAND = 0;
    protected static int sFd = 0;
    public static final int subPwrLevel_FMRx_Starting = 4;
    public static final int subPwrLevel_FMTurning_Off = 6;
    public static final int subPwrLevel_FMTx_Starting = 5;
    public static final int subSrchLevel_ScanInProg = 1;
    public static final int subSrchLevel_SeekInPrg = 0;
    public static final int subSrchLevel_SrchAbort = 4;
    public static final int subSrchLevel_SrchComplete = 3;
    public static final int subSrchLevel_SrchListInProg = 2;
    private final int MUTE_EVENT;
    private final int RDS_EVENT;
    private final int READY_EVENT;
    private final int SEEK_COMPLETE_EVENT;
    private final String TAG;
    private final int TUNE_EVENT;
    protected FmRxControls mControl;
    protected int mPowerMode;
    protected FmRxRdsData mRdsData;
    protected FmRxEventListner mRxEvents;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.fmradio.FmTransceiver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.fmradio.FmTransceiver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.fmradio.FmTransceiver.<clinit>():void");
    }

    public FmTransceiver() {
        this.READY_EVENT = subSrchLevel_ScanInProg;
        this.TUNE_EVENT = subSrchLevel_SrchListInProg;
        this.RDS_EVENT = 8;
        this.MUTE_EVENT = subSrchLevel_SrchAbort;
        this.SEEK_COMPLETE_EVENT = subSrchLevel_SrchComplete;
        this.TAG = "FmTransceiver";
    }

    protected boolean acquire(String device) {
        boolean bStatus;
        int retry = subSrchLevel_SeekInPrg;
        if (sFd <= 0) {
            if (!("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE))) {
                BluetoothAdapter btAdap = BluetoothAdapter.getDefaultAdapter();
                if (btAdap.enableRadio()) {
                    synchronized (this) {
                        Log.d("FmTransceiver", "wait Radio on");
                        while (!btAdap.isRadioEnabled() && retry < FM_ENABLE_RETRY_TIMES) {
                            try {
                                Log.d("FmTransceiver", "wait 100ms for radio on , retry =" + retry);
                                retry += subSrchLevel_ScanInProg;
                                wait(100);
                            } catch (InterruptedException e) {
                                Log.d("FmTransceiver", "Interrupted when waiting for radio on");
                            }
                        }
                    }
                } else {
                    Log.d("FmTransceiver", "fm enableRadio failed");
                    return false;
                }
            }
            Log.d("FmTransceiver", "Radio on");
            sFd = FmReceiverJNI.acquireFdNative("/dev/radio0");
            if (sFd > 0) {
                Log.d("FmTransceiver", "Opened " + sFd);
                bStatus = true;
            } else {
                Log.d("FmTransceiver", "Fail to Open " + sFd);
                bStatus = false;
            }
        } else {
            Log.d("FmTransceiver", "Alredy Opened " + sFd);
            bStatus = true;
        }
        return bStatus;
    }

    static boolean release(String device) {
        if (sFd != 0) {
            FmReceiverJNI.closeFdNative(sFd);
            sFd = subSrchLevel_SeekInPrg;
            Log.d("FmTransceiver", "Turned off: " + sFd);
            if (!("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE))) {
                BluetoothAdapter.getDefaultAdapter().disableRadio();
            }
            Log.d("FmTransceiver", "Radio off");
        } else {
            Log.d("FmTransceiver", "Error turning off");
        }
        return true;
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        if (callback != null) {
            this.mRxEvents.startListner(sFd, callback);
            return true;
        }
        Log.d("FmTransceiver", "Null, do nothing");
        return false;
    }

    public boolean unregisterClient() {
        this.mRxEvents.stopListener();
        return true;
    }

    public boolean registerTransmitClient(FmRxEvCallbacks callback) {
        if (callback != null) {
            return true;
        }
        Log.d("FmTransceiver", "Null, do nothing");
        return false;
    }

    public boolean unregisterTransmitClient() {
        return true;
    }

    public boolean enable(FmConfig configSettings, int device) {
        if (!acquire("/dev/radio0")) {
            return false;
        }
        Log.d("FmTransceiver", "turning on %d" + device);
        this.mControl.fmOn(sFd, device);
        Log.d("FmTransceiver", "Calling fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        if (!status) {
            Log.d("FmTransceiver", "fmConfigure failed");
            FmReceiverJNI.closeFdNative(sFd);
            sFd = subSrchLevel_SeekInPrg;
        }
        return status;
    }

    public boolean disable() {
        this.mControl.fmOff(sFd);
        release("/dev/radio0");
        return true;
    }

    public boolean configure(FmConfig configSettings) {
        int lowerFreq = configSettings.getLowerLimit();
        Log.d("FmTransceiver", "fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        return setStation(lowerFreq);
    }

    public boolean setStation(int frequencyKHz) {
        this.mControl.setFreq(frequencyKHz);
        if (this.mControl.setStation(sFd) < 0) {
            return false;
        }
        return true;
    }

    public void setNotchFilter(boolean value) {
        FmReceiverJNI.setNotchFilterNative(value);
    }

    static void setFMPowerState(int state) {
        FMState = state;
    }

    public static int getFMPowerState() {
        return FMState;
    }
}
