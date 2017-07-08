package com.huawei.android.provider;

import android.content.ContentResolver;
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
        public static final Uri CONTENT_URI = null;
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String ECC_NOCARD = "ecc_nocard";
        public static final String ECC_WITHCARD = "ecc_withcard";
        public static final String MCC = "mcc";
        public static final String MNC = "mnc";
        public static final String NAME = "name";
        public static final String NUMERIC = "numeric";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.provider.TelephonyEx.GlobalMatchs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.provider.TelephonyEx.GlobalMatchs.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.provider.TelephonyEx.GlobalMatchs.<clinit>():void");
        }
    }

    public static final class Mms implements BaseMmsColumns {
        public Mms() {
        }
    }

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
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.provider.TelephonyEx.NumMatchs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.provider.TelephonyEx.NumMatchs.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.provider.TelephonyEx.NumMatchs.<clinit>():void");
        }

        public NumMatchs() {
        }
    }

    public interface TextBasedSmsColumns {
        public static final int MESSAGE_TYPE_INBOX_SUB1 = 7;
        public static final int MESSAGE_TYPE_INBOX_SUB2 = 8;
    }

    public static final class Sms implements BaseColumns, TextBasedSmsColumns {

        public static final class Sent implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = null;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.provider.TelephonyEx.Sms.Sent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.provider.TelephonyEx.Sms.Sent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.provider.TelephonyEx.Sms.Sent.<clinit>():void");
            }

            public Sent() {
            }

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date, int subId) {
                return android.provider.Telephony.Sms.addMessageToUri(subId, resolver, CONTENT_URI, address, body, subject, date, true, false);
            }

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date, int subId, int networkType) {
                throw new NoExtAPIException("method not supported.");
            }
        }

        public Sms() {
        }

        public static Uri addMessageToUri(ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId, int subId) {
            return android.provider.Telephony.Sms.addMessageToUri(subId, resolver, uri, address, body, subject, date, read, deliveryReport, threadId);
        }
    }

    public TelephonyEx() {
    }
}
