package android.app.backup;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public class RestoreSet implements Parcelable {
    public static final Parcelable.Creator<RestoreSet> CREATOR = new Parcelable.Creator<RestoreSet>() {
        /* class android.app.backup.RestoreSet.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RestoreSet createFromParcel(Parcel in) {
            return new RestoreSet(in);
        }

        @Override // android.os.Parcelable.Creator
        public RestoreSet[] newArray(int size) {
            return new RestoreSet[size];
        }
    };
    public String device;
    public String name;
    public long token;

    public RestoreSet() {
    }

    public RestoreSet(String _name, String _dev, long _token) {
        this.name = _name;
        this.device = _dev;
        this.token = _token;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.device);
        out.writeLong(this.token);
    }

    private RestoreSet(Parcel in) {
        this.name = in.readString();
        this.device = in.readString();
        this.token = in.readLong();
    }
}
