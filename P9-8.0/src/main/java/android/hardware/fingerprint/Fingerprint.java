package android.hardware.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class Fingerprint implements Parcelable {
    public static final Creator<Fingerprint> CREATOR = new Creator<Fingerprint>() {
        public Fingerprint createFromParcel(Parcel in) {
            return new Fingerprint(in, null);
        }

        public Fingerprint[] newArray(int size) {
            return new Fingerprint[size];
        }
    };
    private long mDeviceId;
    private int mFingerId;
    private int mGroupId;
    private CharSequence mName;

    /* synthetic */ Fingerprint(Parcel in, Fingerprint -this1) {
        this(in);
    }

    public Fingerprint(CharSequence name, int groupId, int fingerId, long deviceId) {
        this.mName = name;
        this.mGroupId = groupId;
        this.mFingerId = fingerId;
        this.mDeviceId = deviceId;
    }

    private Fingerprint(Parcel in) {
        this.mName = in.readString();
        this.mGroupId = in.readInt();
        this.mFingerId = in.readInt();
        this.mDeviceId = in.readLong();
    }

    public CharSequence getName() {
        return this.mName;
    }

    public int getFingerId() {
        return this.mFingerId;
    }

    public int getGroupId() {
        return this.mGroupId;
    }

    public long getDeviceId() {
        return this.mDeviceId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mName.toString());
        out.writeInt(this.mGroupId);
        out.writeInt(this.mFingerId);
        out.writeLong(this.mDeviceId);
    }
}
