package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwCardTrayInfo;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;

public abstract class HwFullNetworkCheckStateHisiBase extends HwFullNetworkCheckStateBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateHisiBase";
    public HwFullNetworkChipHisi mChipHisi;
    public boolean ready;

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public abstract boolean checkIfAllCardsReady(Message message);

    public HwFullNetworkCheckStateHisiBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        this.ready = true;
        this.mChipHisi = null;
        this.mChipHisi = HwFullNetworkChipHisi.getInstance();
        logd("HwFullNetworkCheckStateHisiBase constructor");
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 302) {
            logd("Received EVENT_SET_DATA_ALLOW_DONE curSetDataAllowCount = " + this.mChipHisi.curSetDataAllowCount);
            HwFullNetworkChipHisi hwFullNetworkChipHisi = this.mChipHisi;
            hwFullNetworkChipHisi.curSetDataAllowCount = hwFullNetworkChipHisi.curSetDataAllowCount + 1;
            if (this.mChipHisi.needSetDataAllowCount == this.mChipHisi.curSetDataAllowCount) {
                this.mChipHisi.needSetDataAllowCount = 0;
                this.mChipHisi.curSetDataAllowCount = 0;
                logd("all EVENT_SET_DATA_ALLOW_DONE message got, start switch main slot");
                this.mChipHisi.setWaitingSwitchBalongSlot(true);
                this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
                this.mCheckStateHandler.obtainMessage(202, this.defaultMainSlot, 0).sendToTarget();
            }
        } else if (i != 1011) {
            logd("Unknown msg:" + msg.what);
        } else {
            logd("EVENT_CMCC_SET_NETWOR_DONE reveived for slot: " + msg.arg1);
            this.mChipHisi.handleSetCmccPrefNetwork(msg);
        }
    }

    public void isCardsReady() {
        disposeCardStatusWhenAllTrayOut();
        this.ready = true;
        int i = 0;
        while (true) {
            if (i >= HwFullNetworkConstantsInner.SIM_NUM) {
                break;
            } else if (this.mChipHisi.mSwitchTypes[i] == -1) {
                logd("mSwitchTypes[" + i + "] == INVALID");
                this.ready = false;
                break;
            } else if (!this.mChipHisi.mGetUiccCardsStatusDone[i]) {
                logd("mGetUiccCardsStatusDone[" + i + "] == false");
                this.ready = false;
                break;
            } else if (this.mChipCommon.mIccIds[i] == null) {
                logd("mIccIds[" + i + "] invalid");
                this.ready = false;
                break;
            } else {
                i++;
            }
        }
        int countGetBalongSimSlotDone = 0;
        for (int i2 = 0; i2 < HwFullNetworkConstantsInner.SIM_NUM; i2++) {
            if (this.mChipHisi.mGetBalongSimSlotDone[i2]) {
                countGetBalongSimSlotDone++;
            }
        }
        if (countGetBalongSimSlotDone == 0) {
            logd("mGetBalongSimSlotDone all false");
            this.ready = false;
        }
        if (this.mChipCommon.mUiccController == null || this.mChipCommon.mUiccController.getUiccCards() == null || this.mChipCommon.mUiccController.getUiccCards().length < HwFullNetworkConstantsInner.SIM_NUM) {
            logd("haven't get all UiccCards done, please wait!");
            this.ready = false;
            return;
        }
        UiccCardExt[] uc = this.mChipCommon.mUiccController.getUiccCards();
        for (int i3 = 0; i3 < uc.length; i3++) {
            if (uc[i3] == null) {
                logd("UiccCard[" + i3 + "]is null");
                this.ready = false;
                return;
            } else if ((HwFullNetworkConfigInner.isCMCCDsdxEnable() || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1) && uc[i3].getCardState() != IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT && !this.mChipHisi.mCardReady4Switch[i3]) {
                logd("uicccard state : " + uc[i3].getCardState());
                this.ready = false;
                return;
            }
        }
    }

    public void disposeCardStatusWhenAllTrayOut() {
        boolean isSingleCardTrayOut = HwFullNetworkConfigInner.IS_SINGLE_CARD_TRAY && HwCardTrayInfo.getInstance().isCardTrayOut(0);
        boolean isBothCardTrayOut = !HwFullNetworkConfigInner.IS_SINGLE_CARD_TRAY && HwCardTrayInfo.getInstance().isCardTrayOut(0) && HwCardTrayInfo.getInstance().isCardTrayOut(1);
        if (!(HwTelephonyManager.MultiSimVariantsEx.DSDA == HwTelephonyManager.getDefault().getMultiSimConfiguration())) {
            return;
        }
        if ((isSingleCardTrayOut || isBothCardTrayOut) && HwFullNetworkConfigInner.IS_HISI_DSDX) {
            logd("DSDX all tray out. disposeCardStatus");
            this.mChipHisi.disposeCardStatus(true);
            this.mChipHisi.setWaitingSwitchBalongSlot(false);
        }
    }

    public void setPrefNetworkIfNeeded() {
        if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && HwFullNetworkConfigInner.IS_VICE_WCDMA && this.mChipCommon.needRetrySetPrefNetwork) {
            logd("needRetrySetPrefNetwork");
            this.mChipHisi.setPrefNwForCmcc(this);
            this.mChipCommon.needRetrySetPrefNetwork = false;
        }
    }

    public boolean switchDualCardsSlotIfNeeded() {
        if (!(!SystemPropertiesEx.getBoolean("persist.sys.dualcards", false) || this.mChipHisi.mAutoSwitchDualCardsSlotDone) || this.mChipHisi.needFixMainSlotPosition) {
            boolean isCard1Present = this.mChipHisi.isSimPresentBySubState(0);
            boolean isCard2Present = this.mChipHisi.isSimPresentBySubState(1);
            if (isCard1Present || isCard2Present) {
                if (judgeDefaltMainSlot()) {
                    logd("Need to set the Main slot");
                } else {
                    this.defaultMainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
                    logd("there is no need to set the 4G slot, setdefault slot as " + this.defaultMainSlot);
                }
                this.mChipHisi.needFixMainSlotPosition = false;
                boolean isVSimOn = HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate());
                if (HwFullNetworkConfigInner.IS_HISI_DSDX && !isVSimOn) {
                    if (this.mChipHisi.needSetDataAllowCount == 0) {
                        HwSubscriptionManager.getInstance().setDefaultDataSubIdBySlotId(this.defaultMainSlot);
                        this.mChipHisi.needSetDataAllowCount = PhoneFactoryExt.onDataSubChange();
                        if (this.mChipHisi.needSetDataAllowCount > 0) {
                            logd("switchDualCardsSlotIfNeeded return because needSetDataAllowCount = " + this.mChipHisi.needSetDataAllowCount);
                            this.mChipHisi.curSetDataAllowCount = 0;
                            this.mChipHisi.setWaitingSwitchBalongSlot(true);
                            this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
                            return false;
                        }
                        logd("switchDualCardsSlotIfNeeded no need set_data_allow to any PhoneExt");
                    } else {
                        logd("switchDualCardsSlotIfNeeded already in set_data_allow process , needSetDataAllowCount = " + this.mChipHisi.needSetDataAllowCount);
                        return false;
                    }
                }
                this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
                if (isVSimOn) {
                    logd("switchDualCardsSlotIfNeeded, vsim is on, not set mark to true.");
                } else {
                    this.mChipHisi.setWaitingSwitchBalongSlot(true);
                }
                return true;
            }
            logd("dual cards absent");
            return false;
        }
        logd("mAutoSwitchDualCardsSlotDone has been completed before");
        this.mChipHisi.setWaitingSwitchBalongSlot(false);
        return false;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public int getDefaultMainSlot() {
        return this.defaultMainSlot;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void checkNetworkType() {
    }

    public boolean judgeDefaltMainSlot() {
        if (judgeDefaultMainSlotForMDM()) {
            return true;
        }
        UiccControllerExt mUiccController = this.mChipCommon.getUiccController();
        if (mUiccController == null || mUiccController.getUiccCards() == null || mUiccController.getUiccCards().length < HwFullNetworkConstantsInner.SIM_NUM) {
            logd("haven't get all UiccCards done, please wait!");
            return false;
        }
        for (UiccCardExt uc : mUiccController.getUiccCards()) {
            if (uc == null) {
                logd("haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        this.defaultMainSlot = this.mOperatorBase.getDefaultMainSlot(this.mChipHisi.needFixMainSlotPosition);
        logd("judgeDefaltMainSlot, defaultMainSlot = " + this.defaultMainSlot);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public boolean judgeSetDefault4GSlotForCMCC(int cmccSlotId) {
        this.defaultMainSlot = this.mOperatorBase.getDefaultMainSlot(true);
        if (this.defaultMainSlot != this.mChipCommon.getUserSwitchDualCardSlots()) {
            return true;
        }
        return false;
    }
}
