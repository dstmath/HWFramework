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
    public static final boolean IS_DSDSPOWER_SUPPORT;
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final int SINGLE_CARD_MODEM_MODE = 0;
    private static final int SLOT1 = 1;
    private static final int SLOT2 = 2;
    private static final String TAG = "HwDsdsController";
    private static HwDsdsController mInstance;
    private static final Object mLock = new Object();
    private boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private int mActiveModemMode = SystemProperties.getInt("persist.radio.activemodem", -1);
    private boolean mAutoSetPowerupModemDone = false;
    private int mBalongSimSlot = 0;
    private CommandsInterface[] mCis;
    Context mContext;
    private boolean mDsdsCfgDone = false;
    private RegistrantList mIccDSDAutoModeSetRegistrants = new RegistrantList();
    private boolean mNeedNotify = false;
    private boolean mNeedWatingSlotSwitchDone = false;
    private int mNumOfCloseModemSuccess = 0;
    private boolean mProcessSetActiveModeForHotPlug = false;
    private boolean mSubinfoAutoUpdateDone = false;
    private int[] mSwitchTypes = new int[2];
    UiccCard[] mUiccCards = new UiccCard[SIM_NUM];
    private boolean mWatingSetActiveModeDone = false;

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.hw_dsdspowerup", false)) {
            z = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false) ^ 1;
        }
        IS_DSDSPOWER_SUPPORT = z;
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
        logd("constructor init");
        this.mContext = c;
        this.mCis = ci;
        if (!IS_DSDSPOWER_SUPPORT || (this.isMultiSimEnabled ^ 1) != 0) {
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

    /* JADX WARNING: Missing block: B:14:0x0046, code:
            return;
     */
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
                        logd("Received " + (msg.what == EVENT_HWDSDS_SET_ACTIVEMODE_DONE ? "EVENT_HWDSDS_SET_ACTIVEMODE_DONE" : "EVENT_HWDSDS_SET_ACTIVEMODE_TIMEOUT") + " on index " + index);
                        handleActiveModeDoneOrTimeout(msg);
                        break;
                    case EVENT_HOTPLUG_CLOSE_MODEM_DONE /*301*/:
                        logd("Received EVENT_HOTPLUG_CLOSE_MODEM_DONE on index " + index);
                        this.mNumOfCloseModemSuccess++;
                        if (this.mNumOfCloseModemSuccess == SIM_NUM) {
                            processCloseModemDone(EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT);
                            break;
                        }
                        break;
                    case EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT /*302*/:
                        logd("Received EVENT_HOTPLUG_CLOSE_MODEM_TIMEOUT on index " + index);
                        processCloseModemDone(EVENT_HOTPLUG_CLOSE_MODEM_DONE);
                        break;
                }
            }
            loge("Invalid index : " + index + " received with event " + msg.what);
        }
    }

    private void handleActiveModeDoneOrTimeout(Message msg) {
        AsyncResult ar_setmode = msg.obj;
        if (ar_setmode == null || ar_setmode.exception != null) {
            logd("Received EVENT_HWDSDS_SET_ACTIVEMODE_DONE  fail ");
            this.mActiveModemMode = -1;
        }
        if (this.mWatingSetActiveModeDone) {
            uiccHwdsdscancelTimeOut();
            logd("EVENT_HWDSDS_SET_ACTIVEMODE_DONE, need to power off and power on");
            setModemPowerDownForHotPlug();
            uiccHwdsdsGetIccStatusSend();
            this.mIccDSDAutoModeSetRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
            setAutoSetPowerupModemDone(true);
            setDsdsCfgDone(true);
            this.mWatingSetActiveModeDone = false;
        }
        if (this.mProcessSetActiveModeForHotPlug) {
            uiccHwdsdscancelTimeOut();
            setModemPowerDownForHotPlug();
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(0);
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
        return currSlot == this.mBalongSimSlot;
    }

    private int judgeBalongSimSlotFromResult(int[] slots) {
        if (slots[0] == 0 && slots[1] == 1 && slots[2] == 2) {
            return 0;
        }
        if (slots[0] == 1 && slots[1] == 0 && slots[2] == 2) {
            return 1;
        }
        if (slots[0] == 2 && slots[1] == 1 && slots[2] == 0) {
            return 0;
        }
        if (slots[0] == 2 && slots[1] == 0 && slots[2] == 1) {
            return 1;
        }
        return -1;
    }

    public void onGetBalongSimDone(AsyncResult ar, Integer index) {
        logd("onGetBalongSimDone. index = " + index);
        if (!(ar == null || ar.result == null)) {
            if (((int[]) ar.result).length == 2) {
                if (((int[]) ar.result)[1] + ((int[]) ar.result)[0] > 1) {
                    this.mBalongSimSlot = ((int[]) ar.result)[0] - 1;
                } else {
                    this.mBalongSimSlot = ((int[]) ar.result)[0];
                }
            } else if (((int[]) ar.result).length == 3) {
                int[] slots = ar.result;
                logd("slot result = " + Arrays.toString(slots));
                this.mBalongSimSlot = judgeBalongSimSlotFromResult(slots);
            } else if (((int[]) ar.result).length == 1) {
                this.mBalongSimSlot = ((int[]) ar.result)[0] - 1;
            } else {
                logd("onGetBalongSimDone. GetBalongSim Failed ! ar.result.length:" + ((int[]) ar.result).length);
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
            this.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[0] & 15;
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
        int subscription = 0;
        try {
            return System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            return subscription;
        }
    }

    private static void logd(String message) {
        Rlog.d(TAG, message);
    }

    private static void loge(String message) {
        Rlog.e(TAG, message);
    }

    public void setActiveModeForHotPlug() {
        if (HwVSimUtils.isVSimEnabled() && HwVSimUtils.isPlatformRealTripple()) {
            this.mProcessSetActiveModeForHotPlug = false;
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
        for (int i = 0; i < SIM_NUM; i++) {
            logd("setModemPowerDownForHotPlug PhoneFactory close modem i = " + i);
            PhoneFactory.getPhone(i).setRadioPower(false, obtainMessage(EVENT_HOTPLUG_CLOSE_MODEM_DONE, Integer.valueOf(i)));
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
        this.mProcessSetActiveModeForHotPlug = false;
        this.mNumOfCloseModemSuccess = 0;
        setRadioPowerForHotPlug();
        logd("processCloseModemDone mNeedNotify = " + this.mNeedNotify);
        if (this.mNeedNotify && HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().notifyMSimHotPlugPrompt();
            this.mNeedNotify = false;
            logd("notifyMSimHotPlugPrompt done.");
        }
    }

    private void setRadioPowerForHotPlug() {
        boolean isCard1Present = false;
        boolean isCard2Present = false;
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) <= 0) {
            for (UiccCard uc : this.mUiccCards) {
                if (this.mUiccCards.length < 2 || uc == null) {
                    logd("setRadioPowerForHotPlug fail");
                    return;
                }
            }
            boolean isCard1Inserted = false;
            int isCard2Inserted = 0;
            if (this.mUiccCards[0] != null) {
                isCard1Present = this.mUiccCards[0].getCardState() == CardState.CARDSTATE_PRESENT;
                isCard1Inserted = isCard1Present;
                if (this.mSubinfoAutoUpdateDone) {
                    logd("setRadioPowerForHotPlug mSubinfoAutoUpdateDone, need to judge sub1 state");
                    isCard1Present = isCard1Present && 1 == SubscriptionController.getInstance().getSubState(0);
                }
            } else {
                logd("setRadioPowerForHotPlug mUiccCards[0] == null");
            }
            if (this.mUiccCards[1] != null) {
                isCard2Present = this.mUiccCards[1].getCardState() == CardState.CARDSTATE_PRESENT;
                isCard2Inserted = isCard2Present;
                if (this.mSubinfoAutoUpdateDone) {
                    logd("setRadioPowerForHotPlug mSubinfoAutoUpdateDone, need to judge sub2 state");
                    isCard2Present = isCard2Present && 1 == SubscriptionController.getInstance().getSubState(1);
                }
            } else {
                logd("setRadioPowerForHotPlug mUiccCards[1] == null");
            }
            logd("setRadioPowerForHotPlug isCard1Present=" + isCard1Present + " ;isCard2Present=" + isCard2Present);
            if (isCard1Present && (isCard2Present ^ 1) != 0) {
                PhoneFactory.getPhone(0).setRadioPower(true);
                logd("setRadioPowerForHotPlug isCard1Present singel mode");
            } else if (isCard2Present && (isCard1Present ^ 1) != 0) {
                PhoneFactory.getPhone(1).setRadioPower(true);
                logd("setRadioPowerForHotPlug isCard2Present singel mode");
            } else if (isCard2Present && isCard1Present) {
                PhoneFactory.getPhone(0).setRadioPower(true);
                PhoneFactory.getPhone(1).setRadioPower(true);
                logd("setRadioPowerForHotPlug dual card  mode");
            } else if (!(isCard2Present || (isCard1Present ^ 1) == 0)) {
                int slotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                if (!this.mSubinfoAutoUpdateDone) {
                    PhoneFactory.getPhone(slotId).setRadioPower(true);
                    logd("setRadioPowerForHotPlug no card mode");
                } else if (isCard1Inserted || (isCard2Inserted ^ 1) == 0) {
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
        if (this.mDsdsCfgDone || (this.isMultiSimEnabled ^ 1) != 0) {
            logd(" don't need SetActiveMode");
            return;
        }
        logd("custHwdsdsSetActiveModeIfNeeded enter");
        if (uc == null || uc.length < 2) {
            logd("custSetActiveModeIfNeeded This Feature is not allowed here");
            return;
        }
        boolean mGetAllUiccCardsDone = true;
        for (int i = 0; i < uc.length; i++) {
            this.mUiccCards[i] = uc[i];
            if (uc[i] == null) {
                mGetAllUiccCardsDone = false;
            }
        }
        logd("mGetAllUiccCardsDone =" + mGetAllUiccCardsDone);
        if (mGetAllUiccCardsDone && !this.mNeedWatingSlotSwitchDone && (this.mWatingSetActiveModeDone ^ 1) != 0 && (isBalongSimSynced() || (HwVSimUtils.isVSimEnabled() && HwVSimUtils.isPlatformRealTripple()))) {
            logd("custHwdsdsSetActiveModeIfNeeded!");
            this.mWatingSetActiveModeDone = true;
            uiccHwdsdsSetActiveModemMode();
        }
    }

    private void uiccHwdsdsSetActiveModemMode() {
        boolean isCard1Present = false;
        boolean isCard2Present = false;
        for (UiccCard uc : this.mUiccCards) {
            if (this.mUiccCards.length < 2 || uc == null) {
                logd("uiccsetActiveModemMode fail");
                if (this.mProcessSetActiveModeForHotPlug) {
                    this.mProcessSetActiveModeForHotPlug = false;
                }
                return;
            }
        }
        logd("setSubinfoAutoUpdateDone false");
        setSubinfoAutoUpdateDone(false);
        if (this.mUiccCards[0] != null) {
            isCard1Present = this.mUiccCards[0].getCardState() == CardState.CARDSTATE_PRESENT;
        } else {
            logd("uiccsetActiveModemMode mUiccCards[0] == null");
        }
        if (this.mUiccCards[1] != null) {
            isCard2Present = this.mUiccCards[1].getCardState() == CardState.CARDSTATE_PRESENT;
        } else {
            logd("uiccsetActiveModemMode mUiccCards[1] == null");
        }
        Message msg = obtainMessage(EVENT_HWDSDS_SET_ACTIVEMODE_DONE);
        logd("uiccsetActiveModemMode isCard1Present=" + isCard1Present + "isCard2Present=" + isCard2Present);
        int setActiveModemMode = -1;
        if (isCard1Present && (isCard2Present ^ 1) != 0) {
            setActiveModemMode = 0;
            logd("uiccsetActiveModemMode isCard1Present singel mode");
        } else if (isCard2Present && (isCard1Present ^ 1) != 0) {
            setActiveModemMode = 0;
            logd("uiccsetActiveModemMode isCard2Present singel mode");
        } else if (isCard2Present && isCard1Present) {
            setActiveModemMode = 1;
            logd("uiccsetActiveModemMode dual card  mode");
        } else if (!(isCard2Present || (isCard1Present ^ 1) == 0)) {
            setActiveModemMode = 1;
            logd("uiccsetActiveModemMode dual card  mode");
        }
        if (HwVSimUtils.isVSimEnabled() && HwVSimUtils.isPlatformRealTripple()) {
            setActiveModemMode = 1;
        }
        if (this.mActiveModemMode != setActiveModemMode) {
            this.mActiveModemMode = setActiveModemMode;
            logd("uiccsetActiveModemMode need set ActiveModemMode, mActiveModemMode = " + this.mActiveModemMode);
            this.mCis[0].setActiveModemMode(this.mActiveModemMode, msg);
            uiccHwdsdsStartTimeOut();
        } else if (this.mProcessSetActiveModeForHotPlug) {
            if (setActiveModemMode == 0) {
                if (!isCard1Present) {
                    PhoneFactory.getPhone(0).setRadioPower(false, null);
                    logd("uiccsetActiveModemMode close card 1");
                } else if (!isCard2Present) {
                    PhoneFactory.getPhone(1).setRadioPower(false, null);
                    logd("uiccsetActiveModemMode close card 2");
                }
            }
            this.mProcessSetActiveModeForHotPlug = false;
            logd("mActiveModemMode = " + this.mActiveModemMode + ";setActiveModemMode = " + setActiveModemMode + "; same active mode do nothing!!");
        } else {
            if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) <= 0) {
                if (setActiveModemMode == 0) {
                    if (isCard1Present) {
                        PhoneFactory.getPhone(0).setRadioPower(true, null);
                        PhoneFactory.getPhone(1).setRadioPower(false, null);
                        logd("uiccsetActiveModemMode, SINGLE_CARD_MODEM_MODE set card 1 power on");
                    } else if (isCard2Present) {
                        PhoneFactory.getPhone(1).setRadioPower(true, null);
                        PhoneFactory.getPhone(0).setRadioPower(false, null);
                        logd("uiccsetActiveModemMode, SINGLE_CARD_MODEM_MODE set card 2 power on");
                    }
                } else if (1 == setActiveModemMode) {
                    if (isCard2Present && isCard1Present) {
                        PhoneFactory.getPhone(0).setRadioPower(true, null);
                        PhoneFactory.getPhone(1).setRadioPower(true, null);
                        logd("uiccsetActiveModemMode, DUAL_CARD_MODEM_MODE set dual card power on");
                    } else {
                        int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        int anotherSlot = mainSlot == 0 ? 1 : 0;
                        PhoneFactory.getPhone(mainSlot).setRadioPower(true, null);
                        PhoneFactory.getPhone(anotherSlot).setRadioPower(false, null);
                        logd("uiccsetActiveModemMode, DUAL_CARD_MODEM_MODE all cards absent set card 1 power on");
                    }
                }
                setAutoSetPowerupModemDone(true);
                setDsdsCfgDone(true);
                this.mWatingSetActiveModeDone = false;
            } else {
                logd("uiccsetActiveModemMode, in airplane mode return");
                setAutoSetPowerupModemDone(true);
                setDsdsCfgDone(true);
                this.mWatingSetActiveModeDone = false;
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
        return (this.mAutoSetPowerupModemDone || !IS_DSDSPOWER_SUPPORT) ? false : this.isMultiSimEnabled;
    }

    public void uiccHwdsdsGetIccStatusSend() {
        for (UiccCard uc : this.mUiccCards) {
            if (this.mUiccCards.length < 2 || uc == null) {
                logd("uiccHwdsdsGetIccStatusSend fail");
                return;
            }
        }
        for (int i = 0; i < this.mCis.length; i++) {
            logd("Received uiccHwdsdsGetIccStatusSend");
            this.mCis[i].getIccCardStatus(obtainMessage(EVENT_HWDSDS_GET_ICC_STATUS_DONE, Integer.valueOf(i)));
        }
    }

    public void uiccHwdsdsUnregRadioStateEvent() {
        for (CommandsInterface unregisterForRadioStateChanged : this.mCis) {
            unregisterForRadioStateChanged.unregisterForRadioStateChanged(this);
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
