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
import com.android.internal.telephony.AbstractSubscriptionController.SubscriptionControllerReference;
import com.android.internal.telephony.AbstractSubscriptionInfoUpdater.SubscriptionInfoUpdaterReference;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.cat.AbstractCatService.CatServiceReference;
import com.android.internal.telephony.cat.AbstractCommandParamsFactory.CommandParamsFactoryReference;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AbstractIccCardProxy;
import com.android.internal.telephony.uicc.AbstractIccCardProxy.IccCardProxyReference;
import com.android.internal.telephony.uicc.AbstractIccFileHandler;
import com.android.internal.telephony.uicc.AbstractIccFileHandler.IccFileHandlerReference;
import com.android.internal.telephony.uicc.AbstractUiccCard;
import com.android.internal.telephony.uicc.AbstractUiccCard.UiccCardReference;
import com.android.internal.telephony.uicc.AbstractUiccController;
import com.android.internal.telephony.uicc.AbstractUiccController.UiccControllerReference;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.io.FileReader;

public interface HwUiccManager {
    public static final String C3 = "0675B95BE3EC0353DDE5C41F4E022588";
    public static final boolean useHwAdnEecode = true;

    String adnStringFieldToStringForSTK(byte[] bArr, int i, int i2);

    boolean arrayCompareNullEqualsEmpty(String[] strArr, String[] strArr2);

    String bcdIccidToString(byte[] bArr, int i, int i2);

    byte[] buildAdnStringHw(int i, String str, String str2);

    String cdmaDTMFToString(byte[] bArr, int i, int i2);

    AdnRecordCache createHwAdnRecordCache(IccFileHandler iccFileHandler);

    CatCmdMessage createHwCatCmdMessage(Parcel parcel);

    CatCmdMessage createHwCatCmdMessage(CommandParams commandParams);

    CatServiceReference createHwCatServiceReference();

    CommandParamsFactoryReference createHwCommandParamsFactoryReference(Object obj);

    IccCardProxyReference createHwIccCardProxyReference(AbstractIccCardProxy abstractIccCardProxy);

    IccFileHandlerReference createHwIccFileHandlerReference(AbstractIccFileHandler abstractIccFileHandler);

    IccPhoneBookInterfaceManager createHwIccPhoneBookInterfaceManager(Phone phone);

    RuimRecords createHwRuimRecords(UiccCardApplication uiccCardApplication, Context context, CommandsInterface commandsInterface);

    SIMRecords createHwSIMRecords(UiccCardApplication uiccCardApplication, Context context, CommandsInterface commandsInterface);

    SubscriptionControllerReference createHwSubscriptionControllerReference(AbstractSubscriptionController abstractSubscriptionController);

    SubscriptionInfoUpdaterReference createHwSubscriptionInfoUpdaterReference(AbstractSubscriptionInfoUpdater abstractSubscriptionInfoUpdater);

    UiccCardReference createHwUiccCardReference(AbstractUiccCard abstractUiccCard);

    UiccControllerReference createHwUiccControllerReference(AbstractUiccController abstractUiccController);

    UiccPhoneBookController createHwUiccPhoneBookController(Phone[] phoneArr);

    UiccSmsController createHwUiccSmsController(Phone[] phoneArr);

    UsimPhoneBookManager createHwUsimPhoneBookManager(IccFileHandler iccFileHandler, AdnRecordCache adnRecordCache);

    UsimPhoneBookManager createHwUsimPhoneBookManagerEmailAnr(IccFileHandler iccFileHandler, AdnRecordCache adnRecordCache);

    Object createHwVoiceMailConstants(Context context, int i);

    AlertDialog createSimAddDialog(Context context, boolean z, int i);

    int getAlphaTagEncodingLength(String str);

    boolean getCommrilMode();

    boolean getSwitchTag();

    boolean getSwitchingSlot();

    int getUserSwitchSlots();

    FileReader getVoiceMailFileReader();

    void initHwAllInOneController(Context context, CommandsInterface[] commandsInterfaceArr);

    void initHwDsdsController(Context context, CommandsInterface[] commandsInterfaceArr);

    void initHwFullNetwork(Context context, CommandsInterface[] commandsInterfaceArr);

    void initHwModemBindingPolicyHandler(Context context, AbstractUiccController abstractUiccController, CommandsInterface[] commandsInterfaceArr);

    void initHwModemStackController(Context context, AbstractUiccController abstractUiccController, CommandsInterface[] commandsInterfaceArr);

    void initHwSubscriptionManager(Context context, CommandsInterface[] commandsInterfaceArr);

    void initUiccCard(AbstractUiccCard abstractUiccCard, IccCardStatus iccCardStatus, Integer num);

    boolean isCDMASimCard(int i);

    boolean isContainZeros(byte[] bArr, int i, int i2, int i3);

    boolean isFullNetworkSupported();

    void isGoingToshowCountDownTimerDialog(RadioState radioState, RadioState radioState2, CardState cardState, CardState cardState2, Handler handler, int i);

    boolean isHotswapSupported();

    boolean isHwSimPhonebookEnabled();

    boolean isRestartingRild();

    boolean isUsingHwSubIdDesign();

    void onGetIccStatusDone(AsyncResult asyncResult, Integer num);

    RadioState powerUpRadioIfhasCard(Context context, int i, RadioState radioState, RadioState radioState2, CardState cardState);

    String prependPlusInLongAdnNumber(String str);

    void registerDSDSAutoModemSetChanged(Handler handler, int i, Object obj);

    void registerForIccChanged(Handler handler, int i, Object obj);

    void registerForSubscriptionActivatedOnSlot(int i, Handler handler, int i2, Object obj);

    void registerForSubscriptionDeactivatedOnSlot(int i, Handler handler, int i2, Object obj);

    void setPreferredNetworkType(int i, int i2, Message message);

    int simContactsDelete(Context context, Uri uri, String str, String[] strArr);

    String simContactsGetType(Context context, Uri uri);

    Uri simContactsInsert(Context context, Uri uri, ContentValues contentValues);

    Cursor simContactsQuery(Context context, Uri uri, String[] strArr, String str, String[] strArr2, String str2);

    int simContactsUpdate(Context context, Uri uri, ContentValues contentValues, String str, String[] strArr);

    boolean uiccHwdsdsNeedSetActiveMode();

    void unregisterDSDSAutoModemSetChanged(Handler handler);

    void unregisterForIccChanged(Handler handler);

    void unregisterForSubscriptionActivatedOnSlot(int i, Handler handler);

    void unregisterForSubscriptionDeactivatedOnSlot(int i, Handler handler);

    String[] updateAnrEmailArrayHelper(String[] strArr, String[] strArr2, int i);

    void updateDataSlot();

    void updateSlotIccId(String str);

    void updateUiccCard(AbstractUiccCard abstractUiccCard, IccCardStatus iccCardStatus, Integer num);

    void updateUserPreferences(boolean z);
}
