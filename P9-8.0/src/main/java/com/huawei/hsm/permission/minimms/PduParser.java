package com.huawei.hsm.permission.minimms;

import com.huawei.connectivitylog.ConnectivityLogManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;

public class PduParser {
    static final /* synthetic */ boolean -assertionsDisabled = (PduParser.class.desiredAssertionStatus() ^ 1);
    private static final int END_STRING_FLAG = 0;
    private static final int LENGTH_QUOTE = 31;
    private static final boolean LOCAL_LOGV = false;
    private static final String LOG_TAG = "PduParser";
    private static final int LONG_INTEGER_LENGTH_MAX = 8;
    private static final int QUOTE = 127;
    private static final int QUOTED_STRING_FLAG = 34;
    private static final int SHORT_INTEGER_MAX = 127;
    private static final int SHORT_LENGTH_MAX = 30;
    private static final int TEXT_MAX = 127;
    private static final int TEXT_MIN = 32;
    private static final int TYPE_QUOTED_STRING = 1;
    private static final int TYPE_TEXT_STRING = 0;
    private static final int TYPE_TOKEN_STRING = 2;
    private PduHeaders mHeaders = null;
    private ByteArrayInputStream mPduDataStream = null;

    public PduParser(byte[] pduDataStream) {
        this.mPduDataStream = new ByteArrayInputStream(pduDataStream);
    }

    public int getTargetCount() {
        int sendToCount = 1;
        if (this.mPduDataStream == null) {
            return 1;
        }
        this.mHeaders = parseHeaders(this.mPduDataStream);
        if (this.mHeaders == null) {
            return 1;
        }
        EncodedStringValue[] diTo = this.mHeaders.getEncodedStringValues(151);
        if (diTo != null) {
            sendToCount = diTo.length;
        }
        return sendToCount;
    }

    private PduHeaders parseHeaders(ByteArrayInputStream pduDataStream) {
        if (pduDataStream == null) {
            return null;
        }
        boolean keepParsing = true;
        PduHeaders headers = new PduHeaders();
        while (keepParsing && pduDataStream.available() > 0) {
            pduDataStream.mark(1);
            int headerField = extractByteValue(pduDataStream);
            if (headerField < 32 || headerField > 127) {
                EncodedStringValue value;
                byte[] address;
                String str;
                int endIndex;
                String str2;
                switch (headerField) {
                    case 129:
                    case 130:
                    case 151:
                        value = parseEncodedStringValue(pduDataStream);
                        if (value != null) {
                            address = value.getTextString();
                            if (address != null) {
                                str = new String(address, Charset.defaultCharset());
                                endIndex = str.indexOf("/");
                                if (endIndex > 0) {
                                    str2 = str.substring(0, endIndex);
                                }
                                try {
                                    value.setTextString(str2.getBytes(Charset.defaultCharset()));
                                } catch (NullPointerException e) {
                                    return null;
                                }
                            }
                            try {
                                headers.appendEncodedStringValue(value, headerField);
                                break;
                            } catch (NullPointerException e2) {
                                break;
                            } catch (RuntimeException e3) {
                                return null;
                            }
                        }
                        continue;
                    case 131:
                    case 139:
                    case PduHeaders.TRANSACTION_ID /*152*/:
                    case PduHeaders.REPLY_CHARGING_ID /*158*/:
                    case PduHeaders.APPLIC_ID /*183*/:
                    case PduHeaders.REPLY_APPLIC_ID /*184*/:
                    case PduHeaders.AUX_APPLIC_ID /*185*/:
                    case PduHeaders.REPLACE_ID /*189*/:
                    case PduHeaders.CANCEL_ID /*190*/:
                        byte[] value2 = parseWapString(pduDataStream, 0);
                        if (value2 != null) {
                            try {
                                headers.setTextString(value2, headerField);
                                break;
                            } catch (NullPointerException e4) {
                                break;
                            } catch (RuntimeException e5) {
                                return null;
                            }
                        }
                        continue;
                    case 132:
                        byte[] contentType = parseContentType(pduDataStream, new HashMap());
                        if (contentType != null) {
                            try {
                                headers.setTextString(contentType, 132);
                            } catch (NullPointerException e6) {
                            } catch (RuntimeException e7) {
                                return null;
                            }
                        }
                        keepParsing = false;
                        break;
                    case 133:
                    case 142:
                    case PduHeaders.REPLY_CHARGING_SIZE /*159*/:
                        try {
                            headers.setLongInteger(parseLongInteger(pduDataStream), headerField);
                            break;
                        } catch (RuntimeException e8) {
                            return null;
                        }
                    case PduHeaders.DELIVERY_REPORT /*134*/:
                    case 143:
                    case 144:
                    case 145:
                    case 146:
                    case 148:
                    case 149:
                    case 153:
                    case PduHeaders.READ_STATUS /*155*/:
                    case PduHeaders.REPLY_CHARGING /*156*/:
                    case PduHeaders.STORE /*162*/:
                    case PduHeaders.MM_STATE /*163*/:
                    case PduHeaders.STORE_STATUS /*165*/:
                    case PduHeaders.STORED /*167*/:
                    case PduHeaders.TOTALS /*169*/:
                    case PduHeaders.QUOTAS /*171*/:
                    case PduHeaders.DISTRIBUTION_INDICATOR /*177*/:
                    case PduHeaders.RECOMMENDED_RETRIEVAL_MODE /*180*/:
                    case PduHeaders.CONTENT_CLASS /*186*/:
                    case PduHeaders.DRM_CONTENT /*187*/:
                    case PduHeaders.ADAPTATION_ALLOWED /*188*/:
                    case PduHeaders.CANCEL_STATUS /*191*/:
                        try {
                            headers.setOctet(extractByteValue(pduDataStream), headerField);
                            break;
                        } catch (InvalidHeaderValueException e9) {
                            return null;
                        } catch (RuntimeException e10) {
                            return null;
                        }
                    case 135:
                    case 136:
                    case PduHeaders.REPLY_CHARGING_DEADLINE /*157*/:
                        parseValueLength(pduDataStream);
                        int token = extractByteValue(pduDataStream);
                        try {
                            long timeValue = parseLongInteger(pduDataStream);
                            if (129 == token) {
                                timeValue += System.currentTimeMillis() / 1000;
                            }
                            try {
                                headers.setLongInteger(timeValue, headerField);
                                break;
                            } catch (RuntimeException e11) {
                                return null;
                            }
                        } catch (RuntimeException e12) {
                            return null;
                        }
                    case 137:
                        EncodedStringValue from;
                        parseValueLength(pduDataStream);
                        if (128 == extractByteValue(pduDataStream)) {
                            from = parseEncodedStringValue(pduDataStream);
                            if (from != null) {
                                address = from.getTextString();
                                if (address != null) {
                                    str = new String(address, Charset.defaultCharset());
                                    endIndex = str.indexOf("/");
                                    if (endIndex > 0) {
                                        str2 = str.substring(0, endIndex);
                                    }
                                    try {
                                        from.setTextString(str2.getBytes(Charset.defaultCharset()));
                                    } catch (NullPointerException e13) {
                                        return null;
                                    }
                                }
                            }
                        }
                        try {
                            from = new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes(Charset.defaultCharset()));
                        } catch (NullPointerException e14) {
                            return null;
                        }
                        try {
                            headers.setEncodedStringValue(from, 137);
                            break;
                        } catch (NullPointerException e15) {
                            break;
                        } catch (RuntimeException e16) {
                            return null;
                        }
                    case 138:
                        pduDataStream.mark(1);
                        int messageClass = extractByteValue(pduDataStream);
                        if (messageClass >= 128) {
                            if (128 != messageClass) {
                                if (129 != messageClass) {
                                    if (130 != messageClass) {
                                        if (131 != messageClass) {
                                            break;
                                        }
                                        headers.setTextString(PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes(Charset.defaultCharset()), 138);
                                        break;
                                    }
                                    headers.setTextString(PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.getBytes(Charset.defaultCharset()), 138);
                                    break;
                                }
                                headers.setTextString(PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.getBytes(Charset.defaultCharset()), 138);
                                break;
                            }
                            try {
                                headers.setTextString(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes(Charset.defaultCharset()), 138);
                                break;
                            } catch (NullPointerException e17) {
                                break;
                            } catch (RuntimeException e18) {
                                return null;
                            }
                        }
                        pduDataStream.reset();
                        byte[] messageClassString = parseWapString(pduDataStream, 0);
                        if (messageClassString != null) {
                            try {
                                headers.setTextString(messageClassString, 138);
                                break;
                            } catch (NullPointerException e19) {
                                break;
                            } catch (RuntimeException e20) {
                                return null;
                            }
                        }
                        continue;
                    case 140:
                        int messageType = extractByteValue(pduDataStream);
                        switch (messageType) {
                            case 137:
                            case 138:
                            case 139:
                            case 140:
                            case 141:
                            case 142:
                            case 143:
                            case 144:
                            case 145:
                            case 146:
                            case 147:
                            case 148:
                            case 149:
                            case 150:
                            case 151:
                                return null;
                            default:
                                try {
                                    headers.setOctet(messageType, headerField);
                                    break;
                                } catch (InvalidHeaderValueException e21) {
                                    return null;
                                } catch (RuntimeException e22) {
                                    return null;
                                }
                        }
                    case 141:
                        try {
                            headers.setOctet(parseShortInteger(pduDataStream), 141);
                            break;
                        } catch (InvalidHeaderValueException e23) {
                            return null;
                        } catch (RuntimeException e24) {
                            return null;
                        }
                    case 147:
                    case 150:
                    case PduHeaders.RETRIEVE_TEXT /*154*/:
                    case PduHeaders.STORE_STATUS_TEXT /*166*/:
                    case PduHeaders.RECOMMENDED_RETRIEVAL_MODE_TEXT /*181*/:
                    case PduHeaders.STATUS_TEXT /*182*/:
                        if (150 == headerField) {
                            pduDataStream.mark(1);
                            if ((pduDataStream.read() & 255) != 0) {
                                pduDataStream.reset();
                            } else {
                                continue;
                            }
                        }
                        value = parseEncodedStringValue(pduDataStream);
                        if (value != null) {
                            try {
                                headers.setEncodedStringValue(value, headerField);
                                break;
                            } catch (NullPointerException e25) {
                                break;
                            } catch (RuntimeException e26) {
                                return null;
                            }
                        }
                        continue;
                    case PduHeaders.PREVIOUSLY_SENT_BY /*160*/:
                        parseValueLength(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            EncodedStringValue previouslySentBy = parseEncodedStringValue(pduDataStream);
                            if (previouslySentBy != null) {
                                try {
                                    headers.setEncodedStringValue(previouslySentBy, PduHeaders.PREVIOUSLY_SENT_BY);
                                    break;
                                } catch (NullPointerException e27) {
                                    break;
                                } catch (RuntimeException e28) {
                                    return null;
                                }
                            }
                            continue;
                        } catch (RuntimeException e29) {
                            return null;
                        }
                    case PduHeaders.PREVIOUSLY_SENT_DATE /*161*/:
                        parseValueLength(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            try {
                                headers.setLongInteger(parseLongInteger(pduDataStream), PduHeaders.PREVIOUSLY_SENT_DATE);
                                break;
                            } catch (RuntimeException e30) {
                                return null;
                            }
                        } catch (RuntimeException e31) {
                            return null;
                        }
                    case PduHeaders.MM_FLAGS /*164*/:
                        parseValueLength(pduDataStream);
                        extractByteValue(pduDataStream);
                        parseEncodedStringValue(pduDataStream);
                        break;
                    case PduHeaders.MBOX_TOTALS /*170*/:
                    case PduHeaders.MBOX_QUOTAS /*172*/:
                        parseValueLength(pduDataStream);
                        extractByteValue(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            break;
                        } catch (RuntimeException e32) {
                            return null;
                        }
                    case PduHeaders.MESSAGE_COUNT /*173*/:
                    case PduHeaders.START /*175*/:
                    case PduHeaders.LIMIT /*179*/:
                        try {
                            headers.setLongInteger(parseIntegerValue(pduDataStream), headerField);
                            break;
                        } catch (RuntimeException e33) {
                            return null;
                        }
                    case PduHeaders.ELEMENT_DESCRIPTOR /*178*/:
                        parseContentType(pduDataStream, null);
                        break;
                    default:
                        break;
                }
            }
            pduDataStream.reset();
            parseWapString(pduDataStream, 0);
        }
        return headers;
    }

    private static int parseUnsignedInt(ByteArrayInputStream pduDataStream) {
        if (-assertionsDisabled || pduDataStream != null) {
            int result = 0;
            int temp = pduDataStream.read();
            if (temp == -1) {
                return temp;
            }
            while ((temp & 128) != 0) {
                result = (result << 7) | (temp & ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED);
                temp = pduDataStream.read();
                if (temp == -1) {
                    return temp;
                }
            }
            return (result << 7) | (temp & ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED);
        }
        throw new AssertionError();
    }

    private static int parseValueLength(ByteArrayInputStream pduDataStream) {
        if (-assertionsDisabled || pduDataStream != null) {
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                int first = temp & 255;
                if (first <= 30) {
                    return first;
                }
                if (first == 31) {
                    return parseUnsignedInt(pduDataStream);
                }
                throw new RuntimeException("Value length > LENGTH_QUOTE!");
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static EncodedStringValue parseEncodedStringValue(ByteArrayInputStream pduDataStream) {
        if (-assertionsDisabled || pduDataStream != null) {
            pduDataStream.mark(1);
            int charset = 0;
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                int first = temp & 255;
                if (first == 0) {
                    return new EncodedStringValue("");
                }
                EncodedStringValue returnValue;
                pduDataStream.reset();
                if (first < 32) {
                    parseValueLength(pduDataStream);
                    charset = parseShortInteger(pduDataStream);
                }
                byte[] textString = parseWapString(pduDataStream, 0);
                if (charset != 0) {
                    try {
                        returnValue = new EncodedStringValue(charset, textString);
                    } catch (Exception e) {
                        return null;
                    }
                }
                returnValue = new EncodedStringValue(textString);
                return returnValue;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static byte[] parseWapString(ByteArrayInputStream pduDataStream, int stringType) {
        if (-assertionsDisabled || pduDataStream != null) {
            pduDataStream.mark(1);
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                if (1 == stringType && 34 == temp) {
                    pduDataStream.mark(1);
                } else if (stringType == 0 && ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED == temp) {
                    pduDataStream.mark(1);
                } else {
                    pduDataStream.reset();
                }
                return getWapString(pduDataStream, stringType);
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static boolean isTokenCharacter(int ch) {
        if (ch < 33 || ch > ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT) {
            return false;
        }
        switch (ch) {
            case 34:
            case 40:
            case 41:
            case 44:
            case 47:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case ConnectivityLogManager.WIFI_CLOSE_FAILED_EX /*91*/:
            case ConnectivityLogManager.WIFI_CONNECT_AUTH_FAILED_EX /*92*/:
            case ConnectivityLogManager.WIFI_CONNECT_ASSOC_FAILED_EX /*93*/:
            case 123:
            case ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
                return false;
            default:
                return true;
        }
    }

    private static boolean isText(int ch) {
        if ((ch >= 32 && ch <= ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT) || (ch >= 128 && ch <= 255)) {
            return true;
        }
        switch (ch) {
            case 9:
            case 10:
            case 13:
                return true;
            default:
                return false;
        }
    }

    private static byte[] getWapString(ByteArrayInputStream pduDataStream, int stringType) {
        if (-assertionsDisabled || pduDataStream != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                while (-1 != temp && temp != 0) {
                    if (stringType == 2) {
                        if (isTokenCharacter(temp)) {
                            out.write(temp);
                        }
                    } else if (isText(temp)) {
                        out.write(temp);
                    }
                    temp = pduDataStream.read();
                    if (!-assertionsDisabled && -1 == temp) {
                        throw new AssertionError();
                    }
                }
                if (out.size() > 0) {
                    return out.toByteArray();
                }
                return null;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static int extractByteValue(ByteArrayInputStream pduDataStream) {
        if (-assertionsDisabled || pduDataStream != null) {
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                return temp & 255;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static int parseShortInteger(ByteArrayInputStream pduDataStream) {
        if (-assertionsDisabled || pduDataStream != null) {
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                return temp & ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static long parseLongInteger(ByteArrayInputStream pduDataStream) {
        if (-assertionsDisabled || pduDataStream != null) {
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                int count = temp & 255;
                if (count > 8) {
                    throw new RuntimeException("Octet count greater than 8 and I can't represent that!");
                }
                long result = 0;
                int i = 0;
                while (i < count) {
                    temp = pduDataStream.read();
                    if (-assertionsDisabled || -1 != temp) {
                        result = (result << 8) + ((long) (temp & 255));
                        i++;
                    } else {
                        throw new AssertionError();
                    }
                }
                return result;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static long parseIntegerValue(ByteArrayInputStream pduDataStream) {
        if (-assertionsDisabled || pduDataStream != null) {
            pduDataStream.mark(1);
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                pduDataStream.reset();
                if (temp > ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED) {
                    return (long) parseShortInteger(pduDataStream);
                }
                return parseLongInteger(pduDataStream);
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static int skipWapValue(ByteArrayInputStream pduDataStream, int length) {
        if (-assertionsDisabled || pduDataStream != null) {
            int readLen = pduDataStream.read(new byte[length], 0, length);
            if (readLen < length) {
                return -1;
            }
            return readLen;
        }
        throw new AssertionError();
    }

    private static void parseContentTypeParams(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map, Integer length) {
        if (!-assertionsDisabled && pduDataStream == null) {
            throw new AssertionError();
        } else if (-assertionsDisabled || length.intValue() > 0) {
            int startPos = pduDataStream.available();
            int lastLen = length.intValue();
            while (lastLen > 0) {
                int param = pduDataStream.read();
                if (-assertionsDisabled || -1 != param) {
                    lastLen--;
                    switch (param) {
                        case 129:
                            pduDataStream.mark(1);
                            int firstValue = extractByteValue(pduDataStream);
                            pduDataStream.reset();
                            if ((firstValue <= 32 || firstValue >= 127) && firstValue != 0) {
                                int charset = (int) parseIntegerValue(pduDataStream);
                                if (map != null) {
                                    map.put(Integer.valueOf(129), Integer.valueOf(charset));
                                }
                            } else {
                                try {
                                    int charsetInt = CharacterSets.getMibEnumValue(new String(parseWapString(pduDataStream, 0), Charset.defaultCharset()));
                                    if (map != null) {
                                        map.put(Integer.valueOf(129), Integer.valueOf(charsetInt));
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    if (map != null) {
                                        map.put(Integer.valueOf(129), Integer.valueOf(0));
                                    }
                                }
                            }
                            lastLen = length.intValue() - (startPos - pduDataStream.available());
                            break;
                        case 131:
                        case 137:
                            pduDataStream.mark(1);
                            int first = extractByteValue(pduDataStream);
                            pduDataStream.reset();
                            Object type;
                            if (first > 127) {
                                int index = parseShortInteger(pduDataStream);
                                if (index < PduContentTypes.contentTypes.length) {
                                    type = PduContentTypes.contentTypes[index].getBytes(Charset.defaultCharset());
                                    if (map != null) {
                                        map.put(Integer.valueOf(131), type);
                                    }
                                }
                            } else {
                                type = parseWapString(pduDataStream, 0);
                                if (!(type == null || map == null)) {
                                    map.put(Integer.valueOf(131), type);
                                }
                            }
                            lastLen = length.intValue() - (startPos - pduDataStream.available());
                            break;
                        case 133:
                        case 151:
                            byte[] name = parseWapString(pduDataStream, 0);
                            if (!(name == null || map == null)) {
                                map.put(Integer.valueOf(151), name);
                            }
                            lastLen = length.intValue() - (startPos - pduDataStream.available());
                            break;
                        case 138:
                        case 153:
                            byte[] start = parseWapString(pduDataStream, 0);
                            if (!(start == null || map == null)) {
                                map.put(Integer.valueOf(153), start);
                            }
                            lastLen = length.intValue() - (startPos - pduDataStream.available());
                            break;
                        default:
                            if (-1 == skipWapValue(pduDataStream, lastLen)) {
                                break;
                            }
                            lastLen = 0;
                            break;
                    }
                }
                throw new AssertionError();
            }
        } else {
            throw new AssertionError();
        }
    }

    private static byte[] parseContentType(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map) {
        if (-assertionsDisabled || pduDataStream != null) {
            pduDataStream.mark(1);
            int temp = pduDataStream.read();
            if (-assertionsDisabled || -1 != temp) {
                byte[] contentType;
                pduDataStream.reset();
                int cur = temp & 255;
                if (cur < 32) {
                    int length = parseValueLength(pduDataStream);
                    int startPos = pduDataStream.available();
                    pduDataStream.mark(1);
                    temp = pduDataStream.read();
                    if (-assertionsDisabled || -1 != temp) {
                        pduDataStream.reset();
                        int first = temp & 255;
                        if (first >= 32 && first <= ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED) {
                            contentType = parseWapString(pduDataStream, 0);
                        } else if (first <= ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED) {
                            return PduContentTypes.contentTypes[0].getBytes(Charset.defaultCharset());
                        } else {
                            int index = parseShortInteger(pduDataStream);
                            if (index < PduContentTypes.contentTypes.length) {
                                contentType = PduContentTypes.contentTypes[index].getBytes(Charset.defaultCharset());
                            } else {
                                pduDataStream.reset();
                                contentType = parseWapString(pduDataStream, 0);
                            }
                        }
                        int parameterLen = length - (startPos - pduDataStream.available());
                        if (parameterLen > 0) {
                            parseContentTypeParams(pduDataStream, map, Integer.valueOf(parameterLen));
                        }
                        if (parameterLen < 0) {
                            return PduContentTypes.contentTypes[0].getBytes(Charset.defaultCharset());
                        }
                    }
                    throw new AssertionError();
                } else if (cur <= ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED) {
                    contentType = parseWapString(pduDataStream, 0);
                } else {
                    contentType = PduContentTypes.contentTypes[parseShortInteger(pduDataStream)].getBytes(Charset.defaultCharset());
                }
                return contentType;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }
}
