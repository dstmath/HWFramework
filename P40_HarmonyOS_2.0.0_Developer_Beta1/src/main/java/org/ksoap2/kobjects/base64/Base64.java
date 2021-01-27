package org.ksoap2.kobjects.base64;

import com.android.server.wifi.hotspot2.anqp.Constants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Base64 {
    static final char[] charTab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    public static String encode(byte[] data) {
        return encode(data, 0, data.length, null).toString();
    }

    public static StringBuffer encode(byte[] data, int start, int len, StringBuffer buf) {
        if (buf == null) {
            buf = new StringBuffer((data.length * 3) / 2);
        }
        int end = len - 3;
        int i = start;
        int n = 0;
        while (i <= end) {
            int d = ((data[i] & 255) << 16) | ((data[i + 1] & 255) << 8) | (data[i + 2] & 255);
            buf.append(charTab[(d >> 18) & 63]);
            buf.append(charTab[(d >> 12) & 63]);
            buf.append(charTab[(d >> 6) & 63]);
            buf.append(charTab[d & 63]);
            i += 3;
            int n2 = n + 1;
            if (n >= 14) {
                n = 0;
                buf.append("\r\n");
            } else {
                n = n2;
            }
        }
        if (i == (start + len) - 2) {
            int d2 = ((data[i] & 255) << 16) | ((data[i + 1] & 255) << 8);
            buf.append(charTab[(d2 >> 18) & 63]);
            buf.append(charTab[(d2 >> 12) & 63]);
            buf.append(charTab[(d2 >> 6) & 63]);
            buf.append("=");
        } else if (i == (start + len) - 1) {
            int d3 = (data[i] & 255) << 16;
            buf.append(charTab[(d3 >> 18) & 63]);
            buf.append(charTab[(d3 >> 12) & 63]);
            buf.append("==");
        }
        return buf;
    }

    static int decode(char c) {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        }
        if (c >= 'a' && c <= 'z') {
            return (c - 'a') + 26;
        }
        if (c >= '0' && c <= '9') {
            return (c - '0') + 26 + 26;
        }
        if (c == '+') {
            return 62;
        }
        if (c == '/') {
            return 63;
        }
        if (c == '=') {
            return 0;
        }
        throw new RuntimeException("unexpected code: " + c);
    }

    public static byte[] decode(String s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            decode(s, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static void decode(String s, OutputStream os) throws IOException {
        int i = 0;
        int len = s.length();
        while (true) {
            if (i < len && s.charAt(i) <= ' ') {
                i++;
            } else if (i != len) {
                int tri = (decode(s.charAt(i)) << 18) + (decode(s.charAt(i + 1)) << 12) + (decode(s.charAt(i + 2)) << 6) + decode(s.charAt(i + 3));
                os.write((tri >> 16) & Constants.BYTE_MASK);
                if (s.charAt(i + 2) != '=') {
                    os.write((tri >> 8) & Constants.BYTE_MASK);
                    if (s.charAt(i + 3) != '=') {
                        os.write(tri & Constants.BYTE_MASK);
                        i += 4;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }
}
