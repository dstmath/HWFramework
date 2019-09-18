package com.huawei.hsm.permission.minimms;

import android.hishow.AlarmInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;

public class PduParser {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    /* JADX WARNING: Code restructure failed: missing block: B:185:0x000a, code lost:
        continue;
     */
    private PduHeaders parseHeaders(ByteArrayInputStream pduDataStream) {
        EncodedStringValue from;
        if (pduDataStream == null) {
            return null;
        }
        boolean keepParsing = true;
        PduHeaders headers = new PduHeaders();
        while (keepParsing && pduDataStream.available() > 0) {
            pduDataStream.mark(1);
            int headerField = extractByteValue(pduDataStream);
            if (headerField < 32 || headerField > 127) {
                switch (headerField) {
                    case 129:
                    case 130:
                    case 151:
                        EncodedStringValue value = parseEncodedStringValue(pduDataStream);
                        if (value == null) {
                            break;
                        } else {
                            byte[] address = value.getTextString();
                            if (address != null) {
                                String str = new String(address, Charset.defaultCharset());
                                int endIndex = str.indexOf("/");
                                if (endIndex > 0) {
                                    str = str.substring(0, endIndex);
                                }
                                try {
                                    value.setTextString(str.getBytes(Charset.defaultCharset()));
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
                    case 131:
                    case 139:
                    case PduHeaders.TRANSACTION_ID:
                    case PduHeaders.REPLY_CHARGING_ID:
                    case PduHeaders.APPLIC_ID:
                    case PduHeaders.REPLY_APPLIC_ID:
                    case PduHeaders.AUX_APPLIC_ID:
                    case PduHeaders.REPLACE_ID:
                    case PduHeaders.CANCEL_ID:
                        byte[] value2 = parseWapString(pduDataStream, 0);
                        if (value2 == null) {
                            break;
                        } else {
                            try {
                                headers.setTextString(value2, headerField);
                                break;
                            } catch (NullPointerException e4) {
                                break;
                            } catch (RuntimeException e5) {
                                return null;
                            }
                        }
                    case 132:
                        byte[] contentType = parseContentType(pduDataStream, new HashMap<>());
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
                    case PduHeaders.REPLY_CHARGING_SIZE:
                        try {
                            headers.setLongInteger(parseLongInteger(pduDataStream), headerField);
                            break;
                        } catch (RuntimeException e8) {
                            return null;
                        }
                    case PduHeaders.DELIVERY_REPORT:
                    case 143:
                    case 144:
                    case 145:
                    case 146:
                    case 148:
                    case 149:
                    case 153:
                    case PduHeaders.READ_STATUS:
                    case PduHeaders.REPLY_CHARGING:
                    case PduHeaders.STORE:
                    case PduHeaders.MM_STATE:
                    case PduHeaders.STORE_STATUS:
                    case PduHeaders.STORED:
                    case PduHeaders.TOTALS:
                    case PduHeaders.QUOTAS:
                    case PduHeaders.DISTRIBUTION_INDICATOR:
                    case PduHeaders.RECOMMENDED_RETRIEVAL_MODE:
                    case PduHeaders.CONTENT_CLASS:
                    case PduHeaders.DRM_CONTENT:
                    case PduHeaders.ADAPTATION_ALLOWED:
                    case PduHeaders.CANCEL_STATUS:
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
                    case PduHeaders.REPLY_CHARGING_DEADLINE:
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
                        parseValueLength(pduDataStream);
                        if (128 == extractByteValue(pduDataStream)) {
                            from = parseEncodedStringValue(pduDataStream);
                            if (from != null) {
                                byte[] address2 = from.getTextString();
                                if (address2 != null) {
                                    String str2 = new String(address2, Charset.defaultCharset());
                                    int endIndex2 = str2.indexOf("/");
                                    if (endIndex2 > 0) {
                                        str2 = str2.substring(0, endIndex2);
                                    }
                                    try {
                                        from.setTextString(str2.getBytes(Charset.defaultCharset()));
                                    } catch (NullPointerException e13) {
                                        return null;
                                    }
                                }
                            }
                        } else {
                            try {
                                from = new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes(Charset.defaultCharset()));
                            } catch (NullPointerException e14) {
                                return null;
                            }
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
                                        } else {
                                            headers.setTextString(PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes(Charset.defaultCharset()), 138);
                                            break;
                                        }
                                    } else {
                                        headers.setTextString(PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.getBytes(Charset.defaultCharset()), 138);
                                        break;
                                    }
                                } else {
                                    headers.setTextString(PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.getBytes(Charset.defaultCharset()), 138);
                                    break;
                                }
                            } else {
                                try {
                                    headers.setTextString(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes(Charset.defaultCharset()), 138);
                                    break;
                                } catch (NullPointerException e17) {
                                    break;
                                } catch (RuntimeException e18) {
                                    return null;
                                }
                            }
                        } else {
                            pduDataStream.reset();
                            byte[] messageClassString = parseWapString(pduDataStream, 0);
                            if (messageClassString == null) {
                                break;
                            } else {
                                try {
                                    headers.setTextString(messageClassString, 138);
                                    break;
                                } catch (NullPointerException e19) {
                                    break;
                                } catch (RuntimeException e20) {
                                    return null;
                                }
                            }
                        }
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
                    case PduHeaders.RETRIEVE_TEXT:
                    case PduHeaders.STORE_STATUS_TEXT:
                    case PduHeaders.RECOMMENDED_RETRIEVAL_MODE_TEXT:
                    case PduHeaders.STATUS_TEXT:
                        if (150 == headerField) {
                            pduDataStream.mark(1);
                            if ((pduDataStream.read() & 255) == 0) {
                                break;
                            } else {
                                pduDataStream.reset();
                            }
                        }
                        EncodedStringValue value3 = parseEncodedStringValue(pduDataStream);
                        if (value3 == null) {
                            break;
                        } else {
                            try {
                                headers.setEncodedStringValue(value3, headerField);
                                break;
                            } catch (NullPointerException e25) {
                                break;
                            } catch (RuntimeException e26) {
                                return null;
                            }
                        }
                    case PduHeaders.PREVIOUSLY_SENT_BY:
                        parseValueLength(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            EncodedStringValue previouslySentBy = parseEncodedStringValue(pduDataStream);
                            if (previouslySentBy == null) {
                                break;
                            } else {
                                try {
                                    headers.setEncodedStringValue(previouslySentBy, PduHeaders.PREVIOUSLY_SENT_BY);
                                    break;
                                } catch (NullPointerException e27) {
                                    break;
                                } catch (RuntimeException e28) {
                                    return null;
                                }
                            }
                        } catch (RuntimeException e29) {
                            return null;
                        }
                    case PduHeaders.PREVIOUSLY_SENT_DATE:
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
                    case PduHeaders.MM_FLAGS:
                        parseValueLength(pduDataStream);
                        extractByteValue(pduDataStream);
                        parseEncodedStringValue(pduDataStream);
                        break;
                    case PduHeaders.MBOX_TOTALS:
                    case PduHeaders.MBOX_QUOTAS:
                        parseValueLength(pduDataStream);
                        extractByteValue(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            break;
                        } catch (RuntimeException e32) {
                            return null;
                        }
                    case PduHeaders.MESSAGE_COUNT:
                    case PduHeaders.START:
                    case PduHeaders.LIMIT:
                        try {
                            headers.setLongInteger(parseIntegerValue(pduDataStream), headerField);
                            break;
                        } catch (RuntimeException e33) {
                            return null;
                        }
                    case PduHeaders.ELEMENT_DESCRIPTOR:
                        parseContentType(pduDataStream, null);
                        break;
                }
            } else {
                pduDataStream.reset();
                parseWapString(pduDataStream, 0);
            }
        }
        return headers;
    }

    private static int parseUnsignedInt(ByteArrayInputStream pduDataStream) {
        int result = 0;
        int temp = pduDataStream.read();
        if (temp == -1) {
            return temp;
        }
        while ((temp & 128) != 0) {
            result = (result << 7) | (temp & AlarmInfo.EVERYDAY_CODE);
            temp = pduDataStream.read();
            if (temp == -1) {
                return temp;
            }
        }
        return (result << 7) | (temp & AlarmInfo.EVERYDAY_CODE);
    }

    private static int parseValueLength(ByteArrayInputStream pduDataStream) {
        int first = pduDataStream.read() & 255;
        if (first <= 30) {
            return first;
        }
        if (first == 31) {
            return parseUnsignedInt(pduDataStream);
        }
        throw new RuntimeException("Value length > LENGTH_QUOTE!");
    }

    private static EncodedStringValue parseEncodedStringValue(ByteArrayInputStream pduDataStream) {
        EncodedStringValue returnValue;
        pduDataStream.mark(1);
        int charset = 0;
        int first = pduDataStream.read() & 255;
        if (first == 0) {
            return new EncodedStringValue("");
        }
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
        } else {
            returnValue = new EncodedStringValue(textString);
        }
        return returnValue;
    }

    private static byte[] parseWapString(ByteArrayInputStream pduDataStream, int stringType) {
        pduDataStream.mark(1);
        int temp = pduDataStream.read();
        if (1 == stringType && 34 == temp) {
            pduDataStream.mark(1);
        } else if (stringType == 0 && 127 == temp) {
            pduDataStream.mark(1);
        } else {
            pduDataStream.reset();
        }
        return getWapString(pduDataStream, stringType);
    }

    private static boolean isTokenCharacter(int ch) {
        if (!(ch < 33 || ch > 126 || ch == 34 || ch == 44 || ch == 47 || ch == 123 || ch == 125)) {
            switch (ch) {
                case 40:
                case 41:
                    break;
                default:
                    switch (ch) {
                        case 58:
                        case 59:
                        case 60:
                        case 61:
                        case 62:
                        case 63:
                        case 64:
                            break;
                        default:
                            switch (ch) {
                                case 91:
                                case 92:
                                case 93:
                                    break;
                                default:
                                    return true;
                            }
                    }
            }
        }
        return false;
    }

    private static boolean isText(int ch) {
        if ((ch < 32 || ch > 126) && ((ch < 128 || ch > 255) && ch != 13)) {
            switch (ch) {
                case 9:
                case 10:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private static byte[] getWapString(ByteArrayInputStream pduDataStream, int stringType) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int temp = pduDataStream.read();
        while (-1 != temp && temp != 0) {
            if (stringType == 2) {
                if (isTokenCharacter(temp)) {
                    out.write(temp);
                }
            } else if (isText(temp)) {
                out.write(temp);
            }
            temp = pduDataStream.read();
        }
        if (out.size() > 0) {
            return out.toByteArray();
        }
        return null;
    }

    private static int extractByteValue(ByteArrayInputStream pduDataStream) {
        return pduDataStream.read() & 255;
    }

    private static int parseShortInteger(ByteArrayInputStream pduDataStream) {
        return pduDataStream.read() & AlarmInfo.EVERYDAY_CODE;
    }

    private static long parseLongInteger(ByteArrayInputStream pduDataStream) {
        int count = pduDataStream.read() & 255;
        if (count <= 8) {
            long result = 0;
            for (int i = 0; i < count; i++) {
                result = (result << 8) + ((long) (pduDataStream.read() & 255));
            }
            return result;
        }
        throw new RuntimeException("Octet count greater than 8 and I can't represent that!");
    }

    private static long parseIntegerValue(ByteArrayInputStream pduDataStream) {
        pduDataStream.mark(1);
        int temp = pduDataStream.read();
        pduDataStream.reset();
        if (temp > 127) {
            return (long) parseShortInteger(pduDataStream);
        }
        return parseLongInteger(pduDataStream);
    }

    private static int skipWapValue(ByteArrayInputStream pduDataStream, int length) {
        int readLen = pduDataStream.read(new byte[length], 0, length);
        if (readLen < length) {
            return -1;
        }
        return readLen;
    }

    private static void parseContentTypeParams(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map, Integer length) {
        int tempPos;
        int lastLen;
        int lastLen2;
        int tempPos2;
        int startPos = pduDataStream.available();
        int lastLen3 = length.intValue();
        while (lastLen3 > 0) {
            int param = pduDataStream.read();
            lastLen3--;
            if (param != 129) {
                if (param != 131) {
                    if (param == 133 || param == 151) {
                        byte[] name = parseWapString(pduDataStream, 0);
                        if (!(name == null || map == null)) {
                            map.put(151, name);
                        }
                        tempPos2 = pduDataStream.available();
                        lastLen2 = length.intValue();
                    } else {
                        if (param != 153) {
                            switch (param) {
                                case 137:
                                    break;
                                case 138:
                                    break;
                                default:
                                    if (-1 != skipWapValue(pduDataStream, lastLen3)) {
                                        lastLen3 = 0;
                                        break;
                                    } else {
                                        break;
                                    }
                            }
                        }
                        byte[] start = parseWapString(pduDataStream, 0);
                        if (!(start == null || map == null)) {
                            map.put(153, start);
                        }
                        tempPos2 = pduDataStream.available();
                        lastLen2 = length.intValue();
                    }
                    lastLen3 = lastLen2 - (startPos - tempPos2);
                }
                pduDataStream.mark(1);
                int first = extractByteValue(pduDataStream);
                pduDataStream.reset();
                if (first > 127) {
                    int index = parseShortInteger(pduDataStream);
                    if (index < PduContentTypes.contentTypes.length) {
                        byte[] type = PduContentTypes.contentTypes[index].getBytes(Charset.defaultCharset());
                        if (map != null) {
                            map.put(131, type);
                        }
                    }
                } else {
                    byte[] type2 = parseWapString(pduDataStream, 0);
                    if (!(type2 == null || map == null)) {
                        map.put(131, type2);
                    }
                }
                tempPos = pduDataStream.available();
                lastLen = length.intValue();
            } else {
                pduDataStream.mark(1);
                int firstValue = extractByteValue(pduDataStream);
                pduDataStream.reset();
                if ((firstValue <= 32 || firstValue >= 127) && firstValue != 0) {
                    int charset = (int) parseIntegerValue(pduDataStream);
                    if (map != null) {
                        map.put(129, Integer.valueOf(charset));
                    }
                } else {
                    try {
                        int charsetInt = CharacterSets.getMibEnumValue(new String(parseWapString(pduDataStream, 0), Charset.defaultCharset()));
                        if (map != null) {
                            map.put(129, Integer.valueOf(charsetInt));
                        }
                    } catch (UnsupportedEncodingException e) {
                        if (map != null) {
                            map.put(129, 0);
                        }
                    }
                }
                tempPos = pduDataStream.available();
                lastLen = length.intValue();
            }
            lastLen3 = lastLen - (startPos - tempPos);
        }
    }

    private static byte[] parseContentType(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map) {
        byte[] contentType;
        pduDataStream.mark(1);
        int temp = pduDataStream.read();
        pduDataStream.reset();
        int cur = temp & 255;
        if (cur < 32) {
            int length = parseValueLength(pduDataStream);
            int startPos = pduDataStream.available();
            pduDataStream.mark(1);
            int temp2 = pduDataStream.read();
            pduDataStream.reset();
            int first = temp2 & 255;
            if (first >= 32 && first <= 127) {
                contentType = parseWapString(pduDataStream, 0);
            } else if (first <= 127) {
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
        } else if (cur <= 127) {
            contentType = parseWapString(pduDataStream, 0);
        } else {
            contentType = PduContentTypes.contentTypes[parseShortInteger(pduDataStream)].getBytes(Charset.defaultCharset());
        }
        return contentType;
    }
}
