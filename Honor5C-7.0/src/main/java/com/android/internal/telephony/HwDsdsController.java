package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.Arrays;

public class HwDsdsController extends Handler {
    private static final int DUAL_CARD_MODEM_MODE = 1;
    private static final int EVENT_GET_BALONG_SIM_DONE = 103;
    private static final int EVENT_HOTPLUG_CLOSE_MODEM_DONE = 301;
    private static final int EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT = 302;
    private static final int EVENT_HWDSDS_GET_ICC_STATUS_DONE = 201;
    private static final int EVENT_HWDSDS_RADIO_STATE_CHANGED = 202;
    private static final int EVENT_HWDSDS_SET_ACTIVEMODE_DONE = 203;
    private static final int EVENT_HWDSDS_SET_ACTIVEMODE_TIMEOUT = 204;
    private static final int EVENT_QUERY_CARD_TYPE_DONE = 102;
    private static final int EVENT_SWITCH_DUAL_CARD_SLOT = 101;
    private static final int EVENT_SWITCH_SIM_SLOT_CFG_DONE = 105;
    private static final int HOTPLUG_CLOSE_WAITING_TIME = 10000;
    private static final int HWDSDS_SET_ACTIVEMODE_WAITING_TIME = 5000;
    private static final int INVALID = -1;
    public static final boolean IS_DSDSPOWER_SUPPORT = false;
    private static final int SIM_NUM = 0;
    private static final int SINGLE_CARD_MODEM_MODE = 0;
    private static final int SLOT1 = 1;
    private static final int SLOT2 = 2;
    private static final String TAG = "HwDsdsController";
    private static HwDsdsController mInstance;
    private static final Object mLock = null;
    private boolean isMultiSimEnabled;
    private int mActiveModemMode;
    private boolean mAutoSetPowerupModemDone;
    private int mBalongSimSlot;
    private CommandsInterface[] mCis;
    Context mContext;
    private boolean mDsdsCfgDone;
    private RegistrantList mIccDSDAutoModeSetRegistrants;
    private boolean mNeedNotify;
    private boolean mNeedWatingSlotSwitchDone;
    private int mNumOfCloseModemSuccess;
    private boolean mProcessSetActiveModeForHotPlug;
    private boolean mSubinfoAutoUpdateDone;
    private int[] mSwitchTypes;
    UiccCard[] mUiccCards;
    private boolean mWatingSetActiveModeDone;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwDsdsController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwDsdsController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwDsdsController.<clinit>():void");
    }

    public boolean isProcessSetActiveModeForHotPlug() {
        return this.mProcessSetActiveModeForHotPlug;
    }

    public void setNeedNotify(boolean needNotify) {
        this.mNeedNotify = needNotify;
    }

    public void setNeedWatingSlotSwitchDone(boolean needWatingSlotSwitchDone) {
        this.mNeedWatingSlotSwitchDone = needWatingSlotSwitchDone;
    }

    public void setSubinfoAutoUpdateDone(boolean subinfoAutoUpdateDone) {
        this.mSubinfoAutoUpdateDone = subinfoAutoUpdateDone;
    }

    private HwDsdsController(Context c, CommandsInterface[] ci) {
        this.mUiccCards = new UiccCard[SIM_NUM];
        this.mNumOfCloseModemSuccess = SINGLE_CARD_MODEM_MODE;
        this.mActiveModemMode = SystemProperties.getInt("persist.radio.activemodem", INVALID);
        this.mProcessSetActiveModeForHotPlug = IS_DSDSPOWER_SUPPORT;
        this.mNeedNotify = IS_DSDSPOWER_SUPPORT;
        this.mSwitchTypes = new int[SLOT2];
        this.mBalongSimSlot = SINGLE_CARD_MODEM_MODE;
        this.mWatingSetActiveModeDone = IS_DSDSPOWER_SUPPORT;
        this.mIccDSDAutoModeSetRegistrants = new RegistrantList();
        this.mAutoSetPowerupModemDone = IS_DSDSPOWER_SUPPORT;
        this.mDsdsCfgDone = IS_DSDSPOWER_SUPPORT;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mNeedWatingSlotSwitchDone = IS_DSDSPOWER_SUPPORT;
        this.mSubinfoAutoUpdateDone = IS_DSDSPOWER_SUPPORT;
        logd("constructor init");
        this.mContext = c;
        this.mCis = ci;
        if (!IS_DSDSPOWER_SUPPORT || !this.isMultiSimEnabled) {
            logd("mDSDSPowerup= " + IS_DSDSPOWER_SUPPORT + " ; isMultiSimEnabled = " + this.isMultiSimEnabled);
        }
    }

    public static HwDsdsController make(Context context, CommandsInterface[] ci) {
        HwDsdsController hwDsdsController;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("HwDsdsController.make() should only be called once");
            }
            mInstance = new HwDsdsController(context, ci);
            hwDsdsController = mInstance;
        }
        return hwDsdsController;
    }

    public static HwDsdsController getInstance() {
        HwDsdsController hwDsdsController;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("HwDsdsController.getInstance can't be called before make()");
            }
            hwDsdsController = mInstance;
        }
        return hwDsdsController;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        synchronized (mLock) {
            Integer index = getCiIndex(msg);
            if (index.intValue() >= 0 && index.intValue() < this.mCis.length) {
                AsyncResult ar = msg.obj;
                switch (msg.what) {
                    case EVENT_QUERY_CARD_TYPE_DONE /*102*/:
                        logd("Received EVENT_QUERY_CARD_TYPE_DONE on index " + index);
                        onQueryCardTypeDone(ar, index);
                        break;
                    case EVENT_GET_BALONG_SIM_DONE /*103*/:
                        logd("Received EVENT_GET_BALONG_SIM_DONE on index " + index);
                        onGetBalongSimDone(ar, index);
                        break;
                    case EVENT_HWDSDS_GET_ICC_STATUS_DONE /*201*/:
                        logd("Received EVENT_HWDSDS_GET_ICC_STATUS_DONE on index " + index);
                        break;
                    case EVENT_HWDSDS_RADIO_STATE_CHANGED /*202*/:
                        if (!(this.mAutoSetPowerupModemDone || this.mCis[index.intValue()].getRadioState() == RadioState.RADIO_UNAVAILABLE)) {
                            logd("Received EVENT_HWDSDS_RADIO_STATE_CHANGED on index " + index);
                            this.mCis[index.intValue()].queryCardType(obtainMessage(EVENT_QUERY_CARD_TYPE_DONE, index));
                            this.mCis[index.intValue()].getBalongSim(obtainMessage(EVENT_GET_BALONG_SIM_DONE, index));
                            this.mCis[index.intValue()].getIccCardStatus(obtainMessage(EVENT_HWDSDS_GET_ICC_STATUS_DONE, index));
                            break;
                        }
                    case EVENT_HWDSDS_SET_ACTIVEMODE_DONE /*203*/:
                    case EVENT_HWDSDS_SET_ACTIVEMODE_TIMEOUT /*204*/:
                        logd("Received EVENT_HWDSDS_SET_ACTIVEMODE_DONE  ");
                        AsyncResult ar_setmode = msg.obj;
                        if (ar_setmode == null || ar_setmode.exception != null) {
                            logd("Received EVENT_HWDSDS_SET_ACTIVEMODE_DONE  fail ");
                            this.mActiveModemMode = INVALID;
                        }
                        if (this.mWatingSetActiveModeDone) {
                            uiccHwdsdscancelTimeOut();
                            logd("EVENT_HWDSDS_SET_ACTIVEMODE_DONE, need to power off and power on");
                            setModemPowerDownForHotPlug();
                            uiccHwdsdsGetIccStatusSend();
                            this.mIccDSDAutoModeSetRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                            setAutoSetPowerupModemDone(true);
                            setDsdsCfgDone(true);
                            this.mWatingSetActiveModeDone = IS_DSDSPOWER_SUPPORT;
                        }
                        if (this.mProcessSetActiveModeForHotPlug) {
                            uiccHwdsdscancelTimeOut();
                            setModemPowerDownForHotPlug();
                            break;
                        }
                        break;
                    case EVENT_HOTPLUG_CLOSE_MODEM_DONE /*301*/:
                        logd("Received EVENT_HOTPLUG_CLOSE_MODEM_DONE on index " + index);
                        this.mNumOfCloseModemSuccess += SLOT1;
                        if (this.mNumOfCloseModemSuccess == SIM_NUM) {
                            processCloseModemDone(EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT);
                            break;
                        }
                        break;
                    case EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT /*302*/:
                        logd("Received EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT!!!");
                        processCloseModemDone(EVENT_HOTPLUG_CLOSE_MODEM_DONE);
                        break;
                }
            }
            loge("Invalid index : " + index + " received with event " + msg.what);
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(SINGLE_CARD_MODEM_MODE);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    public boolean isBalongSimSynced() {
        int currSlot = getUserSwitchDualCardSlots();
        logd("currSlot  = " + currSlot + ", mBalongSimSlot = " + this.mBalongSimSlot);
        return currSlot == this.mBalongSimSlot ? true : IS_DSDSPOWER_SUPPORT;
    }

    public void onGetBalongSimDone(AsyncResult ar, Integer index) {
        logd("onGetBalongSimDone");
        if (!(ar == null || ar.result == null)) {
            if (((int[]) ar.result).length == SLOT2) {
                if (((int[]) ar.result)[SLOT1] + ((int[]) ar.result)[SINGLE_CARD_MODEM_MODE] > SLOT1) {
                    this.mBalongSimSlot = ((int[]) ar.result)[SINGLE_CARD_MODEM_MODE] + INVALID;
                } else {
                    this.mBalongSimSlot = ((int[]) ar.result)[SINGLE_CARD_MODEM_MODE];
                }
            } else if (((int[]) ar.result).length == 3) {
                int[] slots = ar.result;
                logd("slot result = " + Arrays.toString(slots));
                if (slots[SINGLE_CARD_MODEM_MODE] == 0 && slots[SLOT1] == SLOT1 && slots[SLOT2] == SLOT2) {
                    this.mBalongSimSlot = SINGLE_CARD_MODEM_MODE;
                } else if (slots[SINGLE_CARD_MODEM_MODE] == SLOT1 && slots[SLOT1] == 0 && slots[SLOT2] == SLOT2) {
                    this.mBalongSimSlot = SLOT1;
                } else if (slots[SINGLE_CARD_MODEM_MODE] == SLOT2 && slots[SLOT1] == SLOT1 && slots[SLOT2] == 0) {
                    this.mBalongSimSlot = SINGLE_CARD_MODEM_MODE;
                } else if (slots[SINGLE_CARD_MODEM_MODE] == SLOT2 && slots[SLOT1] == 0 && slots[SLOT2] == SLOT1) {
                    this.mBalongSimSlot = SLOT1;
                } else {
                    loge("onGetBalongSimDone invalid slot result");
                    this.mBalongSimSlot = INVALID;
                }
            } else {
                this.mBalongSimSlot = ((int[]) ar.result)[SINGLE_CARD_MODEM_MODE] + INVALID;
            }
        }
        logd("mBalongSimSlot = " + this.mBalongSimSlot);
    }

    public void setBalongSimSlot(int slot) {
        this.mBalongSimSlot = slot;
    }

    public int getBalongSimSlot() {
        return this.mBalongSimSlot;
    }

    private void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        logd("onQueryCardTypeDone");
        if (!(ar == null || ar.result == null)) {
            this.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[SINGLE_CARD_MODEM_MODE] & 15;
        }
        logd("mSwitchTypes[" + index + "] = " + this.mSwitchTypes[index.intValue()]);
    }

    public void registerDSDSAutoModemSetChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            this.mIccDSDAutoModeSetRegistrants.add(new Registrant(h, what, obj));
        }
    }

    public void unregisterDSDSAutoModemSetChanged(Handler h) {
        synchronized (mLock) {
            this.mIccDSDAutoModeSetRegistrants.remove(h);
        }
    }

    public int getUserSwitchDualCardSlots() {
        int subscription = SINGLE_CARD_MODEM_MODE;
        try {
            subscription = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
        }
        return subscription;
    }

    private static void logd(String message) {
        Rlog.d(TAG, message);
    }

    private static void loge(String message) {
        Rlog.e(TAG, message);
    }

    public void setActiveModeForHotPlug() {
        if (HwVSimUtils.isVSimEnabled() && HwVSimUtils.isPlatformRealTripple()) {
            this.mProcessSetActiveModeForHotPlug = IS_DSDSPOWER_SUPPORT;
            logd("vsim is enabled, just return!");
        } else if (this.mProcessSetActiveModeForHotPlug) {
            logd("mProcessSetActiveModeForHotPlug = " + this.mProcessSetActiveModeForHotPlug + " , just return");
        } else {
            this.mProcessSetActiveModeForHotPlug = true;
            this.mUiccCards = UiccController.getInstance().getUiccCards();
            uiccHwdsdsSetActiveModemMode();
        }
    }

    private void setModemPowerDownForHotPlug() {
        for (int i = SINGLE_CARD_MODEM_MODE; i < SIM_NUM; i += SLOT1) {
            logd("setModemPowerDownForHotPlug PhoneFactory close modem i = " + i);
            PhoneFactory.getPhone(i).setRadioPower(IS_DSDSPOWER_SUPPORT, obtainMessage(EVENT_HOTPLUG_CLOSE_MODEM_DONE, Integer.valueOf(i)));
        }
        removeMessages(EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT);
        sendMessageDelayed(obtainMessage(EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT), HwVSimConstants.VSIM_DISABLE_RETRY_TIMEOUT);
    }

    private void processCloseModemDone(int what) {
        logd("processCloseModemDone what = " + what);
        if (hasMessages(what)) {
            logd("remove " + what + " message!");
            removeMessages(what);
        }
        this.mProcessSetActiveModeForHotPlug = IS_DSDSPOWER_SUPPORT;
        this.mNumOfCloseModemSuccess = SINGLE_CARD_MODEM_MODE;
        setRadioPowerForHotPlug();
        logd("processCloseModemDone mNeedNotify = " + this.mNeedNotify);
        if (this.mNeedNotify) {
            HwHotplugController.getInstance().notifyMSimHotPlugPrompt();
            this.mNeedNotify = IS_DSDSPOWER_SUPPORT;
            logd("notifyMSimHotPlugPrompt done.");
        }
    }

    private void setRadioPowerForHotPlug() {
        boolean mdsdsDesiredPowerState;
        boolean z = IS_DSDSPOWER_SUPPORT;
        boolean z2 = IS_DSDSPOWER_SUPPORT;
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", SINGLE_CARD_MODEM_MODE) <= 0) {
            mdsdsDesiredPowerState = true;
        } else {
            mdsdsDesiredPowerState = IS_DSDSPOWER_SUPPORT;
        }
        if (mdsdsDesiredPowerState) {
            UiccCard[] uiccCardArr = this.mUiccCards;
            int length = uiccCardArr.length;
            for (int i = SINGLE_CARD_MODEM_MODE; i < length; i += SLOT1) {
                UiccCard uc = uiccCardArr[i];
                if (this.mUiccCards.length < SLOT2 || uc == null) {
                    logd("setRadioPowerForHotPlug fail");
                    return;
                }
            }
            boolean z3 = IS_DSDSPOWER_SUPPORT;
            boolean z4 = IS_DSDSPOWER_SUPPORT;
            if (this.mUiccCards[SINGLE_CARD_MODEM_MODE] != null) {
                if (this.mUiccCards[SINGLE_CARD_MODEM_MODE].getCardState() == CardState.CARDSTATE_PRESENT) {
                    z = true;
                } else {
                    z = IS_DSDSPOWER_SUPPORT;
                }
                z3 = z;
                if (this.mSubinfoAutoUpdateDone) {
                    logd("setRadioPowerForHotPlug mSubinfoAutoUpdateDone, need to judge sub1 state");
                    z = (z && SLOT1 == SubscriptionController.getInstance().getSubState(SINGLE_CARD_MODEM_MODE)) ? true : IS_DSDSPOWER_SUPPORT;
                }
            } else {
                logd("setRadioPowerForHotPlug mUiccCards[0] == null");
            }
            if (this.mUiccCards[SLOT1] != null) {
                if (this.mUiccCards[SLOT1].getCardState() == CardState.CARDSTATE_PRESENT) {
                    z2 = true;
                } else {
                    z2 = IS_DSDSPOWER_SUPPORT;
                }
                z4 = z2;
                if (this.mSubinfoAutoUpdateDone) {
                    logd("setRadioPowerForHotPlug mSubinfoAutoUpdateDone, need to judge sub2 state");
                    z2 = (z2 && SLOT1 == SubscriptionController.getInstance().getSubState(SLOT1)) ? true : IS_DSDSPOWER_SUPPORT;
                }
            } else {
                logd("setRadioPowerForHotPlug mUiccCards[1] == null");
            }
            logd("setRadioPowerForHotPlug isCard1Present=" + z + " ;isCard2Present=" + z2);
            if (z && !z2) {
                PhoneFactory.getPhone(SINGLE_CARD_MODEM_MODE).setRadioPower(true);
                logd("setRadioPowerForHotPlug isCard1Present singel mode");
            } else if (z2 && !z) {
                PhoneFactory.getPhone(SLOT1).setRadioPower(true);
                logd("setRadioPowerForHotPlug isCard2Present singel mode");
            } else if (z2 && z) {
                PhoneFactory.getPhone(SINGLE_CARD_MODEM_MODE).setRadioPower(true);
                PhoneFactory.getPhone(SLOT1).setRadioPower(true);
                logd("setRadioPowerForHotPlug dual card  mode");
            } else if (!(z2 || z)) {
                int slotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                if (!this.mSubinfoAutoUpdateDone) {
                    PhoneFactory.getPhone(slotId).setRadioPower(true);
                    logd("setRadioPowerForHotPlug no card mode");
                } else if (z3 || r3) {
                    logd("at least one card is inserted!");
                } else {
                    PhoneFactory.getPhone(slotId).setRadioPower(true);
                    logd("no cards in slots!");
                }
            }
            return;
        }
        logd("setRadioPowerForHotPlug in airplane mode");
    }

    public void custHwdsdsSetActiveModeIfNeeded(UiccCard[] uc) {
        if (this.mDsdsCfgDone || !this.isMultiSimEnabled) {
            logd(" don't need SetActiveMode");
            return;
        }
        logd("custHwdsdsSetActiveModeIfNeeded enter");
        if (uc == null || uc.length < SLOT2) {
            logd("custSetActiveModeIfNeeded This Feature is not allowed here");
            return;
        }
        boolean mGetAllUiccCardsDone = true;
        for (int i = SINGLE_CARD_MODEM_MODE; i < uc.length; i += SLOT1) {
            this.mUiccCards[i] = uc[i];
            if (uc[i] == null) {
                mGetAllUiccCardsDone = IS_DSDSPOWER_SUPPORT;
            }
        }
        logd("mGetAllUiccCardsDone =" + mGetAllUiccCardsDone);
        if (mGetAllUiccCardsDone && !this.mNeedWatingSlotSwitchDone && !this.mWatingSetActiveModeDone && (isBalongSimSynced() || (HwVSimUtils.isVSimEnabled() && HwVSimUtils.isPlatformRealTripple()))) {
            logd("custHwdsdsSetActiveModeIfNeeded!");
            this.mWatingSetActiveModeDone = true;
            uiccHwdsdsSetActiveModemMode();
        }
    }

    private void uiccHwdsdsSetActiveModemMode() {
        boolean isCard1Present = IS_DSDSPOWER_SUPPORT;
        boolean isCard2Present = IS_DSDSPOWER_SUPPORT;
        UiccCard[] uiccCardArr = this.mUiccCards;
        int length = uiccCardArr.length;
        for (int i = SINGLE_CARD_MODEM_MODE; i < length; i += SLOT1) {
            UiccCard uc = uiccCardArr[i];
            if (this.mUiccCards.length < SLOT2 || uc == null) {
                logd("uiccsetActiveModemMode fail");
                if (this.mProcessSetActiveModeForHotPlug) {
                    this.mProcessSetActiveModeForHotPlug = IS_DSDSPOWER_SUPPORT;
                }
                return;
            }
        }
        logd("setSubinfoAutoUpdateDone false");
        setSubinfoAutoUpdateDone(IS_DSDSPOWER_SUPPORT);
        if (this.mUiccCards[SINGLE_CARD_MODEM_MODE] != null) {
            isCard1Present = this.mUiccCards[SINGLE_CARD_MODEM_MODE].getCardState() == CardState.CARDSTATE_PRESENT ? true : IS_DSDSPOWER_SUPPORT;
        } else {
            logd("uiccsetActiveModemMode mUiccCards[0] == null");
        }
        if (this.mUiccCards[SLOT1] != null) {
            isCard2Present = this.mUiccCards[SLOT1].getCardState() == CardState.CARDSTATE_PRESENT ? true : IS_DSDSPOWER_SUPPORT;
        } else {
            logd("uiccsetActiveModemMode mUiccCards[1] == null");
        }
        Message msg = obtainMessage(EVENT_HWDSDS_SET_ACTIVEMODE_DONE);
        logd("uiccsetActiveModemMode isCard1Present=" + isCard1Present + "isCard2Present=" + isCard2Present);
        int setActiveModemMode = INVALID;
        if (isCard1Present && !isCard2Present) {
            setActiveModemMode = SINGLE_CARD_MODEM_MODE;
            logd("uiccsetActiveModemMode isCard1Present singel mode");
        } else if (isCard2Present && !isCard1Present) {
            setActiveModemMode = SINGLE_CARD_MODEM_MODE;
            logd("uiccsetActiveModemMode isCard2Present singel mode");
        } else if (isCard2Present && isCard1Present) {
            setActiveModemMode = SLOT1;
            logd("uiccsetActiveModemMode dual card  mode");
        } else if (!(isCard2Present || isCard1Present)) {
            setActiveModemMode = SLOT1;
            logd("uiccsetActiveModemMode dual card  mode");
        }
        if (HwVSimUtils.isVSimEnabled() && HwVSimUtils.isPlatformRealTripple()) {
            setActiveModemMode = SLOT1;
        }
        if (this.mActiveModemMode != setActiveModemMode) {
            this.mActiveModemMode = setActiveModemMode;
            logd("uiccsetActiveModemMode need set ActiveModemMode, mActiveModemMode = " + this.mActiveModemMode);
            this.mCis[SINGLE_CARD_MODEM_MODE].setActiveModemMode(this.mActiveModemMode, msg);
            uiccHwdsdsStartTimeOut();
        } else if (this.mProcessSetActiveModeForHotPlug) {
            if (setActiveModemMode == 0) {
                if (!isCard1Present) {
                    PhoneFactory.getPhone(SINGLE_CARD_MODEM_MODE).setRadioPower(IS_DSDSPOWER_SUPPORT, null);
                    logd("uiccsetActiveModemMode close card 1");
                } else if (!isCard2Present) {
                    PhoneFactory.getPhone(SLOT1).setRadioPower(IS_DSDSPOWER_SUPPORT, null);
                    logd("uiccsetActiveModemMode close card 2");
                }
            }
            this.mProcessSetActiveModeForHotPlug = IS_DSDSPOWER_SUPPORT;
            logd("mActiveModemMode = " + this.mActiveModemMode + ";setActiveModemMode = " + setActiveModemMode + "; same active mode do nothing!!");
        } else {
            if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", SINGLE_CARD_MODEM_MODE) <= 0 ? true : IS_DSDSPOWER_SUPPORT) {
                if (setActiveModemMode == 0) {
                    if (isCard1Present) {
                        PhoneFactory.getPhone(SINGLE_CARD_MODEM_MODE).setRadioPower(true, null);
                        PhoneFactory.getPhone(SLOT1).setRadioPower(IS_DSDSPOWER_SUPPORT, null);
                        logd("uiccsetActiveModemMode, SINGLE_CARD_MODEM_MODE set card 1 power on");
                    } else if (isCard2Present) {
                        PhoneFactory.getPhone(SLOT1).setRadioPower(true, null);
                        PhoneFactory.getPhone(SINGLE_CARD_MODEM_MODE).setRadioPower(IS_DSDSPOWER_SUPPORT, null);
                        logd("uiccsetActiveModemMode, SINGLE_CARD_MODEM_MODE set card 2 power on");
                    }
                } else if (SLOT1 == setActiveModemMode) {
                    if (isCard2Present && isCard1Present) {
                        PhoneFactory.getPhone(SINGLE_CARD_MODEM_MODE).setRadioPower(true, null);
                        PhoneFactory.getPhone(SLOT1).setRadioPower(true, null);
                        logd("uiccsetActiveModemMode, DUAL_CARD_MODEM_MODE set dual card power on");
                    } else {
                        int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        int anotherSlot = mainSlot == 0 ? SLOT1 : SINGLE_CARD_MODEM_MODE;
                        PhoneFactory.getPhone(mainSlot).setRadioPower(true, null);
                        PhoneFactory.getPhone(anotherSlot).setRadioPower(IS_DSDSPOWER_SUPPORT, null);
                        logd("uiccsetActiveModemMode, DUAL_CARD_MODEM_MODE all cards absent set card 1 power on");
                    }
                }
                setAutoSetPowerupModemDone(true);
                setDsdsCfgDone(true);
                this.mWatingSetActiveModeDone = IS_DSDSPOWER_SUPPORT;
            } else {
                logd("uiccsetActiveModemMode, in airplane mode return");
                setAutoSetPowerupModemDone(true);
                setDsdsCfgDone(true);
                this.mWatingSetActiveModeDone = IS_DSDSPOWER_SUPPORT;
            }
        }
    }

    private void uiccHwdsdscancelTimeOut() {
        removeMessages(EVENT_HWDSDS_SET_ACTIVEMODE_TIMEOUT);
        logd("uiccHwdsdscancelTimeOut");
    }

    private void uiccHwdsdsStartTimeOut() {
        uiccHwdsdscancelTimeOut();
        sendMessageDelayed(obtainMessage(EVENT_HWDSDS_SET_ACTIVEMODE_TIMEOUT), 5000);
        logd("uiccHwdsdsStartTimeOut");
    }

    public boolean uiccHwdsdsNeedSetActiveMode() {
        logd("uiccHwdsdsNeedSetActiveMode mAutoSetPowerupModemMode=" + this.mAutoSetPowerupModemDone);
        return (this.mAutoSetPowerupModemDone || !IS_DSDSPOWER_SUPPORT) ? IS_DSDSPOWER_SUPPORT : this.isMultiSimEnabled;
    }

    public void uiccHwdsdsGetIccStatusSend() {
        UiccCard[] uiccCardArr = this.mUiccCards;
        int length = uiccCardArr.length;
        for (int i = SINGLE_CARD_MODEM_MODE; i < length; i += SLOT1) {
            UiccCard uc = uiccCardArr[i];
            if (this.mUiccCards.length < SLOT2 || uc == null) {
                logd("uiccHwdsdsGetIccStatusSend fail");
                return;
            }
        }
        for (int i2 = SINGLE_CARD_MODEM_MODE; i2 < this.mCis.length; i2 += SLOT1) {
            logd("Received uiccHwdsdsGetIccStatusSend");
            this.mCis[i2].getIccCardStatus(obtainMessage(EVENT_HWDSDS_GET_ICC_STATUS_DONE, Integer.valueOf(i2)));
        }
    }

    public void uiccHwdsdsUnregRadioStateEvent() {
        for (int i = SINGLE_CARD_MODEM_MODE; i < this.mCis.length; i += SLOT1) {
            this.mCis[i].unregisterForRadioStateChanged(this);
            logd("unregisterForRadioStateChanged");
        }
    }

    private void setAutoSetPowerupModemDone(boolean isDone) {
        this.mAutoSetPowerupModemDone = isDone;
        if (this.mAutoSetPowerupModemDone) {
            HwVSimUtils.processAutoSetPowerupModemDone();
        }
    }

    public void setDsdsCfgDone(boolean isDone) {
        logd("setDsdsCfgDone, value = " + isDone);
        this.mDsdsCfgDone = isDone;
    }
}
