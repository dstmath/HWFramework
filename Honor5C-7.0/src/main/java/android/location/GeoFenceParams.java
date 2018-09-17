package android.location;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.PrintWriter;

public class GeoFenceParams implements Parcelable {
    public static final Creator<GeoFenceParams> CREATOR = null;
    public static final int ENTERING = 1;
    public static final int LEAVING = 2;
    public final long mExpiration;
    public final PendingIntent mIntent;
    public final double mLatitude;
    public final double mLongitude;
    public final String mPackageName;
    public final float mRadius;
    public final int mUid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GeoFenceParams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GeoFenceParams.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.GeoFenceParams.<clinit>():void");
    }

    public GeoFenceParams(double lat, double lon, float r, long expire, PendingIntent intent, String packageName) {
        this(Binder.getCallingUid(), lat, lon, r, expire, intent, packageName);
    }

    public GeoFenceParams(int uid, double lat, double lon, float r, long expire, PendingIntent intent, String packageName) {
        this.mUid = uid;
        this.mLatitude = lat;
        this.mLongitude = lon;
        this.mRadius = r;
        this.mExpiration = expire;
        this.mIntent = intent;
        this.mPackageName = packageName;
    }

    private GeoFenceParams(Parcel in) {
        this.mUid = in.readInt();
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mRadius = in.readFloat();
        this.mExpiration = in.readLong();
        this.mIntent = (PendingIntent) in.readParcelable(null);
        this.mPackageName = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUid);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeFloat(this.mRadius);
        dest.writeLong(this.mExpiration);
        dest.writeParcelable(this.mIntent, 0);
        dest.writeString(this.mPackageName);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GeoFenceParams:\n\tmUid - ");
        sb.append(this.mUid);
        sb.append("\n\tmLatitide - ");
        sb.append(this.mLatitude);
        sb.append("\n\tmLongitude - ");
        sb.append(this.mLongitude);
        sb.append("\n\tmRadius - ");
        sb.append(this.mRadius);
        sb.append("\n\tmExpiration - ");
        sb.append(this.mExpiration);
        sb.append("\n\tmIntent - ");
        sb.append(this.mIntent);
        return sb.toString();
    }

    public long getExpiration() {
        return this.mExpiration;
    }

    public PendingIntent getIntent() {
        return this.mIntent;
    }

    public int getCallerUid() {
        return this.mUid;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + this);
        pw.println(prefix + "mLatitude=" + this.mLatitude + " mLongitude=" + this.mLongitude);
        pw.println(prefix + "mRadius=" + this.mRadius + " mExpiration=" + this.mExpiration);
    }
}
