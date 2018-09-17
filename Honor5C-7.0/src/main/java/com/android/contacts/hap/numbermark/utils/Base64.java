package com.android.contacts.hap.numbermark.utils;

import com.android.contacts.util.HwLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Base64 {
    private static final char[] legalChars = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.contacts.hap.numbermark.utils.Base64.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.contacts.hap.numbermark.utils.Base64.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.numbermark.utils.Base64.<clinit>():void");
    }

    public static String encode(byte[] data) {
        int len = data.length;
        StringBuffer buf = new StringBuffer((data.length * 3) / 2);
        int end = len - 3;
        int i = 0;
        int n = 0;
        while (i <= end) {
            int d = (((data[i] & 255) << 16) | ((data[i + 1] & 255) << 8)) | (data[i + 2] & 255);
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
        if (i == (len + 0) - 2) {
            d = ((data[i] & 255) << 16) | ((data[i + 1] & 255) << 8);
            buf.append(legalChars[(d >> 18) & 63]);
            buf.append(legalChars[(d >> 12) & 63]);
            buf.append(legalChars[(d >> 6) & 63]);
            buf.append("=");
        } else if (i == (len + 0) - 1) {
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
        byte[] bArr;
        synchronized (Base64.class) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bArr = null;
            if (s != null) {
                try {
                    if (!"".equals(s)) {
                        if (s.length() > 0) {
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
                            bArr = bos.toByteArray();
                        }
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
        return bArr;
    }
}
