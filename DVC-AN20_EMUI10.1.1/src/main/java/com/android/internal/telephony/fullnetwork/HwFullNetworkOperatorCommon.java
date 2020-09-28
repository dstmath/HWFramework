package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.android.telephony.RlogEx;

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

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public int getDefaultMainSlot(boolean forceSwitch) {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            this.mChipHisi = HwFullNetworkChipHisi.getInstance();
            return getDefaultMainSlotForHisi(forceSwitch);
        }
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        return getDefaultMainSlotForQcomAndMtk();
    }

    public int getDefaultMainSlotForHisi(boolean forceSwitch) {
        boolean onlyCard1Present;
        boolean onlyCard2Present = true;
        int temSub = this.mChipCommon.getUserSwitchDualCardSlots();
        this.mChipCommon.getUiccController().getUiccCards();
        boolean isCard1Present = this.mChipHisi.isSimPresentBySubState(0);
        boolean isCard2Present = this.mChipHisi.isSimPresentBySubState(1);
        if (this.mChipHisi.isEuiccInSlot2AndNoProfile()) {
            isCard2Present = false;
        }
        if (!isCard1Present || isCard2Present) {
            onlyCard1Present = false;
        } else {
            onlyCard1Present = true;
        }
        if (isCard1Present || !isCard2Present) {
            onlyCard2Present = false;
        }
        if (onlyCard1Present) {
            temSub = 0;
        } else if (onlyCard2Present) {
            temSub = 1;
        } else if (!isCard1Present || !isCard2Present) {
            logd("defaultMainSlot " + temSub);
        } else {
            temSub = this.mChipCommon.getDefaultMainSlotByIccId(temSub);
        }
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", defaultMainSlot " + temSub);
        return temSub;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    public int getDefaultMainSlotForQcomAndMtk() {
        logd("getDefaultMainSlotForQcomAndMtk start");
        int numOfSimPresent = 0;
        this.isMainSlotFound = false;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (true == this.mChipCommon.isSimInsertedArray[i]) {
                numOfSimPresent++;
            }
        }
        if (numOfSimPresent == 0) {
            logd("no card inserted");
            int defaultMainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
            this.isMainSlotFound = false;
            return defaultMainSlot;
        } else if (numOfSimPresent == 1) {
            int defaultMainSlot2 = this.mChipOther.judgeDefault4GSlotForSingleSim(0);
            this.isMainSlotFound = true;
            return defaultMainSlot2;
        } else if (HwFullNetworkConfigInner.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
            this.isMainSlotFound = true;
            int defaultMainSlot3 = this.mChipCommon.getDefaultMainSlotByIccId(this.mChipCommon.getUserSwitchDualCardSlots());
            logd("USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK is true, use:" + defaultMainSlot3);
            return defaultMainSlot3;
        } else if (!HwFullNetworkConfigInner.IS_CARD2_CDMA_SUPPORTED && !HwFullNetworkConfigInner.IS_QCRIL_CROSS_MAPPING && !HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            return getCdmaCardDefaultMainSlot();
        } else {
            this.isMainSlotFound = true;
            return 0;
        }
    }

    private int getCdmaCardDefaultMainSlot() {
        int numOfCdmaCard = 0;
        int indexOfCCard = 0;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (this.mChipCommon.isSimInsertedArray[i] && this.mChipCommon.subCarrierTypeArray[i].isCCard()) {
                numOfCdmaCard++;
                indexOfCCard = i;
            }
        }
        if (numOfCdmaCard == 1) {
            logd("there is only one CDMA card inserted, set it as the 4G slot");
            this.isMainSlotFound = true;
            return indexOfCCard;
        }
        logd("there are multiple CDMA cards or U cards inserted, set the SUB_0 as the lte slot");
        this.isMainSlotFound = true;
        if (numOfCdmaCard <= 1) {
            return 0;
        }
        this.mChipOther.updateUserDefaultFlag = true;
        return 0;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public boolean isMainSlotFound() {
        return this.isMainSlotFound;
    }
}
