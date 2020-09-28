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
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccSlotEx;
import java.io.FileReader;

public interface HwUiccManager {
    public static final String C3 = "0675B95BE3EC0353DDE5C41F4E022588";
    public static final boolean useHwAdnEecode = true;

    String adnStringFieldToStringForSTK(byte[] bArr, int i, int i2);

    boolean arrayCompareNullEqualsEmpty(String[] strArr, String[] strArr2);

    String bcdIccidToString(byte[] bArr, int i, int i2);

    byte[] buildAdnStringHw(int i, String str, String str2);

    String cdmaBcdToStringHw(byte[] bArr, int i, int i2);

    String cdmaDTMFToString(byte[] bArr, int i, int i2);

    AdnRecordCache createHwAdnRecordCache(IccFileHandler iccFileHandler);

    IHwAdnRecordCacheEx createHwAdnRecordCacheEx(IHwAdnRecordCacheInner iHwAdnRecordCacheInner, IccFileHandler iccFileHandler);

    IHwAdnRecordLoaderEx createHwAdnRecordLoaderEx(IHwAdnRecordLoaderInner iHwAdnRecordLoaderInner, IccFileHandler iccFileHandler);

    CatCmdMessage createHwCatCmdMessage(Parcel parcel);

    AbstractCatService.CatServiceReference createHwCatServiceReference();

    AbstractCommandParamsFactory.CommandParamsFactoryReference createHwCommandParamsFactoryReference(Object obj);

    IccPhoneBookInterfaceManager createHwIccPhoneBookInterfaceManager(Phone phone);

    UiccPhoneBookController createHwUiccPhoneBookController(Phone[] phoneArr);

    UsimPhoneBookManager createHwUsimPhoneBookManager(IccFileHandler iccFileHandler, AdnRecordCache adnRecordCache);

    UsimPhoneBookManager createHwUsimPhoneBookManagerEmailAnr(IccFileHandler iccFileHandler, AdnRecordCache adnRecordCache);

    AlertDialog createSimAddDialog(Context context, boolean z, int i);

    boolean get4GSlotInSwitchProgress();

    int getAlphaTagEncodingLength(String str);

    String getAtrHw(int i, String str);

    EuiccConnector.BaseEuiccCommandCallback getEuiccConnectorCallback(Message message);

    boolean getSwitchingSlot();

    int getUserSwitchSlots();

    FileReader getVoiceMailFileReader();

    void initHwCarrierConfigCardManager(Context context);

    void initHwSubscriptionManager(Context context, CommandsInterface[] commandsInterfaceArr);

    boolean isCDMASimCard(int i);

    boolean isContainZeros(byte[] bArr, int i, int i2, int i3);

    boolean isFullNetworkSupported();

    boolean isHotswapSupported();

    boolean isHwSimPhonebookEnabled();

    String prependPlusInLongAdnNumber(String str);

    void registerForIccChanged(Handler handler, int i, Object obj);

    void registerForSubscriptionActivatedOnSlot(int i, Handler handler, int i2, Object obj);

    void registerForSubscriptionDeactivatedOnSlot(int i, Handler handler, int i2, Object obj);

    void setPreferredNetworkType(int i, int i2, Message message);

    int simContactsDelete(Context context, Uri uri, String str, String[] strArr);

    String simContactsGetType(Context context, Uri uri);

    Uri simContactsInsert(Context context, Uri uri, ContentValues contentValues);

    Cursor simContactsQuery(Context context, Uri uri, String[] strArr, String str, String[] strArr2, String str2);

    int simContactsUpdate(Context context, Uri uri, ContentValues contentValues, String str, String[] strArr);

    void unregisterForIccChanged(Handler handler);

    void unregisterForSubscriptionActivatedOnSlot(int i, Handler handler);

    void unregisterForSubscriptionDeactivatedOnSlot(int i, Handler handler);

    String[] updateAnrEmailArrayHelper(String[] strArr, String[] strArr2, int i);

    void updateDataSlot();

    void updateSlotIccId(String str);

    void updateUserPreferences(boolean z);

    default void isGoingToshowCountDownTimerDialog(int radioState, int lastRadioState, IccCardStatusExt.CardStateEx oldState, IccCardStatusExt.CardStateEx cardState, Handler handler, int phoneId) {
    }

    default int powerUpRadioIfhasCard(Context c, int mSlotId, int radioState, int mLastRadioState, IccCardStatusExt.CardStateEx mCardState) {
        return radioState;
    }

    default void initHwAllInOneController(Context context, CommandsInterfaceEx[] ci) {
    }

    default void initUiccCard(UiccSlotEx uiccSlot, IccCardStatusExt status, Integer index) {
    }

    default void updateUiccCard(UiccCardExt uiccCardExt, IccCardStatusExt status, Integer index) {
    }

    default void onGetIccStatusDone(Object ar, Integer index) {
    }

    default boolean isNetworkLocked(IccCardApplicationStatusEx.PersoSubStateEx persoSubState) {
        return false;
    }

    default void broadcastIccStateChangedIntentInternal(String value, String reason, int phoneId) {
    }
}
