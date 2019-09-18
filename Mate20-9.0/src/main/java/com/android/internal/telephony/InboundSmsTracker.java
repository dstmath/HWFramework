package com.android.internal.telephony;

import android.content.ContentValues;
import android.database.Cursor;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.HexDump;
import java.util.Arrays;
import java.util.Date;

public class InboundSmsTracker {
    private static final int DEST_PORT_FLAG_3GPP = 131072;
    @VisibleForTesting
    public static final int DEST_PORT_FLAG_3GPP2 = 262144;
    @VisibleForTesting
    public static final int DEST_PORT_FLAG_3GPP2_WAP_PDU = 524288;
    @VisibleForTesting
    public static final int DEST_PORT_FLAG_NO_PORT = 65536;
    private static final int DEST_PORT_MASK = 65535;
    @VisibleForTesting
    public static final String SELECT_BY_DUPLICATE_REFERENCE = "address=? AND reference_number=? AND count=? AND sequence=? AND ((date=? AND message_body=?) OR deleted=0) AND (destination_port & 524288=0)";
    @VisibleForTesting
    public static final String SELECT_BY_DUPLICATE_REFERENCE_3GPP2WAP = "address=? AND reference_number=? AND count=? AND sequence=? AND ((date=? AND message_body=?) OR deleted=0) AND (destination_port & 524288=524288)";
    private static final String SELECT_BY_DUPLICATE_REFERENCE_3GPP2WAP_SUBID = "address=? AND reference_number=? AND count=? AND sequence=? AND ((date=? AND message_body=?) OR deleted=0) AND (destination_port & 524288=524288) AND sub_id=?";
    private static final String SELECT_BY_DUPLICATE_REFERENCE_SUBID = "address=? AND reference_number=? AND count=? AND sequence=? AND ((date=? AND message_body=?) OR deleted=0) AND (destination_port & 524288=0) AND sub_id=?";
    @VisibleForTesting
    public static final String SELECT_BY_REFERENCE = "address=? AND reference_number=? AND count=? AND (destination_port & 524288=0) AND deleted=0";
    @VisibleForTesting
    public static final String SELECT_BY_REFERENCE_3GPP2WAP = "address=? AND reference_number=? AND count=? AND (destination_port & 524288=524288) AND deleted=0";
    private static final String SELECT_BY_REFERENCE_3GPP2WAP_SUBID = "address=? AND reference_number=? AND count=? AND (destination_port & 524288=524288) AND deleted=0 AND sub_id=?";
    private static final String SELECT_BY_REFERENCE_SUBID = "address=? AND reference_number=? AND count=? AND (destination_port & 524288=0) AND deleted=0 AND sub_id=?";
    private static final String SUBID = " AND sub_id=?";
    private boolean isOldMessage = false;
    private final String mAddress;
    private String mDeleteWhere;
    private String[] mDeleteWhereArgs;
    private final int mDestPort;
    private final String mDisplayAddress;
    private final boolean mIs3gpp2;
    private final boolean mIs3gpp2WapPdu;
    private final String mMessageBody;
    private final int mMessageCount;
    private final byte[] mPdu;
    private final int mReferenceNumber;
    private final int mSequenceNumber;
    private final long mTimestamp;

    public InboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String displayAddress, String messageBody) {
        this.mPdu = pdu;
        this.mTimestamp = timestamp;
        this.mDestPort = destPort;
        this.mIs3gpp2 = is3gpp2;
        this.mIs3gpp2WapPdu = is3gpp2WapPdu;
        this.mMessageBody = messageBody;
        this.mAddress = address;
        this.mDisplayAddress = displayAddress;
        this.mReferenceNumber = -1;
        this.mSequenceNumber = getIndexOffset();
        this.mMessageCount = 1;
    }

    public InboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, String displayAddress, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody) {
        this.mPdu = pdu;
        this.mTimestamp = timestamp;
        this.mDestPort = destPort;
        this.mIs3gpp2 = is3gpp2;
        this.mIs3gpp2WapPdu = is3gpp2WapPdu;
        this.mMessageBody = messageBody;
        this.mDisplayAddress = displayAddress;
        this.mAddress = address;
        this.mReferenceNumber = referenceNumber;
        this.mSequenceNumber = sequenceNumber;
        this.mMessageCount = messageCount;
    }

    public InboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        this.mPdu = HexDump.hexStringToByteArray(cursor.getString(0));
        if (cursor.isNull(2)) {
            this.mDestPort = -1;
            this.mIs3gpp2 = isCurrentFormat3gpp2;
            this.mIs3gpp2WapPdu = false;
        } else {
            int destPort = cursor.getInt(2);
            if ((131072 & destPort) != 0) {
                this.mIs3gpp2 = false;
            } else if ((262144 & destPort) != 0) {
                this.mIs3gpp2 = true;
            } else {
                this.mIs3gpp2 = isCurrentFormat3gpp2;
            }
            this.mIs3gpp2WapPdu = (524288 & destPort) != 0;
            this.mDestPort = getRealDestPort(destPort);
        }
        this.mTimestamp = cursor.getLong(3);
        this.mAddress = cursor.getString(6);
        this.mDisplayAddress = cursor.getString(9);
        if (cursor.getInt(5) == 1) {
            long rowId = cursor.getLong(7);
            this.mReferenceNumber = -1;
            this.mSequenceNumber = getIndexOffset();
            this.mMessageCount = 1;
            this.mDeleteWhere = InboundSmsHandler.SELECT_BY_ID;
            this.mDeleteWhereArgs = new String[]{Long.toString(rowId)};
        } else {
            this.mReferenceNumber = cursor.getInt(4);
            this.mMessageCount = cursor.getInt(5);
            this.mSequenceNumber = cursor.getInt(1);
            int index = this.mSequenceNumber - getIndexOffset();
            if (index < 0 || index >= this.mMessageCount) {
                throw new IllegalArgumentException("invalid PDU sequence " + this.mSequenceNumber + " of " + this.mMessageCount);
            }
            this.mDeleteWhere = getQueryForSegments();
            this.mDeleteWhereArgs = new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount)};
        }
        this.mMessageBody = cursor.getString(8);
        if (cursor.isNull(10)) {
            this.isOldMessage = true;
        } else {
            this.isOldMessage = false;
        }
    }

    public ContentValues getContentValues() {
        int destPort;
        int destPort2;
        ContentValues values = new ContentValues();
        values.put("pdu", HexDump.toHexString(this.mPdu));
        values.put("date", Long.valueOf(this.mTimestamp));
        if (this.mDestPort == -1) {
            destPort = 65536;
        } else {
            destPort = this.mDestPort & 65535;
        }
        if (this.mIs3gpp2) {
            destPort2 = destPort | DEST_PORT_FLAG_3GPP2;
        } else {
            destPort2 = destPort | 131072;
        }
        if (this.mIs3gpp2WapPdu) {
            destPort2 |= 524288;
        }
        values.put("destination_port", Integer.valueOf(destPort2));
        if (this.mAddress != null) {
            values.put("address", this.mAddress);
            values.put("display_originating_addr", this.mDisplayAddress);
            values.put("reference_number", Integer.valueOf(this.mReferenceNumber));
            values.put("sequence", Integer.valueOf(this.mSequenceNumber));
        }
        values.put("count", Integer.valueOf(this.mMessageCount));
        values.put("message_body", this.mMessageBody);
        return values;
    }

    public static int getRealDestPort(int destPort) {
        if ((65536 & destPort) != 0) {
            return -1;
        }
        return 65535 & destPort;
    }

    public void setDeleteWhere(String deleteWhere, String[] deleteWhereArgs) {
        this.mDeleteWhere = deleteWhere;
        this.mDeleteWhereArgs = deleteWhereArgs;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("SmsTracker{timestamp=");
        builder.append(new Date(this.mTimestamp));
        builder.append(" destPort=");
        builder.append(this.mDestPort);
        builder.append(" is3gpp2=");
        builder.append(this.mIs3gpp2);
        if (this.mAddress != null) {
            builder.append(" address=");
            builder.append(this.mAddress);
            builder.append(" display_originating_addr=");
            builder.append(this.mDisplayAddress);
            builder.append(" refNumber=");
            builder.append(this.mReferenceNumber);
            builder.append(" seqNumber=");
            builder.append(this.mSequenceNumber);
            builder.append(" msgCount=");
            builder.append(this.mMessageCount);
        }
        if (this.mDeleteWhere != null) {
            builder.append(" deleteWhere(");
            builder.append(this.mDeleteWhere);
            builder.append(") deleteArgs=(");
            builder.append(Arrays.toString(this.mDeleteWhereArgs));
            builder.append(')');
        }
        builder.append('}');
        return builder.toString();
    }

    public byte[] getPdu() {
        return this.mPdu;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public int getDestPort() {
        return this.mDestPort;
    }

    public boolean is3gpp2() {
        return this.mIs3gpp2;
    }

    public String getFormat() {
        return this.mIs3gpp2 ? "3gpp2" : "3gpp";
    }

    public String getQueryForSegments() {
        return this.mIs3gpp2WapPdu ? SELECT_BY_REFERENCE_3GPP2WAP : SELECT_BY_REFERENCE;
    }

    public String getQueryForMultiPartDuplicates() {
        if (this.mIs3gpp2WapPdu) {
            return SELECT_BY_DUPLICATE_REFERENCE_3GPP2WAP;
        }
        return SELECT_BY_DUPLICATE_REFERENCE;
    }

    public int getIndexOffset() {
        return (!this.mIs3gpp2 || !this.mIs3gpp2WapPdu) ? 1 : 0;
    }

    public String getAddress() {
        return this.mAddress;
    }

    public String getDisplayAddress() {
        return this.mDisplayAddress;
    }

    public String getMessageBody() {
        return this.mMessageBody;
    }

    public int getReferenceNumber() {
        return this.mReferenceNumber;
    }

    public int getSequenceNumber() {
        return this.mSequenceNumber;
    }

    public int getMessageCount() {
        return this.mMessageCount;
    }

    public String getDeleteWhere() {
        return this.mDeleteWhere;
    }

    public String[] getDeleteWhereArgs() {
        return this.mDeleteWhereArgs;
    }

    public boolean isOldMessageInRawTable() {
        return this.isOldMessage;
    }

    public String getQueryForSegmentsSubId() {
        return this.mIs3gpp2WapPdu ? SELECT_BY_REFERENCE_3GPP2WAP_SUBID : SELECT_BY_REFERENCE_SUBID;
    }

    public String getQueryForMultiPartDuplicatesSubId() {
        if (this.mIs3gpp2WapPdu) {
            return SELECT_BY_DUPLICATE_REFERENCE_3GPP2WAP_SUBID;
        }
        return SELECT_BY_DUPLICATE_REFERENCE_SUBID;
    }
}
