package android.os;

import android.os.Parcelable;
import android.util.Log;
import com.android.internal.os.IShellCallback;

public class ShellCallback implements Parcelable {
    public static final Parcelable.Creator<ShellCallback> CREATOR = new Parcelable.Creator<ShellCallback>() {
        /* class android.os.ShellCallback.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ShellCallback createFromParcel(Parcel in) {
            return new ShellCallback(in);
        }

        @Override // android.os.Parcelable.Creator
        public ShellCallback[] newArray(int size) {
            return new ShellCallback[size];
        }
    };
    static final boolean DEBUG = false;
    static final String TAG = "ShellCallback";
    final boolean mLocal = true;
    IShellCallback mShellCallback;

    /* access modifiers changed from: package-private */
    public class MyShellCallback extends IShellCallback.Stub {
        MyShellCallback() {
        }

        @Override // com.android.internal.os.IShellCallback
        public ParcelFileDescriptor openFile(String path, String seLinuxContext, String mode) {
            return ShellCallback.this.onOpenFile(path, seLinuxContext, mode);
        }
    }

    public ShellCallback() {
    }

    public ParcelFileDescriptor openFile(String path, String seLinuxContext, String mode) {
        if (this.mLocal) {
            return onOpenFile(path, seLinuxContext, mode);
        }
        IShellCallback iShellCallback = this.mShellCallback;
        if (iShellCallback == null) {
            return null;
        }
        try {
            return iShellCallback.openFile(path, seLinuxContext, mode);
        } catch (RemoteException e) {
            Log.w(TAG, "Failure opening " + path, e);
            return null;
        }
    }

    public ParcelFileDescriptor onOpenFile(String path, String seLinuxContext, String mode) {
        return null;
    }

    public static void writeToParcel(ShellCallback callback, Parcel out) {
        if (callback == null) {
            out.writeStrongBinder(null);
        } else {
            callback.writeToParcel(out, 0);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        synchronized (this) {
            if (this.mShellCallback == null) {
                this.mShellCallback = new MyShellCallback();
            }
            out.writeStrongBinder(this.mShellCallback.asBinder());
        }
    }

    ShellCallback(Parcel in) {
        this.mShellCallback = IShellCallback.Stub.asInterface(in.readStrongBinder());
        IShellCallback iShellCallback = this.mShellCallback;
        if (iShellCallback != null) {
            Binder.allowBlocking(iShellCallback.asBinder());
        }
    }
}
