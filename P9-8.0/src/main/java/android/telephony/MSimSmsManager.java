package android.telephony;

import android.app.PendingIntent;
import com.android.internal.telephony.SmsRawData;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;
import java.util.List;

public class MSimSmsManager {
    private static final int DEFAULT_SUB = 0;
    public static final int RESULT_ERROR_FDN_CHECK_FAILURE = 6;
    public static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    public static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    public static final int RESULT_ERROR_NO_SERVICE = 4;
    public static final int RESULT_ERROR_NULL_PDU = 3;
    public static final int RESULT_ERROR_RADIO_OFF = 2;
    public static final int STATUS_ON_ICC_FREE = 0;
    public static final int STATUS_ON_ICC_READ = 1;
    public static final int STATUS_ON_ICC_SENT = 5;
    public static final int STATUS_ON_ICC_UNREAD = 3;
    public static final int STATUS_ON_ICC_UNSENT = 7;
    private static MSimSmsManager mInstance = new MSimSmsManager();

    public void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, int subscription) {
        SmsManager.getSmsManagerForSubscriptionId(subscription).sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
    }

    public ArrayList<String> divideMessage(String text) {
        return SmsManager.getDefault().divideMessage(text);
    }

    public ArrayList<String> divideMessage(String text, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).divideMessage(text);
    }

    public void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int subscription) {
        SmsManager.getSmsManagerForSubscriptionId(subscription).sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
    }

    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, int subscription) {
        SmsManager.getSmsManagerForSubscriptionId(subscription).sendDataMessage(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent);
    }

    public static MSimSmsManager getDefault() {
        return mInstance;
    }

    private MSimSmsManager() {
    }

    public boolean copyMessageToIcc(byte[] smsc, byte[] pdu, int status, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).copyMessageToIcc(smsc, pdu, status);
    }

    public boolean deleteMessageFromIcc(int messageIndex, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).deleteMessageFromIcc(messageIndex);
    }

    public boolean updateMessageOnIcc(int messageIndex, int newStatus, byte[] pdu, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).updateMessageOnIcc(messageIndex, newStatus, pdu);
    }

    public ArrayList<SmsMessage> getAllMessagesFromIcc(int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).getAllMessagesFromIcc();
    }

    public boolean enableCellBroadcast(int messageIdentifier, int subscription) {
        return false;
    }

    public boolean disableCellBroadcast(int messageIdentifier, int subscription) {
        return false;
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int subscription) {
        return false;
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int subscription) {
        return false;
    }

    private static ArrayList<SmsMessage> createMessageListFromRawRecords(List<SmsRawData> list) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getPreferredSmsSubscription() {
        return SubscriptionManager.getDefaultSmsSubscriptionId();
    }

    public String getSmscAddrOnSubscription(int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).getSmscAddr();
    }

    public boolean setSmscAddrOnSubscription(String smsc, int subscription) {
        return SmsManager.getSmsManagerForSubscriptionId(subscription).setSmscAddr(smsc);
    }
}
