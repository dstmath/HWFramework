package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwIccIdUtil;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import java.util.List;

public class HwFullNetworkChipCommon {
    static final String KEY_LAST_MAIN_CARD_ICCID = "last_main_card_iccid";
    static final String KEY_SWITCH_DUAL_CARD_SLOT = "switch_dual_card_slots";
    private static final String LOG_TAG = "HwFullNetworkChipCommon";
    public static final String PREFIX_LOCAL_MCC = "460";
    public static final String ROAMINGSTATE_PREF = "lastroamingstate";
    private static HwFullNetworkChipInterface mChipInterface;
    private static HwHotplugController mHotPlugController;
    private static HwFullNetworkChipCommon mInstance;
    private static final Object mLock = new Object();
    int current4GSlotBackup = 0;
    int default4GSlot = 0;
    int expectedDDSsubId = -1;
    boolean isSet4GSlotInProgress = false;
    boolean isSet4GSlotInSwitchProgress = false;
    boolean isSet4GSlotManuallyTriggered = false;
    boolean[] isSimInsertedArray = new boolean[HwFullNetworkConstants.SIM_NUM];
    boolean isVoiceCallEndedRegistered = false;
    private CommandsInterface[] mCi;
    public int mCmccSubIdOldState = 1;
    Context mContext;
    String[] mIccIds = new String[HwFullNetworkConstants.SIM_NUM];
    Message mSet4GSlotCompleteMsg = null;
    UiccController mUiccController = null;
    boolean needRetrySetPrefNetwork = false;
    int prefer4GSlot = 0;
    HwFullNetworkConstants.SubCarrierType[] subCarrierTypeArray = new HwFullNetworkConstants.SubCarrierType[HwFullNetworkConstants.SIM_NUM];

    public interface HwFullNetworkChipInterface {
        int getBalongSimSlot();

        String getFullIccid(int i);

        int getSpecCardType(int i);

        boolean getWaitingSwitchBalongSlot();

        boolean isRestartRildProgress();

        boolean isSet4GDoneAfterSimInsert();

        boolean isSettingDefaultData();

        boolean isSwitchDualCardSlotsEnabled();

        void refreshCardState();

        void registerForIccChanged(Handler handler, int i, Object obj);

        void resetUiccSubscriptionResultFlag(int i);

        void setWaitingSwitchBalongSlot(boolean z);

        void unregisterForIccChanged(Handler handler);
    }

    private HwFullNetworkChipCommon(Context context, CommandsInterface[] ci) {
        logd("HwFullNetworkChipCommon constructor");
        this.mCi = ci;
        this.mContext = context;
        this.mUiccController = UiccController.getInstance();
    }

    static HwFullNetworkChipCommon make(Context context, CommandsInterface[] ci) {
        HwFullNetworkChipCommon hwFullNetworkChipCommon;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwFullNetworkChipCommon(context, ci);
                if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
                    mHotPlugController = HwHotplugController.make(context, ci);
                }
                hwFullNetworkChipCommon = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkChipCommon.make() should only be called once");
            }
        }
        return hwFullNetworkChipCommon;
    }

    static HwFullNetworkChipCommon getInstance() {
        HwFullNetworkChipCommon hwFullNetworkChipCommon;
        synchronized (mLock) {
            if (mInstance != null) {
                hwFullNetworkChipCommon = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkChipCommon.getInstance can't be called before make()");
            }
        }
        return hwFullNetworkChipCommon;
    }

    /* access modifiers changed from: package-private */
    public void setChipInterface(HwFullNetworkChipInterface chipInterface) {
        mChipInterface = chipInterface;
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        return mChipInterface.isSwitchDualCardSlotsEnabled();
    }

    public boolean isSwitchSlotEnabledForCMCC() {
        if (!HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE || !isCMCCHybird()) {
            return true;
        }
        int cmccSlotId = getCMCCCardSlotId();
        if (TelephonyManager.getDefault().isNetworkRoaming(cmccSlotId)) {
            return true;
        }
        int otherSlotId = cmccSlotId == 0 ? 1 : 0;
        ServiceState cmccState = TelephonyManager.getDefault().getServiceStateForSubscriber(cmccSlotId);
        ServiceState otherState = TelephonyManager.getDefault().getServiceStateForSubscriber(otherSlotId);
        if (!(otherState == null || cmccState == null)) {
            boolean isCmccInService = cmccState.getState() == 0;
            boolean isOtherInService = otherState.getState() == 0;
            if (!isCmccInService && isOtherInService) {
                String regPlmn = otherState.getOperatorNumeric();
                if (TextUtils.isEmpty(regPlmn) || regPlmn.startsWith(PREFIX_LOCAL_MCC)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public int getCMCCCardSlotId() {
        if (isCMCCHybird()) {
            for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
                if (isCMCCCardBySlotId(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void saveLastRoamingStateToSP(boolean roamingState) {
        logd("saveRoamingState " + roamingState);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putBoolean(ROAMINGSTATE_PREF, roamingState);
        editor.apply();
    }

    public boolean getLastRoamingStateFromSP() {
        return PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(ROAMINGSTATE_PREF, false);
    }

    public boolean needForceSetDefaultSlot(boolean roaming, int cmccSlotId) {
        return !roaming && getUserSwitchDualCardSlots() != cmccSlotId;
    }

    public boolean isCmccHybirdBySubCarrierType() {
        return this.subCarrierTypeArray[0].isCMCCCard() != this.subCarrierTypeArray[1].isCMCCCard();
    }

    public void refreshCardState() {
        mChipInterface.refreshCardState();
    }

    public void initUiccCard(UiccCard uiccCard, IccCardStatus status, Integer index) {
        if (mHotPlugController != null) {
            mHotPlugController.initHotPlugCardState(uiccCard, status, index);
        }
    }

    public void updateUiccCard(UiccCard uiccCard, IccCardStatus status, Integer index) {
        if (mHotPlugController != null) {
            mHotPlugController.updateHotPlugCardState(uiccCard, status, index);
        }
    }

    public void setUserSwitchDualCardSlots(int subscription) {
        logd("setUserSwitchDualCardSlots: " + subscription);
        this.prefer4GSlot = subscription;
        Settings.System.putInt(this.mContext.getContentResolver(), KEY_SWITCH_DUAL_CARD_SLOT, subscription);
        if (mHotPlugController != null) {
            UiccCard uc = getUiccController().getUiccCard(subscription);
            if (uc != null) {
                mHotPlugController.updateHotPlugMainSlotIccId(uc.getIccId());
            }
        }
    }

    public void saveMainCardIccId(String iccId) {
        if (iccId == null || "".equals(iccId)) {
            logd("invalid iccId");
            return;
        }
        try {
            iccId = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, iccId);
        } catch (Exception ex) {
            logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
        }
        Settings.System.putString(this.mContext.getContentResolver(), KEY_LAST_MAIN_CARD_ICCID, iccId);
    }

    public int getDefaultMainSlotByIccId(int temSub) {
        String iccIdSub1 = getIccId(0);
        String iccIdSub2 = getIccId(1);
        if (iccIdSub1 == null || iccIdSub2 == null) {
            logd("iccid is null");
            return temSub;
        }
        int mainslot = temSub;
        String oldIccId = getLastMainCardIccId();
        if (iccIdSub1.equals(oldIccId)) {
            logd("temsub is sub1");
            mainslot = 0;
        } else if (iccIdSub2.equals(oldIccId)) {
            logd("temsub is sub2");
            mainslot = 1;
        }
        return mainslot;
    }

    private String getLastMainCardIccId() {
        String lastIccid = Settings.System.getString(this.mContext.getContentResolver(), KEY_LAST_MAIN_CARD_ICCID);
        if (TextUtils.isEmpty(lastIccid)) {
            return lastIccid;
        }
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkConstants.MASTER_PASSWORD, lastIccid);
        } catch (Exception ex) {
            logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
            return lastIccid;
        }
    }

    public String getFullIccid(int subId) {
        return mChipInterface.getFullIccid(subId);
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        mChipInterface.registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        mChipInterface.unregisterForIccChanged(h);
    }

    public boolean getWaitingSwitchBalongSlot() {
        return mChipInterface.getWaitingSwitchBalongSlot();
    }

    public boolean isSet4GDoneAfterSimInsert() {
        return mChipInterface.isSet4GDoneAfterSimInsert();
    }

    public boolean isSettingDefaultData() {
        return mChipInterface.isSettingDefaultData();
    }

    public int getBalongSimSlot() {
        return mChipInterface.getBalongSimSlot();
    }

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        mChipInterface.setWaitingSwitchBalongSlot(iSetResult);
    }

    public int getSpecCardType(int slotId) {
        return mChipInterface.getSpecCardType(slotId);
    }

    public void resetUiccSubscriptionResultFlag(int slotId) {
        mChipInterface.resetUiccSubscriptionResultFlag(slotId);
    }

    public boolean isRestartRildProgress() {
        return mChipInterface.isRestartRildProgress();
    }

    public int getUserSwitchDualCardSlots() {
        try {
            return Settings.System.getInt(this.mContext.getContentResolver(), KEY_SWITCH_DUAL_CARD_SLOT);
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            return 0;
        }
    }

    public UiccController getUiccController() {
        return this.mUiccController;
    }

    public Integer getCiIndex(Message msg) {
        if (msg == null) {
            return 0;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return 0;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return 0;
        }
        return (Integer) ar.userObj;
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < HwFullNetworkConstants.SIM_NUM;
    }

    public void sendResponseToTarget(Message response, int responseCode) {
        if (response != null && response.getTarget() != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            try {
                response.sendToTarget();
            } catch (IllegalStateException e) {
                loge("response is sent, don't send again!!");
            }
        }
    }

    public boolean isCardPresent(int slotId) {
        boolean z = false;
        if (this.mUiccController == null || this.mUiccController.getUiccCard(slotId) == null) {
            return false;
        }
        if (this.mUiccController.getUiccCard(slotId).getCardState() != IccCardStatus.CardState.CARDSTATE_ABSENT) {
            z = true;
        }
        return z;
    }

    public boolean isCMCCCard(String inn) {
        return HwIccIdUtil.isCMCC(inn);
    }

    public boolean isCUCard(String inn) {
        return HwIccIdUtil.isCU(inn);
    }

    public boolean isCTCard(String inn) {
        return HwIccIdUtil.isCT(inn);
    }

    public boolean isAISCard(String inn) {
        return HwIccIdUtil.isAIS(inn);
    }

    public boolean isCMCCCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isCMCCByMccMnc(mccMnc);
    }

    public boolean isCUCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isCUByMccMnc(mccMnc);
    }

    public boolean isAISCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isAISByMccMnc(mccMnc);
    }

    public boolean isCMCCCardBySlotId(int slotId) {
        if (!isValidIndex(slotId)) {
            logd("isCMCCCardBySlotId: Invalid slotId: " + slotId);
            return false;
        } else if (this.subCarrierTypeArray[slotId] != null && this.subCarrierTypeArray[slotId] != HwFullNetworkConstants.SubCarrierType.ABSENT) {
            return this.subCarrierTypeArray[slotId].isCMCCCard();
        } else {
            if (!TextUtils.isEmpty(this.mIccIds[slotId])) {
                return isCMCCCard(this.mIccIds[slotId]);
            }
            return false;
        }
    }

    public boolean isCMCCHybird() {
        boolean hasOther = false;
        boolean hasCMCC = false;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (isCMCCCardBySlotId(i)) {
                hasCMCC = true;
            } else {
                hasOther = true;
            }
        }
        if (!hasCMCC || !hasOther) {
            return false;
        }
        return true;
    }

    public boolean isCTCardBySlotId(int slotId) {
        if (!isValidIndex(slotId)) {
            logd("isCMCCCardBySlotId: Invalid slotId: " + slotId);
            return false;
        } else if (this.subCarrierTypeArray[slotId] != null) {
            return this.subCarrierTypeArray[slotId].isCTCard();
        } else {
            if (!TextUtils.isEmpty(this.mIccIds[slotId])) {
                return isCTCard(this.mIccIds[slotId]);
            }
            return false;
        }
    }

    public boolean isCTHybird() {
        boolean hasOther = false;
        boolean hasCTCard = false;
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            if (isCTCardBySlotId(i)) {
                hasCTCard = true;
            } else {
                hasOther = true;
            }
        }
        logd("isCTHybird : hasCTCard = " + hasCTCard + " ; hasOther = " + hasOther);
        if (!hasCTCard || !hasOther) {
            return false;
        }
        return true;
    }

    public boolean isCDMASimCard(int slotId) {
        HwTelephonyManagerInner hwTelephonyManager = HwTelephonyManagerInner.getDefault();
        return hwTelephonyManager != null && hwTelephonyManager.isCDMASimCard(slotId);
    }

    public boolean isUserPref4GSlot(int slotId) {
        return this.prefer4GSlot == slotId;
    }

    public void judgeSubCarrierType() {
        logd("judgeSubCarrierType: judge the sub Type for each slot");
        for (int sub = 0; sub < HwFullNetworkConstants.SIM_NUM; sub++) {
            logd("judgeSubCarrierType: isSimInsertedArray[" + sub + "] = " + this.isSimInsertedArray[sub]);
            if (true == this.isSimInsertedArray[sub]) {
                String iccId = getIccId(sub);
                if (TextUtils.isEmpty(iccId) || 7 > iccId.length()) {
                    loge("judgeSubCarrierType: iccId is invalid, set the sub carrier type as OTHER");
                    this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.OTHER;
                } else {
                    String inn = iccId.substring(0, 7);
                    int appType = getCardAppType(sub);
                    logd("judgeSubCarrierType: iccId is " + inn + " and app type is " + appType + " for sub " + sub);
                    if (isCMCCCard(inn)) {
                        if (2 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CMCC_USIM;
                        } else if (1 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CMCC_SIM;
                        } else if (4 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_CSIM;
                        } else if (3 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.OTHER;
                        }
                    } else if (isCUCard(inn)) {
                        if (2 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CU_USIM;
                        } else if (1 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CU_SIM;
                        } else if (4 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_CSIM;
                        } else if (3 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.OTHER;
                        }
                    } else if (isCTCard(inn)) {
                        if (4 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_CSIM;
                        } else if (3 == appType) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_RUIM;
                        } else if (43 == HwTelephonyManagerInner.getDefault().getCardType(sub)) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_CSIM;
                        } else if (41 == HwTelephonyManagerInner.getDefault().getCardType(sub) || 30 == HwTelephonyManagerInner.getDefault().getCardType(sub)) {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.OTHER;
                        }
                    } else if (HwFullNetworkConfig.IS_AIS_4G_DSDX_ENABLE && isAISCard(inn)) {
                        this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_FOREIGN_AIS_USIM;
                    } else if (2 == appType) {
                        this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_FOREIGN_USIM;
                    } else if (1 == appType) {
                        this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_FOREIGN_SIM;
                    } else if (4 == appType) {
                        this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_FOREIGN_CSIM;
                    } else if (3 == appType) {
                        this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.CARRIER_FOREIGN_RUIM;
                    } else {
                        this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.OTHER;
                    }
                }
            } else {
                logd("judgeSubCarrierType: sub " + sub + " is absent, set to ABSENT.");
                this.subCarrierTypeArray[sub] = HwFullNetworkConstants.SubCarrierType.ABSENT;
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getIccId(int slot) {
        String iccId = mChipInterface.getFullIccid(slot);
        if (!TextUtils.isEmpty(iccId)) {
            return iccId;
        }
        String iccId2 = this.mUiccController.getUiccCard(slot) == null ? null : this.mUiccController.getUiccCard(slot).getIccId();
        if (!TextUtils.isEmpty(iccId2)) {
            return iccId2;
        }
        List<SubscriptionInfo> subInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexPrivileged(slot, false);
        if (subInfo != null) {
            iccId2 = subInfo.get(0).getIccId();
        }
        return iccId2;
    }

    public boolean judgeSubCarrierTypeByMccMnc(int slotId) {
        logd("judgeSubCarrierTypeByMccMnc for slot: " + slotId);
        if (this.isSimInsertedArray[slotId]) {
            if (this.subCarrierTypeArray[slotId] == null || !this.subCarrierTypeArray[slotId].isCCard()) {
                int appType = getCardAppType(slotId);
                String mccMnc = getMccMnc(slotId, appType);
                if (mccMnc == null) {
                    loge("judgeSubCarrierTypeByMccMnc: mccMnc is invalid, return!");
                    return false;
                }
                logd("judgeSubCarrierTypeByMccMnc: current  is : " + this.subCarrierTypeArray[slotId]);
                if (isCMCCCardByMccMnc(mccMnc)) {
                    if (2 == appType) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstants.SubCarrierType.CARRIER_CMCC_USIM;
                    } else if (1 == appType) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstants.SubCarrierType.CARRIER_CMCC_SIM;
                    }
                } else if (isCUCardByMccMnc(mccMnc)) {
                    if (2 == appType) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstants.SubCarrierType.CARRIER_CU_USIM;
                    } else if (1 == appType) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstants.SubCarrierType.CARRIER_CU_SIM;
                    }
                } else if (HwFullNetworkConfig.IS_AIS_4G_DSDX_ENABLE && isAISCardByMccMnc(mccMnc)) {
                    this.subCarrierTypeArray[slotId] = HwFullNetworkConstants.SubCarrierType.CARRIER_FOREIGN_AIS_USIM;
                } else if (this.subCarrierTypeArray[slotId] == null) {
                    this.subCarrierTypeArray[slotId] = HwFullNetworkConstants.SubCarrierType.OTHER;
                }
                logd("judgeSubCarrierTypeByMccMnc: after is : " + this.subCarrierTypeArray[slotId]);
            } else {
                logd("C Card is no need to judgeSubCarrierTypeByMccMnc!");
                return true;
            }
        }
        return true;
    }

    private String getMccMnc(int slotId, int appType) {
        if (this.mUiccController == null) {
            return null;
        }
        String mccMnc = null;
        UiccCard uiccCard = this.mUiccController.getUiccCard(slotId);
        UiccCardApplication app = null;
        if (uiccCard != null) {
            if (2 == appType || 1 == appType) {
                app = uiccCard.getApplication(1);
            } else if (4 == appType || 3 == appType) {
                app = uiccCard.getApplication(2);
            } else {
                loge("unknown appType, return!");
                return null;
            }
        }
        if (app != null) {
            IccRecords records = app.getIccRecords();
            if (records != null) {
                String imsi = records.getIMSI();
                if (imsi == null || 5 >= imsi.length()) {
                    logd("invalid imsi!");
                } else {
                    mccMnc = imsi.substring(0, 5);
                    logd("slot " + slotId + " mccMnc = " + mccMnc);
                }
            }
        } else {
            loge("app is null, return");
        }
        return mccMnc;
    }

    private int getCardAppType(int slotId) {
        int appType;
        if (this.mUiccController == null) {
            return 0;
        }
        UiccCard uiccCard = this.mUiccController.getUiccCard(slotId);
        if (uiccCard == null) {
            logd("getCardAppType: uiccCard is null for slot " + slotId);
            return 0;
        }
        if (uiccCard.getApplicationByType(4) != null) {
            appType = 4;
        } else if (uiccCard.getApplicationByType(3) != null) {
            appType = 3;
        } else if (uiccCard.getApplicationByType(2) != null) {
            appType = 2;
        } else if (uiccCard.getApplicationByType(1) != null) {
            appType = 1;
        } else {
            appType = 0;
        }
        logd("getCardAppType: the app type for slot " + slotId + " is " + appType);
        return appType;
    }

    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        if (this.isSet4GSlotInProgress) {
            loge("setPreferredNetworkType: In Progress:");
            sendResponseToTarget(response, 2);
        } else if (HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
            logd("CROSS_MAPPING by QCRIL,setPreferredNetworkType directly");
            this.mCi[phoneId].setPreferredNetworkType(networkType, response);
        }
    }

    public boolean isDualImsSwitchOpened() {
        return 1 == SystemProperties.getInt("persist.radio.dualltecap", 0);
    }

    public boolean isCmccHybirdAndNoRoaming() {
        boolean isCmccHybird = HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && isCmccHybirdBySubCarrierType();
        boolean isRoaming = TelephonyManager.getDefault().isNetworkRoaming(getCMCCCardSlotId());
        if (!isCmccHybird || isRoaming) {
            return false;
        }
        return true;
    }

    public int getCombinedRegState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int regState = serviceState.getVoiceRegState();
        int dataRegState = serviceState.getDataRegState();
        if (regState == 1 && dataRegState == 0) {
            regState = dataRegState;
        }
        return regState;
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
