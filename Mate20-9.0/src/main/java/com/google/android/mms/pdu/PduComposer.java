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
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
    /* access modifiers changed from: private */
    public BufferStack mStack = null;

    private class BufferStack {
        private LengthRecordNode stack;
        int stackSize;
        private LengthRecordNode toCopy;

        private BufferStack() {
            this.stack = null;
            this.toCopy = null;
            this.stackSize = 0;
        }

        /* access modifiers changed from: package-private */
        public void newbuf() {
            if (this.toCopy == null) {
                LengthRecordNode temp = new LengthRecordNode();
                temp.currentMessage = PduComposer.this.mMessage;
                temp.currentPosition = PduComposer.this.mPosition;
                temp.next = this.stack;
                this.stack = temp;
                this.stackSize++;
                PduComposer.this.mMessage = new ByteArrayOutputStream();
                PduComposer.this.mPosition = 0;
                return;
            }
            throw new RuntimeException("BUG: Invalid newbuf() before copy()");
        }

        /* access modifiers changed from: package-private */
        public void pop() {
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

        /* access modifiers changed from: package-private */
        public void copy() {
            PduComposer.this.arraycopy(this.toCopy.currentMessage.toByteArray(), 0, this.toCopy.currentPosition);
            this.toCopy = null;
        }

        /* access modifiers changed from: package-private */
        public PositionMarker mark() {
            PositionMarker m = new PositionMarker();
            int unused = m.c_pos = PduComposer.this.mPosition;
            int unused2 = m.currentStackSize = this.stackSize;
            return m;
        }
    }

    private static class LengthRecordNode {
        ByteArrayOutputStream currentMessage;
        public int currentPosition;
        public LengthRecordNode next;

        private LengthRecordNode() {
            this.currentMessage = null;
            this.currentPosition = 0;
            this.next = null;
        }
    }

    private class PositionMarker {
        /* access modifiers changed from: private */
        public int c_pos;
        /* access modifiers changed from: private */
        public int currentStackSize;

        private PositionMarker() {
        }

        /* access modifiers changed from: package-private */
        public int getLength() {
            if (this.currentStackSize == PduComposer.this.mStack.stackSize) {
                return PduComposer.this.mPosition - this.c_pos;
            }
            throw new RuntimeException("BUG: Invalid call to getLength()");
        }
    }

    static {
        mContentTypeMap = null;
        mContentTypeMap = new HashMap<>();
        for (int i = 0; i < PduContentTypes.contentTypes.length; i++) {
            mContentTypeMap.put(PduContentTypes.contentTypes[i], Integer.valueOf(i));
        }
    }

    public PduComposer(Context context, GenericPdu pdu) {
        this.mPdu = pdu;
        this.mResolver = context.getContentResolver();
        this.mPduHeader = pdu.getPduHeaders();
        this.mStack = new BufferStack();
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

    /* access modifiers changed from: protected */
    public void arraycopy(byte[] buf, int pos, int length) {
        this.mMessage.write(buf, pos, length);
        this.mPosition += length;
    }

    /* access modifiers changed from: protected */
    public void append(int value) {
        this.mMessage.write(value);
        this.mPosition++;
    }

    /* access modifiers changed from: protected */
    public void appendShortInteger(int value) {
        append((value | 128) & 255);
    }

    /* access modifiers changed from: protected */
    public void appendOctet(int number) {
        append(number);
    }

    /* access modifiers changed from: protected */
    public void appendShortLength(int value) {
        append(value);
    }

    /* access modifiers changed from: protected */
    public void appendLongInteger(long longInt) {
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

    /* access modifiers changed from: protected */
    public void appendTextString(byte[] text) {
        if ((text[0] & 255) > Byte.MAX_VALUE) {
            append(127);
        }
        arraycopy(text, 0, text.length);
        append(0);
    }

    /* access modifiers changed from: protected */
    public void appendTextString(String str) {
        appendTextString(str.getBytes());
    }

    /* access modifiers changed from: protected */
    public void appendEncodedString(EncodedStringValue enStr) {
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

    /* access modifiers changed from: protected */
    public void appendUintvarInteger(long value) {
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

    /* access modifiers changed from: protected */
    public void appendDateValue(long date) {
        appendLongInteger(date);
    }

    /* access modifiers changed from: protected */
    public void appendValueLength(long value) {
        if (value < 31) {
            appendShortLength((int) value);
            return;
        }
        append(31);
        appendUintvarInteger(value);
    }

    /* access modifiers changed from: protected */
    public void appendQuotedString(byte[] text) {
        append(34);
        arraycopy(text, 0, text.length);
        append(0);
    }

    /* access modifiers changed from: protected */
    public void appendQuotedString(String str) {
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
        switch (field) {
            case 129:
            case 130:
            case 151:
                EncodedStringValue[] addr = this.mPduHeader.getEncodedStringValues(field);
                if (addr != null) {
                    for (EncodedStringValue appendAddressType : addr) {
                        EncodedStringValue temp = appendAddressType(appendAddressType);
                        if (temp == null) {
                            return 1;
                        }
                        appendOctet(field);
                        appendEncodedString(temp);
                    }
                    break;
                } else {
                    return 2;
                }
            case 133:
                long date = this.mPduHeader.getLongInteger(field);
                if (-1 != date) {
                    appendOctet(field);
                    appendDateValue(date);
                    break;
                } else {
                    return 2;
                }
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
                } else {
                    return 2;
                }
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
                } else {
                    return 2;
                }
            case 137:
                appendOctet(field);
                EncodedStringValue from = this.mPduHeader.getEncodedStringValue(field);
                if (from != null && !TextUtils.isEmpty(from.getString()) && !new String(from.getTextString()).equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                    this.mStack.newbuf();
                    PositionMarker fstart = this.mStack.mark();
                    append(128);
                    EncodedStringValue temp2 = appendAddressType(from);
                    if (temp2 != null) {
                        appendEncodedString(temp2);
                        int flen = fstart.getLength();
                        this.mStack.pop();
                        appendValueLength((long) flen);
                        this.mStack.copy();
                        break;
                    } else {
                        return 1;
                    }
                } else {
                    append(1);
                    append(129);
                    break;
                }
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
                                } else {
                                    appendOctet(130);
                                    break;
                                }
                            } else {
                                appendOctet(128);
                                break;
                            }
                        } else {
                            appendOctet(131);
                            break;
                        }
                    } else {
                        appendOctet(129);
                        break;
                    }
                } else {
                    return 2;
                }
            case 139:
            case 152:
                byte[] textString = this.mPduHeader.getTextString(field);
                if (textString != null) {
                    appendOctet(field);
                    appendTextString(textString);
                    break;
                } else {
                    return 2;
                }
            case 141:
                appendOctet(field);
                int version = this.mPduHeader.getOctet(field);
                if (version != 0) {
                    appendShortInteger(version);
                    break;
                } else {
                    appendShortInteger(18);
                    break;
                }
            case 150:
            case 154:
                EncodedStringValue enString = this.mPduHeader.getEncodedStringValue(field);
                if (enString != null) {
                    appendOctet(field);
                    appendEncodedString(enString);
                    break;
                } else {
                    return 2;
                }
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
        if (appendHeader(155) != 0) {
            return 1;
        }
        return 0;
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
        if (appendHeader(137) == 0 && appendHeader(149) == 0) {
            return 0;
        }
        return 1;
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
        if (trid != null) {
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
        throw new IllegalArgumentException("Transaction-ID is null.");
    }

    /* JADX WARNING: Removed duplicated region for block: B:136:0x02d7 A[SYNTHETIC, Splitter:B:136:0x02d7] */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x02ea A[SYNTHETIC, Splitter:B:144:0x02ea] */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x02fe A[SYNTHETIC, Splitter:B:153:0x02fe] */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x0312 A[SYNTHETIC, Splitter:B:162:0x0312] */
    private int makeMessageBody(int type) {
        PduBody body;
        Integer contentTypeIdentifier;
        int dataLength;
        Throwable th;
        this.mStack.newbuf();
        PositionMarker ctStart = this.mStack.mark();
        String contentType = new String(this.mPduHeader.getTextString(132), Charset.defaultCharset());
        Integer contentTypeIdentifier2 = mContentTypeMap.get(contentType);
        int i = 1;
        if (contentTypeIdentifier2 == null) {
            return 1;
        }
        appendShortInteger(contentTypeIdentifier2.intValue());
        if (type == 132) {
            body = ((RetrieveConf) this.mPdu).getBody();
        } else {
            body = ((SendReq) this.mPdu).getBody();
        }
        PduBody body2 = body;
        if (body2 == null) {
            String str = contentType;
            PduBody pduBody = body2;
            Integer num = contentTypeIdentifier2;
        } else if (body2.getPartsNum() == 0) {
            PositionMarker positionMarker = ctStart;
            String str2 = contentType;
            PduBody pduBody2 = body2;
            Integer num2 = contentTypeIdentifier2;
        } else {
            try {
                PduPart part = body2.getPart(0);
                byte[] start = part.getContentId();
                if (start != null) {
                    appendOctet(138);
                    if (60 == start[0] && 62 == start[start.length - 1]) {
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
            int partNum = body2.getPartsNum();
            appendUintvarInteger((long) partNum);
            int dataLength2 = 0;
            while (true) {
                int i2 = dataLength2;
                if (i2 < partNum) {
                    PduPart part2 = body2.getPart(i2);
                    this.mStack.newbuf();
                    PositionMarker attachment = this.mStack.mark();
                    this.mStack.newbuf();
                    PositionMarker contentTypeBegin = this.mStack.mark();
                    byte[] partContentType = part2.getContentType();
                    if (partContentType == null) {
                        return i;
                    }
                    Integer partContentTypeIdentifier = mContentTypeMap.get(new String(partContentType, Charset.defaultCharset()));
                    if (partContentTypeIdentifier == null) {
                        appendTextString(partContentType);
                    } else {
                        appendShortInteger(partContentTypeIdentifier.intValue());
                    }
                    byte[] name = part2.getName();
                    if (name == null) {
                        name = part2.getFilename();
                        if (name == null) {
                            name = part2.getContentLocation();
                            if (name == null) {
                                name = part2.getContentId();
                                if (name == null) {
                                    return 1;
                                }
                            }
                        }
                    }
                    appendOctet(133);
                    appendTextString(name);
                    PositionMarker ctStart2 = ctStart;
                    int charset = part2.getCharset();
                    if (charset != 0) {
                        appendOctet(129);
                        appendShortInteger(charset);
                    }
                    int i3 = charset;
                    int contentTypeLength = contentTypeBegin.getLength();
                    this.mStack.pop();
                    String contentType2 = contentType;
                    PduBody body3 = body2;
                    appendValueLength((long) contentTypeLength);
                    this.mStack.copy();
                    byte[] contentId = part2.getContentId();
                    if (contentId != null) {
                        appendOctet(192);
                        if (60 == contentId[0]) {
                            if (62 == contentId[contentId.length - 1]) {
                                appendQuotedString(contentId);
                                int i4 = contentTypeLength;
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("<");
                        int i5 = contentTypeLength;
                        sb.append(new String(contentId, Charset.defaultCharset()));
                        sb.append(">");
                        appendQuotedString(sb.toString());
                    }
                    byte[] contentLocation = part2.getContentLocation();
                    if (contentLocation != null) {
                        appendOctet(142);
                        appendTextString(contentLocation);
                    }
                    int headerLength = attachment.getLength();
                    int dataLength3 = 0;
                    byte[] bArr = contentLocation;
                    byte[] partData = part2.getData();
                    if (partData != null) {
                        byte[] bArr2 = contentId;
                        arraycopy(partData, 0, partData.length);
                        dataLength = partData.length;
                        byte[] bArr3 = partData;
                        contentTypeIdentifier = contentTypeIdentifier2;
                        Integer num3 = partContentTypeIdentifier;
                    } else {
                        InputStream cr = null;
                        try {
                            byte[] buffer = new byte[1024];
                            byte[] bArr4 = partData;
                            try {
                                try {
                                    cr = this.mResolver.openInputStream(part2.getDataUri());
                                    int len = 0;
                                    while (true) {
                                        int i6 = len;
                                        try {
                                            int len2 = cr.read(buffer);
                                            int len3 = len2;
                                            contentTypeIdentifier = contentTypeIdentifier2;
                                            if (len2 == -1) {
                                                break;
                                            }
                                            try {
                                                Integer partContentTypeIdentifier2 = partContentTypeIdentifier;
                                                int len4 = len3;
                                                try {
                                                    this.mMessage.write(buffer, 0, len4);
                                                    this.mPosition += len4;
                                                    dataLength3 += len4;
                                                    len = len4;
                                                    contentTypeIdentifier2 = contentTypeIdentifier;
                                                    partContentTypeIdentifier = partContentTypeIdentifier2;
                                                } catch (FileNotFoundException e2) {
                                                    e = e2;
                                                    FileNotFoundException fileNotFoundException = e;
                                                    if (cr != null) {
                                                    }
                                                    return 1;
                                                } catch (IOException e3) {
                                                    e = e3;
                                                    IOException iOException = e;
                                                    if (cr != null) {
                                                    }
                                                    return 1;
                                                } catch (RuntimeException e4) {
                                                    e = e4;
                                                    RuntimeException runtimeException = e;
                                                    if (cr != null) {
                                                    }
                                                    return 1;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    if (cr != null) {
                                                    }
                                                    throw th;
                                                }
                                            } catch (FileNotFoundException e5) {
                                                e = e5;
                                                Integer num4 = partContentTypeIdentifier;
                                                FileNotFoundException fileNotFoundException2 = e;
                                                if (cr != null) {
                                                }
                                                return 1;
                                            } catch (IOException e6) {
                                                e = e6;
                                                Integer num5 = partContentTypeIdentifier;
                                                IOException iOException2 = e;
                                                if (cr != null) {
                                                }
                                                return 1;
                                            } catch (RuntimeException e7) {
                                                e = e7;
                                                Integer num6 = partContentTypeIdentifier;
                                                RuntimeException runtimeException2 = e;
                                                if (cr != null) {
                                                }
                                                return 1;
                                            } catch (Throwable th3) {
                                                Integer num7 = partContentTypeIdentifier;
                                                th = th3;
                                                if (cr != null) {
                                                }
                                                throw th;
                                            }
                                        } catch (FileNotFoundException e8) {
                                            e = e8;
                                            Integer num8 = contentTypeIdentifier2;
                                            Integer num9 = partContentTypeIdentifier;
                                            FileNotFoundException fileNotFoundException22 = e;
                                            if (cr != null) {
                                                try {
                                                    cr.close();
                                                } catch (IOException e9) {
                                                }
                                            }
                                            return 1;
                                        } catch (IOException e10) {
                                            e = e10;
                                            Integer num10 = contentTypeIdentifier2;
                                            Integer num11 = partContentTypeIdentifier;
                                            IOException iOException22 = e;
                                            if (cr != null) {
                                                try {
                                                    cr.close();
                                                } catch (IOException e11) {
                                                }
                                            }
                                            return 1;
                                        } catch (RuntimeException e12) {
                                            e = e12;
                                            Integer num12 = contentTypeIdentifier2;
                                            Integer num13 = partContentTypeIdentifier;
                                            RuntimeException runtimeException22 = e;
                                            if (cr != null) {
                                                try {
                                                    cr.close();
                                                } catch (IOException e13) {
                                                }
                                            }
                                            return 1;
                                        } catch (Throwable th4) {
                                            Integer num14 = contentTypeIdentifier2;
                                            Integer num15 = partContentTypeIdentifier;
                                            th = th4;
                                            if (cr != null) {
                                                try {
                                                    cr.close();
                                                } catch (IOException e14) {
                                                }
                                            }
                                            throw th;
                                        }
                                    }
                                    if (cr != null) {
                                        try {
                                            cr.close();
                                        } catch (IOException e15) {
                                        }
                                    }
                                    dataLength = dataLength3;
                                } catch (FileNotFoundException e16) {
                                    e = e16;
                                    Integer num16 = contentTypeIdentifier2;
                                    Integer num17 = partContentTypeIdentifier;
                                    cr = null;
                                    FileNotFoundException fileNotFoundException222 = e;
                                    if (cr != null) {
                                    }
                                    return 1;
                                } catch (IOException e17) {
                                    e = e17;
                                    Integer num18 = contentTypeIdentifier2;
                                    Integer num19 = partContentTypeIdentifier;
                                    cr = null;
                                    IOException iOException222 = e;
                                    if (cr != null) {
                                    }
                                    return 1;
                                } catch (RuntimeException e18) {
                                    e = e18;
                                    Integer num20 = contentTypeIdentifier2;
                                    Integer num21 = partContentTypeIdentifier;
                                    cr = null;
                                    RuntimeException runtimeException222 = e;
                                    if (cr != null) {
                                    }
                                    return 1;
                                } catch (Throwable th5) {
                                    Integer num22 = contentTypeIdentifier2;
                                    Integer num23 = partContentTypeIdentifier;
                                    th = th5;
                                    cr = null;
                                    if (cr != null) {
                                    }
                                    throw th;
                                }
                            } catch (FileNotFoundException e19) {
                                e = e19;
                                Integer num24 = contentTypeIdentifier2;
                                Integer num25 = partContentTypeIdentifier;
                                FileNotFoundException fileNotFoundException2222 = e;
                                if (cr != null) {
                                }
                                return 1;
                            } catch (IOException e20) {
                                e = e20;
                                Integer num26 = contentTypeIdentifier2;
                                Integer num27 = partContentTypeIdentifier;
                                IOException iOException2222 = e;
                                if (cr != null) {
                                }
                                return 1;
                            } catch (RuntimeException e21) {
                                e = e21;
                                Integer num28 = contentTypeIdentifier2;
                                Integer num29 = partContentTypeIdentifier;
                                RuntimeException runtimeException2222 = e;
                                if (cr != null) {
                                }
                                return 1;
                            } catch (Throwable th6) {
                                Integer num30 = contentTypeIdentifier2;
                                Integer num31 = partContentTypeIdentifier;
                                th = th6;
                                if (cr != null) {
                                }
                                throw th;
                            }
                        } catch (FileNotFoundException e22) {
                            e = e22;
                            byte[] bArr5 = partData;
                            Integer num32 = contentTypeIdentifier2;
                            Integer num33 = partContentTypeIdentifier;
                            FileNotFoundException fileNotFoundException22222 = e;
                            if (cr != null) {
                            }
                            return 1;
                        } catch (IOException e23) {
                            e = e23;
                            byte[] bArr6 = partData;
                            Integer num34 = contentTypeIdentifier2;
                            Integer num35 = partContentTypeIdentifier;
                            IOException iOException22222 = e;
                            if (cr != null) {
                            }
                            return 1;
                        } catch (RuntimeException e24) {
                            e = e24;
                            byte[] bArr7 = partData;
                            Integer num36 = contentTypeIdentifier2;
                            Integer num37 = partContentTypeIdentifier;
                            RuntimeException runtimeException22222 = e;
                            if (cr != null) {
                            }
                            return 1;
                        } catch (Throwable th7) {
                            byte[] bArr8 = partData;
                            Integer num38 = contentTypeIdentifier2;
                            Integer num39 = partContentTypeIdentifier;
                            th = th7;
                            if (cr != null) {
                            }
                            throw th;
                        }
                    }
                    if (dataLength == attachment.getLength() - headerLength) {
                        this.mStack.pop();
                        appendUintvarInteger((long) headerLength);
                        appendUintvarInteger((long) dataLength);
                        this.mStack.copy();
                        dataLength2 = i2 + 1;
                        ctStart = ctStart2;
                        contentType = contentType2;
                        body2 = body3;
                        contentTypeIdentifier2 = contentTypeIdentifier;
                        i = 1;
                    } else {
                        throw new RuntimeException("BUG: Length sanity check failed");
                    }
                } else {
                    String str3 = contentType;
                    PduBody pduBody3 = body2;
                    Integer num40 = contentTypeIdentifier2;
                    return 0;
                }
            }
        }
        appendUintvarInteger(0);
        this.mStack.pop();
        this.mStack.copy();
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
