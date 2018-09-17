package com.huawei.android.hardware.fmradio;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class FmTransceiver {
    static int FMState = 0;
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
    private static final String METHOD_DISABLE_RADIO = "disableRadio";
    private static final String METHOD_ENABLE_RADIO = "enableRadio";
    private static final String METHOD_IS_RADIO_ENABLED = "isRadioEnabled";
    private static final int MUTE_EVENT = 4;
    private static final int RDS_EVENT = 8;
    private static final int READY_EVENT = 1;
    private static final int SEEK_COMPLETE_EVENT = 3;
    private static final String TAG = "FmTransceiver";
    private static final int TUNE_EVENT = 2;
    private static boolean mRadioMethodImplemented = false;
    static int sFd = 0;
    public static final int subPwrLevel_FMRx_Starting = 4;
    public static final int subPwrLevel_FMTurning_Off = 6;
    public static final int subPwrLevel_FMTx_Starting = 5;
    public static final int subSrchLevel_ScanInProg = 1;
    public static final int subSrchLevel_SeekInPrg = 0;
    public static final int subSrchLevel_SrchAbort = 4;
    public static final int subSrchLevel_SrchComplete = 3;
    public static final int subSrchLevel_SrchListInProg = 2;
    protected FmRxControls mControl;
    protected int mPowerMode;
    protected FmRxRdsData mRdsData;
    protected FmRxEventListner mRxEvents;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hardware.fmradio.FmTransceiver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hardware.fmradio.FmTransceiver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hardware.fmradio.FmTransceiver.<clinit>():void");
    }

    private static void setFd(int fd) {
        sFd = fd;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean acquire(String device) {
        boolean bStatus;
        int retry = subSrchLevel_SeekInPrg;
        if (sFd <= 0) {
            if (!("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE))) {
                if (enableRadio()) {
                    synchronized (this) {
                        Log.d(TAG, "wait Radio on");
                        while (!isRadioEnabled() && mRadioMethodImplemented && retry < FM_ENABLE_RETRY_TIMES) {
                            try {
                                Log.d(TAG, "wait 100ms for radio on , retry =" + retry);
                                retry += subSrchLevel_ScanInProg;
                                wait(100);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "Interrupted when waiting for radio on");
                            }
                        }
                        if (!mRadioMethodImplemented || isRadioEnabled()) {
                        } else {
                            Log.w(TAG, "enable fmradio timeout.");
                            return false;
                        }
                    }
                }
                Log.d(TAG, "fm enableRadio failed");
                return false;
            }
            Log.d(TAG, "Radio on");
            sFd = FmReceiverWrapper.acquireFdNative("/dev/radio0");
            if (sFd > 0) {
                Log.d(TAG, "Opened " + sFd);
                bStatus = true;
            } else {
                Log.d(TAG, "Fail to Open " + sFd);
                bStatus = false;
            }
        } else {
            Log.d(TAG, "Already Opened:" + sFd);
            bStatus = true;
        }
        return bStatus;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean release(String device) {
        if (sFd != 0) {
            int result = FmReceiverWrapper.closeFdNative(sFd);
            sFd = subSrchLevel_SeekInPrg;
            if (result == -1) {
                return false;
            }
            Log.d(TAG, "Turned off: " + sFd);
            if (!("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE))) {
                int retry = subSrchLevel_SeekInPrg;
                if (disableRadio()) {
                    synchronized (this) {
                        Log.d(TAG, "wait Radio off");
                        while (isRadioEnabled() && mRadioMethodImplemented && retry < FM_ENABLE_RETRY_TIMES) {
                            try {
                                Log.d(TAG, "wait 100ms for radio off , retry =" + retry);
                                retry += subSrchLevel_ScanInProg;
                                wait(100);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "Interrupted when waiting for radio on");
                            }
                        }
                        if (mRadioMethodImplemented && isRadioEnabled()) {
                            Log.w(TAG, "disable fmradio timeout.");
                            return false;
                        }
                    }
                } else {
                    Log.d(TAG, "fm disableRadio failed");
                    return false;
                }
            }
            Log.d(TAG, "Radio off");
        } else {
            Log.d(TAG, "Error turning off");
        }
        return true;
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        if (callback != null) {
            this.mRxEvents.startListner(sFd, callback);
            return true;
        }
        Log.d(TAG, "Null, do nothing");
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
        Log.d(TAG, "Null, do nothing");
        return false;
    }

    public boolean unregisterTransmitClient() {
        return true;
    }

    public boolean enable(FmConfig configSettings, int device) {
        if (!acquire("/dev/radio0")) {
            return false;
        }
        Log.d(TAG, "turning on %d" + device);
        this.mControl.fmOn(sFd, device);
        Log.d(TAG, "Calling fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        if (!status) {
            Log.d(TAG, "fmConfigure failed");
            FmReceiverWrapper.closeFdNative(sFd);
            setFd(subSrchLevel_SeekInPrg);
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
        Log.d(TAG, "fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        return setStation(lowerFreq);
    }

    public boolean setStation(int frequencyKHz) {
        if (frequencyKHz <= 0 || this.mControl == null) {
            return false;
        }
        this.mControl.setFreq(frequencyKHz);
        if (this.mControl.setStation(sFd) < 0) {
            return false;
        }
        return true;
    }

    public void setNotchFilter(boolean value) {
        FmReceiverWrapper.setNotchFilterNative(value);
    }

    static void setFMPowerState(int state) {
        FMState = state;
    }

    public static int getFMPowerState() {
        return FMState;
    }

    private boolean enableRadio() {
        try {
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_ENABLE_RADIO, new Class[subSrchLevel_SeekInPrg]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[subSrchLevel_SeekInPrg])).booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "method not found: enableRadio");
            return true;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private static boolean disableRadio() {
        try {
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_DISABLE_RADIO, new Class[subSrchLevel_SeekInPrg]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[subSrchLevel_SeekInPrg])).booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "method not found: disableRadio");
            return true;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private boolean isRadioEnabled() {
        try {
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_IS_RADIO_ENABLED, new Class[subSrchLevel_SeekInPrg]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[subSrchLevel_SeekInPrg])).booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "method not found: isRadioEnabled");
            mRadioMethodImplemented = false;
            return true;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }
}
