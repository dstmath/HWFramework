package sun.security.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.security.AccessController;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sun.security.action.GetBooleanAction;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class AVA implements DerEncoder {
    static final int DEFAULT = 1;
    private static final boolean PRESERVE_OLD_DC_ENCODING = ((Boolean) AccessController.doPrivileged(new GetBooleanAction("com.sun.security.preserveOldDCEncoding"))).booleanValue();
    static final int RFC1779 = 2;
    static final int RFC2253 = 3;
    private static final Debug debug = Debug.getInstance(X509CertImpl.NAME, "\t[AVA]");
    private static final String escapedDefault = ",+<>;\"";
    private static final String hexDigits = "0123456789ABCDEF";
    private static final String specialChars1779 = ",=\n+<>#;\\\"";
    private static final String specialChars2253 = ",=+<>#;\\\"";
    private static final String specialCharsDefault = ",=\n+<>#;\\\" ";
    final ObjectIdentifier oid;
    final DerValue value;

    public AVA(ObjectIdentifier type, DerValue val) {
        if (type == null || val == null) {
            throw new NullPointerException();
        }
        this.oid = type;
        this.value = val;
    }

    AVA(Reader in) throws IOException {
        this(in, 1);
    }

    AVA(Reader in, Map<String, String> keywordMap) throws IOException {
        this(in, 1, keywordMap);
    }

    AVA(Reader in, int format) throws IOException {
        this(in, format, Collections.emptyMap());
    }

    AVA(Reader in, int format, Map<String, String> keywordMap) throws IOException {
        int c;
        StringBuilder temp = new StringBuilder();
        while (true) {
            int c2 = readChar(in, "Incorrect AVA format");
            if (c2 == 61) {
                break;
            }
            temp.append((char) c2);
        }
        this.oid = AVAKeyword.getOID(temp.toString(), format, keywordMap);
        temp.setLength(0);
        if (format != 3) {
            while (true) {
                c = in.read();
                if (c != 32 && c != 10) {
                    break;
                }
            }
        } else {
            c = in.read();
            if (c == 32) {
                throw new IOException("Incorrect AVA RFC2253 format - leading space must be escaped");
            }
        }
        if (c == -1) {
            this.value = new DerValue("");
            return;
        }
        if (c == 35) {
            this.value = parseHexString(in, format);
        } else if (c != 34 || format == 3) {
            this.value = parseString(in, c, format, temp);
        } else {
            this.value = parseQuotedString(in, temp);
        }
    }

    public ObjectIdentifier getObjectIdentifier() {
        return this.oid;
    }

    public DerValue getDerValue() {
        return this.value;
    }

    public String getValueString() {
        try {
            String s = this.value.getAsString();
            if (s != null) {
                return s;
            }
            throw new RuntimeException("AVA string is null");
        } catch (IOException e) {
            throw new RuntimeException("AVA error: " + e, e);
        }
    }

    private static DerValue parseHexString(Reader in, int format) throws IOException {
        int c;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b = 0;
        int cNdx = 0;
        while (true) {
            c = in.read();
            if (isTerminator(c, format)) {
                break;
            } else if (c != 32 && c != 10) {
                int cVal = hexDigits.indexOf((int) Character.toUpperCase((char) c));
                if (cVal != -1) {
                    if (cNdx % 2 == 1) {
                        b = (byte) ((b * 16) + ((byte) cVal));
                        baos.write(b);
                    } else {
                        b = (byte) cVal;
                    }
                    cNdx++;
                } else {
                    throw new IOException("AVA parse, invalid hex digit: " + ((char) c));
                }
            }
        }
        do {
            if (c == 32 || c == 10) {
                c = in.read();
            } else {
                throw new IOException("AVA parse, invalid hex digit: " + ((char) c));
            }
        } while (!isTerminator(c, format));
        if (cNdx == 0) {
            throw new IOException("AVA parse, zero hex digits");
        } else if (cNdx % 2 != 1) {
            return new DerValue(baos.toByteArray());
        } else {
            throw new IOException("AVA parse, odd number of hex digits");
        }
    }

    private DerValue parseQuotedString(Reader in, StringBuilder temp) throws IOException {
        int c;
        int c2 = readChar(in, "Quoted string did not end in quote");
        List<Byte> embeddedHex = new ArrayList<>();
        boolean isPrintableString = true;
        while (c2 != 34) {
            if (c2 == 92) {
                c2 = readChar(in, "Quoted string did not end in quote");
                Byte embeddedHexPair = getEmbeddedHexPair(c2, in);
                Byte hexByte = embeddedHexPair;
                if (embeddedHexPair != null) {
                    isPrintableString = PRESERVE_OLD_DC_ENCODING;
                    embeddedHex.add(hexByte);
                    c2 = in.read();
                } else if (specialChars1779.indexOf((int) (char) c2) < 0) {
                    throw new IOException("Invalid escaped character in AVA: " + ((char) c2));
                }
            }
            if (embeddedHex.size() > 0) {
                temp.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            isPrintableString &= DerValue.isPrintableStringChar((char) c2);
            temp.append((char) c2);
            c2 = readChar(in, "Quoted string did not end in quote");
        }
        if (embeddedHex.size() > 0) {
            temp.append(getEmbeddedHexString(embeddedHex));
            embeddedHex.clear();
        }
        while (true) {
            c = in.read();
            if (c != 10 && c != 32) {
                break;
            }
        }
        if (c != -1) {
            throw new IOException("AVA had characters other than whitespace after terminating quote");
        } else if (this.oid.equals((Object) PKCS9Attribute.EMAIL_ADDRESS_OID) || (this.oid.equals((Object) X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING)) {
            return new DerValue((byte) 22, temp.toString());
        } else {
            if (isPrintableString) {
                return new DerValue(temp.toString());
            }
            return new DerValue((byte) 12, temp.toString());
        }
    }

    private DerValue parseString(Reader in, int c, int format, StringBuilder temp) throws IOException {
        Reader reader = in;
        int i = format;
        StringBuilder sb = temp;
        List<Byte> embeddedHex = new ArrayList<>();
        boolean isPrintableString = true;
        boolean leadingChar = true;
        int spaceCount = 0;
        int c2 = c;
        do {
            boolean escape = PRESERVE_OLD_DC_ENCODING;
            if (c2 == 92) {
                escape = true;
                c2 = readChar(reader, "Invalid trailing backslash");
                Byte embeddedHexPair = getEmbeddedHexPair(c2, reader);
                Byte hexByte = embeddedHexPair;
                if (embeddedHexPair != null) {
                    isPrintableString = PRESERVE_OLD_DC_ENCODING;
                    embeddedHex.add(hexByte);
                    c2 = in.read();
                    leadingChar = PRESERVE_OLD_DC_ENCODING;
                } else if (i == 1 && specialCharsDefault.indexOf((int) (char) c2) == -1) {
                    throw new IOException("Invalid escaped character in AVA: '" + ((char) c2) + "'");
                } else if (i == 3) {
                    if (c2 == 32) {
                        if (!leadingChar && !trailingSpace(in)) {
                            throw new IOException("Invalid escaped space character in AVA.  Only a leading or trailing space character can be escaped.");
                        }
                    } else if (c2 == 35) {
                        if (!leadingChar) {
                            throw new IOException("Invalid escaped '#' character in AVA.  Only a leading '#' can be escaped.");
                        }
                    } else if (specialChars2253.indexOf((int) (char) c2) == -1) {
                        throw new IOException("Invalid escaped character in AVA: '" + ((char) c2) + "'");
                    }
                }
            } else if (i == 3 && specialChars2253.indexOf((int) (char) c2) != -1) {
                throw new IOException("Character '" + ((char) c2) + "' in AVA appears without escape");
            }
            if (embeddedHex.size() > 0) {
                for (int i2 = 0; i2 < spaceCount; i2++) {
                    sb.append(" ");
                }
                spaceCount = 0;
                sb.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            boolean isPrintableString2 = DerValue.isPrintableStringChar((char) c2) & isPrintableString;
            if (c2 != 32 || escape) {
                for (int i3 = 0; i3 < spaceCount; i3++) {
                    sb.append(" ");
                }
                spaceCount = 0;
                sb.append((char) c2);
            } else {
                spaceCount++;
            }
            c2 = in.read();
            leadingChar = false;
            isPrintableString = isPrintableString2;
        } while (!isTerminator(c2, i));
        if (i != 3 || spaceCount <= 0) {
            if (embeddedHex.size() > 0) {
                sb.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            if (this.oid.equals((Object) PKCS9Attribute.EMAIL_ADDRESS_OID) || (this.oid.equals((Object) X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING)) {
                return new DerValue((byte) 22, temp.toString());
            }
            if (isPrintableString) {
                return new DerValue(temp.toString());
            }
            return new DerValue((byte) 12, temp.toString());
        }
        throw new IOException("Incorrect AVA RFC2253 format - trailing space must be escaped");
    }

    private static Byte getEmbeddedHexPair(int c1, Reader in) throws IOException {
        if (hexDigits.indexOf((int) Character.toUpperCase((char) c1)) < 0) {
            return null;
        }
        int c2 = readChar(in, "unexpected EOF - escaped hex value must include two valid digits");
        if (hexDigits.indexOf((int) Character.toUpperCase((char) c2)) >= 0) {
            return new Byte((byte) ((Character.digit((char) c1, 16) << 4) + Character.digit((char) c2, 16)));
        }
        throw new IOException("escaped hex value must include two valid digits");
    }

    private static String getEmbeddedHexString(List<Byte> hexList) throws IOException {
        int n = hexList.size();
        byte[] hexBytes = new byte[n];
        for (int i = 0; i < n; i++) {
            hexBytes[i] = hexList.get(i).byteValue();
        }
        return new String(hexBytes, "UTF8");
    }

    private static boolean isTerminator(int ch, int format) {
        boolean z = true;
        if (ch != -1) {
            if (ch != 59) {
                switch (ch) {
                    case 43:
                    case 44:
                        break;
                    default:
                        return PRESERVE_OLD_DC_ENCODING;
                }
            } else {
                if (format == 3) {
                    z = false;
                }
                return z;
            }
        }
        return true;
    }

    private static int readChar(Reader in, String errMsg) throws IOException {
        int c = in.read();
        if (c != -1) {
            return c;
        }
        throw new IOException(errMsg);
    }

    private static boolean trailingSpace(Reader in) throws IOException {
        boolean trailing;
        if (!in.markSupported()) {
            return true;
        }
        in.mark(9999);
        while (true) {
            int nextChar = in.read();
            if (nextChar == -1) {
                trailing = true;
                break;
            } else if (nextChar != 32) {
                if (nextChar != 92) {
                    trailing = PRESERVE_OLD_DC_ENCODING;
                    break;
                } else if (in.read() != 32) {
                    trailing = PRESERVE_OLD_DC_ENCODING;
                    break;
                }
            }
        }
        in.reset();
        return trailing;
    }

    AVA(DerValue derval) throws IOException {
        if (derval.tag == 48) {
            this.oid = X500Name.intern(derval.data.getOID());
            this.value = derval.data.getDerValue();
            if (derval.data.available() != 0) {
                throw new IOException("AVA, extra bytes = " + derval.data.available());
            }
            return;
        }
        throw new IOException("AVA not a sequence");
    }

    AVA(DerInputStream in) throws IOException {
        this(in.getDerValue());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AVA)) {
            return PRESERVE_OLD_DC_ENCODING;
        }
        return toRFC2253CanonicalString().equals(((AVA) obj).toRFC2253CanonicalString());
    }

    public int hashCode() {
        return toRFC2253CanonicalString().hashCode();
    }

    public void encode(DerOutputStream out) throws IOException {
        derEncode(out);
    }

    public void derEncode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream tmp2 = new DerOutputStream();
        tmp.putOID(this.oid);
        this.value.encode(tmp);
        tmp2.write((byte) 48, tmp);
        out.write(tmp2.toByteArray());
    }

    private String toKeyword(int format, Map<String, String> oidMap) {
        return AVAKeyword.getKeyword(this.oid, format, oidMap);
    }

    public String toString() {
        return toKeywordValueString(toKeyword(1, Collections.emptyMap()));
    }

    public String toRFC1779String() {
        return toRFC1779String(Collections.emptyMap());
    }

    public String toRFC1779String(Map<String, String> oidMap) {
        return toKeywordValueString(toKeyword(2, oidMap));
    }

    public String toRFC2253String() {
        return toRFC2253String(Collections.emptyMap());
    }

    public String toRFC2253String(Map<String, String> oidMap) {
        StringBuilder typeAndValue = new StringBuilder(100);
        typeAndValue.append(toKeyword(3, oidMap));
        typeAndValue.append('=');
        int j = 0;
        if ((typeAndValue.charAt(0) < '0' || typeAndValue.charAt(0) > '9') && isDerString(this.value, PRESERVE_OLD_DC_ENCODING)) {
            try {
                String valStr = new String(this.value.getDataBytes(), "UTF8");
                StringBuilder sbuffer = new StringBuilder();
                int i = 0;
                while (i < valStr.length()) {
                    char c = valStr.charAt(i);
                    if (DerValue.isPrintableStringChar(c) || ",=+<>#;\"\\".indexOf((int) c) >= 0) {
                        if (",=+<>#;\"\\".indexOf((int) c) >= 0) {
                            sbuffer.append('\\');
                        }
                        sbuffer.append(c);
                    } else if (c == 0) {
                        sbuffer.append("\\00");
                    } else if (debug == null || !Debug.isOn("ava")) {
                        sbuffer.append(c);
                    } else {
                        try {
                            byte[] valueBytes = Character.toString(c).getBytes("UTF8");
                            for (int j2 = j; j2 < valueBytes.length; j2++) {
                                sbuffer.append('\\');
                                sbuffer.append(Character.toUpperCase(Character.forDigit((valueBytes[j2] >>> 4) & 15, 16)));
                                sbuffer.append(Character.toUpperCase(Character.forDigit(valueBytes[j2] & 15, 16)));
                            }
                        } catch (IOException e) {
                            throw new IllegalArgumentException("DER Value conversion");
                        }
                    }
                    i++;
                    j = 0;
                }
                char[] chars = sbuffer.toString().toCharArray();
                StringBuilder sbuffer2 = new StringBuilder();
                int lead = 0;
                while (lead < chars.length && (chars[lead] == ' ' || chars[lead] == 13)) {
                    lead++;
                }
                int trail = chars.length - 1;
                while (trail >= 0 && (chars[trail] == ' ' || chars[trail] == 13)) {
                    trail--;
                }
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= chars.length) {
                        break;
                    }
                    char c2 = chars[i3];
                    if (i3 < lead || i3 > trail) {
                        sbuffer2.append('\\');
                    }
                    sbuffer2.append(c2);
                    i2 = i3 + 1;
                }
                typeAndValue.append(sbuffer2.toString());
            } catch (IOException e2) {
                throw new IllegalArgumentException("DER Value conversion");
            }
        } else {
            try {
                byte[] data = this.value.toByteArray();
                typeAndValue.append('#');
                while (j < data.length) {
                    byte b = data[j];
                    typeAndValue.append(Character.forDigit((b >>> 4) & 15, 16));
                    typeAndValue.append(Character.forDigit(15 & b, 16));
                    j++;
                }
            } catch (IOException e3) {
                throw new IllegalArgumentException("DER Value conversion");
            }
        }
        return typeAndValue.toString();
    }

    public String toRFC2253CanonicalString() {
        boolean previousWhite;
        StringBuilder typeAndValue = new StringBuilder(40);
        typeAndValue.append(toKeyword(3, Collections.emptyMap()));
        typeAndValue.append('=');
        if ((typeAndValue.charAt(0) < '0' || typeAndValue.charAt(0) > '9') && (isDerString(this.value, true) || this.value.tag == 20)) {
            try {
                String valStr = new String(this.value.getDataBytes(), "UTF8");
                StringBuilder sbuffer = new StringBuilder();
                boolean previousWhite2 = false;
                for (int i = 0; i < valStr.length(); i++) {
                    char c = valStr.charAt(i);
                    if (DerValue.isPrintableStringChar(c) || ",+<>;\"\\".indexOf((int) c) >= 0 || (i == 0 && c == '#')) {
                        if ((i == 0 && c == '#') || ",+<>;\"\\".indexOf((int) c) >= 0) {
                            sbuffer.append('\\');
                        }
                        if (!Character.isWhitespace(c)) {
                            previousWhite = PRESERVE_OLD_DC_ENCODING;
                            sbuffer.append(c);
                        } else if (!previousWhite2) {
                            previousWhite = true;
                            sbuffer.append(c);
                        }
                    } else if (debug == null || !Debug.isOn("ava")) {
                        previousWhite = PRESERVE_OLD_DC_ENCODING;
                        sbuffer.append(c);
                    } else {
                        previousWhite2 = PRESERVE_OLD_DC_ENCODING;
                        try {
                            byte[] valueBytes = Character.toString(c).getBytes("UTF8");
                            for (int j = 0; j < valueBytes.length; j++) {
                                sbuffer.append('\\');
                                sbuffer.append(Character.forDigit((valueBytes[j] >>> 4) & 15, 16));
                                sbuffer.append(Character.forDigit(valueBytes[j] & 15, 16));
                            }
                        } catch (IOException e) {
                            throw new IllegalArgumentException("DER Value conversion");
                        }
                    }
                    previousWhite2 = previousWhite;
                }
                typeAndValue.append(sbuffer.toString().trim());
            } catch (IOException e2) {
                throw new IllegalArgumentException("DER Value conversion");
            }
        } else {
            try {
                byte[] data = this.value.toByteArray();
                typeAndValue.append('#');
                for (byte b : data) {
                    typeAndValue.append(Character.forDigit((b >>> 4) & 15, 16));
                    typeAndValue.append(Character.forDigit(15 & b, 16));
                }
            } catch (IOException e3) {
                throw new IllegalArgumentException("DER Value conversion");
            }
        }
        return Normalizer.normalize(typeAndValue.toString().toUpperCase(Locale.US).toLowerCase(Locale.US), Normalizer.Form.NFKD);
    }

    private static boolean isDerString(DerValue value2, boolean canonical) {
        if (canonical) {
            byte b = value2.tag;
            if (b == 12 || b == 19) {
                return true;
            }
            return PRESERVE_OLD_DC_ENCODING;
        }
        byte b2 = value2.tag;
        if (!(b2 == 12 || b2 == 22 || b2 == 27 || b2 == 30)) {
            switch (b2) {
                case 19:
                case 20:
                    break;
                default:
                    return PRESERVE_OLD_DC_ENCODING;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean hasRFC2253Keyword() {
        return AVAKeyword.hasKeyword(this.oid, 3);
    }

    private String toKeywordValueString(String keyword) {
        String valStr;
        char c;
        char c2;
        char c3;
        boolean previousWhite;
        char c4;
        String valStr2;
        char c5;
        StringBuilder retval = new StringBuilder(40);
        retval.append(keyword);
        retval.append("=");
        try {
            String valStr3 = this.value.getAsString();
            char c6 = 15;
            if (valStr3 == null) {
                byte[] data = this.value.toByteArray();
                retval.append('#');
                for (int i = 0; i < data.length; i++) {
                    retval.append(hexDigits.charAt((data[i] >> 4) & 15));
                    retval.append(hexDigits.charAt(data[i] & 15));
                }
            } else {
                StringBuilder sbuffer = new StringBuilder();
                int length = valStr3.length();
                char c7 = '\"';
                boolean alreadyQuoted = length > 1 && valStr3.charAt(0) == '\"' && valStr3.charAt(length + -1) == '\"';
                boolean previousWhite2 = false;
                boolean quoteNeeded = false;
                int i2 = 0;
                while (i2 < length) {
                    char c8 = valStr3.charAt(i2);
                    if (alreadyQuoted) {
                        if (i2 != 0) {
                            if (i2 != length - 1) {
                                c3 = c8;
                            }
                        }
                        sbuffer.append(c8);
                        valStr = valStr3;
                        c = c6;
                        c2 = c7;
                        i2++;
                        c7 = c2;
                        c6 = c;
                        valStr3 = valStr;
                    } else {
                        c3 = c8;
                    }
                    char c9 = '\\';
                    if (DerValue.isPrintableStringChar(c3)) {
                        valStr2 = valStr3;
                        c5 = c6;
                    } else if (",+=\n<>#;\\\"".indexOf((int) c3) >= 0) {
                        valStr2 = valStr3;
                        c5 = c6;
                    } else {
                        if (debug == null || !Debug.isOn("ava")) {
                            valStr = valStr3;
                            c = c6;
                            sbuffer.append(c3);
                            previousWhite2 = false;
                        } else {
                            byte[] valueBytes = Character.toString(c3).getBytes("UTF8");
                            int j = 0;
                            while (j < valueBytes.length) {
                                sbuffer.append(c9);
                                sbuffer.append(Character.toUpperCase(Character.forDigit(15 & (valueBytes[j] >>> 4), 16)));
                                sbuffer.append(Character.toUpperCase(Character.forDigit(15 & valueBytes[j], 16)));
                                j++;
                                valStr3 = valStr3;
                                c9 = '\\';
                            }
                            valStr = valStr3;
                            c = 15;
                            previousWhite2 = false;
                        }
                        c2 = '\"';
                        i2++;
                        c7 = c2;
                        c6 = c;
                        valStr3 = valStr;
                    }
                    if (!quoteNeeded && ((i2 == 0 && (c3 == ' ' || c3 == 10)) || ",+=\n<>#;\\\"".indexOf((int) c3) >= 0)) {
                        quoteNeeded = true;
                    }
                    if (c3 == ' ' || c3 == 10) {
                        c2 = '\"';
                        if (!quoteNeeded && previousWhite2) {
                            quoteNeeded = true;
                        }
                        previousWhite = true;
                    } else {
                        c2 = '\"';
                        if (c3 != '\"') {
                            c4 = '\\';
                            if (c3 == '\\') {
                            }
                            previousWhite = PRESERVE_OLD_DC_ENCODING;
                        } else {
                            c4 = '\\';
                        }
                        sbuffer.append(c4);
                        previousWhite = PRESERVE_OLD_DC_ENCODING;
                    }
                    sbuffer.append(c3);
                    previousWhite2 = previousWhite;
                    i2++;
                    c7 = c2;
                    c6 = c;
                    valStr3 = valStr;
                }
                if (sbuffer.length() > 0) {
                    char trailChar = sbuffer.charAt(sbuffer.length() - 1);
                    if (trailChar == ' ' || trailChar == 10) {
                        quoteNeeded = true;
                    }
                }
                if (alreadyQuoted || !quoteNeeded) {
                    retval.append(sbuffer.toString());
                } else {
                    retval.append("\"" + sbuffer.toString() + "\"");
                }
            }
            return retval.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("DER Value conversion");
        }
    }
}
