package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.io.UnsupportedEncodingException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.global.icu.impl.PatternTokenizer;

public class AnyURIDV extends TypeValidator {
    private static final URI BASE_URI;
    private static char[] gAfterEscaping1 = new char[128];
    private static char[] gAfterEscaping2 = new char[128];
    private static char[] gHexChs = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static boolean[] gNeedEscaping = new boolean[128];

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2079;
    }

    static {
        URI uri;
        try {
            uri = new URI("abc://def.ghi.jkl");
        } catch (URI.MalformedURIException unused) {
            uri = null;
        }
        BASE_URI = uri;
        for (int i = 0; i <= 31; i++) {
            gNeedEscaping[i] = true;
            char[] cArr = gAfterEscaping1;
            char[] cArr2 = gHexChs;
            cArr[i] = cArr2[i >> 4];
            gAfterEscaping2[i] = cArr2[i & 15];
        }
        gNeedEscaping[127] = true;
        gAfterEscaping1[127] = '7';
        gAfterEscaping2[127] = 'F';
        char[] cArr3 = {' ', '<', '>', '\"', '{', '}', '|', PatternTokenizer.BACK_SLASH, '^', '~', '`'};
        for (char c : cArr3) {
            gNeedEscaping[c] = true;
            char[] cArr4 = gAfterEscaping1;
            char[] cArr5 = gHexChs;
            cArr4[c] = cArr5[c >> 4];
            gAfterEscaping2[c] = cArr5[c & 15];
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            if (str.length() != 0) {
                new URI(BASE_URI, encode(str));
            }
            return str;
        } catch (URI.MalformedURIException unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_ANYURI});
        }
    }

    private static String encode(String str) {
        char charAt;
        int length = str.length();
        StringBuffer stringBuffer = new StringBuffer(length * 3);
        int i = 0;
        while (i < length && (charAt = str.charAt(i)) < 128) {
            if (gNeedEscaping[charAt]) {
                stringBuffer.append('%');
                stringBuffer.append(gAfterEscaping1[charAt]);
                stringBuffer.append(gAfterEscaping2[charAt]);
            } else {
                stringBuffer.append((char) charAt);
            }
            i++;
        }
        if (i < length) {
            try {
                byte[] bytes = str.substring(i).getBytes("UTF-8");
                int length2 = bytes.length;
                for (byte b : bytes) {
                    if (b < 0) {
                        int i2 = b + 256;
                        stringBuffer.append('%');
                        stringBuffer.append(gHexChs[i2 >> 4]);
                        stringBuffer.append(gHexChs[i2 & 15]);
                    } else if (gNeedEscaping[b]) {
                        stringBuffer.append('%');
                        stringBuffer.append(gAfterEscaping1[b]);
                        stringBuffer.append(gAfterEscaping2[b]);
                    } else {
                        stringBuffer.append((char) b);
                    }
                }
                length = length2;
            } catch (UnsupportedEncodingException unused) {
                return str;
            }
        }
        return stringBuffer.length() != length ? stringBuffer.toString() : str;
    }
}
