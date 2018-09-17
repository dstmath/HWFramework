package android.app.usage;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ConfigurationStats implements Parcelable {
    public static final Creator<ConfigurationStats> CREATOR = null;
    public int mActivationCount;
    public long mBeginTimeStamp;
    public Configuration mConfiguration;
    public long mEndTimeStamp;
    public long mLastTimeActive;
    public long mTotalTimeActive;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.usage.ConfigurationStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.usage.ConfigurationStats.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.usage.ConfigurationStats.<clinit>():void");
    }

    public ConfigurationStats(ConfigurationStats stats) {
        this.mConfiguration = stats.mConfiguration;
        this.mBeginTimeStamp = stats.mBeginTimeStamp;
        this.mEndTimeStamp = stats.mEndTimeStamp;
        this.mLastTimeActive = stats.mLastTimeActive;
        this.mTotalTimeActive = stats.mTotalTimeActive;
        this.mActivationCount = stats.mActivationCount;
    }

    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    public long getFirstTimeStamp() {
        return this.mBeginTimeStamp;
    }

    public long getLastTimeStamp() {
        return this.mEndTimeStamp;
    }

    public long getLastTimeActive() {
        return this.mLastTimeActive;
    }

    public long getTotalTimeActive() {
        return this.mTotalTimeActive;
    }

    public int getActivationCount() {
        return this.mActivationCount;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mConfiguration != null) {
            dest.writeInt(1);
            this.mConfiguration.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeLong(this.mBeginTimeStamp);
        dest.writeLong(this.mEndTimeStamp);
        dest.writeLong(this.mLastTimeActive);
        dest.writeLong(this.mTotalTimeActive);
        dest.writeInt(this.mActivationCount);
    }
}
