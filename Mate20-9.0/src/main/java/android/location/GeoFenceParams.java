package android.location;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.PrintWriter;

public class GeoFenceParams implements Parcelable {
    public static final Parcelable.Creator<GeoFenceParams> CREATOR = new Parcelable.Creator<GeoFenceParams>() {
        public GeoFenceParams createFromParcel(Parcel in) {
            return new GeoFenceParams(in);
        }

        public GeoFenceParams[] newArray(int size) {
            return new GeoFenceParams[size];
        }
    };
    public static final int ENTERING = 1;
    public static final int LEAVING = 2;
    public final long mExpiration;
    public final PendingIntent mIntent;
    public final double mLatitude;
    public final double mLongitude;
    public final String mPackageName;
    public final float mRadius;
    public final int mUid;

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
        return "GeoFenceParams:\n\tmUid - " + this.mUid + "\n\tmLatitide - " + this.mLatitude + "\n\tmLongitude - " + this.mLongitude + "\n\tmRadius - " + this.mRadius + "\n\tmExpiration - " + this.mExpiration + "\n\tmIntent - " + this.mIntent;
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
