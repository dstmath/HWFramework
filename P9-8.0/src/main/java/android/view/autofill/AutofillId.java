package android.view.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class AutofillId implements Parcelable {
    public static final Creator<AutofillId> CREATOR = new Creator<AutofillId>() {
        public AutofillId createFromParcel(Parcel source) {
            return new AutofillId(source, null);
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
        return ((this.mViewId + 31) * 31) + this.mVirtualId;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AutofillId other = (AutofillId) obj;
        return this.mViewId == other.mViewId && this.mVirtualId == other.mVirtualId;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append(this.mViewId);
        if (this.mVirtual) {
            builder.append(':').append(this.mVirtualId);
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
        boolean z = true;
        this.mViewId = parcel.readInt();
        if (parcel.readInt() != 1) {
            z = false;
        }
        this.mVirtual = z;
        this.mVirtualId = parcel.readInt();
    }
}
