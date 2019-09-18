package com.google.android.mms.pdu;

import java.io.ByteArrayOutputStream;

public class QuotedPrintable {
    private static byte ESCAPE_CHAR = 61;

    public static final byte[] decodeQuotedPrintable(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int i = 0;
        while (i < bytes.length) {
            byte b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    if (13 == ((char) bytes[i + 1]) && 10 == ((char) bytes[i + 2])) {
                        i += 2;
                    } else {
                        int i2 = i + 1;
                        int u = Character.digit((char) bytes[i2], 16);
                        i = i2 + 1;
                        int l = Character.digit((char) bytes[i], 16);
                        if (u != -1) {
                            if (l != -1) {
                                buffer.write((char) ((u << 4) + l));
                            }
                        }
                        return null;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            } else {
                buffer.write(b);
            }
            i++;
        }
        return buffer.toByteArray();
    }
}
