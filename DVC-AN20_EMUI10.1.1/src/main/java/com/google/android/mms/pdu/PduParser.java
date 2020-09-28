package com.google.android.mms.pdu;

import android.os.Bundle;
import android.util.Log;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.HwTelephonyChrManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.nano.TelephonyProto;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

public class PduParser {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean DEBUG = false;
    private static final int END_STRING_FLAG = 0;
    private static final int LENGTH_QUOTE = 31;
    private static final boolean LOCAL_LOGV = false;
    private static final String LOG_TAG = "PduParser";
    private static final int LONG_INTEGER_LENGTH_MAX = 8;
    private static final byte PDU_BODY_NULL = 2;
    private static final byte PDU_CHECK_MANDATORY_HEADER = 4;
    private static final byte PDU_CONTENT_TYPE_NULL = 3;
    private static final byte PDU_DATA_STREAM_NULL = 0;
    private static final byte PDU_HEADER_NULL = 1;
    private static final int QUOTE = 127;
    private static final int QUOTED_STRING_FLAG = 34;
    private static final int SHORT_INTEGER_MAX = 127;
    private static final int SHORT_LENGTH_MAX = 30;
    private static final int TEXT_MAX = 127;
    private static final int TEXT_MIN = 32;
    private static final int THE_FIRST_PART = 0;
    private static final int THE_LAST_PART = 1;
    private static final int TYPE_QUOTED_STRING = 1;
    private static final int TYPE_TEXT_STRING = 0;
    private static final int TYPE_TOKEN_STRING = 2;
    private static byte[] mStartParam = null;
    private static byte[] mTypeParam = null;
    private PduBody mBody = null;
    private PduHeaders mHeaders = null;
    private final boolean mParseContentDisposition;
    private ByteArrayInputStream mPduDataStream = null;

    public PduParser(byte[] pduDataStream, boolean parseContentDisposition) {
        this.mPduDataStream = new ByteArrayInputStream(pduDataStream);
        this.mParseContentDisposition = parseContentDisposition;
    }

    public GenericPdu parse() {
        ByteArrayInputStream byteArrayInputStream = this.mPduDataStream;
        if (byteArrayInputStream == null) {
            Log.v(LOG_TAG, "mPduDataStream is null");
            reportChrSmsEvent(PDU_DATA_STREAM_NULL);
            return null;
        }
        this.mHeaders = parseHeaders(byteArrayInputStream);
        PduHeaders pduHeaders = this.mHeaders;
        if (pduHeaders == null) {
            Log.v(LOG_TAG, "mHeaders is null");
            reportChrSmsEvent(PDU_HEADER_NULL);
            return null;
        }
        int messageType = pduHeaders.getOctet(140);
        Log.v(LOG_TAG, "messageType " + messageType);
        if (!checkMandatoryHeader(this.mHeaders)) {
            Log.v(LOG_TAG, "check mandatory headers failed!");
            reportChrSmsEvent(PDU_CHECK_MANDATORY_HEADER);
            return null;
        }
        boolean readreportasMessage = false;
        if (128 == messageType || 132 == messageType) {
            byte[] messageClass = this.mHeaders.getTextString(138);
            byte[] contentType = this.mHeaders.getTextString(132);
            String contentTypeStr = null;
            if (contentType != null) {
                contentTypeStr = new String(contentType);
            }
            if (messageType != 132 || messageClass == null || !Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes(Charset.defaultCharset())) || contentTypeStr == null || !contentTypeStr.equals(ContentType.TEXT_PLAIN)) {
                this.mBody = parseParts(this.mPduDataStream);
            } else {
                this.mBody = parseReadReport(this.mPduDataStream);
                readreportasMessage = true;
            }
            if (this.mBody == null) {
                Log.v(LOG_TAG, "mBody is null");
                reportChrSmsEvent(PDU_BODY_NULL);
                return null;
            }
        }
        switch (messageType) {
            case 128:
                return new SendReq(this.mHeaders, this.mBody);
            case 129:
                return new SendConf(this.mHeaders);
            case 130:
                return new NotificationInd(this.mHeaders);
            case 131:
                return new NotifyRespInd(this.mHeaders);
            case 132:
                RetrieveConf retrieveConf = new RetrieveConf(this.mHeaders, this.mBody);
                byte[] contentType2 = retrieveConf.getContentType();
                if (contentType2 == null) {
                    Log.v(LOG_TAG, "contentType is null");
                    reportChrSmsEvent(PDU_CONTENT_TYPE_NULL);
                    return null;
                }
                String ctTypeStr = new String(contentType2);
                if (ctTypeStr.equals(ContentType.MULTIPART_MIXED) || ctTypeStr.equals(ContentType.MULTIPART_RELATED) || ctTypeStr.equals(ContentType.MULTIPART_ALTERNATIVE)) {
                    return retrieveConf;
                }
                if (ctTypeStr.equals(ContentType.MULTIPART_ALTERNATIVE)) {
                    PduPart firstPart = this.mBody.getPart(0);
                    this.mBody.removeAll();
                    this.mBody.addPart(0, firstPart);
                    return retrieveConf;
                } else if (readreportasMessage) {
                    return retrieveConf;
                } else {
                    return null;
                }
            case 133:
                return new AcknowledgeInd(this.mHeaders);
            case 134:
                return new DeliveryInd(this.mHeaders);
            case 135:
                return new ReadRecInd(this.mHeaders);
            case 136:
                return new ReadOrigInd(this.mHeaders);
            default:
                log("Parser doesn't support this message type in this version!");
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public PduHeaders parseHeaders(ByteArrayInputStream pduDataStream) {
        String str;
        EncodedStringValue from;
        byte[] address;
        String str2;
        if (pduDataStream == null) {
            return null;
        }
        PduHeaders headers = new PduHeaders();
        boolean keepParsing = true;
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
                            continue;
                        } else {
                            byte[] address2 = value.getTextString();
                            if (address2 != null) {
                                String str3 = new String(address2);
                                int endIndex = str3.indexOf("/");
                                if (endIndex > 0) {
                                    str = str3.substring(0, endIndex);
                                } else {
                                    str = str3;
                                }
                                try {
                                    value.setTextString(str.getBytes());
                                } catch (NullPointerException e) {
                                    log("null pointer error!");
                                    return null;
                                }
                            }
                            try {
                                headers.appendEncodedStringValue(value, headerField);
                                break;
                            } catch (NullPointerException e2) {
                                log("null pointer error!");
                                break;
                            } catch (RuntimeException e3) {
                                log(headerField + "is not Encoded-String-Value header field!");
                                return null;
                            }
                        }
                    case 131:
                    case 139:
                    case 152:
                    case PduHeaders.REPLY_CHARGING_ID:
                    case PduHeaders.APPLIC_ID:
                    case PduHeaders.REPLY_APPLIC_ID:
                    case PduHeaders.AUX_APPLIC_ID:
                    case PduHeaders.REPLACE_ID:
                    case PduHeaders.CANCEL_ID:
                        byte[] value2 = parseWapString(pduDataStream, 0);
                        if (value2 != null) {
                            try {
                                headers.setTextString(value2, headerField);
                                break;
                            } catch (NullPointerException e4) {
                                log("null pointer error!");
                                break;
                            } catch (RuntimeException e5) {
                                log(headerField + "is not Text-String header field!");
                                return null;
                            }
                        } else {
                            continue;
                        }
                    case 132:
                        HashMap<Integer, Object> map = new HashMap<>();
                        byte[] contentType = parseContentType(pduDataStream, map);
                        if (contentType != null) {
                            try {
                                headers.setTextString(contentType, 132);
                            } catch (NullPointerException e6) {
                                log("null pointer error!");
                            } catch (RuntimeException e7) {
                                log(headerField + "is not Text-String header field!");
                                return null;
                            }
                        }
                        mStartParam = (byte[]) map.get(153);
                        mTypeParam = (byte[]) map.get(131);
                        keepParsing = false;
                        continue;
                    case 133:
                    case 142:
                    case PduHeaders.REPLY_CHARGING_SIZE:
                        try {
                            headers.setLongInteger(parseLongInteger(pduDataStream), headerField);
                            continue;
                        } catch (RuntimeException e8) {
                            log(headerField + "is not Long-Integer header field!");
                            return null;
                        }
                    case 134:
                    case 143:
                    case 144:
                    case 145:
                    case 146:
                    case 148:
                    case 149:
                    case 153:
                    case 155:
                    case 156:
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
                        int value3 = extractByteValue(pduDataStream);
                        try {
                            headers.setOctet(value3, headerField);
                            continue;
                        } catch (InvalidHeaderValueException e9) {
                            log("Set invalid Octet value: " + value3 + " into the header filed: " + headerField);
                            return null;
                        } catch (RuntimeException e10) {
                            log(headerField + "is not Octet header field!");
                            return null;
                        }
                    case 135:
                    case 136:
                    case 157:
                        parseValueLength(pduDataStream);
                        int token = extractByteValue(pduDataStream);
                        try {
                            long timeValue = parseLongInteger(pduDataStream);
                            if (129 == token) {
                                timeValue += System.currentTimeMillis() / 1000;
                            }
                            try {
                                headers.setLongInteger(timeValue, headerField);
                                continue;
                            } catch (RuntimeException e11) {
                                log(headerField + "is not Long-Integer header field!");
                                return null;
                            }
                        } catch (RuntimeException e12) {
                            log(headerField + "is not Long-Integer header field!");
                            return null;
                        }
                    case 137:
                        try {
                            parseValueLength(pduDataStream);
                        } catch (RuntimeException e13) {
                            e13.printStackTrace();
                        }
                        if (128 == extractByteValue(pduDataStream)) {
                            from = parseEncodedStringValue(pduDataStream);
                            if (!(from == null || (address = from.getTextString()) == null)) {
                                String str4 = new String(address);
                                int endIndex2 = str4.indexOf("/");
                                if (endIndex2 > 0) {
                                    str2 = str4.substring(0, endIndex2);
                                } else {
                                    str2 = str4;
                                }
                                try {
                                    from.setTextString(str2.getBytes());
                                } catch (NullPointerException e14) {
                                    log("null pointer error!");
                                    return null;
                                }
                            }
                        } else {
                            try {
                                from = new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes());
                            } catch (NullPointerException e15) {
                                log(headerField + "is not Encoded-String-Value header field!");
                                return null;
                            }
                        }
                        try {
                            headers.setEncodedStringValue(from, 137);
                            continue;
                        } catch (NullPointerException e16) {
                            log("null pointer error!");
                            break;
                        } catch (RuntimeException e17) {
                            log(headerField + "is not Encoded-String-Value header field!");
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
                                            headers.setTextString(PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes(), 138);
                                            break;
                                        }
                                    } else {
                                        headers.setTextString(PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.getBytes(), 138);
                                        break;
                                    }
                                } else {
                                    headers.setTextString(PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.getBytes(), 138);
                                    break;
                                }
                            } else {
                                try {
                                    headers.setTextString(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes(), 138);
                                    continue;
                                } catch (NullPointerException e18) {
                                    log("null pointer error!");
                                    break;
                                } catch (RuntimeException e19) {
                                    log(headerField + "is not Text-String header field!");
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
                                } catch (NullPointerException e20) {
                                    log("null pointer error!");
                                    break;
                                } catch (RuntimeException e21) {
                                    log(headerField + "is not Text-String header field!");
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
                                    continue;
                                    continue;
                                } catch (InvalidHeaderValueException e22) {
                                    log("Set invalid Octet value: " + messageType + " into the header filed: " + headerField);
                                    return null;
                                } catch (RuntimeException e23) {
                                    log(headerField + "is not Octet header field!");
                                    return null;
                                }
                        }
                    case 141:
                        int version = parseShortInteger(pduDataStream);
                        try {
                            headers.setOctet(version, 141);
                            continue;
                        } catch (InvalidHeaderValueException e24) {
                            log("Set invalid Octet value: " + version + " into the header filed: " + headerField);
                            return null;
                        } catch (RuntimeException e25) {
                            log(headerField + "is not Octet header field!");
                            return null;
                        }
                    case 147:
                    case 150:
                    case 154:
                    case PduHeaders.STORE_STATUS_TEXT:
                    case PduHeaders.RECOMMENDED_RETRIEVAL_MODE_TEXT:
                    case PduHeaders.STATUS_TEXT:
                        if (150 == headerField) {
                            pduDataStream.mark(1);
                            if ((pduDataStream.read() & 255) == 0) {
                                continue;
                            } else {
                                pduDataStream.reset();
                            }
                        }
                        EncodedStringValue value4 = parseEncodedStringValue(pduDataStream);
                        if (value4 == null) {
                            break;
                        } else {
                            try {
                                headers.setEncodedStringValue(value4, headerField);
                                break;
                            } catch (NullPointerException e26) {
                                log("null pointer error!");
                                break;
                            } catch (RuntimeException e27) {
                                log(headerField + "is not Encoded-String-Value header field!");
                                return null;
                            }
                        }
                    case 160:
                        parseValueLength(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            EncodedStringValue previouslySentBy = parseEncodedStringValue(pduDataStream);
                            if (previouslySentBy != null) {
                                try {
                                    headers.setEncodedStringValue(previouslySentBy, 160);
                                    break;
                                } catch (NullPointerException e28) {
                                    log("null pointer error!");
                                    break;
                                } catch (RuntimeException e29) {
                                    log(headerField + "is not Encoded-String-Value header field!");
                                    return null;
                                }
                            } else {
                                continue;
                            }
                        } catch (RuntimeException e30) {
                            log(headerField + " is not Integer-Value");
                            return null;
                        }
                    case PduHeaders.PREVIOUSLY_SENT_DATE:
                        parseValueLength(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            try {
                                headers.setLongInteger(parseLongInteger(pduDataStream), PduHeaders.PREVIOUSLY_SENT_DATE);
                                continue;
                            } catch (RuntimeException e31) {
                                log(headerField + "is not Long-Integer header field!");
                                return null;
                            }
                        } catch (RuntimeException e32) {
                            log(headerField + " is not Integer-Value");
                            return null;
                        }
                    case PduHeaders.MM_FLAGS:
                        parseValueLength(pduDataStream);
                        extractByteValue(pduDataStream);
                        parseEncodedStringValue(pduDataStream);
                        continue;
                    case PduHeaders.ATTRIBUTES:
                    case 174:
                    case 176:
                    default:
                        log("Unknown header");
                        continue;
                    case PduHeaders.MBOX_TOTALS:
                    case PduHeaders.MBOX_QUOTAS:
                        parseValueLength(pduDataStream);
                        extractByteValue(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            continue;
                        } catch (RuntimeException e33) {
                            log(headerField + " is not Integer-Value");
                            return null;
                        }
                    case PduHeaders.MESSAGE_COUNT:
                    case PduHeaders.START:
                    case PduHeaders.LIMIT:
                        try {
                            headers.setLongInteger(parseIntegerValue(pduDataStream), headerField);
                            continue;
                        } catch (RuntimeException e34) {
                            log(headerField + "is not Long-Integer header field!");
                            return null;
                        }
                    case PduHeaders.ELEMENT_DESCRIPTOR:
                        parseContentType(pduDataStream, null);
                        continue;
                }
            } else {
                pduDataStream.reset();
                parseWapString(pduDataStream, 0);
            }
        }
        return headers;
    }

    /* access modifiers changed from: protected */
    public PduBody parseParts(ByteArrayInputStream pduDataStream) {
        int count;
        PduBody pduBody;
        PduParser pduParser = this;
        PduBody pduBody2 = null;
        if (pduDataStream == null) {
            return null;
        }
        int count2 = parseUnsignedInt(pduDataStream);
        PduBody body = new PduBody();
        int i = 0;
        while (i < count2) {
            int headerLength = parseUnsignedInt(pduDataStream);
            int dataLength = parseUnsignedInt(pduDataStream);
            PduPart part = new PduPart();
            int startPos = pduDataStream.available();
            if (startPos <= 0) {
                return pduBody2;
            }
            HashMap<Integer, Object> map = new HashMap<>();
            byte[] contentType = parseContentType(pduDataStream, map);
            if (contentType != null) {
                part.setContentType(contentType);
            } else {
                part.setContentType(PduContentTypes.contentTypes[0].getBytes());
            }
            byte[] name = (byte[]) map.get(151);
            if (name != null) {
                part.setName(name);
            }
            Integer charset = (Integer) map.get(129);
            if (charset != null) {
                part.setCharset(charset.intValue());
            }
            int partHeaderLen = headerLength - (startPos - pduDataStream.available());
            if (partHeaderLen > 0) {
                if (!pduParser.parsePartHeaders(pduDataStream, part, partHeaderLen)) {
                    return pduBody2;
                }
            } else if (partHeaderLen < 0) {
                return pduBody2;
            }
            if (part.getContentLocation() == null && part.getName() == null && part.getFilename() == null && part.getContentId() == null) {
                part.setContentLocation(Long.toOctalString(System.currentTimeMillis()).getBytes());
            }
            if (dataLength > 0) {
                byte[] partData = new byte[dataLength];
                count = count2;
                String partContentType = new String(part.getContentType());
                pduDataStream.read(partData, 0, dataLength);
                if (partContentType.equalsIgnoreCase(ContentType.MULTIPART_ALTERNATIVE)) {
                    part = pduParser.parseParts(new ByteArrayInputStream(partData)).getPart(0);
                    pduBody = null;
                } else {
                    byte[] partDataEncoding = part.getContentTransferEncoding();
                    if (partDataEncoding != null) {
                        String encoding = new String(partDataEncoding);
                        if (encoding.equalsIgnoreCase(PduPart.P_BASE64)) {
                            partData = Base64.decodeBase64(partData);
                        } else if (encoding.equalsIgnoreCase(PduPart.P_QUOTED_PRINTABLE)) {
                            partData = QuotedPrintable.decodeQuotedPrintable(partData);
                        }
                    }
                    if (partData == null) {
                        log("Decode part data error!");
                        return null;
                    }
                    pduBody = null;
                    part.setData(partData);
                }
            } else {
                count = count2;
                pduBody = null;
            }
            if (checkPartPosition(part) == 0) {
                body.addPart(0, part);
            } else {
                body.addPart(part);
            }
            i++;
            pduBody2 = pduBody;
            count2 = count;
            pduParser = this;
        }
        return body;
    }

    private static void log(String text) {
    }

    protected static int parseUnsignedInt(ByteArrayInputStream pduDataStream) {
        int result = 0;
        int temp = pduDataStream.read();
        if (temp == -1) {
            return temp;
        }
        while ((temp & 128) != 0) {
            result = (result << 7) | (temp & 127);
            temp = pduDataStream.read();
            if (temp == -1) {
                return temp;
            }
        }
        return (result << 7) | (temp & 127);
    }

    protected static int parseValueLength(ByteArrayInputStream pduDataStream) {
        int first = pduDataStream.read() & 255;
        if (first <= 30) {
            return first;
        }
        if (first == 31) {
            return parseUnsignedInt(pduDataStream);
        }
        throw new RuntimeException("Value length > LENGTH_QUOTE!");
    }

    protected static EncodedStringValue parseEncodedStringValue(ByteArrayInputStream pduDataStream) {
        pduDataStream.mark(1);
        int charset = 0;
        int first = pduDataStream.read() & 255;
        if (first == 0) {
            return new EncodedStringValue(PhoneConfigurationManager.SSSS);
        }
        pduDataStream.reset();
        if (first < 32) {
            parseValueLength(pduDataStream);
            charset = parseShortInteger(pduDataStream);
        }
        byte[] textString = parseWapString(pduDataStream, 0);
        if (charset == 0) {
            return new EncodedStringValue(textString);
        }
        try {
            return new EncodedStringValue(charset, textString);
        } catch (Exception e) {
            return null;
        }
    }

    protected static byte[] parseWapString(ByteArrayInputStream pduDataStream, int stringType) {
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

    protected static boolean isTokenCharacter(int ch) {
        if (!(ch < 33 || ch > 126 || ch == 34 || ch == 44 || ch == 47 || ch == 123 || ch == 125 || ch == 40 || ch == 41)) {
            switch (ch) {
                case 58:
                case 59:
                case 60:
                case TelephonyProto.RilErrno.RIL_E_NETWORK_NOT_READY:
                case 62:
                case 63:
                case 64:
                    break;
                default:
                    switch (ch) {
                        case CallFailCause.INVALID_TRANSIT_NETWORK_SELECTION:
                        case 92:
                        case 93:
                            break;
                        default:
                            return true;
                    }
            }
        }
        return false;
    }

    protected static boolean isText(int ch) {
        if ((ch < 32 || ch > 126) && ((ch < 128 || ch > 255) && ch != 9 && ch != 10 && ch != 13)) {
            return false;
        }
        return true;
    }

    protected static byte[] getWapString(ByteArrayInputStream pduDataStream, int stringType) {
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

    protected static int extractByteValue(ByteArrayInputStream pduDataStream) {
        return pduDataStream.read() & 255;
    }

    protected static int parseShortInteger(ByteArrayInputStream pduDataStream) {
        return pduDataStream.read() & 127;
    }

    protected static long parseLongInteger(ByteArrayInputStream pduDataStream) {
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

    protected static long parseIntegerValue(ByteArrayInputStream pduDataStream) {
        pduDataStream.mark(1);
        int temp = pduDataStream.read();
        pduDataStream.reset();
        if (temp > 127) {
            return (long) parseShortInteger(pduDataStream);
        }
        return parseLongInteger(pduDataStream);
    }

    protected static int skipWapValue(ByteArrayInputStream pduDataStream, int length) {
        int readLen = pduDataStream.read(new byte[length], 0, length);
        if (readLen < length) {
            return -1;
        }
        return readLen;
    }

    protected static void parseContentTypeParams(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map, Integer length) {
        int startPos = pduDataStream.available();
        int lastLen = length.intValue();
        while (lastLen > 0) {
            int param = pduDataStream.read();
            lastLen--;
            if (param != 129) {
                if (param != 131) {
                    if (param == 133 || param == 151) {
                        byte[] name = parseWapString(pduDataStream, 0);
                        if (!(name == null || map == null)) {
                            map.put(151, name);
                        }
                        lastLen = length.intValue() - (startPos - pduDataStream.available());
                    } else {
                        if (param != 153) {
                            if (param != 137) {
                                if (param != 138) {
                                    if (-1 == skipWapValue(pduDataStream, lastLen)) {
                                        Log.e(LOG_TAG, "Corrupt Content-Type");
                                    } else {
                                        lastLen = 0;
                                    }
                                }
                            }
                        }
                        byte[] start = parseWapString(pduDataStream, 0);
                        if (!(start == null || map == null)) {
                            map.put(153, start);
                        }
                        lastLen = length.intValue() - (startPos - pduDataStream.available());
                    }
                }
                pduDataStream.mark(1);
                int first = extractByteValue(pduDataStream);
                pduDataStream.reset();
                if (first > 127) {
                    int index = parseShortInteger(pduDataStream);
                    if (index < PduContentTypes.contentTypes.length) {
                        map.put(131, PduContentTypes.contentTypes[index].getBytes());
                    }
                } else {
                    byte[] type = parseWapString(pduDataStream, 0);
                    if (!(type == null || map == null)) {
                        map.put(131, type);
                    }
                }
                lastLen = length.intValue() - (startPos - pduDataStream.available());
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
                    byte[] charsetStr = parseWapString(pduDataStream, 0);
                    try {
                        map.put(129, Integer.valueOf(CharacterSets.getMibEnumValue(new String(charsetStr))));
                    } catch (UnsupportedEncodingException e) {
                        Log.e(LOG_TAG, Arrays.toString(charsetStr), e);
                        map.put(129, 0);
                    }
                }
                lastLen = length.intValue() - (startPos - pduDataStream.available());
            }
        }
        if (lastLen != 0) {
            Log.e(LOG_TAG, "Corrupt Content-Type");
        }
    }

    protected static byte[] parseContentType(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map) {
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
            } else if (first > 127) {
                int index = parseShortInteger(pduDataStream);
                if (index < PduContentTypes.contentTypes.length) {
                    contentType = PduContentTypes.contentTypes[index].getBytes();
                } else {
                    pduDataStream.reset();
                    contentType = parseWapString(pduDataStream, 0);
                }
            } else {
                Log.e(LOG_TAG, "Corrupt content-type");
                return PduContentTypes.contentTypes[0].getBytes();
            }
            int parameterLen = length - (startPos - pduDataStream.available());
            if (parameterLen > 0) {
                parseContentTypeParams(pduDataStream, map, Integer.valueOf(parameterLen));
            }
            if (parameterLen >= 0) {
                return contentType;
            }
            Log.e(LOG_TAG, "Corrupt MMS message");
            return PduContentTypes.contentTypes[0].getBytes();
        } else if (cur <= 127) {
            return parseWapString(pduDataStream, 0);
        } else {
            return PduContentTypes.contentTypes[parseShortInteger(pduDataStream)].getBytes();
        }
    }

    /* access modifiers changed from: protected */
    public boolean parsePartHeaders(ByteArrayInputStream pduDataStream, PduPart part, int length) {
        int startPos = pduDataStream.available();
        int lastLen = length;
        while (lastLen > 0) {
            int header = pduDataStream.read();
            lastLen--;
            if (header > 127) {
                if (header != 142) {
                    if (header != 174) {
                        if (header == 192) {
                            byte[] contentId = parseWapString(pduDataStream, 1);
                            if (contentId != null) {
                                part.setContentId(contentId);
                            }
                            lastLen = length - (startPos - pduDataStream.available());
                        } else if (header != 197) {
                            if (-1 == skipWapValue(pduDataStream, lastLen)) {
                                Log.e(LOG_TAG, "Corrupt Part headers");
                                return false;
                            }
                            lastLen = 0;
                        }
                    }
                    if (this.mParseContentDisposition) {
                        int len = parseValueLength(pduDataStream);
                        pduDataStream.mark(1);
                        int thisStartPos = pduDataStream.available();
                        int value = pduDataStream.read();
                        if (value == 128) {
                            part.setContentDisposition(PduPart.DISPOSITION_FROM_DATA);
                        } else if (value == 129) {
                            part.setContentDisposition(PduPart.DISPOSITION_ATTACHMENT);
                        } else if (value == 130) {
                            part.setContentDisposition(PduPart.DISPOSITION_INLINE);
                        } else {
                            pduDataStream.reset();
                            part.setContentDisposition(parseWapString(pduDataStream, 0));
                        }
                        if (thisStartPos - pduDataStream.available() < len) {
                            if (pduDataStream.read() == 152) {
                                part.setFilename(parseWapString(pduDataStream, 0));
                            }
                            int thisEndPos = pduDataStream.available();
                            if (thisStartPos - thisEndPos < len) {
                                int last = len - (thisStartPos - thisEndPos);
                                pduDataStream.read(new byte[last], 0, last);
                            }
                        }
                        lastLen = length - (startPos - pduDataStream.available());
                    }
                } else {
                    byte[] contentLocation = parseWapString(pduDataStream, 0);
                    if (contentLocation != null) {
                        part.setContentLocation(contentLocation);
                    }
                    lastLen = length - (startPos - pduDataStream.available());
                }
            } else if (header >= 32 && header <= 127) {
                byte[] tempHeader = parseWapString(pduDataStream, 0);
                byte[] tempValue = parseWapString(pduDataStream, 0);
                if (true == PduPart.CONTENT_TRANSFER_ENCODING.equalsIgnoreCase(new String(tempHeader))) {
                    part.setContentTransferEncoding(tempValue);
                }
                lastLen = length - (startPos - pduDataStream.available());
            } else if (-1 == skipWapValue(pduDataStream, lastLen)) {
                Log.e(LOG_TAG, "Corrupt Part headers");
                return false;
            } else {
                lastLen = 0;
            }
        }
        if (lastLen == 0) {
            return true;
        }
        Log.e(LOG_TAG, "Corrupt Part headers");
        return false;
    }

    private static int checkPartPosition(PduPart part) {
        byte[] contentType;
        if (mTypeParam == null && mStartParam == null) {
            return 1;
        }
        if (mStartParam != null) {
            byte[] contentId = part.getContentId();
            if (contentId == null || true != Arrays.equals(mStartParam, contentId)) {
                return 1;
            }
            return 0;
        } else if (mTypeParam == null || (contentType = part.getContentType()) == null || true != Arrays.equals(mTypeParam, contentType)) {
            return 1;
        } else {
            return 0;
        }
    }

    protected static boolean checkMandatoryHeader(PduHeaders headers) {
        if (headers == null) {
            return false;
        }
        int messageType = headers.getOctet(140);
        if (headers.getOctet(141) == 0) {
            return false;
        }
        switch (messageType) {
            case 128:
                if (headers.getTextString(132) == null || headers.getEncodedStringValue(137) == null || headers.getTextString(152) == null) {
                    return false;
                }
                return true;
            case 129:
                if (headers.getOctet(146) == 0 || headers.getTextString(152) == null) {
                    return false;
                }
                return true;
            case 130:
                if (headers.getTextString(131) == null || -1 == headers.getLongInteger(136) || headers.getTextString(138) == null || -1 == headers.getLongInteger(142) || headers.getTextString(152) == null) {
                    return false;
                }
                return true;
            case 131:
                if (headers.getOctet(149) == 0 || headers.getTextString(152) == null) {
                    return false;
                }
                return true;
            case 132:
                if (headers.getTextString(132) == null || -1 == headers.getLongInteger(133)) {
                    return false;
                }
                return true;
            case 133:
                if (headers.getTextString(152) == null) {
                    return false;
                }
                return true;
            case 134:
                if (-1 == headers.getLongInteger(133) || headers.getTextString(139) == null || headers.getOctet(149) == 0 || headers.getEncodedStringValues(151) == null) {
                    return false;
                }
                return true;
            case 135:
                if (headers.getEncodedStringValue(137) == null || headers.getTextString(139) == null || headers.getOctet(155) == 0 || headers.getEncodedStringValues(151) == null) {
                    return false;
                }
                return true;
            case 136:
                if (-1 == headers.getLongInteger(133) || headers.getEncodedStringValue(137) == null || headers.getTextString(139) == null || headers.getOctet(155) == 0 || headers.getEncodedStringValues(151) == null) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    private void reportChrSmsEvent(byte failCause) {
        Log.v(LOG_TAG, "repor chr parse fail");
        Bundle data = new Bundle();
        data.putString("EventScenario", HwTelephonyChrManager.Scenario.SMS);
        data.putInt("EventFailCause", 2001);
        data.putByte("SMS.PDUPARSE.parseFailCause", failCause);
        HwTelephonyFactory.getHwTelephonyChrManager().sendTelephonyChrBroadcast(data);
    }

    protected static PduBody parseReadReport(ByteArrayInputStream pduDataStream) {
        PduBody body = new PduBody();
        PduPart part = new PduPart();
        part.setContentType(PduContentTypes.contentTypes[3].getBytes(Charset.defaultCharset()));
        part.setContentLocation(Long.toOctalString(System.currentTimeMillis()).getBytes(Charset.defaultCharset()));
        int dataLength = pduDataStream.available();
        if (dataLength > 0) {
            byte[] partData = new byte[dataLength];
            pduDataStream.read(partData, 0, dataLength);
            part.setData(partData);
        }
        body.addPart(part);
        return body;
    }
}
