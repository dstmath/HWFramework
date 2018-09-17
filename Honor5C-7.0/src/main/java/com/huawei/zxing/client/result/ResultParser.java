package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class ResultParser {
    private static final Pattern ALPHANUM = null;
    private static final Pattern AMPERSAND = null;
    private static final String BYTE_ORDER_MARK = "\ufeff";
    private static final Pattern DIGITS = null;
    private static final Pattern EQUALS = null;
    private static final ResultParser[] PARSERS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.client.result.ResultParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.client.result.ResultParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.client.result.ResultParser.<clinit>():void");
    }

    public abstract ParsedResult parse(Result result);

    protected static String getMassagedText(Result result) {
        String text = result.getText();
        if (text.startsWith(BYTE_ORDER_MARK)) {
            return text.substring(1);
        }
        return text;
    }

    public static ParsedResult parseResult(Result theResult) {
        for (ResultParser parser : PARSERS) {
            ParsedResult result = parser.parse(theResult);
            if (result != null) {
                return result;
            }
        }
        return new TextParsedResult(theResult.getText(), null);
    }

    protected static String[] maybeWrap(String value) {
        if (value == null) {
            return null;
        }
        return new String[]{value};
    }

    protected static String unescapeBackslash(String escaped) {
        int backslash = escaped.indexOf(92);
        if (backslash < 0) {
            return escaped;
        }
        int max = escaped.length();
        StringBuilder unescaped = new StringBuilder(max - 1);
        unescaped.append(escaped.toCharArray(), 0, backslash);
        boolean nextIsEscaped = false;
        for (int i = backslash; i < max; i++) {
            char c = escaped.charAt(i);
            if (nextIsEscaped || c != '\\') {
                unescaped.append(c);
                nextIsEscaped = false;
            } else {
                nextIsEscaped = true;
            }
        }
        return unescaped.toString();
    }

    protected static int parseHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + 10;
        }
        if (c < 'A' || c > 'F') {
            return -1;
        }
        return (c - 65) + 10;
    }

    protected static boolean isStringOfDigits(CharSequence value, int length) {
        return (value == null || length != value.length()) ? false : DIGITS.matcher(value).matches();
    }

    protected static boolean isSubstringOfDigits(CharSequence value, int offset, int length) {
        boolean z = false;
        if (value == null) {
            return false;
        }
        int max = offset + length;
        if (value.length() >= max) {
            z = DIGITS.matcher(value.subSequence(offset, max)).matches();
        }
        return z;
    }

    protected static boolean isSubstringOfAlphaNumeric(CharSequence value, int offset, int length) {
        boolean z = false;
        if (value == null) {
            return false;
        }
        int max = offset + length;
        if (value.length() >= max) {
            z = ALPHANUM.matcher(value.subSequence(offset, max)).matches();
        }
        return z;
    }

    static Map<String, String> parseNameValuePairs(String uri) {
        int paramStart = uri.indexOf(63);
        if (paramStart < 0) {
            return null;
        }
        Map<String, String> result = new HashMap(3);
        for (String keyValue : AMPERSAND.split(uri.substring(paramStart + 1))) {
            appendKeyValue(keyValue, result);
        }
        return result;
    }

    private static void appendKeyValue(CharSequence keyValue, Map<String, String> result) {
        String[] keyValueTokens = EQUALS.split(keyValue, 2);
        if (keyValueTokens.length == 2) {
            try {
                result.put(keyValueTokens[0], urlDecode(keyValueTokens[1]));
            } catch (IllegalArgumentException e) {
            }
        }
    }

    static String urlDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee);
        }
    }

    static String[] matchPrefixedField(String prefix, String rawText, char endChar, boolean trim) {
        List matches = null;
        int i = 0;
        int max = rawText.length();
        while (i < max) {
            i = rawText.indexOf(prefix, i);
            if (i < 0) {
                break;
            }
            i += prefix.length();
            int start = i;
            boolean more = true;
            while (more) {
                i = rawText.indexOf(endChar, i);
                if (i < 0) {
                    i = rawText.length();
                    more = false;
                } else if (rawText.charAt(i - 1) == '\\') {
                    i++;
                } else {
                    if (matches == null) {
                        matches = new ArrayList(3);
                    }
                    String element = unescapeBackslash(rawText.substring(start, i));
                    if (trim) {
                        element = element.trim();
                    }
                    if (!element.isEmpty()) {
                        matches.add(element);
                    }
                    i++;
                    more = false;
                }
            }
        }
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        return (String[]) matches.toArray(new String[matches.size()]);
    }

    static String matchSinglePrefixedField(String prefix, String rawText, char endChar, boolean trim) {
        String[] matches = matchPrefixedField(prefix, rawText, endChar, trim);
        if (matches == null) {
            return null;
        }
        return matches[0];
    }
}
