package android.view;

import android.os.Parcel;
import android.os.Parcelable;

public final class InputChannel implements Parcelable {
    public static final Parcelable.Creator<InputChannel> CREATOR = new Parcelable.Creator<InputChannel>() {
        public InputChannel createFromParcel(Parcel source) {
            InputChannel result = new InputChannel();
            result.readFromParcel(source);
            return result;
        }

        public InputChannel[] newArray(int size) {
            return new InputChannel[size];
        }
    };
    private static final boolean DEBUG = false;
    private static final String TAG = "InputChannel";
    private long mPtr;

    private native void nativeDispose(boolean z);

    private native void nativeDup(InputChannel inputChannel);

    private native String nativeGetName();

    private static native InputChannel[] nativeOpenInputChannelPair(String str);

    private native void nativeReadFromParcel(Parcel parcel);

    private native void nativeTransferTo(InputChannel inputChannel);

    private native void nativeWriteToParcel(Parcel parcel);

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDispose(true);
        } finally {
            super.finalize();
        }
    }

    public static InputChannel[] openInputChannelPair(String name) {
        if (name != null) {
            return nativeOpenInputChannelPair(name);
        }
        throw new IllegalArgumentException("name must not be null");
    }

    public String getName() {
        String name = nativeGetName();
        return name != null ? name : "uninitialized";
    }

    public void dispose() {
        nativeDispose(false);
    }

    public void transferTo(InputChannel outParameter) {
        if (outParameter != null) {
            nativeTransferTo(outParameter);
            return;
        }
        throw new IllegalArgumentException("outParameter must not be null");
    }

    public InputChannel dup() {
        InputChannel target = new InputChannel();
        nativeDup(target);
        return target;
    }

    public int describeContents() {
        return 1;
    }

    public void readFromParcel(Parcel in) {
        if (in != null) {
            nativeReadFromParcel(in);
            return;
        }
        throw new IllegalArgumentException("in must not be null");
    }

    public void writeToParcel(Parcel out, int flags) {
        if (out != null) {
            nativeWriteToParcel(out);
            if ((flags & 1) != 0) {
                dispose();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("out must not be null");
    }

    public String toString() {
        return getName();
    }
}
