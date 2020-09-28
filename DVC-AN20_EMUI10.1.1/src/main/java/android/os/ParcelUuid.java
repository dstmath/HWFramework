package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcelable;
import java.util.UUID;

public final class ParcelUuid implements Parcelable {
    public static final Parcelable.Creator<ParcelUuid> CREATOR = new Parcelable.Creator<ParcelUuid>() {
        /* class android.os.ParcelUuid.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        @UnsupportedAppUsage
        public ParcelUuid createFromParcel(Parcel source) {
            return new ParcelUuid(new UUID(source.readLong(), source.readLong()));
        }

        @Override // android.os.Parcelable.Creator
        public ParcelUuid[] newArray(int size) {
            return new ParcelUuid[size];
        }
    };
    private final UUID mUuid;

    public ParcelUuid(UUID uuid) {
        this.mUuid = uuid;
    }

    public static ParcelUuid fromString(String uuid) {
        return new ParcelUuid(UUID.fromString(uuid));
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public String toString() {
        return this.mUuid.toString();
    }

    public int hashCode() {
        return this.mUuid.hashCode();
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!(object instanceof ParcelUuid)) {
            return false;
        }
        return this.mUuid.equals(((ParcelUuid) object).mUuid);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUuid.getMostSignificantBits());
        dest.writeLong(this.mUuid.getLeastSignificantBits());
    }
}
