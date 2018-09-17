package com.huawei.android.hardware.fmradio;

import android.bluetooth.BluetoothAdapter;
import android.os.SystemProperties;
import android.util.Log;

public class FmTransceiver {
    static int FMState = 0;
    public static final int FMState_Rx_Turned_On = 1;
    public static final int FMState_Srch_InProg = 3;
    public static final int FMState_Turned_Off = 0;
    public static final int FMState_Tx_Turned_On = 2;
    private static final String FM_CHIPTYPE = SystemProperties.get("ro.connectivity.chiptype");
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
    private static boolean mRadioMethodImplemented = true;
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
    protected FmRxRdsData mRdsData;
    protected FmRxEventListner mRxEvents;

    private static void setFd(int fd) {
        sFd = fd;
    }

    protected boolean acquire(String device) {
        boolean bStatus;
        int retry = 0;
        if (sFd <= 0) {
            if (!("hi110x".equals(FM_CHIPTYPE) || ("hisi".equals(FM_CHIPTYPE) ^ 1) == 0 || ("Qualcomm".equals(FM_CHIPTYPE) ^ 1) == 0)) {
                if (enableRadio()) {
                    synchronized (this) {
                        Log.d(TAG, "wait Radio on");
                        while (!isRadioEnabled() && mRadioMethodImplemented && retry < 100) {
                            try {
                                Log.d(TAG, "wait 100ms for radio on , retry =" + retry);
                                retry++;
                                wait(100);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "Interrupted when waiting for radio on");
                            }
                        }
                        if (!mRadioMethodImplemented || (isRadioEnabled() ^ 1) == 0) {
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

    boolean release(String device) {
        if (sFd != 0) {
            int result = FmReceiverWrapper.closeFdNative(sFd);
            sFd = 0;
            if (result == -1) {
                return false;
            }
            Log.d(TAG, "Turned off: " + sFd);
            if (!("hi110x".equals(FM_CHIPTYPE) || ("hisi".equals(FM_CHIPTYPE) ^ 1) == 0 || ("Qualcomm".equals(FM_CHIPTYPE) ^ 1) == 0)) {
                int retry = 0;
                if (disableRadio()) {
                    synchronized (this) {
                        Log.d(TAG, "wait Radio off");
                        while (isRadioEnabled() && mRadioMethodImplemented && retry < 100) {
                            try {
                                Log.d(TAG, "wait 100ms for radio off , retry =" + retry);
                                retry++;
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
            setFd(0);
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
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_ENABLE_RADIO, new Class[0]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[0])).booleanValue();
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
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_DISABLE_RADIO, new Class[0]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[0])).booleanValue();
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
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_IS_RADIO_ENABLED, new Class[0]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[0])).booleanValue();
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
