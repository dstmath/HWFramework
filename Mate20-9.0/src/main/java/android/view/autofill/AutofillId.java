package android.view.autofill;

import android.os.Parcel;
import android.os.Parcelable;

public final class AutofillId implements Parcelable {
    public static final Parcelable.Creator<AutofillId> CREATOR = new Parcelable.Creator<AutofillId>() {
        public AutofillId createFromParcel(Parcel source) {
            return new AutofillId(source);
        }

        public AutofillId[] newArray(int size) {
            return new AutofillId[size];
        }
    };
    private final int mViewId;
    private final boolean mVirtual;
    private final int mVirtualId;

    public AutofillId(int id) {
        this.mVirtual = false;
        this.mViewId = id;
        this.mVirtualId = -1;
    }

    public AutofillId(AutofillId parent, int virtualChildId) {
        this.mVirtual = true;
        this.mViewId = parent.mViewId;
        this.mVirtualId = virtualChildId;
    }

    public AutofillId(int parentId, int virtualChildId) {
        this.mVirtual = true;
        this.mViewId = parentId;
        this.mVirtualId = virtualChildId;
    }

    public int getViewId() {
        return this.mViewId;
    }

    public int getVirtualChildId() {
        return this.mVirtualId;
    }

    public boolean isVirtual() {
        return this.mVirtual;
    }

    public int hashCode() {
        return (31 * ((31 * 1) + this.mViewId)) + this.mVirtualId;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AutofillId other = (AutofillId) obj;
        if (this.mViewId == other.mViewId && this.mVirtualId == other.mVirtualId) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append(this.mViewId);
        if (this.mVirtual) {
            builder.append(':');
            builder.append(this.mVirtualId);
        }
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mViewId);
        parcel.writeInt(this.mVirtual ? 1 : 0);
        parcel.writeInt(this.mVirtualId);
    }

    private AutofillId(Parcel parcel) {
        this.mViewId = parcel.readInt();
        this.mVirtual = parcel.readInt() != 1 ? false : true;
        this.mVirtualId = parcel.readInt();
    }
}
