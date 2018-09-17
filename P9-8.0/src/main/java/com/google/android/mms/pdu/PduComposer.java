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
    static final /* synthetic */ boolean -assertionsDisabled = (PduComposer.class.desiredAssertionStatus() ^ 1);
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
    protected ByteArrayOutputStream mMessage = null;
    private GenericPdu mPdu = null;
    private PduHeaders mPduHeader = null;
    protected int mPosition = 0;
    private final ContentResolver mResolver;
    private BufferStack mStack = null;

    private class BufferStack {
        private LengthRecordNode stack;
        int stackSize;
        private LengthRecordNode toCopy;

        /* synthetic */ BufferStack(PduComposer this$0, BufferStack -this1) {
            this();
        }

        private BufferStack() {
            this.stack = null;
            this.toCopy = null;
            this.stackSize = 0;
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
            this.stackSize++;
            PduComposer.this.mMessage = new ByteArrayOutputStream();
            PduComposer.this.mPosition = 0;
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
            PduComposer.this.arraycopy(this.toCopy.currentMessage.toByteArray(), 0, this.toCopy.currentPosition);
            this.toCopy = null;
        }

        PositionMarker mark() {
            PositionMarker m = new PositionMarker(PduComposer.this, null);
            m.c_pos = PduComposer.this.mPosition;
            m.currentStackSize = this.stackSize;
            return m;
        }
    }

    private static class LengthRecordNode {
        ByteArrayOutputStream currentMessage;
        public int currentPosition;
        public LengthRecordNode next;

        /* synthetic */ LengthRecordNode(LengthRecordNode -this0) {
            this();
        }

        private LengthRecordNode() {
            this.currentMessage = null;
            this.currentPosition = 0;
            this.next = null;
        }
    }

    private class PositionMarker {
        private int c_pos;
        private int currentStackSize;

        /* synthetic */ PositionMarker(PduComposer this$0, PositionMarker -this1) {
            this();
        }

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
        mContentTypeMap = null;
        mContentTypeMap = new HashMap();
        for (int i = 0; i < PduContentTypes.contentTypes.length; i++) {
            mContentTypeMap.put(PduContentTypes.contentTypes[i], Integer.valueOf(i));
        }
    }

    public PduComposer(Context context, GenericPdu pdu) {
        this.mPdu = pdu;
        this.mResolver = context.getContentResolver();
        this.mPduHeader = pdu.getPduHeaders();
        this.mStack = new BufferStack(this, null);
        this.mMessage = new ByteArrayOutputStream();
        this.mPosition = 0;
    }

    public byte[] make() {
        int type = this.mPdu.getMessageType();
        switch (type) {
            case 128:
            case 132:
                if (makeSendRetrievePdu(type) != 0) {
                    return null;
                }
                break;
            case 130:
                if (makeNotifyInd() != 0) {
                    return null;
                }
                break;
            case 131:
                if (makeNotifyResp() != 0) {
                    return null;
                }
                break;
            case 133:
                if (makeAckInd() != 0) {
                    return null;
                }
                break;
            case 135:
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
        this.mPosition++;
    }

    protected void appendShortInteger(int value) {
        append((value | 128) & 255);
    }

    protected void appendOctet(int number) {
        append(number);
    }

    protected void appendShortLength(int value) {
        append(value);
    }

    protected void appendLongInteger(long longInt) {
        long temp = longInt;
        int size = 0;
        while (temp != 0 && size < 8) {
            temp >>>= 8;
            size++;
        }
        appendShortLength(size);
        int shift = (size - 1) * 8;
        for (int i = 0; i < size; i++) {
            append((int) ((longInt >>> shift) & 255));
            shift -= 8;
        }
    }

    protected void appendTextString(byte[] text) {
        if ((text[0] & 255) > 127) {
            append(127);
        }
        arraycopy(text, 0, text.length);
        append(0);
    }

    protected void appendTextString(String str) {
        appendTextString(str.getBytes());
    }

    protected void appendEncodedString(EncodedStringValue enStr) {
        if (-assertionsDisabled || enStr != null) {
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
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    protected void appendUintvarInteger(long value) {
        long max = 127;
        int i = 0;
        while (i < 5 && value >= max) {
            max = (max << 7) | 127;
            i++;
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
        append(31);
        appendUintvarInteger(value);
    }

    protected void appendQuotedString(byte[] text) {
        append(34);
        arraycopy(text, 0, text.length);
        append(0);
    }

    protected void appendQuotedString(String str) {
        appendQuotedString(str.getBytes());
    }

    private EncodedStringValue appendAddressType(EncodedStringValue address) {
        try {
            int addressType = checkAddressType(address.getString());
            EncodedStringValue temp = EncodedStringValue.copy(address);
            if (1 == addressType) {
                temp.appendTextString(STRING_PHONE_NUMBER_ADDRESS_TYPE.getBytes());
            } else if (3 == addressType) {
                temp.appendTextString(STRING_IPV4_ADDRESS_TYPE.getBytes());
            } else if (4 == addressType) {
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
            case 129:
            case 130:
            case 151:
                EncodedStringValue[] addr = this.mPduHeader.getEncodedStringValues(field);
                if (addr != null) {
                    for (EncodedStringValue appendAddressType : addr) {
                        temp = appendAddressType(appendAddressType);
                        if (temp == null) {
                            return 1;
                        }
                        appendOctet(field);
                        appendEncodedString(temp);
                    }
                    break;
                }
                return 2;
            case 133:
                long date = this.mPduHeader.getLongInteger(field);
                if (-1 != date) {
                    appendOctet(field);
                    appendDateValue(date);
                    break;
                }
                return 2;
            case 134:
            case 143:
            case 144:
            case 145:
            case 149:
            case 153:
            case 155:
                int octet = this.mPduHeader.getOctet(field);
                if (octet != 0) {
                    appendOctet(field);
                    appendOctet(octet);
                    break;
                }
                return 2;
            case 136:
                long expiry = this.mPduHeader.getLongInteger(field);
                if (-1 != expiry) {
                    appendOctet(field);
                    this.mStack.newbuf();
                    PositionMarker expiryStart = this.mStack.mark();
                    append(129);
                    appendLongInteger(expiry);
                    int expiryLength = expiryStart.getLength();
                    this.mStack.pop();
                    appendValueLength((long) expiryLength);
                    this.mStack.copy();
                    break;
                }
                return 2;
            case 137:
                appendOctet(field);
                EncodedStringValue from = this.mPduHeader.getEncodedStringValue(field);
                if (from != null && !TextUtils.isEmpty(from.getString()) && !new String(from.getTextString()).equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                    this.mStack.newbuf();
                    PositionMarker fstart = this.mStack.mark();
                    append(128);
                    temp = appendAddressType(from);
                    if (temp != null) {
                        appendEncodedString(temp);
                        int flen = fstart.getLength();
                        this.mStack.pop();
                        appendValueLength((long) flen);
                        this.mStack.copy();
                        break;
                    }
                    return 1;
                }
                append(1);
                append(129);
                break;
                break;
            case 138:
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
                                appendOctet(130);
                                break;
                            }
                            appendOctet(128);
                            break;
                        }
                        appendOctet(131);
                        break;
                    }
                    appendOctet(129);
                    break;
                }
                return 2;
            case 139:
            case 152:
                byte[] textString = this.mPduHeader.getTextString(field);
                if (textString != null) {
                    appendOctet(field);
                    appendTextString(textString);
                    break;
                }
                return 2;
            case 141:
                appendOctet(field);
                int version = this.mPduHeader.getOctet(field);
                if (version != 0) {
                    appendShortInteger(version);
                    break;
                }
                appendShortInteger(18);
                break;
            case 150:
            case 154:
                EncodedStringValue enString = this.mPduHeader.getEncodedStringValue(field);
                if (enString != null) {
                    appendOctet(field);
                    appendEncodedString(enString);
                    break;
                }
                return 2;
            default:
                return 3;
        }
        return 0;
    }

    private int makeReadRecInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(135);
        if (appendHeader(141) != 0 || appendHeader(139) != 0 || appendHeader(151) != 0 || appendHeader(137) != 0) {
            return 1;
        }
        appendHeader(133);
        return appendHeader(155) != 0 ? 1 : 0;
    }

    private int makeNotifyInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(130);
        if (appendHeader(152) != 0 || appendHeader(141) != 0 || appendHeader(138) != 0) {
            return 1;
        }
        appendOctet(142);
        long size = 0;
        if (this.mPdu instanceof NotificationInd) {
            size = ((NotificationInd) this.mPdu).getMessageSize();
        }
        appendLongInteger(size);
        if (appendHeader(136) != 0) {
            return 1;
        }
        appendOctet(131);
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
        appendOctet(150);
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
        return (appendHeader(137) == 0 && appendHeader(149) == 0) ? 0 : 1;
    }

    private int makeNotifyResp() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(131);
        if (appendHeader(152) != 0 || appendHeader(141) != 0 || appendHeader(149) != 0) {
            return 1;
        }
        appendHeader(145);
        return 0;
    }

    private int makeAckInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(133);
        if (appendHeader(152) != 0 || appendHeader(141) != 0) {
            return 1;
        }
        appendHeader(145);
        return 0;
    }

    private int makeSendRetrievePdu(int type) {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(type);
        appendOctet(152);
        byte[] trid = this.mPduHeader.getTextString(152);
        if (trid == null) {
            throw new IllegalArgumentException("Transaction-ID is null.");
        }
        appendTextString(trid);
        if (appendHeader(141) != 0) {
            return 1;
        }
        appendHeader(133);
        if (appendHeader(137) != 0) {
            return 1;
        }
        boolean recipient = false;
        if (appendHeader(151) != 1) {
            recipient = true;
        }
        if (appendHeader(130) != 1) {
            recipient = true;
        }
        if (appendHeader(129) != 1) {
            recipient = true;
        }
        if (!recipient) {
            return 1;
        }
        appendHeader(150);
        appendHeader(138);
        appendHeader(136);
        appendHeader(143);
        appendHeader(134);
        appendHeader(144);
        if (type == 132) {
            appendHeader(153);
            appendHeader(154);
        }
        appendOctet(132);
        return makeMessageBody(type);
    }

    private int makeMessageBody(int type) {
        this.mStack.newbuf();
        PositionMarker ctStart = this.mStack.mark();
        Integer contentTypeIdentifier = (Integer) mContentTypeMap.get(new String(this.mPduHeader.getTextString(132), Charset.defaultCharset()));
        if (contentTypeIdentifier == null) {
            return 1;
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
            return 0;
        }
        PduPart part;
        try {
            part = body.getPart(0);
            byte[] start = part.getContentId();
            if (start != null) {
                appendOctet(138);
                if ((byte) 60 == start[0] && (byte) 62 == start[start.length - 1]) {
                    appendTextString(start);
                } else {
                    appendTextString("<" + new String(start, Charset.defaultCharset()) + ">");
                }
            }
            appendOctet(137);
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
        for (int i = 0; i < partNum; i++) {
            part = body.getPart(i);
            this.mStack.newbuf();
            PositionMarker attachment = this.mStack.mark();
            this.mStack.newbuf();
            PositionMarker contentTypeBegin = this.mStack.mark();
            byte[] partContentType = part.getContentType();
            if (partContentType == null) {
                return 1;
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
                            return 1;
                        }
                    }
                }
            }
            appendOctet(133);
            appendTextString(name);
            int charset = part.getCharset();
            if (charset != 0) {
                appendOctet(129);
                appendShortInteger(charset);
            }
            int contentTypeLength = contentTypeBegin.getLength();
            this.mStack.pop();
            appendValueLength((long) contentTypeLength);
            this.mStack.copy();
            byte[] contentId = part.getContentId();
            if (contentId != null) {
                appendOctet(192);
                if ((byte) 60 == contentId[0] && (byte) 62 == contentId[contentId.length - 1]) {
                    appendQuotedString(contentId);
                } else {
                    appendQuotedString("<" + new String(contentId, Charset.defaultCharset()) + ">");
                }
            }
            byte[] contentLocation = part.getContentLocation();
            if (contentLocation != null) {
                appendOctet(142);
                appendTextString(contentLocation);
            }
            int headerLength = attachment.getLength();
            int dataLength = 0;
            byte[] partData = part.getData();
            if (partData != null) {
                arraycopy(partData, 0, partData.length);
                dataLength = partData.length;
            } else {
                InputStream inputStream = null;
                try {
                    byte[] buffer = new byte[1024];
                    inputStream = this.mResolver.openInputStream(part.getDataUri());
                    while (true) {
                        int len = inputStream.read(buffer);
                        if (len == -1) {
                            break;
                        }
                        this.mMessage.write(buffer, 0, len);
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
                    return 1;
                } catch (IOException e5) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    return 1;
                } catch (RuntimeException e7) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                    return 1;
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
        return 0;
    }

    protected static int checkAddressType(String address) {
        if (address == null) {
            return 5;
        }
        if (address.matches(REGEXP_IPV4_ADDRESS_TYPE)) {
            return 3;
        }
        if (address.matches(REGEXP_PHONE_NUMBER_ADDRESS_TYPE)) {
            return 1;
        }
        if (address.matches(REGEXP_EMAIL_ADDRESS_TYPE)) {
            return 2;
        }
        if (address.matches(REGEXP_IPV6_ADDRESS_TYPE)) {
            return 4;
        }
        return 5;
    }
}
