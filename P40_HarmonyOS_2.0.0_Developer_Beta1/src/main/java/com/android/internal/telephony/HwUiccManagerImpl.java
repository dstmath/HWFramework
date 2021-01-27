package com.android.internal.telephony;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.uicc.HwSimChangeDialog;
import com.android.internal.telephony.uicc.HwVoiceMailConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccSlotEx;
import com.huawei.internal.telephony.vsim.VSimUtilsInnerEx;
import com.huawei.util.LogEx;
import java.io.FileReader;

public class HwUiccManagerImpl extends DefaultHwUiccManager {
    private static boolean HWFLOW = LogEx.getLogHWInfo();
    private static final String LOG_TAG = "HwUiccManagerImpl";
    private static final String SECOND_CARD_HOT_PLUG_STATE_FOR_ESIM = "second_card_hot_plug_state_for_esim";
    private static HwUiccManager instance = new HwUiccManagerImpl();

    public static HwUiccManager getDefault() {
        return instance;
    }

    private static void log(String message) {
        RlogEx.i(LOG_TAG, "[HwSubscriptionManager]" + message);
    }

    public boolean isHwSimPhonebookEnabled() {
        return true;
    }

    public Cursor simContactsQuery(Context context, Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        return HwIccProviderUtils.getDefault(context).query(url, projection, selection, selectionArgs, sort);
    }

    public String simContactsGetType(Context context, Uri url) {
        return HwIccProviderUtils.getDefault(context).getType(url);
    }

    public Uri simContactsInsert(Context context, Uri url, ContentValues initialValues) {
        return HwIccProviderUtils.getDefault(context).insert(url, initialValues);
    }

    public int simContactsDelete(Context context, Uri url, String where, String[] whereArgs) {
        return HwIccProviderUtils.getDefault(context).delete(url, where, whereArgs);
    }

    public int simContactsUpdate(Context context, Uri url, ContentValues values, String where, String[] whereArgs) {
        return HwIccProviderUtils.getDefault(context).update(url, values, where, whereArgs);
    }

    public byte[] buildAdnStringHw(int recordSize, String mAlphaTag, String mNumber) {
        return HwIccUtils.buildAdnStringHw(recordSize, mAlphaTag, mNumber);
    }

    public String prependPlusInLongAdnNumber(String mNumber) {
        return HwIccUtils.prependPlusInLongAdnNumber(mNumber);
    }

    public boolean arrayCompareNullEqualsEmpty(String[] s1, String[] s2) {
        return HwIccUtils.arrayCompareNullEqualsEmpty(s1, s2);
    }

    public String[] updateAnrEmailArrayHelper(String[] dest, String[] src, int fileCount) {
        return HwIccUtils.updateAnrEmailArrayHelper(dest, src, fileCount);
    }

    public int getAlphaTagEncodingLength(String alphaTag) {
        return HwIccUtils.getAlphaTagEncodingLength(alphaTag);
    }

    public String bcdIccidToString(byte[] data, int offset, int length) {
        return HwIccUtils.bcdIccidToString(data, offset, length);
    }

    public String adnStringFieldToStringForSTK(byte[] data, int offset, int length) {
        return HwIccUtils.adnStringFieldToStringForSTK(data, offset, length);
    }

    public boolean isContainZeros(byte[] data, int length, int totalLength, int curIndex) {
        return HwIccUtils.isContainZeros(data, length, totalLength, curIndex);
    }

    public void updateDataSlot() {
        HwSubscriptionManagerUtils.getInstance().updateDataSlot();
    }

    public void updateUserPreferences(boolean setDds) {
        HwSubscriptionManagerUtils.getInstance().updateUserPreferences(setDds);
    }

    public void initHwSubscriptionManager(Context c, CommandsInterfaceEx[] ci) {
        HwSubscriptionManagerUtils.getInstance().init(c, ci);
    }

    public AlertDialog createSimAddDialog(Context mContext, boolean isAdded, int mSlotId) {
        return HwSimChangeDialog.getInstance().getSimAddDialog(mContext, isAdded, mSlotId);
    }

    public void isGoingToshowCountDownTimerDialog(int radioState, int lastRadioState, IccCardStatusExt.CardStateEx oldState, IccCardStatusExt.CardStateEx cardState, Handler handler, int phoneId) {
        HwSimChangeDialog.getInstance().isGoingToshowCountDownTimerDialog(radioState, lastRadioState, oldState, cardState, handler, phoneId);
    }

    public int powerUpRadioIfhasCard(Context context, int mSlotId, int radioState, int mLastRadioState, IccCardStatusExt.CardStateEx cardState) {
        SubscriptionControllerEx subCtrlr;
        if ((HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimInProcess()) || SystemPropertiesEx.getBoolean("ro.config.hw_dsdspowerup", false)) {
            if (HWFLOW) {
                log("powerUpRadioIfhasCard: vsim in process or dsdspowerup on, just return");
            }
            return radioState;
        } else if (radioState != 0 || mLastRadioState != 1) {
            return radioState;
        } else {
            boolean isAirplaneModeOn = false;
            if (context != null) {
                isAirplaneModeOn = Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
            }
            if (!(isAirplaneModeOn || cardState == null || cardState == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT)) {
                if (HWFLOW) {
                    log("powerUpRadioIfhasCard: power radio on for slot " + mSlotId);
                }
                if ((TelephonyManagerEx.MultiSimVariantsExt.DSDS == TelephonyManagerEx.getMultiSimConfiguration() || SystemPropertiesEx.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) && (subCtrlr = SubscriptionControllerEx.getInstance()) != null && subCtrlr.getSubState(mSlotId) == 0) {
                    if (HWFLOW) {
                        log("powerUpRadioIfhasCard: substate is inactive, just return");
                    }
                    return radioState;
                }
                PhoneFactoryExt.getPhone(mSlotId).setRadioPower(true);
            }
            return 1;
        }
    }

    public FileReader getVoiceMailFileReader() {
        return HwVoiceMailConstants.getVoiceMailFileReader();
    }

    public boolean isHotswapSupported() {
        return SystemPropertiesEx.getBoolean("ro.config.hw_hotswap_on", false);
    }

    public void initHwAllInOneController(Context context, CommandsInterfaceEx[] ci) {
        HwFullNetworkManager.getInstance().makeHwFullNetworkManager(context, ci);
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        HwFullNetworkManager.getInstance().registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        HwFullNetworkManager.getInstance().unregisterForIccChanged(h);
    }

    public void initUiccCard(UiccSlotEx uiccSlot, IccCardStatusExt status, Integer index) {
        HwFullNetworkManager.getInstance().initUiccCard(UiccCardExt.getUiccCardFromUiccSlot(uiccSlot), status, index);
    }

    public void updateUiccCard(UiccCardExt uiccCardExt, IccCardStatusExt status, Integer index) {
        HwFullNetworkManager.getInstance().updateUiccCard(uiccCardExt, status, index);
    }

    public void onGetIccStatusDone(Object ar, Integer index) {
        HwFullNetworkManager.getInstance().onGetIccCardStatusDone(ar, index);
    }

    public boolean getSwitchingSlot() {
        return HwFullNetworkManager.getInstance().getWaitingSwitchBalongSlot() || (HuaweiTelephonyConfigs.isHisiPlatform() && !HwFullNetworkManager.getInstance().isSet4GDoneAfterSimInsert());
    }

    public int getUserSwitchSlots() {
        return HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots();
    }

    public boolean isFullNetworkSupported() {
        return SystemPropertiesEx.getBoolean("ro.config.full_network_support", false) && HuaweiTelephonyConfigs.isHisiPlatform() && SystemPropertiesEx.getBoolean("persist.hisi.fullnetwork", true) && "normal".equals(SystemPropertiesEx.get("ro.runmode", "normal"));
    }

    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        HwFullNetworkManager.getInstance().setPreferredNetworkType(networkType, phoneId, response);
    }

    public String cdmaDTMFToString(byte[] data, int offset, int length) {
        return HwIccUtils.cdmaDTMFToString(data, offset, length);
    }

    public boolean isCDMASimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCDMASimCard(slotId);
    }

    public void initHwCarrierConfigCardManager(Context context) {
        HwCarrierConfigCardManager.getDefault(context);
    }

    public boolean get4GSlotInSwitchProgress() {
        return HwFullNetworkManager.getInstance().get4GSlotInSwitchProgress();
    }

    public String cdmaBcdToStringHw(byte[] data, int offset, int length) {
        return HwIccUtils.cdmaBcdToStringHw(data, offset, length);
    }

    public String getAtrHw(int phoneId, String atr) {
        if (atr == null || BuildConfig.FLAVOR.equals(atr)) {
            return HwTelephonyManagerInner.getDefault().blockingGetIccATR(phoneId);
        }
        return atr;
    }

    public boolean isNetworkLocked(IccCardApplicationStatusEx.PersoSubStateEx persoSubState) {
        log("check whether is network locked state");
        return (persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_SUBSET || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SERVICE_PROVIDER || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_CORPORATE) || (persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_PUK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_CORPORATE_PUK);
    }

    public void broadcastIccStateChangedIntentInternal(String value, String reason, int phoneId) {
        log("broadcastIccStateChangedIntentInternal: phoneId: " + phoneId);
        if (!SubscriptionManagerEx.isValidSlotIndex(phoneId) || (!HwFullNetworkManager.getInstance().isCMCCDsdxEnable() && HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 1)) {
            log("broadcastIccStateChangedIntentInternal: phoneId=" + phoneId + " is invalid or IS_CMCC_4G_DSDX_ENABLE is false, Return!!");
            return;
        }
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SIM_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("phoneName", "Phone");
        intent.putExtra("ss", value);
        intent.putExtra("reason", reason);
        SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, phoneId);
        VSimUtilsInnerEx.putVSimExtraForIccStateChanged(intent, phoneId, value);
        log("broadcastIccStateChangedIntentInternal intent ACTION_SIM_STATE_CHANGED_INTERNAL value=" + value + " reason=" + reason + " for phoneId=" + phoneId);
        ActivityManagerEx.broadcastStickyIntent(intent, 51, -1);
    }

    public void putEsimSlot1ExtraForHotPlugStateChanged(Intent intent) {
        if (intent != null) {
            HwCardTrayInfo hwCardTrayInfo = null;
            try {
                hwCardTrayInfo = HwCardTrayInfo.getInstance();
            } catch (RuntimeException e) {
                log("HwCardTrayInfo not init.");
            }
            if (hwCardTrayInfo != null) {
                intent.putExtra(SECOND_CARD_HOT_PLUG_STATE_FOR_ESIM, hwCardTrayInfo.getSlot1HotPlugStateForEsim());
            }
        }
    }
}
