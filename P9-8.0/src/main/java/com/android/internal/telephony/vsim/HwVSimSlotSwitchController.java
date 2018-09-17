package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

public class HwVSimSlotSwitchController extends StateMachine {
    private static final /* synthetic */ int[] -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues = null;
    public static final int CARD_TYPE_DUAL_MODE = 3;
    public static final int CARD_TYPE_NO_SIM = 0;
    public static final int CARD_TYPE_SINGLE_CDMA = 2;
    public static final int CARD_TYPE_SINGLE_GSM = 1;
    private static final int CMD_SWITCH_COMMRIL_MODE = 11;
    private static final int COMBINE = 0;
    private static final int EVENT_GET_RAT_COMBINE_MODE_DONE = 34;
    private static final int EVENT_GET_SIM_SLOT_DONE = 2;
    private static final int EVENT_GET_SIM_STATE_DONE = 3;
    private static final int EVENT_INITIAL_TIMEOUT = 35;
    private static final int EVENT_RADIO_POWER_OFF_DONE = 36;
    private static final int EVENT_SET_CDMA_MODE_SIDE_DONE = 15;
    private static final int EVENT_SET_RAT_COMBINE_MODE_DONE = 13;
    private static final int EVENT_SET_SIM_STATE_DONE = 4;
    private static final int EVENT_SWITCH_COMMRIL_MODE_DONE = 12;
    private static final int EVENT_SWITCH_RFIC_CHANNEL_DONE = 14;
    private static final int EVENT_SWITCH_SLOT_DONE = 25;
    private static final long INITIAL_TIMEOUT = 120000;
    private static final int INVALID_COMBINE = -1;
    private static final int INVALID_SIM_SLOT = -1;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false);
    private static final boolean IS_HISI_CDMA_SUPPORTED = SystemProperties.getBoolean(PROPERTY_HISI_CDMA_SUPPORTED, false);
    private static final boolean IS_SUPPORT_FULL_NETWORK = SystemProperties.getBoolean(PROPERTY_FULL_NETWORK_SUPPORT, false);
    private static final String LOG_TAG = "VSimSwitchController";
    private static final int NOT_COMBINE = 1;
    private static final int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    private static final String PROPERTY_CG_STANDBY_MODE = "persist.radio.cg_standby_mode";
    private static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    private static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    private static final String PROPERTY_HISI_CDMA_SUPPORTED = "ro.config.hisi_cdma_supported";
    private static final int RF0 = 0;
    private static final int RF1 = 1;
    private static final int SUB_COUNT = (PHONE_COUNT + 1);
    private static final int SUB_VSIM = PHONE_COUNT;
    private static HwVSimSlotSwitchController sInstance;
    private static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private static final Object sLock = new Object();
    private CommandsInterface[] mCis;
    private Message mCompleteMsg;
    private SlotSwitchDefaultState mDefaultState = new SlotSwitchDefaultState(this, null);
    private CommrilMode mExpectCommrilMode;
    private int mExpectSlot;
    private boolean[] mGotRatCombineMode;
    private boolean mGotSimSlot;
    private boolean mInitDoneSent;
    private InitialState mInitialState = new InitialState(this, null);
    private boolean mIsVSimEnabled;
    private boolean mIsVSimOn;
    private int mMainSlot;
    private boolean mNeedSwitchCommrilMode;
    private int mPollingCount;
    private boolean[] mRadioAvailable;
    private boolean[] mRadioPowerStatus = null;
    private int[] mRatCombineMode;
    private boolean mSlotSwitchDone;
    private SwitchCommrilModeState mSwitchCommrilModeState = new SwitchCommrilModeState(this, null);
    private boolean mSwitchingCommrilMode;
    private Message mUserCompleteMsg;
    private HashMap<Integer, String> mWhatToStringMap;
    private boolean[] switchSlotDoneMark;

    public enum CommrilMode {
        NON_MODE,
        SVLTE_MODE,
        CLG_MODE,
        CG_MODE,
        ULG_MODE,
        HISI_CGUL_MODE,
        HISI_CG_MODE,
        HISI_VSIM_MODE;

        public static CommrilMode getCLGMode() {
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                return HISI_CGUL_MODE;
            }
            return CLG_MODE;
        }

        public static CommrilMode getULGMode() {
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                return HISI_CGUL_MODE;
            }
            return ULG_MODE;
        }

        public static CommrilMode getCGMode() {
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                return HISI_CG_MODE;
            }
            return CG_MODE;
        }

        public static boolean isCLGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            boolean z = true;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK && (HwVSimUtilsInner.isChinaTelecom() ^ 1) != 0) {
                return false;
            }
            if (!HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode != CLG_MODE) {
                    z = false;
                }
                return z;
            } else if (mode != HISI_CGUL_MODE) {
                return false;
            } else {
                if (HwVSimUtilsInner.isChinaTelecom()) {
                    return true;
                }
                if (!(cardType[mainSlot] == 2 || cardType[mainSlot] == 3)) {
                    z = false;
                }
                return z;
            }
        }

        public static boolean isULGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            boolean z = true;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK) {
                if (HwVSimUtilsInner.isPlatformRealTripple()) {
                    return !HwVSimUtilsInner.isChinaTelecom();
                } else {
                    if (!HwVSimUtilsInner.isChinaTelecom()) {
                        return true;
                    }
                }
            }
            if (!HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode != ULG_MODE) {
                    z = false;
                }
                return z;
            } else if (mode != HISI_CGUL_MODE) {
                return false;
            } else {
                if (!(cardType[mainSlot] == 1 || cardType[mainSlot] == 0)) {
                    z = false;
                }
                return z;
            }
        }

        public static boolean isCGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            boolean z = true;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK && (HwVSimUtilsInner.isChinaTelecom() ^ 1) != 0) {
                return false;
            }
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode != HISI_CG_MODE) {
                    z = false;
                }
                return z;
            }
            if (mode != CG_MODE) {
                z = false;
            }
            return z;
        }
    }

    private class InitialState extends State {
        private static final /* synthetic */ int[] -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$vsim$HwVSimSlotSwitchController$CommrilMode;

        private static /* synthetic */ int[] -getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues() {
            if (-com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues != null) {
                return -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues;
            }
            int[] iArr = new int[CommrilMode.values().length];
            try {
                iArr[CommrilMode.CG_MODE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CommrilMode.CLG_MODE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CommrilMode.HISI_CGUL_MODE.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CommrilMode.HISI_CG_MODE.ordinal()] = 5;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CommrilMode.HISI_VSIM_MODE.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CommrilMode.NON_MODE.ordinal()] = 7;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CommrilMode.SVLTE_MODE.ordinal()] = 8;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CommrilMode.ULG_MODE.ordinal()] = 3;
            } catch (NoSuchFieldError e8) {
            }
            -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues = iArr;
            return iArr;
        }

        /* synthetic */ InitialState(HwVSimSlotSwitchController this$0, InitialState -this1) {
            this();
        }

        private InitialState() {
        }

        public void enter() {
            HwVSimSlotSwitchController.this.logd("InitialState: enter");
            checkVSimEnabledStatus();
            for (int i = 0; i < HwVSimSlotSwitchController.SUB_COUNT; i++) {
                HwVSimSlotSwitchController.this.mCis[i].registerForAvailable(HwVSimSlotSwitchController.this.getHandler(), 83, Integer.valueOf(i));
            }
            if (HwVSimSlotSwitchController.this.getHandler() != null) {
                HwVSimSlotSwitchController.this.getHandler().sendEmptyMessageDelayed(35, 120000);
            }
        }

        public void exit() {
            HwVSimSlotSwitchController.this.logd("InitialState: exit");
            if (HwVSimSlotSwitchController.this.getHandler() != null) {
                HwVSimSlotSwitchController.this.getHandler().removeMessages(35);
            }
            for (int i = 0; i < HwVSimSlotSwitchController.SUB_COUNT; i++) {
                HwVSimSlotSwitchController.this.mCis[i].unregisterForAvailable(HwVSimSlotSwitchController.this.getHandler());
            }
        }

        public boolean processMessage(Message msg) {
            HwVSimSlotSwitchController.this.logi("InitialState: what = " + HwVSimSlotSwitchController.this.getWhatToString(msg.what));
            switch (msg.what) {
                case 2:
                    onGetSimSlotDone(msg);
                    return true;
                case 3:
                    if (HwVSimSlotSwitchController.this.mSlotSwitchDone) {
                        onCheckSimMode(msg);
                        return true;
                    }
                    onGetSimStateDone(msg);
                    return true;
                case 4:
                    onSetSimStateDone(msg);
                    return true;
                case 25:
                    onSwithSlotDone(msg);
                    return true;
                case HwVSimSlotSwitchController.EVENT_GET_RAT_COMBINE_MODE_DONE /*34*/:
                    onGetRatCombineModeDone(msg);
                    return true;
                case 35:
                    HwVSimSlotSwitchController.this.loge("warning, initial time out");
                    if (HwVSimSlotSwitchController.this.mMainSlot == -1) {
                        HwVSimSlotSwitchController.this.mMainSlot = 0;
                        HwVSimSlotSwitchController.this.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
                    }
                    checkSwitchSlotDone();
                    return true;
                case HwVSimConstants.EVENT_RADIO_AVAILABLE /*83*/:
                    onAvailableInitial(msg);
                    return true;
                default:
                    HwVSimSlotSwitchController.this.logi("InitialState: not handled msg.what = " + msg.what);
                    return false;
            }
        }

        private void onAvailableInitial(Message msg) {
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= HwVSimSlotSwitchController.this.mCis.length) {
                HwVSimSlotSwitchController.this.logd("InitialState: Invalid index : " + index + " received with event " + msg.what);
            } else {
                HwVSimSlotSwitchController.this.mCis[index.intValue()].getBalongSim(HwVSimSlotSwitchController.this.obtainMessage(2, index));
            }
        }

        private void onGetSimSlotDone(Message msg) {
            HwVSimSlotSwitchController.this.logd("onGetSimSlotDone");
            if (HwVSimSlotSwitchController.this.mGotSimSlot) {
                HwVSimSlotSwitchController.this.logd("onGetSimSlotDone, mGotSimSlot done");
                return;
            }
            AsyncResult ar = msg.obj;
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("ar null");
                return;
            }
            if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == 3) {
                onGetTriSimSlotDone(ar.result);
            } else if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == 2) {
                onGetDualSimSlotDone((int[]) ar.result);
            } else {
                HwVSimSlotSwitchController.this.logd("onGetSimSlotDone got error");
                if (HwVSimSlotSwitchController.this.mMainSlot == -1) {
                    HwVSimSlotSwitchController.this.mMainSlot = 0;
                    HwVSimSlotSwitchController.this.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
                }
            }
        }

        private void onGetTriSimSlotDone(int[] slots) {
            HwVSimSlotSwitchController.this.logd("onGetTriSimSlotDone");
            HwVSimSlotSwitchController.this.logd("result = " + Arrays.toString(slots));
            if (slots[0] == 0 && slots[1] == 1 && slots[2] == 2) {
                HwVSimSlotSwitchController.this.mMainSlot = 0;
                HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 1 && slots[1] == 0 && slots[2] == 2) {
                HwVSimSlotSwitchController.this.mMainSlot = 1;
                HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 2 && slots[1] == 1 && slots[2] == 0) {
                HwVSimSlotSwitchController.this.mMainSlot = 0;
                HwVSimSlotSwitchController.this.mIsVSimOn = true;
            } else if (slots[0] == 2 && slots[1] == 0 && slots[2] == 1) {
                HwVSimSlotSwitchController.this.mMainSlot = 1;
                HwVSimSlotSwitchController.this.mIsVSimOn = true;
            } else {
                HwVSimSlotSwitchController.this.mMainSlot = 0;
            }
            HwVSimSlotSwitchController.this.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController.this.logd("mIsVSimOn = " + HwVSimSlotSwitchController.this.mIsVSimOn);
            HwVSimSlotSwitchController.this.mGotSimSlot = true;
            HwVSimController.getInstance().setSimSlotTable(slots);
            getAllRatCombineMode();
        }

        private void onGetDualSimSlotDone(int[] slots) {
            HwVSimSlotSwitchController.this.logd("onGetDualSimSlotDone");
            HwVSimSlotSwitchController.this.logd("result = " + Arrays.toString(slots));
            if (slots[0] == 1 && slots[1] == 2) {
                HwVSimSlotSwitchController.this.mMainSlot = 0;
            } else if (slots[0] == 2 && slots[1] == 1) {
                HwVSimSlotSwitchController.this.mMainSlot = 1;
            } else {
                HwVSimSlotSwitchController.this.mMainSlot = 0;
            }
            HwVSimSlotSwitchController.this.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController.this.mGotSimSlot = true;
            if (HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].isRadioAvailable()) {
                HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].getSimState(HwVSimSlotSwitchController.this.obtainMessage(3, HwVSimSlotSwitchController.this.mMainSlot));
                return;
            }
            HwVSimSlotSwitchController.this.logi("mainSlot " + HwVSimSlotSwitchController.this.mMainSlot + " is not available !");
            HwVSimSlotSwitchController.this.mCis[2].getSimState(HwVSimSlotSwitchController.this.obtainMessage(3, 2));
        }

        private void onGetSimStateDone(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("onGetSimStateDone : ar null");
                return;
            }
            if (ar.exception != null || ar.result == null || ((int[]) ar.result).length <= 3) {
                HwVSimSlotSwitchController.this.loge("onGetSimStateDone got error !");
            } else {
                int simIndex = ((int[]) ar.result)[0];
                int simEnable = ((int[]) ar.result)[1];
                int simSub = ((int[]) ar.result)[2];
                int simNetinfo = ((int[]) ar.result)[3];
                int slaveSlot = HwVSimSlotSwitchController.this.mMainSlot == 0 ? 1 : 0;
                HwVSimSlotSwitchController.this.logd("[2Cards]simIndex= " + simIndex + ", simEnable= " + simEnable);
                HwVSimSlotSwitchController.this.logd("[2Cards]simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (11 == simIndex && HwVSimSlotSwitchController.this.mCis[2].isRadioAvailable()) {
                    HwVSimSlotSwitchController.this.mIsVSimOn = true;
                } else {
                    HwVSimSlotSwitchController.this.mIsVSimOn = false;
                }
                HwVSimSlotSwitchController.this.logd("mIsVSimOn = " + HwVSimSlotSwitchController.this.mIsVSimOn);
                if (HwVSimSlotSwitchController.this.mIsVSimOn || !HwVSimSlotSwitchController.this.mCis[2].isRadioAvailable()) {
                    int[] slots = new int[3];
                    if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                        slots[0] = 2;
                        slots[1] = slaveSlot;
                        slots[2] = HwVSimSlotSwitchController.this.mMainSlot;
                    } else {
                        slots[0] = HwVSimSlotSwitchController.this.mMainSlot;
                        slots[1] = slaveSlot;
                        slots[2] = 2;
                    }
                    HwVSimController.getInstance().setSimSlotTable(slots);
                    getAllRatCombineMode();
                } else {
                    HwVSimSlotSwitchController.this.logi("telephony is VSIM on, modem is VSIM off, change it first!");
                    HwVSimSlotSwitchController.this.mCis[2].hotSwitchSimSlotFor2Modem(HwVSimSlotSwitchController.this.mMainSlot, slaveSlot, 2, null);
                    HwVSimSlotSwitchController.this.switchSlotDoneMark[HwVSimSlotSwitchController.this.mMainSlot] = false;
                    HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].hotSwitchSimSlotFor2Modem(HwVSimSlotSwitchController.this.mMainSlot, slaveSlot, 2, HwVSimSlotSwitchController.this.obtainMessage(25, HwVSimSlotSwitchController.this.mMainSlot));
                    HwVSimSlotSwitchController.this.switchSlotDoneMark[slaveSlot] = false;
                    HwVSimSlotSwitchController.this.mCis[slaveSlot].hotSwitchSimSlotFor2Modem(HwVSimSlotSwitchController.this.mMainSlot, slaveSlot, 2, HwVSimSlotSwitchController.this.obtainMessage(25, slaveSlot));
                }
            }
        }

        private void onSwithSlotDone(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("onSwithSlotDone : ar null");
            } else if (ar.exception != null) {
                HwVSimSlotSwitchController.this.loge("exception " + ar.exception);
            } else {
                Integer index = HwVSimUtilsInner.getCiIndex(msg);
                if (index.intValue() < 0 || index.intValue() >= HwVSimSlotSwitchController.this.mCis.length) {
                    HwVSimSlotSwitchController.this.logd("onSwithSlotDone: Invalid index : " + index);
                    return;
                }
                HwVSimSlotSwitchController.this.switchSlotDoneMark[index.intValue()] = true;
                int i = 0;
                while (i < HwVSimSlotSwitchController.PHONE_COUNT) {
                    HwVSimSlotSwitchController.this.logi("switchSlotDoneMark[" + i + "] is: " + HwVSimSlotSwitchController.this.switchSlotDoneMark[i]);
                    if (HwVSimSlotSwitchController.this.switchSlotDoneMark[i]) {
                        i++;
                    } else {
                        HwVSimSlotSwitchController.this.logi("switch slot not all ready");
                        return;
                    }
                }
                for (i = 0; i < HwVSimSlotSwitchController.PHONE_COUNT; i++) {
                    HwVSimSlotSwitchController.this.switchSlotDoneMark[i] = false;
                }
                int[] slots = new int[3];
                int slaveSlot = HwVSimSlotSwitchController.this.mMainSlot == 0 ? 1 : 0;
                slots[0] = HwVSimSlotSwitchController.this.mMainSlot;
                slots[1] = slaveSlot;
                slots[2] = 2;
                HwVSimController.getInstance().setSimSlotTable(slots);
                getAllRatCombineMode();
            }
        }

        private void checkVSimEnabledStatus() {
            HwVSimSlotSwitchController.this.logi("checkVSimEnabledStatus");
            if (HwVSimSlotSwitchController.sIsPlatformSupportVSim) {
                HwVSimSlotSwitchController.this.mIsVSimEnabled = HwVSimController.getInstance().isVSimEnabled();
            }
            HwVSimSlotSwitchController.this.logi("mIsVSimEnabled = " + HwVSimSlotSwitchController.this.mIsVSimEnabled);
        }

        private boolean checkRatCombineModeMatched(int mainSlot, CommrilMode mode) {
            int slaveSlot = mainSlot == 0 ? 1 : 0;
            HwVSimSlotSwitchController.this.logi("checkRatCombineModeMatched: CommrilMode = " + mode + ", mainSlot = " + mainSlot + ", ratCombineMode = " + HwVSimSlotSwitchController.this.mRatCombineMode[0] + ", " + HwVSimSlotSwitchController.this.mRatCombineMode[1] + ", " + HwVSimSlotSwitchController.this.mRatCombineMode[2]);
            boolean isInvalid = mainSlot < 0 || mainSlot > 1;
            if (isInvalid) {
                HwVSimSlotSwitchController.this.logi("checkRatCombineModeMatched: SlotId is invalid, return true");
                return true;
            }
            int realMainSlot;
            if (!HwVSimUtils.isPlatformTwoModems()) {
                realMainSlot = HwVSimSlotSwitchController.this.mIsVSimOn ? 2 : mainSlot;
            } else if (HwVSimSlotSwitchController.this.mCis[mainSlot].isRadioAvailable() || HwVSimSlotSwitchController.this.mRatCombineMode[2] == -1) {
                realMainSlot = mainSlot;
            } else {
                HwVSimSlotSwitchController.this.logi("checkRatCombineModeMatched: mainSlot is pending, using VSIM!");
                realMainSlot = 2;
            }
            isInvalid = HwVSimSlotSwitchController.this.mRatCombineMode[realMainSlot] == -1 || HwVSimSlotSwitchController.this.mRatCombineMode[slaveSlot] == -1;
            if (isInvalid) {
                HwVSimSlotSwitchController.this.logi("checkRatCombineModeMatched: ratCombineMode is invalid, return true.");
                return true;
            }
            boolean result;
            switch (-getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues()[mode.ordinal()]) {
                case 1:
                    String cg_standby_mode = SystemProperties.get(HwVSimSlotSwitchController.PROPERTY_CG_STANDBY_MODE, "home");
                    HwVSimSlotSwitchController.this.logi("cg_standby_mode = " + cg_standby_mode);
                    if (!"roam_gsm".equals(cg_standby_mode)) {
                        if (HwVSimSlotSwitchController.this.mRatCombineMode[slaveSlot] != 0 || HwVSimSlotSwitchController.this.mRatCombineMode[realMainSlot] != 1) {
                            result = false;
                            break;
                        }
                        result = true;
                        break;
                    } else if (HwVSimSlotSwitchController.this.mRatCombineMode[realMainSlot] != 1 || HwVSimSlotSwitchController.this.mRatCombineMode[slaveSlot] != 1) {
                        result = false;
                        break;
                    } else {
                        result = true;
                        break;
                    }
                    break;
                case 2:
                    result = true;
                    break;
                case 3:
                    if (HwVSimSlotSwitchController.this.mRatCombineMode[realMainSlot] != 1 || HwVSimSlotSwitchController.this.mRatCombineMode[slaveSlot] != 1) {
                        result = false;
                        break;
                    }
                    result = true;
                    break;
                default:
                    result = true;
                    break;
            }
            return result;
        }

        private void onGetRatCombineModeDone(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("ar null");
                return;
            }
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= HwVSimSlotSwitchController.this.mCis.length) {
                HwVSimSlotSwitchController.this.logd("onGetRatCombineModeDone, Invalid index : " + index);
                return;
            }
            if (ar.exception != null || ar.result == null) {
                HwVSimSlotSwitchController.this.logi("warning, onGetRatCombineModeDone");
                HwVSimSlotSwitchController.this.mGotRatCombineMode[index.intValue()] = true;
            } else {
                HwVSimSlotSwitchController.this.mRatCombineMode[index.intValue()] = ((int[]) ar.result)[0];
                HwVSimSlotSwitchController.this.mGotRatCombineMode[index.intValue()] = true;
            }
            HwVSimSlotSwitchController.this.logi("mRatCombineMode[" + index + "] is " + HwVSimSlotSwitchController.this.mRatCombineMode[index.intValue()]);
            checkAndSwitchSlot();
        }

        private boolean isGotAllRatCombineMode() {
            boolean z = false;
            if (HwVSimUtils.isPlatformTwoModems()) {
                boolean result = true;
                int i = 0;
                while (i < HwVSimSlotSwitchController.SUB_COUNT) {
                    if (HwVSimSlotSwitchController.this.mCis[i].isRadioAvailable() && (HwVSimSlotSwitchController.this.mGotRatCombineMode[i] ^ 1) != 0) {
                        result = false;
                    }
                    i++;
                }
                return result;
            }
            if (HwVSimSlotSwitchController.this.mGotRatCombineMode[0] && HwVSimSlotSwitchController.this.mGotRatCombineMode[1]) {
                z = HwVSimSlotSwitchController.this.mGotRatCombineMode[2];
            }
            return z;
        }

        private void getAllRatCombineMode() {
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                HwVSimSlotSwitchController.this.logi("x mode, done");
                checkSwitchSlotDone();
                return;
            }
            for (int i = 0; i < HwVSimSlotSwitchController.SUB_COUNT; i++) {
                Integer index = Integer.valueOf(i);
                HwVSimSlotSwitchController.this.mCis[index.intValue()].getHwRatCombineMode(HwVSimSlotSwitchController.this.obtainMessage(HwVSimSlotSwitchController.EVENT_GET_RAT_COMBINE_MODE_DONE, index));
            }
        }

        private void checkAndSwitchSlot() {
            HwVSimSlotSwitchController.this.logi("checkAndSwitchSlot");
            HwVSimSlotSwitchController.this.logi("mGotSimSlot = " + HwVSimSlotSwitchController.this.mGotSimSlot + ", mGotRatCombineMode = " + Arrays.toString(HwVSimSlotSwitchController.this.mGotRatCombineMode));
            if (!HwVSimSlotSwitchController.this.mGotSimSlot || (isGotAllRatCombineMode() ^ 1) != 0) {
                HwVSimSlotSwitchController.this.logi("check return");
            } else if (HwVSimSlotSwitchController.this.mIsVSimEnabled) {
                checkCommrilMode();
                dealSlotSwitchAndCommrilMode();
            } else {
                HwVSimSlotSwitchController.this.logi("vsim not enabled, done");
                checkSwitchSlotDone();
            }
        }

        private void checkCommrilMode() {
            HwVSimSlotSwitchController.this.logi("checkCommrilMode");
            if (HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK || (HwVSimUtilsInner.isChinaTelecom() ^ 1) == 0) {
                CommrilMode currentCommrilMode = HwVSimSlotSwitchController.this.getCommrilMode();
                HwVSimSlotSwitchController.this.logi("currentCommrilMode = " + currentCommrilMode);
                if (checkRatCombineModeMatched(HwVSimSlotSwitchController.this.mMainSlot, currentCommrilMode)) {
                    HwVSimSlotSwitchController.this.logi("rat combine mode match");
                    return;
                }
                HwVSimSlotSwitchController.this.mExpectCommrilMode = currentCommrilMode;
                HwVSimSlotSwitchController.this.mExpectSlot = HwVSimSlotSwitchController.this.mMainSlot;
                HwVSimSlotSwitchController.this.mNeedSwitchCommrilMode = true;
                HwVSimSlotSwitchController.this.logi("mNeedSwitchCommrilMode = " + HwVSimSlotSwitchController.this.mNeedSwitchCommrilMode + ", mExpectCommrilMode = " + HwVSimSlotSwitchController.this.mExpectCommrilMode + ", mExpectSlot = " + HwVSimSlotSwitchController.this.mExpectSlot);
                return;
            }
            HwVSimSlotSwitchController.this.logi("full net and telecom not support");
        }

        private void dealSlotSwitchAndCommrilMode() {
            HwVSimSlotSwitchController.this.logi("dealSlotSwitchAndCommrilMode");
            if (HwVSimSlotSwitchController.this.mNeedSwitchCommrilMode) {
                HwVSimSlotSwitchController.this.logi("switch comril mode");
                HwVSimSlotSwitchController.this.switchCommrilMode();
                return;
            }
            HwVSimSlotSwitchController.this.logi("nothing to do, done");
            checkSwitchSlotDone();
        }

        private void checkSwitchSlotDone() {
            HwVSimSlotSwitchController.this.logd("checkSwitchSlotDone");
            HwVSimSlotSwitchController.this.mSlotSwitchDone = true;
            if (HwVSimSlotSwitchController.this.mIsVSimEnabled) {
                HwVSimSlotSwitchController.this.sendInitDone();
                HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mDefaultState);
                return;
            }
            checkSimMode();
        }

        private void checkSimMode() {
            int balongSlot;
            int mainSlot = HwVSimSlotSwitchController.this.mMainSlot;
            if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                balongSlot = 2;
            } else {
                balongSlot = mainSlot;
            }
            HwVSimSlotSwitchController.this.mCis[balongSlot].getSimState(HwVSimSlotSwitchController.this.obtainMessage(3, balongSlot));
        }

        private void onCheckSimMode(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("onGetSimStateDone : ar null");
                return;
            }
            if (ar.exception != null || ar.result == null || ((int[]) ar.result).length <= 3) {
                HwVSimSlotSwitchController.this.loge("onCheckSimMode got error !");
                checkSimModeDone();
            } else {
                int simIndex = ((int[]) ar.result)[0];
                int simEnable = ((int[]) ar.result)[1];
                int simSub = ((int[]) ar.result)[2];
                int simNetinfo = ((int[]) ar.result)[3];
                HwVSimSlotSwitchController.this.logd("simIndex= " + simIndex + ", simEnable= " + simEnable);
                HwVSimSlotSwitchController.this.logd("simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (11 != simIndex || (HwVSimSlotSwitchController.this.mIsVSimEnabled ^ 1) == 0) {
                    checkSimModeDone();
                } else {
                    int balongSlot;
                    int mainSlot = HwVSimSlotSwitchController.this.mMainSlot;
                    if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                        balongSlot = 2;
                    } else {
                        balongSlot = mainSlot;
                    }
                    HwVSimSlotSwitchController.this.mCis[balongSlot].setSimState(simIndex, 0, HwVSimSlotSwitchController.this.obtainMessage(4, balongSlot));
                }
            }
        }

        private void checkSimModeDone() {
            HwVSimSlotSwitchController.this.logd("checkSimModeDone");
            HwVSimSlotSwitchController.this.sendInitDone();
            HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mDefaultState);
        }

        private void onSetSimStateDone(Message msg) {
            int balongSlot;
            int mainSlot = HwVSimSlotSwitchController.this.mMainSlot;
            if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                balongSlot = 2;
            } else {
                balongSlot = mainSlot;
            }
            HwVSimSlotSwitchController.this.mCis[balongSlot].setSimState(1, 1, null);
            checkSimModeDone();
        }
    }

    private class SlotSwitchDefaultState extends State {
        /* synthetic */ SlotSwitchDefaultState(HwVSimSlotSwitchController this$0, SlotSwitchDefaultState -this1) {
            this();
        }

        private SlotSwitchDefaultState() {
        }

        public void enter() {
            HwVSimSlotSwitchController.this.logd("DefaultState: enter");
        }

        public void exit() {
            HwVSimSlotSwitchController.this.logd("DefaultState: exit");
        }

        public boolean processMessage(Message msg) {
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            HwVSimSlotSwitchController.this.logd("DefaultState: what = " + HwVSimSlotSwitchController.this.getWhatToString(msg.what));
            switch (msg.what) {
                case 11:
                    HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mSwitchCommrilModeState);
                    break;
                case HwVSimSlotSwitchController.EVENT_RADIO_POWER_OFF_DONE /*36*/:
                    HwVSimSlotSwitchController.this.logd("DefaultState: EVENT_RADIO_POWER_OFF_DONE index:" + index);
                    HwVSimSlotSwitchController.this.onRadioPowerOffDone(index.intValue());
                    break;
                default:
                    HwVSimSlotSwitchController.this.unhandledMessage(msg);
                    break;
            }
            return true;
        }
    }

    private class SwitchCommrilModeState extends State {
        /* synthetic */ SwitchCommrilModeState(HwVSimSlotSwitchController this$0, SwitchCommrilModeState -this1) {
            this();
        }

        private SwitchCommrilModeState() {
        }

        public void enter() {
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: enter");
            HwVSimSlotSwitchController.this.mPollingCount = 0;
            for (int i = 0; i < HwVSimSlotSwitchController.SUB_COUNT; i++) {
                HwVSimSlotSwitchController.this.mCis[i].registerForAvailable(HwVSimSlotSwitchController.this.getHandler(), 83, Integer.valueOf(i));
            }
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: mExpectSlot : " + HwVSimSlotSwitchController.this.mExpectSlot);
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: mMainSlot : " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: mIsVSimOn : " + HwVSimSlotSwitchController.this.mIsVSimOn);
            if (isNeedSwitchSlot()) {
                Message onCompleted = HwVSimSlotSwitchController.this.obtainMessage(25, Integer.valueOf(HwVSimSlotSwitchController.this.mMainSlot));
                if (HwVSimUtils.isPlatformTwoModems()) {
                    HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].switchBalongSim(HwVSimSlotSwitchController.this.mExpectSlot + 1, HwVSimSlotSwitchController.this.mMainSlot + 1, onCompleted);
                } else if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                    HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].switchBalongSim(2, HwVSimSlotSwitchController.this.mMainSlot, HwVSimSlotSwitchController.this.mExpectSlot, onCompleted);
                } else {
                    HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].switchBalongSim(HwVSimSlotSwitchController.this.mExpectSlot, HwVSimSlotSwitchController.this.mMainSlot, 2, onCompleted);
                }
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount + 1;
                HwVSimSlotSwitchController.this.logi("mPollingCount = " + HwVSimSlotSwitchController.this.mPollingCount);
                return;
            }
            HwVSimSlotSwitchController.this.processSwitchCommrilMode();
        }

        public void exit() {
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: exit");
            HwVSimSlotSwitchController.this.mSwitchingCommrilMode = false;
            for (int i = 0; i < HwVSimSlotSwitchController.SUB_COUNT; i++) {
                HwVSimSlotSwitchController.this.mCis[i].unregisterForAvailable(HwVSimSlotSwitchController.this.getHandler());
            }
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= HwVSimSlotSwitchController.this.mCis.length) {
                HwVSimSlotSwitchController.this.logd("SwitchCommrilModeState: Invalid index : " + index + " received with event " + msg.what);
                return true;
            }
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: what = " + msg.what + ", message = " + HwVSimSlotSwitchController.this.getWhatToString(msg.what) + ", on index " + index);
            AsyncResult ar = msg.obj;
            switch (msg.what) {
                case 13:
                case 14:
                case 15:
                    if (ar == null || ar.exception != null) {
                        HwVSimSlotSwitchController.this.loge("Error! get message " + HwVSimSlotSwitchController.this.getWhatToString(msg.what));
                    } else {
                        HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                        hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount - 1;
                    }
                    HwVSimSlotSwitchController.this.logi("mPollingCount = " + HwVSimSlotSwitchController.this.mPollingCount);
                    if (HwVSimSlotSwitchController.this.mPollingCount == 0) {
                        restartRild();
                        break;
                    }
                    break;
                case 25:
                    onSwitchSlotDone(ar);
                    break;
                case HwVSimConstants.EVENT_RADIO_AVAILABLE /*83*/:
                    onRadioAvailable(index.intValue());
                    break;
                default:
                    HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: not handled msg.what = " + msg.what);
                    retVal = false;
                    break;
            }
            return retVal;
        }

        private void onSwitchSlotDone(AsyncResult ar) {
            if (ar.exception == null) {
                HwVSimSlotSwitchController.this.logi("Switch Sim Slot ok");
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount - 1;
                HwVSimSlotSwitchController.this.logi("mPollingCount = " + HwVSimSlotSwitchController.this.mPollingCount);
                HwVSimSlotSwitchController.this.processSwitchCommrilMode();
                int[] slots = new int[3];
                if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                    slots[0] = 2;
                    slots[1] = HwVSimSlotSwitchController.this.mMainSlot;
                    slots[2] = HwVSimSlotSwitchController.this.mExpectSlot;
                } else {
                    slots[0] = HwVSimSlotSwitchController.this.mExpectSlot;
                    slots[1] = HwVSimSlotSwitchController.this.mMainSlot;
                    slots[2] = 2;
                }
                HwVSimController.getInstance().setSimSlotTable(slots);
                return;
            }
            HwVSimSlotSwitchController.this.loge("exception " + ar.exception);
            HwVSimSlotSwitchController.this.sendInitDone();
            HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mDefaultState);
        }

        private void onRadioAvailable(int index) {
            HwVSimSlotSwitchController.this.logd("onRadioAvailable index = " + index);
            HwVSimSlotSwitchController.this.logi("mPollingCount = " + HwVSimSlotSwitchController.this.mPollingCount);
            if (HwVSimSlotSwitchController.this.mPollingCount == 0) {
                HwVSimSlotSwitchController.this.mRadioAvailable[index] = true;
                HwVSimSlotSwitchController.this.logi("mRadioAvailable[" + index + "] = " + HwVSimSlotSwitchController.this.mRadioAvailable[index]);
                boolean allDone = true;
                int i;
                if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                    if (HwVSimSlotSwitchController.this.mRadioAvailable[HwVSimSlotSwitchController.SUB_VSIM]) {
                        HwVSimSlotSwitchController.this.logi("Expect Slot mRadioAvailable[" + HwVSimSlotSwitchController.this.mExpectSlot + "] " + HwVSimSlotSwitchController.this.mRadioAvailable[HwVSimSlotSwitchController.this.mExpectSlot] + " --> true");
                        HwVSimSlotSwitchController.this.mRadioAvailable[HwVSimSlotSwitchController.this.mExpectSlot] = true;
                    }
                    for (i = 0; i < HwVSimSlotSwitchController.SUB_COUNT; i++) {
                        if (!HwVSimSlotSwitchController.this.mRadioAvailable[i]) {
                            allDone = false;
                            break;
                        }
                    }
                } else {
                    for (i = 0; i < HwVSimSlotSwitchController.PHONE_COUNT; i++) {
                        HwVSimSlotSwitchController.this.logd("mRadioAvailable[" + i + "] = " + HwVSimSlotSwitchController.this.mRadioAvailable[i]);
                    }
                    for (i = 0; i < HwVSimSlotSwitchController.PHONE_COUNT; i++) {
                        if (!HwVSimSlotSwitchController.this.mRadioAvailable[i]) {
                            allDone = false;
                            break;
                        }
                    }
                }
                if (allDone) {
                    HwVSimSlotSwitchController.this.logd("all done");
                    HwVSimSlotSwitchController.this.logd("mCompleteMsg = " + HwVSimSlotSwitchController.this.mCompleteMsg);
                    if (HwVSimSlotSwitchController.this.mCompleteMsg != null) {
                        HwVSimSlotSwitchController.this.logi("Switch CommrilMode Done!!");
                        AsyncResult.forMessage(HwVSimSlotSwitchController.this.mCompleteMsg);
                        HwVSimSlotSwitchController.this.mCompleteMsg.sendToTarget();
                        HwVSimSlotSwitchController.this.mCompleteMsg = null;
                        if (isNeedSwitchSlot()) {
                            HwVSimSlotSwitchController.this.logi("update mainSlot to " + HwVSimSlotSwitchController.this.mExpectSlot);
                            HwVSimSlotSwitchController.this.mMainSlot = HwVSimSlotSwitchController.this.mExpectSlot;
                            HwVSimSlotSwitchController.this.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
                        }
                        if (HwVSimSlotSwitchController.this.mUserCompleteMsg != null) {
                            HwVSimSlotSwitchController.this.logi("switchCommrilMode ------>>> end");
                            AsyncResult.forMessage(HwVSimSlotSwitchController.this.mUserCompleteMsg, Boolean.valueOf(true), null);
                            HwVSimSlotSwitchController.this.mUserCompleteMsg.sendToTarget();
                            HwVSimSlotSwitchController.this.mUserCompleteMsg = null;
                        }
                        HwVSimSlotSwitchController.this.sendInitDone();
                        HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mDefaultState);
                    }
                } else {
                    HwVSimSlotSwitchController.this.logd("not done");
                }
            }
        }

        private boolean isNeedSwitchSlot() {
            return HwVSimSlotSwitchController.this.mExpectSlot != HwVSimSlotSwitchController.this.mMainSlot;
        }

        private void restartRild() {
            if (HwVSimSlotSwitchController.this.mExpectCommrilMode != CommrilMode.NON_MODE) {
                HwVSimSlotSwitchController.this.logi("setCommrilMode to " + HwVSimSlotSwitchController.this.mExpectCommrilMode);
                HwVSimSlotSwitchController.this.setCommrilMode(HwVSimSlotSwitchController.this.mExpectCommrilMode);
                HwVSimSlotSwitchController.this.mExpectCommrilMode = CommrilMode.NON_MODE;
            }
            resetStatus();
            HwVSimSlotSwitchController.this.restartRildBySubState();
        }

        private void resetStatus() {
            HwVSimSlotSwitchController.this.logi("resetStatus");
            for (int i = 0; i < HwVSimSlotSwitchController.SUB_COUNT; i++) {
                HwVSimSlotSwitchController.this.mRadioAvailable[i] = false;
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues() {
        if (-com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues != null) {
            return -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues;
        }
        int[] iArr = new int[CommrilMode.values().length];
        try {
            iArr[CommrilMode.CG_MODE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommrilMode.CLG_MODE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommrilMode.HISI_CGUL_MODE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommrilMode.HISI_CG_MODE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommrilMode.HISI_VSIM_MODE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommrilMode.NON_MODE.ordinal()] = 7;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommrilMode.SVLTE_MODE.ordinal()] = 8;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommrilMode.ULG_MODE.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues = iArr;
        return iArr;
    }

    public static void create(Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        slogd("create");
        synchronized (sLock) {
            if (sInstance != null) {
                throw new RuntimeException("VSimSlotSwitchController already created");
            }
            sInstance = new HwVSimSlotSwitchController(context, vsimCi, cis);
            sInstance.start();
        }
    }

    public static HwVSimSlotSwitchController getInstance() {
        HwVSimSlotSwitchController hwVSimSlotSwitchController;
        synchronized (sLock) {
            if (sInstance == null) {
                throw new RuntimeException("VSimSlotSwitchController not yet created");
            }
            hwVSimSlotSwitchController = sInstance;
        }
        return hwVSimSlotSwitchController;
    }

    private HwVSimSlotSwitchController(Context c, CommandsInterface vsimCi, CommandsInterface[] cis) {
        int i;
        super(LOG_TAG, Looper.myLooper());
        logd("VSimSlotSwitchController");
        logi("IS_SUPPORT_FULL_NETWORK: " + IS_SUPPORT_FULL_NETWORK);
        logi("IS_HISI_CDMA_SUPPORTED: " + IS_HISI_CDMA_SUPPORTED);
        this.mCis = new CommandsInterface[(cis.length + 1)];
        for (i = 0; i < cis.length; i++) {
            this.mCis[i] = cis[i];
        }
        this.mCis[cis.length] = vsimCi;
        initWhatToStringMap();
        this.mSlotSwitchDone = false;
        this.mGotSimSlot = false;
        this.mRadioAvailable = new boolean[SUB_COUNT];
        this.mGotRatCombineMode = new boolean[SUB_COUNT];
        this.mRatCombineMode = new int[SUB_COUNT];
        this.mRadioPowerStatus = new boolean[SUB_COUNT];
        for (i = 0; i < SUB_COUNT; i++) {
            this.mRadioAvailable[i] = false;
            this.mGotRatCombineMode[i] = false;
            this.mRatCombineMode[i] = -1;
            this.mRadioPowerStatus[i] = false;
        }
        this.switchSlotDoneMark = new boolean[PHONE_COUNT];
        for (i = 0; i < PHONE_COUNT; i++) {
            this.switchSlotDoneMark[i] = false;
        }
        this.mMainSlot = -1;
        this.mExpectSlot = -1;
        this.mExpectCommrilMode = CommrilMode.NON_MODE;
        this.mIsVSimOn = false;
        this.mIsVSimEnabled = false;
        this.mNeedSwitchCommrilMode = false;
        this.mInitDoneSent = false;
        this.mSwitchingCommrilMode = false;
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mSwitchCommrilModeState, this.mDefaultState);
        setInitialState(this.mInitialState);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private static void slogd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void logi(String s) {
        HwVSimLog.VSimLogI(LOG_TAG, s);
    }

    protected void loge(String s) {
        HwVSimLog.VSimLogE(LOG_TAG, s);
    }

    protected void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
    }

    protected String getWhatToString(int what) {
        String str = null;
        if (this.mWhatToStringMap != null) {
            str = (String) this.mWhatToStringMap.get(Integer.valueOf(what));
        }
        if (str == null) {
            return "<unknown message> - " + what;
        }
        return str;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println(" dummy");
    }

    private void initWhatToStringMap() {
        this.mWhatToStringMap = new HashMap();
        this.mWhatToStringMap.put(Integer.valueOf(2), "EVENT_GET_SIM_SLOT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(3), "EVENT_GET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(4), "EVENT_SET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(11), "CMD_SWITCH_COMMRIL_MODE");
        this.mWhatToStringMap.put(Integer.valueOf(12), "EVENT_SWITCH_COMMRIL_MODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(13), "EVENT_SET_RAT_COMBINE_MODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(14), "EVENT_SWITCH_RFIC_CHANNEL_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(15), "EVENT_SET_CDMA_MODE_SIDE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(83), "EVENT_RADIO_AVAILABLE");
        this.mWhatToStringMap.put(Integer.valueOf(25), "EVENT_SWITCH_SLOT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_GET_RAT_COMBINE_MODE_DONE), "EVENT_GET_RAT_COMBINE_MODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(35), "EVENT_INITIAL_TIMEOUT");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_RADIO_POWER_OFF_DONE), "EVENT_RADIO_POWER_OFF_DONE");
    }

    private void sendInitDone() {
        if (!this.mInitDoneSent) {
            HwVSimController.getInstance().getHandler().sendEmptyMessage(5);
            this.mInitDoneSent = true;
        }
    }

    public CommrilMode getCommrilMode() {
        String mode = SystemProperties.get("persist.radio.commril_mode", "CLG_MODE");
        CommrilMode result = CommrilMode.NON_MODE;
        try {
            return (CommrilMode) Enum.valueOf(CommrilMode.class, mode);
        } catch (IllegalArgumentException e) {
            logd("getCommrilMode, IllegalArgumentException, mode = " + mode);
            return result;
        }
    }

    private void setCommrilMode(CommrilMode mode) {
        SystemProperties.set("persist.radio.commril_mode", mode.toString());
    }

    public CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        if (mainSlot == -1) {
            logd("main slot invalid");
            return expectCommrilMode;
        }
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        expectCommrilMode = (cardType[mainSlot] == 2 || cardType[mainSlot] == 3) ? IS_HISI_CDMA_SUPPORTED ? CommrilMode.HISI_CGUL_MODE : CommrilMode.CLG_MODE : ((cardType[mainSlot] == 1 || cardType[mainSlot] == 0) && (cardType[slaveSlot] == 2 || cardType[slaveSlot] == 3)) ? IS_HISI_CDMA_SUPPORTED ? CommrilMode.HISI_CG_MODE : CommrilMode.CG_MODE : (cardType[mainSlot] == 1 || cardType[slaveSlot] == 1) ? IS_HISI_CDMA_SUPPORTED ? CommrilMode.HISI_CGUL_MODE : CommrilMode.ULG_MODE : CommrilMode.NON_MODE;
        logd("[getExpectCommrilMode]: expectCommrilMode = " + expectCommrilMode);
        return expectCommrilMode;
    }

    private void switchCommrilMode() {
        logd("switchCommrilMode");
        this.mCompleteMsg = obtainMessage(12, Integer.valueOf(0));
        Message msg = obtainMessage(11, Integer.valueOf(0));
        AsyncResult.forMessage(msg);
        msg.sendToTarget();
    }

    private void processSwitchCommrilMode() {
        CommrilMode newCommrilMode = this.mExpectCommrilMode;
        int mainSlot = this.mMainSlot;
        logi("[switchCommrilMode]: newCommrilMode = " + newCommrilMode + ", mainSlot = " + mainSlot + ", is vsim on = " + this.mIsVSimOn);
        if (mainSlot < 0 || mainSlot >= this.mCis.length) {
            logi("main slot invalid");
            return;
        }
        int balongSlot;
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (this.mIsVSimOn) {
            balongSlot = 2;
        } else {
            balongSlot = mainSlot;
        }
        switch (-getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues()[newCommrilMode.ordinal()]) {
            case 1:
                this.mCis[balongSlot].setHwRatCombineMode(1, obtainMessage(13, Integer.valueOf(mainSlot)));
                String cg_standby_mode = SystemProperties.get(PROPERTY_CG_STANDBY_MODE, "home");
                logi("[switchCommrilMode]: cg_standby_mode = " + cg_standby_mode);
                if ("roam_gsm".equals(cg_standby_mode)) {
                    this.mCis[slaveSlot].setHwRatCombineMode(1, obtainMessage(13, Integer.valueOf(slaveSlot)));
                    this.mCis[balongSlot].setHwRFChannelSwitch(0, obtainMessage(14, Integer.valueOf(mainSlot)));
                } else {
                    this.mCis[slaveSlot].setHwRatCombineMode(0, obtainMessage(13, Integer.valueOf(slaveSlot)));
                    this.mCis[balongSlot].setHwRFChannelSwitch(1, obtainMessage(14, Integer.valueOf(mainSlot)));
                }
                this.mPollingCount += 3;
                logi("[switchCommrilMode]: Send set CG_MODE request done...");
                break;
            case 2:
                boolean clg_overseas_mode = SystemProperties.getBoolean("persist.radio.overseas_mode", false);
                logi("[switchCommrilMode]: clg_overseas_mode = " + clg_overseas_mode);
                if (clg_overseas_mode) {
                    this.mCis[mainSlot].setHwRatCombineMode(1, obtainMessage(13, Integer.valueOf(mainSlot)));
                } else {
                    this.mCis[mainSlot].setHwRatCombineMode(0, obtainMessage(13, Integer.valueOf(mainSlot)));
                }
                if (HwVSimUtils.isPlatformTwoModems() && HwVSimUtilsInner.isChinaTelecom()) {
                    this.mPollingCount++;
                } else {
                    this.mCis[slaveSlot].setHwRatCombineMode(1, obtainMessage(13, Integer.valueOf(slaveSlot)));
                    this.mCis[mainSlot].setHwRFChannelSwitch(0, obtainMessage(14, Integer.valueOf(mainSlot)));
                    this.mPollingCount += 3;
                }
                logi("[switchCommrilMode]: Send set CLG_MODE request done...");
                break;
            case 3:
                this.mCis[balongSlot].setCdmaModeSide(0, obtainMessage(15));
                this.mPollingCount++;
                logi("[switchCommrilMode]: Send set HISI_CGUL_MODE request done...");
                break;
            case 4:
                this.mCis[balongSlot].setCdmaModeSide(1, obtainMessage(15));
                this.mPollingCount++;
                logi("[switchCommrilMode]: Send set HISI_CG_MODE request done...");
                break;
            case 5:
                this.mCis[balongSlot].setCdmaModeSide(2, obtainMessage(15));
                this.mPollingCount++;
                logi("[switchCommrilMode]: Send set HISI_VSIM_MODE request done...");
                break;
            case 6:
                this.mCis[balongSlot].setHwRatCombineMode(1, obtainMessage(13, Integer.valueOf(mainSlot)));
                this.mCis[slaveSlot].setHwRatCombineMode(1, obtainMessage(13, Integer.valueOf(slaveSlot)));
                this.mCis[balongSlot].setHwRFChannelSwitch(0, obtainMessage(14, Integer.valueOf(mainSlot)));
                this.mPollingCount += 3;
                logi("[switchCommrilMode]: Send set ULG_MODE request done...");
                break;
            default:
                loge("[switchCommrilMode]: Error!! Shouldn't enter here!!");
                break;
        }
    }

    public void switchCommrilMode(CommrilMode expectCommrilMode, int expectSlot, int mainSlot, boolean isVSimOn, Message onCompleteMsg) {
        logi("switchCommrilMode ------>>> begin");
        if (IS_SUPPORT_FULL_NETWORK || (HwVSimUtilsInner.isChinaTelecom() ^ 1) == 0) {
            logi("expectCommrilMode = " + expectCommrilMode + ", expectSlot = " + expectSlot + ", mainSlot = " + mainSlot + ", isVSimOn = " + isVSimOn);
            this.mExpectCommrilMode = expectCommrilMode;
            this.mUserCompleteMsg = onCompleteMsg;
            this.mExpectSlot = expectSlot;
            this.mMainSlot = mainSlot;
            this.mIsVSimOn = isVSimOn;
            if (IS_FAST_SWITCH_SIMSLOT) {
                setAllRaidoPowerOff();
            } else {
                switchCommrilMode();
            }
            return;
        }
        logi("switchCommrilMode ------>>> end, because full net and telecom not support");
        if (onCompleteMsg != null) {
            AsyncResult.forMessage(onCompleteMsg, Boolean.valueOf(false), null);
            onCompleteMsg.sendToTarget();
        }
    }

    private void setAllRaidoPowerOff() {
        int subId;
        HwVSimController mVSimController = HwVSimController.getInstance();
        mVSimController.setIsWaitingSwitchCdmaModeSide(true);
        for (subId = 0; subId < this.mCis.length; subId++) {
            this.mRadioPowerStatus[subId] = this.mCis[subId].getRadioState().isOn();
        }
        for (subId = 0; subId < this.mCis.length; subId++) {
            if (this.mRadioPowerStatus[subId]) {
                logd("setAllRaidoPowerOff:mRadioPowerOnStatus[" + subId + "]=" + this.mRadioPowerStatus[subId] + " -> off");
                Message onCompleted = obtainMessage(EVENT_RADIO_POWER_OFF_DONE, Integer.valueOf(subId));
                ((GsmCdmaPhone) mVSimController.getPhoneBySub(subId)).getServiceStateTracker().setDesiredPowerState(false);
                this.mCis[subId].setRadioPower(false, onCompleted);
            } else {
                logd("setAllRaidoPowerOff:mRadioPowerOnStatus[" + subId + "]=" + this.mRadioPowerStatus[subId] + " is off");
                onRadioPowerOffDone(subId);
            }
        }
    }

    private void onRadioPowerOffDone(int subId) {
        this.mRadioPowerStatus[subId] = false;
        logd("onRadioPowerOffDone:mRadioPowerStatus[" + subId + "]=" + this.mRadioPowerStatus[subId]);
        boolean isAllRadioPowerOff = true;
        for (int i = 0; i < this.mCis.length; i++) {
            if (this.mRadioPowerStatus[i]) {
                isAllRadioPowerOff = false;
                break;
            }
        }
        if (isAllRadioPowerOff && (this.mSwitchingCommrilMode ^ 1) != 0) {
            logd("onRadioPowerOffDone: AllRadioPowerOff -> switchCommrilMode");
            this.mSwitchingCommrilMode = true;
            switchCommrilMode();
        }
    }

    public void restartRildBySubState() {
        HwVSimController mVSimController = HwVSimController.getInstance();
        for (int subId = 0; subId < PHONE_COUNT; subId++) {
            if (mVSimController.getSubState(subId) != 0) {
                logd("restartRild : setDesiredPowerState is true for subId:" + subId);
                ((GsmCdmaPhone) mVSimController.getPhoneBySub(subId)).getServiceStateTracker().setDesiredPowerState(true);
            } else {
                logd("restartRild : no need to setDesiredPowerState is true for subId:" + subId);
            }
        }
        logi("restart rild");
        if (this.mIsVSimOn) {
            this.mCis[SUB_VSIM].restartRild(null);
        } else {
            this.mCis[this.mMainSlot].restartRild(null);
        }
        if (!HwVSimController.getInstance().isVSimEnabled()) {
            HwVSimPhoneFactory.setIsVsimEnabledProp(false);
        }
        HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
        HwVSimNvMatchController.getInstance().storeIfNeedRestartRildForNvMatch(false);
    }

    public static boolean isCDMACard(int cardtype) {
        if (cardtype == 2 || cardtype == 3) {
            return true;
        }
        return false;
    }

    public CommrilMode getVSimOnCommrilMode(boolean isVSimOn, int mainSlot, int[] cardTypes) {
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        boolean mainSlotIsCDMACard = isCDMACard(cardTypes[mainSlot]);
        boolean slaveSlotIsCDMACard = isCDMACard(cardTypes[slaveSlot]);
        if (mainSlotIsCDMACard && slaveSlotIsCDMACard) {
            return CommrilMode.HISI_CG_MODE;
        }
        if (mainSlotIsCDMACard) {
            return isVSimOn ? CommrilMode.HISI_VSIM_MODE : CommrilMode.HISI_CG_MODE;
        } else {
            if (slaveSlotIsCDMACard) {
                return isVSimOn ? CommrilMode.HISI_CG_MODE : CommrilMode.HISI_VSIM_MODE;
            } else {
                CommrilMode vSimOnCommrilMode = getCommrilMode();
                logd("no c-card, not change commril mode. vSimOnCommrilMode = " + vSimOnCommrilMode);
                return vSimOnCommrilMode;
            }
        }
    }
}
