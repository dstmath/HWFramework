package android.app;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class GrantedUriPermission implements Parcelable {
    public static final Parcelable.Creator<GrantedUriPermission> CREATOR = new Parcelable.Creator<GrantedUriPermission>() {
        public GrantedUriPermission createFromParcel(Parcel in) {
            return new GrantedUriPermission(in);
        }

        public GrantedUriPermission[] newArray(int size) {
            return new GrantedUriPermission[size];
        }
    };
    public final String packageName;
    public final Uri uri;

    public GrantedUriPermission(Uri uri2, String packageName2) {
        this.uri = uri2;
        this.packageName = packageName2;
    }

    public String toString() {
        return this.packageName + ":" + this.uri;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.uri, flags);
        out.writeString(this.packageName);
    }

    private GrantedUriPermission(Parcel in) {
        this.uri = (Uri) in.readParcelable(null);
        this.packageName = in.readString();
    }
}
