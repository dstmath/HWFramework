package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.RegistrantEx;
import com.huawei.android.os.RegistrantListEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionInfoEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public class HwFullNetworkChipHisi implements HwFullNetworkChipCommon.HwFullNetworkChipInterface {
    static final int CARD_TYPE_EUICC = 1;
    static final int CARD_TYPE_UICC = 0;
    static final int EMPTY_APPLICATION = 0;
    private static final String LOG_TAG = "HwFullNetworkChipHisi";
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkChipHisi mInstance;
    private static final Object mLock = new Object();
    int curSetDataAllowCount = 0;
    boolean isHotPlugCompleted = false;
    boolean isPreBootCompleted = false;
    boolean mAllCardsReady = false;
    boolean mAutoSwitchDualCardsSlotDone = false;
    int mBalongSimSlot = 0;
    boolean mBroadcastDone = false;
    boolean[] mCardReady4Switch = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    int mCardType = 0;
    int[] mCardTypes = new int[HwFullNetworkConstantsInner.SIM_NUM];
    boolean mCommrilRestartRild = false;
    private Context mContext;
    String[] mFullIccIds = new String[HwFullNetworkConstantsInner.SIM_NUM];
    boolean[] mGetBalongSimSlotDone = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    boolean[] mGetUiccCardsStatusDone = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    boolean mHasRadioAvailable = false;
    HwFullNetworkConstantsInner.HotplugState[] mHotplugState = new HwFullNetworkConstantsInner.HotplugState[HwFullNetworkConstantsInner.SIM_NUM];
    private RegistrantListEx mIccChangedRegistrants = new RegistrantListEx();
    int mLastAppNums = 0;
    boolean mNvRestartRildDone = false;
    int[] mOldMainSwitchTypes = new int[HwFullNetworkConstantsInner.SIM_NUM];
    boolean[] mRadioOn = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    boolean[] mRadioOns = new boolean[HwFullNetworkConstantsInner.SIM_NUM];
    Message mSetSdcsCompleteMsg = null;
    int[] mSwitchTypes = new int[HwFullNetworkConstantsInner.SIM_NUM];
    boolean needFixMainSlotPosition = false;
    int needSetDataAllowCount = 0;

    private HwFullNetworkChipHisi(Context context, CommandsInterfaceEx[] ci) {
        logi("HwFullNetworkChipHisi constructor");
        this.mContext = context;
    }

    static HwFullNetworkChipHisi make(Context context, CommandsInterfaceEx[] ci) {
        HwFullNetworkChipHisi hwFullNetworkChipHisi;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("HwFullNetworkChipHisi.make() should only be called once");
            }
            mInstance = new HwFullNetworkChipHisi(context, ci);
            mChipCommon = HwFullNetworkChipCommon.getInstance();
            mChipCommon.setChipInterface(mInstance);
            hwFullNetworkChipHisi = mInstance;
        }
        return hwFullNetworkChipHisi;
    }

    static HwFullNetworkChipHisi getInstance() {
        HwFullNetworkChipHisi hwFullNetworkChipHisi;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("HwFullNetworkChipHisi.getInstance can't be called before make()");
            }
            hwFullNetworkChipHisi = mInstance;
        }
        return hwFullNetworkChipHisi;
    }

    public void handleIccATR(String strTempATR, Integer index) {
        String strATR = strTempATR;
        logi("handleIccATR, ATR: [" + strATR + "], index:[" + index + "]");
        if (strATR == null || strATR.isEmpty()) {
            strATR = "null";
        }
        if (strATR.length() > 66) {
            loge("strATR.length() greater than PROP_VALUE_MAX");
            strATR = strATR.substring(0, 66);
        }
        if (index.intValue() == 0) {
            SystemPropertiesEx.set("gsm.sim.hw_atr", strATR);
        } else {
            SystemPropertiesEx.set("gsm.sim.hw_atr1", strATR);
        }
    }

    public void onGetCdmaModeSideDone(AsyncResultEx ar, Integer index) {
        logd("onGetCdmaModeSideDone");
        int mCdmaModemSide = 0;
        HwFullNetworkConstantsInner.CommrilMode currentCommrilModem = HwFullNetworkConstantsInner.CommrilMode.NON_MODE;
        if (!(ar == null || ar.getException() != null || ar.getResult() == null)) {
            mCdmaModemSide = ((int[]) ar.getResult())[0];
        }
        if (mCdmaModemSide == 0) {
            currentCommrilModem = HwFullNetworkConstantsInner.CommrilMode.HISI_CGUL_MODE;
        } else if (mCdmaModemSide == 1) {
            currentCommrilModem = HwFullNetworkConstantsInner.CommrilMode.HISI_CG_MODE;
        } else if (mCdmaModemSide == 2) {
            currentCommrilModem = HwFullNetworkConstantsInner.CommrilMode.HISI_VSIM_MODE;
        } else {
            logd("onGetCdmaModeSideDone CDMA modem in wrong side.");
        }
        SystemPropertiesEx.set(HwFullNetworkConfigInner.PROPERTY_COMMRIL_MODE, currentCommrilModem.toString());
        logi("onGetCdmaModeSideDone mCdmaModemSide = " + mCdmaModemSide + " set currentCommrilModem=" + currentCommrilModem);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isSwitchDualCardSlotsEnabled() {
        if (mChipCommon.mUiccController == null || mChipCommon.mUiccController.getUiccCards() == null || mChipCommon.mUiccController.getUiccCards().length < 2) {
            loge("haven't get all UiccCards done, please wait!");
            return false;
        }
        for (UiccCardExt uc : mChipCommon.mUiccController.getUiccCards()) {
            if (uc == null) {
                loge("haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        if (!mChipCommon.isSwitchSlotEnabledForCMCC()) {
            logi("isSwitchSlotEnabledForCMCC: CMCC hybird and CMCC is not roaming return false");
            return false;
        } else if (HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE && mChipCommon.isCTHybird()) {
            return false;
        } else {
            if ((HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 1 || !mChipCommon.isCMCCHybird()) && (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 2 || !mChipCommon.isCTHybird())) {
                logd("mSwitchTypes[0] = " + this.mSwitchTypes[0] + ", mSwitchTypes[1] = " + this.mSwitchTypes[1]);
                if (!HwFullNetworkConfigInner.IS_CHINA_TELECOM) {
                    refreshCardState();
                    return isSwitchDualEnabledNotChinaTelecom();
                }
                boolean result = false;
                if (isValidSwitchType()) {
                    result = true;
                }
                if (mChipCommon.getWaitingSwitchBalongSlot()) {
                    return false;
                }
                return result;
            }
            logi("MDMCarrierCheck: Hisi hybird and MDMCarrier enable return false");
            return false;
        }
    }

    private boolean isSwitchDualEnabledNotChinaTelecom() {
        boolean result = false;
        boolean isSub0Inserted = this.mSwitchTypes[0] > 0 || mChipCommon.isSimInsertedArray[0];
        boolean isSub1Inserted = this.mSwitchTypes[1] > 0 || mChipCommon.isSimInsertedArray[1];
        if (isEuiccInSlot2AndNoProfile()) {
            isSub1Inserted = false;
        }
        if (isSub0Inserted && isSub1Inserted) {
            result = true;
        }
        if (mChipCommon.getWaitingSwitchBalongSlot()) {
            return false;
        }
        return result;
    }

    private boolean isValidSwitchType() {
        if (this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 3) {
            return true;
        }
        if (this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 2) {
            return true;
        }
        if (this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 3) {
            return true;
        }
        if (this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 2) {
            return true;
        }
        return this.mSwitchTypes[0] == 1 && this.mSwitchTypes[1] == 1;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            RegistrantEx registrantEx = new RegistrantEx(h, what, obj);
            this.mIccChangedRegistrants.add(registrantEx);
            registrantEx.notifyRegistrant();
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
    }

    public boolean isBalongSimSynced() {
        return mChipCommon.getUserSwitchDualCardSlots() == this.mBalongSimSlot;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean getWaitingSwitchBalongSlot() {
        return mChipCommon.isSet4GSlotInProgress;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        logi("setWaitingSwitchBalongSlot  iSetResult = " + iSetResult);
        mChipCommon.isSet4GSlotInProgress = iSetResult;
        SystemPropertiesEx.set("gsm.dualcards.switch", iSetResult ? "true" : "false");
        this.mIccChangedRegistrants.notifyRegistrants((Object) null, 0, (Throwable) null);
        this.mIccChangedRegistrants.notifyRegistrants((Object) null, 1, (Throwable) null);
    }

    public boolean anySimCardChanged() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mChipCommon.mContext);
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            String oldIccId = sp.getString("4G_AUTO_SWITCH_ICCID_SLOT" + i, BuildConfig.FLAVOR);
            if (!BuildConfig.FLAVOR.equals(oldIccId)) {
                try {
                    oldIccId = HwAESCryptoUtil.decrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, oldIccId);
                } catch (IllegalArgumentException e) {
                    loge("HwAESCryptoUtil decrypt IllegalArgumentException changed.");
                } catch (Exception e2) {
                    loge("HwAESCryptoUtil decrypt excepiton.");
                }
            }
            logi("anySimCardChanged oldIccId[" + i + "] = " + SubscriptionInfoEx.givePrintableIccid(oldIccId));
            logi("anySimCardChanged nowIccId[" + i + "] = " + SubscriptionInfoEx.givePrintableIccid(mChipCommon.mIccIds[i]));
            if (!oldIccId.equals(this.mFullIccIds[i])) {
                return true;
            }
        }
        return false;
    }

    public void disposeCardStatus(boolean resetSwitchDualCardsFlag) {
        logi("disposeCardStatus. resetSwitchDualCardsFlag = " + resetSwitchDualCardsFlag);
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            this.mSwitchTypes[i] = -1;
            this.mGetUiccCardsStatusDone[i] = false;
            this.mGetBalongSimSlotDone[i] = false;
            this.mCardTypes[i] = -1;
            mChipCommon.mIccIds[i] = null;
            this.mFullIccIds[i] = null;
        }
        this.mAllCardsReady = false;
        if (HwFullNetworkConfigInner.IS_HISI_DSDX && resetSwitchDualCardsFlag) {
            logi("set mAutoSwitchDualCardsSlotDone to false");
            this.mAutoSwitchDualCardsSlotDone = false;
        }
    }

    public void setPrefNwForCmcc(Handler h) {
        int networkMode;
        logd("setPrefNwForCmcc enter.");
        if (!(h != null && HwFullNetworkConfigInner.isCMCCDsdxDisable() && HwFullNetworkConfigInner.IS_VICE_WCDMA)) {
            return;
        }
        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimEnabled() && !HwVSimUtils.isVSimInProcess())) {
            for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                if (mChipCommon.mIccIds[i] == null) {
                    logi("setPrefNwForCmcc: mIccIds[" + i + "] is null");
                    return;
                }
            }
            PhoneExt[] phones = PhoneFactoryExt.getPhones();
            for (int i2 = 0; i2 < HwFullNetworkConstantsInner.SIM_NUM; i2++) {
                PhoneExt PhoneExt = phones[i2];
                if (PhoneExt == null) {
                    loge("setPrefNwForCmcc: PhoneExt " + i2 + " is null");
                } else {
                    if (mChipCommon.getUserSwitchDualCardSlots() == i2) {
                        networkMode = getMainSlotNetworkMode(i2);
                    } else {
                        networkMode = getSlaveSlotNetworkMode(i2);
                    }
                    PhoneExt.setPreferredNetworkType(networkMode, h.obtainMessage(HwFullNetworkConstantsInner.EVENT_CMCC_SET_NETWOR_DONE, i2, networkMode));
                    logi("setPrefNwForCmcc: i = " + i2 + ", mode = " + networkMode);
                }
            }
            return;
        }
        logi("setPrefNwForCmcc: vsim on sub, not set pref nw for cmcc.");
    }

    private int getMainSlotNetworkMode(int i) {
        HwTelephonyManager mHwTelephonyManager = HwTelephonyManager.getDefault();
        boolean isNrSupport = HwModemCapability.isCapabilitySupport(29);
        int ability = 1;
        int nrAbility = 0;
        if (mHwTelephonyManager != null) {
            ability = mHwTelephonyManager.getLteServiceAbility();
            if (isNrSupport) {
                nrAbility = mHwTelephonyManager.getServiceAbility(i, 1);
            }
            logi("getMainSlotNetworkMode: LteServiceAbility = " + ability + " nrAbility = " + nrAbility);
        }
        if (HwFullNetworkConfigInner.IS_FAST_SWITCH_SIMSLOT || !mChipCommon.isCDMASimCard(i)) {
            if (isNrSupport && nrAbility == 1) {
                return 65;
            }
            return (ability == 1 || mChipCommon.isCMCCCardBySlotId(i)) ? 9 : 3;
        } else if (isNrSupport && nrAbility == 1) {
            return 64;
        } else {
            return ability == 1 ? 8 : 4;
        }
    }

    private int getSlaveSlotNetworkMode(int i) {
        int cmccSubId = SubscriptionManagerEx.getSubIdUsingSlotId(mChipCommon.getCMCCCardSlotId());
        int networkMode = 4;
        int networkMode2 = 3;
        if (!mChipCommon.isCMCCHybird() || mChipCommon.isCMCCCardBySlotId(i) || TelephonyManagerEx.isNetworkRoaming(cmccSubId)) {
            if (!mChipCommon.isCDMASimCard(i)) {
                networkMode = 3;
            }
            if (!HwFullNetworkConfigInner.IS_DUAL_4G_SUPPORTED) {
                return networkMode;
            }
            if (!mChipCommon.isCMCCCardBySlotId(i)) {
                return getDualNonCmccCardsNetworkMode(i);
            }
            if (mChipCommon.isDualImsSwitchOpened()) {
                networkMode2 = 9;
            }
            return networkMode2;
        }
        if (!mChipCommon.isCDMASimCard(i)) {
            networkMode = 3;
        }
        return networkMode;
    }

    private int getDualNonCmccCardsNetworkMode(int i) {
        return mChipCommon.isDualImsSwitchOpened() ? mChipCommon.isCDMASimCard(i) ? 8 : 9 : mChipCommon.isCDMASimCard(i) ? 4 : 3;
    }

    public void handleSetCmccPrefNetwork(Message msg) {
        int prefslot = msg.arg1;
        int setPrefMode = msg.arg2;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            mChipCommon.needRetrySetPrefNetwork = true;
            loge("setPrefNwForCmcc: Fail, slot " + prefslot + " network mode " + setPrefMode);
            return;
        }
        if (HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, prefslot) != setPrefMode) {
            HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, prefslot, setPrefMode);
        }
        logi("setPrefNwForCmcc: Success, slot " + prefslot + " network mode " + setPrefMode);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isSet4GDoneAfterSimInsert() {
        return this.mAutoSwitchDualCardsSlotDone;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isSettingDefaultData() {
        return this.needSetDataAllowCount > 0;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public int getSpecCardType(int slotId) {
        if (slotId < 0 || slotId >= HwFullNetworkConstantsInner.SIM_NUM) {
            return -1;
        }
        return this.mCardTypes[slotId];
    }

    public void saveIccidsWhenAllCardsReady() {
        logi("saveIccidsWhenAllCardsReady");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            String iccIdToSave = this.mFullIccIds[i];
            if (!(iccIdToSave == null || BuildConfig.FLAVOR.equals(iccIdToSave)) || HwFullNetworkConfigInner.isCMCCDsdxDisable() || HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 0) {
                try {
                    iccIdToSave = HwAESCryptoUtil.encrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, iccIdToSave);
                } catch (IllegalArgumentException e) {
                    loge("HwAESCryptoUtil decrypt IllegalArgumentException.");
                } catch (Exception e2) {
                    loge("HwAESCryptoUtil decrypt excepiton.");
                }
                editor.putString("4G_AUTO_SWITCH_ICCID_SLOT" + i, iccIdToSave);
                editor.apply();
            }
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void refreshCardState() {
        for (int index = 0; index < HwFullNetworkConstantsInner.SIM_NUM; index++) {
            mChipCommon.isSimInsertedArray[index] = mChipCommon.isCardPresent(index);
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public boolean isRestartRildProgress() {
        return this.mNvRestartRildDone;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public void resetUiccSubscriptionResultFlag(int slotId) {
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public int getBalongSimSlot() {
        return this.mBalongSimSlot;
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon.HwFullNetworkChipInterface
    public String getFullIccid(int slotId) {
        if (!mChipCommon.isValidIndex(slotId)) {
            return null;
        }
        return this.mFullIccIds[slotId];
    }

    public void setCardReadyState(int slotId, boolean ready) {
        if (mChipCommon.isValidIndex(slotId)) {
            this.mCardReady4Switch[slotId] = ready;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isEuiccInSlot2AndNoProfile() {
        UiccCardExt[] uiccCards;
        if (mChipCommon.isSupportEuicc() && (uiccCards = mChipCommon.getUiccController().getUiccCards()) != null && uiccCards[1] != null && uiccCards[1].isEuiccCard()) {
            int numApps = uiccCards[1].getNumApplications();
            logd("isEuiccInSlot2AndNoProfile, numApps " + numApps);
            if (numApps == 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isEuiccInSlot2() {
        UiccCardExt[] uiccCards;
        if (mChipCommon.isSupportEuicc() && (uiccCards = mChipCommon.getUiccController().getUiccCards()) != null && uiccCards[1] != null && uiccCards[1].isEuiccCard()) {
            return true;
        }
        return false;
    }

    private boolean isSimPresentByCardState(int slotId) {
        UiccCardExt[] uiccCards = mChipCommon.getUiccController().getUiccCards();
        if (uiccCards == null || slotId < 0 || slotId >= TelephonyManagerEx.getDefault().getPhoneCount()) {
            return false;
        }
        if (uiccCards[slotId].getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT || this.mSwitchTypes[slotId] > 0) {
            return true;
        }
        return false;
    }

    public boolean isSimPresentBySubState(int slotId) {
        boolean isSimPresent = isSimPresentByCardState(slotId);
        if (!mChipCommon.isSupportEuicc()) {
            return isSimPresent;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("isSimPresentBySubState, slotId = ");
        sb.append(slotId);
        sb.append(", isSimPresent = ");
        sb.append(isSimPresent);
        sb.append(", subState = ");
        sb.append(HwTelephonyManager.getDefault().getSubState((long) slotId) == 1);
        logd(sb.toString());
        if (!isSimPresent || HwTelephonyManager.getDefault().getSubState((long) slotId) != 1) {
            return false;
        }
        return true;
    }

    private void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private void logi(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
