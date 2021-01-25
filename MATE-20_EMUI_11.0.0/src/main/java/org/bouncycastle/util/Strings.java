package org.bouncycastle.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Vector;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.util.encoders.UTF8;

public final class Strings {
    private static String LINE_SEPARATOR;

    private static class StringListImpl extends ArrayList<String> implements StringList {
        private StringListImpl() {
        }

        public void add(int i, String str) {
            super.add(i, (int) str);
        }

        @Override // org.bouncycastle.util.StringList
        public boolean add(String str) {
            return super.add((StringListImpl) str);
        }

        @Override // java.util.ArrayList, java.util.List, java.util.AbstractList, org.bouncycastle.util.StringList
        public /* bridge */ /* synthetic */ String get(int i) {
            return (String) super.get(i);
        }

        public String set(int i, String str) {
            return (String) super.set(i, (int) str);
        }

        @Override // org.bouncycastle.util.StringList
        public String[] toStringArray() {
            String[] strArr = new String[size()];
            for (int i = 0; i != strArr.length; i++) {
                strArr[i] = (String) get(i);
            }
            return strArr;
        }

        @Override // org.bouncycastle.util.StringList
        public String[] toStringArray(int i, int i2) {
            String[] strArr = new String[(i2 - i)];
            int i3 = i;
            while (i3 != size() && i3 != i2) {
                strArr[i3 - i] = (String) get(i3);
                i3++;
            }
            return strArr;
        }
    }

    static {
        try {
            LINE_SEPARATOR = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                /* class org.bouncycastle.util.Strings.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public String run() {
                    return System.getProperty("line.separator");
                }
            });
        } catch (Exception e) {
            try {
                LINE_SEPARATOR = String.format("%n", new Object[0]);
            } catch (Exception e2) {
                LINE_SEPARATOR = "\n";
            }
        }
    }

    public static char[] asCharArray(byte[] bArr) {
        char[] cArr = new char[bArr.length];
        for (int i = 0; i != cArr.length; i++) {
            cArr[i] = (char) (bArr[i] & 255);
        }
        return cArr;
    }

    public static String fromByteArray(byte[] bArr) {
        return new String(asCharArray(bArr));
    }

    public static String fromUTF8ByteArray(byte[] bArr) {
        char[] cArr = new char[bArr.length];
        int transcodeToUTF16 = UTF8.transcodeToUTF16(bArr, cArr);
        if (transcodeToUTF16 >= 0) {
            return new String(cArr, 0, transcodeToUTF16);
        }
        throw new IllegalArgumentException("Invalid UTF-8 input");
    }

    public static String lineSeparator() {
        return LINE_SEPARATOR;
    }

    public static StringList newList() {
        return new StringListImpl();
    }

    public static String[] split(String str, char c) {
        int i;
        Vector vector = new Vector();
        boolean z = true;
        while (true) {
            if (!z) {
                break;
            }
            int indexOf = str.indexOf(c);
            if (indexOf > 0) {
                vector.addElement(str.substring(0, indexOf));
                str = str.substring(indexOf + 1);
            } else {
                vector.addElement(str);
                z = false;
            }
        }
        String[] strArr = new String[vector.size()];
        for (i = 0; i != strArr.length; i++) {
            strArr[i] = (String) vector.elementAt(i);
        }
        return strArr;
    }

    public static int toByteArray(String str, byte[] bArr, int i) {
        int length = str.length();
        for (int i2 = 0; i2 < length; i2++) {
            bArr[i + i2] = (byte) str.charAt(i2);
        }
        return length;
    }

    public static byte[] toByteArray(String str) {
        byte[] bArr = new byte[str.length()];
        for (int i = 0; i != bArr.length; i++) {
            bArr[i] = (byte) str.charAt(i);
        }
        return bArr;
    }

    public static byte[] toByteArray(char[] cArr) {
        byte[] bArr = new byte[cArr.length];
        for (int i = 0; i != bArr.length; i++) {
            bArr[i] = (byte) cArr[i];
        }
        return bArr;
    }

    public static String toLowerCase(String str) {
        char[] charArray = str.toCharArray();
        boolean z = false;
        for (int i = 0; i != charArray.length; i++) {
            char c = charArray[i];
            if ('A' <= c && 'Z' >= c) {
                charArray[i] = (char) ((c - 'A') + 97);
                z = true;
            }
        }
        return z ? new String(charArray) : str;
    }

    public static void toUTF8ByteArray(char[] cArr, OutputStream outputStream) throws IOException {
        char c;
        int i;
        int i2 = 0;
        while (i2 < cArr.length) {
            char c2 = cArr[i2];
            char c3 = c2;
            if (c2 >= 128) {
                if (c2 < 2048) {
                    i = (c2 >> 6) | CertificateHolderAuthorization.CVCA;
                } else if (c2 < 55296 || c2 > 57343) {
                    outputStream.write((c2 >> '\f') | 224);
                    i = ((c2 >> 6) & 63) | 128;
                } else {
                    i2++;
                    if (i2 < cArr.length) {
                        char c4 = cArr[i2];
                        if (c2 <= 56319) {
                            int i3 = (((c2 & 1023) << 10) | (c4 & 1023)) + PKIFailureInfo.notAuthorized;
                            outputStream.write((i3 >> 18) | 240);
                            outputStream.write(((i3 >> 12) & 63) | 128);
                            outputStream.write(((i3 >> 6) & 63) | 128);
                            c = i3;
                            c3 = ((c == 1 ? 1 : 0) & 63) | 128;
                        } else {
                            throw new IllegalStateException("invalid UTF-16 codepoint");
                        }
                    } else {
                        throw new IllegalStateException("invalid UTF-16 codepoint");
                    }
                }
                outputStream.write(i);
                c = c2;
                c3 = ((c == 1 ? 1 : 0) & 63) | 128;
            }
            outputStream.write(c3 == 1 ? 1 : 0);
            i2++;
        }
    }

    public static byte[] toUTF8ByteArray(String str) {
        return toUTF8ByteArray(str.toCharArray());
    }

    public static byte[] toUTF8ByteArray(char[] cArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            toUTF8ByteArray(cArr, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("cannot encode string to byte array!");
        }
    }

    public static String toUpperCase(String str) {
        char[] charArray = str.toCharArray();
        boolean z = false;
        for (int i = 0; i != charArray.length; i++) {
            char c = charArray[i];
            if ('a' <= c && 'z' >= c) {
                charArray[i] = (char) ((c - 'a') + 65);
                z = true;
            }
        }
        return z ? new String(charArray) : str;
    }
}
