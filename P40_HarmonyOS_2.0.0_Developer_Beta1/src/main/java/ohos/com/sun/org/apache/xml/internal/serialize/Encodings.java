package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.com.sun.org.apache.xerces.internal.util.EncodingMap;

/* access modifiers changed from: package-private */
public class Encodings {
    static final String DEFAULT_ENCODING = "UTF8";
    static final int DEFAULT_LAST_PRINTABLE = 127;
    static final String JIS_DANGER_CHARS = "\\~¢£¥¬—―‖…‾‾∥∯〜＼～￠￡￢￣";
    static final int LAST_PRINTABLE_UNICODE = 65535;
    static final String[] UNICODE_ENCODINGS = {"Unicode", "UnicodeBig", "UnicodeLittle", "GB2312", DEFAULT_ENCODING, "UTF-16"};
    private static final Map<String, EncodingInfo> _encodings = new ConcurrentHashMap();

    Encodings() {
    }

    static EncodingInfo getEncodingInfo(String str, boolean z) throws UnsupportedEncodingException {
        if (str == null) {
            EncodingInfo encodingInfo = _encodings.get(DEFAULT_ENCODING);
            if (encodingInfo != null) {
                return encodingInfo;
            }
            EncodingInfo encodingInfo2 = new EncodingInfo(EncodingMap.getJava2IANAMapping(DEFAULT_ENCODING), DEFAULT_ENCODING, 65535);
            _encodings.put(DEFAULT_ENCODING, encodingInfo2);
            return encodingInfo2;
        }
        String upperCase = str.toUpperCase(Locale.ENGLISH);
        String iANA2JavaMapping = EncodingMap.getIANA2JavaMapping(upperCase);
        int i = 0;
        if (iANA2JavaMapping != null) {
            EncodingInfo encodingInfo3 = _encodings.get(iANA2JavaMapping);
            if (encodingInfo3 != null) {
                return encodingInfo3;
            }
            while (true) {
                String[] strArr = UNICODE_ENCODINGS;
                if (i >= strArr.length) {
                    break;
                } else if (strArr[i].equalsIgnoreCase(iANA2JavaMapping)) {
                    encodingInfo3 = new EncodingInfo(upperCase, iANA2JavaMapping, 65535);
                    break;
                } else {
                    i++;
                }
            }
            if (i == UNICODE_ENCODINGS.length) {
                encodingInfo3 = new EncodingInfo(upperCase, iANA2JavaMapping, 127);
            }
            _encodings.put(iANA2JavaMapping, encodingInfo3);
            return encodingInfo3;
        } else if (z) {
            EncodingInfo.testJavaEncodingName(upperCase);
            EncodingInfo encodingInfo4 = _encodings.get(upperCase);
            if (encodingInfo4 != null) {
                return encodingInfo4;
            }
            while (true) {
                String[] strArr2 = UNICODE_ENCODINGS;
                if (i >= strArr2.length) {
                    break;
                } else if (strArr2[i].equalsIgnoreCase(upperCase)) {
                    encodingInfo4 = new EncodingInfo(EncodingMap.getJava2IANAMapping(upperCase), upperCase, 65535);
                    break;
                } else {
                    i++;
                }
            }
            if (i == UNICODE_ENCODINGS.length) {
                encodingInfo4 = new EncodingInfo(EncodingMap.getJava2IANAMapping(upperCase), upperCase, 127);
            }
            _encodings.put(upperCase, encodingInfo4);
            return encodingInfo4;
        } else {
            throw new UnsupportedEncodingException(upperCase);
        }
    }
}
