package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

public class HwVSimSlotSwitchController extends StateMachine {
    public static final int CARD_TYPE_DUAL_MODE = 3;
    public static final int CARD_TYPE_INVALID = -1;
    public static final int CARD_TYPE_NO_SIM = 0;
    public static final int CARD_TYPE_SINGLE_CDMA = 2;
    public static final int CARD_TYPE_SINGLE_GSM = 1;
    private static final int CMD_SWITCH_COMMRIL_MODE = 11;
    private static final int COMBINE = 0;
    private static final int EVENT_GET_SIM_SLOT_DONE = 2;
    private static final int EVENT_GET_SIM_STATE_DONE = 3;
    private static final int EVENT_INITIAL_TIMEOUT = 35;
    private static final int EVENT_RADIO_POWER_OFF_DONE = 36;
    private static final int EVENT_SET_CDMA_MODE_SIDE_DONE = 15;
    private static final int EVENT_SET_SIM_STATE_DONE = 4;
    private static final int EVENT_SWITCH_COMMRIL_MODE_DONE = 12;
    private static final int EVENT_SWITCH_SLOT_DONE = 25;
    private static final long INITIAL_TIMEOUT = 120000;
    private static final int INVALID_COMBINE = -1;
    private static final int INVALID_SIM_SLOT = -1;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false);
    /* access modifiers changed from: private */
    public static final boolean IS_HISI_CDMA_SUPPORTED = SystemProperties.getBoolean(PROPERTY_HISI_CDMA_SUPPORTED, false);
    /* access modifiers changed from: private */
    public static final boolean IS_SUPPORT_FULL_NETWORK = SystemProperties.getBoolean("ro.config.full_network_support", false);
    private static final String LOG_TAG = "VSimSwitchController";
    private static final int NOT_COMBINE = 1;
    /* access modifiers changed from: private */
    public static final int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    private static final String PROPERTY_CG_STANDBY_MODE = "persist.radio.cg_standby_mode";
    private static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    private static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    private static final String PROPERTY_HISI_CDMA_SUPPORTED = "ro.config.hisi_cdma_supported";
    private static final int RF0 = 0;
    private static final int RF1 = 1;
    /* access modifiers changed from: private */
    public static final int SUB_COUNT = (PHONE_COUNT + 1);
    /* access modifiers changed from: private */
    public static final int SUB_VSIM = PHONE_COUNT;
    private static HwVSimSlotSwitchController sInstance;
    /* access modifiers changed from: private */
    public static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private static final Object sLock = new Object();
    /* access modifiers changed from: private */
    public CommandsInterface[] mCis;
    /* access modifiers changed from: private */
    public Message mCompleteMsg;
    /* access modifiers changed from: private */
    public SlotSwitchDefaultState mDefaultState = new SlotSwitchDefaultState();
    /* access modifiers changed from: private */
    public CommrilMode mExpectCommrilMode;
    /* access modifiers changed from: private */
    public int mExpectSlot;
    /* access modifiers changed from: private */
    public boolean mGotSimSlot;
    private boolean mInitDoneSent;
    private InitialState mInitialState = new InitialState();
    /* access modifiers changed from: private */
    public boolean mIsVSimEnabled;
    /* access modifiers changed from: private */
    public boolean mIsVSimOn;
    /* access modifiers changed from: private */
    public int mMainSlot;
    /* access modifiers changed from: private */
    public int mPollingCount;
    /* access modifiers changed from: private */
    public boolean[] mRadioAvailable;
    private boolean[] mRadioPowerStatus = null;
    /* access modifiers changed from: private */
    public boolean mSlotSwitchDone;
    /* access modifiers changed from: private */
    public SwitchCommrilModeState mSwitchCommrilModeState = new SwitchCommrilModeState();
    /* access modifiers changed from: private */
    public boolean mSwitchingCommrilMode;
    /* access modifiers changed from: private */
    public Message mUserCompleteMsg;
    private HashMap<Integer, String> mWhatToStringMap;

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
            boolean z = false;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK && !HwVSimUtilsInner.isChinaTelecom()) {
                return false;
            }
            if (!HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode == CLG_MODE) {
                    z = true;
                }
                return z;
            } else if (mode != HISI_CGUL_MODE) {
                return false;
            } else {
                if (HwVSimUtilsInner.isChinaTelecom()) {
                    return true;
                }
                if (cardType[mainSlot] == 2 || cardType[mainSlot] == 3) {
                    z = true;
                }
                return z;
            }
        }

        public static boolean isULGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            boolean z = false;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK) {
                if (HwVSimUtilsInner.isPlatformRealTripple()) {
                    return !HwVSimUtilsInner.isChinaTelecom();
                }
                if (!HwVSimUtilsInner.isChinaTelecom()) {
                    return true;
                }
            }
            if (!HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode == ULG_MODE) {
                    z = true;
                }
                return z;
            } else if (mode != HISI_CGUL_MODE) {
                return false;
            } else {
                if (cardType[mainSlot] == 1 || cardType[mainSlot] == 0) {
                    z = true;
                }
                return z;
            }
        }

        public static boolean isCGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            boolean z = false;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK && !HwVSimUtilsInner.isChinaTelecom()) {
                return false;
            }
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode == HISI_CG_MODE) {
                    z = true;
                }
                return z;
            }
            if (mode == CG_MODE) {
                z = true;
            }
            return z;
        }
    }

    private class InitialState extends State {
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
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logi("InitialState: what = " + HwVSimSlotSwitchController.this.getWhatToString(msg.what));
            int i = msg.what;
            if (i == 35) {
                HwVSimSlotSwitchController.this.loge("warning, initial time out");
                if (HwVSimSlotSwitchController.this.mMainSlot == -1) {
                    int unused = HwVSimSlotSwitchController.this.mMainSlot = 0;
                    HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
                    hwVSimSlotSwitchController2.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
                }
                checkSwitchSlotDone();
                return true;
            } else if (i != 83) {
                switch (i) {
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
                    default:
                        HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
                        hwVSimSlotSwitchController3.logi("InitialState: not handled msg.what = " + msg.what);
                        return false;
                }
            } else {
                onAvailableInitial(msg);
                return true;
            }
        }

        private void onAvailableInitial(Message msg) {
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= HwVSimSlotSwitchController.this.mCis.length) {
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.logd("InitialState: Invalid index : " + index + " received with event " + msg.what);
                return;
            }
            HwVSimSlotSwitchController.this.mCis[index.intValue()].getBalongSim(HwVSimSlotSwitchController.this.obtainMessage(2, index));
        }

        private void onGetSimSlotDone(Message msg) {
            HwVSimSlotSwitchController.this.logd("onGetSimSlotDone");
            if (HwVSimSlotSwitchController.this.mGotSimSlot) {
                HwVSimSlotSwitchController.this.logd("onGetSimSlotDone, mGotSimSlot done");
                return;
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("ar null");
                return;
            }
            if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == 3) {
                onGetTriSimSlotDone((int[]) ar.result);
            } else if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == 2) {
                onGetDualSimSlotDone((int[]) ar.result);
            } else {
                HwVSimSlotSwitchController.this.logd("onGetSimSlotDone got error");
                if (HwVSimSlotSwitchController.this.mMainSlot == -1) {
                    int unused = HwVSimSlotSwitchController.this.mMainSlot = 0;
                    HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                    hwVSimSlotSwitchController.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
                }
            }
        }

        private void onGetTriSimSlotDone(int[] slots) {
            HwVSimSlotSwitchController.this.logd("onGetTriSimSlotDone");
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logd("result = " + Arrays.toString(slots));
            if (slots[0] == 0 && slots[1] == 1 && slots[2] == 2) {
                int unused = HwVSimSlotSwitchController.this.mMainSlot = 0;
                boolean unused2 = HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 1 && slots[1] == 0 && slots[2] == 2) {
                int unused3 = HwVSimSlotSwitchController.this.mMainSlot = 1;
                boolean unused4 = HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 2 && slots[1] == 1 && slots[2] == 0) {
                int unused5 = HwVSimSlotSwitchController.this.mMainSlot = 0;
                boolean unused6 = HwVSimSlotSwitchController.this.mIsVSimOn = true;
            } else if (slots[0] == 2 && slots[1] == 0 && slots[2] == 1) {
                int unused7 = HwVSimSlotSwitchController.this.mMainSlot = 1;
                boolean unused8 = HwVSimSlotSwitchController.this.mIsVSimOn = true;
            } else {
                int unused9 = HwVSimSlotSwitchController.this.mMainSlot = 0;
            }
            HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController2.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController3.logd("mIsVSimOn = " + HwVSimSlotSwitchController.this.mIsVSimOn);
            boolean unused10 = HwVSimSlotSwitchController.this.mGotSimSlot = true;
            HwVSimController.getInstance().setSimSlotTable(slots);
            getAllRatCombineMode();
        }

        private void onGetDualSimSlotDone(int[] slots) {
            HwVSimSlotSwitchController.this.logd("onGetDualSimSlotDone");
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logd("result = " + Arrays.toString(slots));
            int modem2 = 2;
            if (slots[0] == 0 && slots[1] == 1) {
                int unused = HwVSimSlotSwitchController.this.mMainSlot = 0;
                boolean unused2 = HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 1 && slots[1] == 0) {
                int unused3 = HwVSimSlotSwitchController.this.mMainSlot = 1;
                boolean unused4 = HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 2 && slots[1] == 0) {
                int unused5 = HwVSimSlotSwitchController.this.mMainSlot = 1;
                boolean unused6 = HwVSimSlotSwitchController.this.mIsVSimOn = true;
                modem2 = HwVSimSlotSwitchController.this.mMainSlot;
            } else if (slots[0] == 2 && slots[1] == 1) {
                int unused7 = HwVSimSlotSwitchController.this.mMainSlot = 0;
                boolean unused8 = HwVSimSlotSwitchController.this.mIsVSimOn = true;
                modem2 = HwVSimSlotSwitchController.this.mMainSlot;
            }
            int[] slotsTable = HwVSimUtilsInner.createSimSlotsTable(slots[0], slots[1], modem2);
            HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController2.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController3.logd("mIsVSimOn = " + HwVSimSlotSwitchController.this.mIsVSimOn);
            boolean unused9 = HwVSimSlotSwitchController.this.mGotSimSlot = true;
            HwVSimController.getInstance().setSimSlotTable(slotsTable);
            getAllRatCombineMode();
        }

        private void onGetSimStateDone(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
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
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.logd("[2Cards]simIndex= " + simIndex + ", simEnable= " + simEnable);
                HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController2.logd("[2Cards]simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (11 != simIndex || !HwVSimUtilsInner.isRadioAvailable(2)) {
                    boolean unused = HwVSimSlotSwitchController.this.mIsVSimOn = false;
                } else {
                    boolean unused2 = HwVSimSlotSwitchController.this.mIsVSimOn = true;
                }
                HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController3.logd("mIsVSimOn = " + HwVSimSlotSwitchController.this.mIsVSimOn);
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
            }
        }

        private void checkVSimEnabledStatus() {
            HwVSimSlotSwitchController.this.logi("checkVSimEnabledStatus");
            if (HwVSimSlotSwitchController.sIsPlatformSupportVSim) {
                boolean unused = HwVSimSlotSwitchController.this.mIsVSimEnabled = HwVSimController.getInstance().isVSimEnabled();
            }
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logi("mIsVSimEnabled = " + HwVSimSlotSwitchController.this.mIsVSimEnabled);
        }

        private void getAllRatCombineMode() {
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                HwVSimSlotSwitchController.this.logi("x mode, done");
                checkSwitchSlotDone();
            }
        }

        private void checkSwitchSlotDone() {
            HwVSimSlotSwitchController.this.logd("checkSwitchSlotDone");
            boolean unused = HwVSimSlotSwitchController.this.mSlotSwitchDone = true;
            if (HwVSimSlotSwitchController.this.mIsVSimEnabled) {
                HwVSimSlotSwitchController.this.sendInitDone();
                HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mDefaultState);
                return;
            }
            checkSimMode();
        }

        private void checkSimMode() {
            int balongSlot = HwVSimSlotSwitchController.this.mIsVSimOn ? 2 : HwVSimSlotSwitchController.this.mMainSlot;
            HwVSimSlotSwitchController.this.mCis[balongSlot].getSimState(HwVSimSlotSwitchController.this.obtainMessage(3, balongSlot));
        }

        private void onCheckSimMode(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
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
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.logd("simIndex= " + simIndex + ", simEnable= " + simEnable);
                HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController2.logd("simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (11 != simIndex || HwVSimSlotSwitchController.this.mIsVSimEnabled) {
                    checkSimModeDone();
                } else {
                    int balongSlot = HwVSimSlotSwitchController.this.mIsVSimOn ? 2 : HwVSimSlotSwitchController.this.mMainSlot;
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
            HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mIsVSimOn ? 2 : HwVSimSlotSwitchController.this.mMainSlot].setSimState(1, 1, null);
            checkSimModeDone();
        }
    }

    private class SlotSwitchDefaultState extends State {
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
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logd("DefaultState: what = " + HwVSimSlotSwitchController.this.getWhatToString(msg.what));
            int i = msg.what;
            if (i == 11) {
                HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mSwitchCommrilModeState);
            } else if (i != HwVSimSlotSwitchController.EVENT_RADIO_POWER_OFF_DONE) {
                HwVSimSlotSwitchController.this.unhandledMessage(msg);
            } else {
                HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController2.logd("DefaultState: EVENT_RADIO_POWER_OFF_DONE index:" + index);
                HwVSimSlotSwitchController.this.onRadioPowerOffDone(index.intValue());
            }
            return true;
        }
    }

    private class SwitchCommrilModeState extends State {
        private SwitchCommrilModeState() {
        }

        public void enter() {
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: enter");
            int i = 0;
            int unused = HwVSimSlotSwitchController.this.mPollingCount = 0;
            while (true) {
                int i2 = i;
                if (i2 >= HwVSimSlotSwitchController.SUB_COUNT) {
                    break;
                }
                HwVSimSlotSwitchController.this.mCis[i2].registerForAvailable(HwVSimSlotSwitchController.this.getHandler(), 83, Integer.valueOf(i2));
                i = i2 + 1;
            }
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logi("SwitchCommrilModeState: mExpectSlot : " + HwVSimSlotSwitchController.this.mExpectSlot);
            HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController2.logi("SwitchCommrilModeState: mMainSlot : " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController3.logi("SwitchCommrilModeState: mIsVSimOn : " + HwVSimSlotSwitchController.this.mIsVSimOn);
            if (isNeedSwitchSlot()) {
                Message onCompleted = HwVSimSlotSwitchController.this.obtainMessage(25, Integer.valueOf(HwVSimSlotSwitchController.this.mMainSlot));
                if (HwVSimUtils.isPlatformTwoModems()) {
                    HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].switchBalongSim(HwVSimSlotSwitchController.this.mExpectSlot + 1, HwVSimSlotSwitchController.this.mMainSlot + 1, onCompleted);
                } else if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                    HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].switchBalongSim(2, HwVSimSlotSwitchController.this.mMainSlot, HwVSimSlotSwitchController.this.mExpectSlot, onCompleted);
                } else {
                    HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mMainSlot].switchBalongSim(HwVSimSlotSwitchController.this.mExpectSlot, HwVSimSlotSwitchController.this.mMainSlot, 2, onCompleted);
                }
                int unused2 = HwVSimSlotSwitchController.this.mPollingCount = HwVSimSlotSwitchController.this.mPollingCount + 1;
                HwVSimSlotSwitchController hwVSimSlotSwitchController4 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController4.logi("mPollingCount = " + HwVSimSlotSwitchController.this.mPollingCount);
                return;
            }
            HwVSimSlotSwitchController.this.processSwitchCommrilMode();
        }

        public void exit() {
            HwVSimSlotSwitchController.this.logi("SwitchCommrilModeState: exit");
            int i = 0;
            boolean unused = HwVSimSlotSwitchController.this.mSwitchingCommrilMode = false;
            while (true) {
                int i2 = i;
                if (i2 < HwVSimSlotSwitchController.SUB_COUNT) {
                    HwVSimSlotSwitchController.this.mCis[i2].unregisterForAvailable(HwVSimSlotSwitchController.this.getHandler());
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= HwVSimSlotSwitchController.this.mCis.length) {
                AsyncResult ar = HwVSimSlotSwitchController.this;
                ar.logd("SwitchCommrilModeState: Invalid index : " + index + " received with event " + msg.what);
                return true;
            }
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logi("SwitchCommrilModeState: what = " + msg.what + ", message = " + HwVSimSlotSwitchController.this.getWhatToString(msg.what) + ", on index " + index);
            AsyncResult ar2 = (AsyncResult) msg.obj;
            int i = msg.what;
            if (i == 15) {
                if (ar2 == null || ar2.exception != null) {
                    HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
                    hwVSimSlotSwitchController2.loge("Error! get message " + HwVSimSlotSwitchController.this.getWhatToString(msg.what));
                } else {
                    HwVSimSlotSwitchController.access$1610(HwVSimSlotSwitchController.this);
                }
                HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController3.logi("mPollingCount = " + HwVSimSlotSwitchController.this.mPollingCount);
                if (HwVSimSlotSwitchController.this.mPollingCount == 0) {
                    restartRild();
                }
            } else if (i == 25) {
                onSwitchSlotDone(ar2);
            } else if (i != 83) {
                HwVSimSlotSwitchController hwVSimSlotSwitchController4 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController4.logi("SwitchCommrilModeState: not handled msg.what = " + msg.what);
                retVal = false;
            } else {
                onRadioAvailable(index.intValue());
            }
            return retVal;
        }

        private void onSwitchSlotDone(AsyncResult ar) {
            if (ar.exception == null) {
                HwVSimSlotSwitchController.this.logi("Switch Sim Slot ok");
                HwVSimSlotSwitchController.access$1610(HwVSimSlotSwitchController.this);
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.logi("mPollingCount = " + HwVSimSlotSwitchController.this.mPollingCount);
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
            HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController2.loge("exception " + ar.exception);
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
                int i = 0;
                if (HwVSimSlotSwitchController.this.mIsVSimOn) {
                    if (HwVSimSlotSwitchController.this.mRadioAvailable[HwVSimSlotSwitchController.SUB_VSIM]) {
                        HwVSimSlotSwitchController.this.logi("Expect Slot mRadioAvailable[" + HwVSimSlotSwitchController.this.mExpectSlot + "] " + HwVSimSlotSwitchController.this.mRadioAvailable[HwVSimSlotSwitchController.this.mExpectSlot] + " --> true");
                        HwVSimSlotSwitchController.this.mRadioAvailable[HwVSimSlotSwitchController.this.mExpectSlot] = true;
                    }
                    while (true) {
                        int i2 = i;
                        if (i2 >= HwVSimSlotSwitchController.SUB_COUNT) {
                            break;
                        } else if (!HwVSimSlotSwitchController.this.mRadioAvailable[i2]) {
                            allDone = false;
                            break;
                        } else {
                            i = i2 + 1;
                        }
                    }
                } else {
                    for (int i3 = 0; i3 < HwVSimSlotSwitchController.PHONE_COUNT; i3++) {
                        HwVSimSlotSwitchController.this.logd("mRadioAvailable[" + i3 + "] = " + HwVSimSlotSwitchController.this.mRadioAvailable[i3]);
                    }
                    while (true) {
                        int i4 = i;
                        if (i4 >= HwVSimSlotSwitchController.PHONE_COUNT) {
                            break;
                        } else if (!HwVSimSlotSwitchController.this.mRadioAvailable[i4]) {
                            allDone = false;
                            break;
                        } else {
                            i = i4 + 1;
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
                        Message unused = HwVSimSlotSwitchController.this.mCompleteMsg = null;
                        if (isNeedSwitchSlot()) {
                            HwVSimSlotSwitchController.this.logi("update mainSlot to " + HwVSimSlotSwitchController.this.mExpectSlot);
                            int unused2 = HwVSimSlotSwitchController.this.mMainSlot = HwVSimSlotSwitchController.this.mExpectSlot;
                            HwVSimSlotSwitchController.this.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
                        }
                        if (HwVSimSlotSwitchController.this.mUserCompleteMsg != null) {
                            HwVSimSlotSwitchController.this.logi("switchCommrilMode ------>>> end");
                            AsyncResult.forMessage(HwVSimSlotSwitchController.this.mUserCompleteMsg, true, null);
                            HwVSimSlotSwitchController.this.mUserCompleteMsg.sendToTarget();
                            Message unused3 = HwVSimSlotSwitchController.this.mUserCompleteMsg = null;
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
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.logi("setCommrilMode to " + HwVSimSlotSwitchController.this.mExpectCommrilMode);
                HwVSimSlotSwitchController.this.setCommrilMode(HwVSimSlotSwitchController.this.mExpectCommrilMode);
                CommrilMode unused = HwVSimSlotSwitchController.this.mExpectCommrilMode = CommrilMode.NON_MODE;
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

    static /* synthetic */ int access$1610(HwVSimSlotSwitchController x0) {
        int i = x0.mPollingCount;
        x0.mPollingCount = i - 1;
        return i;
    }

    public static void create(Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        slogd("create");
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new HwVSimSlotSwitchController(context, vsimCi, cis);
                sInstance.start();
            } else {
                throw new RuntimeException("VSimSlotSwitchController already created");
            }
        }
    }

    public static HwVSimSlotSwitchController getInstance() {
        HwVSimSlotSwitchController hwVSimSlotSwitchController;
        synchronized (sLock) {
            if (sInstance != null) {
                hwVSimSlotSwitchController = sInstance;
            } else {
                throw new RuntimeException("VSimSlotSwitchController not yet created");
            }
        }
        return hwVSimSlotSwitchController;
    }

    private HwVSimSlotSwitchController(Context c, CommandsInterface vsimCi, CommandsInterface[] cis) {
        super(LOG_TAG, Looper.myLooper());
        logd("VSimSlotSwitchController");
        logi("IS_SUPPORT_FULL_NETWORK: " + IS_SUPPORT_FULL_NETWORK);
        logi("IS_HISI_CDMA_SUPPORTED: " + IS_HISI_CDMA_SUPPORTED);
        this.mCis = new CommandsInterface[(cis.length + 1)];
        for (int i = 0; i < cis.length; i++) {
            this.mCis[i] = cis[i];
        }
        this.mCis[cis.length] = vsimCi;
        initWhatToStringMap();
        this.mSlotSwitchDone = false;
        this.mGotSimSlot = false;
        this.mRadioAvailable = new boolean[SUB_COUNT];
        this.mRadioPowerStatus = new boolean[SUB_COUNT];
        for (int i2 = 0; i2 < SUB_COUNT; i2++) {
            this.mRadioAvailable[i2] = false;
            this.mRadioPowerStatus[i2] = false;
        }
        this.mMainSlot = -1;
        this.mExpectSlot = -1;
        this.mExpectCommrilMode = CommrilMode.NON_MODE;
        this.mIsVSimOn = false;
        this.mIsVSimEnabled = false;
        this.mInitDoneSent = false;
        this.mSwitchingCommrilMode = false;
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mSwitchCommrilModeState, this.mDefaultState);
        setInitialState(this.mInitialState);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private static void slogd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        HwVSimLog.VSimLogI(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        HwVSimLog.VSimLogE(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        String result = null;
        if (this.mWhatToStringMap != null) {
            result = this.mWhatToStringMap.get(Integer.valueOf(what));
        }
        if (result != null) {
            return result;
        }
        return "<unknown message> - " + what;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        HwVSimSlotSwitchController.super.dump(fd, pw, args);
        pw.println(" dummy");
    }

    private void initWhatToStringMap() {
        this.mWhatToStringMap = new HashMap<>();
        this.mWhatToStringMap.put(2, "EVENT_GET_SIM_SLOT_DONE");
        this.mWhatToStringMap.put(3, "EVENT_GET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(4, "EVENT_SET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(11, "CMD_SWITCH_COMMRIL_MODE");
        this.mWhatToStringMap.put(12, "EVENT_SWITCH_COMMRIL_MODE_DONE");
        this.mWhatToStringMap.put(15, "EVENT_SET_CDMA_MODE_SIDE_DONE");
        this.mWhatToStringMap.put(83, "EVENT_RADIO_AVAILABLE");
        this.mWhatToStringMap.put(25, "EVENT_SWITCH_SLOT_DONE");
        this.mWhatToStringMap.put(35, "EVENT_INITIAL_TIMEOUT");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_RADIO_POWER_OFF_DONE), "EVENT_RADIO_POWER_OFF_DONE");
    }

    /* access modifiers changed from: private */
    public void sendInitDone() {
        if (!this.mInitDoneSent) {
            HwVSimController.getInstance().getHandler().sendEmptyMessage(5);
            this.mInitDoneSent = true;
        }
    }

    public CommrilMode getCommrilMode() {
        String mode = SystemProperties.get("persist.radio.commril_mode", "HISI_CGUL_MODE");
        CommrilMode result = CommrilMode.NON_MODE;
        try {
            return (CommrilMode) Enum.valueOf(CommrilMode.class, mode);
        } catch (IllegalArgumentException e) {
            logd("getCommrilMode, IllegalArgumentException, mode = " + mode);
            return result;
        }
    }

    /* access modifiers changed from: private */
    public void setCommrilMode(CommrilMode mode) {
        SystemProperties.set("persist.radio.commril_mode", mode.toString());
    }

    public CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        CommrilMode expectCommrilMode;
        CommrilMode expectCommrilMode2 = CommrilMode.NON_MODE;
        if (mainSlot == -1) {
            logd("main slot invalid");
            return expectCommrilMode2;
        }
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (cardType[mainSlot] == 2 || cardType[mainSlot] == 3) {
            expectCommrilMode = IS_HISI_CDMA_SUPPORTED ? CommrilMode.HISI_CGUL_MODE : CommrilMode.CLG_MODE;
        } else if ((cardType[mainSlot] == 1 || cardType[mainSlot] == 0) && (cardType[slaveSlot] == 2 || cardType[slaveSlot] == 3)) {
            expectCommrilMode = IS_HISI_CDMA_SUPPORTED ? CommrilMode.HISI_CG_MODE : CommrilMode.CG_MODE;
        } else if (cardType[mainSlot] == 1 || cardType[slaveSlot] == 1) {
            expectCommrilMode = IS_HISI_CDMA_SUPPORTED ? CommrilMode.HISI_CGUL_MODE : CommrilMode.ULG_MODE;
        } else {
            expectCommrilMode = CommrilMode.NON_MODE;
        }
        logd("[getExpectCommrilMode]: expectCommrilMode = " + expectCommrilMode);
        return expectCommrilMode;
    }

    private void switchCommrilMode() {
        logd("switchCommrilMode");
        this.mCompleteMsg = obtainMessage(12, 0);
        Message msg = obtainMessage(11, 0);
        AsyncResult.forMessage(msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void processSwitchCommrilMode() {
        CommrilMode newCommrilMode = this.mExpectCommrilMode;
        int mainSlot = this.mMainSlot;
        logi("[switchCommrilMode]: newCommrilMode = " + newCommrilMode + ", mainSlot = " + mainSlot + ", is vsim on = " + this.mIsVSimOn);
        if (mainSlot < 0 || mainSlot >= this.mCis.length) {
            logi("main slot invalid");
            return;
        }
        if (mainSlot == 0) {
        }
        int balongSlot = this.mIsVSimOn ? 2 : mainSlot;
        switch (newCommrilMode) {
            case HISI_CGUL_MODE:
                this.mCis[balongSlot].setCdmaModeSide(0, obtainMessage(15));
                this.mPollingCount++;
                logi("[switchCommrilMode]: Send set HISI_CGUL_MODE request done...");
                break;
            case HISI_CG_MODE:
                this.mCis[balongSlot].setCdmaModeSide(1, obtainMessage(15));
                this.mPollingCount++;
                logi("[switchCommrilMode]: Send set HISI_CG_MODE request done...");
                break;
            case HISI_VSIM_MODE:
                this.mCis[balongSlot].setCdmaModeSide(2, obtainMessage(15));
                this.mPollingCount++;
                logi("[switchCommrilMode]: Send set HISI_VSIM_MODE request done...");
                break;
            default:
                loge("[switchCommrilMode]: Error!! Shouldn't enter here!!");
                break;
        }
    }

    public void switchCommrilMode(CommrilMode expectCommrilMode, int expectSlot, int mainSlot, boolean isVSimOn, Message onCompleteMsg) {
        logi("switchCommrilMode ------>>> begin");
        if (IS_SUPPORT_FULL_NETWORK || HwVSimUtilsInner.isChinaTelecom()) {
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
            AsyncResult.forMessage(onCompleteMsg, false, null);
            onCompleteMsg.sendToTarget();
        }
    }

    private void setAllRaidoPowerOff() {
        HwVSimController mVSimController = HwVSimController.getInstance();
        mVSimController.setIsWaitingSwitchCdmaModeSide(true);
        for (int subId = 0; subId < this.mCis.length; subId++) {
            this.mRadioPowerStatus[subId] = this.mCis[subId].getRadioState().isOn();
        }
        for (int subId2 = 0; subId2 < this.mCis.length; subId2++) {
            if (this.mRadioPowerStatus[subId2]) {
                logd("setAllRaidoPowerOff:mRadioPowerOnStatus[" + subId2 + "]=" + this.mRadioPowerStatus[subId2] + " -> off");
                Message onCompleted = obtainMessage(EVENT_RADIO_POWER_OFF_DONE, Integer.valueOf(subId2));
                mVSimController.getPhoneBySub(subId2).getServiceStateTracker().setDesiredPowerState(false);
                this.mCis[subId2].setRadioPower(false, onCompleted);
            } else {
                logd("setAllRaidoPowerOff:mRadioPowerOnStatus[" + subId2 + "]=" + this.mRadioPowerStatus[subId2] + " is off");
                onRadioPowerOffDone(subId2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onRadioPowerOffDone(int subId) {
        int i = 0;
        this.mRadioPowerStatus[subId] = false;
        logd("onRadioPowerOffDone:mRadioPowerStatus[" + subId + "]=" + this.mRadioPowerStatus[subId]);
        boolean isAllRadioPowerOff = true;
        while (true) {
            if (i >= this.mCis.length) {
                break;
            } else if (this.mRadioPowerStatus[i]) {
                isAllRadioPowerOff = false;
                break;
            } else {
                i++;
            }
        }
        if (isAllRadioPowerOff && !this.mSwitchingCommrilMode) {
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
                mVSimController.getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(true);
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
        HwVSimController.getInstance().setIsWaitingNvMatchUnsol(false);
        HwVSimNvMatchController.getInstance().storeIfNeedRestartRildForNvMatch(false);
    }

    public static boolean isCDMACard(int cardtype) {
        return cardtype == 2 || cardtype == 3;
    }

    /* access modifiers changed from: package-private */
    public CommrilMode getVSimOnCommrilMode(int mainSlot, int[] cardTypes) {
        CommrilMode vSimOnCommrilMode;
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        boolean mainSlotIsCDMACard = isCDMACard(cardTypes[mainSlot]);
        boolean slaveSlotIsCDMACard = isCDMACard(cardTypes[slaveSlot]);
        if (mainSlotIsCDMACard && slaveSlotIsCDMACard) {
            vSimOnCommrilMode = CommrilMode.HISI_CG_MODE;
        } else if (mainSlotIsCDMACard) {
            vSimOnCommrilMode = CommrilMode.HISI_VSIM_MODE;
        } else if (slaveSlotIsCDMACard) {
            vSimOnCommrilMode = CommrilMode.HISI_CG_MODE;
        } else {
            vSimOnCommrilMode = getCommrilMode();
            logd("no c-card, not change commril mode. vSimOnCommrilMode = " + vSimOnCommrilMode);
        }
        logd("getVSimOnCommrilMode: mainSlot = " + mainSlot + ", cardTypes = " + Arrays.toString(cardTypes) + ", mode = " + vSimOnCommrilMode);
        return vSimOnCommrilMode;
    }
}
