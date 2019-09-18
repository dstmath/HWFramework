package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwCardTrayInfo;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;

public abstract class HwFullNetworkCheckStateHisiBase extends HwFullNetworkCheckStateBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateHisiBase";
    public HwFullNetworkChipHisi mChipHisi;
    public boolean ready;

    public abstract boolean checkIfAllCardsReady(Message message);

    public HwFullNetworkCheckStateHisiBase(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        this.ready = true;
        this.mChipHisi = null;
        this.mChipHisi = HwFullNetworkChipHisi.getInstance();
        logd("HwFullNetworkCheckStateHisiBase constructor");
    }

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
                this.mCheckStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT, this.defaultMainSlot, 0).sendToTarget();
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
            if (i >= HwFullNetworkConstants.SIM_NUM) {
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
        for (int i2 = 0; i2 < HwFullNetworkConstants.SIM_NUM; i2++) {
            if (this.mChipHisi.mGetBalongSimSlotDone[i2]) {
                countGetBalongSimSlotDone++;
            }
        }
        if (countGetBalongSimSlotDone == 0) {
            logd("mGetBalongSimSlotDone all false");
            this.ready = false;
        }
        if (this.mChipCommon.mUiccController == null || this.mChipCommon.mUiccController.getUiccCards() == null || this.mChipCommon.mUiccController.getUiccCards().length < HwFullNetworkConstants.SIM_NUM) {
            logd("haven't get all UiccCards done, please wait!");
            this.ready = false;
            return;
        }
        UiccCard[] uc = this.mChipCommon.mUiccController.getUiccCards();
        int i3 = 0;
        while (i3 < uc.length) {
            if (uc[i3] == null) {
                logd("UiccCard[" + i3 + "]is null");
                this.ready = false;
                return;
            } else if (!HwFullNetworkConfig.IS_CMCC_4G_DSDX_ENABLE || uc[i3].getCardState() == IccCardStatus.CardState.CARDSTATE_ABSENT || this.mChipHisi.mCardReady4Switch[i3]) {
                i3++;
            } else {
                logd("uicccard state : " + uc[i3].getCardState());
                this.ready = false;
                return;
            }
        }
    }

    public void disposeCardStatusWhenAllTrayOut() {
        boolean isSingleCardTrayOut = HwFullNetworkConfig.IS_SINGLE_CARD_TRAY && HwCardTrayInfo.getInstance().isCardTrayOut(0);
        boolean isBothCardTrayOut = !HwFullNetworkConfig.IS_SINGLE_CARD_TRAY && HwCardTrayInfo.getInstance().isCardTrayOut(0) && HwCardTrayInfo.getInstance().isCardTrayOut(1);
        if (!(TelephonyManager.MultiSimVariants.DSDA == TelephonyManager.getDefault().getMultiSimConfiguration())) {
            return;
        }
        if ((isSingleCardTrayOut || isBothCardTrayOut) && HwFullNetworkConfig.IS_HISI_DSDX) {
            logd("DSDX all tray out. disposeCardStatus");
            this.mChipHisi.disposeCardStatus(true);
            this.mChipHisi.setWaitingSwitchBalongSlot(false);
        }
    }

    public void setPrefNetworkIfNeeded() {
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && HwFullNetworkConfig.IS_VICE_WCDMA && this.mChipCommon.needRetrySetPrefNetwork) {
            logd("needRetrySetPrefNetwork");
            this.mChipHisi.setPrefNwForCmcc(this);
            this.mChipCommon.needRetrySetPrefNetwork = false;
        }
    }

    public boolean switchDualCardsSlotIfNeeded() {
        if (!(!SystemProperties.getBoolean("persist.sys.dualcards", false) || this.mChipHisi.mAutoSwitchDualCardsSlotDone) || this.mChipHisi.needFixMainSlotPosition) {
            if (judgeDefaltMainSlot()) {
                logd("Need to set the Main slot");
            } else {
                this.defaultMainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
                logd("there is no need to set the 4G slot, setdefault slot as " + this.defaultMainSlot);
            }
            this.mChipHisi.needFixMainSlotPosition = false;
            boolean isVSimOn = HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate();
            if (HwFullNetworkConfig.IS_HISI_DSDX && !isVSimOn) {
                if (this.mChipHisi.needSetDataAllowCount == 0) {
                    SubscriptionController.getInstance().setDefaultDataSubId(this.defaultMainSlot);
                    this.mChipHisi.needSetDataAllowCount = PhoneFactory.onDataSubChange(HwFullNetworkConstants.EVENT_SET_DATA_ALLOW_DONE, this);
                    if (this.mChipHisi.needSetDataAllowCount > 0) {
                        logd("switchDualCardsSlotIfNeeded return because needSetDataAllowCount = " + this.mChipHisi.needSetDataAllowCount);
                        this.mChipHisi.curSetDataAllowCount = 0;
                        this.mChipHisi.setWaitingSwitchBalongSlot(true);
                        this.mChipHisi.mAutoSwitchDualCardsSlotDone = true;
                        return false;
                    }
                    logd("switchDualCardsSlotIfNeeded no need set_data_allow to any phone");
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
        logd("mAutoSwitchDualCardsSlotDone has been completed before");
        this.mChipHisi.setWaitingSwitchBalongSlot(false);
        return false;
    }

    public int getDefaultMainSlot() {
        return this.defaultMainSlot;
    }

    /* access modifiers changed from: protected */
    public void checkNetworkType() {
    }

    public boolean judgeDefaltMainSlot() {
        if (judgeDefaultMainSlotForMDM()) {
            return true;
        }
        UiccController mUiccController = this.mChipCommon.getUiccController();
        if (mUiccController == null || mUiccController.getUiccCards() == null || mUiccController.getUiccCards().length < HwFullNetworkConstants.SIM_NUM) {
            logd("haven't get all UiccCards done, please wait!");
            return false;
        }
        for (UiccCard uc : mUiccController.getUiccCards()) {
            if (uc == null) {
                logd("haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        this.defaultMainSlot = this.mOperatorBase.getDefaultMainSlot(this.mChipHisi.needFixMainSlotPosition);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean judgeSetDefault4GSlotForCMCC(int cmccSlotId) {
        this.defaultMainSlot = this.mOperatorBase.getDefaultMainSlot(true);
        if (this.defaultMainSlot != this.mChipCommon.getUserSwitchDualCardSlots()) {
            return true;
        }
        return false;
    }
}
