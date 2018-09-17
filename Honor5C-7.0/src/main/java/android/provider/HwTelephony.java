package android.provider;

import android.net.Uri;

public class HwTelephony {

    public static final class NumMatchs implements BaseColumns {
        public static final Uri CONTENT_URI = null;
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

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.HwTelephony.NumMatchs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.HwTelephony.NumMatchs.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.HwTelephony.NumMatchs.<clinit>():void");
        }
    }

    public static final class VirtualNets implements BaseColumns {
        public static final String APN_FILTER = "apn_filter";
        public static final String CARRIER = "carrier";
        public static final Uri CONTENT_URI = null;
        public static final String ECC_NO_CARD = "ecc_nocard";
        public static final String ECC_WITH_CARD = "ecc_withcard";
        public static final String GID1 = "gid1";
        public static final String GID_MASK = "gid_mask";
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
        public static final int RULE_IMSI = 1;
        public static final int RULE_MATCH_FILE = 4;
        public static final int RULE_NONE = 0;
        public static final int RULE_SPN = 3;
        public static final String SAVED_APN_FILTER = "saved_apn_filter";
        public static final String SAVED_GID1 = "saved_gid1";
        public static final String SAVED_GID_MASK = "saved_gid_mask";
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
        public static final String VOICEMAIL_NUMBER = "voicemail_number";
        public static final String VOICEMAIL_TAG = "voicemail_tag";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.HwTelephony.VirtualNets.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.HwTelephony.VirtualNets.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.HwTelephony.VirtualNets.<clinit>():void");
        }

        public VirtualNets() {
        }
    }

    public HwTelephony() {
    }
}
