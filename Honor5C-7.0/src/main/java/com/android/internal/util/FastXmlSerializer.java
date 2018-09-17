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
    private static final int BUFFER_LEN = 8192;
    private static final String[] ESCAPE_TABLE = null;
    private static String sSpace;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.FastXmlSerializer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.FastXmlSerializer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.FastXmlSerializer.<clinit>():void");
    }

    public FastXmlSerializer() {
        this.mText = new char[BUFFER_LEN];
        this.mBytes = ByteBuffer.allocate(BUFFER_LEN);
        this.mIndent = false;
        this.mNesting = 0;
        this.mLineStart = true;
    }

    private void append(char c) throws IOException {
        int pos = this.mPos;
        if (pos >= 8191) {
            flush();
            pos = this.mPos;
        }
        this.mText[pos] = c;
        this.mPos = pos + 1;
    }

    private void append(String str, int i, int length) throws IOException {
        if (length > BUFFER_LEN) {
            int end = i + length;
            while (i < end) {
                int next = i + BUFFER_LEN;
                append(str, i, next < end ? BUFFER_LEN : end - i);
                i = next;
            }
            return;
        }
        int pos = this.mPos;
        if (pos + length > BUFFER_LEN) {
            flush();
            pos = this.mPos;
        }
        str.getChars(i, i + length, this.mText, pos);
        this.mPos = pos + length;
    }

    private void append(char[] buf, int i, int length) throws IOException {
        if (length > BUFFER_LEN) {
            int end = i + length;
            while (i < end) {
                int next = i + BUFFER_LEN;
                append(buf, i, next < end ? BUFFER_LEN : end - i);
                i = next;
            }
            return;
        }
        int pos = this.mPos;
        if (pos + length > BUFFER_LEN) {
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
        indent *= 4;
        if (indent > sSpace.length()) {
            indent = sSpace.length();
        }
        append(sSpace, 0, indent);
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
        int lastPos = start;
        int pos = start;
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
        if (position > 0) {
            this.mBytes.flip();
            this.mOutputStream.write(this.mBytes.array(), 0, position);
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
        if (os == null) {
            throw new IllegalArgumentException();
        }
        try {
            this.mCharset = Charset.forName(encoding).newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.mOutputStream = os;
        } catch (IllegalCharsetNameException e) {
            throw ((UnsupportedEncodingException) new UnsupportedEncodingException(encoding).initCause(e));
        } catch (UnsupportedCharsetException e2) {
            throw ((UnsupportedEncodingException) new UnsupportedEncodingException(encoding).initCause(e2));
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
        append("<?xml version='1.0' encoding='utf-8' standalone='" + (standalone.booleanValue() ? "yes" : "no") + "' ?>\n");
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
            if (buf[(start + len) - 1] == '\n') {
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
            if (text.length() > 0 && text.charAt(text.length() - 1) == '\n') {
                z = true;
            }
            this.mLineStart = z;
        }
        return this;
    }
}
