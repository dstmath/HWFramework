package sun.security.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.security.AccessController;
import java.text.Normalizer;
import java.text.Normalizer.Form;
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
            c = readChar(in, "Incorrect AVA format");
            if (c == 61) {
                break;
            }
            temp.append((char) c);
        }
        this.oid = AVAKeyword.getOID(temp.-java_util_stream_Collectors-mthref-7(), format, keywordMap);
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
        } catch (Object e) {
            throw new RuntimeException("AVA error: " + e, e);
        }
    }

    private static DerValue parseHexString(Reader in, int format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b = 0;
        int cNdx = 0;
        while (true) {
            int c = in.read();
            if (isTerminator(c, format)) {
                break;
            } else if (c == 32 || c == 10) {
                while (true) {
                    if (c == 32 || c == 10) {
                        c = in.read();
                        if (isTerminator(c, format)) {
                            break;
                        }
                    } else {
                        throw new IOException("AVA parse, invalid hex digit: " + ((char) c));
                    }
                }
            } else {
                int cVal = hexDigits.indexOf(Character.toUpperCase((char) c));
                if (cVal == -1) {
                    throw new IOException("AVA parse, invalid hex digit: " + ((char) c));
                }
                if (cNdx % 2 == 1) {
                    b = (byte) ((b * 16) + ((byte) cVal));
                    baos.write(b);
                } else {
                    b = (byte) cVal;
                }
                cNdx++;
            }
        }
        if (cNdx == 0) {
            throw new IOException("AVA parse, zero hex digits");
        } else if (cNdx % 2 != 1) {
            return new DerValue(baos.toByteArray());
        } else {
            throw new IOException("AVA parse, odd number of hex digits");
        }
    }

    private DerValue parseQuotedString(Reader in, StringBuilder temp) throws IOException {
        int c = readChar(in, "Quoted string did not end in quote");
        List<Byte> embeddedHex = new ArrayList();
        int isPrintableString = 1;
        while (c != 34) {
            if (c == 92) {
                c = readChar(in, "Quoted string did not end in quote");
                Byte hexByte = getEmbeddedHexPair(c, in);
                if (hexByte != null) {
                    isPrintableString = 0;
                    embeddedHex.-java_util_stream_Collectors-mthref-2(hexByte);
                    c = in.read();
                } else if (specialChars1779.indexOf((char) c) < 0) {
                    throw new IOException("Invalid escaped character in AVA: " + ((char) c));
                }
            }
            if (embeddedHex.size() > 0) {
                temp.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            isPrintableString &= DerValue.isPrintableStringChar((char) c);
            temp.append((char) c);
            c = readChar(in, "Quoted string did not end in quote");
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
        } else if (this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID) || (this.oid.equals(X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING)) {
            return new DerValue((byte) 22, temp.-java_util_stream_Collectors-mthref-7());
        } else {
            if (isPrintableString != 0) {
                return new DerValue(temp.-java_util_stream_Collectors-mthref-7());
            }
            return new DerValue((byte) 12, temp.-java_util_stream_Collectors-mthref-7());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f6  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0125 A:{LOOP_END, LOOP:2: B:55:0x0123->B:56:0x0125} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private DerValue parseString(Reader in, int c, int format, StringBuilder temp) throws IOException {
        List<Byte> embeddedHex = new ArrayList();
        boolean isPrintableString = true;
        boolean leadingChar = true;
        int spaceCount = 0;
        do {
            boolean escape = PRESERVE_OLD_DC_ENCODING;
            int i;
            if (c == 92) {
                escape = true;
                c = readChar(in, "Invalid trailing backslash");
                Byte hexByte = getEmbeddedHexPair(c, in);
                if (hexByte != null) {
                    isPrintableString = PRESERVE_OLD_DC_ENCODING;
                    embeddedHex.-java_util_stream_Collectors-mthref-2(hexByte);
                    c = in.read();
                    leadingChar = PRESERVE_OLD_DC_ENCODING;
                } else if (format == 1 && specialCharsDefault.indexOf((char) c) == -1) {
                    throw new IOException("Invalid escaped character in AVA: '" + ((char) c) + "'");
                } else {
                    if (format == 3) {
                        if (c == 32) {
                            if (!(leadingChar || (trailingSpace(in) ^ 1) == 0)) {
                                throw new IOException("Invalid escaped space character in AVA.  Only a leading or trailing space character can be escaped.");
                            }
                        } else if (c == 35) {
                            if (!leadingChar) {
                                throw new IOException("Invalid escaped '#' character in AVA.  Only a leading '#' can be escaped.");
                            }
                        } else if (specialChars2253.indexOf((char) c) == -1) {
                            throw new IOException("Invalid escaped character in AVA: '" + ((char) c) + "'");
                        }
                    }
                    if (embeddedHex.size() > 0) {
                        for (i = 0; i < spaceCount; i++) {
                            temp.append(" ");
                        }
                        spaceCount = 0;
                        temp.append(getEmbeddedHexString(embeddedHex));
                        embeddedHex.clear();
                    }
                    isPrintableString &= DerValue.isPrintableStringChar((char) c);
                    if (c == 32 || escape) {
                        for (i = 0; i < spaceCount; i++) {
                            temp.append(" ");
                        }
                        spaceCount = 0;
                        temp.append((char) c);
                    } else {
                        spaceCount++;
                    }
                    c = in.read();
                    leadingChar = PRESERVE_OLD_DC_ENCODING;
                }
            } else {
                if (format == 3 && specialChars2253.indexOf((char) c) != -1) {
                    throw new IOException("Character '" + ((char) c) + "' in AVA appears without escape");
                }
                if (embeddedHex.size() > 0) {
                }
                isPrintableString &= DerValue.isPrintableStringChar((char) c);
                if (c == 32) {
                }
                while (i < spaceCount) {
                }
                spaceCount = 0;
                temp.append((char) c);
                c = in.read();
                leadingChar = PRESERVE_OLD_DC_ENCODING;
            }
        } while (!isTerminator(c, format));
        if (format != 3 || spaceCount <= 0) {
            if (embeddedHex.size() > 0) {
                temp.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            if (this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID) || (this.oid.equals(X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING)) {
                return new DerValue((byte) 22, temp.-java_util_stream_Collectors-mthref-7());
            }
            if (isPrintableString) {
                return new DerValue(temp.-java_util_stream_Collectors-mthref-7());
            }
            return new DerValue((byte) 12, temp.-java_util_stream_Collectors-mthref-7());
        }
        throw new IOException("Incorrect AVA RFC2253 format - trailing space must be escaped");
    }

    private static Byte getEmbeddedHexPair(int c1, Reader in) throws IOException {
        if (hexDigits.indexOf(Character.toUpperCase((char) c1)) < 0) {
            return null;
        }
        int c2 = readChar(in, "unexpected EOF - escaped hex value must include two valid digits");
        if (hexDigits.indexOf(Character.toUpperCase((char) c2)) >= 0) {
            return new Byte((byte) ((Character.digit((char) c1, 16) << 4) + Character.digit((char) c2, 16)));
        }
        throw new IOException("escaped hex value must include two valid digits");
    }

    private static String getEmbeddedHexString(List<Byte> hexList) throws IOException {
        int n = hexList.size();
        byte[] hexBytes = new byte[n];
        for (int i = 0; i < n; i++) {
            hexBytes[i] = ((Byte) hexList.get(i)).byteValue();
        }
        return new String(hexBytes, "UTF8");
    }

    private static boolean isTerminator(int ch, int format) {
        boolean z = true;
        switch (ch) {
            case -1:
            case 43:
            case 44:
                return true;
            case 59:
                if (format == 3) {
                    z = PRESERVE_OLD_DC_ENCODING;
                }
                return z;
            default:
                return PRESERVE_OLD_DC_ENCODING;
        }
    }

    private static int readChar(Reader in, String errMsg) throws IOException {
        int c = in.read();
        if (c != -1) {
            return c;
        }
        throw new IOException(errMsg);
    }

    private static boolean trailingSpace(Reader in) throws IOException {
        if (!in.markSupported()) {
            return true;
        }
        boolean trailing;
        in.mark(9999);
        while (true) {
            int nextChar = in.read();
            if (nextChar != -1) {
                if (nextChar != 32) {
                    if (nextChar != 92) {
                        trailing = PRESERVE_OLD_DC_ENCODING;
                        break;
                    } else if (in.read() != 32) {
                        trailing = PRESERVE_OLD_DC_ENCODING;
                        break;
                    }
                }
            } else {
                trailing = true;
                break;
            }
        }
        in.reset();
        return trailing;
    }

    AVA(DerValue derval) throws IOException {
        if (derval.tag != (byte) 48) {
            throw new IOException("AVA not a sequence");
        }
        this.oid = X500Name.intern(derval.data.getOID());
        this.value = derval.data.getDerValue();
        if (derval.data.available() != 0) {
            throw new IOException("AVA, extra bytes = " + derval.data.available());
        }
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
        int j;
        if ((typeAndValue.charAt(0) < '0' || typeAndValue.charAt(0) > '9') && (isDerString(this.value, PRESERVE_OLD_DC_ENCODING) ^ 1) == 0) {
            try {
                int i;
                char c;
                String str = new String(this.value.getDataBytes(), "UTF8");
                String escapees = ",=+<>#;\"\\";
                StringBuilder sbuffer = new StringBuilder();
                for (i = 0; i < str.length(); i++) {
                    c = str.charAt(i);
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
                            for (j = 0; j < valueBytes.length; j++) {
                                sbuffer.append('\\');
                                sbuffer.append(Character.toUpperCase(Character.forDigit((valueBytes[j] >>> 4) & 15, 16)));
                                sbuffer.append(Character.toUpperCase(Character.forDigit(valueBytes[j] & 15, 16)));
                            }
                        } catch (IOException e) {
                            throw new IllegalArgumentException("DER Value conversion");
                        }
                    }
                }
                char[] chars = sbuffer.-java_util_stream_Collectors-mthref-7().toCharArray();
                sbuffer = new StringBuilder();
                int lead = 0;
                while (lead < chars.length && (chars[lead] == ' ' || chars[lead] == 13)) {
                    lead++;
                }
                int trail = chars.length - 1;
                while (trail >= 0 && (chars[trail] == ' ' || chars[trail] == 13)) {
                    trail--;
                }
                i = 0;
                while (i < chars.length) {
                    c = chars[i];
                    if (i < lead || i > trail) {
                        sbuffer.append('\\');
                    }
                    sbuffer.append(c);
                    i++;
                }
                typeAndValue.append(sbuffer.-java_util_stream_Collectors-mthref-7());
            } catch (IOException e2) {
                throw new IllegalArgumentException("DER Value conversion");
            }
        }
        try {
            byte[] data = this.value.toByteArray();
            typeAndValue.append('#');
            for (byte b : data) {
                typeAndValue.append(Character.forDigit((b >>> 4) & 15, 16));
                typeAndValue.append(Character.forDigit(b & 15, 16));
            }
        } catch (IOException e3) {
            throw new IllegalArgumentException("DER Value conversion");
        }
        return typeAndValue.-java_util_stream_Collectors-mthref-7();
    }

    public String toRFC2253CanonicalString() {
        StringBuilder typeAndValue = new StringBuilder(40);
        typeAndValue.append(toKeyword(3, Collections.emptyMap()));
        typeAndValue.append('=');
        int j;
        if ((typeAndValue.charAt(0) < '0' || typeAndValue.charAt(0) > '9') && (isDerString(this.value, true) || this.value.tag == (byte) 20)) {
            try {
                String valStr = new String(this.value.getDataBytes(), "UTF8");
                String escapees = ",+<>;\"\\";
                StringBuilder sbuffer = new StringBuilder();
                boolean previousWhite = PRESERVE_OLD_DC_ENCODING;
                int i = 0;
                while (i < valStr.length()) {
                    char c = valStr.charAt(i);
                    if (DerValue.isPrintableStringChar(c) || ",+<>;\"\\".indexOf((int) c) >= 0 || (i == 0 && c == '#')) {
                        if ((i == 0 && c == '#') || ",+<>;\"\\".indexOf((int) c) >= 0) {
                            sbuffer.append('\\');
                        }
                        if (!Character.isWhitespace(c)) {
                            previousWhite = PRESERVE_OLD_DC_ENCODING;
                            sbuffer.append(c);
                        } else if (!previousWhite) {
                            previousWhite = true;
                            sbuffer.append(c);
                        }
                    } else if (debug == null || !Debug.isOn("ava")) {
                        previousWhite = PRESERVE_OLD_DC_ENCODING;
                        sbuffer.append(c);
                    } else {
                        previousWhite = PRESERVE_OLD_DC_ENCODING;
                        try {
                            byte[] valueBytes = Character.toString(c).getBytes("UTF8");
                            for (j = 0; j < valueBytes.length; j++) {
                                sbuffer.append('\\');
                                sbuffer.append(Character.forDigit((valueBytes[j] >>> 4) & 15, 16));
                                sbuffer.append(Character.forDigit(valueBytes[j] & 15, 16));
                            }
                        } catch (IOException e) {
                            throw new IllegalArgumentException("DER Value conversion");
                        }
                    }
                    i++;
                }
                typeAndValue.append(sbuffer.-java_util_stream_Collectors-mthref-7().trim());
            } catch (IOException e2) {
                throw new IllegalArgumentException("DER Value conversion");
            }
        }
        try {
            byte[] data = this.value.toByteArray();
            typeAndValue.append('#');
            for (byte b : data) {
                typeAndValue.append(Character.forDigit((b >>> 4) & 15, 16));
                typeAndValue.append(Character.forDigit(b & 15, 16));
            }
        } catch (IOException e3) {
            throw new IllegalArgumentException("DER Value conversion");
        }
        return Normalizer.normalize(typeAndValue.-java_util_stream_Collectors-mthref-7().toUpperCase(Locale.US).toLowerCase(Locale.US), Form.NFKD);
    }

    private static boolean isDerString(DerValue value, boolean canonical) {
        if (canonical) {
            switch (value.tag) {
                case (byte) 12:
                case (byte) 19:
                    return true;
                default:
                    return PRESERVE_OLD_DC_ENCODING;
            }
        }
        switch (value.tag) {
            case (byte) 12:
            case (byte) 19:
            case (byte) 20:
            case (byte) 22:
            case (byte) 27:
            case (byte) 30:
                return true;
            default:
                return PRESERVE_OLD_DC_ENCODING;
        }
    }

    boolean hasRFC2253Keyword() {
        return AVAKeyword.hasKeyword(this.oid, 3);
    }

    /* JADX WARNING: Missing block: B:53:0x010f, code:
            if (",+=\n<>#;\\\"".indexOf((int) r3) >= 0) goto L_0x00d7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String toKeywordValueString(String keyword) {
        StringBuilder retval = new StringBuilder(40);
        retval.append(keyword);
        retval.append("=");
        String valStr = this.value.getAsString();
        int i;
        if (valStr == null) {
            byte[] data = this.value.toByteArray();
            retval.append('#');
            for (i = 0; i < data.length; i++) {
                retval.append(hexDigits.charAt((data[i] >> 4) & 15));
                retval.append(hexDigits.charAt(data[i] & 15));
            }
        } else {
            boolean quoteNeeded = PRESERVE_OLD_DC_ENCODING;
            StringBuilder sbuffer = new StringBuilder();
            boolean previousWhite = PRESERVE_OLD_DC_ENCODING;
            String escapees = ",+=\n<>#;\\\"";
            int length = valStr.length();
            boolean alreadyQuoted = (length <= 1 || valStr.charAt(0) != '\"') ? PRESERVE_OLD_DC_ENCODING : valStr.charAt(length + -1) == '\"' ? true : PRESERVE_OLD_DC_ENCODING;
            i = 0;
            while (i < length) {
                char c = valStr.charAt(i);
                if (alreadyQuoted && (i == 0 || i == length - 1)) {
                    sbuffer.append(c);
                } else if (DerValue.isPrintableStringChar(c) || ",+=\n<>#;\\\"".indexOf((int) c) >= 0) {
                    if (!quoteNeeded) {
                        if (!(i == 0 && (c == 32 || c == 10))) {
                            try {
                            } catch (IOException e) {
                                throw new IllegalArgumentException("DER Value conversion");
                            }
                        }
                        quoteNeeded = true;
                    }
                    if (c == 32 || c == 10) {
                        if (!quoteNeeded && previousWhite) {
                            quoteNeeded = true;
                        }
                        previousWhite = true;
                    } else {
                        if (c == 34 || c == 92) {
                            sbuffer.append('\\');
                        }
                        previousWhite = PRESERVE_OLD_DC_ENCODING;
                    }
                    sbuffer.append(c);
                } else if (debug == null || !Debug.isOn("ava")) {
                    previousWhite = PRESERVE_OLD_DC_ENCODING;
                    sbuffer.append(c);
                } else {
                    previousWhite = PRESERVE_OLD_DC_ENCODING;
                    byte[] valueBytes = Character.toString(c).getBytes("UTF8");
                    for (int j = 0; j < valueBytes.length; j++) {
                        sbuffer.append('\\');
                        sbuffer.append(Character.toUpperCase(Character.forDigit((valueBytes[j] >>> 4) & 15, 16)));
                        sbuffer.append(Character.toUpperCase(Character.forDigit(valueBytes[j] & 15, 16)));
                    }
                }
                i++;
            }
            if (sbuffer.length() > 0) {
                char trailChar = sbuffer.charAt(sbuffer.length() - 1);
                if (trailChar == ' ' || trailChar == 10) {
                    quoteNeeded = true;
                }
            }
            if (alreadyQuoted || !quoteNeeded) {
                retval.append(sbuffer.-java_util_stream_Collectors-mthref-7());
            } else {
                retval.append("\"").append(sbuffer.-java_util_stream_Collectors-mthref-7()).append("\"");
            }
        }
        return retval.-java_util_stream_Collectors-mthref-7();
    }
}
