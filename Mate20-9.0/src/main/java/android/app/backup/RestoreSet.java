package android.app.backup;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public class RestoreSet implements Parcelable {
    public static final Parcelable.Creator<RestoreSet> CREATOR = new Parcelable.Creator<RestoreSet>() {
        public RestoreSet createFromParcel(Parcel in) {
            return new RestoreSet(in);
        }

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

    public int describeContents() {
        return 0;
    }

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
