package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.Arrays;

public class HwForeignUsimForTelecom extends Handler {
    private static final int CARD_MAX = TelephonyManager.getDefault().getPhoneCount();
    private static final int CARD_TYPE_DUAL_MODE = 3;
    private static final int CARD_TYPE_NO_SIM = 0;
    private static final int CARD_TYPE_SINGLE_CDMA = 2;
    private static final int CARD_TYPE_SINGLE_GSM = 1;
    private static final String CHINA_CC = "86";
    private static final int COMBINE = 0;
    private static final int EVENT_CHECK_ALL_CARDS_READY = 106;
    private static final int EVENT_GET_RAT_COMBINE_MODE_DONE = 104;
    private static final int EVENT_ICC_STATUS_CHANGED = 105;
    private static final int EVENT_RADIO_AVAILABLE = 103;
    private static final int EVENT_RESTART_RILD = 101;
    private static final int EVENT_SET_RAT_COMBINE_DONE = 107;
    private static final int EVENT_SET_RAT_COMBINE_MODE_DONE = 102;
    private static final int ICCCARD = 1;
    private static final int ICCID_LEN_MINIMUM = 7;
    private static final int INVALID = -1;
    private static boolean IS_CHINA_TELECOM = false;
    public static final boolean IS_OVERSEA_USIM_SUPPORT;
    private static final int NOT_COMBINE = 1;
    private static final String OVERSEAS_MODE = "persist.radio.overseas_mode";
    private static final String PREFIX_MII_ICCID = "89";
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    private static final String TAG = "HwForeignUsimForTelecom";
    private static final int UICCCARD = 2;
    private static HwForeignUsimForTelecom mInstance;
    private static final Object mLock = new Object();
    private boolean mAllCardsReady = false;
    private int mBalongSimSlot = -1;
    private int[] mCardTypes = new int[CARD_MAX];
    CommandsInterface[] mCis;
    Context mContext;
    private boolean[] mGetUiccCardsStatusDone = new boolean[CARD_MAX];
    private String[] mIccIds = new String[CARD_MAX];
    private boolean mIsOngoingRestartRild = false;
    private int mMainSlot = -1;
    private boolean mNeedSwitchRatCombine = true;
    private int[] mOldMainSwitchTypes = new int[CARD_MAX];
    private int mPollingCount = 0;
    private boolean[] mRadioOn = new boolean[CARD_MAX];
    private int[] mRatCombineMode = new int[CARD_MAX];
    private Message mSetCombineCompleteMsg;
    private int[] mSwitchTypes = new int[CARD_MAX];

    static {
        boolean equals;
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            equals = false;
        }
        IS_CHINA_TELECOM = equals;
        if (IS_CHINA_TELECOM) {
            z = SystemProperties.getBoolean("persist.radio.supp_oversea_usim", false);
        }
        IS_OVERSEA_USIM_SUPPORT = z;
    }

    public static HwForeignUsimForTelecom make(Context context, CommandsInterface[] ci) {
        HwForeignUsimForTelecom hwForeignUsimForTelecom;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("HwForeignUsimForTelecom.make() should only be called once");
            }
            mInstance = new HwForeignUsimForTelecom(context, ci);
            hwForeignUsimForTelecom = mInstance;
        }
        return hwForeignUsimForTelecom;
    }

    private HwForeignUsimForTelecom(Context context, CommandsInterface[] ci) {
        this.mCis = ci;
        this.mContext = context;
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = Integer.valueOf(i);
            this.mCis[i].registerForIccStatusChanged(this, EVENT_ICC_STATUS_CHANGED, index);
            this.mCis[i].registerForAvailable(this, EVENT_ICC_STATUS_CHANGED, index);
            this.mCis[i].registerForAvailable(this, EVENT_RADIO_AVAILABLE, index);
            this.mRatCombineMode[i] = -1;
            this.mSwitchTypes[i] = -1;
            this.mOldMainSwitchTypes[i] = -1;
            this.mGetUiccCardsStatusDone[i] = false;
            this.mIccIds[i] = null;
            this.mRadioOn[i] = false;
        }
        logi("HwForeignUsimForTelecom constructor!");
    }

    public static HwForeignUsimForTelecom getInstance() {
        HwForeignUsimForTelecom hwForeignUsimForTelecom;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("HwForeignUsimForTelecom.getInstance can't be called before make()");
            }
            hwForeignUsimForTelecom = mInstance;
        }
        return hwForeignUsimForTelecom;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x003e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        Integer index = getCiIndex(msg);
        AsyncResult ar = msg.obj;
        switch (msg.what) {
            case EVENT_RESTART_RILD /*101*/:
                logd("Received EVENT_RESTART_RILD on index " + index + ", mPollingCount = " + this.mPollingCount);
                try {
                    if (this.mPollingCount == 0) {
                        logd("Finally, restart rild...");
                        this.mIsOngoingRestartRild = true;
                        disposeCardStatus();
                        this.mCis[0].restartRild(null);
                        return;
                    }
                    return;
                } catch (RuntimeException e) {
                    return;
                }
            case EVENT_SET_RAT_COMBINE_MODE_DONE /*102*/:
                logd("Received EVENT_SET_RAT_COMBINE_MODE_DONE on index " + index);
                if (ar == null || ar.exception != null) {
                    loge("Error! setRatCommbie is failed!!");
                }
                waitToRestartRild();
                return;
            case EVENT_RADIO_AVAILABLE /*103*/:
                logd("Received EVENT_RADIO_AVAILABLE on index " + index);
                boolean ready = true;
                this.mRadioOn[index.intValue()] = true;
                boolean[] zArr = this.mRadioOn;
                int length = zArr.length;
                int i = 0;
                while (i < length) {
                    if (zArr[i]) {
                        i++;
                    } else {
                        ready = false;
                        if (!ready) {
                            if (this.mSetCombineCompleteMsg != null) {
                                logd("Switch Combine Mode Done!!");
                                AsyncResult.forMessage(this.mSetCombineCompleteMsg, Boolean.valueOf(true), null);
                                this.mSetCombineCompleteMsg.sendToTarget();
                                this.mSetCombineCompleteMsg = null;
                            }
                            this.mIsOngoingRestartRild = false;
                            return;
                        } else if (this.mSetCombineCompleteMsg != null) {
                            logd("clean iccids!!");
                            PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                            return;
                        } else {
                            return;
                        }
                    }
                }
                if (!ready) {
                }
            case EVENT_GET_RAT_COMBINE_MODE_DONE /*104*/:
                logd("Received EVENT_GET_RAT_COMBINE_MODE_DONE on index " + index);
                onGetRatCombineModeDone(ar, index);
                return;
            case EVENT_ICC_STATUS_CHANGED /*105*/:
                logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
                this.mCis[index.intValue()].getHwRatCombineMode(obtainMessage(EVENT_GET_RAT_COMBINE_MODE_DONE, index));
                return;
            case EVENT_CHECK_ALL_CARDS_READY /*106*/:
                logd("Received EVENT_CHECK_ALL_CARDS_READY on index " + index);
                checkIfAllCardsReady();
                return;
            case EVENT_SET_RAT_COMBINE_DONE /*107*/:
                logd("Received EVENT_SET_RAT_COMBINE_DONE on index " + index);
                HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(false);
                return;
            default:
                return;
        }
    }

    private void onGetRatCombineModeDone(AsyncResult ar, Integer index) {
        int slaveSlot = getAnotherSlotId(index.intValue());
        if (!(ar == null || ar.result == null)) {
            this.mRatCombineMode[index.intValue()] = ((int[]) ar.result)[0];
        }
        logd("mRatCombineMode[" + index + "] is " + this.mRatCombineMode[index.intValue()] + ", mRatCombineMode[" + slaveSlot + "] is " + this.mRatCombineMode[slaveSlot]);
        checkIfAllCardsReady();
        HwAllInOneController.getInstance().checkIfAllCardsReady();
    }

    public void onGetIccidDone(AsyncResult ar, Integer index) {
        logd("onGetIccidDone on index " + index);
        if (ar == null || ar.exception != null) {
            logd("get iccid exception, maybe card is absent. set iccid as \"\"");
            this.mIccIds[index.intValue()] = "";
            checkIfAllCardsReady();
            return;
        }
        byte[] data = ar.result;
        String iccid = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
        if (TextUtils.isEmpty(iccid) || 7 > iccid.length()) {
            logd("iccId is invalid, set it as \"\" ");
            this.mIccIds[index.intValue()] = "";
        } else {
            this.mIccIds[index.intValue()] = iccid.substring(0, 7);
        }
        logd("get iccid is " + SubscriptionInfo.givePrintableIccid(this.mIccIds[index.intValue()]) + " on index " + index);
        checkIfAllCardsReady();
    }

    public synchronized void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        int slaveSlot = getAnotherSlotId(index.intValue());
        this.mOldMainSwitchTypes[index.intValue()] = this.mSwitchTypes[index.intValue()];
        if (!(ar == null || ar.result == null)) {
            this.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[0] & 15;
            this.mCardTypes[index.intValue()] = (((int[]) ar.result)[0] & 240) >> 4;
        }
        checkIfAllCardsReady();
        logd("mSwitchTypes[" + index + "] = " + this.mSwitchTypes[index.intValue()] + ", mSwitchTypes[" + slaveSlot + "] = " + this.mSwitchTypes[slaveSlot]);
    }

    public void onGetBalongSimDone(AsyncResult ar, Integer index) {
        logd("onGetBalongSimDone");
        if (ar != null && ar.result != null && ((int[]) ar.result).length == 3) {
            int[] slots = ar.result;
            logd("slot result = " + Arrays.toString(slots));
            judgeBalongSimSlotFromResult(slots);
        } else if (ar == null || ar.result == null || ((int[]) ar.result).length != 2) {
            loge("onGetBalongSimDone error");
        } else {
            if (((int[]) ar.result)[1] + ((int[]) ar.result)[0] > 1) {
                this.mBalongSimSlot = ((int[]) ar.result)[0] - 1;
            } else {
                this.mBalongSimSlot = ((int[]) ar.result)[0];
            }
        }
        logd("mBalongSimSlot = " + this.mBalongSimSlot);
        checkIfAllCardsReady();
    }

    private void judgeBalongSimSlotFromResult(int[] slots) {
        if (slots[0] == 0 && slots[1] == 1 && slots[2] == 2) {
            this.mBalongSimSlot = 0;
        } else if (slots[0] == 1 && slots[1] == 0 && slots[2] == 2) {
            this.mBalongSimSlot = 1;
        } else if (slots[0] == 2 && slots[1] == 1 && slots[2] == 0) {
            this.mBalongSimSlot = 0;
        } else if (slots[0] == 2 && slots[1] == 0 && slots[2] == 1) {
            this.mBalongSimSlot = 1;
        } else {
            loge("onGetBalongSimDone invalid slot result");
        }
    }

    public synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        logd("mGetUiccCardsStatusDone on index " + index);
        if (ar.exception != null) {
            loge("Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error: " + ar.exception);
        } else if (isValidIndex(index.intValue())) {
            this.mGetUiccCardsStatusDone[index.intValue()] = true;
            Message msg = obtainMessage(EVENT_CHECK_ALL_CARDS_READY, index);
            AsyncResult.forMessage(msg, null, null);
            sendMessage(msg);
        } else {
            loge("onGetIccCardStatusDone: invalid index : " + index);
        }
    }

    public void checkIfAllCardsReady() {
        if (!HwModemCapability.isCapabilitySupport(9)) {
            if (HwVSimUtils.isVSimEnabled()) {
                logd("checkIfAllCardsReady, vsim is on and return");
                disposeLocalCardStatus();
                return;
            }
            boolean atLeastOneSlotHasSim = (-1 != this.mOldMainSwitchTypes[0] || -1 == this.mSwitchTypes[0] || this.mSwitchTypes[0] == 0) ? (-1 != this.mOldMainSwitchTypes[1] || -1 == this.mSwitchTypes[1]) ? false : this.mSwitchTypes[1] != 0 : true;
            if (atLeastOneSlotHasSim) {
                logd("checkIfAllCardsReady, set mNeedSwitchRatCombine as true.");
                this.mNeedSwitchRatCombine = true;
            }
            boolean ready = true;
            int i = 0;
            while (i < CARD_MAX) {
                if (-1 == this.mSwitchTypes[i]) {
                    logd("mSwitchTypes[" + i + "] is INVALID");
                    ready = false;
                    break;
                } else if (!this.mGetUiccCardsStatusDone[i]) {
                    logd("mGetUiccCardsStatusDone[" + i + "] is false");
                    ready = false;
                    break;
                } else if (this.mIccIds[i] == null) {
                    logd("mIccIds[" + i + "] is null");
                    ready = false;
                    break;
                } else if (-1 == this.mRatCombineMode[i]) {
                    logd("mRatCombineMode[" + i + "] is INVALID");
                    ready = false;
                    break;
                } else {
                    i++;
                }
            }
            if (-1 == this.mMainSlot) {
                logd("mMainSlot is not set");
                ready = false;
            }
            if (-1 == this.mBalongSimSlot) {
                logd("mBalongSimSlot is not set");
                ready = false;
            }
            this.mAllCardsReady = ready;
            logd("mAllCardsReady is " + this.mAllCardsReady);
            if (this.mAllCardsReady && this.mSetCombineCompleteMsg == null) {
                trySwitchCommrilMode();
            }
        }
    }

    private void trySwitchCommrilMode() {
        int foreignUCardSlot = getForeignCardSlot();
        boolean IS_OVERSEAS_MODE = SystemProperties.getBoolean(OVERSEAS_MODE, false);
        logd("trySwitchCommrilMode foreignUCardSlot: " + foreignUCardSlot + ", IS_OVERSEAS_MODE: " + IS_OVERSEAS_MODE);
        if (!(-1 == foreignUCardSlot || (IS_OVERSEAS_MODE ^ 1) == 0 || this.mRatCombineMode[this.mBalongSimSlot] != 0 || (isDomesticCard(this.mMainSlot) ^ 1) == 0)) {
            logd("trySwitchCommrilMode:need to set ratcombine:NOT_COMBINE on slot " + this.mBalongSimSlot);
            SystemProperties.set(OVERSEAS_MODE, "true");
            this.mSetCombineCompleteMsg = obtainMessage(EVENT_SET_RAT_COMBINE_DONE);
            this.mCis[this.mBalongSimSlot].setHwRatCombineMode(1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(this.mBalongSimSlot)));
            incPollingCount(1);
        }
        if (this.mPollingCount == 0) {
            this.mNeedSwitchRatCombine = false;
            logd("mPollingCount is 0");
            if (HwAllInOneController.getInstance().isSetDualCardSlotComplete()) {
                HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(false);
            }
        }
        logd("trySwitchCommrilMode end");
    }

    private int getAnotherSlotId(int slotId) {
        return slotId == 0 ? 1 : 0;
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

    private boolean isValidIndex(int index) {
        return index >= 0 && index < CARD_MAX;
    }

    public void disposeLocalCardStatus() {
        logd("disposeLocalCardStatus");
        for (int i = 0; i < this.mCis.length; i++) {
            this.mSwitchTypes[i] = -1;
            this.mOldMainSwitchTypes[i] = -1;
            this.mGetUiccCardsStatusDone[i] = false;
            this.mIccIds[i] = null;
            this.mRatCombineMode[i] = -1;
            this.mRadioOn[i] = false;
        }
        this.mBalongSimSlot = -1;
        this.mAllCardsReady = false;
        this.mNeedSwitchRatCombine = false;
    }

    private void disposeCardStatus() {
        logd("disposeCardStatus");
        disposeLocalCardStatus();
        for (int i = 0; i < this.mCis.length; i++) {
            this.mRatCombineMode[i] = -1;
        }
    }

    private int getUserSwitchDualCardSlots() {
        int subscription = 0;
        try {
            return System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            return subscription;
        }
    }

    private boolean isBalongSimSynced() {
        int currSlot = getUserSwitchDualCardSlots();
        logd("currSlot  = " + currSlot + ", mBalongSimSlot = " + this.mBalongSimSlot);
        return currSlot == this.mBalongSimSlot;
    }

    public void decPollingCount() {
        if (this.mPollingCount > 0) {
            this.mPollingCount--;
            logd("decPollingCount, mPollingCount = " + this.mPollingCount);
            return;
        }
        logd("decPollingCount already 0, can't dec!");
    }

    public void incPollingCount(int step) {
        this.mPollingCount += step;
        logd("incPollingCount, mPollingCount = " + this.mPollingCount);
    }

    public void waitToRestartRild() {
        logd("waitToRestartRild");
        decPollingCount();
        removeMessages(EVENT_RESTART_RILD);
        sendEmptyMessage(EVENT_RESTART_RILD);
    }

    public void setMainSlot(int slotId) {
        if (this.mIsOngoingRestartRild) {
            logd("Ready to restart rild, don't change mainSlot, wait till rild restarted!");
        } else {
            this.mMainSlot = slotId;
        }
        logd("setMainSlot is " + this.mMainSlot);
        if (!(slotId == this.mBalongSimSlot ? isBalongSimSynced() : false)) {
            incPollingCount(1);
        }
        checkIfAllCardsReady();
    }

    public boolean getRestartRildTag() {
        logd("get mIsOngoingRestartRild " + this.mIsOngoingRestartRild);
        return this.mIsOngoingRestartRild;
    }

    public boolean getSwitchRatCombineTag() {
        logd("get mNeedSwitchRatCombine " + this.mNeedSwitchRatCombine);
        return this.mNeedSwitchRatCombine;
    }

    public String getIccid(int slotId) {
        if (isValidIndex(slotId)) {
            return this.mIccIds[slotId];
        }
        return null;
    }

    public int getRatCombineMode(int slotId) {
        if (isValidIndex(slotId)) {
            return this.mRatCombineMode[slotId];
        }
        return -1;
    }

    public boolean isDomesticCard(int slotId) {
        if (!isValidIndex(slotId)) {
            return true;
        }
        if (HwVSimUtils.isVSimEnabled()) {
            logd("vsim is on and return true");
            return true;
        }
        boolean bDomesticCard;
        if (!TextUtils.isEmpty(this.mIccIds[slotId]) && (PREFIX_MII_ICCID.equals(this.mIccIds[slotId].substring(0, 2)) ^ 1) == 0) {
            HwAllInOneController.getInstance();
            if (!(HwAllInOneController.isCTCard(this.mIccIds[slotId]) || HwAllInOneController.getInstance().isCUCard(this.mIccIds[slotId]))) {
                bDomesticCard = HwAllInOneController.getInstance().isCMCCCard(this.mIccIds[slotId]);
                logd("bDomesticCard[" + slotId + "] is " + bDomesticCard);
                return bDomesticCard;
            }
        }
        bDomesticCard = true;
        logd("bDomesticCard[" + slotId + "] is " + bDomesticCard);
        return bDomesticCard;
    }

    public int getForeignCardSlot() {
        logd("getForeignCardSlot start");
        if (HwVSimUtils.isVSimEnabled()) {
            logd("getForeignCardSlot, vsim is on and return");
            return -1;
        } else if (!IS_CHINA_TELECOM) {
            logd("getForeignCardSlot, IS_CHINA_TELECOM is false and return -1.");
            return -1;
        } else if (this.mSwitchTypes[0] == 0 && this.mSwitchTypes[1] == 0) {
            logd("getForeignCardSlot, two cards are absent and return -1.");
            return -1;
        } else if (2 == this.mSwitchTypes[0] || 2 == this.mSwitchTypes[1] || 3 == this.mSwitchTypes[0] || 3 == this.mSwitchTypes[1]) {
            logd("getForeignCardSlot, got CDMA card and return -1.");
            return -1;
        } else if (this.mIccIds[0] == null || this.mIccIds[1] == null) {
            logd("getForeignCardSlot, got null mIccid and return -1.");
            return -1;
        } else if ((this.mSwitchTypes[0] == 0 || this.mIccIds[0].length() != 0) && (this.mSwitchTypes[1] == 0 || this.mIccIds[1].length() != 0)) {
            boolean bDomesticCard0 = isDomesticCard(0);
            boolean bDomesticCard1 = isDomesticCard(1);
            if ((1 == this.mSwitchTypes[0] && this.mSwitchTypes[1] == 0 && bDomesticCard0) || ((1 == this.mSwitchTypes[1] && this.mSwitchTypes[0] == 0 && bDomesticCard1) || (1 == this.mSwitchTypes[0] && bDomesticCard0 && 1 == this.mSwitchTypes[1] && bDomesticCard1))) {
                logd("getForeignCardSlot, got only home gsm card and return -1.");
                return -1;
            }
            int mainSlot = -1;
            if (bDomesticCard1 || bDomesticCard0) {
                if (bDomesticCard0) {
                    if (!bDomesticCard1) {
                        if (2 == this.mCardTypes[1]) {
                            mainSlot = isBalongSimSynced() ? 1 : 0;
                        } else if (1 == this.mCardTypes[1]) {
                            mainSlot = isBalongSimSynced() ? 1 : 0;
                        }
                    }
                } else if (2 == this.mCardTypes[0]) {
                    mainSlot = isBalongSimSynced() ? 0 : 1;
                } else if (1 == this.mCardTypes[0]) {
                    mainSlot = isBalongSimSynced() ? 0 : 1;
                }
            } else if (2 == this.mCardTypes[0] && 2 == this.mCardTypes[1]) {
                mainSlot = getUserSwitchDualCardSlots() == 0 ? 0 : 1;
            } else if (2 == this.mCardTypes[0]) {
                mainSlot = isBalongSimSynced() ? 0 : 1;
            } else if (2 == this.mCardTypes[1]) {
                mainSlot = isBalongSimSynced() ? 1 : 0;
            } else if (1 == this.mCardTypes[0] && 1 == this.mCardTypes[1]) {
                mainSlot = getUserSwitchDualCardSlots() == 0 ? 0 : 1;
            } else if (1 == this.mCardTypes[0]) {
                mainSlot = isBalongSimSynced() ? 0 : 1;
            } else if (1 == this.mCardTypes[1]) {
                mainSlot = isBalongSimSynced() ? 1 : 0;
            }
            logd("mainSlot is" + mainSlot);
            return mainSlot;
        } else {
            logd("getForeignCardSlot, got miccid failure and return -1.");
            return -1;
        }
    }

    private static void logd(String message) {
        Rlog.d(TAG, message);
    }

    private static void logi(String message) {
        Rlog.i(TAG, message);
    }

    private static void loge(String message) {
        Rlog.e(TAG, message);
    }
}
