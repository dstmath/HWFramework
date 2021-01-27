package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwTelephonyManagerInnerUtils;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwHotplugControllerImpl;
import com.android.internal.telephony.HwIccIdUtil;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import huawei.android.os.HwProtectAreaManager;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HwFullNetworkChipCommon {
    private static final int DATE_STRING_LENGHT = 8;
    private static final int END_INDEX = 10;
    private static final int ERROR_RET_LEN = 1;
    private static final int HEX_TYPE = 16;
    private static final String KEY_LAST_MAIN_CARD_ICCID = "last_main_card_iccid";
    private static final String KEY_SWITCH_DUAL_CARD_SLOT = "switch_dual_card_slots";
    private static final String KEY_UNBIND_FILG = "UNBIND_FLAG";
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwFullNetworkChipCommon";
    private static final int MAX_NUMBER = 10;
    private static final int MONTH_END_INDEX = 3;
    private static final int OPERATE_OEMINFO_SUCCESS = 0;
    private static final String PREFIX_LOCAL_MCC = "460";
    private static final int READ_BUF_INDEX = 0;
    private static final String READ_BUF_INIT = "AA";
    private static final int READ_LEN = 10;
    public static final String ROAMINGSTATE_PREF = "lastroamingstate";
    private static final int START_INDEX = 5;
    private static final int YEAR_END_INDEX = 2;
    private static final int YEAR_START_INDEX = 0;
    private static HwFullNetworkChipInterface mChipInterface;
    private static HwFullNetworkChipCommon mInstance;
    private static HwHotplugControllerImpl sHotPlugController;
    int current4GSlotBackup = 0;
    int default4GSlot = 0;
    int expectedDDSsubId = -1;
    boolean isSet4GSlotInProgress = false;
    boolean isSet4GSlotInSwitchProgress = false;
    private boolean[] isSimContactLoadeds = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    boolean[] isSimInsertedArray = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    boolean isVoiceCallEndedRegistered = false;
    private CommandsInterfaceEx[] mCi;
    public int mCmccSubIdOldState = 1;
    protected Context mContext;
    protected String[] mIccIds = new String[HwFullNetworkConstantsInner.SIM_NUM];
    protected Message mSet4GSlotCompleteMsg = null;
    int[] mSubscriptionStatus = new int[HwFullNetworkConstantsInner.SIM_NUM];
    protected UiccControllerExt mUiccController = null;
    boolean needRetrySetPrefNetwork = false;
    int prefer4GSlot = 0;
    protected HwFullNetworkConstantsInner.SubCarrierType[] subCarrierTypeArray = new HwFullNetworkConstantsInner.SubCarrierType[HwFullNetworkConstantsInner.SIM_NUM];

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

    private HwFullNetworkChipCommon(Context context, CommandsInterfaceEx[] ci) {
        logd("HwFullNetworkChipCommon constructor");
        this.mCi = ci;
        this.mContext = context;
        this.mUiccController = UiccControllerExt.getInstance();
        this.prefer4GSlot = getUserSwitchDualCardSlots();
    }

    static HwFullNetworkChipCommon make(Context context, CommandsInterfaceEx[] ci) {
        HwFullNetworkChipCommon hwFullNetworkChipCommon;
        synchronized (LOCK) {
            if (mInstance == null) {
                mInstance = new HwFullNetworkChipCommon(context, ci);
                if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
                    sHotPlugController = HwHotplugControllerImpl.make(context, ci);
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
        synchronized (LOCK) {
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
        if (!HwFullNetworkConfigInner.isCMCCDsdxDisable() || !isCMCCHybird()) {
            return true;
        }
        int cmccSlotId = getCMCCCardSlotId();
        int cmccSubId = SubscriptionManagerEx.getSubIdUsingSlotId(cmccSlotId);
        if (TelephonyManagerEx.isNetworkRoaming(cmccSubId)) {
            return true;
        }
        int otherSubId = SubscriptionManagerEx.getSubIdUsingSlotId(cmccSlotId == 0 ? 1 : 0);
        ServiceState cmccState = TelephonyManagerEx.getServiceStateForSubscriber(cmccSubId);
        ServiceState otherState = TelephonyManagerEx.getServiceStateForSubscriber(otherSubId);
        if (otherState == null || cmccState == null) {
            return false;
        }
        boolean isCmccInService = cmccState.getState() == 0;
        boolean isOtherInService = otherState.getState() == 0;
        if (!isCmccInService && isOtherInService) {
            String regPlmn = otherState.getOperatorNumeric();
            if (!TextUtils.isEmpty(regPlmn) && !regPlmn.startsWith(PREFIX_LOCAL_MCC)) {
                return true;
            }
        }
        return false;
    }

    public int getCMCCCardSlotId() {
        if (!isCMCCHybird()) {
            return -1;
        }
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (isCMCCCardBySlotId(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getCTCardSlotId() {
        if (!isCTHybird()) {
            return -1;
        }
        for (int simSlotId = 0; simSlotId < HwFullNetworkConstantsInner.SIM_NUM; simSlotId++) {
            if (isCTCardBySlotId(simSlotId)) {
                return simSlotId;
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

    public void initUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
        HwHotplugControllerImpl hwHotplugControllerImpl = sHotPlugController;
        if (hwHotplugControllerImpl != null) {
            hwHotplugControllerImpl.initHotPlugCardState(status, index);
        }
    }

    public void updateUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
        HwHotplugControllerImpl hwHotplugControllerImpl = sHotPlugController;
        if (hwHotplugControllerImpl != null) {
            hwHotplugControllerImpl.updateHotPlugCardState(status, index);
        }
    }

    public void saveMainCardIccId(String iccId) {
        if (iccId == null || BuildConfig.FLAVOR.equals(iccId)) {
            logd("invalid iccId");
            return;
        }
        try {
            iccId = HwAESCryptoUtil.encrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, iccId);
        } catch (IllegalArgumentException e) {
            logd("HwAESCryptoUtil decrypt IllegalArgumentException.");
        } catch (Exception e2) {
            logd("HwAESCryptoUtil decrypt excepiton.");
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
        String oldIccId = getLastMainCardIccId();
        if (iccIdSub1.equals(oldIccId)) {
            logd("temsub is sub1");
            return 0;
        } else if (!iccIdSub2.equals(oldIccId)) {
            return temSub;
        } else {
            logd("temsub is sub2");
            return 1;
        }
    }

    private String getLastMainCardIccId() {
        String lastIccid = Settings.System.getString(this.mContext.getContentResolver(), KEY_LAST_MAIN_CARD_ICCID);
        if (TextUtils.isEmpty(lastIccid)) {
            return lastIccid;
        }
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, lastIccid);
        } catch (IllegalArgumentException e) {
            logd("HwAESCryptoUtil decrypt IllegalArgumentException iccid.");
            return lastIccid;
        } catch (Exception e2) {
            logd("HwAESCryptoUtil decrypt excepiton.");
            return lastIccid;
        }
    }

    public String getFullIccid(int slotId) {
        return mChipInterface.getFullIccid(slotId);
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

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        mChipInterface.setWaitingSwitchBalongSlot(iSetResult);
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
        int subscription = 0;
        try {
            subscription = Settings.System.getInt(this.mContext.getContentResolver(), KEY_SWITCH_DUAL_CARD_SLOT);
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
        }
        if (!isValidIndex(subscription)) {
            return 0;
        }
        return subscription;
    }

    public void setUserSwitchDualCardSlots(int subscription) {
        UiccCardExt uc;
        logd("setUserSwitchDualCardSlots: " + subscription);
        this.prefer4GSlot = subscription;
        Settings.System.putInt(this.mContext.getContentResolver(), KEY_SWITCH_DUAL_CARD_SLOT, subscription);
        if (sHotPlugController != null && (uc = getUiccController().getUiccCard(subscription)) != null) {
            sHotPlugController.updateHotPlugMainSlotIccId(uc.getIccId());
        }
    }

    public UiccControllerExt getUiccController() {
        return this.mUiccController;
    }

    public Integer getCiIndex(Message msg) {
        if (msg == null) {
            return 0;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (AsyncResultEx.from(msg.obj) != null) {
            AsyncResultEx arEx = AsyncResultEx.from(msg.obj);
            if (arEx.getUserObj() == null || !(arEx.getUserObj() instanceof Integer)) {
                return 0;
            }
            return (Integer) arEx.getUserObj();
        }
        RlogEx.i(LOG_TAG, "invalid index, use default");
        return 0;
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < HwFullNetworkConstantsInner.SIM_NUM;
    }

    public void sendResponseToTarget(Message response, int responseCode) {
        if (response != null && response.getTarget() != null) {
            AsyncResultEx.forMessage(response, (Object) null, CommandExceptionEx.fromRilErrno(responseCode));
            try {
                response.sendToTarget();
            } catch (IllegalStateException e) {
                loge("response is sent, don't send again!!");
            }
        }
    }

    public boolean isCardPresent(int slotId) {
        UiccControllerExt uiccControllerExt = this.mUiccController;
        if (uiccControllerExt == null || uiccControllerExt.getUiccCard(slotId) == null || this.mUiccController.getUiccCard(slotId).getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
            return false;
        }
        return true;
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

    public boolean isCMCCCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isCMCCByMccMnc(mccMnc);
    }

    public boolean isCUCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isCUByMccMnc(mccMnc);
    }

    public boolean isAISCard(String inn) {
        return HwIccIdUtil.isAIS(inn);
    }

    public boolean isSmartCard(String inn) {
        return HwIccIdUtil.isSmart(inn);
    }

    public boolean isMtnCard(String inn) {
        return HwIccIdUtil.isCustomByIccid(inn, "mtn");
    }

    public boolean isAISCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isAISByMccMnc(mccMnc);
    }

    public boolean isSmartCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isSmartByMccMnc(mccMnc);
    }

    public boolean isMtnCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isCustomByMccMnc(mccMnc, "mtn");
    }

    public boolean isCMCCCardBySlotId(int slotId) {
        if (!isValidIndex(slotId)) {
            logd("isCMCCCardBySlotId: Invalid slotId: " + slotId);
            return false;
        }
        HwFullNetworkConstantsInner.SubCarrierType[] subCarrierTypeArr = this.subCarrierTypeArray;
        if (subCarrierTypeArr[slotId] != null && subCarrierTypeArr[slotId] != HwFullNetworkConstantsInner.SubCarrierType.ABSENT) {
            return this.subCarrierTypeArray[slotId].isCMCCCard();
        }
        if (!TextUtils.isEmpty(this.mIccIds[slotId])) {
            return isCMCCCard(this.mIccIds[slotId]);
        }
        return false;
    }

    public boolean isCMCCHybird() {
        boolean hasCMCC = false;
        boolean hasOther = false;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (isCMCCCardBySlotId(i)) {
                hasCMCC = true;
            } else {
                hasOther = true;
            }
        }
        return hasCMCC && hasOther;
    }

    public boolean isCTCardBySlotId(int slotId) {
        if (!isValidIndex(slotId)) {
            logd("isCMCCCardBySlotId: Invalid slotId: " + slotId);
            return false;
        }
        HwFullNetworkConstantsInner.SubCarrierType[] subCarrierTypeArr = this.subCarrierTypeArray;
        if (subCarrierTypeArr[slotId] != null) {
            return subCarrierTypeArr[slotId].isCTCard();
        }
        if (!TextUtils.isEmpty(this.mIccIds[slotId])) {
            return isCTCard(this.mIccIds[slotId]);
        }
        return false;
    }

    public boolean isCTHybird() {
        boolean hasCTCard = false;
        boolean hasOther = false;
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (isCTCardBySlotId(i)) {
                hasCTCard = true;
            } else {
                hasOther = true;
            }
        }
        logd("isCTHybird : hasCTCard = " + hasCTCard + " ; hasOther = " + hasOther);
        return hasCTCard && hasOther;
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
        for (int sub = 0; sub < HwFullNetworkConstantsInner.SIM_NUM; sub++) {
            logd("judgeSubCarrierType: isSimInsertedArray[" + sub + "] = " + this.isSimInsertedArray[sub]);
            if (this.isSimInsertedArray[sub]) {
                String iccId = getIccId(sub);
                if (TextUtils.isEmpty(iccId) || iccId.length() < 7) {
                    loge("judgeSubCarrierType: iccId is invalid, set the sub carrier type as OTHER");
                    this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
                } else {
                    handleIccIdValid(sub, iccId);
                }
            } else {
                logd("judgeSubCarrierType: sub " + sub + " is absent, set to ABSENT.");
                this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.ABSENT;
            }
        }
    }

    private void handleIccIdValid(int sub, String iccId) {
        String inn = iccId.substring(0, 7);
        int appType = getCardAppType(sub);
        logd("judgeSubCarrierType: iccId is " + inn + " and app type is " + appType + " for sub " + sub);
        if (isCMCCCard(inn)) {
            setSubCarrierTypeArrayForCMCC(sub, appType);
        } else if (isCUCard(inn)) {
            setSubCarrierTypeArrayForCU(sub, appType);
        } else if (isCTCard(inn)) {
            setSubCarrierTypeArrayForCT(sub, appType);
        } else if (isAISCard(inn)) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_AIS_USIM;
        } else if (isSmartCard(inn)) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_SMART_USIM;
        } else if (isMtnCard(inn)) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_MTN_USIM;
        } else {
            setSubCarrierTypeArrayForOther(sub, appType);
        }
    }

    private void setSubCarrierTypeArrayForCMCC(int sub, int appType) {
        if (appType == 2) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CMCC_USIM;
        } else if (appType == 1) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CMCC_SIM;
        } else if (appType == 4) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_CSIM;
        } else if (appType == 3) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_RUIM;
        } else {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
        }
    }

    private void setSubCarrierTypeArrayForCU(int sub, int appType) {
        if (appType == 2) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CU_USIM;
        } else if (appType == 1) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CU_SIM;
        } else if (appType == 4) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_CSIM;
        } else if (appType == 3) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_RUIM;
        } else {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
        }
    }

    private void setSubCarrierTypeArrayForCT(int sub, int appType) {
        if (appType == 4) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_CSIM;
        } else if (appType == 3) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_RUIM;
        } else if (HwTelephonyManagerInner.getDefault().getCardType(sub) == 43) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_CSIM;
        } else if (HwTelephonyManagerInner.getDefault().getCardType(sub) == 41 || HwTelephonyManagerInner.getDefault().getCardType(sub) == 30) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CT_RUIM;
        } else {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
        }
    }

    private void setSubCarrierTypeArrayForOther(int sub, int appType) {
        if (appType == 2) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_USIM;
        } else if (appType == 1) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_SIM;
        } else if (appType == 4) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_CSIM;
        } else if (appType == 3) {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_RUIM;
        } else {
            this.subCarrierTypeArray[sub] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
        }
    }

    /* access modifiers changed from: protected */
    public String getIccId(int slot) {
        String iccId = mChipInterface.getFullIccid(slot);
        if (!TextUtils.isEmpty(iccId)) {
            return iccId;
        }
        List<SubscriptionInfo> subInfo = null;
        String iccId2 = this.mUiccController.getUiccCard(slot) == null ? null : this.mUiccController.getUiccCard(slot).getIccId();
        if (!TextUtils.isEmpty(iccId2)) {
            return iccId2;
        }
        if (SubscriptionControllerEx.getInstance() != null) {
            subInfo = SubscriptionControllerEx.getInstance().getSubInfoUsingSlotIndexPrivileged(slot);
        }
        if (subInfo != null) {
            return subInfo.get(0).getIccId();
        }
        return iccId2;
    }

    public boolean judgeSubCarrierTypeByMccMnc(int slotId) {
        logd("judgeSubCarrierTypeByMccMnc for slot: " + slotId);
        if (this.isSimInsertedArray[slotId]) {
            HwFullNetworkConstantsInner.SubCarrierType[] subCarrierTypeArr = this.subCarrierTypeArray;
            if (subCarrierTypeArr[slotId] == null || !subCarrierTypeArr[slotId].isCCard()) {
                int appType = getCardAppType(slotId);
                String mccMnc = getMccMnc(slotId, appType);
                if (mccMnc == null) {
                    loge("judgeSubCarrierTypeByMccMnc: mccMnc is invalid, return!");
                    return false;
                }
                logd("judgeSubCarrierTypeByMccMnc: current  is : " + this.subCarrierTypeArray[slotId]);
                if (isCMCCCardByMccMnc(mccMnc)) {
                    if (appType == 2) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CMCC_USIM;
                    } else if (appType == 1) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CMCC_SIM;
                    } else {
                        loge("judgeSubCarrierTypeByMccMnc cmcc: appType is invalid!");
                    }
                } else if (isCUCardByMccMnc(mccMnc)) {
                    if (appType == 2) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CU_USIM;
                    } else if (appType == 1) {
                        this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_CU_SIM;
                    } else {
                        loge("judgeSubCarrierTypeByMccMnc: appType is invalid!");
                    }
                } else if (isAISCardByMccMnc(mccMnc)) {
                    this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_AIS_USIM;
                } else if (isSmartCardByMccMnc(mccMnc)) {
                    this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_SMART_USIM;
                } else if (isMtnCardByMccMnc(mccMnc)) {
                    this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.CARRIER_FOREIGN_MTN_USIM;
                } else {
                    this.subCarrierTypeArray[slotId] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
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
        UiccControllerExt uiccControllerExt = this.mUiccController;
        if (uiccControllerExt == null) {
            return null;
        }
        UiccCardExt uiccCard = uiccControllerExt.getUiccCard(slotId);
        UiccCardApplicationEx app = null;
        if (uiccCard != null) {
            if (appType == 2 || appType == 1) {
                app = uiccCard.getApplication(UiccControllerExt.APP_FAM_3GPP);
            } else if (appType == 4 || appType == 3) {
                app = uiccCard.getApplication(UiccControllerExt.APP_FAM_3GPP2);
            } else {
                loge("unknown appType, return!");
                return null;
            }
        }
        if (app != null) {
            IccRecordsEx records = app.getIccRecords();
            if (records == null) {
                return null;
            }
            String imsi = records.getIMSI();
            if (imsi == null || imsi.length() <= 5) {
                logd("invalid imsi!");
                return null;
            }
            String mccMnc = imsi.substring(0, 5);
            logd("slot " + slotId + " mccMnc = " + mccMnc);
            return mccMnc;
        }
        loge("app is null, return");
        return null;
    }

    private int getCardAppType(int slotId) {
        int appType;
        UiccControllerExt uiccControllerExt = this.mUiccController;
        if (uiccControllerExt == null) {
            return 0;
        }
        UiccCardExt uiccCard = uiccControllerExt.getUiccCard(slotId);
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
        } else if (HwFullNetworkConfigInner.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            logd("CROSS_MAPPING by QCRIL,setPreferredNetworkType directly");
            this.mCi[phoneId].setPreferredNetworkType(networkType, response);
        }
    }

    public boolean isDualImsSwitchOpened() {
        return SystemPropertiesEx.getInt("persist.radio.dualltecap", 0) == 1;
    }

    public boolean isRetainDualCardNetworkTypeForNr() {
        return HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported() && HwNetworkTypeUtils.isDualNrSwitchOpened(this.mContext);
    }

    public boolean isCmccHybirdAndNoRoaming() {
        return (HwFullNetworkConfigInner.isCMCCDsdxDisable() && isCmccHybirdBySubCarrierType()) && !TelephonyManagerEx.isNetworkRoaming(SubscriptionManagerEx.getSubIdUsingSlotId(getCMCCCardSlotId()));
    }

    public int getCombinedRegState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int regState = ServiceStateEx.getVoiceRegState(serviceState);
        int dataRegState = ServiceStateEx.getDataState(serviceState);
        return (regState == 1 && dataRegState == 0) ? dataRegState : regState;
    }

    public String getCMCCUnbindFlag() {
        String cmccUnbindFlag = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(HwFullNetworkConstantsInner.KEY_CMCC_UNBIND_FLAG, HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_INVALID);
        if (!HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_INVALID.equals(cmccUnbindFlag)) {
            return cmccUnbindFlag;
        }
        String cmccUnbindFlag2 = getUnbindFlagOfOeminfo();
        setCMCCUnbindFlag(cmccUnbindFlag2);
        return cmccUnbindFlag2;
    }

    public void setCMCCUnbindFlag(String flag) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putString(HwFullNetworkConstantsInner.KEY_CMCC_UNBIND_FLAG, flag);
        editor.apply();
        logd("setCMCCUnbindFlag:set flag into SP = " + flag);
        setUnbindFlagOfOeminfo(flag);
    }

    private String getUnbindFlagOfOeminfo() {
        String readValue;
        String[] readBuf = {READ_BUF_INIT};
        boolean isSuccess = true;
        int[] errorRet = new int[1];
        int readRet = HwProtectAreaManager.getInstance().readProtectArea(KEY_UNBIND_FILG, 10, readBuf, errorRet);
        if (readRet != 0 || readBuf.length < 1) {
            readValue = HwFullNetworkConstantsInner.CMCC_UNBIND_FLAG_INVALID;
        } else {
            readValue = readBuf[0];
        }
        if (readRet != 0) {
            isSuccess = false;
        }
        logd("getUnbindFlagOfOeminfo: flag = " + readValue + " isSuccess = " + isSuccess + "ReadBuf = " + Arrays.toString(readBuf) + "errorRet = " + Arrays.toString(errorRet));
        return readValue;
    }

    private boolean setUnbindFlagOfOeminfo(String unbindFlag) {
        if (TextUtils.isEmpty(unbindFlag)) {
            return false;
        }
        boolean isSuccess = true;
        if (HwProtectAreaManager.getInstance().writeProtectArea(KEY_UNBIND_FILG, unbindFlag.length(), unbindFlag, new int[1]) != 0) {
            isSuccess = false;
        }
        logd("setUnbindFlagOfOeminfo : flag=" + unbindFlag + " isSuccess = " + isSuccess);
        return isSuccess;
    }

    public boolean isCMCCUnbindBySNDate() {
        String configDateString = HwFullNetworkConfigInner.CMCC_UNBIND_DATE;
        if (TextUtils.isEmpty(configDateString) || configDateString.length() < DATE_STRING_LENGHT) {
            logd("isCMCCUnbindBySNDate configDateString is :" + configDateString);
            return false;
        }
        String snString = Build.getSerial();
        if (TextUtils.isEmpty(snString) || snString.length() < 10) {
            return false;
        }
        try {
            String snDateString = snString.substring(5, 10);
            StringBuffer snDateStringBuffer = new StringBuffer();
            snDateStringBuffer.append(configDateString.substring(0, 2));
            snDateStringBuffer.append(snDateString.substring(0, 2));
            int month = Integer.parseInt(snDateString.substring(2, 3), HEX_TYPE);
            if (month >= 10) {
                snDateStringBuffer.append(month);
            } else {
                snDateStringBuffer.append("0");
                snDateStringBuffer.append(month);
            }
            snDateStringBuffer.append(snDateString.substring(3));
            logd("snDate = " + snDateStringBuffer.toString() + " configDateString = " + configDateString);
            new Date();
            new Date();
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date configDate = sdf.parse(configDateString);
            Date snDate = sdf.parse(snDateStringBuffer.toString());
            if (snDate.after(configDate) || snDate.equals(configDate)) {
                return true;
            }
            return false;
        } catch (ParseException e) {
            loge("isCMCCUnbindBySNDate Parse date error!");
            return false;
        } catch (Exception e2) {
            loge("isCMCCUnbindBySNDate Exception error!,return false");
            return false;
        }
    }

    public boolean isSupportEuicc() {
        return this.mContext.getPackageManager() != null && this.mContext.getPackageManager().hasSystemFeature("android.hardware.telephony.euicc");
    }

    public void setSimContactLoaded(int slotId, boolean loaded) {
        if (isValidIndex(slotId)) {
            this.isSimContactLoadeds[slotId] = loaded;
            logd("setSimContactLoaded[" + slotId + "]=" + loaded);
        }
    }

    public boolean isAllSimContactLoaded() {
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            if (!this.isSimContactLoadeds[i]) {
                logd("isAllSimContactLoaded, isSimContactLoadeds is false, slotId = " + i);
                return false;
            }
        }
        return true;
    }

    public boolean isVsimWorking() {
        return HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwTelephonyManager.getDefault().isVSimInProcess() || HwTelephonyManager.getDefault().isVSimEnabled());
    }

    private void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
