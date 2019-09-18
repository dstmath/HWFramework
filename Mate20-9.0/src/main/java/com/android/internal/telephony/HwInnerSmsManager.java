package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.SmsMessage;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.google.android.mms.pdu.EncodedStringValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public interface HwInnerSmsManager {
    void addInboxInsertObserver(Context context);

    void addNumberPlusSign(CdmaSmsAddress cdmaSmsAddress, byte[] bArr);

    GsmAlphabet.TextEncodingDetails calcTextEncodingDetailsEx(CharSequence charSequence, boolean z);

    boolean checkShouldWriteSmsPackage(String str, Context context);

    SmsMessage createFromEfRecord(int i, byte[] bArr, int i2);

    CdmaSMSDispatcher createHwCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController);

    GsmSMSDispatcher createHwGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler);

    IccSmsInterfaceManager createHwIccSmsInterfaceManager(Phone phone);

    SmsUsageMonitor createHwSmsUsageMonitor(Context context, Phone phone);

    WapPushOverSms createHwWapPushOverSms(Context context);

    WapPushOverSms createHwWapPushOverSms(Phone phone, SMSDispatcher sMSDispatcher);

    WspTypeDecoder createHwWspTypeDecoder(byte[] bArr);

    void createSmsInterceptionService(Context context);

    boolean currentSubIsChinaTelecomSim(int i);

    int dealBlacklistSms(Context context, SmsMessage smsMessage, int i);

    void filterMyNumber(Context context, boolean z, HashSet<String> hashSet, HashMap<Integer, EncodedStringValue[]> hashMap, int i);

    ArrayList<String> fragmentText(String str);

    CharSequence getAppLabel(Context context, String str, int i, SMSDispatcher sMSDispatcher);

    String getAppNameByPid(int i, Context context);

    SmsMessage.SubmitPdu getDeliverPdu(String str, String str2, String str3, String str4, byte[] bArr);

    SmsMessage.SubmitPdu getDeliverPdu(String str, String str2, String str3, String str4, byte[] bArr, int i);

    int getMessageRefrenceNumber();

    PackageInfo getPackageInfoByPid(PackageInfo packageInfo, PackageManager packageManager, String[] strArr, Context context);

    SmsMessage.SubmitPdu getSubmitPdu(String str, String str2, String str3, String str4, byte[] bArr);

    SmsMessage.SubmitPdu getSubmitPdu(String str, String str2, String str3, String str4, byte[] bArr, int i);

    boolean getTipPremiumFromConfig(int i);

    byte[] getUserDataHeaderForGsm(int i, int i2, int i3);

    int handleExtendTeleService(int i, CdmaSMSDispatcher cdmaSMSDispatcher, com.android.internal.telephony.cdma.SmsMessage smsMessage);

    boolean handleWapPushExtraMimeType(String str);

    boolean hasSameSmsPdu(byte[] bArr);

    boolean isCdmaVoice();

    boolean isExceedSMSLimit(Context context, boolean z);

    boolean isIOTVersion();

    boolean isLimitNumOfSmsEnabled(boolean z);

    boolean isMatchSMSPattern(String str, String str2, int i);

    boolean isSentSmsFromRejectCall(PendingIntent pendingIntent);

    boolean newSmsShouldBeIntercepted(Context context, Intent intent, InboundSmsHandler inboundSmsHandler, String str, String[] strArr, boolean z);

    void report(Context context, int i, String str, int i2);

    void reportSmsReceiveTimeout(Context context, int i);

    void scanAndDeleteOlderPartialMessages(InboundSmsTracker inboundSmsTracker, ContentResolver contentResolver);

    void sendGoogleSmsBlockedRecord(Intent intent);

    boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandler inboundSmsHandler, Context context, Intent intent, String str, String[] strArr);

    void triggerInboxInsertDoneDetect(Intent intent, boolean z, Handler handler);

    void triggerSendSmsOverLoadCheck(SMSDispatcher sMSDispatcher);

    void updateSmsUsedNum(Context context, boolean z);

    boolean useCdmaFormatForMoSms();
}
