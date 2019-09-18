package com.android.internal.telephony.fullnetwork;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwDsdsController;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.Arrays;

public abstract class HwFullNetworkInitStateHisiBase extends HwFullNetworkInitStateBase {
    private static final String LOG_TAG = "HwFullNetworkInitStateHisiBase";
    protected HwFullNetworkChipHisi mChipHisi = HwFullNetworkChipHisi.getInstance();
    protected HwHotplugController mHotPlugController;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFullNetworkInitStateHisiBase.this.loge("intent is null, return");
                return;
            }
            if ("com.huawei.intent.action.ACTION_SIM_STATE_CHANGED".equals(intent.getAction())) {
                HwFullNetworkInitStateHisiBase.this.processSimStateChanged(intent);
            }
        }
    };

    public HwFullNetworkInitStateHisiBase(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        initParams();
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            this.mHotPlugController = HwHotplugController.getInstance();
        }
        for (int i = 0; i < this.mCis.length; i++) {
            this.mCis[i].registerForSimHotPlug(this, HwFullNetworkConstants.EVENT_SIM_HOTPLUG, Integer.valueOf(i));
        }
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("com.huawei.intent.action.ACTION_SIM_STATE_CHANGED"));
    }

    private void initParams() {
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            this.mChipHisi.mGetUiccCardsStatusDone[i] = false;
            this.mChipHisi.mGetBalongSimSlotDone[i] = false;
            this.mChipHisi.mSwitchTypes[i] = -1;
            this.mChipHisi.mCardTypes[i] = -1;
            this.mChipHisi.mRadioOns[i] = false;
            mChipCommon.mIccIds[i] = null;
            mChipCommon.subCarrierTypeArray[i] = null;
            this.mChipHisi.mHotplugState[i] = HwFullNetworkConstants.HotplugState.STATE_PLUG_OUT;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v28, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.os.AsyncResult} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void handleMessage(Message msg) {
        if (msg == null) {
            loge("msg is null, return!");
            return;
        }
        Integer index = mChipCommon.getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        AsyncResult ar = null;
        if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
            ar = msg.obj;
        }
        switch (msg.what) {
            case 1001:
                logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
                onIccStatusChanged(index);
                break;
            case HwFullNetworkConstants.EVENT_QUERY_CARD_TYPE_DONE:
                logd("Received EVENT_QUERY_CARD_TYPE_DONE on index " + index);
                if (ar != null && ar.exception == null) {
                    onQueryCardTypeDone(ar, index);
                    if (this.mHotPlugController != null) {
                        this.mHotPlugController.onHotPlugQueryCardTypeDone(ar, index);
                        break;
                    }
                } else {
                    logd("Received EVENT_QUERY_CARD_TYPE_DONE got exception, ar  = " + ar);
                    break;
                }
                break;
            case HwFullNetworkConstants.EVENT_GET_BALONG_SIM_DONE:
                logd("Received EVENT_GET_BALONG_SIM_DONE on index " + index);
                if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
                    HwDsdsController.getInstance().onGetBalongSimDone(ar, index);
                }
                onGetBalongSimDone(ar, index);
                break;
            case HwFullNetworkConstants.EVENT_SIM_HOTPLUG:
                logd("Received EVENT_SIM_HOTPLUG on index " + index);
                onSimHotPlug(ar, index);
                break;
            case HwFullNetworkConstants.EVENT_GET_ICCID_DONE:
                logd("Received EVENT_GET_ICCID_DONE on index " + index);
                onGetIccidDone(ar, index);
                break;
            case HwFullNetworkConstants.EVENT_GET_ICC_STATUS_DONE:
                logd("Received EVENT_GET_ICC_STATUS_DONE on index " + index);
                onGetIccStatusDone(index);
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

    public void onIccStatusChanged(Integer index) {
        this.mCis[index.intValue()].getICCID(obtainMessage(HwFullNetworkConstants.EVENT_GET_ICCID_DONE, index));
        this.mCis[index.intValue()].queryCardType(obtainMessage(HwFullNetworkConstants.EVENT_QUERY_CARD_TYPE_DONE, index));
        this.mCis[index.intValue()].getBalongSim(obtainMessage(HwFullNetworkConstants.EVENT_GET_BALONG_SIM_DONE, index));
        if (this.mHotPlugController != null) {
            this.mHotPlugController.onHotPlugIccStatusChanged(index);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x006c A[Catch:{ all -> 0x005f }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x007e A[Catch:{ all -> 0x005f }] */
    public synchronized void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        int countGetCardTypeDone;
        if (ar != null) {
            try {
                if (ar.result != null) {
                    this.mChipHisi.mCardTypes[index.intValue()] = ((int[]) ar.result)[0];
                    this.mChipHisi.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[0] & 15;
                    saveCardTypeProperties(((int[]) ar.result)[0], index.intValue());
                    HwVSimUtils.updateSimCardTypes(this.mChipHisi.mSwitchTypes);
                    if (!this.mChipHisi.mBroadcastDone && !HwHotplugController.IS_HOTSWAP_SUPPORT && HwFullNetworkConfig.IS_CHINA_TELECOM && isNoneCTcard()) {
                        this.mChipHisi.mBroadcastDone = true;
                        broadcastForHwCardManager(index.intValue());
                    }
                    countGetCardTypeDone = 0;
                    for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
                        if (this.mChipHisi.mSwitchTypes[i] != -1) {
                            countGetCardTypeDone++;
                        }
                    }
                    if (countGetCardTypeDone == HwFullNetworkConstants.SIM_NUM) {
                        logd("onQueryCardTypeDone check main slot.");
                        this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, index).sendToTarget();
                    }
                }
            } catch (Throwable ar2) {
                throw ar2;
            }
        }
        loge("onQueryCardTypeDone error.");
        countGetCardTypeDone = 0;
        while (i < HwFullNetworkConstants.SIM_NUM) {
        }
        if (countGetCardTypeDone == HwFullNetworkConstants.SIM_NUM) {
        }
    }

    public void onGetBalongSimDone(AsyncResult ar, Integer index) {
        if (ar != null && ar.result != null && ((int[]) ar.result).length == 3) {
            int[] slots = (int[]) ar.result;
            boolean isMainSlotOnVSim = false;
            logd("slot result = " + Arrays.toString(slots));
            if (slots[0] == 0 && slots[1] == 1 && slots[2] == 2) {
                this.mChipHisi.mBalongSimSlot = 0;
                isMainSlotOnVSim = false;
            } else if (slots[0] == 1 && slots[1] == 0 && slots[2] == 2) {
                this.mChipHisi.mBalongSimSlot = 1;
                isMainSlotOnVSim = false;
            } else if (slots[0] == 2 && slots[1] == 1 && slots[2] == 0) {
                this.mChipHisi.mBalongSimSlot = 0;
                isMainSlotOnVSim = true;
            } else if (slots[0] == 2 && slots[1] == 0 && slots[2] == 1) {
                this.mChipHisi.mBalongSimSlot = 1;
                isMainSlotOnVSim = true;
            } else {
                loge("onGetBalongSimDone invalid slot result");
            }
            logd("isMainSlotOnVSim = " + isMainSlotOnVSim);
            this.mChipHisi.mGetBalongSimSlotDone[index.intValue()] = true;
        } else if (ar == null || ar.result == null || ((int[]) ar.result).length != 2) {
            loge("onGetBalongSimDone error");
        } else {
            if (((int[]) ar.result)[0] + ((int[]) ar.result)[1] > 1) {
                this.mChipHisi.mBalongSimSlot = ((int[]) ar.result)[0] - 1;
            } else {
                this.mChipHisi.mBalongSimSlot = ((int[]) ar.result)[0];
            }
            this.mChipHisi.mGetBalongSimSlotDone[index.intValue()] = true;
        }
        logd("mBalongSimSlot = " + this.mChipHisi.mBalongSimSlot);
        int countGetBalongSimSlotDone = 0;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (this.mChipHisi.mGetBalongSimSlotDone[i]) {
                countGetBalongSimSlotDone++;
            }
        }
        if (countGetBalongSimSlotDone == 1) {
            logd("onGetBalongSimDone check main slot.");
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, index).sendToTarget();
        }
    }

    public void onGetIccidDone(AsyncResult ar, Integer index) {
        if (ar == null || ar.exception != null) {
            if (ar == null || !(ar.exception instanceof CommandException) || ar.exception.getCommandError() != CommandException.Error.RADIO_NOT_AVAILABLE) {
                logd("get iccid exception, maybe card is absent. set iccid as \"\"");
                mChipCommon.mIccIds[index.intValue()] = "";
                this.mChipHisi.mFullIccIds[index.intValue()] = "";
            } else {
                logd("get iccid radio not available exception, Do Nothing.");
            }
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, index).sendToTarget();
            return;
        }
        byte[] data = (byte[]) ar.result;
        String iccid = HwIccUtils.bcdIccidToString(data, 0, data.length);
        if (TextUtils.isEmpty(iccid) || 7 > iccid.length()) {
            logd("iccId is invalid, set it as \"\" ");
            mChipCommon.mIccIds[index.intValue()] = "";
        } else {
            mChipCommon.mIccIds[index.intValue()] = iccid.substring(0, 7);
        }
        this.mChipHisi.mFullIccIds[index.intValue()] = iccid;
        logd("get iccid is " + SubscriptionInfo.givePrintableIccid(mChipCommon.mIccIds[index.intValue()]) + " on index " + index);
        int countGetIccIdDone = 0;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (mChipCommon.mIccIds[i] != null) {
                countGetIccIdDone++;
            }
        }
        if (countGetIccIdDone == HwFullNetworkConstants.SIM_NUM) {
            logd("onGetIccidDone check main slot.");
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, index).sendToTarget();
        }
    }

    public void onSimHotPlug(AsyncResult ar, Integer index) {
        logd("onSimHotPlug");
        if (ar != null && ar.result != null && ((int[]) ar.result).length > 0) {
            if (HwFullNetworkConstants.HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[0]) {
                HwVSimUtils.simHotPlugIn(index.intValue());
                if (this.mChipHisi.mHotplugState[index.intValue()] != HwFullNetworkConstants.HotplugState.STATE_PLUG_IN) {
                    this.mChipHisi.isHotPlugCompleted = true;
                    disposeCardStatus(index.intValue());
                }
            } else if (HwFullNetworkConstants.HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.result)[0]) {
                HwVSimUtils.simHotPlugOut(index.intValue());
                this.mChipHisi.mHotplugState[index.intValue()] = HwFullNetworkConstants.HotplugState.STATE_PLUG_OUT;
                this.mChipHisi.needFixMainSlotPosition = false;
            }
        }
    }

    public void disposeCardStatus(int slotID) {
        logd("disposeCardStatus slotid = " + slotID);
        if (slotID >= 0 && slotID < HwFullNetworkConstants.SIM_NUM) {
            this.mChipHisi.mHotplugState[slotID] = HwFullNetworkConstants.HotplugState.STATE_PLUG_IN;
            this.mChipHisi.mSwitchTypes[slotID] = -1;
            this.mChipHisi.mGetUiccCardsStatusDone[slotID] = false;
            this.mChipHisi.mGetBalongSimSlotDone[slotID] = false;
            this.mChipHisi.mCardTypes[slotID] = -1;
            this.mChipHisi.mFullIccIds[slotID] = null;
            this.mChipHisi.mAllCardsReady = false;
            this.mChipHisi.mNvRestartRildDone = false;
            mChipCommon.mIccIds[slotID] = null;
            mChipCommon.subCarrierTypeArray[slotID] = null;
            this.mChipHisi.needFixMainSlotPosition = false;
            if (HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE) {
                saveIccidBySlot(slotID, "");
            }
            if (HwFullNetworkConfig.IS_HISI_DSDX) {
                logd("set mAutoSwitchDualCardsSlotDone to false");
                this.mChipHisi.mAutoSwitchDualCardsSlotDone = false;
                if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && HwFullNetworkConfig.IS_VICE_WCDMA) {
                    Phone phone = PhoneFactory.getPhone(slotID);
                    if (phone != null) {
                        logd("disposeCardStatus: set network mode to NETWORK_MODE_GSM_UMTS");
                        phone.setPreferredNetworkType(3, null);
                    }
                }
            }
        }
    }

    private void saveCardTypeProperties(int cardTypeResult, int index) {
        int cardType = -1;
        int uiccOrIcc = (cardTypeResult & 240) >> 4;
        int appType = cardTypeResult & 15;
        switch (appType) {
            case 1:
                if (uiccOrIcc != 2) {
                    if (uiccOrIcc == 1) {
                        cardType = 10;
                        break;
                    }
                } else {
                    cardType = 20;
                    break;
                }
                break;
            case 2:
                cardType = 30;
                break;
            case 3:
                if (uiccOrIcc != 2) {
                    if (uiccOrIcc == 1) {
                        cardType = 41;
                        break;
                    }
                } else {
                    cardType = 43;
                    break;
                }
                break;
        }
        logd("uiccOrIcc :  " + uiccOrIcc + ", appType : " + appType + ", cardType : " + cardType);
        if (index == 0) {
            SystemProperties.set(HwFullNetworkConstants.CARD_TYPE_SIM1, String.valueOf(cardType));
        } else {
            SystemProperties.set(HwFullNetworkConstants.CARD_TYPE_SIM2, String.valueOf(cardType));
        }
    }

    private void saveIccidBySlot(int slot, String iccId) {
        logd("saveIccidBySlot");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        String iccIdToSave = "";
        try {
            iccIdToSave = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, iccId);
        } catch (Exception ex) {
            logd("HwAESCryptoUtil encrypt excepiton:" + ex.getMessage());
        }
        editor.putString("4G_AUTO_SWITCH_ICCID_SLOT" + slot, iccIdToSave);
        editor.apply();
    }

    private boolean isNoneCTcard() {
        boolean result = false;
        if (!HwFullNetworkConfig.IS_4G_SWITCH_SUPPORTED) {
            if (this.mChipHisi.mSwitchTypes[mChipCommon.getUserSwitchDualCardSlots()] == 1) {
                result = true;
            }
            return result;
        }
        if (this.mChipHisi.mSwitchTypes[0] == 1 && this.mChipHisi.mSwitchTypes[1] == 1) {
            result = true;
        }
        return result;
    }

    private void broadcastForHwCardManager(int sub) {
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        logd("[broadcastForHwCardManager]");
        intent.putExtra("popupDialog", "true");
        ActivityManager.broadcastStickyIntent(intent, 51, -1);
    }

    public void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        logd("onGetIccCardStatusDone on index " + index);
        if (ar.exception != null) {
            loge("Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error: " + ar.exception);
        } else if (!mChipCommon.isValidIndex(index.intValue())) {
            loge("onGetIccCardStatusDone: invalid index : " + index);
        } else {
            this.mChipHisi.mGetUiccCardsStatusDone[index.intValue()] = true;
            if (this.mHotPlugController != null) {
                this.mHotPlugController.processNotifyPromptHotPlug(false);
            }
            sendMessage(obtainMessage(HwFullNetworkConstants.EVENT_GET_ICC_STATUS_DONE, ar));
        }
    }

    private void onGetIccStatusDone(Integer index) {
        logd("onGetIccStatusDone check main slot.");
        this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, index).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void processSimStateChanged(Intent intent) {
        if (HwFullNetworkConfig.IS_CMCC_4G_DSDX_ENABLE) {
            String simState = intent.getStringExtra("ss");
            char c = 65535;
            int slotId = intent.getIntExtra("slot", -1);
            logd("processSimStateChanged simState:" + simState + ", slotId:" + slotId);
            if (simState != null && mChipCommon.isValidIndex(slotId)) {
                switch (simState.hashCode()) {
                    case -2044123382:
                        if (simState.equals("LOCKED")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1971794228:
                        if (simState.equals("INTERNAL_LOCKED")) {
                            c = 5;
                            break;
                        }
                        break;
                    case -1830845986:
                        if (simState.equals("CARD_IO_ERROR")) {
                            c = 6;
                            break;
                        }
                        break;
                    case -1733499378:
                        if (simState.equals("NETWORK")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 79221:
                        if (simState.equals("PIN")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 79590:
                        if (simState.equals("PUK")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 2251386:
                        if (simState.equals("IMSI")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 433141802:
                        if (simState.equals("UNKNOWN")) {
                            c = 10;
                            break;
                        }
                        break;
                    case 1034051831:
                        if (simState.equals("NOT_READY")) {
                            c = 9;
                            break;
                        }
                        break;
                    case 1599753450:
                        if (simState.equals("CARD_RESTRICTED")) {
                            c = 7;
                            break;
                        }
                        break;
                    case 1924388665:
                        if (simState.equals("ABSENT")) {
                            c = 8;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        onImsiReady(slotId);
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        onLocked(slotId);
                        break;
                    case 8:
                    case 9:
                    case 10:
                        this.mChipHisi.setCardReadyState(slotId, false);
                        break;
                }
            }
        }
    }

    private void onImsiReady(int slotId) {
        logd("onImsiReady slotId: " + slotId);
        this.mChipHisi.refreshCardState();
        if (mChipCommon.subCarrierTypeArray[slotId] == null) {
            mChipCommon.judgeSubCarrierType();
        }
        HwFullNetworkConstants.SubCarrierType[] oldsubCarrierTypes = (HwFullNetworkConstants.SubCarrierType[]) mChipCommon.subCarrierTypeArray.clone();
        for (int sub = 0; sub < HwFullNetworkConstants.SIM_NUM; sub++) {
            mChipCommon.judgeSubCarrierTypeByMccMnc(sub);
            logd("processSimStateChanged oldsubCarrierTypes[" + sub + "]:" + oldsubCarrierTypes[sub] + ", newsubCarrierTypes[" + sub + "]:" + mChipCommon.subCarrierTypeArray[sub]);
            if (oldsubCarrierTypes[sub] != mChipCommon.subCarrierTypeArray[sub]) {
                this.mChipHisi.needFixMainSlotPosition = true;
            }
        }
        this.mChipHisi.setCardReadyState(slotId, true);
        if (!mChipCommon.isSet4GSlotInProgress) {
            logd("onImsiReady send event EVENT_CHECK_MAIN_SLOT");
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, 0).sendToTarget();
        }
    }

    private void onLocked(int slotId) {
        logd("onLocked slotId: " + slotId);
        this.mChipHisi.setCardReadyState(slotId, true);
        if (!mChipCommon.isSet4GSlotInProgress) {
            logd("onLocked send event EVENT_CHECK_MAIN_SLOT");
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, 0).sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
