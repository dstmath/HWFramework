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
import com.android.internal.telephony.AbstractSubscriptionController;
import com.android.internal.telephony.AbstractSubscriptionInfoUpdater;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cat.AbstractCatService;
import com.android.internal.telephony.cat.AbstractCommandParamsFactory;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AbstractIccCardProxy;
import com.android.internal.telephony.uicc.AbstractIccFileHandler;
import com.android.internal.telephony.uicc.AbstractUiccCard;
import com.android.internal.telephony.uicc.AbstractUiccController;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccSlot;
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

    CatCmdMessage createHwCatCmdMessage(Parcel parcel);

    CatCmdMessage createHwCatCmdMessage(CommandParams commandParams);

    AbstractCatService.CatServiceReference createHwCatServiceReference();

    AbstractCommandParamsFactory.CommandParamsFactoryReference createHwCommandParamsFactoryReference(Object obj);

    AbstractIccCardProxy.IccCardProxyReference createHwIccCardProxyReference(AbstractIccCardProxy abstractIccCardProxy);

    AbstractIccFileHandler.IccFileHandlerReference createHwIccFileHandlerReference(AbstractIccFileHandler abstractIccFileHandler);

    IccPhoneBookInterfaceManager createHwIccPhoneBookInterfaceManager(Phone phone);

    RuimRecords createHwRuimRecords(UiccCardApplication uiccCardApplication, Context context, CommandsInterface commandsInterface);

    SIMRecords createHwSIMRecords(UiccCardApplication uiccCardApplication, Context context, CommandsInterface commandsInterface);

    AbstractSubscriptionController.SubscriptionControllerReference createHwSubscriptionControllerReference(AbstractSubscriptionController abstractSubscriptionController);

    AbstractSubscriptionInfoUpdater.SubscriptionInfoUpdaterReference createHwSubscriptionInfoUpdaterReference(AbstractSubscriptionInfoUpdater abstractSubscriptionInfoUpdater);

    AbstractUiccCard.UiccCardReference createHwUiccCardReference(AbstractUiccCard abstractUiccCard);

    AbstractUiccController.UiccControllerReference createHwUiccControllerReference(AbstractUiccController abstractUiccController);

    UiccPhoneBookController createHwUiccPhoneBookController(Phone[] phoneArr);

    UsimPhoneBookManager createHwUsimPhoneBookManager(IccFileHandler iccFileHandler, AdnRecordCache adnRecordCache);

    UsimPhoneBookManager createHwUsimPhoneBookManagerEmailAnr(IccFileHandler iccFileHandler, AdnRecordCache adnRecordCache);

    Object createHwVoiceMailConstants(Context context, int i);

    AlertDialog createSimAddDialog(Context context, boolean z, int i);

    boolean get4GSlotInSwitchProgress();

    int getAlphaTagEncodingLength(String str);

    boolean getSwitchingSlot();

    int getUserSwitchSlots();

    FileReader getVoiceMailFileReader();

    void initHwAllInOneController(Context context, CommandsInterface[] commandsInterfaceArr);

    void initHwCarrierConfigCardManager(Context context);

    void initHwDsdsController(Context context, CommandsInterface[] commandsInterfaceArr);

    void initHwSubscriptionManager(Context context, CommandsInterface[] commandsInterfaceArr);

    void initUiccCard(UiccSlot uiccSlot, IccCardStatus iccCardStatus, Integer num);

    boolean isCDMASimCard(int i);

    boolean isContainZeros(byte[] bArr, int i, int i2, int i3);

    boolean isFullNetworkSupported();

    void isGoingToshowCountDownTimerDialog(CommandsInterface.RadioState radioState, CommandsInterface.RadioState radioState2, IccCardStatus.CardState cardState, IccCardStatus.CardState cardState2, Handler handler, int i);

    boolean isHotswapSupported();

    boolean isHwSimPhonebookEnabled();

    boolean isNetworkLocked(IccCardApplicationStatus.PersoSubState persoSubState);

    boolean isUsingHwSubIdDesign();

    void onGetIccStatusDone(AsyncResult asyncResult, Integer num);

    CommandsInterface.RadioState powerUpRadioIfhasCard(Context context, int i, CommandsInterface.RadioState radioState, CommandsInterface.RadioState radioState2, IccCardStatus.CardState cardState);

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
