package com.android.internal.telephony.fullnetwork;

import android.telephony.Rlog;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;

public class HwFullNetworkOperatorCommon implements HwFullNetworkOperatorBase {
    private static final String LOG_TAG = "HwFullNetworkOperatorCommon";
    private boolean isMainSlotFound;
    public HwFullNetworkChipCommon mChipCommon;
    public HwFullNetworkChipHisi mChipHisi;
    public HwFullNetworkChipOther mChipOther;

    public HwFullNetworkOperatorCommon() {
        this.mChipCommon = null;
        this.mChipHisi = null;
        this.mChipOther = null;
        this.isMainSlotFound = false;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
    }

    public int getDefaultMainSlot(boolean forceSwitch) {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            this.mChipHisi = HwFullNetworkChipHisi.getInstance();
            return getDefaultMainSlotForHisi(forceSwitch);
        }
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        return getDefaultMainSlotForQcomAndMtk();
    }

    public int getDefaultMainSlotForHisi(boolean forceSwitch) {
        int temSub = this.mChipCommon.getUserSwitchDualCardSlots();
        UiccCard[] mUiccCards = this.mChipCommon.getUiccController().getUiccCards();
        boolean onlyCard2Present = false;
        boolean isCard1Present = mUiccCards[0].getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT || this.mChipHisi.mSwitchTypes[0] > 0;
        boolean isCard2Present = mUiccCards[1].getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT || this.mChipHisi.mSwitchTypes[1] > 0;
        boolean onlyCard1Present = isCard1Present && !isCard2Present;
        if (!isCard1Present && isCard2Present) {
            onlyCard2Present = true;
        }
        if (onlyCard1Present) {
            temSub = 0;
        } else if (onlyCard2Present) {
            temSub = 1;
        } else if (isCard1Present && isCard2Present) {
            temSub = this.mChipCommon.getDefaultMainSlotByIccId(temSub);
        }
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", defaultMainSlot " + temSub);
        return temSub;
    }

    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public int getDefaultMainSlotForQcomAndMtk() {
        int defaultMainSlot;
        int defaultMainSlot2;
        logd("getDefaultMainSlotForQcomAndMtk start");
        this.isMainSlotFound = false;
        int numOfSimPresent = 0;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (true == this.mChipCommon.isSimInsertedArray[i]) {
                numOfSimPresent++;
            }
        }
        if (numOfSimPresent == 0) {
            logd("no card inserted");
            int defaultMainSlot3 = this.mChipCommon.getUserSwitchDualCardSlots();
            this.isMainSlotFound = false;
            return defaultMainSlot3;
        } else if (1 == numOfSimPresent) {
            int defaultMainSlot4 = this.mChipOther.judgeDefault4GSlotForSingleSim(0);
            this.isMainSlotFound = true;
            return defaultMainSlot4;
        } else if (HwFullNetworkConfig.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK != 0) {
            this.isMainSlotFound = true;
            logd("USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK is true, use:" + defaultMainSlot2);
            return defaultMainSlot2;
        } else if (HwFullNetworkConfig.IS_CARD2_CDMA_SUPPORTED != 0 || HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
            this.isMainSlotFound = true;
            return 0;
        } else {
            int numOfCdmaCard = 0;
            int indexOfCCard = 0;
            for (int i2 = 0; i2 < HwFullNetworkConstants.SIM_NUM; i2++) {
                if (true == this.mChipCommon.isSimInsertedArray[i2] && this.mChipCommon.subCarrierTypeArray[i2].isCCard()) {
                    numOfCdmaCard++;
                    indexOfCCard = i2;
                }
            }
            if (1 == numOfCdmaCard) {
                logd("there is only one CDMA card inserted, set it as the 4G slot");
                this.isMainSlotFound = true;
                defaultMainSlot = indexOfCCard;
            } else {
                logd("there are multiple CDMA cards or U cards inserted, set the SUB_0 as the lte slot");
                this.isMainSlotFound = true;
                defaultMainSlot = 0;
                if (numOfCdmaCard > 1) {
                    this.mChipOther.updateUserDefaultFlag = true;
                }
            }
            return defaultMainSlot;
        }
    }

    public boolean isMainSlotFound() {
        return this.isMainSlotFound;
    }
}
