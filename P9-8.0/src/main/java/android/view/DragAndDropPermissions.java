package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.internal.view.IDragAndDropPermissions.Stub;

public final class DragAndDropPermissions implements Parcelable {
    public static final Creator<DragAndDropPermissions> CREATOR = new Creator<DragAndDropPermissions>() {
        public DragAndDropPermissions createFromParcel(Parcel source) {
            return new DragAndDropPermissions(source, null);
        }

        public DragAndDropPermissions[] newArray(int size) {
            return new DragAndDropPermissions[size];
        }
    };
    private final IDragAndDropPermissions mDragAndDropPermissions;
    private IBinder mTransientToken;

    /* synthetic */ DragAndDropPermissions(Parcel in, DragAndDropPermissions -this1) {
        this(in);
    }

    public static DragAndDropPermissions obtain(DragEvent dragEvent) {
        if (dragEvent.getDragAndDropPermissions() == null) {
            return null;
        }
        return new DragAndDropPermissions(dragEvent.getDragAndDropPermissions());
    }

    private DragAndDropPermissions(IDragAndDropPermissions dragAndDropPermissions) {
        this.mDragAndDropPermissions = dragAndDropPermissions;
    }

    public boolean take(IBinder activityToken) {
        try {
            this.mDragAndDropPermissions.take(activityToken);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean takeTransient() {
        try {
            this.mTransientToken = new Binder();
            this.mDragAndDropPermissions.takeTransient(this.mTransientToken);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void release() {
        try {
            this.mDragAndDropPermissions.release();
            this.mTransientToken = null;
        } catch (RemoteException e) {
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeStrongInterface(this.mDragAndDropPermissions);
        destination.writeStrongBinder(this.mTransientToken);
    }

    private DragAndDropPermissions(Parcel in) {
        this.mDragAndDropPermissions = Stub.asInterface(in.readStrongBinder());
        this.mTransientToken = in.readStrongBinder();
    }
}
