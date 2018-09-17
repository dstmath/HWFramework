package com.android.org.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

class StreamUtil {
    StreamUtil() {
    }

    static int findLimit(InputStream in) {
        if (in instanceof LimitedInputStream) {
            return ((LimitedInputStream) in).getRemaining();
        }
        if (in instanceof ASN1InputStream) {
            return ((ASN1InputStream) in).getLimit();
        }
        if (in instanceof ByteArrayInputStream) {
            return ((ByteArrayInputStream) in).available();
        }
        if (in instanceof FileInputStream) {
            try {
                FileChannel channel = ((FileInputStream) in).getChannel();
                long size = channel != null ? channel.size() : 2147483647L;
                if (size < 2147483647L) {
                    return (int) size;
                }
            } catch (IOException e) {
            }
        }
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory > 2147483647L) {
            return Integer.MAX_VALUE;
        }
        return (int) maxMemory;
    }

    static int calculateBodyLength(int length) {
        int count = 1;
        if (length > 127) {
            int size = 1;
            int val = length;
            while (true) {
                val >>>= 8;
                if (val == 0) {
                    break;
                }
                size++;
            }
            for (int i = (size - 1) * 8; i >= 0; i -= 8) {
                count++;
            }
        }
        return count;
    }

    static int calculateTagLength(int tagNo) throws IOException {
        if (tagNo < 31) {
            return 1;
        }
        if (tagNo < 128) {
            return 2;
        }
        byte[] stack = new byte[5];
        int pos = stack.length - 1;
        stack[pos] = (byte) (tagNo & 127);
        do {
            tagNo >>= 7;
            pos--;
            stack[pos] = (byte) ((tagNo & 127) | 128);
        } while (tagNo > 127);
        return (stack.length - pos) + 1;
    }
}
