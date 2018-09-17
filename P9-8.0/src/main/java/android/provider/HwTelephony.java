package android.provider;

import android.net.Uri;

public class HwTelephony {

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

    public static final class VirtualNets implements BaseColumns {
        public static final String APN_FILTER = "apn_filter";
        public static final String CARRIER = "carrier";
        public static final Uri CONTENT_URI = Uri.parse("content://telephony/virtualNets");
        public static final String ECC_NO_CARD = "ecc_nocard";
        public static final String ECC_WITH_CARD = "ecc_withcard";
        public static final String GID1 = "gid1";
        public static final String GID_MASK = "gid_mask";
        public static final String ICCID_END_POSITION = "iccid_end_position";
        public static final String ICCID_RANGE_VALUE = "iccid_range_value";
        public static final String ICCID_START_POSITION = "iccid_start_position";
        public static final String IMSI_SP_END = "imsi_sp_end";
        public static final String IMSI_SP_LIST = "imsi_sp_list";
        public static final String IMSI_SP_START = "imsi_sp_start";
        public static final String IMSI_START = "imsi_start";
        public static final String MATCH_FILE = "match_file";
        public static final String MATCH_MASK = "match_mask";
        public static final String MATCH_PATH = "match_path";
        public static final String MATCH_VALUE = "match_value";
        public static final String NUMERIC = "numeric";
        public static final String NUM_MATCH = "num_match";
        public static final String NUM_MATCH_SHORT = "num_match_short";
        public static final String ONS_NAME = "ons_name";
        public static final int RULE_GID1 = 2;
        public static final int RULE_ICCID = 6;
        public static final int RULE_IMSI = 1;
        public static final int RULE_IMSI_SP = 5;
        public static final int RULE_MATCH_FILE = 4;
        public static final int RULE_NONE = 0;
        public static final int RULE_SPN = 3;
        public static final String SAVED_APN_FILTER = "saved_apn_filter";
        public static final String SAVED_GID1 = "saved_gid1";
        public static final String SAVED_GID_MASK = "saved_gid_mask";
        public static final String SAVED_ICCID_END_POSITION = "saved_iccid_end_position";
        public static final String SAVED_ICCID_RANGE_VALUE = "saved_iccid_range_value";
        public static final String SAVED_ICCID_START_POSITION = "saved_iccid_start_position";
        public static final String SAVED_IMSI_SP_END = "saved_imsi_sp_end";
        public static final String SAVED_IMSI_SP_LIST = "saved_imsi_sp_list";
        public static final String SAVED_IMSI_SP_START = "saved_imsi_sp_start";
        public static final String SAVED_IMSI_START = "saved_imsi_start";
        public static final String SAVED_MATCH_FILE = "saved_match_file";
        public static final String SAVED_MATCH_MASK = "saved_match_mask";
        public static final String SAVED_MATCH_PATH = "saved_match_path";
        public static final String SAVED_MATCH_VALUE = "saved_match_value";
        public static final String SAVED_NUMERIC = "saved_numeric";
        public static final String SAVED_NUM_MATCH = "saved_num_match";
        public static final String SAVED_NUM_MATCH_SHORT = "saved_num_match_short";
        public static final String SAVED_ONS_NAME = "saved_ons_name";
        public static final String SAVED_SMS_7BIT_ENABLED = "saved_sms_7bit_enabled";
        public static final String SAVED_SMS_CODING_NATIONAL = "saved_sms_coding_national";
        public static final String SAVED_SPN = "saved_spn";
        public static final String SAVED_VIRTUAL_NET_RULE = "saved_virtual_net_rule";
        public static final String SAVED_VOICEMAIL_NUMBER = "saved_voicemail_number";
        public static final String SAVED_VOICEMAIL_TAG = "saved_voicemail_tag";
        public static final String SMS_7BIT_ENABLED = "sms_7bit_enabled";
        public static final String SMS_CODING_NATIONAL = "sms_coding_national";
        public static final String SMS_MAX_MESSAGE_SIZE = "max_message_size";
        public static final String SMS_To_MMS_TEXTTHRESHOLD = "sms_to_mms_textthreshold";
        public static final String SPN = "spn";
        public static final String VIRTUAL_NET_RULE = "virtual_net_rule";
        public static final String VN_KEY = "vn_key";
        public static final String VN_KEY_FOR_SPECIALIMSI = "vn_key_for_specialimsi";
        public static final String VOICEMAIL_NUMBER = "voicemail_number";
        public static final String VOICEMAIL_TAG = "voicemail_tag";
    }
}
