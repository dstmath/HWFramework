package com.huawei.okhttp3.internal.http2;

import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.ByteString;
import java.io.IOException;

public final class Http2 {
    static final String[] BINARY = new String[256];
    static final ByteString CONNECTION_PREFACE = ByteString.encodeUtf8("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n");
    static final String[] FLAGS = new String[64];
    static final byte FLAG_ACK = 1;
    static final byte FLAG_COMPRESSED = 32;
    static final byte FLAG_END_HEADERS = 4;
    static final byte FLAG_END_PUSH_PROMISE = 4;
    static final byte FLAG_END_STREAM = 1;
    static final byte FLAG_NONE = 0;
    static final byte FLAG_PADDED = 8;
    static final byte FLAG_PRIORITY = 32;
    private static final String[] FRAME_NAMES = {"DATA", "HEADERS", "PRIORITY", "RST_STREAM", "SETTINGS", "PUSH_PROMISE", "PING", "GOAWAY", "WINDOW_UPDATE", "CONTINUATION"};
    static final int INITIAL_MAX_FRAME_SIZE = 16384;
    static final byte TYPE_CONTINUATION = 9;
    static final byte TYPE_DATA = 0;
    static final byte TYPE_GOAWAY = 7;
    static final byte TYPE_HEADERS = 1;
    static final byte TYPE_PING = 6;
    static final byte TYPE_PRIORITY = 2;
    static final byte TYPE_PUSH_PROMISE = 5;
    static final byte TYPE_RST_STREAM = 3;
    static final byte TYPE_SETTINGS = 4;
    static final byte TYPE_WINDOW_UPDATE = 8;

    static {
        int i = 0;
        while (true) {
            String[] strArr = BINARY;
            if (i >= strArr.length) {
                break;
            }
            strArr[i] = Util.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
            i++;
        }
        String[] strArr2 = FLAGS;
        strArr2[0] = "";
        strArr2[1] = "END_STREAM";
        int[] prefixFlags = {1};
        strArr2[8] = "PADDED";
        for (int prefixFlag : prefixFlags) {
            FLAGS[prefixFlag | 8] = FLAGS[prefixFlag] + "|PADDED";
        }
        String[] strArr3 = FLAGS;
        strArr3[4] = "END_HEADERS";
        strArr3[32] = "PRIORITY";
        strArr3[36] = "END_HEADERS|PRIORITY";
        int[] frameFlags = {4, 32, 36};
        for (int frameFlag : frameFlags) {
            for (int prefixFlag2 : prefixFlags) {
                FLAGS[prefixFlag2 | frameFlag] = FLAGS[prefixFlag2] + '|' + FLAGS[frameFlag];
                FLAGS[prefixFlag2 | frameFlag | 8] = FLAGS[prefixFlag2] + '|' + FLAGS[frameFlag] + "|PADDED";
            }
        }
        int i2 = 0;
        while (true) {
            String[] strArr4 = FLAGS;
            if (i2 < strArr4.length) {
                if (strArr4[i2] == null) {
                    strArr4[i2] = BINARY[i2];
                }
                i2++;
            } else {
                return;
            }
        }
    }

    private Http2() {
    }

    static IllegalArgumentException illegalArgument(String message, Object... args) {
        throw new IllegalArgumentException(Util.format(message, args));
    }

    static IOException ioException(String message, Object... args) throws IOException {
        throw new IOException(Util.format(message, args));
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0026: APUT  (r4v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r5v0 java.lang.String) */
    static String frameLog(boolean inbound, int streamId, int length, byte type, byte flags) {
        String[] strArr = FRAME_NAMES;
        String formattedType = type < strArr.length ? strArr[type] : Util.format("0x%02x", Byte.valueOf(type));
        String formattedFlags = formatFlags(type, flags);
        Object[] objArr = new Object[5];
        objArr[0] = inbound ? "<<" : ">>";
        objArr[1] = Integer.valueOf(streamId);
        objArr[2] = Integer.valueOf(length);
        objArr[3] = formattedType;
        objArr[4] = formattedFlags;
        return Util.format("%s 0x%08x %5d %-13s %s", objArr);
    }

    static String formatFlags(byte type, byte flags) {
        if (flags == 0) {
            return "";
        }
        if (!(type == 2 || type == 3)) {
            if (type == 4 || type == 6) {
                return flags == 1 ? "ACK" : BINARY[flags];
            }
            if (!(type == 7 || type == 8)) {
                String[] strArr = FLAGS;
                String result = flags < strArr.length ? strArr[flags] : BINARY[flags];
                if (type == 5 && (flags & 4) != 0) {
                    return result.replace("HEADERS", "PUSH_PROMISE");
                }
                if (type != 0 || (flags & 32) == 0) {
                    return result;
                }
                return result.replace("PRIORITY", "COMPRESSED");
            }
        }
        return BINARY[flags];
    }
}
