package com.huawei.zxing.encode;

import android.telephony.PhoneNumberUtils;
import com.huawei.android.smcs.SmartTrimProcessEvent;
import com.huawei.android.telephony.SignalStrengthEx;
import java.util.regex.Pattern;

final class MECARDContactEncoder extends ContactEncoder {
    private static final Pattern COMMA = Pattern.compile(SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN);
    private static final Formatter MECARD_FIELD_FORMATTER = new Formatter() {
        /* class com.huawei.zxing.encode.MECARDContactEncoder.AnonymousClass1 */

        @Override // com.huawei.zxing.encode.Formatter
        public String format(String source) {
            return MECARDContactEncoder.NEWLINE.matcher(MECARDContactEncoder.RESERVED_MECARD_CHARS.matcher(source).replaceAll("\\\\$1")).replaceAll("");
        }
    };
    private static final Pattern NEWLINE = Pattern.compile("\\n");
    private static final Pattern NOT_DIGITS = Pattern.compile("[^0-9]+");
    private static final Pattern RESERVED_MECARD_CHARS = Pattern.compile("([\\\\:;])");
    private static final char TERMINATOR = ';';

    MECARDContactEncoder() {
    }

    @Override // com.huawei.zxing.encode.ContactEncoder
    public String[] encode(Iterable<String> names, String organization, Iterable<String> addresses, Iterable<String> phones, Iterable<String> emails, Iterable<String> urls, String title, String note) {
        StringBuilder newContents = new StringBuilder(100);
        newContents.append("MECARD:");
        StringBuilder newDisplayContents = new StringBuilder(100);
        appendUpToUnique(newContents, newDisplayContents, "N", names, 1, new Formatter() {
            /* class com.huawei.zxing.encode.MECARDContactEncoder.AnonymousClass2 */

            @Override // com.huawei.zxing.encode.Formatter
            public String format(String source) {
                if (source == null) {
                    return null;
                }
                return MECARDContactEncoder.COMMA.matcher(source).replaceAll("");
            }
        });
        append(newContents, newDisplayContents, "ORG", organization);
        appendUpToUnique(newContents, newDisplayContents, "ADR", addresses, 1, null);
        appendUpToUnique(newContents, newDisplayContents, "TEL", phones, SignalStrengthEx.INVALID, new Formatter() {
            /* class com.huawei.zxing.encode.MECARDContactEncoder.AnonymousClass3 */

            @Override // com.huawei.zxing.encode.Formatter
            public String format(String source) {
                return MECARDContactEncoder.keepOnlyDigits(PhoneNumberUtils.formatNumber(source));
            }
        });
        appendUpToUnique(newContents, newDisplayContents, "EMAIL", emails, SignalStrengthEx.INVALID, null);
        appendUpToUnique(newContents, newDisplayContents, "URL", urls, SignalStrengthEx.INVALID, null);
        append(newContents, newDisplayContents, "TIL", title);
        append(newContents, newDisplayContents, "NOTE", note);
        newContents.append(TERMINATOR);
        return new String[]{newContents.toString(), newDisplayContents.toString()};
    }

    /* access modifiers changed from: private */
    public static String keepOnlyDigits(CharSequence s) {
        if (s == null) {
            return null;
        }
        return NOT_DIGITS.matcher(s).replaceAll("");
    }

    private static void append(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, String value) {
        doAppend(newContents, newDisplayContents, prefix, value, MECARD_FIELD_FORMATTER, TERMINATOR);
    }

    private static void appendUpToUnique(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, Iterable<String> values, int max, Formatter formatter) {
        doAppendUpToUnique(newContents, newDisplayContents, prefix, values, max, formatter, MECARD_FIELD_FORMATTER, TERMINATOR);
    }
}
