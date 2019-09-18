package com.android.internal.telephony.fullnetwork;

import android.telephony.Rlog;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;

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
        }
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", defaultMainSlot " + temSub);
        return temSub;
    }

    public int getDefaultMainSlotForQcomAndMtk() {
        int mainSlotByIccId;
        logd("judgeDefault4GSlotForCU enter");
        this.isMainSlotFound = false;
        int default4GSlot = 0;
        int noSimCount = 0;
        int curSimCount = 0;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (true == this.mChipCommon.isSimInsertedArray[i]) {
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
        if (HwFullNetworkConstants.SIM_NUM != curSimCount + noSimCount || curSimCount == 0) {
            loge("cards status error or all cards absent.");
            this.isMainSlotFound = false;
            return 0;
        } else if (1 == curSimCount) {
            return default4GSlot;
        } else {
            if (HwFullNetworkConfig.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
                logd("judgeDefault4GSlotForCU, mainSlot:" + mainSlot + ", mainSlotByIccId: " + mainSlotByIccId);
                return mainSlotByIccId;
            }
            initSubTypes();
            int mSub = this.mChipOther.getMainCardSubByPriority(HwFullNetworkConstants.SubType.CARRIER_PREFERRED);
            logd(this.mChipOther.currentSubTypeMap.toString());
            logd("4G slot sub is " + mSub);
            switch (mSub) {
                case 0:
                case 1:
                    return mSub;
                case 10:
                case 11:
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
        for (int index = 0; index < HwFullNetworkConstants.SIM_NUM; index++) {
            this.mChipOther.currentSubTypeMap.put(Integer.valueOf(index), getSubTypeBySub(index));
        }
    }

    public HwFullNetworkConstants.SubType getSubTypeBySub(int sub) {
        HwFullNetworkConstants.SubType subType;
        logd("in getSubTypeBySub, sub = " + sub);
        HwFullNetworkConstants.SubType subType2 = HwFullNetworkConstants.SubType.ERROR;
        if (sub < 0 || sub >= HwFullNetworkConstants.SIM_NUM || !this.mChipCommon.isSimInsertedArray[sub]) {
            loge("Error, sub = " + sub);
            return HwFullNetworkConstants.SubType.ERROR;
        }
        logd("subCarrierTypeArray[sub] = " + this.mChipCommon.subCarrierTypeArray[sub]);
        switch (this.mChipCommon.subCarrierTypeArray[sub]) {
            case CARRIER_CMCC_USIM:
                subType = HwFullNetworkConstants.SubType.CARRIER_PREFERRED;
                break;
            case CARRIER_CMCC_SIM:
                subType = HwFullNetworkConstants.SubType.CARRIER;
                break;
            case CARRIER_CU_USIM:
                subType = HwFullNetworkConstants.SubType.CARRIER_PREFERRED;
                break;
            case CARRIER_CU_SIM:
                subType = HwFullNetworkConstants.SubType.CARRIER;
                break;
            case CARRIER_FOREIGN_USIM:
            case CARRIER_FOREIGN_CSIM:
                subType = HwFullNetworkConstants.SubType.FOREIGN_CARRIER_PREFERRED;
                break;
            default:
                subType = HwFullNetworkConstants.SubType.FOREIGN_CARRIER;
                break;
        }
        return subType;
    }

    public boolean isMainSlotFound() {
        return this.isMainSlotFound;
    }

    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
