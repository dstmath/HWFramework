package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.SmsMessage;
import android.telephony.SmsMessage.SubmitPdu;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.gsm.SmsMessage.PduParser;
import com.android.internal.util.BitwiseOutputStream;
import com.android.internal.util.BitwiseOutputStream.AccessException;
import java.util.ArrayList;

public interface HwInnerSmsManager {
    boolean allowToSetSmsWritePermission(String str);

    TextEncodingDetails calcTextEncodingDetailsEx(CharSequence charSequence, boolean z);

    boolean checkShouldWriteSmsPackage(String str, Context context);

    SmsMessage createFromEfRecord(int i, byte[] bArr, int i2);

    CdmaSMSDispatcher createHwCdmaSMSDispatcher(Phone phone, SmsStorageMonitor smsStorageMonitor, SmsUsageMonitor smsUsageMonitor, ImsSMSDispatcher imsSMSDispatcher);

    GsmSMSDispatcher createHwGsmSMSDispatcher(Phone phone, SmsStorageMonitor smsStorageMonitor, SmsUsageMonitor smsUsageMonitor, ImsSMSDispatcher imsSMSDispatcher, GsmInboundSmsHandler gsmInboundSmsHandler);

    IccSmsInterfaceManager createHwIccSmsInterfaceManager(Phone phone);

    WapPushOverSms createHwWapPushOverSms(Context context);

    WapPushOverSms createHwWapPushOverSms(Phone phone, SMSDispatcher sMSDispatcher);

    WspTypeDecoder createHwWspTypeDecoder(byte[] bArr);

    void createSmsInterceptionService(Context context);

    boolean currentSubIsChinaTelecomSim(int i);

    void doubleSmsStatusCheck(com.android.internal.telephony.cdma.SmsMessage smsMessage);

    boolean encode7bitMultiSms(UserData userData, byte[] bArr, boolean z);

    void encodeMsgCenterTimeStampCheck(BearerData bearerData, BitwiseOutputStream bitwiseOutputStream) throws AccessException;

    ArrayList<String> fragmentForEmptyText();

    ArrayList<String> fragmentText(String str);

    int getCdmaSub();

    SubmitPdu getDeliverPdu(String str, String str2, String str3, String str4, byte[] bArr);

    SubmitPdu getDeliverPdu(String str, String str2, String str3, String str4, byte[] bArr, int i);

    int getMessageRefrenceNumber();

    byte[] getNewbyte();

    PackageInfo getPackageInfoByPid(PackageInfo packageInfo, PackageManager packageManager, String[] strArr, Context context);

    String getSmscAddr();

    String getSmscAddr(long j);

    SubmitPdu getSubmitPdu(String str, String str2, String str3, String str4, byte[] bArr);

    SubmitPdu getSubmitPdu(String str, String str2, String str3, String str4, byte[] bArr, int i);

    String getUserDataGSM8Bit(PduParser pduParser, int i);

    byte[] getUserDataHeaderForGsm(int i, int i2, int i3);

    boolean handleWapPushExtraMimeType(String str);

    boolean isCdmaVoice();

    boolean newSmsShouldBeIntercepted(Context context, Intent intent, InboundSmsHandler inboundSmsHandler, String str, String[] strArr, boolean z);

    boolean parseGsmSmsSubmit(com.android.internal.telephony.gsm.SmsMessage smsMessage, int i, Object obj, int i2);

    void parseRUIMPdu(com.android.internal.telephony.cdma.SmsMessage smsMessage, byte[] bArr);

    void sendGoogleSmsBlockedRecord(Intent intent);

    boolean setSmscAddr(long j, String str);

    boolean setSmscAddr(String str);

    boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandler inboundSmsHandler, Context context, Intent intent, String str, String[] strArr);

    boolean shouldSetDefaultApplicationForPackage(String str, Context context);

    boolean useCdmaFormatForMoSms();
}
