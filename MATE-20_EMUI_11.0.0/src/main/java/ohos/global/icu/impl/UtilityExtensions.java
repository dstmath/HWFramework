package ohos.global.icu.impl;

import ohos.global.icu.text.Replaceable;
import ohos.global.icu.text.ReplaceableString;
import ohos.global.icu.text.Transliterator;
import ohos.global.icu.text.UnicodeMatcher;

public class UtilityExtensions {
    public static void appendToRule(StringBuffer stringBuffer, String str, boolean z, boolean z2, StringBuffer stringBuffer2) {
        for (int i = 0; i < str.length(); i++) {
            Utility.appendToRule(stringBuffer, str.charAt(i), z, z2, stringBuffer2);
        }
    }

    public static void appendToRule(StringBuffer stringBuffer, UnicodeMatcher unicodeMatcher, boolean z, StringBuffer stringBuffer2) {
        if (unicodeMatcher != null) {
            appendToRule(stringBuffer, unicodeMatcher.toPattern(z), true, z, stringBuffer2);
        }
    }

    public static String formatInput(ReplaceableString replaceableString, Transliterator.Position position) {
        StringBuffer stringBuffer = new StringBuffer();
        formatInput(stringBuffer, replaceableString, position);
        return Utility.escape(stringBuffer.toString());
    }

    public static StringBuffer formatInput(StringBuffer stringBuffer, ReplaceableString replaceableString, Transliterator.Position position) {
        if (position.contextStart < 0 || position.contextStart > position.start || position.start > position.limit || position.limit > position.contextLimit || position.contextLimit > replaceableString.length()) {
            stringBuffer.append("INVALID Position {cs=" + position.contextStart + ", s=" + position.start + ", l=" + position.limit + ", cl=" + position.contextLimit + "} on " + replaceableString);
        } else {
            String substring = replaceableString.substring(position.contextStart, position.start);
            String substring2 = replaceableString.substring(position.start, position.limit);
            String substring3 = replaceableString.substring(position.limit, position.contextLimit);
            stringBuffer.append('{');
            stringBuffer.append(substring);
            stringBuffer.append('|');
            stringBuffer.append(substring2);
            stringBuffer.append('|');
            stringBuffer.append(substring3);
            stringBuffer.append('}');
        }
        return stringBuffer;
    }

    public static String formatInput(Replaceable replaceable, Transliterator.Position position) {
        return formatInput((ReplaceableString) replaceable, position);
    }

    public static StringBuffer formatInput(StringBuffer stringBuffer, Replaceable replaceable, Transliterator.Position position) {
        return formatInput(stringBuffer, (ReplaceableString) replaceable, position);
    }
}
