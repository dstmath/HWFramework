package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.util.StateEx;
import com.huawei.internal.util.StateMachineEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

public class HwVSimSlotSwitchController extends StateMachineEx {
    public static final int CARD_TYPE_DUAL_MODE = 3;
    public static final int CARD_TYPE_INVALID = -1;
    public static final int CARD_TYPE_NO_SIM = 0;
    public static final int CARD_TYPE_SINGLE_CDMA = 2;
    public static final int CARD_TYPE_SINGLE_GSM = 1;
    private static final int EVENT_GET_SIM_SLOT_DONE = 2;
    private static final int EVENT_GET_SIM_STATE_DONE = 3;
    private static final int EVENT_INITIAL_TIMEOUT = 35;
    private static final int EVENT_SET_CDMA_MODE_SIDE_DONE = 15;
    private static final int EVENT_SET_SIM_STATE_DONE = 4;
    private static final int EVENT_SWITCH_SLOT_DONE = 25;
    private static final long INITIAL_TIMEOUT = 120000;
    private static final int INVALID_SIM_SLOT = -1;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemPropertiesEx.getBoolean("ro.config.fast_switch_simslot", false);
    public static final boolean IS_HISI_CDMA_SUPPORTED = SystemPropertiesEx.getBoolean("ro.config.hisi_cdma_supported", false);
    private static final boolean IS_PLATFORM_SUPPORT_VSIM = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);
    private static final boolean IS_SUPPORT_FULL_NETWORK = SystemPropertiesEx.getBoolean(PROPERTY_FULL_NETWORK_SUPPORT, false);
    private static final String LOG_TAG = "VSimSwitchController";
    private static final int PHONE_COUNT = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    private static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    private static final int SUB_COUNT = (PHONE_COUNT + 1);
    private static final int SUB_VSIM = PHONE_COUNT;
    private static HwVSimSlotSwitchController sInstance;
    private static final Object sLock = new Object();
    private CommandsInterfaceEx[] mCis;
    private SlotSwitchDefaultState mDefaultState = new SlotSwitchDefaultState();
    private boolean mGotSimSlot;
    private boolean mInitDoneSent;
    private InitialState mInitialState = new InitialState();
    private boolean mIsVSimEnabled;
    private boolean mIsVSimOn;
    private int mMainSlot;
    private boolean mSlotSwitchDone;
    private HashMap<Integer, String> mWhatToStringMap;

    private HwVSimSlotSwitchController(Context c, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
        super(LOG_TAG, Looper.myLooper());
        HwVSimLog.VSimLogI(LOG_TAG, "VSimSlotSwitchController");
        HwVSimLog.VSimLogI(LOG_TAG, "prop IS_SUPPORT_FULL_NETWORK: " + IS_SUPPORT_FULL_NETWORK);
        HwVSimLog.VSimLogI(LOG_TAG, "prop IS_HISI_CDMA_SUPPORTED: " + IS_HISI_CDMA_SUPPORTED);
        this.mCis = new CommandsInterfaceEx[(cis.length + 1)];
        for (int i = 0; i < cis.length; i++) {
            this.mCis[i] = cis[i];
        }
        this.mCis[cis.length] = vsimCi;
        initWhatToStringMap();
        this.mSlotSwitchDone = false;
        this.mGotSimSlot = false;
        this.mMainSlot = -1;
        this.mIsVSimOn = false;
        this.mIsVSimEnabled = false;
        this.mInitDoneSent = false;
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        setInitialState(this.mInitialState);
    }

    public static void create(Context context, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
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

    private static void slogd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public static boolean isCDMACard(int cardtype) {
        return cardtype == 2 || cardtype == 3;
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
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

    public void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
    }

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
        if (fd != null && pw != null && args != null) {
            HwVSimSlotSwitchController.super.dump(fd, pw, args);
            pw.println(" dummy");
        }
    }

    private void initWhatToStringMap() {
        this.mWhatToStringMap = new HashMap<>();
        this.mWhatToStringMap.put(2, "EVENT_GET_SIM_SLOT_DONE");
        this.mWhatToStringMap.put(3, "EVENT_GET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(4, "EVENT_SET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(15, "EVENT_SET_CDMA_MODE_SIDE_DONE");
        this.mWhatToStringMap.put(83, "EVENT_RADIO_AVAILABLE");
        this.mWhatToStringMap.put(25, "EVENT_SWITCH_SLOT_DONE");
        this.mWhatToStringMap.put(35, "EVENT_INITIAL_TIMEOUT");
        this.mWhatToStringMap.put(41, "EVENT_RADIO_POWER_OFF_DONE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendInitDone() {
        if (!this.mInitDoneSent) {
            HwVSimController.getInstance().getHandler().sendEmptyMessage(5);
            this.mInitDoneSent = true;
        }
    }

    public CommrilMode getCommrilMode() {
        String mode = SystemPropertiesEx.get("persist.radio.commril_mode", "HISI_CGUL_MODE");
        CommrilMode result = CommrilMode.NON_MODE;
        try {
            return (CommrilMode) Enum.valueOf(CommrilMode.class, mode);
        } catch (IllegalArgumentException e) {
            logd("getCommrilMode, IllegalArgumentException, mode = " + mode);
            return result;
        }
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
            expectCommrilMode = CommrilMode.HISI_CGUL_MODE;
        } else if ((cardType[mainSlot] == 1 || cardType[mainSlot] == 0) && (cardType[slaveSlot] == 2 || cardType[slaveSlot] == 3)) {
            expectCommrilMode = CommrilMode.HISI_CG_MODE;
        } else if (cardType[mainSlot] == 1 || cardType[slaveSlot] == 1) {
            expectCommrilMode = CommrilMode.HISI_CGUL_MODE;
        } else {
            expectCommrilMode = CommrilMode.NON_MODE;
        }
        logd("[getExpectCommrilMode]: expectCommrilMode = " + expectCommrilMode);
        return expectCommrilMode;
    }

    public void restartRildBySubState() {
        HwVSimController mVSimController = HwVSimController.getInstance();
        for (int slotId = 0; slotId < PHONE_COUNT; slotId++) {
            if (mVSimController.getSubState(slotId) != 0) {
                logd("restartRild : setDesiredPowerState is true for slotId:" + slotId);
                mVSimController.getPhoneBySub(slotId).getServiceStateTracker().setDesiredPowerState(true);
            } else {
                logd("restartRild : no need to setDesiredPowerState is true for slotId:" + slotId);
            }
        }
        logi("restart rild");
        if (this.mIsVSimOn) {
            this.mCis[SUB_VSIM].restartRild((Message) null);
        } else {
            this.mCis[this.mMainSlot].restartRild((Message) null);
        }
        if (!HwVSimController.getInstance().isVSimEnabled()) {
            HwVSimPhoneFactory.setIsVsimEnabledProp(false);
        }
        HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
        HwVSimController.getInstance().setIsWaitingNvMatchUnsol(false);
        HwVSimNvMatchController.getInstance().storeIfNeedRestartRildForNvMatch(false);
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

    public enum CommrilMode {
        NON_MODE,
        HISI_CGUL_MODE,
        HISI_CG_MODE,
        HISI_VSIM_MODE;

        public static CommrilMode getCLGMode() {
            return HISI_CGUL_MODE;
        }

        public static CommrilMode getULGMode() {
            return HISI_CGUL_MODE;
        }

        public static CommrilMode getCGMode() {
            return HISI_CG_MODE;
        }

        public static boolean isCLGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            if ((!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK && !HwVSimUtilsInner.isChinaTelecom()) || mode != HISI_CGUL_MODE) {
                return false;
            }
            if (HwVSimUtilsInner.isChinaTelecom() || cardType[mainSlot] == 2 || cardType[mainSlot] == 3) {
                return true;
            }
            return false;
        }

        public static boolean isULGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            if (!HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK) {
                if (HwVSimUtilsInner.isPlatformRealTripple()) {
                    return !HwVSimUtilsInner.isChinaTelecom();
                }
                if (!HwVSimUtilsInner.isChinaTelecom()) {
                    return true;
                }
            }
            if (mode != HISI_CGUL_MODE) {
                return false;
            }
            if (cardType[mainSlot] == 1 || cardType[mainSlot] == 0) {
                return true;
            }
            return false;
        }

        public static boolean isCGMode(CommrilMode mode, int[] cardType, int mainSlot) {
            if ((HwVSimSlotSwitchController.IS_SUPPORT_FULL_NETWORK || HwVSimUtilsInner.isChinaTelecom()) && mode == HISI_CG_MODE) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class SlotSwitchDefaultState extends StateEx {
        private SlotSwitchDefaultState() {
        }

        public void enter() {
            HwVSimSlotSwitchController.this.logd("DefaultState: enter");
        }

        public void exit() {
            HwVSimSlotSwitchController.this.logd("DefaultState: exit");
        }

        public boolean processMessage(Message msg) {
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logd("DefaultState: what = " + HwVSimSlotSwitchController.this.getWhatToString(msg.what));
            return true;
        }
    }

    private class InitialState extends StateEx {
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
                    HwVSimSlotSwitchController.this.mMainSlot = 0;
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
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("ar null");
            } else if (ar.getException() == null && ar.getResult() != null && ((int[]) ar.getResult()).length == 3) {
                onGetTriSimSlotDone((int[]) ar.getResult());
            } else if (ar.getException() == null && ar.getResult() != null && ((int[]) ar.getResult()).length == 2) {
                onGetDualSimSlotDone((int[]) ar.getResult());
            } else {
                HwVSimSlotSwitchController.this.logd("onGetSimSlotDone got error");
                if (HwVSimSlotSwitchController.this.mMainSlot == -1) {
                    HwVSimSlotSwitchController.this.mMainSlot = 0;
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
            HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController2.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController3.logd("mIsVSimOn = " + HwVSimSlotSwitchController.this.mIsVSimOn);
            HwVSimSlotSwitchController.this.mGotSimSlot = true;
            HwVSimController.getInstance().setSimSlotTable(slots);
            getAllRatCombineMode();
        }

        private void onGetDualSimSlotDone(int[] slots) {
            HwVSimSlotSwitchController.this.logd("onGetDualSimSlotDone");
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logd("result = " + Arrays.toString(slots));
            int modem2 = 2;
            if (slots[0] == 0 && slots[1] == 1) {
                HwVSimSlotSwitchController.this.mMainSlot = 0;
                HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 1 && slots[1] == 0) {
                HwVSimSlotSwitchController.this.mMainSlot = 1;
                HwVSimSlotSwitchController.this.mIsVSimOn = false;
            } else if (slots[0] == 2 && slots[1] == 0) {
                HwVSimSlotSwitchController.this.mMainSlot = 1;
                HwVSimSlotSwitchController.this.mIsVSimOn = true;
                modem2 = HwVSimSlotSwitchController.this.mMainSlot;
            } else if (slots[0] == 2 && slots[1] == 1) {
                HwVSimSlotSwitchController.this.mMainSlot = 0;
                HwVSimSlotSwitchController.this.mIsVSimOn = true;
                modem2 = HwVSimSlotSwitchController.this.mMainSlot;
            }
            int[] slotsTable = HwVSimUtilsInner.createSimSlotsTable(slots[0], slots[1], modem2);
            HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController2.logd("mMainSlot = " + HwVSimSlotSwitchController.this.mMainSlot);
            HwVSimSlotSwitchController hwVSimSlotSwitchController3 = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController3.logd("mIsVSimOn = " + HwVSimSlotSwitchController.this.mIsVSimOn);
            HwVSimSlotSwitchController.this.mGotSimSlot = true;
            HwVSimController.getInstance().setSimSlotTable(slotsTable);
            getAllRatCombineMode();
        }

        private void onGetSimStateDone(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("onGetSimStateDone : ar null");
            } else if (ar.getException() != null || ar.getResult() == null || ((int[]) ar.getResult()).length <= 3) {
                HwVSimSlotSwitchController.this.loge("onGetSimStateDone got error !");
            } else {
                int simIndex = ((int[]) ar.getResult())[0];
                int simEnable = ((int[]) ar.getResult())[1];
                int simSub = ((int[]) ar.getResult())[2];
                int simNetinfo = ((int[]) ar.getResult())[3];
                int slaveSlot = HwVSimSlotSwitchController.this.mMainSlot == 0 ? 1 : 0;
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.logd("[2Cards]simIndex= " + simIndex + ", simEnable= " + simEnable);
                HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController2.logd("[2Cards]simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (11 != simIndex || !HwVSimUtilsInner.isRadioAvailable(2)) {
                    HwVSimSlotSwitchController.this.mIsVSimOn = false;
                } else {
                    HwVSimSlotSwitchController.this.mIsVSimOn = true;
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
            if (HwVSimSlotSwitchController.IS_PLATFORM_SUPPORT_VSIM) {
                HwVSimSlotSwitchController.this.mIsVSimEnabled = HwVSimController.getInstance().isVSimEnabled();
            }
            HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
            hwVSimSlotSwitchController.logi("mIsVSimEnabled = " + HwVSimSlotSwitchController.this.mIsVSimEnabled);
        }

        private void getAllRatCombineMode() {
            HwVSimSlotSwitchController.this.logi("getAllRatCombineMode, x mode, done");
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
            int balongSlot = HwVSimSlotSwitchController.this.mIsVSimOn ? 2 : HwVSimSlotSwitchController.this.mMainSlot;
            HwVSimSlotSwitchController.this.mCis[balongSlot].getSimState(HwVSimSlotSwitchController.this.obtainMessage(3, balongSlot));
        }

        private void onCheckSimMode(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                HwVSimSlotSwitchController.this.logd("onGetSimStateDone : ar null");
            } else if (ar.getException() != null || ar.getResult() == null || ((int[]) ar.getResult()).length <= 3) {
                HwVSimSlotSwitchController.this.loge("onCheckSimMode got error !");
                checkSimModeDone();
            } else {
                int simIndex = ((int[]) ar.getResult())[0];
                int simEnable = ((int[]) ar.getResult())[1];
                int simSub = ((int[]) ar.getResult())[2];
                int simNetinfo = ((int[]) ar.getResult())[3];
                HwVSimSlotSwitchController hwVSimSlotSwitchController = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController.logd("simIndex= " + simIndex + ", simEnable= " + simEnable);
                HwVSimSlotSwitchController hwVSimSlotSwitchController2 = HwVSimSlotSwitchController.this;
                hwVSimSlotSwitchController2.logd("simSub= " + simSub + ", simNetinfo= " + simNetinfo);
                if (11 != simIndex || HwVSimSlotSwitchController.this.mIsVSimEnabled) {
                    checkSimModeDone();
                    return;
                }
                int balongSlot = HwVSimSlotSwitchController.this.mIsVSimOn ? 2 : HwVSimSlotSwitchController.this.mMainSlot;
                HwVSimSlotSwitchController.this.mCis[balongSlot].setSimState(simIndex, 0, HwVSimSlotSwitchController.this.obtainMessage(4, balongSlot));
            }
        }

        private void checkSimModeDone() {
            HwVSimSlotSwitchController.this.logd("checkSimModeDone");
            HwVSimSlotSwitchController.this.sendInitDone();
            HwVSimSlotSwitchController.this.transitionTo(HwVSimSlotSwitchController.this.mDefaultState);
        }

        private void onSetSimStateDone(Message msg) {
            HwVSimSlotSwitchController.this.mCis[HwVSimSlotSwitchController.this.mIsVSimOn ? 2 : HwVSimSlotSwitchController.this.mMainSlot].setSimState(1, 1, (Message) null);
            checkSimModeDone();
        }
    }
}
