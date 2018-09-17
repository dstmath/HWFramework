package com.huawei.hsm.permission.minimms;

import android.telephony.HwVSimManager;
import com.huawei.connectivitylog.ConnectivityLogManager;
import com.huawei.hsm.permission.StubController;
import huawei.android.app.admin.HwDeviceAdminInfo;
import huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class PduParser {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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
    private PduHeaders mHeaders;
    private ByteArrayInputStream mPduDataStream;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.permission.minimms.PduParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.permission.minimms.PduParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.permission.minimms.PduParser.<clinit>():void");
    }

    public PduParser(byte[] pduDataStream) {
        this.mPduDataStream = null;
        this.mHeaders = null;
        this.mPduDataStream = new ByteArrayInputStream(pduDataStream);
    }

    public int getTargetCount() {
        int sendToCount = TYPE_QUOTED_STRING;
        if (this.mPduDataStream == null) {
            return TYPE_QUOTED_STRING;
        }
        this.mHeaders = parseHeaders(this.mPduDataStream);
        if (this.mHeaders == null) {
            return TYPE_QUOTED_STRING;
        }
        EncodedStringValue[] diTo = this.mHeaders.getEncodedStringValues(PduPart.P_NAME);
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
            pduDataStream.mark(TYPE_QUOTED_STRING);
            int headerField = extractByteValue(pduDataStream);
            if (headerField < TEXT_MIN || headerField > TEXT_MAX) {
                EncodedStringValue value;
                byte[] address;
                String str;
                int endIndex;
                String str2;
                switch (headerField) {
                    case PduPart.P_CHARSET /*129*/:
                    case PduHeaders.PRIORITY_HIGH /*130*/:
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
                    case PduPart.P_TYPE /*131*/:
                    case PduHeaders.MESSAGE_TYPE_MBOX_STORE_REQ /*139*/:
                    case PduHeaders.TRANSACTION_ID /*152*/:
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
                                break;
                            } catch (RuntimeException e5) {
                                return null;
                            }
                        }
                        continue;
                    case PduHeaders.MM_STATE_FORWARDED /*132*/:
                        byte[] contentType = parseContentType(pduDataStream, new HashMap());
                        if (contentType != null) {
                            try {
                                headers.setTextString(contentType, PduHeaders.MM_STATE_FORWARDED);
                            } catch (NullPointerException e6) {
                            } catch (RuntimeException e7) {
                                return null;
                            }
                        }
                        keepParsing = LOCAL_LOGV;
                        break;
                    case PduPart.P_DEP_NAME /*133*/:
                    case PduHeaders.MESSAGE_TYPE_MBOX_VIEW_CONF /*142*/:
                    case PduHeaders.REPLY_CHARGING_SIZE /*159*/:
                        try {
                            headers.setLongInteger(parseLongInteger(pduDataStream), headerField);
                            break;
                        } catch (RuntimeException e8) {
                            return null;
                        }
                    case PduHeaders.DELIVERY_REPORT /*134*/:
                    case PduHeaders.PRIORITY /*143*/:
                    case PduHeaders.READ_REPORT /*144*/:
                    case PduHeaders.REPORT_ALLOWED /*145*/:
                    case PduHeaders.RESPONSE_STATUS /*146*/:
                    case PduHeaders.SENDER_VISIBILITY /*148*/:
                    case PduHeaders.STATUS /*149*/:
                    case PduPart.P_START /*153*/:
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
                    case PduHeaders.STATUS_UNREACHABLE /*135*/:
                    case PduHeaders.RESPONSE_STATUS_ERROR_UNSUPPORTED_MESSAGE /*136*/:
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
                    case PduPart.P_CT_MR_TYPE /*137*/:
                        EncodedStringValue from;
                        parseValueLength(pduDataStream);
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
                                    } catch (NullPointerException e13) {
                                        return null;
                                    }
                                }
                            }
                        }
                        try {
                            from = new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes());
                        } catch (NullPointerException e14) {
                            return null;
                        }
                        try {
                            headers.setEncodedStringValue(from, PduPart.P_CT_MR_TYPE);
                            break;
                        } catch (NullPointerException e15) {
                            break;
                        } catch (RuntimeException e16) {
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
                            } catch (NullPointerException e17) {
                                break;
                            } catch (RuntimeException e18) {
                                return null;
                            }
                        }
                        pduDataStream.reset();
                        byte[] messageClassString = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                        if (messageClassString != null) {
                            try {
                                headers.setTextString(messageClassString, PduPart.P_DEP_START);
                                break;
                            } catch (NullPointerException e19) {
                                break;
                            } catch (RuntimeException e20) {
                                return null;
                            }
                        }
                        continue;
                    case PduHeaders.MESSAGE_TYPE_MBOX_STORE_CONF /*140*/:
                        int messageType = extractByteValue(pduDataStream);
                        switch (messageType) {
                            case PduPart.P_CT_MR_TYPE /*137*/:
                            case PduPart.P_DEP_START /*138*/:
                            case PduHeaders.MESSAGE_TYPE_MBOX_STORE_REQ /*139*/:
                            case PduHeaders.MESSAGE_TYPE_MBOX_STORE_CONF /*140*/:
                            case PduHeaders.MMS_VERSION /*141*/:
                            case PduHeaders.MESSAGE_TYPE_MBOX_VIEW_CONF /*142*/:
                            case PduHeaders.PRIORITY /*143*/:
                            case PduHeaders.READ_REPORT /*144*/:
                            case PduHeaders.REPORT_ALLOWED /*145*/:
                            case PduHeaders.RESPONSE_STATUS /*146*/:
                            case PduHeaders.RESPONSE_TEXT /*147*/:
                            case PduHeaders.SENDER_VISIBILITY /*148*/:
                            case PduHeaders.STATUS /*149*/:
                            case PduHeaders.SUBJECT /*150*/:
                            case PduPart.P_NAME /*151*/:
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
                    case PduHeaders.MMS_VERSION /*141*/:
                        try {
                            headers.setOctet(parseShortInteger(pduDataStream), PduHeaders.MMS_VERSION);
                            break;
                        } catch (InvalidHeaderValueException e23) {
                            return null;
                        } catch (RuntimeException e24) {
                            return null;
                        }
                    case PduHeaders.RESPONSE_TEXT /*147*/:
                    case PduHeaders.SUBJECT /*150*/:
                    case PduHeaders.RETRIEVE_TEXT /*154*/:
                    case PduHeaders.STORE_STATUS_TEXT /*166*/:
                    case PduHeaders.RECOMMENDED_RETRIEVAL_MODE_TEXT /*181*/:
                    case PduHeaders.STATUS_TEXT /*182*/:
                        if (150 == headerField && pduDataStream != null) {
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
            parseWapString(pduDataStream, TYPE_TEXT_STRING);
        }
        return headers;
    }

    private static int parseUnsignedInt(ByteArrayInputStream pduDataStream) {
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
        while ((temp & PduHeaders.VALUE_YES) != 0) {
            result = (result << 7) | (temp & TEXT_MAX);
            temp = pduDataStream.read();
            if (temp == -1) {
                return temp;
            }
        }
        return (result << 7) | (temp & TEXT_MAX);
    }

    private static int parseValueLength(ByteArrayInputStream pduDataStream) {
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

    private static EncodedStringValue parseEncodedStringValue(ByteArrayInputStream pduDataStream) {
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

    private static byte[] parseWapString(ByteArrayInputStream pduDataStream, int stringType) {
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

    private static boolean isTokenCharacter(int ch) {
        if (ch < 33 || ch > ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT) {
            return LOCAL_LOGV;
        }
        switch (ch) {
            case QUOTED_STRING_FLAG /*34*/:
            case HuaweiTelephonyManagerWrapper.DUAL_MODE_CG_CARD /*40*/:
            case HuaweiTelephonyManagerWrapper.CT_NATIONAL_ROAMING_CARD /*41*/:
            case 44:
            case 47:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case StubController.PERMISSION_ACTION_CALL /*64*/:
            case ConnectivityLogManager.WIFI_CLOSE_FAILED_EX /*91*/:
            case ConnectivityLogManager.WIFI_CONNECT_AUTH_FAILED_EX /*92*/:
            case ConnectivityLogManager.WIFI_CONNECT_ASSOC_FAILED_EX /*93*/:
            case 123:
            case ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
                return LOCAL_LOGV;
            default:
                return true;
        }
    }

    private static boolean isText(int ch) {
        if ((ch >= TEXT_MIN && ch <= ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT) || (ch >= PduHeaders.VALUE_YES && ch <= PduHeaders.STORE_STATUS_ERROR_END)) {
            return true;
        }
        switch (ch) {
            case HwDeviceAdminInfo.USES_POLICY_SET_MDM_EMAIL /*9*/:
            case HuaweiTelephonyManagerWrapper.SINGLE_MODE_SIM_CARD /*10*/:
            case HwVSimManager.NETWORK_TYPE_LTE /*13*/:
                return true;
            default:
                return LOCAL_LOGV;
        }
    }

    private static byte[] getWapString(ByteArrayInputStream pduDataStream, int stringType) {
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

    private static int extractByteValue(ByteArrayInputStream pduDataStream) {
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

    private static int parseShortInteger(ByteArrayInputStream pduDataStream) {
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

    private static long parseLongInteger(ByteArrayInputStream pduDataStream) {
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

    private static long parseIntegerValue(ByteArrayInputStream pduDataStream) {
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

    private static int skipWapValue(ByteArrayInputStream pduDataStream, int length) {
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

    private static void parseContentTypeParams(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map, Integer length) {
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
                case PduPart.P_CHARSET /*129*/:
                    pduDataStream.mark(TYPE_QUOTED_STRING);
                    int firstValue = extractByteValue(pduDataStream);
                    pduDataStream.reset();
                    if ((firstValue <= TEXT_MIN || firstValue >= TEXT_MAX) && firstValue != 0) {
                        int charset = (int) parseIntegerValue(pduDataStream);
                        if (map != null) {
                            map.put(Integer.valueOf(PduPart.P_CHARSET), Integer.valueOf(charset));
                        }
                    } else {
                        try {
                            int charsetInt = CharacterSets.getMibEnumValue(new String(parseWapString(pduDataStream, TYPE_TEXT_STRING)));
                            if (map != null) {
                                map.put(Integer.valueOf(PduPart.P_CHARSET), Integer.valueOf(charsetInt));
                            }
                        } catch (UnsupportedEncodingException e) {
                            if (map != null) {
                                map.put(Integer.valueOf(PduPart.P_CHARSET), Integer.valueOf(TYPE_TEXT_STRING));
                            }
                        }
                    }
                    lastLen = length.intValue() - (startPos - pduDataStream.available());
                    break;
                case PduPart.P_TYPE /*131*/:
                case PduPart.P_CT_MR_TYPE /*137*/:
                    pduDataStream.mark(TYPE_QUOTED_STRING);
                    int first = extractByteValue(pduDataStream);
                    pduDataStream.reset();
                    Object type;
                    if (first > TEXT_MAX) {
                        int index = parseShortInteger(pduDataStream);
                        int length2 = PduContentTypes.contentTypes.length;
                        if (index < r0) {
                            type = PduContentTypes.contentTypes[index].getBytes();
                            if (map != null) {
                                map.put(Integer.valueOf(PduPart.P_TYPE), type);
                            }
                        }
                    } else {
                        type = parseWapString(pduDataStream, TYPE_TEXT_STRING);
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
                    if (-1 == skipWapValue(pduDataStream, lastLen)) {
                        break;
                    }
                    lastLen = TYPE_TEXT_STRING;
                    break;
            }
        }
    }

    private static byte[] parseContentType(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map) {
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
            } else if (first <= TEXT_MAX) {
                return PduContentTypes.contentTypes[TYPE_TEXT_STRING].getBytes();
            } else {
                int index = parseShortInteger(pduDataStream);
                if (index < PduContentTypes.contentTypes.length) {
                    contentType = PduContentTypes.contentTypes[index].getBytes();
                } else {
                    pduDataStream.reset();
                    contentType = parseWapString(pduDataStream, TYPE_TEXT_STRING);
                }
            }
            int parameterLen = length - (startPos - pduDataStream.available());
            if (parameterLen > 0) {
                parseContentTypeParams(pduDataStream, map, Integer.valueOf(parameterLen));
            }
            if (parameterLen < 0) {
                return PduContentTypes.contentTypes[TYPE_TEXT_STRING].getBytes();
            }
        } else if (cur <= TEXT_MAX) {
            contentType = parseWapString(pduDataStream, TYPE_TEXT_STRING);
        } else {
            contentType = PduContentTypes.contentTypes[parseShortInteger(pduDataStream)].getBytes();
        }
        return contentType;
    }
}
