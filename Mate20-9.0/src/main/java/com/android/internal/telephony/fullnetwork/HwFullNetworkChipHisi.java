package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.vsim.HwVSimUtils;

public class HwFullNetworkChipHisi implements HwFullNetworkChipCommon.HwFullNetworkChipInterface {
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
    boolean[] mCardReady4Switch = new boolean[HwFullNetworkConstants.SIM_NUM];
    int[] mCardTypes = new int[HwFullNetworkConstants.SIM_NUM];
    boolean mCommrilRestartRild = false;
    private Context mContext;
    String[] mFullIccIds = new String[HwFullNetworkConstants.SIM_NUM];
    boolean[] mGetBalongSimSlotDone = new boolean[HwFullNetworkConstants.SIM_NUM];
    boolean[] mGetUiccCardsStatusDone = new boolean[HwFullNetworkConstants.SIM_NUM];
    HwFullNetworkConstants.HotplugState[] mHotplugState = new HwFullNetworkConstants.HotplugState[HwFullNetworkConstants.SIM_NUM];
    private RegistrantList mIccChangedRegistrants = new RegistrantList();
    boolean mNvRestartRildDone = false;
    int[] mOldMainSwitchTypes = new int[HwFullNetworkConstants.SIM_NUM];
    boolean[] mRadioOn = new boolean[HwFullNetworkConstants.SIM_NUM];
    boolean[] mRadioOns = new boolean[HwFullNetworkConstants.SIM_NUM];
    Message mSetSdcsCompleteMsg = null;
    int[] mSwitchTypes = new int[HwFullNetworkConstants.SIM_NUM];
    boolean needFixMainSlotPosition = false;
    int needSetDataAllowCount = 0;

    private HwFullNetworkChipHisi(Context context, CommandsInterface[] ci) {
        logd("HwFullNetworkChipHisi constructor");
        this.mContext = context;
    }

    static HwFullNetworkChipHisi make(Context context, CommandsInterface[] ci) {
        HwFullNetworkChipHisi hwFullNetworkChipHisi;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwFullNetworkChipHisi(context, ci);
                mChipCommon = HwFullNetworkChipCommon.getInstance();
                mChipCommon.setChipInterface(mInstance);
                hwFullNetworkChipHisi = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkChipHisi.make() should only be called once");
            }
        }
        return hwFullNetworkChipHisi;
    }

    static HwFullNetworkChipHisi getInstance() {
        HwFullNetworkChipHisi hwFullNetworkChipHisi;
        synchronized (mLock) {
            if (mInstance != null) {
                hwFullNetworkChipHisi = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkChipHisi.getInstance can't be called before make()");
            }
        }
        return hwFullNetworkChipHisi;
    }

    public void handleIccATR(String strATR, Integer index) {
        logd("handleIccATR, ATR: [" + strATR + "], index:[" + index + "]");
        if (strATR == null || strATR.isEmpty()) {
            strATR = "null";
        }
        if (strATR.length() > 66) {
            logd("strATR.length() greater than PROP_VALUE_MAX");
            strATR = strATR.substring(0, 66);
        }
        if (index.intValue() == 0) {
            SystemProperties.set("gsm.sim.hw_atr", strATR);
        } else {
            SystemProperties.set("gsm.sim.hw_atr1", strATR);
        }
    }

    public void onGetCdmaModeSideDone(AsyncResult ar, Integer index) {
        logd("onGetCdmaModeSideDone");
        int mCdmaModemSide = 0;
        HwFullNetworkConstants.CommrilMode currentCommrilModem = HwFullNetworkConstants.CommrilMode.NON_MODE;
        if (!(ar == null || ar.exception != null || ar.result == null)) {
            mCdmaModemSide = ((int[]) ar.result)[0];
        }
        if (mCdmaModemSide == 0) {
            currentCommrilModem = HwFullNetworkConstants.CommrilMode.HISI_CGUL_MODE;
        } else if (1 == mCdmaModemSide) {
            currentCommrilModem = HwFullNetworkConstants.CommrilMode.HISI_CG_MODE;
        } else if (2 == mCdmaModemSide) {
            currentCommrilModem = HwFullNetworkConstants.CommrilMode.HISI_VSIM_MODE;
        }
        SystemProperties.set("persist.radio.commril_mode", currentCommrilModem.toString());
        logd("onGetCdmaModeSideDone mCdmaModemSide = " + mCdmaModemSide + " set currentCommrilModem=" + currentCommrilModem);
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        boolean isValidSwitchType = false;
        if (mChipCommon.mUiccController == null || mChipCommon.mUiccController.getUiccCards() == null || mChipCommon.mUiccController.getUiccCards().length < 2) {
            loge("haven't get all UiccCards done, please wait!");
            return false;
        }
        for (UiccCard uc : mChipCommon.mUiccController.getUiccCards()) {
            if (uc == null) {
                loge("haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        if (!mChipCommon.isSwitchSlotEnabledForCMCC()) {
            logd("isSwitchSlotEnabledForCMCC: CMCC hybird and CMCC is not roaming return false");
            return false;
        } else if (HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE && mChipCommon.isCTHybird()) {
            return false;
        } else {
            logd("mSwitchTypes[0] = " + this.mSwitchTypes[0] + ", mSwitchTypes[1] = " + this.mSwitchTypes[1]);
            if (!HwFullNetworkConfig.IS_CHINA_TELECOM) {
                refreshCardState();
                if ((this.mSwitchTypes[0] > 0 || mChipCommon.isSimInsertedArray[0]) && (this.mSwitchTypes[1] > 0 || mChipCommon.isSimInsertedArray[1])) {
                    isValidSwitchType = true;
                }
                return isValidSwitchType;
            }
            boolean result = false;
            if ((this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 3) || ((this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 2) || ((this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 3) || ((this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 2) || (this.mSwitchTypes[0] == 1 && this.mSwitchTypes[1] == 1))))) {
                isValidSwitchType = true;
            }
            if (isValidSwitchType) {
                result = true;
            }
            return result;
        }
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mIccChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
    }

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        logd("setWaitingSwitchBalongSlot  iSetResult = " + iSetResult);
        mChipCommon.isSet4GSlotInProgress = iSetResult;
        SystemProperties.set("gsm.dualcards.switch", iSetResult ? "true" : "false");
        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, 0, null));
        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, 1, null));
    }

    public boolean isBalongSimSynced() {
        return mChipCommon.getUserSwitchDualCardSlots() == this.mBalongSimSlot;
    }

    public boolean getWaitingSwitchBalongSlot() {
        return mChipCommon.isSet4GSlotInProgress;
    }

    public boolean anySimCardChanged() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mChipCommon.mContext);
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            String oldIccId = sp.getString("4G_AUTO_SWITCH_ICCID_SLOT" + i, "");
            if (!"".equals(oldIccId)) {
                try {
                    oldIccId = HwAESCryptoUtil.decrypt(HwFullNetworkConstants.MASTER_PASSWORD, oldIccId);
                } catch (Exception ex) {
                    logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
                }
            }
            logd("anySimCardChanged oldIccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(oldIccId));
            logd("anySimCardChanged nowIccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(mChipCommon.mIccIds[i]));
            if (!oldIccId.equals(this.mFullIccIds[i])) {
                return true;
            }
        }
        return false;
    }

    public void disposeCardStatus(boolean resetSwitchDualCardsFlag) {
        logd("disposeCardStatus. resetSwitchDualCardsFlag = " + resetSwitchDualCardsFlag);
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            this.mSwitchTypes[i] = -1;
            this.mGetUiccCardsStatusDone[i] = false;
            this.mGetBalongSimSlotDone[i] = false;
            this.mCardTypes[i] = -1;
            mChipCommon.mIccIds[i] = null;
            this.mFullIccIds[i] = null;
        }
        this.mAllCardsReady = false;
        if (HwFullNetworkConfig.IS_HISI_DSDX && resetSwitchDualCardsFlag) {
            logd("set mAutoSwitchDualCardsSlotDone to false");
            this.mAutoSwitchDualCardsSlotDone = false;
        }
    }

    public void setPrefNwForCmcc(Handler h) {
        int networkMode;
        logd("setPrefNwForCmcc enter.");
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && HwFullNetworkConfig.IS_VICE_WCDMA) {
            if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimInProcess()) {
                logd("setPrefNwForCmcc: vsim on sub, not set pref nw for cmcc.");
                return;
            }
            for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
                if (mChipCommon.mIccIds[i] == null) {
                    logd("setPrefNwForCmcc: mIccIds[" + i + "] is null");
                    return;
                }
            }
            Phone[] phones = PhoneFactory.getPhones();
            for (int i2 = 0; i2 < HwFullNetworkConstants.SIM_NUM; i2++) {
                Phone phone = phones[i2];
                if (phone == null) {
                    loge("setPrefNwForCmcc: phone " + i2 + " is null");
                } else {
                    int i3 = 8;
                    int i4 = 9;
                    int i5 = 4;
                    if (mChipCommon.getUserSwitchDualCardSlots() == i2) {
                        HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();
                        int ability = 1;
                        if (mHwTelephonyManager != null) {
                            ability = mHwTelephonyManager.getLteServiceAbility();
                            logd("setPrefNwForCmcc: LteServiceAbility = " + ability);
                        }
                        if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT || !mChipCommon.isCDMASimCard(i2)) {
                            if (ability != 1 && !mChipCommon.isCMCCCardBySlotId(i2)) {
                                i4 = 3;
                            }
                            networkMode = i4;
                        } else {
                            if (ability != 1) {
                                i3 = 4;
                            }
                            networkMode = i3;
                        }
                    } else if (!mChipCommon.isCMCCHybird() || mChipCommon.isCMCCCardBySlotId(i2) || TelephonyManager.getDefault().isNetworkRoaming(mChipCommon.getCMCCCardSlotId())) {
                        networkMode = mChipCommon.isCDMASimCard(i2) ? 4 : 3;
                        if (HwFullNetworkConfig.IS_DUAL_4G_SUPPORTED) {
                            if (mChipCommon.isCMCCCardBySlotId(i2)) {
                                if (!mChipCommon.isDualImsSwitchOpened()) {
                                    i4 = 3;
                                }
                                networkMode = i4;
                            } else if (mChipCommon.isDualImsSwitchOpened()) {
                                if (!mChipCommon.isCDMASimCard(i2)) {
                                    i3 = 9;
                                }
                                networkMode = i3;
                            } else {
                                if (!mChipCommon.isCDMASimCard(i2)) {
                                    i5 = 3;
                                }
                                networkMode = i5;
                            }
                        }
                    } else {
                        if (!mChipCommon.isCDMASimCard(i2)) {
                            i5 = 3;
                        }
                        networkMode = i5;
                    }
                    phone.setPreferredNetworkType(networkMode, h.obtainMessage(HwFullNetworkConstants.EVENT_CMCC_SET_NETWOR_DONE, i2, networkMode));
                    logd("setPrefNwForCmcc: i = " + i2 + ", mode = " + networkMode);
                }
            }
        }
    }

    public void handleSetCmccPrefNetwork(Message msg) {
        int prefslot = msg.arg1;
        int setPrefMode = msg.arg2;
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            mChipCommon.needRetrySetPrefNetwork = true;
            loge("setPrefNwForCmcc: Fail, slot " + prefslot + " network mode " + setPrefMode);
            return;
        }
        if (HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, prefslot) != setPrefMode) {
            HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, prefslot, setPrefMode);
        }
        logd("setPrefNwForCmcc: Success, slot " + prefslot + " network mode " + setPrefMode);
    }

    public boolean isSet4GDoneAfterSimInsert() {
        return this.mAutoSwitchDualCardsSlotDone;
    }

    public boolean isSettingDefaultData() {
        return this.needSetDataAllowCount > 0;
    }

    public int getSpecCardType(int slotId) {
        if (slotId < 0 || slotId >= HwFullNetworkConstants.SIM_NUM) {
            return -1;
        }
        return this.mCardTypes[slotId];
    }

    public void saveIccidsWhenAllCardsReady() {
        logd("saveIccidsWhenAllCardsReady");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            String iccIdToSave = this.mFullIccIds[i];
            if ((iccIdToSave != null && !"".equals(iccIdToSave)) || HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE || HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE) {
                try {
                    iccIdToSave = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, iccIdToSave);
                } catch (Exception ex) {
                    logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
                }
                editor.putString("4G_AUTO_SWITCH_ICCID_SLOT" + i, iccIdToSave);
                editor.apply();
            }
        }
    }

    public void refreshCardState() {
        for (int index = 0; index < HwFullNetworkConstants.SIM_NUM; index++) {
            mChipCommon.isSimInsertedArray[index] = mChipCommon.isCardPresent(index);
        }
    }

    public void setCommrilRestartRild(boolean bCommrilRestartRild) {
        if (this.mCommrilRestartRild != bCommrilRestartRild) {
            this.mCommrilRestartRild = bCommrilRestartRild;
            logd("setCommrilRestartRild = " + bCommrilRestartRild);
        }
    }

    public boolean isRestartRildProgress() {
        return this.mNvRestartRildDone;
    }

    public void resetUiccSubscriptionResultFlag(int slotId) {
    }

    public int getBalongSimSlot() {
        return this.mBalongSimSlot;
    }

    public String getFullIccid(int subId) {
        if (!mChipCommon.isValidIndex(subId)) {
            return null;
        }
        return this.mFullIccIds[subId];
    }

    public void setCardReadyState(int slotId, boolean ready) {
        if (mChipCommon.isValidIndex(slotId)) {
            this.mCardReady4Switch[slotId] = ready;
        }
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
