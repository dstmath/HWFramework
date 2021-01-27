package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.telephony.RlogEx;

public class HwFullNetworkOperatorCustom implements HwFullNetworkOperatorBase {
    private static final String LOG_TAG = "HwFullNetworkOperatorCustom";
    private HwFullNetworkChipCommon mChipCommon;
    private HwFullNetworkChipHisi mChipHisi;
    private HwFullNetworkChipOther mChipOther;
    private boolean mIsMainSlotFound;

    public HwFullNetworkOperatorCustom() {
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
        RlogEx.i(LOG_TAG, msg);
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
            if (this.mChipCommon.isSimInsertedArray[slotId]) {
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
        if (curSimCount + noSimCount != HwFullNetworkConstantsInner.SIM_NUM || curSimCount == 0) {
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
            if (mainSub == 0 || mainSub == 1) {
                return mainSub;
            }
            if (mainSub == 10 || mainSub == 11) {
                logd("The two cards inserted have the same priority ");
                HwFullNetworkChipCommon hwFullNetworkChipCommon = this.mChipCommon;
                return hwFullNetworkChipCommon.getDefaultMainSlotByIccId(hwFullNetworkChipCommon.getUserSwitchDualCardSlots());
            }
            this.mIsMainSlotFound = false;
            HwFullNetworkChipCommon hwFullNetworkChipCommon2 = this.mChipCommon;
            return hwFullNetworkChipCommon2.getDefaultMainSlotByIccId(hwFullNetworkChipCommon2.getUserSwitchDualCardSlots());
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
        logd("in getSubTypeBySub, slot = " + slot);
        HwFullNetworkConstantsInner.SubType subType = HwFullNetworkConstantsInner.SubType.ERROR;
        if (slot < 0 || slot >= HwFullNetworkConstantsInner.SIM_NUM || !this.mChipCommon.isSimInsertedArray[slot]) {
            loge("getSubTypeBySub Error, slot = " + slot);
            return HwFullNetworkConstantsInner.SubType.ERROR;
        }
        this.mChipCommon.judgeSubCarrierTypeByMccMnc(slot);
        logd("subCarrierTypeArray[slot] = " + this.mChipCommon.subCarrierTypeArray[slot]);
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[this.mChipCommon.subCarrierTypeArray[slot].ordinal()];
        if (i == 1 || i == 2 || i == 3) {
            return HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED;
        }
        return HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorCustom$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType = new int[HwFullNetworkConstantsInner.SubCarrierType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_AIS_USIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_SMART_USIM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_MTN_USIM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }
}
