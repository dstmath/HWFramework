package android.content;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class UndoOperation<DATA> implements Parcelable {
    UndoOwner mOwner;

    public abstract void commit();

    public abstract void redo();

    public abstract void undo();

    public UndoOperation(UndoOwner owner) {
        this.mOwner = owner;
    }

    protected UndoOperation(Parcel src, ClassLoader loader) {
    }

    public UndoOwner getOwner() {
        return this.mOwner;
    }

    public DATA getOwnerData() {
        return this.mOwner.getData();
    }

    public boolean matchOwner(UndoOwner owner) {
        return owner == getOwner();
    }

    public boolean hasData() {
        return true;
    }

    public boolean allowMerge() {
        return true;
    }

    public int describeContents() {
        return 0;
    }
}
