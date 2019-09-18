package com.android.internal.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

public class IndentingPrintWriter extends PrintWriter {
    private char[] mCurrentIndent;
    private int mCurrentLength;
    private boolean mEmptyLine;
    private StringBuilder mIndentBuilder;
    private char[] mSingleChar;
    private final String mSingleIndent;
    private final int mWrapLength;

    public IndentingPrintWriter(Writer writer, String singleIndent) {
        this(writer, singleIndent, -1);
    }

    public IndentingPrintWriter(Writer writer, String singleIndent, int wrapLength) {
        super(writer);
        this.mIndentBuilder = new StringBuilder();
        this.mEmptyLine = true;
        this.mSingleChar = new char[1];
        this.mSingleIndent = singleIndent;
        this.mWrapLength = wrapLength;
    }

    public IndentingPrintWriter setIndent(String indent) {
        this.mIndentBuilder.setLength(0);
        this.mIndentBuilder.append(indent);
        this.mCurrentIndent = null;
        return this;
    }

    public IndentingPrintWriter setIndent(int indent) {
        int i = 0;
        this.mIndentBuilder.setLength(0);
        while (true) {
            int i2 = i;
            if (i2 >= indent) {
                return this;
            }
            increaseIndent();
            i = i2 + 1;
        }
    }

    public IndentingPrintWriter increaseIndent() {
        this.mIndentBuilder.append(this.mSingleIndent);
        this.mCurrentIndent = null;
        return this;
    }

    public IndentingPrintWriter decreaseIndent() {
        this.mIndentBuilder.delete(0, this.mSingleIndent.length());
        this.mCurrentIndent = null;
        return this;
    }

    public IndentingPrintWriter printPair(String key, Object value) {
        print(key + "=" + String.valueOf(value) + " ");
        return this;
    }

    public IndentingPrintWriter printPair(String key, Object[] value) {
        print(key + "=" + Arrays.toString(value) + " ");
        return this;
    }

    public IndentingPrintWriter printHexPair(String key, int value) {
        print(key + "=0x" + Integer.toHexString(value) + " ");
        return this;
    }

    public void println() {
        write(10);
    }

    public void write(int c) {
        this.mSingleChar[0] = (char) c;
        write(this.mSingleChar, 0, 1);
    }

    public void write(String s, int off, int len) {
        char[] buf = new char[len];
        s.getChars(off, len - off, buf, 0);
        write(buf, 0, len);
    }

    public void write(char[] buf, int offset, int count) {
        int indentLength = this.mIndentBuilder.length();
        int bufferEnd = offset + count;
        int lineStart = offset;
        int lineStart2 = lineStart;
        while (lineStart < bufferEnd) {
            int lineEnd = lineStart + 1;
            char lineEnd2 = buf[lineStart];
            this.mCurrentLength++;
            if (lineEnd2 == 10) {
                maybeWriteIndent();
                super.write(buf, lineStart2, lineEnd - lineStart2);
                lineStart2 = lineEnd;
                this.mEmptyLine = true;
                this.mCurrentLength = 0;
            }
            if (this.mWrapLength > 0 && this.mCurrentLength >= this.mWrapLength - indentLength) {
                if (!this.mEmptyLine) {
                    super.write(10);
                    this.mEmptyLine = true;
                    this.mCurrentLength = lineEnd - lineStart2;
                } else {
                    maybeWriteIndent();
                    super.write(buf, lineStart2, lineEnd - lineStart2);
                    super.write(10);
                    this.mEmptyLine = true;
                    lineStart2 = lineEnd;
                    this.mCurrentLength = 0;
                }
            }
            lineStart = lineEnd;
        }
        if (lineStart2 != lineStart) {
            maybeWriteIndent();
            super.write(buf, lineStart2, lineStart - lineStart2);
        }
    }

    private void maybeWriteIndent() {
        if (this.mEmptyLine) {
            this.mEmptyLine = false;
            if (this.mIndentBuilder.length() != 0) {
                if (this.mCurrentIndent == null) {
                    this.mCurrentIndent = this.mIndentBuilder.toString().toCharArray();
                }
                super.write(this.mCurrentIndent, 0, this.mCurrentIndent.length);
            }
        }
    }
}
