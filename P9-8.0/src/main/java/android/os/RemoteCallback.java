package android.os;

import android.os.IRemoteCallback.Stub;
import android.os.Parcelable.Creator;

public final class RemoteCallback implements Parcelable {
    public static final Creator<RemoteCallback> CREATOR = new Creator<RemoteCallback>() {
        public RemoteCallback createFromParcel(Parcel parcel) {
            return new RemoteCallback(parcel);
        }

        public RemoteCallback[] newArray(int size) {
            return new RemoteCallback[size];
        }
    };
    private final IRemoteCallback mCallback;
    private final Handler mHandler;
    private final OnResultListener mListener;

    public interface OnResultListener {
        void onResult(Bundle bundle);
    }

    public RemoteCallback(OnResultListener listener) {
        this(listener, null);
    }

    public RemoteCallback(OnResultListener listener, Handler handler) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }
        this.mListener = listener;
        this.mHandler = handler;
        this.mCallback = new Stub() {
            public void sendResult(Bundle data) {
                RemoteCallback.this.sendResult(data);
            }
        };
    }

    RemoteCallback(Parcel parcel) {
        this.mListener = null;
        this.mHandler = null;
        this.mCallback = Stub.asInterface(parcel.readStrongBinder());
    }

    public void sendResult(final Bundle result) {
        if (this.mListener == null) {
            try {
                this.mCallback.sendResult(result);
            } catch (RemoteException e) {
            }
        } else if (this.mHandler != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    RemoteCallback.this.mListener.onResult(result);
                }
            });
        } else {
            this.mListener.onResult(result);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStrongBinder(this.mCallback.asBinder());
    }
}
