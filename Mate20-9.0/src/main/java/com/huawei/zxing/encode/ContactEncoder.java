package com.huawei.zxing.encode;

import java.util.Collection;
import java.util.HashSet;

abstract class ContactEncoder {
    /* access modifiers changed from: package-private */
    public abstract String[] encode(Iterable<String> iterable, String str, Iterable<String> iterable2, Iterable<String> iterable3, Iterable<String> iterable4, Iterable<String> iterable5, String str2, String str3);

    ContactEncoder() {
    }

    static String trim(String s) {
        String str = null;
        if (s == null) {
            return null;
        }
        String result = s.trim();
        if (result.length() != 0) {
            str = result;
        }
        return str;
    }

    static void doAppend(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, String value, Formatter fieldFormatter, char terminator) {
        String trimmed = trim(value);
        if (trimmed != null) {
            newContents.append(prefix);
            newContents.append(':');
            newContents.append(fieldFormatter.format(trimmed));
            newContents.append(terminator);
            newDisplayContents.append(trimmed);
            newDisplayContents.append(10);
        }
    }

    static void doAppendUpToUnique(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, Iterable<String> values, int max, Formatter formatter, Formatter fieldFormatter, char terminator) {
        if (values != null) {
            int count = 0;
            Collection<String> uniques = new HashSet<>(2);
            for (String value : values) {
                String trimmed = trim(value);
                if (trimmed != null && trimmed.length() > 0 && !uniques.contains(trimmed)) {
                    newContents.append(prefix);
                    newContents.append(':');
                    newContents.append(fieldFormatter.format(trimmed));
                    newContents.append(terminator);
                    newDisplayContents.append(formatter == null ? trimmed : formatter.format(trimmed));
                    newDisplayContents.append(10);
                    count++;
                    if (count == max) {
                        break;
                    }
                    uniques.add(trimmed);
                }
            }
        }
    }
}
