package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.telephony.RlogEx;

public class HwFullNetworkOperatorCMCC implements HwFullNetworkOperatorBase {
    private static final String LOG_TAG = "HwFullNetworkOperatorCMCC";
    private boolean isMainSlotFound;
    public HwFullNetworkChipCommon mChipCommon;
    public HwFullNetworkChipHisi mChipHisi;
    public HwFullNetworkChipOther mChipOther;

    public HwFullNetworkOperatorCMCC() {
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
        return getDefaultMainSlotForQcom();
    }

    public int getDefaultMainSlotForHisi(boolean forceSwitch) {
        boolean isAnySimCardChanged = true;
        int temSub = this.mChipCommon.getUserSwitchDualCardSlots();
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (this.mChipCommon.mIccIds[i] == null) {
                logd("mIccIds[" + i + "] is null, and return");
                return temSub;
            }
        }
        this.mChipCommon.mUiccController.getUiccCards();
        boolean isCard1Present = this.mChipHisi.isSimPresentBySubState(0);
        boolean isCard2Present = this.mChipHisi.isSimPresentBySubState(1);
        if (!(this.mChipHisi.anySimCardChanged() || this.mChipHisi.isPreBootCompleted || this.mChipHisi.isHotPlugCompleted) && !forceSwitch) {
            isAnySimCardChanged = false;
        }
        if (this.mChipHisi.isPreBootCompleted) {
            logd("judgeDefaultSlotId4HisiCmcc: reset isPreBootCompleted.");
            this.mChipHisi.isPreBootCompleted = false;
        }
        if (this.mChipHisi.isHotPlugCompleted) {
            logd("judgeDefaultSlotId4HisiCmcc: reset isHotPlugCompleted.");
            this.mChipHisi.isHotPlugCompleted = false;
        }
        logd("judgeDefaultSlotId4HisiCmcc isAnySimCardChanged = " + isAnySimCardChanged);
        int temSub2 = getDefaultMainSlotByCardStatus(temSub, isCard1Present, isCard2Present, isAnySimCardChanged);
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", defaultMainSlot " + temSub2);
        return temSub2;
    }

    private int getDefaultMainSlotByCardStatus(int sub, boolean isCard1Present, boolean isCard2Present, boolean isAnySimCardChanged) {
        int temSub = sub;
        if (isCard1Present && !isCard2Present) {
            temSub = 0;
        } else if (!isCard1Present && isCard2Present) {
            temSub = 1;
        } else if (!isCard1Present || !isCard2Present) {
            logd("both cards do not present.");
        } else if (isAnySimCardChanged || !HwFullNetworkConfigInner.IS_HISI_DSDX) {
            boolean[] isCmccCards = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
            int cardType1 = (this.mChipHisi.mCardTypes[0] & 240) >> 4;
            int cardType2 = (this.mChipHisi.mCardTypes[1] & 240) >> 4;
            for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                isCmccCards[i] = this.mChipCommon.isCMCCCardBySlotId(i);
            }
            if (isCmccCards[0] && isCmccCards[1]) {
                if (cardType1 == 2 && cardType2 == 1) {
                    temSub = this.mChipHisi.isBalongSimSynced() ? 0 : 1;
                } else if (cardType1 == 1 && cardType2 == 2) {
                    temSub = this.mChipHisi.isBalongSimSynced() ? 1 : 0;
                } else {
                    logd("getDefaultMainSlotByCardStatus two card types are same.");
                }
                temSub = this.mChipCommon.getDefaultMainSlotByIccId(temSub);
            } else if (isCmccCards[0]) {
                temSub = 0;
            } else if (isCmccCards[1]) {
                temSub = 1;
            } else {
                temSub = this.mChipCommon.getDefaultMainSlotByIccId(temSub);
            }
            logd("cardtype1 = " + cardType1 + ", cardtype2 = " + cardType2 + ", isCmccCards[SUB1] " + isCmccCards[0] + ", isCmccCards[SUB2] " + isCmccCards[1]);
        } else {
            logd("judgeDefaultSlotId4HisiCmcc all sim present but none sim change ");
            return temSub;
        }
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

    public int getDefaultMainSlotForQcom() {
        logd("judgeDefault4GSlotForCMCC enter");
        int curSimCount = 0;
        int noSimCount = 0;
        this.isMainSlotFound = false;
        int default4GSlot = 0;
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
            return this.mChipCommon.getUserSwitchDualCardSlots();
        } else if (curSimCount == 1) {
            return default4GSlot;
        } else {
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
                    logd("The two cards inserted have the same priority or no cmcc");
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

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorBase
    public boolean isMainSlotFound() {
        return this.isMainSlotFound;
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
                subType = HwFullNetworkConstantsInner.SubType.LOCAL_CARRIER;
                break;
            case 4:
            case HwFullNetworkConstantsInner.MCCMNC_LEN_MINIMUM:
                subType = HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER_PREFERRED;
                break;
            default:
                subType = HwFullNetworkConstantsInner.SubType.FOREIGN_CARRIER;
                break;
        }
        return subType;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.fullnetwork.HwFullNetworkOperatorCMCC$1  reason: invalid class name */
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
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CU_SIM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_USIM.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_CSIM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_SIM.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$fullnetwork$HwFullNetworkConstantsInner$SubCarrierType[HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_RUIM.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }
}
