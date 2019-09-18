package java.util.concurrent;

import java.util.Collection;

class Helpers {
    private Helpers() {
    }

    static String collectionToString(Collection<?> c) {
        Object[] a = c.toArray();
        int size = a.length;
        if (size == 0) {
            return "[]";
        }
        int charLength = 0;
        for (int i = 0; i < size; i++) {
            Object e = a[i];
            String s = e == c ? "(this Collection)" : objectToString(e);
            a[i] = s;
            charLength += s.length();
        }
        return toString(a, size, charLength);
    }

    static String toString(Object[] a, int size, int charLength) {
        char[] chars = new char[((2 * size) + charLength)];
        chars[0] = '[';
        int j = 1;
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                int j2 = j + 1;
                chars[j] = ',';
                j = j2 + 1;
                chars[j2] = ' ';
            }
            String s = a[i];
            int len = s.length();
            s.getChars(0, len, chars, j);
            j += len;
        }
        chars[j] = ']';
        return new String(chars);
    }

    static String mapEntryToString(Object key, Object val) {
        String objectToString = objectToString(key);
        String k = objectToString;
        int length = objectToString.length();
        int klen = length;
        String objectToString2 = objectToString(val);
        String v = objectToString2;
        int vlen = objectToString2.length();
        char[] chars = new char[(length + vlen + 1)];
        k.getChars(0, klen, chars, 0);
        chars[klen] = '=';
        v.getChars(0, vlen, chars, klen + 1);
        return new String(chars);
    }

    private static String objectToString(Object x) {
        if (x != null) {
            String obj = x.toString();
            String str = obj;
            if (obj != null) {
                return str;
            }
        }
        return "null";
    }
}
