package com.android.internal.telephony;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import com.android.internal.telephony.cat.AbstractCatService;
import com.android.internal.telephony.cat.AbstractCommandParamsFactory;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IHwAdnRecordCacheEx;
import com.android.internal.telephony.uicc.IHwAdnRecordCacheInner;
import com.android.internal.telephony.uicc.IHwAdnRecordLoaderEx;
import com.android.internal.telephony.uicc.IHwAdnRecordLoaderInner;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.io.FileReader;

public class DefaultHwUiccManager implements HwUiccManager {
    private static HwUiccManager msInstance = new DefaultHwUiccManager();

    public static HwUiccManager getDefault() {
        return msInstance;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isHwSimPhonebookEnabled() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public Cursor simContactsQuery(Context context, Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String simContactsGetType(Context context, Uri url) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public Uri simContactsInsert(Context context, Uri url, ContentValues initialValues) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int simContactsDelete(Context context, Uri url, String where, String[] whereArgs) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int simContactsUpdate(Context context, Uri url, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public IccPhoneBookInterfaceManager createHwIccPhoneBookInterfaceManager(Phone phone) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public UsimPhoneBookManager createHwUsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public UsimPhoneBookManager createHwUsimPhoneBookManagerEmailAnr(IccFileHandler fh, AdnRecordCache cache) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public UiccPhoneBookController createHwUiccPhoneBookController(Phone[] phone) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public AdnRecordCache createHwAdnRecordCache(IccFileHandler fh) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public byte[] buildAdnStringHw(int recordSize, String mAlphaTag, String mNumber) {
        return new byte[0];
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String prependPlusInLongAdnNumber(String mNumber) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean arrayCompareNullEqualsEmpty(String[] s1, String[] s2) {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String[] updateAnrEmailArrayHelper(String[] dest, String[] src, int fileCount) {
        return new String[0];
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int getAlphaTagEncodingLength(String alphaTag) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String bcdIccidToString(byte[] data, int offset, int length) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String adnStringFieldToStringForSTK(byte[] data, int offset, int length) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public CatCmdMessage createHwCatCmdMessage(Parcel in) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public AbstractCatService.CatServiceReference createHwCatServiceReference() {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isContainZeros(byte[] data, int length, int totalLength, int curIndex) {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void initHwSubscriptionManager(Context c, CommandsInterface[] ci) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void registerForSubscriptionActivatedOnSlot(int slotId, Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void unregisterForSubscriptionActivatedOnSlot(int slotId, Handler h) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void registerForSubscriptionDeactivatedOnSlot(int slotId, Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void unregisterForSubscriptionDeactivatedOnSlot(int slotId, Handler h) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public AbstractCommandParamsFactory.CommandParamsFactoryReference createHwCommandParamsFactoryReference(Object commandParamsFactory) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void updateUserPreferences(boolean setDds) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void updateDataSlot() {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public AlertDialog createSimAddDialog(Context mContext, boolean isAdded, int mSlotId) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public FileReader getVoiceMailFileReader() {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isHotswapSupported() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void registerForIccChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void unregisterForIccChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean getSwitchingSlot() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public int getUserSwitchSlots() {
        return 0;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isFullNetworkSupported() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void updateSlotIccId(String iccid) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String cdmaDTMFToString(byte[] data, int offset, int length) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean isCDMASimCard(int slotId) {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public void initHwCarrierConfigCardManager(Context context) {
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public boolean get4GSlotInSwitchProgress() {
        return false;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String getAtrHw(int phoneId, String atr) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public String cdmaBcdToStringHw(byte[] data, int offset, int length) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public EuiccConnector.BaseEuiccCommandCallback getEuiccConnectorCallback(Message message) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public IHwAdnRecordCacheEx createHwAdnRecordCacheEx(IHwAdnRecordCacheInner adnRecordCacheInner, IccFileHandler fh) {
        return null;
    }

    @Override // com.android.internal.telephony.HwUiccManager
    public IHwAdnRecordLoaderEx createHwAdnRecordLoaderEx(IHwAdnRecordLoaderInner adnRecordLoaderInner, IccFileHandler fh) {
        return null;
    }
}
