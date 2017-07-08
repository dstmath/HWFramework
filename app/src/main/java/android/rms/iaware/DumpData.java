package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class DumpData implements Parcelable, Comparable<DumpData> {
    public static final Creator<DumpData> CREATOR = null;
    private int mExeTime;
    private int mFeatureId;
    private String mOperation;
    private long mOperationTimeStamp;
    private String mReason;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.DumpData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.DumpData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.DumpData.<clinit>():void");
    }

    public DumpData(long time, int featureid, String operation, int exetime, String reason) {
        this.mOperationTimeStamp = time;
        this.mFeatureId = featureid;
        this.mOperation = operation;
        this.mExeTime = exetime;
        this.mReason = reason;
    }

    public int compareTo(DumpData other) {
        if (other == null) {
            return 1;
        }
        if (this.mOperationTimeStamp < other.getTime()) {
            return -1;
        }
        if (this.mOperationTimeStamp > other.getTime()) {
            return 1;
        }
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (this == other) {
            return true;
        }
        DumpData otherData = (DumpData) other;
        if (this.mOperationTimeStamp != otherData.getTime() || this.mFeatureId != otherData.getFeatureId() || this.mExeTime != otherData.getExeTime() || ((this.mOperation == null || !this.mOperation.equals(otherData.getOperation())) && (this.mOperation != null || otherData.getOperation() != null))) {
            z = false;
        } else if ((this.mReason == null || !this.mReason.equals(otherData.getReson())) && !(this.mReason == null && otherData.getReson() == null)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int i2 = (((((int) (this.mOperationTimeStamp ^ (this.mOperationTimeStamp >>> 32))) + 31) * 31) + this.mFeatureId) * 31;
        if (this.mOperation != null) {
            hashCode = this.mOperation.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (((i2 + hashCode) * 31) + this.mExeTime) * 31;
        if (this.mReason != null) {
            i = this.mReason.hashCode();
        }
        return hashCode + i;
    }

    public long getTime() {
        return this.mOperationTimeStamp;
    }

    public void setTime(long time) {
        this.mOperationTimeStamp = time;
    }

    public int getFeatureId() {
        return this.mFeatureId;
    }

    public void setFeatureId(int featureId) {
        this.mFeatureId = featureId;
    }

    public String getOperation() {
        return this.mOperation;
    }

    public void setOperation(String operation) {
        this.mOperation = operation;
    }

    public String getReson() {
        return this.mReason;
    }

    public void setReason(String reason) {
        this.mReason = reason;
    }

    public int getExeTime() {
        return this.mExeTime;
    }

    public void setExeTime(int exetime) {
        this.mExeTime = exetime;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mOperationTimeStamp);
        dest.writeInt(this.mFeatureId);
        dest.writeString(this.mOperation);
        dest.writeInt(this.mExeTime);
        dest.writeString(this.mReason);
    }

    public int describeContents() {
        return 0;
    }
}
