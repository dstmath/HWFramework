package org.kxml2.io;

import android.icu.impl.PatternTokenizer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import javax.xml.XMLConstants;
import org.xmlpull.v1.XmlSerializer;

public class KXmlSerializer implements XmlSerializer {
    private static final int BUFFER_LEN = 8192;
    private int auto;
    private int depth;
    private String[] elementStack = new String[12];
    private String encoding;
    private boolean[] indent = new boolean[4];
    private int mPos;
    private final char[] mText = new char[8192];
    private int[] nspCounts = new int[4];
    private String[] nspStack = new String[8];
    private boolean pending;
    private boolean unicode;
    private Writer writer;

    private void append(char c) throws IOException {
        if (this.mPos >= 8192) {
            flushBuffer();
        }
        char[] cArr = this.mText;
        int i = this.mPos;
        this.mPos = i + 1;
        cArr[i] = c;
    }

    private void append(String str, int i, int length) throws IOException {
        while (length > 0) {
            if (this.mPos == 8192) {
                flushBuffer();
            }
            int batch = 8192 - this.mPos;
            if (batch > length) {
                batch = length;
            }
            str.getChars(i, i + batch, this.mText, this.mPos);
            i += batch;
            length -= batch;
            this.mPos += batch;
        }
    }

    private void append(String str) throws IOException {
        append(str, 0, str.length());
    }

    private final void flushBuffer() throws IOException {
        if (this.mPos > 0) {
            this.writer.write(this.mText, 0, this.mPos);
            this.writer.flush();
            this.mPos = 0;
        }
    }

    private final void check(boolean close) throws IOException {
        if (this.pending) {
            this.depth++;
            this.pending = false;
            if (this.indent.length <= this.depth) {
                boolean[] hlp = new boolean[(this.depth + 4)];
                System.arraycopy(this.indent, 0, hlp, 0, this.depth);
                this.indent = hlp;
            }
            this.indent[this.depth] = this.indent[this.depth - 1];
            for (int i = this.nspCounts[this.depth - 1]; i < this.nspCounts[this.depth]; i++) {
                append(" xmlns");
                if (!this.nspStack[i * 2].isEmpty()) {
                    append(':');
                    append(this.nspStack[i * 2]);
                } else if (getNamespace().isEmpty() && !this.nspStack[(i * 2) + 1].isEmpty()) {
                    throw new IllegalStateException("Cannot set default namespace for elements in no namespace");
                }
                append("=\"");
                writeEscaped(this.nspStack[(i * 2) + 1], 34);
                append('\"');
            }
            if (this.nspCounts.length <= this.depth + 1) {
                int[] hlp2 = new int[(this.depth + 8)];
                System.arraycopy(this.nspCounts, 0, hlp2, 0, this.depth + 1);
                this.nspCounts = hlp2;
            }
            this.nspCounts[this.depth + 1] = this.nspCounts[this.depth];
            if (close) {
                append(" />");
            } else {
                append('>');
            }
        }
    }

    private final void writeEscaped(String s, int quot) throws IOException {
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c != 13) {
                if (c == '&') {
                    append("&amp;");
                } else if (c == '<') {
                    append("&lt;");
                } else if (c != '>') {
                    switch (c) {
                        case 9:
                        case 10:
                            break;
                        default:
                            if (c != quot) {
                                if (!((c >= ' ' && c <= 55295) || (c >= 57344 && c <= 65533))) {
                                    if (Character.isHighSurrogate(c) && i < s.length() - 1) {
                                        writeSurrogate(c, s.charAt(i + 1));
                                        i++;
                                        break;
                                    } else {
                                        reportInvalidCharacter(c);
                                        break;
                                    }
                                } else if (!this.unicode && c >= 127) {
                                    append("&#" + c + ";");
                                    break;
                                } else {
                                    append(c);
                                    break;
                                }
                            } else {
                                append(c == '\"' ? "&quot;" : "&apos;");
                                continue;
                            }
                            break;
                    }
                } else {
                    append("&gt;");
                }
                i++;
            }
            if (quot == -1) {
                append(c);
            } else {
                append("&#" + c + ';');
            }
            i++;
        }
    }

    private static void reportInvalidCharacter(char ch) {
        throw new IllegalArgumentException("Illegal character (U+" + Integer.toHexString(ch) + ")");
    }

    public void docdecl(String dd) throws IOException {
        append("<!DOCTYPE");
        append(dd);
        append('>');
    }

    public void endDocument() throws IOException {
        while (this.depth > 0) {
            endTag(this.elementStack[(this.depth * 3) - 3], this.elementStack[(this.depth * 3) - 1]);
        }
        flush();
    }

    public void entityRef(String name) throws IOException {
        check(false);
        append('&');
        append(name);
        append(';');
    }

    public boolean getFeature(String name) {
        if ("http://xmlpull.org/v1/doc/features.html#indent-output".equals(name)) {
            return this.indent[this.depth];
        }
        return false;
    }

    public String getPrefix(String namespace, boolean create) {
        try {
            return getPrefix(namespace, false, create);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private final String getPrefix(String namespace, boolean includeDefault, boolean create) throws IOException {
        String prefix;
        for (int i = (this.nspCounts[this.depth + 1] * 2) - 2; i >= 0; i -= 2) {
            if (this.nspStack[i + 1].equals(namespace) && (includeDefault || !this.nspStack[i].isEmpty())) {
                String cand = this.nspStack[i];
                int j = i + 2;
                while (true) {
                    if (j >= this.nspCounts[this.depth + 1] * 2) {
                        break;
                    } else if (this.nspStack[j].equals(cand)) {
                        cand = null;
                        break;
                    } else {
                        j++;
                    }
                }
                if (cand != null) {
                    return cand;
                }
            }
        }
        if (!create) {
            return null;
        }
        if (namespace.isEmpty()) {
            prefix = "";
        } else {
            do {
                StringBuilder sb = new StringBuilder();
                sb.append("n");
                int i2 = this.auto;
                this.auto = i2 + 1;
                sb.append(i2);
                prefix = sb.toString();
                int i3 = (this.nspCounts[this.depth + 1] * 2) - 2;
                while (true) {
                    if (i3 < 0) {
                        break;
                    } else if (prefix.equals(this.nspStack[i3])) {
                        prefix = null;
                        continue;
                        break;
                    } else {
                        i3 -= 2;
                    }
                }
            } while (prefix == null);
        }
        boolean p = this.pending;
        this.pending = false;
        setPrefix(prefix, namespace);
        this.pending = p;
        return prefix;
    }

    public Object getProperty(String name) {
        throw new RuntimeException("Unsupported property");
    }

    public void ignorableWhitespace(String s) throws IOException {
        text(s);
    }

    public void setFeature(String name, boolean value) {
        if ("http://xmlpull.org/v1/doc/features.html#indent-output".equals(name)) {
            this.indent[this.depth] = value;
            return;
        }
        throw new RuntimeException("Unsupported Feature");
    }

    public void setProperty(String name, Object value) {
        throw new RuntimeException("Unsupported Property:" + value);
    }

    public void setPrefix(String prefix, String namespace) throws IOException {
        check(false);
        if (prefix == null) {
            prefix = "";
        }
        if (namespace == null) {
            namespace = "";
        }
        if (!prefix.equals(getPrefix(namespace, true, false))) {
            int[] iArr = this.nspCounts;
            int i = this.depth + 1;
            int i2 = iArr[i];
            iArr[i] = i2 + 1;
            int pos = i2 << 1;
            if (this.nspStack.length < pos + 1) {
                String[] hlp = new String[(this.nspStack.length + 16)];
                System.arraycopy(this.nspStack, 0, hlp, 0, pos);
                this.nspStack = hlp;
            }
            this.nspStack[pos] = prefix;
            this.nspStack[pos + 1] = namespace;
        }
    }

    public void setOutput(Writer writer2) {
        this.writer = writer2;
        this.nspCounts[0] = 2;
        this.nspCounts[1] = 2;
        this.nspStack[0] = "";
        this.nspStack[1] = "";
        this.nspStack[2] = XMLConstants.XML_NS_PREFIX;
        this.nspStack[3] = "http://www.w3.org/XML/1998/namespace";
        this.pending = false;
        this.auto = 0;
        this.depth = 0;
        this.unicode = false;
    }

    public void setOutput(OutputStream os, String encoding2) throws IOException {
        OutputStreamWriter outputStreamWriter;
        if (os != null) {
            if (encoding2 == null) {
                outputStreamWriter = new OutputStreamWriter(os);
            } else {
                outputStreamWriter = new OutputStreamWriter(os, encoding2);
            }
            setOutput(outputStreamWriter);
            this.encoding = encoding2;
            if (encoding2 != null && encoding2.toLowerCase(Locale.US).startsWith("utf")) {
                this.unicode = true;
                return;
            }
            return;
        }
        throw new IllegalArgumentException("os == null");
    }

    public void startDocument(String encoding2, Boolean standalone) throws IOException {
        append("<?xml version='1.0' ");
        if (encoding2 != null) {
            this.encoding = encoding2;
            if (encoding2.toLowerCase(Locale.US).startsWith("utf")) {
                this.unicode = true;
            }
        }
        if (this.encoding != null) {
            append("encoding='");
            append(this.encoding);
            append("' ");
        }
        if (standalone != null) {
            append("standalone='");
            append(standalone.booleanValue() ? "yes" : "no");
            append("' ");
        }
        append("?>");
    }

    public XmlSerializer startTag(String namespace, String name) throws IOException {
        String prefix;
        check(false);
        if (this.indent[this.depth]) {
            append("\r\n");
            for (int i = 0; i < this.depth; i++) {
                append("  ");
            }
        }
        int esp = this.depth * 3;
        if (this.elementStack.length < esp + 3) {
            String[] hlp = new String[(this.elementStack.length + 12)];
            System.arraycopy(this.elementStack, 0, hlp, 0, esp);
            this.elementStack = hlp;
        }
        if (namespace == null) {
            prefix = "";
        } else {
            prefix = getPrefix(namespace, true, true);
        }
        if (namespace != null && namespace.isEmpty()) {
            int i2 = this.nspCounts[this.depth];
            while (i2 < this.nspCounts[this.depth + 1]) {
                if (!this.nspStack[i2 * 2].isEmpty() || this.nspStack[(i2 * 2) + 1].isEmpty()) {
                    i2++;
                } else {
                    throw new IllegalStateException("Cannot set default namespace for elements in no namespace");
                }
            }
        }
        int esp2 = esp + 1;
        this.elementStack[esp] = namespace;
        this.elementStack[esp2] = prefix;
        this.elementStack[esp2 + 1] = name;
        append('<');
        if (!prefix.isEmpty()) {
            append(prefix);
            append(':');
        }
        append(name);
        this.pending = true;
        return this;
    }

    public XmlSerializer attribute(String namespace, String name, String value) throws IOException {
        String prefix;
        if (this.pending) {
            if (namespace == null) {
                namespace = "";
            }
            if (namespace.isEmpty()) {
                prefix = "";
            } else {
                prefix = getPrefix(namespace, false, true);
            }
            append(' ');
            if (!prefix.isEmpty()) {
                append(prefix);
                append(':');
            }
            append(name);
            append('=');
            char q = '\"';
            if (value.indexOf(34) != -1) {
                q = PatternTokenizer.SINGLE_QUOTE;
            }
            append(q);
            writeEscaped(value, q);
            append(q);
            return this;
        }
        throw new IllegalStateException("illegal position for attribute");
    }

    public void flush() throws IOException {
        check(false);
        flushBuffer();
    }

    public XmlSerializer endTag(String namespace, String name) throws IOException {
        if (!this.pending) {
            this.depth--;
        }
        if ((namespace != null || this.elementStack[this.depth * 3] == null) && ((namespace == null || namespace.equals(this.elementStack[this.depth * 3])) && this.elementStack[(this.depth * 3) + 2].equals(name))) {
            if (this.pending) {
                check(true);
                this.depth--;
            } else {
                if (this.indent[this.depth + 1]) {
                    append("\r\n");
                    for (int i = 0; i < this.depth; i++) {
                        append("  ");
                    }
                }
                append("</");
                String prefix = this.elementStack[(this.depth * 3) + 1];
                if (!prefix.isEmpty()) {
                    append(prefix);
                    append(':');
                }
                append(name);
                append('>');
            }
            this.nspCounts[this.depth + 1] = this.nspCounts[this.depth];
            return this;
        }
        throw new IllegalArgumentException("</{" + namespace + "}" + name + "> does not match start");
    }

    public String getNamespace() {
        if (getDepth() == 0) {
            return null;
        }
        return this.elementStack[(getDepth() * 3) - 3];
    }

    public String getName() {
        if (getDepth() == 0) {
            return null;
        }
        return this.elementStack[(getDepth() * 3) - 1];
    }

    public int getDepth() {
        return this.pending ? this.depth + 1 : this.depth;
    }

    public XmlSerializer text(String text) throws IOException {
        check(false);
        this.indent[this.depth] = false;
        writeEscaped(text, -1);
        return this;
    }

    public XmlSerializer text(char[] text, int start, int len) throws IOException {
        text(new String(text, start, len));
        return this;
    }

    public void cdsect(String data) throws IOException {
        check(false);
        String data2 = data.replace("]]>", "]]]]><![CDATA[>");
        append("<![CDATA[");
        int i = 0;
        while (i < data2.length()) {
            char ch = data2.charAt(i);
            if ((ch >= ' ' && ch <= 55295) || ch == 9 || ch == 10 || ch == 13 || (ch >= 57344 && ch <= 65533)) {
                append(ch);
            } else if (!Character.isHighSurrogate(ch) || i >= data2.length() - 1) {
                reportInvalidCharacter(ch);
            } else {
                append("]]>");
                i++;
                writeSurrogate(ch, data2.charAt(i));
                append("<![CDATA[");
            }
            i++;
        }
        append("]]>");
    }

    private void writeSurrogate(char high, char low) throws IOException {
        if (Character.isLowSurrogate(low)) {
            int codePoint = Character.toCodePoint(high, low);
            append("&#" + codePoint + ";");
            return;
        }
        throw new IllegalArgumentException("Bad surrogate pair (U+" + Integer.toHexString(high) + " U+" + Integer.toHexString(low) + ")");
    }

    public void comment(String comment) throws IOException {
        check(false);
        append("<!--");
        append(comment);
        append("-->");
    }

    public void processingInstruction(String pi) throws IOException {
        check(false);
        append("<?");
        append(pi);
        append("?>");
    }
}
