package com.android.internal.telephony.fullnetwork;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.ProxyControllerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import java.util.HashMap;
import java.util.Map;

public class HwFullNetworkChipOther implements HwFullNetworkChipCommon.HwFullNetworkChipInterface {
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwFullNetworkChipOther";
    private static final String MAIN_CARD_INDEX = "main_card_id";
    private static final String NETWORK_MODE_2G_ONLY = "network_mode_2G_only";
    private static final String NETWORK_MODE_3G_PRE = "network_mode_3G_pre";
    private static final String NETWORK_MODE_4G_PRE = "network_mode_4G_pre";
    static final String PROP_MAIN_STACK = "persist.radio.msim.stackid_0";
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkChipOther mInstance;
    Map<Integer, HwFullNetworkConstantsInner.SubType> currentSubTypeMap = new HashMap();
    int is4GSlotReviewNeeded = 0;
    private Context mContext;
    boolean[] mGetUiccCardsStatusDone = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    int[] mModemPreferMode = new int[HwFullNetworkConstantsInner.SIM_NUM];
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
    int[] mSetUiccSubscriptionResult = new int[HwFullNetworkConstantsInner.SIM_NUM];
    boolean[] mSimHotPlugIn = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    IccCardStatusExt.CardStateEx[] mUiccCardsStatus = new IccCardStatusExt.CardStateEx[HwFullNetworkConstantsInner.SIM_NUM];
    int mUserPref4GSlot = 0;
    int[] nwModeArray = new int[HwFullNetworkConstantsInner.SIM_NUM];
    boolean updateUserDefaultFlag = false;

    private HwFullNetworkChipOther(Context context, CommandsInterfaceEx[] ci) {
        this.mContext = context;
        logd("HwFullNetworkChipOther constructor");
    }

    static HwFullNetworkChipOther make(Context context, CommandsInterfaceEx[] ci) {
        HwFullNetworkChipOther hwFullNetworkChipOther;
        synchronized (LOCK) {
            if (mInstance != null) {
                throw new RuntimeException("HwFullNetworkChipOther.make() should only be called once");
            }
            mInstance = new HwFullNetworkChipOther(context, ci);
            mChipCommon = HwFullNetworkChipCommon.getInstance();
            mChipCommon.setChipInterface(mInstance);
            hwFullNetworkChipOther = mInstance;
        }
        return hwFullNetworkChipOther;
    }

    static HwFullNetworkChipOther getInstance() {
        HwFullNetworkChipOther hwFullNetworkChipOther;
        synchronized (LOCK) {
            if (mInstance == null) {
                throw new RuntimeException("HwFullNetworkChipOther.getInstance can't be called before make()");
            }
            hwFullNetworkChipOther = mInstance;
        }
        return hwFullNetworkChipOther;
    }

    public int getMainCardSubByPriority(HwFullNetworkConstantsInner.SubType targetType) {
        if (targetType == null) {
            return -1;
        }
        logd("in getMainCardSubByPriority, input targetType = " + targetType);
        int count = 0;
        for (Map.Entry<Integer, HwFullNetworkConstantsInner.SubType> mapEntry : this.currentSubTypeMap.entrySet()) {
            if (mapEntry.getValue() == targetType) {
                count++;
            }
        }
        logd("count = " + count);
        if (count == 1) {
            if (targetType == HwFullNetworkConstantsInner.SubType.CARRIER_PREFERRED || targetType == HwFullNetworkConstantsInner.SubType.CARRIER) {
                return getKeyFromMap(this.currentSubTypeMap, targetType).intValue();
            }
            return 11;
        } else if (count > 1) {
            logd("The priority is the same between two slots: return SAME_PRIORITY");
            return 10;
        } else if (targetType.ordinal() < HwFullNetworkConstantsInner.SubType.ERROR.ordinal()) {
            return getMainCardSubByPriority(HwFullNetworkConstantsInner.SubType.values()[targetType.ordinal() + 1]);
        } else {
            return -1;
        }
    }

    private Integer getKeyFromMap(Map<Integer, HwFullNetworkConstantsInner.SubType> map, HwFullNetworkConstantsInner.SubType type) {
        for (Map.Entry<Integer, HwFullNetworkConstantsInner.SubType> mapEntry : map.entrySet()) {
            if (mapEntry.getValue() == type) {
                return mapEntry.getKey();
            }
        }
        return -1;
    }

    public int judgeDefault4GSlotForSingleSim(int defaultMainSlot) {
        int i = 0;
        while (HwFullNetworkConstantsInner.SIM_NUM > i && !mChipCommon.isSimInsertedArray[i]) {
            logd("isSimInsertedArray[" + i + "] = " + mChipCommon.isSimInsertedArray[i]);
            i++;
        }
        if (i == HwFullNetworkConstantsInner.SIM_NUM) {
            logd("there is no sim card inserted, error happen!!");
            return defaultMainSlot;
        }
        logd("there is only one card inserted, set it as the 4G slot");
        return i;
    }

    public boolean isSetDefault4GSlotNeeded(int lteSlotId) {
        boolean isSetDefault4GSlot = true;
        if (lteSlotId != mChipCommon.getUserSwitchDualCardSlots()) {
            logd("lte slot is not the same, return true");
            return true;
        } else if (HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            return false;
        } else {
            if (getExpectedMaxCapabilitySubId(lteSlotId) == -1) {
                isSetDefault4GSlot = false;
            }
            logd("isSetDefault4GSlotNeeded:" + isSetDefault4GSlot);
            return isSetDefault4GSlot;
        }
    }

    public int getExpectedMaxCapabilitySubId(int ddsSubId) {
        int expectedMaxCapSubId = -1;
        int cdmaCardNums = 0;
        int cdmaSubId = -1;
        int currentMaxCapabilitySubId = SystemPropertiesEx.getInt(PROP_MAIN_STACK, 0);
        ProxyControllerEx.getInstance().syncRadioCapability(currentMaxCapabilitySubId);
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (mChipCommon.subCarrierTypeArray[i].isCCard()) {
                cdmaSubId = i;
                cdmaCardNums++;
            }
        }
        if (cdmaCardNums == 1 && currentMaxCapabilitySubId != cdmaSubId) {
            expectedMaxCapSubId = cdmaSubId;
        } else if (cdmaCardNums == 2 && currentMaxCapabilitySubId != ddsSubId) {
            expectedMaxCapSubId = ddsSubId;
        }
        logd("[getExpectedMaxCapabilitySubId] cdmaCardNums=" + cdmaCardNums + " expectedMaxCapSubId=" + expectedMaxCapSubId + " CurrentMaxCapabilitySubId=" + currentMaxCapabilitySubId);
        return expectedMaxCapSubId;
    }

    /* access modifiers changed from: package-private */
    public void judgeNwMode(int lteSlotId) {
        int nwMode4GforCu;
        int nwMode4GforCmcc;
        int nwMode4GforCmcc2;
        int nwMode4GforCt;
        int otherNwModeInCmcc;
        boolean is4GAbilityOn = true;
        if (HwTelephonyManagerInner.getDefault().getLteServiceAbility() != 1) {
            is4GAbilityOn = false;
        }
        logd("judgeNwMode: the LTE slot will be " + lteSlotId + " with the is4GAbilityOn = " + is4GAbilityOn);
        if (is4GAbilityOn) {
            nwMode4GforCu = 9;
            if (HwFullNetworkConfigInner.DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCmcc2 = 17;
            } else {
                nwMode4GforCmcc2 = 20;
            }
            nwMode4GforCt = 10;
            otherNwModeInCmcc = 9;
        } else {
            nwMode4GforCu = 3;
            if (HwFullNetworkConfigInner.DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCmcc = 16;
            } else {
                nwMode4GforCmcc = 18;
            }
            nwMode4GforCt = 7;
            otherNwModeInCmcc = 3;
        }
        logd("judgeNwMode: subCarrierTypeArray[" + lteSlotId + "] = " + mChipCommon.subCarrierTypeArray[lteSlotId]);
        if (HwFullNetworkConfigInner.IS_FULL_NETWORK_SUPPORTED) {
            judgeNwModeForFullNetwork(lteSlotId, nwMode4GforCu, nwMode4GforCmcc2, nwMode4GforCt);
        } else if (HwFullNetworkConfigInner.IS_CMCC_CU_DSDX_ENABLE) {
            judgeNwModeForCmccCu(lteSlotId, nwMode4GforCmcc2, nwMode4GforCu);
            judgePreNwModeSubIdAndListForCU(lteSlotId);
        } else if (HwFullNetworkConfigInner.isCMCCDsdxEnable()) {
            judgeNwModeForCmcc(lteSlotId, nwMode4GforCmcc2, otherNwModeInCmcc);
            judgePreNwModeSubIdAndListForCMCC(lteSlotId);
        } else if (HwFullNetworkConfigInner.IS_CHINA_TELECOM) {
            judgeNwModeForCt(lteSlotId, nwMode4GforCt, nwMode4GforCu);
        } else {
            logd("judgeNwMode: do nothing.");
        }
    }

    private void judgeNwModeForCt(int lteSlotId, int ctDefaultMode, int umtsDefaultMode) {
        logd("judgeNwModeForCt, lteSlotId = " + lteSlotId);
        this.nwModeArray[lteSlotId] = ctDefaultMode;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
        }
        logd("judgeNwModeForCt prefer sub network mode is " + this.nwModeArray[lteSlotId]);
    }

    private void judgeNwModeForCmccCu(int lteSlotId, int tdDefaultMode, int umtsDefaultMode) {
        logd("judgeNwModeForCmccCu, lteSlotId = " + lteSlotId);
        if (mChipCommon.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
            this.nwModeArray[lteSlotId] = tdDefaultMode;
        } else {
            this.nwModeArray[lteSlotId] = umtsDefaultMode;
        }
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
        }
        logd("judgeNwModeForCmccCu prefer sub network mode is " + this.nwModeArray[lteSlotId]);
    }

    private void putPreNwModeListToDbforCmcc() {
        logd("in putPreNwModeListToDbforCmcc");
        ContentResolver resolver = this.mContext.getContentResolver();
        switch (HwFullNetworkConfigInner.DEFAULT_NETWORK_MODE) {
            case 17:
                Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, 17);
                Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, 16);
                Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
                return;
            case 18:
            case 19:
            default:
                Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, -1);
                Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, -1);
                Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, -1);
                return;
            case 20:
                Settings.System.putInt(resolver, NETWORK_MODE_4G_PRE, 20);
                Settings.System.putInt(resolver, NETWORK_MODE_3G_PRE, 18);
                Settings.System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
                return;
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
                putPreNwModeListToDbforCmcc();
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

    public void judgeNwModeForCmcc(int lteSlotId, int nwMode4GforCmcc, int otherNwModeInCmcc) {
        if (mChipCommon.subCarrierTypeArray[lteSlotId].isCUCard() || mChipCommon.subCarrierTypeArray[lteSlotId].isCCard()) {
            this.nwModeArray[lteSlotId] = otherNwModeInCmcc;
        } else {
            this.nwModeArray[lteSlotId] = nwMode4GforCmcc;
        }
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
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
            putPreNwModeListToDbforCmcc();
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
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (i != lteSlotId) {
                setNwModeArray(i, lteSlotId);
            }
        }
    }

    private void setNwModeArray(int slotId, int lteSlotId) {
        if (!HwFullNetworkConfigInner.IS_CARD2_CDMA_SUPPORTED) {
            this.nwModeArray[slotId] = 1;
        } else if (!mChipCommon.subCarrierTypeArray[slotId].isCCard() || mChipCommon.subCarrierTypeArray[lteSlotId].isCCard()) {
            this.nwModeArray[slotId] = 3;
        } else {
            this.nwModeArray[slotId] = 7;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void refreshCardState() {
        for (int index = 0; index < HwFullNetworkConstantsInner.SIM_NUM; index++) {
            boolean isSubActivated = HwTelephonyManager.getDefault().getSubState((long) index) == 1;
            mChipCommon.isSimInsertedArray[index] = mChipCommon.isCardPresent(index) && isSubActivated;
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
    public void setServiceAbility() {
        int slotId = 1;
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        if (hwTelephonyManager != null && this.mNeedSetLteServiceAbility) {
            this.mNeedSetSecondStack = true;
            int mainSlot = hwTelephonyManager.getDefault4GSlotId();
            if (!hwTelephonyManager.isNrSupported()) {
                if (HwFullNetworkConfigInner.IS_4G_SWITCH_SUPPORTED) {
                    slotId = mainSlot;
                }
                int ability = hwTelephonyManager.getServiceAbility(slotId, 0);
                logd("setLteServiceAbility:" + ability);
                hwTelephonyManager.setServiceAbility(slotId, 0, ability);
            } else {
                int ability2 = hwTelephonyManager.getServiceAbility(mainSlot, 1);
                logd("setNrServiceAbility:" + ability2);
                hwTelephonyManager.setServiceAbility(mainSlot, 1, ability2);
            }
            this.mNeedSetLteServiceAbility = false;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isSwitchDualCardSlotsEnabled() {
        boolean z = true;
        UiccControllerExt mUiccController = mChipCommon.getUiccController();
        if (mUiccController == null || mUiccController.getUiccCards() == null || mUiccController.getUiccCards().length < 2) {
            loge("haven't get all UiccCards done, please wait!");
            return false;
        }
        for (UiccCardExt uc : mUiccController.getUiccCards()) {
            if (uc == null) {
                loge("haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        if (!mChipCommon.isSwitchSlotEnabledForCMCC()) {
            logd("isSwitchSlotEnabledForCMCC: CMCC hybird and CMCC is not roaming return false");
            return false;
        } else if (HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE && mChipCommon.isCTHybird()) {
            logd("isSwitchSlotEnabledForCT: CMCC hybird and CMCC is not roaming return false");
            return false;
        } else if ((HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1 && mChipCommon.isCMCCHybird()) || (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2 && mChipCommon.isCTHybird())) {
            logd("MDMCarrierCheck: hybird and MDMCarrier enable return false");
            return false;
        } else if (HwFullNetworkConfigInner.IS_CHINA_TELECOM) {
            return false;
        } else {
            refreshCardState();
            if (!mChipCommon.isSimInsertedArray[0] || !mChipCommon.isSimInsertedArray[1]) {
                z = false;
            }
            return z;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void resetUiccSubscriptionResultFlag(int slotId) {
        if (slotId >= 0 && slotId < HwFullNetworkConstantsInner.SIM_NUM) {
            logd("UiccSubscriptionResult:  slotId=" + slotId + "PreResult:" + this.mSetUiccSubscriptionResult[slotId]);
            this.mSetUiccSubscriptionResult[slotId] = -1;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void registerForIccChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void unregisterForIccChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean getWaitingSwitchBalongSlot() {
        return false;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isSet4GDoneAfterSimInsert() {
        return false;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isSettingDefaultData() {
        return false;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public int getBalongSimSlot() {
        return 0;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public int getSpecCardType(int slotId) {
        return -1;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isRestartRildProgress() {
        return false;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public String getFullIccid(int slotId) {
        return null;
    }

    private void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
