package com.android.internal.telephony;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.AbstractSubscriptionController;
import com.android.internal.telephony.AbstractSubscriptionInfoUpdater;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cat.AbstractCatService;
import com.android.internal.telephony.cat.AbstractCommandParamsFactory;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.CommandParamsFactory;
import com.android.internal.telephony.cat.HwCatCmdMessage;
import com.android.internal.telephony.cat.HwCatServiceReference;
import com.android.internal.telephony.cat.HwCommandParamsFactoryReference;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.gsm.HwUsimPhoneBookManager;
import com.android.internal.telephony.gsm.HwUsimPhoneBookManagerEmailAnr;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AbstractIccCardProxy;
import com.android.internal.telephony.uicc.AbstractIccFileHandler;
import com.android.internal.telephony.uicc.AbstractUiccCard;
import com.android.internal.telephony.uicc.AbstractUiccController;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.HwAdnRecordCache;
import com.android.internal.telephony.uicc.HwIccCardProxyReference;
import com.android.internal.telephony.uicc.HwIccFileHandlerReference;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.uicc.HwRuimRecords;
import com.android.internal.telephony.uicc.HwSIMRecords;
import com.android.internal.telephony.uicc.HwSimChangeDialog;
import com.android.internal.telephony.uicc.HwUiccCardReference;
import com.android.internal.telephony.uicc.HwUiccControllerReference;
import com.android.internal.telephony.uicc.HwVoiceMailConstants;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.uicc.UiccSlot;
import com.android.internal.telephony.uicc.VoiceMailConstants;
import com.android.internal.telephony.uicc.VoiceMailConstantsEx;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.io.FileReader;

public class HwUiccManagerImpl implements HwUiccManager {
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

    public SIMRecords createHwSIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        return new HwSIMRecords(app, c, ci);
    }

    public RuimRecords createHwRuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        return new HwRuimRecords(app, c, ci);
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

    public AbstractIccFileHandler.IccFileHandlerReference createHwIccFileHandlerReference(AbstractIccFileHandler fileHandler) {
        return new HwIccFileHandlerReference((IccFileHandler) fileHandler);
    }

    public AbstractUiccCard.UiccCardReference createHwUiccCardReference(AbstractUiccCard uiccCard) {
        return new HwUiccCardReference((UiccCard) uiccCard);
    }

    public AbstractUiccController.UiccControllerReference createHwUiccControllerReference(AbstractUiccController uiccController) {
        return new HwUiccControllerReference((UiccController) uiccController);
    }

    public AbstractIccCardProxy.IccCardProxyReference createHwIccCardProxyReference(AbstractIccCardProxy iccCardProxy) {
        if (iccCardProxy instanceof UiccProfile) {
            return new HwIccCardProxyReference((UiccProfile) iccCardProxy);
        }
        return null;
    }

    public String bcdIccidToString(byte[] data, int offset, int length) {
        return HwIccUtils.bcdIccidToString(data, offset, length);
    }

    public String adnStringFieldToStringForSTK(byte[] data, int offset, int length) {
        return HwIccUtils.adnStringFieldToStringForSTK(data, offset, length);
    }

    public VoiceMailConstants createHwVoiceMailConstants(Context c, int slotId) {
        return new VoiceMailConstantsEx(c, slotId);
    }

    public CatCmdMessage createHwCatCmdMessage(CommandParams cmdParams) {
        return new HwCatCmdMessage(cmdParams);
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

    public void isGoingToshowCountDownTimerDialog(CommandsInterface.RadioState radioState, CommandsInterface.RadioState lastRadioState, IccCardStatus.CardState oldState, IccCardStatus.CardState cardState, Handler handler, int phoneId) {
        HwSimChangeDialog.getInstance().isGoingToshowCountDownTimerDialog(radioState, lastRadioState, oldState, cardState, handler, phoneId);
    }

    public CommandsInterface.RadioState powerUpRadioIfhasCard(Context c, int mSlotId, CommandsInterface.RadioState radioState, CommandsInterface.RadioState mLastRadioState, IccCardStatus.CardState mCardState) {
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            if (HWFLOW) {
                Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: mtk platform, just return");
            }
            return radioState;
        } else if (HwVSimUtils.isVSimInProcess() || SystemProperties.getBoolean("ro.config.hw_dsdspowerup", false)) {
            if (HWFLOW) {
                Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: vsim in process or dsdspowerup on, just return");
            }
            return radioState;
        } else {
            if (radioState == CommandsInterface.RadioState.RADIO_OFF && mLastRadioState == CommandsInterface.RadioState.RADIO_ON) {
                if (!(Settings.Global.getInt(c.getContentResolver(), "airplane_mode_on", 0) != 0) && mCardState != IccCardStatus.CardState.CARDSTATE_ABSENT) {
                    if (HWFLOW) {
                        Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: power radio on for slot " + mSlotId);
                    }
                    if (TelephonyManager.MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration() || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) {
                        if (!HuaweiTelephonyConfigs.isMTKPlatform() || !SubscriptionHelper.mSubSwitching[mSlotId]) {
                            SubscriptionController subCtrlr = SubscriptionController.getInstance();
                            if (subCtrlr != null && subCtrlr.getSubState(subCtrlr.getSubIdUsingPhoneId(mSlotId)) == 0) {
                                if (HWFLOW) {
                                    Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: substate is inactive, just return");
                                }
                                return radioState;
                            }
                        } else {
                            if (HWFLOW) {
                                Rlog.i(LOG_TAG, "powerUpRadioIfhasCard: substate is seting, just return");
                            }
                            return radioState;
                        }
                    }
                    PhoneFactory.getPhone(mSlotId).setRadioPower(true);
                }
                radioState = CommandsInterface.RadioState.RADIO_ON;
            }
            return radioState;
        }
    }

    public FileReader getVoiceMailFileReader() {
        return HwVoiceMailConstants.getVoiceMailFileReader();
    }

    public AbstractSubscriptionController.SubscriptionControllerReference createHwSubscriptionControllerReference(AbstractSubscriptionController subscriptionController) {
        return new HwSubscriptionControllerReference((SubscriptionController) subscriptionController);
    }

    public AbstractSubscriptionInfoUpdater.SubscriptionInfoUpdaterReference createHwSubscriptionInfoUpdaterReference(AbstractSubscriptionInfoUpdater subscriptionInfoUpdater) {
        return new HwSubscriptionInfoUpdaterReference((SubscriptionInfoUpdater) subscriptionInfoUpdater);
    }

    public boolean isUsingHwSubIdDesign() {
        return true;
    }

    public void initHwDsdsController(Context context, CommandsInterface[] ci) {
        HwDsdsController.make(context, ci);
    }

    public boolean uiccHwdsdsNeedSetActiveMode() {
        return HwDsdsController.getInstance().uiccHwdsdsNeedSetActiveMode();
    }

    public void registerDSDSAutoModemSetChanged(Handler h, int what, Object obj) {
        HwDsdsController.getInstance().registerDSDSAutoModemSetChanged(h, what, obj);
    }

    public void unregisterDSDSAutoModemSetChanged(Handler h) {
        HwDsdsController.getInstance().unregisterDSDSAutoModemSetChanged(h);
    }

    public boolean isHotswapSupported() {
        return SystemProperties.getBoolean("ro.config.hw_hotswap_on", false);
    }

    public void initHwAllInOneController(Context context, CommandsInterface[] ci) {
        HwFullNetworkManager.make(context, ci);
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        HwFullNetworkManager.getInstance().registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        HwFullNetworkManager.getInstance().unregisterForIccChanged(h);
    }

    public void initUiccCard(UiccSlot uiccSlot, IccCardStatus status, Integer index) {
        HwFullNetworkManager.getInstance().initUiccCard(uiccSlot.getUiccCard(), status, index);
    }

    public void updateUiccCard(AbstractUiccCard uiccCard, IccCardStatus status, Integer index) {
        HwFullNetworkManager.getInstance().updateUiccCard((UiccCard) uiccCard, status, index);
    }

    public void onGetIccStatusDone(AsyncResult ar, Integer index) {
        HwFullNetworkManager.getInstance().onGetIccCardStatusDone(ar, index);
    }

    public boolean getSwitchingSlot() {
        return HwFullNetworkManager.getInstance().getWaitingSwitchBalongSlot() || (HuaweiTelephonyConfigs.isHisiPlatform() && !HwFullNetworkManager.getInstance().isSet4GDoneAfterSimInsert());
    }

    public int getUserSwitchSlots() {
        return HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots();
    }

    public boolean isFullNetworkSupported() {
        return SystemProperties.getBoolean(HwFullNetworkConfig.PROPERTY_FULL_NETWORK_SUPPORT, false) && HuaweiTelephonyConfigs.isHisiPlatform() && SystemProperties.getBoolean("persist.hisi.fullnetwork", true) && "normal".equals(SystemProperties.get("ro.runmode", "normal"));
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

    public boolean isNetworkLocked(IccCardApplicationStatus.PersoSubState persoSubState) {
        Rlog.i(LOG_TAG, "check whether is network locked state");
        boolean isNetworkLocked = persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK || persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET || persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER || persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_CORPORATE;
        boolean isNetworkLockedPuk = persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK || persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK || persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK || persoSubState == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK;
        if (isNetworkLocked || isNetworkLockedPuk) {
            return true;
        }
        return false;
    }
}
