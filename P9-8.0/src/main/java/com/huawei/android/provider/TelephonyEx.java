package com.huawei.android.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import com.huawei.android.util.NoExtAPIException;

public final class TelephonyEx {

    public interface BaseMmsColumns extends BaseColumns {
        public static final String NETWORK_TYPE = "network_type";
        public static final String SUB_ID = "sub_id";
    }

    public static final class Carriers implements BaseColumns {
        public static final String PPPPWD = "ppppwd";
    }

    public static final class GlobalMatchs implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://telephony/globalMatchs");
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String ECC_NOCARD = "ecc_nocard";
        public static final String ECC_WITHCARD = "ecc_withcard";
        public static final String MCC = "mcc";
        public static final String MNC = "mnc";
        public static final String NAME = "name";
        public static final String NUMERIC = "numeric";
    }

    public static final class Mms implements BaseMmsColumns {
        public static boolean isEmailAddress(String address) {
            return android.provider.Telephony.Mms.isEmailAddress(address);
        }
    }

    public static final class NumMatchs implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://telephony/numMatchs");
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String IS_VMN_SHORT_CODE = "is_vmn_short_code";
        public static final String MCC = "mcc";
        public static final String MNC = "mnc";
        public static final String NAME = "name";
        public static final String NUMERIC = "numeric";
        public static final String NUM_MATCH = "num_match";
        public static final String NUM_MATCH_SHORT = "num_match_short";
        public static final String SMS_7BIT_ENABLED = "sms_7bit_enabled";
        public static final String SMS_CODING_NATIONAL = "sms_coding_national";
        public static final String SMS_MAX_MESSAGE_SIZE = "max_message_size";
        public static final String SMS_To_MMS_TEXTTHRESHOLD = "sms_to_mms_textthreshold";
    }

    public interface TextBasedSmsColumns {
        public static final int MESSAGE_TYPE_INBOX_SUB1 = 7;
        public static final int MESSAGE_TYPE_INBOX_SUB2 = 8;
    }

    public static final class Sms implements BaseColumns, TextBasedSmsColumns {

        public static final class Sent implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://sms/sent");

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date, int subId) {
                return android.provider.Telephony.Sms.addMessageToUri(subId, resolver, CONTENT_URI, address, body, subject, date, true, false);
            }

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date, int subId, int networkType) {
                throw new NoExtAPIException("method not supported.");
            }
        }

        public static boolean moveMessageToFolder(Context context, Uri uri, int folder, int error) {
            return android.provider.Telephony.Sms.moveMessageToFolder(context, uri, folder, error);
        }

        public static Uri addMessageToUri(ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId, int subId) {
            return android.provider.Telephony.Sms.addMessageToUri(subId, resolver, uri, address, body, subject, date, read, deliveryReport, threadId);
        }
    }
}
