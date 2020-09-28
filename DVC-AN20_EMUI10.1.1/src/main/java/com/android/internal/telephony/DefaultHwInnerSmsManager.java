package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.SmsMessage;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.google.android.mms.pdu.EncodedStringValue;
import java.util.HashMap;
import java.util.HashSet;

public class DefaultHwInnerSmsManager implements HwInnerSmsManager {
    private static HwInnerSmsManager sInstance;

    public static HwInnerSmsManager getDefault() {
        if (sInstance == null) {
            sInstance = new DefaultHwInnerSmsManager();
        }
        return sInstance;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public int getMessageRefrenceNumber() {
        return 0;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public byte[] getUserDataHeaderForGsm(int seqNum, int maxNum, int messageReferenceNum) {
        return new byte[0];
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] udh) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] udh, int subscription) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] udh) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] udh, int subscription) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public SmsMessage createFromEfRecord(int index, byte[] data, int subscription) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean handleWapPushExtraMimeType(String mimeType) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public WapPushOverSms createHwWapPushOverSms(Phone phone, SMSDispatcher smsDispatcher) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public WapPushOverSms createHwWapPushOverSms(Context context) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public GsmSMSDispatcher createHwGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public CdmaSMSDispatcher createHwCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandler handler, Context context, Intent intent, String deleteWhere, String[] deleteWhereArgs) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void createSmsInterceptionService(Context context) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void sendGoogleSmsBlockedRecord(Intent intent) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean newSmsShouldBeIntercepted(Context context, Intent intent, InboundSmsHandler handler, String deleteWhere, String[] deleteWhereArgs, boolean isWapPush) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public PackageInfo getPackageInfoByPid(PackageInfo appInfo, PackageManager pm, String[] packageNames, Context context) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public String getAppNameByPid(int pid, Context context) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean checkShouldWriteSmsPackage(String packageName, Context context) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean currentSubIsChinaTelecomSim(int phoneId) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean isLimitNumOfSmsEnabled(boolean isOutgoing) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void updateSmsUsedNum(Context context, boolean isOutgoing) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean isExceedSMSLimit(Context context, boolean isOutgoing) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean isMatchSMSPattern(String address, String policyTag, int phoneId) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public SmsUsageMonitor createHwSmsUsageMonitor(Context context, Phone phone) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void report(Context context, int errorType, String content, int subId) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void addInboxInsertObserver(Context context) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void triggerInboxInsertDoneDetect(Intent intent, boolean isClass0, Handler handler) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void reportSmsReceiveTimeout(Context context, int durationMillis) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void triggerSendSmsOverLoadCheck(SMSDispatcher smsDispatcher) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean isSentSmsFromRejectCall(PendingIntent sentIntent) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean hasSameSmsPdu(byte[] pdu, int phoneId) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public CharSequence getAppLabel(Context context, String appPackage, int userId, SMSDispatcher smsDispatcher) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void scanAndDeleteOlderPartialMessages(InboundSmsTracker tracker, ContentResolver resolver) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public int dealBlacklistSms(Context context, SmsMessage sms, int result) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void filterMyNumber(Context context, boolean groupMmsEnabled, HashSet<String> hashSet, HashMap<Integer, EncodedStringValue[]> hashMap, int subId) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean getTipPremiumFromConfig(int phoneId) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public boolean isIOTVersion() {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void addNumberPlusSign(CdmaSmsAddress addr, byte[] data) {
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public int handleExtendTeleService(int teleService, CdmaSMSDispatcher smsDispatcher, SmsMessageBase smsb) {
        return 0;
    }

    @Override // com.android.internal.telephony.HwInnerSmsManager
    public void dispatchCTAutoRegSmsPdu(Context context, SmsMessageBase smsb, int subId, Handler handler) {
    }
}
