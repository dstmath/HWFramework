package com.huawei.zxing.encode;

import android.telephony.MSimTelephonyConstants;
import android.telephony.PhoneNumberUtils;
import java.util.regex.Pattern;

final class MECARDContactEncoder extends ContactEncoder {
    private static final Pattern COMMA = null;
    private static final Formatter MECARD_FIELD_FORMATTER = null;
    private static final Pattern NEWLINE = null;
    private static final Pattern NOT_DIGITS = null;
    private static final Pattern RESERVED_MECARD_CHARS = null;
    private static final char TERMINATOR = ';';

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.encode.MECARDContactEncoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.encode.MECARDContactEncoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.encode.MECARDContactEncoder.<clinit>():void");
    }

    MECARDContactEncoder() {
    }

    public String[] encode(Iterable<String> names, String organization, Iterable<String> addresses, Iterable<String> phones, Iterable<String> emails, Iterable<String> urls, String title, String note) {
        StringBuilder newContents = new StringBuilder(100);
        newContents.append("MECARD:");
        StringBuilder newDisplayContents = new StringBuilder(100);
        appendUpToUnique(newContents, newDisplayContents, "N", names, 1, new Formatter() {
            public String format(String source) {
                return source == null ? null : MECARDContactEncoder.COMMA.matcher(source).replaceAll(MSimTelephonyConstants.MY_RADIO_PLATFORM);
            }
        });
        append(newContents, newDisplayContents, "ORG", organization);
        appendUpToUnique(newContents, newDisplayContents, "ADR", addresses, 1, null);
        appendUpToUnique(newContents, newDisplayContents, "TEL", phones, Integer.MAX_VALUE, new Formatter() {
            public String format(String source) {
                return MECARDContactEncoder.keepOnlyDigits(PhoneNumberUtils.formatNumber(source));
            }
        });
        appendUpToUnique(newContents, newDisplayContents, "EMAIL", emails, Integer.MAX_VALUE, null);
        appendUpToUnique(newContents, newDisplayContents, "URL", urls, Integer.MAX_VALUE, null);
        append(newContents, newDisplayContents, "TIL", title);
        append(newContents, newDisplayContents, "NOTE", note);
        newContents.append(TERMINATOR);
        return new String[]{newContents.toString(), newDisplayContents.toString()};
    }

    private static String keepOnlyDigits(CharSequence s) {
        return s == null ? null : NOT_DIGITS.matcher(s).replaceAll(MSimTelephonyConstants.MY_RADIO_PLATFORM);
    }

    private static void append(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, String value) {
        ContactEncoder.doAppend(newContents, newDisplayContents, prefix, value, MECARD_FIELD_FORMATTER, TERMINATOR);
    }

    private static void appendUpToUnique(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, Iterable<String> values, int max, Formatter formatter) {
        ContactEncoder.doAppendUpToUnique(newContents, newDisplayContents, prefix, values, max, formatter, MECARD_FIELD_FORMATTER, TERMINATOR);
    }
}
