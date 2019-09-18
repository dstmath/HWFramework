package java.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Types;
import sun.misc.DoubleConsts;

public class Properties extends Hashtable<Object, Object> {
    private static final char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final long serialVersionUID = 4112578634029874840L;
    protected Properties defaults;

    class LineReader {
        byte[] inByteBuf;
        char[] inCharBuf;
        int inLimit = 0;
        int inOff = 0;
        InputStream inStream;
        char[] lineBuf = new char[1024];
        Reader reader;

        public LineReader(InputStream inStream2) {
            this.inStream = inStream2;
            this.inByteBuf = new byte[8192];
        }

        public LineReader(Reader reader2) {
            this.reader = reader2;
            this.inCharBuf = new char[8192];
        }

        /* access modifiers changed from: package-private */
        public int readLine() throws IOException {
            char c;
            int i;
            int i2;
            boolean precedingBackslash = false;
            boolean appendedLineBegin = false;
            boolean isNewLine = true;
            boolean isCommentLine = false;
            boolean skipWhiteSpace = true;
            int len = 0;
            boolean skipLF = false;
            while (true) {
                if (this.inOff >= this.inLimit) {
                    if (this.inStream == null) {
                        i2 = this.reader.read(this.inCharBuf);
                    } else {
                        i2 = this.inStream.read(this.inByteBuf);
                    }
                    this.inLimit = i2;
                    this.inOff = 0;
                    if (this.inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        if (precedingBackslash) {
                            len--;
                        }
                        return len;
                    }
                }
                if (this.inStream != null) {
                    byte[] bArr = this.inByteBuf;
                    int i3 = this.inOff;
                    this.inOff = i3 + 1;
                    c = (char) (255 & bArr[i3]);
                } else {
                    char[] cArr = this.inCharBuf;
                    int i4 = this.inOff;
                    this.inOff = i4 + 1;
                    c = cArr[i4];
                }
                if (skipLF) {
                    skipLF = false;
                    if (c == 10) {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (!Character.isWhitespace(c) && (appendedLineBegin || !(c == 13 || c == 10))) {
                        skipWhiteSpace = false;
                        appendedLineBegin = false;
                    }
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                    }
                }
                if (c != 10 && c != 13) {
                    int len2 = len + 1;
                    this.lineBuf[len] = c;
                    if (len2 == this.lineBuf.length) {
                        int newLength = this.lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy((Object) this.lineBuf, 0, (Object) buf, 0, this.lineBuf.length);
                        this.lineBuf = buf;
                    }
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                    len = len2;
                } else if (isCommentLine || len == 0) {
                    isCommentLine = false;
                    isNewLine = true;
                    skipWhiteSpace = true;
                    len = 0;
                } else {
                    if (this.inOff >= this.inLimit) {
                        if (this.inStream == null) {
                            i = this.reader.read(this.inCharBuf);
                        } else {
                            i = this.inStream.read(this.inByteBuf);
                        }
                        this.inLimit = i;
                        this.inOff = 0;
                        if (this.inLimit <= 0) {
                            if (precedingBackslash) {
                                len--;
                            }
                            return len;
                        }
                    }
                    if (!precedingBackslash) {
                        return len;
                    }
                    len--;
                    skipWhiteSpace = true;
                    appendedLineBegin = true;
                    precedingBackslash = false;
                    if (c == 13) {
                        skipLF = true;
                    }
                }
            }
        }
    }

    public Properties() {
        this(null);
    }

    public Properties(Properties defaults2) {
        this.defaults = defaults2;
    }

    public synchronized Object setProperty(String key, String value) {
        return put(key, value);
    }

    public synchronized void load(Reader reader) throws IOException {
        load0(new LineReader(reader));
    }

    public synchronized void load(InputStream inStream) throws IOException {
        load0(new LineReader(inStream));
    }

    private void load0(LineReader lr) throws IOException {
        char[] convtBuf = new char[1024];
        while (true) {
            int readLine = lr.readLine();
            int limit = readLine;
            if (readLine >= 0) {
                int keyLen = 0;
                int valueStart = limit;
                boolean hasSep = false;
                boolean precedingBackslash = false;
                while (true) {
                    if (keyLen >= limit) {
                        break;
                    }
                    char c = lr.lineBuf[keyLen];
                    if ((c != '=' && c != ':') || precedingBackslash) {
                        if (Character.isWhitespace(c) && !precedingBackslash) {
                            valueStart = keyLen + 1;
                            break;
                        }
                        if (c == '\\') {
                            precedingBackslash = !precedingBackslash;
                        } else {
                            precedingBackslash = false;
                        }
                        keyLen++;
                    } else {
                        valueStart = keyLen + 1;
                        hasSep = true;
                        break;
                    }
                }
                while (valueStart < limit) {
                    char c2 = lr.lineBuf[valueStart];
                    if (!Character.isWhitespace(c2)) {
                        if (hasSep || (c2 != '=' && c2 != ':')) {
                            break;
                        }
                        hasSep = true;
                    }
                    valueStart++;
                }
                put(loadConvert(lr.lineBuf, 0, keyLen, convtBuf), loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf));
            } else {
                return;
            }
        }
    }

    private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
        int value;
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;
        while (true) {
            int i = 0;
            if (off >= end) {
                return new String(out, 0, outLen);
            }
            int off2 = off + 1;
            char aChar = in[off];
            if (aChar == '\\') {
                int off3 = off2 + 1;
                char aChar2 = in[off2];
                if (aChar2 == 'u') {
                    int value2 = aChar2;
                    off = off3;
                    int value3 = 0;
                    while (i < 4) {
                        int off4 = off + 1;
                        char aChar3 = in[off];
                        switch (aChar3) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = ((value3 << 4) + aChar3) - 48;
                                break;
                            default:
                                switch (aChar3) {
                                    case 'A':
                                    case 'B':
                                    case 'C':
                                    case 'D':
                                    case 'E':
                                    case Types.DATALINK:
                                        value = (((value3 << 4) + 10) + aChar3) - 65;
                                        break;
                                    default:
                                        switch (aChar3) {
                                            case 'a':
                                            case 'b':
                                            case 'c':
                                            case 'd':
                                            case 'e':
                                            case 'f':
                                                value = (((value3 << 4) + 10) + aChar3) - 97;
                                                break;
                                            default:
                                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                                        }
                                }
                        }
                        value3 = value;
                        i++;
                        off = off4;
                    }
                    out[outLen] = (char) value3;
                    outLen++;
                } else {
                    if (aChar2 == 't') {
                        aChar2 = 9;
                    } else if (aChar2 == 'r') {
                        aChar2 = 13;
                    } else if (aChar2 == 'n') {
                        aChar2 = 10;
                    } else if (aChar2 == 'f') {
                        aChar2 = 12;
                    }
                    out[outLen] = aChar2;
                    outLen++;
                    off = off3;
                }
            } else {
                out[outLen] = aChar;
                outLen++;
                off = off2;
            }
        }
    }

    private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);
        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            if (aChar <= '=' || aChar >= 127) {
                switch (aChar) {
                    case 9:
                        outBuffer.append('\\');
                        outBuffer.append('t');
                        break;
                    case 10:
                        outBuffer.append('\\');
                        outBuffer.append('n');
                        break;
                    case 12:
                        outBuffer.append('\\');
                        outBuffer.append('f');
                        break;
                    case 13:
                        outBuffer.append('\\');
                        outBuffer.append('r');
                        break;
                    case ' ':
                        if (x == 0 || escapeSpace) {
                            outBuffer.append('\\');
                        }
                        outBuffer.append(' ');
                        break;
                    case '!':
                    case '#':
                    case ':':
                    case '=':
                        outBuffer.append('\\');
                        outBuffer.append(aChar);
                        break;
                    default:
                        if (!(aChar < ' ' || aChar > '~') || !escapeUnicode) {
                            outBuffer.append(aChar);
                            break;
                        } else {
                            outBuffer.append('\\');
                            outBuffer.append('u');
                            outBuffer.append(toHex((aChar >> 12) & 15));
                            outBuffer.append(toHex((aChar >> 8) & 15));
                            outBuffer.append(toHex((aChar >> 4) & 15));
                            outBuffer.append(toHex(aChar & 15));
                            break;
                        }
                }
            } else if (aChar == '\\') {
                outBuffer.append('\\');
                outBuffer.append('\\');
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    private static void writeComments(BufferedWriter bw, String comments) throws IOException {
        bw.write("#");
        int len = comments.length();
        int current = 0;
        int last = 0;
        char[] uu = new char[6];
        uu[0] = '\\';
        uu[1] = 'u';
        while (current < len) {
            char c = comments.charAt(current);
            if (c > 255 || c == 10 || c == 13) {
                if (last != current) {
                    bw.write(comments.substring(last, current));
                }
                if (c > 255) {
                    uu[2] = toHex((c >> 12) & 15);
                    uu[3] = toHex((c >> 8) & 15);
                    uu[4] = toHex((c >> 4) & 15);
                    uu[5] = toHex(c & 15);
                    bw.write(new String(uu));
                } else {
                    bw.newLine();
                    if (c == 13 && current != len - 1 && comments.charAt(current + 1) == 10) {
                        current++;
                    }
                    if (current == len - 1 || !(comments.charAt(current + 1) == '#' || comments.charAt(current + 1) == '!')) {
                        bw.write("#");
                    }
                }
                last = current + 1;
            }
            current++;
        }
        if (last != current) {
            bw.write(comments.substring(last, current));
        }
        bw.newLine();
    }

    @Deprecated
    public void save(OutputStream out, String comments) {
        try {
            store(out, comments);
        } catch (IOException e) {
        }
    }

    public void store(Writer writer, String comments) throws IOException {
        BufferedWriter bufferedWriter;
        if (writer instanceof BufferedWriter) {
            bufferedWriter = (BufferedWriter) writer;
        } else {
            bufferedWriter = new BufferedWriter(writer);
        }
        store0(bufferedWriter, comments, false);
    }

    public void store(OutputStream out, String comments) throws IOException {
        store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")), comments, true);
    }

    private void store0(BufferedWriter bw, String comments, boolean escUnicode) throws IOException {
        if (comments != null) {
            writeComments(bw, comments);
        }
        bw.write("#" + new Date().toString());
        bw.newLine();
        synchronized (this) {
            Enumeration<?> e = keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String key2 = saveConvert(key, true, escUnicode);
                String val = saveConvert((String) get(key), false, escUnicode);
                bw.write(key2 + "=" + val);
                bw.newLine();
            }
        }
        bw.flush();
    }

    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        XMLUtils.load(this, (InputStream) Objects.requireNonNull(in));
        in.close();
    }

    public void storeToXML(OutputStream os, String comment) throws IOException {
        storeToXML(os, comment, "UTF-8");
    }

    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        XMLUtils.save(this, (OutputStream) Objects.requireNonNull(os), comment, (String) Objects.requireNonNull(encoding));
    }

    public String getProperty(String key) {
        Object oval = super.get(key);
        String sval = oval instanceof String ? (String) oval : null;
        return (sval != null || this.defaults == null) ? sval : this.defaults.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return val == null ? defaultValue : val;
    }

    public Enumeration<?> propertyNames() {
        Hashtable<String, Object> h = new Hashtable<>();
        enumerate(h);
        return h.keys();
    }

    public Set<String> stringPropertyNames() {
        Hashtable<String, String> h = new Hashtable<>();
        enumerateStringProperties(h);
        return h.keySet();
    }

    public void list(PrintStream out) {
        out.println("-- listing properties --");
        Hashtable<String, Object> h = new Hashtable<>();
        enumerate(h);
        Enumeration<String> e = h.keys();
        while (e.hasMoreElements()) {
            String val = (String) h.get(e.nextElement());
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        Hashtable<String, Object> h = new Hashtable<>();
        enumerate(h);
        Enumeration<String> e = h.keys();
        while (e.hasMoreElements()) {
            String val = (String) h.get(e.nextElement());
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    private synchronized void enumerate(Hashtable<String, Object> h) {
        if (this.defaults != null) {
            this.defaults.enumerate(h);
        }
        Enumeration<?> e = keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            h.put(key, get(key));
        }
    }

    private synchronized void enumerateStringProperties(Hashtable<String, String> h) {
        if (this.defaults != null) {
            this.defaults.enumerateStringProperties(h);
        }
        Enumeration<?> e = keys();
        while (e.hasMoreElements()) {
            Object nextElement = e.nextElement();
            Object v = get(nextElement);
            if ((nextElement instanceof String) && (v instanceof String)) {
                h.put((String) nextElement, (String) v);
            }
        }
    }

    private static char toHex(int nibble) {
        return hexDigit[nibble & 15];
    }
}
