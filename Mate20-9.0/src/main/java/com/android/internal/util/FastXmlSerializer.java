package com.android.internal.util;

import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.xmlpull.v1.XmlSerializer;

public class FastXmlSerializer implements XmlSerializer {
    private static final int DEFAULT_BUFFER_LEN = 32768;
    private static final String[] ESCAPE_TABLE = {"&#0;", "&#1;", "&#2;", "&#3;", "&#4;", "&#5;", "&#6;", "&#7;", "&#8;", "&#9;", "&#10;", "&#11;", "&#12;", "&#13;", "&#14;", "&#15;", "&#16;", "&#17;", "&#18;", "&#19;", "&#20;", "&#21;", "&#22;", "&#23;", "&#24;", "&#25;", "&#26;", "&#27;", "&#28;", "&#29;", "&#30;", "&#31;", null, null, "&quot;", null, null, null, "&amp;", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "&lt;", null, "&gt;", null};
    private static String sSpace = "                                                              ";
    private final int mBufferLen;
    private ByteBuffer mBytes;
    private CharsetEncoder mCharset;
    private boolean mInTag;
    private boolean mIndent;
    private boolean mLineStart;
    private int mNesting;
    private OutputStream mOutputStream;
    private int mPos;
    private final char[] mText;
    private Writer mWriter;

    public FastXmlSerializer() {
        this(DEFAULT_BUFFER_LEN);
    }

    public FastXmlSerializer(int bufferSize) {
        this.mIndent = false;
        this.mNesting = 0;
        this.mLineStart = true;
        this.mBufferLen = bufferSize > 0 ? bufferSize : DEFAULT_BUFFER_LEN;
        this.mText = new char[this.mBufferLen];
        this.mBytes = ByteBuffer.allocate(this.mBufferLen);
    }

    private void append(char c) throws IOException {
        int pos = this.mPos;
        if (pos >= this.mBufferLen - 1) {
            flush();
            pos = this.mPos;
        }
        this.mText[pos] = c;
        this.mPos = pos + 1;
    }

    private void append(String str, int i, int length) throws IOException {
        if (length > this.mBufferLen) {
            int end = i + length;
            while (i < end) {
                int next = this.mBufferLen + i;
                append(str, i, next < end ? this.mBufferLen : end - i);
                i = next;
            }
            return;
        }
        int pos = this.mPos;
        if (pos + length > this.mBufferLen) {
            flush();
            pos = this.mPos;
        }
        str.getChars(i, i + length, this.mText, pos);
        this.mPos = pos + length;
    }

    private void append(char[] buf, int i, int length) throws IOException {
        if (length > this.mBufferLen) {
            int end = i + length;
            while (i < end) {
                int next = this.mBufferLen + i;
                append(buf, i, next < end ? this.mBufferLen : end - i);
                i = next;
            }
            return;
        }
        int pos = this.mPos;
        if (pos + length > this.mBufferLen) {
            flush();
            pos = this.mPos;
        }
        System.arraycopy(buf, i, this.mText, pos, length);
        this.mPos = pos + length;
    }

    private void append(String str) throws IOException {
        append(str, 0, str.length());
    }

    private void appendIndent(int indent) throws IOException {
        int indent2 = indent * 4;
        if (indent2 > sSpace.length()) {
            indent2 = sSpace.length();
        }
        append(sSpace, 0, indent2);
    }

    private void escapeAndAppendString(String string) throws IOException {
        if (string == null) {
            Log.w("FastXmlSerializer", "attributed value is null");
            return;
        }
        int N = string.length();
        char NE = (char) ESCAPE_TABLE.length;
        String[] escapes = ESCAPE_TABLE;
        int lastPos = 0;
        int pos = 0;
        while (pos < N) {
            char c = string.charAt(pos);
            if (c < NE) {
                String escape = escapes[c];
                if (escape != null) {
                    if (lastPos < pos) {
                        append(string, lastPos, pos - lastPos);
                    }
                    lastPos = pos + 1;
                    append(escape);
                }
            }
            pos++;
        }
        if (lastPos < pos) {
            append(string, lastPos, pos - lastPos);
        }
    }

    private void escapeAndAppendString(char[] buf, int start, int len) throws IOException {
        char NE = (char) ESCAPE_TABLE.length;
        String[] escapes = ESCAPE_TABLE;
        int end = start + len;
        int pos = start;
        int lastPos = pos;
        while (pos < end) {
            char c = buf[pos];
            if (c < NE) {
                String escape = escapes[c];
                if (escape != null) {
                    if (lastPos < pos) {
                        append(buf, lastPos, pos - lastPos);
                    }
                    lastPos = pos + 1;
                    append(escape);
                }
            }
            pos++;
        }
        if (lastPos < pos) {
            append(buf, lastPos, pos - lastPos);
        }
    }

    public XmlSerializer attribute(String namespace, String name, String value) throws IOException, IllegalArgumentException, IllegalStateException {
        append(' ');
        if (namespace != null) {
            append(namespace);
            append(':');
        }
        append(name);
        append("=\"");
        escapeAndAppendString(value);
        append('\"');
        this.mLineStart = false;
        return this;
    }

    public void cdsect(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void comment(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void docdecl(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        flush();
    }

    public XmlSerializer endTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mNesting--;
        if (this.mInTag) {
            append(" />\n");
        } else {
            if (this.mIndent && this.mLineStart) {
                appendIndent(this.mNesting);
            }
            append("</");
            if (namespace != null) {
                append(namespace);
                append(':');
            }
            append(name);
            append(">\n");
        }
        this.mLineStart = true;
        this.mInTag = false;
        return this;
    }

    public void entityRef(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    private void flushBytes() throws IOException {
        int position = this.mBytes.position();
        int position2 = position;
        if (position > 0) {
            this.mBytes.flip();
            this.mOutputStream.write(this.mBytes.array(), 0, position2);
            this.mBytes.clear();
        }
    }

    public void flush() throws IOException {
        if (this.mPos > 0) {
            if (this.mOutputStream != null) {
                CharBuffer charBuffer = CharBuffer.wrap(this.mText, 0, this.mPos);
                CoderResult result = this.mCharset.encode(charBuffer, this.mBytes, true);
                while (!result.isError()) {
                    if (result.isOverflow()) {
                        flushBytes();
                        result = this.mCharset.encode(charBuffer, this.mBytes, true);
                    } else {
                        flushBytes();
                        this.mOutputStream.flush();
                    }
                }
                throw new IOException(result.toString());
            }
            this.mWriter.write(this.mText, 0, this.mPos);
            this.mWriter.flush();
            this.mPos = 0;
        }
    }

    public int getDepth() {
        throw new UnsupportedOperationException();
    }

    public boolean getFeature(String name) {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public String getNamespace() {
        throw new UnsupportedOperationException();
    }

    public String getPrefix(String namespace, boolean generatePrefix) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    public Object getProperty(String name) {
        throw new UnsupportedOperationException();
    }

    public void ignorableWhitespace(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void processingInstruction(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void setFeature(String name, boolean state) throws IllegalArgumentException, IllegalStateException {
        if (name.equals("http://xmlpull.org/v1/doc/features.html#indent-output")) {
            this.mIndent = true;
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void setOutput(OutputStream os, String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        if (os != null) {
            try {
                this.mCharset = Charset.forName(encoding).newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
                this.mOutputStream = os;
            } catch (IllegalCharsetNameException e) {
                throw ((UnsupportedEncodingException) new UnsupportedEncodingException(encoding).initCause(e));
            } catch (UnsupportedCharsetException e2) {
                throw ((UnsupportedEncodingException) new UnsupportedEncodingException(encoding).initCause(e2));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mWriter = writer;
    }

    public void setPrefix(String prefix, String namespace) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void setProperty(String name, Object value) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='utf-8' standalone='");
        sb.append(standalone.booleanValue() ? "yes" : "no");
        sb.append("' ?>\n");
        append(sb.toString());
        this.mLineStart = true;
    }

    public XmlSerializer startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mInTag) {
            append(">\n");
        }
        if (this.mIndent) {
            appendIndent(this.mNesting);
        }
        this.mNesting++;
        append('<');
        if (namespace != null) {
            append(namespace);
            append(':');
        }
        append(name);
        this.mInTag = true;
        this.mLineStart = false;
        return this;
    }

    public XmlSerializer text(char[] buf, int start, int len) throws IOException, IllegalArgumentException, IllegalStateException {
        boolean z = false;
        if (this.mInTag) {
            append(">");
            this.mInTag = false;
        }
        escapeAndAppendString(buf, start, len);
        if (this.mIndent) {
            if (buf[(start + len) - 1] == 10) {
                z = true;
            }
            this.mLineStart = z;
        }
        return this;
    }

    public XmlSerializer text(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        boolean z = false;
        if (this.mInTag) {
            append(">");
            this.mInTag = false;
        }
        escapeAndAppendString(text);
        if (this.mIndent) {
            if (text.length() > 0 && text.charAt(text.length() - 1) == 10) {
                z = true;
            }
            this.mLineStart = z;
        }
        return this;
    }
}
