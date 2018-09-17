package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.SmsMessage;
import android.telephony.SmsMessage.SubmitPdu;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import java.util.ArrayList;

public interface HwInnerSmsManager {
    TextEncodingDetails calcTextEncodingDetailsEx(CharSequence charSequence, boolean z);

    boolean checkShouldWriteSmsPackage(String str, Context context);

    SmsMessage createFromEfRecord(int i, byte[] bArr, int i2);

    CdmaSMSDispatcher createHwCdmaSMSDispatcher(Phone phone, SmsStorageMonitor smsStorageMonitor, SmsUsageMonitor smsUsageMonitor, ImsSMSDispatcher imsSMSDispatcher);

    GsmSMSDispatcher createHwGsmSMSDispatcher(Phone phone, SmsStorageMonitor smsStorageMonitor, SmsUsageMonitor smsUsageMonitor, ImsSMSDispatcher imsSMSDispatcher, GsmInboundSmsHandler gsmInboundSmsHandler);

    IccSmsInterfaceManager createHwIccSmsInterfaceManager(Phone phone);

    SmsUsageMonitor createHwSmsUsageMonitor(Context context);

    WapPushOverSms createHwWapPushOverSms(Context context);

    WapPushOverSms createHwWapPushOverSms(Phone phone, SMSDispatcher sMSDispatcher);

    WspTypeDecoder createHwWspTypeDecoder(byte[] bArr);

    void createSmsInterceptionService(Context context);

    boolean currentSubIsChinaTelecomSim(int i);

    ArrayList<String> fragmentText(String str);

    SubmitPdu getDeliverPdu(String str, String str2, String str3, String str4, byte[] bArr);

    SubmitPdu getDeliverPdu(String str, String str2, String str3, String str4, byte[] bArr, int i);

    int getMessageRefrenceNumber();

    PackageInfo getPackageInfoByPid(PackageInfo packageInfo, PackageManager packageManager, String[] strArr, Context context);

    SubmitPdu getSubmitPdu(String str, String str2, String str3, String str4, byte[] bArr);

    SubmitPdu getSubmitPdu(String str, String str2, String str3, String str4, byte[] bArr, int i);

    byte[] getUserDataHeaderForGsm(int i, int i2, int i3);

    boolean handleWapPushExtraMimeType(String str);

    boolean isCdmaVoice();

    boolean isExceedSMSLimit(Context context, boolean z);

    boolean isLimitNumOfSmsEnabled(boolean z);

    boolean isMatchSMSPattern(String str, String str2, int i);

    boolean newSmsShouldBeIntercepted(Context context, Intent intent, InboundSmsHandler inboundSmsHandler, String str, String[] strArr, boolean z);

    void sendGoogleSmsBlockedRecord(Intent intent);

    boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandler inboundSmsHandler, Context context, Intent intent, String str, String[] strArr);

    void updateSmsUsedNum(Context context, boolean z);

    boolean useCdmaFormatForMoSms();
}
