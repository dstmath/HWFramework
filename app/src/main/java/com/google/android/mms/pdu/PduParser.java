package com.google.android.mms.pdu;

import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.RadioNVItems;
import com.android.internal.telephony.WspTypeDecoder;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

public class PduParser {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final boolean DEBUG = false;
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
    private static final int THE_FIRST_PART = 0;
    private static final int THE_LAST_PART = 1;
    private static final int TYPE_QUOTED_STRING = 1;
    private static final int TYPE_TEXT_STRING = 0;
    private static final int TYPE_TOKEN_STRING = 2;
    private static byte[] mStartParam;
    private static byte[] mTypeParam;
    private PduBody mBody;
    private PduHeaders mHeaders;
    private final boolean mParseContentDisposition;
    private ByteArrayInputStream mPduDataStream;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.mms.pdu.PduParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.mms.pdu.PduParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.PduParser.<clinit>():void");
    }

    public PduParser(byte[] pduDataStream, boolean parseContentDisposition) {
        this.mPduDataStream = null;
        this.mHeaders = null;
        this.mBody = null;
        this.mPduDataStream = new ByteArrayInputStream(pduDataStream);
        this.mParseContentDisposition = parseContentDisposition;
    }

    public GenericPdu parse() {
        if (this.mPduDataStream == null) {
            return null;
        }
        this.mHeaders = parseHeaders(this.mPduDataStream);
        if (this.mHeaders == null) {
            return null;
        }
        int messageType = this.mHeaders.getOctet(PduPart.P_DEP_COMMENT);
        if (checkMandatoryHeader(this.mHeaders)) {
            byte[] contentType;
            boolean readreportasMessage = LOCAL_LOGV;
            if (128 == messageType || 132 == messageType) {
                byte[] messageClass = this.mHeaders.getTextString(PduPart.P_DEP_START);
                contentType = this.mHeaders.getTextString(PduHeaders.STATUS_UNRECOGNIZED);
                String str = null;
                if (contentType != null) {
                    str = new String(contentType);
                }
                if (messageType == 132 && messageClass != null) {
                    if (Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes(Charset.defaultCharset())) && str != null) {
                        if (str.equals(ContentType.TEXT_PLAIN)) {
                            this.mBody = parseReadReport(this.mPduDataStream);
                            readreportasMessage = true;
                            if (this.mBody == null) {
                                return null;
                            }
                        }
                    }
                }
                this.mBody = parseParts(this.mPduDataStream);
                if (this.mBody == null) {
                    return null;
                }
            }
            switch (messageType) {
                case PduPart.P_Q /*128*/:
                    return new SendReq(this.mHeaders, this.mBody);
                case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                    return new SendConf(this.mHeaders);
                case PduPart.P_LEVEL /*130*/:
                    return new NotificationInd(this.mHeaders);
                case PduPart.P_TYPE /*131*/:
                    return new NotifyRespInd(this.mHeaders);
                case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                    RetrieveConf retrieveConf = new RetrieveConf(this.mHeaders, this.mBody);
                    contentType = retrieveConf.getContentType();
                    if (contentType == null) {
                        return null;
                    }
                    String ctTypeStr = new String(contentType);
                    if (!ctTypeStr.equals(ContentType.MULTIPART_MIXED)) {
                        if (!ctTypeStr.equals(ContentType.MULTIPART_RELATED)) {
                            if (!ctTypeStr.equals(ContentType.MULTIPART_ALTERNATIVE)) {
                                if (ctTypeStr.equals(ContentType.MULTIPART_ALTERNATIVE)) {
                                    PduPart firstPart = this.mBody.getPart(TYPE_TEXT_STRING);
                                    this.mBody.removeAll();
                                    this.mBody.addPart(TYPE_TEXT_STRING, firstPart);
                                    return retrieveConf;
                                } else if (readreportasMessage) {
                                    return retrieveConf;
                                } else {
                                    return null;
                                }
                            }
                        }
                    }
                    return retrieveConf;
                case PduPart.P_DEP_NAME /*133*/:
                    return new AcknowledgeInd(this.mHeaders);
                case PduPart.P_DEP_FILENAME /*134*/:
                    return new DeliveryInd(this.mHeaders);
                case PduPart.P_DIFFERENCES /*135*/:
                    return new ReadRecInd(this.mHeaders);
                case PduPart.P_PADDING /*136*/:
                    return new ReadOrigInd(this.mHeaders);
                default:
                    log("Parser doesn't support this message type in this version!");
                    return null;
            }
        }
        log("check mandatory headers failed!");
        return null;
    }

    protected PduHeaders parseHeaders(ByteArrayInputStream pduDataStream) {
        if (pduDataStream == null) {
            return null;
        }
        boolean keepParsing = true;
        PduHeaders headers = new PduHeaders();
        while (keepParsing && pduDataStream.available() > 0) {
            pduDataStream.mark(TYPE_QUOTED_STRING);
            int headerField = extractByteValue(pduDataStream);
            if (headerField < TEXT_MIN || headerField > TEXT_MAX) {
                EncodedStringValue value;
                byte[] address;
                String str;
                int endIndex;
                String str2;
                switch (headerField) {
                    case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                    case PduPart.P_LEVEL /*130*/:
                    case PduPart.P_NAME /*151*/:
                        value = parseEncodedStringValue(pduDataStream);
                        if (value != null) {
                            address = value.getTextString();
                            if (address != null) {
                                str = new String(address);
                                endIndex = str.indexOf("/");
                                if (endIndex > 0) {
                                    str2 = str.substring(TYPE_TEXT_STRING, endIndex);
                                }
                                try {
                                    value.setTextString(str2.getBytes());
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
                        continue;
                    case PduPart.P_TYPE /*131*/:
                    case PduPart.P_DEP_START_INFO /*139*/:
                    case PduPart.P_FILENAME /*152*/:
                    case PduHeaders.REPLY_CHARGING_ID /*158*/:
                    case PduHeaders.APPLIC_ID /*183*/:
                    case PduHeaders.REPLY_APPLIC_ID /*184*/:
                    case PduHeaders.AUX_APPLIC_ID /*185*/:
                    case PduHeaders.REPLACE_ID /*189*/:
                    case PduHeaders.CANCEL_ID /*190*/:
                        byte[] value2 = parseWapString(pduDataStream, TYPE_TEXT_STRING);
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
                        }
                        continue;
                    case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                        HashMap<Integer, Object> map = new HashMap();
                        byte[] contentType = parseContentType(pduDataStream, map);
                        if (contentType != null) {
                            try {
                                headers.setTextString(contentType, PduHeaders.STATUS_UNRECOGNIZED);
                            } catch (NullPointerException e6) {
                                log("null pointer error!");
                            } catch (RuntimeException e7) {
                                log(headerField + "is not Text-String header field!");
                                return null;
                            }
                        }
                        mStartParam = (byte[]) map.get(Integer.valueOf(PduPart.P_START));
                        mTypeParam = (byte[]) map.get(Integer.valueOf(PduPart.P_TYPE));
                        keepParsing = LOCAL_LOGV;
                        break;
                    case PduPart.P_DEP_NAME /*133*/:
                    case PduPart.P_MAX_AGE /*142*/:
                    case PduHeaders.REPLY_CHARGING_SIZE /*159*/:
                        try {
                            headers.setLongInteger(parseLongInteger(pduDataStream), headerField);
                            break;
                        } catch (RuntimeException e8) {
                            log(headerField + "is not Long-Integer header field!");
                            return null;
                        }
                    case PduPart.P_DEP_FILENAME /*134*/:
                    case PduPart.P_DEP_PATH /*143*/:
                    case PduPart.P_SECURE /*144*/:
                    case PduPart.P_SEC /*145*/:
                    case PduPart.P_MAC /*146*/:
                    case PduPart.P_MODIFICATION_DATE /*148*/:
                    case PduPart.P_READ_DATE /*149*/:
                    case PduPart.P_START /*153*/:
                    case PduPart.P_COMMENT /*155*/:
                    case PduPart.P_DOMAIN /*156*/:
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
                        int value3 = extractByteValue(pduDataStream);
                        try {
                            headers.setOctet(value3, headerField);
                            break;
                        } catch (InvalidHeaderValueException e9) {
                            log("Set invalid Octet value: " + value3 + " into the header filed: " + headerField);
                            return null;
                        } catch (RuntimeException e10) {
                            log(headerField + "is not Octet header field!");
                            return null;
                        }
                    case PduPart.P_DIFFERENCES /*135*/:
                    case PduPart.P_PADDING /*136*/:
                    case PduPart.P_PATH /*157*/:
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
                                log(headerField + "is not Long-Integer header field!");
                                return null;
                            }
                        } catch (RuntimeException e12) {
                            log(headerField + "is not Long-Integer header field!");
                            return null;
                        }
                    case PduPart.P_CT_MR_TYPE /*137*/:
                        EncodedStringValue from;
                        try {
                            parseValueLength(pduDataStream);
                        } catch (RuntimeException e13) {
                            e13.printStackTrace();
                        }
                        if (128 == extractByteValue(pduDataStream)) {
                            from = parseEncodedStringValue(pduDataStream);
                            if (from != null) {
                                address = from.getTextString();
                                if (address != null) {
                                    str = new String(address);
                                    endIndex = str.indexOf("/");
                                    if (endIndex > 0) {
                                        str2 = str.substring(TYPE_TEXT_STRING, endIndex);
                                    }
                                    try {
                                        from.setTextString(str2.getBytes());
                                    } catch (NullPointerException e14) {
                                        log("null pointer error!");
                                        return null;
                                    }
                                }
                            }
                        }
                        try {
                            from = new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes());
                        } catch (NullPointerException e15) {
                            log(headerField + "is not Encoded-String-Value header field!");
                            return null;
                        }
                        try {
                            headers.setEncodedStringValue(from, PduPart.P_CT_MR_TYPE);
                            break;
                        } catch (NullPointerException e16) {
                            log("null pointer error!");
                            break;
                        } catch (RuntimeException e17) {
                            log(headerField + "is not Encoded-String-Value header field!");
                            return null;
                        }
                    case PduPart.P_DEP_START /*138*/:
                        pduDataStream.mark(TYPE_QUOTED_STRING);
                        int messageClass = extractByteValue(pduDataStream);
                        if (messageClass >= 128) {
                            if (128 != messageClass) {
                                if (129 != messageClass) {
                                    if (130 != messageClass) {
                                        if (131 != messageClass) {
                                            break;
                                        }
                                        headers.setTextString(PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes(), PduPart.P_DEP_START);
                                        break;
                                    }
                                    headers.setTextString(PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.getBytes(), PduPart.P_DEP_START);
                                    break;
                                }
                                headers.setTextString(PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.getBytes(), PduPart.P_DEP_START);
                                break;
                            }
                            try {
                                headers.setTextString(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes(), PduPart.P_DEP_START);
                                break;
                            } catch (NullPointerException e18) {
                                log("null pointer error!");
                                break;
                            } catch (RuntimeException e19) {
                                log(headerField + "is not Text-String header field!");
                                return null;
                            }
                        }
                        pduDataStream.reset();
                        byte[] messageClassString = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                        if (messageClassString != null) {
                            try {
                                headers.setTextString(messageClassString, PduPart.P_DEP_START);
                                break;
                            } catch (NullPointerException e20) {
                                log("null pointer error!");
                                break;
                            } catch (RuntimeException e21) {
                                log(headerField + "is not Text-String header field!");
                                return null;
                            }
                        }
                        continue;
                    case PduPart.P_DEP_COMMENT /*140*/:
                        int messageType = extractByteValue(pduDataStream);
                        switch (messageType) {
                            case PduPart.P_CT_MR_TYPE /*137*/:
                            case PduPart.P_DEP_START /*138*/:
                            case PduPart.P_DEP_START_INFO /*139*/:
                            case PduPart.P_DEP_COMMENT /*140*/:
                            case PduPart.P_DEP_DOMAIN /*141*/:
                            case PduPart.P_MAX_AGE /*142*/:
                            case PduPart.P_DEP_PATH /*143*/:
                            case PduPart.P_SECURE /*144*/:
                            case PduPart.P_SEC /*145*/:
                            case PduPart.P_MAC /*146*/:
                            case PduPart.P_CREATION_DATE /*147*/:
                            case PduPart.P_MODIFICATION_DATE /*148*/:
                            case PduPart.P_READ_DATE /*149*/:
                            case PduPart.P_SIZE /*150*/:
                            case PduPart.P_NAME /*151*/:
                                return null;
                            default:
                                try {
                                    headers.setOctet(messageType, headerField);
                                    break;
                                } catch (InvalidHeaderValueException e22) {
                                    log("Set invalid Octet value: " + messageType + " into the header filed: " + headerField);
                                    return null;
                                } catch (RuntimeException e23) {
                                    log(headerField + "is not Octet header field!");
                                    return null;
                                }
                        }
                    case PduPart.P_DEP_DOMAIN /*141*/:
                        int version = parseShortInteger(pduDataStream);
                        try {
                            headers.setOctet(version, PduPart.P_DEP_DOMAIN);
                            break;
                        } catch (InvalidHeaderValueException e24) {
                            log("Set invalid Octet value: " + version + " into the header filed: " + headerField);
                            return null;
                        } catch (RuntimeException e25) {
                            log(headerField + "is not Octet header field!");
                            return null;
                        }
                    case PduPart.P_CREATION_DATE /*147*/:
                    case PduPart.P_SIZE /*150*/:
                    case PduPart.P_START_INFO /*154*/:
                    case PduHeaders.STORE_STATUS_TEXT /*166*/:
                    case PduHeaders.RECOMMENDED_RETRIEVAL_MODE_TEXT /*181*/:
                    case PduHeaders.STATUS_TEXT /*182*/:
                        if (150 == headerField) {
                            pduDataStream.mark(TYPE_QUOTED_STRING);
                            if ((pduDataStream.read() & PduHeaders.STORE_STATUS_ERROR_END) != 0) {
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
                            } catch (NullPointerException e26) {
                                log("null pointer error!");
                                break;
                            } catch (RuntimeException e27) {
                                log(headerField + "is not Encoded-String-Value header field!");
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
                                } catch (NullPointerException e28) {
                                    log("null pointer error!");
                                    break;
                                } catch (RuntimeException e29) {
                                    log(headerField + "is not Encoded-String-Value header field!");
                                    return null;
                                }
                            }
                            continue;
                        } catch (RuntimeException e30) {
                            log(headerField + " is not Integer-Value");
                            return null;
                        }
                    case PduHeaders.PREVIOUSLY_SENT_DATE /*161*/:
                        parseValueLength(pduDataStream);
                        try {
                            parseIntegerValue(pduDataStream);
                            try {
                                headers.setLongInteger(parseLongInteger(pduDataStream), PduHeaders.PREVIOUSLY_SENT_DATE);
                                break;
                            } catch (RuntimeException e31) {
                                log(headerField + "is not Long-Integer header field!");
                                return null;
                            }
                        } catch (RuntimeException e32) {
                            log(headerField + " is not Integer-Value");
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
                        } catch (RuntimeException e33) {
                            log(headerField + " is not Integer-Value");
                            return null;
                        }
                    case PduHeaders.MESSAGE_COUNT /*173*/:
                    case PduHeaders.START /*175*/:
                    case PduHeaders.LIMIT /*179*/:
                        try {
                            headers.setLongInteger(parseIntegerValue(pduDataStream), headerField);
                            break;
                        } catch (RuntimeException e34) {
                            log(headerField + "is not Long-Integer header field!");
                            return null;
                        }
                    case PduHeaders.ELEMENT_DESCRIPTOR /*178*/:
                        parseContentType(pduDataStream, null);
                        break;
                    default:
                        log("Unknown header");
                        break;
                }
            }
            pduDataStream.reset();
            byte[] bVal = parseWapString(pduDataStream, TYPE_TEXT_STRING);
        }
        return headers;
    }

    protected static PduBody parseReadReport(ByteArrayInputStream pduDataStream) {
        PduBody body = new PduBody();
        PduPart part = new PduPart();
        part.setContentType(PduContentTypes.contentTypes[3].getBytes(Charset.defaultCharset()));
        part.setContentLocation(Long.toOctalString(System.currentTimeMillis()).getBytes(Charset.defaultCharset()));
        int dataLength = pduDataStream.available();
        if (dataLength > 0) {
            byte[] partData = new byte[dataLength];
            pduDataStream.read(partData, TYPE_TEXT_STRING, dataLength);
            part.setData(partData);
        }
        body.addPart(part);
        return body;
    }

    protected PduBody parseParts(ByteArrayInputStream pduDataStream) {
        if (pduDataStream == null) {
            return null;
        }
        int count = parseUnsignedInt(pduDataStream);
        PduBody body = new PduBody();
        for (int i = TYPE_TEXT_STRING; i < count; i += TYPE_QUOTED_STRING) {
            int headerLength = parseUnsignedInt(pduDataStream);
            int dataLength = parseUnsignedInt(pduDataStream);
            PduPart part = new PduPart();
            int startPos = pduDataStream.available();
            if (startPos <= 0) {
                return null;
            }
            HashMap<Integer, Object> map = new HashMap();
            byte[] contentType = parseContentType(pduDataStream, map);
            if (contentType != null) {
                part.setContentType(contentType);
            } else {
                part.setContentType(PduContentTypes.contentTypes[TYPE_TEXT_STRING].getBytes());
            }
            byte[] name = (byte[]) map.get(Integer.valueOf(PduPart.P_NAME));
            if (name != null) {
                part.setName(name);
            }
            Integer charset = (Integer) map.get(Integer.valueOf(PduPart.P_DISPOSITION_ATTACHMENT));
            if (charset != null) {
                part.setCharset(charset.intValue());
            }
            int partHeaderLen = headerLength - (startPos - pduDataStream.available());
            if (partHeaderLen > 0) {
                if (!parsePartHeaders(pduDataStream, part, partHeaderLen)) {
                    return null;
                }
            } else if (partHeaderLen < 0) {
                return null;
            }
            if (part.getContentLocation() == null && part.getName() == null && part.getFilename() == null && part.getContentId() == null) {
                part.setContentLocation(Long.toOctalString(System.currentTimeMillis()).getBytes());
            }
            if (dataLength > 0) {
                byte[] partData = new byte[dataLength];
                String str = new String(part.getContentType());
                pduDataStream.read(partData, TYPE_TEXT_STRING, dataLength);
                if (str.equalsIgnoreCase(ContentType.MULTIPART_ALTERNATIVE)) {
                    part = parseParts(new ByteArrayInputStream(partData)).getPart(TYPE_TEXT_STRING);
                } else {
                    byte[] partDataEncoding = part.getContentTransferEncoding();
                    if (partDataEncoding != null) {
                        String encoding = new String(partDataEncoding);
                        if (encoding.equalsIgnoreCase(PduPart.P_BASE64)) {
                            partData = Base64.decodeBase64(partData);
                        } else {
                            if (encoding.equalsIgnoreCase(PduPart.P_QUOTED_PRINTABLE)) {
                                partData = QuotedPrintable.decodeQuotedPrintable(partData);
                            }
                        }
                    }
                    if (partData == null) {
                        log("Decode part data error!");
                        return null;
                    }
                    part.setData(partData);
                }
            }
            if (checkPartPosition(part) == 0) {
                body.addPart(TYPE_TEXT_STRING, part);
            } else {
                body.addPart(part);
            }
        }
        return body;
    }

    private static void log(String text) {
    }

    protected static int parseUnsignedInt(ByteArrayInputStream pduDataStream) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (pduDataStream != null) {
                obj = TYPE_QUOTED_STRING;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int result = TYPE_TEXT_STRING;
        int temp = pduDataStream.read();
        if (temp == -1) {
            return temp;
        }
        while ((temp & PduPart.P_Q) != 0) {
            result = (result << 7) | (temp & TEXT_MAX);
            temp = pduDataStream.read();
            if (temp == -1) {
                return temp;
            }
        }
        return (result << 7) | (temp & TEXT_MAX);
    }

    protected static int parseValueLength(ByteArrayInputStream pduDataStream) {
        Object obj = TYPE_QUOTED_STRING;
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                throw new AssertionError();
            }
        }
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if (-1 == temp) {
                obj = TYPE_TEXT_STRING;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int first = temp & PduHeaders.STORE_STATUS_ERROR_END;
        if (first <= SHORT_LENGTH_MAX) {
            return first;
        }
        if (first == LENGTH_QUOTE) {
            return parseUnsignedInt(pduDataStream);
        }
        throw new RuntimeException("Value length > LENGTH_QUOTE!");
    }

    protected static EncodedStringValue parseEncodedStringValue(ByteArrayInputStream pduDataStream) {
        int i = TYPE_QUOTED_STRING;
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == 0) {
                throw new AssertionError();
            }
        }
        pduDataStream.mark(TYPE_QUOTED_STRING);
        int charset = TYPE_TEXT_STRING;
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if (-1 == temp) {
                i = TYPE_TEXT_STRING;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        int first = temp & PduHeaders.STORE_STATUS_ERROR_END;
        if (first == 0) {
            return new EncodedStringValue("");
        }
        EncodedStringValue returnValue;
        pduDataStream.reset();
        if (first < TEXT_MIN) {
            parseValueLength(pduDataStream);
            charset = parseShortInteger(pduDataStream);
        }
        byte[] textString = parseWapString(pduDataStream, TYPE_TEXT_STRING);
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

    protected static byte[] parseWapString(ByteArrayInputStream pduDataStream, int stringType) {
        int i = TYPE_TEXT_STRING;
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == 0) {
                throw new AssertionError();
            }
        }
        pduDataStream.mark(TYPE_QUOTED_STRING);
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if (-1 != temp) {
                i = TYPE_QUOTED_STRING;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        if (TYPE_QUOTED_STRING == stringType && QUOTED_STRING_FLAG == temp) {
            pduDataStream.mark(TYPE_QUOTED_STRING);
        } else if (stringType == 0 && TEXT_MAX == temp) {
            pduDataStream.mark(TYPE_QUOTED_STRING);
        } else {
            pduDataStream.reset();
        }
        return getWapString(pduDataStream, stringType);
    }

    protected static boolean isTokenCharacter(int ch) {
        if (ch < 33 || ch > 126) {
            return LOCAL_LOGV;
        }
        switch (ch) {
            case QUOTED_STRING_FLAG /*34*/:
            case RadioNVItems.RIL_NV_MIP_PROFILE_MN_HA_SS /*40*/:
            case CallFailCause.TEMPORARY_FAILURE /*41*/:
            case CallFailCause.CHANNEL_NOT_AVAIL /*44*/:
            case WspTypeDecoder.PARAMETER_ID_X_WAP_APPLICATION_ID /*47*/:
            case CallFailCause.BEARER_NOT_AVAIL /*58*/:
            case RadioNVItems.RIL_NV_CDMA_EHRPD_FORCED /*59*/:
            case 60:
            case 61:
            case 62:
            case SignalToneUtil.IS95_CONST_IR_SIG_TONE_NO_TONE /*63*/:
            case CommandsInterface.SERVICE_CLASS_PACKET /*64*/:
            case com.android.internal.telephony.CallFailCause.INVALID_TRANSIT_NW_SELECTION /*91*/:
            case 92:
            case 93:
            case 123:
            case 125:
                return LOCAL_LOGV;
            default:
                return true;
        }
    }

    protected static boolean isText(int ch) {
        if ((ch >= TEXT_MIN && ch <= 126) || (ch >= PduPart.P_Q && ch <= PduHeaders.STORE_STATUS_ERROR_END)) {
            return true;
        }
        switch (ch) {
            case CharacterSets.ISO_8859_6 /*9*/:
            case CharacterSets.ISO_8859_7 /*10*/:
            case UserData.ASCII_CR_INDEX /*13*/:
                return true;
            default:
                return LOCAL_LOGV;
        }
    }

    protected static byte[] getWapString(ByteArrayInputStream pduDataStream, int stringType) {
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                throw new AssertionError();
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if ((-1 != temp ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                throw new AssertionError();
            }
        }
        while (-1 != temp && temp != 0) {
            if (stringType == TYPE_TOKEN_STRING) {
                if (isTokenCharacter(temp)) {
                    out.write(temp);
                }
            } else if (isText(temp)) {
                out.write(temp);
            }
            temp = pduDataStream.read();
            if (!-assertionsDisabled) {
                Object obj;
                if (-1 != temp) {
                    obj = TYPE_QUOTED_STRING;
                } else {
                    obj = TYPE_TEXT_STRING;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
        }
        if (out.size() > 0) {
            return out.toByteArray();
        }
        return null;
    }

    protected static int extractByteValue(ByteArrayInputStream pduDataStream) {
        Object obj = TYPE_QUOTED_STRING;
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                throw new AssertionError();
            }
        }
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if (-1 == temp) {
                obj = TYPE_TEXT_STRING;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return temp & PduHeaders.STORE_STATUS_ERROR_END;
    }

    protected static int parseShortInteger(ByteArrayInputStream pduDataStream) {
        Object obj = TYPE_QUOTED_STRING;
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                throw new AssertionError();
            }
        }
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if (-1 == temp) {
                obj = TYPE_TEXT_STRING;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return temp & TEXT_MAX;
    }

    protected static long parseLongInteger(ByteArrayInputStream pduDataStream) {
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                throw new AssertionError();
            }
        }
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if ((-1 != temp ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                throw new AssertionError();
            }
        }
        int count = temp & PduHeaders.STORE_STATUS_ERROR_END;
        if (count > LONG_INTEGER_LENGTH_MAX) {
            throw new RuntimeException("Octet count greater than 8 and I can't represent that!");
        }
        long result = 0;
        for (int i = TYPE_TEXT_STRING; i < count; i += TYPE_QUOTED_STRING) {
            temp = pduDataStream.read();
            if (!-assertionsDisabled) {
                if ((-1 != temp ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == null) {
                    throw new AssertionError();
                }
            }
            result = (result << 8) + ((long) (temp & PduHeaders.STORE_STATUS_ERROR_END));
        }
        return result;
    }

    protected static long parseIntegerValue(ByteArrayInputStream pduDataStream) {
        int i = TYPE_QUOTED_STRING;
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == 0) {
                throw new AssertionError();
            }
        }
        pduDataStream.mark(TYPE_QUOTED_STRING);
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if (-1 == temp) {
                i = TYPE_TEXT_STRING;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        pduDataStream.reset();
        if (temp > TEXT_MAX) {
            return (long) parseShortInteger(pduDataStream);
        }
        return parseLongInteger(pduDataStream);
    }

    protected static int skipWapValue(ByteArrayInputStream pduDataStream, int length) {
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == 0) {
                throw new AssertionError();
            }
        }
        int readLen = pduDataStream.read(new byte[length], TYPE_TEXT_STRING, length);
        if (readLen < length) {
            return -1;
        }
        return readLen;
    }

    protected static void parseContentTypeParams(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map, Integer length) {
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((length.intValue() > 0 ? TYPE_QUOTED_STRING : null) == null) {
                throw new AssertionError();
            }
        }
        int startPos = pduDataStream.available();
        int lastLen = length.intValue();
        while (lastLen > 0) {
            int param = pduDataStream.read();
            if (!-assertionsDisabled) {
                if ((-1 != param ? TYPE_QUOTED_STRING : null) == null) {
                    throw new AssertionError();
                }
            }
            lastLen--;
            switch (param) {
                case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                    pduDataStream.mark(TYPE_QUOTED_STRING);
                    int firstValue = extractByteValue(pduDataStream);
                    pduDataStream.reset();
                    if ((firstValue <= TEXT_MIN || firstValue >= TEXT_MAX) && firstValue != 0) {
                        int charset = (int) parseIntegerValue(pduDataStream);
                        if (map != null) {
                            map.put(Integer.valueOf(PduPart.P_DISPOSITION_ATTACHMENT), Integer.valueOf(charset));
                        }
                    } else {
                        byte[] charsetStr = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                        try {
                            map.put(Integer.valueOf(PduPart.P_DISPOSITION_ATTACHMENT), Integer.valueOf(CharacterSets.getMibEnumValue(new String(charsetStr))));
                        } catch (UnsupportedEncodingException e) {
                            Log.e(LOG_TAG, Arrays.toString(charsetStr), e);
                            map.put(Integer.valueOf(PduPart.P_DISPOSITION_ATTACHMENT), Integer.valueOf(TYPE_TEXT_STRING));
                        }
                    }
                    lastLen = length.intValue() - (startPos - pduDataStream.available());
                    break;
                case PduPart.P_TYPE /*131*/:
                case PduPart.P_CT_MR_TYPE /*137*/:
                    pduDataStream.mark(TYPE_QUOTED_STRING);
                    int first = extractByteValue(pduDataStream);
                    pduDataStream.reset();
                    if (first > TEXT_MAX) {
                        int index = parseShortInteger(pduDataStream);
                        int length2 = PduContentTypes.contentTypes.length;
                        if (index < r0) {
                            map.put(Integer.valueOf(PduPart.P_TYPE), PduContentTypes.contentTypes[index].getBytes());
                        }
                    } else {
                        Object type = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                        if (!(type == null || map == null)) {
                            map.put(Integer.valueOf(PduPart.P_TYPE), type);
                        }
                    }
                    lastLen = length.intValue() - (startPos - pduDataStream.available());
                    break;
                case PduPart.P_DEP_NAME /*133*/:
                case PduPart.P_NAME /*151*/:
                    byte[] name = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                    if (!(name == null || map == null)) {
                        map.put(Integer.valueOf(PduPart.P_NAME), name);
                    }
                    lastLen = length.intValue() - (startPos - pduDataStream.available());
                    break;
                case PduPart.P_DEP_START /*138*/:
                case PduPart.P_START /*153*/:
                    byte[] start = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                    if (!(start == null || map == null)) {
                        map.put(Integer.valueOf(PduPart.P_START), start);
                    }
                    lastLen = length.intValue() - (startPos - pduDataStream.available());
                    break;
                default:
                    if (-1 != skipWapValue(pduDataStream, lastLen)) {
                        lastLen = TYPE_TEXT_STRING;
                        break;
                    } else {
                        Log.e(LOG_TAG, "Corrupt Content-Type");
                        break;
                    }
            }
        }
        if (lastLen != 0) {
            Log.e(LOG_TAG, "Corrupt Content-Type");
        }
    }

    protected static byte[] parseContentType(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map) {
        byte[] contentType;
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : null) == null) {
                throw new AssertionError();
            }
        }
        pduDataStream.mark(TYPE_QUOTED_STRING);
        int temp = pduDataStream.read();
        if (!-assertionsDisabled) {
            if ((-1 != temp ? TYPE_QUOTED_STRING : null) == null) {
                throw new AssertionError();
            }
        }
        pduDataStream.reset();
        int cur = temp & PduHeaders.STORE_STATUS_ERROR_END;
        if (cur < TEXT_MIN) {
            int length = parseValueLength(pduDataStream);
            int startPos = pduDataStream.available();
            pduDataStream.mark(TYPE_QUOTED_STRING);
            temp = pduDataStream.read();
            if (!-assertionsDisabled) {
                if ((-1 != temp ? TYPE_QUOTED_STRING : null) == null) {
                    throw new AssertionError();
                }
            }
            pduDataStream.reset();
            int first = temp & PduHeaders.STORE_STATUS_ERROR_END;
            if (first >= TEXT_MIN && first <= TEXT_MAX) {
                contentType = parseWapString(pduDataStream, TYPE_TEXT_STRING);
            } else if (first > TEXT_MAX) {
                int index = parseShortInteger(pduDataStream);
                if (index < PduContentTypes.contentTypes.length) {
                    contentType = PduContentTypes.contentTypes[index].getBytes();
                } else {
                    pduDataStream.reset();
                    contentType = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                }
            } else {
                Log.e(LOG_TAG, "Corrupt content-type");
                return PduContentTypes.contentTypes[TYPE_TEXT_STRING].getBytes();
            }
            int parameterLen = length - (startPos - pduDataStream.available());
            if (parameterLen > 0) {
                parseContentTypeParams(pduDataStream, map, Integer.valueOf(parameterLen));
            }
            if (parameterLen < 0) {
                Log.e(LOG_TAG, "Corrupt MMS message");
                return PduContentTypes.contentTypes[TYPE_TEXT_STRING].getBytes();
            }
        } else if (cur <= TEXT_MAX) {
            contentType = parseWapString(pduDataStream, TYPE_TEXT_STRING);
        } else {
            contentType = PduContentTypes.contentTypes[parseShortInteger(pduDataStream)].getBytes();
        }
        return contentType;
    }

    protected boolean parsePartHeaders(ByteArrayInputStream pduDataStream, PduPart part, int length) {
        if (!-assertionsDisabled) {
            if ((pduDataStream != null ? TYPE_QUOTED_STRING : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((part != null ? TYPE_QUOTED_STRING : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((length > 0 ? TYPE_QUOTED_STRING : null) == null) {
                throw new AssertionError();
            }
        }
        int startPos = pduDataStream.available();
        int lastLen = length;
        while (lastLen > 0) {
            int header = pduDataStream.read();
            if (!-assertionsDisabled) {
                if ((-1 != header ? TYPE_QUOTED_STRING : null) == null) {
                    throw new AssertionError();
                }
            }
            lastLen--;
            if (header > TEXT_MAX) {
                switch (header) {
                    case PduPart.P_MAX_AGE /*142*/:
                        byte[] contentLocation = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                        if (contentLocation != null) {
                            part.setContentLocation(contentLocation);
                        }
                        lastLen = length - (startPos - pduDataStream.available());
                        break;
                    case PduPart.P_DEP_CONTENT_DISPOSITION /*174*/:
                    case PduPart.P_CONTENT_DISPOSITION /*197*/:
                        if (!this.mParseContentDisposition) {
                            break;
                        }
                        int len = parseValueLength(pduDataStream);
                        pduDataStream.mark(TYPE_QUOTED_STRING);
                        int thisStartPos = pduDataStream.available();
                        int value = pduDataStream.read();
                        if (value == PduPart.P_Q) {
                            part.setContentDisposition(PduPart.DISPOSITION_FROM_DATA);
                        } else if (value == PduPart.P_DISPOSITION_ATTACHMENT) {
                            part.setContentDisposition(PduPart.DISPOSITION_ATTACHMENT);
                        } else if (value == PduPart.P_LEVEL) {
                            part.setContentDisposition(PduPart.DISPOSITION_INLINE);
                        } else {
                            pduDataStream.reset();
                            part.setContentDisposition(parseWapString(pduDataStream, TYPE_TEXT_STRING));
                        }
                        if (thisStartPos - pduDataStream.available() < len) {
                            if (pduDataStream.read() == PduPart.P_FILENAME) {
                                part.setFilename(parseWapString(pduDataStream, TYPE_TEXT_STRING));
                            }
                            int thisEndPos = pduDataStream.available();
                            if (thisStartPos - thisEndPos < len) {
                                int last = len - (thisStartPos - thisEndPos);
                                pduDataStream.read(new byte[last], TYPE_TEXT_STRING, last);
                            }
                        }
                        lastLen = length - (startPos - pduDataStream.available());
                        break;
                    case PduPart.P_CONTENT_ID /*192*/:
                        byte[] contentId = parseWapString(pduDataStream, TYPE_QUOTED_STRING);
                        if (contentId != null) {
                            part.setContentId(contentId);
                        }
                        lastLen = length - (startPos - pduDataStream.available());
                        break;
                    default:
                        if (-1 != skipWapValue(pduDataStream, lastLen)) {
                            lastLen = TYPE_TEXT_STRING;
                            break;
                        }
                        Log.e(LOG_TAG, "Corrupt Part headers");
                        return LOCAL_LOGV;
                }
            } else if (header >= TEXT_MIN && header <= TEXT_MAX) {
                byte[] tempHeader = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                byte[] tempValue = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                if (PduPart.CONTENT_TRANSFER_ENCODING.equalsIgnoreCase(new String(tempHeader))) {
                    part.setContentTransferEncoding(tempValue);
                }
                lastLen = length - (startPos - pduDataStream.available());
            } else if (-1 == skipWapValue(pduDataStream, lastLen)) {
                Log.e(LOG_TAG, "Corrupt Part headers");
                return LOCAL_LOGV;
            } else {
                lastLen = TYPE_TEXT_STRING;
            }
        }
        if (lastLen == 0) {
            return true;
        }
        Log.e(LOG_TAG, "Corrupt Part headers");
        return LOCAL_LOGV;
    }

    private static int checkPartPosition(PduPart part) {
        if (!-assertionsDisabled) {
            if ((part != null ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING) == 0) {
                throw new AssertionError();
            }
        }
        if (mTypeParam == null && mStartParam == null) {
            return TYPE_QUOTED_STRING;
        }
        if (mStartParam != null) {
            byte[] contentId = part.getContentId();
            return (contentId == null || !Arrays.equals(mStartParam, contentId)) ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING;
        } else {
            if (mTypeParam != null) {
                byte[] contentType = part.getContentType();
                return (contentType == null || !Arrays.equals(mTypeParam, contentType)) ? TYPE_QUOTED_STRING : TYPE_TEXT_STRING;
            }
        }
    }

    protected static boolean checkMandatoryHeader(PduHeaders headers) {
        if (headers == null) {
            return LOCAL_LOGV;
        }
        int messageType = headers.getOctet(PduPart.P_DEP_COMMENT);
        if (headers.getOctet(PduPart.P_DEP_DOMAIN) == 0) {
            return LOCAL_LOGV;
        }
        switch (messageType) {
            case PduPart.P_Q /*128*/:
                if (headers.getTextString(PduHeaders.STATUS_UNRECOGNIZED) == null) {
                    return LOCAL_LOGV;
                }
                if (headers.getEncodedStringValue(PduPart.P_CT_MR_TYPE) == null) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_FILENAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                if (headers.getOctet(PduPart.P_MAC) == 0) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_FILENAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            case PduPart.P_LEVEL /*130*/:
                if (headers.getTextString(PduPart.P_TYPE) == null) {
                    return LOCAL_LOGV;
                }
                if (-1 == headers.getLongInteger(PduPart.P_PADDING)) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_DEP_START) == null) {
                    return LOCAL_LOGV;
                }
                if (-1 == headers.getLongInteger(PduPart.P_MAX_AGE)) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_FILENAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            case PduPart.P_TYPE /*131*/:
                if (headers.getOctet(PduPart.P_READ_DATE) == 0) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_FILENAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                if (headers.getTextString(PduHeaders.STATUS_UNRECOGNIZED) == null) {
                    return LOCAL_LOGV;
                }
                if (-1 == headers.getLongInteger(PduPart.P_DEP_NAME)) {
                    return LOCAL_LOGV;
                }
                break;
            case PduPart.P_DEP_NAME /*133*/:
                if (headers.getTextString(PduPart.P_FILENAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            case PduPart.P_DEP_FILENAME /*134*/:
                if (-1 == headers.getLongInteger(PduPart.P_DEP_NAME)) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_DEP_START_INFO) == null) {
                    return LOCAL_LOGV;
                }
                if (headers.getOctet(PduPart.P_READ_DATE) == 0) {
                    return LOCAL_LOGV;
                }
                if (headers.getEncodedStringValues(PduPart.P_NAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            case PduPart.P_DIFFERENCES /*135*/:
                if (headers.getEncodedStringValue(PduPart.P_CT_MR_TYPE) == null) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_DEP_START_INFO) == null) {
                    return LOCAL_LOGV;
                }
                if (headers.getOctet(PduPart.P_COMMENT) == 0) {
                    return LOCAL_LOGV;
                }
                if (headers.getEncodedStringValues(PduPart.P_NAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            case PduPart.P_PADDING /*136*/:
                if (-1 == headers.getLongInteger(PduPart.P_DEP_NAME)) {
                    return LOCAL_LOGV;
                }
                if (headers.getEncodedStringValue(PduPart.P_CT_MR_TYPE) == null) {
                    return LOCAL_LOGV;
                }
                if (headers.getTextString(PduPart.P_DEP_START_INFO) == null) {
                    return LOCAL_LOGV;
                }
                if (headers.getOctet(PduPart.P_COMMENT) == 0) {
                    return LOCAL_LOGV;
                }
                if (headers.getEncodedStringValues(PduPart.P_NAME) == null) {
                    return LOCAL_LOGV;
                }
                break;
            default:
                return LOCAL_LOGV;
        }
        return true;
    }
}
