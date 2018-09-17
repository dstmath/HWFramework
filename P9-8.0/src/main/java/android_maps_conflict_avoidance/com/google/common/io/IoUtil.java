package android_maps_conflict_avoidance.com.google.common.io;

import android_maps_conflict_avoidance.com.google.common.Config;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

public class IoUtil {
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private IoUtil() {
    }

    public static byte[] encodeUtf8(String s) {
        byte[] result = new byte[encodeUtf8(s, null, 0)];
        encodeUtf8(s, result, 0);
        return result;
    }

    public static int encodeUtf8(String s, byte[] buf, int pos) {
        int len = s.length();
        int i = 0;
        while (i < len) {
            int code = s.charAt(i);
            if (code >= 55296 && code <= 57343 && i + 1 < len) {
                int codeLo = s.charAt(i + 1);
                if (((codeLo & 64512) ^ (code & 64512)) == 1024) {
                    int codeHi;
                    i++;
                    if ((codeLo & 64512) != 55296) {
                        codeHi = code;
                    } else {
                        codeHi = codeLo;
                        codeLo = code;
                    }
                    code = (((codeHi & 1023) << 10) | (codeLo & 1023)) + 65536;
                }
            }
            if (code <= 127) {
                if (buf != null) {
                    buf[pos] = (byte) ((byte) code);
                }
                pos++;
            } else if (code <= 2047) {
                if (buf != null) {
                    buf[pos] = (byte) ((byte) ((code >> 6) | 192));
                    buf[pos + 1] = (byte) ((byte) ((code & 63) | 128));
                }
                pos += 2;
            } else if (code > 65535) {
                if (buf != null) {
                    buf[pos] = (byte) ((byte) ((code >> 18) | 240));
                    buf[pos + 1] = (byte) ((byte) (((code >> 12) & 63) | 128));
                    buf[pos + 2] = (byte) ((byte) (((code >> 6) & 63) | 128));
                    buf[pos + 3] = (byte) ((byte) ((code & 63) | 128));
                }
                pos += 4;
            } else {
                if (buf != null) {
                    buf[pos] = (byte) ((byte) ((code >> 12) | 224));
                    buf[pos + 1] = (byte) ((byte) (((code >> 6) & 63) | 128));
                    buf[pos + 2] = (byte) ((byte) ((code & 63) | 128));
                }
                pos += 3;
            }
            i++;
        }
        return pos;
    }

    public static String decodeUtf8(byte[] data, int start, int end, boolean tolerant) {
        StringBuffer sb = new StringBuffer(end - start);
        int pos = start;
        while (pos < end) {
            int pos2 = pos + 1;
            int b = data[pos] & 255;
            if (b <= 127) {
                sb.append((char) b);
            } else if (b < 245) {
                int border = 224;
                int count = 1;
                int minCode = 128;
                int mask = 31;
                while (b >= border) {
                    border = (border >> 1) | 128;
                    minCode <<= count != 1 ? 5 : 4;
                    count++;
                    mask >>= 1;
                }
                int code = b & mask;
                int i = 0;
                pos = pos2;
                while (i < count) {
                    code <<= 6;
                    if (pos < end) {
                        if (tolerant || (data[pos] & 192) == 128) {
                            pos2 = pos + 1;
                            code |= data[pos] & 63;
                        } else {
                            throw new IllegalArgumentException("Invalid UTF8");
                        }
                    } else if (tolerant) {
                        pos2 = pos;
                    } else {
                        throw new IllegalArgumentException("Invalid UTF8");
                    }
                    i++;
                    pos = pos2;
                }
                if ((!tolerant && code < minCode) || (code >= 55296 && code <= 57343)) {
                    throw new IllegalArgumentException("Invalid UTF8");
                } else if (code > 65535) {
                    code -= 65536;
                    sb.append((char) ((code >> 10) | 55296));
                    sb.append((char) ((code & 1023) | 56320));
                    pos2 = pos;
                } else {
                    sb.append((char) code);
                    pos2 = pos;
                }
            } else if (tolerant) {
                sb.append((char) b);
            } else {
                throw new IllegalArgumentException("Invalid UTF8");
            }
            pos = pos2;
        }
        return sb.toString();
    }

    public static DataInput createDataInputFromBytes(byte[] bytes) {
        return new ByteArrayDataInput(bytes);
    }

    public static byte[] inflate(byte[] deflatedData, int offset, int length, int inflatedDataSize) throws IOException {
        byte[] compressedDataWithDummyByte = new byte[(length + 1)];
        System.arraycopy(deflatedData, offset, compressedDataWithDummyByte, 0, length);
        InputStream is = Config.getInstance().getInflaterInputStream(new ByteArrayInputStream(compressedDataWithDummyByte, 0, length + 1));
        byte[] decompressedData = new byte[inflatedDataSize];
        int numBytesRemaining = inflatedDataSize;
        int numBytesRead = 0;
        while (numBytesRemaining > 0) {
            try {
                int currentReadCount = is.read(decompressedData, numBytesRead, numBytesRemaining);
                if (currentReadCount == -1) {
                    break;
                }
                numBytesRemaining -= currentReadCount;
                numBytesRead += currentReadCount;
            } catch (Throwable th) {
                is.close();
            }
        }
        if (numBytesRemaining == 0) {
            is.close();
            return decompressedData;
        }
        throw new IOException("Failed to read [" + inflatedDataSize + "] bytes, but only read [" + numBytesRead + "]");
    }
}
