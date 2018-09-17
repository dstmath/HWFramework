package android.os;

import android.os.Parcelable.Creator;
import android.util.Log;
import com.android.internal.os.IShellCallback;
import com.android.internal.os.IShellCallback.Stub;

public class ShellCallback implements Parcelable {
    public static final Creator<ShellCallback> CREATOR = new Creator<ShellCallback>() {
        public ShellCallback createFromParcel(Parcel in) {
            return new ShellCallback(in);
        }

        public ShellCallback[] newArray(int size) {
            return new ShellCallback[size];
        }
    };
    static final boolean DEBUG = false;
    static final String TAG = "ShellCallback";
    final boolean mLocal = true;
    IShellCallback mShellCallback;

    class MyShellCallback extends Stub {
        MyShellCallback() {
        }

        public ParcelFileDescriptor openOutputFile(String path, String seLinuxContext) {
            return ShellCallback.this.onOpenOutputFile(path, seLinuxContext);
        }
    }

    public ParcelFileDescriptor openOutputFile(String path, String seLinuxContext) {
        if (this.mLocal) {
            return onOpenOutputFile(path, seLinuxContext);
        }
        if (this.mShellCallback != null) {
            try {
                return this.mShellCallback.openOutputFile(path, seLinuxContext);
            } catch (RemoteException e) {
                Log.w(TAG, "Failure opening " + path, e);
            }
        }
        return null;
    }

    public ParcelFileDescriptor onOpenOutputFile(String path, String seLinuxContext) {
        return null;
    }

    public static void writeToParcel(ShellCallback callback, Parcel out) {
        if (callback == null) {
            out.writeStrongBinder(null);
        } else {
            callback.writeToParcel(out, 0);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        synchronized (this) {
            if (this.mShellCallback == null) {
                this.mShellCallback = new MyShellCallback();
            }
            out.writeStrongBinder(this.mShellCallback.asBinder());
        }
    }

    ShellCallback(Parcel in) {
        this.mShellCallback = Stub.asInterface(in.readStrongBinder());
    }
}
