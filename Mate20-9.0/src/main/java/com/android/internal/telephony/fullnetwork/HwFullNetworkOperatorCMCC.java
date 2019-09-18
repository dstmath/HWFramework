package com.android.internal.telephony.fullnetwork;

import android.telephony.Rlog;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;

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

    public int getDefaultMainSlot(boolean forceSwitch) {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            this.mChipHisi = HwFullNetworkChipHisi.getInstance();
            return getDefaultMainSlotForHisi(forceSwitch);
        }
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        return getDefaultMainSlotForQcom();
    }

    public int getDefaultMainSlotForHisi(boolean forceSwitch) {
        int temSub = this.mChipCommon.getUserSwitchDualCardSlots();
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (this.mChipCommon.mIccIds[i] == null) {
                logd("mIccIds[" + i + "] is null, and return");
                return temSub;
            }
        }
        UiccCard[] mUiccCards = this.mChipCommon.mUiccController.getUiccCards();
        boolean isCard1Present = mUiccCards[0].getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT || this.mChipHisi.mSwitchTypes[0] > 0;
        boolean isCard2Present = mUiccCards[1].getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT || this.mChipHisi.mSwitchTypes[1] > 0;
        boolean isAnySimCardChanged = this.mChipHisi.anySimCardChanged() || this.mChipHisi.isPreBootCompleted || forceSwitch || this.mChipHisi.isHotPlugCompleted;
        if (this.mChipHisi.isPreBootCompleted) {
            logd("judgeDefaultSlotId4HisiCmcc: reset isPreBootCompleted.");
            this.mChipHisi.isPreBootCompleted = false;
        }
        if (this.mChipHisi.isHotPlugCompleted) {
            logd("judgeDefaultSlotId4HisiCmcc: reset isHotPlugCompleted.");
            this.mChipHisi.isHotPlugCompleted = false;
        }
        logd("judgeDefaultSlotId4HisiCmcc isAnySimCardChanged = " + isAnySimCardChanged);
        if (isCard1Present && !isCard2Present) {
            temSub = 0;
        } else if (!isCard1Present && isCard2Present) {
            temSub = 1;
        } else if (isCard1Present && isCard2Present) {
            if (isAnySimCardChanged || !HwFullNetworkConfig.IS_HISI_DSDX) {
                boolean[] isCmccCards = new boolean[HwFullNetworkConstants.SIM_NUM];
                int cardtype1 = (this.mChipHisi.mCardTypes[0] & 240) >> 4;
                int cardtype2 = (this.mChipHisi.mCardTypes[1] & 240) >> 4;
                for (int i2 = 0; i2 < HwFullNetworkConstants.SIM_NUM; i2++) {
                    isCmccCards[i2] = this.mChipCommon.isCMCCCardBySlotId(i2);
                }
                if (isCmccCards[0] && isCmccCards[1]) {
                    if (cardtype1 == 2 && cardtype2 == 1) {
                        temSub = !this.mChipHisi.isBalongSimSynced();
                    } else if (cardtype1 == 1 && cardtype2 == 2) {
                        temSub = this.mChipHisi.isBalongSimSynced();
                    }
                    temSub = this.mChipCommon.getDefaultMainSlotByIccId(temSub);
                } else if (isCmccCards[0]) {
                    temSub = 0;
                } else if (isCmccCards[1]) {
                    temSub = 1;
                } else {
                    temSub = this.mChipCommon.getDefaultMainSlotByIccId(temSub);
                }
                logd("cardtype1 = " + cardtype1 + ", cardtype2 = " + cardtype2 + ", isCmccCards[SUB1] " + isCmccCards[0] + ", isCmccCards[SUB2] " + isCmccCards[1]);
            } else {
                logd("judgeDefaultSlotId4HisiCmcc all sim present but none sim change ");
                return temSub;
            }
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

    public int getDefaultMainSlotForQcom() {
        logd("judgeDefault4GSlotForCMCC enter");
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
            return this.mChipCommon.getUserSwitchDualCardSlots();
        } else if (1 == curSimCount) {
            return default4GSlot;
        } else {
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
        for (int index = 0; index < HwFullNetworkConstants.SIM_NUM; index++) {
            this.mChipOther.currentSubTypeMap.put(Integer.valueOf(index), getSubTypeBySub(index));
        }
    }

    public boolean isMainSlotFound() {
        return this.isMainSlotFound;
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
            case CARRIER_CU_SIM:
                subType = HwFullNetworkConstants.SubType.LOCAL_CARRIER;
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
}
