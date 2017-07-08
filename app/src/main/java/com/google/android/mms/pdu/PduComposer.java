package com.google.android.mms.pdu;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

public class PduComposer {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int END_STRING_FLAG = 0;
    private static final int LENGTH_QUOTE = 31;
    private static final String LOG_TAG = "PduComposer";
    private static final int LONG_INTEGER_LENGTH_MAX = 8;
    private static final int PDU_COMPOSER_BLOCK_SIZE = 1024;
    private static final int PDU_COMPOSE_CONTENT_ERROR = 1;
    private static final int PDU_COMPOSE_FIELD_NOT_SET = 2;
    private static final int PDU_COMPOSE_FIELD_NOT_SUPPORTED = 3;
    private static final int PDU_COMPOSE_SUCCESS = 0;
    private static final int PDU_EMAIL_ADDRESS_TYPE = 2;
    private static final int PDU_IPV4_ADDRESS_TYPE = 3;
    private static final int PDU_IPV6_ADDRESS_TYPE = 4;
    private static final int PDU_PHONE_NUMBER_ADDRESS_TYPE = 1;
    private static final int PDU_UNKNOWN_ADDRESS_TYPE = 5;
    private static final int QUOTED_STRING_FLAG = 34;
    static final String REGEXP_EMAIL_ADDRESS_TYPE = "[a-zA-Z| ]*\\<{0,1}[a-zA-Z| ]+@{1}[a-zA-Z| ]+\\.{1}[a-zA-Z| ]+\\>{0,1}";
    static final String REGEXP_IPV4_ADDRESS_TYPE = "[0-9]{1,3}\\.{1}[0-9]{1,3}\\.{1}[0-9]{1,3}\\.{1}[0-9]{1,3}";
    static final String REGEXP_IPV6_ADDRESS_TYPE = "[a-fA-F]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}";
    static final String REGEXP_PHONE_NUMBER_ADDRESS_TYPE = "\\+?[0-9|\\.|\\-]+";
    private static final int SHORT_INTEGER_MAX = 127;
    static final String STRING_IPV4_ADDRESS_TYPE = "/TYPE=IPV4";
    static final String STRING_IPV6_ADDRESS_TYPE = "/TYPE=IPV6";
    static final String STRING_PHONE_NUMBER_ADDRESS_TYPE = "/TYPE=PLMN";
    private static final int TEXT_MAX = 127;
    private static HashMap<String, Integer> mContentTypeMap;
    protected ByteArrayOutputStream mMessage;
    private GenericPdu mPdu;
    private PduHeaders mPduHeader;
    protected int mPosition;
    private final ContentResolver mResolver;
    private BufferStack mStack;

    private class BufferStack {
        private LengthRecordNode stack;
        int stackSize;
        private LengthRecordNode toCopy;

        private BufferStack() {
            this.stack = null;
            this.toCopy = null;
            this.stackSize = PduComposer.PDU_COMPOSE_SUCCESS;
        }

        void newbuf() {
            if (this.toCopy != null) {
                throw new RuntimeException("BUG: Invalid newbuf() before copy()");
            }
            LengthRecordNode temp = new LengthRecordNode();
            temp.currentMessage = PduComposer.this.mMessage;
            temp.currentPosition = PduComposer.this.mPosition;
            temp.next = this.stack;
            this.stack = temp;
            this.stackSize += PduComposer.PDU_PHONE_NUMBER_ADDRESS_TYPE;
            PduComposer.this.mMessage = new ByteArrayOutputStream();
            PduComposer.this.mPosition = PduComposer.PDU_COMPOSE_SUCCESS;
        }

        void pop() {
            ByteArrayOutputStream currentMessage = PduComposer.this.mMessage;
            int currentPosition = PduComposer.this.mPosition;
            PduComposer.this.mMessage = this.stack.currentMessage;
            PduComposer.this.mPosition = this.stack.currentPosition;
            this.toCopy = this.stack;
            this.stack = this.stack.next;
            this.stackSize--;
            this.toCopy.currentMessage = currentMessage;
            this.toCopy.currentPosition = currentPosition;
        }

        void copy() {
            PduComposer.this.arraycopy(this.toCopy.currentMessage.toByteArray(), PduComposer.PDU_COMPOSE_SUCCESS, this.toCopy.currentPosition);
            this.toCopy = null;
        }

        PositionMarker mark() {
            PositionMarker m = new PositionMarker(null);
            m.c_pos = PduComposer.this.mPosition;
            m.currentStackSize = this.stackSize;
            return m;
        }
    }

    private static class LengthRecordNode {
        ByteArrayOutputStream currentMessage;
        public int currentPosition;
        public LengthRecordNode next;

        private LengthRecordNode() {
            this.currentMessage = null;
            this.currentPosition = PduComposer.PDU_COMPOSE_SUCCESS;
            this.next = null;
        }
    }

    private class PositionMarker {
        private int c_pos;
        private int currentStackSize;

        private PositionMarker() {
        }

        int getLength() {
            if (this.currentStackSize == PduComposer.this.mStack.stackSize) {
                return PduComposer.this.mPosition - this.c_pos;
            }
            throw new RuntimeException("BUG: Invalid call to getLength()");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.mms.pdu.PduComposer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.mms.pdu.PduComposer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.PduComposer.<clinit>():void");
    }

    public PduComposer(Context context, GenericPdu pdu) {
        this.mMessage = null;
        this.mPdu = null;
        this.mPosition = PDU_COMPOSE_SUCCESS;
        this.mStack = null;
        this.mPduHeader = null;
        this.mPdu = pdu;
        this.mResolver = context.getContentResolver();
        this.mPduHeader = pdu.getPduHeaders();
        this.mStack = new BufferStack();
        this.mMessage = new ByteArrayOutputStream();
        this.mPosition = PDU_COMPOSE_SUCCESS;
    }

    public byte[] make() {
        int type = this.mPdu.getMessageType();
        switch (type) {
            case PduPart.P_Q /*128*/:
            case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                if (makeSendRetrievePdu(type) != 0) {
                    return null;
                }
                break;
            case PduPart.P_LEVEL /*130*/:
                if (makeNotifyInd() != 0) {
                    return null;
                }
                break;
            case PduPart.P_TYPE /*131*/:
                if (makeNotifyResp() != 0) {
                    return null;
                }
                break;
            case PduPart.P_DEP_NAME /*133*/:
                if (makeAckInd() != 0) {
                    return null;
                }
                break;
            case PduPart.P_DIFFERENCES /*135*/:
                if (makeReadRecInd() != 0) {
                    return null;
                }
                break;
            default:
                return null;
        }
        return this.mMessage.toByteArray();
    }

    protected void arraycopy(byte[] buf, int pos, int length) {
        this.mMessage.write(buf, pos, length);
        this.mPosition += length;
    }

    protected void append(int value) {
        this.mMessage.write(value);
        this.mPosition += PDU_PHONE_NUMBER_ADDRESS_TYPE;
    }

    protected void appendShortInteger(int value) {
        append((value | PduPart.P_Q) & PduHeaders.STORE_STATUS_ERROR_END);
    }

    protected void appendOctet(int number) {
        append(number);
    }

    protected void appendShortLength(int value) {
        append(value);
    }

    protected void appendLongInteger(long longInt) {
        long temp = longInt;
        int size = PDU_COMPOSE_SUCCESS;
        while (temp != 0 && size < LONG_INTEGER_LENGTH_MAX) {
            temp >>>= 8;
            size += PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendShortLength(size);
        int shift = (size - 1) * LONG_INTEGER_LENGTH_MAX;
        for (int i = PDU_COMPOSE_SUCCESS; i < size; i += PDU_PHONE_NUMBER_ADDRESS_TYPE) {
            append((int) ((longInt >>> shift) & 255));
            shift -= 8;
        }
    }

    protected void appendTextString(byte[] text) {
        if ((text[PDU_COMPOSE_SUCCESS] & PduHeaders.STORE_STATUS_ERROR_END) > TEXT_MAX) {
            append(TEXT_MAX);
        }
        arraycopy(text, PDU_COMPOSE_SUCCESS, text.length);
        append(PDU_COMPOSE_SUCCESS);
    }

    protected void appendTextString(String str) {
        appendTextString(str.getBytes());
    }

    protected void appendEncodedString(EncodedStringValue enStr) {
        if (!-assertionsDisabled) {
            if ((enStr != null ? PDU_PHONE_NUMBER_ADDRESS_TYPE : null) == null) {
                throw new AssertionError();
            }
        }
        int charset = enStr.getCharacterSet();
        byte[] textString = enStr.getTextString();
        if (textString != null) {
            this.mStack.newbuf();
            PositionMarker start = this.mStack.mark();
            appendShortInteger(charset);
            appendTextString(textString);
            int len = start.getLength();
            this.mStack.pop();
            appendValueLength((long) len);
            this.mStack.copy();
        }
    }

    protected void appendUintvarInteger(long value) {
        long max = 127;
        int i = PDU_COMPOSE_SUCCESS;
        while (i < PDU_UNKNOWN_ADDRESS_TYPE && value >= max) {
            max = (max << 7) | 127;
            i += PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        while (i > 0) {
            append((int) ((128 | ((value >>> (i * 7)) & 127)) & 255));
            i--;
        }
        append((int) (value & 127));
    }

    protected void appendDateValue(long date) {
        appendLongInteger(date);
    }

    protected void appendValueLength(long value) {
        if (value < 31) {
            appendShortLength((int) value);
            return;
        }
        append(LENGTH_QUOTE);
        appendUintvarInteger(value);
    }

    protected void appendQuotedString(byte[] text) {
        append(QUOTED_STRING_FLAG);
        arraycopy(text, PDU_COMPOSE_SUCCESS, text.length);
        append(PDU_COMPOSE_SUCCESS);
    }

    protected void appendQuotedString(String str) {
        appendQuotedString(str.getBytes());
    }

    private EncodedStringValue appendAddressType(EncodedStringValue address) {
        try {
            int addressType = checkAddressType(address.getString());
            EncodedStringValue temp = EncodedStringValue.copy(address);
            if (PDU_PHONE_NUMBER_ADDRESS_TYPE == addressType) {
                temp.appendTextString(STRING_PHONE_NUMBER_ADDRESS_TYPE.getBytes());
            } else if (PDU_IPV4_ADDRESS_TYPE == addressType) {
                temp.appendTextString(STRING_IPV4_ADDRESS_TYPE.getBytes());
            } else if (PDU_IPV6_ADDRESS_TYPE == addressType) {
                temp.appendTextString(STRING_IPV6_ADDRESS_TYPE.getBytes());
            }
            return temp;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private int appendHeader(int field) {
        EncodedStringValue temp;
        switch (field) {
            case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
            case PduPart.P_LEVEL /*130*/:
            case PduPart.P_NAME /*151*/:
                EncodedStringValue[] addr = this.mPduHeader.getEncodedStringValues(field);
                if (addr != null) {
                    int i = PDU_COMPOSE_SUCCESS;
                    while (true) {
                        int length = addr.length;
                        if (i >= r0) {
                            break;
                        }
                        temp = appendAddressType(addr[i]);
                        if (temp == null) {
                            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
                        }
                        appendOctet(field);
                        appendEncodedString(temp);
                        i += PDU_PHONE_NUMBER_ADDRESS_TYPE;
                    }
                } else {
                    return PDU_EMAIL_ADDRESS_TYPE;
                }
            case PduPart.P_DEP_NAME /*133*/:
                long date = this.mPduHeader.getLongInteger(field);
                if (-1 != date) {
                    appendOctet(field);
                    appendDateValue(date);
                    break;
                }
                return PDU_EMAIL_ADDRESS_TYPE;
            case PduPart.P_DEP_FILENAME /*134*/:
            case PduPart.P_DEP_PATH /*143*/:
            case PduPart.P_SECURE /*144*/:
            case PduPart.P_SEC /*145*/:
            case PduPart.P_READ_DATE /*149*/:
            case PduPart.P_START /*153*/:
            case PduPart.P_COMMENT /*155*/:
                int octet = this.mPduHeader.getOctet(field);
                if (octet != 0) {
                    appendOctet(field);
                    appendOctet(octet);
                    break;
                }
                return PDU_EMAIL_ADDRESS_TYPE;
            case PduPart.P_PADDING /*136*/:
                long expiry = this.mPduHeader.getLongInteger(field);
                if (-1 != expiry) {
                    appendOctet(field);
                    this.mStack.newbuf();
                    PositionMarker expiryStart = this.mStack.mark();
                    append(PduPart.P_DISPOSITION_ATTACHMENT);
                    appendLongInteger(expiry);
                    int expiryLength = expiryStart.getLength();
                    this.mStack.pop();
                    appendValueLength((long) expiryLength);
                    this.mStack.copy();
                    break;
                }
                return PDU_EMAIL_ADDRESS_TYPE;
            case PduPart.P_CT_MR_TYPE /*137*/:
                appendOctet(field);
                EncodedStringValue from = this.mPduHeader.getEncodedStringValue(field);
                if (from != null && !TextUtils.isEmpty(from.getString()) && !new String(from.getTextString()).equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                    this.mStack.newbuf();
                    PositionMarker fstart = this.mStack.mark();
                    append(PduPart.P_Q);
                    temp = appendAddressType(from);
                    if (temp != null) {
                        appendEncodedString(temp);
                        int flen = fstart.getLength();
                        this.mStack.pop();
                        appendValueLength((long) flen);
                        this.mStack.copy();
                        break;
                    }
                    return PDU_PHONE_NUMBER_ADDRESS_TYPE;
                }
                append(PDU_PHONE_NUMBER_ADDRESS_TYPE);
                append(PduPart.P_DISPOSITION_ATTACHMENT);
                break;
                break;
            case PduPart.P_DEP_START /*138*/:
                byte[] messageClass = this.mPduHeader.getTextString(field);
                if (messageClass != null) {
                    appendOctet(field);
                    if (!Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.getBytes())) {
                        if (!Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes())) {
                            if (!Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes())) {
                                if (!Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.getBytes())) {
                                    appendTextString(messageClass);
                                    break;
                                }
                                appendOctet(PduPart.P_LEVEL);
                                break;
                            }
                            appendOctet(PduPart.P_Q);
                            break;
                        }
                        appendOctet(PduPart.P_TYPE);
                        break;
                    }
                    appendOctet(PduPart.P_DISPOSITION_ATTACHMENT);
                    break;
                }
                return PDU_EMAIL_ADDRESS_TYPE;
            case PduPart.P_DEP_START_INFO /*139*/:
            case PduPart.P_FILENAME /*152*/:
                byte[] textString = this.mPduHeader.getTextString(field);
                if (textString != null) {
                    appendOctet(field);
                    appendTextString(textString);
                    break;
                }
                return PDU_EMAIL_ADDRESS_TYPE;
            case PduPart.P_DEP_DOMAIN /*141*/:
                appendOctet(field);
                int version = this.mPduHeader.getOctet(field);
                if (version != 0) {
                    appendShortInteger(version);
                    break;
                }
                appendShortInteger(18);
                break;
            case PduPart.P_SIZE /*150*/:
            case PduPart.P_START_INFO /*154*/:
                EncodedStringValue enString = this.mPduHeader.getEncodedStringValue(field);
                if (enString != null) {
                    appendOctet(field);
                    appendEncodedString(enString);
                    break;
                }
                return PDU_EMAIL_ADDRESS_TYPE;
            default:
                return PDU_IPV4_ADDRESS_TYPE;
        }
        return PDU_COMPOSE_SUCCESS;
    }

    private int makeReadRecInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = PDU_COMPOSE_SUCCESS;
        }
        appendOctet(PduPart.P_DEP_COMMENT);
        appendOctet(PduPart.P_DIFFERENCES);
        if (appendHeader(PduPart.P_DEP_DOMAIN) != 0 || appendHeader(PduPart.P_DEP_START_INFO) != 0 || appendHeader(PduPart.P_NAME) != 0 || appendHeader(PduPart.P_CT_MR_TYPE) != 0) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendHeader(PduPart.P_DEP_NAME);
        return appendHeader(PduPart.P_COMMENT) != 0 ? PDU_PHONE_NUMBER_ADDRESS_TYPE : PDU_COMPOSE_SUCCESS;
    }

    private int makeNotifyInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = PDU_COMPOSE_SUCCESS;
        }
        appendOctet(PduPart.P_DEP_COMMENT);
        appendOctet(PduPart.P_LEVEL);
        if (appendHeader(PduPart.P_FILENAME) != 0 || appendHeader(PduPart.P_DEP_DOMAIN) != 0 || appendHeader(PduPart.P_DEP_START) != 0) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendOctet(PduPart.P_MAX_AGE);
        long size = 0;
        if (this.mPdu instanceof NotificationInd) {
            size = ((NotificationInd) this.mPdu).getMessageSize();
        }
        appendLongInteger(size);
        if (appendHeader(PduPart.P_PADDING) != 0) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendOctet(PduPart.P_TYPE);
        byte[] contentLocation = null;
        if (this.mPdu instanceof NotificationInd) {
            contentLocation = ((NotificationInd) this.mPdu).getContentLocation();
        }
        if (contentLocation != null) {
            Log.d(LOG_TAG, "makeNotifyInd contentLocation != null");
            appendTextString(contentLocation);
        } else {
            Log.d(LOG_TAG, "makeNotifyInd contentLocation  = null");
        }
        appendOctet(PduPart.P_SIZE);
        EncodedStringValue subject = null;
        if (this.mPdu instanceof NotificationInd) {
            subject = ((NotificationInd) this.mPdu).getSubject();
        }
        if (subject != null) {
            Log.d(LOG_TAG, "makeNotifyInd subject != null");
            appendEncodedString(subject);
        } else {
            Log.d(LOG_TAG, "makeNotifyInd subject  = null");
        }
        return (appendHeader(PduPart.P_CT_MR_TYPE) == 0 && appendHeader(PduPart.P_READ_DATE) == 0) ? PDU_COMPOSE_SUCCESS : PDU_PHONE_NUMBER_ADDRESS_TYPE;
    }

    private int makeNotifyResp() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = PDU_COMPOSE_SUCCESS;
        }
        appendOctet(PduPart.P_DEP_COMMENT);
        appendOctet(PduPart.P_TYPE);
        if (appendHeader(PduPart.P_FILENAME) != 0 || appendHeader(PduPart.P_DEP_DOMAIN) != 0 || appendHeader(PduPart.P_READ_DATE) != 0) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendHeader(PduPart.P_SEC);
        return PDU_COMPOSE_SUCCESS;
    }

    private int makeAckInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = PDU_COMPOSE_SUCCESS;
        }
        appendOctet(PduPart.P_DEP_COMMENT);
        appendOctet(PduPart.P_DEP_NAME);
        if (appendHeader(PduPart.P_FILENAME) != 0 || appendHeader(PduPart.P_DEP_DOMAIN) != 0) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendHeader(PduPart.P_SEC);
        return PDU_COMPOSE_SUCCESS;
    }

    private int makeSendRetrievePdu(int type) {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = PDU_COMPOSE_SUCCESS;
        }
        appendOctet(PduPart.P_DEP_COMMENT);
        appendOctet(type);
        appendOctet(PduPart.P_FILENAME);
        byte[] trid = this.mPduHeader.getTextString(PduPart.P_FILENAME);
        if (trid == null) {
            throw new IllegalArgumentException("Transaction-ID is null.");
        }
        appendTextString(trid);
        if (appendHeader(PduPart.P_DEP_DOMAIN) != 0) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendHeader(PduPart.P_DEP_NAME);
        if (appendHeader(PduPart.P_CT_MR_TYPE) != 0) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        boolean recipient = -assertionsDisabled;
        if (appendHeader(PduPart.P_NAME) != PDU_PHONE_NUMBER_ADDRESS_TYPE) {
            recipient = true;
        }
        if (appendHeader(PduPart.P_LEVEL) != PDU_PHONE_NUMBER_ADDRESS_TYPE) {
            recipient = true;
        }
        if (appendHeader(PduPart.P_DISPOSITION_ATTACHMENT) != PDU_PHONE_NUMBER_ADDRESS_TYPE) {
            recipient = true;
        }
        if (!recipient) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        appendHeader(PduPart.P_SIZE);
        appendHeader(PduPart.P_DEP_START);
        appendHeader(PduPart.P_PADDING);
        appendHeader(PduPart.P_DEP_PATH);
        appendHeader(PduPart.P_DEP_FILENAME);
        appendHeader(PduPart.P_SECURE);
        if (type == PduHeaders.STATUS_UNRECOGNIZED) {
            appendHeader(PduPart.P_START);
            appendHeader(PduPart.P_START_INFO);
        }
        appendOctet(PduHeaders.STATUS_UNRECOGNIZED);
        return makeMessageBody(type);
    }

    private int makeMessageBody(int type) {
        this.mStack.newbuf();
        PositionMarker ctStart = this.mStack.mark();
        String contentType = new String(this.mPduHeader.getTextString(PduHeaders.STATUS_UNRECOGNIZED), Charset.defaultCharset());
        Integer contentTypeIdentifier = (Integer) mContentTypeMap.get(contentType);
        if (contentTypeIdentifier == null) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        PduBody body;
        appendShortInteger(contentTypeIdentifier.intValue());
        if (type == 132) {
            body = ((RetrieveConf) this.mPdu).getBody();
        } else {
            body = ((SendReq) this.mPdu).getBody();
        }
        if (body == null || body.getPartsNum() == 0) {
            appendUintvarInteger(0);
            this.mStack.pop();
            this.mStack.copy();
            return PDU_COMPOSE_SUCCESS;
        }
        try {
            PduPart part = body.getPart(PDU_COMPOSE_SUCCESS);
            byte[] start = part.getContentId();
            if (start != null) {
                appendOctet(PduPart.P_DEP_START);
                if (60 == start[PDU_COMPOSE_SUCCESS]) {
                    if (62 == start[start.length - 1]) {
                        appendTextString(start);
                    }
                }
                appendTextString("<" + new String(start, Charset.defaultCharset()) + ">");
            }
            appendOctet(PduPart.P_CT_MR_TYPE);
            appendTextString(part.getContentType());
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        int ctLength = ctStart.getLength();
        this.mStack.pop();
        appendValueLength((long) ctLength);
        this.mStack.copy();
        int partNum = body.getPartsNum();
        appendUintvarInteger((long) partNum);
        for (int i = PDU_COMPOSE_SUCCESS; i < partNum; i += PDU_PHONE_NUMBER_ADDRESS_TYPE) {
            part = body.getPart(i);
            this.mStack.newbuf();
            PositionMarker attachment = this.mStack.mark();
            this.mStack.newbuf();
            PositionMarker contentTypeBegin = this.mStack.mark();
            byte[] partContentType = part.getContentType();
            if (partContentType == null) {
                return PDU_PHONE_NUMBER_ADDRESS_TYPE;
            }
            Integer partContentTypeIdentifier = (Integer) mContentTypeMap.get(new String(partContentType, Charset.defaultCharset()));
            if (partContentTypeIdentifier == null) {
                appendTextString(partContentType);
            } else {
                appendShortInteger(partContentTypeIdentifier.intValue());
            }
            byte[] name = part.getName();
            if (name == null) {
                name = part.getFilename();
                if (name == null) {
                    name = part.getContentLocation();
                    if (name == null) {
                        name = part.getContentId();
                        if (name == null) {
                            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
                        }
                    }
                }
            }
            appendOctet(PduPart.P_DEP_NAME);
            appendTextString(name);
            int charset = part.getCharset();
            if (charset != 0) {
                appendOctet(PduPart.P_DISPOSITION_ATTACHMENT);
                appendShortInteger(charset);
            }
            int contentTypeLength = contentTypeBegin.getLength();
            this.mStack.pop();
            appendValueLength((long) contentTypeLength);
            this.mStack.copy();
            byte[] contentId = part.getContentId();
            if (contentId != null) {
                appendOctet(PduPart.P_CONTENT_ID);
                if (60 == contentId[PDU_COMPOSE_SUCCESS]) {
                    if (62 == contentId[contentId.length - 1]) {
                        appendQuotedString(contentId);
                    }
                }
                appendQuotedString("<" + new String(contentId, Charset.defaultCharset()) + ">");
            }
            byte[] contentLocation = part.getContentLocation();
            if (contentLocation != null) {
                appendOctet(PduPart.P_MAX_AGE);
                appendTextString(contentLocation);
            }
            int headerLength = attachment.getLength();
            int dataLength = PDU_COMPOSE_SUCCESS;
            byte[] partData = part.getData();
            if (partData != null) {
                arraycopy(partData, PDU_COMPOSE_SUCCESS, partData.length);
                dataLength = partData.length;
            } else {
                InputStream inputStream = null;
                try {
                    byte[] buffer = new byte[PDU_COMPOSER_BLOCK_SIZE];
                    inputStream = this.mResolver.openInputStream(part.getDataUri());
                    while (true) {
                        int len = inputStream.read(buffer);
                        if (len == -1) {
                            break;
                        }
                        this.mMessage.write(buffer, PDU_COMPOSE_SUCCESS, len);
                        this.mPosition += len;
                        dataLength += len;
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (FileNotFoundException e3) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                    return PDU_PHONE_NUMBER_ADDRESS_TYPE;
                } catch (IOException e5) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    return PDU_PHONE_NUMBER_ADDRESS_TYPE;
                } catch (RuntimeException e7) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                    return PDU_PHONE_NUMBER_ADDRESS_TYPE;
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e9) {
                        }
                    }
                }
            }
            if (dataLength != attachment.getLength() - headerLength) {
                throw new RuntimeException("BUG: Length sanity check failed");
            }
            this.mStack.pop();
            appendUintvarInteger((long) headerLength);
            appendUintvarInteger((long) dataLength);
            this.mStack.copy();
        }
        return PDU_COMPOSE_SUCCESS;
    }

    protected static int checkAddressType(String address) {
        if (address == null) {
            return PDU_UNKNOWN_ADDRESS_TYPE;
        }
        if (address.matches(REGEXP_IPV4_ADDRESS_TYPE)) {
            return PDU_IPV4_ADDRESS_TYPE;
        }
        if (address.matches(REGEXP_PHONE_NUMBER_ADDRESS_TYPE)) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        }
        if (address.matches(REGEXP_EMAIL_ADDRESS_TYPE)) {
            return PDU_EMAIL_ADDRESS_TYPE;
        }
        if (address.matches(REGEXP_IPV6_ADDRESS_TYPE)) {
            return PDU_IPV6_ADDRESS_TYPE;
        }
        return PDU_UNKNOWN_ADDRESS_TYPE;
    }
}
