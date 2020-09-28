package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.telephony.RlogEx;

public class HwFullNetworkOperatorAIS implements HwFullNetworkOperatorBase {
    private static final String LOG_TAG = "HwFullNetworkOperatorAIS";
    private HwFullNetworkChipCommon mChipCommon;
    private HwFullNetworkChipHisi mChipHisi;
    private HwFullNetworkChipOther mChipOther;
    private boolean mIsMainSlotFound;

    public HwFullNetworkOperatorAIS() {
        this.mChipCommon = null;
        this.mChipHisi = null;
        this.mChipOther = null;
        this.mIsMainSlotFound = false;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public int getDefaultMainSlot(boolean forceSwitch) {
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        return getDefaultMainSlotForQcomAndMtk();
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
        logd("getDefaultMainSlotForQcomAndMtk enter");
        int curSimCount = 0;
        int noSimCount = 0;
        this.mIsMainSlotFound = false;
        int default4GSlot = 0;
        for (int slotId = 0; slotId < HwFullNetworkConstantsInner.SIM_NUM; slotId++) {
            if (true == this.mChipCommon.isSimInsertedArray[slotId]) {
                curSimCount++;
                if (!this.mIsMainSlotFound) {
                    default4GSlot = slotId;
                }
                this.mIsMainSlotFound = true;
            } else {
                noSimCount++;
            }
        }
        logd("curSimCount =" + curSimCount + ", noSimCount = " + noSimCount);
        if (HwFullNetworkConstantsInner.SIM_NUM != curSimCount + noSimCount || curSimCount == 0) {
            loge("cards status error or all cards absent.");
            this.mIsMainSlotFound = false;
            return this.mChipCommon.getUserSwitchDualCardSlots();
        } else if (curSimCount == 1) {
            return default4GSlot;
        } else {
            initSubTypes();
            int mainSub = this.mChipOther.getMainCardSubByPriority(HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED);
            logd(this.mChipOther.currentSubTypeMap.toString());
            logd("4G slot sub is " + mainSub);
            switch (mainSub) {
                case 0:
                case 1:
                    return mainSub;
                case 10:
                case HwFullNetworkConstantsInner.SUB_NO_CMCC:
                    logd("The two cards inserted have the same priority ");
                    return this.mChipCommon.getDefaultMainSlotByIccId(this.mChipCommon.getUserSwitchDualCardSlots());
                default:
                    this.mIsMainSlotFound = false;
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

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public boolean isMainSlotFound() {
        return this.mIsMainSlotFound;
    }

    public HwFullNetworkConstantsInner.SubType getSubTypeBySub(int slot) {
        HwFullNetworkConstantsInner.SubType subType;
        logd("in getSubTypeBySub, slot = " + slot);
        HwFullNetworkConstantsInner.SubType subType2 = HwFullNetworkConstantsInner.SubType.ERROR;
        if (slot < 0 || slot >= HwFullNetworkConstantsInner.SIM_NUM || !this.mChipCommon.isSimInsertedArray[slot]) {
            loge("getSubTypeBySub Error, slot = " + slot);
            return HwFullNetworkConstantsInner.SubType.ERROR;
        }
        logd("subCarrierTypeArray[slot] = " + this.mChipCommon.subCarrierTypeArray[slot]);
        switch (this.mChipCommon.subCarrierTypeArray[slot]) {
            case CARRIER_FOREIGN_AIS_USIM:
                subType = HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED;
                break;
            default:
                subType = HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER;
                break;
        }
        return subType;
    }
}
