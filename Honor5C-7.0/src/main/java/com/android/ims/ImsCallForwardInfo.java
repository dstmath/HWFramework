package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ImsCallForwardInfo implements Parcelable {
    public static final Creator<ImsCallForwardInfo> CREATOR = null;
    public int mCondition;
    public int mEndHour;
    public int mEndMinute;
    public String mNumber;
    public int mServiceClass;
    public int mStartHour;
    public int mStartMinute;
    public int mStatus;
    public int mTimeSeconds;
    public int mToA;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsCallForwardInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsCallForwardInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallForwardInfo.<clinit>():void");
    }

    public ImsCallForwardInfo() {
        this.mStartHour = 0;
        this.mStartMinute = 0;
        this.mEndHour = 0;
        this.mEndMinute = 0;
    }

    public ImsCallForwardInfo(Parcel in) {
        this.mStartHour = 0;
        this.mStartMinute = 0;
        this.mEndHour = 0;
        this.mEndMinute = 0;
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCondition);
        out.writeInt(this.mStatus);
        out.writeInt(this.mToA);
        out.writeString(this.mNumber);
        out.writeInt(this.mTimeSeconds);
        out.writeInt(this.mServiceClass);
    }

    public String toString() {
        return super.toString() + ", Condition: " + this.mCondition + ", Status: " + (this.mStatus == 0 ? "disabled" : "enabled") + ", ToA: " + this.mToA + ", Service Class: " + this.mServiceClass + ", Number=" + this.mNumber + ", Time (seconds): " + this.mTimeSeconds + ", mStartHour=" + this.mStartHour + ", mStartMinute=" + this.mStartMinute + ", mEndHour=" + this.mEndHour + ", mEndMinute" + this.mEndMinute;
    }

    private void readFromParcel(Parcel in) {
        this.mCondition = in.readInt();
        this.mStatus = in.readInt();
        this.mToA = in.readInt();
        this.mNumber = in.readString();
        this.mTimeSeconds = in.readInt();
        this.mServiceClass = in.readInt();
    }
}
