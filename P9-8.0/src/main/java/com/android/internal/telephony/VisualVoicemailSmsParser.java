package com.android.internal.telephony;

import android.os.Bundle;

public class VisualVoicemailSmsParser {
    private static final String[] ALLOWED_ALTERNATIVE_FORMAT_EVENT = new String[]{"MBOXUPDATE", "UNRECOGNIZED"};

    public static class WrappedMessageData {
        public final Bundle fields;
        public final String prefix;

        public String toString() {
            return "WrappedMessageData [type=" + this.prefix + " fields=" + this.fields + "]";
        }

        WrappedMessageData(String prefix, Bundle keyValues) {
            this.prefix = prefix;
            this.fields = keyValues;
        }
    }

    public static WrappedMessageData parse(String clientPrefix, String smsBody) {
        try {
            if (!smsBody.startsWith(clientPrefix)) {
                return null;
            }
            int prefixEnd = clientPrefix.length();
            if (smsBody.charAt(prefixEnd) != ':') {
                return null;
            }
            int eventTypeEnd = smsBody.indexOf(":", prefixEnd + 1);
            if (eventTypeEnd == -1) {
                return null;
            }
            String eventType = smsBody.substring(prefixEnd + 1, eventTypeEnd);
            Bundle fields = parseSmsBody(smsBody.substring(eventTypeEnd + 1));
            if (fields == null) {
                return null;
            }
            return new WrappedMessageData(eventType, fields);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private static Bundle parseSmsBody(String message) {
        Bundle keyValues = new Bundle();
        for (String entry : message.split(";")) {
            if (entry.length() != 0) {
                int separatorIndex = entry.indexOf("=");
                if (separatorIndex == -1 || separatorIndex == 0) {
                    return null;
                }
                keyValues.putString(entry.substring(0, separatorIndex), entry.substring(separatorIndex + 1));
            }
        }
        return keyValues;
    }

    public static WrappedMessageData parseAlternativeFormat(String smsBody) {
        try {
            int eventTypeEnd = smsBody.indexOf("?");
            if (eventTypeEnd == -1) {
                return null;
            }
            String eventType = smsBody.substring(0, eventTypeEnd);
            if (!isAllowedAlternativeFormatEvent(eventType)) {
                return null;
            }
            Bundle fields = parseSmsBody(smsBody.substring(eventTypeEnd + 1));
            if (fields == null) {
                return null;
            }
            return new WrappedMessageData(eventType, fields);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private static boolean isAllowedAlternativeFormatEvent(String eventType) {
        for (String event : ALLOWED_ALTERNATIVE_FORMAT_EVENT) {
            if (event.equals(eventType)) {
                return true;
            }
        }
        return false;
    }
}
