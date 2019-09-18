package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

final class WriterToUTF8Buffered extends Writer implements WriterChain {
    private static final int BYTES_MAX = 16384;
    private static final int CHARS_MAX = 5461;
    private int count = 0;
    private final char[] m_inputChars = new char[5463];
    private final OutputStream m_os;
    private final byte[] m_outputBytes = new byte[16387];

    public WriterToUTF8Buffered(OutputStream out) {
        this.m_os = out;
    }

    public void write(int c) throws IOException {
        if (this.count >= 16384) {
            flushBuffer();
        }
        if (c < 128) {
            byte[] bArr = this.m_outputBytes;
            int i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) c;
        } else if (c < 2048) {
            byte[] bArr2 = this.m_outputBytes;
            int i2 = this.count;
            this.count = i2 + 1;
            bArr2[i2] = (byte) (192 + (c >> 6));
            byte[] bArr3 = this.m_outputBytes;
            int i3 = this.count;
            this.count = i3 + 1;
            bArr3[i3] = (byte) (128 + (c & 63));
        } else if (c < 65536) {
            byte[] bArr4 = this.m_outputBytes;
            int i4 = this.count;
            this.count = i4 + 1;
            bArr4[i4] = (byte) (224 + (c >> 12));
            byte[] bArr5 = this.m_outputBytes;
            int i5 = this.count;
            this.count = i5 + 1;
            bArr5[i5] = (byte) (((c >> 6) & 63) + 128);
            byte[] bArr6 = this.m_outputBytes;
            int i6 = this.count;
            this.count = i6 + 1;
            bArr6[i6] = (byte) (128 + (c & 63));
        } else {
            byte[] bArr7 = this.m_outputBytes;
            int i7 = this.count;
            this.count = i7 + 1;
            bArr7[i7] = (byte) (240 + (c >> 18));
            byte[] bArr8 = this.m_outputBytes;
            int i8 = this.count;
            this.count = i8 + 1;
            bArr8[i8] = (byte) (((c >> 12) & 63) + 128);
            byte[] bArr9 = this.m_outputBytes;
            int i9 = this.count;
            this.count = i9 + 1;
            bArr9[i9] = (byte) (((c >> 6) & 63) + 128);
            byte[] bArr10 = this.m_outputBytes;
            int i10 = this.count;
            this.count = i10 + 1;
            bArr10[i10] = (byte) (128 + (c & 63));
        }
    }

    public void write(char[] chars, int start, int length) throws IOException {
        int count_loc;
        int count_loc2;
        int chunks;
        char[] cArr = chars;
        int i = length;
        int lengthx3 = 3 * i;
        char c = 56319;
        char c2 = 55296;
        if (lengthx3 >= 16384 - this.count) {
            flushBuffer();
            if (lengthx3 > 16384) {
                int split = i / CHARS_MAX;
                if (i % CHARS_MAX > 0) {
                    chunks = split + 1;
                } else {
                    chunks = split;
                }
                int end_chunk = start;
                for (int chunk = 1; chunk <= chunks; chunk++) {
                    int start_chunk = end_chunk;
                    end_chunk = start + ((int) ((((long) i) * ((long) chunk)) / ((long) chunks)));
                    char c3 = cArr[end_chunk - 1];
                    char c4 = cArr[end_chunk - 1];
                    if (c3 >= 55296 && c3 <= 56319) {
                        end_chunk = end_chunk < start + i ? end_chunk + 1 : end_chunk - 1;
                    }
                    write(cArr, start_chunk, end_chunk - start_chunk);
                }
                return;
            }
        }
        int split2 = i + start;
        byte[] buf_loc = this.m_outputBytes;
        int count_loc3 = this.count;
        int i2 = start;
        while (i2 < split2) {
            char c5 = cArr[i2];
            char c6 = c5;
            if (c5 >= 128) {
                break;
            }
            buf_loc[count_loc] = (byte) c6;
            i2++;
            count_loc3 = count_loc + 1;
        }
        while (i2 < split2) {
            char c7 = cArr[i2];
            if (c7 < 128) {
                buf_loc[count_loc] = (byte) c7;
                count_loc++;
            } else if (c7 < 2048) {
                int count_loc4 = count_loc + 1;
                buf_loc[count_loc] = (byte) (192 + (c7 >> 6));
                count_loc = count_loc4 + 1;
                buf_loc[count_loc4] = (byte) ((c7 & '?') + 128);
            } else {
                if (c7 < c2 || c7 > c) {
                    int count_loc5 = count_loc + 1;
                    buf_loc[count_loc] = (byte) (224 + (c7 >> 12));
                    int count_loc6 = count_loc5 + 1;
                    buf_loc[count_loc5] = (byte) (((c7 >> 6) & 63) + 128);
                    count_loc2 = count_loc6 + 1;
                    buf_loc[count_loc6] = (byte) ((c7 & '?') + 128);
                } else {
                    char high = c7;
                    i2++;
                    char low = cArr[i2];
                    int count_loc7 = count_loc + 1;
                    buf_loc[count_loc] = (byte) ((((high + '@') >> 8) & 240) | 240);
                    int count_loc8 = count_loc7 + 1;
                    buf_loc[count_loc7] = (byte) ((((high + '@') >> 2) & 63) | 128);
                    int count_loc9 = count_loc8 + 1;
                    buf_loc[count_loc8] = (byte) ((((low >> 6) & 15) + ((high << 4) & 48)) | 128);
                    count_loc2 = count_loc9 + 1;
                    buf_loc[count_loc9] = (byte) ((low & '?') | 128);
                }
                count_loc = count_loc2;
            }
            i2++;
            c = 56319;
            c2 = 55296;
        }
        this.count = count_loc;
    }

    public void write(String s) throws IOException {
        int count_loc;
        int count_loc2;
        int chunks;
        String str = s;
        int length = s.length();
        int lengthx3 = 3 * length;
        int i = 0;
        int chunk = 1;
        if (lengthx3 >= 16384 - this.count) {
            flushBuffer();
            if (lengthx3 > 16384) {
                int split = length / CHARS_MAX;
                if (length % CHARS_MAX > 0) {
                    chunks = split + 1;
                } else {
                    chunks = split;
                }
                int end_chunk = 0;
                while (chunk <= chunks) {
                    int start_chunk = end_chunk;
                    int end_chunk2 = ((int) ((((long) length) * ((long) chunk)) / ((long) chunks))) + 0;
                    str.getChars(start_chunk, end_chunk2, this.m_inputChars, 0);
                    int len_chunk = end_chunk2 - start_chunk;
                    char c = this.m_inputChars[len_chunk - 1];
                    if (c >= 55296 && c <= 56319) {
                        end_chunk2--;
                        len_chunk--;
                    }
                    write(this.m_inputChars, 0, len_chunk);
                    chunk++;
                    end_chunk = end_chunk2;
                }
                return;
            }
        }
        str.getChars(0, length, this.m_inputChars, 0);
        char[] chars = this.m_inputChars;
        int n = length;
        byte[] buf_loc = this.m_outputBytes;
        int count_loc3 = this.count;
        while (i < n) {
            char c2 = chars[i];
            char c3 = c2;
            if (c2 >= 128) {
                break;
            }
            buf_loc[count_loc] = (byte) c3;
            i++;
            count_loc3 = count_loc + 1;
        }
        while (i < n) {
            char c4 = chars[i];
            if (c4 < 128) {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) c4;
            } else {
                if (c4 < 2048) {
                    int count_loc4 = count_loc + 1;
                    buf_loc[count_loc] = (byte) (192 + (c4 >> 6));
                    count_loc = count_loc4 + 1;
                    buf_loc[count_loc4] = (byte) ((c4 & '?') + 128);
                } else if (c4 < 55296 || c4 > 56319) {
                    int count_loc5 = count_loc + 1;
                    buf_loc[count_loc] = (byte) (224 + (c4 >> 12));
                    int count_loc6 = count_loc5 + 1;
                    buf_loc[count_loc5] = (byte) (((c4 >> 6) & 63) + 128);
                    count_loc2 = count_loc6 + 1;
                    buf_loc[count_loc6] = (byte) ((c4 & '?') + 128);
                } else {
                    char high = c4;
                    i++;
                    char low = chars[i];
                    int count_loc7 = count_loc + 1;
                    buf_loc[count_loc] = (byte) ((((high + '@') >> 8) & 240) | 240);
                    int count_loc8 = count_loc7 + 1;
                    buf_loc[count_loc7] = (byte) ((((high + '@') >> 2) & 63) | 128);
                    int count_loc9 = count_loc8 + 1;
                    buf_loc[count_loc8] = (byte) ((((low >> 6) & 15) + ((high << 4) & 48)) | 128);
                    count_loc = count_loc9 + 1;
                    buf_loc[count_loc9] = (byte) ((low & '?') | 128);
                }
                i++;
            }
            count_loc = count_loc2;
            i++;
        }
        this.count = count_loc;
    }

    public void flushBuffer() throws IOException {
        if (this.count > 0) {
            this.m_os.write(this.m_outputBytes, 0, this.count);
            this.count = 0;
        }
    }

    public void flush() throws IOException {
        flushBuffer();
        this.m_os.flush();
    }

    public void close() throws IOException {
        flushBuffer();
        this.m_os.close();
    }

    public OutputStream getOutputStream() {
        return this.m_os;
    }

    public Writer getWriter() {
        return null;
    }
}
