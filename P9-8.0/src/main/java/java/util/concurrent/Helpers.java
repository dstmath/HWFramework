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
            Collection<?> e = a[i];
            String s = e == c ? "(this Collection)" : objectToString(e);
            a[i] = s;
            charLength += s.length();
        }
        return toString(a, size, charLength);
    }

    static String toString(Object[] a, int size, int charLength) {
        char[] chars = new char[((size * 2) + charLength)];
        chars[0] = '[';
        int i = 0;
        int j = 1;
        while (i < size) {
            int i2;
            if (i > 0) {
                i2 = j + 1;
                chars[j] = ',';
                j = i2 + 1;
                chars[i2] = ' ';
            }
            i2 = j;
            String s = a[i];
            int len = s.length();
            s.getChars(0, len, chars, i2);
            i++;
            j = i2 + len;
        }
        chars[j] = ']';
        return new String(chars);
    }

    static String mapEntryToString(Object key, Object val) {
        String k = objectToString(key);
        int klen = k.length();
        String v = objectToString(val);
        int vlen = v.length();
        char[] chars = new char[((klen + vlen) + 1)];
        k.getChars(0, klen, chars, 0);
        chars[klen] = '=';
        v.getChars(0, vlen, chars, klen + 1);
        return new String(chars);
    }

    private static String objectToString(Object x) {
        if (x != null) {
            String obj = x.toString();
            if (obj != null) {
                return obj;
            }
        }
        return "null";
    }
}
