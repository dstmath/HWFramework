package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractSubscriptionInfoUpdater;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.IccException;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.List;

public class HwSubscriptionInfoUpdaterReference implements AbstractSubscriptionInfoUpdater.SubscriptionInfoUpdaterReference {
    private static final byte[] C2 = {-89, 82, 3, 85, -88, -104, 57, -10, -103, 108, -88, 122, -38, -12, -55, -2};
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final boolean DBG = true;
    private static final int EVENT_ICC_CHANGED = 101;
    private static final int EVENT_QUERY_ICCID_DONE = 103;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String ICCID_STRING_FOR_NV = "DUMMY_NV_ID";
    public static final boolean IS_QUICK_BROADCAST_STATUS = SystemProperties.getBoolean("ro.quick_broadcast_cardstatus", false);
    private static final boolean IS_SINGLE_CARD_TRAY = SystemProperties.getBoolean("persist.radio.single_card_tray", true);
    private static final String LOG_TAG = "HwSubscriptionInfoUpdaterReference";
    private static final String MASTER_PASSWORD = HwAESCryptoUtil.getKey(SubscriptionHelper.C1, C2, SubscriptionInfoUpdaterUtils.C3);
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final boolean VDBG = false;
    private static Context mContext = null;
    private static IccFileHandler[] mFh = new IccFileHandler[PROJECT_SIM_NUM];
    private static IccRecords[] mIccRecords = new IccRecords[PROJECT_SIM_NUM];
    private static UiccController mUiccController = null;
    private static IccCardStatus.CardState[] sCardState = new IccCardStatus.CardState[PROJECT_SIM_NUM];
    private static SubscriptionInfoUpdaterUtils subscriptionInfoUpdaterUtils = new SubscriptionInfoUpdaterUtils();
    private String[] internalOldIccId = new String[PROJECT_SIM_NUM];
    private boolean isNVSubAvailable = false;
    private boolean mChangeIccidDone = false;
    private CommandsInterface[] mCis;
    private Handler mHandler = null;
    private SubscriptionInfoUpdater mSubscriptionInfoUpdater;

    public HwSubscriptionInfoUpdaterReference(SubscriptionInfoUpdater subscriptionInfoUpdater) {
        this.mSubscriptionInfoUpdater = subscriptionInfoUpdater;
    }

    public void subscriptionInfoInit(Handler handler, Context context, CommandsInterface[] ci) {
        this.mCis = (CommandsInterface[]) ci.clone();
        this.mHandler = handler;
        mContext = context;
        SubscriptionHelper.init(context, ci);
        mUiccController = UiccController.getInstance();
        mUiccController.registerForIccChanged(this.mHandler, EVENT_ICC_CHANGED, null);
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sCardState[i] = IccCardStatus.CardState.CARDSTATE_ABSENT;
        }
        HwCardTrayInfo.make(ci);
    }

    public void handleMessageExtend(Message msg) {
        String iccId;
        AsyncResult ar = (AsyncResult) msg.obj;
        int i = msg.what;
        if (i != EVENT_ICC_CHANGED) {
            if (i != EVENT_QUERY_ICCID_DONE) {
                logd("Unknown msg:" + msg.what);
            } else {
                Integer slotId = (Integer) ar.userObj;
                logd("handleMessage : <EVENT_QUERY_ICCID_DONE> SIM" + (slotId.intValue() + 1));
                if (ar.exception == null) {
                    if (ar.result != null) {
                        if (ar.result instanceof byte[]) {
                            byte[] data = (byte[]) ar.result;
                            iccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                        } else {
                            try {
                                iccId = (String) ar.result;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = iccId;
                        if (subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] != null && subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()].trim().length() == 0) {
                            String[] iccId2 = subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater);
                            int intValue = slotId.intValue();
                            iccId2[intValue] = "emptyiccid" + slotId;
                        }
                        if (HwVSimUtils.needBlockUnReservedForVsim(slotId.intValue())) {
                            subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                            logd("the slot is unreserved for vsim,just set to no_sim");
                        }
                    } else {
                        logd("Null ar");
                        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                    }
                } else if ((!(ar.exception instanceof CommandException) || !(ar.exception.getCommandError() == CommandException.Error.RADIO_NOT_AVAILABLE || ar.exception.getCommandError() == CommandException.Error.GENERIC_FAILURE)) && !(ar.exception instanceof IccException)) {
                    subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                    logd("Query IccId fail: " + ar.exception);
                } else {
                    logd("Do Nothing.");
                }
                logd("mIccId[" + slotId + "] = " + printIccid(subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()]));
                setNeedUpdateIfNeed(slotId.intValue(), subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()]);
                if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                    SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                    if (SubscriptionInfoUpdater.mNeedUpdate) {
                        subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                    }
                }
            }
        } else if (ar.result != null) {
            updateIccAvailability(((Integer) ar.result).intValue());
        } else {
            loge("Error: Invalid card index EVENT_ICC_CHANGED ");
        }
    }

    private void handleCardAbsent(int slotId, IccCardStatus.CardState oldState, IccCardStatus.CardState newState) {
        if (!ICCID_STRING_FOR_NO_SIM.equals(subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId])) {
            logd("SIM" + (slotId + 1) + " hot plug out");
            SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
            SubscriptionInfoUpdater.mNeedUpdate = true;
            resetInternalOldIccId(slotId);
        }
        if (HwVSimUtils.needBlockUnReservedForVsim(slotId) && !IccCardStatusUtils.isCardPresent(newState) && IccCardStatusUtils.isCardPresent(oldState)) {
            logd("SIM" + (slotId + 1) + " hot plug out when BlockUnReservedForVsim");
            SubscriptionInfoUpdater subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
            SubscriptionInfoUpdater.mNeedUpdate = true;
        }
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            changeIccidForHotplug(slotId, sCardState);
            unRegisterForLoadIccID(slotId);
        } else if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            unRegisterForLoadIccID(slotId);
            changeIccidForHotplug(slotId, sCardState);
        }
        mFh[slotId] = null;
        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NO_SIM;
        if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
            SubscriptionInfoUpdater subscriptionInfoUpdater3 = this.mSubscriptionInfoUpdater;
            if (SubscriptionInfoUpdater.mNeedUpdate) {
                subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0033, code lost:
        if (subscriptionInfoUpdaterUtils.getIccId(r4.mSubscriptionInfoUpdater)[r5].equals(ICCID_STRING_FOR_NO_SIM) != false) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
        logd("SIM" + (r5 + 1) + " hot plug in");
        subscriptionInfoUpdaterUtils.getIccId(r4.mSubscriptionInfoUpdater)[r5] = null;
        r0 = r4.mSubscriptionInfoUpdater;
        com.android.internal.telephony.SubscriptionInfoUpdater.mNeedUpdate = true;
        resetInternalOldIccId(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0015, code lost:
        if (com.android.internal.telephony.SubscriptionInfoUpdater.mNeedUpdate != false) goto L_0x0017;
     */
    public void handleCardInsert(int slotId, UiccCard newCard) {
        String str = null;
        if (this.mChangeIccidDone && !HwVSimUtils.isPlatformRealTripple() && HwVSimUtils.isVSimOn()) {
            SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
        }
        if (subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] != null) {
        }
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            changeIccidForHotplug(slotId, sCardState);
            registerForLoadIccID(slotId);
        } else if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            queryIccId(slotId);
        } else if (!IS_QUICK_BROADCAST_STATUS || this.mCis == null || this.mCis[slotId] == null) {
            changeIccidForHotplug(slotId, sCardState);
            registerForLoadIccID(slotId);
            String[] iccId = subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater);
            if (newCard != null) {
                str = newCard.getIccId();
            }
            iccId[slotId] = str;
            if (!TextUtils.isEmpty(str)) {
                logd("need to update subscription after fligt mode on and off..");
                if (HwVSimUtils.needBlockUnReservedForVsim(slotId)) {
                    subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NO_SIM;
                    logd("the slot " + slotId + " is unreserved for vsim,just set to no_sim");
                }
                setNeedUpdateIfNeed(slotId, subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId]);
                if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                    SubscriptionInfoUpdater subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                    if (SubscriptionInfoUpdater.mNeedUpdate) {
                        subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                    }
                }
            }
        } else {
            changeIccidForHotplug(slotId, sCardState);
            this.mCis[slotId].getICCID(this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
        }
    }

    public void updateIccAvailability(int slotId) {
        if (mUiccController != null) {
            SubscriptionHelper subHelper = SubscriptionHelper.getInstance();
            logd("updateIccAvailability: Enter, slotId " + slotId);
            String str = null;
            if (PROJECT_SIM_NUM <= 1 || subHelper.proceedToHandleIccEvent(slotId)) {
                IccCardStatus.CardState newState = IccCardStatus.CardState.CARDSTATE_ABSENT;
                UiccCard newCard = mUiccController.getUiccCard(slotId);
                if (newCard != null) {
                    newState = newCard.getCardState();
                    if (!IccCardStatusUtils.isCardPresent(newState) && this.isNVSubAvailable) {
                        Rlog.i(LOG_TAG, "updateIccAvailability: Returning NV mode ");
                        return;
                    }
                } else {
                    Rlog.i(LOG_TAG, "updateIccAvailability: newCard is null, slotId " + slotId);
                    if (!HwVSimUtils.isPlatformTwoModems() || HwVSimUtils.isRadioAvailable(slotId)) {
                        Rlog.i(LOG_TAG, "updateIccAvailability: not vsim pending sub");
                        return;
                    }
                }
                IccCardStatus.CardState oldState = sCardState[slotId];
                sCardState[slotId] = newState;
                logd("Slot[" + slotId + "]: New Card State = " + newState + " Old Card State = " + oldState);
                if (!IccCardStatusUtils.isCardPresent(newState)) {
                    handleCardAbsent(slotId, oldState, newState);
                } else if (!IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState)) {
                    handleCardInsert(slotId, newCard);
                } else if (IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState) && TextUtils.isEmpty(subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId])) {
                    logd("SIM" + (slotId + 1) + " need to read iccid again in case of rild restart");
                    subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = null;
                    SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                    SubscriptionInfoUpdater.mNeedUpdate = true;
                    resetInternalOldIccId(slotId);
                    if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                        registerForLoadIccID(slotId);
                    } else if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                        queryIccId(slotId);
                    } else if (!IS_QUICK_BROADCAST_STATUS || this.mCis == null || this.mCis[slotId] == null) {
                        registerForLoadIccID(slotId);
                        String[] iccId = subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater);
                        if (newCard != null) {
                            str = newCard.getIccId();
                        }
                        iccId[slotId] = str;
                        if (subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] != null && subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId].trim().length() == 0) {
                            String[] iccId2 = subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater);
                            iccId2[slotId] = "emptyiccid" + slotId;
                        }
                        if (HwVSimUtils.needBlockUnReservedForVsim(slotId)) {
                            subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NO_SIM;
                            logd("the slot " + slotId + " is unreserved for vsim,just set to no_sim");
                        }
                        setNeedUpdateIfNeed(slotId, subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId]);
                        if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                            SubscriptionInfoUpdater subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                            if (SubscriptionInfoUpdater.mNeedUpdate) {
                                subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                            }
                        }
                    } else {
                        this.mCis[slotId].getICCID(this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                    }
                } else if (IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState) && !subHelper.isApmSIMNotPwdn() && subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] == null) {
                    logd("SIM" + (slotId + 1) + " powered up from APM ");
                    mFh[slotId] = null;
                    SubscriptionInfoUpdater subscriptionInfoUpdater3 = this.mSubscriptionInfoUpdater;
                    SubscriptionInfoUpdater.mNeedUpdate = true;
                    resetInternalOldIccId(slotId);
                    if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                        unRegisterForLoadIccID(slotId);
                        registerForLoadIccID(slotId);
                    } else if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                        queryIccId(slotId);
                    } else if (!IS_QUICK_BROADCAST_STATUS || this.mCis == null || this.mCis[slotId] == null) {
                        unRegisterForLoadIccID(slotId);
                        registerForLoadIccID(slotId);
                    } else {
                        this.mCis[slotId].getICCID(this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                    }
                } else if (IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState) && subHelper.needSubActivationAfterRefresh(slotId)) {
                    logd("SIM" + (slotId + 1) + " refresh happened, need sub activation");
                    if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                        subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                    }
                }
                return;
            }
            logd("updateIccAvailability: radio is OFF/unavailable, ignore ");
            if (!subHelper.isApmSIMNotPwdn()) {
                subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = null;
            }
        }
    }

    private void changeIccidForHotplug(int slotId, IccCardStatus.CardState[] cardState) {
        if (IS_SINGLE_CARD_TRAY) {
            int i = 0;
            if (IccCardStatusUtils.isCardPresent(cardState[slotId]) || !HwCardTrayInfo.getInstance().isCardTrayOut(0)) {
                logd("changeIccidForHotplug  mChangeIccidDone= " + this.mChangeIccidDone);
                if (!this.mChangeIccidDone) {
                    while (true) {
                        int i2 = i;
                        if (i2 < PROJECT_SIM_NUM) {
                            if (i2 != slotId) {
                                subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[i2] = null;
                                SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                                SubscriptionInfoUpdater.mNeedUpdate = true;
                                logd("set iccid null i =  " + i2);
                            }
                            i = i2 + 1;
                        } else {
                            this.mChangeIccidDone = true;
                            return;
                        }
                    }
                }
            } else {
                logd("cardTray out set mChangeIccidDone = " + this.mChangeIccidDone);
                if (this.mChangeIccidDone) {
                    this.mChangeIccidDone = false;
                    for (int i3 = 0; i3 < PROJECT_SIM_NUM; i3++) {
                        if (i3 != slotId && IccCardStatusUtils.isCardPresent(cardState[i3])) {
                            SubscriptionInfoUpdater subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                            SubscriptionInfoUpdater.mNeedUpdate = false;
                            logd("cardTray out set first card mNeedUpdate to false");
                        }
                    }
                }
            }
        } else {
            logd("changeIccidForHotplug don't for two card tray.");
        }
    }

    public void queryIccId(int slotId) {
        logd("queryIccId: slotid=" + slotId);
        if (mFh[slotId] == null) {
            logd("Getting IccFileHandler");
            UiccCardApplication validApp = null;
            UiccCard uiccCard = mUiccController.getUiccCard(slotId);
            if (uiccCard != null) {
                int numApps = uiccCard.getNumApplications();
                int i = 0;
                while (true) {
                    if (i >= numApps) {
                        break;
                    }
                    UiccCardApplication app = uiccCard.getApplicationIndex(i);
                    if (app != null && app.getType() != IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN) {
                        validApp = app;
                        break;
                    }
                    i++;
                }
            }
            if (validApp != null) {
                mFh[slotId] = validApp.getIccFileHandler();
            }
        }
        if (mFh[slotId] != null) {
            String iccId = subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId];
            if (iccId == null) {
                logd("Querying IccId");
                mFh[slotId].loadEFTransparent(12258, this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + printIccid(iccId));
            return;
        }
        sCardState[slotId] = IccCardStatus.CardState.CARDSTATE_ABSENT;
        logd("mFh[" + slotId + "] is null, SIM not inserted");
    }

    public void resetIccid(int slotId) {
        if (slotId < 0 || slotId >= subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater).length) {
            logd("resetIccid: invaild slotid =" + slotId);
            return;
        }
        logd("resetIccid: set iccid is null");
        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = null;
    }

    public void updateSubIdForNV(int slotId) {
        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NV;
        SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
        SubscriptionInfoUpdater.mNeedUpdate = true;
        logd("[updateSubIdForNV]+ Start");
        if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
            logd("[updateSubIdForNV]+ updating");
            subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
            this.isNVSubAvailable = true;
        }
    }

    public void updateSubActivation(int[] simStatus, boolean isStackReadyEvent) {
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            SubscriptionHelper.getInstance().updateNwMode();
        }
        SubscriptionHelper.getInstance().updateSubActivation(subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater), false);
    }

    public void broadcastSubinfoRecordUpdated(String[] iccId, String[] oldIccId, int nNewCardCount, int nSubCount, int nNewSimStatus) {
        logd("broadcastSubinfoRecordUpdated");
        if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
            logd("setSubinfoAutoUpdateDone true");
            HwDsdsController.getInstance().setSubinfoAutoUpdateDone(true);
        }
        int i = 0;
        boolean hasSimRemoved = false;
        for (int i2 = 0; i2 < PROJECT_SIM_NUM; i2++) {
            hasSimRemoved = iccId[i2] != null && iccId[i2].equals(ICCID_STRING_FOR_NO_SIM) && !oldIccId[i2].equals(ICCID_STRING_FOR_NO_SIM);
            if (hasSimRemoved) {
                break;
            }
        }
        if (nNewCardCount != 0) {
            setUpdatedDataToNewCard(iccId, nSubCount, nNewSimStatus);
        } else if (hasSimRemoved) {
            while (true) {
                if (i >= PROJECT_SIM_NUM) {
                    break;
                } else if (subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater)[i] == -3) {
                    logd("No new SIM detected and SIM repositioned");
                    setUpdatedData(3, nSubCount, nNewSimStatus);
                    break;
                } else {
                    i++;
                }
            }
            if (i == PROJECT_SIM_NUM) {
                logd("No new SIM detected and SIM removed");
                setUpdatedData(2, nSubCount, nNewSimStatus);
            }
        } else {
            while (true) {
                if (i >= PROJECT_SIM_NUM) {
                    break;
                } else if (subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater)[i] == -3) {
                    logd("No new SIM detected and SIM repositioned");
                    setUpdatedData(3, nSubCount, nNewSimStatus);
                    break;
                } else {
                    i++;
                }
            }
            if (i == PROJECT_SIM_NUM) {
                logd("[updateSimInfoByIccId] All SIM inserted into the same slot");
                setUpdatedData(4, nSubCount, nNewSimStatus);
            }
        }
    }

    private void setUpdatedDataToNewCard(String[] iccId, int nSubCount, int nNewSimStatus) {
        if (HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isVSimEnabled()) {
            logd("VSim is enabled or VSim caused card status change, skip");
            return;
        }
        logd("New SIM detected");
        if (isNewSimCardInserted(iccId).booleanValue()) {
            setUpdatedData(1, nSubCount, nNewSimStatus);
            return;
        }
        logd("Insert Same Sim");
        setUpdatedData(5, nSubCount, nNewSimStatus);
    }

    public Boolean isNewSimCardInserted(String[] sIccId) {
        boolean result = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sp.edit();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            String sIccIdOld = sp.getString("SP_SUBINFO_SLOT" + i, null);
            String iccIdInSP = ICCID_STRING_FOR_NO_SIM;
            if (sIccIdOld != null && !ICCID_STRING_FOR_NO_SIM.equals(sIccIdOld)) {
                try {
                    iccIdInSP = HwAESCryptoUtil.decrypt(MASTER_PASSWORD, sIccIdOld);
                } catch (Exception ex) {
                    logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
                }
            }
            if (sIccId[i] != null && !sIccId[i].equals(ICCID_STRING_FOR_NO_SIM) && (sIccIdOld == null || !sIccId[i].equals(iccIdInSP))) {
                result = true;
                String iccidEncrypted = ICCID_STRING_FOR_NO_SIM;
                try {
                    iccidEncrypted = HwAESCryptoUtil.encrypt(MASTER_PASSWORD, sIccId[i]);
                } catch (Exception ex2) {
                    logd("HwAESCryptoUtil encrypt excepiton:" + ex2.getMessage());
                }
                editor.putString("SP_SUBINFO_SLOT" + i, iccidEncrypted);
                editor.apply();
            }
        }
        return result;
    }

    private static void setUpdatedData(int detectedType, int subCount, int newSimStatus) {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        logd("[setUpdatedData]+ ");
        setIntentExtra(intent, detectedType, subCount, newSimStatus);
        logd("broadcast intent ACTION_SUBINFO_RECORD_UPDATED : [" + detectedType + ", " + subCount + ", " + newSimStatus + "]");
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
        Intent intent2 = new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        setIntentExtra(intent2, detectedType, subCount, newSimStatus);
        sendBroadcastForRecordUpdate(intent2);
        logd("[setUpdatedData]- ");
    }

    private static void sendBroadcastForRecordUpdate(Intent intent) {
        if (mContext != null) {
            PackageManager pm = mContext.getPackageManager();
            if (pm != null) {
                List<ResolveInfo> Receivers = pm.queryBroadcastReceivers(intent, 0);
                if (Receivers != null && !Receivers.isEmpty()) {
                    int size = Receivers.size();
                    for (int index = 0; index < size; index++) {
                        Intent newIntent = new Intent(intent);
                        String packageName = getPackageName(Receivers.get(index));
                        if (packageName != null) {
                            newIntent.setPackage(packageName);
                            ActivityManagerNative.broadcastStickyIntent(newIntent, "android.permission.READ_PHONE_STATE", -1);
                        }
                    }
                }
            }
        }
    }

    private static String getPackageName(ResolveInfo resolveInfo) {
        if (resolveInfo.activityInfo != null) {
            return resolveInfo.activityInfo.packageName;
        }
        if (resolveInfo.serviceInfo != null) {
            return resolveInfo.serviceInfo.packageName;
        }
        if (resolveInfo.providerInfo != null) {
            return resolveInfo.providerInfo.packageName;
        }
        return null;
    }

    private static void setIntentExtra(Intent intent, int detectedType, int subCount, int newSimStatus) {
        if (detectedType == 1) {
            intent.putExtra("simDetectStatus", 1);
            intent.putExtra("simCount", subCount);
            intent.putExtra("newSIMSlot", newSimStatus);
        } else if (detectedType == 3) {
            intent.putExtra("simDetectStatus", 3);
            intent.putExtra("simCount", subCount);
        } else if (detectedType == 2) {
            intent.putExtra("simDetectStatus", 2);
            intent.putExtra("simCount", subCount);
        } else if (detectedType == 4) {
            intent.putExtra("simDetectStatus", 4);
        } else if (detectedType == 5) {
            intent.putExtra("simDetectStatus", 5);
        }
    }

    private String printIccid(String iccid) {
        if (iccid == null) {
            return "null";
        }
        if (iccid.length() < 6) {
            return "less than 6 digits";
        }
        return iccid.substring(0, 6) + new String(new char[(iccid.length() - 6)]).replace(0, '*');
    }

    private void resetInternalOldIccId(int slotId) {
        logd("resetInternalOldIccId slotId:" + slotId);
        if (slotId >= 0 && slotId < PROJECT_SIM_NUM) {
            this.internalOldIccId[slotId] = null;
        }
    }

    public void setNeedUpdateIfNeed(int slotId, String currentIccId) {
        if (slotId >= 0 && slotId < PROJECT_SIM_NUM) {
            if (currentIccId != null && !currentIccId.equals(this.internalOldIccId[slotId])) {
                logd("internalOldIccId[" + slotId + "]:" + printIccid(this.internalOldIccId[slotId]) + " currentIccId[" + slotId + "]:" + printIccid(currentIccId) + " set mNeedUpdate = true");
                this.mSubscriptionInfoUpdater.setNeedUpdate(true);
            }
            this.internalOldIccId[slotId] = currentIccId;
        }
    }

    private void registerForLoadIccID(int slotId) {
        UiccCardApplication validApp;
        UiccCard uiccCard = mUiccController.getUiccCard(slotId);
        if (uiccCard != null) {
            UiccCardApplication app = uiccCard.getApplication(1);
            if (app != null) {
                validApp = app;
            } else {
                validApp = uiccCard.getApplication(2);
            }
            if (validApp != null) {
                IccRecords newIccRecords = validApp.getIccRecords();
                logd("SIM" + (slotId + 1) + " new : ");
                if (validApp.getState() == IccCardApplicationStatus.AppState.APPSTATE_PIN || validApp.getState() == IccCardApplicationStatus.AppState.APPSTATE_PUK) {
                    queryIccId(slotId);
                    logd("registerForLoadIccID query iccid SIM" + (slotId + 1) + " for pin or puk");
                } else if (newIccRecords != null && (newIccRecords instanceof RuimRecords) && PhoneFactory.getPhone(slotId).getPhoneType() == 1) {
                    logd("registerForLoadIccID query iccid SIM" + (slotId + 1) + " for single mode ruim card");
                    queryIccId(slotId);
                } else if (newIccRecords != null && (mIccRecords[slotId] == null || newIccRecords != mIccRecords[slotId])) {
                    if (mIccRecords[slotId] != null) {
                        mIccRecords[slotId].unRegisterForLoadIccID(this.mHandler);
                    }
                    logd("registerForLoadIccID SIM" + (slotId + 1));
                    mIccRecords[slotId] = newIccRecords;
                    mIccRecords[slotId].registerForLoadIccID(this.mHandler, EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId));
                }
            } else {
                logd("validApp is null");
            }
        }
    }

    private void unRegisterForLoadIccID(int slotId) {
        if (mIccRecords[slotId] != null) {
            logd("unRegisterForLoadIccID SIM" + (slotId + 1));
            mIccRecords[slotId].unRegisterForLoadIccID(this.mHandler);
            mIccRecords[slotId] = null;
        }
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
