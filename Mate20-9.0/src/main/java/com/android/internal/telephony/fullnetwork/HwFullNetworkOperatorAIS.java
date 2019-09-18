package com.android.internal.telephony.fullnetwork;

import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;

public class HwFullNetworkOperatorAIS implements HwFullNetworkOperatorBase {
    private static final String LOG_TAG = "HwFullNetworkOperatorAIS";
    private HwFullNetworkChipCommon mChipCommon;
    private HwFullNetworkChipHisi mChipHisi;
    private HwFullNetworkChipOther mChipOther;
    private boolean mIsMainSlotFound;

    /* renamed from: com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorAIS$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstants$SubCarrierType = new int[HwFullNetworkConstants.SubCarrierType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstants$SubCarrierType[HwFullNetworkConstants.SubCarrierType.CARRIER_FOREIGN_AIS_USIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    public HwFullNetworkOperatorAIS() {
        this.mChipCommon = null;
        this.mChipHisi = null;
        this.mChipOther = null;
        this.mIsMainSlotFound = false;
        this.mChipCommon = HwFullNetworkChipCommon.getInstance();
    }

    public int getDefaultMainSlot(boolean forceSwitch) {
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        return getDefaultMainSlotForQcomAndMtk();
    }

    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public int getDefaultMainSlotForQcomAndMtk() {
        logd("getDefaultMainSlotForQcomAndMtk enter");
        this.mIsMainSlotFound = false;
        int default4GSlot = 0;
        int noSimCount = 0;
        int curSimCount = 0;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (true == this.mChipCommon.isSimInsertedArray[i]) {
                curSimCount++;
                if (!this.mIsMainSlotFound) {
                    default4GSlot = i;
                }
                this.mIsMainSlotFound = true;
            } else {
                noSimCount++;
            }
        }
        logd("curSimCount =" + curSimCount + ", noSimCount = " + noSimCount);
        if (HwFullNetworkConstants.SIM_NUM != curSimCount + noSimCount || curSimCount == 0) {
            loge("cards status error or all cards absent.");
            this.mIsMainSlotFound = false;
            return this.mChipCommon.getUserSwitchDualCardSlots();
        } else if (curSimCount == 1) {
            return default4GSlot;
        } else {
            initSubTypes();
            int mainSub = this.mChipOther.getMainCardSubByPriority(HwFullNetworkConstants.SubType.CARRIER_PREFERRED);
            logd(this.mChipOther.currentSubTypeMap.toString());
            logd("4G slot sub is " + mainSub);
            switch (mainSub) {
                case 0:
                case 1:
                    return mainSub;
                case 10:
                case 11:
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
        for (int index = 0; index < HwFullNetworkConstants.SIM_NUM; index++) {
            this.mChipOther.currentSubTypeMap.put(Integer.valueOf(index), getSubTypeBySub(index));
        }
    }

    public boolean isMainSlotFound() {
        return this.mIsMainSlotFound;
    }

    private boolean isVaildSub(int sub) {
        return sub >= 0 && sub <= HwFullNetworkConstants.SIM_NUM;
    }

    public HwFullNetworkConstants.SubType getSubTypeBySub(int sub) {
        HwFullNetworkConstants.SubType subType;
        logd("in getSubTypeBySub, sub = " + sub);
        HwFullNetworkConstants.SubType subType2 = HwFullNetworkConstants.SubType.ERROR;
        if (!SubscriptionManager.isValidSubscriptionId(sub) || !this.mChipCommon.isSimInsertedArray[sub]) {
            loge("Error, sub = " + sub);
            return HwFullNetworkConstants.SubType.ERROR;
        }
        logd("subCarrierTypeArray[sub] = " + this.mChipCommon.subCarrierTypeArray[sub]);
        if (AnonymousClass1.$SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstants$SubCarrierType[this.mChipCommon.subCarrierTypeArray[sub].ordinal()] != 1) {
            subType = HwFullNetworkConstants.SubType.FOREIGN_CARRIER;
        } else {
            subType = HwFullNetworkConstants.SubType.CARRIER_PREFERRED;
        }
        return subType;
    }
}
