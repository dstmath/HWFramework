package android.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;

@SystemApi
public class GpsNavigationMessage implements Parcelable {
    public static final Parcelable.Creator<GpsNavigationMessage> CREATOR = new Parcelable.Creator<GpsNavigationMessage>() {
        /* class android.location.GpsNavigationMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GpsNavigationMessage createFromParcel(Parcel parcel) {
            GpsNavigationMessage navigationMessage = new GpsNavigationMessage();
            navigationMessage.setType(parcel.readByte());
            navigationMessage.setPrn(parcel.readByte());
            navigationMessage.setMessageId((short) parcel.readInt());
            navigationMessage.setSubmessageId((short) parcel.readInt());
            byte[] data = new byte[parcel.readInt()];
            parcel.readByteArray(data);
            navigationMessage.setData(data);
            if (parcel.dataAvail() >= 32) {
                navigationMessage.setStatus((short) parcel.readInt());
            } else {
                navigationMessage.setStatus(0);
            }
            return navigationMessage;
        }

        @Override // android.os.Parcelable.Creator
        public GpsNavigationMessage[] newArray(int size) {
            return new GpsNavigationMessage[size];
        }
    };
    private static final byte[] EMPTY_ARRAY = new byte[0];
    public static final short STATUS_PARITY_PASSED = 1;
    public static final short STATUS_PARITY_REBUILT = 2;
    public static final short STATUS_UNKNOWN = 0;
    public static final byte TYPE_CNAV2 = 4;
    public static final byte TYPE_L1CA = 1;
    public static final byte TYPE_L2CNAV = 2;
    public static final byte TYPE_L5CNAV = 3;
    public static final byte TYPE_UNKNOWN = 0;
    private byte[] mData;
    private short mMessageId;
    private byte mPrn;
    private short mStatus;
    private short mSubmessageId;
    private byte mType;

    GpsNavigationMessage() {
        initialize();
    }

    public void set(GpsNavigationMessage navigationMessage) {
        this.mType = navigationMessage.mType;
        this.mPrn = navigationMessage.mPrn;
        this.mMessageId = navigationMessage.mMessageId;
        this.mSubmessageId = navigationMessage.mSubmessageId;
        this.mData = navigationMessage.mData;
        this.mStatus = navigationMessage.mStatus;
    }

    public void reset() {
        initialize();
    }

    public byte getType() {
        return this.mType;
    }

    public void setType(byte value) {
        this.mType = value;
    }

    private String getTypeString() {
        byte b = this.mType;
        if (b == 0) {
            return "Unknown";
        }
        if (b == 1) {
            return "L1 C/A";
        }
        if (b == 2) {
            return "L2-CNAV";
        }
        if (b == 3) {
            return "L5-CNAV";
        }
        if (b == 4) {
            return "CNAV-2";
        }
        return "<Invalid:" + ((int) this.mType) + ">";
    }

    public byte getPrn() {
        return this.mPrn;
    }

    public void setPrn(byte value) {
        this.mPrn = value;
    }

    public short getMessageId() {
        return this.mMessageId;
    }

    public void setMessageId(short value) {
        this.mMessageId = value;
    }

    public short getSubmessageId() {
        return this.mSubmessageId;
    }

    public void setSubmessageId(short value) {
        this.mSubmessageId = value;
    }

    public byte[] getData() {
        return this.mData;
    }

    public void setData(byte[] value) {
        if (value != null) {
            this.mData = value;
            return;
        }
        throw new InvalidParameterException("Data must be a non-null array");
    }

    public short getStatus() {
        return this.mStatus;
    }

    public void setStatus(short value) {
        this.mStatus = value;
    }

    private String getStatusString() {
        short s = this.mStatus;
        if (s == 0) {
            return "Unknown";
        }
        if (s == 1) {
            return "ParityPassed";
        }
        if (s == 2) {
            return "ParityRebuilt";
        }
        return "<Invalid:" + ((int) this.mStatus) + ">";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByte(this.mType);
        parcel.writeByte(this.mPrn);
        parcel.writeInt(this.mMessageId);
        parcel.writeInt(this.mSubmessageId);
        parcel.writeInt(this.mData.length);
        parcel.writeByteArray(this.mData);
        parcel.writeInt(this.mStatus);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("GpsNavigationMessage:\n");
        builder.append(String.format("   %-15s = %s\n", "Type", getTypeString()));
        builder.append(String.format("   %-15s = %s\n", "Prn", Byte.valueOf(this.mPrn)));
        builder.append(String.format("   %-15s = %s\n", "Status", getStatusString()));
        builder.append(String.format("   %-15s = %s\n", "MessageId", Short.valueOf(this.mMessageId)));
        builder.append(String.format("   %-15s = %s\n", "SubmessageId", Short.valueOf(this.mSubmessageId)));
        builder.append(String.format("   %-15s = %s\n", "Data", "{"));
        String prefix = "        ";
        byte[] bArr = this.mData;
        for (byte value : bArr) {
            builder.append(prefix);
            builder.append((int) value);
            prefix = ", ";
        }
        builder.append(" }");
        return builder.toString();
    }

    private void initialize() {
        this.mType = 0;
        this.mPrn = 0;
        this.mMessageId = -1;
        this.mSubmessageId = -1;
        this.mData = EMPTY_ARRAY;
        this.mStatus = 0;
    }
}
