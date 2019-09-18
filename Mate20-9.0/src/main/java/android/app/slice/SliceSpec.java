package android.app.slice;

import android.os.Parcel;
import android.os.Parcelable;

public final class SliceSpec implements Parcelable {
    public static final Parcelable.Creator<SliceSpec> CREATOR = new Parcelable.Creator<SliceSpec>() {
        public SliceSpec createFromParcel(Parcel source) {
            return new SliceSpec(source);
        }

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

    public int describeContents() {
        return 0;
    }

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
        boolean z = false;
        if (!this.mType.equals(candidate.mType)) {
            return false;
        }
        if (this.mRevision >= candidate.mRevision) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof SliceSpec)) {
            return false;
        }
        SliceSpec other = (SliceSpec) obj;
        if (this.mType.equals(other.mType) && this.mRevision == other.mRevision) {
            z = true;
        }
        return z;
    }

    public String toString() {
        return String.format("SliceSpec{%s,%d}", new Object[]{this.mType, Integer.valueOf(this.mRevision)});
    }
}
