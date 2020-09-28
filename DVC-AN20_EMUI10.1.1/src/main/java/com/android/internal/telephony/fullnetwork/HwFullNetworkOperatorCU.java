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
        boolean onlyCard1Present;
        boolean onlyCard2Present = true;
        int temSub = this.mChipCommon.getUserSwitchDualCardSlots();
        this.mChipCommon.getUiccController().getUiccCards();
        boolean isCard1Present = this.mChipHisi.isSimPresentBySubState(0);
        boolean isCard2Present = this.mChipHisi.isSimPresentBySubState(1);
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
        } else {
            logd("card present state is not normal.");
        }
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", defaultMainSlot " + temSub);
        return temSub;
    }

    public int getDefaultMainSlotForQcomAndMtk() {
        int i = 0;
        logd("judgeDefault4GSlotForCU enter");
        int curSimCount = 0;
        int noSimCount = 0;
        this.isMainSlotFound = false;
        int default4GSlot = 0;
        for (int i2 = 0; i2 < HwFullNetworkConstantsInner.SIM_NUM; i2++) {
            if (this.mChipCommon.isSimInsertedArray[i2]) {
                curSimCount++;
                if (!this.isMainSlotFound) {
                    default4GSlot = i2;
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
                i = this.mChipCommon.getUserSwitchDualCardSlots();
            }
            return i;
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
                case HwFullNetworkConstantsInner.SUB_NO_CMCC:
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
        HwFullNetworkConstantsInner.SubType subType;
        logd("in getSubTypeBySub, sub = " + sub);
        HwFullNetworkConstantsInner.SubType subType2 = HwFullNetworkConstantsInner.SubType.ERROR;
        if (sub < 0 || sub >= HwFullNetworkConstantsInner.SIM_NUM || !this.mChipCommon.isSimInsertedArray[sub]) {
            loge("Error, sub = " + sub);
            return HwFullNetworkConstantsInner.SubType.ERROR;
        }
        logd("subCarrierTypeArray[sub] = " + this.mChipCommon.subCarrierTypeArray[sub]);
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[this.mChipCommon.subCarrierTypeArray[sub].ordinal()]) {
            case 1:
                subType = HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED;
                break;
            case 2:
                subType = HwFullNetworkConstantsInner.SubType.CARRIER;
                break;
            case HwFullNetworkConstantsInner.CARD_TYPE_DUAL_MODE:
                subType = HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED;
                break;
            case 4:
                subType = HwFullNetworkConstantsInner.SubType.CARRIER;
                break;
            case HwFullNetworkConstantsInner.MCCMNC_LEN_MINIMUM:
            case 6:
                subType = HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER_PREFERRED;
                break;
            default:
                subType = HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER;
                break;
        }
        return subType;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorCU$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType = new int[HwFullNetworkConstantsInner.SubCarrierType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CMCC_USIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CMCC_SIM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CU_USIM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CU_SIM.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_USIM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_CSIM.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_SIM.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_RUIM.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public boolean isMainSlotFound() {
        return this.isMainSlotFound;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
