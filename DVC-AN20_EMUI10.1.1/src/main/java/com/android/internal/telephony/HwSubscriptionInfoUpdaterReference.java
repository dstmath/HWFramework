package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.ContentValues;
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
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.IccException;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.uicc.UiccProfileEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HwSubscriptionInfoUpdaterReference extends Handler implements IHwSubscriptionInfoUpdaterEx {
    private static final byte[] C2 = {-89, 82, 3, 85, -88, -104, 57, -10, -103, 108, -88, 122, -38, -12, -55, -2};
    private static final byte[] C3 = {-9, -86, 60, -113, 122, -7, -55, 69, 23, 119, 87, -83, 89, -1, -113, 29};
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final boolean DBG = true;
    private static final int EVENT_EID_READY = 104;
    private static final int EVENT_ICC_CHANGED = 101;
    private static final int EVENT_QUERY_ICCID_DONE = 103;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String ICCID_STRING_FOR_NV = "DUMMY_NV_ID";
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    private static final boolean IS_QUICK_BROADCAST_STATUS = SystemProperties.getBoolean("ro.quick_broadcast_cardstatus", false);
    private static final boolean IS_SINGLE_CARD_TRAY = SystemProperties.getBoolean("persist.radio.single_card_tray", true);
    private static final String LOG_TAG = "HwSubscriptionInfoUpdaterReference";
    private static final String MASTER_PASSWORD = HwFullNetworkManager.getInstance().getMasterPassword();
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final boolean VDBG = false;
    private static Context mContext = null;
    private static IccFileHandler[] mFh;
    private static IccCardStatusExt.CardStateEx[] sCardState;
    private static IccRecordsEx[] sIccRecords;
    private static boolean sIsNeedUpdate = true;
    private static UiccControllerExt sUiccControllerExt = null;
    private String[] internalOldIccId = new String[PROJECT_SIM_NUM];
    private boolean isNVSubAvailable = false;
    private boolean mChangeIccidDone = false;
    private CommandsInterfaceEx[] mCis;
    private ISubscriptionInfoUpdaterInner mSubscriptionInfoUpdater;

    static {
        int i = PROJECT_SIM_NUM;
        sCardState = new IccCardStatusExt.CardStateEx[i];
        mFh = new IccFileHandler[i];
        sIccRecords = new IccRecordsEx[i];
    }

    public HwSubscriptionInfoUpdaterReference(ISubscriptionInfoUpdaterInner subscriptionInfoUpdaterInner, Context context, CommandsInterfaceEx[] ci) {
        this.mSubscriptionInfoUpdater = subscriptionInfoUpdaterInner;
        this.mCis = ci;
        mContext = context;
        SubscriptionHelper.init(context, ci);
        sUiccControllerExt = UiccControllerExt.getInstance();
        sUiccControllerExt.registerForIccChanged(this, (int) EVENT_ICC_CHANGED, (Object) null);
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sCardState[i] = IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
        }
        HwCardTrayInfo.make(ci, mContext);
    }

    public void handleMessage(Message message) {
        String iccId;
        AsyncResult ar = (AsyncResult) message.obj;
        int i = message.what;
        if (i != EVENT_ICC_CHANGED) {
            if (i == EVENT_QUERY_ICCID_DONE) {
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
                            } catch (ClassCastException e) {
                                loge("class cast exception when handle message extend.");
                                return;
                            } catch (Exception e2) {
                                loge("occur other exception when handle message extend.");
                                return;
                            }
                        }
                        this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()] = iccId;
                        if (this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()] != null && this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()].trim().length() == 0) {
                            String[] iccIdHw = this.mSubscriptionInfoUpdater.getIccIdHw();
                            int intValue = slotId.intValue();
                            iccIdHw[intValue] = "emptyiccid" + slotId;
                        }
                        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.needBlockUnReservedForVsim(slotId.intValue())) {
                            this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                            logd("the slot is unreserved for vsim,just set to no_sim");
                        }
                    } else {
                        logd("Null ar");
                        this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                    }
                } else if ((!(ar.exception instanceof CommandException) || !(ar.exception.getCommandError() == CommandException.Error.RADIO_NOT_AVAILABLE || ar.exception.getCommandError() == CommandException.Error.GENERIC_FAILURE)) && !(ar.exception instanceof IccException)) {
                    this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                    logd("Query IccId fail: " + ar.exception);
                } else {
                    logd("Do Nothing.");
                }
                logd("mIccId[" + slotId + "] = " + printIccid(this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()]));
                setNeedUpdateIfNeed(slotId.intValue(), this.mSubscriptionInfoUpdater.getIccIdHw()[slotId.intValue()]);
                updateSubscriptionInfoByIccIdIfNeed(slotId.intValue(), false);
            } else if (i != EVENT_EID_READY) {
                logd("Unknown msg:" + message.what);
                super.handleMessage(message);
            } else {
                logi("EVENT_EID_READY");
                handleEuiccEidReady(ar);
            }
        } else if (ar.result != null) {
            updateIccAvailability(((Integer) ar.result).intValue());
        } else {
            loge("Error: Invalid card index EVENT_ICC_CHANGED ");
        }
    }

    private void handleCardAbsent(int slotId, IccCardStatusExt.CardStateEx oldState, IccCardStatusExt.CardStateEx newState) {
        if (!ICCID_STRING_FOR_NO_SIM.equals(this.mSubscriptionInfoUpdater.getIccIdHw()[slotId])) {
            logd("SIM" + (slotId + 1) + " hot plug out");
            sIsNeedUpdate = true;
            resetInternalOldIccId(slotId);
        }
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.needBlockUnReservedForVsim(slotId) && !IccCardStatusUtils.isCardPresentHw(newState) && IccCardStatusUtils.isCardPresentHw(oldState)) {
            logd("SIM" + (slotId + 1) + " hot plug out when BlockUnReservedForVsim");
            sIsNeedUpdate = true;
        }
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            changeIccidForHotplug(slotId, sCardState);
            unRegisterForLoadIccID(slotId);
        } else if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            unRegisterForLoadIccID(slotId);
            changeIccidForHotplug(slotId, sCardState);
        }
        mFh[slotId] = null;
        this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = ICCID_STRING_FOR_NO_SIM;
        updateSubscriptionInfoByIccIdIfNeed(slotId, false);
    }

    public void handleCardInsert(int slotId, UiccCardExt newCard) {
        CommandsInterfaceEx[] commandsInterfaceExArr;
        String str = null;
        if ((this.mChangeIccidDone && HwTelephonyManager.getDefault().isPlatformSupportVsim() && !HwVSimUtils.isPlatformRealTripple() && HwVSimUtils.isVSimOn() && !sIsNeedUpdate) || (this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] != null && this.mSubscriptionInfoUpdater.getIccIdHw()[slotId].equals(ICCID_STRING_FOR_NO_SIM))) {
            logd("SIM" + (slotId + 1) + " hot plug in");
            this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = null;
            sIsNeedUpdate = true;
            resetInternalOldIccId(slotId);
        }
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            queryIccId(slotId);
        } else if (!IS_QUICK_BROADCAST_STATUS || (commandsInterfaceExArr = this.mCis) == null || commandsInterfaceExArr[slotId] == null) {
            changeIccidForHotplug(slotId, sCardState);
            registerForLoadIccID(slotId);
            String[] iccIdHw = this.mSubscriptionInfoUpdater.getIccIdHw();
            if (newCard != null) {
                str = newCard.getIccId();
            }
            iccIdHw[slotId] = str;
            if (!TextUtils.isEmpty(str)) {
                logd("need to update subscription after fligt mode on and off..");
                if ((HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.needBlockUnReservedForVsim(slotId)) || isEmptyProfileForEuicc(newCard)) {
                    this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = ICCID_STRING_FOR_NO_SIM;
                    logd("the slot " + slotId + " is unreserved for vsim or empty profile(euicc), just set to no_sim");
                }
                setNeedUpdateIfNeed(slotId, this.mSubscriptionInfoUpdater.getIccIdHw()[slotId]);
                updateSubscriptionInfoByIccIdIfNeed(slotId, false);
            }
        } else {
            changeIccidForHotplug(slotId, sCardState);
            this.mCis[slotId].getICCID(obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
        }
    }

    private void updateIccAvailability(int slotId) {
        CommandsInterfaceEx[] commandsInterfaceExArr;
        CommandsInterfaceEx[] commandsInterfaceExArr2;
        if (sUiccControllerExt != null) {
            SubscriptionHelper subHelper = SubscriptionHelper.getInstance();
            logd("updateIccAvailability: Enter, slotId " + slotId);
            String str = null;
            if (PROJECT_SIM_NUM <= 1 || subHelper.proceedToHandleIccEvent(slotId)) {
                IccCardStatusExt.CardStateEx newState = IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
                UiccCardExt newCard = sUiccControllerExt.getUiccCard(slotId);
                if (newCard != null) {
                    newState = newCard.getCardState();
                    if (IccCardStatusUtils.isCardPresentHw(newState) || !this.isNVSubAvailable) {
                        updateIccAvailabilityForEuicc(slotId, newCard);
                    } else {
                        logi("updateIccAvailability: Returning NV mode ");
                        return;
                    }
                } else {
                    logi("updateIccAvailability: newCard is null, slotId " + slotId);
                    if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isPlatformTwoModems() || HwVSimUtils.isRadioAvailable(slotId)) {
                        logi("updateIccAvailability: not vsim pending sub");
                        return;
                    }
                }
                IccCardStatusExt.CardStateEx[] cardStateExArr = sCardState;
                IccCardStatusExt.CardStateEx oldState = cardStateExArr[slotId];
                cardStateExArr[slotId] = newState;
                logd("Slot[" + slotId + "]: New Card State = " + newState + " Old Card State = " + oldState);
                if (!IccCardStatusUtils.isCardPresentHw(newState)) {
                    handleCardAbsent(slotId, oldState, newState);
                } else if (!IccCardStatusUtils.isCardPresentHw(oldState) && IccCardStatusUtils.isCardPresentHw(newState)) {
                    handleCardInsert(slotId, newCard);
                } else if (handleEuiccIccidUpdate(slotId, newCard, oldState, newState)) {
                    logd("SIM" + (slotId + 1) + " handle euicc icc id update");
                } else if (IccCardStatusUtils.isCardPresentHw(oldState) && IccCardStatusUtils.isCardPresentHw(newState) && TextUtils.isEmpty(this.mSubscriptionInfoUpdater.getIccIdHw()[slotId])) {
                    logd("SIM" + (slotId + 1) + " need to read iccid again in case of rild restart");
                    this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = null;
                    sIsNeedUpdate = true;
                    resetInternalOldIccId(slotId);
                    if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                        registerForLoadIccID(slotId);
                    } else if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                        queryIccId(slotId);
                    } else if (!IS_QUICK_BROADCAST_STATUS || (commandsInterfaceExArr2 = this.mCis) == null || commandsInterfaceExArr2[slotId] == null) {
                        registerForLoadIccID(slotId);
                        String[] iccIdHw = this.mSubscriptionInfoUpdater.getIccIdHw();
                        if (!(newCard == null || newCard.getUiccProfile() == null)) {
                            str = newCard.getUiccProfile().getIccId();
                        }
                        iccIdHw[slotId] = str;
                        if (this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] != null && this.mSubscriptionInfoUpdater.getIccIdHw()[slotId].trim().length() == 0) {
                            String[] iccIdHw2 = this.mSubscriptionInfoUpdater.getIccIdHw();
                            iccIdHw2[slotId] = "emptyiccid" + slotId;
                        }
                        if ((HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.needBlockUnReservedForVsim(slotId)) || isEmptyProfileForEuicc(newCard)) {
                            this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = ICCID_STRING_FOR_NO_SIM;
                            logd("the slot " + slotId + " is unreserved for vsim or empty profile(euicc), just set to no_sim");
                        }
                        setNeedUpdateIfNeed(slotId, this.mSubscriptionInfoUpdater.getIccIdHw()[slotId]);
                        updateSubscriptionInfoByIccIdIfNeed(slotId, false);
                    } else {
                        commandsInterfaceExArr2[slotId].getICCID(obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                    }
                } else if (IccCardStatusUtils.isCardPresentHw(oldState) && IccCardStatusUtils.isCardPresentHw(newState) && !subHelper.isApmSIMNotPwdn() && this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] == null) {
                    logd("SIM" + (slotId + 1) + " powered up from APM ");
                    mFh[slotId] = null;
                    sIsNeedUpdate = true;
                    resetInternalOldIccId(slotId);
                    if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                        unRegisterForLoadIccID(slotId);
                        registerForLoadIccID(slotId);
                    } else if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                        queryIccId(slotId);
                    } else if (!IS_QUICK_BROADCAST_STATUS || (commandsInterfaceExArr = this.mCis) == null || commandsInterfaceExArr[slotId] == null) {
                        unRegisterForLoadIccID(slotId);
                        registerForLoadIccID(slotId);
                    } else {
                        commandsInterfaceExArr[slotId].getICCID(obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                    }
                } else if (IccCardStatusUtils.isCardPresentHw(oldState) && IccCardStatusUtils.isCardPresentHw(newState) && subHelper.needSubActivationAfterRefresh(slotId)) {
                    logd("SIM" + (slotId + 1) + " refresh happened, need sub activation");
                    updateSubscriptionInfoByIccIdIfNeed(slotId, true);
                }
            } else {
                logd("updateIccAvailability: radio is OFF/unavailable, ignore ");
                if (!subHelper.isApmSIMNotPwdn()) {
                    this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = null;
                }
            }
        }
    }

    private void changeIccidForHotplug(int slotId, IccCardStatusExt.CardStateEx[] cardState) {
        if (!IS_SINGLE_CARD_TRAY) {
            logd("changeIccidForHotplug don't for two card tray.");
        } else if (IccCardStatusUtils.isCardPresentHw(cardState[slotId]) || !HwCardTrayInfo.getInstance().isCardTrayOut(0)) {
            logd("changeIccidForHotplug  mChangeIccidDone= " + this.mChangeIccidDone);
            if (!this.mChangeIccidDone) {
                for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                    if (i != slotId) {
                        this.mSubscriptionInfoUpdater.getIccIdHw()[i] = null;
                        sIsNeedUpdate = true;
                        logd("set iccid null i =  " + i);
                    }
                }
                this.mChangeIccidDone = true;
            }
        } else {
            logd("cardTray out set mChangeIccidDone = " + this.mChangeIccidDone);
            if (this.mChangeIccidDone) {
                this.mChangeIccidDone = false;
                for (int i2 = 0; i2 < PROJECT_SIM_NUM; i2++) {
                    if (i2 != slotId && IccCardStatusUtils.isCardPresentHw(cardState[i2])) {
                        logd("cardTray out set first card sIsNeedUpdate to false[OUT OF WORK]");
                    }
                }
            }
        }
    }

    private void queryIccId(int slotId) {
        logd("queryIccId: slotid=" + slotId);
        if (mFh[slotId] == null) {
            logd("Getting IccFileHandler");
            UiccCardApplicationEx validApp = null;
            UiccCardExt uiccCard = sUiccControllerExt.getUiccCard(slotId);
            if (uiccCard != null) {
                UiccProfileEx uiccProfileEx = uiccCard.getUiccProfile();
                int numApps = uiccProfileEx.getNumApplications();
                int i = 0;
                while (true) {
                    if (i < numApps) {
                        UiccCardApplicationEx app = uiccProfileEx.getApplicationIndex(i);
                        if (app != null && app.getType() != IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN.ordinal()) {
                            validApp = app;
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
            if (validApp != null) {
                mFh[slotId] = validApp.getIccFileHandler();
            }
        }
        if (IS_MODEM_CAPABILITY_GET_ICCID_AT && isValidCis(slotId)) {
            String iccId = this.mSubscriptionInfoUpdater.getIccIdHw()[slotId];
            if (iccId == null) {
                logd("Querying IccId, IS_MODEM_CAPABILITY_GET_ICCID_AT true");
                this.mCis[slotId].getICCID(obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + printIccid(iccId));
        } else if (mFh[slotId] != null) {
            String iccId2 = this.mSubscriptionInfoUpdater.getIccIdHw()[slotId];
            if (iccId2 == null) {
                logd("Querying IccId");
                mFh[slotId].loadEFTransparent(12258, obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + printIccid(iccId2));
        } else {
            sCardState[slotId] = IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
            logd("mFh[" + slotId + "] is null, SIM not inserted");
        }
    }

    private boolean isValidCis(int slotId) {
        CommandsInterfaceEx[] commandsInterfaceExArr = this.mCis;
        return (commandsInterfaceExArr == null || commandsInterfaceExArr[slotId] == null) ? false : true;
    }

    public void updateSubIdForNV(int slotId) {
        this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = ICCID_STRING_FOR_NV;
        sIsNeedUpdate = true;
        logd("[updateSubIdForNV]+ Start updating");
        updateSubscriptionInfoByIccIdIfNeed(slotId, true);
        this.isNVSubAvailable = true;
    }

    public void updateSubActivation(int slotId) {
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            SubscriptionHelper.getInstance().updateNwMode();
        }
        SubscriptionHelper.getInstance().updateSubActivation(slotId, sCardState);
    }

    public void broadcastSubinfoRecordUpdated(String[] iccId) {
        logd("broadcastSubinfoRecordUpdated");
        String[] oldIccId = SubscriptionHelper.getInstance().getOldIccId();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (oldIccId[i] == null) {
                logd("broadcastSubinfoRecordUpdated: not ready for all cards, return.");
                return;
            }
        }
        int[] insertSimState = SubscriptionHelper.getInstance().getInsertSimState();
        List<SubscriptionInfo> subInfos = SubscriptionControllerEx.getInstance().getActiveSubscriptionInfoList(mContext.getOpPackageName());
        int nSubCount = subInfos == null ? 0 : subInfos.size();
        int nNewSimStatus = SubscriptionHelper.getInstance().getNewSimStatus();
        int nNewCardCount = SubscriptionHelper.getInstance().getNewCardCount();
        logd("broadcastSubinfoRecordUpdated: insertSimState = " + Arrays.toString(insertSimState));
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
            int i3 = 0;
            while (true) {
                if (i3 >= PROJECT_SIM_NUM) {
                    break;
                } else if (insertSimState[i3] == -3) {
                    logd("broadcastSubinfoRecordUpdated: No new SIM detected and SIM repositioned");
                    setUpdatedData(3, nSubCount, nNewSimStatus);
                    break;
                } else {
                    i3++;
                }
            }
            if (i3 == PROJECT_SIM_NUM) {
                logd("broadcastSubinfoRecordUpdated: No new SIM detected and SIM removed");
                setUpdatedData(2, nSubCount, nNewSimStatus);
            }
        } else {
            int i4 = 0;
            while (true) {
                if (i4 >= PROJECT_SIM_NUM) {
                    break;
                } else if (insertSimState[i4] == -3) {
                    logd("broadcastSubinfoRecordUpdated: No new SIM detected and SIM repositioned");
                    setUpdatedData(3, nSubCount, nNewSimStatus);
                    break;
                } else {
                    i4++;
                }
            }
            if (i4 == PROJECT_SIM_NUM) {
                logd("broadcastSubinfoRecordUpdated: All SIM inserted into the same slot");
                setUpdatedData(4, nSubCount, nNewSimStatus);
            }
        }
        SubscriptionHelper.getInstance().resetStateAndIccIdInfos();
        sIsNeedUpdate = false;
    }

    private void setUpdatedDataToNewCard(String[] iccId, int nSubCount, int nNewSimStatus) {
        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimCauseCardReload() && !HwVSimUtils.isVSimEnabled())) {
            logd("broadcastSubinfoRecordUpdated: New SIM detected");
            if (isNewSimCardInserted(iccId).booleanValue()) {
                setUpdatedData(1, nSubCount, nNewSimStatus);
                return;
            }
            logd("broadcastSubinfoRecordUpdated: Insert Same Sim");
            setUpdatedData(5, nSubCount, nNewSimStatus);
            return;
        }
        logd("broadcastSubinfoRecordUpdated: VSim is enabled or VSim caused card status change, skip");
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
                } catch (Exception e) {
                    logd("HwAESCryptoUtil decrypt excepiton");
                }
            }
            if (sIccId[i] != null && !sIccId[i].equals(ICCID_STRING_FOR_NO_SIM) && (sIccIdOld == null || !sIccId[i].equals(iccIdInSP))) {
                result = true;
                String iccidEncrypted = ICCID_STRING_FOR_NO_SIM;
                try {
                    iccidEncrypted = HwAESCryptoUtil.encrypt(MASTER_PASSWORD, sIccId[i]);
                } catch (Exception e2) {
                    logd("HwAESCryptoUtil encrypt excepiton");
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
        PackageManager pm;
        List<ResolveInfo> Receivers;
        Context context = mContext;
        if (!(context == null || (pm = context.getPackageManager()) == null || (Receivers = pm.queryBroadcastReceivers(intent, 0)) == null || Receivers.isEmpty())) {
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
        return iccid.substring(0, 6) + new String(new char[(iccid.length() - 6)]).replace((char) 0, '*');
    }

    private void resetInternalOldIccId(int slotId) {
        logd("resetInternalOldIccId slotId:" + slotId);
        if (slotId >= 0 && slotId < PROJECT_SIM_NUM) {
            this.internalOldIccId[slotId] = null;
        }
    }

    private void setNeedUpdateIfNeed(int slotId, String currentIccId) {
        if (slotId >= 0 && slotId < PROJECT_SIM_NUM) {
            if (currentIccId != null && !currentIccId.equals(this.internalOldIccId[slotId])) {
                logd("internalOldIccId[" + slotId + "]:" + printIccid(this.internalOldIccId[slotId]) + " currentIccId[" + slotId + "]:" + printIccid(currentIccId) + " set sIsNeedUpdate = true");
                sIsNeedUpdate = true;
            }
            this.internalOldIccId[slotId] = currentIccId;
        }
    }

    private void registerForLoadIccID(int slotId) {
        UiccCardApplicationEx validApp;
        UiccCardExt uiccCard = sUiccControllerExt.getUiccCard(slotId);
        if (uiccCard != null) {
            UiccCardApplicationEx app = uiccCard.getApplication(1);
            if (app != null) {
                validApp = app;
            } else {
                validApp = uiccCard.getApplication(2);
            }
            if (validApp != null) {
                IccRecordsEx newIccRecords = validApp.getIccRecords();
                logd("SIM" + (slotId + 1) + " new : ");
                if (validApp.getState() != IccCardApplicationStatusEx.AppStateEx.APPSTATE_PIN) {
                    IccCardApplicationStatusEx.AppStateEx state = validApp.getState();
                    IccCardApplicationStatusEx.AppStateEx appStateEx = IccCardApplicationStatusEx.AppStateEx.APPSTATE_PUK;
                    if (state != IccCardApplicationStatusEx.AppStateEx.APPSTATE_PUK) {
                        if (newIccRecords != null && newIccRecords.isInstanceOfRuim() && PhoneFactory.getPhone(slotId).getPhoneType() == 1) {
                            logd("registerForLoadIccID query iccid SIM" + (slotId + 1) + " for single mode ruim card");
                            queryIccId(slotId);
                            return;
                        } else if (newIccRecords != null) {
                            IccRecordsEx[] iccRecordsExArr = sIccRecords;
                            if (iccRecordsExArr[slotId] == null || newIccRecords != iccRecordsExArr[slotId]) {
                                IccRecordsEx[] iccRecordsExArr2 = sIccRecords;
                                if (iccRecordsExArr2[slotId] != null) {
                                    iccRecordsExArr2[slotId].unRegisterForLoadIccID(this);
                                }
                                logd("registerForLoadIccID SIM" + (slotId + 1));
                                IccRecordsEx[] iccRecordsExArr3 = sIccRecords;
                                iccRecordsExArr3[slotId] = newIccRecords;
                                iccRecordsExArr3[slotId].registerForLoadIccID(this, (int) EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId));
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    }
                }
                queryIccId(slotId);
                logd("registerForLoadIccID query iccid SIM" + (slotId + 1) + " for pin or puk");
                return;
            }
            logd("validApp is null");
        }
    }

    private void unRegisterForLoadIccID(int slotId) {
        if (sIccRecords[slotId] != null) {
            logd("unRegisterForLoadIccID SIM" + (slotId + 1));
            sIccRecords[slotId].unRegisterForLoadIccID(this);
            sIccRecords[slotId] = null;
        }
    }

    public void putExtraValueForEuicc(ContentValues values, String providerName, int state) {
        if (values != null) {
            values.put("carrier_name", providerName);
            values.put("sub_state", Integer.valueOf(state));
        }
    }

    public void recordSimStateBySlotId(int slotId) {
        logd("recordSimStateBySlotId, slotId = " + slotId);
        if (slotId >= 0 && slotId < this.mSubscriptionInfoUpdater.getIccIdHw().length) {
            SubscriptionHelper.getInstance().recordSimState(slotId, this.mSubscriptionInfoUpdater.getIccIdHw()[slotId]);
        }
    }

    public String padTrailingFs(String iccId) {
        return HwIccIdUtil.padTrailingFs(iccId);
    }

    private void updateSubscriptionInfoByIccIdIfNeed(int slotId, boolean ignoreNeedUpdate) {
        String iccId = this.mSubscriptionInfoUpdater.getIccIdHw()[slotId];
        String printableIccId = SubscriptionInfo.givePrintableIccid(iccId);
        logd("updateSubscriptionInfoByIccIdIfNeed, slotId = " + slotId + ", ignoreNeedUpdate = " + ignoreNeedUpdate + ", sIsNeedUpdate = " + sIsNeedUpdate + ", iccid = " + printableIccId);
        if (iccId == null) {
            logd("updateSubscriptionInfoByIccIdIfNeed, hadn't got iccId, return.");
        } else if (sIsNeedUpdate || ignoreNeedUpdate) {
            this.mSubscriptionInfoUpdater.updateSubscriptionInfoByIccIdHw(slotId, true);
        }
    }

    private void updateIccAvailabilityForEuicc(int slotId, UiccCardExt uiccCard) {
        if (uiccCard.getUiccCard() instanceof EuiccCard) {
            logi("updateIccAvailabilityForEuicc: is euicc card.");
            EuiccCard euiccCard = uiccCard.getUiccCard();
            if (TextUtils.isEmpty(euiccCard.getEid())) {
                logi("updateIccAvailabilityForEuicc: eid is empty, register.");
                euiccCard.registerForEidReady(this, (int) EVENT_EID_READY, Integer.valueOf(slotId));
                return;
            }
            logi("updateIccAvailabilityForEuicc: present update");
            List<Integer> cardIds = new ArrayList<>();
            cardIds.add(Integer.valueOf(sUiccControllerExt.convertToPublicCardId(euiccCard.getCardId())));
            this.mSubscriptionInfoUpdater.updateEmbeddedSubscriptionsHw(cardIds);
        }
    }

    private void handleEuiccEidReady(AsyncResult ar) {
        if (ar == null) {
            loge("handleEuiccEidReady, ar is null, return");
            return;
        }
        Integer slotId = (Integer) ar.userObj;
        if (slotId == null || !SubscriptionManagerEx.isValidSlotIndex(slotId.intValue())) {
            loge("handleEuiccEidReady, slotId is error, return");
            return;
        }
        logi("handleEuiccEidReady, slotId is " + slotId);
        UiccCardExt uiccCard = sUiccControllerExt.getUiccCard(slotId.intValue());
        if (uiccCard != null && (uiccCard.getUiccCard() instanceof EuiccCard)) {
            logi("handleEuiccEidReady: is euiccCard, to unregister eid ready.");
            EuiccCard euiccCard = uiccCard.getUiccCard();
            List<Integer> cardIds = new ArrayList<>();
            cardIds.add(Integer.valueOf(sUiccControllerExt.convertToPublicCardId(euiccCard.getCardId())));
            this.mSubscriptionInfoUpdater.updateEmbeddedSubscriptionsHw(cardIds);
            euiccCard.unregisterForEidReady(this);
        }
    }

    private boolean isEmptyProfileForEuicc(UiccCardExt uiccCard) {
        if (uiccCard == null || uiccCard.getUiccProfile() == null || !uiccCard.isEuiccCard() || !uiccCard.getUiccProfile().isEmptyProfile()) {
            return false;
        }
        return true;
    }

    private boolean isSupportEuicc() {
        Context context = mContext;
        return (context == null || context.getPackageManager() == null || !mContext.getPackageManager().hasSystemFeature("android.hardware.telephony.euicc")) ? false : true;
    }

    private boolean handleEuiccIccidUpdate(int slotId, UiccCardExt newCard, IccCardStatusExt.CardStateEx oldState, IccCardStatusExt.CardStateEx newState) {
        if (!isSupportEuicc() || slotId != 1) {
            return false;
        }
        logd("SIM" + (slotId + 1) + " handleEuiccIccidUpdate");
        if (IccCardStatusUtils.isCardPresentHw(oldState) && IccCardStatusUtils.isCardPresentHw(newState)) {
            logd("SIM" + (slotId + 1) + " handleEuiccIccidUpdate, update icc id.");
            String[] iccIdHw = this.mSubscriptionInfoUpdater.getIccIdHw();
            String iccId = newCard != null ? newCard.getIccId() : null;
            iccIdHw[slotId] = iccId;
            if (!TextUtils.isEmpty(iccId)) {
                logd("need to update subscription after fligt mode on and off..");
                sIsNeedUpdate = true;
                if ((HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.needBlockUnReservedForVsim(slotId)) || isEmptyProfileForEuicc(newCard)) {
                    this.mSubscriptionInfoUpdater.getIccIdHw()[slotId] = ICCID_STRING_FOR_NO_SIM;
                    logd("the slot " + slotId + " is unreserved for vsim or empty profile(euicc), just set to no_sim");
                }
                setNeedUpdateIfNeed(slotId, this.mSubscriptionInfoUpdater.getIccIdHw()[slotId]);
                updateSubscriptionInfoByIccIdIfNeed(slotId, false);
                return true;
            }
        }
        return false;
    }

    private static void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private static void logi(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
