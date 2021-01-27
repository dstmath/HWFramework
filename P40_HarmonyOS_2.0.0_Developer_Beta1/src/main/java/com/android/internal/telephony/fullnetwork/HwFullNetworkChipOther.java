package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
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
import java.util.concurrent.atomic.AtomicInteger;

public class HwFullNetworkChipOther implements HwFullNetworkChipCommon.HwFullNetworkChipInterface {
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwFullNetworkChipOther";
    private static final String MAIN_CARD_INDEX = "main_card_id";
    private static final String NETWORK_MODE_2G_ONLY = "network_mode_2G_only";
    private static final String NETWORK_MODE_3G_PRE = "network_mode_3G_pre";
    private static final String NETWORK_MODE_4G_PRE = "network_mode_4G_pre";
    private static final String PROP_MAIN_STACK = "persist.radio.msim.stackid_0";
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkChipOther mInstance;
    Map<Integer, HwFullNetworkConstantsInner.SubType> currentSubTypeMap = new HashMap();
    int is4GSlotReviewNeeded = 0;
    private Context mContext;
    boolean[] mGetUiccCardsStatusDone = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    boolean mHasSetDdsAfterReboot = false;
    int[] mModemPreferMode = new int[HwFullNetworkConstantsInner.SIM_NUM];
    boolean mNeedExchangeDb = false;
    boolean mNeedSetAllowData = false;
    boolean mNeedSetLteServiceAbility = false;
    int mNumOfGetPrefNwModeSuccess = 0;
    int mPrimaryStackNetworkType = -1;
    int mPrimaryStackPhoneId = -1;
    int mSecondaryStackNetworkType = -1;
    int mSecondaryStackPhoneId = -1;
    int mSetPrimaryStackPrefMode = -1;
    AtomicInteger mSetSecondStackCount = new AtomicInteger(0);
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
        synchronized (LOCK) {
            if (mInstance != null) {
                hwFullNetworkChipOther = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkChipOther.getInstance can't be called before make()");
            }
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
                return getKeyFromMap(this.currentSubTypeMap, targetType);
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

    private int getKeyFromMap(Map<Integer, HwFullNetworkConstantsInner.SubType> map, HwFullNetworkConstantsInner.SubType type) {
        for (Map.Entry<Integer, HwFullNetworkConstantsInner.SubType> mapEntry : map.entrySet()) {
            if (mapEntry.getValue() == type) {
                return mapEntry.getKey().intValue();
            }
        }
        return -1;
    }

    public int judgeDefault4GSlotForSingleSim(int defaultMainSlot) {
        int i = 0;
        while (i < HwFullNetworkConstantsInner.SIM_NUM && !mChipCommon.isSimInsertedArray[i]) {
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

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void refreshCardState() {
        for (int index = 0; index < HwFullNetworkConstantsInner.SIM_NUM; index++) {
            boolean z = false;
            boolean isSubActivated = HwTelephonyManager.getDefault().getSubState((long) index) == 1;
            boolean[] zArr = mChipCommon.isSimInsertedArray;
            if (mChipCommon.isCardPresent(index) && isSubActivated) {
                z = true;
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
    public void setServiceAbility() {
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        if (hwTelephonyManager != null && this.mNeedSetLteServiceAbility) {
            if (!mChipCommon.isRetainDualCardNetworkTypeForNr()) {
                this.mSetSecondStackCount.incrementAndGet();
                logd("setServiceAbility, count is " + this.mSetSecondStackCount.get());
                int mainSlot = hwTelephonyManager.getDefault4GSlotId();
                int slotId = 1;
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
            } else if (DualNrSwitchHelper.getInstance().isDualNrSwitchFeatureEnabled()) {
                DualNrSwitchHelper.getInstance().setNrSwitchAutoForMtkPlant();
            } else {
                logd("dual nr switch opened, don't set network.");
            }
            this.mNeedSetLteServiceAbility = false;
        }
    }

    public boolean isNeedExchangeDualCardNetworkDb(boolean isCmcc) {
        return !mChipCommon.isDualImsSwitchOpened() || (this.mSetSecondStackCount.get() > 0 && (isCmcc || (!mChipCommon.isRetainDualCardNetworkTypeForNr() && HwTelephonyManager.getDefault().isNrSupported())));
    }

    public void decrementCountIfNeccessary() {
        if (this.mSetSecondStackCount.get() > 0) {
            this.mSetSecondStackCount.decrementAndGet();
            logd("decrementCountIfNeccessary, count is " + this.mSetSecondStackCount.get());
            return;
        }
        logd("decrementCountIfNeccessary no change");
    }

    /* access modifiers changed from: package-private */
    public void setPropertyIfNecessary() {
        if (HwFullNetworkConfigInner.isSupportFastSetNetworkMode()) {
            if (mChipCommon.isVsimWorking()) {
                logd("setPropertyIfNecessary, vsim is working, return");
            } else if (!mChipCommon.isRetainDualCardNetworkTypeForNr() || !DualNrSwitchHelper.getInstance().isDualNrSwitchFeatureEnabled()) {
                int networkTypeForSlot0 = getNetworkTypeFromDB(0);
                int networkTypeForSlot1 = getNetworkTypeFromDB(1);
                SystemPropertiesEx.set("persist.radio.networkmode0", String.valueOf(networkTypeForSlot1));
                SystemPropertiesEx.set("persist.radio.networkmode1", String.valueOf(networkTypeForSlot0));
                logd("setPropertyIfNecessary, from(" + networkTypeForSlot0 + "," + networkTypeForSlot1 + ") to(" + networkTypeForSlot1 + "," + networkTypeForSlot0 + ")");
            } else {
                DualNrSwitchHelper.getInstance().setPropertyFromSwitchRecord();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void recoverPropertyFromDb() {
        if (HwFullNetworkConfigInner.isSupportFastSetNetworkMode()) {
            int previousNetworkTypeSub0 = getNetworkTypeFromDB(0);
            int previousNetworkTypeSub1 = getNetworkTypeFromDB(1);
            SystemPropertiesEx.set("persist.radio.networkmode0", String.valueOf(previousNetworkTypeSub0));
            SystemPropertiesEx.set("persist.radio.networkmode1", String.valueOf(previousNetworkTypeSub1));
            logd("recoverPropertyFromDb, to (" + previousNetworkTypeSub0 + "," + previousNetworkTypeSub1 + ")");
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isSwitchDualCardSlotsEnabled() {
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
        } else if (mChipCommon.isSet4GSlotInProgress || HwFullNetworkConfigInner.IS_CHINA_TELECOM) {
            return false;
        } else {
            refreshCardState();
            if (!mChipCommon.isSimInsertedArray[0] || !mChipCommon.isSimInsertedArray[1]) {
                return false;
            }
            return true;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void resetUiccSubscriptionResultFlag(int slotId) {
        if (slotId >= 0 && slotId < HwFullNetworkConstantsInner.SIM_NUM) {
            logd("UiccSubscriptionResult: slotId=" + slotId + "PreResult:" + this.mSetUiccSubscriptionResult[slotId]);
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
        RlogEx.i(LOG_TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
