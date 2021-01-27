package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.SmsMessage;
import com.huawei.internal.telephony.CdmaSMSDispatcherEx;
import com.huawei.internal.telephony.EncodedStringValueEx;
import com.huawei.internal.telephony.InboundSmsHandlerEx;
import com.huawei.internal.telephony.InboundSmsTrackerEx;
import com.huawei.internal.telephony.SMSDispatcherEx;
import com.huawei.internal.telephony.SmsMessageBaseEx;
import com.huawei.internal.telephony.cdma.sms.CdmaSmsAddressEx;
import java.util.HashMap;
import java.util.HashSet;

public interface HwInnerSmsManager {
    default int getMessageRefrenceNumber() {
        return 0;
    }

    default byte[] getUserDataHeaderForGsm(int seqNum, int maxNum, int messageReferenceNum) {
        return new byte[0];
    }

    default SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] udh) {
        return null;
    }

    default SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] udh, int subscription) {
        return null;
    }

    default SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] udh) {
        return null;
    }

    default SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] udh, int subscription) {
        return null;
    }

    default boolean handleWapPushExtraMimeType(String mimeType) {
        return false;
    }

    default boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandlerEx handler, Context context, Intent intent, String deleteWhere, String[] deleteWhereArgs) {
        return false;
    }

    default void createSmsInterceptionService(Context context) {
    }

    default void sendGoogleSmsBlockedRecord(Intent intent) {
    }

    default boolean newSmsShouldBeIntercepted(Context context, Intent intent, InboundSmsHandlerEx handler, String deleteWhere, String[] deleteWhereArgs, boolean isWapPush) {
        return false;
    }

    default PackageInfo getPackageInfoByPid(PackageInfo appInfo, PackageManager pm, String[] packageNames, Context context) {
        return null;
    }

    default String getAppNameByPid(int pid, Context context) {
        return null;
    }

    default boolean checkShouldWriteSmsPackage(String packageName, Context context) {
        return false;
    }

    default boolean currentSubIsChinaTelecomSim(int phoneId) {
        return false;
    }

    default boolean isLimitNumOfSmsEnabled(boolean isOutgoing) {
        return false;
    }

    default void updateSmsUsedNum(Context context, boolean isOutgoing) {
    }

    default boolean isExceedSMSLimit(Context context, boolean isOutgoing) {
        return false;
    }

    default boolean isMatchSMSPattern(String address, String policyTag, int phoneId) {
        return false;
    }

    default void report(Context context, int errorType, String content, int subId) {
    }

    default void addInboxInsertObserver(Context context) {
    }

    default void triggerInboxInsertDoneDetect(Intent intent, boolean isClass0, Handler handler) {
    }

    default void reportSmsReceiveTimeout(Context context, int durationMillis) {
    }

    default void triggerSendSmsOverLoadCheck(SMSDispatcherEx smsDispatcher) {
    }

    default boolean isSentSmsFromRejectCall(PendingIntent sentIntent) {
        return false;
    }

    default boolean hasSameSmsPdu(byte[] pdu, int phoneId) {
        return false;
    }

    default CharSequence getAppLabel(Context context, String appPackage, int userId, SMSDispatcherEx smsDispatcher) {
        return null;
    }

    default void scanAndDeleteOlderPartialMessages(InboundSmsTrackerEx tracker, ContentResolver resolver) {
    }

    default int dealBlacklistSms(Context context, SmsMessage sms, int result) {
        return 0;
    }

    default void filterMyNumber(Context context, boolean groupMmsEnabled, HashSet<String> hashSet, HashMap<Integer, EncodedStringValueEx[]> hashMap, int subId) {
    }

    default boolean getTipPremiumFromConfig(int phoneId) {
        return false;
    }

    default boolean isIOTVersion() {
        return false;
    }

    default void addNumberPlusSign(CdmaSmsAddressEx addr, byte[] data) {
    }

    default int handleExtendTeleService(int teleService, CdmaSMSDispatcherEx smsDispatcher, SmsMessageBaseEx smsb) {
        return 0;
    }

    default void dispatchCTAutoRegSmsPdu(Context context, SmsMessageBaseEx smsb, int subId, Handler handler) {
    }
}
