package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.cat.AbstractCatService;
import com.android.internal.telephony.cat.AbstractCommandParamsFactory;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CommandParamsFactory;
import com.android.internal.telephony.cat.HwCatCmdMessage;
import com.android.internal.telephony.cat.HwCatServiceReference;
import com.android.internal.telephony.cat.HwCommandParamsFactoryReference;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.telephony.euicc.HwEuiccConnectorEx;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.gsm.HwUsimPhoneBookManager;
import com.android.internal.telephony.gsm.HwUsimPhoneBookManagerEmailAnr;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.HwAdnRecordCache;
import com.android.internal.telephony.uicc.HwAdnRecordCacheEx;
import com.android.internal.telephony.uicc.HwAdnRecordLoaderEx;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.uicc.HwSimChangeDialog;
import com.android.internal.telephony.uicc.HwVoiceMailConstants;
import com.android.internal.telephony.uicc.IHwAdnRecordCacheEx;
import com.android.internal.telephony.uicc.IHwAdnRecordCacheInner;
import com.android.internal.telephony.uicc.IHwAdnRecordLoaderEx;
import com.android.internal.telephony.uicc.IHwAdnRecordLoaderInner;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccSlotEx;
import java.io.FileReader;

public class HwUiccManagerImpl extends DefaultHwUiccManager {
    private static boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(LOG_TAG, 4)));
    private static final String LOG_TAG = "HwUiccManagerImpl";
    private static HwUiccManager instance = new HwUiccManagerImpl();

    public static HwUiccManager getDefault() {
        return instance;
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

    public IccPhoneBookInterfaceManager createHwIccPhoneBookInterfaceManager(Phone phone) {
        return new HwIccPhoneBookInterfaceManager(phone);
    }

    public UsimPhoneBookManager createHwUsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        return new HwUsimPhoneBookManager(fh, cache);
    }

    public UsimPhoneBookManager createHwUsimPhoneBookManagerEmailAnr(IccFileHandler fh, AdnRecordCache cache) {
        return new HwUsimPhoneBookManagerEmailAnr(fh, cache);
    }

    public UiccPhoneBookController createHwUiccPhoneBookController(Phone[] phone) {
        return new HwUiccPhoneBookController(phone);
    }

    public AdnRecordCache createHwAdnRecordCache(IccFileHandler fh) {
        return new HwAdnRecordCache(fh);
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

    public CatCmdMessage createHwCatCmdMessage(Parcel in) {
        return new HwCatCmdMessage(in);
    }

    public AbstractCatService.CatServiceReference createHwCatServiceReference() {
        return new HwCatServiceReference();
    }

    public AbstractCommandParamsFactory.CommandParamsFactoryReference createHwCommandParamsFactoryReference(Object commandParamsFactory) {
        return new HwCommandParamsFactoryReference((CommandParamsFactory) commandParamsFactory);
    }

    public boolean isContainZeros(byte[] data, int length, int totalLength, int curIndex) {
        return HwIccUtils.isContainZeros(data, length, totalLength, curIndex);
    }

    public void updateDataSlot() {
        HwSubscriptionManager.getInstance().updateDataSlot();
    }

    public void updateUserPreferences(boolean setDds) {
        HwSubscriptionManager.getInstance().updateUserPreferences(setDds);
    }

    public void initHwSubscriptionManager(Context c, CommandsInterface[] ci) {
        HwSubscriptionManager.init(c, ci);
    }

    public void registerForSubscriptionActivatedOnSlot(int slotId, Handler h, int what, Object obj) {
        HwSubscriptionManager.getInstance().registerForSubscriptionActivatedOnSlot(slotId, h, what, obj);
    }

    public void unregisterForSubscriptionActivatedOnSlot(int slotId, Handler h) {
        HwSubscriptionManager.getInstance().unregisterForSubscriptionActivatedOnSlot(slotId, h);
    }

    public void registerForSubscriptionDeactivatedOnSlot(int slotId, Handler h, int what, Object obj) {
        HwSubscriptionManager.getInstance().registerForSubscriptionDeactivatedOnSlot(slotId, h, what, obj);
    }

    public void unregisterForSubscriptionDeactivatedOnSlot(int slotId, Handler h) {
        HwSubscriptionManager.getInstance().unregisterForSubscriptionDeactivatedOnSlot(slotId, h);
    }

    public AlertDialog createSimAddDialog(Context mContext, boolean isAdded, int mSlotId) {
        return HwSimChangeDialog.getInstance().getSimAddDialog(mContext, isAdded, mSlotId);
    }

    public void isGoingToshowCountDownTimerDialog(int radioState, int lastRadioState, IccCardStatusExt.CardStateEx oldState, IccCardStatusExt.CardStateEx cardState, Handler handler, int phoneId) {
        HwSimChangeDialog.getInstance().isGoingToshowCountDownTimerDialog(radioState, lastRadioState, oldState, cardState, handler, phoneId);
    }

    public int powerUpRadioIfhasCard(Context c, int mSlotId, int radioState, int mLastRadioState, IccCardStatusExt.CardStateEx mCardState) {
        SubscriptionController subCtrlr;
        if ((HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimInProcess()) || SystemProperties.getBoolean("ro.config.hw_dsdspowerup", false)) {
            if (HWFLOW) {
                Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: vsim in process or dsdspowerup on, just return");
            }
            return radioState;
        } else if (radioState != 0 || mLastRadioState != 1) {
            return radioState;
        } else {
            if (!(Settings.Global.getInt(c.getContentResolver(), "airplane_mode_on", 0) != 0) && mCardState != IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
                if (HWFLOW) {
                    Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: power radio on for slot " + mSlotId);
                }
                if ((TelephonyManager.MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration() || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) && (subCtrlr = SubscriptionController.getInstance()) != null && subCtrlr.getSubState(mSlotId) == 0) {
                    if (HWFLOW) {
                        Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: substate is inactive, just return");
                    }
                    return radioState;
                }
                PhoneFactory.getPhone(mSlotId).setRadioPower(true);
            }
            return 1;
        }
    }

    public FileReader getVoiceMailFileReader() {
        return HwVoiceMailConstants.getVoiceMailFileReader();
    }

    public boolean isHotswapSupported() {
        return SystemProperties.getBoolean("ro.config.hw_hotswap_on", false);
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
        UiccCardExt uiccCardExt = new UiccCardExt();
        uiccCardExt.setUiccCard(uiccSlot.getUiccSlot().getUiccCard());
        HwFullNetworkManager.getInstance().initUiccCard(uiccCardExt, status, index);
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
        return SystemProperties.getBoolean("ro.config.full_network_support", false) && HuaweiTelephonyConfigs.isHisiPlatform() && SystemProperties.getBoolean("persist.hisi.fullnetwork", true) && "normal".equals(SystemProperties.get("ro.runmode", "normal"));
    }

    public void updateSlotIccId(String iccid) {
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().updateHotPlugMainSlotIccId(iccid);
        }
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
        if (atr == null || "".equals(atr)) {
            return HwTelephonyManagerInner.getDefault().blockingGetIccATR(phoneId);
        }
        return atr;
    }

    public EuiccConnector.BaseEuiccCommandCallback getEuiccConnectorCallback(Message message) {
        return HwEuiccConnectorEx.getCallback(message);
    }

    public boolean isNetworkLocked(IccCardApplicationStatusEx.PersoSubStateEx persoSubState) {
        Rlog.i(LOG_TAG, "check whether is network locked state");
        return (persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_SUBSET || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SERVICE_PROVIDER || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_CORPORATE) || (persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_PUK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK || persoSubState == IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_CORPORATE_PUK);
    }

    public IHwAdnRecordCacheEx createHwAdnRecordCacheEx(IHwAdnRecordCacheInner adnRecordCacheInner, IccFileHandler fh) {
        return new HwAdnRecordCacheEx(adnRecordCacheInner, fh);
    }

    public IHwAdnRecordLoaderEx createHwAdnRecordLoaderEx(IHwAdnRecordLoaderInner adnRecordLoaderInner, IccFileHandler fh) {
        return new HwAdnRecordLoaderEx(adnRecordLoaderInner, fh);
    }

    public void broadcastIccStateChangedIntentInternal(String value, String reason, int phoneId) {
        log("broadcastIccStateChangedIntentInternal: phoneId: " + phoneId);
        if (!SubscriptionManager.isValidSlotIndex(phoneId) || (!HwFullNetworkManager.getInstance().isCMCCDsdxEnable() && HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 1)) {
            log("broadcastIccStateChangedIntentInternal: phoneId=" + phoneId + " is invalid or IS_CMCC_4G_DSDX_ENABLE is false, Return!!");
            return;
        }
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SIM_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("phoneName", "Phone");
        intent.putExtra("ss", value);
        intent.putExtra("reason", reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
        VSimUtilsInner.putVSimExtraForIccStateChanged(intent, phoneId, value);
        log("broadcastIccStateChangedIntentInternal intent ACTION_SIM_STATE_CHANGED_INTERNAL value=" + value + " reason=" + reason + " for phoneId=" + phoneId);
        ActivityManager.broadcastStickyIntent(intent, 51, -1);
    }

    private static void log(String message) {
        RlogEx.i(LOG_TAG, "[HwSubscriptionManager]" + message);
    }
}
