package com.android.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VisualVoicemailSms.Builder;
import android.telephony.VisualVoicemailSmsFilterSettings;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.telephony.VisualVoicemailSmsParser.WrappedMessageData;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class VisualVoicemailSmsFilter {
    private static final PhoneAccountHandleConverter DEFAULT_PHONE_ACCOUNT_HANDLE_CONVERTER = new PhoneAccountHandleConverter() {
        public PhoneAccountHandle fromSubId(int subId) {
            if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                return null;
            }
            int phoneId = SubscriptionManager.getPhoneId(subId);
            if (phoneId == -1) {
                return null;
            }
            return new PhoneAccountHandle(VisualVoicemailSmsFilter.PSTN_CONNECTION_SERVICE_COMPONENT, PhoneFactory.getPhone(phoneId).getIccSerialNumber());
        }
    };
    private static final ComponentName PSTN_CONNECTION_SERVICE_COMPONENT = new ComponentName(TELEPHONY_SERVICE_PACKAGE, "com.android.services.telephony.TelephonyConnectionService");
    private static final String TAG = "VvmSmsFilter";
    private static final String TELEPHONY_SERVICE_PACKAGE = "com.android.phone";
    private static Map<String, List<Pattern>> sPatterns;
    private static PhoneAccountHandleConverter sPhoneAccountHandleConverter = DEFAULT_PHONE_ACCOUNT_HANDLE_CONVERTER;

    public interface PhoneAccountHandleConverter {
        PhoneAccountHandle fromSubId(int i);
    }

    private static class FullMessage {
        public SmsMessage firstMessage;
        public String fullMessageBody;

        /* synthetic */ FullMessage(FullMessage -this0) {
            this();
        }

        private FullMessage() {
        }
    }

    public static boolean filter(Context context, byte[][] pdus, String format, int destPort, int subId) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        VisualVoicemailSmsFilterSettings settings = telephonyManager.getActiveVisualVoicemailSmsFilterSettings(subId);
        if (settings == null) {
            return false;
        }
        PhoneAccountHandle phoneAccountHandle = sPhoneAccountHandleConverter.fromSubId(subId);
        if (phoneAccountHandle == null) {
            Log.e(TAG, "Unable to convert subId " + subId + " to PhoneAccountHandle");
            return false;
        }
        FullMessage fullMessage = getFullMessage(pdus, format);
        WrappedMessageData messageData;
        if (fullMessage == null) {
            Log.i(TAG, "Unparsable SMS received");
            messageData = VisualVoicemailSmsParser.parseAlternativeFormat(parseAsciiPduMessage(pdus));
            if (messageData != null) {
                sendVvmSmsBroadcast(context, phoneAccountHandle, messageData, null);
            }
            return false;
        }
        String messageBody = fullMessage.fullMessageBody;
        messageData = VisualVoicemailSmsParser.parse(settings.clientPrefix, messageBody);
        if (messageData != null) {
            if (settings.destinationPort == -2) {
                if (destPort == -1) {
                    Log.i(TAG, "SMS matching VVM format received but is not a DATA SMS");
                    return false;
                }
            } else if (!(settings.destinationPort == -1 || settings.destinationPort == destPort)) {
                Log.i(TAG, "SMS matching VVM format received but is not directed to port " + settings.destinationPort);
                return false;
            }
            if (settings.originatingNumbers.isEmpty() || (isSmsFromNumbers(fullMessage.firstMessage, settings.originatingNumbers) ^ 1) == 0) {
                sendVvmSmsBroadcast(context, phoneAccountHandle, messageData, null);
                return true;
            }
            Log.i(TAG, "SMS matching VVM format received but is not from originating numbers");
            return false;
        }
        buildPatternsMap(context);
        List<Pattern> patterns = (List) sPatterns.get(telephonyManager.getSimOperator(subId));
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (Pattern pattern : patterns) {
            if (pattern.matcher(messageBody).matches()) {
                Log.w(TAG, "Incoming SMS matches pattern " + pattern + " but has illegal format, " + "still dropping as VVM SMS");
                sendVvmSmsBroadcast(context, phoneAccountHandle, null, messageBody);
                return true;
            }
        }
        return false;
    }

    public static void setPhoneAccountHandleConverterForTest(PhoneAccountHandleConverter converter) {
        if (converter == null) {
            sPhoneAccountHandleConverter = DEFAULT_PHONE_ACCOUNT_HANDLE_CONVERTER;
        } else {
            sPhoneAccountHandleConverter = converter;
        }
    }

    private static void buildPatternsMap(Context context) {
        if (sPatterns == null) {
            sPatterns = new ArrayMap();
            for (String entry : context.getResources().getStringArray(17236047)) {
                String[] mccMncList = entry.split(";")[0].split(",");
                Pattern pattern = Pattern.compile(entry.split(";")[1]);
                for (String mccMnc : mccMncList) {
                    if (!sPatterns.containsKey(mccMnc)) {
                        sPatterns.put(mccMnc, new ArrayList());
                    }
                    ((List) sPatterns.get(mccMnc)).add(pattern);
                }
            }
        }
    }

    private static void sendVvmSmsBroadcast(Context context, PhoneAccountHandle phoneAccountHandle, WrappedMessageData messageData, String messageBody) {
        Log.i(TAG, "VVM SMS received");
        Intent intent = new Intent("com.android.internal.provider.action.VOICEMAIL_SMS_RECEIVED");
        Builder builder = new Builder();
        if (messageData != null) {
            builder.setPrefix(messageData.prefix);
            builder.setFields(messageData.fields);
        }
        if (messageBody != null) {
            builder.setMessageBody(messageBody);
        }
        builder.setPhoneAccountHandle(phoneAccountHandle);
        intent.putExtra("android.provider.extra.VOICEMAIL_SMS", builder.build());
        intent.setPackage(TELEPHONY_SERVICE_PACKAGE);
        context.sendBroadcast(intent);
    }

    private static FullMessage getFullMessage(byte[][] pdus, String format) {
        FullMessage result = new FullMessage();
        StringBuilder builder = new StringBuilder();
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        for (byte[] pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu(pdu, format);
            if (message == null) {
                return null;
            }
            if (result.firstMessage == null) {
                result.firstMessage = message;
            }
            String body = message.getMessageBody();
            if (body == null && message.getUserData() != null) {
                try {
                    body = decoder.decode(ByteBuffer.wrap(message.getUserData())).toString();
                } catch (CharacterCodingException e) {
                    return null;
                }
            }
            if (body != null) {
                builder.append(body);
            }
        }
        result.fullMessageBody = builder.toString();
        return result;
    }

    private static String parseAsciiPduMessage(byte[][] pdus) {
        StringBuilder builder = new StringBuilder();
        for (byte[] pdu : pdus) {
            builder.append(new String(pdu, StandardCharsets.US_ASCII));
        }
        return builder.toString();
    }

    private static boolean isSmsFromNumbers(SmsMessage message, List<String> numbers) {
        if (message == null) {
            Log.e(TAG, "Unable to create SmsMessage from PDU, cannot determine originating number");
            return false;
        }
        for (String number : numbers) {
            if (PhoneNumberUtils.compare(number, message.getOriginatingAddress())) {
                return true;
            }
        }
        return false;
    }
}
