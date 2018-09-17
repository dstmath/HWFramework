package android.telephony;

import android.app.PendingIntent;
import com.android.internal.telephony.SmsRawData;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;
import java.util.List;

public class MSimSmsManager {
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
    private static MSimSmsManager mInstance;
    private final int DEFAULT_SUB;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.MSimSmsManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.MSimSmsManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.MSimSmsManager.<clinit>():void");
    }

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
        this.DEFAULT_SUB = STATUS_ON_ICC_FREE;
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
