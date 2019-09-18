package com.android.internal.telephony.fullnetwork;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import java.util.HashMap;
import java.util.Map;

public class HwFullNetworkChipOther implements HwFullNetworkChipCommon.HwFullNetworkChipInterface {
    private static final String LOG_TAG = "HwFullNetworkChipOther";
    private static final String MAIN_CARD_INDEX = "main_card_id";
    private static final String NETWORK_MODE_2G_ONLY = "network_mode_2G_only";
    private static final String NETWORK_MODE_3G_PRE = "network_mode_3G_pre";
    private static final String NETWORK_MODE_4G_PRE = "network_mode_4G_pre";
    static final String PROP_MAIN_STACK = "persist.radio.msim.stackid_0";
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkChipOther mInstance;
    private static final Object mLock = new Object();
    Map<Integer, HwFullNetworkConstants.SubType> currentSubTypeMap = new HashMap();
    int is4GSlotReviewNeeded = 0;
    private Context mContext;
    boolean[] mGetUiccCardsStatusDone = new boolean[HwFullNetworkConstants.SIM_NUM];
    int[] mModemPreferMode = new int[HwFullNetworkConstants.SIM_NUM];
    boolean mNeedExchangeDB = false;
    boolean mNeedSetAllowData = false;
    boolean mNeedSetLteServiceAbility = false;
    boolean mNeedSetSecondStack = false;
    int mNumOfGetPrefNwModeSuccess = 0;
    int mPrimaryStackNetworkType = -1;
    int mPrimaryStackPhoneId = -1;
    int mSecondaryStackNetworkType = -1;
    int mSecondaryStackPhoneId = -1;
    int mSetPrimaryStackPrefMode = -1;
    int mSetSecondaryStackPrefMode = -1;
    int[] mSetUiccSubscriptionResult = new int[HwFullNetworkConstants.SIM_NUM];
    IccCardStatus.CardState[] mUiccCardsStatus = new IccCardStatus.CardState[HwFullNetworkConstants.SIM_NUM];
    int mUserPref4GSlot = 0;
    int[] nwModeArray = new int[HwFullNetworkConstants.SIM_NUM];
    boolean updateUserDefaultFlag = false;

    private HwFullNetworkChipOther(Context context, CommandsInterface[] ci) {
        this.mContext = context;
        logd("HwFullNetworkChipOther constructor");
    }

    static HwFullNetworkChipOther make(Context context, CommandsInterface[] ci) {
        HwFullNetworkChipOther hwFullNetworkChipOther;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwFullNetworkChipOther(context, ci);
                mChipCommon = HwFullNetworkChipCommon.getInstance();
                mChipCommon.setChipInterface(mInstance);
                hwFullNetworkChipOther = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkChipOther.make() should only be called once");
            }
        }
        return hwFullNetworkChipOther;
    }

    static HwFullNetworkChipOther getInstance() {
        HwFullNetworkChipOther hwFullNetworkChipOther;
        synchronized (mLock) {
            if (mInstance != null) {
                hwFullNetworkChipOther = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkChipOther.getInstance can't be called before make()");
            }
        }
        return hwFullNetworkChipOther;
    }

    public int getMainCardSubByPriority(HwFullNetworkConstants.SubType targetType) {
        logd("in getMainCardSubByPriority, input targetType = " + targetType);
        int count = 0;
        for (Map.Entry<Integer, HwFullNetworkConstants.SubType> mapEntry : this.currentSubTypeMap.entrySet()) {
            if (mapEntry.getValue() == targetType) {
                count++;
            }
        }
        logd("count = " + count);
        if (1 == count) {
            if (targetType == HwFullNetworkConstants.SubType.CARRIER_PREFERRED || targetType == HwFullNetworkConstants.SubType.CARRIER) {
                return getKeyFromMap(this.currentSubTypeMap, targetType).intValue();
            }
            return 11;
        } else if (1 < count) {
            logd("The priority is the same between two slots: return SAME_PRIORITY");
            return 10;
        } else if (targetType.ordinal() < HwFullNetworkConstants.SubType.ERROR.ordinal()) {
            return getMainCardSubByPriority(HwFullNetworkConstants.SubType.values()[targetType.ordinal() + 1]);
        } else {
            return -1;
        }
    }

    private Integer getKeyFromMap(Map<Integer, HwFullNetworkConstants.SubType> map, HwFullNetworkConstants.SubType type) {
        for (Map.Entry<Integer, HwFullNetworkConstants.SubType> mapEntry : map.entrySet()) {
            if (mapEntry.getValue() == type) {
                return mapEntry.getKey();
            }
        }
        return -1;
    }

    public int judgeDefault4GSlotForSingleSim(int defaultMainSlot) {
        int i = 0;
        while (HwFullNetworkConstants.SIM_NUM > i && !mChipCommon.isSimInsertedArray[i]) {
            logd("isSimInsertedArray[" + i + "] = " + mChipCommon.isSimInsertedArray[i]);
            i++;
        }
        if (HwFullNetworkConstants.SIM_NUM == i) {
            logd("there is no sim card inserted, error happen!!");
            return defaultMainSlot;
        }
        logd("there is only one card inserted, set it as the 4G slot");
        return i;
    }

    public boolean isSetDefault4GSlotNeeded(int lteSlotId) {
        boolean isSetDefault4GSlot;
        boolean isSetDefault4GSlot2 = true;
        if (lteSlotId != mChipCommon.getUserSwitchDualCardSlots()) {
            logd("lte slot is not the same, return true");
            return true;
        } else if (HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
            return false;
        } else {
            if (-1 == getExpectedMaxCapabilitySubId(lteSlotId)) {
                isSetDefault4GSlot2 = false;
            }
            logd("isSetDefault4GSlotNeeded:" + isSetDefault4GSlot);
            return isSetDefault4GSlot;
        }
    }

    public int getExpectedMaxCapabilitySubId(int ddsSubId) {
        int expectedMaxCapSubId = -1;
        int cdmaCardNums = 0;
        int cdmaSubId = -1;
        int CurrentMaxCapabilitySubId = SystemProperties.getInt(PROP_MAIN_STACK, 0);
        ProxyController.getInstance().syncRadioCapability(CurrentMaxCapabilitySubId);
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (mChipCommon.subCarrierTypeArray[i].isCCard()) {
                cdmaSubId = i;
                cdmaCardNums++;
            }
        }
        if (1 == cdmaCardNums && CurrentMaxCapabilitySubId != cdmaSubId) {
            expectedMaxCapSubId = cdmaSubId;
        } else if (2 == cdmaCardNums && CurrentMaxCapabilitySubId != ddsSubId) {
            expectedMaxCapSubId = ddsSubId;
        }
        logd("[getExpectedMaxCapabilitySubId] cdmaCardNums=" + cdmaCardNums + " expectedMaxCapSubId=" + expectedMaxCapSubId + " CurrentMaxCapabilitySubId=" + CurrentMaxCapabilitySubId);
        return expectedMaxCapSubId;
    }

    /* access modifiers changed from: package-private */
    public void judgeNwMode(int lteSlotId) {
        int otherNWModeInCMCC;
        int nwMode4GforCT;
        int nwMode4GforCMCC;
        int nwMode4GforCU;
        int nwMode4GforCMCC2;
        boolean is4GAbilityOn = 1 == HwTelephonyManagerInner.getDefault().getLteServiceAbility();
        logd("judgeNwMode: the LTE slot will be " + lteSlotId + " with the is4GAbilityOn = " + is4GAbilityOn);
        if (is4GAbilityOn) {
            nwMode4GforCU = 9;
            if (HwFullNetworkConfig.DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCMCC = 17;
            } else {
                nwMode4GforCMCC = 20;
            }
            nwMode4GforCT = 10;
            otherNWModeInCMCC = 9;
        } else {
            nwMode4GforCU = 3;
            if (HwFullNetworkConfig.DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCMCC2 = 16;
            } else {
                nwMode4GforCMCC2 = 18;
            }
            nwMode4GforCT = 7;
            otherNWModeInCMCC = 3;
        }
        logd("judgeNwMode: subCarrierTypeArray[" + lteSlotId + "] = " + mChipCommon.subCarrierTypeArray[lteSlotId]);
        if (true == HwFullNetworkConfig.IS_FULL_NETWORK_SUPPORTED) {
            judgeNwModeForFullNetwork(lteSlotId, nwMode4GforCU, nwMode4GforCMCC, nwMode4GforCT);
        } else if (true == HwFullNetworkConfig.IS_CMCC_CU_DSDX_ENABLE) {
            judgeNwModeForCMCC_CU(lteSlotId, nwMode4GforCMCC, nwMode4GforCU);
            judgePreNwModeSubIdAndListForCU(lteSlotId);
        } else if (true == HwFullNetworkConfig.IS_CMCC_4G_DSDX_ENABLE) {
            judgeNwModeForCMCC(lteSlotId, nwMode4GforCMCC, otherNWModeInCMCC);
            judgePreNwModeSubIdAndListForCMCC(lteSlotId);
        } else if (true == HwFullNetworkConfig.IS_CHINA_TELECOM) {
            judgeNwModeForCT(lteSlotId, nwMode4GforCT, nwMode4GforCU);
        } else {
            logd("judgeNwMode: do nothing.");
        }
    }

    private void judgeNwModeForCT(int lteSlotId, int CT_DefaultMode, int UMTS_DefaultMode) {
        logd("judgeNwModeForCT, lteSlotId = " + lteSlotId);
        this.nwModeArray[lteSlotId] = CT_DefaultMode;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
        }
        logd("judgeNwModeForCT prefer sub network mode is " + this.nwModeArray[lteSlotId]);
    }

    private void judgeNwModeForCMCC_CU(int lteSlotId, int TD_DefaultMode, int UMTS_DefaultMode) {
        logd("judgeNwModeForCMCC_CU, lteSlotId = " + lteSlotId);
        if (mChipCommon.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
            this.nwModeArray[lteSlotId] = TD_DefaultMode;
        } else {
            this.nwModeArray[lteSlotId] = UMTS_DefaultMode;
        }
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
        }
        logd("judgeNwModeForCMCC_CU prefer sub network mode is " + this.nwModeArray[lteSlotId]);
    }

    private void putPreNWModeListToDBforCMCC() {
        logd("in putPreNWModeListToDBforCMCC");
        ContentResolver resolver = this.mContext.getContentResolver();
        int i = HwFullNetworkConfig.DEFAULT_NETWORK_MODE;
        if (i == 17) {
            Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, 17);
            Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, 16);
            Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
        } else if (i != 20) {
            Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, -1);
        } else {
            Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, 20);
            Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, 18);
            Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
        }
    }

    public void judgePreNwModeSubIdAndListForCU(int lteSlotId) {
        logd("in judgePreNwModeSubIdAndListForCU ");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (mChipCommon.subCarrierTypeArray[lteSlotId].isCCard()) {
            Settings.System.putInt(resolver, MAIN_CARD_INDEX, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, -1);
        } else {
            Settings.System.putInt(resolver, MAIN_CARD_INDEX, lteSlotId);
            if (mChipCommon.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
                putPreNWModeListToDBforCMCC();
            } else {
                Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, 9);
                Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, 3);
                Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
            }
        }
        try {
            logd("main card index: " + Settings.System.getInt(resolver, MAIN_CARD_INDEX) + " network mode 4G pre: " + Settings.System.getInt(resolver, NETWORK_MODE_4G_PRE) + " network mode 3G pre: " + Settings.System.getInt(resolver, NETWORK_MODE_3G_PRE) + " network mode 2G only: " + Settings.System.getInt(resolver, NETWORK_MODE_2G_ONLY));
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading PreNwMode SubId AndList Values");
        }
    }

    public void judgeNwModeForCMCC(int lteSlotId, int nwMode4GforCMCC, int otherNWModeInCMCC) {
        if (mChipCommon.subCarrierTypeArray[lteSlotId].isCUCard() || mChipCommon.subCarrierTypeArray[lteSlotId].isCCard()) {
            this.nwModeArray[lteSlotId] = otherNWModeInCMCC;
        } else {
            this.nwModeArray[lteSlotId] = nwMode4GforCMCC;
        }
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
            logd("judgeNwModeForCMCC, slotId = " + i + "nwModeArray[i] = " + this.nwModeArray[i]);
        }
    }

    public void judgePreNwModeSubIdAndListForCMCC(int lteSlotId) {
        logd("in judgePreNwModeSubIdAndListForCMCC ");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (mChipCommon.subCarrierTypeArray[lteSlotId].isCUCard() || mChipCommon.subCarrierTypeArray[lteSlotId].isCCard()) {
            Settings.System.putInt(resolver, MAIN_CARD_INDEX, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, -1);
            Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, -1);
        } else {
            Settings.System.putInt(resolver, MAIN_CARD_INDEX, lteSlotId);
            putPreNWModeListToDBforCMCC();
        }
        try {
            logd("main card index: " + Settings.System.getInt(resolver, MAIN_CARD_INDEX) + " network mode 4G pre: " + Settings.System.getInt(resolver, NETWORK_MODE_4G_PRE) + " network mode 3G pre: " + Settings.System.getInt(resolver, NETWORK_MODE_3G_PRE) + " network mode 2G only: " + Settings.System.getInt(resolver, NETWORK_MODE_2G_ONLY));
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading PreNwMode SubId AndList Values");
        }
    }

    private void judgeNwModeForFullNetwork(int lteSlotId, int nwMode4GforCU, int nwMode4GforCMCC, int nwMode4GforCT) {
        logd("judgeNwModeForFullNetwork start");
        if (mChipCommon.subCarrierTypeArray[lteSlotId].isCCard()) {
            this.nwModeArray[lteSlotId] = nwMode4GforCT;
        } else if (mChipCommon.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
            this.nwModeArray[lteSlotId] = nwMode4GforCMCC;
        } else {
            this.nwModeArray[lteSlotId] = nwMode4GforCU;
        }
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (i != lteSlotId) {
                if (!HwFullNetworkConfig.IS_CARD2_CDMA_SUPPORTED) {
                    this.nwModeArray[i] = 1;
                } else if (!mChipCommon.subCarrierTypeArray[i].isCCard() || mChipCommon.subCarrierTypeArray[lteSlotId].isCCard()) {
                    this.nwModeArray[i] = 3;
                } else {
                    this.nwModeArray[i] = 7;
                }
            }
        }
    }

    public void refreshCardState() {
        for (int index = 0; index < HwFullNetworkConstants.SIM_NUM; index++) {
            boolean z = true;
            boolean isSubActivated = SubscriptionController.getInstance().getSubState(index) == 1;
            boolean[] zArr = mChipCommon.isSimInsertedArray;
            if (!mChipCommon.isCardPresent(index) || !isSubActivated) {
                z = false;
            }
            zArr[index] = z;
        }
    }

    /* access modifiers changed from: package-private */
    public int getNetworkTypeFromDB(int phoneId) {
        return HwNetworkTypeUtils.getNetworkModeFromDB(mChipCommon.mContext, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void setNetworkTypeToDB(int phoneId, int prefMode) {
        HwNetworkTypeUtils.saveNetworkModeToDB(mChipCommon.mContext, phoneId, prefMode);
    }

    /* access modifiers changed from: package-private */
    public void setLteServiceAbility() {
        HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();
        if (mHwTelephonyManager != null && this.mNeedSetLteServiceAbility) {
            int ability = mHwTelephonyManager.getLteServiceAbility();
            logd("setLteServiceAbility:" + ability);
            this.mNeedSetSecondStack = true;
            mHwTelephonyManager.setLteServiceAbility(ability);
            this.mNeedSetLteServiceAbility = false;
        }
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        UiccController mUiccController = mChipCommon.getUiccController();
        boolean z = false;
        if (mUiccController == null || mUiccController.getUiccCards() == null || mUiccController.getUiccCards().length < 2) {
            loge("haven't get all UiccCards done, please wait!");
            return false;
        }
        for (UiccCard uc : mUiccController.getUiccCards()) {
            if (uc == null) {
                loge("haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        if (!mChipCommon.isSwitchSlotEnabledForCMCC()) {
            logd("isSwitchSlotEnabledForCMCC: CMCC hybird and CMCC is not roaming return false");
            return false;
        } else if (HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE && mChipCommon.isCTHybird()) {
            logd("isSwitchSlotEnabledForCT: CMCC hybird and CMCC is not roaming return false");
            return false;
        } else if (HwFullNetworkConfig.IS_CHINA_TELECOM) {
            return false;
        } else {
            refreshCardState();
            if (mChipCommon.isSimInsertedArray[0] && mChipCommon.isSimInsertedArray[1]) {
                z = true;
            }
            return z;
        }
    }

    public void resetUiccSubscriptionResultFlag(int slotId) {
        if (slotId >= 0 && slotId < HwFullNetworkConstants.SIM_NUM) {
            logd("UiccSubscriptionResult:  slotId=" + slotId + "PreResult:" + this.mSetUiccSubscriptionResult[slotId]);
            this.mSetUiccSubscriptionResult[slotId] = -1;
        }
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForIccChanged(Handler h) {
    }

    public boolean getWaitingSwitchBalongSlot() {
        return false;
    }

    public boolean isSet4GDoneAfterSimInsert() {
        return false;
    }

    public boolean isSettingDefaultData() {
        return false;
    }

    public int getBalongSimSlot() {
        return 0;
    }

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
    }

    public int getSpecCardType(int slotId) {
        return -1;
    }

    public boolean isRestartRildProgress() {
        return false;
    }

    public String getFullIccid(int subId) {
        return null;
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
