package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.SmsConstants;

public abstract class CellInfo implements Parcelable {
    public static final Creator<CellInfo> CREATOR = null;
    public static final int TIMESTAMP_TYPE_ANTENNA = 1;
    public static final int TIMESTAMP_TYPE_JAVA_RIL = 4;
    public static final int TIMESTAMP_TYPE_MODEM = 2;
    public static final int TIMESTAMP_TYPE_OEM_RIL = 3;
    public static final int TIMESTAMP_TYPE_UNKNOWN = 0;
    protected static final int TYPE_CDMA = 2;
    protected static final int TYPE_GSM = 1;
    protected static final int TYPE_LTE = 3;
    protected static final int TYPE_WCDMA = 4;
    private boolean mRegistered;
    private long mTimeStamp;
    private int mTimeStampType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.CellInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.CellInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.CellInfo.<clinit>():void");
    }

    public abstract void writeToParcel(Parcel parcel, int i);

    protected CellInfo() {
        this.mRegistered = false;
        this.mTimeStampType = TIMESTAMP_TYPE_UNKNOWN;
        this.mTimeStamp = Long.MAX_VALUE;
    }

    protected CellInfo(CellInfo ci) {
        this.mRegistered = ci.mRegistered;
        this.mTimeStampType = ci.mTimeStampType;
        this.mTimeStamp = ci.mTimeStamp;
    }

    public boolean isRegistered() {
        return this.mRegistered;
    }

    public void setRegistered(boolean registered) {
        this.mRegistered = registered;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public int getTimeStampType() {
        return this.mTimeStampType;
    }

    public void setTimeStampType(int timeStampType) {
        if (timeStampType < 0 || timeStampType > TYPE_WCDMA) {
            this.mTimeStampType = TIMESTAMP_TYPE_UNKNOWN;
        } else {
            this.mTimeStampType = timeStampType;
        }
    }

    public int hashCode() {
        return (((this.mRegistered ? TIMESTAMP_TYPE_UNKNOWN : TYPE_GSM) * 31) + (((int) (this.mTimeStamp / 1000)) * 31)) + (this.mTimeStampType * 31);
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        try {
            CellInfo o = (CellInfo) other;
            if (this.mRegistered != o.mRegistered || this.mTimeStamp != o.mTimeStamp) {
                z = false;
            } else if (this.mTimeStampType != o.mTimeStampType) {
                z = false;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    private static String timeStampTypeToString(int type) {
        switch (type) {
            case TYPE_GSM /*1*/:
                return "antenna";
            case TYPE_CDMA /*2*/:
                return "modem";
            case TYPE_LTE /*3*/:
                return "oem_ril";
            case TYPE_WCDMA /*4*/:
                return "java_ril";
            default:
                return SmsConstants.FORMAT_UNKNOWN;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mRegistered=").append(this.mRegistered ? "YES" : "NO");
        sb.append(" mTimeStampType=").append(timeStampTypeToString(this.mTimeStampType));
        sb.append(" mTimeStamp=").append(this.mTimeStamp).append("ns");
        return sb.toString();
    }

    public int describeContents() {
        return TIMESTAMP_TYPE_UNKNOWN;
    }

    protected void writeToParcel(Parcel dest, int flags, int type) {
        dest.writeInt(type);
        dest.writeInt(this.mRegistered ? TYPE_GSM : TIMESTAMP_TYPE_UNKNOWN);
        dest.writeInt(this.mTimeStampType);
        dest.writeLong(this.mTimeStamp);
    }

    protected CellInfo(Parcel in) {
        boolean z = true;
        if (in.readInt() != TYPE_GSM) {
            z = false;
        }
        this.mRegistered = z;
        this.mTimeStampType = in.readInt();
        this.mTimeStamp = in.readLong();
    }
}
