package android.icu.impl;

import android.icu.text.Replaceable;
import android.icu.text.ReplaceableString;
import android.icu.text.Transliterator.Position;
import android.icu.text.UnicodeMatcher;

public class UtilityExtensions {
    public static void appendToRule(StringBuffer rule, String text, boolean isLiteral, boolean escapeUnprintable, StringBuffer quoteBuf) {
        for (int i = 0; i < text.length(); i++) {
            Utility.appendToRule(rule, text.charAt(i), isLiteral, escapeUnprintable, quoteBuf);
        }
    }

    public static void appendToRule(StringBuffer rule, UnicodeMatcher matcher, boolean escapeUnprintable, StringBuffer quoteBuf) {
        if (matcher != null) {
            appendToRule(rule, matcher.toPattern(escapeUnprintable), true, escapeUnprintable, quoteBuf);
        }
    }

    public static String formatInput(ReplaceableString input, Position pos) {
        StringBuffer appendTo = new StringBuffer();
        formatInput(appendTo, input, pos);
        return Utility.escape(appendTo.toString());
    }

    public static StringBuffer formatInput(StringBuffer appendTo, ReplaceableString input, Position pos) {
        if (pos.contextStart < 0 || pos.contextStart > pos.start || pos.start > pos.limit || pos.limit > pos.contextLimit || pos.contextLimit > input.length()) {
            appendTo.append("INVALID Position {cs=" + pos.contextStart + ", s=" + pos.start + ", l=" + pos.limit + ", cl=" + pos.contextLimit + "} on " + input);
        } else {
            String b = input.substring(pos.contextStart, pos.start);
            String c = input.substring(pos.start, pos.limit);
            appendTo.append('{').append(b).append('|').append(c).append('|').append(input.substring(pos.limit, pos.contextLimit)).append('}');
        }
        return appendTo;
    }

    public static String formatInput(Replaceable input, Position pos) {
        return formatInput((ReplaceableString) input, pos);
    }

    public static StringBuffer formatInput(StringBuffer appendTo, Replaceable input, Position pos) {
        return formatInput(appendTo, (ReplaceableString) input, pos);
    }
}
