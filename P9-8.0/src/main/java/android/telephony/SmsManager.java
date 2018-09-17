package android.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.BaseBundle;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Log;
import com.android.internal.telephony.IMms;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.ISms.Stub;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsRawData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class SmsManager {
    public static final int CDMA_SMS_RECORD_LENGTH = 255;
    public static final int CELL_BROADCAST_RAN_TYPE_CDMA = 1;
    public static final int CELL_BROADCAST_RAN_TYPE_GSM = 0;
    public static final String CONFIG_GROUP_CHAT_DEFAULT_TO_MMS = "groupChatDefaultsToMMS";
    public static final String CONFIG_SMS_USES_SIMPLE_CHARACTERS_ONLY = "smsUsesSimpleCharactersOnly";
    public static final String CONFIG_USE_CUSTOM_USER_AGENT = "useCustomUserAgent";
    private static final int DEFAULT_SUBSCRIPTION_ID = -1002;
    private static String DIALOG_TYPE_KEY = "dialog_type";
    public static final String EXTRA_MMS_DATA = "android.telephony.extra.MMS_DATA";
    public static final String EXTRA_MMS_HTTP_STATUS = "android.telephony.extra.MMS_HTTP_STATUS";
    public static final String MESSAGE_STATUS_READ = "read";
    public static final String MESSAGE_STATUS_SEEN = "seen";
    public static final String MMS_CONFIG_ALIAS_ENABLED = "aliasEnabled";
    public static final String MMS_CONFIG_ALIAS_MAX_CHARS = "aliasMaxChars";
    public static final String MMS_CONFIG_ALIAS_MIN_CHARS = "aliasMinChars";
    public static final String MMS_CONFIG_ALLOW_ATTACH_AUDIO = "allowAttachAudio";
    public static final String MMS_CONFIG_APPEND_TRANSACTION_ID = "enabledTransID";
    public static final String MMS_CONFIG_CLOSE_CONNECTION = "mmsCloseConnection";
    public static final String MMS_CONFIG_EMAIL_GATEWAY_NUMBER = "emailGatewayNumber";
    public static final String MMS_CONFIG_GROUP_MMS_ENABLED = "enableGroupMms";
    public static final String MMS_CONFIG_HTTP_PARAMS = "httpParams";
    public static final String MMS_CONFIG_HTTP_SOCKET_TIMEOUT = "httpSocketTimeout";
    public static final String MMS_CONFIG_MAX_IMAGE_HEIGHT = "maxImageHeight";
    public static final String MMS_CONFIG_MAX_IMAGE_WIDTH = "maxImageWidth";
    public static final String MMS_CONFIG_MAX_MESSAGE_SIZE = "maxMessageSize";
    public static final String MMS_CONFIG_MESSAGE_TEXT_MAX_SIZE = "maxMessageTextSize";
    public static final String MMS_CONFIG_MMS_DELIVERY_REPORT_ENABLED = "enableMMSDeliveryReports";
    public static final String MMS_CONFIG_MMS_ENABLED = "enabledMMS";
    public static final String MMS_CONFIG_MMS_READ_REPORT_ENABLED = "enableMMSReadReports";
    public static final String MMS_CONFIG_MULTIPART_SMS_ENABLED = "enableMultipartSMS";
    public static final String MMS_CONFIG_NAI_SUFFIX = "naiSuffix";
    public static final String MMS_CONFIG_NOTIFY_WAP_MMSC_ENABLED = "enabledNotifyWapMMSC";
    public static final String MMS_CONFIG_RECIPIENT_LIMIT = "recipientLimit";
    public static final String MMS_CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES = "sendMultipartSmsAsSeparateMessages";
    public static final String MMS_CONFIG_SHOW_CELL_BROADCAST_APP_LINKS = "config_cellBroadcastAppLinks";
    public static final String MMS_CONFIG_SMS_DELIVERY_REPORT_ENABLED = "enableSMSDeliveryReports";
    public static final String MMS_CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD = "smsToMmsTextLengthThreshold";
    public static final String MMS_CONFIG_SMS_TO_MMS_TEXT_THRESHOLD = "smsToMmsTextThreshold";
    public static final String MMS_CONFIG_SUBJECT_MAX_LENGTH = "maxSubjectLength";
    public static final String MMS_CONFIG_SUPPORT_HTTP_CHARSET_HEADER = "supportHttpCharsetHeader";
    public static final String MMS_CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION = "supportMmsContentDisposition";
    public static final String MMS_CONFIG_UA_PROF_TAG_NAME = "uaProfTagName";
    public static final String MMS_CONFIG_UA_PROF_URL = "uaProfUrl";
    public static final String MMS_CONFIG_USER_AGENT = "userAgent";
    public static final int MMS_ERROR_CONFIGURATION_ERROR = 7;
    public static final int MMS_ERROR_HTTP_FAILURE = 4;
    public static final int MMS_ERROR_INVALID_APN = 2;
    public static final int MMS_ERROR_IO_ERROR = 5;
    public static final int MMS_ERROR_NO_DATA_NETWORK = 8;
    public static final int MMS_ERROR_RETRY = 6;
    public static final int MMS_ERROR_UNABLE_CONNECT_MMS = 3;
    public static final int MMS_ERROR_UNSPECIFIED = 1;
    private static final String PHONE_PACKAGE_NAME = "com.android.phone";
    public static final int RESULT_ERROR_FDN_CHECK_FAILURE = 6;
    public static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    public static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    public static final int RESULT_ERROR_NO_SERVICE = 4;
    public static final int RESULT_ERROR_NULL_PDU = 3;
    public static final int RESULT_ERROR_RADIO_OFF = 2;
    private static final int SMS_PICK = 2;
    public static final int SMS_RECORD_LENGTH = 176;
    public static final int SMS_TYPE_INCOMING = 0;
    public static final int SMS_TYPE_OUTGOING = 1;
    public static final int STATUS_ON_ICC_FREE = 0;
    public static final int STATUS_ON_ICC_READ = 1;
    public static final int STATUS_ON_ICC_SENT = 5;
    public static final int STATUS_ON_ICC_UNREAD = 3;
    public static final int STATUS_ON_ICC_UNSENT = 7;
    private static final String TAG = "SmsManager";
    private static final SmsManager sInstance = new SmsManager(DEFAULT_SUBSCRIPTION_ID);
    private static final Object sLockObject = new Object();
    private static final Map<Integer, SmsManager> sSubInstances = new ArrayMap();
    private int mSubId;

    public void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, true);
    }

    private void sendTextMessageInternal(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        }
        if (TextUtils.isEmpty(text)) {
            Log.d(TAG, "send empty text");
        }
        try {
            getISmsServiceOrThrow().sendTextForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage);
        } catch (RemoteException e) {
        }
    }

    public void sendTextMessageWithoutPersisting(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, false);
    }

    public void sendTextMessageWithSelfPermissions(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        } else {
            try {
                getISmsServiceOrThrow().sendTextForSubscriberWithSelfPermissions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage);
            } catch (RemoteException e) {
            }
        }
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        if (format.equals("3gpp") || (format.equals("3gpp2") ^ 1) == 0) {
            try {
                ISms iccISms = Stub.asInterface(ServiceManager.getService("isms"));
                if (iccISms != null) {
                    iccISms.injectSmsPduForSubscriber(getSubscriptionId(), pdu, format, receivedIntent);
                    return;
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid pdu format. format must be either 3gpp or 3gpp2");
    }

    public ArrayList<String> divideMessage(String text) {
        if (text == null || text.length() == 0) {
            return HwFrameworkFactory.getHwBaseInnerSmsManager().fragmentForEmptyText();
        }
        return SmsMessage.fragmentText(text);
    }

    public void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, true);
    }

    private void sendMultipartTextMessageInternal(String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessage) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (parts == null || parts.size() < 1) {
            throw new IllegalArgumentException("Invalid message body");
        } else if (parts.size() > 1) {
            try {
                getISmsServiceOrThrow().sendMultipartTextForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessage);
            } catch (RemoteException e) {
            }
        } else {
            PendingIntent pendingIntent = null;
            PendingIntent pendingIntent2 = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                pendingIntent = (PendingIntent) sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(0);
            }
            sendTextMessage(destinationAddress, scAddress, (String) parts.get(0), pendingIntent, pendingIntent2);
        }
    }

    public void sendMultipartTextMessageWithoutPersisting(String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, false);
    }

    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        } else {
            try {
                getISmsServiceOrThrow().sendDataForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, data, sentIntent, deliveryIntent);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendDataMessageWithSelfPermissions(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        } else {
            try {
                getISmsServiceOrThrow().sendDataForSubscriberWithSelfPermissions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, data, sentIntent, deliveryIntent);
            } catch (RemoteException e) {
            }
        }
    }

    public static SmsManager getDefault() {
        return sInstance;
    }

    public static SmsManager getSmsManagerForSubscriptionId(int subId) {
        SmsManager smsManager;
        synchronized (sLockObject) {
            smsManager = (SmsManager) sSubInstances.get(Integer.valueOf(subId));
            if (smsManager == null) {
                smsManager = new SmsManager(subId);
                sSubInstances.put(Integer.valueOf(subId), smsManager);
            }
        }
        return smsManager;
    }

    private SmsManager(int subId) {
        this.mSubId = subId;
    }

    public int getSubscriptionId() {
        int subId = this.mSubId == DEFAULT_SUBSCRIPTION_ID ? getDefaultSmsSubscriptionId() : this.mSubId;
        boolean isSmsSimPickActivityNeeded = false;
        Context context = ActivityThread.currentApplication().getApplicationContext();
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                isSmsSimPickActivityNeeded = iccISms.isSmsSimPickActivityNeeded(subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in getSubscriptionId");
        }
        if (isSmsSimPickActivityNeeded) {
            Log.d(TAG, "getSubscriptionId isSmsSimPickActivityNeeded is true");
            Intent intent = new Intent();
            intent.setClassName(HwPCUtils.PKG_SETTINGS, "com.android.settings.sim.SimDialogActivity");
            intent.addFlags(268435456);
            intent.putExtra(DIALOG_TYPE_KEY, 2);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                Log.e(TAG, "Unable to launch Settings application.");
            }
        }
        return subId;
    }

    private static ISms getISmsServiceOrThrow() {
        ISms iccISms = getISmsService();
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    private static ISms getISmsService() {
        return Stub.asInterface(ServiceManager.getService("isms"));
    }

    public boolean copyMessageToIcc(byte[] smsc, byte[] pdu, int status) {
        if (pdu == null) {
            throw new IllegalArgumentException("pdu is NULL");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.copyMessageToIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), status, pdu, smsc);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean deleteMessageFromIcc(int messageIndex) {
        byte[] pdu = HwFrameworkFactory.getHwBaseInnerSmsManager().getNewbyte();
        Arrays.fill(pdu, (byte) -1);
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.updateMessageOnIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), messageIndex, 0, pdu);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean updateMessageOnIcc(int messageIndex, int newStatus, byte[] pdu) {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.updateMessageOnIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), messageIndex, newStatus, pdu);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public ArrayList<SmsMessage> getAllMessagesFromIcc() {
        List<SmsRawData> records = null;
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                records = iccISms.getAllMessagesFromIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
            }
        } catch (RemoteException e) {
        }
        return createMessageListFromRawRecords(records);
    }

    public boolean enableCellBroadcast(int messageIdentifier, int ranType) {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.enableCellBroadcastForSubscriber(getSubscriptionId(), messageIdentifier, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disableCellBroadcast(int messageIdentifier, int ranType) {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.disableCellBroadcastForSubscriber(getSubscriptionId(), messageIdentifier, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (endMessageId < startMessageId) {
            throw new IllegalArgumentException("endMessageId < startMessageId");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.enableCellBroadcastRangeForSubscriber(getSubscriptionId(), startMessageId, endMessageId, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (endMessageId < startMessageId) {
            throw new IllegalArgumentException("endMessageId < startMessageId");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.disableCellBroadcastRangeForSubscriber(getSubscriptionId(), startMessageId, endMessageId, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    private ArrayList<SmsMessage> createMessageListFromRawRecords(List<SmsRawData> records) {
        ArrayList<SmsMessage> messages = new ArrayList();
        if (records != null) {
            int count = records.size();
            for (int i = 0; i < count; i++) {
                SmsRawData data = (SmsRawData) records.get(i);
                if (data != null) {
                    SmsMessage sms = SmsMessage.createFromEfRecord(i + 1, data.getBytes(), getSubscriptionId());
                    if (sms != null) {
                        messages.add(sms);
                    }
                }
            }
        }
        return messages;
    }

    public boolean isImsSmsSupported() {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.isImsSmsSupportedForSubscriber(getSubscriptionId());
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getImsSmsFormat() {
        String format = SmsConstants.FORMAT_UNKNOWN;
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.getImsSmsFormatForSubscriber(getSubscriptionId());
            }
            return format;
        } catch (RemoteException e) {
            return format;
        }
    }

    public static int getDefaultSmsSubscriptionId() {
        try {
            return Stub.asInterface(ServiceManager.getService("isms")).getPreferredSmsSubscription();
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e2) {
            return -1;
        }
    }

    public boolean isSMSPromptEnabled() {
        try {
            return Stub.asInterface(ServiceManager.getService("isms")).isSMSPromptEnabled();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void sendMultimediaMessage(Context context, Uri contentUri, String locationUrl, Bundle configOverrides, PendingIntent sentIntent) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.sendMessage(getSubscriptionId(), ActivityThread.currentPackageName(), contentUri, locationUrl, configOverrides, sentIntent);
            }
        } catch (RemoteException e) {
        }
    }

    public void downloadMultimediaMessage(Context context, String locationUrl, Uri contentUri, Bundle configOverrides, PendingIntent downloadedIntent) {
        if (TextUtils.isEmpty(locationUrl)) {
            throw new IllegalArgumentException("Empty MMS location URL");
        } else if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        } else {
            try {
                IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
                if (iMms != null) {
                    iMms.downloadMessage(getSubscriptionId(), ActivityThread.currentPackageName(), locationUrl, contentUri, configOverrides, downloadedIntent);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public Uri importTextMessage(String address, int type, String text, long timestampMillis, boolean seen, boolean read) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.importTextMessage(ActivityThread.currentPackageName(), address, type, text, timestampMillis, seen, read);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public Uri importMultimediaMessage(Uri contentUri, String messageId, long timestampSecs, boolean seen, boolean read) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.importMultimediaMessage(ActivityThread.currentPackageName(), contentUri, messageId, timestampSecs, seen, read);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public boolean deleteStoredMessage(Uri messageUri) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.deleteStoredMessage(ActivityThread.currentPackageName(), messageUri);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean deleteStoredConversation(long conversationId) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.deleteStoredConversation(ActivityThread.currentPackageName(), conversationId);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean updateStoredMessageStatus(Uri messageUri, ContentValues statusValues) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.updateStoredMessageStatus(ActivityThread.currentPackageName(), messageUri, statusValues);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean archiveStoredConversation(long conversationId, boolean archived) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.archiveStoredConversation(ActivityThread.currentPackageName(), conversationId, archived);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public Uri addTextMessageDraft(String address, String text) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.addTextMessageDraft(ActivityThread.currentPackageName(), address, text);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public Uri addMultimediaMessageDraft(Uri contentUri) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.addMultimediaMessageDraft(ActivityThread.currentPackageName(), contentUri);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public void sendStoredTextMessage(Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            getISmsServiceOrThrow().sendStoredText(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, scAddress, sentIntent, deliveryIntent);
        } catch (RemoteException e) {
        }
    }

    public void sendStoredMultipartTextMessage(Uri messageUri, String scAddress, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            getISmsServiceOrThrow().sendStoredMultipartText(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, scAddress, sentIntents, deliveryIntents);
        } catch (RemoteException e) {
        }
    }

    public void sendStoredMultimediaMessage(Uri messageUri, Bundle configOverrides, PendingIntent sentIntent) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.sendStoredMessage(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, configOverrides, sentIntent);
            }
        } catch (RemoteException e) {
        }
    }

    public void setAutoPersisting(boolean enabled) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.setAutoPersisting(ActivityThread.currentPackageName(), enabled);
            }
        } catch (RemoteException e) {
        }
    }

    public boolean getAutoPersisting() {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.getAutoPersisting();
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public Bundle getCarrierConfigValues() {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.getCarrierConfigValues(getSubscriptionId());
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public String getSmscAddr() {
        return HwFrameworkFactory.getHwBaseInnerSmsManager().getSmscAddr((long) getSubscriptionId());
    }

    public boolean setSmscAddr(String smscAddr) {
        return HwFrameworkFactory.getHwBaseInnerSmsManager().setSmscAddr((long) getSubscriptionId(), smscAddr);
    }

    public String createAppSpecificSmsToken(PendingIntent intent) {
        try {
            return getISmsServiceOrThrow().createAppSpecificSmsToken(getSubscriptionId(), ActivityThread.currentPackageName(), intent);
        } catch (RemoteException ex) {
            ex.rethrowFromSystemServer();
            return null;
        }
    }

    public static Bundle getMmsConfig(BaseBundle config) {
        Bundle filtered = new Bundle();
        filtered.putBoolean("enabledTransID", config.getBoolean("enabledTransID"));
        filtered.putBoolean("enabledMMS", config.getBoolean("enabledMMS"));
        filtered.putBoolean("enableGroupMms", config.getBoolean("enableGroupMms"));
        filtered.putBoolean("enabledNotifyWapMMSC", config.getBoolean("enabledNotifyWapMMSC"));
        filtered.putBoolean("aliasEnabled", config.getBoolean("aliasEnabled"));
        filtered.putBoolean("allowAttachAudio", config.getBoolean("allowAttachAudio"));
        filtered.putBoolean("enableMultipartSMS", config.getBoolean("enableMultipartSMS"));
        filtered.putBoolean("enableSMSDeliveryReports", config.getBoolean("enableSMSDeliveryReports"));
        filtered.putBoolean("supportMmsContentDisposition", config.getBoolean("supportMmsContentDisposition"));
        filtered.putBoolean("sendMultipartSmsAsSeparateMessages", config.getBoolean("sendMultipartSmsAsSeparateMessages"));
        filtered.putBoolean("enableMMSReadReports", config.getBoolean("enableMMSReadReports"));
        filtered.putBoolean("enableMMSDeliveryReports", config.getBoolean("enableMMSDeliveryReports"));
        filtered.putBoolean("mmsCloseConnection", config.getBoolean("mmsCloseConnection"));
        filtered.putInt("maxMessageSize", config.getInt("maxMessageSize"));
        filtered.putInt("maxImageWidth", config.getInt("maxImageWidth"));
        filtered.putInt("maxImageHeight", config.getInt("maxImageHeight"));
        filtered.putInt("recipientLimit", config.getInt("recipientLimit"));
        filtered.putInt("aliasMinChars", config.getInt("aliasMinChars"));
        filtered.putInt("aliasMaxChars", config.getInt("aliasMaxChars"));
        filtered.putInt("smsToMmsTextThreshold", config.getInt("smsToMmsTextThreshold"));
        filtered.putInt("smsToMmsTextLengthThreshold", config.getInt("smsToMmsTextLengthThreshold"));
        filtered.putInt("maxMessageTextSize", config.getInt("maxMessageTextSize"));
        filtered.putInt("maxSubjectLength", config.getInt("maxSubjectLength"));
        filtered.putInt("httpSocketTimeout", config.getInt("httpSocketTimeout"));
        filtered.putString("uaProfTagName", config.getString("uaProfTagName"));
        filtered.putString("userAgent", config.getString("userAgent"));
        filtered.putString("uaProfUrl", config.getString("uaProfUrl"));
        filtered.putString("httpParams", config.getString("httpParams"));
        filtered.putString("emailGatewayNumber", config.getString("emailGatewayNumber"));
        filtered.putString("naiSuffix", config.getString("naiSuffix"));
        filtered.putBoolean("config_cellBroadcastAppLinks", config.getBoolean("config_cellBroadcastAppLinks"));
        filtered.putBoolean("supportHttpCharsetHeader", config.getBoolean("supportHttpCharsetHeader"));
        filtered.putBoolean("smsUsesSimpleCharactersOnly", config.getBoolean("smsUsesSimpleCharactersOnly"));
        filtered.putBoolean("groupChatDefaultsToMMS", config.getBoolean("groupChatDefaultsToMMS"));
        filtered.putBoolean("useCustomUserAgent", config.getBoolean("useCustomUserAgent"));
        return filtered;
    }

    public boolean isUimSupportMeid() {
        try {
            ISms simISms = Stub.asInterface(ServiceManager.getService("isms"));
            if (simISms != null) {
                return simISms.isUimSupportMeid(getSubscriptionId());
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getMeidOrPesn() {
        try {
            ISms simISms = Stub.asInterface(ServiceManager.getService("isms"));
            if (simISms != null) {
                return simISms.getMeidOrPesn(getSubscriptionId());
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean setMeidOrPesn(String meid, String pesn) {
        try {
            ISms simISms = Stub.asInterface(ServiceManager.getService("isms"));
            if (simISms != null) {
                return simISms.setMeidOrPesn(getSubscriptionId(), meid, pesn);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }
}
