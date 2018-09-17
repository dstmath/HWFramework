package android.rms.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CollectData implements Parcelable {
    public static final Creator<CollectData> CREATOR = null;
    private Bundle mBundle;
    private String mData;
    private int mResId;
    private long mTimeStamp;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.CollectData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.CollectData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.CollectData.<clinit>():void");
    }

    public CollectData(int resid, long timestamp, String data) {
        this(resid, timestamp, data, null);
    }

    public CollectData(int resid, long timeStamp, Bundle bundle) {
        this(resid, timeStamp, null, bundle);
    }

    public CollectData(int resid, long timeStamp, String strData, Bundle bundleData) {
        this.mResId = resid;
        this.mTimeStamp = timeStamp;
        this.mData = strData;
        this.mBundle = bundleData;
    }

    public int getResId() {
        return this.mResId;
    }

    public void setResId(int mResId) {
        this.mResId = mResId;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(long mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }

    public String getData() {
        return this.mData;
    }

    public void setData(String mData) {
        this.mData = mData;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void setBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public String toString() {
        return "[mResId=" + this.mResId + ", mTimeStamp=" + this.mTimeStamp + ", mData=" + this.mData + ", mBundle=" + this.mBundle + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResId);
        dest.writeLong(this.mTimeStamp);
        dest.writeString(this.mData);
        dest.writeBundle(this.mBundle);
    }
}
