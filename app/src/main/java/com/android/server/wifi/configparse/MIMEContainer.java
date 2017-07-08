package com.android.server.wifi.configparse;

import android.util.Log;
import com.android.server.wifi.hotspot2.Utils;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MIMEContainer {
    private static final String Boundary = "boundary=";
    private static final String CharsetTag = "charset=";
    private static final String Encoding = "Content-Transfer-Encoding";
    private static final String Type = "Content-Type";
    private final boolean mBase64;
    private final Charset mCharset;
    private final String mContentType;
    private final boolean mLast;
    private final List<MIMEContainer> mMimeContainers;
    private final boolean mMixed;
    private final String mText;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MIMEContainer(LineNumberReader in, String boundary) throws IOException {
        Map<String, List<String>> headers = parseHeader(in);
        List<String> type = (List) headers.get(Type);
        if (type == null || type.isEmpty()) {
            throw new IOException("Missing Content-Type @ " + in.getLineNumber());
        }
        boolean multiPart = false;
        boolean mixed = false;
        String str = null;
        Charset charset = StandardCharsets.ISO_8859_1;
        this.mContentType = (String) type.get(0);
        if (this.mContentType.startsWith("multipart/")) {
            multiPart = true;
            for (String attribute : type) {
                if (attribute.startsWith(Boundary)) {
                    str = Utils.unquote(attribute.substring(Boundary.length()));
                }
            }
            if (this.mContentType.endsWith("/mixed")) {
                mixed = true;
            }
        } else {
            if (this.mContentType.startsWith("text/")) {
                for (String attribute2 : type) {
                    if (attribute2.startsWith(CharsetTag)) {
                        charset = Charset.forName(attribute2.substring(CharsetTag.length()));
                    }
                }
            }
        }
        this.mMixed = mixed;
        this.mCharset = charset;
        if (!multiPart || str == null) {
            this.mMimeContainers = null;
        } else {
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("--") && line.length() == str.length() + 2) {
                    if (line.regionMatches(2, str, 0, str.length())) {
                        break;
                    }
                }
            }
            throw new IOException("Unexpected EOF before first boundary @ " + in.getLineNumber());
        }
        List<String> encoding = (List) headers.get(Encoding);
        boolean quoted = false;
        boolean base64 = false;
        if (encoding != null) {
            for (String text : encoding) {
                if (!text.equalsIgnoreCase("quoted-printable")) {
                    if (text.equalsIgnoreCase("base64")) {
                        base64 = true;
                        break;
                    }
                }
                quoted = true;
                break;
            }
        }
        this.mBase64 = base64;
        String hs2LogTag = Utils.hs2LogTag(getClass());
        String str2 = "%s MIME container, boundary '%s', type '%s', encoding %s";
        Object[] objArr = new Object[4];
        objArr[0] = multiPart ? "multipart" : "plain";
        objArr[1] = boundary;
        objArr[2] = this.mContentType;
        objArr[3] = encoding;
        Log.d(hs2LogTag, String.format(str2, objArr));
        AtomicBoolean eof = new AtomicBoolean();
        this.mText = recode(getBody(in, boundary, quoted, eof), charset);
        this.mLast = eof.get();
    }

    public List<MIMEContainer> getMimeContainers() {
        return this.mMimeContainers;
    }

    public String getText() {
        return this.mText;
    }

    public boolean isMixed() {
        return this.mMixed;
    }

    public boolean isBase64() {
        return this.mBase64;
    }

    public String getContentType() {
        return this.mContentType;
    }

    private boolean isLast() {
        return this.mLast;
    }

    private void toString(StringBuilder sb, int nesting) {
        char[] indent = new char[(nesting * 4)];
        Arrays.fill(indent, ' ');
        if (this.mBase64) {
            sb.append("base64, type ").append(this.mContentType).append('\n');
        } else if (this.mMimeContainers != null) {
            sb.append(indent).append("multipart/").append(this.mMixed ? "mixed" : "other").append('\n');
        } else {
            sb.append(indent).append(String.format("%s, type %s", new Object[]{this.mCharset, this.mContentType})).append('\n');
        }
        if (this.mMimeContainers != null) {
            for (MIMEContainer mimeContainer : this.mMimeContainers) {
                mimeContainer.toString(sb, nesting + 1);
            }
        }
        sb.append(indent).append("Text: ");
        if (this.mText.length() < 100000) {
            sb.append("'").append(this.mText).append("'\n");
        } else {
            sb.append(this.mText.length()).append(" chars\n");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Map<String, List<String>> parseHeader(LineNumberReader in) throws IOException {
        String line;
        int i = 0;
        StringBuilder value = null;
        String header = null;
        Map<String, List<String>> headers = new HashMap();
        while (true) {
            line = in.readLine();
            if (line == null) {
                break;
            } else if (line.length() == 0) {
                break;
            } else if (line.charAt(0) > ' ') {
                int nameEnd = line.indexOf(58);
                if (nameEnd < 0) {
                    break;
                }
                if (header != null) {
                    String[] values = value.toString().split(";");
                    List<String> valueList = new ArrayList(values.length);
                    for (String segment : values) {
                        valueList.add(segment.trim());
                    }
                    headers.put(header, valueList);
                }
                header = line.substring(0, nameEnd);
                value = new StringBuilder();
                value.append(line.substring(nameEnd + 1).trim());
            } else if (value == null) {
                break;
            } else {
                value.append(' ').append(line.trim());
            }
        }
        throw new IOException("Illegal blank prefix in header line '" + line + "' @ " + in.getLineNumber());
    }

    private static String getBody(LineNumberReader in, String boundary, boolean quoted, AtomicBoolean eof) throws IOException {
        StringBuilder text = new StringBuilder();
        while (true) {
            String line = in.readLine();
            if (line == null) {
                break;
            }
            Boolean end = boundaryCheck(line, boundary);
            if (end != null) {
                eof.set(end.booleanValue());
                return text.toString();
            } else if (!quoted) {
                text.append(line);
            } else if (line.endsWith("=")) {
                text.append(unescape(line.substring(line.length() - 1), in.getLineNumber()));
            } else {
                text.append(unescape(line, in.getLineNumber()));
            }
        }
        if (boundary == null) {
            return text.toString();
        }
        throw new IOException("Unexpected EOF file in body @ " + in.getLineNumber());
    }

    private static String recode(String s, Charset charset) {
        if (charset.equals(StandardCharsets.ISO_8859_1) || charset.equals(StandardCharsets.US_ASCII)) {
            return s;
        }
        return new String(s.getBytes(StandardCharsets.ISO_8859_1), charset);
    }

    private static Boolean boundaryCheck(String line, String boundary) {
        if (line.startsWith("--") && line.regionMatches(2, boundary, 0, boundary.length())) {
            if (line.length() == boundary.length() + 2) {
                return Boolean.FALSE;
            }
            if (line.length() == boundary.length() + 4 && line.endsWith("--")) {
                return Boolean.TRUE;
            }
        }
        return null;
    }

    private static String unescape(String text, int line) throws IOException {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        while (n < text.length()) {
            char ch = text.charAt(n);
            if (ch > '\u007f') {
                throw new IOException("Bad codepoint " + ch + " in quoted printable @ " + line);
            }
            if (ch != '=' || n >= text.length() - 2) {
                sb.append(ch);
            } else {
                int h1 = fromStrictHex(text.charAt(n + 1));
                int h2 = fromStrictHex(text.charAt(n + 2));
                if (h1 < 0 || h2 < 0) {
                    sb.append(ch);
                } else {
                    sb.append((char) ((h1 << 4) | h2));
                    n += 2;
                }
            }
            n++;
        }
        return sb.toString();
    }

    private static int fromStrictHex(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - 48;
        }
        if (ch < 'A' || ch > 'F') {
            return -1;
        }
        return (ch - 65) + 10;
    }
}
