package com.huawei.zxing.client.result;

import android.telephony.MSimTelephonyConstants;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.telephony.HuaweiTelephonyManager;
import com.huawei.zxing.Result;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VCardResultParser extends ResultParser {
    private static final Pattern BEGIN_VCARD = null;
    private static final Pattern COMMA = null;
    private static final Pattern CR_LF_SPACE_TAB = null;
    private static final Pattern EQUALS = null;
    private static final Pattern NEWLINE_ESCAPE = null;
    private static final Pattern SEMICOLON = null;
    private static final Pattern SEMICOLON_OR_COMMA = null;
    private static final Pattern UNESCAPED_SEMICOLONS = null;
    private static final Pattern VCARD_ESCAPES = null;
    private static final Pattern VCARD_LIKE_DATE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.client.result.VCardResultParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.client.result.VCardResultParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.client.result.VCardResultParser.<clinit>():void");
    }

    public AddressBookParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        Matcher m = BEGIN_VCARD.matcher(rawText);
        if (!m.find() || m.start() != 0) {
            return null;
        }
        List<List<String>> names = matchVCardPrefixedField("FN", rawText, true, false);
        if (names == null) {
            names = matchVCardPrefixedField("N", rawText, true, false);
            formatNames(names);
        }
        List<String> nicknameString = matchSingleVCardPrefixedField("NICKNAME", rawText, true, false);
        String[] split = nicknameString == null ? null : COMMA.split((CharSequence) nicknameString.get(0));
        List<List<String>> phoneNumbers = matchVCardPrefixedField("TEL", rawText, true, false);
        List<List<String>> emails = matchVCardPrefixedField("EMAIL", rawText, true, false);
        List<String> note = matchSingleVCardPrefixedField("NOTE", rawText, false, false);
        List<List<String>> addresses = matchVCardPrefixedField("ADR", rawText, true, true);
        List<String> org = matchSingleVCardPrefixedField("ORG", rawText, true, true);
        List birthday = matchSingleVCardPrefixedField("BDAY", rawText, true, false);
        if (!(birthday == null || isLikeVCardDate((CharSequence) birthday.get(0)))) {
            birthday = null;
        }
        List<String> title = matchSingleVCardPrefixedField("TITLE", rawText, true, false);
        List<List<String>> urls = matchVCardPrefixedField("URL", rawText, true, false);
        List<String> instantMessenger = matchSingleVCardPrefixedField("IMPP", rawText, true, false);
        List<String> geoString = matchSingleVCardPrefixedField("GEO", rawText, true, false);
        String[] geo = geoString == null ? null : SEMICOLON_OR_COMMA.split((CharSequence) geoString.get(0));
        if (!(geo == null || geo.length == 2)) {
            geo = null;
        }
        return new AddressBookParsedResult(toPrimaryValues(names), split, null, toPrimaryValues(phoneNumbers), toTypes(phoneNumbers), toPrimaryValues(emails), toTypes(emails), toPrimaryValue(instantMessenger), toPrimaryValue(note), toPrimaryValues(addresses), toTypes(addresses), toPrimaryValue(org), toPrimaryValue(birthday), toPrimaryValue(title), toPrimaryValues(urls), geo);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static List<List<String>> matchVCardPrefixedField(CharSequence prefix, String rawText, boolean trim, boolean parseFieldDivider) {
        List<List<String>> matches = null;
        int i = 0;
        int max = rawText.length();
        while (i < max) {
            Matcher matcher = Pattern.compile("(?:^|\n)" + prefix + "(?:;([^:]*))?:", 2).matcher(rawText);
            if (i > 0) {
                i--;
            }
            if (!matcher.find(i)) {
                break;
            }
            i = matcher.end(0);
            String metadataString = matcher.group(1);
            List list = null;
            boolean quotedPrintable = false;
            String str = null;
            if (metadataString != null) {
                for (String metadatum : SEMICOLON.split(metadataString)) {
                    if (list == null) {
                        list = new ArrayList(1);
                    }
                    list.add(metadatum);
                    String[] metadatumTokens = EQUALS.split(metadatum, 2);
                    int length = metadatumTokens.length;
                    if (r0 > 1) {
                        String key = metadatumTokens[0];
                        String value = metadatumTokens[1];
                        if ("ENCODING".equalsIgnoreCase(key)) {
                            if ("QUOTED-PRINTABLE".equalsIgnoreCase(value)) {
                                quotedPrintable = true;
                            }
                        }
                        if ("CHARSET".equalsIgnoreCase(key)) {
                            str = value;
                        }
                    }
                }
            }
            int matchStart = i;
            while (true) {
                i = rawText.indexOf(10, i);
                if (i >= 0) {
                    if (i < rawText.length() - 1) {
                        if (rawText.charAt(i + 1) != ' ') {
                        }
                        i += 2;
                    }
                    if (!quotedPrintable) {
                        break;
                    }
                    if (i >= 1) {
                    }
                    if (i < 2) {
                        break;
                    }
                    if (rawText.charAt(i - 2) != '=') {
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            if (i < 0) {
                i = max;
            } else if (i > matchStart) {
                if (matches == null) {
                    matches = new ArrayList(1);
                }
                if (i >= 1) {
                    if (rawText.charAt(i - 1) == '\r') {
                        i--;
                    }
                }
                String element = rawText.substring(matchStart, i);
                if (trim) {
                    element = element.trim();
                }
                if (quotedPrintable) {
                    element = decodeQuotedPrintable(element, str);
                    if (parseFieldDivider) {
                        element = UNESCAPED_SEMICOLONS.matcher(element).replaceAll("\n").trim();
                    }
                } else {
                    if (parseFieldDivider) {
                        element = UNESCAPED_SEMICOLONS.matcher(element).replaceAll("\n").trim();
                    }
                    element = VCARD_ESCAPES.matcher(NEWLINE_ESCAPE.matcher(CR_LF_SPACE_TAB.matcher(element).replaceAll(MSimTelephonyConstants.MY_RADIO_PLATFORM)).replaceAll("\n")).replaceAll("$1");
                }
                if (list == null) {
                    List<String> match = new ArrayList(1);
                    match.add(element);
                    matches.add(match);
                } else {
                    list.add(0, element);
                    matches.add(list);
                }
                i++;
            } else {
                i++;
            }
        }
        return matches;
    }

    private static String decodeQuotedPrintable(CharSequence value, String charset) {
        int length = value.length();
        StringBuilder result = new StringBuilder(length);
        ByteArrayOutputStream fragmentBuffer = new ByteArrayOutputStream();
        int i = 0;
        while (i < length) {
            char c = value.charAt(i);
            switch (c) {
                case HuaweiTelephonyManager.SINGLE_MODE_SIM_CARD /*10*/:
                case MetricConstant.AUDIO_METRIC_ID /*13*/:
                    break;
                case '=':
                    if (i >= length - 2) {
                        break;
                    }
                    char nextChar = value.charAt(i + 1);
                    if (!(nextChar == '\r' || nextChar == '\n')) {
                        char nextNextChar = value.charAt(i + 2);
                        int firstDigit = ResultParser.parseHexDigit(nextChar);
                        int secondDigit = ResultParser.parseHexDigit(nextNextChar);
                        if (firstDigit >= 0 && secondDigit >= 0) {
                            fragmentBuffer.write((firstDigit << 4) + secondDigit);
                        }
                        i += 2;
                        break;
                    }
                default:
                    maybeAppendFragment(fragmentBuffer, charset, result);
                    result.append(c);
                    break;
            }
            i++;
        }
        maybeAppendFragment(fragmentBuffer, charset, result);
        return result.toString();
    }

    private static void maybeAppendFragment(ByteArrayOutputStream fragmentBuffer, String charset, StringBuilder result) {
        if (fragmentBuffer.size() > 0) {
            String fragment;
            byte[] fragmentBytes = fragmentBuffer.toByteArray();
            if (charset == null) {
                fragment = new String(fragmentBytes, Charset.forName("UTF-8"));
            } else {
                try {
                    fragment = new String(fragmentBytes, charset);
                } catch (UnsupportedEncodingException e) {
                    fragment = new String(fragmentBytes, Charset.forName("UTF-8"));
                }
            }
            fragmentBuffer.reset();
            result.append(fragment);
        }
    }

    static List<String> matchSingleVCardPrefixedField(CharSequence prefix, String rawText, boolean trim, boolean parseFieldDivider) {
        List<List<String>> values = matchVCardPrefixedField(prefix, rawText, trim, parseFieldDivider);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return (List) values.get(0);
    }

    private static String toPrimaryValue(List<String> list) {
        return (list == null || list.isEmpty()) ? null : (String) list.get(0);
    }

    private static String[] toPrimaryValues(Collection<List<String>> lists) {
        if (lists == null || lists.isEmpty()) {
            return null;
        }
        List<String> result = new ArrayList(lists.size());
        for (List<String> list : lists) {
            String value = (String) list.get(0);
            if (!(value == null || value.isEmpty())) {
                result.add(value);
            }
        }
        return (String[]) result.toArray(new String[lists.size()]);
    }

    private static String[] toTypes(Collection<List<String>> lists) {
        if (lists == null || lists.isEmpty()) {
            return null;
        }
        List<String> result = new ArrayList(lists.size());
        for (List<String> list : lists) {
            String type = null;
            int i = 1;
            while (i < list.size()) {
                String metadatum = (String) list.get(i);
                int equals = metadatum.indexOf(61);
                if (equals < 0) {
                    type = metadatum;
                    break;
                } else if ("TYPE".equalsIgnoreCase(metadatum.substring(0, equals))) {
                    type = metadatum.substring(equals + 1);
                    break;
                } else {
                    i++;
                }
            }
            result.add(type);
        }
        return (String[]) result.toArray(new String[lists.size()]);
    }

    private static boolean isLikeVCardDate(CharSequence value) {
        return value != null ? VCARD_LIKE_DATE.matcher(value).matches() : true;
    }

    private static void formatNames(Iterable<List<String>> names) {
        if (names != null) {
            for (List<String> list : names) {
                String name = (String) list.get(0);
                String[] components = new String[5];
                int start = 0;
                int componentIndex = 0;
                while (componentIndex < components.length - 1) {
                    int end = name.indexOf(59, start);
                    if (end < 0) {
                        break;
                    }
                    components[componentIndex] = name.substring(start, end);
                    componentIndex++;
                    start = end + 1;
                }
                components[componentIndex] = name.substring(start);
                StringBuilder newName = new StringBuilder(100);
                maybeAppendComponent(components, 3, newName);
                maybeAppendComponent(components, 1, newName);
                maybeAppendComponent(components, 2, newName);
                maybeAppendComponent(components, 0, newName);
                maybeAppendComponent(components, 4, newName);
                list.set(0, newName.toString().trim());
            }
        }
    }

    private static void maybeAppendComponent(String[] components, int i, StringBuilder newName) {
        if (components[i] != null && !components[i].isEmpty()) {
            if (newName.length() > 0) {
                newName.append(' ');
            }
            newName.append(components[i]);
        }
    }
}
