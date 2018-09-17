package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.InvalidParameterException;

public final class GnssNavigationMessage implements Parcelable {
    public static final Creator<GnssNavigationMessage> CREATOR = null;
    private static final byte[] EMPTY_ARRAY = null;
    public static final int STATUS_PARITY_PASSED = 1;
    public static final int STATUS_PARITY_REBUILT = 2;
    public static final int STATUS_UNKNOWN = 0;
    public static final int TYPE_BDS_D1 = 1281;
    public static final int TYPE_BDS_D2 = 1282;
    public static final int TYPE_GAL_F = 1538;
    public static final int TYPE_GAL_I = 1537;
    public static final int TYPE_GLO_L1CA = 769;
    public static final int TYPE_GPS_CNAV2 = 260;
    public static final int TYPE_GPS_L1CA = 257;
    public static final int TYPE_GPS_L2CNAV = 258;
    public static final int TYPE_GPS_L5CNAV = 259;
    public static final int TYPE_UNKNOWN = 0;
    private byte[] mData;
    private int mMessageId;
    private int mStatus;
    private int mSubmessageId;
    private int mSvid;
    private int mType;

    public static abstract class Callback {
        public static final int STATUS_LOCATION_DISABLED = 2;
        public static final int STATUS_NOT_SUPPORTED = 0;
        public static final int STATUS_READY = 1;

        public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
        }

        public void onStatusChanged(int status) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GnssNavigationMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GnssNavigationMessage.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.GnssNavigationMessage.<clinit>():void");
    }

    public GnssNavigationMessage() {
        initialize();
    }

    public void set(GnssNavigationMessage navigationMessage) {
        this.mType = navigationMessage.mType;
        this.mSvid = navigationMessage.mSvid;
        this.mMessageId = navigationMessage.mMessageId;
        this.mSubmessageId = navigationMessage.mSubmessageId;
        this.mData = navigationMessage.mData;
        this.mStatus = navigationMessage.mStatus;
    }

    public void reset() {
        initialize();
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int value) {
        this.mType = value;
    }

    private String getTypeString() {
        switch (this.mType) {
            case STATUS_UNKNOWN /*0*/:
                return "Unknown";
            case TYPE_GPS_L1CA /*257*/:
                return "GPS L1 C/A";
            case TYPE_GPS_L2CNAV /*258*/:
                return "GPS L2-CNAV";
            case TYPE_GPS_L5CNAV /*259*/:
                return "GPS L5-CNAV";
            case TYPE_GPS_CNAV2 /*260*/:
                return "GPS CNAV2";
            case TYPE_GLO_L1CA /*769*/:
                return "Glonass L1 C/A";
            case TYPE_BDS_D1 /*1281*/:
                return "Beidou D1";
            case TYPE_BDS_D2 /*1282*/:
                return "Beidou D2";
            case TYPE_GAL_I /*1537*/:
                return "Galileo I";
            case TYPE_GAL_F /*1538*/:
                return "Galileo F";
            default:
                return "<Invalid:" + this.mType + ">";
        }
    }

    public int getSvid() {
        return this.mSvid;
    }

    public void setSvid(int value) {
        this.mSvid = value;
    }

    public int getMessageId() {
        return this.mMessageId;
    }

    public void setMessageId(int value) {
        this.mMessageId = value;
    }

    public int getSubmessageId() {
        return this.mSubmessageId;
    }

    public void setSubmessageId(int value) {
        this.mSubmessageId = value;
    }

    public byte[] getData() {
        return this.mData;
    }

    public void setData(byte[] value) {
        if (value == null) {
            throw new InvalidParameterException("Data must be a non-null array");
        }
        this.mData = value;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int value) {
        this.mStatus = value;
    }

    private String getStatusString() {
        switch (this.mStatus) {
            case STATUS_UNKNOWN /*0*/:
                return "Unknown";
            case STATUS_PARITY_PASSED /*1*/:
                return "ParityPassed";
            case STATUS_PARITY_REBUILT /*2*/:
                return "ParityRebuilt";
            default:
                return "<Invalid:" + this.mStatus + ">";
        }
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mType);
        parcel.writeInt(this.mSvid);
        parcel.writeInt(this.mMessageId);
        parcel.writeInt(this.mSubmessageId);
        parcel.writeInt(this.mData.length);
        parcel.writeByteArray(this.mData);
        parcel.writeInt(this.mStatus);
    }

    public int describeContents() {
        return STATUS_UNKNOWN;
    }

    public String toString() {
        int i = STATUS_UNKNOWN;
        String format = "   %-15s = %s\n";
        StringBuilder builder = new StringBuilder("GnssNavigationMessage:\n");
        Object[] objArr = new Object[STATUS_PARITY_REBUILT];
        objArr[STATUS_UNKNOWN] = "Type";
        objArr[STATUS_PARITY_PASSED] = getTypeString();
        builder.append(String.format("   %-15s = %s\n", objArr));
        objArr = new Object[STATUS_PARITY_REBUILT];
        objArr[STATUS_UNKNOWN] = "Svid";
        objArr[STATUS_PARITY_PASSED] = Integer.valueOf(this.mSvid);
        builder.append(String.format("   %-15s = %s\n", objArr));
        objArr = new Object[STATUS_PARITY_REBUILT];
        objArr[STATUS_UNKNOWN] = "Status";
        objArr[STATUS_PARITY_PASSED] = getStatusString();
        builder.append(String.format("   %-15s = %s\n", objArr));
        objArr = new Object[STATUS_PARITY_REBUILT];
        objArr[STATUS_UNKNOWN] = "MessageId";
        objArr[STATUS_PARITY_PASSED] = Integer.valueOf(this.mMessageId);
        builder.append(String.format("   %-15s = %s\n", objArr));
        objArr = new Object[STATUS_PARITY_REBUILT];
        objArr[STATUS_UNKNOWN] = "SubmessageId";
        objArr[STATUS_PARITY_PASSED] = Integer.valueOf(this.mSubmessageId);
        builder.append(String.format("   %-15s = %s\n", objArr));
        objArr = new Object[STATUS_PARITY_REBUILT];
        objArr[STATUS_UNKNOWN] = "Data";
        objArr[STATUS_PARITY_PASSED] = "{";
        builder.append(String.format("   %-15s = %s\n", objArr));
        String prefix = "        ";
        byte[] bArr = this.mData;
        int length = bArr.length;
        while (i < length) {
            byte value = bArr[i];
            builder.append(prefix);
            builder.append(value);
            prefix = ", ";
            i += STATUS_PARITY_PASSED;
        }
        builder.append(" }");
        return builder.toString();
    }

    private void initialize() {
        this.mType = STATUS_UNKNOWN;
        this.mSvid = STATUS_UNKNOWN;
        this.mMessageId = -1;
        this.mSubmessageId = -1;
        this.mData = EMPTY_ARRAY;
        this.mStatus = STATUS_UNKNOWN;
    }
}
