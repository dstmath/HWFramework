package com.huawei.zxing.encode;

import java.util.Collection;
import java.util.HashSet;

abstract class ContactEncoder {
    abstract String[] encode(Iterable<String> iterable, String str, Iterable<String> iterable2, Iterable<String> iterable3, Iterable<String> iterable4, Iterable<String> iterable5, String str2, String str3);

    ContactEncoder() {
    }

    static String trim(String s) {
        if (s == null) {
            return null;
        }
        String result = s.trim();
        if (result.length() == 0) {
            result = null;
        }
        return result;
    }

    static void doAppend(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, String value, Formatter fieldFormatter, char terminator) {
        String trimmed = trim(value);
        if (trimmed != null) {
            newContents.append(prefix).append(':').append(fieldFormatter.format(trimmed)).append(terminator);
            newDisplayContents.append(trimmed).append('\n');
        }
    }

    static void doAppendUpToUnique(StringBuilder newContents, StringBuilder newDisplayContents, String prefix, Iterable<String> values, int max, Formatter formatter, Formatter fieldFormatter, char terminator) {
        if (values != null) {
            int count = 0;
            Collection<String> uniques = new HashSet(2);
            for (String value : values) {
                String trimmed = trim(value);
                if (!(trimmed == null || trimmed.length() <= 0 || uniques.contains(trimmed))) {
                    newContents.append(prefix).append(':').append(fieldFormatter.format(trimmed)).append(terminator);
                    newDisplayContents.append(formatter == null ? trimmed : formatter.format(trimmed)).append('\n');
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
