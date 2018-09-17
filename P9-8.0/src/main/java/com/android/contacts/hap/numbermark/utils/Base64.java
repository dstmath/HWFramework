package com.android.contacts.hap.numbermark.utils;

import com.android.contacts.util.HwLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Base64 {
    private static final char[] legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    public static String encode(byte[] data) {
        int d;
        int len = data.length;
        StringBuffer buf = new StringBuffer((data.length * 3) / 2);
        int end = len - 3;
        int i = 0;
        int n = 0;
        while (i <= end) {
            d = (((data[i] & 255) << 16) | ((data[i + 1] & 255) << 8)) | (data[i + 2] & 255);
            buf.append(legalChars[(d >> 18) & 63]);
            buf.append(legalChars[(d >> 12) & 63]);
            buf.append(legalChars[(d >> 6) & 63]);
            buf.append(legalChars[d & 63]);
            i += 3;
            int n2 = n + 1;
            if (n >= 14) {
                n2 = 0;
                buf.append(" ");
            }
            n = n2;
        }
        if (i == (0 + len) - 2) {
            d = ((data[i] & 255) << 16) | ((data[i + 1] & 255) << 8);
            buf.append(legalChars[(d >> 18) & 63]);
            buf.append(legalChars[(d >> 12) & 63]);
            buf.append(legalChars[(d >> 6) & 63]);
            buf.append("=");
        } else if (i == (0 + len) - 1) {
            d = (data[i] & 255) << 16;
            buf.append(legalChars[(d >> 18) & 63]);
            buf.append(legalChars[(d >> 12) & 63]);
            buf.append("==");
        }
        return buf.toString();
    }

    private static int decode(char c) throws Exception {
        if (c >= 'A' && c <= 'Z') {
            return c - 65;
        }
        if (c >= 'a' && c <= 'z') {
            return (c - 97) + 26;
        }
        if (c >= '0' && c <= '9') {
            return ((c - 48) + 26) + 26;
        }
        switch (c) {
            case '+':
                return 62;
            case '/':
                return 63;
            case '=':
                return 0;
            default:
                throw new Exception("Base64 char decode failed.");
        }
    }

    public static synchronized byte[] decode(String s) throws Exception {
        byte[] decodedBytes;
        synchronized (Base64.class) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            decodedBytes = null;
            if (s != null) {
                try {
                    if (!"".equals(s) && s.length() > 0) {
                        int i = 0;
                        int len = s.length();
                        while (true) {
                            if (i >= len || s.charAt(i) > ' ') {
                                if (i != len) {
                                    int tri = (((decode(s.charAt(i)) << 18) + (decode(s.charAt(i + 1)) << 12)) + (decode(s.charAt(i + 2)) << 6)) + decode(s.charAt(i + 3));
                                    bos.write((tri >> 16) & 255);
                                    if (s.charAt(i + 2) == '=') {
                                        break;
                                    }
                                    bos.write((tri >> 8) & 255);
                                    if (s.charAt(i + 3) == '=') {
                                        break;
                                    }
                                    bos.write(tri & 255);
                                    i += 4;
                                } else {
                                    break;
                                }
                            }
                            i++;
                        }
                        decodedBytes = bos.toByteArray();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException e2) {
                            HwLog.e("Base64", "failed to close FileOutputStream");
                        }
                    }
                } catch (IOException e3) {
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException e4) {
                            HwLog.e("Base64", "failed to close FileOutputStream");
                        }
                    }
                } catch (Throwable th) {
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException e5) {
                            HwLog.e("Base64", "failed to close FileOutputStream");
                        }
                    }
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e6) {
                    HwLog.e("Base64", "failed to close FileOutputStream");
                }
            }
        }
        return decodedBytes;
    }
}
