package com.huawei.android.telephony;

import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;

public final class MSimSmsManagerEx {
    public static int getPreferredSmsSubscription() {
        return SubscriptionManager.getDefaultSmsSubscriptionId();
    }

    public static String getSmscAddrOnSubscription(int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).getSmscAddr();
    }

    public static boolean setSmscAddrOnSubscription(String smsc, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).setSmscAddr(smsc);
    }

    public static void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int subscription) {
        SmsManager.getSmsManagerForSubscriptionId(subscription).sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
    }

    public static ArrayList<String> divideMessage(String text, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).divideMessage(text);
    }

    public static ArrayList<SmsMessage> getAllMessagesFromIcc(int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).getAllMessagesFromIcc();
    }

    public static boolean deleteMessageFromIcc(int messageIndex, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).deleteMessageFromIcc(messageIndex);
    }

    public static boolean copyMessageToIcc(byte[] smsc, byte[] pdu, int status, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).copyMessageToIcc(smsc, pdu, status);
    }

    public static int getSimIdFromIntent(Intent intent, int defaultId) {
        return intent.getIntExtra("subscription", defaultId);
    }

    public static Intent setSimIdToIntent(Intent intent, int subId) {
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, subId);
        return intent;
    }
}
