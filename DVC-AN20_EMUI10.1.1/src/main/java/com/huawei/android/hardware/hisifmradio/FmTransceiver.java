package com.huawei.android.hardware.hisifmradio;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import com.huawei.android.hardware.fmradio.FmConfig;
import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import java.lang.reflect.InvocationTargetException;

/* access modifiers changed from: package-private */
public class FmTransceiver {
    static int FMState = 0;
    public static final int FMState_Rx_Turned_On = 1;
    public static final int FMState_Srch_InProg = 3;
    public static final int FMState_Turned_Off = 0;
    public static final int FMState_Tx_Turned_On = 2;
    private static final String FM_CHIPTYPE = SystemPropertiesEx.get("ro.connectivity.chiptype");
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
    private static final String TAG = "Hisi-FmTransceiver";
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

    FmTransceiver() {
    }

    private static void setFd(int fd) {
        sFd = fd;
    }

    /* access modifiers changed from: protected */
    public boolean acquire(String device) {
        int retry = 0;
        if (sFd > 0) {
            return true;
        }
        if (!"hi110x".equals(FM_CHIPTYPE) && !"hisi".equals(FM_CHIPTYPE) && !"Qualcomm".equals(FM_CHIPTYPE)) {
            if (!enableRadio()) {
                return false;
            }
            synchronized (this) {
                while (!isRadioEnabled() && mRadioMethodImplemented && retry < 100) {
                    retry++;
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Interrupted when waiting for radio on");
                    }
                }
                if (retry >= 100) {
                    return false;
                }
            }
        }
        sFd = FmReceiverWrapper.acquireFdNative("/dev/radio0");
        if (sFd > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean release(String device) {
        int i = sFd;
        if (i != 0) {
            int result = FmReceiverWrapper.closeFdNative(i);
            sFd = 0;
            if (result == -1) {
                return false;
            }
            if ("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE)) {
                return true;
            }
            int retry = 0;
            if (!disableRadio()) {
                return false;
            }
            synchronized (this) {
                while (isRadioEnabled() && mRadioMethodImplemented && retry < 100) {
                    retry++;
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Interrupted when waiting for radio on");
                    }
                }
                if (retry >= 100) {
                    return false;
                }
                return true;
            }
        }
        Log.d("FmTransceiver", "Error turning off");
        return true;
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        if (callback == null) {
            return false;
        }
        SlogEx.e(TAG, "registerClient " + callback);
        this.mRxEvents.startListner(sFd, callback);
        return true;
    }

    public boolean unregisterClient() {
        this.mRxEvents.stopListener();
        return true;
    }

    public boolean registerTransmitClient(FmRxEvCallbacks callback) {
        if (callback != null) {
            return true;
        }
        return false;
    }

    public boolean unregisterTransmitClient() {
        return true;
    }

    public boolean enable(BaseFmConfig configSettings, int device) {
        if (!acquire("/dev/radio0")) {
            SlogEx.e(TAG, "enable acquire device fail");
            return false;
        }
        this.mControl.fmOn(sFd, device);
        boolean status = false;
        if (configSettings != null) {
            status = configSettings.fmConfigure(sFd);
        }
        SlogEx.e(TAG, "fmConfigure status = " + status);
        if (!status) {
            FmReceiverWrapper.closeFdNative(sFd);
            setFd(0);
        }
        return status;
    }

    public boolean enable(FmConfig configSettings, int device) {
        return false;
    }

    public boolean disable() {
        this.mControl.fmOff(sFd);
        release("/dev/radio0");
        return true;
    }

    public boolean configure(BaseFmConfig configSettings) {
        if (configSettings == null) {
            Log.w(TAG, "configure(): configSettings is null!");
            return false;
        }
        int lowerFreq = configSettings.getLowerLimit();
        configSettings.fmConfigure(sFd);
        return setStation(lowerFreq);
    }

    public boolean configure(FmConfig configSettings) {
        return false;
    }

    public boolean setStation(int frequencyKHz) {
        FmRxControls fmRxControls;
        if (frequencyKHz <= 0 || (fmRxControls = this.mControl) == null) {
            return false;
        }
        fmRxControls.setFreq(frequencyKHz);
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
        } catch (IllegalAccessException | InvocationTargetException e2) {
            Log.w(TAG, "method invoke error");
            return false;
        }
    }

    private static boolean disableRadio() {
        try {
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_DISABLE_RADIO, new Class[0]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "method not found: disableRadio");
            return true;
        } catch (IllegalAccessException | InvocationTargetException e2) {
            Log.w(TAG, "method invoke error");
            return false;
        }
    }

    private boolean isRadioEnabled() {
        try {
            return ((Boolean) BluetoothAdapter.class.getMethod(METHOD_IS_RADIO_ENABLED, new Class[0]).invoke(BluetoothAdapter.getDefaultAdapter(), new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            mRadioMethodImplemented = false;
            return true;
        } catch (IllegalAccessException | InvocationTargetException e2) {
            Log.w(TAG, "method invoke error");
            return false;
        }
    }
}
