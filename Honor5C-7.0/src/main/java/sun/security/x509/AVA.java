package sun.security.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.util.calendar.BaseCalendar;

public class AVA implements DerEncoder {
    static final int DEFAULT = 1;
    private static final boolean PRESERVE_OLD_DC_ENCODING = false;
    static final int RFC1779 = 2;
    static final int RFC2253 = 3;
    private static final Debug debug = null;
    private static final String hexDigits = "0123456789ABCDEF";
    private static final String specialChars = ",+=\n<>#;";
    private static final String specialChars2253 = ",+\"\\<>;";
    private static final String specialCharsAll = ",=\n+<>#;\\\" ";
    final ObjectIdentifier oid;
    final DerValue value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.AVA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.AVA.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.AVA.<clinit>():void");
    }

    public AVA(ObjectIdentifier type, DerValue val) {
        if (type == null || val == null) {
            throw new NullPointerException();
        }
        this.oid = type;
        this.value = val;
    }

    AVA(Reader in) throws IOException {
        this(in, (int) DEFAULT);
    }

    AVA(Reader in, Map<String, String> keywordMap) throws IOException {
        this(in, DEFAULT, keywordMap);
    }

    AVA(Reader in, int format) throws IOException {
        this(in, format, Collections.emptyMap());
    }

    AVA(Reader in, int format, Map<String, String> keywordMap) throws IOException {
        StringBuilder temp = new StringBuilder();
        while (true) {
            int c = readChar(in, "Incorrect AVA format");
            if (c == 61) {
                break;
            }
            temp.append((char) c);
        }
        this.oid = AVAKeyword.getOID(temp.toString(), format, keywordMap);
        temp.setLength(0);
        if (format != RFC2253) {
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
        } else if (c != 34 || format == RFC2253) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static DerValue parseHexString(Reader in, int format) throws IOException {
        int c;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b = 0;
        int cNdx = 0;
        while (true) {
            c = in.read();
            if (isTerminator(c, format)) {
                break;
            } else if (c == 32 || c == 10) {
                while (true) {
                    if (c != 32 && c != 10) {
                        break;
                    }
                    c = in.read();
                    if (isTerminator(c, format)) {
                        break;
                    }
                }
            } else {
                int cVal = hexDigits.indexOf(Character.toUpperCase((char) c));
                if (cVal == -1) {
                    break;
                }
                if (cNdx % RFC1779 == DEFAULT) {
                    b = (byte) ((b * 16) + ((byte) cVal));
                    baos.write(b);
                } else {
                    b = (byte) cVal;
                }
                cNdx += DEFAULT;
            }
        }
        throw new IOException("AVA parse, invalid hex digit: " + ((char) c));
    }

    private DerValue parseQuotedString(Reader in, StringBuilder temp) throws IOException {
        int c = readChar(in, "Quoted string did not end in quote");
        List<Byte> embeddedHex = new ArrayList();
        int i = DEFAULT;
        while (c != 34) {
            if (c == 92) {
                c = readChar(in, "Quoted string did not end in quote");
                Byte hexByte = getEmbeddedHexPair(c, in);
                if (hexByte != null) {
                    i = 0;
                    embeddedHex.add(hexByte);
                    c = in.read();
                } else if (!(c == 92 || c == 34 || specialChars.indexOf((char) c) >= 0)) {
                    throw new IOException("Invalid escaped character in AVA: " + ((char) c));
                }
            }
            if (embeddedHex.size() > 0) {
                temp.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            i &= DerValue.isPrintableStringChar((char) c);
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
            return new DerValue((byte) DerValue.tag_IA5String, temp.toString());
        } else {
            if (i != 0) {
                return new DerValue(temp.toString());
            }
            return new DerValue((byte) DerValue.tag_UTF8String, temp.toString());
        }
    }

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
                    embeddedHex.add(hexByte);
                    c = in.read();
                    leadingChar = PRESERVE_OLD_DC_ENCODING;
                } else if (!(format == DEFAULT && specialCharsAll.indexOf((char) c) == -1) && (format != RFC1779 || specialChars.indexOf((char) c) != -1 || c == 92 || c == 34)) {
                    if (format == RFC2253) {
                        if (c == 32) {
                            if (!(leadingChar || trailingSpace(in))) {
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
                        for (i = 0; i < spaceCount; i += DEFAULT) {
                            temp.append(" ");
                        }
                        spaceCount = 0;
                        temp.append(getEmbeddedHexString(embeddedHex));
                        embeddedHex.clear();
                    }
                    isPrintableString &= DerValue.isPrintableStringChar((char) c);
                    if (c == 32 || escape) {
                        for (i = 0; i < spaceCount; i += DEFAULT) {
                            temp.append(" ");
                        }
                        spaceCount = 0;
                        temp.append((char) c);
                    } else {
                        spaceCount += DEFAULT;
                    }
                    c = in.read();
                    leadingChar = PRESERVE_OLD_DC_ENCODING;
                } else {
                    throw new IOException("Invalid escaped character in AVA: '" + ((char) c) + "'");
                }
            }
            if (format == RFC2253 && specialChars2253.indexOf((char) c) != -1) {
                throw new IOException("Character '" + ((char) c) + "' in AVA appears without escape");
            }
            if (embeddedHex.size() > 0) {
                for (i = 0; i < spaceCount; i += DEFAULT) {
                    temp.append(" ");
                }
                spaceCount = 0;
                temp.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            isPrintableString &= DerValue.isPrintableStringChar((char) c);
            if (c == 32) {
            }
            for (i = 0; i < spaceCount; i += DEFAULT) {
                temp.append(" ");
            }
            spaceCount = 0;
            temp.append((char) c);
            c = in.read();
            leadingChar = PRESERVE_OLD_DC_ENCODING;
        } while (!isTerminator(c, format));
        if (format != RFC2253 || spaceCount <= 0) {
            if (embeddedHex.size() > 0) {
                temp.append(getEmbeddedHexString(embeddedHex));
                embeddedHex.clear();
            }
            if (this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID) || (this.oid.equals(X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING)) {
                return new DerValue((byte) DerValue.tag_IA5String, temp.toString());
            }
            if (isPrintableString) {
                return new DerValue(temp.toString());
            }
            return new DerValue((byte) DerValue.tag_UTF8String, temp.toString());
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
        for (int i = 0; i < n; i += DEFAULT) {
            hexBytes[i] = ((Byte) hexList.get(i)).byteValue();
        }
        return new String(hexBytes, "UTF8");
    }

    private static boolean isTerminator(int ch, int format) {
        boolean z = true;
        switch (ch) {
            case GeneralNameInterface.NAME_DIFF_TYPE /*-1*/:
            case 43:
            case 44:
                return true;
            case 59:
            case 62:
                if (format == RFC2253) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean trailingSpace(Reader in) throws IOException {
        if (!in.markSupported()) {
            return true;
        }
        in.mark(9999);
        while (true) {
            int nextChar = in.read();
            if (nextChar != -1) {
                if (nextChar != 32) {
                    if (nextChar != 92) {
                        break;
                    } else if (in.read() != 32) {
                        break;
                    }
                }
            } else {
                break;
            }
            in.reset();
            return trailing;
        }
        boolean trailing = PRESERVE_OLD_DC_ENCODING;
        in.reset();
        return trailing;
    }

    AVA(DerValue derval) throws IOException {
        if (derval.tag != 48) {
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
        tmp2.write((byte) DerValue.tag_SequenceOf, tmp);
        out.write(tmp2.toByteArray());
    }

    private String toKeyword(int format, Map<String, String> oidMap) {
        return AVAKeyword.getKeyword(this.oid, format, oidMap);
    }

    public String toString() {
        return toKeywordValueString(toKeyword(DEFAULT, Collections.emptyMap()));
    }

    public String toRFC1779String() {
        return toRFC1779String(Collections.emptyMap());
    }

    public String toRFC1779String(Map<String, String> oidMap) {
        return toKeywordValueString(toKeyword(RFC1779, oidMap));
    }

    public String toRFC2253String() {
        return toRFC2253String(Collections.emptyMap());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String toRFC2253String(Map<String, String> oidMap) {
        int j;
        int length;
        StringBuilder typeAndValue = new StringBuilder(100);
        typeAndValue.append(toKeyword(RFC2253, oidMap));
        typeAndValue.append('=');
        if (typeAndValue.charAt(0) < '0' || typeAndValue.charAt(0) > '9') {
            if (isDerString(this.value, PRESERVE_OLD_DC_ENCODING)) {
                try {
                    int i;
                    char c;
                    int trail;
                    String str = new String(this.value.getDataBytes(), "UTF8");
                    String escapees = ",=+<>#;\"\\";
                    StringBuilder sbuffer = new StringBuilder();
                    for (i = 0; i < str.length(); i += DEFAULT) {
                        c = str.charAt(i);
                        if (!DerValue.isPrintableStringChar(c)) {
                            if (",=+<>#;\"\\".indexOf((int) c) < 0) {
                                if (c == '\u0000') {
                                    sbuffer.append("\\00");
                                } else if (debug == null || !Debug.isOn("ava")) {
                                    sbuffer.append(c);
                                } else {
                                    try {
                                        byte[] valueBytes = Character.toString(c).getBytes("UTF8");
                                        j = 0;
                                        while (true) {
                                            length = valueBytes.length;
                                            if (j >= r0) {
                                                break;
                                            }
                                            sbuffer.append('\\');
                                            sbuffer.append(Character.toUpperCase(Character.forDigit((valueBytes[j] >>> 4) & 15, 16)));
                                            sbuffer.append(Character.toUpperCase(Character.forDigit(valueBytes[j] & 15, 16)));
                                            j += DEFAULT;
                                        }
                                    } catch (IOException e) {
                                        throw new IllegalArgumentException("DER Value conversion");
                                    }
                                }
                            }
                        }
                        if (",=+<>#;\"\\".indexOf((int) c) >= 0) {
                            sbuffer.append('\\');
                        }
                        sbuffer.append(c);
                    }
                    char[] chars = sbuffer.toString().toCharArray();
                    sbuffer = new StringBuilder();
                    int lead = 0;
                    while (true) {
                        length = chars.length;
                        if (lead >= r0 || !(chars[lead] == ' ' || chars[lead] == '\r')) {
                            trail = chars.length - 1;
                        } else {
                            lead += DEFAULT;
                        }
                    }
                    trail = chars.length - 1;
                    while (trail >= 0 && (chars[trail] == ' ' || chars[trail] == '\r')) {
                        trail--;
                    }
                    i = 0;
                    while (true) {
                        length = chars.length;
                        if (i >= r0) {
                            break;
                        }
                        c = chars[i];
                        if (i < lead || i > trail) {
                            sbuffer.append('\\');
                        }
                        sbuffer.append(c);
                        i += DEFAULT;
                    }
                    typeAndValue.append(sbuffer.toString());
                    return typeAndValue.toString();
                } catch (IOException e2) {
                    throw new IllegalArgumentException("DER Value conversion");
                }
            }
        }
        try {
            byte[] data = this.value.toByteArray();
            typeAndValue.append('#');
            j = 0;
            while (true) {
                length = data.length;
                if (j >= r0) {
                    break;
                }
                byte b = data[j];
                typeAndValue.append(Character.forDigit((b >>> 4) & 15, 16));
                typeAndValue.append(Character.forDigit(b & 15, 16));
                j += DEFAULT;
            }
            return typeAndValue.toString();
        } catch (IOException e3) {
            throw new IllegalArgumentException("DER Value conversion");
        }
    }

    public String toRFC2253CanonicalString() {
        StringBuilder typeAndValue = new StringBuilder(40);
        typeAndValue.append(toKeyword(RFC2253, Collections.emptyMap()));
        typeAndValue.append('=');
        int j;
        if ((typeAndValue.charAt(0) < '0' || typeAndValue.charAt(0) > '9') && (isDerString(this.value, true) || this.value.tag == 20)) {
            try {
                String valStr = new String(this.value.getDataBytes(), "UTF8");
                String escapees = ",+<>;\"\\";
                StringBuilder sbuffer = new StringBuilder();
                boolean previousWhite = PRESERVE_OLD_DC_ENCODING;
                int i = 0;
                while (i < valStr.length()) {
                    char c = valStr.charAt(i);
                    if (DerValue.isPrintableStringChar(c) || ",+<>;\"\\".indexOf((int) c) >= 0 || (i == 0 && c == '#')) {
                        if (!(i == 0 && c == '#')) {
                            if (",+<>;\"\\".indexOf((int) c) >= 0) {
                            }
                            if (!Character.isWhitespace(c)) {
                                previousWhite = PRESERVE_OLD_DC_ENCODING;
                                sbuffer.append(c);
                            } else if (!previousWhite) {
                                previousWhite = true;
                                sbuffer.append(c);
                            }
                        }
                        sbuffer.append('\\');
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
                            for (j = 0; j < valueBytes.length; j += DEFAULT) {
                                sbuffer.append('\\');
                                sbuffer.append(Character.forDigit((valueBytes[j] >>> 4) & 15, 16));
                                sbuffer.append(Character.forDigit(valueBytes[j] & 15, 16));
                            }
                        } catch (IOException e) {
                            throw new IllegalArgumentException("DER Value conversion");
                        }
                    }
                    i += DEFAULT;
                }
                typeAndValue.append(sbuffer.toString().trim());
            } catch (IOException e2) {
                throw new IllegalArgumentException("DER Value conversion");
            }
        }
        try {
            byte[] data = this.value.toByteArray();
            typeAndValue.append('#');
            for (j = 0; j < data.length; j += DEFAULT) {
                byte b = data[j];
                typeAndValue.append(Character.forDigit((b >>> 4) & 15, 16));
                typeAndValue.append(Character.forDigit(b & 15, 16));
            }
        } catch (IOException e3) {
            throw new IllegalArgumentException("DER Value conversion");
        }
        return Normalizer.normalize(typeAndValue.toString().toUpperCase(Locale.US).toLowerCase(Locale.US), Form.NFKD);
    }

    private static boolean isDerString(DerValue value, boolean canonical) {
        if (canonical) {
            switch (value.tag) {
                case BaseCalendar.DECEMBER /*12*/:
                case DigitList.MAX_COUNT /*19*/:
                    return true;
                default:
                    return PRESERVE_OLD_DC_ENCODING;
            }
        }
        switch (value.tag) {
            case BaseCalendar.DECEMBER /*12*/:
            case DigitList.MAX_COUNT /*19*/:
            case Record.trailerSize /*20*/:
            case ZipConstants.LOCLEN /*22*/:
            case (byte) 27:
            case AbstractSpinedBuffer.MAX_CHUNK_POWER /*30*/:
                return true;
            default:
                return PRESERVE_OLD_DC_ENCODING;
        }
    }

    boolean hasRFC2253Keyword() {
        return AVAKeyword.hasKeyword(this.oid, RFC2253);
    }

    private String toKeywordValueString(String keyword) {
        StringBuilder retval = new StringBuilder(40);
        retval.append(keyword);
        retval.append("=");
        String valStr = this.value.getAsString();
        int i;
        int length;
        if (valStr == null) {
            byte[] data = this.value.toByteArray();
            retval.append('#');
            i = 0;
            while (true) {
                length = data.length;
                if (i >= r0) {
                    break;
                }
                retval.append(hexDigits.charAt((data[i] >> 4) & 15));
                retval.append(hexDigits.charAt(data[i] & 15));
                i += DEFAULT;
            }
        } else {
            boolean alreadyQuoted;
            boolean quoteNeeded = PRESERVE_OLD_DC_ENCODING;
            StringBuilder sbuffer = new StringBuilder();
            boolean previousWhite = PRESERVE_OLD_DC_ENCODING;
            String escapees = ",+=\n<>#;\\\"";
            int length2 = valStr.length();
            if (length2 <= DEFAULT || valStr.charAt(0) != '\"') {
                alreadyQuoted = PRESERVE_OLD_DC_ENCODING;
            } else {
                alreadyQuoted = valStr.charAt(length2 + -1) == '\"' ? true : PRESERVE_OLD_DC_ENCODING;
            }
            i = 0;
            while (i < length2) {
                char c = valStr.charAt(i);
                if (alreadyQuoted && (i == 0 || i == length2 - 1)) {
                    sbuffer.append(c);
                } else {
                    if (!DerValue.isPrintableStringChar(c)) {
                        if (",+=\n<>#;\\\"".indexOf((int) c) < 0) {
                            if (debug != null && Debug.isOn("ava")) {
                                previousWhite = PRESERVE_OLD_DC_ENCODING;
                                byte[] valueBytes = Character.toString(c).getBytes("UTF8");
                                int j = 0;
                                while (true) {
                                    length = valueBytes.length;
                                    if (j >= r0) {
                                        break;
                                    }
                                    sbuffer.append('\\');
                                    sbuffer.append(Character.toUpperCase(Character.forDigit((valueBytes[j] >>> 4) & 15, 16)));
                                    sbuffer.append(Character.toUpperCase(Character.forDigit(valueBytes[j] & 15, 16)));
                                    j += DEFAULT;
                                }
                            } else {
                                previousWhite = PRESERVE_OLD_DC_ENCODING;
                                sbuffer.append(c);
                            }
                        }
                    }
                    if (!quoteNeeded) {
                        if (!(i == 0 && (c == 32 || c == 10))) {
                            try {
                                if (",+=\n<>#;\\\"".indexOf((int) c) >= 0) {
                                }
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
                }
                i += DEFAULT;
            }
            if (sbuffer.length() > 0) {
                char trailChar = sbuffer.charAt(sbuffer.length() - 1);
                if (trailChar == ' ' || trailChar == '\n') {
                    quoteNeeded = true;
                }
            }
            if (alreadyQuoted || !quoteNeeded) {
                retval.append(sbuffer.toString());
            } else {
                retval.append("\"").append(sbuffer.toString()).append("\"");
            }
        }
        return retval.toString();
    }
}
