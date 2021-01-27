package android.app.slice;

import android.os.Parcel;
import android.os.Parcelable;

public final class SliceSpec implements Parcelable {
    public static final Parcelable.Creator<SliceSpec> CREATOR = new Parcelable.Creator<SliceSpec>() {
        /* class android.app.slice.SliceSpec.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SliceSpec createFromParcel(Parcel source) {
            return new SliceSpec(source);
        }

        @Override // android.os.Parcelable.Creator
        public SliceSpec[] newArray(int size) {
            return new SliceSpec[size];
        }
    };
    private final int mRevision;
    private final String mType;

    public SliceSpec(String type, int revision) {
        this.mType = type;
        this.mRevision = revision;
    }

    public SliceSpec(Parcel source) {
        this.mType = source.readString();
        this.mRevision = source.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mType);
        dest.writeInt(this.mRevision);
    }

    public String getType() {
        return this.mType;
    }

    public int getRevision() {
        return this.mRevision;
    }

    public boolean canRender(SliceSpec candidate) {
        if (this.mType.equals(candidate.mType) && this.mRevision >= candidate.mRevision) {
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SliceSpec)) {
            return false;
        }
        SliceSpec other = (SliceSpec) obj;
        if (!this.mType.equals(other.mType) || this.mRevision != other.mRevision) {
            return false;
        }
        return true;
    }

    public String toString() {
        return String.format("SliceSpec{%s,%d}", this.mType, Integer.valueOf(this.mRevision));
    }
}
