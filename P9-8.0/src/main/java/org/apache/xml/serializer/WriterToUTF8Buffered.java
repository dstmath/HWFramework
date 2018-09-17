package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.apache.xml.dtm.DTMFilter;

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
        byte[] bArr;
        int i;
        if (c < 128) {
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) c;
        } else if (c < DTMFilter.SHOW_NOTATION) {
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c >> 6) + 192);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c & 63) + 128);
        } else if (c < 65536) {
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c >> 12) + 224);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) (((c >> 6) & 63) + 128);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c & 63) + 128);
        } else {
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c >> 18) + 240);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) (((c >> 12) & 63) + 128);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) (((c >> 6) & 63) + 128);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c & 63) + 128);
        }
    }

    public void write(char[] chars, int start, int length) throws IOException {
        char c;
        int count_loc;
        int lengthx3 = length * 3;
        if (lengthx3 >= 16384 - this.count) {
            flushBuffer();
            if (lengthx3 > 16384) {
                int chunks;
                int split = length / CHARS_MAX;
                if (length % CHARS_MAX > 0) {
                    chunks = split + 1;
                } else {
                    chunks = split;
                }
                int end_chunk = start;
                for (int chunk = 1; chunk <= chunks; chunk++) {
                    int start_chunk = end_chunk;
                    end_chunk = start + ((int) ((((long) length) * ((long) chunk)) / ((long) chunks)));
                    c = chars[end_chunk - 1];
                    int ic = chars[end_chunk - 1];
                    if (c >= 55296 && c <= 56319) {
                        end_chunk = end_chunk < start + length ? end_chunk + 1 : end_chunk - 1;
                    }
                    write(chars, start_chunk, end_chunk - start_chunk);
                }
                return;
            }
        }
        int n = length + start;
        byte[] buf_loc = this.m_outputBytes;
        int count_loc2 = this.count;
        int i = start;
        while (true) {
            count_loc = count_loc2;
            if (i >= n) {
                break;
            }
            c = chars[i];
            if (c >= 128) {
                break;
            }
            count_loc2 = count_loc + 1;
            buf_loc[count_loc] = (byte) c;
            i++;
        }
        while (i < n) {
            c = chars[i];
            if (c < 128) {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) c;
            } else if (c < 2048) {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c >> 6) + 192);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((c & 63) + 128);
                count_loc2 = count_loc;
            } else if (c < 55296 || c > 56319) {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c >> 12) + 224);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) (((c >> 6) & 63) + 128);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c & 63) + 128);
            } else {
                char high = c;
                i++;
                char low = chars[i];
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((((c + 64) >> 8) & 240) | 240);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((((c + 64) >> 2) & 63) | 128);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((((low >> 6) & 15) + ((c << 4) & 48)) | 128);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((low & 63) | 128);
                count_loc2 = count_loc;
            }
            i++;
            count_loc = count_loc2;
        }
        this.count = count_loc;
    }

    public void write(String s) throws IOException {
        char c;
        int count_loc;
        int length = s.length();
        int lengthx3 = length * 3;
        if (lengthx3 >= 16384 - this.count) {
            flushBuffer();
            if (lengthx3 > 16384) {
                int chunks;
                int split = length / CHARS_MAX;
                if (length % CHARS_MAX > 0) {
                    chunks = split + 1;
                } else {
                    chunks = split;
                }
                int end_chunk = 0;
                for (int chunk = 1; chunk <= chunks; chunk++) {
                    int start_chunk = end_chunk;
                    end_chunk = ((int) ((((long) length) * ((long) chunk)) / ((long) chunks))) + 0;
                    s.getChars(start_chunk, end_chunk, this.m_inputChars, 0);
                    int len_chunk = end_chunk - start_chunk;
                    c = this.m_inputChars[len_chunk - 1];
                    if (c >= 55296 && c <= 56319) {
                        end_chunk--;
                        len_chunk--;
                    }
                    write(this.m_inputChars, 0, len_chunk);
                }
                return;
            }
        }
        s.getChars(0, length, this.m_inputChars, 0);
        char[] chars = this.m_inputChars;
        int n = length;
        byte[] buf_loc = this.m_outputBytes;
        int i = 0;
        int count_loc2 = this.count;
        while (i < length) {
            c = chars[i];
            if (c >= 128) {
                break;
            }
            count_loc = count_loc2 + 1;
            buf_loc[count_loc2] = (byte) c;
            i++;
            count_loc2 = count_loc;
        }
        while (i < length) {
            c = chars[i];
            if (c < 128) {
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) c;
            } else if (c < 2048) {
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((c >> 6) + 192);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c & 63) + 128);
                count_loc = count_loc2;
            } else if (c < 55296 || c > 56319) {
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((c >> 12) + 224);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) (((c >> 6) & 63) + 128);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((c & 63) + 128);
            } else {
                char high = c;
                i++;
                char low = chars[i];
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((((c + 64) >> 8) & 240) | 240);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((((c + 64) >> 2) & 63) | 128);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((((low >> 6) & 15) + ((c << 4) & 48)) | 128);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((low & 63) | 128);
                count_loc = count_loc2;
            }
            i++;
            count_loc2 = count_loc;
        }
        this.count = count_loc2;
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
