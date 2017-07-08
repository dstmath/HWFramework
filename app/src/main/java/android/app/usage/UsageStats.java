package android.app.usage;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class UsageStats implements Parcelable {
    public static final Creator<UsageStats> CREATOR = null;
    public long mBeginTimeStamp;
    public long mEndTimeStamp;
    public long mLandTimeInForeground;
    public int mLastEvent;
    public long mLastLandTimeUsed;
    public long mLastTimeUsed;
    public int mLaunchCount;
    public String mPackageName;
    public long mTotalTimeInForeground;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.usage.UsageStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.usage.UsageStats.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.usage.UsageStats.<clinit>():void");
    }

    public UsageStats(UsageStats stats) {
        this.mPackageName = stats.mPackageName;
        this.mBeginTimeStamp = stats.mBeginTimeStamp;
        this.mEndTimeStamp = stats.mEndTimeStamp;
        this.mLastTimeUsed = stats.mLastTimeUsed;
        this.mTotalTimeInForeground = stats.mTotalTimeInForeground;
        this.mLaunchCount = stats.mLaunchCount;
        this.mLastEvent = stats.mLastEvent;
        this.mLandTimeInForeground = stats.mLandTimeInForeground;
        this.mLastLandTimeUsed = stats.mLastLandTimeUsed;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public long getFirstTimeStamp() {
        return this.mBeginTimeStamp;
    }

    public long getLastTimeStamp() {
        return this.mEndTimeStamp;
    }

    public long getLastTimeUsed() {
        return this.mLastTimeUsed;
    }

    public long getTotalTimeInForeground() {
        return this.mTotalTimeInForeground;
    }

    public long getLandTimeInForeground() {
        return this.mLandTimeInForeground;
    }

    public long getLastLandTimeUsed() {
        return this.mLastLandTimeUsed;
    }

    public void add(UsageStats right) {
        if (this.mPackageName.equals(right.mPackageName)) {
            if (right.mBeginTimeStamp > this.mBeginTimeStamp) {
                this.mLastEvent = right.mLastEvent;
                this.mLastTimeUsed = right.mLastTimeUsed;
                this.mLastLandTimeUsed = right.mLastLandTimeUsed;
            }
            this.mBeginTimeStamp = Math.min(this.mBeginTimeStamp, right.mBeginTimeStamp);
            this.mEndTimeStamp = Math.max(this.mEndTimeStamp, right.mEndTimeStamp);
            this.mTotalTimeInForeground += right.mTotalTimeInForeground;
            this.mLaunchCount += right.mLaunchCount;
            this.mLandTimeInForeground += right.mLandTimeInForeground;
            return;
        }
        throw new IllegalArgumentException("Can't merge UsageStats for package '" + this.mPackageName + "' with UsageStats for package '" + right.mPackageName + "'.");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeLong(this.mBeginTimeStamp);
        dest.writeLong(this.mEndTimeStamp);
        dest.writeLong(this.mLastTimeUsed);
        dest.writeLong(this.mTotalTimeInForeground);
        dest.writeInt(this.mLaunchCount);
        dest.writeInt(this.mLastEvent);
        dest.writeLong(this.mLandTimeInForeground);
        dest.writeLong(this.mLastLandTimeUsed);
    }
}
