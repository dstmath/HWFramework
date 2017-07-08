package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import java.security.InvalidParameterException;

public class GpsNavigationMessage implements Parcelable {
    public static final Creator<GpsNavigationMessage> CREATOR = null;
    private static final byte[] EMPTY_ARRAY = null;
    public static final short STATUS_PARITY_PASSED = (short) 1;
    public static final short STATUS_PARITY_REBUILT = (short) 2;
    public static final short STATUS_UNKNOWN = (short) 0;
    public static final byte TYPE_CNAV2 = (byte) 4;
    public static final byte TYPE_L1CA = (byte) 1;
    public static final byte TYPE_L2CNAV = (byte) 2;
    public static final byte TYPE_L5CNAV = (byte) 3;
    public static final byte TYPE_UNKNOWN = (byte) 0;
    private byte[] mData;
    private short mMessageId;
    private byte mPrn;
    private short mStatus;
    private short mSubmessageId;
    private byte mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GpsNavigationMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GpsNavigationMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.GpsNavigationMessage.<clinit>():void");
    }

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
        switch (this.mType) {
            case TextToSpeech.SUCCESS /*0*/:
                return "Unknown";
            case AudioState.ROUTE_EARPIECE /*1*/:
                return "L1 C/A";
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return "L2-CNAV";
            case Engine.DEFAULT_STREAM /*3*/:
                return "L5-CNAV";
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return "CNAV-2";
            default:
                return "<Invalid:" + this.mType + ">";
        }
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
        if (value == null) {
            throw new InvalidParameterException("Data must be a non-null array");
        }
        this.mData = value;
    }

    public short getStatus() {
        return this.mStatus;
    }

    public void setStatus(short value) {
        this.mStatus = value;
    }

    private String getStatusString() {
        switch (this.mStatus) {
            case TextToSpeech.SUCCESS /*0*/:
                return "Unknown";
            case AudioState.ROUTE_EARPIECE /*1*/:
                return "ParityPassed";
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return "ParityRebuilt";
            default:
                return "<Invalid:" + this.mStatus + ">";
        }
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByte(this.mType);
        parcel.writeByte(this.mPrn);
        parcel.writeInt(this.mMessageId);
        parcel.writeInt(this.mSubmessageId);
        parcel.writeInt(this.mData.length);
        parcel.writeByteArray(this.mData);
        parcel.writeInt(this.mStatus);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        int i = 0;
        String format = "   %-15s = %s\n";
        StringBuilder builder = new StringBuilder("GpsNavigationMessage:\n");
        builder.append(String.format("   %-15s = %s\n", new Object[]{"Type", getTypeString()}));
        builder.append(String.format("   %-15s = %s\n", new Object[]{"Prn", Byte.valueOf(this.mPrn)}));
        builder.append(String.format("   %-15s = %s\n", new Object[]{"Status", getStatusString()}));
        builder.append(String.format("   %-15s = %s\n", new Object[]{"MessageId", Short.valueOf(this.mMessageId)}));
        builder.append(String.format("   %-15s = %s\n", new Object[]{"SubmessageId", Short.valueOf(this.mSubmessageId)}));
        builder.append(String.format("   %-15s = %s\n", new Object[]{"Data", "{"}));
        String prefix = "        ";
        byte[] bArr = this.mData;
        int length = bArr.length;
        while (i < length) {
            byte value = bArr[i];
            builder.append(prefix);
            builder.append(value);
            prefix = ", ";
            i++;
        }
        builder.append(" }");
        return builder.toString();
    }

    private void initialize() {
        this.mType = (byte) 0;
        this.mPrn = (byte) 0;
        this.mMessageId = (short) -1;
        this.mSubmessageId = (short) -1;
        this.mData = EMPTY_ARRAY;
        this.mStatus = STATUS_UNKNOWN;
    }
}
