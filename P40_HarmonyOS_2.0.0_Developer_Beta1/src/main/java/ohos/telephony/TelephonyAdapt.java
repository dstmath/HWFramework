package ohos.telephony;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.telephony.CallerInfoHW;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.text.SpannableStringBuilder;
import java.util.ArrayList;
import ohos.data.dataability.ContentProviderConverter;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class TelephonyAdapt {
    private static final String EMPTY_STRING = "";
    private static final int INVALID_INDEX = -1;
    private static final int LOG_DOMAIN_TELEPHONY_ADAPT = 218111744;
    private static final int SEND_SMS_SUCESS = -1;
    private static final String SMS_DELIVERED_ACTION = "ohos.action.sms_delivered";
    private static final String SMS_SEND_ACTIOIN = "ohos.action.sms_sent";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218111744, "TelephonyAdapt");
    private static DeliverySmsReceiver sDeliveryReceiver;
    private static IDeliveryShortMessageCallback sIDeliveryShortMessageCallback;
    private static ISendShortMessageCallback sISendShortMessageCallback;
    private static boolean sIsInDeliveringSms = false;
    private static boolean sIsInSendingSms = false;
    private static boolean sRegDeliverySms = false;
    private static boolean sRegSendSms = false;
    private static SendSmsReceiver sSendReceiver;

    public static boolean hasSmsCapability() {
        Resources system = Resources.getSystem();
        if (system != null) {
            return system.getBoolean(17891526);
        }
        return true;
    }

    public static String formatPhoneNumber(String str, String str2) {
        return str == null ? "" : PhoneNumberUtils.formatNumber(str, str2);
    }

    public static String formatPhoneNumber(String str, String str2, String str3) {
        return str == null ? "" : PhoneNumberUtils.formatNumber(str, str2, str3);
    }

    public static String formatPhoneNumberToE164(String str, String str2) {
        return str == null ? "" : PhoneNumberUtils.formatNumberToE164(str, str2);
    }

    public static String getCountryIsoFromDbNumber(String str) {
        CallerInfoHW instance = CallerInfoHW.getInstance();
        return instance != null ? instance.getCountryIsoFromDbNumber(str) : "";
    }

    public static int getCallerIndex(ResultSet resultSet, String str) {
        CallerInfoHW instance = CallerInfoHW.getInstance();
        if (instance == null || resultSet == null) {
            return -1;
        }
        return instance.getCallerIndex(ContentProviderConverter.resultSetToCursor(resultSet), str);
    }

    public static boolean comparePhoneNumbers(String str, String str2, String str3, String str4) {
        CallerInfoHW instance = CallerInfoHW.getInstance();
        if (instance != null) {
            return instance.compareNums(str, str2, str3, str4);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static class SendSmsReceiver extends BroadcastReceiver {
        private SendSmsReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || TelephonyAdapt.sISendShortMessageCallback == null || intent == null) {
                boolean unused = TelephonyAdapt.sIsInSendingSms = false;
                return;
            }
            int transResultCode = transResultCode();
            if (transResultCode != 0) {
                if (TelephonyAdapt.sIsInSendingSms) {
                    TelephonyAdapt.sISendShortMessageCallback.sendShortMessageResult(transResultCode, "", true);
                }
                boolean unused2 = TelephonyAdapt.sIsInSendingSms = false;
                HiLog.error(TelephonyAdapt.TAG, "Send Message to fail!", new Object[0]);
                return;
            }
            boolean booleanExtra = intent.getBooleanExtra("SendNextMsg", false);
            if (TelephonyAdapt.sIsInSendingSms) {
                TelephonyAdapt.sISendShortMessageCallback.sendShortMessageResult(transResultCode, "", booleanExtra);
            }
            if (booleanExtra) {
                boolean unused3 = TelephonyAdapt.sIsInSendingSms = false;
            }
        }

        private int transResultCode() {
            int resultCode = getResultCode();
            if (resultCode == -1) {
                return 0;
            }
            if (resultCode != 4) {
                return (resultCode == 1 || resultCode != 2) ? 1 : 2;
            }
            return 3;
        }
    }

    /* access modifiers changed from: private */
    public static class DeliverySmsReceiver extends BroadcastReceiver {
        private DeliverySmsReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || TelephonyAdapt.sIDeliveryShortMessageCallback == null || intent == null) {
                boolean unused = TelephonyAdapt.sIsInDeliveringSms = false;
                return;
            }
            if (TelephonyAdapt.sIsInDeliveringSms) {
                TelephonyAdapt.sIDeliveryShortMessageCallback.deliveryShortMessageResult(intent.getByteArrayExtra("pdu"));
            }
            boolean unused2 = TelephonyAdapt.sIsInDeliveringSms = false;
        }
    }

    private static Context getAospContext(ohos.app.Context context) {
        Object hostContext = context.getHostContext();
        if (hostContext instanceof Context) {
            return (Context) hostContext;
        }
        HiLog.error(TAG, "get Context::Can not convert zContext to aContext.", new Object[0]);
        return null;
    }

    private static void registerSendSmsReceiver(Context context) {
        sIsInSendingSms = true;
        if (!sRegSendSms) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SMS_SEND_ACTIOIN);
            sSendReceiver = new SendSmsReceiver();
            context.registerReceiver(sSendReceiver, intentFilter, "android.permission.SEND_SMS", null);
            sRegSendSms = true;
        }
    }

    private static void registerDeliverySmsReceiver(Context context) {
        sIsInDeliveringSms = true;
        if (!sRegDeliverySms) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SMS_DELIVERED_ACTION);
            sDeliveryReceiver = new DeliverySmsReceiver();
            context.registerReceiver(sDeliveryReceiver, intentFilter, "android.permission.SEND_SMS", null);
            HiLog.debug(TAG, "registerDeliverySmsReceiver success.", new Object[0]);
            sRegDeliverySms = true;
        }
    }

    public static void sendSmsMessage(int i, String str, String str2, String str3, ohos.app.Context context, ISendShortMessageCallback iSendShortMessageCallback, IDeliveryShortMessageCallback iDeliveryShortMessageCallback) {
        if (context == null) {
            HiLog.error(TAG, "sendSmsMessage Context is null", new Object[0]);
            return;
        }
        Context aospContext = getAospContext(context);
        if (aospContext == null) {
            HiLog.error(TAG, "sendSmsMessage aContext is null", new Object[0]);
            return;
        }
        if (iSendShortMessageCallback != null) {
            registerSendSmsReceiver(aospContext);
        }
        if (iDeliveryShortMessageCallback != null) {
            registerDeliverySmsReceiver(aospContext);
        }
        sISendShortMessageCallback = iSendShortMessageCallback;
        sIDeliveryShortMessageCallback = iDeliveryShortMessageCallback;
        ArrayList<String> divideMessage = SmsManager.getDefault().divideMessage(str3);
        if (divideMessage.size() == 1) {
            sendOneSmsMessage(i, str, str2, str3, aospContext);
        } else {
            sendMutiSmsMessage(i, str, str2, divideMessage, aospContext);
        }
    }

    private static void sendOneSmsMessage(int i, String str, String str2, String str3, Context context) {
        PendingIntent pendingIntent = null;
        PendingIntent broadcast = sISendShortMessageCallback != null ? PendingIntent.getBroadcast(context, 0, new Intent(SMS_SEND_ACTIOIN), 0) : null;
        if (sIDeliveryShortMessageCallback != null) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0);
        }
        SmsManager smsManager = getSmsManager(i);
        if (smsManager == null) {
            HiLog.error(TAG, "sendOneSmsMessage get sms manager is null slot %{public}d.", Integer.valueOf(i));
            return;
        }
        try {
            smsManager.sendTextMessage(str, str2, str3, broadcast, pendingIntent);
        } catch (SecurityException unused) {
            HiLog.error(TAG, "sendOneSmsMessage fail, does not have ohos.permission.SEND_MESSAGES.", new Object[0]);
        }
    }

    private static void sendMutiSmsMessage(int i, String str, String str2, ArrayList<String> arrayList, Context context) {
        int size = arrayList.size();
        ArrayList<PendingIntent> sendPendingIntents = getSendPendingIntents(size, context);
        ArrayList<PendingIntent> deliveryPendingIntents = getDeliveryPendingIntents(size, context);
        SmsManager smsManager = getSmsManager(i);
        if (smsManager == null) {
            HiLog.error(TAG, "sendMutiSmsMessage get sms manager is null slot %{public}d.", Integer.valueOf(i));
            return;
        }
        try {
            smsManager.sendMultipartTextMessage(str, str2, arrayList, sendPendingIntents, deliveryPendingIntents);
        } catch (SecurityException unused) {
            HiLog.error(TAG, "sendMutiSmsMessage fail, does not have ohos.permission.SEND_MESSAGES.", new Object[0]);
        }
    }

    private static ArrayList<PendingIntent> getSendPendingIntents(int i, Context context) {
        ArrayList<PendingIntent> arrayList = new ArrayList<>();
        if (sISendShortMessageCallback == null) {
            return arrayList;
        }
        for (int i2 = 0; i2 < i; i2++) {
            arrayList.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_SEND_ACTIOIN), 0));
        }
        return arrayList;
    }

    private static ArrayList<PendingIntent> getDeliveryPendingIntents(int i, Context context) {
        ArrayList<PendingIntent> arrayList = new ArrayList<>();
        if (sIDeliveryShortMessageCallback == null) {
            return arrayList;
        }
        for (int i2 = 0; i2 < i; i2++) {
            arrayList.add(PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));
        }
        return arrayList;
    }

    public static void sendDataMessage(int i, String str, String str2, short s, byte[] bArr, ohos.app.Context context, ISendShortMessageCallback iSendShortMessageCallback, IDeliveryShortMessageCallback iDeliveryShortMessageCallback) {
        PendingIntent pendingIntent;
        if (context == null) {
            HiLog.error(TAG, "sendDataMessage Context is null", new Object[0]);
            return;
        }
        Context aospContext = getAospContext(context);
        if (aospContext == null) {
            HiLog.error(TAG, "sendSmsMessage aContext is null", new Object[0]);
            return;
        }
        PendingIntent pendingIntent2 = null;
        if (iSendShortMessageCallback != null) {
            registerSendSmsReceiver(aospContext);
            pendingIntent = PendingIntent.getBroadcast(aospContext, 0, new Intent(SMS_SEND_ACTIOIN), 0);
        } else {
            pendingIntent = null;
        }
        if (iDeliveryShortMessageCallback != null) {
            registerDeliverySmsReceiver(aospContext);
            pendingIntent2 = PendingIntent.getBroadcast(aospContext, 0, new Intent(SMS_DELIVERED_ACTION), 0);
        }
        sISendShortMessageCallback = iSendShortMessageCallback;
        sIDeliveryShortMessageCallback = iDeliveryShortMessageCallback;
        SmsManager smsManager = getSmsManager(i);
        if (smsManager == null) {
            HiLog.error(TAG, "sendMutiSmsMessage get sms manager is null slot %{public}d.", Integer.valueOf(i));
        } else {
            smsManager.sendDataMessage(str, str2, s, bArr, pendingIntent, pendingIntent2);
        }
    }

    private static SmsManager getSmsManager(int i) {
        int[] subId = SubscriptionManager.getSubId(i);
        if (subId == null) {
            return null;
        }
        return SmsManager.getSmsManagerForSubscriptionId(subId[0]);
    }

    public static String formartPhoneNumber(String str, String str2) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        new PhoneNumberFormattingTextWatcher(str2).afterTextChanged(spannableStringBuilder);
        return spannableStringBuilder.toString();
    }
}
