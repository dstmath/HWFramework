package com.android.internal.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

public class LineBreakBufferedWriter extends PrintWriter {
    private char[] buffer;
    private int bufferIndex;
    private final int bufferSize;
    private int lastNewline;
    private final String lineSeparator;

    public LineBreakBufferedWriter(Writer out, int bufferSize2) {
        this(out, bufferSize2, 16);
    }

    public LineBreakBufferedWriter(Writer out, int bufferSize2, int initialCapacity) {
        super(out);
        this.lastNewline = -1;
        this.buffer = new char[Math.min(initialCapacity, bufferSize2)];
        this.bufferIndex = 0;
        this.bufferSize = bufferSize2;
        this.lineSeparator = System.getProperty("line.separator");
    }

    @Override // java.io.PrintWriter, java.io.Writer, java.io.Flushable
    public void flush() {
        writeBuffer(this.bufferIndex);
        this.bufferIndex = 0;
        super.flush();
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(int c) {
        int i = this.bufferIndex;
        char[] cArr = this.buffer;
        if (i < cArr.length) {
            cArr[i] = (char) c;
            this.bufferIndex = i + 1;
            if (((char) c) == '\n') {
                this.lastNewline = this.bufferIndex;
                return;
            }
            return;
        }
        write(new char[]{(char) c}, 0, 1);
    }

    @Override // java.io.PrintWriter
    public void println() {
        write(this.lineSeparator);
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(char[] buf, int off, int len) {
        while (true) {
            int i = this.bufferIndex;
            int i2 = i + len;
            int i3 = this.bufferSize;
            if (i2 <= i3) {
                break;
            }
            int nextNewLine = -1;
            int maxLength = i3 - i;
            for (int i4 = 0; i4 < maxLength; i4++) {
                if (buf[off + i4] == '\n') {
                    if (this.bufferIndex + i4 >= this.bufferSize) {
                        break;
                    }
                    nextNewLine = i4;
                }
            }
            if (nextNewLine != -1) {
                appendToBuffer(buf, off, nextNewLine);
                writeBuffer(this.bufferIndex);
                this.bufferIndex = 0;
                this.lastNewline = -1;
                off += nextNewLine + 1;
                len -= nextNewLine + 1;
            } else {
                int i5 = this.lastNewline;
                if (i5 != -1) {
                    writeBuffer(i5);
                    removeFromBuffer(this.lastNewline + 1);
                    this.lastNewline = -1;
                } else {
                    int rest = this.bufferSize - this.bufferIndex;
                    appendToBuffer(buf, off, rest);
                    writeBuffer(this.bufferIndex);
                    this.bufferIndex = 0;
                    off += rest;
                    len -= rest;
                }
            }
        }
        if (len > 0) {
            appendToBuffer(buf, off, len);
            for (int i6 = len - 1; i6 >= 0; i6--) {
                if (buf[off + i6] == '\n') {
                    this.lastNewline = (this.bufferIndex - len) + i6;
                    return;
                }
            }
        }
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(String s, int off, int len) {
        while (true) {
            int i = this.bufferIndex;
            int i2 = i + len;
            int i3 = this.bufferSize;
            if (i2 <= i3) {
                break;
            }
            int nextNewLine = -1;
            int maxLength = i3 - i;
            for (int i4 = 0; i4 < maxLength; i4++) {
                if (s.charAt(off + i4) == '\n') {
                    if (this.bufferIndex + i4 >= this.bufferSize) {
                        break;
                    }
                    nextNewLine = i4;
                }
            }
            if (nextNewLine != -1) {
                appendToBuffer(s, off, nextNewLine);
                writeBuffer(this.bufferIndex);
                this.bufferIndex = 0;
                this.lastNewline = -1;
                off += nextNewLine + 1;
                len -= nextNewLine + 1;
            } else {
                int i5 = this.lastNewline;
                if (i5 != -1) {
                    writeBuffer(i5);
                    removeFromBuffer(this.lastNewline + 1);
                    this.lastNewline = -1;
                } else {
                    int rest = this.bufferSize - this.bufferIndex;
                    appendToBuffer(s, off, rest);
                    writeBuffer(this.bufferIndex);
                    this.bufferIndex = 0;
                    off += rest;
                    len -= rest;
                }
            }
        }
        if (len > 0) {
            appendToBuffer(s, off, len);
            for (int i6 = len - 1; i6 >= 0; i6--) {
                if (s.charAt(off + i6) == '\n') {
                    this.lastNewline = (this.bufferIndex - len) + i6;
                    return;
                }
            }
        }
    }

    private void appendToBuffer(char[] buf, int off, int len) {
        int i = this.bufferIndex;
        if (i + len > this.buffer.length) {
            ensureCapacity(i + len);
        }
        System.arraycopy(buf, off, this.buffer, this.bufferIndex, len);
        this.bufferIndex += len;
    }

    private void appendToBuffer(String s, int off, int len) {
        int i = this.bufferIndex;
        if (i + len > this.buffer.length) {
            ensureCapacity(i + len);
        }
        s.getChars(off, off + len, this.buffer, this.bufferIndex);
        this.bufferIndex += len;
    }

    private void ensureCapacity(int capacity) {
        int newSize = (this.buffer.length * 2) + 2;
        if (newSize < capacity) {
            newSize = capacity;
        }
        this.buffer = Arrays.copyOf(this.buffer, newSize);
    }

    private void removeFromBuffer(int i) {
        int i2 = this.bufferIndex;
        int rest = i2 - i;
        if (rest > 0) {
            char[] cArr = this.buffer;
            System.arraycopy(cArr, i2 - rest, cArr, 0, rest);
            this.bufferIndex = rest;
            return;
        }
        this.bufferIndex = 0;
    }

    private void writeBuffer(int length) {
        if (length > 0) {
            super.write(this.buffer, 0, length);
        }
    }
}
