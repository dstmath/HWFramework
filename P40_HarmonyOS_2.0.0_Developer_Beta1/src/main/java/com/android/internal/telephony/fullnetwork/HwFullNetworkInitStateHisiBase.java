package com.android.internal.telephony.fullnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwHotplugControllerImpl;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionInfoEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import java.util.Arrays;

public abstract class HwFullNetworkInitStateHisiBase extends HwFullNetworkInitStateBase {
    private static final String LOG_TAG = "HwFullNetworkInitStateHisiBase";
    protected HwFullNetworkChipHisi mChipHisi = HwFullNetworkChipHisi.getInstance();
    protected HwHotplugControllerImpl mHotPlugController;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateHisiBase.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFullNetworkInitStateHisiBase.this.loge("intent is null, return");
            } else if ("com.huawei.intent.action.ACTION_SIM_STATE_CHANGED".equals(intent.getAction())) {
                HwFullNetworkInitStateHisiBase.this.processSimStateChanged(intent);
            }
        }
    };

    public HwFullNetworkInitStateHisiBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        initParams();
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            this.mHotPlugController = HwHotplugControllerImpl.getInstance();
        }
        for (int i = 0; i < this.mCis.length; i++) {
            this.mCis[i].registerForSimHotPlug(this, (int) HwFullNetworkConstantsInner.EVENT_SIM_HOTPLUG, Integer.valueOf(i));
        }
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("com.huawei.intent.action.ACTION_SIM_STATE_CHANGED"));
    }

    private void initParams() {
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            this.mChipHisi.mGetUiccCardsStatusDone[i] = false;
            this.mChipHisi.mGetBalongSimSlotDone[i] = false;
            this.mChipHisi.mSwitchTypes[i] = -1;
            this.mChipHisi.mCardTypes[i] = -1;
            this.mChipHisi.mRadioOns[i] = false;
            mChipCommon.mIccIds[i] = null;
            mChipCommon.subCarrierTypeArray[i] = null;
            this.mChipHisi.mHotplugState[i] = HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_OUT;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase, android.os.Handler
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
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int i = msg.what;
        if (i == 1001) {
            logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
            onIccStatusChanged(index);
        } else if (i == 1012) {
            logd("Received EVENT_GET_ICC_STATUS_DONE on index " + index);
            onGetIccStatusDone(index);
        } else if (i == 1005) {
            logd("Received EVENT_QUERY_CARD_TYPE_DONE on index " + index);
            if (ar == null || ar.getException() != null) {
                logd("Received EVENT_QUERY_CARD_TYPE_DONE got exception, ar  = " + ar);
                return;
            }
            onQueryCardTypeDone(ar, index);
            HwHotplugControllerImpl hwHotplugControllerImpl = this.mHotPlugController;
            if (hwHotplugControllerImpl != null) {
                hwHotplugControllerImpl.onHotPlugQueryCardTypeDone(ar, index);
            }
        } else if (i == 1006) {
            logd("Received EVENT_GET_BALONG_SIM_DONE on index " + index);
            onGetBalongSimDone(ar, index);
        } else if (i == 1008) {
            logd("Received EVENT_SIM_HOTPLUG on index " + index);
            onSimHotPlug(ar, index);
        } else if (i != 1009) {
            super.handleMessage(msg);
        } else {
            logd("Received EVENT_GET_ICCID_DONE on index " + index);
            onGetIccidDone(ar, index);
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void onIccStatusChanged(Integer index) {
        this.mCis[index.intValue()].getICCID(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_ICCID_DONE, index));
        this.mCis[index.intValue()].queryCardType(obtainMessage(HwFullNetworkConstantsInner.EVENT_QUERY_CARD_TYPE_DONE, index));
        this.mCis[index.intValue()].getBalongSim(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_BALONG_SIM_DONE, index));
        if (this.mChipHisi.isEuiccInSlot2() && index.intValue() == 1) {
            logd("euicc in slot2, set mGetUiccCardsStatusDone to false");
            this.mChipHisi.mGetUiccCardsStatusDone[index.intValue()] = false;
        }
        HwHotplugControllerImpl hwHotplugControllerImpl = this.mHotPlugController;
        if (hwHotplugControllerImpl != null) {
            hwHotplugControllerImpl.onHotPlugIccStatusChanged(index);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0094  */
    public synchronized void onQueryCardTypeDone(AsyncResultEx ar, Integer index) {
        int countGetCardTypeDone;
        if (ar != null) {
            if (ar.getResult() != null) {
                this.mChipHisi.mCardTypes[index.intValue()] = ((int[]) ar.getResult())[0];
                this.mChipHisi.mSwitchTypes[index.intValue()] = ((int[]) ar.getResult())[0] & 15;
                saveCardTypeProperties(((int[]) ar.getResult())[0], index.intValue());
                if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
                    HwVSimUtils.updateSimCardTypes(this.mChipHisi.mSwitchTypes);
                }
                if (!this.mChipHisi.mBroadcastDone && !HwHotplugController.IS_HOTSWAP_SUPPORT && HwFullNetworkConfigInner.IS_CHINA_TELECOM && isNoneCTcard()) {
                    this.mChipHisi.mBroadcastDone = true;
                    broadcastForHwCardManager(index.intValue());
                }
                countGetCardTypeDone = 0;
                for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                    if (this.mChipHisi.mSwitchTypes[i] != -1) {
                        countGetCardTypeDone++;
                    }
                }
                if (countGetCardTypeDone == HwFullNetworkConstantsInner.SIM_NUM) {
                    logd("onQueryCardTypeDone check main slot.");
                    this.mStateHandler.obtainMessage(201, index).sendToTarget();
                }
            }
        }
        loge("onQueryCardTypeDone error.");
        countGetCardTypeDone = 0;
        while (i < HwFullNetworkConstantsInner.SIM_NUM) {
        }
        if (countGetCardTypeDone == HwFullNetworkConstantsInner.SIM_NUM) {
        }
    }

    public void onGetBalongSimDone(AsyncResultEx ar, Integer index) {
        if (ar != null && ar.getResult() != null && ((int[]) ar.getResult()).length == 3) {
            int[] slots = (int[]) ar.getResult();
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
        } else if (ar == null || ar.getResult() == null || ((int[]) ar.getResult()).length != 2) {
            loge("onGetBalongSimDone error");
        } else {
            if (((int[]) ar.getResult())[0] + ((int[]) ar.getResult())[1] > 1) {
                this.mChipHisi.mBalongSimSlot = ((int[]) ar.getResult())[0] - 1;
            } else {
                this.mChipHisi.mBalongSimSlot = ((int[]) ar.getResult())[0];
            }
            this.mChipHisi.mGetBalongSimSlotDone[index.intValue()] = true;
        }
        logd("mBalongSimSlot = " + this.mChipHisi.mBalongSimSlot);
        int countGetBalongSimSlotDone = 0;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (this.mChipHisi.mGetBalongSimSlotDone[i]) {
                countGetBalongSimSlotDone++;
            }
        }
        if (countGetBalongSimSlotDone == 1) {
            logd("onGetBalongSimDone check main slot.");
            this.mStateHandler.obtainMessage(201, index).sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void onGetIccidDone(AsyncResultEx ar, Integer index) {
        boolean isRadioNotAvaiable = false;
        if (ar == null || ar.getException() != null) {
            if (ar != null && CommandExceptionEx.isSpecificError(ar.getException(), CommandExceptionEx.Error.RADIO_NOT_AVAILABLE)) {
                isRadioNotAvaiable = true;
            }
            if (isRadioNotAvaiable) {
                logd("get iccid radio not available exception, Do Nothing.");
            } else {
                logd("get iccid exception, maybe card is absent. set iccid as \"\"");
                mChipCommon.mIccIds[index.intValue()] = BuildConfig.FLAVOR;
                this.mChipHisi.mFullIccIds[index.intValue()] = BuildConfig.FLAVOR;
            }
            this.mStateHandler.obtainMessage(201, index).sendToTarget();
            return;
        }
        byte[] data = (byte[]) ar.getResult();
        String iccid = HwIccUtils.bcdIccidToString(data, 0, data.length);
        if (TextUtils.isEmpty(iccid) || 7 > iccid.length()) {
            logd("iccId is invalid, set it as \"\" ");
            mChipCommon.mIccIds[index.intValue()] = BuildConfig.FLAVOR;
        } else {
            mChipCommon.mIccIds[index.intValue()] = iccid.substring(0, 7);
        }
        this.mChipHisi.mFullIccIds[index.intValue()] = iccid;
        logd("get iccid is " + SubscriptionInfoEx.givePrintableIccid(mChipCommon.mIccIds[index.intValue()]) + " on " + index);
        int countGetIccIdDone = 0;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (mChipCommon.mIccIds[i] != null) {
                countGetIccIdDone++;
            }
        }
        if (countGetIccIdDone == HwFullNetworkConstantsInner.SIM_NUM) {
            logd("onGetIccidDone check main slot.");
            this.mStateHandler.obtainMessage(201, index).sendToTarget();
        }
    }

    public void onSimHotPlug(AsyncResultEx ar, Integer index) {
        logd("onSimHotPlug");
        if (ar != null && ar.getResult() != null && ((int[]) ar.getResult()).length > 0) {
            if (HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.getResult())[0]) {
                if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
                    HwVSimUtils.simHotPlugIn(index.intValue());
                }
                if (this.mChipHisi.mHotplugState[index.intValue()] != HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_IN) {
                    this.mChipHisi.isHotPlugCompleted = true;
                    disposeCardStatus(index.intValue());
                }
            } else if (HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.getResult())[0]) {
                if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
                    HwVSimUtils.simHotPlugOut(index.intValue());
                }
                this.mChipHisi.mHotplugState[index.intValue()] = HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_OUT;
                this.mChipHisi.needFixMainSlotPosition = false;
            } else {
                logd("onSimHotPlug result state invalid.");
            }
        }
    }

    public void disposeCardStatus(int slotID) {
        logd("disposeCardStatus slotid = " + slotID);
        if (slotID >= 0 && slotID < HwFullNetworkConstantsInner.SIM_NUM) {
            mChipCommon.mSubscriptionStatus[slotID] = -1;
            this.mChipHisi.mHotplugState[slotID] = HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_IN;
            this.mChipHisi.mSwitchTypes[slotID] = -1;
            this.mChipHisi.mGetUiccCardsStatusDone[slotID] = false;
            this.mChipHisi.mGetBalongSimSlotDone[slotID] = false;
            this.mChipHisi.mCardTypes[slotID] = -1;
            this.mChipHisi.mFullIccIds[slotID] = null;
            HwFullNetworkChipHisi hwFullNetworkChipHisi = this.mChipHisi;
            hwFullNetworkChipHisi.mAllCardsReady = false;
            hwFullNetworkChipHisi.mNvRestartRildDone = false;
            mChipCommon.mIccIds[slotID] = null;
            mChipCommon.subCarrierTypeArray[slotID] = null;
            this.mChipHisi.needFixMainSlotPosition = false;
            if (HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2) {
                saveIccidBySlot(slotID, BuildConfig.FLAVOR);
            }
            if (HwFullNetworkConfigInner.IS_HISI_DSDX) {
                setHisiDsdxPreferredNetworkType(slotID);
            }
        }
    }

    private void setHisiDsdxPreferredNetworkType(int slotId) {
        PhoneExt PhoneExt;
        logd("set mAutoSwitchDualCardsSlotDone to false");
        this.mChipHisi.mAutoSwitchDualCardsSlotDone = false;
        if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && HwFullNetworkConfigInner.IS_VICE_WCDMA && (PhoneExt = PhoneFactoryExt.getPhone(slotId)) != null) {
            logd("disposeCardStatus: set network mode to NETWORK_MODE_GSM_UMTS");
            PhoneExt.setPreferredNetworkType(3, (Message) null);
        }
    }

    private void saveCardTypeProperties(int cardTypeResult, int index) {
        int cardType = -1;
        int uiccOrIcc = (cardTypeResult & 240) >> 4;
        int appType = cardTypeResult & 15;
        if (appType != 1) {
            if (appType == 2) {
                cardType = 30;
            } else if (appType == 3) {
                if (uiccOrIcc == 2) {
                    cardType = 43;
                } else if (uiccOrIcc == 1) {
                    cardType = 41;
                } else {
                    logd("not uicc or icc card.");
                }
            }
        } else if (uiccOrIcc == 2) {
            cardType = 20;
        } else if (uiccOrIcc == 1) {
            cardType = 10;
        } else {
            logd("not uicc or icc card.");
        }
        logd("uiccOrIcc :  " + uiccOrIcc + ", appType : " + appType + ", cardType : " + cardType);
        if (index == 0) {
            SystemPropertiesEx.set(HwFullNetworkConstantsInner.CARD_TYPE_SIM1, String.valueOf(cardType));
        } else {
            SystemPropertiesEx.set(HwFullNetworkConstantsInner.CARD_TYPE_SIM2, String.valueOf(cardType));
        }
    }

    private void saveIccidBySlot(int slot, String iccId) {
        logd("saveIccidBySlot");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        String iccIdToSave = BuildConfig.FLAVOR;
        try {
            iccIdToSave = HwAESCryptoUtil.encrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, iccId);
        } catch (IllegalArgumentException e) {
            logd("HwAESCryptoUtil decrypt IllegalArgumentException slot.");
        } catch (Exception e2) {
            logd("HwAESCryptoUtil encrypt excepiton");
        }
        editor.putString("4G_AUTO_SWITCH_ICCID_SLOT" + slot, iccIdToSave);
        editor.apply();
    }

    private boolean isNoneCTcard() {
        boolean result = false;
        if (!HwFullNetworkConfigInner.IS_4G_SWITCH_SUPPORTED) {
            return this.mChipHisi.mSwitchTypes[mChipCommon.getUserSwitchDualCardSlots()] == 1;
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
        ActivityManagerEx.broadcastStickyIntent(intent, 51, -1);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void onGetIccCardStatusDone(Object ar, Integer index) {
        logd("onGetIccCardStatusDone on index " + index);
        AsyncResultEx asyncResultEx = AsyncResultEx.from(ar);
        if (asyncResultEx != null) {
            if (asyncResultEx.getException() != null) {
                loge("Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error: " + asyncResultEx.getException());
            } else if (!mChipCommon.isValidIndex(index.intValue())) {
                loge("onGetIccCardStatusDone: invalid index : " + index);
            } else {
                this.mChipHisi.mGetUiccCardsStatusDone[index.intValue()] = true;
                HwHotplugControllerImpl hwHotplugControllerImpl = this.mHotPlugController;
                if (hwHotplugControllerImpl != null) {
                    hwHotplugControllerImpl.processNotifyPromptHotPlug(false);
                }
                sendMessage(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_ICC_STATUS_DONE, ar));
            }
        }
    }

    private void onGetIccStatusDone(Integer index) {
        logd("onGetIccStatusDone check main slot.");
        this.mStateHandler.obtainMessage(201, index).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c3, code lost:
        if (r0.equals("LOCKED") != false) goto L_0x00c7;
     */
    private void processSimStateChanged(Intent intent) {
        char c = 1;
        if (HwFullNetworkConfigInner.isCMCCDsdxEnable() || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1) {
            String simState = intent.getStringExtra("ss");
            int slotId = intent.getIntExtra("phone", -1);
            logd("processSimStateChanged simState:" + simState + ", slotId:" + slotId);
            if (mChipCommon.isValidIndex(slotId) && !TextUtils.isEmpty(simState)) {
                switch (simState.hashCode()) {
                    case -2044123382:
                        break;
                    case -1971794228:
                        if (simState.equals("INTERNAL_LOCKED")) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1830845986:
                        if (simState.equals("CARD_IO_ERROR")) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1733499378:
                        if (simState.equals("NETWORK")) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case 79221:
                        if (simState.equals("PIN")) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    case 79590:
                        if (simState.equals("PUK")) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 2251386:
                        if (simState.equals("IMSI")) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case 433141802:
                        if (simState.equals("UNKNOWN")) {
                            c = '\n';
                            break;
                        }
                        c = 65535;
                        break;
                    case 1034051831:
                        if (simState.equals("NOT_READY")) {
                            c = '\t';
                            break;
                        }
                        c = 65535;
                        break;
                    case 1599753450:
                        if (simState.equals("CARD_RESTRICTED")) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1924388665:
                        if (simState.equals("ABSENT")) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        onImsiReady(slotId);
                        return;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case HwFullNetworkConstantsInner.ICCID_LEN_MINIMUM /* 7 */:
                        onLocked(slotId);
                        return;
                    case '\b':
                    case '\t':
                    case '\n':
                        this.mChipHisi.setCardReadyState(slotId, false);
                        return;
                    default:
                        return;
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
        HwFullNetworkConstantsInner.SubCarrierType[] oldsubCarrierTypes = (HwFullNetworkConstantsInner.SubCarrierType[]) mChipCommon.subCarrierTypeArray.clone();
        for (int sub = 0; sub < HwFullNetworkConstantsInner.SIM_NUM; sub++) {
            mChipCommon.judgeSubCarrierTypeByMccMnc(sub);
            logd("processSimStateChanged oldsubCarrierTypes[" + sub + "]:" + oldsubCarrierTypes[sub] + ", newsubCarrierTypes[" + sub + "]:" + mChipCommon.subCarrierTypeArray[sub]);
            if (oldsubCarrierTypes[sub] != mChipCommon.subCarrierTypeArray[sub]) {
                this.mChipHisi.needFixMainSlotPosition = true;
            }
        }
        this.mChipHisi.setCardReadyState(slotId, true);
        if (!mChipCommon.isSet4GSlotInProgress) {
            logd("onImsiReady send event EVENT_CHECK_MAIN_SLOT");
            this.mStateHandler.obtainMessage(201, 0).sendToTarget();
        }
    }

    private void onLocked(int slotId) {
        logd("onLocked slotId: " + slotId);
        this.mChipHisi.setCardReadyState(slotId, true);
        if (!mChipCommon.isSet4GSlotInProgress) {
            logd("onLocked send event EVENT_CHECK_MAIN_SLOT");
            this.mStateHandler.obtainMessage(201, 0).sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
