package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.utils.XMLChar;
import org.apache.xpath.axes.WalkerFactory;

final class WriterToUTF8Buffered extends Writer implements WriterChain {
    private static final int BYTES_MAX = 16384;
    private static final int CHARS_MAX = 5461;
    private int count;
    private final char[] m_inputChars;
    private final OutputStream m_os;
    private final byte[] m_outputBytes;

    public WriterToUTF8Buffered(OutputStream out) {
        this.m_os = out;
        this.m_outputBytes = new byte[16387];
        this.m_inputChars = new char[5463];
        this.count = 0;
    }

    public void write(int c) throws IOException {
        if (this.count >= BYTES_MAX) {
            flushBuffer();
        }
        byte[] bArr;
        int i;
        if (c < XMLChar.MASK_NCNAME) {
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
            bArr[i] = (byte) ((c & 63) + XMLChar.MASK_NCNAME);
        } else if (c < WalkerFactory.BIT_CHILD) {
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c >> 12) + 224);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) (((c >> 6) & 63) + XMLChar.MASK_NCNAME);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c & 63) + XMLChar.MASK_NCNAME);
        } else {
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c >> 18) + 240);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) (((c >> 12) & 63) + XMLChar.MASK_NCNAME);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) (((c >> 6) & 63) + XMLChar.MASK_NCNAME);
            bArr = this.m_outputBytes;
            i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) ((c & 63) + XMLChar.MASK_NCNAME);
        }
    }

    public void write(char[] chars, int start, int length) throws IOException {
        char c;
        int lengthx3 = length * 3;
        int i = 16384 - this.count;
        if (lengthx3 >= r0) {
            flushBuffer();
            if (lengthx3 > BYTES_MAX) {
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
                    if (c >= '\ud800' && c <= '\udbff') {
                        end_chunk = end_chunk < start + length ? end_chunk + 1 : end_chunk - 1;
                    }
                    write(chars, start_chunk, end_chunk - start_chunk);
                }
                return;
            }
        }
        int n = length + start;
        byte[] buf_loc = this.m_outputBytes;
        int i2 = start;
        int count_loc = this.count;
        while (i2 < n) {
            c = chars[i2];
            if (c >= '\u0080') {
                break;
            }
            int count_loc2 = count_loc + 1;
            buf_loc[count_loc] = (byte) c;
            i2++;
            count_loc = count_loc2;
        }
        while (i2 < n) {
            c = chars[i2];
            if (c < '\u0080') {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) c;
            } else if (c < '\u0800') {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c >> 6) + 192);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((c & 63) + XMLChar.MASK_NCNAME);
                count_loc2 = count_loc;
            } else if (c < '\ud800' || c > '\udbff') {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c >> 12) + 224);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) (((c >> 6) & 63) + XMLChar.MASK_NCNAME);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c & 63) + XMLChar.MASK_NCNAME);
            } else {
                char high = c;
                i2++;
                char low = chars[i2];
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((((c + 64) >> 8) & 240) | 240);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((((c + 64) >> 2) & 63) | XMLChar.MASK_NCNAME);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((((low >> 6) & 15) + ((c << 4) & 48)) | XMLChar.MASK_NCNAME);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((low & 63) | XMLChar.MASK_NCNAME);
                count_loc2 = count_loc;
            }
            i2++;
            count_loc = count_loc2;
        }
        this.count = count_loc;
    }

    public void write(String s) throws IOException {
        char c;
        int length = s.length();
        int lengthx3 = length * 3;
        if (lengthx3 >= 16384 - this.count) {
            flushBuffer();
            if (lengthx3 > BYTES_MAX) {
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
                    long j = (long) chunks;
                    end_chunk = ((int) ((((long) length) * ((long) chunk)) / r0)) + 0;
                    s.getChars(start_chunk, end_chunk, this.m_inputChars, 0);
                    int len_chunk = end_chunk - start_chunk;
                    c = this.m_inputChars[len_chunk - 1];
                    if (c >= '\ud800' && c <= '\udbff') {
                        end_chunk--;
                        len_chunk--;
                        if (chunk == chunks) {
                        }
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
        int count_loc = this.count;
        while (i < length) {
            c = chars[i];
            if (c >= '\u0080') {
                break;
            }
            int count_loc2 = count_loc + 1;
            buf_loc[count_loc] = (byte) c;
            i++;
            count_loc = count_loc2;
        }
        while (i < length) {
            c = chars[i];
            if (c < '\u0080') {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) c;
            } else if (c < '\u0800') {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c >> 6) + 192);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((c & 63) + XMLChar.MASK_NCNAME);
                count_loc2 = count_loc;
            } else if (c < '\ud800' || c > '\udbff') {
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c >> 12) + 224);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) (((c >> 6) & 63) + XMLChar.MASK_NCNAME);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((c & 63) + XMLChar.MASK_NCNAME);
            } else {
                char high = c;
                i++;
                char low = chars[i];
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((((c + 64) >> 8) & 240) | 240);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((((c + 64) >> 2) & 63) | XMLChar.MASK_NCNAME);
                count_loc2 = count_loc + 1;
                buf_loc[count_loc] = (byte) ((((low >> 6) & 15) + ((c << 4) & 48)) | XMLChar.MASK_NCNAME);
                count_loc = count_loc2 + 1;
                buf_loc[count_loc2] = (byte) ((low & 63) | XMLChar.MASK_NCNAME);
                count_loc2 = count_loc;
            }
            i++;
            count_loc = count_loc2;
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
