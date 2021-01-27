package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.ProxyControllerEx;
import com.huawei.internal.telephony.RadioCapabilityEx;

public class HwFullNetworkSetStateHisi2_0 extends HwFullNetworkSetStateBase {
    private static final String LOG_TAG = "HwFullNetworkSetStateHisi2_0";
    private static final int NEED_RESTART_RILD = 0;
    private HwFullNetworkChipHisi mChipHisi;
    private boolean needResartRild;

    HwFullNetworkSetStateHisi2_0(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        this.mChipHisi = null;
        this.needResartRild = false;
        this.mChipHisi = HwFullNetworkChipHisi.getInstance();
        logd("HwFullNetworkSetStateHisi2_0 constructor");
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = Integer.valueOf(i);
            this.mCis[i].registerForIccStatusChanged(this, (int) HwFullNetworkConstantsInner.EVENT_RESTART_RILD_FOR_NV, index);
            this.mCis[i].registerUnsolHwRestartRildStatus(this, (int) HwFullNetworkConstantsInner.EVENT_UNSOL_RESTART_RILD_STATUS, index);
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        Integer index = this.mChipCommon.getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        int i = msg.what;
        if (i == 401) {
            logd("Received EVENT_SET_MAIN_SLOT_DONE on index " + index);
            setMainSlotDone(msg, index.intValue());
        } else if (i == 402) {
            logd("Received EVENT_SET_MAIN_SLOT_TIMEOUT on index " + index);
            setMainSlotTimeOut(msg, index.intValue());
        } else if (i == 1007) {
            logd("Received EVENT_GET_CDMA_MODE_SIDE_DONE on index " + index);
            this.mChipHisi.onGetCdmaModeSideDone(AsyncResultEx.from(msg.obj), index);
        } else if (i == 1011) {
            logd("EVENT_CMCC_SET_NETWOR_DONE reveived for slot: " + msg.arg1);
            this.mChipHisi.handleSetCmccPrefNetwork(msg);
        } else if (i == 1013) {
            logd("Received EVENT_RESTART_RILD_FOR_NV on index " + index);
            restartRildForNvcfg();
        } else if (i == 2205) {
            logd("EVENT_SWITCH_SLOT_TYPE_DONE");
        } else if (i != 2206) {
            logd("Unknown msg:" + msg.what);
        } else {
            logd("EVENT_UNSOL_RESTART_RILD_STATUS");
            onUnsolRestartRildStatus(msg);
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setMainSlot(int slotId, Message responseMsg) {
        this.mChipCommon.expectedDDSsubId = slotId;
        if (!judgeIfNeedSetMainSlot(slotId)) {
            this.mChipHisi.mAutoSwitchDualCardsSlotDone = false;
            this.mChipCommon.sendResponseToTarget(responseMsg, 2);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT).sendToTarget();
            return;
        }
        logd("setDefault4GSlot: target slot id is: " + slotId + " response:" + responseMsg);
        sendHwSwitchSlotStartBroadcast();
        this.mChipCommon.mSet4GSlotCompleteMsg = responseMsg;
        this.mChipCommon.isSet4GSlotInProgress = true;
        this.mChipCommon.isSet4GSlotInSwitchProgress = true;
        this.mChipCommon.current4GSlotBackup = this.mChipCommon.getUserSwitchDualCardSlots();
        fastSwitchDualCardsSlot(slotId);
    }

    private boolean judgeIfNeedSetMainSlot(int slotId) {
        if (!SubscriptionManagerEx.isValidSlotIndex(slotId)) {
            logd("judgeIfNeedSetMainSlot: target slot id is invalid:slot " + slotId);
            return false;
        } else if (this.mCis[slotId] != null && this.mCis[slotId].getRadioState() == 2) {
            logd("judgeIfNeedSetMainSlot: radio power unavailable:slot " + slotId);
            return false;
        } else if (!this.mChipCommon.isSupportEuicc() || !this.mChipCommon.isSet4GSlotInSwitchProgress) {
            return true;
        } else {
            logd("judgeIfNeedSetMainSlot: set main slot is in progress:slot " + slotId);
            return false;
        }
    }

    private void fastSwitchDualCardsSlot(int expectedMainSlotId) {
        PhoneExt phoneExt;
        logd("fastSwitchDualCardsSlot: expectedMainSlot=" + expectedMainSlotId);
        int i = 0;
        if (isHandleVsim()) {
            logd("vsim on sub");
            this.mChipHisi.setWaitingSwitchBalongSlot(false);
            this.mChipCommon.isSet4GSlotInSwitchProgress = false;
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 2);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT));
            return;
        }
        logd("fastSwitchDualCardsSlot:setDefaultDataSubId=" + expectedMainSlotId + ",no set database to expectedMainSlotId");
        ProxyControllerEx proxyController = ProxyControllerEx.getInstance();
        int cdmaSimSlotId = getCdmaSimCardSlotId(expectedMainSlotId);
        if (!isNeedSetRadioCapability(expectedMainSlotId, cdmaSimSlotId)) {
            logd("fastSwitchDualCardsSlot: don't need SetRadioCapability,response SUCCESS");
            this.mChipCommon.sendResponseToTarget(obtainMessage(401, Integer.valueOf(expectedMainSlotId)), 0);
            return;
        }
        for (int i2 = 0; i2 < HwFullNetworkConstantsInner.SIM_NUM; i2++) {
            if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && HwFullNetworkConfigInner.IS_VICE_WCDMA && HwFullNetworkManager.getInstance().isCMCCHybird() && (phoneExt = PhoneFactoryExt.getPhone(i2)) != null) {
                logd("fastSwitchDualCardsSlot: set network mode to NETWORK_MODE_GSM_UMTS");
                phoneExt.setPreferredNetworkType(3, (Message) null);
            }
        }
        if (this.mChipCommon.mSet4GSlotCompleteMsg != null) {
            i = 1;
        }
        int switchSlotType = i;
        if (this.mChipCommon.isAllSimContactLoaded()) {
            switchSlotType = 2;
        } else {
            logd("Sim Contact is not loaded");
        }
        Message switchSoltTypeMsg = obtainMessage(HwFullNetworkConstantsInner.EVENT_SWITCH_SLOT_TYPE_DONE);
        if (this.mCis[this.mChipCommon.current4GSlotBackup] != null) {
            logd("fastSwitchDualCardsSlot: sendSimChgTypeInfo type:" + switchSlotType);
            this.mCis[this.mChipCommon.current4GSlotBackup].sendSimChgTypeInfo(switchSlotType, switchSoltTypeMsg);
        }
        if (!proxyController.setRadioCapability(expectedMainSlotId, cdmaSimSlotId)) {
            logd("fastSwitchDualCardsSlot: setRadioCapability fail ,response GENERIC_FAILURE");
            this.mChipCommon.sendResponseToTarget(obtainMessage(401, Integer.valueOf(expectedMainSlotId)), 2);
        }
        startFastSwithSIMSlotTimer();
    }

    private boolean isHandleVsim() {
        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
            return false;
        }
        if (!HwVSimUtils.isVSimEnabled() && !HwVSimUtils.isVSimCauseCardReload() && !HwVSimUtils.isSubActivationUpdate() && HwVSimUtils.isAllowALSwitch()) {
            return false;
        }
        return true;
    }

    private int getCdmaSimCardSlotId(int expectedMainSlotId) {
        HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();
        if (mHwTelephonyManager.isCDMASimCard(0) && mHwTelephonyManager.isCDMASimCard(1)) {
            return expectedMainSlotId;
        }
        if (mHwTelephonyManager.isCDMASimCard(0)) {
            return 0;
        }
        if (mHwTelephonyManager.isCDMASimCard(1)) {
            return 1;
        }
        logd("getCdmaSimCardSlotId: CDMA sim card slot id invalid");
        return -1;
    }

    private boolean isNeedSetRadioCapability(int expectedMainSlotId, int cdmaSimSlotId) {
        PhoneExt[] mPhones = PhoneFactoryExt.getPhones();
        if (mPhones == null) {
            logd("isNeedSetRadioCapability: mPhones is null");
            return false;
        }
        boolean same = true;
        if (SubscriptionManagerEx.isValidSlotIndex(expectedMainSlotId) && mPhones[expectedMainSlotId] != null) {
            RadioCapabilityEx expectedMainSlotRC = mPhones[expectedMainSlotId].getRadioCapability();
            if (expectedMainSlotRC == null || "0".equals(expectedMainSlotRC.getLogicalModemUuid())) {
                logd("isNeedSetRadioCapability: expectedMainSlotId equals with LogicalModemUuid");
            } else {
                logd("isNeedSetRadioCapability: need switch LogicalModemUuid for expectedMainSlotId");
                same = false;
            }
        }
        if (SubscriptionManagerEx.isValidSlotIndex(cdmaSimSlotId) && mPhones[cdmaSimSlotId] != null) {
            RadioCapabilityEx cdmaSimSlotRC = mPhones[cdmaSimSlotId].getRadioCapability();
            int cdmaSimSlotRaf = 0;
            if (cdmaSimSlotRC != null) {
                cdmaSimSlotRaf = cdmaSimSlotRC.getRadioAccessFamily();
            }
            if (64 != (cdmaSimSlotRaf & 64)) {
                logd("isNeedSetRadioCapability: need add RAF_1xRTT for cdmaSimSlotRaf");
                same = false;
            } else {
                logd("isNeedSetRadioCapability: cdmaSimSlotRaf has RAF_1xRTT");
            }
        }
        if (same) {
            logd("isNeedSetRadioCapability: Already in requested configuration, nothing to do.");
            return false;
        }
        this.mChipHisi.mNvRestartRildDone = true;
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setRadioCapabilityDone(Intent intent) {
        logd("reset mNvRestartRildDone");
        this.mChipHisi.mNvRestartRildDone = false;
        if (intent != null) {
            this.mChipCommon.sendResponseToTarget(obtainMessage(401, Integer.valueOf(intent.getIntExtra("intContent", 0))), 0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setRadioCapabilityFailed(Intent intent) {
        Message response = obtainMessage(401, Integer.valueOf(this.mChipCommon.expectedDDSsubId));
        this.mChipHisi.mAutoSwitchDualCardsSlotDone = false;
        this.mChipCommon.sendResponseToTarget(response, 2);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void setMainSlotDone(Message response, int index) {
        if (hasMessages(402)) {
            removeMessages(402);
        }
        this.mCis[index].getCdmaModeSide(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_CDMA_MODE_SIDE_DONE, Integer.valueOf(index)));
        AsyncResultEx ar = AsyncResultEx.from(response.obj);
        if (ar == null || ar.getException() != null) {
            loge("EVENT_FAST_SWITCH_SIM_SLOT_DONE failed ,response GENERIC_FAILURE");
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 2);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
            revertDefaultDataSubId();
            this.mChipCommon.isSet4GSlotInSwitchProgress = false;
            this.mChipHisi.setPrefNwForCmcc(this);
        } else {
            logd("EVENT_FAST_SWITCH_SIM_SLOT_DONE success for slot: " + index);
            if (HwFullNetworkConfigInner.isCMCCDsdxEnable() || HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 0) {
                this.mChipHisi.saveIccidsWhenAllCardsReady();
            }
            HwTelephonyManagerInner.getDefault().updateCrurrentPhone(index);
            sendHwSwitchSlotDoneBroadcast(index);
            this.mChipCommon.saveMainCardIccId(this.mChipHisi.mFullIccIds[index]);
            if ("0".equals(SystemPropertiesEx.get("gsm.nvcfg.rildrestarting", "0"))) {
                logd("send mSet4GSlotCompleteMsg");
                this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
                this.mChipCommon.mSet4GSlotCompleteMsg = null;
                this.mChipHisi.setPrefNwForCmcc(this);
            } else {
                logd("waiting for rild restart");
                this.mChipCommon.needRetrySetPrefNetwork = true;
            }
            logd("set DDS for slot " + index);
            this.mChipCommon.isSet4GSlotInSwitchProgress = false;
            HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(index);
            Settings.Global.putInt(this.mContext.getContentResolver(), HwFullNetworkConstantsInner.USER_DEFAULT_SUBSCRIPTION, index);
        }
        this.mChipHisi.setWaitingSwitchBalongSlot(false);
        this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT).sendToTarget();
        if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data")) {
            reCheckDefaultMainSlotForMDM();
        }
        restartRildForNvcfg();
        if (this.needResartRild) {
            restartRild();
        }
    }

    private void reCheckDefaultMainSlotForMDM() {
        this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(204, 0));
    }

    private void setMainSlotTimeOut(Message msg, int index) {
        logd("Received EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT on index " + index);
        this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 2);
        this.mChipCommon.mSet4GSlotCompleteMsg = null;
        revertDefaultDataSubId();
        this.mChipHisi.setWaitingSwitchBalongSlot(false);
        this.mChipCommon.isSet4GSlotInSwitchProgress = false;
        this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT).sendToTarget();
        if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data")) {
            reCheckDefaultMainSlotForMDM();
        }
    }

    private void revertDefaultDataSubId() {
        int mainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        HwSubscriptionManager.getInstance().setDefaultDataSubIdToDbBySlotId(mainSlot);
        logd("revertDefaultDataSubId,setDefaultDataSubId=" + mainSlot + ",only set database to original");
    }

    private void restartRildForNvcfg() {
        logd("restartRildForNvcfg, mNvRestartRildDone: " + this.mChipHisi.mNvRestartRildDone + ", mAutoSwitchDualCardsSlotDone: " + this.mChipHisi.mAutoSwitchDualCardsSlotDone + ", mSetSdcsCompleteMsg: " + this.mChipHisi.mSetSdcsCompleteMsg + ", gsm.nvcfg.resetrild: " + SystemPropertiesEx.get("gsm.nvcfg.resetrild", "0") + ", isSet4GSlotInProgress: " + this.mChipCommon.isSet4GSlotInProgress);
        if ((!this.mChipHisi.mNvRestartRildDone && this.mChipHisi.mAutoSwitchDualCardsSlotDone && this.mChipHisi.mSetSdcsCompleteMsg == null) && "1".equals(SystemPropertiesEx.get("gsm.nvcfg.resetrild", "0")) && !this.mChipCommon.isSet4GSlotInProgress) {
            logd("restartRildForNvcfg needSetDataAllowCount: " + this.mChipHisi.needSetDataAllowCount);
            if (this.mChipHisi.needSetDataAllowCount == 0) {
                logd("restartRildForNvcfg: call restartRild");
                if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isVSimEnabled()) {
                    this.mChipHisi.mNvRestartRildDone = true;
                    restartRild();
                    return;
                }
                logd("restartRildForNvcfg: vsim is enabled, delay it.");
                HwVSimUtils.storeIfNeedRestartRildForNvMatch(true);
                HwVSimUtils.restartRildIfIdle();
            }
        }
    }

    private void onUnsolRestartRildStatus(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int restartRildStatus = -1;
        if (ar == null || ar.getException() != null || ar.getResult() == null) {
            logd("onUnsolRestartRildStatus: err");
        } else {
            restartRildStatus = ((Integer) ar.getResult()).intValue();
        }
        this.needResartRild = restartRildStatus == 0;
        logd("onUnsolRestartRildStatus:restartRildStatus=" + restartRildStatus + " needResartRild=" + this.needResartRild);
        if (this.needResartRild && !this.mChipCommon.isSet4GSlotInSwitchProgress) {
            restartRild();
        }
    }

    private void restartRild() {
        logd("restartRild");
        this.needResartRild = false;
        try {
            this.mCis[0].restartRild((Message) null);
        } catch (IllegalArgumentException e) {
            logd("restartRild IllegalArgumentException.");
        } catch (RuntimeException e2) {
            logd("restartRild RuntimeException");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkSetStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
