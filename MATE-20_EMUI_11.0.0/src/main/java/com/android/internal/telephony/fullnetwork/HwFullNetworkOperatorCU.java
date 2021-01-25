package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.telephony.RlogEx;

public class HwFullNetworkOperatorCU implements HwFullNetworkOperatorBase {
    private static final String LOG_TAG = "HwFullNetworkOperatorCU";
    private boolean isMainSlotFound;
    public HwFullNetworkChipCommon mChipCommon;
    public HwFullNetworkChipHisi mChipHisi;
    public HwFullNetworkChipOther mChipOther;

    public HwFullNetworkOperatorCU() {
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
        int temSub = this.mChipCommon.getUserSwitchDualCardSlots();
        this.mChipCommon.getUiccController().getUiccCards();
        boolean onlyCard2Present = false;
        boolean isCard1Present = this.mChipHisi.isSimPresentBySubState(0);
        boolean isCard2Present = this.mChipHisi.isSimPresentBySubState(1);
        boolean onlyCard1Present = isCard1Present && !isCard2Present;
        if (!isCard1Present && isCard2Present) {
            onlyCard2Present = true;
        }
        if (onlyCard1Present) {
            temSub = 0;
        } else if (onlyCard2Present) {
            temSub = 1;
        } else {
            logd("card present state is not normal.");
        }
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", defaultMainSlot " + temSub);
        return temSub;
    }

    public int getDefaultMainSlotForQcomAndMtk() {
        logd("judgeDefault4GSlotForCU enter");
        this.isMainSlotFound = false;
        int default4GSlot = 0;
        int noSimCount = 0;
        int curSimCount = 0;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (this.mChipCommon.isSimInsertedArray[i]) {
                curSimCount++;
                if (!this.isMainSlotFound) {
                    default4GSlot = i;
                }
                this.isMainSlotFound = true;
            } else {
                noSimCount++;
            }
        }
        logd("curSimCount =" + curSimCount + ", noSimCount = " + noSimCount);
        if (curSimCount + noSimCount != HwFullNetworkConstantsInner.SIM_NUM || curSimCount == 0) {
            loge("cards status error or all cards absent.");
            this.isMainSlotFound = false;
            if (HwFullNetworkConfigInner.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
                return this.mChipCommon.getUserSwitchDualCardSlots();
            }
            return 0;
        } else if (curSimCount == 1) {
            return default4GSlot;
        } else {
            if (HwFullNetworkConfigInner.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
                int mainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
                int mainSlotByIccId = this.mChipCommon.getDefaultMainSlotByIccId(mainSlot);
                logd("judgeDefault4GSlotForCU, mainSlot:" + mainSlot + ", mainSlotByIccId: " + mainSlotByIccId);
                return mainSlotByIccId;
            }
            initSubTypes();
            int mSub = this.mChipOther.getMainCardSubByPriority(HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED);
            logd(this.mChipOther.currentSubTypeMap.toString());
            logd("4G slot sub is " + mSub);
            switch (mSub) {
                case 0:
                case 1:
                    return mSub;
                case 10:
                case HwFullNetworkConstantsInner.SUB_NO_CMCC /* 11 */:
                    logd("The two cards inserted have the same priority or no CARRIER_PREFERRED");
                    return this.mChipCommon.getDefaultMainSlotByIccId(this.mChipCommon.getUserSwitchDualCardSlots());
                default:
                    this.isMainSlotFound = false;
                    return this.mChipCommon.getDefaultMainSlotByIccId(this.mChipCommon.getUserSwitchDualCardSlots());
            }
        }
    }

    private void initSubTypes() {
        logd("in initSubTypes.");
        for (int index = 0; index < HwFullNetworkConstantsInner.SIM_NUM; index++) {
            this.mChipOther.currentSubTypeMap.put(Integer.valueOf(index), getSubTypeBySub(index));
        }
    }

    public HwFullNetworkConstantsInner.SubType getSubTypeBySub(int sub) {
        logd("in getSubTypeBySub, sub = " + sub);
        HwFullNetworkConstantsInner.SubType subType = HwFullNetworkConstantsInner.SubType.ERROR;
        if (sub < 0 || sub >= HwFullNetworkConstantsInner.SIM_NUM || !this.mChipCommon.isSimInsertedArray[sub]) {
            loge("Error, sub = " + sub);
            return HwFullNetworkConstantsInner.SubType.ERROR;
        }
        logd("subCarrierTypeArray[sub] = " + this.mChipCommon.subCarrierTypeArray[sub]);
        switch (this.mChipCommon.subCarrierTypeArray[sub]) {
            case CARRIER_CMCC_USIM:
                return HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED;
            case CARRIER_CMCC_SIM:
                return HwFullNetworkConstantsInner.SubType.CARRIER;
            case CARRIER_CU_USIM:
                return HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED;
            case CARRIER_CU_SIM:
                return HwFullNetworkConstantsInner.SubType.CARRIER;
            case CARRIER_FOREIGN_USIM:
            case CARRIER_FOREIGN_CSIM:
                return HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER_PREFERRED;
            default:
                return HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public boolean isMainSlotFound() {
        return this.isMainSlotFound;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
