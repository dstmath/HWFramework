package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
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
    private static final int EVENT_RADIO_AVAILABLE = 17;
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
    public static final boolean IS_FAST_SWITCH_SIMSLOT = false;
    private static final boolean IS_HISI_CDMA_SUPPORTED = false;
    private static final boolean IS_SUPPORT_FULL_NETWORK = false;
    private static final String LOG_TAG = "VSimSwitchController";
    private static final int MODEM0 = 0;
    private static final int MODEM1 = 1;
    private static final int NOT_COMBINE = 1;
    private static final int PHONE_COUNT = 0;
    private static final String PROPERTY_CG_STANDBY_MODE = "persist.radio.cg_standby_mode";
    private static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    private static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    private static final String PROPERTY_HISI_CDMA_SUPPORTED = "ro.config.hisi_cdma_supported";
    private static final int RF0 = 0;
    private static final int RF1 = 1;
    private static final int SUB_COUNT = 0;
    private static final int SUB_VSIM = 0;
    private static HwVSimSlotSwitchController sInstance;
    private static final boolean sIsPlatformSupportVSim = false;
    private static final Object sLock = null;
    private CommandsInterface[] mCis;
    private Message mCompleteMsg;
    private Context mContext;
    private SlotSwitchDefaultState mDefaultState;
    private CommrilMode mExpectCommrilMode;
    private int mExpectSlot;
    private boolean[] mGotRatCombineMode;
    private boolean mGotSimSlot;
    private boolean mInitDoneSent;
    private InitialState mInitialState;
    private boolean mIsVSimEnabled;
    private boolean mIsVSimOn;
    private int mMainSlot;
    private boolean mNeedSwitchCommrilMode;
    private int mPollingCount;
    private boolean[] mRadioAvailable;
    private boolean[] mRadioPowerStatus;
    private int[] mRatCombineMode;
    private boolean mSlotSwitchDone;
    private SwitchCommrilModeState mSwitchCommrilModeState;
    private Message mUserCompleteMsg;
    private HashMap<Integer, String> mWhatToStringMap;
    private boolean[] switchSlotDoneMark;

    public enum CommrilMode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode.<clinit>():void");
        }

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
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK && !HwVSimUtilsInner.isChinaTelecom()) {
                return HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            }
            if (!HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode != CLG_MODE) {
                    z = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                }
                return z;
            } else if (mode != HISI_CGUL_MODE) {
                return HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            } else {
                if (HwVSimUtilsInner.isChinaTelecom()) {
                    return true;
                }
                if (!(cardType[mainSlot] == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE || cardType[mainSlot] == HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE)) {
                    z = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                }
                return z;
            }
        }

        public static boolean isULGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            boolean z = true;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK) {
                if (HwVSimUtilsInner.isPlatformRealTripple()) {
                    return HwVSimUtilsInner.isChinaTelecom() ? HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK : true;
                } else {
                    if (!HwVSimUtilsInner.isChinaTelecom()) {
                        return true;
                    }
                }
            }
            if (!HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode != ULG_MODE) {
                    z = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                }
                return z;
            } else if (mode != HISI_CGUL_MODE) {
                return HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            } else {
                if (!(cardType[mainSlot] == HwVSimSlotSwitchController.RF1 || cardType[mainSlot] == 0)) {
                    z = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                }
                return z;
            }
        }

        public static boolean isCGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            boolean z = true;
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK && !HwVSimUtilsInner.isChinaTelecom()) {
                return HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            }
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                if (mode != HISI_CG_MODE) {
                    z = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                }
                return z;
            }
            if (mode != CG_MODE) {
                z = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            }
            return z;
        }
    }

    private class InitialState extends State {
        private static final /* synthetic */ int[] -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$vsim$HwVSimSlotSwitchController$CommrilMode;
        final /* synthetic */ HwVSimSlotSwitchController this$0;

        private static /* synthetic */ int[] -getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues() {
            if (-com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues != null) {
                return -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues;
            }
            int[] iArr = new int[CommrilMode.values().length];
            try {
                iArr[CommrilMode.CG_MODE.ordinal()] = HwVSimSlotSwitchController.RF1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CommrilMode.CLG_MODE.ordinal()] = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CommrilMode.HISI_CGUL_MODE.ordinal()] = HwVSimSlotSwitchController.EVENT_SET_SIM_STATE_DONE;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CommrilMode.HISI_CG_MODE.ordinal()] = 5;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CommrilMode.NON_MODE.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CommrilMode.SVLTE_MODE.ordinal()] = 7;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CommrilMode.ULG_MODE.ordinal()] = HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE;
            } catch (NoSuchFieldError e7) {
            }
            -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues = iArr;
            return iArr;
        }

        /* synthetic */ InitialState(HwVSimSlotSwitchController this$0, InitialState initialState) {
            this(this$0);
        }

        private InitialState(HwVSimSlotSwitchController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("InitialState: enter");
            checkVSimEnabledStatus();
            for (int i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.SUB_COUNT; i += HwVSimSlotSwitchController.RF1) {
                this.this$0.mCis[i].registerForAvailable(this.this$0.getHandler(), HwVSimSlotSwitchController.EVENT_RADIO_AVAILABLE, Integer.valueOf(i));
            }
            if (this.this$0.getHandler() != null) {
                this.this$0.getHandler().sendEmptyMessageDelayed(HwVSimSlotSwitchController.EVENT_INITIAL_TIMEOUT, HwVSimSlotSwitchController.INITIAL_TIMEOUT);
            }
        }

        public void exit() {
            this.this$0.logd("InitialState: exit");
            if (this.this$0.getHandler() != null) {
                this.this$0.getHandler().removeMessages(HwVSimSlotSwitchController.EVENT_INITIAL_TIMEOUT);
            }
            for (int i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.SUB_COUNT; i += HwVSimSlotSwitchController.RF1) {
                this.this$0.mCis[i].unregisterForAvailable(this.this$0.getHandler());
            }
        }

        public boolean processMessage(Message msg) {
            this.this$0.logi("InitialState: what = " + this.this$0.getWhatToString(msg.what));
            switch (msg.what) {
                case HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE /*2*/:
                    onGetSimSlotDone(msg);
                    return true;
                case HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE /*3*/:
                    if (this.this$0.mSlotSwitchDone) {
                        onCheckSimMode(msg);
                        return true;
                    }
                    onGetSimStateDone(msg);
                    return true;
                case HwVSimSlotSwitchController.EVENT_SET_SIM_STATE_DONE /*4*/:
                    onSetSimStateDone(msg);
                    return true;
                case HwVSimSlotSwitchController.EVENT_RADIO_AVAILABLE /*17*/:
                    onAvailableInitial(msg);
                    return true;
                case HwVSimSlotSwitchController.EVENT_SWITCH_SLOT_DONE /*25*/:
                    onSwithSlotDone(msg);
                    return true;
                case HwVSimSlotSwitchController.EVENT_GET_RAT_COMBINE_MODE_DONE /*34*/:
                    onGetRatCombineModeDone(msg);
                    return true;
                case HwVSimSlotSwitchController.EVENT_INITIAL_TIMEOUT /*35*/:
                    this.this$0.loge("warning, initial time out");
                    if (this.this$0.mMainSlot == HwVSimSlotSwitchController.INVALID_SIM_SLOT) {
                        this.this$0.mMainSlot = HwVSimSlotSwitchController.RF0;
                        this.this$0.logd("mMainSlot = " + this.this$0.mMainSlot);
                    }
                    checkSwitchSlotDone();
                    return true;
                default:
                    this.this$0.logi("InitialState: not handled msg.what = " + msg.what);
                    return HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            }
        }

        private void onAvailableInitial(Message msg) {
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= this.this$0.mCis.length) {
                this.this$0.logd("InitialState: Invalid index : " + index + " received with event " + msg.what);
            } else {
                this.this$0.mCis[index.intValue()].getBalongSim(this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE, index));
            }
        }

        private void onGetSimSlotDone(Message msg) {
            this.this$0.logd("onGetSimSlotDone");
            if (this.this$0.mGotSimSlot) {
                this.this$0.logd("onGetSimSlotDone, mGotSimSlot done");
                return;
            }
            AsyncResult ar = msg.obj;
            if (ar == null) {
                this.this$0.logd("ar null");
                return;
            }
            int[] slots;
            if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE) {
                slots = ar.result;
                this.this$0.logd("result = " + Arrays.toString(slots));
                if (slots[HwVSimSlotSwitchController.RF0] == 0 && slots[HwVSimSlotSwitchController.RF1] == HwVSimSlotSwitchController.RF1 && slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE) {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF0;
                    this.this$0.mIsVSimOn = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                } else if (slots[HwVSimSlotSwitchController.RF0] == HwVSimSlotSwitchController.RF1 && slots[HwVSimSlotSwitchController.RF1] == 0 && slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE) {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF1;
                    this.this$0.mIsVSimOn = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                } else if (slots[HwVSimSlotSwitchController.RF0] == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE && slots[HwVSimSlotSwitchController.RF1] == HwVSimSlotSwitchController.RF1 && slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] == 0) {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF0;
                    this.this$0.mIsVSimOn = true;
                } else if (slots[HwVSimSlotSwitchController.RF0] == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE && slots[HwVSimSlotSwitchController.RF1] == 0 && slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] == HwVSimSlotSwitchController.RF1) {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF1;
                    this.this$0.mIsVSimOn = true;
                } else {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF0;
                }
                this.this$0.logd("mMainSlot = " + this.this$0.mMainSlot);
                this.this$0.logd("mIsVSimOn = " + this.this$0.mIsVSimOn);
                this.this$0.mGotSimSlot = true;
                HwVSimController.getInstance().setSimSlotTable(slots);
                getAllRatCombineMode();
            } else if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE) {
                slots = (int[]) ar.result;
                this.this$0.logd("result = " + Arrays.toString(slots));
                if (slots[HwVSimSlotSwitchController.RF0] == HwVSimSlotSwitchController.RF1 && slots[HwVSimSlotSwitchController.RF1] == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE) {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF0;
                } else if (slots[HwVSimSlotSwitchController.RF0] == HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE && slots[HwVSimSlotSwitchController.RF1] == HwVSimSlotSwitchController.RF1) {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF1;
                } else {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF0;
                }
                this.this$0.logd("mMainSlot = " + this.this$0.mMainSlot);
                this.this$0.mGotSimSlot = true;
                if (this.this$0.mCis[this.this$0.mMainSlot].isRadioAvailable()) {
                    this.this$0.mCis[this.this$0.mMainSlot].getSimState(this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE, this.this$0.mMainSlot));
                } else {
                    this.this$0.logi("mainSlot " + this.this$0.mMainSlot + " is not available !");
                    this.this$0.mCis[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE].getSimState(this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE, HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE));
                }
            } else {
                this.this$0.logd("onGetSimSlotDone got error");
                if (this.this$0.mMainSlot == HwVSimSlotSwitchController.INVALID_SIM_SLOT) {
                    this.this$0.mMainSlot = HwVSimSlotSwitchController.RF0;
                    this.this$0.logd("mMainSlot = " + this.this$0.mMainSlot);
                }
            }
        }

        private void onGetSimStateDone(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                this.this$0.logd("onGetSimStateDone : ar null");
                return;
            }
            if (ar.exception != null || ar.result == null || ((int[]) ar.result).length <= HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE) {
                this.this$0.loge("onGetSimStateDone got error !");
            } else {
                int simIndex = ((int[]) ar.result)[HwVSimSlotSwitchController.RF0];
                int simEnable = ((int[]) ar.result)[HwVSimSlotSwitchController.RF1];
                int simSub = ((int[]) ar.result)[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE];
                int simNetinfo = ((int[]) ar.result)[HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE];
                int slaveSlot = this.this$0.mMainSlot == 0 ? HwVSimSlotSwitchController.RF1 : HwVSimSlotSwitchController.RF0;
                this.this$0.logd("[2Cards]simIndex= " + simIndex + ", simEnable= " + simEnable);
                this.this$0.logd("[2Cards]simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (HwVSimSlotSwitchController.CMD_SWITCH_COMMRIL_MODE == simIndex && this.this$0.mCis[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE].isRadioAvailable()) {
                    this.this$0.mIsVSimOn = true;
                } else {
                    this.this$0.mIsVSimOn = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                }
                this.this$0.logd("mIsVSimOn = " + this.this$0.mIsVSimOn);
                if (this.this$0.mIsVSimOn || !this.this$0.mCis[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE].isRadioAvailable()) {
                    int[] slots = new int[HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE];
                    if (this.this$0.mIsVSimOn) {
                        slots[HwVSimSlotSwitchController.RF0] = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
                        slots[HwVSimSlotSwitchController.RF1] = slaveSlot;
                        slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] = this.this$0.mMainSlot;
                    } else {
                        slots[HwVSimSlotSwitchController.RF0] = this.this$0.mMainSlot;
                        slots[HwVSimSlotSwitchController.RF1] = slaveSlot;
                        slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
                    }
                    HwVSimController.getInstance().setSimSlotTable(slots);
                    getAllRatCombineMode();
                } else {
                    this.this$0.logi("telephony is VSIM on, modem is VSIM off, change it first!");
                    this.this$0.mCis[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE].hotSwitchSimSlotFor2Modem(this.this$0.mMainSlot, slaveSlot, HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE, null);
                    this.this$0.switchSlotDoneMark[this.this$0.mMainSlot] = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                    this.this$0.mCis[this.this$0.mMainSlot].hotSwitchSimSlotFor2Modem(this.this$0.mMainSlot, slaveSlot, HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE, this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_SWITCH_SLOT_DONE, this.this$0.mMainSlot));
                    this.this$0.switchSlotDoneMark[slaveSlot] = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                    this.this$0.mCis[slaveSlot].hotSwitchSimSlotFor2Modem(this.this$0.mMainSlot, slaveSlot, HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE, this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_SWITCH_SLOT_DONE, slaveSlot));
                }
            }
        }

        private void onSwithSlotDone(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                this.this$0.logd("onSwithSlotDone : ar null");
            } else if (ar.exception != null) {
                this.this$0.loge("exception " + ar.exception);
            } else {
                Integer index = HwVSimUtilsInner.getCiIndex(msg);
                if (index.intValue() < 0 || index.intValue() >= this.this$0.mCis.length) {
                    this.this$0.logd("onSwithSlotDone: Invalid index : " + index);
                    return;
                }
                this.this$0.switchSlotDoneMark[index.intValue()] = true;
                int i = HwVSimSlotSwitchController.RF0;
                while (i < HwVSimSlotSwitchController.PHONE_COUNT) {
                    this.this$0.logi("switchSlotDoneMark[" + i + "] is: " + this.this$0.switchSlotDoneMark[i]);
                    if (this.this$0.switchSlotDoneMark[i]) {
                        i += HwVSimSlotSwitchController.RF1;
                    } else {
                        this.this$0.logi("switch slot not all ready");
                        return;
                    }
                }
                for (i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.PHONE_COUNT; i += HwVSimSlotSwitchController.RF1) {
                    this.this$0.switchSlotDoneMark[i] = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                }
                int[] slots = new int[HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE];
                int slaveSlot = this.this$0.mMainSlot == 0 ? HwVSimSlotSwitchController.RF1 : HwVSimSlotSwitchController.RF0;
                slots[HwVSimSlotSwitchController.RF0] = this.this$0.mMainSlot;
                slots[HwVSimSlotSwitchController.RF1] = slaveSlot;
                slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
                HwVSimController.getInstance().setSimSlotTable(slots);
                getAllRatCombineMode();
            }
        }

        private void checkVSimEnabledStatus() {
            this.this$0.logi("checkVSimEnabledStatus");
            if (HwVSimSlotSwitchController.sIsPlatformSupportVSim) {
                this.this$0.mIsVSimEnabled = HwVSimController.getInstance().isVSimEnabled();
            }
            this.this$0.logi("mIsVSimEnabled = " + this.this$0.mIsVSimEnabled);
        }

        private boolean checkRatCombineModeMatched(int mainSlot, CommrilMode mode) {
            int slaveSlot = mainSlot == 0 ? HwVSimSlotSwitchController.RF1 : HwVSimSlotSwitchController.RF0;
            this.this$0.logi("checkRatCombineModeMatched: CommrilMode = " + mode + ", mainSlot = " + mainSlot + ", ratCombineMode = " + this.this$0.mRatCombineMode[HwVSimSlotSwitchController.RF0] + ", " + this.this$0.mRatCombineMode[HwVSimSlotSwitchController.RF1] + ", " + this.this$0.mRatCombineMode[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE]);
            if (mainSlot < 0 || mainSlot > HwVSimSlotSwitchController.RF1) {
                this.this$0.logi("checkRatCombineModeMatched: SlotId is invalid, return true");
                return true;
            }
            int realMainSlot;
            if (HwVSimUtils.isPlatformTwoModems()) {
                if (this.this$0.mCis[mainSlot].isRadioAvailable() || this.this$0.mRatCombineMode[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] == HwVSimSlotSwitchController.INVALID_SIM_SLOT) {
                    realMainSlot = mainSlot;
                } else {
                    this.this$0.logi("checkRatCombineModeMatched: mainSlot is pending, using VSIM!");
                    realMainSlot = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
                }
            } else if (this.this$0.mIsVSimOn) {
                realMainSlot = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
            } else {
                realMainSlot = mainSlot;
            }
            if (this.this$0.mRatCombineMode[realMainSlot] == HwVSimSlotSwitchController.INVALID_SIM_SLOT || this.this$0.mRatCombineMode[slaveSlot] == HwVSimSlotSwitchController.INVALID_SIM_SLOT) {
                this.this$0.logi("checkRatCombineModeMatched: ratCombineMode is invalid, return true.");
                return true;
            }
            boolean result = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            switch (-getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues()[mode.ordinal()]) {
                case HwVSimSlotSwitchController.RF1 /*1*/:
                    String cg_standby_mode = SystemProperties.get(HwVSimSlotSwitchController.PROPERTY_CG_STANDBY_MODE, "home");
                    this.this$0.logi("cg_standby_mode = " + cg_standby_mode);
                    if (!"roam_gsm".equals(cg_standby_mode)) {
                        if (this.this$0.mRatCombineMode[slaveSlot] == 0 && this.this$0.mRatCombineMode[realMainSlot] == HwVSimSlotSwitchController.RF1) {
                            result = true;
                            break;
                        }
                    } else if (this.this$0.mRatCombineMode[realMainSlot] == HwVSimSlotSwitchController.RF1 && this.this$0.mRatCombineMode[slaveSlot] == HwVSimSlotSwitchController.RF1) {
                        result = true;
                        break;
                    }
                case HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE /*2*/:
                    result = true;
                    break;
                case HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE /*3*/:
                    if (this.this$0.mRatCombineMode[realMainSlot] == HwVSimSlotSwitchController.RF1 && this.this$0.mRatCombineMode[slaveSlot] == HwVSimSlotSwitchController.RF1) {
                        result = true;
                        break;
                    }
                default:
                    result = true;
                    break;
            }
            return result;
        }

        private void onGetRatCombineModeDone(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                this.this$0.logd("ar null");
                return;
            }
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= this.this$0.mCis.length) {
                this.this$0.logd("onGetRatCombineModeDone, Invalid index : " + index);
                return;
            }
            if (ar.exception != null || ar.result == null) {
                this.this$0.logi("warning, onGetRatCombineModeDone");
                this.this$0.mGotRatCombineMode[index.intValue()] = true;
            } else {
                this.this$0.mRatCombineMode[index.intValue()] = ((int[]) ar.result)[HwVSimSlotSwitchController.RF0];
                this.this$0.mGotRatCombineMode[index.intValue()] = true;
            }
            this.this$0.logi("mRatCombineMode[" + index + "] is " + this.this$0.mRatCombineMode[index.intValue()]);
            checkAndSwitchSlot();
        }

        private boolean isGotAllRatCombineMode() {
            boolean z = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            if (HwVSimUtils.isPlatformTwoModems()) {
                boolean result = true;
                int i = HwVSimSlotSwitchController.RF0;
                while (i < HwVSimSlotSwitchController.SUB_COUNT) {
                    if (this.this$0.mCis[i].isRadioAvailable() && !this.this$0.mGotRatCombineMode[i]) {
                        result = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                    }
                    i += HwVSimSlotSwitchController.RF1;
                }
                return result;
            }
            if (this.this$0.mGotRatCombineMode[HwVSimSlotSwitchController.RF0] && this.this$0.mGotRatCombineMode[HwVSimSlotSwitchController.RF1]) {
                z = this.this$0.mGotRatCombineMode[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE];
            }
            return z;
        }

        private void getAllRatCombineMode() {
            if (HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED) {
                this.this$0.logi("x mode, done");
                checkSwitchSlotDone();
                return;
            }
            for (int i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.SUB_COUNT; i += HwVSimSlotSwitchController.RF1) {
                Integer index = Integer.valueOf(i);
                this.this$0.mCis[index.intValue()].getHwRatCombineMode(this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_GET_RAT_COMBINE_MODE_DONE, index));
            }
        }

        private void checkAndSwitchSlot() {
            this.this$0.logi("checkAndSwitchSlot");
            this.this$0.logi("mGotSimSlot = " + this.this$0.mGotSimSlot + ", mGotRatCombineMode = " + Arrays.toString(this.this$0.mGotRatCombineMode));
            if (!this.this$0.mGotSimSlot || !isGotAllRatCombineMode()) {
                this.this$0.logi("check return");
            } else if (this.this$0.mIsVSimEnabled) {
                checkCommrilMode();
                dealSlotSwitchAndCommrilMode();
            } else {
                this.this$0.logi("vsim not enabled, done");
                checkSwitchSlotDone();
            }
        }

        private void checkCommrilMode() {
            this.this$0.logi("checkCommrilMode");
            if (HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK || HwVSimUtilsInner.isChinaTelecom()) {
                CommrilMode currentCommrilMode = this.this$0.getCommrilMode();
                this.this$0.logi("currentCommrilMode = " + currentCommrilMode);
                if (checkRatCombineModeMatched(this.this$0.mMainSlot, currentCommrilMode)) {
                    this.this$0.logi("rat combine mode match");
                    return;
                }
                this.this$0.mExpectCommrilMode = currentCommrilMode;
                this.this$0.mExpectSlot = this.this$0.mMainSlot;
                this.this$0.mNeedSwitchCommrilMode = true;
                this.this$0.logi("mNeedSwitchCommrilMode = " + this.this$0.mNeedSwitchCommrilMode + ", mExpectCommrilMode = " + this.this$0.mExpectCommrilMode + ", mExpectSlot = " + this.this$0.mExpectSlot);
                return;
            }
            this.this$0.logi("full net and telecom not support");
        }

        private void dealSlotSwitchAndCommrilMode() {
            this.this$0.logi("dealSlotSwitchAndCommrilMode");
            if (this.this$0.mNeedSwitchCommrilMode) {
                this.this$0.logi("switch comril mode");
                this.this$0.switchCommrilMode();
                return;
            }
            this.this$0.logi("nothing to do, done");
            checkSwitchSlotDone();
        }

        private void checkSwitchSlotDone() {
            this.this$0.logd("checkSwitchSlotDone");
            this.this$0.mSlotSwitchDone = true;
            if (this.this$0.mIsVSimEnabled) {
                this.this$0.sendInitDone();
                this.this$0.transitionTo(this.this$0.mDefaultState);
                return;
            }
            checkSimMode();
        }

        private void checkSimMode() {
            int balongSlot;
            int mainSlot = this.this$0.mMainSlot;
            if (this.this$0.mIsVSimOn) {
                balongSlot = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
            } else {
                balongSlot = mainSlot;
            }
            this.this$0.mCis[balongSlot].getSimState(this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE, balongSlot));
        }

        private void onCheckSimMode(Message msg) {
            AsyncResult ar = msg.obj;
            if (ar == null) {
                this.this$0.logd("onGetSimStateDone : ar null");
                return;
            }
            if (ar.exception != null || ar.result == null || ((int[]) ar.result).length <= HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE) {
                this.this$0.loge("onCheckSimMode got error !");
                checkSimModeDone();
            } else {
                int simIndex = ((int[]) ar.result)[HwVSimSlotSwitchController.RF0];
                int simEnable = ((int[]) ar.result)[HwVSimSlotSwitchController.RF1];
                int simSub = ((int[]) ar.result)[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE];
                int simNetinfo = ((int[]) ar.result)[HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE];
                this.this$0.logd("simIndex= " + simIndex + ", simEnable= " + simEnable);
                this.this$0.logd("simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (HwVSimSlotSwitchController.CMD_SWITCH_COMMRIL_MODE != simIndex || this.this$0.mIsVSimEnabled) {
                    checkSimModeDone();
                } else {
                    int balongSlot;
                    int mainSlot = this.this$0.mMainSlot;
                    if (this.this$0.mIsVSimOn) {
                        balongSlot = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
                    } else {
                        balongSlot = mainSlot;
                    }
                    this.this$0.mCis[balongSlot].setSimState(simIndex, HwVSimSlotSwitchController.RF0, this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_SET_SIM_STATE_DONE, balongSlot));
                }
            }
        }

        private void checkSimModeDone() {
            this.this$0.logd("checkSimModeDone");
            this.this$0.sendInitDone();
            this.this$0.transitionTo(this.this$0.mDefaultState);
        }

        private void onSetSimStateDone(Message msg) {
            int balongSlot;
            int mainSlot = this.this$0.mMainSlot;
            if (this.this$0.mIsVSimOn) {
                balongSlot = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
            } else {
                balongSlot = mainSlot;
            }
            this.this$0.mCis[balongSlot].setSimState(HwVSimSlotSwitchController.RF1, HwVSimSlotSwitchController.RF1, null);
            checkSimModeDone();
        }
    }

    private class SlotSwitchDefaultState extends State {
        final /* synthetic */ HwVSimSlotSwitchController this$0;

        /* synthetic */ SlotSwitchDefaultState(HwVSimSlotSwitchController this$0, SlotSwitchDefaultState slotSwitchDefaultState) {
            this(this$0);
        }

        private SlotSwitchDefaultState(HwVSimSlotSwitchController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("DefaultState: enter");
        }

        public void exit() {
            this.this$0.logd("DefaultState: exit");
        }

        public boolean processMessage(Message msg) {
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            this.this$0.logd("DefaultState: what = " + this.this$0.getWhatToString(msg.what));
            switch (msg.what) {
                case HwVSimSlotSwitchController.CMD_SWITCH_COMMRIL_MODE /*11*/:
                    this.this$0.transitionTo(this.this$0.mSwitchCommrilModeState);
                    break;
                case HwVSimSlotSwitchController.EVENT_RADIO_POWER_OFF_DONE /*36*/:
                    this.this$0.logd("DefaultState: EVENT_RADIO_POWER_OFF_DONE index:" + index);
                    this.this$0.onRadioPowerOffDone(index.intValue());
                    break;
                default:
                    this.this$0.unhandledMessage(msg);
                    break;
            }
            return true;
        }
    }

    private class SwitchCommrilModeState extends State {
        final /* synthetic */ HwVSimSlotSwitchController this$0;

        /* synthetic */ SwitchCommrilModeState(HwVSimSlotSwitchController this$0, SwitchCommrilModeState switchCommrilModeState) {
            this(this$0);
        }

        private SwitchCommrilModeState(HwVSimSlotSwitchController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logi("SwitchCommrilModeState: enter");
            this.this$0.mPollingCount = HwVSimSlotSwitchController.RF0;
            for (int i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.SUB_COUNT; i += HwVSimSlotSwitchController.RF1) {
                this.this$0.mCis[i].registerForAvailable(this.this$0.getHandler(), HwVSimSlotSwitchController.EVENT_RADIO_AVAILABLE, Integer.valueOf(i));
            }
            this.this$0.logi("SwitchCommrilModeState: mExpectSlot : " + this.this$0.mExpectSlot);
            this.this$0.logi("SwitchCommrilModeState: mMainSlot : " + this.this$0.mMainSlot);
            this.this$0.logi("SwitchCommrilModeState: mIsVSimOn : " + this.this$0.mIsVSimOn);
            if (isNeedSwitchSlot()) {
                Message onCompleted = this.this$0.obtainMessage(HwVSimSlotSwitchController.EVENT_SWITCH_SLOT_DONE, Integer.valueOf(this.this$0.mMainSlot));
                if (HwVSimUtils.isPlatformTwoModems()) {
                    this.this$0.mCis[this.this$0.mMainSlot].switchBalongSim(this.this$0.mExpectSlot + HwVSimSlotSwitchController.RF1, this.this$0.mMainSlot + HwVSimSlotSwitchController.RF1, onCompleted);
                } else if (this.this$0.mIsVSimOn) {
                    this.this$0.mCis[this.this$0.mMainSlot].switchBalongSim(HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE, this.this$0.mMainSlot, this.this$0.mExpectSlot, onCompleted);
                } else {
                    this.this$0.mCis[this.this$0.mMainSlot].switchBalongSim(this.this$0.mExpectSlot, this.this$0.mMainSlot, HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE, onCompleted);
                }
                HwVSimSlotSwitchController hwVSimSlotSwitchController = this.this$0;
                hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount + HwVSimSlotSwitchController.RF1;
                this.this$0.logi("mPollingCount = " + this.this$0.mPollingCount);
                return;
            }
            this.this$0.processSwitchCommrilMode();
        }

        public void exit() {
            this.this$0.logi("SwitchCommrilModeState: exit");
            for (int i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.SUB_COUNT; i += HwVSimSlotSwitchController.RF1) {
                this.this$0.mCis[i].unregisterForAvailable(this.this$0.getHandler());
            }
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            Integer index = HwVSimUtilsInner.getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= this.this$0.mCis.length) {
                this.this$0.logd("SwitchCommrilModeState: Invalid index : " + index + " received with event " + msg.what);
                return true;
            }
            this.this$0.logi("SwitchCommrilModeState: what = " + msg.what + ", message = " + this.this$0.getWhatToString(msg.what) + ", on index " + index);
            AsyncResult ar = msg.obj;
            HwVSimSlotSwitchController hwVSimSlotSwitchController;
            switch (msg.what) {
                case HwVSimSlotSwitchController.EVENT_SET_RAT_COMBINE_MODE_DONE /*13*/:
                    if (ar == null || ar.exception != null) {
                        this.this$0.loge("Error! setRatCommbie is failed!!");
                    } else {
                        hwVSimSlotSwitchController = this.this$0;
                        hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount + HwVSimSlotSwitchController.INVALID_SIM_SLOT;
                    }
                    this.this$0.logi("mPollingCount = " + this.this$0.mPollingCount);
                    if (this.this$0.mPollingCount == 0) {
                        restartRild();
                        break;
                    }
                    break;
                case HwVSimSlotSwitchController.EVENT_SWITCH_RFIC_CHANNEL_DONE /*14*/:
                    if (ar == null || ar.exception != null) {
                        this.this$0.loge("Error! switch rf channel is failed!!");
                    } else {
                        hwVSimSlotSwitchController = this.this$0;
                        hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount + HwVSimSlotSwitchController.INVALID_SIM_SLOT;
                    }
                    this.this$0.logi("mPollingCount = " + this.this$0.mPollingCount);
                    if (this.this$0.mPollingCount == 0) {
                        restartRild();
                        break;
                    }
                    break;
                case HwVSimSlotSwitchController.EVENT_SET_CDMA_MODE_SIDE_DONE /*15*/:
                    if (ar == null || ar.exception != null) {
                        this.this$0.loge("Error! setCdmaModeSide is failed!!");
                    } else {
                        hwVSimSlotSwitchController = this.this$0;
                        hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount + HwVSimSlotSwitchController.INVALID_SIM_SLOT;
                    }
                    this.this$0.logi("mPollingCount = " + this.this$0.mPollingCount);
                    if (this.this$0.mPollingCount == 0) {
                        restartRild();
                        break;
                    }
                    break;
                case HwVSimSlotSwitchController.EVENT_RADIO_AVAILABLE /*17*/:
                    this.this$0.logi("mPollingCount = " + this.this$0.mPollingCount);
                    if (this.this$0.mPollingCount == 0) {
                        this.this$0.mRadioAvailable[index.intValue()] = true;
                        this.this$0.logi("mRadioAvailable[" + index + "] = " + this.this$0.mRadioAvailable[index.intValue()]);
                        boolean allDone = true;
                        int i;
                        if (this.this$0.mIsVSimOn) {
                            if (this.this$0.mRadioAvailable[HwVSimSlotSwitchController.SUB_VSIM]) {
                                this.this$0.logi("Expect Slot mRadioAvailable[" + this.this$0.mExpectSlot + "] " + this.this$0.mRadioAvailable[this.this$0.mExpectSlot] + " --> true");
                                this.this$0.mRadioAvailable[this.this$0.mExpectSlot] = true;
                            }
                            i = HwVSimSlotSwitchController.RF0;
                            while (i < HwVSimSlotSwitchController.SUB_COUNT) {
                                if (this.this$0.mRadioAvailable[i]) {
                                    i += HwVSimSlotSwitchController.RF1;
                                } else {
                                    allDone = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                                }
                            }
                        } else {
                            for (i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.PHONE_COUNT; i += HwVSimSlotSwitchController.RF1) {
                                this.this$0.logd("mRadioAvailable[" + i + "] = " + this.this$0.mRadioAvailable[i]);
                            }
                            i = HwVSimSlotSwitchController.RF0;
                            while (i < HwVSimSlotSwitchController.PHONE_COUNT) {
                                if (this.this$0.mRadioAvailable[i]) {
                                    i += HwVSimSlotSwitchController.RF1;
                                } else {
                                    allDone = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                                }
                            }
                        }
                        if (!allDone) {
                            this.this$0.logd("not done");
                            break;
                        }
                        this.this$0.logd("all done");
                        this.this$0.logd("mCompleteMsg = " + this.this$0.mCompleteMsg);
                        if (this.this$0.mCompleteMsg != null) {
                            this.this$0.logi("Switch CommrilMode Done!!");
                            AsyncResult.forMessage(this.this$0.mCompleteMsg);
                            this.this$0.mCompleteMsg.sendToTarget();
                            this.this$0.mCompleteMsg = null;
                            if (isNeedSwitchSlot()) {
                                this.this$0.logi("update mainSlot to " + this.this$0.mExpectSlot);
                                this.this$0.mMainSlot = this.this$0.mExpectSlot;
                                this.this$0.logd("mMainSlot = " + this.this$0.mMainSlot);
                            }
                            if (this.this$0.mUserCompleteMsg != null) {
                                this.this$0.logi("switchCommrilMode ------>>> end");
                                AsyncResult.forMessage(this.this$0.mUserCompleteMsg, Boolean.valueOf(true), null);
                                this.this$0.mUserCompleteMsg.sendToTarget();
                                this.this$0.mUserCompleteMsg = null;
                            }
                            this.this$0.sendInitDone();
                            this.this$0.transitionTo(this.this$0.mDefaultState);
                            break;
                        }
                    }
                    break;
                case HwVSimSlotSwitchController.EVENT_SWITCH_SLOT_DONE /*25*/:
                    onSwitchSlotDone(ar);
                    break;
                default:
                    this.this$0.logi("SwitchCommrilModeState: not handled msg.what = " + msg.what);
                    retVal = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
                    break;
            }
            return retVal;
        }

        private void onSwitchSlotDone(AsyncResult ar) {
            if (ar.exception == null) {
                this.this$0.logi("Switch Sim Slot ok");
                HwVSimSlotSwitchController hwVSimSlotSwitchController = this.this$0;
                hwVSimSlotSwitchController.mPollingCount = hwVSimSlotSwitchController.mPollingCount + HwVSimSlotSwitchController.INVALID_SIM_SLOT;
                this.this$0.logi("mPollingCount = " + this.this$0.mPollingCount);
                this.this$0.setUserSwitchDualCardSlots(this.this$0.mExpectSlot);
                this.this$0.processSwitchCommrilMode();
                int[] slots = new int[HwVSimSlotSwitchController.EVENT_GET_SIM_STATE_DONE];
                if (this.this$0.mIsVSimOn) {
                    slots[HwVSimSlotSwitchController.RF0] = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
                    slots[HwVSimSlotSwitchController.RF1] = this.this$0.mMainSlot;
                    slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] = this.this$0.mExpectSlot;
                } else {
                    slots[HwVSimSlotSwitchController.RF0] = this.this$0.mExpectSlot;
                    slots[HwVSimSlotSwitchController.RF1] = this.this$0.mMainSlot;
                    slots[HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE] = HwVSimSlotSwitchController.EVENT_GET_SIM_SLOT_DONE;
                }
                HwVSimController.getInstance().setSimSlotTable(slots);
                return;
            }
            this.this$0.loge("exception " + ar.exception);
            this.this$0.sendInitDone();
            this.this$0.transitionTo(this.this$0.mDefaultState);
        }

        private boolean isNeedSwitchSlot() {
            return this.this$0.mExpectSlot != this.this$0.mMainSlot ? true : HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
        }

        private void restartRild() {
            if (this.this$0.mExpectCommrilMode != CommrilMode.NON_MODE) {
                this.this$0.logi("setCommrilMode to " + this.this$0.mExpectCommrilMode);
                this.this$0.setCommrilMode(this.this$0.mExpectCommrilMode);
                this.this$0.mExpectCommrilMode = CommrilMode.NON_MODE;
            }
            resetStatus();
            this.this$0.logi("restart rild");
            if (this.this$0.mIsVSimOn) {
                this.this$0.mCis[HwVSimSlotSwitchController.SUB_VSIM].restartRild(null);
            } else {
                this.this$0.mCis[this.this$0.mMainSlot].restartRild(null);
            }
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK);
        }

        private void resetStatus() {
            this.this$0.logi("resetStatus");
            for (int i = HwVSimSlotSwitchController.RF0; i < HwVSimSlotSwitchController.SUB_COUNT; i += HwVSimSlotSwitchController.RF1) {
                this.this$0.mRadioAvailable[i] = HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK;
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues() {
        if (-com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues != null) {
            return -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues;
        }
        int[] iArr = new int[CommrilMode.values().length];
        try {
            iArr[CommrilMode.CG_MODE.ordinal()] = RF1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommrilMode.CLG_MODE.ordinal()] = EVENT_GET_SIM_SLOT_DONE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommrilMode.HISI_CGUL_MODE.ordinal()] = EVENT_GET_SIM_STATE_DONE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommrilMode.HISI_CG_MODE.ordinal()] = EVENT_SET_SIM_STATE_DONE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommrilMode.NON_MODE.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommrilMode.SVLTE_MODE.ordinal()] = 7;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommrilMode.ULG_MODE.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimSlotSwitchController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimSlotSwitchController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimSlotSwitchController.<clinit>():void");
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
        this.mRadioPowerStatus = null;
        this.mDefaultState = new SlotSwitchDefaultState();
        this.mInitialState = new InitialState();
        this.mSwitchCommrilModeState = new SwitchCommrilModeState();
        logd("VSimSlotSwitchController");
        logi("IS_SUPPORT_FULL_NETWORK: " + IS_SUPPORT_FULL_NETWORK);
        logi("IS_HISI_CDMA_SUPPORTED: " + IS_HISI_CDMA_SUPPORTED);
        this.mContext = c;
        this.mCis = new CommandsInterface[(cis.length + RF1)];
        for (i = RF0; i < cis.length; i += RF1) {
            this.mCis[i] = cis[i];
        }
        this.mCis[cis.length] = vsimCi;
        initWhatToStringMap();
        this.mSlotSwitchDone = IS_SUPPORT_FULL_NETWORK;
        this.mGotSimSlot = IS_SUPPORT_FULL_NETWORK;
        this.mRadioAvailable = new boolean[SUB_COUNT];
        this.mGotRatCombineMode = new boolean[SUB_COUNT];
        this.mRatCombineMode = new int[SUB_COUNT];
        this.mRadioPowerStatus = new boolean[SUB_COUNT];
        for (i = RF0; i < SUB_COUNT; i += RF1) {
            this.mRadioAvailable[i] = IS_SUPPORT_FULL_NETWORK;
            this.mGotRatCombineMode[i] = IS_SUPPORT_FULL_NETWORK;
            this.mRatCombineMode[i] = INVALID_SIM_SLOT;
            this.mRadioPowerStatus[i] = IS_SUPPORT_FULL_NETWORK;
        }
        this.switchSlotDoneMark = new boolean[PHONE_COUNT];
        for (i = RF0; i < PHONE_COUNT; i += RF1) {
            this.switchSlotDoneMark[i] = IS_SUPPORT_FULL_NETWORK;
        }
        this.mMainSlot = INVALID_SIM_SLOT;
        this.mExpectSlot = INVALID_SIM_SLOT;
        this.mExpectCommrilMode = CommrilMode.NON_MODE;
        this.mIsVSimOn = IS_SUPPORT_FULL_NETWORK;
        this.mIsVSimEnabled = IS_SUPPORT_FULL_NETWORK;
        this.mNeedSwitchCommrilMode = IS_SUPPORT_FULL_NETWORK;
        this.mInitDoneSent = IS_SUPPORT_FULL_NETWORK;
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
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_GET_SIM_SLOT_DONE), "EVENT_GET_SIM_SLOT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_GET_SIM_STATE_DONE), "EVENT_GET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_SET_SIM_STATE_DONE), "EVENT_SET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(CMD_SWITCH_COMMRIL_MODE), "CMD_SWITCH_COMMRIL_MODE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_SWITCH_COMMRIL_MODE_DONE), "EVENT_SWITCH_COMMRIL_MODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_SET_RAT_COMBINE_MODE_DONE), "EVENT_SET_RAT_COMBINE_MODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_SWITCH_RFIC_CHANNEL_DONE), "EVENT_SWITCH_RFIC_CHANNEL_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_SET_CDMA_MODE_SIDE_DONE), "EVENT_SET_CDMA_MODE_SIDE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_RADIO_AVAILABLE), "EVENT_RADIO_AVAILABLE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_SWITCH_SLOT_DONE), "EVENT_SWITCH_SLOT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_GET_RAT_COMBINE_MODE_DONE), "EVENT_GET_RAT_COMBINE_MODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(EVENT_INITIAL_TIMEOUT), "EVENT_INITIAL_TIMEOUT");
    }

    public void setUserSwitchDualCardSlots(int subscription) {
        System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", subscription);
        logi("setUserSwitchDualCardSlots: " + subscription);
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            updateHotPlugMainSlotIccId(subscription);
        }
    }

    private void updateHotPlugMainSlotIccId(int subscription) {
        HwHotplugController hc = HwHotplugController.getInstance();
        UiccCard card = UiccController.getInstance().getUiccCard(subscription);
        if (card != null) {
            hc.updateHotPlugMainSlotIccId(card.getIccId());
        }
    }

    private void sendInitDone() {
        if (!this.mInitDoneSent) {
            HwVSimController.getInstance().getHandler().sendEmptyMessage(5);
            this.mInitDoneSent = true;
        }
    }

    public CommrilMode getCommrilMode() {
        String mode = SystemProperties.get(PROPERTY_COMMRIL_MODE, "CLG_MODE");
        CommrilMode result = CommrilMode.NON_MODE;
        try {
            return (CommrilMode) Enum.valueOf(CommrilMode.class, mode);
        } catch (IllegalArgumentException e) {
            logd("getCommrilMode, IllegalArgumentException, mode = " + mode);
            return result;
        }
    }

    private void setCommrilMode(CommrilMode mode) {
        SystemProperties.set(PROPERTY_COMMRIL_MODE, mode.toString());
    }

    public CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        if (mainSlot == INVALID_SIM_SLOT) {
            logd("main slot invalid");
            return expectCommrilMode;
        }
        int slaveSlot = mainSlot == 0 ? RF1 : RF0;
        if (cardType[mainSlot] == EVENT_GET_SIM_SLOT_DONE || cardType[mainSlot] == EVENT_GET_SIM_STATE_DONE) {
            if (IS_HISI_CDMA_SUPPORTED) {
                expectCommrilMode = CommrilMode.HISI_CGUL_MODE;
            } else {
                expectCommrilMode = CommrilMode.CLG_MODE;
            }
        } else if ((cardType[mainSlot] == RF1 || cardType[mainSlot] == 0) && (cardType[slaveSlot] == EVENT_GET_SIM_SLOT_DONE || cardType[slaveSlot] == EVENT_GET_SIM_STATE_DONE)) {
            if (IS_HISI_CDMA_SUPPORTED) {
                expectCommrilMode = CommrilMode.HISI_CG_MODE;
            } else {
                expectCommrilMode = CommrilMode.CG_MODE;
            }
        } else if (cardType[mainSlot] != RF1 && cardType[slaveSlot] != RF1) {
            expectCommrilMode = CommrilMode.NON_MODE;
        } else if (IS_HISI_CDMA_SUPPORTED) {
            expectCommrilMode = CommrilMode.HISI_CGUL_MODE;
        } else {
            expectCommrilMode = CommrilMode.ULG_MODE;
        }
        logd("[getExpectCommrilMode]: expectCommrilMode = " + expectCommrilMode);
        return expectCommrilMode;
    }

    private void switchCommrilMode() {
        logd("switchCommrilMode");
        this.mCompleteMsg = obtainMessage(EVENT_SWITCH_COMMRIL_MODE_DONE, Integer.valueOf(RF0));
        Message msg = obtainMessage(CMD_SWITCH_COMMRIL_MODE, Integer.valueOf(RF0));
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
        int slaveSlot = mainSlot == 0 ? RF1 : RF0;
        if (this.mIsVSimOn) {
            balongSlot = EVENT_GET_SIM_SLOT_DONE;
        } else {
            balongSlot = mainSlot;
        }
        switch (-getcom-android-internal-telephony-vsim-HwVSimSlotSwitchController$CommrilModeSwitchesValues()[newCommrilMode.ordinal()]) {
            case RF1 /*1*/:
                this.mCis[balongSlot].setHwRatCombineMode(RF1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(mainSlot)));
                String cg_standby_mode = SystemProperties.get(PROPERTY_CG_STANDBY_MODE, "home");
                logi("[switchCommrilMode]: cg_standby_mode = " + cg_standby_mode);
                if ("roam_gsm".equals(cg_standby_mode)) {
                    this.mCis[slaveSlot].setHwRatCombineMode(RF1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(slaveSlot)));
                    this.mCis[balongSlot].setHwRFChannelSwitch(RF0, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                } else {
                    this.mCis[slaveSlot].setHwRatCombineMode(RF0, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(slaveSlot)));
                    this.mCis[balongSlot].setHwRFChannelSwitch(RF1, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                }
                this.mPollingCount += EVENT_GET_SIM_STATE_DONE;
                logi("[switchCommrilMode]: Send set CG_MODE request done...");
                break;
            case EVENT_GET_SIM_SLOT_DONE /*2*/:
                boolean clg_overseas_mode = SystemProperties.getBoolean("persist.radio.overseas_mode", IS_SUPPORT_FULL_NETWORK);
                logi("[switchCommrilMode]: clg_overseas_mode = " + clg_overseas_mode);
                if (clg_overseas_mode) {
                    this.mCis[mainSlot].setHwRatCombineMode(RF1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(mainSlot)));
                } else {
                    this.mCis[mainSlot].setHwRatCombineMode(RF0, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(mainSlot)));
                }
                if (HwVSimUtils.isPlatformTwoModems() && HwVSimUtilsInner.isChinaTelecom()) {
                    this.mPollingCount += RF1;
                } else {
                    this.mCis[slaveSlot].setHwRatCombineMode(RF1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(slaveSlot)));
                    this.mCis[mainSlot].setHwRFChannelSwitch(RF0, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                    this.mPollingCount += EVENT_GET_SIM_STATE_DONE;
                }
                logi("[switchCommrilMode]: Send set CLG_MODE request done...");
                break;
            case EVENT_GET_SIM_STATE_DONE /*3*/:
                this.mCis[balongSlot].setCdmaModeSide(RF0, obtainMessage(EVENT_SET_CDMA_MODE_SIDE_DONE));
                this.mPollingCount += RF1;
                logi("[switchCommrilMode]: Send set HISI_CGUL_MODE request done...");
                break;
            case EVENT_SET_SIM_STATE_DONE /*4*/:
                this.mCis[balongSlot].setCdmaModeSide(RF1, obtainMessage(EVENT_SET_CDMA_MODE_SIDE_DONE));
                this.mPollingCount += RF1;
                logi("[switchCommrilMode]: Send set HISI_CG_MODE request done...");
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                this.mCis[balongSlot].setHwRatCombineMode(RF1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(mainSlot)));
                this.mCis[slaveSlot].setHwRatCombineMode(RF1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(slaveSlot)));
                this.mCis[balongSlot].setHwRFChannelSwitch(RF0, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                this.mPollingCount += EVENT_GET_SIM_STATE_DONE;
                logi("[switchCommrilMode]: Send set ULG_MODE request done...");
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
            AsyncResult.forMessage(onCompleteMsg, Boolean.valueOf(IS_SUPPORT_FULL_NETWORK), null);
            onCompleteMsg.sendToTarget();
        }
    }

    private void setAllRaidoPowerOff() {
        HwVSimController mVSimController = HwVSimController.getInstance();
        mVSimController.setIsWaitingSwitchCdmaModeSide(true);
        for (int subId = RF0; subId < this.mCis.length; subId += RF1) {
            this.mRadioPowerStatus[subId] = this.mCis[subId].getRadioState().isOn();
            if (this.mRadioPowerStatus[subId]) {
                logd("setAllRaidoPowerOff:mRadioPowerOnStatus[" + subId + "]=" + this.mRadioPowerStatus[subId] + " -> off");
                Message onCompleted = obtainMessage(EVENT_RADIO_POWER_OFF_DONE, Integer.valueOf(subId));
                ((GsmCdmaPhone) mVSimController.getPhoneBySub(subId)).getServiceStateTracker().setDesiredPowerState(IS_SUPPORT_FULL_NETWORK);
                this.mCis[subId].setRadioPower(IS_SUPPORT_FULL_NETWORK, onCompleted);
            } else {
                logd("setAllRaidoPowerOff:mRadioPowerOnStatus[" + subId + "]=" + this.mRadioPowerStatus[subId] + " is off");
            }
        }
    }

    private void onRadioPowerOffDone(int subId) {
        this.mRadioPowerStatus[subId] = IS_SUPPORT_FULL_NETWORK;
        logd("onRadioPowerOffDone:mRadioPowerStatus[" + subId + "]=" + this.mRadioPowerStatus[subId]);
        boolean isAllRadioPowerOff = true;
        for (int i = RF0; i < this.mCis.length; i += RF1) {
            if (this.mRadioPowerStatus[i]) {
                isAllRadioPowerOff = IS_SUPPORT_FULL_NETWORK;
                break;
            }
        }
        if (isAllRadioPowerOff) {
            logd("onRadioPowerOffDone: AllRadioPowerOff -> switchCommrilMode");
            switchCommrilMode();
        }
    }

    public static boolean isCDMACard(int cardtype) {
        if (cardtype == EVENT_GET_SIM_SLOT_DONE || cardtype == EVENT_GET_SIM_STATE_DONE) {
            return true;
        }
        return IS_SUPPORT_FULL_NETWORK;
    }
}
